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

import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents a contribution in the webdeb system. It holds only the common properties of all
 * types of contributions
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface Contribution {

  /**
   * Retrieve the contribution unique id
   *
   * @return the contribution id, or -1 if unset
   */
  Long getId();

  /**
   * Set the contribution id
   *
   * @param id the contribution id to set
   * @see EContributionType
   */
  void setId(Long id);

  /**
   * Retrieve the contribution type as a ContributionType object
   *
   * @return the Contribution type object for this contribution
   */
  ContributionType getContributionType();

  /**
   * Retrieve the contribution type.
   *
   * @return the enum representation of the contribution type
   *
   * @see EContributionType
   */
  EContributionType getType();

  /**
   * Get the title of this contribution
   *
   * @param lang the user lang
   * @return the title of this contribution
   */
  String getContributionTitle(String lang);

  /**
   * Save this Contribution on behalf of a given Contributor. If this contribution has an id (this.getId() != -1)
   * this id is considered as valid and this contribution is updated, otherwise a new contribution is
   * persisted. If any non existing Contribution (Actor or Tag) is linked to this contribution
   * (in case of affiliations, authors/actors or linked tags, again checked based on the id value),
   * it is created and returned.
   *
   * A save action is always preceded by a call to isContributorValid() method. Any error will be wrapped as the
   * message of the exception thrown. As a side effect, the id of this Contribution is updated, if needed.
   *
   * @param contributor the Contributor id that initiated the save action
   * @param currentGroup the group id under which the save action is performed, will be used for auto-created actors.
   * @return a map of contribution type and possibly empty list of Contributions (actors or tags) that have been persisted
   * as a side effect, i.e all previously unknown contributions bound to this Contribution that have been persisted too (may be empty)
   *
   * @throws FormatException if any contribution field is not valid or missing
   * @throws PermissionException if the save action(s) could not been performed because given contributor is not allowed
   * to publish in given group, or (s)he's trying to update an existing contribution (s)he's not allowed to
   * @throws PersistenceException if the save action(s) could not been performed because of an issue with
   * the persistence layer
   */
  Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException;

  /**
   * Remove this contribution from the database
   *
   * @param contributor the Contributor id that initiated the delete action
   * @throws PermissionException if given contributor is not allowed to delete this contribution
   * @throws PersistenceException if this.getId() does not exist in repository of does not correspond
   * to this.getType()
   */
  void remove(Long contributor) throws PermissionException, PersistenceException;

  /**
   * Remove this contribution from the database
   *
   * @param contributor the Contributor id that initiated the delete action
   * @param force true if the deletion must be total
   * @throws PermissionException if given contributor is not allowed to delete this contribution
   * @throws PersistenceException if this.getId() does not exist in repository of does not correspond
   * to this.getType()
   */
  void remove(Long contributor, boolean force) throws PermissionException, PersistenceException;

  /**
   * Get the Contributor that created this Contribution
   *
   * @return the creator of this Contribution
   */
  Contributor getCreator();

  /**
   * Get the Contributor whoever made a change last
   *
   * @return the last contributor of this Contribution
   */
  ContributionHistory getLatestContributor();

  /**
   * Get all contributors that created or updated this contribution
   *
   * @return the list of contributors of this Contribution
   */
  List<Contributor> getContributors();

  /**
   * Check whether this contribution has been locked by admin
   *
   * @return true if this contribution has been locked
   */
  boolean isDeleted();

  /**
   * Set whether this contribution has been deleted or not
   *
   * @param deleted true if this contribution has been deleted
   */
  void setDeleted(boolean deleted);

  /**
   * Check whether this contribution has been locked by admin
   *
   * @return true if this contribution has been locked
   */
  boolean isLocked();

  /**
   * Set whether this contribution has been locked by admins
   *
   * @param locked true if this contribution has been locked
   */
  void setLocked(boolean locked);

  /**
   * Check if this contribution contains all needed values to be considered as valid. Semantic validation is
   * dependant on the subtype semantics.
   *
   * @return a List of field names in error if any, an empty list if this Contribution may be considered as
   * valid
   */
  List<String> isValid();

  /**
   * Get groups in which this contribution has been published. First group in list is assumed to be the current group
   * in which this contribution is manipulated, ie, in case of a save action is performed, all auto-created actors will be
   * saved in first retrieved group.
   *
   * @return a list of groups
   */
  List<Group> getInGroups();

  /**
   * Add given group id to the list of groups where this contribution is visible. Has no effect if group is unknown.
   * This action requires an extra save call to be persisted into the database.
   *
   * @param group a group id
   * @return true if given group has been added (or was already assigned) to this contribution, false if given group does not exist.
   */
  boolean addInGroup(int group);

  /**
   * Add given group id to the list of groups where this contribution is visible. Has no effect if group is unknown.
   * This action requires not an extra save call to be persisted into the database.
   *
   * @param group a group id
   * @return true if given group has been added (or was already assigned) to this contribution, false if given group does not exist.
   * @throws PersistenceException if this.getId() does not exist in repository of does not correspond
   */
  boolean addInGroupAndUpdate(int group) throws PersistenceException;

  /**
   * Check if this contribution is member of the given group id
   *
   * @param group a group id
   * @return true if given group contains this contribution
   */
  boolean isMemberOfGroup(int group);

  /**
   * Check if this contribution is member of a group with public visibility
   *
   * @return true if a public group contains this contribution
   */
  boolean isMemberOfAPublicGroup();

  /**
   * Remove given group id to the list of groups where this contribution is visible. Has no effect if group is unknown.
   *
   * @param group a group id
   * @return true if given group has been removed (or was not assigned) to this contribution, false if given group does not exist.
   */
  boolean removeFromGroup(int group);

  /**
   * Remove given group id to the list of groups where this contribution is visible. Has no effect if group is unknown.
   * Unlike addInGroup, this method persists the removal into the database.
   *
   * @param group a group id
   * @param contributor contributor id of contributor asking the removal
   * @return true if given group has been removed (or was not assigned) to this contribution, false if given group does not exist.
   * @throws PermissionException if given contributor is not allowed to delete this contribution from given group
   * @throws PersistenceException if this group could not be removed because of an issue with the persistence layer
   */
  boolean removeFromGroup(int group, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Set the groups where this contribution has been published
   *
   * @param groups a list of groups
   */
  void setInGroups(List<Group> groups);

  /**
   * Check whether this contribution has been validated by a contributor
   *
   * @return the validation state of this contribution
   */
  ValidationState getValidated();

  /**
   * Set the state saying if this contribution has been validated by a contributor
   *
   * @param validated a validation state for this contribution
   */
  void setValidated(ValidationState validated);

  /**
   * Get the version of this contribution instance (optimistic locking)
   *
   * @return a timestamp representing the version of this particular instance
   */
  long getVersion();

  /**
   * Get the version of this contribution instance (optimistic locking) as string
   *
   * @return the version date as string
   */
  String getVersionAsString();

  /**
   * Set a timestamp as the version number of this object (used for optimistic concurrent-locking)
   *
   * @param version a timestamp (as Date.getTime())
   */
  void setVersion(long version);

  /**
   * Get the map of number of linked elements with this contribution
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the id of the group where stats must be counted
   * @return the map of number of linked elements with this contribution by contribution type
   */
  Map<EContributionType, Integer> getCountRelationsMap(Long contributorId, int groupId);


  /**
   * Get the tags set linked to this Contribution, if any.
   *
   * @return the set of tags, possibly empty
   */
  Set<Tag> getTags();

  /**
   * Get the tags linked to this Contribution, if any.
   *
   * @return the list of tags, possibly empty
   */
  List<Tag> getTagsAsList();

  /**
   * Get the tags linked to this Contribution, if any as a string comma-separated list.
   *
   * @return the string list, possibly empty
   */
  String getTagsAsStringList();

  /**
   * Set the tags linked to this Contribution
   *
   * @param tags a list of tags
   * @throws FormatException if given tags are incomplete
   */
  void setTags(List<Tag> tags) throws FormatException;

  /**
   * Add a given tag to list of tags, if it already existed, tags are unchanged.
   *
   * @param tag a tag to add to current tags linked to this contribution
   * @throws FormatException if given tagobject is incomplete or invalid
   */
  void addTag(Tag tag) throws FormatException;

  /**
   * Remove given tag linked to this contribution, tags are unchanged if given tag does not exist
   *
   * @param tag a tag
   */
  void removeTag(Long tag);

  /**
   * Init tags set
   *
   */
  void initTags();

  /**
   * Get the list of places concerned by this context
   *
   * @return a possibly empty list of geographical places
   */
  List<Place> getPlaces();

  /**
   * Set the list of places concerned by this context
   *
   * @param places a list of geographical places
   */
  void setPlaces(List<Place> places);

  /**
   * Add a place linked with this contextualized argument
   *
   * @param place a place to add
   */
  void addPlace(Place place);

  /**
   * Init places list
   *
   */
  void initPlaces();

  /**
   * Get the absolute sort key for that contribution (null for argument links) containing either the
   * actor's name, the argument's standard form or the text title
   *
   * @return the sortkey of this contribution
   */
  String getSortkey();

  /**
   * Set the absolute sort key for that contribution (null for argument links) containing either the
   * actor's name, the argument's standard form or the text title
   *
   * @param sortkey the sortkey of this contribution
   */
  void setSortkey(String sortkey);

  /**
   * Get the number of contributions linked to this one
   *
   * @return the number of linked contributions
   */
  Integer getNbContributions();

  /**
   * Set the number of contributions linked to this one
   *
   * @param nbContributions the number of linked contributions
   */
  void setNbContributions(Integer nbContributions);

}
