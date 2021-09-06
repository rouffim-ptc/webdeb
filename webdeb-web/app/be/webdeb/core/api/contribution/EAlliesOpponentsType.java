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

import be.webdeb.core.api.contribution.link.EJustificationLinkShade;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds type of allies of opponents vision
 * <ul>
 *   <li>0 for arguments vision</li>
 *   <li>1 for positions vision</li>
 *   <li>2 for tags vision</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EAlliesOpponentsType {

    /**
     * the vision from citation justification links
     */
    ARGUMENTS(0),

    /**
     * the vision from citation position links
     */
    POSITIONS(1),

    /**
     * the vision from citation tags
     */
    TAGS(2);

    private int id;
    private static Map<Integer, EAlliesOpponentsType> map = new LinkedHashMap<>();

    static {
        for (EAlliesOpponentsType source : EAlliesOpponentsType.values()) {
            map.put(source.id, source);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing an allies opponents vision type
     */
    EAlliesOpponentsType(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EAlliesOpponentsType
     * @return the EAlliesOpponentsType enum value corresponding to the given id, null otherwise.
     */
    public static EAlliesOpponentsType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this allies opponents vision type
     */
    public int id() {
        return id;
    }

    public int maxShades() {
        switch (this){
            case ARGUMENTS:
                return 2;
            case POSITIONS:
                return 5;
            default:
                return 1;
        }
    }
}
