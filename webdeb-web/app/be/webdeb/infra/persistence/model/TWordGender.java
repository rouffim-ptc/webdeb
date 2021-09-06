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
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_word_gender database table, holding predefined values for word genders
 *
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "t_word_gender")
@CacheBeanTuning
@Unqueryable
public class TWordGender extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<String, TWordGender> find = new Model.Finder<>(TWordGender.class);

    @Id
    @Column(name = "id_word_gender", unique = true, nullable = false, length = 1)
    private String idWordGender;

    @OneToMany(mappedBy = "gender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Unqueryable
    private List<ProfessionI18name> names;

    /**
     * Get this word gender id
     *
     * @return an id
     */
    public String getIdGender() {
        return this.idWordGender;
    }

    /**
     * Set this word gender id
     *
     * @param idWordGender an id
     */
    public void setIdGender(String idWordGender) {
        this.idWordGender = idWordGender;
    }

    /**
     * Get the list of profession names with this gender
     *
     * @return a (possibly empty) list of profession names
     */
    public List<ProfessionI18name> getNames() {
        return names != null ? names : new ArrayList<>();
    }

    /**
     * Set the list of profession names of this gender
     *
     * @param names a list of profession names of this gender
     */
    public void setNames(List<ProfessionI18name> names) {
        this.names = names;
    }


}
