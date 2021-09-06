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

/**
 * Simplified Place name representation
 *
 * @author Martin Rouffiange
 */
public class PlaceNameForm {

  protected Long idPlace;
  protected Long idGeoname;
  protected String lang;
  protected String name;
  protected Boolean isPreferred;

  /**
   * Play / JSON compliant constructor
   */
  public PlaceNameForm() {
    // needed by Play
  }

  /**
   * Construct a simple place name form
   *
   * @param idPlace the place id
   * @param idGeoname the geoname id
   * @param lang the lang of the name
   * @param name the spelling of the name
   */
  public PlaceNameForm(Long idPlace, Long idGeoname, String lang, String name) {
    this.idPlace = idPlace;
    this.idGeoname = idGeoname;
    this.lang = lang;
    this.name = name;
  }

  /**
   * Construct a simple place name form
   *
   * @param lang the lang of the name
   * @param name the spelling of the name
   */
  public PlaceNameForm(String lang, String name) {
    this.lang = lang;
    this.name = name;
  }


  public Long getIdPlace() {
    return idPlace;
  }

  public void setIdPlace(Long idPlace) {
    this.idPlace = idPlace;
  }

  public Long getIdGeoname() {
    return idGeoname;
  }

  public void setIdGeoname(Long idGeoname) {
    this.idGeoname = idGeoname;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getPreferred() {
    return isPreferred;
  }

  public void setPreferred(Boolean preferred) {
    isPreferred = preferred;
  }

  @Override
  public String toString() {
    return lang + " " + name;
  }

}
