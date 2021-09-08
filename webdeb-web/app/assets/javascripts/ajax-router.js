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
 *
 */

/**
 * This collection of functions are proxies for all Play ajax reverse routing.
 * Javascript functions have the same names as the ones declared in routes file (for easy maintenance).
 *
 * Ajax calls (play reverse routing) must also be declared in Application.javascriptRoutes() method.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

/*
 * ACTORS
 */

/**
 * Search for an actor based on a name and a type
 *
 * @param query the term (name) to look for
 * @param actortype the actor type (int value)
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @return {*}
 */
function searchActor(query, actortype, fromIndex, toIndex) {
  actortype = actortype === undefined ? -1 : actortype;
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.searchActor(query, actortype, fromIndex, toIndex).ajax({ async: true });
}

function findAffiliations(actor, viewBy, filters) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findAffiliations(actor, viewBy, filters).ajax({ async: true });
}

/**
 * Search for an actor based on a name among party members for election 2019
 *
 * @param query the term (name) to look for
 * @return {*}
 */
function searchPartyMembers(query) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.searchPartyMembers(query).ajax({ async: true });
}

/**
 * Validate current step of the given actor form
 *
 * @param form the actor form to validate
 * @returns either a bad request, internal server error, or a success status code
 */
function actorStepValidation(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.validateStep().ajax({
        type: 'POST',
        processData: false,
        contentType: false,
        data: new FormData(form[0]),
        async: true
    });
}

/**
 * Search for details of a given unknown actor (DB-pedia call)
 *
 * @param type the actor type id (0 for person, 1 for organization)
 * @param isUrl boolean saying if we search from an url or from a name (put in value param)
 * @param value the value to look for
 * @param optional an optional value that will be used if given type is 0 and value is not an url
 * @returns {*|{}}
 */
function searchActorDetails(type, isUrl, value, optional) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.searchActorDetails(type, isUrl, value, optional).ajax({ async: true });
}

/**
 * Get all linked contributions with an actor by viz pane type
 *
 * @param actor the actor id
 * @param type the name of the viz pane type
 * @param pov for a specific contains in pane, -1 if none
 * @returns {*}
 */
function getActorLinkedContributions(actor, type, pov) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getLinkedContributions(actor, type, pov).ajax({ async: true });
}

/**
 * Find the list of contextualized arguments where the given author is thinker of an citation linked with
 *
 * @param actor an actor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function findActorArgumentsWhereThinker(actor, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorArgumentsWhereThinker(actor, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of citations where the given author is thinker limited by indexes
 *
 * @param actor an actor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function findActorCitationsWhereThinker(actor, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorCitationsWhereThinker(actor, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of contextualized arguments where the given author is cited of an citation linked with or directly with the argument
 *
 * @param actor an actor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function findActorArgumentsWhereCited(actor, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorArgumentsWhereCited(actor, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of citations where the given author is cited limited by indexes
 *
 * @param actor an actor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function findActorCitationsWhereCited(actor, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorCitationsWhereCited(actor, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of citations where the given author is thinker limited by indexes
 *
 * @param actor an actor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function findActorAffiliations(actor, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorCitationsWhereThinker(actor, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of citations of the given actor where he has the given actor role, displayed by contribution type
 *
 * @param actor an actor id
 * @param role the actor role on the citation
 * @param type a contribution type to display by
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 * @returns {*}
 */
function findActorCitations(actor, role, type, fromIndex, toIndex, filters) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.findActorCitations(actor, role, type, fromIndex, toIndex, filters).ajax({ async: true });
}

/**
 * Get the modal with all citations where given actor is text's author
 *
 * @param actor an actor id
 * @param text an text id
 * @returns {*}
 */
function getActorTextCitations(actor, text) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActorTextCitations(actor, text).ajax({ async: true });
}

/**
 * Retrieve a raw picture file from given url and get filename back (will be used to be retrieved at actor form submit)
 *
 * @param url the url where we have to get the picture
 * @returns {*|{}}
 */
function getPictureFile(url) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getPictureFile(url).ajax({ async: true });
}

/**
 * Ajax call to retrieve an actor by its id
 *
 * @param actorId the actor ID to retrieve
 * @return {*}
 */
function getActor(actorId) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActor(actorId).ajax({ async: true });
}

/**
 * Ajax call to retrieve all functions of a given actor (by id)
 *
 * @param actorId the actor id to fetch the functions for
 * @param type a profession type
 * @param term a string term to look for also
 * @return {*}
 */
function getActorFunctions(actorId, type, term) {
    actorId = actorId !== '' ? actorId : -1;
    type = type == null ? -1 : type;
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActorFunctions(actorId, type, term).ajax();
}

/**
 * open modal for edit an actor
 *
 * @param actorId the actor id to edit
 * @returns {*}
 */
function editActor(actorId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.editFromModal(actorId).ajax({ async: true });
}

/**
 * Handle actor form submission from modal frame
 *
 * @param form the actor form
 * @returns either a bad request with the content of the form in error, or a success status code
 */
function saveFromModal(id, form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.saveFromModal().ajax({
    type: 'POST',
    processData: false,
    contentType: false,
    data: new FormData(form[0]),
    async: true
  });
}

/**
 * Warn user canceled the submission of an Actor from a modal form
 *
 * @returns an empty response with the status code
 */
function cancelFromModal() {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.cancelFromModal().ajax({ async: true });
}

/**
 * Cancel the transformation of an citation to a contextualized argument
 *
 * @returns a rendered message to warn user about the cancellation
 */
function cancelCitationToArgumentFromModal() {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.cancelFromModal().ajax({ async: true });
}

/**
 * Upload a picture for a given actor (in form)
 *
 * @param form the form query (post request) with the file and the id to post
 * @param actorId the actor id for whom we add a new picture
 * @returns {*|{}}
 */
function uploadActorPicture(form, actorId) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.uploadActorPicture(actorId).ajax({
    type: 'POST',
    data: new FormData(form[0]),
    contentType: false,
    processData: false,
		async: true
  });
}

/**
 * Get modal frames to handle auto-created actors, if any
 * @returns {*}
 */
function getAutoCreatedActors() {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getAutoCreatedActors().ajax({ async: true });
}

/**
 * Get modal frames to handle auto-created professors, if any
 * @returns {*}
 */
function getAutoCreatedProfessions() {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getAutoCreatedProfessions().ajax({ async: true });
}

/**
 * Get modal frame to add new affiliations to existing actor (id)
 *
 * @param id an actor id
 * @param isAffiliated true if the new affiliation must be affiliated
 * @param isPerson true if the new affiliation concerns a person, false for a organization
 * @returns {*|{}}
 */
function newAffiliations(id, isAffiliated, isPerson) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.newAffiliations(id, isAffiliated, isPerson).ajax({ async: true });
}

/**
 * Post new affiliations for given actor id
 *
 * @param id an actor id
 * @param isAffiliated true if the new affiliation must be affiliated
 * @param isPerson true if the new affiliation concerns a person, false for a organization
 * @param form the ActorAffiliationForm to submit
 * @returns {*|{}}
 */
function addAffiliations(id, isAffiliated, isPerson, form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.addAffiliations(id, isAffiliated, isPerson).ajax({
    type: 'POST',
    data: form.serialize(),
		async: true
  });
}

/**
 * Get an actor card (properties and avatar pane)
 *
 * @param id an actor id
 * @returns {*|{}}
 */
function getActorCard(id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActorCard(id).ajax({ async: true });
}

/**
 * Get all contributions of given actor
 *
 * @param id an actor id
 *
 * @returns the partial searchResult scala template, or an error message, if an error occurred
 */
function getActorContributions(id) {
	return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActorContributions(id).ajax({ async: true });
}

/**
 * Reset the actor type of a given actor. This operation also delete the linked person or organization from db
 *
 * @param id an actor id
 * @return the edit page for this actor
 */
function resetActorType(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.resetActorType(id).ajax({ async: true });
}

/**
 * Get the modal with all debates concerned by the given actor and socio key value
 *
 * @param actor an actor id
 * @param key a socio key to see
 * @param value the value of the socio key to see
 * @return the modal with all debates
 */
function getActorPositionsForSocioValue(actor, key, value) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.getActorPositionsForSocioValue(actor, key, value).ajax({ async: true });
}

/*
 * TEXTS
 */

/**
 * open modal for edit a text
 *
 * @param textId the text id to edit
 * @returns {*}
 */
