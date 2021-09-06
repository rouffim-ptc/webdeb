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

import be.webdeb.core.api.contribution.Contribution;

import java.util.List;

/**
 * This interface gathers all type of roles for an holding Actor regarding a Contribution.
 * <p>
 * <pre>
 *   author, i.e., the Actor being the actual reporter of the Contribution
 *   reporter, i.e., the Actor having the ownership of the terms said in the Contribution (may be the same as
 * author)
 * </pre>
 *
 * @author Fabian Gilson
 */
public interface ActorRole {

  /**
   * Get the holding Actor
   *
   * @return an Actor to which this role applies
   */
  Actor getActor();

  /**
   * Get the related contribution to which this role applies for related Actor
   *
   * @return a Contribution to which this role applies to holding Actor.
   */
  Contribution getContribution();

  /**
   * Check if an Actor is flagged as being an author of this Contribution (either text author or
   * argument reporter)
   *
   * @return true if this Actor is an author of this Contribution
   */
  boolean isAuthor();

  /**
   * Set an Actor as being the author/reporter of a Contribution
   *
   * @param isAuthor true if the Actor is the author of the Contribution
   */
  void setIsAuthor(boolean isAuthor);

  /**
   * Check if the Actor is reported as being the thinker in the Contribution (for excerpts)
   *
   * @return true if the Actor is the thinker of the bound Contribution
   */
  // TODO refactoring name
  boolean isReporter();

  /**
   * Set the Actor as being the thinker of a Contribution, i.e., the Actor has the ownership of the terms
   * reported in the Contribution
   *
   * @param isReporter true if the Actor is the thinker of this Contribution
   */
  void setIsReporter(boolean isReporter);


  /**
   * Check if the Actor is cited in the Contribution (for excerpts and arguments)
   *
   * @return true if this actor is simply cited in this contribution
   */
  boolean isJustCited();

  /**
   * Set an Actor as cited in the Contribution
   *
   * @param isAuthor true if the Actor is simply cited in the Contribution
   */
  void setIsJustCited(boolean isCited);

  /**
   * Get the affiliation of the holding Actor that is relevant for the particular related contribution
   *
   * @return the affiliation, if any
   */
  Affiliation getAffiliation();

  /**
   * Set the relevant affiliation of the holding Actor for a particular contribution
   *
   * @param affiliation the affiliation of the bound author
   */
  void setAffiliation(Affiliation affiliation);

  /**
   * Check if this Actor role is valid, all the Actor, contribution and affiliation contains valid values.
   *
   * @return a list of invalid field, or an empty list otherwise.
   */
  List<String> isValid();
}
