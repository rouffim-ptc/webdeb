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
 * This file gathers cross-cutting custom scripts
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

/*
 * GLOBAL TYPEAHEADS
 */

let MIN_TYPEAHEAD = 0;
let MAX_TYPEAHEAD = 20;
let TIMEOUT_TYPEAHEAD = 100;
let SUBMIT_BTN_SELECTOR = 'button[type="submit"], button[name="submit"], button[class="submit-btn"]';

let BOOTSTRAP_XS_MAX = 576;
let BOOTSTRAP_SM_MAX = 768;
let BOOTSTRAP_MD_MAX = 992;
let BOOTSTRAP_LG_MAX = 1200;

/**
 * Trigger the disable a submit button after one click to avoid multiple form send event when the document is ready.
 */
$(document).ready(function () {
    disableButtonsAfterSubmit();
});

/**
 * Disable a submit button after one click to avoid multiple form send
 */
function disableButtonsAfterSubmit() {
    $("body").on('click', 'button', doDisableButtonsAfterSubmit);
}

/**
 * Disable a submit button after one click to avoid multiple form send
 */
function doDisableButtonsAfterSubmit(e) {
    var that = $(this);
    if (that.is(SUBMIT_BTN_SELECTOR)) {
        if (that.hasClass("disabled")) {
            e.preventDefault();
        } else {
            that.addClass("disabled");
            setTimeout(function () {
                that.removeClass('disabled');
            }, 5000);
        }
    }
}

/**
 * Enable all submit buttons
 */
function enableSubmitButtons() {
    $(SUBMIT_BTN_SELECTOR).prop('disabled', false);
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add place-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 */
function addPlacesTypeahead(element) {
    let elements = element.is('input[name$=".completeName"]') || element.length > 1 ? element : element.find(':input[name$=".completeName"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        avoidSubmitOnTypeahead(this);
        // add typeahead to that element
        addPlaceTypeahead($(this));
    });
}

/**
 * Add place typeahead to a given input element
 *
 * @param element an html element
 * @param type undefined for all, 0 for only existing
 */
function addPlaceTypeahead(element, type) {
    var ttimeout;
    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: "completeName",
        source: function (query, process) {
            if (ttimeout) {
                clearTimeout(ttimeout);
            }
            ttimeout = setTimeout(function () {
                return (type === 0 ? searchExistingPlace(query) : searchPlace(query)).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD); // threshold
        },
        templates: {
            suggestion: function (item) {
                return item.name;
            }
        }
    });

    // manage selected event => save id and name lang in hidden field
    element.on('typeahead:selected', function (obj, datum) {
        // will fill in either the affid and the actor id (since this typeahead works for both affiliations and authors
        let elmnt = $(this);
        elmnt.typeahead('val', datum['name']);
        elmnt.parents('.input-field').find('[name$=".id"]').val(datum['idPlace']);
        elmnt.parents('.input-field').find('[name$="geonameId"]').val(datum['idGeoname']);
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add text-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 */
function addTextsTypeahead(element) {
    var elements = element.find(' :input[type="text"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        avoidSubmitOnTypeahead(this);
        if (!$(this).hasClass("hidden")) {
            // add typeahead to that element
            addTextTypeahead(this, true);
        }
    });
}

/**
 * Add text typeahead to a given input element
 *
 * @param element an html element
 * @param withTypeaheadselection true if the selection must be done
 */
function addTextTypeahead(element, withTypeaheadselection, key) {
    withTypeaheadselection = withTypeaheadselection === undefined ? true : withTypeaheadselection;
    key = key === undefined ? 'title' : key;

    var ttimeout;
    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: key,
        source: function (query, process) {
            if (ttimeout) {
                clearTimeout(ttimeout);
            }
            ttimeout = setTimeout(function () {
                return searchText(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD); // threshold
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item, "TEXT");
            }
        }
    });

    if (withTypeaheadselection) {
        // manage selected event => save id and name lang in hidden field
        element.on('typeahead:selected', function (obj, datum) {
            $(this).typeahead('val', datum[key]);
            $(this).parents('.input-typeahead').find('[name$=".id"]').val(datum['id']);
            $(this).parents(".input-field").find('input[name$="id"]').val(datum['id']);
            $(this).parents(".input-field").find('input[name$="textId"]').val(datum['id']);
            $(this).parents(".input-field").find('input[name$="textIdFromUrl"]').val(datum['id']);
            $(this).parents(".input-field").find('input[name$="textIdFormUrlOrTitle"]').val(datum['id']);
            $(this).parents(".input-field").find('input[name$="textIdFromTitle"]').val(datum['id']);
        });
    }
}

/**
 * Add text typeahead to a given input element
 *
 * @param element an html element
 * @param type the type of argument, if any otherwise -1
 * @param shade of the argument, if any otherwise -1
 * @param key     the key (field from json data) to be displayed
 *
 */
function addArgumentTypeahead(element, type, shade, key) {
    var ttimeout;
    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: key,
        source: function (query, process) {
            if (ttimeout) {
                clearTimeout(ttimeout);
            }
            ttimeout = setTimeout(function () {
                shade = element.parents('form').find('#shade').exists() ?
                    element.parents('form').find('#shade').val() : shade;

                return searchArgument(query, type, shade, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD); // threshold
        },
        templates: {
            suggestion: function (item) {
                return item.title;
            }
        }
    });

    // manage selected event => save title
    $(element).on('typeahead:selected', function (obj, datum) {
        $(this).typeahead('val', datum['title']);
        $(this).parents('.input-field').find('[name$=".id"]').val(datum['id']);
        $(this).parents('.input-typeahead').find('[name$=".argumentId"]').val(datum['id']);
    });
}

/**
 * Add argument dictionary typeahead to a given input element
 *
 * @param element an html element
 * @param key the key (field from json data) to be displayed
 *
 */
function addArgumentDictionaryTypeahead(element, key) {
    element = element instanceof jQuery ? element : $(element);
    var ttimeout;
    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: 'title',
        source: function (query, process) {
            if (ttimeout) {
                clearTimeout(ttimeout);
            }
            ttimeout = setTimeout(function () {
                return searchArgumentDictionary(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD); // threshold
        },
        templates: {
            suggestion: function (item) {
                return item.title;
            }
        }
    });

    // manage selected event => save title
    element.on('typeahead:selected', function (obj, datum) {
        $(this).typeahead('val', datum['title']);
        $(this).parents('.input-typeahead').find('[name$=".dictionaryId"]').val(datum['id']);
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add citation-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 * @param argIdToIgnore the contextualized argument id to ignore, if any
 * @param textToLook the text id where to search, if any
 */
function addCitationsTypeahead(element, argIdToIgnore, textToLook) {
    var elements = element.find(' :input[type="text"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        avoidSubmitOnTypeahead(this);
        if (!$(this).hasClass("hidden")) {
            // add typeahead to that element
            addCitationTypeahead(this, argIdToIgnore, textToLook)
        }
    });
}

/**
 * Add citation typeahead to a given input element
 *
 * @param element an html element
 * @param argIdToIgnore the contextualized argument id to ignore, if any
 * @param textToLook the text id where to search, if any
 *
 */
function addCitationTypeahead(element, argIdToIgnore, textToLook, key) {
    element = element instanceof jQuery ? element : $(element);
    var ttimeout;
    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: key ? key : 'originalExcerpt',
        source: function (query, process) {
            if (ttimeout) {
                clearTimeout(ttimeout);
            }
            ttimeout = setTimeout(function () {
                return searchCitation(query, textToLook, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD); // threshold
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item, "CITATION");
            }
        }
    });

    // manage selected event => save title
    $(element).on('typeahead:selected', function (obj, datum) {
        $(this).typeahead('val', datum['originalExcerpt']);
        $(this).parents('.input-typeahead').find('[name$=".id"]').val(datum['id']);
        $(this).parents('.input-field').find('[name$=".id"]').val(datum['id']);
        $(this).parents('.input-field').find('input[name="' + key + '"]').val(datum['id']);
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add actor-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 * @param child filter name (among child elements from id)
 * @param actortype a given actortype to filter among actors for typeahead call
 * @param professionType a given professiontype to filter among professions for typeahead call
 */
function addActorsTypeahead(element, child, actortype, professionType, withFunction) {
    withFunction = withFunction === undefined ? true : withFunction;
    let elements = element.find('input[id$="' + child + '"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        // filter on child-like names only
        if ($(this).prop('id').indexOf(child) !== -1) {
            avoidSubmitOnTypeahead(this);
            // add typeahead to that element
            addActorTypeahead(this, actortype);

            // check whether we have a "function" field in same input-group
            let func = $(this).parents('.entry').find('[name$="function"]');

            if (func.exists() && withFunction) {
                let id = $(this).parents('.input-group').find('[name$=".id"]');
                // if a value exists in hidden actor id field (form has been pre-loaded with actor), load actor functions
                id = id.length === 0 ? -1 : $(id).val();

                addActorFunctionTypeahead(id, func, 2, professionType);
                // manage selected event => save id in hidden field and fetch functions, if necessary
                $(this).on('typeahead:selected', function (obj, datum) {
                    // add functions of selected actor to next input box (function)
                    addActorFunctionTypeahead(datum['id'], func, 2, professionType);
                });
            }
        }
    });
}

/**
 * Add typeahead to an actor input element
 *
 * @param element an html element
 * @param actortype the type of the actors we want to retrieve to be displayed (-1 for any, 0 for person, 1 for org)
 * @param key the typeahead key to display
 *
 */
function addActorTypeahead(element, actortype, key) {
    actortype = actortype === undefined ? -1 : actortype;
    let atimeout;

    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: key ? key : 'fullname',
        source: function (query, process) {
            if (atimeout) {
                clearTimeout(atimeout);
            }
            atimeout = setTimeout(function () {
                return searchActor(query, actortype, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item, "ACTOR");
            }
        }
    });

    // manage selected event => save id and name lang in hidden field
    $(element).on('typeahead:selected', function (obj, datum) {
        // will fill in either the affid and the actor id (since this typeahead works for both affiliations and authors
        $(this).parents('.input-field').find('[name$=".id"]').val(datum['id']);
        $(this).parents('.input-typeahead').find('[name$=".id"]').val(datum['id']);
        $(this).parents('.input-field').find('[name$="affid"]').val(datum['id']);
        $(this).parents('.input-field').find('[name$="lang"]').val(datum['lang']);

        let entry = $(this).parents('.entry');
        let firstname = entry.find('input[name$=".first"]');
        let lastname = entry.find('input[name$=".last"]');
        if (entry.exists() && firstname.exists() && lastname.exists()) {
            firstname.val(datum['firstname']);
            lastname.val(datum['lastname']);
        } else {
            let name = key ? datum[key] : datum['actortype'] === 1 ? datum['lastname'] : datum['fullname'];
            $(this).typeahead('val', name);
        }
    });

}

/**
 * Add typeahead to an actor input element
 *
 * @param element an html element
 *
 */
function addMemberElectionTypeahead(element) {
    var atimeout;
    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3
    }, {
        displayKey: "fullname",
        source: function (query, process) {
            if (atimeout) {
                clearTimeout(atimeout);
            }
            atimeout = setTimeout(function () {
                return searchPartyMembers(query).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                return item;
            }
        }
    });

    // manage selected event => save title
    $(element).on('typeahead:selected', function (obj, datum) {
        $(this).typeahead('val', datum.split(" (")[0]);
        $(this).parents("form").find('button').click();
    });
}

/**
 * Get all functions of a given actor
 *
 * @param actorId the id of the actor selected in the preceeding input box
 * @param element the html element to which the typeahead will be added
 * @param minLength the minimum typing length
 * @param professionType a given professiontype to filter among professions for typeahead call
 */
function addActorFunctionTypeahead(actorId, element, minLength, professionType) {
    let ftimeout;
    avoidSubmitOnTypeahead(element);
    $(element).typeahead('destroy'); // must explicitly destroy typeahead before rebuilding it
    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: minLength
    }, {
        displayKey: 'function',
        source: function (query, process) {
            if (ftimeout) {
                clearTimeout(ftimeout);
            }

            ftimeout = setTimeout(function () {
                return getActorFunctions(actorId, professionType, query).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                let tips = item.fullfunction;

                if (item.aha !== -1) {
                    tips = '<span class="fa fa-save"></span>' + ' ' + '<i>' + tips + '<i>';
                }
                return tips;
            }
        }
    });
    $(element).on('typeahead:selected', function (obj, datum) {
        let group = $(this).parents('.entry').first();
        $(this).typeahead('val', datum['function']);
        // set affname, function and ids in separated field
        group.find('[name$="function"]').typeahead('val', datum['function']);
        if (datum['affname'] !== '') {
          group.find('[name$="affname"]').typeahead('val', datum['affname']);
          group.find('[name$="affid"]').val(datum['affid']);
        }
        if (datum['aha'] !== -1) {
          // do not reset aha since we may have a value (updating an existing affiliation)
          group.find('[name$="aha"]').val(datum['aha']);
        }
        let functionid = group.find('[name$="functionid"]');
        if (functionid.exists()) {
            $(functionid).val(datum['functionid']);
        }
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add tag-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 * @param type the type of tag
 * @param child filter name (among child elements from id)
 */
function addTagsTypeahead(element, type, child) {
    let elements = element.is('input[type="text"]') || element.length > 1 ? element : element.find(':input[type="text"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        avoidSubmitOnTypeahead(this);
        if (!$(this).hasClass("hidden")) {
            // add typeahead to that element
            addTagTypeahead(this, type);
        }
    });
}

/**
 * Get all tags and add auto-completion to given element
 *
 * @param element an html element
 * @param type the type of tag
 */
function addTagTypeahead(element, type, withDetails) {
    let ftimeout;
    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 2,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: 'name',
        source: function (query, process) {
            if (ftimeout) {
                clearTimeout(ftimeout);
            }

            ftimeout = setTimeout(function () {
                return searchTag(query, type, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, 100);
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item, "TAG", withDetails);
            }
        }
    });

    // manage selected event => save id and name lang in hidden field
    $(element).on('typeahead:selected', function (obj, datum) {
        $(this).typeahead('val', datum['name']);
        $(this).parents('.input-typeahead').find('[name$=".id"]').val(datum['tagId']);
        $(this).parents('.input-field').find('[name$=".id"]').val(datum['tagId']);
        let group = $(this).parents('.input-field');
        // set name and id
        let tagId = group.find('[name$=".tagId"]');
        if (tagId.exists()) {
            tagId.val(datum['tagId']);
        }
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add tag-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 */
function addContributorGroupsTypeahead(element) {
    let elements = element.find(' :input[type="text"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        let elmnt = $(this);
        avoidSubmitOnTypeahead(this);

        if (!elmnt.hasClass("hidden")) {
            // add typeahead to that element
            addContributorGroupTypeahead(elmnt);
            // manage selected event => save id and name lang in hidden field
            elmnt.on('typeahead:selected', function (obj, datum) {
                // will fill in either the affid and the actor id (since this typeahead works for both affiliations and authors
                elmnt.typeahead('val', datum['name']);
                elmnt.parents('.input-field').find('[name$=".groupId"]').val(datum['id']);
            });
        }
    });
}

/**
 * Get all tags and add auto-completion to given element
 *
 * @param element an html element
 */
function addContributorGroupTypeahead(element) {
    let ftimeout;

    element.typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 2,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: 'name',
        source: function (query, process) {
            if (ftimeout) {
                clearTimeout(ftimeout);
            }

            ftimeout = setTimeout(function () {
                return searchGroups(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                return item.name;
            }
        }
    });
}

/**
 * Get all input sub-elements from the given element identified by id (possibly filtered on child name)
 * and add debate-related typeaheads (autocompletion)
 *
 * @param element parent div element (input group) for all inputs to add typeahead to
 * @param idToIgnore the debate id to ignore, or null
 * @param child filter name (among child elements from id)
 */
function addDebatesTypeahead(element, idToIgnore, child) {
    let elements = element.find(' :input[type="text"]');
    // add typeahead to all children elements containing 'child' name
    elements.each(function () {
        avoidSubmitOnTypeahead(this);
        // add typeahead to that element
        addDebateTypeahead(this, idToIgnore);

        // manage selected event => save id and name lang in hidden field
        $(this).on('typeahead:selected', function (obj, datum) {
            $(this).typeahead('val', datum['name']);
            let group = $(this).closest('.input-field');
            // set name and id
            let debateId = group.find('[name$="debateId"]');
            if (debateId !== undefined) {
                $(debateId).val(datum['debateId']);
            }
        });
    });
}

/**
 * Get all debates and add auto-completion to given element
 *
 * @param element an html element
 * @param idToIgnore the debate id to ignore, or null
 */
function addDebateTypeahead(element, idToIgnore, key) {
    let ftimeout;
    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 2,
        limit: MAX_TYPEAHEAD
    }, {
        displayKey: key ? key : 'fullTitle',
        source: function (query, process) {
            if (ftimeout) {
                clearTimeout(ftimeout);
            }

            ftimeout = setTimeout(function () {
                return searchDebate(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item, "DEBATE");
            }
        }
    });

    // add value to element when typeahead is selected
    $(element).on('typeahead:selected', function (obj, datum) {
        // fill the value with the tag name
        $(element).typeahead('val', datum['fullTitle']);
        $(element).parents('.input-field').find('[name$=".id"]').val(datum['id']);
        $(element).parents('.input-typeahead').find('[name$=".id"]').val(datum['id']);
    });
}

