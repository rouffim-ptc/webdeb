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

import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.stream.Collectors;

/**
 * The persistent class for the person database table, conceptual subtype of an actor.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "person")
public class Person extends WebdebModel {

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Actor actor;

  @Column(name = "birthdate")
  private String birthdate;

  @Column(name = "deathdate")
  private String deathdate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gender")
  private TGender gender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "residence")
  private TCountry residence;

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the person id
   *
   * @return an id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set the person id
   *
   * @param idContribution an id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get the person's date of birth
   *
   * @return the person's date of birth as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public String getBirthdate() {
    return birthdate;
  }

  /**
   * Set the person's date of birth
   *
   * @param birthdate the person's date of birth as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  /**
   * Get the person's date of death
   *
   * @return the person's date of death as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public String getDeathdate() {
    return deathdate;
  }

  /**
   * Set the person's date of birth
   *
   * @param deathdate the person's date of birth as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public void setDeathdate(String deathdate) {
    this.deathdate = deathdate;
  }

  /**
   * Get the gender
   *
   * @return the gender, or null if none
   */
  public TGender getGender() {
    return gender;
  }

  /**
   * Set the gender
   *
   * @param gender the gender
   */
  public void setGender(TGender gender) {
    this.gender = gender;
  }

  /**
   * Get the parent actor object
   *
   * @return the parent actor object
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the parent actor object
   *
   * @param actor the parent actor object
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get the country of residence for this actor
   *
   * @return a territory, may be null
   */
  public TCountry getResidence() {
    return residence;
  }

  /**
   * Set the the country of residence for this actor
   *
   * @param residence a territory
   */
  public void setResidence(TCountry residence) {
    this.residence = residence;
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    StringBuilder builder =  new StringBuilder(", gender: ").append(getGender() != null ? getGender().getIdGender() : "unset")
        .append(", date of birth: ").append(getBirthdate())
        .append(", date of death: ").append(getDeathdate())
        .append(", residence: ").append(residence != null ? residence.getIdCountry() : "unset");

    return builder.toString();
  }
}
