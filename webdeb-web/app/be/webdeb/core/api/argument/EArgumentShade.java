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

package be.webdeb.core.api.argument;

import be.webdeb.core.api.debate.EDebateShade;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds shade types for arguments
 * <pre>
 *   -1 for not shaded argument
 *   0 for There is no doubt that
 *   1 for It is necessary to
 *   2 for argument comes from debate strong and weak points
 *   2 for argument comes from debate what conditions
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EArgumentShade {

    /**
     * Argument has no shade
     */
    NO_SHADE(-1),
    /**
     * Argument begins with 'There is no doubt that'
     */
    NO_DOUBT(0),
    /**
     * Argument begins with 'It is necessary to'
     */
    NECESSARY(1),
    /**
     * Argument come from debate strong and weak points
     */
    STRONG_AND_WEAK_POINTS(2),
    /**
     * Argument come from debate what conditions
     */
    CONDITIONS(3),
    /**
     *  Argument come from debate 'What' in singular feminine in other languages
     */
    WHAT_SINGULAR_FEMININE(4),
    /**
     *  Argument come from debate 'What' in singular masculine in other languages
     */
    WHAT_SINGULAR_MASCULINE(5),
    /**
     *  Argument come from debate 'What' in plural feminine in other languages
     */
    WHAT_PLURAL_FEMININE(6),
    /**
     * Argument come from debate 'What' in plural masculine in other languages
     */
    WHAT_PLURAL_MASCULINE(7);

    private int id;

    private static Map<Integer, EArgumentShade> map = new LinkedHashMap<>();

    static {
        for (EArgumentShade type : EArgumentShade.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a shade type
     */
    EArgumentShade(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an argument shade type
     * @return the EArgumentShade enum value corresponding to the given id, null otherwise.
     */
    public static EArgumentShade value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this argument shade type
     */
    public int id() {
        return id;
    }

    /**
     * Convert this argument shade to debate shade
     *
     * @return the corresponding debate shade
     */
    public EDebateShade asDebateShade() {
        switch (this) {
            case NO_DOUBT :
                return EDebateShade.TRUE_OR_FALSE;
            case NECESSARY :
                return EDebateShade.SHOULD_WE;
            case CONDITIONS:
                return EDebateShade.HOW;
            case WHAT_SINGULAR_FEMININE:
                return EDebateShade.WHAT_SINGULAR_FEMININE;
            case WHAT_SINGULAR_MASCULINE:
                return EDebateShade.WHAT_SINGULAR_MASCULINE;
            case WHAT_PLURAL_FEMININE:
                return EDebateShade.WHAT_PLURAL_FEMININE;
            case WHAT_PLURAL_MASCULINE:
                return EDebateShade.WHAT_PLURAL_MASCULINE;
            default :
                return EDebateShade.NO_SHADE;
        }
    }

    /**
     * Convert this shade into is corresponding shade type
     *
     * @return the corresponding shade type
     * @see EArgumentShadeType
     */
    public EArgumentShadeType getShadeType() {
        return EArgumentShadeType.argumentShadeToArgumentShadeType(this);
    }

}