/**
 * Manage add/remove buttons generic
 *
 * @param id the parent input group id to clone (no leading dash)
 * @param child array of filter elements
 * @param delete_msg a message at the deletion
 * @param functiondel the function that called after deletation
 * @param addTypeahead a typehead to add at the new entry
 * @param typeaheadArg other argument for typehead
 * @param typeaheadArg2 other argument for typehead
 */
function manageAddRmButton(id, child, delete_msg, functiondel, addTypeahead, typeaheadArg, typeaheadArg2) {
    // manage add button (clone input)
    let parent = id instanceof jQuery ? id : $('#' + id);

    parent.on('click', '.btn-add', function () {
        addField($(this), child, addTypeahead, typeaheadArg, typeaheadArg2);
    });

    // manage 'remove' button
    parent.on('click', '.btn-remove', function () {
        let entry = $($(this).parents('.entry')[0]).parent().children('.entry:last');
        let descr = child != null && child.length > 0 ?
            $(entry).find('[name$="' + child[0] + '"]')
            : $(entry).find('[name$="first"]').val() + " " + $(entry).find('[name$="last"]');
        if (descr !== undefined && descr.val() !== undefined && descr.val() !== '') {
            descr = "" + descr.val() + "";
        } else {
            descr = "";
        }
        if (typeof functiondel === "function") {
            functiondel(this);
        } else {
            showConfirmationPopup(removeGenericEntry, this, 'entry.delete.confirm.', descr);
        }
        return false;
    });
}

/**
 * Add new general fields in form
 *
 * @param element the parent input group
 * @param child array of filter elements
 * @param addTypeahead a typehead to add at the new entry
 * @param typeaheadArg other argument for typehead
 * @param typeaheadArg2 other argument for typehead
 */
function addField(element, child, addTypeahead, typeaheadArg, typeaheadArg2) {
    // clone them and add clone at the end of list
    let entry = $(element.parents('.entry')[0]).parent().children('.entry:last');
    entry.find('input').typeahead('destroy'); // avoid having typeahead troubles on cloned input
    let newEntry = $(entry.clone());
    entry.after(newEntry);

    // build new ids for any elements in cloned elements
    let splitId = newEntry.prop('id').split('_');
    let idx = parseInt(splitId[1]) + 1;
    newEntry.prop('id', splitId[0] + "_" + idx + "_" + splitId[2]);
    newEntry.find('.entry-to-increment').text(parseInt(newEntry.find('.entry-to-increment').text()) + 1);

    newEntry.find('*').each(function () {
        if (this.hasAttribute('id')) {
            splitId = $(this).prop('id').split('_');
            $(this).prop('id', splitId[0] + "_" + idx + "_" + splitId[2]);

            // many ifs, but no other way...
            if (this.hasAttribute('name')) {
                $(this).prop('name', splitId[0] + "[" + idx + "]." + splitId[2]);
            }

            // for text inputs, clear values
            if (($(this).prop('type') === 'text') || $(this).prop('type') === 'url' || $(this).prop('tagName') === 'SELECT') {
                $(this).val("");
                $(this).removeClass("is-invalid");
                $(this).parents('.form-group').find('.invalid-feedback').empty();
            }

        } else {
            if (this.hasAttribute("for")) { // labels
                splitId = $(this).prop('for').split('_');
                $(this).prop('for', splitId[0] + "_" + idx + "_" + splitId[2]);
            }
        }
    });

    if (addTypeahead != null && addTypeahead !== undefined) {
        if (typeaheadArg != null && typeaheadArg !== undefined) {
            addTypeahead(findEntryTypeaheadInput(newEntry, child), typeaheadArg);
            addTypeahead(findEntryTypeaheadInput(entry, child), typeaheadArg);
        } else {
            addTypeahead(findEntryTypeaheadInput(newEntry, child));
            addTypeahead(findEntryTypeaheadInput(entry, child));
        }
    }
}

function findEntryTypeaheadInput(entry, child) {
    return entry.find('input' + (child[0] ? '[name$="' + child[0] + '"]' : '')).first();
}

/*
 * ACTOR/AUTHOR/AFFILIATION-RELATED MANAGEMENT
 */

/**
 * Manage add/remove buttons for actor fields as affiliations or authors
 *
 * @param element the parent of inputs group
 * @param child array of filter elements (will add 'actor typeaheads' to each passed elements)
 * @param actortype a given actortype to filter among actors
 * @param professionType a given professiontype to filter among professions for typeahead call
 */
function manageActorButton(element, child, actortype, professionType) {

    // manage add button (clone input)
    element = element instanceof jQuery ? element : $('#' + element);

    element.on('click', '.btn-add', function () {
        addActorField(element, child, actortype, professionType);
    });

    // manage 'remove' button
    element.on('click', '.btn-remove', function () {
        let entry = $(this).parents('.entry:first');
        let descr = child.length > 0 ?
            $(entry).find('[name$="' + child[0] + '"]').val()
            : $(entry).find('[name$="first"]').val() + " " + $(entry).find('[name$="last"]').val();
        let func = $(entry).find('[name$="function"]');
        if (func !== undefined && func.val() !== undefined && func.val() !== '') {
            descr += " (" + func.val() + ")";
        }
        showConfirmationPopup(removeGenericEntry, this, "entry.delete.confirm.", descr);
        return false;
    });
}

/**
 * Add new actor fields in form
 *
 * @param element the parent input group
 * @param child array of ids to filter child elements from given elements to add typeahead feature
 * @param actortype a given actortype to filter among actors for the added typeahead feature
 * @param professionType a given professiontype to filter among professions for typeahead call
 */
function addActorField(element, child, actortype, professionType) {
    // clone them and add clone at the end of list
    let entry = element.find('.entry:last');
    entry.find('input').typeahead('destroy'); // avoid having typeahead troubles on cloned input
    let newEntry = $(entry.clone());
    entry.after(newEntry);

    // build new ids for any elements in cloned elements
    // id of the form "somename_i_somefield", name of the form  "affiliation[i].field (ifexists)
    let splitId = newEntry.prop('id').split('_');
    let part2 = splitId[splitId.length - 1];
    let idx = parseInt(splitId[splitId.length - 2]) + 1;
    let part1Id = splitId.slice(0, splitId.length - 2).join('_');
    let part1Name = splitId.slice(0, splitId.length - 2).join('.');
    newEntry.prop('id', part1Id + "_" + idx + "_" + part2);

    newEntry.find('.form-control').each(function () {
        if (this.hasAttribute('id')) {
            splitId = $(this).prop('id').split('_');
            part2 = splitId[splitId.length - 1];

            $(this).prop('id', part1Id + "_" + idx + "_" + part2);

            // many ifs, but no other way...
            if (this.hasAttribute('name')) {
                $(this).prop('name', part1Name + "[" + idx + "]." + part2);
            }

            // for text inputs, clear values
            if (($(this).prop('type') === 'text' && $(this).prop('id') !== 'lang') || $(this).prop('tagName') === 'SELECT') {
                $(this).val('');
                $(this).prop("readonly", false);
                $(this).removeClass("is-invalid");
                $(this).parents('.form-group').find('.invalid-feedback').empty();
            } else {
                if ($(this).prop('type') === 'checkbox') {
                    // reset disambiguated hidden value, in case of resubmission
                    $(this).prop('checked', false);
                    $(this).removeClass("is-invalid");
                    $(this).parents('.form-group').find('.invalid-feedback').empty();
                }
            }

            // rounded toggle box
            if ($(this).hasClass('roundedbox')) {
                $(this).find('input').val("false");
                updateRoundedbox($(this).parent());
                listenerOnRoundedBox($(this).parent(), '[name$="ongoingDate"]');
            }

        } else {
            if (this.hasAttribute("for")) { // labels
                splitId = $(this).prop('for').split('_');
                part2 = splitId[splitId.length - 1];
                $(this).prop('for', part1Id + "_" + idx + "_" + part2);
            }
        }
    });

    // add typeahead to cloned inputs and input
    addActorsTypeahead(entry, 'fullname', actortype, undefined, false);
    addActorsTypeahead(entry, 'affname', '1', professionType);
    manageActorButton(entry, ['fullname', 'affname'], actortype);

    addActorsTypeahead(newEntry, 'fullname', actortype, undefined, false);
    addActorsTypeahead(newEntry, 'affname', '1', professionType);
    manageActorButton(newEntry, ['fullname', 'affname'], actortype);

    if(typeof managePrecisionDate === 'function')
        managePrecisionDate(newEntry);
}

function treatContributionNameMatchesAsync(toCallAsync, managePanel, container, id, submitBtn){
    submitBtn.on('click', function(e){

        if(!$(this).hasClass(".hide-submit")) {
            e.preventDefault();
            treatHandleContributionMatchesAsyncFunction(toCallAsync, container, id, managePanel, submitBtn)
        }
    });
}

