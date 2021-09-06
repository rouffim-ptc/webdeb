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

package be.webdeb.core.api.contribution;

import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Set;

/**
 * This interface gathers common properties for all textual contributions, i.e texts and arguments.
 *
 * @author Fabian Gilson
 */
public interface TextualContribution extends Contribution {

  /**
   * Add an Actor with a given role to this contribution. This action won't be persisted.
   *
   * @param role the role of a bound Actor in this contribution
   */
  void addActor(ActorRole role) throws FormatException;

  /**
   * Bind a given Actor to this Contribution with given Contributor as author of this binding
   *
   * @param role the role of a bound Actor in this contribution
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor a Contributor id that initiated the save action
   * @return a list of a unique Actor (as Contribution) if the given Actor did not already exist in database
   * @throws PermissionException if given contributor may not remove given actor from given contribution in given group
   * @throws PersistenceException if the action could not been performed
   */
  List<Contribution> bindActor(ActorRole role, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Unbind the given Actor to this Contribution
   *
   * @param actor an Actor id
   * @param contributor a Contributor id that initiated the save action
   * @throws PersistenceException if the unbinding could not been performed
   */
  // TODO add the role to unbind (author, reporter, cited)
  void unbindActor(Long actor, Long contributor) throws PersistenceException;

  /**
   * Init the actors list
   *
   */
  void initActors();

  /**
   * Get the list of bound actors to this Contribution
   *
   * @return a list of Actor roles of this Contribution
   */
  List<ActorRole> getActors();

  /**
   * Get the list of bound actors to this Contribution by actor role of this contribution.
   *
   * @param limit the limit of results
   * @param role the actor role for this contribution
   * @return a list of Actor roles of this Contribution
   */
  List<ActorRole> getActors(int limit, EActorRole role);

  /**
   * Get the number of actors by role of this contribution
   *
   * @return the number of actors for given role
   */
  int getNbActors(EActorRole role);

}
