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

import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contribution.TextualContribution;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.text.Text;

import java.util.List;

/**
 * This interface represents a citation in the webdeb system (formerly Citation, splitted with Argument in 1.0).
 * A citation is taken from a Text (the original excerpt).
 * And the contributor can add some information to contextualized the citation (the working excerpt).
 *
 * @author Martin Rouffiange
 */
public interface Citation extends TextualContribution {

    /**
     * Get the original excerpt from the Text content to which this citation belongs to
     *
     * @return the original excerpt
     */
    String getOriginalExcerpt();

    /**
     * Set the original excerpt from the Text content ot which this citation belongs to.
     *
     * @param originalExcerpt the original Text excerpt
     */
    void setOriginalExcerpt(String originalExcerpt);

    /**
     * Get the excerpt with cut parts and added data by contributors (the contributor contextualizes the excerpt).
     *
     * @return the contextualized excerpt
     */
    String getWorkingExcerpt();

    /**
     * Set the excerpt with cut parts and added data by contributors.
     *
     * @param workingExcerpt contextualized excerpt
     */
    void setWorkingExcerpt(String workingExcerpt);

    /**
     * Get the citation language
     *
     * @return a Language object
     *
     * @see Language
     */
    Language getLanguage();

    /**
     * Set the citation language
     *
     * @param language a Language object
     * @see Language
     */
    void setLanguage(Language language);

    /**
     * Get the Text id from which this Citation belongs to
     *
     * @return the owning Text id
     */
    Long getTextId();

    /**
     * Set the Text id from which this Citation belongs to
     *
     * @param textId a Text id
     */
    void setTextId(Long textId);

    /**
     * Get the Text object bound to this Citation, i.e. ContributionAccessor.retrieve(getTextId())
     *
     * @return the Text object with getTextId if found, null otherwise
     */
    Text getText();

    /**
     * Get the external citation id where this citation comes from, if any
     *
     * @return the external citation id, may be null
     */
    Long getExternalCitationId();

    /**
     * Set the external citation id where this citation comes from
     *
     * @param externalCitationId an external citation id
     */
    void setExternalCitationId(Long externalCitationId);

    /**
     * Get the link id where the citation comes from, if needed
     *
     * @return the link id
     */
    Long getLinkId();

    /**
     * Set the link id where the citation comes from
     *
     * @param linkId the link id
     */
    void setLinkId(Long linkId);

    /**
     * Find a random debate linked to given citation
     *
     * @param citation the citation id
     * @param contributor the contributor id
     * @param group the group id
     * @param rejectedDebate a debate id to not find
     * @return a debate linked to given contribution
     */
    Debate findLinkedDebate(Long contributor, int group, Long rejectedDebate);

    /**
     * Find a list debates linked to given citation
     *
     * @param citation the citation id
     * @param contributor the contributor id
     * @param group the group id
     * @param rejectedDebate a debate id to not find
     * @return a possibly empty debates linked to given contribution
     */
    List<Debate> findLinkedDebates(Long contributor, int group, Long rejectedDebate);
}