function treatHandleContributionMatchesAsyncFunction(toCallAsync, container, id, managePanel, submitBtn){
    if(toCallAsync != null) {
        toCallAsync(id, container.is('form') ? container : container.find('form')).done(function (data) {
            window.location.href = data.replace(/"/g, '');
        }).fail(function (xhr) {
            let status = xhr.responseText.length === 0 ? -1 : xhr.status;

            switch (status) {
                case 400:
                case 406:
                    // form has errors => clear form and rebuilt
                    replaceContent(container.find('form').find('fieldset').first(), xhr.responseText, 'fieldset');
                    $(document).scrollTop(0);
                    if(typeof managePanel === "function")
                        managePanel(container);
                    break;

                case 409:
                    // actor names match
                    treatHandleContributionMatchesModal(container, xhr.responseText, submitBtn);
                    break;

                default:
                    // any other (probably a crash)
                    if(isValidUrl(xhr.responseText) || xhr.responseText.replace(/"/g, '').startsWith('/')){
                        window.location = xhr.responseText.replace(/"/g, '');
                    } else {
                        slideDefaultErrorMessage();
                    }
            }
        });
    }
}

function claimContributionListener(container) {

    container.find('.contribution-claim-btn').on('click', function(){
        let id = getContributionOptionData($(this), "id");

        getClaimContribution(id, window.location.href).done(function (html) {
            loadAndShowModal($('#modal-anchor'), html);
            let modal = $("#modal-claim");

            modal.find('button[name="submit"]').on('click', function(){
                let form = {};
                form.contribution = id;
                form.url = modal.find('#url').val();
                form.comment = modal.find('#comment').val();
                form.type = modal.find('#claimType').children("option:selected").val();

                saveClaimContribution(JSON.stringify(form)).done(function (data) {
                    hideAndDestroyModal(modal);
                    slideDefaultSuccessMessage();
                    stopWaitingModal();
                }).fail(function (xhr) {
                    stopWaitingModal();
                    slideDefaultErrorMessage();
                });
            });

        }).fail(function (jqXHR) {
            stopWaitingModal();
            if (jqXHR.status === 401) {
                redirectToLogin();
            } else {
                console.log("Error with claim modal");
            }
        });
    });

}

function treatHandleContributionMatchesModal(container, content, submitBtn){
    let autocreated_modal = $('#autocreated');
    submitBtn = submitBtn === undefined || !submitBtn.exists() ? container.find('.submit').first() : submitBtn;

    loadAndShowModal(autocreated_modal, content);

    let namesakeModal = $('#namesake-modal');
    if (namesakeModal.exists()) {
        if (namesakeModal.find('#isActor').val() === 'true') {
            handleContributionNameMatches(container.find('[id$="fullname"]'), '[id$="_id"]', '[id$="_isDisambiguated"]', submitBtn, "Actor");
        }else{
            handleContributionNameMatches(container.find('[id$="affname"]'), '[id$="_affid"]', '[id$="_isAffDisambiguated"]', submitBtn, "Actor");
        }

        treatCreatedActors(namesakeModal.find('.created-actors').text());
    }
}

function treatCreatedActors(created) {
    if(created) {
        let container = $('.contribution-form');
        created = JSON.parse(created);

        if (Array.isArray(created)) {
            created.forEach(newOne => {
                let entry = container.find('#' + newOne.formId).find('.entry').eq(newOne.formIndex);

                if (newOne.isNew) {
                    entry.find('[id$="id"]').val(newOne.id);
                    console.log(entry.find('[id$="isNew"]').val());
                    entry.find('[id$="isNew"]').val(true);
                    console.log(entry.find('[id$="isNew"]').val());
                }

                if (newOne.isAffNew) {
                    entry.find('[id$="affid"]').val(newOne.affid);
                    entry.find('[id$="isAffNew"]').val(true);
                }
            });
        }
    }
}

/**
 * Manage name matches for contributions fields (author or tag fields in arguments or texts)
 *
 * @param name2update the selector to search in for selected name in modal
 * @param id2update the id selector to retrieve the id element to update with selected contribution from modal if needed
 * @param flag the selector to a checkbox to search in name2update parent div to set to true when this name has been disambiguated
 * @param submit the submit button to click after the decision is made
 * @param type the contribution type name
 */
function handleContributionNameMatches(name2update, id2update, flag, submit, type) {
    let modal = $('#namesake-modal');
    if (modal.length > 0) {
        let okbtn = "#createnew";
        let loadbtn = '#load';

        new Pager(modal.find('.pageable'), 3).reset();

        // reset load button label, activate toggling between multiple choices (namesakes cards) and show modal
        $(loadbtn).text(Messages('entry.' + type.toLowerCase() + '.existing.use'));
        $(loadbtn).prop('disabled', true);
        toggleSummaryBoxes('#namesake-boxes', '.toggleable');
        modal.modal();

        // clamping affiliations list
        modal.find('li').each(function () {
            $(this).addClass('forcewrap');
        });

        // handle click on create a "new contribution" button (this creating a namesake)
        $(modal).on('click', okbtn, function () {
            updateNameMatch(modal, name2update, undefined, flag, submit, type);
        });

        // handle click on load
        $(modal).on('click', loadbtn, function () {
            // get selected element and fill form with this wrapper
            if ($(modal).find('.selected').prop('id').split("_")[1] === null) {
                // no selected element, display error message (should not happen thanks to toggleSummaryBoxes
                showMe($(modal).find("#msg-div"), true, true);
            } else {
                updateNameMatch(modal, name2update, id2update, flag, submit, type);
            }
        });
    }
}

function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

/**
 * Update name of a matched contribution (actor or tag) from DB in given div as selected from given modal
 *
 * @param modal the namesake modal where user selects a namesake or choose to create a new one
 * @param name2update the selector to search in for selected name in modal
 * @param id2update the selector to search in name2update parent div to update with the selected id (if updateValue is true)
 * @param flag the selector to a checkbox to search in name2update parent div to set to true when this name has been disambiguated
 * @param submit the submit button to trigger in order to send the form (in given div)
 * @param type the contribution type name
 */
function updateNameMatch(modal, name2update, id2update, flag, submit, type) {

    // fill all possible spellings (displayed name, acronym and hidden alternative spellings)
    let updated = false;
    let index = $(modal).find('#indexActorForm').val();
    index = !isNaN(index) ? Number.parseInt(index) : undefined;

    let spellings = [];
    let selectedId;

    if($(modal).find('.selected').exists()) {
        spellings.push($(modal).find('.selected').data('name').removeAccents().toLowerCase());

        selectedId = $(modal).find('.selected').data('id');
    }

    let selector = modal.find('#selector').val();
    name2update = selector.length > 0 ? name2update.filter('[id^="' + selector + '"]') : name2update;

    // find all elements with same name as modal title (or any .namesake element) with no namesake value
    name2update.each(function (name2updateIndex) {
        if(!updated) {
            let parent = $(this).closest('div.input-field');

            parent = parent.exists() ? parent.parent() : $(this).closest('div.input-div-group');
            let isDisambiguated = parent.find(flag);

            if(id2update === undefined) {

                isDisambiguated.prop('checked', true);

            } else if ($(this).val() !== '' && !isDisambiguated.prop('checked')) {

                if(Number.isInteger(index) && name2updateIndex === index){
                    doUpdateNameMatch($(this), isDisambiguated, parent, id2update, spellings[0], selectedId);
                    updated = true;
                } else {
                    let current = $(this).val().removeAccents().toLowerCase();

                    for (let i = 0; i < spellings.length && !updated; i++) {
                        if (spellings[i] === current) {
                            doUpdateNameMatch($(this), isDisambiguated, parent, id2update, spellings[i], selectedId);
                            updated = true;
                            // we found it, break current loop and go to next name (because isDisambiguated will be true)
                            break;
                        }
                    }
                }
            }
        }
    });
    // emulate submit click to trigger right event (submitting the form)
    hideAndDestroyModal(modal, submit);
}

function doUpdateNameMatch(field, isDisambiguated, parent, id2update, spelling, selectedId){
    isDisambiguated.prop('checked', true);
    if (id2update !== undefined) {
        // ensure name is the selected contribution's name, and for actors not the acronym (or further server-side checking won't agree on name match)
        field.typeahead('val', spelling);
        // put selected id (from modal) in given id2update parent-relative element
        parent.find(id2update).val(selectedId);;
    }
}

/**
 * Hide given modal, remove it from DOM tree when completely hidden, remove modal-backdrop and trigger
 * click on given submit button.
 *
 * @param modal a modal frame
 * @param submit the submit button selector to trigger a click on (optional)
 */
function hideAndDestroyModal(modal, submit, removeBackdrop) {
    submit = submit instanceof jQuery ? submit : $(submit);

    if(modal != null) {
        modal = modal instanceof jQuery ? modal : $(modal);
        removeBackdrop = removeBackdrop !== undefined ? removeBackdrop : true;

        modal.modal('hide');
        modal.on('hidden.bs.modal', function () {
            modal.remove();

            if (removeBackdrop)
                $('.modal-backdrop').remove();
        });

        modal.remove();
    }

    if (submit != null && submit.exists()) {
        submit.trigger('click');
    }
}

function setInputValid(input, valid) {
    valid = valid === undefined ? true : valid;

    input.toggleClass('is-valid', valid);
    input.toggleClass('is-invalid', !valid);
}

/**
 * Hide given modal, remove it from DOM tree when completely hidden, remove modal-backdrop and trigger
 * click on given submit button and call a given function.
 *
 * @param modal a modal frame
 * @param func the event function to call
 * @param submit the submit button selector to trigger a click on (optional)
 */
function hideAndDestroyModalAndCallFunction(modal, func, submit) {
    hideAndDestroyModal(modal, submit);

    func();
}


/*
 * CROSS-CUTTING
 */

/**
 * open modal with "search text" bar
 *
 * @param contributionType a given contribution
 */
function openContributionSearchModal(contributionType) {
    contributionSelection(contributionType).done(function (html) {
        loadAndShowModal($('#merge-modal-anchor'), html);
    }).fail(function (jqXHR) {
        console.log("Error with contribution search modal");
    });
}

/**
 * open modal with "search text" bar
 *
 * @param id a contribution id
 * @param type its contribution type id
 * @param group the contributor group
 * @param force true to force
 * @param noChange true to for no change
 * @param contextOrReload a context id if contribution comes from a context or true for reloading the drag n drop
 */
function doDeleteContributionAsync(id, type, group, force, contextOrReload, noChange) {
    deleteContributionAsync(id, type, group, force, noChange).done(function (html) {
        triggerReloadVizEvent('.debate-' + contextOrReload, contextOrReload === true);
        slideMessage($('#success-delete'));
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            slideMessage($('#error-delete'));
        }
    });
}

/**
 * Handle simple dynamic input form with + and - buttons
 * (other than actors, arguments, etc. for which dedicated functions are provided)
 *
 * @param parentdiv the parent div that contains the input element
 * @param control the controls class element id
 * @param selector input selector to be renamed
 * @param typeahead the typeahead function to apply to cloned input (optional)
 */
function handleAddRemoveButton(parentdiv, control, selector, typeahead) {
    let controlid = '#' + control;

    // magic of jquery, add click listener also on newly created elements
    parentdiv.on('click', controlid + ' .btn-add', function () {
        cloneGenericEntry(parentdiv, control, selector, typeahead);
    });

    parentdiv.on('click', controlid + ' .btn-remove', function () {
        let descr = $(this).parents('.entry:first').find('[name^="' + control + '"] option:selected').text();
        showConfirmationPopup(removeGenericEntry, this, "entry.delete.confirm.", descr);
        return false;
    });
}

/**
 * Reusable modal popup to show informations to user
 *
 * param msg partial message key for title (.title) and content (.msg) to display in popup
 * param content the content to be passed to the message content as dynamic content
 */
function showInformativePopup(msg, content) {
    bootbox.dialog({
        message: (content == null ? Messages(msg + "msg") : Messages(msg + "msg", '<b>' + content + '</b>')),
        title: Messages(msg + "title"),
        buttons: {
            main: {
                label: Messages("general.btn.ok"),
                className: "btn-primary",
                callback: function () { /* ignore */
                }
            }
        }
    });
}

/**
 * Reusable modal popup to ask a confirmation to user about something
 *
 * param callback the function to call as a "confirm" callback
 * param element the element to pass as parameter to the callback function
 * param msg partial message key for title (.title) and content (.msg) to display in popup
 * param content the content to be passed to the message content as dynamic content
 */
function showConfirmationPopup(callback, element, msg, content) {
    bootbox.dialog({
        message: Messages(msg + "text", '<b>' + content + '</b>'),
        title: Messages(msg + "title"),
        buttons: {
            main: {
                label: Messages("general.btn.cancel"),
                className: "btn-default",
                // do nothing when cancel
                callback: function () { /* ignore */
                }
            },
            modify: {
                label: Messages("general.btn.confirm"),
                className: "btn-primary",
                callback: function () {
                    callback(element)
                }
            }
        }
    });
}

/**
 * Clone any "add-remove" entry other than actors/authors one. Get the last '.entry' element with id
 * from parentdiv context, clone and rename it (suffixed by counter). Add typeahead function, if any
 *
 * @param parentdiv the parent div that contains the input element
 * @param control the controls class element id
 * @param selector input selector to be renamed
 * @param typeahead the typeahead function to apply to cloned input (optional)
 */
function cloneGenericEntry(parentdiv, control, selector, typeahead) {
    // clone fields and add clone at the end of list
    let controlid = '#' + control;
    let entry = parentdiv.find(controlid).find('.entry:last');
    if (selector === 'input' && typeahead !== undefined) {
        // avoid having typeahead troubles on cloned input
        $(entry).find('input').typeahead('destroy');
    }
    let newEntry = $(entry.clone());
    entry.after(newEntry);
    let newInputs = newEntry.find(selector);
    let input = newInputs[0];

    // rename and set id by incrementing the index
    let splitId = $(input).prop('id').split('_');
    let idx = parseInt(splitId[1]) + 1;
    let suffixId = splitId.length > 2 ? '_' + splitId[2] : '';
    let suffixName = splitId.length > 2 ? '.' + splitId[2] : '';
    $(input).prop('id', splitId[0] + '_' + idx + suffixId);
    $(input).prop('name', splitId[0] + '[' + idx + ']' + suffixName);
    $(input).val('');

    // if a typeahead has been passed, apply it
    if (typeahead !== undefined) {
        typeahead($(entry).find('input'));
        typeahead(input);
    }
}

/**
 * Remove an .entry element from a .controls
 *
 * @param toremove the sub-button of the line to remove
 */
function removeGenericEntry(toremove) {
    // check if this line is the last displayed one, if not, remove it, otherwise clean inputs
    if ($(toremove).parents('.controls').children('.entry').length > 1) {
        $(toremove).parents('.entry:first').remove();
    } else {
        $(toremove).parents('.entry:first').find('[type="text"]').each(function () {
            if ($(this).hasClass('hidden')) {
                $(this).val(($(this).prop('id').indexOf('id') >= 0 || $(this).prop('id').indexOf('aha') >= 0) ? -1 : '');
            } else {
                $(this).val('');
                $(this).typeahead('val', ''); // for typeahead fields
            }
        });
        // put default values in select element (if any)
        $(toremove).parents('.entry:first').find('select option').prop('selected', false);
        // updated rounded box, if any
        let rounded = $(toremove).parents('.entry:first').find('.roundedbox');
        if (rounded.length > 0) {
            rounded.find('input').val("false");
            updateRoundedbox(rounded.parent());
        }
    }
}

/**
 * Show/hide an element
 *
 * @param element an html element id
 * @param b a boolean saying if the element must be shown (true) or hide (false)
 * @param effect boolean saying if the show/hide transition must be showed with a fade effect
 */
function showMe(element, b, effect) {
    if (effect) {
        b ? $(element).fadeIn(400) : $(element).fadeOut(400);
    } else {
        b ? $(element).show() : $(element).hide();
    }
}

/**
 * Disable submit on enter-key pressed (mainly used for typeahead fields)
 *
 * @param element an html element
 * @return false for "return" key pressed
 */
function avoidSubmitOnTypeahead(element) {
    $(element).keydown(function (event) {
        if (event.keyCode === 13) {
            return false;
        }
    });
}

/**
 * Call given callback method when enter key is pressed
 *
 * @param element a selector to an html element
 * @param callback a javascript method to be called when the 'enter' key is pressed
 */
function submitOnEnterKey(element, callback) {
    $(element).on('keydown', function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            callback();
        }
    });
}

/**
 * Load given modal content into given selector
 *
 * @param selector a selector that will contain the modal
 * @param content a modal content (full modal)
 */
function loadAndShowModal(selector, content) {
    stopWaitingModal();
    $('.modal-backdrop').remove();
    $(selector).empty().append(content);
    $(selector).children('.modal').modal('show');
}

/**
 * Replace the content of given selector object with given content
 *
 * @param selector the selector string that will be replaced by the new content
 * @param content the content to replace given selector
 * @param tag tag name to get content from in given content
 */
function replaceContent(selector, content, tag) {
    let element = selector instanceof jQuery ? selector : $(selector);
    let toappend = document.createElement('html');
    toappend.innerHTML = content;
    if(tag){
        element.replaceWith(toappend.getElementsByTagName(tag)[0]);
    } else {
        element.html(content);
    }
}

/**
 * Show a given message (html content, typically a bootstrap alert) in given div
 *
 * @param msg a message to display
 * @param div the div that will contain the given message
 */
function showMessage(msg, div) {
    $(document).find(div).append(msg);
    fadeMessage();
}

/**
 * Generic error message handling
 *
 * @param jqXHR an ajax request error object
 */
function showErrorMessage(jqXHR) {
    showMessage(jqXHR.responseText, '#msg-div');
}

