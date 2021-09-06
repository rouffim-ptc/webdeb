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

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

public class ConcreteContributionToExplore implements ContributionToExplore {

    private ContributorAccessor accessor;

    private Long contributionToExploreId;

    private Long contributionId;
    private Contribution contribution;

    private Integer groupId;
    private Group group;

    private int order = 1;

    public ConcreteContributionToExplore(ContributorAccessor accessor){
        this.accessor = accessor;
    }

    @Override
    public Long getContributionToExploreId() {
        return contributionToExploreId;
    }

    @Override
    public void setContributionToExploreId(Long contributionToExploreId) {
        this.contributionToExploreId = contributionToExploreId;
    }

    @Override
    public Long getContributionId() {
        return contributionId;
    }

    @Override
    public void setContributionId(Long contributionId) {
        this.contributionId = contributionId;
    }

    @Override
    public Contribution getContribution() {
        return contribution;
    }

    @Override
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    @Override
    public Integer getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public void setGroup(Group group) {
        this.group = group;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void save(Long admin) throws PersistenceException, PermissionException {
        accessor.save(this, admin);
    }

    @Override
    public String toString() {
        return "ConcreteContributionToExplore{" +
                "contributionToExploreId=" + contributionToExploreId +
                ", contributionId=" + contributionId +
                ", groupId=" + groupId +
                ", order=" + order +
                '}';
    }
}
