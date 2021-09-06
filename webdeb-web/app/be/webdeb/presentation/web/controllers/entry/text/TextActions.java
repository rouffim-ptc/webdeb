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

package be.webdeb.presentation.web.controllers.entry.text;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.application.nlphelper.WDTALAnnotator;
import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.ws.external.ExternalForm;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.entry.NameMatch;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;
import be.webdeb.presentation.web.controllers.viz.text.ETextVizPane;
import be.webdeb.presentation.web.controllers.viz.text.TextVizHolder;
import be.webdeb.presentation.web.views.html.entry.text.editText;
import be.webdeb.presentation.web.views.html.entry.text.editTextModal;
import be.webdeb.presentation.web.views.html.entry.text.editTextFields;
import be.webdeb.presentation.web.views.html.util.handleNameMatches;
import be.webdeb.presentation.web.views.html.util.message;

import be.webdeb.presentation.web.views.html.viz.text.textCitations;
import be.webdeb.presentation.web.views.html.viz.text.textArgumentsStructure;
import be.webdeb.presentation.web.views.html.viz.common.contributionArgumentsDragnDrop;
import be.webdeb.presentation.web.views.html.viz.text.util.displayTextContentModal;
import be.webdeb.presentation.web.views.html.viz.citation.citationContainerList;

import com.google.gson.Gson;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import play.cache.CacheApi;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * Handle submissions from web forms for Text-related actions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
// TODO create new controller ContextActions and put all methods for context contributions presents here
public class TextActions extends CommonController {

  @Inject
  protected RequestProxy proxy;

  @Inject
  private WDTALAnnotator annotator;

  @Inject
  private CacheApi cache;

  private TextVizHolder viz;

  private static final String NOTFOUND = "text.args.notfound";

