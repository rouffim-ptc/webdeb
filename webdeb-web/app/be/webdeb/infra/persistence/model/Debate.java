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
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The persistent class for the debate database table, conceptual subtype of contribution.

 * A debate may be :
 *  - an unique response debate and be titled by a shaded argument (simple debate)
 *  - multiple possible responses debate and titled by a simple argument (not shaded) and be composed by simple debates.
 *  - tag debate automatically created with tag, and tagged citations
 *
 * May also have justification with arguments and citations.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "debate")
public class Debate extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, Debate> find = new Model.Finder<>(Debate.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_argument", nullable = false)
    private ArgumentShaded argument;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_shade", nullable = false)
    private TDebateShadeType shade;

    @Column(name = "description", length = 2048)
    private String description;

    @Column(name = "is_multiple", nullable = false)
    @Unqueryable
    private int isMultiple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_picture")
    private ContributorPicture picture;

    @OneToMany(mappedBy = "debate", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DebateExternalUrl> externalUrls;

    @OneToMany(mappedBy = "debateFrom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DebateSimilarity> similaritiesFrom;

    @OneToMany(mappedBy = "debateTo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DebateSimilarity> similaritiesTo;

    @Transient
    @Unqueryable
    private Integer nb_contributions;

    @Transient
    @Unqueryable
    private Integer nb_positions;

    @Transient
    @Unqueryable
    private Integer nb_justifications;

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
     * Get the argument (simple one, not shaded) that titled this debate
     *
     * @return the debate title argument
     */
    public ArgumentShaded getArgument() {
        return argument;
    }

    /**
     * Set the argument (simple one, not shaded) that titled this debate
     *
     * @param argument the debate title argument
     */
    public void setArgument(ArgumentShaded argument) {
        this.argument = argument;
    }

    /**
     * Get the shade of this debate. The shade can also be obtained by the conversion of the argument shade.
     *
     * @return the debate shade
     */
    public TDebateShadeType getShade() {
        return shade;
    }

    /**
     * Set the shade of this debate.
     *
     * @param shade the debate shade
     */
    public void setShade(TDebateShadeType shade) {
        this.shade = shade;
    }

    /**
     * Get the text description to contextualized and add textual information about this debate
     *
     * @return the description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the text description to contextualized and add textual information about this debate
     *
     * @param description the description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Check whether this debate is multiple thesis or not
     *
     * @return true if this debate is multiple thesis
     */
    public boolean isMultiple() {
        return isMultiple == 1;
    }

    /**
     * Set the if this debate is multiple thesis or not
     *
     * @param isMultiple true if this debate is multiple thesis
     */
    public void setMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple ? 1 : 0;
    }

    /**
     * Get all data about the picture of this debate
     *
     * @return a contributor picture, null if none
     */
    public ContributorPicture getPicture() {
        return picture;
    }

    /**
     * Set all data about the picture of this debate
     *
     * @param picture a contributor picture
     */
    public void setPicture(ContributorPicture picture) {
        this.picture = picture;
    }

    /**
     * Get the list of external url linked to this debate
     *
     * @return a possibly empty list of external urls
     */
    public List<DebateExternalUrl> getExternalUrls() {
        return externalUrls;
    }

    /**
     * Set the list of external url linked to this debate
     *
     * @param externalUrls a list of external urls
     */
    public void setExternalUrls(List<DebateExternalUrl> externalUrls) {
        if (externalUrls != null) {
            if (this.externalUrls == null) {
                this.externalUrls = new ArrayList<>();
            }
            // get previous url for current external url
            List<String> currentUrls = this.externalUrls.stream().map(DebateExternalUrl::getUrl).collect(Collectors.toList());

            // add/update new names
            externalUrls.forEach(this::addExternalUrl);

            currentUrls.stream().filter(former -> externalUrls.isEmpty() || externalUrls.stream().noneMatch(url -> url.getUrl().equals(former)))
                    .forEach(urlToDelete -> DebateExternalUrl.findByUrlAndDebateId(urlToDelete, idContribution).forEach(DebateExternalUrl::delete));
        }
    }

    /**
     * Add an external url to this debate, if such url already exists, will update existing external url
     *
     * @param url an external url structure
     */
    public void addExternalUrl(DebateExternalUrl url) {
        if (this.externalUrls == null) {
            this.externalUrls = new ArrayList<>();
        }
        Optional<DebateExternalUrl> match = this.externalUrls.stream().filter(n ->
                n.getUrl().equals(url.getUrl())).findAny();
        if (match.isPresent()) {
            DebateExternalUrl toUpdate = match.get();
            toUpdate.setAlias(url.getAlias());
            url.update();
        } else {
            // auto increment
            url.setIdUrl(0L);
            url.save();
            this.externalUrls.add(url);
        }
    }

    /**
     * Get the similarities debate "from"
     *
     * @return a possibly empty list of ebate similarity
     */
    public List<DebateSimilarity> getSimilaritiesFrom() {
        return similaritiesFrom;
    }

    /**
     * Get the similarities debate "from"
     *
     * @return a possibly empty list of debate similarity
     */
    public List<DebateSimilarity> getSimilaritiesTo() {
        return similaritiesTo;
    }

    /**
     * Get the number of contributions if this data is asked in a query
     *
     * @return the number of contributions
     */
    public Integer getNbContributions() {
        return nb_contributions;
    }

    public Integer getNbPositions() {
        return nb_positions;
    }

    public Integer getNbJustifications() {
        return nb_justifications;
    }

    /**
     * Get the number of links of given contribution type
     *
     * @param type the type of link
     * @param contributorId the id of the contributor for which we need that stats
     * @param groupId the group where see the stats
     * @return the number of links with this debate
     */
    public int getNbLinks(EContributionType type, Long contributorId, int groupId){
        String linkTable = null;
        String joinAttribute = null;

        switch (type) {
            case ARGUMENT_JUSTIFICATION:
                linkTable = "argument_justification_link";
                joinAttribute = "id_context";
                break;
            case CITATION_JUSTIFICATION:
                linkTable = "citation_justification_link";
                joinAttribute = "id_context";
                break;
            case CITATION_POSITION:
                linkTable = "citation_position_link";
                joinAttribute = "id_debate";
                break;
        }

        if(linkTable != null) {
            String select = "select count(distinct link.id_contribution) as 'count' FROM " + linkTable + " link " +
                    "left join contribution c on link.id_contribution = c.id_contribution " +
                    getContributionStatsJoins() +
                    " where link." + joinAttribute + " = " + idContribution +
                    getContributionStatsWhereClause(contributorId, groupId);
            return Ebean.createSqlQuery(select).findUnique().getInteger("count");
        }

        return 0;
    }

    /**
     * Get the current version of this argument
     *
     * @return a timestamp with the latest update moment of this argument
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)

        StringBuilder builder = new StringBuilder(", argument_shaded: [").append(getArgument().getIdContribution()).append("] ")
                .append(getArgument().getArgument().getDictionary().getTitle())
                .append(", debate shade: [").append(getShade().getIdShade()).append("] ")
                .append(getShade().getEn())
                .append(", description: ").append(getDescription()).append(", multiple: ")
                .append(isMultiple()).append(", picture: ");

        if(getPicture() != null) {
            builder.append(getPicture().getIdPicture()).append(".").append(getPicture().getExtension());
        } else {
            builder.append("null");
        }

        builder.append(", external urls: [").append(
            getExternalUrls().stream().map(DebateExternalUrl::toString).collect(Collectors.joining(", ")));

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a debate by its id
     *
     * @param id an id
     * @return the debate corresponding to the given id, null if not found
     */
    public static Debate findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Check if an debate is similar to another one
     *
     * @param toCompare the debate id to compare with the other one
     * @param debate an debate id 
     * @return true if both debates are similar
     */
    public static boolean similarDebates(Long toCompare, Long debate){
        String select = "SELECT id_contribution FROM debate_similarity where " +
                "((id_debate_from = " + toCompare + " and id_debate_to = " + debate + ") " +
                "or (id_debate_from = " + debate + " and id_debate_to = " + toCompare + ") and shade = " + ESimilarityLinkShade.SIMILAR.id() + ")";
        return Ebean.find(DebateSimilarity.class).setRawSql(RawSqlBuilder.parse(select).create()).findRowCount() > 0;
    }
    
    /**
     * Get the list of similar links for this debate
     *
     * @return a possibly empty list of debate similarity links
     */
    public List<DebateSimilarity> getSimilarDebates() {
        List<DebateSimilarity> similarities = getSimilaritiesFrom();
        similarities.addAll(getSimilaritiesTo());
        return similarities;
    }

    /**
     * Find a list of debates by a complete title, debate shade and lang
     *
     * @param title a debate title
     * @param shade a debate shade
     * @param lang a i18 lang
     * @return the list of Debate matching the given title, debate shade and lang
     */
    public static List<Debate> findByTitleAndLang(String title, Integer shade, String lang) {
        List<Debate> result = null;
        if (title != null) {
            // will be automatically ordered by relevance
            title = title.replace('?', ' ');
            String token = getSearchToken(title);

            String select = "select distinct d.id_contribution from debate d " +

                    "left join argument_shaded ars on ars.id_contribution = d.id_argument " +
                    "left join argument a on a.id_contribution = ars.id_contribution " +
                    "left join argument_dictionary ad on ad.id_contribution = a.id_dictionary " +

                    "where (ad.title like '" + token + "%'" +
                    (lang == null ? "" : " and ad.id_language = '" + lang + "'") +
                    (shade == null ? "" : " and d.id_shade = " + shade) + ")";

            logger.debug("search for debate : " + select);
            result = Ebean.find(Debate.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null ? result : new ArrayList<>();
    }

    /**
     * Get the list of debates linked with given citation (from position and justification links)
     *
     * @param id
     * @return
     */
    public static List<Debate> getDebatesLinkedToCitation(Long id) {
        return new ArrayList<>();
    }

    /**
     * Get a randomly chosen debate from the database
     *
     * @return a random Debate
     */
    public static Debate random() {
        return findById(random(EContributionType.DEBATE));
    }
}
