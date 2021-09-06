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

package be.webdeb.presentation.web.controllers.entry.tag;

import be.webdeb.core.api.tag.*;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.presentation.web.controllers.entry.ContextContributionHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of a Tag (no IDs, but their description, as defined in the
 * database). Except by using a constructor, no value can be edited outside of this package or by
 * subclassing.
 *
 * @author Martin Rouffiange
 */
public class TagHolder extends ContextContributionHolder {

  @Inject
  protected TagFactory tagFactory = Play.current().injector().instanceOf(TagFactory.class);

  // used for lazy loading of tag name
  protected Tag tag;

  // Note: as for all wrappers, all fields MUST hold empty values for proper form validation
  protected List<TagNameForm> tagNames = new ArrayList<>();
  protected List<TagNameForm> tagRewordingNames = new ArrayList<>();
  protected String name = "";
  protected String completeName = "";
  protected ETagType etagType;
  protected int tagType = ETagType.SIMPLE_TAG.id();
  protected String tagTypeName = "";
  protected int nbTotalContributions = -1;

  private Long linkId = null;

  protected Long contextId = -1L;

  // all parent tags of this tag
  protected List<SimpleTagForm> parents = null;
  // all child tags of this tag
  protected List<SimpleTagForm> children = null;
  // all child tags of this tag
  protected List<SimpleTagForm> randomChildren = null;

  protected List<DebateHolder> debates = null;
  private List<CitationHolder> citations = null;


  /**
   * Play / JSON compliant constructor
   */
  public TagHolder() {
    super();
    type = EContributionType.TAG;
  }

  /**
   * Constructor. Create a holder for a Tag (i.e. no type/data IDs, but their descriptions, as defined in
   * the database).
   *
   * @param tag an Tag
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagHolder(Tag tag, WebdebUser user, String lang) {
    this(tag, user, lang, false);
  }

  /**
   * Constructor. Create a holder for a TagLink (i.e. no type/data IDs, but their descriptions, as defined in
   * the database).
   *
   * @param link a tag link
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagHolder(TagLink link, WebdebUser user, String lang, boolean light, boolean forParents) {
    this(forParents ? link.getParent() : link.getChild(), user, lang, light);

    this.linkId = link.getId();
    this.nbContributions = link.getNbContributions();
  }
  /**
   * Constructor. Create a holder for a Tag (i.e. no type/data IDs, but their descriptions, as defined in
   * the database).
   *
   * @param tag an Tag
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagHolder(Tag tag, WebdebUser user, String lang, boolean light) {
    super(tag, user, lang, light);

    this.tag = tag;
    name = values.firstLetterUpper(tag.getName(lang));
    completeName = tag.getCompleteName(lang);

    if(!light) {
      this.tagNames = tag.getNames().entrySet().stream().map(n -> new TagNameForm(n.getKey(), n.getValue())).collect(Collectors.toList());
      initRewordingNames(tag, lang, false);
    }

    tagType = tag.getTagType().getType();
    etagType = tag.getTagType().getEType();
    tagTypeName = tag.getTagType().getName(lang);

    switch (etagType) {
      case CATEGORY_TAG:
      case SUB_DEBATE_TAG:
        TagCategory tagCategory = (TagCategory) tag;
        this.contextId = tagCategory.getCurrentContextId();
        break;
      default:
    }
  }

  /**
   * Initialize rewording names in depends of the lang
   *
   * @param tag an Tag
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @param onlyLang only keep rewording names for the given lang
   */
  private void initRewordingNames(Tag tag, String lang, boolean onlyLang) {
    List<TagName> rewordingNames =
            (onlyLang ? tag.getRewordingNamesByLang(lang) : tagFactory.getTagRewordingNames(tag.getId()));
    this.tagRewordingNames = rewordingNames.stream().map(n ->
            new TagNameForm(n.getLang(), n.getName())).collect(Collectors.toList());
  }

  /**
   * Lazy loading of parents and children tags links
   */
  protected void initLinks() {
    parents = new ArrayList<>();
    children = new ArrayList<>();

    if(tag != null) {
      tag.getParentsAsLink().forEach(l -> parents.add(new SimpleTagForm(l.getParent(), l.getId(), lang)));
      tag.getChildrenAsLink().forEach(l -> children.add(new SimpleTagForm(l.getChild(), l.getId(), lang)));
    }
  }

