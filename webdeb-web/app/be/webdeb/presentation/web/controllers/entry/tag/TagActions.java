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

package be.webdeb.presentation.web.controllers.entry.tag;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.model.Group;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.routes;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.EActionType;
import be.webdeb.presentation.web.controllers.viz.actor.ActorVizHolder;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;
import be.webdeb.presentation.web.controllers.viz.tag.ETagVizPane;
import be.webdeb.presentation.web.controllers.viz.tag.TagVizHolder;
import be.webdeb.presentation.web.views.html.oops.privateContribution;
import be.webdeb.presentation.web.views.html.entry.tag.editTag;
import be.webdeb.presentation.web.views.html.entry.tag.editTagModal;
import be.webdeb.presentation.web.views.html.entry.tag.editTagFields;
import be.webdeb.presentation.web.views.html.util.message;
import be.webdeb.presentation.web.views.html.viz.common.contributionAlliesOpponents;
import be.webdeb.presentation.web.views.html.viz.common.contributionArgumentsDragnDrop;
import be.webdeb.presentation.web.views.html.viz.tag.tagDebates;
import be.webdeb.presentation.web.views.html.viz.tag.tagArguments;
import be.webdeb.presentation.web.views.html.viz.tag.util.tagContainerList;
import be.webdeb.presentation.web.views.html.viz.debate.util.debateListModal;
import be.webdeb.presentation.web.views.html.viz.debate.util.debateContainerList;
import be.webdeb.presentation.web.views.html.viz.citation.citationListModal;
import be.webdeb.presentation.web.views.html.viz.citation.citationContainerList;
import be.webdeb.presentation.web.views.html.viz.common.contributionBibliography;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * Tag-related actions, ie controller of all pages to edit tags
 *
 * @author Martin Rouffiange
 */
public class TagActions extends CommonController {

  @Inject
  protected RequestProxy proxy;

  private static final String NOTFOUND = "tag.notfound";

