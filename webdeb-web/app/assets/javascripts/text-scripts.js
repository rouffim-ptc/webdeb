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
 * This javascript file contains all reusable function for text contributions' screens
 *
 * @author Fabian Gilson
 * @author Nicolas Forêt (initial contributor of JustificationTree)
 * @author Martin Rouffiange
 */

/**
 * Manage the whole text form panel
 */
function manageTextPanel(container, fromModal) {

    let id = container.find('#id');

    let textPublicationDate = container.find('#publicationDateGroup');
    let title = container.find('#title');
    let titles = container.find('#titles');

    let authors = container.find('#authors');
    let sourceAuthors = container.find('#sourceAuthors');
    let textSourceType = container.find('#textSourceType');

    let isOnInternet = container.find('#b-isOnInternet').find('input[type="checkbox"]');
    let isOnInternetTrue = container.find("#isOnInternet-true");
    let isOnInternetFalse = container.find("#isOnInternet-false");
    let isOnInternetInput = container.find('#isOnInternet');

    let url = container.find('#url');
    let waitForIt = container.find('#wait-for-it');
    let isNotSame = container.find('#isNotSame');
    let submit = container.find('#submit');

    let textOrigin = container.find('#b-textOrigin');
    let textOriginInput = container.find('#textOrigin');

    let upload_container = container.find('#upload-div');
    let upload_input = upload_container.find('input[name="upload"]');
    let upload_input_clear = upload_container.find('.file-input-clear');

    let textarea = container.find('#textarea');
    let fakeTextarea = container.find('#fakeTextarea');
    let filename = container.find('#filename');

    let textVisibility = container.find('#text-viz');
    let textVisibilityInput = container.find('#textVisibility');

    let textContentImportBtn = container.find('.text-import');
    let textContentDeleteBtn = container.find('.text-delete');

    let isAdmin = container.find('#isAdmin');

    if(fromModal) {
        submit.off('click');
        submit.on('click', function(){
            saveTextFromModal(container);
        });

        title.typeahead('destroy');
        titles.typeahead('destroy');
        authors.find('input').typeahead('destroy');
    } else {
        submit.off('click');
        submit.on('click', function(e){
            e.preventDefault();
            let form = container.find('form');

            startWaitingModal();

            saveText(id.val(), form).done(function (data) {
                slideDefaultSuccessMessage();
                window.location.href = JSON.parse(decodeURI(data));
            }).fail(function (xhr) {
                let status = xhr.responseText.length === 0 ? -1 : xhr.status;
                stopWaitingModal();

                switch (status) {
                    case 400:
                        container.show(xhr.responseText);
                        // form has errors => clear form and rebuilt
                        replaceContent('fieldset', xhr.responseText, 'fieldset');
                        manageTextPanel(container, true);
                        fadeMessage();
                        break;

                    case 406:
                        replaceContent('fieldset', xhr.responseText, 'fieldset');
                        manageTextPanel(container, true);
                        break;

                    case 409:
                        container.hide();
                        // actor names match
                        treatHandleContributionMatchesModal(container, xhr.responseText, submit);
                        break;

                    default:
                        // any other (probably a crash)
                        hideAndDestroyModal(container);
                        slideDefaultErrorMessage();
                }
            });
        });
    }

    addActorsTypeahead(authors, 'fullname', '-1');
    addActorsTypeahead(authors, 'affname', '1', undefined, false);

    manageActorButton(authors, ['fullname', 'affname'], '-1');
    manageActorButton(sourceAuthors, ['fullname', 'affname'], '-1');
    manageAddRmButton(titles, ['name'], "text.delete.confirm.");

    // manage exclusive boxes for gender and statuses
    manageExclusiveCheckboxes(isOnInternet, isOnInternetInput);
    manageExclusiveCheckboxes(textVisibility .find('input[type="checkbox"]'), textVisibilityInput, true);

    // if we have a "same title" to handle
    if ($('#text_candidate_modal').length > 0) {
        handleCandidates(isNotSame, submit);
    }

    if(isOnInternetTrue.is(':checked') || isOnInternetFalse.is(':checked')){
        toggleIsOnInternet(container, isOnInternetTrue.is(':checked')? 'true' : 'false');
    }

    // handle click event on isOnInternet
    isOnInternet.on('click', function () {
        toggleIsOnInternet(container, $(this).val());
    });

    // handle detection of existing text
    manageExistingText(title);
    // must tweak manually css from here because twitter-typeahead has trouble when no help-inline is given
    title.parent().addClass("no-help");

    // button group for text origin
    manageExclusiveCheckboxes(textOrigin.find('input[type="checkbox"]'), textOriginInput, true);

    // tooltip bubble management
    handleHelpBubble(container);

    // publication date widget management
    updateRoundedbox(textPublicationDate);
    textPublicationDate.find('.input-group-action2').each(function() {
        listenerOnRoundedBox('#publicationDateGroup');
    });

    // ajax on url insertion
    url.on('focusout', function () {
        findTextByUrl(url.val()).done(function (textId) {
            textId = JSON.parse(textId);

            if(id.val() === textId) {
                getTextFromUrl(url, '', waitForIt, isAdmin);
            } else {
                url.addClass('is-invalid');

                let err = $('<div>' + Messages("text.error.url.exiting2")  + '</div>');
                err.find('a').attr('href', urlOfTextViz(textId));

                let feedback = url.parents('.form-group').find('.invalid-feedback');
                feedback.html(err);
                feedback.show();
            }
        }).fail(function(){
            getTextFromUrl(url,'', waitForIt, isAdmin);
        });

        if(url.val().endsWith('.pdf')){
            upload_container.hide();
        } else {
            upload_container.show();
        }
    });

    url.keypress(function(e) {
        // TODO get data from site
        if (e.which === 13) {
            getTextFromUrl(url, '', waitForIt, isAdmin);
            e.stopPropagation();
            e.preventDefault();
        }
    });

    textContentImportBtn.on('click', function () {
        extractWebTextContent(url, '').then(() => {
            textarea.val(makeTextContentReadable(fakeTextarea.val()));
        });
    });

    textContentDeleteBtn.on('click', function () {
        textarea.val('');
    });

    if(textSourceType.val() === '1') {
        textarea.val('');
        textarea.prop('disabled', true);
        textContentImportBtn.hide();
        textContentDeleteBtn.hide();
    }

    // handle file input fields
    upload_input.on('change', function () {
        let file = this.files[0];
        // do not accept files larger than 50MB
        if (file.size > 52428800) {
            slideDefaultErrorMessage();
        } else {
            upload_input_clear.show();
            filename.val(file.name);
            showMe("#language-div", true, true);
            textarea.val('');
            textarea.prop('disabled', true);

            textContentImportBtn.hide();
            textContentDeleteBtn.hide();
        }
    });

    upload_input_clear.on('click', function () {
        filename.val('');
        upload_input.val('');
        upload_input_clear.hide();
        textarea.prop('disabled', false);

        textContentImportBtn.show();
        textContentDeleteBtn.show();
    });

    textarea.on('focusout', function () {
        // if textarea is cleaned and noContent is not set, show upload div
        // user maybe wants to upload a content
        if (textarea.val() === '') {
            showMe(upload_input, true, true);
            // cleanup also hidden name
            filename.val('');
        }
    });

    manageTextType();
}