  @Override
  public String getContributionDescription(){
    List<String> descriptions = new ArrayList<>();
    descriptions.addAll(getTagRewordingNames(3));
    //descriptions.addAll(getLinkedDebates(new WebdebUser(), 3).stream().map(DebateHolder::getTitle).collect(Collectors.toList()));
    //descriptions.addAll(getLinkedArgumentContexts(new WebdebUser(), 3).stream().map(e -> e.getArgument().getTitle()).collect(Collectors.toList()));
    //descriptions.addAll(getLinkedTexts(new WebdebUser(), 3).stream().map(TextHolder::getTitle).collect(Collectors.toList()));

    return String.join(", ", descriptions);
  }

  @Override
  public MediaSharedData getMediaSharedData(){
    if(mediaSharedData == null){
      mediaSharedData = new MediaSharedData(name, "tag");
    }
    return mediaSharedData;
  }

  @Override
  public String getDefaultAvatar(){
    return "";
  }

  @Override
  public String toString() {
    return "tag [" + id + "] "+name;
  }

  /*
   * GETTERS
   */

  /**
   * Get the tag name
   *
   * @return a tag name
   */
  public String getTagName() {
    return name;
  }

  public String getCompleteName() {
   // return completeName;
    return name;
  }

  public String getChildrenTagsDescription() {
    return getRandomChildren().stream().map(SimpleTagForm::getName).collect(Collectors.joining("#"));
  }

  public Long getContextId() {
    return contextId;
  }

  /**
   * Get the tag full name
   *
   * @return a tag full name (with description)
   */
  public String getTagFullName(boolean full) {
      String ch = name;
      if(full && !tagRewordingNames.isEmpty())
        ch += String.join(", ", getTagRewordingNames(3));
    return ch;
  }

  /**
   * Get the list of parent tag links
   *
   * @return a list of SimpleTagForm
   */
  public synchronized List<SimpleTagForm> getParents() {
    if (parents == null) {
      initLinks();
    }
    return parents;
  }

  /**
   * Get the list of child tag links
   *
   * @return a list of SimpleTagForm
   */
  public synchronized List<SimpleTagForm> getChildren() {
    if (children == null) {
      initLinks();
    }
    return children;
  }

  /**
   * Get the list of parent tag links sorted by number of given contribution type in parents
   *
   * @return a list of SimpleTagForm
   */
  public synchronized List<SimpleTagForm> getParentsSortedByNbCType(WebdebUser user, EContributionType ctype) {
    getParents();

    return parents.stream()
            .sorted(Comparator.comparingInt(e -> ((SimpleTagForm) e).getNbContributionsByType(ctype, user.getId(), user.getGroupId())).reversed())
            .collect(Collectors.toList());
  }

  /**
   * Get the list of child tag links sorted by number of given contribution type in children
   *
   * @return a list of SimpleTagForm
   */
  public synchronized List<SimpleTagForm> getChildrenSortedByNbCType(WebdebUser user, EContributionType ctype) {
    getChildren();

    return children.stream()
            .sorted(Comparator.comparingInt(e -> ((SimpleTagForm) e).getNbContributionsByType(ctype, user.getId(), user.getGroupId())).reversed())
            .collect(Collectors.toList());
  }

  /**
   * Get a random list of child tags, max 3 choosen randomly
   *
   * @return a list of SimpleTagForm
   */
  public synchronized List<SimpleTagForm> getRandomChildren() {
    if(randomChildren == null) {
      randomChildren = tag.getRandomChildren()
              .stream()
              .map(l -> new SimpleTagForm(l.getChild(), l.getId(), lang))
              .collect(Collectors.toList());
    }
    return randomChildren;
  }

  /**
   * Get the list of parent tag links
   *
   * @param nbParents the number to maximum parents in the list
   * @return a list of tags name
   */
  public List<String> getParentsName(int nbParents) {
    List<SimpleTagForm> parents = getParents();
    parents = (parents.size() > nbParents ? parents.subList(0, nbParents) : parents);
    return parents.stream().map(SimpleTagForm::getName).collect(Collectors.toList());
  }

  /**
   * Get the list of child tag links
   *
   * @param nbChildren the number to maximum children in the list
   * @return a list of tags names
   */
  public List<String> getChildrenName(int nbChildren) {
    List<SimpleTagForm> children = getChildren();
    children = (children.size() > nbChildren ? children.subList(0, nbChildren) : children);
    return children.stream().map(SimpleTagForm::getName).collect(Collectors.toList());
  }

  /**
   * Get the list of complete hierachy of tag links
   *
   * @return a list of SimpleTagForm
   */
  @JsonIgnore
  public List<SimpleTagForm> getHierarchy() {
    if (parents == null || children == null) {
      initLinks();
    }
    parents.addAll(children);
    return parents;
  }

