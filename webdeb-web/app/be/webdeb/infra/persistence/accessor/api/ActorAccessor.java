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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.ActorAffiliations;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an accessor for Actor persisted objects.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ActorAccessor extends ContributionAccessor {

  /**
   * Get all actor organizations
   *
   * @return the list of actor organizations
   */
  List<Actor> getAllActorOrganizations();

  /**
   * Retrieve an actor by its id
   *
   * @param id a Contribution id
   * @param hit true if this retrieval must be counted a visualization hit
   * @return the actor for given id, null otherwise
   */
  Actor retrieve(Long id, boolean hit);

  /**
   * Retrieve a list of Actor by a list of actor ids
   *
   * @param ids a list of actor ids
   * @return a possibly empty list of actors
   */
  List<Actor> retrieveAll(List<Long> ids);

  /**
   *Get the gender of an actor
   *
   * @param idActor a Contribution id
   * @return the code of the gender (M, F)
   */
  String getGenderActor(Long idActor);

  /**
   * Find a list of Actor by their (partial) name
   *
   * @param name a name
   * @param type the Actor type to search for (UNKNOWN type means all types here)
   * @return the list of Actors with their names containing the given name, or an empty list if none found
   */
  List<Actor> findByName(String name, EActorType type);

  /**
   * Find a list of Actor by their (partial) name among paty member for election 2019
   *
   * @param name a name to search for
   * @param lang the user lang code
   * @return the list of Actors names - party
   */
  List<String> findPartyMemberByName(String name, String lang);

  /**
   * Find a list of Actor by their (partial) name
   *
   * @param name a name
   * @param type the Actor type to search for (UNKNOWN type means all types here)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of Actors with their names containing the given name, or an empty list if none found
   */
  List<Actor> findByName(String name, EActorType type, int fromIndex, int toIndex);

  /**
   * Find a list of Actor by their full name, i.e. the Actor name must perfectly match the given name.
   *
   * @param fullname a full name
   * @param type the Actor type to search for (UNKNOWN type means all types here)
   * @return the list of Actors of given type with their names being identical to the given name, or an empty
   * list if none found
   */
  List<Actor> findByFullname(String fullname, EActorType type);

  /**
   * Get all contributions in which given actor is flagged has being involved in
   *
   * @param actor an Actor id
   * @param type the contribution type enum value (in case of ALL, no actor will obviously be retrieved)
   * @return a Map of Contribution and associated ActorRole for these Contributions
   */
  Map<Contribution, ActorRole> getContributions(Long actor, EContributionType type);

  /**
   * Find actor role for given Actor and contribution
   *
   * @param actor an Actor
   * @param contribution a contribution
   * @return an ActorRole object for given Actor in given contribution
   */
  // TODO add the role to unbind (author, reporter, cited)
  ActorRole findActorRole(Actor actor, Contribution contribution);

  /**
   * Get all affiliated actors of given actor, in this case affiliation.getActor represents an affiliated
   * actor either than the affiliation actor, i.e., the actor belonging to given actor instead of all
   * actors to which given actor belongs to.
   *
   * @param actor an actor ID
   * @param type the actor type for affiliated
   * @return the (possibly empty) list of affiliation such as affiliation actors have given actor has affiliation,
   * an empty list if none found or unknown actor given
   */
  List<Affiliation> findAffiliatedActors(Long actor, EActorType type);

  /**
   * Find the order of affiliated actor of a given one by actor type
   *
   * @param id an actor id
   * @param type the actor type for affiliation
   * @return the (possibly empty) list of actor id
   */
  List<Long> sortAffiliatedActor(Long id, EActorType type);

  /**
   * Get all affiliated actors of given actor, in this case affiliation.getActor represents an affiliated
   * actor either than the affiliation actor, i.e., the actor belonging to given actor instead of all
   * actors to which given actor belongs to by aff type
   *
   * @param actor an actor ID
   * @param type the actor type for affiliated
   * @return the (possibly empty) list of affiliated actors for given actor for the given affiliation type,
   * an empty list if none found or unknown actor given
   */
  List<Affiliation> findAffiliatedActors(Long actor, EAffiliationType type);

  /**
   * Get all affiliations of given actor, all actors to which given actor is belonging to.
   *
   * @param actor an actor ID
   * @param type the actor type for affiliations
   * @return the (possibly empty) list of affiliations for given actor, an empty list if none found or unknown actor given
   */
  List<Affiliation> findAffiliations(Long actor, EActorType type);

  /**
   * Find the order of affiliated actor of a given one by actor type
   *
   * @param id an actor id
   * @param type the actor type for affiliation
   * @return the (possibly empty) list of actor id
   */
  List<Long> sortAffiliationActor(Long id, EActorType type);

  /**
   * Get all affiliations of given actor, all actors to which given actor is belonging to.
   *
   * @param actor an actor ID
   * @return the (possibly empty) list of affiliations for given actor, an empty list if none found or unknown actor given
   */
  List<Affiliation> findAllAffiliations(Long actor);

  /**
   * Get all affiliations (but not graduating and filiations) of given actor, all actors to which given actor is belonging to.
   *
   * @param actor an actor ID
   * @return the (possibly empty) list of affiliations for given actor, an empty list if none found or unknown actor given
   */
  List<Affiliation> findAffiliations(Long actor);

  /**
   * Get all affiliations of given actor for the given affiliation type, all actors to which given actor is belonging to.
   *
   * @param actor an actor ID
   * @param type an affiliation type
   * @return the (possibly empty) list of affiliations for given actor for the given affiliation type,
   * an empty list if none found or unknown actor given
   */
  List<Affiliation> findAffiliations(Long actor, EAffiliationType type);

  /**
   * Save an actor on behalf of a given contributor If actor.getId has been set, update the
   * actor, otherwise create actor and update contribution id.
   *
   * All passed contribution (affiliation ids (aha) and folders) are also considered as valid.If an contribution has no id,
   * the contribution is considered as non-existing and created. This contribution is then returned.
   *
   * @param contribution a contribution actor to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   * @return a map of Contribution type and a possibly empty list Contributions (Actors or Folders) created automatically with this
   * save action (new contributions)
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(Actor contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Retrieve all Actor types
   *
   * @return a list of Actor types
   */
  List<ActorType> getActorTypes();

  /**
   * Retrieve all precision date types
   *
   * @return a list of precision date types
   */
  List<PrecisionDateType> getPrecisionDateTypes();

  /**
   * Retrieve all Profession types
   *
   * @return a list of Profession types
   */
  List<ProfessionType> getProfessionTypes();

  /**
   * Retrieve all genders
   *
   * @return a list of genders
   */
  List<Gender> getGenders();

  /**
   * Retrieve all actor affiliation types
   *
   * @return a list of actor AffiliationTypes
   */
  List<AffiliationType> getAffiliationTypes();

  /**
   * Retrieve all countries
   *
   * @return a list of countries
   */
  List<Country> getCountries();

  /**
   * Find all professions
   *
   * @return a list of all known professions
   */
  List<Profession> getProfessions();

  /**
   * Search for all professions containing the given term in the given language and in the given gender
   *
   * @param term a term to search for
   * @param lang a two-char ISO code representing the language for the profession
   * @param gender a char code representing the gender for the term
   * @param type the profession type
   * @return a list of professions with their name containing the given term
   */
  List<be.webdeb.core.api.actor.Profession> findProfessions(String term, String lang, String gender, int type);

  /**
   * Search for all professions containing the given term in the given language and in the profession type
   *
   * @param term a term to search for
   * @param lang a two-char ISO code representing the language for the profession
   * @param type the profession type
   * @return a list of professions with their name containing the given term
   */
  List<be.webdeb.core.api.actor.Profession> findProfessions(String term, String lang, int type);

  /**
   * Search for all professions containing the given term in the given language
   *
   * @param term a term to search for
   * @param idToIgnore an profession id to ignore
   * @param lang a two-char ISO code representing the language for the profession
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of professions with their name containing the given term
   */
  List<Profession> findProfessions(String term, Long idToIgnore, String lang, int fromIndex, int toIndex);

  /**
   * Search for a profession of given id
   *
   * @param id the profession id
   * @return the (possibly null) profession with given id
   */
  Profession findProfession(int id);

  /**
   * Search for a profession of given name
   *
   * @param name the name to look for (exact match)
   * @param lang a two-char ISO 639-1 code representing the language for the profession
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, String lang);

  /**
   * Search for a profession of given name and a profession type
   *
   * @param name the name to look for (exact match)
   * @param lang a two-char ISO 639-1 code representing the language for the profession
   * @param type the profession type
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, String lang, int type);

  /**
   * Search for a profession of given name
   *
   * @param name the name to look for (exact match)
   * @param strict true if only strict matches are considered
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, boolean strict);

  /**
   * Search for a substitute profession of given profession id
   *
   * @param id a profession id
   * @return a (possibly null) profession that may be used as a substitute for given profession
   */
  Profession findSubstituteForProfession(int id);

  /**
   * Search for equivalent professions of given profession id
   *
   * @param id a profession id
   * @return a (possibly empty) list of professions that are equivalent to a given profession
   */
  List<Profession> findEquivalentsForProfession(int id);

  /**
   * Get the super link of given profession id
   *
   * @param id a profession
   * @return a (possibly null) profession
   */
  Profession getSuperLink(int id);

  /**
   * Retrieve all legal statuses for organizations
   *
   * @return a list of countries
   */
  List<LegalStatus> getLegalStatuses();

  /**
   * Retrieve all business sectors
   *
   * @return a list of sectors
   */
  List<BusinessSector> getBusinessSectors();

  /**
   * Get the profession simple name (to keep the same rules between api and db)
   *
   * @param professionId a profession id
   * @param lang the addition of a two char ISO 639-1 code and a char code (lang + gender)
   * @return a simple profession name or null
   */
  String getProfessionSimpleName(Integer professionId, String lang);

  /**
   * Retrieve a profession that could be the super link of a given profession
   *
   * @param professionId the identifier of the profession for which it is necessary to determine the super-link
   * @return the profession super link, may be null
   */
  Profession determineSuperLink(Integer professionId);

  /**
   * Retrieve all professions that could be sub-links of a given profession
   *
   * @param professionId the identifier of the profession for which it is necessary to determine the sub-links
   * @return a list of professions, could be empty
   */
  List<Profession> determineSubLinks(Integer professionId);

  /**
   * Build the hierarchy of function as String. Ex : "President -> Federal President -> ..."
   *
   * @param professionId the identifier of the profession for which it is necessary to determine the sub-links
   * @param lang the addition of a two char ISO 639-1 code and a char code (lang + gender)
   * @return a map of profession names and boolean to displayHierarchy with the given profession itself, or empty list if profession is not found
   */
  Map<String, Boolean> getFunctionHierarchy(Integer professionId, String lang);

  /**
   * Get merge two given professions
   *
   * @param profession the profession id that will be keep
   * @param professionToMerge the profession id that will be merged into the first one
   * @return the id of the merged profession
   */
  int mergeProfessions(int profession, int professionToMerge) throws PersistenceException;

  /**
   * Reset the actor type of a given actor. This operation also delete the linked person or organization from db
   *
   * @param id an actor id
   */
  void resetActorType(Long id);

  /**
   * Get the list of texts where actor is author of at least one citation
   *
   * @param actorId an actor id
   * @param contributor the contributor id
   * @param group the current group id
   * @return a possibly empty list of texts
   */
  List<Text> getTextsWhereCitationAuthor(Long actorId, Long contributor, int group);

  /**
   * Get the list of texts where actor is cited in at least one citation
   *
   * @param actorId an actor id
   * @param contributor the contributor id
   * @param group the current group id
   * @return a possibly empty list of texts
   */
  List<Text> getTextsWhereCitationCited(Long actorId, Long contributor, int group);

  /**
   * Get all affiliations related with given actor that are in the given actors id list
   *
   * @param id the concerned actor id
   * @param actorIds the list of possibly related actors
   * @return the list of affiliations
   */
  List<Affiliation> getAffiliationsRelatedTo(Long id, List<Long> actorIds);

  /**
   * Get the map of group key / allies opponents actor of the given context (in query). The result of this map depends
   * of the type of vision passed on the SearchContainer.
   *
   * @param query all needed data to perform the query
   * @return the map of allies opponents actors
   * @see be.webdeb.core.api.contribution.EAlliesOpponentsType
   */
  Map<ESocioGroupKey, List<ActorAlliesOpponents>> getAlliesOpponentsMap(SearchContainer query);

  /**
   * Get the map of link type / citations from allies opponents actor of the given context (in query) for a specific
   * id (actor id, age category, country, ...) and for a specific ESocioGroupKey.
   *
   * @param query all needed data to perform the query
   * @return the map of allies opponents actors
   * @see be.webdeb.core.api.contribution.EAlliesOpponentsType
   * @see ESocioGroupKey
   */
  Map<Integer, List<Citation>> getAlliesOpponentsCitationsMap(SearchContainer query);

  /**
   * Get the map of ESocio key / actor distance with given actor id for global positions
   *
   * @param query all needed data to perform the query
   * @return the map with possibly empty values list
   */
  Map<ESocioGroupKey, List<ActorDistance>> getActorsDistance(SearchContainer query);

  /**
   * Get the map of actor distance with given actor id for global positions
   *
   * @param query all needed data to perform the query
   * @return the map with possibly empty values list
   */
  List<ActorDistance> getActorsDistanceByDebates(SearchContainer query);

  /**
   * Get the minimum or maximum affiliation of given actor for given actor type.
   *
   * @param id the actor id to found dates
   * @param actorType the given actor type
   * @param viewedType the actor type that we need to look
   * @param forMin true for minimum, false for maximum
   * @return the minimum or maximum date
   */
  String getMinAndMaxAffiliationDate(Long id, EActorType actorType, EActorType viewedType, boolean forMin);

  /**
   * Get the list of affiliations where actors own given actor (given actor must be an organization)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getOwners(SearchContainer query);

  /**
   * Get the list of affiliations where actors are owned by given actor (given actor must be an organization)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getOwnedOrganizations(SearchContainer query);

  /**
   * Get the list of affiliations where given actor is part of (given actor must be a person)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getAffiliationOrganizations(SearchContainer query);

  /**
   * Get all citations where given actor is text's and citation's author
   *
   * @param actor an actor id
   * @param text the text id
   * @return a possibly empty list of citations
   */
  List<Citation> getTextsAuthorCitations(Long actor, Long text);

  /**
   * Get the list of affiliations where actors own given actor (must be an organization)
   *
   * @param query all needed data to perform the query
   * @return the list of affiliations as ActorAffiliations
   */
  List<ActorAffiliations> getMembers(SearchContainer query);

  /**
   * Get all actors where that speak about given actor (authors where given actor is cited)
   *
   * @param query all data needed to get all actors
   * @return a possibly empty list of actors
   */
  List<Actor> getOthersActorsCitations(SearchContainer query);

  /**
   * Get all texts where given actors is author or cited in text's tags
   *
   * @param query all data needed to get all tags
   * @return a possibly empty list of tags
   */
  List<Tag> getTagsCitations(SearchContainer query);

  /**
   * Get all texts where given actors is author or cited in text's citations
   *
   * @param query all data needed to get all texts
   * @return a possibly empty list of texts
   */
  List<Text> getTextsCitations(SearchContainer query);

  /**
   * Get all debates where given actors is cited or where given actors is author or cited in debate's citations
   *
   * @param query all data needed to get all debates
   * @return a possibly empty list of debates
   */
  List<Debate> getDebatesCitations(SearchContainer query);

  /**
   * Get all citations where given actors is author or cited
   *
   * @param query all data needed to get all actor's citation
   * @return a possibly empty list of citations
   */
  List<Citation> getCitations(SearchContainer query);

  /**
   * Get all citations where given actors is author or cited in given contribution
   *
   * @param query all data needed to get all actor's citation
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromContribution(SearchContainer query);

  /**
   * Get a randomly chose Actor
   *
   * @return a Debate
   */
  Actor random();

}
