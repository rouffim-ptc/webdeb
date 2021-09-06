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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The persistent class for the text_source_name database table, holding dynamically collected sources
 * of texts.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "text_source_name")
public class TextSourceName extends Model {

  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Finder<Integer, TextSourceName> find = new Finder<>(TextSourceName.class);

  @Id
  @Column(name = "id_source", unique = true, nullable = false)
  private int idSource;

  @Column(name = "name", length = 255)
  private String name;

  @OneToMany(mappedBy = "sourceName", fetch = FetchType.LAZY)
  private List<Text> texts;

  /**
   * Constructor, creates a new TextSourceName object with a given name
   *
   * @param name the source name
   */
  public TextSourceName(String name) {
    // auto increment
    idSource = 0;
    this.name = name;
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get this source id
   *
   * @return an id
   */
  public int getIdSource() {
    return idSource;
  }

  /**
   * Set this source id
   *
   * @param idSource an id
   */
  public void setIdSource(int idSource) {
    this.idSource = idSource;
  }

  /**
   * Get this source name
   *
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Set this source name
   *
   * @param name a name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the list of texts where this source is the source name
   *
   * @return a list of texts
   */
  public List<Text> getTexts() {
    return texts;
  }

  /**
   * Set the list of texts where this source is the source name
   *
   * @param texts a list of texts
   */
  public void setTexts(List<Text> texts) {
    this.texts = texts;
  }

  @Override
  public String toString() {
    return "source: [" + getIdSource() + "] " + getName();
  }

  /*
   * QUERIES
   */

  /**
   * Find a text source by its id
   *
   * @param id a source id
   * @return found text source for given id, or null if not found
   */
  public static TextSourceName findById(int id) {
    return find.byId(id);
  }

  /**
   * Get a source by its name
   *
   * @param name the source name to look for
   * @return the TextSourceName where lang=name, or null if not found
   */
  public static TextSourceName findByName(String name) {
    return find.where().eq("name", name).findUnique();
  }

  /**
   * Find a list of source names by a given term
   *
   * @param term the partial name to look for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly) empty list of all text source name in the given language containing the given term
   */
  public static List<TextSourceName> findTextSource(String term, int fromIndex, int toIndex) {
    return findTextSource(Collections.singletonList(term), fromIndex, toIndex);
  }

  /**
   * Find a list of professions by a given terms
   *
   * @param term the partial names to look for
   * @return a (possibly) empty list of all text source name in the given language containing the given term
   */
  public static List<TextSourceName> findTextSource(List<String> term) {
    return findTextSource(term, 0, 0);
  }

  /**
   * Find a list of professions by a given terms
   *
   * @param term the partial names to look for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly) empty list of all text source name in the given language containing the given term
   */
  public static List<TextSourceName> findTextSource(List<String> term, int fromIndex, int toIndex) {
    logger.debug("search for " + term);
    int lower = fromIndex > 0 ? fromIndex : 0;
    int upper = toIndex > 0 && toIndex > fromIndex ? toIndex - fromIndex : Integer.MAX_VALUE;

    List<TextSourceName> result =
            find.where().contains("name", String.join(" ", term)).setFirstRow(lower).setMaxRows(upper).findList();
    return result != null ? result : new ArrayList<>();
  }
}
