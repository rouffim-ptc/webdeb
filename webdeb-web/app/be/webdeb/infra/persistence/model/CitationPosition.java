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

import be.webdeb.core.api.contribution.link.EPositionLinkShade;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the citation_position_link database table. Holds a position link between
 * a citation and a debate (and possibly a sub debate)
 * as well as the type of link between them.
 *
 * @author Martin Rouffiange
 * @see EPositionLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "citation_position_link")
public class CitationPosition extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, CitationPosition> find = new Model.Finder<>(CitationPosition.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_citation", nullable = false)
    private Citation citation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_debate", nullable = false)
    private Debate debate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sub_debate")
    private Tag subDebate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_shade", nullable = false)
    private TPositionLinkShadeType shade;

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the position link id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the position link id
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
     * Get the debate of this position link
     *
     * @return the debate where the link exists
     */
    public Debate getDebate() {
        return debate;
    }

    /**
     * Set the debate of this position link
     *
     * @param debate the debate where the link exists
     */
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    /**
     * Get the sub debate of this position link
     *
     * @return the sub debate where the link exists
     */
    public Tag getSubDebate() {
        return subDebate;
    }

    /**
     * Set the sub debate of this position link
     *
     * @param subDebate the sub debate where the link exists
     */
    public void setSubDebate(Tag subDebate) {
        this.subDebate = subDebate;
    }

    /**
     * Get the citation of the link
     *
     * @return the citation
     */
    public Citation getCitation() {
        return citation;
    }

    /**
     * Set the citation of the link
     *
     * @param citation the link citation
     */
    public void setCitation(Citation citation) {
        this.citation = citation;
    }

    /**
     * Get the position link shade
     *
     * @return a link shade
     */
    public TPositionLinkShadeType getShade() {
        return shade;
    }

    /**
     * Set the position link shade
     *
     * @param shade a link shade
     */
    public void setShade(TPositionLinkShadeType shade) {
        this.shade = shade;
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of this position link
     *
     * @return a timestamp with the latest update moment of this position link
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        StringBuilder builder = new StringBuilder(", debate: [")
                .append(getDebate().getIdContribution()).append("] ").append(getDebate().getContribution().getSortkey())
                .append(", subDebate: ").append(getSubDebate() != null ? "[" + getSubDebate().getIdContribution() + "]" : "null")
                .append(getSubDebate() != null ? getSubDebate().getDefaultName() : "")
                .append(", citation: [").append(getCitation().getIdContribution()).append("] ").append(getCitation().getOriginalExcerpt())
                .append(", shade: [").append(getShade().getIdShade()).append("] ").append(getShade().getEn());

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a position link by its id
     *
     * @param id the position link id
     * @return the position link corresponding to that id, null otherwise
     */
    public static CitationPosition findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find unique citation position by its citation id, debate id, sub debate id and shade id.
     * This signature must be unique.
     *
     * @param citationId the citation of the link id
     * @param debateId the debate of the link id
     * @param subDebateId the sub debate of the link id, if any
     * @param shadeId the link shade id
     * @return a citation position link corresponding to given contribution ids
     */
    public static CitationPosition findUnique(Long citationId, Long debateId, Long subDebateId, int shadeId){
        List<CitationPosition> links = find.where()
                .eq("id_citation", citationId)
                .eq("id_debate", debateId)
                .eq("id_sub_debate", isBlank(subDebateId) ? null : subDebateId)
                .eq("id_shade", shadeId)
                .findList();

        return links.isEmpty() ? null : links.get(0);
    }

    /**
     * Get the list of citation position link linked to the given citation id
     *
     * @param citation a citation id
     * @return a possibly empty list of citation position links
     */
    public static List<CitationPosition> findLinksForCitation(Long citation){
        return find.where().eq("id_citation", citation)
                .or().eq("id_citation", citation).findList();
    }

    /**
     * Get the list of citation position link in the given debate
     *
     * @param debate a debate id
     * @return a possibly empty list of citation position links
     */
    public static List<CitationPosition> findLinksForDebate(Long debate){
        return find.where().eq("id_debate", debate).findList();
    }

    /**
     * Get the list of citation links in the given context where that come from given text
     *
     * @param context a context contribution id
     * @param text a text id
     * @return a possibly empty list of citation links
     */
    public static List<CitationPosition> findCitationLinksByText(Long context, Long text){
        String select = "SELECT l.id_contribution FROM citation_position_link l " +
                "left join citation c on c.id_contribution = l.id_citation " +
                "where l.id_debate = " + context + " and c.id_text = " + text;
        return Ebean.find(CitationPosition.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    /**
     * Check that this citation position doesn't exists in given debate yet.
     *
     * @return true if this link exists in the given debate
     */
    public boolean existsInOtherDebate(Long debateId){
        return existsInOtherDebateAndSubDebate(debateId, null);
    }

    /**
     * Check that this citation justification doesn't exists in given debate and sub debate yet.
     *
     * @return true if this link already exists
     */
    public boolean existsInOtherDebateAndSubDebate(Long debateId, Long subContextId){
        return findUnique(getCitation().getIdContribution(),
                debateId,
                subContextId,
                shade.getIdShade())
                != null;
    }
}
