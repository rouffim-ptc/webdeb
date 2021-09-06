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
 * Simple enum to select across the available visualization panes for all types of contributions
 * When you want to add a new tab in visualization screens, you must:
 * <ul>
 *   <li>add a new enum value</li>
 *   <li>create the pane in any xxVisualization template using the enum name and id, such as "name_id"</li>
 * </ul>
 *
 *
 * @author Fabian Gilson
 */
public enum EVizPane {

  /**
   * cartography view, ie, actor's affiliations, argument's justification map and text's structure
   */
  CARTO(0),

  /**
   * radiography view, ie, actor's talks, arguments similarity map (does not exist for texts)
   */
  RADIO(1),

  /**
   * sociography view, ie, actor's allies, arguments alliances and linked text's (does not exists for texts)
   */
  SOCIO(2),

  /**
   * citation view, ie, actor's list of arguments where they're cited, same for texts (does not exist for arguments)
   */
  CITATION(3),

  /**
   * text aggregation view, ie, actor's list of text where they appear, or linked texts to other texts (through their arguments)
   */
  TEXTS(4),

  /**
   * details view for all types of contributions with all their properties
   */
  DETAILS(5),
  /**
   * cartography view bis, ie, actor's affiliated
   */
  CARTO2(6);

  private int id;
  private static Map<Integer, EVizPane> map = new LinkedHashMap<>();

  static {
    for (EVizPane type : EVizPane.values()) {
      map.put(type.id, type);
    }
  }

  EVizPane(int id) {
    this.id = id;
  }

    /**
   * Get a pane enum value
   *
   * @param id an id
   * @return this pane enum value, null if non-existing id passed
   */
  public static EVizPane value(int id) {
    return map.get(id);
  }

    /**
   * Get this pane id
   *
   * @return this pane id
   */
  public int id() {
    return id;
  }
}