function editText(textId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.editFromModal(textId).ajax({ async: true });
}

/**
 * Submit text form
 *
 * @param textId the text id
 * @param form the form with the text details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveText(textId, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.saveFromModal(textId).ajax({
        type: 'POST',
        processData: false,
        contentType: false,
        data: new FormData(form[0]),
        async: true
    });
}

/**
 * Find the list of contextualized arguments that are part of the given text limited by index
 *
 * @param text a text id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 */
function findTextArguments(text, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.findTextArguments(text, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Find the list of citations that are part of the given text limited by index and with filters
 *
 * @param text a text id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 */
function findTextCitations(text, fromIndex, toIndex, filters) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.findTextCitations(text, fromIndex, toIndex, filters).ajax({ async: true });
}

/**
 * Find a text by url
 *
 * @param url a url to search
 * @return the corresponding text, if any
 */
function findTextByUrl(url) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.findTextByUrl(url).ajax({ async: true });
}

/**
 * Get the text content of a html content found by the given url
 *
 * @param url a url for which we need is content
 * @return the html text content of the given url
 */
function getHtmlContent(url) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getHtmlContent(url).ajax({ async: true });
}

/**
 * Get modal frames to display text content, if any
 *
 * @param id a text id
 * @returns {*}
 */
function getTextContentModal(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getTextContentModal(id).ajax({ async: true });
}

/**
 * Search for a text
 *
 * @param query a string query
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @return {*}
 */
function searchText(query, fromIndex, toIndex) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.searchText(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Search for all text source name containing this term
 *
 * @param query the term to search for
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchTextSource(query, fromIndex, toIndex) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.searchTextSource(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get the annotated version of given text (from WDTAL service or cache)
 *
 * @param textId a text id
 * @returns {*|{}}
 */
function getAnnotatedText(textId, synchronous, highlighted) {
  synchronous = synchronous || false;
  highlighted = highlighted === undefined ? true : highlighted;
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getAnnotatedText(textId, highlighted).ajax({ async: !synchronous });
}

/**
 * Get the annotated text or the html web content
 *
 * @param textId a text id
 * @returns {*|{}}
 */
function getTextContentOrHtmlContent(textId, synchronous, highlighted) {
    synchronous = synchronous || false;
    highlighted = highlighted === undefined ? true : highlighted;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getTextContentOrHtmlContent(textId, highlighted).ajax({ async: !synchronous });
}

/**
 * Get the content of a text from a given url
 *
 * @param url an url to get the text and other properties from (typically a web page)
 * @returns {*|{}}
 */
function getTextContent(url) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getTextContent(url).ajax({ async: true });
}

/**
 * Get embed html code from a platform (Twitter, TikTok, ...) url
 *
 * @param url an url to get embed code
 * @param platform the type of platform (Twitter, TikTok, ...)
 * @returns *
 */
function getEmbed(url, platform) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getEmbed(url, platform).ajax({ async: true });
}

/**
 * Get the content of a PDF from a given url
 *
 * @param url an url pointing to a pdf file
 * @returns {*|{}}
 */
function extractPDFContent(url) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.extractPDFContent(url).ajax({ async: true });
}

/**
 * Get text language
 *
 * @param text a text content
 * @returns {*|{}}
 */
function getTextLanguage(text) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getTextLanguage(text).ajax({ async: true });
}

/**
 * Get the share media modal for given contribution
 *
 * @param idContribution a contribution id
 * @returns {*}
 */
function getShareMediaModal(id, pane, pov) {
    pane = pane === undefined ? 0 : pane;
    pov = pov === undefined ? 0 : pov;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getShareMediaModal(id, pane, pov).ajax({ async: true });
}

/**
 * Get the list of actors linked to given contribution with given role that redirect to given pane
 *
 * @param id a contribution id
 * @param role the role of the actor
 * @param pane the pane to redirect
 * @returns {*}
 */
function getContributionActors(id, role, pane) {
    pane = pane === undefined ? 0 : pane;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getContributionActors(id, role, pane).ajax({ async: true });
}


/**
 * open modal to find linked contributions by given contribution type to given contribution
 *
 * @param contributionId a contribution id
 * @returns {*}
 */
function getFindContributionsModal(contributionId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getFindContributionsModal(contributionId).ajax({ async: true });
}

/**
 * open modal or get the list of linked contributions by given contribution type to given contribution
 *
 * @param contributionId a contribution id
 * @param forContributionId the related contribution id
 * @param forContributionType the contribution type
 * @param contributionRole the role of related contribution
 * @param pane the pane to redirect
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 * @returns {*}
 */
function findContributions(contributionId, forContributionId, forContributionType, contributionRole, pane, fromIndex, toIndex, filters) {
    forContributionId = forContributionId != null ? forContributionId : -1;
    contributionRole = contributionRole != null ? contributionRole : -1;
    pane = pane != null ? pane : 0;
    fromIndex = fromIndex != null ? fromIndex : -1;
    toIndex = toIndex != null ? toIndex : -1;

    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.findContributions(contributionId, forContributionId, forContributionType, contributionRole, pane, fromIndex, toIndex, filters).ajax({ async: true });
}

/**
 * Get a text radiography (all texts having arguments with similarity links to this text's arguments)
 *
 * @param id a text id
 * @param sortby int value representing a ERadiographyViewKey
 * @returns {*}
 */
function textRadiography(id, sortby) {
  return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.textRadiography(id, sortby).ajax({ async: true });
}

/**
 * Get the name of a given contribution id
 *
 * @param id a contribution id
 * @returns {*}
 */
function getContributionName(id) {
    id = id == null ? -1 : id;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getContributionName(id).ajax({ async: true });
}

/**
 * Get the name of a given place id
 *
 * @param id a place id
 * @returns {*}
 */
function getPlaceName(id) {
    id = id == null ? -1 : id;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getPlaceName(id).ajax({ async: true });
}

/**
 * Get the name of a given technical type id
 *
 * @param value a technical value
 * @param id a technical id
 * @returns {*}
 */
function getTechnicalName(value, id) {
    value = value || "";
    id = id || "";
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getTechnicalName(id, value).ajax({ async: true });
}

/**
 * Get the name of a given profession id
 *
 * @param id a profession id
 * @returns {*}
 */
function getProfessionName(id) {
    id = id == null ? -1 : id;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getProfessionName(id).ajax({ async: true });
}

/**
 * Get the description of a given affiliation
 *
 * @param id an affiliation id
 * @returns {*}
 */
function getAffiliationDescription(id) {
    id = id == null ? -1 : id;
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getAffiliationName(id).ajax({ async: true });
}

/**
 * Get suggested contributions related to given contribution
 *
 * @param id a contribution id
 * @returns {*}
 */
function getSuggestionsContribution(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getSuggestionsContribution(id).ajax({ async: true });
}

/**
 * Find a list of Contribution ordered by number of visualization hits (actor, text and argument only)
 *
 * @param type a contribution type (may be -1 for actors, texts and arguments)
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param orderBy the way to order the entries
 * @returns {*}
 */
function getPopularEntries(type, fromIndex, toIndex, orderBy) {
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getPopularEntries(type, fromIndex, toIndex, orderBy).ajax({ async: true });
}

/**
 * Get the modal with all citations in the context contribution for the given actor
 *
 * @param context a context contribution id
 * @param actor an actor id
 * @returns {*}
 */
function getActorCitationInContext(context, actor) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getActorCitationInContext(context, actor).ajax({ async: true });
}

/**
 * Get the modal with all citations in the context contribution that come from given text
 *
 * @param context a context contribution id
 * @param text a text id
 * @returns {*}
 */
function getTextCitationInContext(context, text) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getTextCitationInContext(context, text).ajax({ async: true });
}

/**
 * Get the modal with all citations in the context contribution for the given sociography key and key value
 *
 * @param context a context contribution id
 * @param subContext a tag sub context id
 * @param key a ESocioGroupKey key id
 * @param keyValue the key value
 * @param shade the shade needed
 * @param isArgument true for arguments, false for positions
 * @returns {*}
 */
function getSociographyCitationInContext(context, subContext, key, keyValue, shade, isArgument) {
    subContext = subContext === undefined || !subContext ? -1 : subContext;
    shade = shade === undefined ? -1 : shade;
    isArgument = isArgument === undefined ? true : isArgument;

    startWaitingModal();

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getSociographyCitationInContext(context, subContext, key, keyValue, shade, isArgument).ajax({ async: true });
}

