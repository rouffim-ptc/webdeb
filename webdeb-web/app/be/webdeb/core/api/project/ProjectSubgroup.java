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

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;

import java.util.List;

/**
 * This interface represents a tag in the webdeb system. A tag consists
 * to sort arguments and texts that share the same theme or category.
 * A tag has commonly parents and children that form a tree hierarchy structure.
 *
 * @author Martin Rouffiange
 */
public interface ProjectSubgroup extends BaseProject {

    /**
     * Get the number of participants for this subgroup.
     *
     * @return the number of participants
     */
    int getNbContributors();

    /**
     * Set the number of participants for this subgroup.
     *
     * @param nbContributors the number of participants
     */
    void setNbContributors(int nbContributors);

    /**
     * Get the project group id where firstly add this subgroup
     *
     * @return a project group id
     */
    Integer getProjectGroupId();

    /**
     * Set the project group id where firstly add this subgroup
     *
     * @param projectGroupId a project group id
     */
    void setProjectGroupId(Integer projectGroupId);

    /**
     * Get the project group technical name
     *
     * @return a project group technical name
     */
    String getProjectGroupTechnicalName();

    /**
     * Set the project group technical name
     *
     * @param name a project group technical name
     */
    void setProjectGroupTechnicalName(String name);

    /**
     * Get the subgroup generated password (must not be saved in db !)
     *
     * @return the subgroup password
     */
    String getPassword();

    /**
     * Set the subgroup password
     *
     * @param password the subgroup password
     */
    void setPassword(String password);

    /**
     * Get the group who owns this subgroup
     *
     * @return a project group
     */
    ProjectGroup getGroup();

    /**
     * Set the group who owns this subgroup
     *
     * @param group a project group
     */
    void setGroup(ProjectGroup group);

    /**
     * Get the list of contributors linked to this subgroup project
     *
     * @return a possibly empty list of contributors
     */
    List<Contributor> getContributors();

    /**
     * Set the list of contributors linked to this subgroup project
     *
     * @param contributors a list of contributors
     */
    void setContributors(List<Contributor> contributors);

    /**
     * Get the list of contributor groups linked to this subgroup project
     *
     * @return a possibly empty list of contributor groups
     */
    List<Group> getContributorGroups();

    /**
     * Set the list of contributor groups linked to this subgroup project
     *
     * @param groups a list of contributor groups
     */
    void setContributorGroups(List<Group> groups);

}
