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
import be.webdeb.infra.ws.nlp.WikiAffiliation;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.libs.Json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the form part related to affiliations.
 *
 * Using isDisambiguated flag to know if the actor must be disambiguated, ie, the id is empty and we found possible
 * matches in database. If so, the views.util.handleNameMatches frame must be triggered to ask to user to explicitly
 * choose between binding to an existing actor or create a new one. This name matches check is performed explicitly as
 * part of the form validation processes, but outside of Play's implicit validation method.
 *
 * {@link be.webdeb.presentation.web.controllers.entry.ContributionHelper#findAffiliationsNameMatches(List, WebdebUser, String)
 * Name matches} are currently performed on full matches, but the method in charge may be easily amended to perform
 * more advanced matching queries. The UI process would remain the same, but actual accessor method would require
 * to be changed (not using factory's findByFullname anymore).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class AffiliationForm extends AffiliationHolder {

  // function id for function matches
  protected int functionid = -1;
  private String gender = "M";

  // boolean to check if this affiliation organisation has been explicitly disambiguated
  private boolean isDisambiguated = false;
  private List<ActorHolder> nameMatches = new ArrayList<>();

  // hold the as-is json string retrieved from the wikidata service
  private String fetched = "";


  /**
   * Play / JSON compliant constructor
   */
  public AffiliationForm() {
    super();
  }

  /**
   * Construct an AffiliationForm only with the name of a profession
   *
   * @param profession a profession
   * @param lang a 2-char code representing a language
   * @param gender a one char code that representing a gender
   */
  public AffiliationForm(Profession profession, String lang, String gender) {
    this();

    this.lang = lang;
    this.gender = gender;
    function = profession.getName(lang);
    fullfunction = profession.getGendersNames(lang);
    functionid = profession.getId();
  }

  /**
   * Construct an AffiliationForm with an ActorHasAffiliation
   *
   * @param affiliation the Affiliation api object
   * @param lang a 2-char code representing a language
   */
  public AffiliationForm(Affiliation affiliation, WebdebUser user, String lang) {
    super(affiliation, user, lang);

    functionid = affiliation.getFunction() != null ? affiliation.getFunction().getId() : -1;
    // overwrite type with type id
    afftype = affiliation.getAffiliationType() != null ? String.valueOf(affiliation.getAffiliationType().id()) : "";
  }

  /**
   * Verify the validity of the content of this affiliation, ie, if a date is specified, a function or/and an
   * affname must be specified if both dates are specified, they must be ascending
   *
   * Also checks whether an unknown affiliation may not be bound to an existing Actor.
   * Finally, checks if fetched values from wikidata (affiliation name spellings) may not be found in the database.
   *
   * @return a list of error messages, if any, an empty list otherwise
   */
  public Map<String, String> checkAffiliation() {
    Map<String, String> errors = new HashMap<>();

    if (!values.isBlank(function.trim()) && values.isDate(function.trim())) {
      errors.put("function", "affiliation.error.function.format");
    }

    if (!values.isBlank(affname.trim()) && values.isDate(affname.trim())) {
      errors.put("affname", "affiliation.error.affname.format");
    }

    if (!values.isBlank(startDate.trim()) || !values.isBlank(endDate.trim())) {
      // if a date is given, either affiliation or function must be specified
      if (values.isBlank(function) && values.isBlank(affname)) {
        errors.put("affname", "affiliation.error.missing.affiliation");
        errors.put("function", "");
      }

      // check format of given date(s)
      if ((!values.isBlank(startDate.trim()) && !values.isDate(startDate.trim()))
              || (!values.isBlank(endDate.trim()) && !values.isDate(endDate.trim()))) {
        errors.put("endDate", "affiliation.error.date.format");
        errors.put("startDate", "");
      } else {
        // dates are ok, must check if startdate is before endDate
        if (!values.isBlank(startDate.trim()) && !values.isBlank(endDate.trim())
                && !values.isBefore(startDate.trim(), endDate.trim())) {
          errors.put("endDate", "affiliation.error.date.order");
          errors.put("startDate", "");
        }
      }
    }
    Actor actor;

    if (!values.isBlank(affname)) {
      affname = affname.trim();
      // check if we have an id and if any, it must match the name in DB (ie, was not changed without
      // updating the id by auto-complete event

      if (!values.isBlank(affid)) {
        actor = actorFactory.retrieve(affid);
        EActorType foundType = actor != null ? actor.getActorType() : null;
        if (actor == null) {
          // no match, must clear id and try to find a corresponding actor afterwards
          logger.debug("affid wrongfully set to " + affid + " for affiliation " + affname);
          affid = -1L;
        }
      }

    } else {
      // we do not have a name, clear id and type fields (may cause inconsistencies otherwise)
      affid = -1L;
      afftype = "";
    }
    // sends back the list of errors (list may be empty)
    return errors;
  }

  /**
   * Cast this form to an API affiliation object. Functions ids are checked / updated here.
   * As preconditions,
   * * Affiliation ids must be valid here
   * * Fetched spellings, if existing, must also be valid
   * In our case (workflow coming from form validation then saving), both conditions have been checked
   * in {@link ActorForm#validate() form implicit validate} via {@link #checkAffiliation() affiliation checks}.
   *
   * @return the Affiliation holding all values of this form
   */
  public Affiliation toAffiliation() {
    return toAffiliation(null, EProfessionType.OTHERS);
  }

  /**
   * Cast this form to an API affiliation object. Functions ids are checked / updated here.
   * As preconditions,
   * * Affiliation ids must be valid here
   * * Fetched spellings, if existing, must also be valid
   * In our case (workflow coming from form validation then saving), both conditions have been checked
   * in {@link ActorForm#validate() form implicit validate} via {@link #checkAffiliation() affiliation checks}.
   *
   * @param affiliationType the affiliation type
   * @param professionType the profession type of the affiliation
   * @return the Affiliation holding all values of this form
   */
  public Affiliation toAffiliation(EAffiliationType affiliationType, EProfessionType professionType) {
    logger.debug("transform affiliation " + toString());

    Affiliation affiliation = actorFactory.getAffiliation();

    // deserialize wikidata fetched value, if any
    WikiAffiliation wikiAffiliation = deserializeFetched();

    if (!values.isBlank(afftype) || affiliationType != null) {
      try {
        affiliation.setAffiliationType(!values.isBlank(afftype) ? EAffiliationType.value(Integer.parseInt(afftype)) : affiliationType);
      } catch (FormatException | NumberFormatException e) {
        logger.error("unparsable affiliation type (should not happen here): " + afftype, e);
      }
    }

    // check function field (if id => retrieve it and verify it is correct)
    if (!values.isBlank(function)) {
      function = function.trim();

      Map<String, Map<String, String>> unfetched = wikiAffiliation != null ?
              getDeserializedFetchedSpelling(wikiAffiliation.getFunction()) : new HashMap<>();
      Map<String, Map<String, String>> spellings = new HashMap<>();
      Profession p = null;

      if (!values.isBlank(functionid)) {
        try {
          p = actorFactory.getProfession(functionid);
          // check if id and name corresponds (may be selected and updated from view)
          if (p == null || !p.getName(lang).equals(function)) {
            logger.debug("clear profession id since name does not correspond " + functionid + " " + function);
            functionid = -1;
            p = null;
          } else {
            // update spellings, if any
            for (Map.Entry<String, Map<String, String>> lang : unfetched.entrySet()) {
              for (Map.Entry<String, String> name : lang.getValue().entrySet()) {
                p.addName(name.getValue(), lang.getKey(), name.getKey());
              }
            }
          }
        } catch (FormatException e) {
          logger.error("unfound profession for id " + functionid, e);
        }
      }

      // check whether we cannot find it by name
      if (p == null) {
        // must ensure function is present in fetched spellings, otherwise, only consider function field
        if(checkNameInMap(unfetched, function)) {
          spellings.putAll(unfetched);
        } else {
          if(!spellings.containsKey(lang)){
            spellings.put(lang, new HashMap<>());
          }
          spellings.get(lang).put(gender, function);
        }

        // check in all spellings if we find a match
        Profession match = null;
        for (Map.Entry<String, Map<String, String>> lang : spellings.entrySet()) {
          for (Map.Entry<String, String> name : lang.getValue().entrySet()) {
            match = actorFactory.findProfession(name.getValue(), lang.getKey(), professionType.id());
            if (match != null)break;
          }
          if (match != null)break;
        }

        if (match != null) {
          p = match;
          // update spellings where necessary
          for (Map.Entry<String, Map<String, String>> lang : spellings.entrySet()) {
            for (Map.Entry<String, String> name : lang.getValue().entrySet()) {
              if (match.getName(lang.getKey()) == null) {
                match.addName(name.getValue(), lang.getKey(), name.getKey());
              }
            }
          }
        } else {
          // create new profession with all spellings
          Map<String, Map<String, String>> profession = new HashMap<>();
          profession.putAll(spellings);
          p = actorFactory.createProfession(-1, professionType, profession);
        }
      }
      affiliation.setFunction(p);
    } else {
      // force cleanup of functionid
      functionid = -1;
    }

    if (!values.isBlank(affname)) {
      Actor actor;
      if (values.isBlank(affid)) {
        EActorType actorType;

        if(affiliation.getAffiliationType() != null) {
            switch (affiliation.getAffiliationType()) {
                case SON_OF:
                case CABINET_OF:
                    actorType = EActorType.PERSON;
                    break;
                case DEPARTMENT_OF:
                case MEMBER_OF:
                case PRODUCED_BY:
                case AWARDED_BY:
                case PARTICIPATING_IN:
                case GRADUATING_FROM:
                case HAS_DEPARTMENT:
                case HAS_MEMBER:
                case PRODUCES:
                case AWARDS:
                case HAS_PARTICIPANT:
                case HAS_CABINET:
                default:
                    actorType = EActorType.ORGANIZATION;
            }
        } else {
            actorType = EActorType.ORGANIZATION;
        }

        actor = actorType == EActorType.PERSON ? actorFactory.getPerson() : actorFactory.getOrganization();
        
        // add all retrieved spellings (we know they corresponds because they've been checked also)
        if (wikiAffiliation != null && wikiAffiliation.getOrganization() != null) {
          actor.setNames(getDeserializedFetched(wikiAffiliation.getOrganization()).entrySet().stream().map(e -> {
            ActorName name = actorFactory.getActorName(e.getKey());
            name.setLast(e.getValue());
            return name;
          }).collect(Collectors.toList()));
        } else {
          ActorName name = actorFactory.getActorName(lang);
          name.setLast(affname);
          actor.setNames(Collections.singletonList(name));
        }
      } else {
        // we know it's valid (methods pre-condition), add unknown spellings for this actor, if any
        actor = actorFactory.retrieve(affid);
        if (wikiAffiliation != null) {
          // for all fetched values, add any unknown spellings
          List<String> existingLang = actor.getNames().stream().map(ActorName::getLang).collect(Collectors.toList());
          getDeserializedFetched(wikiAffiliation.getOrganization()).forEach((key, value) -> {
            if (!existingLang.contains(key)) {
              ActorName name = actorFactory.getActorName(key);
              name.setLast(value);
              actor.getNames().add(name);
            }
          });
        }
      }
      affiliation.setActor(actor);
    }

    if (!values.isBlank(aha)) {
      affiliation.setId(aha);
    } else {
      // must be forced since Play put a bloody null in it for elements created from javascript
      aha = -1L;
    }

    if (!values.isBlank(startDateType)) {
      try {
        affiliation.setStartDateType(actorFactory.getPrecisionDateType(startDateType));
      } catch (FormatException e) {
        logger.error("given start date type is not valid " + startDateType, e);
      }
    }

    if (!values.isBlank(endDateType)) {
      try {
        affiliation.setEndDateType(actorFactory.getPrecisionDateType(endDateType));
      } catch (FormatException e) {
        logger.error("given end date type is not valid " + endDateType, e);
      }
    }

    if (!values.isBlank(startDate)) {
      try {
        affiliation.setStartDate(startDate);
      } catch (FormatException e) {
        logger.error("unparsable start affiliation date (should not happen here): " + startDate, e);
      }
    }

    if (!values.isBlank(endDate)) {
      try {
        affiliation.setEndDate(endDate);
      } catch (FormatException e) {
        logger.error("unparsable end affiliation date (should not happen here): " + endDate, e);
      }
    }

    affiliation.setVersion(version);

    return affiliation;
  }

  /**
   * Check if a name is in map
   *
   * @return true if it is
   */
  private boolean checkNameInMap(Map<String, Map<String, String>> map, String nameToMatch){
    for(Map.Entry<String, Map<String, String>> lang : map.entrySet()){
      for(Map.Entry<String, String> name : lang.getValue().entrySet()){
        if(name.getValue().equals(nameToMatch)){
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check whether the affiliation is fully empty
   *
   * @return true if all fields (except ids) are empty
   */
  public boolean isEmpty() {
    return values.isBlank(affname) && values.isBlank(startDate) && values.isBlank(endDate) && values.isBlank(function);
  }

  /*
   * SETTERS
   */

  /**
   * Set an affiliation id (won't update other related fields such as affiliation names, etc)
   *
   * @param aha an id to set
   */
  public void setAha(Long aha) {
    this.aha = aha;
  }

  /**
   * Set this affiliation function name (language dependant)
   *
   * @param function a function name
   */
  public void setFunction(String function) {
    this.function = function;
  }

  /**
   * Set the name of the affiliation actor
   *
   * @param affname the affiliation actor's name
   */
  public void setAffname(String affname) {
    this.affname = affname;
  }

  /**
   * Set the affiliation actor's id
   *
   * @param id an affiliation id
   */
  public void setAffid(Long id) {
    affid = id;
  }

  /**
   * Set the affiliation type id
   *
   * @param afftype an affiliation type id
   */
  public void setAfftype(String afftype) {
    this.afftype = afftype;
  }

  /**
   * Set the string representation of the starting date of the affiliation
   *
   * @param startDate a DD/MM/YYYY date (with D and M optional), may be empty
   */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /**
   * Set the string representation of the ending date of the affiliation
   *
   * @param endDate a DD/MM/YYYY date (with D and M optional), may be empty
   */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /**
   * Set the precision start date type
   *
   * @param type a precision start date type
   */
  public void setStartDateType(Integer type) {
    startDateType = type == null ? -1 : type;
  }

  /**
   * Set the precision end date type
   *
   * @param type a precision end date type
   */
  public void setEndDateType(Integer type) {
    endDateType = type == null ? -1 : type;
  }

  /**
   * Get the function (profession) id
   *
   * @return an id, -1 if unset or unknown
   */
  public Integer getFunctionid() {
    return functionid;
  }

  /**
   * Set the function id
   *
   * @param functionid an id
   */
  public void setFunctionid(Integer functionid) {
    this.functionid = functionid != null ? functionid : -1;
  }

  /*
   * NAME MATCHES HANDLING
   */

  /**
   * Set the isDisambiguated flag, ie, if this flag is false and this affiliation actor has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @param isDisambiguated true if this affiliation actor has been explicitly disambiguated
   */
  public void setIsDisambiguated(boolean isDisambiguated) {
    this.isDisambiguated = isDisambiguated;
  }

  /**
   * Get the isDisambiguated flag, ie, if this flag is false and this affiliation actor has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @return true if this affiliation actor has been explicitly disambiguated
   */
  public boolean getIsDisambiguated() {
    return isDisambiguated;
  }

  /**
   * Set the list possible matches on this affiliation actor's full name
   *
   * @param nameMatches a list of actor holder having the same name as this affiliation actor
   */
  public void setNameMatches(List<ActorHolder> nameMatches) {
    this.nameMatches = nameMatches;
  }

  /**
   * Get the list possible matches on this affiliation actor's full name
   *
   * @return a (possibly empty) list of actor holder having the same name as this affiliation actor
   */
  public List<ActorHolder> getNameMatches() {
    return nameMatches;
  }

  /**
   * Get the fetched json string as retrieved from the wikidata service. Will be used to handle multi-languages
   * and search the database for existing values in other languages than the form's current (user) lang
   *
   * @return a string serialization of a WikiAffiliation
   * @see WikiAffiliation
   */
  public String getFetched() {
    return fetched;
  }

  /**
   * Set the fetched json string as retrieved from the wikidata service. Will be used to handle multi-languages
   * and search the database for existing values in other languages than the form's current (user) lang
   *
   * @param fetched a string serialization of a WikiAffiliation
   * @see WikiAffiliation
   */
  public void setFetched(String fetched) {
    this.fetched = fetched;
  }

  /**
   * Get the function gender (one char code M, F or N)
   *
   * @return a string serialization of a WikiAffiliation
   */
  public String getGender() {
    return gender;
  }

  /**
   * Set the function gender (one char code M, F or N)
   *
   * @param gender a string serialization of a WikiAffiliation
   */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /**
   * Deserialize the fetched wikidata affiliation to a wiki affiliation object
   *
   * @return the deserialized object, null if no fetched value exist for this affiliation
   */
  public WikiAffiliation deserializeFetched() {
    try {
      return !values.isBlank(fetched) ? Json.fromJson(Json.parse(fetched), WikiAffiliation.class) : null;
    } catch (Exception e) {
      logger.warn("unable to unfetch " + fetched, e);
      return  null;
    }
  }

  /**
   * Get a map of key (field name) values for given dictionary
   *
   * @param dictionary a wikidata dictionary containing a set of values
   * @return the map of field name (ie lang code) and associated values, an empty map if given dictionary is null
   */
  public Map<String, String> getDeserializedFetched(WikiAffiliation.WikiDictionary dictionary) {
    Map<String, String> spellings = new HashMap<>();
    if (dictionary != null) {
      for (Field field : dictionary.getClass().getDeclaredFields()) {
        try {
          field.setAccessible(true);
          Object value = field.get(dictionary);
          if (value != null) {
            spellings.put(field.getName(), (String) value);
          }
        } catch (IllegalAccessException e) {
          logger.warn("unable to access to field " + field.getName(), e);
        }
      }
    }
    return spellings;
  }

  /**
   * Get a map of key (field name) values for given dictionary
   *
   * @param dictionary a wikidata dictionary containing a set of values
   * @return the map of field name (ie lang code and gender code) and associated values, an empty map if given dictionary is null
   */
  public Map<String, Map<String, String>> getDeserializedFetchedSpelling(WikiAffiliation.WikiDictionary dictionary) {
    Map<String, Map<String, String>> spellings = new HashMap<>();
    if (dictionary != null) {
      for (Field field : dictionary.getClass().getDeclaredFields()) {
        try {
          field.setAccessible(true);
          Object value = field.get(dictionary);
          if (value != null) {
            Map<String, String> functionMap = new HashMap<>();
            functionMap.put("N", (String) value);
            spellings.put(field.getName(), functionMap);
          }
        } catch (IllegalAccessException e) {
          logger.warn("unable to access to field " + field.getName(), e);
        }
      }
    }
    return spellings;
  }

  @Override
  public String toString() {
    return "AffiliationForm {" +
            "aha=" + aha +
            ", function='" + function + '\'' +
            ", functionid=" + functionid +
            ", affname='" + affname + '\'' +
            ", affid=" + affid +
            ", type=" + afftype +
            ", fullfunction='" + fullfunction + '\'' +
            ", startDate='" + startDate + '\'' +
            ", endDate='" + endDate + '\'' +
            ", startDateType='" + endDateType + '\'' +
            ", endDateType='" + endDateType + '\'' +
            ", lang='" + lang + '\'' +
            '}';
  }
}
