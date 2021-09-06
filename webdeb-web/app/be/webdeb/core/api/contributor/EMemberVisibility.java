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
 * This enumeration holds values for members' details visibility in a group (always group specific).
 * Note that if a member is publicly visible from one group, he become publically visible, but his subscription
 * to groups where his visibility is group/private are hidden.
 *
 * <pre>
 *   0 for PUBLIC, ie member's details are visible to anyone being at least a contributor
 *   1 for GROUP, member's details are visible only inside the group to which this visibility is attached to
 *   2 for PRIVATE, member's details are not visible by anyone, except group owners
 * </pre>
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EContributorRole
 * @see be.webdeb.core.api.contributor.Group
 */
public enum EMemberVisibility {

  /**
   * member's details are visible to anyone being at least a contributor
   */
  PUBLIC(0),
  /**
   * member's details are visible only inside the group to which this visibility is attached to
   */
  GROUP(1),
  /**
   * member's details are not visible by anyone, except group owners (EContributorRole)
   */
  PRIVATE(2);

  private int id;
  private static Map<Integer, EMemberVisibility> map = new LinkedHashMap<>();

  static {
    for (EMemberVisibility type : EMemberVisibility.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an member visibility
   */
  EMemberVisibility(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EMemberVisibility
   * @return the EMemberVisibility enum value corresponding to the given id, null otherwise.
   */
  public static EMemberVisibility value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EMemberVisibility
   */
  public int id() {
    return id;
  }
}
