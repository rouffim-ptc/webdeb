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
package be.webdeb.core.api.contribution.link;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This enumeration holds shades for justification links
 * <pre>
 * Justification; (uni-directional ,ie A SUPPORTS B is not equivalent to B SUPPORTS A)
 *   0 for SUPPORTS
 *   1 for REJECTS
 * </pre>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EJustificationLinkShade {

    NONE(-1),
    /**
     * supporting arguments / citation, sustaining an argument (justification-type link, uni-directional)
     */
    SUPPORTS(0),
    /**
     * rejecting arguments / citation, disagreeing on an argument (justification-type link, uni-directional)
     */
    REJECTS(1);

    private int id;

    private static Map<Integer, EJustificationLinkShade> map = new LinkedHashMap<>();

    static {
        for (EJustificationLinkShade type : EJustificationLinkShade.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a justification link shade
     */
    EJustificationLinkShade(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a justification link shade
     * @return the EJustificationLinkShade enum value corresponding to the given id, null otherwise.
     */
    public static EJustificationLinkShade value(int id) {
        return map.get(id);
    }

    public EJustificationLinkShade reverse() {
        switch (this) {
            case SUPPORTS:
                return REJECTS;
            case REJECTS:
                return SUPPORTS;
        }

        return this;
    }

    /**
     * Get this id
     *
     * @return an int representation of this justification link shade
     */
    public int id() {
        return id;
    }

    public static List<EJustificationLinkShade> valuesAsList(){
        return Arrays.asList(SUPPORTS, REJECTS);
    }

    public String toStyleName() {
        switch (this) {
            case SUPPORTS:
                return "justify";
            case REJECTS:
                return "opposes";
        }

        return "unshaded";
    }

    public String getLogo() {
        switch (this) {
            case SUPPORTS:
                return "fas fa-thumbs-up";
            case REJECTS:
                return "fas fa-thumbs-down";
        }

        return "";
    }
}
