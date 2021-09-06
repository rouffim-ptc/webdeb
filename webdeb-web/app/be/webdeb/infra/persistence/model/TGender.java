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
 * The persistent class for the t_gender database table, holding predefined values for genders of
 * individual actor (person) and contributors.
 *
 * @author Fabian Gilson
 */
@Entity
@Table(name = "t_gender")
@CacheBeanTuning
public class TGender extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<String, TGender> find = new Model.Finder<>(TGender.class);

  @Id
  @Column(name = "id_gender", unique = true, nullable = false, length = 1)
  private String idGender;

  @OneToMany(mappedBy = "gender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Unqueryable
  private List<Person> persons;

  @OneToMany(mappedBy = "gender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Unqueryable
  private List<Contributor> contributors;

  /**
   * Get this gender id
   *
   * @return an id
   * @see be.webdeb.core.api.actor.EActorType
   */
  public String getIdGender() {
    return this.idGender;
  }

  /**
   * Set this gender id
   *
   * @param idGender an id
   * @see be.webdeb.core.api.actor.EActorType
   */
  public void setIdGender(String idGender) {
    this.idGender = idGender;
  }

  /**
   * Get the list of persons with this gender
   *
   * @return a (possibly empty) list of persons
   */
  public List<Person> getPersons() {
    return persons != null ? persons : new ArrayList<>();
  }

  /**
   * Set the list of persons of this gender
   *
   * @param persons a list of persons of this gender
   */
  public void setPersons(List<Person> persons) {
    this.persons = persons;
  }

  /**
   * Get the list of contributors with this gender
   *
   * @return a (possibly empty) list of contributors
   */
  public List<Contributor> getContributors() {
    return contributors != null ? contributors : new ArrayList<>();
  }

  /**
   * Set the list of contributors of this gender
   *
   * @param contributors a list of contributors of this gender
   */
  public void setContributors(List<Contributor> contributors) {
    this.contributors = contributors;
  }
}
