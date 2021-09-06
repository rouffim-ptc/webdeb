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
 * Paging facility
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

class Pager {
    /**
     * Constructor for a pager.
     *
     * @param root the root element from which the pageable div will be retrieved.
     * @param perPage amount of elements per page
     * @param pager jquery element selector of the pager
     * @param callback function to be called when a new page is displayed (optional)
     * @param goToTop false if no click must be triggered on scrollToTop button at page changes (optional, true by default)
     * @param onlyArrows true if we must only display arrows
     * @param alphaOrder 0 if no sorting, 1 if optional sorting and 2 for a forced sorting
     * @param shift the number of items to ignore if alpha ordered
     * @constructor
     */
    constructor(root, perPage, pager, callback, goToTop, onlyArrows, alphaOrder, shift) {
        var r = ((root instanceof jQuery) ? root : $(root));

        if(pager == null){
            let pager_container = createPagerContainer(r);
            pager_container[0].insertAfter(r);
            pager = pager_container[1];
        }

        var p = ((pager instanceof jQuery) ? pager : $(pager));

        this.root = r.hasClass('pageable') ? r : r.find('.pageable');
        this.perPage = perPage;
        this.callback = callback;
        this.goToTop = (goToTop === undefined || goToTop);
        this.pager = p;
        this.onlyArrows = onlyArrows === undefined ? false : onlyArrows;
        this.alphaOrder = isNaN(alphaOrder) || alphaOrder < 0 || alphaOrder > 2 ? 0 : alphaOrder;
        this.shift = isNaN(shift) ? 0 : shift;
        this.SORT_NONE = 0;
        this.SORT_FORCED = 1;
        this.SORT_OPTIONAL = 2;
        this.alphaTab = [];
    }

    /**
     * Reset the pager. Used when a filter has been applied and "filtered" elements have been hidden.
     *
     * @param keepCurrentPage true if the current page must be keept
     */
    reset(keepCurrentPage) {
        let that = this;
        keepCurrentPage = keepCurrentPage || false;
        that.size = this.root.children(':not(.filtered)').length;
        that.pages = Math.ceil(that.size / that.perPage);
        that.init(keepCurrentPage);
    }

    /**
     * Initialize the pager
     *
     * @param keepCurrentPage true if the current page must be keept
     * @param sort true if the elements must be sorted
     */
    init(keepCurrentPage, sort) {
        sort = sort || false;
        var that = this;

        that.sortElements(sort);

        // create paging facility (at most 10 + previous and next buttons)
        let formerPage = parseInt(this.pager.attr("curr"));
        this.pager.empty().data("curr", 0);

        // add pager anchors
        if (that.pages > 1) {
            this.drawBtnLinks(this.pager);
        }

        // add listeners to previous and next buttons (if such buttons are present)
        this.pager.find(".previous").on('click', function (e) {
            e.preventDefault();
            that.goTo(parseInt(that.pager.attr("curr")) - 1, that.pager);
        });

        this.pager.find(".next").on('click', function (e) {
            e.preventDefault();
            that.goTo(parseInt(that.pager.attr("curr")) + 1, that.pager);
        });

        // add listeners on other page anchors
        this.pager.find('.page-link:not(.previous):not(.next)').on('click', function (e) {
            e.preventDefault();
            that.goTo(parseInt($(this).text()) - 1, that.pager);
        });

        if(this.alphaOrder === this.SORT_OPTIONAL){
            this.pager.find('.pager-sort > a').on('click', function (e) {
                e.preventDefault();
                that.init(true, true);
            });
        }

        // jump to page 0 or the former page if asking
        that.goTo(keepCurrentPage && !isNaN(formerPage) && formerPage <= this.pages ? formerPage : 0);
    }

    /**
     * Draw buttons links to move between page. The number of buttons is displayed in terms of type of screen
     *
     * @param pager the pager container where add the buttons
     */
    drawBtnLinks(pager) {
        let container = $('<ul class="pagination justify-content-center"></ul>');

        container.append('<li class="page-item"><a class="page-link previous" href="#"><i class="fa fa-angle-double-left"></i></a></li>');

        if (!this.onlyArrows) {
            var nbLinks = 8;
            for (var curr = 0; curr < this.pages && curr < nbLinks; curr++) {
                container.append('<li class="page-item"><a class="page-link" href="#"><span></span><span></span></a></li>');
            }
        }

        container.append('<li class="page-item"><a class="page-link next" href="#"><i class="fa fa-angle-double-right"></i></a></li>');

        if(this.alphaOrder === this.SORT_OPTIONAL){
            container.append('<span class="pager-sort"><a href="#" class="primary" style="position : relative; left : 5px; top : 5px">'
                + Messages("general.sort.label") + '&nbsp;<i class="fas fa-sort-alpha-down"></i></a></span>');
        }

        pager.append(container);
    }

