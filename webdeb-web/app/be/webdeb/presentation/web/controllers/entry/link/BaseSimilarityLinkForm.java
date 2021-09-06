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
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import be.webdeb.core.api.contribution.link.SimilarityLink;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;


/**
 * This class holds supertype wrapper for every type of similarity links.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class BaseSimilarityLinkForm extends BaseLinkForm {

    @Inject
    protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

    @Inject
    protected DebateFactory debateFactory = Play.current().injector().instanceOf(DebateFactory.class);

    protected ESimilarityLinkShade shade;

    /**
     * Play / JSON compliant constructor
     */
    public BaseSimilarityLinkForm() {
        super();
    }

    /**
     * Constructor from a given contribution
     *
     * @param link a similarity link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public BaseSimilarityLinkForm(SimilarityLink link, WebdebUser user, String lang) {
        super(link, user, lang);

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
     * Transform this form into an API similarity link
     *
     * @return an API similarity link corresponding to this similarity link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    public abstract SimilarityLink toLink() throws PersistenceException ;

    /**
     * Update the given API similarity link with this form values
     *
     * @param link the api similarity link to update
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    protected void updateLink(SimilarityLink link) throws PersistenceException  {
        super.updateLink(link);

        try {
            link.setLinkType(argumentFactory.getSimilarityLinkType(shadeId));
        } catch (FormatException e) {
            logger.error("unvalid similarity link shade " + shadeId);
            throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
        }
    }

    /*
     * GETTERS
     */

    /**
     * Get the link shade as EArgumentLinkShade
     *
     * @return a link shade
     * @see  ESimilarityLinkShade
     */
    public ESimilarityLinkShade getEShade() {
        return shade;
    }

}
