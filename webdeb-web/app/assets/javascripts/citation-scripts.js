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
 * This collection of functions are meant to handle citation-related javascript functions, mainly dedicated to
 * manage insertion of citations from either texts annotation or tweet validation workflow.
 *
 * author Fabian Gilson
 * author Martin Rouffiange
 */

/**
 * Initialize listener o citation form fieldset
 *
 * param container main div containing the form
 * param firstLoad true if the panel is loaded for the first time
 * param fromModal true if the citation comes from a modal
 */
function manageCitationPanel(container, firstLoad, fromModal, reloadDragnDrop) {
    firstLoad = firstLoad !== undefined ? firstLoad : false;
    fromModal = fromModal !== undefined ? fromModal : false;
    reloadDragnDrop = reloadDragnDrop !== undefined ? reloadDragnDrop : false;

    let id = container.find('#id');
    let reloadDragnDropInput = container.find('#reloadDragnDrop');

    let textId = container.find('#textId');
    let textTitle = container.find('#textTitle');
    let textUrl = container.find('#text_url');
    let textUrl2 = container.find('#text_url2');
    let textTitleOrUrl = container.find('#text_titleOrUrl');

    let authors = container.find('#authors');
    let authorsPers = container.find('#authorsPers');
    let authorsOrgs = container.find('#authorsOrgs');
    let authorsTextPers = container.find('#authorsTextPers');
    let authorsTextOrgs = container.find('#authorsTextOrgs');
    let reporters = container.find('#reporters');
    let citedactors = container.find('#citedactors');
    let textAuthors = container.find('#text_authors');

    let persons = [ authorsPers, authorsTextPers];
    let organisations = [ authorsOrgs, authorsTextOrgs];
    let actors = [ authors, reporters, citedactors, textAuthors];
    let allActors = persons.concat(organisations.concat(actors));

    let tags = container.find('#tags');
    let places = container.find('#places');

    let submitBtn = container.find('.submit').first();

    let originalExcerpt = container.find('#originalExcerpt');
    let workingExcerpt = container.find('#workingExcerpt');
    let workingExcerpt1 = container.find('#workingExcerpt1');
    let workingExcerpt2 = container.find('#workingExcerpt2');
    let citationPreview = container.find('.citation-preview');

    let originalExcerptMaxLength = container.find('#originalExcerptMaxLength').val();
    let workingExcerptMaxLength = container.find('#workingExcerptMaxLength').val();

    let textPublicationDate = container.find('#publicationDateGroup');

    if(firstLoad){
        container.find('form').stepFormJs({
            validationCall : citationStepValidation,
            panelManager : manageCitationPanel,
            alwaysDisplaySubmit : !isNaN(id.val()) && id.val() >= 0,
            beginStep: (((!id.val() || (!isNaN(id.val()) && Number.parseInt(id.val()) === -1)) && (!textId.val() || textId.val() === "-1")) || !originalExcerpt.val() || originalExcerpt.val().length > 600 ? null : 2),
            canStepEnd: 3,
            submissionCall: fromModal ? saveCitationFromModal : null,
            toCheckSyncCall : fromModal ? null : treatHandleContributionMatchesAsyncFunction,
            toCheckSyncCallFunc : fromModal ? null : saveCitation,
            cancelRedirection : fromModal ? null : true
        });

        reloadDragnDropInput.val(reloadDragnDrop);
    } else {

        textTitle.typeahead('destroy');
        textTitleOrUrl.typeahead('destroy');
        textUrl.off();

        allActors.forEach(actor => {
            actor.find('input').typeahead('destroy');
            actor.off('click');
        });

        places.find('input').typeahead('destroy');
        tags.find('input').typeahead('destroy');

        places.off('click');
        tags.off('click');

        originalExcerpt.off('change focusout keyup mouseup');
        workingExcerpt1.off('change focusout keydown keyup mouseup');
        workingExcerpt2.off('change focusout keyup mouseup');
    }

    // manage tooltip (help) bubbles
    handleHelpBubble(container);

    // for all actors fields, bind typeahead and listeners
    persons.forEach(actor => {
        addActorsTypeahead(actor, 'fullname', '0');
    });
    organisations.forEach(actor => {
        addActorsTypeahead(actor, 'fullname', '1');
    });
    actors.forEach(actor => {
        addActorsTypeahead(actor, 'fullname', '-1');
    });
    allActors.forEach(actor => {
        addActorsTypeahead(actor, 'affname', '1', undefined, false);
        manageActorButton(actor, ['fullname', 'affname'], '-1');
    });

    textUrl2.on('focusout', function () {
        if(isValidUrl(textUrl2.val())){
            findTextByUrl(textUrl2.val()).done(function (textId) {
                textUrl.val(textUrl2.val());
            }).fail(function () {
                getTextFromUrl(textUrl2, 'text_');
            });
        }
    });

    textUrl.keyup(function(e){

        if(isValidUrl(textUrl.val())){
            findTextByUrl(textUrl.val()).done(function (textId) {
                textUrl2.val(textUrl.val());
            }).fail(function () {
                getTextFromUrl(textUrl, 'text_');
            });
        }
    });

    textTitleOrUrl.keyup(function(e){
        if(isValidUrl(textTitleOrUrl.val())){
            textUrl.val(textTitleOrUrl.val());
        }
    });

    addPlacesTypeahead(places);
    addTagsTypeahead(tags, 0);

    addTextTypeahead(textTitle);
    addTextTypeahead(textTitleOrUrl);

    manageAddRmButton(places, [''], '', null, addPlaceTypeahead);

    inputWithMaxLength(originalExcerpt, originalExcerptMaxLength);

    citationEdition(workingExcerpt1);

    citationPreview.text(workingExcerpt2.text());

    originalExcerpt.on('change focusout keyup', function(){
        updateWorkingExcerpt($(this))
    });

    inputWithMaxLength(workingExcerpt1, workingExcerptMaxLength);

    workingExcerpt1.on('change focusout keyup', function(){
        updateWorkingExcerpt($(this))
    });

    inputWithMaxLength(workingExcerpt2, workingExcerptMaxLength);

    workingExcerpt2.on('change focusout keyup', function(){
        updateWorkingExcerpt($(this))
    });

    // publication date widget management
    updateRoundedbox(textPublicationDate);
        textPublicationDate.find('.input-group-action2').each(function() {
            listenerOnRoundedBox('#publicationDateGroup');
    });

    function updateWorkingExcerpt(input) {
        let text = input.val();
        workingExcerpt.val(text);
        workingExcerpt1.val(text);
        workingExcerpt2.val(text);
        citationPreview.text(text);
    }
}

