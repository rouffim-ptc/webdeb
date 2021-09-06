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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for arguments
 * <pre>
 *   0 for simple argument, the title is the full argument
 *   1 for shaded argument, the full argument is the shade + the title
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EArgumentType {

    /**
     * simple argument
     */
    SIMPLE(0),
    /**
     * shaded argument
     */
    SHADED(1);

    private int id;

    private static Map<Integer, EArgumentType> map = new LinkedHashMap<>();

    static {
        for (EArgumentType type : EArgumentType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing an Argument type
     */
    EArgumentType(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an argument type
     * @return the EArgumentType enum value corresponding to the given id, null otherwise.
     */
    public static EArgumentType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this argument type
     */
    public int id() {
        return id;
    }
}
