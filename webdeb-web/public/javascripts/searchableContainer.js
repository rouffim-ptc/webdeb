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
 * Searchable container is a jQuery library to search and sort inside a list of related elements.
 *
 * Includes jquery.js
 * https://jquery.com/
 */
(function(e) { e.fn.searchableContainerJs = function (options) {

    let eOptions,
        container = $(this),
        inputs = container.find('.searchable-input'),
        elementsContainer = container.find('.searchable-elements').exists() ? container.find('.searchable-elements') : container,
        elements = elementsContainer.find('.searchable-element'),
        searchTypes = {},
        eventLock = false;

    $(document).ready(function() {
        initOptions();
        initListeners();
    });

    function initOptions(){
        // handle passed options to searchableContainerJS

        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                sortElements : typeof(options.sortElements) === "boolean" ? options.sortElements : false,
                multipleSearch : typeof(options.multipleSearch) === "boolean" ? options.multipleSearch : false,
                sortBy : typeof(options.sortBy) === "string" ? options.sortBy : false
            };
        } else {
            eOptions = {
                sortElements : false,
                multipleSearch : false,
                sortBy : false
            };
        }
    }

    function initListeners(){
        if(inputs.exists()){
            inputs.on('keydown', function(){
                doSearchElements($(this));
            });

            inputs.on('click', function(){
                doSearchElements($(this));
            });
        }
    }

    function doSearchElements(element){
        if(!eventLock && (!eOptions.multipleSearch || element.data("searchtype") !== undefined)) {
            eventLock = true;

            if(eOptions.multipleSearch) {
                if (element.prop('type') === 'checkbox' && !element.prop('checked')) {
                    delete searchTypes[element.data("searchtype")];
                } else {
                    searchTypes[element.data("searchtype")] = element.val();
                }
            }

            searchElements(element.val());

            setTimeout(function(){ eventLock = false; }, 20);
        }
    }

    function searchElements(searchString){
        if(elements.exists()) {
            if (isString(searchString)) {
                elements.each(function () {searchElement($(this), searchString);});
            } else {
                elements.show();
            }
        }
    }

    function searchElement(element, searchString){
        let show = true;

        if(eOptions.multipleSearch) {
            for (let typeName in searchTypes) {
                let data_name = 'search-' + typeName;
                let element_val = isString(element.data(data_name)) ? element.data(data_name) :
                    typeof(element.data(data_name)) === 'boolean' ? element.data(data_name) + "" : null;

                if (show && element_val != null) {
                    show = element_val.toLowerCase().includes(searchTypes[typeName].toLowerCase());
                }
            }
        } else {
            show = element.data('search').toLowerCase().includes(searchString.toLowerCase());
        }

        element.toggle(show);
    }

    function isString(string){
        return typeof(string) === 'string' && string.length > 0;
    }

};}(jQuery));