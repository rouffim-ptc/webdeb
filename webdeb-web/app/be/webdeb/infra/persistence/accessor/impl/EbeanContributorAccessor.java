/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see COPYING file).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package be.webdeb.infra.persistence.accessor.impl;

import akka.actor.ActorSystem;
import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.picture.ContributorPictureSource;
import be.webdeb.core.api.contributor.picture.PictureLicenceType;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.api.contributor.Advice;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.OutdatedVersionException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.persistence.Exportable;
import be.webdeb.infra.persistence.accessor.api.APIObjectMapper;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Contribution;
import be.webdeb.infra.persistence.model.Contributor;
import be.webdeb.infra.persistence.model.Group;
import be.webdeb.infra.persistence.model.TmpContributor;
import be.webdeb.infra.ws.external.auth.TokenGenerator;
import be.webdeb.util.ValuesHelper;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import play.Configuration;
import scala.concurrent.duration.Duration;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class implements the accessor and wrapping facility for webdeb contributors
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanContributorAccessor implements ContributorAccessor {

  @Inject
  protected ContributorFactory factory;

  @Inject
  protected ValuesHelper values;

  @Inject
  protected FileSystem files;

  private APIObjectMapper mapper;

  private static final Logger logger = play.Logger.underlying();

  private static final long DEFAULT_CONTRIBUTOR = 0L;
  private static final int DEFAULT_GROUP = 0;

  // Reset confirmation tokens will expire after 15 days.
  private static final int CONFIRMATION_EXPIRATION_DAYS = 15;
  // Reset auth tokens will expire after 60 days.
  private static final int AUTH_EXPIRATION_DAYS = 60;
  // A auth token must be reloaded after 3 days if it often used.
  private static final int AUTH_RELOAD_DAYS = 3;

  // Threshold in days to cleanup unvalidated contributors
  private static final int CLEANUP_DAYS = 30;

  private List<GroupColor> groupColors = null;
  private List<ModelDescription> modelDescription = null;

  private List<ContributorPictureSource> contributorPictureSources= null;
  private List<PictureLicenceType> pictureLicenceTypes = null;

  // string constant for logs
  private static final String CONTRIBUTOR_NOT_FOUND = "no contributor found for ";
  private static final String UNABLE_RETRIEVE_CONTRIBUTOR = "unable to retrieve contributor ";
  private static final String UNABLE_RETRIEVE_GROUP = "unable to retrieve group ";
  private static final String TRY_TO_DELETE = "try to delete ";
  private static final String FROM_GROUP = " from group ";

  /**
   * Injected constructor
   *
   * @param mapper the object mapper
   * @param akka akka agent system to start cronjob for contributor cleaning
   * @param configuration the configuration system
   */
  @Inject
  public EbeanContributorAccessor(APIObjectMapper mapper, ActorSystem akka, Configuration configuration) {
    this.mapper = mapper;

    // launch once a day cleaning of non validated a accounts
    if (configuration.getBoolean("cleanup.scheduler.run")) {
      akka.scheduler().schedule(
          Duration.create(configuration.getInt("cleanup.scheduler.start"), TimeUnit.SECONDS),
          Duration.create(configuration.getInt("cleanup.scheduler.delay"), TimeUnit.HOURS),
          this::removeUnvalidatedContributors,
          akka.dispatcher()
      );
    }
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor authenticate(String emailOrPseudo, String password) throws TokenExpiredException {
    be.webdeb.core.api.contributor.Contributor contributor = retrieve(emailOrPseudo);
    if (contributor != null) {
      if(contributor.getTmpContributor() == null || !contributor.getTmpContributor().getProject().isInProgress()) {
        if (contributor.hasConfirmationTokenExpired()) {
          logger.debug("token for user " + emailOrPseudo + " has expired");
          throw new TokenExpiredException();
        }
        if (contributor.isValidated() && checkPassword(password, contributor.getPassword())) {
          return contributor;
        }
      }else if (checkPassword(password, contributor.getPassword())) {
        return contributor;
      }
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + "email / pseudo or password do not match: " + emailOrPseudo);
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.TmpContributor tmpauthenticate(String pseudo, String password) throws TokenExpiredException {
    be.webdeb.core.api.contributor.TmpContributor contributor = retrieveTmp(pseudo);
    if (contributor != null && contributor.getProject().isInProgress() && checkPassword(password, contributor.getPassword())) {
      return contributor;
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + "pseudo tmp or password do not match: " + pseudo);
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor tokenAuthentication(String emailOrPseudo, String password) {
    be.webdeb.core.api.contributor.Contributor contributor = retrieve(emailOrPseudo);
    if (contributor != null) {
      if (contributor.isValid() && checkPassword(password, contributor.getPassword())) {
        try {
          contributor.newAuthToken();
          return contributor;
        }catch (Exception e){
          logger.error("Auth token problem ", e);
        }
      }
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + "email / pseudo or password do not match: " + emailOrPseudo);
    return null;
  }

  @Override
  public ContributorPicture retrieveContributorPicture(Long id) {
    // retrieve object
    be.webdeb.infra.persistence.model.ContributorPicture picture =
            be.webdeb.infra.persistence.model.ContributorPicture.findById(id);

    // if we got one
    if (picture != null) {
      try {
        return mapper.toContributorPicture(picture);
      } catch (FormatException e) {
        logger.error("unable to cast generic contributor picture " + id, e);
      }
    }
    // not found
    return null;
  }

  @Override
  public boolean checkAuthTokenValidity(String emailOrPseudo, String token) {
    Contributor contributor = (values.isEmail(emailOrPseudo) ?
            Contributor.findByEmailAndAuthToken(emailOrPseudo, token) : Contributor.findByPseudoAndAuthToken(emailOrPseudo, token));
    return isContributorValid(contributor) && checkAuthTokenValidity(contributor);
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieve(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      try {
        return mapper.toContributor(c);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved contributor " + emailOrPseudo, e);
      }
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieve(Long id) {
    be.webdeb.infra.persistence.model.Contributor c = be.webdeb.infra.persistence.model.Contributor.findById(id);
    if (c != null) {
      try {
        return mapper.toContributor(c);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved contributor " + id, e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + id);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieve(String openid, EOpenIdType openidType) {
    be.webdeb.infra.persistence.model.Contributor c = be.webdeb.infra.persistence.model.Contributor.findByOpenId(openid, openidType);
    if (c != null) {
      try {
        return mapper.toContributor(c);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved contributor with openid " + openid, e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + openid);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.TmpContributor retrieveTmp(Long id) {
    be.webdeb.infra.persistence.model.TmpContributor c = be.webdeb.infra.persistence.model.TmpContributor.findById(id);
    if (c != null) {
      try {
        return mapper.toTmpContributor(c);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved contributor " + id, e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + id);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.TmpContributor retrieveTmp(String pseudo) {
    TmpContributor c = TmpContributor.findByPseudo(pseudo);
    if (c != null) {
      try {
        return mapper.toTmpContributor(c);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved tmpcontributor " + pseudo, e);
      }
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + pseudo);
    return null;
  }

  @Override
  public String newConfirmationToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      try {
        return newConfirmationToken(c);
      } catch (Exception e) {
        logger.error("unable to save new token for " + emailOrPseudo, e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    }
    return null;
  }

  @Override
  public String getConfirmationToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return c.getConfirmationToken();
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return null;
  }

  @Override
  public boolean isConfirmationTokenExpired(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return isConfirmationTokenExpired(c);
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return false;
  }

  @Override
  public String newNewsletterToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      try {
        return newNewsletterToken(c);
      } catch (Exception e) {
        logger.error("unable to save new token for " + emailOrPseudo, e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    }
    return null;
  }

  @Override
  public String getNewsletterToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return c.getNewsletterToken() == null || isNewsletterTokenExpired(emailOrPseudo) ? newNewsletterToken(emailOrPseudo) : c.getNewsletterToken();
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return null;
  }

  @Override
  public boolean isNewsletterTokenExpired(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return c.getNewsletterToken() == null || c.getNewsletterTokenExpirationDate() == null || new Date().after(c.getNewsletterTokenExpirationDate());
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return false;
  }

  @Override
  public String newAuthToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      Calendar calendar = Calendar.getInstance();

      if(c.getAuthTokenExpirationDate() != null) {
        calendar.setTime(c.getAuthTokenExpirationDate());
        calendar.add(Calendar.DAY_OF_YEAR, - AUTH_RELOAD_DAYS);
      }

      if(c.getAuthToken() == null || c.getAuthTokenExpirationDate() == null ||
              c.getAuthToken().equals("") || new Date().after(calendar.getTime())) {
        try {
          String token = createAuthToken();
          c.setAuthToken(token);
          setAuthTokenNewExpirationDate(c);
          c.update();
          return token;
        } catch (Exception e) {
          logger.error("unable to save new auth token for " + emailOrPseudo, e);
        }
      }else{
        return c.getAuthToken();
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    }
    return null;
  }

  @Override
  public String getAuthToken(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return c.getAuthToken();
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return null;
  }

  @Override
  public boolean isAuthTokenExpired(String emailOrPseudo) {
    Contributor c = findContributorByEmailOrPseudo(emailOrPseudo);
    if (c != null) {
      return checkAuthTokenValidity(c);
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + emailOrPseudo);
    return false;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieveByAuthToken(String email, String token) {
    Contributor contributor = Contributor.findByAuthToken(email, token);
    if (contributor != null && checkAuthTokenValidity(contributor)) {
      try {
        return mapper.toContributor(contributor);
      } catch (FormatException e) {
        logger.error("unparsable contributor " + contributor.getEmail(), e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + "token " + token);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieveByConfirmationToken(String token) {
    Contributor contributor = Contributor.findByConfirmationToken(token);
    if (contributor != null) {
      try {
        return mapper.toContributor(contributor);
      } catch (FormatException e) {
        logger.error("unparsable contributor " + contributor.getEmail(), e);
      }
    } else {
      logger.warn(CONTRIBUTOR_NOT_FOUND + "token " + token);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor retrieveByInvitation(String token) {
    Contributor contributor = ContributorHasGroup.byInvitation(token);
    if (contributor != null) {
      try {
        return mapper.toContributor(contributor);
      } catch (FormatException e) {
        logger.error("unparsable contributor " + contributor.getEmail(), e);
      }
    } else {
      logger.warn("no contributor found (or missmatch with password) for invitation " + token);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.contributor.Contributor getDefaultContributor() {
    // get default contributor
    Contributor contributor = Contributor.findById(DEFAULT_CONTRIBUTOR);
    try {
      return mapper.toContributor(contributor);
    } catch (FormatException e) {
      logger.error("unable to create default contributor", e);
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.contributor.Contributor> findContributorsByRole(EContributorRole role) {
    List<Contributor> contributors = Contributor.findContributorsByRole(role);
    List<be.webdeb.core.api.contributor.Contributor> result = new ArrayList<>();
    for (Contributor c : contributors) {
      try {
        result.add(mapper.toContributor(c));
      } catch (FormatException e) {
        logger.error("unparseable contributor" + c.getEmail(), e);
      }
    }
    return result;
  }

  @Override
  public List<be.webdeb.core.api.contributor.Contributor> findContributors(String query) {
    return findContributors(query, 0, 0);
  }

  @Override
  public List<be.webdeb.core.api.contributor.Contributor> findContributors(String query, int fromIndex, int toIndex) {
    List<Contributor> contributors = Contributor.findByNameOrEmail(query, fromIndex, toIndex);
    List<be.webdeb.core.api.contributor.Contributor> result = new ArrayList<>();
    for (Contributor c : contributors) {
      try {
        result.add(mapper.toContributor(c));
      } catch (FormatException e) {
        logger.error("unparseable contributor" + c.getEmail(), e);
      }
    }
    return result;
  }

  @Override
  public void changePassword(Long contributor, String password) throws PersistenceException {
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      throw new ObjectNotFoundException(be.webdeb.core.api.contributor.Contributor.class, contributor);
    }
    try {
      c.setPasswordHash(hashPassword(password));
      c.setConfirmationToken(null);
      c.setValidated(true);
      c.update();
      logger.info("password changed for " + c.getEmail());
    } catch (Exception e) {
      logger.error("unable to change user password" + c.getEmail(), e);
      throw new PersistenceException(PersistenceException.Key.UPDATE_CONTRIBUTOR, e);
    }
  }

  @Override
  public void validate(Long contributor, String email) throws PersistenceException, TokenExpiredException {
    logger.debug("confirmation received from " + email + ". Will activate contributor");
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      throw new ObjectNotFoundException(be.webdeb.core.api.contributor.Contributor.class, contributor);
    }
    if (isConfirmationTokenExpired(c)) {
      logger.warn("token for " + email + "[id:" + contributor + "] has expired");
      throw new TokenExpiredException();
    }

    try {
      c.setEmail(email);
      c.setConfirmationToken(null);
      c.setValidated(true);
      c.update();
    } catch (Exception e) {
      logger.error("unable to validate user " + c.getEmail(), e);
      throw new PersistenceException(PersistenceException.Key.UPDATE_CONTRIBUTOR, e);
    }
  }

  @Override
  public List<be.webdeb.core.api.contribution.Contribution> getContributions(Long contributor, int group, int amount) {
    Contributor cor = Contributor.findById(contributor);
    if (cor != null) {
      return toContributions(cor.getContributions(group, 0, amount));
    }
    return new ArrayList<>();
  }

  @Override
  public void save(be.webdeb.core.api.contributor.Contributor contributor, int currentGroup) throws PersistenceException {
    logger.debug("try to save contributor " + contributor.getEmail() + " with id " + contributor.getId() + " in group " + currentGroup);

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Contributor c = Contributor.findById(contributor.getId());

      // check version
      if (c != null && c.getVersion().getTime() > contributor.getVersion()) {
        logger.error("old version for given object " + contributor.getId()
            + " actual is " + c.getVersion().getTime() + " (" + new Date(c.getVersion().getTime()) + ") and yours is "
            + contributor.getVersion() + " (" + new Date(contributor.getVersion()) + ")");
        be.webdeb.core.api.contributor.Contributor newOne = null;
        try {
          newOne = mapper.toContributor(c);
        } catch (FormatException e) {
          logger.error("unable to cast retrieved contribution " + c.getIdContributor(), e);
        }
        throw new OutdatedVersionException(contributor, newOne);
      }

      try {
        Group defaultGroup = null;
        if (c == null) {
          // does not exist yet
          c = new Contributor();
          // auto-increment
          c.setIdContributor(0L);
          // generate auth token
          c.setAuthToken(createAuthToken());
          // generate confirmation token
          c.setConfirmationToken(UUID.randomUUID().toString());
          // set registration date
          c.setRegistrationDate(new Timestamp(new Date().getTime()));
          // set auth token expiration date (the current date + 14 days)
          setAuthTokenNewExpirationDate(c);
          // browser warned
          c.setBrowserWarned(contributor.isBrowserWarned());

          if(!values.isBlank(contributor.getPassword())) {
            // password is in clear
            c.setPasswordHash(hashPassword(contributor.getPassword()));
          }

          // assign to default PUBLIC group
          defaultGroup = Group.findById(DEFAULT_GROUP);

          // set default group
          c.setDefaultGroup(defaultGroup);

        }

        // check if this user has been invited, if so, clear invitation
        // password may be null in case of update of existing (update password needs a dedicated call to changePassword
        if(!values.isBlank(c.getPasswordHash())) {
          Contributor invited = ContributorHasGroup.byInvitation(c.getPasswordHash());
          if (invited != null && contributor.getPassword() != null) {
            c.setPasswordHash(hashPassword(contributor.getPassword()));
            ContributorHasGroup chg = ContributorHasGroup.byContributorAndGroup(c.getIdContributor(), c.getDefaultGroup().getIdGroup());
            if (chg != null) {
              chg.setInvitation(null);
              chg.update();
            }
          }
        }

        // update other fields
        c = updateContributor(contributor, c);
        c.setResidence(contributor.getResidence() != null ?
            TCountry.find.byId(contributor.getResidence().getCode()) : null);

        c.setBanned(contributor.isBanned());
        c.save();

        if(contributor.getTmpContributor() != null){
          TmpContributor tmpContributor = TmpContributor.findByPseudo(contributor.getTmpContributor().getPseudo());
          if(tmpContributor != null){
            boolean defaultGroupChanged = false;

            for(Group group : tmpContributor.getProjectSubgroup().getContributorGroups()){
              if(!defaultGroupChanged){
                c.setDefaultGroup(group);
                c.update();
                defaultGroupChanged = true;
              }
              group.addMember(c, Role.find.byId(EContributorRole.CONTRIBUTOR.id()), null);
              group.update();
            }

            c.setTmpContributor(tmpContributor);
            c.update();
          }
        }

        if (defaultGroup != null) {
          defaultGroup.addMember(c, Role.find.byId(EContributorRole.CONTRIBUTOR.id()), null);
          defaultGroup.update();
        }
      } catch (Exception e) {
        logger.error("error while updating contributor " + contributor.getEmail(), e);
        throw new PersistenceException(contributor.getId() == null || contributor.getId() == -1L ?
            PersistenceException.Key.SAVE_CONTRIBUTOR : PersistenceException.Key.UPDATE_CONTRIBUTOR, e);
      }

      // update id of given contributor (if non existing before)
      contributor.setId(c.getIdContributor());

      // affiliation to actors
      Map<Long, ContributorHasAffiliation> previous = c.getContributorHasAffiliations().stream()
          .collect(Collectors.toMap(ContributorHasAffiliation::getIdCha, a -> a));

      try {
        for (Affiliation aff : contributor.getAffiliations()) {
          // aff id has been created as a side effect
          previous.remove(aff.getId());
          aff.save(contributor.getId(), currentGroup);
        }
      } catch (PermissionException | FormatException e) {
        logger.error("unable to save affiliation", e);
        throw new PersistenceException(PersistenceException.Key.UPDATE_CONTRIBUTOR, e);
      }
      // now delete all affiliations that have not been submitted
      previous.values().forEach(aff -> {
        logger.debug("will delete affiliation with " + aff.toString());
        aff.delete();
      });

      logger.info("saved contributor " + contributor.toString());
      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Override
  public void save(be.webdeb.core.api.project.Project project, be.webdeb.core.api.project.ProjectSubgroup subgroup, String idTmpContributor) throws PersistenceException{
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      String pseudo = project.getTechnicalName() + "_" + subgroup.getTechnicalName() + "_" + idTmpContributor;
      Contributor c = Contributor.findByPseudo(pseudo);
      Project p = Project.findById(project.getId());
      ProjectSubgroup sub = ProjectSubgroup.findById(subgroup.getId());
      ProjectGroup g = sub != null ? ProjectGroup.findById(sub.getProjectGroup().getIdProjectGroup()) : null;

      // check if contributor already exists
      if (c != null) {
        logger.error("contributor already exists " + c.getIdContributor());
        throw new PersistenceException(PersistenceException.Key.SAVE_TMP_CONTRIBUTOR);
      }
      // check if given projet, subproject and the project group linked to the given subproject exists, otherwise -> ObjectNotFound
      if (p == null) {
        logger.error("Project not found " + project.getId());
        throw new ObjectNotFoundException(Project.class, project.getId().longValue());
      }
      if (sub == null) {
        logger.error("Project subgroup not found " + subgroup.getId());
        throw new ObjectNotFoundException(ProjectSubgroup.class, subgroup.getId().longValue());
      }
      if (g == null) {
        logger.error("Project group not found " + sub.getProjectGroup().getIdProjectGroup());
        throw new ObjectNotFoundException(ProjectGroup.class, sub.getProjectGroup().getIdProjectGroup().longValue());
      }

      try {
        // does not exist yet
        c = new Contributor();
        // auto-increment
        c.setIdContributor(0L);
        // pseudonym
        c.setPseudo(pseudo);
        // set if this user wants to get newsletters
        c.setNewsletter(true);

        c.save();

        List<Group> groups = sub.getContributorGroups();
        for(Group group : groups){
          group.addMember(c, Role.find.byId(EContributorRole.CONTRIBUTOR.id()), null);
          group.update();
        }

        sub.addMember(c);
        sub.update();

      } catch (Exception e) {
        logger.error("error while saving contributor " + pseudo, e);
        throw new PersistenceException(PersistenceException.Key.SAVE_CONTRIBUTOR, e);
      }

      /*try {
        // does not exist yet
        TmpContributor tmp = new TmpContributor();
        // auto-increment
        tmp.setIdContributor(0L);
        // pseudonym
        tmp.setPseudo(pseudo);
        // password is in clear
        tmp.setPasswordHash(hashPassword(subgroup.getPassword()));

        tmp.setContributor(c);
        tmp.setProject(p);
        tmp.setProjectSubgroup(sub);

        tmp.save();
      } catch (Exception e) {
        logger.error("error while saving tmp contributor " + pseudo, e);
        throw new PersistenceException(PersistenceException.Key.SAVE_TMP_CONTRIBUTOR, e);
      }*/
      transaction.commit();

    } finally {
      transaction.end();
    }
  }

  @Override
  public be.webdeb.core.api.contributor.Group retrieveGroup(int id) {
    Group group = Group.findById(id);
    if (group != null) {
      try {
        return mapper.toGroup(group);
      } catch (FormatException e) {
        logger.error("unable to wrap retrieved group " + id, e);
      }
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.contributor.Group> retrieveGroups(String query, boolean openOnly) {
    return retrieveGroups(query, openOnly, 0, 0);
  }

  @Override
  public List<be.webdeb.core.api.contributor.Group> retrieveGroups(String query, boolean openOnly, int fromIndex, int toIndex) {
    List<Group> groups = Group.findGroups(query, openOnly, fromIndex, toIndex);
    List<be.webdeb.core.api.contributor.Group> result = new ArrayList<>();
    for (Group g : groups) {
      try {
        result.add(mapper.toGroup(g));
      } catch (FormatException e) {
        logger.error("unable to wrap group " + g.getIdGroup() + ", ignore it.", e);
      }
    }
    return result;
  }

  @Override
  public List<be.webdeb.core.api.contributor.GroupSubscription> retrieveGroups(Long contributor) {
    Contributor c = Contributor.findById(contributor);
    List<be.webdeb.core.api.contributor.GroupSubscription> result = new ArrayList<>();
    if (c != null) {
      try {
        be.webdeb.core.api.contributor.Contributor apiContributor = mapper.toContributor(c);
        for (ContributorHasGroup g : c.getContributorHasGroups()) {
          result.add(mapper.toGroupSubscription(g, apiContributor, mapper.toGroup(g.getGroup())));
        }
        // must set default group
        Optional<GroupSubscription> defaultGroup = result.stream()
            .filter(g -> g.getGroup().getGroupId() == c.getDefaultGroup().getIdGroup()).findFirst();
        if (!defaultGroup.isPresent()) {
          if (result.isEmpty()) {
            // no group -> add it "virtually" to public default group
            GroupSubscription virtualDefault = getDefaultGroup();
            virtualDefault.setContributor(mapper.toContributor(c));
            virtualDefault.isDefault(true);
            result.add(virtualDefault);
          } else {
            // use first one as default
            logger.warn("FIXME no default group for contributor " + contributor + ", virtually assigned to " + result.get(0).getGroup().getGroupName());
            result.get(0).isDefault(true);
          }
        }
      } catch (FormatException e) {
        logger.error("unable to get any group for contributor, return default public group as fallback " + contributor, e);
        return new ArrayList<>();
      }
    }
    return result;
  }

  @Override
  public GroupSubscription getDefaultGroup() {
    // get default group
    Group group = Group.findById(DEFAULT_GROUP);
    try {
      return mapper.toGroupSubscription(mapper.toGroup(group));
    } catch (FormatException e) {
      logger.error("unable to create default group subscription", e);
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.contributor.GroupSubscription> retrieveGroupMembers(int groupId, EContributorRole role) {
    Group group = Group.findById(groupId);
    List<be.webdeb.core.api.contributor.GroupSubscription> result = new ArrayList<>();
    if (group != null) {
      try {
        be.webdeb.core.api.contributor.Group apiGroup = mapper.toGroup(group);
        group.getContributorHasGroups().stream().filter(g -> g.getRole().getIdRole() >= role.id()).forEach(g -> {
          try {
            result.add(mapper.toGroupSubscription(g, mapper.toContributor(g.getContributor()), apiGroup));
          } catch (FormatException e) {
            logger.error("unable to cast member of group " + g.getId(), e);
          }
        });
      } catch (FormatException e) {
        logger.error("unable to get members of group " + groupId, e);
      }
    }
    return result;
  }

  @Override
  public GroupSubscription retrieveGroupSubscription(String emailOrPseudo, String groupName) {
    Contributor contributor = findContributorByEmailOrPseudo(emailOrPseudo);
    Group group = Group.findByName(groupName);
    if (contributor != null && group != null) {
      for (ContributorHasGroup chg : contributor.getContributorHasGroups()) {
        try {
          if (chg.getGroup().getIdGroup() == group.getIdGroup() && !chg.isBanned()) {
            return mapper.toGroupSubscription(chg, mapper.toContributor(contributor), mapper.toGroup(group));
          }
        } catch (FormatException e) {
          logger.error("unable to create subscription from " + chg.getId(), e);
          return null;
        }
      }
    }
    return null;
  }

  @Override
  public GroupSubscription retrieveGroupSubscription(Long contributorId, Integer groupId) {
    Contributor contributor = Contributor.findById(contributorId);
    Group group = Group.findById(groupId);
    if (contributor != null && group != null) {
      for (ContributorHasGroup chg : contributor.getContributorHasGroups()) {
        try {
          if (chg.getGroup().getIdGroup() == group.getIdGroup() && !chg.isBanned()) {
            return mapper.toGroupSubscription(chg, mapper.toContributor(contributor), mapper.toGroup(group));
          }
        } catch (FormatException e) {
          logger.error("unable to create subscription from " + chg.getId(), e);
          return null;
        }
      }
    }
    return null;
  }

  @Override
  public void setDefaultGroupFor(int group, long contributor) throws PersistenceException {
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      logger.error("unable to find contributor with id " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Group g = Group.findById(group);
    if (g == null) {
      logger.error("unable to find group with id " + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      c.setDefaultGroup(g);
      c.update();
      transaction.commit();

    } catch (Exception e) {
      logger.error("unable to set default group " + group + " for " + contributor, e);
      throw new PersistenceException(PersistenceException.Key.DEFAULT_GROUP, e);
    } finally {
      transaction.end();
    }

  }

  @Override
  public void addMemberInGroup(int group, Long contributor, EContributorRole role) throws PersistenceException {
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      logger.error("given contributor does not exist " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }
    addMemberInGroup(group, c, role, null);
  }

  @Override
  public void inviteMemberInGroup(int group, Long contributor, EContributorRole role) throws PersistenceException {
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      logger.error("given contributor does not exist " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }
    addMemberInGroup(group, c, role, UUID.randomUUID().toString());
  }

  @Override
  public void inviteMemberInGroup(int group, String emailOrPseudo, EContributorRole role) throws PersistenceException {
    logger.debug("will invite member " + emailOrPseudo + " to group " + group);

    Contributor contributor = findContributorByEmailOrPseudo(emailOrPseudo);
    String invitationToken = UUID.randomUUID().toString();

    if (contributor != null) {
      // now add this (newly created) contributor to given group
      addMemberInGroup(group, contributor, role, invitationToken);
    }
  }

  @Override
  public void setBannedInGroup(int group, Long contributor, boolean revoke) throws PersistenceException {
    logger.debug("will " + (revoke ? "revoke" : "unban") + " member " + contributor + FROM_GROUP + group);
    Contributor c = Contributor.findById(contributor);

    if (c == null) {
      logger.error(UNABLE_RETRIEVE_CONTRIBUTOR + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Group g = Group.findById(group);
    if (g == null) {
      logger.error(UNABLE_RETRIEVE_GROUP + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Optional<ContributorHasGroup> chg = c.getContributorHasGroups().stream().filter(s -> s.getId().getIdGroup() == group).findFirst();
      if (chg.isPresent()) {
        chg.get().isBanned(revoke);
        c.update();
        transaction.commit();
      }

    } catch (Exception e) {
      logger.error("unable to set banned state member " + c.toString() + FROM_GROUP + g.toString(), e);
      throw new PersistenceException(PersistenceException.Key.REMOVE_MEMBER, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public void setBanned(Long contributor, boolean revoke) throws PersistenceException {
    logger.debug("will " + (revoke ? "revoke" : "unban") + " member " + contributor);
    Contributor c = Contributor.findById(contributor);

    if (c == null) {
      logger.error(UNABLE_RETRIEVE_CONTRIBUTOR + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      c.setBanned(revoke);
      c.update();
      transaction.commit();
    } catch (Exception e) {
      logger.error("unable to set banned state " + c.toString(), e);
      throw new PersistenceException(PersistenceException.Key.REMOVE_MEMBER, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public void setFollowGroup(int group, Long contributor, boolean follow) throws PersistenceException {
    //logger.debug("group " + group + " is now " + (follow ? "followed" : "unfollowed") + " by " + contributor);
    Contributor c = Contributor.findById(contributor);

    if (c == null) {
      logger.error(UNABLE_RETRIEVE_CONTRIBUTOR + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Group g = Group.findById(group);
    if (g == null) {
      logger.error(UNABLE_RETRIEVE_GROUP + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    if(!follow && c.getDefaultGroup().getIdGroup() == g.getIdGroup()){
      logger.error("The user can''t unfollow his default group " + c.toString());
      throw new PersistenceException(PersistenceException.Key.UNFOLLOW_DEFAULT_GROUP);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Optional<ContributorHasGroup> chg = c.getContributorHasGroups().stream().filter(s -> s.getId().getIdGroup() == group).findFirst();
      if (chg.isPresent()) {
        chg.get().isFollowed(follow);
        c.update();
        transaction.commit();
      }

    } catch (Exception e) {
      logger.error("unable to set followed state member " + c.toString() + FROM_GROUP + g.toString(), e);
      throw new PersistenceException(PersistenceException.Key.FOLLOW_GROUP_ERROR, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public boolean removeMemberFromGroup(int group, Long contributor) throws PersistenceException {
    logger.debug("will remove member " + contributor + FROM_GROUP + group);
    Contributor c = Contributor.findById(contributor);
    boolean removed;

    if (c == null) {
      logger.error(UNABLE_RETRIEVE_CONTRIBUTOR + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Group g = Group.findById(group);
    if (g == null) {
      logger.error(UNABLE_RETRIEVE_GROUP + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    if (c.getDefaultGroup().getIdGroup() == g.getIdGroup()) {
      logger.error("unable to remove member " + c.toString() + FROM_GROUP + g.toString()
          + " because group is his/her default");
      throw new PersistenceException(PersistenceException.Key.REMOVE_MEMBER);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      removed = g.removeMember(c);
      g.update();
      transaction.commit();

    } catch (Exception e) {
      logger.error("unable to remove member " + c.toString() + FROM_GROUP + g.toString(), e);
      throw new PersistenceException(PersistenceException.Key.REMOVE_MEMBER, e);

    } finally {
      transaction.end();
    }
    return removed;
  }

  @Override
  public void save(be.webdeb.core.api.contributor.Group group, Long contributor) throws PersistenceException {
    if (group == null) {
      logger.error("given group is null");
      throw new ObjectNotFoundException(Group.class, null);
    }

    logger.debug("try to save group " + group.toString());
    // check if contributor can be found
    Contributor c = Contributor.findById(contributor);
    if (c == null) {
      logger.debug(CONTRIBUTOR_NOT_FOUND + "given id " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Group g = null;
    if (group.getGroupId() != -1) {
      g = Group.findById(group.getGroupId());
      if (g == null) {
        logger.error(UNABLE_RETRIEVE_GROUP + group.getGroupId());
        throw new ObjectNotFoundException(Group.class, (long) group.getGroupId());
      } else {
        // check version
        long version = g.getVersion().getTime();
        if (group.getVersion() != version) {
          logger.error("old version for given object " + group.getGroupId()
              + " actual is " + version + " (" + new Date(version) + ") and yours is " + group.getVersion()
              + " (" + new Date(group.getVersion()) + ")");
          be.webdeb.core.api.contributor.Group newOne = null;
          try {
            newOne = mapper.toGroup(g);
          } catch (FormatException e) {
            logger.error("unable to cast retrieved group " + g.getIdGroup(), e);
          }
          throw new OutdatedVersionException(group, newOne);
        }
      }
    }

    if (g == null) {
      // create new
      // for auto-increment
      g = updateGroup(group, new Group());
      g.setIdGroup(0);

      // add contributor as group owner
      ContributorHasGroup mainsub = ContributorHasGroup.byContributorAndGroup(c.getIdContributor(), Group.getPublicGroup().getIdGroup());
      if(mainsub == null || mainsub.getRole().getIdRole() < EContributorRole.ADMIN.id()) {
        g.addMember(c, Role.find.byId(EContributorRole.OWNER.id()), null);
      }

      // add all admins to the new group
      for(Contributor admin : Contributor.getAllAdmins()){
        g.addMember(admin, Role.find.byId(EContributorRole.ADMIN.id()), null, admin.getIdContributor().equals(c.getIdContributor()));
      }
    } else {
      // just update existing
      updateGroup(group, g);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // save group
      g.save();
      transaction.commit();
    } catch (Exception e) {
      logger.error("unable to save group", e);
      throw new PersistenceException(PersistenceException.Key.SAVE_GROUP, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public List<EPermission> getPermissionForRole(EContributorRole role) {
    List<EPermission> permissions = new ArrayList<>();
    Role dbRole = Role.find.byId(role.id());
    if (dbRole != null) {
      permissions.addAll(dbRole.getPermissions().stream()
          .map(p -> EPermission.value(p.getIdPermission())).collect(Collectors.toList()));
    }
    return permissions;
  }

  @Override
  public void clean(int group, boolean deleteGroup) throws PersistenceException {
    logger.debug("try to clean group " + group + " completely " + deleteGroup);

    // check group exists
    Group g = Group.findById(group);
    if (g == null || g.getIdGroup() == Group.getPublicGroup().getIdGroup()) {
      logger.error(UNABLE_RETRIEVE_GROUP + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    // remove all contributions from this group
    try {

      // get all elements to delete since we have to follow a right order while deleting all this stuff
      // first all texts (that will remove their arguments in cascade) then all remaining arguments
      // (originating from other texts), finally all actors.
      // for all contributions, they'll be either completely removed or simply removed from group if they belong to other groups
      List<Long> toremove = g.getContributions().stream().map(Contribution::getIdContribution).collect(Collectors.toList());

      // first delete all texts cascading also arguments and links
      Iterator<Contribution> i = g.getContributions().stream().filter(c ->
              c.getContributionType().getIdContributionType() == EContributionType.TEXT.id()).iterator();

      while (i.hasNext()) {
        Contribution c = i.next();
        logger.debug(TRY_TO_DELETE + c.toString());
        toremove.remove(c.getIdContribution());
        if (c.getGroups().size() == 1) {
          // we may safely delete it since it exists only in this group
          c.delete();
        } else {
          // simply remove the binding to this group
          c.removeGroup(g);
          c.update();
        }
      }

      // now get remaining arguments (if any)
      i = g.getContributions().stream().filter(c -> toremove.contains(c.getIdContribution())
              && c.getContributionType().getIdContributionType() == EContributionType.ARGUMENT.id()).iterator();
      while (i.hasNext()) {
        Contribution c = i.next();
        logger.debug(TRY_TO_DELETE + c.toString());
        toremove.remove(c.getIdContribution());
        if (c.getGroups().size() == 1) {
          // we may safely delete it since it exists only in this group
          c.delete();
        } else {
          // simply remove the binding to this group
          c.removeGroup(g);
          c.update();
        }
      }

      // finally remove remaining actors
      i = g.getContributions().stream().filter(c ->
          c.getContributionType().getIdContributionType() == EContributionType.ACTOR.id()).iterator();
      while (i.hasNext()) {
        Contribution c = i.next();
        logger.debug(TRY_TO_DELETE + c.toString());
        if (c.getGroups().size() == 1
            && c.getActor().getContributions(-1).stream().allMatch(o -> o.getContribution().getGroups().size() == 1)) {
          // we may safely delete it since it exists only in this group
          c.delete();
        } else {
          // simply remove the binding to this group
          c.removeGroup(g);
          c.update();
        }
      }

      // remove memberships
      List<ContributorHasGroup> memberships = g.getContributorHasGroups();
      if (!deleteGroup) {
        memberships.removeIf(m -> m.getRole().getIdRole() > EContributorRole.CONTRIBUTOR.id());
      }
      memberships.forEach(Model::delete);

      // do we have to clean the group too ?
      if (deleteGroup) {
        g.delete();
      }
      transaction.commit();
      logger.info("cleaned group " + g.toString());
    } catch (Exception e) {
      logger.error("unable to clean group " + group, e);
      throw new PersistenceException(PersistenceException.Key.SAVE_GROUP, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public List<be.webdeb.core.api.contribution.Contribution> searchContributorContributions(String searchText, Long contributor, int fromIndex, int toIndex){
    logger.info("Search for contributor's contributions " + contributor);
    Contributor c = Contributor.findById(contributor);
    if (c != null) {
      return toContributions(c.getContributions(fromIndex, toIndex).stream().filter(f ->
          f.matchSearchForContribution(searchText)).collect(Collectors.toList()));
    }
    return new ArrayList<>();
  }

  @Override
  public List<ExternalContribution> getContributorExternalContributions(Long contributor, int fromIndex, int toIndex, String sourceName){
    logger.info("Get contributor external contributions " + contributor);
    Contributor c = Contributor.findById(contributor);
    TExternalSourceName externalSource = TExternalSourceName.findById(EExternalSource.valueOf(sourceName).id());
    if (c != null && externalSource != null) {
      return toExternalContributions(c.getExternalContributions(fromIndex, toIndex, externalSource.getIdSource()));
    }
    return new ArrayList<>();
  }

  @Override
  public Map<EContributionType, Long> getContributionsCount(Long contributor) {
    Map<EContributionType, Long> countMap = new LinkedHashMap<>();
    countMap.put(EContributionType.ACTOR, Contribution.getAmountOf(contributor, EContributionType.ACTOR.id(), -1));
    countMap.put(EContributionType.DEBATE, Contribution.getAmountOf(contributor, EContributionType.DEBATE.id(), -1));
    countMap.put(EContributionType.TEXT, Contribution.getAmountOf(contributor, EContributionType.TEXT.id(), -1));
    countMap.put(EContributionType.CITATION, Contribution.getAmountOf(contributor, EContributionType.CITATION.id(), -1));
    return countMap;
  }

  @Override
  public GroupColor findGroupColorByCode(String code){
    if(code != null) {
      code = code.replace("#", "");
      TGroupColor c = TGroupColor.findByCode(code);
      return (c != null ? factory.createGroupColor(c.getIdColor(), c.getColorCode()) : null);
    }
    return null;
  }

  @Override
  public List<GroupColor> getGroupColors() {
    if (groupColors == null) {
      groupColors = TGroupColor.getAll().stream().map(t -> factory.createGroupColor(t.getIdColor(), t.getColorCode()))
          .collect(Collectors.toList());
    }
    return groupColors;
  }

  @Override
  public void userIsWarnedAboutBrowser(Long user){
    logger.debug("User " + user + " is warned about old browser danger");
    Contributor c = Contributor.findById(user);
    if(c != null){
      c.setBrowserWarned(true);
      c.update();
    }
  }

  @Override
  public void giveAdminRights(Long contributor){
    logger.warn("Try to give admin rights to " + contributor);
    Contributor c = Contributor.findById(contributor);

    if(c != null){
      try {
        for(Group group : Group.getAllGroups()){
          addMemberInGroup(group.getIdGroup(), c.getIdContributor(),EContributorRole.ADMIN);
        }

      } catch (Exception e) {
        logger.error("unable to give admin rights to " + c , e);
      }
    }
  }

  @Override
  public void removeAdminRights(Long contributor){
    logger.warn("Try to remove admin rights to " + contributor);
    Contributor c = Contributor.findById(contributor);

    if(c != null){
      try {
        for(ContributorHasGroup chg : c.getContributorHasGroups()){
            //if(chg.getGroup().getContributionVisibility().getId() == EContributionVisibility.PUBLIC.id()){
            if(chg.getGroup().getIdGroup() == Group.getPublicGroup().getIdGroup()){
              addMemberInGroup(chg.getGroup().getIdGroup(), c.getIdContributor(),EContributorRole.ADMIN);
            }else{
              chg.delete();
            }
        }
      } catch (Exception e) {
        logger.error("unable to remove admin rights to " + c , e);
      }
    }
  }

  @Override
  public String hashPassword(String password) throws PersistenceException {
    if (password == null || "".equals(password)) {
      throw new PersistenceException(PersistenceException.Key.UPDATE_CONTRIBUTOR);
    }
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  @Override
  public boolean checkPassword(String clear, String hashed) {
    return clear != null && hashed != null && BCrypt.checkpw(clear, hashed);
  }

  @Override
  public List<ModelDescription> getModelDescription() {
    if(modelDescription == null){
      modelDescription = Contribution.getModelDescriptions();
    }

    return modelDescription;
  }

  @Override
  public List<List<String>> executeApiQuery(Map<String,String[]> query) {
    try {
      Exportable exportable = new Exportable(query);
      exportable.initialize();
      return be.webdeb.infra.persistence.model.Contribution.executeApiQuery(exportable.makeSqlQuery());
    }
    catch (Exception e) {
      return new ArrayList<>();
    }
  }


  @Override
  public boolean isContributorValid(Long contributor) {
    return isContributorValid(Contributor.findById(contributor));
  }

  @Override
  public boolean deleteContributor(Long idContributor, String password) {
    Contributor contributor = Contributor.findById(idContributor);
    if (contributor != null && checkPassword(password, contributor.getPasswordHash())) {

      if(contributor.getPseudo() == null){
        contributor.setPseudo(contributor.generatePseudo());
      }

      contributor.setFirstname(null);
      contributor.setLastname(null);
      contributor.setEmail(null);
      contributor.setPasswordHash(null);
      contributor.setGender(null);
      contributor.setBirthYear(null);
      contributor.setResidence(null);
      contributor.setAvatar(null);

      contributor.setAuthToken(null);
      contributor.setConfirmationToken(null);

      contributor.getContributorHasAffiliations().forEach(ContributorHasAffiliation::delete);
      contributor.getContributorHasGroups().forEach(ContributorHasGroup::delete);

      contributor.getAddedPictures().forEach(picture -> {
        picture.setContributor(null);
        picture.update();
      });

      contributor.setDeleted();
      contributor.update();

      if(contributor.getAvatar() != null)
        files.deleteUserPictureFile(contributor.getAvatar().getFilename());

      return true;
    }
    logger.warn(CONTRIBUTOR_NOT_FOUND + "id contributor or password do not match: " + idContributor);
    return false;
  }

  @Override
  public ValuesHelper getValuesHelper() {
    return values;
  }

  @Override
  public List<ContributionToExplore> getContributionsToExploreForGroup(int type, int group) {
    return toContributionsToExplore(be.webdeb.infra.persistence.model.ContributionToExplore.getContributionsToExplore(type, group));
  }

  @Override
  public ContributionToExplore retrieveContributionToExplore(Long id) {
    be.webdeb.infra.persistence.model.ContributionToExplore cte = be.webdeb.infra.persistence.model.ContributionToExplore.findById(id);

    if(cte != null){
      try{
        return mapper.toContributionToExplore(cte);
      } catch (FormatException e) {
        logger.error("unable to parse contribution to explore " + cte , e);
      }
    }
    return null;
  }

  @Override
  public void deleteContributionToExplore(Set<Long> idsToKeep, EContributionType contributionType, int group) {
    be.webdeb.infra.persistence.model.ContributionToExplore.findAllContributionToExplores(contributionType, group).forEach(cte -> {
      if(idsToKeep == null || !idsToKeep.contains(cte.getIdContributionToExplore())){
        cte.delete();
      }
    });
  }

  @Override
  public void save(ContributionToExplore contributionToExplore, Long admin) throws PersistenceException, PermissionException {
    logger.debug("try to save contributionToExplore " + contributionToExplore.toString());
    // check if contributor can be found
    Contributor contributor = Contributor.findById(admin);
    if (contributor == null) {
      logger.debug(CONTRIBUTOR_NOT_FOUND + "given id " + admin);
      throw new ObjectNotFoundException(Contributor.class, admin);
    }

    Contribution contribution = Contribution.findById(contributionToExplore.getContributionId());
    if (contribution == null) {
      logger.debug("Contribution not found for given id " + contributionToExplore.getContributionId());
      throw new ObjectNotFoundException(Contribution.class, contributionToExplore.getContributionId());
    }

    Group group = Group.findById(contributionToExplore.getGroupId());
    if (group == null) {
      logger.debug("Contributor group not found for given id " + contributionToExplore.getGroupId());
      throw new ObjectNotFoundException(Group.class, contributionToExplore.getGroupId().longValue());
    }

    ContributorHasGroup chg = ContributorHasGroup.byContributorAndGroup(admin, contributionToExplore.getGroupId());
    if(chg == null || chg.getEContributorRole().id() < EContributorRole.OWNER.id()){
      throw new PermissionException(PermissionException.Key.NOT_GROUP_OWNER);
    }

    be.webdeb.infra.persistence.model.ContributionToExplore cte = be.webdeb.infra.persistence.model.ContributionToExplore.findById(contributionToExplore.getContributionToExploreId());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      if (cte == null) {
        // create new
        // for auto-increment
        cte = new be.webdeb.infra.persistence.model.ContributionToExplore();
        cte.setIdContributionToExplore(0L);
        cte.setContribution(contribution);
        cte.setGroup(group);
        cte.setOrder(contributionToExplore.getOrder());
        cte.save();
        contributionToExplore.setContributionToExploreId(cte.getIdContributionToExplore());
      }else{
        cte.setOrder(contributionToExplore.getOrder());
        cte.update();
      }

      transaction.commit();
    } catch (Exception e) {
      logger.error("unable to save contribution to explore", e);
      throw new PersistenceException(PersistenceException.Key.SAVE_CONTRIBUTION_TO_EXPLORE, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public List<Advice> getAdvices() {
    return toAdvices(be.webdeb.infra.persistence.model.Advice.findAllAdvices());
  }

  @Override
  public void deleteAdvices(Set<Integer> idsToKeep) {
    be.webdeb.infra.persistence.model.Advice.findAllAdvices().forEach(advice -> {
      if(idsToKeep == null || !idsToKeep.contains(advice.getIdAdvice())){
        advice.delete();
      }
    });
  }

  @Override
  public void save(Advice advice, Long contributor) throws PersistenceException, PermissionException {
    logger.debug("try to save advice " + advice.toString());
    // check if contributor can be found
    Contributor contributorDB = Contributor.findById(contributor);
    if (contributorDB == null) {
      logger.debug(CONTRIBUTOR_NOT_FOUND + "given id " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    if(!contributorDB.isAdmin()){
      throw new PermissionException(PermissionException.Key.NOT_GROUP_OWNER);
    }

    be.webdeb.infra.persistence.model.Advice adviceDB = be.webdeb.infra.persistence.model.Advice.findById(advice.getId());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      if (adviceDB == null) {
        // create new
        // for auto-increment
        adviceDB = new be.webdeb.infra.persistence.model.Advice();
        adviceDB.setIdAdvice(0);
        adviceDB.save();
        advice.setId(adviceDB.getIdAdvice());
      }

      int id = adviceDB.getIdAdvice();
      List<AdviceI18name> titles = new ArrayList<>();

      advice.getNames().forEach((k, v) -> {
        titles.add(new AdviceI18name(id, k, v));
      });

      adviceDB.setSpellings(titles);
      adviceDB.update();


      transaction.commit();
    } catch (Exception e) {
      logger.error("unable to save advice", e);
      throw new PersistenceException(PersistenceException.Key.SAVE_ADVICE, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public List<ContributorPicture> getContributorPictures(Long contributor) {
    return toContributorPictures(be.webdeb.infra.persistence.model.ContributorPicture.findAllPicturesByContributor(contributor));
  }

  @Override
  public void save(ContributorPicture contributorPicture, Long contributor) throws PersistenceException, PermissionException {

  }

  @Override
  public List<be.webdeb.core.api.contributor.Group> getAllGroups() {
    return toGroups(Group.getAllGroups());
  }

  @Override
  public int countNbContributors(int group) {
    return Group.countNbContributors(group);
  }

  @Override
  public int countNbContributions(int group, EContributionType type) {
    return Group.countNbContributions(group, type);
  }

  @Override
  public List<ContributorPictureSource> getContributorPictureSources() {
    if (contributorPictureSources == null) {
      contributorPictureSources = TContributorPictureSource.find.all().stream().map(t ->
              factory.createContributorPictureSource(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return contributorPictureSources;
  }

  @Override
  public List<PictureLicenceType> getPictureLicenceTypes() {
    if (pictureLicenceTypes == null) {
      pictureLicenceTypes = TPictureLicenceType.find.all().stream().map(t ->
              factory.createPictureLicenceType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return pictureLicenceTypes;
  }

  @Override
  public boolean contributorIsPedagogicForGroupAndContribution(Long contributorId, Long contributionId) {
    return Contribution.contributorIsPedagogicForGroupAndContribution(contributorId, contributionId);
  }

  @Override
  public int getNumberOfClaims(Long contributor) {
    return ContributionHasClaim.getNumberOfClaims(contributor);
  }

  /*
   * PRIVATE HELPERS
   */

  private boolean isContributorValid(Contributor contributor) {
    if(contributor != null){
      return contributor.isValidated() && !contributor.isBanned() && !contributor.isDeleted() &&
              values.isEmail(contributor.getEmail()) && !values.isBlank(contributor.getFirstname()) && !values.isBlank(contributor.getLastname());
    }

    return false;
  }

  /**
   * Find a Contributor by its email or pseudonym
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the Contributor identified by this email or pseudonym, null if not found
   */
  private Contributor findContributorByEmailOrPseudo(String emailOrPseudo){
    return (values.isEmail(emailOrPseudo) ?
            Contributor.findByEmail(emailOrPseudo) : Contributor.findByPseudo(emailOrPseudo));
  }

  /**
   * Cast an API contributor into a DB contributor, do not handle group subscriptions
   *
   * @param contributor an API contributor
   * @param c a DB contributor to update with given API contributor
   * @return the updated DB contributor
   */
  private Contributor updateContributor(be.webdeb.core.api.contributor.Contributor contributor, Contributor c) {
    c.setEmail(contributor.getEmail());
    c.setPseudo(contributor.getPseudo());
    c.setFirstname(contributor.getFirstname());
    c.setLastname(contributor.getLastname());
    c.setBirthYear(contributor.getBirthyear());
    c.setGender(contributor.getGender() != null ? TGender.find.byId(contributor.getGender().getCode()) : null);
    c.setPedagogic(contributor.isPedagogic());
    c.setNewsletter(contributor.isNewsletter());
    c.setBrowserWarned(contributor.isBrowserWarned());
    c.setOpenId(contributor.getOpenId());
    c.setOpenIdToken(contributor.getOpenIdToken());
    c.setOpenIdType(contributor.getOpenIdType() != null ? TOpenIdType.find.byId(contributor.getOpenIdType().id()) : null);

    if(!values.isBlank(contributor.getOpenId())) {
      c.setValidated(true);
    }

    if(c.getAvatar() != null && !c.getAvatar().getIdPicture().equals(contributor.getAvatarId())) {
      c.getAvatar().delete();
    }

    c.setAvatar(be.webdeb.infra.persistence.model.ContributorPicture.findById(contributor.getAvatarId()));
    return c;
  }

  /**
   * Cast an api group to a DB one
   *
   * @param group an API group
   * @param g the resulting DB group
   * @return the given DB group g updated with the data in given API group
   */
  private Group updateGroup(be.webdeb.core.api.contributor.Group group, Group g) {
    g.setGroupName(group.getGroupName());
    g.setGroupDescription(group.getDescription());
    g.setContributionVisibility(TContributionVisibility.find.byId(group.getContributionVisibility().id()));
    g.setMemberVisibility(TMemberVisibility.find.byId(group.getMemberVisibility().id()));
    g.setPermissions(group.getPermissions().stream().map(p -> Permission.find.byId(p.id())).collect(Collectors.toList()));
    g.isOpen(group.isOpen());
    g.isPedagogic(group.isPedagogic());
    g.setGroupColor(group.getGroupColor());
    return g;
  }

  /**
   * Add a new member into given group with given role and invitation token
   *
   * @param group a group id
   * @param contributor a valid contributor
   * @param role a role enum value
   * @param token a unique invitation token
   * @throws PersistenceException if any given parameter is not found in database or anything went wrong when
   * saving the binding between the contributor and the group
   */
  private void addMemberInGroup(int group, Contributor contributor, EContributorRole role, String token) throws PersistenceException {
    Group g = Group.findById(group);
    if (g == null) {
      logger.error("given group does not exist " + group);
      throw new ObjectNotFoundException(Group.class, (long) group);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      g.addMember(contributor, Role.find.byId(role.id()), token);
      g.update();
      logger.debug("contributor " + contributor.toString() + " added to group " + g.toString() + " with role " + role.name());
      transaction.commit();

    } catch (Exception e) {
      logger.error("unable to add member " + contributor.toString() + " to group " + g.toString(), e);
      throw new PersistenceException(PersistenceException.Key.ADD_MEMBER, e);

    } finally {
      transaction.end();
    }
  }

  /**
   * Create a auth token for the user
   *
   * @return the new user auth token
   */
  private String createAuthToken() {
    TokenGenerator generator = new TokenGenerator();
    return generator.nextString();
  }

  /**
   * Create a confirmation token to validate the registration of this contributor
   *
   * @return a unique token
   */
  private String newConfirmationToken(Contributor contributor) {
    String token = UUID.randomUUID().toString();
    contributor.setConfirmationToken(token);
    // reset validated state, eg to prevent from login with old password for instance
    contributor.setValidated(false);
    contributor.update();
    return token;
  }

  /**
   * Create a newsletter token to unsubscribe from newsletter without an auth
   *
   * @return a unique token
   */
  private String newNewsletterToken(Contributor contributor) {
    String token = UUID.randomUUID().toString();
    contributor.setNewsletterToken(token);

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, AUTH_EXPIRATION_DAYS);
    contributor.setNewsletterTokenExpirationDate(new Timestamp(calendar.getTime().getTime()));

    contributor.update();
    return token;
  }

  /**
   * Check if given contributor's token is not expired (regarding the version field)
   *
   * @param contributor a contributor
   * @return true if given contributor's token is too old to use (regarding this.EXPIRATION_DAYS), false otherwise
   */
  private boolean isConfirmationTokenExpired(Contributor contributor) {
    return isConfirmationTokenExpired(contributor, CONFIRMATION_EXPIRATION_DAYS);
  }

  /**
   * Check if given contributor's token is not expired regarding given threshold
   *
   * @param contributor a contributor
   * @param threshold a threshold in days
   * @return true if given contributor's token is not older than given today's + given threshold, false otherwise
   */
  private boolean isConfirmationTokenExpired(Contributor contributor, int threshold) {
    Date tokenDate = contributor.getVersion();
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DATE, -threshold);
    return !contributor.isValidated() && tokenDate != null && tokenDate.before(cal.getTime());
  }

  /**
   * Clean contributor table from all unvalidated contributors (using this.CLEANUP_DAYS)
   */
  private void removeUnvalidatedContributors() {
    logger.info("--- start removal of non validated contributors from database");
    Contributor.findAllUnvalidated().stream().filter(c -> isConfirmationTokenExpired(c, CLEANUP_DAYS)).forEach(c -> {
      if(c.getContributions().isEmpty()) {
        logger.info("will remove contributor " + c.toString() + " last activity was: " + new Date(c.getVersion().getTime()));
        c.getContributorHasAffiliations().forEach(ContributorHasAffiliation::delete);
        c.getContributorHasGroups().forEach(ContributorHasGroup::delete);
        c.delete();
      }
    });
    logger.info("--- end removal of non validated contributors from database");
  }

  /**
   * Transform given list of contribution into a list of API contributions
   *
   * @param contributions a list of DB contribution beans
   * @return a list of API contributions
   */
  private List<be.webdeb.core.api.contribution.Contribution> toContributions(List<Contribution> contributions){
    List<be.webdeb.core.api.contribution.Contribution> result = new LinkedList<>();
    for (Contribution c : contributions) {
      try {
        result.add(mapper.toContribution(c));
      } catch (FormatException e) {
        logger.warn("unable to cast contribution " + c.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Transform given list of external contribution into a list of API external contributions
   *
   * @param contributions a list of DB external contribution beans
   * @return a list of API contributions
   */
  private List<ExternalContribution> toExternalContributions(List<be.webdeb.infra.persistence.model.ExternalContribution> contributions){
    List<ExternalContribution> result = new LinkedList<>();
    for (be.webdeb.infra.persistence.model.ExternalContribution c : contributions) {
      try {
        result.add(mapper.toExternalContribution(c));
      } catch (FormatException e) {
        logger.warn("unable to cast contribution " + c.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Transform given list of contribution to explore into a list of API contribution to explore
   *
   * @param contributionsToExplore a list of DB contribution to explore beans
   * @return a list of API contributions to explore
   */
  private List<ContributionToExplore> toContributionsToExplore(List<be.webdeb.infra.persistence.model.ContributionToExplore> contributionsToExplore){
    List<ContributionToExplore> result = new LinkedList<>();
    for (be.webdeb.infra.persistence.model.ContributionToExplore c : contributionsToExplore) {
      try {
        result.add(mapper.toContributionToExplore(c));
      } catch (FormatException e) {
        logger.warn("unable to cast contribution to explore " + c.getIdContributionToExplore(), e);
      }
    }
    return result;
  }

  /**
   * Transform given list of advices into a list of API advices
   *
   * @param advices a list of DB advices beans
   * @return a list of API advices
   */
  private List<Advice> toAdvices(List<be.webdeb.infra.persistence.model.Advice> advices){
    List<Advice> result = new LinkedList<>();
    for (be.webdeb.infra.persistence.model.Advice c : advices) {
      try {
        result.add(mapper.toAdvice(c));
      } catch (FormatException e) {
        logger.warn("unable to cast advice " + c.getIdAdvice(), e);
      }
    }
    return result;
  }

  /**
   * Transform given list of contributor pictures into a list of API pictures
   *
   * @param pictures a list of DB pictures beans
   * @return a list of API pictures
   */
  private List<ContributorPicture> toContributorPictures(List<be.webdeb.infra.persistence.model.ContributorPicture> pictures){
    List<ContributorPicture> result = new LinkedList<>();
    for (be.webdeb.infra.persistence.model.ContributorPicture picture : pictures) {
      try {
        result.add(mapper.toContributorPicture(picture));
      } catch (FormatException e) {
        logger.warn("unable to cast picture " + picture.getIdPicture(), e);
      }
    }
    return result;
  }

  /**
   * Transform given list of groups into a list of API groups
   *
   * @param groups a list of DB groups beans
   * @return a list of API groups
   */
  private List<be.webdeb.core.api.contributor.Group> toGroups(List<Group> groups){
    List<be.webdeb.core.api.contributor.Group> result = new LinkedList<>();
    for (Group g : groups) {
      try {
        result.add(mapper.toGroup(g));
      } catch (FormatException e) {
        logger.warn("unable to cast group " + g.getIdGroup(), e);
      }
    }
    return result;
  }

  /**
   * Set new expiration date for auth token of given contributor
   *
   * @param contributor the contributor that need a new expiration date
   */
  private void setAuthTokenNewExpirationDate(Contributor contributor){
    // set auth token expiration date (the current date + 50 days)
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, AUTH_EXPIRATION_DAYS);
    contributor.setAuthTokenExpirationDate(new Timestamp(calendar.getTime().getTime()));
  }

  /**
   * Check auth token validity for given contributor
   *
   * @param contributor a contributor
   * @return true if token is valid
   */
  private boolean checkAuthTokenValidity(Contributor contributor){
        return contributor.getAuthTokenExpirationDate() == null || new Date().before(contributor.getAuthTokenExpirationDate());
  }

}
