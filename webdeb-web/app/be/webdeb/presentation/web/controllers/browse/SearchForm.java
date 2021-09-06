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

package be.webdeb.presentation.web.controllers.browse;

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Query form (browse page)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class SearchForm {

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  // the query string
  private Long idContribution = -1L;
  private Long ignore = -1L;
  private Long contextToIgnore = -1L;
  private Long textToLook = -1L;
  private Long contributor = -1L;
  private String query = "";
  private String queryString = "";
  private int inGroup = -1;
  private int amongGroup = -1;
  private boolean latest = false;

  private String textSource = "";
  private String citationSource = "";
  private String textAuthor = "";
  private String citationAuthor = "";

  // type of contributions
  private boolean isArgument = false;
  private boolean isDebate = false;
  private boolean isCitation = false;
  private boolean isText = false;
  private boolean isActor = false;
  private boolean isTag = false;
  private boolean isAll = false;

  // boolean saying if the search must be restrictive, ie, if multiple token are passed, they may not be decoupled
  private boolean isStrict = false;

  protected Collection<ContributionHolder> result = new ArrayList<>();

  /**
   * Default constructor.
   */
  public SearchForm() {
    // do nothing (default constructor is needed for proper json handling by play)
  }

  /**
   * Constructor from a contribution type
   *
   * @param contributionType a contribution type
   * @see EContributionType
   */
  public SearchForm(EContributionType contributionType) {
    switch(contributionType){
      case ALL:
        isDebate = true;
        isCitation = true;
        isText = true;
        isActor = true;
        isTag = true;
        break;
      case TEXT:
        isText = true;
        break;
      case ACTOR:
        isActor = true;
        break;
      case ARGUMENT:
        isArgument = true;
        break;
      case CITATION:
        isCitation = true;
        break;
      case DEBATE:
        isDebate = true;
        break;
      case TAG:
        isTag = true;
        break;
    }
  }

  /**
   * Constructor. Build a query object from given query string
   *
   * @param query a query string (as valid for the Executor)
   * @param inGroup the current group, if not passed in query string
   */
  public SearchForm(String query, int inGroup) {
    logger.debug(query+"//");
    for (String criterion : query.split("\\+")) {
      String[] pair = criterion.split("=");

      switch (EQueryKey.value(pair[0])) {
        case LATEST:
          this.latest = true;
          break;
        case CONTRIBUTOR:
          // shorthand to get all contributions of a contributor
          this.query = ""; // do not show anything in query
          setAllContributionsFlags(true);
          if (pair.length > 1 && values.isNumeric(pair[1])) {
            this.contributor = Long.parseLong(pair[1]);
          }
          break;
        case GROUP:
          // shorthand to get all contributions of a group
          if (pair.length > 1 && values.isNumeric(pair[1])) {
            this.inGroup = Integer.parseInt(pair[1]);
          }
          break;
        case AMONG_GROUP:
          // shorthand to get all contributions of a group
          if (pair.length > 1 && values.isNumeric(pair[1])) {
            this.amongGroup = Integer.parseInt(pair[1]);
          }
          break;
        case QUERY:
          // shorthand to make simple queries
          this.query = pair.length > 1 ? pair[1] : null;
          setAllContributionsFlags(true);
          break;
        case CONTRIBUTION_TYPE:
          if (pair.length > 1 && values.isNumeric(pair[1])) {
            switch (EContributionType.value(Integer.parseInt(pair[1]))) {
              case ACTOR:
                isActor = true;
                break;
              case TEXT:
                isText = true;
                break;
              case ARGUMENT:
                isArgument = true;
                break;
              case TAG:
                isTag = true;
                break;
              case DEBATE:
                isDebate = true;
                break;
              case CITATION:
                isCitation = true;
                break;
              default:
                logger.warn("wrong contribution type id passed " + pair[1]);
                break;
            }
          } else {
            logger.warn("wrong data type for contribution type passed " + pair[1]);
          }
          break;
        case ALL:
          setAllContributionsFlags(true);
          break;
        case ID_CONTRIBUTION:
          // shorthand to make simple queries
          try {
            this.idContribution = pair.length > 1 ? Long.parseLong(pair[1], 10) : null;
          }catch(NumberFormatException e){
            logger.debug(pair[1] + " is not a number");
          }
          break;
          // arguments
        case ARGUMENT_TITLE:
          isArgument = true;
          this.query = pair.length > 1 ? pair[1] : null;
          break;
        // citations
        case CITATION_TITLE:
          isCitation = true;
          this.query = pair.length > 1 ? pair[1] : null;
          break;
        case CITATION_AUTHOR:
          this.citationAuthor = pair.length > 1 ? pair[1] : null;
          break;
        case CITATION_SOURCE:
          this.citationSource = pair.length > 1 ? pair[1] : null;
          break;
          // texts
        case TEXT_TITLE:
          isText = true;
          this.query = pair.length > 1 ? pair[1] : null;
          break;
        case TEXT_AUTHOR:
          this.textAuthor = pair.length > 1 ? pair[1] : null;
          break;
        case TEXT_SOURCE:
          this.textSource = pair.length > 1 ? pair[1] : null;
          break;
        // tags
        case TAG_NAME:
          isTag = true;
          this.query = pair.length > 1 ? pair[1] : null;
          break;
          // texts and arguments

        case ACTOR:
        case AUTHOR:
        case REPORTER:
        case SOURCE_AUTHOR:
          this.query = pair.length > 1 ? pair[1] : null;
          break;

          // actors
        case ACTOR_NAME:
          isActor = true;
          this.query = pair.length > 1 ? pair[1] : null;
          break;
        case STRICT:
          isStrict = true;
          break;
        default:
          logger.warn("unknown key given " + pair[0]);
      }
    }

    if(!isActor && !isText && !isDebate && !isTag && !isCitation && !isArgument) {
      setAllContributionsFlags(true);
    }

    // finalize query string
    queryString = query;
    if (this.inGroup == -1) {
      this.inGroup = inGroup;
    }
  }

  /*
   * GETTERS / SETTERS
   */

  public boolean isAllContributions() {
    return isActor && isText && isDebate && isTag && isCitation;
  }

  public void setAllContributionsFlags(boolean isContribution) {
    isAll = isActor = isText = isDebate = isTag = isCitation = isContribution;
  }

  /**
   * Get the contribution id to browse first
   *
   * @return a contribution id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set the search query as entered by user
   *
   * @param idContribution a contribution id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  public Long getContributor() {
    return contributor;
  }

  public void setContributor(Long contributor) {
    this.contributor = contributor;
  }

  /**
   * Get the contribution id to ignore if any
   *
   * @return a contribution id to ignore
   */
  public Long getIgnore() {
    return ignore;
  }

  /**
   * Set the contribution id to ignore
   *
   * @param ignore a contribution id to ignore
   */
  public void setIgnore(Long ignore) {
    this.ignore = ignore;
  }

  /**
   * Get the context contribution id to ignore if any
   *
   * @return a context contribution id to ignore
   */
  public Long getContextToIgnore() {
    return contextToIgnore ;
  }

  /**
   * Set the context contribution id to ignore
   *
   * @param ignore a context contribution id to ignore
   */
  public void setContextToIgnore(Long ignore) {
    this.contextToIgnore = ignore;
  }

  /**
   * Get the text id where to look, if any
   *
   * @return a text id where to look
   */
  public Long getTextToLook() {
    return textToLook;
  }

  /**
   * Set the text id where to look
   *
   * @param toLook a text id where to look
   */
  public void setTextToLook(Long toLook) {
    this.textToLook = toLook;
  }

  /**
   * Get the search query only (what user entered in input field)
   *
   * @return a query
   */
  public String getQuery() {
    return query;
  }

  /**
   * Set the search query as entered by user
   *
   * @param query a query
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * Get the full string query of "key=value" pairs separated by "+" signs, built from user query and filters
   *
   * All keys should comply to EQueryKey values
   *
   * @return a query
   */
  public String getQueryString() {
    return queryString;
  }

  /**
   * Get the search query only (what user entered in input field) and the group where perform the search
   *
   * @return a query
   */
  public String getCompleteQueryString() {
    return queryString + (amongGroup != -1 ? "+" + EQueryKey.AMONG_GROUP.id() + "=" + amongGroup :
            (inGroup != -1 ? "+" + EQueryKey.GROUP.id() + "=" + inGroup : ""));
  }

  /**
   * Set the full string query of "key=value" pairs separated by "+" signs, built from query and filters
   *
   * All keys should comply to EQueryKey values.
   *
   * @param queryString a query
   */
  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  /**
   * Get group id, if any (aka, current scope)
   *
   * @return a group id
   */
  public int getInGroup() {
    return inGroup;
  }

  /**
   * Set group id (of current scope)
   *
   * @param inGroup a group id
   */
  public void setInGroup(int inGroup) {
    this.inGroup = inGroup;
  }

  /**
   * Get group id to browse among it and public groups, if any
   *
   * @return a group id or null
   */
  public int getAmongGroup() {
    return amongGroup;
  }

  /**
   * Set group id to browse among it and public groups (of current scope)
   *
   * @param group a group id
   */
  public void setAmongGroup(int group) {
    this.amongGroup = group;
  }

  /**
   * Get the "search for argument" flag
   *
   * @return true if the argument filter is "on"
   */
  public boolean getIsArgument() {
    return isArgument;
  }

  /**
   * Set the "search for argument" flag
   *
   * @param isArgument true if the argument filter is "on"
   */
  public void setIsArgument(boolean isArgument) {
    this.isArgument = isArgument;
  }

  /**
   * Get the "search for text" flag
   *
   * @return true if the text filter is "on"
   */
  public boolean getIsText() {
    return isText;
  }

  /**
   * Set the "search for text" flag
   *
   * @param isText true if the text filter is "on"
   */
  public void setIsText(boolean isText) {
    this.isText = isText;
  }

  /**
   * Get the "search for actor" flag
   *
   * @return true if the actor filter is "on"
   */
  public boolean getIsActor() {
    return isActor;
  }

  /**
   * Set the "search for actor" flag
   *
   * @param isActor true if the actor filter is "on"
   */
  public void setIsActor(boolean isActor) {
    this.isActor = isActor;
  }

  /**
   * Get the "search for tag" flag
   *
   * @return true if the tag filter is "on"
   */
  public boolean getIsTag() {
    return isTag;
  }

  /**
   * Set the "search for tag" flag
   *
   * @param isTag true if the tag filter is "on"
   */
  public void setIsTag(boolean isTag) {
    this.isTag = isTag;
  }

  /**
   * Get the "search for citation" flag
   *
   * @return true if the citation filter is "on"
   */
  public boolean getIsCitation() {
    return isCitation;
  }

  /**
   * Set the "search for citation" flag
   *
   * @param isCitation true if the citation filter is "on"
   */
  public void setIsCitation(boolean isCitation) {
    this.isCitation = isCitation;
  }

  /**
   * Get the "search for debate" flag
   *
   * @return true if the debate filter is "on"
   */
  public boolean getIsDebate() {
    return isDebate;
  }

  /**
   * Set the "search for debate" flag
   *
   * @param isDebate true if the debate filter is "on"
   */
  public void setIsDebate(boolean isDebate) {
    this.isDebate = isDebate;
  }

  /**
   * Check whether a strict search must be performed, ie, all tokens must be treated as is without splitting them
   *
   * @return true if a strict search must be performed
   */
  public boolean getIsStrict() {
    return isStrict;
  }

  /**
   * Set whether a strict search must be performed, ie, all tokens must be treated as is without splitting them
   *
   * @param strict true if a strict search must be performed
   */
  public void setIsStrict(boolean strict) {
    isStrict = strict;
  }

  public boolean isLatest() {
    return latest;
  }

  public void setLatest(boolean latest) {
    this.latest = latest;
  }

  public boolean getIsAll() {
    return isAll;
  }

  public void setIsAll(boolean all) {
    isAll = all;
  }

  /**
   * Get the collection of contributions matching the search query and filters
   *
   * @return a collection of contributions
   */
  public Collection<ContributionHolder> getResult() {
    return result;
  }

  /**
   * Set the collection of contributions matching the search query and filters
   *
   * @param result a collection of contributions that matches the query and filters set in this object
   */
  public void setResult(Collection<ContributionHolder> result) {
    this.result = result;
  }

  public String getTextSource() {
    return textSource;
  }

  public void setTextSource(String textSource) {
    this.textSource = textSource;
  }

  public String getCitationSource() {
    return citationSource;
  }

  public void setCitationSource(String citationSource) {
    this.citationSource = citationSource;
  }

  public String getTextAuthor() {
    return textAuthor;
  }

  public void setTextAuthor(String textAuthor) {
    this.textAuthor = textAuthor;
  }

  public String getCitationAuthor() {
    return citationAuthor;
  }

  public void setCitationAuthor(String citationAuthor) {
    this.citationAuthor = citationAuthor;
  }

  public EContributionType getConcernedType() {

    if(isActor && !isTag && !isText && !isCitation && !isDebate) {
      return EContributionType.ACTOR;
    } else if(!isActor && isTag && !isText && !isCitation && !isDebate) {
      return EContributionType.TAG;
    } else if(!isActor && !isTag && isText && !isCitation && !isDebate) {
      return EContributionType.TEXT;
    } else if(!isActor && !isTag && !isText && isCitation && !isDebate) {
      return EContributionType.CITATION;
    } else if(!isActor && !isTag && !isText && !isCitation && isDebate) {
      return EContributionType.DEBATE;
    }

    return EContributionType.ALL;
  }

  @Override
  public String toString() {
    return getQuery() + " text:" + isText + " actor:" + isActor + " argument:" + isArgument  + " tag:" + isTag +
            " debate:" + isDebate + " citation:" + isCitation + " in group: " + inGroup + " strict:" + isStrict;
  }
}