  /**
   * Ask page to add a new tag category
   *
   * @param contextId       a contribution context id
   * @param asCategory if the tag must be added as category, false if it is a sub debate of a multiple debate
   * @return the modal to complete the argument
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> newTagCategory(Long contextId, boolean asCategory) {
    logger.debug("GET new tag category");

    WebdebUser user = sessionHelper.getUser(ctx());
    ContextContribution context = tagFactory.retrieveContextContribution(contextId);

    if(context == null){
      return sendBadRequest();
    }

    if(!user.mayView(context) || !textFactory.contributionCanBeEdited(user.getId(), contextId, user.getGroupId())) {
      return sendUnauthorizedContribution();
    }

    String title = i18n.get(ctx().lang(), asCategory ? "entry.tag.new.category" : "entry.tag.new.thesis");
    TagForm tagForm = new TagForm(contextId, asCategory ? ETagType.CATEGORY_TAG : ETagType.SUB_DEBATE_TAG, ctx().lang().code());
    tagForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    tagForm.setFormTitle(title);
    Form<TagForm> form = formFactory.form(TagForm.class).fill(tagForm);
    // display first page of argument edition
    return sendOk(editTagModal.render(form, helper, sessionHelper.getUser(ctx()), null));
  }

  /**
   * Get the tag edition page for given text id and context id (if needed)
   *
   * @param id a tag id (-1 or null for a new tag)
   * @param contextId a context id if needed
   * @return the tag form to either add a new tag or modify an existing one
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> edit(Long id, Long contextId) {
    logger.debug("GET edit tag page " + id + " and context " + contextId);
    return editTag(id, contextId, false, null, false);
  }

  /**
   * Get the tag edition page for given text id and context id (if needed)
   *
   * @param id a tag id (-1 or null for a new tag)
   * @param contextId a context id if needed
   * @param fromContribution a contribution id from where we add the citation
   * @param asParent true if the fromContribution is a tag, and that tag must be added as parent
   * @return the tag form in a modal to either add a new tag or modify an existing one
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editFromModal(Long id, Long contextId, Long fromContribution, boolean asParent) {
    logger.debug("GET edit tag from modal " + id + " and context " + contextId);
    return editTag(id, contextId, true, fromContribution, asParent);
  }

  /**
   * Update or save a tag into the database.
   *
   * @param id the tag id (-1 of null for a new tag)
   * @return either the addTag page if form contains error, if it appears to already exist
   * its visualisation page if the action succeeded, or redirect to main entry page if an error occurred.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public synchronized CompletionStage<Result> save(Long id) {
    logger.debug("POST save tag " + id);
    Form<TagForm> form = formFactory.form(TagForm.class).bindFromRequest();
    try {
      TagForm result = saveTag(form);

      // all good -> go to this tag's page (and remove session key_goto if any)
      sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"entry.tag.save") + " " + result.getTagName());
      return sendRedirectTo(be.webdeb.presentation.web.controllers.viz.routes.VizActions.tag(result.getId(), -1, 0).toString());

    } catch (TagNotSavedException e) {
      return handleTagError(e, false);
    }
  }

  /**
   * Update or create a tag from a modal form
   *
   * @return either the addTagFields form if the form contains errors (code 400),
   * the modal page with next (autocreated) tag to be added or a confirmation message if the action succeeded
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveFromModal() {
    logger.debug("POST save tag from modal");

    Form<TagForm> form = formFactory.form(TagForm.class).bindFromRequest();

    try {
      TagForm tagForm = saveTag(form);

      // get next modal, if any
      return sendOk();

    } catch (TagNotSavedException e) {
      return handleTagError(e, true);
    }
  }

  /**
   * Save a link between two tags
   *
   * @param parent the parent tag
   * @param child the child tag
   *
   * @return redirect to either the cached goto key or the referer page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveTagLink(Long parent, Long child) {
    logger.info("GET save tag link direct between " + parent + " and " + child);

    String goTo = sessionHelper.get(ctx(), SessionHelper.KEY_GOTO);
    if (goTo == null) {
      goTo = sessionHelper.getReferer(ctx());
    }
    String finalGoto = goTo;
    sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);

    Tag p = tagFactory.retrieve(parent);
    Tag c = tagFactory.retrieve(child);
    if (p == null || c == null) {
      logger.error("a given reference is null or unfound");
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),NOTFOUND));
      return CompletableFuture.supplyAsync(() -> redirect(finalGoto), context.current());
    }

    // check if no such link exists
    if (tagFactory.retrieveLink(parent, child) != null) {
      logger.warn("such tag link already exist between " + parent + " " + child);
      flash(SessionHelper.WARNING, i18n.get(ctx().lang(),"tag.link.alreadyexist"));
      return CompletableFuture.supplyAsync(() -> redirect(finalGoto), context.current());
    }

    try {
      // set up link fields and save
      TagLink link = tagFactory.getTagLink();
      int group = Group.getPublicGroup().getIdGroup();
      link.setParent(p);
      link.setChild(c);
      link.addInGroup(group);
      link.save(sessionHelper.getUser(ctx()).getContributor().getId(), group);
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"tag.link.added"));
    } catch (FormatException | PersistenceException | PermissionException e) {

      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),"error.crash"));
      logger.error("unable to save new tag link", e);
    }

    // redirect to final goto page (as stored in session)
    return CompletableFuture.supplyAsync(() -> redirect(finalGoto), context.current());
  }

  /*
   * AJAX calls
   */

  /**
   * Search for a tag based on given query.
   *
   * @param term the values to search for
   * @param type the type of tag
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a jsonified list of tag holders
   */
  public CompletionStage<Result> searchTag(String term, int type, int fromIndex, int toIndex) {
    List<SimpleTagForm> result = new ArrayList<>();
    String lang = ctx().lang().code();

    for(Contribution c : tagFactory.findByName(term).stream()
            .filter(tag -> type == -1 || type == tag.getTagType().getType())
            .limit(toIndex)
            .collect(Collectors.toList())) {

      result.add(new SimpleTagForm((Tag) c, lang));
    }

    return sendOk(Json.toJson(result));
  }

