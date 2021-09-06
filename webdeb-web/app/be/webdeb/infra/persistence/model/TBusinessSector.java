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
 * The persistent class for the t_business_sector database table, holding predefined sectors for Organizations
 *
 * @author Fabian Gilson
 */
@Entity
@Table(name = "t_business_sector")
@CacheBeanTuning
public class TBusinessSector extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TBusinessSector> find = new Model.Finder<>(TBusinessSector.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idBusinessSector;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(
      name = "organization_has_sector",
      joinColumns = { @JoinColumn(name = "business_sector", referencedColumnName = "id_type") },
      inverseJoinColumns = { @JoinColumn(name = "id_contribution", referencedColumnName = "id_contribution") }
  )
  @Unqueryable
  private List<Organization> organizations;

  /**
   * Get this sector id
   *
   * @return an id
   */
  public int getIdBusinessSector() {
    return this.idBusinessSector;
  }

  /**
   * Set this sector id
   *
   * @param idBusinessSector an id
   */
  public void setIdBusinessSector(int idBusinessSector) {
    this.idBusinessSector = idBusinessSector;
  }

  /**
   * Get the list of organizations active in this sector
   *
   * @return a (possbibly empty) list of organizations
   */
  public List<Organization> getOrganizations() {
    return organizations != null ? organizations : new ArrayList<>();
  }

  /**
   * Set the list of organizations active in this sector
   *
   * @param organizations a list of organizations
   */
  public void setOrganizations(List<Organization> organizations) {
    this.organizations = organizations;
  }
}
