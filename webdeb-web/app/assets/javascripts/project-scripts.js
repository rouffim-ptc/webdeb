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
 * This javascript file contains all reusable functions for projects management
 *
 * @author Martin Rouffiange
 */


/*
 * PROJECT EDIT MANAGEMENT
 */

/**
 * Listeners projects
 */
function initProjectsListener(){
    $(document).on("click", ".edit-project-btn", function(){
        openEditProjectModal($(this).data("id"));
    });

    $(document).on("click", ".edit-projectgroup-btn", function(){
        openEditProjectGroupModal($(this).data("projectid"), $(this).data("id"));
    });

    $(document).on("click", ".edit-projectsubgroup-btn", function(){
        openEditProjectSubgroupModal($(this).data("projectid"), $(this).data("groupid"), $(this).data("id"));
    });

    $(document).on("click", ".see-project-activity", function(){
        openProjectActivityModal($(this).data("id"));
    });

    $(document).on("click", ".generate-project-users", function(){
        let projectId = $(this).data("id");

        generateProjectUsers(projectId).done(function (html) {
            slideDefaultSuccessMessage();
            reloadPage(projectId);
        }).fail(function (jqXHR) {
            slideDefaultErrorMessage(jqXHR.responseText);
        });
    });

    $(document).on("click", ".delete-project-users", function(){
        let projectId = $(this).data("id");

        deleteProjectUsers(projectId).done(function (html) {
            slideDefaultSuccessMessage();
            reloadPage(projectId);
        }).fail(function (jqXHR) {
            slideDefaultErrorMessage(jqXHR.responseText);
        });
    });

    countStats();
}

/**
 * open the modal to see contribution activity for a given project
 *
 * @param projectId the project id
 */
function openProjectActivityModal(projectId) {
    getProjectActivity(projectId).done(function (html) {
        loadAndShowModal( $('#modal-anchor'), html);
        var modal = $("#see-project-activity-modal");
        convertModal(modal);
        initProjectActivityModalListener(modal);
    }).fail(function (jqXHR) {
        console.log("Error with edit project activity modal");
    });
}

/**
 * Init listeners for project activity modal
 *
 * @param modal the project activity modal
 */
function initProjectActivityModalListener(modal){

    modal.find(".project-activity-changetime").on("click", function(){
        modal.find(".project-activity").hide();
        modal.find(".project-activity-" + $(this).data("activity-time")).show();

    });

    modal.find(".project-activity-changepov").on("click", function(){
        modal.find(".project-activity-pov").hide();
        modal.find("#project-activity-pov-" + $(this).data("activity-pov")).show();
    });
}

/**
 * Count statistics about groups, subgroups, members for all projects
 */
function countStats(){
    // count projects contributors stats
    $(".count-project-contributors").each(function(){
        treatCount($(this));
    });

    // count project subgroups stats
    $(".count-projectsubgroup").each(function(){
        treatCount($(this));
    });
}

/**
 * Count statistics about groups, subgroups, members for a project
 */
function treatCount(that){
    var ref = $("."  + that.data("ref"));
    var value = Number(that.text());

    ref.each(function(){
        $(this).text(value + Number($(this).text()));
    });
}

function convertModal(modal){
    // Since confModal is essentially a nested modal it's enforceFocus method
    // must be no-op'd or the following error results
    // "Uncaught RangeError: Maximum call stack size exceeded"
    // But then when the nested modal is hidden we reset modal.enforceFocus
    var enforceModalFocusFn = $.fn.modal.Constructor.prototype.enforceFocus;

    $.fn.modal.Constructor.prototype.enforceFocus = function() {};

    modal.on('hidden', function() {
        $.fn.modal.Constructor.prototype.enforceFocus = enforceModalFocusFn;
    });

    modal.modal({ backdrop : false });
}

/**
 * Listeners edit project
 *
 * @param container
 */
function initEditProjectListener(container){

    // button group to select pedagogic state
    manageExclusiveCheckboxes($('#b-pedagogic').find('input[type="checkbox"]'), $('#pedagogic'), true);

    submitAction(container, "#project-form", doSaveProject);
}

/**
 * Listeners edit project group
 *
 * @param container
 */
function initEditProjectGroupListener(container){
    submitAction(container, "#projectgroup-form", doSaveProjectGroup);
}

/**
 * Listeners edit project subgroup
 *
 * @param container
 */
