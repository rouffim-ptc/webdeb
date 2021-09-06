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

package be.webdeb.core.api.citation;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;

/**
 * This interface represents an abstract factory to handle citations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface CitationFactory extends ContributionFactory {

    /**
     * Retrieve an citation by its id
     *
     * @param id a Contribution id
     * @return an citation if given id is a citation, null otherwise
     */
    Citation retrieve(Long id);

    /**
     * Retrieve an citation by its id and increment visualization hit of this contribution
     *
     * @param id an citation id
     * @return the citation concrete object corresponding to the given id, null if no found
     */
    Citation retrieveWithHit(Long id);

    /**
     * Retrieve a citation justification link by its id
     *
     * @param id a Contribution id
     * @return a CitationJustification if given id is a citation justification link, null otherwise
     */
    CitationJustification retrieveJustificationLink(Long id);

    /**
     * Retrieve a citation position link by its id
     *
     * @param id a Contribution id
     * @return a CitationPosition if given id is a citation position link, null otherwise
     */
    CitationPosition retrievePositionLink(Long id);

    /**
     * Construct an empty citation instance
     *
     * @return a new citation instance
     */
    Citation getCitation();

    /**
     * Construct an empty citation justification link instance
     *
     * @return a new CitationJustification instance
     */
    CitationJustification getCitationJustificationLink();

    /**
     * Construct an empty citation position link instance
     *
     * @return a new CitationPosition instance
     */
    CitationPosition getCitationPositionLink();

    /**
     * Search for citations
     *
     * @param query all the elements needed to perform the search
     * @return a list of Citations corresponding to the browse
     */
    List<Citation> searchCitationByType(SearchContainer query);

    /**
     * Find a list of citation justification links for given context, sub context, category, superCitation and shade
     *
     * @param context the context contribution id
     * @param subContext the tag sub context id
     * @param category the tag category id
     * @param superArgument the super argument of the link id
     * @param shade the link shade id
     * @return a possibly empty list of citation justification links
     */
    List<CitationJustification> findCitationLinks(Long context, Long subContext, Long category, Long superArgument, int shade);

    /**
     * Check if a citation position link exists.
     *
     * @param debateId        a debate of this position link
     * @param subDebateId     a tag category id
     * @param citationId      a citation id
     * @param shade           a position link shade
     * @return true if such a link already exists
     */
    boolean citationPositionLinkAlreadyExists(Long debateId, Long subDebateId, Long citationId, int shade);
    
    /**
     * Get a randomly chose Citation
     *
     * @return a Citation
     */
    Citation random();

    /**
     * Get a citation suggestion for the given contribution. This citation could be linked to given contribution, or
     * can be a new citation.
     *
     * @param idContribution the related contribution for which we need a suggstion
     * @return a Citation
     */
    Citation getSuggestionCitation(Long idContribution);

}