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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements the Group abstraction. It manages a group, ie, invite/revoke members, access all
 * contributors/contributions, etc.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteGroup implements Group {

  private int id;
  private String name;
  private String description;
  private boolean isOpen;
  private boolean isPedagogic;
  private List<EPermission> permissions;
  private List<Long> membersIds;
  private List<Long> contributionIds;
  private EContributionVisibility contributionVisibility;
  private String groupColor = null;
  private EMemberVisibility memberVisibility;
  private ContributorAccessor accessor;
  private long version;

  /**
   * Create a new ConcreteGroup instance
   *
   * @param accessor the contributor accessor
   */
  ConcreteGroup(ContributorAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public int getGroupId() {
    return id;
  }

  @Override
  public void setGroupId(int id) {
    this.id = id;
  }

  @Override
  public String getGroupName() {
    return name;
  }

  @Override
  public void setGroupName(String name) {
    this.name = name != null ? name.trim() : null;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description != null ? description.trim() : null;
  }

  @Override
  public void setDefaultFor(Long contributor) throws PersistenceException {
    if (membersIds == null) {
      initMembers();
    }
    if (membersIds.contains(contributor)) {
      accessor.setDefaultGroupFor(id, contributor);
    }
  }

  @Override
  public void addMember(Long member, EContributorRole role) throws PersistenceException {
    if (membersIds == null) {
      initMembers();
    }
    if (!membersIds.contains(member)) {
      accessor.addMemberInGroup(id, member, role);
    }
  }

  @Override
  public void inviteMember(String email, EContributorRole role) throws PersistenceException {
    accessor.inviteMemberInGroup(id, email, role);
  }

  @Override
  public void setBanned(Long member, boolean banned) throws PersistenceException {
    accessor.setBannedInGroup(id, member, banned);
  }

  @Override
  public boolean removeMember(Long member) throws PersistenceException {
    return accessor.removeMemberFromGroup(id, member);
  }

  @Override
  public List<GroupSubscription> getMembers() {
    List<GroupSubscription> result = accessor.retrieveGroupMembers(id, EContributorRole.CONTRIBUTOR);
    result.sort(Comparator.comparing(a -> a.getContributor().getVersion(), Comparator.reverseOrder()));
    return result;
  }

  @Override
  public GroupSubscription getMember(Long contributor) {
    Contributor c = accessor.retrieve(contributor);
    if (c != null) {
      return c.belongsTo(id);
    }
    return null;
  }

  @Override
  public List<GroupSubscription> getGroupOwners() {
    return accessor.retrieveGroupMembers(id, EContributorRole.OWNER);
  }

  @Override
  public boolean isOpen() {
    return isOpen;
  }

  @Override
  public void isOpen(boolean open) {
    this.isOpen = open;
  }

  @Override
  public boolean isPedagogic() {
    return isPedagogic;
  }

  @Override
  public void isPedagogic(boolean pedagogic) {
    isPedagogic = pedagogic;
  }

  @Override
  public EMemberVisibility getMemberVisibility() {
    return memberVisibility;
  }

  @Override
  public void setMemberVisibility(EMemberVisibility visibility) {
    this.memberVisibility = visibility;
  }

  @Override
  public EContributionVisibility getContributionVisibility() {
    return contributionVisibility;
  }

  @Override
  public void setContributionVisibility(EContributionVisibility visibility) {
    this.contributionVisibility = visibility;
  }

  @Override
  public String getGroupColor(){
    return groupColor;
  }

  @Override
  public void setGroupColor(String color){
    groupColor = color;
  }

  @Override
  public boolean determineBackgroundLightness(){
    List<Integer> rgbColor = hexToRgb();
    Double lightness = getColorLightness(rgbColor);
    return (lightness != null && lightness > 180);
  }

  @Override
  public List<EPermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions(List<EPermission> permissions) {
    this.permissions = permissions;
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public void save(Long admin) throws PersistenceException, PermissionException {
    accessor.save(this, admin);
  }

  @Override
  public List<String> clean(Long admin, boolean deleteGroup) throws PersistenceException, PermissionException {
    List<GroupSubscription> members = getMembers();
    if (!deleteGroup) {
      members.removeIf(m -> m.getRole().id() > EContributorRole.CONTRIBUTOR.id());
    }
    accessor.clean(id, deleteGroup);
    return members.stream()
            .filter(c -> c.getContributor().getEmail() != null)
            .map(m -> m.getContributor().getEmail())
            .collect(Collectors.toList());
  }

  @Override
  public int countNbContributors() {
    return accessor.countNbContributors(id);
  }

  @Override
  public int countNbContributions(EContributionType type) {
    return accessor.countNbContributions(id, type);
  }

  @Override
  public String toString() {
    return "group " + name + "[" + id + "]" + " -- " + description + "-- is open:" + isOpen()
        + " contributions are " + contributionVisibility.name() + " members are " + memberVisibility.name();
  }

  @Override
  public int compareTo(Group o) {
    if (name == null) {
      return 1;
    }
    if (o.getGroupName() == null) {
      return -1;
    }
    return name.compareToIgnoreCase(o.getGroupName());
  }

  @Override
  public int hashCode() {
    return 73 * name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Group && id == ((Group) obj).getGroupId();
  }

  /**
   * Initialize list of members
   */
  private void initMembers() {
    membersIds = accessor.retrieveGroupMembers(id, EContributorRole.CONTRIBUTOR).parallelStream().filter(m ->
        !m.isBanned()).map(s -> s.getContributor().getId()).collect(Collectors.toList());
  }

  /**
   * Convert hexadecimal color value tor rgb color value
   *
   * @return the a List that represent the r, g, b values
   */
  private List<Integer> hexToRgb() {
    List<Integer> rgb = new ArrayList<>();
    if(groupColor != null && groupColor.length() == 6) {
      for (int i = 2; i <= groupColor.length(); i += 2) {
        rgb.add(Integer.parseInt(groupColor.substring(i - 2, i), 16));
      }
    }
    return rgb;
  }

  /**
   * Converts an RGB color value to HSL lightness.
   *
   * @param rgb the red color value, green color value and blue color value
   * @return the lightness in the set [0, 255]
   */
  private Double getColorLightness(List<Integer> rgb){
    if(rgb != null && rgb.size() == 3) {
      return 0.2126*rgb.get(0) + 0.7152*rgb.get(1) + 0.0722*rgb.get(2);
    }
    return null;
  }
}
