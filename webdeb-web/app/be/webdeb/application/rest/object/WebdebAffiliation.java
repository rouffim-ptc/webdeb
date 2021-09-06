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
 *
 */

package be.webdeb.application.rest.object;

import be.webdeb.core.api.actor.Affiliation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is devoted to exchange Affiliations in JSON format for an holding Actor/contributor.
 * Contains the link to an affiliation Actor (its id), the function and starting/ending dates.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebAffiliation extends WebdebContribution {

  /**
   * id of actor being the organization to which bound actor is affiliated
   */
  @JsonSerialize
  protected Long affiliationActor;

  /**
   * List of function names (possibly in many languages) of bound actor in this.affiliationActor
   */
  @JsonSerialize
  protected List<WebdebProfession> function = new ArrayList<>();
  /**
   * ((DD/)MM/)YYYY staring date of this affiliation (D and M optional)
   */
  @JsonSerialize
  protected String test;
  /**
   * ((DD/)MM/)YYYY staring date of this affiliation (D and M optional)
   */
  @JsonSerialize
  protected String startDate;

  /**
   * ((DD/)MM/)YYYY ending date of this affiliation (D and M optional)
   */
  @JsonSerialize
  protected String endDate;

  /**
   * @api {get} WebdebAffiliation Actor affiliation
   * @apiName Affiliation
   * @apiGroup Structures
   * @apiDescription An affiliation represents the linkage between an actor and, possibly, another with a function
   * @apiSuccess {Integer} affiliationActor the id of the affiliation actor, if any (null otherwise)
   * @apiSuccess {WebdebProfession[]} function array of profession object (i18n names) (optional)
   * @apiSuccess {String} startDate a starting date in DD/MM/YYYY format with D and M optional (optional)
   * @apiSuccess {String} endDate an ending date in DD/MM/YYYY format with D and M optional (optional, "-1" denotes an "ongoing" affiliation)
   * @apiExample Actor's affiliation
   * "affiliations": [
   *   {
   *     "id": 265,
   *     "type": "affiliation",
   *     "version": 1464270120000,
   *     "affiliationActor": 769,
   *     "function": [
   *       { "name": "businessperson", "lang": "en", "gender": "N" },
   *     ],
   *     "endDate": "-1"
   *   },
   *   {
   *     "id": 266,
   *     "type": "affiliation",
   *     "version": 1486739277000,
   *     "affiliationActor": 749,
   *     "function": [
   *       { "name": "president", "lang": "en", "gender": "M" },
   *       { "name": "président", "lang": "fr", "gender": "M" }
   *     ],
   *     "startDate": "01/2017"
   *   }
   * ]
   * @apiVersion 0.0.3
   */
  public WebdebAffiliation(Affiliation affiliation) {
    super(affiliation);
    if (affiliation.getActor() != null) {
      affiliationActor = affiliation.getActor().getId();
    }
    if (affiliation.getFunction() != null && affiliation.getAffiliated() != null) {
      String gender = affiliation.getAffiliated().getGenderAsString();
      for(Map.Entry<String, String> f : affiliation.getFunction().getNameByGender(gender).entrySet()){
        function.add(new WebdebProfession(f.getKey(), gender, f.getValue()));
      }
    }
    startDate = affiliation.getStartDate();
    endDate = affiliation.getEndDate();
  }

}
