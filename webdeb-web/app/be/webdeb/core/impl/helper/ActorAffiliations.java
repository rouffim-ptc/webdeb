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

package be.webdeb.core.impl.helper;

import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.actor.EAffiliationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class holds all needed informations for actor affiliations with other one.
 *
 * @author Martin Rouffiange
 */
public class ActorAffiliations extends ActorHelper implements Comparable<ActorAffiliations> {

    private List<ActorAffiliation> affiliations = new ArrayList<>();
    private String orgType;

    private Date minDate = null;
    private boolean minDateCalculated = false;

    private Date maxDate = null;
    private boolean maxDateCalculated = false;

    private Long distance = null;
    private Boolean graduatingFrom = null;

    public ActorAffiliations(Long id, String name, String avatar, Integer type, String orgType) {
        super(id, name, avatar, type);
        this.orgType = orgType;
    }

    public ActorAffiliations(Long id, String name, String avatar, Integer type, String gender, String orgType) {
        super(id, name, avatar, type, gender);
        this.orgType = orgType;
    }


    public List<ActorAffiliation> getAffiliations() {
        return affiliations;
    }

    public String getOrgType() {
        return orgType;
    }

    public synchronized void addAffiliation(ActorAffiliation affiliation) {
        affiliations.add(affiliation);
    }

    @JsonIgnore
    public Date getMinDate() {
        if(!minDateCalculated) {
            affiliations.stream()
                        .filter(aff -> aff != null && aff.getFromDate() != null)
                        .min(Comparator.comparing(ActorAffiliation::getFromDate))
                        .ifPresent(affiliation -> minDate = affiliation.getFromDate());

            minDateCalculated = true;
        }

        return minDate;
    }

    @JsonIgnore
    public Date getMaxDate() {
        if(!maxDateCalculated) {
            affiliations.stream()
                    .filter(aff -> aff != null && aff.getToDate() != null)
                    .max(Comparator.comparing(ActorAffiliation::getToDate))
                    .ifPresent(affiliation -> maxDate = affiliation.getToDate());

            maxDateCalculated = true;
        }

        return maxDate;
    }

    @JsonIgnore
    public Long getDateDistance() {
        if(distance == null && getMinDate() != null && getMaxDate() != null) {
            distance = getMaxDate().getTime() - getMinDate().getTime();
        }

        if(distance == null)
            distance = 0L;

        return distance;
    }

    @JsonIgnore
    public boolean isGraduatingFrom() {
        if(graduatingFrom == null) {
            if(this.actorType == EActorType.ORGANIZATION.id()) {
                graduatingFrom = false;
            } else {
                graduatingFrom = affiliations.stream()
                        .filter(aff -> aff != null && aff.getAffType() != null && aff.getAffType() == EAffiliationType.GRADUATING_FROM.id())
                        .count() == affiliations.size();
            }
        }

        return graduatingFrom;
    }

    @Override
    public int compareTo(@NotNull ActorAffiliations a) {

        if(this.isGraduatingFrom() && !a.isGraduatingFrom())
            return 1;

        if(!this.isGraduatingFrom() && a.isGraduatingFrom())
            return -1;

        if(this.getMaxDate() == null) {
            if(a.getMaxDate() == null) {
                return this.getName().compareToIgnoreCase(a.getName());
            }
            return 1;
        }

        if(a.getMaxDate() == null) {
            return -1;
        }

        int compare = a.getMaxDate().compareTo(this.getMaxDate());

        if(compare != 0) {
            return compare;
        }

        compare = this.getDateDistance().compareTo(a.getDateDistance());

        if(compare != 0) {
            return compare;
        }

        return this.getName().compareToIgnoreCase(a.getName());
    }

    public ActorAffiliations clone() {
        return new ActorAffiliations(id, name, avatar, actorType, orgType);
    }

}
