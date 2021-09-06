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

package be.webdeb.core.api.text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for text visibilities
 * <pre>
 *   0 for publicly visible
 *   1 for visible for pedagogic usage only
 *   2 for visible only by the creator of the text
 * </pre>
 *
 * @author Fabian Gilson
 */
public enum ETextVisibility {

  /**
   * visible by everyone (no copyright, or "open-access" copyright)
   */
  PUBLIC(0),

  /**
   * visible for pedagogic usage, ie, only by contributors coming from the academic world
   * @see be.webdeb.core.api.contributor.Contributor
   */
  PEDAGOGIC(1),

  /**
   * not visible by any contributor but the creator
   * @see be.webdeb.core.api.contributor.Contributor
   */
  PRIVATE(2);

  private int id;
  private static Map<Integer, ETextVisibility> map = new LinkedHashMap<>();

  static {
    for (ETextVisibility type : ETextVisibility.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a text visibility type
   */
  ETextVisibility(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an ETextVisibility
   * @return the ETextVisibility enum value corresponding to the given id, null otherwise.
   */
  public static ETextVisibility value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this visibility type
   */
  public int id() {
    return id;
  }
}
