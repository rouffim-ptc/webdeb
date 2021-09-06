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

package be.webdeb.application.rest.object;

import be.webdeb.core.api.actor.BusinessSector;
import be.webdeb.core.api.actor.LegalStatus;
import be.webdeb.core.api.actor.Organization;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * This class is devoted to exchange Organizations in JSON format. All details of an API Organization are represented.
 * Links to other contributions are represented by their ids. Enumerated (predefined) data are passed as typed objects
 * with their ids and i18n descriptions.
 *
 * @author Fabian Gilson
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebOrganization extends WebdebActor {

  /**
   * List of sectors where this organization is active (full object)
   */
  @JsonSerialize
  protected List<BusinessSector> sectors;

  /**
   * Creation date
   */
  @JsonSerialize
  protected String creationDate;

  /**
   * Dissolution date
   */
  @JsonSerialize
  protected String terminationDate;

  /**
   * legal status of this organization (full object)
   */
  @JsonSerialize
  protected LegalStatus legalStatus;

  /**
   * Country where this organization's head office is located (in iso 3166-1 alpha-2)
   */
  @JsonSerialize
  protected String headOffice;

  /**
   * @api {get} WebdebOrganization Organizational actor
   * @apiName Organization
   * @apiGroup Structures
   * @apiDescription An organization is an actor that represents a moral entity, it contains all fields from a WebdebActor.
   * @apiSuccess {Object[]} sectors array of business sectors (optional)
   * @apiSuccess {Integer} sectors.id business sector id (optional)
   * @apiSuccess {String} creationDate the date at which this organization had been created in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)
   * @apiSuccess {String} terminationDate the date at which this organization had been dissolved in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)
   * @apiSuccess {Object} sectors.names of the form { "en" : "englishSector", "fr" : "SecteurFrançais" }
   * @apiSuccess {Object} legalStatus the legal status of this organization
   * @apiSuccess {Integer} legalStatus.id  legal status id
   * @apiSuccess {Object} legalStatus.names of the form { "en" : "englishLegal", "fr" : "StatutFrançais" }
   * @apiSuccess {String} country where the head office is located in iso 3166-1 alpha-2 country code (optional)
   * @apiExample Organizational / institutional actor
   * {
   *   "id": 784,
   *   "type": "actor",
   *   "version": 1464351543000,
   *   "name": [ { "first": null, "last": "Unilever", "pseudo": null, "lang": "fr" }],
   *   "actorType": "organization",
   *   "crossReference": "https://fr.wikipedia.org/wiki/Unilever",
   *   "sectors": [
   *     {
   *       "names": {
   *         "en": "Retail trade",
   *         "fr": "Commerce de détail"
   *       },
   *       "id": 3
   *     },
   *     {
   *       "names": {
   *        "en": "Other services to individuals or organisations",
   *        "fr": "Autres services aux particuliers et organisations"
   *       },
   *       "id": 19
   *     }
   *   ],
   *   "legalStatus": { "id": 0, "names": { "en": "Company", "fr": "Entreprise" } },
   *   "territories": "uk"
   * }
   * @apiVersion 0.0.1
   */
  public WebdebOrganization(Organization organization) {
    super(organization);
    sectors = organization.getBusinessSectors();
    legalStatus = organization.getLegalStatus();
    creationDate = organization.getCreationDate();
    terminationDate = organization.getTerminationDate();
    //headOffice = organization.getHeadOffice() != null ? organization.getHeadOffice().getCode() : null;
  }
}
