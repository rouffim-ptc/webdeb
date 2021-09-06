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

import java.util.*;

/**
 * This enumeration holds values for contributor roles. Roles describe the global permissions that must be used in
 * conjunction with group permissions: roles gathers a collection of permissions that may be valid inside a group.
 * Group visibility and own permissions may also restrict those default permissions.
 *
 * Roles are organized hierarchically, ie, higher values of roles inherit from previous ones and are predominant to
 * group permissions.
 *
 * <pre>
 *   0 for VIEWER may only access in read-only mode
 *   1 for CONTRIBUTOR may contribute inside linked group, ie, at least add contributions in group
 *   2 for OWNER may edit any contribution, add, invite and block group members and edit group permissions
 *   3 for ADMIN may also create, delete groups, ban users, assign roles to contributors
 * </pre>
 *
 * @author Fabian Gilson
 * @see Group
 */
public enum EContributorRole {

  /**
   * contributor is temporary for now, sign up process is on
   */
  TMP_CONTRIBUTOR(-1),
  /**
   * contributor is just a viewer, may only access in read-only mode
   */
  VIEWER(0),
  /**
   * contributor may contribute inside linked group, ie, at least add contributions in group
   */
  CONTRIBUTOR(1),
  /**
   * group owner, usually may edit any contribution, add, invite and block group members and edit group permissions
   */
  OWNER(2),
  /**
   * super user that may also create, delete groups, ban users, assign roles to contributors. Admin is only valid
   * for default 'public' group.
   */
  ADMIN(3),
  /**
   * all user type, used for admin email
   */
  ALL(4),
  /**
   * only myself, used for admin email
   */
  MYSLEF(5);

  private int id;
  private static Map<Integer, EContributorRole> map = new LinkedHashMap<>();

  static {
    for (EContributorRole role : EContributorRole.values()) {
      map.put(role.id, role);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an contributor role
   */
  EContributorRole(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EContributorRole
   * @return the EContributorRole enum value corresponding to the given id, null otherwise.
   */
  public static EContributorRole value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EContributorRole
   */
  public int id() {
    return id;
  }

  public static List<EContributorRole> roles(){
    return new ArrayList<>(Arrays.asList(VIEWER, CONTRIBUTOR, OWNER, ADMIN));
  }
}
