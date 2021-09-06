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

package be.webdeb.infra.persistence.accessor.api;

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.*;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.debate.DebateSimilarity;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.presentation.web.controllers.account.ClaimHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents an accessor for contribution-related management,i.e,retrieving generic
 * contributions from various properties,or retrieve specific contributions(actors,texts and arguments and
 * argument)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ContributionAccessor {

  /**
   * Retrieve a Contribution by its id
   *
   * @param id a Contribution id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the Contribution concrete object corresponding to the given id, null if not found
   */
  Contribution retrieve(Long id, boolean hit);

  /**
   * Retrieve a Contribution by its id and type. Invoker must explicitly cast returned value into
   * concrete type (as given in "type" parameter) to access concrete methods.
   *
   * @param id a Contribution id
   * @param type a contribution type (may pass the ALL type)
   * @return the Contribution concrete object corresponding to the given id, null if not found
   */
  Contribution retrieve(Long id, EContributionType type);

  /**
   * Retrieve a Contribution by its id. Invoker must explicitly cast returned value into concrete type to
   * access concrete methods.
   *
   * @param id a Contribution id
   * @return the Contribution concrete object corresponding to the given id, null if not found
   */
  Contribution retrieveContribution(Long id);

  /**
   * Retrieve a context Contribution by its id. Invoker must explicitly cast returned value into concrete type to
   * access concrete methods.
   *
   * @param id a ContextContribution id
   * @return the ContextContribution concrete object corresponding to the given id, null if not found
   */
  ContextContribution retrieveContextContribution(Long id);

  /**
   * Get all actors directly involved in a given contribution. Only retrieve actors directly connected to
   * given contribution, not all actors connected to all contributions bound to this contribution.
   *
   * @param contribution a contribution id
   * @return a list of Actor roles in given contribution, empty list if contribution not found
   */
  List<ActorRole> getActors(Long contribution);

  /**
   * Get a limited list of actors by role
   *
   * @param contribution a contribution id
   * @param limit the limit of results
   * @param role the actor role for given contribution
   * @return a limited list of Actor roles in given contribution, empty list if contribution not found
   */
  List<ActorRole> getActors(Long contribution, int limit, EActorRole role);

  /**
   * Get the number of actors by role of given contribution
   *
   * @param contribution a contribution id
   * @param role the actor role for given contribution
   * @return the number of actors for given role
   */
  int getNbActors(Long contribution, EActorRole role);

  /**
   * Find a place by its id
   *
   * @param id a place id
   * @return a place
   */
  Place findPlace(Long id);

  /**
   * Find a list of place by given partial place name
   *
   * @param name a place name
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of find places
   */
  List<Place> findPlace(String name, int fromIndex, int toIndex);

  /**
   * Find a list of Contributions by a value. Will search in text titles, argument standard forms and
   * actor names
   *
   * @param value a value to search for
   * @param group a group id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of Contributions that contains given value
   */
  List<Contribution> findByValue(String value, int group, int fromIndex, int toIndex);

  /**
   * Find a list of Contribution by a list of criteria, being key-value pairs.
   *
   * @param criteria a list of key-value pairs to search for
   * @param strict check if we must perform a strict search
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of Contribution that fulfils the given criteria, may be empty
   * @see EQueryKey for the list of valid keys
   */
  List<Contribution> findByCriteria(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex);

  /**
   * Find a list of Contribution ordered by version time (actor, text and argument only)
   *
   * @param type the contribution type we are interested in
   * @param contributor a contributor id (-1 to ignore it)
   * @param amount the amount of entries to retrieve
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned)
   * @return the list of latest touched Contributions of given type for given contributor
   */
  List<Contribution> getLatestEntries(EContributionType type, Long contributor, int amount, int group);

  /**
   * Find a list of Contribution ordered by number of visualization hits (actor, text and argument only)
   *
   * @param type a contribution type (may be -1 for actors, texts and arguments)
   * @param contributor a given contributor id (-1 to ignore)
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param orderBy the way to order the entries
   * @return the list of most hit Contributions of given type for given contributor
   */
  List<Contribution> getPopularEntries(EContributionType type, Long contributor, int group, int fromIndex, int toIndex, EOrderBy orderBy);

  /**
   * Retrieve the creator of a contribution
   *
   * @param contribution a Contribution id
   * @return the creator of the given contribution, or null if contribution does not exist
   */
  ContributionHistory getCreator(Long contribution);

  /**
   * Get the Contributor whoever made a change last
   *
   * @param contribution a Contribution id
   * @return the last contributor of this Contribution
   */
  ContributionHistory getLatestContributor(Long contribution);

  /**
   * Get the last contributor that is in a given group that update a given contribution
   *
   * @param contribution a Contribution id
   * @param group a Group id
   * @return the last Contributor of the given Contribution in the given group
   */
  Contributor getLastContributorInGroup(Long contribution, int group);

  /**
   * Get all contributors that created or updated given contribution
   *
   * @param contribution a Contribution id
   * @return the list of contributors of this Contribution, an empty list if contribution is not found
   */
  List<Contributor> getContributors(Long contribution);

  /**
   * Merge data from two contributions. Any detail present in the origin for which no detail exist in the replacement
   * contribution will be stored in this replacement contribution, any conflicting detail will be ignored.
   * <br><br>
   * Any link to the origin contribution will be replaced by the replacement contribution, ie
   * <ul>
   *   <li>for actors, any bound contribution to the origin will be bound to the replacement actor. Reconciliations
   *   regarding affiliation will be performed on affiliation actors. For all origin affiliations, if no such affiliation
   *   to an actor exists in replacement, it will be copied from origin. If the affiliation has no link to an actor, and
   *   such function does not exists, it will be copied in replacement actor too. Affiliated actors to the origin will
   *   be rebound to replacement actor.</li>
   *
   *   <li>for texts, any argument will be attached to the replacement text. In case of private contents, they will be
   *   copied into the replacement one (if not already existing) and if no shared content exist in the replacement,
   *   the origin content will be copied too.</li>
   *
   *   <li>for arguments, any similarity link to the origin will be attached to the replacement, justification links
   *   will be deleted</li>
   * </ul>
   *
   * @param origin a contribution id to be merged into and replaced by given replacement contribution
   * @param replacement the replacement contribution id
   * @param contributor the contributor id asking for the merge
   * @return true if the origin is deleted, or false if replacement is deleted
   * @throws PersistenceException if any given contribution or contributor does not exist, or if both contributions
   * haven't the same type, or if any other error occurred while saving into the database
   * @throws PermissionException if given contributor is not owner of both groups containing given contributions
   */
  boolean merge(Long origin, Long replacement, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Remove a given contribution from the repository. If given contribution belonged to many groups, it
   * is removed from all groups.
   *
   * Such removal will not be allowed if
   * <ul>
   *   <li>this contribution is a text and arguments have been extracted from it</li>
   *   <li>this contribution is an actor and it has been bound to other contributions</li>
   * </ul>
   *
   * @param contribution a contribution id
   * @param type the type of the contribution (used to crosscheck id and type)
   * @param contributor a contributor id that issued the removal action
   * @throws PersistenceException if given contribution was not found in the database or any of the aforementioned
   * constraint was violated.
   * @throws PermissionException if given contributor is not at least an owner of the only group to which this contribution
   * belongs to, or a platform admin
   */
  void remove(Long contribution, EContributionType type, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Remove a given contribution from the repository. If given contribution belonged to many groups, it
   * is removed from all groups.
   *
   * Such removal will not be allowed if
   * <ul>
   *   <li>this contribution is a text and arguments have been extracted from it</li>
   *   <li>this contribution is an actor and it has been bound to other contributions</li>
   * </ul>
   *
   * @param contribution a contribution id
   * @param type the type of the contribution (used to crosscheck id and type)
   * @param contributor a contributor id that issued the removal action
   * @param option remove option
   * @throws PersistenceException if given contribution was not found in the database or any of the aforementioned
   * constraint was violated.
   * @throws PermissionException if given contributor is not at least an owner of the only group to which this contribution
   * belongs to, or a platform admin
   */
  void remove(Long contribution, EContributionType type, Long contributor, ERemoveOption option) throws PermissionException, PersistenceException;

  /**
   * Bind an author to a contribution, return the updated Actor if it was unknown (after creating it in
   * database). Existence check is performed on id.
   *
   * @param contribution a Contribution object (must exist)
   * @param role the role of a bound Actor (in this role) in given contribution
   * @param currentGroup the current group id, will be used if the actor in given role is not yet known
   * @param contributor contributor  id that issued the binding action
   * @return a possibly empty list of Contributions created automatically with this save action (new contributions)
   *
   * @throws PermissionException if given contributor may not bind given actor to given contribution in given group
   * @throws PersistenceException if an error occurred, a.o., unset parameter or not found element from database
   */
  List<Contribution> bindActor(Long contribution, ActorRole role, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Unbind an author to a contribution, return the updated Actor if it was unknown (after creating it in
   * database). Existence check is performed on id.
   *
   * @param contribution a Contribution id
   * @param actor an Actor representing an author
   * @param contributor a contributor that issued the unbind action
   * @throws PersistenceException if the binding removal did not complete
   */
  // TODO add the role to unbind (author, reporter, cited)
  void unbindActor(Long contribution, Long actor, Long contributor) throws PersistenceException;

  /**
   * Bind a tag linked to a contribution, return the updated Tag if it was unknown (after creating it in
   * database). Existence check is performed on id.
   *
   * @param contribution a Contribution id
   * @param tags a list of tag to bound in given contribution
   * @param contributor contributor  id that issued the binding action
   * @return a possibly empty list of Contributions created automatically with this save action (new contributions)
   *
   * @throws PermissionException if given contributor may not bind given actor to given contribution in given group
   * @throws PersistenceException if an error occurred, a.o., unset parameter or not found element from database
   */
  List<Contribution> bindTags(Long contribution, List<Tag> tags, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Add given group id to the list of groups where given contribution is visible. Has no effect if group is unknown.
   * This action requires not an extra save call to be persisted into the database.
   *
   * @param contribution a contribution id
   * @param group a group id
   * @return true if given group has been added (or was already assigned) to given contribution, false if given group does not exist.
   * @throws PersistenceException if contribution does not exist in repository of does not correspond
   */
  boolean addInGroupAndUpdate(Long contribution, int group) throws PersistenceException;

  /**
   * Get all tags linked with a given contribution
   *
   * @param contribution a Contribution id
   * @return a possibly empty set of tags
   */
  Set<Tag> getContributionsTags(Long contribution);

  /**
   * Get all places linked with a given contribution
   *
   * @param contribution a Contribution id
   * @return a possibly empty set of geographical places
   */
  List<Place> getContributionsPlaces(Long contribution);

  /**
   * Retrieve all contribution types
   *
   * @return a list of contribution types
   */
  List<ContributionType> getContributionTypes();

  /**
   * Get the amount of given type of contribution
   *
   * @param type a type of contribution
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned)
   * @return the amount of contribution of given type
   */
  long getAmountOf(EContributionType type, int group);

  /**
   * Save validated state for given contributions (formerly also save marking, but it was judged later as a non-pedagogical way to learn)
   *
   * @param contributions a list of contributions
   * @throws PersistenceException if any validations could not be saved (all or nothing)
   */
  void saveMarkings(List<Contribution> contributions) throws PersistenceException;

  /**
   * Remove given contribution from given group. If this contribution only belonged to this group, it is simply
   * removed completely from the repository.
   *
   * @param contribution a contribution id
   * @param group a group id
   * @param contributor id the contributor that issued that removal
   * @throws PersistenceException if given contribution or group does not exists or if an other database error occurred
   * @throws PermissionException if given contributor is not owner of given group
   */
  void removeFromGroup(Long contribution, int group, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Check if the given contribution is member of a group with public visibility
   *
   * @param contribution a contribution id
   * @return true if a public group contains the given contribution
   */
  boolean isMemberOfAPublicGroup(Long contribution);

  /**
   * Get the full history of given contribution, ie, all modifications with their owners made to given contribution
   *
   * @param contribution a contribution id
   * @return a (possibly empty) list of history traces (list will be empty for non found contributions)
   */
  List<ContributionHistory> getHistory(Long contribution);

  /**
   * Retrieve all word profession banned
   *
   * @param contextType the context of banned words
   * @param type the type of banned words (beginning of the word, ...)
   * @param lang a two char iso-639-1 code
   * @return a list of words profession banned
   */
  List<WarnedWord> getWarnedWords(int contextType, int type, String lang);

  /**
   * Found a Place continent by code code
   *
   * @param code the code of the place
   * @return the matched place continent id, or null
   */
  Long retrievePlaceContinentCode(String code);

  /**
   * Found a Place by id
   *
   * @param placeId the id of the place
   * @return the place, or null
   */
  Place retrievePlace(Long placeId);

  /**
   * Found a Place continent by code code
   *
   * @param geonameId the geoname id of the place
   * @param placeId the id of the place
   * @return the matched place id, or null
   */
  Long retrievePlaceByGeonameIdOrPlaceId(Long geonameId, Long placeId);

  /**
   * Find PlaceType by id
   *
   * @param code the id of the place type
   * @return a PlaceType
   */
  PlaceType findPlaceTypeByCode(int code);

  /**
   * Save place and return the save results
   *
   * @param places the places to save
   * @param contribution the contribution where link place
   */
  void savePlaces(List<Place> places, be.webdeb.infra.persistence.model.Contribution contribution);

  /**
   * Get the db Place that match geoname id or code, otherwise create it
   *
   * @param place the api place to found
   * @return the db place
   */
  be.webdeb.infra.persistence.model.Place savePlace(Place place);

  /**
   * Retrieve all validation states
   *
   * @return a list of validation states
   */
  List<ValidationState> getValidationStates();

  /**
   * Retrieve an external contribution by its id
   *
   * @param id a Contribution id
   * @return an ExternalContribution if given id is an external contribution, null otherwise
   */
  ExternalContribution retrieveExternal(Long id);

  /**
   * Get the list of contributions added by given contributor for given external source and with a max of results.
   *
   * @param contributor the contributor that added the contributions
   * @param externalSource the source where the contributions come from
   * @param maxResults the maximum of contributions to return
   * @return a (possibly empty) list of temporary contributions
   */
  List<ExternalContribution> getExternalContributionsByExternalSource(Long contributor, int externalSource, int maxResults);

  /**
   * Save an external contribution on behalf of a given contributor If externalExcerpt.getId has been set, update the
   * external contribution, otherwise create argument and update contribution id.
   *
   * All passed contribution (affiliation ids (aha) and folders) are also considered as valid.If an contribution has no id,
   * the contribution is considered as non-existing and created. This contribution is then returned.
   *
   * @param contribution a contribution external contribution to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   * @return a map of Contribution type and a possibly empty list of Contributions (Actors or Folders) created automatically with this
   * save action (new contributions)
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(ExternalContribution contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Update rejected state of given external contribution from temporary external service list.
   *
   * @param id an external contribution id to be update
   * @param rejected true if this external contribution must be set as rejected, false otherwise
   * @throws PersistenceException if given id does not correspond to an external contribution or an error occurred when
   * persisting the new state in the database
   */
  void updateDiscoveredExternalState(Long id, boolean rejected) throws PersistenceException;

  /**
   * Get the list of tag debates of the context contribution (only for debate multiple), if any
   *
   * @param id a context contribution id
   * @param contributor a contributor id
   * @param group a group id
   * @return the possibly empty list of tag debates
   */
  List<DebateTag> getTagDebates(Long id, Long contributor, int group);

  /**
   * Get the tag debates of the context contribution by given id (only for debate multiple), if any
   *
   * @param debateId a context contribution id
   * @param id the tag debate id
   * @return the corresponding tag debate, or null
   */
  DebateTag getTagDebate(Long debateId, Long id);

  /**
   * Get the list of categories of the given context contribution, if any
   *
   * @param id a context contribution id
   * @return the possibly empty list of categories for the given context contribution
   */
  List<TagCategory> getCategories(Long id);

  /**
   * Get the list of context has categories of the given context contribution, if any
   *
   * @param id a context contribution id
   * @return the possibly empty list of context has categories for the given context contribution
   */
  List<ContextHasCategory> getContextCategories(Long id);

  /**
   * Get all argument justification linked to this contribution if this contribution is a contextualized contribution
   *
   * @param context a context contribution id
   * @param subContext the tag sub context id
   * @param category a context contribution category id
   * @param superArgument an argument id
   * @param shade the link shade id
   * @return a possibly empty list of argument justifications
   */
  List<ArgumentJustification> getArgumentJustificationLinks(Long context, Long subContext, Long category, Long superArgument, Integer shade);

  /**
   * Get all citation justification linked to this contribution if this contribution is a contextualized contribution
   *
   * @param context a context contribution id
   * @param subContext the tag sub context id
   * @param category a context contribution category id
   * @param superArgument an argument id
   * @param shade the link shade id
   * @return a possibly empty list of citation justifications
   */
  List<CitationJustification> getCitationJustificationLinks(Long context, Long subContext, Long category, Long superArgument, Integer shade);

  /**
   * Get all argument justification linked to this contribution if this contribution is a contextualized contribution
   *
   * @param context a context contribution id
   * @return a possibly empty list of argument justifications
   */
  List<ArgumentJustification> getArgumentJustificationLinks(Long context);

  /**
   * Get all citation justification linked to this contribution if this contribution is a contextualized contribution
   *
   * @param context a context contribution id
   * @return a possibly empty list of citation justifications
   */
  List<CitationJustification> getCitationJustificationLinks(Long context);

  /**
   * Check if a citation justification link exists.
   *
   * @param contextId       a contribution context id
   * @param subContextId the tag sub context id
   * @param categoryId      a tag category id
   * @param superArgumentId an argument id
   * @param citationId      a citation id
   * @param shade           a justification link shade
   * @return true if such a link already exists
   */
  boolean citationJustificationLinkAlreadyExists(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade);

  /**
   * Retrieve an unique citation justification link.
   *
   * @param contextId       a contribution context id
   * @param subContextId the tag sub context id
   * @param categoryId      a tag category id
   * @param superArgumentId an argument id
   * @param citationId      a citation id
   * @param shade           a justification link shade
   * @return a citation justification link
   */
  CitationJustification findCitationJustificationLink(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade);

  /**
   * Get the last order number in the context
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param argument an argument id
   * @param shade the link shade id
   * @return the last order
   */
  int getMaxCitationJustificationLinkOrder(Long context, Long subContext, Long category, Long argument, int shade);

  /**
   * Get the list of all citations in the given context contribution
   *
   * @param id a context contribution id
   * @return a possibly empty list of citations
   */
  List<Citation> getAllCitations(Long id);

  /**
   * Get the list of all citations in the given context contribution
   *
   * @param query the query used for retrieve citations
   * @return a possibly empty list of citations
   */
  List<Citation> getAllCitations(SearchContainer query);

  /**
   * Get all citations linked to this contribution if this contribution is a contextualized contribution
   *
   * @param id a context contribution id
   * @param category a context contribution category id
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromJustifications(Long id, Long category);

  /**
   * Get the list of texts where come the citations in given context contribution
   *
   * @param context the context contribution id
   * @param contributor the contributor id
   * @param group the current group id
   * @return a possibly empty list of texts
   */
  List<Text> getTextsCitations(Long context, Long contributor, int group);

  /**
   * Get the list of citation links in the given context where the given actor is the author
   *
   * @param actor an actor id
   * @return a possibly empty list of citation links
   */
  List<CitationJustification> getActorCitationJustifications(Long id, Long actor);

  /**
   * Get the list of citation links in the give context that come from given text
   *
   * @param text a text id
   * @return a possibly empty list of citation  links
   */
  List<CitationJustification> getTextCitationJustifications(Long id, Long text);

  /**
   * Get the list of citation links in this context that come from given text
   *
   * @param text a text id
   * @return a possibly empty list of citation  links
   */
  List<CitationPosition> getTextCitationPositions(Long id, Long text);

  /**
   * Retrieve all languages
   *
   * @return a list of languages
   */
  List<Language> getLanguages();

  /**
   * Retrieve all modification status
   *
   * @return a list of modification status
   */
  List<ModificationStatus> getModificationStatus();

  /**
   * Retrieve all justification link types
   *
   * @return the list of all justification link types
   */
  List<JustificationLinkType> getJustificationLinkTypes();

  /**
   * Retrieve all position link types
   *
   * @return the list of all position link types
   */
  List<PositionLinkType> getPositionLinkTypes();

  /**
   * Retrieve all similarity link types
   *
   * @return the list of all similarity link types
   */
  List<SimilarityLinkType> getSimilarityLinkTypes();

  /**
   * Temporary save this context contribution to get an id for sub objects
   *
   * @param contribution the context contribution id to save
   * @param type the context contribution type
   * @param contributor the Contributor id that initiated the save action
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   *
   * @throws PermissionException if the save action(s) could not been performed because of an issue regarding a
   * contributor permissions or because this operation would cause a problem of integrity.
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with
   * the persistence layer
   */
  void saveContextContribution(ContextContribution contribution, EContributionType type, Long contributor, int currentGroup) throws PermissionException, PersistenceException;

  /**
   * Delete the temporary context contribution in case of error
   *
   * @param contribution the context contribution id to delete
   * @param contributor the Contributor id that initiated the save action
   *
   * @throws PermissionException if the save action(s) could not been performed because of an issue regarding a
   * contributor permissions or because this operation would cause a problem of integrity.
   */
  void deleteContextContribution(Long contribution, Long contributor) throws PermissionException;

  /**
   * Get the map of number of linked elements with this contribution
   *
   * @param id a contribution id
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the id of the group where stats must be counted
   * @return the map of number of linked elements with this contribution by contribution type
   */
  Map<EContributionType, Integer> getCountRelationsMap(Long id, Long contributorId, int groupId);

  /**
   * Get all contribution id for a given contribution type
   *
   * @param type the contribution type to focus
   * @return a possibly empty list of contribution id for the given type
   */
  List<Long> getAllIdByContributionType(EContributionType type);

  /**
   * Get the name of a technical data like ActorType, ...
   *
   * @param id a ETechnicalType id
   * @param value the value of the type
   * @param lang the user lang
   * @return the name of the technical type
   */
  String getTechnicalName(String id, String value, String lang);

  /**
   * Check if a link exists between two given contributions.
   *
   * @param origin an origin contribution id (can be context, or origin)
   * @param destination a destination contribution id
   * @return true if such a link already exists
   */
  boolean linkAlreadyExists(Long origin, Long destination);


  void createFakeCitations(int nbCitations);

  void createFakePositions(int nbDebates, int nbPositionsPerDebate);

  /**
   * Update contributions relevance.
   */
  void updateContributionsRelevance();

  /**
   * Make a claim on a contribution from a contributor.
   *
   * @param contribution the contribution to claim
   * @param contributor the contributor who claims
   * @param url the url where the contributor do the claim
   * @param comment the comment of the claim
   * @param type the type of claim
   * @param group the group where the claim is done
   * @return true if the claim if done
   */
  boolean claimContribution(Long contribution, Long contributor, String url, String comment, EClaimType type, int group);

  /**
   * Delete a claim from a contribution and a contributor.
   *
   * @param contribution the contribution to claim
   * @return true if the claim is deleted
   */
  boolean deleteClaim(Long contribution, Long contributor);

  /**
   * Get the list of claims
   *
   * @param contributor the contributor that want to see claims
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param lang the user lang
   * @return a possiblity empty list of claims
   */
  List<ClaimHolder> retrieveClaims(Long contributor, int fromIndex, int toIndex, String lang);

  /**
   * Check if given contribution can be edited in given group by given user
   *
   * @param contributionId a contribution id
   * @param contributorId a contributor id
   * @param groupId a group id
   * @return true if the given contribution can be edited
   */
  boolean contributionCanBeEdited(Long contributorId, Long contributionId, int groupId);
}
