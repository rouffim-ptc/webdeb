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

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The type of contributor picture source
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_contributor_picture_source")
public class TContributorPictureSource extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TContributorPictureSource> find = new Model.Finder<>(TContributorPictureSource.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    /**
     * Get the id of this picture source
     *
     * @return an id
     */
    public int getIdType() {
        return this.idType;
    }

    /**
     * Set the id of this picture source
     *
     * @param idType an id
     */
    public void setIdType(int idType) {
        this.idType = idType;
    }

}
