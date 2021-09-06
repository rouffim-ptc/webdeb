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

package be.webdeb.infra.ws.external;

import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.text.TextFactory;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import play.api.Play;
import javax.inject.Inject;

public class VizExternalContributionResponse extends VizContributionResponse {

    /**
     * Contribution lang
     */
    @JsonSerialize
    protected String lang;

    /**
     * Contribution version date
     */
    @JsonSerialize
    protected String version;

    /**
     * Contribution type
     */
    @JsonSerialize
    protected Integer type;

    /**
     * The id of the corresponding internal contribution
     */
    @JsonSerialize
    protected Long idInternalContribution;

    @Inject
    protected TextFactory textFactory = Play.current().injector().instanceOf(TextFactory.class);

    @Inject
    protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

    /**
     * Default constructor
     *
     * @param contribution the external contribution
     * @param lang the lang of the contribution
     */
    public VizExternalContributionResponse(ExternalContribution contribution, String lang){
        super(contribution.getId(), contribution.getSourceUrl(), contribution.getTitle());
        this.lang = lang;
        this.type = contribution.getType().id();
        this.version = contribution.getVersionAsString();
        this.idInternalContribution = contribution.getInternalContribution();
    }
}