/**
 * Simple helper to slide up given pre-filled message div without disposing it from html tree
 *
 * @param msg a msg div (jquery) to slide (show-hide)
 */
function slideMessage(msg, message) {
    msg.addClass('show');
    msg.show();
    msg.css('opacity', '');

    if(message){
        msg.find('span.message').text(message);
    }

    setTimeout(function () {
        msg.fadeTo(500, 0).slideUp(500, function () {
            $(this).removeClass('show');
        });
    }, 6000);
}

function slideDefaultErrorMessage(message) {
    slideMessage($('#general-message-danger' + (message ? '-empty' : '')), message);
}

function slideDefaultWarningMessage(message) {
    slideMessage($('#general-message-warning' + (message ? '-empty' : '')), message);
}

function slideDefaultSuccessMessage(message) {
    slideMessage($('#general-message-success' + (message ? '-empty' : '')), message);
}

/**
 * Fade alert messages of type .alert-fixed after 6 seconds
 */
function fadeMessage() {
    window.setTimeout(function () {
        $('.alert-fixed.alert-dismissible').fadeTo(500, 0).slideUp(500, function () {
            $(this).removeClass('show');
        });
    }, 6000);
}

/**
 * Manage a set of checkboxes for only one selection
 *
 * @param boxes an array of boxes
 * @param field the field that will contain the selected value
 * @param forceSelect true if one field must be selected (acting as radio boxes)
 */
function manageExclusiveCheckboxes(boxes, field, forceSelect) {
    // page load
    if (field != null && field.val() !== '' && boxes.length > 1) {
        boxes.each(function () { // must explicitly loop manually, direct jquery not working
            if ($(this).val() === field.val()) {
                $(this).prop('checked', true);
            }
        });
    }
    boxes.on('change', function () {
        if (field != null && $(this).prop('checked')) {
            field.val($(this).val());
        } else {
            if (forceSelect) {
                $(this).prop('checked', true);
            } else {
                if (field != null) {
                    field.val('');
                }
            }
        }
        boxes.not(this).prop('checked', false);
    });
}

/**
 * Initialize popover features for all helpbubbles in the given div
 *
 * @param div a div in which helpbubbles are present
 */
function handleHelpBubble(div) {
    $(div).find('.helpbubble').each(function () {
        $(this).popover(); // to be initialized (opt-in)
    });
}

/**
 * Add click event listener on rounded check box (toggle button)
 *
 * @param elem the element to attach the listener to
 * @param selector a jquery selector for the field holding the boolean value of the toggle box
 */
function listenerOnRoundedBox(elem) {

    $(elem).find('.input-group-action2').on("click", function () {
        let text = $(this).find('input');
        text.val(text.val() !== "true"); // boolean as string ...
        updateRoundedbox(elem);
    });
}

/**
 * Update graphically a input field that contains a rounded box to enable/disable it (to specify "unknown" value)
 *
 * @param boxgroup a group of inputs containing a rounded box
 */
function updateRoundedbox(boxgroup) {
    let checked = $(boxgroup).find('.input-group-action2').find('input').val();
    $(boxgroup).find('.input-group-action2').toggleClass('colored', checked === "true");
    // in case we have a text input to disable
    $(boxgroup).find('input[type="text"]').not('.d-none').prop('disabled', checked === "true");
}

/**
 * Put ellipsis on text boxes (multiline)
 *
 * @param el an html element
 * @param size the height of the box
 */
function ellipsizeTextBox(el, size) {
    let wordArray = el.html().split(' ');
    while (el.prop('scrollHeight') > size) {
        wordArray.pop();
        el.html(wordArray.join(' ') + '...');
        el.css('height', size + 'px');
    }
}

/**
 * Get a Y-M-D date and transform it in an appropriate format
 *
 * @param date a Y-M-D date (M and D maybe not present)
 * @returns {String} corresponding (DD/MM/)YYYY date (year possibly negative and with one to 4 digits)
 */
function formatDate(date) {
    let isBC = false;
    // date starts with negative sign -> negative date, remove sign
    if (date.indexOf('-') === 0) {
        isBC = true;
        date = date.substr(1, date.length - 2);
    }
    let split = date.split('-');
    switch (split.length) {
        case 1:
            return date;
        case 2:
            return split[1] + '/' + (isBC ? '-' : '') + split[0];
        case 3:
            // we may have received 0's
            return (split[2] !== '00' ? split[2] + '/' : '') + (split[1] !== '00' ? split[1] + '/' : '') + (isBC ? '-' : '') + split[0];
        default:
            return '';
    }
}

/*
 * CROSS-CUTTING VISUALIZATION HELPERS
 */

/**
 * open the modal to display all linked places to a given contribution
 *
 * @param id a contribution id
 * @param selectedPlace the selected place by the user if any
 */
function openPlacesModal(id, selectedPlace) {
    getContributionPlaces(id, selectedPlace).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);
        setTimeout(function () {
            $(document).trigger("reload-map")
        }, 500);
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with places modal");
        }
    });
}

/**
 * Handle displayed tab: add listeners on tabs and update url with tab id (pane url attribute)
 *
 * @param tab tab to be displayed
 * @param pov the point of vue value
 */
function handleTabs(container, tab, pov) {

    replaceHistory('pane', tab);
    replaceHistory('pov', pov);

    container.find('.contribution-pane').on('click', function () {

        replaceHistory('pane', $(this).data('pane') !== undefined ? $(this).data('pane') : $(this).prop('href').split('_')[1]);
        $(document).trigger("on-focus");
        $(document).trigger("reload-map");
        //on-focus
        let filterbar = $('.vertical_filter__opened');
        if (filterbar.exists()) {
            filterbar.removeClass("vertical_filter__opened");
            filterbar.addClass("vertical_filter");
        }

        let povBtn = $($(this).data('target') !== undefined ? $(this).data('target') : $(this).attr('href')).find('.btn-pov.active');
        if (povBtn.exists()) {
            povBtn.click();
        } else {
            replaceHistory('pov', 0);
        }

        // If it is accordion menu
        if($(this).parent().is('.card-header')){
            let that = $(this);
            let show = $(that.data('target')).hasClass('show');
            let small_pane = $(that.data('target')).height() < ($(window).height() / 2);

            setTimeout(function(){
                history.replaceState({}, document.title, window.location.toString().split('#')[0]);

                if(show || small_pane) {
                    $(window).scrollTop(0);
                } else {
                    window.location.href = "#" + that.parent().attr('id');
                }

            }, 200);
        }
    });

    container.on('click', '.btn-pov', function () {
        replaceHistory('pov', $(this).prevAll('.btn-pov').length);
    });

    // manage scroll event to keep the argument standard form visible on the top of the screen
    vizHeadScrollAnimation(container);
}

function triggerPovWhenLoaded(container, pov_element) {
    setTimeout(function () {
        if ($("#citedcontent").children().length === 0) {
            triggerPovWhenLoaded(container, pov_element);
        } else {
            pov_element.trigger('click');
        }
    }, 100);
}

/*
 * REUSABLE SEARCH
 */

function addContributionTypeahead(element, contributionId) {
    switch (contributionId) {
        case 0 :
            addActorTypeahead(element);
            break;
        case 1 :
            addDebateTypeahead(element);
            break;
        case 2 :
            addTextTypeahead(element, true);
            break;
        case 3 :
            addCitationTypeahead(element);
            break;
        case 4 :
            addArgumentTypeahead(element);
            break;
        case 6 :
            addTagTypeahead(element);
            break;
    }
}

function addSearchTypeahead(element, groupId, parent) {
    let stimeout;
    $(element).typeahead({
        hint: false,
        highlight: true,
        autoselect: false,
        minLength: 3,
        limit: MAX_TYPEAHEAD,
        parent : parent,
        maxWith : false
    }, {
        source: function (query, process) {
            if (stimeout) {
                clearTimeout(stimeout);
            }
            stimeout = setTimeout(function () {
                return searchContributions(query, groupId, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
                    return process($.makeArray(data));
                });
            }, TIMEOUT_TYPEAHEAD);
        },
        templates: {
            suggestion: function (item) {
                return contributionTypeaheadSuggestion(item);
            }
        }
    });

    $(element).on('typeahead:selected', function (obj, datum) {
        // must fill in data in field based on actual type
        redirectToActorViz()
        switch (datum.type) {
            case 'ACTOR':
                redirectToActorViz(datum.id);
                break;
            case 'DEBATE':
            case 'ARGUMENT':
                redirectToDebateViz(datum.id);
                break;
            case 'CITATION':
                redirectToTextViz(datum.textId, 1, 1);
                break;
            case 'TEXT':
                redirectToTextViz(datum.id);
                break;
            case 'TAG':
                redirectToTagViz(datum.id);
                break;
            default:
            // ignore
        }
    });
}

function contributionTypeaheadSuggestion(item, contributionType, withDetails) {
    withDetails = withDetails === undefined ? true : withDetails;

    // must put twice the force wrap, dunno why
    let container = $('<div class="mt-1"></div>');

    let containerHead = $('<div class="flex-container align-items-center"></div>').appendTo(container);
    let picture = $('<div class="tt-picture mr-2"></div>').appendTo(containerHead);

    let containerInfo = $('<div></div>').appendTo(containerHead);
    let info = $('<div class="tt-info"></div>').appendTo(containerInfo);
    let details = $('<div class="text-muted small-font mt-1"></div>').appendTo(containerInfo);
    let detailsText = "— ";

    let type = contributionType === undefined ? item.type : contributionType;
    switch (type) {
        case 'ACTOR':
            detailsText += item.headOffice != null ? item.headOffice : item.residence != null ? item.residence : "";
            picture.css("background-image", "url('" + item.defaultAvatar + "')");
            info.text(item.fullname);
            break;
        case 'DEBATE':
            if (item.tags != null)
                detailsText += stringSeparator(item.tags, "name");
        case 'ARGUMENT':
            info.text(item.fullTitle);
            break;
        case 'ARGUMENT_DICTIONARY':
            info.text(item.title);
            break;
        case 'CITATION' :
            if (item.authorsDescription != null)
                detailsText += item.authorsDescription;
            info.text(item.workingExcerpt);
            break;
        case 'TEXT':
            if (item.authors != null)
                detailsText += stringSeparator(item.authors, "fullname");
            info.text(item.title);
            break;
        case 'TAG':
            picture.html('<i class="icon_left ' + Messages("browse.search.tip.tag") + '"></i>');
            info.text(item.tagName === undefined ? item.completeName : item.tagName);
            if (item.nbTotalContributions > 0)
                detailsText += Messages('tag.composition.nbContributions', item.nbTotalContributions);
            break;
        default:
            // ignore and return empty string
            return '';
    }

    if (contributionTypeAsNum(type) > 0) {
        $('<i class="color-primary ' + Messages('browse.search.tip.' + type) + '"></i>').appendTo(picture);
    }

    if (withDetails && (typeof detailsText === 'string' || detailsText instanceof String) && detailsText.length > 2) {
        details.text(detailsText);
    } else {
        details.hide();
    }

    container.append('<hr class="hr-small">');

    return container;
}

function contributionTypeAsNum(type) {
    switch (type) {
        case 'ACTOR':
            return 0;
        case 'DEBATE':
            return 1;
        case 'ARGUMENT_CONTEXTUALIZED':
            return 3;
        case 'CITATION' :
            return 4;
        case 'TEXT':
            return 5;
        case 'TAG':
            return 6;
        default:
            return -1;
    }
}

/**
 * Execute a search request with given searchInput form. Will fill in results of request into given
 * resultDiv html container. If defined, toggle given buttons on selection of one results.
 *
 * @param searchInput the search input form to be used to build the search query
 * @param resultDiv the div panel where the result html code must be placed
 * @param toggleBtn selector to associate buttons that must be toggled when a result entry is selected (optional)
 * @param detailed boolean saying if contribution cards (more details) must be retrieved (optional, default false)
 * @param embedded boolean to say if the search is embedded in a modal frame, ie, elements will be selectable (optional)
 * @returns {Promise} the pager Object (used to re-page if post filtering is done on results)
 */
function executeSearch(searchInput, resultDiv, toggleBtn, detailed, embedded) {
    return displayResults(doSearch((searchInput instanceof jQuery ? searchInput : $(searchInput)), detailed, embedded), resultDiv, toggleBtn, embedded);
}

/**
 * Fill in given results (html) into given resultDiv html container. If defined, toggle given buttons on
 * selection of one results
 *
 * @param results the html code to be added into given result div
 * @param resultDiv the div panel where the result html code must be placed
 * @param toggleBtn selector to associate buttons that must be toggled when a result entry is selected (optional)
 * @param embedded boolean to say if the search is embedded in a modal frame, ie, elements will be selectable (optional)
 * @returns {Promise} the pager Object (used to re-page if post filtering is done on results)
 */
function displayResults(results, resultDiv, toggleBtn, embedded) {
    // start timer for long search queries
    let timeout = startWaitingModal();
    let previousFilters = $('.vertical_filter');

    return new Promise(function (resolve, reject) {
        results.done(function (html) {
            // close possible suggestions, if any
            $(resultDiv).parent().find('.tt-dropdown-menu').hide();
            $(resultDiv).empty().append(html);

            stopWaitingModal(timeout);

            if (toggleBtn !== undefined) {
                // we are in an embedded search panel
                $(toggleBtn).show();
                toggleSummaryBoxes(resultDiv, toggleBtn);
                let pageable = $(resultDiv).find('.pageable');
                pageable.addClass('pointable-content');
                pageable.prev('.instructions').show();

            } else {
                // replace query string in url bar since we are not in an embedded search panel
                let fullquery = $('#fullquery').val();
                if (fullquery !== "") {
                    let url = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('browse') + 6);
                    history.replaceState({}, document.title, url + "/" + encodeURIComponent(fullquery));
                }
            }

            // force cleanup of search query (because we maybe have been redirected from an specialised search
            $('#queryString').val('');
            resolve();

        }).fail(function () {
            // show general error div
            stopWaitingModal(timeout);
            let errorDiv = $('#error-div');
            showMe($(errorDiv), true, true);
            window.setTimeout(function () {
                showMe($(errorDiv), false, true);
            }, 6000);
            reject();
        });
    });
}

function execBeforeSearch(container, results) {
    let fullquery = $('<p></p>').html(results).find('.fullquery');

    if (fullquery.exists() && fullquery.text() !== "") {
        let url = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('browse') + 6);
        history.replaceState({}, document.title, url + "/" + encodeURI(fullquery.text()));
    }
}

/**
 * Handle toggling of summary boxes and enable/disable linked button
 *
 * @param context selector for the context to which the summary boxes must be retrieved and handled
 * @param toggleBtn selector for buttons that must be toggled when a summary box is selected
 * @param multiSelector true if the user can select multiple content
 */
