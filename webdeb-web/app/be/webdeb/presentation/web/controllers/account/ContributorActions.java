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

package be.webdeb.presentation.web.controllers.account;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EClaimType;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.mail.MailerException;
import be.webdeb.infra.mail.WebdebMail;
import be.webdeb.infra.ws.external.VizExternalContributionResponse;
import be.webdeb.infra.ws.external.auth.AuthForm;
import be.webdeb.infra.ws.external.auth.UserRequest;
import be.webdeb.infra.ws.geonames.RequestProxy;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.account.register.ContributorForm;
import be.webdeb.presentation.web.controllers.account.settings.DeleteAccountForm;
import be.webdeb.presentation.web.controllers.account.settings.ESettingsPane;
import be.webdeb.presentation.web.controllers.account.settings.MailForm;
import be.webdeb.presentation.web.controllers.account.settings.PasswordForm;
import be.webdeb.presentation.web.controllers.browse.SearchForm;
import be.webdeb.presentation.web.controllers.entry.*;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.SessionHelper;

import be.webdeb.presentation.web.views.html.account.admin.advicesModal;
import be.webdeb.presentation.web.views.html.account.admin.claims;
import be.webdeb.presentation.web.views.html.account.contactUserToUserFormModal;
import be.webdeb.presentation.web.views.html.account.register.nomailreceived;
import be.webdeb.presentation.web.views.html.account.settings.*;
import be.webdeb.presentation.web.views.html.account.register.created;
import be.webdeb.presentation.web.views.html.account.register.signup;
import be.webdeb.presentation.web.views.html.contactus;
import be.webdeb.presentation.web.views.html.oops.hack;
import be.webdeb.presentation.web.views.html.oops.oops;
import be.webdeb.presentation.web.views.html.util.message;

