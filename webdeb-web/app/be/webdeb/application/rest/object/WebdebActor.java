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

import be.webdeb.core.api.actor.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is devoted to exchange Actors in JSON format. All details of an API Actor are represented.
 * Links to other contributions are represented by their ids. Enumerated (predefined) data are passed as
 * typed objects with their ids and i18n descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebActor extends WebdebContribution {

  /**
   * list of names
   */
  @JsonSerialize
  protected List<ActorName> name;

  /**
   * list of old names
   */
  @JsonSerialize
  protected List<ActorName> oldname;

  /**
   * type of actor (as EActorType.name())
   * @see EActorType
   */
  @JsonSerialize
  protected String actorType;

  /**
   * external url
   */
  @JsonSerialize
  protected String crossReference;

  /**
   * list of affiliations of this actor
   */
  @JsonSerialize
  protected List<WebdebAffiliation> affiliations = new ArrayList<>();

  /**
   * @api {get} WebdebActor Actor (generic)
   * @apiName WebdebObjects
   * @apiGroup Structures
   * @apiDescription An actor is a generic object that contains common data to all types of actors.
   * @apiSuccess {Object} name a structure containing all spellings for this actor's name
   * @apiSuccess {String} name.first the person's first name or the organization's acronym (may be null)
   * @apiSuccess {String} name.last the person's last name or the organization's name (may be null)
   * @apiSuccess {String} name.pseudo the person's pseudonym (always null for organizations, may be null for persons)
   * @apiSuccess {String} name.lang the 2-char ISO code of the language corresponding to this spelling
   * @apiSuccess {Object} oldname a structure of the same type as name, containing all spellings for this actor's previous or alike names (optional)
   * @apiSuccess {String} actorType either "unknown", "person" or "organization"
   * @apiSuccess {String} crossReference the url of a personal webpage, if any (optional)
   * @apiSuccess {WebdebAffiliation[]} affiliations an array of WebdebAffiliation objects (optional)
   * @apiExample Actor in webdeb
   * {
   *   "id": 204,
   *   "type": "actor",
   *   "version": 1439366598000,
   *   "name": [ { "first": null, "last": "Sébastien Dupont", "pseudo": null, "lang": "fr" } ],
   *   "actorType": "unknown",
   *   "affiliations": [
   *     {
   *       "id": 87,
   *       "type": "affiliation",
   *       "version": 1440075236000,
   *       "function": [
   *         { "name": "psychologist", "lang": "en", "gender": "N" },
   *         { "name": "psychologue", "lang": "fr", "gender": "N" }
   *       ]
   *     }
   *   ]
   * }
   * @apiVersion 0.0.3
   */
  public WebdebActor(Actor actor) {
    super(actor);
    name = actor.getNames();
    oldname = EActorType.ORGANIZATION.equals(actor.getActorType()) ? ((Organization) actor).getOldNames() : null;
    actorType = actor.getActorType().name().toLowerCase();
    crossReference = actor.getCrossReference();
    actor.getAffiliations().forEach(a -> affiliations.add(new WebdebAffiliation(a)));
  }
}
