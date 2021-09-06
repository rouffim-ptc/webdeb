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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This enumeration holds the type of actors.
 * <ul>
 *   <li>-1 for UNKNOWN</li>
 *   <li>0 for PERSON</li>
 *   </li>1 for ORGANIZATION</li>
 *   </li>1 for PROJECT, BRAND, EVENT</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EActorType {

  /**
   * unknown actor type
   */
  UNKNOWN(-1),

  /**
   * individual person
   */
  PERSON(0),

  /**
   * organization such as an enterprise, a public administration, etc.
   */
  ORGANIZATION(1),

  /**
   * Project, brand, event
   */
  PROJECT(2);

  private int id;
  private static Map<Integer, EActorType> map = new LinkedHashMap<>();

  static {
    for (EActorType type : EActorType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an actor type
   */
  EActorType(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EActorType
   * @return the EActorType enum value corresponding to the given id, null otherwise.
   */
  public static EActorType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EActorType
   */
  public int id() {
    return id;
  }

  /**
   * Get the list of EActorType ids
   *
   * @return a list of EActorType ids
   */
  public static List<Integer> getActorTypeIds() {
    return map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
  }
}
