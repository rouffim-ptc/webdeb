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
 * This file handle filter-related functions that must works in a Web Worker
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */

onmessage = function(e) {
    applyFilterAsync(JSON.parse(e.data));
    postMessage(true);
};

function applyFilterAsync(data){
    // for all summary elements, check if we have to show them or not
    // by applying all filters
    let remainingKeys = [];
    let filterKeys = data.commonDatum.filterKeys;

    for(let i in data.datum) {
        let filterable = data.datum[i].filterable;
        let onlyfilteron = data.datum[i].onlyfilteron;

        if (filterable !== undefined) {
            // loop through keys and check if element must be displayed or not
            let rKeys = countNbKeysInFilterKeys(filterKeys, onlyfilteron);

            for (let key in filterKeys) {
                if(onlyfilteron === undefined || onlyfilteron === key)
                    rKeys -= applyFilterKeyAsync(filterKeys, key, filterable);
            }

            remainingKeys.push(rKeys);
        }
    }

    postMessage(remainingKeys);
}

function applyFilterKeyAsync(filterKeys, key, filterable){
    let prefix, i;
    let remainingKeys = 0;

    if (filterKeys.hasOwnProperty(key)) {
        // for topics, all selected values must match
        switch (key) {
            case 'publidate0':
            case 'birthdate0':
                prefix = key.substring(0, key.length - 1);
                if (prefix in filterable && parseInt(filterable[prefix]) >= parseInt(filterKeys[key][0])) {
                    remainingKeys++;
                }
                break;

            case 'publidate1':
            case 'birthdate1':
                prefix = key.substring(0, key.length - 1);
                if (prefix in filterable && parseInt(filterable[prefix]) <= parseInt(filterKeys[key][0])) {
                    remainingKeys++;
                }
                break;

            default:
                // only one match is sufficient
                for (i in filterKeys[key]) {
                    if (key in filterable && filterable[key].includes(filterKeys[key][i])) {
                        if (key === "place") {
                            remainingKeys -= (filterKeys[key].length);
                            break;
                        }
                        remainingKeys++;
                    }
                }

        }
    }

    return remainingKeys;
}

function countNbKeysInFilterKeys(filterKeys, onlyfilteron){
    let count = 0;
    for (let key in filterKeys) {
        if(onlyfilteron === undefined || onlyfilteron === key)
            count += filterKeys[key].length;
    }
    return count;
}

