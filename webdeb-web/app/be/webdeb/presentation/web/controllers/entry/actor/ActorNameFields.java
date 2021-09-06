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

package be.webdeb.presentation.web.controllers.entry.actor;

import be.webdeb.core.api.actor.ActorName;

/**
 * Simple class to hold all fields of an actor name
 *
 * @author Fabian Gilson
 */
public class ActorNameFields {

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private String last;
  private String first;
  private String pseudo;
  private String lang;

  /**
   * Empty default constructor
   */
  public ActorNameFields() {
    // needed by Play / JSON
  }

  /**
   * Constructor. Create an actor fields from a given simple actor form
   *
   * @param simpleActor a simple actor form
   */
  public ActorNameFields(ActorSimpleForm simpleActor, String lang) {
    this.last = simpleActor.getFullname();
    this.lang = lang;
  }

  /**
   * Constructor. Create an actor fields from a given actor name
   *
   * @param name a name
   */
  public ActorNameFields(ActorName name) {
    this.first = name.getFirst();
    this.last = name.getLast();
    this.pseudo = name.getPseudo();
    this.lang = name.getLang();
  }

  /**
   * Get the person's last name or organization name
   *
   * @return a name
   */
  public String getLast() {
    return last;
  }

  /**
   * Set the person's last name or organization name
   *
   * @param last a name
   */
  public void setLast(String last) {
    this.last = last;
  }

  /**
   * Get the person's first name or organization acronym
   *
   * @return a name or acronym
   */
  public String getFirst() {
    return first;
  }

  /**
   * Set the person's first name or organization acronym
   *
   * @param first a name or acronym
   */
  public void setFirst(String first) {
    this.first = first;
  }

  /**
   * Get the person's pseudonym
   *
   * @return a person's pseudonym
   */
  public String getPseudo() {
    return pseudo;
  }

  /**
   * Set the person's pseudonym
   *
   * @param pseudo a person's pseudonym
   */
  public void setPseudo(String pseudo) {
    this.pseudo = pseudo;
  }

  /**
   * Get the 2-char ISO code attached to this name
   *
   * @return a language code
   */
  public String getLang() {
    return lang;
  }

  /**
   * Set the 2-char ISO code attached to this name
   *
   * @param lang a language code
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Helper method to check whether this name is empty (contains empty or null values)
   *
   * @return true if all fields are empty
   */
  public boolean isEmpty() {
    return (first == null || "".equals(first))
        && (last == null || "".equals(last))
        && (pseudo == null || "".equals(pseudo))
        && (lang == null || "".equals(lang));
  }

  @Override
  public String toString() {
    return (first != null ? first + " " : "") + last
        + (pseudo != null ? " (" + pseudo + ") " : " ")
        + "[in " + lang + "]";
  }
}
