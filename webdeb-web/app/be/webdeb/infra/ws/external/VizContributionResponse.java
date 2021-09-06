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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This abstract class is used to send a contribution to external service
 *
 * @author Martin Rouffiange
 */
public abstract class VizContributionResponse {

    /**
     * Contribution id
     */
    @JsonSerialize
    protected Long id;

    /**
     * Contribution url
     */
    @JsonSerialize
    protected String url;

    /**
     * Text title
     */
    @JsonSerialize
    protected String title;

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Constructor.
     *
     * @param id the contribution id
     * @param url the contribution url
     */
    public VizContributionResponse(Long id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}