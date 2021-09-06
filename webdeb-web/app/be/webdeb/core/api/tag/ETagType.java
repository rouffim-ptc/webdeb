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
package be.webdeb.core.api.tag;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for tags
 * <pre>
 *   0 for simple tag (or normal tag)
 *   1 for tag that are category for debate
 *   2 for tag that are sub debate of multiple debate
 *   3 for tag social objects
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum ETagType {

    /**
     * tag is a normal tag
     */
    SIMPLE_TAG(0),
    /**
     * tag is a category tag
     */
    CATEGORY_TAG(1),
    /**
     * tag is a subdebate of multiple debate
     */
    SUB_DEBATE_TAG(2),
    /**
     * tag is a organization social object
     */
    SOCIAL_OBJECT(3);

    private int id;

    private static Map<Integer, ETagType > map = new LinkedHashMap<>();

    static {
        for (ETagType  type : ETagType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a Tag type
     */
    ETagType(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a tag type
     * @return the ETagType enum value corresponding to the given id, null otherwise.
     */
    public static ETagType  value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this tag type
     */
    public int id() {
        return id;
    }
}
