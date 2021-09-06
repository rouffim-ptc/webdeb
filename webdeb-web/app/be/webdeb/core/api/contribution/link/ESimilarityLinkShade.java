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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds shades for argumentation links
 * <pre>
 * Similarity (bidirectional, ie, A SIMILAR B is equivalent to B SIMILAR B)
 *   0 for SIMILAR
 *   1 for OPPOSES
 *   2 for TRANSLATES
 *
 * </pre>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum ESimilarityLinkShade {

    /**
     * similar arguments / debates, expressing the same meaning (similarity-type link, bi-directional)
     */
    SIMILAR(0),
    /**
     * opposed arguments / debates, expressing a contrary meaning (similarity-type link, bi-directional)
     */
    OPPOSES(1),
    /**
     * translated arguments / debates, expressing the same meaning in another language (similarity-type link, bi-directional)
     */
    TRANSLATES(2);

    private int id;

    private static Map<Integer, ESimilarityLinkShade> map = new LinkedHashMap<>();

    static {
        for (ESimilarityLinkShade type : ESimilarityLinkShade.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a similarity link shade
     */
    ESimilarityLinkShade(int id) {
        this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a similarity link shade
     * @return the ESimilarityLinkShade enum value corresponding to the given id, null otherwise.
     */
    public static ESimilarityLinkShade value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this similarity link shade
     */
    public int id() {
        return id;
    }

}
