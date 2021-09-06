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
package be.webdeb.core.api.debate;

import be.webdeb.core.api.argument.EArgumentShade;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This enumeration holds shade types for arguments
 * <pre>
 *   -1 for not shaded debate
 *   0 for Should we
 *   1 for Is it true or false that
 *   2 for What are the strong and weak points
 *   3 for How
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EDebateShade {

    /**
     * Debate has no shade
     */
    NO_SHADE(-1),
    /**
     * Debate begins with 'Is it true or false that'
     */
    TRUE_OR_FALSE(0),
    /**IsMultiple
     * Debate begins with 'Should we'
     */
    SHOULD_WE(1),
    /**
     * Debate begins with 'What are the strong and weak points'
     */
    STRONG_WEAK_POINTS(2),
    /**
     * Debate begins with 'How'
     */
    HOW(3),
    /**
     * Debate begins with 'What' in singular feminine in other languages
     */
    WHAT_SINGULAR_FEMININE(4),
    /**
     * Debate begins with 'What' in singular masculine in other languages
     */
    WHAT_SINGULAR_MASCULINE(5),
    /**
     * Debate begins with 'What' in plural feminine in other languages
     */
    WHAT_PLURAL_FEMININE(6),
    /**
     * Debate begins with 'What' in plural masculine in other languages
     */
    WHAT_PLURAL_MASCULINE(7);

    private int id;

    private static Map<Integer, EDebateShade> map = new LinkedHashMap<>();

    static {
        for (EDebateShade type : EDebateShade.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a shade type
     */
    EDebateShade(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an argument shade type
     * @return the EArgumentShade enum value corresponding to the given id, null otherwise.
     */
    public static EDebateShade value(int id) {
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
     * Convert this debate shade into its corresponding argument shade
     *
     * @return the corresponding shade for argument
     */
    public EArgumentShade asArgumentShade() {
        switch (this){
            case TRUE_OR_FALSE :
                return EArgumentShade.NO_DOUBT;
            case SHOULD_WE :
                return EArgumentShade.NECESSARY;
            case STRONG_WEAK_POINTS :
                return EArgumentShade.STRONG_AND_WEAK_POINTS;
            case HOW:
                return EArgumentShade.CONDITIONS;
            case WHAT_SINGULAR_FEMININE:
                return EArgumentShade.WHAT_SINGULAR_FEMININE;
            case WHAT_SINGULAR_MASCULINE:
                return EArgumentShade.WHAT_SINGULAR_MASCULINE;
            case WHAT_PLURAL_FEMININE:
                return EArgumentShade.WHAT_PLURAL_FEMININE;
            case WHAT_PLURAL_MASCULINE:
                return EArgumentShade.WHAT_PLURAL_MASCULINE;
            default:
                return EArgumentShade.NO_SHADE;
        }
    }

    public boolean canBeMultiple() {
        return EDebateShade.canBeMultipleShades().contains(this);
    }

    public boolean isAlwaysMultiple() {
        return EDebateShade.alwaysMultipleShades().contains(this);
    }

    public static List<EDebateShade> canBeMultipleShades() {
        return Arrays.asList(STRONG_WEAK_POINTS);
    }

    public static List<EDebateShade> alwaysMultipleShades() {
        return Arrays.asList(WHAT_PLURAL_FEMININE, WHAT_PLURAL_MASCULINE, WHAT_SINGULAR_MASCULINE, WHAT_SINGULAR_FEMININE, HOW);
    }

    public static List<Integer> shadesToIds(List<EDebateShade> shades) {
        return shades.stream().map(shade -> shade.id).collect(Collectors.toList());
    }
}
