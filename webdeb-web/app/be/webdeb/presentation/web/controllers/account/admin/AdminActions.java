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

package be.webdeb.presentation.web.controllers.account.admin;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.Profession;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.text.TextCopyrightfreeSource;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.csv.CsvImporter;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.mail.MailerException;
import be.webdeb.infra.mail.WebdebMail;
import be.webdeb.infra.ws.nlp.RSSFeedSource;
import be.webdeb.infra.ws.nlp.RemoveTwitterAccount;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.infra.ws.nlp.TwitterAccount;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.account.routes;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.account.admin.*;
import be.webdeb.presentation.web.views.html.account.admin.csv.importActorFromCsv;
import be.webdeb.presentation.web.views.html.account.admin.csv.importCsvReports;
import be.webdeb.presentation.web.views.html.account.admin.csv.importFromCsv;
import be.webdeb.presentation.web.views.html.util.message;
import be.webdeb.presentation.web.views.html.util.messagelike;
import play.Configuration;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * All user admin actions like changing roles and searching for contributors with their roles
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class AdminActions extends CommonController {

  @Inject
  private Mailer mailer;

  @Inject
  private RequestProxy proxy;

  @Inject
  private CsvImporter importer;

  @Inject
  private FileSystem files;

  /**
   * Update role of given contributor into given group
   *
   * @param contributor a contributor id
   * @param group the group id where given contributor has a role
   * @param role the role id to assign to given contributor into given group
   * @return a message template with a ok or badrequest status containing the result of the request
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> changeRole(Long contributor, int group, int role) {
    logger.debug("CHANGE role of contributor " + contributor + " to " + role);
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    if (user.getContributor() == null) {
      // no user from context => error
      logger.error("no contributor could be retrieved from context");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    EContributorRole r = EContributorRole.value(role);
    Contributor c = contributorFactory.retrieveContributor(contributor);

    if (c == null || r == null) {
      logger.error("no contributor or no role could be retrieved");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    GroupSubscription g = c.belongsTo(group);
    if (g == null) {
      logger.error("contributor does not belong to group");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());    }

    try {
      if(r.id() == EContributorRole.ADMIN.id()){
        c.giveAdminRights();
      }else{
        if(g.getRole().id() == EContributorRole.ADMIN.id() && r.id() != EContributorRole.ADMIN.id()){
          c.removeAdminRights();
        }
        g.setRole(r);
        g.updateRole(user.getContributor().getId());
      }
    } catch (PermissionException | PersistenceException e) {
      logger.error("unable to save group", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // all good
    /*messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.change.role.ok",
        c.getFirstname() + " " + c.getLastname(), g.getGroup().getGroupName(), i18n.get(ctx().lang(), "role.label." + role)));*/
    return searchContributorsAndRoles(c.getFirstname() + " " + c.getLastname(), "");
  }

  /**
   * Get the contributorRole template filled with all contributor found from given query (in their names or addresses)
   *
   * @param query a term to look for in contributor's details
   * @param sort either 'name' or 'date' to sort the contributors on resp. their names or their registration date
   * @return the contributorRole template filled with the results
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> searchContributorsAndRoles(String query, String sort) {
    List<Contributor> result = contributorFactory.findContributors(query);
    if(sort.equals("date") || sort.equals("name")) {
      result.sort((o1, o2) -> {
        switch (sort) {
          case "date":
            return o2.getSubscriptionDate().compareTo(o1.getSubscriptionDate());
          case "name":
          default:
            return (o1.getLastname() != null ? o1.getLastname() : o1.getEmail())
                    .compareTo(o2.getLastname() != null ? o2.getLastname() : o2.getEmail());
        }
      });
    }
    return CompletableFuture.supplyAsync(() ->
        ok(contributorRole.render(result.stream().map(c ->
            new WebdebUser(c.getDefaultGroup(), contributorFactory, textFactory, ctx().lang().code(), null)).collect(Collectors.toList()))),
        context.current());
  }

  /**
   * Validate a contributor inscription
   *
   * @param contributor id of the contributor
   * @return ok
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> validateContributor(Long contributor) {

    Map<String, String> messages = new HashMap<>();
    Contributor c = contributorFactory.retrieveContributor(contributor);

    if (c == null) {
      logger.error("no contributor could be retrieved");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "error.crash"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    c.isValidated(true);

    try {
      c.save(0);
    } catch (PersistenceException e) {
      logger.error("unable to validate contributor", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "error.crashr"));
      return CompletableFuture.supplyAsync(() -> internalServerError(message.render(messages)), context.current());
    }

    return CompletableFuture.completedFuture(ok(""));
  }

  /**
   * Get the partial page to discpaly csv reports
   *
   * @param type the contribution type that need reports
   * @return a partial page with all previous csv import reports
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> getCsvReports(int type) {
    logger.debug("GET partial page with CSV reports");
    EContributionType eType = EContributionType.value(type);

    if(eType == null) {
      return sendBadRequest();
    }

    return CompletableFuture.completedFuture(ok(importCsvReports.render(eType, files.listCsvFile(eType))));
  }


  /**
   * Get the modal page to upload csv files
   *
   * @param type the contribution type that need reports
   * @return the simple modal page to select one or two files to upload actor/affiliations from csv files
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> importCsvFile(int type) {
    logger.debug("GET modal page to upload CSV files " + type);
    EContributionType eType = EContributionType.value(type);

    if(eType == null) {
      return sendBadRequest();
    }

    return CompletableFuture.completedFuture(ok(importFromCsv.render(eType, sessionHelper.getUser(ctx()))));
  }

  /**
   * Handle submission of csv file(s) with affiliations and actors
   *
   * @return either a link to a generated csv file with the import result, or an error message if submitted
   * files do not look valid
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> uploadCsvFile(int type, String charset, char delimiter, int group) {
    logger.debug("POST upload actor CSV files for type " + type + " group " + group + " with charset " + charset + " and delimiter " + delimiter);

    Map<String, String> messages = new HashMap<>();
    EContributionType eType = EContributionType.value(type);

    if(eType == null) {
      return sendBadRequest();
    }

    if (contributorFactory.retrieveGroup(group) == null) {
      logger.error("invalid group passed " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "admin.csv.group"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }

    if (charset == null) {
      logger.error("no charset selected");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "admin.csv.nocharset"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }

    Http.MultipartFormData<File> body = request().body().asMultipartFormData();

    switch (eType) {
      case ACTOR:
        return uploadActorCsvFile(body, charset, delimiter, group);
      case CITATION:
        return uploadCitationCsvFile(body, charset, delimiter, group);
      default:
        return sendBadRequest();
    }

  }

  private CompletionStage<Result> uploadActorCsvFile(Http.MultipartFormData<File> body, String charset, char delimiter, int group) {
    Map<String, String> messages = new HashMap<>();
    Http.MultipartFormData.FilePart<File> actorCsv = body.getFile("actorFile");
    Http.MultipartFormData.FilePart<File> affiliationCsv = body.getFile("affiliationFile");

    if (actorCsv == null || actorCsv.getFile() == null
            || affiliationCsv == null || affiliationCsv.getFile() == null) {
      logger.error("no file passed");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "admin.csv.empty"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }

    if (actorCsv.getFile().length() <= 0 && affiliationCsv.getFile().length() <= 0) {
      return CompletableFuture.completedFuture(badRequest(i18n.get(ctx().lang(), "admin.csv.empty")));
    }

    // save them to tmpfs (in case of error)
    String folder = EContributionType.ACTOR + "/" + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date());
    new File(configuration.getString("csv.store.path") + folder).mkdirs();
    String suffix = ".csv";
    if (actorCsv.getFilename() != null && !"".equals(actorCsv.getFilename())) {
      files.saveCsvFile(actorCsv.getFile(), folder, configuration.getString("csv.import.actor") + suffix);
    }
    if (affiliationCsv.getFilename() != null && !"".equals(affiliationCsv.getFilename())) {
      files.saveCsvFile(affiliationCsv.getFile(), folder, configuration.getString("csv.import.affiliation") + suffix);
    }
    String result = importer.importCsvActor(actorCsv.getFile(), affiliationCsv.getFile(), folder, charset, delimiter, sessionHelper.getUser(ctx()).getId(), group);
    return CompletableFuture.completedFuture(
            ok(i18n.get(ctx().lang(), (result == null ? "admin.csv.noresult" : "admin.csv.result"),
                    be.webdeb.presentation.web.controllers.routes.Application.getFile(folder + "/" + result, "tmp").url())));
  }

  private CompletionStage<Result> uploadCitationCsvFile(Http.MultipartFormData<File> body, String charset, char delimiter, int group) {
    Map<String, String> messages = new HashMap<>();
    Http.MultipartFormData.FilePart<File> citationCsv = body.getFile("citationFile");
    Http.MultipartFormData.FilePart<File> authorCsv = body.getFile("authorFile");

    if (citationCsv == null || citationCsv.getFile() == null
            || authorCsv == null || authorCsv.getFile() == null) {
      logger.error("no file passed");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "admin.csv.empty"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }

    if (citationCsv.getFile().length() <= 0 && authorCsv.getFile().length() <= 0) {
      return CompletableFuture.completedFuture(badRequest(i18n.get(ctx().lang(), "admin.csv.empty")));
    }

    // save them to tmpfs (in case of error)
    String folder = EContributionType.CITATION + "/" + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date());
    new File(configuration.getString("csv.store.path") + folder).mkdirs();
    String suffix = ".csv";
    if (citationCsv.getFilename() != null && !"".equals(citationCsv.getFilename())) {
      files.saveCsvFile(citationCsv.getFile(), folder, configuration.getString("csv.import.citation") + suffix);
    }
    if (authorCsv.getFilename() != null && !"".equals(authorCsv.getFilename())) {
      files.saveCsvFile(authorCsv.getFile(), folder, configuration.getString("csv.import.author") + suffix);
    }
    String result = importer.importCsvCitation(citationCsv.getFile(), authorCsv.getFile(), folder, charset, delimiter, sessionHelper.getUser(ctx()).getId(), group);
    return CompletableFuture.completedFuture(
            ok(i18n.get(ctx().lang(), (result == null ? "admin.csv.noresult" : "admin.csv.result"),
                    be.webdeb.presentation.web.controllers.routes.Application.getFile(folder + "/" + result, "tmp").url())));
  }

  /**
   * Get the partial page that display all RSS feeders and allow modification/deletion or add new ones
   *
   * @return a partial html page with the list of rss feeders or displaying an error message if the list could not be
   * retrieved from external WDTAL service (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> getRssFeeders() {
    Lang lang = ctx().lang();
    return getRssFeedersForm(lang.code()).thenApplyAsync(feeds -> {
      Map<String, String> messages = new HashMap<>();
      if (feeds.isEmpty()) {
        messages.put(SessionHelper.WARNING, i18n.get(lang, "admin.rss.nolist"));
      }
      return ok(adminRss.render(feeds, messages));
    }, context.current());
  }

  /**
   * Get the modal page to edit given rss feeder
   *
   * @param id a feeder id (-1 to create a new one)
   * @return the modal page to edit / add a rss feed source,
   * or an message-like template in case of error with WDTAL service (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editRssFeed(int id) {
    logger.debug("GET edit RSS feed of " + id);
    if (id == -1L) {
      return CompletableFuture.completedFuture(
          ok(editRssFeed.render(formFactory.form(RSSFeedForm.class).fill(new RSSFeedForm()), helper, null)));
    }

    Lang lang = ctx().lang();
    // call rss service to get its details
    return getRssFeedersForm(null).thenApplyAsync(sources -> {
      Optional<RSSFeedForm> source = sources.stream().filter(s -> s.getId() == id).findFirst();
      if (source.isPresent()) {
        return ok(editRssFeed.render(formFactory.form(RSSFeedForm.class).fill(source.get()), helper, null));
      }

      // could not find account or could not call twitter service
      Map<String, String> messages = new HashMap<>();
      messages.put(SessionHelper.ERROR, i18n.get(lang, "admin.rss.notfound"));
      return internalServerError(messagelike.render(messages));

    }, context.current());
  }

  /**
   * Save given rss feed by sending a request to external WDTAL RSS service
   *
   * @return either the rss form object if it contains error (bad request, 400),
   * or if the form looks ok, the panel with the list of feeders
   * or a message-like template in case of WDTAL error when saving given feed (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> saveRssFeed() {
    logger.debug("POST save rss");
    Form<RSSFeedForm> form = formFactory.form(RSSFeedForm.class).bindFromRequest();
    Http.Context ctx = ctx();
    Map<String, String> messages = new HashMap<>();

    if (form.hasErrors()) {
      logger.debug("rss form contains errors " + form.errors());
      messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      return CompletableFuture.supplyAsync(() ->
          badRequest(editRssFeed.render(form, helper, messages)), context.current());
    }

    RSSFeedForm rss = form.get();

    // create RSSFeedSource object from form (brute casting from string will not cause problems
    // because form validation succeeded)
    RSSFeedSource source = rss.getId() != -1 ?
        new RSSFeedSource(rss.getId(), rss.getName(), Integer.valueOf(rss.getType()), rss.getCategory(),
            rss.getSubcategory(), rss.getCountry(), rss.getUrl(), Integer.valueOf(rss.getVisibility()))
        : new RSSFeedSource(rss.getName(), Integer.valueOf(rss.getType()), rss.getCategory(),
        rss.getSubcategory(), rss.getCountry(), rss.getUrl(), Integer.valueOf(rss.getVisibility()));

    // get all sources and check if given url does not exist already
    return getRssFeedersForm(null).thenComposeAsync(sources ->
        CompletableFuture.completedFuture(sources.stream().anyMatch(s -> s.getId() != rss.getId() && s.getUrl().equals(rss.getUrl()))), context.current()

    // now add rss if not yet existing, otherwise sends back form with error message
    ).thenComposeAsync(existing -> {
      if (!existing) {
        // chain addition of rss feed and update of the list or return server error if WDTAL service crashed
        return proxy.addRssFeed(source).thenComposeAsync(r -> {
          if (r != null) {
            logger.debug("result from WDTAL save RSS " + r);
            messages.put(SessionHelper.SUCCESS, i18n.get(ctx.lang(), "admin.rss.save.success"));
            return getRssFeedersForm(ctx.lang().code()).thenApply(sources -> ok(adminRss.render(sources, messages)));
          }
          messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "admin.rss.save.error"));
          return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
        }, context.current());
      }

      // url is a duplicate -> return form
      messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      form.reject(new ValidationError("url", "admin.rss.duplicate.url"));
      logger.debug("rss form contains errors " + form.errors());
      return CompletableFuture.completedFuture(badRequest(editRssFeed.render(form, helper, messages)));

    }, context.current());
  }


  /**
   * Update activated / deactivated status of given source by sending a request to external WDTAL service
   *
   * @param source a source id
   * @param activate true if given source must be activated for feeding, false otherwise
   * @return either the rss feeders panels or a message-like template in case of error (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> activateRssFeed(int source, boolean activate) {
    logger.debug("GET update status of Rss source " + source + " to activated " + activate);
    Http.Context ctx = ctx();
    return proxy.updateStatusOfRssFeed(new RSSFeedSource(source, activate ? RSSFeedSource.EStatus.ACCEPT : RSSFeedSource.EStatus.IGNORE)).thenComposeAsync(r -> {
      Map<String, String> messages = new HashMap<>();
      if (r != null) {
        logger.debug("result from WDTAL activate RSS " + r);
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx.lang(), "admin.rss.activate." + activate + ".success"));
        return getRssFeedersForm(ctx.lang().code()).thenApply(sources -> ok(adminRss.render(sources, messages)));
      } else {
        messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "admin.rss.activate." + activate + ".error"));
        return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
      }
    }, context.current());
  }

  /**
   * Remove given RSS source by sending a request to external WDTAL service
   *
   * @param source a source id
   * @return either the rss feeders panel or a message-like template in case of error (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> removeRssFeed(int source) {
    logger.debug("GET remove RSS source " + source);
    Http.Context ctx = ctx();
    return proxy.removeRssFeed(new RSSFeedSource(source)).thenComposeAsync(r -> {
      Map<String, String> messages = new HashMap<>();
      if (r != null) {
        logger.debug("result from WDTAL remove RSS " + r);
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx.lang(), "admin.rss.remove.success"));
        return getRssFeedersForm(ctx.lang().code()).thenApply(sources -> ok(adminRss.render(sources, messages)));
      } else {
        messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "admin.rss.remove.error"));
        return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
      }
    }, context.current());
  }

  /**
   * Get the partial page that display all twitter accounts and allow modification/deletion or add new ones
   *
   * @return a partial html page with the list of twitter accounts or displaying an error message if the list could
   * not be retrieved from external WDTAL service (400 response)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> getTwitterAccounts() {
    Lang lang = ctx().lang();
    return getTwitterAccountsForm(true).thenApplyAsync(accounts -> {
      Map<String, String> messages = new HashMap<>();
      if (accounts.isEmpty()) {
        messages.put(SessionHelper.WARNING, i18n.get(lang, "admin.twitter.nolist"));
      }
      return ok(adminTwitter.render(accounts, messages));
    }, context.current());
  }

  /**
   * Get the modal page to edit current twitter account (ATM, only creation is possible)
   *
   * @param id an actor id (-1 to create a new one)
   * @return the modal page to edit / add a twitter account, or a 500 response with an error message template
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editTwitterAccount(Long id) {
    logger.debug("GET edit Twitter account of " + id);
    if (id == null || id == -1L) {
      return CompletableFuture.completedFuture(
          ok(editTwitterAccount.render(formFactory.form(TwitterAccountForm.class).fill(new TwitterAccountForm()), helper, null)));
    }

    Lang lang = ctx().lang();
    // call twitter service to get its details
    return getTwitterAccountsForm(false).thenApplyAsync(accounts -> {
      Optional<TwitterAccountForm> account = accounts.stream().filter(a -> id.equals(a.getId())).findFirst();
      if (account.isPresent()) {
        return ok(editTwitterAccount.render(formFactory.form(TwitterAccountForm.class).fill(account.get()), helper, null));
      }
      // could not find account or could not call twitter service
      Map<String, String> messages = new HashMap<>();
      messages.put(SessionHelper.ERROR, i18n.get(lang, "admin.twitter.notfound"));
      return badRequest(messagelike.render(messages));

    }, context.current());
  }

  /**
   * Save / update a Twitter account by sending a request to external WDTAL service (no update possible ATM)
   *
   * @return either the twitter form object if it contains error (bad request, 400),
   * or if the form looks ok, the panel with the list of accounts
   * or a message-like template in case of WDTAL error when saving given account (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> saveTwitterAccount() {
    logger.debug("POST save twitter account");
    Form<TwitterAccountForm> form = formFactory.form(TwitterAccountForm.class).bindFromRequest();
    Http.Context ctx = ctx();
    Map<String, String> messages = new HashMap<>();

    if (form.hasErrors()) {
      logger.debug("twitter form contains errors " + form.errors());
      messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      return CompletableFuture.supplyAsync(() ->
          badRequest(editTwitterAccount.render(form, helper, messages)), context.current());
    }

    // create TwitterAccount object from form (brute casting from string will not cause problems
    // because form validation succeeded)
    TwitterAccountForm accountForm = form.get();
    // check whether id and names correspond
    Actor actor = actorFactory.retrieve(accountForm.getId());
    if (actor == null || !accountForm.getFullname().equals(actor.getFullname(ctx.lang().code()))) {
      form.reject(new ValidationError("fullname", "admin.twitter.error.idfullname"));
      logger.debug("twitter form contains errors " + form.errors());
      messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      return CompletableFuture.supplyAsync(() ->
          badRequest(editTwitterAccount.render(form, helper, messages)), context.current());
    }


    TwitterAccount account = new TwitterAccount(accountForm.getId(), accountForm.getAccount(), accountForm.getFullname(),
        accountForm.getGender().toLowerCase(), accountForm.getLanguages());

    // first check if this account does not yet exist
    return getTwitterAccountsForm(true).thenComposeAsync(accounts ->
      CompletableFuture.completedFuture(accounts.stream().anyMatch(a -> a.getAccount().equals(account.getAccount()))), context.current()

    // now add account if not yet existing, otherwise sends back form with error message
    ).thenComposeAsync(existing -> {
      if (!existing) {
        return addTwitterAccount(account, messages);
      }else{
        // NLP has just add / remove API, for update we need to remove the account before adding the updated one
        return proxy.removeTwitterAccount(new RemoveTwitterAccount(account.getAccount())).thenComposeAsync(r -> {
          if (r != null) {
            return addTwitterAccount(account, messages);
          }
          messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.twitter.save.error"));
          return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
        }, context.current());
      }
    }, context.current());
  }

  /**
   * Save / update a Twitter account by sending a request to external WDTAL service (no update possible ATM)
   *
   * @param account the Twitter account to add
   * @param messages the message map
   * @return either the twitter form object if it contains error (bad request, 400),
   * or if the form looks ok, the panel with the list of accounts
   * or a message-like template in case of WDTAL error when saving given account (code 500)
   */
  public CompletionStage<Result> addTwitterAccount(TwitterAccount account, Map<String, String> messages){
    // chain addition of twitter account and update of the list or return server error if WDTAL service crashed
    return proxy.addTwitterAccount(account).thenComposeAsync(r -> {
      if (r != null) {
        logger.debug("result from WDTAL save twitter " + r);
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.twitter.save.success"));
        return getTwitterAccountsForm(true).thenApply(l -> ok(adminTwitter.render(l, messages)));
      }

      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.twitter.save.error"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));

    }, context.current());
  }


  /**
   * Remove given Twitter account by sending a request to external WDTAL service
   *
   * @param account a twitter account name
   * @return either the twitter accounts panel or a message-like template in case of error (code 500)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> removeTwitterAccount(String account) {
    logger.debug("GET remove twitter account " + account);
    return proxy.removeTwitterAccount(new RemoveTwitterAccount(account)).thenComposeAsync(r -> {
      Map<String, String> messages = new HashMap<>();
      if (r != null) {
        logger.debug("result from WDTAL remove twitter " + r);
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.twitter.remove.success"));
        return getTwitterAccountsForm(true).thenApply(l -> ok(adminTwitter.render(l, messages)));
      } else {
        messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.twitter.remove.error"));
        return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
      }
    }, context.current());
  }

  /**
   * Get the page to edit free copyright sources for texts
   *
   * @return page to edit free copyright sources for texts
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editFreeCopyrightSources() {
    logger.debug("GET edit free sources");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    List<TextCopyrightfreeSource> sources = textFactory.getAllCopyrightfreeSources();
    return CompletableFuture.completedFuture(
        ok(adminFreeCopyrightSources.render(
            formFactory.form(TextCopyrightfreeSourcesForm.class).fill(new TextCopyrightfreeSourcesForm(sources)), user, messages)));
  }

  /**
   * Save / update free copyright sources for texts
   *
   * @return either the free copyright sources form object if it contains error (bad request, 400),
   * or if it is ok, the up-to-date form of free copyright sources without errors
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> saveFreeCopyrightSources() {
    logger.debug("POST save free sources");
    Form<TextCopyrightfreeSourcesForm> form = formFactory.form(TextCopyrightfreeSourcesForm.class).bindFromRequest();
    Http.Context ctx = ctx();
    WebdebUser user = sessionHelper.getUser(ctx);
    Map<String, String> messages = new HashMap<>();

    if (form.hasErrors()) {
      logger.debug("free sources form contains errors " + form.errors());
      messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "error.form"));
      return CompletableFuture.supplyAsync(() ->
          badRequest(adminFreeCopyrightSources.render(form, user, messages)), context.current());
    }

    TextCopyrightfreeSourcesForm sourcesForm = form.get();
    for(TextCopyrightfreeSourceForm sourceForm : sourcesForm.getFreeSources()){
      if((sourceForm.getSourceId() == null || sourceForm.getSourceId() == -1)
          && !textFactory.sourceIsCopyrightfree(sourceForm.getDomainName())) {
        try {
          TextCopyrightfreeSource source = textFactory.getTextCopyrightfreeSource();
          source.setId(sourceForm.getSourceId());
          source.setDomainName(sourceForm.getDomainName());
          int sourceId = textFactory.saveTextCopyrightfreeSource(source);
          sourceForm.setSourceId(sourceId);
        } catch (PersistenceException e) {
          flash(SessionHelper.ERROR, i18n.get(ctx().lang(), e.getMessage()));
          return CompletableFuture.completedFuture(
              ok(adminFreeCopyrightSources.render(
                  formFactory.form(TextCopyrightfreeSourcesForm.class).fill(sourcesForm), user, messages)));
        }
      }
    }

    flash("success", i18n.get(ctx().lang(),"admin.freeSource.success"));
    return CompletableFuture.completedFuture(
        ok(adminFreeCopyrightSources.render(
            formFactory.form(TextCopyrightfreeSourcesForm.class).fill(sourcesForm), user, messages)));
  }

  /**
   * Remove given TextFreeCopyrightSources
   *
   * @param idSource the id of the free source to delete
   * @return either the free copyright sources form object if it contains error (bad request, 400),
   * or if it is ok, the up-to-date form of free copyright sources without errors
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> removeFreeCopyrightSource(int idSource) {
    logger.debug("GET remove free source " + idSource);
    try{
      textFactory.removeTextCopyrightfreeSource(idSource);
    }catch (PersistenceException e) {
      return CompletableFuture.completedFuture(ok(e.getMessage()));
    }
    return CompletableFuture.completedFuture(ok(i18n.get(ctx().lang(),"admin.freeSource.delete.success.")));
  }


 /**
   * Get the modal page for editing profession link
   *
   * @param
   * @return the modal page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editProfessionHasLink(int professionId) {
    Map<String, String> messages = new HashMap<>();
    Lang lang = ctx().lang();

    try {
      Profession profession = retrieveProfessionAndDetermineSubLinks(professionId);
      ProfessionForm professionF = new ProfessionForm(profession, ctx().lang().code());
      // all good, send modal
      return CompletableFuture.supplyAsync(() ->
              ok(editProfessionHasLink.render(professionF, formFactory.form(ProfessionHasLinkForm.class).fill(new ProfessionHasLinkForm(profession, lang.code())), messages)), context.current());
    } catch (FormatException e) {
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.profession.error.notFound"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /**
   * Handle post request to edit profession link
   *
   * @return the edit modal if submitted form contains error or the adminProfession use tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> sendProfessionHasLink(int professionId) {
    logger.debug("SEND the editing of professions links");
    Map<String, String> messages = new HashMap<>();

    try {
      Profession profession = actorFactory.getProfession(professionId);
      ProfessionForm professionF = new ProfessionForm(profession, ctx().lang().code());

      // check form
      Form<ProfessionHasLinkForm> professionForm = formFactory.form(ProfessionHasLinkForm.class).bindFromRequest();
      if (professionForm.hasErrors()) {
        logger.debug("error in form " + professionForm.errors());
        messages.put(SessionHelper.ERROR, SessionHelper.ERROR_FORM);
        return CompletableFuture.supplyAsync(() -> badRequest(editProfessionHasLink.render(professionF, professionForm, messages)), context.current());
      }

      // save profession's links
      ProfessionHasLinkForm editForm = professionForm.get();

      profession.setLinks(new HashMap<>());
      // add links to profession to save
      for (ProfessionForm link : editForm.getProfessionHasLink()) {
        Profession professionLink = actorFactory.getProfession(link.getId());
        profession.addLink(professionLink);
      }
      // save profession
      actorFactory.saveProfession(profession);

      // check if we do not have messages (meaning all went smoothly)
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.edit.profession.ok", ""));
      return CompletableFuture.supplyAsync(() ->
              ok(adminProfession.render()), context.current());
    } catch (FormatException e) {
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.profession.error.notFound"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /**
   * Get the modal page for merge a given profession with another one
   *
   * @param professionId
   * @return the modal page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
   public CompletionStage<Result> mergeProfessions(int professionId) {
    logger.debug("GET the modal page for merge a given profession with another one");
    Map<String, String> messages = new HashMap<>();

    try {
      Profession profession = actorFactory.getProfession(professionId);
      ProfessionForm professionForm = new ProfessionForm(profession, ctx().lang().code());
      // all good, send modal
      return CompletableFuture.supplyAsync(() ->
              ok(mergeProfessions.render(professionForm, formFactory.form(ProfessionMergeForm.class).fill(new ProfessionMergeForm(profession)), messages)), context.current());
    } catch (FormatException e) {
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.profession.error.notFound"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /**
   * Handle post request to merging of professions
   *
   * @return the merge modal if submitted form contains error or the adminProfession tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> sendMergeProfessions(Integer professionId) {
    logger.debug("SEND the merging of professions");
    Map<String, String> messages = new HashMap<>();

    try {
      Profession profession = actorFactory.getProfession(professionId);
      ProfessionForm professionF = new ProfessionForm(profession, ctx().lang().code());

      // check form
      Form<ProfessionMergeForm> professionForm = formFactory.form(ProfessionMergeForm.class).bindFromRequest();
      if (professionForm.hasErrors()) {
        logger.debug("error in form " + professionForm.errors());
        messages.put(SessionHelper.ERROR, SessionHelper.ERROR_FORM);
        return CompletableFuture.supplyAsync(() -> badRequest(mergeProfessions.render(professionF, professionForm, messages)), context.current());
      }

      // merge professions
      int professionIdToMerge = professionForm.get().getProfessionTomergewith().getId();
      actorFactory.mergeProfessions(professionId, professionIdToMerge);

      // check if we do not have messages (meaning all went smoothly)
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.merge.professions.ok", ""));
      return CompletableFuture.supplyAsync(() ->
          ok(adminProfession.render()), context.current());
    } catch (PersistenceException | FormatException e) {
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "persistence.error.merge.professions", ""));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /**
   * Get the page to edit argument dictionary
   *
   * @return page to edit argument dictionary
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> editArgumentsDictionary() {
    logger.debug("GET edit argument dictionary");
    WebdebUser user = sessionHelper.getUser(ctx());

    return CompletableFuture.completedFuture(
            ok(adminArguments.render(user)));
  }

  /**
   * Get the page to edit argument dictionary
   *
   * @return page to edit argument dictionary
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> getAdminEmailForm() {
    logger.debug("GET admin email form");
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    return CompletableFuture.supplyAsync(() ->
            ok(adminMail.render(formFactory.form(AdminMailForm.class).fill(new AdminMailForm()), helper, user, messages)), context.current());
  }

  /**
   * Get the page to edit argument dictionary
   *
   * @return page to edit argument dictionary
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> sendAdminEmailForm() {
    logger.debug("POST admin email form");
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    // check form
    Form<AdminMailForm> adminMailForm = formFactory.form(AdminMailForm.class).bindFromRequest();
    if (adminMailForm.hasErrors()) {
      logger.debug("error in form " + adminMailForm.errors());
      messages.put(SessionHelper.ERROR, SessionHelper.ERROR_FORM);
      return CompletableFuture.supplyAsync(() -> badRequest(adminMail.render(adminMailForm, helper, user, messages)), context.current());
    }

    AdminMailForm form = adminMailForm.get();
    Set<Contributor> contributorsMail = form.getUserrole() == EContributorRole.MYSLEF ? new HashSet<>() :
            contributorFactory.findContributorsByRole(form.getUserrole())
            .stream().filter(e -> (e.isNewsletter() && form.getIsNewsletter()) || !form.getIsNewsletter()).collect(Collectors.toSet());
    contributorsMail.add(user.getContributor().getContributor());

    // try to send mail to contributors
    int nbErrors = 0;
    for(Contributor recipient : contributorsMail) {
      try {
        mailer.sendMail(new WebdebMail(form, recipient, ctx().lang()));
      } catch (MailerException ex) {
        nbErrors++;
      }
    }

    if(nbErrors == contributorsMail.size()){
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "admin.mail.general.error"));
    }else{
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.mail.general.success"));
    }

    return redirectToGoTo(null);
  }

  /**
   * Create fake citations
   *
   * @return ok when its done
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> createFakeCitations(int nbCitations) {
    logger.debug("Create fake citations : " + nbCitations);

    //actorFactory.createFakeCitations(nbCitations);

    logger.debug("Citations has been created !");

    return sendOk();
  }

  /**
   * Create fake citation positions
   *
   * @return ok when its done
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> createFakePositions(int nbDebates, int nbPositionsPerDebate) {
    logger.debug("Create fake positions : " + nbDebates + "  debates to create, " + nbPositionsPerDebate + " positions per debate.");

    //actorFactory.createFakePositions(nbDebates, nbPositionsPerDebate);

    logger.debug("Positions has been created !");

    return sendOk();
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Call external WDTAL Rss service to get the list of feeders and map them to RSSFeedForm
   *
   * @param lang the current lang is-639-1 code, null if the form must be created with ids instead of lang-dependant values
   * @return a list of rss feed form (may be empty if service does not respond)
   */
  private CompletionStage<List<RSSFeedForm>> getRssFeedersForm(String lang) {
    try {
      return proxy.listRssFeeders().thenApply(r -> {
        if (r != null) {
          return Json.fromJson(r, RSSFeedSource[].class);
        }
        return new RSSFeedSource[0];
      }).thenApplyAsync(sources -> {
        // map feeds to forms and sort them alphabetically on name and category
        List<RSSFeedForm> result = Arrays.stream(sources).map(s -> lang != null ?
            new RSSFeedForm(s, lang) : new RSSFeedForm(s)).collect(Collectors.toList());
        result.sort((r1, r2) -> {
          int compare = r1.getName().compareTo(r2.getName());
          if (compare == 0) {
            compare = r1.getCategory().compareTo(r2.getCategory());
          }
          return compare;
        });
        return result;
      }, context.current());

    } catch (Exception e) {
      logger.error("unable to retrieve RSS feeders list", e);
      return CompletableFuture.completedFuture(new ArrayList<>());
    }
  }

  /**
   * Call external WDTAL twitter service to get the list of accounts and map them to TwitterAccountForms
   *
   * @param toDisplayed true if the form is to displayed (not for edition)
   * @return a list of twitter account form (may be empty if service does not respond)
   */
  private CompletionStage<List<TwitterAccountForm>> getTwitterAccountsForm(boolean toDisplayed) {
    Lang lang = ctx().lang();
    try {
      return proxy.listTwitterAccounts().thenApply(r -> {
        if (r != null) {
          return Json.fromJson(r, TwitterAccount[].class);
        }
        return new TwitterAccount[0];
      }).thenApplyAsync(accounts -> {
        // map accounts to twitter forms and sort them alphabetically
        List<TwitterAccountForm> result = Arrays.stream(accounts).map(a -> (toDisplayed ? new TwitterAccountForm(a, lang.code()) : new TwitterAccountForm(a))).collect(Collectors.toList());
        result.sort(Comparator.comparing(TwitterAccountForm::getFullname));
        return result;
      }, context.current());
    } catch (Exception e) {
      logger.error("unable to retrieve twiiter accounts list", e);
      return CompletableFuture.completedFuture(new ArrayList<>());
    }
  }

  /**
   * Retrieve profession with a given id and try to found all sub-links if the given profession hasn't any one
   *
   * @param professionId the id of a profession
   * @return a list of profession sub-links of the given profession, may be empty
   */
  private Profession retrieveProfessionAndDetermineSubLinks(int professionId) throws FormatException{
    Profession profession = actorFactory.getProfession(professionId);
    if(profession != null && (profession.getLinks() == null || profession.getLinks().isEmpty())) {
      profession.setLinks(actorFactory.determineSubLinks(professionId).stream().collect(Collectors.toMap(Profession::getId, p -> p)));
    }
    return profession;
  }
}
