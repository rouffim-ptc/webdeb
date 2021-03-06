/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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

import be.webdeb.core.api.contributor.EOpenIdType;
import be.webdeb.core.api.debate.EDebateType;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the t_openid_type database table. It determines the type of open id (from where the user used it, facebook, google,...)
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_openid_type")
public class TOpenIdType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TOpenIdType> find = new Model.Finder<>(TOpenIdType.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    /**
     * Get the type id
     *
     * @return an openid type id
     */
    public int getIdType() {
        return idType;
    }

    /**
     * Set the type id
     *
     * @param idType an openid type id
     */
    public void setIdType(int idType) {
        this.idType = idType;
    }

    /**
     * Get the EOpenIdType corresponding to this db type link
     *
     * @return the EOpenIdType corresponding
     */
    public EOpenIdType getEOpenIdType(){
        return EOpenIdType.value(idType);
    }
}