function toggleIsOnInternet(container, val){
    showMe(container.find('#text-right-fields'), true, true);
    showMe(container.find('#text-left-fields'), true, true);
    showMe(container.find('#urldiv'), val === 'true', true);
    showMe(container.find('.text-on-internet'), val === 'true', true);
    showMe(container.find('.text-non-internet'), val === 'false', true);
    showMe(container.find('#submit-container'), true, true);
}

/*
 * ADD / EDIT TEXT
 */

function textOptionsHandler(container, isModal){
    container = isModal ? container.hasClass('modal') ? container : container.find('.modal').first() : container;
    let modalanchor = $('#merge-modal-anchor');

    // redirect to modal for adding an argument to the debate
    let addArgumentBtn = container.find('.text-add-argument-btn');
    addArgumentBtn.off('click');
    addArgumentBtn.on('click', function() {
        let data = actionFromDragnDrop($(this)) ? getDataFromActionDragnDrop($(this)) : null;
        openEditArgumentModal(
            null,
            actionFromDragnDrop($(this)),
            data != null ? data.context : getContributionOptionData($(this), "id"),
            null,
            null,
            null,
            0
        );
    });

    // redirect to the text linked to a debate
    let seeHistoryBtn = container.find('.text-see-history-btn');
    seeHistoryBtn.off('click');
    seeHistoryBtn.on('click', function() {
        getContributionHistoryModal(getContributionOptionData($(this), "id"), 2).done(function (html) {
            modalanchor.empty().append(html);
            modalanchor.find('.modal').modal('show');
        });
    });

    // redirect to modal for show the debate page
    let showBtn = container.find('.text-show-btn');
    showBtn.off('click');
    showBtn.on('click', function() {
        redirectToTextViz(getContributionOptionData($(this), "id"), -1);
    });

    // redirect to modal for editing tag debate
    let editBtn = container.find('.text-edit-btn');
    editBtn.off('click');
    editBtn.on('click', function() {
        if(isModal)
            hideAndDestroyModal(container);

        openEditTextModal(
            getContributionOptionData($(this), "id")
        );
    });

}