  /**
   * Get the text edition page for given text id
   *
   * @param id a text id (-1 or null for a new text)
   * @return the text form to either add a new text or modify an existing one
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> edit(Long id) {
    logger.debug("GET edit text page " + id);
    return editText(id, false);
  }

  /**
   * Get the text edition modal for given text id
   *
   * @param id a text id (-1 or null for a new text)
   * @return the text form in a modal to either add a new text or modify an existing one
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editFromModal(Long id) {
    logger.debug("GET edit text from modal " + id);
    return editText(id, true);
  }

  /**
   * Update or save a text into the database.
   *
   * @param id the text id (-1 of null for a new text)
   * @return either the addText page if form contains error, if it appears to already exist or if author(s) seems to be
   * known and must be disambiguated (400 response), its visualisation page if the action succeeded, or
   * redirect to main entry page if an error occurred.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public synchronized CompletionStage<Result> save(Long id) {
    logger.debug("POST save properties of " + id);

    Form<TextForm> form = formFactory.form(TextForm.class).bindFromRequest();

    try {
      TextForm result = saveText(request(), form);

      flash("success", i18n.get(ctx().lang(),"entry.text.success"));
      return sendRedirectTo(be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(result.getId(), -1, 0).toString());

    } catch (TextActions.TextNotSavedException e) {
      return handleTextError(e,false);
    }
  }

  /**
   * Update or create a text from a modal
   *
   * @param textId the text id (-1 if new one)
   * @return the text form object if it contains errors, or the context contribution page if this new text
   * has been created to be linked to another one, or in case of success or DB crash, redirect to "goto" session key url.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveFromModal(Long textId) {
    logger.debug("POST save properties of " + textId);

    Form<TextForm> form = formFactory.form(TextForm.class).bindFromRequest();

    try {
      TextForm result = saveText(request(), form);

      flash("success", i18n.get(ctx().lang(),"entry.text.success"));
      return sendOk(be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(result.getId(), -1, 0).url());

    } catch (TextActions.TextNotSavedException e) {
      return handleTextError(e,true);
    }
  }

  /**
   * Reload a text with an id passed in the POST content
   *
   * @return redirect to the "work with text" page with the id passed in the "id" body text
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> reloadText() {
    Map<String, String[]> map = request().body().asFormUrlEncoded();
    long id = -1L;
    try {
      id = Long.parseLong(map.get("id")[0]);
      logger.debug("POST reload text " + id);
    } catch (NullPointerException | NumberFormatException e) {
      logger.error("POST reload text: unable to parse text id " + String.join(", ", map.get("id")), e);
      flash(SessionHelper.WARNING, i18n.get(ctx().lang(), NOTFOUND));
    }
    return sendOk();
  }

  /**
   * Search a text by its title or authors
   *
   * @param term a search term (from title)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a List<TextName> as a json structure with all texts containing the given term in their title
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public Result searchText(String term, int fromIndex, int toIndex) {
    List<TextName> result = new ArrayList<>();
    List<Map.Entry<EQueryKey, String>> query = new ArrayList<>();
    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.TEXT.id())));
    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.AUTHOR, term));
    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_TITLE, term));
    try {
      executor.searchContributions(query, fromIndex, toIndex).stream().filter(sessionHelper.getUser(ctx())::mayView).forEach(c -> {
        Text text = (Text) c;
        List<ActorSimpleForm> authors = text.getAuthors().stream().map(r ->
                new ActorSimpleForm(r, ctx().lang().code())).collect(Collectors.toList());
        result.add(new TextName(text.getId(), text.getTitle(ctx().lang().code()), authors));
      });
    } catch (BadQueryException e) {
      logger.warn("unable to search for texts with given term " + term, e);
    }
    return ok(Json.toJson(result));
  }

  /**
   * Retrieve citations from given text limited by index and with filters
   *
   * @param text a text id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return a List<TextName> as a json structure with all texts containing the given term in their title
   */
  public CompletionStage<Result> findTextCitations(Long text, int fromIndex, int toIndex, String filters) {
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    Text textContribution = textFactory.retrieve(text);

    if(textContribution != null) {
      List<CitationHolder> holders = helper.toCitationsHolders(textContribution.getTextCitations(new SearchContainer(text, filters, fromIndex, toIndex)), user, lang, false);

      return sendOk(citationContainerList
                .render(holders, user, true, false, false, null, -1, null));
    }

    return sendOk();
  }

  /**
   * Get all source titles with given term
   *
   * @param term a term to search in text's source_title
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of TextSourceNames that contain the given searched term
   */
  public Result searchTextSource(String term, int fromIndex, int toIndex) {
    logger.debug("search sources for " + term);
    return ok(Json.toJson(textFactory.findSourceNames(term, fromIndex, toIndex)));
  }

