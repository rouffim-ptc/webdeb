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
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.link.ContextHasSubDebate;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an accessor for debates persisted into the database
 *
 * @author Martin Rouffiange
 */
public interface DebateAccessor extends ContributionAccessor {

  /**
   * Retrieve a Debate by its id
   *
   * @param id  a debate id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the debate concrete object corresponding to the given id, null if not found
   */
  Debate retrieve(Long id, boolean hit);

  /**
   * Retrieve a debate similarity link by its id
   *
   * @param id a Contribution id
   * @return an DebateSimilarity if given id is a debate similarity link, null otherwise
   */
  DebateSimilarity retrieveSimilarityLink(Long id);

  /**
   * Retrieve a context ahs subdebate by its id. Invoker must explicitly cast returned value into concrete type to
   * access concrete methods.
   *
   * @param id a context has subdebate link id
   * @return the ContextHasSubDebate concrete object corresponding to the given id, null if not found
   */
  ContextHasSubDebate retrieveContextHasSubDebate(Long id);

  /**
   * Save a debate on behalf of a given contributor If debate.getId has been set, update the
   * debate, otherwise create argument and update contribution id.
   *
   * @param contribution a contribution argument to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor  the contributor id that asked to save the contribution
   * @return a map of Contribution type and a possibly empty list of Contributions (Actors or Folders) created automatically with this
   * save action (new contributions)
   * @throws PermissionException  if given contributor may not publish in current group or given contribution may not
   *                              be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   *                              an existing contribution (id set). The exception message will contain a more complete description of the
   *                              error.
   */
  Map<Integer, List<Contribution>> save(Debate contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save a debate similarity link. Both debates provided in given link must exist, as well as the contributor.
   *
   * @param link         the link to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor  the contributor id that asked to save the contribution
   * @throws PermissionException  if given contributor may not publish in current group or given contribution may not
   *                              be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(DebateSimilarity link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save a debate has text link. Both debate and text provided in given link must exist, as well as the contributor.
   *
   * @param link         the link to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor  the contributor id that asked to save the contribution
   * @throws PermissionException  if given contributor may not publish in current group or given contribution may not
   *                              be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(DebateHasText link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save a context has subdebate link. Context and debate provided in given link must exist, as well as the contributor.
   *
   * @param link         the link to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor  the contributor id that asked to save the contribution
   * @throws PermissionException  if given contributor may not publish in current group or given contribution may not
   *                              be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(ContextHasSubDebate link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Retrieve all debate shades
   *
   * @return a list of debate shades
   */
  List<DebateShade> getDebateShades();

  /**
   * Check if a debate is similar to another one
   *
   * @param debateToCompare the debate to compare with the other one
   * @param debate          an debate
   * @return true if both debates are similar
   */
  boolean isSimilarWith(Long debateToCompare, Long debate);

  /**
   * Get all external url that are linked to the given debate.
   *
   * @param id the given debate id
   * @return the (possibly empty) list of external url involving the given debate
   */
  List<DebateExternalUrl> getExternalUrls(Long id);

  /**
   * Get all similarity links that are bound to the given debate.
   *
   * @param id the given debate id
   * @return the (possibly empty) list of similarity links involving the given debate
   */
  List<DebateSimilarity> getSimilarityLinks(Long id);

  /**
   * Find a list of Debate by title
   *
   * @param title     an debate title
   * @param lang      a two char iso-639-1 language code
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of Debate with their title containing the given title
   */
  List<Debate> findByTitle(String title, String lang, int fromIndex, int toIndex);

  /**
   * Find a list of Debate by title and debate shade
   *
   * @param title a debate title
   * @param shade a specific debate shade
   * @param lang a two char iso-639-1 language code
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a possibly empty list of Debate with their title containing the given title
   */
  List<Debate> findByTitleAndShade(String title, Integer shade, String lang, int fromIndex, int toIndex);

  /**
   * Get the list of citations in this context contribution from a position link
   *
   * @param id  a debate id
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromPositions(Long id);

  /**
   * Get the list of citations in this context contribution and in a specific subDebate from a position link
   *
   * @param id  a debate id
   * @param subDebate a subDebate id (tag id)
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromPositions(Long id, Long subDebate);

  /**
   * Get the list of all citation position links that are in this context contribution
   *
   * @param id  a debate id
   * @param subDebate a subDebate id (tag id)
   * @return a possibly empty list of citation position links
   */
  List<CitationPosition> getAllCitationPositionLinks(Long id, Long subDebate);

  /**
   * Get the list of citation links in this context where the given actor is the author
   *
   * @param id  a debate id
   * @param actor an actor id
   * @return a possibly empty list of citation  links
   */
  List<CitationPosition> getActorCitationPositions(Long id, Long actor);

  /**
   * Find the list of linked text with Debate Has Text
   *
   * @param id  a debate id
   * @return a possibly empty list of texts
   */
  List<Text> findLinkedTexts(Long id);

  /**
   * Get a randomly chose Debate
   *
   * @return a Debate
   */
  Debate random();
}
