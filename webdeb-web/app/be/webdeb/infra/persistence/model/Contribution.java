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

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.EPermission;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.impl.contribution.ConcreteModelAttributeDescription;
import be.webdeb.core.impl.contribution.ConcreteModelDescription;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CacheBeanTuning;
import org.reflections.Reflections;

import javax.persistence.*;
import javax.persistence.Version;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The persistent class for the contribution database table, conceptual abstract supertype of texts, actors,
 * arguments and links. May be bound to a list of Topic for texts and arguments.
 *
 * Because of ebean limitations, there is no way to create a class_per_table inheritance, so links to subtypes
 * must be handled by hand.
 *
 * This class is a soft deleted one because we need to trace the whole evolution of contributions in the
 * contribution_has_contributor table, ie, it is flagged as deleted (with the JPA @softdeleted annotation).
 * "Subtype" objects are hard deleted, on the other hand.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.contribution.EContributionType
 */
@Entity
@CacheBeanTuning
@Table(name = "contribution")
public class Contribution extends WebdebModel {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, Contribution> find = new Model.Finder<>(Contribution.class);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contribution_type", nullable = false)
  private TContributionType contributionType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "validated", nullable = false)
  private TValidationState validated;

  @Column(name = "locked")
  @Unqueryable
  private boolean locked;

  @Column(name = "sortkey")
  private String sortkey;

  @Column(name = "hidden")
  @Unqueryable
  private int hidden;

  @Column(name = "deleted")
  @Unqueryable
  private boolean deleted;

  @Version
  @Column(name = "version")
  @Unqueryable
  private Timestamp version;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ContributionHasRelevance relevance;

  // SUBTYPES
  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Actor actor;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Debate debate;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Argument argument;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ArgumentDictionary argumentDictionary;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Citation citation;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Text text;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private Tag tag;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ArgumentJustification argumentJustificationLink;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ArgumentSimilarity argumentSimilarityLink;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private CitationJustification citationJustificationLink;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private DebateSimilarity debateSimilarityLink;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private DebateLink debatelink;
  
  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private TagLink taglink;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ExternalContribution externalContribution;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ContextHasCategory contextHasCategory;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private CitationPosition citationPosition;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private DebateHasText debateHasText;

  @OneToOne(mappedBy = "contribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ContextHasSubDebate contextHasSubDebate;

  @OneToOne(mappedBy = "internalContribution", optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @Unqueryable
  private ExternalContribution originExternalContribution;

  // BINDINGS
  @OneToMany(mappedBy = "contribution", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
  @Unqueryable
  private List<ContributionHasContributor> contributionHasContributors;

  @OneToMany(mappedBy = "contribution", cascade = CascadeType.ALL)
  private List<ContributionHasActor> contributionHasActors;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(
      name="contribution_has_tag",
      joinColumns = { @JoinColumn(name="id_contribution", referencedColumnName = "id_contribution") },
      inverseJoinColumns = { @JoinColumn(name="id_tag", referencedColumnName = "id_contribution") }
  )
  private List<Tag> tags;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(
          name="contribution_has_place",
          joinColumns = { @JoinColumn(name="id_contribution", referencedColumnName = "id_contribution") },
          inverseJoinColumns = { @JoinColumn(name="id_place", referencedColumnName = "id_place") }
  )
  private List<Place> places;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(
      name="contribution_in_group",
      joinColumns = { @JoinColumn(name="id_contribution", referencedColumnName = "id_contribution") },
      inverseJoinColumns = { @JoinColumn(name="id_group", referencedColumnName = "id_group") }
  )
  private List<Group> groups;

  // because we are using raw sql to query database we must create variables to receive column aliases
  @Transient
  @Unqueryable
  private int searched;

  @Transient
  @Unqueryable
  private String name;

  @Transient
  @Unqueryable
  private Timestamp latest;

  @Transient
  @Unqueryable
  private int ctype;

  @Unqueryable
  private static final String ORDER_BY_LATEST = "latest";

  /**
   * Get the contribution id
   *
   * @return an id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set the contribution
   *
   * @param idContribution a unique id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get this contribution type
   *
   * @return the contribution type
   */
  public TContributionType getContributionType() {
    return contributionType;
  }

  /**
   * Set the contribution type
   *
   * @param contributionType the contribution type
   */
  public void setContributionType(TContributionType contributionType) {
    this.contributionType = contributionType;
  }

  /**
   * Get the hit (popularity) value
   *
   * @return the number of hit (amount of visualiation requests) of this contribution
   */
  public Long getHit() {
    return getRelevance() != null ? getRelevance().getHit() : 0;
  }

  /**
   * Check whether this contribution has been validated by a contributor
   *
   * @return the validated state
   */
  public TValidationState getValidated() {
    return validated;
  }

  /**
   * Set the state saying if this contribution has been validated by a contributor
   *
   * @param validated a validated state
   */
  public void setValidated(TValidationState validated) {
    this.validated = validated;
  }

  /**
   * Get the absolute sort key for that contribution (null for argument links) containing either the
   * actor's name, the argument's standard form or the text title
   *
   * @return an absolute search/sort key for that contribution
   */
  public String getSortkey() {
    return sortkey;
  }

  /**
   * Set the absolute sort key for that contribution (must be null for argument links) containing either the
   * actor's name, the argument's standard form or the text title, removing all non alphanumeric characters
   *
   * @param sortkey an absolute search/sort key for that contribution
   */
  public void setSortkey(String sortkey) {
    this.sortkey = sortkey != null && sortkey.length() > 700 ? sortkey.substring(0,699) : sortkey;
  }

  /**
   * Get the relevance of this contribution. The relevance shows consistency of the contribution.
   *
   * @return the relevance
   */
  public ContributionHasRelevance getRelevance() {
    return relevance;
  }

  /**
   * Set the relevance of this contribution. The relevance shows consistency of the contribution.
   *
   * @param relevance the relevance
   */
  public void setRelevance(ContributionHasRelevance relevance) {
    this.relevance = relevance;
  }

  /**
   * Check whether this contribution has been locked by admin
   *
   * @return true if this contribution has been locked
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Set whether this contribution has been locked by admins
   *
   * @param locked true if this contribution has been locked
   */
  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  /**
   * Check whether this contribution has been deleted
   *
   * @return true if this contribution has been deleted
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * Set whether this contribution has been deleted
   *
   * @param deleted true if this contribution has been deleted
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * Check if this contribution must is hidden, ie this contribution (text) only serves as property holder
   * for self-contained arguments (like tweets)
   *
   * @return true if this contribution is an empty shell
   */
  public boolean isHidden() {
    return hidden == 1;
  }

  /**
   * Set whether this contribution must is hidden, ie this contribution (text) only serves as property holder
   * for self-contained arguments (like tweets)
   *
   * @param hidden true if this contribution is an empty shell
   */
  public void isHidden(boolean hidden) {
    this.hidden = hidden ? 1 : 0;
  }

  /**
   * Get the list contributors for this contribution
   *
   * @return a list of joint-objects for contributors of this contribution
   */
  public List<ContributionHasContributor> getContributionHasContributors() {
    return contributionHasContributors;
  }

  /**
   * Set the list contributors for this contribution
   *
   * @param contributionHasContributors a list of joint-objects for contributors of this contribution
   */
  public void setContributionHasContributors(List<ContributionHasContributor> contributionHasContributors) {
    this.contributionHasContributors = contributionHasContributors;
  }

  /**
   * Get the list of actors bound to this contribution (joint-objects)
   *
   * @return the list of actors bound to this contribution
   */
  public List<ContributionHasActor> getContributionHasActors() {
    return contributionHasActors;
  }

  /**
   * Set the list of actors bound to this contribution (joint-objects)
   *
   * @param contributionHasActors the list of actors bound to this contribution
   */
  public void setContributionHasActors(List<ContributionHasActor> contributionHasActors) {
    this.contributionHasActors = contributionHasActors;
  }

  /**
   * Get the actor sub-object, if any
   *
   * @return an actor if contribution_type is actor, null otherwise
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the actor sub-object (won't update contributionType accordingly)
   *
   * @param actor an actor object
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get the debate sub-object, if any
   *
   * @return a debate object if contribution_type is debate, null otherwise
   */
  public Debate getDebate() {
    return debate;
  }

  /**
   * Set the debate sub-object (won't update contributionType accordingly)
   *
   * @param debate a debate object
   */
  public void setDebate(Debate debate) {
    this.debate = debate;
  }

  /**
   * Get the argument sub-object, if any
   *
   * @return an argument object if contribution_type is argument, null otherwise
   */
  public Argument getArgument() {
    return argument;
  }

  /**
   * Set the argument sub-object (won't update contributionType accordingly)
   *
   * @param argument an argument object
   */
  public void setArgument(Argument argument) {
    this.argument = argument;
  }

  /**
   * Get the argument dictionary sub-object, if any
   *
   * @return an argument dictionary object if contribution_type is argument dictionary, null otherwise
   */
  public ArgumentDictionary getArgumentDictionary() {
    return argumentDictionary;
  }

  /**
   * Set the argument dictionar sub-object (won't update contributionType accordingly)
   *
   * @param argumentDictionary an argument dictionar object
   */
  public void setArgumentDictionary(ArgumentDictionary argumentDictionary) {
    this.argumentDictionary = argumentDictionary;
  }

  /**
   * Get the citation sub-object, if any
   *
   * @return an citation object if contribution_type is citation, null otherwise
   */
  public Citation getCitation() {
    return citation;
  }

  /**
   * Set the citation sub-object (won't update contributionType accordingly)
   *
   * @param citation an citation object
   */
  public void setCitation(Citation citation) {
    this.citation = citation;
  }

  /**
   * Get the sub-object text, if any
   *
   * @return a text object if contribution_type is text, null otherwise
   */
  public Text getText() {
    return text;
  }

  /**
   * Set the text sub-object (won't update contributionType accordingly)
   *
   * @param text a text object
   */
  public void setText(Text text) {
    this.text = text;
  }

  /**
   * Get the sub-object tag, if any
   *
   * @return a tag object if contribution_type is tag, null otherwise
   */
  public Tag getTag() {
    return tag;
  }

  /**
   * Set the tag sub-object (won't update contributionType accordingly)
   *
   * @param tag a tag object
   */
  public void setTag(Tag tag) {
    this.tag = tag;
  }

  /**
   * Get the argument justification link sub-object, if any
   *
   * @return an argument justification link if contribution_type is argument justification, null otherwise
   */
  public ArgumentJustification getArgumentJustificationLink() {
    return argumentJustificationLink;
  }

  /**
   * Set the argument justification link sub-object (won't update contributionType accordingly)
   *
   * @param link an argument justification link object
   */
  public void setArgumentJustificationLink(ArgumentJustification link) {
    this.argumentJustificationLink = link;
  }

  /**
   * Get the argument similarity link sub-object, if any
   *
   * @return an argument similarity link  if contribution_type is argument similarity, null otherwise
   */
  public ArgumentSimilarity getArgumentSimilarityLink() {
    return argumentSimilarityLink;
  }

  /**
   * Set the argument similarity link sub-object (won't update contributionType accordingly)
   *
   * @param link an argument similarity link object
   */
  public void setArgumentSimilarityLink(ArgumentSimilarity link) {
    this.argumentSimilarityLink = link;
  }

  /**
   * Get the citation justification link sub-object, if any
   *
   * @return an citation justification link  if contribution_type is citation justification, null otherwise
   */
  public CitationJustification getCitationJustificationLink() {
    return citationJustificationLink;
  }

  /**
   * Set the citation justification link sub-object (won't update contributionType accordingly)
   *
   * @param link an citation justification link object
   */
  public void setCitationJustificationLink(CitationJustification link) {
    this.citationJustificationLink = link;
  }

  /**
   * Get the debate similarity link sub-object, if any
   *
   * @return a debate similarity link if contribution_type is debate similarity, null otherwise
   */
  public DebateSimilarity getDebateSimilarityLink() {
    return debateSimilarityLink;
  }

  /**
   * Set the debate similarity link sub-object (won't update contributionType accordingly)
   *
   * @param link a debate similarity link object
   */
  public void setDebateSimilarityLink(DebateSimilarity link) {
    this.debateSimilarityLink = link;
  }
  
  /**
   * Get the sub-object debate link, if any
   *
   * @return a debate link object if contribution_type is debate link, null otherwise
   */
  public DebateLink getDebateHasTagDebate() {
    return debatelink;
  }

  /**
   * Set the debate link sub-object (won't update contributionType accordingly)
   *
   * @param debateLink a debate object
   */
  public void setDebateHasTagDebate(DebateLink debateLink) {
    this.debatelink = debateLink;
  }

  /**
   * Get the sub-object tag link, if any
   *
   * @return a tag link object if contribution_type is tag link, null otherwise
   */
  public TagLink getTagLink() {
    return taglink;
  }

  /**
   * Set the tag link sub-object (won't update contributionType accordingly)
   *
   * @param tagLink a tag object
   */
  public void setTagLink(TagLink tagLink) {
    this.taglink = tagLink;
  }

  /**
   * Get the external contribution where this contribution come from, if any
   *
   * @return a external contribution if this contribution comes from external source
   */
  public ExternalContribution getOriginExternalContribution() {
    return originExternalContribution;
  }

  /**
   * Get the sub-object external contribution, if any
   *
   * @return an external contribution object if contribution_type is external contribution, null otherwise
   */
  public ExternalContribution getExternalContribution() {
    return externalContribution;
  }

  /**
   * Set the external contribution sub-object (won't update contributionType accordingly)
   *
   * @param externalContribution a ExternalContribution object
   */
  public void setExternalContribution(ExternalContribution externalContribution) {
    this.externalContribution = externalContribution;
  }

  /**
   * Get the sub-object context contribution has category, if any
   *
   * @return a contextHasCategory contribution object if contribution_type is contextHasCategory contribution, null otherwise
   */
  public ContextHasCategory getContextHasCategory() {
    return contextHasCategory;
  }

  /**
   * Set the context contribution sub-object (won't update contributionType accordingly)
   *
   * @param contextHasCategory a ContextHasCategory object
   */
  public void setContextHasCategory(ContextHasCategory contextHasCategory) {
    this.contextHasCategory = contextHasCategory;
  }

  /**
   * Get the sub-object citation has position, if any
   *
   * @return a CitationPosition object if contribution_type is citation position, null otherwise
   */
  public CitationPosition getCitationPosition() {
    return citationPosition;
  }

  /**
   * Set the citation has position sub-object (won't update contributionType accordingly)
   *
   * @param citationPosition a CitationPosition object
   */
  public void setCitationPosition(CitationPosition citationPosition) {
    this.citationPosition = citationPosition;
  }

  /**
   * Get the debate has text sub-object, if any
   *
   * @return a DebateHasText object if contribution_type is debate has text, null otherwise
   */
  public DebateHasText getDebateHasText() {
    return debateHasText;
  }

  /**
   * Set the debate has text sub-object (won't update contributionType accordingly)
   *
   * @param debateHasText a DebateHasText object
   */
  public void setDebateHasText(DebateHasText debateHasText) {
    this.debateHasText = debateHasText;
  }

  /**
   * Get the context has subdebate sub-object, if any
   *
   * @return acontext has subdebate object if contribution_type is context has subdebate, null otherwise
   */
  public ContextHasSubDebate getContextHasSubDebate() {
    return contextHasSubDebate;
  }

  /**
   * Set the  context has subdebate sub-object (won't update contributionType accordingly)
   *
   * @param contextHasSubDebate a ContextHasSubDebate object
   */
  public void setContextHasSubDebate(ContextHasSubDebate contextHasSubDebate) {
    this.contextHasSubDebate = contextHasSubDebate;
  }

  /**
   * Get the list of tags for this contribution
   *
   * @return the list of tags associated to this contribution
   */
  public List<Tag> getTags() {
    return tags;
  }

  /**
   * Set the list of tags for this contribution
   *
   * @param tags the list of tags associated to this contribution
   */
  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  /**
   * Get the list of places that is concerned by this contribution
   *
   * @return a list of geographical places
   */
  public List<Place> getPlaces() {
    return places;
  }

  /**
   * Add a list of places that is concerned by this contribution
   *
   * @param places a list of geographical places
   */
  public void addPlaces(List<Place> places) {
    if(this.places == null || this.places.isEmpty()){
      this.places = places;
    }else{
      for(Place place : places){
        if(this.places.stream().noneMatch(e -> e.getId().equals(place.getId()))){
          this.places.add(place);
        }
      }
    }
  }

  /**
   * Set the list of places that is concerned by this contribution
   *
   * @param places a list of geographical places
   */
  public void setPlaces(List<Place> places) {
    this.places = places;
  }

  /**
   * Add given tag to this contribution.
   *
   * @param tag a tag
   */
  public void addTag(Tag tag) {
      if(tags.stream().noneMatch(e -> e.getIdContribution().equals(tag.getIdContribution()))) {
          tags.add(tag);
      }
  }

  /**
   * Remove given tag to this contribution.
   *
   * @param tag a tag
   */
  public void removeTag(Tag tag) {
      tags.removeIf(f -> f.getIdContribution().equals(tag.getIdContribution()));
  }

  /**
   * Get the relevant groups for which this contribution belongs to, if any
   *
   * @return a list of groups
   */
  public List<Group> getGroups() {
    return groups != null ? groups : new ArrayList<>();
  }

  /**
   * Get the relevant group ids for which this contribution belongs to, if any
   *
   * @return a list of group ids
   */
  public Set<Integer> getGroupIds() {
    Set<Integer> ids = new HashSet<>();
    Ebean.createSqlQuery("SELECT id_group FROM contribution_in_group cig where cig.id_contribution = " + idContribution)
            .findEach(e ->  ids.add(e.getInteger("id_group")));
    return ids;
  }

  /**
   * Set the relevant groups where this contribution is visible
   *
   * @param groups a list of groups
   */
  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  /**
   * Add given group visibility to this contribution.
   *
   * @param group a group
   */
  public void addGroup(Group group) {
    if (groups == null) {
      setGroups(new ArrayList<>());
    }
    if (groups.stream().noneMatch(g -> g.getIdGroup() == group.getIdGroup())) {
      groups.add(group);
    }
  }

  /**
   * Remove given group visibility to this contribution.
   *
   * @param group a group
   */
  public void removeGroup(Group group) {
    if (groups == null) {
      setGroups(new ArrayList<>());
    }
    groups.removeIf(g -> g.getIdGroup() == group.getIdGroup());
  }

  /**
   * Get the current version of this contribution
   *
   * @return a timestamp with the latest update moment of this contribution
   */
  public Timestamp getVersion() {
    return version;
  }

  /**
   * Set the version of this contribution
   *
   * @param version the timestamp with the latest update moment of this contribution
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }

  /*
   * CONVENIENCE METHODS
   */
  @Override
  public String toString() {
    switch (EContributionType.value(getContributionType().getIdContributionType())) {
      case ACTOR:
        return getActor().toString();
      case DEBATE:
        return getDebate().toString();
      case ARGUMENT:
        return getArgument().toString();
      case CITATION:
        return getCitation().toString();
      case TEXT:
        return getText().toString();
      case TAG:
        return getTag().toString();
      case ARGUMENT_JUSTIFICATION:
        return getArgumentJustificationLink().toString();
      case ARGUMENT_SIMILARITY:
        return getArgumentSimilarityLink().toString();
      case CITATION_JUSTIFICATION:
        return getCitationJustificationLink().toString();
      case DEBATE_SIMILARITY:
        return getDebateSimilarityLink().toString();
      case DEBATE_HAS_TAG_DEBATE:
        return getDebateHasTagDebate().toString();
      case TAG_LINK:
        return getTagLink().toString();
      case EXTERNAL_TEXT:
      case EXTERNAL_CITATION:
        return getExternalContribution().toString();
      case CONTEXT_HAS_CATEGORY:
        return getContextHasCategory().toString();
      case CITATION_POSITION:
        return getCitationPosition().toString();
      case DEBATE_HAS_TEXT:
        return getDebateHasText().toString();
      case CONTEXT_HAS_SUBDEBATE:
        return getContextHasSubDebate().toString();
      default:
        logger.error("wrong contribution type " + getContributionType().getIdContributionType());
        return "";
    }
  }

  /**
   * Retrieve most relevant actor involved in of this contribution
   *
   * @return the ContributionHasActor with the isAuthor field set to true
   */
  public ContributionHasActor getMostActorRelevantActor() {
    String select = "SELECT cha.id_cha FROM webdeb.contribution_has_actor cha " +
            "inner join contribution_has_relevance chr on chr.id_contribution = cha.id_actor " +
            "where cha.id_contribution = " + idContribution + " and cha.is_author = 1 " +
            "order by chr.relevance desc " +
            "limit 1";
    return Ebean.find(ContributionHasActor.class).setRawSql(RawSqlBuilder.parse(select).create()).findUnique();
  }

  /**
   * Retrieve all actors involved in of this contribution
   *
   * @return the list<ContributionHasActor> with the isAuthor field set to true
   */
  public List<ContributionHasActor> getActors() {
    return getContributionHasActors();
  }

  /**
   * Retrieve all actors flagged as "authors" of this contribution
   *
   * @return the list<ContributionHasActor> with the isAuthor field set to true
   */
  public List<ContributionHasActor> getAuthors() {
    return getContributionHasActors().stream().filter(ContributionHasActor::isAuthor).collect(Collectors.toList());
  }

  /**
   * Get the map of number of linked elements with this contribution
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the id of the group where stats must be counted
   * @return the map of number of linked elements with this contribution by contribution type
   */
  public Map<EContributionType, Integer> getCountRelationsMap(Long contributorId, int groupId){
    Map<EContributionType, Integer> map = new LinkedHashMap<>();

    switch (contributionType.getEContributionType()) {
      case ACTOR:
        map.put(EContributionType.AFFILIATION, getActor().getNbAffiliations());
        map.put(EContributionType.CITATION,
                getActor().getNbCitations(true, contributorId, groupId) +
                getActor().getNbCitations(false, contributorId, groupId));
        break;
      case DEBATE:
        map.put(EContributionType.ARGUMENT_JUSTIFICATION, getDebate().getNbLinks(EContributionType.ARGUMENT_JUSTIFICATION, contributorId, groupId));
        map.put(EContributionType.CITATION, getDebate().getNbLinks(EContributionType.CITATION_JUSTIFICATION, contributorId, groupId) + getDebate().getNbLinks(EContributionType.CITATION_POSITION, contributorId, groupId));
        break;
      case TAG:
        map.put(EContributionType.DEBATE, Tag.getNbContributionsByType(idContribution, EContributionType.DEBATE, contributorId, groupId));
        map.put(EContributionType.CITATION, Tag.getNbContributionsByType(idContribution, EContributionType.CITATION, contributorId, groupId));
        map.put(EContributionType.TAG_LINK, Tag.getNbLinks(idContribution, contributorId, groupId));
        break;
      case TEXT:
        map.put(EContributionType.CITATION, getText().getNbCitations(contributorId, groupId));
        break;
    }

    return map;
  }

  @Override
  public boolean delete() {
    boolean r;
    setDeleted(true);
    update();
    getContributionHasActors().forEach(Model::delete);

    switch (EContributionType.value(getContributionType().getIdContributionType())) {
      case ACTOR:
        return getActor().delete();
      case DEBATE:
        return getDebate().delete();
      case ARGUMENT:
        return getArgument().delete();
      case CITATION:
        return getCitation().delete();
      case TEXT:
        return getText().delete();
      case TAG:
        return getTag().delete();
      case ARGUMENT_JUSTIFICATION:
        return getArgumentJustificationLink().delete();
      case ARGUMENT_SIMILARITY:
        return getArgumentSimilarityLink().delete();
      case CITATION_JUSTIFICATION:
        return getCitationJustificationLink().delete();
      case DEBATE_SIMILARITY:
        return getDebateSimilarityLink().delete();
      case DEBATE_HAS_TAG_DEBATE:
        return getDebateHasTagDebate().delete();
      case TAG_LINK:
        return getTagLink().delete();
      case EXTERNAL_TEXT:
        r = getExternalContribution().delete();
        return r;
      case EXTERNAL_CITATION:
        r = getExternalContribution().delete();
        return r;
      case CONTEXT_HAS_CATEGORY:
        return getContextHasCategory().delete();
      case CITATION_POSITION:
        return getCitationPosition().delete();
      case DEBATE_HAS_TEXT:
        return getDebateHasText().delete();
      case CONTEXT_HAS_SUBDEBATE:
        return getContextHasSubDebate().delete();
      default:
        logger.warn("trying to delete a wrong contribution " + getIdContribution());
        return false;
    }
  }

  /**
   * Check if bound contribution contains given searchText in Contribution's sortkey field
   * or, in case of actors, also checks in all names
   *
   * @param searchText a string to search for (ignore case)
   * @return true if given search text has been found, false otherwise
   */
  public boolean matchSearchForContribution(String searchText){
    if(searchText != null){
      searchText = searchText.toLowerCase();
      if(searchText.equals("") || searchText.contains(getSortkey().toLowerCase())
          || getSortkey().toLowerCase().contains(searchText)
          || analyseContributionsNames(searchText)){
        return true;
      }
    }
    return false;
  }

  /**
   * Check if given contributor is member of a pedagogic group where given contribution is
   *
   * @param contributorId a contributor id
   * @param contributionId a contribution id
   * @return true if the contributor is member of a pedagogic group where given contribution is
   */
  public static boolean contributorIsPedagogicForGroupAndContribution(Long contributorId, Long contributionId) {
    String select = "SELECT g.id_group FROM contributor_group g " +
            "inner join contribution_in_group cig on cig.id_group = g.id_group " +
            "inner join contributor_has_group chg on chg.id_group = g.id_group " +
            "where is_pedagogic = 1 and cig.id_contribution = " + contributionId + " and chg.id_contributor = " + contributorId;

    return !Ebean.find(Group.class).setRawSql(RawSqlBuilder.parse(select).create()).findList().isEmpty();
  }

  private boolean analyseContributionsNames(String searchText){
    return getActor() != null && getActor().getFullNames().stream().anyMatch(n ->
          n.toLowerCase().contains(searchText) || searchText.contains(n.toLowerCase()));
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve a Contribution by its id
   *
   * @param id an id
   * @return the Contribution corresponding to the given id, or null if not found
   */
  public static Contribution findById(Long id) {
    if (id == null || id == -1L) {
      return null;
    }

    return find.byId(id);
  }

  /**
   * Find a list of contributions containing given criteria
   *
   * @param value a value to search for in the aggregated sortKey column
   * @param group the group id to search in
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of contributions matching given search value
   */
  public static List<Contribution> findBySortKey(String value, int group, int fromIndex, int toIndex) {
    if (value == null) {
      return new ArrayList<>();
    }

    Query<Contribution> query = Ebean.find(Contribution.class);
    // will be automatically ordered by relevance
    String token = getSearchToken(value);

    String select = "select contribution.id_contribution, sortkey from contribution left join contribution_in_group on " +
        "contribution.id_contribution = contribution_in_group.id_contribution where sortkey like '%" + token + "%' " +
        "and contribution.contribution_type in (" + EContributionType.getMajorContributionTypes().stream().map(String::valueOf).collect(Collectors.joining(",")) + ") and (" +
        "id_group in (" + String.join(",", Group.getVisibleGroupsFor(group)) + ") " +
        "or id_group in (" + String.join(",", Group.getVisibleGroupsFor(Group.getPublicGroup().getIdGroup())) + ")) " +
        "and contribution.deleted = 0 and contribution.hidden = 0 " +
        "ORDER BY CASE WHEN contribution.sortkey like '" + token + "%' THEN 0 " +
        "WHEN sortkey like '" + token + " %' THEN 1 " +
        "WHEN sortkey like '% " + token + "%' THEN 2 " +
        "ELSE 3 END, sortkey" + getSearchLimit(fromIndex, toIndex);
    List<Contribution> result = query.setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }


  /**
   * Retrieve all contributions by given criteria, ie, key-value pairs as accepted by QueryExecutor v2
   *
   * @param criteria a list of key,value pairs
   * @param strict check if we must perform a strict search
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of contributions that matches given criteria
   */
  public static List<Contribution> findByCriteriaV2(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex) {
    String query;
    String commonJoins;

    List<String> selects = new ArrayList<>();
    // handle given keys
    List<String> types = new ArrayList<>();
    Set<EContributionType> eTypes = new HashSet<>();
    List<String> contributionIds = new ArrayList<>();
    // contribution ids to ignore
    List<String> ignoreIds = new ArrayList<>();
    String ignoreIdsCh = "";

    // context ids to ignore
    List<String> ignoreContextIds = new ArrayList<>();
    // text ids where to look
    List<String> lookTextIds = new ArrayList<>();

    // for texts
    String textTitle = null;
    String textType = null;
    String textAuthor = null;
    String textSource = null;

    // actors
    String actorName = null;
    String actorType = null;
    String function = null;

    // debates
    String debateTitle = null;

    // citations
    String citationExcerpt = null;
    String citationAuthor = null;
    String citationSource = null;

    // tags
    String tag = null;

    // contribution's tags
    String contributionTag = null;
    String contributionTagQuery;
    // contribution's places
    String contributionPlace = null;
    String contributionPlaceQuery;

    String fromDate = null;
    String toDate = null;
    String dateTextualFilter;
    String datePersonFilter;
    String dateOrganizationFilter;

    // involved actors in contribution
    EnumMap<EQueryKey, List<String>> involvedActors = new EnumMap<>(EQueryKey.class);
    involvedActors.put(EQueryKey.ACTOR, new ArrayList<>());
    involvedActors.put(EQueryKey.AUTHOR, new ArrayList<>());
    involvedActors.put(EQueryKey.REPORTER, new ArrayList<>());
    involvedActors.put(EQueryKey.SOURCE_AUTHOR, new ArrayList<>());

    String contributorFilter;
    String contributor = null;

    // for visibility management
    String groupFilter;
    List<String> groups = new ArrayList<>();
    Set<String> public_groups = new HashSet<>(Group.getVisibleGroupsFor(Group.getPublicGroup().getIdGroup()));

    // build query helpers with given keys
    for (Map.Entry<EQueryKey, String>  c : criteria) {
      if (c.getValue() != null) {
        String value = c.getValue().trim();
        String token = (strict ? "" : "%") + value.replace("'", "\\'") + (strict ? "" : "%");

        switch (c.getKey()) {
          // group-related
          case GROUP:
            groups.add(c.getValue());
            break;
          case AMONG_GROUP:
            groups.add(c.getValue());
            break;
          // contributor
          case CONTRIBUTOR:
            contributor = c.getValue();
            break;
          // contribution id
          case ID_CONTRIBUTION:
            contributionIds.add(c.getValue());
            break;
          // contribution id to ignore
          case ID_IGNORE:
            ignoreIds.add(c.getValue());
            break;
          // context contribution id to ignore
          case CONTEXT_TO_IGNORE:
            ignoreContextIds.add(c.getValue());
            break;
          // text id where to look
          case TEXT_TO_LOOK:
            lookTextIds.add(c.getValue());
            break;
          // textual contributions
          case CONTRIBUTION_TYPE:
            types.add(c.getValue());
            break;
          case ACTOR:
          case AUTHOR:
          case REPORTER:
          case SOURCE_AUTHOR:
            involvedActors.get(c.getKey()).add(token);
            break;
          // actors
          case ACTOR_NAME:
            actorName = value.isEmpty() ? null : token;
            break;
          case ACTOR_TYPE:
            actorType = c.getValue();
            break;
          case FUNCTION:
            function = token;
            break;

          // texts
          case TEXT_TITLE:
            textTitle = value.isEmpty() ? null : token;
            break;
          case TEXT_TYPE:
            textType = token;
            break;
          case TEXT_AUTHOR:
            textAuthor = value;
            break;
          case TEXT_SOURCE:
            textSource = value;
            break;

          // debates
          case DEBATE_TITLE:
            if(!value.isEmpty()) {
              String tempToken = value.replace("?", "");
              for (TDebateShadeType shade : TDebateShadeType.find.all()) {
                for (String shadeName : shade.getTechnicalNames().values()) {
                  if (tempToken.startsWith(shadeName)) {
                    tempToken = tempToken.replace(shadeName, "");
                    break;
                  }

                  if (shadeName.endsWith(" que")) {
                    String newShadeName = shadeName.replace(" que", " qu'");

                    if (tempToken.startsWith(newShadeName)) {
                      tempToken = tempToken.replace(newShadeName, "");
                      break;
                    }
                  }
                }
              }
              debateTitle = (strict ? "" : "%") + tempToken.replace("'", "\\'").trim() + (strict ? "" : "%");
            }
            break;

          // citations
          case CITATION_TITLE:
            citationExcerpt = value.isEmpty() ? null : token;
            break;
          case CITATION_AUTHOR:
            citationAuthor = value;
            break;
          case CITATION_SOURCE:
            citationSource = value;
            break;

          // tags
          case TAG_NAME:
            tag = value.isEmpty() ? null : token;
            break;

          // contribution's tags
          case TAG:
            contributionTag = c.getValue();
            break;

          // contribution's place
          case PLACE:
            contributionPlace = c.getValue();
            break;

          case FROM_DATE:
            fromDate = c.getValue();
            break;

          case TO_DATE:
            toDate = c.getValue();
            break;

          default:
            logger.warn("unsupported key given " + c.getKey() + " with value " + c.getValue());
        }
      }
    }

    eTypes = types.stream().map(t -> EContributionType.value(Integer.decode(t))).collect(Collectors.toSet());

    if(textAuthor != null || textSource != null) {
      eTypes = new HashSet<>(Arrays.asList(EContributionType.TEXT, EContributionType.CITATION));
    }

    if(!ignoreIds.isEmpty()) {
      ignoreIdsCh = " and c.id_contribution not in (" + String.join(",", ignoreIds) + ") ";
    }

    commonJoins = " inner join contribution_in_group cig on cig.id_contribution = c.id_contribution " +
            " inner join contribution_has_contributor chc on chc.id_contribution = c.id_contribution ";

    if(groups.size() == 1)
      groups = Group.getVisibleGroupsFor(Integer.parseInt(groups.get(0)));

    public_groups.addAll(groups);
    groupFilter = " and cig.id_group in (" + String.join(",", public_groups) + ") ";

    contributorFilter = contributor != null ? " and chc.id_contributor = " + contributor + " " : "";

    String tagInners = "left join contribution_has_tag cht on cht.id_contribution = c.id_contribution " +
            "left join tag_i18names chtn on cht.id_tag = chtn.id_contribution ";

    String placeInners = "left join contribution_has_place chp on chp.id_contribution = c.id_contribution " +
            "left join place on place.id_place = chp.id_place ";

    contributionTagQuery = contributionTag != null ? " and chtn.name like '%" + contributionTag + "%' " : "";

    contributionPlaceQuery = contributionPlace != null ? " and (place.id_place = " + contributionPlace +
            " or place.id_continent = " + contributionPlace + " or place.id_country = " + contributionPlace +
            " or place.id_region = " + contributionPlace + " or place.id_subregion = " + contributionPlace + ") " : "";

    dateTextualFilter = fromDate != null && toDate != null ? (" and " + (fromDate.equals("null") ? "" : "t.publication_date >= '" +
            (fromDate) + "'" + (toDate.equals("null") ? "" : " and ")) +
            (toDate.equals("null") ? "" : "t.publication_date <= '" + (toDate) + "'")) : "";

    datePersonFilter = fromDate != null && toDate != null ? (" and (p.id_contribution is null or (" + (fromDate.equals("null") ? "" : "p.birthdate >= '" +
            (fromDate) + "'" + (toDate.equals("null") ? "" : " and ")) +
            (toDate.equals("null") ? "" : "p.deathdate <= '" + (toDate) + "'") + "))") : "";

    dateOrganizationFilter = fromDate != null && toDate != null ? (" and (o.id_contribution is null or (" + (fromDate.equals("null") ? "" : "o.creation_date >= '" +
            (fromDate) + "'" + (toDate.equals("null") ? "" : " and ")) +
            (toDate.equals("null") ? "" : "o.termination_date <= '" + (toDate) + "'") + "))") : "";

    // ACTORS
    if (actorName != null && (eTypes.isEmpty() || eTypes.contains(EContributionType.ACTOR))) {

      if(actorType == null || actorType.equals(String.valueOf(EActorType.UNKNOWN.id()))) {
        selects.add("select distinct an.id_contribution, length(an.name) + 1000 as searched from actor_i18names an " +
                "inner join actor a on a.id_contribution = an.id_contribution " +
                "inner join contribution c on c.id_contribution = a.id_contribution " +
                commonJoins +
                "where an.name like '" + actorName + "' and a.id_type = " + EActorType.UNKNOWN.id() +
                ignoreIdsCh + groupFilter);
      }

      if(actorType == null || actorType.equals(String.valueOf(EActorType.PERSON.id()))) {
        selects.add("select distinct an.id_contribution, length(if(an.pseudo is null, concat(an.first_or_acro, ' ', an.name), an.pseudo)) as searched from actor_i18names an " +
                "inner join actor a on a.id_contribution = an.id_contribution " +
                "inner join contribution c on c.id_contribution = a.id_contribution " +
                "inner join person p on p.id_contribution = a.id_contribution " +
                commonJoins +
                "where if(an.pseudo is null, concat(an.first_or_acro, ' ', an.name), concat(an.first_or_acro, ' ', an.name, ' (',an.pseudo, ')')) like '" + actorName + "'" +
                datePersonFilter +
                ignoreIdsCh + groupFilter + contributorFilter);
      }

        if(actorType == null || actorType.equals(String.valueOf(EActorType.ORGANIZATION.id()))) {
          selects.add("select distinct an.id_contribution, length(if(an.first_or_acro is null, an.name, concat(an.first_or_acro, ' ', an.name))) as searched from actor_i18names an " +
                  "inner join actor a on a.id_contribution = an.id_contribution " +
                  "inner join contribution c on c.id_contribution = a.id_contribution " +
                  "inner join organization o on o.id_contribution = a.id_contribution " +
                  commonJoins +
                  tagInners +
                  placeInners +
                  "where (if(an.first_or_acro is null, an.name, concat(an.first_or_acro, ' ', an.name)) like '" + actorName + "' " +
                  "or if(an.first_or_acro is null, an.name, concat(an.first_or_acro, ' - ', an.name)) like '" + actorName + "') " +
                  dateOrganizationFilter +
                  contributionTagQuery +
                  contributionPlaceQuery +
                  ignoreIdsCh + groupFilter + contributorFilter);

          selects.add("select distinct an.id_contribution, length(an.first_or_acro) as searched from actor_i18names an " +
                  "inner join actor a on a.id_contribution = an.id_contribution " +
                  "inner join contribution c on c.id_contribution = a.id_contribution " +
                  "inner join organization o on o.id_contribution = a.id_contribution " +
                  commonJoins +
                  "where an.first_or_acro is not null and an.first_or_acro like '" + actorName + "'" +
                  dateOrganizationFilter +
                  ignoreIdsCh + groupFilter + contributorFilter);
        }
    }

    if (debateTitle != null && (eTypes.isEmpty() || eTypes.contains(EContributionType.DEBATE))) {

      selects.add("SELECT distinct d.id_contribution, length(ad.title) as searched FROM debate d " +
              "inner join argument a on d.id_argument = a.id_contribution " +
              "inner join contribution c on c.id_contribution = d.id_contribution " +
              "inner join argument_dictionary ad on a.id_dictionary = ad.id_contribution " +
              commonJoins +
              tagInners +
              placeInners +
              "where ad.title like '" + debateTitle + "'" +
              contributionTagQuery +
              contributionPlaceQuery +
              ignoreIdsCh + groupFilter + contributorFilter);
    }

    if (tag != null && (eTypes.isEmpty() || eTypes.contains(EContributionType.TAG))) {
      selects.add("SELECT distinct tag.id_contribution, length(tn.name) as searched FROM tag " +
              "inner join contribution c on c.id_contribution = tag.id_contribution " +
              "inner join tag_i18names tn on tn.id_contribution = tag.id_contribution " +
              "left join tag_rewording_i18names trn on trn.id_contribution = tag.id_contribution " +
              commonJoins +
              "where (tn.name like '" + tag + "' or trn.name like '" + tag + "')" +
              ignoreIdsCh + groupFilter + contributorFilter);
    }

    if (textTitle != null && (eTypes.isEmpty() || eTypes.contains(EContributionType.TEXT))) {

      selects.add("SELECT distinct t.id_contribution, length(tn.spelling) as searched FROM text t " +
              "inner join contribution c on c.id_contribution = t.id_contribution " +
              "inner join text_i18names tn on t.id_contribution = tn.id_contribution " +
              (textAuthor != null ? "left join contribution_has_actor cha on t.id_contribution = cha.id_contribution " : "") +
              (textSource != null ? "left join text_source_name ts on t.id_source_name = ts.id_source " : "") +
              commonJoins +
              tagInners +
              placeInners +
              "where tn.spelling like '" + textTitle + "'" +
              (textType != null ? (" and t.id_type = " + textType) : "") +
              (textAuthor != null ? (" and cha.is_author = 1 and cha.id_actor = " + textAuthor) : "") +
              (textSource != null ? (" and ts.name like '%" + textSource) + "%'" : "") +
              dateTextualFilter +
              contributionTagQuery +
              contributionPlaceQuery +
              ignoreIdsCh + groupFilter + contributorFilter);
    }

    if (citationExcerpt != null && (eTypes.isEmpty() || eTypes.contains(EContributionType.CITATION))) {
      selects.add("SELECT distinct ci.id_contribution, length(ci.original_excerpt) as searched FROM citation ci " +
              "inner join contribution c on c.id_contribution = ci.id_contribution " +
              "left join text t on t.id_contribution = ci.id_text " +
              (textAuthor != null ? "left join contribution_has_actor cha on ci.id_contribution = cha.id_contribution " : "") +
              (textSource != null ? "left join text_source_name ts on t.id_source_name = ts.id_source " : "") +
              commonJoins +
              tagInners +
              placeInners +
              "where ci.original_excerpt like '" + citationExcerpt + "'" +
              (textAuthor != null ? (" and cha.is_author = 1 and cha.id_actor = " + textAuthor) : "") +
              (textSource != null ? (" and ts.name like '%" + textSource) + "%'" : "") +
              dateTextualFilter +
              contributionTagQuery +
              contributionPlaceQuery +
              ignoreIdsCh + groupFilter + contributorFilter);

      selects.add("SELECT distinct ci.id_contribution, length(ci.working_excerpt) as searched FROM citation ci " +
              "inner join contribution c on c.id_contribution = ci.id_contribution " +
              "left join text t on t.id_contribution = ci.id_text " +
              (textAuthor != null ? "left join contribution_has_actor cha on ci.id_contribution = cha.id_contribution " : "") +
              (textSource != null ? "left join text_source_name ts on t.id_source_name = ts.id_source " : "") +
              commonJoins +
              tagInners +
              placeInners +
              "where ci.working_excerpt like '" + citationExcerpt + "'" +
              (textAuthor != null ? (" and cha.is_author = 1 and cha.id_actor = " + textAuthor) : "") +
              (textSource != null ? (" and ts.name like '%" + textSource) + "%'" : "") +
              dateTextualFilter +
              contributionTagQuery +
              contributionPlaceQuery +
              ignoreIdsCh + groupFilter + contributorFilter);
    }

    // check if some specific criteria have been passed, otherwise, maybe only contribution types have been requested
    // so no specific selects have been built
    //String query;
    boolean byType = selects.isEmpty();

    if (byType) {

      List<EContributionType> ctypes = eTypes.isEmpty() ?
              EContributionType.getMajorContributionETypes() :
              new ArrayList<>(eTypes);

      for (EContributionType t : ctypes) {
        String select =  "select distinct c.id_contribution, c.version as version from contribution c " + commonJoins + tagInners + placeInners;
        String where = "where c.deleted = 0 and c.contribution_type = " + t.id() +
                ignoreIdsCh + groupFilter + contributorFilter + contributionTagQuery + contributionPlaceQuery;

        switch (t) {

          case ACTOR:
            selects.add(select + "inner join actor a on a.id_contribution = c.id_contribution " +
                    "left join person p on p.id_contribution = a.id_contribution " +
                    "left join organization o on o.id_contribution = a.id_contribution " +
                    where + (actorType != null ? (" and a.id_type = " + actorType) + " " : "") + datePersonFilter + dateOrganizationFilter);
            break;
          case DEBATE:
            selects.add(select + "inner join debate d on d.id_contribution = c.id_contribution " + where);
            break;
          case TEXT:
            selects.add(select + "inner join text t on t.id_contribution = c.id_contribution " +
                    (textAuthor != null ? "left join contribution_has_actor cha on t.id_contribution = cha.id_contribution " : "") +
                    (textSource != null ? "left join text_source_name ts on t.id_source_name = ts.id_source " : "") +
                    where +
                    (textAuthor != null ? (" and cha.is_author = 1 and cha.id_actor = " + textAuthor) + " " : "") +
                    (textSource != null ? (" and ts.name like '%" + textSource) + "%' " : "") +
                    (textType != null ? (" and t.id_type = " + textType + " ") : "") +
                    dateTextualFilter);
            break;
          case CITATION:
            selects.add(select + "inner join citation ci on ci.id_contribution = c.id_contribution left join text t on t.id_contribution = ci.id_text " +
                    (textAuthor != null ? "left join contribution_has_actor cha on ci.id_contribution = cha.id_contribution " : "") +
                    (textSource != null ? "left join text_source_name ts on t.id_source_name = ts.id_source " : "") +
                    where +
                    (textAuthor != null ? (" and cha.is_author = 1 and cha.id_actor = " + textAuthor) + " " : "") +
                    (textSource != null ? (" and ts.name like '%" + textSource) + "%' " : "") +
                    dateTextualFilter);
            break;
          case TAG:
            selects.add(select + "inner join tag on tag.id_contribution = c.id_contribution " +
                    where + " and tag.id_type = " + ETagType.SIMPLE_TAG.id());
            break;

          default:
            selects.add(select + where);
        }
      }
    }

    query = String.join(" union ", selects) +
            (byType ? " order by version desc" : " order by searched") +
            getSearchLimit(fromIndex, toIndex, 100);

    // execute query
    //logger.debug("will now execute query " + query);
    RawSql rawSql = RawSqlBuilder.parse(query).create();
    return new ArrayList<>(Ebean.find(Contribution.class).setRawSql(rawSql).findSet());
  }

  /**
   * Retrieve all contributions by given criteria, ie, key-value pairs as accepted by QueryExecutor
   *
   * @param criteria a list of key,value pairs
   * @param strict check if we must perform a strict search
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of contributions that matches given criteria
   */
  public static List<Contribution> findByCriteria(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex) {
    logger.debug("find by criteria " + criteria.toString());
    Set<Contribution> contributions;

    List<String> selects = new ArrayList<>();
    // handle given keys
    List<String> types = new ArrayList<>();
    List<String> contributionIds = new ArrayList<>();
    // contribution ids to ignore
    List<String> ignoreIds = new ArrayList<>();

    // context ids to ignore
    List<String> ignoreContextIds = new ArrayList<>();
    // context ids where to look
    List<String> lookTextIds = new ArrayList<>();

    // for texts
    List<String> textTitles = new ArrayList<>();
    List<String> textSources = new ArrayList<>();

    // actors
    List<String> actorNames = new ArrayList<>();
    List<String> actorTypes = new ArrayList<>();
    List<String> functions = new ArrayList<>();

    // arguments
    List<String> argumentTitles = new ArrayList<>();

    // debates
    List<String> debatesTitles = new ArrayList<>();

    // citations
    List<String> originalCitations = new ArrayList<>();

    // tags
    List<String> tags = new ArrayList<>();

    // involved actors in contribution
    EnumMap<EQueryKey, List<String>> involvedActors = new EnumMap<>(EQueryKey.class);
    involvedActors.put(EQueryKey.ACTOR, new ArrayList<>());
    involvedActors.put(EQueryKey.AUTHOR, new ArrayList<>());
    involvedActors.put(EQueryKey.REPORTER, new ArrayList<>());
    involvedActors.put(EQueryKey.SOURCE_AUTHOR, new ArrayList<>());

    // constant sql partial queries
    final String mode = "' in boolean mode)";
    final String relevance = " as relevance ";
    final String searched = " as searched ";
    boolean addSearched = false;
    boolean amongGroup = false;
    boolean allResults = false;
    final String from = " from contribution ";
    final String select_contribution_sortkey = "select contribution.id_contribution, contribution_type as ctype, sortkey as name ";
    String contributor = null;
    String ids = "";

    // for visibility management
    List<String> groups = new ArrayList<>();
    List<String> notInGroups = new ArrayList<>();
    List<String> public_groups = Group.getVisibleGroupsFor(Group.getPublicGroup().getIdGroup());
    final String groupJoin = " left join contribution_in_group on contribution.id_contribution = contribution_in_group.id_contribution ";
    final String groupFilter;
    final String simpleGroupFilter;
    final String groupPublicFilter;
    final String simpleGroupPublicFilter;
    final String groupMainFilter;
    String validatedFilter = ""; // empty by default
    String fetchedFilter = ""; // empty by default

    // default select template clause
    final String select = "select contribution.id_contribution, contribution_type as ctype, ";
    final String nameMatch = "match(first_or_acro, name, pseudo) against ('";

    // lower id to search from
    String lowest = "0";

    // order by key for contribution_type only requests
    String orderBy = "name";

    // sql where clause
    String where = "where ";

    // char lenght clause
    String charLength = ", char_length(sortkey) ";

    // relevance score
    String maxRelevance = "100";
    String hightRelevance = "* 1.2";
    String normalRelevance = "* 0.9";
    String actorRelevance = "* 0.009";
    String lowRelevance = "* 0.8";

    // build query helpers with given keys
    for (Map.Entry<EQueryKey, String>  c : criteria) {
      if (c.getValue() != null) {
        String token = c.getValue().trim()
            // sanitize curly quote
            .replace("’", "'")
            // protect single quotes
            .replace("'", "\\'")
            // remove parenthesis
            .replace("(", "").replace(")", "")
            // remove dashes, +, * and others
            .replace("-", "").replace("*", "").replace("+", "")
            .replace(">", "").replace("<", "").replace("~", "").trim() + "*\"";

        // replace spaces by any char if we are not performing a strict search
        if (!(strict || !token.contains("\""))) {
          token = token.replace(" ", "\"* \"");
        }
        // now remove double quotes, if any
        token = token.replace("\"", "");
        switch (c.getKey()) {
          // group-related
          case GROUP:
            groups.add(c.getValue());
            break;
          case AMONG_GROUP:
            amongGroup = true;
            groups.add(c.getValue());
            break;
          case NOT_IN_GROUP:
            if(c.getValue().equals(String.valueOf(Group.getPublicGroup().getIdGroup()))){
              notInGroups.addAll(public_groups);
            }else{
              notInGroups.add(c.getValue());
            }
            break;
          case VALIDATED:
            validatedFilter = " and validated = " + ("true".equals(c.getValue()) ? 1 : 0) + " ";
            break;
          case FETCHED:
            fetchedFilter = " and fetched = " + ("true".equals(c.getValue()) ? 1 : 0) + " ";
            break;
          // contributor
          case CONTRIBUTOR:
            contributor = c.getValue();
            break;
          // contribution id
          case ID_CONTRIBUTION:
            contributionIds.add(c.getValue());
            break;
          // contribution id to ignore
          case ID_IGNORE:
            ignoreIds.add(c.getValue());
            break;
          // context contribution id to ignore
          case CONTEXT_TO_IGNORE:
            ignoreContextIds.add(c.getValue());
            break;
          // text id where to look
          case TEXT_TO_LOOK:
            lookTextIds.add(c.getValue());
            break;
          // textual contributions
          case CONTRIBUTION_TYPE:
            types.add(c.getValue());
            break;
          case ACTOR:
          case AUTHOR:
          case REPORTER:
          case SOURCE_AUTHOR:
            involvedActors.get(c.getKey()).add(token);
            break;
          // technical
          case FROMID:
            lowest = c.getValue();
            break;
          case ORDERBY:
            if ("id".equals(c.getValue())) {
              orderBy = "contribution.id_contribution";
            }
            break;

          // actors
          case ACTOR_NAME:
            actorNames.add(token);
            break;
          case ACTOR_TYPE:
            actorTypes.add(c.getValue());
            break;
          case FUNCTION:
            functions.add(token);
            break;

          // texts
          case TEXT_TITLE:
            textTitles.add(token);
            break;
          case TEXT_SOURCE:
            textSources.add(token);
            break;

          // debates
          case DEBATE_TITLE:
            debatesTitles.add(token);
            break;

          // arguments
          case ARGUMENT_TITLE:
            argumentTitles.add(token);
            break;

          // citations
          case CITATION_TITLE:
            allResults = token.equals("*");
            originalCitations.add(token);
            break;

          // tags
          case TAG_NAME:
            tags.add(token);
            break;
          case STRICT:
            break;
          default:
            logger.warn("unsupported key given " + c.getKey() + " with value " + c.getValue());
        }
      }
    }

    // add group filter, if none or public group passed, replace with ids of all public groups
    if (groups.isEmpty() || groups.contains(String.valueOf(Group.getPublicGroup().getIdGroup()))) {
      groups.addAll(public_groups);
    }

    String gf = validatedFilter + " and contribution.id_contribution >= " + lowest + " and contribution.deleted = 0 and contribution.hidden = 0";
    if(!notInGroups.isEmpty()) gf += " and contribution.id_contribution  not in (select id_contribution from contribution_in_group where id_group in (" + String.join(",", notInGroups) + "))";

    // set group, validated and lowest contribution id filters
    simpleGroupFilter = "id_group in (" + String.join(",", groups) + ")";
    groupFilter = " and "+ simpleGroupFilter + gf;

    // set groups public filters
    simpleGroupPublicFilter = "id_group in (" + String.join(",", public_groups) + ")";
    groupPublicFilter = " and (" +  simpleGroupPublicFilter + " or " + simpleGroupFilter + ")" + gf;

    // set public webdeb group for tags
    groupMainFilter = " and id_group = " + Group.getPublicGroup().getIdGroup() + gf;

    // specific type given, add clause to contribution types
    String cClause = "";
    if (!types.isEmpty() && types.size() != 3) {
      cClause = " and contribution_type in (" + String.join(", ", types) + ")";
    }

    // specific contribution ids
    if (!contributionIds.isEmpty()) {
      selects.add(select + maxRelevance + relevance + charLength + searched + from + where +
              " id_contribution in (" + String.join(", ", contributionIds) + ")");
    }

    String clause;
    String query;

    // ACTORS
    if (!actorNames.isEmpty()) {
      clause = nameMatch + String.join(" ", actorNames) + mode;
      selects.add(select + clause + actorRelevance + relevance + charLength + searched + from +
              "left join actor on contribution.id_contribution = actor.id_contribution right join actor_i18names " +
              "on actor.id_contribution = actor_i18names.id_contribution" + groupJoin + where +
              clause + groupPublicFilter);
      // id_type
      if (!actorTypes.isEmpty()) {
        String subclause = " and id_type in (" + String.join(",", actorTypes) + ")";
        selects.set(selects.size() - 1, selects.get(selects.size() - 1) + subclause);
      }

      // actors with given name as affiliation
      selects.add("select actor_has_affiliation.id_actor, contribution_type, " + clause + actorRelevance + relevance
              + charLength + searched + from +
              "right join actor on contribution.id_contribution = actor.id_contribution " +
              "right join actor_i18names on actor.id_contribution = actor_i18names.id_contribution " +
              "right join actor_has_affiliation on actor.id_contribution = actor_has_affiliation.id_actor_as_affiliation " +
              "right join actor a2 on a2.id_contribution =  actor_has_affiliation.id_actor " +
              groupJoin + where + clause + groupPublicFilter);

      if (!actorTypes.isEmpty()) {
        String subclause = " and a2.id_type in (" + String.join(",", actorTypes) + ")";
        selects.set(selects.size() - 1, selects.get(selects.size() - 1) + subclause);
      }
    }

    if (!functions.isEmpty()) {
      // actors with given function
      clause = "match(spelling) against ('" + String.join(" ", functions) + mode;
      selects.add(select + clause + actorRelevance + relevance + ", char_length(spelling) " + searched + from +
              "left join actor on contribution.id_contribution = actor.id_contribution " +
              "left join actor_has_affiliation on actor.id_contribution = actor_has_affiliation.id_actor " +
              "left join profession on actor_has_affiliation.function = profession.id_profession " +
              "left join profession_i18names on profession.id_profession = profession_i18names.profession "
              + groupJoin + "where " + clause + groupPublicFilter);

      // contributions where an affiliation has this function
      selects.add(select + clause + actorRelevance + relevance + ", char_length(spelling) " + searched + from +
              "left join contribution_has_actor on contribution.id_contribution = contribution_has_actor.id_contribution " +
              "right join actor_has_affiliation on contribution_has_actor.actor_id_aha = actor_has_affiliation.id_aha " +
              "right join profession on actor_has_affiliation.function = profession.id_profession " +
              "left join profession_i18names on profession.id_profession = profession_i18names.profession " +
              groupJoin + "where " + clause + (amongGroup ? groupPublicFilter : groupFilter));
    }

    // TEXT
    if (!textTitles.isEmpty()) {
      clause = "(match(text_i18names.spelling) against ('" + String.join(" ", textTitles) + mode + ")";
      addSearched = true;

      if (types.contains(String.valueOf(EContributionType.TEXT.id()))) {
        // search texts with title or original title with given values
        selects.add(select + clause + normalRelevance + relevance + ", char_length(text_i18names.spelling) " + searched + from +
                " right join text on contribution.id_contribution = text.id_contribution" +
                " right join text_i18names on text.id_contribution = text_i18names.id_contribution" + groupJoin +
                "where " + clause + fetchedFilter + (amongGroup ? groupPublicFilter : groupFilter));

      }
      if (types.contains(String.valueOf(EContributionType.CITATION.id()))) {
        // search all citations from text having these titles
        selects.add(select + clause + lowRelevance + relevance + ", char_length(text_i18names.spelling) " + searched + from +
                " right join citation on citation.id_contribution = contribution.id_contribution" +
                " left join text on citation.id_text = text.id_contribution" +
                " right join text_i18names on citation.id_text = text_i18names.id_contribution" + groupJoin +
                "where " + clause + (amongGroup ? groupPublicFilter : groupFilter));
      }
    }

    if (!textSources.isEmpty()) {
      clause = "match(text_source_name.name) against ('" + String.join(" ", textSources) + mode;
      selects.add(select + clause + normalRelevance + relevance + ", char_length(text_source_name.name) " + searched + from +
              "left join text on contribution.id_contribution = text.id_contribution left join " +
              "text_source_name on text.id_source_name = text_source_name.id_source" + groupJoin + where +
              clause + (amongGroup ? groupPublicFilter : groupFilter));
      addSearched = true;
    }

    // DEBATES
    if (!debatesTitles.isEmpty()) {
      clause = "match(argument_dictionary.title) against ('" + String.join(" ", debatesTitles) + mode;
      selects.add(select + clause + hightRelevance + relevance + ", char_length(argument_dictionary.title) " + searched + from +
              "left join debate on contribution.id_contribution = debate.id_contribution" + groupJoin +
              " left join debate_simple on debate.id_contribution = debate_simple.id_contribution " +
              "left join argument_shaded on debate_simple.id_argument_shaded = argument_shaded.id_contribution " +
              "left join argument argument on argument_shaded.id_contribution = argument.id_contribution " +
              "left join argument_dictionary on argument.id_dictionary = argument_dictionary.id_contribution " +
              "where " + clause + (amongGroup ? groupPublicFilter : groupFilter));

      clause = "match(argument_dictionary.title) against ('" + String.join(" ", debatesTitles) + mode;
      selects.add(select + clause + hightRelevance + relevance + ", char_length(argument_dictionary.title) " + searched + from +
              "left join debate on contribution.id_contribution = debate.id_contribution" + groupJoin +
              "left join debate_multiple on debate.id_contribution = debate_multiple.id_contribution " +
              "left join argument argument on debate_multiple.id_argument = argument.id_contribution " +
              "left join argument_dictionary on argument.id_dictionary = argument_dictionary.id_contribution " +
              "where " + clause + (amongGroup ? groupPublicFilter : groupFilter));
      //+ " group by argument.id_argument_dictionary");
      addSearched = true;
    }

    // ARGUMENTS
    if (!argumentTitles.isEmpty()) {
      clause = "match(argument_dictionary.title) against ('" + String.join(" ", argumentTitles) + mode;
      selects.add(select + clause + relevance + ", char_length(argument_dictionary.title) " + searched + from +
              "left join argument on contribution.id_contribution = argument.id_contribution" + groupJoin +
              "left join argument_dictionary on argument.id_dictionary = argument_dictionary.id_contribution " +
              "where " + clause + (amongGroup ? groupPublicFilter : groupFilter) +
              " group by argument.id_dictionary");
      addSearched = true;
    }

    // CITATIONS
    if (!originalCitations.isEmpty()) {
      clause = allResults ? "" : "match(citation.original_excerpt) against ('" + String.join(" ", originalCitations) + mode;
      String ch = (clause.equals("") ? select.substring(0, select.length()-2) : select + clause + normalRelevance + relevance + ", char_length(citation.original_excerpt) " + searched) + from +
              "left join citation on contribution.id_contribution = citation.id_contribution" + groupJoin +
              "where " + (allResults ? "contribution.id_contribution > 0" : clause) + (amongGroup ? groupPublicFilter : groupFilter);

      if(!lookTextIds.isEmpty()){
        ch += " and citation.id_text in (" + String.join(", ", lookTextIds) + ")";
      }
      selects.add(ch);
      addSearched = true;
    }

    // TAGS
    if (!tags.isEmpty()) {
      clause = "(match(tag_i18names.name) against ('" + String.join(" ", tags) + mode + ")";
      String simpleTagClause = " and tag.id_type = " + ETagType.SIMPLE_TAG.id();
      addSearched = true;

      // search tags with name that match given values
      selects.add(select + clause + hightRelevance + relevance + ", char_length(tag_i18names.name) " + searched + from +
              " left join tag on contribution.id_contribution = tag.id_contribution " +
              " right join tag_i18names on tag.id_contribution = tag_i18names.id_contribution" + groupJoin +
              where + clause + simpleTagClause + groupMainFilter);

      clause = "(match(tag_rewording_i18names.name) against ('" + String.join(" ", tags) + mode + ")*5";
      // search tags with rewording name that match given values
      selects.add(select + clause + normalRelevance + relevance + ", char_length(tag_rewording_i18names.name) " + searched + from +
              " left join tag on contribution.id_contribution = tag.id_contribution" +
              " right join tag_rewording_i18names on tag.id_contribution = tag_rewording_i18names.id_contribution" + groupJoin +
              where + clause + simpleTagClause + fetchedFilter + groupFilter);
    }

    // involved actors
    String involvedQuery = " " + normalRelevance + " " + relevance + charLength + searched + from +
            "left join contribution_has_actor on contribution.id_contribution = contribution_has_actor.id_contribution " +
            "right join actor on contribution_has_actor.id_actor = actor.id_contribution right join actor_i18names " +
            "on actor.id_contribution = actor_i18names.id_contribution" + groupJoin + where;

    if (!involvedActors.get(EQueryKey.ACTOR).isEmpty()) {
      // contributions where this actor appears
      clause = nameMatch + String.join(" ", involvedActors.get(EQueryKey.ACTOR)) + mode;
      selects.add(select + clause + involvedQuery + clause + cClause + (amongGroup ? groupPublicFilter : groupFilter));
    }

    if (!involvedActors.get(EQueryKey.AUTHOR).isEmpty()) {
      // contributions where this actor appears as authors
      clause = nameMatch + String.join(" ", involvedActors.get(EQueryKey.AUTHOR)) + mode;
      selects.add(select + clause + involvedQuery + clause + "and is_author = 1" + cClause + (amongGroup ? groupPublicFilter : groupFilter));
    }

    if (!involvedActors.get(EQueryKey.REPORTER).isEmpty()) {
      // contributions where this actor appears as reporters
      clause = nameMatch + String.join(" ", involvedActors.get(EQueryKey.REPORTER)) + mode;
      selects.add(select + clause + involvedQuery + clause + "and is_speaker = 1" + cClause + (amongGroup ? groupPublicFilter : groupFilter));
    }

    if (!involvedActors.get(EQueryKey.SOURCE_AUTHOR).isEmpty()) {
      // contributions where this actor appears as source authors
      clause = nameMatch + String.join(" ", involvedActors.get(EQueryKey.SOURCE_AUTHOR)) + mode;
      selects.add(select + clause + involvedQuery + clause + "and is_source_author = 1" + cClause + (amongGroup ? groupPublicFilter : groupFilter));
    }

    // check if some specific criteria have been passed, otherwise, maybe only contribution types have been requested
    // so no specific selects have been built
    //String query;
    if (selects.isEmpty()) {
      StringBuilder join = new StringBuilder();
      join.append(contributor != null ?
              "inner join contribution_has_contributor on contribution.id_contribution = " +
                      "contribution_has_contributor.id_contribution" + groupJoin + where + "id_contributor = " + contributor
              : groupJoin);

      // use group filter var, but we need to remove the leading " and "
      where += groupFilter.substring(4);
      for (String t : types) {
        switch (t) {
          case "0":
            selects.add(select_contribution_sortkey + from + join + where + " and contribution_type = 0 and contribution.deleted = 0");
            break;

          case "1":
            selects.add(select_contribution_sortkey + from + join + where + " and contribution_type = 1 and contribution.deleted = 0");
            break;

          case "2":
            selects.add(select_contribution_sortkey + from + join + where + " and contribution_type = 2 and contribution.deleted = 0");
            break;

          case "3":
            selects.add(select_contribution_sortkey + from + join + "left join argument_context ac on ac.id_contribution = contribution.id_contribution" +
                    " left join argument on argument.id_contribution = ac.id_argument " + where + " and contribution_type = 3 and contribution.deleted = 0 group by argument.id_argument_dictionary");
            break;

          case "4":
            selects.add(select_contribution_sortkey + from + join + where + " and contribution_type = 4 and contribution.deleted = 0");
            break;

          case "5":
            if (!"".equals(fetchedFilter)) {
              join.append(" left join text on text.id_contribution = contribution.id_contribution ");
            }
            selects.add(select_contribution_sortkey + from + join + where + fetchedFilter
                    + " and contribution_type = 5 and contribution.deleted = 0");
            break;

          case "6":
            join.append(" left join tag on tag.id_contribution = contribution.id_contribution ");

            selects.add(select_contribution_sortkey + from + join + where + fetchedFilter
                    + " and contribution_type = 6 and contribution.deleted = 0 and tag.id_type = " + ETagType.SIMPLE_TAG.id());
            break;

          default:
            // ignore
        }
      }
      // now finalize query and execute it
      query = String.join(" union ", selects) + " order by " + orderBy + " asc";
    } else {
      // force mysql to treat relevance as a numeric value by multiplying it by 1 (don't ask me why I must force casting)
      query = String.join(" union ", selects) + " order by " + (allResults ? "" : "relevance desc,") + "ctype asc";
      if (addSearched && !allResults) {
        query += ", searched asc";
      }
    }
    query += getSearchLimit(fromIndex, toIndex, 100);

    // execute query
   //logger.debug("will now execute query " + query);
    RawSql rawSql = RawSqlBuilder.parse(query).create();
    contributions = Ebean.find(Contribution.class).setRawSql(rawSql).findSet();

    if(contributions != null){
      ignoreIds.forEach(id -> contributions.removeIf(e -> e.getIdContribution() == Long.parseLong(id)));
      return new ArrayList<>(contributions);
    }
    return new ArrayList<>();
  }

  /**
   * Get latest entries (based on version time) for actors, texts and/or arguments
   *
   * @param type a contribution type (may be -1 for actors, texts and arguments)
   * @param contributor a given contributor id (-1 to ignore)
   * @param amount the amount of contributions to retrieve (only strictly positive value are considered)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned, -1 to ignore)
   * @return a (possibly empty) list of contribution according to the given parameters
   */
  public static List<Contribution> findLatestEntries(int type, Long contributor, int amount, int group) {
    return getEntries(type == -1 ? Arrays.asList("0", "1", "2", "6") : Collections.singletonList(String.valueOf(type)),
        contributor, 0, amount, ORDER_BY_LATEST, group);
  }

  /**
   * Get latest entries (based on version time) for actors, texts and/or arguments
   *
   * @param type a contribution type (may be -1 for actors, texts and arguments)
   * @param contributor a given contributor id (-1 to ignore)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned, -1 to ignore)
   * @return a (possibly empty) list of contribution according to the given parameters
   */
  public static List<Contribution> findLatestEntries(int type, Long contributor, int fromIndex, int toIndex, int group) {
    return getEntries(type == -1 ? Arrays.asList("0", "1", "2", "3") : Collections.singletonList(String.valueOf(type)),
        contributor, fromIndex, toIndex, ORDER_BY_LATEST, group);
  }

  /**
   * Get latest external entries (based on version time) for texts and arguments
   *
   * @param contributor a given contributor id (-1 to ignore)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param externalSource the source where contributions come from
   * @return a (possibly empty) list of contribution according to the given parameters
   */
  public static List<ExternalContribution> findLatestExternalEntries(Long contributor, int fromIndex, int toIndex, int externalSource) {
    List<Contribution> contributions = getEntries(Arrays.asList("13", "14"), contributor, fromIndex, toIndex, ORDER_BY_LATEST, 0, true);
    if (contributions != null) {
      return contributions.stream()
              .filter(e -> e.getExternalContribution() != null && externalSource == e.getExternalContribution().getExternalSource().getIdSource())
              .map(Contribution::getExternalContribution)
              .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  /**
   * Get the most viewed contributions of given type, contributor
   *
   * @param type a contribution type (may be -1 for actors, texts and arguments)
   * @param contributor a given contributor id (-1 to ignore)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param orderBy the way to order the entries
   * @return a (possibly empty) list of contributions according to given parameters
   */
  public static List<Contribution> findPopularEntries(EContributionType type, Long contributor, int group, int fromIndex, int toIndex, EOrderBy orderBy) {
    String select = "SELECT distinct c.id_contribution FROM contribution c " +
            "left join contribution_has_relevance chr on chr.id_contribution = c.id_contribution " +
            ((type == EContributionType.ACTOR || type == EContributionType.ACTOR_PERSON) ?
            "inner join actor a on a.id_contribution = c.id_contribution " : "") +
            getContributionStatsJoins() +
            " where " + (type == EContributionType.ALL ?
            "contribution_type in ("  + String.join(",", Arrays.asList("0", "1", "2", "6") + ")") :
            "c.contribution_type = " + (type != EContributionType.ACTOR_PERSON ? type.id() : EContributionType.ACTOR.id())) +
            " and c.hidden = 0 and c.deleted = 0 " +
            ((type == EContributionType.ACTOR || type == EContributionType.ACTOR_PERSON) ?
                  " and a.id_type = " + (type == EContributionType.ACTOR ?
                  EActorType.ORGANIZATION.id() : EActorType.PERSON.id()) : "") +
            " and cig.id_group in (" + String.join(",", Group.getVisibleGroupsFor(group != -1  ? group : Group.getPublicGroup().getIdGroup())) + ")" +
            " order by " + orderBy.toSQL() +
            getSearchLimit(fromIndex, toIndex);
    return Ebean.find(Contribution.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get a list of contribution according to given parameters
   *
   * @param type a list of contribution types to retrieve
   * @param contributor a given contributor id (-1 to ignore)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param orderBy the property name to order the list on (either hit or latest for version)
   * @param group the group id where to look for contributions. If default public group is passed, any public
   *    contribution is returned. If -1 is passed, does not filter on group.
   * @return a (possibly empty) list of contributions according to given parameters
   */
  public static List<Contribution> getEntries(List<String> type, Long contributor, int fromIndex, int toIndex, String orderBy, int group) {
    return getEntries(type, contributor, fromIndex, toIndex, orderBy, group, false);
  }
  /**
   * Get a list of contribution according to given parameters
   *
   * @param type a list of contribution types to retrieve
   * @param contributor a given contributor id (-1 to ignore)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param orderBy the property name to order the list on (either hit or latest for version)
   * @param hidden check if the contribution is hide or not
   * @param group the group id where to look for contributions. If default public group is passed, any public
   *    contribution is returned. If -1 is passed, does not filter on group.
   * @return a (possibly empty) list of contributions according to given parameters
   */
  private static List<Contribution> getEntries(List<String> type, Long contributor, int fromIndex, int toIndex, String orderBy, int group, boolean hidden) {
    String select = "select contribution.id_contribution " +
        (ORDER_BY_LATEST.equals(orderBy) || contributor != -1L ?
            ", max(contribution_has_contributor.version) as latest from contribution left join contribution_has_contributor " +
                "on contribution_has_contributor.id_contribution = contribution.id_contribution "
            : "from contribution ") +
        (group != -1 ?
            "left join contribution_in_group on contribution_in_group.id_contribution = contribution.id_contribution "
            : " ") +
        "left join tag t on contribution.id_contribution = t.id_contribution " +
        "where contribution_type in ("  + String.join(",", type) +  ") " +
        (contributor != -1L ?
            "and id_contributor = " + contributor + " ": " ") +
        (group != -1
            ? "and contribution_in_group.id_group in (" + (Group.getPublicGroup().getIdGroup() == group ?  String.join(",",Group.getVisibleGroupsFor(group)) : group) + ") "
            : " ") +
        "and contribution.deleted = 0 and contribution.hidden = " + (hidden ? "1" : "0") +
        " and (contribution.contribution_type != " + EContributionType.TAG.id() + " or t.id_type = " + ETagType.SIMPLE_TAG.id() + ")" +
        " group by id_contribution order by " + orderBy + " desc" + getSearchLimit(fromIndex, toIndex);

    logger.debug("will execute query: " + select);
    List<Contribution> result = Ebean.find(Contribution.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get the amount of contribution by type (0 for actors, 1 for texts, 2 for arguments, 3 for links, -1 for all)
   *
   * @param type a contribution type id (-1 to ignore)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned) (-1 to ignore)
   * @return the amount of contributions in database, depending on given type
   */
  public static long getAmountOf(int type, int group) {
    return getAmountOf(null, type, group);
  }

  /**
   * Get the amount of contribution by type (0 for actors, 1 for texts, 2 for arguments, 3 for links, -1 for all)
   * in given group for given contributor
   *
   * @param contributor a contributor id (null to ignore)
   * @param type a contribution type id (-1 to ignore)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned) (-1 to ignore)
   * @return the amount of contributions in database, depending on given type, contributor and group
   */
  public static long getAmountOf(Long contributor, int type, int group) {
    List<String> groups = Group.getVisibleGroupsFor(group);
    EContributionType eType = EContributionType.value(type);

    if (type == -1 || eType == null) {
      return find.findRowCount();
    } else {
      if(eType.isAlwaysPublic()) {
        group = -1;
      }

      // should use proper ebean dialect API..., but join sucks...
      String select = "select count(contribution.id_contribution) as 'count' from contribution " +
          (group != -1 ? "left join contribution_in_group on contribution.id_contribution = contribution_in_group.id_contribution " : "") +
          (contributor != null ? "left join contribution_has_contributor on " +
              "contribution.id_contribution = contribution_has_contributor.id_contribution " : "") +
          (type == EContributionType.TAG.id() ? "left join tag t on contribution.id_contribution = t.id_contribution " : "") +
          "where contribution_type = " + type + " and contribution.deleted = 0 and contribution.hidden = 0" +
          (group != -1 ? " and id_group in (" + String.join(",", groups) + ") " : "") +
          (contributor != null ? " and id_contributor = " + contributor : "") +
          (type == EContributionType.TAG.id() ? " and t.id_type = " + ETagType.SIMPLE_TAG.id() : "");
      return Ebean.createSqlQuery(select).findUnique().getLong("count");
    }
  }

  /**
   * Get the creator of a contribution
   *
   * @param id a contribution id
   * @return the contributor that created given contribution if the contribution exists, null otherwise
   */
  public static Contributor getCreator(Long id) {
    ContributionHasContributor chc = getCreatorHistory(id);
    return chc != null ? chc.getContributor() : null;
  }

  /**
   * Get the creator of a contribution
   *
   * @param id a contribution id
   * @return the contributor that created given contribution if the contribution exists, null otherwise
   */
  public static ContributionHasContributor getCreatorHistory(Long id) {
    Contribution contribution = findById(id);
    if (contribution != null) {
      for (ContributionHasContributor chc : contribution.getContributionHasContributors()) {
        if (chc.getStatus().getIdStatus() == EModificationStatus.CREATE.id()) {
          return chc;
        }
      }
    }
    return null;
  }

  /**
   * Get the Contributor whoever made a change last
   *
   * @param id a Contribution id
   * @return the last contributor of this Contribution
   */
  public static ContributionHasContributor getLastestContributor(Long id) {
    Contribution contribution = findById(id);

    if (contribution != null) {
      List<ContributionHasContributor> contributors = contribution.getContributionHasContributors();

      if(contributors != null && contributors.size() > 0) {
        contributors.sort(Comparator.comparing(ContributionHasContributor::getVersion));
        return contributors.get(0);
      }

    }
    return null;
  }

  /**
   * Get the creator of a contribution
   *
   * @param id a contribution id
   * @return the contributor that created given contribution if the contribution exists, null otherwise
   */
  public static Contributor getLastContributorInGroup(Long id, int group) {
    Contribution contribution = findById(id);
    Group g = Group.findById(group);
    if (contribution != null && g != null) {
      List<ContributionHasContributor> contributors = contribution.getContributionHasContributors();
      contributors.sort(Comparator.comparing(ContributionHasContributor::getVersion));

      for (ContributionHasContributor chc : contribution.getContributionHasContributors()) {
        if (chc.getContributor().getContributorHasGroups().stream().anyMatch(e -> e.getGroup().getIdGroup() == group)) {
          return chc.getContributor();
        }
      }
    }
    return getCreator(id);
  }

  /**
   * Execute the given sql query to perform it into the DB et get the results as a list of list of values. The first
   * list is the keys of sql columns name.
   *
   * @param query the sql query to execute
   * @return the result list, possibly empty
   */
  public static List<List<String>> executeApiQuery(String query){
    logger.debug("Execute API query " + query);
    List<List<String>> response = new ArrayList<>();

    if(query != null){
      try {
        List<SqlRow> results = Ebean.createSqlQuery(query).findList();
        if (!results.isEmpty()) {
          // Collect keys
          response.add(new ArrayList<>(results.get(0).keySet()));
          // Collect values
          results.forEach(e ->
                  response.add(e.values().stream().map(e2 -> e2 != null ? e2.toString() : "").collect(Collectors.toList())));
        }
      } catch (Exception e){
        logger.debug(e+"");
      }
    }

    return response;
  }

  /**
   * Get all classes of the model description, with description of theirs attributes and relations with others classes.
   *
   * @return the list of classes of the model
   */
  public static List<ModelDescription> getModelDescriptions(){
    List<ModelDescription> descriptions = new ArrayList<>();

    Reflections reflections = new Reflections("be.webdeb.infra.persistence.model");

    Set<Class<? extends Model>> allClasses = reflections.getSubTypesOf(Model.class);
    for(Class<?> model : allClasses){
      if (model.getAnnotation(Unqueryable.class) == null) {
        descriptions.add(getModelAttributesDescription(model));
      }
    }

    return descriptions;
  }

  /**
   * Get all contribution id for a given contribution type
   *
   * @param type the contribution type to focus
   * @return a possibly empty list of contribution id for the given type
   */
  public static List<Long> getAllIdByContributionType(EContributionType type){
    List<Long> result = new ArrayList<>();
    if(type != null) {
      String select = "SELECT distinct c.id_contribution as 'id' FROM webdeb.contribution c " +
              "inner join contribution_in_group cig on cig.id_contribution = c.id_contribution " +
              "inner join contributor_group g on g.id_group = cig.id_group " +
              "where contribution_type = " + type.id() + " and hidden = 0 and deleted = 0 and g.contribution_visibility = 0";

      Ebean.createSqlQuery(select).findEach(e ->
              result.add(e.getLong("id")));
    }
    return result;
  }

  public static List<Citation> getAllCitations(Long id) {
    String select = "SELECT ci.id_contribution FROM citation ci " +
            "left join contribution c on c.id_contribution = ci.id_contribution " +
            "left join citation_justification_link jl on jl.id_citation = ci.id_contribution " +
            "left join citation_position_link pl on pl.id_citation = ci.id_contribution " +
            "left join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
            "where ci.id_text = " + id + " or jl.id_context = " + id +
            " or pl.id_debate = " + id + " or cht.id_tag = " + id +
            getOrderByContributionDate();

    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  public static List<Citation> getAllCitations(SearchContainer query) {
    String select = addFiltersToSql("SELECT distinct ci.id_contribution FROM citation ci " +
            "inner join contribution c on c.id_contribution = ci.id_contribution " +
            "left join citation_justification_link jl on jl.id_citation = ci.id_contribution " +
            "left join citation_position_link pl on pl.id_citation = ci.id_contribution " +
            "inner join contribution_has_tag cht on cht.id_contribution = c.id_contribution " +
            getContributionStatsJoins() +
            " where (ci.id_text = " + query.getId() + " or jl.id_context = " + query.getId() +
            " or pl.id_debate = " + query.getId() + " or cht.id_tag = " + query.getId() + ")" +
            getContributionStatsWhereClause(query) +
            getOrderByContributionDate() +
            getSearchLimit(query),
            query.getFilters());
    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  public static List<Contribution> findContributionsToUpdateRelevance() {
    String select = "SELECT c.id_contribution from contribution c " +
            "left join contribution_has_relevance chr on chr.id_contribution = c.id_contribution " +
            "where c.contribution_type in (" + EContributionType.getMajorContributionTypes().stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
            "and c.deleted = 0 and c.hidden = 0 and (chr.relevance is null or chr.relevance < 0 or c.version > subdate(current_date, 1)) " +
            "order by c.id_contribution desc limit 1000";

    return Ebean.find(Contribution.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get the list of texts where come the citations in the given context or super context
   *
   * @param context a context contribution id
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return a possibly empty list of texts
   */
  public static List<Text> findLinkTexts(Long context, Long contributorId, int groupId){
    String select = "SELECT distinct t.id_contribution FROM text t " +
            "left join citation ci on ci.id_text = t.id_contribution " +
            "left join contribution c on c.id_contribution = ci.id_contribution " +
            "left join citation_justification_link l1 on l1.id_citation = ci.id_contribution " +
            "left join citation_position_link l2 on l2.id_citation = ci.id_contribution " +
            getContributionStatsJoins() +
            getContributionStatsJoins("l1.id_contribution", "1") +
            getContributionStatsJoins("l2.id_contribution", "2") +
            " where (l1.id_context = " + context + " or l2.id_debate = " + context + ")" +
            getContributionStatsWhereClause(contributorId, groupId) +
            getContributionStatsWhereClause(contributorId, groupId, "1") +
            getContributionStatsWhereClause(contributorId, groupId, "2");
    return Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  public boolean canOnlyBeEditedInGroup() {
    String select = "SELECT distinct g.id_group FROM contributor_group g " +
            "inner join contribution_in_group cig on cig.id_group = g.id_group " +
            "left join group_has_permission ghp on ghp.id_group = g.id_group " +
            " where cig.id_contribution = " + idContribution +
            " and (ghp.id_permission is null or ghp.id_permission != " + EPermission.EDIT_CONTRIBUTION_INGROUP.id() + ")";
    return !Ebean.find(Group.class).setRawSql(RawSqlBuilder.parse(select).create()).findList().isEmpty();
  }

  /**
   * Get all attributes of a class of the model.
   *
   * @return the list of attributes of a class of the model
   */
  private static ModelDescription getModelAttributesDescription(Class<?> model){
    ModelDescription description = new ConcreteModelDescription(model.getSimpleName());

    List<Field> fields = new ArrayList<>();
    fields.addAll(Arrays.asList(model.getDeclaredFields()));

    if(model.getSuperclass().equals(TechnicalTable.class)){
      fields.addAll(Arrays.asList(TechnicalTable.class.getDeclaredFields()));
    }

    fields.forEach(field -> {
      if (field.getAnnotation(Unqueryable.class) == null) {
        EmbeddedId embedded = field.getAnnotation(EmbeddedId.class);
        if(embedded != null){
          Arrays.asList(field.getType().getDeclaredFields()).forEach(embeddedField ->
                  addFieldToModelDescription(embeddedField, description, true));
        }
        else {
          boolean isId = field.getAnnotation(Id.class) != null;

          addFieldToModelDescription(field, description, isId);

          List<Annotation> annotations = new ArrayList<>();
          annotations.add(field.getAnnotation(OneToOne.class));
          annotations.add(field.getAnnotation(ManyToOne.class));
          annotations.add(field.getAnnotation(OneToMany.class));
          annotations.add(field.getAnnotation(ManyToMany.class));
          Annotation annotation = getAnnotationInList(annotations);

          if (annotation != null) {
            Class<?> classToQuery;
            if (Collection.class.isAssignableFrom(field.getType())) {
              classToQuery = ((Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
            } else {
              classToQuery = field.getType();
            }

            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            String columnName = null;

            if (joinColumn != null) {
              columnName = joinColumn.name();
            } else {
              String mappedBy = null;
              if (annotation instanceof OneToOne) {
                mappedBy = ((OneToOne) annotation).mappedBy();
              } else if (annotation instanceof OneToMany) {
                mappedBy = ((OneToMany) annotation).mappedBy();
              }
              try {
                if (mappedBy != null) {
                  Field f = classToQuery.getDeclaredField(mappedBy);
                  joinColumn = f.getAnnotation(JoinColumn.class);
                  if (joinColumn != null) {
                    columnName = joinColumn.name();
                  }
                }
              } catch (NoSuchFieldException e) {
                logger.debug("The relation field is not in the related class. Must not append if model is well define.");
              }
            }

            if (columnName != null) {
              description.getAttributesMap().add(
                      new ConcreteModelAttributeDescription(classToQuery.getSimpleName(), columnName, isId, EDBRelationType.annotationToType(annotation)));
            }
          }
        }
      }
    });

    return description;
  }

  private static void addFieldToModelDescription(Field field, ModelDescription description, boolean isId){
    Column column = field.getAnnotation(Column.class);
    if (column != null) {
      description.getAttributesMap().add(
              new ConcreteModelAttributeDescription(field.getName(), column.name(), isId, EDBRelationType.SIMPLE_FIELD));
    }
  }

  /**
   * Get the annotation in given list that said the nature of the relation between classes.
   *
   * @return an annotation
   */
  private static Annotation getAnnotationInList(List<Annotation> annotations){
    for(Annotation a : annotations){
      if(a != null)
        return a;
    }
    return null;
  }

  /**
   * Get the number of tags linked to this contribution
   *
   * @return the number of tags linked to this contribution
   */
  private int getNbTags(){
    return Ebean.createSqlQuery("SELECT count(*) as 'count' FROM contribution_has_tag where id_contribution = " + idContribution).findUnique().getInteger("count");
  }
}
