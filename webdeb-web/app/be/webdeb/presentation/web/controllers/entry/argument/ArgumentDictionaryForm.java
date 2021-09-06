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
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This wrapper is used to contains data about an argument dictionary, like its title, language and translations.
 *
 * @author Martin Rouffiange
 */
public class ArgumentDictionaryForm extends ContributionHolder {

    @Inject
    protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);


    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    protected String title;
    protected String language;


    /**
     * Constructor (Play / Json compliant)
     */
    public ArgumentDictionaryForm () {
        super();
        type = EContributionType.ARGUMENT_DICTIONARY;
    }

    /**
     * Constructor a form from ArgumentDictionary api object
     *
     * @param dictionary an argument dictionary
     */
    public ArgumentDictionaryForm (ArgumentDictionary dictionary) {
        this.id = dictionary.getId();
        this.title = dictionary.getTitle();
        this.language = dictionary.getLanguage().getCode();
        this.version = dictionary.getVersion();
    }

    @Override
    public String getDefaultAvatar() {
        return null;
    }

    @Override
    public MediaSharedData getMediaSharedData() {
        return null;
    }

    @Override
    public String getContributionDescription() {
        return null;
    }

    /*
     * Getters and setters
     */

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (values.isBlank(title)) {
            errors.put("title", Collections.singletonList(new ValidationError("title", "argument.error.title.blank")));
        }else if(title.length() > ArgumentHolder.TITLE_MAX_LENGTH){
            errors.put("title", Collections.singletonList(new ValidationError("title", "argument.error.title.size")));
        }

        if (values.isBlank(language)) {
            errors.put("language", Collections.singletonList(new ValidationError("language", "folder.error.name.lang")));
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save an argument dictionary into the database. This id is updated if it was not set before.
     *
     * @param contributor the contributor id that ask to save this contribution
     * @return the map of Contribution type and a list of contribution (actors or folders) that have been created during
     * this insertion(for all unknown contributions), an empty list if none had been created
     *
     * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     */
    public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
        logger.debug("try to save argument dictionary " + id + " " + toString() + " with version " + version + " in group " + inGroup);

        inGroup = values.isBlank(inGroup) ? 0 : inGroup;

        ArgumentDictionary dictionary = argumentFactory.getArgumentDictionary();
        dictionary.setId(id != null ? id : -1L);
        dictionary.setVersion(version);
        dictionary.addInGroup(inGroup);

        // remove ending "." if any
        title =  title.trim();
        if ( title.endsWith(".")) {
            title =  title.substring(0,  title.lastIndexOf('.'));
        }
        dictionary.setTitle(title);

        // argument language
        try {
            dictionary.setLanguage(argumentFactory.getLanguage(language));
        } catch (FormatException e) {
            logger.error("unknown language code " + language, e);
        }

        // save this argument dictionary
        Map<Integer, List<Contribution>> result = dictionary.save(contributor, inGroup);
        // update this id (in case of creation)
        id = dictionary.getId();
        return result;
    }

    @Override
    public String toString() {
        return "argument dictionary [" + id + "] with title: " + title + " and language " + language;
    }

}