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

package be.webdeb.core.api.contributor.picture;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types of licence for pictures added by contributor
 * <ul>
 *   <li>-1 for NONE/li>
 *   <li>0 for CC BY 4.0</li>
 *   <li>1 for CC BY-SA 4.0</li>
 *   <li>2 for CC BY-ND 4.0</li>
 *   <li>3 for CC BY-NC 4.0</li>
 *   <li>4 for CC BY-NC-SA 4.0</li>
 *   <li>5 for CC BY-NC-ND 4.0</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EPictureLicenceType {

    /**
     * No licence or no known licence
     */
    NONE(-1),
    /**
     * Creative Commons 4.0 ATTRIBUTION
     */
    CC_BY(0),
    /**
     * Creative Commons 4.0 ATTRIBUTION and SHARE-ALIKE
     */
    CC_BY_SA(1),
    /**
     * Creative Commons 4.0 ATTRIBUTION and NO DERIVATIVE WORKS
     */
    CC_BY_ND(2),
    /**
     * Creative Commons 4.0 ATTRIBUTION and NON-COMMERCIAL
     */
    CC_BY_NC(3),
    /**
     * Creative Commons 4.0 ATTRIBUTION, NON-COMMERCIAL and SHARE-ALIKE
     */
    CC_BY_NC_SA(4),
    /**
     * Creative Commons 4.0 ATTRIBUTION, NON-COMMERCIAL and NO DERIVATIVE WORKS
     */
    CC_BY_NC_ND(5);


    private int id;

    private static Map<Integer, EPictureLicenceType> map = new LinkedHashMap<>();

    static {
        for (EPictureLicenceType type : EPictureLicenceType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a picture licence type
     */
    EPictureLicenceType(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a picture licence type
     * @return the EPictureLicenceType enum value corresponding to the given id, null otherwise.
     */
    public static EPictureLicenceType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this picture licence type
     */
    public int id() {
        return id;
    }

}
