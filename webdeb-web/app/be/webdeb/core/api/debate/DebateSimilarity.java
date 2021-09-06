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

package be.webdeb.core.api.debate;

import be.webdeb.core.api.contribution.link.SimilarityLink;

/**
 * This interface represents the similarity links between an origin and a destination debate and the shade
 * between them.
 *
 * @author Martin Rouffiange
 */
public interface DebateSimilarity extends SimilarityLink {

    /**
     * Get the origin debate of this similarity link
     *
     * @return the debate being the origin of this similarity link
     */
    Debate getOrigin();

    /**
     * Set the origin debate
     *
     * @param debate a Debate to be bound as the origin of this similarity link
     */
    void setOrigin(Debate debate);

    /**
     * Get the destination debate of this similarity link
     *
     * @return the debate being the destination of this similarity link
     */
    Debate getDestination();

    /**
     * Set the destination Debate
     *
     * @param debate a Debate to be bound as the destination of this similarity link
     */
    void setDestination(Debate debate);

}
