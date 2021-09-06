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
 * This enumeration holds available date precision type
 * <ul>
 *   <li>Exactly since (past)</li>
 *   <li>At least since (past)</li>
 *   <li>Exactly until (futur)</li>
 *   <li>At least until (futur)</li>
 *   <li>Expected until (futur)</li>
 *   <li>Ongoing (futur)</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EPrecisionDate {
    /**
     * Exactly since (past)
     */
    EXACTLY_SINCE(0),
    /**
     * At least since (past)
     */
    AT_LEAST_SINCE(1),
    /**
     * Exactly until (futur)
     */
    EXACTLY_UNTIL(2),
    /**
     * At least until (futur)
     */
    AT_LEAST_UNTIL(3),
    /**
     * Expected until (futur)
     */
    EXPECTED_UNTIL(4),
    /**
     * Ongoing (futur)
     */
    ONGOING(5);

    private int id;
    private static Map<Integer, EPrecisionDate> map = new LinkedHashMap<>();

    static {
        for (EPrecisionDate type : EPrecisionDate.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a date precision type
     */
    EPrecisionDate(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EPrecisionDate
     * @return the EPrecisionDate enum value corresponding to the given id, null otherwise.
     */
    public static EPrecisionDate value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this date precision type
     */
    public Integer id() {
        return id;
    }

    /**
     * Get the flag that says if this type concerns a past date type
     *
     * @return true if this type concerns a past date type
     */
    public boolean isPast(){
        switch (this){
            case EXACTLY_SINCE :
            case AT_LEAST_SINCE :
                return true;
            case EXACTLY_UNTIL :
            case AT_LEAST_UNTIL :
            case EXPECTED_UNTIL :
            case ONGOING :
            default:
                return false;
        }
    }

    /**
     * Get the flag that says if this type is interesting to be displayed
     *
     * @return true if this type is interesting to be displayede
     */
    public boolean isDrawable(){
        switch (this){
            case EXACTLY_SINCE :
            case EXACTLY_UNTIL :
                return false;
            case AT_LEAST_SINCE :
            case AT_LEAST_UNTIL :
            case EXPECTED_UNTIL :
            case ONGOING :
            default:
                return true;
        }
    }

}
