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
 * This javascript file contains the drag n drop system for context (debate and text) arguments structure.
 *
 * @author Martin Rouffiange
 */
(function(e) { e.fn.contextClassifier = function (options) {

    let eOptions,
        container = $(this),
        classifierContainer = null,
        targetDraggable = null,
        targetAction = null,
        targetDraggableOriginal = null,
        dragDelay = null,
        fakePlacementDelay = null,
        fakePlacementTargetId = null,
        makeTargetDraggableDelayId = null,
        formerParentObj = null,
        toggleElementId = {},
        classifiers = null,
        undefined_classifier = null,
        contributions = null,
        isReloaded = false,
        formerContribution = null,
        scrollId = null,
        locked,
        test;

    const SHADE_JUSTIFY = 'justify';
    const SHADE_OPPOSES = 'opposes';
    const SHADE_NONE = 'none';

    const TYPE_CITATION = 1;
    const TYPE_ARGUMENT = 2;
    const TYPE_CATEGORY = 3;
    const TYPE_SHADE_CLASSIFIER = 4;
    const TYPE_CLASSIFIER = 5;

    const TYPE_NAME_CITATION = 'citation';
    const TYPE_NAME_ARGUMENT = 'argument';
    const TYPE_NAME_CATEGORY = 'category';
    const TYPE_NAME_SHADE_CLASSIFIER = 'shade_classifier';
    const TYPE_NAME_CLASSIFIER = 'classifier';

    const TARGET_ACTION_ADD = 'add';
    const TARGET_ACTION_COPY = 'copy';
    const TARGET_ACTION_MERGE = 'merge';
    const TARGET_ACTION_SIMILAR = 'similar';

    const LOGO_ACTION_COPY = 'far fa-copy';
    const LOGO_ACTION_MERGE = 'far fa-object-group';
    const LOGO_ACTION_SIMILAR = 'fas fa-link';
    const ACTION_LOGOS = [LOGO_ACTION_COPY, LOGO_ACTION_MERGE, LOGO_ACTION_SIMILAR];

    const MENU_PEN = 'pen';
    const MENU_PLUS = 'plus';

    const CLASSIFIER_TYPE_ADD = 'add_container';
    const CLASSIFIER_TYPE_SEPARATOR = 'separator';

    $(document).ready(function() {
        classifierContainer = container.find('.context-classify-tool');
        classifierContainer = classifierContainer.exists() ? classifierContainer : null;

        initOptions();
        initListeners();
        //createFakeClassifiers();
        initData();
    });

    function initOptions(){

        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {
                getDataAsync : typeof options.getDataAsync === 'function' ? options.getDataAsync : null,
                saveDataAsync : typeof options. saveDataAsync === 'function' ? options. saveDataAsync : null,
                dataId : typeof options.dataId === 'number' ? options.dataId : null,
                shade_classifier_justify_title : typeof options.shade_classifier_justify_title === 'string' ? options.shade_classifier_justify_title : '',
                shade_classifier_opposes_title : typeof options.shade_classifier_opposes_title === 'string' ? options.shade_classifier_opposes_title : '',
                isMultipleShaded : typeof options.isMultipleShaded === 'boolean' ? options.isMultipleShaded : true,
                opinionCanAddOpinion : typeof options.opinionCanAddOpinion === 'boolean' ? options.opinionCanAddOpinion : false,
                categoryCanAddCitation : typeof options.categoryCanAddCitation === 'boolean' ? options.categoryCanAddCitation : false,
                dragDelay : 200,
                openDelay : 600,
                fakePlacementDelay : 300
            };
        } else {
            eOptions = {
                getDataAsync : null,
                saveDataAsync : null,
                dataId : null,
                shade_classifier_justify_title : '',
                shade_classifier_opposes_title : '',
                isMultipleShaded : true,
                opinionCanAddOpinion : false,
                categoryCanAddCitation : false,
                dragDelay : 200,
                openDelay : 600,
                fakePlacementDelay : 100
            };
        }

        locked = !container.is(':visible');
    }

    function initListeners() {

        document.addEventListener('touchstart', function(e) {
            if(!locked) {
                if (targetDraggable != null)
                    e.preventDefault()
            }
        }, { passive: false });

        document.addEventListener('touchmove', function(e) {
            if(!locked) {
                if (targetDraggable != null)
                    e.preventDefault()
            }
        }, { passive: false });

        container.on('mousedown dblclick touchstart', '.draggable', function(evt){
            if(!locked) {
                let isDbclick = eventIsDblclick(evt);

                if (!isDbclick) {
                    if (targetDraggable == null && dragDelay == null && (evt.type !== 'mousedown' || evt.which === 1)) {

                        dragDelay = new Date();
                        container.addClass('unselectable');

                        makeTargetDraggableDelayId = setTimeout(function () {
                            makeTargetDraggable(evt);
                        }, eOptions.dragDelay);
                    } else {
                        targetAction = null;
                        removeIndicators();
                        evt.preventDefault();
                    }
                } else {
                    targetDbClickElement(evt);
                    clearTimeout(makeTargetDraggableDelayId);
                    makeTargetDraggableDelayId = null;
                }
            }
        });

        $(document).on('mouseup touchend', async function(evt){
            if(!locked) {
                if (targetDraggable != null && new Date().getTime() - dragDelay.getTime() > eOptions.dragDelay) {
                    await dropAction();
                } else if (makeTargetDraggableDelayId != null) {
                    clearTimeout(makeTargetDraggableDelayId);
                    if (targetAction == null) {
                        removeIndicators();
                    }
                }

                if (scrollId != null)
                    clearTimeout(scrollId);

                container.removeClass('unselectable');
                makeTargetDraggableDelayId = null;
                scrollId = null;
                dragDelay = null;
            }
        });

        $(document).on('mousemove touchmove', function(evt){
            if(!locked) {
                if (targetDraggable != null) {
                    placeTargetDraggable(evt);

                    targetDraggable.hide();

                    let target = getPointedElement(evt);

                    expandOnMouseover(target, '.nav-link', '.active');
                    expandOnMouseover(target, '.contribution-title', '[aria-expanded="true"]');

                    fakePlacement(target);

                    targetDraggable.show();

                    doScrollAction(evt.clientY, evt.clientX);
                } else if (makeTargetDraggableDelayId != null) {
                    evt.preventDefault();
                }
            }
        });

        $(document).on('click', '.add-category', function(){
            if(!locked) {
                let newCategoryInput = container.find('#add-category-name');

                /*createEmptyContribution(container.find(getTypeAsClassName(TYPE_NAME_CATEGORY)).length,
                    newCategoryInput.val(),
                    TYPE_CATEGORY)
                    .insertBefore(container.find('.debate-classify-tool').first().children().last());*/

                newCategoryInput.val('');
            }
        });

        $(document).on('dragndrop-reload', function(evt, reload){
            if(!locked) {
                //isReloaded = reload !== undefined ? reload : false;
                initData();
            }
        });

        container.on('dragndrop-reload', function(evt, reload){
            if(!locked) {
                //isReloaded = reload !== undefined ? reload : false;
                initData();
            }
        });

        $(window).resize(function(){
            if(locked && container.is(':visible')) {
                locked = false;
                initData();
            } else if(!locked && !container.is(':visible')) {
                locked = true;
            }
        });
    }

    function fakePlacement(targ) {
        let target = searchForTypeElement(targ);

        if(target != null) {
            if (fakePlacementDelay == null ||
                (new Date().getTime() - fakePlacementDelay.getTime() > eOptions.fakePlacementDelay
                    && (fakePlacementTargetId == null || fakePlacementTargetId !== target.attr('data-id')))) {

                fakePlacementDelay = new Date();
                fakePlacementTargetId = target.attr('data-id');

                let elementObj = contributions[targetDraggable.data('id')];
                let parent = getElementFromView(target);

                if (parent && elementObj && (targetAction === null || targetAction === TARGET_ACTION_ADD)) {
                    parent.doActionWith(elementObj, targetAction, true, true);
                }
            }
        } else if(formerParentObj) {
            formerParentObj.removeFake(false, true);
            formerParentObj = null;
            fakePlacementTargetId = null;
        }
    }

    function doScrollAction(clientY){
        if(scrollId != null)
            clearTimeout(scrollId);

        if(clientY <= 25 || clientY >= $(window).height() - 25) {
            scrollId = setInterval(function () {
                let top = targetDraggable.position().top;
                if (clientY <= 25) {
                    window.scrollBy(0, -5);
                    top -= 5;
                } else if (clientY >= $(window).height() - 25) {
                    window.scrollBy(0, 5);
                    top += 5;
                }

                if(top > 0 && top < document.body.clientHeight - targetDraggable.height())
                    targetDraggable.css('top', top + 'px');
            }, 10);
        }
    }

    function eventIsDblclick(evt) {
        return evt != null && evt.type === 'dblclick';
    }

    function expandOnMouseover(target, targetClass, notClass) {
        if(targetDraggable.data('type') !== TYPE_CATEGORY) {
            let timeoutId = toggleElementId[targetClass] !== undefined ? toggleElementId[targetClass] : null;

            if (timeoutId == null && (target.hasClass(targetClass) || target.parents(targetClass).exists())) {
                let element = searchForTypeElement(target);
                target = target.is(targetClass) ? target : target.parents(targetClass).first();

                if (element != null && (notClass === undefined || !target.hasClass(notClass))) {
                    toggleElementId[targetClass] = setTimeout(function () {
                        element.trigger('open', [target]);
                        toggleElementId[targetClass] = null;
                    }, eOptions.openDelay);
                }
            } else if (timeoutId != null && !(target.is(targetClass) || target.parents(targetClass).exists())) {
                clearTimeout(timeoutId);
                toggleElementId[targetClass] = null;
            }
        }
    }

    function getTypeAsClassName(type){
        return '.' + type;
    }

    function getTypeAsContainerClassName(type){
        return '.' + type + '-container';
    }

    function getTypeAsContainerTabClassName(type){
        return getTypeAsContainerClassName(type) + '-tab';
    }

    function getTypeAsShadedContainerClassName(type, shade){
        return getTypeAsContainerClassName(type) + '-' + shade;
    }

    function getTypeAsListClassName(type){
        return '.' + type + '-list';
    }

    function getTypeAsTitleClassName(type){
        return '.' + type + '-title';
    }

    function getClassNameAsHtmlName(className) {
        return className.substr(1);
    }

    function getShadeName(shade) {
        switch (shade) {
            case SHADE_JUSTIFY :
                return "Pour";
            case SHADE_OPPOSES :
                return "Contre";
            default :
                return "Non classé"
        }
    }

    function getShadeId(shade) {
        switch (shade) {
            case SHADE_JUSTIFY :
                return 0;
            case SHADE_OPPOSES :
                return 1;
            default :
                return -1;
        }
    }

    function getSelectorByType(type, isMin){
        switch (type) {
            case TYPE_CITATION :
                return getTypeAsClassName(TYPE_NAME_CITATION) + (isMin ? ', ' + getTypeAsClassName(TYPE_NAME_ARGUMENT) + ', ' + getTypeAsClassName(TYPE_NAME_CATEGORY) : '');
            case TYPE_ARGUMENT :
                return getTypeAsClassName(TYPE_NAME_ARGUMENT) + (isMin ? ', ' + getTypeAsClassName(TYPE_NAME_CATEGORY) : '');
            case TYPE_CATEGORY :
                return getTypeAsClassName(TYPE_NAME_CATEGORY);
            case TYPE_SHADE_CLASSIFIER :
                return getTypeAsClassName(TYPE_NAME_SHADE_CLASSIFIER);
            case TYPE_CLASSIFIER :
                return getTypeAsClassName(TYPE_NAME_CLASSIFIER);
            default :
                return '';
        }
    }

    function getElementTypeNameByType(type){
        switch (type) {
            case TYPE_CITATION :
                return TYPE_NAME_CITATION;
            case TYPE_ARGUMENT :
                return TYPE_NAME_ARGUMENT;
            case TYPE_CATEGORY :
                return TYPE_NAME_CATEGORY;
            case TYPE_SHADE_CLASSIFIER :
                return TYPE_NAME_SHADE_CLASSIFIER;
            case TYPE_CLASSIFIER :
                return TYPE_NAME_CLASSIFIER;
            default :
                return '';
        }
    }

    function getClientPosX(evt) {
        return evt.clientX !== undefined ? evt.clientX : evt.originalEvent.touches[0].clientX;
    }

    function getClientPosY(evt) {
        return evt.clientY !== undefined ? evt.clientY : evt.originalEvent.touches[0].clientY;
    }

    function getPagePosX(evt) {
        return (evt.pageX !== undefined ? evt.pageX : evt.originalEvent.touches[0].pageX);
    }

    function getPagePosY(evt) {
        return (evt.pageY !== undefined ? evt.pageY : evt.originalEvent.touches[0].pageY);
    }

    function targetDbClickElement(evt) {
        let targetElement = getElementFromView(getPointedDraggableElement(evt));

        if (targetElement == null || eOptions.categoryCanAddCitation) {
            targetAction = null;
        }
        else {
            targetElement.copy();
            targetAction = TARGET_ACTION_COPY;
        }
    }

    function makeTargetDraggable(evt){
        let target = getPointedDraggable(evt);

        if(target != null) {
            targetDraggableOriginal = target.parent();

            let width = targetDraggableOriginal.width();

            targetDraggable = targetDraggableOriginal.clone().detach().prependTo($('body'));

            let obj = getElementFromView(targetDraggableOriginal);

            if(obj) {
                targetDraggable.data('id', obj.getId());
                targetDraggable.data('type', obj.type);
            }

            if (targetAction == null) {
                targetDraggableOriginal.hide();
            }

            placeTargetDraggable(evt);

            targetDraggable.css('background-color', 'white');
            targetDraggable.css('position', 'absolute');
            targetDraggable.css('z-index', '999');
            targetDraggable.css('min-width', '50px');
            targetDraggable.css('min-height', '50px');
            targetDraggable.css('width', width + 'px');
            targetDraggable.css('max-width', width + 'px');
        }
    }

    function placeTargetDraggable(evt){
        targetDraggable.css('left', getPagePosX(evt) + 'px');
        targetDraggable.css('top', getPagePosY(evt) + 'px');
    }

    function getPointedElementByPos(posX, posY) {
        return $(document.elementFromPoint(posX, posY));
    }

    function getPointedElement(evt) {
        return getPointedElementByPos(getClientPosX(evt), getClientPosY(evt));
    }

    function getPointedDraggable(evt) {
        let pointed = getPointedElement(evt);
        let target = pointed.hasClass('draggable') ? pointed : pointed.parents('.draggable').first();
        return target.exists() ? target : null;
    }

    function getPointedDraggableElement(evt) {
        let element = getPointedDraggable(evt);
        return element != null ? element.parent() : null;
    }

    function removeIndicators() {
        ACTION_LOGOS.forEach(logo => container.find('.' + logo.replace(/ /, '.')).remove());
    }

    async function dropAction(){
        let posX = targetDraggable.offset().left - $(document).scrollLeft();
        let posY = targetDraggable.offset().top - $(document).scrollTop();

        targetDraggable.remove();
        targetDraggable = targetDraggableOriginal;
        removeIndicators();

        let targetEvent = getPointedElementByPos(posX, posY);
        let elementObj = getElementFromView(targetDraggable);

        if (elementObj != null) {
            formerContribution = elementObj.parent ? elementObj.parent.id : null;
            let targetEventElement = searchForTypeElement(targetEvent);

            if (targetEventElement != null) {
                let parentObj = getElementFromView(targetEventElement);

                if (parentObj != null) {
                    let rightShift = (posX - targetEvent.offset().left) / targetEvent.width() > 0.25;
                    await parentObj.doActionWith(elementObj, targetAction === null ? TARGET_ACTION_ADD : targetAction, undefined, undefined, rightShift);
                }
            }

            targetDraggable.remove();
            targetDraggable = null;

            if(elementObj.parent != null) {
                elementObj.parent.draw();
            }
        }

        targetDraggable = null;
        targetAction = null;
    }

    function searchForTypeElement(element, type, isMin){

        if(!$.contains(container[0], element[0])){
            return null;
        }

        let contributions = getTypeAsClassName(TYPE_NAME_CITATION) + ', ' +
            getTypeAsClassName(TYPE_NAME_ARGUMENT) + ', ' +
            getTypeAsClassName(TYPE_NAME_CATEGORY) + ', ' +
            getTypeAsClassName(TYPE_NAME_SHADE_CLASSIFIER) + ', ' +
            getTypeAsClassName(TYPE_NAME_CLASSIFIER);

        if(type === undefined ? element.is(contributions) : element.is(getSelectorByType(type, isMin))) {
            return element;
        }

        return searchForTypeElement(element.parent(), type, isMin);
    }

    function getElementId(element){
        return element != null ? element.data("complete-id") ? element.data("complete-id") : element.data("id") : null;
    }

    function getElementFromView(element) {
        let id = getElementId(element);
        return id != null ? contributions[id] : null;
    }

    function displayWaitForIt(){
        if(classifierContainer != null) {
            classifierContainer.html(getWaitForIt());
        }
    }

    function initData(){
        if(eOptions.getDataAsync != null) {

            displayWaitForIt();

            eOptions.getDataAsync(eOptions.dataId).done(function(data){

                contributions = {};
                classifiers = [];

                let classifier;
                let unshadedShadeClassifier;
                let leftShadeClassifier;
                let rightShadeClassifier;
                let rightShade2Classifier;
                let category;
                let argument;

                data.forEach(dataClassifier => {
                    classifier = new Classifier(dataClassifier.id, dataClassifier.linkId, dataClassifier.title, false);
                    classifiers.push(classifier);
                    contributions[classifier.id] = classifier;

                    if (eOptions.isMultipleShaded) {
                        leftShadeClassifier =
                            new ShadedClassifier(
                                eOptions.categoryCanAddCitation ? SHADE_NONE : SHADE_JUSTIFY,
                            !eOptions.categoryCanAddCitation ? eOptions.shade_classifier_justify_title : Messages('viz.tag.arguments.drag.citations'),
                                eOptions.categoryCanAddCitation);
                        classifier.addShade(leftShadeClassifier);

                        rightShadeClassifier =
                            new ShadedClassifier(
                                eOptions.categoryCanAddCitation ? SHADE_NONE : SHADE_OPPOSES,
                            !eOptions.categoryCanAddCitation ? eOptions.shade_classifier_opposes_title : Messages('tag.parent'),
                                false, true);
                        classifier.addShade(rightShadeClassifier);

                        if(eOptions.categoryCanAddCitation) {
                            rightShade2Classifier =
                                new ShadedClassifier(SHADE_NONE, Messages('tag.child'), false, false);
                            classifier.addShade(rightShade2Classifier);
                        }
                    } else {
                        unshadedShadeClassifier = new ShadedClassifier(SHADE_NONE, '');
                        classifier.addShade(unshadedShadeClassifier);
                    }

                    dataClassifier.categories.forEach(dataCategory => {
                        if(eOptions.opinionCanAddOpinion) {
                            category = new Category(dataCategory.id, dataCategory.linkId, dataCategory.title);
                            unshadedShadeClassifier.addCategory(category);

                            dataCategory.arguments.forEach(dataArgument =>
                                addArgument(dataArgument, category)
                            );
                        } else if(eOptions.categoryCanAddCitation) {
                            category = new Category(dataCategory.id, dataCategory.linkId, dataCategory.title);

                            switch(dataCategory.isParent){
                                case true :
                                    rightShadeClassifier.addCategory(category);
                                    break;
                                case false :
                                    rightShade2Classifier.addCategory(category);
                                    break;
                                default :
                                    leftShadeClassifier.addCategory(category);

                            }

                            dataCategory.citations.forEach(citation =>
                                category.addCitation(new Citation(citation.id, citation.linkId, citation.title)));
                        } else {
                            category = new Category(dataCategory.id, dataCategory.linkId, dataCategory.title);
                            leftShadeClassifier.addCategory(category);

                            dataCategory.argumentsMap.SUPPORTS.forEach(dataArgument => {
                                argument = new Argument(dataArgument.id, dataArgument.linkId, dataArgument.title);
                                category.addArgument(argument);
                                dataArgument.citations.forEach(citation => argument.addCitation(new Citation(citation.id, citation.linkId, citation.title)));
                            });

                            category = new Category(dataCategory.id, dataCategory.linkId, dataCategory.title);
                            rightShadeClassifier.addCategory(category);

                            dataCategory.argumentsMap.REJECTS.forEach(dataArgument => {
                                argument = new Argument(dataArgument.id, dataArgument.linkId, dataArgument.title);
                                category.addArgument(argument);
                                dataArgument.citations.forEach(citation => argument.addCitation(new Citation(citation.id, citation.linkId, citation.title)));
                            });
                        }
                    });
                });
                initClassifiers();

            }).fail(function(err){
                if (err.status === 401) {
                    redirectToLogin();
                } else {
                    console.log("Load drag n drop data failed.");
                }
            });

        }
    }

    function addArgument(dataArgument, parent){
        let argument = new Argument(dataArgument.id, dataArgument.linkId, dataArgument.title, dataArgument.shade);
        parent.addArgument(argument);

        dataArgument.arguments.forEach(dataArgument2 =>
            addArgument(dataArgument2, argument));

        dataArgument.citations.forEach(citation =>
            argument.addCitation(new Citation(citation.id, citation.linkId, citation.title)));
    }

    function initClassifiers(){
        if(classifiers != null && classifierContainer != null) {
            classifierContainer.empty();
            classifiers.forEach(classifier => classifierContainer.append(classifier.draw()));
            initContributionListeners(container);
        }
    }


    class Contribution {

        constructor(id, linkId, name, type, isDraggable, hiddenTitle) {
            this.id = id;
            this.linkId = linkId;
            this.name = name;
            this.type = type;
            this.order = 0;
            this.fake = false;
            this.isDraggable = isDraggable === undefined ? id !== -1 : isDraggable;
            this.hiddenTitle = hiddenTitle === undefined ? false : hiddenTitle;

            this.parent = null;
            this.container = null;
        }

        getId(){
            let id = this.id;
            let parent = this.parent;

            while(parent != null) {
                id += '-' + parent.id;
                parent = parent.parent;
            }

            return id;
        }

        getHierarchyIds(){
            let id = '';
            let parent = this.parent;

            while(parent != null) {
                id += '/' + (eOptions.opinionCanAddOpinion && parent.type === TYPE_CATEGORY ? -1 : parent.id);
                parent = parent.parent;
            }

            if(eOptions.dataId != null)
                id += '/' + eOptions.dataId;

            return id.replace('/', '');
        }

        getTypeName() {
            return getElementTypeNameByType(this.type);
        }

        typeToContributionType() {
            switch (this.type) {
                case TYPE_CITATION :
                    return 4;
                case TYPE_ARGUMENT :
                    return 3;
                case TYPE_CATEGORY :
                case TYPE_CLASSIFIER :
                    return 6;
                default :
                    return -1;
            }
        }

        add(redraw, action, fake, rightShift) {
            if(!fake)
                contributions[this.getId()] = this;

            if(redraw) {
                if(this.parent != null) {
                    isReloaded = this.type !== TYPE_CATEGORY;
                    if(this.parent.type === TYPE_ARGUMENT && this.parent.parent != null){
                        this.parent.parent.draw();
                    } else {
                        this.parent.draw();
                    }
                    initContributionListeners(container);
                }

                if(!fake)
                    this.save(action, rightShift);
            }
        }

        remove(redraw) {
            delete contributions[this.getId()];

            if(redraw) {
                if(this.parent != null) {
                    if(this.parent.type === TYPE_ARGUMENT && this.parent.parent != null){
                        this.parent.parent.draw();
                    } else {
                        this.parent.draw();
                    }
                } else if(this.container instanceof jQuery) {
                    this.container.remove();
                }

                this.container = null;
            }
        }

        searchParent(type) {
            let parent = this;

            while(parent != null && parent.type < type) {
                parent = parent.parent;
            }

            return parent;
        }

        searchChildContributionForAdding(child){
            if(child.type === TYPE_CITATION) {
                switch (this.type) {
                    case TYPE_CLASSIFIER :
                        let shadeParent = child.searchParent(TYPE_SHADE_CLASSIFIER);

                        if(shadeParent !== undefined){
                            let shadeClassifier = this.getShadeClassifier(shadeParent.id);
                            return shadeClassifier !== undefined ? shadeClassifier.searchChildContributionForAdding(child) : undefined;
                        }
                        break;
                    case TYPE_SHADE_CLASSIFIER :
                        let category = this.getDefaultCategory();
                        return category !== undefined ? category.searchChildContributionForAdding(child) : undefined;
                    case TYPE_CATEGORY :
                        return this.getDefaultArgument();
                }
            }
            return undefined;
        }

        async doActionWith(contribution, action, redraw, fake, rightShift) {
            redraw = redraw === undefined ? true : redraw;
            fake = fake === undefined ? false : fake;
            rightShift = rightShift === undefined ? false : rightShift;

            switch (action) {
                case TARGET_ACTION_COPY :
                    await this.addChild(contribution, action, redraw, fake, rightShift);
                    break;
                case TARGET_ACTION_MERGE :
                    this.mergeWith(contribution, redraw);
                    break;
                case TARGET_ACTION_SIMILAR :
                    this.similarWith(contribution, redraw);
                    break;
                case TARGET_ACTION_ADD :
                default :
                    await this.addChild(contribution, TARGET_ACTION_ADD, redraw, fake, rightShift);
                    break;
            }
        }

        async addChild(child, action, redraw, fake, rightShift){
            if(this.getId() !== child.getId() || !fake) {
                let parent = this;
                let candidate = this.type !== TYPE_CATEGORY ? this.searchChildContributionForAdding(child) : this;
                let order;

                parent = candidate !== undefined ? candidate : parent;
                while (parent != null && !parent.canAddChildType(child)) {
                    parent = parent.parent;
                }

                if (parent != null) {
                    switch (child.type) {
                        case TYPE_CITATION :
                            order = this.getId() === child.getId() ? -1 : this.type === TYPE_CITATION ? this : this.id === parent.id ? 0 : null;
                            await parent.addCitation(child, action, redraw, order, fake);
                            break;
                        case TYPE_ARGUMENT :
                            order = this.getId() === child.getId() ? -1 : this.type === TYPE_ARGUMENT ? this : this.id === parent.id ? 0 : null;
                            await parent.addArgument(child, action, redraw, order, fake, rightShift);
                            break;
                        case TYPE_CATEGORY :
                            order = this.getId() === child.getId() ? -1 : this.type === TYPE_CATEGORY ? this : this.id === parent.id ? 0 : null;
                            await parent.addCategory(child, action, redraw, order, fake);
                            break;
                    }
                }
            }
        }

        removeFromParent(permanently, redraw) {
            if(this.parent != null) {
                switch (this.type) {
                    case TYPE_CITATION :
                        if(!permanently) {
                            this.addToGrandParent(redraw);
                        }
                        else {
                            this.addChildrenToGrandParent(redraw);
                            this.parent.removeCitation(this, redraw);
                        }
                        break;
                    case TYPE_ARGUMENT :
                        if(!permanently) {
                            this.addToGrandParent(redraw);
                        }
                        else {
                            this.addChildrenToGrandParent(redraw);
                            this.parent.removeArgument(this, redraw);
                        }
                        break;
                }
            }
        }

        canAddChildType(child){
            switch (child.type) {
                case TYPE_CITATION :
                    return this.hasMethod('addCitation') && (this.type !== TYPE_CATEGORY || eOptions.categoryCanAddCitation);
                case TYPE_ARGUMENT :
                    return this.hasMethod('addArgument') && (this.type !== TYPE_ARGUMENT || (eOptions.opinionCanAddOpinion && child.parent.getId() === this.getId()));
                case TYPE_CATEGORY :
                    return this.hasMethod('addCategory');
            }

            return false;
        }

        hasMethod(method) {
            return typeof this[method] === 'function';
        }

        addToGrandParent(redraw) {
            if(this.parent != null && this.parent.parent != null) {
                if(this.parent.type === TYPE_CATEGORY) {
                    undefined_classifier.addContributionByShade(this);
                }
                else{
                    this.parent.parent.addChild(this, false, redraw);
                }
            }
        }

        addChildrenToGrandParent(redraw) {
            if (this.parent != null) {
                if(Array.isArray(this.citations)) {
                    this.citations.forEach(citation =>
                        citation.addToGrandParent(true)
                    );
                }
                if(Array.isArray(this.arguments)) {
                    this.arguments.forEach(argument =>
                        argument.addToGrandParent(true)
                    );
                }
            }
        }

        addActionLogo(enabled, logo) {

            if(this.container != null){
                let title = this.container.find(getTypeAsTitleClassName(this.getTypeName())).find('span').first();

                if(enabled && !title.find('i').exists()) {
                    removeIndicators();
                    title.prepend('<i class="' + logo + ' mr-1"></i>');
                }
            }
        }

        copy(enabled) {
            if(this.type <= TYPE_ARGUMENT) {
                enabled = enabled === undefined ? true : enabled;

                targetAction = enabled ? TARGET_ACTION_COPY : null;
                this.addActionLogo(enabled, LOGO_ACTION_COPY);
            }
        }

        merge(enabled) {
            if(this.type === TYPE_ARGUMENT || this.type === TYPE_CATEGORY) {
                enabled = enabled === undefined ? true : enabled;

                targetAction = enabled ? TARGET_ACTION_MERGE : null;
                this.addActionLogo(enabled, LOGO_ACTION_MERGE);
            }
        }

        mergeWith(contribution, redraw) {
            if(this.type === contribution.type) {
                contribution.removeFromParent(true, redraw);
            }
        }

        similar(enabled) {
            if(this.type === TYPE_ARGUMENT) {
                enabled = enabled === undefined ? true : enabled;

                targetAction = enabled ? TARGET_ACTION_SIMILAR : null;
                this.addActionLogo(enabled, LOGO_ACTION_SIMILAR);
            }
        }

        similarWith(contribution, redraw) {
            //save similarity ?
        }

        async save(action, rightShift) {
            if(!await this.doSaveAction(action, rightShift)){
                initData();
                slideDefaultErrorMessage();
            } else {
                /*
                if(eOptions.opinionCanAddOpinion && this.type === TYPE_ARGUMENT && this.parent.type === TYPE_CATEGORY && !rightShift) {
                    initData();
                }*/
                slideDefaultSuccessMessage();
            }
        }

         doSaveAction(action, rightShift){
            return new Promise(resolve => {

                if (eOptions.saveDataAsync != null) {
                    let hierarchyIds = this.getHierarchyIds().split('/');

                    if(hierarchyIds.length >= 4) {
                        let isArgumentToMain = eOptions.opinionCanAddOpinion && this.type === TYPE_ARGUMENT && this.parent.type === TYPE_CATEGORY;
                        let newContextId = eOptions.categoryCanAddCitation ? formerContribution : hierarchyIds[hierarchyIds.length - 2];
                        let newShadeId = getShadeId(hierarchyIds[hierarchyIds.length - 3]);
                        let newCategoryId = eOptions.opinionCanAddOpinion ? -1 : hierarchyIds[hierarchyIds.length - 4];
                        let newSuperArgumentId = isArgumentToMain ? this.parent.id : hierarchyIds.length >= 5 ? hierarchyIds[0] : -1;
                        let order = this.order;

                        eOptions.saveDataAsync(this.linkId, newContextId, newCategoryId, newSuperArgumentId, newShadeId, action === TARGET_ACTION_COPY, order).done(function (data) {
                            resolve(true);
                        }).fail(function (err) {
                            if (err.status === 401) {
                                redirectToLogin();
                            } else {
                                resolve(false);
                                console.log("Error while saving changes.");
                            }
                        });
                    } else {
                        eOptions.saveDataAsync(this.linkId, -1, -1, -1, -1, false, this.order).done(function (data) {
                            resolve(true);
                        }).fail(function (err) {
                            if (err.status === 401) {
                                redirectToLogin();
                            } else {
                                resolve(false);
                                console.log("Error while saving changes.");
                            }
                        });
                    }
                } else {
                    resolve(true);
                }
            });
        }

        initContainer() {
            let that = this;

            let newContainer = this.createEmptyContribution();

            if(this.container != null) {
                this.container.replaceWith(newContainer)
            }

            this.container = newContainer;
            this.container.attr('data-id', this.getId());

            if(this.shade !== undefined) {
                this.container.addClass(this.shade === 0 ? SHADE_JUSTIFY : SHADE_OPPOSES);
            }

            let titleContainer = this.container.find(getTypeAsTitleClassName(this.getTypeName()));

            if(this.isDraggable)
                titleContainer.addClass('draggable');

            if(this.hiddenTitle) {
                titleContainer.removeClass('d-flex');
                titleContainer.hide();
            }

            this.initListeners();
        }

        createEmptyContribution() {
            let contributionTypeName = getElementTypeNameByType(this.type);
            let contribution = $(container.find('#empty-' + contributionTypeName + '-container').clone().html());
            let contributionTitle = contribution.find(getTypeAsTitleClassName(contributionTypeName));

            contribution.data('is-drag', true);
            contribution.data('id', this.id);
            contribution.data('link', this.linkId);
            contribution.data('complete-id', this.getId());
            contribution.data('complete-hierarchy', this.getHierarchyIds());

            if(eOptions.opinionCanAddOpinion && eOptions.dataId != null) {
                contribution.data('context', eOptions.dataId);
            }

            let panel = contributionTitle.find('.contribution-panel');

            if(!this.fake) {
                if (typeof this.drawPenMenu === 'function') {
                    if ((!eOptions.categoryCanAddCitation && this.id !== -1)
                        || (eOptions.categoryCanAddCitation && this.id !== eOptions.dataId)) {
                        panel.find('.dropdown-toggle-pen').next().html(this.drawPenMenu());
                    } else {
                        panel.find('.dropdown-toggle-pen').hide();
                    }
                }
                if (typeof this.drawPlusMenu === 'function') {
                    if (!this.noAction) {
                        panel.find('.dropdown-toggle-plus').next().html(this.drawPlusMenu());
                    } else {
                        panel.find('.dropdown-toggle-plus').hide();
                    }
                }
            } else {
                contribution.addClass('faked');
                panel.empty();
            }

            contributionTitle.find('span').first().text(this.name);

            if(this.id === -1){
                contributionTitle.find('.shaded-element').hide();
            }

            return contribution;
        }

        initListeners() {
            let that = this;
            let title_container = this.container.children(getTypeAsTitleClassName(this.getTypeName()));

            //this.initToggleMenuHandler(MENU_PEN, title_container);
            //this.initToggleMenuHandler(MENU_PLUS, title_container);

            this.container.on('open', function(evt, target){
                if(target.is('.nav-link')) {
                    target.click();
                }
                else {
                    that.collapseTarget(target.siblings().first());
                }
            });

            title_container.contextmenu(function(evt) {
                evt.preventDefault();
                $(this).find('.dropdown-toggle-points').click();
            });

            title_container.on('click', function(evt){
                if(!$(evt.target).is('.contribution-panel-dropdowns') && !$(evt.target).parents('.contribution-panel-dropdowns').exists()) {
                    that.collapseTarget($(this).siblings().first(), true);
                }
            });

            title_container.on('click', function() {
                title_container.find('.dropdown-menu.show').removeClass('show');
            });

            title_container.on('click', '.copy',function(){
                that.copy();
            });

            title_container.on('click', '.merge',function(){
                that.merge();
            });

            title_container.on('click', '.similar',function(){
                that.similar();
            });

            title_container.on('click', '.toggle-collapse',function(){
                title_container.click();
            });
        }

        collapseTarget(target, toggle) {
            toggle = toggle || false;
            target.collapse(toggle ? 'toggle' : 'show');
        }

        hasContent() {
            return false;
        }

        countContent(){
            return 0;
        }

        draw() {
            this.initContainer();

            return this.container.find(getTypeAsContainerClassName(this.getTypeName()));
        }

        drawPenMenu() {

        }

        drawPenMenu() {
            return "";
        }

        drawPlusMenu(){
            return "";
        }

        drawMenuItem(action, logo, title, data) {
            return '<button class="dropdown-item ' + action + '" type="button" ' + data + '><i class="fas ' + logo + ' text-primary mr-1"></i>' + title + '</a>';
        }

        drawPenItem(action, title, data) {
            return this.drawMenuItem(action, 'fa-pen', title, data);
        }

        drawPlusItem(action, title, data) {
            return this.drawMenuItem(action, 'fa-plus', title, data);
        }

        static drawMenuDivider() {
            return '<div class="dropdown-divider"></div>';
        }

        static removeFromArray(array, id){
            return array.filter(element => element.getId() !== id);
        }

        static sortArray(array) {
            if(Array.isArray(array)) {
                array.sort((c1, c2) => c1.id === -1 ? -1 : c2.id === -1 ? 1 : c1.order - c2.order);
            }
        }

        static drawArray(parent, array, container, toAppend, width) {
            if(Array.isArray(array)) {
                this.doDrawArray(parent, array.filter(c => c.id !== -1), container, toAppend, width);
                this.doDrawArray(parent, array.filter(c => c.id === -1), container, toAppend, width);
            }
        }

        static doDrawArray(parent, array, container, toAppend, width) {
            toAppend = toAppend === undefined ? true : toAppend;
            width = width === undefined ? null : width;

            array.forEach(contribution => {
                if((contribution.fake || targetDraggable == null || targetDraggable.data('id') !== contribution.getId()) &&
                    (contribution.type !== TYPE_ARGUMENT || contribution.id !== -1 || contribution.hasContent())) {

                    let toDraw = contribution.draw();

                    if (width != null) {
                        toDraw.css('width', width + '%');
                    }

                    toAppend ? container.append(toDraw) : container.prepend(toDraw);
                }
            });
        }
    }

    class Classifier extends Contribution {
        constructor(id, linkId, name, toClassify, limitType, classifierType){
            super(id, linkId, name, TYPE_CLASSIFIER, false);
            this.toClassify = toClassify;
            this.limitType = limitType;
            this.classifierType = classifierType;
            this.shadeClassifiers = [];
        }

        clone() {
            let cloned = new Classifier(this.id, this.linkId, this.name, this.toClassify, this.limitType);

            this.shadeClassifiers.forEach(shade => cloned.addShade(shade.clone(), false));

            return cloned;
        }

        getShadeClassifier(id){
            return this.shadeClassifiers.find(shadeClassifier => shadeClassifier.id === id);
        }

        addShade(shadeClassifier, redraw){
            this.shadeClassifiers.push(shadeClassifier);
            shadeClassifier.parent = this;
            shadeClassifier.add(redraw);
        }

        addContributionByShade(contribution) {
            let shade_classifier = contribution.searchParent(TYPE_SHADE_CLASSIFIER);

            if(shade_classifier != null) {
                let new_shade_classifier = this.shadeClassifiers.find(classifier => classifier.id === shade_classifier.id);

                if(new_shade_classifier && new_shade_classifier.hasCategories()){
                    new_shade_classifier.categories[0].addChild(contribution);
                }
            }
        }

        removeShade(shadeClassifier, redraw){
            this.categories = Contribution.removeFromArray(this.shadeClassifiers, shadeClassifier.getId());
            shadeClassifier.remove(redraw);
            shadeClassifier.parent = null;
        }

        countContent(){
            return this.categories.length;
        }

        draw() {
            let container = super.draw();

            let titleContainer = this.container.find(getTypeAsTitleClassName(this.getTypeName()));

            switch(this.classifierType) {
                case CLASSIFIER_TYPE_ADD :
                    titleContainer.html('<button type="button" class="btn btn-primary add-classifier m-2"><i class="fas fa-plus mr-1"></i>Ajouter un item</button>');
                    break;
                case CLASSIFIER_TYPE_SEPARATOR :
                    titleContainer.html('<div class="w-100"><hr class="m-2"></div>');
                    break;
                default:
                    if(eOptions.categoryCanAddCitation && this.shadeClassifiers.length === 3) {
                        let leftContainer = $('<div class="shaded-classifier-div"></div>').appendTo(container);
                        this.shadeClassifiers[0].draw().appendTo(leftContainer);

                        let rightContainer = $('<div class="shaded-classifier-div"></div>').appendTo(container);
                        this.shadeClassifiers[1].draw().appendTo(rightContainer);
                        this.shadeClassifiers[2].draw().appendTo(rightContainer);
                    } else {
                        Contribution.drawArray(this, this.shadeClassifiers, container, true, Math.floor(100 / this.shadeClassifiers.length));
                    }
            }

            return this.container;
        }
    }

    class ShadedClassifier extends Contribution {
        constructor(id, name, noAction, isParent){
            super(id, null, name, TYPE_SHADE_CLASSIFIER, false);
            this.categories = [];
            this.noAction = noAction;
            this.isParent = isParent;
        }

        clone() {
            let cloned = new ShadedClassifier(this.id, null, this.name);

            this.categories.forEach(categorie => cloned.addCategory(categorie.clone()));

            return cloned;
        }

        getDefaultCategory(){
            return this.categories.find(category => category.id === -1);
        }

        async addCategory(category, action, redraw, order, fake){
            fake = fake === undefined ? false : fake;

            if(order !== -1)
                this.removeFake(fake);

            if(fake) {
                category = category.clone();
            }

            if(!fake && this.containsCategory(category)) {
                this.categories = this.categories.filter(c => c.id !== category.id || c.fake);
            }

            if(order === -1) {
                this.categories.find(c => c.fake).fake = false;
            } else {
                category.parent = this;
                category.fake = fake;
            }

            if(action) {
                if (order == null) {
                    this.categories.push(category);
                } else if (order === 0) {
                    this.categories.unshift(category)
                } else if(order !== -1) {
                    this.categories.splice(this.categories.indexOf(order) + 1, 0, category);
                }

                if(!fake)
                    this.removeFake(fake);

                await this.updateCategoriesOrder(fake);

                if(fake) {
                    category.add(redraw, action, fake);
                } else {
                    //initData();
                }
            } else {
                this.categories.push(category);
                category.add(redraw);
            }
        }

        removeCategory(category, redraw){
            this.categories = Contribution.removeFromArray(this.categories, category.getId());
            category.remove(redraw);
            category.parent = null;
        }

        containsCategory(category) {
            return this.categories.find(o => o.id === category.id && !o.fake) !== undefined;
        }

        hasCategories() {
            return this.categories.length > 0;
        }

        async updateCategoriesOrder(fake) {
            for (let iCategory in this.categories) {
                let category = this.categories[iCategory];

                if (category.id > -1) {
                    category.order = parseInt(iCategory);
                }
            }

            if(!fake) {
                for (let iCategory in this.categories) {
                    let category = this.categories[iCategory];

                    if (category && category.id > -1 && !category.fake) {
                        await category.doSaveAction();
                    }
                }
            }
        }

        removeFake(fake, redraw) {
            this.categories = this.categories.filter(o => !o.fake);

            if(formerParentObj != null && fake)
                formerParentObj.removeFake(false, true);

            if(fake)
                formerParentObj = this;

            if(redraw)
                this.draw();
        }

        draw() {
            let container = super.draw();

            this.container.addClass(this.id);
           // this.container.css('width', Math.floor(100 / this.parent.shadeClassifiers.length) + '%')

            Contribution.sortArray(this.categories);
            Contribution.drawArray(this, this.categories, container);

            return this.container;
        }

        drawPlusMenu() {
            let menu = '';

            if(!eOptions.categoryCanAddCitation && !eOptions.opinionCanAddOpinion)
                menu = super.drawPlusItem('add-category debate-add-category-btn', Messages('general.add.btn.category') + ' <i class="fas fa-folder ml-1"></i>');
            if(eOptions.opinionCanAddOpinion)
                menu = super.drawPlusItem('text-add-argument-btn', Messages('general.add.btn.argument.first') + ' <i class="fas fa-comment ml-1"></i>');
            if(eOptions.categoryCanAddCitation) {
                if(this.isParent) {
                    menu += super.drawPlusItem('contribution-add-tag-btn', Messages('general.add.btn.tag.parent') + ' <i class="fas fa-tag ml-1"></i>', 'data-parent=true');
                } else {
                    menu += super.drawPlusItem('contribution-add-tag-btn', Messages('general.add.btn.tag.child') + ' <i class="fas fa-tag ml-1"></i>', 'data-parent=false');
                }
            }

            return menu;
        }
    }

    class Folderable extends Contribution {

        constructor(id, linkId, name, type, isDraggable, hiddenTitle) {
            super(id, linkId, name, type, isDraggable, hiddenTitle);
            this.arguments = [];
            this.citations = [];
        }

        clone(cloned) {
            this.arguments.forEach(argument => cloned.addArgument(argument.clone(), false, false));
            this.citations.forEach(citation => cloned.addCitation(citation.clone(), null, false));
        }

        getDefaultArgument(){
            return this.arguments.find(argument => argument.id === -1);
        }

        mergeWith(contribution, redraw) {
            // merge categories only on same shade and what if same categories for all items like best presidents for USA ?
            /*if(this.type === contribution.type) {
                let that = this;

                contribution.arguments.forEach(argument => {
                    if(!that.containsArgument(argument)) {
                        that.addArgument(argument, TARGET_ACTION_ADD, false);
                    }
                });

                super.mergeWith(contribution, redraw);
                            if(this.type === contribution.type) {
                let that = this;

                contribution.citations.forEach(citation => {
                    if(!that.containsCitation(citation)) {
                        that.addCitation(citation, TARGET_ACTION_ADD, false);
                    }
                });

                super.mergeWith(contribution, redraw);
            }
            }*/
        }

        async addArgument(argument, action, redraw, order, fake, rightShift){
            action = action || null;
            fake = fake === undefined ? false : fake;

            if(this.type !== TYPE_ARGUMENT || eOptions.opinionCanAddOpinion) {
                if(order !== -1)
                    this.removeFake(fake);

                if(fake) {
                    argument = argument.clone();
                }

                argument = action === TARGET_ACTION_COPY ? argument.clone() : argument;

                if (argument.parent != null && argument.parent !== this && action !== TARGET_ACTION_COPY)
                    argument.parent.removeArgument(argument, redraw);

                if(!fake && this.containsArgument(argument)) {
                    this.arguments = this.arguments.filter(a => a.id !== argument.id || a.fake);
                }

                if(order === -1) {
                    this.arguments.find(a => a.fake).fake = false;
                } else {
                    argument.parent = this;
                    argument.fake = fake;
                }

                if(action) {
                    if (order == null) {
                        this.arguments.push(argument);
                    } else if (order === 0) {
                        this.arguments.unshift(argument)
                    } else {
                        this.arguments.splice(this.arguments.indexOf(order) + 1, 0, argument);
                    }

                    if(!fake)
                        this.removeFake(fake);

                    if(!fake && this.arguments.filter(a => a.id === argument.id).length === 2) {
                        this.arguments.splice(this.arguments.indexOf(this.arguments.find(a => a.id === argument.id)), 1);
                    }

                    await this.updateArgumentsOrder(fake);

                    if(fake) {
                        argument.add(redraw, action, fake, rightShift);
                    }
                } else {
                    this.arguments.push(argument);
                    argument.add(redraw, action, fake, rightShift);
                }
            }
        }

        removeArgument(argument, redraw){
            this.arguments = Contribution.removeFromArray(this.arguments, argument.getId());

            argument.remove(redraw);
            argument.parent = null;
        }

        containsArgument(argument) {
            return this.arguments.find(o => o.id === argument.id && !o.fake) !== undefined;
        }

        containsFakeArgument(id) {
            return this.arguments.find(o => o.fake && o.id === id) !== undefined;
        }

        async updateArgumentsOrder(fake) {
            for(let iArgument in this.arguments) {
                let argument = this.arguments[iArgument];
                if(argument.id > -1) {
                    argument.order = parseInt(iArgument);
                }
            }

            if(!fake) {
                for (let iArgument in this.arguments) {
                    let argument = this.arguments[iArgument];
                    if (argument.id > -1 && !argument.fake) {
                        await argument.doSaveAction();
                    }
                }
            }
        }

        async addCitation(citation, action, redraw, order, fake){
            action = action || null;
            fake = fake === undefined ? false : fake;

            if(order !== -1)
                this.removeFake(fake);

            if(fake) {
                citation = citation.clone();
            }

            citation = action === TARGET_ACTION_COPY ? citation.clone() : citation;

            if (citation.parent != null && citation.parent !== this && action !== TARGET_ACTION_COPY) {
                citation.parent.removeCitation(citation, redraw);
            }

            if(!fake && this.containsCitation(citation)) {
                this.citations = this.citations.filter(c => c.id !== citation.id || c.fake);
            }

            if(order === -1) {
                this.citations.find(c => c.fake).fake = false;
            } else {
                citation.parent = this;
                citation.fake = fake;
            }

            if(action) {
                if (order == null) {
                    this.citations.push(citation);
                } else if (order === 0) {
                    this.citations.unshift(citation)
                } else {
                    this.citations.splice(this.citations.indexOf(order) + 1, 0, citation);
                }

                if(!fake)
                    this.removeFake(fake);

                if(!fake && this.citations.filter(a => a.id === citation.id).length === 2) {
                    this.citations.splice(this.citations.indexOf(this.citations.find(a => a.id === citation.id)), 1);
                }

                await this.updateCitationsOrder(fake);

                if(fake) {
                    citation.add(redraw, action, fake);
                }
            } else {
                this.citations.push(citation);
                citation.add(redraw, action, fake);
            }
        }

        removeCitation(citation, redraw){
            this.citations = Contribution.removeFromArray(this.citations, citation.getId());

            citation.remove(redraw);
            citation.parent = null;
        }

        containsCitation(citation) {
            return this.citations.find(c => c.id === citation.id && !c.fake) !== undefined;
        }

        containsFakeCitation(id) {
            return this.citations.find(o => o.fake && o.id === id) !== undefined;
        }

        async updateCitationsOrder(fake) {
            for(let iCitation in this.citations) {
                let citation = this.citations[iCitation];
                citation.order = parseInt(iCitation);
            }

            if(!fake) {
                for (let iCitation in this.citations) {
                    let citation = this.citations[iCitation];
                    if (!citation.fake)
                        await citation.doSaveAction();
                }
            }
        }

        removeFake(fake, redraw) {
            this.citations = this.citations.filter(o => !o.fake);
            this.arguments = this.arguments.filter(o => !o.fake);

            if(formerParentObj != null && fake)
                formerParentObj.removeFake(false, true);

            if(fake)
                formerParentObj = this;

            if(redraw)
                this.draw();
        }

        hasContent() {
            return (this.arguments.length > 1 || (this.arguments.length === 1 && this.arguments[0].hasContent()))
                || this.citations.length > 0;
        }

        draw() {
            let container = super.draw();

            let panel = this.container.find('.contribution-panel');

            if(Array.isArray(this.arguments) && this.arguments.length > 0) {
                let nbArguments =
                    this.arguments.length - (eOptions.opinionCanAddOpinion || this.getDefaultArgument().citations.length > 0 ? 0 : 1);
                if(nbArguments > 0) {
                    panel.find('.nb-arguments').first().children('span').text(nbArguments);
                    panel.find('.nb-arguments').first().show();
                } else {
                    panel.find('.nb-arguments').first().hide();
                }
            } else {
                panel.find('.nb-arguments').first().hide();
            }

            if(Array.isArray(this.citations) && this.citations.length > 0) {
                panel.find('.nb-citations').first().children('span').text(this.citations.length);
                panel.find('.nb-citations').first().show();
            } else {
                panel.find('.nb-citations').first().hide();
            }

            if((this.type === TYPE_CATEGORY && this.id === -1) || isReloaded) {
            //if((this.hasContent() || this.hiddenTitle) && (this.type !== TYPE_CATEGORY || this.id === -1)) {
                container.addClass('show');
                if(this.hiddenTitle) {
                    container.css('min-height', '100px');
                }
            }

            Contribution.sortArray(this.arguments);
            Contribution.sortArray(this.citations);

            Contribution.drawArray(this, this.arguments, container);
            Contribution.drawArray(this, this.citations, container);

            return this.container;
        }
    }

    class Category extends Folderable {

        constructor(id, linkId, name, hidden) {
            super(id, linkId, name, TYPE_CATEGORY, true, hidden);
        }

        clone() {
            return new Category(this.id, this.linkId, this.name, this.hidden);
        }

        countContent(){
            return eOptions.categoryCanAddCitation ? this.citations.length : this.arguments.length;
        }

        drawPenMenu() {
            let menu = '';

            if(eOptions.opinionCanAddOpinion) {
                menu += super.drawPenItem('argument-edit-btn', Messages('general.btn.edit'));
                menu += super.drawPenItem('remove argument-delete-force-link-btn', Messages('general.label.btn.clearlink'));
            } else if(!this.isUndefined){
                menu += super.drawPenItem(eOptions.categoryCanAddCitation ? 'edit tag-edit-btn' : 'edit category-edit-btn', eOptions.categoryCanAddCitation ? Messages('tag.edit.title') : Messages('tag.category.edit.title'));
                //menu += super.drawPenItem('merge', "Fusionner la catégorie");
                menu += super.drawPenItem(eOptions.categoryCanAddCitation ? 'remove tag-delete-link-btn' : 'remove category-delete-force-link-btn', eOptions.categoryCanAddCitation ? Messages('tag.delete.title') : Messages('tag.category.delete.title'));
            }

            return menu;
        }

        drawPlusMenu() {
            let menu = '';

            if(eOptions.opinionCanAddOpinion) {
                menu += super.drawPlusItem('argument-add-argument-btn', Messages('argument.addsub.justify.label') + ' <i class="fas fa-comment ' + SHADE_JUSTIFY + ' ml-1"></i>', 'data-shade="0"');
                menu += super.drawPlusItem('argument-add-argument-btn', Messages('argument.addsub.opposes.label') + ' <i class="fas fa-comment ' + SHADE_OPPOSES + ' ml-1"></i>', 'data-shade="1"');
            }

            if(!eOptions.categoryCanAddCitation && !eOptions.opinionCanAddOpinion)
                menu += super.drawPlusItem('category-add-argument-btn', Messages('general.add.btn.argument') +' <i class="fas fa-comment ml-1"></i>');

            menu += super.drawPlusItem(eOptions.categoryCanAddCitation ? 'contribution-add-citation-btn' : 'category-add-citation-btn', Messages('general.add.btn.citation') + ' <i class="fas fa-align-left ml-1"></i>');

            return menu;
        }
    }

    class Argument extends Folderable {

        constructor(id, linkId, name, shade) {
            super(id, linkId, name, TYPE_ARGUMENT);
            this.shade = shade;
        }

        clone() {
            return new Argument(this.id, this.linkId, this.name, this.shade);
        }

        hasContent() {
            return super.hasContent() || this.citations.length > 0;
        }

        countContent(){
            return this.citations.length;
        }

        drawPenMenu() {
            let menu = '';
            //menu += super.drawPenItem('edit', "Editer l'opignion");
            menu += super.drawPenItem('argument-edit-btn', Messages('general.btn.edit'));

            if(eOptions.opinionCanAddOpinion)
                menu += super.drawPenItem('argument-change-shade-btn', Messages('argument.options.edit.change.shade'), 'data-link=' + this.linkId);
            if(!eOptions.categoryCanAddCitation)
                menu += super.drawPenItem('copy', Messages('argument.copy.title'));

            //menu += super.drawPenItem('merge', "Fusionner l'opignion");
            //menu += super.drawPenItem('similar', "Mise en similarité");
            //menu += super.drawPenItem('create-debate', "Créer le débat");
            menu += super.drawPenItem('remove argument-delete-force-link-btn', Messages('general.label.btn.clearlink'));
            return menu;
        }

        drawPlusMenu() {
            let menu = '';

            if(eOptions.opinionCanAddOpinion) {
                menu += super.drawPlusItem('argument-add-argument-btn', Messages('argument.addsub.justify.label') + ' <i class="fas fa-comment ' + SHADE_JUSTIFY + ' ml-1"></i>', 'data-shade="0"');
                menu += super.drawPlusItem('argument-add-argument-btn', Messages('argument.addsub.opposes.label') + ' <i class="fas fa-comment ' + SHADE_OPPOSES + ' ml-1"></i>', 'data-shade="1"');
            }

            menu += super.drawPlusItem('add-citation argument-add-citation-btn', Messages('general.add.btn.citation') + ' <i class="fas fa-align-left ml-1"></i>');

            return menu;
        }
    }

    class Citation extends Contribution{

        constructor(id, linkId, name) {
            super(id, linkId, name, TYPE_CITATION);
        }

        clone() {
            return new Citation(this.id, this.linkId, this.name);
        }

        draw() {
            super.draw();
            return this.container;
        }

        drawPenMenu() {
            let menu = '';
            //menu +=  super.drawPenItem('edit', "Editer la citation");
            menu += super.drawPenItem('citation-edit-btn', Messages('general.btn.edit'));

            if(!eOptions.categoryCanAddCitation) {
                menu += super.drawPenItem('copy',  Messages('citation.copy.title'));
                /*if(this.parent.id === -1 && this.searchParent(TYPE_CATEGORY).id === -1) {
                    menu += super.drawPenItem('remove-from-argument', "Supprimer");
                } else {
                    menu += super.drawPenItem('remove-from-category', "Supprimer de la catégorie");
                    menu += super.drawPenItem('remove-from-argument', "Supprimer de l'argument");
                }*/
                menu += super.drawPenItem('remove citation-delete-force-link-btn', Messages('general.label.btn.clearlink'));
            }
            return menu;
        }
    }


    function createFakeClassifiers(){
        contributions = {};
        classifiers = [];

        let classifier;
        let shadeClassifier;
        let category;
        let argument;

        classifier = new Classifier(-1, null, "", null, null, CLASSIFIER_TYPE_ADD);
        classifiers.push(classifier);
        contributions[classifier.id] = classifier;

        classifier = new Classifier(800, null, "Faut-il construire le métro nord ?", false);
        classifiers.push(classifier);

        shadeClassifier = new ShadedClassifier(SHADE_JUSTIFY, "Arguments en faveur du oui")
        classifier.addShade(shadeClassifier);

        category = new Category(1, null, "Mobilité");
        shadeClassifier.addCategory(category);

        argument = new Argument(111, null, "La circulation automobile diminuera avec la création du métro");
        category.addArgument(argument);

        argument = new Argument(12, null, "Le métro peut transporter plus de voyageurs que le tram");
        category.addArgument(argument);
        argument.addCitation(new Citation(1122, null, "Un métro peut acceuillir 24.000 voyageurs par heure et par sens"));


        category = new Category(2, null, "Finances");
        shadeClassifier.addCategory(category);

        argument = new Argument(12, null, "Le coût des travaux ne dépassera pas le budget annoncé");
        category.addArgument(argument);


        category = new Category(3, null, "Démographie");
        shadeClassifier.addCategory(category);


        shadeClassifier = new ShadedClassifier(SHADE_OPPOSES, "Arguments en faveur du non");
        classifier.addShade(shadeClassifier);

        category = new Category(1, null, "Mobilité");
        shadeClassifier.addCategory(category);

        argument = new Argument(12, null, "Le métro Nord ne génèrera pas de transfert d'automobilistes vers le metro");
        category.addArgument(argument);

        argument = new Argument(12, null, "Le métro Nord de pourrait réduire le trafic automobile que si de lourds investissements sont faits ailleurs");
        category.addArgument(argument);

        argument = new Argument(12, null, "Le métro comptera moins de stations que le tram");
        category.addArgument(argument);

        category = new Category(2, null, "Finances");
        shadeClassifier.addCategory(category);

        argument = new Argument(12, null, "Il y a généralement des surcoûts dans les travaux de métro");
        category.addArgument(argument);

        argument = new Argument(12, null, "Tout surplus de coût sera à charge de la Région Bruxelloise");
        category.addArgument(argument);

        category = new Category(3, null, "Démographie");
        shadeClassifier.addCategory(category);


        classifier = new Classifier(-1, null, "", null, null, CLASSIFIER_TYPE_SEPARATOR);
        classifiers.push(classifier);


        undefined_classifier = new Classifier(0, null, "Arguments non classés");
        undefined_classifier.limitType = TYPE_ARGUMENT;
        classifiers.push(undefined_classifier);

        shadeClassifier = new ShadedClassifier(SHADE_JUSTIFY, "Arguments en faveur du oui");
        undefined_classifier.addShade(shadeClassifier);

        category = new Category(0, null, "fake category", true);
        shadeClassifier.addCategory(category);

        category.addCitation(new Citation(1, null, "Un tram peut accueillir au maxium 2.200 voyageurs par heure et par sens"));
        category.addCitation(new Citation(2, null, "Grâce à la création de la future ligne de métro entre Bordet et Albert, la STIB pourra absorber le flux croissant de voyageurs, dans une région où la population s’accroit d’années en années"));
        category.addCitation(new Citation(12, null, "La capacité supérieure des rames de métro permet de les configurer pour maximiser le confort des usagers (plus de places assises)"));
        category.addCitation(new Citation(13, null, "Les risques d’accident de la circulation avec un métro sont nulles. Les usagers qui attendent aux stations souterraines sont également mieux protégés."));
        category.addCitation(new Citation(14, null, "Puisque le métro ne peut être ralenti ni arrêté par les accidents de la route, les travaux sur la chaussée, la circulation (notamment les véhicules prioritaires), la neige, le verglas ou les manifestations, il est plus fiable que le tramway"));


        shadeClassifier = new ShadedClassifier(SHADE_OPPOSES, "Arguments en faveur du non");
        undefined_classifier.addShade(shadeClassifier);

        category = new Category(0, null, "fake category", true);
        shadeClassifier.addCategory(category);

        category.addCitation(new Citation(4, null, "Annoncé pour 2025, le métro Nord ne sera pas prêt avant 2028 selon Beliris"));
        category.addCitation(new Citation(5, null, "La construction de la future ligne de métro Nord-Sud qui reliera la place Albert (Forest) à Bordet (Evere) requiert au préalable une modification partielle du Plan régional d’affectation du sol (Pras)"));
        category.addCitation(new Citation(6, null, "Réunis au sein du comité helm3tro depuis janvier 2017, riverains et commerçants dénoncent le caractère surdimensionné de ce projet qui ignore totalement les atouts paysagers et urbanistiques du square Riga"));
        category.addCitation(new Citation(7, null, "Plutôt que de lancer de gros projets d’infrastructure, mieux vaut d’abord améliorer l’existant"));
        category.addCitation(new Citation(8, null, "Il faut permettre à plus de trams de rouler en sites propres"));
        category.addCitation(new Citation(9, null, "Il faut développer les pistes cyclables"));
        category.addCitation(new Citation(10, null, "Il faut utiliser davantage le potentiel ferroviaire existant (il existe 34 gares en fonction à Bruxelles!)"));
        category.addCitation(new Citation(11, null, "il y a un conflit d’intérêts. Dans la mesure où le bureau d’études qui est à l’origine de la réflexion sur le projet est aussi chargé des études techniques pour la réalisation de celui-ci"));

        shadeClassifier = new ShadedClassifier(SHADE_NONE, "Arguments indeterimés");
        undefined_classifier.addShade(shadeClassifier);

        category = new Category(0, null, "fake category", true);
        shadeClassifier.addCategory(category);

        category.addCitation(new Citation(3, null, "La Région de Bruxelles-Capitale prendra à sa charge, au travers de la STIB et de son administration régionale, la construction des infrastructures nécessaires au passage du pré-métro au métro sur le tronçon existant entre la Gare du Nord et Albert. Beliris"));

        console.log(classifiers);

        initClassifiers();
    }

};}(jQuery));

