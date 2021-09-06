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

import play.api.Play;
import play.data.validation.ValidationError;

import java.util.*;
import be.webdeb.util.ValuesHelper;

import javax.inject.Inject;

/**
 * Simple form class that contains a mail to send at the members of a group
 *
 * @author Martin Rouffiange
 */
public class GroupMailForm {

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    private int groupId;
    private String title;
    private String content;

    /**
     * Play / Json compliant constructor
     */
    public GroupMailForm() {
        // needed by json/play
    }

    /**
     * Initialize a invitation form for given group
     *
     * @param groupId the id of the group for which a mail will be sent
     */
    public GroupMailForm(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Play form validation method
     *
     * @return a list of errors or null if none
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if(values.isBlank(title)){
            errors.put("title", Collections.singletonList(
                    new ValidationError("title", "group.mail.noTitle")));
        }

        if(values.isBlank(content)){
            errors.put("content", Collections.singletonList(
                    new ValidationError("content", "group.mail.noContent")));
        }

        return errors.isEmpty() ? null : errors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

}
