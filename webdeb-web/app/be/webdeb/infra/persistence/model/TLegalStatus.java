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
 * The persistent class for the t_legal_status database table, holding predefined values of legal
 * statuses of Organizations
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "t_legal_status")
public class TLegalStatus extends TechnicalTable {

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idStatus;

  @OneToMany(mappedBy = "legalStatus", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Organization> organizations;

  /**
   * finder utility to access predefined values
   */
  public static final Model.Finder<Integer, TLegalStatus> find = new Model.Finder<>(TLegalStatus.class);

  /**
   * Get the id of this organization legal status
   *
   * @return an id
   */
  public int getIdStatus() {
    return this.idStatus;
  }

  /**
   * Set the id of this organization legal status
   *
   * @param idStatus an id
   */
  public void setIdStatus(int idStatus) {
    this.idStatus = idStatus;
  }

  /**
   * Get the list of organizations of this legal status
   *
   * @return a (possibly empty) list of organizations
   */
  public List<Organization> getOrganizations() {
    return organizations != null ? organizations : new ArrayList<>();
  }

  /**
   * Set the list of organizations of this legal status
   *
   * @param organizations a list of organizations
   */
  public void setOrganizations(List<Organization> organizations) {
    this.organizations = organizations;
  }
}
