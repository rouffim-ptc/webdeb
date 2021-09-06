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

import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the debate_has_tag_debate database table. Holds a link between a debate and a tag debate (tag).
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "debate_has_text")
public class DebateHasText extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, DebateHasText> find = new Model.Finder<>(DebateHasText.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_debate", nullable = false)
    private Debate debate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_text", nullable = false)
    private Text text;

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
     * Get the text of the link
     *
     * @return a text
     */
    public Text getText() {
        text.setLinkId(idContribution);
        return text;
    }

    /**
     * Set the text of the link
     *
     * @param text a text
     */
    public void setText(Text text) {
        this.text = text;
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a debate has text by its id
     *
     * @param id an id
     * @return the debate has text corresponding to the given id, null if not found
     */
    public static DebateHasText findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve an unique debate has text by
     *
     * @param debate a debate id
     * @param text a text id
     * @return the debate corresponding to the given id, null if not found
     */
    public static DebateHasText findUnique(Long debate, Long text) {
        return find.where().eq("id_debate", debate).eq("id_text", text).findUnique();
    }

    /**
     * Find a list of debate has text by debate id
     *
     * @param debate a debate id
     * @return a possibly empty list
     */
    public static List<DebateHasText> findLinkByDebate(Long debate){
        return find.where().eq("id_debate", debate).findList();
    }

    /**
     * Find a list of debate has text by text id
     *
     * @param text a text id
     * @return a possibly empty list
     */
    public static List<DebateHasText> findLinkByText(Long text){
        return find.where().eq("id_text", text).findList();
    }
}
