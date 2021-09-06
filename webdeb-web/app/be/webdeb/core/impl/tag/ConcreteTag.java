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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.*;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContextContribution;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;
import play.i18n.Lang;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements a Webdeb Tag, that is linked with contributions.
 *
 * @author Martin Rouffiange
 */
class ConcreteTag extends AbstractContextContribution<TagFactory, TagAccessor> implements Tag {

  protected TagType tagType;

  private List<Tag> parents;
  private List<Tag> children;
  private List<TagLink> parentLinks;
  private List<TagLink> childLinks;
  private HierarchyTree hierarchyTree = null;

  private Map<String, String> names = new HashMap<>();
  private List<TagName> tagNames = new ArrayList<>();
  private Map<String, List<TagName>> rewordingNames = new HashMap<>();

  private List<Debate> debates = null;
  private List<Text> texts = null;
  private List<Citation> citations = null;

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  ConcreteTag(TagFactory factory, TagAccessor tagAccessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, tagAccessor, actorAccessor, contributorFactory);
    type = EContributionType.TAG;
  }


  @Override
  public List<Tag> getParents() {
    if (parents == null) {
      parents = accessor.getParents(id);
    }
    return parents;
  }

  @Override
  public void initParents(){
    parents = new ArrayList<>();
  }


  @Override
  public List<TagLink> getParentsAsLink() {
    if (parentLinks == null) {
      parentLinks = accessor.getParentLinks(id);
    }
    return parentLinks;
  }

  @Override
  public List<TagLink> getParentsAsLink(SearchContainer query) {
    return accessor.getParentsLinks(query);
  }

  @Override
  public void setParents(List<Tag> tags) {
    if (tags != null) {
      parents = tags;
    }
  }

  @Override
  public List<Tag> getChildren() {
    if (children == null) {
      children = accessor.getChildren(id);
    }
    return children;
  }

  @Override
  public void initChildren(){
    children = new ArrayList<>();
  }

  @Override
  public List<TagLink> getChildrenAsLink() {
    if (childLinks == null) {
      childLinks = accessor.getChildLinks(id);
    }
    return childLinks;
  }

  @Override
  public List<TagLink> getChildrenAsLink(SearchContainer query) {
    return accessor.getChildrenLinks(query);
  }

  @Override
  public List<TagLink> getRandomChildren() {
    return accessor.getRandomChildren(id);
  }

  @Override
  public void setChildren(List<Tag> tags) {
    if(children != null){
      children = tags;
    }
  }

  @Override
  public void addParent(Tag tag) throws FormatException {
    if(tag != null) {
      List<String> fieldsInError = tag.isValid();
      if (!fieldsInError.isEmpty()) {
        throw new FormatException(FormatException.Key.UNKNOWN_TAG, fieldsInError.toString());
      }
      getParents().add(tag);
    }
  }

  @Override
  public void removeParent(Long tag) {
    getParents();
    if (parents != null) {
      parents.removeIf(a -> a.getId().equals(tag));
    }
  }

  @Override
  public void addChild(Tag tag) throws FormatException {
    if(tag != null) {
      List<String> fieldsInError = tag.isValid();
      if (!fieldsInError.isEmpty()) {
        throw new FormatException(FormatException.Key.UNKNOWN_TAG, fieldsInError.toString());
      }
      getChildren().add(tag);
    }
  }

  @Override
  public void removeChild(Long tag) {
    getChildren();
    if (children != null) {
      children.removeIf(a -> a.getId().equals(tag));
    }
  }

  @Override
  public List<Tag> getHierarchy(){
    List<Tag> hierarchy = getParents();
    hierarchy.addAll(getChildren());
    return hierarchy;
  }

  @Override
  public List<TagLink> getHierarchyAsLink(){
    List<TagLink> hierarchy = getParentsAsLink();
    hierarchy.addAll(getChildrenAsLink());
    return hierarchy;
  }

  @Override
  public String getDefaultName() {
    String name = "";
    if(names != null && !names.isEmpty()){
      if(names.containsKey("en")){
        name = names.get("en");
      }else if(names.containsKey("fr")){
        name = names.get("fr");
      }else{
        Optional<Map.Entry<String, String>> firstMatchedName = names.entrySet().stream().findFirst();
        name = firstMatchedName.get().getValue();
      }
    }

    return name;
  }

  @Override
  public String getDefaultLanguage() {
    String lang = "";
    if(names != null && !names.isEmpty()){
      if(names.containsKey("en")){
        return "en";
      }else if(names.containsKey("fr")){
        return "fr";
      }else{
        Optional<Map.Entry<String, String>> firstMatchedName = names.entrySet().stream().findFirst();
        lang = firstMatchedName.get().getKey();
      }
    }
    return lang;
  }

  @Override
  public String getName(String lang) {
    if(names != null && names.containsKey(lang)){
      return names.get(lang);
    }else{
      return getDefaultName();
    }
  }

  @Override
  public String getCompleteName(String lang) {
    return computeShadeAndTitle(lang);
  }

  @Override
  public Map<String, String> getNames() {
    return names;
  }

  @Override
  public List<TagName> getNamesAsTagName(){
    if(names.size() != tagNames.size()){
      tagNames = names.entrySet().stream()
          .map(n -> factory.createTagName(n.getKey(), n.getValue())).collect(Collectors.toList());
    }
    return tagNames;
  }

  @Override
  public void setNames(Map<String, String> i18names) {
    if(i18names != null){
      names = i18names;
    }
  }

  @Override
  public void addName(String lang, String name) {
    if(names != null && !"".equals(name.trim()) && lang != null && !"".equals(lang.trim())) {
      if(getTagType() != null && getTagType().getEType() == ETagType.SIMPLE_TAG) {
        name = factory.getValuesHelper().firstLetterLower(name);
      }
      names.put(lang, name);
    }
  }

  @Override
  public List<TagName> getRewordingNamesByLang(String lang) {
    if(lang != null && rewordingNames != null && rewordingNames.containsKey(lang)){
      return rewordingNames.get(lang);
    }
    return new ArrayList<>();
  }

  @Override
  public Map<String, List<TagName>> getRewordingNames() {
    return (rewordingNames != null ? rewordingNames : new HashMap<>());
  }

  @Override
  public void setRewordingNames(Map<String, List<TagName>> i18names) {
    rewordingNames = i18names;
  }

  @Override
  public void addRewordingName(String lang, String name) {
    if(rewordingNames != null){
      if(!rewordingNames.containsKey(lang)){
        rewordingNames.put(lang, new ArrayList<>());
      }
      rewordingNames.get(lang).add(factory.createTagName(lang, name));
    }
  }

  @Override
  public TagType getTagType() {
    return tagType;
  }

  @Override
  public void setTagType(TagType type) {
    this.tagType = type;
  }

  @Override
  public HierarchyTree getHierarchyTree(){
    if(hierarchyTree == null){
      hierarchyTree = factory.getTagHierarchyTree(id);
    }
    return hierarchyTree;
  }

  @Override
  public String getParentsHierarchyAsJson() {
    return (getHierarchyTree() != null ? hierarchyTree.getParentsHierarchyAsJson(id) : "");
  }

  @Override
  public String getChildrenHierarchyAsJson() {
    return (getHierarchyTree() != null ? hierarchyTree.getChildrenHierarchyAsJson(id) : "");
  }

  @Override
  public int getNbContributions(Long contributor, int group) {
    return accessor.getNbContributions(id, contributor, group);
  }

  @Override
  public int getNbContributionsByType(EContributionType type, Long contributor, int group) {
    return accessor.getNbContributionsByType(id, type, contributor, group);
  }

  @Override
  public List<Citation> getCitationsFromText(Long text) {
    return accessor.getCitationsFromTagAndText(id, text);
  }

  @Override
  public List<Debate> getDebates() {
    if(debates == null) {
      debates = accessor.getDebates(id);
    }

    return debates;
  }

  @Override
  public List<Debate> getDebates(SearchContainer query) {
    return accessor.getDebates(query);
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
    List<String> isValid = isValid();
    if (!isValid.isEmpty()) {
      logger.error("tag contains errors " + isValid.toString());
      throw new FormatException(FormatException.Key.TAG_ERROR, String.join(",", isValid.toString()));
    }
    return accessor.save(this, contributor);
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();
    if (getDefaultName() == null) {
      fieldsInError.add("tag has no name");
    }
    return fieldsInError;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConcreteTag that = (ConcreteTag) o;

    // if we have ids, use them
    if (id != null && id != -1L && that.getId() != null && that.getId() != -1L) {
      return id.equals(that.getId());
    }

    String name = getName(factory.getDefaultLanguage());
    if (tagType != that.tagType) {
      return false;
    }

    return name != null ? name.equals(that.getName(factory.getDefaultLanguage()))
        : that.getName(factory.getDefaultLanguage()) == null;
  }

  @Override
  public int hashCode() {
    return 91 * (id != -1L ? id.hashCode() : 83) + getDefaultName().hashCode() + (tagType != null ? tagType.getType() : 0);
  }

  @Override
  public String toString() {
    return "id " + id + " " + getDefaultName()
        + " with parents " + (parents != null ? parents.stream().map(Tag::getDefaultName).collect(Collectors.joining(", ")) : "[]")
        + " with children " + (children != null ? children.stream().map(Tag::getDefaultName).collect(Collectors.joining(", ")) : "[]")
        + " of type " + (tagType != null ? tagType.getName("en") : " null");
  }

  /*
   * private helpers
   */

  /**
   * Convert hierarchy list of tags to list of taglinks
   *
   * @param hierarchy the list of tags in the hierarchy
   * @param isParent true if this tag is the parent in the hierarchy
   * @return either the actor name in given language, or in English if not found and finally in any existing language if none found
   */
  private List<TagLink> asLink(List<Tag> hierarchy, boolean isParent){
    List<TagLink> links = new ArrayList<>();
    for(Tag f : hierarchy){
      TagLink link = factory.getTagLink();
      if(isParent){
        link.setParent(this);
        link.setChild(f);
      }else{
        link.setParent(f);
        link.setChild(this);
      }
      link.addInGroup(Group.getGroupPublic());
      links.add(link);
    }

    return links;
  }

  /**
   * Compute a language-specific fully readable shade an title combined
   *
   * @return a reader-friendly representation of the shade and title of the debate
   */
  private String computeShadeAndTitle(String lang) {
    if(isValid().isEmpty()) {
      if(tagType.getEType() == ETagType.SIMPLE_TAG){
        String name = getName(lang);
        name = name != null && name.length() > 1 ? name.substring(0, 1).toLowerCase() + name.substring(1) : name;

        return i18n.get(lang == null ? Lang.defaultLang() : Lang.forCode(lang), "entry.citation.tags.tag.label") + " " + name + " ?";
      }

      return getName(lang);
    }

    return "";
  }
}