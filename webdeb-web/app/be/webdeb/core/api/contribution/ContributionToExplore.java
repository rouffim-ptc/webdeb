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

package be.webdeb.core.api.contribution;

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.Map;

/**
 * This interface represents a contribution to explore in a specific group. It's a Contribution put forward by a
 *
 * @author Martin Rouffiange
 */
public interface ContributionToExplore {

    /**
     * Get the contribution to explore id
     *
     * @return the contribution to explore id
     */
    Long getContributionToExploreId();

    /**
     * Set the contribution to explore id
     *
     * @param contributionToExploreId the contribution to explore id
     */
    void setContributionToExploreId(Long contributionToExploreId);

    /**
     * Get the contribution id that need to be explored
     *
     * @return a Contribution id
     */
    Long getContributionId();

    /**
     * Set the contribution id that need to be explored
     *
     * @param contributionId a Contribution id
     */
    void setContributionId(Long contributionId);

    /**
     * Get the contribution that need to be explored
     *
     * @return a Contribution
     */
    Contribution getContribution();

    /**
     * Set the contribution that need to be explored
     *
     * @param contribution a Contribution
     */
    void setContribution(Contribution contribution);

    /**
     * Get the group id where put forward the contribution
     *
     * @return a Contributor group id
     */
    Integer getGroupId();

    /**
     * Set the group id where put forward the contribution
     *
     * @param groupId a Contributor group id
     */
    void setGroupId(Integer groupId);

    /**
     * Get the group where put forward the contribution
     *
     * @return a Contributor group
     */
    Group getGroup();

    /**
     * Set the group where put forward the contribution
     *
     * @param group a Contributor group
     */
    void setGroup(Group group);

    /**
     * Get the order of the contribution
     *
     * @return an order number
     */
    int getOrder();

    /**
     * Set the order ob the contribution
     *
     * @param order the order of the contribution
     */
    void setOrder(int order);

    /**
     * Persist this ContributionToExplore into database
     *
     * @param admin the id of the group admin requesting the save action
     * @throws PermissionException if given admin has insufficient rights
     * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
     * message key and a root cause)
     */
    void save(Long admin) throws PersistenceException, PermissionException;
}
