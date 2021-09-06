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

package be.webdeb.presentation.web.controllers.permission;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Default basic implementation of deadbolt handler cache object
 *
 * @author Fabian Gilson
 */
@Singleton
public class WebdebHandlerCache implements HandlerCache {

  private final Map<String, DeadboltHandler> handlers = new HashMap<>();

  /**
   * Deadbolt-compliant default constructor
   *
   * @param ecProvider the execution context provider (since we are in an async world)
   */
  @Inject
  public WebdebHandlerCache(final ExecutionContextProvider ecProvider) {
    handlers.put(ConfigKeys.DEFAULT_HANDLER_KEY, new WebdebDeadboltHandler(ecProvider));
  }

  @Override
  public DeadboltHandler apply(final String key) {
    return handlers.get(key);
  }

  @Override
  public DeadboltHandler get() {
    return handlers.get(ConfigKeys.DEFAULT_HANDLER_KEY);
  }

}
