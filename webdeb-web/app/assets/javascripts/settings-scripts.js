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
 * This collection of functions are meant to handle user settings-related javascript functions (profile,
 * group admin and other platform administration functions).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

function addContributorsTypeahead(elements){
	// add typeahead to all children elements containing 'child' name
	elements.find(".entry").each(function () {
		// add typeahead to that element
		addContributorTypeahead($(this));
	});
}

/**
 * Typeahead for contributors
 *
 * @param element an element to search for
 */
function addContributorTypeahead(element) {
	var ctimeout;
	element = element.is("input") ? element : element.find(':input[type=text]');
	avoidSubmitOnTypeahead(element);
	element.typeahead({
		hint: false,
		highlight: true,
		autoselect: false,
		minLength: 1,
		limit: MAX_TYPEAHEAD
	}, {
		displayKey: 'email',
		showAutocompleteOnFocus: true,
		source: function (query, process) {
			if (ctimeout) {
				clearTimeout(ctimeout);
			}
			ctimeout = setTimeout(function () {
				return searchContributors(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
					return process($.makeArray(data));
				});
			}, 300);
		},
		templates: {
			suggestion: function (item) {
        var affiliations = [];
        for (var i = 0; i < item.affiliations.length; i++) {
          affiliations.push(item.affiliations[i].fullfunction);
        }
				return item.fullnameAndEmail + ' <br><i class="text-muted smaller-font">' + affiliations.join() + '</i>';
			}
		}
	});
	element.on('typeahead:selected', function (obj, datum) {
		var memberid = $(this).closest('.input-field').find('[name$="id"]');
		if (memberid !== undefined) {
			$(memberid).val(datum['id']);
		}
		
		$(this).val(datum['fullnameAndEmail']);
		$(this).parents('.entry').first().find('[name$="userId"]').val(datum['id']);
	});
}

/**
 * Typeahead for professions
 *
 * @param element an element to search for
 */
function addProfessionTypeahead(element, idToIgnore) {
	var ctimeout;
	avoidSubmitOnTypeahead(element);
	$(element).typeahead({
		hint: false,
		highlight: true,
		autoselect: false,
		minLength: 1,
		limit: MAX_TYPEAHEAD
	}, {
		displayKey: 'name',
		showAutocompleteOnFocus: true,
		source: function (query, process) {
			if (ctimeout) {
				clearTimeout(ctimeout);
			}
			ctimeout = setTimeout(function () {
				return searchProfessions(query, idToIgnore, true, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
					return process($.makeArray(data));
				});
			}, 300);
		},
		templates: {
			suggestion: function (item) {
				return item.name;
			}
		}
	});

	$(element).on('typeahead:selected', function (obj, datum) {
		$(this).typeahead('val', datum['name'].trim());
		let functionid = $(this).parents('.input-field').first().find('[name$="id"]');

		if (functionid !== undefined) {
			$(functionid).val(datum['id']);
		}
	});
}

/**
 * Get all groups and add auto-completion to given element
 *
 * @param element an html element
 */
function addGroupTypeahead(element) {
	var gtimeout;
	avoidSubmitOnTypeahead(element);
	$(element).typeahead({
		hint: false,
		highlight: true,
		autoselect: false,
		minLength: 1,
		limit: MAX_TYPEAHEAD
	}, {
		displayKey: 'name',
		showAutocompleteOnFocus: true,
		source: function (query, process) {
			if (gtimeout) {
				clearTimeout(gtimeout);
			}
			gtimeout = setTimeout(function () {
				return searchGroups(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
					return process($.makeArray(data));
				});
			}, 300);
		},
		templates: {
			suggestion: function (item) {
				var tips = item.name;
				if (item.description != null) {
					tips += '&nbsp&nbsp <i class="text-muted">' + item.description + '<i>'
				}
				return tips;
			}
		}
	});
}

/*
 * SUBSCRIPTIONS
 */

/**
 * Manage "unsubscribe from group" requests
 *
 * param btn the btn clicked by the user
 */