/**
 * Get the concurrent citations list of the main actor compared to given key / value actor(s) in given context / sub context
 *
 * @param context a context contribution id
 * @param subContext a sub context contribution id
 * @param actor the main actor id
 * @param key a ESocioGroupKey key id
 * @param keyValue the key value
 * @param isArgument true for arguments, false for positions
 * @return the concurrent citations list
 */
function getSociographyCitationsConcurrent(context, subContext, actor, key, keyValue, isArgument) {
    subContext = subContext === undefined || !subContext ? -1 : subContext;
    isArgument = isArgument === undefined ? true : isArgument;

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getSociographyCitationsConcurrent(context, subContext, actor, key, keyValue, isArgument).ajax({ async: true });
}

/**
 * Get all linked contributions with a text by viz pane type
 *
 * @param text the text id
 * @param type the name of the viz pane type
 * @param pov for a specific contains in pane, -1 if none
 * @returns {*}
 */
function getTextLinkedContributions(text, type, pov) {
    pov = pov === undefined ? -1 : pov;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getLinkedContributions(text, type, pov).ajax({ async: true });
}

/**
 * Get all citations for given text id
 *
 * @param id a text id
 * @returns {*}
 */
function getTextCitations(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getCitations(id).ajax({ async: true });
}

/**
 * Get the first argument id for given context contribution id
 *
 * @param id a context contributio id
 * @returns {*}
 */
function getFirstArgument(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getFirstArgument(id).ajax({ async: false });
}

/**
 * Check if a given domain name is a free source or not. Free means free of rights.
 *
 * @param domain the domain name to check
 * @return true if the given domain name is a free source
 */
function checkFreeSource(domain) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.checkFreeSource(domain).ajax({ async: false });
}

/*
 * CONTEXT CONTRIBUTION
 */

/**
 * Get the context contribution behind the contribution id
 *
 * @param id a context contribution id
 * @returns a debate or a text
 */
function getContextContribution(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getContextContribution(id).ajax({ async: true });
}

/*
 * ARGUMENTS
 */

/**
 * Search for arguments with given query (in standard form and authors)
 *
 * @param query a query string
 * @param type the type of argument, if any otherwise -1
 * @param shade the shade of argument, if any otherwise -1
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchArgument(query, type, shade, fromIndex, toIndex) {
    shade = shade === undefined ? -2 : shade;
    type = type === undefined ? -1 : type;
    fromIndex = fromIndex === undefined ? -1 : fromIndex;
    toIndex = toIndex === undefined ? -1 : toIndex;
	return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.searchArgument(query, type, shade, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Search for arguments dictionary with given query title
 *
 * @param query a query string
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchArgumentDictionary(query, fromIndex, toIndex) {
    fromIndex = fromIndex === undefined ? -1 : fromIndex;
    toIndex = toIndex === undefined ? -1 : toIndex;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.searchArgumentDictionary(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get modal frame to add a new link by either searching for an existing context contribution or adding new one
 *
 * @param contextId the context contribution where add this link
 * @param id an argument or citation id that will be linked to the new argument
 * @param shade the link shade that will bind both argument (or citation) (give one and future one), if any
 * @param fromAuto true if this is called from automatic transformation process
 * @returns {*|{}}
 */
function argumentSelection(contextId, id, shade, fromAuto) {
    shade = shade === undefined ? -1 : shade;
    fromAuto = fromAuto === undefined ? false : fromAuto;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.argumentContextSelection(contextId, id, shade, fromAuto).ajax({ async: true });
}

/**
 * Get all shades of a given argument type and timing
 *
 * @param type an argument type
 * @param timing a timing
 * @param lang language code to fetch the shade for
 * @return the list of all shades corresponding to the given argument type and timing
 */
function getArgumentShades(type, timing, lang) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.getArgumentShades(type, timing, lang).ajax({ async: true });
}

/**
 * Get the justification map for a given argument
 *
 * @param argument an argument id
 * @return the argument cartography pane
 */
function getJustificationMap(argument) {
    return jsRoutes.be.webdeb.presentation.web.controllers.viz.VizActions.getJustificationMap(argument).ajax({ async: true });
}

/**
 * open modal for create an argument
 *
 * @param contextId the context where add the argument
 * @param subContextId a tag sub context id
 * @param categoryId if the new argument is in a specific category
 * @param superArgumentId if the new argument is a sub argument of a specific super argument
 * @param shadeId the shade of the potential link
 * @returns {*}
 */
function newArgument(contextId, subContextId, categoryId, superArgumentId, shadeId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.newArgument(contextId, subContextId, categoryId, superArgumentId, shadeId).ajax({ async: true });
}

/**
 * open modal for create an argument from a citation justification link
 *
 * @param citationId a citation justification link id
 * @return the modal to complete the argument
 */
function newArgumentFromCitation(citationId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.newArgumentFromCitation(citationId).ajax({ async: true });
}

/**
 * open modal for edit an argument
 *
 * @param argId the argument id to edit
 * @returns {*}
 */
function editArgument(argId, contextId) {
    contextId = contextId === undefined ? -1 : contextId;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.editFromModal(argId, contextId).ajax({ async: true });
}

/**
 * Get citations linked with argument in given context
 *
 * @param contextId the context where add the argument
 * @param subContextId a tag sub context id
 * @param categoryId if the new argument is in a specific category
 * @param argumentId the argument id
 * @param shadeId the shade of the potential link
 * @returns {*}
 */
function getArgumentCitationLinks(contextId, subContextId, categoryId, argumentId, shadeId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.getArgumentCitationLinks(contextId, subContextId, categoryId, argumentId, shadeId).ajax({ async: true });
}

/**
 * Submit  argument form
 *
 * @param argId the argument id
 * @param contextId the context id
 * @param form the form with the argument details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveArgument(argId, contextId, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.saveFromModal(argId, contextId).ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * open modal for edit an argument dictionary
 *
 * @param argId the argument dictionary id to edit
 * @returns {*}
 */
function editArgumentDictionary(argId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.editDictionary(argId).ajax({ async: true });
}

/**
 * Submit argument dictionary form
 *
 * @param argId the argument dictionary id
 * @param form the form with the argument dictionary details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveArgumentDictionary(argId, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.saveDictionary(argId).ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Save links from argument viz
 *
 * @param fromArgument the argument where the save is performed
 * @param type the name of the viz pane type
 * @param linkType the linkType from carto
 * @param links json-representation of LinkForms with all justification links of given text
 * @returns {*}
 */
function saveLinks(fromArgument, type, linkType, links) {
    linkType = (linkType == null ? -1 : linkType);
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.saveLinks(fromArgument, type, linkType).ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: links,
        async: true
    });
}

/**
 * Save a link between two contextualized or simple arguments or between a contextualized argument and an citation
 *
 * @param from an (contextualized) argument id
 * @param to an other (contextualized) argument or an citation id
 * @param shade the shade id that will be used to bind both argument
 * @param group the group where save this link
 * @param context a context contribution or -1 or undefined if not needed
 * @returns {*}
 */
function saveLink(from, to, shade, group, context) {
    context = context === undefined ? -1 : context;
    shade = shade === undefined ? -1 : shade;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.saveLink(from, to, context, shade, group).ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: "{}",
        async: true
    });
}

/**
 * Cancel the creation of a new argument to be linked to another one
 *
 * @returns {*|{}}
 */
function abortLinkCreation() {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.argument.ArgumentActions.abortLinkCreation().ajax({ async: true });
}

/*
 * CITATIONS
 */

/**
 * Validate current step of the given citation form
 *
 * @param form the citation form to validate
 * @returns either a bad request, internal server error, or a success status code
 */
function citationStepValidation(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.validateStep().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Add selected shade in session on the citationSelection process.
 *
 * @param argId an argument id
 * @param shade an argument link shade, if any
 * @return ok if given argId and shade are good
 */
function citationSelectionShade(argId, shade) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.citationSelectionShade(argId, shade).ajax({ async: true });
}

/**
 * See all arguments that are linked with the given citation
 *
 * @param excId an citation id
 * @return {*}
 */
function seeCitationArguments(excId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.seeCitationArguments(excId).ajax({ async: true });
}

/**
 * open modal for a new citation with given excerpt
 *
 * @param textId the text where add the citation
 * @param excerpt the original excerpt of the new citation
 * @returns {*}
 */
function newCitation(textId, excerpt) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.newCitation(textId, excerpt).ajax({ async: true });
}

