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

package be.webdeb.presentation.web.controllers.account.settings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple enum to select across the available settings panes (for user settings).
 * When you want to add a new tab in settings screens, you must:
 * <ul>
 *   <li>add a new enum value</li>
 *   <li>create the pane in userSettings template using the enum name and id, such as "name_id"</li>
 * </ul>
 *
 * @author Fabian Gilson
 */
public enum ESettingsPane {

  /**
   * profile pane
   */
  PROFILE(0),

  /**
   * my contributions' pane
   */
  CONTRIBUTIONS(1),

  /**
   * my group subscriptions
   */
  SUBSCRIPTION(2),

  /**
   * manage my groups
   */
  GROUP_MGMT(3),

  /**
   * admin-related tasks
   */
  FEEDER(4),

  /**
   * admin-related tasks
   */
  ADMIN(5),

  /**
   * manage professions related tasks
   */
  PROFESSION(6),

  /**
   * manage claims
   */
  CLAIM(7);

  private int id;
  private static Map<Integer, ESettingsPane> map = new LinkedHashMap<>();

  static {
    for (ESettingsPane type : ESettingsPane.values()) {
      map.put(type.id, type);
    }
  }

  ESettingsPane(int id) {
    this.id = id;
  }

  /**
   * Get a pane enum value
   *
   * @param id an id
   * @return this pane enum value, null if non-existing id passed
   */
  public static ESettingsPane value(int id) {
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
