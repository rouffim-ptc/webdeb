/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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

package be.webdeb.core.api.text;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface contains text visibility value for a text. Contains also descriptions in common languages
 * of the platform
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see ETextVisibility
 */
public interface TextVisibility extends PredefinedIntValue {

  /**
   * Get the corresponding ETextVisibility value to this.
   *
   * @return the ETextVisibility enum value corresponding to this visibility type
   */
  ETextVisibility getEType();

}
