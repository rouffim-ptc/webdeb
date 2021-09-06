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
 * The persistent class for the permission database table. Permissions may be Role specific, or Group specific.
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EPermission
 */
@Entity
@CacheBeanTuning
@Table(name="permission")
@Unqueryable
public class Permission extends Model {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, Permission> find = new Model.Finder<>(Permission.class);

  @Id
  @Column(name = "id_permission", unique = true, nullable = false)
  private Integer idPermission;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @ManyToMany(mappedBy = "permissions", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Role> roles;

  /**
   * Get the permission id
   *
   * @return an id
   */
  public int getIdPermission() {
    return this.idPermission;
  }

  /**
   * Set the permission id
   *
   * @param id a permission id
   */
  public void setIdPermission(int id) {
    this.idPermission = id;
  }

  /**
   * Get the identifying name of this permission
   *
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Set identifying name of this permission
   *
   * @param name a name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the description of this permission
   *
   * @return a full-text description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Set the description of this permission
   *
   * @param description a full-text description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the list of roles where this permission is available
   *
   * @return a list of roles
   */
  public List<Role> getRoles() {
    return this.roles;
  }

  /**
   * Set the list of roles where this permission is available
   *
   * @param roles a list of roles
   */
  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }
}
