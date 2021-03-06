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
package be.webdeb.core.api.debate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for debates
 * <pre>
 *   0 for normal debate
 *   1 for debate that comes from a tag
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EDebateType {

    /**
     * debate has only one answer
     */
    NORMAL(0),
    /**
     * debate comes from a tag
     */
    TAG_DEBATE(1);

    private int id;

    private static Map<Integer, EDebateType> map = new LinkedHashMap<>();

    static {
        for (EDebateType type : EDebateType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a Debate type
     */
    EDebateType(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a debate type
     * @return the EDebateType enum value corresponding to the given id, null otherwise.
     */
    public static EDebateType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this debate type
     */
    public int id() {
        return id;
    }
}
