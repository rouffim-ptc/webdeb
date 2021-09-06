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
 * This enumeration holds available profession type
 * <ul>
 *   <li>Formation</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EProfessionType {
    /**
     * Formation
     */
    OTHERS(0),
    /**
     * Formation
     */
    FORMATION(1);

    private int id;
    private static Map<Integer, EProfessionType> map = new LinkedHashMap<>();

    static {
        for (EProfessionType type : EProfessionType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a profession type
     */
    EProfessionType(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EProfessionType
     * @return the EProfessionType enum value corresponding to the given id, null otherwise.
     */
    public static EProfessionType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this profession type
     */
    public Integer id() {
        return id;
    }

    /**
     * Get the subtype of this profession type
     *
     * @return an int as subtype, 0 for hierarchy and 1 for formation
     */
    public int getSubtype(){
        switch (this){
            case FORMATION:
                return 1;
            case OTHERS:
            default:
                return 0;
        }
    }

}
