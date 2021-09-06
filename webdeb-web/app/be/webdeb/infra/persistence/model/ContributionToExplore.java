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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class to records contributions to explore. Used to put forward some contributions in explore pages for example.
 *
 * @author Martin Rouffiange
 */
@Unqueryable
@Entity
@CacheBeanTuning
@Table(name = "contribution_to_explore")
public class ContributionToExplore extends Model {

    private static final Model.Finder<Long, ContributionToExplore> find = new Model.Finder<>(ContributionToExplore.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contribution_to_explore", unique = true, nullable = false)
    private Long idContributionToExplore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contribution", nullable = false)
    private Contribution contribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contributor_group", nullable = false)
    private Group group;

    @Column(name = "num_order")
    private int order;

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the contribution to explore id
     *
     * @return the contribution to explore id
     */
    public Long getIdContributionToExplore() {
        return idContributionToExplore;
    }

    /**
     * Set the contribution to explore id
     *
     * @param idContributionToExplore a contribution to explore id
     */
    public void setIdContributionToExplore(Long idContributionToExplore) {
        this.idContributionToExplore = idContributionToExplore;
    }

    /**
     * Get the contribution where bound actor plays a role
     *
     * @return a contribution
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set the contribution where bound actor plays a role
     *
     * @param contribution a contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get the contributor group where this contribution must be appeared in the group explore list
     *
     * @return a contributor group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Set the contributor group where this contribution must be appeared in the group explore list
     *
     * @param group a contributor group
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Get the order of the contribution in explore list
     *
     * @return the order of the contribution
     */
    public int getOrder() {
        return order;
    }

    /**
     * Set the order of the contribution in explore list
     *
     * @param order the order of the contribution
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a contribution to explore by its id
     *
     * @param id the contribution to explore id
     * @return the contribution to explore corresponding to that id, null otherwise
     */
    public static ContributionToExplore findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find all contributions to explore objects that exists in database
     *
     * @return a list of contributions to explore
     */
    public static List<ContributionToExplore> findAllContributionToExplores() {
        return find.all();
    }

    /**
     * Find all contributions to explore objects that exists in database for given contribution id
     *
     * @param id a contribution id
     * @return a list of contributions to explore
     */
    public static List<ContributionToExplore> findContributionToExploresForContribution(Long id) {
        return find.where()
                .eq("id_contribution", id)
                .findList();
    }

    /**
     * Find all contributions to explore objects that exists in database by contribution type and user group
     *
     * @param contributionType a contribution type
     * @param group a user group
     * @return a list of contributions to explore
     */
    public static List<ContributionToExplore> findAllContributionToExplores(EContributionType contributionType, int group) {
        return contributionType == null || contributionType == EContributionType.ALL ?
                findAllContributionToExplores() : getContributionsToExplore(contributionType.id(), group);
    }

    /**
     * Get contributions to explore by contribution type in given group
     *
     * @param type a contribution type id (-1 to ignore)
     * @param group the group id where to look for contributions (if public group is passed, any public contribution is returned) (-1 to ignore)
     * @return the list of contributions to explore in database, depending on given type and group
     */
    public static List<ContributionToExplore> getContributionsToExplore(int type, int group) {
        String select = "select distinct cte.id_contribution_to_explore from contribution_to_explore cte " +
                (group != -1 ? "left join contribution_in_group on cte.id_contribution = contribution_in_group.id_contribution " : "") +
                (type != -1 ? "left join contribution on cte.id_contribution = contribution.id_contribution  " : "") +
                "where cte.id_contribution_to_explore >= 0 " +
                (type != -1 ? " and contribution_type = " + type + " " : "") +
                " and id_group = " + (group != -1 ? group : Group.getPublicGroup().getIdGroup());
        List<ContributionToExplore> result = Ebean.find(ContributionToExplore.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        return result != null ? result : new ArrayList<>();
    }
}
