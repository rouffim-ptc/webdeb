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
import java.util.List;


/**
 * The persistent class for temporary contribution from external services (like browser extension) that will be added
 * manually by contributor.
 *
 * Used as temporary repository for an easiest communication between services and the platform.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "external_contribution")
@Unqueryable
public class ExternalContribution extends Model {

    private static final Model.Finder<Long, ExternalContribution> find = new Model.Finder<>(ExternalContribution.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "external_source", nullable = false)
    private TExternalSourceName externalSource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language", nullable = true)
    private TLanguage language;

    @Column(name = "title")
    private String title;

    @Column(name = "rejected")
    private int rejected;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_internal_contribution")
    private Contribution internalContribution;

    /**
     * Get the "supertype" contribution object
     *
     * @return a contribution object
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set the "supertype" contribution object
     *
     * @param contribution a contribion
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get this text id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set this text id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the external source url
     *
     * @return the external source url
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Set the external source url
     *
     * @param sourceUrl the external source url
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Get the external source where this contribution come from
     *
     * @return the external source
     */
    public TExternalSourceName getExternalSource() {
        return externalSource;
    }

    /**
     * Set the external source where this contribution come from
     *
     * @param externalSource the external source
     */
    public void setExternalSource(TExternalSourceName externalSource) {
        this.externalSource = externalSource;
    }

    /**
     * Get the language of the title of this external contribution
     *
     * @return the argument language
     */
    public TLanguage getLanguage() {
        return language;
    }

    /**
     * Set the language of the title of this external contribution
     *
     * @param language a language
     */
    public void setLanguage(TLanguage language) {
        this.language = language;
    }

    /**
     * Get the equivalent internal contribution to this one if any
     *
     * @return the equivalent internal contribution
     */
    public Contribution getInternalContribution() {
        return internalContribution;
    }

    /**
     * Set the equivalent internal contribution
     *
     * @param internalContribution an equivalent internal contribution
     */
    public void setInternalContribution(Contribution internalContribution) {
        this.internalContribution = internalContribution;
    }

    /**
     * Get the flag saying if this external contribution has been rejected (as a false positive)
     *
     * @return true if this external contribution has been rejected
     */
    public boolean getRejected() {
        return rejected == 1;
    }

    /**
     * Set the flag saying if this external contribution has been rejected (as a false positive)
     *
     * @param rejected true if this external contribution has been rejected
     */
    public void setRejected(boolean rejected) {
        this.rejected = rejected ? 1 : 0;
    }

    /**
     * Get the title of this external contribution
     *
     * @return contribution title
     */
    public String getTitle(){
        return title;
    }

    /**
     * Set the title of this external contribution
     *
     * @param title the contribution title
     */
    public void setTitle(String title){
        this.title = title;
    }


    /*
     * QUERIES
     */

    /**
     * Retrieve an external contribution by its id
     *
     * @param id an id
     * @return the external contribution corresponding to the given id, null if not found
     */
    public static ExternalContribution findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }


    /**
     * Retrieve an external contribution by its url and contribution type
     *
     * @param url an url
     * @param type a contribution type
     * @return the external contribution corresponding to the given url and type, null if not found
     */
    public static ExternalContribution findByUrlAndContributionType(String url, EContributionType type) {
        return null;
    }
}