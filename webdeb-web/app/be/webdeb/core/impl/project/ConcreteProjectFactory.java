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

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;
import be.webdeb.util.ValuesHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;

/**
 * This class implements the project factory that gives access to project implementation and manipulate projects,
 * project groups, subgroups and contributor inscription.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteProjectFactory implements ProjectFactory {

    @Inject
    private ProjectAccessor accessor;

    @Inject
    protected ValuesHelper values;

    @Override
    public Project retrieveProject(Integer id) {
        return accessor.retrieveProject(id);
    }

    @Override
    public ProjectGroup retrieveGroup(Integer id) {
        return accessor.retrieveGroup(id);
    }

    @Override
    public ProjectSubgroup retrieveSubgroup(Integer id) {
        return accessor.retrieveSubgroup(id);
    }

    @Override
    public Project getProject() {
        return new ConcreteProject(this, accessor);
    }

    @Override
    public ProjectGroup getProjectGroup() {
        return new ConcreteProjectGroup(this, accessor);
    }

    @Override
    public ProjectSubgroup getProjectSubgroup() {
        return new ConcreteProjectSubgroup(this, accessor);
    }

    @Override
    public List<Project> getAllProjects() {
        return accessor.getAllProjects();
    }

    @Override
    public List<Project> findProjectByName(String name) {
        return accessor.findProjectByName(name);
    }

    @Override
    public List<ProjectGroup> findProjectGroupByName(String name) {
        return accessor.findProjectGroupByName(name);
    }

    @Override
    public List<ProjectSubgroup> findProjectSubgroupByName(String name) {
        return accessor.findProjectSubgroupByName(name);
    }

    @Override
    public Project findProjectByTechnicalName(String name) {
        return accessor.findProjectByTechnicalName(name);
    }

    @Override
    public ProjectGroup findProjectGroupByTechnicalName(String name, Integer projectId) {
        return accessor.findProjectGroupByTechnicalName(name, projectId);
    }

    @Override
    public ProjectSubgroup findProjectSubgroupByTechnicalName(String name, Integer projectId, Integer projectGroupId) {
        return accessor.findProjectSubgroupByTechnicalName(name, projectId, projectGroupId);
    }

    @Override
    public ValuesHelper getValuesHelper() {
        return values;
    }

    @Override
    public List<ProjectGroup> getGroupsLinkedToProject(int project) {
        return accessor.getGroupsLinkedToProject(project);
    }

    @Override
    public List<ProjectSubgroup> getSubgroupsLinkedToGroup(int group) {
        return accessor.getSubgroupsLinkedToGroup(group);
    }

    @Override
    public List<Contributor> getContributorsLinkedToSubgroup(int subgroup) {
        return accessor.getContributorsLinkedToSubgroup(subgroup);
    }

    @Override
    public List<Group> getContributorGroupsLinkedToSubgroup(int subgroup) {
        return accessor.getContributorGroupsLinkedToSubgroup(subgroup);
    }

    @Override
    public String generateProjectUser(int projectId) throws PersistenceException {
        return accessor.generateProjectUser(projectId);
    }

    @Override
    public void deleteProjectUser(int projectId) {
        accessor.deleteProjectUser(projectId);
    }
}
