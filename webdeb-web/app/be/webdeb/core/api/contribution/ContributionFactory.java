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

package be.webdeb.core.api.contribution;

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.link.ContextHasSubDebate;
import be.webdeb.core.api.contribution.link.JustificationLinkType;
import be.webdeb.core.api.contribution.link.PositionLinkType;
import be.webdeb.core.api.contribution.link.SimilarityLinkType;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.account.ClaimHolder;
import be.webdeb.util.ValuesHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents an abstract factory to handle all types of Contribution
 *
 * @author Fabian Gilson
 */
public interface ContributionFactory {

  /**
   * Retrieve a Contribution by its id. Since this method is only implemented in concrete accessors,
   * a concrete subtype is returned, and given id must be of same type as accessor's manipulated data.
   *
   * @param id a Contribution id
   * @return the Contribution concrete object corresponding to the given id, null if no found
   */
  Contribution retrieve(Long id);

  /**
   * Retrieve a Contribution by its id and type. Invoker must explicitly cast returned value into
   * concrete type (as given in "type" parameter) to access concrete methods.
   *
   * @param id a Contribution id
   * @param type a contribution type (may pass EContributionType.ALL value if type is unknown)
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
   * Retrieve a Contribution by its id and increment visualization hit of this contribution
   *
   * @param id a Contribution id
   * @return the Contribution concrete object corresponding to the given id, null if no found
   */
  Contribution retrieveWithHit(Long id);

  /**
   * Get a ContributionType instance by its id
   *
   * @param id a type id
   * @return a ContributionType instance corresponding to given id
   *
   * @throws FormatException if given id is invalid
   */
  ContributionType getContributionType(EContributionType id);

  /**
   * Create a new ContributionType instance
   *
   * @param id a contribution type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created ContributionType instance
   */
  ContributionType createContributionType(Integer id, Map<String, String> i18names);

  /**
   * Create a ContributionHistory instance with given contributor, status and full trace
   *
   * @param contributor a contributor that made a modification with given trace
   * @param status the status of the modification
   * @param trace the full trace, ie, the stringified contribution as saved at that moment
   * @param version the version date
   * @return a contribution history instance
   */
  ContributionHistory createHistory(Contributor contributor, EModificationStatus status, String trace, Date version);

  /**
   * Get all Contribution types
   *
   * @return the list of all contribution types
   */
  List<ContributionType> getContributionTypes();

  /**
   * Get the Contributor that created the given Contribution
   *
   * @param id a Contribution id
   * @return the creator of the given Contribution
   */
  ContributionHistory getCreator(Long id);

  /**
   * Get the last contributor that is in a given group that update a given contribution
   *
   * @param id a Contribution id
   * @param group a Group id
   * @return the last Contributor of the given Contribution in the given group
   */
  Contributor getLastContributorInGroup(Long id, int group);

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
   * Find a list of Contributions by a list of criteria, being key-value pairs.
   *
   * @param criteria a list of key-value pairs to search for
   * @param strict check if we must perform a strict search
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of Contributions that fulfils the given criteria
   */
  List<Contribution> findByCriteria(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex);

  /**
   * Get the amount of given type of contribution
   * @param type a type of contribution
   * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned)
   * @return the amount of contribution of given type
   */
  long getAmountOf(EContributionType type, int group);

  /**
   * Find a list of Contribution ordered by version time (actor, text and argument only) in given group
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
   * Get the helper class to check constraints on values
   *
   * @return the values instance
   */
  ValuesHelper getValuesHelper();

