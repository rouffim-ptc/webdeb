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

package be.webdeb.core.api.contribution.place;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

import java.util.Map;

/**
 * This interface represents a type of place, like country, city, region, ...
 * Accepted values are loaded at run time from a dedicated configuration database table.
 *
 * For more info, see data in table t_place_type
 *
 * Currently, those types are
 * <ul>
 *   <li>0 - Continent</li>
 *   <li>1 - World region, union of countries</li>
 *   <li>2 - Country</li>
 *   <li>3 - Region, state</li>
 *   <li>4 - Province, sub-region</li>
 *   <li>5 - City, others kind of place</li>
 * </ul>
 *
 * @author Martin Rouffiange
 * @see EPlaceType
 */
public interface PlaceType extends PredefinedIntValue {

  /**
   * Get the corresponding EPlaceType value to this.
   *
   * @return the EPlaceType enum value corresponding to this legal status
   */
  EPlaceType getEType();
}
