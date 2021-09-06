/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.entry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration is used to specify type of comments for textual contribution
 *
 * @author Fabian Gilson
 */
public enum ECommentsType {


    /**
     * comments for which one and only one actor is the author
     */
    SIGNED(0),

    /**
     * comments that have been reported by another actor than the actor that actually said it
     */
    REPORTED(1),

    /**
     * multiple actors are referred as the authors of this comments
     */
    COSIGNED(2);

    private int id;
    private static Map<Integer, ECommentsType> map = new LinkedHashMap<>();

    static {
        for (ECommentsType type : ECommentsType.values()) {
            map.put(type.id, type);
        }
    }

    ECommentsType(int id) {
        this.id = id;
    }

    /**
     * Get a particular comment type by its id
     *
     * @param id a comment id
     * @return the ECommentsType corresponding to given value (may be null)
     */
    public static ECommentsType value(int id) {
        return map.get(id);
    }

    /**
     * Get this ECommentsType id
     *
     * @return identifying id of this ECommentsType
     */
    public int id() {
        return id;
    }
}
