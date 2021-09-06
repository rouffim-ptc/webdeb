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

import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentHolder;
import be.webdeb.presentation.web.controllers.entry.link.BaseJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.link.BaseLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class holds concrete values of an CitationJustification link (i.e. no type/data IDs, but their descriptions, as
 * defined in the database)
 *
 * @author Martin Rouffiange
 */
public class CitationJustificationLinkForm extends BaseJustificationLinkForm {

    private CitationHolder citation;

    /**
     * Play / JSON compliant constructor
     */
    public CitationJustificationLinkForm() {
        super();
        type = EContributionType.CITATION_JUSTIFICATION;
    }

    /**
     * Construct a link wrapper with a given CitationJustification link (full initialization)
     *
     * @param link an existing CitationJustification
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationJustificationLinkForm(CitationJustification link, WebdebUser user, String lang) {
        super(link, user, lang);

        destinationTitle = link.getCitation().getWorkingExcerpt();
        citation = new CitationHolder(link.getCitation(), this, user, lang);
    }

    /**
     * Transform this form into an API justification link
     *
     * @return an API justification link corresponding to this justification link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    @Override
    public CitationJustification toLink() throws PersistenceException {
        CitationJustification link = citationFactory.getCitationJustificationLink();
        updateLink(link);
        return link;
    }

    @JsonIgnore
    public CitationHolder getCitation() {
        return citation;
    }
}
