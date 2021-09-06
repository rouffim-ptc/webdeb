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

package be.webdeb.core.api.argument;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for argument shades
 * <pre>
 *   0 for descriptive
 *   1 for prescriptive
 * </pre>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EArgumentShadeType {

  /**
   * argument has not shade type
   */
  NO_SHADE(-1),
  /**
   * arguments like "it's a true fact that..."
   */
  DESCRIPTIVE(0),
  /**
   * arguments like "someone should..."
   */
  PRESCRIPTIVE(1);

  private int id;

  private static Map<Integer, EArgumentShadeType> map = new LinkedHashMap<>();

  static {
    for (EArgumentShadeType type : EArgumentShadeType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an argument shade type
   */
  EArgumentShadeType(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an argument shade type
   * @return the EArgumentShadeType enum value corresponding to the given id, null otherwise.
   */
  public static EArgumentShadeType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this argument shade type
   */
  public int id() {
    return id;
  }

  /**
   * Convert the given argument shade into an argument shade type
   *
   * @return the corresponding argument shade type
   */
  public static EArgumentShadeType argumentShadeToArgumentShadeType(EArgumentShade argumentShade) {
    switch (argumentShade){
      case NO_DOUBT :
        return DESCRIPTIVE;
      case NECESSARY :
        return PRESCRIPTIVE;
      default :
        return NO_SHADE;
    }
  }
}
