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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_contribution_type database table, defining the concrete type of contribution
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contribution.EContributionType
 */
@Entity
@Table(name = "t_contribution_type")
@CacheBeanTuning
public class TContributionType extends TechnicalTable {

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idContributionType;

  @Column(name = "name", nullable = false, length = 20)
  private String name;

  @OneToMany(mappedBy = "contributionType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Contribution> contributions;

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TContributionType> find = new Model.Finder<>(TContributionType.class);


  /*
   * GETTERS / SETTERS
   */

  /**
   * Get this type id
   *
   * @return an id
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public int getIdContributionType() {
    return idContributionType;
  }

  /**
   * Set the type id
   *
   * @param idContributionType a type id
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public void setIdContributionType(int idContributionType) {
    this.idContributionType = idContributionType;
  }

  /**
   * Get the identifying name of this type
   *
   * @return the identifying name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the identifying name of this type
   *
   * @param name the identifying name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the list of contributions of this type
   *
   * @return a (possibly empty) list of contributions
   */
  public List<Contribution> getContributions() {
    return contributions != null ? contributions : new ArrayList<>();
  }

  /**
   * Set the list of contribution of this type
   *
   * @param contributions a list of contributions
   */
  public void setContributions(List<Contribution> contributions) {
    this.contributions = contributions;
  }

  /**
   * Get the EContributionType corresponding to this db contribution type
   *
   * @return the EContributionType corresponding
   */
  public EContributionType getEContributionType(){
    return EContributionType.value(idContributionType);
  }
}
