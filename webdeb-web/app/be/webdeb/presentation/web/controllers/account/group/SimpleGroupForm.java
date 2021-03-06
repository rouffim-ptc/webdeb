/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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

import be.webdeb.core.api.contributor.Group;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;

/**
 * Simple form for group
 *
 * @author Martin Rouffiange
 */
public class SimpleGroupForm {

    private Integer groupId = -1;
    private String name;

    @Inject
    private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Play / JSON compliant constructor
     */
    public SimpleGroupForm () {
        // needed by play
    }

    /**
     * Creates a simple group form from a API group and a lang
     *
     * @param group a group
     */
    public SimpleGroupForm(Group group) {
        this.groupId = group.getGroupId();
        this.name = group.getGroupName();
    }

    /**
     * Helper method to check whether the name and id are empty (contains empty or null values)
     *
     * @return true if all fields are empty
     */
    public boolean isEmpty() {
        return groupId == null || groupId == -1 || values.isBlank(name);
    }

    @Override
    public String toString() {
        return "SimpleGroupForm{" +
                "groupId=" + groupId +
                ", name='" + name + '\'' +
                '}';
    }

    /*
     * GETTERS / SETTERS
     */

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