/**
 * open modal for edit or create a text
 *
 * param textId a text id
 */
function openEditTextModal(textId) {

    editText(textId).done(function (html) {
        loadAndShowModal($('#modal-anchor'), html);
        let modal = $("#modal-text");
        manageTextPanel(modal, true);
    }).fail(function (jqXHR) {
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            console.log("Error with text edit modal");
        }
    });
}

function saveTextFromModal(container) {
    let id = container.find('#id').val();
    let form = container.find('#text-form');

    saveText(id, form).done(function (data) {
        hideAndDestroyModal(container);
        triggerReloadVizEvent();
        slideDefaultSuccessMessage();
    }).fail(function (xhr) {
        let status = xhr.responseText.length === 0 ? -1 : xhr.status;

        switch (status) {
            case 400:
                container.show();
                // form has errors => clear form and rebuilt
                replaceContent(form, xhr.responseText, 'form');
                manageTextPanel(container, true);
                fadeMessage();
                break;

            case 406:
                replaceContent(form, xhr.responseText, 'form');
                manageTextPanel(container, true);
                break;

            case 409:
                container.hide();
                // actor names match
                treatHandleContributionMatchesModal(container, xhr.responseText);
                break;

            default:
                // any other (probably a crash)
                hideAndDestroyModal(container);
                slideDefaultErrorMessage();
        }
    });
}

/**
 * Get all text source names and auto-compleiton to given element
 *
 * @param element an hmtl element id
 * @param key the key (field from retrieved jspn data) to the field to be displayed
 */
function addSourceNameTypeahead(element, key) {
	$(element).typeahead({
		hint: false,
		highlight: true,
		autoselect: false,
		minLength: 2,
		limit: MAX_TYPEAHEAD
	}, {
		displayKey: key,
		source: function (query, process) {
			setTimeout(function () {
				return searchTextSource(query, MIN_TYPEAHEAD, MAX_TYPEAHEAD).done(function (data) {
					return process($.makeArray(data));
				});
			}, 300);
		},
		templates: {
			suggestion: function (item) {
				return item.sourceName;
			}
		}
	});
}

/**
 * Manage toggling of textType
 */
function manageTextType() {
  let boxes = $('#b-textType').find('input[type="checkbox"]');
  let field = $('#actortype');
  manageExclusiveCheckboxes(boxes, field, true);
}

/**
 * Manage typeahead selection of a text in fill information with data
 *
 *    @param element the html field where the typeahead will be put
 */
function manageExistingText(element) {
  addTextTypeahead(element, false);
    element.on('typeahead:selected', function (obj, datum) {
        // create modal popup for confirmation
        bootbox.dialog({
            message: Messages("entry.text.existing.text"),
            title: Messages("entry.text.existing.title"),
            buttons: {
                main: {
                    // do nothing if user chose not to load existing text
                    label: Messages("entry.text.existing.nok"),
                    className: "btn-primary",
                    callback: function () { /* do nothing, just dispose frame */
                    }
                },
                modify: {
                    label: Messages("entry.text.existing.ok"),
                    className: "btn-default",
                    callback: function () {
                        redirectToEdit(datum.id);
                    }
                }
            }
        });
    });
}

/**
 * Reload workwith text page when text is selected from pick list
 *
 * @param element the element to attache the typeahead to
 */
function textTypeahead(element) {
  addTextTypeahead(element, false);
  $(element).on('typeahead:selected', function (obj, datum) {
		redirectToTextExcerpts(datum.id);
  });
}

/**
 * Call annotation service for a given text
 *
 * @param container the container where add annotation
 * @param data previous loaded data if any
 * @param textid the id of the text to retrieve the annotated text
 * @param widthSize the container width size
 * @param heightSize the container hight size
 */
function updateAnnotated(container, data, textid, widthSize, heightSize) {
  if (textid !== -1) {
      if (data != null) {
          loadTextarea(container, data, widthSize, heightSize);
          return data;
      }else{
        let r = "";
        // launch ajax request to get annotated text
          getTextContentOrHtmlContent(textid, true).done(function (data) {
            let objData = JSON.parse(data);
            // ensure user did not load another one
            loadTextarea(container, objData, widthSize, heightSize);
            r = objData;
        }).fail(function (jqXHR) {
            if (jqXHR.status === 401) {
                loadTextarea(container, jqXHR.responseJSON, widthSize, heightSize);
                container.find('#textarea').html('<h4 class="text-muted">' + Messages('text.args.unautautorized') + '</h4>');

            } else if (jqXHR.status === 404) {
                loadTextarea(container, jqXHR.responseJSON, widthSize, heightSize);
                container.find('#textarea').html(jqXHR.responseJSON);
            } else {
                // will receive the full text back
                loadTextarea(container, jqXHR.responseJSON, widthSize, heightSize);
                showMe(container.find('#annotation-error'), true, true);
                showMe(container.find('#nlpboxes'), false, false);
            }
        });
        return r;
    }
  }
  return "";
}