  /**
   * Find the tag hierarchy of the given tag
   *
   * @param tag a tag id
   * @param forParents true for the parents of the given tag
   * @param forContributionType a contribution type if tags must be sorted by linked contributions of given type
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return the list of tag hierarchy, or badrequest if given tag is unkown
   */
  public CompletionStage<Result> findHierarchy(Long tag, boolean forParents, int forContributionType, int fromIndex, int toIndex, String filters) {
    Tag tagContribution = tagFactory.retrieve(tag);
    EContributionType cType = EContributionType.value(forContributionType);

    if(tagContribution != null) {
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();
      TagHolder tagHolder = helper.toTagHolder(tagContribution, user, lang, false);
      EActionType actionType = cType == EContributionType.CITATION ? EActionType.CITATION : EActionType.DEBATE;

      return sendOk(forParents ?
              tagContainerList.render(tagHolder.getParentHolders(cType, fromIndex, toIndex, filters), user, true, actionType, cType == EContributionType.CITATION ? ETagVizPane.ARGUMENTS.id() : ETagVizPane.DEBATES.id()) :
              tagContainerList.render(tagHolder.getChildHolders(cType, fromIndex, toIndex, filters), user, true, actionType, cType == EContributionType.CITATION ? ETagVizPane.ARGUMENTS.id() : ETagVizPane.DEBATES.id()));
    }

    return sendBadRequest();
  }

