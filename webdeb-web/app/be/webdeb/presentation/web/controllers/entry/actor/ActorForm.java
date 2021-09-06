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
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributorPictureForm;
import be.webdeb.presentation.web.controllers.entry.PlaceForm;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.data.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This class wraps all fields of both Actor types to build the web form.
 *
 * Note that all supertype getters corresponding to predefined values (ie, types) are sending
 * ids instead of language-dependent descriptions.
 *
 * Using isDisambiguated flag to know if the actor must be disambiguated, ie, the id is empty and we found possible
 * matches in database. If so, the views.util.handleNameMatches frame must be triggered to ask to user to explicitly
 * choose between binding to an existing actor or create a new one. This name matches check is performed explicitly as
 * part of the form validation processes, but outside of Play's implicit validation method.
 *
 * {@link be.webdeb.presentation.web.controllers.entry.ContributionHelper#searchForNameMatches(ActorSimpleForm, String)
 * Actor's potential name matches} and {@link be.webdeb.presentation.web.controllers.entry.ContributionHelper#findAffiliationsNameMatches(List, WebdebUser, String)
 * Affiliation name matches} are currently performed on full matches, but the method in charge may be easily amended to perform
 * more advanced matching queries. The UI process would remain the same, but actual accessor method would require
 * to be changed (not using factory's findByFullname anymore).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ActorForm extends ActorHolder  {

  protected List<AffiliationForm> affiliationsForm;
  protected List<AffiliationForm> qualificationsForm;
  protected List<AffiliationForm> parentsForm;
  protected List<AffiliationForm> orgaffiliationsForm;

  // will be put to true if user explicitly explicitly disambiguated this actor
  // so no checks on possible name matches must be performed
  private boolean isDisambiguated;
  // holding the list of possible matches on this actor name
  private List<ActorHolder> nameMatches;
  private String defaultName;

  // redefine sectors: initialize with all sectors
  private List<Boolean> allSectors;

  private ContributorPictureForm avatarForm;
  private ContributorPictureForm orgAvatarForm;
  private String avatarString;

  /**
   * Play / JSON compliant constructor
   */
  public ActorForm() {
    super();
    allSectors = new ArrayList<>();
    nameMatches = new ArrayList<>();
    actorFactory.getBusinessSectors().forEach(s -> allSectors.add(false));
    affiliationsForm = new ArrayList<>();
    parentsForm = new ArrayList<>();
    qualificationsForm = new ArrayList<>();
    orgaffiliationsForm = new ArrayList<>();
    avatarForm = new ContributorPictureForm();
    orgAvatarForm = new ContributorPictureForm();
  }

  /**
   * Construct an ActorWrapper with the data of an existing Actor (for visualization/editing)
   *
   * @param actor an Actor
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ActorForm(Actor actor, WebdebUser user, String lang) {
    super(actor, user, lang);

    defaultName = actor.getFullname(lang);
    nameMatches = new ArrayList<>();
    isDisambiguated = false;
  }

  protected void init() {
    affiliationsForm = makeAffiliationsForms();
    orgaffiliationsForm = makeAffiliationsForms();
  }

  /**
   * Initialize gender and countries with ids, not labels since they are editable
   *
   * @param p a person
   */
  @Override
  protected void init(Person p) {
    gender = genderId = p.getGender() != null ? p.getGender().getCode() : "";
    residence = p.getResidence() != null ? p.getResidence().getCode() : "";
    affiliationsForm = makeAffiliationsForms();
    qualificationsForm = makeAffiliationsForms(EAffiliationType.GRADUATING_FROM);
    parentsForm = makeAffiliationsForms(EAffiliationType.SON_OF);
    orgaffiliationsForm = new ArrayList<>();
    avatarForm = new ContributorPictureForm(p.getAvatar());
  }

  /**
   * Initialize legal status, territories and sectors with ids not labels, since they are editable
   *
   * @param o an organization
   */
  @Override
  protected void init(Organization o) {
    legalStatus = o.getLegalStatus() != null ? String.valueOf(o.getLegalStatus().getType()) : "";
    headOffice = o.getPlaces() != null && !o.getPlaces().isEmpty() ? o.getPlaces().get(0).getName(lang) : "";
    // set business sectors checkboxes
    o.getBusinessSectors().forEach(s -> businessSectors.add(String.valueOf(s.getType())));
    allSectors = new ArrayList<>();
    actorFactory.getBusinessSectors().forEach(s -> allSectors.add(businessSectors.contains(String.valueOf(s.getType()))));
    affiliationsForm = new ArrayList<>();
    orgaffiliationsForm = makeAffiliationsForms();
    orgAvatarForm = new ContributorPictureForm(o.getAvatar());

    initPlaces(o.getPlaces(), lang);
    initTags(o.getTagsAsList(), lang);
  }

  /**
   * Validate the creation of an Actor (implicit call from form submission)
   *
   * @return null if validation ok, map of errors for each fields in error otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();
    Map<String, List<ValidationError>> tempErrors;
    List<ValidationError> list;
    switch (EActorType.value(actortype)) {
      case PERSON:
        // check name
        if (name.isEmpty()) {
          errors.put("name.last", Collections.singletonList(new ValidationError("name.last", "actor.error.name")));
        } else {
          // must remap keys of errors to "name."
          checkActorNames(Collections.singletonList(name), "allnames", false).entrySet().forEach(s ->
              errors.put("name" + s.getKey().substring(s.getKey().lastIndexOf('.')), s.getValue()));
        }
        errors.putAll(checkActorNames(allnames, "allnames", true));

        if (!values.isBlank(birthdate.trim()) && !values.isDate(birthdate.trim())) {
          errors.put("birthdate", Collections.singletonList(new ValidationError("birthdate", "actor.error.date.format")));
        }

        if (!values.isBlank(deathdate.trim()) && !values.isDate(deathdate.trim())) {
          errors.put("deathdate", Collections.singletonList(new ValidationError("deathdate", "actor.error.date.format")));
        }

        if (!values.isBlank(birthdate.trim()) && !values.isBlank(deathdate.trim())
            && !values.isBefore(birthdate.trim(), deathdate.trim())) {
          errors.put("deathdate", Collections.singletonList(new ValidationError("deathdate", "actor.error.date.after.person")));
        }

        if (!values.isBlank(crossref)) {
          crossref = crossref.contains("://") ? crossref : "http://" + crossref;
          list = helper.checkUrl(crossref, "crossref");
          if (!list.isEmpty()) {
            errors.put("crossref", list);
          }
        }
        // check affiliations
        affiliationsForm.removeIf(AffiliationForm::isEmpty);
        if ((tempErrors = checkAffiliations(getFullname(), birthdate.trim(), deathdate.trim(), true, affiliationsForm, "affiliationsForm")) != null) {
          errors.putAll(tempErrors);
        }
        qualificationsForm.removeIf(AffiliationForm::isEmpty);
        if ((tempErrors = checkAffiliations(getFullname(), birthdate.trim(), deathdate.trim(), true, qualificationsForm, "qualificationsForm")) != null) {
          errors.putAll(tempErrors);
        }
        parentsForm.removeIf(AffiliationForm::isEmpty);
        if ((tempErrors = checkAffiliations(getFullname(), birthdate.trim(), deathdate.trim(), true, parentsForm, "parentsForm")) != null) {
          errors.putAll(tempErrors);
        }
        break;

      case ORGANIZATION:
        Map<String, List<ValidationError>> subErrors;

        if (orgname.isEmpty()) {
          errors.put("orgname.last", Collections.singletonList(new ValidationError("orgname.last", "actor.error.orgname")));
        } else {
          checkActorNames(Collections.singletonList(orgname), "allorgnames", false).entrySet().forEach(s ->
              errors.put("orgname" + s.getKey().substring(s.getKey().lastIndexOf('.')), s.getValue()));
        }
        errors.putAll(checkActorNames(allorgnames, "allorgnames", true));
        errors.putAll(checkActorNames(oldorgnames, "oldorgnames", false));

        /*
        if (tags != null && !tags.isEmpty() && !values.isBlank(tags.get(0).getName())) {
          subErrors = checkTags(tags, "tags");
          if(subErrors != null) errors.putAll(subErrors);
        } */

        if(places != null){
          subErrors = checkPlaces(places);
          if(subErrors != null) errors.putAll(subErrors);
        }

        if (!values.isBlank(creationDate.trim()) && !values.isDate(creationDate.trim())) {
          errors.put("creationDate", Collections.singletonList(new ValidationError("creationDate", "actor.error.date.format")));
        }

        if (!values.isBlank(terminationDate.trim()) && !values.isDate(terminationDate.trim())) {
          errors.put("terminationDate", Collections.singletonList(new ValidationError("terminationDate", "actor.error.date.format")));
        }

        if (!values.isBlank(creationDate.trim()) && !values.isBlank(terminationDate.trim())
            && !values.isBefore(creationDate.trim(), terminationDate.trim())) {
          errors.put("terminationDate", Collections.singletonList(new ValidationError("terminationDate", "actor.error.date.after.org")));
        }

        if (!values.isBlank(orgcrossref)) {
          orgcrossref = orgcrossref.contains("://") ? orgcrossref : "http://" + orgcrossref;
          list = helper.checkUrl(orgcrossref, "orgcrossref");
          if (!list.isEmpty()) {
            errors.put("orgcrossref", list);
          }
        }

        // check affiliations
        orgaffiliationsForm.removeIf(AffiliationForm::isEmpty);
        if ((tempErrors = checkAffiliations(getFullname(), creationDate.trim(), terminationDate.trim(), false, orgaffiliationsForm, "orgaffiliationsForm")) != null) {
          errors.putAll(tempErrors);
        }
        break;

      default:
        // unknown type => return error
        errors.put("actortype", Collections.singletonList(new ValidationError("actortype", "actor.error.actortype")));
    }

    // must return null if errors is empty
    return errors.isEmpty() ? null : errors;
  }

  /**
   * Store the Actor into the database, if this.id has been set, update Actor. All passed affiliation ids
   * (aha) are also considered as valid, if no id is set, the affiliation is considered as non-existing.
   * Updates this.id with the id_contribution of the created byActor
   *
   * @param contributor the contributor id that filled in this byActor
   * @return the map of contribution type and automatically created actors (as Contribution) when saving this byActor
   * (i.e. unknown affiliations)
   *
   * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
   * @throws PermissionException if given contributor may not perform this action or if such action would cause
   * integrity problems
   * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
   */
  public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
    logger.debug("try to save " + toString() + " with version " + version + " in group " + inGroup);
    Map<Integer, List<Contribution>> result;

    inGroup = values.isBlank(inGroup) ? 0 : inGroup;

    switch (EActorType.value(actortype)) {
      case PERSON:
        Person p = actorFactory.getPerson();
        p.setId(id != null ? id : -1L);
        p.setVersion(version);
        p.addInGroup(inGroup);
        allnames.add(name);
        p.setNames(toActorNames(allnames));

        if (!values.isBlank(crossref)) {
          try {
            p.setCrossReference(crossref);
          } catch (FormatException e) {
            logger.error("invalid url has been ignored " + crossref, e);
          }
        }

        if (!values.isBlank(birthdate.trim())) {
          try {
            p.setBirthdate(birthdate.trim());
          } catch (FormatException e) {
            logger.error("unparsable birth date has been ignored " + birthdate, e);
          }
        }

        if (!values.isBlank(deathdate.trim())) {
          try {
            p.setDeathdate(deathdate.trim());
          } catch (FormatException e) {
            logger.error("unparsable death date has been ignored " + deathdate, e);
          }
        }

        if (!values.isBlank(residence)) {
          try {
            p.setResidence(actorFactory.getCountry(residence));
          } catch (FormatException e) {
            // should not happen since form has been validated
            logger.error("unknown country code has been ignored " + residence, e);
          }
        }
        if (!values.isBlank(gender)) {
          try {
            p.setGender(actorFactory.getGender(gender));
          } catch (FormatException e) {
            logger.error("unknown gender id has been ignored " + gender, e);
          }
        }

        p.setAffiliations(new ArrayList<>());
        updateAffiliations(affiliationsForm, p);
        updateAffiliations(qualificationsForm, p, EAffiliationType.GRADUATING_FROM, EProfessionType.FORMATION);
        updateAffiliations(parentsForm, p, EAffiliationType.SON_OF);

        // handle possible duplicated in unknown affiliation actors
        removeDuplicateUnknownActors(p);

        p.setAvatarExtension(avatarString);

        result = p.save(contributor, inGroup);
        id = p.getId();
        break;

      case ORGANIZATION:
        Organization org = actorFactory.getOrganization();
        org.setId(id);
        org.setVersion(version);
        org.addInGroup(inGroup);

        // add (old) names
        allorgnames.add(orgname);
        org.setNames(toActorNames(allorgnames));
        org.setOldNames(toActorNames(oldorgnames));

        org.setOfficialNumber(officialNumber);

        if (!values.isBlank(orgcrossref)) {
          try {
            org.setCrossReference(orgcrossref.trim());
          } catch (FormatException e) {
            logger.error("invalid url given " + orgcrossref, e);
          }
        }

        // sectors
        for (int i = 0; i < allSectors.size(); i++) {
          if (allSectors.get(i)) {
            try {
              org.addBusinessSector(actorFactory.getBusinessSector(i));
            } catch (FormatException e) {
              logger.error("unknown sector id has been ignored " + i, e);
            }
          }
        }
        // tags
        org.initTags();
        for (SimpleTagForm f : tags.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()))  {
          try {
            org.addTag(helper.toTag(f, lang, ETagType.SOCIAL_OBJECT));
          } catch (FormatException e) {
            logger.error("unparsable tag " + f, e);
          }
        }

        // bound places
        org.initPlaces();
        for (PlaceForm place : places) {
          if(!place.isEmpty()) {
            Place placeToAdd = createPlaceFromForm(place);
            if(placeToAdd != null)
              org.addPlace(placeToAdd);
          }
        }

        if (!values.isBlank(legalStatus) && values.isNumeric(legalStatus)) {
          try {
            org.setLegalStatus(actorFactory.getLegalStatus(Integer.parseInt(legalStatus)));
          } catch (FormatException e) {
            logger.error("unknown legal status id has been ignored " + legalStatus, e);
          }
        }

        if (!values.isBlank(creationDate.trim())) {
          try {
            org.setCreationDate(creationDate.trim());
          } catch (FormatException e) {
            logger.error("unparsable creation date has been ignored " + birthdate, e);
          }
        }

        if (!values.isBlank(terminationDate.trim())) {
          try {
            org.setTerminationDate(terminationDate.trim());
          } catch (FormatException e) {
            logger.error("unparsable termination date has been ignored " + deathdate, e);
          }
        }

        if (!values.isBlank(officialNumber)) {
          org.setOfficialNumber(officialNumber);
        }

        org.setAffiliations(new ArrayList<>());
        updateAffiliations(orgaffiliationsForm, org);

        // handle possible duplicated in unknown affiliation actors
        removeDuplicateUnknownActors(org);

        org.setAvatarExtension(avatarString);

        result = org.save(contributor, inGroup);
        id = org.getId();
        break;

      default:
        Actor a = actorFactory.getActor();
        a.setId(id);
        a.setVersion(version);
        a.addInGroup(inGroup);
        allorgnames.add(orgname);
        a.setNames(toActorNames(allorgnames));
        result = a.save(contributor, inGroup);
        id = a.getId();
    }
    return result;
  }


  /*
   * SETTERS
   */

  /**
   * Set this actor's name (persons)
   *
   * @param name a name
   */
  public void setName(ActorNameFields name) {
    this.name = name;
  }

  /**
   * Set the list of all other names for this actor
   *
   * @param allnames a list of names
   */
  public void setAllnames(List<ActorNameFields> allnames) {
    this.allnames = allnames;
  }

  /**
   * Set the country of residence for a person
   *
   * @param residence a 2-char ISO 3166-1 alpha-2
   */
  public void setResidence(String residence) {
    this.residence = residence;
  }

  /**
   * Set the person's gender
   *
   * @param gender the gender id (F or M)
   */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /**
   * Get the list of affiliations (person) form
   *
   * @return a possibily empty list of affiliations form
   */
  public List<AffiliationForm> getAffiliationsForm() {
    return affiliationsForm;
  }

  /**
   * Set the list of affiliations (person) form
   *
   * @param affiliationsForm a list of affiliations form
   */
  public void setAffiliationsForm(List<AffiliationForm> affiliationsForm) {
    this.affiliationsForm = affiliationsForm;
  }

  /**
   * Set the person date of birth in an acceptable format (DD/MM/YYYY with D and M optional)
   *
   * @param birthdate a date of birth
   */
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  /**
   * Set the person date of death in an acceptable format (DD/MM/YYYY with D and M optional)
   *
   * @param deathdate a date of death
   */
  public void setDeathdate(String deathdate) {
    this.deathdate = deathdate;
  }

  /**
   * Set the person external reference
   *
   * @param crossref an url
   */
  public void setCrossref(String crossref) {
    this.crossref = crossref;
  }

  /**
   * Set the organization name
   *
   * @param orgname a name
   */
  public void setOrgname(ActorNameFields orgname) {
    this.orgname = orgname;
  }

  /**
   * Set the list of all other names for this organization
   *
   * @param allorgnames a list of names
   */
  public void setAllorgnames(List<ActorNameFields> allorgnames) {
    this.allorgnames = allorgnames;
  }

  /**
   * Set the list of all old names for this organization
   *
   * @param oldorgnames a list of names
   */
  public void setOldorgnames(List<ActorNameFields> oldorgnames) {
    this.oldorgnames = oldorgnames;
  }

  /**
   * Set the official number of this organization actor
   *
   * @param officialNumber a number
   */
  public void setOfficialNumber(String officialNumber) {
    this.officialNumber = officialNumber;
  }

  /**
   * Set the list of business sector ids where this organization is active.
   *
   * @param businessSectors a list of business sector ids
   */
  public void setBusinessSectors(List<String> businessSectors) {
    this.businessSectors = businessSectors;
  }

  /**
   * Set the legal status id of this organization
   *
   * @param legalStatus a legal status id
   * @see be.webdeb.core.api.actor.LegalStatus
   */
  public void setLegalStatus(String legalStatus) {
    this.legalStatus = legalStatus;
  }

  /**
   * Set the DD/MM/YYYY (DD/MM optional)-formatted creation date
   *
   * @param creationDate the creation date of this organization
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Set the DD/MM/YYYY (DD/MM optional)-formatted dissolution date
   *
   * @param terminationDate the dissolution date of this organization
   */
  public void setTerminationDate(String terminationDate) {
    this.terminationDate = terminationDate;
  }


  /**
   * Set the country where this organization's head office is located
   *
   * @param headOffice a list country ISO 3166-1 alpha code
   * @see Country
   */
  public void setHeadOffice(String headOffice) {
    this.headOffice = headOffice;
  }

  /**
   * Set the organization external reference
   *
   * @param orgcrossref an url
   */
  public void setOrgcrossref(String orgcrossref) {
    this.orgcrossref = orgcrossref;
  }

  /**
   * Get the list of affiliations (for organization) form
   *
   * @return a possibly empty list of affiliations form
   */
  public List<AffiliationForm> getOrgaffiliationsForm() {
    return orgaffiliationsForm;
  }

  /**
   * Set the list of affiliations (for organization) form
   *
   * @param orgaffiliationsForm a list of affiliations form
   */
  public void setOrgaffiliationsForm (List<AffiliationForm> orgaffiliationsForm) {
    this.orgaffiliationsForm = orgaffiliationsForm;
  }

  /**
   * Get the list of qualifications (for person) form
   *
   * @return a possibly empty list of affiliations form
   */
  public List<AffiliationForm> getQualificationsForm() {
    return qualificationsForm;
  }

  /**
   * Set the list of qualifications (for person) form
   *
   * @param qualificationsForm a list of affiliations form
   */
  public void setQualificationsForm (List<AffiliationForm> qualificationsForm) {
    this.qualificationsForm =  qualificationsForm;
  }

  /**
   * Get the list of qualifications (for person) form
   *
   * @return a possibly empty list of affiliations form
   */
  public List<AffiliationForm> getParentsForm() {
    return parentsForm;
  }

  /**
   * Set the list of qualifications (for person) form
   *
   * @param parentsForm a list of affiliations form
   */
  public void setParentsForm (List<AffiliationForm> parentsForm) {
    this.parentsForm = parentsForm;
  }

  /**
   * Set the actortype id
   *
   * @param actortype an actor type id
   * @see be.webdeb.core.api.actor.EActorType
   */
  public void setActortype(int actortype) {
    this.actortype = actortype;
  }

  public ContributorPictureForm getAvatarForm() {
    return avatarForm;
  }

  public ContributorPictureForm getOrgAvatarForm() {
    return orgAvatarForm;
  }

  /**
   * Set the avatar form
   *
   * @param avatarForm a contributor picture form
   */
  public void setAvatarForm(ContributorPictureForm avatarForm) {
    this.avatarForm = avatarForm;
  }

  /**
   * Set the avatar form
   *
   * @param avatarForm a contributor picture form
   */
  public void setOrgAvatarForm(ContributorPictureForm avatarForm) {
    this.orgAvatarForm = avatarForm;
  }

  /**
   * Get the list of business sector flags (because business sectors are checkboxes in form)
   *
   * @return a list of boolean such as if sector-i applies to this organization, then List(i) = true
   */
  public List<Boolean> getAllSectors() {
    return allSectors;
  }

  /**
   * Set the list of business sector flags (because business sectors are checkboxes in form)
   *
   * @param allSectors a list of boolean such as if sector-i applies to this organization, then List(i) = true
   */
  public void setAllSectors(List<Boolean> allSectors) {
    this.allSectors = allSectors;
  }

  /*
   * NAME MATCHES HANDLING
   */

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
   * Set the list of possible name matches on this actor's name
   *
   * @param nameMatches a list of actors with same name
   */
  public void setNameMatches(List<ActorHolder> nameMatches) {
    this.nameMatches = nameMatches;
  }

  /**
   * Get the list of possible name matches on this actor's name
   *
   * @return a list of actors with same name
   */
  public List<ActorHolder> getNameMatches() {
    return nameMatches;
  }

  /**
   * Set the places concerned by this actor (organization)
   *
   * @param places a possibly empty list of places
   */
  public void setPlaces(List<PlaceForm> places) {
    this.places = places;
  }

  public String getAvatarString() {
    return avatarString;
  }

  public void setAvatarString(String avatarString) {
    this.avatarString = avatarString;
  }

  public String getDefaultName() {
    return defaultName;
  }

  public void setDefaultName(String defaultName) {
    this.defaultName = defaultName;
  }

  /*
   * PACKAGE / PRIVATE HELPERS
   */

  /**
   * Get all affiliations of this actor
   *
   * @return the (possibly empty) list of affiliations
   */
  private List<AffiliationForm> makeAffiliationsForms() {
    return makeAffiliationsForms(null);
  }

  /**
   * Get all affiliations of this actor
   *
   * @param type the affiliation type
   * @return the (possibly empty) list of affiliations
   */
  private List<AffiliationForm> makeAffiliationsForms(EAffiliationType type) {
    List<AffiliationForm> aff = (type == null ? actor.getSimpleAffiliations() : actor.getAffiliations(type))
            .stream()
            .filter(a -> a.getActor() != null)
            .sorted(Comparator.comparing(Affiliation::getActor))
            .map(a -> new AffiliationForm(a, user, lang))
            .collect(Collectors.toList());
    aff.forEach(e -> e.setEndDateType(EPrecisionDate.ONGOING.id() == e.getEndDateType() ? EPrecisionDate.AT_LEAST_UNTIL.id() : e.getEndDateType()));

    return aff;
  }


  /**
   * Check the name content
   *
   * @param names a list of names
   * @param checkLang also check language for duplicates
   * @return a (possibly empty) map of errors
   */
  private Map<String, List<ValidationError>> checkActorNames(List<ActorNameFields> names, String fieldname, boolean checkLang) {
    Map<String, List<ValidationError>> errors = new HashMap<>();
    for (ActorNameFields name : names.stream().filter(n -> !n.isEmpty()).collect(Collectors.toList())) {
      String field = fieldname + "[" + names.indexOf(name) + "].";
      if (actortype == EActorType.PERSON.id()) {
        // either we have a last and first name or we have a pseudo or all of them
        if (values.isBlank(name.getFirst()) && !values.isBlank(name.getLast())) {
          errors.put(field + "first", Collections.singletonList(new ValidationError(field + "first", "actor.error.nofirst")));
        }

        if (!values.isBlank(name.getFirst()) && values.isBlank(name.getLast())) {
          errors.put(field + "last", Collections.singletonList(new ValidationError(field + "last", "actor.error.nolast")));
        }

        if (values.isBlank(name.getFirst()) && values.isBlank(name.getLast()) && values.isBlank(name.getPseudo())) {
          errors.put(field + "first", Collections.singletonList(new ValidationError(field + "first", "actor.error.noname")));
          // put empty error message to make form highlight those fields too
          errors.put(field + "last", Collections.singletonList(new ValidationError(field + "last", "")));
          errors.put(field + "pseudo", Collections.singletonList(new ValidationError(field + "pseudo", "")));
        }
      } else {
        // the name is mandatory and the acronym must not contain punctuation char
        if (values.isBlank(name.getLast())) {
          errors.put(field + "last", Collections.singletonList(new ValidationError(field + "last", "actor.error.orgname")));
        }
        if (!values.isBlank(name.getFirst()) && name.getFirst().matches(".*[;.,].*]")) {
          errors.put(field + "acronym", Collections.singletonList(new ValidationError(field + "last", "actor.error.acronym")));
        }
      }
      // check language value, must be selected and must not been defined twice
      if (values.isBlank(name.getLang())) {
        errors.put(field + "lang", Collections.singletonList(new ValidationError(field + "lang", "actor.error.namelang")));
      } else {
        // check if we do not have twice the same lang (also with current language)
        if (checkLang && names.stream().filter(n -> !n.isEmpty()).anyMatch(n -> (n != name && n.getLang().equals(name.getLang())) || n.getLang().equals(lang))) {
          errors.put(field + "lang", Collections.singletonList(new ValidationError(field + "lang", "actor.error.lang.twice")));
        }
      }
    }
    return errors;
  }

  /**
   * Rebind duplicate unknown actor to same instance
   *
   * @param actor the actor from which all affiliations must be checked
   */
  private void removeDuplicateUnknownActors(Actor actor) {
    for (int i = 0; i < actor.getAffiliations().size(); i++) {
      // only actors with one and only one affiliation with actor's id = -1 are of interest
      Affiliation current = actor.getAffiliations().get(i);
      if (current != null && current.getActor() != null && values.isBlank(current.getActor().getId())) {
        // check all previous elements if such an affiliation does not yet exist, if so, replace current instance with other one
        // replace current affiliation actor with already created one
        Optional<Affiliation> affiliation = actor.getAffiliations().subList(0, i).stream().filter(a ->
            a.getActor() != null && values.isBlank(a.getActor().getId())
                && a.getActor().getFullname(lang).equals(current.getActor().getFullname(lang)))
            .findFirst();
        if (affiliation.isPresent()) {
          // replace current affiliation actor with already created one
          logger.debug("bind unknown actor " + affiliation.get().getActor().getFullname(lang) + " to unique instance");
          current.setActor(affiliation.get().getActor());
        }
      }
    }
  }

  /**
   * Update affiliation from given actor with given affiliation forms
   *
   * @param affiliations a list of affiliation forms
   * @param actor the actor to be updated
   */
  private void updateAffiliations(List<AffiliationForm> affiliations, Actor actor) throws PersistenceException {
    updateAffiliations(affiliations, actor, null, null);
  }

  /**
   * Update affiliation from given actor with given affiliation forms
   *
   * @param affiliations a list of affiliation forms
   * @param actor the actor to be updated
   * @param affiliationType the affiliation type
   */
  private void updateAffiliations(List<AffiliationForm> affiliations, Actor actor, EAffiliationType affiliationType) throws PersistenceException {
    updateAffiliations(affiliations, actor, affiliationType, null);
  }

  /**
   * Update affiliation from given actor with given affiliation forms
   *
   * @param affiliations a list of affiliation forms
   * @param actor the actor to be updated
   * @param affiliationType the affiliation type
   * @param professionType the profession type of the functions of the affiliations
   */
  private void updateAffiliations(List<AffiliationForm> affiliations, Actor actor, EAffiliationType affiliationType, EProfessionType professionType) throws PersistenceException {
    professionType = professionType == null ? EProfessionType.OTHERS : professionType;
    try {
      for (AffiliationForm a : affiliations.stream().filter(aff -> !aff.isEmpty()).collect(Collectors.toList())) {
        // lang may not be set, in case of unknown function / organization
        if (values.isBlank(a.getLang()) || "-1".equals(a.getLang())) {
          a.setLang(lang);
        }
        actor.addAffiliation(a.toAffiliation(affiliationType, professionType));
      }
    } catch (FormatException e) {
      logger.error("unable to bind affiliation to " + actor.getId(), e);
      throw new PersistenceException(PersistenceException.Key.SAVE_AFFILIATION, e);
    }
  }

  /**
   * Convert a list of actor name fields into API actor names
   *
   * @param nameFields a name field to convert
   * @return the API actor name object corresponding to given nameFields
   */
  private List<ActorName> toActorNames(List<ActorNameFields> nameFields) {
    List<ActorName> actorNames = new ArrayList<>();
    nameFields.stream().filter(nameField -> !nameField.isEmpty()).forEach(nameField -> {
      ActorName name = actorFactory.getActorName(nameField.getLang().trim());
      name.setLast(nameField.getLast().trim());
      if (!values.isBlank(nameField.getFirst())) {
        if (actortype == EActorType.ORGANIZATION.id()) {
          name.setFirst(nameField.getFirst().trim());
        } else {
          String first = nameField.getFirst().trim();
          name.setFirst(first.substring(0, 1).toUpperCase(new Locale(lang)) + first.substring(1));
        }
      }
      if (!values.isBlank(nameField.getPseudo())) {
        name.setPseudo(nameField.getPseudo().trim());
      }
      actorNames.add(name);
    });
    return actorNames;
  }
}
