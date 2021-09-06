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
 * This enumeration represents the subtype of affiliations
 * <ul>
 *   <li>0 for AFFILIATION</li>
 *   <li>1 for AFFILIATED</li>
 *   <li>2 for FILIATION</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EAffiliationSubtype {

    /**
     * Type is affiliation
     */
    AFFILIATION(0),
    /**
     * Type is affiliated
     */
    AFFILIATED(1),
    /**
     * Type is filiation
     */
    FILIATION(2);

    private int id;
    private static Map<Integer, EAffiliationSubtype> map = new LinkedHashMap<>();

    static {
        for (EAffiliationSubtype type : EAffiliationSubtype.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing an affiliation subtype
     */
    EAffiliationSubtype(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EAffiliationSubtype
     * @return the EAffiliationSubtype enum value corresponding to the given id, null otherwise.
     */
    public static EAffiliationSubtype value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this EAffiliationSubtype
     */
    public int id() {
        return id;
    }

}
