/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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
 * This javascript file contains functions that extends jQuery
 *
 * @author Martin Rouffiange
 */

/**
 * Check if a selected dom element exists
 *
 * @return true if it exists
 */
$.fn.exists = function () {
  return this.length !== 0;
};

/**
 * Remove classes than starts with given prefix
 *
 * @param prefix the prefix name of the class to remove
 */
$.fn.removeClassPrefix = function(prefix) {
    svgAnimation = svgAnimation || false;
    this.each(function(i, el) {
      var className = (svgAnimation ? el.className.animVal : el.className);
      var classes = className.split(" ").filter(function(c) {
          return c.lastIndexOf(prefix, 0) !== 0;
      });
      className = $.trim(classes.join(" "));
    });
    return this;
};

/**
 * Get the entire HTML element
 *
 */
$.fn.outerHTML = function() {
    return jQuery('<div />').append(this.eq(0).clone()).html();
};

$(document).ready(function() {
    $.each($('textarea.data-autoresize'), function (key, element) {
        $(element).autoResize();
    });
});

/**
 * Auto resize textarea when the scroll height is bigger than the client height to always see the text content
 *
 */
$.fn.autoResize = function() {
    let resizeTextarea = function (el) {
        el.css('height', el[0].scrollHeight + 'px');
    };

    resizeTextarea(this);

    $(this).on('keyup input', function () {
        resizeTextarea($(this));
    });
};

/**
 * MutationObserver fournit un moyen d‚Äôintercepter les changements dans le DOM.
 * Il a √©t√© con√ßu pour remplacer les Mutation Events d√©finis dans la sp√©cification DOM3 Events.
 *
 * @author https://developer.mozilla.org/fr/docs/Web/API/MutationObserver
 */
$.fn.observer = function(){
    // Options de l'observateur (quelles sont les mutations √† observer)
    let config = { attributes: true, childList: true };

    // Fonction callback √† √©x√©cuter quand une mutation est observ√©e
    let callback = function(mutationsList) {
        for(let mutation of mutationsList) {
            if (mutation.type === 'childList') {
                console.log('Un noeud enfant a √©t√© ajout√© ou supprim√©.');
            }
            else if (mutation.type === 'attributes') {
                console.log("L'attribut '" + mutation.attributeName + "' a √©t√© modifi√©.");
            }
        }
    };

    // Cr√©√© une instance de l'observateur li√© √† la fonction de callback
    let observer = new MutationObserver(callback);
console.log($(this).get(0));
    // Commence √† observer le noeud cible pour les mutations pr√©c√©demment configur√©es
    observer.observe($(this).get(0), config);
};