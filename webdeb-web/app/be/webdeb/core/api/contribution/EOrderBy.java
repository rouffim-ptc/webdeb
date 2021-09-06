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
 * This enumeration holds external source names
 * <ul>
 *   <li>0 for order by POPULARITY</li>
 *   <li>1 for order by RELEVANCE/li>
 *   <li>2 for order by VERSION DESC</li>
 *   <li>3 for order by VERSION ASC</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EOrderBy {

    /**
     * order by POPULARITY
     */
    POPULARITY(0),
    /**
     * order by RELEVANCE
     */
    RELEVANCE(1),
    /**
     * order by VERSION DESC
     */
    VERSION_DESC(2),
    /**
     * order by VERSION ASC
     */
    VERSION_ASC(3);

    private Integer id;
    private static Map<Integer, EOrderBy> map = new LinkedHashMap<>();

    static {
        for (EOrderBy source : EOrderBy.values()) {
            map.put(source.id, source);
        }
    }

    /**
     * Constructor
     *
     * @param id a Integer representing a order by key
     */
    EOrderBy(Integer id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an Integer representing an EOrderBy
     * @return the EOrderBy enum value corresponding to the given id, null otherwise.
     */
    public static EOrderBy value(Integer id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return a Integer representation of this order by key
     */
    public Integer id() {
        return id;
    }

    public String toSQL() {
        switch (this) {
            case VERSION_DESC:
                return "c.version desc";
            case VERSION_ASC:
                return "c.version";
            case RELEVANCE:
                return "chr.relevance desc";
            default:
                return "chr.hit desc";
        }
    }
}
