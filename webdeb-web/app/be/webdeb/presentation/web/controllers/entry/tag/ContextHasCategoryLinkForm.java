/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.entry.tag;

import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.link.BaseLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;

public class ContextHasCategoryLinkForm extends BaseLinkForm {

    @Inject
    protected TagFactory tagFactory = Play.current().injector().instanceOf(TagFactory.class);

    private TagCategoryHolder category;

    /**
     * Play / JSON compliant constructor
     */
    public ContextHasCategoryLinkForm() {
        super();
        type = EContributionType.CONTEXT_HAS_CATEGORY;
    }

    /**
     * Construct a link wrapper with a given ContextHasCategory link (full initialization)
     *
     * @param link an existing ContextHasCategory
     * @param context the context of the link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public ContextHasCategoryLinkForm(ContextHasCategory link, ContextContribution context, WebdebUser user, String lang) {
        super(link, user, lang);

        destinationTitle = link.getTagCategory().getName(lang);
        category = new TagCategoryHolder(link.getTagCategory(), context, this, user, lang, true);
    }

    /**
     * Transform this form into an API ContextHasCategory
     *
     * @return an API ContextHasCategory corresponding to this context has category link form
     */
    @Override
    public ContextHasCategory toLink() throws PersistenceException {
        ContextHasCategory link = tagFactory.getContextHasCategory();
        updateLink(link);
        return link;
    }

    public TagCategoryHolder getCategory() {
        return category;
    }

}
