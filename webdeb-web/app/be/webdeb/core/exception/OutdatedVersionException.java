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
 * This exception is used to warn users that he tried to update an object that has been modified since
 * he retrieved it from the repository. User may retrieve the last version of the object by calling getActual
 * and retrieve their submitted object by calling getOld
 *
 * @author Fabian Gilson
 */
public class OutdatedVersionException extends PersistenceException {

  private static final long serialVersionUID = 1L;
  private Object actual;
  private Object old;

  /**
   * Default constructor.
   *
   * @param old the submitted object
   * @param actual the actual object (last version of it as present in the database)
   */
  public OutdatedVersionException(Object old, Object actual) {
    super(Key.OUTDATED_VERSION);
    this.actual = actual;
    this.old = old;
  }

  /**
   * Get the actual object, ie, last version of it as present in the database
   *
   * @return last version of the object
   */
  public Object getActual() {
    return actual;
  }

  /**
   * Get the old (submitted) object
   *
   * @return an outdated object
   */
  public Object getOld() {
    return old;
  }
}
