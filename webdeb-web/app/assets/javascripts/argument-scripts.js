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
 * This collection of functions are meant to handle argument-related javascript functions, mainly dedicated to
 * manage insertion of arguments from either texts annotation or tweet validation workflow.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

/**
 * Add all needed listeners to panels
 *
 * @param content main div containing the form
 */
function initArgumentListeners(content) {
    addArgumentTypeahead(content.find('#title'), 1);
    // tooltip bubble management
    handleHelpBubble(content);
}

function initArgumentModalListeners(argId, anchorId, modal, reloadDragnDrop){
    initArgumentListeners(modal);

    // handle submit button
    modal.find('[name="submit"]').on('click', function () {
        doSaveArgument(argId, anchorId, modal.find('#argument-form'), modal, reloadDragnDrop)
    });
}

/**
 * open modal for edit or create an argument
 *
 * @param argId the argument id to edit (-1 for new one)
 * @param contextId the context where add the argument
 * @param subContextId a tag sub context id
 * @param categoryId if the new argument is in a specific category
 * @param superArgumentId if the new argument is a sub argument of a specific super argument
 * @param shadeId the shade of the potential link
 * @param citationId a citation justification link id
 */
function openEditArgumentModal(argId, reloadDragnDrop, contextId, subContextId, categoryId, superArgumentId, shadeId, citationId) {
    argId = argId != null ? argId : -1;
    contextId = contextId != null ? contextId : -1;
    subContextId = subContextId != null ? subContextId : -1;
    categoryId = categoryId != null ? categoryId : -1;
    superArgumentId = superArgumentId != null ? superArgumentId : -1;
    shadeId = shadeId != null ? shadeId : -1;

    (citationId ? newArgumentFromCitation(citationId) : Number.isInteger(argId) && argId >= 0 ? editArgument(argId, contextId) : newArgument(contextId, subContextId, categoryId, superArgumentId, shadeId)).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);
        let modal = $("#modal-argument");
        initArgumentModalListeners(argId, contextId, modal, reloadDragnDrop);
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with argument edit modal");
        }
    });
}

