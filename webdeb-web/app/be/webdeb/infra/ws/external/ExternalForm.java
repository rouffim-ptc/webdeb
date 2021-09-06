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

package be.webdeb.infra.ws.external;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EExternalSource;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.ws.external.auth.AuthForm;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This form is used to request from external service that need a token to authenticate
 *
 * @author Martin Rouffiange
 */
public abstract class ExternalForm {

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    @Inject
    protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);

    @Inject
    protected CitationFactory citationFactory = Play.current().injector().instanceOf(CitationFactory.class);

    @Inject
    protected TextFactory textFactory = Play.current().injector().instanceOf(TextFactory.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * The external contribution id
     */
    protected Long id;

    /**
     * The external contribution url
     */
    protected String url;

    /**
     * The external contribution language
     */
    protected String language;

    /**
     * The external contribution source name
     */
    protected String sourceName;

    /**
     * The external contribution title
     */
    protected String title;

    /**
     * The external contribution type
     */
    protected int type;

    /**
     * The user auth data
     */
    protected AuthForm user = new AuthForm();

    /**
     * Play / JSON compliant constructor
     */
    public ExternalForm() {

    }

    /**
     * Validate the creation of an ExternalContribution
     *
     * @return null if validation ok, map of errors for each fields in error otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (values.isBlank(url) && !values.isURL(url)) {
            errors.put("url", Collections.singletonList(new ValidationError("url", "nlp.text.invalidurl")));
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Store the External Contribution into the database
     *
     * @param contributor the contributor id
     *
     * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     */
    public void save(Long contributor) throws FormatException, PermissionException, PersistenceException {
        logger.debug("try to save external contribution " + title + " (" + url + ")");

        ExternalContribution contribution = textFactory.getExternalContribution();
        contribution.setSourceUrl(url);
        contribution.setTitle(title);
        contribution.setSourceId(EExternalSource.valueOf(sourceName).id());
        contribution.setContributionType(textFactory.getContributionType(EContributionType.value(type)));
        // contribution language
        try {
            contribution.setLanguage(textFactory.getLanguage(language.trim()));
        } catch (FormatException e) {
            logger.error("unknown language code " + language, e);
        }

        contribution.save(contributor, Group.getGroupPublic());
        // do not forget to update the id, since the controller needs it for redirection
        id = contribution.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public AuthForm getUser() {
        return user;
    }

    public void setUser(AuthForm user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "ExternalForm{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", language='" + language + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                '}';
    }
}