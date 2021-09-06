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
package be.webdeb.presentation.web.controllers;

import be.webdeb.presentation.web.views.html.oops.error;
import be.webdeb.presentation.web.views.html.oops.hack;
import be.webdeb.presentation.web.views.html.oops.oops;
import play.Configuration;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Error handler for Webdeb web platform. Display user-friendly pages in case of errors or bad requests.
 *
 * @author Fabian Gilson
 */
public class ErrorHandler extends DefaultHttpErrorHandler {

  private SessionHelper sessionHelper;
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Injected constructor
   *
   * @param conf the play configuration module
   * @param env the play environment module
   * @param mapper the source mapper
   * @param provider the routes provider
   */
  @Inject
  public ErrorHandler(Configuration conf, Environment env, OptionalSourceMapper mapper,
      Provider<Router> provider, SessionHelper sessionHelper) {
    super(conf, env, mapper, provider);
    this.sessionHelper = sessionHelper;
  }

  @Override
  public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
    logger.debug("invalid call received " + request.toString() + " from " + request.getHeader(SessionHelper.REMOTE_ADDRESS));
    return CompletableFuture.completedFuture(Results.notFound(
        oops.render(request.host() + request.uri(), sessionHelper.getUser(Http.Context.current()))));
  }

  @Override
  public CompletionStage<Result> onBadRequest(RequestHeader request, String message) {
    logger.debug("invalid call received " + request.toString() + " from " + request.getHeader(SessionHelper.REMOTE_ADDRESS));
    return CompletableFuture.completedFuture(Results.badRequest(
        hack.render(sessionHelper.getUser(Http.Context.current()))));
  }

  @Override
  public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
    logger.error("error while executing request " + request.toString(), exception);
    return CompletableFuture.completedFuture(Results.internalServerError(
        error.render(sessionHelper.getUser(Http.Context.current()))));
  }
}
