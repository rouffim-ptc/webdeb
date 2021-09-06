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

import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface represents an accessor for excerpts persisted into the database
 *
 * @author Martin Rouffiange
 */
public interface CitationAccessor extends ContributionAccessor  {

    /**
     * Retrieve an Citation by its id
     *
     * @param id an citation id
     * @param hit true if this retrieval must be counted as a visualization
     * @return the citation concrete object corresponding to the given id, null if not found
     */
    Citation retrieve(Long id, boolean hit);

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
     * Save an citation on behalf of a given contributor If citation.getId has been set, update the
     * citation, otherwise create argument and update contribution id.
     *
     * All passed contribution (affiliation ids (aha) and folders) are also considered as valid.If an contribution has no id,
     * the contribution is considered as non-existing and created. This contribution is then returned.
     *
     * @param contribution a contribution citation to save
     * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
     * @param contributor the contributor id that asked to save the contribution
     * @return a map of Contribution type and a possibly empty list of Contributions (Actors or Folders) created automatically with this
     * save action (new contributions)
     *
     * @throws PermissionException if given contributor may not publish in current group or given contribution may not
     * be published in current group, or given contribution does not belong to current group
     * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
     * an existing contribution (id set). The exception message will contain a more complete description of the
     * error.
     */
    Map<Integer, List<Contribution>> save(Citation contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

    /**
     * Save a citation justification link. Context contribution, tag category, super argument and the citation provided 
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
    void save(CitationJustification link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;


    /**
     * Save a citation position link. Debate, subDebate, and the citation provided in given link must exist, as
     * well as the contributor.
     *
     * @param link the link to save
     * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
     * @param contributor the contributor id that asked to save the contribution
     *
     * @throws PermissionException if given contributor may not publish in current group or given contribution may not
     * be published in current group, or given contribution does not belong to current group
     * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
     */
    void save(CitationPosition link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;
    
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
     * Find a random debate linked to given citation
     *
     * @param citation the citation id
     * @param contributor the contributor id
     * @param group the group id
     * @param rejectedDebate a debate id to not find
     * @return a debate linked to given contribution
     */
    Debate findLinkedDebate(Long citation, Long contributor, int group, Long rejectedDebate);

    /**
     * Find a list debates linked to given citation
     *
     * @param citation the citation id
     * @param contributor the contributor id
     * @param group the group id
     * @param rejectedDebate a debate id to not find
     * @return a possibly empty debates linked to given contribution
     */
    List<Debate> findLinkedDebates(Long citation, Long contributor, int group, Long rejectedDebate);

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
