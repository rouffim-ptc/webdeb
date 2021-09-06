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

package be.webdeb.core.api.contribution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds values for the validation states of contributions. Contributions, mainly from
 * groups may be formally validated by the group owner, in order to be pushed into the public group or
 * (batch) deleted. A validation state is also valuable in the context of the contributor's desire to indicate
 * that a automatically created contribution is acceptable or not. Values are :
 * <ul>
 *   <li>UNSET(0) the validation status is unknown</li>
 *   <li>VALIDATED(1) the contribution has been validated</li>
 *   <li>INVALIDATED(2) the contribution has been invalidated, so it's not viewable anymore</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EValidationState {
  /**
   * Nobody validated / invalidated the subject contribution
   */
  UNSET(0),
  /**
   * The subject contribution has been validated and is a candidate to be pushed
   * into the public group (in case of group owner validation), if not already
   */
  VALIDATED(1),
  /**
   * The subject contribution has been invalidated and is not viewable anymore until revalidated (or deleted)
   */
  INVALIDATED(2);

  private int id;
  private static Map<Integer, EValidationState> map = new LinkedHashMap<>();

  static {
    for (EValidationState type : EValidationState.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a validated state for a contribution
   */
  EValidationState(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EValidationState
   * @return the EValidationState enum value corresponding to the given id, null otherwise.
   */
  public static EValidationState value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this validated state
   */
  public int id() {
    return id;
  }
}
