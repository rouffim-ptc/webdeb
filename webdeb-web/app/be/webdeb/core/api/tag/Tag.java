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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.impl.helper.SearchContainer;

import java.util.List;
import java.util.Map;

/**
 * This interface represents a tag in the webdeb system. A tag consists
 * to sort arguments, citations, debates and texts that share the same theme or category.
 * A tag has commonly parents and children that form a tree hierarchy structure.
 *
 * @author Martin Rouffiange
 */
public interface Tag extends ContextContribution {

  /**
   * Get parents tags of this tag
   *
   * @return a possibly empty list of tags
   */
  List<Tag> getParents();

  /**
   * Initialize the list of parents
   */
  void initParents();

  /**
   * Get parents tags of this tag as TagLink
   *
   * @return a possibly empty list of TagLink
   *
   */
  List<TagLink> getParentsAsLink();

  /**
   * Get parents tags of this tag as TagLink
   *
   * @param query the query used for retrieve parents
   * @return a possibly empty list of TagLink
   *
   */
  List<TagLink> getParentsAsLink(SearchContainer query);

  /**
   * Set parents tags of this tag
   *
   * @param tags a list of tags to set
   */
  void setParents(List<Tag> tags);

  /**
   * Get children tags of this tag
   *
   * @return a possibly empty list of tags
   */
  List<Tag> getChildren();

  /**
   * Initialize the list of children
   */
  void initChildren();

  /**
   * Get children tags of this tag as TagLink
   *
   * @return a possibly empty list of TagLink
   */
  List<TagLink> getChildrenAsLink();

  /**
   * Get children tags of this tag as TagLink
   *
   * @param query the query used for retrieve children
   * @return a possibly empty list of TagLink
   *
   */
  List<TagLink> getChildrenAsLink(SearchContainer query);

  /**
   * Get a sublist random children
   *
   * @return a possibly empty list of TagLink
   */
  List<TagLink> getRandomChildren();

  /**
   * Set children tags of this tag
   *
   * @param tags a list of tags to set
   */
  void setChildren(List<Tag> tags);

  /**
   * Get parents and children tags of this tag
   *
   * @return a possibly empty list of tags
   */
  List<Tag> getHierarchy();

  /**
   * Get parents and children tags of this tag as TagLink
   *
   * @return a possibly empty list of TagLink
   */
  List<TagLink> getHierarchyAsLink();

  /**
   * Add a parent tag to this one.
   * As other fields, additions are persisted when calling save().
   *
   * @param tag a parent tag to add
   * @throws FormatException if the given tag link is incomplete
   */
  void addParent(Tag tag) throws FormatException;

  /**
   * Remove given tag from this one. If the tag is unfound, this one is unchanged.
   * As other fields, removals are persisted when calling save().
   *
   * @param tag a parent tag id to remove from this Tag
   */
  void removeParent(Long tag);

  /**
   * Add a child tag to this one.
   * As other fields, additions are persisted when calling save().
   *
   * @param tag a child tag to add
   * @throws FormatException if the given tag link is incomplete
   */
  void addChild(Tag tag) throws FormatException;

  /**
   * Remove given tag from this one. If the tag is unfound, this one is unchanged.
   * As other fields, removals are persisted when calling save().
   *
   * @param tag a child tag id to remove from this Tag
   */
  void removeChild(Long tag);

  /**
   * Get the english name or default one
   *
   * @return the corresponding value
   */
  String getDefaultName();

  /**
   * Get the language corresponding to the default name
   *
   * @return the corresponding value
   */
  String getDefaultLanguage();

  /**
   * Get the tag i18 name for given lang key
   *
   * @param lang a two char iso-639-1 language code
   * @return the corresponding value
   */
  String getName(String lang);

  /**
   * Get the tag i18 name for given lang key with the begins sentence if needed
   *
   * @param lang a two char iso-639-1 language code
   * @return the corresponding value
   */
  String getCompleteName(String lang);

  /**
   * Get the tag i18 names
   *
   * @return a map of iso-639-1 language code and corresponding values
   */
  Map<String, String> getNames();

  /**
   * Get the tag names as TagName
   *
   * @return a possibly empty list of TagName
   */
  List<TagName> getNamesAsTagName();

  /**
   * Set the tag names
   *
   * @param i18names a map of iso-639-1 language code and corresponding names
   */
  void setNames(Map<String, String> i18names);

  /**
   * Add a new name for this tag. If such a spelling existed, simply overwrite it.
   *
   * @param lang a two char ISO 639-1 code
   * @param name a lang-dependent name
   */
  void addName(String lang, String name);

  /**
   * Get the tag rewording i18 names for given lang key
   *
   * @param lang a two char iso-639-1 language code
   * @return the list of rewording names for a given lang
   * @see TagName
   */
  List<TagName> getRewordingNamesByLang(String lang);

  /**
   * Get the tag rewording i18 names
   *
   * @return the list of rewording names as a map of lang, list of TagName
   * @see TagName
   */
  Map<String, List<TagName>> getRewordingNames();

  /**
   * Set the tag rewording names
   *
   * @param i18names a map of lang, list of TagName
   * @see TagName
   */
  void setRewordingNames(Map<String, List<TagName>> i18names);

  /**
   * Add a new rewording name for this tag.
   *
   * @param lang a two char ISO 639-1 code
   * @param name a lang-dependent name
   */
  void addRewordingName(String lang, String name);

  /**
   * Get the tag type
   *
   * @return a TagType object
   */
  TagType getTagType();

  /**
   * Set the tag type
   *
   * @param type the type of tag
   */
  void setTagType(TagType type);

  /**
   * Get the hierarchy tree of this tag
   *
   * @return the tag hierarchy tree
   */
  HierarchyTree getHierarchyTree();

  /**
   * Get the parents hierarchy of this tag as JSON
   *
   * @return the parents hierarchy as json
   */
  String getParentsHierarchyAsJson();

  /**
   * Get the children hierarchy of this tag as JSON
   *
   * @return the children hierarchy as json
   */
  String getChildrenHierarchyAsJson();

  /**
   * Get the number of contributions contained in this tag
   *
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of contributions contained in this tag
   */
  int getNbContributions(Long contributor, int group);

  /**
   * Get the number of contributions contained in this tag by contribution type
   *
   * @param type a contribution type
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of contributions contained in this tag
   */
  int getNbContributionsByType(EContributionType type, Long contributor, int group);

  /**
   * Get the list of citations of this tag and for a given text
   *
   * @param text a text id
   * @return a possibly empty list of citations
   */
  List<Citation> getCitationsFromText(Long text);

  /**
   * Get the list of debates that have this tag
   *
   * @return a possibly empty list of debates
   */
  List<Debate> getDebates();

  /**
   * Get the list of debates that have this tag
   *
   * @param query the query used for retrieve debates
   * @return a possibly empty list of debates
   */
  List<Debate> getDebates(SearchContainer query);
}
