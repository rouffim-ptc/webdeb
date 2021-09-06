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

import java.util.List;

/**
 * Simple representation of tag for draw tag hierarchy or in linked contribution (argument, text).
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebSimpleTag {

  /**
   * the id of the tag
   */
  @JsonSerialize
  private Long id;

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
   * Construct a simple json representation of a tag from a given api object Tag
   *
   * @param tag the tag to represent
   */
  public WebdebSimpleTag(Tag tag) {
    id = tag.getId();
    tagType = tag.getType().name();
    names = tag.getNamesAsTagName();
  }
}
