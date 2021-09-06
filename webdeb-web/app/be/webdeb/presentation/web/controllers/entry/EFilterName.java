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

package be.webdeb.presentation.web.controllers.entry;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

/**
 * Contribution filtering categories. They are used to dynamically build filters in views.
 *
 * Specify all available categories and those keys map to messages.xx suffixes.
 * Those messages keys are specified under general.filter.{label, title, icon}.KEY
 *
 * Using lowercase to avoid to implicitly use those values in messages.xx files and views
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.presentation.web.views.util.filterbar template
 */
public enum EFilterName {
  // order does matter since they are added into a linked hashmap to be displayed in a precise order
  /**
   * Contribution type
   */
  CTYPE("ctype"),
  /**
   * Tag contribution
   */
  TAG("tag"),
  /**
   * country or territory
   */
  COUNTRY("country"),
  /**
   * Contribution place
   */
  PLACE("place"),
  /**
   * actor type
   */
  ATYPE("atype"),
  /**
   * affiliation type
   */
  AFFTYPE("afftype"),
  /**
   * Geneder for persons (individual actors)
   */
  GENDER("gender"),
  /**
   * Affiliation functions (actors and authors)
   */
  FUNCTION("function"),
  /**
   * Legal status for organization (actors)
   */
  LEGAL("legal"),
  /**
   * Business sectors for organization (actors)
   */
  SECTOR("sector"),
  /**
   * Affiliation names (actors and authors)
   */
  AFFILIATION("affiliation"),
  /**
   * Text and argument language
   */
  LANGUAGE("language"),
  /**
   * Text type
   */
  TTYPE("ttype"),
  /**
   * Debate type
   */
  DTYPE("dtype"),
  /**
   * Debate shade
   */
  DSHADE("dshade"),
  /**
   * Text origin
   */
  ORIGIN("origin"),
  /**
   * Text source (transitively applies to arguments)
   */
  SOURCE("source"),
  /**
   * Argument shade
   */
  ARGSHADE("argshade"),
  /**
   * Date of birth and death for persons (individual actors)
   */
  BIRTHDATE("birthdate"),
  /**
   * Publication date of texts (transitively applies to arguments)
   */
  PUBLIDATE("publidate"),
  /**
   * Actor name
   */
  NAME("name"),
  /**
   * Tag type
   */
  FTYPE("ftype"),
  /**
   * The name of the filter
   */
  FILTERID("filtername");

  private String name;
  private static Map<String, EFilterName> map = new LinkedHashMap<>();

  static {
    for (EFilterName type : EFilterName.values()) {
      map.put(type.name, type);
    }
  }

  EFilterName(String name) {
    this.name = name;
  }

  /**
   * Get the enum value for a given id
   *
   * @param name a String representing an EFilterName
   * @return the EFilterName enum value corresponding to the given id, null otherwise.
   */
  public static EFilterName value(String name) {
    return map.get(name);
  }

  /**
   * Get this id
   *
   * @return a String representation of this filter name
   */
  public String id() {
    return name;
  }

  public static List<EFilterName> all(){
    List<EFilterName> l = new ArrayList<>(Arrays.asList(values()));
    l.remove(l.size()-1);
    return l;
  }

  @JsonValue
  @Override
  public String toString() {
    return name;
  }
}
