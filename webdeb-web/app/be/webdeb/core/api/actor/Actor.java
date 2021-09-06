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
 *
 */

package be.webdeb.core.api.actor;


import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.impl.helper.*;

import java.util.List;
import java.util.Map;

/**
 * This Interface describes an Actor in the webdeb system with all his properties. An Actor is either an
 * individual or an organization that may be involved in text/arguments as authors or speakers.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

public interface Actor extends Contribution, Comparable<Actor> {

  /**
   * Get this Actor type
   *
   * @return the int value representing this Actor type
   */
  EActorType getActorType();

  /**
   * Get the list of names for this actor
   *
   * @return the names of this Actor
   */
  List<ActorName> getNames();

  /**
   * Set the list of names for this Actor (will simply overwrite existing names).
   *
   * @param name a name for this Actor
   */
  void setNames(List<ActorName> name);

  /**
   * Add given name to this actor (replace existing name if a name in the same language exists)
   *
   * @param name a name to add or update
   */
  void addName(ActorName name);

  /**
   * Get a particular name for this actor for given language. If such a language does not exist,
   *
   * @param lang a 2-char ISO code representing the language, if not found, if not found, a default language is used, or any other value as last resort
   * @return the actor name object for this Actor in given language, or
   */
  ActorName getName(String lang);

  /**
   * Get this actor's full name in given language, as specified in ActorName.getFullName
   *
   * @param lang a 2-char ISO code representing the language, if not found, a default language is used, or any other value as last resort
   * @return the actor's full name according to ActorName.getFullName description
   * @see ActorName
   */
  String getFullname(String lang);

  /**
   * Get the avatar id of this actor, if any
   *
   * @return a contributor picture id
   */
  Long getAvatarId();

  /**
   * Set the avatar id of this actor
   *
   * @param avatarId a contributor picture id
   */
  void setAvatarId(Long avatarId);

  /**
   * Get the avatar extension of this actor, if any
   *
   * @return a contributor picture extension
   */
  String getAvatarExtension();

  /**
   * Set the avatar extension of this actor
   *
   * @param extension a contributor picture extension
   */
  void setAvatarExtension(String extension);

  /**
   * Get the avatar picture picture, if any
   *
   * @return the avatar as Contributor Picture, may be null
   */
  ContributorPicture getAvatar();

  /**
   * Set the avatar picture picture
   *
   * @return avatar the avatar as Contributor Picture
   */
  void setAvatar(ContributorPicture avatar);

  /**
   * Get all contributions in which this Actor is flagged has being involved in
   *
   * @param type the contribution type enum value (in case of ALL, no actor will obviously be retrieved)
   * @return a Map of Contribution and associated ActorRole for these Contributions
   */
  Map<Contribution, ActorRole> getContributions(EContributionType type);

  /**
   * Get this Actor's cross reference on the internet (may be null).
   *
   * @return a url to some description or personal page for this Actor
   */
  String getCrossReference();

  /**
   * Set this Actor's cross reference on the internet.
   * If it is longer than MAX_NAME_SIZE, it will be truncated
   *
   * @param url a url to some webpage where more info over this Actor may be found
   * @throws FormatException if the given url does not look valid
   */
  void setCrossReference(String url) throws FormatException;

  /**
   * Get all affiliations of this Actor (may be empty)
   *
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getAffiliations();

  /**
   * Get all affiliations (but not graduating and filiations) of this Actor (may be empty)
   *
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getSimpleAffiliations();

  /**
   * Get all affiliations of this Actor (may be empty) for a given affiliation type
   *
   * @param type an affiliation type
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getAffiliations(EAffiliationType type);

  /**
   * Get all affiliated actor by affiliation type of this Actor (may be empty)
   *
   * @param type an affiliation type
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getActorsAffiliated(EAffiliationType type);

  /**
   * Get all affiliated actors affiliations map
   *
   * @param type the actor type for the affiliated
   * @return the related actors aff map
   */
  Map<Long, List<Affiliation>> getAffMap(EActorType type);

  /**
   * Get all affiliated actors affiliations map
   *
   * @param type the actor type for the affiliated
   * @param actorId an actor id
   * @return the related actors aff map
   */
  List<Affiliation> getAffMap(EActorType type, Long actorId);

  /**
   * Get all affiliated actor by actor type of this Actor (may be empty)
   *
   * @param type the actor type for the affiliated
   * @return the list of AffiliationActor to which this Actor is affiliation
   */
  List<Affiliation> getActorsAffiliated(EActorType type);

  /**
   * Get all affiliations actor by actor type of this Actor (may be empty)
   *
   * @param type the actor type for the affiliation
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getActorsAffiliations(EActorType type);

  /**
   * Get all affiliations of this Actor (may be empty)
   *
   * @param type the actor type for the affiliation
   * @return the list of AffiliationActor to which this Actor is affiliated
   */
  List<Affiliation> getAllAffiliations(EActorType type);

  /**
   * Set the list of affiliations for this Actor
   *
   * @param affiliations a list of AffiliationActor
   * @throws FormatException if any of given affiliation is incomplete
   */
  void setAffiliations(List<Affiliation> affiliations) throws FormatException;

  /**
   * Add an Affiliation to this Actor.
   * As other fields, additions are persisted when calling save().
   *
   * @param affiliation an affiliation to add
   * @throws FormatException if the given affiliation is incomplete
   */
  void addAffiliation(Affiliation affiliation) throws FormatException;

  /**
   * Remove given affiliation from this Actor. If the affiliation is unfound, this Actor is unchanged.
   * As other fields, removals are persisted when calling save().
   *
   * @param affiliation an affiliation id to remove from this Actor
   * @throws PermissionException if the requested deletion was possible due to an existing reference
   */
  void removeAffiliation(Long affiliation) throws PermissionException;

  /**
   * Get the actor type id
   *
   * @return the actor type id
   */
  int getActorTypeId();

  /**
   * Get the actor's gender if the actor is a person, null otherwise
   *
   * @return the gender as string
   */
  String getGenderAsString();

  /**
   * Get the list of texts where actor is author of at least one citation
   *
   * @param contributor the contributor id
   * @param group the current group id
   * @return a possibly empty list of texts
   */
  List<Text> getTextsWhereCitationAuthor(Long contributor, int group);

  /**
   * Get the list of texts where actor is cited in at least one citation
   *
   * @param contributor the contributor id
   * @param group the current group id
   * @return a possibly empty list of texts
   */
  List<Text> getTextsWhereCitationCited(Long contributor, int group);

  /**
   * Get the minimum or maximum affiliation of this actor for given actor type.
   *
   * @param type the actor type that we need to look
   * @param forMin true for minimum, false for maximum
   * @return the minimum or maximum date
   */
  String getMinOrMaxAffiliationDate(EActorType type, boolean forMin);

  /**
   * Get the list of affiliations where actors own this actor (this actor must be an organization)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getOwners(SearchContainer query);

  /**
   * Get the list of affiliations where actors are owned by this actor (this actor must be an organization)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getOwnedOrganizations(SearchContainer query);

  /**
   * Get the list of affiliations where this actor is part of (this actor must be a person)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getAffiliationOrganizations(SearchContainer query);

  /**
   * Get all citations where this actor is text's and citation's author
   *
   * @param text the text id
   * @return a possibly empty list of citations
   */
  List<Citation> getTextsAuthorCitations(Long text);

  /**
   * Get the list of affiliations where actors own this actor (must be an organization)
   *
   * @param query all needed data to perform the querys
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getMembers(SearchContainer query);

  /**
   * Get all actors where that speak about this actor (authors where this actor is cited)
   *
   * @param query all data needed to get all actors
   * @return a possibly empty list of actors
   */
  List<Actor> getOthersActorsCitations(SearchContainer query);

  /**
   * Get all texts where this actors is author or cited in text's tags
   *
   * @param query all data needed to get all tags
   * @return a possibly empty list of tags
   */
  List<Tag> getTagsCitations(SearchContainer query);

  /**
   * Get all texts where this actors is author or cited in text's citations
   *
   * @param query all data needed to get all texts
   * @return a possibly empty list of texts
   */
  List<Text> getTextsCitations(SearchContainer query);

  /**
   * Get all debates where this actors is cited or where this actors is author or cited in debate's citations
   *
   * @param query all data needed to get all debates
   * @return a possibly empty list of debates
   */
  List<Debate> getDebatesCitations(SearchContainer query);

  /**
   * Get all citations where this actors is author or cited
   *
   * @param query all data needed to get all actor's citation
   * @return a possibly empty list of citations
   */
  List<Citation> getCitations(SearchContainer query);

  /**
   * Get all citations where this actors is author or cited in given contribution
   *
   * @param query all data needed to get all actor's citation
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromContribution(SearchContainer query);
}
