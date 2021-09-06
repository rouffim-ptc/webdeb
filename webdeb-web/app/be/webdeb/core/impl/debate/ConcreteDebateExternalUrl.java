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
package be.webdeb.core.impl.debate;

import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateExternalUrl;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.debate.DebateSimilarity;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractSimilarityLink;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a DebateExternalUrl representing an external url for a debate
 *
 * @author Martin Rouffiange
 */
public class ConcreteDebateExternalUrl implements DebateExternalUrl {

    private Long idUrl;
    private String url;
    private String alias;

    public ConcreteDebateExternalUrl() {
        idUrl = -1L;
    }

    public ConcreteDebateExternalUrl(Long idUrl, String url, String alias) {
        this.idUrl = idUrl;
        this.url = url;
        this.alias = alias;
    }

    @Override
    public Long getIdUrl() {
        return idUrl;
    }

    @Override
    public void setIdUrl(Long idUrl) {
        this.idUrl = idUrl;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }


    @Override
    public String toString() {
        return "ConcreteDebateExternalUrl{" +
                "idUrl=" + idUrl +
                ", url='" + url + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }
}