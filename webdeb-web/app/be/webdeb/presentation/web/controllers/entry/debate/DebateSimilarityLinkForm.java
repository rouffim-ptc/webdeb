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

package be.webdeb.presentation.web.controllers.entry.debate;

import be.webdeb.core.api.debate.DebateSimilarity;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.link.BaseSimilarityLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

/**
 * This class holds concrete values of an DebateSimilarity link (i.e. no type/data IDs, but their descriptions, as
 * defined in the database)
 *
 * @author Martin Rouffiange
 */
public class DebateSimilarityLinkForm extends BaseSimilarityLinkForm {

    /**
     * Play / JSON compliant constructor
     */
    public DebateSimilarityLinkForm() {
        super();
        type = EContributionType.DEBATE_SIMILARITY;
    }

    /**
     * Construct a link wrapper with a given DebateSimilarity link (full initialization)
     *
     * @param link an existing DebateSimilarity
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public DebateSimilarityLinkForm(DebateSimilarity link, WebdebUser user, String lang) {
        super(link, user, lang);
        originTitle = link.getOrigin().getFullTitle();
        destinationTitle = link.getDestination().getFullTitle();
    }

    /**
     * Transform this form into an API similarity link, maybe setting the link type
     *
     * @return an API link corresponding to this similarity link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    public DebateSimilarity toLink() throws PersistenceException {
        DebateSimilarity link = debateFactory.getDebateSimilarityLink();
        updateLink(link);
        return link;
    }

}
