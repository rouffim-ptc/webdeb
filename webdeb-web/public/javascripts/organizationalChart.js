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
 * organizationalChartJs v1.0 is a jQuery extension to create a organization chart with html elements.
 * https://github.com/martini224
 *
 * Includes jquery.js
 * https://jquery.com/
 *
 * Copyright Martin Rouffiange (martini224) 2018
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
(function(e) { e.fn.organizationalChartJs = async function (data, options) {

    let eOptions,
        container = $(this),
        tables_container = null,
        table_title_container = null,
        table_container = null,
        header_y = null,
        begin_date = null,
        end_date = null,
        chart_data = null,
        year_ends = null,
        year_by_column = null,
        actorNewLine = false,
        tooMuchColumns = false,
        max_year = null,
        box_width = false,
        nbColumns,
        scroll = false,

        filtersContainer = null,
        filtersContent = null,
        filtersBtn = null,
        filters = undefined,
        affiliationDateFrom = null,
        affiliationDateTo = null,
        affiliationId = null,
        affiliation = null,
        otherAffiliationId = null,
        otherAffiliation = null,
        actorType = null,
        affiliationType = null,
        profession = null,
        placeId = null,
        place = null,
        genderMale = null,
        genderFemale = null;

    async function init(filters) {
        await initChart(filters);
        initListeners();
    }

    async function initChart(filters) {
        await initOptions(filters);
        createChartContainer();
        createChart();
    }

    async function initOptions(filters){
        // handle passed options to calendarJs
        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                box_height : isNaN(options.box_height) || options.box_height <= 0 ? 55 : options.box_height,
                nb_columns : isNaN(options.nb_columns) || options.nb_columns <= 0 ? 10 : options.nb_columns,
                year_step : isNaN(options.year_scale) || options.year_scale <= 0 ? 6 : options.year_scale,
                endYear : (options.endDate == null || options.endDate.getTime() > Date.now() ? new Date() : options.endDate).getFullYear() + 1,
                beginYear : options.beginDate == null ? this.endYear - 250 : options.beginDate.getFullYear(),
                year_scale_max : isNaN(options.year_scale_max) || options.year_scale_max <= 0 ? 100 : options.year_scale_max,
                year_scale_min : isNaN(options.year_scale_min) || options.year_scale_min <= 0 ? 1 : options.year_scale_min,
                separatedChart : isNaN(options.separatedChart) || options.separatedChart < 0 || options.separatedChart > 1 ? 0 : options.separatedChart,
                waitForIt : options.waitForIt instanceof jQuery ? options.waitForIt : false,
                scrollAnimation : typeof(options.scrollAnimation) === "boolean" ? options.scrollAnimation : false,
                navbar : typeof(options.navbar) === "boolean" ? options.navbar : true,
                completeAffiliation : typeof(options.completeAffiliation) === "boolean" ? options.completeAffiliation : false,
                affiliatedId : isNaN(options.affiliatedId) || options.affiliatedId < 0 ? false : options.affiliatedId,
                filtersBtn : options.filtersBtn instanceof jQuery ? options.filtersBtn : null,
                min_box_width : isNaN(options.min_box_width) ? 10 : options.min_box_width,
                toCall : typeof options.toCall === 'function' ? options.toCall : null,
            };
            updateDateOptions(options.beginDate);
        } else {
            eOptions = {
                year_scale : 40,
                box_height : 60,
                nb_columns : 10,
                year_step : 10,
                year_scale_max : 100,
                year_scale_min : 10,
                endYear : new Date().getFullYear() + 1,
                beginYear : this.endYear - 250,
                separatedChart : 0,
                waitForIt : false,
                scrollAnimation: false,
                navbar : true,
                completeAffiliation : false,
                affiliatedId : false,
                filtersBtn : null,
                min_box_width : 20,
                toCall : null
            };
        }

        nbColumns = eOptions.nb_columns;

        await treatData(filters);
    }

    function updateDateOptions(beginYear) {
        eOptions.beginYear = beginYear == null ? eOptions.endYear - 250 : beginYear.getFullYear();
        eOptions.year_scale = isNaN(options.year_scale) || options.year_scale <= 0 ?
            eOptions.endYear - eOptions.beginYear < 40 ? eOptions.endYear - eOptions.beginYear : 40 : options.year_scale;
        eOptions.beginYear = eOptions.year_scale % 2 === 1 ? eOptions.beginYear - 1 : eOptions.beginYear;
        eOptions.year = eOptions.year_scale % 2 === 1 ? eOptions.year_scale + 1 : eOptions.year_scale;
    }

    function initListeners(){

        if(Array.isArray(chart_data)) {

            /*
             * Rebuild chart on screen resize
             */
            $(window).on('resize', function () {
                createChart();
            });

            $(window).on('scroll', function () {
                if(header_y.exists()) {
                    let otherSticky = $('.contribution-viz-header.viz-head-scrolled');
                    header_y.css('top', (otherSticky.exists() ? otherSticky.height() + 'px' : 0));
                }
            });

            /*
             * Rebuild chart on reflow event
             */
            container.on('reflow-chart', function () {
                setTimeout(createChart, 200);
            });

            if(eOptions.navbar) {
                container.on("click", ".bZoom", function () {
                    let former = eOptions.year_scale;

                    if ($(this).hasClass("bZoomIn")) {
                        eOptions.year_scale = eOptions.year_scale - eOptions.year_step < eOptions.year_scale_min ?
                            eOptions.year_scale_min : eOptions.year_scale - eOptions.year_step;
                    } else {
                        eOptions.year_scale = eOptions.year_scale + eOptions.year_step > eOptions.year_scale_max ?
                            eOptions.year_scale_max : eOptions.year_scale + eOptions.year_step;
                    }

                    if (former !== eOptions.year_scale)
                        createChart().then();
                });

                container.on("click", ".bNav", function () {
                    let former = year_ends;

                    if ($(this).hasClass("bPrev")) {
                        year_ends = year_ends - eOptions.year_scale < eOptions.beginYear ? year_ends : year_ends - eOptions.year_scale;
                    } else {
                        year_ends = year_ends + eOptions.year_scale > eOptions.endYear ? eOptions.endYear : year_ends + eOptions.year_scale;
                    }

                    if (former !== year_ends)
                        createChart().then();
                });
            }

            table_container.on("mouseover", "tr", function () {
                highlightTableLine(true, $(this), true);

            });

            table_container.on("mouseout", "tr", function () {
                highlightTableLine(false, $(this), true);
            });

            table_container.on("mouseover", ".organigram-line", function () {
                highlightTableLine(true, $(this).data("index") + 1, true);
            });

            if(eOptions.scrollAnimation) {
                table_container.find(".organigram-container-table").scroll(function (e) {
                    if (scroll) {
                        $(this).prop("scrollTop", 0);

                        let scrollValue = container.parent().offset().top - 130;

                        $([document.documentElement, document.body]).animate({
                            scrollTop: scrollValue
                        }, 100);

                        $(this).css("max-height", ($(window).height() - (scrollValue + 50)) + "px");
                        scroll = false;
                    }
                });
            }

            table_container.on('click', function(e){
                let target = $(e.target);

                if(target.hasClass('organigram-linecontent')) {
                    table_container.find('.organigram-lineinfo').remove();

                    let info = $('<div class="organigram-lineinfo">' + target.prop("title") + '</div>').appendTo(target.parent());
                    info.css('top', (target.position().top - 50) + 'px');
                    let left = (table_container.width() / 2) < target.position().left ? 230 : 0;
                    info.css('left', ($(window).width() < 800 ? ($(window).width() / 2 - info.width() / 2) : (target.position().left - left)) + 'px');
                }else{
                    table_container.find('.organigram-lineinfo').remove();
                }
            });

            if(eOptions.filtersBtn != null) {
                filtersContainer = eOptions.filtersBtn;

                filtersBtn = filtersContainer.find('.filter-collapse-btn');
                filtersBtn = filtersBtn.exists() ? filtersBtn : null;

                filtersContent = filtersContainer.find('.filter-content');
                filtersContent = filtersContent.exists() ? filtersContent : null;

                filtersContainer.show();
                if(filtersBtn != null)
                    filtersBtn.show();

                affiliationDateFrom = filtersContainer.find('input[name="affiliation_date_from"]');
                affiliationDateTo = filtersContainer.find('input[name="affiliation_date_to"]');
                actorType = filtersContainer.find('select[name="actor_type"]');
                placeId = filtersContainer.find('input[name="place.id"]');
                place = filtersContainer.find('input[name="place"]');
                profession = filtersContainer.find('input[name="profession"]');
                affiliationId = filtersContainer.find('input[name="affiliation.id"]');
                affiliation = filtersContainer.find('input[name="affiliation"]');
                otherAffiliationId = filtersContainer.find('input[name="other_affiliation.id"]');
                otherAffiliation = filtersContainer.find('input[name="other_affiliation"]');
                affiliationType = filtersContainer.find('input[name="affiliation_type"]');
                genderMale = filtersContainer.find('input[name="gender_m"]');
                genderFemale = filtersContainer.find('input[name="gender_f"]');

                if(filtersBtn != null && filtersContent != null) {
                    filtersBtn.on('click', function () {
                        filtersContent.toggle();
                        filtersBtn.toggleClass('open');
                    });
                }

                filtersContainer.find('.filter-btn').on('click', function () {
                    reloadWithFilters();
                });

                filtersContainer.find('.delete-filter-btn').on('click', function () {
                    if(filtersContent != null) {
                        filtersContent.find('input').val('');
                        reloadWithFilters();
                    }
                });

                if(affiliationDateTo.exists()) {
                    affiliationDateFrom.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                    affiliationDateTo.datepicker({
                        dateFormat: 'dd/mm/yy'
                    });
                }

                if(place.exists()) {
                    addPlaceTypeahead(place, 0);
                }
                if(profession.exists()) {
                    addActorFunctionTypeahead(-1, profession, 2, -1);
                }

                if(affiliation.exists()) {
                    addActorTypeahead(affiliation, '1');
                }
                if(otherAffiliation.exists()) {
                    addActorTypeahead(otherAffiliation, '1');
                }

                let idInputs = [
                    {idInput:affiliationId, input:affiliation},
                    {idInput:otherAffiliationId, input:otherAffiliation},
                    {idInput:placeId, input:place},
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
        }
    }

    function reloadWithFilters() {
        filters = '';

        let dateInputs = [
            {key:'affiliation_date', inputs:[affiliationDateFrom, affiliationDateTo]},
        ];
        let fullTextInputs = [
            {key:'affiliation_function', input:profession},
            {key:'affiliation_type', input:affiliationType}
        ];
        let idInputs = [
            {key:'other_affiliation', idInput:otherAffiliationId, input:otherAffiliation},
            {key:'place', idInput:placeId, input:place}
        ];
        let selectInputs = [
            {key:'actor_type', select:actorType}
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
            if (input.input.exists() && (!input.input.is('input[type="checkbox"]') || input.input.is(':checked'))) {
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

        if(genderMale.exists() && genderFemale.exists() && genderMale.is(':checked') || genderFemale.is(':checked')) {
            if(genderMale.is(':checked') && !genderFemale.is(':checked')) {
                filters += "gender:" + genderMale.val() + ";";
            } else if(!genderMale.is(':checked') && genderFemale.is(':checked')) {
                filters += "gender:" + genderFemale.val() + ";";
            }
        }

        initChart(filters);
    }

    function highlightTableLine(highlight, element, forSibling){

        if(forSibling)
            container.find(".highlight-line").removeClass("highlight-line");

        if(!isNaN(element)){
            element = table_container.find("tr").eq(element);
        }

        if(!element.hasClass("no-highlight-line")) {
            element.toggleClass("highlight-line", highlight);
        }

        if(actorNewLine && forSibling) {
            highlightTableLine(highlight, element.find('a').exists() ? element.next() : element.prev(), false);
        }
    }

    function createChartContainer(){

        if (header_y == null) {
            header_y = $('<div class="organigram-container-header"></div>').appendTo(container);
        }

        if (table_container == null) {
            table_container = $('<div style="flex-grow: 1;overflow: hidden;"></div>').appendTo(container);
        }
    }

    function displayWaitForIt(show){
        if (eOptions.waitForIt) {
            eOptions.waitForIt.toggle(show);

            /*
            if(show && $('.waiting').is(':visible')){
                eOptions.waitForIt.hide();
            }
            */
        }
    }

    function createChart(){

        displayWaitForIt(true);

        doCreateChartAsync();

        displayWaitForIt(false);
    }

    function doCreateChartAsync(){

        if (Array.isArray(chart_data) && chart_data.length && container.is(':visible')) {
            let dataNotEmpty = (eOptions.separatedChart > 0 && (chart_data[0].affiliations.length > 0 || chart_data[1].affiliations.length > 0))
                || (eOptions.separatedChart === 0 && chart_data[0].affiliations.length > 0);
            table_container.empty();

            let table_subcontainer = $('<div class="organigram-container-table"></div>').appendTo(table_container);
            let table = $('<table class="organigram"></table>').appendTo(table_subcontainer);

            // build table
            actorNewLine = $(window).width() <= 992;
            let title_width = actorNewLine ? 0 : dataNotEmpty ? $(window).width() > 1600 ? 400 : 350 : 100;
            let table_width = table.width() - title_width;

            // compute year and dates
            year_ends = eOptions.endYear;
            let year = year_ends - eOptions.year_scale;
            begin_date = new Date(year, 0, 1, 1, 0, 0);
            end_date = new Date(year_ends, 0, 1, 1, 0, 0);

            let year_diff = year_ends - year;
            nbColumns = computeNbColumns(table_width, year_diff);
            year_by_column = Math.floor( year_diff / nbColumns);
            max_year = year + (year_by_column * nbColumns);
            table_width = table.width() - title_width;
            let total_width = actorNewLine ? table.width() : table_width;
            nbColumns += 1;
            let half_box_width_percent = ((100 - (actorNewLine ? 0 : (title_width / table.width()) * 100)) / (actorNewLine ?  nbColumns * 2 : ((nbColumns - 1) * 2) + 1));

            let header = $('<tr class="no-highlight-line"></tr>').prependTo(table);
            header.append('<th style="width:' + (actorNewLine ? (half_box_width_percent) + '%' : title_width + 'px') + '"></th>');

            for (let iHeader = 0; iHeader < nbColumns - 1; iHeader++) {
                header.append('<th style="width:' + (half_box_width_percent * 2) + '%"></th>');
            }

            let lastCol = header.find('th:last-child');
            header.append('<th style="width:' + (half_box_width_percent) + '%"></th>');

            box_width = lastCol.width();
            tooMuchColumns = box_width < 50;
            title_width = actorNewLine ? (box_width / 2) : title_width;

            header_y.empty();

            let subHeaderY2 = $('<div class="d-flex flex-column align-items-center align-items-lg-end"></div>').appendTo(header_y);

            if(dataNotEmpty) {
                if (eOptions.navbar) {
                    let menu = '<div class="d-flex" style="height: 30px;width : ' + total_width + 'px">' +
                        createMenuBtn("bNav bPrev ml-2 ml-md-0", "general.btn.prev", "fa fa-arrow-left", true, true) +
                        createMenuBtn("bZoom bZoomIn", "general.btn.zoomin", "fa fa-search-plus", true, true) +
                        '<div class="flex-grow-1"></div>' +
                        createMenuBtn("bZoom bZoomOut", "general.btn.zoomout", "fa fa-search-minus", true, false) +
                        createMenuBtn("bNav bNext mr-2 mr-md-0", "general.btn.next", "fa fa-arrow-right", false, false) +
                        '</div>';
                    subHeaderY2.append(menu);
                }
            }

            let year_header = $('<div class="d-flex" style="height: 30px;position:relative;width : ' + (total_width + (actorNewLine ? 0 : (box_width / 2))) + 'px"></div>').appendTo(subHeaderY2);

            for (let iHeader = 0; iHeader < nbColumns; iHeader++) {
                let boxWidth = tooMuchColumns ? box_width*2 : box_width;
                let yearToDisplay = !tooMuchColumns || iHeader%2 === 0 ? boxWidth < 50 && year.toString().length === 4 ? year.toString().substring(2) : year : '';

                year_header.append('<div style="width: ' + (box_width) + 'px;text-align: center;">' + yearToDisplay + '</div>');

                year += year_by_column;
            }

            createChartActors(chart_data, table, actorNewLine ? table_width : title_width);
            createChartDurations(chart_data, title_width + (actorNewLine ? -1 : + 1), table_width, table_subcontainer);

            if(tooMuchColumns) {
                table.addClass('organigram-reduced');
            }
            table_subcontainer.prop("scrollTop", 0);
            scroll = true;
        }
    }

    function createChartActors(chart_data, table, title_width){

            for (let iData in chart_data) {
                fillDataTitle(chart_data[iData].label, chart_data[iData].btn, table);
                if(chart_data[iData].affiliations.length > 0) {
                    fillDataActors(chart_data[iData].affiliations, table, title_width);
                } else {
                    $('<tr><td style="height : ' + eOptions.box_height + 'px;"></td></tr>').appendTo(table);
                }
            }

        addActorsColumns(table);
    }

    function addActorsColumns(table){
        table.find("tr").each(function () {
            if (!$(this).hasClass("no-highlight-line"))
                addColumns($(this));
        });
    }

    function createChartDurations(chart_data, title_width, table_width, table_subcontainer){
        let chartTop = 20 + (actorNewLine ? eOptions.box_height : 0);

        let index = 1;
        let endDate = new Date(end_date.getTime());
        endDate.setFullYear(max_year + Math.floor(year_by_column / 2));

        for (let iData in chart_data) {
            let obj = fillDataDurations(chart_data[iData].affiliations, chartTop + (eOptions.box_height), title_width, table_width, table_subcontainer, index, endDate);
            chartTop = obj.top + (chart_data[iData].affiliations.length === 0 ? eOptions.box_height : 0);
            index = obj.index + 1;
        }

    }

    function cloneOptions(){
        return {
            year_scale : eOptions.year_scale,
            box_height : eOptions.box_height,
            nb_columns : eOptions.nb_columns,
            year_step : eOptions.year_step,
            year_scale_max : eOptions.year_scale_max,
            year_scale_min : eOptions.year_scale_min,
            endYear : eOptions.endYear,
            beginYear : eOptions.beginYear,
            separatedChart : eOptions.separatedChart,
            scrollAnimation: eOptions.scrollAnimation,
            navbar : eOptions.navbar,
            completeAffiliation : eOptions.completeAffiliation,
            affiliatedId : eOptions.affiliatedId
        };
    }

    function createMenuBtn(classes, message, logo, logoLeft){
        let content = logoLeft ? '<i class="' + logo + ' btn-logo"></i>&nbsp;<span class="btn-text d-none d-md-inline">' + Messages(message) + '</span>&nbsp;&nbsp;' :
            '<span class="btn-text d-none d-md-inline">' + Messages(message) + '</span>&nbsp;<i class="' + logo + ' btn-logo"></i>&nbsp;';
        return '<button type="button" class="btn btn-link btn-org-chart ' + classes + ' primary">' + content + '</button>';
    }

    function fillDataTitle(title, btn_id, table_titles){
        let line = $('<tr class="highlight-line-standing no-highlight-line"></tr>').appendTo(table_titles);
        line.append('<td colspan="' + (nbColumns + 1) + '" style="height : ' + eOptions.box_height + 'px;padding-left : 10px;">' +
            '<h4 class="d-flex align-items-center justify-content-between mb-0">' + title +
            '<button type="button" class="btn btn-link p-0 mr-3 ' + btn_id + '" style="font-size : 24px">' +
            '   <i class="fas fa-plus-square"></i> <span class="d-none d-lg-inline">' + Messages('general.add.btn') + '</span>' +
            '</button></h4>' +
            '</td>');
    }

    function fillDataActors(actors, table_titles, title_width){
        for (let iLine = 0; iLine < actors.length; iLine++) {
            fillDataActor(actors[iLine], table_titles, title_width);
        }
    }

    function fillDataActor(actor, table_titles, title_width){
        let line = $("<tr></tr>").appendTo(table_titles);

        let box = $('<td ' + (actorNewLine ? 'colspan="' + (nbColumns + (actorNewLine ? 2 : 1)) + '"' : '') + '></td>').appendTo(line);
        box.append(actor.display(eOptions.box_height, title_width));
    }

    function addColumns(line){
        let newLine = actorNewLine ?
            $('<tr style="height : ' + eOptions.box_height + 'px;"></tr>').insertAfter(line):
            line;

        for (let iElement = 0; iElement < nbColumns + (actorNewLine ? 1 : 0); iElement++) {
            newLine.append('<td></td>');
        }
    }

    function fillDataDurations(actors, top, left, table_width, line_container, index, endDate){
        let nbMonths = monthDiff(begin_date, endDate);

        for(let iActor in actors) {
            fillDataDuration(actors[iActor], top, left, table_width, line_container, index, nbMonths, endDate);
            top += (eOptions.box_height * (actorNewLine ? 2 : 1));
            index++;
        }

       return {top, index};
    }

    function fillDataDuration(actor, top, left, table_width, line_container, index, nbMonths, endDate){
        for(let iAffiliation in actor.affiliations) {
            let affiliation = actor.affiliations[iAffiliation];
            let prevAff = actor.affiliations[iAffiliation - 1];

            if (affiliation.isDefined()) {
                let el = $('<div class="organigram-line" data-index="' + (actorNewLine ? index*2 : index) + '"></div>').prependTo(line_container);

                let line_width = table_width / (nbMonths / affiliation.monthDiff(begin_date, endDate));
                let line_width_calc = line_width;

                let line_left = affiliation.dateBegin instanceof Date ?
                    table_width / (nbMonths / monthDiff(begin_date, affiliation.dateBegin)) :
                    (table_width / (nbMonths / monthDiff(begin_date, affiliation.dateEnd))) - eOptions.min_box_width;

                if (line_width < eOptions.min_box_width && !affiliation.hasSameDurationAs(prevAff)) {
                    //line_width = eOptions.min_box_width - (affiliation.isBeginUnknown() ? 2 : 0) - (affiliation.isEndUnknown() ? 2 : 0);
                    line_width_calc = eOptions.min_box_width - (affiliation.isEndUnknown() ? 2 : 0);
                } else {
                   // line_width -= (affiliation.isBeginUnknown() ? 5 : 0) - (affiliation.isEndUnknown() ? 5 : 0);
                    line_width_calc -= (affiliation.isEndUnknown() ? 5 : 0);
                }

                let el2 = $('<div class="organigram-linecontent' + affiliation.othersClasses(line_width) + '" title="' + affiliation.duration() + '"></div>').appendTo(el);
                el2.width(line_width_calc + 'px');
                el2.height('30px');
                el2.css('top', top + 'px');
                el2.css('left', (left + line_left) + 'px');
            }
        }
    }

    function computeNbColumns(table_width, year_diff){
        let nbColumns = null;

        if(table_width / eOptions.nb_columns < 50){
            nbColumns = Math.floor(table_width / 50);
        }

        nbColumns = nbColumns == null ? eOptions.nb_columns : nbColumns;
        nbColumns = eOptions.year_scale < nbColumns ? eOptions.year_scale : nbColumns;

        let comma = computeYearByColumnAfterDot(nbColumns, year_diff);
        if(comma !== 0) {

            let val1 = searchBestNbOfColumns(nbColumns, year_diff, false);
            let val2 = searchBestNbOfColumns(nbColumns, year_diff, true);

            return val1 != null && val2 != null ?
                        val1.val < val2.val ?
                                val1.val < comma ? nbColumns + val1.i : nbColumns :
                                val2.val < comma ? nbColumns - val2.i : nbColumns :
                        nbColumns;
        }

        return nbColumns;
    }

    function searchBestNbOfColumns(nbColumns, year_diff, minus){
        let values = [];
        let i;

        for(i = 0; i < 5 && nbColumns - i > 0; i++) {
            values.push({val : computeYearByColumnAfterDot(nbColumns + (minus ? -i : i), year_diff), i : i});
        }

        return values.length === 0 ? null : values.sort((a, b) => a.val === b.val ? a.i - b.i : a.val - b.val)[0];
    }

    function computeYearByColumnAfterDot(nbColumns, year_diff) {
        return (year_diff / nbColumns) - Math.floor(year_diff / nbColumns);
    }

    function monthDiff(d1, d2) {
        let months;
        months = (d2.getFullYear() - d1.getFullYear()) * 12;
        months -= d1.getMonth();
        months += d2.getMonth();
        return months <= 0 ? 0 : months;
    }

    function isDate(date) {
        return date instanceof Date;
    }

    class Actor {
        constructor(id, name, avatar, affiliations){
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.affiliations = this.sortAffiliations(affiliations);
        }

        sortAffiliations(affiliations){
            return affiliations.sort(function(a, b) {
                return !isDate(a.dateBegin) ? 1 : !isDate(b.dateBegin) ? -1 :
                    a.dateBegin.getTime() < b.dateBegin.getTime() ? -1 :
                    a.dateBegin.getTime() > b.dateBegin.getTime() ? 1 :
                    a.monthDiff() - b.monthDiff();
            });
        }

        display(height, width) {
            return $('<a class="d-flex align-items-center color-grey-dark actor-thumbnail chart ' + (actorNewLine ? 'bordered' : '') + '" ' +
                'href="' + urlOfActorViz(this.id, 2) + '" title="' + this.name + '" ' +
                'style="height : ' + height + 'px;' + (actorNewLine ? '' : 'width : '+ width + 'px') + '">' +
                '<div style="width:10px"></div>' +
                '<div class="thumbnail-holder" style="background-color: white;background-image: url(' + this.avatar + ');"></div>' +
                '<div class="text-truncate pl-2" style="width : ' + (width - 45) + 'px">' + this.name + '</div></a>');
        }
    }

    class Affiliation {
        constructor(aff, affiliatedId, affiliatedName){
            this.type = aff.affType;
            this.typeName = aff.affTypeName;
            this.profession = aff.function;
            this.affiliatedId = affiliatedId;
            this.affiliatedName = affiliatedName;
            this.dateBegin = stringToDate(aff.from);
            this.dateBeginTypeId = aff.fromTypeId;
            this.dateBeginTypeName = aff.fromTypeName;
            this.dateEnd = stringToDate(aff.to);
            this.dateEndTypeId = aff.toTypeId;
            this.dateEndTypeName = aff.toTypeName;
        }

        hasSameDurationAs(affiliation){
           return affiliation !== undefined && isDate(this.dateBegin) && isDate(affiliation.dateBegin)
               && this.dateBegin.getTime() - affiliation.dateBegin.getTime() <= 1;
        }

        monthDiff(calendar_begin_date, calendar_end_date) {

            let from = this.getDefaultDate(this.dateBegin, this.dateEnd, 5);
            let to = this.getDefaultDate(this.dateEnd, this.dateBegin, -5);

            if(from != null && to != null) {
                return monthDiff(from < calendar_begin_date ? calendar_begin_date : from,
                    to > calendar_end_date ? calendar_end_date : to);
            }
            return 0;
        }

        getDefaultDate(date, otherDate, diff){
            if(date instanceof Date){
                return date;
            }
            if(otherDate instanceof Date){
                date = new Date();
                date.setFullYear(otherDate.getFullYear() - diff);
                return date;
            }
            return null;
        }

        isDefined(){
            return (isDate(this.dateBegin) || isDate(this.dateEnd)) &&
                (!(isDate(this.dateBegin)) || this.dateBegin < end_date) &&
                (!(isDate(this.dateBegin)) || this.dateEnd > begin_date)
        }

        duration(){
            let title = (this.typeName ? this.typeName : '') + (eOptions.completeAffiliation && this.affiliatedName ? this.affiliatedName + " - " : "") + (this.profession != null ? this.profession : '');
            let dates = (this.dateBeginTypeName ? this.dateBeginTypeName + " " : "") +
                this.displayDate(this.dateBegin) + " - " +
                (this.dateEndTypeName ? this.dateEndTypeName + " " : "")+
                (this.dateEndTypeId === 5 ? '' : this.displayDate(this.dateEnd));
            return title + " ( " + dates + " )";
        }

        othersClasses(line_width){
            let ch = "";

            ch += this.isBeginUnknown() ? " organigram-linecontent-unkownbegin" + (line_width <= eOptions.min_box_width ? ' small' : '') : "";
            ch += this.isEndUnknown() ? " organigram-linecontent-unkownend" + (line_width <= eOptions.min_box_width ? ' small' : '')  : "";
            ch += eOptions.affiliatedId && eOptions.affiliatedId !== this.affiliatedId ? " organigram-linecontent-dissimilar" : "";

            return ch;
        }

        isBeginUnknown() {
            return this.dateBeginTypeId === 1;
        }

        isEndUnknown() {
            //return this.dateEndTypeId === 3 || this.dateEndTypeId === 5;
            return this.dateEndTypeId === 5;
        }

        displayDate(date){
            return date instanceof Date ?
                this.displayZero(date.getDate()) + "/" + this.displayZero(date.getMonth() + 1) + "/" + date.getFullYear()
                : Messages("label.unknown.f");
        }

        displayZero(number){
            return (number <= 9 ? "0" : "") + number;
        }
    }

    function toActors(actors_obj){
        let actors = [];
        let minYear = null;
        let maxYear = null;

        actors_obj.forEach(actor => {
            let affiliations = [];

            actor.affiliations.forEach(affiliation => {
                let aff = new Affiliation(affiliation, actor.id, actor.name);

                minYear = aff.dateBegin != null && (minYear == null || minYear.getTime() > aff.dateBegin.getTime()) ? aff.dateBegin : minYear;
                maxYear = aff.dateEnd != null && (maxYear == null || maxYear.getTime() < aff.dateEnd.getTime()) ? aff.dateEnd : maxYear;

                affiliations.push(aff);
            });

            actors.push(new Actor(actor.id, actor.jsonName, actor.avatar, affiliations));
        });

        if(maxYear != null)
            eOptions.endYear = maxYear.getFullYear() + 1;

        updateDateOptions(minYear);

        return actors;
    }

    function treatData(filters){
        return new Promise((resolve, reject) => {
            displayWaitForIt(true);

            if (eOptions.toCall != null) {
                eOptions.toCall(data.id, data.viewBy, filters).done(function (res) {
                    chart_data = res.map(d => ({label: d.label, btn: d.btn, affiliations: toActors(d.affiliations)}));

                    displayWaitForIt(false);
                    resolve();
                }).fail(function () {
                    console.log('error affiliations.');
                    displayWaitForIt(false);
                    reject();
                });
            } else {
                chart_data = data.map(d => ({label: d.label, btn: d.btn, affiliations: toActors(d.affiliations)}));

                displayWaitForIt(false);
                resolve();
            }
        });
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

    init();


};}(jQuery));