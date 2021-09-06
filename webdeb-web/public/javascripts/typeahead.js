/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

/*!
 * typeahead v1.0 is a jQuery extension to create suggestions on a input field.
 * https://github.com/martini224
 *
 * Includes jquery.js
 * https://jquery.com/
 *
 * Copyright Martin Rouffiange (martini224) 2018
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
(function(e) { e.fn.typeahead = async function (options, params) {

    let eOptions,
        input = $(this),
        suggestionsContainer = null,
        lock = false;

    function init() {
        if(input.exists() && (input.is('input') || input.is('textarea'))) {
            switch (options) {
                case 'val' :
                    input.val(params);
                    break;
                case 'destroy' :
                    input.off('keyup');
                    if(suggestionsContainer != null) {
                        suggestionsContainer.remove();
                    }
                    break;
                default :
                    initOptions();
                    initListeners();
            }
        }
    }


    function initOptions() {
        // handle passed options to typeahead
        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                minLength: isNaN(options.minLength) || options.minLength <= 0 ? 3 : options.minLength,
                limit: isNaN(options.minLength) || options.minLength <= 0 ? 10 : options.minLength,
                lockTime: isNaN(options.lockTime) || options.lockTime <= 0 ? 50 : options.lockTime,
                parent : options.parent instanceof jQuery ? options.parent : null,
                maxWith : typeof(options.maxWith) === "boolean" ? options.maxWith : true,
            };
        } else {
            eOptions = {
                minLength : 3,
                limit : 10,
                lockTime : 50,
                parent : null,
                maxWith: true
            };
        }
    }

    function initListeners() {

        input.on('keyup', function(){
            let value = $(this).val();

            if(!lock && $(this).val().length >= eOptions.minLength) {
                lock = true;

                setTimeout(function () {

                    callSuggestions(value);
                    lock = false;

                }, eOptions.lockTime);

                callSuggestions(value);
            }
        });

        input.on('focusin', function(){
            if(suggestionsContainer != null) {
                suggestionsContainer.show();
            }
        });

        input.on('focusout', function(){
            if(suggestionsContainer != null) {
                setTimeout(function() {
                    suggestionsContainer.hide();
                }, 250);
            }
        });

    }

    function callSuggestions(value) {

        if(params && typeof params.source === 'function') {

            params.source(value, displaySuggestions);

        }
    }

    function displaySuggestions(suggestions) {

        if(params && typeof params.templates && typeof params.templates.suggestion === 'function') {

            if(!Array.isArray(suggestions))
                suggestions = [suggestions];

            if(suggestionsContainer != null) {
                suggestionsContainer.remove();
            }

            if(suggestions.length > 0) {

                suggestionsContainer = $('<div class="tt-suggestions ' + (eOptions.maxWith ? '' : 'unlimited') + '"></div>');

                suggestions.forEach(suggestion => {
                    let suggestionContainer = $('<div class="tt-suggestion" data-index="' + suggestions.indexOf(suggestion) + '"></div>');

                    suggestionContainer.append(params.templates.suggestion(suggestion));

                    highlightSuggestion(suggestionContainer);

                    suggestionsContainer.append(suggestionContainer);
                });

                insertSuggestions();

                suggestionsContainer.find('.tt-suggestion').on('click', function () {
                    input.trigger('typeahead:selected', [suggestions[$(this).data('index')]]);
                });
            }

        }
    }

    function highlightSuggestion(container) {
        let value = input.val();

        if(container.children().length > 0) {
            container.find('*').each(function () {
                if ($(this).text()) {
                    doHighlightSuggestion($(this), value);
                }
            })
        } else {
            doHighlightSuggestion(container, value);
        }
    }

    function doHighlightSuggestion(element, value) {
        let rgxp = new RegExp(value, 'i');
        let repl = '<strong>' + value + '</strong>';
        element.html(element.html().replace(rgxp, repl));
    }

    function insertSuggestions() {
        if(eOptions.parent == null) {
            let parent;

            if (input.parents('.input-group').exists()) {
                parent = input.parents('.input-group').first();
            } else {
                parent = input;
            }

            let existing = parent.parent().find('.tt-suggestions');
            if(existing.exists())
                existing.remove();

            suggestionsContainer.insertAfter(parent);
        } else {
            suggestionsContainer.insertAfter(eOptions.parent);

            let existing = eOptions.parent.parent().find('.tt-suggestions');
            if(existing.exists())
                existing.remove();
        }
    }


    init();

};}(jQuery));
