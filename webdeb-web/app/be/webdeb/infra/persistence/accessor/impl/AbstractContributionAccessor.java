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

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.contribution.link.*;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.picture.EContributorPictureSource;
import be.webdeb.core.api.contributor.picture.EPictureLicenceType;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.debate.EDebateShade;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.OutdatedVersionException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.persistence.accessor.api.*;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.ContextHasSubDebate;
import be.webdeb.infra.persistence.model.ContributionToExplore;
import be.webdeb.infra.persistence.model.Contributor;
import be.webdeb.infra.persistence.model.Group;
import be.webdeb.infra.persistence.model.Organization;
import be.webdeb.infra.persistence.model.Person;
import be.webdeb.infra.persistence.model.Text;
import be.webdeb.presentation.web.controllers.account.ClaimHolder;
import be.webdeb.util.ValuesHelper;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import javax.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This accessor handles common actions to persist or retrieve all types of contribution
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class AbstractContributionAccessor<T extends ContributionFactory> implements ContributionAccessor {

  @Inject
  protected T factory;

  @Inject
  private ActorAccessor actorAccessor;

  @Inject
  private TagAccessor tagAccessor;

  @Inject
  private ArgumentAccessor argumentAccessor;

  @Inject
  private DebateAccessor debateAccessor;

  @Inject
  protected APIObjectMapper mapper;

  @Inject
  protected ValuesHelper values;

  @Inject
  protected FileSystem files;

  private List<ContributionType> contributionTypes = null;
  private List<ValidationState> validationStates = null;
  private List<Language> languages = null;
  private List<ModificationStatus> modificationStatuses = null;
  private List<JustificationLinkType> justificationTypes = null;
  private List<PositionLinkType> positionTypes = null;
  private List<SimilarityLinkType> similarityTypes = null;

  // string constant for logs
  private static final String TO_CONTRIBUTION = " to contribution ";
  private static final String CONTRIBUTION_NOT_FOUND = "no contribution found for given id ";
  private static final String INTO = " into ";
  private static final String MERGING = "merging ";
  private static final String UNABLE_TO_MERGE = "unable to merge ";

  // hadcoding default group id to avoid circular refs
  private static final int DEFAULT_GROUP = 0;

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Override
  public Contribution retrieve(Long id, EContributionType type) {
    // if no type passed, return null
    if (type == null) {
      logger.warn("no contribution type passed");
      return null;
    }

    // retrieve object
    be.webdeb.infra.persistence.model.Contribution contribution =
        be.webdeb.infra.persistence.model.Contribution.findById(id);

    // if we got one and it has the right type (or the ALL type)
    if (contribution != null &&
        (contribution.getContributionType().getIdContributionType() == type.id() || EContributionType.ALL.equals(type))) {
      try {
        return mapper.toContribution(contribution);
      } catch (FormatException e) {
        logger.error("unable to cast generic contribution " + id, e);
      }
    }
    //logger.warn("no contribution found for id " +  id);
    return null;
  }

  @Override
  public Contribution retrieveContribution(Long id) {
    // retrieve object
    be.webdeb.infra.persistence.model.Contribution contribution =
            be.webdeb.infra.persistence.model.Contribution.findById(id);

    // if we got one
    if (contribution != null) {
      try {
        return mapper.toContribution(contribution);
      } catch (FormatException e) {
        logger.error("unable to cast generic contribution " + id, e);
      }
    }
    logger.warn("no contribution found for id " +  id);
    return null;
  }

  @Override
  public ContextContribution retrieveContextContribution(Long id) {
    // retrieve object
    be.webdeb.infra.persistence.model.Contribution contribution =
            be.webdeb.infra.persistence.model.Contribution.findById(id);

    // if we got one
    if (contribution != null) {
      try {
        return mapper.toContextContribution(contribution);
      } catch (FormatException e) {
        logger.error("unable to cast generic context contribution " + id, e);
      }
    }
    //logger.warn("no context contribution found for id " +  id);
    return null;
  }

  @Override
  public List<ContributionType> getContributionTypes() {
    if (contributionTypes == null) {
      contributionTypes = TContributionType.find.all().stream().map(t ->
        factory.createContributionType(t.getIdContributionType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return contributionTypes;
  }

  @Override
  public long getAmountOf(EContributionType type, int group) {
    return be.webdeb.infra.persistence.model.Contribution.getAmountOf(type.id(), group);
  }

  @Override
  public final ContributionHistory getCreator(Long contribution) {
    be.webdeb.infra.persistence.model.ContributionHasContributor contributor =
        be.webdeb.infra.persistence.model.Contribution.getCreatorHistory(contribution);
    if (contributor != null) {
      try {
        return toContributionHistory(contributor);
      } catch (FormatException e) {
        logger.error("unable to construct API contributor for creator of " + contribution, e);
      }
    } else {
      logger.error("given contribution id does not exist or it has no creator " + contribution);
    }
    return null;
  }

  @Override
  public ContributionHistory getLatestContributor(Long contribution) {
    be.webdeb.infra.persistence.model.ContributionHasContributor contributor =
            be.webdeb.infra.persistence.model.Contribution.getLastestContributor(contribution);

    if (contributor != null) {
      try {
        return toContributionHistory(contributor);
      } catch (FormatException e) {
        logger.warn("unable to create history line for " + contributor, e);
      }
    } else {
      logger.error("given contribution id does not exist or it has no contributor " + contribution);
    }
    return null;
  }

  @Override
  public final be.webdeb.core.api.contributor.Contributor getLastContributorInGroup(Long contribution, int group){
    be.webdeb.infra.persistence.model.Contributor contributor =
            be.webdeb.infra.persistence.model.Contribution.getLastContributorInGroup(contribution, group);
    if (contributor != null) {
      try {
        return mapper.toContributor(contributor);
      } catch (FormatException e) {
        logger.error("unable to construct API contributor for creator of " + contribution, e);
      }
    } else {
      logger.error("given contribution id does not exist or it has no creator " + contribution + " or given grop does not exist " + group);
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.contributor.Contributor> getContributors(Long contribution) {
    be.webdeb.infra.persistence.model.Contribution c =
        be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    List<be.webdeb.core.api.contributor.Contributor> contributors = new ArrayList<>();
    if (c != null) {
      try {
        for (ContributionHasContributor chc : c.getContributionHasContributors()) {
          contributors.add(mapper.toContributor(chc.getContributor()));
        }
      } catch (FormatException e) {
        logger.error("unable to construct API contributor for creator of " + contribution, e);
      }
    } else {
      logger.error("given contribution id does not exist " + contribution);
    }
    return contributors;
  }

  @Override
  public List<ActorRole> getActors(Long contribution) {
    Transaction transaction = Ebean.getDefaultServer().currentTransaction();
    boolean commitNeeded = transaction == null;
    if (commitNeeded) {
      transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    }
    be.webdeb.infra.persistence.model.Contribution contrib =
        be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    try {
      if (contrib != null) {
          List<ActorRole> result = new ArrayList<>();
          for (ContributionHasActor cha : contrib.getActors()) {
            result.add(mapper.toActorRole(cha, mapper.toActor(cha.getActor()), mapper.toContribution(contrib)));
          }
          return result;

      } else {
        logger.error("given id does not exist " + contribution);
      }
    } catch (FormatException e) {
      logger.error("unable to construct API actors for bound actors of " + contribution, e);

    } finally {
      if (commitNeeded) {
        transaction.end();
      }
    }
    return new ArrayList<>();
  }

  @Override
  public synchronized List<ActorRole> getActors(Long contribution, int limit, EActorRole role) {
    be.webdeb.infra.persistence.model.Contribution contrib =
            be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    try {
      if (contrib != null) {
        List<ActorRole> result = new ArrayList<>();

        for (ContributionHasActor cha : ContributionHasActor.findContributionHasActorLimited(contribution, limit, role)) {
          result.add(mapper.toActorRole(cha, mapper.toActor(cha.getActor()), mapper.toContribution(contrib)));
        }
        return result;
      } else {
        logger.error("given id does not exist " + contribution);
      }
    } catch (FormatException e) {
      logger.error("unable to construct API actors for bound actors of " + contribution, e);
    }

    return new ArrayList<>();
  }

  @Override
  public int getNbActors(Long contribution, EActorRole role) {
    return ContributionHasActor.getNbActors(contribution, role);
  }

  @Override
  public Place findPlace(Long id) {
    try {
      return mapper.toPlace(be.webdeb.infra.persistence.model.Place.find.byId(id));
    } catch (Exception e) {
      logger.error("unable create place from db  " + id, e);
    }
    return null;
  }

  @Override
  public List<Place> findPlace(String name, int fromIndex, int toIndex) {

    try {
      return buildPlacesList(be.webdeb.infra.persistence.model.Place.findByPartialTitle(name, fromIndex, toIndex));
    } catch (Exception e) {
      logger.error("unable to search for value " + name, e);
    }
    return new ArrayList<>();

  }

  @Override
  public List<Contribution> findByValue(String value, int group, int fromIndex, int toIndex) {
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      return toContributions(be.webdeb.infra.persistence.model.Contribution.findBySortKey(value, group, fromIndex, toIndex));
    } catch (Exception e) {
      logger.error("unable to search for value " + value, e);
    } finally {
      transaction.end();
    }
    return new ArrayList<>();

  }

  @Override
  public List<Contribution> findByCriteria(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex) {
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      return toContributions(be.webdeb.infra.persistence.model.Contribution.findByCriteriaV2(criteria, strict, fromIndex, toIndex));
    } catch (Exception e) {
      logger.error("unable to execute query with given criteria " + criteria, e);

    } finally {
      transaction.end();
    }
    return new ArrayList<>();
  }

  @Override
  public List<Contribution> getLatestEntries(EContributionType type, Long contributor, int amount, int group) {
    return toContributions(be.webdeb.infra.persistence.model.Contribution.findLatestEntries(type.id(), contributor, amount, group));
  }

  @Override
  public List<Contribution> getPopularEntries(EContributionType type, Long contributor, int group, int fromIndex, int toIndex, EOrderBy orderBy) {
    return toContributions(be.webdeb.infra.persistence.model.Contribution.findPopularEntries(type, contributor, group, fromIndex, toIndex, orderBy));
  }

  @Override
  public final List<Contribution> bindActor(Long contribution, ActorRole role, int currentGroup, Long contributor)
      throws PermissionException, PersistenceException {

    logger.debug("try to bind actor " + role.toString() + TO_CONTRIBUTION + contribution);

    // check if contributor can be found
    be.webdeb.infra.persistence.model.Contributor dbContributor = checkContributor(contributor, currentGroup);

    // check if contribution exists
    be.webdeb.infra.persistence.model.Contribution dbContribution = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if (dbContribution == null) {
      logger.error(CONTRIBUTION_NOT_FOUND + contribution);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contribution.class, contribution);
    }

    Transaction transaction = Ebean.getDefaultServer().currentTransaction();
    boolean commitNeeded = transaction == null;

    if (commitNeeded) {
      logger.debug("no transaction running (binding of actor saved by itself), create one");
      transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    }
    List<Contribution> result = new ArrayList<>();

    try {
      // check if actor does not exit or its affiliation role does not exist
      if (role.getActor().getId() == -1L) {
        logger.debug("unknown actor " + role.getActor().getFullname(factory.getDefaultLanguage()));
        // does he have an affiliation ? if so, add it to actor
        if (role.getAffiliation() != null && role.getAffiliation().getId() == -1L) {
          role.getActor().addAffiliation(role.getAffiliation());
        }
        // set inGroup value for actor
        role.getActor().addInGroup(currentGroup);
        // we will get at most two actors back (but one at least) since, first one is the actor to be bound
        Map<Integer, List<Contribution>> c = actorAccessor.save(role.getActor(), currentGroup, contributor);
        if(c.containsKey(EContributionType.ACTOR.id()))
          result.addAll(c.get(EContributionType.ACTOR.id()));

        result.add(role.getActor());

      }

      // if given role's affiliation does not exist, add it
      if (role.getAffiliation() != null && role.getAffiliation().getId() == -1L) {
        // add this affiliation to actor and save it
        int index = role.getActor().getAffiliations().indexOf(role.getAffiliation());
        if (index != -1) {
          logger.debug("unknown affiliation has been bound to " + role.getActor().getAffiliations().get(index));
          role.setAffiliation(role.getActor().getAffiliations().get(index));
        } else {
          logger.info("register unknown affiliation for actor " + role.getActor().getFullname(factory.getDefaultLanguage()));
          // add this affiliation to actor to be saved
          Map<Integer, List<Contribution>> c = role.getAffiliation().save(role.getActor().getId(), currentGroup, contributor);
          if(c.containsKey(EContributionType.ACTOR.id()))
            result.addAll(c.get(EContributionType.ACTOR.id()));
        }
      }

      // now we can store the mapping from the contribution to this actor
      ContributionHasActor cha = ContributionHasActor.findContributionHasActor(contribution, role.getActor().getId(), role.isAuthor(), role.isReporter(), role.isJustCited());
      if (cha == null) {
        cha = new ContributionHasActor(contribution, role.getActor().getId());
      }
      cha.setIsAuthor(role.isAuthor());
      cha.setIsReporter(role.isReporter());
      cha.setIsAbout(role.isJustCited());
      if (role.getAffiliation() != null) {
        cha.setActorIdAha(role.getAffiliation().getId());
      } else {
        // force null value (in cas of update)
        cha.setActorIdAha(null);
      }

      cha.save();
      // only commit if we created a dedicated transaction for it
      if (commitNeeded) {
        transaction.commit();
        bindContributor(dbContribution, dbContributor, EModificationStatus.UPDATE);
      }
      logger.info("bound actor to " + cha.toString() + " with id " + cha.getIdCha());

    } catch (PermissionException e) {
      // wrap permission error key as persistence error message
      logger.error("permission error while binding actor " + role.getActor().getFullname(factory.getDefaultLanguage())
          + TO_CONTRIBUTION + contribution, e);
      throw e;

    } catch (Exception e) {
      String more = "error while binding actor " + role.getActor().getFullname(factory.getDefaultLanguage())
          + TO_CONTRIBUTION + contribution;
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.BIND_ACTOR, more, e);

    } finally {
      // only commit if we created a dedicated transaction for it
      if (commitNeeded) {
        transaction.end();
      }
    }

    // return newly created actor, if needed
    return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public final void unbindActor(Long contribution, Long actor, Long contributor) throws PersistenceException {

    // check we may retrieve all needed info
    be.webdeb.infra.persistence.model.Contributor dbContributor = be.webdeb.infra.persistence.model.Contributor.findById(contributor);
    if (dbContributor == null) {
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

    ContributionHasActor cha = ContributionHasActor.findContributionHasActor(contribution, actor);
    if (cha == null) {
      throw new ObjectNotFoundException(ContributionHasActor.class, contribution);
    }

    // now unbind
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      cha.delete();
      bindContributor(be.webdeb.infra.persistence.model.Contribution.findById(contribution),
          dbContributor, EModificationStatus.UPDATE);

      logger.info("deleted binding between " + contribution + " and actor " + actor);
      transaction.commit();

    } catch (Exception e) {
      String more = "error while deleting binding from contribution " + contribution + " to actor " + actor;
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.UNBIND_ACTOR, more, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public final List<Contribution> bindTags(Long contribution, List<be.webdeb.core.api.tag.Tag> tags, Long contributor)
          throws PermissionException, PersistenceException {
    logger.debug("try to bind tags");
    List<Contribution> results = new ArrayList<>();

    // check if contribution exists
    be.webdeb.infra.persistence.model.Contribution dbContribution = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if (dbContribution == null) {
      logger.error(CONTRIBUTION_NOT_FOUND + contribution);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contribution.class, contribution);
    }

    Transaction transaction = Ebean.getDefaultServer().currentTransaction();
    boolean commitNeeded = transaction == null;
    if (commitNeeded) {
      logger.debug("no transaction running (binding of tag saved by itself), create one");
      transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    }

    try {
      List<Tag> dbTags = new ArrayList<>();
      for(be.webdeb.core.api.tag.Tag tag : tags){
        // check if tag does not exit
        Tag dbTag = Tag.findById(tag.getId());

        if(dbTag == null) {
          tagAccessor.save(tag, contributor);
          dbTag = Tag.findById(tag.getId());
        }

        if(dbTag != null) {
          forceContributionUpdate(dbTag.getContribution());
          dbTags.add(dbTag);
        }
      }

      dbContribution.setTags(dbTags);
      dbContribution.update();

      // only commit if we created a dedicated transaction for it
      if (commitNeeded) {
        transaction.commit();
      }
    } catch (PermissionException e) {
      // wrap permission error key as persistence error message
      logger.error("permission error while binding contribution " + contribution.toString()
              + TO_CONTRIBUTION + contribution, e);
      throw e;
    } finally {
      // only commit if we created a dedicated transaction for it
      if (commitNeeded) {
        transaction.end();
      }
    }

    // return newly created folders, if needed
    return results.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public boolean addInGroupAndUpdate(Long contribution, int group) throws PersistenceException {
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    be.webdeb.infra.persistence.model.Group g = be.webdeb.infra.persistence.model.Group.findById(group);

    if(c != null && g != null){
      c.addGroup(g);
      c.update();
      return true;
    } else {
      throw new PersistenceException(PersistenceException.Key.NOT_FOUND);
    }
  }

  @Override
  public Set<be.webdeb.core.api.tag.Tag> getContributionsTags(Long contribution){
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if(c != null){
      List<Tag> tags = c.getTags();
      return (tags != null ? buildTagSet(tags) : new HashSet<>());
    }

    return new HashSet<>();
  }

  @Override
  public List<Place> getContributionsPlaces(Long contribution){
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if(c != null){
      List<be.webdeb.infra.persistence.model.Place> places = c.getPlaces();
      return (places != null ? buildPlacesList(places) : new ArrayList<>());
    }

    return new ArrayList<>();
  }

  @Override
  public void remove(Long contribution, EContributionType type, Long contributor) throws PermissionException,  PersistenceException {
    remove(contribution, type, contributor, ERemoveOption.TOTAL);
  }

  @Override
  public void remove(Long contribution, EContributionType type, Long contributor, ERemoveOption option) throws PermissionException,  PersistenceException {
    logger.debug("will remove " + contribution + " of type " + type.name() + " for contributor " + contributor + " for option " + option);
    be.webdeb.infra.persistence.model.Contributor cor =
            be.webdeb.infra.persistence.model.Contributor.findById(contributor);
    be.webdeb.infra.persistence.model.Contribution cib =
            be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    boolean removeAllJustications = false;
    boolean replaceAllJustications = false;
    Long replaceId = null;
    EContributionType replaceType = null;

      if (cor == null) {
      throw new ObjectNotFoundException(Contributor.class, contributor);
    }

      if (cib == null || type.id() != cib.getContributionType().getIdContributionType()) {
      throw new ObjectNotFoundException(Contribution.class, contribution);
    }

    // in case of many groups, user is admin, or in case of one group user is owner of group
      if (!contributorIsOwnerOfContribution(cor, cib)) {
      throw new PermissionException(PermissionException.Key.NOT_GROUP_OWNER);
    }

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
      boolean notDeleted = false;

      try {
      // must handle deletion of bound contributions by hand because of soft delete
      switch (cib.getContributionType().getEContributionType()) {

        case ACTOR:

          be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(cib.getIdContribution());

          // update affiliations
          actor.getAffiliations().forEach(ActorHasAffiliation::delete);
          // update affiliated actors
          actor.getActors().forEach(ActorHasAffiliation::delete);
          // update link to contributions
          actor.getContributions(-1).forEach(ContributionHasActor::delete);



        case TAG :

          DebateLink.findByTag(cib.getIdContribution())
            .forEach(DebateLink::delete);

          be.webdeb.infra.persistence.model.ContextHasCategory.findByCategory(cib.getIdContribution())
                  .forEach(be.webdeb.infra.persistence.model.ContextHasCategory::delete);

          break;

        case TEXT:
          removeAllJustications = true;
          be.webdeb.infra.persistence.model.ExternalContribution origin = cib.getOriginExternalContribution();
          if(origin != null) {
            remove(origin.getIdContribution(), origin.getContribution().getContributionType().getEContributionType(), contributor);
          }

          for (Citation citation : cib.getText().getCitations()) {
            remove(citation.getIdContribution(), EContributionType.CITATION, contributor);
          }

          for(DebateHasText l : DebateHasText.findLinkByText(contribution)) {
            remove(l.getIdContribution(), EContributionType.DEBATE_HAS_TEXT, contributor);
          }

          break;

        case ARGUMENT:
          removeAllJustications = true;

          for (ArgumentSimilarity link : cib.getArgument().getSimilarArguments()) {
            remove(link.getIdContribution(), EContributionType.ARGUMENT_SIMILARITY, contributor);
          }

          for (ArgumentJustification justification : ArgumentJustification.findLinksForContribution(cib.getIdContribution(), cib.getContributionType().getEContributionType(), true)) {
            remove(justification.getIdContribution(), EContributionType.ARGUMENT_JUSTIFICATION, contributor);
          }

          break;

        case DEBATE:
          removeAllJustications = true;

          for(DebateLink link : DebateLink.findByDebate(contribution)) {
            link.delete();
          }

          for (DebateExternalUrl url : cib.getDebate().getExternalUrls()) {
            url.delete();
          }

          for (DebateSimilarity link : cib.getDebate().getSimilarDebates()) {
            remove(link.getIdContribution(), EContributionType.DEBATE_SIMILARITY, contributor);
          }

          for(DebateHasText l : DebateHasText.findLinkByDebate(contribution)) {
            remove(l.getIdContribution(), EContributionType.DEBATE_HAS_TEXT, contributor);
          }

          for (ContextHasSubDebate l : ContextHasSubDebate.findByDebate(contribution)) {
            remove(l.getIdContribution(), EContributionType.CONTEXT_HAS_SUBDEBATE, contributor);
          }

          break;

        case CITATION:
          removeAllJustications = true;

          for (CitationPosition link : CitationPosition.findLinksForCitation(contribution)) {
            link.delete();
          }

          origin = cib.getOriginExternalContribution();
          if(origin != null) {
            remove(origin.getIdContribution(), origin.getContribution().getContributionType().getEContributionType(), contributor);
          }

          break;

        case CONTEXT_HAS_CATEGORY:
          replaceAllJustications =
                  cib.getContextHasCategory().getContext().getContributionType().getEContributionType() != EContributionType.TEXT;
          replaceId = cib.getContextHasCategory().getCategory().getIdContribution();
          replaceType = EContributionType.TAG;

          break;

        case ARGUMENT_JUSTIFICATION:
          replaceAllJustications =
                  cib.getArgumentJustificationLink().getContext().getContributionType().getEContributionType() != EContributionType.TEXT;
          replaceId = cib.getArgumentJustificationLink().getArgument().getIdContribution();
          replaceType = EContributionType.ARGUMENT;

          for (ContextHasSubDebate link : ContextHasSubDebate.findByArgumentJustification(contribution)) {
            link.setArgument(null);
            link.update();
          }

          break;

        case CITATION_JUSTIFICATION:
          CitationJustification justification = cib.getCitationJustificationLink();

          if(justification.getCategory() != null && option != ERemoveOption.TOTAL) {
            notDeleted = true;

            justification.setCategory(null);
            justification.setSuperArgument(null);

            if(justification.isUnique())
              justification.update();
          }

        default:
          // ignore others
      }

      ContributionHasClaim.findByContribution(contribution).forEach(Model::delete);

      ContributionToExplore.findContributionToExploresForContribution(contribution).forEach(ContributionToExplore::delete);

      for (ArgumentJustification justification : ArgumentJustification.findLinksForContribution(cib.getIdContribution(), cib.getContributionType().getEContributionType())) {
        remove(justification.getIdContribution(), EContributionType.ARGUMENT_JUSTIFICATION, contributor, ERemoveOption.TOTAL);
      }

      if(removeAllJustications) {
        for (CitationJustification justification : CitationJustification.findLinksForContribution(cib.getIdContribution(), cib.getContributionType().getEContributionType())) {
          remove(justification.getIdContribution(), EContributionType.CITATION_JUSTIFICATION, contributor, ERemoveOption.TOTAL);
        }
      }

      for (ContextHasSubDebate l : ContextHasSubDebate.findByContext(contribution)) {
        remove(l.getIdContribution(), EContributionType.CONTEXT_HAS_SUBDEBATE, contributor);
      }

      if(replaceAllJustications && option != ERemoveOption.TOTAL) {
        for (CitationJustification justification : CitationJustification.findLinksForContribution(replaceId, replaceType)) {
          switch (replaceType){
            case TAG:
              justification.setCategory(null);
              justification.setSuperArgument(null);
              break;
            case ARGUMENT:
              justification.setSuperArgument(null);
              break;
          }

          if(justification.isUnique())
            justification.update();
        }
      }

      if(!notDeleted) {
        // trace deletion
        bindContributor(cib, cor, EModificationStatus.DELETE);
        // now delete this contribution
        cib.delete();
        logger.info("removed contribution " + contribution);
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("error while removing contribution " + contribution);
      throw new PersistenceException(PersistenceException.Key.DELETE_CONTRIBUTION, e);

    } finally {
      transaction.end();
    }
  }

  @Override
  public void saveMarkings(List<Contribution> contributions) throws PersistenceException {
    logger.debug("will save markings");

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      for (Contribution contribution : contributions) {
        // get contribution
        be.webdeb.infra.persistence.model.Contribution c =
            be.webdeb.infra.persistence.model.Contribution.findById(contribution.getId());
        if (c != null) {
          logger.debug("set validated " + contribution.getValidated().getType() + " for contribution " + contribution.getId());
          // set marking and validated state
          c.setValidated(TValidationState.findById(contribution.getValidated().getType()));
          c.update();
        }
      }
      transaction.commit();
    } catch (Exception e){
      logger.error("unable to update validated state", e);
      throw new PersistenceException(PersistenceException.Key.MARK_GROUP, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public boolean merge(Long origin, Long replacement, Long contributor) throws PersistenceException, PermissionException {
    logger.debug("try to merge contributions " + origin + INTO + replacement);
    // check all preconditions
    be.webdeb.infra.persistence.model.Contributor cor =
        be.webdeb.infra.persistence.model.Contributor.findById(contributor);

    if (cor == null) {
      logger.error("no contributor found for given id " + contributor);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contributor.class, contributor);
    }

    be.webdeb.infra.persistence.model.Contribution old = be.webdeb.infra.persistence.model.Contribution.findById(origin);
    if (old == null) {
      logger.error(CONTRIBUTION_NOT_FOUND + origin);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contribution.class, origin);
    }

    be.webdeb.infra.persistence.model.Contribution repl = be.webdeb.infra.persistence.model.Contribution.findById(replacement);
    if (repl == null) {
      logger.error(CONTRIBUTION_NOT_FOUND + replacement);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contribution.class, replacement);
    }

    if (!contributorIsOwnerOfContribution(cor, old) || !contributorIsOwnerOfContribution(cor, repl)) {
      logger.error("contributor is not ozner of either " + old.getIdContribution() + " or " + repl.getIdContribution());
      throw new PermissionException(PermissionException.Key.NOT_GROUP_OWNER);
    }

    if (old.getContributionType().getIdContributionType() != repl.getContributionType().getIdContributionType()) {
      String more = "contributions types missmatch, old is " + old.getContributionType().getEn()
          + " replacement is " + repl.getContributionType().getEn();
      logger.error(more);
      throw new PersistenceException(PersistenceException.Key.MERGE_MISMATCH, more);
    }

    // now switch on concrete contribution type
    switch (EContributionType.value(old.getContributionType().getIdContributionType())) {
      case ACTOR:
        merge(repl.getActor().getActortype().getEActorType() == EActorType.UNKNOWN && old.getActor().getActortype().getEActorType() != EActorType.UNKNOWN ? repl.getActor() : old.getActor(),
              repl.getActor().getActortype().getEActorType() == EActorType.UNKNOWN && old.getActor().getActortype().getEActorType() != EActorType.UNKNOWN ? old.getActor() : repl.getActor(),
              cor);
        return !(repl.getActor().getActortype().getEActorType() == EActorType.UNKNOWN && old.getActor().getActortype().getEActorType() != EActorType.UNKNOWN);
      case TEXT:
        merge(old.getText(), repl.getText(), cor);
        break;
      case DEBATE:
        merge(old.getDebate(), repl.getDebate(), cor);
        break;
      case ARGUMENT:
        merge(old.getArgument(), repl.getArgument(), cor);
        break;
      case CITATION:
        merge(old.getCitation(), repl.getCitation(), cor);
        break;
      case TAG:
        merge(old.getTag(), repl.getTag(), cor);
        break;
      default:
        logger.info("no way to merge this contribution type " + old.getContributionType().getEn());
    }

    return true;
  }

  @Override
  public void removeFromGroup(Long contribution, int group, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("will remove contribution " + contribution + " from group " + group);

    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if (c == null) {
      logger.error(CONTRIBUTION_NOT_FOUND + contribution);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contribution.class, contribution);
    }

    // check if group can be found
    be.webdeb.infra.persistence.model.Group g = be.webdeb.infra.persistence.model.Group.findById(group);
    if (g == null) {
      logger.error("no group found for given id " + group);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Group.class, (long) group);
    }

    // contributor exists and has sufficient rights ?
    Contributor cor = checkContributor(contributor, group);
    if (cor == null ||
        cor.getContributorHasGroups().stream().noneMatch(gr -> gr.getGroup().getIdGroup() == group
            && gr.getRole().getIdRole() > EContributorRole.CONTRIBUTOR.id())) {
      logger.error("contributor " + contributor + " has no right on " + c.getIdContribution());
      throw new PermissionException(PermissionException.Key.NOT_GROUP_OWNER);
    }

    // now remove group from given contribution
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // if this contribution does not belong to any group anymore, delete it
      if (c.getGroups().size() == 1) {
        remove(contribution, EContributionType.value(c.getContributionType().getIdContributionType()), contributor);
      } else {
        // trace removal of group in db
        bindContributor(c, cor, EModificationStatus.GROUP_REMOVAL);
        c.getGroups().removeIf(gr -> gr.getIdGroup() == group);
        c.update();
      }
      transaction.commit();
    } catch (Exception e) {
      logger.error("unable to remove group " + group + " from contribution " + contribution, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public boolean isMemberOfAPublicGroup(Long contribution) {
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    if(c != null){

      Set<Integer> publicGroupIds = Group.findPublicGroupIds();

      for(Integer id : c.getGroupIds()){
        if(publicGroupIds.contains(id)){
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public List<ContributionHistory> getHistory(Long contribution) {
    List<ContributionHistory> result = new ArrayList<>();
    for (ContributionHasContributor chc : ContributionHasContributor.findByContribution(contribution)) {
      try {
        result.add(toContributionHistory(chc));

      } catch (FormatException e) {
        logger.warn("unable to create history line for " + chc, e);
      }
    }
    return result;
  }

  @Override
  public List<DebateTag> getTagDebates(Long id, Long contributor, int group) {
    be.webdeb.core.api.debate.Debate debate = debateAccessor.retrieve(id, false);

    if(debate != null) {
      return buildTagDebateList(
              DebateLink.findByDebate(id, contributor, group).stream()
                      .map(link -> {
                        Tag tag = link.getTag();
                        tag.setLinkId(link.getIdContribution());
                        return tag;
                      })
                      .collect(Collectors.toList()),
              debate);
    }

    return new ArrayList<>();
  }

  @Override
  public DebateTag getTagDebate(Long debateId, Long id) {

    DebateLink link = DebateLink.findUniqueByDebateAndTag(debateId, id);
    be.webdeb.core.api.debate.Debate debate = debateAccessor.retrieve(debateId, false);

    if(debate != null && link != null){
      try {
        return mapper.toDebateTag(link.getTag(), debate);
      } catch (FormatException e) {
        logger.debug("unparsable tag debate from context " + debateId + " and id " + id);
      }
    }
    return null;
  }

  @Override
  public List<TagCategory> getCategories(Long id) {
    ContextContribution context = retrieveContextContribution(id);

    if(context != null) {
      return buildCategoriesList(Tag.getCategories(id), context);
    }

    return new ArrayList<>();
  }

  @Override
  public List<ContextHasCategory> getContextCategories(Long id) {
    be.webdeb.infra.persistence.model.Contribution contribution = be.webdeb.infra.persistence.model.Contribution.findById(id);
    ContextContribution context = retrieveContextContribution(id);

    if (contribution != null && context != null) {
      return contribution.getContributionType().getEContributionType() == EContributionType.TAG ?
              buildContextHasCategoryListFromTags(be.webdeb.infra.persistence.model.ContextHasCategory.getTagDebateFakeCategories(id), context) :
              buildContextHasCategoryList(be.webdeb.infra.persistence.model.ContextHasCategory.findByContext(id), context);
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.argument.ArgumentJustification> getArgumentJustificationLinks(Long context, Long subContext, Long category, Long superArgument, Integer shade) {
    ContextContribution contextApi = retrieveContextContribution(context);

    if(contextApi != null) {
      TagCategory apiCategory = tagAccessor.retrieveTagCategory(category, false);
      Argument apiArgument = argumentAccessor.retrieve(superArgument, false);

      return buildArgumentJustificationLinkList(
              ArgumentJustification.findLinksForContext(context, subContext, category, superArgument, shade),
              contextApi,
              apiCategory,
              apiArgument
      );
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.citation.CitationJustification> getCitationJustificationLinks(Long context, Long subContext, Long category, Long superArgument, Integer shade) {
    ContextContribution contextApi = retrieveContextContribution(context);

    if(context != null) {
      TagCategory apiCategory = tagAccessor.retrieveTagCategory(category, false);
      Argument apiArgument = argumentAccessor.retrieve(superArgument, false);


      return buildCitationJustificationLinkList(CitationJustification.findLinksForContext(context, subContext, category, superArgument, shade),
              contextApi,
              apiCategory,
              apiArgument
      );
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.argument.ArgumentJustification> getArgumentJustificationLinks(Long context) {
    ContextContribution contextApi = retrieveContextContribution(context);

    if(contextApi != null) {
      return buildArgumentJustificationLinkList(
              ArgumentJustification.findLinksForContext(context),
              contextApi,
              null,
              null
      );
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.citation.CitationJustification> getCitationJustificationLinks(Long context) {
    ContextContribution contextApi = retrieveContextContribution(context);

    if(context != null) {
      return buildCitationJustificationLinkList(CitationJustification.findLinksForContext(context),
              contextApi,
              null,
              null
      );
    }

    return new ArrayList<>();
  }

  @Override
  public boolean citationJustificationLinkAlreadyExists(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade) {
    return CitationJustification.findUnique(contextId, subContextId, categoryId, superArgumentId, citationId, shade) != null;
  }

  @Override
  public be.webdeb.core.api.citation.CitationJustification findCitationJustificationLink(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade) {
    be.webdeb.infra.persistence.model.CitationJustification justification = be.webdeb.infra.persistence.model.CitationJustification
            .findUnique(contextId, subContextId, categoryId, superArgumentId, citationId, shade);

    if (justification != null) {
      try {
        return mapper.toCitationJustification(justification);
      }catch(FormatException e){
        logger.warn("unable to cast retrieved citation justification");
      }
    } else {
      logger.warn("no citation justification found");
    }
    return null;
  }

  @Override
  public int getMaxCitationJustificationLinkOrder(Long context, Long subContext, Long category, Long argument, int shade) {
    return CitationJustification.getMaxCitationJustificationLinkOrder(context, subContext, category, argument, shade);
  }

  @Override
  public List<be.webdeb.core.api.citation.Citation> getAllCitations(Long id) {
    return buildCitationList(be.webdeb.infra.persistence.model.Contribution.getAllCitations(id));
  }

  @Override
  public List<be.webdeb.core.api.citation.Citation> getAllCitations(SearchContainer query) {
    return buildCitationList(be.webdeb.infra.persistence.model.Contribution.getAllCitations(query));
  }

  @Override
  public List<be.webdeb.core.api.citation.Citation> getCitationsFromJustifications(Long id, Long category) {
    ContextContribution context = retrieveContextContribution(id);

    if(context != null){
      return buildCitationList(CitationJustification.findLinkCitations(id));
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.text.Text> getTextsCitations(Long id, Long contributor, int group) {
    ContextContribution context = retrieveContextContribution(id);

    if(context != null){
      boolean contextIsTag = context.getType() == EContributionType.TAG;
      return buildTextList(
              contextIsTag ? Text.findTextsFromCitationsTag(id, contributor, group) :
                      be.webdeb.infra.persistence.model.Contribution.findLinkTexts(id, contributor, group),
              id,
              contributor,
              group,
              contextIsTag);
    }

    return new ArrayList<>();
  }

  @Override
  public List<be.webdeb.core.api.citation.CitationJustification> getActorCitationJustifications(Long id, Long actor) {
    return buildCitationJustificationLinkList(CitationJustification.findCitationLinksByActor(id, actor));
  }

  @Override
  public List<be.webdeb.core.api.citation.CitationJustification> getTextCitationJustifications(Long id, Long text) {
    return buildCitationJustificationLinkList(CitationJustification.findCitationLinksByText(id, text));
  }

  @Override
  public List<be.webdeb.core.api.citation.CitationPosition> getTextCitationPositions(Long id, Long text) {
    return buildCitationPositionLinkList(CitationPosition.findCitationLinksByText(id, text), null, null);
  }

  @Override
  public List<Language> getLanguages() {
    if (languages == null) {
      languages = TLanguage.find.all().stream().map(t -> {
        LinkedHashMap<String, String> names = new LinkedHashMap<>(t.getTechnicalNames());
        names.put("own", t.getOwn());
        return factory.createLanguage(t.getCode(), names);
      }).collect(Collectors.toList());
    }
    return languages;
  }

  @Override
  public List<ModificationStatus> getModificationStatus() {
    if (modificationStatuses == null) {
      modificationStatuses = TModificationStatus.find.all().stream().map(t ->
              factory.createModificationStatus(t.getIdStatus(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return modificationStatuses;
  }

  @Override
  public List<JustificationLinkType> getJustificationLinkTypes() {
    if(justificationTypes == null) {
      justificationTypes = TJustificationLinkShadeType.find.all().stream().map(t ->
              factory.createJustificationLinkType(t.getIdShade(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return justificationTypes;
  }

  @Override
  public List<PositionLinkType> getPositionLinkTypes() {
    if(positionTypes == null) {
      positionTypes = TPositionLinkShadeType.find.all().stream().map(t ->
              factory.createPositionLinkType(t.getIdShade(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return positionTypes;
  }

  @Override
  public List<SimilarityLinkType> getSimilarityLinkTypes() {
    if(similarityTypes == null) {
      similarityTypes = TSimilarityLinkShadeType.find.all().stream().map(t ->
              factory.createSimilarityLinkType(t.getIdShade(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return similarityTypes;
  }

  @Override
  public Map<EContributionType, Integer> getCountRelationsMap(Long id, Long contributorId, int groupId) {
    be.webdeb.infra.persistence.model.Contribution contribution = be.webdeb.infra.persistence.model.Contribution.findById(id);
    return contribution != null ? contribution.getCountRelationsMap(contributorId, groupId) : new HashMap<>();
  }

  @Override
  public List<Long> getAllIdByContributionType(EContributionType type) {
    return be.webdeb.infra.persistence.model.Contribution.getAllIdByContributionType(type);
  }

  @Override
  public String getTechnicalName(String id, String value, String lang) {
    ETechnicalTableName technicalTableName = ETechnicalTableName.value(id);

    if(technicalTableName != null){
      return TechnicalTable.getTechnicalTableName(technicalTableName, value, lang, id.contains("shade"));
    }

    return null;
  }

  /*
   * Convenience methods
   */

  /**
   * Bind a contributor to a contribution, i.e, we track that a given contributor made some changes to a
   * contribution.
   *
   * @param contribution a contribution id, must exist
   * @param contributor the contributor that created or updated the given contribution
   * @param status the status of the action performed on the contribution
   */
  protected final void bindContributor(be.webdeb.infra.persistence.model.Contribution contribution,
      be.webdeb.infra.persistence.model.Contributor contributor, EModificationStatus status)
      throws PersistenceException {

    logger.debug("try to bind contributor with id " + contributor.getIdContributor() + " to " + contribution.getIdContribution());
    try {
      // enforce refresh from contribution because we are in a transaction and must read uncommited data
      contribution.refresh();
      ContributionHasContributor chc = new ContributionHasContributor(contribution, contributor, TModificationStatus.find.byId(status.id()));

      Optional<ContributionHasContributor> lastModification = ContributionHasContributor.findByContribution(contribution.getIdContribution()).stream().findFirst();
      if(!lastModification.isPresent() || !reduceSerialization(lastModification.get().getSerialization()).equals(reduceSerialization(contribution.toString()))){
        chc.save();
        contribution.update();
        logger.info("saved " + chc.toString());
      }else{
        logger.info("not saved, no modification has been done...");
      }

    } catch (Exception e) {
      String more = "error while binding contributor " + contributor + TO_CONTRIBUTION + contribution;
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.BIND_CONTRIBUTOR, more, e);
    }
  }

  private String reduceSerialization(String serialization){
    return serialization.length() > 34 ? serialization.substring(0, serialization.length() - 34) : serialization;
  }


  /**
   * Bind a contributor to a merge action of two contributions
   *
   * @param origin a contribution id, must exist
   * @param replacement another contribution id, must exist
   * @param contributor the contributor that created or updated the given contribution
   */
  protected final void bindContributor(be.webdeb.infra.persistence.model.Contribution origin,
      be.webdeb.infra.persistence.model.Contribution replacement,
      be.webdeb.infra.persistence.model.Contributor contributor)
      throws PersistenceException {

    logger.debug("try to bind contributor with id " + contributor.getIdContributor() + " to " + replacement.getIdContribution());
    try {
      ContributionHasContributor chc = new ContributionHasContributor(origin, replacement, contributor);
      chc.save();
      logger.info("saved " + chc.toString());

    } catch (Exception e) {
      String more = "error while binding contributor " + contributor + " for replacement of contribution "
          + origin + " by " + replacement;
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.BIND_CONTRIBUTOR, more, e);
    }
  }

  /**
   * Create or update the contributor picture for a given contribution id and contriubutor id
   *
   * @param pictureExtension the file extension of the picture
   * @param contribution the contribution id
   * @param contributor the contributor id
   * @return the created or updated contributor picture or null if none
   */
  protected ContributorPicture updateContributorPicture(String pictureExtension, Long contribution, Long contributor) throws PersistenceException{
    ContributorPicture picture;

    try {
      picture = ContributorPicture.findById(contribution);

      if (!values.isBlank(pictureExtension)) {
        if (picture == null) {
          picture = new ContributorPicture();

          picture.setIdPicture(contribution);
          picture.setContributor(Contributor.findById(contributor));
          picture.setExtension(pictureExtension);
          picture.setLicenceType(TPictureLicenceType.find.byId(EPictureLicenceType.NONE.id()));
          picture.setSource(TContributorPictureSource.find.byId(EContributorPictureSource.UNKNOWN.id()));

          picture.save();
        } else if (!picture.getExtension().equals(pictureExtension)) {
          picture.setContributor(Contributor.findById(contributor));
          picture.setExtension(pictureExtension);
          picture.update();
        }
      }
    }catch(Exception e){
      throw new PersistenceException(PersistenceException.Key.SAVE_PICTURE);
    }

    return picture;
  }

  /**
   * This method checks the common preconditions to any save actions of contribution.
   * If given contribution has an id, it must have the same type as the one retrieved from the
   * repository.
   *
   * @param contribution a contribution to check before being persisted into the repository
   * @param contributor the id of the user that works with the contribution
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @return the db contribution corresponding to given api contribution, if it exists, null otherwise
   * @throws OutdatedVersionException if modifications to an existing element are performed on an outdated
   * element (version numbers do not match)
   * @throws ObjectNotFoundException if given contribution id does not exist
   * @throws PermissionException if given contribution does not belong to given current group
   */
  protected be.webdeb.infra.persistence.model.Contribution checkContribution(Contribution contribution, Long contributor, int currentGroup)
      throws PermissionException, ObjectNotFoundException, OutdatedVersionException {
    // check if contribution exists, right type and version number
    if (contribution == null) {
      logger.error("given contribution is null");
      throw new ObjectNotFoundException(Contribution.class, null);
    }

    be.webdeb.infra.persistence.model.Contribution c = null;
    if (contribution.getId() != null && contribution.getId() != -1L) {
      c = be.webdeb.infra.persistence.model.Contribution.findById(contribution.getId());

      if (c == null || c.getContributionType().getIdContributionType() != contribution.getType().id()) {
        logger.error(CONTRIBUTION_NOT_FOUND + contribution.getId());
        throw new ObjectNotFoundException(contribution.getClass(), contribution.getId());

      } else {
        long version = c.getVersion().getTime();
        if (version > contribution.getVersion()) {
          logger.error("old version for given object " + contribution.getId()
              + " actual is " + version + " (" + new Date(version) + ") and yours is " + contribution.getVersion()
              + " (" + new Date(contribution.getVersion()) + ")");
          Contribution newOne = null;
          try {
            newOne = mapper.toContribution(c);
          } catch (FormatException e) {
            logger.error("unable to cast retrieved contribution " + c.getIdContribution(), e);
          }
          throw new OutdatedVersionException(contribution, newOne);
        }
      }
    }

    // check if groups can be found
    for (be.webdeb.core.api.contributor.Group group : contribution.getInGroups()) {
      if (be.webdeb.infra.persistence.model.Group.findById(group.getGroupId()) == null) {
        logger.error("no group found for given id " + group);
        throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Group.class, (long) group.getGroupId());
      }
    }

    be.webdeb.infra.persistence.model.Contributor user = Contributor.findById(contributor);
    // check if current group belongs to contribution groups
    // currently, we don't check for actor contribution due to private group problem (to change)
    if ((user == null || !user.isAdmin()) && !contribution.getType().isAlwaysPublic() &&
        contribution.getInGroups().stream().noneMatch(g -> g.getGroupId() == currentGroup)) {
      String more = "current group " + currentGroup + " is not in contribution group list " + contribution.getInGroups();
      logger.error(more);
      throw new PermissionException(PermissionException.Key.ERROR_SCOPE, more);
    }
    return c;
  }

  /**
   * This method checks the common preconditions to any save actions of contribution regarding a contributor.
   *
   * @param contributor a contributor id to check if existing
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @return the db contributor corresponding to given api contributor
   * @throws PersistenceException if contributor is not found, there is a type mismatch, or version numbers do not correspond
   * @throws PermissionException if contributor does not belong to given group
   */
  be.webdeb.infra.persistence.model.Contributor checkContributor(Long contributor, int currentGroup) throws PersistenceException, PermissionException {
    // check if contributor can be found
    be.webdeb.infra.persistence.model.Contributor c = be.webdeb.infra.persistence.model.Contributor.findById(contributor);
    if (c == null) {
      logger.error("no contributor found for given id " + contributor);
      throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Contributor.class, contributor);
    }

    // check if contributor can publish in group
    if (c.getContributorHasGroups().stream().noneMatch(g -> g.getGroup().getIdGroup() == currentGroup)) {
      logger.error("contributor " + contributor + " may not publish in group " + currentGroup);
      throw new PermissionException(PermissionException.Key.NOT_GROUP_MEMBER);
    }
    return c;
  }

  /**
   * Update groups in given db contribution with given api contribution groups. Will only add all groups
   * in given api object into given db object, groups that are not present in api object are not removed
   * from given db object. To explicitly remove groups, you must use the dedicated removeFromGroup method
   *
   * @param api an api contribution
   * @param db a db contribution that will be updated with groups in given api contribution
   */
  protected final void updateGroups(Contribution api, be.webdeb.infra.persistence.model.Contribution db) {
    switch(api.getType()){
      case DEBATE:
      case CITATION:
      case ARGUMENT_JUSTIFICATION:
      case CITATION_JUSTIFICATION:
      case CONTEXT_HAS_CATEGORY:
      case DEBATE_HAS_TAG_DEBATE:
        forceUpdateGroups(api, db);
        break;
      default:
        db.addGroup(Group.getPublicGroup());
        forceUpdateGroups(api, db);
    }
  }

  /**
   * Update groups in given db contribution with given api contribution groups. Force update.
   *
   * @param api an api contribution
   * @param db a db contribution that will be updated with groups in given api contribution
   */
  protected final void forceUpdateGroups(Contribution api, be.webdeb.infra.persistence.model.Contribution db) {
    api.getInGroups().forEach(g -> db.addGroup(be.webdeb.infra.persistence.model.Group.findById(g.getGroupId())));
  }

  /**
   * Manage bindings to actors, retrieve them, save unexisting ones and remove others.
   * Update the list of auto-created actors (as contribution)
   *
   * @param apiContribution the api contribution as updated by a contributor
   * @param contribution the existing contribution as persisted in database
   * @param currentGroup the current group id to be used for auto-saved actors
   * @param contributor the contributor that performs the update
   * @param contributions the map/list of auto-created contributions
   */
  final void updateActorBindings(TextualContribution apiContribution,
      be.webdeb.infra.persistence.model.Contribution contribution, int currentGroup, Long contributor, Map<Integer, List<Contribution>> contributions)
      throws PersistenceException, PermissionException {

    contributions.put(EContributionType.ACTOR.id(), new ArrayList<>());

    for (ActorRole a : apiContribution.getActors()) {
      logger.debug("will bind actor " + a.toString());
      // either create or update mapping
      contributions.get(EContributionType.ACTOR.id())
              .addAll(bindActor(contribution.getIdContribution(), a, currentGroup, contributor));
    }

    removeOldAffiliations(
            ContributionHasActor.findContributionHasActors(contribution.getIdContribution(), true, false, false),
            apiContribution.getActors().stream().filter(ActorRole::isAuthor).collect(Collectors.toList()),
            contributor
    );

    removeOldAffiliations(
            ContributionHasActor.findContributionHasActors(contribution.getIdContribution(), false, true, false),
            apiContribution.getActors().stream().filter(ActorRole::isReporter).collect(Collectors.toList()),
            contributor
    );

    removeOldAffiliations(
            ContributionHasActor.findContributionHasActors(contribution.getIdContribution(), false, false, true),
            apiContribution.getActors().stream().filter(ActorRole::isJustCited).collect(Collectors.toList()),
            contributor
    );
  }

  private void removeOldAffiliations(List<ContributionHasActor> chaList, List<ActorRole> newRoles, Long contributor) {
    chaList.stream()
          .filter(cha -> newRoles.stream().noneMatch(api -> api.getActor().getId().equals(cha.getActor().getIdContribution())))
          .forEach(cha -> ContributionHasActor.deleteStatic(cha.getIdCha()));
  }

  /**
   * Manage bindings to actors, retrieve them, save unexisting ones and remove others.
   * Update the list of auto-created folders (as contribution)
   *
   * @param dbContribution the db contribution
   * @param apiContribution the api contribution as updated by a contributor
   * @param currentGroup the current group id to be used for auto-saved actors
   * @param contributor the contributor that performs the update
   * @param contributions the map/list of auto-created contributions
   */
  final void bindActorsToContribution(be.webdeb.infra.persistence.model.Contribution dbContribution, TextualContribution apiContribution, int currentGroup, Long contributor, Map<Integer, List<Contribution>> contributions)
          throws PermissionException, PersistenceException {

    contributions.put(EContributionType.ACTOR.id(), new ArrayList<>());

    for (ActorRole a : apiContribution.getActors()) {
      logger.debug("will bind actor " + a.toString() + " to " + apiContribution.getId());
      contributions.get(EContributionType.ACTOR.id())
              .addAll(bindActor(dbContribution.getIdContribution(), a, currentGroup, contributor));
    }

  }

  /**
   * Manage bindings to actors, retrieve them, save unexisting ones and remove others.
   * Update the list of auto-created folders (as contribution)
   *
   * @param dbContribution the db contribution
   * @param apiContribution the api contribution as updated by a contributor
   * @param contributor the contributor that performs the update
   */
  final void bindTagsToContribution(be.webdeb.infra.persistence.model.Contribution dbContribution, Contribution apiContribution, Long contributor)
          throws PermissionException, PersistenceException {
    bindTagsToContribution(dbContribution, apiContribution, contributor, null);
  }

  /**
   * Manage bindings to actors, retrieve them, save unexisting ones and remove others.
   * Update the list of auto-created folders (as contribution)
   *
   * @param dbContribution the db contribution
   * @param apiContribution the api contribution as updated by a contributor
   * @param contributor the contributor that performs the update
   * @param contributions the map/list of auto-created contributions
   */
  final void bindTagsToContribution(be.webdeb.infra.persistence.model.Contribution dbContribution, Contribution apiContribution, Long contributor, Map<Integer, List<Contribution>> contributions)
          throws PermissionException, PersistenceException {

    List<be.webdeb.core.api.tag.Tag> tags = apiContribution.getTagsAsList();
    tags.forEach(t -> t.setInGroups(apiContribution.getInGroups()));

    List<Contribution> list = bindTags(dbContribution.getIdContribution(), tags, contributor);

    if(contributions != null)
      contributions.put(EContributionType.TAG.id(), list);
  }

  /**
   * Create a list of Actor18name for given db actor from given api actor
   *
   * @param api the api actor with a list of names
   * @param db the db actor to which those names will be bound
   * @return the db names from given api names
   */
  protected List<ActorI18name> toI18names(Actor api, be.webdeb.infra.persistence.model.Actor db) {
    List<ActorI18name> result = api.getNames().stream().map(n -> toI18name(db, n, false)).collect(Collectors.toList());
    if (EActorType.ORGANIZATION.equals(api.getActorType())) {
      result.addAll(((be.webdeb.core.api.actor.Organization) api).getOldNames().stream().map(n ->
          toI18name(db, n, true)).collect(Collectors.toList()));
    }
    return result;
  }

  /**
   * Create a list of DebateExternalUrl for given db debate from given api debate
   *
   * @param api the api debate with a list of external url
   * @param db the db debate to which those external url will be bound
   * @return the db external urls from given api external urls
   */
  protected List<DebateExternalUrl> toDebateExternalUrls(be.webdeb.core.api.debate.Debate api, be.webdeb.infra.persistence.model.Debate db) {
    return api.getExternalUrls().stream().map(url -> toDebateExternalUrl(db, url)).collect(Collectors.toList());
  }

  @Override
  public List<WarnedWord> getWarnedWords(int contextType, int type, String lang) {
    List<WarnedWord> words = new ArrayList<>();
    TWarnedWord.findByTypesAndLang(type, contextType, lang).forEach(w -> {
      try {
        words.add(factory.createWarnedWord(
                w.getIdWarnedWord(),
                w.getTitle(),
                w.getLanguage().getCode(),
                w.getType().getIdType(),
                w.getContextType().getIdWarnedWordType()));

      } catch (FormatException e) {
        logger.debug("Unparasable warned word " + w.getIdWarnedWord());
      }
    });

    return words;
  }

  @Override
  public Long retrievePlaceContinentCode(String code){
    be.webdeb.infra.persistence.model.Place p = be.webdeb.infra.persistence.model.Place.findContinentByCode(code);
    if(p != null){
      return p.getId();
    }
    return null;
  }

  @Override
  public Place retrievePlace(Long placeId) {
    be.webdeb.infra.persistence.model.Place p = be.webdeb.infra.persistence.model.Place.findByGeonameIdorPlaceId(null, placeId);
    if(p != null){
      return mapper.toPlace(p);
    }
    return null;
  }

  @Override
  public Long retrievePlaceByGeonameIdOrPlaceId(Long geonameId, Long placeId){
    be.webdeb.infra.persistence.model.Place p = be.webdeb.infra.persistence.model.Place.findByGeonameIdorPlaceId(geonameId, placeId);
    if(p != null){
      return p.getId();
    }
    return null;
  }

  @Override
  public PlaceType findPlaceTypeByCode(int code){
    TPlaceType t = TPlaceType.find.byId(code);
    if(t != null){
      return factory.createPlaceType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()));
    }
    return null;
  }

  @Override
  public void savePlaces(List<Place> places, be.webdeb.infra.persistence.model.Contribution contribution){
    contribution.setPlaces(new ArrayList<>());

    for(Place place : places) {
      be.webdeb.infra.persistence.model.Place p = savePlace(place);
      if(p != null) {
        if(contribution.getPlaces().stream().noneMatch(p2 -> p2.getId().equals(p.getId())))
          contribution.getPlaces().add(p);
      }
    }
  }

  @Override
  public be.webdeb.infra.persistence.model.Place savePlace(Place place){
    if(place != null) {
      be.webdeb.infra.persistence.model.Place p =
          be.webdeb.infra.persistence.model.Place.findByGeonameIdorPlaceId(place.getGeonameId(), place.getId());
      if (p == null && place.getPlaceType() != null) {
        p = new be.webdeb.infra.persistence.model.Place(place.getGeonameId(), place.getCode(), place.getLatitude(), place.getLongitude());
        p.setPlaceType(TPlaceType.find.byId(place.getPlaceType().getType()));
        if(p.getPlaceType() != null) {
          p.save();
          final long pId = p.getId();
          p.setSpellings(pId, place.getNames());
          p.setContinent(savePlace(place.getContinent()));
          p.setCountry(savePlace(place.getCountry()));
          p.setRegion(savePlace(place.getRegion()));
          p.setSubregion(savePlace(place.getSubregion()));
          p.update();
        }
      }
      return p;
    }
    return null;
  }

  @Override
  public List<ValidationState> getValidationStates() {
    if (validationStates == null) {
      validationStates = TValidationState.find.all().stream().map(t ->
              factory.createValidationState(t.getIdValidationState(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return validationStates;
  }

  @Override
  public ExternalContribution retrieveExternal(Long id) {
    return null;
  }

  @Override
  public List<ExternalContribution> getExternalContributionsByExternalSource(Long contributor, int externalSource, int maxResults) {
    return null;
  }

  @Override
  public Map<Integer, List<Contribution>> save(ExternalContribution contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    return null;
  }

  /*@Override
  public Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> save(ExternalText contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save external text " + contribution.getTitle() + WITH_ID + contribution.getId());
    Contributor dbContributor = Contributor.findById(contributor);

    TExternalSourceName externalSource = TExternalSourceName.findById(contribution.getSourceId());
    if (externalSource == null) {
      logger.error("unable to retrieve external source " + contribution.getSourceName());
      throw new ObjectNotFoundException(TExternalSourceName.class, -1L);
    }

    if(dbContributor != null) {
      Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
      try {
        be.webdeb.infra.persistence.model.Contribution c;
        be.webdeb.infra.persistence.model.ExternalContribution externalContribution;
        be.webdeb.infra.persistence.model.ExternalText text;

        // create contribution super type
        c = initContribution(EContributionType.EXTERNAL_TEXT.id(), contribution.getTitle(), true);
        updateGroups(contribution, c);
        c.save();

        // create external contribution object and binding
        externalContribution = initExternalContribution(contribution, c, externalSource);

        // create text object and binding
        text = new be.webdeb.infra.persistence.model.ExternalText();
        text.setExternalContribution(externalContribution);

        // set text data
        text.setIdContribution(c.getIdContribution());
        text.setTitle(contribution.getTitle());
        text.save();

        contribution.setId(text.getIdContribution());

        // bind contributor to this external text
        bindContributor(c, dbContributor, EModificationStatus.CREATE);

        transaction.commit();
        logger.info("saved " + text.toString());

        for(ExternalCitation a : contribution.getCitations()){
          a.setTextId(text.getIdContribution());
          a.save(contributor, currentGroup);
        }

      } catch (Exception e) {
        logger.error("unable to save external text " + contribution.getTitle(), e);
        throw new PersistenceException(PersistenceException.Key.SAVE_EXTERNAL_TEXT, e);

      } finally {
        transaction.end();
      }
    }

    return null;
  }*/

  @Override
  public void updateDiscoveredExternalState(Long id, boolean rejected) throws PersistenceException {
    logger.debug("try to set rejected state " + rejected + " to external contribution " + id);

    be.webdeb.infra.persistence.model.ExternalContribution c = be.webdeb.infra.persistence.model.ExternalContribution.findById(id);
    if (c == null) {
      throw new PersistenceException(PersistenceException.Key.SAVE_EXTERNAL);
    }

    try {
      c.setRejected(rejected);
      c.update();
      logger.info("set rejected state of " + id + " to " + (rejected ? "rejected" : "re-added"));
    } catch (Exception e) {
      logger.error("unable to update rejected state of external contribution " + id, e);
    }
  }

  @Override
  public void saveContextContribution(ContextContribution contribution, EContributionType type, Long contributor, int currentGroup) throws PermissionException, PersistenceException {
    logger.debug("try to save context contribution with id " + contribution.getId() + " in group " + contribution.getInGroups());
    checkContribution(contribution, contributor, currentGroup);
    Contributor dbContributor = checkContributor(contributor, currentGroup);

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    be.webdeb.infra.persistence.model.Contribution c = initContribution(type.id(), "");
    c.save();
    // update groups
    updateGroups(contribution, c);

    switch(type){
      case DEBATE:
        Debate d = new Debate();
        c.setDebate(d);
        d.setContribution(c);
        d.setIdContribution(c.getIdContribution());
        d.save();
        break;
      case TEXT:
        Text t = new Text();
        c.setText(t);
        t.setContribution(c);
        t.setIdContribution(c.getIdContribution());
        t.save();
        break;
    }
    c.update();
    bindContributor(c, dbContributor, EModificationStatus.CREATE);

    contribution.setId(c.getIdContribution());
    transaction.commit();
    transaction.end();

  }

  @Override
  public void deleteContextContribution(Long contribution, Long contributor) throws PermissionException {
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    Contributor dbContributor = Contributor.findById(contributor);
    if(c != null && dbContributor != null && ContributionHasContributor.checkContributionCreator(contributor, contribution)){
      c.delete();
    }
  }

  @Override
  public boolean linkAlreadyExists(Long origin, Long destination) {
    return false;
  }

  @Override
  public void createFakeCitations(int nbCitations) {

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

    try {
      Contributor dbContributor = Contributor.getWebdebContributor();
      List<Group> dbGroup = Collections.singletonList(Group.getPublicGroup());
      TModificationStatus status = TModificationStatus.find.byId(EModificationStatus.CREATE.id());
      TLanguage language = TLanguage.find.byId("fr");

      IntStream.range(0, nbCitations).forEach(i -> {
        be.webdeb.infra.persistence.model.Contribution citationContribution = initContribution(EContributionType.CITATION.id(), "");

        Citation citation = new be.webdeb.infra.persistence.model.Citation();
        citationContribution.setCitation(citation);
        citationContribution.save();

        String excerpt = "Extrait " + citationContribution.getIdContribution();

        citation.setIdContribution(citationContribution.getIdContribution());
        citation.setContribution(citationContribution);
        citation.setLanguage(language);
        citation.setOriginalExcerpt(excerpt);
        citation.setWorkingExcerpt(excerpt);
        citation.setText(Text.random());

        ContributionHasActor cha = new ContributionHasActor(
                citationContribution.getIdContribution(),
                be.webdeb.infra.persistence.model.Actor.random(EContributionType.ACTOR)
        );
        cha.setIsAuthor(true);
        cha.save();

        citationContribution.addTag(Tag.random());
        citationContribution.addPlaces(Collections.singletonList(be.webdeb.infra.persistence.model.Place.random()));
        citationContribution.setGroups(dbGroup);

        citationContribution.setSortkey(excerpt);
        citationContribution.update();
        citation.save();

        ContributionHasContributor chc = new ContributionHasContributor(citationContribution, dbContributor, status);
        chc.save();
      });

      transaction.commit();

    } catch (Exception e) {
      logger.error("error while saving citation " + e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public void createFakePositions(int nbDebates, int nbPositionsPerDebate) {

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

    try {
      Contributor dbContributor = Contributor.getWebdebContributor();
      List<Group> dbGroup = Collections.singletonList(Group.getPublicGroup());
      Tag subDebate = Tag.findById(176081L);
      ArgumentShaded argumentShaded = be.webdeb.infra.persistence.model.ArgumentShaded.findById(175956L);
      Citation defaultCitation = Citation.findById(177009L);
      TModificationStatus status = TModificationStatus.find.byId(EModificationStatus.CREATE.id());

      for(int iDebate = 0; iDebate < nbDebates; iDebate++) {
        // create contribution super type
        be.webdeb.infra.persistence.model.Contribution debateContribution = initContribution(EContributionType.DEBATE.id(), "Debate test " + iDebate);
        //create new debate
        Debate debate = new be.webdeb.infra.persistence.model.Debate();
        // create debate and binding
        debateContribution.setDebate(debate);
        debateContribution.save();

        // set id of debate
        debate.setIdContribution(debateContribution.getIdContribution());
        debate.setContribution(debateContribution);
        debate.setArgument(argumentShaded);
        debate.setShade(TDebateShadeType.find.byId(values.getRandomInt(0, 2)));

        // update groups
        debateContribution.setGroups(dbGroup);

        debateContribution.update();
        debate.save();

        ContributionHasContributor chc = new ContributionHasContributor(debateContribution, dbContributor, status);
        chc.save();

        Tag concreteTag = debate.isMultiple() ? subDebate : null;

        createFakePosition(debate, dbContributor, dbGroup, concreteTag, defaultCitation, status, "Position " + iDebate + " " + "-1");

        for(int iPosition = 0; iPosition < nbPositionsPerDebate; iPosition++) {
          createFakePosition(debate, dbContributor, dbGroup, concreteTag, Citation.random(), status, "Position " + iDebate + " " + iPosition);
        }
      }

      transaction.commit();

    } catch (Exception e) {
      logger.error("error while saving contribution " + e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public void updateContributionsRelevance() {
    be.webdeb.infra.persistence.model.Contribution.findContributionsToUpdateRelevance().forEach(contribution -> {
      int relevance = 0;
      int group = be.webdeb.core.api.contributor.Group.getGroupPublic();

      switch (contribution.getContributionType().getEContributionType()) {
        case ACTOR:
          be.webdeb.infra.persistence.model.Actor actor = contribution.getActor();

          if(actor != null) {
            switch (actor.getActortype().getEActorType()) {
              case PERSON:
                relevance =
                        actor.getNbAffiliations() +
                                (3 * (actor.getNbCitations(true, -1L, group) +
                                actor.getNbCitations(false, -1L, group)));
                break;
              case ORGANIZATION:
                relevance =
                        actor.getNbAffiliations() +
                                (5 * (actor.getNbCitations(true, -1L, group) +
                                        actor.getNbCitations(false, -1L, group)));
                break;
            }
          }
          break;
        case DEBATE:
          Debate debate = contribution.getDebate();

          if(debate != null) {
            relevance =
                    debate.getNbLinks(EContributionType.ARGUMENT_JUSTIFICATION, -1L, group) +
                    debate.getNbLinks(EContributionType.CITATION_POSITION, -1L, group);
          }
          break;
        case TEXT :
          Text text = contribution.getText();

          if(text != null) {
            relevance =
                    text.getNbCitations(-1L, group);
          }
          break;
        case TAG :
          Tag tag = contribution.getTag();

          if(tag != null) {
            relevance =
                    Tag.getNbContributions(tag.getIdContribution(), -1L, group) +
                    Tag.getNbLinks(tag.getIdContribution(), -1L, group);
          }
          break;
        case CITATION:
          Citation citation = contribution.getCitation();

          if(citation != null) {
            relevance += citation.findLinkedDebates(-1L, group, null).size();

            relevance += citation.getText() != null ? 1 : 0;

            relevance +=  citation.getContribution().getMostActorRelevantActor() != null ? 1 : 0;

            relevance += citation.getContribution().getTags().size();
          }
          break;
      }

      if(contribution.getRelevance() != null) {
        contribution.getRelevance().setRelevance(relevance);
        contribution.getRelevance().update();
      } else {
        ContributionHasRelevance chr = new ContributionHasRelevance();
        chr.setIdContribution(contribution.getIdContribution());
        chr.setContribution(contribution);
        chr.setRelevance(relevance);
        chr.save();
      }

    });
  }

  @Override
  public boolean claimContribution(Long contribution, Long contributor, String url, String comment, EClaimType type, int group) {
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution);
    Contributor con = Contributor.findById(contributor);
    TClaimType claimType = TClaimType.find.byId(type.id());
    Group g = Group.findById(group);

    if(c != null && con != null && claimType != null && g != null) {
      try {
        ContributionHasClaim claim = new ContributionHasClaim(c, con, url, comment, claimType, g);
        claim.save();
        return true;
      } catch (Exception e) {
        logger.debug(e+"");
        return false;
      }
    }
    logger.debug("no");

    return false;
  }

  @Override
  public boolean deleteClaim(Long contribution, Long contributor) {
    ContributionHasClaim claim = ContributionHasClaim.retrieveByContributionAndContributor(contribution, contributor);

    if(claim != null) {
      try {
        claim.delete();
        return true;
      } catch(Exception e) {
        return false;
      }
    }

    return false;
  }

  @Override
  public List<ClaimHolder> retrieveClaims(Long contributor, int fromIndex, int toIndex, String lang) {
    return ContributionHasClaim.find(contributor, fromIndex, toIndex)
            .stream()
            .map(claim ->
                    new ClaimHolder(
                            claim.getContribution().getIdContribution(),
                            claim.getContributor().getIdContributor(),
                            EClaimType.value(claim.getType().getIdType()),
                            claim.getGroup().getGroupName(),
                            claim.getComment(),
                            claim.getUrl(),
                            new Date(claim.getVersion().getTime()),
                            lang))
            .collect(Collectors.toList());
  }

  @Override
  public boolean contributionCanBeEdited(Long contributorId, Long contributionId, int groupId) {
    Contributor contributor = Contributor.findById(contributorId);

    if(contributor != null) {
      if(contributor.isAdmin())
        return true;
      ContributorHasGroup chg = ContributorHasGroup.byContributorAndGroup(contributorId, groupId);

      if(chg == null || chg.isBanned())
        return false;
      Group group = Group.findById(groupId);

      if(group == null)
        return false;

      be.webdeb.infra.persistence.model.Contribution contribution = be.webdeb.infra.persistence.model.Contribution.findById(contributionId);

      if(contribution == null)
        return false;

      if(contribution.canOnlyBeEditedInGroup()){
        return group.containsContribution(contributionId);
      }

      return true;
    }

    return false;
  }

  /**
   * Increment the amount of hit for given contribution
   *
   * @param contribution a contribution
   */
  protected void addHitToContribution(be.webdeb.infra.persistence.model.Contribution contribution) {
    try {
      if (contribution.getRelevance() == null) {
        ContributionHasRelevance chr = new ContributionHasRelevance();
        chr.setIdContribution(contribution.getIdContribution());
        chr.setContribution(contribution);
        chr.setRelevance(-1);
        chr.save();
      }

      contribution.getRelevance().addHit();
      contribution.getRelevance().update();

    }catch(Exception e){

    }
  }

  private void createFakePosition(Debate debate, Contributor dbContributor, List<Group> dbGroup, Tag subDebate, Citation citation, TModificationStatus status, String sortkey) throws Exception {
    be.webdeb.infra.persistence.model.Contribution positionContribution = initContribution(EContributionType.CITATION_POSITION.id(), sortkey);

    CitationPosition position = new be.webdeb.infra.persistence.model.CitationPosition();
    positionContribution.setCitationPosition(position);
    positionContribution.save();

    position.setIdContribution(positionContribution.getIdContribution());
    position.setContribution(positionContribution);
    position.setDebate(debate);
    position.setSubDebate(subDebate);
    position.setCitation(citation);
    position.setShade(TPositionLinkShadeType.find.byId(values.getRandomInt(0, 4)));

    // update groups
    positionContribution.setGroups(dbGroup);
    positionContribution.update();
    position.save();

    ContributionHasContributor chc = new ContributionHasContributor(positionContribution, dbContributor, status);
    chc.save();
  }

  /**
   * Convert given debate external url into a db external url
   *
   * @param db the db debate holding the external url to the new debate external url
   * @param url the external url to convert
   * @return the mapped external url for this given external url and linked to given db debate
   */
  private DebateExternalUrl toDebateExternalUrl(be.webdeb.infra.persistence.model.Debate db, be.webdeb.core.api.debate.DebateExternalUrl url) {
    return new DebateExternalUrl(url.getIdUrl(), url.getUrl(), url.getAlias(), db);
  }

  /**
   * Convert given actor name into a db actori18name
   *
   * @param db the db actor holding the link to the new actor name
   * @param name the name to convert
   * @param isOld true if this is a previous name (only effective for organizations)
   * @return the mapped actori18name for this given name and linked to given db actor
   */
  private ActorI18name toI18name(be.webdeb.infra.persistence.model.Actor db, ActorName name, boolean isOld) {
    ActorI18name result = new ActorI18name(db, name.getLang());
    result.setPseudo(name.getPseudo());
    result.setName(name.getLast());
    result.setFirstOrAccro(name.getFirst());
    result.isOld(isOld);
    return result;
  }

  /**
   * Create a list of TextI18namefor given db text from given api text
   *
   * @param api the api text with a list of titles
   * @param db the db text to which those titles will be bound
   * @return the db titles from given api titles
   */
  protected List<TextI18name> toTextI18titles(be.webdeb.core.api.text.Text api, Text db) {
    return api.getTitles().entrySet().stream().map(n -> toTextI18title(db, n.getKey(), n.getValue())).collect(Collectors.toList());
  }

  /**
   * Convert given text ntitle into a db textI18name
   *
   * @param db the db text holding the link to the new text title
   * @param lang the lang to convert
   * @param name the name to convert
   * @return the mapped textI18name for this given title and linked to given db text
   */
  private TextI18name toTextI18title(Text db, String lang, String name) {
    return new TextI18name(db, lang, name);
  }

  /**
   * Helper method to wrap DB contributions to API contributions
   * @param contributions a list of DB contributions
   * @return the corresponding list of API contributions
   */
  protected List<Contribution> toContributions(List<be.webdeb.infra.persistence.model.Contribution> contributions) {
    List<Contribution> result = new ArrayList<>();
    if (!contributions.isEmpty()) {
      for (be.webdeb.infra.persistence.model.Contribution c : contributions) {
        try {
          Contribution contribution = mapper.toContribution(c);

          if(contribution != null) {
            result.add(contribution);
          } else {
            logger.error(c.getIdContribution() + " has no sub type contribution...");
          }
        } catch (FormatException e) {
          logger.error("unable to cast contribution " + c.getIdContribution(), e);
        }
      }
    }
    return result;
  }

  /**
   * Helper method to wrap DB context contributions to API context contributions
   * @param contributions a list of DB context contributions
   * @return the corresponding list of API context contributions
   */
  protected List<ContextContribution> toContextContributions(List<be.webdeb.infra.persistence.model.Contribution> contributions) {
    List<ContextContribution> result = new ArrayList<>();
    if (!contributions.isEmpty()) {
      for (be.webdeb.infra.persistence.model.Contribution c : contributions) {
        try {
          result.add(mapper.toContextContribution(c));
        } catch (FormatException e) {
          logger.error("unable to cast context contribution " + c.getIdContribution(), e);
        }
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API affiliations from DB actor has affiliations as member. All uncastable elements are ignored.
   *
   * @param affiliations a list of DB affiliations
   * @return a list of API affiliations with elements that could have actually been casted to API element (may be empty)
   */
  protected List<Affiliation> buildMemberList(List<ActorHasAffiliation> affiliations) {
    List<Affiliation> result = new ArrayList<>();
    for (ActorHasAffiliation aha : affiliations) {
      try {
        result.add(mapper.toMember(aha));
      } catch (FormatException e) {
        logger.error("unable to cast affiliation " + aha.getId(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API affiliations from DB actor has affiliations as affiliation. All uncastable elements are ignored.
   *
   * @param affiliations a list of DB affiliations
   * @return a list of API affiliations with elements that could have actually been casted to API element (may be empty)
   */
  protected List<Affiliation> buildAffiliationList(List<ActorHasAffiliation> affiliations) {
    List<Affiliation> result = new ArrayList<>();
    for (ActorHasAffiliation aha : affiliations) {
      try {
        result.add(mapper.toAffiliation(aha));
      } catch (FormatException e) {
        logger.error("unable to cast affiliation " + aha.getId(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API texts from DB texts. All uncastable elements are ignored.
   *
   * @param texts a list of DB texts
   * @return a list of API texts with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.text.Text> buildTextList(List<be.webdeb.infra.persistence.model.Text> texts) {
    return buildTextList(texts, null, null, 0, false);
  }

  /**
   * Helper method to build a list of API texts from DB texts. All uncastable elements are ignored.
   *
   * @param texts a list of DB texts
   * @param actor the actor to be focus on
   * @param contributor a contributor id
   * @param group the contributor current group id
   * @return a list of API texts with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.text.Text> buildTextList(List<be.webdeb.infra.persistence.model.Text> texts, Long actor, Long contributor, int group) {
    return buildTextList(texts, null, actor, contributor, group, false);
  }

  /**
   * Helper method to build a list of API texts from DB texts. All uncastable elements are ignored.
   *
   * @param texts a list of DB texts
   * @param context the context contribution where these texts are needed
   * @param contributor a contributor id
   * @param group the contributor current group id
   * @param contextIsTag true if the context is a tag
   * @return a list of API texts with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.text.Text> buildTextList(List<be.webdeb.infra.persistence.model.Text> texts, Long context, Long contributor, int group, boolean contextIsTag) {
    return buildTextList(texts, context, null, contributor, group, contextIsTag);
  }

  /**
   * Helper method to build a list of API texts from DB texts. All uncastable elements are ignored.
   *
   * @param texts a list of DB texts
   * @param context the context contribution where these texts are needed
   * @param actor the actor to be focus on
   * @param contributor a contributor id
   * @param group the contributor current group id
   * @param contextIsTag true if the context is a tag
   * @return a list of API texts with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.text.Text> buildTextList(List<be.webdeb.infra.persistence.model.Text> texts, Long context, Long actor, Long contributor, int group, boolean contextIsTag) {
    List<be.webdeb.core.api.text.Text> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Text t : texts) {
      try {
        be.webdeb.core.api.text.Text text = mapper.toText(t);

        if(context != null) {
          text.setNbCitations(t.getNbCitationsInContext(context, contributor, group, contextIsTag));
        }

        if(actor != null) {
          text.setNbCitations(t.getNbCitationsWhereActorAuthor(actor, contributor, group));
        }

        result.add(text);
      } catch (FormatException e) {
        logger.error("unable to cast text " + t.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API debates from DB debates. All uncastable elements are ignored.
   *
   * @param debates a list of DB debates
   * @return a list of API debates with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.debate.Debate> buildDebateList(List<be.webdeb.infra.persistence.model.Debate> debates) {
    List<be.webdeb.core.api.debate.Debate> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Debate d : debates) {
      try {
        result.add(mapper.toDebate(d));
      } catch (FormatException e) {
        logger.error("unable to cast debate " + d.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API tag debates from DB tag. All uncastable elements are ignored.
   *
   * @param tags a list of DB tags
   * @param superDebate the multiple thesis debate where tags come from
   * @return a list of API tag debates with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.debate.DebateTag> buildTagDebateList(List<be.webdeb.infra.persistence.model.Tag> tags, be.webdeb.core.api.debate.Debate superDebate) {
    List<be.webdeb.core.api.debate.DebateTag> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Tag t : tags) {
      try {
        result.add(mapper.toDebateTag(t, superDebate));
      } catch (FormatException e) {
        logger.error("unable to cast tag debate " + t.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API arguments from DB arguments. All uncastable elements are ignored.
   *
   * @param arguments a list of DB arguments
   * @return a list of API arguments with elements that could have actually been casted to API element (may be empty)
   */
  protected List<Argument> buildArgumentList(List<be.webdeb.infra.persistence.model.Argument> arguments) {
    List<Argument> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Argument a : arguments) {
      try {
        result.add(mapper.toArgument(a));
      } catch (FormatException e) {
        logger.error("unable to cast argument " + a.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API argument dictionaries from DB argument dictionaries. All uncastable elements are ignored.
   *
   * @param dictionaries a list of DB argument dictionaries
   * @return a list of API argument dictionaries with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.argument.ArgumentDictionary> buildArgumentDictionaryList(List<ArgumentDictionary> dictionaries) {
    List<be.webdeb.core.api.argument.ArgumentDictionary> result = new ArrayList<>();
    for (ArgumentDictionary d : dictionaries) {
      try {
        result.add(mapper.toArgumentDictionary(d));
      } catch (FormatException e) {
        logger.error("unable to cast argument dictionary " + d.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API citations from DB citations. All uncastable elements are ignored.
   *
   * @param citations a list of DB citations
   * @return a list of API citations with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.citation.Citation> buildCitationList(List<Citation> citations) {
    List<be.webdeb.core.api.citation.Citation> result = new ArrayList<>();
    for (Citation e : citations) {
      try {
        result.add(mapper.toCitation(e));
      } catch (FormatException ex) {
        logger.error("unable to cast citation " + e.getIdContribution(), ex);
      }
    }
    return result;
  }

  /**
   * Helper method to build a map of shade id / API citations from DB shade id / citations. All uncastable elements are ignored.
   *
   * @param citationsMap a map of DB shade id /  citations
   * @return a map of API shade id / citations with elements that could have actually been casted to API element (may be empty)
   */
  protected Map<Integer, List<be.webdeb.core.api.citation.Citation>> buildShadedCitationMap(Map<Integer, List<Citation>> citationsMap) {
    Map<Integer, List<be.webdeb.core.api.citation.Citation>> result = new LinkedHashMap<>();

    for (Map.Entry<Integer, List<Citation>> entry : citationsMap.entrySet()) {
      result.put(entry.getKey(), new ArrayList<>());

      for (Citation e : entry.getValue()) {
        try {
          result.get(entry.getKey()).add(mapper.toCitation(e));
        } catch (FormatException ex) {
          logger.error("unable to cast citation " + e.getIdContribution(), ex);
        }
      }
    }

    return result;
  }

  /**
   * Helper method to build a list of API external contributions from DB external contributions. All uncastable elements are ignored.
   *
   * @param contributions a list of DB external contributions
   * @return a list of API external contributions with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.contribution.ExternalContribution> buildExternalContributionList(List<be.webdeb.infra.persistence.model.ExternalContribution> contributions) {
    List<be.webdeb.core.api.contribution.ExternalContribution> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.ExternalContribution e : contributions) {
      try {
        result.add(mapper.toExternalContribution(e));
      } catch (FormatException ex) {
        logger.error("unable to cast external argument " + e.getIdContribution(), ex);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API Tags from DB tags. All uncastable elements are ignored.
   *
   * @param tags a list of DB tags
   * @return a list of API tags with elements that could have actually been casted to API element (may be empty)
   */
  protected List<be.webdeb.core.api.tag.Tag> buildTagList(List<Tag> tags) {
    List<be.webdeb.core.api.tag.Tag> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Tag t : tags) {
      try {
        result.add(mapper.toTag(t));
      } catch (FormatException e) {
        logger.error("unable to cast tag " + t.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a set of API Tags from DB tags. All uncastable elements are ignored.
   *
   * @param tags a list of DB tags
   * @return a set of API tags with elements that could have actually been casted to API element (may be empty)
   */
  protected Set<be.webdeb.core.api.tag.Tag> buildTagSet(List<Tag> tags) {
    Set<be.webdeb.core.api.tag.Tag> result = new HashSet<>();
    for (be.webdeb.infra.persistence.model.Tag t : tags) {
      try {
        result.add(mapper.toTag(t));
      } catch (FormatException e) {
        logger.error("unable to cast tag " + t.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API Places from DB places.
   *
   * @param places a list of DB places
   * @return a list of API places with elements that could have actually been casted to API element (may be empty)
   */
  protected List<Place> buildPlacesList(List<be.webdeb.infra.persistence.model.Place> places) {
    List<Place> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Place p : places) {
        result.add(mapper.toPlace(p));
    }
    return result;
  }

  /**
   * Merge two actors, filling all details not present in replacement with the ones from origin.
   * <br><br>
   * Any bound contribution to the origin will be bound to the replacement actor. Reconciliations regarding affiliation
   * will be performed on affiliation actors. For all origin affiliations, if no such affiliation to an actor exists
   * in replacement, it will be copied from origin. If the affiliation has no link to an actor, and such function does not
   * exists, it will be copied in replacement actor too.
   *
   * @param origin an actor to be merged into the replacement
   * @param replacement the actor that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(be.webdeb.infra.persistence.model.Actor origin, be.webdeb.infra.persistence.model.Actor replacement,
      Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // for all affiliations of origin, check if we have a perfect match, otherwise, bind it to replacement
      origin.getAffiliations().forEach(aff -> {
        if (aff.getAffiliation() != null) {
          // try to find a matching organization, if none, add it
          List<ActorHasAffiliation> matches = replacement.getAffiliations().stream().filter(a -> a.getAffiliation() != null
              && a.getAffiliation().getIdContribution().equals(aff.getAffiliation().getIdContribution())).collect(Collectors.toList());
          if (matches.isEmpty()) {
            aff.setActor(replacement);
            aff.update();
          } else {
            // we have a match, if functions corresponds, rebind possible
            Optional<ActorHasAffiliation> found = matches.stream().filter(aha ->
                // either both are null
                (aha.getFunction() == null && aff.getFunction() == null)
                // or they are equal
                || (aha.getFunction() != null && aff.getFunction() != null && aha.getFunction().getIdProfession() == aff.getFunction().getIdProfession())).findAny();
            if (found.isPresent()) {
              // rebind contribution_has_actor aha id to this replacement affiliation
              ContributionHasActor.findByAffiliation(aff.getId()).forEach(a -> {
                a.setActorIdAha(found.get().getId());
                a.update();
              });

            } else {
              // we found an affiliation, but they do not perfectly match on function, add it to this replacement actor
              aff.setActor(replacement);
              aff.update();
            }
          }

        } else {
          // no affiliation actor, then there is a function, try to find match
          Optional<ActorHasAffiliation> match = replacement.getAffiliations().stream().filter(a -> a.getFunction() != null
              && a.getFunction().getIdProfession() == aff.getFunction().getIdProfession()).findAny();
          if (!match.isPresent()) {
            aff.setActor(replacement);
            aff.update();
          } else {
            // rebind contribution_has_actor aha ids to this replacement affiliation
            ContributionHasActor.findByAffiliation(aff.getId()).forEach(a -> {
              a.setActorIdAha(match.get().getId());
              a.update();
            });
          }
        }
      });

      // update link to contributions
      origin.getContributions(-1).forEach(c -> {
        c.setActor(replacement);
        c.update();
      });

      // update affiliated actors
      origin.getActors().forEach(a -> {
        a.setAffiliation(replacement);
        a.update();
      });

      ContributionHasActor.findContributionHasActor(replacement.getIdContribution(), EContributionType.CITATION.id()).forEach(c -> {
        if(ContributionHasActor.findContributionHasActor(c.getContribution().getIdContribution(), replacement.getIdContribution(), true, false, false) != null){
          ContributionHasActor.findContributionHasActors(c.getContribution().getIdContribution(), false, true, false).forEach(cha -> {
            ActorHasAffiliation aha = ActorHasAffiliation.findById(cha.getActorIdAha());

            if(aha != null) {
              aha.delete();
            }

            cha.delete();
          });
        }
      });

      // update names, if any
      origin.getNames().forEach(n -> {
        if (replacement.getNames().stream().noneMatch(name -> n.getLang().equals(name.getLang()))) {
          // create new name because replacing the linked contribution to this name doesn't work
          ActorI18name cpy = new ActorI18name(replacement, n.getLang());
          cpy.setPseudo(n.getPseudo());
          cpy.setName(n.getName());
          cpy.setFirstOrAccro(n.getFirstOrAcro());
          cpy.isOld(n.isOld());
          replacement.addName(cpy);
        }
      });

      // if origin has a picture but not replacement, use it
      if (origin.getAvatar() != null && replacement.getAvatar() == null){
        replacement.setAvatar(origin.getAvatar());
      }

      Person originPerson = origin.getPerson();
      Person replacementPerson = replacement.getPerson();

      if(originPerson != null && replacementPerson != null) {
        if(replacementPerson.getBirthdate() == null)
          replacementPerson.setBirthdate(originPerson.getBirthdate());

        if(replacementPerson.getDeathdate() == null)
          replacementPerson.setDeathdate(originPerson.getDeathdate());

        if(replacementPerson.getResidence() == null)
          replacementPerson.setResidence(originPerson.getResidence());

        if(replacementPerson.getGender() == null)
          replacementPerson.setGender(originPerson.getGender());

        replacementPerson.update();
      }

      Organization originOrganization = origin.getOrganization();
      Organization replacementOrganization = replacement.getOrganization();

      if(originOrganization != null && replacementOrganization != null) {
        if(replacementOrganization.getCreationDate() == null)
          replacementOrganization.setCreationDate(originOrganization.getCreationDate());

        if(replacementOrganization.getTerminationDate() == null)
          replacementOrganization.setTerminationDate(originOrganization.getTerminationDate());

        if(replacementOrganization.getLegalStatus() == null)
          replacementOrganization.setLegalStatus(originOrganization.getLegalStatus());

        if(replacementOrganization.getOfficialNumber() == null)
          replacementOrganization.setOfficialNumber(originOrganization.getOfficialNumber());

        if(replacementOrganization.getSectors().isEmpty())
          originOrganization.getSectors().forEach(replacementOrganization::addSector);

        if(replacement.getContribution().getTags().isEmpty())
          origin.getContribution().getTags().forEach(socialObject -> replacement.getContribution().addTag(socialObject));

        if(replacement.getContribution().getPlaces().isEmpty())
          replacement.getContribution().addPlaces(origin.getContribution().getPlaces());

        replacementOrganization.update();
      }

      replacement.update();
      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      origin.getContribution().delete();

      transaction.commit();

      logger.info("merged actor" + INTO + replacement.toString());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Merge two texts, filling all unfilled data from replacement with the one present in origin.
   * <br><br>
   * Any citation will be attached to the replacement text. Any justification link that have origin has context will be attached to the replacement.
   *
   * @param origin a text to be merged into the replacement
   * @param replacement the text that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(Text origin, Text replacement, Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // update optional properties if not set in replacement and origin had some
      if (replacement.getUrl() == null && origin.getUrl() != null) {
        replacement.setUrl(origin.getUrl());
      }
      if (replacement.getPublicationDate() == null && origin.getPublicationDate() != null) {
        replacement.setPublicationDate(origin.getPublicationDate());
      }
      if (replacement.getSourceName() == null && origin.getSourceName() != null) {
        replacement.setSourceName(origin.getSourceName());
      }

      // rebind contents if there are private ones
      if (ETextVisibility.PRIVATE.equals(ETextVisibility.value(origin.getVisibility().getIdVisibility()))) {
        origin.getContents().forEach(c -> {
          // bind new content object
          TextContent content = c.getFilename().contains(".") ?
              new TextContent(replacement, contributor, c.getFilename().substring(c.getFilename().lastIndexOf('.')))
              : new TextContent(replacement, contributor);

          // try to add content, if content is not added, such private content already existed in replacement,
          // do not overwrite it
          if (replacement.addContent(content)) {
            // copy file, not moving since an error may still occur
            logger.debug("will copy private content of " + c.getFilename());
            files.copyContributionFile(c.getFilename(), content.getFilename());
          }
        });
      } else {
        // retrieve shared content, if in replacement empty, copy the one from the origin (that may also be empty)
        Optional<TextContent> originContent = origin.getContents().stream().filter(c -> c.getContributor() == null).findAny();
        Optional<TextContent> replContent = replacement.getContents().stream().filter(c -> c.getContributor() == null).findAny();
        if (originContent.isPresent() && replContent.isPresent() && files.isEmpty(replContent.get().getFilename())) {
          logger.debug("will copy shared content of " + originContent.get().getFilename());
          files.copyContributionFile(originContent.get().getFilename(), replContent.get().getFilename());
        }
      }

      // bind all citations from origin to replacement
      origin.getCitations().forEach(a -> {
        a.setText(replacement);
        a.update();
      });

      // update titles, if any
      origin.getTitles().forEach(n -> {
        if (replacement.getTitles().stream().noneMatch(name -> n.getLang().equals(name.getLang()))) {
          // create new name because replacing the linked contribution to this name doesn't work
          TextI18name cpy = new TextI18name(replacement, n.getLang(), n.getSpelling());
          replacement.addTitle(cpy);
        }
      });

      // updates justification links
      mergeContextContribution(origin.getContribution(), replacement.getContribution());

      // update places and tags
      mergeContributionLinks(replacement.getContribution(), origin.getContribution());

      for(DebateHasText l : DebateHasText.findLinkByText(origin.getIdContribution())) {
        if(DebateHasText.findUnique(l.getDebate().getIdContribution(), replacement.getIdContribution()) == null) {
          l.setText(replacement);
          l.update();
        }
      }

      // for all context has subdebate links where context is this origin
      for (ContextHasSubDebate l : ContextHasSubDebate.findByContext(origin.getIdContribution())) {
        // if no such similarity exists in replacement, add it
        if (ContextHasSubDebate.findByOriginAndDestination(replacement.getIdContribution(), l.getDebate().getIdContribution()) == null) {
          l.setContext(replacement.getContribution());
          l.update();
        }
      }

      replacement.update();
      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      origin.getContribution().delete();
      transaction.commit();
      logger.info("merged text" + INTO + replacement.toString());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Replace origin argument by given replacement
   * <br><br>
   * Any similarity link and translations to the origin will be attached to the replacement.
   *
   * @param origin an argument to be merged into the replacement
   * @param replacement the argument that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(be.webdeb.infra.persistence.model.Argument origin, be.webdeb.infra.persistence.model.Argument replacement,
      Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {

      // for all similarity links where origin is the destination
      for (ArgumentSimilarity l : origin.getSimilarArguments()) {
        // if no such similarity exists in replacement, add it
        if (l.getArgumentTo().getIdContribution().equals(origin.getIdContribution()) && replacement.getSimilarArguments().stream().noneMatch(s ->
                s.getArgumentFrom().getIdContribution().equals(l.getArgumentFrom().getIdContribution()))) {
          l.setArgumentTo(replacement);
          l.update();
        } else if (l.getArgumentFrom().getIdContribution().equals(origin.getIdContribution()) && replacement.getSimilarArguments().stream().noneMatch(s ->
                s.getArgumentTo().getIdContribution().equals(l.getArgumentTo().getIdContribution()))) {
          l.setArgumentFrom(replacement);
          l.update();
        }
      }

      // for all argument justification links where this argument is super argument
      for (ArgumentJustification l : ArgumentJustification.findLinksForSuperArgument(origin.getIdContribution())) {
        // if no such argument justification exists in replacement, add it
        if (ArgumentJustification.findUnique(
                l.getContext().getIdContribution(),
                l.getSubContext() != null ? l.getSubContext().getIdContribution() : null,
                l.getCategory() != null ? l.getCategory().getIdContribution() : null,
                replacement.getIdContribution(),
                l.getArgument().getIdContribution(),
                l.getShade().getIdShade()) == null) {
          l.setSuperArgument(replacement);
          l.update();
        }
      }

      // for all argument justification links where this argument is the argument
      for (ArgumentJustification l : ArgumentJustification.findLinksForSimpleArgument(origin.getIdContribution())) {
        // if no such argument justification exists in replacement, add it
        if (ArgumentJustification.findUnique(
                l.getContext().getIdContribution(),
                l.getSubContext() != null ? l.getSubContext().getIdContribution() : null,
                l.getCategory() != null ? l.getCategory().getIdContribution() : null,
                l.getSuperArgument() != null ? l.getSuperArgument().getIdContribution() : null,
                replacement.getIdContribution(),
                l.getShade().getIdShade()) == null) {
          l.setArgument(replacement);
          l.update();
        }
      }

      // for all citation justification links where this argument is super argument
      for (CitationJustification l : CitationJustification.findLinksForArgument(origin.getIdContribution())) {
        // if no such citation justification exists in replacement, add it
        if (CitationJustification.findUnique(
                l.getContext().getIdContribution(),
                l.getSubContext() != null ? l.getSubContext().getIdContribution() : null,
                l.getCategory() != null ? l.getCategory().getIdContribution() : null,
                replacement.getIdContribution(),
                l.getCitation().getIdContribution(),
                l.getShade().getIdShade()) == null) {
          l.setSuperArgument(replacement);
          l.update();
        }
      }

      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      // now delete origin
      origin.getContribution().delete();
      transaction.commit();
      logger.info("merged argument" + INTO + replacement.getIdContribution());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Replace origin citation by given replacement
   * <br><br>
   * Any illustration link to the origin will be attached to the replacement.
   *
   * @param origin an citation to be merged into the replacement
   * @param replacement the citation that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(Citation origin, Citation replacement,
                     Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      // for all citation justification links where this citation is the citation
      for (CitationJustification l : CitationJustification.findLinksForCitation(origin.getIdContribution())) {
        // if no such citation justification exists in replacement, add it
        if (CitationJustification.findUnique(
                l.getContext().getIdContribution(),
                l.getSubContext() != null ? l.getSubContext().getIdContribution() : null,
                l.getCategory() != null ? l.getCategory().getIdContribution() : null,
                l.getSuperArgument().getIdContribution(),
                replacement.getIdContribution(),
                l.getShade().getIdShade()) == null) {
          l.setCitation(replacement);
          l.update();
        }
      }

      for(CitationPosition l : CitationPosition.findLinksForCitation(origin.getIdContribution())) {
        // if no such citation justification exists in replacement, add it
        if (CitationPosition.findUnique(
                replacement.getIdContribution(),
                l.getDebate().getIdContribution(),
                l.getSubDebate().getIdContribution(),
                l.getShade().getIdShade()) == null) {
          l.setCitation(replacement);
          l.update();
        }
      }

      // update places and tags
      mergeContributionLinks(replacement.getContribution(), origin.getContribution());

      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      // now delete origin
      origin.getContribution().delete();
      transaction.commit();
      logger.info("merged citation" + INTO + replacement.getIdContribution());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Replace origin debate by given replacement
   * <br><br>
   * Any justification link that have origin has context will be attached to the replacement
   *
   * @param origin a debate to be merged into the replacement
   * @param replacement the debate that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(be.webdeb.infra.persistence.model.Debate origin, be.webdeb.infra.persistence.model.Debate replacement,
                     Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      if(values.isBlank(replacement.getDescription())) {
        replacement.setDescription(origin.getDescription());
      }

      if(replacement.getPicture() == null){
        replacement.setPicture(origin.getPicture());
      }

      // for all similarity links where origin is the destination
      for (DebateSimilarity l : origin.getSimilarDebates()) {
        // if no such similarity exists in replacement, add it
        if (l.getDebateTo().getIdContribution().equals(origin.getIdContribution()) && replacement.getSimilarDebates().stream().noneMatch(s ->
                s.getDebateFrom().getIdContribution().equals(l.getDebateFrom().getIdContribution()))) {
          l.setDebateTo(replacement);
          l.update();
        } else if (l.getDebateFrom().getIdContribution().equals(origin.getIdContribution()) && replacement.getSimilarDebates().stream().noneMatch(s ->
                s.getDebateTo().getIdContribution().equals(l.getDebateTo().getIdContribution()))) {
          l.setDebateFrom(replacement);
          l.update();
        }
      }

      // for all context has subdebate links where context is this origin
      for (ContextHasSubDebate l : ContextHasSubDebate.findByContext(origin.getIdContribution())) {
        // if no such similarity exists in replacement, add it
        if (ContextHasSubDebate.findByOriginAndDestination(replacement.getIdContribution(), l.getDebate().getIdContribution()) == null) {
          l.setContext(replacement.getContribution());
          l.update();
        }
      }

      // for all context has subdebate links where debate is this origin
      for (ContextHasSubDebate l : ContextHasSubDebate.findByDebate(origin.getIdContribution())) {
        // if no such similarity exists in replacement, add it
        if (ContextHasSubDebate.findByOriginAndDestination(l.getContext().getIdContribution(), replacement.getIdContribution()) == null) {
          l.setDebate(replacement);
          l.update();
        }
      }

      // updates justification links
      mergeContextContribution(origin.getContribution(), replacement.getContribution());

      // for all citation position links where this debate
      for (CitationPosition l : CitationPosition.findLinksForDebate(origin.getIdContribution())) {
        // if no such citation position exists in replacement, add it
        if (l.existsInOtherDebate(replacement.getIdContribution())) {
          l.setDebate(replacement);
          l.update();
        }
      }

      for(DebateHasText l : DebateHasText.findLinkByDebate(origin.getIdContribution())) {
        if(DebateHasText.findUnique(replacement.getIdContribution(), l.getText().getIdContribution()) == null) {
          l.setDebate(replacement);
          l.update();
        }
      }

      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      // now delete origin
      origin.getContribution().delete();
      transaction.commit();
      logger.info("merged debate" + INTO + replacement.getIdContribution());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Merge two tags, filling all unfilled data from replacement with the one present in origin.
   * <br><br>
   * Any linked arguments and texts will be attached to the replacement tag.
   * Any parents and children in hierarchy will be attached to the replacement tag.
   *
   * @param origin a tag to be merged into the replacement
   * @param replacement the tag that will replace given origin
   * @param contributor the contributor asking for that merge action
   * @throws PersistenceException if any error occurred at the database level
   */
  private void merge(be.webdeb.infra.persistence.model.Tag origin, be.webdeb.infra.persistence.model.Tag replacement,
                     Contributor contributor) throws PersistenceException {
    logger.debug(MERGING + origin.toString() + INTO + replacement.toString());

    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {

      // update parents links
    origin.getParentsAsLinks().forEach(link -> {
        if(replacement.getParentsAsLinks().stream().noneMatch(l ->
                l.getTagParent().getIdContribution().equals(link.getTagParent().getIdContribution())
                || replacement.getIdContribution().equals(l.getTagParent().getIdContribution()))) {
          link.setTagChild(replacement);
          link.update();
        }
      });

      // update children links
      origin.getChildrenAsLinks().forEach(link -> {
        if(replacement.getChildrenAsLinks().stream().noneMatch(l ->
                l.getTagChild().getIdContribution().equals(link.getTagChild().getIdContribution())
                || replacement.getIdContribution().equals(l.getTagChild().getIdContribution()))) {
          link.setTagParent(replacement);
          link.update();
        }
      });

      for(TagLink linkToRemove : replacement.getLinksToRemoveWithGivenTagOrHimself(origin.getIdContribution())) {
        remove(linkToRemove.getIdContribution(), EContributionType.TAG_LINK, contributor.getIdContributor(), ERemoveOption.TOTAL);
      }

      // update link to contributions
      origin.getContributions().forEach(c -> {
        c.removeTag(origin);
        c.addTag(replacement);
        c.update();
      });

      // update names, if any
      origin.getNames().forEach(n -> {
        if (replacement.getNames().stream().noneMatch(name -> n.getLang().equals(name.getLang()))) {
          // create new name because replacing the linked contribution to this name doesn't work
          TagI18name cpy = new TagI18name(replacement, n.getLang(), n.getName());
          cpy.save();
          replacement.addName(cpy);
        }else if (replacement.getRewordingNames().stream().noneMatch(n2 ->
                n2.getLang().equals(n.getLang()) && n2.getName().equals(n.getName()))) {
          TagRewordingI18name cpy = new TagRewordingI18name(replacement, n.getLang(), n.getName());
          cpy.save();
          replacement.addRewordingName(cpy);
        }
      });

      // update rewording names, if any
      origin.getRewordingNames().forEach(n -> {
        if (replacement.getRewordingNames().stream()
                .noneMatch(name -> n.getLang().equals(name.getLang()) && n.getName().equalsIgnoreCase(name.getName()))) {
          TagRewordingI18name cpy = new TagRewordingI18name(replacement, n.getLang(), n.getName());
          cpy.save();
          replacement.addRewordingName(cpy);
        }
      });

      DebateLink.findByTag(origin.getIdContribution()).forEach(link -> {
        if(DebateLink.findUniqueByDebateAndTag(link.getDebate().getIdContribution(), replacement.getIdContribution()) == null) {
          link.setTag(replacement);
          link.update();
        } else {
          link.delete();
        }
      });

      be.webdeb.infra.persistence.model.ContextHasCategory.findByCategory(origin.getIdContribution()).forEach(chc -> {
        if(be.webdeb.infra.persistence.model.ContextHasCategory.findByContextAndCategory(chc.getContext().getIdContribution(), replacement.getIdContribution()) == null){
          chc.setCategory(replacement);
          chc.update();
        } else {
          chc.delete();
        }
      });

      CitationJustification.findLinksForTag(origin.getIdContribution()).forEach(link -> {
        if(!link.existsWithOtherCategory(replacement.getIdContribution())){
          link.setCategory(replacement);
          link.update();
        } else {
          link.delete();
        }
      });

      ArgumentJustification.findLinksForTag(origin.getIdContribution()).forEach(link -> {
        if(!link.existsWithOtherCategory(replacement.getIdContribution())){
          link.setCategory(replacement);
          link.update();
        } else {
          link.delete();
        }
      });

      //replacement.update();
      bindContributor(origin.getContribution(), replacement.getContribution(), contributor);
      remove(origin.getIdContribution(), EContributionType.TAG, contributor.getIdContributor());
      transaction.commit();
      logger.info("merged actor" + INTO + replacement.toString());
    } catch (Exception e) {
      String more = UNABLE_TO_MERGE + origin.getIdContribution() + INTO + replacement.getIdContribution();
      logger.error(more, e);
      throw new PersistenceException(PersistenceException.Key.MERGE, more, e);
    } finally {
      transaction.end();
    }
  }

  /**
   * Merge given context contribution
   *
   * @param origin a contribution to be merged into the replacement
   * @param replacement the contribution that will replace given origin
   */
  private void mergeContextContribution(be.webdeb.infra.persistence.model.Contribution origin, be.webdeb.infra.persistence.model.Contribution replacement) {
    // for all argument justification links where this text is the context
    for (ArgumentJustification l : ArgumentJustification.findLinksForContext(origin.getIdContribution())) {
      // if no such argument justification exists in replacement, add it
      if (l.existsInOtherContext(replacement.getIdContribution())) {
        l.setContext(replacement);
        l.update();
      }
    }

    // for all citation justification links where this text is the context
    for (CitationJustification l : CitationJustification.findLinksForContext(origin.getIdContribution())) {
      // if no such citation justification exists in replacement, add it
      if (l.existsInOtherContext(replacement.getIdContribution())) {
        l.setContext(replacement);
        l.update();
      }
    }
  }

  /**
   * Merge origin actors, places and tags into replacement contribution
   *
   * @param origin a tag to be merged into the replacement
   * @param replacement the tag that will replace given origin
   */
  private void mergeContributionLinks(be.webdeb.infra.persistence.model.Contribution replacement,
                                      be.webdeb.infra.persistence.model.Contribution origin){

    // update linked places
    origin.getActors().forEach(a -> {
      if (replacement.getActors().stream().noneMatch(actor -> a.getActor().getIdContribution().equals(actor.getActor().getIdContribution()))) {
        replacement.getActors().add(a);
      }
    });

    // update linked places
    origin.getPlaces().forEach(p -> {
      if (replacement.getPlaces().stream().noneMatch(place -> p.getId().equals(place.getId()))) {
        replacement.getPlaces().add(p);
      }
    });

    // update linked tags
    origin.getTags().forEach(f -> {
      if (replacement.getTags().stream().noneMatch(tag -> f.getIdContribution().equals(tag.getIdContribution()))) {
        replacement.getTags().add(f);
      }
    });
  }

  /**
   * Check if given contributor is the owner if this contribution, ie
   * <ul>
   *   <li>he's an admin</li>
   *   <li>contribution belongs to only one group and he's an owner of that group</li>
   * </ul>
   * @param contributor a contributor, may not be null
   * @param contribution a contribution, may not be null
   * @return true is the above conditions are met, false otherwise
   */
  private boolean contributorIsOwnerOfContribution(Contributor contributor, be.webdeb.infra.persistence.model.Contribution contribution) {
    // contributor is an admin of public group (with id 0)
    return be.webdeb.infra.persistence.model.Contribution.getCreator(contribution.getIdContribution()).getIdContributor().equals(contributor.getIdContributor())
        || contributor.getContributorHasGroups().stream().anyMatch(g ->
          g.getGroup().getIdGroup() == 0 && g.getRole().getIdRole() == EContributorRole.ADMIN.id())

          // contribution is present in one group and contributor is at least owner of it
          || (contribution.getGroups().size() == 1 && contributor.getContributorHasGroups().stream().anyMatch(g ->
          g.getGroup().getIdGroup() == contribution.getGroups().get(DEFAULT_GROUP).getIdGroup()
              && g.getRole().getIdRole() >= EContributorRole.OWNER.id()));
  }

  private ContributionHistory toContributionHistory(ContributionHasContributor chc) throws FormatException {
      return factory.createHistory(mapper.toContributor(chc.getContributor()), EModificationStatus.value(chc.getStatus().getIdStatus()),
              chc.getSerialization(), new Date(chc.getId().getVersion().getTime()));
  }

  /**
   * Init a contribution from an api contribution
   *
   * @param contributionType the contribution type id
   * @param sortkey an absolute search/sort key
   * @return the db initialized contribution
   */
  protected be.webdeb.infra.persistence.model.Contribution initContribution(int contributionType, String sortkey){
    return initContribution(contributionType, sortkey, false);
  }

  /**
   * Copy a new contribution from a given contribution
   *
   * @param contribution the given contribution to copy
   * @return the db initialized contribution
   */
  protected be.webdeb.infra.persistence.model.Contribution initContribution(be.webdeb.infra.persistence.model.Contribution contribution){
    return initContribution(contribution.getContributionType().getIdContributionType(), contribution.getSortkey(), contribution.isHidden());
  }

  /**
   * Init a contribution from an api contribution with hidden option
   *
   * @param contributionType the contribution type id
   * @param sortkey an absolute search/sort key
   * @param hidden true if this contribution must is hidden
   * @return the db initialized contribution
   */
  protected be.webdeb.infra.persistence.model.Contribution initContribution(int contributionType, String sortkey, boolean hidden){
    be.webdeb.infra.persistence.model.Contribution c = new be.webdeb.infra.persistence.model.Contribution();
    c.setIdContribution(0L);
    c.setContributionType(TContributionType.find.byId(contributionType));
    c.setValidated(TValidationState.findById(EValidationState.UNSET.id()));
    c.setSortkey(sortkey);
    c.isHidden(hidden);
    return c;
  }

  /**
   * Init an external contribution from an api external contribution
   *
   * @param externalApi the external api
   * @param contribution the corresponding db contribution
   * @param externalSource the external source where the external contribution comes from
   * @return true is the above conditions are met, false otherwise
   */
  protected be.webdeb.infra.persistence.model.ExternalContribution initExternalContribution(ExternalContribution externalApi,
                                        be.webdeb.infra.persistence.model.Contribution contribution, TExternalSourceName externalSource){

    be.webdeb.infra.persistence.model.ExternalContribution externalContribution =
            new be.webdeb.infra.persistence.model.ExternalContribution();

    externalContribution.setContribution(contribution);
    externalContribution.setIdContribution(contribution.getIdContribution());
    externalContribution.setSourceUrl(externalApi.getSourceUrl());
    externalContribution.setExternalSource(externalSource);
    externalContribution.setLanguage(TLanguage.find.byId(externalApi.getLanguage().getCode()));
    externalContribution.setTitle(externalApi.getTitle());
    externalContribution.save();

    return externalContribution;
  }

  /**
   * Build a list of API tag categories of a context contribution from DB tags
   *
   * @param categories a list of DB tag categories
   * @param context the context contribution of the categories
   * @return the corresponding list of API tag categories of a context contribution
   */
  protected List<be.webdeb.core.api.tag.TagCategory> buildCategoriesList(List<be.webdeb.infra.persistence.model.Tag> categories, ContextContribution context) {
    List<be.webdeb.core.api.tag.TagCategory> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Tag category : categories) {
      try {
        result.add(mapper.toTagCategory(category, context));
      } catch (FormatException e) {
        logger.error("unable to cast tag category " + category.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API context has categories from DB links
   *
   * @param links a list of DB context has categories links
   * @return the corresponding lit of API context has categories list
   */
  protected List<be.webdeb.core.api.contribution.link.ContextHasCategory> buildContextHasCategoryList(List<be.webdeb.infra.persistence.model.ContextHasCategory> links) {
    return buildContextHasCategoryList(links, null);
  }

  /**
   * Build a list of API context has categories from DB links
   *
   * @param links a list of DB context has categories
   * @param context the context contribution of the link
   * @return the corresponding lit of API context has categories list
   */
  protected List<be.webdeb.core.api.contribution.link.ContextHasCategory> buildContextHasCategoryList(List<be.webdeb.infra.persistence.model.ContextHasCategory> links, ContextContribution context) {
    List<be.webdeb.core.api.contribution.link.ContextHasCategory> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.ContextHasCategory l : links) {
      try {
        result.add(mapper.toContextHasCategory(l, context));
      } catch (FormatException e) {
        logger.error("unable to cast context has categories " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API context has categories from DB tags
   *
   * @param tags a list of DB tags
   * @param context the context contribution of the link
   * @return the corresponding lit of API context has categories list
   */
  protected List<be.webdeb.core.api.contribution.link.ContextHasCategory> buildContextHasCategoryListFromTags(List<Tag> tags, ContextContribution context) {
    List<be.webdeb.core.api.contribution.link.ContextHasCategory> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Tag t : tags) {
      try {
        result.add(mapper.toContextHasCategory(t, context));
      } catch (FormatException e) {
        logger.error("unable to cast context has categories " + t.getIdContribution(), e);
      }
    }
    return result;
  }


  /**
   * Build a list of API argument justification links from DB links
   *
   * @param links a list of DB argument justification links
   * @return the corresponding lit of API argument justification list
   */
  protected List<be.webdeb.core.api.argument.ArgumentJustification> buildArgumentJustificationLinkList(List<be.webdeb.infra.persistence.model.ArgumentJustification> links) {
    return buildArgumentJustificationLinkList(links, null, null, null);
  }

  /**
   * Build a list of API argument justification links from DB links
   *
   * @param links a list of DB argument justification links
   * @param context the context contribution of the link
   * @param category the category where the link is, can be null
   * @param superArgument the superArgument of link is, can be null
   * @return the corresponding lit of API argument justification list
   */
  protected List<be.webdeb.core.api.argument.ArgumentJustification> buildArgumentJustificationLinkList(List<be.webdeb.infra.persistence.model.ArgumentJustification> links, ContextContribution context, TagCategory category, Argument superArgument) {
    List<be.webdeb.core.api.argument.ArgumentJustification> result = new ArrayList<>();

    for (be.webdeb.infra.persistence.model.ArgumentJustification l : links) {
      try {
        result.add(mapper.toArgumentJustification(l, context, category, superArgument));
      } catch (FormatException e) {
        logger.error("unable to cast argument justification link " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API citation justification links from DB links
   *
   * @param links a list of DB citation justification links
   * @return the corresponding lit of API citation justification list
   */
  protected List<be.webdeb.core.api.citation.CitationJustification> buildCitationJustificationLinkList(List<be.webdeb.infra.persistence.model.CitationJustification> links) {
    return buildCitationJustificationLinkList(links, null, null, null);
  }

  /**
   * Build a list of API citation justification links from DB links
   *
   * @param links a list of DB citation justification links
   * @param context the context contribution of the link
   * @param category the category where the link is, can be null
   * @param superArgument the superArgument of link is, can be null
   * @return the corresponding lit of API citation justification list
   */
  protected List<be.webdeb.core.api.citation.CitationJustification> buildCitationJustificationLinkList(List<be.webdeb.infra.persistence.model.CitationJustification> links, ContextContribution context, TagCategory category, Argument superArgument) {
    List<be.webdeb.core.api.citation.CitationJustification> result = new ArrayList<>();

    for (be.webdeb.infra.persistence.model.CitationJustification l : links) {
      try {
        result.add(mapper.toCitationJustification(l, context, category, superArgument));
      } catch (FormatException e) {
        logger.error("unable to cast citation justification link " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API citation position links from DB links
   *
   * @param links a list of DB citation position links
   * @param debate the debate of the link
   * @param subDebate the debate category where the link is, can be null
   * @return the corresponding lit of API citation position list
   */
  protected List<be.webdeb.core.api.citation.CitationPosition> buildCitationPositionLinkList(List<be.webdeb.infra.persistence.model.CitationPosition> links, be.webdeb.core.api.debate.Debate debate, TagCategory subDebate) {
    List<be.webdeb.core.api.citation.CitationPosition> result = new ArrayList<>();

    for (be.webdeb.infra.persistence.model.CitationPosition l : links) {
      try {
        result.add(mapper.toCitationPosition(l, debate, subDebate));
      } catch (FormatException e) {
        logger.error("unable to cast citation position link " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API argument similarity links from DB links
   *
   * @param links a list of DB argument similarity links
   * @return the corresponding lit of API argument list
   */
  protected List<be.webdeb.core.api.argument.ArgumentSimilarity> buildArgumentSimilarityLinkList(List<be.webdeb.infra.persistence.model.ArgumentSimilarity> links) {
    List<be.webdeb.core.api.argument.ArgumentSimilarity> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.ArgumentSimilarity l : links) {
      try {
        result.add(mapper.toArgumentSimilarity(l));
      } catch (FormatException e) {
        logger.error("unable to cast argument similarity link " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API debate similarity links from DB links
   *
   * @param links a list of DB debate similarity links
   * @return the corresponding lit of API debate list
   */
  protected List<be.webdeb.core.api.debate.DebateSimilarity> buildDebateSimilarityLinkList(List<be.webdeb.infra.persistence.model.DebateSimilarity> links) {
    List<be.webdeb.core.api.debate.DebateSimilarity> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.DebateSimilarity l : links) {
      try {
        result.add(mapper.toDebateSimilarity(l));
      } catch (FormatException e) {
        logger.error("unable to cast debate similarity link " + l.getIdContribution(), e);
      }
    }
    return result;
  }

  /**
   * Build a list of API debate external url from DB url
   *
   * @param urls a list of DB debate external url
   * @return the corresponding lit of API debate list
   */
  protected List<be.webdeb.core.api.debate.DebateExternalUrl> buildDebateExternalUrlLinkList(List<be.webdeb.infra.persistence.model.DebateExternalUrl> urls) {
    List<be.webdeb.core.api.debate.DebateExternalUrl> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.DebateExternalUrl url : urls) {
      try {
        result.add(mapper.toDebateExternalUrl(url));
      } catch (FormatException e) {
        logger.error("unable to cast debate external url " + url.getIdUrl(), e);
      }
    }
    return result;
  }

  /**
   * Check if given indexes are correct
   *
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return true if the indexes are correct
   */
  protected boolean checkSubIndexes(int fromIndex, int toIndex) {
    return fromIndex >= 0 && toIndex > fromIndex;
  }

  protected void forceContributionUpdate(be.webdeb.infra.persistence.model.Contribution contribution) {
    if(contribution != null) {
      contribution.setLocked(!contribution.isLocked());
      contribution.update();
      contribution.setLocked(!contribution.isLocked());
      contribution.update();
    }
  }

}
