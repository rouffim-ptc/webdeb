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

import be.webdeb.core.api.actor.EActorType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_actor_type database table holding the available actor types
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.actor.EActorType
 */
@Entity
@Table(name = "t_actor_type")
@CacheBeanTuning
public class TActorType extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TActorType> find = new Model.Finder<>(TActorType.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idActorType;

  @Column(name = "name", nullable = false, length = 20)
  private String name;

  @OneToMany(mappedBy = "actortype", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Actor> actors;

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the actor type id
   *
   * @return a type id
   */
  public int getIdActorType() {
    return idActorType;
  }

  /**
   * Set the actor type id
   *
   * @param idActorType a type id
   */
  public void setIdActorType(int idActorType) {
    this.idActorType = idActorType;
  }

  /**
   * Get the identifying name
   *
   * @return a name
   */
  public String getName() {
    return name;
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
   * Get all actors of this type
   *
   * @return a (possibly empty) list of actors
   */
  public List<Actor> getActors() {
    return actors != null ? actors : new ArrayList<>();
  }

  /**
   * Set all actors of this type
   *
   * @param actors a list of actors
   */
  public void setActors(List<Actor> actors) {
    this.actors = actors;
  }

  /**
   * Get the EActorType corresponding to this db actor type
   *
   * @return the EActorType corresponding
   */
  public EActorType getEActorType(){
    return EActorType.value(idActorType);
  }
}
