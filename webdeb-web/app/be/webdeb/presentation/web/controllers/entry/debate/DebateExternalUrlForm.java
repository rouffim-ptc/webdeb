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

package be.webdeb.presentation.web.controllers.entry.debate;

import be.webdeb.core.api.debate.DebateExternalUrl;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;

/**
 * This class holds concrete values of a Debate external url
 *
 * @author Martin Rouffiange
 */
public class DebateExternalUrlForm {

    private Long idUrl = -1L;
    private String url;
    private String alias;

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Play / JSON compliant constructor
     */
    public DebateExternalUrlForm() {
        // needed by play
    }

    public DebateExternalUrlForm(DebateExternalUrl url) {
        this.idUrl = url.getIdUrl();
        this.url = url.getUrl();
        this.alias = url.getAlias();
    }


    public boolean isEmpty(){
        return values.isBlank(url);
    }


    public Long getIdUrl() {
        return idUrl;
    }

    public void setIdUrl(Long idUrl) {
        this.idUrl = idUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "DebateExternalUrlForm{" +
                "idUrl=" + idUrl +
                ", url='" + url + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }
}
