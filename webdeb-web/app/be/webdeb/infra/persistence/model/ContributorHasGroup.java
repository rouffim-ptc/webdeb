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

package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the joint table between a group and a contributor. Defines the role of a contributor
 * inside bound group.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name="contributor_has_group")
@Unqueryable
public class ContributorHasGroup extends Model {

  private static final Finder<ContributorHasGroupPK, ContributorHasGroup> find = new Model.Finder<>(ContributorHasGroup.class);

  @EmbeddedId
  private ContributorHasGroupPK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="id_contributor", nullable = false)
  private Contributor contributor;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name="id_group", nullable = false)
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name="id_role", nullable = false)
  private Role role;

  // used to prevent contributor to contribute into this group
  @Column(name = "is_banned")
  private int isBanned;

  // used to not display all subscribed group
  @Column(name = "is_followed")
  private int isFollowed;

  // used when a group is on invitation-only
  @Column(name = "invitation")
  private String invitation;

  @Version
  @Column(name = "version")
  private Timestamp version;

  /**
   * Create a new joint-table object to add a contributor into a group with given role (none may be null)
   *
   * @param contributor a contributor
   * @param group a group object
   * @param role the role object
   * @param token the invitation token
   * @param followed true if the group is followed by the contributor
   */
  ContributorHasGroup(Contributor contributor, Group group, Role role, String token, boolean followed) {
    this.contributor = contributor;
    this.group = group;
    this.role = role;
    isBanned = 0;
    invitation = token;
    this.isFollowed = (followed ? 1 : 0);
  }

  /**
   * Get the complex id
   *
   * @return a complex id
   */
  public ContributorHasGroupPK getId() {
    return this.id;
  }

  /**
   * Set the complex id object
   *
   * @param id a complex id object
   */
  public void setId(ContributorHasGroupPK id) {
    this.id = id;
  }

  /**
   * Get bound contributor
   *
   * @return a contributor
   */
  public Contributor getContributor() {
    return this.contributor;
  }

  /**
   * Set bound contributor
   *
   * @param contributor a contributor
   */
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  /**
   * Get bound group
   *
   * @return a group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * Set bound group
   *
   * @param group a group
   */
  public void setGroup(Group group) {
    this.group = group;
  }

  /**
   * Get the role of bound contributor in bound group
   *
   * @return the contributor's role
   */
  public Role getRole() {
    return role;
  }


  /**
   * Get the EContributorRole corresponding to this db contributor role
   *
   * @return the EContributorRole corresponding
   */
  public EContributorRole getEContributorRole(){
    return EContributorRole.value(role.getIdRole());
  }

  /**
   * Set the role of bound contributor in bound group
   *
   * @param role the contributor's role
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Get the invitation token for bound contributor to join bound group
   *
   * @return the invitation token
   */
  public String getInvitation() {
    return invitation;
  }

  /**
   * Set the invitation token for bound contributor to join bound group
   *
   * @param invitation a universally unique invitation token
   */
  public void setInvitation(String invitation) {
    this.invitation = invitation;
  }

  /**
   * Check whether the contributor is banned from group, ie, may not publish in that group.
   *
   * @return true if linked contributor is banned
   */
  public boolean isBanned() {
    return isBanned == 1;
  }

  /**
   * Set the banned status of linked contributor, preventing him to publish in linked group
   *
   * @param banned true to prevent linked contributor to publish in linked group
   */
  public void isBanned(boolean banned) {
    this.isBanned = banned ? 1 : 0;
  }

  /**
   * Check whether the group is followed by the contributor, followed filter subscribed groups.
   *
   * @return true if group is followed
   */
  public boolean isFollowed() {
    return isFollowed == 1;
  }

  /**
   * Set the followed status for this group.
   *
   * @param followed true if group is followed
   */
  public void isFollowed(boolean followed) {
    this.isFollowed = followed ? 1 : 0;
  }

  /**
   * Get the join date of bound contributor to bound group
   *
   * @return a timestamp
   */
  public Timestamp getVersion() {
    return this.version;
  }

  /**
   * Set the join date of bound contributor to bound group
   *
   * @param version a timestamp
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }


  /*
   * QUERIES
   */

  /**
   * Accessor to subscriptions for a given contributor. Must use this accessor to avoid never ending
   * bi-directional references from groups and members, since we may retrieve groups from members or
   * members from groups.
   *
   * @param contributor a contributor id to look for
   * @return a (possibly empty) list of "contributor has group" joint objects
   */
  public static List<ContributorHasGroup> byContributor(Long contributor) {
    List<ContributorHasGroup> result = find.where().eq("id_contributor", contributor).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Find a contributor by a given invitation token
   *
   * @param token an invitation token
   * @return the contributor with given token as invitation, null if not found
   */
  public static Contributor byInvitation(String token) {
    ContributorHasGroup chg = find.where().eq("invitation", token).findUnique();
    return chg != null ? chg.getContributor() : null;
  }

  /**
   * Find a contributorHasGroup by a given contributor and group
   *
   * @param contributor a contributor id to look for
   * @param contributor a group id where this contributor should belong to
   * @return the contributor has group corresponding to given contributor and group, if found, null otherwise
   */
  public static ContributorHasGroup byContributorAndGroup(Long contributor, int group) {
    return find.where().eq("id_contributor", contributor).and().eq("id_group", group).findUnique();
  }

  /**
   * The primary key class for the group_has_contributor joint table.
   *
   * @author Fabian Gilson
   */
  @Embeddable
  public static class ContributorHasGroupPK extends Model {

    @Column(name="id_group", insertable = false, updatable = false)
    private Integer idGroup;

    @Column(name="id_contributor", insertable = false, updatable = false)
    private Long idContributor;

    @Column(name="id_role", insertable = false, updatable = false)
    private Integer idRole;

    ContributorHasGroupPK(Long contributor, int group, int role) {
      idGroup = group;
      idContributor = contributor;
      idRole = role;
    }

    /**
     * Get the group id
     *
     * @return an id
     */
    public int getIdGroup() {
      return idGroup;
    }

    /**
     * Set the group id
     *
     * @param idGroup an id
     */
    public void setIdGroup(int idGroup) {
      this.idGroup = idGroup;
    }

    /**
     * Get the contributor id
     *
     * @return ac contributor id
     */
    public Long getIdContributor() {
      return idContributor;
    }

    /**
     * Set the contributor id
     *
     * @param idContributor an id
     */
    public void setIdContributor(Long idContributor) {
      this.idContributor = idContributor;
    }

    /**
     * Get the role id for current contributor in current group
     *
     * @return a role id
     */
    public Integer getIdRole() {
      return idRole;
    }

    /**
     * Set the role id for current contributor in current group
     *
     * @param idRole a role id
     */
    public void setIdRole(Integer idRole) {
      this.idRole = idRole;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ContributorHasGroupPK)) {
        return false;
      }
      ContributorHasGroupPK castOther = (ContributorHasGroupPK)other;
      return
          this.idGroup.equals(castOther.idGroup)
              && this.idContributor.equals(castOther.idContributor)
              && this.idRole.equals(castOther.idRole);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.idGroup.hashCode();
      hash = hash * prime + this.idContributor.hashCode();
      hash = hash * prime + this.idRole.hashCode();

      return hash;
    }
  }
}
