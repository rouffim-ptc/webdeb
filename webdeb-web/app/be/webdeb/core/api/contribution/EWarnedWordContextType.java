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

package be.webdeb.core.api.contribution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds values for warned word contexts. Context means where the word need a warning when it's appeard.
 * <ul>
 *   <li>PROFESSION(0) for profession names</li>
 *   <li>ARGUMENT(1) for argument titles</li>
 *   <li>ARGUMENT(2) for text tiles</li>
 *   <li>ARGUMENT(3) for tag names</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EWarnedWordContextType {
    /**
     * Warned word for profession names
     */
    PROFESSION(0),
    /**
     * Warned word for argument title
     */
    ARGUMENT(1),
    /**
     * Warned word for text title
     */
    TEXT(2),
    /**
     * Warned word for tag name
     */
    TAG(3);

    private int id;
    private static Map<Integer, EWarnedWordContextType> map = new LinkedHashMap<>();

    static {
        for (EWarnedWordContextType type : EWarnedWordContextType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a warned word context
     */
    EWarnedWordContextType(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EWarnedWordContextType
     * @return the EWarnedWordContextType enum value corresponding to the given id, null otherwise.
     */
    public static EWarnedWordContextType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this context type
     */
    public int id() {
        return id;
    }
}
