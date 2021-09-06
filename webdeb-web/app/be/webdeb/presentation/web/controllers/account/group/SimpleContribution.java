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

/**
 * Abstract class with common part for a contribution used in validation/merge processes.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class SimpleContribution {

  protected Long id;
  protected Integer type;

  /**
   * Get the contribution id
   *
   * @return an id
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the contribution id
   *
   * @param id a contribution id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Get the contribution type id (used to cast back to right type)
   *
   * @return an id, as defined in EContributionType
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public int getType() {
    return type;
  }

  /**
   * Set the contribution type id (used to cast back to right type)
   *
   * @param type type id, as defined in EContributionType
   * @see be.webdeb.core.api.contribution.EContributionType
   */
  public void setType(int type) {
    this.type = type;
  }
}
