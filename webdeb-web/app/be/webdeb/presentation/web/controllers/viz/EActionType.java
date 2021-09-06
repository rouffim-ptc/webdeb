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

import be.webdeb.core.api.contribution.EContributionType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration represents the differents action possible for a folder
 * <pre>
 *   -1 for none
 *   0 for action on citations
 *   1 for action on debates
 *   2 for action on positions
 * </pre>
 *
 * @author Martin Rouffiange
 */

public enum EActionType {

  /**
   * No / all actions
   */
  NONE(-1),
  /**
   * action on citation
   */
  CITATION(0),
  /**
   * action on debate
   */
  DEBATE(1),
  /**
   * action on position
   */
  POSITION(2);

  private int id;
  private static Map<Integer, EActionType> map = new LinkedHashMap<>();

  static {
    for (EActionType type : EActionType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a group key
   */
  EActionType(int id) {
    this.id = id;
  }

  /**
   * Get all available keys
   *
   * @return a collection of ETagActionType
   */
  public static Collection<EActionType> getAll() {
    return map.values();
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a contribution action
   * @return the ETagActionType enum value corresponding to the given id, null otherwise.
   */
  public static EActionType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this contribution action
   */
  public int id() {
    return id;
  }

  public static EActionType contributionTypeToActionType(EContributionType contributionType){
    switch (contributionType){
      case CITATION:
        return EActionType.CITATION;
      case DEBATE:
        return EActionType.DEBATE;
      default:
        return EActionType.NONE;
    }
  }
}
