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

package be.webdeb.presentation.web.controllers.entry.actor;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.ActorAffiliations;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.infra.ws.nlp.WikidataExtractRequest;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.account.admin.ProfessionForm;
import be.webdeb.presentation.web.controllers.entry.*;
import be.webdeb.presentation.web.controllers.entry.citation.CitationActions;
import be.webdeb.presentation.web.controllers.entry.citation.CitationForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.routes;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.EActionType;
import be.webdeb.presentation.web.controllers.viz.EVizPane;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.viz.actor.ActorVizHolder;
import be.webdeb.presentation.web.controllers.viz.actor.EActorVizPane;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;
import be.webdeb.presentation.web.views.html.browse.simpleSearchResult;
import be.webdeb.presentation.web.views.html.entry.actor.editProfession;
import be.webdeb.presentation.web.views.html.account.admin.professions;
import be.webdeb.presentation.web.views.html.entry.actor.editActor;
import be.webdeb.presentation.web.views.html.entry.actor.editActorFields;
import be.webdeb.presentation.web.views.html.entry.actor.editActorModal;
import be.webdeb.presentation.web.views.html.entry.actor.addAffiliation;
import be.webdeb.presentation.web.views.html.entry.citation.editCitationFields;
import be.webdeb.presentation.web.views.html.util.handleNameMatches;
import be.webdeb.presentation.web.views.html.util.messagelike;
import be.webdeb.presentation.web.views.html.viz.actor.actorAffiliations;
import be.webdeb.presentation.web.views.html.viz.actor.actorCitations;
import be.webdeb.presentation.web.views.html.viz.actor.actorPositions;
import be.webdeb.presentation.web.views.html.viz.actor.util.actorCard;
import be.webdeb.presentation.web.views.html.viz.actor.actorDebatePositionsModal;
import be.webdeb.presentation.web.views.html.util.message;
import javax.inject.Inject;

import be.webdeb.presentation.web.views.html.viz.citation.citationListModal;
import be.webdeb.presentation.web.views.html.viz.citation.citationContainerList;

import be.webdeb.presentation.web.views.html.viz.common.contributionAlliesOpponents;
import be.webdeb.presentation.web.views.html.viz.common.contributionArgumentsDragnDrop;
import be.webdeb.presentation.web.views.html.viz.common.contributionBibliography;
import be.webdeb.presentation.web.views.html.viz.debate.debateArguments;
import be.webdeb.presentation.web.views.html.viz.debate.debatePositions;
import be.webdeb.presentation.web.views.html.viz.debate.util.debateContainerList;

import be.webdeb.presentation.web.views.html.viz.actor.util.actorContainerList;
import be.webdeb.presentation.web.views.html.viz.tag.util.tagContainerList;
import be.webdeb.presentation.web.views.html.viz.text.util.textContainerList;

