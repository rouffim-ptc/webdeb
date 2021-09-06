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
import be.webdeb.core.api.actor.EAffiliationType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The persistent class for the joint table between an actor and his affiliations. May hold a link to another actor
 * (organization or unknown subtype), start and end dates and possibly a link to a Profession.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "actor_has_affiliation")
public class ActorHasAffiliation extends Model {

  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, ActorHasAffiliation> find = new Model.Finder<>(ActorHasAffiliation.class);

  @Id
  @Column(name = "id_aha", unique = true, nullable = false)
  private Long idAha;

  @OneToOne (fetch=FetchType.LAZY)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_actor", nullable = false)
  private Actor actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_actor_as_affiliation", nullable = false)
  private Actor affiliation;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "type")
  private TAffiliationType type;

  @Version
  @Column(name = "version")
  @Unqueryable
  private Timestamp version;

  /**
   * Constructor, creates a default affiliation for given actor
   *
   * @param actor an actor
   */
  public ActorHasAffiliation(Long actor) {
    this(actor, -1L, -1);
  }

  /**
   * Create an ActorHasAffiliation from a given actor, affiliation and function
   *
   * @param actor an actor
   * @param affiliation an actor as the affiliation
   * @param function the function (profession) id of the actor at the affiliation
   */
  public ActorHasAffiliation(Long actor, Long affiliation, int function) {
    idAha = 0L;
    this.actor = Actor.findById(actor);
    this.affiliation = Actor.findById(affiliation);
    this.function = Profession.findById(function);
    startDate = null;
    endDate = null;
    startDateType = null;
    endDateType = null;
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the technical id
   *
   * @return the technical id
   */
  public Long getId() {
    return idAha;
  }

  /**
   * Set the technical id
   *
   * @param id the technical id
   */
  public void setId(Long id) {
    idAha = id;
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
   * Get the function of bound actor in bound affiliation actor, if any
   *
   * @return a profession, or null if unset
   */
  public Profession getFunction() {
    return function;
  }

  /**
   * Set the function of bound actor in bound affiliation actor
   *
   * @param function a profession
   */
  public void setFunction(Profession function) {
    this.function = function;
  }

  /**
   * Set the function of bound actor in bound affiliation actor
   *
   * @param function a profession id
   */
  public void setFunction(int function) {
    this.function = Profession.findById(function);
  }

  /**
   * Get the affiliated actor
   *
   * @return an actor
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the affiliated actor
   *
   * @param actor an actor having an affiliation
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get affiliation actor
   *
   * @return the affiliation actor if any, null otherwise
   */
  public Actor getAffiliation() {
    return affiliation;
  }

  /**
   * Set the affiliation actor
   *
   * @param affiliation the affiliation actor
   */
  public void setAffiliation(Actor affiliation) {
    this.affiliation = affiliation;
  }

  /**
   * Get the affiliation type of this affiliation. Always null for persons
   *
   * @return the affiliation type
   */
  public TAffiliationType getType() {
    return type;
  }

  /**
   * Set the affiliation type of this affiliation.
   *
   * @param type the affiliation type
   */
  public void setType(TAffiliationType type) {
    this.type = type;
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
    // because of lazy load, must explicitly call getter
    StringBuilder message = new StringBuilder("affiliation: [").append(getId())
        .append("], actor: ").append(getActor().getIdContribution());
    if (getType() != null) {
      message.append(", type: ").append(getType().getEn());
    }
    if (getAffiliation() != null) {
      message.append(", orga: ").append(getAffiliation().getIdContribution());
    }
    if (getFunction() != null) {
      message.append(", function: ").append(getFunction().getIdProfession());
    }
    message.append(", dates: ").append(getStartDate()).append("-").append(getEndDate());
    return message.append(", version: [").append(getVersion()).append("]").toString();
  }

  /**
   * Delete this affiliation and remove links to all contribution_has_actor to this affiliation
   *
   * @return true if the deletion succeeded, false otherwise
   */
  @Override
  public boolean delete() {
    // reset to null all contribution_has_actor binding to this affiliation
    ContributionHasActor.findByAffiliation(idAha).forEach(cha -> {
      cha.setActorIdAha(null);
      cha.update();
    });

    return super.delete();
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve an ActorHasAffiliation by its id
   *
   * @param id the ActorHasAffiliation id
   * @return the ActorHasAffiliation corresponding to that id, null otherwise
   */
  public static ActorHasAffiliation findById(Long id) {
    if (id == null || id == -1L) {
      return null;
    }
    return find.byId(id);
  }

  /**
   * Find all affiliated actor for a given affiliation actor (via id_actor_as_affiliation), i.e.,
   * all affiliations where given id is recorded as the affiliation
   *
   * @param id an actor id
   * @param type the actor type for affiliated
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliation
   */
  public static List<ActorHasAffiliation> findAffiliated(Long id, EActorType type) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha " +
            "left join actor a on a.id_contribution = aha.id_actor " +
            "where a.actortype = " +  type.id() + " and aha.id_actor_as_affiliation = " + id + " and " + filterOnAffType() +
            " order by aha.start_date";
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results == null ? new ArrayList<>() : results;
  }

  /**
   * Find the order of affiliated actor of a given one by actor type
   *
   * @param id an actor id
   * @param type the actor type for affiliation
   * @return the (possibly empty) list of actor id
   */
  public static List<Long> sortAffiliatedActor(Long id, EActorType type) {
    String select = "SELECT id_actor," +
            "if(start_date is not null && end_date is not null, " +
            "(max(CONVERT(SUBSTRING_INDEX(end_date,'-',-1),UNSIGNED INTEGER)) - min(CONVERT(SUBSTRING_INDEX(start_date,'-',-1),UNSIGNED INTEGER))), " +
            "-1) as 'sum' " +
            "FROM webdeb.actor_has_affiliation aha " +
            "left join actor a on a.id_contribution = aha.id_actor " +
            "left join contribution c on a.id_contribution = c.id_contribution " +
            "where a.actortype = " +  type.id() + " and aha.id_actor_as_affiliation = " + id + " and " + filterOnAffType()  +
            " group by aha.id_actor order by end_date desc, sum desc, start_date, c.sortkey";
    return Ebean.createSqlQuery(select).findList().stream().map(e -> e.getLong("id_actor")).collect(Collectors.toList());
  }

  /**
   * Find all affiliations for a given affiliation actor for a given affiliation type
   *
   * @param id an actor id
   * @param type an affiliation type
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliated actor
   */
  public static List<ActorHasAffiliation> findAffiliated(Long id, EAffiliationType type) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha where aha.id_actor_as_affiliation = " + id + " and aha.type = " + type.id();
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results != null ? results : new ArrayList<>();
  }

  /**
   * Find all affiliations for a given affiliation actor
   *
   * @param id an actor id
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliated actor
   */
  public static List<ActorHasAffiliation> findAllAffiliations(Long id) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha where aha.id_actor = " + id;
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results != null ? results : new ArrayList<>();
  }

  /**
   * Find all affiliations (but not graduating and filiations) for a given affiliation actor
   *
   * @param id an actor id
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliated actor
   */
  public static List<ActorHasAffiliation> findAffiliations(Long id) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha where aha.id_actor = " + id + " and " + filterOnAffType();
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results != null ? results : new ArrayList<>();
  }

  /**
   * Find all affiliations for a given affiliation actor for a given affiliation type
   *
   * @param id an actor id
   * @param type an affiliation type
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliated actor
   */
  public static List<ActorHasAffiliation> findAffiliations(Long id, EAffiliationType type) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha where aha.id_actor = " + id + " and aha.type = " + type.id();
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results != null ? results : new ArrayList<>();
  }

  /**
   * Find all affiliations for a given affiliation actor
   *
   * @param id an actor id
   * @param type the actor type for affiliation
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given actor id appears as affiliated actor
   */
  public static List<ActorHasAffiliation> findAffiliations(Long id, EActorType type) {
    String select = "SELECT aha.id_aha FROM webdeb.actor_has_affiliation aha " +
            "left join actor a on a.id_contribution = aha.id_actor_as_affiliation " +
            "where a.actortype = " +  type.id() + " and aha.id_actor = " + id + " and " + filterOnAffType() +
            " order by aha.start_date";
    List<ActorHasAffiliation> results = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return results == null ? new ArrayList<>() : results;
  }

  /**
   * Find the order of affiliation actor of a given one by actor type
   *
   * @param id an actor id
   * @param type the actor type for affiliation
   * @return the (possibly empty) list of actor id
   */
  public static List<Long> sortAffiliationActor(Long id, EActorType type) {
    String select = "SELECT id_actor_as_affiliation," +
            "if(start_date is not null && end_date is not null, " +
            "(max(CONVERT(SUBSTRING_INDEX(end_date,'-',-1),UNSIGNED INTEGER)) - min(CONVERT(SUBSTRING_INDEX(start_date,'-',-1),UNSIGNED INTEGER))), " +
            "-1) as 'sum' " +
            "FROM webdeb.actor_has_affiliation aha " +
            "left join actor a on a.id_contribution = aha.id_actor_as_affiliation " +
            "left join contribution c on a.id_contribution = c.id_contribution " +
            "where a.actortype = " +  type.id() + " and aha.id_actor = " + id + " and " + filterOnAffType()  +
            " group by aha.id_actor_as_affiliation order by end_date desc, sum desc, start_date, c.sortkey";
    return Ebean.createSqlQuery(select).findList().stream().map(e -> e.getLong("id_actor_as_affiliation")).collect(Collectors.toList());
  }

  /**
   * Find all affiliations for a given function
   *
   * @param profession a profession
   * @return the (possibly empty) list of all ActorHasAffiliation objects where given profession id appears as affiliated function
   */
  public static List<ActorHasAffiliation> findAffiliationsByFunction(Profession profession) {
    List<ActorHasAffiliation> result = find.where().eq("function", profession).findList();
    return result != null ? result : new ArrayList<>();
  }

  private static String filterOnAffType(){
    return "(aha.type is null or (aha.type != " + EAffiliationType.GRADUATING_FROM.id() + " and aha.type != " + EAffiliationType.SON_OF.id() + "))";
  }

}
