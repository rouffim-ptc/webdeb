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

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.contributor.GroupSubscription;
import play.data.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple form class that contains a list of RoleForm to change member roles
 *
 * @author Martin Rouffiange
 * @see RoleForm
 */
public class ChangeMembersTypeForm {

  private int groupId;
  private String groupName;
  private List<RoleForm> roles = new ArrayList<>();

  /**
   * Play / Json compliant constructor
   */
  public ChangeMembersTypeForm() {
    // needed by json/play
  }

  /**
   * Initialize a change member role form for given group
   *
   * @param group a group api object for which invitations will be sent
   */
  public ChangeMembersTypeForm(Group group) {
    groupId = group.getGroupId();
    groupName = group.getGroupName();
    roles = group.getMembers().stream()
        .filter(m -> m.getContributor().getFirstname() != null || m.getContributor().getLastname() != null)
        .map(RoleForm::new).collect(Collectors.toList());
  }

  /**
   * Initialize a change member role form for given group and user subscription if we only want to change one
   *
   * @param group a group api object for which invitations will be sent
   * @param subscription the user group subscription
   */
  public ChangeMembersTypeForm(Group group, GroupSubscription subscription) {
    groupId = group.getGroupId();
    groupName = group.getGroupName();
    roles.add(new RoleForm(subscription));
  }

  /**
   * Play form validation method
   *
   * @return a list of errors or null if none
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for (int i = 0; i < roles.size(); i++) {
      RoleForm role = roles.get(i);
      if (role.getRoleId() == null || role.getRoleId() <= 0) {
        String field = "roles[" + i + "].roleId";
        errors.put(field, Collections.singletonList(new ValidationError(field, "group.invite.members.role.error")));
      }
    }
    return errors.isEmpty() ? null : errors;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public List<RoleForm> getRoles() {
    return roles;
  }

  public void setRoles(List<RoleForm> roles) {
    this.roles = roles;
  }
}
