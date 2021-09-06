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

import be.webdeb.application.query.QueryExecutor;
import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.*;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.LinkException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.ws.ml.ImageDetection;
import be.webdeb.presentation.web.controllers.browse.SearchForm;
import be.webdeb.presentation.web.controllers.entry.*;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.oops.oops;
import be.webdeb.presentation.web.views.html.oops.privateContribution;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import play.twirl.api.Content;
import play.twirl.api.Html;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Common ie controller for avoid repetition
 *
 * @author Martin Rouffiange
 */
public abstract class CommonController extends Controller {

    @Inject
    protected FormFactory formFactory;

    @Inject
    protected ActorFactory actorFactory;

    @Inject
    protected ArgumentFactory argumentFactory;

    @Inject
    protected CitationFactory citationFactory;

    @Inject
    protected TagFactory tagFactory;

    @Inject
    protected DebateFactory debateFactory;

    @Inject
    protected TextFactory textFactory;

    @Inject
    protected MessagesApi i18n;

    @Inject
    protected ValuesHelper values;

    @Inject
    protected SessionHelper sessionHelper;

    @Inject
    protected ContributionHelper helper;

    @Inject
    protected QueryExecutor executor;

    @Inject
    protected ContributorFactory contributorFactory;

    @Inject
    protected HttpExecutionContext context;

    @Inject
    protected be.webdeb.infra.ws.geonames.RequestProxy geonames;

    @Inject
    protected Configuration configuration;

    @Inject
    protected FileSystem files;

