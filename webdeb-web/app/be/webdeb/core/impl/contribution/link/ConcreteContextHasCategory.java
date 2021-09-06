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

package be.webdeb.core.impl.contribution.link;

import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import java.util.*;

/**
 * This class implements a context has category link.
 *
 * @author Martin Rouffiange
 */
public class ConcreteContextHasCategory extends AbstractContributionLink<TagFactory, TagAccessor> implements ContextHasCategory {

    private ContextContribution context = null;
    private TagCategory category = null;

    /**
     * Create a ContextHasCategory instance
     *
     * @param factory the tag factory
     * @param accessor the tag accessor
     * @param contributorFactory the contributor accessor
     */
    public ConcreteContextHasCategory(TagFactory factory, TagAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        type = EContributionType.CONTEXT_HAS_CATEGORY;
    }

    @Override
    public ContextContribution getContextContribution() {
        if(context == null){
            context = accessor.retrieveContextContribution(originId);
        }
        return context;
    }

    @Override
    public void setContext(ContextContribution context) {
        this.context = context;
    }

    @Override
    public TagCategory getTagCategory() {
        if(category == null){
            category = accessor.retrieveTagCategory(destinationId, false);
            category.setCurrentContextId(context.getId());
        }
        return category;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("context has category link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }
}