/**
 * Load a text into a textarea, hide preview message and show legend
 *
 * @param container the container where add annotation
 * @param data the data to be loaded into the textarea
 * @param widthSize the container width size
 * @param heightSize the container hight size
 */
function loadTextarea(container, data, widthSize, heightSize) {
  showMe(container.find('#annotation-error'), false, false);
  let textarea = container.find('#textarea');

  let textareaWidthTemp = widthSize === undefined ? textarea.width() : widthSize;
  textarea.width((textareaWidthTemp-10)/2);
  textarea.css("height", "60vh");
  container.find('.next-page-indicator').parent().height(textarea.height());
  showMe(textarea, false, false);

  if(typeof data === 'object') {
      textarea.empty().append(data.text);
      textarea.annotator(data.excerpts, window.location.origin, false);
  }else{
      textarea.empty().append(data);
  }

  showMe(container.find('#preview'), false, true);
  showMe(textarea, true, true);
  showMe(container.find('#nlpboxes'), true, false);

  let textareaHeightTemp = heightSize === undefined ?  textarea.prop('scrollHeight') : heightSize;
  textarea.width(textareaWidthTemp);

  togglexxs();
  textPager(textarea, textareaHeightTemp);

  // detect window resize to reload text content
  let doit;
  window.onresize = function (){
      clearTimeout(doit);
      doit = setTimeout(function() {
          updateAnnotated(container, data, $('#text-id').val(), widthSize, heightSize);
      }, 50);
  };

  // auto-paging and highlights
  autoPageAndHighlights(textarea);
  initializeSearchText(textarea, true);
  showMe($('#search'), false, true);
  showMe($('#searchtext'), true, true);
  showMe($('#show-search'), true, true);
  updateTextarea(textarea);
}

/**
 * Register highlighter plugin for new excerpt extraction from text
 *
 * @param container the container concerned by the highlight
 * @param textareadiv the div containing the text
 */
function setHighlighter(container, textareadiv) {

    let excerptTooLong = container.find('.excerpt-too-long-msg');

    $(document).on('mousedown', function(e){
        excerptTooLong.hide();

        if(!$(e.target).hasClass('btn-new-citation-from-excerpt'))
            textareadiv.find(".highlighter-container").hide();
    });
/*
    textareadiv.on('mousedown', function(e){
        if(!$(e.target).hasClass('btn-new-citation-from-excerpt'))
            textareadiv.find('hr').remove();
    });*/

    textareadiv.on('mouseup', function(e){
        if(!$(e.target).hasClass('btn-new-citation-from-excerpt')) {
            let selection = window.getSelection();
            let excerpt = selection.toString().replace('\n', ' ');

            if (excerpt.length > 0) {
                //textareadiv.find('hr').remove();

               // $(selection.anchorNode).before('<hr>');
                // $(selection.anchorNode).after('<hr>');

                let elem = textareadiv[0].getBoundingClientRect();

                let decal = container.find(".modal-content").exists() ? container.find(".modal-content").offset().top * -1 : 0;

                let clientY = Math.floor(e.clientY - elem.top + decal + 140);
                let clientX = Math.floor(e.pageX - elem.left - 40);

                setTimeout(function () {
                    let highlighter_container = excerpt.length <= 600 ? textareadiv.find(".highlighter-container") : excerptTooLong;

                    if (highlighter_container.exists()) {
                        highlighter_container.show();
                        highlighter_container[0].style.top = clientY + "px";
                        highlighter_container[0].style.left = clientX + "px";
                    }
                }, 50);
            }

            if (excerpt.length === 0 || excerpt.length > 600) {
                textareadiv.find(".highlighter-container").hide();
            }

            textareadiv.find(".highlighter-container").find('.excerpt-to-add').text(excerpt);
        }
  });
}
/**
 * Show icon for auto-paging
 *
 * @param textareadiv div containing the text area
 */
