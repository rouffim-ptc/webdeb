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

package be.webdeb.presentation.web.controllers.entry.actor;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import play.data.validation.ValidationError;

import java.util.*;

/**
 * Simple form to only manage affiliations of an actor
 *
 * @author Fabian Gilson
 */
public class ActorAffiliationForm extends ContributionHolder {

  protected String fullname;
  // used to check validity of affiliations
  protected String birthdate;
  protected String deathdate;
  protected int actorType;
  protected boolean reversed;
  protected Boolean affiliationPerson;
  protected boolean affiliatedPerson;
  protected List<AffiliationForm> affiliationsForm = new ArrayList<>();

  /**
   * Play! / JSON compliant default constructor
   */
  public ActorAffiliationForm() {
    type = EContributionType.ACTOR;
  }

  /**
   * Constructor. Create an ActorAffiliationForm for a given actor
   *
   * @param actor an API actor
   * @param lang 2-char ISO 639-1 code of the current UI language
   * @param person if affiliations concern a person
   * @param affiliation true if this form must be reversed
   */
  public ActorAffiliationForm(Actor actor, String lang, Boolean person, boolean affiliation) {
    this();
    id = actor.getId();
    reversed = !affiliation && actor.getActorType() == EActorType.ORGANIZATION;
    this.affiliatedPerson = EActorType.PERSON.equals(actor.getActorType());
    this.affiliationPerson = person;

    switch (actor.getActorType()) {
      case PERSON:
        Person p = (Person) actor;
        birthdate = p.getBirthdate();
        deathdate = p.getDeathdate();
        break;
      case ORGANIZATION:
        Organization o = (Organization) actor;
        birthdate = o.getCreationDate();
        deathdate = o.getTerminationDate();
        break;
      default:
        // ignore unknown actor type
    }
    this.lang = lang;
    version = actor.getVersion();
    fullname = actor.getFullname(lang);
    actorType = actor.getActorType().id();
  }

  /**
   * Play validation method (implicitly called)
   *
   * @return null if validation ok, map of errors for each fields in error otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    affiliationsForm.removeIf(AffiliationForm::isEmpty);
    return checkAffiliations(fullname, birthdate, deathdate, (affiliationPerson != null && affiliationPerson) || actorType == EActorType.PERSON.id(), affiliationsForm, "affiliationsForm");
  }

  /**
   * Save all affiliations present in this form for bound actor.
   * Some actors may not exist, so will be created and returned.
   *
   * @param contributor the contributor id that issued that action
   * @return a list of auto-created actors (as Contribution)
   *
   * @throws FormatException if any field contains error
   * @throws PermissionException if current contributor may not perform such action
   * @throws PersistenceException if any error occurred with the database
   */
  public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
    logger.debug("try to save " + toString() + " with version " + version + " in group " + inGroup);
    Map<Integer, List<Contribution>> result = new HashMap<>();
    result.put(EContributionType.ACTOR.id(), new ArrayList<>());

