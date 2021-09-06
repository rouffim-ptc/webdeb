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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.*;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents an accessor for tags persisted into the database
 *
 * @author Martin Rouffiange
 */
public interface TagAccessor extends ContributionAccessor {

  /**
   * Retrieve a Tag by its id
   *
   * @param id a tag id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the tag concrete object corresponding to the given id, null if not found
   */
  Tag retrieve(Long id, boolean hit);

  /**
   * Retrieve a TagCategory by its id
   *
   * @param id a tag category id
   * @param hit true if this retrieval must be counted as a visualization
   * @return the tag category concrete object corresponding to the given id, null if not found
   */
  TagCategory retrieveTagCategory(Long id, boolean hit);

  /**
   * Retrieve a TagLink between two tags
   *
   * @param parent the parent tag id
   * @param child the child tag id
   * @return a TagLink if given ids are tag and linked, null otherwise
   */
  TagLink retrieveLink(Long parent, Long child);

  /**
   * Find a list of Tag by their (partial) name
   *
   * @param name a name
   * @param type the Tag type to search
   * @return the list of Tags with their names containing the given name, or an empty list if none found
   */
  List<Tag> findByName(String name, ETagType type);

  /**
   * Find a tag by its complete name, lang and tag type
   *
   * @param name the name to find
   * @param lang a two-char iso-639-1 language code
   * @param type a tag type
   * @return a the matched Tag, null otherwise
   */
  Tag findUniqueByNameAndLang(String name, String lang, ETagType type);

  /**
   * Retrieve all context contributions where a tag is category
   *
   * @param category a tag category id
   * @return a list of context contributions
   */
  List<ContextContribution> getContextContributions(Long category);

  /**
   * Save a tag on behalf of a given contributor If tag.getId has been set, update the
   * tag, otherwise create tag and update contribution id.
   *
   * @param contribution a contribution tag to save
   * @param contributor the contributor id that asked to save the contribution
   * @return a map of Contribution type and a possibly empty list of Contributions created tags
   *
   * @throws PermissionException if given contributor may not publish in WebDeb public group
   * @throws PersistenceException if an error occurred, a.o., unset required field or no version number for
   * an existing contribution (id set). The exception message will contain a more complete description of the
   * error.
   */
  Map<Integer, List<Contribution>> save(Tag contribution, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save a tag link. Both tags provided in given link must exist, as well as the contributor.
   *
   * @param link the link to save
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in WebDeb public group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(TagLink link, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Save a context has category link. Both contributions provided in given link must exist, as well as the contributor.
   *
   * @param link the link to save
   * @param currentGroup the current contributor group
   * @param contributor the contributor id that asked to save the contribution
   *
   * @throws PermissionException if given contributor may not publish in WebDeb public group
   * @throws PersistenceException if an error occurred at the database layer (concrete error will be wrapped)
   */
  void save(ContextHasCategory link, int currentGroup, Long contributor) throws PermissionException, PersistenceException;

  /**
   * Retrieve all tag types
   *
   * @return a list of tag types
   */
  List<TagType> getTagTypes();

  /**
   * Find the list of parents tag for a given tag id
   *
   * @param tag a tag id
   * @return the list of parents Tag
   */
  List<Tag> getParents(Long tag);

  /**
   * Find the list of children tag for a given tag id
   *
   * @param tag a tag id
   * @return the list of children Tag
   */
  List<Tag> getChildren(Long tag);

  /**
   * Find the list of parent links tag for a given tag id
   *
   * @param tag a tag id
   * @return the list of parents Tag
   */
  List<TagLink> getParentLinks(Long tag);

  /**
   * Get parents tags of given tag as TagLink
   *
   * @param query the query used for retrieve parents
   * @return a possibly empty list of TagLink
   *
   */
  List<TagLink> getParentsLinks(SearchContainer query);

  /**
   * Find the list of child tag links for a given tag id
   *
   * @param tag a tag id
   * @return the list of children Tag
   */
  List<TagLink> getChildLinks(Long tag);

  /**
   * Get children tags of given tag as TagLink
   *
   * @param query the query used for retrieve children
   * @return a possibly empty list of TagLink
   */
  List<TagLink> getChildrenLinks(SearchContainer query);

  /**
   * Get a sublist random children
   *
   * @param tag the parent tag
   * @return a possibly empty list of TagLink
   */
  List<TagLink> getRandomChildren(Long tag);

  /**
   * Find a list of rewording name from a given tag
   *
   * @param tag a tag id
   * @return a list of TagName
   * @see TagName
   */
  List<TagName> getTagRewordingNames(Long tag);

  /**
   * Get the list of citations of the tag and for a given text
   *
   * @param tag the tag id
   * @param text a text id
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromTagAndText(Long tag, Long text);

  /**
   * Get the hierarchy tree of a given tag
   *
   * @param tag a tag id
   * @return a hierarchy tree
   * @see HierarchyTree
   */
  HierarchyTree getTagHierarchyTree(Long tag);

  /**
   * Check if a tag (as hierarchy) can be add in the hierarchy of a given tag
   *
   * @param tag a tag
   * @param hierarchy a tag to add at the given tag hierarchy
   * @param isParent true if the given tag will be the parent of the hierarchy tag
   * @return a hierarchy code depending of the issue
   * @see EHierarchyCode
   */
  EHierarchyCode checkHierarchy(Tag tag, Tag hierarchy, boolean isParent);

  /**
   * Get the number of contributions contained in this tag
   *
   * @param tag a tag id
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of contributions contained in this tag
   */
  int getNbContributions(Long tag, Long contributor, int group);

  /**
   * Get the number of contributions contained in this tag by contribution type
   *
   * @param tag a tag id
   * @param type a contribution type
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of contributions contained in this tag
   */
  int getNbContributionsByType(Long tag, EContributionType type, Long contributor, int group);

  /**
   * Get the list of new children for tag in the database
   *
   * @return a possibly empty map of parent tag / set of new discovered children
   */
  Map<Long, Set<Long>> findNewTagsChildren();

  /**
   * Get the list of debates that have the given tag
   *
   * @param tag a tag id
   * @return a possibly empty list of debates
   */
  List<Debate> getDebates(Long tag);

  /**
   * Get the list of debates that have given tag
   *
   * @param query the query used for retrieve debates
   * @return a possibly empty list of debates
   */
  List<Debate> getDebates(SearchContainer query);

  /**
   * Get a randomly chose Tag
   *
   * @return a Tag
   */
  Tag random();

}
