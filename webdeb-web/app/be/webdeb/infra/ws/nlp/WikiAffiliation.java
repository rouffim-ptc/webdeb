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

package be.webdeb.infra.ws.nlp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This POJO class is used to deserialize wikidata affiliations as retrieved from the WDTAL service and
 * transmitted as-is in the html form. The deserialized object will be used to try to find matches in db with
 * existing organizations and professions (because retrieved affiliations are in multiple languages)
 *
 * @author Fabian Gilson
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WikiAffiliation {

  /**
   * dictionary of affiliation organizations (with key-value pairs with iso-639-1 language code and names)
   */
  @JsonDeserialize
  private WikiDictionary organization;

  /**
   * dictionary of functions (with key-value pairs with iso-639-1 language code and names)
   */
  @JsonDeserialize
  private WikiDictionary function;

  /**
   * YYYY-MM-DD formatted date when this affiliation started
   */
  @JsonDeserialize
  @JsonProperty("start_date")
  private String startDate;

  /**
   * YYYY-MM-DD formatted date when this affiliation ended
   */
  @JsonDeserialize
  @JsonProperty("end_date")
  private String endDate;

  /**
   * Get the list of translations for this affiliation's organization
   *
   * @return a dictionary of spellings
   */
  public WikiDictionary getOrganization() {
    return organization;
  }

  /**
   * Get the list of translations for this affiliation's function
   *
   * @return a dictionary of spellings
   */
  public WikiDictionary getFunction() {
    return function;
  }

  /**
   * Inner class that handles all possible languages that may be set for organization's names and functions
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WikiDictionary {

    /**
     * English iso-639-1 code
     */
    @JsonDeserialize
    private String en;

    /**
     * French iso-639-1 code
     */
    @JsonDeserialize
    private String fr;

    /**
     * Chinese iso-639-1 code
     */
    @JsonDeserialize
    private String zh;

    /**
     * Portuguese iso-639-1 code
     */
    @JsonDeserialize
    private String pt;

    /**
     * German iso-639-1 code
     */
    @JsonDeserialize
    private String de;

    /**
     * Turkish iso-639-1 code
     */
    @JsonDeserialize
    private String tr;

    /**
     * Italian iso-639-1 code
     */
    @JsonDeserialize
    private String it;

    /**
     * Arabic iso-639-1 code
     */
    @JsonDeserialize
    private String ar;

    /**
     * Polish iso-639-1 code
     */
    @JsonDeserialize
    private String pl;

    /**
     * Dutch iso-639-1 code
     */
    @JsonDeserialize
    private String nl;

    /**
     * Spanish iso-639-1 code
     */
    @JsonDeserialize
    private String es;
  }
}
