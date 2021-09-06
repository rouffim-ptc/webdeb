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

import akka.stream.scaladsl.ZipWith18;
import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EFilterKey;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.contribution.link.EPositionLinkShade;
import be.webdeb.core.impl.helper.*;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The persistent class for the actor database table, conceptual subtype of contribution and supertype
 * of person and organization. Concrete subtype is specified via the actortype
 *
 * Because of ebean limitations, there is no way to create a class_per_table inheritance, so links to subtypes
 * must be handled by hand.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.actor.EActorType
 */
@Entity
@CacheBeanTuning
@Table(name = "actor")
public class Actor extends WebdebModel {

  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, Actor> find = new Model.Finder<>(Actor.class);

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  // forcing updates from this object, deletions are handled at the contribution level
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Contribution contribution;

  @OneToOne(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Organization organization;

  @OneToOne(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Person person;

  @OneToMany(mappedBy = "actor", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<ActorI18name> names;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_type", nullable = false)
  private TActorType actortype;

  @Column(name = "crossref")
  private String crossref;

  // all my affiliations
  @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<ActorHasAffiliation> affiliations;

  // all affiliated actors
  @OneToMany(mappedBy = "affiliation", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<ActorHasAffiliation> actors;

  // all contributors having me has affiliations
  @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private List<ContributorHasAffiliation> contributors;

  // all contributions where I appear
  @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<ContributionHasActor> contributions;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_picture")
  private ContributorPicture avatar;

  @Transient
  @Unqueryable
  private Integer nb_contributions;

  /**
   * Get the actor id
   *
   * @return an id
   */
  public Long getIdContribution() {
    return this.idContribution;
  }

  /**
   * Set the actor id
   *
   * @param idContribution an id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get the list of names for this actor (names may have multiple spellings)
   *
   * @return a list of names
   */
  public List<ActorI18name> getNames() {
    return names;
  }

  /**
   * Set the list of names for this actor (names may have multiple spellings)
   *
   * @param names a list of names to set
   */
  public void setNames(List<ActorI18name> names) {
    if (names != null) {
      if (this.names == null) {
        this.names = new ArrayList<>();
      }
      // get previous languages for current names
      List<String> currentlangs = this.names.stream().filter(n -> !n.isOld()).map(ActorI18name::getLang).collect(Collectors.toList());
      List<String> oldlangs = this.names.stream().filter(ActorI18name::isOld).map(ActorI18name::getLang).collect(Collectors.toList());

      // add/update new names
      names.forEach(this::addName);

      currentlangs.stream().filter(lang -> names.stream().noneMatch(n -> n.getLang().equals(lang))).forEach(lang ->
        this.names.removeIf(current -> current.getLang().equals(lang) && !current.isOld())
      );
      oldlangs.stream().filter(lang -> names.stream().noneMatch(n -> n.getLang().equals(lang))).forEach(lang ->
        this.names.removeIf(current -> current.getLang().equals(lang) && current.isOld())
      );
    }
  }

  /**
   * Add a name to this actor, if such language already exists, will update existing name
   *
   * @param name a name structure
   */
  public void addName(ActorI18name name) {
    if (names == null) {
      names = new ArrayList<>();
    }
    Optional<ActorI18name> match = names.stream().filter(n ->
        n.getLang().equals(name.getLang()) && n.isOld() == name.isOld()).findAny();
    if (match.isPresent()) {
      ActorI18name toUpdate = match.get();
      toUpdate.setFirstOrAccro(name.getFirstOrAcro());
      toUpdate.setName(name.getName());
      toUpdate.setPseudo(name.getPseudo());
      toUpdate.isOld(name.isOld());
    } else {
      names.add(name);
    }
  }

  /**
   * Get this actortype
   *
   * @return the actortype
   */
  public TActorType getActortype() {
    return actortype;
  }

  /**
   * Set the actortype
   *
   * @param actortype the actortype to set
   */
  public void setActortype(TActorType actortype) {
    this.actortype = actortype;
  }

  /**
   * Get the external reference (url) for this actor
   *
   * @return an url, or null if unset
   */
  public String getCrossref() {
    return crossref;
  }

  /**
   * Set the external reference (url) for this actor
   *
   * @param crossref an url
   */
  public void setCrossref(String crossref) {
    this.crossref = crossref;
  }

  /**
   * Get the contribution parent object
   *
   * @return the contribution "supertype" object
   */
  public Contribution getContribution() {
    return contribution;
  }

  /**
   * Set the contribution parent object
   *
   * @param contribution the contribution "supertype" object
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get the list of affiliations (joint objects), ie, actors or functions where this
   * actor is affiliated
   *
   * @return a (possibly empty) list of affiliations joint-objects
   */
  public List<ActorHasAffiliation> getAffiliations() {
    return affiliations != null ? affiliations : new ArrayList<>();
  }

  /**
   * Set the list of affiliations (joint objects), ie, actors or functions where this
   * actor is affiliated
   *
   * @param affiliations a list of affiliations joint-objects
   */
  public void setAffiliations(List<ActorHasAffiliation> affiliations) {
    this.affiliations = affiliations;
  }

  /**
   * Get the list of affiliated actor (as joint-objects), ie actors having this actor as affiliations
   *
   * @return a (possibly empty) list of affiliations joint-objects
   */
  public List<ActorHasAffiliation> getActors() {
    return actors != null ? actors : new ArrayList<>();
  }

  /**
   * Set the list of affiliated actor (as joint-objects), ie actors having this actor as affiliations
   *
   * @param actors a list of affiliations joint-objects
   */
  public void setActors(List<ActorHasAffiliation> actors) {
    this.actors = actors;
  }

  /**
   * Add given affiliation to this actor
   *
   * @param affiliation an affiliation
   * @return the added affiliation
   */
  public ActorHasAffiliation addAffiliation(ActorHasAffiliation affiliation) {
    getAffiliations().add(affiliation);
    affiliation.setActor(this);
    return affiliation;
  }

  /**
   * Get the list of contributions where this actor appears
   *
   * @param type the contribution type id to retrieve (-1 to get all of them)
   * @return a (possibly empty) list of contributions joint-objects
   */
  public List<ContributionHasActor> getContributions(int type) {
    if (contribution == null) {
      return new ArrayList<>();
    }

    if (type != -1) {
      return ContributionHasActor.findContributionHasActor(getContribution().getIdContribution(), type);
    }

    return contributions.stream().filter(c -> !c.getContribution().isHidden()).collect(Collectors.toList());
  }

  /**
   * Set the list of contributions where this actor appears
   *
   * @param contributions a list of contributions joint-objects
   */
  public void setContributions(List<ContributionHasActor> contributions) {
    this.contributions = contributions;
  }

  /**
   * Get the list of contributors having this actor as affiliations
   *
   * @return a (possibly empty) list of contributors
   */
  public List<ContributorHasAffiliation> getContributors() {
    return contributors != null ? contributors : new ArrayList<>();
  }

  /**
   * Set the list of contributors having this actor as affiliations
   *
   * @param contributors a list of contributors
   */
  public void setContributors(List<ContributorHasAffiliation> contributors) {
    this.contributors = contributors;
  }

  /**
   * Get the organization "subtype" object, if this actor is an organization (actortype-dependent)
   *
   * @return the organization subtype if this actor is an organization, null otherwise
   */
  public Organization getOrganization() {
    return organization;
  }

  /**
   * Set the organization "subtype" object, if this actor is an organization (actortype-dependent)
   *
   * @param organization an organization object
   */
  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  /**
   * Get the person "subtype" object, if this actor is a person (actortype-dependent)
   *
   * @return the person subtype if this actor is a person, null otherwise
   */
  public Person getPerson() {
    return person;
  }

  /**
   * Set the person "subtype" object, if this actor is a person (actortype-dependent)
   *
   * @param person the person subtype object
   */
  public void setPerson(Person person) {
    this.person = person;
  }

  /**
   * Get the avatar and all data about the picture of this actor
   *
   * @return a contributor picture, null if none
   */
  public ContributorPicture getAvatar() {
    return avatar;
  }

  /**
   * Set the avatar and all data about the picture of this actor
   *
   * @param avatar a contributor picture
   */
  public void setAvatar(ContributorPicture avatar) {
    this.avatar = avatar;
  }

  /**
   * Get the number of contributions if this data is asked in a query
   *
   * @return the number of contributions
   */
  public Integer getNbContributions() {
    return nb_contributions;
  }

  /*
   * CONVENIENCE METHODS
   */

  /**
   * Get the current version of this actor
   *
   * @return a timestamp with the latest update moment of this actor
   */
  public Timestamp getVersion() {
    return getContribution().getVersion();
  }

  /**
   * Retrieve the full names of this Actor
   *
   * @return the concatenation of first and last or pseudo only for persons and only name for other
   */
  public List<String> getFullNames() {
    if (getActortype().getIdActorType() == EActorType.PERSON.id()) {
      return getNames().stream().map(n -> n.getName() != null ? n.getFirstOrAcro() + " " + n.getName() : n.getPseudo())
          .collect(Collectors.toList());
    }
    return getNames().stream().map(ActorI18name::getName).collect(Collectors.toList());
  }

  /**
   * Retrieve the full name of this Actor in given lang
   *
   * @param lang the lang of the name
   * @return the concatenation of first and last or pseudo only for persons and only name for other
   */
  public String getFullName(String lang) {
    ActorI18name name = ActorI18name.findByActorAndLang(idContribution, lang);

    if(name != null) {
      if (getActortype().getIdActorType() == EActorType.PERSON.id()) {
        return name.getName() != null ? name.getFirstOrAcro() + " " + name.getName() : name.getPseudo();
      }
      return name.getName();
    }

    return "Actor[" + idContribution + "]Unknown";
  }

  /**
   * Get the list of texts where actor is author of at least one citation
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return a possibly empty list of texts
   */
  public List<Text> getTextsWhereCitationAuthor(Long contributorId, int groupId){
    return getTextsWhereCitationRelated(true, contributorId, groupId);
  }

  /**
   * Get the list of texts where actor is cited in at least one citation
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return a possibly empty list of texts
   */
  public List<Text> getTextsWhereCitationCited(Long contributorId, int groupId){
    return getTextsWhereCitationRelated(false, contributorId, groupId);
  }

  @SuppressWarnings("fallthrough")
  @Override
  public String toString() {
    // because of lazy load, must explicitly call getter
    StringBuilder builder = new StringBuilder(", named: {")
            .append(getNames().stream().map(Object::toString).collect(Collectors.joining(",")))
            .append("}, affiliations: {").append(getAffiliations().stream()
                    .map(ActorHasAffiliation::toString).collect(Collectors.joining(", ")))
            .append("}, url: ").append(getCrossref()).append(", picture: ");

    if(getAvatar() != null) {
      builder.append(getAvatar().getIdPicture()).append(".").append(getAvatar().getExtension());
    } else {
      builder.append("null");
    }

      switch (getActortype().getEActorType()) {
          case PERSON:
              if(getPerson() != null)
                builder.append(getPerson().toString());
              break;
          case ORGANIZATION:
              if(getOrganization() != null)
                builder.append(getOrganization().toString());
              break;
      }

    return getModelDescription(getContribution(), builder.toString());
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve an actor by its id
   *
   * @param id an id
   * @return the Actor corresponding to the given id, or null if not found
   */
  public static Actor findById(Long id) {
    return id == null || id == -1L ? null : find.byId(id);
  }

  /**
   * Get a randomly chosen actor from the database
   *
   * @return a random Debate
   */
  public static Actor random() {
    return findById(random(EContributionType.ACTOR));
  }

  /**
   * Retrieve a list of actor of the given type with their name containing the given name
   *
   * @param name actor name
   * @param type actor type (int representation, pass -1 if not relevant)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of matches (may be empty)
   */
  public static List<Actor> findByPartialName(String name, int type, int fromIndex, int toIndex) {

    if (name == null) {
      return new ArrayList<>();
    }
    // to retrieve unknown and given type
    // -> type = 0 => allButType is 1, so we look for all actors but type 1 (so for persons and unknown)
    // -> type = 1 => allButType is 0, so we look for all actors but type 0 (so for org. and unknown)
    // -> type = -1 => allButType is 2, so we look for all actors (unknown)
    int allButType = 1 - type;

    String select = "select distinct actor.id_contribution from actor right join actor_i18names " +
        "on actor.id_contribution = actor_i18names.id_contribution where " +
        "concat(ifnull(first_or_acro, ''), concat(ifnull(name, ''), ifnull(pseudo, ''))) " +
        "like '%" + getSearchToken(name) + "%' and id_type <> " + allButType + getSearchLimit(fromIndex, toIndex);

    logger.debug("search for actor: " + select);
    List<Actor> result = Ebean.find(Actor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Retrieve a list of actor based on a name and a type
   *
   * @param fullname actor full name
   * @param type actor type (int representation, pass -1 if not relevant)
   * @return a list of potential matches (may be empty)
   */
  public static List<Actor> findByFullName(String fullname, int type) {
    logger.debug("search actor by name " + fullname + " of type " + type);
    if (fullname == null || "".equals(fullname)) {
      return new ArrayList<>();
    }
    List<Actor> actors = findByPartialName(fullname, type, 0, 0);
    logger.debug(fullname.toLowerCase() +  " " + actors);
    switch (EActorType.value(type)) {
      case PERSON :
        actors.removeIf(a -> a.getFullNames().stream().noneMatch(n -> equalsIgnoreAccents(fullname, n)));
        break;
      case ORGANIZATION:
        actors.removeIf(a -> a.getNames().stream().noneMatch(n ->
            fullname.equalsIgnoreCase(n.getFirstOrAcro()) || fullname.equalsIgnoreCase(n.getName())));
        break;
      default:
        actors.removeIf(a ->
            // current one is a person
            (a.getActortype().getIdActorType() == EActorType.PERSON.id()
                // full names must match exactly
                && a.getFullNames().stream().noneMatch(n -> equalsIgnoreAccents(fullname, n))
            )
            // organization and unknown
            || (a.getActortype().getIdActorType() != EActorType.PERSON.id()
                // either name or acronym must match
                && a.getNames().stream().noneMatch(n ->
                  equalsIgnoreAccents(n.getFirstOrAcro(), fullname) || equalsIgnoreAccents(n.getName(), fullname))));
        break;
    }
    return actors;
  }

  /**
   * Get all actor of the given type
   *
   * @param type actor type (int representation, pass -1 if not relevant)
   * @return a list of actors (may be empty)
   */
  public static List<Actor> getAllActorsByType(int type) {
    List<Actor> result = null;
    if(type >= -1 && type <= 1) {
      // to retrieve unknown and given type
      // -> type = 1 => organizations
      // -> type = 0 => person
      // -> type = -1 => unknow

      String select = "select distinct actor.id_contribution from actor right join actor_i18names " +
          "on actor.id_contribution = actor_i18names.id_contribution where id_type = " + type;

      logger.debug("search for actor: " + select);
      result = Ebean.find(Actor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Retrieve a list of Actor by a list of actor ids
   *
   * @param ids a list of actor ids
   * @return a possibly empty list of actors
   */
  public static List<Actor> retrieveAll(List<Long> ids){
    String select = "select distinct actor.id_contribution from actor " +
            "where actor.id_contribution in (" + StringUtils.join(ids, ',') + ")";
    List<Actor> result = Ebean.find(Actor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get the minimum or maximum affiliation of given actor for given actor type.
   *
   * @param id the actor id to found dates
   * @param actorType the given actor type
   * @param viewedType the actor type that we need to look
   * @param forMin true for minimum, false for maximum
   * @return the minimum or maximum date
   */
  public static String getMinOrMaxAffiliationDate(Long id, EActorType actorType, EActorType viewedType, boolean forMin) {
    String select = "SELECT " +
            (forMin ? "SUBSTRING(min(aha.start_date), 1, 4)" :
                    "if(max(aha.end_date_type) = 5, YEAR(CURDATE()), SUBSTRING(max(aha.end_date), 1, 4))") + " as 'date' " +
            "FROM actor_has_affiliation aha " +
            "inner join actor a1 on a1.id_contribution = aha.id_actor " +
            "inner join actor a2 on a2.id_contribution = aha.id_actor_as_affiliation " +
            "where (aha.id_actor_as_affiliation = " + id + " and a1.id_type = " + viewedType.id() + ") " +
            "or (aha.id_actor = " + id + " " + (actorType == EActorType.PERSON ? "and a2.id_type = " + viewedType.id() : "") + ")";
    return Ebean.createSqlQuery(select).findUnique().getString("date");
  }

  public static List<ActorAffiliations> getOwners(SearchContainer query) {
    Map<Long, ActorAffiliations> owners = new ConcurrentHashMap<>();

    String select = addFiltersToSql("SELECT c.id_contribution as 'id_actor', " + selectActorName() + ", a.id_type as 'actor_type', concat(ap.id_picture, ap.extension) as 'image' FROM actor_has_affiliation aha " +
            "left join contribution c on c.id_contribution = aha.id_actor_as_affiliation " +
            "left join actor a on a.id_contribution = c.id_contribution " +
            joinActorName(query) +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            "where aha.id_actor = " + query.getId() + " " +
            "group by aha.id_actor_as_affiliation ",
            query.getFilters(), true);
    initAffiliationsMap(owners, select, null);

    select = getBaseAffiliationSelect(query.getLang(), false) +
            addFiltersToSql(
            ", aha.type as 'type', aff_type." + query.getLang() + " as 'type_name' FROM actor_has_affiliation aha " +
            "left join contribution c on c.id_contribution = aha.id_actor_as_affiliation " +
            "left join actor a on a.id_contribution = c.id_contribution " +
            "left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type " +
            "left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type " +
            "left join t_affiliation_type aff_type on aff_type.id_type = aha.type " +
            "where aha.id_actor = " + query.getId(),
            query.getFilters(), true);
    fillAffiliationsMap(owners, select, query.getLang());

    return owners.values()
            .stream()
            .sorted()
            .collect(Collectors.toList());
  }

  public static List<ActorAffiliations> getOwnedOrganizations(SearchContainer query) {
    return getAffiliations(query, false);
  }

  public static List<ActorAffiliations> getAffiliationOrganizations(SearchContainer query) {
    return getAffiliations(query, true);
  }

  private static List<ActorAffiliations> getAffiliations(SearchContainer query, boolean forPerson) {
    Map<Long, ActorAffiliations> affiliations = new ConcurrentHashMap<>();

    String select = addFiltersToSql("SELECT c.id_contribution as 'id_actor', " + selectActorName() + ", concat(ap.id_picture, ap.extension) as 'image', status.fr as 'status' FROM actor_has_affiliation aha " +
            "left join contribution c on c.id_contribution = aha.id_actor" + (forPerson ? "_as_affiliation " : " ") +
            "left join actor a on a.id_contribution = c.id_contribution " +
            joinActorName(query) +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            "inner join organization o on o.id_contribution = c.id_contribution " +
            "left join t_legal_status status on status.id_type = o.legal_status " +
            "where aha.id_actor" + (!forPerson ? "_as_affiliation" : "") + " = " + query.getId() + " " +
            "group by aha.id_actor" + (forPerson ? "_as_affiliation" : ""),
            query.getFilters(), true);
    initAffiliationsMap(affiliations, select, EActorType.ORGANIZATION);

    select = getBaseAffiliationSelect(query.getLang(), !forPerson) +
                    (forPerson ? selectAffiliationFunctions(query) : "") +
            addFiltersToSql(
                    ", aha.type as 'type', aff_type." + query.getLang() + " as 'type_name' FROM actor_has_affiliation aha " +
                    "left join contribution c on c.id_contribution = aha.id_actor" + (forPerson ? "_as_affiliation " : " ") +
                    "left join actor a on a.id_contribution = c.id_contribution " +
                    (forPerson ? "left join person p on p.id_contribution = aha.id_actor " : "") +
                    (forPerson ? "left join profession_has_link phl on phl.id_profession_from = aha.function " : "") +
                    "left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type " +
                    "left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type " +
                    "left join t_affiliation_type aff_type on aff_type.id_type = aha.type + 12 " +
                    "where aha.id_actor" + (!forPerson ? "_as_affiliation" : "") + " = " + query.getId() + " and a.id_type = " + EActorType.ORGANIZATION.id() + " " +
                    "group by aha.id_aha",
            query.getFilters(), true);
    fillAffiliationsMap(affiliations, select, query.getLang());

    return affiliations.values()
            .stream()
            .sorted()
            .collect(Collectors.toList());
  }

  public static List<ActorAffiliations> getMembers(SearchContainer query) {
    Map<Long, ActorAffiliations> members = new ConcurrentHashMap<>();
    String filterAffiliation = getSqlFilter(query.getFilters(), EFilterKey.OTHER_AFFILIATION);

    String select = addFiltersToSql("SELECT c.id_contribution as 'id_actor', " + selectActorName() + ", concat(ap.id_picture, ap.extension) as 'image', p.gender as 'actor_gender' FROM actor_has_affiliation aha " +
            "left join contribution c on c.id_contribution = aha.id_actor " +
            "left join actor a on a.id_contribution = c.id_contribution " +
            joinActorName(query) +
            "inner join person p on p.id_contribution = c.id_contribution " +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            (filterAffiliation != null ? "left join actor_has_affiliation oaha on oaha.id_actor_as_affiliation = aha.id_actor " : "") +
            "where aha.id_actor_as_affiliation = " + query.getId() +
            (filterAffiliation != null ? " and oaha.id_actor_as_affiliation = " + filterAffiliation + " " : "") +
            " and (aha.type is null or aha.type != " + EAffiliationType.GRADUATING_FROM.id() + ") " +
            " group by aha.id_actor",
            query.getFilters(), true);
    initAffiliationsMap(members, select, EActorType.PERSON);

    select = getBaseAffiliationSelect(query.getLang(), true) +
            selectAffiliationFunctions(query) +
            addFiltersToSql(
            "FROM actor_has_affiliation aha " +
            "left join contribution c on c.id_contribution = aha.id_actor " +
            "inner join person p on p.id_contribution = c.id_contribution " +
            "left join profession_has_link phl on phl.id_profession_from = aha.function " +
            "left join t_precision_date_type start_date_type on start_date_type.id_type = aha.start_date_type " +
            "left join t_precision_date_type end_date_type on end_date_type.id_type = aha.end_date_type " +
              (filterAffiliation != null ? "left join actor_has_affiliation oaha on oaha.id_actor_as_affiliation = aha.id_actor " : "") +
            "where aha.id_actor_as_affiliation = " + query.getId() +
            (filterAffiliation != null ? " and oaha.id_actor_as_affiliation = " + filterAffiliation + " " : "") +
            " and (aha.type is null or aha.type != " + EAffiliationType.GRADUATING_FROM.id() + ") " +
            "group by aha.id_aha",
            query.getFilters(), true);
    fillAffiliationsMap(members, select, query.getLang());

    return members.values()
            .stream()
            .sorted()
            .collect(Collectors.toList());
  }

  private static String selectAffiliationFunctions(SearchContainer query) {
    return ", @f1 := (select spelling from profession_i18names where lang = '" + query.getLang() + "' and gender = p.gender and profession = aha.function) as 'gendered_function', " +
            "@f2 := (select spelling from profession_i18names where lang = '" + query.getLang() + "' and profession = aha.function and (gender = 'M' or gender = 'N') group by profession) as 'neutral_function1', " +
            "if(@f2 is null, (select spelling from profession_i18names where profession = aha.function group by profession), \"\") as 'neutral_function2', " +
            "@f3 := (select spelling from profession_i18names where lang = '" + query.getLang() + "' and (gender = 'M' or gender = 'N') and profession = phl.id_profession_to group by profession) as 'generic_function1', " +
            "if(@f3 is null, @f4 := (select spelling from profession_i18names where profession = phl.id_profession_to group by profession), \"\") as 'generic_function2' ";
  }

  private static String getBaseAffiliationSelect(String lang, boolean forActor) {
    return "SELECT aha.id_aha as 'link_id', aha.id_actor" + (!forActor ? "_as_affiliation" : "") + " as 'id_actor', aha.start_date as 'start_date', aha.end_date as 'end_date', " +
            "aha.start_date_type as 'start_date_type', start_date_type." + lang + " as 'start_date_name', " +
            "aha.end_date_type as 'end_date_type', end_date_type." + lang + " as 'end_date_name'";
  }

  private static void initAffiliationsMap(Map<Long, ActorAffiliations> affMap, String select, EActorType type) {
    Ebean.createSqlQuery(select).findList().parallelStream().forEach(row -> {
      Long id = row.getLong("id_actor");

      affMap.put(id, new ActorAffiliations(id,
              row.getString("name"),
              row.containsKey("image") ? row.getString("image") : null,
              type != null ? type.id() : row.containsKey("actor_type") ? row.getInteger("actor_type") : null,
              row.containsKey("actor_gender") ? row.getString("actor_gender") : null,
              row.containsKey("status") ? row.getString("status") : null));
    });
  }

  private static void fillAffiliationsMap(Map<Long, ActorAffiliations> affMap, String select, String lang) {
    Ebean.createSqlQuery(select).findList().parallelStream().forEach(row -> {
      Long id = row.getLong("id_actor");
      ActorAffiliation aff = new ActorAffiliation(
              row.getLong("link_id"),
              row.getString("start_date"),
              row.getInteger("start_date_type"),
              row.getString("start_date_name"),
              row.getString("end_date"),
              row.getInteger("end_date_type"),
              row.getString("end_date_name"),
              lang
      );

      if(row.containsKey("gendered_function")) {
        aff.setNeutralFunction((String) row.getOrDefault("neutral_function1", "neutral_function2"));
        aff.setGenericFunction((String) row.getOrDefault("generic_function1", "generic_function2"));
        aff.setFunction(row.getString("gendered_function") != null ? row.getString("gendered_function") : aff.getNeutralFunction());
      }

      if(row.containsKey("type")) {
        aff.setAffType(row.getInteger("type"));
        aff.setAffTypeName(row.getString("type_name"));
      }

      affMap.get(id).addAffiliation(aff);
    });
  }


  public static List<ActorAlliesOpponents> getAlliedOpponentsByActor(SearchContainer query) {
    return fillAlliesOpponentsMap(query, "SELECT cha.id_actor as 'id', " + selectActorName() + ", concat(cha.id_actor, ap.extension) as 'image', a.id_type as 'actor_type', p.gender as 'actor_gender', " +
            getAlliedOpponentsByActorQuery(query, true) +
            " group by cha.id_actor" + fillGroupByAlliesOpponentsQuery(query));

  }

  public static Map<Integer, List<Citation>> getAlliedOpponentsCitationsByActor(SearchContainer query) {
    return fillAlliesOpponentsCitationsMap(query, "SELECT l.id_contribution as 'link', c.id_contribution as 'citation', " +
            getAlliedOpponentsByActorQuery(query, false) +
            " and cha.id_actor = " + query.getId() +
            " group by c.id_contribution" + fillGroupByAlliesOpponentsQuery(query) +
            " order by c.version desc");
  }

  private static String getAlliedOpponentsByActorQuery(SearchContainer query, boolean forLinks) {
    return  fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution ac on ac.id_contribution = cha.id_actor " +
            "left join actor a on a.id_contribution = cha.id_actor " +
            joinActorName(query) +
            "left join person p on p.id_contribution = cha.id_actor " +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            fillEndAlliesOpponentsQuery(query);
  }

  public static List<ActorAlliesOpponents> getAlliedOpponentsByAge(SearchContainer query){
    return fillAlliesOpponentsMap(query, "SELECT " + selectByAge() +
            getAlliedOpponentsByAgeQuery(query, true) +
            " group by id" + fillGroupByAlliesOpponentsQuery(query));
  }

  public static Map<Integer, List<Citation>> getAlliedOpponentsCitationsByAge(SearchContainer query) {
    return fillAlliesOpponentsCitationsMap(query, "SELECT l.id_contribution as 'link', c.id_contribution as 'citation', " + selectByAge() +
            getAlliedOpponentsByAgeQuery(query, false) +
            " group by c.id_contribution" + fillGroupByAlliesOpponentsQuery(query) +
            " having id = " + query.getId() +
            " order by c.version desc");
  }

  private static String getAlliedOpponentsByAgeQuery(SearchContainer query, boolean forLinks) {
    return  fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join actor a on a.id_contribution = cha.id_actor " +
            "left join person p on p.id_contribution = a.id_contribution " +
            fillEndAlliesOpponentsQuery(query);
  }

  public static List<ActorAlliesOpponents> getAlliedOpponentsByCountry(SearchContainer query){
    return fillAlliesOpponentsMap(query, "SELECT if(t_country.id_country is null, -1, concat(ASCII(substr(t_country.id_country, 1, 1)), ASCII(substr(t_country.id_country, 2, 1)))) as 'id', if(t_country." + query.getLang() + " is null, 'unknown', t_country." + query.getLang() + ") as 'name', " +
            getAlliedOpponentsByCountryQuery(query, true) +
            " group by t_country.id_country" + fillGroupByAlliesOpponentsQuery(query));
  }

  public static Map<Integer, List<Citation>> getAlliedOpponentsCitationsByCountry(SearchContainer query) {
    return fillAlliesOpponentsCitationsMap(query, "SELECT l.id_contribution as 'link', c.id_contribution as 'citation', " +
            getAlliedOpponentsByCountryQuery(query, false) +
            " and t_country.id_country " + countryIdToString(query.getId()) +
            " group by c.id_contribution" + fillGroupByAlliesOpponentsQuery(query) +
            " order by c.version desc");
  }

  private static String getAlliedOpponentsByCountryQuery(SearchContainer query, boolean forLinks) {
    return  fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join actor a on a.id_contribution = cha.id_actor " +
            "left join person p on p.id_contribution = a.id_contribution " +
            "left join t_country on t_country.id_country = p.residence " +
            fillEndAlliesOpponentsQuery(query);
  }

  public static List<ActorAlliesOpponents> getAlliedOpponentsByProfession(SearchContainer query){
    return fillAlliesOpponentsMap(query, "SELECT if(phl.id_profession_to is null, if(p.profession is null, -1, p.profession), phl.id_profession_to) as 'id', if(phl.id_profession_to is null, if(p.spelling is null, pbis.spelling, p.spelling), if(p2.spelling is null, p2bis.spelling, p2.spelling)) as 'name', " +
            getAlliedOpponentsByProfessionQuery(query, true) +
            " group by if(phl.id_profession_to is null, p.profession, phl.id_profession_to)" + fillGroupByAlliesOpponentsQuery(query));
  }

  public static Map<Integer, List<Citation>> getAlliedOpponentsCitationsByProfession(SearchContainer query) {
    return fillAlliesOpponentsCitationsMap(query, "SELECT l.id_contribution as 'link', c.id_contribution as 'citation', " +
            getAlliedOpponentsByProfessionQuery(query, false) +
            " and if(phl.id_profession_to is null, p.profession, phl.id_profession_to) " + isNotBlankOrNull(query.getId()) +
            " group by c.id_contribution" + fillGroupByAlliesOpponentsQuery(query) +
            " order by c.version desc");
  }

  private static String getAlliedOpponentsByProfessionQuery(SearchContainer query, boolean forLinks) {
    return  fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            selectByProfession(query) +
            fillEndAlliesOpponentsQuery(query);
  }

  public static List<ActorAlliesOpponents> getAlliedOpponentsByOrganization(SearchContainer query){
    String select = "SELECT if(ac.id_contribution is null, -1, ac.id_contribution) as 'id', " + selectActorName() + ", concat(ac.id_contribution, ap.extension) as 'image', a.id_type as 'actor_type', ";
    return fillAlliesOpponentsMap(query,
            getAlliedOpponentsByOrganizationQuery1(query, select, true) +
            " group by cha.id_actor" + fillGroupByAlliesOpponentsQuery(query) +
            getAlliedOpponentsByOrganizationQuery2(query, select, true) +
            " group by a.id_contribution" + fillGroupByAlliesOpponentsQuery(query));
  }

  public static Map<Integer, List<Citation>> getAlliedOpponentsCitationsByOrganization(SearchContainer query) {
    String select = "SELECT l.id_contribution as 'link', c.id_contribution as 'citation', c.version as 'version', ";
    return fillAlliesOpponentsCitationsMap(query,
            getAlliedOpponentsByOrganizationQuery1(query, select, false) +
            " and cha.id_actor " + isNotBlankOrNull(query.getId()) +
            " group by c.id_contribution " + fillGroupByAlliesOpponentsQuery(query) +
            getAlliedOpponentsByOrganizationQuery2(query, select, false) +
            " and aha.id_actor_as_affiliation " + isNotBlankOrNull(query.getId()) +
            " group by c.id_contribution" + fillGroupByAlliesOpponentsQuery(query) +
            " order by 'version' desc");
  }

  private static String getAlliedOpponentsByOrganizationQuery1(SearchContainer query, String select, boolean forLinks) {
    return  select + fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution ac on ac.id_contribution = cha.id_actor " +
            "left join actor a on a.id_contribution = cha.id_actor " +
            joinActorName(query) +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            fillEndAlliesOpponentsQuery(query) + " and a.id_type = " + EActorType.ORGANIZATION.id();
  }

  private static String getAlliedOpponentsByOrganizationQuery2(SearchContainer query, String select, boolean forLinks) {
    return  " UNION " + select +
            fillBeginAlliesOpponentsQuery(query) +
            "left join contribution c on c.id_contribution = " +
            (forLinks || query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join contribution_has_actor cha on cha.id_contribution = " +
            (query.getAlliesOpponentsType() == EAlliesOpponentsType.TAGS ? "l.id_contribution " : "l.id_citation ") +
            "left join actor_has_affiliation aha on aha.id_aha = cha.actor_id_aha " +
            "left join contribution ac on ac.id_contribution = aha.id_actor_as_affiliation " +
            "left join actor a on a.id_contribution = ac.id_contribution " +
            joinActorName(query) +
            "left join contributor_picture ap on ap.id_picture = a.id_picture " +
            fillEndAlliesOpponentsQuery(query);
  }

  private static String fillBeginAlliesOpponentsQuery(SearchContainer query) {
    String select = "";

    switch (query.getAlliesOpponentsType()) {
      case ARGUMENTS:
        select = "l.id_shade as 'id_shade', count(distinct l.id_citation) as 'nb_citations' FROM citation_justification_link l ";
        break;
      case POSITIONS:
        select = "l.id_shade as 'id_shade', count(distinct l.id_citation) as 'nb_citations' FROM citation_position_link l ";
        break;
      case TAGS:
        select = "count(distinct l.id_contribution) as 'nb_citations' FROM contribution_has_tag l ";
        break;
    }

    return select;
  }

  private static String fillEndAlliesOpponentsQuery(SearchContainer query) {
    String whereByType = "";

    switch (query.getAlliesOpponentsType()) {
      case ARGUMENTS:
        whereByType =
                "l.id_context = " + query.getContext() +
                        (!isBlank(query.getSubContext()) ? " and l.id_sub_context = " + query.getSubContext() : "");
        break;
      case POSITIONS:
        whereByType =
                "l.id_debate = " + query.getContext() +
                        (!isBlank(query.getSubContext()) ? " and l.id_sub_debate = " + query.getSubContext() : "");
        break;
      case TAGS:
        whereByType =
                "l.id_tag = " + query.getContext();
        break;
    }


    return getContributionStatsJoins() +
            " where " + whereByType + " and cha.is_author = 1 " +
            getContributionStatsWhereClause(query);
  }

  private static String fillGroupByAlliesOpponentsQuery(SearchContainer query) {
    switch (query.getAlliesOpponentsType()) {
      case ARGUMENTS:
      case POSITIONS:
        return ", l.id_shade";
      case TAGS:
      default:
        return "";
    }
  }

  private static List<ActorAlliesOpponents> fillAlliesOpponentsMap(SearchContainer query, String select) {
    Map<Long, ActorAlliesOpponents> preResults = new HashMap<>();

    Ebean.createSqlQuery(select).findList().forEach(row -> {
      Long idActor = row.getLong("id");

      if(!preResults.containsKey(idActor)){
        preResults.put(idActor,
                new ActorAlliesOpponents(
                        idActor,
                        row.containsKey("name") ? row.getString("name") : null,
                        row.containsKey("image") ? row.getString("image") : null,
                        row.containsKey("actor_type") ? row.getInteger("actor_type") : null,
                        row.containsKey("actor_gender") ? row.getString("actor_gender") : null,
                        query.getAlliesOpponentsType()));
      }

      preResults.get(idActor).addForShade(
              row.containsKey("id_shade") ? row.getInteger("id_shade") : EJustificationLinkShade.NONE.id(),
              row.getInteger("nb_citations"));
    });

    return preResults.values()
            .stream()
            .sorted()
            .collect(Collectors.toList());
  }

  private static Map<Integer, List<Citation>> fillAlliesOpponentsCitationsMap(SearchContainer query, String select) {
    Map<Integer, Set<Long>> preResults = new LinkedHashMap<>();
    Map<Integer, List<Citation>> results = new LinkedHashMap<>();

    switch (query.getAlliesOpponentsType()){
      case ARGUMENTS:
        EJustificationLinkShade.valuesAsList().forEach(shade -> results.put(shade.id(), new ArrayList<>()));
        EJustificationLinkShade.valuesAsList().forEach(shade -> preResults.put(shade.id(), new HashSet<>()));
        break;
      case POSITIONS:
        EPositionLinkShade.valuesAsList().forEach(shade -> results.put(shade.id(), new ArrayList<>()));
        EPositionLinkShade.valuesAsList().forEach(shade -> preResults.put(shade.id(), new HashSet<>()));
        break;
      default:
        results.put(EJustificationLinkShade.NONE.id(), new ArrayList<>());
        preResults.put(EJustificationLinkShade.NONE.id(), new HashSet<>());
    }

    Ebean.createSqlQuery(select).findList().forEach(row -> {
      int shade = row.containsKey("id_shade") ? row.getInteger("id_shade") : EJustificationLinkShade.NONE.id();
      Long citationId = row.getLong("citation");

      if(!preResults.get(shade).contains(citationId)) {
        preResults.get(shade).add(citationId);
        Citation citation = Citation.findById(citationId);

        if(citation != null) {
          citation.setLinkId(row.getLong("link"));
          results.get(shade).add(citation);
        }
      }
    });

    return results;
  }

  /**
   * Get the map of actors / distance with given actor id for global positions
   *
   * @param query all needed data to perform the query
   * @return the map of global positions distances
   */
  public static List<ActorDistance> getActorsDistancesByActor(SearchContainer query){
    String having = (query.isActorDistanceByDebate() ? " having cha.id_actor = " + query.getValue() : "");
    return getActorsDistances(query,
            "SELECT " + selectForDebate(query) +
                    "cha.id_actor as 'id', " + selectActorName() + ", concat(cha.id_actor, ap.extension) as 'image', a.id_type as 'actor_type', p.gender as 'actor_gender', " +
            fillBeginActorsDistancesQuery(query) +
            joinForDebate(query) +
            "left join contribution ac on ac.id_contribution = cha.id_actor " +
            "left join actor a on a.id_contribution = cha.id_actor " +
            joinActorName(query) +
            "left join person p on p.id_contribution = cha.id_actor " +
            "left join contributor_picture ap on ap.id_picture = cha.id_actor " +
            fillEndActorsDistancesQuery(query) +
            " group by cha.id_actor, l.id_debate, l.id_sub_debate" +
            having);
  }

  public static List<ActorDistance> getActorsDistancesByAge(SearchContainer query){
    String having = (query.isActorDistanceByDebate() ? " having id = " + query.getValue() : "");
    return getActorsDistances(query,
            "SELECT " + selectForDebate(query) + selectByAge() +
            fillBeginActorsDistancesQuery(query) +
            joinForDebate(query) +
            "left join actor a on a.id_contribution = cha.id_actor " +
            "left join person p on p.id_contribution = cha.id_actor " +
            fillEndActorsDistancesQuery(query) +
            " group by id, l.id_debate, l.id_sub_debate" +
            having);
  }

  public static List<ActorDistance> getActorsDistancesByCountry(SearchContainer query){
    String having = (query.isActorDistanceByDebate() ? " having t_country.id_country " + countryIdToString(query.getValue()) : "");
    return getActorsDistances(query,
            "SELECT " + selectForDebate(query) +
                    "if(t_country.id_country is null, -1, concat(ASCII(substr(t_country.id_country, 1, 1)), ASCII(substr(t_country.id_country, 2, 1)))) as 'id', if(t_country." + query.getLang() + " is null, 'unknown', t_country." + query.getLang() + ") as 'name', " +
            fillBeginActorsDistancesQuery(query) +
            joinForDebate(query) +
            "left join actor a on a.id_contribution = cha.id_actor " +
            "left join person p on p.id_contribution = cha.id_actor " +
            "left join t_country on t_country.id_country = p.residence " +
            fillEndActorsDistancesQuery(query) +
            " group by t_country.id_country, l.id_debate, l.id_sub_debate" +
            having);
  }

  public static List<ActorDistance> getActorsDistancesByProfession(SearchContainer query){
    String having = (query.isActorDistanceByDebate() ? " having id " + isNotBlankOrNull(query.getValue()) : "");
    return getActorsDistances(query,
            "SELECT "  + selectForDebate(query) +
            "if(phl.id_profession_to is null, if(p.profession is null, -1, p.profession), phl.id_profession_to) as 'id', if(phl.id_profession_to is null, if(p.spelling is null, pbis.spelling, p.spelling), if(p2.spelling is null, p2bis.spelling, p2.spelling)) as 'name', " +
            fillBeginActorsDistancesQuery(query) +
            joinForDebate(query) +
            "left join actor a on a.id_contribution = cha.id_actor " +
            "left join person p on p.id_contribution = cha.id_actor " +
            selectByProfession(query) +
            fillEndActorsDistancesQuery(query) +
            " group by id, l.id_debate, l.id_sub_debate" +
            having);
  }

  public static List<ActorDistance> getActorsDistancesByOrganization(SearchContainer query){
    String where = (query.isActorDistanceByDebate() ? " and ac.id_contribution " + isNotBlankOrNull(query.getValue()) : "");
    return getActorsDistances(query,
      "SELECT " + selectForDebate(query) +
              "if(ac.id_contribution is null, -1, ac.id_contribution) as 'id', " + selectActorName() + ", concat(ac.id_contribution, ap.extension) as 'image', a.id_type as 'actor_type', " +
              fillBeginActorsDistancesQuery(query) +
              joinForDebate(query) +
              "left join contribution ac on ac.id_contribution = cha.id_actor " +
              "left join actor a on a.id_contribution = cha.id_actor " +
              joinActorName(query) +
              "left join contributor_picture ap on ap.id_picture = a.id_picture " +
              fillEndActorsDistancesQuery(query) + " and a.id_type = " + EActorType.ORGANIZATION.id() +
              where +
              " group by cha.id_actor, l.id_debate, l.id_sub_debate" +
              " UNION SELECT " + selectForDebate(query) +
              "if(ac.id_contribution is null, -1, ac.id_contribution) as 'id', " + selectActorName() + ", concat(ac.id_contribution, ap.extension) as 'image', a.id_type as 'actor_type', " +
              fillBeginActorsDistancesQuery(query) +
              joinForDebate(query) +
              "left join actor_has_affiliation aha on aha.id_aha = cha.actor_id_aha " +
              "left join contribution ac on ac.id_contribution = aha.id_actor_as_affiliation " +
              "left join actor a on a.id_contribution = ac.id_contribution " +
              joinActorName(query) +
              "left join contributor_picture ap on ap.id_picture = a.id_picture " +
              fillEndActorsDistancesQuery(query) +
              where +
              " group by cha.id_actor, l.id_debate, l.id_sub_debate");
  }

  private static String selectForDebate(SearchContainer query) {
    return query.isActorDistanceByDebate() ?
            "concat(debs." + query.getLang() + ", ' ', argd.title, '?') as 'deb_title', sub_name.name as 'sub_deb_title', " : "";
  }

  private static String joinForDebate(SearchContainer query) {
    return query.isActorDistanceByDebate() ?
            "left join debate deb on deb.id_contribution = l.id_debate " +
            "left join argument arg on arg.id_contribution = deb.id_argument " +
            "left join (" +
            "  SELECT id_contribution, title" +
            "  FROM argument_dictionary" +
            "  where id_language = '" + query.getLang() + "'" +
            ") argd ON argd.id_contribution = arg.id_dictionary " +
            "left join t_debate_shade_type debs ON debs.id_shade = deb.id_shade " +
            "left join (" +
            "  SELECT id_contribution, name" +
            "  FROM tag_i18names" +
            "  where id_language = '" + query.getLang() + "'" +
            ") sub_name ON sub_name.id_contribution = id_sub_debate " : "";
  }

  private static String fillBeginActorsDistancesQuery(SearchContainer query) {
    return "l.id_debate as 'debate', l.id_sub_debate as 'sub_debate', count(distinct(l.id_contribution)) as 'nb_links', AVG(l.id_shade) as 'score' FROM citation_position_link l " +
        "left join contribution c on c.id_contribution = l.id_contribution " +
        "left join contribution_has_actor cha on cha.id_contribution = l.id_citation ";
  }

  private static String fillEndActorsDistancesQuery(SearchContainer query) {
    return getContributionStatsJoins() +
            " where cha.id_actor != " + query.getId() + " and cha.is_author = 1 and " +
            "if(id_sub_debate is null, id_debate, concat(id_debate,';',id_sub_debate)) in " +
            "(SELECT distinct(if(id_sub_debate is null, id_debate, concat(id_debate,';',id_sub_debate))) FROM citation_position_link l2  " +
            "left join contribution_has_actor cha2 on cha2.id_contribution = l2.id_citation  " +
            "where cha2.id_actor = " + query.getId() + " and cha2.is_author = 1) " +
            getContributionStatsWhereClause(query);
  }

  private static List<ActorDistance> getActorsDistances(SearchContainer query, String select) {
    Date date = new Date();
    Map<Long, ActorDistance> preResultsMap = new ConcurrentHashMap<>();
    Map<Long, Double> actorDistances = new ConcurrentHashMap<>();
    Map<Long, Integer> actorLinks = new ConcurrentHashMap<>();

    String actorSelect = "SELECT l.id_debate as 'debate', l.id_sub_debate as 'sub_debate', count(distinct(l.id_contribution)) as 'nb_links', AVG(l.id_shade) as 'score' FROM citation_position_link l " +
            "left join contribution c on c.id_contribution = l.id_contribution " +
            "left join contribution_has_actor cha on cha.id_contribution = l.id_citation " +
            getContributionStatsJoins() +
            " where cha.id_actor = " + query.getId() + " and cha.is_author = 1 " +
            getContributionStatsWhereClause(query) +
            " group by cha.id_actor, l.id_debate, l.id_sub_debate";
    Ebean.createSqlQuery(actorSelect).findList().parallelStream().forEach(row -> {
      Long debateId = row.getLong("debate") + (row.get("sub_debate") != null ? row.getLong("sub_debate") : 0);
      actorDistances.put(debateId, row.getDouble("score"));
      actorLinks.put(debateId, row.getInteger("nb_links"));
    });

    Ebean.createSqlQuery(select).findList().parallelStream().forEach(row -> {
      Long debateId = row.getLong("debate");
      Long subDebateId = row.getLong("sub_debate");
      Long completeDebateId = debateId + (subDebateId != null ? subDebateId : 0);
      Long actorId = row.getLong("id");
      Long id = query.isActorDistanceByDebate() ? completeDebateId : actorId;

      if(!preResultsMap.containsKey(id)){
        preResultsMap.put(id,
                query.isActorDistanceByDebate() ?
                        new ActorDistance(
                                debateId,
                                row.getString("deb_title") +
                                        (row.getString("sub_deb_title") != null ? " - " + row.getString("sub_deb_title") : ""),
                                subDebateId,
                                actorId,
                                row.getString("name")
                        ) :
                        new ActorDistance(
                                id,
                                row.getString("name"),
                                row.containsKey("image") ? row.getString("image") : null,
                                row.containsKey("actor_type") ? row.getInteger("actor_type") : null,
                                row.containsKey("actor_gender") ? row.getString("actor_gender") : null)
        );
      }

      preResultsMap.get(id).addDistance(
              row.getDouble("score"),
              actorDistances.get(completeDebateId),
              row.getInteger("nb_links"),
              actorLinks.get(completeDebateId)
      );
    });

    List<ActorDistance> results = preResultsMap.values().stream().sorted().collect(Collectors.toList());
    //logger.debug((new Date().getTime() - date.getTime()) + "//");
    return results;
  }

  private static String selectByAge() {
    return "@age := IF(p.deathdate is null, CONVERT(YEAR(CURDATE()) - SUBSTR(p.birthdate, 1, 4),UNSIGNED INTEGER), CONVERT(SUBSTR(p.deathdate, 1, 4),UNSIGNED INTEGER) - CONVERT(SUBSTR(p.birthdate, 1, 4),UNSIGNED INTEGER)), " +
            "case " +
            "   when p.birthdate is null then " + EAgeCategory.UNKNOWN.id() +
            "   when @age < 15 then " + EAgeCategory.AGES_0_14.id() +
            "   when @age <= 25 then " + EAgeCategory.AGES_15_25.id() +
            "   when @age <= 35 then " + EAgeCategory.AGES_26_35.id() +
            "   when @age <= 45 then " + EAgeCategory.AGES_36_45.id() +
            "   when @age <= 55 then " + EAgeCategory.AGES_46_55.id() +
            "   when @age <= 65 then " + EAgeCategory.AGES_56_65.id() +
            "   when @age > 65 then " + EAgeCategory.AGES_66_PLUS.id() +
            " END as id, ";
  }

  private static String selectByProfession(SearchContainer query) {
    return  "left join actor_has_affiliation aha on aha.id_aha = cha.actor_id_aha " +
            "left join profession_has_link phl on phl.id_profession_from = aha.function " +
            "left join (" +
            "  SELECT profession, spelling " +
            "  FROM profession_i18names " +
            " WHERE lang = '" + query.getLang() + "' and (gender = 'N' or gender = 'M')" +
            "  GROUP BY profession " +
            "  ) p ON p.profession = aha.function " +
            "left join (" +
            "  SELECT profession, spelling " +
            "  FROM profession_i18names " +
            "  GROUP BY profession " +
            "  ) pbis ON pbis.profession = aha.function " +
            "left join (" +
            "  SELECT profession, spelling " +
            "  FROM   profession_i18names " +
            " WHERE lang = '" + query.getLang() + "' and (gender = 'N' or gender = 'M')" +
            "  GROUP BY profession " +
            "  ) p2 ON p2.profession = phl.id_profession_to " +
            "left join (" +
            "  SELECT profession, spelling " +
            "  FROM   profession_i18names " +
            "  GROUP BY profession " +
            "  ) p2bis ON p2bis.profession = phl.id_profession_to ";
  }

  private static String selectActorName() {
    return  "if(aname.name is null and aname.pseudo is null, c.sortkey, if(aname.name is null, aname.pseudo, if(aname.first_or_acro is null, aname.name, concat(aname.first_or_acro, ' ', aname.name)))) as 'name'";
  }

  private static String joinActorName(SearchContainer query) {
    return  "left join (" +
            "  SELECT id_contribution, name, first_or_acro, pseudo " +
            "  FROM actor_i18names " +
            " WHERE lang = '" + query.getLang() + "' and is_old = 0" +
            "  GROUP BY id_contribution " +
            "  ) aname ON aname.id_contribution = a.id_contribution ";
  }

  private static String countryIdToString(Long id){
    if(isBlank(id)) {
      return "is null";
    } else {
      String idToString = String.valueOf(id);

      if(idToString.length() == 4) {
        return "= '" + ((char) Integer.parseInt(idToString.substring(0, 2))) + ((char) Integer.parseInt(idToString.substring(2, 4))) + "'";
      } else if(idToString.length() == 6) {
        return "= '" + ((char) Integer.parseInt(idToString.substring(0, 3))) + ((char) Integer.parseInt(idToString.substring(3, 6))) + "'";
      } else if(idToString.charAt(0) == '1') {
        return "= '" + ((char) Integer.parseInt(idToString.substring(0, 3))) + ((char) Integer.parseInt(idToString.substring(3, 5))) + "'";
      } else {
        return "= '" + ((char) Integer.parseInt(idToString.substring(0, 2))) + ((char) Integer.parseInt(idToString.substring(2, 5))) + "'";
      }
    }
  }

  /**
   * Get the number of affiliations of this actor
   *
   * @return the number of affiliations of this actor
   */
  public int getNbAffiliations() {
    String select = "select count(distinct aha.id_aha) as 'count' FROM actor_has_affiliation aha " +
            "where aha.id_actor = " + idContribution + " or aha.id_actor_as_affiliation = " + idContribution;

    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  /**
   * Get the number of citations where this actor is author
   *
   * @param whereAuthor true if actor must be author or juste cited otherwise
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return the number of citations where this actor is author
   */
  public int getNbCitations(Boolean whereAuthor, Long contributorId, int groupId){
    return getNbCitations(whereAuthor, contributorId, groupId, null);
  }

  /**
   * Get the number of citations where this actor is author from a specific text (or not)
   *
   * @param whereAuthor true if actor must be author or juste cited otherwise
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @param textId if citation must come from a specific text
   * @return the number of citations where this actor is author
   */
  public int getNbCitations(Boolean whereAuthor, Long contributorId, int groupId, Long textId){
    String select = "select count(distinct cha.id_contribution) as 'count' FROM contribution_has_actor cha" +
            " left join contribution c on cha.id_contribution = c.id_contribution " +
            getContributionStatsJoins() +
            (textId != null ? " left join citation e on e.id_contribution = c.id_contribution " : "") +
            " where cha.id_actor = " + idContribution +
            (whereAuthor != null ? " and cha." + (whereAuthor ? "is_author" : "is_about") + " = 1 " : "") +
            " and c.contribution_type = 3 " + getContributionStatsWhereClause(contributorId, groupId) +
            (textId != null ? " and e.id_text = " + textId : "");
    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  /**
   * Find a list of Actor by their (partial) name among paty member for election 2019
   *
   * @param name a name to search for
   * @param lang the user lang code
   * @return the list of Actors names - party
   */
  public static List<String> findPartyMemberByName(String name, String lang){
    Map<String, Set<Long>> partiesMap = new HashMap<>();
    partiesMap.put("Ecolo",  new HashSet<>(Arrays.asList(new Long[]{20114L,2383L,166270L,25087L,26902L,22136L,26319L,166271L,20105L,27966L,26114L,166272L,25923L,24459L,26653L,166273L,166274L,165468L,26914L,24659L,26949L,166275L,166276L,166277L,164403L,166278L,166279L,25121L,166280L,166281L,166282L,166283L,26096L,166284L,25356L,166285L,166286L,24880L,166287L,23825L,166288L,166289L,166290L,166291L,166292L,166293L,166294L,24799L,26644L,166295L,166296L,166297L,26550L,166298L,164334L,24147L,166299L,24089L,166300L,24154L,166301L,166302L,123292L,166303L,26247L,23862L,165460L,24310L,164593L,24670L,4120L,20068L})));
    partiesMap.put("Open VLD", new HashSet<>(Arrays.asList(new Long[]{6407L,5506L,21899L,5403L,23049L,164734L,24277L,165212L,166999L,167000L,25238L,167001L,167002L,167003L,164811L,167004L,1214L})));
    partiesMap.put("cdH", new HashSet<>(Arrays.asList(new Long[]{6384L,22698L,25263L,28302L,169543L,24774L,20406L,26149L,20107L,27937L,25658L,169245L,169226L,20817L,164725L,28762L,169185L,28347L,165512L,26773L,169544L,169545L,169546L,169547L,169548L,24634L,169549L,171905L,27881L,169551L,169552L,169553L,25331L,169554L,27773L,25920L,27878L,169555L,169556L,169557L,25942L,28225L,169558L,165490L,24494L,169559L,169560L,25723L,169561L,169562L,165502L,24361L,26413L,169563L,28386L,169564L,169565L,169566L,23833L,169567L,169568L,27852L,171775L,169570L,169571L,27831L,170938L,169573L,28058L,169574L,123919L,169575L})));
    partiesMap.put("MR", new HashSet<>(Arrays.asList(new Long[]{3715L,5505L,23838L,19810L,5517L,28327L,5408L,24172L,25628L,23964L,25633L,28109L,25391L,26444L,169277L,23868L,25116L,165611L,169309L,25240L,169518L,169519L,25724L,24354L,25347L,25466L,24336L,24079L,26416L,169520L,169521L,26154L,24647L,24419L,27871L,169522L,25425L,165177L,169523L,169524L,169525L,24588L,169526L,169527L,169228L,25214L,169287L,27725L,25174L,165217L,169528L,164688L,169529L,169530L,169531L,169532L,169243L,169533L,170684L,169535L,25569L,169536L,169537L,169538L,169539L,169540L,169541L,24611L,28164L,169542L,20076L,5513L})));
    partiesMap.put("PP", new HashSet<>(Arrays.asList(new Long[]{169576L,169577L,164786L,169578L,169579L,169580L,169581L,169582L,169583L,169584L,169585L,163835L,169586L,169587L,169588L,169589L,169590L,169591L,2207L,169592L,164153L,169593L,169594L,169595L,169596L,169597L,169598L,169599L,169600L,164069L,163750L,169601L,169602L,169603L,163766L,169604L,169605L,169606L,164660L})));
    partiesMap.put("Pensioen Plus ", new HashSet<>(Arrays.asList(new Long[]{20541L,167675L,167676L,25142L,167677L,167678L,167679L,25085L,28375L,167680L,167681L,167682L,167683L,167684L,27804L,167685L,21668L})));
    partiesMap.put("Vlaams Belang", new HashSet<>(Arrays.asList(new Long[]{20657L,27880L,28291L,169607L,169608L,24744L,169609L,169610L,164596L,169611L,164310L,169612L,169613L,24709L,169614L,165233L,26084L})));
    partiesMap.put("CD&V", new HashSet<>(Arrays.asList(new Long[]{6369L,5508L,21706L,167730L,10078L,23895L,167731L,25267L,167732L,28154L,114151L,167735L,25775L,24457L,167736L,28333L,6372L})));
    partiesMap.put("Défi", new HashSet<>(Arrays.asList(new Long[]{5084L,19877L,6383L,22172L,5410L,5731L,22375L,27848L,26751L,19824L,167686L,24332L,167687L,25338L,25531L,165535L,24316L,26669L,167688L,167689L,167690L,167691L,167692L,167693L,25128L,167694L,167695L,25038L,24480L,167696L,167697L,164658L,167698L,167699L,26869L,167700L,167701L,28124L,167702L,165548L,167703L,27999L,167704L,24944L,167705L,23946L,167706L,167707L,167708L,23836L,167709L,167710L,167711L,25297L,167712L,165550L,167713L,167714L,167715L,167716L,167717L,167718L,167719L,26959L,167720L,167721L,24546L,167723L,167724L,23967L,26460L,20537L})));
    partiesMap.put("PTB", new HashSet<>(Arrays.asList(new Long[]{28055L,23150L,167767L,167768L,116082L,24244L,167769L,26240L,2419L,167770L,164343L,167771L,26003L,28210L,23968L,26536L,165582L,24801L,26104L,167772L,27746L,163755L,167773L,25161L,164017L,164022L,167774L,165225L,167775L,24174L,164341L,167776L,25476L,167777L,167778L,167779L,167780L,167781L,164571L,167782L,167783L,167784L,167785L,167786L,167787L,24462L,28370L,167788L,167789L,167790L,167791L,27815L,25352L,167792L,26486L,167793L,167794L,167795L,28281L,167796L,25831L,167797L,165351L,24645L,167798L,167799L,167800L,167801L,167802L,24524L,26668L,28452L})));
    partiesMap.put("Destexhe", new HashSet<>(Arrays.asList(new Long[]{169623L,25183L,15342L,169624L,169625L,25715L,26098L,169626L,26095L,165604L,169627L,169628L,169629L,169630L,25998L,169631L,116232L,169632L,169633L,169634L,169635L,169636L,169637L,169638L,169639L,169640L,169641L,169642L,164347L,169644L,169645L,169646L,169647L,169648L,169649L,169680L,169650L,169651L,169652L,169653L,169654L,169655L,169656L,169657L,169658L,169659L,169660L,169661L,24848L,169662L,169663L,169664L,169665L,169666L,169667L,169668L,169669L,169670L,169671L,169672L,169673L,25398L,169674L,169675L,24256L,25250L,169676L,169677L,169678L,169679L,26358L,24015L})));
    partiesMap.put("Groen!", new HashSet<>(Arrays.asList(new Long[]{20715L,20310L,167838L,165328L,167839L,164226L,167840L,167841L,167842L,167843L,165238L,167844L,167845L,167846L,167847L,24616L,5502L})));
    partiesMap.put("PS", new HashSet<>(Arrays.asList(new Long[]{6429L,6398L,2176L,2705L,17823L,5399L,166960L,21351L,21437L,21838L,22459L,112867L,20059L,24071L,22437L,164229L,5188L,21898L,26580L,24636L,22923L,166961L,26214L,24706L,21194L,166963L,26952L,24418L,166964L,28017L,28279L,25363L,166965L,26166L,166966L,166967L,27768L,166968L,27844L,166969L,25931L,24866L,26846L,166970L,166971L,24222L,166972L,166973L,166974L,166975L,26165L,24600L,166976L,166977L,166978L,164211L,166979L,166980L,166981L,166982L,166983L,27769L,25741L,166984L,26671L,166985L,166986L,24988L,166987L,24298L,5512L,5400L})));
    partiesMap.put("PVDA", new HashSet<>(Arrays.asList(new Long[]{25981L,167827L,167828L,167829L,167830L,24673L,167831L,167832L,164699L,164143L,26747L,24109L,167833L,167834L,167835L,167836L,25948L})));
    partiesMap.put("Agora", new HashSet<>(Arrays.asList(new Long[]{169683L,169684L,169685L,169686L,169687L,169688L,169689L,169690L,169691L,169692L,169693L,169694L,169695L,740L,169696L,169697L,169698L})));
    partiesMap.put("one.brussels-sp.a", new HashSet<>(Arrays.asList(new Long[]{6421L,169699L,5383L,169700L,165319L,169183L,169701L,169702L,164532L,169703L,169704L,169705L,169706L,169707L,164647L,169708L,20717L})));
    partiesMap.put("Act-Salem", new HashSet<>(Arrays.asList(new Long[]{169709L,169710L,25899L,169711L,169712L,169713L,169714L,169715L,169716L,169717L,169718L,169719L,165567L,169720L,169721L,165069L,169722L,169723L,169724L,169725L,169726L,169727L,169728L,169729L,169730L,169731L,169732L,169733L,169734L,169735L,169736L,169737L,169738L,169739L,169740L,169741L,169742L})));
    partiesMap.put("DierAnimal - FR", new HashSet<>(Arrays.asList(new Long[]{169743L,169744L,169745L,169746L,28089L,169747L,169748L,169749L,169750L,169751L,169752L,169753L,169754L,169755L,169756L})));
    partiesMap.put("Plan B", new HashSet<>(Arrays.asList(new Long[]{163753L,25840L,169757L,169758L,24412L,169759L,169760L,169761L,169762L,164626L,169763L,169764L,169765L,169766L,169767L,169768L})));
    partiesMap.put("Hé", new HashSet<>(Arrays.asList(new Long[]{169769L})));
    partiesMap.put("Be.one", new HashSet<>(Arrays.asList(new Long[]{169770L,169771L,169772L,169773L,169774L,169775L,169776L,169777L})));
    partiesMap.put("Collectif Citoyen", new HashSet<>(Arrays.asList(new Long[]{164742L,169778L,169779L,169780L,169781L,169782L,169783L,169784L,169785L,169786L,26567L,169787L})));
    partiesMap.put("DierAnimal - NL", new HashSet<>(Arrays.asList(new Long[]{169788L,169789L})));
    partiesMap.put("be@eu", new HashSet<>(Arrays.asList(new Long[]{169790L})));

    List<Long> ids = new ArrayList<>();
    partiesMap.values().forEach(ids::addAll);
    List<String> idsString = ids.stream()
            .map(String::valueOf)
            .collect(Collectors.toList());

    String select = "select distinct actor.id_contribution from actor right join actor_i18names " +
            "on actor.id_contribution = actor_i18names.id_contribution where " +
            "concat(ifnull(first_or_acro, ''), concat(ifnull(name, ''), ifnull(pseudo, ''))) " +
            "like '%" + getSearchToken(name) + "%' and id_type = 0 and actor.id_contribution in (" + String.join(",", idsString) + ")";

    List<Actor> actorsResult = Ebean.find(Actor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    List<String> result = new ArrayList<>();

    for(Actor r : actorsResult){

      String partyName = "";

      Iterator<Map.Entry<String, Set<Long>>> iterator = partiesMap.entrySet().iterator();
      while(iterator.hasNext()){
        Map.Entry<String, Set<Long>> candidate = iterator.next();
        if(candidate.getValue().contains(r.getIdContribution())){
          partyName = candidate.getKey();
          break;
        }
      }

      Optional<String> fname = r.getFullNames().stream().findAny();
      if(fname.isPresent()){
        result.add(fname.get() + " (" + partyName + ")");
      }
    }

    return result;
  }

  public List<ActorHasAffiliation> getAffiliationsRelatedTo(List<Long> actorIds){
    String select = "SELECT aha.id_aha from actor_has_affiliation aha " +
            "where aha.id_actor = " + idContribution + " and aha.id_actor_as_affiliation in ("
            + StringUtils.join(actorIds, ',') + ")";

    List<ActorHasAffiliation> result = Ebean.find(ActorHasAffiliation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get all actors where that speak about given actor (authors where given actor is cited)
   *
   * @param query all data needed to get all actors
   * @return a possibly empty list of actors
   */
  public static List<Actor> getOthersActorsCitations(SearchContainer query){
    String select = addFiltersToSql("SELECT distinct a.id_contribution, count(distinct ci.id_contribution) as nb_contributions " +
            getOthersActorsCitationsQuery(query) +
            " group by a.id_contribution " +
            "order by nb_contributions desc " +
            getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Actor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get all actors where that speak about given actor (authors where given actor is cited)
   *
   * @param query all data needed to get all actors
   * @return a possibly empty list of actors
   */
  public static List<Citation> getOthersActorsCitationsList(SearchContainer query){
    String select = "SELECT distinct ci.id_contribution " +
            getOthersActorsCitationsQuery(query) +
            " and cha.id_actor = " + query.getContext() +
            getSearchLimit(query);
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get common query where that speak about given actor (authors where given actor is cited)
   *
   * @param query all data needed to build the query
   * @return a sql query
   */
  public static String getOthersActorsCitationsQuery(SearchContainer query){
    return "from actor a " +
            "inner join contribution c on c.id_contribution = a.id_contribution " +
            "inner join contribution_has_actor cha on cha.id_actor = c.id_contribution " +
            "inner join citation ci on ci.id_contribution = cha.id_contribution " +
            "inner join contribution_has_actor cha2 on cha2.id_contribution = cha.id_contribution " +
            getContributionStatsJoins("ci.id_contribution") +
            " where cha2.is_about = 1 and cha2.id_actor = " + query.getId() + " and cha.id_actor != " + query.getId() +
            getContributionStatsWhereClause(query);
  }

  /**
   * Get all tags where given actors is author or cited in a citation taggued with
   *
   * @param query all data needed to get all tags
   * @return a possibly empty list of tags
   */
  public static List<Tag> getTagsCitations(SearchContainer query){
    String select = addFiltersToSql("SELECT distinct tag.id_contribution, count(distinct ci.id_contribution) as nb_contributions " +
            getTagsCitationsQuery(query) +
            " group by tag.id_contribution " +
            "order by nb_contributions desc " +
            getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get all citations where given actor is author or cited in given tag
   *
   * @param query all data needed to get citations
   * @return a possibly empty list of citations
   */
  public static List<Citation> getTagsCitationsList(SearchContainer query){
    String select = "SELECT distinct ci.id_contribution " +
            getTagsCitationsQuery(query) +
            " and tag.id_contribution = " + query.getContext() +
            getSearchLimit(query);
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get common query where given actors is author or cited in a citation taggued with
   *
   * @param query all data needed to build the query
   * @return a sql query
   */
  private static String getTagsCitationsQuery(SearchContainer query){
    return "from tag " +
            "inner join contribution c on c.id_contribution = tag.id_contribution " +
            "inner join contribution_has_tag cht on cht.id_tag = tag.id_contribution " +
            "inner join citation ci on ci.id_contribution = cht.id_contribution " +
            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
            getContributionStatsJoins("ci.id_contribution") +
            " where cha.id_actor = " + query.getId() + " and " +
            toActorRoleQuery(query.getActorRole()) + getContributionStatsWhereClause(query);
  }

  /**
   * Get all texts where given actors is author or cited in text's citations
   *
   * @param query all data needed to get all texts
   * @return a possibly empty list of texts
   */
  public static List<Text> getTextsCitations(SearchContainer query){
    String select = addFiltersToSql("SELECT distinct t.id_contribution, count(distinct ci.id_contribution) as nb_contributions " +
            getTextsCitationsQuery(query) +
            " group by t.id_contribution " +
            "order by nb_contributions desc " +
            getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get all citations where given actors is author or cited in text's citations
   *
   * @param query all data needed to get all citations
   * @return a possibly empty list of citations
   */
  public static List<Citation> getTextsCitationsList(SearchContainer query){
    String select = "SELECT distinct ci.id_contribution " +
            getTextsCitationsQuery(query) +
            " and t.id_contribution = " + query.getContext() +
            getSearchLimit(query);
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get common query where given actors is author or cited in text's citations
   *
   * @param query all data needed to build the query
   * @return a sql query
   */
  public static String getTextsCitationsQuery(SearchContainer query){
    return "from text t " +
            "inner join contribution c on c.id_contribution = t.id_contribution " +
            "inner join citation ci on ci.id_text = c.id_contribution " +
            "inner join contribution_has_actor cha2 on cha2.id_contribution = ci.id_contribution " +
            getContributionStatsJoins() +
            getContributionStatsJoins("ci.id_contribution ", "1") +
            " where " + toActorRoleQuery(query.getActorRole(), "2") +
            " and cha2.id_actor = " + query.getId() +
            getContributionStatsWhereClause(query) +
            getContributionStatsWhereClause(query, "1");
  }

  /**
   * Get all debates where given actors is cited or where given actors is author or cited in debate's citations
   *
   * @param query all data needed to get all debates
   * @return a possibly empty list of debates
   */
  public static List<Debate> getDebatesCitations(SearchContainer query){
    String select = addFiltersToSql("SELECT distinct d.id_contribution, count(distinct ci.id_contribution) as nb_contributions " +
            getDebatesCitationsQuery(query) +
            " group by d.id_contribution " +
            "order by nb_contributions desc " +
            getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get allcitations where given actors is cited or where given actors is author or cited in debate's citations
   *
   * @param query all data needed to get all citations
   * @return a possibly empty list of citations
   */
  public static List<Citation> getDebatesCitationsList(SearchContainer query){
    String select = "SELECT distinct ci.id_contribution " +
            getDebatesCitationsQuery(query) +
            " and d.id_contribution = " + query.getContext() +
            getSearchLimit(query);
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get common query where given actors is cited or where given actors is author or cited in debate's citations
   *
   * @param query all data needed to build the query
   * @return a sql query
   */
  public static String getDebatesCitationsQuery(SearchContainer query){
    return "from debate d " +
            "inner join contribution c on c.id_contribution = d.id_contribution " +
            "left join contribution_has_actor cha on cha.id_contribution = c.id_contribution " +
            "left join citation_justification_link cjl on cjl.id_context = c.id_contribution " +
            "left join citation_position_link cpl on cpl.id_debate = c.id_contribution " +
            "left join contribution_has_actor cha1 on (cha1.id_contribution = cjl.id_citation or cha1.id_contribution = cpl.id_citation) " +
            "and cha1.id_actor = " + query.getId() + " and " + toActorRoleQuery(query.getActorRole(), "1") +
            " left join citation ci on ci.id_contribution = cha1.id_contribution " +
            getContributionStatsJoins() +
            getContributionStatsJoins("cha1.id_contribution ", "1") +
            " where (" + toActorRoleQuery(query.getActorRole()) + " or " + toActorRoleQuery(query.getActorRole(), "1") +
            ") and (cha.id_actor = " + query.getId() + " or cha1.id_actor = " + query.getId() + ") " +
            getContributionStatsWhereClause(query) +
            getContributionStatsWhereClause(query, "1");
  }

  /**
   * Get all citations where given actors is author or cited
   *
   * @param query all data needed to get all actor's citation
   * @return a possibly empty list of citations
   */
  public static List<Citation> getCitations(SearchContainer query){
    String select = addFiltersToSql("SELECT distinct ci.id_contribution from citation ci " +
            "inner join contribution c on c.id_contribution = ci.id_contribution " +
            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
            getContributionStatsJoins() +
            " where " + toActorRoleQuery(query.getActorRole()) + " and cha.id_actor = " + query.getId() +
            getContributionStatsWhereClause(query) +
            getOrderByContributionDate() + getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /*
   * PRIVATE HELPERS
   */

  private static String toActorRoleQuery(EActorRole role) {
    return toActorRoleQuery(role, "");
  }
  private static String toActorRoleQuery(EActorRole role, String id) {
    return "cha" + id + ".is_" + (role == EActorRole.AUTHOR ? "author" : "about") + " = 1";
  }

  /**
   * Get the texts related texts where given actor is author or cited in texts citations
   *
   * @param whereAuthor true if the actor must be the thinker / author, otherwise he must be cited
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return a possibly empty list of texts
   */
  private List<Text> getTextsWhereCitationRelated(boolean whereAuthor, Long contributorId, int groupId){
    String select = "SELECT distinct t.id_contribution FROM text t " +
            "left join contribution c on c.id_contribution = t.id_contribution " +
            "left join contribution_has_actor cha on cha.id_contribution = t.id_contribution " +
            //"left join citation e on e.id_text = t.id_contribution " +
            //"left join contribution_has_actor cha on cha.id_contribution = e.id_contribution " +
            //"left join contribution c on c.id_contribution = e.id_contribution " +
            getContributionStatsJoins() +
            " where cha.id_actor = " + idContribution + " and cha." + (whereAuthor ? "is_author" : "is_about") + " = 1" +
            getContributionStatsWhereClause(contributorId, groupId) + getOrderByContributionDate();
    return Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Compare two strings by stripping all accents and ignoring case
   *
   * @param s1 a string
   * @param s2 another string
   * @return true if both string are equals ignoring their case and accentuated characters
   */
  private static boolean equalsIgnoreAccents(String s1, String s2) {
    if (s1 == null || s2 == null) {
      return false;
    }
    Collator collator = Collator.getInstance();
    collator.setStrength(Collator.PRIMARY);
    return collator.compare(s1.toLowerCase(), s2.toLowerCase()) == 0;
  }

}
