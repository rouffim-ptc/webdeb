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

import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.Date;

/**
 * This class describes a subscription of a contributor into a group. Defines it role in a linked group
 * from which his individual permissions are described, superseded by the group permissions. Also,
 * defines if he/she is banned from linked group.
 *
 * @author Fabian Gilson
 */
public interface GroupSubscription extends Comparable<GroupSubscription> {

  /**
   * Get the contributor owning this subscription
   *
   * @return the contributor
   */
  Contributor getContributor();

  /**
   * Set the contributor owning this subscription
   *
   * @param contributor the contributor
   */
  void setContributor(Contributor contributor);

  /**
   * Get the role of contributor that owns this subscription
   *
   * @return a contributor role
   * @see be.webdeb.core.api.contributor.EContributorRole
   */
  EContributorRole getRole();

  /**
   * Get the role of this contributor in this group
   *
   * @param role a contributor role in linked group
   * @see be.webdeb.core.api.contributor.EContributorRole
   */
  void setRole(EContributorRole role);

  /**
   * Get the group where linked contributor has a role
   *
   * @return a group
   */
  Group getGroup();

  /**
   * Set the group where linked contributor has a role
   *
   * @param group a group
   */
  void setGroup(Group group);

  /**
   * Check whether this contributor is banned from linked group
   *
   * @return true if this contributor has been banned from group
   */
  boolean isBanned();

  /**
   * Set whether this contributor is banned from linked group
   *
   * @param banned true if this contributor has been banned from group
   */
  void isBanned(boolean banned);

  /**
   * Check whether the group is followed by the contributor, followed filter subscribed groups.
   *
   * @return true if group is followed
   */
  boolean isFollowed();

  /**
   * Set the followed status for this group.
   *
   * @param followed true if group is followed
   */
  void isFollowed(boolean followed);

  /**
   * Check whether this group is the default one
   *
   * @return true if this group is the default (preferred) one for linked contributor
   */
  boolean isDefault();

  /**
   * Set whether this group is the default one
   *
   * @param isDefault a true if this group is the default (preferred) one for linked contributor
   */
  void isDefault(boolean isDefault);

  /**
   * Get the date when linked contributor joined linked group
   *
   * @return a date
   */
  Date getJoinDate();

  /**
   * Set the date when linked contributor joined linked group
   *
   * @param date the join date
   */
  void setJoinDate(Date date);

  /**
   * Get the invitation token
   *
   * @return the invitation token linked contributor in linked group (may be null)
   */
  String getInvitation();

  /**
   * Set the invitation token
   *
   * @param invitation the invitation token linked contributor in linked group
   */
  void setInvitation(String invitation);

  /**
   * Update role of contributor in current group subscription
   *
   * @param admin the contributor id being an admin or owner of current group
   * @throws PersistenceException if an error occurred during the save action
   * @throws PermissionException if given admin is not allowed to save current subscription
   */
  void updateRole(Long admin) throws PersistenceException, PermissionException;
}
