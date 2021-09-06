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
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the t_group_color database table, holding predefined value of color code
 *
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "t_group_color")
@CacheBeanTuning
@Unqueryable
public class TGroupColor extends Model {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TGroupColor> find = new Model.Finder<>(TGroupColor.class);

  @Id
  @Column(name = "id_group_color", unique = true, nullable = false)
  private Integer idColor;

  @Column(name = "color_code", length = 6)
  private String colorCode;

  /**
   * Get this group color id
   *
   * @return an id
   */
  public int getIdColor() {
    return this.idColor;
  }

  /**
   * Set this group color id
   *
   * @param idColor an id
   */
  public void setIdColor(int idColor) {
    this.idColor = idColor;
  }

  /**
   * Get the color code
   *
   * @return a color code
   */
  public String getColorCode() {
    return this.colorCode;
  }

  /**
   * Set the color code
   *
   * @param colorCode a color code
   */
  public void setColorCode(String colorCode) {
    this.colorCode = colorCode;
  }

  /**
   * Find by color code
   *
   * @param code the color code
   * @return a group color
   */
  public static TGroupColor findByCode(String code){
    return find.where().conjunction().eq("color_code", code).findUnique();
  }

  /**
   * Get all color codes
   *
   * @return a list of GroupColors
   */
  public static List<TGroupColor> getAll(){
    return find.orderBy("id_group_color").findList();
  }
}
