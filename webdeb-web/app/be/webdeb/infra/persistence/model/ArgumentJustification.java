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
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.debate.EDebateShade;
import be.webdeb.core.api.debate.EDebateType;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the argument_justification_link database table. Holds a justification link between
 * an argument and the context contribution (and possibly a category and a super argument),
 * as well as the type of link between them.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see EJustificationLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "argument_justification_link")
public class ArgumentJustification extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ArgumentJustification> find = new Model.Finder<>(ArgumentJustification.class);

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
    @JoinColumn(name = "id_sub_context")
    private Tag subContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category")
    private Tag category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_super_argument")
    private Argument superArgument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_argument", nullable = false)
    private Argument argument;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_shade", nullable = false)
    private TJustificationLinkShadeType shade;

    @Column(name = "nb_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "argument", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ContextHasSubDebate> debates;

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the justification link id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the justification link id
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
     * Get the context of this justification link
     *
     * @return the contribution that is the context where the link exists
     */
    public Contribution getContext() {
        return context;
    }

    /**
     * Set the context of this justification link
     *
     * @param context a contribution that is the context where the link exists
     */
    public void setContext(Contribution context) {
        this.context = context;
    }

    /**
     * Get the tag sub context where this link is, if any
     *
     * @return tag sub context of the link, may be null
     */
    public Tag getSubContext() {
        return subContext;
    }

    /**
     * Set the tag sub context where this link is
     *
     * @param subContext the tag sub context of the link
     */
    public void setSubContext(Tag subContext) {
        this.subContext = subContext;
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
     * Get the argument that is the parent argument of the link, if any
     *
     * @return the super argument, may be null
     */
    public Argument getSuperArgument() {
        return superArgument;
    }

    /**
     * Set the argument that is the parent argument of the link
     *
     * @param superArgument the super argument
     */
    public void setSuperArgument(Argument superArgument) {
        this.superArgument = superArgument;
    }

    /**
     * Get the argument of the link
     *
     * @return the link argument
     */
    public Argument getArgument() {
        return argument;
    }

    /**
     * Set the argument of the link
     *
     * @param argument  the link argument
     */
    public void setArgument(Argument argument) {
        this.argument = argument;
    }

    /**
     * Get the justification link shade
     *
     * @return a link shade
     */
    public TJustificationLinkShadeType getShade() {
        return shade;
    }

    /**
     * Set the justification link shade
     *
     * @param shade a link shade
     */
    public void setShade(TJustificationLinkShadeType shade) {
        this.shade = shade;
    }

    /**
     * Get the order of the justification link in its context
     *
     * @return the order of the link
     */
    public int getOrder() {
        return order;
    }

    /**
     * Set the order of the justification link in its context
     *
     * @param order the order of the link
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the list of debates linked to this argument link
     *
     * @return the list of debates
     */
    public List<ContextHasSubDebate> getDebates() {
        return debates;
    }

    /**
     * Get one debate added from debate justification
     *
     * @return a debate if any
     */
    public Debate getDebate() {
        if(debates == null || debates.isEmpty())
            return null;

        return debates.get(0).getDebate();
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of this argument link
     *
     * @return a timestamp with the latest update moment of this argument link
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        StringBuilder builder = new StringBuilder(", context: [")
                .append(getContext().getIdContribution()).append("] ").append(getContext().getSortkey())
                .append(", sub context: ").append(getSubContext() != null ? "[" + getSubContext().getIdContribution() + "]" : "null")
                .append(", category: ").append(getCategory() != null ? "[" + getCategory().getIdContribution() + "]" : "null")
                .append(getCategory() != null ? getCategory().getDefaultName() : "")
                .append(", super argument: ").append(getSuperArgument() != null ? "[" + getSuperArgument().getIdContribution() + "]" : "null")
                .append(getSuperArgument() != null ? getSuperArgument().getDictionary().getTitle() : "")
                .append(", argument: [").append(getArgument().getIdContribution()).append("] ").append(getArgument().getDictionary().getTitle())
                .append(", shade: [").append(getShade().getIdShade()).append("] ").append(getShade().getEn())
                .append(", order :").append(order);

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Check that this argument justification is unique. Its context id, tag category id, super argument id
     * and argument id must not already linked.
     *
     * @return true if this link is unique
     */
    public boolean isUnique(){
        return existsInOtherContextAndSubContext(getContext().getIdContribution(), getSubContext().getIdContribution());
    }

    /**
     * Check that this argument justification doesn't exists with given category
     *
     * @param category a tag id
     * @return true if this link exists in the given category
     */
    public boolean existsWithOtherCategory(Long category){
        return findUnique(getContext().getIdContribution(),
                getSubContext() != null ? getSubContext().getIdContribution() : null,
                category,
                getSuperArgument() != null ? getSuperArgument().getIdContribution() : null,
                getArgument().getIdContribution(),
                shade.getIdShade())
                != null;
    }

    /**
     * Check that this argument justification doesn't exists in given context and sub context yet.
     *
     * @return true if this link already exists
     */
    public boolean existsInOtherContextAndSubContext(Long contextId, Long subContextId){
        return findUnique(contextId,
                subContextId,
                getCategory() != null ? getCategory().getIdContribution() : null,
                getSuperArgument() != null ? getSuperArgument().getIdContribution() : null,
                getArgument().getIdContribution(),
                shade.getIdShade())
                != null;
    }

    /**
     * Check that this argument justification doesn't exists in given context yet.
     *
     * @return true if this link exists in the given context
     */
    public boolean existsInOtherContext(Long contextId){
        return existsInOtherContextAndSubContext(contextId, null);
    }

    /**
     * Retrieve a justification link by its id
     *
     * @param id the justification link id
     * @return the justification link corresponding to that id, null otherwise
     */
    public static ArgumentJustification findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find unique argument justification by its super context id, context id, tag category id, super argument id, argument id and shade id.
     * This signature must be unique.
     *
     * @param contextId the context contribution id
     * @param subContextId the tag sub context id
     * @param tagId the tag category id
     * @param superArgumentId the super argument of the link id
     * @param argumentId the argument of the link id
     * @param shadeId the link shade id
     * @return an argument justification link corresponding to given contribution ids
     */
    public static ArgumentJustification findUnique(Long contextId, Long subContextId, Long tagId, Long superArgumentId, Long argumentId, int shadeId){
        List<ArgumentJustification> links = find.where()
                .eq("id_context", isBlank(contextId) ? null : contextId)
                .eq("id_sub_context", isBlank(subContextId) ? null : subContextId)
                .eq("id_category", isBlank(tagId) ? null : tagId)
                .eq("id_super_argument", isBlank(superArgumentId) ? null : superArgumentId)
                .eq("id_argument", isBlank(argumentId) ? null : argumentId)
                .eq("id_shade", isBlank(shadeId) ? null : shadeId)
                .findList();

        return links.isEmpty() ? null : links.get(0);
    }

    /**
     * Find the list of argument justification links where the given contribution is implicated
     *
     * @param contribution a contribution id
     * @param type a contribution enum type
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContribution(Long contribution, EContributionType type){
        return findLinksForContribution(contribution, type, false);
    }

    /**
     * Find the list of argument justification links where the given contribution is implicated
     *
     * @param contribution a contribution id
     * @param type a contribution enum type
     * @param forSuperArgument look for super argument
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContribution(Long contribution, EContributionType type, boolean forSuperArgument){
        switch (type) {
            case DEBATE:
            case TEXT:
                return findLinksForContext(contribution);
            case TAG:
                return findLinksForTag(contribution);
            case DEBATE_HAS_TAG_DEBATE:
                DebateLink link = DebateLink.findById(contribution);
                if(link != null)
                    return findLinksForContextAndSubContext(link.getDebate().getIdContribution(), contribution);
                break;
            case ARGUMENT:
                return forSuperArgument ?
                        findLinksForSuperArgument(contribution) :
                        findLinksForSimpleArgument(contribution);
        }

        return new ArrayList<>();
    }

    /**
     * Get the list of argument justification link in the given super context and sub context
     *
     * @param context a tag contribution id
     * @param subContext the tag sub context id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContextAndSubContext(Long context, Long subContext){
        return find.where().eq("id_context", context).eq("id_sub_context", subContext).findList();
    }

    /**
     * Get the list of argument justification link in the given context
     *
     * @param context a context contribution id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContext(Long context){
        return find.where().eq("id_context", context).findList();
    }

    /**
     * Get the list of argument justification link in the given context and category
     *
     * @param context a context contribution id
     * @param category a category id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContextAndCategory(Long context, Long category){
        return find.where().eq("id_context", context).eq("id_category", category).findList();
    }

    /**
     * Get the list of argument justification link in the given context, sub context, category and super argument
     *
     * @param context a context contribution id
     * @param subContext the tag sub context id
     * @param category a context contribution tag category id
     * @param superArgument an argument id
     * @param shadeId the link shade id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForContext(Long context, Long subContext, Long category, Long superArgument, Integer shadeId){
        ExpressionList<ArgumentJustification> where =  find.where()
                .eq("id_context", context)
                .eq("id_sub_context", isBlank(subContext) ? null : subContext)
                .eq("id_category", isBlank(category) ? null : category)
                .eq("id_super_argument", isBlank(superArgument) ? null : superArgument);

        if(!isBlank(shadeId)) {
            where.eq("id_shade", shadeId);
        }

        return where
                .orderBy().asc("order")
                .findList();
    }

    /**
     * Get the list of argument justification link in the given tag category
     *
     * @param tag a tag category id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForTag(Long tag){
        return find.where().eq("id_category", tag).findList();
    }

    /**
     * Get the list of argument justification link where the given argument id is super argument or the argument
     *
     * @param argument an argument id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForArgument(Long argument){
        return find.where().eq("id_super_argument", argument)
                .or().eq("id_argument", argument).findList();
    }

    /**
     * Get the list of argument justification link where the given argument id is super argument
     *
     * @param argument an argument id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForSuperArgument(Long argument){
        return find.where().eq("id_super_argument", argument).findList();
    }

    /**
     * Get the list of argument justification link where the given argument id is the argument
     *
     * @param argument an argument id
     * @return a possibly empty list of argument justification links
     */
    public static List<ArgumentJustification> findLinksForSimpleArgument(Long argument){
        return find.where().eq("id_argument", argument).findList();
    }

    /**
     * Get the last order number in the context
     *
     * @param context the context contribution id
     * @param subContext the tag sub context id
     * @param category the tag category id
     * @param superArgument the super argument of the link id
     * @param shade the link shade id
     * @return the last order
     */
    public static int getMaxArgumentJustificationLinkOrder(Long context, Long subContext, Long category, Long superArgument, int shade) {
        String select = "SELECT max(nb_order) as 'max_order' FROM argument_justification_link l " +
                "where l.id_context = " + context +
                " and l.id_sub_context " + isNotBlankOrNull(subContext) +
                " and l.id_category " + isNotBlankOrNull(category) +
                " and l.id_super_argument " + isNotBlankOrNull(superArgument) +
                " and l.id_shade = " + shade;

        Integer maxOrder = Ebean.createSqlQuery(select).findUnique().getInteger("max_order");
        return maxOrder == null ? -1 : maxOrder;
    }

}
