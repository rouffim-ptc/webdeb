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

package be.webdeb.core.api.contribution.place;

import java.util.Map;

/**
 * This interface represents a place in the webdeb system.
 *
 * @author Martin Rouffiange
 */
public interface Place{

  /**
   * Get the place id
   *
   * @return the place id
   */
  Long getId();

  /**
   * Get the geoname id of this place (from geonames.org api)
   *
   * @return the geoname id
   */
  public Long getGeonameId();

  /**
   * Get the 3-char ISO 3166 code of the place
   *
   * @return the 3-char ISO 3166 code
   */
  public String getCode();

  /**
   * Get the latitudeof the place
   *
   * @return the latitude
   */
  String getLatitude();

  /**
   * Get the longitude of the place
   *
   * @return the longitude
   */
  String getLongitude();

  /**
   * Get the continent of the place
   *
   * @return the continent of the place
   */
  Place getContinent();

  /**
   * Set the continent of the place
   *
   * @param continent the continent of the place
   */
  void setContinent(Place continent);

  /**
   * Get the country of the place
   *
   * @return the country of the place
   */
  Place getCountry();

  /**
   * Set the of the place
   *
   * @param country the country of the place
   */
  void setCountry(Place country);

  /**
   * Get the region of the place
   *
   * @return the region of the place
   */
  Place getRegion();

  /**
   * Set the of the place
   *
   * @param region the region of the place
   */
  void setRegion(Place region);

  /**
   * Get the sub-region of the place
   *
   * @return the sub-region of the place
   */
  Place getSubregion();

  /**
   * Set the sub-region of the place
   *
   * @param subregion the sub-region of the place
   */
  void setSubregion(Place subregion);

  /**
   * Get the names of the place
   *
   * @return the place names
   */
  Map<String, String> getNames();

  /**
   * Add a name of the place
   *
   * @param name a place name
   */
  void addName(String name, String lang);

  /**
   * Get the name of the place for the specified language
   *
   * @param lang a two-char ISO-639-1 code representing the language for the name
   */
  String getName(String lang);

  /**
   * Get the default lang of the place
   *
   */
  String getDefaultLang(String lang);

  /**
   * Get the default name of the place
   *
   */
  String getDefaultName();

  /**
   * Get the complete place name
   *
   * @param lang a two-char ISO-639-1 code representing the language for the name
   * @return the complete place name
   */
  String getCompletePlacename(String lang);

  /**
   * Get the type of the place
   *
   * @return a PlaceType object
   *
   * @see PlaceType
   */
  PlaceType getPlaceType();

  /**
   * Set the type of the place
   *
   * @param placeType a PlaceType object
   *
   * @see PlaceType
   */
  void setPlaceType(PlaceType placeType);


}
