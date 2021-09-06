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

package be.webdeb.core.api.contributor;

import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.Map;

/**
 * This interface represents an advice to help contributors
 *
 * @author Martin Rouffiange
 */
public interface Advice {

    /**
     * Get the advice id
     *
     * @return an int representing an advice
     */
    int getId();

    /**
     * Set the advice id
     *
     * @param id an int representing an advice
     */
    void setId(int id);

    /**
     * Get the name of this advice (en lang by default)
     *
     * @param lang a two-char ISO-639-1 code representing the language for the name
     * @return a name for this Advice object
     */
    String getName(String lang);

    /**
     * Get the whole map of names in all known languages
     *
     * @return a map of (2-char ISO-639-1 code, map of name in this language) of this advice
     */
    Map<String, String> getNames();

    /**
     * Set the whole map of names in all known languages
     *
     * @param names a map of (2-char ISO-639-1 code, map of name in this language) of this advice
     */
    void setNames(Map<String, String> names);

    /**
     * Add a new title for this advice. If such a title existed, simply overwrite it.
     *
     * @param name a lang-dependent spelling for this advice
     * @param lang a two char ISO 639-1 code
     */
    void addName(String name, String lang);

    /**
     * Persist this Advice into database
     *
     * @param contributor the id of the contributor requesting the save action
     * @throws PermissionException if given contributor has insufficient rights
     * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
     * message key and a root cause)
     */
    void save(Long contributor) throws PersistenceException, PermissionException;
}