    if(reversed) {
      Long currentId = id;
      for (AffiliationForm aff : affiliationsForm) {
        List<Contribution> c = toActorAffiliationForm(aff, affiliationPerson).save(contributor).get(EContributionType.ACTOR.id());
        if(c != null)
          result.get(EContributionType.ACTOR.id()).addAll(c);
        id = currentId;
      }
    }else {

      Actor actor;
      boolean toAdd = false;
      if (!values.isBlank(id)) {
        actor = actorFactory.retrieve(id);
        if (actor == null) {
          logger.error("could not retrieve actor with id " + id);
          throw new PersistenceException(PersistenceException.Key.SAVE_AFFILIATION);
        }
      } else {
        logger.debug("will also create new actor with name " + fullname + " in lang " + lang);

        actor = (affiliationPerson == null ? actorFactory.getActor() : affiliationPerson ? actorFactory.getPerson() : actorFactory.getOrganization());

        ActorName name = helper.toActorName(actor, fullname, lang);
        actor.setId(id != null ? id : -1L);
        actor.setVersion(version);
        actor.addInGroup(inGroup);
        actor.setNames(Collections.singletonList(name));

        id = actor.getId();
        result.get(EContributionType.ACTOR.id()).add(actor);
        toAdd = true;
      }

      for (AffiliationForm aff : affiliationsForm) {
        // if affiliation has no language (because it's a new one), set language as UI one
        try {
          if (values.isBlank(aff.getLang())) {
            aff.setLang(lang);
          }

          actor.addAffiliation(aff.toAffiliation());
        } catch (FormatException e) {
          throw new PersistenceException(PersistenceException.Key.SAVE_AFFILIATION, e);
        }
      }
      // force add in current group for this actor (this request may come from the visualisation of a
      // "private" actor in public scope by clicking around affiliations
      actor.addInGroup(inGroup);
      result.putAll(actor.save(contributor, inGroup));
      if(toAdd)
        result.get(EContributionType.ACTOR.id()).add(actor);
    }
    return result;
  }

  @Override
  public MediaSharedData getMediaSharedData() {
    return null;
  }

  @Override
  public String getContributionDescription() {
    return null;
  }

  @Override
  public String getDefaultAvatar(){
    return "";
  }

  /**
   * Get the actor name
   *
   * @return the actor's name
   */
  public String getFullname() {
    return fullname;
  }

  /**
   * Set the actor's name
   *
   * @param fullname a name
   */
  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  /**
   * Get this actor birth/creation date
   *
   * @return a (DD/MM/)YYYY date, may be null
   */
  public String getBirthdate() {
    return birthdate;
  }

  /**
   * Set this actor birth/creation date
   *
   * @param birthdate a (DD/MM/)YYYY date
   */
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  /**
   * Get this actor death/termination date
   *
   * @return a (DD/MM/)YYYY date, may be null
   */
  public String getDeathdate() {
    return deathdate;
  }

  /**
   * Set this actor death/termination date
   *
   * @param deathdate a (DD/MM/)YYYY date
   */
  public void setDeathdate(String deathdate) {
    this.deathdate = deathdate;
  }

  public boolean isReversed() {
    return reversed;
  }

  public void setReversed(boolean reversed) {
    this.reversed = reversed;
  }

  public Boolean getAffiliationPerson() {
    return affiliationPerson;
  }

  public void setAffiliationPerson(Boolean person) {
    this.affiliationPerson = person;
  }

  public boolean getAffiliatedPerson() {
    return affiliatedPerson;
  }

  public void setAffiliatedPerson(boolean person) {
    this.affiliatedPerson = person;
  }

  /**
   * Get the list of affiliations for this actor (identified here by its name)
   *
   * @return the (possibly empty) list of affiliations
   */
  public List<AffiliationForm> getAffiliationsForm() {
    return affiliationsForm;
  }

  /**
   * Set the list of affiliations for this actor
   *
   * @param affiliationsForm a list of affiliations
   */
  public void setAffiliationsForm(List<AffiliationForm> affiliationsForm) {
    this.affiliationsForm = affiliationsForm;
  }

  /**
   * Add one affiliation to given
   *
   * @param affiliation an affiliation to add to the list of affiliations
   */
  public void addAffiliation(AffiliationForm affiliation) {
    affiliationsForm.add(affiliation);
  }

  /**
   * Get the actortype id (see EActorType)
   *
   * @return the actor type id (-1 for unknown, 0 for person, 1 for organization)
   */
  public int getActorType() {
    return actorType;
  }

    /**
   * Set the actortype id (see EActorType
   *
   * @param actorType the actor type id (-1 for unknown, 0 for person, 1 for organization)
   */
  public void setActorType(int actorType) {
    this.actorType = actorType;
  }

  @Override
  public String toString() {
    return fullname + "[" + id +  "] has affiliation " + affiliationsForm.toString();
  }

  /**
   * Reverse this form to make it compliant with the affiliation way (as the affname in this form is the affiliated actor,
   * not the affiliation), ie the returned affiliation.affid being this.id and the affname being this fullname
   *
   * @param affiliation an affiliation form filled in the "affiliated point of view"
   * @param person true if the holder of the affiliated is a person, false otherwise
   * @return a new affiliation form where given affiliation has been reversed
   */
  private AffiliationForm reverseForm(AffiliationForm affiliation, boolean person) {
    AffiliationForm reversed = new AffiliationForm();
    // reverse ids
    reversed.setAffid(id);
    id = affiliation.getAffid();
    // and name
    reversed.setAffname(fullname);
    reversed.setLang(affiliation.getLang());
    if (person) {
      reversed.setFunction(affiliation.getFunction());
      reversed.setFunctionid(affiliation.getFunctionid());
    } else {
      reversed.setAfftype(affiliation.getAfftype());
    }
    reversed.setStartDate(affiliation.getStartDate());
    reversed.setEndDate(affiliation.getEndDate());
    reversed.setStartDateType(affiliation.getStartDateType());
    reversed.setEndDateType(affiliation.getEndDateType());
    return reversed;
  }

  /**
   * Convert an affiliation to an ActorAffiliationForm by its own and reversing given affiliation
   *
   * @param affiliation the affiliation to convert
   * @param person true if given affiliation should be a person
   * @return the converted actor affiliation form to be saved
   */
  private ActorAffiliationForm toActorAffiliationForm(AffiliationForm affiliation, boolean person) {
    ActorAffiliationForm result = new ActorAffiliationForm();
    result.setId(affiliation.getAffid());
    result.setFullname(affiliation.getAffname());
    result.setAffiliationsForm(Collections.singletonList(reverseForm(affiliation, person)));
    result.setLang(values.isBlank(affiliation.getLang()) ? lang : affiliation.getLang());
    result.setAffiliatedPerson(affiliatedPerson);
    result.setAffiliationPerson(affiliationPerson);
    return result;
  }

}