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

package be.webdeb.core.impl.tag;

import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.tag.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.core.impl.contribution.link.ConcreteContextHasCategory;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements an factory for tags, tags types and links.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteTagFactory extends AbstractContributionFactory<TagAccessor> implements TagFactory {

  @Inject
  private ActorAccessor actorAccessor;

  // key is shade id
  private Map<Integer, TagType> tagTypes;

  @Override
  public Tag retrieve(Long id) {
    return accessor.retrieve(id, false);
  }

  @Override
  public Tag retrieveWithHit(Long id) {
    return accessor.retrieve(id, true);
  }

  @Override
  public TagLink retrieveLink(Long parent, Long child) {
    return accessor.retrieveLink(parent, child);
  }

  @Override
  public Tag getTag() {
    return new ConcreteTag(this, accessor, actorAccessor, contributorFactory);
  }

  @Override
  public TagCategory getTagCategory() {
    return new ConcreteTagCategory(this, accessor, actorAccessor, contributorFactory);
  }

  @Override
  public TagLink getTagLink() {
    return new ConcreteTagLink(this, accessor, contributorFactory);
  }

  @Override
  public ContextHasCategory getContextHasCategory() {
    return new ConcreteContextHasCategory(this, accessor, contributorFactory);
  }

  @Override
  public Tag findUniqueByNameAndLang(String name, String lang, ETagType type){
    return accessor.findUniqueByNameAndLang(name, lang, type);
  }

  @Override
  public Tag random() {
    return accessor.random();
  }

  @Override
  public TagType createTagType(int type, Map<String, String> i18names) {
    return new ConcreteTagType(type, i18names);
  }

  @Override
  public TagType getTagType(int type) throws FormatException {
    if (tagTypes == null) {
      getTagTypes();
    }
    return tagTypes.get(type);
  }

  @Override
  public List<TagType> getTagTypes() {
    if (tagTypes == null) {
      tagTypes = accessor.getTagTypes().stream().collect(Collectors.toMap(TagType::getType, e -> e));
    }
    return new ArrayList<>(tagTypes.values());
  }

  @Override
  public TagName createTagName(String lang, String name){
    return new ConcreteTagName(lang, name);
  }

  @Override
  public List<Tag> findByName(String name){
    return accessor.findByName(name, null);
  }

  @Override
  public List<Tag> findByName(String name, ETagType type){
    return accessor.findByName(name, type);
  }

  @Override
  public List<TagName> getTagRewordingNames(Long tag){
    return accessor.getTagRewordingNames(tag);
  }

  @Override
  public HierarchyTree createHierarchyTree() {
    return new ConcreteHierarchyTree();
  }

  @Override
  public HierarchyNode createHierarchyNode(long id, String name) {
    return new ConcreteHierarchyNode(id, name);
  }

  @Override
  public HierarchyNode createHierarchyNode(long id, String name, int depth) {
    return new ConcreteHierarchyNode(id, name, depth);
  }

  @Override
  public HierarchyTree getTagHierarchyTree(Long tag) {
    return accessor.getTagHierarchyTree(tag);
  }

  @Override
  public EHierarchyCode checkHierarchy(Tag tag, Tag hierarchy, boolean isParent) {
    return accessor.checkHierarchy(tag, hierarchy, isParent);
  }

  @Override
  public Tag getRssDefaultTag(){
    Tag tag = accessor.retrieve(100000L, false);
    if(tag == null){
      tag = accessor.random();
    }
    return tag;
  }

  @Override
  public Map<Long, Set<Long>> findNewTagsChildren() {
    return accessor.findNewTagsChildren();
  }
}