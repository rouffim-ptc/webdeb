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
 * This enumeration holds shades for position links
 * <pre>
 *   0 for HIGH APPROBATION position
 *   1 for APPROBATION position
 *   2 for INDECISION position
 *   3 for DISAPPROVAL position
 *   4 for HIGH DISAPPROVAL position
 * </pre>
 *
 * @author Martin Rouffiange
 */
public enum EPositionLinkShade {

    /**
     * Actor has an high approbation position in the debate
     */
    HIGH_APPROBATION(0),
    /**
     * Actor has an approbation position in the debate
     */
    APPROBATION(1),
    /**
     * Actor has an indecision position in the debate
     */
    INDECISION(2),
    /**
     * Actor has a disapproval position in the debate
     */
    DISAPPROVAL(3),
    /**
     * Actor has an high disapproval position in the debate
     */
    HIGH_DISAPPROVAL(4);

    private int id;

    private static Map<Integer, EPositionLinkShade> map = new LinkedHashMap<>();

    static {
        for (EPositionLinkShade type : EPositionLinkShade.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a position link shade
     */
    EPositionLinkShade(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a position link shade
     * @return the EPositionLinkShade enum value corresponding to the given id, null otherwise.
     */
    public static EPositionLinkShade value(int id) {
        return map.get(id);
    }

    public EPositionLinkShade reverse() {
        switch (this) {
            case HIGH_APPROBATION:
                return HIGH_DISAPPROVAL;
            case APPROBATION:
                return DISAPPROVAL;
            case DISAPPROVAL:
                return APPROBATION;
            case HIGH_DISAPPROVAL:
                return HIGH_APPROBATION;
        }

        return this;
    }

    /**
     * Get this id
     *
     * @return an int representation of this position link shade
     */
    public int id() {
        return id;
    }

    public static List<EPositionLinkShade> valuesAsList(){
        return Arrays.asList(HIGH_APPROBATION, APPROBATION, INDECISION, DISAPPROVAL, HIGH_DISAPPROVAL);
    }

    public String toStyleName() {
        return this.name().toLowerCase();
    }

    public String getLogo() {
        switch (this) {
            case HIGH_APPROBATION:
            case APPROBATION:
                return "fas fa-thumbs-up";
            case DISAPPROVAL:
            case HIGH_DISAPPROVAL:
                return "fas fa-thumbs-down";
            case INDECISION:
                return "fas fa-thumbs-up fa-rotate-270";
        }

        return "";
    }
}
