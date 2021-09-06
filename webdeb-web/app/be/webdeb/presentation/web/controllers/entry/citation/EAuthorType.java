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

package be.webdeb.presentation.web.controllers.entry.citation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types of author for citation
 * <ul>
 *   <li>0 for text author</li>
 *   <li>1 for persons</li>
 *   <li>2 for organizations</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EAuthorType {

  /**
   * Text author
   */
  FROM_TEXT(0),
  /**
   * Authors are persons
   */
  PERSONS(1),
  /**
   * Authors are organizations
   */
  ORGANIZATIONS(2);


  private int id;

  private static Map<Integer, EAuthorType> map = new LinkedHashMap<>();

  static {
    for (EAuthorType type : EAuthorType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a type of author for citation
   */
  EAuthorType(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a type of author for citation
   * @return the EAuthorType enum value corresponding to the given id, null otherwise.
   */
  public static EAuthorType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this type of author for citation
   */
  public int id() {
    return id;
  }

}
