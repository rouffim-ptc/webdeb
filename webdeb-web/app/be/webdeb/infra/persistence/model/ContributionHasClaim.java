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

import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The persistent class for a contributor claim on a contribution.
 *
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "contribution_has_claim")
@Unqueryable
public class ContributionHasClaim extends WebdebModel {

    private static final Model.Finder<ContributionHasClaimPK, ContributionHasClaim> find =
            new Model.Finder<>(ContributionHasClaim.class);

    @EmbeddedId
    private ContributionHasClaimPK id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_contribution", nullable = false)
    private Contribution contribution;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_contributor", nullable = false)
    private Contributor contributor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type", nullable = false)
    private TClaimType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_group", nullable = false)
    private Group group;

    @Column(name = "url")
    private String url;

    @Column(name = "comment")
    private String comment;

    /**
     * Construct a claim
     *
     * @param contribution the contribution to claim
     * @param contributor the contributor who claims
     * @param url the url where the contributor do the claim
     * @param type the type of claim
     * @param group the group where the contribution is claim
     */
    public ContributionHasClaim(Contribution contribution, Contributor contributor, String url, String comment, TClaimType type, Group group) {
        this.contribution = contribution;
        this.contributor = contributor;
        this.url = url;
        this.comment = comment;
        this.type = type;
        this.group = group;
        setId(new ContributionHasClaimPK(contribution.getIdContribution(), contributor.getIdContributor()));
    }

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the complex join-object id
     *
     * @return a complex id
     */
    public ContributionHasClaimPK getId() {
        return this.id;
    }

    /**
     * Set the complex join-object id
     *
     * @param id a complex id
     */
    public void setId(ContributionHasClaimPK id) {
        this.id = id;
    }

    public TClaimType getType() {
        return type;
    }

    public void setType(TClaimType type) {
        this.type = type;
    }

    /**
     * Get bound contribution
     *
     * @return a contribution
     */
    public Contribution getContribution() {
        return this.contribution;
    }

    /**
     * Set bound contribution
     *
     * @param contribution a contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get bound contributor (that created / updated bound contribution)
     *
     * @return a contributor
     */
    public Contributor getContributor() {
        return contributor;
    }

    /**
     * Set bound contributor (that created / updated bound contribution)
     *
     * @param contributor a contributor
     */
    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Get version timestamp, ie, when linked contributor made a modification to linked contribution
     *
     * @return a timestamp
     */
    public Timestamp getVersion() {
        return id.getVersion();
    }

    /*
     * CONVENIENCE METHODS
     */



    /*
     * QUERIES
     */

    /**
     * Find all claims for given contribution
     *
     * @param contribution a contribution id
     * @return a (possibly empty) list of claims
     */
    public static List<ContributionHasClaim> findByContribution(Long contribution) {
        List<ContributionHasClaim> result = find.where().eq("id_contribution", contribution).orderBy("version desc").findList();
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Find all claims for given contributor
     *
     * @param contributor a contributor id
     * @return a (possibly empty) list of claims
     */
    public static List<ContributionHasClaim> findByContributor(Long contributor) {
        List<ContributionHasClaim> result = find.where().eq("id_contributor", contributor).orderBy("version desc").findList();
        return result != null ? result : new ArrayList<>();
    }

    public static List<ContributionHasClaim> find(Long contributor, int fromIndex, int toIndex){
        List<Integer> groups = ContributorHasGroup.byContributor(contributor)
                .stream()
                .map(g -> g.getGroup().getIdGroup())
                .collect(Collectors.toList());

        return find.where()
                .in("id_group", groups)
                .orderBy("version desc")
                .setFirstRow(fromIndex)
                .setMaxRows(toIndex)
                .findList();
    }

    /**
     * Retrieve a claim by contribution and contributor ids
     *
     * @param contribution a contribution id
     * @return a claim
     */
    public static ContributionHasClaim retrieveByContributionAndContributor(Long contribution, Long contributor) {
        return find.where().eq("id_contribution", contribution).eq("id_contributor", contributor).findUnique();
    }

    public static int getNumberOfClaims(Long contributor) {
        String select = "SELECT count(distinct chc.id_contribution) as 'quantity' FROM contribution_has_claim chc " +
                "inner join contributor_has_group chg on chg.id_group = chc.id_group " +
                "where chg.id_contributor = " + contributor + " and chg.id_role >= " + EContributorRole.OWNER.id();
        return Ebean.createSqlQuery(select).findUnique().getInteger("quantity");
    }

    /**
     * The primary key class for the contribution_has_claim joint table.
     *
     * @author Martin Rouffiange
     */
    @Embeddable
    public static class ContributionHasClaimPK extends Model {

        @Column(name = "id_contribution", insertable = false, updatable = false, unique = true, nullable = false)
        private Long idContribution;

        @Column(name = "id_contributor", insertable = false, updatable = false, unique = true, nullable = false)
        private Long idContributor;

        @Version
        @Column(name = "version", insertable = false, updatable = false, unique = true, nullable = false)
        private Timestamp version;

        /**
         * Constructor
         *
         * @param idContribution a contribution id
         * @param idContributor a contributor id
         */
        public ContributionHasClaimPK(Long idContribution, Long idContributor) {
            this.idContribution = idContribution;
            this.idContributor = idContributor;
        }

        /**
         * Get the contribution id
         *
         * @return a contribution id
         */
        public Long getIdContribution() {
            return this.idContribution;
        }

        /**
         * Set the contribution id
         *
         * @param idContribution a contribution id
         */
        public void setIdContribution(Long idContribution) {
            this.idContribution = idContribution;
        }

        /**
         * Get the contributor id
         *
         * @return a contributor id
         */
        public Long getIdContributor() {
            return this.idContributor;
        }

        /**
         * Set the contributor id
         *
         * @param idContributor a contributor id
         */
        public void setIdContributor(Long idContributor) {
            this.idContributor = idContributor;
        }

        /**
         * Get version timestamp, ie, when linked contributor made a modification to linked contribution
         *
         * @return a timestamp
         */
        public Timestamp getVersion() {
            return version;
        }

        /**
         * Set version timestamp, ie, when linked contributor made a modification to linked contribution
         *
         * @param version a timestamp
         */
        public void setVersion(Timestamp version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "chcPK:" + getIdContribution() + "-" + getIdContributor() + "-" + getVersion();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ContributionHasClaimPK)) {
                return false;
            }
            ContributionHasClaimPK castOther = (ContributionHasClaimPK) other;
            return this.idContribution.equals(castOther.idContribution)
                    && this.idContributor.equals(castOther.idContributor)
                    && this.version.equals(castOther.version);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 17;
            hash = hash * prime + this.idContribution.hashCode();
            hash = hash * prime + this.idContributor.hashCode();
            return hash * prime + this.version.hashCode();
        }
    }
}
