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
 * Simple form to handle login requests
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class LoginForm {

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  private String emailOrPseudo;
  private String password;
  private boolean remember;

  /**
   * Validate the authentication form, ie, only checks if "valid" values are filled in (implicit call)
   *
   * @return null if validation succeeded, error message otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (values.isBlank(emailOrPseudo) || values.isBlank(password)) {
      errors.put("emailOrPseudo", Collections.singletonList(new ValidationError(emailOrPseudo, "signin.error.mailorpass")));
      return errors;
    }

    return null;
  }

  /**
   * Get the login email or login pseudo
   *
   * @return a valid email or a pseudo
   */
  public String getEmailOrPseudo() {
    return emailOrPseudo;
  }

  /**
   * Set the login email or login pseudo
   *
   * @param emailOrPseudo a valid email or a pseudo
   */
  public void setEmailOrPseudo(String emailOrPseudo) {
    this.emailOrPseudo = emailOrPseudo;
  }

  /**
   * Get the clear password
   *
   * @return a password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the clear password
   *
   * @param password a clear password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Get if the user want his connection data remembered
   *
   * @return true if he want to
   */
  public boolean isRemember() {
    return remember;
  }

  /**
   * Set if the user want his connection data remembered
   *
   * @param remember if he want to
   */
  public void setRemember(Boolean remember) {
    this.remember = remember != null && remember;
  }


}