function doUnsubscribe(btn) {
	leaveGroup(btn.prop('id').split('_')[1]).done(function (data) {
        $('#manage-groups').empty().append(data);
    	addManageGroupListeners() ;
	}).fail(function (jqXHR) {
    if (jqXHR.status === 400) {
      showErrorMessage(jqXHR);
    } else {
      // full rebuild
      replaceContent('body', jqXHR.responseText, 'body');
    }
	});
}

/**
 * Manage "change default group" requests
 *
 * param btn the btn clicked by the user
 */
function doChangeDefault(btn) {
	changeDefaultGroup(btn.prop('id').split('_')[1]).done(function (data) {
		$('#manage-groups').empty().append(data);
    addManageGroupListeners();

	}).fail(function (jqXHR) {
    if (jqXHR.status === 400) {
      showErrorMessage(jqXHR);
    } else {
      // full rebuild
      replaceContent('body', jqXHR.responseText, 'body');
    }
	});
}

/**
 * Add all listeners to "manage professions" tab
 */
function addManageProfessionListeners() {
	var modalanchor = $("#modal-anchor");
	// edit profession names and like
	$('[id^="edit-profession_"]').on('click', function () {
		getModalFromFunction(editProfession, modalanchor, $(this).prop('id').split("_")[1]);
	});

	// edit professions links
	$('[id^="edit-profession-haslink_"]').on('click', function () {
		getModalFromFunction(editProfessionHasLink, modalanchor, $(this).prop('id').split("_")[1]);
	});

	// merge professions
	$('[id^="merge-professions_"]').on('click', function () {
		getModalFromFunction(mergeProfessions, modalanchor, $(this).prop('id').split("_")[1]);
	});
}

/*
 * MANAGE GROUPS
 */

/**
 * Add all listeners to "manage groups" tab
 */
function addManageGroupListeners() {
	var modalanchor = $("#modal-anchor");
	var contributor = $("#contributor");
	var timeout;

	// add new group
	$('[id^="create-group"]').on('click', function () {
    	getModalFromFunction(editGroup, modalanchor, -1);
	});

	$('[id^="open-see-members_"]').on('click', function () {
		changeMembersRole($(this).prop('id').split('_')[1]).done(function (modal) {
			loadAndShowModal(modalanchor, modal);
		});
	});

	// handle requests to edit existing group
	$('[id^="edit-group_"]').on('click', function () {
    	getModalFromFunction(editGroup, modalanchor, $(this).prop('id').split("_")[1]);
	});

	// handle add member modal
	$('[id^="open-add-member_"]').on('click', function () {
    	getModalFromFunction(inviteInGroup, modalanchor, $(this).prop('id').split('_')[1]);
	});

	// handle send mail modal
	$('[id^="open-send-mail_"]').on('click', function () {
		getModalFromFunction(getMailToGroupModal, modalanchor, $(this).prop('id').split('_')[1]);
	});

	// handle confirmation of empty and close group
	$('[id^="empty-group_"]').on('click', function () {
		var btn = $(this);
		showConfirmationPopup(emptyOrCloseGroup, btn, "group.empty.modal.",
				$('#group-name_' + btn.prop('id').split('_')[1]).text());
	});

	$('[id^="close-group_"]').on('click', function () {
		var btn = $(this);
		showConfirmationPopup(emptyOrCloseGroup, btn, "group.close.modal.",
				$('#group-name_' + btn.prop('id').split('_')[1]).text());
	});

    // change followed states
    $('[id^="follow-group"]').on('click', function () {
    	var followed = $('.group-followed-form');
    	var actionButtons = $('#action-buttons');
    	var saveButtons = $('#save-buttons');
    	var saveFollowed = $('#save-follow-group');
    	actionButtons.hide();
    	saveButtons.show();
        followed.show();
        saveFollowed.on('click', function () {
            timeout = startWaitingModal();
            followGroups( buildFollowedStateDate(followed)).done(function (data) {
                stopWaitingModal(timeout);
				slideDefaultSuccessMessage();
				doLoadGroups();
            }).fail(function (jqXHR) {
                stopWaitingModal(timeout);
                $('#msg-div').append(jqXHR.responseText);
			});
        });

        // toggle all checkboxes
        $('[id^="group-followed-checkall"]').on('click', function () {
            followed.prop("checked", $(this).prop("checked"));
        });
    });

    // follow one given group
    $('[id^="do_follow_group_"]').on('click', function () {
        timeout = startWaitingModal();
        followGroup($(this).prop('id').split('_')[3], true).done(function (data) {
            stopWaitingModal(timeout);
			slideDefaultSuccessMessage();
			doLoadGroups();
        }).fail(function (jqXHR) {
            stopWaitingModal(timeout);
            $('#msg-div').append(jqXHR.responseText);
        });
    });

    // unfollow one given group
    $('[id^="do_unfollow_group_"]').on('click', function () {
        timeout = startWaitingModal();
        followGroup($(this).prop('id').split('_')[3], false).done(function (data) {
            stopWaitingModal(timeout);
			slideDefaultSuccessMessage();
			doLoadGroups();
        }).fail(function (jqXHR) {
            stopWaitingModal(timeout);
            $('#msg-div').append(jqXHR.responseText);
        });
    });

    // toggle groups by followed state
    $('#manage-groups').find('#toggle-follow-group').on('click', function () {
        $('#manage-groups').find('.notfollowed-group').toggle();
    	$(this).find('span').toggle();
	});

    // subscription

    // handler on leave-group buttons
    $('[id^="leave-group"]').on('click', function () {
        showConfirmationPopup(doUnsubscribe, $(this), "group.leave.modal.", $(this).val());
    });

    // handler on switch-group (default) buttons
    $('[id^="switch-group"]').on('click', function () {
        showConfirmationPopup(doChangeDefault, $(this), "group.switch.modal.", $(this).val());
    });

    // handler on "join-group" button
	$('[id^="join_group-"]').on('click', function () {
		joinGroup($(this).prop('id').split('-')[1]).done(function (data) {
			slideDefaultSuccessMessage();
			window.location.reload();
		}).fail(function (jqXHR) {
			showErrorMessage(jqXHR);
		});
    });

}

