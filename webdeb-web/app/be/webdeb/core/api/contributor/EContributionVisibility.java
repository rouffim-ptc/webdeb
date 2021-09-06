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
 * This enumeration holds values for contribution visibility across the webdeb system. Visibility are applied to groups.
 *
 * <pre>
 *   0 for PUBLIC
 *   1 for GROUP, ie only visible for members of the same group as the one(s) where the contribution is published
 *   2 for PRIVATE, ie only the creator of the contribution and the group owners/admins.
 * </pre>
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EContributorRole
 * @see be.webdeb.core.api.contributor.Group
 */
public enum EContributionVisibility {

  /**
   * contributions are visible to anyone being at least a contributor
   */
  PUBLIC(0),
  /**
   * contributions are only visible to same group's members
   */
  GROUP(1),
  /**
   * contributions are private and only visible by group owners (EContributorRole)
   */
  PRIVATE(2);

  private int id;
  private static Map<Integer, EContributionVisibility> map = new LinkedHashMap<>();

  static {
    for (EContributionVisibility type : EContributionVisibility.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a contribution visibility
   */
  EContributionVisibility(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EContributionVisibility
   * @return the EContributionVisibility enum value corresponding to the given id, null otherwise.
   */
  public static EContributionVisibility value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EContributionVisibility
   */
  public int id() {
    return id;
  }
}
