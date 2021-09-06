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

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the shaded argument database table, conceptual subtype of argument.
 * A shaded argument is an argument which title is preceded by a shade.
 *
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "argument_shaded")
public class ArgumentShaded extends WebdebModel {

    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ArgumentShaded > find = new Model.Finder<>(ArgumentShaded .class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Argument argument;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_shade", nullable = false)
    private TArgumentShadeType shade;

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
     * Get this argument shade
     *
     * @return the argument shade
     */
    public TArgumentShadeType getShade() {
        return shade;
    }

    /**
     * Set this argument shade
     *
     * @param shade a shade to set
     */
    public void setShade(TArgumentShadeType shade) {
        this.shade = shade;
    }

    /**
     * Get the parent argument object
     *
     * @return the parent argument object
     */
    public Argument getArgument() {
        return argument;
    }

    /**
     * Set the parent argument object
     *
     * @param argument the parent argument object
     */
    public void setArgument(Argument argument) {
        this.argument = argument;
    }

    @Override
    public String toString() {
        // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
        // (lazy load not triggered from toString methods)
        return new StringBuilder(", shade: [").append(getShade().getIdShade()).append("] ").append(getShade().getEn()).toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve an argument by its id
     *
     * @param id an id
     * @return the argument corresponding to the given id, null if not found
     */
    public static ArgumentShaded findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find an unique argument shaded by a complete title and shade
     *
     * @param title an argument title
     * @param lang a i18 lang
     * @param shade an argument shade
     * @return a matched argument shaded or null
     */
    public static ArgumentShaded findUniqueByTitleAndLang(String title, String lang, int shade) {
        List<ArgumentShaded> result = null;
        if (title != null && lang != null) {
            String token = getStrictSearchToken(title);
            String select = "select distinct a.id_contribution from argument_shaded ars " +
                    "inner join argument a on ars.id_contribution = a.id_contribution " +
                    "inner join argument_dictionary ad on ad.id_contribution = a.id_dictionary " +
                    "where ad.title = '" + token + "' and ad.id_language = '" + lang + "' and id_shade = " + shade;
            result = Ebean.find(ArgumentShaded.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
        }
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

}
