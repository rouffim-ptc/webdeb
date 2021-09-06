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

package be.webdeb.core.api.tag;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types of tag hierarchy codes
 * <pre>
 *   0 when hierarchy is ok
 *   1 when hierarchy already contains a tag
 *   2 when hierarchy is full
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EHierarchyCode {

  /**
   * the hierarchy is ok
   */
  OK(0),
  /**
   * Impossible to add a tag into a hierarchy because it is already in
   */
  ALREADY(1),
  /**
   * Impossible to add a tag into a hierarchy because the hierarchy depth is full (currently max 10)
   */
  FULL_DEPTH(2),
  /**
   * Impossible to add a tag because its hierarchy depth is full (currently max 10)
   */
  NEW_NODE_FULL_DEPTH(3),
  /**
   * Impossible to add a tag into a node beacause it has its maximum of children (currently max 10 children nodes)
   */
  FULL_CHILDREN(4),
  /**
   * Impossible to add a tag into a node beacause it has its maximum of parents (currently max 5 children nodes)
   */
  FULL_PARENTS(5),
  /**
   * Impossible to add a tag into a hierarchy because the given parent or child node is not found
   */
  NOT_FOUND(6),
  /**
   * Impossible to add a new tag in the hierarchy because it is null or it has no id
   */
  NULL_OR_NO_ID(7),
  /**
   * Impossible to add a new tag in the hierarchy as parent of a given one cause its parent is not found
   */
  NO_PARENT(8);

  private int id;

  private static Map<Integer, EHierarchyCode> map = new LinkedHashMap<>();

  static {
    for (EHierarchyCode type : EHierarchyCode.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a hierarchy code
   */
  EHierarchyCode(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a hierarchy code
   * @return the EHierarchyCode enum value corresponding to the given id, null otherwise.
   */
  public static EHierarchyCode value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this hierarchy code
   */
  public int id() {
    return id;
  }
}
