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

package be.webdeb.core.api.debate;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.link.ContextHasSubDebate;
import be.webdeb.core.exception.FormatException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an abstract factory to handle debates.
 *
 * @author Martin Rouffiange
 */
public interface DebateFactory extends ContributionFactory {

    /**
     * Retrieve a debate by its id
     *
     * @param id a Contribution id
     * @return a debate if given id is a debate, null otherwise
     */
    Debate retrieve(Long id);

    /**
     * Retrieve a debate by its id and increment visualization hit of this contribution
     *
     * @param id a debate id
     * @return the debate concrete object corresponding to the given id, null if no found
     */
    Debate retrieveWithHit(Long id);

    /**
     * Retrieve a debate similarity link by its id
     *
     * @param id a Contribution id
     * @return a DebateSimilarity if given id is a debate similarity link, null otherwise
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
     * Construct an empty simple debate instance
     *
     * @return a new DebateSimple instance
     */
    DebateSimple getDebateSimple();

    /**
     * Construct an empty debate that comes from a tag instance
     *
     * @return a new DebateTag instance
     */
    DebateTag getDebateTag();

    /**
     * Construct an empty debate external url instance
     *
     * @return a new DebateExternalUrl instance
     */
    DebateExternalUrl getDebateExternalUrl();

    /**
     * Construct a debate external url instance with given attributes
     *
     * @param id the id of the instance
     * @param url the url of the instance
     * @param alias the url alias of the instance
     * @return a new DebateExternalUrl instance
     */
    DebateExternalUrl getDebateExternalUrl(Long id, String url, String alias);

    /**
     * Construct an empty debate similarity link instance
     *
     * @return a new DebateSimilarity instance
     */
    DebateSimilarity getDebateSimilarityLink();

    /**
     * Construct an empty debate has tag debate instance
     *
     * @return a new DebateHasTagDebate instance
     */
    DebateHasTagDebate getDebateHasTagDebate();

    /**
     * Construct an empty debate has text instance
     *
     * @return a new DebateHasText instance
     */
    DebateHasText getDebateHasText();

    /**
     * Construct an empty context has subdebate instance
     *
     * @return a new context has subdebate instance
     */
    ContextHasSubDebate getContextHasSubDebate();

    /**
     * Construct an DebateShade with a shade and singular flag
     *
     * @param shade the debate shade id
     * @param i18names a map of pairs of the form (2-char iso-code, shade name)
     * @return an DebateShade instance
     */
    DebateShade createDebateShade(int shade, Map<String, String> i18names);

    /**
     * Get an DebateShade object from a given shade id.
     *
     * @param shade a shade id
     * @return the Debate shade corresponding to the given shade
     *
     * @throws FormatException if given id is invalid
     */
    DebateShade getDebateShade(int shade) throws FormatException;

    /**
     * Retrieve all debate shades
     *
     * @return the list of all debate shades
     */
    List<DebateShade> getDebateShades();

    /**
     * Find a list of Debate by title
     *
     * @param title a debate title
     * @param lang a two char iso-639-1 language code
     * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
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
     * Get a randomly chose Debate
     *
     * @return a Debate
     */
    Debate random();
}
