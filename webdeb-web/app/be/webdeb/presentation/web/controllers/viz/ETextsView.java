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
 * This enumeration is used for actor's texts EVizPane to show all texts where an actor appears.
 * <ul>
 *   <li>0 for author, ie where this actor is the author of the text</li>
 *   <li>1 for citation's author, ie where this actor is the author of an citation from this text</li>
 *   <li>2 for citation's cited, ie where this actor is cited in an citation from this text</li>
 * </ul>
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum ETextsView {

  /**
   * view where actor is author
   */
  AUTHOR(0),

  /**
   * view where actor is author of an citation
   */
  EXCERPT_AUTHOR(1),

  /**
   * view where actor is cited in citation
   */
  EXCERPT_CITED(2);

  private int id;
  private static Map<Integer, ETextsView> map = new LinkedHashMap<>();

  static {
    for (ETextsView type : ETextsView.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a view key
   */
  ETextsView(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a view key
   * @return the ETextsView enum value corresponding to the given id, null otherwise.
   */
  public static ETextsView value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this view key
   */
  public int id() {
    return id;
  }
}
