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

function initAffiliationListeners(container, actorId, actorType, forActorType){
    // handle add affiliation buttons
    let modalanchor = $("#modal-anchor");

    container.off('click', '.add-affiliation');
    container.on('click', '.add-affiliation', function() {
        addAffiliationListener(modalanchor, actorId, true, forActorType)
    });

    container.off('click', '.add-member');
    container.on('click', '.add-member', function() {
        addAffiliationListener(modalanchor, actorId, false, forActorType)
    });

    container.off('click', '.toggle-history');
    container.on('click', '.toggle-history', function() {
        $(".organizational-chart").toggle();
        $(".organizational-list").toggle();
        $(this).find(".btn-message").toggle();
    });
}

function addAffiliationListener(modal, actorId, isAffiliated, forActorType) {
    newAffiliations(actorId, isAffiliated, forActorType === 0).done(function (data) {
        // load modal content
        modal.empty().append(data);
        modal.children('.modal').modal('show');
    }).fail(function (jqXHR) {
        stopWaitingModal();
        if (jqXHR.status === 401) {
            redirectToLogin();
        } else {
            slideDefaultErrorMessage();
        }
    });
}

function transformAffiliationToData(affiliations, affiliateds, actorType, fullname){
    let r = [];

    if(affiliateds != null){
        r.push({label: Messages("viz.actor.carto.belongsto.n"), btn: 'add-affiliation', affiliations: JSON.parse(affiliateds)});
        r.push({label: Messages("viz.actor.carto.belongings.n"), btn: 'add-member', affiliations: JSON.parse(affiliations)});
    } else {
        r.push({label: Messages("viz.actor.desc.cartography." + actorType, fullname), btn: 'add-member', affiliations: JSON.parse(affiliations)});
    }

    return r;
}

function transformToAffiliations(list){
    let affiliations = [];

    if(list.exists()) {
        // Fill the affiliation data by retrieving the not filtered actor thumbnail
        list.find('.actor-thumbnail:not(.filtered)').each(function (i, actor) {
            var actor_obj = {};
            var avatar = $(actor).clone().addClass('chart');
            avatar.find(".hidden").remove();
            actor_obj.name = "<div>" + avatar.prop('outerHTML') + "</div>";
            actor_obj.affiliations = [];

            $(actor).find(".actor-affiliation").each(function (i, aff) {
                let a = {};
                let affiliation = $(aff).text().split(";");
                let iAff = 0;
                // graph data values
                a.affType = affiliation[iAff++];
                a.profession = affiliation[iAff++];

                a.affiliatedId = parseInt(affiliation[iAff++]);
                a.affiliatedName = affiliation[iAff++];

                a.dateBegin = stringToDate(affiliation[iAff++]);
                a.dateBeginType = {};
                a.dateBeginType.id = parseInt(affiliation[iAff++]);
                a.dateBeginType.name = affiliation[iAff++];

                a.dateEnd = stringToDate(affiliation[iAff++]);
                a.dateEndType = {};
                a.dateEndType.id = parseInt(affiliation[iAff++]);
                a.dateEndType.name = affiliation[iAff++];

                a.filtered = $(aff).hasClass("filtered");

                actor_obj.affiliations.push(a);
            });

            affiliations.push(actor_obj);
        });
    }

    return affiliations;
}

function stringToDate(date) {
    if (date) {
        date = String(date).trim();
        let parts = date.split("/");

        switch (parts.length) {
            case 1 :
                switch (date) {
                    case "null" :
                        return null;
                    default :
                        return new Date(date);
                }
            case 2 :
                return new Date(parts[1], parts[0] - 1);
            case 3 :
                return new Date(parts[2], parts[1] - 1, parts[0]);
        }
    }

    return null;
}