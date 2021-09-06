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

package be.webdeb.infra.ws.external.citation;

import be.webdeb.infra.ws.external.VizContributionResponse;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This simple class holds values about citation that will be jsonified to be sent to external services.
 * This class allows to response to VizTextForm and sent all data about excerpts in text.
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VizCitationResponse extends VizContributionResponse {

    /**
     * Constructor.
     *
     * @param excerpt an api citation
     */
    public VizCitationResponse(CitationHolder excerpt) {
        super(excerpt.getId(), excerpt.getTextUrl(), excerpt.getOriginalExcerpt());
    }
}
