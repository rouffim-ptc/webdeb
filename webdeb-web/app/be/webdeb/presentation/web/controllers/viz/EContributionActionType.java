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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration represents the type of actions for a contribution.
 * <pre>
 *   0 for visualisation actions
 *   1 for editing actions
 *   2 for adding actions
 * </pre>
 *
 * @author Martin Rouffiange
 */

public enum EContributionActionType {

  /**
   * visualisation actions
   */
  VIZ(0),
  /**
   * editing actions
   */
  EDIT(1),

  /**
   * adding actions
   */
  ADD(2);

  private int id;
  private static Map<Integer, EContributionActionType> map = new LinkedHashMap<>();

  static {
    for (EContributionActionType type : EContributionActionType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a type of actions for a contribution
   */
  EContributionActionType(int id) {
    this.id = id;
  }

  /**
   * Get all available type of actions for a contribution
   *
   * @return a collection of EContributionActionType
   */
  public static Collection<EContributionActionType> getAll() {
    return map.values();
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a type of actions for a contribution
   * @return the EContributionActionType enum value corresponding to the given id, null otherwise.
   */
  public static EContributionActionType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this type of actions for a contribution
   */
  public int id() {
    return id;
  }
}
