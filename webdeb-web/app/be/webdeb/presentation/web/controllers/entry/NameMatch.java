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

import java.util.List;

/**
 * This class send the index of the match and the list of contribution of type E name-matched
 *
 * @author Martin Rouffiange
 */
public interface NameMatch<E> {

  /**
   * Get the selector string to desambiguate the good form
   *
   * @return the selector string
   */
  String getSelector();

  /**
   * Get the index of the match
   *
   * @return the index of the match
   */
  int getIndex();

  /**
   * Get the list of matched names
   *
   * @return a list of matched names
   */
  List<E> getNameMatches();

  /**
   * Check if the index is equal to -1 or if the nameMatches list is empty
   *
   * @return true if one of them is empty
   */
  boolean isEmpty();

  /**
   * Check if we must disambiguate for an actor name, false if it concerns an affiliation name
   *
   * @return rue if we must disambiguate for an actor name, false if it concerns an affiliation name
   */
  boolean isActor();
}
