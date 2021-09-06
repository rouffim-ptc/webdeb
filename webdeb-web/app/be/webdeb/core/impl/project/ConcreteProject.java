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

package be.webdeb.core.impl.project;

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class implements a Project.
 *
 * @author Martin Rouffiange
 */
class ConcreteProject extends AbstractBaseProject implements Project {

    private Date beginDate;
    private Date endDate;
    private List<ProjectGroup> groups = null;

    /**
     * Create a Project instance
     *
     * @param factory the project factory
     * @param accessor the project accessor
     */
    ConcreteProject(ProjectFactory factory, ProjectAccessor accessor) {
        super(factory, accessor);
    }

    @Override
    public Date getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(Date date) throws FormatException {
        if (date == null || (endDate != null && date.after(endDate))) {
            throw new FormatException(FormatException.Key.PROJECT_ERROR, "begin date is after end date " + date);
        }

        this.beginDate = date;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(Date date) throws FormatException {
        if (date == null || (beginDate != null && date.before(beginDate))) {
            throw new FormatException(FormatException.Key.PROJECT_ERROR, "end date is before begin date " + endDate);
        }

        this.endDate = date;
    }

    @Override
    public boolean isInProgress() {
        Date currentDate = new Date();
        return currentDate.after(beginDate) && currentDate.before(endDate);
    }

    @Override
    public boolean hasTmpContributors() {
        return accessor.hasTmpContributors(id);
    }

    @Override
    public List<ProjectGroup> getGroups() {
        if(groups == null){
            groups = factory.getGroupsLinkedToProject(id);
        }
        return groups;
    }

    @Override
    public void setGroups(List<ProjectGroup> groups) {
        this.groups = groups;
    }

    @Override
    public void save() throws FormatException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("project contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.PROJECT_ERROR, String.join(",", isValid.toString()));
        }
        accessor.save(this);
    }

    @Override
    public void remove() throws PersistenceException {

    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();
        if (beginDate.after(endDate)) {
            fieldsInError.add("project begin date is after end date");
        }
        return fieldsInError;
    }

    @Override
    public Map<Group, Map<Integer, Long>> getProjectContributionReportByContributorGroup(Date fromDate, Date toDate) {
        return accessor.getProjectContributionReportByContributorGroup(id, fromDate, toDate);
    }

    @Override
    public Map<ProjectSubgroup, Map<Integer, Long>> getProjectContributionReportByProjectSubgroup(Date fromDate, Date toDate) {
        return accessor.getProjectContributionReportByProjectSubgroup(id, fromDate, toDate);
    }
}
