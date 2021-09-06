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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements an Organizational Actor in the webdeb system.
 *
 * @author Fabian Gilson
 */
class ConcreteOrganization extends ConcreteActor implements Organization {

  private String officialNumber;
  private List<ActorName> oldNames = new ArrayList<>();
  private List<BusinessSector> sectors = new ArrayList<>();
  private LegalStatus legalStatus;
  private String creationDate;
  private String terminationDate;

  ConcreteOrganization(ActorFactory factory, ActorAccessor accessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    actortype = EActorType.ORGANIZATION;
  }

  @Override
  public String getOfficialNumber() {
    return officialNumber;
  }

  @Override
  public void setOfficialNumber(String officialNumber) {
    this.officialNumber = officialNumber;
  }

  @Override
  public List<ActorName> getOldNames() {
    return oldNames != null ? oldNames : new ArrayList<>();
  }

  @Override
  public void setOldNames(List<ActorName> names) {
    this.oldNames = names;
  }

  @Override
  public void addOldName(ActorName name) {
    updateNameList(oldNames, name);
  }

  @Override
  public List<BusinessSector> getBusinessSectors() {
    return sectors;
  }

  @Override
  public void setBusinessSectors(List<BusinessSector> sectors) {
    if (sectors != null) {
      this.sectors = sectors;
    }
  }

  @Override
  public void addBusinessSector(BusinessSector sector) {
    if (sector != null && sectors.stream().noneMatch(s -> s.getType() == sector.getType())) {
      sectors.add(sector);
    }
  }

  @Override
  public void removeBusinessSector(BusinessSector sector) {
    if (sector != null) {
      sectors.remove(sector);
    }
  }

  @Override
  public int getActorTypeId() {
    if(legalStatus != null && legalStatus.getType() == ELegalStatus.PROJECT.id()){
      return EActorType.PROJECT.id();
    }
    return actortype.id();
  }

  @Override
  public LegalStatus getLegalStatus() {
    return legalStatus;
  }

  @Override
  public void setLegalStatus(LegalStatus status) {
    legalStatus = status;
  }

  @Override
  public String getCreationDate() {
    return creationDate;
  }

  @Override
  public void setCreationDate(String creationDate) throws FormatException {
    this.creationDate = factory.getValuesHelper().checkAndHarmonizeDate(creationDate);
  }

  @Override
  public String getTerminationDate() {
    return terminationDate;
  }

  @Override
  public void setTerminationDate(String terminationDate) throws FormatException {
    this.terminationDate = factory.getValuesHelper().checkAndHarmonizeDate(terminationDate);
  }
}