  /**
   * Get the annotation of a given text, if context group allows it, otherwise, simply send back the content
   *
   * @param textId the text id to get the annotated version
   * @param highlighted true if the text must be highlighted
   * @return either the annotated text (from annotator helper) or the text content if no annotation could be created
   * from external service or cache, with existing and viewable texts highlighted in it. If given text
   * does not exist or is not viewable for context user (from cookie), a message is returned in respectively a
   * unauthorized (401) a bad request (400)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getAnnotatedText(Long textId, boolean highlighted) {
    logger.info("get annotated text for " + textId);
    Map<String, String> messages = new HashMap<>();
    messages.put(SessionHelper.INFO, i18n.get(ctx().lang(), "text.args.nocontent"));
    Http.Context ctx = ctx();
    Text text = textFactory.retrieve(textId);

    if (text != null) {
      WebdebUser user = sessionHelper.getUser(ctx);
      String lang = ctx().lang().code();

      // must explicitly cast back to Text after filter
      List<CitationHolder> filtered = highlighted ?
              helper.toCitationsHolders(text.getTextCitations(), user, lang, true) : new ArrayList<>();
      // retrieve annotated file, or ask annotation if not yet annotated
      try {
        Gson gson = new Gson();
        String ch = "\"text\":" + gson.toJson(getAnnotatedTextContent(textId));
        ch += ", \"citations\" : " + Json.toJson(filtered);
        ch = "{" + ch + "}";

        return CompletableFuture.completedFuture(ok(ch));
      } catch (PermissionException e) {
        //logger.info("contributor " + user.getId() + " has no (right to see) content of " + textId, e);
        return CompletableFuture.completedFuture(unauthorized(Json.toJson(message.render(messages).toString())));
      }
    } else {
      logger.error("unable to retrieve text " + textId);
    }
    return CompletableFuture.completedFuture(badRequest(Json.toJson(message.render(messages).toString())));
  }

  /**
   * Get the content (may be annotated, but not highlighted) of a given text on a modal page.
   *
   * @param textId the text id
   * @return either the modal page with the content text. If given text does not exist or is not viewable for context
   * user (from cookie), a message is returned in respectively a unauthorized (401) a bad request (400)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getTextContentModal(Long textId) {
    logger.info("get modal text content for " + textId);
    Map<String, String> messages = new HashMap<>();
    messages.put(SessionHelper.INFO, i18n.get(ctx().lang(), "text.args.nocontent"));
    Http.Context ctx = ctx();
    Text text = textFactory.retrieve(textId);
    if (text != null) {
      WebdebUser user = sessionHelper.getUser(ctx);
      TextHolder holder = helper.toTextHolder(text, user, ctx.lang().code(), false);
      return CompletableFuture.completedFuture(ok(displayTextContentModal.render(holder, user)));
    } else {
      logger.error("unable to retrieve text " + textId);
    }
    return CompletableFuture.completedFuture(badRequest(Json.toJson(message.render(messages).toString())));
  }

  /**
   * Call NLP service to retrieve the content behind a url
   *
   * @param url a url
   * @return a json structure according to NLP service with, a.o, text content, title, date, etc, or
   * a bad request (400) if given url is not valid or gave no result
   */
  public CompletionStage<Result> getTextContent(String url) {
    logger.debug("get " + url);
    Http.Context ctx = ctx();
    Map<String, String> messages = new HashMap<>();
    if (values.isURL(url)) {
      return proxy.getTextContentAsJson(url).thenApply(result -> {
        if (result != null) {
          return ok(result);
        }
        messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),"nlp.text.error"));
        return badRequest(message.render(messages));
      });
    }
    messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),"nlp.text.invalidurl"));
    return CompletableFuture.completedFuture(badRequest(message.render(messages)));
  }

  /**
   * Call NLP service to retrieve the language of a given text
   *
   * @param content a (truncated) content to analyze
   * @return a json json structure according to NLP service of the form { language=id } or a message in
   * a bad request (400) if WDTAL call did not succeed
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getTextLanguage(String content) {
    return CompletableFuture.completedFuture(ok(values.detectTextLanguage(content, ctx().lang().code())));

  }

  /**
   * Extract content of pdf from given url
   *
   * @param url an url to get the PDF from
   * @return either a JSON structure with the language and content of the text or a message to be rendered
   * in a bad request (400)
   */
  public CompletionStage<Result> extractPDFContent(String url) {
    logger.debug("GET pdf content from " + url);
    String trimmed = url != null ? url.trim() : null;
    Map<String,String> messages = new HashMap<>();
    // we don't use isBlank method fron valuesHelper to avoid compilator warning
    if (trimmed != null && !trimmed.trim().isEmpty()
            && ".pdf".equalsIgnoreCase(trimmed.substring(trimmed.lastIndexOf('.'), trimmed.lastIndexOf('.') + 4))) {
      Http.Context ctx = ctx();
      return proxy.retrievePDF(url).thenApplyAsync(response -> {
        if (response != null) {
          return ok(response);
        }
        messages.put(SessionHelper.ERROR, i18n.get(ctx.lang(), "text.pdf.error"));
        return badRequest(message.render(messages));
      }, context.current());
    }
    messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "text.pdf.nourl"));
    return CompletableFuture.completedFuture(badRequest(message.render(messages)));
  }

  /**
   * Get all linked contributions by type
   *
   * @param textId the text id
   * @param panes a comma separated EPane that need to be loaded
   * @param pov for a specific contains in pane, -1 if none
   * @return a jsonified list of debate holders
   */
  public CompletionStage<Result> getLinkedContributions(Long textId, String panes, Integer pov) {
    logger.debug("GET linked contributions for text " + textId + " for panes " + panes + " and pov " + pov);
    Text text = textFactory.retrieve(textId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    List<ETextVizPane> panesList = new ArrayList<>();
    final int fpov = pov != null ? pov : 0;

    if(!values.isBlank(panes)) {
      String splitted[] = panes.split(";");
      for(int iPane = 0; iPane < splitted.length; iPane++){
        ETextVizPane pane = ETextVizPane.value(Integer.decode(splitted[iPane]));
        if(pane != null) {
          panesList.add(pane);
        }
      }
    }

    if(text != null && !panes.isEmpty()) {
      TextVizHolder textVizHolder = new TextVizHolder(text, user, lang);
      Map<Integer, String> response = new HashMap<>();

      for(ETextVizPane pane : panesList) {
        switch (pane) {
          case ARGUMENTS:
            if(fpov < 0){
              if(!user.mayView(text) || !textFactory.contributionCanBeEdited(user.getId(), textId, user.getGroupId())) {
                return sendUnauthorizedContribution();
              }
              response.put(pane.id(), contributionArgumentsDragnDrop.render(textVizHolder, null, user).toString());
            }else{
              response.put(pane.id(), textArgumentsStructure.render(textVizHolder, user, false).toString());
            }
            break;
        }
      }

      return sendOk(Json.toJson(response));
    }

    return sendBadRequest();
  }

  /*
   * External text requests
   */


  /**
   * Get all data about the text (text details and text citations). The post method is used to hide the toke from spy.
   *
   * @return the text data or bad request if the text is unknown
   */
  public CompletionStage<Result> getTextData() {
        /*
            logger.debug("[POST] get data about text");
        Form<ExternalForm> form = formFactory.form(ExternalForm.class).bindFromRequest();

        if (!form.hasErrors()) {
            ExternalForm textForm = form.get();
            Contributor contributor = textForm.getUser().getContributor();
            if (contributor != null) {
                session(SessionHelper.KEY_USERMAILORPSEUDO, contributor.getEmail());
            }
            WebdebUser user = sessionHelper.getUser(ctx());
            Text text = textFactory.findByUrl(textForm.getUrl());

            if (text != null) {
                VizTextResponse textsResponse =
                        new VizTextResponse(helper.toTextHolder(text, user, textForm.getUser().getLang(), false));

                return CompletableFuture.supplyAsync(() ->
                        ok(Json.toJson(textsResponse).toString()), context.current());
            }
        }
        return CompletableFuture.supplyAsync(Results::badRequest, context.current());

         */
    return sendBadRequest();
  }

  /**
   * Save a text from external service into the database
   *
   * @return the saved text id or bad request if an error occured
   */
  public CompletionStage<Result> saveFromExternal() {
    logger.debug("POST save external text");
            /*
    Form<ExternalForm> form = formFactory.form(ExternalForm.class).bindFromRequest();

    if (!form.hasErrors()) {
      ExternalForm textForm = form.get();
      Contributor contributor = textForm.getUser().getContributor();
      if(contributor != null){
        session(SessionHelper.KEY_USERMAILORPSEUDO, contributor.getEmail());
        sessionHelper.getUser(ctx());
        try{
          Long textId;
          Text text = textFactory.findByUrl( textForm.getUrl());

          if(text == null) {
              textForm.save(contributor.getId());
              textId = textForm.getId();
          } else {
            return sendBadRequest();
          }

          return CompletableFuture.supplyAsync(() ->
                  ok(be.webdeb.presentation.web.controllers.entry.text.routes.TextActions.edit(textId).toString()), context.current());
        } catch (FormatException | PersistenceException | PermissionException e) {
          logger.error("unable to save external text", e);
        }
      }else{
        return sendUnauthorized();
      }
    }
        */
    return sendBadRequest();
  }

  /**
   * Get the text content or the  html content for the given text
   *
   * @param id a text id
   * @param highlighted true if the content must be highlighted
   * @return the text content or the html text content of the given text
   */
  public CompletionStage<Result> getTextContentOrHtmlContent(Long id, boolean highlighted){
    Text text = textFactory.retrieve(id);
    WebdebUser user = sessionHelper.getUser(ctx());

    if(text != null){
      if(text.isContentVisibleFor(user.getId())){
        return getAnnotatedText(id, highlighted);
      }

      if(text.getUrl() != null && values.isURL(text.getUrl())){
        Map<String, String> response = getHtmlContentMap(text.getUrl());

        if(response.containsKey("content")){
          return sendOk(values.transformTextToHtml(response.get("content")));
        }
      }
    }

    return sendBadRequest();
  }

  /**
   * Find a text by url
   *
   * @param url a url to search
   * @return ok with the corresponding text id, if any or badrequest if not found
   */
  public CompletionStage<Result> findTextByUrl(String url){
    Text text = textFactory.findByUrl(url);
    return text != null ? sendOk(text.getId().toString()) : sendBadRequest();
  }

  /**
   * Get the text content of a html content found by the given url
   *
   * @param url a url for which we need is content
   * @return the html text content of the given url
   */
  public CompletionStage<Result> getHtmlContent(String url){
    return sendOk(Json.toJson(getHtmlContentMap(url)).toString());
  }

  public Map<String, String> getHtmlContentMap(String url){
    logger.debug("Get html content of url : " + url);

    Map<String, String> response = new HashMap<>();

    try{
      Document doc = Jsoup.connect(url).get();

      boolean isTwitter = values.getURLDomain(url).equals("twitter.com");
      Element head = doc.head();
      Element article = getHtmlArticleElement(doc.body(), isTwitter);

      response.put("content", getHtmlArticleContent(article, isTwitter));
      response.put("title", getHtmlArticleTitle(head, article));
      response.put("date", getHtmlArticlePublicationDate(head, article, isTwitter));
      response.put("author", getHtmlArticleAuthor(head, article, isTwitter));
      response.put("isArticle", String.valueOf(isPageAnArticle(head)));
      response.put("source", values.getURLDomain(url));
    }
    catch(Exception e) {
      logger.debug("Unable to get html content map for " + url);
    }

    return response;
  }

  /**
   * Check if a given domain name is a free source or not. Free means free of rights.
   *
   * @param domain the domain name to check
   * @return true if the given domain name is a free source
   */
  public CompletionStage<Result> checkFreeSource(String domain){
    logger.debug("Get is free source : " + domain);

    return CompletableFuture.supplyAsync(() -> ok(Json.toJson(textFactory.sourceIsCopyrightfree(domain, sessionHelper.getUser(ctx()).getId()))), context.current());
  }

  /*
   * Private helpers
   */

  /**
   * Edit a text from a modal or not.
   *
   * @param textId a text id
   * @param fromModal true if it is from a modal
   * @return bad request if citation not found, unauthorize if user must not see this text, the edit form if all is ok.
   */
  private CompletionStage<Result> editText(Long textId, boolean fromModal) {

    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    Text text = textFactory.retrieve(textId);
    ExternalContribution externalText = null;
    TextForm form;

    if (text != null) {
      if(!user.mayView(text)){
        return sendUnauthorizedContribution();
      }
      form = new TextForm(text, user, lang);
      form.setInGroup(sessionHelper.getCurrentGroup(ctx()));

    } else {
      if (textId != -1L) {
        externalText = textFactory.retrieveExternal(textId);
        if(externalText.getInternalContribution() != null){
          return sendBadRequest();
        }
      }
      // set "from internet" to true by default for new texts and set current group
      form = new TextForm(externalText, ctx().lang().code());
      // set default group
      form.setInGroup(sessionHelper.getCurrentGroup(ctx()));
      // set current language
      form.setLang(ctx().lang().code());
      // set default visibility to private
      form.setTextVisibility(String.valueOf(ETextVisibility.PEDAGOGIC.id()));
      // set mayViewContent to true by default (otherwise form will be blocked)
      form.setMayViewContent(true);
    }

    return sendOk(fromModal ? editTextModal.render(formFactory.form(TextForm.class).fill(form), helper, user) :
            editText.render(formFactory.form(TextForm.class).fill(form), helper, user, messages));
  }

  /**
   * Save an text from a given form.
   *
   * @param form an text form object that may contain errors
   * @return given (updated) actor form
   * @throws TextActions.TextNotSavedException if an error exist in passed form or any error arisen from save action
   */
  public synchronized TextForm saveText(Http.Request request, Form<TextForm> form) throws TextActions.TextNotSavedException {
    File file = getFileFromRequest(request);

    // sends back form if there are some errors
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors().toString());
      // save file in temp fs
      if (file != null) {
        files.saveToCache(file, form.data().get("filename"));
      }
      throw new TextActions.TextNotSavedException(form, TextActions.TextNotSavedException.ERROR_FORM);
    }

    TextForm text = form.get();
    WebdebUser user = sessionHelper.getUser(ctx());

    // check name matches for authors and source authors
    NameMatch<ActorHolder> match = helper.searchForNameMatches("authors", text.getAuthors(), user, ctx().lang().code());
    if (!match.isEmpty()) {
      // save file in temp fs
      if (file != null) {
        files.saveToCache(file, form.data().get("filename"));
      }
      throw new TextActions.TextNotSavedException(form.fill(text), TextActions.TextNotSavedException.AUTHOR_NAME_MATCH, match);
    }

    // check if the functions are in DB, if isn't create them and return their id
    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWPROFESSION);
    List<Integer> newProfessions = helper.checkIfNewProfessionsMustBeCreated(
            helper.convertActorSimpleFormToProfessionForm(text.getAuthors(), ctx().lang().code()));
    newProfessions.forEach(idP -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWPROFESSION, idP+""));

    // store contribution and get list of unknown actors to be shown
    try {
      Long contributor = sessionHelper.getUser(ctx()).getContributor().getId();
      text.setUser(user);
      treatSaveContribution(text.save(contributor));

      // this may not crash since we just created it
      String textFilename = null;
      try {
        textFilename = textFactory.retrieve(text.getId()).getFilename(contributor);
        // clear cache and xml annotated file to force reload of parsed content
        if (!text.getId().equals(-1L)) {
          cache.remove("annotated.raw." + text.getId());
          cache.remove("annotated.user." + text.getId());
          cache.remove("file." + textFilename + ".xml");
          cache.remove("file." + textFilename);
          files.deleteAnnotatedText(textFilename);
        }

        // launch annotation (async) if text is plain
        if (!text.getNoContent()) {
          if ("text/plain".equals(files.getContentType(textFilename))) {
            getAnnotatedText(text.getId(), true);
          } else {
            // check where is the file
            if (file == null || file.length() == 0) {
              // check in cache with form file name
              logger.debug("try to retrieve from temp cache " + text.getFilename());
              file = files.getFromCache(text.getFilename());
            }

            if (file != null) {
              logger.debug("will save external file " + text.getFilename() + " to " + textFilename);
              files.saveContributionTextFile(textFilename, file);
            }
          }
        }
      } catch (PermissionException e) {
        logger.debug("Nothing to clear in cache.");
      }

      // redirect to VizActions text
      return text;

    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to save text", e);
      throw new TextActions.TextNotSavedException(form, TextActions.TextNotSavedException.ERROR_DB);
    }
  }

  /**
   * Handle error on text form submission and returns the actor form view (depending on the switch).
   * If an unknown error occurred, either a "goto" page or the general entry view is returned.
   *
   * @param exception the exception raised from unsuccessful save
   * @return if the form contains error, a bad request (400) response is returned with, if onlyfield, the
   * editTextFields template or the editText full form otherwise. In case of possible author name matches,
   * a 409 response is returned with the modal frame to select among possible matches.In case of possible
   * tag name matches, a 410 response is returned with the modal frame to select among possible matches.
   * If another error occurred, a redirect to either a "goto" session-cached url or the main entry page.
   */
  public CompletionStage<Result> handleTextError(TextNotSavedException exception, boolean onlyFields) {
    Map<String, String> messages = new HashMap<>();
    Form<TextForm> form = exception.form;
    WebdebUser user = sessionHelper.getUser(ctx());
    TextForm text;

    switch (exception.error) {
      case TextNotSavedException.AUTHOR_NAME_MATCH:
        if(exception.match != null && !exception.match.isEmpty()) {
          return CompletableFuture.supplyAsync(() ->
                          status(409, handleNameMatches.render(exception.match.getNameMatches(), exception.match.isActor(), exception.match.getIndex(), exception.match.getSelector(), null, values))
                  , context.current());
        }

      case TextNotSavedException.ERROR_FORM:
        // error in form, just resend it
        if(!onlyFields)
          flash(SessionHelper.WARNING, "error.form");
        return CompletableFuture.supplyAsync(() -> badRequest(editTextFields.render(form, helper, user)), context.current());
      default:
        // any other error, check where do we have to go after and show message in exception
        flash(SessionHelper.ERROR, "error.crash");
        return sendInternalServerError(be.webdeb.presentation.web.controllers.entry.routes.EntryActions.contribute().url());
    }
  }

  /**
   * Get the annotation of a given text
   *
   * @param textId the text id to get the annotated version
   * @return the annotated context text
   */
  private String getAnnotatedTextContent(Long textId) throws PermissionException {
    String content = "";
    Http.Context ctx = ctx();

    Text text = textFactory.retrieve(textId);
    if (text != null) {
      WebdebUser user = sessionHelper.getUser(ctx);
      String filename = text.getFilename(user.getId());
      content = files.getContributionTextFile(filename);
      content = content.replaceAll("\\r\\n|\\r|\\n", " <br>")
              .replaceAll("’", "'");
    }

    return content;
  }

  /**
   * HTML article element selection
   *
   * @return an html element(s)
   */
  private Element searchArticleElement(Element article, String [] selectors){
    int iArticle;

    for(iArticle = 0; iArticle < selectors.length && article.select(selectors[iArticle]).isEmpty(); iArticle++);

    return iArticle < selectors.length ? article.select(selectors[iArticle]).first() : null;
  }

  /**
   * HTML article element selection
   *
   * @return an html element(s)
   */
  private Element searchArticleElement(Element article, String selector){
    return article.select(selector).first();
  }

  /**
   * HTML article element selection
   *
   * Search element like article content, authors, publication date and title in the given html article.
   */
  private String searchArticleElementContent(Element element){

    if(element != null){
      if(element.text().length() > 0){
        return element.text();
      }else if(element.attr("content").length() > 0){
        return element.attr("content");
      }
    }

    return null;
  }

  /**
   * Get the article element
   *
   * @param isTwitter does the article come from twitter
   * @return the article element or null
   */
  private Element getHtmlArticleElement(Element article, boolean isTwitter){
    List<String> selectors = Arrays.asList(
            ".js-main-container",
            "main",
            "body");

    if(isTwitter)
      selectors.add(0, ".js-original-tweet");

    return searchArticleElement(article, selectors.toArray(new String[selectors.size()]));
  }

  /**
   * Get the article content
   *
   * @param isTwitter does the article come from twitter
   * @return the article content or null
   */
  private String getHtmlArticleContent(Element article, boolean isTwitter){

    if(isTwitter) {
      String [] selectors = {".tweet-text"};
      return searchArticleElementContent(searchArticleElement(article, selectors));
    }

    String [] selectors = {
      ".rtbf-article-main__content",
      "[itemprop=\"articleBody\"]",
      ".gr-article-content",
      ".article-text",
      "#body-detail"
    };
    return searchArticleElementContent(searchArticleElement(article, selectors));
  }

  /**
   * Get the article title
   *
   * @param head the page head
   * @return the article title or null
   */
  private String getHtmlArticleTitle(Element head, Element article){
    Element title = searchArticleElement(head, "meta[property='og:title']");

    if(title == null || title.attr("content").isEmpty()) {
      String[] selectors = {
              "h1",
              "[itemprop='headline']",
              ".headline",
              "[class*='headline']",
              ".title",
              "[class='title']",
              "[class='article-title']",
              "[class*='title']",
              "[itemprop='name']",
              "h2", "h3", "h4", "h6"};
      return searchArticleElementContent(searchArticleElement(article, selectors));
    }
    return title.attr("content");
  }

  /**
   * Get the article publication date
   *
   * @param isTwitter does the article come from twitter
   * @return the article publication date or null
   */
  private String getHtmlArticlePublicationDate(Element head, Element article, boolean isTwitter){
    if(isTwitter){
      String [] selectors = {".metadata"};
      return searchArticleElementContent(searchArticleElement(article, selectors));
    }

    Element dateMeta = searchArticleElement(head, "meta[property='article:published_time']");
    String [] selectors = {"[itemprop^='date']",
            "[itemprop*='date']",
            ".date",
            "time",
            "[class*='date']"};
    String date = dateMeta == null || dateMeta.attr("content").isEmpty() ?
            searchArticleElementContent(searchArticleElement(article, selectors)) :
            dateMeta.attr("content");

    if(date != null) {
      DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();

      try {
        DateTime dateObj = parser.parseDateTime(date);
        date = dateObj.getYear()
                + "-" + (dateObj.getMonthOfYear() < 10 ? "0" : "") + dateObj.getMonthOfYear()
                + "-" + (dateObj.getDayOfMonth() < 10 ? "0" : "") + dateObj.getDayOfMonth();
      } catch (Exception e) {

      }
    }

    return date;
  }

  /**
   * Get the article author
   *
   * @param isTwitter does the article come from twitter
   * @return the article author or null
   */
  private String getHtmlArticleAuthor(Element head, Element article, boolean isTwitter){
    if(isTwitter){
      return article.attr("data-name");
    }
    String [] selectors = {"[itemprop='author']",
            "[itemprop='creator']",
            "[itemprop*='author']",
            ".author",
            "[class*='author']",
            ".fullname",
            ".name"};
    return searchArticleElementContent(searchArticleElement(article, selectors));
  }

  /**
   * Determine if the page is an article
   *
   * @param head the page head
   * @return true if it is an article, false otherwise
   */
  private boolean isPageAnArticle(Element head){
    Element type = searchArticleElement(head, "meta[property='og:type']");

    return type != null && type.attr("content").equals("article");
  }

  /**
   * Inner class to handle text exception when an text cannot be saved from private save execution
   */
  public class TextNotSavedException extends Exception {

    private static final long serialVersionUID = 1L;
    final Form<TextForm> form;
    final NameMatch<ActorHolder> match;
    final int error;

    static final int ERROR_FORM = 0;
    static final int AUTHOR_NAME_MATCH = 1;
    static final int ERROR_DB = 3;

    TextNotSavedException(Form<TextForm> form, int error) {
      this.error = error;
      this.form = form;
      this.match = null;
    }

    TextNotSavedException(Form<TextForm> form, int error, String message) {
      super(message);
      this.error = error;
      this.form = form;
      this.match = null;
    }

    TextNotSavedException(Form<TextForm> form, int error, NameMatch<ActorHolder> match) {
      this.error = error;
      this.form = form;
      this.match = match;
    }
  }
}

