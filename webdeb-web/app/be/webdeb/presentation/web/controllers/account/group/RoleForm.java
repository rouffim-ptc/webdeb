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

package be.webdeb.presentation.web.controllers.account.group;

import be.webdeb.core.api.contributor.GroupSubscription;

/**
 * Simple form class that contains a role and a mailForm to invitation a member
 *
 * @author Martin Rouffiange
 */
public class RoleForm {

  private Integer roleId = -1;
  private Long userId = -1L;
  private String invitation;

  /**
   * Play / Json compliant constructor
   */
  public RoleForm() {
    // needed by json/play
  }

  /**
   * Initialize a role form from a given subscription
   *
   * @param subscription a group subscription api object
   */
  public RoleForm(GroupSubscription subscription) {
    roleId = subscription.getRole().id();
    userId = subscription.getContributor().getId();
    invitation = subscription.getContributor().getFirstname() + " " +
        subscription.getContributor().getLastname() + " (" +
       subscription.getContributor().getEmail() + ")";
  }

  /**
   * Initialize a role form from a given subscription
   *
   * @param roleId the member role
   * @param userId the id of the user
   * @param name the user name
   */
  public RoleForm(Integer roleId, Long userId, String name) {
    this.roleId = roleId;
    this.userId = userId;
    this.invitation = name;
  }

  public Integer getRoleId() {
    return roleId;
  }

  public void setRoleId(Integer roleId) {
    this.roleId = roleId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getInvitation() {
    return invitation;
  }

  public void setInvitation(String invitation) {
    this.invitation = invitation;
  }
}
