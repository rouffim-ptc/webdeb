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

import be.webdeb.core.api.contribution.place.Place;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is meant to exchange webdeb places in json format. Not all details of an API Place are represented.
 *
 * @author Martin Rouffiange
 */
public class WebdebPlace {

  /**
   * 2-char iso-639-1 code of the language associated to this place name
   */
  @JsonSerialize
  private String lang;

  /**
   * place name in this.lang
   */
  @JsonSerialize
  private String name;

  /**
   * subregion name in this.lang
   */
  @JsonSerialize
  private String subregion;

  /**
   * region name in this.lang
   */
  @JsonSerialize
  private String region;

  /**
   * country name in this.lang
   */
  @JsonSerialize
  private String country;

  /**
   * continent name in this.lang
   */
  @JsonSerialize
  private String continent;

  /**
   * @api {get} WebdebPlace Citation place
   * @apiName Place names
   * @apiGroup Structures
   * @apiDescription Citation's places.
   * @apiSuccess {String} name profession name
   * @apiSuccess {String} lang iso-639-1 language code associated to this profession name
   * @apiSuccess {String} subregion subregion name
   * @apiSuccess {String} region region name
   * @apiSuccess {String} country country name
   * @apiSuccess {String} continent continent name
   * @apiExample Profession in affiliation structures
   * { "lang": "fr", "name": "Namur", "subregion": "Province de Namur", "region": "Wallonie", "country": "Belgique", "continent": "Europe" }
   * @apiVersion 0.0.3
   */
  public WebdebPlace(Place place, String lang) {
    this.lang = place.getDefaultLang(lang);
    this.name = place.getName(lang);
    this.subregion = (place.getSubregion() != null ? place.getSubregion().getName(lang) : null);
    this.region = (place.getRegion() != null ? place.getRegion().getName(lang) : null);
    this.country = (place.getCountry() != null ? place.getCountry().getName(lang) : null);
    this.continent = (place.getContinent() != null ? place.getContinent().getName(lang) : null);
  }
}
