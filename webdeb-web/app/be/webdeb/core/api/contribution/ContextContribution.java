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
package be.webdeb.core.api.contribution;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;

/**
 * This interface represents a context contribution in the webdeb system. A context contribution has has
 * some justification links with arguments and citations that create a justification structure.
 *
 * @author Martin Rouffiange
 */
public interface ContextContribution extends TextualContribution  {

    /**
     * Get the super debate id of the context contribution, if any
     *
     * @return the super debate id, if any
     */
    Long getCurrentSuperContextId();

    /**
     * Get the super debate id of the context contribution, if any
     *
     * @return the super debate id, if any
     */
    Long getSuperContextId();

    /**
     * Get the sub debate id of the context contribution, if any
     *
     * @return the sub debate id, if any
     */
    Long getSubContextId();

    /**
     * Get the list of tag debates of the context contribution (only for debate multiple), if any
     *
     * @param contributor a contributor id
     * @param group a group id
     * @return the possibly empty list of tag debates
     */
    List<DebateTag> getTagDebates(Long contributor, int group);

    /**
     * Get the tag debates of the context contribution by given id (only for debate multiple), if any
     *
     * @param id the tag debate id
     * @return the corresponding tag debate, or null
     */
    DebateTag getTagDebate(Long id);

    /**
     * Get the list of categories of the context contribution, if any
     *
     * @return the possibly empty list of context contribution categories
     */
    List<TagCategory> getCategories();

    /**
     * Get the list of context has categories of the context contribution, if any
     *
     * @return the possibly empty list of context has categories for the context contribution
     */
    List<ContextHasCategory> getContextCategories();

    /**
     * Get the list of arguments in this context contribution
     *
     * @return a possibly empty list of arguments
     */
    List<Argument> getAllArguments();

    /**
     * Get the list of all citations in this context contribution
     *
     * @return a possibly empty list of citations
     */
    List<Citation> getAllCitations();

    /**
     * Get the list of all citations in this context contribution
     *
     * @param query the query used for retrieve citations
     * @return a possibly empty list of citations
     */
    List<Citation> getAllCitations(SearchContainer query);

    /**
     * Get the list of citations in this context contribution from a justification link
     *
     * @return a possibly empty list of citations
     */
    List<Citation> getCitationsFromJustifications();

    /**
     * Get the list of citations in this context contribution and in a specific category from a justification link
     *
     * @param category a context contribution category id
     * @return a possibly empty list of citations
     */
    List<Citation> getCitationsFromJustifications(Long category);

    /**
     * Get the list of texts where come the citations in this context contribution
     *
     * @param contributor the contributor id
     * @param group the current group id
     * @return a possibly empty list of texts
     */
    List<Text> getTextsCitations(Long contributor, int group);

    /**
     * Get the list of citation links in this context where the given actor is the author
     *
     * @param actor an actor id
     * @return a possibly empty list of citation  links
     */
    List<CitationJustification> getActorCitationJustifications(Long actor);

    /**
     * Get the list of citation links in this context that come from given text
     *
     * @param text a text id
     * @return a possibly empty list of citation  links
     */
    List<CitationJustification> getTextCitationJustifications(Long text);

    /**
     * Get the list of citation links in this context that come from given text
     *
     * @param text a text id
     * @return a possibly empty list of citation  links
     */
    List<CitationPosition> getTextCitationPositions(Long text);

    /**
     * Get the list of all argument justification links that are in this context contribution
     *
     * @return a possibly empty list of argument justification links
     */
    List<ArgumentJustification> getAllArgumentJustificationLinks();

    /**
     * Get the list of all citation justification links that are in this context contribution
     *
     * @return a possibly empty list of citation justification links
     */
    List<CitationJustification> getAllCitationJustificationLinks();

    /**
     * Get the list of all argument justification links that are in this context contribution and that are linked
     * with given category id and super argument id
     *
     * @param category a context contribution category id
     * @param superArgument an argument id
     * @param shade the link shade id
     * @return a possibly empty list of argument justification links
     */
    List<ArgumentJustification> getArgumentJustificationLinks(Long category, Long superArgument, Integer shade);

    /**
     * Get the list of all citation justification links that are in this context contribution and that are linked
     * with given category id and super argument id
     *
     * @param category a context contribution category id
     * @param superArgument an argument id
     * @param shade the link shade id
     * @return a possibly empty list of citation justification links
     */
    List<CitationJustification> getCitationJustificationLinks(Long category, Long superArgument, Integer shade);

}
