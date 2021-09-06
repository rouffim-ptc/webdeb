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
 *
 */

package be.webdeb.application.rest.object;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is devoted to exchange Contributions between webdeb instances (or any other system). It contains all
 * details of a contribution with links to related other contributions (ids only).
 * <p>
 * This particular abstract class contains details common to all types of contributions.
 * <p>
 * All "enumerated" values are also passed as typed objects to send id values as well as descriptions (in i18n)
 *
 * @author Fabian Gilson
 */
public abstract class WebdebContribution {

  /**
   * contribution unique id
   */
  @JsonSerialize
  protected Long id = -1L;

  /**
   * contribution type
   * @see EContributionType
   */
  @JsonSerialize
  protected String type;

  /**
   * version timestamp (long value in milliseconds from 1/01/1970)
   */
  @JsonSerialize
  protected long version = 1L;

  /**
   * @api {get} WebdebContribution Contribution (generic)
   * @apiName Contribution
   * @apiGroup Structures
   * @apiDescription Common properties to all types of contributions, i.e., all other structures contain these fields
   * too.
   * @apiSuccess {Integer} id the contribution id
   * @apiSuccess {String} type the concrete type of this contribution (define remaining fields), either "actor", "argument", "text" or "arglink"
   * @apiSuccess {Integer} version a version number (long value in milliseconds from 1/01/1970)
   * @apiVersion 0.0.1
   */
  public WebdebContribution(Contribution contribution) {
    id = contribution.getId();
    type = contribution.getContributionType().getEType().name().toLowerCase();
    version = contribution.getVersion();
  }
}
