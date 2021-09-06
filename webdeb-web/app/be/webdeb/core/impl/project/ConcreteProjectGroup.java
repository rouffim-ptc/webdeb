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

import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;

import java.util.List;

/**
 * This class implements a ProjectGroup.
 *
 * @author Martin Rouffiange
 */
class ConcreteProjectGroup extends AbstractBaseProject implements ProjectGroup {

    private Integer projectId;
    private Project project = null;
    private List<ProjectSubgroup> subgroups = null;

    /**
     * Create a ProjectGroup instance
     *
     * @param factory the project factory
     * @param accessor the project accessor
     */
    ConcreteProjectGroup(ProjectFactory factory, ProjectAccessor accessor) {
        super(factory, accessor);
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public List<ProjectSubgroup> getSubgroups() {
        if(subgroups == null){
            subgroups = factory.getSubgroupsLinkedToGroup(id);
        }
        return subgroups;
    }

    @Override
    public void setSubgroups(List<ProjectSubgroup> subgroups) {
        this.subgroups = subgroups;
    }

    @Override
    public Integer getProjectId() {
        return projectId;
    }

    @Override
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public void save() throws FormatException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("project group contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.PROJECT_GROUP_ERROR, String.join(",", isValid.toString()));
        }
        accessor.save(this);
    }

    @Override
    public void remove() throws PersistenceException {

    }
}
