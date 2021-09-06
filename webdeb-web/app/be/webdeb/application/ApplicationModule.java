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
 *
 */

package be.webdeb.application;

import be.webdeb.application.query.QueryExecutor;
import be.webdeb.application.rest.service.RESTAccessor;
import com.google.inject.AbstractModule;

/**
 * This module registers the injected resources for the application layer, ie,
 * <ul>
 *   <li>the query executor used to query the database either from rest api or from UI</li>
 *   <li>the rest accessor that answers to queries from rest api and wraps objects in json format</li>
 * </ul>
 *
 * @author Fabian Gilson
 */
public class ApplicationModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(QueryExecutor.class);
    bind(RESTAccessor.class);
  }
}
