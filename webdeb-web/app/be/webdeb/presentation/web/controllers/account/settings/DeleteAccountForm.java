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

package be.webdeb.presentation.web.controllers.account.settings;

import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple form to delete a contributor account
 *
 * @author Martin Rouffiange
 */
public class DeleteAccountForm {

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    private Long contributor;
    private String password;

    /**
     * Play / JSON compliant constructor
     */
    public DeleteAccountForm() {
        contributor = -1L;
    }

    /**
     * Default constructor, create a DeleteAccountForm from a contributor id
     *
     * @param contributor a contributor id
     */
    public DeleteAccountForm(Long contributor) {
        this.contributor = contributor;
    }

    /**
     * Validate the delete account form (implicit call from Play): must be long enough and match
     *
     * @return null if validation succeeded, error message otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();
        // passwords must match and be long enough
        if (values.isBlank(password) || password.length() < SessionHelper.MIN_PWD_SIZE) {
            errors.put("password", Collections.singletonList(new ValidationError("password", "contributor.error.password.length")));
        }
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Get the contributor id
     *
     * @return a contributor id
     */
    public Long getContributor() {
        return contributor;
    }

    /**
     * Set the contributor id
     *
     * @param contributor a contributor id
     */
    public void setContributor(Long contributor) {
        this.contributor = contributor;
    }

    /**
     * Get the input password
     *
     * @return a password (clear)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the input password
     *
     * @param password the clear password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