/**
 * open modal for edit an citation
 *
 * @param excId the citation id to edit
 * @param fromContribution a contribution id from where we add the citation
 * @param contributionRole the role of the contribution in the citation
 * @returns {*}
 */
function editCitation(excId, fromContribution, contributionRole) {
    excId = excId == null ? -1 : excId;
    fromContribution = fromContribution === undefined ? -1 : fromContribution;
    contributionRole = contributionRole === undefined ? -1 : contributionRole;
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.editFromModal(excId, fromContribution, contributionRole).ajax({ async: true });
}

/**
 * Get modal frame to add citations in a particular context
 *
 * @param contextId the context where add the citations
 * @param subContextId a tag sub context id
 * @param categoryId if the citations must be in a specific category
 * @param argumentId if the citations justify an argument
 * @param isJustification true if a justification link must be add, false for a position link
 * @param shadeId the shade of the potential link
 * @returns {*|{}}
 */
function citationSelection(contextId, subContextId, categoryId, argumentId, isJustification, shadeId) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.citationSelection(contextId, subContextId, categoryId, argumentId, isJustification, shadeId).ajax({ async: true });
}

/**
 * Submit citation form
 *
 * @param excId the citation id
 * @param form the form with the citation details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveCitation(excId, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.saveFromModal(excId).ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Submit citation justification links form
 *
 * @param context       a contribution context id
 * @param subContext     a tag sub context id
 * @param category      a tag category id
 * @param argument an argument id
 * @param shade           a justification link shade
 * @param group           the group where save this link
 * @param ids the list of citations ids for which create the justification links
 * @returns {*|{}} either an empty ok response (200) or bad request if an error occured
 */
function saveCitationJustificationLinks(context, subContext, category, argument, shade, group, ids) {
    subContext = subContext != null && subContext ? subContext : -1;
    category = category != null ? category : -1;
    argument = argument != null ? argument : -1;

    let idsObject = {};
    idsObject.ids = ids;

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.saveCitationJustificationLinks(context, subContext, category, argument, shade, group).ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(idsObject),
        async: true
    });
}

/**
 * Submit citation position links form
 *
 * @param context       a contribution context id
 * @param subContext     a tag sub context id
 * @param shade           a position link shade
 * @param group           the group where save this link
 * @param ids the list of citations ids for which create the position links
 * @returns {*|{}} either an empty ok response (200) or bad request if an error occured
 */
function saveCitationPositionLinks(context, subContext, shade, group, ids) {
    subContext = subContext != null && subContext ? subContext : -1;

    let idsObject = {};
    idsObject.ids = ids;

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.saveCitationPositionLinks(context, subContext, shade, group).ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(idsObject),
        async: true
    });
}

/**
 * Search for citations base
 *
 * @param term the values to search for
 * @param type the type of citation browse
 * @param context the context where the browse is, if any
 * @param subContext a tag sub context id
 * @param category the tag category where the browse is, if any
 * @param argument the argument where the browse is, if any
 * @param isJustification true if a justification link must be add, false for a position link
 * @param shade a justification link shade, if any
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 * @param linkType a enum if a link must not already exists in context
 * @return the list of citations
 */
function searchCitationByType(term, type, context, subContext, category, argument, isJustification, shade, fromIndex, toIndex, filters, linkType) {
    context = context != null ? context : -1;
    subContext = subContext != null ? subContext : -1;
    category = category != null ? category : -1;
    argument = argument != null ? argument : -1;
    shade = shade != null ? shade : -1;
    filters = filters != null ? filters : "";
    linkType = linkType != null ? linkType : -1;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.searchCitationByType(term, type, context, subContext, category, argument, isJustification, shade, fromIndex, toIndex, filters, linkType).ajax({ async: true });
}

/**
 * Get the edit temporary citation form for given tmp citation to be added into current page
 *
 * @param excId id of tmp citation to retrieve
 * @returns {*}
 */
function editExternalCitation(excId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.validate.ValidateActions.editExternalCitation(excId).ajax({ async: true });
}

/**
 * Submit temporary citation form after manual validation
 *
 * @param excId the temporary citation id
 * @param form the form with the citation details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function submitExternalCitation(excId, form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.validate.ValidateActions.submitExternalCitation(excId).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Set the rejected state for given temporary argument, ie, if rejected is true, this argument will not be shown again.
 * Reversible action until the user quit the validation screen.
 *
 * @param excId a temporary argument id
 * @param rejected true to reject this argument
 * @returns {*}
 */
function setExternalContributionState(excId, rejected) {
  return jsRoutes.be.webdeb.presentation.web.controllers.validate.ValidateActions.setExternalContributionState(excId, rejected).ajax({ async: true });
}

/**
 * Set the rejected state for given temporary argument, ie, if rejected is true, this argument will not be shown again.
 * Reversible action until the user quit the validation screen.
 *
 * @param tweetID the WDTAL ID of the Tweet
 * @returns {*}
 */
function rejectTweet(tweetID) {
  return jsRoutes.be.webdeb.presentation.web.controllers.validate.ValidateActions.rejectTweet(tweetID).ajax({ async: true });
}

/**
 * Get the arguments structure of the given context
 *
 * @param context a context contribution
 * @return the context arguments structure if given super context / context exists
 */
function getContextArgumentStructure(context) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getContextArgumentStructure(context).ajax({ async: true });
}

/**
 * Move an element of the context arguments structure
 *
 * @param linkId the link to change
 * @param newSubContextId the new tag sub context id for the given link
 * @param newCategoryId the new tag category id for the given link
 * @param newSuperArgumentId the new super argument id for the given link
 * @param newShade the new link shade id for the given link
 * @param newLink true if it must be a new link, false otherwise
 * @param order the order of the link in the context
 * @returns {*}
 */
function changeContextArgumentStructure(linkId, newSubContextId, newCategoryId, newSuperArgumentId, newShade, newLink, order) {
    newSubContextId = newSubContextId != null ? newSubContextId : -1;
    newCategoryId = newCategoryId != null ? newCategoryId : -1;
    newSuperArgumentId = newSuperArgumentId != null ? newSuperArgumentId : -1;
    newShade = newShade != null ? newShade : -1;
    newLink = newLink != null ? newLink : false;
    order = order != null ? order : 0;

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.changeContextArgumentStructure(linkId, newSubContextId, newCategoryId, newSuperArgumentId, newShade, newLink, order).ajax({
        type: 'POST',
        async: true
    });
}

/**
 * Change the given justification link shade
 *
 * @param link a justification link id
 * @return ok if the link shade is well changed, badrequest otherwise
 */
function changeJustificationShade(link) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.changeJustificationShade(link).ajax({ async: true });
}

/**
 * Get the modal to add a text to a debate
 *
 * @param debate a debate id
 * @returns {*}
 */
function getAddTextToDebateModal(debate) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getAddTextToDebateModal(debate).ajax({ async: true });
}

/**
 * Save given debate has text
 *
 * @param debate a debate id
 * @param text a text id,
 * @param textForm if needed
 * @returns {*}
 */
function addTextToDebate(debate, text, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.addTextToDebate(debate, text).ajax({
        type: 'POST',
        processData: false,
        contentType: false,
        data: new FormData(form[0]),
        async: true
    });
}

/**
 * Get all contextualized argument for given context contribution id
 *
 * @param id a context contribution id
 * @returns {*}
 */
function getContextualizedArguments(id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getContextualizedArguments(id).ajax({ async: true });
}

/**
 * Get all contextualized argument for given context contribution id filter on given shade link type
 *
 * @param id a context contribution id
 * @param shade the link shade type to filter on
 * @returns {*}
 */
function getContextualizedArgumentsByShade(id, shade) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getContextualizedArgumentsByShade(id, shade).ajax({ async: true });
}

/**
 * Get all justification links for given text id
 *
 * @param id a context contribution id
 * @returns {*}
 */
function getJustificationLinks(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getJustificationLinks(id).ajax({ async: true });
}

/**
 * Get all justification links for given text id filter on given shade link type
 *
 * @param id a context contribution id
 * @param shade the link shade type to filter on
 * @returns {*}
 */
function getJustificationLinksByShade(id, shade) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.getJustificationLinksByShade(id, shade).ajax({ async: true });
}

/**
 * Save given justification links passed as serialized LinkForms
 *
 * @param text text id
 * @param links json-representation of LinkForms with all justification links of given text
 * @param group the group id in which the link must be saved
 * @returns {*}
 */