function doSaveArgument(argId, anchorId, form, modal, reloadDragnDrop){
    saveArgument(argId, anchorId, form).done(function () {
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        triggerReloadVizEvent('.debate-' + anchorId, reloadDragnDrop);
    }).fail(function (xhr) {
        let status = xhr.responseText.length === 0 && xhr.status == 400 ? -1 : xhr.status;

        switch (status) {
            case 400:
                modal.show();
                // form has errors => clear form and rebuilt
                replaceContent(form, xhr.responseText);
                initArgumentModalListeners(argId, anchorId, modal, reloadDragnDrop);
                fadeMessage();
                break;

            case 409:
                hideAndDestroyModal(modal);
                slideMessage($('#warning-link-exists'));
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

/**
 * Load data in a select group
 *
 * @param element the select html element (id)
 * @param data (json) data to put as options
 */
function loadSelectData(element, data) {
  let placeholder = element + " .blank";
  let optionsValues = '<option class="blank" value="-1">' + $(placeholder).text() + '</option>';
  if (data != null) {
    $.each(data, function (id, e) {
      optionsValues += '<option value="' + e.id + '">' + getDescription(e) + '</option>';
    });
  }
  $(element).empty().append(optionsValues);
}

/**
 * Extract the description part of the given json element (type specific). Used for ajax-based filling of dropdown boxes
 * Kind of polymorphic dispatcher
 *
 * @param element a json element
 * @returns {String} 'description' of the given element (depending on the actual type of the json object) or '' if unfound
 */
function getDescription(element) {
  if (element.hasOwnProperty('shade')) {
    return element.shade;
  }
  if (element.hasOwnProperty('origin')) {
    return element.origin;
  }
  return '';
}

/**
 * Verify if the title of the argument is valid or not
 *
 * @param type the type of the argument
 * @param lang a two char iso-639-1 code that represents the language of the argument
 */
function verifyArgument(type, lang){
  let standardInput = $("#standardPart");
  let output = $("#standardPart_warning");
  let submitBtn = $("#submit-form");

  standardInput.on("focusin", function() {
    standardInput.attr("data-warned", true);
  });

  submitBtn.on("click", function(e) {
    let warned = standardInput.attr("data-warned");

    if (warned !== undefined && warned === "true") {
      e.preventDefault();
      output.empty();
      verifyStandardForm(standardInput.val(), type, lang).done(function (data) {
        if (data[0]) {
          if (data[2]) {
            standardInput.parent().addClass("has-warning");
            output.append('<div class="div-warning"> - ' + Messages("argument.standard.part.warning.words") + '</div>');
          }
          standardInput.attr("data-warned", false);
        }else{
          submitBtn.closest("form").submit();
        }
      }).fail(function(){
        submitBtn.closest("form").submit();
      });
    }
  });
}

function createArgumentOptions(argId, contextId, debateId, isFirstArgument){
    return createContributionOptions(createArgumentVizOptions(argId, debateId, isFirstArgument),
        createArgumentEditOptions(argId, contextId, isFirstArgument), createArgumentAddOptions(argId, contextId));
}

function createArgumentVizOptions(argId, debateId, isFirstArgument){
    let ul = '<ul class="dropdown-menu dropdown-menu-right" data-id="' + argId + '" data-debate-id="' + debateId + '">';
    if(isFirstArgument){
        ul += createContributionOptionContextMenuBtn("argument-see-debate-btn", "argument.options.viz.debate.see", "fa-comment", "");
    }
    ul += createContributionOptionContextMenuBtn("argument-see-citations-btn", "argument.options.viz.citation.see", "fa-comment-alt", "");
    ul += createContributionOptionContextMenuBtn("argument-see-history-btn", "viz.admin.history.label", "fa-history", "");
    ul += createContributionOptionContextMenuBtn("argument-show-details-btn", "viz.admin.details.label", "fa-info-circle", "");
    ul += '</ul>';
    return ul;
}

function createArgumentEditOptions(argId, contextId, isFirstArgument){
    let ul = '<ul class="dropdown-menu dropdown-menu-right" data-id="' + argId + '" data-context-id="' + contextId + '">';
    ul += createContributionOptionContextMenuBtn("argument-edit-arg-btn", "argument.edit.title", "fa-edit", "viz.admin.update.tooltip");
    if(!isFirstArgument){
        ul += createContributionOptionContextMenuBtn("argument-create-debate-btn", "argument.options.edit.debate.create", "fa-plus", "");
    }
    //ul += createContributionOptionContextMenuBtn("argument-delete-link-btn", "general.label.btn.clearlink", "fa-trash-alt", "viz.admin.update.tooltip");
    ul += createContributionOptionContextMenuBtn("argument-delete-arg-btn", "general.label.btn.clear", "fa-trash-alt", "");
    ul += '</ul>';
    return ul;
}

function createArgumentAddOptions(argId, contextId){
    let ul = '<ul class="dropdown-menu dropdown-menu-right" data-id="' + argId + '" data-context-id="' + contextId + '">';
    ul += createContributionOptionContextMenuBtn("argument-add-argument-btn", "argument.options.edit.argument.add", "fa-plus-square", "");
    ul += createContributionOptionContextMenuBtn("argument-add-citation-btn", "argument.options.edit.citation.add", "fa-plus-square", "");
    ul += '</ul>';
    return ul;
}

function argumentOptionsHandler(container, isModal){
    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    // display the contribution history of the selected argument
    let seeHistoryBtn = container.find('.argument-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        getContributionHistoryModal(getContributionOptionData($(this), "id"), 4).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });

    // add new argument modal
    let addArgumentBtn = container.find('.argument-add-argument-btn');
    addArgumentBtn.off('click');
    addArgumentBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditArgumentModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            -1,
            getContributionOptionData($(this), "id"),
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // add new citation argument modal
    let addCitationBtn = container.find('.argument-add-citation-btn');
    addCitationBtn.off('click');
    addCitationBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditCitationModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            data != null ? data.category : getContributionOptionData($(this), "category"),
            getContributionOptionData($(this), "id"),
            true,
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // open edit argument modal
    let editBtn = container.find('.argument-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        openEditArgumentModal(
            getContributionOptionData($(this), "id"),
            actionFromDragnDrop($(this)),
            getContributionOptionData($(this), "context"));
    });

    // go to argument debate
    let argumentDebateBtn = container.find('.argument-debate-redirect');
    argumentDebateBtn.off('click');
    argumentDebateBtn.on('click', function() {
        redirectToDebateViz(getContributionOptionData($(this), "id"));
    });

    // open edit argument modal
    let loadCitationsBtn = container.find('.argument-citations-btn');
    loadCitationsBtn.off('click');
    loadCitationsBtn.on('click', function() {
        let that = $(this);
        let collapse = that.siblings('.contribution-container-content');
        let content = collapse.children('.argument-citations-container')

        if(collapse.hasClass('show') && (getContributionOptionData(that, "citations") <= 0 || content.children().length > 0)) {
            collapse.removeClass('show');
        } else {
            collapse.addClass('show');
        }

        if(getContributionOptionData(that, "citations") > 0 && content.children().length === 0) {

            content.html(getWaitForIt());

            getArgumentCitationLinks(
                getContributionOptionData($(this), "context"),
                getContributionOptionData($(this), "sub-context"),
                getContributionOptionData($(this), "category"),
                getContributionOptionData($(this), "id"),
                getContributionOptionData($(this), "shade")
            ).done(function (html) {
                content.html(html);
                citationOptionsHandler(container);
            }).fail(function () {
                console.log("Get argument citations failed...");
            });
        }
    });

    // change argument justification link shade
    let changeShadeBtn = container.find('.argument-change-shade-btn');
    changeShadeBtn.off('click');
    changeShadeBtn.on('click', function() {
        let that = $(this);
        let id = getContributionOptionData($(this), "link");

        changeJustificationShade(id).done(function(){
            triggerReloadVizEvent('.debate-' + id, actionFromDragnDrop(that));
            slideDefaultSuccessMessage();

        }).fail(function(jqXHR){
            if (jqXHR.status === 401) {
                redirectToLogin();
            } else {
                slideDefaultErrorMessage();
            }
        });
    });

    // open show places modal
    let placeVizBtn = container.find('.btn-place-viz');
    placeVizBtn.off('click');
    placeVizBtn.on('click', '.btn-place-viz', function() {
        openPlacesModal(getContributionOptionData($(this), "id"), $(this).data("place-id"));
    });

    // delete the given justification link
    let deleteLinkBtn = container.find('.argument-delete-link-btn');
    deleteLinkBtn.off('click');
    deleteLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 8, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"));
        }, null, "viz.admin.modal.delete.8.");
    });

    // delete the given justification link
    let forceDeleteLinkBtn = container.find('.argument-delete-force-link-btn');
    forceDeleteLinkBtn.off('click');
    forceDeleteLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 8, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"), true);
        }, null, "viz.admin.modal.delete.8.");
    });

    // redirect to modal for adding a citation to the category
    let addDebateInContributionBtn = container.find('.argument-add-debate-btn');
    addDebateInContributionBtn.off('click');
    addDebateInContributionBtn.on('click', function() {
        openEditDebateModal(
            -1,
            getContributionOptionData($(this), "link")
        );
    });

    // delete the given argument
    /*let seeHistoryBtn = container.find('.argument-see-history-btn');
    seeHistoryBtn.off('click');
    container.on('click', '.argument-delete-btn', function() {
        let that = $(this);
        showConfirmationPopup(function(){doDeleteContributionAsync(getContributionOptionData(that, "id"), 4, 0, true)}, null, "viz.admin.modal.delete.3.");
    });*/

    // show details
    let showDetailsBtn = container.find('.argument-show-details-btn');
    showDetailsBtn.off('click');
    showDetailsBtn.on('click', '.argument-show-details-btn', function() {
        redirectToDetailsPage(getContributionOptionData($(this), "id"));
    });
}
