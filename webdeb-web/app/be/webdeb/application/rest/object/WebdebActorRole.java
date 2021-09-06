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

import be.webdeb.core.api.actor.ActorRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is devoted to exchange ActorRoles in JSON format. All details of an API ActorRole are represented.
 * Links to other contributions are represented by their ids.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebActorRole {

  /**
   * id of actor playing a role in a webdeb contribution
   */
  @JsonSerialize
  private Long actor;

  /**
   * id of affiliation of this.actor in a webdeb contribution
   */
  @JsonSerialize
  private Long affiliation;

  /**
   * true if this.actor is the author (enunciated / signed the contribution)
   */
  @JsonSerialize
  private boolean isAuthor;

  /**
   * true if this.actor reported the contribution but is not the author of it
   */
  @JsonSerialize
  private boolean isReporter;

  /**
   * @api {get} WebdebActorRole Actor role
   * @apiName Actor Role
   * @apiGroup Structures
   * @apiDescription Actor's role in a contribution (always used in a concrete WebdebContribution).
   * @apiSuccess {Integer} actor the actor id
   * @apiSuccess {Integer} affiliation id of the affiliation of the above actor in the bound contribution
   * @apiSuccess {Boolean} isAuthor saying if this actor is the author of the linked contribution
   * @apiSuccess {Boolean} isReporter saying if this actor is the reporter of the linked contribution
   * @apiExample Actor's role in contribution
   * { "actor": 775, "isAuthor": true, "isReporter": false}
   * @apiVersion 0.0.3
   */
  public WebdebActorRole(ActorRole role) {
    actor = role.getActor().getId();
    affiliation = role.getAffiliation() != null ? role.getAffiliation().getId() : null;
    isAuthor = role.isAuthor();
    isReporter = role.isReporter();
  }
}
