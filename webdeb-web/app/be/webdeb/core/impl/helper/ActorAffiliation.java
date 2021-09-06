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

import be.webdeb.core.api.actor.EAffiliationType;
import be.webdeb.core.api.actor.EPrecisionDate;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import play.api.Play;

import javax.inject.Inject;
import java.util.Date;

/**
 * This class holds all needed informations for an actor affiliation with other one.
 *
 * @author Martin Rouffiange
 */
public class ActorAffiliation implements Comparable<ActorAffiliation> {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    private Long id;
    private String lang;

    private String from;
    private Date fromDate;
    private Integer fromTypeId;
    private String fromTypeName;

    private String to;
    private Integer toTypeId;
    private Date toDate;
    private String toTypeName;

    private String function;
    private String neutralFunction;
    private String genericFunction;

    private Integer affType;
    private String affTypeName;
    private String affTypeNameOrSubstitute;


    public ActorAffiliation(Long id, String from, Integer fromTypeId, String fromTypeName, String to, Integer toTypeId, String toTypeName, String lang) {
        this.id = id;

        this.from = values.fromDBFormat(from);
        this.fromTypeId = fromTypeId;
        this.fromTypeName = fromTypeName;

        this.to = values.fromDBFormat(to);
        this.toTypeId = toTypeId;
        this.toTypeName = toTypeName;

        this.fromDate = values.toDate(this.from);
        this.toDate = EPrecisionDate.ONGOING.id().equals(this.toTypeId) ?
                values.getDateOfDay() : values.toDate(this.to);

        this.to = EPrecisionDate.ONGOING.id().equals(this.toTypeId) ?
                values.dateToString(this.toDate) : this.to;

        this.lang = lang;
    }

    @Override
    public int compareTo(@NotNull ActorAffiliation a) {
        if(this.getFromDate() == null) {
            if(a.getFromDate() == null) {
                return 0;
            }
            return 1;
        }

        if(a.getFromDate() == null) {
            return -1;
        }

        return this.getFromDate().compareTo(a.getFromDate());
    }

    public boolean isEmpty() {
        return values.isBlank(from) && values.isBlank(to) &&
                values.isBlank(fromTypeName) && values.isBlank(toTypeName) &&
                values.isBlank(function);
    }

    public Long getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    @JsonIgnore
    public Date getFromDate() {
        return fromDate;
    }

    public Integer getFromTypeId() {
        return fromTypeId;
    }

    public String getFromTypeName() {
        return fromTypeName;
    }

    public String getTo() {
        return to;
    }

    public Integer getToTypeId() {
        return toTypeId;
    }

    @JsonIgnore
    public Date getToDate() {
        return toDate;
    }

    public String getToTypeName() {
        return toTypeName;
    }

    public String getFunction() {
        return function;
    }

    public String getNeutralFunction() {
        return values.isBlank(neutralFunction) ? function : neutralFunction;
    }

    public String getGenericFunction() {
        return values.isBlank(genericFunction) ? getNeutralFunction() : genericFunction;
    }

    public Integer getAffType() {
        return affType;
    }

    public String getAffTypeName() {
        return affTypeName;
    }

    public String getAffTypeNameOrSubstitute() {
        return values.isBlank(affTypeNameOrSubstitute) ? affTypeName : affTypeNameOrSubstitute;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public void setNeutralFunction(String neutralFunction) {
        this.neutralFunction = neutralFunction;
    }

    public void setGenericFunction(String genericFunction) {
        this.genericFunction = values.isBlank(genericFunction) ? neutralFunction : genericFunction;
    }

    public void setAffType(Integer affType) {
        this.affType = affType;
        if(affType != null)
            this.affTypeNameOrSubstitute = EAffiliationType.value(affType).getSubstitudeName(lang);
    }

    public void setAffTypeName(String affTypeName) {
        this.affTypeName = affTypeName;
    }

}
