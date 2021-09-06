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

import be.webdeb.core.api.actor.Person;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is devoted to exchange Person in JSON format. All details of an API Person are represented.
 * Links to other contributions are represented by their ids. Enumerated (predefined) data are passed as typed objects
 * with their ids and i18n descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebPerson extends WebdebActor {

  /**
   * Gender of this person (M or F, may be null)
   */
  @JsonSerialize
  protected String gender;

  /**
   * date of birth in ((DD/)MM/)YYYY format (D and M optional)
   */
  @JsonSerialize
  protected String birthdate;

  /**
   * date of death in ((DD/)MM/)YYYY format (D and M optional)
   */
  @JsonSerialize
  protected String deathdate;

  /**
   * list of nationalities (in iso 3166-1 alpha-2)
   */
  @JsonSerialize
  protected String residence;

  /**
   * @api {get} WebdebPerson Individual actor
   * @apiName Person
   * @apiGroup Structures
   * @apiDescription A person is an actor being an individual, it contains all fields from a WebdebActor.
   * @apiSuccess {String} gender id (either F for female or M for male) (optional)
   * @apiSuccess {String} birthdate the actor's date of birth in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)
   * @apiSuccess {String} deathdate the actor's date of death in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)
   * @apiSuccess {String} residence country code of this person's country of residence (in iso 3166-1 alpha-2)
   * @apiExample Individual person
   * {
   *   "id": 768,
   *   "type": "actor",
   *   "version": 1491377168000,
   *   "name": [
   *     { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "en" },
   *     { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "fr" }
   *   ],
   *   "actorType": "person",
   *   "crossReference": "https://fr.wikipedia.org/wiki/Donald_Trump",
   *   "affiliations": [
   *     {
   *       "id": 265,
   *       "type": "affiliation",
   *       "version": 1464270120000,
   *       "affiliationActor": 769,
   *       "function": [
   *         { "name": "businessperson", "lang": "en" },
   *       ],
   *       "endDate": "-1"
   *     },
   *     {
   *       "id": 266,
   *       "type": "affiliation",
   *       "version": 1486739277000,
   *       "affiliationActor": 749,
   *       "function": [
   *         { "name": "president", "lang": "en", "gender": "M" },
   *         { "name": "président", "lang": "fr", "gender": "M" }
   *       ],
   *       "startDate": "01/2017"
   *     }
   *   ],
   *   "gender": "M",
   *   "birthdate": "14/06/1946",
   *   "residence": "us"
   * }
   * @apiVersion 0.0.3
   */
  public WebdebPerson(Person person) {
    super(person);
    gender = person.getGender() != null ? person.getGender().getCode() : null;
    birthdate = person.getBirthdate();
    deathdate = person.getDeathdate();
    residence = person.getResidence() != null ? person.getResidence().getCode() : null;
  }
}
