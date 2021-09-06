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
 * This interface represents a type of source for text, like interne, pdf file or other kind of source.
 * Accepted values are loaded at run time from a dedicated configuration database table.
 *
 * For more info, see data in table t_text_type
 *
 * Currently, those types are
 * <ul>
 *   <li>0 - Internet</li>
 *   <li>1 - PDF file</li>
 *   <li>2 - Other</li>
 * </ul>
 *
 * @author Martin Rouffiange
 * @see ETextSourceType
 */
public interface TextSourceType extends PredefinedIntValue {

    /**
     * Get the corresponding ETextSourceType value to this.
     *
     * @return the ETextSourceType enum value corresponding to this text source type
     */
    ETextSourceType getEType();
}