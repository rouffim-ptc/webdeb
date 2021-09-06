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

import be.webdeb.core.api.actor.EPrecisionDate;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_precision_date_type database table, holding predefined values for types of date precision,
 * like
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_precision_date_type")
public class TPrecisionDateType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TPrecisionDateType> find = new Model.Finder<>(TPrecisionDateType.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    @Column(name = "is_past", nullable = false)
    private int isInPast;

    /**
     * Get the precision date type id
     *
     * @return an id
     */
    public int getIdType() {
        return this.idType;
    }

    /**
     * Set the precision date type id
     *
     * @param idType an id
     */
    public void setIdType(int idType) {
        this.idType = idType;
    }

    /**
     * Check whether this precision date is in past or not
     *
     * @return true if it's in past
     */
    public boolean isInPast() {
        return isInPast == 1;
    }

    /**
     * Set the flag saying if this precision date is in past or not
     *
     * @param isInPast true if it's in past
     */
    public void setInPast(boolean isInPast) {
        this.isInPast = isInPast ? 1 : 0;
    }

    /**
     * Get the EPrecisionDate corresponding to this db precision date type
     *
     * @return the EPrecisionDate corresponding
     */
    public EPrecisionDate getEPrecisionDateType(){
        return EPrecisionDate.value(idType);
    }
}
