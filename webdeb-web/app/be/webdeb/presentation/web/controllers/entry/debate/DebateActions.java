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

package be.webdeb.presentation.web.controllers.entry.debate;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.argument.ArgumentShaded;
import be.webdeb.core.api.argument.EArgumentType;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.*;
import be.webdeb.core.api.debate.*;

import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.entry.ContextContributionHolder;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.NameMatch;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.debate.structure.StructureArgument;
import be.webdeb.presentation.web.controllers.entry.debate.structure.StructureCategory;
import be.webdeb.presentation.web.controllers.entry.debate.structure.StructureCitation;
import be.webdeb.presentation.web.controllers.entry.debate.structure.StructureClassifier;
import be.webdeb.presentation.web.controllers.entry.routes;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.entry.tag.TagCategoryHolder;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextActions;
import be.webdeb.presentation.web.controllers.entry.text.TextForm;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.core.api.actor.ESocioGroupKey;
import be.webdeb.presentation.web.controllers.viz.debate.DebateVizHolder;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;

import be.webdeb.presentation.web.views.html.entry.debate.editDebate;
import be.webdeb.presentation.web.views.html.entry.debate.editDebateFields;
import be.webdeb.presentation.web.views.html.entry.debate.editDebateModal;
import be.webdeb.presentation.web.views.html.entry.text.textSelectionModal;
import be.webdeb.presentation.web.views.html.util.handleNameMatches;

import be.webdeb.presentation.web.views.html.util.message;
import be.webdeb.presentation.web.views.html.viz.common.contributionAlliesOpponents;
import be.webdeb.presentation.web.views.html.viz.common.contributionBibliography;
import be.webdeb.presentation.web.views.html.viz.debate.debateArguments;
import be.webdeb.presentation.web.views.html.viz.debate.debateArgumentsByArguments;
import be.webdeb.presentation.web.views.html.viz.debate.debateArgumentsByAuthors;
import be.webdeb.presentation.web.views.html.viz.debate.debatePositions;
import be.webdeb.presentation.web.views.html.viz.common.contributionArgumentsDragnDrop;
import be.webdeb.presentation.web.views.html.viz.citation.citationList;
import be.webdeb.presentation.web.views.html.viz.citation.citationListModal;
import be.webdeb.presentation.web.views.html.viz.citation.citationConcurrentList;
import play.data.Form;
import play.data.validation.ValidationError;
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
 * Debate-related actions, ie controller of all pages to edit debates
 *
 * @author Martin Rouffiange
 */
public class DebateActions extends CommonController {

  @Inject
  private TextActions textzActions;

  /**
   * Display the page to edit the debate properties
   *
   * @param id the debate id to which this debate comes from
   * @return the property page with given debate data
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> edit(Long id) {
    logger.debug("GET debate properties of " + id);
    return editDebate(id, false, null);
  }

  /**
   * Display the page to edit the debate properties from a modal
   *
   * @param id the debate id to which this debate comes from
   * @param fromContribution a contribution id from where we add the citation
   * @return the property page with given debate data
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editFromModal(Long id, Long fromContribution) {
    logger.debug("GET debate modal properties of " + id);
    return editDebate(id, true, fromContribution);
  }

  /**
   * Update or create a debate
   *
   * @param id a debate id (-1 if new one)
   * @return the debate form object if it contains errors or debate data page.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> save(Long id) {
    logger.debug("POST save properties of " + id);
    Form<DebateForm> form = formFactory.form(DebateForm.class).bindFromRequest();
    try {
      DebateForm result = saveDebate(request(), form);

      // all good -> go to this actor's page (and remove session key_goto if any)
      ctx().flash().put(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"argument.properties.added"));
      return sendRedirectTo(be.webdeb.presentation.web.controllers.viz.routes.VizActions.debate(result.getId(), -1, 0).toString());

    } catch (DebateNotSavedException e) {
      return handleDebateError(e, false);
    }
  }

  /**
   * Update or create a debate
   *
   * @param id a debate id (-1 if new one)
   * @return the debate form object if it contains errors or debate data page.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveFromModal(Long id) {
    logger.debug("POST save properties of " + id);

    Form<DebateForm> form = formFactory.form(DebateForm.class).bindFromRequest();

    try {
      DebateForm result = saveDebate(request(), form);
      ctx().flash().put(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"debate.new.added"));
      return sendOk(be.webdeb.presentation.web.controllers.viz.routes.VizActions.debate(result.getId(), -1, 0).url());

    } catch (DebateNotSavedException e) {
      return handleDebateError(e,true);
    }
  }

  /*
   * AJAX calls
   */

