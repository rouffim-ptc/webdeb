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

package be.webdeb.core.api.text;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an abstract factory to handle texts.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface TextFactory extends ContributionFactory {

  /**
   * Retrieve a Text by its id
   *
   * @param id a Contribution id
   * @return a Text if given id is a text, null otherwise
   */
  Text retrieve(Long id);

  /**
   * Retrieve a text by its id and increment visualization hit of this contribution
   *
   * @param id a text id
   * @return the text concrete object corresponding to the given id, null if no found
   */
  Text retrieveWithHit(Long id);

  /**
   * Construct a Text instance
   *
   * @return a new Text instance
   */
  Text getText();

  /**
   * Construct a TextType instance
   *
   * @param type a type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a TextType corresponding to given id
   */
  TextType createTextType(int type, Map<String, String> i18names);

  /**
   * Get a text type by its id
   *
   * @param type a text type id
   * @return the TextType corresponding to the given type id
   */
  TextType getTextType(Integer type);

  /**
   * Retrieve all text types
   *
   * @return the list of all types
   */
  List<TextType> getTextTypes();

  /**
   * Construct a TextSourceType instance
   *
   * @param type a source type id
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return a TextSourceType corresponding to given id
   */
  TextSourceType createTextSourceType(int type, Map<String, String> i18names);

  /**
   * Get a text source type by its id
   *
   * @param type a text source type id
   * @return the TextSourceType corresponding to the given source type id
   */
  TextSourceType getTextSourceType(Integer type);

  /**
   * Retrieve all text source types
   *
   * @return the list of all source types
   */
  List<TextSourceType> getTextSourceTypes();

  /**
   * Find a list of Text by a (partial) title
   *
   * @param title a text title
   * @return a possibly empty list of Text with their title containing the given title
   */
  List<Text> findByTitle(String title);

  /**
   * Find a Text by its external url
   *
   * @param url an url
   * @return a Text or null
   */
  Text findByUrl(String url);

  /**
   * Find a list of Source names by a (partial) source title
   *
   * @param source a source name
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of TextSourceName containing the given title
   */
  List<TextSourceName> findSourceNames(String source, int fromIndex, int toIndex);

  /**
   * Get all copyright free sources
   *
   * @return a list of copyright free sources
   */
  List<TextCopyrightfreeSource> getAllCopyrightfreeSources();

  /**
   * Check if a domain name is copyright free
   *
   * @param source a source name
   * @return true if it is free
   */
  boolean sourceIsCopyrightfree(String source);

  /**
   * Check if a domain name is copyright free
   *
   * @param source a source name
   * @param contributor the contributor id
   * @return true if it is free
   */
  boolean sourceIsCopyrightfree(String source, Long contributor);

  /**
   * Construct a TextCopyrightfreeSource instance
   *
   * @return a new TextCopyrightfreeSource instance
   */
  TextCopyrightfreeSource getTextCopyrightfreeSource();

  /**
   * Save a free copyright source text. If text.getId has been set, update the free sour otherwise create it
   *
   * @param freeSource the free source to save
   * @return the id of the created free copyright source, or -1 if a error occured
   *
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with
   * the persistence layer
   */
  int saveTextCopyrightfreeSource(TextCopyrightfreeSource freeSource) throws PersistenceException;

  /**
   * Remove a free copyright source text.
   *
   * @param idSource the free source to remove
   *
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with
   * the persistence layer
   */
  void removeTextCopyrightfreeSource(int idSource) throws PersistenceException;

  /**
   * Construct a ExternalContribution instance
   *
   * @return a new ExternalContribution instance
   */
  ExternalContribution getExternalContribution();

  /**
   * Get a randomly chose Text
   *
   * @return a Text
   */
  Text random();

  /**
   * Construct a TextSourceName instance
   *
   * @return a new TextSourceName instance
   */
  TextSourceName getTextSourceName();

  /**
   * Get a WordGender instance by its char code
   *
   * @param code gender code
   * @return a WordGender instance corresponding to given code
   *
   * @throws FormatException if given id does not exist
   */
  WordGender getWordGender(String code) throws FormatException;

  /**
   * Create a new WordGender instance
   *
   * @param code char code representing the gender
   * @param i18names a map of pairs of the form (char code, name)
   * @return the created WordGender instance
   */
  WordGender createWordGender(String code, Map<String, String> i18names);

  /**
   * Retrieve all word genders
   *
   * @return the list of all word genders
   */
  List<WordGender> getWordGenders();

  /**
   * Retrieve a text visibility based on given id
   *
   * @param visibility a text visibility id
   * @return the visibility object corresponding to given visibility id
   * @throws FormatException if given id does not exist
   */
  TextVisibility getTextVisibility(int visibility) throws FormatException;

  /**
   * Create a new TextVisibility instance
   *
   * @param visibility a visibility enum value
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the TextVisibility object for given visibility and names
   */
  TextVisibility createTextVisibility(ETextVisibility visibility, Map<String, String> i18names);

  /**
   * Retrieve all text visibilities
   *
   * @return a list of text visibilities
   */
  List<TextVisibility> getTextVisibilities();

}
