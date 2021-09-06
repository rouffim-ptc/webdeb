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
 * This javascript file contains all reusable functions for debate contributions' screens
 *
 * @author Martin Rouffiange
 */


/*
 * DEBATE EDIT MANAGEMENT
 */

/**
 * Initialize listener on debate form fieldset
 *
 * @param container main div containing the form
 * @param firstLoad true if the panel is loaded for the first time
 * @param fromModal true if the debate comes from a modal
 */
function manageDebatePanel(container, firstLoad, fromModal) {
    firstLoad = firstLoad !== undefined ? firstLoad : false;
    fromModal = fromModal !== undefined ? fromModal : false;

    let shade = container.find('#shade');
    let title = container.find('#title');

    let isMultipleContainer = container.find('#is_multiple');

    let places = container.find('#places');
    let authors = container.find('#authors');
    let tags = container.find('#tags');
    let links = container.find('#externalUrls');

    let description = container.find('#description');
    let picture = container.find('#pictureForm_filename');
    let pictureString = container.find('#pictureString');

    let submitBtn = container.find('.submit');

    if(firstLoad){
        container.find('form').stepFormJs({
            validationCall : debateStepValidation,
            panelManager : manageDebatePanel,
            submissionCall: fromModal ? saveDebateFromModal : null,
            toCheckSyncCall : fromModal ? null : treatHandleContributionMatchesAsyncFunction,
            toCheckSyncCallFunc : fromModal ? null : saveDebate,
            cancelRedirection : fromModal ? null : true
        });
    } else {
        title.typeahead('destroy');

        places.find('input').typeahead('destroy');
        authors.find('input').typeahead('destroy');
        tags.find('input').typeahead('destroy');

        places.off('click');
        authors.off('click');
        links.off('click');

        shade.off('change');
    }

    if(shade.children("option:selected").val().length === 0) {
        title.parents('.form-group').hide();
        addArgumentTypeahead(title, 1, shade.val());
    } else {
        shade.children('option[value=""]').remove();
    }

    manageDebateShadeFormChange(shade, title, isMultipleContainer);

    addPlacesTypeahead(places);
    addActorsTypeahead(authors, 'fullname', '-1');
    addTagsTypeahead(tags, 0);

    manageAddRmButton(places, [''], '', null, addPlaceTypeahead);
    manageActorButton(authors, [''], '-1');
    manageAddRmButton(links, [''], '');

    inputWithMaxLength(description, 1500);

    // manage tooltip (help) bubbles
    handleHelpBubble(container);

    picture.change(function(){
        updatePictureField($(this), pictureString);
    });
}

/**
 * Manage typeahead selection of a debate in fill information with data.
 * If current tag if a new one ask for editing existing. Otherwise, ask to merge.
 *
 * @param element the html field where the typeahead will be put
 * @param newDebate true if the current debate is a new one
 * @param currentDebate the current debate id
 */
function manageExistingDebate(element, newDebate, currentDebate) {
    addDebatesTypeahead(element, currentDebate);
    $(element).on('typeahead:selected', function (obj, datum) {
        // create modal popup for confirmation
        bootbox.dialog({
            message: Messages("entry.tag.existing." + (newDebate ? "" : "merge.") + "text"),
            title: Messages("entry.tag.existing.title"),
            buttons: {
                main: {
                    // do nothing if user chose not to load existing tag
                    label: Messages("entry.tag.new"),
                    className: "btn-primary",
                    callback: function () { /* do nothing, just dispose frame */
                    }
                },
                modify: {
                    label: Messages("entry.tag.btn." + (newDebate ? "modify" : "merge")),
                    className: "btn-default",
                    callback: function () {
                        if(newDebate) {
                            redirectToEditDebate(datum.id);
                        }else{
                            redirectToDoMerge(currentDebate, datum.id, 5);
                        }
                    }
                }
            }
        })
    })
}

/**
 * open modal for edit or create a debate
 *
 * param debateId a debate id
 * param fromContribution a contribution id from where we add the debate
 */
