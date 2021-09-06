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

package be.webdeb.presentation.web.controllers.entry.argument;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.LinkException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.util.*;
import be.webdeb.presentation.web.views.html.entry.argument.editArgumentModal;
import be.webdeb.presentation.web.views.html.entry.argument.editArgumentFields;
import be.webdeb.presentation.web.views.html.entry.argument.editArgumentDictionaryModal;
import be.webdeb.presentation.web.views.html.viz.citation.citationContainerList;
import be.webdeb.presentation.web.views.html.viz.citation.citationListModal;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Argument-related actions, ie controller of all pages to edit arguments
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ArgumentActions extends CommonController {

  @Inject
  protected RequestProxy proxy;

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private static final String OOPS = "argument.properties.oops";

  /**
   * Ask page to add a new argument
   *
   * @param contextId       a contribution context id
   * @param subContextId    a tag sub context id, if any
   * @param categoryId      a tag category id
   * @param superArgumentId an argument id
   * @param shade           an justification link shade
   * @return the modal to complete the argument
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> newArgument(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Integer shade) {
    logger.debug("GET new argument for context " + contextId + " , sub context " + subContextId + " , category " + categoryId +
            " and super argument " + superArgumentId + " with shade " + shade);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    ContextContribution context = textFactory.retrieveContextContribution(contextId);

    if(context == null) {
      return sendBadRequest();
    }

    if(!user.mayView(context) || !textFactory.contributionCanBeEdited(user.getId(), contextId, user.getGroupId())) {
      return sendUnauthorizedContribution();
    }

    // store link in user session with shade
    deleteNewLinkKeys();
    List<String> toStore =
            Arrays.asList(String.valueOf(contextId), String.valueOf(subContextId), String.valueOf(categoryId), String.valueOf(superArgumentId), String.valueOf(shade));

    sessionHelper.setValues(ctx(), SessionHelper.KEY_NEW_ARG_JUS_LINK, toStore);

    ArgumentForm argForm = new ArgumentForm(EArgumentType.SHADED, lang);
    argForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    Form<ArgumentForm> form = formFactory.form(ArgumentForm.class).fill(argForm);

    Text text = textFactory.retrieve(contextId);
    TextHolder textHolder = text != null ? helper.toTextHolder(text, user, lang, false) : null;

    // display first page of argument edition
    return sendOk(editArgumentModal.render(form, textHolder, null, helper, user, null));
  }

  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> newArgumentFromCitation(Long citationId) {
    logger.debug("GET new argument from citation justification link " + citationId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    // store link in user session with shade
    deleteNewLinkKeys();

    CitationJustification justification = citationFactory.retrieveJustificationLink(citationId);

    if(justification != null) {

      if(justification.getContext() == null) {
        return sendBadRequest();
      }

      if(!user.mayView(justification.getContext()) || !textFactory.contributionCanBeEdited(user.getId(), justification.getOriginId(), user.getGroupId())) {
        return sendUnauthorizedContribution();
      }

      ArgumentForm argForm = new ArgumentForm(justification, EArgumentType.SHADED, lang);
      argForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
      Form<ArgumentForm> form = formFactory.form(ArgumentForm.class).fill(argForm);

      CitationHolder citationHolder = helper.toCitationHolder(justification.getCitation(), user, lang, false);

      // display first page of argument edition
      return sendOk(editArgumentModal.render(form, null, citationHolder, helper, user, null));
    }

    return sendBadRequest();
  }

  /**
   * Display the modal to edit the  argument properties
   *
   * @param argId the  argument id to edit
   * @return the edit modal page with given argument data, or redirect to the session "goto" page with an error message
   * to display (flash) if  argument is not found
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editFromModal(Long argId, Long contextId) {
    logger.debug("GET argument properties of " + argId + " and context " + contextId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    deleteNewLinkKeys();

    // prepare wrapper for argument
    Argument argument = argumentFactory.retrieve(argId);
    if (argument == null) {
      // leave if we couldn't find  argument
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), OOPS));
      return sendBadRequest();
    } else if (!user.mayView(argument) || !textFactory.contributionCanBeEdited(user.getId(), argId, user.getGroupId())) {
      return sendUnauthorizedContribution();
    }


    ArgumentForm argForm = new ArgumentForm(argument, lang);
    argForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    // display first page of argument edition
    Form<ArgumentForm> form = formFactory.form(ArgumentForm.class).fill(argForm);

    Text text = textFactory.retrieve(contextId);
    TextHolder textHolder = text != null ? helper.toTextHolder(text, user, lang, false) : null;

    return sendOk(editArgumentModal.render(form, textHolder, null, helper, user, null));
  }

  /**
   * Update or create a  argument
   *
   * @param argId the  argument id (-1 if new one)
   * @return the argument context form object if it contains errors, or the context contribution page if this new
   * argument has been created to be linked to another one, or in case of success or DB crash,
   * redirect to "goto" session key url.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveFromModal(Long argId, Long contextId) {
    logger.debug("POST save properties of " + argId);
    Form<ArgumentForm> form = formFactory.form(ArgumentForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());

    // check errors, sends back whole form if any
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
      ContextContribution context = textFactory.retrieveContextContribution(contextId);
      return sendBadRequest(editArgumentFields.render(form, !(context != null && context.getType() == EContributionType.TEXT), helper, user, null));
    }

    ArgumentForm argument = form.get();

    // try to save in DB
    try {
      treatSaveContribution(argument.save(user.getContributor().getId()));

      if(argument.getCitationJustificationLinkId() != null) {
        CitationJustification justification = citationFactory.retrieveJustificationLink(argument.getCitationJustificationLinkId());

        if(justification != null) {
          ArgumentJustification argumentJustification = argumentFactory.getArgumentJustificationLink();

          argumentJustification.setOriginId(justification.getOriginId());
          argumentJustification.setSubContextId(justification.getSubContextId());
          argumentJustification.setTagCategoryId(justification.getTagCategoryId());
          argumentJustification.setLinkType(justification.getLinkType());
          argumentJustification.setDestinationId(argument.getId());

          if(justification.getContext().getType() == EContributionType.TEXT)
            argumentJustification.setSuperArgumentId(justification.getSuperArgumentId());

          argumentJustification.save(user.getContributor().getId(), user.getGroupId());

          justification.setSuperArgumentId(argument.getId());
          justification.save(user.getContributor().getId(), user.getGroupId());
        }
      }

    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to save argument", e);
      return sendInternalServerError();
    }

    // check if this argument need to be included in a justification link
    List<String> cachedLink = sessionHelper.getValues(ctx(), SessionHelper.KEY_NEW_ARG_JUS_LINK);
    if (cachedLink != null && cachedLink.size() >= 5) {
      logger.info("ask confirmation for previously new link request to new argument " + argument.getId());

      sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_ARG_JUS_LINK);

      try {
        Long subContextId = Long.parseLong(cachedLink.get(1));
        Long categoryId = Long.parseLong(cachedLink.get(2));
        Long superArgumentId = Long.parseLong(cachedLink.get(3));
        int shade = Integer.parseInt(cachedLink.get(4));

        ArgumentJustification existing = argumentFactory.findArgumentJustification(
                contextId,
                subContextId,
                values.isBlank(categoryId) ? null : categoryId,
                values.isBlank(superArgumentId) ? null : superArgumentId,
                argument.getId(),
                shade);
        if(existing != null && existing.isMemberOfGroup(user.getGroupId())) {
          return CompletableFuture.supplyAsync(() -> status(409), context.current());
        }

        return saveArgumentJustificationLink(argument.getId(), contextId, subContextId, categoryId, superArgumentId, shade, user.getGroupId());
      } catch (NumberFormatException e) {
        logger.debug("session params for argument justification link are not well formatted " + e);
        return sendBadRequest();
      }

    }

    deleteNewLinkKeys();
    return sendOk();

  }

  /**
   * Update or create a  argument
   *
   * @param argId the  argument id (-1 if new one)
   * @return the argument context form object if it contains errors, or the context contribution page if this new
   * argument has been created to be linked to another one, or in case of success or DB crash,
   * redirect to "goto" session key url.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editDictionary(Long argId) {
    logger.debug("GET edit properties of " + argId);

    Map<String, String> messages = new HashMap<>();
    ArgumentDictionary dictionary = argumentFactory.retrieveDictionary(argId);
    if (dictionary == null) {
      return CompletableFuture.supplyAsync(Results::badRequest, context.current());
    }

    ArgumentDictionaryForm form = new ArgumentDictionaryForm(dictionary);

    return CompletableFuture.supplyAsync(() -> ok(editArgumentDictionaryModal.render(
            formFactory.form(ArgumentDictionaryForm.class).fill(form),
            helper,
            sessionHelper.getUser(ctx()),
            messages
    )), context.current());
  }

  /**
   * Update or create a  argument
   *
   * @param argId the  argument id (-1 if new one)
   * @return the argument context form object if it contains errors, or the context contribution page if this new
   * argument has been created to be linked to another one, or in case of success or DB crash,
   * redirect to "goto" session key url.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveDictionary(Long argId) {
    logger.debug("POST save properties of " + argId);

    Form<ArgumentDictionaryForm> form = formFactory.form(ArgumentDictionaryForm.class).bindFromRequest();

    // check errors, sends back whole form if any
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      return sendBadRequest();
    }

    // try to save in DB
    try {
      ArgumentDictionaryForm dictionary = form.get();
      dictionary.save(sessionHelper.getUser(ctx()).getContributor().getId());
    } catch (FormatException | PermissionException | PersistenceException e) {
      logger.error("unable to save argument dictionary", e);
      return sendInternalServerError();
    }
    return sendOk();
  }

  /**
   * Save an argument justification link
   *
   * @param argumentId      an argument id
   * @param contextId       a contribution context id
   * @param subContextId    a tag sub context id, if any
   * @param categoryId      a tag category id
   * @param superArgumentId an argument id
   * @param shade           an justification link shade
   * @param group           the group where save this link
   * @return the status code depending of the success or fail or the process
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveArgumentJustificationLink(Long argumentId, Long contextId, Long subContextId, Long categoryId, Long superArgumentId, int shade, int group) {
    logger.info("POST save argument justification link between argument " + argumentId + " context " + contextId + ", sub context " + subContextId +
            " , category " + categoryId + " and super argument " + superArgumentId + " with shade " + shade + " in group " + group);

    WebdebUser user = sessionHelper.getUser(ctx());

    if (argumentFactory.retrieve(argumentId) == null) {
      logger.debug("argument is null for argument justification link, id was " + argumentId);
      return sendBadRequest();
    }

    try {
      int order = argumentFactory.getMaxArgumentJustificationLinkOrder(contextId, subContextId, categoryId, superArgumentId, shade) + 1;
      ArgumentJustification justification = argumentFactory.getArgumentJustificationLink();
      saveJustificationLink(justification, argumentId, contextId, subContextId, categoryId, superArgumentId, shade, order, group == -1 ? user.getGroupId() : group);
    } catch (LinkException e) {
      return handleLinkExceptionResponse(e);
    }

    return sendOk();
  }

  /**
   * Save a argument similarity link between two arguments
   *
   * @param originId      an argument id
   * @param destinationId an argument id
   * @param shade         an justification link shade
   * @param group         the group where save this link
   * @return the status code depending of the success or fail or the process
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveArgumentSimilarityLink(Long originId, Long destinationId, int shade, int group) {
    logger.info("POST save argument similarity link between arguments " + originId + " and " + destinationId +
            " with shade " + shade + " in group " + group);

    if (argumentFactory.retrieve(originId) == null) {
      logger.debug("argument origin is null for argument similarity link, id was " + originId);
      return sendBadRequest();
    }

    if (argumentFactory.retrieve(destinationId) == null) {
      logger.debug("argument destination is null for argument similarity link, id was " + destinationId);
      return sendBadRequest();
    }

    try {
      saveSimilarityLink(argumentFactory.getArgumentSimilarityLink(), originId, destinationId, shade, group);
    } catch (LinkException e) {
      return handleLinkExceptionResponse(e);
    }

    return sendOk();
  }


  /*
   * AJAX calls
   */

  /**
   * Search for an argument title.
   *
   * @param term      the values to search for
   * @param type the type of argument, if any otherwise -1
   * @param shade the shade of argument, if any otherwise -1
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a jsonified list of argument dictionary simple form
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchArgument(String term, int type, int shade, int fromIndex, int toIndex) {
    List<ArgumentDictionarySimpleForm> result = argumentFactory.findByTitle(term, null, type, shade, fromIndex, toIndex).stream()
            .map(a -> new ArgumentDictionarySimpleForm(a.getId(), a.getTitle(), a.getLanguage().getCode())).collect(Collectors.toList());
    return CompletableFuture.supplyAsync(() -> ok(Json.toJson(result)), context.current());
  }

  /**
   * Search for an argument dictionary.
   *
   * @param term      the values to search for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a jsonified list of argument dictionary simple form
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchArgumentDictionary(String term, int fromIndex, int toIndex) {
    List<ArgumentDictionarySimpleForm> result = argumentFactory.findDictionaryByTitle(term, null, fromIndex, toIndex).stream()
            .map(ArgumentDictionarySimpleForm::new).collect(Collectors.toList());
    return CompletableFuture.supplyAsync(() -> ok(Json.toJson(result)), context.current());
  }

  /**
   * Get the html content with all citations related of given argument in given context
   *
   * @param contextId       a contribution context id
   * @param subContextId      a tag sub context id
   * @param categoryId      a tag category id
   * @param argumentId an argument id
   * @param shade           an justification link shade
   * @return the status code depending of the success or fail or the process
   */
  public CompletionStage<Result> getArgumentCitationLinks(Long contextId, Long subContextId, Long categoryId, Long argumentId, int shade) {
    //logger.debug(contextId +" "+subContextId + " " + categoryId + " " + argumentId + " " + shade);
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(contextId);

    if(contextContribution != null && !values.isBlank(subContextId)) {
      contextContribution = contextContribution.getTagDebate(subContextId);
    }

    if(contextContribution != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      return sendOk(citationContainerList.render(
                  helper.toCitationHoldersFromJustification(
                          contextContribution.getCitationJustificationLinks(contextContribution.getType() == EContributionType.TEXT ? null : categoryId, argumentId, contextContribution.getType() == EContributionType.TEXT ? null : shade),
                          user,
                          lang,
                          true),
              user,
              true,
              true,
              false,
              null,
              -1,
              contextId));
    }

    return sendBadRequest();
  }
}