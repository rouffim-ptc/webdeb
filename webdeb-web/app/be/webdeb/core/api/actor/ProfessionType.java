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
 *
 */

package be.webdeb.core.api.actor;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface represents a profession type for person affiliated to organizations.
 *
 * Used with EProfessionType enum, where consistence rules are explained. Those rules are verified by the
 * Profession object itself.
 *
 * @author Martin Rouffiange
 * @see EProfessionType
 */
public interface ProfessionType extends PredefinedIntValue {

    /**
     * Get the profession subtype id value
     *
     * @return a value representating the profession subtype
     */
    int getSubId();

    /**
     * Get the corresponding EProfessionType value to this.
     *
     * @return the EProfessionType enum value corresponding to this profession type
     */
    EProfessionType getEType();
}
