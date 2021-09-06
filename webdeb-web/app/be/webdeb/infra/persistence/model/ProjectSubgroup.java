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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The persistent class for the project subgroup database table, holding project.
 */
@Entity
@CacheBeanTuning
@Table(name = "project_subgroup")
@Unqueryable
public class ProjectSubgroup extends Model {

    private static final Model.Finder<Integer, ProjectSubgroup> find = new Model.Finder<>(ProjectSubgroup.class);
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_project_subgroup", unique = true, nullable = false)
    private Integer idProjectSubgroup;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "technical_name", length = 255)
    private String technicalName;

    @Column(name = "nb_contributors")
    private int nbContributors;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project_group")
    private ProjectGroup projectGroup;

    @Version
    @Column(name = "version")
    @Unqueryable
    private Timestamp version;

    @OneToMany(mappedBy = "subgroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TmpContributor> tmpContributors;


    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name="project_subgroup_has_contributor_group",
            joinColumns = { @JoinColumn(name="id_project_subgroup", referencedColumnName = "id_project_subgroup") },
            inverseJoinColumns = { @JoinColumn(name="id_contributor_group", referencedColumnName = "id_group") }
    )
    private List<Group> groups;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name="contributor_in_project_subgroup",
            joinColumns = { @JoinColumn(name="id_project_subgroup", referencedColumnName = "id_project_subgroup") },
            inverseJoinColumns = { @JoinColumn(name="id_contributor", referencedColumnName = "id_contributor") }
    )
    private List<Contributor> contributors;

    /**
     * Get project subgroup id
     *
     * @return the project subgroup id
     */
    public Integer getIdProjectSubgroup() {
        return idProjectSubgroup;
    }

    /**
     * Set project subgroup id
     *
     * @param idProjectSubgroup the project subgroup id
     */
    public void setIdProjectSubgroup(Integer idProjectSubgroup) {
        this.idProjectSubgroup = idProjectSubgroup;
    }

    /**
     * Get project subgroup name
     *
     * @return the project subgroup name
     */
    public String getName() {
        return name;
    }

    /**
     * Set project subgroup name
     *
     * @param name the project subgroup name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get project subgroup technical name, used to attribute login to automated inscription of contributor.
     *
     * @return the project subgroup technical name
     */
    public String getTechnicalName() {
        return technicalName;
    }

    /**
     * Set project subgroup technical name
     *
     * @param technicalName the project technical name
     */
    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    /**
     * Get the number of participants for this subgroup.
     *
     * @return the number of participants
     */
    public int getNbContributors() {
        return nbContributors;
    }

    /**
     * Set the number of participants for this subgroup.
     *
     * @param nbContributors the number of participants
     */
    public void setNbContributors(int nbContributors) {
        this.nbContributors = nbContributors;
    }

    /**
     * Get project group who owns this subgroup
     *
     * @return a project group
     */
    public ProjectGroup getProjectGroup() {
        return projectGroup;
    }

    /**
     * Set project group who owns this subgroup
     *
     * @param group a project group
     */
    public void setProjectGroup(ProjectGroup group) {
        this.projectGroup = group;
    }

    /**
     * Get the current version of this project subgroup
     *
     * @return a timestamp with the latest update moment of this project subgroup
     */
    public Timestamp getVersion() {
        return version;
    }

    /**
     * Set the version of this project subgroup
     *
     * @param version the timestamp with the latest update moment of this project subgroup
     */
    public void setVersion(Timestamp version) {
        this.version = version;
    }

    /**
     * Get project subgroup affiliated tmp contributors
     *
     * @return the tmp contributors
     */
    public List<TmpContributor> getTmpContributors() {
        return tmpContributors;
    }

    /**
     * Set project subgroup affiliated tmp contributors
     *
     * @param contributors the project subgroup tmp contributors
     */
    public void setTmpContributors(List<TmpContributor> contributors) {
        this.tmpContributors = contributors;
    }

    /**
     * Get the list of involved contributors in this project subgroup
     *
     * @return a possibly empty list of contributors
     */
    public List<Contributor> getContributors() {
        return contributors;
    }

    /**
     * Set the list of involved contributors in this project subgroup
     *
     * @param contributors a list of contributors
     */
    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    /**
     * Get the list of affiliated groups
     *
     * @return a possibly empty list of contributor groups
     */
    public List<Group> getContributorGroups() {
        return groups;
    }

    /**
     * Set the list of affiliated groups
     *
     * @param groups a list of contributor groups
     */
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        return new StringBuffer("ProjectSubgroup [").append(idProjectSubgroup).append("]")
                .append(" with name ").append(name).toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a project subgroup by its id
     *
     * @param id an id
     * @return the project subgroup corresponding to the given id, null if not found
     */
    public static ProjectSubgroup findById(Integer id) {
        return id == -1 ? null : find.byId(id);
    }

    /**
     * Find a list of project subgroups by a partial name
     *
     * @param name a project subgroup name
     * @return the list of ProjectSubgroup matching the given partial name
     */
    public static List<ProjectSubgroup> findByTitle(String name) {
        List<ProjectSubgroup> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct p.id_project_subgroup from project_subgroup p where p.name like '%" + name + "%'" +
                    " or p.technical_name like '%" + name + "%'";

            logger.debug("search for project subgroup : " + select);
            result = Ebean.find(ProjectSubgroup.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Find one by technical name
     *
     * @param name a project subgroup technical name
     * @param projectId a project id
     * @param projectGroupId a project group id
     * @return a ProjectSubgroup matching the given technical name, or null
     */
    public static ProjectSubgroup findByTechnicalName(String name, Integer projectId, Integer projectGroupId) {
        List<ProjectSubgroup> result = null;
        if (name != null) {
            // will be automatically ordered by relevance
            String token = /* "\"" + */ name.trim()
                    // protect single quotes
                    .replace("'", "\\'")
                    // add '%' for spaces
                    .replace(" ", "%");

            String select = "select distinct sg.id_project_subgroup from project_subgroup sg " +
                    "left join project_group pg on pg.id_project_group = sg.id_project_group " +
                    "where pg.id_project = " + projectId + " and pg.id_project_group = " + projectGroupId + " and sg.technical_name like '" + name + "'";

            logger.debug("search for project subgroup : " + select);
            result = Ebean.find(ProjectSubgroup.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    /*
     * HELPER METHODS
     */

    /**
     * Add a given contributor as member of this project group
     *
     * @param contributor a contributor
     */
    public void addMember(Contributor contributor) {
        // check if user is already in group
        if(contributors.stream().noneMatch(e -> e.getIdContributor().equals(contributor.getIdContributor()))) {
            // not found => add it
            contributors.add(contributor);
        }
    }

    /**
     * Get the map of amount of contributions for this subgroup by contribution type, for all contribution created or update
     * between given dates.
     *
     * @param fromDate contribution created or updated from date
     * @param toDate contribution created or updated to date
     * @return a possibly empty map of contribution type, amount of contributions
     */
    public Map<Integer, Long> getContributionsAmount(Date fromDate, Date toDate) {
        Map<Integer, Long> results = new LinkedHashMap<>();

        String pattern = "yyyy-MM-dd HH:mm:ss";
        String fDate = new SimpleDateFormat(pattern).format(fromDate);
        String tDate = new SimpleDateFormat(pattern).format(toDate);

        String select = "SELECT c.contribution_type as 'type', count(c.id_contribution) as 'nb_contributions' FROM contribution c " +
                "left join contribution_has_contributor chc on chc.id_contribution = c.id_contribution " +
                "left join contributor con on con.id_contributor = chc.id_contributor " +
                "left join tmp_contributor tcon on tcon.id_tmp_contributor = con.tmp_contributor " +
                "where tcon.id_project_subgroup = " + idProjectSubgroup + " and c.contribution_type <= 6 and c.contribution_type != 2 " +
                "and (c.version BETWEEN '" + fDate + "' AND '" + tDate + "') and c.deleted = 0 and c.hidden = 0 group by c.contribution_type";

        Ebean.createSqlQuery(select).findList().forEach(e -> {
            if(e.getInteger("type") != null && e.getLong("nb_contributions") > 0)
                results.put(e.getInteger("type"), e.getLong("nb_contributions"));
        });

        return results;
    }
}
