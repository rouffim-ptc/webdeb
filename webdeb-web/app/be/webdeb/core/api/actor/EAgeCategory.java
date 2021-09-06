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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds type of actor age category
 * <ul>
 *   <li>-1 for unknown</li>
 *   <li>0 for - 15 years old</li>
 *   <li>1 for 15 - 25 years old</li>
 *   <li>2 for 26 - 35 years old</li>
 *   <li>3 for 36 - 35 years old</li>
 *   <li>4 for 46 - 35 years old</li>
 *   <li>5 for 56 - 35 years old</li>
 *   <li>6 for 66 +</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EAgeCategory {

    /**
     * unknown
     */
   UNKNOWN(-1),
    /**
     * 0 to 14 years old
     */
    AGES_0_14(0),
    /**
     * 15 to 25 years old
     */
    AGES_15_25(1),
    /**
     * 26 to 35 years old
     */
    AGES_26_35(2),
    /**
     * 36 to 45 years old
     */
    AGES_36_45(3),
    /**
     * 46 to 55 years old
     */
    AGES_46_55(4),
    /**
     * 56 to 65 years old
     */
    AGES_56_65(5),
    /**
     * 66 and + years old
     */
    AGES_66_PLUS(6);

    private int id;
    private static Map<Integer, EAgeCategory> map = new LinkedHashMap<>();

    static {
        for (EAgeCategory source : EAgeCategory.values()) {
            map.put(source.id, source);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing an actor age category
     */
    EAgeCategory(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EAgeCategory
     * @return the EAgeCategory enum value corresponding to the given id, null otherwise.
     */
    public static EAgeCategory value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this actor age category
     */
    public int id() {
        return id;
    }
}
