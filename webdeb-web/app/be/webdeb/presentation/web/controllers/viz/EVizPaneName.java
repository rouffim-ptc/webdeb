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

package be.webdeb.presentation.web.controllers.viz;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple enum to get the viz names
 *
 * @author Martin Rouffiange
 */
public enum EVizPaneName {

  /**
   * cartography view, ie, actor's affiliations, argument's justification map and text's structure
   */
  CARTO("carto"),

  /**
   * radiography view, ie, actor's talks, arguments similarity map (does not exist for texts)
   */
  RADIO("radio"),

  /**
   * sociography view, ie, actor's allies, arguments alliances and linked text's (does not exists for texts)
   */
  SOCIO("socio"),

  /**
   * citation view, ie, actor's list of arguments where they're cited, same for texts (does not exist for arguments)
   */
  CITATION("cited"),

  /**
   * text aggregation view, ie, actor's list of text where they appear, or linked texts to other texts (through their arguments)
   */
  TEXTS("texts"),

  /**
   * details view for all types of contributions with all their properties
   */
  DETAILS("details"),
  /**
   * cartography view bis, ie, actor's affiliated
   */
  CARTO2("carto2");

  private String id;
  private static Map<String, EVizPaneName> map = new LinkedHashMap<>();

  static {
    for (EVizPaneName type : EVizPaneName.values()) {
      map.put(type.id, type);
    }
  }

  EVizPaneName(String id) {
    this.id = id;
  }

  /**
   * Get a pane enum value
   *
   * @param id an id
   * @return this pane enum value, null if non-existing id passed
   */
  public static EVizPaneName value(String id) {
    return map.get(id);
  }

  /**
   * Get this pane id
   *
   * @return this pane id
   */
  public String id() {
    return id;
  }
}