import play.Configuration;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * This class handles all actions (html page handling) related to actors.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ActorActions extends CommonController {

  @Inject
  protected RequestProxy proxy;

  private ActorVizHolder viz = null;

  /**
   * Render the "create new author" single page (possibly with a pre-filled actor, if given id exists)
   *
   * @param id an actor id (-1 for new one)
   * @return either a blank form, or a pre-filled actor form if given id exists
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> edit(Long id) {
    logger.debug("GET edit actor " + id);
    return editActor(id, false);
  }

  /**
   * Get the actor edition modal for given actor id
   *
   * @param id an actor id (-1 for new one)
   * @return either a blank form, or a pre-filled actor form if given id exists
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editFromModal(Long id) {
    logger.debug("GET edit actor from modal " + id);
    return editActor(id, true);
  }

  /**
   * Update or create an actor from standard form
   *
   * @param id an actor id
   * @return actor form in error if any error in posted form (code 400) or a redirect to the created actor page in case of success
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> save(Long id) {
    logger.debug("POST save actor " + id);
    Form<ActorForm> form = formFactory.form(ActorForm.class).bindFromRequest();
    try {
      ActorForm result = saveActor(request(), form);

      // all good -> go to this actor's page (and remove session key_goto if any)
      sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"entry.actor.save") + " " + result.getFullname());
      return CompletableFuture.supplyAsync(() ->
          redirect(be.webdeb.presentation.web.controllers.viz.routes.VizActions.actor(result.getId(), EVizPane.CARTO.id(), 0)),
          context.current());

    } catch (ActorNotSavedException e) {
      return handleActorError(e, false);
    }
  }

  /**
   * Update or create an actor from a modal form
   *
   * @return either the addActorFields form if the form contains errors (code 400),
   * the modal page with next (autocreated) actor to be added or a confirmation message if the action succeeded
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveFromModal() {
    logger.debug("POST save actor from modal");

    Map<String, String> messages = new HashMap<>();
    Form<ActorForm> form = formFactory.form(ActorForm.class).bindFromRequest();

    try {
      ActorForm actorForm = saveActor(request(), form);
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"entry.actor.save") + " " + actorForm.getFullname());
      // remove actor from session (new ones have been added in saveActor)
      sessionHelper.removeValue(ctx(), SessionHelper.KEY_NEWACTOR, String.valueOf(actorForm.getId()));

      // get next modal, if any
      return sendOk(be.webdeb.presentation.web.controllers.viz.routes.VizActions.actor(actorForm.getId(), EVizPane.CARTO.id(), 0).toString());

    } catch (ActorNotSavedException e) {
      return handleActorError(e, true);
    }
  }

  /**
   * Validate a step of the actor form
   *
   * @return ok if the current step of the form is valid. The form fields with errors otherwise.
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> validateStep() {
    Form<ActorForm> form = formFactory.form(ActorForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
      return handleActorError(new ActorNotSavedException(form, ActorNotSavedException.ERROR_FORM), true);
    }

    return sendOk(editActorFields.render(formFactory.form(ActorForm.class).fill(form.get()), helper, new HashMap<>()));
  }

  /**
   * Upload a new picture for given actor
   *
   * @param id an actor id
   * @return the newly created avatar file name, or a message template if an error occurred (code 400)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> uploadActorPicture(Long id) {
    logger.debug("POST upload picture for actor " + id);
    Http.MultipartFormData<File> body = request().body().asMultipartFormData();
    Http.MultipartFormData.FilePart<File> picture = body.getFile("picture");
    Map<String, String> messages = new HashMap<>();
    messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "viz.actor.upload.pic.error"));

    if (picture == null) {
      logger.error("no actor id passed or no picture");
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    Actor actor = actorFactory.retrieve(id);
    if (actor == null) {
      logger.error("passed id is unfound "+ id);
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // check this picture is ok
    files.saveToCache(picture.getFile(), picture.getFilename());
    Http.Context ctx = ctx();

    return detector.isImageSafe(configuration.getString("server.hostname") + be.webdeb.presentation.web.controllers.routes.Application
        .getFile(picture.getFilename(), "tmp").url()).handleAsync((safe, t) -> {
      if (safe || t != null) {
        // save avatar extension in DB and file in FS
        if(actor.getAvatar() != null)
          actor.getAvatar().setExtension(picture.getFilename());
        try {
          actor.save(sessionHelper.getUser(ctx).getContributor().getId(), sessionHelper.getCurrentGroup(ctx));
        } catch (FormatException | PermissionException | PersistenceException e) {
          logger.error("unable to update avatar for " + actor.getId(), e);
          return badRequest(message.render(messages));
        }
        // retrieve name of avatar as stored in DB since it may have been adapted (avoid name conflicts)
        files.savePictureFile(picture.getFile(), actor.getAvatar() != null ? actor.getAvatar().getPictureFilename() : null, false);
        return ok(Json.toJson(actor.getAvatar()));
      } else {
        // unsafe image, warn user
        messages.put(SessionHelper.ERROR, i18n.get(ctx.lang(), "general.upload.pic.unsafe"));
        return badRequest(message.render(messages));
      }
    });
  }

  /**
   * Get modal page to add new affiliations for given actor
   *
   * @param id an actor id
   * @param isAffiliated true if the new affiliation must be affiliated
   * @param isPerson true if the new affiliation concerns a person, false for a organization
   * @return the modal page to add new affiliations, or a bad request (400) if no actor could be retrieved from given id
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> newAffiliations(Long id, boolean isAffiliated, boolean isPerson) {
    logger.debug("GET add affiliation modal for " + id + " is affiliated " + isAffiliated + " is person " + isPerson);

    Actor actor = actorFactory.retrieve(id);

    if (actor == null) {
      logger.error("unable to find actor " + id);
      return CompletableFuture.supplyAsync(Results::badRequest);
    }

    Boolean forPerson = isAffiliated && EActorType.ORGANIZATION.equals(actor.getActorType()) && !isPerson ? null : isPerson;
    ActorAffiliationForm form = new ActorAffiliationForm(actor, ctx().lang().code(), forPerson, isAffiliated);
    form.setLang(ctx().lang().code());

    return sendOk(addAffiliation.render(
                formFactory.form(ActorAffiliationForm.class).fill(form),
                actor.getActorType(),
                isPerson,
                isAffiliated,
                helper));
  }

  /**
   * Save a (list of) affiliation for given actor
   *
   * @return either the full modal page in case of form errors (400)
   * or a special 409 error with the modal page meant to deal with name matches
   * or the visualization page for given actor
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> addAffiliations(Long id, boolean isAffiliated, boolean isPerson) {
    logger.debug("POST add affiliation for " + id);
    Form<ActorAffiliationForm> form = formFactory.form(ActorAffiliationForm.class).bindFromRequest();
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors().toString());
      Actor actor = actorFactory.retrieve(id);
      return sendBadRequest(addAffiliation.render(
              form,
              actor.getActorType(),
              isPerson,
              isAffiliated,
              helper));
    }

    ActorAffiliationForm actor = form.get();
    // if actor.getId is blank => throw error
    if (values.isBlank(actor.getId())) {
      logger.error("unable to save new affiliation since actor had no id (should not happen");
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(),"error.crash"));
    } else {
      EActorType type = isAffiliated ? null : isPerson ? EActorType.PERSON : EActorType.ORGANIZATION;
      NameMatch<ActorHolder> namematch = helper.findAffiliationsNameMatches("affiliationsForm", actor.getAffiliationsForm(), type, user, lang);

      if (!namematch.isEmpty()) {
        // sends back name-matches modal to be rendered (with special 409 code)
        return CompletableFuture.supplyAsync(() ->
          status(409, handleNameMatches.render(namematch.getNameMatches(), false, namematch.getIndex(), namematch.getSelector(), null, values)),
            context.current());
      }

      sessionHelper.remove(ctx(), SessionHelper.KEY_NEWPROFESSION);
      List<Integer> newProfessions = helper.checkIfNewProfessionsMustBeCreated(
          helper.convertAffiliationFormToProfessionForm(actor.getAffiliationsForm(), lang));
      newProfessions.forEach(idP -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWPROFESSION, idP+""));

      try {
        // add those affiliations to actor and auto-created actors in session cache
        Map<Integer, List<Contribution>> toBeProposed = actor.save(sessionHelper.getUser(ctx()).getContributor().getId());
        if(toBeProposed.containsKey(EContributionType.ACTOR.id()))
          toBeProposed.get(EContributionType.ACTOR.id())
              .forEach(a -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWACTOR, a.getId().toString()));
        flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"viz.actor.affiliation.success", actor.getFullname()));

      } catch (FormatException | PermissionException | PersistenceException e) {
        logger.error("unable to save new affiliation for " + id, e);
        flash(SessionHelper.ERROR, i18n.get(ctx().lang(), e.getMessage()));
      }
    }

    return CompletableFuture.supplyAsync(() ->
        ok(be.webdeb.presentation.web.controllers.viz.routes.VizActions.actor(id, isPerson ? EVizPane.CARTO2.id() : EVizPane.CARTO.id(), 0).url()),
        context.current());
  }

  /**
   * Cancel the edition of an existing auto-created actor, i.e. remove its entry from session cache. All
   * other cached elements will be removed too (ie, cancel all)
   *
   * @return a rendered message to warn user about the cancellation
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> cancelFromModal() {
    logger.debug("user canceled auto creatred modal");
    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWACTOR);
    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWDEBATE);
    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWACTOR);
    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWPROFESSION);
    deleteNewLinkKeys();

    return CompletableFuture.completedFuture(ok(""));
  }

  /*
   * AJAX REQUESTS
   */

  /**
   * Search for an actor with given term of given type
   *
   * @param term searched term
   * @param type -1 for all types of actors, 0 for persons only, 1 for organization
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return an array of ActorSimpleForm that matches the given term and type (JSON)
   */
  public Result searchActor(String term, int type, int fromIndex, int toIndex) {
    logger.debug("search for " + term + " type " + type);
    List<ActorSimpleForm> result = new ArrayList<>();
    List<Map.Entry<EQueryKey, String>> query = new ArrayList<>();

    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.ACTOR.id())));
    query.add(new AbstractMap.SimpleEntry<>(EQueryKey.ACTOR_NAME, term));

    if(type > -1)
      query.add(new AbstractMap.SimpleEntry<>(EQueryKey.ACTOR_TYPE, type+""));
    try {
      executor.searchContributions(query, fromIndex, toIndex).stream().filter(sessionHelper.getUser(ctx())::mayView).forEach(c -> {
        result.add(new ActorSimpleForm((Actor) c, ctx().lang().code(), false));
      });
    } catch (BadQueryException e) {
      logger.warn("unable to search for actors with given term " + term, e);
    }
    return ok(Json.toJson(result));
  }

  public CompletionStage<Result> findAffiliations(Long actor, int viewBy, String filters) {
    Actor a = actorFactory.retrieve(actor);
    EActorType eviewBy = EActorType.value(viewBy);

    if(a != null && eviewBy != null && a.getActorType() != EActorType.UNKNOWN) {
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();
      ActorVizHolder viz = new ActorVizHolder(a, user, lang);
      List<AffiliationsObj> objs = new ArrayList<>();

      if(a.getActorType() == EActorType.ORGANIZATION && eviewBy == EActorType.ORGANIZATION) {
        objs.add(new AffiliationsObj(
                i18n.get(ctx().lang(), "viz.actor.carto.belongsto.n"),
                "add-affiliation",
                viz.getAffiliateds(filters)));
        objs.add(new AffiliationsObj(
                i18n.get(ctx().lang(), "viz.actor.carto.belongings.n"),
                "add-member",
                viz.getAffiliations(eviewBy, filters)));
      } else {
        objs.add(new AffiliationsObj(
                i18n.get(ctx().lang(), "viz.actor.desc.cartography." + viz.getActortype(), viz.getFullname()),
                "add-member",
                viz.getAffiliations(eviewBy, filters)));
      }

      return sendOk(Json.toJson(objs));
    }

    return sendBadRequest();
  }

  /**
   * Search for an actor with given term of given type
   *
   * @param term searched term
   * @return an array of String actor name + actor party
   */
  public Result searchPartyMembers(String term) {
    logger.debug("search for " + term);
    return ok(Json.toJson(actorFactory.findPartyMemberByName(term, ctx().lang().code())));
  }

  /**
   * Get a particular actor with the given id
   *
   * @param id an actor id
   * @return the actor form object corresponding to the given id, or a 401-unauthorized empty response
   */
  public CompletionStage<Result> getActor(Long id) {
    Actor actor = actorFactory.retrieve(id);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (sessionHelper.getUser(ctx()).mayView(actor)) {
      return CompletableFuture.supplyAsync(() -> ok(Json.toJson(new ActorForm(actor, user, ctx().lang().code()))), context.current());
    } else {
      return CompletableFuture.supplyAsync(() -> unauthorized(""), context.current());
    }
  }

  /**
   * Retrieve the list of functions (and affiliations) for a given actor
   *
   * @param id the actor id
   * @param type profession type, is for a formation affiliation or any other type of affiliation
   * @param term the term to search for
   *
   * @return the list of AffiliationWrapper containing the actor's functions and affiliations (JSON) and all other
   * professions containing given term
   */
  public CompletionStage<Result> getActorFunctions(Long id, int type, String term) {
    String lang = ctx().lang().code();
    List<AffiliationForm> result = new ArrayList<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    EProfessionType etype = EProfessionType.value(type) == null ? EProfessionType.OTHERS : EProfessionType.value(type);

    if (id != -1L && etype == EProfessionType.OTHERS) {
      Actor actor = actorFactory.retrieve(id);
      if (actor != null && sessionHelper.getUser(ctx()).mayView(actor)) {
        actor.getAffiliations().stream().filter(a ->
            a.getFullfunction(lang, false, true).contains(term)).forEach(a -> result.add(new AffiliationForm(a, user, lang)));
      }
    }
    if (!"".equals(term)) {
      actorFactory.findProfessions(term, lang, null, etype.id()).forEach(p ->
              result.add(new AffiliationForm(p, lang, null)));
    }
    //logger.debug(result+"//");
    return CompletableFuture.supplyAsync(() -> ok(Json.toJson(sessionHelper.sublist(result))), context.current());
  }

  /**
   * Call WDTAL-wikipedia service to retrieve the details of an actor. Whole response is cached
   *
   * @param type the actor type id (as defined in EActorType)
   * @param isUrl boolean saying if we look for an url or a name
   * @param value contains either an url (if isUrl = true) or a name to look for
   * @param optional an optional first name to look for (will be ignored if actortype = EActorType.ORGANIZATION.id or isUrl = true)
   * @return the Json content of the WDTAL call
   */
  public CompletionStage<Result> searchActorDetails(int type, boolean isUrl, String value, String optional) {
    logger.debug("GET search actor details for " + (optional != null ? optional + " ": "") + value + " of type " + type);
    Map<String, String> messages = new HashMap<>();
    Http.Context ctx = ctx();
    messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(), "nlp.actor.error"));

    // check given actortype
    EActorType actorType = EActorType.value(type);
    if (actorType == null){
      logger.warn("unable to call wikidata service since given type is invalid " + type);
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // construct valid request object to be serialized for rest service
    WikidataExtractRequest content;
    if (isUrl) {
      content = new WikidataExtractRequest(ctx.lang().code(), value, EActorType.value(type));
    } else {
      if (EActorType.PERSON.equals(actorType)) {
        content = new WikidataExtractRequest(ctx.lang().code(), optional, value);
      } else {
        content = new WikidataExtractRequest(ctx.lang().code(), value);
      }
    }
    // call wikidata service
    return proxy.getActorDetailsAsJson(content).thenApplyAsync(result -> {
      if (result != null && result.size() > 0) {
        logger.debug("result from WDTAL actor call: " + result.toString());
        return ok(Json.toJson(result));
      }
      // result does not look valid
      logger.debug("retrieve json object is empty or null");
      return badRequest(message.render(messages));
    }, context.current());
  }

  /**
   * Get a raw image from given url and save it into temp fs.
   *
   * @param url the url to get
   * @return the filename of the retrieve content saved in tempfs, or a bad request (400) with a detailed
   * message if an error occurred
   */
  public CompletionStage<Result> getPictureFile(String url) {
    Map<String, String> messages = new HashMap<>();
    Http.Context ctx = ctx();
    logger.debug("will save picture file to temp fs from " + url);
    if (values.isURL(url)) {
      String file = url.substring(url.lastIndexOf('/')).replace("%20", " ");
      if (file.contains("?")) {
        file = file.substring(0, file.lastIndexOf('?'));
      }
      String path = configuration.getString("cache.store.path") + values.stripAccents(file);
      return proxy.getImageFile(url, path).thenApplyAsync(result -> {
        if (result != null) {
          logger.debug("file has been successfully retrieved and saved under " + result.getAbsolutePath());
          return ok(Json.toJson(result.getName()));
        }

        messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),"nlp.text.error"));
        return badRequest(message.render(messages));
      }, context.current());
    }
    messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),"nlp.text.invalidurl"));
    return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
  }

  /**
   * Build a list of ActorForm with all actors present in session (auto-created actors from other contributions)
   *
   * @return the list of ActorForm containing all actors stored in session or an empty bad request (400) if none present
   */
  public CompletionStage<Result> getAutoCreatedActors() {
    if (!values.isBlank(sessionHelper.getUser(ctx()).getId())) {
      Html modal = getAutoCreatedActorModal(ctx(), null);
      return CompletableFuture.supplyAsync(() -> modal != null ? ok(modal) : badRequest(Json.toJson("")), context.current());
    }
    return CompletableFuture.completedFuture(badRequest(Json.toJson("")));
  }

  /**
   * Build a list of ProfessionEditForm with all professions present in session (auto-created professions from other contributions)
   *
   * @return the list of ProfessionEditForm containing all professions stored in session or an empty bad request (400) if none present
   */
  public CompletionStage<Result> getAutoCreatedProfessions() {
    if (!values.isBlank(sessionHelper.getUser(ctx()).getId())) {
      Html modal = getAutoCreatedProfessionModal(ctx(), null);
      return CompletableFuture.supplyAsync(() -> modal != null ? ok(modal) : badRequest(Json.toJson("")), context.current());
    }
    return CompletableFuture.completedFuture(badRequest(Json.toJson("")));
  }

  /**
   * Get an actor card from given id (return partial page)
   *
   * @param id an actor id
   * @return the contribution card partial page, or a 400 response with a message saying no actor could be retrieved
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getActorCard(Long id) {
    logger.debug("GET actor card for " + id);

    Actor actor = actorFactory.retrieve(id);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (actor == null) {
      Map<String, String> messages = new HashMap<>();
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(),"browse.search.noresult"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    return CompletableFuture.supplyAsync(() ->
        ok(actorCard.render(helper.toActorHolder(actor, user, ctx().lang().code(), false), true, values)), context.current());
  }

  /**
   * Find the list of citations of the given actor where he has the given actor role, displayed by contribution type
   *
   * @param id an actor id
   * @param role the actor role on the citation
   * @param type a contribution type to display by
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return the list of citation or contribution to display by, 400 if there is an error
   */
  public CompletionStage<Result> findActorCitations(Long id, int role, int type, int fromIndex, int toIndex, String filters) {
    //logger.debug("Find actor's citation for actor " + id + " with role " + role + " for ctype " + type + " from index " + fromIndex + " to " + toIndex);

    Actor actor = actorFactory.retrieve(id);
    EActorRole eRole = EActorRole.value(role);
    EContributionType ctype = EContributionType.value(type);

    if(actor != null && eRole != null && ctype != null) {
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      ActorVizHolder actorVizHolder = new ActorVizHolder(actor, user, lang);

      switch (ctype){
        case CITATION:
          return sendOk(citationContainerList.render(actorVizHolder.getCitations(eRole, filters, fromIndex, toIndex), user, true, false, false, null, -1, null));
        case ACTOR:
          return sendOk(actorContainerList.render(actorVizHolder.getActorsWhoQuotes(filters, fromIndex, toIndex), user, true, EActionType.CITATION, -1));
        case TEXT:
          return sendOk(textContainerList.render(actorVizHolder.getTextsCitations(eRole, filters, fromIndex, toIndex), user, true, EActionType.CITATION, -1));
        case TAG:
          return sendOk(tagContainerList.render(actorVizHolder.getTagsCitations(eRole, filters, fromIndex, toIndex), user, true, EActionType.CITATION, -1));
        case DEBATE:
          return sendOk(debateContainerList.render(actorVizHolder.getDebatesCitations(eRole, filters, fromIndex, toIndex), user, true, false, EActionType.CITATION, EDebateVizPane.SOCIOGRAPHY.id()));
      }
    }

    return sendBadRequest();
  }

  /*
   * PROFESSION MANAGEMENT FUNCTIONS
   */

  /**
   * Get professions found from given query (in their names)
   *
   * @param query a term to look for professions
   * @param idToIgnore an profession id to ignore
   * @param json return as json format
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the professions template filled with the results
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchProfessions(String query, Long idToIgnore, boolean json, int fromIndex, int toIndex) {
    String lang = ctx().lang().language();
    List<Profession> results = actorFactory.findProfessions(query, values.isBlank(idToIgnore) ? null : idToIgnore, lang, fromIndex, toIndex);

    return(!json ? CompletableFuture.supplyAsync(() ->
            ok(professions.render(lang, results)),
        context.current())
        : CompletableFuture.supplyAsync(() -> ok(Json.toJson(
            sessionHelper.sublist(results.stream().map(r -> new ProfessionForm(r.getId(), r.getType().id(), r.isDisplayHierarchy(), r.getName(lang))).collect(Collectors.toList())))),
            context.current()));
  }

  /**
   * Get the modal page for editing profession names
   *
   * @param professionId the id of a profession
   * @return the modal page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editProfession(int professionId) {
    logger.debug("GET modal page to edit profession names");
    String lang = ctx().lang().language();
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();
    try {
      Profession profession = retrieveProfessionAndDetermineSuperLink(professionId);
      // all good, send modal
      return CompletableFuture.supplyAsync(() ->
          ok(editProfession.render(formFactory.form(ProfessionEditForm.class).fill(new ProfessionEditForm(profession, lang, user.getERole().id())), helper, sessionHelper.getUser(ctx()), messages)), context.current());
    } catch (FormatException e) {
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.profession.error.notFound"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /**
   * Reset the actor type of a given actor. This operation also delete the linked person or organization from db
   *
   * @param id an actor id
   * @return the edit page for this actor
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
  public CompletionStage<Result> resetActorType(Long id) {
    logger.debug("Reset the actor type of " + id);
    actorFactory.resetActorType(id);
    return edit(id);
  }

  /**
   * Retrieve a profession with a given id and try to found a super-link if the given profession hasn't one
   *
   * @param professionId the id of a profession
   * @return the super link of the given profession
   */
  private Profession retrieveProfessionAndDetermineSuperLink(int professionId) throws FormatException{
    Profession profession = actorFactory.getProfession(professionId);
    if(profession != null && profession.getSuperLink() == null) {
      profession.setSuperLink(actorFactory.determineSuperLink(professionId));
    }
    return profession;
  }

  /**
   * Handle post request to edit profession names
   *
   * @return the edit profession modal if submitted form contains error or the adminProfession tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendEditProfession() {
    logger.debug("SEND the editing of a profession");
    Map<String, String> messages = new HashMap<>();
    List<String> ids = sessionHelper.getValues(ctx(), SessionHelper.KEY_NEWPROFESSION);

    // check form
    Form<ProfessionEditForm> professionForm = formFactory.form(ProfessionEditForm.class).bindFromRequest();
    if (professionForm.hasErrors()) {
      logger.debug("error in form " + professionForm.errors());
      messages.put(SessionHelper.ERROR, SessionHelper.ERROR_FORM);
      return CompletableFuture.supplyAsync(() -> badRequest(editProfession.render(professionForm, helper, sessionHelper.getUser(ctx()), messages)), context.current());
    }

    // save profession names and link
    ProfessionEditForm editForm = professionForm.get();
    int professionId = editForm.getProfessionId();
    try {
      Profession profession = actorFactory.createProfession(professionId, EProfessionType.value(editForm.getProfessionTypeId()), Boolean.parseBoolean(editForm.getDisplayHierarchy()), null);
      // ad a profession superlink to profession
      Profession superLink =  actorFactory.getProfession(editForm.getSuperProfessionId());
      if(superLink != null)
        profession.setSuperLink(superLink);

      // add names to profession to save
      for (ProfessionNameForm name : editForm.getProfessionNames()) {
        profession.addName(name.getName(), name.getLang(), name.getGender());
      }

      // save profession
      int id = actorFactory.saveProfession(profession);
      if(id > -1 && ids != null && !ids.isEmpty())
        sessionHelper.removeValue(ctx(), SessionHelper.KEY_NEWPROFESSION, ids.get(0));

      // check if we do not have messages (meaning all went smoothly)
      if (messages.isEmpty()) {
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"admin.edit.profession.ok") + " " + profession.getGendersNames(ctx().lang().language()));
      }
      if(ids != null && !ids.isEmpty()){
        Html modal = getAutoCreatedProfessionModal(ctx(), messages);
        sessionHelper.removeValue(ctx(), SessionHelper.KEY_NEWPROFESSION, ids.get(0));
        return CompletableFuture.supplyAsync(() ->
            modal != null ? ok(modal) : ok(message.render(messages)), context.current());
      }
      return CompletableFuture.supplyAsync(() ->
          ok(message.render(messages)), context.current());
    } catch (FormatException e) {
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "admin.profession.error.notFound"));
      return CompletableFuture.completedFuture(internalServerError(messagelike.render(messages)));
    }
  }

  /*
   * PRIVATE HELPER FUNCTIONS
   */

  /**
   * Edit an actor from a modal or not.
   *
   * @param actorId an actor id
   * @param fromModal true if it is from a modal
   * @return bad request if actor not found, unauthorize if user must not see this actor, the edit form if all is ok.
   */
  private CompletionStage<Result> editActor(Long actorId, boolean fromModal) {
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    Actor actor = actorFactory.retrieve(actorId);
    ActorForm form;

    if (actor != null) {
      form = new ActorForm(actor, user, lang);
      form.setInGroup(sessionHelper.getCurrentGroup(ctx()));

    } else {
      // set group value in form
      form = new ActorForm();
      form.setInGroup(sessionHelper.getCurrentGroup(ctx()));
      form.setLang(ctx().lang().code());
    }

    return sendOk(fromModal ? editActorModal.render(formFactory.form(ActorForm.class).fill(form), true, helper, user, messages) :
            editActor.render(formFactory.form(ActorForm.class).fill(form), helper, user, messages));
  }

  /**
   * Helper method to build an actor modal frame
   *
   * @param context the HTTP request context
   * @param messages messages to be shown on modal frame
   * @return the add actor modal frame if there are still auto-created actors, null otherwise
   */
  private Html getAutoCreatedActorModal(Http.Context context, Map<String, String> messages) {
    // check session cache for possible actors to be filled by user (automatic creation
    List<String> ids = sessionHelper.getValues(context, SessionHelper.KEY_NEWACTOR);
    WebdebUser user = sessionHelper.getUser(ctx());

    if (ids != null && !ids.isEmpty()) {
      logger.debug("will ask details about " + ids.get(0));
      try {
        Actor a = actorFactory.retrieve(Long.parseLong(ids.get(0)));
        if (a != null) {
          ActorForm form = new ActorForm(a, user, context.lang().code());
          form.setInGroup(sessionHelper.getCurrentGroup(context));
          return editActorModal.render(
              formFactory.form(ActorForm.class).fill(form), false, helper, sessionHelper.getUser(ctx()), messages);

        } else {
          logger.error("unable to retrieve actor " + ids.get(0));
        }
      } catch (NumberFormatException e) {
        logger.error("unaparsable actor id " + ids.get(0));
      }
      sessionHelper.removeValue(ctx(), SessionHelper.KEY_NEWACTOR, ids.get(0));
    }
    // return null if nothing to show
    return null;
  }

  /**
   * Helper method to build an actor modal frame
   *
   * @param context the HTTP request context
   * @param messages messages to be shown on modal frame
   * @return the add actor modal frame if there are still auto-created actors, null otherwise
   */
  private Html getAutoCreatedProfessionModal(Http.Context context, Map<String, String> messages) {
    List<String> ids = sessionHelper.getValues(context, SessionHelper.KEY_NEWPROFESSION);
    if (ids != null && !ids.isEmpty()) {
      logger.debug("will ask details about " + ids.get(0));
      try {
        Profession p = retrieveProfessionAndDetermineSuperLink(Integer.parseInt(ids.get(0)));
        if (p != null) {
          WebdebUser user = sessionHelper.getUser(ctx());
          ProfessionEditForm form = new ProfessionEditForm(p, context.lang().code(), user.getERole().id());
          return editProfession.render(
              formFactory.form(ProfessionEditForm.class).fill(form), helper, sessionHelper.getUser(ctx()), messages);

        } else {
          logger.error("unable to retrieve profession " + ids.get(0));
        }
      } catch (FormatException e) {
        logger.error("unaparsable profession id " + ids.get(0));
      }
    }
    // return null if nothing to show
    return null;
  }

  /**
   * Handle error on actor form submission and returns the actor form view (depending on the switch).
   * If an unknown error occurred, either a "goto" page or the general entry view is returned.
   *
   * @param exception the exception raised from unsuccessful save
   * @param onlyFields switch to know if the addActorFields or addActor views must be returned
   * @return if the form contains error, a bad request (400) response is returned with, if onlyfield, the
   * editActorFields template or the editActor full form otherwise. In case of possible name matches, a
   * 409 response is returned with the modal frame to select among possible matches. If another error occurred,
   * a redirect to either a "goto" session-cached url or the main entry page.
   */
  private CompletionStage<Result> handleActorError(ActorNotSavedException exception, boolean onlyFields) {
    Map<String, String> messages = new HashMap<>();
    Form<ActorForm> form = exception.form;
    switch (exception.error) {
      case ActorNotSavedException.NAME_MATCH:
        return CompletableFuture.supplyAsync(() ->
                        status(409, handleNameMatches.render(exception.match.getNameMatches(), exception.match.isActor(), exception.match.getIndex(), exception.match.getSelector(), null, values))
                , context.current());

      case ActorNotSavedException.ERROR_FORM:
        // error in form, just resend it
        if(!onlyFields)
          flash(SessionHelper.WARNING, "error.form");
        return CompletableFuture.supplyAsync(() -> onlyFields ?
            badRequest(editActorFields.render(form, helper, messages))
            : badRequest(editActor.render(form, helper, sessionHelper.getUser(ctx()), messages)), context.current());

      default:
        // any other error, check where do we have to go after and show message in exception
        if(!onlyFields)
          flash(SessionHelper.ERROR, exception.getMessage());
        return onlyFields ?
                CompletableFuture.supplyAsync(() -> internalServerError(message.render(messages)), context.current())
                : sendRedirectTo(routes.EntryActions.contribute().toString());
    }
  }

  /**
   * Save an actor from a given form and add in session cookie the list of auto-created actors' id if any
   * (being the possible unknown affiliation for this actor)
   *
   * @param form an actor form object that may contain errors
   * @return given (updated) actor form
   * @throws ActorNotSavedException if an error exist in passed form or any error arisen from save action
   */
  private synchronized ActorForm saveActor(Http.Request request, Form<ActorForm> form) throws ActorNotSavedException {
    File file = getFileFromRequest(request);

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      // save picture in temp fs
      files.saveToCache(file, form.data().get("avatarString"));
      throw new ActorNotSavedException(form, ActorNotSavedException.ERROR_FORM);
    }

    ActorForm actor = form.get();
    WebdebUser user = sessionHelper.getUser(ctx());

    NameMatch<ActorHolder> match = helper.findAffiliationsNameMatches("affiliationsForm", actor.getAffiliationsForm(), EActorType.ORGANIZATION, user, ctx().lang().code());

    if(match.isEmpty()){
      match = helper.findAffiliationsNameMatches("orgAffiliationsForm", actor.getOrgaffiliationsForm(), EActorType.UNKNOWN, user, ctx().lang().code());
    }

    if(match.isEmpty()){
      match = helper.findAffiliationsNameMatches("qualificationsForm", actor.getQualificationsForm(), EActorType.ORGANIZATION, user, ctx().lang().code());
    }

    if(match.isEmpty()){
      match = helper.findAffiliationsNameMatches("parentsForm", actor.getParentsForm(), EActorType.PERSON, user, ctx().lang().code());
    }

    // check if matches exists for this actor, if any, return badRequest that will construct a pop-up form to ask what to do
    if (!match.isEmpty()){
      // save picture in temp fs
      files.saveToCache(file, form.data().get("avatarString"));
      throw new ActorNotSavedException(form.fill(actor), ActorNotSavedException.NAME_MATCH, match);
    }

    // all good, let's save validated actor
    List<Integer> newProfessions = helper.checkIfNewProfessionsMustBeCreated(
        helper.convertAffiliationFormToProfessionForm(actor.getAffiliationsForm(), ctx().lang().code()));

    newProfessions.addAll(helper.checkIfNewProfessionsMustBeCreated(
            helper.convertAffiliationFormToProfessionForm(actor.getQualificationsForm(), ctx().lang().code())));

    sessionHelper.remove(ctx(), SessionHelper.KEY_NEWPROFESSION);
    newProfessions.forEach(id -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWPROFESSION, id+""));

    actor.setPlaces(savePlaces(actor.getPlaces(), false));

    try {
      // save will return actors that have been automatically created
      treatSaveContribution(actor.save(sessionHelper.getUser(ctx()).getContributor().getId()));
      // prepend this
    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to save actor", e);
      // both exceptions are sending message keys
      throw new ActorNotSavedException(form, ActorNotSavedException.ERROR_DB, i18n.get(ctx().lang(),e.getMessage()));
    }

    // save picture if any
    savePictureFile(actor.getAvatarString(), actor.getId(), file, false);

    return actor;
  }

  /**
   * Get the partial page containing all contributions for a given actor
   *
   * @param id an actor id
   * @return the "all contributions" partial page, or an error message (in a 400 bad request) if given actor was not found
   */
  public CompletionStage<Result> getActorContributions(Long id) {
    logger.debug("GET actor contributions for " + id);
    Actor actor = actorFactory.retrieve(id);
    Map<String, String> messages = new HashMap<>();

    if (actor == null) {
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(),"viz.actor.contributions.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }
    WebdebUser user = sessionHelper.getUser(ctx());

    List<ContributionHolder> holders =
        helper.toHolders(user.filterContributionList(actor.getContributions(EContributionType.ALL).keySet()),
            user, ctx().lang().code());
    // sends back results and filters
    return CompletableFuture.supplyAsync(() ->
            ok(simpleSearchResult.render(" ", holders, values)), context.current());
  }

  /**
   * Get the modal with all debates concerned by the given actor and socio key value
   *
   * @param actor the actor id
   * @param key a ESocioGroupKey key id
   * @param value the key value
   * @return the modal with all debates with positions
   */
  public CompletionStage<Result> getActorPositionsForSocioValue(Long actor, int key, Long value) {
    Actor a = actorFactory.retrieve(actor);
    ESocioGroupKey eKey = ESocioGroupKey.value(key);

    if(a != null && eKey != null) {
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      ActorVizHolder holder = new ActorVizHolder(a, user, lang);
      List<ActorDistance> positions = holder.getActorsDistanceByDebate(eKey, value);
      String relatedName = positions.isEmpty() ? "" :
              helper.getActorSocioName(eKey, positions.get(0).getRelatedId(), positions.get(0).getRelatedName(), lang);

      return sendOk(actorDebatePositionsModal.render(
              positions,
              holder.getId(),
              holder.getFullname(),
              relatedName,
              eKey,
              i18n.get(ctx().lang(), "viz.actor.position.debate.title", holder.getFullname(), relatedName),
              user));
    }

    return sendBadRequest();
  }

  /**
   * Get the modal with all citations where given actor is text's author
   *
   * @param actor an actor id
   * @param text an text id
   * @return the modal with all citations from given actor in given context, or bad request if context is unknown
   */
  public CompletionStage<Result> getActorTextCitations(Long actor, Long text) {
    Actor a = actorFactory.retrieve(actor);

    if(a != null){
      WebdebUser user = sessionHelper.getUser(ctx());
      String lang = ctx().lang().code();

      return sendOk(citationListModal.render(
              transformToCitationsMap(a.getTextsAuthorCitations(text), user, lang),
              a.getFullname(lang),
              user,
              null,
              true,
              null,
              null,
              null));
    }

    return sendBadRequest();
  }

  /**
   * Get all linked contributions by type
   *
   * @param actorId the actor id
   * @param panes a comma separated EPane that need to be loaded
   * @param pov for a specific contains in pane, -1 if none
   * @return a jsonified list of holders or bad request if actor is unknown
   */
  public CompletionStage<Result> getLinkedContributions(Long actorId, String panes, Integer pov) {
    logger.debug("GET linked contributions for actor " + actorId + " for panes " + panes + " and pov " + pov);
    Actor actor = actorFactory.retrieve(actorId);
    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();
    List<EActorVizPane> panesList = new ArrayList<>();
    final int fpov = pov != null ? pov : 0;

    if(!values.isBlank(panes)) {
      String splitted[] = panes.split(";");
      for(int iPane = 0; iPane < splitted.length; iPane++){
        EActorVizPane pane = EActorVizPane.value(Integer.decode(splitted[iPane]));
        if(pane != null) {
          panesList.add(pane);
        }
      }
    }

    if(actor != null && !panes.isEmpty()) {
      ActorVizHolder actorVizHolder = new ActorVizHolder(actor, user, lang);
      Map<Integer, String> response = new HashMap<>();

      panesList.forEach(pane -> {
        switch (pane) {
          case AFFILIATIONS:
            response.put(pane.id(), actorAffiliations.render(actorVizHolder, EActorType.ORGANIZATION, fpov, user, helper).toString());
            break;
          case PERSONS:
            if(actorVizHolder.getEActortype() == EActorType.ORGANIZATION)
              response.put(pane.id(), actorAffiliations.render(actorVizHolder, EActorType.PERSON, fpov, user, helper).toString());
            break;
          case ABOUT:
           // response.put(pane.id(), actorCitations.render(actorVizHolder, EActorRole.CITED, fpov).toString());
            break;
          case ARGUMENTS:
           // response.put(pane.id(), actorCitations.render(actorVizHolder, EActorRole.AUTHOR, fpov).toString());
            break;
          case SOCIOGRAPHY:
            response.put(pane.id(), actorPositions.render(actorVizHolder, user, helper, fpov).toString());
            break;
          case BIBLIOGRAPHY:
            response.put(pane.id(), contributionBibliography.render(actorVizHolder.getId(), EContributionType.ACTOR, actorVizHolder.getTextsMap(), actorVizHolder.getTextsCitations(), user).toString());
            break;
        }
      });

      return sendOk(Json.toJson(response));
    }

    return sendBadRequest();
  }

  /*
   * INNER CLASS
   */

  /**
   * Inner class to handle actor exception when an actor cannot be saved from private save execution
   */
  private class ActorNotSavedException extends Exception {

    private static final long serialVersionUID = 1L;
    final Form<ActorForm> form;
    final NameMatch<ActorHolder> match;
    final int error;

    static final int ERROR_FORM = 0;
    static final int NAME_MATCH = 1;
    static final int ERROR_DB = 2;

    ActorNotSavedException(Form<ActorForm> form, int error) {
      this.error = error;
      this.form = form;
      this.match = null;
    }

    ActorNotSavedException(Form<ActorForm> form, int error, String message) {
      super(message);
      this.error = error;
      this.form = form;
      this.match = null;
    }

    ActorNotSavedException(Form<ActorForm> form, int error, NameMatch<ActorHolder> match) {
      this.error = error;
      this.form = form;
      this.match = match;
    }
  }

  private class AffiliationsObj {
    private String label;
    private String btn;
    private List<ActorAffiliations> affiliations;

    public AffiliationsObj(String label, String btn, List<ActorAffiliations> affiliations) {
      this.label = label;
      this.btn = btn;
      this.affiliations = affiliations;
    }

    public String getLabel() {
      return label;
    }

    public String getBtn() {
      return btn;
    }

    public List<ActorAffiliations> getAffiliations() {
      return affiliations;
    }
  }
}
