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
import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface is the Factory that handles Actor objects
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ActorFactory extends ContributionFactory {

  /**
   * Retrieve an Actor by its id
   *
   * @param id a Contribution id
   * @return an Actor if given id is an actor, null otherwise
   */
  Actor retrieve(Long id);

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
  String getPersonGender(Long idActor);

  /**
   * Retrieve an Actor by its id and increment visualization hit of this contribution
   *
   * @param id an Actor id
   * @return the Actor concrete object corresponding to the given id, null if no found
   */
  Actor retrieveWithHit(Long id);

  /**
   * Construct a new generic Actor instance
   *
   * @return an Actor instance
   */
  Actor getActor();

  /**
   * Construct a new Person instance
   *
   * @return a Person instance
   */
  Person getPerson();

  /**
   * Construct a new organization instance
   *
   * @return an Organization instance
   */
  Organization getOrganization();

  /**
   * Construct a new ExternalAuthor instance
   *
   * @return an ExternalAuthor instance
   */
  ExternalAuthor getExternalAuthor();

  /**
   * Get all actor organizations
   *
   * @return the list of actor organizations
   */
  List<Actor> getAllActorOrganizations();

  /**
   * Construct a new actor name instance
   *
   * @param lang iso-639-1 alpha-2 code of the language associated to this name
   * @return an actor name instance
   */
  ActorName getActorName(String lang);

  /**
   * Construct a new Affiliation instance
   *
   * @return an Affiliation instance
   */
  Affiliation getAffiliation();

  /**
   * Try to find an affiliation with given id and construct a new Affiliation instance
   *
   * @param id an affiliation id
   * @return an Affiliation instance from given id, or null if not found
   */
  Affiliation getAffiliation(Long id);

  /**
   * Construct an Actor role object for given Actor and contribution
   *
   * @param actor an Actor
   * @param contribution a contribution
   * @return an ActorRole object for given Actor in given contribution
   */
  ActorRole getActorRole(Actor actor, Contribution contribution);

  /**
   * Find actor role for given Actor and contribution
   *
   * @param actor an Actor id
   * @param contribution a contribution id
   * @param type the contribution type
   * @return an ActorRole object for given Actor in given contribution
   */
  // TODO add the role to unbind (author, reporter, cited)
  ActorRole findActorRole(Long actor, Long contribution, EContributionType type);

  /**
   * Find a list of Actor by their (partial) name
   *
   * @param name a name to search for
   * @param type the Actor type to look for, if UNKNOWN passed, all Actor types are retrieved.
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
   * Find a list of Actor by their full name, i.e. the Actor name must perfectly match the given name.
   *
   * @param fullname a full name to search for
   * @param type the Actor type to search for (unknown to search for any type of actor)
   * @return the list of Actors of given type with their names being identical to the given name, or an empty
   * list if none found
   */
  List<Actor> findByFullname(String fullname, EActorType type);

  /**
   * Construct a ActorType instance
   *
   * @param type an Actor type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a new ActorType instance
   */
  ActorType createActorType(int type, Map<String, String> i18names);

  /**
   * Get an Actor type by its id
   *
   * @param type a type id
   * @return an ActorType instance
   *
   * @throws FormatException if given id is invalid
   */
  ActorType getActorType(int type) throws FormatException;

  /**
   * Retrieve all Actor types
   *
   * @return a list of Actor types
   */
  List<ActorType> getActorTypes();

  /**
   * Construct a ProfessionType instance
   *
   * @param type a Profession type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a new ProfessionType instance
   */
  ProfessionType createProfessionType(int type, Map<String, String> i18names);

  /**
   * Get a Profession type by its id
   *
   * @param type a type id
   * @return an ProfessionType instance
   *
   * @throws FormatException if given id is invalid
   */
  ProfessionType getProfessionType(int type) throws FormatException;

  /**
   * Retrieve all Profession types
   *
   * @return a list of Profession types
   */
  List<ProfessionType> getProfessionTypes();

  /**
   * Construct a ProfessionType instance
   *
   * @param type a precision date type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a new PrecisionDateType instance
   */
  PrecisionDateType createPrecisionDateType(int type, Map<String, String> i18names);

  /**
   * Get a precision date type by its id
   *
   * @param type a type id
   * @return an PrecisionDateType instance
   *
   * @throws FormatException if given id is invalid
   */
  PrecisionDateType getPrecisionDateType(int type) throws FormatException;

  /**
   * Retrieve all precision date types
   *
   * @return a list of precision date types
   */
  List<PrecisionDateType> getPrecisionDateTypes();

  /**
   * Get a Gender instance by its id
   *
   * @param id a gender id
   * @return a Gender instance corresponding to given id
   *
   * @throws FormatException if given id does not exist
   */
  Gender getGender(String id) throws FormatException;

  /**
   * Create a new Gender instance
   *
   * @param id a gender id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created Gender instance
   */
  Gender createGender(String id, Map<String, String> i18names);

  /**
   * Retrieve all genders
   *
   * @return the list of genders
   */
  List<Gender> getGenders();

  /**
   * Construct an affiliation type
   *
   * @param id an affiliation type id
   * @param actorType an affiliation actortype id
   * @param subtype an affiliation subtype id
   * @param i18names a map of (
   * @return an AffiliationType instance with given parameters
   */
  AffiliationType createAffiliationType(int id, int actorType, int subtype, Map<String, String> i18names);

  /**
   * Get the corresponding affiliation type for given id (only suitable for organizations)
   *
   * @param type an affiliation type id
   * @return an AffiliationType instance corresponding to given type id
   *
   * @throws FormatException if given id is invalid
   */
  AffiliationType getAffiliationType(int type) throws FormatException;

  /**
   * Get all affiliation types (only suitable for organizations)
   *
   * @return a list of affiliation types
   */
  List<AffiliationType> getAffiliationTypes();

  /**
   * Construct a legal status instance
   *
   * @param status a status id
   * @param i18names a map of pairs of the form (2-char ISO 639-1, name)
   * @return a LegalStatus instance
   */
  LegalStatus createLegalStatus(int status, Map<String, String> i18names);

  /**
   * Get a legal status by its id
   *
   * @param status a legal status id
   * @return a new LegalStatus instance
   *
   * @throws FormatException if given id is invalid
   */
  LegalStatus getLegalStatus(int status) throws FormatException;

  /**
   * Retrieve all legal statuses
   *
   * @return the list of all countries
   */
  List<LegalStatus> getLegalStatuses();

  /**
   * Construct an BusinessSector instance
   *
   * @param sector an business sector id
   * @param i18names a map of pairs of the form (2-char ISO 639-1, name)
   * @return an BusinessSector instance
   */
  BusinessSector createBusinessSector(int sector, Map<String, String> i18names);

  /**
   * Get a business sector by its id
   *
   * @param sector a sector id
   * @return a BusinessSector instance
   *
   * @throws FormatException if given id is invalid
   */
  BusinessSector getBusinessSector(int sector) throws FormatException;

  /**
   * Retrieve all business sectors
   *
   * @return the list of all sectors
   */
  List<BusinessSector> getBusinessSectors();

  /**
   * Retrieve the list of all known professions
   *
   * @return the list Profession already known
   */
  List<Profession> getProfessions();

  /**
   * Get a Profession instance by its id
   *
   * @param id a profession id
   * @return a Profession instance corresponding to given id
   *
   * @throws FormatException if given id does not exist
   */
  Profession getProfession(int id) throws FormatException;

  /**
   * Create a new Profession instance.
   *
   * @param id a profession id
   * @param i18names a map of pairs of the form (2-char ISO 639-1 code, name)
   * @return a Profession instance
   */
  Profession createProfession(int id, Map<String, Map<String, String>> i18names);

  /**
   * Create a new Profession instance.
   *
   * @param id a profession id
   * @param lang a 2-char ISO 639-1 code
   * @param gender the gender of the profession
   * @param title the title of the profession
   * @return a Profession instance
   */
  Profession createProfession(int id, String lang, String gender, String title);

  /**
   * Create a new Profession instance.
   *
   * @param id a profession id
   * @param type a profession type
   * @param i18names a map of pairs of the form (2-char ISO 639-1 code, name)
   * @return a Profession instance
   */
  Profession createProfession(int id, EProfessionType type, Map<String, Map<String, String>> i18names);

  /**
   * Create a new Profession instance.
   *
   * @param id a profession id
   * @param type a profession type
   * @param displayHierarchy the hierarchy must be displayed or not
   * @param i18names a map of pairs of the form (2-char ISO 639-1 code, name)
   * @return a Profession instance
   */
  Profession createProfession(int id, EProfessionType type, boolean displayHierarchy, Map<String, Map<String, String>> i18names);

  /**
   * Search for all professions containing the given term in the given language and in the given gender and given profession type
   *
   * @param term a term to search for
   * @param lang a two-char ISO code representing the language for the profession
   * @param gender a char code representing the gender for the term
   * @param type the profession type
   * @return a list of professions with their name containing the given term
   */
  List<Profession> findProfessions(String term, String lang, String gender, int type);

  /**
   * Search for all professions containing the given term in the given language and given profession type
   *
   * @param term a term to search for
   * @param lang a two-char ISO code representing the language for the profession
   * @param type the profession type
   * @return a list of professions with their name containing the given term
   */
  List<Profession> findProfessions(String term, String lang, int type);

  /**
   * Search for all professions containing given term in given language
   *
   * @param term a term to look for
   * @param idToIgnore an profession id to ignore
   * @param lang a two-char ISO 639-1 code representing the language for the profession
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of all professions containing given term in given language.
   */
  List<Profession> findProfessions(String term, Long idToIgnore, String lang, int fromIndex, int toIndex);

  /**
   * Search for a profession of given name
   *
   * @param name the name to look for (exact match)
   * @param lang a two-char ISO 639-1 code representing the language for the profession
   * @param type the profession type
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, String lang, int type);

  /**
   * Search for a profession of given name and profession type
   *
   * @param name the name to look for (exact match)
   * @param lang a two-char ISO 639-1 code representing the language for the profession
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, String lang);

  /**
   * Search for a profession of given name
   *
   * @param name the name to look for (exact match)
   * @param strict true if only strict matches are considered
   * @return the (possibly null) profession with given name
   */
  Profession findProfession(String name, boolean strict);

  /**
   * Get merge two given professions
   *
   * @param profession the profession id that will be keep
   * @param professionToMerge the profession id that will be merged into the first one
   * @return the id of the merged profession
   */
  int mergeProfessions(int profession, int professionToMerge) throws PersistenceException;

  /**
   * Get a Country instance by its id
   *
   * @param id a country iso-3166-1 alpha-2 code
   * @return a Country instance corresponding to given id
   *
   * @throws FormatException if given id does not exist
   */
  Country getCountry(String id) throws FormatException;

  /**
   * Create a new Country instance
   *
   * @param id a country id (should be in iso-3166-1 alpha-2 code)
   * @param i18names a map of pairs of the form (iso 639-1 language code, name)
   * @return the created Country instance
   */
  Country createCountry(String id, Map<String, String> i18names);

  /**
   * Retrieve all countries
   *
   * @return the list of all countries
   * @see Country
   */
  List<Country> getCountries();

  /**
   * Save a profession
   *
   * @param profession the profession to save
   * @return the id of the saved profession (-1 if an error occurred)
   */
  int saveProfession(Profession profession);

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
   * Reset the actor type of a given actor. This operation also delete the linked person or organization from db
   *
   * @param id an actor id
   */
  void resetActorType(Long id);

  /**
   * Get all affiliations related with this actor that are in the given actors id list
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
   * Get a randomly chose Actor
   *
   * @return a Debate
   */
  Actor random();

}