function toggleSummaryBoxes(context, toggleBtn, multiSelector) {
    toggleBtn = toggleBtn == null ? null : (toggleBtn instanceof jQuery) ? toggleBtn : $(toggleBtn);
    context = (context instanceof jQuery) ? context : $(context);
    multiSelector = multiSelector === undefined ? false : multiSelector;

    let summary = context.find('.summary');
    summary.each(function () {
        // toggle 'selected' class of clicked summary (only one at a time) and toggle disabled state of toggleBtn
        $(this).on('click', function (e) {
            e.preventDefault();

            let that = $(this);

            let othersSelected = context.find('.selected');
            // was not previously selected
            if (!that.hasClass('selected')) {
                if (othersSelected.length > 0 && !multiSelector) {
                    // another one was selected and we selected a new one => do not toggle buttons
                    othersSelected.removeClass('selected');
                } else if(toggleBtn != null){
                    // no one was selected => toggle buttons
                    toggleBtn.each(function () {
                        $(this).prop('disabled', !$(this).prop('disabled'));
                    });
                }
            } else {
                // unselected this one => toggle buttons
                if (toggleBtn != null && othersSelected.length === 0) {
                    toggleBtn.each(function () {
                        $(this).prop('disabled', !$(this).prop('disabled'));
                    });
                }
            }
            that.toggleClass('selected');
        });
    });
}

/**
 * Image preview and file input (thanks to http://bootsnipp.com/snippets/featured/input-file-popover-preview-image)
 *
 * @param subdiv the subdiv pointing either on person or organization
 * @param path the path where the picture must be retrieved from
 */
function managePictureField(subdiv, path) {
    // Create the close button
    let closebtn = $('<button/>', {
        type: 'button',
        text: 'x',
        id: 'close-preview',
        style: 'font-size: initial;'
    });

    closebtn.attr('class', 'close pull-right');
    // Set the popover default content
    $(subdiv + ' .file-input-group').popover({
        trigger: 'manual',
        html: true,
        title: Messages("general.label.pic.preview") + $(closebtn)[0].outerHTML,
        content: Messages("general.label.pic.noimage"),
        placement: 'top'
    });

    $(subdiv).find('#close-preview').on('click', function () {
        $(subdiv + ' .file-input-group').popover('hide');
        // Hover before close the preview
        $(subdiv + ' .file-input-group').hover(
            function () {
                $(subdiv + ' .file-input-group').popover('show');
            },
            function () {
                $(subdiv + ' .file-input-group').popover('hide');
            }
        );
    });

    // Clear event
    $(subdiv + ' .file-input-clear').on('click', function () {
        $(subdiv + ' .file-input-group').attr("data-content", '').popover('hide');
        $(subdiv + ' .file-input-group-filename').val('');
        $(subdiv + ' .file-input-clear').hide();
        $(subdiv + ' .file-input-group-input input:file').val('');
    });

    // Create the preview image
    $(subdiv + ' .file-input-group-input').find('input:file').on('change', function () {
        let timeout = setTimeout(function () {
            $('#wait-for-it').modal('show');
        }, 1000);

        let img = $('<img/>').addClass('avatar');
        let file = this.files[0];
        if (file.size > 5242880) {
            clearTimeout(timeout);
            hideAndDestroyModal('#wait-for-it');
            slideMessage($('#error-image'));
        } else {
            // Set preview image into the popover data-content if content looks appropriate
            checkOffensivePicture($(subdiv).find('form').length > 0 ? $(subdiv).find('form') : $(subdiv).closest('form')).done(function () {
                let reader = new FileReader();
                reader.onload = function (e) {
                    clearTimeout(timeout);
                    hideAndDestroyModal('#wait-for-it');

                    $(subdiv + ' .file-input-clear').show();
                    $(subdiv + ' .file-input-group-filename').val(file.name);
                    img.attr('src', e.target.result);
                    $(subdiv + ' .file-input-group').attr('data-content', $(img)[0].outerHTML).popover('show');
                };
                reader.readAsDataURL(file);

            }).fail(function (xhr) {
                clearTimeout(timeout);
                hideAndDestroyModal('#wait-for-it');
                showErrorMessage(xhr);
            });
        }
    });

    $(document).on('click', '.popover .close', function () {
        $(this).parents(".popover").popover('hide');
    });

    $(subdiv + ' .file-input-group-filename').on('click', function () {
        $(this).parent().popover('toggle');
    });

    // if elements are pre-filled (update), manage picture and preview
    loadExistingPicture(subdiv, path);
}

/**
 * Load and display an image in the popover for an actor
 *
 * @param subdiv the actual subdiv where the picture field is
 * @param path the path where the picture must be retrieved from
 */
function loadExistingPicture(subdiv, path) {
    let file = $(subdiv + ' .avatar-field').val();
    if (file !== undefined && file !== '') {
        let img = $('<img/>').addClass('avatar');
        let id = $('#id').val();
        // check where we have to find the avatar image, since the form may be re-loaded in error
        img.attr('src', (id !== -1 && file.indexOf(id) === 0 ? path : '/tmp/') + file);
        $(subdiv + ' .file-input-group').attr("data-content", $(img)[0].outerHTML).popover("show");
    } else {
        $(subdiv + ' .file-input-clear').hide();
    }
}

/**
 * Dispatching function to get a modal page from given ajax call and add it to given html anchor
 *
 * @param callme a function to call
 * @param anchor the html anchor to replace the content with the result of given function call
 * @param params all params needed by callme
 */
function getModalFromFunction(callme, anchor, ...params) {
    callme(...params).done(function (modal) {
        loadAndShowModal(anchor, modal);
    }).fail(function (xhr) {
        if (xhr.status === 400) {
            showErrorMessage(xhr);
        } else {
            // error -> full rebuild
            replaceContent('body', xhr.responseText, 'body');
        }
    })
}

/**
 * Remove all accents and set to lower case this string
 * @returns {string} this string where all strings have been replaced and all characters set to lower case
 */
String.prototype.removeAccents = function () {
    return this
        .replace(/[áàãâä]/gi, "a")
        .replace(/[éè¨ê]/gi, "e")
        .replace(/[íìïî]/gi, "i")
        .replace(/[óòöôõ]/gi, "o")
        .replace(/[úùüû]/gi, "u")
        .replace(/[ç]/gi, "c")
        .replace(/[ñ]/gi, "n")
        .replace(/[^a-zA-Z0-9]/g, " ")
        .toLowerCase();
};

/**
 * Remove all blank spaces
 * @returns {string} this string where all blank spaces are removed
 */
String.prototype.removeSpaces = function () {
    return this
        .replace(" ", "")
};

function getUrlParam(param) {
    let vars = {};
    window.location.href.replace(location.hash, '').replace(
        /[?&]+([^=&]+)=?([^&]*)?/gi, // regexp
        function (m, key, value) { // callback
            vars[key] = value !== undefined ? value : '';
        }
    );

    if (param) {
        return vars[param] ? vars[param] : null;
    }
    return vars;
}

function getAutoCreatedModals() {
    autoCreateModalsFunctions();
    autoCreateModalsListener();
}

function autoCreateModalsFunctions() {
    // actor auto-creation if any
    getAutoCreatedActors().done(function (data, status, xhr) {
        if (xhr.status === 200) {
            manageActorModal(data, '#msg-div');
            fadeMessage();
        }
    })
    .fail(function () {
        // profession auto-creation if any
        getAutoCreatedProfessions().done(function (data, status, xhr) {
            if (xhr.status === 200) {
                $('#autocreated').html(data);
                $('#edit-profession').modal('show');
                fadeMessage();
            }
        });
    });
}

function autoCreateModalsListener() {
    $(document).on("autoCreateModalRemoved", function (event) {
        setTimeout(function(){
            autoCreateModalsFunctions();
        }, 200);
    });
}

function autoCreateModalRemoved() {
    $(document).trigger("autoCreateModalRemoved");
}


/**
 * Handle response of contribution (actor or tag) submission from modal frame, may replace modal content with new modal
 * or simply dispose it and show the message to body
 *
 * @param modal the modal frame
 * @param data the data to put in modal frame, if any
 * @param msgdiv the div where the messages (e.g. submission statuses) will be shown
 * @param managePanel the dedicated form manage panel
 */
function handleContributionModalResponse(modal, data, msgdiv, managePanel) {
    // get modal data, if any
    let modaldata = $('<div>').append(data).append('</div>');
    let content = modaldata.find('.modal-body');
    if (content.length > 0) {
        let title = modaldata.find('.modal-header');
        // replace header and body content
        $(modal).find('.modal-header').empty().append($(title).children());
        $(modal).find('.modal-body').empty().append($(content).children());
        // re-add content-dependant listeners
        managePanel(modal);
        fadeMessage();
    } else {
        // no more modals, put message on body and hide modal
        hideAndDestroyModalAndCallFunction(modal, autoCreateModalRemoved);
    }
}

function convertDate(date) {
    return date.getDate() + "/" + (date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1) + "/" + date.getFullYear();
}

/**
 * Convert string to boolean. If string is not equels to true or false return undefined
 *
 * @param string the string to convert
 * @return the converted boolean
 */
function convertStringToBoolean(string) {
    return (string === "true" ? true : (string === "false" ? false : undefined));
}

/**
 * Manage change tab for visualization (filters and dependances)
 *
 * @param getLinkedContributions function to get linked contributions in depend of the tab viz
 * @param id the id of the visualized contribution
 * @param params for each pane, reload true if the pane doesn't need to be reloaded
 * @param pov for a specific contains in pane, -1 if none
 * @param tabContainer container to manage
 * @return the converted boolean
 */
function manageChangeTab(getLinkedContributions, id, params, tabContainer) {
    loadVisualizations(getLinkedContributions, id, params, tabContainer);

    tabContainer.find('.contribution-pane').on('click', function () {
        doLoadVisualizationsCall(
            getLinkedContributions,
            id,
            $($(this).data('target') === undefined ? $(this).attr('href') : $(this).data('target')).data('tab-id'),
            undefined,
            tabContainer);
    });

    tabContainer.find('.contribution-pane-pov-load').find('.btn-pov').on('click', function () {
        doLoadVisualizationsCall(
            getLinkedContributions,
            id,
            $(this).parents('.contribution-pane-pov-load').first().data('tab-id'),
            $(this).prevAll('.btn-pov').length,
            tabContainer);
    });

    $(document).on("reload-viz", async function (event, pane, collapsable) {
        try {
            await loadVisualizations(getLinkedContributions, id, params, tabContainer);

            if ((collapsable && $(collapsable + ':visible').exists())) {
                let el = $(collapsable + ':visible').last();
                $(document).scrollTop( el.offset().top - $('.contribution-viz-header').height() - 15);
                el.children('.collapse').collapse('show');
            }
        } catch(e){

        }
    });
}

/**
 * Manage change tab for visualization V2
 *
 */
function manageChangeTab2() {
    let tabs = $("#tabs");

    $('.nav-tabs a').on('click', function () {
        let container = $("#" + $(this).attr("aria-controls").split("graphy")[0] + "content");

        $(document).trigger("close_filterbar");

        if (container.exists()) {
            if (container.hasClass('dataloader-container')) {
                container.trigger('dataloader-container-focused');
            } else {
                container.find('.dataloader-container:visible').each(function (key, element) {
                    $(element).trigger('dataloader-container-focused');
                });
            }
        }
    });

    managePOV(tabs);
}

function managePOV(tabs) {
    tabs.parents('div[id$="-viz"]').on('click', '.btn-pov', function () {
        let tabType = getTabType(tabs);
        updatePOV($(this), tabType);
    });
}

function updatePOV(btn, tabType) {
    let target = $("#" + btn.data("target"));
    let nochange = btn.data("nochange");
    let current;

    if (!btn.hasClass('btn-success')) {
        current = btn.siblings('.btn-success');
        if (!nochange) {
            btn.addClass('btn-success');
            current.removeClass('btn-success');
        }
    } else {
        current = btn;
    }

    let val = isNaN(btn.val()) ? 0 : btn.val();

    replaceHistory('pov', val);

    let staticDataSelector = "#" + tabType + "-data-display-" + val;
    let staticData = target.exists() ? target.find(staticDataSelector) : $(staticDataSelector);

    if (staticData.exists()) {

        if (staticData.find('.dataloader-container').exists()) {
            $(document).trigger("close_filterbar");
            staticData.find('.dataloader-container').trigger('dataloader-container-focused');
        }

        let currentData = $("#" + tabType + "-data-display-" + current.val());

        if (staticData.parents(".chart-container").exists()) {
            currentData.parent().parent().parent().hide();
            staticData.parent().parent().parent().show();
        } else {
            staticData.siblings().hide();
            staticData.show();
        }
    }
}

function getTabType(tabs) {
    return tabs.find(".active").children().first().attr("aria-controls").split("graphy")[0];
}

/**
 * Get the current viz and manage behavior
 *
 * @param getLinkedContributions function to get linked contributions in depend of the tab viz
 * @param id the id of the visualized contribution
 * @param params for each pane, onlyBar is true if the callback only return filterbar
 * @param pov for a specific contains in pane, -1 if none
 * @param tabContainer container to manage
 * @return the converted boolean
 */

function loadVisualizations(getLinkedContributions, id, params, tabContainer) {
    return new Promise(async (resolve, reject) => {
        if (getLinkedContributions != null) {
            params.forEach(pane => {
                let container = tabContainer.find('.viz-pane-' + pane);
                if (container.exists()) {
                    showMe(container.children(".waiting"), true, false);
                    container.children(".content").css('visibility', 'hidden');
                    container.removeClass('loaded');

                    if (container.hasClass('contribution-pane-pov-load')) {
                        let subContent = container.find('.contribution-pane-pov');
                        subContent.removeClass('loaded');
                        showMe(subContent.children(".waiting"), true, false);
                        subContent.children(".content").css('visibility', 'hidden');
                    }
                }
            });

            await doLoadVisualizationsCall(getLinkedContributions, id, params, undefined, tabContainer);
        }
        resolve();
    });
}

