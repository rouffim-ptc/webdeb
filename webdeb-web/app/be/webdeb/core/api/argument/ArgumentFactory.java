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

package be.webdeb.core.api.argument;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.exception.FormatException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an abstract factory to handle arguments.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ArgumentFactory extends ContributionFactory {

  /**
   * Retrieve an argument by its id
   *
   * @param id a Contribution id
   * @return an argument if given id is an argument, null otherwise
   */
  Argument retrieve(Long id);

  /**
   * Retrieve an argument by its id and increment visualization hit of this contribution
   *
   * @param id an argument id
   * @return the argument concrete object corresponding to the given id, null if no found
   */
  Argument retrieveWithHit(Long id);

  /**
   * Retrieve a shaded argument by its id
   *
   * @param id a Contribution id
   * @return a shaded argument if given id is a shaded argument, null otherwise
   */
  ArgumentShaded retrieveShaded(Long id);

  /**
   * Retrieve a shaded argument by its id and increment visualization hit of this contribution
   *
   * @param id an argument id
   * @return a shaded argument if given id is a shaded argument, null otherwise
   */
  ArgumentShaded retrieveShadedWithHit(Long id);

  /**
   * Retrieve an argument dictionary by its id
   *
   * @param id a Contribution id
   * @return an argument dictionary if given id is an argument dictionary, null otherwise
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
   * Construct an empty argument instance
   *
   * @return a new Argument instance
   */
  Argument getArgument();

  /**
   * Construct an empty shaded argument instance
   *
   * @return a new ArgumentShaded instance
   */
  ArgumentShaded getArgumentShaded();

  /**
   * Construct an empty argument dictionary instance
   *
   * @return a new  ArgumentDictionary instance
   */
  ArgumentDictionary getArgumentDictionary();

  /**
   * Construct an empty argument justification link instance
   *
   * @return a new ArgumentJustification instance
   */
  ArgumentJustification getArgumentJustificationLink();

  /**
   * Construct an empty argument similarity link instance
   *
   * @return a new ArgumentSimilarity instance
   */
  ArgumentSimilarity getArgumentSimilarityLink();

  /**
   * Construct an ArgumentType with a type and singular flag
   *
   * @param type the argument type id
   * @param i18names a map of pairs of the form (2-char iso-code, type name)
   * @return an ArgumentType instance
   */
  ArgumentType createArgumentType(Integer type, Map<String, String> i18names);

  /**
   * Get an ArgumentType object from a given type id.
   *
   * @param type a type id
   * @return the Argument type corresponding to the given shadet
   *
   * @throws FormatException if given id is invalid
   */
  ArgumentType getArgumentType(int type) throws FormatException;

  /**
   * Retrieve all argument types
   *
   * @return the list of all argument types
   */
  List<ArgumentType> getArgumentTypes();

  /**
   * Construct an ArgumentShade with a shade and singular flag
   *
   * @param shade the argument shade id
   * @param i18names a map of pairs of the form (2-char iso-code, shade name)
   * @return an ArgumentShade instance
   */
  ArgumentShade createArgumentShade(Integer shade, Map<String, String> i18names);

  /**
   * Get an ArgumentShade object from a given shade id.
   *
   * @param shade a shade id
   * @return the Argument shade corresponding to the given shade
   *
   * @throws FormatException if given id is invalid
   */
  ArgumentShade getArgumentShade(int shade) throws FormatException;

  /**
   * Retrieve all argument shades
   *
   * @return the list of all argument shades
   */
  List<ArgumentShade> getArgumentShades();

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
   * Find unique argument justification by its context id, sub context id, tag category id, super argument id, argument id and shade id.
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
