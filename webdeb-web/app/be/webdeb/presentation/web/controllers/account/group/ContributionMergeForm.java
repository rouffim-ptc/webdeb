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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple form containing a list of ContributionMerge, ie, a list of pair with the contribution id and a
 * boolean saying if the contribution must be pushed to the public website
 *
 * @author Fabian Gilson
 */
public class ContributionMergeForm {

  private List<ContributionMerge> merge;

  /**
   * Play compliant constructor
   */
  public ContributionMergeForm() {
    merge = new ArrayList<>();
  }

  /**
   * Constructor. Initialize the list of merge form objects with given contribution holders (ie, set ids)
   *
   * @param holders a list of contributions
   */
  public ContributionMergeForm(List<ContributionHolder> holders) {
    merge = holders.parallelStream().map(ContributionMerge::new).collect(Collectors.toList());
  }

  /**
   * Get the list of pairs of (contribution ids, boolean saying if the contribution must be pushed to the public website)
   *
   * @return a list of ContributionMerge object (pairs)
   */
  public List<ContributionMerge> getMerge() {
    return merge;
  }

  /**
   * Set the list of pairs of (contribution ids, boolean saying if the contribution must be pushed to the public website)
   *
   * @param merge a list of ContributionMerge object (pairs)
   */
  public void setMerge(List<ContributionMerge> merge) {
    this.merge = merge;
  }
}
