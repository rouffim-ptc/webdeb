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
 * organizationalChartJs v1.0 is a jQuery extension to get a pdf content as HTML from URL
 *
 * Includes jquery.js
 * https://jquery.com/
 *
 * @author Martin Rouffiange
 */
(function(e) { e.fn.pdfLoaderJs = function (options) {

    let eOptions,
        container = $(this),
        containerOptions = null,
        containerResult = null,
        currentPDFURL = null,
        currentPDF = null,
        timeoutId = null;

    $(document).ready(function() {
        initOptions();
        initContainers();
        initListeners();
    });

    function initOptions(){
        // handle passed options to pdfLoaderJs

        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                fromPage : isNaN(options.fromPage) || options.fromPage <= 0 ? null : options.fromPage,
                toPage : isNaN(options.toPage) || eOptions.fromPage > options.toPage ? null : options.toPage,
                exclusion : {
                    up : typeof options.exclusion === 'object' && !isNaN(options.exclusion.up) && options.exclusion.up <= 1 && options.exclusion.up > 0 ? options.exclusion.up : null,
                    down : typeof options.exclusion === 'object' && !isNaN(options.exclusion.down) && options.exclusion.down <= 1 && options.exclusion.down > 0 ? options.exclusion.down : null,
                    right : typeof options.exclusion === 'object' && !isNaN(options.exclusion.right) && options.exclusion.right <= 1 && options.exclusion.right > 0 ? options.exclusion.right : null,
                    left : typeof options.left === 'object' && !isNaN(options.exclusion.left) && options.exclusion.left <= 1 && options.exclusion.left > 0 ? options.exclusion.left : null,
                },
                notVertical : typeof(options.notVertical) === "boolean" ? options.notVertical : true,
                keepOriginalWidth : typeof(options.keepOriginalWidth) === "boolean" ? options.keepOriginalWidth : false,
                displayByVerticalPos : typeof(options.displayByVerticalPos) === "boolean" ? options.displayByVerticalPos : true,
                textJustify : typeof(options.textJustify) === "boolean" ? options.textJustify : false,
                displayCentered : typeof(options.displayCentered) === "boolean" ? options.displayCentered : true,
                heightSameParagraph : typeof !isNaN(options.heightSameParagraph) && options.heightSameParagraph <= 1 && options.heightSameParagraph > 0 ? options.heightSameParagraph : 0.1,
                heightNewParagraph : typeof !isNaN(options.heightNewParagraph) && options.heightNewParagraph > 0 ? options.heightNewParagraph : 1.5
            };
        } else {
            eOptions = {
                fromPage : null,
                toPage : null,
                exclusion : {
                    up : 0.08,
                    down : 0.08,
                    right : 0.04,
                    left : 0.04,
                },
                notVertical : true,
                keepOriginalWidth : false,
                displayByVerticalPos : true,
                textJustify : false,
                displayCentered : true,
                heightSameParagraph : 0.1,
                heightNewParagraph : 1.5
            };
        }

        pdfjsLib.GlobalWorkerOptions.workerSrc = '/assets/javascripts/pdf.worker.js';

        return eOptions;
    }

    function initContainers(){
        containerOptions = $('<div></div>').appendTo(container);
        $('<div>' +
            '<div class="form-group mt-2">' +
            '   <label for="pdf-url">URL du pdf : </label>' +
            '   <button type="button" class="btn btn-link pdf-see" title="Voir le pdf"><i class="fas fa-eye"></i></button>' +
            '   <input id="pdf-url" type="text" class="form-control" value="' + window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/assets/1.pdf">' +
            '</div>' +
            '<div class="form-group">' +
            '   <input type="file" id="pdf-file" accept="application/pdf" />' +
            '</div>' +
            '<hr>' +
            '<div class="d-flex mt-2">' +
            '<div class="form-group">' +
            '   <label for="pdf-from-page">Page de début : </label>' +
            '   <input id="pdf-from-page" type="number" class="form-control pdf-zero" step="1" min="0" value="' + eOptions.maxPages + '">' +
            '</div>' +
            '<div class="form-group">' +
            '   <label for="pdf-to-page">Page de fin : </label>' +
            '   <input id="pdf-to-page" type="number" class="form-control pdf-zero" step="1" min="0" value="' + eOptions.maxPages + '">' +
            '</div>' +
            '<div class="d-flex flex-column border border-right-0 border-top-0 border-bottom-0 ml-4 pl-4">' +
            createOptionCheckbox('notVertical', "Exclure les textes de travers") +
            createOptionCheckbox('displayByVerticalPos', "Afficher dans ordre d'affichage (du plus haut élément au plus bas)") +
            createOptionCheckbox('keepOriginalWidth', "Garder la largeur original du texte") +
            createOptionCheckbox('textJustify', "Texte justifié", 'keepOriginalWidth') +
            createOptionCheckbox('displayCentered', "Centrer le texte", 'keepOriginalWidth') +
            '</div>' +
            '</div>' +
            '<div class="mt-2">Zone d\'importation</div>' +
            '   <div class="d-flex flex-column justify-content-center" style="width:280px">' +
                    createPDFExclusionInput('up') +
            '       <div class="d-flex align-items-center">' +
                        createPDFExclusionInput('left') +
            '           <div class="d-flex align-items-center">' +
            '               <div class="rect-example" style="height:126px;width:88px"><div></div></div>' +
            '               <div class="rect-example" style="height:88px;width:126px;"><div></div></div>' +
            '           </div>' +
                        createPDFExclusionInput('right') +
            '       </div>' +
                    createPDFExclusionInput('down') +
            '   </div>' +
            '</div>' +
            '<hr>' +
            '<div class="d-flex">' +
            '<button type="button" class="btn btn-primary load-pdf">Importer</button>' +
            '<div class="pdf-go-page-container" style="display: none">' +
            '<div class="d-flex align-items-center ml-4">' +
            '   <label for="pdf-to-page" class="col-form-label text-nowrap">Aller à la page : </label>' +
            '   <input id="pdf-to-page" type="number" class="form-control ml-2" step="1" min="1" value="1">' +
            '   <button type="button" class="btn btn-secondary pdf-to-page-btn">Y aller</button>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '<hr>' +
        '</div>').appendTo(containerOptions);

        showExclusionZone();

        containerResult = $('<div class="pdf-result-container"></div>').appendTo(container);
    }

    function createOptionCheckbox(option, message, displayedBy){
        let dataOption = displayedBy ? 'data-displayed-by="' + displayedBy + '"' : '';

        return '<div class="form-check ' + (displayedBy ? 'ml-4' : '') + '" ' + (displayedBy ? 'style="display : none"' : '') + '>' +
            '    <input type="checkbox" class="form-check-input pdf-boolean" ' + dataOption + ' id="' + option + '" value="' + eOptions[option] + '" ' + (eOptions[option] ? 'checked' : '') + '>' +
            '    <label class="form-check-label" for="' + option + '">' + message + '</label>' +
            '    <hr class="hr-small w-100">' +
            '</div>';
    }

    function createWaitSpinner(){
        return '<span class="fa fa-spinner fa-spin"></span>&nbsp;' + Messages("general.dialog.wait");
    }

    function createPDFExclusionInput(direction){
        let isLateral = directionIsLateral(direction);
        let directions = isLateral ? ["left", "right"] : ["up", "down"];
        let arrows = directions.map(dir =>
           '<button type="button" class="btn btn-link btn-link-osimple pdf-exclusion p-1" data-direction="' + direction + '" data-toadd="' + (direction !== dir) + '">' +
               '<i class="fas fa-arrow-circle-' + dir + '"></i>' +
               '</button>'
        ).join('');

        return '<div class="d-flex ' + (isLateral ? 'flex-column' : '') + ' align-items-center justify-content-center">' +
            arrows +
            '    <input type="number" class="form-control d-none" data-direction="' + direction + '" step="0.1" max="100" min="0.1" value="' + (eOptions.exclusion[direction] * 100) + '">' +
            ' </div>'
    }

    function directionIsLateral(direction){
        return direction === "right" || direction === "left";
    }

    function initListeners(){

        containerOptions.on('mousedown', '.pdf-exclusion', function(){
            doExclusion($(this));
        });

        containerOptions.on('mouseup', '.pdf-exclusion', function(){
            clearTimeout(timeoutId);
        });

        containerOptions.on('click keyup', '.pdf-zero', function(){
            let val = parseInt($(this).val());
            let value = val === 0 || isNaN(val) ? null : val;

            if($(this).is('#pdf-from-page')){
                eOptions.fromPage = value == null || (eOptions.toPage != null && value > eOptions.toPage) ? null : value;
                $(this).val(eOptions.fromPage != null ? eOptions.fromPage : '');
            } else {
                eOptions.toPage = value == null || (eOptions.fromPage != null && value < eOptions.fromPage) ? null : value;
                $(this).val(eOptions.toPage != null ? eOptions.toPage : '');
            }
        });

        containerOptions.on('click', '.pdf-see', function(){
            window.open(fileIsPDF(containerOptions.find('#pdf-file')) ? getPDFFileURL(containerOptions.find('#pdf-file')) : containerOptions.find('#pdf-url').val());
        });

        containerOptions.on('click', '.pdf-boolean', function(){
            let idOption = $(this).attr('id');
            eOptions[idOption] = $(this).is(':checked');

            let elementsToDisplay = containerOptions.find('[class*="pdf-boolean"][data-displayed-by="' + idOption + '"]');
            if(elementsToDisplay.exists()){
                elementsToDisplay.parent().toggle($(this).is(':checked'));
            }
        });

        containerOptions.on('click', '.load-pdf', function(){
            displayPDFAsHTML(fileIsPDF(containerOptions.find('#pdf-file')) ? getPDFFileURL(containerOptions.find('#pdf-file')) : containerOptions.find('#pdf-url').val());
        });

        containerOptions.on('click', '#pdf-url', function(){
            containerOptions.find('#pdf-file').val('');
        });

        containerOptions.on('click', '#pdf-file', function(){
            containerOptions.find('#pdf-url').val('');
        });

        containerOptions.on('click', '.pdf-to-page-btn', function(){
            let url = location.href;
            location.href = "#pdf-page-" + $('#pdf-to-page').val();
            history.replaceState(null,null,url);
        });

    }

    function doExclusion(element) {
        let direction = element.data('direction');
        let toadd = element.data('toadd');
        let input = element.siblings('input');

        if((toadd && parseInt(input.attr("max")) > parseInt(input.val()))
            || (!toadd && parseInt(input.attr("min")) < parseInt(input.val()))) {
            eOptions.exclusion[direction] += parseFloat((toadd ? '+' : '-') + (parseFloat(input.attr("step")) / 100));
        }

        showExclusionZone();

        timeoutId = setTimeout(function() {
            doExclusion(element);
        }, 5);
    }

    function getPDFFileURL(input){
        let file = getPDFFileFromInput(input);
        return file != null ? URL.createObjectURL(file) : '';
    }

    function getPDFFileFromInput(input){
        return input.exists() ? input[0].files[0] : null;
    }

    function fileIsPDF(input){
        let file = getPDFFileFromInput(input);
        return file != null && ['application/pdf'].indexOf(file.type) !== -1;
    }

    function showExclusionZone() {
        $('.rect-example').each(function(){
            let div = $(this).children('div');

            let width = $(this).width();
            let height = $(this).height();

            div.width(width);
            div.height(height);

            let newWidth = width - (width * eOptions.exclusion['left']);
            div.css("left", Math.round(width - newWidth) + 'px');
            div.width(newWidth - (width * eOptions.exclusion['right']));

            let newHeight = height - (height * eOptions.exclusion['up']);
            div.css("top", (height - newHeight) + 'px');
            div.height(newHeight - (height * eOptions.exclusion['down']));

        });
    }

    function displayPDFAsHTML(url){
        let goToPageContainer = $('.pdf-go-page-container');
        goToPageContainer.hide();
        containerResult.css('display', 'block');
        containerResult.html(createWaitSpinner());

        getPDFAsHtml(url).then(function (pdfAsHtml) {
            containerResult.html(pdfAsHtml);

            if(eOptions.keepOriginalWidth && eOptions.textJustify){
                containerResult.find('.pdf-paragraph').each(function(){
                    $(this).width($(this).width() + 1);
                    $(this).find('br').remove();
                    $(this).css('text-align', 'justify');
                    $(this).css('display', 'block');
                });
            }

            if(eOptions.keepOriginalWidth && eOptions.displayCentered)
                containerResult.css('display', 'flex');

            goToPageContainer.find('input').attr('max', containerResult.find('.pdf-page').length);
            goToPageContainer.show();
        }, function (reason) {
            console.error(reason);
            containerResult.html('<p>Error with given PDF</p>');
        });
    }

    /**
     * Get a pdf content as HTML from URL
     *
     * @author Martin Rouffiange
     */
    function getPDFAsHtml(pdfUrl, options){
        currentPDF = currentPDFURL !== pdfUrl ? pdfjsLib.getDocument(pdfUrl) : currentPDF;

        // get all pages text
        return currentPDF.promise.then(function(pdf) {
            let fromPage = eOptions.fromPage == null ? 1 : eOptions.fromPage;
            let toPage = eOptions.toPage == null ? pdf['_pdfInfo'].numPages : eOptions.toPage;
            let promises = []; // collecting all page promises

            for (var j = fromPage; j <= toPage; j++) {
                let page = pdf.getPage(j);

                promises.push(page.then(function(page) { // add page promise
                    return page.getTextContent().then(function(contents){ // return the array of contents
                        return {info : page, items : contents.items};
                    });
                }));
            }
            // Wait for all pages
            return Promise.all(promises).then(function (pages) {
                return treatPDFResult(pages, eOptions);
            });
        });
    }

    function treatPDFResult(pdfPages, eOptions){
        let pages = [];

        pdfPages.forEach(function(page){
            let paragraphs = [];
            let currentLine = null;

            page.items.forEach(function(item){
                let lineHeight = item.height;
                let lineWidth = item.width;
                let linePosY = item.transform[5];
                let linePosX = item.transform[4];
                let lineContent = item.str;
                let currentParagraph = paragraphs.length === 0 ? null : paragraphs[paragraphs.length - 1];

                if(!isToExclude(page.info, item, eOptions)) {
                    if (currentLine == null || !currentLine.isOnLine(linePosY)) {
                        if(currentParagraph == null || !currentParagraph.isOnParagraph(lineHeight, linePosY, eOptions)){
                            currentParagraph = new PDFParagraph();
                            paragraphs.push(currentParagraph);
                        }
                        currentLine = new PDFLine(lineContent, linePosY, linePosX, lineHeight, lineWidth);
                        currentParagraph.addLine(currentLine);
                    }
                    else {
                        currentLine.addContent(lineContent, lineWidth);
                    }
                }
            });

            pages.push(new PDFPage(page.info.pageIndex + 1, paragraphs));
        });

        let result = [];
        pages.forEach(page => {
            result.push(page.draw());
            result.push($('<div class="w-100 d-flex"><div class="flex-grow-1"><hr></div><div class="num-page">' + page.numPage + '</div></div>'));
        });
        return result;
    }

    function isToExclude(page, item, eOptions){
        let pageHeight = page['_pageInfo'].view[3];
        let pageWidth = page['_pageInfo'].view[2];
        let linePosX = item.transform[4];
        let linePosY = item.transform[5];

        let isInExclusionZone = (eOptions.exclusion.up != null && linePosY >= pageHeight - (pageHeight * eOptions.exclusion.up))
            || (eOptions.exclusion.down != null && linePosY <= (pageHeight * eOptions.exclusion.down))
            || (eOptions.exclusion.right != null && linePosX >= pageWidth - (pageWidth * eOptions.exclusion.right))
            || (eOptions.exclusion.left != null && linePosX <= (pageWidth * eOptions.exclusion.left));

        let isVertical = item.transform[1] < 0 || item.transform[2] < 0;

        return isInExclusionZone || (eOptions.notVertical && isVertical);
    }

    class PDFPage{

        constructor(numPage, paragraphs){
            this.numPage = numPage;
            this.paragraphs = paragraphs;
        }

        draw(){
            let paragraphs = eOptions.displayByVerticalPos ?
                this.paragraphs.sort((p1, p2) => p2.getPosY() - p1.getPosY()) : this.paragraphs;

            paragraphs = paragraphs.map(p => p.draw());

            return $('<div id="pdf-page-' + this.numPage + '" class="pdf-page"></div>').append(paragraphs);
        }
    }

    class PDFParagraph{

        constructor(){
            this.lines = [];
        }

        addLine(line){
            this.lines.push(line);
        }

        getPosY(){
            return this.lines.length > 0 ? this.lines[0].posY : 0;
        }

        isOnParagraph(height, posY, eOptions){
            if(this.lines.length > 0){
                let lastLine = this.lines[this.lines.length - 1];

                let sameHeight = lastLine.height + (lastLine.height * eOptions.heightSameParagraph) > height
                    && lastLine.height - (lastLine.height * eOptions.heightSameParagraph) < height;

                let sameBlock = Math.abs(lastLine.posY - posY) < (height / 10) * eOptions.heightNewParagraph;

                return sameHeight && sameBlock;
            }

            return true;
        }

        draw(){
            return $('<div class="pt-2 pb-2"><div class="pdf-paragraph">' + this.lines.map(line => line.draw()).join(eOptions.keepOriginalWidth ? '<br>' : '') + '</div></div>');
        }
    }

    class PDFLine{

        constructor(content, posY, posX, height, width){
            this.content = this.treatContent(content);
            this.posY = posY;
            this.posX = posX;
            this.height = height;
            this.width = width;
            this.correctedPosY = this.getCorrectedPos(posY);
        }

        addContent(content, width){
            this.content += " " + content;
            this.width += width;
        }

        isOnLine(posY){
            return this.getCorrectedPos(posY) === this.correctedPosY;
        }

        treatContent(content){
            return content.replace(/\u0006/gi, "fi");
        }

        getCorrectedPos(pos){
            return Math.floor(pos * 0.99);
        }

        draw(){
            return this.content + " ";
            //return this.content + " (" + this.posY + " - " + this.posX + " - " + this.height + " - " + this.width + ")";
        }
    }


    function treatArray(texts){
        let table = $('<table></table>');

        let tbody = $('<tbody></tbody>').appendTo(table);
        texts.forEach(function(text){
            console.log(text);
            text = text.replace('Toutes les gares - Alle stations Toutes les gares - Alle stations GARE Semaine N67 Samedi R6 Dimanche R7 STATION Week TOP Zaterdag TOP Zondag TOP ', '');
            text = text.replace('Toutes les gares - Alle stations GARE Semaine N67 Samedi R6 Dimanche R7 STATION Week TOP Zaterdag TOP Zondag TOP ', '');
            tbody.append(treatRows(text, tbody));
        });

        return table;
    }

    function treatRows(texts, tbody){
        let rows = [];
        texts = texts.split(' ');
        let tr = null;

        texts.forEach(function(text){
            if(isNaN(text)){
                if(tr != null)
                    rows.push(tr);
                tr = $('<tr></tr>');
            }

            $('<td>' + text + '</td>').appendTo(tr);
        });

        return rows;
    }

};}(jQuery));