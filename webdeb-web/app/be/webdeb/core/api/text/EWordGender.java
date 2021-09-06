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

package be.webdeb.core.api.text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This enumeration holds types for text types
 * <pre>
 *   F for feminine word
 *   M for masculine word
 *   N for neuter word
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EWordGender {

    /**
     * Word is feminine
     */
    FEMININE('F'),

    /**
     * Word is masculine
     */
    MASCULINE('M'),

    /**
     * Word is neuter
     */
    NEUTER('F');

    private Character id;
    private static Map<Character, EWordGender> map = new LinkedHashMap<>();

    static {
        for (EWordGender type : EWordGender.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an char representing a word gender
     */
    EWordGender(char id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id a char representing an EWordGender
     * @return the EWordGender enum value corresponding to the given id, null otherwise.
     */
    public static EWordGender value(char id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return a char representation of this EWordGender
     */
    public char id() {
        return id;
    }

    /**
     * Get the list of EWordGender ids
     *
     * @return a list of EWordGender ids
     */
    public static List<Character> getGenderTypeIds() {
        return map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static EWordGender genderStringAsType(String gender) {
        if(gender == null || gender.length() == 0) {
            return NEUTER;
        }

        return EWordGender.value(gender.charAt(0));
    }
}
