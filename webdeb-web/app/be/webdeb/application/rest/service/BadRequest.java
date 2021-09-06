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

package be.webdeb.application.rest.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is used to give more details in case of wrong request done by REST services
 *
 * @author Fabian Gilson
 */
public class BadRequest {

  /**
   * name of service called
   */
  @JsonSerialize
  private String service;
  /**
   * message explaining the cause of the error
   */
  @JsonSerialize
  private String cause;
  /**
   * url to the documentation
   */
  @JsonSerialize
  private String documentation;


  /**
   * @api {get} BadRequest Error message
   * @apiName BadRequest
   * @apiGroup Structures
   * @apiDescription Bad requests may occur when any rest access contains invalid parameters
   * @apiSuccess {String} service name of service called
   * @apiSuccess {String} cause a message giving the root cause of the error
   * @apiSuccess {String} documentation url to the documentation page of this service
   * @apiExample Error message
   * {
   *   "service":"ById",
   *   "cause":"unknown id given 8 or wrong type 2",
   *   "documentation":"webdeb.be/assets/apidoc-rest/index.html"
   * }
   * @apiVersion 0.0.1
   */
  /**
   * Create a BadRequest exception
   *
   * @param service the service that caused the problem
   * @param cause the root cause of the problem
   * @param documentation a link to the documentation
   */
  public BadRequest(String service, String cause, String documentation) {
    this.service = service;
    this.cause = cause;
    this.documentation = documentation;
  }
}
