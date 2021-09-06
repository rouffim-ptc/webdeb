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

import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.actor.EAffiliationType;
import be.webdeb.core.api.actor.Profession;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds values for actor's affiliation. (no IDs, but their descriptions, as defined in the database).
 * Except by using a constructor, no value can be edited outside of this package or by subclassing.
 *
 * @author Fabian Gilson
 */
public class AffiliationHolder extends ContributionHolder {

  // ActorHasAffiliation id (if existing)
  protected Long aha = -1L;
  // functions
  protected Profession profession;
  protected String function = "";
  protected String substitute = "";
  // type of affiliation for organization's affiliation
  protected String afftype = "";
  protected EAffiliationType affEType;
  // name of affiliation Actor
  protected Long affid = -1L;
  protected String affname = "";
  protected String affavatar = "";
  // hold function + affname
  protected String fullfunction = "";
  protected String startDate = "";
  protected String endDate = "";
  protected int startDateType = -1;
  protected int endDateType = -1;

  private Affiliation affiliation;
  private ActorHolder affiliated = null;

  /**
   * Play / JSON compliant constructor
   */
  public AffiliationHolder() {
    super();
    type = EContributionType.AFFILIATION;
  }

  /**
   * Construct an AffiliationHolder from an Affiliation
   *
   * @param affiliation the Affiliation api object
   */
  public AffiliationHolder(Affiliation affiliation, WebdebUser user, String lang){
    this(affiliation, user, lang, false);
  }
  /**
   * Construct an AffiliationHolder from an Affiliation
   *
   * @param affiliation the Affiliation api object
   */
  public AffiliationHolder(Affiliation affiliation, WebdebUser user, String lang, boolean light) {
    this();

    this.affiliation = affiliation;
    this.user = user;

    this.id = affiliation.getId();
    this.lang = lang;
    this.version = affiliation.getVersion();
    aha = affiliation.getId();
    version = affiliation.getVersion();
    if (affiliation.getActor() != null) {
      affid = affiliation.getActor().getId();
      affname = affiliation.getActor().getFullname(lang);
      affavatar = affiliation.getActor().getAvatar()== null ?
              helper.computeAvatar( affiliation.getActor().getActorTypeId(),  affiliation.getActor().getGenderAsString()) :
              affiliation.getActor().getAvatar().getPictureFilename();
    }
    if (affiliation.getFunction() != null) {
      profession = affiliation.getFunction();
      function = affiliation.getFunction().getSimpleName(lang);
      Profession sub = affiliation.getFunction().getSubstitute();
      substitute = sub != null ? sub.getName(lang) : function;
    }

    fullfunction = affiliation.getFullfunction(lang, true, true);
    startDate = affiliation.getStartDate() != null ? affiliation.getStartDate() : "";
    endDate = affiliation.getEndDate() != null ? affiliation.getEndDate() : "";
    startDateType = affiliation.getStartType() != null ? affiliation.getStartType().getType() : -1;
    endDateType = affiliation.getEndType() != null ? affiliation.getEndType().getType() : -1;

    try {
      afftype = affiliation.getAffiliationType() != null && !EAffiliationType.UNSET.equals(affiliation.getAffiliationType())?
          actorFactory.getAffiliationType(affiliation.getAffiliationType().id()).getName(lang) : "";
      affEType = affiliation.getAffiliationType() != null ? affiliation.getAffiliationType() : EAffiliationType.UNSET;
    } catch (FormatException e) {
      logger.error("unable to cast affiliation type " + affiliation.getAffiliationType(), e);
    }
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

  /*
   * GETTERS
   */

  /**
   * Get this affiliation id
   *
   * @return an id, -1 if this affiliation does not yet exist in database layer
   */
  public Long getAha() {
    return aha;
  }

  /**
   * Get this affiliation function, if any (language dependant)
   *
   * @return the function name bound to this affiliation
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
   * Get the function hierarchy
   *
   * @return a map of profession names and boolean to displayHierarchy with the given profession itself, or empty list if profession is not found
   */
  public Map<String, Boolean> getFunctionComposition(){
    return profession != null ? profession.getFunctionHierarchy(lang) : new HashMap<>();
  }

  /**
   * Get the avatar of the affiliation actor
   *
   * @return the avatar
   */
  public String getAffavatar() {
    return affavatar;
  }

  /**
   * Get the name of the affiliation actor, empty string if unset
   *
   * @return the affiliation actor's name (may be empty)
   */
  public String getAffname() {
    return affname;
  }

  /**
   * Get the affiliation actor's id
   *
   * @return the affiliation actor id, -1 if unset
   */
  public Long getAffid() {
    return affid;
  }

  /**
   * Get the affiliation type
   *
   * @return the affiliation type label
   */
  public String getAfftype() {
    return afftype;
  }

  /**
   * Get the affiliation enum type
   *
   * @return the affiliation enum type
   */
  public EAffiliationType getAffEType() {
    return affEType;
  }


  /**
   * Get the concatenation of function and affiliation actor name
   *
   * @return function + " " + affname, may be empty
   */
  public String getFullfunction() {
    return fullfunction;
  }

  /**
   * Get the string representation of the starting date of the affiliation
   *
   * @return a DD/MM/YYYY date (with D and M optional), may be empty
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Get the string representation of the ending date of the affiliation
   *
   * @return a DD/MM/YYYY date (with D and M optional), may be empty
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Get the precision start date type
   *
   * @return a precision start date type
   */
  public int getStartDateType() {
    return startDateType;
  }

  /**
   * Get the precision end date type
   *
   * @return a precision end date type
   */
  public int getEndDateType() {
    return endDateType;
  }



  public ActorHolder getAffiliatedHolder(){
    if(affiliated == null && affiliation != null && affiliation.getActor() != null){
      affiliated = new ActorHolder(affiliation.getActor(), user, lang);
    }
    return affiliated;
  }

}
