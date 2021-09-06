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

import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.browse.search;
import be.webdeb.presentation.web.views.html.browse.searchResult;
import be.webdeb.presentation.web.views.html.browse.searchResultScroller;
import play.Configuration;
import play.data.Form;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


/**
 * This class controls all pages related to searching facilities
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class BrowseActions extends CommonController {

  @Inject
  private Configuration configuration;

  /**
   * Show the search page with popular contributions
   *
   * @return the search page
   */
  public CompletionStage<Result> search() {
    logger.debug("GET search page");

    SearchForm query = new SearchForm();
    query.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    query.setIsActor(true);
    query.setIsArgument(true);
    query.setIsText(true);
    query.setIsTag(true);
    query.setIsDebate(true);
    query.setIsCitation(true);
    Form<SearchForm> form = formFactory.form(SearchForm.class).fill(query);

    WebdebUser user = sessionHelper.getUser(ctx());
    return CompletableFuture.supplyAsync(() ->
        ok(search.render(form, query.getConcernedType(), helper, new ArrayList<>(), user, null)),
        context.current());
  }

  /**
   * Redirect to search page and fill request from the navbar form or any other direct REST call.
   * Search request will be sent via ajax call when rendering search template, calling the
   * {@link #doSearch search} action.
   *
   * @param query a search query (see QueryExecutor and RESTAccessor)
   * @return the search page with a pre-filled query object
   */
  public CompletionStage<Result> redirectSearch(String query) {
    logger.debug("do search with query " + query);
    WebdebUser user = sessionHelper.getUser(ctx());
    SearchForm squery = new SearchForm(query, user.getGroup().getGroupId());
    return CompletableFuture.supplyAsync(() ->
          ok(search.render(formFactory.form(SearchForm.class).fill(squery), squery.getConcernedType(), helper, new ArrayList<>(), user, null)), context.current());
  }

  /**
   * Execute a search request from the web page. Sends back content to be put in dedicated location (partial page)
   *
   * @param card boolean telling if the contribution must be rendered (true) or the simple overview (false)
   * @param embedded boolean telling if the search is done in an embedded frame
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return the result content from the search request
   */
  public CompletionStage<Result> doSearch(boolean card, boolean embedded, int fromIndex, int toIndex, String filters) {
    Form<SearchForm> form = formFactory.form(SearchForm.class).bindFromRequest();
    logger.debug("GET " + (embedded ? "embedded " : "") +  "search results " + form.data());
    if (form.hasErrors()) {
      logger.error("search form errors " + form.errors().toString());
      return CompletableFuture.supplyAsync(() ->
          badRequest(searchResult.render("", new ArrayList<>(), values, card, !embedded, null, false, false, null, null)), context.current());
    }

    SearchForm query = form.get();
    // set current group, if not default one
    if(query.getAmongGroup() == -1)
      query.setInGroup(sessionHelper.getCurrentGroup(ctx()));
    executeQuery(query, sessionHelper.getUser(ctx()).getId(), fromIndex, toIndex, filters);

    logger.debug("sends result back for query " + query.toString());
    return sendOk(searchResultScroller.render(query.getResult(), query.getQueryString(), values, card, !embedded,false, EOverviewType.SIMPLE));
  }

  /**
   * Fill query object with result of query
   *
   * @param query a query object to be executed
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex   the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   */
  private void executeQuery(SearchForm query, Long contributor, int fromIndex, int toIndex, String filters) {
    logger.debug("do search with query " + query.toString());
    fromIndex = fromIndex == -1 ? configuration.getInt("search.contribution.min") : fromIndex;
    toIndex = toIndex == -1 ? configuration.getInt("search.contribution.max") : toIndex;

    WebdebUser user = sessionHelper.getUser(ctx());
    final int minLength = 2;

    try {
      // if we have a queryString set, this means we come from a redirection, so we have to simply execute it
      // the queryString will be cleaned up in search form as soon as the result is shown to user.
      if ("".equals(query.getQueryString())) {
        List<String> queryString = new ArrayList<>();
        // query with too short words removed (for topics, functions and free-text)
        String prunedQuery = String.join(" ",
            Arrays.stream(query.getQuery().split(" ")).filter(s -> s.length() >= minLength).collect(Collectors.toList()));

        query.setResult(new LinkedHashSet<>());
        Set<SimpleEntry<EQueryKey, String>> criteria = new HashSet<>();

        //set contribution id
        if(query.getIdContribution() != -1)
          criteria.add(new SimpleEntry<>(EQueryKey.ID_CONTRIBUTION, String.valueOf(query.getIdContribution())));

        //set contributor id
        if(query.getContributor() != -1)
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTOR, String.valueOf(query.getContributor())));

        //set contribution id to ignore
        if(query.getIgnore() != -1)
          criteria.add(new SimpleEntry<>(EQueryKey.ID_IGNORE, String.valueOf(query.getIgnore())));

        //set context contribution id to ignore
        if(query.getContextToIgnore() != -1)
            criteria.add(new SimpleEntry<>(EQueryKey.CONTEXT_TO_IGNORE, String.valueOf(query.getContextToIgnore())));

        //set context contribution id where to look
        if(query.getTextToLook() != -1)
          criteria.add(new SimpleEntry<>(EQueryKey.TEXT_TO_LOOK, String.valueOf(query.getTextToLook())));

        // set current group id
        criteria.add(new SimpleEntry<>(EQueryKey.GROUP, String.valueOf(query.getInGroup())));
        if(query.getAmongGroup() != -1)
          criteria.add(new SimpleEntry<>(EQueryKey.AMONG_GROUP, String.valueOf(query.getAmongGroup())));


        if(!values.isBlank(query.getQuery()))
            queryString.add(EQueryKey.QUERY.id() + "=" + query.getQuery());

        if(query.isAllContributions())
            queryString.add(EQueryKey.ALL.id() + "=" + true);


        // transform form switches to (key, value) pairs for search executor
        // arguments
        if (query.getIsArgument()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.ARGUMENT.id())));
          //if(!query.isAllContributions())
            //queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.ARGUMENT.id());
          if (!"".equals(prunedQuery)) {
            criteria.add(new SimpleEntry<>(EQueryKey.ARGUMENT_TITLE, query.getQuery()));
          }
        }

        // debates
        if (query.getIsDebate()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.DEBATE.id())));
          if(!query.isAllContributions())
            queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.DEBATE.id());
          criteria.add(new SimpleEntry<>(EQueryKey.DEBATE_TITLE, query.getQuery()));
        }

        // citations
        if (query.getIsCitation()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.CITATION.id())));
          if(!query.isAllContributions())
            queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.CITATION.id());
          criteria.add(new SimpleEntry<>(EQueryKey.CITATION_TITLE, query.getQuery()));

          if(!values.isBlank(query.getCitationAuthor()))
            criteria.add(new SimpleEntry<>(EQueryKey.CITATION_AUTHOR, String.valueOf(query.getCitationAuthor())));
          if(!values.isBlank(query.getCitationSource()))
            criteria.add(new SimpleEntry<>(EQueryKey.CITATION_SOURCE, String.valueOf(query.getCitationSource())));
        }

        // texts
        if (query.getIsText()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.TEXT.id())));
          if(!query.isAllContributions())
            queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.TEXT.id());

          criteria.add(new SimpleEntry<>(EQueryKey.TEXT_TITLE, query.getQuery()));

          if(!values.isBlank(query.getTextAuthor()))
            criteria.add(new SimpleEntry<>(EQueryKey.TEXT_AUTHOR, String.valueOf(query.getTextAuthor())));
          if(!values.isBlank(query.getTextSource()))
            criteria.add(new SimpleEntry<>(EQueryKey.TEXT_SOURCE, String.valueOf(query.getTextSource())));
        }

        // actors
        if (query.getIsActor()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.ACTOR.id())));
          if(!query.isAllContributions())
            queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.ACTOR.id());
          criteria.add(new SimpleEntry<>(EQueryKey.ACTOR_NAME, query.getQuery()));
          //queryString.add(EQueryKey.ACTOR_NAME.id() + "=" + query.getQuery());
          if (!"".equals(prunedQuery)) {
            //criteria.add(new SimpleEntry<>(EQueryKey.FUNCTION, prunedQuery));
            //queryString.add(EQueryKey.FUNCTION.id() + "=" + prunedQuery);
          }
        }

        // tags
        if (query.getIsTag()) {
          criteria.add(new SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, String.valueOf(EContributionType.TAG.id())));
          if(!query.isAllContributions())
            queryString.add(EQueryKey.CONTRIBUTION_TYPE.id() + "=" + EContributionType.TAG.id());

          criteria.add(new SimpleEntry<>(EQueryKey.TAG_NAME, query.getQuery()));
          //queryString.add(EQueryKey.TAG_NAME.id() + "=" + query.getQuery());
        }

        if ((query.getIsCitation() || query.getIsText()) && query.getTextToLook() == -1L) {
          // set also by involved actor key
          criteria.add(new SimpleEntry<>(EQueryKey.ACTOR, query.getQuery()));
          //queryString.add(EQueryKey.ACTOR.id() + "=" + query.getQuery());
        }

        if (query.getIsStrict()) {
          criteria.add(new SimpleEntry<>(EQueryKey.STRICT, "true"));
          queryString.add(EQueryKey.STRICT.id() + "=" + "true");
        }
        query.setQueryString(String.join("+", queryString));
        query.getResult().addAll(helper.toHolders(executor.searchContributions(
                new ArrayList<>(criteria),
                fromIndex,
                toIndex,
                filters)
                , user, ctx().lang().code()));

      } else {
          // we have a queryString to execute as is
        query.getResult().addAll(helper.toHolders(executor.searchContributions(
                query.getCompleteQueryString(),
                fromIndex,
                toIndex,
                filters)
                , user, ctx().lang().code()));
      }

    } catch (BadQueryException e) {
      logger.warn("unable to run query " + query.getQueryString(), e);
      query.setResult(new ArrayList<>());
    }
  }
}
