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

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.actor.Person;
import be.webdeb.core.api.actor.Gender;
import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;


/**
 * This class implements an Actor that is an individual person.
 *
 * @author Fabian Gilson
 */
class ConcretePerson extends ConcreteActor implements Person {

  private Gender gender;
  private String birthdate;
  private String deathdate;
  private Country residence;

  ConcretePerson(ActorFactory factory, ActorAccessor accessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    actortype = EActorType.PERSON;
  }

  @Override
  public Gender getGender() {
    return gender;
  }

  @Override
  public void setGender(Gender gender) {
    this.gender = gender;
  }

  @Override
  public String getBirthdate() {
    return birthdate;
  }

  @Override
  public void setBirthdate(String date) throws FormatException {
    birthdate = factory.getValuesHelper().checkAndHarmonizeDate(date);
  }

  @Override
  public String getDeathdate() {
    return deathdate;
  }

  @Override
  public void setDeathdate(String deathdate) throws FormatException {
    this.deathdate = factory.getValuesHelper().checkAndHarmonizeDate(deathdate);
  }

  @Override
  public Country getResidence() {
    return residence;
  }

  @Override
  public void setResidence(Country residence) {
    this.residence = residence;
  }
}
