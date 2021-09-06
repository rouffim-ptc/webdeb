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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for text source types
 * <pre>
 *   0 for Internet
 *   1 for PDF
 *   2 for Other source
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum ETextSourceType {

    /**
     * The text comes from internet
     */
    INTERNET(0),
    /**
     * The text comes from a PDF file
     */
    PDF(1),
    /**
     * Other source
     */
    OTHER(2);

    private int id;
    private static Map<Integer, ETextSourceType> map = new LinkedHashMap<>();

    static {
        for (ETextSourceType type : ETextSourceType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a text type
     */
    ETextSourceType(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an ETextSourceType
     * @return the ETextSourceType enum value corresponding to the given id, null otherwise.
     */
    public static ETextSourceType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this type
     */
    public Integer id() {
        return id;
    }
}
