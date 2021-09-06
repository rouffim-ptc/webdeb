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
 * Generic canvas for exceptions
 *
 * @author Fabian Gilson
 */
public abstract class AbstractException extends Exception {

  private static final long serialVersionUID = 1L;
  private String more;

  /**
   * Default constructor to build a generic exception with a given message
   *
   * @param message a generic message key that may be used to display a i18n message to a user
   */
  AbstractException(String message) {
    super(message);
    more = null;
  }

  /**
   * Constructor. Create an exception with given message and embedded throwable
   *
   * @param message a generic message key that may be used to display a i18n message to a user
   * @param t throwable causing this exception
   */
  AbstractException(String message, Throwable t) {
    super(message, t);
    more = null;
  }

  /**
   * Constructor. Create an exception with more details
   *
   * @param message a generic message key that may be used to display a i18n message to a user
   * @param more some more details
   */
  AbstractException(String message, String more) {
    super(message);
    this.more = more;
  }

  /**
   * Set a textual explanation of the exception raised
   *
   * @param more a textual explanation
   */
  public void setMore(String more) {
    this.more = more;
  }

  /**
   * Get a textual explanation of the exception raised
   *
   * @return a textual explanation, if any (otherwise, return "unknown" value)
   */
  public String getMore() {
    return more != null ? more : "unknown";
  }
}
