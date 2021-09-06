function applyFilterAsync(data, commonDatum){
    // for all summary elements, check if we have to show them or not
    // by applying all filters
    let filterKeys = commonDatum.filterKeys;
    let filterable = data.filterable;
    let onlyfilteron = data.onlyfilteron;

    if(filterable !== undefined) {
        // loop through keys and check if element must be displayed or not
        let remainingKeys = countNbKeysInFilterKeys(filterKeys, onlyfilteron);

        for (let key in filterKeys) {
            if(onlyfilteron === undefined || onlyfilteron === key)
                remainingKeys -= applyFilterKeyAsync(filterKeys, key, filterable);
        }

        applyFilterAsyncResult(data.item, commonDatum.filters, filterable, remainingKeys);
    }
}

function applyFilterAsyncResults(datum, commonDatum, remainingKeys){
    for(let i in datum) {
        applyFilterAsyncResult(datum[i].item, commonDatum.filters, datum[i].filterable, remainingKeys[i]);
    }
}

function applyFilterAsyncResult(item, filters, filterable, remainingKeys){
    // Add one to filter count if a match has been found for all keys
    if (remainingKeys === 0) {
        updateFilterCount(filterable, filters);
    }
    // show only if a match has been found for all keys
    item.toggleClass('filtered', remainingKeys !== 0);
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
                            remainingKeys += (filterKeys[key].length);
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