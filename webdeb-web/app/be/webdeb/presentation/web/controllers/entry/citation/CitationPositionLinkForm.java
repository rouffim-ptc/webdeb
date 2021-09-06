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

package be.webdeb.presentation.web.controllers.entry.citation;

import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.link.BaseLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;

import javax.inject.Inject;

/**
 * This class holds concrete values of an CitationPosition link (i.e. no type/data IDs, but their descriptions, as
 * defined in the database)
 *
 * @author Martin Rouffiange
 */
public class CitationPositionLinkForm extends BaseLinkForm {

    @Inject
    protected CitationFactory citationFactory = Play.current().injector().instanceOf(CitationFactory.class);

    private Long subDebateId;
    
    /**
     * Play / JSON compliant constructor
     */
    public CitationPositionLinkForm() {
        super();
        type = EContributionType.CITATION_POSITION;
    }

    /**
     * Construct a link wrapper with a given CitationPosition link (full initialization)
     *
     * @param link an existing CitationPosition
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationPositionLinkForm(CitationPosition link, WebdebUser user, String lang) {
        super(link, user, lang);
        this.subDebateId = link.getSubDebateId();
    }

    /**
     * Get the sub debate id of this position link
     *
     * @return the sub debate id
     */
    public Long getSubDebateId() {
        return subDebateId;
    }

    /**
     * Transform this form into an API position link
     *
     * @return an API position link corresponding to this position link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    @Override
    public CitationPosition toLink() throws PersistenceException {
        CitationPosition link = citationFactory.getCitationPositionLink();
        updateLink(link);
        return link;
    }

    /**
     * Update the given API position link with this form values
     *
     * @param link the api position link to update
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    protected void updateLink(CitationPosition link) throws PersistenceException  {
        super.updateLink(link);

        link.setSubDebateId(subDebateId);

        try {
            link.setLinkType(citationFactory.getPositionLinkType(shadeId));
        } catch (FormatException e) {
            logger.error("unvalid position link shade " + shadeId);
            throw new PersistenceException(PersistenceException.Key.SAVE_POSITION_LINK, e);
        }
    }
}
