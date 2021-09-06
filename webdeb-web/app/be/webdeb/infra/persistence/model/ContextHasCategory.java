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
import be.webdeb.core.api.debate.EDebateType;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the context_has_category database table. Holds a link between a context contribution
 * and a tag category.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "context_has_category")
public class ContextHasCategory extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ContextHasCategory > find = new Model.Finder<>(ContextHasCategory .class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_context", nullable = false)
    private Contribution context;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", nullable = false)
    private Tag category;

    @Column(name = "nb_order", nullable = false)
    private Integer order;

    public ContextHasCategory(Contribution context, Tag category) {
        this.context = context;
        this.category = category;
        this.order = 0;
    }

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the context has category id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the context has category id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get "supertype" contribution object
     *
     * @return a contribution
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set "supertype" contribution object
     *
     * @param contribution a contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get the context of this context has category
     *
     * @return the contribution that is the context where the link exists
     */
    public Contribution getContext() {
        return context;
    }

    /**
     * Set the context of this context has category
     *
     * @param context a contribution that is the context where the link exists
     */
    public void setContext(Contribution context) {
        this.context = context;
    }

    /**
     * Get the tag category where this link is, if any
     *
     * @return tag category of the link, may be null
     */
    public Tag getCategory() {
        return category;
    }

    /**
     * Set the tag category where this link is
     *
     * @param category the tag category of the link
     */
    public void setCategory(Tag category) {
        this.category = category;
    }

    /**
     * Get the order of the category in its context
     *
     * @return the order of the category
     */
    public int getOrder() {
        return order;
    }

    /**
     * Set the order of the category in its context
     *
     * @param order the order of the category
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of this context has category
     *
     * @return a timestamp with the latest update moment of this context has category
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        StringBuilder builder = new StringBuilder(", context: [")
                .append(getContext().getIdContribution()).append("] ").append(getContext().getSortkey())
                .append(", category: ").append(getCategory() != null ? "[" + getCategory().getIdContribution() + "]" : "null")
                .append(", order :").append(order);

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Get the existing relation by its id, if any
     *
     * @param id a link id
     * @return the existing context has category if exists, null otherwise
     */
    public static ContextHasCategory findById(Long id) {
        return find.where().eq("id_contribution", id).findUnique();
    }

    /**
     * Get the existing relation between given context contribution id and tag category id, if any
     *
     * @param context a context contribution id
     * @param category a tag category id
     * @return the existing context has category if exists, null otherwise
     */
    public static ContextHasCategory findByContextAndCategory(Long context, Long category) {
        return find.where().eq("id_context", context).eq("id_category", category).findUnique();
    }

    /**
     * Get the list of context has categories of the given context contribution, if any
     *
     * @param contribution a context contribution id
     * @return the possibly empty list of context has categories for the given context contribution
     */
    public static List<ContextHasCategory> findByContext(Long contribution) {
        String select = "select distinct chc.id_contribution from context_has_category chc " +
                    "where chc.id_context = " + contribution +
                    " order by chc.nb_order";

        return Ebean.find(ContextHasCategory.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of context has categories with the given category, if any
     *
     * @param category a tag id
     * @return the possibly empty list of context has categories
     */
    public static List<ContextHasCategory> findByCategory(Long category) {
        String select = "select distinct chc.id_contribution from context_has_category chc " +
                "where chc.id_category = " + category;

        return Ebean.find(ContextHasCategory.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of context has categories of the given context contribution, if any
     *
     * @param contribution a tag debate id
     * @return the possibly empty list of context has categories for the given context contribution
     */
    public static List<Tag> getTagDebateFakeCategories(Long contribution) {
        String select = "select distinct t.id_contribution from tag t " +
                "inner join contribution_has_tag cht on cht.id_tag = " + contribution +
                " inner join citation ci on ci.id_contribution = cht.id_contribution " +
                "inner join contribution_has_tag cht2 on cht2.id_contribution = ci.id_contribution " +
                "where t.id_contribution = cht2.id_tag and t.id_contribution != " + contribution;

        return Ebean.find(Tag.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();

    }
}