function openEditDebateModal(debateId, fromContribution, linkId) {
    fromContribution = fromContribution != null ? fromContribution : -1;

    editDebate(debateId, fromContribution).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);
        let modal = $("#modal-debate");
        manageDebatePanel(modal, true, true);
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with debate edit modal");
        }
    });
}

function saveDebateFromModal(container) {
    let id = container.find('#id').val();
    let modal = container.parents('#modal-debate');
    let form = modal.find('#debate-form');

    saveDebate(id, form).done(function (data) {
        hideAndDestroyModal(modal);
        triggerReloadVizEvent();
        slideDefaultSuccessMessage();
    }).fail(function (xhr) {
        let status = xhr.responseText.length === 0 ? -1 : xhr.status;

        switch (status) {
            case 400:
                modal.show();
                // form has errors => clear form and rebuilt
                replaceContent(form, xhr.responseText, 'form');
                manageDebatePanel(container, false);
                fadeMessage();
                break;

            case 406:
                replaceContent(form, xhr.responseText, 'form');
                manageDebatePanel(container, false);
                break;

            case 409:
                modal.hide();
                // actor names match
                treatHandleContributionMatchesModal(container, xhr.responseText);
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

function initPositionsListeners(container, debateId, debateShade, selectCitation, positionId, selectedShade) {

    if(selectedShade !== undefined) {
        changePositionRange(selectedShade);
    }

    container.on('click', '.position-range', function() {
        changePositionRange($(this).val());
    });

    container.on('click', '.add-position-btn', function() {
        let shadeId = $(this).parents('.add-position-container').find('.position-range').val();
        let subDebateId = $(this).data('sub-debate');

        if(selectCitation) {
            openEditCitationModal(
                null,
                false,
                debateId,
                subDebateId,
                null,
                null,
                false,
                shadeId
            );
        } else {
            hideAndDestroyModal(container);
            changeCitationPositionShadel(positionId, shadeId).done(function () {
                stopWaitingModal();
                triggerReloadVizEvent('.debate-' + subDebateId);
                slideDefaultSuccessMessage();
            }).fail(function (jqXHR) {
                stopWaitingModal();
                slideDefaultErrorMessage();
            });
        }
    });

    function changePositionRange(positionId) {
        let positionContainer = container.find('.add-position-container');
        let positionTitle = positionContainer.find('.position-range-title').children('span');

        positionTitle.text(Messages('viz.debate.position.' + debateShade + '.' + positionId));
        positionTitle.removeClass();
        positionTitle.addClass(positionIdToCss(positionId));

        container.find('.position-range').val(positionId);
    }

}

function positionIdToCss(id) {
    switch (Number(id)) {
        case 0 :
            return 'high_approbation';
        case 1 :
            return 'approbation';
        case 2 :
            return 'indecision';
        case 3 :
            return 'disapproval';
        case 4 :
            return 'high_disapproval';
    }
    return '';
}

function debateOptionsHandler(container, isModal){
    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    // redirect to the text linked to a debate
    let seeHistoryBtn = container.find('.debate-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        getContributionHistoryModal(getContributionOptionData($(this), "id"), 1).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });

    // redirect to modal for show the debate page
    let showBtn = container.find('.debate-show-btn');
    showBtn.off('click');
    showBtn.on('click', function() {
        redirectToDebateViz(getContributionOptionData($(this), "id"), -1);
    });

    // redirect to modal for editing tag debate
    let editTagBtn = container.find('.debate-tag-edit-btn');
    editTagBtn.off('click');
    editTagBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        openEditTagModal(
            getContributionOptionData($(this), "id"),
            actionFromDragnDrop($(this))
        );
    });

    // redirect to modal for editing tag debate
    let editBtn = container.find('.debate-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        openEditDebateModal(
            getContributionOptionData($(this), "id")
        );
    });

    // redirect to modal for adding a subdebate to the debate
    let addSubdebateBtn = container.find('.debate-add-subdebate-btn');
    addSubdebateBtn.off('click');
    addSubdebateBtn.on('click', function() {
        openEditTagModal(
            null,
            actionFromDragnDrop($(this)),
            getContributionOptionData($(this), "id"),
            null,
            false
        );
    });

    // redirect to modal for adding a category to the debate
    let addCategoryBtn = container.find('.debate-add-category-btn');
    addCategoryBtn.off('click');
    addCategoryBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        let id = getContributionOptionData($(this), "id");
        openEditTagModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            id,
            true
        );
    });

    // redirect to modal for adding an argument to the debate
    let addArgumentBtn = container.find('.debate-add-argument-btn');
    addArgumentBtn.off('click');
    addArgumentBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditArgumentModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            null,
            null,
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // redirect to modal for adding a citation to the debate
    let addCitationBtn = container.find('.debate-add-citation-btn');
    addCitationBtn.off('click');
    addCitationBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;

        openEditCitationModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            null,
            null,
            true,
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // delete the given debate has tab debate link
    let deleteLinkBtn = container.find('.debate-delete-link-btn');
    deleteLinkBtn.off('click');
    deleteLinkBtn.on('click', function() {
        let that = $(this);
        let type = getContributionOptionData(that, "link-type");

        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), (type === -1 ? 12 : type), 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"));
        }, null, "viz.admin.modal.delete.12.");
    });

    // delete the given debate has tab debate link
    let forceDeleteLinkBtn = container.find('.debate-delete-link-force-btn');
    forceDeleteLinkBtn.off('click');
    forceDeleteLinkBtn.on('click', function() {
        let that = $(this);
        let type = getContributionOptionData(that, "link-type");

        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), (type === -1 ? 12 : type), 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"), true);
        }, null, "viz.admin.modal.delete.12.");
    });


}