import be.webdeb.presentation.web.views.html.viz.common.util.claimContributionModal;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Handle all actions regarding contributors: sign in/up, update profile/avatar, change/recover password or change mail.
 * Does not handle group-related actions
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ContributorActions extends CommonController {

  @Inject
  private Mailer mailer;

  @Inject
  protected be.webdeb.infra.ws.nlp.RequestProxy proxy;

  @Inject
  protected RequestProxy geonames;

  private static final JacksonFactory jacksonFactory = new JacksonFactory();

  // string constante
  private static final String LOGIN_FAILURE = "login.failure";
  private static final String EMAIL_OR_PSEUDO = "emailOrPseudo";
  private static final String EMAIL_NOTSEND = "could not send mail to user ";

  /**
   * Display login page
   *
   * @return the login page
   */
  public Result login() {
    logger.debug("GET login page");
    return ok(login.render(sessionHelper.getUser(ctx()), null));
  }

  /**
   * Handle login form submission.
   *
   * @return the dashboard page if authorization succeeded or login form if authorization failed
   */
  public Result authenticate() {
    logger.debug("POST authenticate");
    Form<LoginForm> form = formFactory.form(LoginForm.class).bindFromRequest();
    Map<String, String> message = new HashMap<>();

    if (form.hasErrors()) {
      message.put("danger", i18n.get(ctx().lang(), LOGIN_FAILURE));
      WebdebUser user = sessionHelper.getUser(ctx());
      user.setLoginForm(form);
      logger.debug("form has errors " + form.errors().toString());
      return badRequest(login.render(user, message));
    }

    // now check if email - password matches and if user is validated
    LoginForm loginForm = form.get();
    try {
      Contributor contributor = contributorFactory.authenticate(loginForm.getEmailOrPseudo(), loginForm.getPassword());
      if (contributor == null) {
        TmpContributor tmpContributor = contributorFactory.tmpauthenticate(loginForm.getEmailOrPseudo(), loginForm.getPassword());
        if(tmpContributor != null && tmpContributor.getContributor() == null){
          // tmp is good, keep pseudo in session and redirect to signup from project
          session(SessionHelper.KEY_USERPSEUDO_TMP, loginForm.getEmailOrPseudo());
          return signupFromProject(loginForm.getEmailOrPseudo());
        }else{
          // no contributor retrieved from given email / pseudo - password
          message.put("danger", i18n.get(ctx().lang(), LOGIN_FAILURE));
          form.reject(new ValidationError(EMAIL_OR_PSEUDO, LOGIN_FAILURE));
          WebdebUser user = sessionHelper.getUser(ctx());
          user.setLoginForm(form);
          return badRequest(login.render(user, message));
        }
      } else if (!contributor.isValidated() && (contributor.getTmpContributor() == null || !contributor.getTmpContributor().getProject().isInProgress())) {
        // user has not yet validated his account, ask him to check his email
        message.put("warning", i18n.get(ctx().lang(), "login.not.validated"));
        form.reject(new ValidationError(EMAIL_OR_PSEUDO, "signin.not.validated"));
        WebdebUser user = sessionHelper.getUser(ctx());
        user.setLoginForm(form);
        return badRequest(login.render(user, message));

      } else if (contributor.isBanned()) {
        // user is banned -> redirect to main page and warn him
        logger.info("banned user " + contributor.getEmail() + " tried to connect");
        flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "login.banned"));
        session().clear();
        return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
      }

      // all good, put email in session
      session(SessionHelper.KEY_USERMAILORPSEUDO, loginForm.getEmailOrPseudo());
      WebdebUser user = sessionHelper.getUser(ctx());

      if(loginForm.isRemember()) {
        response().setCookie(sessionHelper.getRememberUserEmailCookie(loginForm.getEmailOrPseudo()));
        response().setCookie(sessionHelper.getRememberUserPasswordCookie(contributor.newAuthToken()));
      }else{
        response().setCookie(sessionHelper.getRememberUserEmailCookie(""));
        response().setCookie(sessionHelper.getRememberUserPasswordCookie(""));
      }

      // get user from session to ensure cookies are correctly stored and readable
      if (user == null) {
        logger.error("correctly validated user is not able to login " + loginForm.getEmailOrPseudo());
        message.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "login.error.cookies"));
        return badRequest(login.render(sessionHelper.getUser(ctx()), message));
      }
      session(SessionHelper.KEY_USERNAME, user.getContributor().getFirstname());
      flash("success", i18n.get(ctx().lang(), "login.success") + " " + session(SessionHelper.KEY_USERNAME));

      String next = sessionHelper.get(ctx(), SessionHelper.KEY_GOTO);
      // if user was coming looking for a page, but had to login first
      if (next != null) {
        logger.debug("redirect to " + next);
        sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
        return redirect(next);
      }
      // are we on a specific page (different login) ?
      String referer = sessionHelper.getReferer(ctx());

      if (referer != null
          && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.login().url())
          && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.signup().url())
          && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.confirm("").url())) {
        return redirect(referer);
      }

      // redirect to user dashboard in last resort
      return redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1));
    } catch (TokenExpiredException e) {
      logger.warn("token has expired for user ", e);
      Contributor contributor = contributorFactory.retrieveContributor(loginForm.getEmailOrPseudo());
      // this contributor did not confirm his registration, but he's still in database,
      // resend a new validation token
      try {
        mailer.sendMail(new WebdebMail(WebdebMail.EMailType.SUBSCRIBE, contributor.getEmail(),
            routes.ContributorActions.confirm(contributor.newConfirmationToken()).url(), ctx().lang()));
      } catch (PersistenceException | MailerException ex) {
        logger.error(EMAIL_NOTSEND + contributor.getEmail(), ex);
        message.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.signup.error.email"));
      }
      form.reject(new ValidationError(EMAIL_OR_PSEUDO, "login.not.validated"));
      WebdebUser user = sessionHelper.getUser(ctx());
      user.setLoginForm(form);
      return badRequest(login.render(user, message));
    }
  }

  /**
   * Handle login form submission from open id auth.
   *
   * @return the dashboard page if authorization succeeded or login form if authorization failed
   */
  public CompletionStage<Result> authenticateWithOpenId() {
    logger.debug("POST authenticate with open id");
    Form<LoginFormOpenId> form = formFactory.form(LoginFormOpenId.class).bindFromRequest();

    if (form.hasErrors()) {
      logger.debug(form.data()+"///");
      return sendBadRequest();
    }

    LoginFormOpenId loginForm = form.get();
    OpenIdUserData userData;

    switch (loginForm.getEtype()) {
      case FACEBOOK:
        userData = checkFacebookAuth(loginForm);
        break;
      case GOOGLE:
        userData = checkGoogleAuth(loginForm.getToken());
        break;
      default :
        userData = null;
    }

    if(userData == null) {
      return sendUnauthorized();
    }

    Contributor contributor = contributorFactory.retrieveContributor(loginForm.getId(), loginForm.getEtype());

    if(contributor != null) {
      if (contributor.isBanned()) {
        // user is banned -> redirect to main page and warn him
        logger.info("banned user " + contributor.getPseudo() + " tried to connect");
        flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "login.banned"));
        session().clear();
        return sendBadRequest();
      }

      contributor.setOpenIdToken(loginForm.getToken());

    } else {

      Contributor existingPseudo = contributorFactory.retrieveContributor(userData.getPseudo());
      logger.debug("existing open id " + userData.getPseudo());
      if (existingPseudo != null) {
        return sendInternalServerError();
      }

      contributor = contributorFactory.getContributor();
      contributor.setId(-1L);

      contributor.setOpenId(userData.getId());
      contributor.setOpenIdToken(loginForm.getToken());
      contributor.setOpenIdType(loginForm.getEtype());

      try {
        if(!values.isBlank(userData.getEmail())) {
          contributor.setEmail(userData.getEmail());
        }
        contributor.setPseudo(userData.getPseudo());
      } catch (FormatException e) {
        logger.error("unparsable email : " + userData.getEmail() + " or pseudo : " + userData.getPseudo());
        return sendBadRequest();
      }
      contributor.setFirstname(userData.getFirstName());
      contributor.setLastname(userData.getLastName());


      if (!values.isBlank(userData.getResidence())) {
        try {
          contributor.setResidence(actorFactory.getCountry(userData.getResidence()));
        } catch (FormatException e) {
          // should not happen here since the form has been validated
          logger.error("unknown country code " + userData.getResidence(), e);
        }
      }

      contributor.isNewsletter(false);
    }

    // default group
    try {
      contributor.save(be.webdeb.core.api.contributor.Group.getGroupPublic());
      logger.info("saved " + contributor.toString());
    } catch (PersistenceException e) {
      // should not happen here since the form has been validated
      logger.error("opend id auth failed ", e);
      return sendInternalServerError();
    }

    return doOpenIdAuthentication(contributor);
  }

  private CompletionStage<Result> doOpenIdAuthentication(Contributor contributor) {
    // all good, put email in session
    session(SessionHelper.KEY_USERMAILORPSEUDO, contributor.getPseudo());
    WebdebUser user = sessionHelper.getUser(ctx());

    // get user from session to ensure cookies are correctly stored and readable
    if (user == null) {
      logger.error("correctly validated user is not able to login " + contributor.getPseudo());
      return sendBadRequest();
    }

    session(SessionHelper.KEY_USERNAME, user.getContributor().getFirstname());

    String next = sessionHelper.get(ctx(), SessionHelper.KEY_GOTO);
    // if user was coming looking for a page, but had to login first
    if (next != null) {
      logger.debug("redirect to " + next);
      sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
      return sendOk(next);
    }
    // are we on a specific page (different login) ?
    String referer = sessionHelper.getReferer(ctx());

    if (referer != null
            && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.login().url())
            && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.signup().url())
            && !referer.contains(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.confirm("").url())) {
      return sendOk(referer);
    }

    // redirect to user dashboard in last resort
    return sendOk(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1).url());
  }

  /**
   * Handle login form submission.
   *
   * @return the dashboard page if authorization succeeded or login form if authorization failed
   */
  public CompletionStage<Result> tokenAuthentication() {
    logger.debug("POST token authentication");
    Form<LoginForm> form = formFactory.form(LoginForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return CompletableFuture.supplyAsync(() ->
              badRequest(form.errorsAsJson()), context.current());
    }

    LoginForm loginForm = form.get();

    // now check if email - password matches and if user is validated and not banned
    Contributor contributor = contributorFactory.tokenAuthentication(loginForm.getEmailOrPseudo(), loginForm.getPassword());

    if (contributor == null || !contributor.isValid()) {
      // no contributor retrieved from given email - password
      return CompletableFuture.supplyAsync(Results::badRequest, context.current());

    }

    // all good, return the user data and the user auth token
    UserRequest userRequest = new UserRequest(
            contributor.getEmail(),
            contributor.getFirstname(),
            contributor.getLastname(),
            contributor.newAuthToken());
    return CompletableFuture.supplyAsync(() ->
            ok(Json.toJson(userRequest)), context.current());
  }

  /**
   * Check authentification token validity
   *
   * @return 200 if the token is valid or 400
   */
  public CompletionStage<Result> checkAuthTokenValidity() {
    Form<LoginForm> form = formFactory.form(LoginForm.class).bindFromRequest();

    if (form.hasErrors()) {
      logger.debug(form.errors()+"");
      return CompletableFuture.supplyAsync(() ->
              badRequest(form.errorsAsJson()), context.current());
    }

    LoginForm loginForm = form.get();
    // now check if email - token matches and if user is validated and not banned
    if (contributorFactory.checkAuthTokenValidity(loginForm.getEmailOrPseudo(), loginForm.getPassword())) {
      // token is valid
      return CompletableFuture.supplyAsync(Results::ok, context.current());

    }
    return CompletableFuture.supplyAsync(Results::badRequest, context.current());
  }

  /**
   * Logout and clean the session.
   *
   * @return the main index page
   */
  public Result logout() {
    WebdebUser user = sessionHelper.getUser(ctx());
    session().clear();
    response().setCookie(sessionHelper.getRememberUserEmailCookie(""));
    response().setCookie(sessionHelper.getRememberUserPasswordCookie(""));
    flash(SessionHelper.INFO, i18n.get(ctx().lang(),"login.logout"));
    return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
  }

  /**
   * Logout and clean the session async.
   *
   * @return the main index page
   */
  public CompletionStage<Result> logoutAsync() {
    session().clear();
    response().setCookie(sessionHelper.getRememberUserEmailCookie(""));
    response().setCookie(sessionHelper.getRememberUserPasswordCookie(""));
    flash(SessionHelper.INFO, i18n.get(ctx().lang(),"login.logout"));
    return sendOk(be.webdeb.presentation.web.controllers.routes.Application.index().url());
  }

  /**
   * Main settings page
   *
   * @param pane a ESettingsPane id to display
   * @return the index settings page at given pane
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> settings(Long contributor, int pane) {
    logger.debug("GET settings page on pane " + pane + " for contributor " + contributor);
    WebdebUser user = sessionHelper.getUser(ctx());
    Contributor c = contributorFactory.retrieveContributor(contributor);
    if (c == null) {
      logger.error("user not found with id " + contributor);
      return CompletableFuture.completedFuture(
          notFound(oops.render(sessionHelper.getReferer(ctx()), user)));
    }
    // check if context user is the same as requested contributor, or if this user belongs to a public group
    // or if session user owns a group to which requested contributor belongs
    if (c.getId().equals(user.getId())
        || c.getGroups().stream().anyMatch(s ->
        s.getGroup().getMemberVisibility().equals(EMemberVisibility.PUBLIC)
            || s.getGroup().getGroupOwners().stream().anyMatch(g -> g.getContributor().getId().equals(user.getId())))) {

      Form<SearchForm> form = getSettingsSearchForm(contributor);

      return CompletableFuture.supplyAsync(() ->
          ok(userSettings.render(ESettingsPane.value(pane), new ContributorHolder(c, user, ctx().lang().code()),
              form, userMayEditProfile(user, contributor), helper, user, null)), context.current());
    }
    // user may not see profile -> hack page
    return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
  }

  /**
   * Check that the user is well authenticated
   *
   * @return ok if authenticated
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> checkUserAuth() {
    return CompletableFuture.completedFuture(
            ok(""));
  }

  /**
   * Search for contributor's contributions
   *
   * @param searchText a query string
   * @param id the contributor id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of Contributions
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchContributorContributions(String searchText, Long id, int fromIndex, int toIndex) {
    WebdebUser user = sessionHelper.getUser(ctx());
    logger.debug("GET contributions for contributor " + id);

    List<ContributionHolder> contributions = user.getContributor()
        .toHolders(contributorFactory.searchContributorContributions(searchText, id, fromIndex, toIndex), user, ctx().lang().code());

    return CompletableFuture.completedFuture(
        ok(userContributionsDashboardList.render(contributions, null)));
  }

  /**
   * Display the signup form page.
   *
   * @return signup form
   */
  public Result signup() {
    logger.debug("GET signup page");
    ContributorForm form = new ContributorForm();
    form.setLang(ctx().lang().code());
    return ok(signup.render(formFactory.form(ContributorForm.class).fill(form), sessionHelper.getUser(ctx()), helper, null));
  }

  /**
   * Display the signup form page from project.
   *
   * @return signup form
   */
  public Result signupFromProject(String login) {
    logger.debug("GET signup from project page");
    // Check that the tmp user is previously auth
    WebdebUser user = sessionHelper.getUser(ctx(), true);
    TmpContributor tmpContributor = contributorFactory.retrieveTmp(login);

    if(user == null || tmpContributor == null || login == null || !login.equals(user.getTmpPseudo())){
      sessionHelper.remove(ctx(), SessionHelper.KEY_USERPSEUDO_TMP);
      return badRequest();
    }

    ContributorForm form = new ContributorForm(tmpContributor);
    form.setLang(ctx().lang().code());
    return ok(signup.render(formFactory.form(ContributorForm.class).fill(form), sessionHelper.getUser(ctx()), helper, null));
  }

  /**
   * Display the signup form page when coming from an invitation
   *
   * @return signup form, or the hack page if token does not exist
   */
  public CompletionStage<Result> signupWithToken(String token) {
    logger.debug("GET signup page with token " + token);

    // check if token exist
    Contributor contributor = contributorFactory.retrieveContributorByInvitation(token);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (contributor == null) {
      logger.warn("someone tried to get invited with wrong token");
      return CompletableFuture.completedFuture(
          badRequest(hack.render(new WebdebUser(formFactory.form(LoginForm.class), contributorFactory, textFactory, null))));
    }

    // return sign up page
    return CompletableFuture.completedFuture(
        ok(signup.render(formFactory.form(ContributorForm.class).fill(new ContributorForm(contributor, true, user, ctx().lang().code())),
            sessionHelper.getUser(ctx()), helper, null))
    );
  }

  /**
   * Display the contributor's profile edition page
   *
   * @return edit profile form, or the hack page if current user may not requested contributor
   */
  @Restrict(@Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> update(Long contributor) {
    logger.debug("GET update contributor page for " + contributor);

    // check if current user may do it
    WebdebUser user = sessionHelper.getUser(ctx());
    if (!userMayEditProfile(user, contributor)) {
      return CompletableFuture.supplyAsync(() -> unauthorized(hack.render(user)), context.current());
    }

    // check if contributor exists
    Contributor c = contributorFactory.retrieveContributor(contributor);

    if (c == null) {
      logger.error("no contributor with id " + contributor);
      flash(SessionHelper.ERROR, "contributor.edit.nosuch");
      return CompletableFuture.completedFuture(redirect(sessionHelper.getReferer(ctx())));
    }

    // return edition page
    return CompletableFuture.completedFuture(
        ok(editContributor.render(
            formFactory.form(ContributorForm.class).fill(new ContributorForm(c, false, user, ctx().lang().code())),
            sessionHelper.getUser(ctx()), helper, null))
    );
  }

  /**
   * Save the new user as a Contributor
   *
   * @return Successful page or created form if bad
   */
  public CompletionStage<Result> save() {
    logger.debug("new registration request");
    Form<ContributorForm> contributorForm = formFactory.form(ContributorForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> message = new HashMap<>();

    Contributor contributor;
    try {
      contributor = saveContributor(request(), contributorForm);
    } catch (ContributorNotSavedException e) {
      return handleContributorError(e, user, false);
    }

    // now try to send a mail user has been saved
    try {
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.SUBSCRIBE, contributor.getEmail(),
          routes.ContributorActions.confirm(contributor.newConfirmationToken()).url(), ctx().lang()));
    } catch (PersistenceException | MailerException e) {
      logger.error(EMAIL_NOTSEND + contributor.getEmail(), e);
      message.put(SessionHelper.ERROR, i18n.get(ctx().lang(),"contributor.signup.error.email"));
      return CompletableFuture.supplyAsync(() ->
          badRequest(signup.render(contributorForm, user, helper, message)), context.current());
    }
    // all good, send the created page
    return CompletableFuture.supplyAsync(() ->
        ok(created.render(sessionHelper.getUser(ctx()), message)), context.current());
  }

  /**
   * Update a user's profile, check whether current user may edit user's profile
   *
   * @return either the update page if the form contains error, or the page displaying the user's profile
   */
  @Restrict(@Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveUpdate() {
    logger.debug("POST update profile");
    Form<ContributorForm> contributorForm = formFactory.form(ContributorForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());

    try {
      saveContributor(request(), contributorForm);
    } catch (ContributorNotSavedException e) {
      return handleContributorError(e, user, true);
    }

    // all good, send back the profile page
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.edit.success"));
    return CompletableFuture.supplyAsync(() ->
        redirect(routes.ContributorActions.settings(user.getId(), ESettingsPane.PROFILE.id())), context.current());
  }

  /**
   * Get the modal to claim a contributin
   *
   * @return the modal
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getClaimContribution(Long contribution, String url) {
    Contribution c = textFactory.retrieveContribution(contribution);
    String lang = ctx().lang().code();

    return c != null ? sendOk(claimContributionModal.render(c.getContributionTitle(lang), contribution, url)) : sendBadRequest();
  }

  /**
   * Save a contributor claim about a contribution
   *
   * @return ok if saved
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveClaimContribution() {
    try {
      JsonNode response = ctx().request().body().asJson();
      WebdebUser user = sessionHelper.getUser(ctx());
      Long contribution = response.get("contribution").asLong();
      String url = response.get("url").asText();
      String comment = response.get("comment").asText();
      int type = response.get("type").asInt();

      return textFactory.claimContribution(contribution, user.getId(), url, comment, EClaimType.value(type), user.getGroupId()) ? sendOk() : sendBadRequest();
    } catch(Exception e) {
      return sendBadRequest();
    }
  }

  /**
   * Delete given claim
   *
   */
  @Restrict({
          @be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN),
          @be.objectify.deadbolt.java.actions.Group(WebdebRole.OWNER)
  })
  public CompletionStage<Result> deleteClaim(Long contribution, Long contributor) {
    return textFactory.deleteClaim(contribution, contributor) ? sendOk() : sendBadRequest();
  }

  /**
   * Retrieve contributor claims
   *
   * @return the list of claims
   */
  @Restrict({
          @be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN),
          @be.objectify.deadbolt.java.actions.Group(WebdebRole.OWNER)
  })
  public CompletionStage<Result> retrieveClaims(int fromIndex, int toIndex) {
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    return sendOk(claims.render(textFactory.retrieveClaims(user.getId(), fromIndex, toIndex, lang)));
  }

  /**
   * Handle form error in contributor creation / edition
   *
   * @param e the exception raised when submitting the form
   * @param user current user submitting the form (ensure he has the permission to do it)
   * @param existing true if user submitted the form for an existing contributor (editContributor), false otherwise (signup)
   * @return either the unauthorized page if given user may not submit this form, the signup or editContributor pages
   */
  @SuppressWarnings("fallthrough")
  private CompletionStage<Result> handleContributorError(ContributorNotSavedException e, WebdebUser user, boolean existing) {
    Map<String, String> message = new HashMap<>();
    switch (e.error) {
      case ContributorNotSavedException.UNAUTHORIZED:
        return CompletableFuture.supplyAsync(() -> unauthorized(hack.render(user)), context.current());

      case ContributorNotSavedException.ERROR_FORM:
      case ContributorNotSavedException.NAME_MATCH:
        if(e.error == ContributorNotSavedException.ERROR_FORM)
          message.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "contributor.error"));
        return CompletableFuture.supplyAsync(() ->
            badRequest(existing ?
                editContributor.render(e.form, user, helper, message)
                : signup.render(e.form, user, helper, message)),
            context.current());

      default:
        // any other problem (eg with DB)
        message.put(SessionHelper.ERROR, i18n.get(ctx().lang(), e.getMessage()));
        return CompletableFuture.supplyAsync(() ->
                badRequest(existing ?
                    editContributor.render(e.form, user, helper, message)
                    : signup.render(e.form, user, helper, message)),
            context.current());
    }
  }

  /**
   * Upload a new picture for given contributor
   *
   * @param id a contributor id
   * @return the newly created avatar name, or a message template if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> uploadContributorPicture(Long id) {
    logger.debug("POST upload picture for contributor " + id);
    Http.MultipartFormData<File> body = request().body().asMultipartFormData();
    Http.MultipartFormData.FilePart<File> picture = body.getFile("picture");
    Map<String, String> messages = new HashMap<>();
    flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "general.upload.pic.error"));

    if (picture == null) {
      logger.error("no picture passed");
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    Contributor contributor = contributorFactory.retrieveContributor(id);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (!userMayEditProfile(user, id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // check this picture is ok
    files.saveToCache(picture.getFile(), picture.getFilename());
    Http.Context ctx = ctx();

    return detector.isImageSafe(configuration.getString("server.hostname") + be.webdeb.presentation.web.controllers.routes.Application
        .getFile(picture.getFilename(), "tmp").url()).handleAsync((safe, t) -> {
      if (safe || t != null) {
        // save avatar extension in DB and file in FS
        if(contributor.getAvatar() != null)
          contributor.getAvatar().setExtension(picture.getFilename());
        try {
          contributor.save(sessionHelper.getCurrentGroup(ctx));
        } catch (PersistenceException e) {
          logger.error("unable to update avatar for " + id, e);
          return badRequest(message.render(messages));
        }
        // retrieve name of avatar as stored in DB since it may have been adapted (avoid name conflicts)
        if(contributor.getAvatar() != null)
          files.saveUserPictureFile(picture.getFile(), contributor.getAvatar().getPictureFilename(), false);
        return ok(Json.toJson(contributor.getAvatar()));
      } else {
        // unsafe image, warn user
        flash(SessionHelper.ERROR, i18n.get(ctx.lang(), "general.upload.pic.unsafe"));
        return badRequest(message.render(messages));
      }
    });
  }

  /**
   * Validate an account (after a signup, user must validate his/her account with a unique token he received)
   *
   * @param token a unique token attached to the user when he subscribed
   * @return login page with a message of the status
   */
  public Result confirm(String token) {
    logger.debug("GET confirm subscription");
    Contributor contributor = contributorFactory.retrieveContributorByConfirmationToken(token);
    Map<String, String> message = new HashMap<>();

    WebdebUser user = sessionHelper.getUser(ctx());

    if (contributor == null) {
      logger.warn("no contributor found for token " + token);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),"contributor.validate.unknown"));
      return badRequest(login.render(user, message));
    }

    if (contributor.isValidated()) {
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),"contributor.validate.already"));
      return badRequest(login.render(user, message));
    }

    if (contributor.hasConfirmationTokenExpired()) {
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),"contributor.validate.expired"));
      return badRequest(login.render(user, message));
    }

    try {
      // validate and send mail confirmation
      contributor.validate();
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.CONFIRM_SUBSCRIBE, contributor.getEmail(),
          be.webdeb.presentation.web.controllers.routes.Application.index().url(), ctx().lang()));
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"contributor.validate.success", contributor.getFirstname()));
      return ok(login.render(user, message));

    } catch (TokenExpiredException | PersistenceException | MailerException e) {
      logger.debug("unable to validate account", e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.validate.failure"));
      return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
    }
  }

  /**
   * Get modal page to let a contributor to change his/her email
   *
   * @return the modal page to change the email, a hack page if given id and retrieved context user id do not match
   */
  public CompletionStage<Result> askChangeMail(Long id) {
    logger.debug("GET modal page to change email for contributor " + id);
    WebdebUser user = sessionHelper.getUser(ctx());

    // both ids do not match -> hack page
    if (!user.getId().equals(id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // return modal form
    return CompletableFuture.completedFuture(
        ok(changeMail.render(id, formFactory.form(MailForm.class).fill(new MailForm()))));
  }

  /**
   * Execute change mail request
   *
   * @param id the contributor id
   * @return either the modal page if the submitted form contains error, or the profile pane
   */
  public CompletionStage<Result> changeMail(Long id) {
    logger.debug("POST change email for contributor " + id);
    WebdebUser user = sessionHelper.getUser(ctx());

    // both ids do not match -> hack page
    if (!user.getId().equals(id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // check form
    Form<MailForm> form = formFactory.form(MailForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      return CompletableFuture.completedFuture(badRequest(changeMail.render(id, form)));
    }

    // handle change mail request
    Map<String, String> messages = new HashMap<>();
    try {
      // simply send an email to given mail address with new token for user.
      // He will simply enter in process of email validation (like for a new subscription)
      String email = form.get().getEmail();
      Contributor contributor = contributorFactory.retrieveContributor(id);
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.CHANGE_MAIL, email,
          routes.ContributorActions.validateEmail(contributor.newConfirmationToken(), email).url(), ctx().lang()));

    } catch (PersistenceException | MailerException e) {
      logger.error("unable to initiate change mail for " + id, e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.change.mail.fail"));
      return CompletableFuture.completedFuture(
        internalServerError(userSettings.render(ESettingsPane.PROFILE, user.getContributor(), getSettingsSearchForm(id), true, helper, user, messages)));
    }

    // all good
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.change.mail.success"));
    return CompletableFuture.completedFuture(ok(userProfile.render(user.getContributor(), values, user, messages)));
  }

  /**
   * Validate an email, used when a user changed his registration email.
   *
   * @return index page with flash error or success
   */
  public Result validateEmail(String token, String mail) {
    logger.debug("GET validate new email for contributor " + mail);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (token == null) {
      return badRequest(hack.render(user));
    }

    Contributor contributor = contributorFactory.retrieveContributorByConfirmationToken(token);
    if (contributor == null) {
      return badRequest(hack.render(user));
    }

    // check if token has expired, if such, user will have to restart process
    if (contributor.hasConfirmationTokenExpired()) {
      flash(SessionHelper.WARNING, i18n.get(ctx().lang(), "contributor.validate.mail.expired"));
      return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
    }

    // looks ok, update email and re-validate user
    try {
      logger.info("validate new email " + mail + " for user " + contributor.getEmail());
      contributor.setEmail(mail);
      contributor.validate();

      // update email in session cookie
      session(SessionHelper.KEY_USERMAILORPSEUDO, mail);
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.validate.success", contributor.getEmail()));
      return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());

    } catch (FormatException | PersistenceException | TokenExpiredException e) {
      logger.error("unable to store new email for " + contributor.getEmail(), e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.validate.failure"));
      return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
    }
  }

  /**
   * Get modal page to let a contributor to change his/her password
   *
   * @return the modal page to change the password, a hack page if given id and retrieved context user id do not match
   */
  public CompletionStage<Result> askChangePassword(Long id) {
    logger.debug("GET modal page to change password for " + id);
    WebdebUser user = sessionHelper.getUser(ctx());

    // both ids do not match -> hack page
    if (!user.getId().equals(id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // return modal form
    return CompletableFuture.completedFuture(
        ok(changePassword.render(id, formFactory.form(PasswordForm.class).fill(new PasswordForm()))));
  }

  /**
   * Execute change password request
   *
   * @param id the contributor id
   * @return either the modal page if the submitted form contains error, or a message with the result of the request
   */
  public CompletionStage<Result> changePassword(Long id) {
    logger.debug("POST change password for " + id);
    WebdebUser user = sessionHelper.getUser(ctx());

    // both ids do not match -> hack page
    if (!user.getId().equals(id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // check form
    Form<PasswordForm> form = formFactory.form(PasswordForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      return CompletableFuture.completedFuture(badRequest(changePassword.render(id, form)));
    }

    // handle request
    Map<String, String> messages = new HashMap<>();
    try {
      Contributor contributor = contributorFactory.retrieveContributor(id);
      contributor.changePassword(form.get().getPassword());
    } catch (PersistenceException e) {
      logger.error("unable to change password for " + id, e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.change.password.fail"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }

    // all good
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.change.password.success"));
    return CompletableFuture.completedFuture(ok(message.render(messages)));
  }

  /**
   * Get modal page to let a contributor delete his account
   *
   * @param id the contributor id
   * @return the modal page to delete the contributor account, a hack page if given id and retrieved context user id do not match
   */
  public CompletionStage<Result> askDeleteAccount(Long id) {
    logger.debug("GET modal page to delete contributor " + id);
    WebdebUser user = sessionHelper.getUser(ctx());

    // both ids do not match -> hack page
    if (!user.getId().equals(id)) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // return modal form
    return CompletableFuture.completedFuture(
            ok(deleteAccountModal.render(id, formFactory.form(DeleteAccountForm.class).fill(new DeleteAccountForm(id)))));
  }

  /**
   * Execute delete account request
   *
   * @return either the modal page if the submitted form contains error, or a message with the result of the request
   */
  public CompletionStage<Result> deleteAccount(Long id) {
    logger.debug("POST delete account");
    WebdebUser user = sessionHelper.getUser(ctx());

    // check form
    Form<DeleteAccountForm> form = formFactory.form(DeleteAccountForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      return CompletableFuture.completedFuture(badRequest(deleteAccountModal.render(id, form)));
    }

    DeleteAccountForm deleteAccountForm = form.get();

    // both ids do not match -> hack page
    if (!user.getId().equals(deleteAccountForm.getContributor())) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // handle request
    Map<String, String> messages = new HashMap<>();
    if(contributorFactory.deleteContributor(deleteAccountForm.getContributor(), deleteAccountForm.getPassword())){
      // all good
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.delete.account.success"));
      return CompletableFuture.completedFuture(ok(Json.toJson(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.logout().url())));
    }


    // all good
    flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.delete.account.fail"));
    return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
  }

  /**
   * Ask modal page to recover lost password
   *
   * @return the modal page to recover a lost password
   */
  public CompletionStage<Result> recoverPassword() {
    logger.debug("GET recover password");
    return CompletableFuture.completedFuture(
        ok(askPassword.render(formFactory.form(MailForm.class).fill(new MailForm()))));
  }

  /**
   * Send a mail with a password reset unique link.
   *
   * @return either a message saying a mail has been sent (or not), or the askPassword modal frame if given email is invalid
   */
  public CompletionStage<Result> sendPasswordRecovery() {
    logger.debug("POST recover password");
    Map<String, String> messages = new HashMap<>();

    // check errors in form
    Form<MailForm> form = formFactory.form(MailForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("error in form " + form.errors());
      return CompletableFuture.completedFuture(badRequest(askPassword.render(form)));
    }

    // try to find a contributor with given email, if none, just say we did it, and send a special mail to given address
    String email = form.get().getEmail();
    Contributor contributor = contributorFactory.retrieveContributor(email);

    try {
      if (contributor == null) {
        logger.warn("received request to reset password for unknown email address " + email);
        mailer.sendMail(new WebdebMail(WebdebMail.EMailType.UNKNOWN_CHANGE_PASSWORD, email,
            be.webdeb.presentation.web.controllers.routes.Application.index().url(), ctx().lang()));
        flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.recover.password.success", email));
        return CompletableFuture.completedFuture(ok(message.render(messages)));
      }

      // contributor is known, create reset token and send mail
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.CHANGE_PASSWORD, contributor.getEmail(),
          routes.ContributorActions.resetPassword(contributor.newConfirmationToken()).url(), ctx().lang()));

    } catch (PersistenceException | MailerException e) {
      logger.error("unable to get a new token", e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.recover.password.nomailsent"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }
    // all good
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.recover.password.success", email));
    return CompletableFuture.completedFuture(ok(message.render(messages)));
  }

  /**
   * Ask modal page to resend the signup mail
   *
   * @return modal to resend signup mail page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.VIEWER))
  public CompletionStage<Result> resendSignupMail() {
      logger.debug("GET recend signup mail");
      return CompletableFuture.completedFuture(
              ok(nomailreceived.render(formFactory.form(MailForm.class).fill(new MailForm()))));
  }

    /**
     * Resend the mail to confirme the subscription to WebDeb
     *
     * @return either a message saying a mail has been sent (or not), or the nomailreceived modal frame if given email is invalid
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.VIEWER))
    public CompletionStage<Result> sendSignupMail() {
        logger.debug("POST recend signup mail");
        Map<String, String> messages = new HashMap<>();

        // check errors in form
        Form<MailForm> form = formFactory.form(MailForm.class).bindFromRequest();
        if (form.hasErrors()) {
            logger.debug("error in form " + form.errors());
            return CompletableFuture.completedFuture(badRequest(askPassword.render(form)));
        }

        // try to find a contributor with given email, if none the nomailreceived modal with a message error
        String email = form.get().getEmail();
        Contributor contributor = contributorFactory.retrieveContributor(email);
        if (contributor == null) {
          logger.warn("unknown contributor " + email);
          return CompletableFuture.completedFuture(badRequest(askPassword.render(form)));
        }
        // User validated yet, just ignore it
        if (contributor.isValidated()) {
            logger.warn("Contributor validated yet " + email);
            return CompletableFuture.completedFuture(ok(""));
        }

        try {
            mailer.sendMail(new WebdebMail(WebdebMail.EMailType.SUBSCRIBE, contributor.getEmail(),
                    routes.ContributorActions.confirm(contributor.newConfirmationToken()).url(), ctx().lang()));
        } catch (PersistenceException | MailerException ex) {
          logger.error(EMAIL_NOTSEND + contributor.getEmail(), ex);
          flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.signup.error.email"));
        }
        // all good
        flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.recover.password.success", email));
        return CompletableFuture.completedFuture(ok(message.render(messages)));
    }

  /**
   * Show the page to reset a password
   *
   * @param token a unique token to reset a password
   * @return resetPassword page, or the hack page (if error)
   */
  public CompletionStage<Result> resetPassword(String token) {
    logger.debug("ask to reset password");
    // get contributor by token
    Contributor contributor = contributorFactory.retrieveContributorByConfirmationToken(token);
    if (contributor == null) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(sessionHelper.getUser(ctx()))));
    }

    if (contributor.hasConfirmationTokenExpired()) {
      logger.warn("token has expired");
      flash(SessionHelper.WARNING, "reset.error.expired.password");
      return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.routes.Application.index()));
    }

    // ok token and requests are valid, show resetPassword page
    return CompletableFuture.completedFuture(
        ok(resetPassword.render(formFactory.form(PasswordForm.class).fill(new PasswordForm()), token, sessionHelper.getUser(ctx()))));
  }

  /**
   * Handle reset password requests
   *
   * @param token new password token
   * @return the resetPassword view page if no error occurred
   */
  public CompletionStage<Result> doResetPassword(String token) {
    logger.debug("POST new password request");

    if (token == null) {
      logger.error("given recover token is null");
      return CompletableFuture.completedFuture(badRequest(hack.render(sessionHelper.getUser(ctx()))));
    }

    // get contributor by token and check if not expired
    Contributor contributor = contributorFactory.retrieveContributorByConfirmationToken(token);
    if (contributor == null) {
      logger.warn("no contributor found for token");
      return CompletableFuture.completedFuture(badRequest(hack.render(sessionHelper.getUser(ctx()))));
    }

    if (contributor.hasConfirmationTokenExpired()) {
      logger.warn("token has expired");
      flash(SessionHelper.WARNING, "reset.error.expired.password");
      return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.routes.Application.index()));
    }

    // check form now
    Form<PasswordForm> form = formFactory.form(PasswordForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      return CompletableFuture.completedFuture(badRequest(resetPassword.render(form, token, sessionHelper.getUser(ctx()))));
    }

    // all ok, reset password
    try {
      contributor.changePassword(form.get().getPassword());
    } catch (PersistenceException e) {
      logger.error("unable to change password for " + contributor.getEmail(), e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.change.password.fail"));
      return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.routes.Application.index()));
    }

    // send confirmation mail
    try {
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.CONFIRM_CHANGE_PASSWORD, contributor.getEmail(),
          be.webdeb.presentation.web.controllers.routes.Application.index().url(), ctx().lang()));
    } catch (MailerException e) {
      logger.warn("unable to send confirmation mail after password reset", e);
    }
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.change.password.success"));
    return CompletableFuture.supplyAsync(() ->
        redirect(be.webdeb.presentation.web.controllers.routes.Application.index()), context.current());
  }

  /**
   * Handle search place by query
   *
   * @param query to excecute
   * @return a possibly empty list of places
   */
  public CompletionStage<Result> searchPlace(String query) {
    logger.debug("Search for place "+query);
    String lang = ctx().lang().code();

    if(query != null && !query.equals("")) {

      return geonames.searchPlace(query, lang).thenApplyAsync(jsonResponse -> {
        List<PlaceNameForm> places  = findPlaces(query, lang);
        Iterator<JsonNode> nodes = jsonResponse.get("geonames").elements();

        Set<String> bannedFcl = new HashSet<>(Arrays.asList(new String[]{"H", "R", "S", "T", "U", "V"}));
        try {
          while (nodes.hasNext()) {
            JsonNode node = nodes.next();
            String fcl = getJsonNodeStringOrNull(node, "fcl");
            String toponymName = getJsonNodeStringOrNull(node, "toponymName");
            Long geonameId = getJsonNodeLong(node, "geonameId");

            if (!toponymName.isEmpty() && !bannedFcl.contains(fcl) && geonameId > -1L && !geonameId.equals(6295630L)) {
              String region = getJsonNodeStringOrNull(node, "adminName1");
              String subregion = getJsonNodeStringOrNull(node, "adminName2");
              String placename = getPlacename(node, lang);
              String postalCode = getPlacename(node, "post");
              String countryName = getJsonNodeStringOrNull(node, "countryName");
              String name = getJsonNodeStringOrNull(node, "name");

              List<String> desc = new ArrayList<>(Arrays.asList(new String[]{postalCode, subregion, region, countryName}));
              desc.removeIf(e -> e.isEmpty());

              String description = (fcl.equals("P") ?
                      " (" +  String.join(", ", desc) + ")"
                      : (!placename.isEmpty() ? " (" + toponymName + ")" : ""));

              places.add(new PlaceNameForm(null, geonameId, lang, (!placename.isEmpty() ? placename : name) + description));
            }
          }
        }catch(Exception e){
          logger.debug(e.toString());
        }
        return ok(Json.toJson(places));
      }).exceptionally(t -> {
        return ok(Json.toJson(findPlaces(query, lang)));
      }).toCompletableFuture();

    }
    return CompletableFuture.completedFuture(ok(Json.toJson("")));
  }

  /**
   * Handle search place in db query
   *
   * @param query to excecute
   * @return a possibly empty list of places
   */
  public CompletionStage<Result> searchExistingPlace(String query) {
    return sendOk(Json.toJson(findPlaces(query, ctx().lang().code())));
  }

  private String getJsonNodeStringOrNull(JsonNode currentNode, String key){
   return currentNode.get(key) != null && currentNode.get(key).isTextual() ? currentNode.get(key).textValue() : "";
  }

  private Long getJsonNodeLong(JsonNode currentNode, String key){
    return currentNode.get(key) != null && currentNode.get(key).isInt() ? currentNode.get(key).asLong() : -1L;
  }

  /**
   * Get the name of a place for a specified language
   *
   * @param placenode the current place node
   * @param lang the lang to get
   * @return the place name in the given lang or null if not found
   */
  private String getPlacename(JsonNode placenode, String lang) {
    if(placenode.get("alternateNames") != null){
      Iterator<JsonNode> iterator = placenode.get("alternateNames").elements();
      while(iterator.hasNext()) {
        JsonNode current = iterator.next();
        if(getJsonNodeStringOrNull(current, "lang").equals(lang)){
          return getJsonNodeStringOrNull(current, "name");
        }
      }
    }
    return "";
  }

  private List<PlaceNameForm> findPlaces(String query, String lang){
    return textFactory.findPlace(query, 0, 5).stream().map(e ->
            new PlaceNameForm(e.getId(), e.getGeonameId(), lang, e.getCompletePlacename(lang))).collect(Collectors.toList());
  }

  /**
   * Handle search place by geoname id
   *
   * @param id the geoname id
   * @return the found place or null
   */
  public CompletionStage<Result> searchPlaceById(long id) {
    logger.debug("Search for place with geoname id : " + id);
    try {
      return geonames.searchPlaceById(id).thenApplyAsync(Results::ok, context.current());
    } catch (Exception e) {
      logger.error("unable to get geoname for " + id, e);
      return CompletableFuture.completedFuture(ok(Json.toJson("")));
    }
  }

  /**
   * The user is warned about old browser danger and want to use WebDeb with
   *
   * @return the main page
   */
  public Result userIsBrowserWarned() {
    logger.debug("POST user browser warned");
    WebdebUser user = sessionHelper.getUser(ctx());

    be.webdeb.presentation.web.controllers.routes.Application.acceptCookies();
    session(SessionHelper.KEY_USERWARNED, "true");

    if(user.getContributor() != null){
      contributorFactory.userIsWarnedAboutBrowser(user.getId());
    }

    return redirect(be.webdeb.presentation.web.controllers.routes.Application.index());
  }

  /**
   * Get the contact us modal frame
   *
   * @return the contact us modal frame
   */
  public CompletionStage<Result> contactus() {
    logger.debug("GET contact us modal");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    ContactForm contactForm = new ContactForm(user.getContributor());

    return CompletableFuture.supplyAsync(() ->
            ok(contactus.render(formFactory.form(ContactForm.class).fill(contactForm), messages)), context.current());
  }

  /**
   * Send the contactus form by mail to administrator
   *
   * @return either the contact form object if it contains error (bad request, 400)
   * if mail can't be send (internal error, 500) or ok (200)
   */
  public CompletionStage<Result> sendContactus() {
    logger.debug("POST send contact form");
    Form<ContactForm> form = formFactory.form(ContactForm.class).bindFromRequest();
    Http.Context ctx = ctx();
    Map<String, String> messages = new HashMap<>();

    if (form.hasErrors()) {
      logger.debug("contact form contains errors " + form.errors());
      flash(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      return CompletableFuture.supplyAsync(() ->
              badRequest(contactus.render(form, messages)), context.current());
    }

    ContactForm contact = form.get();
    try {
      mailer.sendMail(new WebdebMail(contact));
    } catch (Exception e) {
      return CompletableFuture.supplyAsync(() ->
              internalServerError(""), context.current());
    }

    return CompletableFuture.supplyAsync(() ->
            ok(""), context.current());
  }

  /**
   * Get the contact user to user modal frame
   *
   * @param contributor a contributor id
   * @return the contact user to user modal frame
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> userContactUser(Long contributor) {
    logger.debug("GET contact user to user modal");

    Contributor c = contributorFactory.retrieveContributor(contributor);
    WebdebUser user = sessionHelper.getUser(ctx());

    if(c != null) {
      ContactUserToUserForm contactUserToUserForm =
              new ContactUserToUserForm(sessionHelper.getUser(ctx()).getContributor(), new ContributorHolder(c, user, ctx().lang().code()));

      return sendOk(contactUserToUserFormModal.render(formFactory.form(ContactUserToUserForm.class).fill(contactUserToUserForm)));
    }
    return sendBadRequest();
  }

  /**
   * Send the contact user to user form
   *
   * @return either the contact form object if it contains error (bad request, 400)
   * if mail can't be send (internal error, 500) or ok (200)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendUserContactUser() {
    logger.debug("POST send contact user to user form");
    Form<ContactUserToUserForm> form = formFactory.form(ContactUserToUserForm.class).bindFromRequest();
    Http.Context ctx = ctx();

    if (form.hasErrors()) {
      logger.debug("contact user to user form contains errors " + form.errors());
      return CompletableFuture.supplyAsync(() ->
              badRequest(contactUserToUserFormModal.render(form)), context.current());
    }

    ContactUserToUserForm contact = form.get();
    try {
      mailer.sendMail(new WebdebMail(contact));
    } catch (Exception e) {
      return sendInternalServerError();
    }

    return sendOk();
  }

  /**
   * Get all external contributions that the contributor added with a given external service.
   *
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param sourceName the name of the external source
   * @return bad request 400 if the given user auth token is wrong or the list of external contributions added with an
   * external sources in two array : the first is the array of contributions well added and the second for the contributions
   * that wait to be added on Webdeb.
   */
  public CompletionStage<Result> getContributorExternalContributions(int fromIndex, int toIndex, String sourceName){
    logger.debug("[POST] get contributor external contributions from a specified external source");
    Form<AuthForm> form = formFactory.form(AuthForm.class).bindFromRequest();

    if (!form.hasErrors()) {
      AuthForm user = form.get();
      Contributor contributor = user.getContributor();
      if(contributor != null) {
        session(SessionHelper.KEY_USERMAILORPSEUDO, contributor.getEmail());
        sessionHelper.getUser(ctx());

        List<VizExternalContributionResponse> response = new ArrayList<>();
        List<ExternalContribution> contributions = contributorFactory.getContributorExternalContributions(contributor.getId(), fromIndex, toIndex, sourceName);

        for(ExternalContribution contribution : contributions){
            response.add(new VizExternalContributionResponse(contribution, user.getLang()));
        }

        return CompletableFuture.supplyAsync(() ->
                ok(Json.toJson(response).toString()), context.current());
      }
    }
    return CompletableFuture.supplyAsync(() ->
            badRequest(), context.current());
  }

  /**
   * Get edit advices modal page
   *
   * @return the modal page to edit advices or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editAdvices() {
    logger.debug("GET modal page to manage advices");

    // all good, send modal
    return CompletableFuture.supplyAsync(() ->
            ok(advicesModal.render(formFactory.form(AdvicesForm.class)
                    .fill(new AdvicesForm(contributorFactory.getAdvices(), ctx().lang().code())))), context.current());
  }

  /**
   * Post contributors advices
   *
   * @return 404 if form errors, internalError if error in save processing and 200 ok well saved.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> sendAdvices() {
    logger.debug("POST save advices");

    WebdebUser user = sessionHelper.getUser(ctx());
    // check form
    Form<AdvicesForm> form = formFactory.form(AdvicesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("error in form " + form.errors());
      return CompletableFuture.supplyAsync(() -> badRequest(advicesModal.render(form)), context.current());
    }

    // check group id and subscription
    AdvicesForm advicesForm = form.get();
    try {
      advicesForm.save(user.getId());
    } catch (PersistenceException | PermissionException e) {
      logger.debug("Error when save advices form");
      return CompletableFuture.supplyAsync(Results::internalServerError, context.current());
    }


    return CompletableFuture.supplyAsync(Results::ok, context.current());
  }

  /**
   * Post unsubsribe to newsletters for given contributor
   *
   * @param contributor a contributor id
   * @param token a auth token to ensure the identity of the user to unsubscribe him without he need to be auth
   * @return
   */
  public CompletionStage<Result> unsubscribeNewsletters(Long contributor, String token){
    logger.debug("POST unsubsribe to newsletters for given contributor" + contributor);

    Contributor c = contributorFactory.retrieveContributor(contributor);
    if(c != null && !c.hasNewsletterTokenExpired() && token != null && token.equals(c.getNewsletterToken())){
      c.isNewsletter(false);

      try {
        c.save(be.webdeb.core.api.contributor.Group.getGroupPublic());
        flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "contributor.label.newsletter.success"));
      } catch (PersistenceException e) {
        flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "contributor.label.newsletter.error"));
      }
    }else{
      flash(SessionHelper.WARNING, i18n.get(ctx().lang(), "contributor.label.newsletter.token"));
      return update(contributor);
    }

    return redirectToGoTo(null);

  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Save a new contributor
   *
   * @param request the http request
   * @param form the contributor's form
   * @return the contributor if correctly saved
   * @throws ContributorNotSavedException if any error occurred
   */
  private Contributor saveContributor(Http.Request request, Form<ContributorForm> form) throws ContributorNotSavedException {
    // handle file upload
    Http.MultipartFormData<File> body = request.body().asMultipartFormData();
    WebdebUser user = sessionHelper.getUser(ctx());

    Optional<Http.MultipartFormData.FilePart<File>> picture =
        body.getFiles().stream().filter(f -> f != null && f.getFile() != null && f.getFile().length() > 0).findFirst();

    File file = null;
    if (picture.isPresent()) {
      file = picture.get().getFile();
    }

    if (form.hasErrors()) {
      logger.debug("form contains errors");
      form.errors().forEach((k, v) -> logger.debug(k + " " + v.toString()));
      // save picture in temp fs
      if (file != null) {
        files.saveToCache(file, form.data().get("avatar"));
      }
      throw new ContributorNotSavedException(form, ContributorNotSavedException.ERROR_FORM);
    }

    ContributorForm contributorForm = form.get();

    // check for name matches in contributor's affiliations
    NameMatch<ActorHolder> nameMatch = helper.findAffiliationsNameMatches("", contributorForm.getAffiliationsForm(), EActorType.ORGANIZATION, user, ctx().lang().code());
    if (!nameMatch.isEmpty()) {
      throw new ContributorNotSavedException(form.fill(contributorForm), ContributorNotSavedException.NAME_MATCH);
    }

    // check if current user may edit this profile: ok if
    // -- it's a new user
    // -- it's an invited user
    // -- user is this contributor
    // -- user at least owner of a group where this user is a member and group's permissions allow it)
    if (!values.isBlank(contributorForm.getId())) {
      Contributor temp = contributorFactory.retrieveContributor(contributorForm.getId());
      if (temp != null && !values.isBlank(temp.getConfirmationToken())
          && !userMayEditProfile(sessionHelper.getUser(ctx()), contributorForm.getId())) {
        throw new ContributorNotSavedException(form, ContributorNotSavedException.UNAUTHORIZED);
      }
    }

    if(user.getTmpPseudo() != null && !user.getTmpPseudo().equals(contributorForm.getPseudo())){
      logger.debug("Session tmp pseudo not corresponding to form pseudo, should not append");
      throw new ContributorNotSavedException(form, ContributorNotSavedException.ERROR_FORM);
    }
    if(user.getTmpPseudo() != null){
      // If form from tmp contributor, ensure to notice it in the form
      contributorForm.setIsTmp(true);
    }

    // save new contributor
    logger.debug("save contributor " + contributorForm.getEmail() + " named " + contributorForm.getFirstname() + " " + contributorForm.getLastname());

    Contributor contributor;
    try {
      contributor = contributorForm.save();
    } catch (PersistenceException e) {
      logger.error("unable to complete registration of user", e);
      throw new ContributorNotSavedException(form, ContributorNotSavedException.ERROR_DB, e.getMessage());
    }

    // save picture if any
    if (!values.isBlank(contributorForm.getAvatar())) {
      // check if we have a picture sent or if file is in cache
      if (file == null || file.length() == 0) {
        // check in cache
        logger.debug("retrieve from temp cache " + contributorForm.getAvatar());
        file = files.getFromCache(contributorForm.getAvatar());
      }

      if (file != null) {
        String name = contributorForm.getAvatar();
        logger.debug("will save picture file " + name);
        try {
          files.saveUserPictureFile(file, contributor.getId() + name.substring(name.lastIndexOf('.')), false);
        } catch (StringIndexOutOfBoundsException e) {
          logger.error("given filename had no extension, unable to save file " + name, e);
        }
      }
    }
    return contributor;
  }

  private OpenIdUserData checkFacebookAuth(LoginFormOpenId loginForm) {
    OpenIdUserData user = null;

    if(!values.isBlank(loginForm.getPseudo()) && !values.isBlank(loginForm.getId())) {
      String clientId = configuration.getString("openid.facebook.appid");
      String clientSecret = configuration.getString("openid.facebook.secretid");

      try {
        JsonNode appToken = proxy.readJsonFromUrl("https://graph.facebook.com/oauth/access_token?client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials");
        JsonNode userId = proxy.readJsonFromUrl("https://graph.facebook.com/debug_token?input_token=" + loginForm.getToken() + "&access_token=" + appToken.get("access_token").asText());

        if(userId.has("data")) {
          userId = userId.get("data");

          if (userId.has("user_id") && userId.has("is_valid") && userId.get("is_valid").asBoolean()) {
            user = new OpenIdUserData();
            user.setId(userId.get("user_id").asText());
            user.setPseudo(user.getId() + "_fauth");
            user.setLastName(loginForm.getPseudo().contains(" ") ?
                    loginForm.getPseudo().substring(loginForm.getPseudo().indexOf(" ")) :
                    loginForm.getPseudo());
            user.setFirstName(loginForm.getPseudo().contains(" ") ?
                    loginForm.getPseudo().split(" ")[0] :
                    loginForm.getPseudo());
          }
        }

      } catch (Exception e) {

      }
    }

    return user;
  }

  private OpenIdUserData checkGoogleAuth(String token) {
    OpenIdUserData user = null;
    String clientId = configuration.getString("openid.google.appid");

    try {
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jacksonFactory)
              // Specify the CLIENT_ID of the app that accesses the backend:
              .setAudience(Collections.singletonList(clientId))
              // Or, if multiple clients access the backend:
              //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
              .build();

      // (Receive idTokenString by HTTPS POST)

      GoogleIdToken idToken = verifier.verify(token);
      if (idToken != null) {
        Payload payload = idToken.getPayload();
        user = new OpenIdUserData();

        // Get profile information from payload
        user.setId(payload.getSubject());
        user.setEmail(values.isBlank(payload.getEmail()) || !payload.getEmailVerified() ? null : payload.getEmail());
        user.setPseudo(user.getId() + "_gauth");
        user.setLastName((String) payload.get("family_name"));
        user.setFirstName((String) payload.get("given_name"));
        //user.setResidence((String) payload.get("locale")); locale = lang

      } else {
        System.out.println("Invalid ID token.");
      }
    } catch (GeneralSecurityException | IOException e) {

    }

    return user;
  }

  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }


  /**
   * Check whether given user may edit the profile of given contributor
   *
   * @param user a user (may not be null)
   * @param contributor a contributor id
   * @return if given contributor belong to a group where given user is owner and group allows user's profile edition
   */
  private boolean userMayEditProfile(WebdebUser user, Long contributor) {
    if (user.getId().equals(contributor)) {
      return true;
    }
    Contributor c = contributorFactory.retrieveContributor(contributor);
    if (c != null) {
      for (GroupSubscription subscription : c.getGroups()) {
        // if group allows edition of member's details
        if (subscription.getGroup().getPermissions().contains(EPermission.EDIT_MEMBER)
            // and given user is an owner of that group
            && subscription.getGroup().getGroupOwners().stream().anyMatch(s -> s.getContributor().getId().equals(user.getId()))) {
          return true;
        }
      }
    }
    logger.error("user " + user.getId() + " may not edit contributor's profile " + contributor);
    return false;
  }

  /*
   * INNER CLASS
   */

  /**
   * Inner class to keep user data from open id auth
   */
  private class OpenIdUserData {
    private String id;
    private String email;
    private String pseudo;
    private String firstName;
    private String lastName;
    private String residence;

    public OpenIdUserData() {
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPseudo() {
      return pseudo;
    }

    public void setPseudo(String pseudo) {
      this.pseudo = pseudo;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getResidence() {
      return residence;
    }

    public void setResidence(String residence) {
      this.residence = residence;
    }

    @Override
    public String toString() {
      return "OpenIdUserData{" +
              "id='" + id + '\'' +
              ", email='" + email + '\'' +
              ", pseudo='" + pseudo + '\'' +
              ", firstName='" + firstName + '\'' +
              ", lastName='" + lastName + '\'' +
              ", residence='" + residence + '\'' +
              '}';
    }
  }

  /**
   * Inner class to handle exception when a contributor cannot be saved from private save execution
   */
  private class ContributorNotSavedException extends Exception {

    private static final long serialVersionUID = 1L;
    final Form<ContributorForm> form;
    final int error;

    static final int ERROR_FORM = 0;
    static final int NAME_MATCH = 1;
    static final int UNAUTHORIZED = 2;
    static final int ERROR_DB = 3;

    ContributorNotSavedException(Form<ContributorForm> form, int error) {
      this.error = error;
      this.form = form;
    }

    ContributorNotSavedException(Form<ContributorForm> form, int error, String message) {
      super(message);
      this.error = error;
      this.form = form;
    }

  }
}
