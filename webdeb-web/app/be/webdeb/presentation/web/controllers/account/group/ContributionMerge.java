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

import be.webdeb.presentation.web.controllers.entry.ContributionHolder;

/**
 * Simple pair object containing a contribution id and a boolean saying if related contribution must be merged into
 * the public webdeb site
 *
 * @author Fabian Gilson
 */
public class ContributionMerge extends SimpleContribution {

  private boolean toMerge;

  /**
   * Play-compliant constructor
   */
  public ContributionMerge() {
    // needed by play/jackson
  }

  /**
   * Constructor. Create a merge form object from an existing contribution
   *
   * @param holder a contribution holder
   */
  public ContributionMerge(ContributionHolder holder) {
    id = holder.getId();
    type = holder.getType().id();
  }


  /**
   * Check whether this contribution must be merged into the public website
   *
   * @return true if this contribution must be merged
   */
  public boolean getToMerge() {
    return toMerge;
  }

  /**
   * Set whether this contribution must be merged into the public website
   *
   * @param merged true if this contribution must be merged
   */
  public void setToMerge(boolean merged) {
    toMerge = merged;
  }
}
