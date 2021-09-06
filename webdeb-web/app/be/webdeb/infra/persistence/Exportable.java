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
package be.webdeb.infra.persistence;

import org.slf4j.Logger;

import java.util.*;

/**
 * This interface represents a query made by user to export datum from persistence
 *
 * @author Martin Rouffiange
 */
public class Exportable {

    private static final Logger logger = play.Logger.underlying();

    private Map<String,String[]> query;
    private boolean initialized;
    private EModelKey model;
    private List<String> attributes;
    private EModelKey relatedToModel;
    private List<ERelatedJoinKey> relatedToJoins;
    private List<Long> relatedToIds;

    public Exportable(Map<String, String[]> query) {
        logger.debug(query+"/");
        this.query = query;
        this.initialized = false;
    }

    public void initialize() throws Exception {
        // needed items
        model = getModel(EExportableKey.MODEL);
        attributes = getAttributes();

        // optional items
        try {
            relatedToModel = getModel(EExportableKey.RELATED_TO_MODEL);
            relatedToJoins = getRelatedToJoin();
            relatedToIds = getRelatedToIds();
        } catch (Exception e){
            // nothing to do
        }

        this.initialized = true;
    }

    public String makeSqlQuery() throws Exception {
        if(initialized){
            String ch = "SELECT " + String.join(", ", attributes) + " FROM " + model.id;
            return ch;
        } else {
            throw new Exception();
        }
    }

    private String[] getDataFromQuery(String key) throws Exception {
        String[] data = query.get(key);

        if(data == null || data.length == 0){
            throw new Exception();
        }

        return data;
    }

    private EModelKey getModel(EExportableKey key) throws Exception {
        String model = getDataFromQuery(key.id)[0];

        if(EModelKey.value(model) == null){
            throw new Exception();
        }

        return EModelKey.value(model);
    }

    private List<String> getAttributes() throws Exception {
        return Arrays.asList(getDataFromQuery(EExportableKey.ATTRIBUTES.id));
    }

    private List<ERelatedJoinKey> getRelatedToJoin() throws Exception {
        String[] joins = getDataFromQuery(EExportableKey.RELATED_TO_JOINS.id);
        List<ERelatedJoinKey> relatedJoins = new ArrayList<>();

        for(String join : joins){
            if(ERelatedJoinKey.value(join) != null) {
                relatedJoins.add(ERelatedJoinKey.value(join));
            }
        }

        return relatedJoins;
    }

    private List<Long> getRelatedToIds() throws Exception {
        String[] ids = getDataFromQuery(EExportableKey.RELATED_TO_IDS.id);
        List<Long> convertedIds = new ArrayList<>();

        for(String id : ids){
            try {
                convertedIds.add(Long.decode(id));
            } catch (NumberFormatException e) {
                // ignored number
            }
        }

        return convertedIds;
    }

    /**
     * This enum regroups all possible value for the exportable query
     *
     * @author Martin Rouffiange
     */
    private enum EExportableKey {

        /**
         *
         */
        MODEL("model"),
        /**
         *
         */
        ATTRIBUTES("attributes[]"),
        /**
         *
         */
        RELATED_TO_MODEL("relatedToModel"),
        /**
         *
         */
        RELATED_TO_JOINS("relatedToJoins[]"),
        /**
         *
         */
        RELATED_TO_IDS("relatedToIds[]");

        private String id;
        private static Map<String, EExportableKey> map = new LinkedHashMap<>();

        static {
            for (EExportableKey type : EExportableKey.values()) {
                map.put(type.id, type);
            }
        }

        EExportableKey(String id) {
            this.id = id;
        }

        /**
         * Get a exportable key
         *
         * @param id an id
         * @return this exportable key value
         */
        public static EExportableKey value(String id) {
            return map.get(id);
        }

        /**
         * Get this exportable key value
         *
         * @return this exportable key value
         */
        public String id() {
            return id;
        }
    }

    /**
     * This enum regroups all possible value for an exportable model
     *
     * @author Martin Rouffiange
     */
    private enum EModelKey {

        /**
         *
         */
        ACTOR("actor"),
        /**
         *
         */
        OPINION("opinion"),
        /**
         *
         */
        CITATION("citation"),
        /**
         *
         */
        DEBATE("debate"),
        /**
         *
         */
        TEXT("text"),
        /**
         *
         */
        TAG("tag");

        private String id;
        private static Map<String, EModelKey> map = new LinkedHashMap<>();

        static {
            for (EModelKey type : EModelKey.values()) {
                map.put(type.id, type);
            }
        }

        EModelKey(String id) {
            this.id = id;
        }

        /**
         * Get a exportable key
         *
         * @param id an id
         * @return this exportable key value
         */
        public static EModelKey value(String id) {
            return map.get(id);
        }

        /**
         * Get this exportable key value
         *
         * @return this exportable key value
         */
        public String id() {
            return id;
        }
    }

    /**
     * This enum regroups all possible for join key
     *
     * @author Martin Rouffiange
     */
    private enum ERelatedJoinKey {

        /**
         *
         */
        AFFILIATED("id_actor"),
        /**
         *
         */
        AFFILIATION("id_actor_as_affiliation");

        private String id;
        private static Map<String, ERelatedJoinKey> map = new LinkedHashMap<>();

        static {
            for (ERelatedJoinKey type : ERelatedJoinKey.values()) {
                map.put(type.id, type);
            }
        }

        ERelatedJoinKey(String id) {
            this.id = id;
        }

        /**
         * Get a exportable key
         *
         * @param id an id
         * @return this exportable key value
         */
        public static ERelatedJoinKey value(String id) {
            return map.get(id);
        }

        /**
         * Get this exportable key value
         *
         * @return this exportable key value
         */
        public String id() {
            return id;
        }
    }


}
