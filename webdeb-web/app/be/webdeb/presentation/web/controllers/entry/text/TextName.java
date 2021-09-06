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

package be.webdeb.presentation.web.controllers.entry.text;

import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified text name, used for auto completion
 *
 * @author Fabian Gilson
 */
public class TextName {

  private Long id = -1L;
  private String title;
  private List<ActorSimpleForm> authors = new ArrayList<>();

  /**
   * Play / JSON compliant constructor
   */
  public TextName() {
    // needed by play
  }

  /**
   * Creates a simplified text name with only the given title and list of authors
   *
   * @param id the text contribution_id
   * @param title the title of this text
   * @param authors a list of authors for this text
   */
  public TextName(Long id, String title, List<ActorSimpleForm> authors) {
    this.id = id;
    this.title = title;
    this.authors = authors;
  }

  /*
   * GETTERS / SETTERS
   */

    /**
   * Get the text id
   *
   * @return a text id
   */
  public Long getId() {
    return id;
  }

    /**
   * Set the text id
   *
   * @param id a text id
   */
  public void setId(Long id) {
    this.id = id;
  }

    /**
   * Get the text title
   *
   * @return a title
   */
  public String getTitle() {
    return title;
  }

    /**
   * Set the text title
   *
   * @param title a title
   */
  public void setTitle(String title) {
    this.title = title;
  }

    /**
   * Get the list of authors of this text
   *
   * @return a list of actors
   */
  public List<ActorSimpleForm> getAuthors() {
    return authors;
  }

    /**
   * Set the list of authors for this text
   *
   * @param authors a list of actors
   */
  public void setAuthors(List<ActorSimpleForm> authors) {
    this.authors = authors;
  }

  @Override
  public boolean equals(Object o) {

    if (!(o instanceof TextName)) {
      return false;
    }
    TextName textname = (TextName) o;
    if (!textname.title.equals(title)) {
      return false;
    }
    for (ActorSimpleForm a : textname.authors) {
      if (!authors.contains(a)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 93;
    if (getAuthors() != null) {
      hash = getAuthors().stream().mapToInt(ActorSimpleForm::hashCode).sum();
    }
    return title.hashCode() + hash;
  }
}
