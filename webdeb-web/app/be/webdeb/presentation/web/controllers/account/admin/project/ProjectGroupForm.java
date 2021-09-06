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

package be.webdeb.presentation.web.controllers.account.admin.project;

import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import play.data.validation.ValidationError;

import java.util.*;

public class ProjectGroupForm extends BaseProjectForm {

    private Integer projectId = -1;
    private ProjectForm project;
    private List<ProjectSubgroupForm> subgroups = new ArrayList<>();

    private ProjectGroup group;

    /**
     * Play / JSON compliant constructor
     */
    public ProjectGroupForm() {
        super();
    }

    /**
     * Constructor from a project object
     *
     * @param project a project
     */
    public ProjectGroupForm(Project project) {
        super();
        this.projectId = project.getId();
    }

    /**
     * Constructor from a project object and a project group object
     *
     * @param group a project group
     * @param project a project
     */
    public ProjectGroupForm(ProjectGroup group, Project project) {
        super(group);
        this.group = group;
        this.projectId = project.getId();
    }

    /**
     * Validator (called from form submit)
     *
     * @return null if no error has been found, otherwise the list of found errors
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        // must have a name
        if (values.isBlank(name)) {
            errors.put("name", Collections.singletonList(new ValidationError("name", "project.group.error.name")));
        }

        // must have a technical name
        if (values.isBlank(technicalName)) {
            errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name")));
        }else{
            ProjectGroup group = factory.findProjectGroupByTechnicalName(technicalName, projectId);
            if(group != null && !group.getId().equals(id)){
                errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name.alreadyexists")));
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this project group in database
     *
     * @throws PersistenceException if an error occurred while saving this object into DB
     */
    public void save() throws PersistenceException, FormatException {
        logger.debug("try to save project group " + toString());
        // set all primitive fields
        ProjectGroup group = factory.getProjectGroup();
        group.setId(id);
        group.setName(name);
        group.setTechnicalName(technicalName);
        group.setProjectId(projectId);

        // now save it
        group.save();
    }

    /*
     * Getters and setters
     */

    public Integer getProjectId() {
        return projectId;
    }

    public ProjectForm getProject() {
        return project;
    }

    public List<ProjectSubgroupForm> getSubgroups() {
        return subgroups;
    }

    public ProjectGroup getGroup() {
        return group;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public void setSubgroups(List<ProjectSubgroupForm> subgroups) {
        this.subgroups = subgroups;
    }

    @Override
    public String toString() {
        return "ProjectGroupForm{" +
                "projectId=" + projectId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", technicalName='" + technicalName + '\'' +
                '}';
    }
}