  /**
   * Save markings and validated state for given contributions
   *
   * @param contributions a list of contributions
   * @throws PersistenceException if any marking could not be saved (all or nothing)
   */
  void saveMarkings(List<Contribution> contributions) throws PersistenceException;

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
   *   be rebound to replacement actor. </li>
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
   * @throws PermissionException if given contributor is not owner of both groups to which given contributions belongs to
   */
  boolean merge(Long origin, Long replacement, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Get the full history of given contribution, ie, all modifications with their owners made to given contribution
   *
   * @param contribution a contribution id
   * @return a (possibly empty) list of history traces (list will be empty for non found contributions)
   */
  List<ContributionHistory> getHistory(Long contribution);

  /**
   * Get the default language code
   *
   * @return a two char iso-639-1 code
   */
  String getDefaultLanguage();

  /**
   * Create a new WordBannedProfession instance
   *
   * @param id an id representing the banned word
   * @param title the title of the warned word
   * @param lang the language of the warned word
   * @param type a int representing the type of the word (begin word, ...)
   * @param contextType a int representing the warned word type (or context)
   * @return the created WordBannedProfession instance
   * @see EWarnedWordType
   * @throws FormatException if given lang doesn't exists
   */
  WarnedWord createWarnedWord(int id, String title, String lang, int type, int contextType) throws FormatException;

  /**
   * Get a WordBannedProfession instance by its id
   *
   * @param contextType the context of banned words
   * @param type the type of banned words (beginning of the word, ...)
   * @param lang a two char iso-639-1 code
   * @return the list of all word banned profession
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
   * Create a Place instance with just the id
   *
   * @param idPlace the id of the place
   * @return the created Place instance
   */
  Place createSimplePlace(Long idPlace);

  /**
   * Create a Place instance
   *
   * @param idPlace the id of the place
   * @param geonameId the geoname id of the place, can be null
   * @param code the code of the place, can be null
   * @param latitude the latitude of the place
   * @param longitude the longitude of the place
   * @param names the map of names of the place
   * @return the created Place instance
   */
  Place createPlace(Long idPlace, Long geonameId, String code, String latitude, String longitude, Map<String, String> names);

  /**
   * Create a PlaceType instance
   *
   * @param idPlaceType the id of the place type
   * @param i18names names of the place type
   * @return the created PlaceType instance
   */
  PlaceType createPlaceType(int idPlaceType, Map<String, String> i18names);

  /**
   * Find PlaceType by id
   *
   * @param code the id of the place type
   * @return a PlaceType
   */
  PlaceType findPlaceTypeByCode(int code);

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
   * Retrieve all validation states
   *
   * @return the list of all validation states
   */
  List<ValidationState> getValidationStates();

  /**
   * Construct a ValidationState instance
   *
   * @param state a state id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a ValidationState corresponding to given id
   */
  ValidationState createValidationState(int state, Map<String, String> i18names);

  /**
   * Get a validation state by its id
   *
   * @param state a text validation state id
   * @return the ValidationState corresponding to the given state id
   *
   *
   * @throws FormatException if given id is invalid
   */
  ValidationState getValidationState(int state) throws FormatException;

  /**
   * Get a validation state by boolean
   *
   * @param state a boolean state
   * @return the ValidationState corresponding to the given boolean state
   *
   */
  ValidationState getValidationState(boolean state);

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
   * Update status of external contribution with given rejected status. If rejected, this contribution will not be shown
   * again for validation and not retrieved when getting all.
   *
   * @param id an external contribution id
   * @param rejected true if this contribution must be ignored for further validation requests, false otherwise
   * @throws PersistenceException if given id does not correspond to an external contribution or an error occurred when
   * persisting the new state in the database
   */
  void updateDiscoveredExternalContributionState(Long id, boolean rejected) throws PersistenceException;

  /**
   * Get a Language instance by its iso-639-1 code
   *
   * @param code a language code
   * @return a Language instance corresponding to given code
   *
   * @throws FormatException if given id does not exist
   */
  Language getLanguage(String code) throws FormatException;

  /**
   * Create a new Language instance
   *
   * @param code two-char ISO-639-1 code of the language
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created Language instance
   */
  Language createLanguage(String code, Map<String, String> i18names);

  /**
   * Retrieve all languages
   *
   * @return the list of all languages
   */
  List<Language> getLanguages();

  /**
   * Get a ModificationStatus instance by its iso-639-1 code
   *
   * @param status a modification status
   * @return a ModificationStatus instance corresponding to given status
   *
   * @throws FormatException if given id does not exist
   */
  ModificationStatus getModificationStatus(int status) throws FormatException;

  /**
   * Create a new ModificationStatus instance
   *
   * @param status the modification status
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created ModificationStatus instance
   */
  ModificationStatus createModificationStatus(int status, Map<String, String> i18names);

  /**
   * Retrieve all modification status
   *
   * @return the list of all modification status
   */
  List<ModificationStatus> getModificationStatus();

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
   * @param origin the origin contribution id (the context contribution or super contribution)
   * @param destination the destination contribution id
   * @return true if such a link already exists
   */
  boolean linkAlreadyExists(Long origin, Long destination);

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
   * Construct a justification link type
   *
   * @param type a type id
   * @param i18names a map of pairs of the form (2-char iso-code, type name)
   * @return a JustificationLinkType instance
   */
  JustificationLinkType createJustificationLinkType(int type, Map<String, String> i18names);

  /**
   * Get a JustificationLinkType object from a given justification link shade id
   *
   * @param shade a shade id
   * @return the JustificationLinkType corresponding to the given shade
   *
   * @throws FormatException if given id is invalid
   */
  JustificationLinkType getJustificationLinkType(int shade) throws FormatException;

  /**
   * Retrieve all justification link types
   *
   * @return the list of all justification link types
   */
  List<JustificationLinkType> getJustificationLinkTypes();

  /**
   * Construct a position link type
   *
   * @param type a type id
   * @param i18names a map of pairs of the form (2-char iso-code, type name)
   * @return a PositionLinkType instance
   */
  PositionLinkType createPositionLinkType(int type, Map<String, String> i18names);

  /**
   * Get a PositionLinkType object from a given position link shade id
   *
   * @param shade a shade id
   * @return the PositionLinkType corresponding to the given shade
   *
   * @throws FormatException if given id is invalid
   */
  PositionLinkType getPositionLinkType(int shade) throws FormatException;

  /**
   * Retrieve all position link types
   *
   * @return the list of all position link types
   */
  List<PositionLinkType> getPositionLinkTypes();

  /**
   * Construct a similarity link type
   *
   * @param type a type id
   * @param i18names a map of pairs of the form (2-char iso-code, type name)
   * @return a SimilarityLinkType instance
   */
  SimilarityLinkType createSimilarityLinkType(int type, Map<String, String> i18names);

  /**
   * Get a SimilarityLinkType object from a given similarity link shade id
   *
   * @param shade a shade id
   * @return the SimilarityLinkType corresponding to the given shade
   *
   * @throws FormatException if given id is invalid
   */
  SimilarityLinkType getSimilarityLinkType(int shade) throws FormatException;

  /**
   * Retrieve all similarity link types
   *
   * @return the list of all similarity link types
   */
  List<SimilarityLinkType> getSimilarityLinkTypes();

  /**
   * Make shade reader friendly by avoiding shade ending vowel when title starts with a vowel
   *
   * @param shade a shade term
   * @param follower the string that will follow the shade
   * @param lang the language code (2-char ISO) used for the shade term
   * @return a user friendly shade term (including space if needed)
   */
  String makeShadeReaderFriendly(String shade, String follower, String lang);


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
   * @param contributor the contributor who claims
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
