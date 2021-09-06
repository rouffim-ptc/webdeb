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

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;

/**
 * Simple form class that contains a list of MailForms to invite a list of users
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see RoleForm
 */
public class GroupInvitation {
  @Inject
  protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  @Inject
  protected ContributorFactory contributorFactory = Play.current().injector().instanceOf(ContributorFactory.class);

  private int groupId;
  private String name;
  private List<RoleForm> invitations = new ArrayList<>();


  /**
   * Play / Json compliant constructor
   */
  public GroupInvitation() {
    // needed by json/play
  }

  /**
   * Initialize a invitation form for given group
   *
   * @param group a group api object for which invitations will be sent
   */
  public GroupInvitation(Group group) {
    groupId = group.getGroupId();
    name = group.getGroupName();
  }

  /**
   * Play form validation method
   *
   * @return a list of errors or null if none
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for (int i = 0; i < invitations.size(); i++) {
      RoleForm role = invitations.get(i);
      if (role.getRoleId() == null || role.getRoleId() <= 0) {
        String field = "invitations[" + i + "].roleId";
        errors.put(field, Collections.singletonList(new ValidationError(field, "group.invite.members.role.error")));
      }

      String field = "invitations[" + i + "].invitation";
      Contributor contributor = contributorFactory.retrieveContributor(role.getUserId());
      if (contributor != null && contributor.getLastname() != null) {
        role.setUserId(contributor.getId());
      }else{
        errors.put(field, Collections.singletonList(new ValidationError(field, "group.invite.members.notmember.error")));
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<RoleForm> getInvitations() {
    return invitations;
  }

  public void setInvitations(List<RoleForm> invitations) {
    this.invitations = invitations;
  }
}
