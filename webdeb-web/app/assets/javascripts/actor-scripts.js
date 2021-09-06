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
 * This javascript file contains all reusable functions for actor contributions' screens
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */


/*
 * ACTOR EDIT MANAGEMENT
 */


/**
 * Manage actor panels as modal dialogs (automatic creations)
 *
 * @param modalContent the content of the actor modal
 * @param msgdiv the msg div panel where the alert messages will be put as fallback
 */
function manageActorModal(modalContent, msgdiv) {
  let modalAnchor = $('#autocreated');
  loadAndShowModal(modalAnchor, modalContent);

  let modal = modalAnchor.find('#modal-actor');
  manageActorPanel(modal, true, true);
}

/**
 * open modal for edit or create an actor
 *
 * param actorId an actor id
 */
function openEditActorModal(actorId) {

  editActor(actorId).done(function (html) {
    loadAndShowModal($('#modal-anchor'), html);
    let modal = $("#modal-actor");
    manageActorPanel(modal, true, true);
  }).fail(function (jqXHR) {
    if (jqXHR.status === 401) {
      redirectToLogin();
    } else {
      console.log("Error with actor edit modal");
    }
  });
}

/**
 * Submit or cancel submission of the actor form via ajax (when form is displayed in a modal frame).
 *
 * Rebuild form if it contains error. Close frame and add the received data if the call succeeded in
 * either in the next modal if any, or in the main page.
 *
 * @param modal the modal frame containing the actor form
 * @param msgdiv the div where the messages (e.g. submission statuses) will be shown
 */
function saveActorFromModal(form, msgdiv) {
  let modal = form.parents('.modal');

    saveFromModal(null, form).done(function (data) {
      handleContributionModalResponse(modal, data, msgdiv, manageActorPanel);
      slideDefaultSuccessMessage();
    }).fail(function (jqXHR) {
      switch (jqXHR.status) {

        case 400:
          // form has errors => clear actor modal and rebuild it with errors
          modal.find('#actor-form').empty().append(jqXHR.responseText);
          manageActorPanel(modal);
          fadeMessage();
          break;

        case 409:
          // we got a modal frame to resolve name matches
          // hide current actor modal
          modal.hide();
          // show new one (name-matches resolution)
          $('#modal-namematches').html(jqXHR.responseText);
          // affiliations or orgaffiliations ?
          var name = ($('#actortype').val() === '0' ? $('#affiliations').find('[id$="affname"]') : $('#orgaffiliations').find('[id$="affname"]'));
          handleContributionNameMatches(name, '[id$="_affid"]', '[id$="_isDisambiguated"]', modal.find('[name="submit"]'), "Actor");
          break;

        default:
          // any other (probably a crash)
          hideAndDestroyModal(modal);
          showErrorMessage(jqXHR);
      }
    });
}

/**
 * Handle response of actor submission from modal frame, may replace modal content with new modal
 * or simply dispose it and show the message to body
 *
 * @param modal the modal frame
 * @param data the data to put in modal frame, if any
 * @param msgdiv the div where the messages (e.g. submission statuses) will be shown
 */
function handleActorModalResponse(modal, data, msgdiv) {
  // get modal data, if any
  var modaldata = $('<div>').append(data).append('</div>');
  var content = modaldata.find('.modal-body');
  if (content.length > 0) {
    var title = modaldata.find('.modal-header');
    // replace header and body content
    $(modal).find('.modal-header').empty().append($(title).children());
    $(modal).find('.modal-body').empty().append($(content).children());
    // re-add content-dependant listeners
    manageActorPanel(modal);
    fadeMessage();
  } else {
    // no more modals, put message on body and hide modal
    hideAndDestroyModalAndCallFunction(modal, autoCreateModalRemoved);
    showMessage(data, msgdiv);
  }
}

/**
 * Manage toggling of actortype
 */
function manageActorType() {
  var boxes = $('#b-actortype').find('input[type="checkbox"]');
  var field = $('#actortype');
  manageExclusiveCheckboxes(boxes, field, true);
  showPersonOrOrg($(field).val());

  // add click event management when using multiple tabs
  boxes.on('click', function () {
    // show div according to type
    showPersonOrOrg($(this).val());
    showMe($('#submit-container'), true, true);
  });
}

/**
 * Toggle shown div for actor panels
 *
 * @param type the actor type to be shown (0 for person, 1 for organization)
 */
function showPersonOrOrg(type) {
  showMe($('.person'), type === '0');
  showMe($('.org'), type === '1');
}

