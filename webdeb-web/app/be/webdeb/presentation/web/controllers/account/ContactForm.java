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

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple form to contact administrator
 *
 * @author Martin Rouffiange
 */
public class ContactForm {

    @Inject
    private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    private Long userId;
    private String email;
    private String firstname;
    private String name;
    private String subject;
    private String content;
    private String userAgent;

    /**
     * Play / JSON compliant constructor
     */
    public ContactForm() {
        userId = -1L;
    }

    /**
     * Default constructor, create a ContactForm from a contributor
     *
     * @param user a contributor holder form corresponding to the current user who want to contact
     */
    public ContactForm(ContributorHolder user) {
        if(user != null) {
            this.userId = user.getId();
            this.firstname = user.getFirstname();
            this.name = user.getLastname();
            this.email = user.getEmail();
        }

    }

    /**
     * Validate the authentication form, ie, only checks if "valid" values are filled in (implicit call)
     *
     * @return null if validation succeeded, error message otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (values.isBlank(email) && !values.isEmail(email)) {
            errors.put("email", Collections.singletonList(new ValidationError("email", "contributor.error.mail")));
        }
        if (values.isBlank(firstname)) {
            errors.put("firstname", Collections.singletonList(new ValidationError("firstname", "contributor.error.firstname")));
        }
        if (values.isBlank(name)) {
            errors.put("name", Collections.singletonList(new ValidationError("name", "contributor.error.lastname")));
        }
        if (values.isBlank(subject)) {
            errors.put("subject", Collections.singletonList(new ValidationError("subject", "contact.subject.required")));
        }
        if (values.isBlank(content)) {
            errors.put("content", Collections.singletonList(new ValidationError("content", "contact.content.required")));
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Get the user email
     *
     * @return a valid email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the user email
     *
     * @param email a valid email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the user firstname
     *
     * @return a user firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Set the user firstname
     *
     * @param firstname a user firstname
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * Get the user name
     *
     * @return a user name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the user name
     *
     * @param name a user name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the contact subject
     *
     * @return a contact subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the contact subject
     *
     * @param subject a contact subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get the content message
     *
     * @return a content message
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the content message as html
     *
     * @return a content message
     */
    public String getContentAsHtml() {
        return content.replaceAll("(\r\n|\n)", "<br>");
    }

    /**
     * Set the content message
     *
     * @param content a content message
     */
    public void setContent(String content) {
        this.content = content;
    }

    // Hidden fields

    /**
     * Get the user id
     *
     * @return an user id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set the user id
     *
     * @param userId an user id
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Get the user agent
     *
     * @return an user agent description
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Get the user agent
     *
     * @param userAgent an user agent description
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

}
