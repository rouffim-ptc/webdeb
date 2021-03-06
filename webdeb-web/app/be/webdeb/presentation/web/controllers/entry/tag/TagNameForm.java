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

package be.webdeb.presentation.web.controllers.entry.tag;

/**
 * Simple form for tag name
 *
 * @author Martin Rouffiange
 */
public class TagNameForm {

  private String lang;
  private String name;

  /**
   * Play / JSON compliant constructor
   */
  public TagNameForm() {
    // needed by play
  }

  /**
   * Creates a simple form for tag name
   *
   * @param lang the language of the name
   * @param name the tag name
   */
  public TagNameForm(String lang, String name) {
    this.lang = lang;
    this.name = name;
  }

  /**
   * Helper method to check whether the name and lang are empty (contains empty or null values)
   *
   * @return true if all fields are empty
   */
  public boolean isEmpty() {
    return (name == null || "".equals(name)
        && (lang == null || "".equals(lang)));
  }

  /**
   * Helper method to check whether the name or lang are not empty (contains empty or null values)
   *
   * @return true if one field is empty
   */
  public boolean isValid() {
    return (name != null && !"".equals(name)
        || (lang != null && !"".equals(lang)));
  }

  /*
   * GETTERS / SETTERS
   */

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "TagNameForm{" +
            "lang='" + lang + '\'' +
            ", name='" + name + '\'' +
            '}';
  }
}
