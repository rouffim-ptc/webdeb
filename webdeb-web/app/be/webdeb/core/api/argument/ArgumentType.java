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

package be.webdeb.core.api.argument;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface represents a type of argument. An ArgumentType is composed by a type, a timing
 * (unknown, past, present, future) and an optional shade (verb). For the two first types, a shade must also be specified
 * (shades values are given between [] ).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see EArgumentType
 */
public interface ArgumentType extends PredefinedIntValue {

  /**
   * Get the corresponding EArgumenType value to this.
   *
   * @return the EArgumentType enum value corresponding to this argument type
   */
  EArgumentType getEType();
}
