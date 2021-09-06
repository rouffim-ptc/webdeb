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
 * This javascript file contains all reusable functions for tag contributions' screens
 *
 * @author Martin Rouffiange
 */


/*
 * TAG EDIT MANAGEMENT
 */

/**
 * Initialize listener on tag form fieldset
 */
function manageTagPanel(container) {

  let currentTagId = container.find('#id').val();
  let tagType = container.find('#tagType').val();

  addTagTypeahead(container.find('#categoryName'), tagType, false);
  addTagsTypeahead(container.find('#tagNames'), tagType, false);
  addTagsTypeahead(container.find('#tagRewordingNames'), tagType, false);

  // Manage add / remove buttons for names
  manageAddRmButton(container.find('#tagNames'), ['name'], "tag.delete.confirm.", null, addTagTypeahead, tagType);
  manageAddRmButton(container.find('#tagRewordingNames'), ['name'], "tag.delete.confirm.", null, addTagTypeahead, tagType);

  // Manage add / remove buttons and typeahead for parents and children
  addTagsTypeahead(container.find('#parents'), tagType, false);
  addTagsTypeahead(container.find('#children'), tagType, false);
  manageAddRmButton(container.find('#parents'), ['name'], "tag.delete.confirm.", null, addTagTypeahead, tagType);
  manageAddRmButton(container.find('#children'), ['name'], "tag.delete.confirm.", null, addTagTypeahead, tagType);
}

/**
 * open modal for edit or create a tag
 *
 * @param tagId the argument id to edit (-1 for new one, new one is tag category)
 * @param contextId the context where add the tag category (if new one)
 * @param anchorId the html id to anchor (if new one)
 * @param asCategory if the tag must be added as category, false if it is a sub debate of a multiple debate
 * @param fromContribution a contribution id from where we add the citation
 * @param asParent true if the fromContribution is a tag, and that tag must be added as parent
 */
function openEditTagModal(tagId, reloadDragnDrop, contextId, anchorId, asCategory, fromContribution, asParent) {
    contextId = contextId != null ? contextId : -1;

    let edit = tagId != null && !isNaN(tagId);

    (edit ? editTag(tagId, contextId, fromContribution, asParent) : newTagCategory(contextId, asCategory)).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);

        let modal = $("#modal-tag");
        manageTagPanel(modal);
        manageModalTagSubmit(modal, anchorId, reloadDragnDrop);
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with tag edit modal");
        }
    });
}

/**
 * Submit or cancel submission of the tag form via ajax (when form is displayed in a modal frame).
 *
 * Rebuild form if it contains error. Close frame and add the received data if the call succeeded in
 * either in the next modal if any, or in the main page.
 *
 * @param modal the modal frame containing the tag form
 * @param id the debate id
 */
function manageModalTagSubmit(modal, id, reloadDragnDrop) {
  // handle submit button
  modal.find('[name="submit"]').on('click', function () {
    let form = modal.find('form');

    saveTagFromModal(modal.find('form')).done(function (data) {
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        triggerReloadVizEvent('.debate' + (id == null ? '' : '-' + id), reloadDragnDrop);
    }).fail(function (jqXHR) {
      switch (jqXHR.status) {
        case 400:
          // form has errors => clear actor modal and rebuild it with errors
          modal.find('#tag-form').empty().append(jqXHR.responseText);
          manageTagPanel(modal);
          fadeMessage();
          break;
        default:
          // any other (probably a crash)
          hideAndDestroyModal(modal);
          slideDefaultErrorMessage();
      }
    });
  });
}

