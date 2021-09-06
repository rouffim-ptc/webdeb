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

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents an accessor for arguments persisted into the database
 *
 * @author Fabian Gilson
 */
public interface ArgumentAccessor extends ContributionAccessor {

  /**
   * Retrieve an Argument by its id
   *
   * @param id an argument id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the argument concrete object corresponding to the given id, null if not found
   */
  Argument retrieve(Long id, boolean hit);

  /**
   * Retrieve a ArgumentShaded by its id
   *
   * @param id a Contribution id
   * @param hit true if this retrieval must be counted as a visualization
   * @return a shaded argument concrete object corresponding to the given id, null if not found
   */
  ArgumentShaded retrieveShaded(Long id, boolean hit);

  /**
   * Retrieve an ArgumentDictionary by its id
   *
   * @param id an argument dictionary id
   * @return the argument dictionary concrete object corresponding to the given id, null if not found
   */
  ArgumentDictionary retrieveDictionary(Long id);

  /**
   * Retrieve an argument justification link by its id
   *
   * @param id a Contribution id
   * @return an ArgumentJustification if given id is an argument justification link, null otherwise
   */
  ArgumentJustification retrieveJustificationLink(Long id);

  /**
   * Retrieve an argument similarity link by its id
   *
   * @param id a Contribution id
   * @return an ArgumentSimilarity if given id is an argument similarity link, null otherwise
   */
  ArgumentSimilarity retrieveSimilarityLink(Long id);

  /**
   * Save an argument on behalf of a given contributor If argument.getId has been set, update the
   * argument, otherwise create argument and update contribution id.
   *
   * All passed contribution (affiliation ids (aha) and tags) are also considered as valid.
   *
   * @param contribution a contribution argument to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  void save(Argument contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save an argument dictionary on behalf of a given contributor If dictionary.getId has been set, update the
   * dictionary, otherwise create dictionary and update contribution id.
   *
   * All passed contribution and dependances are also considered as valid.
   *
   * @param contribution a contribution argument dictionary to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  void save(ArgumentDictionary contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save an argument justification link. Context contribution, tag category, super argument and the argument provided 
   * in given link must exist, as well as the contributor.
   *
   * @param link the link to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(ArgumentJustification link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save an argument similarity link. Both arguments provided in given link must exist, as well as the contributor.
   *
   * @param link the link to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(ArgumentSimilarity link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Get all similarity links that are bound to the given argument.
   *
   * @param id the given argument id
   * @return the (possibly empty) list of similarity links involving the given argument
   */
  List<ArgumentSimilarity> getSimilarityLinks(Long id);

  /**
   * Get all similar arguments id that match with given shade for the given argument.
   *
   * @param id the argument id
   * @param shade the link shade to keep
   * @return the (possibly empty) list of similar argument id involving this argument
   */
  Set<Long> getSimilarIds(Long id, ESimilarityLinkShade shade);

  /**
   * Retrieve all argument types
   *
   * @return a list of argument types
   */
  List<ArgumentType> getArgumentTypes();

  /**
   * Retrieve all argument shades
   *
   * @return a list of argument shades
   */
  List<ArgumentShade> getArgumentShades();

  /**
   * Check if an argument is similar to another one
   *
   * @param argToCompare the argument to compare with the other one
   * @param argument an argument
   * @return true if both arguments are similar
   */
  boolean isSimilarWith(Long argToCompare, Long argument);

  /**
   * Find a list of Argument by title
   *
   * @param title an argument title
   * @param lang a two char iso-639-1 language code
   * @param type the type of argument, if any otherwise -1
   * @param shade the shade of argument, if any otherwise -1
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of Argument with their title containing the given title
   */
  List<Argument> findByTitle(String title, String lang, int type, int shade, int fromIndex, int toIndex);

  /**
   * Find an unique Argument by title, lang and shade
   *
   * @param title an argument title
   * @param lang a two char iso-639-1 language code
   * @param shade the argument shade if needed
   * @return a corresponding argument or null
   */
  Argument findUniqueByTitleLangAndShade(String title, String lang, EArgumentShade shade);

  /**
   * Find an ArgumentDictionary by title
   *
   * @param title an argument dictionary title
   * @param lang a two char iso-639-1 language code
   * @return a corresponding argument dictionary or null
   */
  ArgumentDictionary findUniqueDictionaryByTitle(String title, String lang);

  /**
   * Find a list of ArgumentDictionary by title
   *
   * @param title an argument dictionary title
   * @param lang a two char iso-639-1 language code
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of ArgumentDictionary with their title containing the given title
   */
  List<ArgumentDictionary> findDictionaryByTitle(String title, String lang, int fromIndex, int toIndex);

  /**
   * Find unique argument justification by its context id, us context id, tag category id, super argument id, argument id and shade id.
   * This signature must be unique.
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param superArgument the super argument of the link id
   * @param argument the argument of the link id
   * @param shade the link shade id
   * @return true if the argument exists
   */
  boolean argumentJustificationLinkAlreadyExists(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade);

  /**
   * Find unique argument justification by its context id, sub context id, tag category id, super argument id, argument id and shade id.
   * This signature must be unique.
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param superArgument the super argument of the link id
   * @param argument the argument of the link id
   * @param shade the link shade id
   * @return the argument justification link
   */
  ArgumentJustification findArgumentJustification(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade);

  /**
   * Get the last order number in the context
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param superArgument the super argument of the link id
   * @param shade the link shade id
   * @return the last order
   */
  int getMaxArgumentJustificationLinkOrder(Long context, Long subContext, Long category, Long superArgument, int shade);

  /**
   * Find a list of argument justification links for given context, sub context, category, superArgument and shade
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param superArgument the super argument of the link id
   * @param shade the link shade id
   * @return a possibly empty list of argument justification links
   */
  List<ArgumentJustification> findArgumentLinks(Long context, Long subContext, Long category, Long superArgument, int shade);

  /**
   * Get the number of citations linked with given argument in given context
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param argument the argument of the link id
   * @param shade the link shade id
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of citation justification links
   */
  int getArgumentNbCitationsLink(Long context, Long subContext, Long category, Long argument, Integer shade, Long contributor, int group);

  /**
   * Get a randomly chose Argument
   *
   * @return an Argument
   */
  Argument random();

}
