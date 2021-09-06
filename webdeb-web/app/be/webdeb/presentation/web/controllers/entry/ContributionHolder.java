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

package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.actor.AffiliationForm;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentHolder;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.tag.ContextHasCategoryLinkForm;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.libs.Json;

import javax.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used as a supertype wrapper for any contribution holder
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class ContributionHolder implements Comparable<ContributionHolder> {

  @Inject
  protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);
  @Inject
  protected FileSystem files = Play.current().injector().instanceOf(FileSystem.class);
  @Inject
  protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);
  @Inject
  protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);
  @Inject
  protected MessagesApi i18n = Play.current().injector().instanceOf(MessagesApi.class);


  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  // common fields
  protected Long id;
  // only set for "new elements" not already persisted at controller's level
  protected Integer inGroup;
  protected WebdebUser user;
  protected boolean light;
  protected List<Integer> groups;
  protected Contribution contribution;
  protected long version;
  protected EContributionType type;
  protected String lang;
  protected EValidationState validated;
  protected List<SimpleTagForm> tags = new ArrayList<>();
  protected List<PlaceForm> places = new ArrayList<>();
  protected Integer nbContributions = -1;

  protected int stepNum = -1;

  // used for some group-based visualization
  protected Long creator;
  protected String creatorName;

  private Date latestContributorDate = null;
  private String latestContributorName = null;

  protected boolean indecision = false;
  protected Map<EContributionType, Integer> countRelationsMap = null;

  protected MediaSharedData mediaSharedData = null;

  // for step form
  protected Boolean hasPlaces = null;
  protected Boolean hasTags = null;

  /**
   * Default constructor, simply initialize id, version and group resp. to -1, 0 and 0 (default public group).
   */
  public ContributionHolder() {
    id = -1L;
    version = 0L;
    inGroup = 0;
  }


  /**
   * Constructor from a given contribution
   *
   * @param contribution a contribution
   * @param user a webdeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ContributionHolder(Contribution contribution, WebdebUser user, String lang) {
    this(contribution, user, lang, false);
  }
  /**
   * Constructor from a given contribution
   *
   * @param contribution a contribution
   * @param user a webdeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ContributionHolder(Contribution contribution, WebdebUser user, String lang, boolean light) {
    this.contribution = contribution;

    this.user = user;
    this.lang = lang;
    this.light = light;

    if(contribution != null) {
      id = contribution.getId();
      version = contribution.getVersion();
      type = contribution.getType();
      validated = contribution.getValidated().getEType();
      nbContributions = contribution.getNbContributions();

      if (!light) {
        groups = contribution.getInGroups().stream().map(Group::getGroupId).collect(Collectors.toList());
        if (!values.isBlank(id)) {
          creator = contribution.getCreator().getId();
          creatorName = contribution.getCreator().getPseudo();

          latestContributorDate = contribution.getLatestContributor().getVersion();
          latestContributorName = contribution.getLatestContributor().getContributor().getPseudo();
        }
      }
    }
  }

  public Map<String, List<ValidationError>> validate() {
    return new HashMap<>();
  }

  protected Map<String, List<ValidationError>> returnErrorsMap(Map<String, List<ValidationError>> errorsMap) {
    // must return null if errors is empty
    return errorsMap.isEmpty() ? null : errorsMap;
  }

  protected Map<String, List<ValidationError>> validationListToMap(List<ValidationError> errorsList) {
    if (errorsList != null) {
      Map<String, List<ValidationError>> errorsMap = new HashMap<>();
      errorsList.forEach(e -> errorsMap.put(e.key(), Collections.singletonList(new ValidationError(e.key(), e.message()))));
      return errorsMap;
    }
    return null;
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get this contribution id
   *
   * @return an id
   */
  public Long getId() {
    return id;
  }

  /**
   * Set this contribution id
   *
   * @param id an id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the list of group ids where this contribution is visible
   *
   * @return a list of group id
   */
  public List<Integer> getGroups() {
    return groups;
  }

  /**
   * Get this contribution version
   *
   * @return a version (timestamp)
   */
  public long getVersion() {
    return version;
  }

  /**
   * Set the version (timestamp) of this contribution
   *
   * @param version a timestamp value
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Get the concrete type of contribution
   *
   * @return the EContributionType of this contribution
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public EContributionType getType() {
    return type;
  }

  /**
   * Get the concrete type of contribution
   *
   * @return the EContributionType of this contribution
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public EContributionType getPreciseType() {
    return this instanceof ActorHolder && ((ActorHolder) this).getEActortype() == EActorType.PERSON ? EContributionType.ACTOR_PERSON : type;
  }

  /**
   * Get the concrete type of contribution
   *
   * @return the EContributionType of this contribution
   */
  @JsonIgnore
  public int getTypeNum() {
    return type.id();
  }

  /**
   * Set the concrete type of contribution
   *
   * @param type the EContributionType of this contribution
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public void setType(EContributionType type) {
    this.type = type;
  }

  /**
   * Get the group (visibility) of this contribution
   *
   * @return a group id in which this contribution is visible (-1 denotes publicly visible contributions)
   */
  public Integer getInGroup() {
    return inGroup;
  }

  /**
   * Set the group (visibility) of this contribution
   *
   * @param group a group id in which this contribution is visible (-1 denotes publicly visible contributions)
   */
  public void setInGroup(Integer group) {
    this.inGroup = group;
  }

  /**
   * Get the id of the contributor that created this contribution, may be unset (for performance reason)
   *
   * @return the creator id, or null of unset
   */
  public Long getCreator() {
    return creator;
  }

  /**
   * Set the id of the contributor that created this contribution
   *
   * @param creator the id of the contributor that created this contribution
   */
  public void setCreator(Long creator) {
    this.creator = creator;
  }

  /**
   * Get the name of the contributor that created this contribution, may be unset (for performance reason)
   *
   * @return the creator name, or null of unset
   */
  public String getCreatorName() {
    return creatorName;
  }

  /**
   * Get the date of the latest contributor's action with this contribution, may be unset (for performance reason)
   *
   * @return the latest contributor contribution date, or null of unset
   */
  public Date getLatestContributorDate() {
    return latestContributorDate;
  }

  /**
   * Get the name of the latest contributor of this contribution, may be unset (for performance reason)
   *
   * @return the latest contributor name, or null of unset
   */
  public String getLatestContributorName() {
    return latestContributorName;
  }

  /**
   * Set the name of the contributor that created this contribution
   *
   * @param creatorName the name of the contributor that created this contribution
   */
  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  /**
   * Check whether this contribution is validated
   *
   * @return the validation stateof this contribution
   */
  public EValidationState isValidated() {
    return validated;
  }

  /**
   * Set whether this contribution is validated
   *
   * @param validated the validation state
   */
  public void setValidated(EValidationState validated) {
    this.validated = validated;
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

  /**
   * Get the list of tags linked with this contribution
   *
   * @return a list of tags
   */
  public List<SimpleTagForm> getTags() {
    return tags;
  }

  @JsonIgnore
  public Boolean getHasTags() {
    return hasTags;
  }

  public void setHasTags(Boolean hasTags) {
    this.hasTags = hasTags;
  }

  /**
   * Get the places concerned by this contribution
   *
   * @return a possibly empty list of places
   */
  public List<PlaceForm> getPlaces() {
    return places;
  }

  /**
   * Get the places concerned by this contribution as json
   *
   * @return a possibly empty list of places as json
   */
  @JsonIgnore
  public String getPlacesAsJson() {
    return Json.toJson(places).toString();
  }

  @JsonIgnore
  public Boolean getHasPlaces() {
    return hasPlaces;
  }

  public void setHasPlaces(Boolean hasPlaces) {
    this.hasPlaces = hasPlaces;
  }

  /**
   * True if an actor has ambiguous position about a contribution in a context
   *
   * @return the ambiguous flag
   */
  public boolean isIndecision() {
    return indecision;
  }

  /**
   * True if an actor has ambiguous position about a contribution in a context
   *
   * @param indecision the ambiguous flag
   */
  public void setIndecision(boolean indecision) {
    this.indecision = indecision;
  }

  public int getStepNum() {
    return stepNum;
  }

  public void setStepNum(int stepNum) {
    this.stepNum = stepNum;
  }

  /**
   * Get an image that represent this contribution
   *
   * @return the path for the image
   */
  public abstract String getDefaultAvatar();

  /**
   * Get the map of number of linked elements with this contribution
   *
   * @return the map of number of linked elements with this contribution by contribution type
   */
  @JsonIgnore
  public synchronized Map<EContributionType, Integer> getCountRelationsMap() {
    if(countRelationsMap == null){
      countRelationsMap = user != null ?
              contribution.getCountRelationsMap(user.getId(), user.getGroupId()) : new HashMap<>();
    }
    return countRelationsMap;
  }

  /**
   * Get the related elements for the given contribution type
   *
   * @param type the type of contribution to show the count
   * @return the number of related elements
   */
  public synchronized int getNbRelatedElements(EContributionType type) {

    getCountRelationsMap();

    if(countRelationsMap.containsKey(type)){
      return countRelationsMap.get(type);
    }
    return 0;
  }

  @JsonIgnore
  public WebdebUser getUser() {
    return user;
  }

  @JsonIgnore
  public void setUser(WebdebUser user) {
    this.user = user;
  }

  public int getNbContributions() {
    return nbContributions != null ? nbContributions : 0;
  }

  /**
   * Get a string to describe all linked actor to this contribution.
   *
   * @param actors the list of actors to describe
   * @param withLink true if tag must be linked to its viz page
   * @return a description of actors linked to this contribution
   */
  public String getActorDescription(List<ActorSimpleForm> actors, boolean withLink){
    return getActorDescription(actors, withLink, 0, 0);
  }
  /**
   * Get a string to describe all linked actor to this contribution.
   *
   * @param actors the list of actors to describe
   * @param withLink true if tag must be linked to its viz page
   * @return a description of actors linked to this contribution
   */
  public String getActorDescription(List<ActorSimpleForm> actors, boolean withLink, int pane, int pov){
    List<String> actorsName = new ArrayList<>();
    for(ActorSimpleForm actor : actors){
      actorsName.add(withLink ? writeContributionHtmlLink(actor.getId(), EContributionType.ACTOR, actor.getFullname(), pane, pov) : actor.getFullname());
    }
    return String.join(", ", actorsName);
  }

  /**
   * Get a string to describe all linked tags to this contribution. It will return the name of the
   * composed tag that have the most parents, or if this one has no composed tag, the name of the first tag
   * found.
   *
   * @param withLink true if tag must be linked to its viz page
   * @return a description of the tags linked to this contextualized argument
   */
  public String getTagDescription(boolean withLink){
    Optional<SimpleTagForm> optionalTag = tags.stream().max((Comparator.comparing(SimpleTagForm::getNbParents)));

    if(optionalTag.isPresent()){
      SimpleTagForm tag = optionalTag.get();
      return withLink ? writeContributionHtmlLink(tag.getTagId(), EContributionType.TAG, tag.getName()) : tag.getName();
    }

    return "";
  }

  /**
   * Initialize tags linked with this contribution
   *
   * @param tags a list of tags
   * @param lang two-char ISO code used for function names
   */
  protected void initTags(List<Tag> tags, String lang) {
    tags.forEach(e -> {
      this.tags.add(new SimpleTagForm(e, lang));
    });
    hasTags = !this.tags.isEmpty();
  }

  /**
   * Get a string to describe all places linked to this .
   *
   * @return a description of the places linked to this contribution
   */
  @JsonIgnore
  public String getPlaceDescription(){
    return getPlaceDescription(false);
  }

  /**
   * Get a string to describe all places linked to this. All places will have a redirection link to viz them.
   *
   * @return a description of the places linked to this contribution
   */
  public String getPlaceDescription(boolean withLink){
    if(places != null){
      return places.stream().map(e ->  withLink ? writePlaceHtmlLink(e.getId(), e.getName()) : e.getName())
              .collect(Collectors.joining(" / "));
    }
    return "";
  }


  /**
   * Initialize places linked with this contribution
   *
   * @param places a list of places
   * @param lang two-char ISO code used for function names
   */
  protected void initPlaces(List<Place> places, String lang) {
    this.places = new ArrayList<>();
    places.forEach(p -> {
      this.places.add(new PlaceForm(p, lang));
    });

    if(this.places.size() == 1 && this.places.get(0).getId() == 0) {
      this.places = new ArrayList<>();
    }

    hasPlaces = !this.places.isEmpty();
  }

  /**
   * Check a list of tags
   *
   * @param tags the list of tags to check
   * @param field the field name in which the tags are present (for proper error binding)
   * @return a map of field name - list of validation errors if any, null otherwise
   */
  protected Map<String, List<ValidationError>> checkTags(List<SimpleTagForm> tags, String field) {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for (int i = 0; i < tags.size(); i++){
      if(!values.isBlank(tags.get(i).getName())){
        String fieldName = field + "[" + i + "].name";
        /*if(values.isNotValidTagName(tags.get(i).getName())) {
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.name.chars")));
        }
        if(values.checkTagNameSize(tags.get(i).getName())){
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.name.size")));
        */
        if(tags.get(i).getTagId().equals(-1L) && helper.checkTagName(tags.get(i).getName(), lang)) {
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.begin")));
        }
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  /**
   * Check a list of places
   *
   * @param places the list of places to check
   * @return a map of field name - list of validation errors if any, null otherwise
   */
  protected Map<String, List<ValidationError>> checkPlaces(List<PlaceForm> places) {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for (int i = 0; i < places.size(); i++) {
      PlaceForm place = places.get(i);
      if (!place.isEmpty() &&
              places.stream().filter(p -> !p.isEmpty()).anyMatch(p -> (p != place &&
                      (p.getGeonameId() == null ? p.getId() != null && p.getId().equals(place.getId())
                              : p.getGeonameId().equals(place.getGeonameId()))))) {
        errors.put("places[" + i + "].placename", Collections.singletonList(new ValidationError("places[" + i + "].placename", "argument.error.placename.twice")));
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  /**
   * Check a list of affiliation objects and return a map of (key, list of validation errors)
   *
   * @param fullname the name of the contributor/actor that will hold those affiliations
   * @param birthdate the holder's date of birth or creation date in (DD/MM/)YYYY format (may be null)
   * @param deathdate the holder's date of death or termination date in (DD/MM/)YYYY format (may be null)
   * @param person true if the holder of this affiliation is a person, false otherwise
   * @param affiliations the affiliations to check
   * @param field the field name in which the affiliations are present (for proper error binding)
   * @return a map of field name - list of validation errors if any, null otherwise
   */
  protected Map<String, List<ValidationError>> checkAffiliations(String fullname, String birthdate, String deathdate, boolean person,
                                                                 List<AffiliationForm> affiliations, String field) {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    // if we have an id, must check that dumb user didn't change it and is trying to affiliate this actor to previous name
    String oldname = fullname;
    if (!values.isBlank(id)) {
      Actor actor = actorFactory.retrieve(id);
      oldname = actor != null ? actor.getFullname(lang) : oldname;
    }
    for (int i = 0; i < affiliations.size(); i++){
      AffiliationForm current = affiliations.get(i);
      Map<String, String> afferrors = current.checkAffiliation();
      Actor affiliationActor = (!values.isBlank(current.getAffid()) ? actorFactory.retrieve(current.getAffid()) : null);

      for (Map.Entry<String, String> entry : afferrors.entrySet()) {
        String fieldname = field + "[" + i + "]." + entry.getKey();
        errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, entry.getValue())));
      }

      // changed name and put old name as affiliation (avoid cyclic ref)
      if (oldname.equals(affiliations.get(i).getAffname())) {
        String fieldname = field + "[" + i + "].affname";
        errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.oldself")));
      }

      // same id (except -1L) or same name
      if (!values.isBlank(current.getId()) && id.equals(current.getId())
          || !values.isBlank(current.getAffname()) && oldname.equals(current.getAffname())) {
        String fieldname = field + "[" + i + "].affname";
        errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.notself")));
      }

      // dates may not be prior birth and later than death
      if (!values.isBlank(current.getStartDate()) || !values.isBlank(current.getEndDate())) {

        if (!values.isBlank(birthdate) && !values.isBlank(current.getStartDate()) && !values.isBefore(birthdate, current.getStartDate())) {
          String fieldname = field + "[" + i + "].startDate";
          errors.put(fieldname, Collections.singletonList(
              new ValidationError(fieldname, "affiliation.error." + (person ? "person" : "org") + ".start")));
        }
        if (!values.isBlank(deathdate) && !values.isBlank(current.getEndDate()) && !values.isBefore(current.getEndDate(), deathdate)) {
          String fieldname = field + "[" + i + "].endDate";
          errors.put(fieldname, Collections.singletonList(
              new ValidationError(fieldname, "affiliation.error." + (person ? "person" : "org") + ".end")));
        }
        if (!values.isBlank(current.getStartDate()) && values.isBlank(current.getStartDateType())) {
          String fieldname = field + "[" + i + "].startDateType";
          errors.put(fieldname, Collections.singletonList(
                  new ValidationError(fieldname, "affiliation.error.datetype")));
        }
        if (!values.isBlank(current.getEndDate()) && values.isBlank(current.getEndDateType())) {
          String fieldname = field + "[" + i + "].endDateType";
          errors.put(fieldname, Collections.singletonList(
                  new ValidationError(fieldname, "affiliation.error.datetype")));
        }

        // affiliation dates are not included in affiliation birth-creation/death-termination dates
        if (!values.isBlank(current.getAffid())) {
          // because of previous call to checkAffiliation, we know that if we have an id, we have an affiliation actor
          switch (affiliationActor.getActorType()) {
            case PERSON:
              Person p = (Person) affiliationActor;
              // in case of any date is null or empty, isBefore returns true
              if (!values.isBlank(current.getStartDate()) && !values.isBefore(p.getBirthdate(), current.getStartDate())) {
                String fieldname = field + "[" + i + "].startDate";
                errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.before.born.start")));
              }
              if (!values.isBlank(current.getEndDate()) && !values.isBefore(current.getEndDate(), p.getDeathdate())) {
                String fieldname = field + "[" + i + "].endDate";
                errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.after.death.end")));
              }
              break;

            case ORGANIZATION:
              Organization o = (Organization) affiliationActor;
              // in case of any date is null or empty, isBefore returns true
              if (!values.isBlank(current.getStartDate()) && !values.isBefore(o.getCreationDate(), current.getStartDate())) {
                logger.debug(current.getStartDate() + " " + o.getCreationDate());
                String fieldname = field + "[" + i + "].startDate";
                errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.before.creation.start")));
              }
              if (!values.isBlank(current.getEndDate()) && !values.isBefore(current.getEndDate(), o.getTerminationDate())) {
                logger.debug(current.getEndDate() + " " + o.getTerminationDate());
                String fieldname = field + "[" + i + "].endDate";
                errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.after.termination.end")));
              }
              break;

            default:
              // ignore, nothing to do
          }
        }
      }

      // check type of affiliation if holder is not a person
      if (!person) {
        String fieldname = field + "[" + i + "].afftype";
        if (!values.isBlank(current.getAfftype())) {
          // valid value
          if (!values.isNumeric(current.getAfftype())) {
            errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.type.unset")));
          } else {
            EAffiliationType parsedType = EAffiliationType.value(Integer.parseInt(current.getAfftype()));
            if (parsedType == null) {
              errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.type.wrong")));
            }
          }
        } else {
          errors.put(fieldname, Collections.singletonList(new ValidationError(fieldname, "affiliation.error.type.unset")));
        }
      }
    }
    return errors.isEmpty() ? null : errors;
  }

  /**
   * Redefine equals at this level since all contributions may have an id. Hashcode method must be overridden
   * in concrete classes
   *
   * @param obj an object to compare to this
   * @return true if given object is considered as equal to this object
   */
  @Override
  public boolean equals(Object obj) {

    if (obj == null || !(obj instanceof ContributionHolder)) {
      return false;
    }

    ContributionHolder c = (ContributionHolder) obj;
    if (!id.equals(-1L) && !c.getId().equals(-1L)) {
      return id.equals(c.getId());
    }

    return hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 59 * Long.hashCode(id) * Long.hashCode(version);
  }

  @Override
  public int compareTo(ContributionHolder o) {
    String name = getNameToCompare(this);
    String otherName = getNameToCompare(o);

    if (name == null) {
      return 1;
    }

    if (otherName == null) {
      return -1;
    }

    return name.compareToIgnoreCase(otherName);
  }

  /**
   * Get a comparable name for given contribution holder (depending on concrete implementation
   *
   * @param holder a contribution holder
   * @return a name, depending on the concrete type
   */
  protected String getNameToCompare(ContributionHolder holder) {
    switch (holder.getType()) {
      case ACTOR:
        return ((ActorHolder) holder).getFullname();
      case DEBATE:
        return ((DebateHolder) holder).getFullTitle();
      case ARGUMENT:
        return ((ArgumentHolder) holder).getFullTitle();
      case CITATION:
        return ((CitationHolder) holder).getOriginalExcerpt();
      case TEXT:
        return  ((TextHolder) holder).getTitle();
      default:
        logger.warn("wrong type passed as holder or should not be used " + holder.getId() + " with type " + holder.getType());
        return null;
    }
  }

  /**
   * Create Place with place form
   *
   * @param place the place form
   * @return the created place
   */
  protected Place createPlaceFromForm(PlaceForm place){
    Place p = null;
    if(place != null) {
      Long placeId = actorFactory.retrievePlaceByGeonameIdOrPlaceId(place.getGeonameId(), place.getId());
      if (placeId == null) {
        PlaceType t = actorFactory.findPlaceTypeByCode(place.getPlaceType());
        if (t != null) {
          Map<String, String> names = new HashMap<>();
          // Add names from form
          place.getNames().forEach(n -> names.put(n.getLang(), n.getName()));
          // Create the place
          p = actorFactory.createPlace(place.getId(), place.getGeonameId(),
              place.getCode(), place.getLatitude(), place.getLongitude(), names);
          p.setPlaceType(t);
          p.setContinent(createPlaceFromForm(place.getContinent()));
          p.setCountry(createPlaceFromForm(place.getCountry()));
          p.setRegion(createPlaceFromForm(place.getRegion()));
          p.setSubregion(createPlaceFromForm(place.getSubregion()));
        }
      } else {
        p = actorFactory.createSimplePlace(placeId);
      }
    }
    return p;
  }

  @JsonIgnore
  public String getContributionTitle(){
    if(type != null) {
      switch (type) {
        case ACTOR:
          return this instanceof ActorHolder ? ((ActorHolder) this).getFullname() : "";
        case TEXT:
          return this instanceof TextHolder ? ((TextHolder) this).getTitle() : "";
        case DEBATE:
          return this instanceof DebateHolder ? ((DebateHolder) this).getFullTitle() : "";
        case CITATION:
          return this instanceof CitationHolder ? ((CitationHolder) this).getWorkingExcerpt() : "";
        case TAG:
          return this instanceof TagHolder ? values.firstLetterUpper(((TagHolder) this).getTagName()) : "";
        case ARGUMENT:
          return this instanceof ArgumentHolder ? ((ArgumentHolder) this).getFullTitle() : "";
        default:
          return "";
      }
    }

    return "";
  }

  @JsonIgnore
  public String getContributionMediaTitle(){
    return getContributionMediaMessage(true, false);
  }

  @JsonIgnore
  public String getContributionMediaTitleOG(){
    return getContributionMediaMessage(false, false);
  }

  @JsonIgnore
  public String getContributionMediaDescription(){
    return getContributionMediaMessage(true, true);
  }

  @JsonIgnore
  public String getContributionMediaDescriptionOG(){
    return getContributionMediaMessage(false, true);
  }

  @JsonIgnore
  public String getContributionMediaMessage(boolean forTitle, boolean isDescription){
    if(type != null) {
      Lang language = Lang.forCode(lang);

      switch (type) {
        case ACTOR:
          if(this instanceof ActorHolder){
            ActorHolder actorHolder = ((ActorHolder) this);

            switch (actorHolder.getEActortype()) {
              case PERSON:
                return isDescription ?
                        forTitle ?
                          i18n.get(language, "general.metatitle.title.person.desc", actorHolder.getFullname()) :
                          i18n.get(language, "general.metatitle.og.person.desc") :
                        forTitle ?
                          i18n.get(language, "general.metatitle.title.person.title", actorHolder.getFullname()) :
                          i18n.get(language, "general.metatitle.og.person.title", actorHolder.getFullname());
              case ORGANIZATION:
                return isDescription ?
                        forTitle ?
                          i18n.get(language, "general.metatitle.title.org.desc", actorHolder.getFullname()) :
                          i18n.get(language, "general.metatitle.og.org.desc") :
                        forTitle ?
                          i18n.get(language, "general.metatitle.title.org.title", actorHolder.getFirstNameOrLast()) :
                          i18n.get(language, "general.metatitle.og.org.title", actorHolder.getFullname());
              default:
                return actorHolder.getFullname();
            }
          }
          break;
        case TEXT:
          if(this instanceof TextHolder) {
            TextHolder textHolder = ((TextHolder) this);

            return isDescription ?
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.text.desc") :
                      i18n.get(language, "general.metatitle.og.text.desc") :
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.text.title", textHolder.getTitle()) :
                      i18n.get(language, "general.metatitle.og.text.title", textHolder.getTitle());
          }
        case DEBATE:
          if(this instanceof DebateHolder) {
            DebateHolder debateHolder = ((DebateHolder) this);

            return isDescription ?
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.debate.desc", debateHolder.getFullTitle()) :
                      i18n.get(language, "general.metatitle.og.debate.desc", debateHolder.getDescription()) :
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.debate.title", debateHolder.getTags().get(0).getCompleteName()) :
                      i18n.get(language, "general.metatitle.og.debate.title", debateHolder.getFullTitle());
          }
        case CITATION:
          if(this instanceof CitationHolder) {
            CitationHolder citationHolder = ((CitationHolder) this);

            return citationHolder.getWorkingExcerpt();
          }
        case TAG:
          if(this instanceof TagHolder) {
            TagHolder tagHolder = ((TagHolder) this);

            return isDescription ?
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.tag.desc") :
                      i18n.get(language, "general.metatitle.og.tag.desc") :
                    forTitle ?
                      i18n.get(language, "general.metatitle.title.tag.title", tagHolder.getCompleteName()) :
                      i18n.get(language, "general.metatitle.og.tag.title", tagHolder.getCompleteName());
          }
        default:
          return "";
      }
    }

    return "";
  }

  @JsonIgnore
  public String getContributionMediaTitleForUrl(){
    return encodeValue(getContributionMediaTitle());
  }

  private String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      return "";
    }
  }

    /*
   * PRIVATE HELPER
   */

  /**
   * Get the data about this contribution for media sharing
   *
   * @return the media shared data object
   */
  @JsonIgnore
  abstract public MediaSharedData getMediaSharedData();

  /**
   * Get the description of the contribution as string
   *
   * @return the string contribution description
   */
  @JsonIgnore
  abstract public String getContributionDescription();

  /**
   * Inner class that represents data needed for social media sharing
   */
  public class MediaSharedData {
    private String title;
    private String description;
    private String image;

    public MediaSharedData(String title, String image){
      this.title = title;
      this.description = getContributionDescription();
      this.image = (image.startsWith("/") ? image : "https://webdeb.be/assets/images/picto/" + image + ".png");
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public String getImage() {
      return image;
    }
  }

  /*
   * OTHERS
   */

  /**
   * Create a link to redirect to the given contribution
   *
   * @return a html link to redirect to contribution
   */
  protected String writeContributionHtmlLink(Long id, EContributionType type, String name){
    return writeContributionHtmlLink(id, type, name, 0, 0);
  }

  /**
   * Create a link to redirect to the given contribution
   *
   * @return a html link to redirect to contribution
   */
  protected String writeContributionHtmlLink(Long id, EContributionType type, String name, int pane, int pov){
    return "<a class=\"primary\" href=\"" + be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(id, type.id(), pane, pov) + "\">" + name + "</a>";
  }

  /**
   * Create an action button to do something with a place
   *
   * @return a button for a place
   */
  protected String writePlaceHtmlLink(Long id, String name){
    return "<button type=\"button\" class=\"btn btn-link btn-link-simple primary btn-place-viz\" data-id =\"" + this.id + "\" data-place-id=\"" + id + "\">" + name + "</button>";
  }

  protected List<ContextHasCategoryLinkForm> fromApiToContextHasCategoryLinkForms(List<ContextHasCategory> links, ContextContribution context) {
    return links.stream()
            .filter(user::mayView)
            .map(category -> new ContextHasCategoryLinkForm(category, context, user, lang))
            .collect(Collectors.toList());
  }

  protected List<ArgumentJustificationLinkForm> fromApiToArgumentJustificationLinkForms(List<ArgumentJustification> links, ContextContribution context) {
    return links.stream()
            .filter(user::mayView)
            .map(link -> new ArgumentJustificationLinkForm(link, context, user, lang))
            .collect(Collectors.toList());
  }

  protected Map<EJustificationLinkShade, List<ArgumentJustificationLinkForm>> fromApiToArgumentJustificationLinkFormsMap(List<ArgumentJustification> links, ContextContribution context) {
    Map<EJustificationLinkShade, List<ArgumentJustificationLinkForm>> argumentsMap = new HashMap<>();

    links.stream()
            .filter(user::mayView)
            .forEach(link -> {

      if(!argumentsMap.containsKey(link.getLinkType().getEType()))
        argumentsMap.put(link.getLinkType().getEType(), new ArrayList<>());

      argumentsMap.get(link.getLinkType().getEType()).add(new ArgumentJustificationLinkForm(link, context, user, lang));
    });

    return argumentsMap;
  }

  protected Map<EJustificationLinkShade, List<CitationJustificationLinkForm>> fromApiToCitationJustificationLinkFormsMap(List<CitationJustification> links) {
    Map<EJustificationLinkShade, List<CitationJustificationLinkForm>> citationsMap = new HashMap<>();

    links.stream()
            .filter(user::mayView)
            .forEach(link -> {

      if(!citationsMap.containsKey(link.getLinkType().getEType()))
        citationsMap.put(link.getLinkType().getEType(), new ArrayList<>());

      citationsMap.get(link.getLinkType().getEType()).add(new CitationJustificationLinkForm(link, user, lang));
    });

    return citationsMap;
  }
  
}