function initEditProjectSubgroupListener(container){
    submitAction(container, "#projectsubgroup-form", doSaveProjectSubgroup);

    addContributorGroupsTypeahead($("#projectsubgroup-form").find("#contributorGroups"));
    manageAddRmButton($("#projectsubgroup-form").find("#contributorGroups"), ['name'], "entry.delete.confirm.", null, addContributorGroupsTypeahead);
}

function submitAction(container, formSelector, saveFunction){
    container.find("#submit-btn").on("click", function(){
        saveFunction(container.find(formSelector), container);
    });
}

/**
 * open the modal to edit a project
 *
 * @param projectId a project id
 */
function openEditProjectModal(projectId) {
    editProject(projectId).done(function (html) {
        loadAndShowModal( $('#modal-anchor'), html);
        var modal = $("#create-project-modal");
        convertModal(modal);
        initEditProjectListener(modal);
    }).fail(function (jqXHR) {
        slideDefaultErrorMessage(jqXHR.responseText);
        console.log("Error with edit project modal");
    });
}

/**
 * open the modal to edit a project
 *
 * @param projectId the project id that owns the group
 * @param projectGroupId the project group id to edit
 */
function openEditProjectGroupModal(projectId, projectGroupId) {
    editProjectGroup(projectId, projectGroupId).done(function (html) {
        loadAndShowModal( $('#modal-anchor'), html);
        var modal = $("#create-projectgroup-modal");
        convertModal(modal);
        initEditProjectGroupListener(modal);
    }).fail(function (jqXHR) {
        slideDefaultErrorMessage(jqXHR.responseText);
        console.log("Error with edit project modal");
    });
}

/**
 * open the modal to edit a project
 *
 * @param projectId the project id context
 * @param projectGroupId the project group id that owns the subgroup
 * @param projectSubgroupId the project subgroup id to edit
 */
function openEditProjectSubgroupModal(projectId, projectGroupId, projectSubgroupId) {
    editProjectSubgroup(projectId, projectGroupId, projectSubgroupId).done(function (html) {
        loadAndShowModal( $('#modal-anchor'), html);
        var modal = $("#create-projectsubgroup-modal");
        convertModal(modal);
        initEditProjectSubgroupListener(modal);
    }).fail(function (jqXHR) {
        slideDefaultErrorMessage(jqXHR.responseText);
        console.log("Error with edit project modal");
    });
}

function doSaveProject(form, modal){
    let projectId = form.find('#id').val();

    saveProject(form).done(function () {
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        reloadPage(projectId > 0 ? projectId : undefined);
    }).fail(function (xhr) {
        switch (xhr.status) {
            case 400:
                modal.show();
                var selector = "#project-form";
                modal.find(selector).replaceWith($(xhr.responseText).find(selector));
                // form has errors => clear form and rebuilt
                initEditProjectListener(modal);
                fadeMessage();
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

function doSaveProjectGroup(form, modal){
    let projectId = form.find('#projectId').val();

    saveProjectGroup(form).done(function () {
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        reloadPage(projectId);
    }).fail(function (xhr) {
        switch (xhr.status) {
            case 400:
                modal.show();
                var selector = "#projectgroup-form";
                modal.find(selector).replaceWith($(xhr.responseText).find(selector));
                // form has errors => clear form and rebuilt
                initEditProjectGroupListener(modal);
                fadeMessage();
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

function doSaveProjectSubgroup(form, modal){
    let projectId = form.find('#projectId').val();

    saveProjectSubgroup(form).done(function () {
        hideAndDestroyModal(modal);
        slideDefaultSuccessMessage();
        reloadPage(projectId);
    }).fail(function (xhr) {
        switch (xhr.status) {
            case 400:
                modal.show();
                var selector = "#projectsubgroup-form";
                modal.find(selector).replaceWith($(xhr.responseText).find(selector));
                // form has errors => clear form and rebuilt
                initEditProjectSubgroupListener(modal);
                fadeMessage();
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(modal);
                slideDefaultErrorMessage();
        }
    });
}

function reloadPage(projectId){
    if(projectId) {
        manageProject(projectId).done(function (html) {
            var selector = "#project-container";
            $(selector).replaceWith($(html).find(selector));
            countStats();
        }).fail(function (jqXHR) {
            console.log("Error with manage projects page");
        });
    } else {
        document.location.reload();
    }
}