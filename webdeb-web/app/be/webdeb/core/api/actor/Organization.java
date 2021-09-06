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

package be.webdeb.core.api.actor;

import be.webdeb.core.exception.FormatException;

import java.util.List;

/**
 * This interface represents an Organizational Actor in the webdeb system. An organization may be of different
 * type, like associations, public authorities, etc.
 *
 * @author Fabian Gilson
 */
public interface Organization extends Actor {

  /**
   * This organization's official unique number
   *
   * @return an official reference (number)
   */
  String getOfficialNumber();

  /**
   * Set this organization's official unique number
   *
   * @param officialNumber an official reference (number)
   */
  void setOfficialNumber(String officialNumber);

  /**
   * Get the list of previous names for this actor
   *
   * @return the previous names of this Actor
   */
  List<ActorName> getOldNames();

  /**
   * Set the list of previous names for this Actor (will simply overwrite existing names).
   *
   * @param name a name for this Actor
   */
  void setOldNames(List<ActorName> name);

  /**
   * Add given old name to this actor (replace existing name if a name in the same language exists)
   *
   * @param name a name to add or update
   */
  void addOldName(ActorName name);

  /**
   * Get the business sector of this organization (may be empty)
   *
   * @return the list of business sectors where this organization is active
   */
  List<BusinessSector> getBusinessSectors();

  /**
   * Set the business sectors where this organization is active. If given sectors contain any duplicate, they
   * are ignored.
   *
   * @param sectors a list of BusinessSectors
   */
  void setBusinessSectors(List<BusinessSector> sectors);

  /**
   * Add a business sector to this organization business sectors. If sector previously existed, it is
   * ignored.
   *
   * @param sector a business sector
   */
  void addBusinessSector(BusinessSector sector);

  /**
   * Remove a business sector from this organization sectors. If sector does not exist, this organization is
   * unchanged
   *
   * @param sector a sector to remove
   */
  void removeBusinessSector(BusinessSector sector);

  /**
   * Get the legal status of this organization (may be null)
   *
   * @return the legal status
   */
  LegalStatus getLegalStatus();

  /**
   * Set the legal status of this organization
   *
   * @param status a legal status to assign to this Organization
   */
  void setLegalStatus(LegalStatus status);

  /**
   * Get the date at which this organization has been created (in DD/MM/YYYY format with DD/MM optional)
   *
   * @return the creation date if any, null otherwise
   */
  String getCreationDate();

  /**
   * Set the date at which this organization has been created (in DD/MM/YYYY format with DD/MM optional)
   *
   * @param creationDate the creation date
   * @throws FormatException  if the given date does not have the expected format
   */
  void setCreationDate(String creationDate) throws FormatException;

  /**
   * Get the date at which this organization has been dissolved (in DD/MM/YYYY format with DD/MM optional)
   *
   * @return the creation date if any, null otherwise
   */
  String getTerminationDate();

  /**
   * Set the date at which this organization has been dissolved (in DD/MM/YYYY format with DD/MM optional)
   *
   * @param terminationDate the termination date
   * @throws FormatException  if the given date does not have the expected format
   */
  void setTerminationDate(String terminationDate) throws FormatException;

}
