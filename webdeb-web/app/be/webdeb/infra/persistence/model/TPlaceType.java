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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_place_type database table, holding predefined values for types of place,
 * like place, region, country
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_place_type")
public class TPlaceType extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TPlaceType> find = new Model.Finder<>(TPlaceType.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idType;

  @OneToMany(mappedBy = "placeType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Place> places;

  /**
   * Get the place type id
   *
   * @return an id
   */
  public int getIdType() {
    return this.idType;
  }

  /**
   * Set the place type id
   *
   * @param idType an id
   */
  public void setIdType(int idType) {
    this.idType = idType;
  }

  /**
   * Get the list of places with this type
   *
   * @return a (possibly empty) list of places
   */
  public List<Place> getPlaces() {
    return places == null ? new ArrayList<>() : places;
  }

  /**
   * Set the list of places with this type
   *
   * @param places a list of places
   */
  public void setPlaces(List<Place> places) {
    this.places = places;
  }
}
