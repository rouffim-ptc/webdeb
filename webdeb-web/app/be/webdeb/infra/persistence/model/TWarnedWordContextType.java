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
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the t_warned_word_context_type database table, holding predefined context
 * values for type of warned word
 *
 * @author Martin Rouffiange
 * @see TWarnedWord
 */
@Entity
@Table(name = "t_warned_word_context_type")
@CacheBeanTuning
@Unqueryable
public class TWarnedWordContextType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TWarnedWordContextType> find = new Model.Finder<>(TWarnedWordContextType.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    @OneToMany(mappedBy = "contextType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Unqueryable
    private List<TWarnedWord> warnedWords;

    /**
     * Get this warned word context type id
     *
     * @return an id
     */
    public int getIdWarnedWordType() {
        return idType;
    }

    /**
     * Set this warned word context type id
     *
     * @param idType an id
     */
    public void setIdWarnedWordType(int idType) {
        this.idType = idType;
    }

    /**
     * Get the warned words with this type
     *
     * @return a list of warned words
     */
    public List<TWarnedWord> getWarnedWords() {
        return warnedWords;
    }

    /**
     * Set the warned words with this type
     *
     * @param warnedWords a list of warned words
     */
    public void setWarnedWords(List<TWarnedWord> warnedWords) {
        this.warnedWords = warnedWords;
    }

}
