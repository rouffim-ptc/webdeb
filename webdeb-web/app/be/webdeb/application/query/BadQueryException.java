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
 *
 */

package be.webdeb.application.query;

/**
 * This exception is used to warn a (API) user that a given search query is invalid
 *
 * @author Fabian Gilson
 */
public class BadQueryException extends Exception {

  private final String query;
  private final String errorKey;
  private final String errorValue;

  private static final long serialVersionUID = 1L;

  /**
   * Default constructor
   *
   * @param query the query passed that caused the error
   * @param errorKey the key in error
   */
  public BadQueryException(String query, String errorKey) {
    super("Bad search query, given key " + errorKey + " does not exist");
    this.errorKey = errorKey;
    this.query = query;
    this.errorValue = null;
  }

  /**
   * Default constructor
   *
   * @param query the query passed that caused the error
   * @param errorKey the key related to the value in error
   * @param errorValue the unknown value for given key
   */
  public BadQueryException(String query, String errorKey, String errorValue) {
    super("Bad search query, given key " + errorKey + " contains invalid value " + errorValue);
    this.errorKey = errorKey;
    this.query = query;
    this.errorValue = errorValue;
  }

  /**
   * Get the query string that caused the error
   *
   * @return the query that caused the error
   */
  public String getQuery() {
    return query;
  }

  /**
   * Get the query key that caused the error
   *
   * @return a key
   */
  public String getErrorKey() {
    return errorKey;
  }

  /**
   * Get the value for this.key that caused the error
   *
   * @return a value
   */
  public String getErrorValue() {
    return errorValue;
  }
}
