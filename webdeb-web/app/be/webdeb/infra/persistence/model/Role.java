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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the role database table. Roles are globally defined with a set of permissions, those
 * permissions being completed by group-specific permissions
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EContributorRole
 * @see be.webdeb.core.api.contributor.EPermission
 */
@Entity
@CacheBeanTuning
@Table(name="role")
@Unqueryable
public class Role extends Model {

  /**
   * Public finder to retrieve predefined values
   */
  public static final Model.Finder<Integer, Role> find = new Model.Finder<>(Role.class);

  @Id
  @Column(name="id_role")
  private Integer idRole;

  @Column(name="role_name", nullable = false)
  private String roleName;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ContributorHasGroup> contributorsHasGroup;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_has_permission",
      joinColumns = { @JoinColumn(name = "id_role", referencedColumnName = "id_role") },
      inverseJoinColumns = { @JoinColumn(name = "id_permission", referencedColumnName = "id_permission") }
  )
  private List<Permission> permissions;

  /**
   * Get the role id
   *
   * @return an id
   */
  public int getIdRole() {
    return idRole;
  }

  /**
   * Set the role id
   *
   * @param idRole an id
   */
  public void setIdRole(int idRole) {
    this.idRole = idRole;
  }

  /**
   * Get the role identifying name
   *
   * @return this role name
   */
  public String getRoleName() {
    return roleName;
  }

  /**
   * Set this role's name
   *
   * @param roleName an identifying name
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /**
   * Get the list of contributors having this role in a specific group
   *
   * @return a list of ContributorHasGroup joint objects
   */
  public List<ContributorHasGroup> getContributorsHasGroup() {
    return this.contributorsHasGroup;
  }

  /**
   * Set the list of contributors having this role in a specific group
   *
   * @param contributorsHasGroup a list of contributorHasGroup joint-objects
   */
  public void setContributorsHasGroup(List<ContributorHasGroup> contributorsHasGroup) {
    this.contributorsHasGroup = contributorsHasGroup;
  }

  /**
   * Get the list of permissions associated to this role
   *
   * @return a non-empty list of permissions
   */
  public List<Permission> getPermissions() {
    return this.permissions;
  }

  /**
   * Set the list of permission associated to this role
   *
   * @param permissions a list of permissions to associate
   */
  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }

}
