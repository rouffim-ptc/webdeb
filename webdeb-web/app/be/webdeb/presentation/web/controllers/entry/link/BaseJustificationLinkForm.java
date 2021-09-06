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

package be.webdeb.presentation.web.controllers.entry.link;

import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.contribution.link.JustificationLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;


/**
 * This class holds supertype wrapper for every type of justification links.
 *
 * @author Martin Rouffiange
 */
public abstract class BaseJustificationLinkForm extends BaseLinkForm {

    @Inject
    protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

    @Inject
    protected CitationFactory citationFactory = Play.current().injector().instanceOf(CitationFactory.class);

    protected Long subContextId;
    protected Long categoryId;
    protected String categoryTitle;
    protected Long superArgumentId;
    protected String superArgumentTitle;

    protected EJustificationLinkShade shade;
    protected EContributionType contextType;


    /**
     * Play / JSON compliant constructor
     */
    public BaseJustificationLinkForm() {
        super();
    }

    /**
     * Constructor from a given contribution
     *
     * @param link a justification link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public BaseJustificationLinkForm(JustificationLink link, WebdebUser user, String lang) {
        super(link, user, lang);

        originTitle = link.getContext().getContributionTitle(lang);
        contextType = link.getContext().getType();

        subContextId = link.getSubContextId();

        categoryId = link.getTagCategoryId();
        categoryTitle = link.getTagCategory() != null ? link.getTagCategory().getContributionTitle(lang) : null;

        superArgumentId = link.getSuperArgumentId();
        superArgumentTitle = link.getSuperArgument() != null ? link.getSuperArgument().getFullTitle() : null;

        shade = link.getLinkType().getEType();
        shadeId = shade.id();
        shadeName = link.getLinkType().getName(lang);
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = super.validate();

        if (!values.isBlank(shadeId)) {
            errors.put("shadeId", Collections.singletonList(new ValidationError("shadeId", "argument.links.error.linkshade")));
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Transform this form into an API justification link
     *
     * @return an API justification link corresponding to this justification link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    @Override
    public abstract JustificationLink toLink() throws PersistenceException ;

    /**
     * Update the given API justification link with this form values
     *
     * @param link the api justification link to update
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    protected void updateLink(JustificationLink link) throws PersistenceException  {
        super.updateLink(link);

        link.setSubContextId(subContextId);
        link.setTagCategoryId(categoryId);
        link.setSuperArgumentId(superArgumentId);
        try {
            link.setLinkType(argumentFactory.getJustificationLinkType(shadeId));
        } catch (FormatException e) {
            logger.error("unvalid justification link shade " + shadeId);
            throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
        }
    }

    @Override
    public String toString() {
        return " and category " + categoryId + " and super argument " + superArgumentId;
    }

    /*
     * GETTERS
     */

    public Long getSubContextId() {
        return subContextId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public Long getSuperArgumentId() {
        return superArgumentId;
    }

    public String getSuperArgumentTitle() {
        return superArgumentTitle;
    }

    /**
     * Get the link shade as EArgumentLinkShade
     *
     * @return a link shade
     * @see  EJustificationLinkShade
     */
    public EJustificationLinkShade getEShade() {
        return shade;
    }

    public EContributionType getContextType() {
        return contextType;
    }

    /*
     * SETTERS
     */

    public void setSubContextId(Long subContextId) {
        this.subContextId = subContextId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setSuperArgumentId(Long superArgumentId) {
        this.superArgumentId = superArgumentId;
    }
}
