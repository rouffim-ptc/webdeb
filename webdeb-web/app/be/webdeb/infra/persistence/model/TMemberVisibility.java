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
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the t_member_visibility database table. Holds default values regarding
 * members' details visibility (public, group, private)
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "t_member_visibility")
@Unqueryable
public class TMemberVisibility extends Model {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TMemberVisibility> find = new Model.Finder<>(TMemberVisibility.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private Integer id;

  @Column(name = "description", nullable = false)
  private String description;

  @OneToMany(mappedBy = "memberVisibility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Unqueryable
  private List<Group> groups;

  /**
   * Get this member visibility id
   *
   * @return an id
   */
  public int getId() {
    return id;
  }

  /**
   * Set this contribution visibility
   *
   * @param id an id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the full description of this type of visibility
   *
   * @return a description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the full description of this type of visibility
   *
   * @param description a description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the list of groups having this type of visibility
   *
   * @return a (possibly empty) list of groups
   */
  public List<Group> getGroups() {
    return groups != null ? groups : new ArrayList<>();
  }

  /**
   * Set the list of groups having this type of visibility
   *
   * @param groups a list of groups
   */
  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

}