function autoPageAndHighlights(textareadiv) {
  let mouseDown = 0;
  let nextpage = textareadiv.parents(".textzone").parent().find('.next-page-indicator');

  // show button to auto-page
  let doit;
  if(nextpage.exists()) {
      textareadiv.on('mousedown', function () {
          ++mouseDown;
          clearTimeout(doit);
          doit = setTimeout(function () {
              nextpage.show();
          }, 1000);

      }).on('mouseup', function () {
          clearTimeout(doit);
          nextpage.hide();
          --mouseDown;

      }).on('mouseout', function (e) {
          // must recalculate offset of next-page-indicator each time (since it needs to be visible)
          let offset = nextpage.offset();
          if (e.pageY > offset.top && (e.pageY < offset.top + 25)
              && (e.pageX > offset.left - 10) && (e.pageX < offset.left + 25)
              && mouseDown && nextpage.is(':visible')) {

              $(document).trigger('next-page');
              nextpage.hide();
          }
      });

      $(document).on('mousemove', function (e) {
          let offset = nextpage.offset();
          if (e.pageY > offset.top && (e.pageY < offset.top + 25)
              && (e.pageX > offset.left - 10) && (e.pageX < offset.left + 25)
              && mouseDown && nextpage.is(':visible')) {
              $(document).trigger('next-page');
              nextpage.hide();
          }
      });
  }

  // display excerpt on hover
  textareadiv.find('span.highlight').each(function () {
    $(this).popover(); // opt-in init

    $(this).on('mouseenter', function () {
      if (mouseDown === 0) {
        $(this).popover("show");
      }
    }).on('mouseleave', function () {
      $(this).popover("hide");

    }).on('click', function () {
      let popup = $('#popover-' + $(this).attr('data-excid'));
      popup.popover("toggle");
      // must handle size by hand on demand
      let popover = $(popup).next('.popover');
      // must add manually class to effective popup if linked span has class
      if (popup.hasClass('btn-only')) {
        $(popover).addClass("btn-only");
      }
    });

    // close on click outside
    $('body').on('click', function (e) {
      if (!($(e.target).parent().hasClass('highlight')
          || $(e.target).hasClass('highlight'))
          || $(e.target).parent().hasClass('popover')) {
        $(".popoverbox").each(function () {
          $(this).popover("hide");
        });
      }
    });
  });
}

/**
 * Get a text content from an url and fill in form with response, if any
 *
 * @param url the url to make a request to
 * @param formTitle the begin of form
 * @param waitForIt the wait-for-it panel to display while loading text
 */
async function getTextFromUrl(url, formTitle, waitForIt, isAdmin) {
	if (url.val().trim().length > 0) {
	    if (waitForIt)
            waitForIt.modal('show');

        // for check if an url is PDF, we must check if content-type response header is PDF in a controller (not in JS
        // because of CORS policy

        try {
            new URL(url.val());

            let urlIsPdf = url.val().endsWith('.pdf');

            if (!urlIsPdf) {
                $('#textSourceType').val(0);
                extractWebContent(url, formTitle, waitForIt, isAdmin);
            } else {
                $('#textSourceType').val(1);
                //extractPDFContentAction(url, waitForIt);
            }
        } catch (e) {
            console.log(e);
            if (waitForIt)
                hideAndDestroyModal(waitForIt);
        }
	}
}

/**
 * Extract data from given url pointing to a web page to get the content from (typically a press article)
 *
 * @param url an url
 * @param formTitle the begin of form
 * @param waitForIt the "waiting pane" to hide at the end of the process
 */
function extractWebContent(url, formTitle, waitForIt, isAdmin) {
  getHtmlContent(url.val()).done(function (response) {
      let urlForm = $('#' + formTitle + 'url2');
      let title = $('#' + formTitle + 'title');
      let textarea = $('#' + formTitle + 'textarea');
      let source = $('#' + formTitle + 'sourceTitle');
      let publicationDate = $('#' + formTitle + 'publicationDate');
      let authors = $('#' + formTitle + 'authors');
      let textType = $('#' + formTitle + 'textType');
      let data = JSON.parse(JSON.parse(response));

      setInputValid(url);

      if(urlForm.exists()) {
          urlForm.val(url.val());
          setInputValid(urlForm);
      }

      // fill title and highlight element
      if(data.title != null && data.title.length > 0) {
          title.val(data.title);
          title.typeahead('val', data.title);
          setInputValid(title);
      }

      // fill source title
      if(data.source != null && data.source.length > 0) {
          source.val(data.source);
          setInputValid(source);
      }

      // try to parse date and highlight element
      if (data.date != null && data.date.length > 0) {
          publicationDate.val(extractDateFromString(data.date));
          setInputValid(publicationDate);
      }

      // fill authors
      if (data.author != null && data.author.length > 0) {
        // clear all author values
          authors.find('.btn-remove').each(function () {
          removeGenericEntry(this);
        });

          let input = authors.find('[name="' + formTitle + 'authors[0].fullname"]');
         input.typeahead('val', data.author);
         setInputValid(input);
      }

      // fill text type
      if (data.isArticle != null && data.isArticle === 'true') {
          //textType.val(3);
          //setInputValid(textType);
      }

      // fill content and highlight element
      if(data.source != null) {
          checkFreeSource(data.source).done(function (data2) {

          }).fail(function (error) {
              updateTextContent(false, textarea);
          });
      }else{
          updateTextContent(false, textarea);
      }

      if (waitForIt)
        hideAndDestroyModal(waitForIt);

  }).fail(function (jqXHR) {
      console.log(jqXHR);
      if (waitForIt)
         handleFailedExtract(url, waitForIt, jqXHR.responseText)
  });
}

