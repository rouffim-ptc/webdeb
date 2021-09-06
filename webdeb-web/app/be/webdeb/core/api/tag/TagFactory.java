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

package be.webdeb.core.api.tag;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.exception.FormatException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents an abstract factory to handle tags.
 *
 * @author Martin Rouffiange
 */
public interface TagFactory extends ContributionFactory {

  /**
   * Retrieve a Tag by its id
   *
   * @param id a Contribution id
   * @return a Tag if given id is a tag, null otherwise
   */
  Tag retrieve(Long id);

  /**
   * Retrieve a tag by its id and increment visualization hit of this contribution
   *
   * @param id a tag id
   * @return the tag concrete object corresponding to the given id, null if no found
   */
  Tag retrieveWithHit(Long id);

  /**
   * Retrieve a TagLink between two tags
   *
   * @param parent the parent tag id
   * @param child the child tag id
   * @return a TagLink if given ids are tag and linked, null otherwise
   */
  TagLink retrieveLink(Long parent, Long child);

  /**
   * Construct an empty Tag instance
   *
   * @return a new Tag instance
   */
  Tag getTag();

  /**
   * Construct an empty TagCategory instance
   *
   * @return a new TagCategory instance
   */
  TagCategory getTagCategory();

  /**
   * Construct an empty TagLink instance
   *
   * @return a new TagLink instance
   */
  TagLink getTagLink();

  /**
   * Construct an empty ContextHasCategory instance
   *
   * @return a new ContextHasCategory instance
   */
  ContextHasCategory getContextHasCategory();

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
   * Construct a TagType
   *
   * @param type the tag type id
   * @param i18names a map of pairs of the form (2-char iso-code, type name)
   * @return an TagType instance
   */
  TagType createTagType(int type, Map<String, String> i18names);

  /**
   * Get a tag type by its id
   *
   * @param type a tag type id
   * @return the TagType corresponding to the given type id
   *
   * @throws FormatException if given id does not exist
   */
  TagType getTagType(int type) throws FormatException;

  /**
   * Retrieve all tag types
   *
   * @return the list of all tag types
   */
  List<TagType> getTagTypes();

  /**
   * Construct a TagName
   *
   * @param lang a two-char iso-639-1 language code
   * @param name the tag name
   * @return a TagName instance
   */
  TagName createTagName(String lang, String name);

  /**
   * Find a list of Tag by their (partial) name
   *
   * @param name a name
   * @return the list of Tags with their names containing the given name, or an empty list if none found
   */
  List<Tag> findByName(String name);

  /**
   * Find a list of Tag by their (partial) name and type
   *
   * @param name a name
   * @param type the Tag type to search
   * @return the list of Tags with their names containing the given name, or an empty list if none found
   */
  List<Tag> findByName(String name, ETagType type);

  /**
   * Find a list of rewording name from a given tag
   *
   * @param tag a tag id
   * @return a list of TagName
   * @see TagName
   */
  List<TagName> getTagRewordingNames(Long tag);

  /**
   * Construct an empty tag HierarchyTree instance
   *
   * @return a new HierarchyTree  instance
   */
  HierarchyTree createHierarchyTree();

  /**
   * Construct a tag HierarchyNode instance
   *
   * @param id the node unique id
   * @param name the node name
   * @return a new tag HierarchyNode instance
   */
  HierarchyNode createHierarchyNode(long id, String name);

  /**
   * Construct a tag HierarchyNode instance
   *
   * @param id the node unique id
   * @param name the node name
   * @param depth the depth of the node in the hierarchy tree
   * @return a new tag HierarchyNode instance
   */
  HierarchyNode createHierarchyNode(long id, String name, int depth);

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
   * Get de default tag for rss text feeding, if nlp don't found any
   *
   * @return a hierarchy code depending of the issue
   */
  Tag getRssDefaultTag();

  /**
   * Get the list of new children for tag in the database
   *
   * @return a possibly empty map of parent tag / set of new discovered children
   */
  Map<Long, Set<Long>> findNewTagsChildren();

  /**
   * Get a randomly chose Tag
   *
   * @return a Tag
   */
  Tag random();

}
