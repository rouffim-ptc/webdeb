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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the joint table between contributors and their affiliations. May hold the link
 * to an Actor and a Profession.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "contributor_has_affiliation")
@Unqueryable
public class ContributorHasAffiliation extends Model {

  private static final Model.Finder<Long, ContributorHasAffiliation> find = new Model.Finder<>(ContributorHasAffiliation.class);

  @Id
  @Column(name = "id_cha", unique = true, nullable = false)
  private Long idCha;

  @OneToOne (cascade=CascadeType.PERSIST, fetch=FetchType.LAZY)
  @JoinColumn(name="function", unique= true)
  private Profession function;

  @Column(name = "start_date")
  private String startDate;

  @Column(name = "end_date")
  private String endDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "start_date_type")
  private TPrecisionDateType startDateType;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "end_date_type")
  private TPrecisionDateType endDateType;

  // bi-directional many-to-one association to Contributor
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_contributor", nullable = false)
  private Contributor contributor;

  // bi-directional many-to-one association to Actor
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "id_actor")
  private Actor actor;

  @Version
  private Timestamp version;

  /**
   * Construct a CHA for a given contributor to a given actor (function set to null)
   *
   * @param contributor a contributor id
   */
  public ContributorHasAffiliation(Long contributor) {
    this(contributor, -1L, -1);
  }

  /**
   * Construct a CHA for a given contributor to a given actor with a given function
   *
   * @param contributor a contributor id
   * @param actor       an actor id being the affiliation of the given contributor
   * @param function    the contributor's function at its affiliation actor
   */
  public ContributorHasAffiliation(Long contributor, Long actor, int function) {
    idCha = 0L;
    this.contributor = Contributor.findById(contributor);
    this.actor = Actor.findById(actor);
    this.function = Profession.findById(function);
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the technical id of this contributor's affiliation
   *
   * @return a technical id
   */
  public Long getIdCha() {
    return idCha;
  }

  /**
   * Set the technical id of this contributor's affiliation
   *
   * @param idCha a technical id
   */
  public void setIdCha(Long idCha) {
    this.idCha = idCha;
  }

  /**
   * Get the starting affiliation date, NULL for unset, -1 for "ongoing"
   *
   * @return the ending affiliation date as a string of the form YYYYMMDD where M and D may be zeros and Y may be preceded by a "-"
   */
  public String getStartDate() {
    return startDate;
  }

  /**
   * Set the starting affiliation date, 0 for unset, -1 for "ongoing"
   *
   * @param startDate the ending affiliation date as a string of the form YYYYMMDD where M and D may be zeros and Y may be preceded by "-".
   */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /**
   * Get the ending affiliation date, 0 for unset, -1 for "ongoing"
   *
   * @return the ending affiliation date as a string of the form YYYYMMDD where M and D may be zeros and Y may be preceded by "-".
   */
  public String getEndDate() {
    return endDate;
  }

  /**
   * Set the ending affiliation date, 0 for unset, -1 for "ongoing"
   *
   * @param endDate the starting affiliation date as a string of the form YYYYMMDD where M and D may be zeros and Y may be preceded by "-"
   */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /**
   * Get the start date precision type
   *
   * @return the start date precision type
   */
  public TPrecisionDateType getStartDateType() {
    return startDateType;
  }

  /**
   * Set the start date precision type
   *
   * @param type the start date precision type
   */
  public void setStartDateType(TPrecisionDateType type) {
    this.startDateType = type;
  }

  /**
   * Get the end date precision type
   *
   * @return the start date precision type
   */
  public TPrecisionDateType getEndDateType() {
    return endDateType;
  }

  /**
   * Set the end date precision type
   *
   * @param type the start date precision type
   */
  public void setEndDateType(TPrecisionDateType type) {
    this.endDateType = type;
  }

  /**
   * Get the function of bound contributor in bound affiliation actor, if any
   *
   * @return a profession, or null if unset
   */
  public Profession getFunction() {
    return function;
  }

  /**
   * Set the function of bound contributor in bound affiliation actor
   *
   * @param function a profession
   */
  public void setFunction(Profession function) {
    this.function = function;
  }

  /**
   * Set the function of bound contributor in bound affiliation actor
   *
   * @param function a profession id
   */
  public void setFunction(int function) {
    this.function = Profession.findById(function);
  }

  /**
   * Get the contributor having this affiliation
   *
   * @return a contributor
   */
  public Contributor getContributor() {
    return contributor;
  }

  /**
   * Set the contributor having this affiliation
   *
   * @param contributor  a contributor
   */
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  /**
   * Get the affiliation actor of this contributor
   *
   * @return an actor, may be null if none
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the affiliation actor of this contributor
   *
   * @param actor an affiliation actor
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get the latest moment this affiliation has been updated
   *
   * @return a timestamp of the latest revision of this affiliation
   */
  public Timestamp getVersion() {
    return version;
  }

  /**
   * Set the latest moment this affiliation has been updated
   *
   * @param version a timestamp of the latest revision of this affiliation
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    StringBuilder message = new StringBuilder("affiliation [").append(getIdCha())
        .append("], contributor: ").append(getContributor().getIdContributor());
    if (getActor() != null) {
      message.append(" orga: ").append(getActor().getIdContribution());
    }
    if (getFunction() != null) {
      message.append(", function: ").append(getFunction().getIdProfession());
    }
    message.append(", dates: ").append(getStartDate()).append("-").append(getEndDate());
    return message.append(" [version:").append(getVersion()).append("]").toString();
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve an affiliation by its id
   *
   * @param id the affiliation id
   * @return the ContributorHasAffiliation corresponding to that id, null otherwise
   */
  public static ContributorHasAffiliation findById(Long id) {
    if (id == null || id == -1L) {
      return null;
    }

    return find.byId(id);
  }

  /**
   * Retrieve all affiliation actors by contributor id
   *
   * @param id an actor id
   * @return the list of actors this contributor is affiliated to, null otherwise
   */
  public static List<ContributorHasAffiliation> findByContributor(Long id) {
    return find.where().eq("id_contributor", id).findList();
  }

  /**
   * Find all affiliations for a given function
   *
   * @param profession a profession
   * @return the (possibly empty) list of all ContributorHasAffiliation objects where given profession id appears as affiliated function
   */
  public static List<ContributorHasAffiliation> findAffiliationsByFunction(Profession profession) {
    List<ContributorHasAffiliation> result = find.where().eq("function", profession).findList();
    return result != null ? result : new ArrayList<>();
  }
}
