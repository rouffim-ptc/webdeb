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

package be.webdeb.core.api.text;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface represents a type of text, like articles, interviews, thesis, etc.
 * Accepted values are loaded at run time from a dedicated configuration database table.
 *
 * For more info, see data in table t_text_type
 *
 * Currently, those types are
 * <ul>
 *   <li>0 - artistic</li>
 *   <li>1 - discussion (e.g. interview)</li>
 *   <li>2 - journalistic</li>
 *   <li>3 - normative</li>
 *   <li>4 - opinion</li>
 *   <li>5 - prospective</li>
 *   <li>6 - practical</li>
 *   <li>7 - advertising</li>
 *   <li>8 - scientific</li>
 *   <li>9 - scientific</li>
 *   <li>10 - other</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see ETextType
 */
public interface TextType extends PredefinedIntValue {

  /**
   * Get the corresponding ETextType value to this.
   *
   * @return the ETextType enum value corresponding to this text type
   */
  ETextType getEType();
}