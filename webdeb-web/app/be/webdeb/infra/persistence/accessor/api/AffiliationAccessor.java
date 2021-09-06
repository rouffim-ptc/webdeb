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

package be.webdeb.infra.persistence.accessor.api;

import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;

/**
 * Simple accessor for affiliations of actors only. Ultimately, affiliations should be subtypes of
 * contributions and all type of affiliations (for contributor too) should be gathered in the same table.
 * (would need a large data-model migration, quite though task).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface AffiliationAccessor extends ContributionAccessor {

  /**
   * Retrieve an affiliation by its id
   *
   * @param id an affiliation id
   * @param hit true if this retrieval must be counted as a visualization
   * @return an affiliation object corresponding to given id, null if not found
   */
  Affiliation retrieve(Long id, boolean hit);

  /**
   * Save an affiliation for a given contributor. If affiliation.getId has been set, update the
   * affiliation, otherwise create it.
   *
   * @param affiliation an affiliation to save
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   * @return a possibly empty map of Contribution type and Contribution (Actors or Folders) created automatically with this
   * save action (new contributions)
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing affiliation (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(Affiliation affiliation, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save an affiliation on behalf of a given contributor for a given Actor.If affiliation.getId
   * has been set, update the affiliation, otherwise create it.
   *
   * @param affiliation an affiliation to save
   * @param actor the Actor to which this affiliation will be bound
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor id that asked to save the contribution
   * @return a possibly empty map of Contribution type and Contribution (Actors or Folders) created automatically with this
   * save action (new contributions)
   *
   * @throws PermissionException if given contributor may not publish in current group or given contribution may not
   * be published in current group, or given contribution does not belong to current group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing affiliation (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(Affiliation affiliation, Long actor, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Remove a given affiliation from the repository
   *
   * @param contribution an affiliation id
   * @param contributor a contributor id
   * @param type the type of the contribution (used to crosscheck id and type)
   * @param isActor boolean saying if this affiliation belongs to an Actor (true) or contributor (false)
   *
   * @throws PermissionException if given contributor may not remove given affiliation
   * @throws ObjectNotFoundException if any of given object was not retrieved from database or the contribution
   * retrieved from database has not the right type
   */
  void remove(Long contribution, Long contributor, EContributionType type, boolean isActor) throws PermissionException, PersistenceException;

  /**
   * Save a profession
   *
   * @param profession a profession object
   * @return the profession id corresponding to given profession, -1 if any error occured
   */
  int saveProfession(be.webdeb.core.api.actor.Profession profession);

}