    @Inject
    protected ImageDetection detector;

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Redirect to go to session key.
     *
     * @param defaultCall the default rediction if go to session key is null
     * @return either the session "goto" page or the given defaultCall, or if is null the index page
     */
    protected CompletionStage<Result> redirectToGoTo(Call defaultCall) {
        String goTo = sessionHelper.get(ctx(), SessionHelper.KEY_GOTO);
        removeGoTo();

        if (goTo == null || goTo.contains("/entry/text/annotated")) {
            if (defaultCall == null) {
                logger.info("REDIRECT to index");
                return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.routes.Application.index()));
            } else {
                logger.info("REDIRECT to " + defaultCall.url());
                return CompletableFuture.completedFuture(redirect(defaultCall));
            }
        }
        logger.info("REDIRECT to " + goTo);
        return CompletableFuture.completedFuture(redirect(goTo));
    }

    protected void removeGoTo() {
        sessionHelper.remove(ctx(), SessionHelper.KEY_GOTO);
    }

    /**
     * Fill given place form from geonames data
     *
     * @param places       the place forms to fill
     * @param defaultWorld true if at least one place must be added
     * @return the filled place forms
     */
    protected List<PlaceForm> savePlaces(List<PlaceForm> places, boolean defaultWorld) {
        // save context places
        for (int i = 0; i < places.size(); i++) {
            try {
                PlaceForm p = geonames.fillPlace(places.get(i));
                if (p != null && !values.isBlank(places.get(i).getCompleteName()))
                    places.set(i, p);
                else
                    places.remove(i);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error in fill placeForm : ", e);
            }
        }

        if (defaultWorld && places.isEmpty()) {
            places.add(new PlaceForm(0L));
        }

        return places;
    }

    /**
     * Build list of forms for unknown contributions to be proposed to the user
     *
     * @param wrappers the map of Contribution type and a list of contribution (actors or folders) that have been created during
     *                 this insertion(for all unknown contributions), an empty list if none had been created
     */
    protected void treatSaveContribution(Map<Integer, List<Contribution>> wrappers) {
        if (wrappers != null) {
            if (wrappers.containsKey(EContributionType.ACTOR.id()))
                wrappers.get(EContributionType.ACTOR.id())
                        .forEach(a -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWACTOR, a.getId().toString()));
        }
    }

    /**
     * Delete all keys about link creation
     */
    protected void deleteNewLinkKeys() {
        sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_ARG_JUS_LINK);
        sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_ARG_SIM_LINK);
        sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_CIT_JUS_LINK);
        sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_DEBATE_SIM_LINK);
    }

    /**
     * Send partial given loaded data as Json
     *
     * @param data         the Json data to send
     * @param nbDataLoaded the total number of loaded data (could be different of send data because of user may view constraint)
     * @param nbDataToLoad the number of maximum data that should have been loaded
     * @return status 200 if all data as been send, 206 if more data could be over loaded
     */
    protected CompletionStage<Result> sendJsonPartialLoadedData(JsonNode data, int nbDataLoaded, int nbDataToLoad) {
        return CompletableFuture.supplyAsync(() ->
                nbDataLoaded >= nbDataToLoad ? status(206, data) : ok(data), context.current());
    }

    protected CompletionStage<Result> sendOk() {
        return CompletableFuture.supplyAsync(() ->
                ok(""), context.current());
    }

    protected CompletionStage<Result> sendOk(ContributionHolder holder) {
        return CompletableFuture.supplyAsync(() ->
                ok(Html.apply(Json.toJson(holder).toString())), context.current());
    }

    protected CompletionStage<Result> sendOk(String response) {
        return CompletableFuture.supplyAsync(() ->
                ok(Html.apply(Json.toJson(response).toString())), context.current());
    }

    protected CompletionStage<Result> sendOk(Html html) {
        return CompletableFuture.supplyAsync(() ->
                ok(html), context.current());
    }

    protected CompletionStage<Result> sendOk(JsonNode json) {
        return CompletableFuture.supplyAsync(() ->
                ok(json), context.current());
    }

    protected CompletionStage<Result> sendOk(Result result) {
        return CompletableFuture.supplyAsync(() ->
                result, context.current());
    }

    protected CompletionStage<Result> sendBadRequest() {
        return CompletableFuture.supplyAsync(() ->
                badRequest(""), context.current());
    }

    protected CompletionStage<Result> sendBadRequest(Content content) {
        return CompletableFuture.supplyAsync(() ->
                badRequest(content), context.current());
    }

    protected CompletionStage<Result> sendBadRequest(String content) {
        return CompletableFuture.supplyAsync(() ->
                badRequest(content), context.current());
    }

    protected CompletionStage<Result> sendUnauthorized() {
        return CompletableFuture.supplyAsync(() ->
                unauthorized(""), context.current());
    }

    protected CompletionStage<Result> sendInternalServerError() {
        return CompletableFuture.supplyAsync(() ->
                internalServerError(""), context.current());
    }

    protected CompletionStage<Result> sendInternalServerError(String error) {
        return CompletableFuture.supplyAsync(() ->
                internalServerError(error), context.current());
    }

    protected CompletionStage<Result> sendUnauthorizedContribution() {
        return CompletableFuture.completedFuture(Results.unauthorized(
                privateContribution.render(sessionHelper.getUser(ctx()))));
    }

    protected CompletionStage<Result> sendNotFoundContribution(Long id, EContributionType type) {
        flash(SessionHelper.ERROR, i18n.get(ctx().lang(), type.name().toLowerCase() + ".not.found"));
        return CompletableFuture.completedFuture(Results.notFound(
                oops.render(type.name() + " visualization page for id=" + id, sessionHelper.getUser(ctx()))));
    }

    protected CompletionStage<Result> sendRedirectTo(String call) {
        return CompletableFuture.supplyAsync(() ->
                        redirect(call),
                context.current());

    }

    /**
     * Save a justification link
     *
     * @param link the concrete subtype of justification link
     * @param destinationId the destination contribution id of the link
     * @param contextId a contribution context id or a tag debate id
     * @param subContextId    a tag sub context id
     * @param categoryId a tag category id
     * @param superArgumentId an argument id
     * @param shade a justification link shade
     * @param group the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    protected void saveJustificationLink(JustificationLink link, Long destinationId, Long contextId, Long subContextId, Long categoryId, Long superArgumentId, int shade, int order, int group) throws LinkException {

        if(textFactory.retrieveContextContribution(contextId) == null){
            logger.debug("justification link context is null, given id is " + contextId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        if(!values.isBlank(subContextId) && tagFactory.retrieve(subContextId) == null){
            logger.debug("justification link sub context is null, given id is " + subContextId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        if(!values.isBlank(categoryId) && tagFactory.retrieve(categoryId) == null){
            logger.debug("justification link category is null, given id is " + categoryId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        if(!values.isBlank(superArgumentId) && argumentFactory.retrieve(superArgumentId) == null){
            logger.debug("justification link super argument is null, given id is " + superArgumentId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        try {
            link.setLinkType(argumentFactory.getJustificationLinkType(shade));
        } catch (FormatException e) {
            logger.debug("justification link shade type is not valid " + shade);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        link.setSubContextId(subContextId);
        link.setTagCategoryId(categoryId);
        link.setSuperArgumentId(superArgumentId);
        link.setOrder(order);

        saveContributionLink(link, contextId, destinationId, group);
    }

    /**
     * Save a position link
     *
     * @param link the concrete subtype of position link
     * @param destinationId the destination contribution id of the link
     * @param debateId a debate id
     * @param subDebateId    a tag sub debate id
     * @param shade a position link shade
     * @param group the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    protected void savePositionLink(CitationPosition link, Long destinationId, Long debateId, Long subDebateId, int shade, int group) throws LinkException {

        if(debateFactory.retrieve(debateId) == null){
            logger.debug("position link debate is null, given id is " + debateId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        if(!values.isBlank(subDebateId) && tagFactory.retrieve(subDebateId) == null){
            logger.debug("position link sub debate is null, given id is " + subDebateId);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        try {
            link.setLinkType(argumentFactory.getPositionLinkType(shade));
        } catch (FormatException e) {
            logger.debug("position link shade type is not valid " + shade);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        link.setSubDebateId(subDebateId);

        saveContributionLink(link, debateId, destinationId, group);
    }

    /**
     * Save a similarity link
     *
     * @param link the concrete subtype of similarity link
     * @param originId the origin contribution id of the link
     * @param destinationId the destination contribution id of the link
     * @param shade a similarity link shade
     * @param group the group where save this link
     */
    protected void saveSimilarityLink(SimilarityLink link, Long originId, Long destinationId, int shade, int group) throws LinkException {
        try {
            link.setLinkType(argumentFactory.getSimilarityLinkType(shade));
        } catch (FormatException e) {
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }

        saveContributionLink(link, originId, destinationId, group);
    }

    /**
     * Save a contribution link
     *
     * @param link the concrete subtype of contribution link
     * @param originId the origin contribution id of the link
     * @param destinationId the destination contribution id of the link
     * @param group the group where save this link
     */
    protected void saveContributionLink(ContributionLink link, Long originId, Long destinationId, int group) throws LinkException {
        try {
            // the origin id is considered as a valid contribution depending of the subtype of contribution link given
            link.setOriginId(originId);

            // the destination id is considered as a valid contribution depending of the subtype of contribution link given
            link.setDestinationId(destinationId);

            link.addInGroup(group);

            link.save(sessionHelper.getUser(ctx()).getContributor().getId(), group);
        }
        catch (FormatException e) {
            logger.debug("save contribution link throw format exception " + e);
            throw new LinkException(LinkException.Key.LINKED_OBJECT);
        }
        catch (PermissionException e) {
            logger.debug("save contribution link throw permission exception " + e);
            throw new LinkException(LinkException.Key.UNAUTHORIZED);
        }
        catch (PersistenceException e) {
            logger.debug("save contribution link throw persistence exception " + e);
            throw new LinkException(LinkException.Key.INTERNAL_ERROR);
        }
    }

    protected CompletionStage<Result> handleLinkExceptionResponse(LinkException exception) {
        switch (exception.getType()){
            case LINKED_OBJECT:
                return sendBadRequest();
            case UNAUTHORIZED:
                return sendUnauthorized();
            case INTERNAL_ERROR:
            default:
                return sendInternalServerError();
        }
    }

    protected Map<String, List<ValidationError>> checkStepFormErrors(ContributionHolder holder) {
        int currentStepNum = holder.getStepNum();
        holder.setStepNum(-1);

        Map<String, List<ValidationError>> errors = holder.validate();
        holder.setStepNum(currentStepNum);

        return errors;
    }

    protected synchronized File getFileFromRequest(Http.Request request){
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();
        Optional<Http.MultipartFormData.FilePart<File>> picture =
                body.getFiles().stream().filter(f -> f != null && f.getFile() != null && f.getFile().length() > 0).findFirst();

        File file = null;
        if (picture.isPresent()) {
            file = picture.get().getFile();
        }

        return file;
    }

    protected synchronized void savePictureInCache(File file, String filename){
        if (file != null) {
            if(!"".equals(filename))
                files.saveToCache(file, filename);
        }
    }

    protected synchronized void savePictureFile(String picture, Long contribution, File file, boolean bigSize){
        if (!values.isBlank(picture)) {
            // check if we have a picture sent or if file is in cache
            if (file == null || file.length() == 0) {
                // check in cache
                logger.debug("retrieve from temp cache " + picture);
                file = files.getFromCache(picture);
            }

            if (file != null) {
                logger.debug("will save picture file " + picture);
                try {
                    files.savePictureFile(file, contribution + picture.substring(picture.lastIndexOf('.')), bigSize);
                } catch (StringIndexOutOfBoundsException e) {
                    logger.error("given filename had no extension, unable to save file " + picture, e);
                }
            }
        }
    }

    protected Map<Integer, List<CitationHolder>> transformToCitationsMap(List<Citation> citations, WebdebUser user, String lang){
        return transformToCitationsMap(helper.toCitationsHolders(citations, user, lang, true));
    }

    protected Map<Integer, List<CitationHolder>> transformToCitationsMap(List<CitationHolder> citations){
        Map<Integer, List<CitationHolder>> citationsMap = new LinkedHashMap<>();
        Set<Long> citationsSet = new HashSet<>();

        if(!citations.isEmpty()) {

            citationsMap.put(EJustificationLinkShade.NONE.id(), new ArrayList<>());

            citations.forEach(citation -> {
                if(!citationsSet.contains(citation.getId())) {
                    citationsSet.add(citation.getId());
                    citationsMap.get(EJustificationLinkShade.NONE.id()).add(citation);
                }
            });
        }

        return citationsMap;
    }

    protected void saveUnknownActors(List<ActorSimpleForm> actors, NameMatch<ActorHolder> match, String id, WebdebUser user, String lang, EActorType type) {
        if(match.isEmpty()) {
            for (int iActor = 0; iActor < actors.size(); iActor++) {
                ActorSimpleForm form = actors.get(iActor);
                if (values.isBlank(form.getId()) && !form.isEmpty()) {
                    helper.fromSimpleFormToActor(form, id, iActor, user, lang, type);

                    if(form.getIsNew())
                        sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWACTOR, form.getId().toString());

                    if(form.getIsAffNew())
                        sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWACTOR, form.getAffid().toString());
                }
            }
        }
    }

    protected Form<SearchForm> getSettingsSearchForm(Long userId) {
        SearchForm query = new SearchForm();
        query.setInGroup(sessionHelper.getCurrentGroup(ctx()));
        query.setIsAll(true);
        query.setContributor(userId);
        return formFactory.form(SearchForm.class).fill(query);
    }
}
