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

/**
 * This javascript file contains
 *
 * @author Martin Rouffiange
 */
(function(e) { e.fn.scrollerPager = function (params, toCallAsync, options) {

    let eOptions,
        container = $(this),
        resultContainer = null,
        orderContainer = null,
        filtersContainer = null,
        filtersContent = null,
        filtersBtn = null,
        filters = undefined,
        waitingContainer = null,
        noResultContainer = null,
        searchbar = null,
        searchDateFrom = null,
        searchDateTo = null,
        citationPublicationDateFrom = null,
        citationPublicationDateTo = null,
        textPublicationDateFrom = null,
        textPublicationDateTo = null,
        actorType = null,
        actorRole = null,
        textType = null,
        textSource = null,
        citationSource = null,
        profession = null,
        placeId = null,
        place = null,
        tag = null,
        authorId = null,
        author = null,
        citationAuthorId = null,
        citationAuthor = null,
        textAuthorId = null,
        textAuthor = null,
        affiliationId = null,
        affiliation = null,
        fulltextText = null,
        fulltextDebate = null,
        fulltextTag = null,
        fulltextCitation = null,
        fulltextActor = null,
        currentFromIndex = 0,
        lock = false,
        total_lock = false;

    init();

    function init(){

        initOptions();

        if(toCallAsync != null) {
            resultContainer = eOptions.resultContainer != null ? container.children(eOptions.resultContainer) : container;

            if (resultContainer.exists()) {

                filtersContainer = eOptions.filtersContainer != null ? container.children(eOptions.filtersContainer) : null;
                orderContainer = eOptions.orderContainer != null ? container.children(eOptions.orderContainer) : null;
                waitingContainer = eOptions.waitingContainer != null ? container.children(eOptions.waitingContainer) : null;
                noResultContainer = eOptions.noResultContainer != null ? container.children(eOptions.noResultContainer) : null;

                if(filtersContainer != null) {
                    filtersBtn = filtersContainer.find('.filter-collapse-btn');
                    filtersBtn = filtersBtn.exists() ? filtersBtn : null;

                    filtersContent = filtersContainer.find('.filter-content');
                    filtersContent = filtersContent.exists() ? filtersContent : null;

                    updateFiltersContainer();
                }

                if(orderContainer != null) {
                    orderContainer = orderContainer.exists() ? orderContainer : null;
                }

                resultContainer.empty();
                initListeners();
                execToCallAsync();
            }
        }
    }

    function initOptions(){

        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                resultContainer : typeof options.resultContainer === 'string' ? options.resultContainer : '.results-container',
                orderContainer : typeof options.orderContainer === 'string' ? options.orderContainer : '.order-container',
                filtersContainer : typeof options.filtersContainer === 'string' ? options.filtersContainer : '.filters-container',
                waitingContainer : typeof options.waitingContainer === 'string' ? options.waitingContainer : '.waiting-container',
                noResultContainer : typeof options.noResultContainer === 'string' ? options.noResultContainer : '.no-result-container',
                step : typeof options.step === 'number' && Number.isInteger(options.step) ? options.step : 10,
                displayFunction : typeof options.displayFunction === 'function' ? options.displayFunction : null,
                toExecBefore : typeof options.toExecBefore === 'function' ? options.toExecBefore : null,
                toExecAfter : typeof options.toExecAfter === 'function' ? options.toExecAfter : null,
                typeaheadPlace : typeof options.typeaheadPlace === 'function' ? options.typeaheadPlace : addPlaceTypeahead,
                typeaheadTag : typeof options.typeaheadTag === 'function' ? options.typeaheadTag : addTagTypeahead,
                isModal : typeof options.isModal === 'boolean' ? options.isModal : false,
                typeof : options.relatedContributionId === 'number' && Number.isInteger(options.relatedContributionId) ? options.relatedContributionId : null,
                withFilters : typeof options.withFilters === 'boolean' ? options.isModal : true,
                search : typeof options.search === 'boolean' ? options.search : false,
                simpleSearch : typeof options.simpleSearch === 'boolean' ? options.simpleSearch : false,
                searchContainer : options.searchContainer instanceof jQuery ? options.searchContainer : null,
            };
        } else {
            eOptions = {
                resultContainer : '.results-container',
                orderContainer : '.order-container',
                filtersContainer : '.filters-container',
                waitingContainer : '.waiting-container',
                noResultContainer : '.no-result-container',
                step : 10,
                displayFunction : null,
                toExecAfter : null,
                typeaheadPlace : addPlaceTypeahead,
                typeaheadTag : addTagTypeahead,
                isModal : false,
                relatedContributionId : null,
                withFilters : true,
                search : false,
                simpleSearch : false,
                searchContainer : null
            };
        }

        toCallAsync = typeof toCallAsync === 'function' ? toCallAsync : null;
        params = Array.isArray(params) ? params : null;
    }

    function initListeners() {

        if(eOptions.isModal) {
           container.parents('.modal-body').first().scroll(function(e){
                loadMoreEvent();
            });
        } else {
            $(document).scroll(function(e){
                loadMoreEvent();
            });
        }

        if(eOptions.simpleSearch && eOptions.searchContainer != null &&
            eOptions.searchContainer.find('.search-input').exists() && eOptions.searchContainer.find('.search-btn').exists()) {

            eOptions.searchContainer.find('.search-btn').on('click', function(){
                params[0] = eOptions.searchContainer.find('.search-input').val();
                reload();
            });

            eOptions.searchContainer.find('.search-input').on('keydown', function(evt){
                if(evt.which === 13) {
                    params[0] = eOptions.searchContainer.find('.search-input').val();
                    reload();
                }
            });
        }

        $(document).on('reload-viz', function(){
            reload();
        });

        container.on('click', '.more-results-btn', function(){
            loadMoreEvent();
        });

        if(orderContainer != null) {
            orderContainer.find('.order-btn').on('click', function(){
                reload($(this).data('order'));
                orderContainer.dropdown('toggle');
            });
        }

        if(eOptions.search) {
            container.find('.search-ctype-selector').on('click', function(){
                if(filtersContainer != null) {
                    let isAll = filtersContainer.find('#isAll');

                    if(isAll.exists()) {
                        if(!container.find('.search-ctype-selector.selected').exists() && isAll.val()) {
                            container.find('.search-ctype-selector').find('input').val(false);
                            isAll.val(false);
                        }
                    }
                }


                $(this).toggleClass('selected');
                $(this).children('input').val($(this).hasClass('selected'));

                if(filtersContainer != null) {
                    let isAll = filtersContainer.find('#isAll');

                    if(isAll.exists()) {
                        if(!container.find('.search-ctype-selector:not(.selected)').exists()) {
                            container.find('.search-ctype-selector').removeClass('selected');
                            container.find('.search-ctype-selector').find('input').val(true);
                            isAll.val(true);
                        } else if(!container.find('.search-ctype-selector.selected').exists()) {
                            container.find('.search-ctype-selector').find('input').val(true);
                            isAll.val(true);
                        }
                    }
                }

                updateFiltersContainer();
                execToCallWithFiltersAsync();
            });
        }

        if(filtersContainer != null) {
            if(eOptions.withFilters) {
                searchDateFrom = filtersContainer.find('input[name="search_date_from"]');
                searchDateTo = filtersContainer.find('input[name="search_date_to"]');
                citationPublicationDateFrom = filtersContainer.find('input[name="citation_publication_date_from"]');
                citationPublicationDateTo = filtersContainer.find('input[name="citation_publication_date_to"]');
                textPublicationDateFrom = filtersContainer.find('input[name="text_publication_date_from"]');
                textPublicationDateTo = filtersContainer.find('input[name="text_publication_date_to"]');
                actorType = filtersContainer.find('select[name="actor_type"]');
                actorRole = filtersContainer.find('select[name="actor_role"]');
                textType = filtersContainer.find('select[name="text_type"]');
                textSource = filtersContainer.find('input[name="text_source"]');
                citationSource = filtersContainer.find('input[name="citation_source"]');
                placeId = filtersContainer.find('input[name="place.id"]');
                place = filtersContainer.find('input[name="place"]');
                authorId = filtersContainer.find('input[name="author.id"]');
                author = filtersContainer.find('input[name="author"]');
                citationAuthorId = filtersContainer.find('input[name="citation_author.id"]');
                citationAuthor = filtersContainer.find('input[name="citation_author"]');
                textAuthorId = filtersContainer.find('input[name="text_author.id"]');
                textAuthor = filtersContainer.find('input[name="text_author"]');
                affiliationId = filtersContainer.find('input[name="affiliation.id"]');
                affiliation = filtersContainer.find('input[name="affiliation"]');
                tag = filtersContainer.find('input[name="tag"]');
                profession = filtersContainer.find('input[name="profession"]');
                fulltextCitation = filtersContainer.find('input[name="fulltext_citation"]');
                fulltextText = filtersContainer.find('input[name="fulltext_text"]');
                fulltextDebate = filtersContainer.find('input[name="fulltext_debate"]');
                fulltextTag = filtersContainer.find('input[name="fulltext_tag"]');
                fulltextActor = filtersContainer.find('input[name="fulltext_actor"]');

                if(filtersBtn != null && filtersContent != null) {
                    filtersBtn.on('click', function () {
                        filtersContent.toggle();
                        filtersBtn.toggleClass('open');
                    });
                }

                filtersContainer.find('.filter-btn').on('click', function () {
                    execToCallWithFiltersAsync();
                });

                filtersContainer.find('.delete-filter-btn').on('click', function () {
                    if(filtersContent != null) {
                        filtersContent.find('input').val('');
                        execToCallWithFiltersAsync();

                        if (filtersBtn != null) {
                            filtersBtn.hide();
                            filtersContent.hide();
                            filtersBtn.removeClass('open');
                        }
                    }
                });

                if(searchDateTo.exists()) {
                    searchDateFrom.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                    searchDateTo.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                }

                if(citationPublicationDateTo.exists()) {
                    citationPublicationDateFrom.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                    citationPublicationDateTo.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                }

                if(textPublicationDateTo.exists()) {
                    textPublicationDateFrom.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                    textPublicationDateTo.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                }

                if(place.exists()) {
                    eOptions.typeaheadPlace(place, 0);
                }
                if(tag.exists()) {
                    //eOptions.typeaheadTag(tag, 0);
                }
                if(profession.exists()) {
                    addActorFunctionTypeahead(-1, profession, 2, -1);
                }

                if(author.exists()) {
                    addActorTypeahead(author, '-1');
                }
                if(citationAuthor.exists()) {
                    addActorTypeahead(citationAuthor, '-1');
                }
                if(textAuthor.exists()) {
                    addActorTypeahead(textAuthor, '-1');
                }
                if(affiliation.exists()) {
                    addActorTypeahead(affiliation, '1');
                }

                let idInputs = [
                    {idInput:affiliationId, input:affiliation},
                    {idInput:placeId, input:place},
                    {idInput:authorId, input:author},
                    {idInput:citationAuthorId, input:citationAuthor},
                    {idInput:textAuthorId, input:textAuthor}
                ];

                idInputs.forEach(input => {
                    input.input.on('keyup', function () {
                        input.idInput.val('');
                    });

                    input.input.on('focusout', function () {
                        if (input.idInput.val() === "") {
                            input.input.val('');
                            input.input.typeahead('val', '');
                        }
                    });
                });
            }

            searchbar = filtersContainer.find('.searchbar-container');

            filtersContainer.find('input[type="text"]:visible').keypress(function(e) {
                if (e.which === 13) {
                    execToCallWithFiltersAsync();
                }
            });
            filtersContainer.find('.search-btn').on('click', function () {
                execToCallWithFiltersAsync();
            });
        }

    }

    function updateFiltersContainer() {
        let selectors = container.find('.search-ctype-selector');

        if(eOptions.search && filtersContent != null && selectors.exists()) {
            let selectedTypes = [];
            let filters = filtersContent.find('.filter');

            (selectors.filter('.selected').exists() ? selectors.filter('.selected') : selectors).each(function() {
                selectedTypes.push($(this).data('type'));
            });

            filters.hide();

            selectedTypes.forEach(type => filters.filter('.type-' + type).show());
        }
    }

    function reload(value) {
        currentFromIndex = 0;
        lock = false;
        total_lock = false;
        resultContainer.empty();
        execToCallWithFiltersAsync(value);
    }

    function loadMoreEvent() {

        if(container.is(':visible') && !total_lock && !lock && window.scrollY > resultContainer.height() + resultContainer.offset().top - window.innerHeight) {
            lock = true;
            currentFromIndex += eOptions.step;
            execToCallAsync();
        }
    }

    function execToCallWithFiltersAsync(value) {
        currentFromIndex = 0;
        lock = false;
        total_lock = false;
        resultContainer.empty();
        filters = value;

        if(eOptions.withFilters && !value) {
            filters = '';

            let dateInputs = [
                    {key:'search_date', inputs:[searchDateFrom, searchDateTo]},
                    {key:'citation_publication_date', inputs:[citationPublicationDateFrom, citationPublicationDateTo]},
                    {key:'text_publication_date', inputs:[textPublicationDateFrom, textPublicationDateTo]}
                ];
            let fullTextInputs = [
                    {key:'affiliation_function', input:profession},
                    {key:'tag', input:tag},
                    {key:'text_source', input:textSource},
                    {key:'citation_source', input:citationSource},
                    {key:'fulltext_citation', input:fulltextCitation},
                    {key:'fulltext_debate', input:fulltextDebate},
                    {key:'fulltext_tag', input:fulltextTag},
                    {key:'fulltext_text', input:fulltextText},
                    {key:'fulltext_actor', input:fulltextActor}
                ];
            let idInputs = [
                    {key:'affiliation', idInput:affiliationId, input:affiliation},
                    {key:'place', idInput:placeId, input:place},
                    {key:'author', idInput:authorId, input:author},
                    {key:'citation_author', idInput:citationAuthorId, input:citationAuthor},
                    {key:'text_author', idInput:textAuthorId, input:textAuthor}
                ];
            let selectInputs = [
                {key:'actor_type', select:actorType},
                {key:'actor_role', select:actorRole},
                {key:'text_type', select:textType},
            ];

            dateInputs.forEach(date => {
                if (date.inputs[0].exists() && date.inputs[1].exists()
                    && (date.inputs[0].val() !== "" || date.inputs[1].val() !== "")) {

                    filters += date.key + ":";
                    filters += treatDateValue(date.inputs[0]) + ",";
                    filters += treatDateValue(date.inputs[1]) + ";";
                }
            });

            fullTextInputs.forEach(input => {
                if (input.input.exists()) {
                    let value = input.input.val().trim().replace(/;/g, '').replace(/:/g, '');

                    if (value) {
                        filters += input.key + ":" + encodeURI(value) + ";";
                    }

                    input.input.val(value);
                    input.input.typeahead('val', value);
                }
            });

            idInputs.forEach(input => {
                if (input.idInput.exists()) {
                    if (input.idInput.val()) {
                        filters += input.key + ":" + input.idInput.val() + ";";
                    } else {
                        input.input.val('');
                        input.input.typeahead('val', '');
                    }
                }
            });

            selectInputs.forEach(obj => {
                if (obj.select.exists() && obj.select.val() > -1) {
                    filters += obj.key + ":" + obj.select.val() + ";";
                }
            });

        }

        execToCallAsync();
    }

    function treatDateValue(input) {
        let date = "null";
        let value = input.val();

        if(value) {
            let values = value.split('/');

            switch (values.length) {
                case 1 :
                    date = values[0];
                    break;
                case 2 :
                    date = values[1] + (values[0].length === 1 ? '0' : '') + values[0];
                    break;
                case 3 :
                    date = values[2] + (values[1].length === 1 ? '0' : '') + values[1] + (values[0].length === 1 ? '0' : '') + values[0];
                    break;
            }
        }

        return date;
    }

    function execToCallAsync() {

        toggleWaitingContainer(true);

        resultContainer.parent().find('.more-results-container').remove();

        if(noResultContainer != null) {
            noResultContainer.hide();
        }

        let willCall;
        let lastParam = orderContainer != null && !filters && params != null ? params[params.length - 1] : filters;

        if(params != null) {
            switch (params.length - (orderContainer != null ? 1 : 0)) {
                case 2 :
                    willCall = toCallAsync(params[0], params[1], currentFromIndex, currentFromIndex + eOptions.step, lastParam);
                    break;
                case 3 :
                    willCall = toCallAsync(params[0], params[1], params[2], currentFromIndex, currentFromIndex + eOptions.step, lastParam);
                    break;
                case 4 :
                    willCall = toCallAsync(params[0], params[1], params[2], params[3], currentFromIndex, currentFromIndex + eOptions.step, lastParam);
                    break;
                case 5 :
                    willCall = toCallAsync(params[0], params[1], params[2], params[3], params[4], currentFromIndex, currentFromIndex + eOptions.step, lastParam);
                    break;
                default :
                    willCall = toCallAsync(params[0], currentFromIndex, currentFromIndex + eOptions.step, lastParam);
            }
        } else {
            willCall = toCallAsync(currentFromIndex, currentFromIndex + eOptions.step);
        }

        if(eOptions.search) {
            container.find('#queryString').val('');
        }

        willCall.done(function(results){
            if(eOptions.toExecBefore != null)
                eOptions.toExecBefore(container, results);

            if(eOptions.search && Array.isArray(results) && results.length > 0) {
                results.shift();
            } else if(eOptions.search && !Array.isArray(results) && $(results).length === 1 && $(results).is('.fullquery')) {
                results = [];
            }

            if((Array.isArray(results) && results.length === 0) || (!Array.isArray(results) && (!results || results.trim().length === 0 || $(results).hasClass('no-more-results')))) {
                total_lock = true;

                if(currentFromIndex === 0) {
                    if(filtersBtn != null && !filters) {
                        filtersBtn.show();
                        filtersBtn.hide();

                        if (filtersContent != null) {
                            filtersContent.hide();
                            filtersBtn.removeClass('open');
                        }
                    }
                    if(noResultContainer != null) {
                        noResultContainer.show();
                    }
                }
            } else {
                let nbLoadedItems;

                if (Array.isArray(results)) {

                    results.forEach(result =>
                        resultContainer.append(eOptions.displayFunction != null ? eOptions.displayFunction(result) : result));

                    nbLoadedItems = results.length;

                } else {
                    resultContainer.append(results);
                    nbLoadedItems = $(results).filter("div").length;
                }

                if(nbLoadedItems === eOptions.step) {
                    resultContainer.after(getSeeMoreResultsBtn());

                    if(filtersBtn != null) {
                        filtersBtn.show();
                    }
                } else {
                    total_lock = true;

                    if(currentFromIndex === 0 && nbLoadedItems <= 1 && !filters && filtersBtn != null && filtersContent != null) {
                        filtersBtn.show();
                        filtersBtn.hide();
                        filtersContent.hide();
                        filtersBtn.removeClass('open');
                    } else if(filtersBtn != null) {
                        filtersBtn.show();
                    }
                }

                lock = false;
            }

            if(eOptions.toExecAfter != null)
                eOptions.toExecAfter(container, eOptions.isModal);

            toggleWaitingContainer(false);

        }).fail(function(err){
            toggleWaitingContainer(false);
            total_lock = true;

            if(filtersBtn != null) {
                filtersBtn.hide();

                if (filtersContent != null) {
                    filtersContent.hide();
                    filtersBtn.removeClass('open');
                }
            }

            //console.log('Scroller pager to call async fail...' + err);
        });

    }

    function toggleWaitingContainer(show) {
        if(waitingContainer != null) {
            waitingContainer.toggle(show);
        }
    }

    function getSeeMoreResultsBtn() {
        return '<div class="d-flex justify-content-center mt-3 more-results-container"><button type="button" class="btn btn-info more-results-btn">' + Messages("general.results.see.more") + '</button></div>';
    }

};}(jQuery));
