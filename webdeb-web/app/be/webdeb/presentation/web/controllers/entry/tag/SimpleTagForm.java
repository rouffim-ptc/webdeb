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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.tag.TagName;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.model.Group;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import play.api.Play;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple form for tag name
 *
 * @author Martin Rouffiange
 */
public class SimpleTagForm {

  @Inject
  protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

  private Tag tag;
  private Long linkId = null;
  private Long tagId = -1L;
  private String completeName;
  private String name;
  private String lang;
  private int tagType;
  private int nbParents = 0;
  private Integer nbContributions = null;
  private Map<EContributionType, Integer> nbContributionsByType = new LinkedHashMap<>();

  // will be put to true if user explicitly disambiguated this tag
  // if this id is -1, the tag will be created (whatever this tag is a namesake or not)
  // if this tag has an id, we will not check for name matches
  private boolean isDisambiguated = false;
  // list of tags whith the same name as this tag (used when tag has no id and we possibly found existing tags)
  private List<TagHolder> nameMatches = new ArrayList<>();

  private List<DebateHolder> debates = null;
  private List<CitationHolder> citations = null;

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);
  @Inject
  protected TagFactory tagFactory = Play.current().injector().instanceOf(TagFactory.class);


  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Play / JSON compliant constructor
   */
  public SimpleTagForm() {
    // needed by play
  }

  /**
   * Creates a simple tag form from an id, a name and a tag type
   *
   * @param tag a tag
   * @param lang the user lang
   */
  public SimpleTagForm(Tag tag, String lang) {
    this.tag = tag;
    this.tagId = tag.getId();
    this.name = tag.getName(lang);
    this.completeName = tag.getRewordingNamesByLang(lang).isEmpty() ? tag.getName(lang) :
            tag.getName(lang) + " (" + tag.getRewordingNamesByLang(lang).stream().map(TagName::getName)
                    .collect(Collectors.joining(" ,")) + ")";
    this.tagType = tag.getTagType().getType();
    this.lang = lang;
  }

  /**
   * Creates a simple tag form from a tag and a tag link id
   *
   * @param tag a tag
   * @param linkId a tag link id
   * @param lang the user lang
   */
  public SimpleTagForm(Tag tag, Long linkId, String lang) {
    this(tag, lang);
    this.linkId = linkId;
  }

  /**
   * Cast this form to an API Tag object
   *
   * @param lang the user lang
   * @return the Form holding all values of this form
   */
  public Tag toTag(String lang) {
    logger.debug("transform tag " + toString());
    Tag tag = tagFactory.getTag();

    // check tag name
    if (!values.isBlank(name)) {
      tag.addName(lang, name.trim());
      try {
        tag.setTagType(tagFactory.getTagType(ETagType.SIMPLE_TAG.id()));
        tag.addInGroup(Group.getPublicGroup().getIdGroup());
        return tag;
      } catch (FormatException e) {
        logger.debug("Tag type problem : " + e);
      }
    }
    return null;
  }

  /**
   * Helper method to check whether the name and id are empty (contains empty or null values)
   *
   * @return true if all fields are empty
   */
  public boolean isEmpty() {
    return values.isBlank(name);
  }

  @Override
  public String toString() {
    return "SimpleTagForm{" +
        "tagId=" + tagId +
        "tagLinkId=" + linkId +
        ", completename='" + completeName + '\'' +
        ", name='" + name + '\'' +
        ", type='" + tagType + '\'' +
        '}';
  }

  /*
   * GETTERS / SETTERS
   */

  public Long getTagId() {
    return tagId;
  }

  public void setTagId(Long tagId) {
    this.tagId = tagId;
  }

  public Long getLinkId() {
    return linkId;
  }

  public void setLinkId(Long linkId) {
    this.linkId = linkId;
  }

  public String getCompleteName() {
    return completeName;
  }

  public void setCompleteName(String completeName) {
    this.completeName = completeName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTagType() {
    return tagType;
  }

  public void setTagType(int tagType) {
    this.tagType = tagType;
  }

  public int getNbParents() {
    return nbParents;
  }

  public void setNbParents(int nbParents) {
    this.nbParents = nbParents;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  /*
   * NAME MATCHES HANDLING
   */

  /**
   * Get the isDisambiguated flag, ie, if this flag is false and this tag has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @return true if this tag has been explicitly disambiguated
   */
  public boolean getIsDisambiguated() {
    return isDisambiguated;
  }

  /**
   * Set the isDisambiguated flag, ie, if this flag is false and this tag has no id, a search by name (full match)
   * must be performed at form validation time to try to retrieve a possible match in database.
   * Otherwise (this flag is true), no check must be performed whatever the id value is
   *
   * @param isDisambiguated true if this tag has been explicitly disambiguated
   */
  public void setIsDisambiguated(boolean isDisambiguated) {
    this.isDisambiguated = isDisambiguated;
  }

  /**
   * Get the list  possible matches on this tag's name
   *
   * @return a (possibly empty) list of tag holder having the same name as this tag
   */
  public List<TagHolder> getNameMatches() {
    return nameMatches;
  }

  /**
   * Set the list of possible matches on this tag's full name
   *
   * @param nameMatches a list of tag holder having the same name as this tag
   */
  public void setNameMatches(List<TagHolder> nameMatches) {
    this.nameMatches = nameMatches;
  }

  /**
   * Get the number of contributions contained in this tag
   *
   * @param user the user id
   * @param group the group id
   * @return the number of contributions contained in this tag
   */
  public int getNbContributions(Long user, int group){
    if(nbContributions == null) {
      nbContributions = tag.getNbContributions(user, group);
    }

    return nbContributions;
  }

  /**
   * Get the number of contributions contained in this tag by contribution type
   *
   * @param type the contribution type
   * @param user the user id
   * @param group the group id
   * @return the number of contributions contained in this tag
   */
  public synchronized int getNbContributionsByType(EContributionType type, Long user, int group){
    if(!nbContributionsByType.containsKey(type)) {
      nbContributionsByType.put(type, tag.getNbContributionsByType(type, user, group));
    }

    return nbContributionsByType.get(type);
  }

  @JsonIgnore
  public List<DebateHolder> getDebates(WebdebUser user) {
    if(debates == null){
      debates = helper.toDebatesHolders(tag.getDebates(), user, lang, true);
    }
    return debates;
  }

  @JsonIgnore
  public List<CitationHolder> getCitations(WebdebUser user) {
    if(citations == null){
      citations = helper.toCitationsHolders(tag.getAllCitations(), user, lang, false);
    }
    return citations;
  }
}
