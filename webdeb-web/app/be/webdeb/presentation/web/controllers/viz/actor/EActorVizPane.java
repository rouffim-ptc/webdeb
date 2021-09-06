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

package be.webdeb.presentation.web.controllers.viz.actor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple enum to select across the available visualization panes for actor contribution
 * When you want to add a new tab in visualization screens, you must:
 *
 * @author Martin Rouffiange
 */
public enum EActorVizPane {

    /**
     *
     */
    PRESENTATION(0),

    /**
     *
     */
    IDENTITY(1),

    /**
     *
     */
    AFFILIATIONS(2),

    /**
     *
     */
    PERSONS(3),

    /**
     *
     */
    ABOUT(4),

    /**
     *
     */
    ARGUMENTS(5),

    /**
     * Allies / Opponents pane
     */
    SOCIOGRAPHY(6),

    /**
     *
     */
    BIBLIOGRAPHY(7);

    private int id;
    private static Map<Integer, EActorVizPane> map = new LinkedHashMap<>();

    static {
        for (EActorVizPane type : EActorVizPane.values()) {
            map.put(type.id, type);
        }
    }

    EActorVizPane(int id) {
        this.id = id;
    }

    /**
     * Get a pane enum value
     *
     * @param id an id
     * @return this pane enum value, null if non-existing id passed
     */
    public static EActorVizPane value(int id) {
        return map.get(id);
    }

    /**
     * Check if this is a subtype of EActorVizPane.PRESENTATION or PRESENTATION
     *
     * @return true if it is the case
     */
    public boolean isPresentationSubtype() {
        return this == EActorVizPane.PRESENTATION || this == EActorVizPane.ABOUT || this == EActorVizPane.AFFILIATIONS
                || this == EActorVizPane.PERSONS || this == EActorVizPane.IDENTITY;
    }

    /**
     * Get this pane id
     *
     * @return this pane id
     */
    public int id() {
        return id;
    }
}

