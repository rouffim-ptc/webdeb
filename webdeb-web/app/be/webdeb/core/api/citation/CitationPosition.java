/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 * 	
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 * 	
 * This program (WebDeb) is free software: 
 * you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.api.citation;

import be.webdeb.core.api.contribution.link.ContributionLink;
import be.webdeb.core.api.contribution.link.PositionLinkType;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.TagCategory;

/**
 * This interface represents the position of an actor through a citation in a debate and possibly subdebate. In this context,
 * The position is determined by a shade.
 *
 * @author Martin Rouffiange
 */
public interface CitationPosition extends ContributionLink {

    /**
     * Get the citation of this link.
     *
     * @return the citation of this link.
     */
    Citation getCitation();

    /**
     * Set the the citation of this link.
     *
     * @param citation the citation of this link.
     */
    void setCitation(Citation citation);
    
    /**
     * Get the debate of this link.
     *
     * @return the debate of this link.
     */
    Debate getDebate();

    /**
     * Set the the debate of this link.
     *
     * @param debate the debate of this link.
     */
    void setDebate(Debate debate);

    /**
     * Get the debate category (sub debate) id of this link.
     *
     * @return the debate category (sub debate) id of this link.
     */
    Long getSubDebateId();

    /**
     * Set the debate category (sub debate) id of this link.
     *
     * @param subDebateId the debate category (sub debate) id of this link.
     */
    void setSubDebateId(Long subDebateId);

    /**
     * Get the debate category (sub debate) of this link, can be null
     *
     * @return the debate category (sub debate) of the link
     */
    TagCategory getSubDebate();

    /**
     * Set the debate category (sub debate) of this link
     *
     * @param subDebate the debate category (sub debate) of the link
     */
    void setSubDebate(TagCategory subDebate);

    /**
     * Get the link shade type
     *
     * @return the link shade type
     */
    PositionLinkType getLinkType();

    /**
     * Set the link shade type
     *
     * @param type the link shade type
     */
    void setLinkType(PositionLinkType type);

}
