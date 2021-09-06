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
 */

package be.webdeb.infra.csv;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import com.opencsv.bean.CsvBindByName;
import play.i18n.Lang;

import javax.inject.Inject;

/**
 * This class serves as a mapper from a line in affiliation csv file into a plain java bean.
 * Fields are using only "object" types to allow empty value to be read checked for null values
 *
 * @author Fabian Gilson
 */
public class CsvAffiliation {

  @Inject
  private ActorFactory factory = play.api.Play.current().injector().instanceOf(ActorFactory.class);

  @CsvBindByName
  Integer order;

  @CsvBindByName
  Long webdebId;

  @CsvBindByName
  Integer affType;

  @CsvBindByName
  String function;

  @CsvBindByName
  Integer superFunction;

  @CsvBindByName
  Integer affOrder;

  @CsvBindByName
  Long affWebdebId;

  @CsvBindByName
  String startDate;

  @CsvBindByName
  String endDate;

  @CsvBindByName
  Integer startDateType;

  @CsvBindByName
  Integer endDateType;

  /**
   * Map this csv affiliation into an API affiliation. If this bean does not contain both a webdebId and an
   * affWebdebID, corresponding actor id must be passed to ensure this csv affiliation bean can be mapped to a valid
   * affiliation.
   *
   * @param affiliatedId if this bean does not contain a webdebId, an actor id be the affiliated actor must be passed
   * @param affiliationId if this bean does not contain an affWebdebId, an actor id being the affiliation must be passed
   * @return the mapped API affiliation containing a reference to the affiliated API actor (affiliation actor has
   * also been checked for its existence)
   *
   * @throws FormatException if any field in this csv bean has an invalid/inconsistent value
   * @throws ObjectNotFoundException if either the webdebID/affiliatedId or affWebdebId/affiliationId could not be found
   */
  Affiliation toAffiliation(Long affiliatedId, Long affiliationId) throws FormatException, ObjectNotFoundException {
    Affiliation affiliation = factory.getAffiliation();
    Actor affiliatedActor = getActor(affiliationId);
    affiliation.setActor(affiliatedActor);

    // ensure affiliated actor exists
    getActor(webdebId != null ? webdebId : affiliatedId);

    if (affType != null) {
      EAffiliationType type = EAffiliationType.value(affType);
      if (type == null) {
        throw new FormatException(FormatException.Key.AFFILIATION_TYPE_ERROR, "unknown affiliation type given " + affType);
      }
      play.Logger.underlying().debug(type.name());
      affiliation.setAffiliationType(type);
    }

    if (function != null && !"".equals(function)) {
      Profession profession = factory.findProfession(function, true);
      if (profession == null) {
        profession = factory.createProfession(-1, "fr", EProfessionGender.NEUTRAL.id().toString(), function);
        if(affType != null && EAffiliationType.value(affType) == EAffiliationType.GRADUATING_FROM)
          profession.setType(EProfessionType.FORMATION);
        if(superFunction != null) {
            Profession superLink = factory.getProfession(superFunction);
            if(superLink == null){
                throw new FormatException(FormatException.Key.AFFILIATION_ERROR, "unknown super function " + superLink);
            }
            profession.setSuperLink(superLink);
        }
        profession = factory.saveProfession(profession) == -1 ? null : profession;
      }
      affiliation.setFunction(profession);
    }

    affiliation.setStartDate(startDate);
    affiliation.setEndDate(endDate);

    if(startDateType != null){
      affiliation.setStartDateType(factory.getPrecisionDateType(startDateType));
    }

    if(endDateType != null){
      affiliation.setEndDateType(factory.getPrecisionDateType(endDateType));
    }

    return affiliation;
  }

  /**
   * Retrieve an existing actor from given id
   *
   * @param id an actor id
   * @return the actor corresponding to given id
   * @throws ObjectNotFoundException if no actor with given id could be found in database
   */
  private Actor getActor(Long id) throws ObjectNotFoundException {
    Actor actor = factory.retrieve(id);
    if (actor == null) {
      throw new ObjectNotFoundException(Actor.class, id);
    }
    return actor;
  }


  @Override
  public String toString() {
    return "CsvAffiliation{" +
        "order=" + order +
        ", webdebId=" + webdebId +
        ", affType=" + affType +
        ", function='" + function + '\'' +
        ", affOrder=" + affOrder +
        ", affWebdebId=" + affWebdebId +
        ", startDate='" + startDate + '\'' +
        ", endDate='" + endDate + '\'' +
        ", startDateType='" + startDateType + '\'' +
        ", endDate='" + endDateType + '\'' +
        '}';
  }
}
