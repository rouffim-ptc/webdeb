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

package be.webdeb.core.api.contribution.link;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.tag.TagCategory;

/**
 * This interface represents the justification links between a contribution and its context contribution. In this context,
 * the contribution can be also linked to a TagCategory and / or a super argument.
 * It also determined in the context by a shade.
 *
 * @author Martin Rouffiange
 */
public interface JustificationLink extends ContributionLink {

    /**
     * Get the context of this link.
     *
     * @return the context contribution of this link.
     */
    ContextContribution getContext();

    /**
     * Set the the context of this link.
     *
     * @param context the context contribution of this link.
     */
    void setContext(ContextContribution context);

    /**
     * Get the tag sub context id of this link.
     *
     * @return the tag sub context id of this link.
     */
    Long getSubContextId();

    /**
     * Set the tag sub context id of this link.
     *
     * @param subDebateId the tag tag sub context id of this link.
     */
    void setSubContextId(Long subDebateId);

    /**
     * Get the tag sub context of this link, can be null
     *
     * @return the tag sub context of the link
     */
    ContextContribution getSubContext();

    /**
     * Set the tag sub context of this link
     *
     * @param subContext the tag sub context of the link
     */
    void setSubContext(ContextContribution subContext);

    /**
     * Get the category id of this link.
     *
     * @return the tag category id of this link.
     */
    Long getTagCategoryId();

    /**
     * Set the category id of this link.
     *
     * @param categoryId the tag category id of this link.
     */
    void setTagCategoryId(Long categoryId);

    /**
     * Get the category of this link, can be null
     *
     * @return the category of the link
     */
    TagCategory getTagCategory();

    /**
     * Set the category of this link
     *
     * @param tagCategory the category of the link
     */
    void setTagCategory(TagCategory tagCategory);

    /**
     * Get the super argument id of this link.
     *
     * @return the super argument id of this link.
     */
    Long getSuperArgumentId();

    /**
     * Set the super argument id of this link.
     *
     * @param contextId the super argument id of this link.
     */
    void setSuperArgumentId(Long contextId);

    /**
     * Get the super argument of this link, can be null
     *
     * @return the super argument of the link
     */
    Argument getSuperArgument();

    /**
     * Set the super argument of this link
     *
     * @param superArgument the super argument of the link
     */
    void setSuperArgument(Argument superArgument);

    /**
     * Get the link shade type
     *
     * @return the link shade type
     */
    JustificationLinkType getLinkType();

    /**
     * Set the link shade type
     *
     * @param type the link shade type
     */
    void setLinkType(JustificationLinkType type);

}
