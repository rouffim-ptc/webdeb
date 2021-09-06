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

package be.webdeb.presentation.web.controllers.permission;

import be.objectify.deadbolt.java.models.Permission;
import be.webdeb.core.api.contributor.EPermission;

/**
 * Mapper class from EPermission to deadbolt-compliant permissions.
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EPermission
 */
public class WebdebPermission implements Permission {

  private EPermission permission;

  WebdebPermission(EPermission permission) {
    this.permission = permission;
  }

  @Override
  public String getValue() {
    return permission.name();
  }

  /**
   * Get the EPermission value for this permission scheme
   *
   * @return the EPermission value
   */
  public EPermission getPermission() {
    return permission;
  }

  @Override
  public String toString() {
    return getPermission().name();
  }
}
