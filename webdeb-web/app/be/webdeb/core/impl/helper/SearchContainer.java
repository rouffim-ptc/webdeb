/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.impl.helper;

import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.actor.ESocioGroupKey;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds all needed informations to perform a query in the persistence system.
 *
 * @author Martin Rouffiange
 */
public class SearchContainer {

  String term;
  Long id;
  Long value;
  Long context;
  Long subContext;
  Long category;
  Long superArgument;
  Integer shade;
  String filters;
  Long contributor;
  Integer group;
  String lang;
  EContributionType contributionType;
  EActorRole actorRole;
  ESearchLinkType searchLinkType;
  ECitationBrowseType searchCitationType;
  EAlliesOpponentsType alliesOpponentsType;
  ESocioGroupKey socioGroupKey;
  boolean onlyInText;
  boolean actorDistanceByDebate;
  int fromIndex;
  int toIndex;

  public SearchContainer() {
    this.fromIndex = -1;
    this.toIndex = -1;
    this.searchLinkType = ESearchLinkType.NONE;
  }

  /**
   * A complete citation constructor
   *
   * @param term the term to browse
   * @param context the context where the browse is, if any
   * @param subContext a tag sub context id, if any
   * @param category the tag category where the browse is, if any
   * @param superArgument the argument where the browse is, if any
   * @param isJustification true if a justification link must be add, false for a position link
   * @param shade a justification link shade, if any
   * @param linkType a enum if a link must not already exists in context
   * @param citationType a enum for citation search
   * @param onlyInText true if the search must be only in context as text
   * @param filters a string that contains data for filter results
   * @param contributor the id of the contributor that want to process a search, or query
   * @param group the group where the contributor want to process a search, or query
   * @param lang the language to of the excerpt, if any
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   */
  public SearchContainer(String term, Long context, Long subContext, Long category, Long superArgument, Boolean isJustification, Integer shade, String filters, Long contributor, Integer group, String lang, ESearchLinkType linkType, ECitationBrowseType citationType, boolean onlyInText, int fromIndex, int toIndex) {
    this.term = term;
    this.context = context;
    this.subContext = subContext;
    this.category = category;
    this.superArgument = superArgument;
    this.alliesOpponentsType = isJustification ? EAlliesOpponentsType.ARGUMENTS : EAlliesOpponentsType.POSITIONS;
    this.shade = shade;
    this.filters = filters;
    this.contributor = contributor;
    this.group = group;
    this.lang = lang;
    this.searchLinkType = linkType;
    this.searchCitationType = citationType;
    this.onlyInText = onlyInText;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  public SearchContainer(Long id, Long contributorId, int groupId, String lang) {
    this.id = id;
    this.contributor = contributorId;
    this.group = groupId;
    this.lang = lang;
    this.actorDistanceByDebate = false;
  }

  public SearchContainer(Long id, Long contributorId, int groupId, String lang, String filters, int fromIndex, int toIndex) {
    this.id = id;
    this.contributor = contributorId;
    this.group = groupId;
    this.lang = lang;
    this.actorDistanceByDebate = false;
    this.filters = filters;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  public SearchContainer(Long context, String filters, int fromIndex, int toIndex) {
    this.context = context;
    this.filters = filters;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  public SearchContainer(String lang, String filters, Long contributorId, int groupId) {
    this.lang = lang;
    this.filters = filters;
    this.contributor = contributorId;
    this.group = groupId;
  }

  public SearchContainer(Long context, Long subContext, Long contributorId, int groupId, String lang, EAlliesOpponentsType alliesOpponentsType) {
    this.context = context;
    this.subContext = subContext;
    this.contributor = contributorId;
    this.group = groupId;
    this.lang = lang;
    this.alliesOpponentsType = alliesOpponentsType;
  }

  public SearchContainer(Long id, ESocioGroupKey key, Long context, Long subContext, Long contributorId, int groupId, String lang, EAlliesOpponentsType alliesOpponentsType) {
    this(context, subContext, contributorId, groupId, lang, alliesOpponentsType);
    this.id = id;
    this.socioGroupKey = key;
  }

  public SearchContainer(Long id, ESocioGroupKey socioGroupKey, Long value, Long contributorId, int groupId, String lang) {
    this(id, contributorId, groupId, lang);
    this.socioGroupKey = socioGroupKey;
    this.value = value;
    this.actorDistanceByDebate = true;
  }

  public SearchContainer(Long id, Long contributorId, int groupId, String filters, int fromIndex, int toIndex) {
    this.context = context;
    this.filters = filters;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
  }

  public SearchContainer(Long id, EActorRole role, Long contributorId, int groupId, String lang, String filters, int fromIndex, int toIndex) {
    this(id, contributorId, groupId, lang, filters, fromIndex, toIndex);
    this.actorRole = role;
  }

  public SearchContainer(Long id, EContributionType contributionType, Long contributorId, int groupId, String lang, String filters, int fromIndex, int toIndex) {
    this(id, contributorId, groupId, lang, filters, fromIndex, toIndex);
    this.contributionType = contributionType;
  }

  public SearchContainer(Long id, Long relatedContributionId, EContributionType contributionType, EActorRole role, Long contributorId, int groupId, String lang, String filters, int fromIndex, int toIndex) {
    this(id, role, contributorId, groupId, lang, filters, fromIndex, toIndex);
    this.contributionType = contributionType;
    this.context = relatedContributionId;
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getValue() {
    return value;
  }

  public void setValue(Long value) {
    this.value = value;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public Long getContext() {
    return context;
  }

  public void setContext(Long context) {
    this.context = context;
  }

  public Long getSubContext() {
    return subContext;
  }

  public void setSubContext(Long subContext) {
    this.subContext = subContext;
  }

  public Long getCategory() {
    return category;
  }

  public void setCategory(Long category) {
    this.category = category;
  }

  public Long getSuperArgument() {
    return superArgument;
  }

  public void setSuperArgument(Long superArgument) {
    this.superArgument = superArgument;
  }

  public Integer getShade() {
    return shade;
  }

  public void setShade(Integer shade) {
    this.shade = shade;
  }

  public String getFilters() {
    return filters;
  }

  public void setFilters(String filters) {
    this.filters = filters;
  }

  public Long getContributor() {
    return contributor;
  }

  public void setContributor(Long contributor) {
    this.contributor = contributor;
  }

  public Integer getGroup() {
    return group;
  }

  public void setGroup(Integer group) {
    this.group = group;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public ESearchLinkType getSearchLinkType() {
    return searchLinkType;
  }

  public void setSearchLinkType(ESearchLinkType linkType) {
    this.searchLinkType = linkType;
  }

  public ECitationBrowseType getSearchCitationType() {
    return searchCitationType;
  }

  public void setSearchCitationType(ECitationBrowseType searchCitationType) {
    this.searchCitationType = searchCitationType;
  }

  public EAlliesOpponentsType getAlliesOpponentsType() {
    return alliesOpponentsType;
  }

  public void setAlliesOpponentsType(EAlliesOpponentsType alliesOpponentsType) {
    this.alliesOpponentsType = alliesOpponentsType;
  }

  public ESocioGroupKey getSocioGroupKey() {
    return socioGroupKey;
  }

  public void setSocioGroupKey(ESocioGroupKey socioGroupKey) {
    this.socioGroupKey = socioGroupKey;
  }

  public EContributionType getContributionType() {
    return contributionType;
  }

  public void setContributionType(EContributionType contributionType) {
    this.contributionType = contributionType;
  }

  public EActorRole getActorRole() {
    return actorRole;
  }

  public void setActorRole(EActorRole actorRole) {
    this.actorRole = actorRole;
  }

  public boolean isOnlyInText() {
    return onlyInText;
  }

  public void setOnlyInText(boolean onlyInText) {
    this.onlyInText = onlyInText;
  }

  public boolean isActorDistanceByDebate() {
    return actorDistanceByDebate;
  }

  public void setActorDistanceByDebate(boolean actorDistanceByDebate) {
    this.actorDistanceByDebate = actorDistanceByDebate;
  }

  public int getFromIndex() {
    return fromIndex;
  }

  public void setFromIndex(int fromIndex) {
    this.fromIndex = fromIndex;
  }

  public int getToIndex() {
    return toIndex;
  }

  public void setToIndex(int toIndex) {
    this.toIndex = toIndex;
  }

  /**
   * This enumeration holds type of link search
   * <pre>
   *   -1 for none
   *   0 for already have a justification link in the context
   *   1 for already have a position link in the context
   * </pre>
   *
   * @author Martin Rouffiange
   */
  public enum ESearchLinkType {

    /**
     * None
     */
    NONE(-1),
    /**
     * No justification link in context
     */
    NO_JUSTIFICATION_LINK(0),
    /**
     * No position link in context
     */
    NO_POSITION_LINK(1);

    private int id;

    private static Map<Integer, ESearchLinkType> map = new LinkedHashMap<>();

    static {
      for (ESearchLinkType type : ESearchLinkType.values()) {
        map.put(type.id, type);
      }
    }

    /**
     * Constructor
     *
     * @param id an int representing a search link type
     */
    ESearchLinkType(int id) {
      this.id = id;
    }

    /**
     * Get the enum value for a search link type
     *
     * @param id an int representing a search link type
     * @return the ESearchLinkType enum value corresponding to the given id, null otherwise.
     */
    public static ESearchLinkType value(int id) {
      return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this search link type
     */
    public int id() {
      return id;
    }

    public String toStyleName() {
      return this.name().toLowerCase();
    }
  }

  /**
   * This enumeration holds types of citation browse
   * <ul>
   *   <li>0 for browse by author name</li>
   *   <li>1 for browse by text title</li>
   *   <li>2 for browse by debate title</li>
   *   <li>3 for browse by tag name</li>
   *   <li>4 for browse by citation excerpt</li>
   *   <li>5 for browse by citation added by current contributor</li>
   * </ul>
   *
   * @author Martin Rouffiange
   */
  public enum ECitationBrowseType {

    /**
     * Browse by author name
     */
    BY_AUTHOR(0),
    /**
     * Browse by text title
     */
    BY_TEXT(1),
    /**
     * Browse by debate title
     */
    BY_DEBATE(2),
    /**
     * Browse by tag name
     */
    BY_TAG(3),
    /**
     * Browse by citation excerpt
     */
    BY_EXCERPT(4),
    /**
     * Browse by current contributor citations
     */
    BY_CONTRIBUTOR_CITATIONS(5);


    private int id;

    private static Map<Integer, ECitationBrowseType> map = new LinkedHashMap<>();

    static {
      for (ECitationBrowseType type : ECitationBrowseType.values()) {
        map.put(type.id, type);
      }
    }

    /**
     * Constructor
     *
     * @param id an int representing a type of citation browse
     */
    ECitationBrowseType(int id) {
      this.id = id;
    }

    /**
     * Get the enum value for a given id
     *
     * @param id an int representing a type of citation browse
     * @return the ECitationBrowseType enum value corresponding to the given id, null otherwise.
     */
    public static ECitationBrowseType value(int id) {
      return map.get(id);
    }

    /**
     * Get this id
     *
     * @return an int representation of this type of citation browse
     */
    public int id() {
      return id;
    }

  }

}
