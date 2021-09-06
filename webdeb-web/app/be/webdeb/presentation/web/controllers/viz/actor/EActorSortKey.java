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

package be.webdeb.presentation.web.controllers.viz.actor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration represents the sort key for actors
 * <pre>
 *   0 for date
 *   1 for personal excerpts
 *   2 for proportion of similar arguments (# similar / # similarity links)
 *   3 for amount of arguments with a similarity link
 * </pre>
 *
 * @author Fabian Gilson
 */

public enum EActorSortKey {

  /**
   * date of owning text
   */
  DATE(0),

  /**
   * number of own excerpts regarding an argument (ie, own similar arguments)
   */
  EXCERPTS(1),

  /**
   * proportion of similar arguments of a particular argument of related actor (# similar / # similarity links)
   * aka, degree of agreement
   */
  P_SIMILAR(2),

  /**
   * total of similarity links of a particular argument, aka, debate intensity
   */
  T_SIMILAR(3);

  private int id;

  private static Map<Integer, EActorSortKey> map = new LinkedHashMap<>();

  static {
    for (EActorSortKey type : EActorSortKey.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a view key
   */
  EActorSortKey(int id) {
    this.id = id;
  }

  /**
   * Get all available keys
   *
   * @return a collection of EActorViewKey
   */
  public static Collection<EActorSortKey> getAll() {
    return map.values();
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a view key
   * @return the EActorViewKey enum value corresponding to the given id, null otherwise.
   */
  public static EActorSortKey value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this view key
   */
  public int id() {
    return id;
  }
}
