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

package be.webdeb.presentation.web.controllers.browse;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple enum that says how to display overviews
 *
 * @author Martin Rouffiange
 */
public enum EOverviewType {

    /**
     * Only one overview by line
     */
    SIMPLE(0),

    /**
     * Two overviews by line
     */
    DOUBLE(1),

    /**
     * Four overviews by line
     */
    QUADRA(2),

    /**
     * Six overviews by line
     */
    HEXA(3);

    private int id;
    private static Map<Integer, EOverviewType> map = new LinkedHashMap<>();

    static {
        for (EOverviewType type : EOverviewType.values()) {
            map.put(type.id, type);
        }
    }

    EOverviewType(int id) {
        this.id = id;
    }

    /**
     * Get a pane enum value
     *
     * @param id an id
     * @return this pane enum value, null if non-existing id passed
     */
    public static EOverviewType value(int id) {
        return map.get(id);
    }

    /**
     * Get this overview type
     *
     * @return this overview type
     */
    public int id() {
        return id;
    }

    public String getBootstrapDivision(){
        String ch = "col-12";

        if(id >= EOverviewType.DOUBLE.id()){
            ch += " col-sm-6";
        }

        if(id >= EOverviewType.QUADRA.id()){
            ch += " col-md-3";
        }

        if(id >= EOverviewType.HEXA.id()){
            ch += " col-lg-2";
        }

        return ch;
    }

    public boolean isMultiple(){
        return id > EOverviewType.SIMPLE.id();
    }
}
