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

package be.webdeb.core.exception;

/**
 * This exception is used to warn that an element that should have been found in the repository was not
 * found, i.e., an object with an id considered as valid was not found.
 *
 * One may retrieve the intended class name and id.
 *
 * @author Fabian Gilson
 */
public class ObjectNotFoundException extends PersistenceException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor to warn user about a object not found
   *
   * @param clazz the class of the object that wasn't found in the repository
   * @param id the id of the object as passed to the repository
   */
  public <T> ObjectNotFoundException(Class<T> clazz, Long id) {
    super(Key.NOT_FOUND, "object of type " + (clazz != null ? clazz.getSimpleName() : "UNKNOWN")
        + " with id " + id + " does not exist");
  }
}
