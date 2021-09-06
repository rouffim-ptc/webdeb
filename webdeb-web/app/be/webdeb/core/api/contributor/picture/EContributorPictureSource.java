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
 * This enumeration holds types of source for pictures added by contributor
 * <ul>
 *   <li>0 for picture created by contributor</li>
 *   <li>1 for picture takes from an open data bank</li>
 *   <li>2 for picture takes from another open source</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EContributorPictureSource {

    /**
     * Picture source is unknown
     */
    UNKNOWN(-1),
    /**
     * Picture created by contributor
     */
    OWN(0),
    /**
     * Picture takes from an open data bank
     */
    OPEN_BANK(1),
    /**
     * Picture takes from another open source
     */
    OTHER(2);


    private int id;

    private static Map<Integer, EContributorPictureSource> map = new LinkedHashMap<>();

    static {
        for (EContributorPictureSource type : EContributorPictureSource.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a picture source
     */
    EContributorPictureSource(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a picture source
     * @return the EContributorPictureSource enum value corresponding to the given id, null otherwise.
     */
    public static EContributorPictureSource value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this picture source
     */
    public int id() {
        return id;
    }

}
