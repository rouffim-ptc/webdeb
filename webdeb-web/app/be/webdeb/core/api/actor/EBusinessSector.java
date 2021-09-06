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
 * This enumeration holds available business sectors
 * <ul>
 *   <li>0 for Agriculture and fisheries</li>
 *   <li>1 for Art and culture</li>
 *   <li>2 for Sport and leisure</li>
 *   <li>3 for Retail trade</li>
 *   <li>4 for Building and real estate</li>
 *   <li>5 for Education and training</li>
 *   <li>6 for Energy</li>
 *   <li>7 for Environment</li>
 *   <li>8 for Finance, holding, banking and insurance</li>
 *   <li>9 for Hospitality, catering and tourism</li>
 *   <li>10 for Industry</li>
 *   <li>11 for Mining industry</li>
 *   <li>12 for Justice</li>
 *   <li>13 for Press and media</li>
 *   <li>14 for Research and expertise</li>
 *   <li>15 for Health</li>
 *   <li>16 for Security</li>
 *   <li>17 for Information and Communications Technology</li>
 *   <li>18 for Transport and logistics</li>
 *   <li>19 for Other services to individuals or organizations</li>
 *   <li>19 for Cross-sectoral</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EBusinessSector {
  /**
   * Agriculture and fisheries
   */
  AGRICULTURE(0),
  /**
   * Art and culture
   */
  ART(1),
  /**
   * Sport and leisure
   */
  SPORT(2),
  /**
   * Retail trade
   */
  RETAIL(3),
  /**
   * Building and real estate
   */
  BUILDING(4),
  /**
   * Education and training
   */
  EDUCATION(5),
  /**
   * Energy (production, transport, etc)
   */
  ENERGY(6),
  /**
   * Environment
   */
  ENVIRONMENT(7),
  /**
   * Finance, holding, banking and insurance
   */
  FINANCE(8),
  /**
   * Hospitality, catering and tourism
   */
  TOURISM(9),
  /**
   * Industries
   */
  INDUSTRY(10),
  /**
   * extraction and mining industries
   */
  MINING(11),
  /**
   * Justice
   */
  JUSTICE(12),
  /**
   * Press and media
   */
  PRESS(13),
  /**
   * Research and expertise
   */
  RESEARCH(14),
  /**
   * Health
   */
  HEALTH(15),
  /**
   * Security
   */
  SECURITY(16),
  /**
   * Information and commuication technologies
   */
  INFORMATION(17),
  /**
   * Transport and logistics
   */
  TRANSPORT(18),
  /**
   * Other services to individuals or organisations
   */
  OTHER(19),
  /**
   * Cross-sectoral
   */
  CROSS_SECTORAL(20);

  private int id;
  private static Map<Integer, EBusinessSector> map = new LinkedHashMap<>();

  static {
    for (EBusinessSector type : EBusinessSector.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a business sector
   */
  EBusinessSector(int id) {
    this.id = id;
  }


  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EBusinessSector
   * @return the EBusinessSector enum value corresponding to the given id, null otherwise.
   */
  public static EBusinessSector value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this business sector
   */
  public Integer id() {
    return id;
  }

}
