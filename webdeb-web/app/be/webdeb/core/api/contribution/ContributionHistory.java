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

package be.webdeb.core.api.contribution;

import be.webdeb.core.api.contributor.Contributor;

import java.util.Date;

/**
 * This interface contains historic data regarding a modification done to a Contribution
 *
 * @author Fabian Gilson
 */
public interface ContributionHistory {

  /**
   * Get the Contributor that made this particular modification to owning Contribution
   *
   * @return a Contributor
   */
  Contributor getContributor();

  /**
   * Get the status of this modification by this contributor
   *
   * @return a modification status
   */
  EModificationStatus getModificationStatus();

  /**
   * Get the status of this modification by this contributor
   *
   * @return lang the user lang
   * @return a modification status title
   */
  String getModificationStatusTitle(String lang);

  /**
   * Get the full trace of the modification, ie, the stringified contribution
   *
   * @return a full trace
   */
  String getTrace();

  /**
   * Get the date at which this modification has been made
   *
   * @return the date of this version
   */
  Date getVersion();

}
