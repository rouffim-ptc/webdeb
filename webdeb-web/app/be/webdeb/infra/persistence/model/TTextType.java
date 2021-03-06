/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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
 * The persistent class for the t_text_type database table, holding predefined values for types of text,
 * like journalistic, opinion, discourses, etc.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "t_text_type")
public class TTextType extends TechnicalTable {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TTextType> find = new Model.Finder<>(TTextType.class);

  @Id
  @Column(name = "id_type", unique = true, nullable = false)
  private int idType;

  @OneToMany(mappedBy = "textType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<Text> texts;

  /**
   * Get the text type id
   *
   * @return an id
   */
  public int getIdType() {
    return this.idType;
  }

  /**
   * Set the text type id
   *
   * @param idType an id
   */
  public void setIdType(int idType) {
    this.idType = idType;
  }

  /**
   * Get the list of texts with this type
   *
   * @return a (possibly empty) list of texts
   */
  public List<Text> getTexts() {
    return texts == null ? new ArrayList<>() : texts;
  }

  /**
   * Set the list of texts with this type
   *
   * @param texts a list of texts
   */
  public void setTexts(List<Text> texts) {
    this.texts = texts;
  }
}
