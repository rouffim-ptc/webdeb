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

package be.webdeb.presentation.web.controllers.viz;

import be.webdeb.core.api.contribution.EContributionType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This enumeration represents the view key for radiography (point of view, or grouping), ie, it is used
 * for arguments "excerpts" view where all similar (or opposed) arguments are shown reagrding a selected argument,
 * and for texts "linked texts" view where all texts having similar or opposed arguments with any argument extracted
 * from a given text are shown.
 *
 * <ul>
 *   <li>0 for author</li>
 *   <li>1 for source</li>
 *   <li>2 for date</li>
 *   <li>3 for text</li>
 * </ul>
 *
 * @author Fabian Gilson
 */
public enum ERadiographyViewKey {

  /**
   * view by author
   */
  AUTHOR(0),

  /**
   * view by owning text's source name
   */
  SOURCE(1),

  /**
   * view by owning text's publication date
   */
  DATE(2),
  /**
   * view by text
   */
  TEXT(3);

  private int id;
  private static Map<Integer, ERadiographyViewKey> map = new LinkedHashMap<>();

  static {
    for (ERadiographyViewKey type : ERadiographyViewKey.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a view key
   */
  ERadiographyViewKey(int id) {
    this.id = id;
  }

  /**
   * Get all available keys for given contribution type
   *
   * @param type a contribution type
   * @return a collection of ERadiographyViewKey application for given type
   */
  public static Collection<ERadiographyViewKey> getAll(EContributionType type) {
    switch (type) {
      case ARGUMENT:
        return map.values().stream().filter(v -> !ERadiographyViewKey.TEXT.equals(v)).collect(Collectors.toList());
      case TEXT:
        return map.values().stream().collect(Collectors.toList());
      default:
        return Collections.emptyList();
    }
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing a view key
   * @return the ERadiographyViewKey enum value corresponding to the given id, null otherwise.
   */
  public static ERadiographyViewKey value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this view key
   */
  public int id() {
    return id;
  }
}
