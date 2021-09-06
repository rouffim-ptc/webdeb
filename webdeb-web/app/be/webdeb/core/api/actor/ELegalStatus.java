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
package be.webdeb.core.api.actor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds available legal status
 * <ul>
 *   <li>0 for Company</li>
 *   <li>1 for Political party</li>
 *   <li>2 for Public administration, public authorities, political or consultative decision instance</li>
 *   <li>3 for Union, organisational federation, lobby, social movement</li>
 *   <li>4 for Any other kind of non profit organization</li>
 *   <li>5 for Project or event</li>
 *   <li>6 for Prize</li>
 *   <li>7 for Product or brand</li>
 *   <li>8 for Label</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum ELegalStatus {
  /**
   * Company
   */
  COMPANY(0),
  /**
   * Political party
   */
  POLITICAL(1),
  /**
   * Public administration, public authorities, political or consultative decision instance
   */
  ADMINISTRATION(2),
  /**
   * Union, organisational federation, lobby, social movement
   */
  UNION(3),
  /**
   *  Any other kind of non profit organization
   */
  ONG(4),
  /**
   * Project or brand
   */
  PROJECT(5),
  /**
   * Prize
   */
  PRIZE(6),
  /**
   * Product or brand
   */
  PRODUCT(7),
  /**
   * Label
   */
  LABEL(8);

  private int id;
  private static Map<Integer, ELegalStatus> map = new LinkedHashMap<>();

  static {
    for (ELegalStatus type : ELegalStatus.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a legal status
   */
  ELegalStatus(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an ELegalStatus
   * @return the ELegalStatus enum value corresponding to the given id, null otherwise.
   */
  public static ELegalStatus value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this legal status
   */
  public Integer id() {
    return id;
  }

}
