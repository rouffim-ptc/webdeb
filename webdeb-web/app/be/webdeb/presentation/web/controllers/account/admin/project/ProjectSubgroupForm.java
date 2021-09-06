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

import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.account.group.SimpleGroupForm;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;

public class ProjectSubgroupForm extends BaseProjectForm {

    @Inject
    private ContributorFactory contributorFactory = Play.current().injector().instanceOf(ContributorFactory.class);;

    private Integer projectGroupId = -1;
    private Integer projectId = -1;
    private ProjectForm project;
    private ProjectGroupForm projectGroup;
    private List<SimpleGroupForm> contributorGroups = new ArrayList<>();

    private String nbContributors;
    private String projectTechnicalName;
    private String projectGroupTechnicalName;

    /**
     * Play / JSON compliant constructor
     */
    public ProjectSubgroupForm() {
        super();
    }

    /**
     * Constructor from a project object and a project group object
     *
     * @param group a project group
     * @param project a project
     */
    public ProjectSubgroupForm(ProjectGroup group, Project project) {
        super();
        this.projectId = project.getId();
        this.projectGroupId = group.getId();
    }

    /**
     * Constructor from a project object and a project subgroup object
     *
     * @param subgroup a project subgroup
     * @param project a project
     */
    public ProjectSubgroupForm(ProjectSubgroup subgroup, Project project) {
        super(subgroup);
        this.projectId = project.getId();
        this.projectGroupId = subgroup.getProjectGroupId();
        this.nbContributors = String.valueOf(subgroup.getNbContributors());
        this.projectTechnicalName = project.getTechnicalName();
        this.projectGroupTechnicalName = subgroup.getProjectGroupTechnicalName();
        subgroup.getContributorGroups().forEach(e -> contributorGroups.add(new SimpleGroupForm(e)));
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
            errors.put("name", Collections.singletonList(new ValidationError("name", "project.subgroup.error.name")));
        }

        // must have a technical name
        if (values.isBlank(technicalName)) {
            errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name")));
        }else {
            ProjectSubgroup subgroup = factory.findProjectSubgroupByTechnicalName(technicalName, projectId, projectGroupId);
            if(subgroup != null && !subgroup.getId().equals(id)){
                errors.put("technicalName", Collections.singletonList(new ValidationError("technicalName", "project.project.error.name.alreadyexists")));
            }
        }

        // must have a number of contributors
        if (!values.isNumeric(nbContributors)) {
            errors.put("nbContributors", Collections.singletonList(new ValidationError("nbContributors", "project.subgroup.error.nbContributors")));
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this project subgroup in database
     *
     * @throws PersistenceException if an error occurred while saving this object into DB
     */
    public void save() throws PersistenceException, FormatException {
        logger.debug("try to save project subgroup " + name);
        // set all primitive fields
        ProjectSubgroup subgroup = factory.getProjectSubgroup();
        subgroup.setId(id);
        subgroup.setName(name);
        subgroup.setTechnicalName(technicalName);
        subgroup.setNbContributors(Integer.parseInt(nbContributors));
        subgroup.setProjectGroupId(projectGroupId);

        List<Group> groups = new ArrayList<>();
        for(SimpleGroupForm group : contributorGroups){
            Group g = contributorFactory.retrieveGroup(group.getGroupId());
            if(g != null)
                groups.add(g);
        }
        subgroup.setContributorGroups(groups);

        // now save it
        subgroup.save();
    }

    /*
     * Getters and setters
     */

    public ProjectGroupForm getProjectGroup() {
        return projectGroup;
    }

    public List<SimpleGroupForm> getContributorGroups() {
        return contributorGroups;
    }

    public void setContributorGroups(List<SimpleGroupForm> contributorGroups) {
        this.contributorGroups = contributorGroups;
    }

    public ProjectForm getProject() {
        return project;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getNbContributors() {
        return nbContributors;
    }

    public void setNbContributors(String nbContributors) {
        this.nbContributors = nbContributors;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getProjectGroupId() {
        return projectGroupId;
    }

    public void setProjectGroupId(Integer projectGroupId) {
        this.projectGroupId = projectGroupId;
    }

    public String getContributorExample(){
        return projectTechnicalName + "_" + projectGroupTechnicalName + "_" + technicalName + "_" + 1;
    }
}
