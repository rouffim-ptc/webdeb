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

import be.webdeb.core.api.tag.Tag;

import java.util.List;

/**
 * This interface represents a tag debate. That kind of debate is automatically created with tag, and the citations
 * are taken if they are tagged with thi tag. A tag debate may have sub-debates if some citations are tagged with two tags.
 * It can be part of biggest debates (multiple debate).
 *
 * @author Martin Rouffiange
 */
public interface DebateTag extends Debate {

    /**
     * Get the tag id of the tag that titled this debate
     *
     * @return a tag id
     */
    Long getTagId();

    /**
     * Set the tag id of the tag that titled this debate
     *
     * @param id a tag id
     */
    void setTagId(Long id);

    /**
     * Get the tag that titled the debate
     *
     * @return the tag that titled the debate
     */
    Tag getTag();

    /**
     * Get the link id, if this debate tag comes from a debate multiple
     *
     * @return the debate link id
     */
    Long getLinkId();

    /**
     * Set the link id, if this debate tag comes from a debate multiple
     *
     * @param link the debate link id
     */
    void setLinkId(Long link);

    /**
     * Get the current super debate
     *
     * @return a possibly null value, if tag debate not comes from a multiple debate
     */
    Debate getCurrentSuperDebate();

    /**
     * Set the current super debate
     *
     * @param debate a multiple debate where this tag debate comes from
     */
    void setCurrentSuperDebate(Debate debate);

    /**
     * Get the list of biggest debates where this debate is part of
     *
     * @return a possibly empty list of super debates
     */
    List<Debate> getSuperDebates();
}