function doLoadVisualizationsCall(toCall, id, panes, pov, tabContainer, force) {
    force = force === undefined ? false : force;

    return new Promise((ok, error) => {

        let params = new URLSearchParams(new URL(window.location.href).search);
        let currentPov = force || pov === undefined ? params.get('pov') : pov;
        let promises = [];
        pov = pov === undefined ? currentPov : pov;

        (Array.isArray(panes) ? panes : [panes]).forEach(pane => {
            let containers = tabContainer.find('.viz-pane-' + pane);
            let containerLoadByPane = containers.hasClass('contribution-pane-pov-load');
            let containerIsLoaded = containerLoadByPane ? containers.find('.contribution-pane-pov-' + currentPov).hasClass('loaded') : containers.first().hasClass('loaded');

            if((parseInt(params.get('pane')) === parseInt(pane) || !isUserWantsSaveData()) && (!containerIsLoaded || force)) {
                if(containerLoadByPane) {
                    containers.find('.contribution-pane-pov-' + currentPov).addClass('loaded');
                } else {
                    containers.addClass('loaded');
                }

                promises.push(new Promise((resolve, reject) => {
                    toCall(id, pane, pov).done(function (data) {
                        if (data[pane]) {
                            let poved = containers.exists();
                            containers = containers.exists() ? containers : tabContainer;

                            containers.each(function (index) {
                                let container = containerLoadByPane ? $(this).find('.contribution-pane-pov-' + currentPov) : $(this);
                                showMe(container.find(".waiting").first(), false, false);

                                let content = poved ? container.find(".content").first() : container;
                                let previousScroll = content.children().length > 0 ? window.scrollY : 0;

                                content.html(data[pane].replace(/viz%code#id/g, index));
                                container.children(".content").css('visibility', 'visible');

                                $(document).scrollTop(previousScroll);

                                if (containers.length > 1) {
                                    let tabMenu = content.find('#tab');
                                    tabMenu.prop('id', 'tab-' + index);

                                    tabMenu.find('.nav-item').each(function () {
                                        $(this).prop('id', $(this).prop('id') + '-' + index);
                                        $(this).attr('href', $(this).attr('href') + '-' + index);
                                        $(this).attr('aria-controls', $(this).attr('aria-controls') + '-' + index);
                                    });

                                    let tabContent = content.find('#tab-content');
                                    tabContent.prop('id', 'tab-content-' + index);

                                    tabContent.find('.tab-pane').each(function () {
                                        $(this).prop('id', $(this).prop('id') + '-' + index);
                                        $(this).attr('aria-labelledby', $(this).attr('aria-labelledby') + '-' + index);
                                    });
                                }

                                if (poved && container.is(':visible')) {
                                    let povBtn = container.find('.btn-pov:visible').eq(getUrlParam('pov'));
                                    if (povBtn.exists()) {
                                        povBtn.click();
                                    }
                                }
                            });
                        }

                        resolve();
                    }).fail(function (jqXHR) {

                        if (jqXHR.status === 401) {
                            redirectToLogin();
                            error();
                        } else if (jqXHR.status !== 400) {
                            slideDefaultErrorMessage();
                        }

                        resolve();
                    });
                }));
            }
        });

        Promise.all(promises).then(values => {
            ok();
        });
    });
}

function isUserWantsSaveData() {
    let connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;

    if(connection){
        return connection.saveData ||
            (connection.type !== undefined && connection.type !== "ethernet" && connection.type !== "wifi" && connection.type !== "wimax" && connection.type !== "unknown" && connection.effectiveType !== "4g")

    }

    return false;
}

/**
 * Apply the change viz
 *
 * @param container the tab container
 * @param filterbars the list of tabs filterbar
 * @param triggerFilter true if filters must be triggered at the end
 * @param filterVisible true if filter must match only visible filterable, false if onyl hidden and null if the whole.
 */
function applyChangeVizTab(container, filterbars, triggerFilter, filterVisible) {
    removeFilterbar();

    let togglerFilters = container.find('.toggle_filter');
    togglerFilters.prop('disabled', false);

    let pager;
    if (container != null && container !== undefined) {
        let pagerContainer = container.parent().find('#pager');
        let pageable = container.find('.pageable');
        if (pageable !== undefined && pagerContainer !== undefined) {
            pager = new Pager(pageable, 15, container.parent().find('#pager'));
            pager.reset();
        }
    }

    handleFilter(togglerFilters, pager, container, null, null, true, filterbars[container.attr("id")], filterVisible);
    if (triggerFilter)
        $(document).trigger('open_filterbar');
}

/**
 * Remove the current mail filterbar if any
 *
 */
function removeFilterbar() {
    let container = $('#main-content');
    let filterbar = container.children('.vertical_filter');
    if (filterbar !== undefined && filterbar.exists()) {
        filterbar.removeClass('vertical_filter__opened');
        container.removeClass('opened');
        filterbar.remove();
    }
}

/**
 * Get the given url param value
 *
 * @param name the name of the param
 */
function getUrlParam(name) {
    if(name !== undefined) {

        let url = new URL(window.location.href);
        //Update or create de given param and add the related value
        return url.searchParams.get(name);
    }

    return null;
}

/**
 * Replace the url address given param with given value
 *
 * @param name the name of the param
 * @param replaceValue the value to give at the param
 */
function replaceHistory(name, replaceValue) {
    if(name !== undefined && replaceValue !== undefined) {

        let url = new URL(window.location.href);
        //Update or create de given param and add the related value
        url.searchParams.set(name, replaceValue);
        //Update the current url
        history.replaceState({}, document.title, url);
    }
}

/**
 * Remove a param value from url address
 *
 * @param name the name of the param to remove
 */
function deleteFromHistory(name) {
    let url = new URL(window.location.href);
    //Delete the given param in the current url
    url.searchParams.delete(name);
    //Update the current url
    history.replaceState({}, document.title, url);
}

/**
 * Switch font awesome icones
 *
 * @param elem the element where the icones are
 * @param icon1 the first icone to switch
 * @param icon2 the second icone to switch
 */
function changeFaIcon(elem, icon1, icon2) {
    if (elem.attr('data-icon') === icon1) {
        elem.attr('data-icon', icon2);
    } else {
        elem.attr('data-icon', icon1);
    }
}

/**
 * Manage on scroll animation event for contribution visualization.
 * Keep the contribution viz title visible on the top of the screen when scroll down.
 *
 */
function vizHeadScrollAnimation(container) {
    let viz_header = container.find('.contribution-viz-header');
    let viz_header_title = viz_header.find('.contribution-title');
    let lastTime = null;
    let lastTimeType = null;

    $(window).scroll(function () {
        let viz_header_scrolled = "viz-head-scrolled";
        let scrollTop = $(this).scrollTop();
        let thisTimeType = scrollTop < 50 && viz_header.hasClass(viz_header_scrolled) ? true :
            scrollTop >= 50 && !viz_header.hasClass(viz_header_scrolled) ? false : null;

        if(lastTime != null && Date.now() - lastTime < 2 && lastTimeType != null && thisTimeType !== lastTimeType) {
            $('html, body').animate({scrollTop: 50});
        } else {
            lastTime = Date.now();
            lastTimeType = thisTimeType;

            if (thisTimeType) {
                viz_header.removeClass(viz_header_scrolled);
                viz_header_title.text(viz_header_title.attr('title'));
            } else if (thisTimeType === false) {
                viz_header.addClass(viz_header_scrolled);
                viz_header_title.trunk8({lines: 3});
            }
        }
    });
}


/**
 * Build both charts (displayed one and exportable/hidden one)
 *
 * param chartData the data needed to create the chart
 * param label the label of the y axe
 */
function initSocioChart(id, chartData, label) {
    let graph = '#socio-data-display-' + id;
    let content = $('#socio-data-content-' + id);

    doInitSocioChart(chartData, label, $(graph).parent(), $(graph).clone(), graph, content);
}

/**
 * Build both charts (displayed one and exportable/hidden one) and reload when filtered
 *
 * param chartData the data needed to create the chart
 * param label the label of the y axe
 * param graphContainer the container the contains the graph
 * param graphCopy the copy of the graph canvas to reset it when reload
 * param graphName the id of the graph
 * param content the data needed to build the graph
 */
function doInitSocioChart(chartData, label, graphContainer, graphCopy, graphName, content) {
    let categories = [];
    graphContainer.empty();
    graphContainer.append(graphCopy.clone());

    makeCategories(content, categories, chartData, label);
    makeSocioGraph(graphName, categories, chartData);

    $(document).on('change-filter', function () {
        doInitSocioChart(chartData, label, graphContainer, graphCopy, graphName, content);
    });
}

/**
 * Fill in the categories for the socio graph
 *
 * param content the container where data are
 * param categories a dictionary of categories to be used as graph labels
 * param chartData data to be shown
 * param label the label of the y axe
 */
function makeCategories(content, categories, chartData, label) {
    let values;

    chartData.labels = [];
    for (let i = 0; i < chartData.datasets.length; i++) {
        chartData.datasets[i].data = [];
    }

    content.find('.actor-thumbnail:not(.filtered)').each(function () {
        categories.push($(this).prop("outerHTML"));

        let value = 0;
        values = $(this).find('span').text().split(',');
        for (let i = 0; i < chartData.datasets.length; i++) {
            value += parseInt(values[i]);
            chartData.datasets[i].data.push(parseInt(values[i]));
        }

        chartData.labels.push(value + " " + label);
    });
}

/**
 * Create the sociography graph to happen to given graph div with given categories
 *
 * param graphName the graph container id
 * param categories the categories of the graph (names to be shown in front of each lines)
 * param data the data to show
 */
function makeSocioGraph(graphName, categories, data) {
    // Make label
    if (categories.length === 0) {
        $(graphName).hide();
        $(graphName).closest(".chart-container").find(".no-socio").show();
    } else {
        let graph = $(graphName);
        graph.show();
        $("#no-socio").hide();
        let container = graph.parent().siblings(".chart-labels");
        container.empty();
        for (let i in categories) {
            container.append(categories[i]);
        }
        // Make graph
        graph.prop("height", 70 + 40 * categories.length);
        let width = graph.parent().parent().parent().width() - 50;
        let labelWidth = container.width();
        graph.prop("width", (width - labelWidth < 700 ? 700 : width - labelWidth));
        makeHorizontalBarGraph(graph, data, null, {display: true});
    }
}

/**
 * Create a horizontal bar graph
 *
 * param graph the graph canvas
 * param data the data to show
 * param title the title param
 * param legend the legend param
 */
function makeHorizontalBarGraph(graph, data, title, legend) {
    let ctx = graph[0].getContext('2d');
    window.myBar = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: data.labels,
            datasets: [{
                label: data.datasets[0].label,
                data: data.datasets[0].data,
                backgroundColor: data.datasets[0].backgroundColor
            }, {
                label: data.datasets[1].label,
                data: data.datasets[1].data,
                backgroundColor: data.datasets[1].backgroundColor
            }, {
                label: data.datasets[2].label,
                data: data.datasets[2].data,
                backgroundColor: data.datasets[2].backgroundColor
            }]
        },
        options: {
            responsive: false,
            maintainAspectRatio: false,
            tooltips: {
                callbacks: {
                    title: function (tooltipItem, data) {
                        return "";
                    },
                    label: function (tooltipItem, data) {
                        return tooltipItem.xLabel > 0 ? (tooltipItem.xLabel + " " + data.datasets[tooltipItem.datasetIndex].label) : "";
                    }
                }
            },
            scales: {
                xAxes: [{
                    stacked: true,
                    position: 'top',
                    ticks: {
                        beginAtZero: true,
                        stepSize: 1
                    }
                }],
                yAxes: [{
                    stacked: true,
                    position: 'right',
                    barThickness: 20,
                    gridLines: {
                        display: false
                    }
                }]
            },
            title: title,
            legend: legend
        }
    });
}

/**
 * Start timer for long of the operation to show waiting modal
 *
 * return the timeout to stop the timer
 */
function startWaitingModal() {
    // start timer for long search queries
    return setTimeout(function () {
        $('#wait-for-it').modal('show');
    }, 10);
}

/**
 * Stop waiting modal
 *
 * param the current timer to stop
 */
function stopWaitingModal(timeout) {
    clearTimeout(timeout);
    setTimeout(function(){
        $('#wait-for-it').modal('hide');
    }, 50);
}

function findGetParameter(parameterName) {
    let result = null,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}

/**
 * Check if an given object if empty or not
 *
 * @param obj the object to check
 */
function isEmpty(obj) {
    for (let key in obj) {
        if (hasOwnProperty.call(obj, key)) return false;
    }

    return true;
}

function loadDataAsync(toCall, params, postTreat) {
    toCall(params,)
}


/*
 * various helper functions
 */

/**
 * Check whether given value exists in given array
 *
 * @param value a value
 * @param array an array
 * @returns {boolean}
 */
function isInArray(value, array) {
    return array.indexOf(value) > -1;
}


/**
 * Remove given value from given array
 *
 * @param value a value to remove
 * @param array an array
 * @returns {*}
 */
function removeValueFromArray(value, array) {
    let index = array.indexOf(value);
    if (index > -1) {
        array.splice(index, 1);
    }
    return array;
}


/**
 * Determine if a variable is a int
 *
 * @param node the node to determine
 * @return true if it is
 */
function isInt(value) {
    return !isNaN(value) &&
        parseInt(Number(value)) === value &&
        !isNaN(parseInt(value, 10));
}


function createContributionOptions(vizOptions, editOpitons, addOptions) {
    return createContributionOption("fa-eye", vizOptions, "general.btn.contribution.see.tooltip", "")
        + createContributionOption("fa-pencil-alt", editOpitons, "general.btn.contribution.edit.tooltip", '')
        + createContributionOption("fa-plus", addOptions, "general.btn.contribution.add.tooltip", 'padding-right:0;');
}

/**
 *
 *
 * @param
 */
function createContributionOption(faType, options, tooltip, style) {
    return '<div class="dropdown" style="margin: 0;padding: 0;"><button type="button" ' +
        'class="btn primary btn-link btn-simple-link tree_node_option dropdown-toggle" ' +
        'style="font-size : 16px; ' + style + '" data-toggle="dropdown" title="' + Messages(tooltip) + '">' +
        '<i class="fas ' + faType + '"></i></span></button>' + options + '</div>';
}

function createContributionOptionContextMenuBtn(id, title, fa, tooltip, nodeClass) {
    nodeClass = nodeClass || '';
    return '<li><button class="btn btn-link primary ' + id + '" title="' + Messages("empty") + '">' +
        '<span class="' + nodeClass + '"><span class="fas ' + fa + ' fixed-size"></span>&nbsp' + Messages(title) + '</span></button></li>';
}

function getContributionOptionData(option, data) {
    let val = option.data(data) !== undefined ? option.data(data) : option.parents(".contribution-container").data(data);
    return val === 0 || val ? val : -1;
}

/**
 * Trigger an event from anywhere to ask to reload viz or tree
 *
 * @param collapsable a collapse container to show
 * @param reloadDragnDrop true if we only need to reload the dragndrop
 * @param pane a specific pane to reload
 */
function triggerReloadVizEvent(collapsable, reloadDragnDrop, pane) {
    if(reloadDragnDrop === true){
        $(document).trigger('dragndrop-reload', [true]);
    } else {
        $(document).trigger("reload-viz",
            [pane === undefined ? undefined : [pane],
            collapsable === undefined ? undefined : collapsable]);
    }
}