    /**
     * Load a page from a pager. Will ignore elements with class "filtered".
     *
     * @param page the page index to load
     */
    goTo(page) {
        if (page >= 0 && page < this.pages) {
            let that = this;

            // filter result and truncate 'to-clamp' stuff if any
            that.paginateElements(page);
            this.pager.attr("curr", page);
            this.truncateElmts();

            $(window).on('resize filter-resize on-focus', function(){
                that.truncateElmts();
            });

            $(window).on('on-focus', function(){
                that.paginateElements(page);
            });

            // update labels of buttons (displaying at most 10 pages, moving when getting at end of displayed labels
            var page_links = '.page-link:not(.previous):not(.next)';
            this.updateBtnLinks(this.pager.find(page_links), page);

            // if we have a callback function, call it
            if (this.callback !== undefined) {
                this.callback();
            }
            // if there is a "scroll to top" button, click on it to go back to top of page
            var scroller = $('.scroll-top-wrapper');
            if (scroller.length > 0 && this.goToTop) {
                scroller.trigger('click');
            }
        }
    }

    paginateElements(page) {
        var startAt = page * this.perPage;
        var endOn = startAt + this.perPage;

        this.root.children(':not(.filtered)').css('display', 'none').slice(startAt, endOn).css('display', 'block');
    }

    sortElements(sort){

        if ((sort || this.alphaOrder === this.SORT_FORCED) && this.root.find(".pager-text-to-sort").exists()) {
            let children = [];

            this.root.children().each(function(key, element){

                element = $(element);
                let textContent = element.find(".pager-text-to-sort");

                if(textContent.exists() && !textContent.hasClass("pager-text-put-first")){
                    let obj = {};
                    obj.text = textContent.text().trim();
                    obj.element = element;
                    obj.element.detach();
                    children.push(obj);
                }
            });

            children.sort(function(a, b){
                var nameA = a.text.toLowerCase(), nameB = b.text.toLowerCase();
                if (nameA < nameB) //sort string ascending
                    return -1;
                if (nameA > nameB)
                    return 1;
                return 0 //default return value (no sorting)
            });

            let currentPage = 0;
            let startAt = 0;
            let endOn = this.perPage;


            for(let i in children) {
                i = parseInt(i);

                if(i === startAt + (endOn * currentPage)){
                    this.alphaTab[currentPage] = {};
                    this.alphaTab[currentPage].start = children[i <= 0 ? i : i - this.shift].text.charAt(0).toUpperCase();
                } if(i === (endOn - 1) + (endOn * currentPage) || i === children.length - 1){
                    this.alphaTab[currentPage++].end = children[i === children.length - 1 ? i : i - this.shift].text.charAt(0).toUpperCase();
                }
                this.root.append(children[i].element);
            }
        }
    }

    /**
     *  Truncate 'to-clamp' stuff if any
     */
    truncateElmts() {
        this.root.children(':not(.filtered)').find('.to-clamp:visible').each(function () {
            $(this).trunk8({lines: 2, tooltip: false});
        });
    }

    /**
     * Update labels of buttons (displaying at most 10 pages, moving when getting at end of displayed labels
     *
     * @param buttons the buttons to update
     * @param page the page index to load
     */
    updateBtnLinks(buttons, page) {
        var that = this;
        var minPage = (page > 5 ? page - (buttons.length - 1) : 0);
        buttons.each(function () {
            let parent = $(this).parent();

            parent.show();

            if (minPage === page) {
                parent.addClass('active');
            } else {
                parent.removeClass('active');
                // if we're going close to the end, hide last page buttons
                if (minPage >= that.pages) {
                    parent.hide();
                }
            }
            that.updateBtnText($(this), ++minPage);
        });

        that.updateBtnText(buttons.first(), 1, true);
        that.updateBtnText(buttons.last(), this.pages, true);
    }

    updateBtnText(btn, num, show){
        show = show || false;
        if(show)
            btn.show();
        btn.find("span").first().text(num);

        if(this.alphaOrder !== this.SORT_NONE) {
            let alpha = this.alphaTab[parseInt(num) - 1];
            if (alpha !== undefined) {
                btn.find("span").last().text(' [' + alpha.start + '-' + alpha.end + ']');
            }
        }
    }
}
