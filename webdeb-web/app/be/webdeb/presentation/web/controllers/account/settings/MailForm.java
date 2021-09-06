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

import be.webdeb.util.ValuesHelper;
import javax.inject.Inject;

import play.api.Play;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple form class to let contributors changing their mail addresses
 *
 * @author Fabian Gilson
 */
public class MailForm {

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  private String email;

  /**
   * Play form validation method
   *
   * @return a list of errors or null if none
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();
    final String emailField = "email";

    if (!values.isEmail(email)) {
      List<ValidationError> list = new ArrayList<>();
      list.add(new ValidationError(emailField, "contributor.error.mail"));
      errors.put(emailField, list);
    }
    return errors.isEmpty() ? null : errors;
  }

  /**
   * get the user email
   *
   * @return the email entered by user
   */
  public String getEmail() {
    return email;
  }

  /**
   * set the email
   *
   * @param email an email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return email;
  }
}
