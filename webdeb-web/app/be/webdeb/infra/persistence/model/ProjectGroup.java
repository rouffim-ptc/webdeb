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


import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the project group database table, holding project.
 */
@Entity
@CacheBeanTuning
@Table(name = "project_group")
@Unqueryable
public class ProjectGroup extends Model {

    private static final Model.Finder<Integer, ProjectGroup> find = new Model.Finder<>(ProjectGroup.class);
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_project_group", unique = true, nullable = false)
    private Integer idProjectGroup;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "technical_name", length = 255, nullable = false)
    private String technicalName;

    @Version
    @Column(name = "version")
    @Unqueryable
    private Timestamp version;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project")
    private Project project;

    @OneToMany(mappedBy = "projectGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectSubgroup> subgroups;

    /**
     * Get project group id
     *
     * @return the project group id
     */
    public Integer getIdProjectGroup() {
        return idProjectGroup;
    }

    /**
     * Set project group id
     *
     * @param idProjectGroup the project group id
     */
    public void setIdProjectGroup(Integer idProjectGroup) {
        this.idProjectGroup = idProjectGroup;
    }

    /**
     * Get project group name
     *
     * @return the project group name
     */
    public String getName() {
        return name;
    }

    /**
     * Set project group name
     *
     * @param name the project group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get project group technical name, used to attribute login to automated inscription of contributor.
     *
     * @return the project group technical name
     */
    public String getTechnicalName() {
        return technicalName;
    }

    /**
     * Set project group technical name
     *
     * @param technicalName the project group technical name
     */
    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    /**
     * Get project who owns this group
     *
     * @return a project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Set project who owns this group
     *
     * @param project a project
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get the current version of this project group
     *
     * @return a timestamp with the latest update moment of this project group
     */
    public Timestamp getVersion() {
        return version;
    }

    /**
     * Set the version of this project group
     *
     * @param version the timestamp with the latest update moment of this project group
     */
    public void setVersion(Timestamp version) {
        this.version = version;
    }

    /**
     * Get project group affiliated subgroups
     *
     * @return the project subgroups
     */
    public List<ProjectSubgroup> getSubgroups() {
        return subgroups;
    }

    /**
     * Set project group affiliated subgroups
     *
     * @param subgroups the project group subgroups
     */
    public void setGroups(List<ProjectSubgroup> subgroups) {
        this.subgroups = subgroups;
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        return new StringBuffer("ProjectGroup [").append(idProjectGroup).append("]")
                .append(" with name ").append(name).toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a project group by its id
     *
     * @param id an id
     * @return the project group corresponding to the given id, null if not found
     */
    public static ProjectGroup findById(Integer id) {
        return id == -1 ? null : find.byId(id);
    }

    /**
     * Find a list of project groups by a partial name
     *
     * @param name a project name
     * @return the list of ProjectGroup matching the given partial name
     */
    public static List<ProjectGroup> findByTitle(String name) {
        List<ProjectGroup> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct p.id_projectGroup from project_group p where p.name like '%" + name + "%'" +
                    " or p.technical_name like '%" + name + "%'";

            logger.debug("search for project group : " + select);
            result = Ebean.find(ProjectGroup.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Find one by technical name
     *
     * @param name a project group technical name
     * @param projectId a project id
     * @return a ProjectGroup matching the given technical name, or null
     */
    public static ProjectGroup findByTechnicalName(String name, Integer projectId) {
        List<ProjectGroup> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct pg.id_project_group from project_group pg " +
                    "where pg.id_project = " + projectId + " and pg.technical_name like '" + name + "'";

            logger.debug("search for project group : " + select);
            result = Ebean.find(ProjectGroup.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }
}
