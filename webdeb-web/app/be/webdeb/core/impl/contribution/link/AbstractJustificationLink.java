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

package be.webdeb.core.impl.contribution.link;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.JustificationLink;
import be.webdeb.core.api.contribution.link.JustificationLinkType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import java.util.List;

/**
 * This class implements a JustificationLink
 *
 * @author Martin Rouffiange
 */
public abstract class AbstractJustificationLink<T extends ContributionFactory, V extends ContributionAccessor> extends AbstractContributionLink<T, V> implements JustificationLink {

    protected ContextContribution contextContribution = null;
    protected Long subContextId;
    protected ContextContribution subContextContribution = null;
    protected Long tagId;
    protected TagCategory tagCategory = null;
    protected Long superArgumentId;
    protected Argument superArgument = null;
    protected JustificationLinkType linkType;

    protected TagAccessor tagAccessor;

    /**
     * Constructor
     *
     * @param factory       a ContributionFactory the factory to construct concrete instances
     * @param accessor      a ContributionAccessor the accessor to retrieve and persist concrete Contribution
     */
    public AbstractJustificationLink(T factory, V accessor, TagAccessor tagAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        this.tagAccessor = tagAccessor;
    }

    @Override
    public ContextContribution getContext() {
        if(contextContribution == null){
            contextContribution = accessor.retrieveContextContribution(originId);
        }
        return contextContribution;
    }

    @Override
    public void setContext(ContextContribution context) {
        this.contextContribution = context;
    }

    @Override
    public Long getSubContextId() {
        return subContextId;
    }

    @Override
    public void setSubContextId(Long subContextId) {
        this.subContextId = subContextId;
    }

    @Override
    public ContextContribution getSubContext() {
        return subContextContribution;
    }

    @Override
    public void setSubContext(ContextContribution subContextContribution) {
        this.subContextContribution = subContextContribution;
    }

    @Override
    public Long getTagCategoryId() {
        return tagId;
    }

    @Override
    public void setTagCategoryId(Long tagId) {
        this.tagId = tagId;
    }

    @Override
    public TagCategory getTagCategory() {
        if(tagCategory == null){
            Contribution c = tagAccessor.retrieve(tagId, false);
            tagCategory = c != null ? (TagCategory) c : null;
        }
        return tagCategory;
    }

    @Override
    public void setTagCategory(TagCategory tagCategory) {
        this.tagId = tagCategory != null ? tagCategory.getId() : this.tagId;
        this.tagCategory = tagCategory;
    }

    @Override
    public Long getSuperArgumentId() {
        return superArgumentId;
    }

    @Override
    public void setSuperArgumentId(Long superArgumentId) {
        this.superArgumentId = superArgumentId;
    }

    @Override
    public Argument getSuperArgument() {
        if(superArgument == null){
            Contribution c = accessor.retrieve(superArgumentId, EContributionType.TAG);
            superArgument = c != null ? (Argument) c : null;
            superArgumentId = superArgument != null ? superArgument.getId() : null;
        }
        return superArgument;
    }

    @Override
    public void setSuperArgument(Argument superArgument) {
        this.superArgumentId = superArgument != null ? superArgument.getId() : this.superArgumentId;
        this.superArgument = superArgument;
    }

    @Override
    public JustificationLinkType getLinkType() {
        return linkType;
    }

    @Override
    public void setLinkType(JustificationLinkType linkType) {
        this.linkType = linkType;
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getContext() == null) {
            fieldsInError.add("context contribution is null");
        }
        if(linkType == null || !linkType.isValid()) {
            fieldsInError.add("link type is null or not valid");
        }
        return fieldsInError;
    }

    @Override
    public String toString() {
        return "justification " + super.toString() + " with type " + linkType.getType() + " in sub context " + subContextId +
                " in category : " + tagId + " and super argument : " + superArgumentId;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (subContextId != null ? subContextId.hashCode() : 0) + (tagId != null ? tagId.hashCode() : 0) +
                (superArgumentId != null ? superArgumentId.hashCode() : 0) + linkType.getType().hashCode();
    }
}