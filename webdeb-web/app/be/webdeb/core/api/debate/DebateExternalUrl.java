/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General  License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General  License for more details.
 *
 * You should have received a copy of the GNU General  License along with this program (see COPYING file).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package be.webdeb.core.api.debate;

/**
 * This interface represents a debate external link in the webdeb system.
 *
 * @author Martin Rouffiange
 */
public interface DebateExternalUrl {

    /**
     * Get the external url id
     *
     * @return an id
     */
     Long getIdUrl();

    /**
     * Set the external url id
     *
     * @param idUrl an id
     */
     void setIdUrl(Long idUrl);

    /**
     * Get the external url
     *
     * @return an url
     */
     String getUrl();

    /**
     * Set the external url
     *
     * @param url an url
     */
     void setUrl(String url);

    /**
     * Get the alias of the url
     *
     * @return the url alias
     */
     String getAlias();

    /**
     * Set the alias of the url
     *
     * @param alias the url alias
     */
     void setAlias(String alias);


}