function extractWebTextContent(url, formTitle) {
    return new Promise(function (resolve, reject) {
        getHtmlContent(url.val()).done(function (response) {
            let data = JSON.parse(JSON.parse(response));
            $('#' + formTitle + 'fakeTextarea').val(data.content);
            resolve();
        }).fail(function (jqXHR) {
            console.log(jqXHR);
            resolve();
        });
    });
}

function updateTextContent(isFree, textarea, content){
    if(isFree){
        textarea.attr("readonly", false).html(makeTextContentReadable(content))
            .parent('.form-group').addClass('has-success').removeClass('has-error');
    }else{
        textarea.attr("readonly", true).html(" ")
            .parent('.form-group').addClass('has-error').removeClass('has-success');
    }

    textarea.toggle(isFree);
    $('#text-import-copyright').toggle(!isFree);
}

/**
 * Extract data from given url pointing to a pdf to get the content from
 *
 * @param url an url
 * @param waitForIt the "waiting pane" to hide at the end of the process
 */
function extractPDFContentAction(url, waitForIt) {
  extractPDFContent(url.val()).done(function (data) {
    $("#language").val(data.language).change().parent('.form-group').addClass('has-success');
    let ch = splitExtractHead(data.text);
    $('#textarea').val(ch).parent('.form-group').addClass('has-success');
    onPreExecuteExtractPDFContentAction(waitForIt);
    showInformativePopup("text.extract.", data.text);

  }).fail(function (jqXHR) {
    onPreExecuteExtractPDFContentAction(waitForIt);
    showInformativePopup("text.extract.error.", "");
    //handleFailedExtract(url, waitForIt, jqXHR.responseText);
  });
}

/**
 * Split the head of the extracted text from PDF and ignore it
 *
 * @param text the extracted text
 * @return the reconstructed text
 */
function splitExtractHead(text){
  let resultText = "";
  if(typeof text === 'string' || text instanceof String) {
    let splitted = text.split("\n");
    if (splitted.length >= 3) {
      for (let i = 3; i < splitted.length; i++) {
        resultText += splitted[i] + "\n";
      }
    }
  }
  return resultText;
}

/**
 * Instructions to excecute before the extraction
 *
 * @param waitForIt the "waiting pane" to hide at the end of the process
 */
function onPreExecuteExtractPDFContentAction(waitForIt){
  hideAndDestroyModal(waitForIt);
  showMe('#upload-div', true, true);
}

/**
 * Handle failed retrieval of the content from a given url. Will display content of given response in
 * dedicated message div (present div anchor with id "msg-div" present in main html template)
 *
 * @param urlInput the input of the url (to be highlighted)
 * @param waitForIt the pane to ask user to wait for the end of the process
 * @param response the html code of the response msg to be rendered
 */
function handleFailedExtract(urlInput, waitForIt, response) {
  hideAndDestroyModal(waitForIt);
  $('#msg-div').append(response);
  urlInput.parent('.form-group').addClass('has-warning');
  fadeMessage();
}

/**
 * Handle modal frame where texts with same title are displayed to ask user if his newly introduced
 * text is not already present (mainly used for text that could have been added privately to avoid
 * duplicates)
 *
 * @param isNotSame hidden input element that will hold "true" to tell that this text must be created
 * @param submit the modal submit button
 */
function handleCandidates(isNotSame, submit) {
	let modal = "#text_candidate_modal";
	let okbtn = "#createnew";
	let loadbtn = '#load';

	toggleSummaryBoxes('#candidate-boxes', '.toggleable');
	$(modal).modal('show');

	// handle click on create a "new text" button
	$(modal).on('click', okbtn, function () {
		isNotSame.val('true');
        hideAndDestroyModal(modal);
        submit.off();
		submit.trigger('click');
	});

	// handle click on load, simply reload page with known text
	$(modal).on('click', loadbtn, function () {
		redirectToEdit($('.selected').prop('id').split("_")[1]);
	});
}

//////////////////////////////////////////////////////////////////////////////////////Search Text

function handleSearchText(container){
    container.find('#searchtextbtn').on('click', function(e) {
        e.preventDefault();
        let query = $('#searchtext').find("input[type=text]").val();
        $('#searchtextbtn').blur();
        doSearchText(query);
    });

    container.find('#query_searchtext').keypress(function(e) {
        if (e.which === 13) {
            e.preventDefault();
            let query = $('#searchtext').find("input[type=text]").val();
            doSearchText(query);
        }
    });
}

