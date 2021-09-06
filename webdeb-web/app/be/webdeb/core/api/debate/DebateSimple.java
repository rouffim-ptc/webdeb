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

import be.webdeb.core.api.argument.ArgumentShaded;

import java.util.List;

/**
 * This interface represents a debate with a unique possible responses. That kind of debate is titled by a shaded
 * argument (not shaded).
 *
 * @author Martin Rouffiange
 */
public interface DebateSimple extends Debate {

    /**
     * Get the shaded argument id of the argument that titled this debate
     *
     * @return an argument shaded id
     */
    Long getArgumentId();

    /**
     * Set the shaded argument id of the argument that titled this debate
     *
     * @param id an argument shaded id
     */
    void setArgumentId(Long id);

    /**
     * Get the shaded argument that titled this debate
     *
     * @return the shaded argument that titled this debate
     */
    ArgumentShaded getArgument();

    /**
     * Set the shaded argument that titled this debate
     *
     * @param argument the shaded argument that titled this debate
     */
    void setArgument(ArgumentShaded argument);

    /**
     * Get the list of sub debates of this multiple debates
     *
     * @param contributor a contributor id
     * @param group a group id
     * @return a possibly empty list of sub tag debates
     */
    List<DebateTag> getSubDebates(Long contributor, int group);

}