function manageCitationSelectionPanel(modal, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, reloadDragnDrop){
    let waitForIt = modal.find('.search-waiting');
    let textId = modal.find('.text-id');
    let saveBtn = modal.find('#submit-btn');
    let searchButtons = [];
    let searchInputs = [];
    let searchBtn;
    let reloadBtn;
    let searchInput;

    modal.find('.nav-item').on('click', function(){
        modal.find('.modal-footer').toggle(!$(this).is('#tab_new-tab'));
    });

    modal.find('#tab_search-tab').click(function(){
        modal.find('.modal-footer').show();
    });

    modal.find('#tab_new-tab').click(function(){
        modal.find('.modal-footer').hide();
    });

    modal.find('.step-btn').click(function(e){
        modal.find('.tab-new').hide();
        modal.find('.citationdiv').children('nav').first().hide();
        modal.find('.citationdiv').children('hr').first().hide();
    });

    handleSearchCitation(modal);

    for(let iType = 0; iType <= 5; iType++){
        searchBtn = modal.find('#button-addon-citation-' + iType);
        reloadBtn = modal.find('#button-reload-citation-' + iType);
        searchInput = modal.find('#search-citation-input-' + iType);

        searchBtn.on('click', function(){
            doSearchCitationByType(modal, iType, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, waitForIt);
        });

        reloadBtn.on('click', function(){
            doSearchCitationByType(modal, iType, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, waitForIt);
        });

        if(textId.val() > -1 && iType === 4) {
            searchBtn.click();
        }

        if(iType === 5) {
            searchBtn.click();
        }

        searchInput.on('typeahead:selected', function (obj, datum) {
            doSearchCitationByType(modal, iType, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, waitForIt);
        });

        searchInput.on('keydown', function(e) {
            e.preventDefault;
            if (e.which === 13) {
                searchInput.focusout();
                doSearchCitationByType(modal, iType, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, waitForIt);
            }
        });

        searchBtn.parents('form').submit(function(){
            return false;
        });

        searchButtons.push(searchBtn);
    }

    saveBtn.on('click', function(){
        let selectedCitations = modal.find('.contributionPop_container.selected');

        if(selectedCitations.exists()) {
            let ids = [];

            selectedCitations.each(function(){
                ids.push($(this).data('id'));
            });

            ids = Array.from(new Set(ids));

            if(ids.length > 0) {
                if(isJustification) {
                    doSaveCitationJustificationLinks(modal, contextId, subContextId, categoryId, superArgumentId, shadeId, ids, waitForIt, reloadDragnDrop);
                } else {
                    doSaveCitationPositionLinks(modal, contextId, subContextId, shadeId, ids, waitForIt);
                }
            }
        }
    });

}

