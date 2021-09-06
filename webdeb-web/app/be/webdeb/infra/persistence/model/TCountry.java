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

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The persistent class for the t_country database table, holding predefined countries
 * for organizations, persons and contributors.
 *
 * Countries' ids are specified with their 2-char ISO 3166-1 alpha-2 codes
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "t_country")
public class TCountry extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<String, TCountry> find = new Model.Finder<>(TCountry.class);

  @Id
  @Column(name = "id_country", unique = true, nullable = false, length = 3)
  private String idCountry;

  /**
   * Get the country id
   *
   * @return an id (iso-3166-1 alpha-2 code)
   */
  public String getIdCountry() {
    return this.idCountry;
  }

    /**
   * Set the country id
   *
   * @param idCountry an id (iso-3166-1 alpha-2 code)
   */
  public void setIdCountry(String idCountry) {
    this.idCountry = idCountry;
  }



}
