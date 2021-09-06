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

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.exception.FormatException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This interface represents a tag in the webdeb system. A tag consists
 * to sort arguments and texts that share the same theme or category.
 * A tag has commonly parents and children that form a tree hierarchy structure.
 *
 * @author Martin Rouffiange
 */
public interface Project extends BaseProject {

    /**
     * Get the begin date of the project. Date must be dd/mm/yyyy full date (with a "/" as separator)
     *
     * @return the begin date
     */
    Date getBeginDate();

    /**
     * Set the begin date of this project. Date must be dd/mm/yyyy full date (with a "/" as separator)
     * @param date a date
     * @throws FormatException if the given date has an invalid format
     */
    void setBeginDate(Date date) throws FormatException;

    /**
     * Get the end date of the project. Date must be dd/mm/yyyy full date (with a "/" as separator)
     *
     * @return the end date
     */
    Date getEndDate();

    /**
     * Set the end date of the project. Date must be dd/mm/yyyy full date (with a "/" as separator)
     *
     * @param date a date
     * @throws FormatException if the given date has an invalid format
     */
    void setEndDate(Date date) throws FormatException;

    /**
     * Get the flag that says if the project has already started and is still in progress
     *
     * @return true if the project has already started and is still in progress
     */
    boolean isInProgress();

    /**
     * Check if this project has tmp contributors
     *
     * @return true if it is the case
     */
    boolean hasTmpContributors();

    /**
     * Get the project groups that are owned by this project
     *
     * @return a possibly empty list of project groups
     */
    List<ProjectGroup> getGroups();

    /**
     * Set the project groups that are owned by this project
     *
     * @param groups a list of project groups
     */
    void setGroups(List<ProjectGroup> groups);

    /**
     * Get the report about contribution in this project by contribution type and contributor group (if any linked with subgroups)
     *
     * @param fromDate contribution created or updated from date
     * @param toDate contribution created or updated to date
     * @return a map of group, contribution type and number of contribution of this type for this group.
     */
     Map<Group, Map<Integer, Long>> getProjectContributionReportByContributorGroup(Date fromDate, Date toDate);

    /**
     * Get the report about contribution in this project by contribution type and project subgroup
     *
     * @param fromDate contribution created or updated from date
     * @param toDate contribution created or updated to date
     * @return a map of group, contribution type and number of contribution of this type for this subgroup.
     */
    Map<ProjectSubgroup, Map<Integer, Long>> getProjectContributionReportByProjectSubgroup(Date fromDate, Date toDate);
}
