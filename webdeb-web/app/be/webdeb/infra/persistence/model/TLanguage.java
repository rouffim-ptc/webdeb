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
 * The persistent class for the t_language database table. Those languages are not the supported
 * languages by the system, but languages for Texts
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "t_language")
public class TLanguage extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<String, TLanguage> find = new Model.Finder<>(TLanguage.class);

  @Id
  @Column(name = "code", nullable = false, length = 2)
  private String code;

  @Column(name = "own", nullable = false, length = 45)
  private String own;

  /**
   * Get the language ISO code
   *
   * @return an ISO code
   */
  public String getCode() {
    return code;
  }

  /**
   * Set the language ISO code
   *
   * @param code an ISO code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Get the description in this language
   *
   * @return a description in this own language (and writing)
   */
  public String getOwn() {
    return own;
  }

  /**
   * Set the description in this language
   *
   * @param own a description in this own language (and writing)
   */
  public void setOwn(String own) {
    this.own = own;
  }
}
