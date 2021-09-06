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

package be.webdeb.presentation.web.controllers.entry;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.account.AdvicesForm;
import be.webdeb.presentation.web.controllers.browse.SearchForm;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.entry.contribute;
import be.webdeb.presentation.web.views.html.entry.contributionSelection;
import be.webdeb.presentation.web.views.html.oops.oops;
import be.webdeb.presentation.web.views.html.viz.admin.mergeContributionModal;
import be.webdeb.presentation.web.views.html.viz.admin.contributionHistoryModal;
import be.webdeb.presentation.web.views.html.oops.privateContribution;
import be.webdeb.presentation.web.views.html.viz.place.placesModal;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * New contributions management (main menu) and cross-cutting contribution management regarding
 * contribution history, deletion, general search auto-completion, etc.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class EntryActions extends CommonController {

  @Inject
  private FormFactory formFactory;

  /**
   * Main page that describe how to contribute on webdeb
   *
   * @return the main page with the available possibilities for a contribution
   */
  public Result contribute() {
    AdvicesForm advicesForms = new AdvicesForm(contributorFactory.getAdvices(), ctx().lang().code());
    return ok(contribute.render(advicesForms, sessionHelper.getUser(ctx())));
  }

  /**
   * Main router to edit a contribution
   *
   * @return redirect to right edit page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public Result edit(Long id) {

    Contribution contribution = textFactory.retrieve(id, EContributionType.ALL);
    if (contribution != null) {

      switch (contribution.getContributionType().getEType()) {
        case ACTOR:
          return redirect(be.webdeb.presentation.web.controllers.entry.actor.routes.ActorActions.edit(id));
        case DEBATE:
          return redirect(be.webdeb.presentation.web.controllers.entry.debate.routes.DebateActions.edit(id));
        case TEXT:
          return redirect(be.webdeb.presentation.web.controllers.entry.text.routes.TextActions.edit(id));
        case TAG:
          return redirect(be.webdeb.presentation.web.controllers.entry.tag.routes.TagActions.edit(id, -1L));
        case CITATION:
          return redirect(be.webdeb.presentation.web.controllers.entry.citation.routes.CitationActions.edit(id));
        default:
          logger.error("wrong contribution type retrieved for contribution " + id + ", should not happen");
      }
    }
    logger.error("no contribution retrieved for contribution " + id);
    return notFound(oops.render("contribution edition page for id=" + id, sessionHelper.getUser(ctx())));
  }

  /**
   * Cancel link creation request, i.e. remove cached new link from user session
   *
   * @return true in a json structure
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> abortLinkCreation() {
    logger.debug("GET abort link creation");
    deleteNewLinkKeys();
    sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
    return sendOk();
  }

  /**
   * Search Contributions by given term
   *
   * @param term a term to look for
   * @param group a group to look in (if a public group is passed, all public groups are queried)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of jsonified ContributionHolders containing the given term
   */
  public CompletionStage<Result> searchContributions(String term, int group, int fromIndex, int toIndex) {
    return CompletableFuture.supplyAsync(() ->
        ok(Json.toJson(helper.toHolders(textFactory.findByValue(term, group, fromIndex, toIndex),
            sessionHelper.getUser(ctx()), ctx().lang().code()))),
        context.current());
  }

  /**
   * Remove given contribution from given group, will be totally removed if it does not belong to any other
   * group or if the force parameter is true
   *
   * @param id a contribution id
   * @param type its type
   * @param group the group id from which this contribution must be removed from
   * @param force true if this contribution must be permanently removed from all groups
   * @param noChange true if contribution must be deleted and not changed (like with links)
   * @return either this contribution page if the deletion concerned only one of its groups or if it failed,
   * the dashboard page if it was deleted (with a flash message)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> delete(Long id, int type, int group, boolean force, boolean noChange, boolean async) {
    logger.debug("POST delete " + id + " of type " + type + " force" + force + " no change " + noChange);
    WebdebUser user = sessionHelper.getUser(ctx());

    // does contribution exists?
    EContributionType cType = EContributionType.value(type);
    if (cType == null) {
      logger.error("given contribution type is invalid " + type);
      if(!async) flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.delete.fail"));
      return CompletableFuture.supplyAsync(() -> async ? badRequest() : redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
    }
    Contribution contribution = textFactory.retrieve(id, cType);
    if (contribution == null) {
      logger.error("no such contribution with id " + id + " and type " + cType.name());
      if(!async) flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.delete.fail"));
      return CompletableFuture.supplyAsync(() -> async ? badRequest() : redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
    }

    // does group exists and user has rights on this contribution
    GroupSubscription subscription = user.belongsTo(group);
    if (!user.isAdminOf(contribution)) {
      logger.error("user " + user.getContributor().getEmail() + " may not delete " + id);
      if(!async) flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.delete.fail"));
      return CompletableFuture.supplyAsync(() -> async ? badRequest() :
              redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(id, type, 0, 0)), context.current());
    }

    // now remove contribution
    try {
      if (force && group == contributorFactory.getDefaultGroup().getGroup().getGroupId()) {
        // remove from all groups
        contribution.remove(user.getId(), noChange);
        if(!async) flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "viz.admin.delete.success"));
        return CompletableFuture.supplyAsync(() -> async ? ok() : redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
      } else {
        // remove from given group
        contribution.removeFromGroup(group, user.getId());
        if(!async) flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "viz.admin.remove.group.success", subscription.getGroup().getGroupName()));

        // if this group was its only group, go to dashboard, otherwise go to this viz
        if (textFactory.retrieve(id, cType) != null) {
          return CompletableFuture.supplyAsync(() -> async ? ok() :
                  redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(id, type, 0, 0)), context.current());
        }
        return CompletableFuture.supplyAsync(() -> async ? ok() :
                redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
      }

    } catch (PersistenceException | PermissionException e) {
      logger.error("unable to delete contribution " + contribution.toString(), e);
      if(!async) flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.delete.fail"));
      return CompletableFuture.supplyAsync(() -> async ? badRequest() :
              redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(id, type, 0, 0)), context.current());
    }
  }

  /**
   * Ask the modal frame to search for contribution to be merged with given contribution
   *
   * @param id a contribution id
   * @param type a contribution type
   * @return the modal frame to search for a contribution to be merged  with this contribution
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getMergeContributionsModal(Long id, int type) {
    logger.debug("GET merge for " + id + " of type " + type);
    WebdebUser user = sessionHelper.getUser(ctx());
    EContributionType cType = EContributionType.value(type);
    if (cType == null) {
      logger.error("given contribution type is invalid " + type);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.merge.fail"));
      return CompletableFuture.supplyAsync(() -> redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
    }
    Contribution contribution = textFactory.retrieve(id, cType);
    if (contribution == null) {
      logger.error("no such contribution with id " + id + " and type " + cType.name());
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.merge..fail"));
      return CompletableFuture.supplyAsync(() -> redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
    }

    if(!contribution.getContributionType().getEType().isAlwaysPublic() && !user.mayView(contribution)){
      return CompletableFuture.completedFuture(Results.unauthorized(
              privateContribution.render(user)));
    }

    // init search query (reuse query from search form
    SearchForm query = new SearchForm();
    query.setAllContributionsFlags(false);
    query.setInGroup(user.getGroup().getGroupId());
    return CompletableFuture.completedFuture(ok(
        mergeContributionModal.render(helper.toHolder(contribution, user, ctx().lang().code()),
            formFactory.form(SearchForm.class).fill(query), user)
    ));
  }

  /**
   * Merge given origin contribution into given replacement contribution
   *
   * @param origin a contribution id to be merged into given replacement
   * @param replacement a replacement contribution id
   * @param type their contribution type id
   * @return either the visualization page of the replacement contribution if the merge succeeded, or
   * the origin viz page if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> doMerge(Long origin, Long replacement, int type) {
    logger.debug("POST merge for " + origin + " by " + replacement + " of type " + type);
    if (origin != null && origin.equals(replacement)) {
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.merge.same"));
      return CompletableFuture.supplyAsync(() ->
          redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(origin, type, 0, 0)), context.current());
    }
    boolean originDeleted;
    try {
      originDeleted = textFactory.merge(origin, replacement, sessionHelper.getUser(ctx()).getId());
    } catch (PermissionException | PersistenceException e) {
      logger.error("unable to merge contributions", e);
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.admin.merge.fail"));
      if (origin == null) {
        return CompletableFuture.supplyAsync(() ->
            redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1)), context.current());
      } else {
        return CompletableFuture.supplyAsync(() ->
            redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(origin, type, 0, 0)), context.current());
      }
    }
    flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "viz.admin.merge.success"));
    return CompletableFuture.supplyAsync(() ->
        redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(originDeleted ? replacement : origin, type, 0, 0)), context.current());
  }


  /**
   * Get modal page to display the history ofa contribution in a very rough manner
   *
   * @param id a contribution id
   * @param type a contribution type id
   * @return either the modal page with the stringified history of given contribution, or an empty badRequest if
   * given contribution does not exist
   */
  public CompletionStage<Result> getContributionHistoryModal(Long id, int type) {
    logger.debug("GET history for " + id + " of type " + type);
    WebdebUser user = sessionHelper.getUser(ctx());
    EContributionType cType = EContributionType.value(type);
    if (cType == null) {
      logger.error("given contribution type is invalid " + type);
      return CompletableFuture.supplyAsync(Results::badRequest);
    }
    Contribution contribution = textFactory.retrieve(id, cType);
    if (contribution == null) {
      logger.error("no such contribution with id " + id + " and type " + cType.name());
      return CompletableFuture.supplyAsync(Results::badRequest);
    }

    if(!contribution.getContributionType().getEType().isAlwaysPublic() && !user.mayView(contribution)){
      return CompletableFuture.completedFuture(Results.unauthorized(
              privateContribution.render(user)));
    }

    // get history of this contribution
    return sendOk(contributionHistoryModal.render("", contribution.getId(), contribution.getContributionType().getEType(), textFactory.getHistory(id), values, user));
  }

  /**
   * Get all linked places to a given contribution
   *
   * @param id a contribution id
   * @param selectedPlace the selected place by the user if any
   */
  public CompletionStage<Result> getContributionPlaces(Long id, Long selectedPlace) {
    logger.debug("GET all places linked to contribution " + id);
    List<PlaceForm> places = textFactory.getContributionsPlaces(id).stream()
            .map(e -> new PlaceForm(e, ctx().lang().code())).collect(Collectors.toList());
    return CompletableFuture.supplyAsync(() ->
            ok(placesModal.render(places, selectedPlace)), context.current());
  }


  /**
   * Get modal frame to search and select after an existing contribution
   *
   * @param contributionType a given contribution
   * @return the contributionSelection page
   */
  public CompletionStage<Result> contributionSelection(int contributionType) {
    logger.debug("GET contribution selection modal to seardch and select a contribution of type : " + contributionType);

    EContributionType type =
            EContributionType.value(contributionType) == null ? EContributionType.ALL : EContributionType.value(contributionType);
    // init search query (reuse query from search form)
    SearchForm query = new SearchForm(type);

    return CompletableFuture.supplyAsync(() ->
                    ok(contributionSelection.render(formFactory.form(SearchForm.class).fill(query), type)),
            context.current());
  }

}
