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

package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentDictionary;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a Webdeb argument dictionary, ie, a sentence for arguments written by a contributor.
 *
 * @author Martin Rouffiange
 */
class ConcreteArgumentDictionary extends AbstractContribution<ArgumentFactory, ArgumentAccessor> implements ArgumentDictionary {

    protected String title;
    protected Language language;


    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Default constructor.
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    ConcreteArgumentDictionary(ArgumentFactory factory, ArgumentAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        type = EContributionType.ARGUMENT_DICTIONARY;
    }

    /*
     * GETTERS / SETTERS
     */

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("argument dictionary contains error " + errors.toString());
            throw new FormatException(FormatException.Key.ARGUMENT_DICTIONARY_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return null;
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();

        if (language == null) {
            fieldsInError.add("language is null");
        }

        if (factory.getValuesHelper().isBlank(title)) {
            fieldsInError.add("title is null");
        }
        return fieldsInError;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArgumentDictionary && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return 42 * (id == -1L ? 47 : id.hashCode())  + language.getCode().hashCode() + title.hashCode();
    }

    @Override
    public String toString() {
        return "argument dictionary " + id + " with title " + getTitle() + " in " + language.getCode();
    }

}
