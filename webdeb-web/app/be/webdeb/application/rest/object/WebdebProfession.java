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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is meant to exchange webdeb professions in json format. All details of an API Profession are represented.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class WebdebProfession {

  /**
   * profession name in this.lang
   */
  @JsonSerialize
  private String name;

  /**
   * 2-char iso-639-1 code of the language associated to this profession name
   */
  @JsonSerialize
  private String lang;

  /**
   * char code of the gender of this profession name
   */
  @JsonSerialize
  private String gender;

  /**
   * @api {get} WebdebProfession Actor profession
   * @apiName Profession names
   * @apiGroup Structures
   * @apiDescription Actor's profession in a affiliations.
   * @apiSuccess {String} name profession name
   * @apiSuccess {String} lang iso-639-1 language code associated to this profession name
   * @apiSuccess {String} gender gender code associated to this profession name
   * @apiExample Profession in affiliation structures
   * { "name": "businessperson", "lang": "en", "gender":"N" }
   * @apiVersion 0.0.1
   */
  public WebdebProfession(String lang, String gender, String name) {
    this.lang = lang;
    this.gender = gender;
    this.name = name;
  }
}
