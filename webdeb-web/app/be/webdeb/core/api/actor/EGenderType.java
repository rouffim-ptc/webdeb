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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This enumeration holds the gender of a person
 * <ul>
 *   <li>- for UNKNOWN</li>
 *   <li>X for NEUTRAL</li>
 *   <li>M for MALE</li>
 *   </li>F for FEMALE</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EGenderType {

    /**
     * Person is neutral
     */
    UNKNOWN('-'),

    /**
     * Person is neutral
     */
    NEUTRAL('X'),

    /**
     * Person is a man
     */
    MALE('M'),

    /**
     * Person is a woman
     */
    FEMALE('F');

    private Character id;
    private static Map<Character, EGenderType> map = new LinkedHashMap<>();

    static {
        for (EGenderType type : EGenderType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id a char representing a person gender
     */
    EGenderType(char id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id a char representing an EGenderType
     * @return the EGenderType enum value corresponding to the given id, null otherwise.
     */
    public static EGenderType value(char id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return a char representation of this EGenderType
     */
    public char id() {
        return id;
    }

    /**
     * Get the list of EGenderType ids
     *
     * @return a list of EGenderType ids
     */
    public static List<Character> getGenderTypeIds() {
        return map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static EGenderType genderStringAsType(String gender) {
        if(gender == null || gender.length() == 0) {
            return EGenderType.UNKNOWN;
        }

        return EGenderType.value(gender.charAt(0));
    }
}