/**
 * open modal for a new citation with given excerpt
 *
 * @param textId the text where add the citation
 * @param excerpt the original excerpt of the new citation
 */
function openNewCitationModal(textId, excerpt) {
    return new Promise(resolve => {
        newCitation(textId, excerpt).done(function (html) {
            loadAndShowModal($('#modal-anchor'), html);
            let modal = $('#modal-citation');
            manageCitationPanel(modal, true, true);
            resolve();
        }).fail(function (jqXHR) {
            stopWaitingModal();
            if (jqXHR.status === 401) {
                redirectToLogin();
            } else {
                console.log("Error with citation edit modal");
            }
            resolve();
        });
    });
}

/**
 * open modal for edit or create a citation
 *
 * @param citationId the citation id to edit (-1 for new one)
 * @param contextId the context where add the citation
 * @param subContextId a tag sub context id
 * @param categoryId if the newcitation is in a specific category
 * @param superArgumentId if the citation justify an argument
 * @param isJustification true if a justification link must be add, false for a position link
 * @param shadeId the shade of the potential link
 * @param fromContribution a contribution id from where we add the citation
 * @param contributionRole the role of the contribution in the citation
 */
function openEditCitationModal(citationId, reloadDragnDrop, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, fromContribution, contributionRole) {
    contextId = contextId != null ? contextId : -1;
    subContextId = subContextId != null ? subContextId : -1;
    categoryId = categoryId != null ? categoryId : -1;
    superArgumentId = superArgumentId != null ? superArgumentId : -1;
    isJustification = isJustification != null ? isJustification : true;
    shadeId = shadeId != null ? shadeId : -1;
    fromContribution = fromContribution != null ? fromContribution : -1;
    contributionRole = contributionRole != null ? contributionRole : -1;

    let selection = citationId == null || isNaN(citationId);

    (!selection ? editCitation(citationId, fromContribution, contributionRole) : citationSelection(contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId)).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);
        let modal = $("#modal-citation" + (!selection ? '' : '-selection'));
        manageCitationPanel(modal, true, true, reloadDragnDrop);
        if(selection)
            manageCitationSelectionPanel(modal, contextId, subContextId, categoryId, superArgumentId, isJustification, shadeId, reloadDragnDrop);
    }).fail(function (jqXHR) {
        stopWaitingModal();
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with citation edit modal");
        }
    });
}

