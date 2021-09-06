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

package be.webdeb.presentation.web.controllers.entry.citation;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.contribution.link.EPositionLinkShade;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.LinkException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.ws.external.ExternalForm;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.browse.EOverviewType;
import be.webdeb.presentation.web.controllers.entry.NameMatch;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.actor.ProfessionForm;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.browse.overview.citationOverview;
import be.webdeb.presentation.web.views.html.oops.privateContribution;
import be.webdeb.presentation.web.views.html.entry.citation.editCitation;
import be.webdeb.presentation.web.views.html.entry.citation.editCitationFields;
import be.webdeb.presentation.web.views.html.entry.citation.editCitationModal;
import be.webdeb.presentation.web.views.html.entry.citation.citationSelectionModal;
import be.webdeb.presentation.web.views.html.util.handleNameMatches;
import be.webdeb.presentation.web.views.html.util.message;
import be.webdeb.presentation.web.views.html.viz.citation.citationChoosePositionShadeModal;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Citation-related actions, ie controller of all pages to edit citations
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CitationActions extends CommonController {

    @Inject
    protected RequestProxy proxy;

    /**
     * Ask modal to add a new citation
     *
     * @return the form page where users may add details regarding a selected citation, or redirect to
     * the "work with text" page if passed form contains error (should not happen since form is created
     * implicitly from request and not by user)
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> newCitation(Long textId, String excerpt) {
        logger.debug("GET new citation");

        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        Text text = textFactory.retrieve(textId);

        if (text == null) {
            return sendBadRequest();
        }

        if (values.isBlank(excerpt)) {
            return sendBadRequest();
        }

        // pre-fill citation with text's actors, folders and current group
        CitationForm citationForm = new CitationForm(text, user, text.getLanguage().getCode(), lang, excerpt);
        addAuthorsToForm(citationForm, text);

        citationForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
        citationForm.setLang(ctx().lang().code());

        // display the page of citation edition
        Form<CitationForm> form = formFactory.form(CitationForm.class).fill(citationForm);
        sessionHelper.set(ctx(), SessionHelper.KEY_GOTO, sessionHelper.getReferer(ctx()));

        return sendOk(editCitationModal.render(form, helper, user, null));
    }

    /**
     * Display the page to edit the citation properties
     *
     * @param excId the citation id to edit
     * @return the property page with given citation data, or redirect to "text citation" page
     * of given text with an, error message to display (flash) if citation is not found
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> edit(Long excId) {
        logger.debug("GET citation properties of " + excId);
        return editCitation(excId, false, null, -1);
    }

    /**
     * Display the modal to edit the citation properties from a modal
     *
     * @param excId the citation id to edit (-1 if it must be created)
     * @param fromContribution a contribution id from where we add the citation
     * @param contributionRole the role of the contribution in the citation
     * @return the edit modal with citation, or badrequest if something goes wrong
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> editFromModal(Long excId, Long fromContribution, int contributionRole) {
        logger.debug("GET citation modal properties of " + excId + " from contribution " + fromContribution + " with role " + contributionRole);
        return editCitation(excId, true, fromContribution, contributionRole);
    }

    /**
     * Update or create a citation
     *
     * @param excId the citation id (-1 if new one)
     * @return the citation form object if it contains errors or actor names must be disambiguated (400 response)
     * been set, or to that "goto" url.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> save(Long excId) {
        logger.debug("POST save properties of " + excId);
        Form<CitationForm> form = formFactory.form(CitationForm.class).bindFromRequest();
        try {
            CitationForm result = saveCitation(form);

            // all good -> go to this actor's page (and remove session key_goto if any)
            ctx().flash().put(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"argument.properties.added"));
            return sendRedirectTo(be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(result.getTextId(), -1, 0).toString());
            //return sendRedirectTo(be.webdeb.presentation.web.controllers.viz.routes.VizActions.details(result.getId()).toString());

        } catch (CitationNotSavedException e) {
            return handleCitationError(e, false);
        }
    }

    /**
     * Update or create an citation from a modal
     *
     * @param excId the citation id (-1 if new one)
     * @return the citation form object if it contains errors, or the context contribution page if this new citation
     * has been created to be linked to another one, or in case of success or DB crash, redirect to "goto" session key url.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> saveFromModal(Long excId) {
        logger.debug("POST save properties of " + excId);

        Form<CitationForm> form = formFactory.form(CitationForm.class).bindFromRequest();

        try {
            CitationForm result = saveCitation(form);
            ctx().flash().put(SessionHelper.SUCCESS, i18n.get(ctx().lang(),"argument.properties.added"));
            return sendOk(be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(result.getTextId(), -1, 0).url());

        } catch (CitationNotSavedException e) {
            return handleCitationError(e,true);
        }
    }

    /**
     * Save a citation justification link
     *
     * @param citationId      a citation id
     * @param contextId       a contribution context id
     * @param subContextId    a tag sub context id
     * @param categoryId      a tag category id
     * @param superArgumentId an argument id
     * @param shade           a justification link shade
     * @param group           the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> saveCitationJustificationLink(Long citationId, Long contextId, Long subContextId, Long categoryId, Long superArgumentId, int shade, int group) {
        logger.info("POST save citation justification link between citation " + citationId + " context " + contextId + ", sub context " + subContextId +
                " , category " + categoryId + " and super argument " + superArgumentId + " with shade " + shade);

        WebdebUser user = sessionHelper.getUser(ctx());

        CitationJustification existing = citationFactory.findCitationJustificationLink(contextId, subContextId, categoryId, superArgumentId, citationId, shade);

        if(existing != null && existing.isMemberOfGroup(user.getGroupId())){
            return CompletableFuture.supplyAsync(() -> status(409), context.current());
        }

        if (citationFactory.retrieve(citationId) == null) {
            logger.debug("citation is null for citation justification link, id was " + citationId);
            return sendBadRequest();
        }

        try {
            int order = existing == null ?
                    citationFactory.getMaxCitationJustificationLinkOrder(contextId, subContextId, categoryId, superArgumentId, shade) + 1 :
                    existing.getOrder();
            saveJustificationLink(citationFactory.getCitationJustificationLink(), citationId, contextId, subContextId, categoryId, superArgumentId, shade, order, user.getGroupId());
        } catch (LinkException e) {
            return handleLinkExceptionResponse(e);
        }

        return sendOk();
    }

    /**
     * Save a list of citation justification link
     *
     * @param contextId       a contribution context id
     * @param subContextId    a tag sub context id
     * @param categoryId      a tag category id
     * @param superArgumentId an argument id
     * @param shade           a justification link shade
     * @param group           the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> saveCitationJustificationLinks(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, int shade, int group) {
        logger.info("POST save citation justification links between context " + contextId + ", sub context " + subContextId +
                " , category " + categoryId + " and super argument " + superArgumentId + " with shade " + shade);
        WebdebUser user = sessionHelper.getUser(ctx());

        Form<CitationJustificationLinksRequest> form = formFactory.form(CitationJustificationLinksRequest.class).bindFromRequest();

        if (form.hasErrors()) {
            return sendBadRequest();
        }

        try {
            for(Long citation : form.get().getIds()){
                try {
                    if(!citationFactory.citationJustificationLinkAlreadyExists(contextId, subContextId, categoryId, superArgumentId, citation, shade)){
                        int order = citationFactory.getMaxCitationJustificationLinkOrder(contextId, subContextId, categoryId, superArgumentId, shade) + 1;
                        saveJustificationLink(citationFactory.getCitationJustificationLink(), citation, contextId, subContextId, categoryId, superArgumentId, shade, order, group == -1 ? user.getGroupId() : group);
                    } else if(form.get().getIds().size() == 1){
                        return CompletableFuture.supplyAsync(() -> status(409), context.current());
                    }
                } catch (LinkException e) {
                    logger.debug("Link with citation " + citation + " can be saved.");
                }
            }
        } catch (Exception e) {
            return sendBadRequest();
        }

        return sendOk();
    }

    /**
     * Save a citation position link
     *
     * @param citationId      a citation id
     * @param contextId       a contribution context id
     * @param subContextId    a tag sub context id
     * @param shade           a position link shade
     * @param group           the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> saveCitationPositionLink(Long citationId, Long contextId, Long subContextId, int shade, int group) {
        logger.info("POST save citation position link between citation " + citationId + " context " + contextId +
                ", sub context " + subContextId + " with shade " + shade);

        WebdebUser user = sessionHelper.getUser(ctx());

        if(citationFactory.citationPositionLinkAlreadyExists(contextId, subContextId, citationId, shade)){
            return CompletableFuture.supplyAsync(() -> status(409), context.current());
        }

        if (citationFactory.retrieve(citationId) == null) {
            logger.debug("citation is null for citation position link, id was " + citationId);
            return sendBadRequest();
        }

        try {
            savePositionLink(citationFactory.getCitationPositionLink(), citationId, contextId, subContextId, shade, group == -1 ? user.getGroupId() : group);
        } catch (LinkException e) {
            return handleLinkExceptionResponse(e);
        }

        return sendOk();
    }

    /**
     * Save a list of citation position link
     *
     * @param contextId       a contribution context id
     * @param subContextId    a tag sub context id
     * @param shade           a position link shade
     * @param group           the group where save this link
     * @return the status code depending of the success or fail or the process
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> saveCitationPositionLinks(Long contextId, Long subContextId, int shade, int group) {
        logger.info("POST save citation position links between context " + contextId + ", sub context " + subContextId + " with shade " + shade);
        WebdebUser user = sessionHelper.getUser(ctx());

        Form<CitationPositionLinksRequest> form = formFactory.form(CitationPositionLinksRequest.class).bindFromRequest();

        if (form.hasErrors()) {
            return sendBadRequest();
        }

        try {
            for(Long citation : form.get().getIds()){
                try {
                    if(!citationFactory.citationPositionLinkAlreadyExists(contextId, subContextId, citation, shade)){
                        savePositionLink(citationFactory.getCitationPositionLink(), citation, contextId, subContextId, shade, group == -1 ? user.getGroupId() : group);
                    } else if(form.get().getIds().size() == 1){
                        return CompletableFuture.supplyAsync(() -> status(409), context.current());
                    }
                } catch (LinkException e) {
                    logger.debug("Link with citation " + citation + " can be saved.");
                }
            }
        } catch (Exception e) {
            return sendBadRequest();
        }

        return sendOk();
    }

    /**
     * Search for an citation based on given query. Will look in original citation, authors and text title
     *
     * @param term the values to search for
     * @param textToLook the text id where to search, if any
     * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @return a jsonified list of citation holders
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> searchCitation(String term, Long textToLook, int fromIndex, int toIndex) {
        List<CitationHolder> result = new ArrayList<>();
        List<Map.Entry<EQueryKey, String>> query = new ArrayList<>();
        WebdebUser user = sessionHelper.getUser(ctx());

        query.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.CITATION.id())));
        query.add(new AbstractMap.SimpleEntry<>(EQueryKey.CITATION_TITLE, term));
        //query.add(new AbstractMap.SimpleEntry<>(EQueryKey.AUTHOR, term));

        if(textToLook == -1L) {
            //query.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_TITLE, term));
        }else{
            //query.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_TO_LOOK, textToLook.toString()));
        }

        try {
            String lang = ctx().lang().code();
            executor.searchContributions(query, fromIndex, toIndex).stream().filter(
                    sessionHelper.getUser(ctx())::mayView).forEach(c ->
                    result.add(helper.toCitationHolder((Citation) c, user, lang, true)));
        } catch (BadQueryException e) {
            logger.warn("unable to search for citations with given term " + term, e);
        }
        return sendOk(Json.toJson(result));
    }

    /**
     * Search for citations base
     *
     * @param term the values to search for
     * @param type the type of citation browse
     * @param context the context where the browse is, if any
     * @param subContext a tag sub context id, if any
     * @param category the tag category where the browse is, if any
     * @param argument the argument where the browse is, if any
     * @param isJustification true if a justification link must be add, false for a position link
     * @param shade a justification link shade, if any
     * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param filters a string that contains data for filter results
     * @param linkType a enum if a link must not already exists in context
     * @return a jsonified list of citation overview or bad request is type is wrong
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> searchCitationByType(String term, int type, Long context, Long subContext, Long category, Long argument, Boolean isJustification, Integer shade, int fromIndex, int toIndex, String filters, int linkType) {
        logger.debug("GET search citation by type " + term + ", type " + type + " context " + context + " sub context " +
                subContext + ", category " + category + ", argument " + argument + ", is justification " + isJustification + " with shade " + shade);

        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        return sendOk(Json.toJson(
                citationFactory.searchCitationByType(new SearchContainer(term, context, subContext, category, argument, isJustification, shade, filters, user.getId(), user.getGroupId(), lang, SearchContainer.ESearchLinkType.value(linkType), SearchContainer.ECitationBrowseType.value(type), false, fromIndex, toIndex))
                        .stream()
                        .filter(user::mayView)
                        .map(citation -> citationOverview.render(helper.toCitationHolder(citation, user, lang, false), false, true, false, EOverviewType.SIMPLE).toString())
                        .collect(Collectors.toList())));
    }

    /**
     * Validate a step of the citation form
     *
     * @return ok if the current step of the form is valid. The form fields with errors otherwise.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> validateStep() {
        Form<CitationForm> form = formFactory.form(CitationForm.class).bindFromRequest();
        WebdebUser user = sessionHelper.getUser(ctx());

        if (form.hasErrors()) {
            logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
            return handleCitationError(new CitationNotSavedException(form, CitationNotSavedException.ERROR_FORM), true);
        }

        return sendOk(editCitationFields.render(formFactory.form(CitationForm.class).fill(form.get()), helper, user, new HashMap<>()));
    }

    /**
     * Get modal to add citations in a particular context
     *
     * @param contextId the context where add the citations
     * @param subContextId a tag sub context id, if any
     * @param categoryId if the citations must be in a specific category
     * @param argumentId if the citations justify an argument
     * @param isJustification true if a justification link must be add, false for a position link
     * @param shade the shade of the potential link
     * @return the modal to add citations
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> citationSelection(Long contextId, Long subContextId, Long categoryId, Long argumentId, boolean isJustification, int shade) {
        logger.debug("GET citation selection modal for context " + contextId + ", sub context " + subContextId + ", category " +
                categoryId + ", argument " + argumentId + ", is justification " + isJustification + ", shade " + shade);
        WebdebUser user = sessionHelper.getUser(ctx());
        deleteNewLinkKeys();
        String lang = ctx().lang().code();

        ContextContribution context = textFactory.retrieveContextContribution(contextId);

        if(context == null) {
            return sendBadRequest();
        }

        if(context.getType() == EContributionType.TEXT && !values.isBlank(categoryId)) {
            argumentId = categoryId;
            categoryId = -1L;
        }

        if(!user.mayView(context) || !textFactory.contributionCanBeEdited(user.getId(), contextId, user.getGroupId())) {
            return sendUnauthorizedContribution();
        }
        
        // store link in user session with shade
        if(isJustification) {
            List<String> toStore = Arrays.asList(String.valueOf(contextId), String.valueOf(subContextId), String.valueOf(categoryId), String.valueOf(argumentId), String.valueOf(shade));
            sessionHelper.setValues(ctx(), SessionHelper.KEY_NEW_CIT_JUS_LINK, toStore);
        } else {
            List<String> toStore = Arrays.asList(String.valueOf(contextId), String.valueOf(subContextId), String.valueOf(shade));
            sessionHelper.setValues(ctx(), SessionHelper.KEY_NEW_CIT_POS_LINK, toStore);
        }

        Text text = context.getType() == EContributionType.TEXT ? (Text) context : null;
        TextHolder textHolder = text != null ? helper.toTextHolder(text, user, lang, false) : null;

        CitationForm citationForm = text != null ? new CitationForm(text, user, lang) : new CitationForm(lang, contextId);
        citationForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
        citationForm.setLang(ctx().lang().code());

        // display the page of citation edition
        Form<CitationForm> form = formFactory.form(CitationForm.class).fill(citationForm);
        sessionHelper.set(ctx(), SessionHelper.KEY_GOTO, sessionHelper.getReferer(ctx()));

        return sendOk(citationSelectionModal.render(form, textHolder, helper, user, null));
    }

    /**
     * Save an citation from external service into the database
     *
     * @return not found if form has errors, unauthorized if contributor or text are not defined or a db crash, or the
     * citation edit page if everything is ok.
     */
    public CompletionStage<Result> saveFromExternal() {
        logger.debug("POST save external citation");
        /*
        Form<ExternalForm> form = formFactory.form(ExternalForm.class).bindFromRequest();

        if (!form.hasErrors()) {
            ExternalForm citationForm = form.get();
            Contributor contributor = citationForm.getUser().getContributor();

            if(contributor != null){
                session(SessionHelper.KEY_USERMAILORPSEUDO, contributor.getEmail());
                sessionHelper.getUser(ctx());
                try{
                    Long citationId;

                    citationForm.save(contributor.getId());
                    citationId = citationForm.getId();

                    return CompletableFuture.supplyAsync(() ->
                            ok(be.webdeb.presentation.web.controllers.entry.citation.routes.CitationActions.edit(citationId)
                            .toString()), context.current());
                } catch (FormatException | PersistenceException | PermissionException e) {
                    logger.error("unable to save external citation", e);
                }
            }else{
                return sendUnauthorized();
            }
        }
         */
        return sendBadRequest();
    }

    /**
     * Save an citation from external service into the database
     *
     * @return not found if form has errors, unauthorized if contributor or text are not defined or a db crash, or the
     * citation edit page if everything is ok.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> changeCitationPositionShadeModal(Long positionId) {
        logger.debug("GET the modal to change a citation position shade for " + positionId);

        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();
        CitationPosition position = citationFactory.retrievePositionLink(positionId);

        if(position != null) {
            String title = position.getDebate().getFullTitle(lang) + (position.getSubDebate() != null ? " - " + position.getSubDebate().getName(lang) : "");
            CitationHolder holder = helper.toCitationHolder(position.getCitation(), user, lang, true);

            return sendOk(citationChoosePositionShadeModal.render(positionId, position.getLinkType().getEType(), holder, title, user, position.getDebate().getEShade()));
        }

        return sendBadRequest();
    }

    /**
     * Save an citation from external service into the database
     *
     * @return not found if form has errors, unauthorized if contributor or text are not defined or a db crash, or the
     * citation edit page if everything is ok.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
    public CompletionStage<Result> changeCitationPositionShade(Long positionId, int shade) {
        logger.debug("POST the change a citation position shade for " + positionId + " and shade " + shade);

        WebdebUser user = sessionHelper.getUser(ctx());
        CitationPosition position = citationFactory.retrievePositionLink(positionId);
        EPositionLinkShade eShade = EPositionLinkShade.value(shade);

        if(position != null && eShade != null) {
            try {
                position.setLinkType(citationFactory.getPositionLinkType(shade));
                position.save(user.getId(), user.getGroupId());
                return sendOk();
            } catch (FormatException | PermissionException | PersistenceException e) {
                logger.debug("Error with change position shade");
                return sendInternalServerError();
            }
        }

        return sendBadRequest();
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Add authors to form, from text
     *
     * @param citationForm the citationForm where add authors
     * @param text the text linked to the citation
     */
    private void addAuthorsToForm(CitationForm citationForm, Text text){
        if(text != null) {
            text.getActors().forEach(r -> citationForm.getAuthors().add(new ActorSimpleForm(r, ctx().lang().code())));
        }
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Edit an citation from a given from a modal or not.
     *
     * @param excId an citation id
     * @param fromContribution a contribution id from where we add the citation
     * @param contributionRole the role of the contribution in the citation
     * @return bad request if citation not found, unauthorize if user must not see this citation, the edit form if all is ok.
     */
    private CompletionStage<Result> editCitation(Long excId, boolean fromModal, Long fromContribution, int contributionRole) {
        WebdebUser user = sessionHelper.getUser(ctx());
        deleteNewLinkKeys();
        String lang = ctx().lang().code();

        // prepare wrapper for citation
        CitationForm citationForm;
        Citation citation = citationFactory.retrieve(excId);

        if (citation == null) {

            ExternalContribution externalCitation = citationFactory.retrieveExternal(excId);
            if(externalCitation != null) {
                if(externalCitation.getInternalContribution() != null){
                    return sendBadRequest();
                }
                citationForm = new CitationForm(externalCitation, lang);
            } else {
                citationForm = new CitationForm(lang);
                Contribution contribution = textFactory.retrieveContribution(fromContribution);

                if(contribution != null) {

                    switch (contribution.getContributionType().getEType()) {
                        case TAG:
                            citationForm.setTags(Collections.singletonList(new SimpleTagForm((Tag) contribution, lang)));
                            break;
                        case ACTOR:
                            List<ActorSimpleForm> actorForm = Collections.singletonList(new ActorSimpleForm((Actor) contribution, lang));

                            if(contributionRole == EActorRole.AUTHOR.id()) {

                                if (actorForm.get(0).getActortype() == EActorType.ORGANIZATION.id()) {
                                    citationForm.setAuthorsOrgs(actorForm);
                                    citationForm.setAuthorsTextOrgs(actorForm);

                                    citationForm.setAuthorType(EAuthorType.ORGANIZATIONS.id());
                                    citationForm.setAuthorTextType(EAuthorType.ORGANIZATIONS.id());
                                } else {
                                    citationForm.setAuthorsPers(actorForm);
                                    citationForm.setAuthorsTextPers(actorForm);

                                    citationForm.setAuthorType(EAuthorType.PERSONS.id());
                                    citationForm.setAuthorTextType(EAuthorType.PERSONS.id());
                                }
                            } else if(contributionRole == EActorRole.CITED.id()) {

                                citationForm.setHasCitedactors(true);
                                citationForm.setCitedactors(actorForm);
                            }

                            break;
                    }
                }
            }
        }
        else{
            if(!user.mayView(citation) || !textFactory.contributionCanBeEdited(user.getId(), citation.getId(), user.getGroupId())){
                return CompletableFuture.completedFuture(Results.unauthorized(
                        privateContribution.render(user)));
            }

            citationForm = new CitationForm(citation, user, lang);
        }

        citationForm.setInGroup(sessionHelper.getCurrentGroup(ctx()));
        citationForm.setLang(ctx().lang().code());

        // display the page of citation edition
        Form<CitationForm> form = formFactory.form(CitationForm.class).fill(citationForm);
        sessionHelper.set(ctx(), SessionHelper.KEY_GOTO, sessionHelper.getReferer(ctx()));

        return CompletableFuture.supplyAsync(() ->
                ok(fromModal ? editCitationModal.render(form, helper, user, null) :
                        editCitation.render(form, helper, sessionHelper.getUser(ctx()), null)), context.current());
    }

    /**
     * Save an citation from a given form.
     *
     * @param form an citation form object that may contain errors
     * @return given (updated) actor form
     * @throws CitationNotSavedException if an error exist in passed form or any error arisen from save action
     */
    private synchronized CitationForm saveCitation(Form<CitationForm> form) throws CitationNotSavedException {
        // check errors, sends back whole form if any
        if (form.hasErrors()) {
            logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
            throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_FORM);
        }

        CitationForm citation = form.get();
        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        // second check because of step form (full check, to avoid js modification or problem)
        Map<String, List<ValidationError>> errors = checkStepFormErrors(citation);
        if (errors != null) {
            logger.debug("form has errors " + errors);
            throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_FORM);
        }

        NameMatch<ActorHolder> match = helper.searchForNameMatches("authors", citation.getAuthors(), user, lang);
        saveUnknownActors(citation.getAuthors(), match,"authors", user, lang, EActorType.UNKNOWN);

        if (match.isEmpty()) {
            match = helper.searchForNameMatches("authorsPers", citation.getAuthorsPers(), user, lang);
            saveUnknownActors(citation.getAuthorsPers(), match, "authorsPers", user, lang, EActorType.PERSON);
        }

        if (match.isEmpty()) {
            match = helper.searchForNameMatches("authorsOrgs", citation.getAuthorsOrgs(), user, lang);
            saveUnknownActors(citation.getAuthorsOrgs(), match, "authorsOrgs", user, lang, EActorType.ORGANIZATION);
        }

        if (match.isEmpty()) {
            match = helper.searchForNameMatches("authorsTextPers", citation.getAuthorsTextPers(), user, lang);
            saveUnknownActors(citation.getAuthorsTextPers(), match, "authorsTextPers", user, lang, EActorType.PERSON);
        }

        if (match.isEmpty()) {
            match = helper.searchForNameMatches("authorsTextOrgs", citation.getAuthorsTextOrgs(), user, lang);
            saveUnknownActors(citation.getAuthorsTextOrgs(), match, "authorsTextOrgs", user, lang, EActorType.ORGANIZATION);
        }

        if (match.isEmpty()) {
            match = helper.searchForNameMatches("citedactors", citation.getCitedactors(), user, lang);
            saveUnknownActors(citation.getCitedactors(), match, "citedactors", user, lang, EActorType.UNKNOWN);
        }

        if (match.isEmpty() && (citation.getCitationAuthorsAreTextAuthors() == null || !citation.getCitationAuthorsAreTextAuthors())) {
            match = helper.searchForNameMatches("text_authors", citation.getText().getActors(), user, lang);
            saveUnknownActors(citation.getText().getActors(), match, "text_authors", user, lang, EActorType.UNKNOWN);
        }

        if (!match.isEmpty())  {
            throw new CitationNotSavedException(form.fill(citation), CitationNotSavedException.AUTHOR_NAME_MATCH, match);
        }

        List<ProfessionForm> professionForms = helper.convertActorSimpleFormToProfessionForm(citation.getActors(), lang);
        professionForms.addAll(helper.convertActorSimpleFormToProfessionForm(citation.getText().getActors(), lang));
        List<Integer> newProfessions = helper.checkIfNewProfessionsMustBeCreated(professionForms);
        sessionHelper.remove(ctx(), SessionHelper.KEY_NEWPROFESSION);
        newProfessions.forEach(id -> sessionHelper.addValue(ctx(), SessionHelper.KEY_NEWPROFESSION, id+""));

        citation.setPlaces(savePlaces(citation.getPlaces(), true));

        // try to save in DB
        try {
            citation.setUser(user);
            treatSaveContribution(citation.save(sessionHelper.getUser(ctx()).getContributor().getId()));
        } catch (FormatException | PersistenceException | PermissionException e) {
            logger.error("unable to save citation", e);
            throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_DB, i18n.get(ctx().lang(),e.getMessage()));
        }

        // check if this argument need to be included in a justification link
        List<String> cachedLink = sessionHelper.getValues(ctx(), SessionHelper.KEY_NEW_CIT_JUS_LINK);
        if (cachedLink != null && cachedLink.size() >= 5) {
            logger.info("ask confirmation for previously new justification link request to new citation " + citation.getId());

            sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_CIT_JUS_LINK);

            try {
                Long contextId = Long.parseLong(cachedLink.get(0));
                Long subContextId = Long.parseLong(cachedLink.get(1));
                Long categoryId = Long.parseLong(cachedLink.get(2));
                Long superArgumentId = Long.parseLong(cachedLink.get(3));
                int shade = Integer.parseInt(cachedLink.get(4));

                CompletionStage<Result> result = saveCitationJustificationLink(citation.getId(), contextId, subContextId, categoryId, superArgumentId, shade, citation.getInGroup());

                if(result.toCompletableFuture().get().status() != 200) {
                    throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_DB);
                }

            } catch (NumberFormatException | ExecutionException | InterruptedException e) {
                logger.debug("session params for citation justification link are not well formatted " + e);
                throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_DB);
            }
        }

        cachedLink = sessionHelper.getValues(ctx(), SessionHelper.KEY_NEW_CIT_POS_LINK);
        if (cachedLink != null && cachedLink.size() == 3) {
            logger.info("ask confirmation for previously new position link request to new citation " + citation.getId());

            sessionHelper.remove(ctx(), SessionHelper.KEY_NEW_CIT_POS_LINK);

            try {
                Long debateId = Long.parseLong(cachedLink.get(0));
                Long subDebateId = Long.parseLong(cachedLink.get(1));
                int shade = Integer.parseInt(cachedLink.get(2));

                CompletionStage<Result> result = saveCitationPositionLink(citation.getId(), debateId, subDebateId, shade, citation.getInGroup());

                if(result.toCompletableFuture().get().status() != 200) {
                    throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_DB);
                }

            } catch (NumberFormatException | ExecutionException | InterruptedException e) {
                logger.debug("session params for citation position link are not well formatted " + e);
                throw new CitationNotSavedException(form, CitationNotSavedException.ERROR_DB);
            }
        }

        return citation;
    }


    /**
     * Handle error on citation form submission and returns the actor form view (depending on the switch).
     * If an unknown error occurred, either a "goto" page or the general entry view is returned.
     *
     * @param exception the exception raised from unsuccessful save
     * @return if the form contains error, a bad request (400) response is returned with, if onlyfield, the
     * editCitationFields template or the editCitation full form otherwise. In case of possible author name matches,
     * a 409 response is returned with the modal frame to select among possible matches.In case of possible
     * tag name matches, a 410 response is returned with the modal frame to select among possible matches.
     * If another error occurred, a redirect to either a "goto" session-cached url or the main entry page.
     */
    private CompletionStage<Result> handleCitationError(CitationNotSavedException exception, boolean onlyFields) {
        Map<String, String> messages = new HashMap<>();
        Form<CitationForm> form = exception.form;
        WebdebUser user = sessionHelper.getUser(ctx());

        switch (exception.error) {
            case CitationNotSavedException.AUTHOR_NAME_MATCH:
                if(exception.match != null && !exception.match.isEmpty()) {
                    return CompletableFuture.supplyAsync(() ->
                                    status(409, handleNameMatches.render(exception.match.getNameMatches(), exception.match.isActor(), exception.match.getIndex(), exception.match.getSelector(), Json.toJson(form.get().getAllNewActors()).toString(), values))
                            , context.current());
                }

            case CitationNotSavedException.ERROR_FORM:
                // error in form, just resend it
                if(!onlyFields)
                    flash(SessionHelper.WARNING, "error.form");
                return CompletableFuture.supplyAsync(() -> onlyFields ?
                        badRequest(editCitationFields.render(form, helper, user, messages))
                        : badRequest(editCitation.render(form, helper, sessionHelper.getUser(ctx()), messages)), context.current());
            default:
                // any other error, check where do we have to go after and show message in exception
                flash(SessionHelper.ERROR, "error.crash");
                return onlyFields ? CompletableFuture.supplyAsync(() ->
                        internalServerError(message.render(messages)), context.current())
                        : sendInternalServerError(be.webdeb.presentation.web.controllers.entry.routes.EntryActions.contribute().url());
        }
    }

    /**
     * Inner class to handle citation exception when an citation cannot be saved from private save execution
     */
    private class CitationNotSavedException extends Exception {

        private static final long serialVersionUID = 1L;
        final Form<CitationForm> form;
        final NameMatch<ActorHolder> match;
        final int error;

        static final int ERROR_FORM = 0;
        static final int AUTHOR_NAME_MATCH = 1;
        static final int ERROR_DB = 3;
        CitationNotSavedException(Form<CitationForm> form, int error) {
            this.error = error;
            this.form = form;
            this.match = null;
        }

        CitationNotSavedException(Form<CitationForm> form, int error, String message) {
            super(message);
            this.error = error;
            this.form = form;
            this.match = null;
        }

        CitationNotSavedException(Form<CitationForm> form, int error, NameMatch<ActorHolder> match) {
            this.error = error;
            this.form = form;
            this.match = match;
        }
    }
}
