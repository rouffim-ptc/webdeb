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
(function (e) {
    e.fn.stepFormJs = function (options) {

        let eOptions,
            container = $(this),
            stepNum = container.find('#stepNum'),
            steps = [],
            currentStep = null,
            lock = false;

        const MODIFICATION_NEXT = 'next';
        const MODIFICATION_PREV = 'prev';
        const MODIFICATION_CURRENT = 'current';

        const NEXT_STEP_ANIMATION = 500;
        const NEXT_STEP_ANIMATION_SECURITY = 10;


        $(document).ready(function () {
            initOptions();
            initListeners();
            initStepper();
        });


        function initStepper() {
            steps = [];

            container.find('.step-form').each(function () {
                $(this).addClass('right');
                steps.push(new Step($(this).index(), $(this)));
            });

            container.find('textarea').removeClass('is-invalid');

            container.find('textarea').on('mouseup keyup', function(){
                $(this).removeClass('is-invalid');
            });

            let step = eOptions.beginStep != null ? eOptions.beginStep : 0;
            currentStep = steps.length > step ? steps[step] : null;
            changeStep(MODIFICATION_CURRENT);
        }

        function initOptions() {
            if (options !== null && typeof (options) !== 'undefined') {
                eOptions = {
                    redirectLink: options.redirectLink !== undefined ? options.redirectLink : null,
                    validationCall: typeof options.validationCall === "function" ? options.validationCall : null,
                    submissionCall: typeof options.submissionCall === "function" ? options.submissionCall : null,
                    toCheckSyncCall: typeof options.toCheckSyncCall === "function" ? options.toCheckSyncCall : null,
                    toCheckSyncCallFunc: typeof options.toCheckSyncCallFunc === "function" ? options.toCheckSyncCallFunc : null,
                    panelManager: options.panelManager !== undefined && typeof options.panelManager === "function"? options.panelManager : null,
                    alwaysDisplaySubmit : typeof(options.alwaysDisplaySubmit) === "boolean" ? options.alwaysDisplaySubmit : false,
                    stepActionsAddon : options.stepActionsAddon instanceof jQuery ? options.stepActionsAddon : null,
                    beginStep : Number.isInteger(options.beginStep) ? options.beginStep : null,
                    canStepEnd : Number.isInteger(options.canStepEnd) ? options.canStepEnd : null,
                    cancelRedirection : typeof options.cancelRedirection === "boolean" ? options.cancelRedirection : null,
                    cancelAction : typeof options.cancelAction === "function" ? options.cancelAction : null
                };
            } else {
                eOptions = {
                    redirectLink: null,
                    validationCall: null,
                    submissionCall: null,
                    toCheckSyncCall: null,
                    toCheckSyncCallFunc: null,
                    panelManager: null,
                    alwaysDisplaySubmit : false,
                    stepActionsAddon : null,
                    canStepEnd : null,
                    cancelRedirection : null,
                    cancelAction : null
                };
            }
        }

        function initListeners() {

            container.on('click', 'button', function (evt) {
                if (!$(this).hasClass('hide-submit'))
                    evt.preventDefault();
            });

            container.on('click', '.step-prev', function (evt) {
                if (!lock) {
                    lock = true;
                    prevStep();
                }
            });

            container.on('click', '.step-cancel', function (evt) {
                if(eOptions.cancelAction){
                    eOptions.cancelAction().done(function(){
                        container.parents('.modal').modal('hide');
                    });
                } else if(eOptions.cancelRedirection){
                    window.history.back();
                }
            });

            container.on('click', '.form-check.form-option-action', function (evt) {
                $(this).find('input:visible').first().prop( "checked", true );
            });

            container.on('click', '.checked-step-next', function (evt) {
                stepNextAction();
            });

            container.on('click', '.step-next', function (evt) {
                stepNextAction();
            });

            container.on('click', '.submit', function (evt) {
                if (!lock) {
                    lock = true;
                    submitForm();
                }
            });

            container.on('click', '.redirect', function (evt) {
                if (eOptions.redirectLink !== null) {
                    window.location = eOptions.redirectLink;
                } else {
                    // submit
                }
            });

            container.find('input').on('keypress', function(e) {
                if (e.which === 13) {
                    e.stopPropagation();
                    e.preventDefault();
                }
            });

            container.on('click', '.form-option-action', function (evt) {
                if (currentStep != null && currentStep.container != null) {
                    if ($(this).hasClass('form-radio-option-action')) {
                        currentStep.container.find('.form-radio-option-' + $(this).data('group')).hide();
                    }

                    let type = $(this).data('type');
                    if (type === "radio")
                        currentStep.container.find('.form-option').hide();

                    let target = $(this).data('target');
                    currentStep.container.find('.form-option' + (target !== undefined ? '.form-option-' + target : '')).toggle($(this).data('action'));

                    let target2 = $(this).data('target2');
                    if(target2 !== undefined)
                        currentStep.container.find('.form-option.form-option-' + target2).toggle($(this).data('action'));

                }
            });

        }

        function stepNextAction() {
            if (!lock) {
                lock = true;

                stepValidation().then((val) => {
                    if (val) {
                        container.find('textarea').removeClass('is-invalid');

                        container.find('textarea').on('mouseup keyup', function(){
                            $(this).removeClass('is-invalid');
                        });
                        nextStep();
                    } else {
                        lock = false;
                    }
                });
            }
        }

        function stepValidation() {
            return new Promise(async function (resolve, reject) {
                let hasNoErrors = true;

                if (currentStep != null && currentStep.container != null) {
                    let nextBtn = currentStep.container.find('.step-btn');
                    let waitForIt = currentStep.container.find('.step-wait');
                    let validFeedback = currentStep.container.find('.valid-feedback');

                    nextBtn.prop("disabled", true);
                    waitForIt.show();

                    currentStep.container.find('input[required]:visible, select[required]:visible, textarea[required]:visible').each(function () {

                        let formGroup = $(this).parents('.form-group');
                        let invalidFeedback = formGroup.find('.invalid-feedback');

                        if (!$(this)[0].checkValidity()) {
                            hasNoErrors = false;

                            $(this).addClass('is-invalid');
                            $(this).removeClass('is-valid');

                            invalidFeedback.text($(this)[0].validationMessage);
                            invalidFeedback.show();
                        } else {
                            $(this).removeClass('is-invalid');

                            if (!$(this).is('input[type="checkbox"], input[type="radio"]')) {
                                $(this).addClass('is-valid');
                            }

                            invalidFeedback.text('');
                            invalidFeedback.hide();
                        }
                    });

                    if (eOptions.validationCall != null) {

                        /*await new Promise((resolve) =>
                                setTimeout(function(){
                                    resolve();
                                }, 3000)
                            );*/

                        let validationResponse = await doValidationCall();

                        if(validationResponse.status) {

                            if (currentStep.nextStepReloadNext) {
                                for(let iStep = currentStep.stepNum + 1; iStep < steps.length; iStep++) {
                                    replaceStepWithStepInContainer(steps[iStep], $(validationResponse.response), true, false);
                                }

                                if(eOptions.panelManager != null){
                                    eOptions.panelManager(container);
                                }
                            }
                        } else {
                            switch(validationResponse.response.status){
                                case 400 :
                                    replaceStepWithStepInContainer(currentStep, $(validationResponse.response.responseText));
                                    break;
                                case 406 :
                                    nextStep();
                                    replaceStepWithStepInContainer(currentStep, $(validationResponse.response.responseText), true);
                                    break;
                                default :
                                    slideDefaultErrorMessage();
                            }
                        }

                        hasNoErrors = hasNoErrors && validationResponse.status;
                    }

                    if (hasNoErrors) {
                        validFeedback.show();
                    } else {
                        validFeedback.hide();
                    }

                    nextBtn.prop("disabled", false);
                    waitForIt.hide();
                }

                return resolve(hasNoErrors);
            });
        }

        function replaceStepWithStepInContainer(step, newSteps, replaceAll, replacePanelManager) {
            step.replaceContainer(newSteps.find('.step-form').eq(step.stepNum), replaceAll, replacePanelManager);
        }

        function isNextStepCanBeSkipped() {
            if (currentStep != null && currentStep.container != null) {
                let csn = currentStep.container.find('.checked-step-next');
                return csn.exists() && csn.prop('checked');
            }

            return false;
        }

        function doValidationCall() {
            return new Promise(async function (resolve, reject) {
                stepNum.val(currentStep.stepNum);
                eOptions.validationCall(container).done(function (data) {
                    resolve({status : true, response: data});
                }).fail(function (err) {
                    resolve({status : false, response: err});
                });
            });
        }

        function submitForm() {
            stepValidation().then((val) => {
                if (val) {
                    currentStep.container.find('.step-wait').show();
                    currentStep.container.find('.step-btn').prop("disabled", true);

                    if(eOptions.submissionCall != null) {
                        eOptions.submissionCall(container);
                    } else if(eOptions.toCheckSyncCall != null) {
                        eOptions.toCheckSyncCall(eOptions.toCheckSyncCallFunc, container, container.find('#id').val());
                    } else {
                        container.find('.hide-submit').click();
                    }
                }
                lock = false;
            });
        }

        function prevStep() {
            if (currentStep != null && steps[currentStep.stepNum - 1] !== undefined) {
                changeStep(MODIFICATION_PREV);
            } else {
                lock = false;
            }
        }

        function nextStep() {
            if (currentStep != null) {
                if (steps[currentStep.stepNum + 1] !== undefined) {
                    changeStep(MODIFICATION_NEXT);
                } else {
                    lock = false;
                }
            } else {
                lock = false;
            }
        }

        function changeStep(modification) {
            if (currentStep != null) {
                if (modification === MODIFICATION_CURRENT) {
                    currentStep.toggleContainer(true);
                    lock = false;
                } else {
                    let prevStep = currentStep;
                    currentStep = steps[currentStep.stepNum + (modification === MODIFICATION_NEXT ? 1 : -1)];

                    prevStep.changeStep(modification);
                    currentStep.changeStep(modification);

                    setTimeout(function () {
                        prevStep.toggleContainer(false, modification);
                        currentStep.toggleContainer(true);
                        lock = false;

                        if(modification === MODIFICATION_NEXT && isNextStepCanBeSkipped()) {
                            stepNextAction();
                        }

                    }, NEXT_STEP_ANIMATION + NEXT_STEP_ANIMATION_SECURITY);
                }

                container.scrollTop(0);

                currentStep.buildStepAction();
                currentStep.updateStateNum();
            }
        }

        function reloadAllNextSteps() {

        }

        class Step {

            constructor(stepNum, container) {
                this.stepNum = stepNum;
                this.setContainer(container);
            }

            setContainer(container, show){
                let isJQuery = container instanceof jQuery;

                if(isJQuery && this.container != null) {
                    this.container.replaceWith(container);
                }

                this.container = isJQuery ? container : null;
                this.nextStepReloadNext = this.container != null && this.container.hasClass('step-form-reload-next');

                this.initStepContainer();
                this.initStepNumContainer();

                if(show) {
                    this.toggleContainer(show);
                    this.buildStepAction();
                    this.updateStateNum();
                }
            }

            replaceContainer(newContainer, replaceAll, reloadPanelManager) {
                replaceAll = replaceAll === undefined ? this.container.hasClass('step-form-replace-all') : replaceAll;
                reloadPanelManager = reloadPanelManager === undefined ? true : reloadPanelManager;

                if (newContainer instanceof jQuery) {
                    let that = this;

                    if(!replaceAll) {
                        newContainer.find('input, select, textarea').each(function () {
                            if ($(this).attr('id') !== undefined) {
                                if ($(this).hasClass('is-invalid') || $(this).hasClass('invalid')) {
                                    let superFormGroup = $(this).parents('.form-super-group');
                                    let selector = superFormGroup.exists() ? '.form-super-group' : '.form-group';
                                    that.container.find('#' + $(this).attr('id')).parents(selector).replaceWith($(this).parents(selector));
                                } else if(that.container.find('#' + $(this).attr('id') + "-error").exists()) {
                                    that.container.find('#' + $(this).attr('id') + "-error")
                                        .replaceWith(newContainer.find('#' + $(this).attr('id') + "-error"));
                                }
                            }
                         });
                    } else {
                        this.container.find('.step-form-content').replaceWith(newContainer.find('.step-form-content'));
                    }

                    if(eOptions.panelManager != null && reloadPanelManager){
                        eOptions.panelManager(container);
                    }

                    this.container.find('input').on('keypress', function(e) {
                        if (e.which === 13) {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    });
                }

            }

            initStepContainer() {
                if (this.container != null) {
                    let stepContainer = this.container.find('.step-form-actions');
                    this.stepContainer = stepContainer.exists() ? stepContainer : null;
                } else {
                    this.stepContainer = null;
                }
            }

            initStepNumContainer() {
                if (this.container != null) {
                    this.stepNumContainer = this.container.find('.step-num');
                    this.stepNumContainer = this.stepNumContainer.exists() ? this.stepNumContainer : null;

                    this.stepMaxContainer = this.container.find('.step-max');
                    this.stepMaxContainer = this.stepMaxContainer.exists() ? this.stepMaxContainer : null;
                } else {
                    this.stepNumContainer = null;
                    this.stepMaxContainer = null;
                }
            }

            toggleContainer(show, direction) {
                if (this.container != null) {
                    this.container.toggleClass('active', show);
                    this.container.toggleClass('left', !show);
                    this.container.toggleClass('right', !show);
                    this.container.removeClass('prev');

                    if (direction) {
                        switch (direction) {
                            case MODIFICATION_NEXT :
                                this.container.addClass('left');
                                this.container.removeClass('right');
                                break;
                            case MODIFICATION_PREV :
                                this.container.removeClass('left');
                                this.container.addClass('right');
                                break;
                        }
                    }
                }
            }

            changeStep(modification) {
                if (this.container != null) {
                    this.container.addClass('prev');
                    this.container.removeClass('active');

                    this.container.css('left', '0');

                    this.container
                        .animate({
                            left: (modification === MODIFICATION_NEXT ? "-" : "") + "100%"
                        }, {
                            queue: false,
                            duration: NEXT_STEP_ANIMATION
                        });
                }
            }

            updateStateNum() {
                if (this.stepNumContainer != null) {
                    this.stepNumContainer.text(this.stepNum + 1);

                    if (this.stepMaxContainer != null) {
                        this.stepMaxContainer.text(steps.length);
                    }
                }
            }

            buildStepAction() {
                if (this.stepContainer != null) {

                    if(eOptions.stepActionsAddon != null && eOptions.stepActionsAddon.exists()) {
                        eOptions.stepActionsAddon.show();
                        this.stepContainer.find('.step-form-actions-addon').append(eOptions.stepActionsAddon);
                    }

                    if (this.stepNum > 0) {
                        this.stepContainer.find('.step-prev').show();
                    } else {
                        this.stepContainer.find('.step-cancel').show();

                        if(eOptions.cancelAction) {
                            this.stepContainer.find('.step-cancel').data('dismiss', 'false');
                        }
                    }

                    if (this.stepNum + 1 < steps.length) {
                        this.stepContainer.find('.step-next').show();
                    }

                    if(eOptions.alwaysDisplaySubmit || this.stepNum === steps.length - 1
                        || (eOptions.canStepEnd != null && this.stepNum >= eOptions.canStepEnd)) {
                        this.stepContainer.find('.submit' + (eOptions.stepActionsAddon != null && eOptions.stepActionsAddon.exists() ? '-savein' : '-save')).show();
                        this.stepContainer.find('.step-form-actions-addon').show();
                    }
                }
            }
        }

    };
}(jQuery));
