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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;

import java.util.List;

/**
 * This interface represents a debate in the webdeb system.
 *
 * A debate may be :
 *  - an unique response debate and be titled by a shaded argument (simple debate)
 *  - multiple possible responses debate and titled by a simple argument (not shaded) and be composed by simple debates.
 *  - tag debate automatically created with tag, and tagged citations
 *
 * A debate has tag categories in its justification links.
 *
 * @author Martin Rouffiange
 */
public interface Debate extends ContextContribution {

    /**
     * Get the debate type
     *
     * @return the type of debate
     */
    EDebateType getEType();

    /**
     * Get the fully readable title with shade if needed
     *
     * @return a fully readable reconstructed title with shade if needed
     */
    String getFullTitle();

    /**
     * Get the fully readable title with shade if needed in the given language if possible
     *
     * @param lang      a two char iso-639-1 language code
     * @return a fully readable reconstructed title with shade if needed in the given language
     */
    String getFullTitle(String lang);

    /**
     * Get the title without shade, if any
     *
     * @param lang      a two char iso-639-1 language code
     * @return the title without shade
     */
    String getTitle(String lang);

    /**
     * Get the contribution id that titled this debate
     *
     * @return a contribution id
     */
    Long getTitleContributionId();

    /**
     * Get the debate text description, if any
     *
     * @return the debate text description, may be null
     */
    String getDescription();

    /**
     * Set the debate text description
     *
     * @param description the debate text description
     */
    void setDescription(String description);

    /**
     * Get the picture extension of this debate, if any
     *
     * @return a contributor picture id
     */
    String getPictureExtension();

    /**
     * Set the picture extension of this debate
     *
     * @param extension a contributor picture extension
     */
    void setPictureExtension(String extension);

    /**
     * Check whether this debate is multiple thesis or not
     *
     * @return true if this debate is multiple thesis or not
     */
    boolean isMultiple();

    /**
     * Set if this debate is multiple thesis or not
     *
     * @param isMultiple true if this debate is multiple thesis or not
     */
    void isMultiple(boolean isMultiple);

    /**
     * Init the debate external urls list
     */
    void initExternalUrls();

    /**
     * Get the list of external urls linked to this debate
     *
     * @return a possibly empty list of external urls
     */
    List<DebateExternalUrl> getExternalUrls();

    /**
     * Add an external url instance to this debate
     *
     * @param url a debate external url
     */
    void addExternalUrl(DebateExternalUrl url);

    /**
     * Get the picture id of this debate, if any
     *
     * @return a contributor picture id
     */
    Long getPictureId();

    /**
     * Set the picture id of this debate
     *
     * @param pictureId a contributor picture id
     */
    void setPictureId(Long pictureId);

    /**
     * Get the picture that portray the debate
     *
     * @return the picture that portray the debate
     */
    ContributorPicture getPicture();

    /**
     * Get the shade that begins the debate as enum type
     *
     * @return the shade that begins the debate
     */
    EDebateShade getEShade();

    /**
     * Get the shade that begins the debate
     *
     * @return the shade that begins the debate
     */
    DebateShade getShade();

    /**
     * Set the shade that begins the debate
     *
     * @param shade the shade that begins the debate
     */
    void setShade(DebateShade shade);

    /**
     * Get the list of similar debate links
     *
     * @return the possibly empty list of similar debate links
     */
    List<DebateSimilarity> getSimilar();

    /**
     * Get the list of citations in this context contribution from a position link
     *
     * @return a possibly empty list of citations
     */
    List<Citation> getCitationsFromPositions();

    /**
     * Get the list of citations in this context contribution and in a specific subDebate from a position link
     *
     * @param subDebate a subDebate id (tag id)
     * @return a possibly empty list of citations
     */
    List<Citation> getCitationsFromPositions(Long subDebate);

    /**
     * Get the list of all citation position links that are in this context contribution
     *
     * @param subDebate a subDebate id (tag id)
     * @return a possibly empty list of citation position links
     */
    List<CitationPosition> getAllCitationPositionLinks(Long subDebate);

    /**
     * Get the list of citation links in this context where the given actor is the author
     *
     * @param actor an actor id
     * @return a possibly empty list of citation  links
     */
    List<CitationPosition> getActorCitationPositions(Long actor);

    /**
     * Get the number of position links related with this debate
     *
     * @return the number of position links
     */
    int getNbPositionLinks();

    /**
     * Get the number of justification links related with this debate
     *
     * @return the number of justification links
     */
    int getNbJustificationLinks();

    /**
     * Set the number of position links related with this debate
     *
     * @param nbPositions the number of position links
     */
    void setNbPositionLinks(Integer nbPositions);

    /**
     * Set the number of justification links related with this debate
     *
     * @param nbJustifications the number of justification links
     */
    void setNbJustificationLinks(Integer nbJustifications);

    /**
     * Find the list of linked text with Debate Has Text
     *
     * @return a possibly empty list of texts
     */
    List<Text> findLinkedTexts();

}
