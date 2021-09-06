
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

package be.webdeb.presentation.web.controllers.viz;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.actor.Profession;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EOrderBy;
import be.webdeb.core.api.contribution.TextualContribution;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.ws.external.ExternalForm;
import be.webdeb.infra.ws.external.text.VizTextResponse;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.account.AdvicesForm;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.actor.ActorVizHolder;
import be.webdeb.presentation.web.controllers.viz.actor.EActorVizPane;
import be.webdeb.presentation.web.controllers.viz.debate.DebateVizHolder;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;
import be.webdeb.presentation.web.controllers.viz.tag.ETagVizPane;
import be.webdeb.presentation.web.controllers.viz.tag.TagVizHolder;
import be.webdeb.presentation.web.controllers.viz.text.ETextVizPane;
import be.webdeb.presentation.web.controllers.viz.text.TextVizHolder;
import be.webdeb.presentation.web.views.html.browse.card.contributionsCardContainer;
import be.webdeb.presentation.web.views.html.browse.searchResultScroller;
import be.webdeb.presentation.web.views.html.oops.oops;
import be.webdeb.presentation.web.views.html.viz.actor.actorVisualization;
import be.webdeb.presentation.web.views.html.viz.actor.util.authorSimpleList;
import be.webdeb.presentation.web.views.html.viz.citation.citationContainerList;
import be.webdeb.presentation.web.views.html.viz.citation.citationListModal;
import be.webdeb.presentation.web.views.html.viz.common.util.contributionAsyncListModal;
import be.webdeb.presentation.web.views.html.viz.common.util.contributionContainerList;
import be.webdeb.presentation.web.views.html.viz.common.util.shareContributionModal;
import be.webdeb.presentation.web.views.html.viz.common.util.suggestions;
import be.webdeb.presentation.web.views.html.viz.debate.debateVisualization;
import be.webdeb.presentation.web.views.html.viz.debate.util.debateContainerList;
import be.webdeb.presentation.web.views.html.viz.debate.util.debateListModal;
import be.webdeb.presentation.web.views.html.viz.tag.tagVisualization;
import be.webdeb.presentation.web.views.html.viz.explore;
import be.webdeb.presentation.web.views.html.viz.text.textVisualization;
import be.webdeb.presentation.web.views.html.viz.contributionDetails;
import be.webdeb.presentation.web.views.html.oops.privateContribution;

