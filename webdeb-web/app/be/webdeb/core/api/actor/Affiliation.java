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
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;

/**
 * This interface describes an affiliation, i.e. a function/Actor to which the holder of this affiliation is
 * affiliated. Affiliation holders may be Actors or Contributors (no reference to the holder class is present
 * in this class).
 *
 * @author Fabian Gilson
 */
public interface Affiliation extends Contribution, Comparable<Affiliation> {

  /**
   * Get the Actor to which the holder of this Affiliation is affiliated (may be null)
   *
   * @return the bound Actor
   */
  Actor getActor();

  /**
   * Set an Actor as being an affiliation of this holder class.
   *
   * @param actor the Actor to set as an affiliation
   */
  void setActor(Actor actor);

  /**
   * Get the function of this class holder, maybe in its affiliation Actor (may be null)
   *
   * @return the function as a profession object
   */
  Profession getFunction();

  /**
   * Set the function of this class holder, at this Actor if any
   *
   * @param function a function as a profession object
   */
  void setFunction(Profession function);

  /**
   * Get the affiliated actor of this affiliation
   *
   * @return affiliated the affiliated actor
   */
  Actor getAffiliated();

  /**
   * Set the affiliated actor of this affiliation
   *
   * @param affiliated the affiliated actor
   */
  void setAffiliated(Actor affiliated);

  /**
   * Get a concatenation of the function name and the affiliation Actor name
   *
   * @param lang a 2-char ISO code representing the language of the function
   * @param gendered true if the function name must be gendered
   * @param withOrganization true if the organization where the function is done must be draw
   * @return getFunction + " " + getActor().getName()
   */
  String getFullfunction(String lang, boolean gendered, boolean withOrganization);

  /**
   * Get the type of affiliation for organization's behing affiliated to either persons or another organization
   *
   * @return the affiliation type if any, null otherwise
   */
  AffiliationType getAffType();

  /**
   * Get the type of affiliation for organization's behing affiliated to either persons or another organization
   *
   * @return the enum affiliation type if any, null otherwise
   */
  EAffiliationType getAffiliationType();

  /**
   * Set the type of affiliation for organization's being affiliated to either persons or another organization.
   *
   * Note that any affiliation type may not be set for any affiliation actor:.
   *
   * @param type the affiliation type if any
   * @throws FormatException if given affiliation type may not be set to this affiliation or the type is set prior
   * the bound actor.
   * @see EAffiliationType
   */
  void setAffiliationType(EAffiliationType type) throws FormatException;

  /**
   * Get the starting date of this function (may be null). Date must be of the form:
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   * </pre>
   *
   * @return the start date of this function
   */
  String getStartDate();

  /**
   * Set the starting date of this function, at this Actor if any. Date must be of the form
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   * </pre>
   *
   * @param date the start date
   * @throws FormatException if the given date does not have the expected format
   */
  void setStartDate(String date) throws FormatException;

  /**
   * Get the ending date of this function (may be null). Date must be of the form
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   *   -1, meaning ongoing endDate
   * </pre>
   *
   * @return the ending date of this function
   */
  String getEndDate();

  /**
   * Set the ending date of this function at this Actor. Date must be of the form
   * <pre>
   *   yyyy full year
   *   mm/yyyy month/year (with a "/" as separator)
   *   dd/mm/yyyy full date (with a "/" as separator)
   *   -1, meaning ongoing endDate
   * </pre>
   *
   * @param date the ending date
   * @throws FormatException if the given date does not have the expected format or it is preceding the start
   * date
   */
  void setEndDate(String date) throws FormatException;

  /**
   * Get the start date precision type
   *
   * @return the start date precision
   */
  PrecisionDateType getStartType();

  /**
   * Set the start date precision type. Type must be a past type.
   *
   * @param type the start date precision
   *
   * @throws FormatException if given precision date type is not in past
   */
  void setStartDateType(PrecisionDateType type) throws FormatException;

  /**
   * Get the end date precision type
   *
   * @return the end date precision
   */
  PrecisionDateType getEndType();

  /**
   * Set the end date precision type
   *
   * @param type the start date precision
   *
   * @throws FormatException if given precision date type is in past
   */
  void setEndDateType(PrecisionDateType type) throws FormatException;

  /**
   * Save this Affiliation to a given Contributor. If this contribution has an id (this.getId() != -1) this
   * id is considered as valid and this affiliation is updated, otherwise a new one is persisted. If any non
   * existing Actor is linked to this affiliation (again checked based on the id value), it is created and
   * returned. A save action is always preceded by a call to isContributorValid() method. Any error will be wrapped as
   * the message of the exception thrown.
   *
   * @param contributor the Contributor to which this affiliation will be bound
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @return a new Actor wrapped in a map (key is contribution type) if the affiliation Actor (as Contribution)
   * has been persisted as a side effect.
   *
   * @throws FormatException if any contribution field is not valid or missing
   * @throws PermissionException if the save action(s) could not been performed because given contributor is not allowed
   * to publish in given group, or (s)he's trying to update an existing contribution (s)he's not allowed to
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with the persistence layer
   */
  Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException;

  /**
   * Save this Affiliation on behalf of a given Contributor for a given Actor. If this contribution has an id
   * (this.getId() != -1) this id is considered as valid and this affiliation is updated, otherwise a new one
   * is persisted. If any non existing Actor is linked to this affiliation (again checked based on the id
   * value), it is created and returned. A save action is always preceded by a call to isContributorValid() method. Any
   * error will be wrapped as the message of the exception thrown.
   *
   * @param actor an existing Actor id
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @param contributor the contributor that registered this contribution
   * @return a new Actor wrapped in a map (key is contribution type) if the affiliation Actor (as Contribution)
   * has been persisted as a side effect.
   *
   * @throws FormatException if any contribution field is not valid or missing
   * @throws PermissionException if the save action(s) could not been performed because given contributor is not allowed
   * to publish in given group, or (s)he's trying to update an existing contribution (s)he's not allowed to
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with the
   * persistence layer
   */
  Map<Integer, List<Contribution>> save(Long actor, int currentGroup, Long contributor) throws FormatException, PermissionException, PersistenceException;
}