  /**
   * Get all linked contributions by type
   *
   * @param tagId the tag id
   * @param panes a comma separated EPane that need to be loaded
   * @param pov for a specific contains in pane, -1 if none
   * @return a jsonified list of debate holders
   */
  public CompletionStage<Result> getLinkedContributions(Long tagId, String panes, Integer pov) {
    logger.debug("GET linked contributions for tag " + tagId + " for panes " + panes + " and pov " + pov);
    Tag tag = tagFactory.retrieve(tagId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    List<ETagVizPane> panesList = new ArrayList<>();
    final Integer fpov = pov != null ? pov : 0;

    if(!values.isBlank(panes)) {
      String splitted[] = panes.split(";");
      for(int iPane = 0; iPane < splitted.length; iPane++){
        ETagVizPane pane = ETagVizPane.value(Integer.decode(splitted[iPane]));
        if(pane != null) {
          panesList.add(pane);
        }
      }
    }

    if(tag != null && !panes.isEmpty()) {
      TagVizHolder tagVizHolder = new TagVizHolder(tag, user, lang);
      Map<Integer, String> response = new HashMap<>();

      panesList.forEach(pane -> {
        switch (pane) {
          case DEBATES:
            //response.put(pane.id(), tagDebates.render(tagVizHolder, user).toString());
            break;
          case ARGUMENTS:
            if(fpov < 0){
              response.put(pane.id(), contributionArgumentsDragnDrop.render(tagVizHolder, null, user).toString());
            }else{
              //response.put(pane.id(), tagArguments.render(tagVizHolder, user).toString());
            }
            break;
          case ACTORS:
            response.put(pane.id(), contributionAlliesOpponents.render(tagVizHolder, user, helper, EAlliesOpponentsType.TAGS, fpov.longValue(), -1L, null).toString());
            break;
          case BIBLIOGRAPHY:
            response.put(pane.id(), contributionBibliography.render(tagVizHolder.getId(), EContributionType.TAG, tagVizHolder.getTextsMap(), tagVizHolder.getTextsCitations(), user).toString());
            break;
        }
      });

      return sendOk(Json.toJson(response));
    }

    return sendBadRequest();
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Handle error on tag form submission and returns the tag form view (depending on the switch).
   * If an unknown error occurred, either a "goto" page or the general entry view is returned.
   *
   * @param exception the exception raised from unsuccessful save
   * @param onlyFields switch to know if the addTagFields or addTag views must be returned
   * @return if the form contains error, a bad request (400) response is returned with, if onlyfield, the
   * editTagFields template or the editTag full form otherwise. In case of possible name matches, a
   * 409 response is returned with the modal frame to select among possible matches. If another error occurred,
   * a redirect to either a "goto" session-cached url or the main entry page.
   */
  private CompletionStage<Result> handleTagError(TagNotSavedException exception, boolean onlyFields) {
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    Form<TagForm> form = exception.form;

    switch (exception.error) {

      case TagNotSavedException.ERROR_FORM:
        // error in form, just resend it
        if(!onlyFields)
          flash(SessionHelper.WARNING, "error.form");
        return CompletableFuture.supplyAsync(() -> onlyFields ?
            badRequest(editTagFields.render(form, helper, user, messages))
            : badRequest(editTag.render(form, helper, sessionHelper.getUser(ctx()), messages)), context.current());

      default:
        if(!onlyFields)
          flash(SessionHelper.ERROR, exception.getMessage());
        return onlyFields ?
              CompletableFuture.supplyAsync(() -> internalServerError(message.render(messages)), context.current())
              : sendRedirectTo(routes.EntryActions.contribute().toString());
    }
  }

  /**
   * Edit a tag from a modal or not.
   *
   * @param tagId a tag id
   * @param contextId a context id if needed
   * @param fromContribution a contribution id from where we add the citation
   * @param asParent true if the fromContribution is a tag, and that tag must be added as parent
   * @return bad request if citation not found, unauthorize if user must not see this tag, the edit form if all is ok.
   */
  private CompletionStage<Result> editTag(Long tagId, Long contextId, boolean fromModal, Long fromContribution, boolean asParent) {
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    String title;
    Map<String, String> messages = new HashMap<>();

    if(!values.isBlank(contextId)) {
      ContextContribution context = tagFactory.retrieveContextContribution(contextId);

      if (context == null) {
        return sendBadRequest();
      }

      if (!user.mayView(context) || !textFactory.contributionCanBeEdited(user.getId(), contextId, user.getGroupId())) {
        return sendUnauthorizedContribution();
      }
    }

    Tag tag = tagFactory.retrieve(tagId);
    TagForm form;

    if (tag != null) {

      form = new TagForm(tag, contextId, user, ctx().lang().code());

      switch(tag.getTagType().getEType()){
        case CATEGORY_TAG:
          title = i18n.get(ctx().lang(), "entry.tag.modify.category");
          break;
        case SUB_DEBATE_TAG:
          title = i18n.get(ctx().lang(), "entry.tag.modify.thesis");
          break;
        default:
          title = i18n.get(ctx().lang(), "entry.tag.modify");
      }
    } else {
      form = new TagForm(lang);

      Tag toAddInHierarchy = tagFactory.retrieve(fromContribution);

      if(toAddInHierarchy != null) {
        title = i18n.get(ctx().lang(), asParent ? "entry.tag.new.from.parent" : "entry.tag.new.from.child", toAddInHierarchy.getName(lang));
        (asParent ? form.getChildren() : form.getParents()).add(new SimpleTagForm(toAddInHierarchy, lang));
      } else {
        title = i18n.get(ctx().lang(), "entry.tag.new");
      }
    }

    form.setInGroup(Group.getPublicGroup().getIdGroup());
    form.setLang(ctx().lang().code());
    form.setFormTitle(title);

    return CompletableFuture.supplyAsync(() ->
            ok(fromModal ? editTagModal.render(formFactory.form(TagForm.class).fill(form), helper, user, messages) :
                    editTag.render(formFactory.form(TagForm.class).fill(form), helper, user, messages) ), context.current());
  }

  /**
   * Save a tag from a given form and add in session cookie the list of auto-created tags' id if any
   *
   * @param form an tag form object that may contain errors
   * @return given (updated) tag form
   * @throws TagNotSavedException if an error exist in passed form or any error arisen from save action
   */
  private synchronized TagForm saveTag(Form<TagForm> form) throws TagNotSavedException {
    // sends back form if there are some errors
    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors().toString());
      throw new TagNotSavedException(form, TagNotSavedException.ERROR_FORM);
    }

    WebdebUser user = sessionHelper.getUser(ctx());
    TagForm tag = form.get();

    // all good, let's save validated actor
    try {
      // save will return actors that have been automatically created
      tag.save(sessionHelper.getUser(ctx()).getContributor().getId(), user.getGroupId());
      // prepend this
    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to save tag", e);
      // both exceptions are sending message keys
      throw new TagNotSavedException(form, TagNotSavedException.ERROR_DB, i18n.get(ctx().lang(),e.getMessage()));
    }

    return tag;
  }

  /*
   * INNER CLASS
   */

  /**
   * Inner class to handle tag exception when a tag cannot be saved from private save execution
   */
  private class TagNotSavedException extends Exception {

    private static final long serialVersionUID = 1L;
    final Form<TagForm> form;
    final int error;

    static final int ERROR_FORM = 0;
    static final int ERROR_DB = 1;

    TagNotSavedException(Form<TagForm> form, int error) {
      this.error = error;
      this.form = form;
    }

    TagNotSavedException(Form<TagForm> form, int error, String message) {
      super(message);
      this.error = error;
      this.form = form;
    }
  }
}