/**
 * Manage the whole actor form panel:
 *
 * # switch button on type
 * # typeahead on actor names with confirmation popup
 * # typeahead on affiliations
 * # add/remove buttons
 * # namesake management
 */
function manageActorPanel(container, firstLoad, fromModal) {
  firstLoad = firstLoad !== undefined ? firstLoad : false;
  fromModal = fromModal !== undefined ? fromModal : false;

  let id = container.find('#id');

  let orgaffiliationsForm = container.find('#orgaffiliationsForm');
  let affiliationsForm = container.find('#affiliationsForm');
  let qualificationsForm = container.find('#qualificationsForm');
  let parentsForm = container.find('#parentsForm');
  
  let spellings = container.find('#spellings');
  let orgspellings = container.find('#orgspellings');
  let oldnames = container.find('#oldnames');
  
  let tags = container.find('#tags');
  let places = container.find('#places');

  let picturePers = container.find('#avatarForm_filename');
  let pictureOrg = container.find('#orgAvatarForm_filename');
  let pictureString = container.find('#avatarString');

  let first = container.find('.person').find('#name_first');
  let last = container.find('.person').find('#name_last');

  if(firstLoad) {
    container.find('form').stepFormJs({
      validationCall : actorStepValidation,
      panelManager : manageActorPanel,
      alwaysDisplaySubmit : !isNaN(id.val()) && id.val() >= 0,
      canStepEnd: 0,
      submissionCall: fromModal ? saveActorFromModal : null,
      toCheckSyncCall : fromModal ? null : treatHandleContributionMatchesAsyncFunction,
      toCheckSyncCallFunc : fromModal ? null : saveFromModal,
      cancelAction : fromModal ? cancelFromModal : null,
      cancelRedirection : fromModal ? null : true
    });

    if(fromModal) {
      // handle cancel button
      container.find('[name="cancel"]').on('click', function () {
        // cancel success (no other possible result) => just call handle response that will close it
        cancelFromModal.done(function (data) {
          handleContributionModalResponse(container, data, null, manageActorPanel);
        });
      });
    }
  } else {

  }

  // handle existing actors if id in form is -1, meaning it is a new actor
  if ($('#id').val() === "-1") {
    manageKnownActor('.person', '#name_first', '0', 'firstname');
    manageKnownActor('.person', '#name_last', '0', 'lastname');
    manageKnownActor('.person', '#name_pseudo', '0', 'pseudo');
    manageKnownActor('.org', '#orgname_last', '1');
  }

  // add autocompletion to affiliations
  addActorsTypeahead(orgaffiliationsForm, 'affname', '-1');
  addActorsTypeahead(affiliationsForm, 'affname', '1');
  addActorsTypeahead(qualificationsForm, 'affname', '1', '1');
  addActorsTypeahead(parentsForm, 'affname', '0');

  // manage + / - buttons on affiliations
  manageActorButton(affiliationsForm, ['affname'], '1');
  manageActorButton(orgaffiliationsForm, ['affname'], '-1');
  manageActorButton(qualificationsForm, ['affname'], '1', '1');
  manageActorButton(parentsForm, ['affname'], '2');

  managePrecisionDate(container);

  // manage + / - buttons on multiple names
  manageActorButton(spellings, [], '0');
  manageActorButton(orgspellings, [], '1');
  manageActorButton(oldnames, [], '1');

  // initialize tag typeahead
  addTagsTypeahead(tags, 3);
  // initialize place typeahead
  addPlacesTypeahead(places);

  manageAddRmButton(tags, [''], '');
  manageAddRmButton(places, [''], '');

  // add content-dependant listeners
  manageActorPanelButtons();

  // split name for persons if possible
  container.find('#actortype-0').on('click', function () {
      if (first.val() === '' && last.val().split(' ').length > 1) {
          first.val(last.val().split(' ')[0]);
          last.val(last.val().split(' ').slice(1).join(' '));
      }
  });

  picturePers.change(function(){
    updatePictureField($(this), pictureString);
  });

  pictureOrg.change(function(){
    updatePictureField($(this), pictureString);
  });
}

function managePrecisionDate(container){
  let targets = container.find(".aff-ongoing");
  /*targets.each( function(){
    if($(this).val() === "5") {
      $(this).parents(".entry").find('input[name$=".endDate"]').val(convertDate(new Date()));
    }
  });*/

  targets.off("change");
  targets.change(function(){
    let input = $(this).parents(".entry").find('input[name$=".endDate"]');
    if($(this).val() === "5"){
      input.val(convertDate(new Date()));
      input.prop("readonly", true);
    }else{
      input.val('');
      input.prop("readonly", false);
    }
  });
}

