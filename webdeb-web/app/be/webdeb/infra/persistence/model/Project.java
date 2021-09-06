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

package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;
import play.data.format.Formats;


import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the project database table, holding project.
 */
@Entity
@CacheBeanTuning
@Table(name = "project")
@Unqueryable
public class Project extends Model {

    private static final Model.Finder<Integer, Project> find = new Model.Finder<>(Project.class);
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_project", unique = true, nullable = false)
    private Integer idProject;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "technical_name", length = 255)
    private String technicalName;

    @Formats.DateTime(pattern="yyyy-MM-dd")
    @Column(name = "begin_date")
    private Date beginDate;

    @Formats.DateTime(pattern="yyyy-MM-dd")
    @Column(name = "end_date")
    private Date endDate;

    @Formats.DateTime(pattern="yyyy-MM-dd")
    @Column(name = "generation_date")
    private Date generationDate;

    @Version
    @Column(name = "version")
    @Unqueryable
    private Timestamp version;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectGroup> groups;

    /**
     * Get project id
     *
     * @return the project id
     */
    public Integer getIdProject() {
        return idProject;
    }

    /**
     * Set project id
     *
     * @param idProject the project id
     */
    public void setIdProject(Integer idProject) {
        this.idProject = idProject;
    }

    /**
     * Get project name
     *
     * @return the project name
     */
    public String getName() {
        return name;
    }

    /**
     * Set project name
     *
     * @param name the project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get project technical name, used to attribute login to automated inscription of contributor.
     *
     * @return the project technical name
     */
    public String getTechnicalName() {
        return technicalName;
    }

    /**
     * Set project technical name
     *
     * @param technicalName the project technical name
     */
    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    /**
     * Get project begin date
     *
     * @return the project begin date
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * Set project begin date
     *
     * @param beginDate the project begin date
     */
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    /**
     * Get project end date
     *
     * @return the project end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set project end date
     *
     * @param endDate the project end
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Get the date of the last generation of contributor for the project
     *
     * @return the last generation date
     */
    public Date getGenerationDate() {
        return generationDate;
    }

    /**
     * Set the date of the last generation of contributor for the project
     *
     * @param generationDate the last generation date
     */
    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    /**
     * Get the current version of this project
     *
     * @return a timestamp with the latest update moment of this project
     */
    public Timestamp getVersion() {
        return version;
    }

    /**
     * Set the version of this project
     *
     * @param version the timestamp with the latest update moment of this project
     */
    public void setVersion(Timestamp version) {
        this.version = version;
    }

    /**
     * Get the flag that says if the project has already started and is still in progress
     *
     * @return true if the project has already started and is still in progress
     */
    public boolean isInProgress() {
        Date currentDate = new Date();
        return currentDate.after(beginDate) && currentDate.before(endDate);
    }

    /**
     * Get project affiliated groups
     *
     * @return the project groups
     */
    public List<ProjectGroup> getGroups() {
        return groups;
    }

    /**
     * Set project affiliated groups
     *
     * @param groups the project groups
     */
    public void setGroups(List<ProjectGroup> groups) {
        this.groups = groups;
    }


    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        return new StringBuffer("Project [").append(idProject).append("]")
                .append(" with name ").append(name).toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a project by its id
     *
     * @param id an id
     * @return the project corresponding to the given id, null if not found
     */
    public static Project findById(Integer id) {
        return id == -1 ? null : find.byId(id);
    }

    /**
     * Find a list of projects by a partial name
     *
     * @param name a project name
     * @return the list of Project matching the given partial name
     */
    public static List<Project> findByTitle(String name) {
        List<Project> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct p.id_project from project p where p.name like '%" + name + "%'" +
                    " or p.technical_name like '%" + name + "%'";

            logger.debug("search for project : " + select);
            result = Ebean.find(Project.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Find one by technical name
     *
     * @param name a project technical name
     * @return a Project matching the given technical name, or null
     */
    public static Project findByTechnicalName(String name) {
        List<Project> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct p.id_project from project p where p.technical_name like '" + name + "'";

            logger.debug("search for project : " + select);
            result = Ebean.find(Project.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Get the list of existing projects
     *
     * @return a possibly empty list of projects
     */
    public static List<Project> getAllProjects() {
        List<Project> projects = find.all();
        return projects != null ? projects : new ArrayList<>();
    }

    /**
     * Get the list of contributor groups in this project
     *
     * @return a possibly empty list of contributor groups
     */
    public List<Group> getAllContributorGroups() {
        List<Group> result;

        String select = "SELECT distinct cg.id_group FROM project_subgroup sg " +
                "left join project_subgroup_has_contributor_group pshcg on pshcg.id_project_subgroup = sg.id_project_subgroup " +
                "left join contributor_group cg on pshcg.id_contributor_group = cg.id_group " +
                "where sg.id_project = " + idProject;

        result = Ebean.find(Group.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Get the list of project subgroups linked with this project
     *
     * @return a possibly empty list of project subgroups
     */
    public List<ProjectSubgroup> getAllSubgroups() {
        List<ProjectSubgroup> result;

        String select = "SELECT distinct sg.id_project_subgroup FROM project_subgroup sg " +
                "left join project_group pg on pg.id_project_group = sg.id_project_group " +
                "where pg.id_project = " + idProject;

        result = Ebean.find(ProjectSubgroup.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return result != null ? result : new ArrayList<>();
    }
}