let searchTextResultManager = null;

/**
 * Initialize the search text manager
 *
 */
function initializeSearchText(textarea, textpager) {
    searchTextResultManager = new SearchTextResultManager(textarea, textpager);
    searchTextResultListener();
}

/**
 * Manage search inside the text
 * @param query a string to search inside the text
 *
 */
function doSearchText(query){
  if(searchTextResultManager instanceof SearchTextResultManager)
    searchTextResultManager.setSearch(query);
}

/**
 * Prepare the search text function, verify the textarea and the query
 *
 * param textarea a JQuery object of the text to browse
 * param query a string to search inside the text
 * @return the number of results
 */
function textSearchHandler(textarea, query){
  let nbResults = 0;
  if(textarea instanceof jQuery && typeof query === 'string'
      && query.length > 0 && query.replace(/\s/g, '').length){
    nbResults = textSearch(textarea, query, nbResults);
  }
  return nbResults;
}

/**
 * Search a string inside a text
 *
 * @param elem the Jquery elem to browse
 * @param query the string to search inside the text
 * @param nbResults the number of results in the text
 * @return the number of results
 */
function textSearch(elem, query, nbResults){
  elem.contents().each(function () {
    let elemChild = $(this);
    if(!isNodeText(elemChild)){
      nbResults = textSearch(elemChild, query, nbResults);
    }
    else{
      nbResults = findMatchAndhighlightSearchResults(elemChild, query, nbResults);
    }
  });
  return nbResults;
}

/**
 * Draw search result after the updating of the textarea
 *
 * @param textarea the textarea to update
 */
function updateTextarea(textarea){
  if(searchTextResultManager instanceof SearchTextResultManager && searchTextResultManager.hasTextarea()){
    searchTextResultManager.carryOutSearch(textarea);
  }
}

function initTextArgumentsStructureHandler(container) {

    container.on('click', '.toggle-argument-structure', function() {

        let container = $(this).parents('.text-arguments-container');
        let isOpen = container.find('.argument').first().find('.contribution-container-content').first().hasClass('show');

        container.find('.contribution-container-content').toggleClass('show', !isOpen);
    });
}

/**
 * Search a string on a part of the text
 *
 * @param textNode the Jquery element pointing on the text to perform the search
 * @param query the string to search inside the text
 * @param nbResults the number of results in the text
 * @return the number of results
 */
function findMatchAndhighlightSearchResults(textNode, query, nbResults){
    let chfinal = "";
    let textString = textNode.text();
    let r = searchString(textString, query);
    while (r > -1) {
        //Separate the string in three part, before the query match, the match and after the match
        let chpre = textString.substring(0, r);
        let chquery = textString.substring(r, r + query.length);
        textString = textString.substring(r + query.length, textString.length);
        chfinal += chpre + "<span id='searchTextResult" + (nbResults++) + "' class='searchTextResult searchTextResultSpan'>" + chquery + "</span>";
        //Search after another match in the text
        r = searchString(textString, query);
    }
    if(chfinal.length > 0){
        //Replace the text content by the new one
        elemTextReplace(textNode, chfinal+textString);
    }
    return nbResults;
}

/**
 * Remove all annotation of search results
 *
 */
function removeSearchResults(){
  let resultNodes = $(".searchTextResultSpan");
  resultNodes.each(function () {
    $(this).contents().unwrap();
  });
}

/**
 * Search a string (query) on another one (textString)
 *
 * @param textString the string to perform the search
 * @param query the string to search inside the textString
 */
function searchString(textString, query){
  let textStringToSearch = textString.toLowerCase();
  return textStringToSearch.search(query);
}

/**
 * Replace a Jquery node text content
 *
 * @param the element which the content must be remplaced
 * @param content the content that remplace the content node
 */
function elemTextReplace(elem, content){
  let textNodePerm = elem.after(content);
  elem.remove();
  return textNodePerm;
}

/**
 * Manage the events linked with the display of the results of a search text
 *
 */
function searchTextResultListener(){
  $(document).on( "keydown",function(e) {
    if (searchTextResultManager instanceof SearchTextResultManager && searchTextResultManager.inSearch){
      switch(e.keyCode) {
        case 13 : //Enter
        case 39 : //Cursor right
          searchTextResultManager.nextResult();
          break;
        case 37 : //Cursor left
          searchTextResultManager.previousResult();
          break;
        case 27 : //Escape
          removeSearchResults();
          break;
        default:
          // ignore
      }
    }
  });
  /*$(window).on('hashchange', function(e){
    if (searchTextResultManager instanceof SearchTextResultManager && searchTextResultManager.inSearch) {
      let result = getUrlParam("searchTextResult");
      console.log(result);
      //searchTextResultManager.changeResult(anchor.substr("searchTextResult".length, anchor.length));
    }
  });*/
}

