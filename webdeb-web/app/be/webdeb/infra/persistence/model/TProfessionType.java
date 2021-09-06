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

package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.actor.EProfessionType;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_profession_type database table, holding predefined values for types of profession,
 * like chief, formation, ..
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_profession_type")
public class TProfessionType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TProfessionType> find = new Model.Finder<>(TProfessionType.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    @Column(name = "id_type", nullable = false)
    private int subtype;

    /**
     * Get the profession type id
     *
     * @return an id
     */
    public int getIdType() {
        return this.idType;
    }

    /**
     * Set the profession type id
     *
     * @param idType an id
     */
    public void setIdType(int idType) {
        this.idType = idType;
    }

    /**
     * Get the profession subtype id (Hierarchy or type)
     *
     * @return a profession subtype id
     */
    public int getSubtype() {
        return subtype;
    }

    /**
     * Set the profession subtype id (Hierarchy or type)
     *
     * @param subtype the profession subtype id
     */
    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    /**
     * Get the EProfessionType corresponding to this db profession type
     *
     * @return the EProfessionType corresponding
     */
    public EProfessionType getEProfessionType(){
        return EProfessionType.value(idType);
    }
}
