/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.viz.text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple enum to select across the available visualization panes for text contribution
 * When you want to add a new tab in visualization screens, you must:
 *
 * @author Martin Rouffiange
 */
public enum ETextVizPane {

  /**
   * The text details
   */
  PRESENTATION(0),

  /**
   * The text content and citations
   */
  CITATIONS(1),

  /**
   * The text arguments structure
   */
  ARGUMENTS(2);

  private int id;
  private static Map<Integer, ETextVizPane> map = new LinkedHashMap<>();

  static {
    for (ETextVizPane type : ETextVizPane.values()) {
      map.put(type.id, type);
    }
  }

  ETextVizPane(int id) {
    this.id = id;
  }

  /**
   * Get a pane enum value
   *
   * @param id an id
   * @return this pane enum value, null if non-existing id passed
   */
  public static ETextVizPane value(int id) {
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

