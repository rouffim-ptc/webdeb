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

package be.webdeb.infra.persistence.model;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ETechnicalTableName {

    /**
     * TActorType
     */
    ACTOR_TYPE("actor_type"),
    /**
     * TAffiliationType
     */
    AFFILIATION_TYPE("affiliation_type"),
    /**
     * TArgumentShadeType
     */
    ARGUMENT_SHADE_TYPE("argument_shade_type"),
    /**
     * TDebateShadeType
     */
    DEBATE_SHADE_TYPE("debate_shade_type"),
    /**
     * TArgumentType
     */
    ARGUMENT_TYPE("argument_type"),
    /**
     * TABusinessSector
     */
    BUSINESS_SECTOR("business_sector"),
    /**
     * TContributionType
     */
    CONTRIBUTION_TYPE("contribution_type"),
    /**
     * TCountry
     */
    COUNTRY("country"),
    /**
     * TFolderType
     */
    FOLDER_TYPE("folder_type"),
    /**
     * TGender
     */
    GENDER("gender"),
    /**
     * TLegalStatus
     */
    LEGAL_STATUS("legal_status"),
    /**
     * TPlaceType
     */
    PLACE_TYPE("place_type"),
    /**
     * TProfessionType
     */
    PROFESSION_TYPE("profession_type"),
    /**
     * TTextType
     */
    TEXT_TYPE("text_type");


    private String id;
    private static Map<String, ETechnicalTableName> map = new LinkedHashMap<>();

    static {
        for (ETechnicalTableName type : ETechnicalTableName.values()) {
            map.put(type.id, type);
        }
    }

    ETechnicalTableName(String id) {
        this.id = id;
    }

    /**
     * Get a technical table name enum value
     *
     * @param id an id
     * @return this technical table name enum value, null if non-existing id passed
     */
    public static ETechnicalTableName value(String id) {
        return map.get(id);
    }

    /**
     * Get this technical table name id
     *
     * @return this technical table name id
     */
    public String id() {
        return id;
    }

    public String getTableName(){
        return "t_" + id;
    }
}
