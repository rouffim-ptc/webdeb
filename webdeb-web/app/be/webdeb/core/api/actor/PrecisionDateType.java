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
 *
 */

package be.webdeb.core.api.actor;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface represents a date precision type for Affiliation date.
 *
 * Used with EPrecisionDate enum, where consistence rules are explained. Those rules are verified by the
 * Affiliation object itself.
 *
 * @author Martin Rouffiange
 * @see EPrecisionDate
 */
public interface PrecisionDateType extends PredefinedIntValue {

    /**
     * Get the flag that says if precision date refers to a date in the past or not
     *
     * @return true if the related that is in the past
     */
    boolean isInPast();

    /**
     * Get the corresponding EPrecisionDate value to this.
     *
     * @return the EPrecisionDate enum value corresponding to this precision type
     */
    EPrecisionDate getEType();

}
