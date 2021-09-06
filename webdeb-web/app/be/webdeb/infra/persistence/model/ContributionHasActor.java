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

import be.webdeb.core.api.actor.EActorRole;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the joint table between contributions and actors. Materialize the implication of an actor
 * into a contribution (texts and arguments) with its role
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.actor.EActorRole
 */
@Entity
@CacheBeanTuning
@Table(name = "contribution_has_actor")
public class ContributionHasActor extends Model {

  private static final Model.Finder<Long, ContributionHasActor> find = new Model.Finder<>(ContributionHasActor.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_cha", unique = true, nullable = false)
  private Long idCha;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_contribution", nullable = false)
  private Contribution contribution;

  @Column(name = "actor_id_aha")
  private Long actorIdAha;

  @Column(name = "is_author")
  private int isAuthor;

  @Column(name = "is_speaker")
  private int isReporter;

  @Column(name = "is_about")
  private int isAbout;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_actor", nullable = false)
  private Actor actor;

  @Version
  @Column(name = "version")
  private Timestamp version;

  /**
   * Create a contribution has actor
   *
   * @param contribution a contribution id (must exist)
   * @param actor an actor id (must exist)
   */
  public ContributionHasActor(Long contribution, Long actor) {
    this.contribution = Contribution.findById(contribution);
    this.actor = Actor.findById(actor);
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the technical id
   *
   * @return the technical id
   */
  public Long getIdCha() {
    return idCha;
  }

  /**
   * Set the technical id
   *
   * @param idCha a technical id
   */
  public void setIdCha(Long idCha) {
    this.idCha = idCha;
  }

  /**
   * Get technical id of the affiliation of bound actor of this contribution
   *
   * @return technical actor_has_affiliation id
   */
  public Long getActorIdAha() {
    return actorIdAha;
  }

  /**
   * Set technical id of the affiliation of bound actor of this contribution
   *
   * @param actorIdAha technical actor_has_affiliation id
   */
  public void setActorIdAha(Long actorIdAha) {
    this.actorIdAha = actorIdAha;
  }

  /**
   * Get the flag saying if bound actor is an author of bound contribution
   *
   * @return true if bound actor is a an author
   */
  public boolean isAuthor() {
    return isAuthor == 1;
  }

  /**
   * Set the flag saying if bound actor is an author of bound contribution
   *
   * @param isAuthor true if bound actor is a an author
   */
  public void setIsAuthor(boolean isAuthor) {
    this.isAuthor = isAuthor ? 1 : 0;
  }

  /**
   * Get the flag saying if bound actor is a reporter of bound contribution
   *
   * @return true if bound actor is a a reporter
   */
  public boolean isReporter() {
    return isReporter == 1;
  }

  /**
   * Set the flag saying if bound actor is a reporter of bound contribution
   *
   * @param isReporter true if bound actor is a a reporter
   */
  public void setIsReporter(boolean isReporter) {
    this.isReporter = isReporter ? 1 : 0;
  }

  /**
   * Get the flag saying if contribution is about bounded actor
   *
   * @return true if the contribution is about bounded actor
   */
  public boolean isAbout() {
    return isAbout == 1;
  }

  /**
   * Set the flag saying if contribution is about bounded actor
   *
   * @param isAbout true if the contribution is about bounded actor
   */
  public void setIsAbout(boolean isAbout) {
    this.isAbout = isAbout ? 1 : 0;
  }

  /**
   * Get the actor having a role in bound contribution
   *
   * @return an actor
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the actor having a role in bound contribution
   *
   * @param actor an actor
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get the contribution where bound actor plays a role
   *
   * @return a contribution
   */
  public Contribution getContribution() {
    return contribution;
  }

  /**
   * Set the contribution where bound actor plays a role
   *
   * @param contribution a contribution
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get the latest revision time when this binding actor-contribution was updated
   *
   * @return a timestamp
   */
  public Timestamp getVersion() {
    return version;
  }

  /**
   * Set the latest revision time when this binding actor-contribution was updated
   *
   * @param version a timestamp
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    return new StringBuffer("contribution_has_actor: [").append(getIdCha())
        .append("], contribution: ").append(getContribution().getIdContribution())
        .append(", actor: ").append(getActor().getIdContribution()).append(", affiliation: ").append(getActorIdAha())
        .append(", roles: author ").append(isAuthor()).append(", reporter ").append(isReporter())
        .append(", is about ").append(isAbout())
        .append(", version: [").append(getVersion()).append("]").toString();
  }

  /*
   * QUERIES
   */

  /**
   * Find a binding contribution-actor by its id
   *
   * @param id an id
   * @return a contribution (joint-object) if given id exists, null otherwise
   */
  public static ContributionHasActor findById(Long id) {
    if (id == null || id == -1L) {
      return null;
    }
    return find.byId(id);
  }

  /**
   * Retrieve a ContributionHasActor object by a given contribution and actor
   *
   * @param idContribution a contribution id
   * @param idActor        an actor id being involved in the given contribution
   * @return the ContributionhasActor corresponding to given ids, null if unfound
   */
  public static ContributionHasActor findContributionHasActor(Long idContribution, Long idActor) {
    if (idContribution == null || idActor == null) {
      return null;
    }
    return find.fetch("contribution").where().conjunction()
        .eq("id_actor", idActor)
        .eq("contribution.idContribution", idContribution)
            .findUnique();
  }

  /**
   * Retrieve a ContributionHasActor object by a given contribution, actor and role
   *
   * @param idContribution a contribution id
   * @param idActor        an actor id being involved in the given contribution
   * @param isAuthor true if actor is the author
   * @param isReporter true if actor is the reporter
   * @param isJustCited true if actor is just citec
   * @return the ContributionhasActor corresponding to given ids, null if unfound
   */
  public static ContributionHasActor findContributionHasActor(Long idContribution, Long idActor, boolean isAuthor, boolean isReporter, boolean isJustCited) {
    if (idContribution == null || idActor == null) {
      return null;
    }
    return find.fetch("contribution").where().conjunction()
            .eq("id_actor", idActor)
            .eq("is_author", isAuthor)
            .eq("is_speaker", isReporter)
            .eq("is_about", isJustCited)
            .eq("contribution.idContribution", idContribution)
            .findUnique();
  }

  /**
   * Retrieve a list of ContributionHasActor object by a given contribution and role
   *
   * @param idContribution a contribution id
   * @param isAuthor true if actor is the author
   * @param isReporter true if actor is the reporter
   * @param isJustCited true if actor is just citec
   * @return the possibly empty list of ContributionhasActor corresponding to given ids, null if unfound
   */
  public static List<ContributionHasActor> findContributionHasActors(Long idContribution, boolean isAuthor, boolean isReporter, boolean isJustCited) {
    if (idContribution == null) {
      return new ArrayList<>();
    }
    return find.fetch("contribution").where().conjunction()
            .eq("is_author", isAuthor)
            .eq("is_speaker", isReporter)
            .eq("is_about", isJustCited)
            .eq("contribution.idContribution", idContribution)
            .findList();
  }

  /**
   * Retrieve a ContributionHasActor object by a given contribution and actor limited
   *
   * @param idContribution a contribution id
   * @param limit the limit of number of result
   * @param role the actor role for given contribution
   * @return a (possibly empty) list of ContributionhasActor for given contribution
   */
  public static List<ContributionHasActor> findContributionHasActorLimited(Long idContribution, int limit, EActorRole role) {
    if (idContribution == null) {
      return new ArrayList<>();
    }

    return find.fetch("contribution").where().conjunction()
            .eq("is_author", role == EActorRole.AUTHOR)
            .eq("is_speaker", role == EActorRole.REPORTER)
            .eq("is_about", role == EActorRole.CITED)
            .eq("contribution.idContribution", idContribution)
            .setMaxRows(limit)
            .findList();
  }

  /**
   * Get the number of actors by role of given contribution
   *
   * @param idContribution a contribution id
   * @param role the actor role for given contribution
   * @return the number of actors for given role
   */
  public static int getNbActors(Long idContribution, EActorRole role) {
    return find.fetch("contribution").where().conjunction()
            .eq("is_author", role == EActorRole.AUTHOR)
            .eq("is_speaker", role == EActorRole.REPORTER)
            .eq("is_about", role == EActorRole.CITED)
            .eq("contribution.idContribution", idContribution).findRowCount();
  }

  /**
   * Retrieve a ContributionHasActor object by a given contribution and actor
   *
   * @param idActor        an actor id being involved in the given contribution
   * @param type the contribution type id to retrieve (-1 to get all of them)
   * @return a (possibly empty) list of ContributionhasActor for given actor and type
   */
  public static List<ContributionHasActor> findContributionHasActor(Long idActor, int type) {
    if (idActor == null) {
      return new ArrayList<>();
    }

    return find
            .fetch("contribution.contributionType")
            .where().conjunction()
            .eq("id_actor", idActor)
            .eq("contribution_type", type)
            .findList();
  }

  /**
   * Find all contribution has actor for given actor affiliation id
   *
   * @param aha an actor affiliation id
   * @return a (possibly empty) list of ContributionhasActor for affiliation id
   */
  public static List<ContributionHasActor> findByAffiliation(Long aha) {
    List<ContributionHasActor> result = find.where().eq("actor_id_aha", aha).findList();
    return result != null ? result : new ArrayList<>();
  }

  public static void deleteStatic(Long id){
    ContributionHasActor.find.where().eq("id_cha", id).delete();
  }

}
