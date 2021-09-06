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

import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EFilterKey;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;

import javax.persistence.MappedSuperclass;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The abstract class for regroup the main behavior between class models
 *
 * @author Martin Rouffiange
 */
@MappedSuperclass
@Unqueryable
public abstract class WebdebModel extends Model {

    private static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Get the string to join contribution in group table
     *
     * @return the corresponding query string
     */
    static String getContributionStatsJoins(){
        return getContributionStatsJoins("c.id_contribution");
    }

    /**
     * Get the string to join contribution in group table
     *
     * @param toJoin the key to join
     * @return the corresponding query string
     */
    static String getContributionStatsJoins(String toJoin){
        return getContributionStatsJoins(toJoin, "");
    }

    /**
     * Get the string to join contribution in group table
     *
     * @param toJoin the key to join
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    static String getContributionStatsJoins(String toJoin, String alias){
        return joinContributionInGroup(toJoin, alias) + joinContributionHasContributor(toJoin, alias);
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param contributorId a contributor id
     * @param groupId a group id
     * @return the corresponding query string
     */
    static String getContributionStatsWhereClause(Long contributorId, int groupId){
        return getContributionStatsWhereClause(contributorId, groupId, "");
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param query the query needed to perform the selection
     * @return the corresponding query string
     */
    static String getContributionStatsWhereClause(SearchContainer query){
        return getContributionStatsWhereClause(query, "");
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param query the query needed to perform the selection
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    static String getContributionStatsWhereClause(SearchContainer query, String alias){
        return getContributionStatsWhereClause(query.getContributor(), query.getGroup(), alias);
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param contributorId a contributor id
     * @param groupId a group id
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    static String getContributionStatsWhereClause(Long contributorId, int groupId, String alias){
        return getContributionStatsWhereClause(contributorId, groupId, alias, false);
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param contributorId a contributor id
     * @param groupId a group id
     * @param alias an alias for join if needed
     * @param withoutAnd if it must not begins with and
     * @return the corresponding query string
     */
    static String getContributionStatsWhereClause(Long contributorId, int groupId, String alias, boolean withoutAnd){
        return " " + (withoutAnd ? "" : "and") + " (" + contributionInGroupRequest(groupId, alias) + " or " + contributionHasContributor(contributorId, alias) + ")";
    }

    /**
     * Get the string to join contribution in group table
     *
     * @param toJoin the key to join
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    private static String joinContributionInGroup(String toJoin, String alias){
        return " left join contribution_in_group cig" + alias + " on cig" + alias + ".id_contribution = " + toJoin;
    }

    /**
     * Get the string to put in a where clause to only match contribution in given group id
     *
     * @param groupId a group id
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    private static String contributionInGroupRequest(int groupId, String alias){
        return " cig" + alias + ".id_group is null or cig" + alias + ".id_group in (" + String.join(",",
                Group.getVisibleGroupsFor(groupId != -1  ? groupId : Group.getPublicGroup().getIdGroup())) + ") ";
    }

    /**
     * Get the string to join contribution has contributor table
     *
     * @param toJoin the key to join
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    private static String joinContributionHasContributor(String toJoin, String alias){
        return " left join contribution_has_contributor chc" + alias + " on chc" + alias + ".id_contribution = " + toJoin;
    }

    /**
     * Get the string to put in a where clause to only match contribution that can be view by given contributor id
     *
     * @param contributorId a contributor id
     * @param alias an alias for join if needed
     * @return the corresponding query string
     */
    private static String contributionHasContributor(Long contributorId, String alias){
        return " chc" + alias + ".id_contributor = " + contributorId;
    }

    /**
     * Get the string to join contributor has group table
     *
     * @param contributorId a contributor id
     * @return the corresponding query string
     */
    private static String joinContributorHasGroup(Long contributorId){
        return " left join contributor_has_group on contributor_has_group.id_contributor = " + contributorId;
    }

    /**
     * Get the string to put in a where clause to only match contribution that can be viewed by given contributor id
     *
     * @return the corresponding query string
     */
    private static String contributorHasGroup(){
        return " contributor_has_group.id_group = contribution_in_group.id_group";
    }

    /**
     * Transform the given string token to well match in DB.
     *
     * @param token the token to treat
     * @return the treated token
     */
    static String getStrictSearchToken(String token){
        return /* "\"" + */ token.trim()
                // protect single quotes
                .replace("'", "''");
    }

    /**
     * Transform the given string token to well match in DB.
     *
     * @param token the token to treat
     * @return the treated token
     */
    static String getSearchToken(String token){
        return getStrictSearchToken(token)
                .replace(" ", "%");
    }

    /**
     * Get the string to limit a select in sql
     *
     * @param query the query needed to perform the selection
     * @return the corresponding query string
     */
    static String getSearchLimit(SearchContainer query){
        return getSearchLimit(query.getFromIndex(), query.getToIndex());
    }

    /**
     * Get the string to limit a select in sql
     *
     * @param lowerIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param upperIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @return the corresponding query string
     */
    static String getSearchLimit(int lowerIndex, int upperIndex){
        return getSearchLimit(lowerIndex, upperIndex, Integer.MAX_VALUE);
    }

    /**
     * Get the string to limit a select in sql
     *
     * @param lowerIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param upperIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param maxValue the maximum value if upper index is wrong
     * @return the corresponding query string
     */
    static String getSearchLimit(int lowerIndex, int upperIndex, int maxValue){
        return " limit " + (lowerIndex > 0 ? lowerIndex : 0)  +
                "," + (upperIndex > 0 && upperIndex > lowerIndex ? upperIndex - lowerIndex : maxValue);
    }

    protected static boolean isBlank(Long number){
        return number == null || number == -1L;
    }

    protected static String isNotBlankOrNull(Long number){
        return isBlank(number) ? "is null" : "= " + number;
    }

     protected static boolean isBlank(Integer number){
    return number == null || number == -1;
  }

    protected static String isNotBlankOrNull(Integer number){
    return isBlank(number) ? "is null" : "= " + number;
  }

    protected static boolean isBlank(String ch){
        return ch == null || ch.isEmpty();
    }

    /**
     * Get the string to order by contribution date
     *
     * @return the corresponding query string
     */
    static String getOrderByContributionDate(){
        return " order by c.version desc ";
    }

    protected String getModelDescription(Contribution contribution, String othersDatum){
        return getModelDescription(contribution, null, othersDatum, true);
    }

    protected String getModelDescription(Contribution contribution, String name, String othersDatum){
        return getModelDescription(contribution, name, othersDatum, true);
    }

    protected String getModelDescription(Contribution contribution, String name, String othersDatum, boolean withGroups){

        StringBuilder descriptionBuffer = new StringBuilder(getContributionTypeName(contribution) + " [")
                .append(contribution.getIdContribution()).append("]");

        if(name != null)
            descriptionBuffer.append(", name: [").append(name).append("]");

        if(othersDatum != null)
            descriptionBuffer.append(othersDatum);
        
        switch (contribution.getContributionType().getEContributionType()){
            case DEBATE:
            case CITATION:
            case TEXT:
                descriptionBuffer.append("], actors: {").append(contribution.getActors().stream()
                        .map(ContributionHasActor::toString).collect(Collectors.joining(", "))).append("}");

        }

        switch (contribution.getContributionType().getEContributionType()){
            case ACTOR:
            case DEBATE:
            case CITATION:
                if(contribution.getContributionType().getEContributionType() != EContributionType.ACTOR
                || contribution.getActor().getActortype().getEActorType() == EActorType.ORGANIZATION) {
                    descriptionBuffer.append(", places: [").append(
                            contribution.getPlaces().stream()
                                    .map(p -> String.valueOf(p.getId()))
                                    .collect(Collectors.joining(","))).append("]");

                    descriptionBuffer.append(", tags: [").append(
                            contribution.getTags().stream()
                                    .map(f -> String.valueOf(f.getIdContribution()))
                                    .collect(Collectors.joining(","))).append("]");
                }

        }

        descriptionBuffer.append(", groups: [").append(contribution.getGroups().stream()
                        .map(g -> String.valueOf(g.getIdGroup())).collect(Collectors.joining(","))).append("]");

        descriptionBuffer.append(", version: [").append(contribution.getVersion()).append("]");

        return descriptionBuffer.toString();
    }

    protected static String addFiltersToSql(String sql, String filters) {
        return addFiltersToSql(sql, filters, false);
    }

    protected static String addFiltersToSql(String sql, String filters, boolean forAffiliation) {
        if(!isBlank(sql) && !isBlank(filters) && sql.contains(" where ")) {

            try {
                filters = URLDecoder.decode(filters, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) { }

            for(String filter : filters.split(";")) {
                String[] keyValFilter = filter.split(":");

                if(keyValFilter.length == 2) {
                    EFilterKey key = EFilterKey.value(keyValFilter[0]);

                    if(key != null) {
                        List<String> inners = new ArrayList<>();
                        List<String> where = new ArrayList<>();
                        String[] values = keyValFilter[1].split(",");

                        switch (key) {
                            case TEXT_PUBLICATION_DATE:
                            case CITATION_PUBLICATION_DATE:
                                if(values.length == 2) {
                                    if(key == EFilterKey.CITATION_PUBLICATION_DATE)
                                        inners.add("inner join text t on t.id_contribution = ci.id_text");

                                    where.add((values[0].equals("null") ? "" : "t.publication_date >= '" + (values[0]) + "'" +
                                            (values[1].equals("null") ? "" : " and ")) +
                                            (values[1].equals("null") ? "" : "t.publication_date <= '" + (values[1]) + "'"));
                                }
                                break;
                            case AFFILIATION:
                                if(!forAffiliation) {
                                    inners.add("inner join actor_has_affiliation aha on aha.id_actor = a.id_contribution");
                                    where.add("aha.id_actor_as_affiliation = " + values[0]);
                                }
                                break;
                            case AFFILIATION_DATE:
                                if(values.length == 2) {
                                    where.add((values[0].equals("null") ? "" : "aha.start_date >= '" + (values[0]) + "'" +
                                            (values[1].equals("null") ? "" : " and ")) +
                                            (values[1].equals("null") ? "" : "aha.end_date <= '" + (values[1]) + "'"));
                                }
                                break;
                            case AFFILIATION_FUNCTION:
                                if(!forAffiliation)
                                    inners.add("inner join actor_has_affiliation aha on aha.id_actor = a.id_contribution");
                                inners.add("inner join profession_i18names pn on pn.profession = aha.function");
                                where.add("pn.spelling like '" + values[0] + "%'");
                                break;
                            case AFFILIATION_TYPE:
                                if(!forAffiliation)
                                    inners.add("inner join actor_has_affiliation aha on aha.id_actor = a.id_contribution");
                                where.add("aha.type = " + values[0]);
                                break;
                            case AUTHOR:
                                inners.add("inner join contribution_has_actor cha on cha.id_contribution = c.id_contribution");
                                where.add("cha.id_actor = " + values[0]);
                                break;
                            case ACTOR_ROLE:
                                EActorRole role = EActorRole.value(Integer.parseInt(values[0]));
                                if(role != null) {
                                    inners.add("left join (" +
                                            "select id_contribution from contribution_has_actor cha_role " +
                                            "where is_speaker = 1 " +
                                            ") cha_role on cha_role.id_contribution = ci.id_contribution");
                                    where.add("cha_role.id_contribution is " + (role == EActorRole.AUTHOR ? "" : "not") + " null");
                                }
                                break;
                            case TEXT_AUTHOR:
                                inners.add("inner join contribution_has_actor cha on cha.id_contribution = t.id_contribution");
                                where.add("cha.id_actor = " + values[0]);
                                break;
                            case CITATION_AUTHOR:
                                inners.add("inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution");
                                where.add("cha.id_actor = " + values[0]);
                                break;
                            case PLACE:
                                inners.add("inner join contribution_has_place chp on chp.id_contribution = c.id_contribution");
                                inners.add("inner join place on place.id_place = chp.id_place");
                                where.add("(place.id_place = " + values[0] + " or place.id_continent = " + values[0] +
                                        " or place.id_country = " + values[0] + " or place.id_region = " + values[0] +
                                        " or place.id_subregion = " + values[0] + ")");
                                break;
                            case TAG:
                                inners.add("inner join contribution_has_tag cht on cht.id_contribution = c.id_contribution");
                                inners.add("inner join tag_i18names chtn on cht.id_tag = chtn.id_contribution");
                                where.add("chtn.name like '%" + values[0] + "%'");
                                break;
                            case TEXT_TYPE:
                                where.add("t.id_type = " + values[0]);
                                break;
                            case TEXT_SOURCE:
                            case CITATION_SOURCE:
                                if(key == EFilterKey.CITATION_SOURCE)
                                    inners.add("inner join text t on t.id_contribution = ci.id_text");
                                inners.add("inner join text_source_name tsn on tsn.id_source = t.id_source_name");
                                where.add("tsn.name like '%" + values[0] + "%'");
                                break;
                            case FULLTEXT_CITATION:
                                where.add("(ci.original_excerpt like '%" + values[0] + "%' or ci.working_excerpt like '%" + values[0] + "%')");
                                break;
                            case FULLTEXT_TAG:
                                inners.add("inner join tag_i18names tagn on tagn.id_contribution = tag.id_contribution");
                                where.add("tagn.name like '%" + values[0] + "%'");
                                break;
                            case FULLTEXT_TEXT:
                                inners.add("inner join text_i18names tn on tn.id_contribution = t.id_contribution");
                                where.add("tn.spelling like '%" + values[0] + "%'");
                                break;
                            case FULLTEXT_DEBATE:
                                inners.add("inner join argument a on a.id_contribution = d.id_argument");
                                inners.add("inner join argument_dictionary ad on ad.id_contribution = a.id_dictionary");
                                where.add("ad.title like '%" + values[0] + "%'");
                                break;
                            case FULLTEXT_ACTOR:
                                inners.add("inner join actor_i18names an on an.id_contribution = a.id_contribution");
                                where.add("(if(an.first_or_acro is null, an.name, concat(an.first_or_acro, ' ', an.name)) like '%" + values[0] + "%' or " +
                                        "if(an.first_or_acro is null, an.name, concat(an.first_or_acro, ' - ', an.name)) like '%" + values[0] + "%')");
                                break;
                            case GENDER:
                                where.add("p.gender = '" + values[0] + "'");
                                break;
                        }

                        if(!where.isEmpty()) {
                            String[] splitted = sql.split(" where", 2);
                            final String sqlCopy = sql;

                            inners = inners.stream().filter(inner -> !sqlCopy.contains(inner)).collect(Collectors.toList());

                            sql = splitted[0] + " " + String.join(" ", inners) + " where " + String.join(" and ", where) + " and " + splitted[1];
                        }
                    }
                }
            }
        }

        return sql;
    }

    protected static String getSqlFilter(String filters, EFilterKey searchKey) {
        if(!isBlank(filters) &&  searchKey != null) {

            try {
                filters = URLDecoder.decode(filters, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) { }

            for(String filter : filters.split(";")) {
                String[] keyValFilter = filter.split(":");

                if(keyValFilter.length == 2) {
                    EFilterKey key = EFilterKey.value(keyValFilter[0]);

                    if(key == searchKey) {
                        return keyValFilter[1];
                    }
                }
            }
        }

        return null;
    }

    private String getContributionTypeName(Contribution contribution){
        switch (contribution.getContributionType().getEContributionType()){
            case ACTOR:
                return contribution.getActor().getActortype().getEActorType().name().toLowerCase();
            case ARGUMENT:
                return contribution.getArgument().getType().getEArgumentType().name().toLowerCase();
            default :
                return contribution.getContributionType().getEContributionType().name().toLowerCase();

        }
    }

    public static String getSuggestionCitationRequest(Long idContribution) {
        Contribution c = Contribution.findById(idContribution);

        if(c != null) {
            switch (c.getContributionType().getEContributionType()){
                case ACTOR:
                    return "select distinct ci.id_contribution from citation ci " +
                            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = ci.id_contribution " +
                            "where cha.id_actor in ( " +
                            "SELECT distinct cha2.id_actor FROM contribution_has_actor cha1 " +
                            "inner join contribution_has_actor cha2 on cha2.id_contribution = cha1.id_contribution " +
                            "inner join contribution c on cha2.id_contribution = c.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = c.id_contribution " +
                            "where cha1.id_actor = " + idContribution + " and cha2.id_actor != " + idContribution + ") " +
                            " and cig.id_group = " + Group.getPublicGroup().getIdGroup() +
                            " and ci.id_contribution not in  " +
                            "(select distinct cha3.id_contribution from contribution_has_actor cha3 where cha3.id_actor = " + idContribution + ") " +
                            "" +
                            "union select distinct ci.id_contribution from citation ci " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = ci.id_contribution " +
                            "where cht.id_tag in ( " +
                            "select tag.id_contribution from tag " +
                            "inner join contribution_has_tag cht2 on cht2.id_tag = tag.id_contribution " +
                            "inner join citation ci on ci.id_contribution = cht2.id_contribution " +
                            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
                            "where cha.id_actor = " + idContribution + ") " +
                            "and cig.id_group = " + Group.getPublicGroup().getIdGroup() +
                            " and ci.id_contribution not in  " +
                            "(select distinct cha2.id_contribution from contribution_has_actor cha2 where cha2.id_actor = " + idContribution + ")";
                case TEXT:
                    return "select distinct ci.id_contribution from citation ci " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = ci.id_contribution " +
                            "where cht.id_tag in (" +
                            "SELECT distinct cht.id_tag FROM citation ci " +
                            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "where ci.id_text = " + idContribution + ") " +
                            "and ci.id_text != " + idContribution +
                            " and cig.id_group = " + Group.getPublicGroup().getIdGroup();
                case DEBATE:
                    return "select distinct ci.id_contribution from citation ci " +
                            "left join citation_justification_link cjl on cjl.id_citation = ci.id_contribution " +
                            "left join citation_position_link cpl on cpl.id_citation = ci.id_contribution " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = ci.id_contribution " +
                            "where cht.id_tag in ( " +
                            "SELECT distinct cht.id_tag FROM citation ci  " +
                            "left join citation_justification_link cjl on cjl.id_citation = ci.id_contribution " +
                            "left join citation_position_link cpl on cpl.id_citation = ci.id_contribution " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "where cjl.id_context = " + idContribution + " or cpl.id_debate = " + idContribution + ") " +
                            "and (cjl.id_context is null or cjl.id_context != " + idContribution + ") " +
                            "and (cpl.id_debate is null or cpl.id_debate != " + idContribution + ")" +
                            " and cig.id_group = " + Group.getPublicGroup().getIdGroup();
                case TAG:
                    return "select distinct ci.id_contribution from citation ci " +
                            "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
                            "inner join contribution_in_group cig on cig.id_contribution = ci.id_contribution " +
                            "where cht.id_tag in (" +
                            "SELECT distinct link.id_tag_to FROM tag_link link " +
                            "where link.id_tag_from = " + idContribution +
                            " union SELECT distinct link.id_tag_from FROM tag_link link " +
                            "where link.id_tag_to = " + idContribution + ")" +
                            " and cig.id_group = " + Group.getPublicGroup().getIdGroup();
            }
        }

        return null;
    }

    /**
     * Get a randomly chosen contribution id from the database
     *
     * @param type the type of contributions
     * @return a random contribution id
     */
    public static Long random(EContributionType type) {
        return random(type, false);
    }
    /**
     * Get a randomly chosen contribution id from the database
     *
     * @param type the type of contributions
     * @param onlyNew true for only new contributions
     * @return a random contribution id
     */
    public static Long random(EContributionType type, boolean onlyNew) {
        List<String> visibleGroups = Group.getVisibleGroupsFor(Group.getPublicGroup().getIdGroup());
        String sql = "select contribution.id_contribution from contribution right join contribution_in_group " +
                "on contribution_in_group.id_contribution = contribution.id_contribution where contribution_type = " + type.id() +
                " and id_group in (" + String.join(",", visibleGroups) + ") " +
                " and deleted = 0 and hidden = 0 " +
                (onlyNew ? "and version > NOW() - INTERVAL 1 WEEK " : "") +
                "order by rand() limit 1";
        return Ebean.createSqlQuery(sql).findUnique().getLong("id_contribution");
    }

    public static List<String> getLanguages() {
        return Arrays.asList("fr", "en", "nl");
    }

}
