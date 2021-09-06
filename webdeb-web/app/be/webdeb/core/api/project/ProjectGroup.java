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

import java.util.List;

/**
 * This interface represents a tag in the webdeb system. A tag consists
 * to sort arguments and texts that share the same theme or category.
 * A tag has commonly parents and children that form a tree hierarchy structure.
 *
 * @author Martin Rouffiange
 */
public interface ProjectGroup extends BaseProject {

    /**
     * Get the project id where firstly add this group
     *
     * @return a project id
     */
    Integer getProjectId();

    /**
     * Set the project id where firstly add this group
     *
     * @param projectId a project id
     */
    void setProjectId(Integer projectId);

    /**
     * Get the project who owns this group
     *
     * @return a project
     */
    Project getProject();

    /**
     * Set the project who owns this group
     *
     * @param project a project
     */
    void setProject(Project project);

    /**
     * Get the list of project subgroups linked to this group project
     *
     * @return a possibly empty list of project subgroupss
     */
    List<ProjectSubgroup> getSubgroups();

    /**
     * Set the list of project subgroups linked to this group project
     *
     * @param subgroups a list of project subgroups
     */
    void setSubgroups(List<ProjectSubgroup> subgroups);

}
