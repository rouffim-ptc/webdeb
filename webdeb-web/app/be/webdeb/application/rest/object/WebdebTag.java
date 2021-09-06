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

import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is devoted to exchange Tags in JSON format. All details of an API Tag are represented.
 * Links to other contributions are represented by their ids. Enumerated (predefined) data are passed as
 * typed objects with their ids and i18n descriptions.
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebTag extends WebdebContribution {

  /**
   * the type of the tag (root for a tag without parent tag, node for normal tag and leaf for tag
   * without children tag)
   */
  @JsonSerialize
  private String tagType;

  /**
   * the names of the tag
   */
  @JsonSerialize
  private List<TagName> names;

  /**
   * the rewording names of the tag
   */
  @JsonSerialize
  private List<TagName> rewordingNames = new ArrayList<>();

  /**
   * the list of parents tags
   */
  @JsonSerialize
  private List<WebdebSimpleTag> parents = new ArrayList<>();

  /**
   * the list of children tags
   */
  @JsonSerialize
  private List<WebdebSimpleTag> children = new ArrayList<>();

  /**
   * @api {get} WebdebTag Tag
   * @apiName Tag
   * @apiGroup Structures
   * @apiDescription A tag regroup texts and arguments on the same theme. They are lined one another to make a hierarchy.
   * @apiSuccess {String} tagType the type of the tag (root for a tag without parent tag, node for normal
   *                      tag and leaf for tag without children tag)
   * @apiSuccess {Object} name and rewording name a structure containing all spellings for this tag's name
   * @apiSuccess {String} name.name the spelling of the name
   * @apiSuccess {String} name.lang the 2-char ISO code of the language corresponding to this spelling
   * @apiSuccess {WebdebSimpleTag[]} parents the parents tag in the hierarchy
   * @apiSuccess {WebdebSimpleTag[]} children the children tag in the hierarchy
   * @apiSuccess {WebdebPlace[]} places a list of places involved in this tag
   * @apiExample Tag
   * {
   *   "id": 100000,
   *   "type": "tag",
   *   "version": 07465823022018,
   *   "tagType": "node",
   *   "name": [ { "name": "actualité", "lang": "fr" } ],
   *   "rewordingNames": [ { "name": "nouvelle", "lang": "fr",  "name": "nouveauté", "lang": "fr"} ],
   *   "parents": {
   *      "id": 100000,
   *      "tagType": "node",
   *      "name": [ { "name": "actualité", "lang": "fr" } ],
   *   },
   *   "children": {
   *      "id": 100000,
   *      "tagType": "node",
   *      "name": [ { "name": "actualité", "lang": "fr" } ],
   *   },
   *   "argumentPlace": {
   *     { "lang": "fr", "name": "Namur", "subregion": "Province de Namur", "region": "Wallonie", "Country": "Belgique", "continent": "Europe" },
   *   }
   * }
   * @apiVersion 0.0.3
   */
  public WebdebTag(Tag tag) {
    super(tag);
    tagType = tag.getType().name();
    names = tag.getNamesAsTagName();
    for(List<TagName> rNames : tag.getRewordingNames().values()){
      rewordingNames.addAll(rNames);
    }

    tag.getParents().forEach(f -> parents.add(new WebdebSimpleTag(f)));
    tag.getChildren().forEach(f -> children.add(new WebdebSimpleTag(f)));
  }
}
