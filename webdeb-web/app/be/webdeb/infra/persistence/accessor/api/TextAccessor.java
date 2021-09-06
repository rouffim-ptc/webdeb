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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.PartialContributions;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.text.*;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an accessor to text persisted into the database
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface TextAccessor extends ContributionAccessor {

  /**
   * Retrieve a text by its id
   *
   * @param id a Text id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the text concrete object corresponding to the given id, null if not found
   */
  Text retrieve(Long id, boolean hit);

  /**
   * Save a text on behalf of a given contributor If text.getId has been set, update the
   * text, otherwise create text and update contribution id.
   * <p>
   * All passed contribution (affiliation ids (aha) and tags) are also considered as valid.If an contribution has no id,
   * the contribution is considered as non-existing and created. This contribution is then returned.
   *
   * @param contribution a contribution text to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   * @return a map of Contribution type and possibly empty list of Contribution (Actors or Tags) created automatically
   * with this save action (new contributions)
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(Text contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Retrieve the content of a text
   *
   * @param filename a Text filename
   * @return the content of the given text, or an empty string if either given filename does not exist or has no content
   */
  String getContributionTextFile(String filename);

  /**
   * Retrieve the content of a text
   *
   * @param filename a Text filename
   * @param maxSize max amount of characters to retrieve
   * @return the content of the given text, or null if either given filename does not exist or has no content
   */
  String getContributionTextFile(String filename, int maxSize);

  /**
   * Retrieve all bound citations for a given Text id
   *
   * @param id a Text id
   * @return a possibly empty list of Citations bound to given text, i.e. extracted from given Text.
   */
  List<Citation> getCitations(Long id);

  /**
   * Retrieve the list of citations coming from given Text limited by index and with filters
   *
   * @param query all the elements needed to perform the search
   * @return a possibly empty list of Citation that have been defined from given Text
   */
  List<Citation> getTextCitations(SearchContainer query);

  /**
   * Retrieve citations for a given Text id limited by indexes
   *
   * @param id a Text id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of Citations bound to given text, i.e. extracted from given Text.
   */
  PartialContributions<Citation> getCitations(Long id, int fromIndex, int toIndex);

  /**
   * Find a list of Text by a (partial) title
   *
   * @param title a text title
   * @return a possibly empty list of Text with their title containing the given title
   */
  List<Text> findByTitle(String title);

  /**
   * Find a Text by an url
   *
   * @param url the external url
   * @return a  Text, or null
   */
  Text findByUrl(String url);

  /**
   * Find a list of TextSourceNames by a (partial) source name
   *
   * @param source a text source name
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
   * @param contributor the contributor id or null if not needed
   * @return true if it is free
   */
  boolean sourceIsCopyrightfree(String source, Long contributor);

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
   * Retrieve all word genders
   *
   * @return a list of word genders
   */
  List<WordGender> getWordGenders();

  /**
   * Retrieve all text types
   *
   * @return a list of text types
   */
  List<TextType> getTextTypes();

  /**
   * Retrieve all text source types
   *
   * @return a list of text source types
   */
  List<TextSourceType> getTextSourceTypes();

  /**
   * Retrieve all text visibilities
   *
   * @return a list of text visibilities
   */
  List<TextVisibility> getTextVisibilities();

  /**
   * Get a randomly chose Text
   *
   * @return a Text
   */
  Text random();
}
