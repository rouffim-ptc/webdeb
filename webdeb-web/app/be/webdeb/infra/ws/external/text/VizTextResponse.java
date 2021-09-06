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

package be.webdeb.infra.ws.external.text;

import be.webdeb.infra.ws.external.VizContributionResponse;
import be.webdeb.infra.ws.external.citation.VizCitationResponse;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This simple class holds values about text that will be jsonified to be sent to external services.
 * This class allows to response to VizTextForm and sent all data about text.
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VizTextResponse extends VizContributionResponse {

    /**
     * The list of excerpts in the text, may be empty
     */
    @JsonSerialize
    private List<VizCitationResponse> excerpts;

    /**
     * Constructor.
     *
     * @param text an api text
     */
    public VizTextResponse(TextHolder text) {
        super(text.getId(), text.getUrl(), text.getTitle());
        this.excerpts = text.getCitations().stream().map(VizCitationResponse::new).collect(Collectors.toList());
    }

    public String getTitle() {
        return title;
    }

    public List<VizCitationResponse> getExcerpts() {
        return excerpts;
    }
}