function saveJustificationLinks(text, links, group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.text.TextActions.saveJustificationLinks(text, group).ajax({
    type: 'POST',
    contentType: "application/json; charset=utf-8",
    data: links,
		async: true
  });
}

/**
 * Search for citations with given query title
 *
 * @param query a query string
 * @param textToLook the text id where to search, if any
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchCitation(query, textToLook, fromIndex, toIndex) {
    textToLook = textToLook === undefined ? -1 : textToLook;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.searchCitation(query, textToLook, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get the modal to change the position shade
 *
 * @param positionId a position citation link id
 * @returns {*}
 */
function changeCitationPositionShadeModal(positionId) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.changeCitationPositionShadeModal(positionId).ajax({ async: true });
}

/**
 * Save change of citation position link
 *
 * @param positionId a position citation link id
 * @param shade the new shade id
 * @returns {*}
 */
function changeCitationPositionShadel(positionId, shade) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.citation.CitationActions.changeCitationPositionShade(positionId, shade).ajax({
        type: 'POST',
        async: true
    });
}

/*
 * TAGS
 */

/**
 * Search for tags with given query
 *
 * @param query a query string
 * @param idToIgnore the tag id to ignore, or null
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchTag(query, type, fromIndex, toIndex) {
    type = type === undefined ? -1 : type;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.searchTag(query, type, fromIndex, toIndex).ajax({ async: true });
}

/**
 * open modal for create a tag category
 *
 * @param contextId the context where add the tag category
 * @param asCategory if the tag must be added as category, false if it is a sub debate of a multiple debate
 * @returns {*}
 */
function newTagCategory(contextId, asCategory) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.newTagCategory(contextId, asCategory).ajax({ async: true });
}

/**
 * Submit a link between tags
 *
 * @param parent the parent tag id
 * @param child the child tag id
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveTagLink(parent, child) {
  return jsRoutes.be.webdeb.presentation.web.controllers.tag.saveTagLink(parent, child).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * open modal for edit a tag
 *
 * @param tagId the tag id to edit
 * @param contextId the context where add the tag category
 * @param fromContribution a contribution id from where we add the citation
 * @param asParent true if the fromContribution is a tag, and that tag must be added as parent
 * @returns {*}
 */
function editTag(tagId, contextId, fromContribution, asParent) {
    contextId = contextId !== undefined ? contextId : -1;
    fromContribution = fromContribution !== undefined ? fromContribution : -1;
    asParent = asParent !== undefined ? asParent : true;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.editFromModal(tagId, contextId, fromContribution, asParent).ajax({ async: true });
}

/**
 * Handle tag form submission from modal frame
 *
 * @param form the tag form
 * @returns either a bad request with the content of the form in error, or a success status code
 */
function saveTagFromModal(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.saveFromModal().ajax({
      type: 'POST',
      data: form.serialize(),
      async: true
  });
}

/**
 * Get all linked contributions with a tag by viz pane type
 *
 * @param tag the tag id
 * @param type the name of the viz pane type
 * @param pov for a specific contains in pane, -1 if none
 * @returns {*}
 */
function getTagLinkedContributions(tag, type, pov) {
    pov = pov === undefined ? -1 : pov;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.getLinkedContributions(tag, type, pov).ajax({ async: true });
}

/**
 * Find the tag hierarchy of the given tag
 *
 * @param tagId a tag id
 * @param forParents true for the parents of the given tag
 * @param forContributionType a contribution type if tags must be sorted by linked contributions of given type
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 * @returns {*}
 */
function findHierarchyFromTag(tagId, forParents, forContributionType, fromIndex, toIndex, filters) {
    forParents = forParents != null ? forParents : false;
    forContributionType = forContributionType != null ? forContributionType : -1;
    fromIndex = fromIndex != null ? fromIndex : -1;
    toIndex = toIndex != null ? toIndex : -1;

    return jsRoutes.be.webdeb.presentation.web.controllers.entry.tag.TagActions.findHierarchy(tagId, forParents, forContributionType, fromIndex, toIndex, filters).ajax({ async: true });
}

/*
 * DEBATES
 */

/**
 * open modal for edit a debate
 *
 * @param debateId the debate id to edit
 * @param fromContribution a contribution id from where we add the citation
 * @returns {*}
 */
function editDebate(debateId, fromContribution) {
    fromContribution = fromContribution === undefined ? -1 : fromContribution;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.editFromModal(debateId, fromContribution).ajax({ async: true });
}

/**
 * Submit debate form
 *
 * @param debateId the debate id
 * @param form the form with the debate details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveDebate(debateId, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.saveFromModal(debateId).ajax({
        type: 'POST',
        processData: false,
        contentType: false,
        data: new FormData(form[0]),
        async: true
    });
}

/**
 * Validate current step of the given debate form
 *
 * @param form the debate form to validate
 * @returns either a bad request, internal server error, or a success status code
 */
function debateStepValidation(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.validateStep().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Search for debates with given query
 *
 * @param query a query string
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchDebate(query, fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.searchDebate(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get all linked contributions with a debate by viz pane type
 *
 * @param debate the debate id
 * @param type the name of the viz pane type
 * @param pov for a specific contains in pane, -1 if none
 * @returns {*}
 */
function getDebateLinkedContributions(debate, type, pov) {
    pov = pov === undefined ? -1 : pov;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.debate.DebateActions.getLinkedContributions(debate, type, pov).ajax({ async: true });
}

/*
 * CONTRIBUTORS and GROUPS
 */

/**
 * Post authenticate with open id
 *
 * @param form the whole form containing the open id form
 * @returns {*}
 */
function authenticateWithOpenId(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.authenticateWithOpenId().ajax({
        type: 'POST',
        data: form,
        async: true
    });
}

/**
 * Logout and clean the session.
 *
 * @return the main index page url
 */
function logoutAsync() {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.logoutAsync().ajax({ async: true });
}

/**
 * Get edit advices modal page
 *
 * @returns {*}
 */
function editAdvices() {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.editAdvices().ajax({ async: true });
}

/**
 * Post contributors advices
 *
 * @param form the whole form containing the advices form
 * @returns {*}
 */
function sendAdvices(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.sendAdvices().ajax({
        type: 'POST',
        data: new FormData(form[0]),
        contentType: false,
        processData: false,
        async: true
    });
}

/**
 * Get contact user to user form
 *
 * @returns {*}
 */
function userContactUser(contributor) {
    contributor = contributor === undefined ? -1 : contributor;
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.userContactUser(contributor).ajax({ async: true });
}

/**
 * Post contact user to user form
 *
 * @param form the whole form containing the contact form
 * @returns {*}
 */
function sendUserContactUser(form) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.sendUserContactUser().ajax({
        type: 'POST',
        data: new FormData(form[0]),
        contentType: false,
        processData: false,
        async: true
    });
}

/**
 * Get the modal to claim a contributin
 *
 * @return the modal
 */
function getClaimContribution(contribution, url) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.getClaimContribution(contribution, url).ajax({ async: true });
}

/**
 * Save a contributor claim about a contribution
 *
 * @form the claim form
 * @return ok if saved
 */
function saveClaimContribution(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.saveClaimContribution().ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: form,
        async: true
    });
}

/**
 * Delete given claim
 *
 * @returns {*}
 */
function deleteClaim(contribution, contributor) {
    startWaitingModal();
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.deleteClaim(contribution, contributor).ajax({ async: true });
}

/**
 * Retrieve claims
 *
 * @returns {*}
 */
function retrieveClaims(fromIndex, toIndex) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.retrieveClaims(fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get send mail to group modal page
 *
 * @returns {*}
 */
function editContributionsToExplore(contributionType) {
    contributionType = contributionType === undefined ? -1 : contributionType;
    return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.editContributionsToExplore(contributionType).ajax({ async: true });
}

/**
 * Post contributions to explore
 *
 * @param form the whole form containing the contributions to explore
 * @returns {*}
 */
function sendContributionsToExplore(contributionType, form) {
    contributionType = contributionType === undefined ? -1 : contributionType;
    return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.sendContributionsToExplore(contributionType).ajax({
        type: 'POST',
        data: new FormData(form[0]),
        contentType: false,
        processData: false,
        async: true
    });
}

/**
 * Get modal frame to search and select after an existing contribution
 *
 * @param contributionType a given contribution
 * @returns {*|{}}
 */
function contributionSelection(contributionType) {
    contributionType = contributionType === undefined ? -1 : contributionType;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.contributionSelection(contributionType).ajax({ async: true });
}

/**
 * Get contact us modal frame to contact administrator
 *
 * @returns {*}
 */
function getContactus() {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.contactus().ajax({ async: true });
}

/**
 * Set contact us modal frame to contact administrator
 *
 * @param form the whole form containing the contact form
 * @returns {*}
 */
function sendContactus(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.sendContactus().ajax({
        type: 'POST',
        data: new FormData(form[0]),
        contentType: false,
        processData: false,
        async: true
    });
}

/**
 * Upload a picture for a given contributor (in form)
 *
 * @param form the form query (post request) with the file and the id to post
 * @param contributorId the contributor id for whom we add a new picture
 * @returns {*|{}}
 */
function uploadContributorPicture(form, contributorId) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.uploadContributorPicture(contributorId).ajax({
    type: 'POST',
    data: new FormData(form[0]),
    contentType: false,
    processData: false,
		async: true
  });
}

