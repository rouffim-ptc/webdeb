package be.webdeb.infra.persistence.model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the debate_has_tag_debate database table. Holds a link between a debate and a tag debate (tag).
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "debate_has_tag_debate")
public class DebateLink extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, DebateLink> find = new Model.Finder<>(DebateLink.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_debate", nullable = false)
    private Debate debate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag", nullable = false)
    private Tag tag;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    public DebateLink(Debate debate, Tag tag) {
        this.debate = debate;
        this.tag = tag;
    }

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the link id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the link id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the debate of this link
     *
     * @return a debate
     */
    public Debate getDebate() {
        return debate;
    }

    /**
     * Set the debate of this link
     *
     * @param debate a debate
     */
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    /**
     * Get the tag debate as tag of this link
     *
     * @return a tag
     */
    public Tag getTag() {
        return tag;
    }

    /**
     * Set tag debate as tag of this link
     *
     * @param tag a tag
     */
    public void setTag(Tag tag) {
        this.tag = tag;
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

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of this debate link
     *
     * @return a timestamp with the latest update moment of this debate link
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        StringBuilder builder = new StringBuilder(" between ")
                .append(getDebate().getIdContribution()).append(" and ")
                .append(getTag().getIdContribution());

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a link by its id
     *
     * @param id the link id
     * @return the link corresponding to that id, null otherwise
     */
    public static DebateLink findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve links by debate
     *
     * @param debate the debate id
     * @return a possibly empty list of links corresponding to given debate, null otherwise
     */
    public static List<DebateLink> findByDebate(Long debate){
        return find.where().eq("id_debate", debate).findList();
    }

    /**
     * Retrieve links by debate
     *
     * @param debate the debate id
     * @param contributor a contributor id
     * @param group a group id
     * @return a possibly empty list of links corresponding to given debate, null otherwise
     */
    public static List<DebateLink> findByDebate(Long debate, Long contributor, int group){
        String select = "SELECT link.id_contribution FROM debate_has_tag_debate link " +
                "inner join contribution c on c.id_contribution = link.id_contribution" +
                getContributionStatsJoins() +
                " where link.id_debate = " + debate +
                getContributionStatsWhereClause(contributor, group);
        return Ebean.find(DebateLink.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Retrieve a link by tag
     *
     * @param tag a tag id
     * @return the link corresponding to given debate, null otherwise
     */
    public static List<DebateLink> findByTag(Long tag) {
        return find.where().eq("id_tag", tag).findList();
    }


    /**
     * Find an link by debate and tag id
     *
     * @param debate the debate id
     * @param tag the tag id
     * @return the link if exists, null otherwise
     */
    public static DebateLink findUniqueByDebateAndTag(Long debate, Long tag){
        return find.where().eq("id_debate", debate).eq("id_tag", tag).findUnique();
    }
}