/**
 * Build data from page for followed groups state
 *
 * param followed all followed input state
 */
function buildFollowedStateDate(followed){
    var data = {};
    data.followed = [];
    followed.each(function() {
    	if($( this ).attr("data-group") !== undefined) {
            var that = {};
            that.groupId = parseInt($(this).attr("data-group"));
            that.followed = $(this).prop("checked");
            data.followed.push(that);
        }
    });
    return JSON.stringify(data);
}

/**
 * Handle revoke/authorize member requests
 *
 * param btn the actual button pressed (used to retrieve group id and member id)
 */
function revokeMember(btn) {
	var revoke = btn.prop('id').indexOf('true') !== -1;
	setBannedInGroup(btn.prop('id').split("_")[1], btn.prop('id').split("_")[2], revoke).done(function (data) {
		if (btn.prop('id').split("_")[2] === '-1') {
			// replace admin content
			$('#admin').empty().append(data);
		} else {
      // replace manage groups content
      $('#manage-groups').empty().append(data);
      addManageGroupListeners();
			// open div of group
			$('.showall-' + btn.prop('id').split("_")[2]).addClass('in');
		}

	}).fail(function (jqXHR) {
    if (jqXHR.status === 400) {
      showErrorMessage(jqXHR);
    } else {
      // full rebuild
      replaceContent('body', jqXHR.responseText, 'body');
    }
	});
}

/**
 * Empty group content or delete whole group
 *
 * @param btn the actual button pressed (used to retrieve group id and if group must be completely deleted)
 */
function emptyOrCloseGroup(btn) {
	emptyGroup(btn.prop('id').split("_")[1], btn.prop('id').indexOf('close') === 0).done(function (data) {
    $('#manage-groups').empty().append(data);
		addManageGroupListeners();

	}).fail(function (jqXHR) {
    if (jqXHR.status === 401) {
      replaceContent('body', jqXHR.responseText, 'body')
    } else {
      showErrorMessage(jqXHR);
    }
	});
}
