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

import be.webdeb.core.api.actor.EAffiliationType;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the t_affiliation_type database table holding available affiliation types for organizations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see ActorHasAffiliation
 */
@Entity
@Table(name = "t_affiliation_type")
@CacheBeanTuning
public class TAffiliationType extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TAffiliationType> find = new Model.Finder<>(TAffiliationType.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idType;

  @Column(name = "id_actor_type", nullable = false)
  private Integer actorType;

  @Column(name = "id_subtype", nullable = false)
  private Integer subtype;

  /**
   * Get the type id
   *
   * @return an affiliation type id
   */
  public int getIdType() {
    return idType;
  }

  /**
   * Set the affiliation type
   *
   * @param idType the affiliation type
   */
  public void setIdType(int idType) {
    this.idType = idType;
  }

  /**
   * Get the actor type of the ActorHasAffiliation left actor
   *
   * @return an affiliation actor type id
   */
  public int getActorType() {
    return actorType;
  }

  /**
   * Set the actor type of the ActorHasAffiliation left actor
   *
   * @param actorType the affiliation actor type ide
   */
  public void setActorType(int actorType) {
    this.actorType = actorType;
  }

  /**
   * Get the affiliation subtype (is an affiliation, filiation, affiliated (must not be used in actor has affiliation))
   *
   * @return an affiliation subtype id
   */
  public int getSubtype() {
    return subtype;
  }

  /**
   * Set the affiliation subtype (is an affiliation, filiation, affiliated (must not be used in actor has affiliation))
   *
   * @param subtype the affiliation subtype
   */
  public void setSubtype(int subtype) {
    this.subtype = subtype;
  }

  /**
   * Get the EAffiliationType corresponding to this db affiliation type
   *
   * @return the EAffiliationType corresponding
   */
  public EAffiliationType getEAffiliationType(){
    return EAffiliationType.value(idType);
  }

}
