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

package be.webdeb.presentation.web.controllers.account.group;

import play.data.validation.ValidationError;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This form object is used to validate contributions. (formerly also for marks contributions, but it was judged later as a non-pedagogical way to learn)
 * Contains a list of list of ContributionValidation objects, each list being attached to a contributor.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ContributionValidationForm {

  private List<List<ContributionValidation>> validations = new LinkedList<>();

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Validate the validations of contributions
   *
   * @return null if validation ok, map of errors for each fields in error otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for (int i = 0; i < validations.size(); i++) {
      for (int j = 0; j < validations.get(i).size(); j++) {
        ContributionValidation current = validations.get(i).get(j);
        // deleted and validated state may not be activated at the same time (should not happen since it's handled in view)
        if (current.isDeleted() && current.isValidated()) {
          String field = "validations[" + i + "][" + j + "].validated";
          errors.put(field, Collections.singletonList(new ValidationError(field, "group.mark.validated.error")));
        }
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  /**
   * Get the list of lists of validations
   *
   * @return a list of list
   */
  public List<List<ContributionValidation>> getValidations() {
    return validations;
  }

  /**
   * Set the list of lists of contribution validations
   *
   * @param validations the contribution validations objects
   */
  public void setValidations(List<List<ContributionValidation>> validations) {
    this.validations = validations;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    validations.forEach(l -> l.forEach(m -> buffer.append(m.toString())));
    return buffer.toString();
  }
}
