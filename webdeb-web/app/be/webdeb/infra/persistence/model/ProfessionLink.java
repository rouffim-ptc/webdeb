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

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This persistent class holds available type of links between profession (e.g. equivalence, used to gather multiple functions
 * behind the same 'generic' functions)
 *
 * @author Fabian Gilson
 */

@Entity
@CacheBeanTuning
@Table(name = "profession_link")
public class ProfessionLink extends Model {

  /**
   * Public finder to retrieve predefined values
   */
  public static final Model.Finder<Integer, ProfessionLink> find = new Model.Finder<>(ProfessionLink.class);

  @Id
  @Column(name = "id_link")
  private Integer idLink;

  @Column(name="description", nullable = false)
  private String description;


  /**
   * Get the link id
   *
   * @return an id
   */
  public int getIdLink() {
    return idLink;
  }

  /**
   * Set the link id
   *
   * @param idLink an id
   */
  public void setIdLink(int idLink) {
    this.idLink = idLink;
  }

  /**
   * Get the description of this type of link
   *
   * @return this description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of this type of link
   *
   * @param description a description
   */
  public void setRoleName(String description) {
    this.description= description;
  }
}
