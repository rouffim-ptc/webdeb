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

package be.webdeb.presentation.web.controllers.account.group;

/**
 * Simple form class that contains a followed state for a group and a contributor
 *
 * @author Martin Rouffiange
 */
public class GroupFollowedForm {

    private Integer groupId = -1;
    private boolean followed;

    /**
     * Play / Json compliant constructor
     */
    public GroupFollowedForm() {
        // needed by json/play
    }

    /**
     * Initialize a group followed form for a given group and a given contributor
     *
     * @param group the group id
     * @param followed the following state
     */
    public GroupFollowedForm(Integer group, boolean followed) {
        this.groupId = group;
        this.followed = followed;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }
}
