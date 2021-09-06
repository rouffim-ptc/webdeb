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

package be.webdeb.presentation.web.controllers.entry.actor;

import be.webdeb.presentation.web.controllers.account.admin.AbtractProfessionForm;

/**
 * Simple form to only manage profession names
 *
 * @author Martin Rouffiange
 */
public class ProfessionNameForm extends AbtractProfessionForm {

    private String name;
    private String lang;
    private String gender;

    /**
     * Play / Json compliant constructor
     */
    public ProfessionNameForm() {
        super();
    }

    /**
     * Constructor. Create an ProfessionNameForm with a given name and gender
     *

     */
    public ProfessionNameForm(String name, String lang, String gender) {
        this();
        this.name = name;
        this.lang = lang;
        this.gender = gender;
    }

    /**
     * Helper method to check whether the name and ge,der are empty (contains empty or null values)
     *
     * @return true if all fields are empty
     */
    public boolean isEmpty() {
        return (name == null || "".equals(name)
                && (lang == null || "".equals(lang))
                && (gender == null || "".equals(gender)));
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
