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

package be.webdeb.core.api.contributor;

import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;

/**
 * This interface represents a Temporary Contributor of the webdeb system. Temporary contributors are contributor
 * that made to facilitate de inscription process for projects.
 *
 * @author Martin Rouffiange
 */
public interface TmpContributor {

    /**
     * Retrieve the Contributor id
     *
     * @return the Contributor id
     */
    Long getId();

    /**
     * Set the Contributor id
     *
     * @param id the Contributor id to set
     */
    void setId(Long id);

    /**
     * Get this Contributor's pseudonym
     *
     * @return the pseudonym
     */
    String getPseudo();

    /**
     * Set this Contributor's pseudonym
     *
     * @param pseudo the pseudonym
     */
    void setPseudo(String pseudo) throws FormatException;

    /**
     * Get this Contributor's password (hashed)
     *
     * @return the hashed password
     */
    String getPassword();

    /**
     * Set this Contributor's password (hashed, except if this.getId is unset, then password is clear)
     *
     * @param password a hashed password
     */
    void setPassword(String password);

    Project getProject();

    void setProject(Project project);

    ProjectSubgroup getProjectSubgroup();

    void setProjectSubgroup(ProjectSubgroup subgroup);

    Contributor getContributor();
}