function initTagContributions(container, tagId, cType) {
    let cTypeName = cType === 1 ? "debates" : "citations";

    if(cType === 1){
        debateOptionsHandler(container);
    }
    let tagParentsContainer = container.find('.tab-' + cTypeName + '-parents');
    let tagChildrenContainer = container.find('.tab-' + cTypeName + '-children');

    tagOptionsHandler(tagParentsContainer);
    tagOptionsHandler(tagChildrenContainer);

    initContributionsScroller([tagId, null, cType, null, null], container.find('.tab-' + cTypeName), findContributions, {toExecAfter : cType === 1 ? debateOptionsHandler : citationOptionsHandler});
    initContributionsScroller([tagId, true, cType], tagParentsContainer, findHierarchyFromTag, {toExecAfter : tagOptionsHandler});
    initContributionsScroller([tagId, false, cType], tagChildrenContainer, findHierarchyFromTag, {toExecAfter : tagOptionsHandler});

}

function tagCategoryOptionsHandler(container, isModal){
    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    // redirect to the text linked to a debate
    let seeHistoryBtn = container.find('.category-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        getContributionHistoryModal(getContributionOptionData($(this), "id"), 6).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });

    // redirect to modal for editing a category
    let editBtn = container.find('.category-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditTagModal(
            getContributionOptionData($(this), "id"),
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context")
        );
    });

    // redirect to modal for adding an argument to the category
    let addArgumentBtn = container.find('.category-add-argument-btn');
    addArgumentBtn.off('click');
    addArgumentBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditArgumentModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            getContributionOptionData($(this), "id"),
            null,
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // redirect to modal for adding a citation to the category
    let addCitationBtn = container.find('.category-add-citation-btn');
    addCitationBtn.off('click');
    addCitationBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditCitationModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "context"),
            data != null ? data.sub_context : getContributionOptionData($(this), "sub-context"),
            getContributionOptionData($(this), "id"),
            null,
            true,
            data != null ? data.shade : getContributionOptionData($(this), "shade")
        );
    });

    // delete the given context has category link
    let deleteLinkBtn = container.find('.category-delete-link-btn');
    deleteLinkBtn.off('click');
    deleteLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 16, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"), false);
        }, null, "viz.admin.modal.delete.16.");
    });

    // delete the given context has category link
    let forceDeleteLinkBtn = container.find('.category-delete-force-link-btn');
    forceDeleteLinkBtn.off('click');
    forceDeleteLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 16, 0, true, actionFromDragnDrop(that) ? true : getContributionOptionData(that, "context"), true);
        }, null, "viz.admin.modal.delete.16.");
    });

}

function tagOptionsHandler(container, isModal, multiple){
    multiple = multiple === undefined ? false : multiple;
    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    if(!multiple) {
        contributionOptionsHandler(container, isModal);
    }

    // redirect to the text linked to a debate
    let seeHistoryBtn = container.find('.tag-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        getContributionHistoryModal(getContributionOptionData($(this), "id"), 6).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });

    // redirect to modal for editing a category
    let editBtn = container.find('.tag-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        openEditTagModal(
            getContributionOptionData($(this), "id"),
            actionFromDragnDrop($(this))
        );
    });

    // redirect to modal for show the debate page
    let showBtn = container.find('.tag-show-btn');
    showBtn.off('click');
    showBtn.on('click', function() {
        redirectToTagViz(getContributionOptionData($(this), "id"), -1);
    });

    // redirect to modal for adding a citation to the category
    let addTagInContributionBtn = container.find('.contribution-add-tag-btn');
    addTagInContributionBtn.off('click');
    addTagInContributionBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditTagModal(
            -1,
            actionFromDragnDrop($(this)),
            null, null, null,
            data != null ? data.category ? data.category : data.context : getContributionOptionData($(this), "id"),
            getContributionOptionData($(this), "parent") === true
        );
    });

    // delete the given context has category link
    let deleteTagLinkBtn = container.find('.tag-delete-link-btn');
    deleteTagLinkBtn.off('click');
    deleteTagLinkBtn.on('click', function() {
        let that = $(this);
        showConfirmationPopup(function(){
            doDeleteContributionAsync(getContributionOptionData(that, "link"), 13, 0, true);
        }, null, "viz.admin.modal.delete.13.");
    });

}