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

package be.webdeb.core.api.actor;

import be.webdeb.core.api.contribution.type.PredefinedIntValue;

/**
 * This interface represents an Affiliation type for organizations to be affiliated to either other
 * organizations or persons.
 *
 * Used with EAffiliationType enum, where consistence rules are explained. Those rules are verified by the
 * Affiliation object itself.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see EAffiliationType
 */
public interface AffiliationType extends PredefinedIntValue {

  /**
   * Get the affiliation actor type id value
   *
   * @return a value representating the actor aff type
   */
  int getActorId();

  /**
   * Get the affiliation subtype id value
   *
   * @return a value representating the aff subtype
   */
  int getSubId();

  /**
   * Get the corresponding EAffiliationType value to this.
   *
   * @return the EAffiliationType enum value corresponding to this affiliation type
   */
  EAffiliationType getEType();

  /**
   * Get the corresponding EAffiliationActortype value to this.
   *
   * @return the EAffiliationActortype enum value corresponding to the type of actor cncerned by this type of affiliation
   */
  EAffiliationActorType getEActortype();

  /**
   * Get the corresponding EAffiliationSubtype value to this.
   *
   * @return the EAffiliationSubtype enum value corresponding to the affiliation subtype
   */
  EAffiliationSubtype getESubtype();
}
