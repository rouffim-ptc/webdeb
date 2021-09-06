/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for keep the relevance of a contribution.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "contribution_has_relevance")
public class ContributionHasRelevance extends Model {

    private static final Model.Finder<Long, ContributionHasActor> find = new Model.Finder<>(ContributionHasActor.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @Column(name = "relevance", nullable = false)
    private int relevance;

    @Column(name = "hit")
    private Long hit = 0L;

    @Version
    @Column(name = "version")
    @Unqueryable
    private Timestamp version;


    /**
     * Get id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return this.idContribution;
    }

    /**
     * Set id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the contribution object
     *
     * @return the contribution object
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set the contribution object
     *
     * @param contribution the contribution object
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get the relevance of this contribution. The relevance shows consistency of the contribution.
     *
     * @return the relevance
     */
    public int getRelevance() {
        return relevance;
    }

    /**
     * Set the relevance of this contribution. The relevance shows consistency of the contribution.
     *
     * @param relevance the relevance
     */
    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    /**
     * Get the hit (popularity) value
     *
     * @return the number of hit (amount of visualiation requests) of this contribution
     */
    public Long getHit() {
        return hit;
    }

    /**
     * Set the hit (popularity) value
     *
     * @param hit the number of hit (amount of visualiation requests) of this contribution
     */
    public void setHit(Long hit) {
        this.hit = hit;
    }

    /**
     * Get the current version of this relevance
     *
     * @return a timestamp with the latest update moment of this relevance
     */
    public Timestamp getVersion() {
        return version;
    }

    /**
     * Set the version of this relevance
     *
     * @param version the timestamp with the latest update moment of this relevance
     */
    public void setVersion(Timestamp version) {
        this.version = version;
    }

    /*
     * HELPERS
     */

    /**
     * Increment the amount of hit for this contribution
     */
    public void addHit() {
        setHit(hit + 1);
    }

    /**
     * Decrement the amount of hit for this contribution
     */
    public void removeHit() {
        setHit(hit - 1);
    }
}
