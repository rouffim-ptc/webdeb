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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the context_has_subdebate database table. Holds a link between a context and a sub debate.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "context_has_subdebate")
@Unqueryable
public class ContextHasSubDebate extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ContextHasSubDebate> find = new Model.Finder<>(ContextHasSubDebate.class);

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
    @JoinColumn(name = "id_debate", nullable = false)
    private Debate debate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_link")
    private ArgumentJustification argument;

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
     * Get origin context of this link
     *
     * @return a context contribution
     */
    public Contribution getContext() {
        return this.context;
    }

    /**
     * Set origin context of this link
     *
     * @param context a context contribution
     */
    public void setContext(Contribution context) {
        this.context = context;
    }

    /**
     * Get destination debate of this link
     *
     * @return a debate
     */
    public Debate getDebate() {
        return debate;
    }

    /**
     * Set destination debate of this link
     *
     * @param debate a debate
     */
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    /**
     * Get the argument justification if the link has been added from an argument in a context justification structure.
     *
     * @return the argument justification link
     */
    public ArgumentJustification getArgument() {
        return argument;
    }

    /**
     * Set the argument justification if the link has been added from an argument in a context justification structure.
     *
     * @param argument the argument justification link
     */
    public void setArgument(ArgumentJustification argument) {
        this.argument = argument;
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of thislink
     *
     * @return a timestamp with the latest update moment of this link
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        StringBuilder builder = new StringBuilder(" between ")
                .append(getContext().getIdContribution()).append(" and ")
                .append(getDebate().getContribution().getIdContribution())
                .append(", argument_link: [");

        if(getArgument() != null) {
            builder.append(getArgument().getIdContribution());
        } else {
            builder.append("null");
        }

        builder.append("]");

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
    public static ContextHasSubDebate findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve a link by debate origin and destination.
     *
     * @param origin the context id
     * @param destination the debate id
     * @return the corresponding link, null otherwise
     */
    public static ContextHasSubDebate findByOriginAndDestination(Long origin, Long destination) {
        return find.where().eq("id_context", origin).eq("id_debate", destination).findUnique();
    }

    /**
     * Retrieve all links by argument justification link id
     *
     * @param link an argument justification link id
     * @return a possibly empty list of links
     */
    public static List<ContextHasSubDebate> findByArgumentJustification(Long link) {
        return find.where().eq("id_link", link).findList();
    }

    /**
     * Get the list of sub debates for given context
     *
     * @param context a context id
     * @return a possibly empty list of links
     */
    public static List<ContextHasSubDebate> findByContext(Long context){
        return find.where().eq("id_context", context).findList();
    }

    /**
     * Get the list of link for given debate
     *
     * @param debate a debate id
     * @return a possibly empty list of links
     */
    public static List<ContextHasSubDebate> findByDebate(Long debate){
        return find.where().eq("id_debate", debate).findList();
    }

}
