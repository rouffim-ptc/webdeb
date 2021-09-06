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

package be.webdeb.core.api.contribution;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface holds contribution's modification statuses.
 * <ul>
 *   <li>0 CREATE for the creation status</li>
 *   <li>1 UPDATE for any update done to a contribution (or any linked object from it)</li>
 *   <li>2 DELETE when a contribution has been deleted</li>
 *   <li>3 MERGE when a contribution has been merged into another one</li>
 *   <li>4 GROUP_REMOVAL when a contribution has been removed from a group</li>
 * </ul>
 *
 * @author Martin Rouffiange
 * @see EModificationStatus
 */
public interface ModificationStatus extends PredefinedIntValue {

    /**
     * Get the corresponding EModificationStatus value to this.
     *
     * @return the EModificationStatus enum value corresponding to this modification status
     */
    EModificationStatus getEType();
}