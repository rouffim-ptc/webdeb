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

package be.webdeb.core.api.contributor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds values for personal pronoun (I, you, he, ...)
 *
 * <pre>
 *   1 for I, my, mine
 *   2 for you, your, yours
 *   3 for he, his, him
 *   4 for she, her, hers
 *   5 for we, our, ours
 *   6 for you, your, yours
 *   7 for they, them, theirs
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EPronoun {

  /**
   * I, my, mine
   */
  I(1),
  /**
   * you, your, yours
   */
  YOU(2),
  /**
   * he, his, him
   */
  HE(3),
  /**
   * she, her, hers
   */
  SHE(4),
  /**
   * we, our, ours
   */
  WE(5),
  /**
   * you, your, yours
   */
  YOU_2(6),
  /**
   * they, their, theirs
   */
  THEY(7);

  private int id;
  private static Map<Integer, EPronoun> map = new LinkedHashMap<>();

  static {
    for (EPronoun type : EPronoun.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a pronoun
   */
  EPronoun(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EPronoun
   * @return the EPronoun enum value corresponding to the given id, null otherwise.
   */
  public static EPronoun value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EPronoun
   */
  public int id() {
    return id;
  }
}