/**
 * Add all listeners to buttons and checkboxes that are content-dependant, ie, that must be re-added
 * after panel is trashed and rebuilt
 */
function manageActorPanelButtons() {

  // bind event on actorType buttons
  manageActorType();

  // toggle-button for ongoing affiliation
  $('[id$="groupEndDate"]').each(function () {
    var that = this;
    updateRoundedbox(that);
    $(this).find('.input-group-addon.roundedbox').each(function () {
      listenerOnRoundedBox(that, '[name$="ongoingDate"]');
    });
  });

  manageSectorButtons($('#sectordiv'), $('#checkAllSectors'), $("#status-1"));

  // handle help bubbles
  handleHelpBubble('.actordiv');

  // manage exclusive boxes for gender and statuses
  manageExclusiveCheckboxes($('#b-gender').find('input[type="checkbox"]'), $('#gender'));
  manageExclusiveCheckboxes($('#b-statuses').find('input[type="checkbox"]'), $('#legalStatus'));

  // manage picture file input
  //managePictureField('.person', '/avatar/');
  //managePictureField('.org', '/avatar/');

  // send requests to WDTAL for actor name or url
  var waitForIt = $('#wait-for-it');
  // add listeners on names and crossref if type is unknown, otherwise, only crossref
  var names = [$('[name$="crossref"]')];
  if ($('#actortype').val() === "-1") {
    names.push($('[name="name.last"]'));
    names.push($('[name="orgname.last"]'));
  }

  // TODO WDTAL DBPedia service or equivalent
  /*$(names).each(function () {
    $(this).on('focusout', function () {
      searchForActor(waitForIt, this);
    });
    $(this).on('keypress', function (e) {
      if (e.keyCode === 13) {
        searchForActor(waitForIt, this);
        e.stopPropagation();
        e.preventDefault();
      }
    });
  });*/
}

/**
 * Manage sector buttons (ie trigger selection of all sectors)
 *
 * @param sectordiv div containing all sectors
 * @param checkall checkbox to select/unselect all sectors
 * @param checkallTrigger bound checkboxes that trigger the selection of all sectors
 */
function manageSectorButtons(sectordiv, checkall, checkallTrigger) {
  // select / unselect all sectors;
  var sectorInputs = sectordiv.find('input');
  checkall.on('click', function () {
    sectorInputs.prop('checked', $(this).prop('checked'));
  });

  sectorInputs.on('click', function () {
    var checked = sectordiv.find('input:checked').length;
    if ($(this).prop('checked') && checked === sectorInputs.length) {
      // check if all are checked now
      checkall.prop('checked', true);
    } else {
      if (checked === sectorInputs.length - 1) {
        // unchecked only one => uncheck checkAllSectors
        checkall.prop('checked', false);
      }
    }
  });
  // set checkAllSectors (page load)
  checkall.prop('checked', sectordiv.find('input:checked').length === sectorInputs.length);

  // trigger click event on "all sectors" if actor is "political party"
  checkallTrigger.on('change', function () {
    if ($(this).is(':checked') && !checkall.is(':checked')) {
      checkall.trigger('click');
    }
  });
}

function manageActorPositions(container, forDebate) {
  forDebate = forDebate === undefined ? false : forDebate;
  let modal = $('#modal-anchor');

  if(forDebate) {

    placeCursors(50);

    container.find('.actor-position-debate-citations').on('click', function () {
      let that = $(this);
      let subContainer = that.parents('.actor-position-gradient-item').next();

      if(!subContainer.hasClass('loaded')) {
        subContainer.addClass('loaded');

        getSociographyCitationsConcurrent(that.data('debate'), that.data('sub-debate'), that.data('actor'), that.data('key'), that.data('related-actor'), false).done(function (html) {
          subContainer.html(html);
          citationOptionsHandler(subContainer);
          checkNavPills(subContainer);
        }).fail(function () {
          subContainer.hide();
          console.log('Error with positions socio citations');
          slideDefaultErrorMessage();
        });
      }

      if(subContainer.hasClass('show')) {
        subContainer.removeClass('show');
        subContainer.hide();
      } else {
        subContainer.addClass('show');
        subContainer.show();
      }
    });

  } else {

    $(document).on('viz-async-loaded', function () {
      placeCursors();
    });

    $(window).resize(function () {
      placeCursors();
    });

    container.on('click', '.btn-pov', function () {
      placeCursors();
    });

    container.find('#tab_6-tab').on('click', function () {
      placeCursors();
    });

    container.find('#accordion_header_6').on('click', function () {
      placeCursors();
    });

    container.on('click', '.actor-position-details', function () {
      getActorPositionsForSocioValue($(this).data('actor'), $(this).data('key'), $(this).data('value')).done(function (html) {
        loadAndShowModal(modal, html);
        manageActorPositions(modal, true);
      }).fail(function () {
        console.log('Error with positions socio modal');
        slideDefaultErrorMessage();
      });
    });

  }

 function placeCursors(time) {
    time = time === undefined ? 250 : time;

    setTimeout(function() {
     container.find('.position-line:visible').each(function () {
       let left = ($(this).data('distance') * $(this).width()) - 4;
       let cursor = $(this).children().first();

       cursor.css('left', left + 'px');
       cursor.show();
     });
    }, time);
 }
}

