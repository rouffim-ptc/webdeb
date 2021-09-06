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

import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.tag.TagCategory;

/**
 * This interface represents the links between a context contribution and a tag category.
 *
 * @author Martin Rouffiange
 */
public interface ContextHasCategory extends ContributionLink {

    /**
     * Get the context contribution of the link
     *
     * @return the Context Contribution object
     */
    ContextContribution getContextContribution();

    /**
     * Set the the context of this link.
     *
     * @param context the context contribution of this link.
     */
    void setContext(ContextContribution context);

    /**
     * Get the tag category linked to this link
     *
     * @return the Tag Category object
     */
    TagCategory getTagCategory();

}