  /**
   * Validate a step of the debate form
   *
   * @return ok if the current step of the form is valid. The form fields with errors otherwise.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> validateStep() {
    Form<DebateForm> form = formFactory.form(DebateForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
      return handleDebateError(new DebateNotSavedException(form, DebateNotSavedException.ERROR_FORM), true);
    }

    DebateForm debateForm = form.get();

    if(debateForm.getStepNum() == 0 && values.isBlank(debateForm.getId())) {
      List<DebateHolder> candidates = helper.toDebatesHolders(debateFactory.findByTitleAndShade(debateForm.getTitle(), debateForm.getShade(), lang,0, 30), user, lang, true);

      return handleDebateError(new DebateNotSavedException(form, DebateNotSavedException.HAS_CANDIDATES, candidates), true);
    }

    return sendOk(editDebateFields.render(formFactory.form(DebateForm.class).fill(form.get()), null, helper, user, new HashMap<>()));
  }

  /**
   * Search for a debate based on given query.
   *
   * @param term      the values to search for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a jsonified list of debate holders
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchDebate(String term, int fromIndex, int toIndex) {
    logger.debug("search for debate : " + term);

    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    List<DebateHolder> result = new ArrayList<>();
    List<Map.Entry<EQueryKey, String>> query = new ArrayList<>();

    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.DEBATE.id())));
    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.DEBATE_TITLE, term));
    //query.add(new AbstractMap.SimpleEntry<>(EQueryKey.GROUP, String.valueOf(user.getGroupId())));

    try {
      executor.searchContributions(query, fromIndex, toIndex).stream().filter(sessionHelper.getUser(ctx())::mayView).forEach(c -> {
        result.add(helper.toDebateHolder((Debate) c, user, lang, false));
      });
    } catch (BadQueryException e) {
      logger.warn("unable to search for actors with given term " + term, e);
    }
    return sendOk(Json.toJson(result));
  }

  /**
   * Get all linked contributions by type
   *
   * @param debateId the debate id
   * @param panes a comma separated EPane that need to be loaded
   * @param pov for a specific contains in pane, -1 if none
   * @return a jsonified list of debate holders
   */
  public CompletionStage<Result> getLinkedContributions(Long debateId, String panes, Long pov) {
    logger.debug("GET linked contributions for debate " + debateId + " for panes " + panes + " and pov " + pov);
    Debate debate = debateFactory.retrieve(debateId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    List<EDebateVizPane> panesList = new ArrayList<>();
    final int fpov = pov != null ? pov.intValue() : 0;

    if(!values.isBlank(panes)) {
      String splitted[] = panes.split(";");
      for(int iPane = 0; iPane < splitted.length; iPane++){
        EDebateVizPane pane = EDebateVizPane.value(Integer.decode(splitted[iPane]));
        if(pane != null) {
          panesList.add(pane);
        }
      }
    }

    if(debate != null && !panes.isEmpty()) {
      DebateVizHolder debateVizHolder = new DebateVizHolder(debate, user, lang);
      Map<Integer, String> response = new HashMap<>();

      for(EDebateVizPane pane : panesList) {
        switch (pane) {
          case ARGUMENTS:
            switch (fpov) {
              case -1 :
                if(!user.mayView(debate) || !textFactory.contributionCanBeEdited(user.getId(), debateId, user.getGroupId())) {
                  return sendUnauthorizedContribution();
                }
                response.put(pane.id(), contributionArgumentsDragnDrop.render(debateVizHolder, null, user).toString());
                break;
              case 1 :
                response.put(pane.id(), debateArgumentsByAuthors.render(debateVizHolder, user, helper).toString());
                break;
              default:
                response.put(pane.id(), debateArgumentsByArguments.render(debateVizHolder, user, false).toString());
                //response.put(pane.id(), debateArguments.render(debateVizHolder, user, false, fpov, helper).toString());
            }
            break;
          case SOCIOGRAPHY:
            if(debateVizHolder.isMultipleDebate()) {
              if(fpov > ESocioGroupKey.getAll().size()) {
                response.put(pane.id(), contributionAlliesOpponents.render(debateVizHolder, user, helper, EAlliesOpponentsType.POSITIONS, -1L, pov, debateVizHolder.getEShade()).toString());
              } else {
                response.put(pane.id(), debatePositions.render(debateVizHolder, user, helper, -1L, debateVizHolder.getEShade()).toString());
              }
            } else {
              response.put(pane.id(), contributionAlliesOpponents.render(debateVizHolder, user, helper, EAlliesOpponentsType.POSITIONS, pov, -1L, debateVizHolder.getEShade()).toString());
            }
            break;
          case BIBLIOGRAPHY:
            response.put(pane.id(), contributionBibliography.render(debateVizHolder.getId(), EContributionType.DEBATE, debateVizHolder.getTextsMap(), debateVizHolder.getTextsCitations(), user).toString());
            break;
        }
      }

      return sendOk(Json.toJson(response));
    }

    return sendBadRequest();
  }

  /**
   * Get the modal with all citations in the context contribution for the given actor
   *
   * @param context a context contribution id
   * @param actor an actor id
   * @return the modal with all citations from given actor in given context, or bad request if context is unknown
   */
  public CompletionStage<Result> getActorCitationInContext(Long context, Long actor) {
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(context);

    if(context != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

     return sendOk(citationListModal.render(
              justificationsToCitationsMap(contextContribution.getActorCitationJustifications(actor), user),
              contextContribution.getContributionTitle(lang),
              user,
             null,
             true
              ,null,
             context,
             null));
    }

    return sendBadRequest();
  }

  /**
   * Get the modal with all citations in the context contribution that come from given text
   *
   * @param context a context contribution id
   * @param text a text id
   * @return the modal with all citations from given actor in given context, or bad request if context is unknown
   */
  public CompletionStage<Result> getTextCitationInContext(Long context, Long text) {
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(context);

    if(context != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();
      EDebateShade debateShade = contextContribution.getType() == EContributionType.DEBATE ? ((Debate) contextContribution).getEShade() : null;

      Map<Integer, List<CitationHolder>> citations = contextContribution.getType() != EContributionType.TAG ?
              justificationsToCitationsMap(
                      contextContribution.getTextCitationJustifications(text),
                      user
              ) :
              transformToCitationsMap(
                      ((Tag) contextContribution).getCitationsFromText(text),
                      user,
                      lang);

      Map<Integer, List<CitationHolder>> othersCitations = positionsToCitationsMap(
              contextContribution.getTextCitationPositions(text),
              user);
      
      return sendOk(citationListModal.render(
              citations,
              contextContribution.getContributionTitle(ctx().lang().code()),
              user,
              null,
              true,
              debateShade,
              context,
              othersCitations));
    }

    return sendBadRequest();
  }

  /**
   * Get the modal with all citations in the context contribution for the given sociography key and key value
   *
   * @param context a context contribution id
   * @param subContext a sub context contribution id
   * @param key a ESocioGroupKey key id
   * @param value the key value
   * @param shade a shade if needed
   * @param isArgument true for arguments, false for positions
   * @return the modal with all citations from given actor in given context, or bad request if context is unknown
   */
  public CompletionStage<Result> getSociographyCitationInContext(Long context, Long subContext, int key, Long value, int shade, boolean isArgument) {
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(context);
    ESocioGroupKey eKey = ESocioGroupKey.value(key);

    if(context != null && eKey != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      ContextContributionHolder holder = new ContextContributionHolder(contextContribution, user, lang);

      return sendOk(citationListModal.render(
              holder.sociographyCitations(subContext, eKey, value, isArgument),
              null,
              user,
              shade == -1 ? null : shade,
              isArgument,
              contextContribution.getType() == EContributionType.DEBATE ? ((Debate)contextContribution).getEShade() : null,
              contextContribution.getType() == EContributionType.DEBATE ? context : null,
              null));
    }

    return sendBadRequest();
  }

  /**
   * Get the modal to add a given text to given debate
   *
   * @param debate a debate id
   * @return the corresponding modal if everything is ok, 400 is the request is malformed
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getAddTextToDebateModal(Long debate) {
    Debate d = debateFactory.retrieve(debate);

    if(d != null){
      WebdebUser user = sessionHelper.getUser(ctx());

      if(!user.mayView(d) || !textFactory.contributionCanBeEdited(user.getId(), debate, user.getGroupId())) {
        return sendUnauthorizedContribution();
      }

      String lang = ctx().lang().code();
      DebateHolder holder = helper.toDebateHolder(d, user, lang, true);

      TextForm textForm = new TextForm(lang);
      // set default group
      textForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
      // set current language
      textForm.setLang(ctx().lang().code());
      // set default visibility to private
      textForm.setTextVisibility(String.valueOf(ETextVisibility.PEDAGOGIC.id()));
      // set mayViewContent to true by default (otherwise form will be blocked)
      textForm.setMayViewContent(true);

      return sendOk(textSelectionModal.render(formFactory.form(TextForm.class).fill(textForm), holder, helper, user));
    }

    return sendBadRequest();
  }

  /**
   * Add a given text to given debate
   *
   * @param debate a debate id
   * @param text a text id
   * @return ok if everything is ok, 400 is the request is malformed, 500 if an error occurred.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> addTextToDebate(Long debate, Long text) {
    Debate d = debateFactory.retrieve(debate);
    Text t;

    if(values.isBlank(text)) {
      Form<TextForm> form = formFactory.form(TextForm.class).bindFromRequest();
      try {
        text = textzActions.saveText(request(), form).getId();

      } catch (TextActions.TextNotSavedException e) {
        return textzActions.handleTextError(e, false);
      }
    }

    t = textFactory.retrieve(text);

    if(d != null && t != null){
      WebdebUser user = sessionHelper.getUser(ctx());

      if(!user.mayView(d) || !textFactory.contributionCanBeEdited(user.getId(), debate, user.getGroupId())) {
        return sendUnauthorizedContribution();
      }

      DebateHasText dht = debateFactory.getDebateHasText();

      try {
        dht.setOriginId(debate);
        dht.setDestinationId(text);
        dht.save(user.getId(), user.getGroupId());
      } catch (FormatException | PermissionException | PersistenceException e) {
        logger.debug("error while saving debate has text " + e);
        return sendInternalServerError();
      }

      return sendOk();
    }

    return sendBadRequest();
  }

  /**
   * Get the concurrent citations list of the main actor compared to given key / value actor(s) in given context / sub context
   *
   * @param context a context contribution id
   * @param subContext a sub context contribution id
   * @param actor the main actor id
   * @param key a ESocioGroupKey key id
   * @param value the key value
   * @param isArgument true for arguments, false for positions
   * @return the concurrent citations list
   */
  public CompletionStage<Result> getSociographyCitationsConcurrent(Long context, Long subContext, Long actor, int key, Long value, boolean isArgument) {
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(context);
    Actor a = actorFactory.retrieve(actor);
    ESocioGroupKey eKey = ESocioGroupKey.value(key);

    if(context != null && a != null && eKey != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      ContextContributionHolder holder = new ContextContributionHolder(contextContribution, user, lang);

      return sendOk(citationConcurrentList.render(
              holder.sociographyCitations(subContext, ESocioGroupKey.AUTHOR, actor, isArgument),
              holder.sociographyCitations(subContext, eKey, value, isArgument),
              a.getFullname(lang),
              helper.getCompleteActorSocioName(eKey, value, lang),
              user,
              null,
              isArgument,
              contextContribution.getType() == EContributionType.DEBATE ? ((Debate)contextContribution).getEShade() : null));
    }

    return sendBadRequest();
  }

  /**
   * Move an element of the context arguments structure
   *
   * @param linkId the link to change
   * @param newSubContextId the new tag sub context id for the given link
   * @param newCategoryId the new tag category id for the given link
   * @param newSuperArgumentId the new super argument id for the given link
   * @param newShade the new link shade id for the given link
   * @param order the order of the link in the context
   * @return ok if the structure if correctly changed, bad request otherwise
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> changeContextArgumentStructure(Long linkId, Long newSubContextId, Long newCategoryId, Long newSuperArgumentId, int newShade, boolean newLink, int order) {
    logger.debug("Change context arguments structure for link (" + (newLink ? "nouveau lien" : "modification") + ") " + linkId + " sub context " + newSubContextId + " category " + newCategoryId + " super argument " + newSuperArgumentId + " shade " + newShade + " order " + order);
    WebdebUser user = sessionHelper.getUser(ctx());
    Contribution link = debateFactory.retrieve(linkId, EContributionType.ALL);
    boolean noError = false;

    if(link != null){

      switch (link.getContributionType().getEType()){
        case ARGUMENT_JUSTIFICATION:
        case CITATION_JUSTIFICATION:
        case CONTEXT_HAS_CATEGORY:
          ContextContribution context = textFactory.retrieveContextContribution(((ContributionLink) link).getOriginId());

          if(context == null) {
            return sendBadRequest();
          }

          if(!user.mayView(context) || !textFactory.contributionCanBeEdited(user.getId(), context.getId(), user.getGroupId())) {
            return sendUnauthorizedContribution();
          }
          break;

      }

      Tag newSubContext = tagFactory.retrieve(newSubContextId);
      Tag newCategory = tagFactory.retrieve(newCategoryId);
      Argument newSuperArgument = argumentFactory.retrieve(newSuperArgumentId);

      switch (link.getContributionType().getEType()){
        case ARGUMENT_JUSTIFICATION:
          ArgumentJustification argumentJustification = (ArgumentJustification) link;
          noError = !argumentFactory.argumentJustificationLinkAlreadyExists(argumentJustification.getDestinationId(), newSubContextId, newCategory != null ? newCategory.getId() : null, newSuperArgument != null ? newSuperArgument.getId() : null, argumentJustification.getDestinationId(), newShade);
          newShade = newShade == -1 ? argumentJustification.getLinkType().getType() : newShade;
          if(noError)
            noError = treatArgumentJustificationLink((ArgumentJustification) link, newSubContext, newCategory, newSuperArgument, newShade, newLink, order, user);
          break;
        case CITATION_JUSTIFICATION:
          noError = treatJustificationLinkChange((CitationJustification) link, newSubContext, newCategory, newSuperArgument, newShade, newLink, order, user);
          break;
        case CITATION:
          noError = treatTagLinkChange((Citation) link, newSubContext, newCategory, user);
          break;
        case CONTEXT_HAS_CATEGORY:
          noError = treatContextHasCategoryLinkChange((ContextHasCategory) link, order, user);
          break;
        default:
          logger.debug(link.getContributionType().getEType() + " can't change context arguments structure for that kind of link.");
      }
    }

    return noError ? sendOk() : sendBadRequest();
  }

  private boolean treatArgumentJustificationLink(ArgumentJustification argumentLink, ContextContribution newContext, Tag newCategory, Argument newSuperArgument, int newShade, boolean newLink, int order, WebdebUser user){
    boolean noError = true;

    List<ArgumentJustification> argumentsLinks = argumentFactory.findArgumentLinks(argumentLink.getOriginId(), argumentLink.getSubContextId(), argumentLink.getTagCategoryId(), argumentLink.getDestinationId(), argumentLink.getLinkType().getType());
    for(int iALink = 0; iALink < argumentsLinks.size() && noError; iALink++) {
      noError = treatArgumentJustificationLink(argumentsLinks.get(iALink), newContext, newCategory, argumentLink.getArgument(), newShade, newLink, iALink, user);
    }

    List<CitationJustification> citationLinks = citationFactory.findCitationLinks(argumentLink.getOriginId(), argumentLink.getSubContextId(), argumentLink.getTagCategoryId(), argumentLink.getDestinationId(), argumentLink.getLinkType().getType());
    for(int iCLink = 0; iCLink < citationLinks.size() && noError; iCLink++) {
      noError = treatJustificationLinkChange(citationLinks.get(iCLink), newContext, newCategory, argumentLink.getArgument(), newShade, newLink, iCLink, user);
    }

    if(noError)
      noError = treatJustificationLinkChange(argumentLink, newContext, newCategory, newSuperArgument, newShade, newLink, order, user);

    return noError;
  }

  private boolean treatJustificationLinkChange(JustificationLink justificationLink, ContextContribution newContext, Tag newCategory, Argument newSuperArgument, int newShade, boolean newLink, int order, WebdebUser user){

    if(newLink){
      justificationLink.setId(-1L);
    }

    if(newContext != null){
      justificationLink.setContext(newContext);
    }

    if(newCategory != null && newCategory.getTagType().getEType() == ETagType.CATEGORY_TAG){
      justificationLink.setTagCategory((TagCategory) newCategory);
    }

    justificationLink.setSuperArgument(newSuperArgument);

    if(EJustificationLinkShade.value(newShade) != null){
      try {
        justificationLink.setLinkType(argumentFactory.getJustificationLinkType(newShade));
      } catch (FormatException e) {
        logger.debug(newShade + " shade is unknown in change context arguments structure.");
      }
    }

    justificationLink.setOrder(order);

    try {
      justificationLink.save(user.getId(), user.getGroupId());
      return true;
    } catch (FormatException | PermissionException | PersistenceException e) {
      logger.warn("Can't save change context arguments structure. " + e);
      return false;
    }

  }

  private boolean treatTagLinkChange(Citation citation, Tag oldTag, Tag newTag, WebdebUser user){

    if(citation.getTags().stream().noneMatch(t -> t.getId().equals(newTag.getId()))) {

      citation.getTags().removeIf(t -> t.getId().equals(oldTag.getId()));
      citation.getTags().add(newTag);

      try {
        citation.save(user.getId(), user.getGroupId());
      } catch (FormatException | PermissionException | PersistenceException e) {
        logger.warn("Can't save change of tag for citation. " + e);
        return false;
      }
    }

    return true;
  }

  private boolean treatContextHasCategoryLinkChange(ContextHasCategory contextHasCategory, int order, WebdebUser user){

    contextHasCategory.setOrder(order);

    try {
      contextHasCategory.save(user.getId(), user.getGroupId());
      return true;
    } catch (FormatException | PermissionException | PersistenceException e) {
      logger.warn("Can't save change context arguments structure. " + e);
      return false;
    }

  }

  /**
   * Get the arguments structure of the given context
   *
   * @param context a context contribution
   * @return the context arguments structure if given context exists
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getContextArgumentStructure(Long context) {
    ContextContribution contextContribution = debateFactory.retrieveContextContribution(context);

    if(context != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();
      StructureClassifier classifier =
              new StructureClassifier(contextContribution.getId(), null, contextContribution.getContributionTitle(lang), -1);
      ContextContributionHolder holder = new ContextContributionHolder(contextContribution, user, lang);
      List<StructureClassifier> classifiers = new ArrayList<>();

      switch (contextContribution.getType()) {
        case DEBATE :
          if (holder.isMultipleDebate()) {
            holder.getTagDebates().forEach(debate -> {
              StructureClassifier subDebate = new StructureClassifier(debate.getId(), debate.getLinkId(), debate.getFullTitle(), -1);
              subDebate.getCategories().addAll(getDebateStructureCategories(debate));
              classifiers.add(subDebate);
            });
          } else {
            classifier.getCategories().addAll(getDebateStructureCategories(holder));
            classifiers.add(classifier);
          }
        break;
        case TEXT :
          classifier.getCategories().addAll(getTextStructureCategories(holder));
          classifiers.add(classifier);
        break;
        case TAG :
          TagHolder tag = helper.toTagHolder(tagFactory.retrieve(context), user, lang, false);
          classifier.getCategories().addAll(getTagStructureCategories(tag, user));
          classifiers.add(classifier);
        break;
      }

      return sendOk(Json.toJson(classifiers));
    }

    return sendBadRequest();
  }

  /**
   * Change the given justification link shade
   *
   * @param link a justification link id
   * @return ok if the link shade is well changed, badrequest otherwise
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> changeJustificationShade(Long link) {
    Contribution contribution = debateFactory.retrieve(link, EContributionType.ALL);

    if(contribution != null && contribution.getContributionType().getEType().isJustificationLink()) {
      WebdebUser user = sessionHelper.getUser(ctx());
      JustificationLink justificationLink = (JustificationLink) contribution;

      if(justificationLink.getContext() == null) {
        return sendBadRequest();
      }

      if(!user.mayView(justificationLink.getContext()) || !textFactory.contributionCanBeEdited(user.getId(), justificationLink.getOriginId(), user.getGroupId())) {
        return sendUnauthorizedContribution();
      }

      try {
        justificationLink.setLinkType(argumentFactory.getJustificationLinkType(justificationLink.getLinkType().getEType().reverse().id()));
        justificationLink.save(user.getId(), user.getGroupId());
        return sendOk();
      } catch (Exception e) {
        logger.debug("Change justification shade error...");
      }
    }

    return sendBadRequest();
  }

  private List<StructureCategory> getDebateStructureCategories(ContextContributionHolder holder){
    List<StructureCategory> categories = new ArrayList<>();

    holder.getCategories().forEach(categoryLink ->
      categories.add(getDebateStructureCategory(categoryLink.getDestinationId(), categoryLink.getId(), categoryLink.getDestinationTitle(), categoryLink.getOrder(), null, categoryLink.getCategory()))
    );

    categories.add(getDebateStructureCategory(-1L, null, i18n.get(ctx().lang(), "debate.categorty.unclassified"), -1, holder, null));

    return categories;
  }

  private StructureCategory getDebateStructureCategory(Long id, Long linkId, String name, int order, ContextContributionHolder contextHolder, TagCategoryHolder categoryHolder){
    StructureCategory category = new StructureCategory(id, linkId, name, order);

    EJustificationLinkShade.valuesAsList().forEach(shade -> {

      List<StructureArgument> arguments = new ArrayList<>();

      (contextHolder == null ? categoryHolder.getArguments(shade) : contextHolder.getArguments(shade)).forEach(argumentLink -> {
        StructureArgument argument = new StructureArgument(argumentLink.getDestinationId(), argumentLink.getId(), argumentLink.getDestinationTitle(), argumentLink.getOrder());
        argument.getCitations().addAll(linksToStructureCitations(argumentLink.getArgument().getCitations()));
        arguments.add(argument);
      });

      StructureArgument argument = new StructureArgument(-1L, null, i18n.get(ctx().lang(), "argument.fake.title"), -1);
      argument.getCitations().addAll(linksToStructureCitations((contextHolder == null ? categoryHolder.getCitations(shade) : contextHolder.getCitations(shade))));
      arguments.add(argument);

      category.getArgumentsMap().get(shade).addAll(arguments);
    });

    return category;
  }


  private List<StructureCategory> getTextStructureCategories(ContextContributionHolder holder){
    List<StructureCategory> categories = new ArrayList<>();

    holder.getArguments().forEach(argumentLink ->
            categories.add(getTextStructureCategory(argumentLink.getDestinationId(), argumentLink.getId(), argumentLink.getDestinationTitle(), argumentLink.getArgument()))
    );

    return categories;
  }

  private StructureCategory getTextStructureCategory(Long id, Long linkId, String name, ArgumentHolder argumentHolder){
    StructureCategory category = new StructureCategory(id, linkId, name, -1);

    category.getArguments().addAll(getArgumentStructure(argumentHolder));

    return category;
  }

  private List<StructureArgument> getArgumentStructure(ArgumentHolder argumentHolder) {
    List<StructureArgument> arguments = new ArrayList<>();

    argumentHolder.getArguments().forEach(argumentLink -> {
      StructureArgument argument = new StructureArgument(argumentLink.getDestinationId(), argumentLink.getId(), argumentLink.getDestinationTitle(), argumentLink.getOrder(), argumentLink.getEShade());
      argument.getArguments().addAll(getArgumentStructure(argumentLink.getArgument()));
      argument.getCitations().addAll(linksToStructureCitations(argumentLink.getArgument().getCitations()));
      arguments.add(argument);
    });

    return arguments;
  }

  private List<StructureCategory> getTagStructureCategories(TagHolder holder, WebdebUser user){
    List<StructureCategory> categories = new ArrayList<>();

    StructureCategory category = new StructureCategory(holder.getId(), -1L, holder.getTagName(), -1);
    addCitationsToCategory(category, holder.getAllCitations());
    categories.add(category);

    holder.getParents().forEach(tag -> categories.add(getTagStructureCategory(tag, true, user)));

    holder.getChildren().forEach(tag -> categories.add(getTagStructureCategory(tag, false, user)));

    return categories;
  }

  private StructureCategory getTagStructureCategory( SimpleTagForm tag, boolean isParent, WebdebUser user){
    StructureCategory category = new StructureCategory(tag.getTagId(), tag.getLinkId(), tag.getName(), -1, isParent);

    addCitationsToCategory(category, tag.getCitations(user));

    return category;
  }

  private void addCitationsToCategory(StructureCategory category, List<CitationHolder> citations){
    category.getCitations().addAll(citations
            .stream()
            .map(citation -> new StructureCitation(citation.getId(), citation.getId(), citation.getWorkingExcerpt(), -1))
            .collect(Collectors.toList()));
  }

  private List<StructureCitation> linksToStructureCitations(List<CitationJustificationLinkForm> links){
    return links.stream()
            .map(link -> new StructureCitation(link.getCitation().getId(), link.getId(), link.getCitation().getWorkingExcerpt(), link.getOrder()))
            .collect(Collectors.toList());
  }

  /*
   * PRIVATE HELPERS
   */

  private Map<Integer, List<CitationHolder>> justificationsToCitationsMap(List<CitationJustification> justifications, WebdebUser user){
    Map<Integer, List<CitationHolder>> citationsMap = new LinkedHashMap<>();
    Set<Long> citationsSet = new HashSet<>();
    justifications = justifications.parallelStream().filter(user::mayView).collect(Collectors.toList());

    if(!justifications.isEmpty()) {
      String lang = ctx().lang().code();

      EJustificationLinkShade.valuesAsList().forEach(shade ->
        citationsMap.put(shade.id(), new ArrayList<>())
      );

      justifications.forEach(justification -> {
        if (!citationsSet.contains(justification.getDestinationId())) {
          citationsSet.add(justification.getDestinationId());
          citationsMap.get(justification.getLinkType().getType())
                  .add(helper.toCitationHolder(justification.getCitation(), user, lang, false));
        }
      });
    }

    return citationsMap;
  }

  private Map<Integer, List<CitationHolder>> positionsToCitationsMap(List<CitationPosition> positions, WebdebUser user){
    Map<Integer, List<CitationHolder>> citationsMap = new LinkedHashMap<>();
    Set<Long> citationsSet = new HashSet<>();
    positions = positions.parallelStream().filter(user::mayView).collect(Collectors.toList());

    if(!positions.isEmpty()) {
      String lang = ctx().lang().code();

      positions.forEach(position -> {
        if(!citationsMap.containsKey(position.getLinkType().getType())) {
          citationsMap.put(position.getLinkType().getType(), new ArrayList<>());
        }
        if (!citationsSet.contains(position.getDestinationId())) {
          citationsSet.add(position.getDestinationId());
          citationsMap.get(position.getLinkType().getType())
                  .add(helper.toCitationHolder(position.getCitation(), user, lang, false));
        }
      });
    }

    return citationsMap;
  }

  /**
   * Edit a debate from a modal or not.
   *
   * @param id a debate id
   * @param fromContribution a contribution id from where we add the citation
   * @return bad request if citation not found, unauthorize if user must not see this debate, the edit form if all is ok.
   */
  private CompletionStage<Result> editDebate(Long id, boolean fromModal, Long fromContribution) {
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    deleteNewLinkKeys();

    // prepare wrapper for citation
    DebateForm debateForm;
    Debate debate = debateFactory.retrieve(id);

    if (debate == null) {
      debateForm = new DebateForm(user, lang);

      Contribution contribution = textFactory.retrieveContribution(fromContribution);

      if(contribution != null) {

        switch (contribution.getContributionType().getEType()) {
          case ARGUMENT_JUSTIFICATION:
            debateForm = new DebateForm((ArgumentJustification) contribution, user, lang);
            break;
          case ARGUMENT:
            if(((Argument) contribution).getArgumentType() == EArgumentType.SHADED) {
              debateForm = new DebateForm((ArgumentShaded) contribution, user, lang);
            }
            break;
          case TAG:
            debateForm.getTags().add(new SimpleTagForm((Tag) contribution, lang));
            break;
        }
      }

    } else {
      if (!user.mayView(debate) || !textFactory.contributionCanBeEdited(user.getId(), debate.getId(), user.getGroupId())) {
        return sendUnauthorizedContribution();
      }

      debateForm = new DebateForm(debate, user, lang);
    }

    return sendToDebateEdit(debateForm, fromModal);
  }

  /**
   * Send to edit debate form
   *
   * @param debateForm the debate to edit
   * @return the debate edit page or modal.
   */
  private CompletionStage<Result> sendToDebateEdit(DebateForm debateForm, boolean fromModal) {
    debateForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    debateForm.setLang(ctx().lang().code());

    // display the page of citation edition
    Form<DebateForm> form = formFactory.form(DebateForm.class).fill(debateForm);
    sessionHelper.set(ctx(), SessionHelper.KEY_GOTO, sessionHelper.getReferer(ctx()));

    return CompletableFuture.supplyAsync(() ->
            ok(fromModal ? editDebateModal.render(form, helper, sessionHelper.getUser(ctx()), null) :
                    editDebate.render(form, helper, sessionHelper.getUser(ctx()), null)), context.current());
  }

  /**
   * Save a debate from a given form.
   *
   * @param form an debate form object that may contain errors
   * @return given (updated) actor form
   * @throws DebateNotSavedException if an error exist in passed form or any error arisen from save action
   */
  private synchronized DebateForm saveDebate(Http.Request request, Form<DebateForm> form) throws DebateNotSavedException {
    File file = getFileFromRequest(request);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
      // save picture in temp fs
      files.saveToCache(file, form.data().get("pictureString"));
      throw new DebateNotSavedException(form, DebateNotSavedException.ERROR_FORM);
    }

    DebateForm debate = form.get();

    // second check because of step form (full check, to avoid js modification or problem)
    Map<String, List<ValidationError>> errors = checkStepFormErrors(debate);
    if (errors != null) {
      logger.debug("form has errors " + errors);
      // save picture in temp fs
      files.saveToCache(file, form.data().get("pictureString"));
      throw new DebateNotSavedException(form, DebateNotSavedException.ERROR_FORM);
    }

    NameMatch<ActorHolder> match = helper.searchForNameMatches("authors", debate.getActors(), user, ctx().lang().code());
    if (!match.isEmpty()) {
      // save picture in temp fs
      files.saveToCache(file, form.data().get("pictureString"));
      throw new DebateNotSavedException(form.fill(debate), DebateNotSavedException.AUTHOR_NAME_MATCH, match);
    }

    debate.setPlaces(savePlaces(debate.getPlaces(), true));

    // try to save in DB
    try {
      treatSaveContribution(debate.save(user.getContributor().getId()));

      if(debate.getArgumentJustificationId() != null) {
        ArgumentJustification justification = argumentFactory.retrieveJustificationLink(debate.getArgumentJustificationId());

        if(justification != null) {
          ContextHasSubDebate link = debateFactory.getContextHasSubDebate();

          link.setOriginId(justification.getContext().getId());
          link.setDestinationId(debate.getId());
          link.setArgumentJustificationLinkId(debate.getArgumentJustificationId());
          link.save(user.getContributor().getId(), user.getGroupId());
        }
      }
    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to save debate", e);
      throw new DebateNotSavedException(form, DebateNotSavedException.ERROR_DB, i18n.get(ctx().lang(), e.getMessage()));
    }

    // save picture if any
    savePictureFile(debate.getPictureString(), debate.getId(), file, true);

    return debate;
  }