function saveCitationFromModal(container) {
    let id = container.find('#id').val();
    let contextId = container.find('#contextId').val();
    let reloadDragnDrop = container.find('#reloadDragnDrop').val();
    let modal = container.parents('#modal-citation-selection').exists() ?
        container.parents('#modal-citation-selection') : container.parents('#modal-citation');
    let form = modal.find('#citation-form');
    let autocreated_modal = null;

    startWaitingModal();

    saveCitation(id, form).done(function (data) {
        //window.location.replace(data.replace(/"/g, ''));
        hideAndDestroyModal(autocreated_modal);
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        triggerReloadVizEvent('.debate-' + contextId, reloadDragnDrop === "true");
        stopWaitingModal();
    }).fail(function (xhr) {
        let status = xhr.responseText.length === 0 ? -1 : xhr.status;

        stopWaitingModal();

        switch (status) {
            case 400:
                modal.show();
                // form has errors => clear form and rebuilt
                replaceContent(form, xhr.responseText);
                manageCitationPanel(container.parent(), true, reloadDragnDrop);
                fadeMessage();
                break;

           case 409:
                modal.hide();
                // actor names match
                treatHandleContributionMatchesModal(container, xhr.responseText);
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(autocreated_modal);
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

function doSaveCitationJustificationLinks(modal, contextId, subContext, category, argument, shade, ids, waitForIt, reloadDragnDrop){
    saveCitationJustificationLinks(contextId, subContext, category, argument, shade, modal.find('#inGroup').val(), ids).done(function(data){
        hideAndDestroyModal(modal);
        triggerReloadVizEvent('.debate-' + subContext, reloadDragnDrop);
        slideDefaultSuccessMessage();
    }).fail(function(xhr){
        hideAndDestroyModal(modal);

        if(xhr.status === 409){
            slideMessage($('#warning-link-exists'));
        }else{
            slideDefaultErrorMessage();
        }
    });
}

function doSaveCitationPositionLinks(modal, contextId, subContext, shade, ids, waitForIt){
    saveCitationPositionLinks(contextId, subContext, shade, modal.find('#inGroup').val(), ids).done(function(data){
        hideAndDestroyModal(modal);
        triggerReloadVizEvent();
        slideDefaultSuccessMessage();
    }).fail(function(xhr){
        hideAndDestroyModal(modal);

        if(xhr.status === 409){
            slideMessage($('#warning-link-exists'));
        }else{
            slideDefaultErrorMessage();
        }
    });
}


function doSearchCitationByType(modal, eType, context, subContext, category, argument, isJustification, shade, waitForIt){
    let term = modal.find('#search-citation-input-' + eType).val();
    let resultContainer = $('#search-citation-' + eType);

    waitForIt.show();

    searchCitationByType(term, eType, context, subContext, category, argument, isJustification, shade, 0, 100).done(function(data){

        resultContainer.html(data);

        toggleSummaryBoxes(resultContainer, null, true);
        waitForIt.hide();

    }).fail(function(){
        slideDefaultErrorMessage();
    });
}

function citationOptionsHandler(container, isModal){

    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').exists() ? container.find('.modal').first() : container.parents('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    claimContributionListener(container);

    // redirect to the text linked to an citation
    let seeHistoryBtn = container.find('.citation-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        getContributionHistoryModal(getContributionOptionData($(this), "id"), 3).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });


    // redirect to the text linked to an citation, to the citations pane
    let seeTextBtn = container.find('.citation-see-text-btn');
    seeTextBtn.off('click');
    seeTextBtn.on('click', function() {
        redirectToTextViz(getContributionOptionData($(this), "text"), 3);
    });

    // open edit citation modal
    let editBtn = container.find('.citation-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        openEditCitationModal(getContributionOptionData($(this), "id"), actionFromDragnDrop($(this)));
    });

    // redirect to modal for adding a citation to the category
    let addCitationInContributionBtn = container.find('.contribution-add-citation-btn');
    addCitationInContributionBtn.off('click');
    addCitationInContributionBtn.on('click', function() {
        openEditCitationModal(
            -1, null, null, null, null, null, null, null,
            getContributionOptionData($(this), "id"),
            getContributionOptionData($(this), "role")
        );
    });

    // redirect to modal for create an argument from a citation
    let citationArgumentBtn = container.find('.citation-argument-btn');
    citationArgumentBtn.off('click');
    citationArgumentBtn.on('click', function() {
        openEditArgumentModal(null, null, null, null, null, null, null,
            getContributionOptionData($(this), "link"));
    });

    // redirect to modal for change citation position shade
    let changeCitationPositionShadeBtn = container.find('.citation-position-change-shade-btn');
    changeCitationPositionShadeBtn.off('click');
    changeCitationPositionShadeBtn.on('click', function() {
        let linkId = getContributionOptionData($(this), "position");
        let debateShadeId = $('.debate-shade-id').first().text();

        hideAndDestroyModal(container);
        changeCitationPositionShadeModal(linkId).done(function (html) {
            let modal = $('#modal-anchor');
            loadAndShowModal(modal, html);

            initPositionsListeners(
                modal.find('#modal-choose-citation-position-shade'),
                getContributionOptionData($(this), "id"),
                debateShadeId,
                false,
                linkId,
                modal.find('#link-shade').text());
        }).fail(function (jqXHR) {
            stopWaitingModal();
            if (jqXHR.status === 401) {
                redirectToLogin();
            } else {
                console.log("Error with change citation position shade modal");
            }
        });
    });

    // delete the given justification link
    let deleteLinkBtn = container.find('.citation-delete-link-btn');
    deleteLinkBtn.off('click');
    deleteLinkBtn.on('click', function() {
        let that = $(this);
        let linkType = getContributionOptionData(that, "link") !== -1;
        let linkId = linkType ? getContributionOptionData(that, "link") : getContributionOptionData(that, "position");
        let cType = linkType ? 10 : 17;

        showConfirmationPopup(function(){
            if(isModal)
                hideAndDestroyModal(container);

            doDeleteContributionAsync(linkId, cType, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"));
        }, null, "viz.admin.modal.delete." + cType + ".");
    });

    // delete the given justification link
    let forceDeleteLinkBtn = container.find('.citation-delete-force-link-btn');
    forceDeleteLinkBtn.off('click');
    forceDeleteLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            if(isModal)
                hideAndDestroyModal(container);

            doDeleteContributionAsync(getContributionOptionData(that, "link"), 10, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"), true);
        }, null, "viz.admin.modal.delete.10.");
    });

    // delete the given argument
    /*container.on('click', '.citation-delete-btn', function() {
        let that = $(this);
        showConfirmationPopup(function(){doDeleteContributionAsync(getContributionOptionData(that, "id"), 3, 0, true)}, null, "viz.admin.modal.delete.4.");
    });*/

    // show details
    let showDetailsBtn = container.find('.citation-show-details-btn');
    showDetailsBtn.off('click');
    showDetailsBtn.on('click', function() {
        redirectToDetailsPage(getContributionOptionData($(this), "id"));
    });

}

function citationEdition(element){
    let history = [];

    element.on('keydown', function(evt){
        // ctrl+z (undo)
        if(evt.which === 90 && evt.ctrlKey && history.length > 0){
            element.val(history[0]);
            history.shift();
        } // deletion
        else if(evt.which === 8 || evt.which === 46) {
            history.unshift(element.val());
            if(history.length > 30) {
                history.pop();
            }
        } // not arrows
        else if(!(evt.which >= 37 && evt.which <= 40)) {
            evt.preventDefault();
        }
    });

    element.on('keyup', function(evt){
        // only deletion
        if(evt.which === 8 || evt.which === 46) {
            let citation = element.val().trim();
            let selection = element[0].selectionStart;

            if(citation.length > selection) {
                let beginPart = citation.substring(0, selection).trim();
                let endPart = citation.substring(selection).trim();

                if(!beginPart.endsWith("[...]") && !endPart.startsWith("[...]")) {
                    element.val(beginPart + " [...] " + endPart);
                } else if(beginPart.endsWith("[...]") && endPart.startsWith("[...]")) {
                    element.val(beginPart.substring(0, beginPart.length - 5) + endPart);
                } else {
                    element.val(beginPart + " " + endPart);
                }
            } else {
                element.val(citation + " [...] ");
            }

            element[0].selectionStart = selection;
            element[0].selectionEnd = selection;
        }
    });

    element.on('contextmenu', function(evt){
        evt.preventDefault();
    });
}

/**
 * open the modal to display all arguments linked to an citation
 *
 * param id an citation id
 */
function openCitationArgumentsModal(id) {
    seeCitationArguments(id).done(function (html) {
        loadAndShowModal( $('#modal-anchor'), html);
    }).fail(function (jqXHR) {
        console.log("Error with citation arguments modal");
    });
}

function handleSearchCitation(container) {
    let searchCitationContainers = container.find('.search-citation-container');

    searchCitationContainers.each(function(){
        let type = $(this).data('type');
        let input = $(this).find('input');

        switch(type) {
            case 0 :
                addActorTypeahead(input, -1);
                break;
            case 1 :
                addTextTypeahead(input);
                break;
            case 2 :
                addDebateTypeahead(input);
                break;
            case 3 :
                addTagTypeahead(input, 0);
                break;
            case 4 :
                addCitationTypeahead(input);
                break;
        }
    });
}

function drawCitationContainer(citation) {

    return '<div class="d-flex align-items-center justify-content-center">' +
    '    <div class="flex-grow-1">' +
        '   <div class="contribution-container citation" data-id="' + citation.id + '">' +
    '            <div class="contribution-container-header ml-3">' +
    '                <span class="d-block citation-title mb-1 d-flex align-items-center">' +
    '                    <div class="mr-3">' +
    '                        <i class="fas fa-align-left"></i>' +
    '                    </div>' +
    '                    <div class="flex-grow-1">' +
                            citation.workingExcerpt +
    '                    </div>' +
    '                </span>' +
    '                <span class="d-block text-muted font-italic">' +
    '                    — ' + citation.authorsDescriptionWithLink + ',' +
    '                    <a class="normal-style primary" href="' + urlOfTextViz(citation.textId, -1) +'">' +
                            citation.textTitle +
    '                    </a>' +
                        (citation.publicationDate ? ', ' + citation.publicationDate : '') +
                        (citation.source ?
                        ', <a class="normal-style primary" href="' + citation.textUrl + '" target="_blank">' +
                                citation.source +
    '                        </a>' : '') +
    '                </span>' +
    '            </div>' +
    '        </div>' +
    '    </div>' +
    '    <div>' +
    '        <a type="button" href="' + urlOfEditCitation(citation.id) +'" class="btn btn-link primary">' +
    '            <i class="fas fa-eye"></i>' +
    '        </a>' +
    '    </div>' +
    '    <div>' +
    '        <a type="button" href="' + urlOfEditCitation(citation.id) +'" class="btn btn-link primary">' +
    '            <i class="fas fa-edit"></i>' +
    '        </a>' +
    '    </div>' +
    '</div>' +
    '<hr class="hr-xsmall">';

}

/*
function worksWithCitations(containerView, containerValue){
    containerView.text(containerView.text().trim());
    containerValue.val(containerValue.val().trim());
    let lock = false;

    addContextElementsToCitation(containerView);
    convertToWorkingExcerpt(containerView, containerValue);

    containerView.on("click", function(e) {
        if(!$(e.target).hasClass("wkg-ctx") && (!e.target.localName === "path" || !$(e.target.parentElement).hasClass("wkg-ctx"))) {
            let that = $(this);

            let userSelection = window.getSelection();
            let cursorIndex = userSelection.anchorOffset;
            let areaContent = userSelection.anchorNode.textContent;

            let nextIndexOfSpace = areaContent.substring(cursorIndex, areaContent.length).indexOf(" ");
            cursorIndex += (nextIndexOfSpace > -1 ? nextIndexOfSpace : areaContent.length);

            let firstPart = areaContent.substring(0, cursorIndex);
            let secondPart = areaContent.substring(cursorIndex + 1, areaContent.length);
            userSelection.anchorNode.textContent = firstPart + " [...] " + secondPart;

            openContributionSearchModal(0);

            $(document).on("contribution-selected", function (e, id, type, name) {
                containerView.text(containerView.text().replace('[...]','[' + id + ',' + type + ',' + name + ']'));
                that.html(addContextElementsToCitation(containerView));
                convertToWorkingExcerpt(containerView, containerValue);
            });

            $(document).on("contribution-selection-canceled", function () {
                containerView.text(containerView.text().replace('[...] ',''));
            });
        }
    });

    containerView.on("click", ".wkg-ctx", function(e) {
        if(!lock) {
            lock = true;
            $(this).closest(".wkg-ctx-first").remove();
            convertToWorkingExcerpt(containerView, containerValue);
            setTimeout(function(){lock = false;}, 10);
        }
    });
}

function addContextElementsToCitation(containerView){
    let ch = "";
    // get the text of the container without the children text to avoid deal with previously created span elements
    containerView.contents().each(function (index) {
        if(this.nodeType === 1){
            ch += this.outerHTML;
        }else{
            ch += addContextElementsInText(this.textContent);
        }
    });
    containerView.html(ch);
}

function addContextElementsInText(text) {
    let working = text;
    let regex = /\[([^)]+)\]/;
    let index = working.search(regex);
    let ch = index === -1 ? text : text.substring(0, index);

    while (index > -1) {
        let endIndex = working.indexOf("]");
        let val = working.substring(index + 1, endIndex);
        let values = val.split(",");
        working = working.substring(endIndex + 1, working.length);
        index = working.search(regex);
        let span = '<span class="wkg-ctx wkg-ctx-first primary" ' +
            'data-id="' + values[0] + '" data-type="' + values[1] + '">[' + values[2] + '&nbsp;' +
            '<span class="delete-working-context wkg-ctx" style="color : red">' +
            '<i class="fa fa-times-circle wkg-ctx"></i></span>]</span>';
        ch += " " + span + (index > -1 ? working.substring(0, index) : working);
    }

    return ch;
}

function convertToWorkingExcerpt(containerView, containerValue){
    let ch = "";
    containerView.contents().each(function( index ) {
        if(this.nodeType === 1){
            ch += (ch.endsWith(" ") ? "" : " ") +  "[" + $(this).data("id") + "," + $(this).data("type") + "]";
        }else{
            ch += this.textContent.trimEnd();
        }
    });
    containerValue.val(ch);
}*/