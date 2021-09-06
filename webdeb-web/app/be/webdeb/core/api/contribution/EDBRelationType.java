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

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration holds types for relations type (one to many, many to one, simple field)
 * <ul>
 *   <li>-1 for NONE/li>
 *   <li>0 for SIMPLE_FIELD</li>
 *   <li>1 for MANY_TO_MANY</li>
 *   <li>1 for ONE_TO_ONE</li>
 *   <li>1 for MANY_TO_ONE</li>
 *   <li>2 for ONE_TO_MANY</li>
 * </ul>
 *
 * @author Martin Rouffiange
 */
public enum EDBRelationType {

    /**
     * No relation
     */
    NONE(-1),
    /**
     * simple field, no relation
     */
    SIMPLE_FIELD(0),

    /**
     * many to many relation
     */
    MANY_TO_MANY(1),

    /**
     * one to one relation
     */
    ONE_TO_ONE(2),

    /**
     * many to one relation
     */
    MANY_TO_ONE(3),

    /**
     * one to many relation
     */
    ONE_TO_MANY(4);

    private int id;
    private static Map<Integer, EDBRelationType> map = new LinkedHashMap<>();

    static {
        for (EDBRelationType type : EDBRelationType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Constructor
     *
     * @param id an int representing a db relation type
     */
    EDBRelationType(int id) {
        this.id = id;
    }


    /**
     * Get the enum value for a given id
     *
     * @param id an int representing an EDBRelationType
     * @return the EDBRelationType enum value corresponding to the given id, null otherwise.
     */
    public static EDBRelationType value(int id) {
        return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this db relation type
     */
    public int id() {
        return id;
    }

    /**
     * Converts a given annotation to EDBRelationType
     *
     * @return a EDBRelationType
     */
    public static EDBRelationType annotationToType(Annotation annotation){
        if(annotation instanceof ManyToMany){
            return EDBRelationType.MANY_TO_MANY;
        }else if(annotation instanceof OneToOne){
            return EDBRelationType.ONE_TO_ONE;
        }else if(annotation instanceof ManyToOne){
            return EDBRelationType.MANY_TO_ONE;
        }else if(annotation instanceof OneToMany){
            return EDBRelationType.ONE_TO_MANY;
        }
        return EDBRelationType.NONE;
    }

    /**
     * True if this relation type is a join type
     *
     * @return true if it is a join type
     */
    public boolean isJoinType(){
        return this == ONE_TO_ONE || this == MANY_TO_MANY || this == ONE_TO_MANY || this == MANY_TO_ONE;
    }
}
