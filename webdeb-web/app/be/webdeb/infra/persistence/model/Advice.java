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
import java.util.*;

/**
 * The persistent class for the advice database table, holding advices that should be translated
 * in all supported languages (some default ones are, others dynamically created from users are mon-lingual).
 */
@Unqueryable
@Entity
@CacheBeanTuning
@Table(name = "advice")
public class Advice extends WebdebModel {

    private static final Model.Finder<Integer, Advice> find = new Model.Finder<>(Advice.class);
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    @Id
    @Column(name = "id_advice", unique = true, nullable = false)
    private int idAdvice;

    @OneToMany(mappedBy = "advice", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AdviceI18name> titles;

    /**
     * Default constructor. Initialize id to 0.
     */
    public Advice() {
        // initialize id to 0 (for auto increment)
        idAdvice = 0;
    }

    /**
     * Get this advice id
     *
     * @return this advice id
     */
    public int getIdAdvice() {
        return this.idAdvice;
    }

    /**
     * Set this advice id
     *
     * @param idAdvice an id
     */
    public void setIdAdvice(int idAdvice) {
        this.idAdvice = idAdvice;
    }

    /**
     * Get all available titles for this advice
     *
     * @return a list of titles as AdviceI18names
     */
    public List<AdviceI18name> getTitles() {
        return titles;
    }

    /**
     * Set all available titles for this advice
     *
     * @param titles a list of titles
     */
    public void setSpellings(List<AdviceI18name> titles) {
        if (titles != null) {
            // add/update new titles
            this.titles = new ArrayList<>();
            update();
            this.titles.addAll(titles);
        }
    }
       

    /*
     * QUERIES
     */

    /**
     * Find all advice objects that exists in database
     *
     * @return a list of advices
     */
    public static List<Advice> findAllAdvices() {
        return find.all();
    }

    /**
     * Find a advice by its id
     *
     * @param id a advice id
     * @return the corresponding advice, or null if not found
     */
    public static Advice findById(int id) {
        return find.byId(id);
    }

   
}
