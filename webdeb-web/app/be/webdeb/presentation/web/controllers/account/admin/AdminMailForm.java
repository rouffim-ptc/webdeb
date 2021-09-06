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

package be.webdeb.presentation.web.controllers.account.admin;

import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.util.ValuesHelper;
import play.data.validation.ValidationError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple form object to send a email to contributors
 *
 * @author Martin Rouffiange
 */
public class AdminMailForm {

    private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

    private static final org.slf4j.Logger logger = play.Logger.underlying();

    private String usergroup;
    private EContributorRole userrole;
    private String title;
    private String newsletter;
    private String style;
    private String content;

    /**
     * Default play-compliant empty constructor
     */
    public AdminMailForm() {
        // needed by play / jackson
        usergroup = EContributorRole.ALL.id() + "";
        newsletter = "true";
    }

    /**
     * Validate the creation of an RSS Feed (implicit call from form submission)
     *
     * @return null if validation ok, map of errors for each fields in error otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        userrole = EContributorRole.value(Integer.parseInt(usergroup));

        if (values.isBlank(usergroup) || userrole == null) {
            errors.put("usergroup", Collections.singletonList(new ValidationError("usergroup", "admin.mail.usergroup.error")));
        }

        if (values.isBlank(title)) {
            errors.put("title", Collections.singletonList(new ValidationError("title", "admin.mail.title.error")));
        }

        if (values.isBlank(content)) {
            errors.put("content", Collections.singletonList(new ValidationError("content", "admin.mail.newsletter.error")));
        }

        return errors.isEmpty() ? null : errors;
    }

    /*
     * GETTER / SETTERS
     */

    public String getUsergroup() {
        return usergroup;
    }

    public void setUsergroup(String usergroup) {
        this.usergroup = usergroup;
    }

    public EContributorRole getUserrole() {
        return userrole;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getIsNewsletter() {
        return values.isBlank(newsletter) || newsletter.equals("true");
    }

    public String getNewsletter() {
        return newsletter;
    }

    public void setNewsletter(String newsletter) {
        this.newsletter = newsletter;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
