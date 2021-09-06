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

import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface represents a text Contribution in the webdeb system. A text represents (a piece of) text to
 * which excerpts as citations are extracted and specified. A text is also linked to authors or other actors.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

public interface Text extends ContextContribution {

  /**
   * The original title of the text
   *
   * @return the corresponding value
   */
  String getOriginalTitle();

  /**
   * Get the i18 title for given lang key
   *
   * @param lang a two char iso-639-1 language code
   * @return the corresponding value
   */
  String getTitle(String lang);

  /**
   * Get the text titles
   *
   * @return a map of iso-639-1 language code and corresponding values
   */
  Map<String, String> getTitles();

  /**
   * Set the text titles
   *
   * @param i18names a map of iso-639-1 language code and corresponding titles
   */
  void setTitles(Map<String, String> i18names);

  /**
   * Add a new title's spelling for this text. If such a spelling existed, simply overwrite it.
   *
   * @param lang a two char ISO 639-1 code
   * @param name a lang-dependent spelling for the title
   */
  void addTitle(String lang, String name);

  /**
   * Get the text language
   *
   * @return a Language object
   *
   * @see Language
   */
  Language getLanguage();

  /**
   * Set the text language
   *
   * @param language a Language object
   * @see Language
   */
  void setLanguage(Language language);

  /**
   * Get the list of Actor being the text authors
   *
   * @return a map of Actor being the authors of this contribution (with their roles)
   */
  List<ActorRole> getAuthors();

  /**
   * Get the publication date. Date may be
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   * </pre>
   *
   * @return the publication date
   */
  String getPublicationDate();

  /**
   * Set the publication date. Date may be
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   * </pre>
   *
   * @param date a publication date
   * @throws FormatException if the given date has an invalid format
   */
  void setPublicationDate(String date) throws FormatException;

  /**
   * Get the text type
   *
   * @return a TextType object
   *
   * @see TextType
   */
  TextType getTextType();

  /**
   * Set the text type
   *
   * @param type a TextType object
   * @see TextType
   */
  void setTextType(TextType type);

  /**
   * Get the text source type
   *
   * @return a TextSourceType object
   *
   * @see TextSourceType
   */
  TextSourceType getTextSourceType();

  /**
   * Set the text source type
   *
   * @param type a TextSourceType object
   * @see TextSourceType
   */
  void setTextSourceType(TextSourceType type);

  /**
   * Set the URL source
   *
   * @param url a URL to bind to this source
   * @throws FormatException if the URL has a wrong format or this Source does not accept URL or if it is larger
   * than MAX_URL_SIZE
   */
  void setUrl(String url) throws FormatException;

  /**
   * Get the URL associated to this source
   *
   * @return the URL associated to this source, may be null
   */
  String getUrl();

  /**
   * Retrieve the list of citations coming from this Text
   *
   * @return a possibly empty list of Citation that have been defined from this Text
   */
  List<Citation> getTextCitations();

  /**
   * Retrieve the list of citations coming from this Text limited by index and with filters
   *
   * @param query all the elements needed to perform the search
   * @return a possibly empty list of Citation that have been defined from this Text
   */
  List<Citation> getTextCitations(SearchContainer query);

  /**
   * Add an citation to this Text
   *
   * @param citation an citation of this Text
   */
  void addCitation(Citation citation);

  /**
   * Check if this text is hidden for visualization purposes (eg, for tweets).
   * Hidden texts are empty shelves only meant to gather text-related properties for arguments
   *
   * @return true if this text is hidden
   */
  boolean isHidden();

  /**
   * Set whether this text must be hidden for visualization purposes (eg, for tweets)
   * Hidden texts are empty shelves only meant to gather text-related properties for arguments
   *
   * @param hidden true if this text must be hidden
   */
  void isHidden(boolean hidden);

  /**
   * Check whether this text has been automatically fetched from an external source (RSS feed client for example)
   *
   * @return true if this text has been automatically fetched
   */
  boolean isFetched();

  /**
   * Set whether this text has been automatically fetched from an external source (RSS feed client for example)
   *
   * @param fetched true if this text has been automatically fetched
   */
  void isFetched(boolean fetched);

  /**
   * Get the visibility of this text
   *
   * @return a text visibility object
   * @see ETextVisibility
   */
  TextVisibility getTextVisibility();

