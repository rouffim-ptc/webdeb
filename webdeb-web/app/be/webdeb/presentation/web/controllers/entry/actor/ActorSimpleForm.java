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
import be.webdeb.core.exception.FormatException;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Simplified Actor representations (used in forms where the Actor is condensed, like in text and argument
 * edition forms.
 *
 * Only one of the actor's affiliation is referred in this actor name, since this view for an actor
 * is used only as authors/reporters/involved actors in contributions where actors have one
 * identified function/affiliation.
 *
 * Using isDisambiguated and isAffDisambiguated flags to know if the (affiliation) actor must be disambiguated,
 * ie, their ids are empty and we found possible matches in database. If so, the views.util.handleNameMatches frame
 * must be triggered (see usages arguments and text edition forms) to ask to user to explicitly choose between binding to
 * an existing actor or create a new one. These name matches checks are performed explicitly as part of the form validation
 * processes, but outside of Play's implicit validation method.
 *
 * {@link be.webdeb.presentation.web.controllers.entry.ContributionHelper#searchForNameMatches(ActorSimpleForm, be.webdeb.presentation.web.controllers.permission.WebdebUser, String)
 * Name matches} are currently performed on full matches, but the method in charge may be easily amended to perform
 * more advanced matching queries. The UI process would remain the same, but actual accessor method would require
 * to be changed (not using factory's findByFullname anymore).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ActorSimpleForm {

  // Actor fields
  protected Long id = -1L;
  protected int actortype = -1;
  // contain the concatenation of first and lastname
  // used for affiliation-like fields
  protected String fullname = null;
  // store only last name for individual person and name for organisation
  // used for autocomplete on add new Actor form
  protected String lastname = null;
  protected String firstname = null;
  protected String pseudo = null;

  protected String avatar;

  // for affiliation
  protected String function = "";
  protected String functionGender = "N";
  protected String substitute = "";
  // using id in lower case because of generic typeahead management (see webdeb-scripts)
  protected Integer functionid = -1;

  protected String affname = "";

  // using id in lower case because of generic typeahead management
  protected Long affid = -1L;
  // actor_as_affiliation id, used for text and arguments to identify a particular
  protected Long aha = -1L;

  // for filterable
  protected String residence;
  protected String birthOrCreation = "";
  protected String deathOrTermination = "";
  protected String gender = "";
  protected String genderId;
  protected String legal = "";
  protected List<String> sectors = new ArrayList<>();

  // for contributions
  protected boolean author = false;
  protected boolean reporter = false;
  protected boolean cited = false;

  // will be put to true if user explicitly disambiguated this actor
  // if this id is -1, the actor will be created (whatever this actor is a namesake or not)
  // if this actor has an id, we will not check for name matches
  private boolean isDisambiguated = false;
  private boolean isNew = false;
  // list of actors whith the same name as this actor (used when actor has no id and we possibly found existing actors)
  private List<ActorHolder> nameMatches = new ArrayList<>();
  // same logic as the above name matches process for affiliation actor
  private boolean isAffDisambiguated = false;
  private boolean isAffNew = false;
  private List<ActorHolder> affNameMatches = new ArrayList<>();

  private ActorSimpleForm affiliation;
  private ActorDistance distance;

  private String formId;
  private int formIndex;

  private String lang;

  @Inject
  protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  @Inject
  protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();


  /**
   * Play / JSON compliant constructor
   */
  public ActorSimpleForm() {
    // needed by Play
  }

  /**
   * Construct an ActorSimpleForm from an existing Actor role object
   *
   * @param role an Actor role object
   * @param lang 2-char ISO code of the user language
   */
  public ActorSimpleForm(ActorRole role, String lang) {
    init(role.getActor(), lang);

    if (role.getAffiliation() != null) {
      aha = role.getAffiliation().getId();

      if (role.getAffiliation().getFunction() != null) {
        functionid = role.getAffiliation().getFunction().getId();
        function = role.getAffiliation().getFunction().getName(lang);
        Profession sub = role.getAffiliation().getFunction().getSubstitute();
        substitute = sub != null ? sub.getName(lang) : function;
      }

      if (role.getAffiliation().getActor() != null) {
        affname = role.getAffiliation().getActor().getFullname(lang);
        affid = role.getAffiliation().getActor().getId();
        affiliation = new ActorSimpleForm(role.getAffiliation().getActor(), lang);
      }
    }

    author = role.isAuthor();
    reporter = role.isReporter();
  }

  /**
   * Construct an ActorSimpleForm from an Actor only
   *
   * @param actor an Actor object
   */
  public ActorSimpleForm(Actor actor, String lang) {
    this(actor, lang, true);
  }

  /**
   * Construct an ActorSimpleForm from an Actor only
   *
   * @param actor an Actor object
   */
  public ActorSimpleForm(Actor actor, String lang, boolean full) {
    init(actor, lang);

    // set affiliation to most recent one
    if (full && !actor.getAffiliations().isEmpty()) {
      Affiliation affiliation = actor.getAffiliations().get(0);
      aha = affiliation.getId();
      if (affiliation.getFunction() != null) {
        functionid = affiliation.getFunction().getId();
        function = affiliation.getFunction().getName(lang);
        String personGender = actorFactory.getPersonGender(actor.getId());
        if(personGender != null && !personGender.equals(""))functionGender = personGender;
      }
      if (affiliation.getActor() != null) {
        affname = affiliation.getActor().getFullname(lang);
        affid = affiliation.getActor().getId();
      }
    }
  }

  /**
   * Construct an ActorSimpleForm from an Actor, and a distance with another actor
   *
   * @param actor an Actor object
   * @param distance the distance with another actor
   */
  public ActorSimpleForm(Actor actor, String lang, ActorDistance distance) {
    id = actor.getId();
    fullname = actor.getFullname(lang);
    avatar = helper.getDefaultActorAvatar(actor);
    this.lang = lang;

    this.distance = distance;
  }

  /**
   * Cast an external actor name to an actor name
   *
   * @param actorId the actor id
   * @param actorName the name from external source
   */
  public ActorSimpleForm(Long actorId, String actorName) {
    id = actorId;
    fullname = actorName;
  }

  /**
   * Private constructor, set up actor-related properties from given actor
   *
   * @param actor an API actor to cast into an ActorSimpleForm
   */
  private void init(Actor actor, String lang) {

    if (actor.getActorType() == EActorType.PERSON) {
      Person p = (Person) actor;

      actortype = EActorType.PERSON.id();
      birthOrCreation = p.getBirthdate() != null ? p.getBirthdate() : "";
      deathOrTermination = p.getDeathdate() != null ? p.getDeathdate() : "";
      gender = p.getGender() != null ? p.getGender().getName(lang) : "";
      genderId = p.getGender() != null ? p.getGender().getCode() : "";
      residence = p.getResidence() != null ? p.getResidence().getName(lang) : "";
      functionGender = gender.equals("") ? "N" : gender;
    } else if (actor.getActorType() == EActorType.ORGANIZATION) {
      Organization o = (Organization) actor;

      actortype = EActorType.ORGANIZATION.id();
      birthOrCreation = o.getCreationDate() != null ? o.getCreationDate() : "";
      deathOrTermination = o.getTerminationDate() != null ? o.getTerminationDate() : "";
      legal = o.getLegalStatus() != null ? o.getLegalStatus().getName(lang) : "";
      residence = o.getPlaces() != null && !o.getPlaces().isEmpty() ? o.getPlaces().get(0).getName(lang) : "";
      o.getBusinessSectors().forEach(s -> sectors.add(s.getName(lang)));
    } else {
      actortype = EActorType.UNKNOWN.id();
    }

    id = actor.getId();
    lastname = actor.getName(lang).getLast();
    firstname = actor.getName(lang).getFirst();
    fullname = actor.getFullname(lang);
    pseudo = actor.getName(lang).getPseudo();
    this.lang = lang;

    avatar = helper.getDefaultActorAvatar(actor);
  }

  @Override
  public String toString() {
    return fullname + " (" + id + ")" + " " + function +  " (" + functionid  + ") "
        + affname + " (" + affid + ") [aha:" + aha + "] " + isDisambiguated;
  }

  /**
   * Check if firstname, lastname, name and function are empty
   *
   * @return true if all fields are considered as empty (null or "")
   */
  public boolean isEmpty() {
    return values.isBlank(fullname) && values.isBlank(getFullFunction());
  }

  /**
   * Check if firstname and lastname are empty
   *
   * @return true if all fields are considered as empty (null or "")
   */
  public boolean isFullnameEmpty() {
    return values.isBlank(fullname);
  }

  /**
   * Check if this Actor has an affiliation
   *
   * @return true if this Actor has an affiliation (function and/or affiliation Actor), false otherwise
   */
  public boolean hasAffiliation() {
    return !values.isBlank(getFullFunction());
  }

  /**
   * Helper method used to check and update (or clear) all this form's fields, ie
   * <ul>
   *   <li>if this actor's name corresponds to the (hidden) id that has been auto-completed from UI</li>
   *   <li>try to get an existing function (id) or clear function id if it does not correspond with DB</li>
   *   <li>check affiliation and, if affiliation id and function id are present, try to get an existing affiliation, if any</li>
   * </ul>
   */
  public void checkFieldsIntegrity() {
    Actor actor = checkActor();

    // check function field now
    if (!values.isBlank(getFunction())) {
      checkFunction();
    } else {
      // force cleaning of functionid
      setFunctionid(-1);
    }

    // check affname
    if (!values.isBlank(getAffname())) {
      checkAffName();
    } else {
      // force cleaning of affid
      setAffid(-1L);
    }

    // if we retrieved an actor and got a match for either a function or an affiliation, check if we may not find a aha
    if (actor != null && values.isBlank(getAha()) &&
        (!values.isBlank(getAffid()) || !values.isBlank(getFunctionid()))) {
      findAffiliationMatch(actor);
    }
  }


  /**
   * Check if this.id correspond to the actor present in database and if we have an affiliation id (aha),
   * that has been auto-filled by the autocomplete feature, it must correspond to the affiliation present in DB too.
   *
   * @return the actor corresponding to this ActorSimpleForm if such an actor could be found, null otherwise
   */
  private Actor checkActor() {

    if (values.isBlank(id)) {
      return null;
    }

    Actor actor = actorFactory.retrieve(id);
    // does name match ?
    if (actor == null) {
      id = -1L;
      aha = -1L;
      return null;
    }

    // check affiliations for retrieved actor
    if (!values.isBlank(aha)) {
      // check that if we have a aha, concatenation of function and affname corresponds
      Optional<Affiliation> optional = actor.getAffiliations().stream().filter(aff ->
          aff.getId().equals(aha)).findAny();

      if (optional.isPresent()) {
        Affiliation affiliation = optional.get();
        // if this affiliation has a function
        if ((affiliation.getFunction() != null
              // its name must match
              && !affiliation.getFunction().professionHasName(function))
            // or if affiliation actor exists
            || (affiliation.getActor() != null
              // its name must match name in label
              && !affiliation.getActor().getFullname(lang).equals(affname))) {

          logger.debug("found aha for id " + aha + " and description " + getFullFunction() + " does not correspond in DB " +
              "for " + getFullname() + ". Affiliation in DB is " + affiliation.toString());
          // clear affiliation ids
          aha = -1L;
          affid = -1L;
        }

      } else {
        logger.debug("no such affiliation " + aha + " for actor " + getFullname());
        // clear affiliation ids
        aha = -1L;
        affid = -1L;
      }
    }
    return actor;
  }

  /**
   * Check and update function field by verifying if id and names matches and if no id is present, it
   * can not be bound to an existing profession object from database.
   *
   * If a match has been found, this.functionid is updated, otherwise, it is cleared.
   */
  private Profession checkFunction() {
    // if we have an id, name must match
    Profession profession = null;
    if (!values.isBlank(functionid)) {
      try {
        profession = actorFactory.getProfession(functionid);
        if (profession == null || profession.getNames().values().stream().noneMatch(function::equals)) {
          functionid = -1;
        }
      } catch (FormatException e) {
        logger.debug("unable to get function " + functionid, e);
        functionid = -1;
      }
    }

    // re-try to find a match (id may have been cleared because of mismatch just before
    if (values.isBlank(functionid)) {
      profession = actorFactory.findProfession(function, lang);
      if (profession != null) {
        logger.debug("found match for profession " + profession);
        functionid = profession.getId();
      }
    }
    return profession;
  }

  /**
   * This method is called when either this affid or functionid (or both) is not blank, but no aha (actor has affiliation id)
   * as been automatically added from form (with autocomplete feature). It tries to update this.aha by comparing
   * function/affname in this actor and given actor being the db actor corresponding to this actor form.
   *
   * Function id and affiliation actor id must be valid if they are set in this actor simple form.
   *
   * If this actor has multiple affiliation to the same organization without functions (only disambiguated on
   * dates), the match will be made on the last iterated among his/her affiliations to that organization.
   *
   * @param actor the actor to be compared to this actorname to try to find a match in affiliation
   */
  private void findAffiliationMatch(Actor actor) {
    logger.debug("try to find affiliation match for " + actor.getFullname(lang) + " with " + getFullFunction());
    actor.getAffiliations().forEach(aff -> {

      // either both functions are null
      if (((aff.getFunction() == null && values.isBlank(function))
          // or their function ids match
          || (aff.getFunction() != null && aff.getFunction().getId() == functionid))
          // AND either both affiliation actor are null
          && ((aff.getActor() == null && values.isBlank(affname))
          // or their ids match
          || (aff.getActor() != null && aff.getActor().getId().equals(affid)))) {

        // set aha value according to match
        logger.debug("found affiliation match for " + actor.getFullname(lang) + ", bound to aha: " + aff.getId());
        aha = aff.getId();
      }
    });
  }

  /**
   * Check this affiliation name and clear id if it does not correspond to the one retrieve in database (in current lang)
   */
  private void checkAffName() {
    // if we have an id, names must match
    if (!values.isBlank(affid)) {
      Actor affiliation = actorFactory.retrieve(affid);
      if (affiliation == null) {
        // names do not correspond, clear it
        logger.debug("given affid " + affid + " does not exists in db");
        affid = -1L;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ActorSimpleForm && obj.hashCode() == hashCode();
  }

  @Override
  public int hashCode() {
    return 83
        + (fullname != null ? fullname.hashCode() : 0)
        + (function != null ? function.hashCode() : 0)
        + (affname != null ? affname.hashCode() : 0);
  }

  /*
   * GETTERS / SETTERS
   */

    /**
   * Get this actor id
   *
   * @return the actor id (API actor that has been cast)
   */
  public Long getId() {
    return id;
  }

  /**
   * Set this actor id
   *
   * @param id an id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the integer value corresponding to the EActorType of this actor
   *
   * @return an integer value
   */
  public int getActortype() {
    return actortype;
  }

  /**
   * Get the last name of this actor (if it is not a person - actortype = 0 -, same as name)
   *
   * @return the lastname
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * Set this actor's lastname
   *
   * @param lastname a lastname (for a person) or a name (any other)
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * Get the concatenation of firstname and lastname for persons (actortype = 0) or same as lastname for others
   *
   * @return the name
   */
  public String getFullname() {
    return fullname;
  }

  /**
   * Set the name for this actor (won't set lastname and firstname)
   *
   * @param fullname a name to set
   */
  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getPseudo() {
    return pseudo;
  }

  /**
   * Get this actor's function (aka profession)
   *
   * @return a function name (may be empty)
   */
  public String getFunction() {
    return function;
  }

  /**
   * Get the substitute function (ie, equivalent that will be shown in viz graphs and filters)
   *
   * @return the equivalent function, if any, otherwise return the function
   */
  public String getSubstitute() {
    return substitute;
  }

  /**
   * Set this actor's function (aka profession)
   *
   * @param function a function name
   */
  public void setFunction(String function) {
    this.function = function;
  }

  /**
   * Get the function id, if any
   *
   * @return a function id (or -1 if none)
   */
  public Integer getFunctionid() {
    return functionid;
  }

  /**
   * Set a function id (won't update corresponding function accordingly)
   *
   * @param functionid a function id
   */
  public void setFunctionid(Integer functionid) {
    this.functionid = functionid;
  }

  /**
   * Get the current affiliation id of this actor (as identified in the related contribution)
   *
   * @return an affiliation id (-1L if none)
   */
  public Long getAffid() {
    return affid;
  }

  /**
   * Set the current affiliation actor id of this actor (as identified in the related contribution). Won't update
   * affiliation-related fields accordingly (function, affname, aha,...)
   *
   * @param affid an affiliation actor id
   */
  public void setAffid(Long affid) {
    this.affid = affid;
  }

  /**
   * Get the affiliation name
   *
   * @return a name
   */
  public String getAffname() {
    return affname;
  }

  /**
   * Set the current affiliation name of this actor (as identified in the related contribution). Won't update
   * affiliation-related fields accordingly (function, affid, aha,...)
   *
   * @param affname an affiliation name
   */
  public void setAffname(String affname) {
    this.affname = affname;
  }

  /**
   * Get the affiliation id, ie, the id under which this actor is bound to this affiliation (affname, function,...)
   *
   * @return an affiliation id
   */
  public Long getAha() {
    return aha;
  }

  /**
   * Set the current affiliation id of this actor (as identified in the related contribution). Won't update
   * affiliation-related fields accordingly (function, affid, affname,...)
   *
   * @param aha an affiliation id
   */
  public void setAha(Long aha) {
    this.aha = aha;
  }

  /**
   * Check if this actor is referred as an author of bound contribution
   *
   * @return true if this actor is an author of bound contribution
   */
  public boolean getAuthor() {
    return author;
  }

  /**
   * Set if this actor is referred as an author of bound contribution
   *
   * @param author true if this actor is an author of bound contribution
   */
  public void setAuthor(boolean author) {
    this.author = author;
  }

  /**
   * Check if this actor is referred as a reporter of bound contribution
   *
   * @return true if this actor is a reporter of bound contribution
   */
  public boolean getReporter() {
    return reporter;
  }

  /**
   * Set if this actor is referred as a reporter of bound contribution
   *
   * @param reporter true if this actor is a reporter of bound contribution
   */
  public void setReporter(boolean reporter) {
    this.reporter = reporter;
  }

  /**
   * Check if this actor is referred as a cited of bound contribution
   *
   * @return true if this actor is a cited of bound contribution
   */
  public boolean getCited() {
    return cited;
  }

  /**
   * Set if this actor is referred as a cited of bound contribution
   *
   * @param cited true if this actor is a cited of bound contribution
   */
  public void setCited(boolean cited) {
    this.cited = cited;
  }

  /**
   * Helper function to get role's key
   *
   * @return a string representation of the actual role
   */
  public String getRole() {
    return getAuthor() ? "author" : (getReporter() ? "reporter" :"none");
  }

  /**
   * Get the current language 2-char ISO code of the user's UI
   *
   * @return a two char iso code as defined in accepted languages for the webdeb system
   */
  public String getLang() {
    return lang;
  }

  /**
   * Set the current language 2-char ISO code of the user's UI
   *
   * @param lang a two char iso code as defined in accepted languages for the webdeb system
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getDefaultAvatar(){
    return this.avatar;
  }

  /*
   * NAME MATCHES HANDLING
   */

  /**
   * Get the isDisambiguated flag, ie, if this flag is false and this actor has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @return true if this actor has been explicitly disambiguated
   */
  public boolean getIsDisambiguated() {
    return isDisambiguated;
  }

  /**
   * Set the isDisambiguated flag, ie, if this flag is false and this actor has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @param isDisambiguated true if this actor has been explicitly disambiguated
   */
  public void setIsDisambiguated(boolean isDisambiguated) {
    this.isDisambiguated = isDisambiguated;
  }

  /**
   * Get the list  possible matches on this actor's full name
   *
   * @return a (possibly empty) list of actor holder having the same name as this actor
   */
  public List<ActorHolder> getNameMatches() {
    return nameMatches;
  }

  /**
   * Set the list of possible matches on this actor's full name
   *
   * @param nameMatches a list of actor holder having the same name as this actor
   */
  public void setNameMatches(List<ActorHolder> nameMatches) {
    this.nameMatches = nameMatches;
  }

  /**
   * Get the isAffDisambiguated flag, ie, if this flag is false and this affiliation actor has no id, a search by name
   * (full match) must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the affiliation actor id value is
   *
   * @return true if this affiliation actor has been explicitly disambiguated
   */
  public boolean getIsAffDisambiguated() {
    return isAffDisambiguated;
  }

  /**
   * Set the isAffDisambiguated flag, ie, if this flag is false and this affiliation actor has no id, a search by name
   * (full match) must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the affiliation actor id value is
   *
   * @param isAffDisambiguated true if this affiliation actor has been explicitly disambiguated
   */
  public void setIsAffDisambiguated(boolean isAffDisambiguated) {
    this.isAffDisambiguated = isAffDisambiguated;
  }

  /**
   * Get the list of possible matches on this affiliation actor's full name
   *
   * @return a (possibily empty) list of actor holder having the same name as this affiliation actor
   */
  public List<ActorHolder> getAffNameMatches() {
    return affNameMatches;
  }

  /**
   * Set the list of possible matches on this affiliation actor's full name
   *
   * @param nameMatches a list of actor holder having the same name as this affiliation actor
   */
  public void setAffNameMatches(List<ActorHolder> nameMatches) {
    this.affNameMatches = nameMatches;
  }
  
  /**
   * Get the concatenation of this actor's function and affiliation name
   *
   * @return a (possibly empty) concatenation of function and affiliation
   */
  public String getFullFunction() {
    return (function + " " + affname).trim();
  }

  @JsonIgnore
  public ActorSimpleForm getAffiliation() {
    return affiliation;
  }

  /*
   * FOR FILTERABLE FEATURE
   */

  /**
   * Get the residence/head office of this actor
   *
   * @return a list of names in this.lang, may be empty
   */
  public String getResidence() {
    return residence;
  }

  /**
   * Get the birth (person) or creation (organization) date of this actor
   *
   * @return a string formatted date of the form (MM/DD)/YYYY (MM and DD optional), may be empty
   */
  @JsonIgnore
  public String getBirthOrCreation() {
    return birthOrCreation;
  }

  /**
   * Get the death (person) or termination (organization) date of this actor
   *
   * @return a string formatted date of the form (MM/DD)/YYYY (MM and DD optional), may be empty
   */
  @JsonIgnore
  public String getDeathOrTermination() {
    return deathOrTermination;
  }

  /**
   * Get the gender of the function related to the actor
   *
   * @return the gender
   */
  @JsonIgnore
  public String getFunctionGender() {
    return functionGender;
  }

  /**
   * Get the gender of this actor, if this actor form is a person
   *
   * @return the gender in this.lang, may be empty
   */
  @JsonIgnore
  public String getGender() {
    return gender;
  }

  /**
   * Get the legal status of this actor, if this actor form is an organization
   *
   * @return the legal status in this.lang, may be empty
   */
  @JsonIgnore
  public String getLegal() {
    return legal;
  }

  /**
   * Get the list of business sectors, if this actor form is an organization
   *
   * @return a list of sectors' name in this.lang, may be empty
   */
  @JsonIgnore
  public List<String> getSectors() {
    return sectors;
  }

  /**
   * Get the position distance with another actor
   *
   * @return the distance
   */
  public ActorDistance getPositionDistance() {
    return distance;
  }

  public boolean getIsNew() {
    return isNew;
  }

  public void setIsNew(boolean aNew) {
    isNew = aNew;
  }

  public boolean getIsAffNew() {
    return isAffNew;
  }

  public void setIsAffNew(boolean affNew) {
    isAffNew = affNew;
  }

  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public int getFormIndex() {
    return formIndex;
  }

  public void setFormIndex(int formIndex) {
    this.formIndex = formIndex;
  }
}