function initActorCitations(superContainer, actorId, viewBy) {
  let container = superContainer.find('.actor-citations-' + viewBy);

  citationOptionsHandler(container);

  initActorCitationsScroller(actorId, viewBy,6, container.find('.tags-tab'), findActorCitations);
  initActorCitationsScroller(actorId, viewBy, 2, container.find('.texts-tab'), findActorCitations);
  initActorCitationsScroller(actorId, viewBy, 1, container.find('.debates-tab'), findActorCitations);
  initActorCitationsScroller(actorId, viewBy, 3, container.find('.citations-tab'), findActorCitations);

  if(viewBy === 3)
    initActorCitationsScroller(actorId, viewBy, 0, container.find('.actors-tab'), findActorCitations);

}

function initActorCitationsScroller(actor, role, ctype, container, toCall) {

  container.each(function(){
    let cont = $(this);

    if( cont.find('.results-container').exists()) {
      cont.scrollerPager(
          [actor, role, ctype],
          toCall,
          {
            toExecAfter : initContributionListeners,
            relatedContributionId : actor
          }
      );
    }
  });

}

function actorOptionsHandler(container, isModal){
  container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
  let modalanchor = $('#merge-modal-anchor');

  // redirect to the text linked to a debate
  let seeHistoryBtn = container.find('.actor-see-history-btn');
  seeHistoryBtn.off('click');
  seeHistoryBtn.on('click', function() {
    getContributionHistoryModal(getContributionOptionData($(this), "id"), 0).done(function (html) {
      modalanchor.empty().append(html);
      modalanchor.find('.modal').modal('show');
    });
  });

  // redirect to modal for show the debate page
  let showBtn = container.find('.actor-show-btn');
  showBtn.off('click');
  showBtn.on('click', function() {
    redirectToActorViz(getContributionOptionData($(this), "id"), -1);
  });

  // redirect to modal for editing tag debate
  let editBtn = container.find('.actor-edit-btn');
  editBtn.off('click');
  editBtn.on('click', function() {
    if(isModal)
      hideAndDestroyModal(container);

    openEditActorModal(
        getContributionOptionData($(this), "id")
    );
  });

}

/**
 * Get data, send request to WDTAL DBPedia service and handle response
 *
 * @param waitForIt the wait for it modal panel to be shown while executing the query
 * @param element the element on which the event has been triggered
 */
