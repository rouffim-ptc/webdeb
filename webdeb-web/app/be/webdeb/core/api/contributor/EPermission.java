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
 * This enumeration holds values for permission allowed to contributors (via roles) and groups. They are used to
 * define if contributions are visible (concrete visibility scope in defined in EContributionVisibility), the
 * members's profile visibility (also scoped in conjunction with EMemberVisibility).
 *
 * <pre>
 *   0 for VIEW_CONTRIBUTION (applies to all roles)
 *   1 for ADD_CONTRIBUTION (applies to all roles but viewer)
 *   2 for EDIT_CONTRIBUTION (applies to all roles but viewer)
 *   3 for DELETE_CONTRIBUTION (applies to owners and admin)
 *   4 for VIEW_MEMBERS (applies to all roles but viewer)
 *   5 for ADD_MEMBER (applies to owner and admin)
 *   6 for EDIT_MEMBER (applies to owners and admin)
 *   7 for BLOCK_MEMBER (applies to owners and admin)
 *   8 for MERGE_CONTRIBUTION (applies to owners and admin)
 *   9 for BLOCK_CONTRIBUTOR (applies to owners and admin)
 *   10 for ASSIGN_ROLE (applies to owners and admin)
 *   11 for CREATE_GROUP (applies to admin only)
 *   12 for DELETE_GROUP (applies to admin only)
 *   14 for EDIT_CONTRIBUTION_INGROUP (applies to all roles)
 * </pre>
 *
 * @author Fabian Gilson
 * @see Group
 * @see EPermission
 */
public enum EPermission {

  /**
   * visualize a contribution (may apply to all roles)
   */
  VIEW_CONTRIBUTION(0),
  /**
   * add a new contribution (may apply to all roles but the viewer)
   */
  ADD_CONTRIBUTION(1),
  /**
   * edit any contribution (ie, a contribution created by someone else, may apply to all roles but the viewer)
   */
  EDIT_CONTRIBUTION(2),
  /**
   * delete a contribution (may apply to owners and admins)
   */
  DELETE_CONTRIBUTION(3),
  /**
   * view members' details (may apply to all roles but the viewer)
   */
  VIEW_MEMBERS(4),
  /**
   * add a member into a group (may apply to owners and admins)
   */
  ADD_MEMBER(5),
  /**
   * edit member's details (may apply to owners and admins)
   */
  EDIT_MEMBER(6),
  /**
   * prevent a member to contribute in the group (may apply to owners and admins)
   */
  BLOCK_MEMBER(7),
  /**
   * merge contributions into default public group to make them publicly visible (may apply to owners and admins)
   */
  MERGE_CONTRIBUTION(8),
  /**
   * prevent a contributor to contribute in any group (may apply to owners and admins)
   */
  BLOCK_CONTRIBUTOR(9),
  /**
   * assign role and group permissions (may apply to owners and admins)
   */
  ASSIGN_ROLE(10),
  /**
   * create groups (may apply to admins only)
   */
  CREATE_GROUP(11),
  /**
   * delete groups (may apply to admins only)
   */
  DELETE_GROUP(12),
  /**
   * disable text annotation service
   */
  DISABLE_ANNOTATION(13),
  /**
   * disable argument classification
   */
  DISABLE_CLASSIFICATION(14),
  /**
   * editing contribution only in group
   */
  EDIT_CONTRIBUTION_INGROUP(15);

  private int id;
  private static Map<Integer, EPermission> map = new LinkedHashMap<>();

  static {
    for (EPermission type : EPermission.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an permission
   */
  EPermission(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EPermission
   * @return the EPermission enum value corresponding to the given id, null otherwise.
   */
  public static EPermission value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EPermission
   */
  public int id() {
    return id;
  }
}
