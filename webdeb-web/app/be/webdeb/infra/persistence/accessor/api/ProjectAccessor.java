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

package be.webdeb.infra.persistence.accessor.api;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.PersistenceException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This interface represents an accessor for projects persisted into the database
 *
 * @author Martin Rouffiange
 */
public interface ProjectAccessor {

    /**
     * Retrieve a Project by its id
     *
     * @param id a Project id
     * @return a Project if given id corresponding to a project, null otherwise
     */
    Project retrieveProject(Integer id);

    /**
     * Retrieve a ProjectGroup by its id
     *
     * @param id a ProjectGroup id
     * @return a ProjectGroup if given id corresponding to a project group, null otherwise
     */
    ProjectGroup retrieveGroup(Integer id);

    /**
     * Retrieve a ProjectSubgroup by its id
     *
     * @param id a ProjectSubgroup id
     * @return a ProjectSubgroup if given id corresponding to a project subgroup, null otherwise
     */
    ProjectSubgroup retrieveSubgroup(Integer id);

    /**
     * Save a project, if project.getId has been set, update the project, otherwise create project.
     *
     * @param project the project to save
     *
     * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
     * an existing project (id set). The exception message will contain a more complete description of the
     * error.
     */
    void save(Project project) throws PersistenceException;

    /**
     * Save a project group, if group.getId has been set, update the group, otherwise create group.
     *
     * The passed project is considered as valid.
     *
     * @param group the project group to save
     *
     * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
     * an existing project (id set). The exception message will contain a more complete description of the
     * error.
     */
    void save(ProjectGroup group) throws PersistenceException;

    /**
     * Save a project subgroup, if subgroup.getId has been set, update the subgroup, otherwise create subgroup.
     *
     * The passed group is considered as valid..
     *
     * @param subgroup the project subgroup to save
     *
     * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
     * an existing project (id set). The exception message will contain a more complete description of the
     * error.
     */
    void save(ProjectSubgroup subgroup) throws PersistenceException;

    /**
     * Get the list of existing projects
     *
     * @return a possibly empty list of projects
     */
    List<Project> getAllProjects();

    /**
     * Find a list of Project by a (partial) name
     *
     * @param name a project name
     * @return a possibly empty list of Project with their name containing the given name
     */
    List<Project> findProjectByName(String name);

    /**
     * Find a list of ProjectGroup by a (partial) name
     *
     * @param name a project group name
     * @return a possibly empty list of ProjectGroup with their name containing the given name
     */
    List<ProjectGroup> findProjectGroupByName(String name);

    /**
     * Find a list of ProjectSubgroup by a (partial) name
     *
     * @param name a project subgroup name
     * @return a possibly empty list of ProjectSubgroup with their name containing the given name
     */
    List<ProjectSubgroup> findProjectSubgroupByName(String name);

    /**
     * Find a Project by a technical name
     *
     * @param name a project technical name
     * @return a project or null
     */
    Project findProjectByTechnicalName(String name);

    /**
     * Find a ProjectGroup by a technical name
     *
     * @param name a project group technical name
     * @param projectId a project id
     * @return a ProjectGroup or null
     */
    ProjectGroup findProjectGroupByTechnicalName(String name, Integer projectId);

    /**
     * Find a ProjectSubgroup by a technical name
     *
     * @param name a project subgroup technical name
     * @param projectId a project id
     * @param projectGroupId a project group id
     * @return a ProjectSubgroup or null
     */
    ProjectSubgroup findProjectSubgroupByTechnicalName(String name, Integer projectId, Integer projectGroupId);

    /**
     * Get the list of project groups linked to a given project
     *
     * @param project a project id
     * @return a possibly empty list of project groups
     */
    List<ProjectGroup> getGroupsLinkedToProject(int project);

    /**
     * Get the list of project subgroups linked to a given project group
     *
     * @param group a project group id
     * @return a possibly empty list of project subgroups
     */
    List<ProjectSubgroup> getSubgroupsLinkedToGroup(int group);

    /**
     * Get the list of contributors linked to a given subgroup
     *
     * @param subgroup a project subgroup id
     * @return a possibly empty list of contributors
     */
    List<Contributor> getContributorsLinkedToSubgroup(int subgroup);

    /**
     * Get the list of contributor groups linked to a given subgroup
     *
     * @param subgroup a project subgroup id
     * @return a possibly empty list of contributor groups
     */
    List<Group> getContributorGroupsLinkedToSubgroup(int subgroup);

    /**
     * Generate the temporary contributors for a given group
     *
     * @param projectId a project id
     */
    String generateProjectUser(int projectId) throws PersistenceException;

    /**
     * Delete the temporary contributors for a given group
     *
     * @param projectId a project id
     */
    void deleteProjectUser(int projectId);

    /**
     * Check if the given project has tmp contributor(s)
     *
     * @return true if it is the case
     */
    boolean hasTmpContributors(int project);

    /**
     * Get the report about contribution in this project by contribution type and contributor group (if any linked with subgroups)
     *
     * @param projectId a project id
     * @param fromDate contribution created or updated from date
     * @param toDate contribution created or updated to date
     * @return a map of group, contribution type and number of contribution of this type for this group.
     */
    Map<Group, Map<Integer, Long>> getProjectContributionReportByContributorGroup(Integer projectId, Date fromDate, Date toDate);

    /**
     * Get the report about contribution in this project by contribution type and project subgroup
     *
     * @param projectId a project id
     * @param fromDate contribution created or updated from date
     * @param toDate contribution created or updated to date
     * @return a map of group, contribution type and number of contribution of this type for this subgroup.
     */
    Map<ProjectSubgroup, Map<Integer, Long>> getProjectContributionReportByProjectSubgroup(Integer projectId, Date fromDate, Date toDate);

}