function searchForActor(waitForIt, element) {
  var value, optional, subdiv;
  var lang = $('#lang').val();
  // check which tab is active
  var type = $('#actortype').val();
  // check if we got an url or a name
  var isUrl = $(element).prop('name').endsWith('crossref');

  if (type === '0') {
    value = isUrl ? $('[name="crossref"]').val().trim() : $('[name="name.last"]').val().trim();
    optional = $('[name="name.first"]').val().trim();
    subdiv = '.person';
  } else {
    value = isUrl ? $('[name="orgcrossref"]').val().trim() : $('[name="orgname.last"]').val().trim();
    subdiv = '.org';
  }

  // we got a name to look for, call WDTAL service
  if (value.trim().length > 0 && $('#previousCall').val() !== (optional + value)) {
    $('#previousCall').val(optional + value);
    waitForIt.modal('show');

    // call WDTAL service
    searchActorDetails(type, isUrl, value, optional).done(function (data) {

      // hide spinner
      hideAndDestroyModal(waitForIt);

      $('#wikiCalled').val(true);
      // reusable counter
      var i;
      // start with image (because it may take some time to retrieve and load data
      if (data.image !== undefined && $(subdiv + ' .avatar-field').val() === '') {
        getPictureFile(data.image).done(function (data) {
          $(subdiv + ' .avatar-field').val(data);
          $(subdiv + ' .file-input-clear').show();
          loadExistingPicture(subdiv);
        });
      }

      // given_name and family_name or organization name (dict iso-code:name)
      if (data.family_name !== undefined || data.org_name !== undefined) {
        var prefix = (type === '0' ? '' : 'org');
        var name = (data.family_name !== undefined ? data.family_name : data.org_name);
        // manual counter
        i = 0;
        for (var lg in name) {
          // current name is in current lang
          if (lg === lang) {
            if (type === '0') {
              $('[name="name.first"]').typeahead('val', data.given_name[lg]);
            }
            $('[name="' + prefix + 'name.last"]').typeahead('val', name[lg]);

          } else {
            if ($('[name="all' + prefix + 'names[' + i + '].lang"]').length === 0) {
              addActorField($('#' + prefix + 'spellings'), [], type);
            }
            // add name and language
            if (type === '0') {
              $('[name="allnames[' + i + '].first"]').val(data.given_name[lg]);
            }
            $('[name="all' + prefix + 'names[' + i + '].lang"]').val(lg);
            $('[name="all' + prefix + 'names[' + i + '].last"]').val(name[lg]);
            // increment name counter to know which line in names must be filled next
            i++;
          }
        }
      }

      // wiki url
      if ($('[name="' + (type === '0' ? 'crossref' : 'orgcrossref') + '"]').val() === '' && data.wiki_url !== undefined) {
        if (data.wiki_url[lang] !== undefined) {
          $('[name="' + (type === '0' ? 'crossref' : 'orgcrossref') + '"]').val(data.wiki_url[lang]);
        } else {
          $('[name="' + (type === '0' ? 'crossref' : 'orgcrossref') + '"]').val(data.wiki_url[Object.keys(data.wiki_url)[0]]);
        }
      }

      // type-specific fields
      if (type === '0') {
        // PERSON
        // dob (YYYY-MM-DD), dod (YYYY-MM-DD), gender (char id), nationality (array of iso code),
        if (data.dob !== undefined) {
          $('#birthdate').val(formatDate(data.dob));
        }
        if (data.dod !== undefined) {
          $('#deathdate').val(formatDate(data.dod));
        }
        if (data.gender !== undefined) {
          $('#gender-' + data.gender).prop('checked', true);
          $('#gender').val(data.gender);
        }

        if (data.nationality !== undefined && data.nationality.length > 0) {
          $('#residence').val(data.nationality[0].toLowerCase());
        }

        // handle affiliations
        if (data.affiliations !== undefined) {
          fillAffiliations(data, 'affiliations', lang, '1');
        }

      } else {
        // ORGANIZATION
        // legal_status, country (list), sector (list)
        if (data.legal_status !== undefined) {
          $('#status-' + data.legal_status).prop('checked', true);
          $('#legalStatus').val($('#status-' + data.legal_status).val());
        }

        if (data.country !== undefined && data.country.length > 0) {
          $('#headOffice').val(data.country[0].toLowerCase());
        }
        if (data.sectors !== undefined) {
          for (i = 0; i < data.sectors.length; i++) {
            $('#allSectors_' + data.sectors[i]).prop('checked', true);
          }
        }

        if (data.start_date !== undefined) {
          $('#creationDate').val(formatDate(data.start_date));
        }
        if (data.end_date !== undefined) {
          $('#terminationDate').val(formatDate(data.end_date));
        }

        // owner affiliation
        if (data.owner !== undefined) {
          $('#orgaffiliations_0_affname').typeahead('val', (data.owner[lang] !== undefined ? data.owner[lang] : data.owner['en']));
          if (data.start_date !== undefined) {
            $('#orgaffiliations_0_startDate').val(formatDate(data.start_date));
          }
          // save stringified full affiliations as received from wikidata (for i18n handling)
          $('#orgaffiliations_0_fetched').val(JSON.stringify(data.owner));
        }

        // handle affiliations (not present atm)
        if (data.affiliations !== undefined) {
          fillAffiliations(data, 'orgaffiliations', lang, '-1');
        }
      }

    }).fail(function (jqXHR) {
      // handle error -> display message passed in response
      hideAndDestroyModal(waitForIt);
      $('#msg-div').append(jqXHR.responseText);
      fadeMessage();
    });
  }
}

