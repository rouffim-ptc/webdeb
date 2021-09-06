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
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.ContributorPictureHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.i18n.Lang;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of an Actor (no IDs, but their description, as defined in the database).
 * Except by using a constructor, no value can be edited outside of this package or by subclassing.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ActorHolder extends ContributionHolder {

  protected int actortype = EActorType.UNKNOWN.id();
  protected EActorType eactortype = EActorType.UNKNOWN;

  /*
   * person attributes
   */
  protected ActorNameFields name;
  protected List<ActorNameFields> allnames = new ArrayList<>();
  protected String residence;
  protected String gender;
  protected String genderId;
  protected List<AffiliationHolder> affiliations;
  protected List<AffiliationHolder> qualifications = null;
  protected List<AffiliationHolder> filiations = null;
  protected List<AffiliationHolder> childfiliations = null;
  protected String birthdate;
  protected String deathdate;
  protected String crossref;
  protected ContributorPictureHolder avatar = null;

  /*
   * organization attributes
   */
  protected String officialNumber;
  protected ActorNameFields orgname;
  protected List<ActorNameFields> allorgnames = new ArrayList<>();
  protected List<ActorNameFields> oldorgnames = new ArrayList<>();
  protected List<String> businessSectors = new LinkedList<>();
  protected String legalStatus;
  protected String creationDate;
  protected String terminationDate;
  protected String headOffice;
  protected String orgcrossref;
  protected List<AffiliationHolder> orgaffiliations;
  protected ContributorPictureHolder orgavatar = null;

  // for lazy loading of affiliations
  protected Actor actor;

  /**
   * Play / JSON compliant constructor
   */
  public ActorHolder() {
    super();
    type = EContributionType.ACTOR;
  }

  /**
   * Construct an ActorHolder from a given Actor
   *
   * @param actor an Actor used to initialize this holder
   * @param lang 2-char ISO 639-1 code of context language (among play accepted languages)
   */
  public ActorHolder(Actor actor, WebdebUser user, String lang) {
    this(actor, user, lang, false);
  }

  /**
   * Construct an ActorHolder from a given Actor
   *
   * @param actor an Actor used to initialize this holder
   * @param lang 2-char ISO 639-1 code of context language (among play accepted languages)
   */
  public ActorHolder(Actor actor, WebdebUser user, String lang, boolean light) {
    super(actor, user, lang, light);

    actortype = actor.getActorType().id();
    eactortype = actor.getActorType();
    this.actor = actor;

    switch (EActorType.value(actortype)) {
      case PERSON:
        name = new ActorNameFields(actor.getName(lang));
        // we are not sure we'll get the name in given lang (i.e., such a lang may not exist)
        avatar = new ContributorPictureHolder(actor.getAvatar());
        crossref = actor.getCrossReference() != null ? actor.getCrossReference() : "";
        Person p = (Person) actor;
        birthdate = p.getBirthdate() != null ? p.getBirthdate() : "";
        deathdate = p.getDeathdate() != null ? p.getDeathdate() : "";

        if(!light) {
          allnames = actor.getNames().stream().filter(n -> !n.getLang().equals(name.getLang()))
                  .map(ActorNameFields::new).collect(Collectors.toList());
        }

        // remaining fields "view-dependent" (form or viz)
        init(p);
        break;

      case ORGANIZATION:
        orgname = new ActorNameFields(actor.getName(lang));
        orgavatar = new ContributorPictureHolder(actor.getAvatar());
        orgcrossref = actor.getCrossReference() != null ? actor.getCrossReference() : "";
        Organization o = (Organization) actor;
        officialNumber = o.getOfficialNumber() != null ? o.getOfficialNumber() : "";
        creationDate = o.getCreationDate() != null ? o.getCreationDate() : "";
        terminationDate = o.getTerminationDate() != null ? o.getTerminationDate() : "";

        if(!light) {
          // we are not sure we'll get the name in given lang (i.e., such a lang may not exist)
          allorgnames = actor.getNames().stream().filter(n -> !n.getLang().equals(orgname.getLang()))
                  .map(ActorNameFields::new).collect(Collectors.toList());
          oldorgnames = ((Organization) actor).getOldNames().stream()
                  .map(ActorNameFields::new).collect(Collectors.toList());
        }

        // remaining fields "view-dependent" (form or viz)
        init(o);
        break;

      default:
        name = new ActorNameFields(actor.getName(lang));
        orgname = new ActorNameFields(actor.getName(lang));
        // we are not sure we'll get the name in given lang (i.e., such a lang may not exist)
        if(!light) {
          allnames = actor.getNames().stream().filter(n -> !n.getLang().equals(name.getLang()))
                  .map(ActorNameFields::new).collect(Collectors.toList());
          allorgnames = actor.getNames().stream().filter(n -> !n.getLang().equals(name.getLang()))
                  .map(ActorNameFields::new).collect(Collectors.toList());
        }
        init();
        break;
    }

    if(avatar == null) {
      avatar = new ContributorPictureHolder();
    }

    if(orgavatar == null) {
      orgavatar = new ContributorPictureHolder();
    }
  }

  /**
   * Construct an ActorHolder with given affiliation. Given affiliation may not have its actor null.
   * Reuse {@link #ActorHolder(Actor, WebdebUser, String) actor-based constructor} but adds more filterable keys for affiliations and functions
   *
   * @param affiliation an affiliation with a non-null actor
   * @param lang 2-char ISO 639-1 code of context language (among play accepted languages)
   * @param affiliations the affiliations of this actor related to the given affiliation
   */
  public ActorHolder(Affiliation affiliation, WebdebUser user, String lang, List<Affiliation> affiliations) {
    this(affiliation.getActor(), user, lang);
    // add this actor as affiliated to itself (for affiliation filtering)

    switch (EActorType.value(actortype)) {
      case PERSON:
        this.affiliations = makeAffiliationsHolders(affiliations);
        break;
      case ORGANIZATION:
        orgaffiliations = makeAffiliationsHolders(affiliations);
        break;
      default:
        this.affiliations = makeAffiliationsHolders(affiliations);
        orgaffiliations = makeAffiliationsHolders(affiliations);
        break;
    }
  }

  /**
   * Initialize values for which labels are displayed (and ids are not used)
   *
   */
  protected void init() {
    affiliations = new ArrayList<>();
    orgaffiliations = new ArrayList<>();
  }

  /**
   * Initialize values for which labels are displayed (and ids are not used)
   *
   * @param p a person
   */
  protected void init(Person p) {
    gender = p.getGender() != null ? p.getGender().getName(lang) : "";
    genderId = p.getGender() != null ? p.getGender().getCode() : "";
    residence = p.getResidence() != null ? p.getResidence().getName(lang) : "";
    affiliations = new ArrayList<>();
  }

  /**
   * Initialize values for which labels are displayed (and ids are not used)
   *
   * @param o an organization
   */
  protected void init(Organization o) {

    if(!light) {
      legalStatus = o.getLegalStatus() != null ? o.getLegalStatus().getName(lang) : "";
      o.getBusinessSectors().stream().map(s -> s.getName(lang)).forEach(s -> {
        businessSectors.add(s);
      });
    }
    headOffice = !o.getPlaces().isEmpty() ? o.getPlaces().get(0).getName(lang) : "";
    orgaffiliations = new ArrayList<>();

    if(!light) {
      initPlaces(o.getPlaces(), lang);
      initTags(o.getTagsAsList(), lang);
    }
  }

  @Override
  public String getContributionDescription(){
    List<String> descriptions = new ArrayList<>();
    switch (actor.getActorType()){
      case PERSON:
        descriptions.add(residence);
        descriptions.add(birthdate);
        descriptions.add(gender);
        //descriptions.addAll(affiliations.stream().map(AffiliationHolder::getAffname).collect(Collectors.toList()));
        break;
      case ORGANIZATION:
      case PROJECT:
        descriptions.add(headOffice);
        descriptions.add(creationDate);
        //descriptions.addAll(orgaffiliations.stream().map(AffiliationHolder::getAffname).collect(Collectors.toList()));
        break;
      default:
    }

    return String.join(", ", descriptions);
  }

  @Override
  public MediaSharedData getMediaSharedData(){
    if(mediaSharedData == null){
      mediaSharedData = new MediaSharedData(getFullname(), "https://webdeb.be" + getSomeAvatar());
    }
    return mediaSharedData;
  }

  @Override
  public String getDefaultAvatar(){
    return getSomeAvatar();
  }

  @Override
  public String toString() {
    String result = "actor [" + id + "] ";
    switch (EActorType.value(actortype)) {
      case PERSON:
        return result + name + " with residence in " + residence;
      case ORGANIZATION:
        return result + orgname + " with legal status " + legalStatus + " active in sector " + businessSectors + "" +
                " with head office in " + headOffice;
      default:
        return result + orgname;
    }
  }

  /*
   * GETTERS
   */

  /**
   * Get this actor name in (persons)
   *
   * @return this actor's name as it should be displayed in this.lang
   */
  public ActorNameFields getName() {
    return name;
  }

  /**
   * Get the actor names in all known languages
   *
   * @return the actor names
   */
  public List<ActorNameFields> getAllnames() {
    return allnames;
  }

  /**
   * Get the actor full name as described in ActorName.getFullname
   *
   * @return this actor's full name
   * @see be.webdeb.core.api.actor.ActorName
   */
  public String getFullname() {
    // persons
    if (actortype == EActorType.PERSON.id()) {
      if (values.isBlank(name.getFirst()) && !values.isBlank(name.getLast())) {
        return name.getLast();
      }else if (!values.isBlank(name.getLast())) {
        return name.getFirst() + " " + name.getLast()
                + (!values.isBlank(name.getPseudo()) ? " (" + name.getPseudo() + ")" : "");
      }
      // pseudo only
      return name.getPseudo();
    }else if (actortype == EActorType.ORGANIZATION.id()) {
      return (orgname.getFirst() != null ? orgname.getFirst() + " - " : "") + orgname.getLast();
    }

    // unknown
    return name.getLast();
  }

  /**
   * Get the actor actor firstname if any, lastname otherwise
   *
   * @return this actor's name
   * @see be.webdeb.core.api.actor.ActorName
   */
  public String getFirstNameOrLast() {
    // persons
    if (actortype == EActorType.ORGANIZATION.id()) {
      return orgname.getFirst() != null ? orgname.getFirst() : orgname.getLast();
    }

    // others
    return getFullname();
  }

  /**
   * Get the country of residence of this actor (depending on the language set for this holder)
   *
   * @return a name of a country (language dependant on user interface)
   */
  public String getResidence() {
    return residence;
  }

  /**
   * Get this actor's gender, if he a person
   *
   * @return this actor's gender (language dependant), empty string if unknown or this actor is not a person
   */
  public String getGender() {
    return gender;
  }

  /**
   * Get this actor's gender id, if he a person
   *
   * @return this actor's gender id, empty string if unknown or this actor is not a person
   */
  public String getGenderId() {
    return genderId;
  }

  /**
   * Get this actor's gender as EGenderType, if he a person
   *
   * @return this actor's gender id as EGenderType
   */
  public EGenderType getEGenderType() {
    return EGenderType.genderStringAsType(genderId);
  }

  /**
   * Get the list of affiliations for this actor (person)
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getAffiliations() {
    return affiliations;
  }

  /**
   * Get the list of qualifications for this actor (person)
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getQualifications() {
    if(qualifications == null){
      qualifications = helper.toAffiliationsHolders(actor.getAffiliations(EAffiliationType.GRADUATING_FROM), user, lang);
    }
    return qualifications;
  }

  /**
   * Get the list of filiations for this actor (person)
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getFiliations() {
    if(filiations == null){
      filiations = helper.toAffiliationsHolders(actor.getAffiliations(EAffiliationType.SON_OF), user, lang);
    }
    return filiations;
  }

  /**
   * Get the list of filiations (as children) for this actor (person)
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getFiliationsChildren() {
    if(childfiliations == null){
      childfiliations = helper.toAffiliationsHolders(actor.getActorsAffiliated(EAffiliationType.SON_OF), user, lang);
    }
    return childfiliations;
  }

  /**
   * Get the list of affiliations for this actor (person) with max elems
   *
   * @param maxSize max element in the list
   * @return a (possibly empty) list of affiliations
   */
  public List<AffiliationHolder> getAffiliations(int maxSize) {
    return affiliations.stream().limit(maxSize).collect(Collectors.toList());
  }

  /**
   * Get this actor's date of birth if he's a person. Otherwise it's creation date if it's an organization
   *
   * @return a DD/MM/YYYY (D and M optional) date, an empty string if unset, or this actor is not a person and not an organization
   */
  public String getBeginDate(){
    return actortype == EActorType.PERSON.id() ? getBirthdate() : actortype == EActorType.ORGANIZATION.id() ? getCreationDate() : null;
  }

  /**
   * Get this actor's date of death if he's a person. Otherwise it's termination date if it's an organization
   *
   * @return a DD/MM/YYYY (D and M optional) date, an empty string if unset, or this actor is not a person and not an organization
   */
  public String getEndDate(){
    return actortype == EActorType.PERSON.id() ? getDeathdate() : actortype == EActorType.ORGANIZATION.id() ? getTerminationDate() : null;
  }

  /**
   * Get this actor's date of birth, if he's a person
   *
   * @return a DD/MM/YYYY (D and M optional) date, an empty string if unset, or this actor is not a person
   */
  public String getBirthdate() {
    return birthdate;
  }

  /**
   * Get this actor's date of death, if he's a person
   *
   * @return a DD/MM/YYYY (D and M optional) date, an empty string if unset, or this actor is not a person
   */
  public String getDeathdate() {
    return deathdate;
  }

  /**
   * Get an external url for this actor (person)
   *
   * @return an url, or an empty string if unset
   */
  public String getCrossref() {
    return crossref;
  }

  /**
   * Get an external url for this actor (person) named
   *
   * @return an url, or an empty string if unset
   */
  public String getCrossrefNamed() {
    return values.checkIfUrlIsWikipedia(crossref) ?
            i18n.get(Lang.forCode(lang), "viz.actor.url.wiki") : i18n.get(Lang.forCode(lang), "viz.actor.url.personal");
  }

  /**
   * Get this organization official number
   *
   * @return a unique official identifier
   */
  public String getOfficialNumber() {
    return officialNumber;
  }

  /**
   * Get the actor's name (organization) in this.lang (or in another default language)
   *
   * @return the organization's name
   */
  public ActorNameFields getOrgname() {
    return orgname;
  }

  /**
   * Get all this organizational actor's names in all known languages excpet the displayed one (ie this.lang)
   *
   * @return this organization list of names
   */
  public List<ActorNameFields> getAllorgnames() {
    return allorgnames;
  }

  /**
   * Get all this organizational actor's previous names in all known languages
   *
   * @return this organization list of previous names
   */
  public List<ActorNameFields> getOldorgnames() {
    return oldorgnames;
  }

  /**
   * Get all this organizational actor's previous names in the user lang
   *
   * @param limit the number of old org names
   * @return this organization list of previous names
   */
  public List<String> getOldorgnames(int limit) {
    return oldorgnames.stream().limit(limit).map(e -> (e.getFirst() != null ? e.getFirst() + " - " : "") + e.getLast()).collect(Collectors.toList());
  }

  /**
   * Get all given names list as string list
   *
   * @return the names list as string list
   */
  public List<String> getActorNameAsStringList(List<ActorNameFields> list){
    return list.stream().map(ActorNameFields::getLast).collect(Collectors.toList());
  }

  /**
   * Get the list of business sectors of this organization
   *
   * @return a (possibly empty) list of business sectors
   */
  public List<String> getBusinessSectors() {
    return businessSectors;
  }

  /**
   * Get the legal status of this organization (language dépendent)
   *
   * @return this organisation's legal status, empty string if unset
   */
  public String getLegalStatus() {
    return legalStatus;
  }

  /**
   * Get the DD/MM/YYYY (DD/MM optional)-formatted creation date
   *
   * @return the creation date of this organization, an empty string if none set
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Get the DD/MM/YYYY (DD/MM optional)-formatted dissolution date
   *
   * @return the dissolution date of this organization, an empty string if none set
   */
  public String getTerminationDate() {
    return terminationDate;
  }

  /**
   * Get the country where this organization's head office is located (depending on the language set for this holder)
   *
   * @return a names of a country (language dependant on user interface)
   */
  public String getHeadOffice() {
    return headOffice;
  }

  /**
   * Get an external url for this actor (organization)
   *
   * @return an url, or an empty string if unset
   */
  public String getOrgcrossref() {
    return orgcrossref;
  }

  /**
   * Get an external url for this actor (person) named
   *
   * @return an url, or an empty string if unset
   */
  public String getOrgcrossrefNamed() {
    return values.checkIfUrlIsWikipedia(orgcrossref) ?
            i18n.get(Lang.forCode(lang), "viz.actor.url.wiki") : i18n.get(Lang.forCode(lang), "viz.actor.url.personal");
  }

  /**
   * Get the list of affiliations for this actor (organization or unknown)
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getOrgaffiliations() {
    return orgaffiliations;
  }

  /**
   * Get the EActorType id of this actor (0 for person, 1 for organization, -1 for unknown)
   *
   * @return the EActorType id
   */
  public int getActortype() {
    return actortype;
  }

  /**
   * Get the EActorType of this actor
   *
   * @return the EActorType
   */
  public EActorType getEActortype() {
    return eactortype;
  }

  /**
   * Get the avatar of the person as picture holder
   *
   * @return a contributor picture holder
   */
  public ContributorPictureHolder getAvatar() {
    return avatar;
  }

  /**
   * Get the avatar of the organization as picture holder
   *
   * @return a contributor picture holder
   */
  public ContributorPictureHolder getOrgavatar() {
    return orgavatar;
  }

  /**
   * Helper method to get the avatar filename, regardless the actortype. If no avatar exists,
   * return a default file name
   *
   * @return a file name
   */
  public String getSomeAvatar() {
    // if we have an avatar, return it
    if (avatar != null && avatar.isDefined()) {
      return avatar.getFilename();
    }
    if (orgavatar != null && orgavatar.isDefined()) {
      return orgavatar.getFilename();
    }

    // otherwise, return default pics
    return helper.computeAvatar(actortype, genderId);
  }

  /**
   * Check whether this actor has an avatar
   *
   * @return true if this actor has a default avatar, ie has no personal avatar
   */
  public boolean hasDefaultAvatar() {
    return (avatar == null || !avatar.isDefined()) && (orgavatar == null || !orgavatar.isDefined());
  }

  /**
   * Get affiliations if this actor come frome an affiliation of this actor and update this filterable items
   *
   * @return the (possibly empty) list of affiliations
   */
  private List<AffiliationHolder> makeAffiliationsHolders(List<Affiliation> affiliations) {
    if(affiliations == null){
      return new ArrayList<>();
    }
    return helper.toAffiliationsHolders(affiliations, user, lang);
  }
}
