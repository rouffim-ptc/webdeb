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

/*!
 * annotator v1.0 is a jQuery extension to highlight excerpt in a text
 *
 * Includes jquery.js
 * https://jquery.com/
 */
(function(e) { e.fn.annotator = function (data, link, targetBlank) {

    let container = $(this),
        container_excerpt,
        excerpts,
        dataExcerptKey = "excerpt",
        excerptHighlightedClassName = "excerpt-highlight",
        closeResultClassName = "close-result",
        resultContainerClassName = "excerpt-highlight-result-container",
        excerptHighlightedHoverClassName = excerptHighlightedClassName + "-hover";

    $(document).ready(function() {
        init();
    });

    function init(){
        initContainer();
        createResultContainer();
        initData();
        highlightExcerpts();
        initListeners();
    }

    function initContainer(){
        let tmp = container;
        let div = tmp.children('div');
        while(div.length > 0) {
            tmp = div.eq(0);
            div = tmp.children('div');
        }

        container = tmp;
    }

    function createResultContainer(){
        container_excerpt = $('<div class="' + resultContainerClassName + '" style="display: none"></div>').prependTo("body");
        let container = $('<div class="' + resultContainerClassName + '-result"><div>').appendTo(container_excerpt);
        container.append('<div><h4 style="padding-left:15px;text-decoration: underline">Citation(s) extraites de : </h4>' +
            '<h5 style="padding-left:22px;">"<span class="original_excerpt"></span>"</h5><hr></div>');
        container.append('<div class="excerpts"></div>');
        container.append('<div><button type="button" class="btn pull-right ' + closeResultClassName + '">Fermer</button></div>');

        return container;
    }

    function initListeners(){
        /*
         *
         */
        container.on("mouseover", asClassSelector(excerptHighlightedClassName), function(e){
            e.stopImmediatePropagation();
            let allExcerpts = container.find(asClassSelector(excerptHighlightedClassName));
            let idsSelected = getExcerptNodeIds($(this));

            if(idsSelected != null) {

                $.each(allExcerpts, function (index, excerpt) {
                    excerpt = $(excerpt);
                    excerpt.toggleClass(excerptHighlightedHoverClassName, nodeContainsId(excerpt, idsSelected));
                });

                $(this).on("mouseleave", function () {
                    container.find(asClassSelector(excerptHighlightedClassName)).removeClass(excerptHighlightedHoverClassName);
                });
            }
        });

        container.on("click", asClassSelector(excerptHighlightedClassName), function(e){
            e.stopImmediatePropagation();
            let idsSelected = getExcerptNodeIds($(this));

            if(idsSelected != null && idsSelected[0] !== undefined && excerpts[idsSelected[0]] !== undefined) {

                container_excerpt.find(".original_excerpt").html(excerpts[idsSelected[0]].originalExcerpt);
                let content = container_excerpt.find(".excerpts");
                let nbElements = 0;
                content.empty();

                for(let iSelected in idsSelected){
                    let excerpt = excerpts[idsSelected[iSelected]];
                    if(excerpt !== undefined) {
                        content.append(excerpt.html());
                        content.append("<hr>");
                        nbElements++;
                    }
                }


                let width = $(window).width() * 0.75;
                container_excerpt.css("top", e.pageY + "px");
                container_excerpt.css("left", (($(window).width() - width) / 2) + "px");
                content.width(width);
                container_excerpt.show();
            }
        });

        $(document).on("click", asClassSelector(closeResultClassName), function(e){
            container_excerpt.hide();
        });
    }

    function initData(){
        excerpts = [];

        for(let iData in data){
            let excerpt = data[iData];
            excerpts[excerpt.id] = new Excerpt(excerpt.id, excerpt.textId, excerpt.originalExcerpt.trim(), excerpt.workingExcerpt);
        }
    }

    function normalizeText(text){
        return (text.replace(/\s\s+/g, ' ').trim() + " ");
    }

    function highlightExcerpts(){
        container = convertContainer(container);
        let nodes = container[0].childNodes;
        let text = normalizeText(container.text());

        for(let iExcerpt in excerpts) {
            let excerpt = excerpts[iExcerpt];
            let index = text.indexOf(excerpt.originalExcerpt);

            if(index > -1)
                highlightExcerpt(nodes, excerpt, index, 0, true);
        }

        differentiateEmbedded();
    }

    function convertContainer(container){
        let nodes = container[0].childNodes;

        for(let iNode in nodes) {
            let node = nodes[iNode];

            if(node.nodeType === 3){
                $(node).replaceWith('<span>' + node.textContent + '</span>')
            }
        }

        return container;
    }

    function highlightExcerpt(nodes, excerpt, indexToReach, currentIndex, isBegin) {

        for (let iNode in nodes) {
            let node = nodes[iNode];

            if(node.nodeType === 3) {
                let text = normalizeText(node.textContent);

                if (currentIndex + text.length >= indexToReach) {
                    if (isBegin) {
                        if($(node).parent().hasClass(excerptHighlightedClassName) && excerpts[getExcerptNodeIds($(node).parent())[0]].originalExcerpt === excerpt.originalExcerpt){
                            addDataToNode($(node).parent(), dataExcerptKey, excerpt.id);
                            return -1;
                        }else {
                            let begin_cut = indexToReach - currentIndex;
                            begin_cut = begin_cut < 0 ? 0 : begin_cut;
                            let right_part = text.substr(begin_cut, text.length);
                            let indexInclusion = checkTextInclusion(excerpt.originalExcerpt, right_part);

                            if (indexInclusion > -1) {
                                if (indexInclusion > 0) {
                                    begin_cut += indexInclusion;
                                    right_part = text.substr(begin_cut, text.length);
                                }

                                indexToReach = excerpt.originalExcerpt.length;
                                node.textContent = text.substr(0, begin_cut);

                                let span = createHighlightSpan(excerpt, $(node), (right_part.length < indexToReach ?
                                    right_part : right_part.substr(0, indexToReach))).insertAfter(node);

                                if (right_part.length >= indexToReach) {
                                    $(document.createTextNode(right_part.substr(indexToReach, right_part.length))).insertAfter(span);
                                    return -1;
                                }

                                currentIndex = right_part.length;
                                isBegin = false;
                            } else {
                                currentIndex += text.length;
                            }
                        }
                    }else{
                        let cut = indexToReach - currentIndex;
                        node.textContent = text.substr(cut, node.textContent.length);
                        createHighlightSpan(excerpt, $(node), text.substr(0, cut)).insertBefore(node);

                        return -1;
                    }
                }else{
                    currentIndex += text.length;
                }
            }else if(node.nodeType === 1 && node.nodeName === "SPAN") {
                let element = $(node);

                if (!isBegin && currentIndex <= indexToReach  && currentIndex + element.text().length <= indexToReach) {
                    element.toggleClass(excerptHighlightedClassName, true);
                    addDataToNode(element, dataExcerptKey, excerpt.id);
                    currentIndex += normalizeText(element.text()).length;
                }else {
                    let r = highlightExcerpt(node.childNodes, excerpt, indexToReach, currentIndex, isBegin);
                    if(Array.isArray(r)){
                        currentIndex = r[0];
                        indexToReach = r[1];
                        isBegin = r[2];
                    }else{
                        return -1;
                    }
                }
            }
        }

        // not found
        return [currentIndex, indexToReach, isBegin];
    }

    function checkTextInclusion(textToFound, textToAnalyse){
        textToFound = textToAnalyse.length < textToFound.length ? textToFound.substr(0, textToAnalyse.length) : textToFound;
        textToAnalyse = textToAnalyse.length > textToFound.length ? textToAnalyse.substr(0, textToFound.length) : textToAnalyse;

        for (var i = 0; i < textToAnalyse.length && i < textToFound.length; i++) {
            if(textToFound === textToAnalyse)
                return i;

            textToAnalyse = textToAnalyse.substr(1);
            textToFound = textToFound.substr(0, textToFound.length - 1);
        }

        return -1;
    }

    function createHighlightSpan(excerpt, currentNode, text){
        let idExcerpt = currentNode.parent().hasClass(excerptHighlightedClassName)
                        && currentNode.parent().data(dataExcerptKey) !== undefined ?
                        currentNode.parent().data(dataExcerptKey) + ";" + excerpt.id : excerpt.id;
        return $('<span class="' + excerptHighlightedClassName + '" data-' + dataExcerptKey + '="' + idExcerpt + '">' + text + '</span>');
    }

    function addDataToNode(node, dataKey, dataValue){
        let currentValue = node.data(dataKey) === undefined ? null : String(node.data(dataKey));
        node.data(dataKey, (currentValue == null || currentValue.length === 0) ? dataValue : currentValue + ";" + dataValue);
    }

    function getExcerptNodeIds(node){
        return node.data(dataExcerptKey) === undefined ? null : String(node.data(dataExcerptKey)).split(";");
    }

    function nodeContainsId(node, idsToCheck){
        let ids = getExcerptNodeIds(node);

        if(ids != null) {
            for (let i in idsToCheck) {
                if (ids.includes(idsToCheck[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    function differentiateEmbedded(){
        searchForEmbedded(container,0);
    }

    function searchForEmbedded(node, depth){
        node.children("span").each(function (index, element) {
            element = $(element);
            let subdepth = depth;
            if ( element.hasClass(excerptHighlightedClassName)) {
                let darker = 1.10 * depth;
                darker = darker ===0 ? 1 : darker;
                let rgb = element.css("background-color");
                rgb = rgb.substr(4, rgb.length - 5);
                rgb = rgb.split(', ');
                let new_rgb = "rgb(" + Math.round(parseInt(rgb[0]) * darker) + ", " + Math.round(parseInt(rgb[1]) * darker) + ", " + Math.round(parseInt(rgb[2]) * darker) + ")";
                element.css("background-color", new_rgb);
                subdepth += 1;
            }
            searchForEmbedded(element, subdepth);
        });
    }

    function asClassSelector(name){
        return '.' + name;
    }

    function asIdSelector(name){
        return '#' + name;
    }

    class Excerpt{
        constructor(id, textId, original_excerpt, working_excerpt){
            this.id = id;
            this.textId = textId;
            this.originalExcerpt = original_excerpt;
            this.workingExcerpt = working_excerpt;
            this.vizLink = link + "/viz/text?id=" + this.textId + "&pane=3";
            this.editLink = link + "/entry/excerpt?textId=" + this.textId + "&excId=" + this.id;
        }

        html(){
            return '<div class="flex-container" style="padding-left:15px"><div style="flex-grow: 1">' +
                '<i class="fas fa-align-left"></i>&nbsp;' +
                '<a class="primary" href="' + this.vizLink + '" ' + (targetBlank ? 'target="_blank"' : '') + '>' + this.workingExcerpt + '</a></div>' +
                '<div>&nbsp;<a class="primary" href="' + this.vizLink + '" ' + (targetBlank ? 'target="_blank"' : '') + '><i class="fas fa-eye"></i></a>' +
                '&nbsp;<a class="primary" href="' + this.editLink + '" ' + (targetBlank ? 'target="_blank"' : '') + '><i class="fas fa-pencil-alt"></i></a>&nbsp;&nbsp;&nbsp;' +
                '</div></div>';
        }
    }

};}(jQuery));