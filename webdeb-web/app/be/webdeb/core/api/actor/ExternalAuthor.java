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
package be.webdeb.core.api.actor;

/**
 * This interface represents a external author.
 * It's a temporary contribution that is keep for an easiest communication between services.
 *
 * @author Martin Rouffiange
 */

public interface ExternalAuthor {

    /**
     * Get the author webdeb id
     *
     * @return the actor id corresponding if any
     */
    Long getId();

    /**
     * Set the author webdeb id
     *
     * @param id an actor id
     */
    void setId(Long id);

    /**
     * Get the author name
     *
     * @return the actor name
     */
    String getName();

    /**
     * Set the author name
     *
     * @param name an author name
     */
    void setName(String name);
}
