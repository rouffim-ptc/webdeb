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

package be.webdeb.core.impl.contributor;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

import java.util.Date;

/**
 * This class implements the group subscription interface, used to bing contributors to their groups
 *
 * @author Fabian Gilson
 */
class ConcreteGroupSubscription implements GroupSubscription, Comparable<GroupSubscription> {

  private Group group;
  private Contributor contributor;
  private EContributorRole role;
  private Date joinDate;
  private boolean isBanned;
  private boolean isFollowed;
  private boolean isDefault;
  private String invitation;
  private ContributorAccessor accessor;

  ConcreteGroupSubscription(ContributorAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public Group getGroup() {
    return group;
  }

  @Override
  public void setGroup(Group group) {
    this.group = group;
  }

  @Override
  public Contributor getContributor() {
    return contributor;
  }

  @Override
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  @Override
  public EContributorRole getRole() {
    return role;
  }

  @Override
  public void setRole(EContributorRole role) {
    this.role = role;
  }

  @Override
  public Date getJoinDate() {
    return joinDate;
  }

  @Override
  public void setJoinDate(Date joinDate) {
    this.joinDate = joinDate;
  }

  @Override
  public boolean isBanned() {
    return isBanned;
  }

  @Override
  public void isBanned(boolean banned) {
    isBanned = banned;
  }

  @Override
  public boolean isFollowed(){
    return isFollowed;
  }

  @Override
  public void isFollowed(boolean followed){
    isFollowed = followed;
  }

  @Override
  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public void isDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  @Override
  public void updateRole(Long admin) throws PersistenceException, PermissionException {
    accessor.addMemberInGroup(group.getGroupId(), contributor.getId(), role);
  }

  @Override
  public void setInvitation(String invitation) {
    this.invitation = invitation;
  }

  @Override
  public String getInvitation() {
    return invitation;
  }

  @Override
  public String toString() {
    return "subscription to " + group.getGroupName() + "[" + group.getGroupId() + "] (default " + isDefault() + ")"
        + " with role: " + role.name();
  }

  @Override
  public int hashCode() {
    return 67 * (group != null ? group.getGroupId() : 51) * (contributor != null ? contributor.hashCode() : 79);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GroupSubscription)) {
      return false;
    }

    GroupSubscription s = (GroupSubscription) o;
    if (contributor == null || group == null || s.getContributor() == null || s.getGroup() == null) {
      return false;
    }

    return group.getGroupId() == s.getGroup().getGroupId() && contributor.getId().equals(s.getContributor().getId());
  }

  @Override
  public int compareTo(GroupSubscription o) {
    if (o.getContributor() == null || o.getContributor().getFirstname() == null || o.getContributor().getLastname() == null) {
      return -1;
    }

    if (contributor == null || contributor.getLastname() == null || contributor.getFirstname() == null) {
      return 1;
    }

    int result = (contributor.getLastname() + contributor.getFirstname())
        .compareToIgnoreCase(o.getContributor().getLastname() + o.getContributor().getFirstname());

    if (result == 0 && group != null && group.getGroupName() != null && o.getGroup() != null) {
      // same name, check on group name, if any
      result = group.getGroupName().compareToIgnoreCase(o.getGroup().getGroupName());
    }
    return result;
  }
}
