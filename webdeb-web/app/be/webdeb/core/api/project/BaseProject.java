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

package be.webdeb.core.api.project;

import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;

/**
 * This interface represents a contribution in the webdeb system. It holds only the common properties of all
 * types of contributions
 *
 * @author Martin Rouffiange
 */
public interface BaseProject extends Comparable<BaseProject> {

    /**
     * Get the project unique id
     *
     * @return the project id, or -1 if unset
     */
    Integer getId();

    /**
     * Set the project unique id
     *
     * @param id the project id to set
     */
    void setId(Integer id);

    /**
     * Get the project name
     *
     * @return the project name
     */
    String getName();

    /**
     * Get the project name
     *
     * @param name the project name
     */
    void setName(String name);

    /**
     * Get the project technical name
     *
     * @return the project technical name
     */
    String getTechnicalName();

    /**
     * Get the project technical name
     *
     * @param name the project technical name
     */
    void setTechnicalName(String name);

    /**
     * Save this Project, if this project has an id (this.getId() != -1) this id is considered as valid and this
     * project is updated, otherwise a new project persisted.
     *
     * A save action is always preceded by a call to isContributorValid() method. Any error will be wrapped as the
     * message of the exception thrown. As a side effect, the id of this Project is updated, if needed.
     *
     * @throws FormatException if any contribution field is not valid or missing
     * @throws PersistenceException if the save action(s) could not been performed because of an issue with
     * the persistence layer
     */
    void save() throws FormatException, PersistenceException;

    /**
     * Remove this project from the database
     *
     * @throws PersistenceException if this.getId() does not exist in repository of does not correspond
     * to this.getType()
     */
    void remove() throws PersistenceException;

    /**
     * Check if this project contains all needed values to be considered as valid. Semantic validation is
     * dependant on the subtype semantics.
     *
     * @return a List of field names in error if any, an empty list if this Project may be considered as valid
     */
    List<String> isValid();


}