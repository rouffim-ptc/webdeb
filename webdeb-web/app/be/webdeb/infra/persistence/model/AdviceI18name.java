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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The persistent class for the advice spellings (called title) database table. Advices have multiple spellings, depending
 * on a 2-char ISO code corresponding to the language and a char code corresponding to the word gender.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name="advice_i18names")
public class AdviceI18name  extends Model {

    @EmbeddedId
    private AdvicePK id;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "id_advice", nullable = false)
    @Unqueryable
    private Advice advice;

    /**
     * Create a new title for given advice
     *
     * @param id an advice id
     * @param lang a 2-char ISO 639 language code
     * @param title an advice title (language dependent)
     */
    public AdviceI18name(int id, String lang, String title) {
        this.id = new AdviceI18name.AdvicePK(id, lang);
        this.title = title;
    }

    /**
     * Get the complex id for this advice title
     *
     * @return a complex id (concatenation of id_advice and lang)
     */
    public AdvicePK getId() {
        return id;
    }

    /**
     * Set the complex id for this advice title
     *
     * @param id a complex id (concatenation of id_advice and lang)
     */
    public void setId(AdvicePK id) {
        this.id = id;
    }

    /**
     * Get the current language code for this title
     *
     * @return a two-char iso-639-1 language code
     */
    public String getLang() {
        return id.getLang();
    }

    /**
     * Set the current language code for this title
     *
     * @param lang a two-char iso-639-1 language code
     */
    public void setLang(String lang) {
        id.setLang(lang);
    }

    /**
     * Get the advice title
     *
     * @return an advice title in associated lang
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the advice title
     *
     * @param title an advice title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the advice associated to this title
     *
     * @return an advice
     */
    public Advice getAdvice() {
        return advice;
    }

    /**
     * Set the advice associated to this title
     *
     * @param advice an advice
     */
    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public String toString() {
        return getId() + " " + getTitle();
    }

    /**
     * The primary key class for the profession multiple name database table.
     *
     * @author Martin Rouffiange
     */
    @Embeddable
    public static class AdvicePK extends Model {

        @Column(name = "id_advice", insertable = false, updatable = false, unique = true, nullable = false)
        private Integer advice;

        @Column(name = "lang", insertable = false, updatable = false, unique = true, nullable = false)
        private String lang;

        /**
         * Default no-arg constructor
         */
        public AdvicePK() {
            // needed by ebean
        }

        /**
         * Create a complex key for given advice and language
         *
         * @param id a advice id
         * @param lang a 2-char iso 639 language code
         */
        public AdvicePK(int id, String lang) {
            advice = id;
            this.lang = lang;
        }

        /**
         * Get the advice id
         *
         * @return an id
         */
        public Integer getIdAdvice() {
            return advice;
        }

        /**
         * Set the advice id
         *
         * @param advice an id of an advice
         */
        public void setAdvice(Integer advice) {
            this.advice = advice;
        }

        /**
         * Get the language associated to this title
         *
         * @return a two-char iso-639-1 language code
         */
        public String getLang() {
            return this.lang;
        }

        /**
         * Set the language associated to this title
         *
         * @param lang a two-char iso-639-1 language code
         */
        public void setLang(String lang) {
            this.lang = lang;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AdvicePK)) {
                return false;
            }
            AdvicePK castOther = (AdvicePK) other;
            return this.advice.equals(castOther.advice)
                    && this.lang.equals(castOther.lang);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 17;
            hash = hash * prime + this.advice.hashCode();
            return hash * prime + this.lang.hashCode();
        }

        @Override
        public String toString() {
            return "[" + advice + ":" + lang + "]";
        }
    }
}