/**
 * Get modal page to change current user's email
 *
 * @param id the contributor id (used to check match with user id in session cookie)
 * @returns {*|{}}
 */
function askChangeMail(id) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.askChangeMail(id).ajax({ async: true });
}

/**
 * Send request to change contributor mail
 *
 * @param id the contributor id asking for the request (used to check match with user id in session cookie)
 * @param form the form with the new email
 * @returns {*|{}}
 */
function changeMail(id, form) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.changeMail(id).ajax({
		type: 'POST',
		data: form.serialize(),
		async: true
	});
}

/**
 * Get modal page to change current user's password
 *
 * @param id the contributor id (used to check match with user id in session cookie)
 * @returns {*|{}}
 */
function askChangePassword(id) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.askChangePassword(id).ajax({ async: true });
}

/**
 * Send request to change contributor password
 *
 * @param id the contributor id asking for the request (used to check match with user id in session cookie)
 * @param form the form with the new password
 * @returns {*|{}}
 */
function changePassword(id, form) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.changePassword(id).ajax({
		type: 'POST',
		data: form.serialize(),
		async: true
	});
}

/**
 * Get modal page to delete contributor's account
 *
 * @param id the contributor id (used to check match with user id in session cookie)
 * @returns {*|{}}
 */
function askDeleteAccount(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.askDeleteAccount(id).ajax({ async: true });
}

/**
 * Send request to delete contributor's account
 *
 * @param id the contributor id (used to check match with user id in session cookie)
 * @param form the form with the deletion
 * @returns {*|{}}
 */
function deleteAccount(id, form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.deleteAccount(id).ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Get modal page to ask a password recovery
 *
 * @returns {*}
 */
function recoverPassword() {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.recoverPassword().ajax({ async: true });
}

/**
 * Send request to recover a lost password for email passed in form
 *
 * @param form a form containing an email
 * @returns {*}
 */
function sendPasswordRecovery(form) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.sendPasswordRecovery().ajax({
		type: 'POST',
		data: form.serialize(),
		async: true
	});
}

/**
 * Get modal page to resend the signup mail
 *
 * @returns {*}
 */
function resendSignupMail() {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.resendSignupMail().ajax({ async: true });
}

/**
 * Send the mail to confirme the subscription to WebDeb
 *
 * @param form a form containing an email
 * @returns {*}
 */
function sendSignupMail(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.sendSignupMail().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Get places by query
 *
 * @param query the query to excecute
 * @returns {*}
 */
function searchPlace(query){
  return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.searchPlace(query).ajax({ async: true });
}

/**
 * Get existing places by query
 *
 * @param query the query to excecute
 * @returns {*}
 */
function searchExistingPlace(query){
    return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.searchExistingPlace(query).ajax({ async: true });
}

/**
 * Set the user warned about old browser danger
 *
 * @returns {*}
 */
function userIsBrowserWarned(){
  return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.userIsBrowserWarned().ajax({ async: true });
}

/**
 * Leave from given group for current user 
 *
 * @param group a group id
 * @returns {*}
 */
function leaveGroup(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.leaveGroup(group).ajax({ async: true });
}

/**
 * Revoke given user from given group
 *
 * @param user a user id
 * @param group a group id
 * @param ban true if user must be banned, false to unban
 * @returns {*}
 */
function setBannedInGroup(user, group, ban) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.setBannedInGroup(user, group, ban).ajax({ async: true });
}

/**
 * Change default group for current user
 *
 * @param group a group id
 * @returns {*}
 */
function changeDefaultGroup(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.changeDefaultGroup(group).ajax({ async: true });
}

/**
 * Get modal page to search for groups
 *
 * @returns {*}
 */
function newSubscription() {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.newSubscription().ajax({ async: true });
}

/**
 * Retrieve a contributor group
 *
 * @param groupId the id of the group to retrieve
 * @returns {*}
 */
function retrieveContributorGroup(groupId) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.retrieveContributorGroup(groupId).ajax({ async: true });
}

/**
 * Search for groups (for typeahead)
 * 
 * @param query a partial group name to search for
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchGroups(query, fromIndex, toIndex) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.searchGroups(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Search for groups and get partial result page
 *
 * @param query a partial group name to search for
 * @returns {*}
 */
function getGroupResults(query) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.getGroupResults(query).ajax({ async: true });
}

/**
 * Add a contributor (retrieved from passed session cookie) as new member to given group
 * 
 * @param group a group id
 * @returns {*}
 */
function joinGroup(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.joinGroup(group).ajax({ async: true });
}

/**
 * Edit details of given group
 * 
 * @param group a group id 
 * @returns {*}
 */
function editGroup(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.editGroup(group).ajax({ async: true });
}

/**
 * Save details of given group
 *
 * @param form the form element containing the data to save
 * @returns {*}
 */
function saveGroup(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.saveGroup().ajax({
    type: 'POST',
    data: form.serialize(),
		async: true
  });
}

/**
 * Empty all contributions and member in given group and possibly delete completely the group itself
 * @param group a group id
 * @param deleteGroup true if the group itself must be deleted too
 * @returns {*}
 */
function emptyGroup(group, deleteGroup) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.emptyGroup(group, deleteGroup).ajax({ async: true });
}

/**
* Get the list of editable groups
*
* @returns {*}
*/
function loadGroups() {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.groups().ajax({ async: true });
}

/**
 * Change roles of member inside a given group
 * @param group a group id
 * @returns {*}
 */
function changeMembersRole(group) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.changeMembersRole(group).ajax({ async: true });
}

/**
 * Change role of member inside a given group
 * @param group a group id
 * @param userId the user to change the role if we only change one (-1 otherwise)
 * @returns {*}
 */
function changeMemberRole(group, userId) {
  userId = userId || -1;
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.changeMemberRole(group, userId).ajax({ async: true });
}

/**
 * Change members role
 *
 * @param group a group id
 * @param form the form element containing the data to save
 * @returns {*}
 */
function sendChangeMemberRole(group, form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.sendChangeMemberRole(group).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Send followed state to given group for given contributor
 *
 * @param group the id of group to follow
 * @param follow the follow state to put
 * @returns {*}
 */
function followGroup(group, follow) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.followGroup(group, follow).ajax({ async: true });
}

/**
 * Send followed state for all groups linked with given contributor
 *
 * @param form the form element containing the data to save
 * @returns {*}
 */
function followGroups(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.followGroups().ajax({
        type: 'POST',
        contentType: "application/json; charset=utf-8",
        data: form,
        async: true
    });
}

/**
 * Apply given role to given contributor in given group
 * 
 * @param contributor a contributor id
 * @param group a group id
 * @param role a role id
 * @returns {*|{}}
 */
function changeRole(contributor, group, role) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.changeRole(contributor, group, role).ajax({ async: true });
}

/**
 * Get modal page to invite new members in group
 * 
 * @param group a group id
 * @returns {*|{}}
 */
function inviteInGroup(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.inviteInGroup(group).ajax({ async: true });
}

/**
 * Send invitations to filled-in persons/emails 
 * 
 * @param form the form to serialize
 * @returns {*|{}}
 */
function sendInvitations(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.sendInvitations().ajax({
    type: 'POST',
    data: form.serialize(),
		async: true
  });
}

/**
 * Get modal page to send mail to group
 *
 * @param group a group id
 * @returns {*|{}}
 */
function getMailToGroupModal(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.getMailToGroupModal(group).ajax({ async: true });
}

/**
 * Send mail to group
 *
 * @param form the form to serialize
 * @returns {*|{}}
 */