/**
 * Fill in affiliation data
 *
 * @param data the data to fill in
 * @param prefix the html element for the affiliations div
 * @param lang current UI language iso-639-1 code
 * @param actortype the actortype id to add the typeahead on (for call to addActorField if necessary)
 */
function fillAffiliations(data, prefix, lang, actortype ) {
  var field = '#' + prefix + '_';
  // check amount of affiliations line in form
  var shift = $('#' + prefix).children('div').length;

  // if only one, must ensure there are data in it, either affname or function must be filled
  if (shift === 1 && $(field + '0_affname').val() === ''
      && ($(field + '0_function').length === 0 || $(field + '0_function').val() === '')) {
    // no data -> we may simply reuse it
    shift = 0;
  } else {
    // must create a new line to avoid rewritting data over existing data
    addActorField($('#' + prefix), ['affname'], actortype);
  }

  var current;
  var fieldId;
  for (var i = 0; i < data.affiliations.length; i++) {
    current = data.affiliations[i];
    fieldId = Number(shift) + Number(i);
    if (current.function !== undefined) {
      $(field + fieldId + '_function').typeahead('val', (current.function[lang] !== undefined ?
          current.function[lang] : current.function['en']));
    }

    $(field + fieldId + '_affname').typeahead('val', (current.organization[lang] !== undefined ?
        current.organization[lang] : current.organization['en']));
    if (current.start_date !== undefined) {
      $(field + fieldId + '_startDate').val(formatDate(current.start_date));
    }
    if (current.end_date !== undefined) {
      $(field + fieldId + '_endDate').val(formatDate(current.end_date));
    }
    // save stringified full affiliation as received from wikidata (for i18n handling)
    $(field + fieldId + '_fetched').val(JSON.stringify(current));
    $(field + fieldId + '_lang').val((current.organization[lang] !== undefined ? lang : 'en'));

    // do we need an extra line
    if (i < data.affiliations.length - 1) {
      addActorField($('#' + prefix), ['affname'], actortype);
    }
  }
}

/**
 * Manage known entries (actor), i.e. add a typeahead on the actor name and fill in the form if users hit
 * a known actor and wants to update it
 *
 * @param div the div containing this element
 * @param element the html element id (jquery) where the typeahead must be placed
 * @param actortype the type of actor (to build auto-completion suggestions)
 * @param key the typeahead key to display
 */
function manageKnownActor(div, element, actortype, key) {
  addActorTypeahead(element, actortype, key);
  $(div).off('typeahead:selected');
  $(div).on('typeahead:selected', element, function (e, datum) {
    // create modal popup
    bootbox.dialog({
      message: Messages("entry.actor.existing.text"),
      title: Messages("entry.actor.existing.title"),
      buttons: {
        main: {
          label: Messages("entry.actor.existing.ok"),
          className: "btn-primary",
          callback: function () {
            redirectToEdit(datum.id);
          }
        },
        create: {
          // do nothing if user chose not to load existing actor
          label: Messages("entry.actor.existing.nok"),
          className: "btn-link",
          callback: function () {
            $('#isDisambiguated').val(true);
          }
        }
      }
    })
  });
}

/**
 * Display a modal dialog to handle possible matches for an actor's name and choose between add a new (namesake) or
 * editing an exisitng actor.
 */
function handleActorNameMatches() {
  var modal = $('#namesake-modal');
  var okbtn = "#createnew";
  var loadbtn = '#load';

  $(loadbtn).prop('disabled', true);
  toggleSummaryBoxes('#namesake-boxes', '.toggleable');

  modal.modal('show');
  // handle click on create a "new actor" (namesake) button
  modal.on('click', okbtn, function () {
    // set isNamesake to true and submit form
    $('#isDisambiguated').val('true');
    hideAndDestroyModal(modal);
    var submitBtn = $('#submit');
    submitBtn.off();
    submitBtn.trigger('click');
  });

  // handle click on modify an "existing actor" button
  modal.on('click', loadbtn, function () {
    // get selected element and fill form with this wrapper
    var id = modal.find('.selected').prop('id').split("_")[1];
    if (id == null) {
      // no selected element, display error message
      showMe(modal.find('#msg-div'), true, true);
    } else {
      // fill form with selected element (depends on what is the actual view)
      $('#isDisambiguated').val('true');
      hideAndDestroyModal(modal);
      redirectToEdit(id);
    }
  });
}