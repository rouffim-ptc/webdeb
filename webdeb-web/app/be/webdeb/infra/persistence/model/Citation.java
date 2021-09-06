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

import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the citation database table, conceptual subtype of contribution.
 * Always linked to a text (pointing to start and end indices in that text). Can be part of justification link
 * in a context contribution (debate / text)
 *
 * Contains full-text citation instead of simple offsets in text because a text content may be split into many
 * user-dependent files and crossing over all of them would be a bad idea and some citation may come from PDF files.
 * Also, speeds up annotation processes for text files.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "citation")
public class Citation extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, Citation> find = new Model.Finder<>(Citation.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @Column(name = "original_excerpt", length = 512, nullable = false)
    private String originalExcerpt;

    @Column(name = "working_excerpt", length = 512, nullable = false)
    private String workingExcerpt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_language", nullable = false)
    private TLanguage language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_text", nullable = false)
    private Text text;

    @Transient
    @Unqueryable
    private Long linkId;

    @Transient
    @Unqueryable
    private Integer nb_contributions;

    /**
     * Get parent contribution object
     *
     * @return the parent contribution
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set parent contribution object
     *
     * @param contribution the parent contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get the id of argument
     *
     * @return the argument id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the id of argument
     *
     * @param idContribution the argument id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the original citation
     *
     * @return the original citation
     */
    public String getOriginalExcerpt() {
        return originalExcerpt;
    }

    /**
     * Set the original citation
     *
     * @param  originalExcerpt the original citation
     */
    public void setOriginalExcerpt(String originalExcerpt) {
        this.originalExcerpt =  originalExcerpt;
    }

    /**
     * Get the working citation
     *
     * @return the working citation
     */
    public String getWorkingExcerpt() {
        return workingExcerpt;
    }

    /**
     * Set the working citation
     *
     * @param workingExcerpt the working citation
     */
    public void setWorkingExcerpt(String workingExcerpt) {
        this.workingExcerpt = workingExcerpt;
    }

    /**
     * Get the language of the title of this citation
     *
     * @return the citation language
     */
    public TLanguage getLanguage() {
        return language;
    }

    /**
     * Set the language of the title of this citation
     *
     * @param language a language
     */
    public void setLanguage(TLanguage language) {
        this.language = language;
    }

    /**
     * Get the text from which this argument has been extracted
     *
     * @return a text
     */
    public Text getText() {
        return text;
    }

    /**
     * Set the text from which this argument has bee,n extracted
     *
     * @param text the owning text
     */
    public void setText(Text text) {
        this.text = text;
    }

    /**
     * Get the current version of this argument
     *
     * @return a timestamp with the latest update moment of this argument
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    /**
     * Get the link id where the citation comes from, if needed
     *
     * @return the link id
     */
    public Long getLinkId() {
        return linkId;
    }

    /**
     * Set the link id where the citation comes from
     *
     * @param linkId the link id
     */
    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    /**
     * Get the number of contributions if this data is asked in a query
     *
     * @return the number of contributions
     */
    public Integer getNbContributions() {
        return nb_contributions;
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        StringBuilder builder =  new StringBuilder(", original citation: [").append(getOriginalExcerpt())
                .append("], working citation: [").append(getWorkingExcerpt())
                .append("], language: ").append(getLanguage().getCode())
                .append(", from text: [").append(getText().getContribution().getIdContribution());

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve an citation by its id
     *
     * @param id an id
     * @return the citation corresponding to the given id, null if not found
     */
    public static Citation findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve citation by original or working excerpt
     *
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of citations corresponding with term and lang
     */
    public static List<Citation> searchByExcerpt(SearchContainer query){
        String token = getStrictSearchToken(query.getTerm());
        String select = "select distinct ci.id_contribution from citation ci " +
                "inner join contribution c on c.id_contribution = ci.id_contribution " +
                "where (ci.original_excerpt like '%" + token + "%' or ci.working_excerpt like '%" + token + "%') " +
                (query.getLang() != null ? "and ci.id_language = '" + query.getLang() + "'" : "") +
                addInTextLine(query) +
                checkExistingLinkInSearch(query) +
                getOrderByContributionDate() +
                getSearchLimit(query);
        logger.debug(select);
        return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Retrieve citation by given texts
     *
     * @param textIds a list of text id
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of citations from given texts
     */
    public static List<Citation> searchByTextIds(List<String> textIds, SearchContainer query){
        if(!textIds.isEmpty()) {
            String select = "select distinct ci.id_contribution from citation ci " +
                    "inner join contribution c on co.id_contribution = ci.id_contribution " +
                    "where ci.id_text in (" + String.join(",", textIds) + ") " +
                    checkExistingLinkInSearch(query) +
                    getOrderByContributionDate() +
                    getSearchLimit(query);

            return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve citation by given debates
     *
     * @param debateIds a list of debate id
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of citations from given debates
     */
    public static List<Citation> searchByDebateIds(List<String> debateIds, SearchContainer query){
        if(!debateIds.isEmpty()) {
            String select = "select distinct ci.id_contribution from citation ci " +
                    "inner join contribution c on c.id_contribution = ci.id_contribution " +
                    "inner join citation_justification_link l on ci.id_contribution = l.id_citation " +
                    "where l.id_context in (" + String.join(",", debateIds) + ") " +
                    addInTextLine(query) +
                    checkExistingLinkInSearch(query) +
                    getOrderByContributionDate() +
                    getSearchLimit(query);

            return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve citation by given tags
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of citations from given tags
     */
    public static List<Citation> searchByTagIds(List<String> tagIds, SearchContainer query){
        if(!tagIds.isEmpty()) {
            String select = "select distinct ci.id_contribution from citation ci " +
                    "inner join contribution c on c.id_contribution = ci.id_contribution " +
                    "inner join contribution_has_tag t on ci.id_contribution = t.id_contribution " +
                    "where t.id_tag in (" + String.join(",", tagIds) + ") " +
                    addInTextLine(query) +
                    checkExistingLinkInSearch(query) +
                    getOrderByContributionDate() +
                    getSearchLimit(query);

            return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve citation by given authors
     *
     * @param authorIds a list of author id
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of citations from given authors
     */
    public static List<Citation> searchByAuthorIds(List<String> authorIds, SearchContainer query){
        if(!authorIds.isEmpty()) {
            String select = "select distinct ci.id_contribution from citation ci " +
                    "inner join contribution c on c.id_contribution = ci.id_contribution " +
                    "inner join contribution_has_actor a on ci.id_contribution = a.id_contribution " +
                    "where a.id_actor in (" + String.join(",", authorIds) + ") " +
                    addInTextLine(query) +
                    checkExistingLinkInSearch(query) +
                    getOrderByContributionDate() +
                    getSearchLimit(query);
            return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return new ArrayList<>();
    }

  /**
   * Retrieve citation by original or working excerpt and contributor id
   *
   * @param query all the elements needed to perform the search
   * @return a possibly empty list of citations corresponding with term and lang
   */
  public static List<Citation> searchByExcerptAndContributor(SearchContainer query){
    String token = getStrictSearchToken(query.getTerm());
    String select = "select distinct ci.id_contribution from citation ci " +
            "inner join contribution c on c.id_contribution = ci.id_contribution " +
            "inner join contribution_has_contributor chc on ci.id_contribution = chc.id_contribution " +
            "where (ci.original_excerpt like '%" + token + "%' or ci.working_excerpt like '%" + token + "%') " +
            (query.getLang() != null ? "and ci.id_language = '" + query.getLang() + "'" : "")  + " " +
            "and chc.id_contributor = " + query.getContributor() + " " +
            addInTextLine(query) +
            checkExistingLinkInSearch(query) +
            getOrderByContributionDate() +
            getSearchLimit(query);

    return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

    /**
     * Retrieve the list of citations coming from given Text limited by index and with filters
     *
     * @param query all the elements needed to perform the search
     * @return a possibly empty list of Citation that have been defined from given Text
     */
    public static List<Citation> findCitationsFromText(SearchContainer query){
        String select = addFiltersToSql(
                "SELECT distinct ci.id_contribution FROM citation ci " +
                "inner join contribution c on ci.id_contribution = c.id_contribution " +
                "where ci.id_text = " + query.getContext() + " order by c.version desc " +
                getSearchLimit(query),
                query.getFilters());
        return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of citations in a tag
     *
     * @param tag a tag id
     * @return a possibly empty list of citations links
     */
    public static List<Citation> findCitationsFromTag(Long tag){
        String select = "SELECT c.id_contribution FROM citation ci " +
                "inner join contribution c on ci.id_contribution = c.id_contribution " +
                "left join contribution_has_tag cht on cht.id_contribution = c.id_contribution " +
                "where cht.id_tag = " + tag + " order by c.version desc";
        return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of citations in a tag debate, and a category if needed
     *
     * @param tag a tag contribution id
     * @param text a text contribution id
     * @return a possibly empty list of citations
     */
    public static List<Citation> findCitationsFromTagAndText(Long tag, Long text){
        String select = "select distinct c.id_contribution from tag t " +
                "inner join contribution_has_tag cht on cht.id_tag = t.id_contribution " +
                "inner join citation c on c.id_contribution  = cht.id_contribution " +
                "where cht.id_tag = " + tag + " and c.id_text = " + text;
        return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Get the list of text's citations where given actor is author
     *
     * @param actor an actor id
     * @param text a text contribution id
     * @return a possibly empty list of citations
     */
    public static List<Citation> findCitationsFromActorAndText(Long actor, Long text){
        String select = "select distinct ci.id_contribution from text t " +
                "inner join citation ci on t.id_contribution = ci.id_text " +
                "left join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
                "where t.id_contribution = " + text +
                " and cha.id_actor = " + actor + " and cha.is_author = 1 ";
        return Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Find a random debate linked to given citation
     *
     * @param contributorId the contributor id
     * @param groupId the group id
     * @param rejectedDebate a debate id to not find
     * @return a debate linked to given contribution
     */
    public List<Debate> findLinkedDebates(Long contributorId, int groupId, Long rejectedDebate) {
        return Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(getLinkedDebatesQuery(contributorId, groupId, rejectedDebate)).create()).findList();
    }

    /**
     * Find a list debates linked to given citation
     *
     * @param contributorId the contributor id
     * @param groupId the group id
     * @param rejectedDebate a debate id to not find
     * @return a possibly empty debates linked to given contribution
     */
    public Debate findLinkedDebate(Long contributorId, int groupId, Long rejectedDebate) {
        String select = getLinkedDebatesQuery(contributorId, groupId, rejectedDebate) + " order by rand() limit 1";
        return Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(select).create()).findUnique();
    }

    private String getLinkedDebatesQuery(Long contributorId, int groupId, Long rejectedDebate){
        return "SELECT d.id_contribution, count(distinct cjl.id_contribution) as nb_justifications, count(distinct cpl.id_contribution) as nb_positions from debate d " +
                "left join (" +
                " select cjl.id_contribution, cjl.id_context " +
                "    from citation_justification_link cjl " +
                "    where cjl.id_citation = " + idContribution +
                ") cjl on cjl.id_context = d.id_contribution " +
                "left join (" +
                " select cpl.id_contribution, cpl.id_debate " +
                "    from citation_position_link cpl " +
                "    where cpl.id_citation = " + idContribution +
                ") cpl on cpl.id_debate = d.id_contribution " +
                getContributionStatsJoins("d.id_contribution") +
                getContributionStatsJoins("cjl.id_contribution", "1") +
                getContributionStatsJoins("cpl.id_contribution", "2") +
                " where " +
                getContributionStatsWhereClause(contributorId, groupId, "", true) +
                getContributionStatsWhereClause(contributorId, groupId, "1") +
                getContributionStatsWhereClause(contributorId, groupId, "2") +
                (rejectedDebate != null ? " and d.id_contribution != " + rejectedDebate : "") +
                " group by d.id_contribution " +
                "having (nb_justifications > 0 or nb_positions > 0)";
    }

    /**
     * Add to where clause of searchBy line to check that the citation is not already linked to given datas
     *
     * @param query all informations about the search
     * @return the sql line
     */
    private static String checkExistingLinkInSearch(SearchContainer query) {
        return query.getAlliesOpponentsType() == EAlliesOpponentsType.POSITIONS ?
                "and (select count(distinct cpl.id_contribution) from citation_position_link cpl " +
                "where cpl.id_debate " + isNotBlankOrNull(query.getContext()) + " and cpl.id_sub_debate " + isNotBlankOrNull(query.getSubContext()) +
                " and cpl.id_shade " + isNotBlankOrNull(query.getShade()) + " and cpl.id_citation = ci.id_contribution) = 0 " :
                "and (select count(distinct chl.id_contribution) from citation_justification_link chl " +
                "where chl.id_context " + isNotBlankOrNull(query.getContext()) + " and chl.id_sub_context " + isNotBlankOrNull(query.getSubContext()) +
                " and chl.id_category " + isNotBlankOrNull(query.getCategory()) + " and chl.id_argument " + isNotBlankOrNull(query.getSuperArgument()) +
                " and chl.id_shade " + isNotBlankOrNull(query.getShade()) + " and chl.id_citation = ci.id_contribution) = 0 ";
    }

    /**
     * Add constrain to only search on text citation
     *
     * @param query all informations about the search
     * @return the sql line
     */
    private static String addInTextLine(SearchContainer query) {
        return query.isOnlyInText() ? " and ci.id_text = " + query.getContext() + " " : "";
    }

    /**
     * Get a randomly chosen citation from the database
     *
     * @return a random Citation
     */
    public static Citation random() {
        return random(false);
    }

    /**
     * Get a randomly chosen citation from the database
     *
     * @param onlyNew true for only new contributions
     * @return a random Citation
     */
    public static Citation random(boolean onlyNew) {
        return findById(random(EContributionType.CITATION, onlyNew));
    }

    /**
     * Get a citation suggestion for the given contribution. This citation could be linked to given contribution, or
     * can be a new citation.
     *
     * @param idContribution the related contribution for which we need a suggstion
     * @return a Citation
     */
    public static Citation getSuggestionCitation(Long idContribution) {
        String select = getSuggestionCitationRequest(idContribution) + " order by rand() limit 1";

        List<Citation> suggested = Ebean.find(Citation.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();

        return suggested.isEmpty() ? random(true) : suggested.get(0);
    }

}
