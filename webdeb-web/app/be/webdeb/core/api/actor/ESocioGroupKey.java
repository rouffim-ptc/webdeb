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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration represents the group key for sociography visualization.
 * <pre>
 *   0 for author
 *   1 for age
 *   2 for country
 *   3 for function
 *   4 for organization
 * </pre>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

public enum ESocioGroupKey {

    /**
     * group by author
     */
    AUTHOR(0),

    /**
     * group by ages ranges
     */
    AGE(1),

    /**
     * group by country
     */
    COUNTRY(2),

    /**
     * group by function name (Profession)
     */
    FUNCTION(3),

    /**
     * group by affiliation organization
     */
    ORGANIZATION(4);

    private int id;
    private static Map<Integer, ESocioGroupKey> map = new LinkedHashMap<>();

    static {
        for (ESocioGroupKey type : ESocioGroupKey.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a group key
     */
    ESocioGroupKey(int id) {
        this.id = id;
    }

    /**
     * Get all available keys
     *
     * @return a collection of ESocioGroupKey
     */
    public static Collection<ESocioGroupKey> getAll() {
        return map.values();
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a group key
     * @return the ESocioGroupKey enum value corresponding to the given id, null otherwise.
     */
    public static ESocioGroupKey value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this group key
     */
    public int id() {
        return id;
    }
}