function destroyTypeahead(elements) {
    console.log(elements);
    elements.each(function(){
        console.log($(this));
        console.log($(this).children('input'));
        $(this).replaceWith($(this).children('input'));
        console.log($(this));
    });
}

/**
 * EXPANDABLE ZONE
 */

function initExpandableZone() {

    centerAllContents();

    $(".expandable-btn-left").on('click', function (e) {
        doSideChange($(this), true);
    });

    $(".expandable-btn-right").on('click', function (e) {
        doSideChange($(this), false);
    });

    $(".expandable-btn-middle").on('click', function (e) {
        centerContents($(this).parents(".expandable-container"));
        $(document).trigger("reload-annotated-text");
    });
}

function doSideChange(container, toLeft) {
    let parent = container.parents(".expandable-container");
    let zone1 = toLeft ? parent.find(".expandable-left") : parent.find(".expandable-right");
    let zone2 = toLeft ? parent.find(".expandable-right") : parent.find(".expandable-left");
    zone1.css("width", "0");
    if (toLeft) zone1.css("visibility", "hidden");
    zone2.css("width", "100%");
    zone2.css("visibility", "visible");
    $(document).trigger("reload-annotated-text");
}

function centerAllContents() {
    let container = $(".expandable-container");
    container.each(function () {
        centerContents($(this));
    });
}

function centerContents(container) {
    let children = container.find('.expandable-zone');
    let child_width = (container.width() - 10) / children.length;

    children.each(function () {
        $(this).width(child_width);
        $(this).css("visibility", "visible");
    });
}

function togglexxs() {
    $(".hidden-xxs").each(function () {
        if ($(this).width() < 300) {
            $(this).hide();
        } else {
            $(this).show();
        }
    });
}

function treatSaveLinkFail(modal, status) {
    hideAndDestroyModal(modal);
    switch (status) {
        case 400:
            slideMessage($('#warning-link-exists'));
            break;
        default :
            slideMessage($('#error-save'));
    }
}

function convertTextAnnotationModalWidth(modal, half) {
    half = half || false;
    let width = modal.width() * 0.9;
    return half ? width / 2 : width - 102;
}

function initContainerPagers(container, perPage) {
    perPage = perPage || 5;

    container.find(".pageable").each(function (key, element) {
        element = $(element);

        new Pager(element, perPage,
            element.next().hasClass("pager-container") ? element.next() : createPagerContainer(element)[0],
            undefined, undefined, false, 2).reset();
    });
}

function createPagerContainer(container) {
    let r = [];
    r[0] = $('<div class="col-12 flex-container pager-container"></div>').insertAfter(container);
    r[1] = $('<div class="pager pagination"></div>').appendTo(r[0]);
    return r;
}

function toggleFollowedGroups(container) {
    // toggle groups by followed state
    container.find('#toggle-follow-group').on('click', function (e) {
        container.find('.notfollowed-group').toggle();
        $(this).find('span').toggle();
    });
}

function makeTextContentReadable(content, nbParagraph) {
    nbParagraph = nbParagraph || 3;
    let response = "";
    let sentences = content != null ? content.split(".") : "";

    for (let iSentence = 1; iSentence < sentences.length; iSentence++) {
        response += sentences[iSentence - 1].trim() + ". ";
        if (iSentence % nbParagraph === 0) {
            response += "\n\n";
        }
    }

    return response;
}

function showHideInputPassword(input) {
    let formGroup = input.parents('.form-group');

    let label = formGroup.find('label');
    label.parent().addClass("d-flex");
    label.addClass("flex-grow-1");

    let button = $('<button class="btn btn-link primary"><i class="fas fa-eye"></i></button>');
    label.after(button);

    button.on('focus', function(e) {
        button.blur();
        input.focus();
    });

    input.on('keydown', function(e) {
        if (e.which === 13) {
            e.preventDefault();
        }
    });

    button.on('click', function(e) {
        e.preventDefault();

        $(this).find('i').toggleClass("fa-eye fa-eye-slash");

        if (input.attr("type") === "password") {
            input.attr("type", "text");
        } else {
            input.attr("type", "password");
        }
    });
}

/**
 * Extract a date from a given string.
 *
 * @param text the string.
 */
function extractDateFromString(text) {
    let date = new Date(text);

    if (isNaN(date.getTime())) {

        let months = {
            "jan": "01",
            "jan.": "01",
            "janvier": "01",
            "january": "01",
            "januari": "01",
            "fev": "02",
            "fev.": "02",
            "feb": "02",
            "février": "02",
            "february": "02",
            "februari": "02",
            "mar.": "03",
            "mar": "03",
            "mars": "03",
            "march": "03",
            "maart": "03",
            "avr": "04",
            "avr.": "04",
            "apr": "04",
            "avril": "04",
            "april": "04",
            "mai.": "05",
            "mai": "05",
            "may": "05",
            "mei": "05",
            "jun.": "06",
            "jun": "06",
            "juin": "06",
            "june": "06",
            "juni": "06",
            "jul.": "07",
            "jul": "07",
            "juillet": "07",
            "july": "07",
            "juli": "07",
            "aou.": "08",
            "aou": "08",
            "aug": "08",
            "août": "08",
            "august": "08",
            "augustus": "08",
            "sep": "09",
            "sep.": "09",
            "septembre": "09",
            "september": "09",
            "oct.": "10",
            "oct": "10",
            "okt": "10",
            "octobre": "10",
            "october": "10",
            "oktober": "10",
            "nov": "11",
            "nov.": "11",
            "novembre": "11",
            "november": "11",
            "dec.": "12",
            "dec": "12",
            "decembre": "12",
            "december": "12"
        };

        let patternMonth = new RegExp('( )(' + getTabAsPattern(months) + ')( )', 'i');
        let match = patternMonth.exec(text);

        if (match != null) {
            let patternDay = new RegExp('0[1-9]|[12][0-9]|3[01]', 'i');
            let patternYear = new RegExp('[12][0-9]{3}', 'i');

            let splitted = text.split(" ");
            let monthName = match[0].toLowerCase().trim();
            let indexOfMonth = text.substr(0, match.index + monthName.length).split(" ").length - 1;
            // search day
            let day = matchPatternInStrings(splitted, patternDay, indexOfMonth);
            // search year
            let year = matchPatternInStrings(splitted, patternYear, indexOfMonth);

            if (day != null && year != null) {
                return day + "/" + months[monthName] + "/" + year;
            }
        }
    } else {
        let month = parseInt(date.getMonth()) + 1;
        return date.getDate() + "/" + (month < 10 ? '0' + month : month) + "/" + date.getFullYear()
    }
    return null;
}

/**
 * Get a list indexed array of strings into a regex pattern.
 *
 * @param tab the indexed array
 */
function getTabAsPattern(tab) {
    let ch = "";
    Object.keys(tab).map((e, i) => ch += e + "|");
    return ch.substring(0, ch.length - 1);
}

/**
 * Match a given pattern in a list of strings.
 *
 * @param strings the list of strings
 * @param pattern the pattern to match
 * @param indexOfMonth the index where the month has been discovered.
 */
function matchPatternInStrings(strings, pattern, indexOfMonth) {
    if (indexOfMonth < strings.length) {
        let i = indexOfMonth;
        while (i >= 0) {
            let match = pattern.exec(strings[i]);
            if (match != null) return strings[i];
            i--;
        }

        i = indexOfMonth;
        while (i < strings.length) {
            let match = pattern.exec(strings[i]);
            if (match != null) return strings[i];
            i++;
        }
    }
    return null;
}

function checkIfObjectIsEmpty(obj) {
    for (let key in obj) {
        if (obj.hasOwnProperty(key))
            return false;
    }
    return true;
}

function elementIsVisible(element, maxCheck) {
    maxCheck = isNaN(maxCheck) ? 10 : maxCheck;

    let iCheck = 0;
    while (iCheck < maxCheck++ && element.exists() && !element.is('body')) {
        if (element.css('display') === 'none') {
            return false;
        }
        element = element.parent();
    }

    return true;
}

/**
 * Get some linked contribution data async
 *
 * @param container the container where the btn that will trigger the call is
 * @param btn that trigger the call event
 * @param toCall the function to call async to get that data
 */
function getLinkedDataAsync(container, btn, toCall) {
    $(container).on('click', btn, function () {
        let that = $(this);
        if (!that.hasClass("loaded")) {
            let target = $(that.data("target"));
            let waitForIt = that.siblings('.wait-spinner').show();

            toCall(that.data("id")).done(function (data) {
                that.addClass("loaded");
                waitForIt.hide();
                target.html(data);
            }).fail(function () {
                console.log("Error with " + toCall);
            });
        }
    });
}

function generateHashcode(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        let character = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + character;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
}

/**
 * Get the image modal for the given image element
 *
 * @param img the image element
 */
function getImageModal(img) {
    img.on('click', function () {
        imageModal($(this).prop("src")).done(function (html) {
            loadAndShowModal($('#modal-anchor'), html);
        }).fail(function () {
            console.log("Image modal error");
        });
    });
}

function stringSeparator(array, fieldName) {
    let ch = "";
    array.forEach(function (element) {
        ch += (fieldName !== undefined ? element[fieldName] : element) + ", ";
    });
    return ch.substr(0, ch.length - 2);
}

function getContributionSuggestions(container, contribution) {

    let suggestions = container.find('.contribution-suggestions');

    if(suggestions.exists()) {
        getSuggestionsContribution(contribution).done(function (html) {
            suggestions.html(html);

            hideSuggestionContainerIfNecessary(suggestions);

            $(window).resize(function () {
                setTimeout(function(){
                    hideSuggestionContainerIfNecessary(suggestions);
                }, 50);
            });

            container.click('.contribution-pane', function () {
                setTimeout(function(){
                    hideSuggestionContainerIfNecessary(suggestions);
                }, 300);
            });

        }).fail(function (jqXHR) {
            console.log("Error with contribution suggestions.");
        });
    }
}
function hideSuggestionContainerIfNecessary(container) {

    container.each(function(){
        $(this).css('visibility', $(this).width() < 500 ? 'hidden' : 'visible');
    });

}

/*
 * WORKERS
 */

function callWorkers(workerName, datum, datumJson, commonDatum, commonDatumJson, toCallIfNotSupported, withWorker, callback, limit) {
    return new Promise(resolve => {
        if (Array.isArray(datum) && datum.length > 0) {
            if (typeof (Worker) !== "undefined" && withWorker) {
                // Worker supported
                let promises = [];
                let separatedDatum = [];
                let separatedDatumJson = [];

                for (let limited = 0; limited < datum.length; limited += limit) {
                    separatedDatum.push(datum.slice(limited, limited + limit - 1));
                    separatedDatumJson.push(datumJson.slice(limited, limited + limit - 1));
                }

                for (let i in separatedDatum) {
                    promises.push(callWorkersResult(separatedDatum[i], separatedDatumJson[i], commonDatum, commonDatumJson, workerName, callback));
                }

                Promise.all(promises).then(function () {
                    resolve(true);
                });
            } else if (toCallIfNotSupported !== undefined) {
                datum.forEach(function (data) {
                    toCallIfNotSupported(data, commonDatum);
                });
                resolve(true);
            } else {
                resolve(false);
            }
        } else {
            resolve(false);
        }
    });
}

function callWorkersResult(separatedDatum, separatedDatumJson, commonDatum, commonDatumJson, workerName, callback) {
    return new Promise(resolve => {
        let w = new Worker(workerName);

        w.postMessage(JSON.stringify({datum: separatedDatumJson, commonDatum: commonDatumJson}));
        w.onmessage = function (e) {
            if (callback !== undefined) {
                e.data === undefined ? callback() : callback(separatedDatum, commonDatum, e.data);
            }
            w.terminate();
            w = undefined;
            resolve(true);
        };
    });
}

function removeModalBackdrop(modal) {
    modal.find('input[name="cancel"]').on('click', function () {
        doRemoveModalBackdrop();
    });
}

function doRemoveModalBackdrop() {
    $('.modal-backdrop.show').remove();
}

function checkIframeLoading(iframe, siteUrl, siteAlias) {
    try {
        let iframeDoc = iframe[0].contentDocument || iframe[0].contentWindow.document;

        // Check if error
        if ( iframeDoc.readyState  !== 'complete') {
            displayIframeError();
        }
    } catch (e) {
        if (e instanceof DOMException) {
            //displayIframeError();
        } else {
            displayIframeError();
        }
    }

    function displayIframeError() {
        console.log('iframe can t be loaded');
        iframe.replaceWith($('<span class="iframe-replacement"><span>' + Messages('text.onlyonsource', siteUrl, siteAlias && siteAlias.length > 0 ? siteAlias : 'source') + '</span></div>'));
    }
}

function inputWithMaxLength(input, maxLength) {
    doInputWithMaxLengthAction();

    input.on('keyup mouseup', function() {
        doInputWithMaxLengthAction();
    });

    function doInputWithMaxLengthAction(){
        let info = input.parent().find('.input_length_info');

        if(!info.exists()) {
            info = $('<div class="input_length_info"></div>').insertAfter(input);
        }

        info.toggleClass('input_length_hmax', input.val().length > maxLength);
        info.text(input.val().length + '/' + maxLength);

        input.addClass('is-invalid');
    }
}

function updatePictureField(field, toUpdate){
    let ch = field.val() ? field.val().split('.') : null;
    toUpdate.val(ch != null && ch.length > 1 ? '.' + ch[ch.length - 1] : "");
}

function initShowCitationsListener(){
    $(document).on("click", ".show-citations", function(){
        let modalAnchor = $('#modal-anchor');

       if($(this).data('context') !== undefined){

           if($(this).data('actor') !== undefined){
               getActorCitationInContext($(this).data('context'), $(this).data('actor')).done(function(data){
                   loadAndShowModal(modalAnchor, data);
               }).fail(function(){
                   stopWaitingModal();
                   console.log('Error in show citations from context for actor modal');
               });
           }
           else if($(this).data('text') !== undefined){
               getTextCitationInContext($(this).data('context'), $(this).data('text')).done(function(data){
                   loadAndShowModal(modalAnchor, data);
               }).fail(function(){
                   stopWaitingModal();
                   console.log('Error in show citations from context for text modal');
               });
           }
           else if($(this).data('key') !== undefined){
               getSociographyCitationInContext($(this).data('context'), $(this).data('sub-context'), $(this).data('key'), $(this).data('value'), $(this).data('amount') > 0 ? $(this).data('shade') : undefined, $(this).data('type')).done(function(data){
                   loadAndShowModal(modalAnchor, data);
                   checkNavPills(modalAnchor);
                   citationOptionsHandler(modalAnchor.children('#modal-citations'), true);
               }).fail(function(){
                   stopWaitingModal();
                   console.log('Error in show citations from context for sociography modal');
               });
           }
       } else {
           getActorTextCitations($(this).data('id'), $(this).data('text')).done(function(data){
               loadAndShowModal(modalAnchor, data);
           }).fail(function(){
               stopWaitingModal();
               console.log('Error in show citations from actor modal');
           });
       }
    });
}

