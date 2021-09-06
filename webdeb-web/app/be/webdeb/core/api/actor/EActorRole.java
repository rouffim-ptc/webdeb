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

package be.webdeb.core.api.actor;

import java.lang.reflect.Array;
import java.util.*;

/**
 * This enumeration holds values for actor roles in contributions
 * <pre>
 *   1 for reporter, ie the actor that reported the contribution
 *   2 for source author, it the actor that is the author of the source where this contribution comes from
 * </pre>
 *
 * @author Fabian Gilson
 */
public enum EActorRole {

  /**
   * actor is the author of the contribution (texts and arguments)
   */
  AUTHOR(0),
  /**
   * actor is the reporter of this contribution (for arguments)
   */
  REPORTER(1),
  /**
   * actor is the co-author of the contribution (texts and arguments)
   */
  CO_AUTHOR(2),
  /**
   * actor is just cited in the contribution (texts and arguments)
   */
  CITED(3);

  private int id;
  private static Map<Integer, EActorRole> map = new LinkedHashMap<>();

  static {
    for (EActorRole type : EActorRole.values()) {
      map.put(type.id, type);
    }
  }

   /**
   * Constructor
   *
   * @param id an int representing an actor role
   */
  EActorRole(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EActorRole
   * @return the EActorRole enum value corresponding to the given id, null otherwise.
   */
  public static EActorRole value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EActorRole
   */
  public int id() {
    return id;
  }

  public static List<EActorRole> mainToList() {
    return Arrays.asList(AUTHOR, CITED);
  }
}
