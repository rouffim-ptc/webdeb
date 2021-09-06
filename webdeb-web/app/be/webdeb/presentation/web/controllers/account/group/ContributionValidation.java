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

import be.webdeb.core.api.contribution.EValidationState;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import java.text.NumberFormat;

/**
 * Simple form to handle validation for a contribution. (formerly for marks contributions, but it was judged later as a non-pedagogical way to learn)
 * Defines the contribution id and booleans to say either if this contribution is validated or must be deleted.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ContributionValidation extends SimpleContribution {

  private boolean validated;
  private boolean deleted;

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Play compliant constructor
   */
  public ContributionValidation() {
    // needed by Play
  }

	/**
   * Constructor. Initialize a contribution mark with given contribution and user-locale formatter
   *
   * @param contribution a contribution
   * @param formatter a number formatter to know how to present the data (typically using a decimal point or comma)
   */
  public ContributionValidation(ContributionHolder contribution, NumberFormat formatter) {
    id = contribution.getId();
    type = contribution.getType().id();
    validated = EValidationState.VALIDATED.equals(contribution.isValidated());
    deleted = EValidationState.INVALIDATED.equals(contribution.isValidated());
  }

  /**
   * Check whether related contribution is validated
   *
   * @return true if related contribution is validated
   */
  public boolean isValidated() {
    return validated;
  }

  /**
   * Set whether related contribution is validated
   *
   * @param validated true if related contribution is validated
   */
  public void setValidated(boolean validated) {
    this.validated = validated;
  }

  /**
   * Check whether related contribution must be deleted
   *
   * @return true if related contribution is deleted
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * Check whether related contribution must be deleted
   *
   * @param deleted true if related contribution must be deleted
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String toString() {
    return "id: " + id + ", validated: " + validated + ", deleted: " + deleted;
  }
}