function sendMailToGroup(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.sendMailToGroup().ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Search for contributors as a list of contributor holders
 * 
 * @param query a query string
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchContributors(query, fromIndex, toIndex) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.searchContributors(query, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Search for contributors as a partial result page (with their groups and roles)
 *
 * @param query a query string
 * @param sort either 'name' or 'date' for resp. sorting the result by name or registration date
 * @returns {*}
 */
function searchContributorsAndRoles(query, sort) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.searchContributorsAndRoles(query, sort).ajax({ async: true });
}

/**
 * Search for contributor's contributions
 *
 * @param searchText a query string
 * @param id the contributor id
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchContributorContributions(searchText, id, fromIndex, toIndex) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.ContributorActions.searchContributorContributions(searchText, id, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get the partial page with all csv reports
 *
 * @param type the contribution type that need reports
 */
function getCsvReports(type) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.getCsvReports(type).ajax({ async: true });
}

/**
 * Get modal page to upload csv files
 *
 * @param type the contribution type that need reports
 * @returns {*|{}}
 */
function importCsvFile(type) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.importCsvFile(type).ajax({ async: true });
}

/**
 * Upload given csv file(s) passed in form to be pushed into the DB
 *
 * @param form the form to serialize (containing the csv file-s-)
 * @param type the contribution type that need reports
 * @param charset the selected character set for the csv file
 * @param delimiter the selected delimiter for the csv file
 * @param groupid the group id in which the import must be performed
 * @returns {*|{}}
 */
function uploadCsvFile(form, type, charset, delimiter, groupid) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.uploadCsvFile(type, charset, delimiter, groupid).ajax({
		type: 'POST',
		data: new FormData(form[0]),
		contentType: false,
		processData: false,
		async: true
	});
}

/**
 * Get the list of rss sources (partial html page)
 *
 * @returns {*}
 */
function getRssFeeders() {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.getRssFeeders().ajax({ async: true });
}

/**
 * Get the modal page to edit given rss channel
 *
 * @returns {*}
 */
function editRssFeed(id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.editRssFeed(id).ajax({ async: true });
}

/**
 * Activate / Deactivate Rss source feeding
 *
 * @param id an Rss source id
 * @param activate true if this source must be activated
 * @returns {*|{}}
 */
function activateRssFeed(id, activate) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.activateRssFeed(id, activate).ajax({ async: true });
}

/**
 * Save given rss feed form
 *
 * @param form an rss form to save
 * @returns {*}
 */
function saveRssFeed(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.saveRssFeed().ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Remove Rss source feeding
 *
 * @param id an Rss source id
 * @returns {*|{}}
 */
function removeRssFeed(id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.removeRssFeed(id).ajax({ async: true });
}

/**
 * Get the list of twitter accounts (partial html page)
 *
 * @returns {*}
 */
function getTwitterAccounts() {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.getTwitterAccounts().ajax({ async: true });
}

/**
 * Get the modal page to edit given twitter account
 *
 * @returns {*}
 */
function editTwitterAccount(id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.editTwitterAccount(id).ajax({ async: true });
}

/**
 * Save given twitter account form
 *
 * @param form a twitter form to save
 * @returns {*}
 */
function saveTwitterAccount(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.saveTwitterAccount().ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Delete Twitter account from feeding sources
 *
 * @param account a twitter account name
 * @returns {*|{}}
 */
function removeTwitterAcccount(account) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.removeTwitterAccount(account).ajax({ async: true });
}

/**
 * Delete TextFreeCopyrightSource
 *
 * @param idSource the id of the free source to delete
 * @returns {*|{}}
 */
function removeFreeCopyrightSource(idSource) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.removeFreeCopyrightSource(idSource).ajax({ async: true });
}


/*
 * PROJECT
 */

/**
 * Get the page to manage given project
 *
 * @param projectId the project to manage
 * @returns {*|{}}
 */
function manageProject(projectId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.manageProject(projectId).ajax({ async: true });
}

/**
 * Get a project activity modal as project form for the given id
 *
 * @param projectId the project id
 * @returns {*|{}}
 */
function getProjectActivity(projectId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.getProjectActivity(projectId).ajax({ async: true });
}

/**
 * Generate project users
 *
 * @param projectId a project id
 * @returns {*|{}}
 */
function generateProjectUsers(projectId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.generateProjectUsers(projectId).ajax({
        type: 'POST',
        async: true
    });
}

/**
 * Delete all project users
 *
 * @param projectId a project id
 * @returns {*|{}}
 */
function deleteProjectUsers(projectId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.deleteProjectUsers(projectId).ajax({
        type: 'POST',
        async: true
    });
}

/**
 * Get the modal page to edit given project
 *
 * @param projectId a project id
 * @returns {*|{}}
 */
function editProject(projectId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.editProject(projectId).ajax({ async: true });
}

/**
 * Get the modal page to edit given project group
 *
 * @param projectId the project id that owns the group
 * @param projectGroupId the project group id to edit
 * @returns {*|{}}
 */
function editProjectGroup(projectId, projectGroupId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.editProjectGroup(projectId, projectGroupId).ajax({ async: true });
}

/**
 * Get the modal page to edit given project subgroup
 *
 * @param projectId the project id context
 * @param projectGroupId the project group id that owns the subgroup
 * @param projectSubgroupId the project subgroup id to edit
 * @returns {*|{}}
 */
function editProjectSubgroup(projectId, projectGroupId, projectSubgroupId) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.editProjectSubgroup(projectId, projectGroupId, projectSubgroupId).ajax({ async: true });
}

/**
 * Submit project form
 *
 * @param form the form with the project details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveProject(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.saveProject().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Submit project group form
 *
 * @param form the form with the project group details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveProjectGroup(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.saveProjectGroup().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}

/**
 * Submit project subgroup form
 *
 * @param form the form with the project subgroup details
 * @returns {*|{}} either an empty ok response (200) or the full page to be reloaded
 */
function saveProjectSubgroup(form) {
    return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.project.ProjectActions.saveProjectSubgroup().ajax({
        type: 'POST',
        data: form.serialize(),
        async: true
    });
}


/**
 * Change current scope for contributor, ie, update scope id in cookie
 * 
 * @param group the selected group id (for current scope)
 */
function changeScope(group) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.changeCurrentScope(group).ajax({ async: true });
}

/**
 * Add given contribution to given group's scope
 *
 * @param group a group id
 * @param contribution a contribution id
 * @param type the contribution type id
 */
function addContributionToGroup(group, contribution, type) {
	return jsRoutes.be.webdeb.presentation.web.controllers.account.group.GroupActions.addContributionToGroup(group, contribution, type).ajax({ async: true });
}


/*
 * AJAX-based SEARCH
 */

/**
 * Execute a search query (post request) with a given search form
 *
 * @param form the form element that will be serialized to be sent to server
 * @param card boolean saying if contribution cards must be retrieved (optional, default false)
 * @param embedded boolean saying if the search is done from an embedded frame
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param filters a string that contains data for filter results
 * @returns {*|{}}
 */
function doSearch(form, card, embedded, fromIndex, toIndex, filters) {
    card = typeof card === 'boolean' ? card : false;
    embedded = typeof embedded === 'boolean' ? embedded : false;
    fromIndex = !isNaN(fromIndex) ? fromIndex : -1;
    toIndex = !isNaN(toIndex) ? toIndex : -1;
    filters = filters === undefined ? '' : filters;

	return jsRoutes.be.webdeb.presentation.web.controllers.browse.BrowseActions.doSearch(card, embedded, fromIndex, toIndex, filters).ajax({
    type: 'POST',
    data: form.serialize(),
		async: true
  });
}

/**
 * Search for contributions (auto-complete feature)
 *
 * @param query the search query
 * @param group the group to search for
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns a list of jsonified contribution holders
 */
