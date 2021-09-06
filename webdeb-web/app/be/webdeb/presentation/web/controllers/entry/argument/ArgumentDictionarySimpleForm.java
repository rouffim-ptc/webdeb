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

package be.webdeb.presentation.web.controllers.entry.argument;

import be.webdeb.core.api.argument.ArgumentDictionary;

/**
 * This simple wrapper is used to contain only argument dictionary id, title and language data.
 *
 * @author Martin Rouffiange
 */
public class ArgumentDictionarySimpleForm {

    private Long id;
    private String title;
    private String lang;


    /**
     * Constructor (Play / Json compliant)
     */
    public ArgumentDictionarySimpleForm () {

    }

    /**
     * Constructor a simple form from ArgumentDictionary api object
     *
     * @param dictionary an argument dictionary
     */
    public ArgumentDictionarySimpleForm (ArgumentDictionary dictionary) {
        this.id = dictionary.getId();
        this.title = dictionary.getTitle();
        this.lang = dictionary.getLanguage().getCode();
    }

    public ArgumentDictionarySimpleForm(Long id, String title, String lang) {
        this.id = id;
        this.title = title;
        this.lang = lang;
    }

    /*
     * Getters and setters
     */

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLang() {
        return lang;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}