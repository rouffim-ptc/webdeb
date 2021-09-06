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

package be.webdeb.presentation.web.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration is used to know the way to send data to view
 * <ul>
 *   <li>0 if data must be send as JSON</li>
 *   <li>1 if data must be send as HTML with template 1</li>
 *   <li>2 if data must be send as HTML with template 2</li>
 *   <li>3 if data must be send as HTML with template 3</li>
 *   <li>4 if data must be send as HTML with template 4</li>
 *   <li>5 if data must be send as HTML with template 5</li>
 * </ul>
 * @author Martin Rouffiange
 */
public enum EDataSendType {

    /**
     * data must be send as JSON
     */
    AS_JSON(0),

    /**
     * data must be send as HTML with template 1
     */
    AS_HTML_TEMPLATE_1(1),

    /**
     * data must be send as HTML with template 2
     */
    AS_HTML_TEMPLATE_2(2),

    /**
     * data must be send as HTML with template 3
     */
    AS_HTML_TEMPLATE_3(3),

    /**
     * data must be send as HTML with template 4
     */
    AS_HTML_TEMPLATE_4(4),

    /**
     * data must be send as HTML with template 5
     */
    AS_HTML_TEMPLATE_5(5);

    private int id;
    private static Map<Integer, EDataSendType> map = new LinkedHashMap<>();

    static {
        for (EDataSendType type : EDataSendType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a type key
     */
    EDataSendType(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a type key
     * @return the EDataSendType enum value corresponding to the given id, null otherwise.
     */
    public static EDataSendType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this type key
     */
    public int id() {
        return id;
    }
}