import org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * Simple routing class for contribution visualization
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class VizActions extends CommonController {

    /**
     * main page for visualization -> redirect to search
     *
     * @return the main page with the available possibilities as new entries
     */
    public CompletionStage<Result> index(Integer pane) {
        logger.debug("GET explore page");

        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        AdvicesForm advicesForms = new AdvicesForm(contributorFactory.getAdvices(), lang);

        EContributionType type = EContributionType.value(pane) == null || EContributionType.value(pane) == EContributionType.ALL ? EContributionType.DEBATE : EContributionType.value(pane);

        return CompletableFuture.supplyAsync(() -> ok(explore.render(type, advicesForms, values, user)), context.current());
    }

    /**
     * Find a list of Contribution ordered by number of visualization hits (actor, text and argument only)
     *
     * @param type a contribution type (may be -1 for actors, texts and arguments)
     *                  * @param orderBy the way to order the entries
     * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param orderBy the way to order the entries
     * @return the list of most hit Contributions of given type for given contributor
     */
    public CompletionStage<Result> getPopularEntries(int type, int fromIndex, int toIndex, int orderBy) {
        //logger.debug(type + " " + fromIndex + " " + toIndex + " " + orderBy);
        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        EContributionType eType = EContributionType.value(type);
        eType = eType == null ? EContributionType.ALL : eType;

        EOrderBy eOrderBy = EOrderBy.value(orderBy);
        eOrderBy = eOrderBy == null ? EOrderBy.POPULARITY : eOrderBy;

        return sendOk(contributionsCardContainer.render(
                this.helper.toHolders(
                        this.textFactory.getPopularEntries(eType, user.getId(), user.getGroupId(), fromIndex, toIndex, eOrderBy),
                        user, lang),
                values,
                true,
                true,
                false));
    }

    /**
     * Dispatcher method to redirect to right visualization page depending on given id and type.
     * Does not perform any validation on parameters, except that type must exist
     *
     * @param id   a contribution id
     * @param type a valid type id
     * @param viz  the pane wanted
     * @param pov  the point of vue wanted for the pane
     * @return redirect to appropriate viz page depending on given type, otherwise redirect to browse page
     */
    public CompletionStage<Result> dispatch(Long id, int type, int viz, int pov) {
        EContributionType ctype = EContributionType.value(type);
        if (ctype == null) {
            Contribution contribution = actorFactory.retrieveContribution(id);

            if(contribution == null) {
                logger.warn("invalid call on dispatcher visualisation index page");
                return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.browse.routes.BrowseActions.search()));
            }

            ctype = contribution.getType();
        }

        // in case we have a flashing scope, must pass it for redirect
        Http.Flash redirected = flash();
        if (!redirected.isEmpty()) {
            redirected.forEach((key, value) -> flash().put(key, value));
        }

        // switch on type and redirect appropriately
        switch (ctype) {
            case ACTOR:
                return CompletableFuture.completedFuture(redirect(routes.VizActions.actor(id, viz, pov)));
            case DEBATE:
                return CompletableFuture.completedFuture(redirect(routes.VizActions.debate(id, viz, pov)));
            case TEXT:
                return CompletableFuture.completedFuture(redirect(routes.VizActions.text(id, viz, pov)));
            case TAG:
                return CompletableFuture.completedFuture(redirect(routes.VizActions.tag(id, viz, pov)));
            default:
                return details(id);
        }
    }

    /**
     * Display details about given contribution, if any
     *
     * @param id a contribution id
     * @return the details page for given contribution
     */
    public CompletionStage<Result> details(Long id) {
        logger.debug("GET details for " + id);
        Contribution contribution = textFactory.retrieveContribution(id);
        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        if (contribution == null) {
            flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "actor.not.found"));
            logger.warn("invalid type given for details page " + id);
            return CompletableFuture.completedFuture(redirect(be.webdeb.presentation.web.controllers.browse.routes.BrowseActions.search()));
        }

        if (!user.mayView(contribution) && !user.isPublicAdmin()) {
            return sendUnauthorizedContribution();
        }

        return sendOk(contributionDetails.render(helper.toVizHolder(contribution, user, lang), helper, user));
    }

    /**
     * Display the visualization screen for given debate and pane
     *
     * @param id   a debate id
     * @param pane the visualization pane id (aka EDebateVizPane key)
     * @param pov  the point of vue wanted for the pane
     * @return the visualization page for given debate
     */
    public CompletionStage<Result> debate(Long id, int pane, int pov) {
        logger.debug("GET debate viz for " + id + " and pane " + pane);
        Debate debate = sessionHelper.isBot(request()) ? debateFactory.retrieve(id) : debateFactory.retrieveWithHit(id);
        WebdebUser user = sessionHelper.getUser(ctx());
        EDebateVizPane viz = EDebateVizPane.value(pane);

        if (debate == null) {
            flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "debate.not.found"));
            return CompletableFuture.completedFuture(Results.notFound(
                    oops.render("debate visualization page for id=" + id, user)));
        }

        if (!user.mayView(debate)) {
            return CompletableFuture.completedFuture(Results.unauthorized(
                    privateContribution.render(user)));
        }

        DebateVizHolder debateVizHolder = new DebateVizHolder(debate, user, ctx().lang().code());

        return CompletableFuture.supplyAsync(() ->
                        ok(debateVisualization.render(
                                debateVizHolder,
                                null,
                                viz == null ? EDebateVizPane.ARGUMENTS : viz,
                                pov,
                                helper,
                                user,
                                null))
                , context.current()
        );
    }

    /**
     * Display the visualization screen for given tag and pane
     *
     * @param id   a tag id
     * @param pane the visualization pane id (aka ETagVizPane key)
     * @param pov  the point of vue wanted for the pane
     * @return the visualization page for given tag
     */
    public CompletionStage<Result> tag(Long id, int pane, int pov) {
        logger.debug("GET debate viz for " + id + " and pane " + pane);
        Tag tag = sessionHelper.isBot(request()) ? tagFactory.retrieve(id) : tagFactory.retrieveWithHit(id);
        WebdebUser user = sessionHelper.getUser(ctx());
        ETagVizPane viz = ETagVizPane.value(pane);

        if (tag == null) {
            flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "debate.not.found"));
            return CompletableFuture.completedFuture(Results.notFound(
                    oops.render("tag visualization page for id=" + id, user)));
        }

        if (!user.mayView(tag)) {
            return CompletableFuture.completedFuture(Results.unauthorized(
                    privateContribution.render(user)));
        }

        TagVizHolder tagVizHolder = new TagVizHolder(tag, user, ctx().lang().code());

        return CompletableFuture.supplyAsync(() ->
                        ok(tagVisualization.render(
                                tagVizHolder,
                                null,
                                viz == null ? ETagVizPane.ARGUMENTS : viz,
                                pov,
                                helper,
                                user,
                                null))
                , context.current()
        );
    }

    /**
     * Display the visualization screen for a given actor and pane
     *
     * @param id   an actor id
     * @param pane the visualization pane id (aka EActorVizPane key)
     * @return the visualization page for given actor
     */
    public CompletionStage<Result> actor(Long id, int pane, int pov) {
        logger.debug("GET actor viz for " + id + " and pane " + pane);

        Actor actor = sessionHelper.isBot(request()) ? actorFactory.retrieve(id) : actorFactory.retrieveWithHit(id);
        WebdebUser user = sessionHelper.getUser(ctx());
        EActorVizPane viz = EActorVizPane.value(pane);

        if (actor == null) {
            flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "actor.not.found"));
            return CompletableFuture.completedFuture(Results.notFound(
                    oops.render("actor visualization page for id=" + id, user)));
        }

        if (!user.mayView(actor)) {
            return CompletableFuture.completedFuture(Results.unauthorized(
                    privateContribution.render(user)));
        }

        return CompletableFuture.supplyAsync(() ->
                        ok(actorVisualization.render(
                                new ActorVizHolder(actor, user, ctx().lang().code()),
                                null,
                                viz != null ? viz : EActorVizPane.IDENTITY,
                                pov,
                                helper,
                                user,
                                null)),
                context.current()
        );
    }

    /**
     * Display the visualization screen for a given text and pane
     *
     * @param id   a text id
     * @param pane the visualization pane id (aka ETextVizPane key)
     * @return the visualization page for given text
     */
    public CompletionStage<Result> text(Long id, int pane, int pov) {
        logger.debug("GET text viz for " + id + " and pane " + pane);

        Text text = sessionHelper.isBot(request()) ? textFactory.retrieve(id) : textFactory.retrieveWithHit(id);
        WebdebUser user = sessionHelper.getUser(ctx());
        ETextVizPane viz = ETextVizPane.value(pane);

        if (text == null) {
            flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "text.not.found"));
            return CompletableFuture.completedFuture(Results.notFound(
                    oops.render("text visualization page for id=" + id, user)));
        }

        if (!user.mayView(text)) {
            return CompletableFuture.completedFuture(Results.unauthorized(
                    privateContribution.render(user)));
        }

        return CompletableFuture.supplyAsync(() ->
                        ok(textVisualization.render(
                                new TextVizHolder(text, user, ctx().lang().code()),
                                null,
                                viz != null ? viz : ETextVizPane.CITATIONS,
                                pov,
                                helper,
                                user,
                                null)),
                context.current()
        );
    }

    /**
     * Get the list of actors linked to given contribution with given role that redirect to given pane
     *
     * @param idContribution a contribution id
     * @param role the role of the actor
     * @param pane the pane to redirect
     * @return the modal if given contribution exists, bad request otherwhise
     */
    public CompletionStage<Result> getContributionActors(Long idContribution, int role, int pane) {
        Contribution contribution = actorFactory.retrieveContribution(idContribution);
        EActorRole eRole = EActorRole.value(role);
        EActorVizPane ePane = EActorVizPane.value(pane);


        if(contribution != null && contribution.getType().isTextualContribution() && eRole != null) {
            String lang = ctx().lang().code();
            List<ActorSimpleForm> actors = ((TextualContribution) contribution).getActors(999, eRole)
                    .stream()
                    .map(actor -> new ActorSimpleForm(actor, lang))
                    .collect(Collectors.toList());

            return sendOk(authorSimpleList.render(actors, ePane == null ? EActorVizPane.AFFILIATIONS : ePane));
        }

        return sendBadRequest();
    }

    /**
     * Get the share media modal for given contribution
     *
     * @param idContribution a contribution id
     * @return the modal if given contribution exists, bad request otherwhise
     */
    public CompletionStage<Result> getShareMediaModal(Long idContribution, int pane, int pov) {
        Contribution contribution = actorFactory.retrieveContribution(idContribution);
        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        if(contribution != null) {
            return sendOk(shareContributionModal.render(helper.toHolder(contribution, user, lang), pane, pov, user));
        }

        return sendBadRequest();
    }

    /**
     * Get the content of given url
     *
     * @param url the url to fetch
     * @return the html content
     */
    public CompletionStage<Result> getUrlContent(String url) {
        if(url != null && values.isURL(url)){
            try{
                URL test = new URL(url);
                URLConnection uc = test.openConnection();
                uc.addRequestProperty("User-Agent", "Mozilla/4.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }

                in.close();
                return CompletableFuture.supplyAsync(() ->
                        ok(Html.apply(sb.toString())));
            }
            catch(Exception e) {
                return sendBadRequest();
            }
        }

        return sendBadRequest();
    }

    /**
     * Get suggested contributions related to given contribution
     *
     * @param idContribution a contribution id
     * @return a partial page with suggested contributions
     */
    public CompletionStage<Result> getSuggestionsContribution(Long idContribution) {
        WebdebUser user = sessionHelper.getUser(ctx());
        String lang = ctx().lang().code();

        Citation citation = citationFactory.getSuggestionCitation(idContribution);
        CitationHolder citationHolder = citation != null ? helper.toCitationHolder(citation, user, lang, true) : null;

        return sendOk(suggestions.render(null, citationHolder, null, null, null, user));
    }

    /**
     * Get the modal to find contributions by given contribution type linked to given contribution
     *
     * @param contributionId a contribution id
     * @return the modal of contributions, or badrequest if given contribution is unkown
     */
    public CompletionStage<Result> getFindContributionsModal(Long contributionId) {
        Contribution contribution = tagFactory.retrieveContribution(contributionId);

        if(contribution != null) {
            String lang = ctx().lang().code();

            return sendOk(contributionAsyncListModal.render(contribution.getContributionTitle(lang)));
        }

        return sendBadRequest();
    }

    /**
     * Find contributions by given contribution type linked to given contribution
     *
     * @param contributionId a contribution id
     * @param forContributionId the related contribution id
     * @param forContributionType the contribution type
     * @param contributionRole the role of related contribution
     * @param pane the pane to redirect
     * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
     * @param filters a string that contains data for filter results
     * @return the list of contributions, or badrequest if given contribution is unkown
     */
    public CompletionStage<Result> findContributions(Long contributionId, Long forContributionId, int forContributionType, int contributionRole, int pane, int fromIndex, int toIndex, String filters) {
        Contribution contribution = tagFactory.retrieveContribution(contributionId);
        EContributionType cType = EContributionType.value(forContributionType);

        if(contribution != null && cType != null) {
            WebdebUser user = sessionHelper.getUser(ctx());
            String lang = ctx().lang().code();

            Contribution relatedContribution = actorFactory.retrieveContribution(forContributionId);
            ContributionHolder holder = relatedContribution != null ? helper.toVizHolder(relatedContribution, user, lang) : null;

            List<ContributionHolder> contributions = new ArrayList<>();

            switch (contribution.getContributionType().getEType()) {
                case TAG:
                    Tag tagContribution = (Tag) contribution;
                    TagHolder tagHolder = helper.toTagHolder(tagContribution, user, lang, false);

                    switch (cType) {
                        case CITATION:
                            contributions.addAll(holder != null && holder.getType() == EContributionType.ACTOR ?
                                    ((ActorVizHolder) holder).getTagCitationsList(contributionId, EActorRole.value(contributionRole), fromIndex, toIndex, filters) :
                                    tagHolder.getCitations(fromIndex, toIndex, filters));
                            break;
                        case DEBATE:
                            contributions.addAll(tagHolder.getDebates(fromIndex, toIndex, filters));
                            break;
                    }
                    break;
                case DEBATE:
                    switch (cType) {
                        case CITATION:
                            contributions.addAll(((ActorVizHolder) holder).getDebateCitationsList(contributionId, EActorRole.value(contributionRole), fromIndex, toIndex, filters));
                            break;
                    }
                    break;
                case TEXT:
                    switch (cType) {
                        case CITATION:
                            contributions.addAll(((ActorVizHolder) holder).getTextCitationsList(contributionId, EActorRole.value(contributionRole), fromIndex, toIndex, filters));
                            break;
                    }
                    break;
                case ACTOR:
                    switch (cType) {
                        case CITATION:
                            contributions.addAll(((ActorVizHolder) holder).getActorWhoQuotesCitationsList(contributionId, fromIndex, toIndex, filters));
                            break;
                    }
                    break;
            }

            return sendOk(contributionContainerList.render(
                    contributions,
                    user, cType,
                    true,
                    false,
                    holder != null ? null : EActionType.contributionTypeToActionType(cType),
                    pane));
        }

        return sendBadRequest();
    }

    /*
     * ARGUMENT PARTIAL PAGES
     */

    /**
     * Get the justification map for a given argument
     *
     * @param argument the argument for which we need to draw the justification links
     * @param pov      the point of vue
     * @return the argument cartography pane
     */
    public CompletionStage<Result> getJustificationMap(Long argument, int pov) {
        WebdebUser user = sessionHelper.getUser(ctx());
        Argument arg = argumentFactory.retrieve(argument);

        if (arg == null) {
            logger.error("argument not found");
            return CompletableFuture.completedFuture(notFound());
        }

        return CompletableFuture.supplyAsync(Results::ok, context.current());
    }

    /**
     * Get the name of a given contribution id.
     *
     * @param id a contribution id
     * @return the name of the contribution in the user langage if the contribution exists and is not deleted,
     * otherwise the sortkey name of the deleted contribution or DELETED if contribution is not found.
     */
    public CompletionStage<Result> getContributionName(Long id) {
        String name;
        Contribution contribution = textFactory.retrieveContribution(id);

        if (contribution == null) {
            name = getNameOrDefaultName();
        } else {
            String lang = ctx().lang().code();

            switch (contribution.getContributionType().getEType()) {
                case ACTOR:
                    Actor actor = actorFactory.retrieve(id);
                    name = actor != null ? actor.getFullname(lang) : getNameOrDefaultName(contribution.getSortkey());
                    break;
                case DEBATE:
                    Debate debate = debateFactory.retrieve(id);
                    name = debate != null ? debate.getFullTitle() : getNameOrDefaultName(contribution.getSortkey());
                    break;
                case ARGUMENT:
                    Argument argumentContext = argumentFactory.retrieve(id);
                    name = argumentContext != null ? argumentContext.getFullTitle() : getNameOrDefaultName(contribution.getSortkey());
                    break;
                case CITATION:
                    Citation citation = citationFactory.retrieve(id);
                    name = citation != null ? citation.getOriginalExcerpt() : getNameOrDefaultName(contribution.getSortkey());
                    break;
                case TEXT:
                    Text text = textFactory.retrieve(id);
                    name = text != null ? text.getTitle(lang) : getNameOrDefaultName(contribution.getSortkey());
                    break;
                case TAG:
                    Tag tag = tagFactory.retrieve(id);
                    name = values.firstLetterUpper(tag != null ? tag.getName(lang) : getNameOrDefaultName(contribution.getSortkey()));
                    break;
                default:
                    name = getNameOrDefaultName(contribution.getSortkey());
            }
        }

        return sendOk(name);
    }

    /**
     * Get the name of a given place id.
     *
     * @param id a place id
     * @return the name of the place or DELETED if place is not found.
     */
    public CompletionStage<Result> getPlaceName(Long id) {
        String name;
        Place place = textFactory.retrievePlace(id);

        if (place == null) {
            name = getNameOrDefaultName();
        } else {
            name = getNameOrDefaultName(place.getName(ctx().lang().code()));
        }

        return sendOk(name);
    }

    /**
     * Get the name of a technical data like ActorType, ...
     *
     * @param id    a ETechnicalType id
     * @param value the value of the type
     * @return the name of the technical type
     */
    public CompletionStage<Result> getTechnicalName(String id, String value) {
        return sendOk(getNameOrDefaultName(textFactory.getTechnicalName(id, value, ctx().lang().code())));
    }

    /**
     * Get the name of a Profession
     *
     * @param id a Profession id
     * @return the name of the given Profession
     */
    public CompletionStage<Result> getProfessionName(int id) {
        String name;
        try {
            Profession profession = actorFactory.getProfession(id);
            if (profession == null) {
                name = getNameOrDefaultName();
            } else {
                name = profession.getName(ctx().lang().code());
            }
        } catch (FormatException e) {
            name = getNameOrDefaultName();
        }

        return sendOk(name);
    }

    /**
     * Get the description of a given affiliation
     *
     * @param id an affiliation id
     * @return the description of the given affiliation
     */
    public CompletionStage<Result> getAffiliationName(Long id) {
        String name;

        Affiliation affiliation = actorFactory.getAffiliation(id);
        if (affiliation == null) {
            name = getNameOrDefaultName();
        } else {
            name = affiliation.getFullfunction(ctx().lang().code(), true, true);
        }

        return sendOk(name);
    }

    private String getNameOrDefaultName() {
        return getNameOrDefaultName(null);
    }

    private String getNameOrDefaultName(String name) {
        return name == null || name.isEmpty() ? "[DELETED]" : name;
    }

    /*
     * HELPER METHODS
     */

    /**
     * Sort a map of values based on the RadioKey values
     * key = AUTHOR: sort alphabetically and handle last name and name separately
     * key = DATE: sort them in reverse order
     * key = SOURCE: sort them alphabetically
     * key = TEXT : sort text alphabetically on their titles
     *
     * @param unsortedMap an unsorted map
     * @param <V>         the actual type of values in the map
     * @return a sorted map
     */
    public <V> Map<RadiographyKey, V> sortByRadioKey(Map<RadiographyKey, V> unsortedMap) {
        @SuppressWarnings("fallthrough")
        Map<RadiographyKey, V> sortedMap = new TreeMap<>((o1, o2) -> {
            switch (o1.getKey()) {
                case AUTHOR:
                    // if we have a lastname, use it (ie, we are handling persons)
                    String name1 = o1.getLastname() != null ? o1.getLastname() : o1.getName();
                    String name2 = o2.getLastname() != null ? o2.getLastname() : o2.getName();
                    if (name1.equals(name2)) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return name1.compareTo(name2);

                case DATE:
                case SOURCE:
                case TEXT:
                    if (o1.getKey().equals(ERadiographyViewKey.DATE)) {
                        // IMPORTANT NOTE -> compare is reversed (want to sort desc)
                        Date date1 = values.toDate(o1.getName());
                        Date date2 = values.toDate(o2.getName());
                        if (date1 != null && date2 != null) {
                            return date2.compareTo(date1);
                        }
                    }
                    return o1.getName().compareTo(o2.getName());

                default:
                    return -1;
            }
        });

        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

}
