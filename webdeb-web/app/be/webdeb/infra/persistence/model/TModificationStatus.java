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
 * The persistent class for the t_modification_status database table, used to trace type of modifications
 * made to contributions
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contribution.EModificationStatus
 */
@Entity
@CacheBeanTuning
@Table(name = "t_modification_status")
@Unqueryable
public class TModificationStatus extends TechnicalTable {

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idStatus;

  @Column(name = "name", nullable = false, length = 20)
  private String name;

  @OneToMany(mappedBy = "status", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<ContributionHasContributor> contributions;

  /**
   * Finder to access predifined values
   */
  public static final Model.Finder<Integer, TModificationStatus> find = new Model.Finder<>(TModificationStatus.class);

  /**
   * Get the modification status id
   *
   * @return an id
   * @see be.webdeb.core.api.contribution.EModificationStatus
   */
  public int getIdStatus() {
    return this.idStatus;
  }

  /**
   * Set the modification status id
   *
   * @param idStatus an id
   * @see be.webdeb.core.api.contribution.EModificationStatus
   */
  public void setIdStatus(int idStatus) {
    this.idStatus = idStatus;
  }

  /**
   * Get the identifying name
   *
   * @return a name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the identifying name
   *
   * @param name a name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the list of ContributionHasContributor joint objects with this status, ie, the list of
   * bindings between contributions, their contributors and timestamps.
   *
   * @return a list of joint objects
   */
  public List<ContributionHasContributor> getContributions() {
    return contributions;
  }

  /**
   * Set the list of ContributionHasContributor joint objects with this status, ie, the list of
   * bindings between contributions, their contributors and timestamps.
   *
   * @param contributions a list of joint objects
   */
  public void setContributions(List<ContributionHasContributor> contributions) {
    this.contributions = contributions;
  }
}
