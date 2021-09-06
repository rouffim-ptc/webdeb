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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for visibility types for texts. It defines who may view a text.
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.text.ETextVisibility
 */
@Entity
@CacheBeanTuning
@Table(name = "t_text_visibility")
@Unqueryable
public class TTextVisibility extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TTextVisibility> find = new Model.Finder<>(TTextVisibility.class);

  @Id
  @Column(name = "id_visibility")
  private Integer idVisibility;

  @OneToMany(mappedBy = "visibility", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Text> texts;

  /**
   * Get the visibility id
   *
   * @return the visibility id
   * @see be.webdeb.core.api.text.ETextVisibility
   */
  public int getIdVisibility() {
    return idVisibility;
  }

  /**
   * Set the visibility id
   *
   * @param idVisibility a visibility id
   */
  public void setIdVisibility(int idVisibility) {
    this.idVisibility = idVisibility;
  }

  /**
   * Get the list of texts with this visibility
   *
   * @return a (possibly empty) list of texts
   */
  public List<Text> getTexts() {
    return texts == null ? new ArrayList<>() : texts;
  }

  /**
   * Set the list of texts with this visibility
   *
   * @param texts a list of texts
   */
  public void setTexts(List<Text> texts) {
    this.texts = texts;
  }
}
