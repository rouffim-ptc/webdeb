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

import be.webdeb.core.api.argument.EArgumentType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The persistent class for the argument_dictionary database table, conceptual subtype of contribution.
 * This table keep title and language unique in the DB.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "argument_dictionary")
public class ArgumentDictionary extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ArgumentDictionary> find = new Model.Finder<>(ArgumentDictionary.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_language", nullable = false)
    private TLanguage language;

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Argument> arguments;

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
     * Get the title of this argument
     *
     * @return the title of this argument
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of this argument
     *
     * @param title a title for this argument
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the language of the title of this argument
     *
     * @return the argument language
     */
    public TLanguage getLanguage() {
        return language;
    }

    /**
     * Set the language of the title of this argument
     *
     * @param language a language
     */
    public void setLanguage(TLanguage language) {
        this.language = language;
    }

    /**
     * Get the list of arguments linked to this dictionary
     *
     * @return a possibly empty list of arguments
     */
    public List<Argument> getArguments() {
        return arguments;
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
        StringBuilder builder = new StringBuilder(", title: ").append(getTitle())
                .append(", language: ").append(getLanguage().getCode());

        return getModelDescription(getContribution(), builder.toString());
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve an argument dictionary by its id
     *
     * @param id an id
     * @return the argument dictionary corresponding to the given id, null if not found
     */
    public static ArgumentDictionary findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find an unique argument dictionary by a complete title
     *
     * @param title an argument dictionary title
     * @param lang a i18 lang
     * @return a matched argument dictionary or null
     */
    public static ArgumentDictionary findUniqueByTitleAndLang(String title, String lang) {
        return find.where().eq("title", title).eq("id_language", lang).findUnique();
    }

    /**
     * Find a list of arguments dictionary by a complete title
     *
     * @param title an argument dictionary title
     * @return the list of ArgumentDictionary matching the given title
     */
    public static List<ArgumentDictionary> findByTitleAndLang(String title) {
        return findByTitleAndLang(title, null, -1, -1, -1);
    }

    /**
     * Find a list of arguments dictionary by a complete title and lang
     *
     * @param title an argument dictionary title
     * @param lang a i18 lang
     * @param type the type of argument, if needed
     * @return the list of ArgumentDictionary matching the given title and lang
     */
    public static List<ArgumentDictionary> findByTitleAndLang(String title, String lang, int type, int fromIndex, int toIndex) {
        List<ArgumentDictionary> result = null;
        if (title != null) {
            // will be automatically ordered by relevance
            String token = getSearchToken(title);
            String select = "select distinct ad.id_contribution from argument_dictionary ad " +
                    "left join argument a on ad.id_contribution = a.id_dictionary " +
                    "where ad.title like '%" + token + "%'" + (lang == null ? "" : " and ad.id_language = '" + lang + "'") +
                    (type != -1 ? " and a.id_type = " + type : "") +
                    getSearchLimit(fromIndex, toIndex);

            result = Ebean.find(ArgumentDictionary.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null ? result : new ArrayList<>();
    }
}
