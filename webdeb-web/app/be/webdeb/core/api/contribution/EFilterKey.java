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

package be.webdeb.core.api.contribution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds external source names
 * <ul>
 *   <li>0 for PUBLICATION_DATE</li>
 *   <li>1 for PLACE</li>
 *   <li>2 for TAG</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EFilterKey {

    /**
     * filter by contribution date
     */
    SEARCH_DATE("search_date"),
    /**
     * filter by citation publication date
     */
    CITATION_PUBLICATION_DATE("citation_publication_date"),

    /**
     * filter by text publication date
     */
    TEXT_PUBLICATION_DATE("text_publication_date"),

    /**
     * filter by actor type
     */
    ACTOR_TYPE("actor_type"),

    /**
     * filter by actor role
     */
    ACTOR_ROLE("actor_role"),

    /**
     * filter by affiliation
     */
    AFFILIATION("affiliation"),

    /**
     * filter by other affiliation
     */
    OTHER_AFFILIATION("other_affiliation"),

    /**
     * filter by affiliation function
     */
    AFFILIATION_FUNCTION("affiliation_function"),

    /**
     * filter by affiliation date
     */
    AFFILIATION_DATE("affiliation_date"),

    /**
     * filter by affiliation type
     */
    AFFILIATION_TYPE("affiliation_type"),

    /**
     * filter by contribution author
     */
    AUTHOR("author"),

    /**
     * filter by text author
     */
    TEXT_AUTHOR("text_author"),

    /**
     * filter by citation author
     */
    CITATION_AUTHOR("citation_author"),

    /**
     * filter by place
     */
    PLACE("place"),

    /**
     * filter by text type
     */
    TEXT_TYPE("text_type"),

    /**
     * filter by text source
     */
    TEXT_SOURCE("text_source"),

    /**
     * filter by citation's text source
     */
    CITATION_SOURCE("citation_source"),

    /**
     * filter by fulltext tag
     */
    FULLTEXT_TAG("fulltext_tag"),

    /**
     * filter by fulltext tag
     */
    FULLTEXT_TEXT("fulltext_text"),

    /**
     * filter by fulltext debate
     */
    FULLTEXT_DEBATE("fulltext_debate"),

    /**
     * filter by fulltext actor
     */
    FULLTEXT_ACTOR("fulltext_actor"),

    /**
     * filter by fulltext citation
     */
    FULLTEXT_CITATION("fulltext_citation"),

    /**
     * filter by fulltext citation
     */
    GENDER("gender"),

    /**
     * filter by tag
     */
    TAG("tag");

    private String id;
    private static Map<String, EFilterKey> map = new LinkedHashMap<>();

    static {
        for (EFilterKey source : EFilterKey.values()) {
            map.put(source.id, source);
        }
    }

    /**
     * Constructor
     *
     * @param id a String representing a filter key
     */
    EFilterKey(String id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an String representing an EFilterKey
     * @return the EFilterKey enum value corresponding to the given id, null otherwise.
     */
    public static EFilterKey value(String id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return a String representation of this filter key
     */
    public String id() {
        return id;
    }
}
