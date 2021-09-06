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

package be.webdeb.core.api.contribution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds values for warned word type, where the warned word must be analyzed.
 * <ul>
 *   <li>BEGIN the start of the sentence</li>
 *   <li>END the end of the sentence</li>
 *   <li>ALL for all the sentence</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EWarnedWordType {
  /**
   * Warned word for the begins of the sentence
   */
  BEGIN(0),
  /**
   * Warned word for the end of the sentence
   */
  END(1),
  /**
   * Warned word for all the sentence
   */
  ALL(2);

  private int id;
  private static Map<Integer, EWarnedWordType> map = new LinkedHashMap<>();

  static {
    for (EWarnedWordType type : EWarnedWordType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a warned word context
   */
  EWarnedWordType(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EWarnedWordType
   * @return the EWarnedWordType enum value corresponding to the given id, null otherwise.
   */
  public static EWarnedWordType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this context
   */
  public int id() {
    return id;
  }
}
