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
 * This exception is used to warn a user that a requested operation is not permitted regarding user privileges.
 *
 * Message keys used as parameters should be added in i18n dedicated messages.xx files, in order to be correctly
 * processed by the XXActions classes (Controllers layer).
 *
 * The mapping between key names and messages.xx property names is of the form "permission." + Key.name.replace("_", ".")
 *
 * @author Fabian Gilson
 */
public class PermissionException extends AbstractException {

  private static final long serialVersionUID = 1L;

  /**
   * The enumerated values to give details regarding permission exceptions
   */
  public enum Key {
    /**
     * Message key used to warn user an affiliation may not be deleted
     */
    AFFILIATION_DELETE_NOTPERMITTED,
    /**
     * Message key used to warn user he's not member of group
     */
    NOT_GROUP_MEMBER,
    /**
     * Message key used to warn user he's not owner of group
     */
    NOT_GROUP_OWNER,
    /**
     * Message key used to warn user requested group is not public
     */
    NOT_PUBLIC_GROUP,
    /**
     * Message key used to warn user the content of a text is not visible to him, ie, text content is private
     * and current user has no copy of it, or text is pedagogic and user is not from the academic domain
     */
    TEXT_NOT_VISIBLE,
    /**
     * Message key used to warn user the content of a text was not private, so it may not be put back to private
     */
    TEXT_NOT_PRIVATE,
    /**
     * Message key to warn users about an error while saving a contribution into the wrong scope, eg,
     * the contribution does not belong to a given scope, or user has no write access in that group
     */
    ERROR_SCOPE
  }

  /**
   * Default constructor
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   */
  public PermissionException(Key key) {
    super("permission." + key.name().toLowerCase().replace("_", "."));
  }

  /**
   * Default constructor
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   * @param reason a more detailed reason attached to this exception
   */
  public PermissionException(Key key, String reason) {
    this(key);
    setMore(reason);
  }
}
