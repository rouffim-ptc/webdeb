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

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.views.html.account.settings.login;
import be.webdeb.presentation.web.views.html.oops.hack;
import play.api.Play;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * This class implements the deadbolt handler used to check permissions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class WebdebDeadboltHandler extends AbstractDeadboltHandler {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  // a list of partial paths from which unauthorised user must be redirected to login
  // any other paths with user with insufficient privileges would denote a tentative of hacking
  private static final List<String> REDIRECT_PATHS = Arrays.asList("/entry", "/validate", "/settings", "/group");

  // this special partial path is used to get modal pages, for such pages, redirection must be handled
  // differently by redirecting to the full page after user logged in instead of just the modal page
  private static final String MODAL_PATH = "/modal";
  private static final String ASYNC_PATH = "/async";

  @Inject
  private SessionHelper sessionHelper = Play.current().injector().instanceOf(SessionHelper.class);

  @Inject
  private MessagesApi i18n = Play.current().injector().instanceOf(MessagesApi.class);

	/**
   * Deadbolt-compliant default constructor
   *
   * @param ecProvider the execution context provider (since we are in an async world)
   */
  public WebdebDeadboltHandler(ExecutionContextProvider ecProvider) {
    super(ecProvider);
  }

  @Override
  public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
    // no check run before authentication, but explicit override is needed
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
    return CompletableFuture.supplyAsync(() ->
        Optional.ofNullable(sessionHelper.getUser(context)), (Executor) executionContextProvider.get());
  }

  @Override
  public CompletionStage<Result> onAuthFailure(final Http.Context context, final Optional<String> content) {

    // if requested url contains any accessible paths from web pages => send to login/register page
    if (REDIRECT_PATHS.stream().anyMatch(context.request().uri()::contains)) {
      logger.info("unauthorized user, redirect to main page or main login page if user if not logged (from "
          + context.request().getHeader(SessionHelper.REMOTE_ADDRESS) + ")");
      Map<String, String> messages = new HashMap<>();

      WebdebUser user = sessionHelper.getUser(context);

      if(user.getERole() == EContributorRole.VIEWER) {
        messages.put("warning", i18n.get(context.lang(), "login.first"));
        // clear session and put in session requested page to be displayed afterwards
        context.session().clear();

        // set right page to be used as a redirect after successful login
        if (context.request().uri().contains(MODAL_PATH) || context.request().uri().contains(ASYNC_PATH)) {
          sessionHelper.set(context, SessionHelper.KEY_GOTO, sessionHelper.getReferer(context));
        } else {
          sessionHelper.set(context, SessionHelper.KEY_GOTO, context.request().uri());
        }

        // return login page
        return CompletableFuture.completedFuture(unauthorized(login.render(user, messages)));
      }
    }

    // otherwise send the hack page
    logger.warn("bad request from " + context.request().getHeader(SessionHelper.REMOTE_ADDRESS));
    return CompletableFuture.completedFuture(unauthorized(hack.render(sessionHelper.getUser(context))));
  }
}