  /**
   * Get the tag type from which this tag originates from
   *
   * @return the tag type
   */
  public int getTagType() {
    return tagType;
  }

  /**
   * Get the tag type name from which this tag originates from
   *
   * @return the tag type name
   */
  public String getTagTypeName() {
    return tagTypeName;
  }

  /**
   * Get the tag names
   *
   * @return the possibly empty list of tag names
   */
  public List<TagNameForm> getTagNames() {
    return (tagNames != null ? tagNames : new ArrayList<>());
  }

  /**
   * Get the tag names limited by number
   *
   * @param limit the maximum number of names in the returned list
   * @return the possibly empty list of tag names
   */
  public List<TagNameForm> getTagNames(int limit) {
    if(tagNames == null){
      return new ArrayList<>();
    }
    return (limit < tagNames.size() ? tagNames.subList(0, limit) : tagNames);
  }

  /**
   * Get the rewording tag names
   *
   * @return the possibly empty list of rewording tag names
   */
  public List<TagNameForm> getTagRewordingNames() {
    return (tagRewordingNames != null ? tagRewordingNames : new ArrayList<>());
  }

  /**
   * Get the tag rewording name limited by number
   *
   * @param limit the maximum number of names in the returned list
   * @return the possibly empty list of tag names
   */
  public List<String> getTagRewordingNames(int limit) {
    getTagRewordingNames();
    List<TagNameForm> asForm = (limit < tagRewordingNames.size() ? tagRewordingNames.subList(0, limit) : tagRewordingNames);

    return asForm.stream().map(f -> f.getName()).collect(Collectors.toList());
  }

  public int getNbTotalContributions() {
    if(nbTotalContributions == -1){
      nbTotalContributions = tag.getNbContributions(user.getId(), user.getGroup().getGroupId());
    }
    return nbTotalContributions;
  }

  public Long getLinkId() {
    return linkId;
  }

  @JsonIgnore
  public List<DebateHolder> getDebates() {
    if(debates == null){
      debates = getDebates(0, -1, null);
    }
    return debates;
  }

  @JsonIgnore
  public List<CitationHolder> getCitations() {
    if(citations == null){
      citations = getCitations(0, -1, null);
    }
    return citations;
  }

  public List<DebateHolder> getDebates(int fromIndex, int toIndex, String filters) {
      return helper.toDebatesHolders(tag.getDebates(
              new SearchContainer(
                      tag.getId(),
                      EContributionType.DEBATE,
                      user.getId(),
                      user.getGroupId(),
                      lang,
                      filters,
                      fromIndex,
                      toIndex)
      ), user, lang, true);
  }

  public List<CitationHolder> getCitations(int fromIndex, int toIndex, String filters) {
      return helper.toCitationsHolders(tag.getAllCitations(
              new SearchContainer(
                      tag.getId(),
                      EContributionType.CITATION,
                      user.getId(),
                      user.getGroupId(),
                      lang,
                      filters,
                      fromIndex,
                      toIndex)
      ), user, lang, true);
  }


  public synchronized List<TagHolder> getParentHolders(EContributionType contributionType) {
     return getParentHolders(contributionType,0, -1, null);
  }

  public synchronized List<TagHolder> getChildHolders(EContributionType contributionType) {
      return getChildHolders(contributionType,0,  -1, null);
  }

  public synchronized List<TagHolder> getParentHolders(EContributionType contributionType, int fromIndex, int toIndex, String filters) {
    return helper.toTagsHolders(tag.getParentsAsLink(
            new SearchContainer(
                    tag.getId(),
                    contributionType,
                    user.getId(),
                    user.getGroupId(),
                    lang,
                    filters,
                    fromIndex,
                    toIndex)
    ), user, lang, false, true);
  }

  public synchronized List<TagHolder> getChildHolders(EContributionType contributionType, int fromIndex, int toIndex, String filters) {
    return helper.toTagsHolders(tag.getChildrenAsLink(
            new SearchContainer(
                    tag.getId(),
                    contributionType,
                    user.getId(),
                    user.getGroupId(),
                    lang,
                    filters,
                    fromIndex,
                    toIndex)
    ), user, lang, false, false);
  }

  /**
   * Get the parents hierarchy of this tag as JSON
   *
   * @return the parents hierarchy as json
   */
  @JsonIgnore
  public String getParentsHierarchyAsJson() {
    return tag.getParentsHierarchyAsJson();
  }

  /**
   * Get the children hierarchy of this tag as JSON
   *
   * @return the children hierarchy as json
   */
  @JsonIgnore
  public String getChildrenHierarchyAsJson() {
    return tag.getChildrenHierarchyAsJson();
  }

}
