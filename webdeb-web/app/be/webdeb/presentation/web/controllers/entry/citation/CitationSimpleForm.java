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

package be.webdeb.presentation.web.controllers.entry.citation;

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;

/**
 * This simple wrapper is used to contain only citation's id, parent's text id, original and working citation.
 * Mainly used to create a new citation or to manipulate excerpts with minimum data
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CitationSimpleForm extends ContributionHolder {

    protected Long textId;
    protected String originalExcerpt;
    protected String workingExcerpt;
    protected String language;


    /**
     * Constructor (Play / Json compliant)
     */
    public CitationSimpleForm() {
        type = EContributionType.CITATION;
    }

    /**
     * Constructor for an empty form referencing a text
     *
     * @param textId the text id to which this new citation will belong to
     */
    public CitationSimpleForm(Long textId) {
        this();
        this.textId = textId;
    }

    /**
     * Constructor with a given citation
     *
     * @param citation an API citation to be wrapped
     */
    public CitationSimpleForm(Citation citation, String lang) {
        this(citation.getTextId());
        id = citation.getId();
        version = citation.getVersion();
        originalExcerpt = citation.getOriginalExcerpt();
        workingExcerpt = values.isBlank(citation.getOriginalExcerpt()) ? citation.getOriginalExcerpt() : citation.getWorkingExcerpt();
        language = citation.getLanguage().getCode();
    }

    @Override
    public MediaSharedData getMediaSharedData() {
        return null;
    }

    @Override
    public String getContributionDescription() {
        return null;
    }

    @Override
    public String getDefaultAvatar(){
        return "";
    }

    /**
     * Get the text id from which this citation has been extracted
     *
     * @return an id
     */
    public Long getTextId() {
        return textId;
    }

    /**
     * Set the text id from which this citation has been extracted
     *
     * @param textId a text id
     */
    public void setTextId(Long textId) {
        this.textId = textId;
    }

    /**
     * Get the original citation from which this citation has been extracted
     *
     * @return the original citation
     */
    public String getOriginalExcerpt() {
        return originalExcerpt;
    }

    /**
     * Set the original citation from which this citation has been extracted
     *
     * @param excerpt an original citation
     */
    public void setOriginalExcerpt(String excerpt) {
        this.originalExcerpt = excerpt;
    }

    /**
     * Get the working citation(free-form part) corresponding to the original citation
     *
     * @return a working citation
     */
    public String getWorkingExcerpt() {
        return workingExcerpt;
    }

    /**
     * Set the working citation(free-form part) corresponding to the original citation
     *
     * @param excerpt a working citation
     */
    public void setWorkingExcerpt(String excerpt) {
        this.workingExcerpt = excerpt;
    }

    /**
     * Get the citation language
     *
     * @return the language description in this.lang
     *
     * @see Language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set the citation language id
     *
     * @param language a language id
     * @see Language
     */
    public void setLanguage(String language) {
        if(lang == null){
            lang = language;
        }
        this.language = language;
    }



}