/**
 * Determine if a DOM node is a text (return true if it is)
 *
 * @param node the node to determine
 */
function isNodeText(node){
  //3 is for a jQuery text node type
  return node.get(0).nodeType === 3;

}

function unlockSearch(){
  if (searchTextResultManager instanceof SearchTextResultManager && searchTextResultManager.inSearch){
    searchTextResultManager.unlock();
  }
}

/**
 * Class that manage the search inside a text
 *
 */
class SearchTextResultManager {
  constructor(textarea, textpager) {
    this.setTextarea(textarea);
    this.textpager = textpager;
    this.locked = false;
    this.resultId = "#searchTextResult";
    this.inSearch = false;
    this.initialize();
  }

  hasTextarea(){
    return this.textarea instanceof jQuery;
  }

  getAnchorLink(){
    if(this.hasTextarea()){
      if(this.textpager) {
        return "#"+this.textarea.attr("id");
      }
      else{
        return this.getCurrentSearchResultNodeName();
      }
    }
  }

  initialize(query){
    query = query || "";
    if(this.verifyQuery(query)){
      this.query = query.toLowerCase();
    }else{
      this.query = "";
    }
    this.nbResults = 0;
    this.currentResult = -1;
    this.resetTextarea();
  }

  verifyQuery(query){
    return typeof query === 'string' && query.length > 0;
  }

  resetTextarea(){
    if(this.hasTextarea() && this.inSearch){
      removeSearchResults();
    }
  }

  hasTextPager(){
    return this.textpager;
  }

  getCurrentSearchResultNodeName(){
    return this.resultId + this.currentResult;
  }

  getFirstSearchResultNode(){
    if(this.inSearch) {
      let node = $(this.resultId + 0);
      if(node !== undefined)
        return node;
    }
    return false;
  }

  getCurrentSearchResultNode(){
    if(this.inSearch && this.currentResult >= 0) {
      return $(this.getCurrentSearchResultNodeName());
    }
    return false;
  }

  setTextarea(textarea){
    this.textarea = textarea;
    if(textarea instanceof jQuery){
      if(textarea.attr("id") === undefined){
        textarea.attr("id", "searchTextArea");
      }
      this.textareaClone = textarea.children();
    }
  }

  setSearch(query){
    if(this.hasTextarea()) {
      this.initialize(query);
      return this.carryOutSearch();
    }
    return false;
  }

  carryOutSearch(textarea){
    textarea = textarea || this.textarea;
    if(this.hasTextarea() && this.verifyQuery(this.query)) {
      let search = textSearchHandler(textarea, this.query);
      if (!search || search <= 0) {
        this.inSearch = false;
      }
      else {
        this.inSearch = true;
        this.nbResults = search;
        this.nextResult();
      }
      return this.inSearch;
    }
    return false;
  }

  previousResult(){
    return this.changeResult(this.currentResult-1);
  }

  nextResult(){
    return this.changeResult(this.currentResult+1);
  }

  changeResult(nbr){
    let nbrInt = parseInt(nbr);
    if(this.verifySearchStatus() && this.isLock() && isInt(nbr) && nbrInt >= 0 && nbrInt < this.nbResults){
      this.locker();
      this.resetCurrentResult();
      this.currentResult = nbrInt;
      this.setCurrentResult();
      this.anchorPage();
      return this.getCurrentSearchResultNodeName();
    }
    return false;
  }

  verifySearchStatus(){
    return this.getFirstSearchResultNode();

  }

  setCurrentResult(){
    let rElem = this.getCurrentSearchResultNode();
    if(rElem) {
      rElem.addClass("searchTextResultPointed");
      rElem.removeClass("searchTextResult");
    }
    return false;
  }

  resetCurrentResult(){
    let rElem = this.getCurrentSearchResultNode();
    if(rElem && rElem.hasClass("searchTextResultPointed")) {
      rElem.removeClass("searchTextResultPointed");
      rElem.addClass("searchTextResult");
    }
    return false;
  }

  anchorPage(){
    let searchNode = this.getCurrentSearchResultNode();
    if (this.hasTextPager() && searchNode) {
      $(document).trigger("textPage", [searchNode.position().left]);
    }

    window.location.href = this.getAnchorLink();
  }

  locker(){
    this.lock();
    setTimeout(unlockSearch, 400);
  }

  lock(){
    this.locked = true;
  }

  unlock(){
    this.locked = false;
  }

  isLock(){
    return !this.locked;
  }
}