function checkNavPills(container) {
    container.find('.nav-pills').each(function() {
        let items = $(this).find('.nav-item.active');

        if(items.length > 1) {
            items.removeClass('active');
            items.first().addClass('active');
        }
    });
}

function initContributionListeners(container, isModal) {
    actorOptionsHandler(container, isModal, true);
    debateOptionsHandler(container, isModal, true);
    textOptionsHandler(container, isModal, true);
    tagOptionsHandler(container, isModal, true);
    tagCategoryOptionsHandler(container, isModal, true);
    argumentOptionsHandler(container, isModal, true);
    citationOptionsHandler(container, isModal, true);
    contributionOptionsHandler(container, isModal, true);
}

function contributionOptionsHandler(container, isModal) {
    let modalanchor = $('#merge-modal-anchor');

    // See all citations from a tag
    let seeCitationsBtn = container.find('.citation-see-citations-btn');
    seeCitationsBtn.off('click');
    seeCitationsBtn.on('click', function() {
        let id = getContributionOptionData($(this), "id");

        getFindContributionsModal(id).done(function(html){
            loadAndShowModal(modalanchor, html);
            initFindContributionsModal(modalanchor, id, container.data('id'), 3, container.data('role'), container.data('pane'));
        }).fail(() => stopWaitingModal());
    });

    // See all debates from a tag
    let seeDebatesBtn = container.find('.citation-see-debates-btn');
    seeDebatesBtn.off('click');
    seeDebatesBtn.on('click', function() {
        let id = getContributionOptionData($(this), "id");

        getFindContributionsModal(id).done(function(html){
            loadAndShowModal(modalanchor, html);
            debateOptionsHandler(modalanchor, true);
            initFindContributionsModal(modalanchor, id, container.data('id'), 1, container.data('role'), container.data('pane'));
        }).fail(() => stopWaitingModal());
    });
}

function initFindContributionsModal(modalanchor, contributionId, forContributionId, forContributionType, contributionRole, pane) {

    initContributionsScroller([contributionId, forContributionId, forContributionType, contributionRole, pane],
        modalanchor.find('#modal-async-contributions').find('.scroller-container'),
        findContributions,
        {
            toExecAfter : forContributionType === 1 ? debateOptionsHandler : citationOptionsHandler,
            isModal : true,
            step : forContributionType === 1 ? 10 : 6
        });
}

function  initContributionsScroller(params, container, toCall, options) {

    if( container.find('.results-container').exists()) {
        container.scrollerPager(
            params,
            toCall,
            options
        );
    }

}

function logoutFromGoogle() {
    let auth2 = gapi.auth2.getAuthInstance();

    auth2.signOut().then(function () {
        logoutAsync().done(function(redirect) {
            console.log('User signed out.');
            window.location.href = JSON.parse(redirect);
        }).fail(function(){
            console.log('Logout async failed...');
        });
    });
}

function logoutFromFacebook() {
    FB.logout(function(response) {

    });

    logoutAsync().done(function(redirect) {
        console.log('User signed out.');
        window.location.href = JSON.parse(redirect);
    }).fail(function(){
        console.log('Logout async failed...');
    });
}

function treatFacebookOpenIdAuthentication(facebookUser, type, displayError) {
    if (facebookUser.status === 'connected') {   // Logged into your webpage and Facebook.
        FB.api('/me', function(response) {
            let profile = {};

            profile.id = response.id;
            profile.pseudo = response.name;
            profile.token= facebookUser.authResponse.accessToken;

            openIdAuthentication(profile, type);
        });

    } else if(displayError) {              // Not logged into your webpage or we are unable to tell.
        //slideDefaultErrorMessage();
    }
}

function treatGoogleOpenIdAuthentication(googleUser, type) {
    let profile = {};
    let profileOpenId = googleUser.getBasicProfile();

    profile.id = profileOpenId.getId();
    profile.email = profileOpenId.getEmail();
    profile.pseudo = profileOpenId.getName();
    profile.token = googleUser.getAuthResponse().id_token;

    openIdAuthentication(profile, type);
}

function openIdAuthentication(profile, type) {
    let userId = $('#user-id');

    if(!userId.val() || userId.val() < 0) {
        let profileForm = '';

        profileForm += 'id=' + profile.id;
        profileForm += '&email=' + profile.email;
        profileForm += '&pseudo=' + profile.pseudo;
        profileForm += '&token=' + profile.token;
        profileForm += '&type=' + type;

        authenticateWithOpenId(profileForm).done(function (redirect) {
            slideDefaultSuccessMessage();
            window.location.href = JSON.parse(redirect);
        }).fail(function () {
            logoutFromGoogle();
            logoutFromFacebook();
            slideDefaultErrorMessage();
        });
    }
}

function revokeGoogleAccess(accessToken) {
    // Google's OAuth 2.0 endpoint for revoking access tokens.
    var revokeTokenEndpoint = 'https://oauth2.googleapis.com/revoke';

    // Create <form> element to use to POST data to the OAuth 2.0 endpoint.
    var form = document.createElement('form');
    form.setAttribute('method', 'post');
    form.setAttribute('action', revokeTokenEndpoint);

    // Add access token to the form so it is set as value of 'token' parameter.
    // This corresponds to the sample curl request, where the URL is:
    //      https://oauth2.googleapis.com/revoke?token={token}
    var tokenField = document.createElement('input');
    tokenField.setAttribute('type', 'hidden');
    tokenField.setAttribute('name', 'token');
    tokenField.setAttribute('value', accessToken);
    form.appendChild(tokenField);

    // Add form to page and submit it to actually revoke the token.
    document.body.appendChild(form);
    form.submit();


    logoutAsync().done(function(redirect) {
        window.location.href = JSON.parse(redirect);
    }).fail(function(){
        console.log('Logout async failed...');
    });

}

function initImportInGroupModal(modal) {
    let contributionId = modal.find('#contribution-id').val();
    let contributionTypeId = modal.find('#contribution-type-id').val();
    let importId = modal.find('#import-id');

    let div = modal.find('#select-group');
    let boxes = div.find('input');
    manageExclusiveCheckboxes(boxes, importId, true);

    let confirm = modal.find('#addto-group');
    boxes.on('click', function () {
        confirm.prop('disabled', false);
    });

    confirm.on('click', function () {
        // send request to add this contribution into selected group
        addContributionToGroup(div.find('input[type="checkbox"]:checked').val(), contributionId, contributionTypeId).done(function (data) {
            hideAndDestroyModal(modal);
            slideDefaultSuccessMessage();
        }).fail(function (jqXHR) {
            hideAndDestroyModal(modal);
            slideDefaultErrorMessage();
        });
    });

    toggleFollowedGroups(modal);
}

function dragndropListeners(container, argumentContainerSelector, contextId, classify, toCall, pane, options, fix){
    let argumentContainer = container.find(argumentContainerSelector);

    let dragndropOpen = argumentContainer.find('.context-dragndrop-btn');
    dragndropOpen.off('click');
    dragndropOpen.on('click', function(){
        if(fix) {
            argumentContainer.find('.content').show();
            argumentContainer.find('.contributions-list').hide();

            $()
        }
        loadDragnDrop(container, argumentContainerSelector, getContributionOptionData($(this), "id"), toCall, pane, options, fix);
    });

    let dragndropClose = argumentContainer.find('.context-dragndrop-close-btn');
    dragndropClose.off('click');
    dragndropClose.on('click', function(){
        if(fix) {
            argumentContainer.find('.content').hide();
            argumentContainer.find('.contributions-list').show();
        }
        triggerReloadVizEvent(contextId === -1 ? undefined : '.debate-' + contextId);
    });

    if(classify) {
        let dragndropReload = argumentContainer.find('.context-dragndrop-reload-btn');
        dragndropReload.off('click');
        dragndropReload.on('click', function(){
            classify.trigger('dragndrop-reload', [false]);
        });
    }

}

async function loadDragnDrop(container, argumentContainerSelector, contextId, toCall, pane, options, fix){
    return new Promise(function (resolve, reject) {
        doLoadVisualizationsCall(toCall, contextId, pane, -1, container, true).then(function () {
            let classify = container.find('.context-classify-container');

            classify.each(function () {
                $(this).contextClassifier(options);
            });

            $(document).scrollTop(classify.position().top);

            dragndropListeners(container, argumentContainerSelector, contextId, classify, toCall, pane, options, fix);

            resolve();
        }).catch(function (jqXHR) {
            console.log("Error with drag n drop...");
        });
    });
}

function actionFromDragnDrop(btn){
    return getContributionOptionData(btn, "is-drag") !== -1;
}

function getWaitForIt() {
    return '<div class="d-flex align-items-center justify-content-center mt-5 mb-5"><div class="spinner-border" role="status"><span class="sr-only"></span></div></div>';
}

function initMediaShareButtonListeners(container) {
    let modalAnchor = $('#modal-anchor');

    container.find('.media-toggle').on('click', function(){

        let url = new URL(window.location.href);
        let params = new URLSearchParams(url.search);

        getShareMediaModal($(this).data('id'), params.get('pane'), params.get('pov')).done(function(modal){
            loadAndShowModal(modalAnchor, modal);
        }).fail(function(){
            console.log('Share media modal failed...');
        });
    });

}

function initLoadMoreActorsListeners(container) {
    container.find('.contribution-load-more-actors').on('click', function(){
        let that = $(this);
        that.after(getWaitForIt());

        getContributionActors(that.data('id'), that.data('role'), that.data('pane')).done(function(actors){
            that.parents('ul').first().html(actors);
        }).fail(function(){
            console.log('Load more contribution actors failed...');
        });
    });
}

function getDataFromActionDragnDrop(btn){
    let obj = {};
    let hierarchy = getContributionOptionData(btn, "complete-hierarchy");
    let ids = hierarchy !== -1 ? hierarchy.split('/') : [];

    if(ids.length >= 2) {
        obj.context = ids[ids.length - 1];
        obj.sub_context = ids[ids.length - 2] !== obj.context ? ids[ids.length - 2] : -1;

        obj.shade = getContributionOptionData(btn, "shade") !== -1 ? getContributionOptionData(btn, "shade")
            : ids.length >= 3 ? ids[ids.length - 3] === 'justify' ? 0 : 1 : -1;

        if(ids.length >= 4)
            obj.category = ids[ids.length - 4];

        if(ids.length >= 5)
            obj.argument = ids[0];
    }

    return Object.entries(obj).length === 0 ? null : obj;
}

function encodeMailTo(ch) {
    ch = encodeURIComponent(ch);

    ch = ch.replace(/%26quot%3B/g, '%22');
    ch = ch.replace(/%26%23x27%3B/g, '%27');

    return ch;
}

function scrollToTop() {
    $(document).scrollTop(0);
}

function initOpenLayerMap(places, selectedPlace, divId) {
    places = Array.isArray(places) ? places : places.places;
    let links = $('.' + divId + '-place_link');
    let mapContainer = $('#' + divId + '-map-container');
    let selectedPos;
    let selectedId;
    let pos;
    let lock = false;
    let map = null;

    initMap();

    function initMap() {
        if(map != null) {
            map.setTarget(null);
            map = null;

            links.off();
            mapContainer.find('.marker-container').remove();
        }

        selectedPos = null;
        selectedId = null;
        pos = initializePlacesPos();

        if (pos !== undefined && Array.isArray(pos)) {
            selectedPos = selectedPos == null ? pos[0] : selectedPos;

            map = new ol.Map({
                target: divId,
                layers: [
                    new ol.layer.Tile({
                        source: new ol.source.OSM()
                    })
                ],
                view: new ol.View({
                    center: selectedPos,
                    zoom: getZoomByPlaceType(places[0].placeType)
                })
            });

            // Place markers
            let i = 0;
            mapContainer.find(".marker").each(function () {
                let marker = new ol.Overlay({
                    position: pos[i++],
                    positioning: 'center-center',
                    element: this,
                    stopEvent: false
                });
                map.addOverlay(marker);
            });

            // Place label
            i = 0;
            mapContainer.find(".marker-place").each(function () {
                let place = new ol.Overlay({
                    position: pos[i++],
                    element: this
                });
                map.addOverlay(place);
            });

            if (selectedId != null)
                mapContainer.find('#' + divId + '-place-' + selectedId).parent().addClass('place-at-first');

            placesListener(map);
        }
    }

    function initializePlacesPos(){
        let pos = [];

        places.forEach(place => {
            if (!isNaN(place.latitude) && !isNaN(place.longitude)) {
                pos.push(ol.proj.fromLonLat([Number.parseInt(place.longitude), Number.parseInt(place.latitude)]));

                let elem = $('<div class="marker-container" style="display: none;">'+
                    '<span id="' + divId + '-place-' + place.id + '" class="overlay marker-place">' + place.name + '</span>'+
                    '<div class="marker" title="Marker"></div>'+
                    '</div>');
                mapContainer.append(elem);

                if(selectedPlace && selectedPlace === place.id){
                    selectedId = place.id;
                    selectedPos = pos[pos.length-1];
                }
            }
        });
        return pos;
    }

    function getZoomByPlaceType(placeType) {
        switch (placeType) {
            case 0 :
                return 2;
            case 1 :
                return 3;
            case 2 :
                return 5;
            case 3 :
                return 6;
            case 4 :
                return 8;
            case 5 :
                return 10;
            default :
                return 5;
        }
    }

    function placesListener(map){
        links.on("click", function() {
            let that = $(this);
            let lng = parseFloat(that.data("lng"));
            let lat = parseFloat(that.data("lat"));
            let zoom = getZoomByPlaceType(parseInt(that.data("zoom")));

            if (!isNaN(lng) && !isNaN(lat) && !isNaN(zoom)) {
                map.getView().setCenter(ol.proj.fromLonLat([lng, lat]));
                map.getView().setZoom(zoom);
                links.removeClass("place-at-first");
                $(that.data("place-id")).parent().addClass('place-at-first');
            }
            links.removeClass("place_underline");
            $(this).addClass("place_underline");
        });

        window.onresize = function() {
            reloadMap();
        };

        $(document).onresize = function() {
            reloadMap();
        };

        $(document).on("reload-map", function() {
            reloadMap();
            setTimeout(function(){
                reloadMap();
            }, 500);
        });
    }

    function reloadMap() {
        if(!lock){
            lock = true;
            initMap();

            setTimeout(function(){
                lock = false;
            }, 500);
        }
    }
}