  /**
   * Set the visibility of this text
   *
   * @param visibility a text visibility object
   * @see ETextVisibility
   */
  void setTextVisibility(TextVisibility visibility);

  /**
   * Check whether given contributor may see content of this text (see {@link #getFilename(Long) filename handling}
   *
   * @param contributor a contributor id
   * @return true if this contributor may see this content, false otherwise
   */
  boolean isContentVisibleFor(Long contributor);

  /**
   * Get the filenames under which this contribution is stored. A text may have multiple contents if its visibility is
   * private, each content being private to a particular contributor.
   *
   * Otherwise, if content is either public or pedagogic, the map will contain only one key (-1L value) and the unique
   * file name (effective visibility is still depending on the pedagogic state of this file's visibility).
   *
   * @return a (possibly empty) map of (contributor ids, file names) under which the contents are stored.
   * @see ETextVisibility
   */
  Map<Long, String> getFilenames();

  /**
   * Set the filenames under which this contribution is stored. A text may have multiple contents if its visibility is
   * private, each content being private to a particular contributor.
   *
   * Otherwise, if content is either public or pedagogic, the map must contain only one key (-1L value) and the unique
   * file name (effective visibility is still depending on the pedagogic state of this file's visibility).
   *
   * @param filenames the map of (contributor ids, file names) under which the contents are stored.
   */
  void setFilenames(Map<Long, String> filenames);

  /**
   * Add a filename for given contributor. Actual filename will depend from this text visibility.
   * <ul>
   *   <li>if this text is public or pedagogic, a mapping to default -1 id will be created</li>
   *   <li>if this text is private, a mapping with given contributor and filename will be created</li>
   * </ul>
   * If this text has no visibility defined, this action has no effect.
   *
   * @param contributor a contributor to whom a filename will be saved for (may be overridden according above rules)
   * @param filename a filename. Note that the actual filename that will be persisted may be modified for implementation reasons
   */
  void addFilename(Long contributor, String filename);

  /**
   * Convenience method to get the filename for given contributor.
   *
   * @param contributor a contributor id
   * @return the filename for given contributor as specified in (see {@link #getFilenames() filename handling}, null if no content is accessible for given
   * contributor
   *
   * @throws PermissionException if given contributor may not see this text because either text is pedagogic and (s)he's not,
   * or text is private and there's no content viewable for him
   */
  String getFilename(Long contributor) throws PermissionException;

  /**
   * Get the text content for a given contributor only if the content is of mime type "plain/text".
   * If this text is public, default -1L contributor may be passed.
   * If the text is pedagogic, the contributor must have his pedagogic flag set to true too,
   * otherwise for private texts, the contributor id is checked to know if a content exists for this contributor.
   *
   * @param contributor a contributor id.
   * @return the content of this Text, as saved under its filename
   *
   * @throws PermissionException if given contributor may not see this text because either text is pedagogic and (s)he's not,
   * or text is private and there's no content viewable for him
   */
  String getTextContent(Long contributor) throws PermissionException;

  /**
   * Set the text content. If no file name has been specified, it will be automatically assigned
   *
   * @param content the content of this Text
   */
  void setTextContent(String content);

  /**
   * Get an excerot for this text (for any accepted format).
   * Any contributor may access to an excerpt as long as this text is not private.
   *
   * @param maxSize max number of characters for this excerpt
   * @return the excerpt of this text content, trimmed to maxSize characters max
   *
   * @throws PermissionException if given contributor may not see this text because text is private and he has no
   * content viewable for him
   */
  String getTextExcerpt(Long contributor, int maxSize) throws PermissionException;

  /**
   * Get the source title where this Text originates from, if any
   *
   * @return the source title, may be null
   */
  String getSourceTitle();

  /**
   * Set the source title
   *
   * @param title a source title, where this Text is originating from
   */
  void setSourceTitle(String title);

  /**
   * Get the external text id where the text comes from, if any
   *
   * @return the external text id, may be null
   */
  Long getExternalTextId();

  /**
   * Set the external text id where the text comes from
   *
   * @param externalTextId an external text id
   */
  void setExternalTextId(Long externalTextId);

  /**
   * Get the number of citations of the text
   *
   * @return the number of citations
   */
  int getNbCitations();

  /**
   * Set the number of citations of the text
   * @param nbCitations the number of citations
   */
  void setNbCitations(int nbCitations);

  Long getLinkId();

  void setLinkId(Long id);

}
