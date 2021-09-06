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
 * This javascript file contains all reusable functions for actor's professions
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */


/**
 * Manage the edit profession modal
 *
 */
function manageEditProfessionModal(){
  $(document).ready(function(){
    let modal = $("#edit-profession");

    addListeners(modal);
    addProfessionTypeahead(modal.find('#professionLink_name').find("input").first());
    addListenerProfessionName(modal);
    manageAddRmButton(modal.find('#professionNames'), [''], '', null, addProfessionTypeahead);
    manageExclusiveCheckboxes(modal.find('#b-displayHierarchy').find('input[type="checkbox"]'), modal.find('#displayHierarchy'), true);
  });

  function addListeners(modal) {
    modal.find('#submit-btn').on('click', function () {
      sendEditProfession(modal.find('#profession-form')).done(function (data) {
        //$('#admin-profession').empty().append(data);
        hideAndDestroyModalAndCallFunction(modal, autoCreateModalRemoved);
        $(document).trigger("modalRemoved");
        //location.reload();
      }).fail(function (jqXHR) {
        if (jqXHR.status === 400) {
          // rebuild form from response text
          replaceContent('#profession-form', jqXHR.responseText, 'form');
          addProfessionTypeahead($('#professionLink').find("input").first());
          addListenerProfessionName(modal);
        } else {
          //$('#admin-professions').empty().append(jqXHR.responseText);
          hideAndDestroyModalAndCallFunction(modal, autoCreateModalRemoved);
        }
      });
    });

    // handle cancel button
    modal.find('[name="cancel"]').on('click', function () {
        var result = cancelFromModal();
        // cancel success (no other possible result) => just call handle response that will close it
        result.done(function (data) {
            handleContributionModalResponse(modal, data, $("#msg-div"), manageEditProfessionModal);
        });
    });
  }

  function addListenerProfessionName(modal){
    modal.find(".profession_name").focus(function () {
      modal.find("#userWarned").val('false');
    });
  }


}
