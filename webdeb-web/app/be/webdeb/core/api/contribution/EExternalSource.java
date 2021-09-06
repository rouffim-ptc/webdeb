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
 *   <li>0 for TWITTER</li>
 *   <li>1 for CHROME EXTENSION</li>
 *   <li>2 for FIREFOX EXTENSION</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EExternalSource {

    /**
     * the external source is Chrome extension
     */
    CHROME_EXTENSION(1),

    /**
     * the external source is Firefox extension
     */
    FIREFOX_EXTENSION(2),

    /**
     * the external source is twitter, or more exactly WDTAL service
     */
    TWITTER(3);

    private int id;
    private static Map<Integer, EExternalSource> map = new LinkedHashMap<>();

    static {
        for (EExternalSource source : EExternalSource.values()) {
            map.put(source.id, source);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing an external source name
     */
    EExternalSource(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EExternalSource
     * @return the EExternalSource enum value corresponding to the given id, null otherwise.
     */
    public static EExternalSource value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this external source name
     */
    public int id() {
        return id;
    }
}
