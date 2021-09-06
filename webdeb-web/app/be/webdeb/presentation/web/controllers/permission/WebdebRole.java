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

import be.objectify.deadbolt.java.models.Role;
import be.webdeb.core.api.contributor.EContributorRole;

/**
 * Mapper class from EContributorRole to deadbolt-compliant roles
 *
 * @author Fabian Gilson
 * @see be.webdeb.core.api.contributor.EContributorRole
 */
public class WebdebRole implements Role {

  // since deadbold needs string values, reflect ContributorRole enum here
  /**
   * The string literal corresponding to the EContributorRole.VIEWER value
   */
  public static final String VIEWER = "viewer";
  /**
   * The string literal corresponding to all but EContributorRole.VIEWER
   */
  public static final String NOTVIEWER = "!viewer";
  /**
   * The string literal corresponding to the EContributorRole.CONTRIBUTOR value
   */
  public static final String CONTRIBUTOR = "contributor";
  /**
   * The string literal corresponding to the EContributorRole.OWNER value
   */
  public static final String OWNER = "owner";
  /**
   * The string literal corresponding to the EContributorRole.ADMIN value
   */
  public static final String ADMIN = "admin";

  private EContributorRole role;

  WebdebRole(EContributorRole role) {
    this.role = role;
  }

  @Override
  public String getName() {
    return role.name().toLowerCase();
  }

  /**
   * Get corresponding EContributorRole value
   *
   * @return the EContributorRole object corresponding to this role
   */
  public EContributorRole getRole() {
    return role;
  }
}
