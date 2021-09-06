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
 * Simple form to contact between contributors
 *
 * @author Martin Rouffiange
 */
public class ContactUserToUserForm {

    @Inject
    private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    private String emailUserSender;
    private String emailUserReceiver;
    private String pseudoUserReceiver;
    private String subject;
    private String content;

    /**
     * Play / JSON compliant constructor
     */
    public ContactUserToUserForm() {
    }

    /**
     * Default constructor, create a ContactUserToUserForm from a contributor sender and one receiver
     *
     * @param userSender a contributor holder form corresponding to the current user who want to contact another user
     * @param userReceiver a contributor holder form corresponding to the user that will be contacted
     */
    public ContactUserToUserForm(ContributorHolder userSender, ContributorHolder userReceiver) {
        this.emailUserSender = userSender.getEmail();
        this.emailUserReceiver = userReceiver.getEmail();
        this.pseudoUserReceiver = userReceiver.getPseudo();
    }

    /**
     * Validate the authentication form, ie, only checks if "valid" values are filled in (implicit call)
     *
     * @return null if validation succeeded, error message otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (values.isBlank(emailUserSender) || !values.isEmail(emailUserSender)) {
            errors.put("emailUserSender", Collections.singletonList(new ValidationError("emailUserSender", "contributor.error.mail")));
        }
        if (values.isBlank(emailUserReceiver) || !values.isEmail(emailUserReceiver)) {
            errors.put("emailUserSender", Collections.singletonList(new ValidationError("emailUserSender", "contributor.error.mail")));
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
     * Get the user sender email
     *
     * @return a valid email
     */
    public String getEmailUserSender() {
        return emailUserSender;
    }

    /**
     * Set the user sender email
     *
     * @param emailUserSender a valid user email
     */
    public void setEmailUserSender(String emailUserSender) {
        this.emailUserSender = emailUserSender;
    }

    /**
     * Get the user receiver email
     *
     * @return a valid email
     */
    public String getEmailUserReceiver() {
        return emailUserReceiver;
    }

    /**
     * Set the user receiver email
     *
     * @param emailUserReceiver a valid user receiver email
     */
    public void setEmailUserReceiver(String emailUserReceiver) {
        this.emailUserReceiver = emailUserReceiver;
    }

    /**
     * Get the user receiver pseudo
     *
     * @return a contributor pseudonym
     */
    public String getPseudoUserReceiver() {
        return pseudoUserReceiver;
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
}
