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
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;

import java.util.List;

/**
 * This class implements a ProjectSubgroup.
 *
 * @author Martin Rouffiange
 */
class ConcreteProjectSubgroup extends AbstractBaseProject implements ProjectSubgroup {

    private String generatedPassword = "";
    private Project project;
    private ProjectGroup group;
    private Integer projectId = -1;
    private Integer projectGroupId = -1;
    private int nbContributors;
    private String groupTechnicalName;

    private List<Contributor> contributors = null;
    private List<Group> contributorGroups = null;

    /**
     * Create a ProjectSubgroup instance
     *
     * @param factory the project factory
     * @param accessor the project accessor
     */
    ConcreteProjectSubgroup(ProjectFactory factory, ProjectAccessor accessor) {
        super(factory, accessor);
    }

    @Override
    public int getNbContributors() {
        return nbContributors;
    }

    @Override
    public void setNbContributors(int nbContributors) {
        this.nbContributors = nbContributors;
    }

    @Override
    public String getPassword() {
        return generatedPassword;
    }

    @Override
    public void setPassword(String password) {
        this.generatedPassword = password;
    }

    @Override
    public ProjectGroup getGroup() {
        return group;
    }

    @Override
    public void setGroup(ProjectGroup group) {
        this.group = group;
    }

    @Override
    public List<Contributor> getContributors() {
        if(contributors == null){
            contributors = factory.getContributorsLinkedToSubgroup(id);
        }
        return contributors;
    }

    @Override
    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    @Override
    public List<Group> getContributorGroups() {
        if(contributorGroups == null){
            contributorGroups = factory.getContributorGroupsLinkedToSubgroup(id);
        }
        return contributorGroups;
    }

    @Override
    public void setContributorGroups(List<Group> groups) {
        this.contributorGroups = groups;
    }

    @Override
    public Integer getProjectGroupId() {
        return projectGroupId;
    }

    @Override
    public void setProjectGroupId(Integer projectGroupId) {
        this.projectGroupId = projectGroupId;
    }

    @Override
    public String getProjectGroupTechnicalName() {
        return groupTechnicalName;
    }

    @Override
    public void setProjectGroupTechnicalName(String name) {
        this.groupTechnicalName = name;
    }

    @Override
    public void save() throws FormatException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("project subgroup contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.PROJECT_SUBGROUP_ERROR, String.join(",", isValid.toString()));
        }
        accessor.save(this);
    }

    @Override
    public void remove() throws PersistenceException {

    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(projectGroupId == null){
            fieldsInError.add("subgroup has no project id");
        }

        return fieldsInError;
    }
}
