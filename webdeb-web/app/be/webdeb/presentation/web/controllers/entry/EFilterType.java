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

/**
 * Keys for graphical elements used to filter contributions.
 *
 * Used to dynamically build filters in views.
 *
 * @author Fabian Gilson
 * @see be.webdeb.presentation.web.views.util.filterbar template
 */
public enum EFilterType {
  /**
   * A range input, allowing to select range values (from - to)
   */
  RANGE("range"),
  /**
   * A checkbox input
   */
  BOX("box"),
  /**
   * A tree of checkbox input
   */
  TREE("tree");

  private String name;

  EFilterType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
