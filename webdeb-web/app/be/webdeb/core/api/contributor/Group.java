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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;

/**
 * This interface describes a group to which contributors may belong to
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface Group extends Comparable<Group> {

  /**
   * Get the group unique id
   *
   * @return the group id
   */
  int getGroupId();

  /**
   * Set the group unique id
   *
   * @param id an id to set
   */
  void setGroupId(int id);

  /**
   * Get the name of this group
   *
   * @return a name
   */
  String getGroupName();

  /**
   * Set the name of this group
   *
   * @param name the name to set
   */
  void setGroupName(String name);

  /**
   * Get this group description
   *
   * @return a description
   */
  String getDescription();

  /**
   * Set this group description
   *
   * @param description a description to set
   */
  void setDescription(String description);

  /**
   * Check whether this group is publicly opened for joining
   *
   * @return true if any contributor may join this group without being invited
   */
  boolean isOpen();

  /**
   * Set whether this group is publicly opened for joining
   *
   * @param open true if any contributor may join this group without being invited
   */
  void isOpen(boolean open);

  /**
   * Check whether this group is for a pedagogic use
   *
   * @return true if the group is for a pedagogic use
   */
  boolean isPedagogic();

  /**
   * Set whether this group is for a pedagogic use
   *
   * @param pedagogic true if the group is for a pedagogic use
   */
  void isPedagogic(boolean pedagogic);

  /**
   * Get the type of visibility for members, ie, are their details visible or not
   *
   * @return the type of visibility
   * @see EMemberVisibility
   */
  EMemberVisibility getMemberVisibility();

  /**
   * Set the type of visibility for members, ie, are their details visible or not
   *
   * @param visibility the type of visibility
   * @see EMemberVisibility
   */
  void setMemberVisibility(EMemberVisibility visibility);

  /**
   * Get the type of visibility for contributions, ie, are they visible by other contributors or not
   *
   * @return the type of visibility
   * @see EContributionVisibility
   */
  EContributionVisibility getContributionVisibility();

  /**
   * Set the type of visibility for contributions, ie, are they visible by other contributors or not
   *
   * @param visibility the type of visibility
   * @see EContributionVisibility
   */
  void setContributionVisibility(EContributionVisibility visibility);

  /**
   * Get the color that representing the group
   *
   * @return the color that representing the group
   */
  String getGroupColor();

  /**
   * Set the color that representing the group
   *
   * @param color the color that representing the group
   */
  void setGroupColor(String color);


  /**
   * Determine if background of an element is dark or light
   *
   * @return return true if the background is light
   */
  boolean determineBackgroundLightness();

  /**
   * Get the list of permission keys for this group
   *
   * @return a list of permissions
   * @see EPermission
   */
  List<EPermission> getPermissions();

  /**
   * Set the list of permission keys for this group
   *
   * @param permissions a list of permissions
   * @see EPermission
   */
  void setPermissions(List<EPermission> permissions);

  /**
   * Get the version of this group instance (optimistic locking)
   *
   * @return a timestamp representing the version of this particular instance
   */
  long getVersion();

  /**
   * Set a timestamp as the version number of this object (used for optimistic concurrent-locking)
   *
   * @param version a timestamp (as Date.getTime())
   */
  void setVersion(long version);

  /**
   * Set this group as the default for given contributor, contributor must be part of group (persisted in database)
   *
   * @param contributor a contributor id
   */
  void setDefaultFor(Long contributor) throws PersistenceException;

  /**
   * Add given contributor as new member of this group, will update member role if given member was already
   * registered in this group.
   *
   * @param member the member id to add
   * @param role the role for the new member
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  void addMember(Long member, EContributorRole role) throws PersistenceException;

  /**
   * Invite user to join this group. If given email exists already, simply notifies existing contributor, otherwise,
   * send an invitation to join platform and group. A new empty contributor will be created as a side-effect.
   *
   * @param email the member email to add
   * @param role the role for the new member
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  void inviteMember(String email, EContributorRole role) throws PersistenceException;

  /**
   * Set banned state of given member in this group
   *
   * @param member the member id to ban or not
   * @param banned true if given member must be banned, false if it must be re-accepted
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  void setBanned(Long member, boolean banned) throws PersistenceException;

  /**
   * Remove a user from this group, used by users that want to leave a group..
   *
   * @param member the member id to remove
   * @return true if given member has been removed from group, false otherwise
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   */
  boolean removeMember(Long member) throws PersistenceException;

  /**
   * Get the list of members of this group
   *
   * @return a (possibly empty) list of contributors being members of this group
   */
  List<GroupSubscription> getMembers();

  /**
   * Get a member of this group by given id
   *
   * @param contributor the id of the contributor to retrieve
   * @return the contributor's subscription details
   */
  GroupSubscription getMember(Long contributor);

  /**
   * Get the list of group owners
   *
   * @return the list of contributors being owners of this group
   */
  List<GroupSubscription> getGroupOwners();

  /**
   * Persist this group into database
   *
   * @param admin the id of the group admin requesting the save action
   * @throws PermissionException if given admin has insufficient rights
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  void save(Long admin) throws PersistenceException, PermissionException;

  /**
   * Clean this group from database: remove all contributions and members subscriptions. If deleteGroup flag is true, also
   * remove subscriptions of admins and owners and delete the group itself.
   * with sufficient privileges.
   *
   * @param admin the id of the group admin requesting the save action
   * @param deleteGroup true if the group itself must also be deleted
   * @return the list of members email addresses that were unsubscribed from this group
   * @throws PermissionException if given admin has insufficient rights
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  List<String> clean(Long admin, boolean deleteGroup) throws PersistenceException, PermissionException;

  /**
   * Get the public group id
   *
   * @return the public group id
   */
  static int getGroupPublic() {
    return 0;
  }

  /**
   * Get the number of contributor in the group
   *
   * @return the number of contributors in the group
   */
  int countNbContributors();

  /**
   * Get the number of contributions in the group
   *
   * @param type the contribution type
   * @return the number of contributions in the group
   */
  int countNbContributions(EContributionType type);

}
