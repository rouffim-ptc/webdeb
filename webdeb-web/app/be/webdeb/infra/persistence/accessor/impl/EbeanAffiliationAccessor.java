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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EModificationStatus;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.AffiliationAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Profession;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This accessor handles retrieval and saving of affiliations
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanAffiliationAccessor extends AbstractContributionAccessor<ActorFactory> implements AffiliationAccessor  {

  @Inject
  private ActorAccessor actorAccessor;

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  // string constant for logs
  private static final String ACTOR_NOT_FOUND = "no actor found for given id ";

  @Override
  public Affiliation retrieve(Long id, boolean hit) {
    ActorHasAffiliation aha = ActorHasAffiliation.  findById(id);
    if (aha != null) {
      try {
        return mapper.toAffiliation(aha);
      } catch (FormatException e) {
        logger.error("unable to cast affiliation " + aha.getId(), e);
      }
    } else {
      logger.warn("no affiliation found for id " + id);
    }
    return null;
  }

  @Override
  public Map<Integer, List<Contribution>> save(Affiliation affiliation, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("save affiliation for contributor " + contributor);
    ContributorHasAffiliation cha = null;
    Map<Integer, List<Contribution>> result = null;

    // if we have an id, retrieve affiliation or -> exception
    if (affiliation.getId() != -1L) {
      cha = ContributorHasAffiliation.findById(affiliation.getId());
      if (cha == null) {
        logger.error("no affiliation found for given id " + affiliation.getId());
        throw new ObjectNotFoundException(Affiliation.class, affiliation.getId());
      }
    }

    // check if contributor can be found, otherwise -> exception
    if (be.webdeb.infra.persistence.model.Contributor.findById(contributor) == null) {
      logger.error("no contributor found for given id " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    // check actor and create it if needed
    Actor apiActor = null;
    if (affiliation.getActor() != null) {
      apiActor = affiliation.getActor();
      // if we have an actor with an id, find it or -> exception
      if (apiActor.getId() != -1L) {
        if (be.webdeb.infra.persistence.model.Actor.findById(apiActor.getId()) == null) {
          logger.error(ACTOR_NOT_FOUND + apiActor.getId());
          throw new ObjectNotFoundException(apiActor.getClass(), apiActor.getId());
        }

      } else {
        // we have no actor id, create actor, returned list may not be empty since this actor must be returned
        logger.info("will auto-create actor since it is unknown " + apiActor.getFullname(factory.getDefaultLanguage()));
        apiActor.addInGroup(currentGroup);
        result = actorAccessor.save(apiActor, currentGroup, contributor);
        result.get(EContributionType.ACTOR.id()).add(apiActor);
      }
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // now store affiliation
      if (cha == null) {
        cha = new ContributorHasAffiliation(contributor);
      }

      if (affiliation.getFunction() != null) {
        cha.setFunction(getProfessionID(affiliation.getFunction()));
      } else {
        // force null value, since we may remove a function from existing affiliation
        cha.setFunction(null);
      }

      if (apiActor != null) {
        // we know it exist since it has been checked upper
        cha.setActor(be.webdeb.infra.persistence.model.Actor.findById(apiActor.getId()));
      }

      // persist in DB
      cha.setStartDate(values.toDBFormat(affiliation.getStartDate()));
      if(affiliation.getStartType() != null && affiliation.getStartType().getEType() == EPrecisionDate.ONGOING && affiliation.getEndDate() == null){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date today = Calendar.getInstance().getTime();
        cha.setEndDate(values.toDBFormat(df.format(today)));
      }else{
        cha.setEndDate(values.toDBFormat(affiliation.getEndDate()));
      }
      if(affiliation.getStartType() != null)
        cha.setStartDateType(TPrecisionDateType.find.byId(affiliation.getStartType().getType()));
      if(affiliation.getEndType() != null)
        cha.setEndDateType(TPrecisionDateType.find.byId(affiliation.getEndType().getType()));
      cha.save();
      // reset id, in case it has been created now
      affiliation.setId(cha.getIdCha());

      logger.info("saved " + cha.toString());
      transaction.commit();
      // may contain the newly created Actor
      return result;

    } catch (Exception e) {
      logger.error("error while saving affiliation " + affiliation, e);
      throw new PersistenceException(PersistenceException.Key.SAVE_AFFILIATION, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public Map<Integer, List<Contribution>> save(Affiliation affiliation, Long actor, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save " + affiliation.toString() + " (" + affiliation.getId() + ") for " + actor);
    ActorHasAffiliation aha = null;
    Map<Integer, List<Contribution>> result = new HashMap<>();

    // if we have an id, retrieve affiliation or -> exception
    if (affiliation.getId() != -1L) {
      aha = ActorHasAffiliation.findById(affiliation.getId());
      if (aha == null) {
        logger.error("no affiliation found for given id " + affiliation.getId());
        throw new ObjectNotFoundException(Affiliation.class, affiliation.getId());
      }
    }

    // check if contributor can be found, otherwise -> exception is thrown
    Contributor dbContributor = checkContributor(contributor, currentGroup);

    // check if actor can be found, otherwise -> exception
    be.webdeb.infra.persistence.model.Actor affiliated = be.webdeb.infra.persistence.model.Actor.findById(actor);
    if (affiliated == null) {
      logger.error(ACTOR_NOT_FOUND + actor);
      throw new ObjectNotFoundException(Actor.class, actor);
    }

    // now store affiliation
    Transaction transaction = Ebean.getDefaultServer().currentTransaction();
    boolean commitNeeded = transaction == null;
    if (commitNeeded) {
      logger.debug("no transaction running (affiliation saved by itself), create one");
      transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    }
    try {
      // do we have an existing affiliation, if not create a new one
      if (aha == null) {
        aha = new ActorHasAffiliation(actor);
      }

      // check affiliation actor and create it if needed
      if (affiliation.getActor() != null) {
        Actor affiliationActor = affiliation.getActor();

        // if we have an actor with an id, find it or -> exception
        if (affiliationActor.getId() != -1L) {
          // check if we're not trying to create an affiliation to itself
          if (affiliationActor.getId().equals(actor)) {
            logger.error("trying to affiliate actor to itself, should not happen, just ignore for actor" + affiliationActor);
            // commit (nothing) to let the process continues normally
            if (commitNeeded) {
              transaction.commit();
            }
            return null;
          }

          be.webdeb.infra.persistence.model.Actor dbActor = be.webdeb.infra.persistence.model.Actor.findById(affiliationActor.getId());
          if (dbActor == null) {
            logger.error(ACTOR_NOT_FOUND + actor);
            throw new ObjectNotFoundException(Actor.class, affiliationActor.getId());
          }

          // set this actor as the affiliation actor and save it if new names
          aha.setAffiliation(dbActor);

          // if any name (spelling) passed in affiliation actor does not yet exist, save actor to update spellings
          List<String> existingLang = dbActor.getNames().stream().map(ActorI18name::getLang).collect(Collectors.toList());
          if (!affiliationActor.getNames().stream().allMatch(n -> existingLang.contains(n.getLang()))) {
            logger.info("will update affiliation too because we got new spellings for actor " + dbActor.getIdContribution());
            dbActor.setNames(toI18names(affiliationActor, dbActor));
            dbActor.update();
            bindContributor(dbActor.getContribution(), dbContributor, EModificationStatus.UPDATE);
          }

        } else {
          // we have no actor id, create actor, returned list may not be empty since this actor must be returned
          logger.debug("will auto-create actor since it is unknown " + affiliationActor.getFullname(factory.getDefaultLanguage()));
          // set same group as affiliated actor
          for (Group g : affiliated.getContribution().getGroups()) {
            affiliationActor.addInGroup(g.getIdGroup());
          }
          // we know we'll get one element as result from save since it's a new actor
          result = actorAccessor.save(affiliationActor, currentGroup, contributor);
          result.get(EContributionType.ACTOR.id()).add(affiliationActor);
          aha.setAffiliation(be.webdeb.infra.persistence.model.Actor
                .findById(affiliationActor.getId()));
        }
      } else {
        // reset actor because binding may have been suppressed
        aha.setAffiliation(null);
      }

      if (affiliation.getFunction() != null) {
        aha.setFunction(getProfessionID(affiliation.getFunction()));
      } else {
        aha.setFunction(null);
      }
      
      aha.setType(affiliation.getAffiliationType() != null ?
          TAffiliationType.find.byId(affiliation.getAffiliationType().id()) : null);
      aha.setStartDate(values.toDBFormat(affiliation.getStartDate()));
      if(affiliation.getStartType() != null && affiliation.getStartType().getEType() == EPrecisionDate.ONGOING && affiliation.getEndDate() == null){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date today = Calendar.getInstance().getTime();
        aha.setEndDate(values.toDBFormat(df.format(today)));
      }else{
        aha.setEndDate(values.toDBFormat(affiliation.getEndDate()));
      }
      if(affiliation.getStartType() != null)
        aha.setStartDateType(TPrecisionDateType.find.byId(affiliation.getStartType().getType()));
      if(affiliation.getEndType() != null)
        aha.setEndDateType(TPrecisionDateType.find.byId(affiliation.getEndType().getType()));

      // persist in DB
      aha.save();

      if (commitNeeded) {
        bindContributor(aha.getActor().getContribution(), dbContributor, EModificationStatus.UPDATE);
        transaction.commit();

        forceContributionUpdate(aha.getActor().getContribution());

        if(aha.getAffiliation() != null && aha.getAffiliation().getContribution() != null) {
          forceContributionUpdate(aha.getAffiliation().getContribution());
        }

        transaction.commit();
      }

      // must set id of affiliation to be correctly bound afterwards, if this save is within an automatic creation
      affiliation.setId(aha.getId());
      logger.info("saved " + aha.toString());

    } catch (PermissionException e) {
      logger.error("permission error while saving/updating affiliation", e);
      throw e;
    } catch (PersistenceException e) {
      // do not re-packed it, let it raise as is
      throw e;

    } catch (Exception e) {
      logger.error("error while saving/updating affiliation", e);
      throw new PersistenceException(PersistenceException.Key.SAVE_AFFILIATION, e);

    } finally {
      // only commit if we created a dedicated transaction for it
      if (commitNeeded) {
        transaction.end();
      }
    }

    // may contain the newly created Actor
    return result;
  }

  @Override
  public void remove(Long contribution, Long contributor, EContributionType type, boolean isActor) throws PersistenceException {
    logger.debug("try to remove affiliation " + contribution + (isActor ? " for actor" : " for contributor"));
    if (type != EContributionType.AFFILIATION) {
      logger.error("given type is invalid, needed " + EContributionType.AFFILIATION.name() + " received " + type.name());
      throw new ObjectNotFoundException(Contribution.class, contribution);
    }

    if (be.webdeb.infra.persistence.model.Contributor.findById(contributor) == null) {
      logger.error("unable to retrieve contributor with id " + contributor);
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      if (isActor) {
        // actor affiliations and contributor affiliations are stored in different tables
        ActorHasAffiliation aha = be.webdeb.infra.persistence.model.ActorHasAffiliation.findById(contribution);
        if (aha == null) {
          logger.error("unable to retrieve affiliation with id " + contribution);
          throw new ObjectNotFoundException(ActorHasAffiliation.class, contribution);
        }

        aha.delete();

      } else {
        ContributorHasAffiliation cha = be.webdeb.infra.persistence.model.ContributorHasAffiliation.findById(contribution);

        if (cha == null) {
          logger.error("unable to retrieve affiliation with id " + contribution);
          throw new ObjectNotFoundException(ContributorHasAffiliation.class, contribution);
        }
        cha.delete();
      }

      transaction.commit();

    } catch (Exception e) {
      logger.error("error while removing contribution " + contribution);
      throw new PersistenceException(PersistenceException.Key.REMOVE_AFFILIATION, e);

    } finally {
      transaction.end();
    }
    logger.info("removed affiliation with id " + contribution);
  }

  /**
   * Save a profession
   *
   * @param profession a profession object
   * @return the profession id corresponding to given profession, -1 if any error occured
   */
  @Override
  public int saveProfession(be.webdeb.core.api.actor.Profession profession) {
    int pId;
    if(profession.getId() == -1){
      pId = getProfessionID(profession);
    }
    else {
      pId = profession.getId();
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Profession p = Profession.findById(pId);
      if (p == null) {
        // try to find in db
        Set<Profession> matches = new HashSet<>();
        profession.getNames().entrySet().forEach(e ->
          matches.addAll(e.getValue().entrySet().stream().flatMap(f ->
            Profession.findProfessions(f.getValue(), e.getKey(), f.getKey()).stream()).collect(Collectors.toSet())));
        if (!matches.isEmpty()) {
          // just take first one
          p = matches.iterator().next();
        } else {
          throw new Exception("Profession not found");
        }

        p.save();
        pId = p.getIdProfession();
      }

      //update super link
      if(profession.getSuperLinkWithoutSetting() != null) {
        Profession superLink = Profession.findById(profession.getSuperLink().getId());
        if (superLink != null && ProfessionHasLink.findLink(p.getIdProfession(), superLink.getIdProfession()) == null) {
          ProfessionHasLink phl = new ProfessionHasLink(p, superLink);
          phl.save();

        }
      }else if(profession.getSuperLink() != null){
        Profession superLink = Profession.findById(profession.getSuperLink().getId());
        if (superLink != null) {
          ProfessionHasLink linkToDelete = ProfessionHasLink.findLink(p.getIdProfession(), superLink.getIdProfession());
          if(linkToDelete != null){
            linkToDelete.delete();
          }
        }

      }

      // update profession spellings, if any
      List<ProfessionI18name> names = new ArrayList<>();
      for (Map.Entry<String, Map<String, String>> e : profession.getNames().entrySet()) {
        for (Map.Entry<String, String> f : e.getValue().entrySet()) {
          names.add(new ProfessionI18name(p.getIdProfession(), e.getKey(), f.getKey(), f.getValue()));
        }
      }
      p.setSpellings(names);

      // update profession links, if any
      List<ProfessionHasLink> links = new ArrayList<>();
      for (Map.Entry<Integer, be.webdeb.core.api.actor.Profession> e : profession.getLinks().entrySet()) {
        Profession pFrom = Profession.findById(e.getValue().getId());
        if (pFrom == null) {
          logger.debug("Profession "+e.getValue().getId()+" not found");
        }else{
          links.add(new ProfessionHasLink(pFrom, p));
        }
      }
      p.setLinks(links);

      p.setDisplayHierarchy(profession.isDisplayHierarchy());
      // cascade changes, if any
      p.update();
      transaction.commit();

    } catch (Exception e) {
      logger.error("unable to save profession " + profession.getId(), e);
      pId = -1;

    } finally {
      transaction.end();
    }
    return pId;
  }

  /**
   * Retrieve a profession id for a given profession, may create it, if needed.
   * If existing, will update profession spellings, if necessary.
   *
   * @param profession a profession object
   * @return the profession id corresponding to given profession, -1 if any error occured
   */
  private int getProfessionID(be.webdeb.core.api.actor.Profession profession) {
    int pId = profession.getId();

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Profession p = Profession.findById(pId);
      if (p == null) {
        // try to find in db
        Set<Profession> matches = new HashSet<>();
        profession.getNames().entrySet().forEach(e ->
                matches.addAll(e.getValue().entrySet().stream().flatMap(f ->
                        Profession.findProfessions(f.getValue(), e.getKey(), f.getKey()).stream()).collect(Collectors.toSet())));
        if (!matches.isEmpty()) {
          // just take first one
          p = matches.iterator().next();
        } else {
          // create a new profession in database
          p = new Profession();
          p.setProfessionType(TProfessionType.find.byId(profession.getType().id()));
        }

        p.save();
        pId = p.getIdProfession();
      }

      List<ProfessionI18name> names = new ArrayList<>();
      // update profession spellings, if any
      for (Map.Entry<String, Map<String, String>> e : profession.getNames().entrySet()) {
        for (Map.Entry<String, String> f : e.getValue().entrySet()) {
          names.add(new ProfessionI18name(p.getIdProfession(), e.getKey(), f.getKey(), f.getValue()));
        }
      }
      p.setSpellings(names);

      // cascade changes, if any
      p.update();
      transaction.commit();

    } catch (Exception e) {
      logger.error("unable to save new profession " + profession, e);
      pId = -1;

    } finally {
      transaction.end();
    }
    return pId;
  }
}
