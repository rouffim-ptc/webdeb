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
package be.webdeb.infra.ws.external.auth;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
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
public class AuthForm {

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    @Inject
    protected ContributorFactory contributorFactory = Play.current().injector().instanceOf(ContributorFactory.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * The user email or pseudonym
     */
    protected String emailOrPseudo;

    /**
     * The user token for authentication
     */
    protected String token;

    /**
     * The user lang
     */
    protected String lang;

    /**
     * Play / JSON compliant constructor
     */
    public AuthForm() {

    }

    /**
     * Validate the auth form, ie, only checks if some values are filled in
     *
     * @return null if validation succeeded, error message otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (values.isBlank(lang)) {
            errors.put("user lang", Collections.singletonList(new ValidationError(lang, "")));
            return errors;
        }

        return null;
    }

    /**
     * Get the contributor by given email and auth token
     */
    public Contributor getContributor(){
        return contributorFactory.getContributorByToken(emailOrPseudo, token);
    }

    public void setEmailOrPseudo(String emailOrPseudo) {
        this.emailOrPseudo = emailOrPseudo;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return "AuthForm{" +
                "emailOrPseudo='" + emailOrPseudo + '\'' +
                "token='" + token + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }
}