  /**
   * Handle error on debate form submission and returns the actor form view (depending on the switch).
   * If an unknown error occurred, either a "goto" page or the general entry view is returned.
   *
   * @param exception the exception raised from unsuccessful save
   * @param onlyFields switch to know if the argumentContextDiv views must be returned
   * @return if the form contains error, a bad request (400) response is returned with, if onlyfield, the
   * editDebateFields template or the editDebate full form otherwise. In case of possible author name matches,
   * a 409 response is returned with the modal frame to select among possible matches.In case of possible
   * tag name matches, a 410 response is returned with the modal frame to select among possible matches.
   * If another error occurred, a redirect to either a "goto" session-cached url or the main entry page.
   */
  private CompletionStage<Result> handleDebateError(DebateNotSavedException exception, boolean onlyFields) {
    Map<String, String> messages = new HashMap<>();
    Form<DebateForm> form = exception.form;
    WebdebUser user = sessionHelper.getUser(ctx());
    DebateForm debate;

    switch (exception.error) {
      case DebateNotSavedException.AUTHOR_NAME_MATCH:
        if(exception.match != null && !exception.match.isEmpty()) {
          return CompletableFuture.supplyAsync(() ->
                          status(409, handleNameMatches.render(exception.match.getNameMatches(), exception.match.isActor(), exception.match.getIndex(), "", null, values))
                  , context.current());
        }

      case DebateNotSavedException.ERROR_FORM:
        // error in form, just resend it
        if(!onlyFields)
          flash(SessionHelper.WARNING, "error.form");
        return CompletableFuture.supplyAsync(() -> onlyFields ?
                badRequest(editDebateFields.render(form, null, helper, user, messages))
                : badRequest(editDebate.render(form, helper, sessionHelper.getUser(ctx()), messages)), context.current());

      case DebateNotSavedException.HAS_CANDIDATES:
          DebateForm debateForm = form.get();
          debateForm.setStepNum(1);
          debateForm.setHasNoExistingPropositions(exception.candidates.isEmpty());

          return CompletableFuture.supplyAsync(() ->
                  status(406, editDebateFields.render(form.fill(debateForm), exception.candidates, helper, user, messages))
                  , context.current());

      default:
        // any other error, check where do we have to go after and show message in exception
        if(!onlyFields)
          flash(SessionHelper.ERROR, "error.crash");
        return onlyFields ? CompletableFuture.supplyAsync(() ->
                internalServerError(message.render(messages)), context.current())
                : sendRedirectTo(routes.EntryActions.contribute().toString());
    }
  }

  /**
   * Inner class to handle debate exception when an debate cannot be saved from private save execution
   */
  private class DebateNotSavedException extends Exception {

    private static final long serialVersionUID = 1L;
    final Form<DebateForm> form;
    final NameMatch<ActorHolder> match;
    final List<DebateHolder> candidates;
    final int error;

    static final int ERROR_FORM = 0;
    static final int AUTHOR_NAME_MATCH = 1;
    static final int ERROR_DB = 2;
    static final int HAS_CANDIDATES = 3;

    DebateNotSavedException(Form<DebateForm> form, int error) {
      this.error = error;
      this.form = form;
      this.match = null;
      this.candidates = null;
    }

    DebateNotSavedException(Form<DebateForm> form, int error, String message) {
      super(message);
      this.error = error;
      this.form = form;
      this.match = null;
      this.candidates = null;
    }

    DebateNotSavedException(Form<DebateForm> form, int error, NameMatch<ActorHolder> match) {
      this.error = error;
      this.form = form;
      this.match = match;
      this.candidates = null;
    }

    DebateNotSavedException(Form<DebateForm> form, int error, List<DebateHolder> candidates) {
      this.error = error;
      this.form = form;
      this.match = null;
      this.candidates = candidates;
    }
  }


}
