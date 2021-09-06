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
 * This enumeration holds types for text types
 * <pre>
 *   0 for Discussion
 *   1 for Artistic
 *   2 for Informative
 *   3 for Journalistic
 *   4 for Normative
 *   5 for Opinion
 *   6 for Prospective
 *   7 for Practical
 *   8 for Advertising
 *   9 for Scientific
 *   10 for Other
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum ETextType {

  /**
   * Discussion content
   */
  DISCUSSION(0),
  /**
   * Artistic content
   */
  ARTISTIC(1),
  /**
   * Informative content
   */
  INFORMATIVE(2),
  /**
   * Journalistic content
   */
  JOURNALISTIC(3),
  /**
   * Normative content
   */
  NORMATIVE(4),
  /**
   * Opinion content
   */
  OPINION(5),
  /**
   * Propective content
   */
  PROPECTIVE(6),
  /**
   * Practical content
   */
  PRACTICAL(7),
  /**
   * Advertising content
   */
  ADVERTISING(8),
  /**
   * Scientific content
   */
  SCIENTIFIC(9),
  /**
   * Other content
   */
  OTHER(10);

  private int id;
  private static Map<Integer, ETextType> map = new LinkedHashMap<>();

  static {
    for (ETextType type : ETextType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a text type
   */
  ETextType(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an ETextType
   * @return the ETextType enum value corresponding to the given id, null otherwise.
   */
  public static ETextType value(int id) {
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
