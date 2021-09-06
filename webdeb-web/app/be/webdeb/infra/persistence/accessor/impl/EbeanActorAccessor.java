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
import be.webdeb.core.api.actor.Organization;
import be.webdeb.core.api.actor.Person;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.ActorAffiliations;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Profession;
import be.webdeb.core.impl.helper.ActorDistance;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This accessor handles retrieval and save actions of Actors.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanActorAccessor extends AbstractContributionAccessor<ActorFactory> implements ActorAccessor {

  private List<ActorType> actorTypes = null;
  private List<AffiliationType> affiliationTypes = null;

  private List<Gender> genders = null;
  private List<Country> countries = null;
  private List<LegalStatus> legalStatuses = null;
  private List<BusinessSector> businessSectors = null;
  private List<ProfessionType> professionTypes = null;
  private List<PrecisionDateType> precisionDateTypes = null;

  @Override
  public List<Actor> getAllActorOrganizations(){
    return buildList(be.webdeb.infra.persistence.model.Actor.getAllActorsByType(1));
  }

  @Override
  public Actor retrieve(Long id, boolean hit) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(id);
    if (actor != null) {
      try {
        Actor api = mapper.toActor(actor);
        if (hit) {
          addHitToContribution(actor.getContribution());
        }
        return api;
      } catch (FormatException e) {
        logger.error("unable to cast retrieved actor " + id, e);
      }
    } else {
      logger.warn("no actor found for id " + id);
    }
    return null;
  }

  @Override
  public List<Actor> retrieveAll(List<Long> ids) {
    return buildList(be.webdeb.infra.persistence.model.Actor.retrieveAll(ids));
  }

  @Override
  public String getGenderActor(Long idActor) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(idActor);

    if (actor != null && actor.getPerson() != null && actor.getPerson().getGender() != null) {
      return actor.getPerson().getGender().getIdGender();
    }
    return EProfessionGender.NEUTRAL.id().toString();
  }

  @Override
  public List<Actor> findByName(String name, EActorType type) {
    return findByName(name, type, 0, 0);
  }

  @Override
  public List<String> findPartyMemberByName(String name, String lang) {
    return be.webdeb.infra.persistence.model.Actor.findPartyMemberByName(name, lang);
  }

  @Override
  public List<Actor> findByName(String name, EActorType type, int fromIndex, int toIndex) {
    List<be.webdeb.infra.persistence.model.Actor> actors =
            be.webdeb.infra.persistence.model.Actor.findByPartialName(name, type.id(), fromIndex, toIndex);
    if (!actors.isEmpty()) {
      return buildList(actors);
    }
    return new ArrayList<>();
  }

  @Override
  public List<Actor> findByFullname(String fullname, EActorType type) {
    List<be.webdeb.infra.persistence.model.Actor> actors =
        be.webdeb.infra.persistence.model.Actor.findByFullName(fullname, type.id());
    if (!actors.isEmpty()) {
      return buildList(actors);
    }
    logger.debug("no actor found for name " + fullname + " of type " + type.name());
    return new ArrayList<>();
  }


  @Override
  public Map<Contribution, ActorRole> getContributions(Long actor, EContributionType type) {
    be.webdeb.infra.persistence.model.Actor a = be.webdeb.infra.persistence.model.Actor.findById(actor);
    if (a != null) {
      List<ContributionHasActor> contributions = a.getContributions(type.id());
      Map<Contribution, ActorRole> result = new LinkedHashMap<>();
      Actor wrapped;
      try {
        wrapped = mapper.toActor(a);
      } catch (FormatException e) {
        logger.error("unable to cast retrieved actor " + actor, e);
        return result;
      }
      for (ContributionHasActor cha : contributions) {
        try {
          Contribution contribution = mapper.toContribution(cha.getContribution());
          ActorRole role = mapper.toActorRole(cha, wrapped, contribution);
          result.put(contribution, role);
        } catch (FormatException e) {
          logger.error("unable to cast element contribution has actor " + cha.getIdCha(), e);
        }
      }
      return result;
    }
    logger.debug("no actor found for id " + actor);
    return new HashMap<>();
  }

  @Override
  public ActorRole findActorRole(Actor actor, Contribution contribution){
    be.webdeb.infra.persistence.model.Actor a = be.webdeb.infra.persistence.model.Actor.findById(actor.getId());
    be.webdeb.infra.persistence.model.Contribution c = be.webdeb.infra.persistence.model.Contribution.findById(contribution.getId());
    ActorRole role = null;

    if(a != null && c != null){
      ContributionHasActor cha = ContributionHasActor.findContributionHasActor(contribution.getId(), actor.getId());
      if(cha != null){
        role = mapper.toActorRole(cha, actor, contribution);
      }
    }

    return role;
  }

  @Override
  synchronized public List<Affiliation> findAffiliatedActors(Long actor, EActorType type) {
    return buildMemberList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAffiliated(actor, type));
  }

  @Override
  synchronized public List<Long> sortAffiliatedActor(Long id, EActorType type) {
    return ActorHasAffiliation.sortAffiliatedActor(id, type);
  }

  @Override
  synchronized public List<Affiliation> findAffiliatedActors(Long actor, EAffiliationType type) {
    return buildMemberList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAffiliated(actor, type));
  }

  @Override
  public List<Affiliation> findAllAffiliations(Long actor) {
    return buildAffiliationList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAllAffiliations(actor));
  }

  @Override
  synchronized public List<Affiliation> findAffiliations(Long actor) {
    return buildAffiliationList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAffiliations(actor));
  }

  @Override
  synchronized public List<Affiliation> findAffiliations(Long actor, EAffiliationType type) {
    return buildAffiliationList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAffiliations(actor, type));
  }

  @Override
  synchronized public List<Affiliation> findAffiliations(Long actor, EActorType type) {
    return buildAffiliationList(be.webdeb.infra.persistence.model.ActorHasAffiliation.findAffiliations(actor, type));
  }

  @Override
  public List<Long> sortAffiliationActor(Long id, EActorType type) {
    return ActorHasAffiliation.sortAffiliationActor(id, type);
  }

  @Override
  public Map<Integer, List<Contribution>> save(Actor contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save actor " + contribution.getFullname(factory.getDefaultLanguage()) + " with id " + contribution.getId() +
        " in group " + currentGroup);

    // for auto-created actors
    Map<Integer, List<Contribution>> actors = new HashMap<>();
    actors.put(EContributionType.ACTOR.id(), new ArrayList<>());
    be.webdeb.infra.persistence.model.Contribution c = checkContribution(contribution, contributor, currentGroup);
    Contributor dbContributor = checkContributor(contributor, currentGroup);
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

    try {
      EModificationStatus status;
      be.webdeb.infra.persistence.model.Actor actor;

      // contribution does not exist yet
      if (c == null) {
        logger.debug("start creation of actor " + contribution.getFullname(factory.getDefaultLanguage()));
        status = EModificationStatus.CREATE;

        // create contribution supertype
        c = initContribution(EContributionType.ACTOR.id(), makeSortKey(contribution.getName(factory.getDefaultLanguage()), contribution.getActorType()));
        updateGroups(contribution, c);

        // create actor and set binding
        actor = new be.webdeb.infra.persistence.model.Actor();
        c.setActor(actor);
        actor.setContribution(c);

        try {
          c.save();
        } catch (Exception e) {
          logger.error("error while saving contribution " + contribution.getFullname(factory.getDefaultLanguage()));
          throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);
        }

        // set id of actor and names
        updateActor(contribution, actor, actors, contributor, c.getIdContribution());
        actor.setIdContribution(c.getIdContribution());
        actor.setNames(toI18names(contribution, actor));

        try {
          actor.save();
          c.update();
        } catch (Exception e) {
          logger.error("error while saving contribution " + contribution.getFullname(factory.getDefaultLanguage()));
          throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);
        }

        // create subtype objects
        try {
          switch (contribution.getActorType()) {
            case PERSON:
              actor.getPerson().setIdContribution(c.getIdContribution());
              actor.getPerson().save();
              break;
            case ORGANIZATION:
              actor.getOrganization().setIdContribution(c.getIdContribution());
              actor.getOrganization().save();
              break;
          }
        } catch (Exception e) {
          logger.error("error while saving actor " + contribution.getFullname(factory.getDefaultLanguage()));
          throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);
        }

        // set new id for given contribution
        contribution.setId(c.getIdContribution());

      } else {
        // update element
        logger.debug("update actor " + contribution.getFullname(factory.getDefaultLanguage()) + " with id " + contribution.getId());
        status = EModificationStatus.UPDATE;
        actor = c.getActor();
        updateActor(contribution, actor, actors, contributor, c.getIdContribution());
        actor.setNames(toI18names(contribution, actor));

        // update groups and sort key
        updateGroups(contribution, actor.getContribution());
        actor.getContribution().setSortkey(makeSortKey(contribution.getName(factory.getDefaultLanguage()), contribution.getActorType()));
        actor.getContribution().update();

        try {
          actor.update();
          c.update();

          // update subtype objects
          switch (contribution.getActorType()) {
            case PERSON:
              actor.getPerson().save();
              break;
            case ORGANIZATION:
              actor.getOrganization().save();
              break;
            default:
              // should not happen...
          }

        } catch (Exception e) {
          logger.error("error while saving or updating actor subtype bean " + contribution.getFullname(factory.getDefaultLanguage()), e);
          throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);
        }
        logger.info("updated " + actor.toString());
      }

      // handle affiliations, first retrieve ids of existing ones (that will be used to remove deleted affiliations)
      Map<Long, ActorHasAffiliation> previous = actor.getAffiliations().stream()
          .collect(Collectors.toMap(ActorHasAffiliation::getId, a -> a));

      try {
        for (Affiliation aff : contribution.getAffiliations()) {
          previous.remove(aff.getId());
          // affiliation id will be created as a side effect, if non existing earlier
          Map<Integer, List<Contribution>> map = aff.save(contribution.getId(), currentGroup, contributor);
          List<Contribution> contributions = (map != null ? map.get(EContributionType.ACTOR.id()) : null);
          if(contributions != null)
            actors.get(EContributionType.ACTOR.id()).addAll(contributions);
        }

        // now delete all affiliations that have not been submitted
        previous.values().forEach(aff -> {
          logger.debug("will delete affiliation with " + aff.toString());
          aff.delete();
        });

      } catch (PermissionException e) {
        logger.error("permission error while saving/updating affiliation", e);
        throw e;

      } catch (FormatException e) {
        logger.error("unable to save affiliation for actor " + actor.toString(), e);
        throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);
      }

      // bind to contributor
      bindContributor(actor.getContribution(), dbContributor, status);
      transaction.commit();

    } finally {
      transaction.end();
    }

    // return auto-created actors
    return actors;
  }

  /**
   * Make a sorting key from given name object
   *
   * @param name a name object for an actor
   * @param type the actor type
   * @return the sorting key, used for rapid indexation
   */
  private String makeSortKey(ActorName name, EActorType type) {
    if (name.getLast() != null) {
      return (name.getFirst() != null ? name.getFirst() + (type == EActorType.ORGANIZATION ? " - " : " ") : "")
              + name.getLast() + (name.getPseudo() != null ? " " + name.getPseudo() : "");
    }
    return name.getPseudo();
  }

  /**
   * Update a DB actor with given API actor data
   *
   * @param apiActor an API actor with data to store
   * @param actor a DB actor recipient (may contain data to be updated)
   * @param contributions a map of Contribution type and a possibly empty list Contributions (Actors or Tags)
   * @param contributor the contributor id
   * created automatically with this save action (new contributions)
   */
  private void updateActor(Actor apiActor, be.webdeb.infra.persistence.model.Actor actor, Map<Integer, List<Contribution>> contributions, Long contributor, Long idActor) throws PermissionException, PersistenceException {

    int actorTypeId = (apiActor.getActorType().id() ==
        EActorType.PROJECT.id() ? EActorType.ORGANIZATION.id() : apiActor.getActorType().id());

    actor.setActortype(TActorType.find.byId(actorTypeId));
    actor.setCrossref(apiActor.getCrossReference());
    actor.setAvatar(updateContributorPicture(apiActor.getAvatarExtension(), idActor, contributor));

    switch (apiActor.getActorType()) {
      case PERSON:
        Person apiPerson = (Person) apiActor;
        be.webdeb.infra.persistence.model.Person person = actor.getPerson();
        if (person == null) {
          person = new be.webdeb.infra.persistence.model.Person();
          actor.setPerson(person);
          person.setActor(actor);
          person.setIdContribution(actor.getIdContribution());
        }

        person.setBirthdate(values.toDBFormat(apiPerson.getBirthdate()));
        person.setDeathdate(values.toDBFormat(apiPerson.getDeathdate()));
        person.setGender(apiPerson.getGender() != null ? TGender.find.byId(apiPerson.getGender().getCode()) : null);
        person.setResidence(apiPerson.getResidence() != null ?
            TCountry.find.byId(apiPerson.getResidence().getCode()) : null);
        break;

      case ORGANIZATION:
        Organization apiOrg = (Organization) apiActor;
        actor.setCrossref(apiActor.getCrossReference());
        be.webdeb.infra.persistence.model.Organization org = actor.getOrganization();
        if (org == null) {
          org = new be.webdeb.infra.persistence.model.Organization();
          actor.setOrganization(org);
          org.setActor(actor);
          org.setIdContribution(actor.getIdContribution());
        }

        if (apiOrg.getLegalStatus() != null) {
          org.setLegalStatus(TLegalStatus.find.byId(apiOrg.getLegalStatus().getType()));
        } else {
          // default legal status value
          org.setLegalStatus(null);
        }

        org.setOfficialNumber(apiOrg.getOfficialNumber());
        org.setCreationDate(values.toDBFormat(apiOrg.getCreationDate()));
        org.setTerminationDate(values.toDBFormat(apiOrg.getTerminationDate()));
        // manage head office places
        savePlaces(apiOrg.getPlaces(), actor.getContribution());
        //org.setHeadOffice(apiOrg.getHeadOffice() != null ? TCountry.find.byId(apiOrg.getHeadOffice().getCode()) : null);
        // manage bindings to tags as social objects
        contributions.put(EContributionType.TAG.id(),
                bindTags(actor.getContribution().getIdContribution(), apiOrg.getTagsAsList(), contributor));

        // handle bindings to sectors
        org.setSectors(apiOrg.getBusinessSectors().stream()
            .map(s -> TBusinessSector.find.byId(s.getType())).collect(Collectors.toList()));
        break;

      case UNKNOWN:
        actor.setCrossref(apiActor.getCrossReference());
        break;

      default:
        logger.debug("given actor does not have a valid type " + apiActor.getActorType().name());
    }
  }

  @Override
  public List<ActorType> getActorTypes() {
    if (actorTypes == null) {
      actorTypes = TActorType.find.all().stream().map(t ->
        factory.createActorType(t.getIdActorType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return actorTypes;
  }

  @Override
  public List<PrecisionDateType> getPrecisionDateTypes() {
    if (precisionDateTypes == null) {
      precisionDateTypes = TPrecisionDateType.find.all().stream().map(t ->
              factory.createPrecisionDateType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return precisionDateTypes;
  }

  @Override
  public List<ProfessionType> getProfessionTypes() {
    if (professionTypes == null) {
      professionTypes = TProfessionType.find.all().stream().map(t ->
              factory.createProfessionType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return professionTypes;
  }

  @Override
  public List<Gender> getGenders() {
    if (genders == null) {
      genders = TGender.find.all().stream().map(t ->
        factory.createGender(t.getIdGender(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return genders;
  }

  @Override
  public List<AffiliationType> getAffiliationTypes() {
    if (affiliationTypes == null) {
      affiliationTypes = TAffiliationType.find.all().stream().map(t ->
        factory.createAffiliationType(t.getIdType(), t.getActorType(), t.getSubtype(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return affiliationTypes;
  }

  @Override
  public List<Country> getCountries() {
    if (countries == null) {
      countries = TCountry.find.all().stream().map(t ->
        factory.createCountry(t.getIdCountry(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return countries;
  }

  @Override
  public List<LegalStatus> getLegalStatuses() {
    if (legalStatuses == null) {
      legalStatuses = TLegalStatus.find.all().stream().map(t ->
        factory.createLegalStatus(t.getIdStatus(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return legalStatuses;
  }

  @Override
  public List<BusinessSector> getBusinessSectors() {
    if (businessSectors == null) {
      businessSectors = TBusinessSector.find.all().stream().map(t ->
        factory.createBusinessSector(t.getIdBusinessSector(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return businessSectors;
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> getProfessions() {
    return toProfessions(Profession.findAllProfessions());
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> findProfessions(String term, String lang, String gender, int type) {
    return toProfessions(Profession.findProfessions(term, lang, gender, type));
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> findProfessions(String term, String lang, int type) {
    return toProfessions(Profession.findProfessions(term, lang, type));
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> findProfessions(String term, Long idToIgnore, String lang, int fromIndex, int toIndex) {
    return toProfessions(Profession.findProfessions(term, idToIgnore, lang, fromIndex, toIndex));
  }

  @Override
  public be.webdeb.core.api.actor.Profession findProfession(int id) {
    Profession p = Profession.findById(id);
    if (p != null) {
      return toProfession(p);
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.actor.Profession findProfession(String name, String lang) {
    Profession p = Profession.findByName(name, lang);
    return p != null ? toProfession(p) : null;
  }

  @Override
  public be.webdeb.core.api.actor.Profession findProfession(String name, String lang, int type) {
    Profession p = Profession.findByName(name, lang, type);
    return p != null ? toProfession(p) : null;
  }

  @Override
  public be.webdeb.core.api.actor.Profession findProfession(String name, boolean strict) {
    Profession p = Profession.findByName(name, strict);
    return p != null ? toProfession(p) : null;
  }

  @Override
  public be.webdeb.core.api.actor.Profession findSubstituteForProfession(int id) {
    Profession p = ProfessionHasLink.findEquivalent(id);
    if (p != null) {
      return toProfession(p);
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> findEquivalentsForProfession(int id) {
    Profession p = Profession.findById(id);
    if(p != null) {
      return toProfessions(ProfessionHasLink.findAllEquivalents(id));
    }
    return new ArrayList<>();
  }

  @Override
  public be.webdeb.core.api.actor.Profession getSuperLink(int id) {
    Profession p = ProfessionHasLink.getSuperLink(id);
    if (p != null) {
      return toProfession(p);
    }
    return null;
  }

  @Override
  public String getProfessionSimpleName(Integer professionId, String lang) {
    Profession p = Profession.findById(professionId);
    if (p != null && p.getSpelling(lang) != null) {
      return p.getSpelling(lang).getSpelling();
    }
    return null;
  }

  @Override
  public be.webdeb.core.api.actor.Profession determineSuperLink(Integer professionId){
    Profession p = Profession.findById(professionId);
    if(p != null){
      ProfessionI18name i18name = p.getSpelling();
      if(i18name != null){
        String name = i18name.getSpelling();
        if(name != null && !"".equals(name) && name.contains(" ")) {
          String superLinkNameToSearch = name.split(" ")[0];
          Profession superLink = Profession.findByName(superLinkNameToSearch, i18name.getGender(), i18name.getLang(), true);
          if(superLink != null) return toProfession(superLink);
        }
      }
    }
    return null;
  }

  @Override
  public List<be.webdeb.core.api.actor.Profession> determineSubLinks(Integer professionId){
    List<be.webdeb.core.api.actor.Profession> subLinks = new ArrayList<>();
    Profession p = Profession.findById(professionId);
    if(p != null){
      ProfessionI18name i18name = p.getSpelling();
      if(i18name != null) {
        String name = i18name.getSpelling();
        if (name != null && !"".equals(name)) {
          List<Profession> subProfessions = Profession.findProfessions(name + " ", i18name.getLang(), i18name.getGender());
          if(!subProfessions.isEmpty())subLinks.addAll(toProfessions(subProfessions));
        }
      }
    }
    return subLinks;
  }

  @Override
  public Map<String, Boolean> getFunctionHierarchy(Integer professionId, String lang) {
    Map<String, Boolean> hierarchy = new LinkedHashMap<>();
    Set<String> hierarchySet = new HashSet<>();
    buildFunctionHierarchy(Profession.findById(professionId), hierarchy, hierarchySet, lang);
    return hierarchy;
  }

    /**
     * Create the function hierarchy recursively
     *
     * @param profession a profession to add to the hierarchy
     * @param hierarchy a map of profession names and boolean to displayHierarchy
     * @param hierarchySet a set of looked profession name to ensure they are unique
     * @param lang the user lang
     */
  private void buildFunctionHierarchy(Profession profession, Map<String, Boolean> hierarchy, Set<String> hierarchySet, String lang){
    if(profession != null) {
      String name = profession.getSpelling(lang).getSpelling();
      if (!hierarchySet.contains(name)) {
        hierarchySet.add(name);
        buildFunctionHierarchy(profession.getSuperLink(), hierarchy, hierarchySet, lang);
        hierarchy.put(name, profession.isDisplayHierarchy());
      }
    }
  }

  @Override
  public int mergeProfessions(int profession, int professionToMerge) throws PersistenceException{
    logger.debug("try to merge profession " + profession + " with " + professionToMerge);
    int professionId = -1;
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      Profession p = Profession.findById(profession);
      Profession pToMerge = Profession.findById(professionToMerge);
      if(p != null && pToMerge != null) {

        // add all names of profession to merge to profession if it hasn't have it
        List<ProfessionI18name> names = new ArrayList<>();
        for (ProfessionI18name name : pToMerge.getSpellings()) {
          if (!p.hasSpelling(name.getLang(), name.getGender())) {
            names.add(new ProfessionI18name(p.getIdProfession(), name.getLang(), name.getGender(),name.getSpelling()));
          }
        }
        p.getSpellings().addAll(names);
        p.update();

        // change foreign key for all matched professionToMerge with profession
        List<ActorHasAffiliation> affiliationsA = ActorHasAffiliation.findAffiliationsByFunction(pToMerge);
        for (ActorHasAffiliation aff : affiliationsA) {
          aff.setFunction(p.getIdProfession());
          aff.update();
        }

        List<ContributorHasAffiliation> affiliationsC = ContributorHasAffiliation.findAffiliationsByFunction(pToMerge);
        for (ContributorHasAffiliation aff : affiliationsC) {
          aff.setFunction(p.getIdProfession());
          aff.update();
        }

        List<ProfessionHasLink> links = ProfessionHasLink.findLinks(pToMerge.getIdProfession(), true);
        for (ProfessionHasLink link : links) {
          if(ProfessionHasLink.findLink(pToMerge.getIdProfession(), link.getSubstitute().getIdProfession()) != null) {
            link.delete();
          } else {
            link.setProfession(p);
            link.update();
          }
        }

        links = ProfessionHasLink.findLinks(pToMerge.getIdProfession(), false);
        for (ProfessionHasLink link : links) {
          if(ProfessionHasLink.findLink(link.getProfession().getIdProfession(), pToMerge.getIdProfession()) != null) {
            link.delete();
          } else {
            link.setSubstitute(p);
            link.update();
          }
        }

        pToMerge.delete();
        professionId = p.getIdProfession();

        transaction.commit();
        logger.info("professions merged in " + professionId);
      }else{
        logger.error("Profession(s) not found");
      }
    } catch (Exception e) {
      logger.error("error while merging professions", e);
      throw new PersistenceException(PersistenceException.Key.SAVE_ACTOR, e);

    } finally {
      transaction.end();
    }
    return professionId;
  }

  @Override
  public void resetActorType(Long id) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(id);
    if(actor != null){
      if(actor.getActortype().getIdActorType() == EActorType.PERSON.id()){
        actor.setPerson(null);
      }else if(actor.getActortype().getIdActorType() == EActorType.ORGANIZATION.id()){
        actor.setOrganization(null);
      }
      actor.setActortype(TActorType.find.byId(EActorType.UNKNOWN.id()));
      actor.update();
    }
  }

  @Override
  public List<Text> getTextsWhereCitationAuthor(Long actorId, Long contributor, int group) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(actorId);
    if (actor != null) {
      return buildTextList(actor.getTextsWhereCitationAuthor(contributor, group), actorId, contributor, group);
    }
    return new ArrayList<>();
  }

  @Override
  public List<Text> getTextsWhereCitationCited(Long actorId, Long contributor, int group) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(actorId);
    if (actor != null) {
      return buildTextList(actor.getTextsWhereCitationCited(contributor, group));
    }
    return new ArrayList<>();
  }

  @Override
  public List<Affiliation> getAffiliationsRelatedTo(Long id, List<Long> actorIds) {
    be.webdeb.infra.persistence.model.Actor actor = be.webdeb.infra.persistence.model.Actor.findById(id);

    if(actor != null){
      return buildAffiliationList(actor.getAffiliationsRelatedTo(actorIds));
    }

    return new ArrayList<>();
  }

  @Override
  public Map<ESocioGroupKey, List<ActorAlliesOpponents>> getAlliesOpponentsMap( SearchContainer query) {
   // Date date = new Date();
    Map<ESocioGroupKey, List<ActorAlliesOpponents>> results = new LinkedHashMap<>();

    results.put(ESocioGroupKey.AUTHOR, be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsByActor(query));
    results.put(ESocioGroupKey.AGE, be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsByAge(query));
    results.put(ESocioGroupKey.COUNTRY, be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsByCountry(query));
    results.put(ESocioGroupKey.FUNCTION, be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsByProfession(query));
    results.put(ESocioGroupKey.ORGANIZATION, be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsByOrganization(query));
    //logger.debug((new Date().getTime() - date.getTime()) + "//");
    return results;
  }

  @Override
  public Map<Integer, List<Citation>> getAlliesOpponentsCitationsMap(SearchContainer query) {
    switch (query.getSocioGroupKey()){
      case AUTHOR:
        return buildShadedCitationMap(be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsCitationsByActor(query));
      case AGE:
        return buildShadedCitationMap(be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsCitationsByAge(query));
      case COUNTRY:
        return buildShadedCitationMap(be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsCitationsByCountry(query));
      case FUNCTION:
        return buildShadedCitationMap(be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsCitationsByProfession(query));
      case ORGANIZATION:
        return buildShadedCitationMap(be.webdeb.infra.persistence.model.Actor.getAlliedOpponentsCitationsByOrganization(query));
    }

    return new HashMap<>();
  }

  @Override
  public Map<ESocioGroupKey, List<ActorDistance>> getActorsDistance(SearchContainer query) {
    Map<ESocioGroupKey, List<ActorDistance>> results = new LinkedHashMap<>();

    results.put(ESocioGroupKey.AUTHOR, be.webdeb.infra.persistence.model.Actor.getActorsDistancesByActor(query));
    results.put(ESocioGroupKey.AGE, be.webdeb.infra.persistence.model.Actor.getActorsDistancesByAge(query));
    results.put(ESocioGroupKey.COUNTRY, be.webdeb.infra.persistence.model.Actor.getActorsDistancesByCountry(query));
    results.put(ESocioGroupKey.FUNCTION, be.webdeb.infra.persistence.model.Actor.getActorsDistancesByProfession(query));
    results.put(ESocioGroupKey.ORGANIZATION, be.webdeb.infra.persistence.model.Actor.getActorsDistancesByOrganization(query));

    return results;
  }

  @Override
  public List<ActorDistance> getActorsDistanceByDebates(SearchContainer query) {
    switch (query.getSocioGroupKey()) {
      case AUTHOR:
        return be.webdeb.infra.persistence.model.Actor.getActorsDistancesByActor(query);
      case AGE:
        return be.webdeb.infra.persistence.model.Actor.getActorsDistancesByAge(query);
      case COUNTRY:
        return be.webdeb.infra.persistence.model.Actor.getActorsDistancesByCountry(query);
      case FUNCTION:
        return be.webdeb.infra.persistence.model.Actor.getActorsDistancesByProfession(query);
      case ORGANIZATION:
        return be.webdeb.infra.persistence.model.Actor.getActorsDistancesByOrganization(query);
    }

    return new ArrayList<>();
  }

  @Override
  public String getMinAndMaxAffiliationDate(Long id, EActorType actorType, EActorType viewedType, boolean forMin) {
    return be.webdeb.infra.persistence.model.Actor.getMinOrMaxAffiliationDate(id, actorType, viewedType, forMin);
  }

  @Override
  public List<ActorAffiliations> getOwners(SearchContainer query) {
    return be.webdeb.infra.persistence.model.Actor.getOwners(query);
  }

  @Override
  public List<ActorAffiliations> getOwnedOrganizations(SearchContainer query) {
    return be.webdeb.infra.persistence.model.Actor.getOwnedOrganizations(query);
  }

  @Override
  public List<ActorAffiliations> getAffiliationOrganizations(SearchContainer query) {
    return be.webdeb.infra.persistence.model.Actor.getAffiliationOrganizations(query);
  }

  @Override
  public List<Citation> getTextsAuthorCitations(Long actor, Long text) {
    return buildCitationList(be.webdeb.infra.persistence.model.Citation.findCitationsFromActorAndText(actor, text));
  }

  @Override
  public List<ActorAffiliations> getMembers(SearchContainer query) {
    return be.webdeb.infra.persistence.model.Actor.getMembers(query);
  }

  @Override
  public List<Actor> getOthersActorsCitations(SearchContainer query) {
    return buildList(be.webdeb.infra.persistence.model.Actor.getOthersActorsCitations(query));
  }

  @Override
  public List<Tag> getTagsCitations(SearchContainer query) {
    return buildTagList(be.webdeb.infra.persistence.model.Actor.getTagsCitations(query));
  }

  @Override
  public List<Text> getTextsCitations(SearchContainer query) {
    return buildTextList(be.webdeb.infra.persistence.model.Actor.getTextsCitations(query));
  }

  @Override
  public List<Debate> getDebatesCitations(SearchContainer query) {
    return buildDebateList(be.webdeb.infra.persistence.model.Actor.getDebatesCitations(query));
  }

  @Override
  public List<Citation> getCitations(SearchContainer query) {
    return buildCitationList(be.webdeb.infra.persistence.model.Actor.getCitations(query));
  }

  @Override
  public List<Citation> getCitationsFromContribution(SearchContainer query) {
    switch (query.getContributionType()){
      case ACTOR:
        return buildCitationList(be.webdeb.infra.persistence.model.Actor.getOthersActorsCitationsList(query));
      case DEBATE:
        return buildCitationList(be.webdeb.infra.persistence.model.Actor.getDebatesCitationsList(query));
      case TAG:
        return buildCitationList(be.webdeb.infra.persistence.model.Actor.getTagsCitationsList(query));
      case TEXT:
        return buildCitationList(be.webdeb.infra.persistence.model.Actor.getTextsCitationsList(query));
    }

    return new ArrayList<>();
  }

  @Override
  public Actor random() {
    return null;
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Wrap a list of DB professions into a list of API professions
   * @param professions a list of DB professions
   * @return the corresponding list of API professions
   */
  private List<be.webdeb.core.api.actor.Profession> toProfessions(List<Profession> professions) {
    return professions.stream().map(this::toProfession).collect(Collectors.toList());
  }

  /**
   * Wrap a DB profession into an API profession
   *
   * @param profession a DB profession
   * @return an API profession
   */
  private be.webdeb.core.api.actor.Profession toProfession(Profession profession) {
    return factory.createProfession(profession.getIdProfession(),
            profession.getProfessionType().getEProfessionType(),
            profession.isDisplayHierarchy(),
            mapLangsAndNames(profession.getSpellings()));
  }

  private Map<String, Map<String, String>> mapLangsAndNames(List<ProfessionI18name> names){
    Map<String, Map<String, String>> i18langs = new HashMap<>();
    for(ProfessionI18name name : names){
      if(!i18langs.containsKey(name.getLang())){
        i18langs.put(name.getLang(), new HashMap<>());
      }
      i18langs.get(name.getLang()).put(name.getGender(), name.getSpelling());
    }
    return i18langs;
  }

  /**
   * Helper method to build a list of API actor from DB actors. All uncastable elements are ignored.
   *
   * @param actors a list of DB actors
   * @return a list of API actors with elements that could have actually been casted to API element (may be
   * empty)
   */
  private List<Actor> buildList(List<be.webdeb.infra.persistence.model.Actor> actors) {
    List<Actor> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Actor a : actors) {
      try {
        result.add(mapper.toActor(a));
      } catch (FormatException e) {
        logger.error("unable to cast actor " + a.getIdContribution() + " Reason: " + e.getMessage(), e);
      }
    }
    return result;
  }
}