function initAddTextToDebateListeners(container) {
    // redirect to modal for adding a text to the debate
    container.on('click', '.debate-add-text-btn', function() {
        let debateId = $(this).data('debate');

        getAddTextToDebateModal(debateId).done(function (html) {
            loadAndShowModal($('#modal-anchor'), html);
            let modal = $('#modal-text-selection');
            let textSearchForm = modal.find('#text-search-form');
            let textInput = textSearchForm.find('#text');
            let textIdInput = textSearchForm.find('#textId');
            let textForm = modal.find('#text-form');
            let submitBtn = modal.find('button[name="submit"]');

            addTextTypeahead(textInput);

            manageTextPanel(textForm, true);

            submitBtn.on('click', function() {
                let existingText = textSearchForm.is(':visible');

                addTextToDebate(debateId, existingText ? textIdInput.val() : -1, textForm).done(function (html) {
                    hideAndDestroyModal(modal);
                    triggerReloadVizEvent();
                    slideDefaultSuccessMessage();
                }).fail(function (xhr) {
                    if (!existingText) {
                        let status = xhr.responseText.length === 0 ? -1 : xhr.status;

                        switch (status) {
                            case 400:
                                modal.show();
                                // form has errors => clear form and rebuilt
                                replaceContent(textForm, xhr.responseText, 'form');
                                manageTextPanel(modal, true);
                                fadeMessage();
                                break;

                            case 406:
                                replaceContent(textForm, xhr.responseText, 'form');
                                manageTextPanel(modal, true);
                                break;

                            case 409:
                                modal.hide();
                                // actor names match
                                treatHandleContributionMatchesModal(modal, xhr.responseText, submitBtn);
                                break;

                            default:
                                // any other (probably a crash)
                                hideAndDestroyModal(modal);
                                slideDefaultErrorMessage();
                        }
                    } else {
                        slideDefaultErrorMessage();
                    }
                });
            });

        }).fail(function (jqXHR) {
            stopWaitingModal();
            if (jqXHR.status === 401) {
                redirectToLogin();
            } else {
                console.log("Error with add text to debate modal");
            }
        });

    });

    container.on('click', '.debate-delete-text-link-btn', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 19, 0, true, null, true);
        }, null, "viz.admin.modal.delete.19.");
    });
}