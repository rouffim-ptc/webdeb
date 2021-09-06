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

package be.webdeb.presentation.web.controllers.viz.actor;

import be.webdeb.core.api.actor.*;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleHolder;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import play.api.Play;
import play.libs.Json;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Simple class to wrap affiliation dates of actors into ready-to-be-displayed data.
 * Contains both (special) string representation and year for starting and ending dates.
 *
 * In case of "ongoing" (-1) or "unknown" (0) dates, special values (i18n messages keys) are passed
 * in string representations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class AffiliationViz implements Comparable<AffiliationViz> {

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Inject
  protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  private Affiliation affiliation;
  private String lang;

  private String from;
  private Date fromDate;
  private String to;
  private int fromTypeId;
  private String fromTypeName;
  private int toTypeId;
  private String toTypeName;
  private String gender;

  private String function;
  private String neutralFunction;
  private String genericFunction;
  private String functionFilter = "";
  private String affType;
  private String affTypeNameOrSubstitude;
  private String orgType;

  private Long affiliatedId;
  private String affiliatedName;
  private Long id;

  /**
   * Constructor
   *
   * @param affiliation an API affiliation
   * @param lang 2-char ISO 639-1 code of the interface language
   */
  public AffiliationViz(Affiliation affiliation, String lang) {
    this.affiliation = affiliation;
    this.lang = lang;

    this.from = affiliation.getStartDate();
    this.fromDate = values.toDate(affiliation.getStartDate());
    this.to = affiliation.getEndDate();
    this.fromTypeId = affiliation.getStartType() != null ? affiliation.getStartType().getType() : -1;
    this.fromTypeName = affiliation.getStartType() != null && affiliation.getStartType().getEType().isDrawable() ? affiliation.getStartType().getName(lang) : "";
    this.toTypeId = affiliation.getEndType() != null ? affiliation.getEndType().getType() : -1;
    this.toTypeName = affiliation.getEndType() != null && affiliation.getEndType().getEType().isDrawable() ? affiliation.getEndType().getName(lang) : "";

    this.gender = affiliation.getActor().getActorType() == EActorType.PERSON ? affiliation.getActor().getGenderAsString() : affiliation.getAffiliated().getGenderAsString();
    this.function = affiliation.getFunction() != null ? affiliation.getFunction().getName(lang, this.gender).replace(";", ",") : "";
    this.neutralFunction = affiliation.getFunction() != null ? affiliation.getFunction().getName(lang).replace(";", ",") : "";
    this.genericFunction = affiliation.getFunction() != null ? affiliation.getFunction().getSuperLink() != null ? affiliation.getFunction().getSuperLink().getName(lang) : "" : "";

    this.affiliatedId = affiliation.getActor().getId();
    this.affiliatedName = affiliation.getActor().getFullname(lang).split("\\(")[0];

    if(!values.isBlank( this.neutralFunction)) {
      Map<String, List<String>> filterMap = new HashMap<>();
      filterMap.put(EFilterName.FUNCTION.toString(), Collections.singletonList(this.neutralFunction));
      this.functionFilter = Json.toJson(filterMap).toString();
    }

    this.affType = affiliation.getAffType() != null ? affiliation.getAffType().getName(lang) : "";
    this.affTypeNameOrSubstitude = affiliation.getAffiliationType() != null && affiliation.getAffiliationType().getSubstitudeName(lang) != null ?
            affiliation.getAffiliationType().getSubstitudeName(lang) : this.affType;

    Organization o = affiliation.getActor().getActorType() == EActorType.ORGANIZATION ? ((Organization) affiliation.getActor()) : null;
    this.orgType = o != null && o.getLegalStatus() != null ? o.getLegalStatus().getName(lang) : "";

    this.id = affiliation.getId();
  }

  /**
   * Get the string value of the starting date of this affiliation
   *
   * @return a date of the form DD/MM/YYYY (D and M optional)
   */
  public String getFrom() {
    return from;
  }

  /**
   * Get the string value of the ending date of this affiliation
   *
   * @return a date of the form DD/MM/YYYY (D and M optional)
   */
  public String getTo() {
    return to;
  }

  public String getFromTypeName() {
    return fromTypeName;
  }

  public String getToTypeName() {
    return toTypeName;
  }

  public int getToTypeId() {
    return toTypeId;
  }

  public String getFunction() {
    return function;
  }

  public String getNeutralFunction() {
    return neutralFunction;
  }

  public String getGenericFunction() {
    return genericFunction;
  }


  public String getGenericFunctionOrDefault() {
    return values.isBlank(genericFunction) ? neutralFunction : genericFunction;
  }

  public String getAffType() {
    return affType;
  }

  /*
    Get the regrouped aff type
   */

  public String getAffTypeNameOrSubstitude() {
    return affTypeNameOrSubstitude;
  }

  public String getOrgType() {
    return orgType;
  }

  public Long getAffiliatedId() {
    return affiliatedId;
  }

  public String getAffiliatedName() {
    return affiliatedName;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public ActorSimpleHolder getAffiliatedAsHolder(){
    return new ActorSimpleHolder(affiliation.getActor(), lang);
  }

  @Override
  public int compareTo(@NotNull AffiliationViz o) {
    if(fromDate != null && o.fromDate != null) {
      int result = fromDate.compareTo(o.fromDate);
      if(result == 0){
        result = neutralFunction.compareTo(o.neutralFunction);
      }
      return result;

    }else if(fromDate == null && o.fromDate != null) {
      return -1;
    }else if(fromDate != null) {
      return 1;
    }
    return 0;
  }

  /**
   * Check if there is something to filtering.
   *
   * @return true if it is filterable.
   */
  @JsonIgnore
  public boolean isFilterable() {
    return !values.isBlank(this.functionFilter);
  }

  /**
   * Get the filterable string, ie, a stringified map of key : [array of values] separated by commas "," containing
   * all values that may be subject to filter contributions on. Mainly used when displaying results of search queries.
   *
   * Looks like a stringified javascript map, to be handled as such by the view.
   *
   * @return a stringified list of key, value pairs.
   */
  @JsonIgnore
  public String getFilterable() {
    return this.functionFilter;
  }

  /**
   * Get a comma separated view of this element
   *
   * @return affType,function,affiliatedId,affiliatedName,from,fromType,to,toType,idAff
   */
  @Override
  public String toString() {
    return affType + ";" + function + ";" + affiliatedId + ";" + affiliatedName + ";" + from + ";" + fromTypeId + ";" + fromTypeName + ";" + to + ";" + toTypeId + ";" + toTypeName + ";" + id;
  }

}
