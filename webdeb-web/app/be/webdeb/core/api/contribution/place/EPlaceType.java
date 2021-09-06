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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for place types
 * <pre>
 *   0 - Continent
 *   1 - World region, union of countries
 *   2 - Country
 *   3 - Region, state
 *   4 - Province, sub-region
 *   5 - City, others kind of place
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EPlaceType {

  /**
   * Place is a continent
   */
  CONTINENT(0),
  /**
   * Place is a world region
   */
  SUBCONTINENT(1),
  /**
   * Place is a country
   */
  COUNTRY(2),
  /**
   * Place is a region
   */
  REGION(3),
  /**
   * Place is a sub-region
   */
  SUBREGION(4),
  /**
   * Place is a city, town or other
   */
  PLACE(5),
  /**
   * Place is smaller than a city
   */
  OTHER(6);

  private int id;
  private static Map<Integer, EPlaceType> map = new LinkedHashMap<>();

  static {
    for (EPlaceType type : EPlaceType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a place type
   */
  EPlaceType(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EPlaceType
   * @return the EPlaceType enum value corresponding to the given id, null otherwise.
   */
  public static EPlaceType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this type
   */
  public Integer id() {
    return id;
  }
}