function searchContributions(query, group, fromIndex, toIndex) {
	return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.searchContributions(query, group, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Search for professions as a partial result page
 *
 * @param query a query string
 * @param idToIgnore an profession id to ignore
 * @param json true if the return must be a json
 * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
 * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
 * @returns {*}
 */
function searchProfessions(query, idToIgnore, json, fromIndex, toIndex) {
    idToIgnore = idToIgnore === undefined ? -1 : idToIgnore;
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.searchProfessions(query, idToIgnore, json, fromIndex, toIndex).ajax({ async: true });
}

/**
 * Get modal page to edit profession names
 *
 * @param profession the profession concerned
 * @returns {*|{}}
 */
function editProfession(profession) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.editProfession(profession).ajax({ async: true });
}

/**
 * Confirm the editing of profession names
 *
 * @param form the form to serialize
 * @returns {*|{}}
 */
function sendEditProfession(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.entry.actor.ActorActions.sendEditProfession(form).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Get modal page to edit link between professions
 *
 * @param profession the profession concerned
 * @returns {*|{}}
 */
function editProfessionHasLink(profession) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.editProfessionHasLink(profession).ajax({ async: true });
}

/**
 * Confirm the editing of profession links
 *
 * @param form the form to serialize
 * @returns {*|{}}
 */
function sendProfessionHasLink(form, id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.sendProfessionHasLink(id).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/**
 * Get modal page to merge professions
 *
 * @param profession the profession concerned
 * @returns {*|{}}
 */
function mergeProfessions(profession) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.mergeProfessions(profession).ajax({ async: true });
}

/**
 * Confirm the merge of professions
 *
 * @param form the form to serialize
 * @returns {*|{}}
 */
function sendMergeProfessions(form, id) {
  return jsRoutes.be.webdeb.presentation.web.controllers.account.admin.AdminActions.sendMergeProfessions(id).ajax({
    type: 'POST',
    data: form.serialize(),
    async: true
  });
}

/*
 * OTHERS
 */

/**
 * Get all linked places to a given contribution
 *
 * @param id a contribution id
 * @param selectedPlace the selected place by the user if any
 */
function getContributionPlaces(id, selectedPlace){
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.getContributionPlaces(id, selectedPlace).ajax({ async: true });
}

/**
 * Get the modal frame to search a contribution to merge with given contribution
 *
 * @param id a contribution id
 * @param type its contribution type id
 * @returns {*|{}}
 */
function getMergeContributionModal(id, type) {
	return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.getMergeContributionsModal(id, type).ajax({ async: true });
}

/**
 * Get the modal frame to search a contribution to merge with given contribution
 *
 * @param id a contribution id
 * @param type its contribution type id
 * @param group the contributor group
 * @param force true to force
 * @param noChange true to no change
 * @returns {*|{}}
 */
function deleteContributionAsync(id, type, group, force, noChange) {
    group = group !== undefined ? group : 0;
    force = force !== undefined ? force : true;
    noChange = noChange !== undefined ? noChange : false;
    return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.delete(id, type, group, force, noChange, true).ajax({ async: true });
}

/**
 * Get the modal frame to show the history of a contribution
 *
 * @param id a contribution id
 * @param type its contribution type id
 * @returns {*|{}}
 */
function getContributionHistoryModal(id, type) {
	return jsRoutes.be.webdeb.presentation.web.controllers.entry.EntryActions.getContributionHistoryModal(id, type).ajax({ async: true });
}

/**
 * Check if the "accept cookies" message has already been showed on main page
 *
 * @returns {*}
 */
function acceptCookies() {
  return jsRoutes.be.webdeb.presentation.web.controllers.Application.acceptCookies().ajax({ async: true });
}

/**
 * Check if given picture file is offensive (nudity or violence)
 *
 * @param form the whole form containing the picture
 * @returns {*}
 */
function checkOffensivePicture(form) {
  return jsRoutes.be.webdeb.presentation.web.controllers.Application.checkOffensivePicture().ajax({
    type: 'POST',
    data: new FormData(form[0]),
    contentType: false,
    processData: false,
    async: true
  });
}

/**
 * Get help modal
 *
 * @returns {*}
 */
function getHelpModal(names){
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.getHelpModal().ajax({
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(names),
        async: true
    });
}

/**
 * Get a image modal for the given image
 *
 * @param path the path of the image
 * @returns {*}
 */
function imageModal(path) {
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.imageModal(path).ajax({ async: true });
}

/**
 * Display the modal with details about a collaborator
 *
 * @param id the id of the collaborator
 * @returns {*}
 */
function collaboratorDetails(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.collaboratorDetails(id).ajax({ async: true });
}

/*
 * EXPORTATION
 */

/**
 * Get the complete model description (all classes and attributes)
 *
 * @returns {*}
 */
function getModelDescription() {
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.getModelDescription().ajax({ async: true });
}

/**
 * Execute the given query to transform into a sql query to perform it into the DB et get the results as a list of list of values.
 *
 * @param query the query to execute
 * @returns {*}
 */
function executeApiQuery(query) {
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.executeApiQuery().ajax({
        type: 'POST',
        data: query,
        async: true
    });
}

/*
 * ELECTIONS 2019
 */

/**
 * Get data async about stats page for election 2019
 *
 * @param id the id of the page
 * @return {*}
 */
function otherElectionStatsPageAsync(id) {
    return jsRoutes.be.webdeb.presentation.web.controllers.Application.otherElectionStatsPageAsync(id).ajax({ async: true });
}

/*
 * MANUAL REDIRECTS
 * 
 * Using the same "pattern" as Play routes file to abstract concrete routes behind javascript functions.
 * Must be maintained together with the routes file
 */

function cancelCitationEdit(textId, excId) {
    window.location = "/entry/citation/cancel?textId=" + textId + "&excId=" + excId;
}

function redirectToEdit(id) {
  window.location = "/entry/edit?id=" + id;
}

function redirectToTextCitations(id) {
  window.location = "/entry/text/edit?id=" + id;
}

function redirectToExplore() {
	window.location = "/browse";
}

function redirectToContribute() {
	window.location = "/entry";
}

function redirectToBrowse(query, contributionId) {
    var contribution = (contributionId !== undefined ? "+id_contribution=" + contributionId : "");
	window.location = "/browse/query=" + encodeURIComponent(query) + contribution + "+all=true";
}

function deleteContribution(contribution, type, group, force, noChange) {
	window.location = '/entry/delete?id=' + contribution + '&type=' + type + '&group=' + group + '&force=' + force + '&noChange=' + noChange +' &async=false';
}

function redirectToEditActor(id) {
    window.location = "/entry/actor?id=" + id;
}

function redirectToEditTag(id) {
    window.location = "/entry/tag?id=" + id;
}

function redirectToEditDebate(id) {
    window.location = "/entry/debate?id=" + id;
}

function redirectToEditCitation(id) {
    window.location = urlOfEditCitation(id);
}

function redirectToDoMerge(originId, replacementId, contributionId) {
    window.location = "/entry/domerge?origin=" + originId + '&replacement=' + replacementId + '&type=' + contributionId;
}

function redirectToGivenLink(link) {
    window.location = link;
}

function redirectToLogin() {
    let userId = $('#user-id');

    if(userId.exists() && userId.val() && userId.val() > 0) {
        slideDefaultWarningMessage(Messages('general.contribution.private.edit.msg'));
    } else {
        window.location = "/login";
    }
}

function redirectToActorViz(id, pane) {
    window.location = urlOfActorViz(id, pane);
}

function redirectToDebateViz(id, pane) {
    window.location = urlOfDebateViz(id, pane);
}

function redirectToTextViz(id, pane, pov) {
    window.location = urlOfTextViz(id, pane, pov);
}

function redirectToTagViz(id, pane) {
    window.location = urlOfTagViz(id, pane);
}

function redirectToDetailsPage(id) {
    window.location = urlOfDetailsPage(id);
}

function redirectToNewCitation(id, excerpt) {
    window.location = encodeURI("/entry/citation/new?textId=" + id + "&excerpt=" + excerpt);
}

function openInNewTab(url){
    let win = window.open(url, '_blank');
    win.focus();
}

/*
 * URL locations
 */

function urlOfContribute() {
    return '/entry';
}

function urlOfEditCitation(id) {
    return '/entry/citation?id=' + id;
}

function urlOfDetailsPage(id) {
    return '/viz/details/' + id;
}

function urlOfActorViz(id, pane) {
    return '/viz/actor/' + id + (pane !== undefined ? '?pane=' + pane : '');
}

function urlOfDebateViz(id, pane) {
    return '/viz/debate/' + id + (pane !== undefined ? '?pane=' + pane : '');
}

function urlOfTextViz(id, pane, pov) {
    return '/viz/text/' + id +
        (pane !== undefined ? '?pane=' + pane : '') +
        (pov !== undefined ? '&pov=' + pov : '');
}

function urlOfTagViz(id, pane) {
    return '/viz/tag/' + id + (pane !== undefined ? '?pane=' + pane : '');
}