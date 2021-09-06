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
 *
 */

package be.webdeb.application.query;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.ActorType;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.argument.ArgumentType;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ContributionType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EFilterKey;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.util.ValuesHelper;
import play.Configuration;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This class is used to send queries to the data base and retrieve them in a user-friendly manner.
 * Queries use (key, value) pairs to search the database.
 * Key values may be passed as key=value and pairs are separated by plus sign "+", or as a Map of SimpleEntry
 * <p>
 * The list of available keys are (keys for contributions are usable for all others):
 * <pre>
 * <p>All contribution types:
 * <ul>
 *   <li>contribution_type: 0 for Actor, 1 for Debate, 2 for Argument, 3 for ARGUMENT_CONTEXT, 4 for EXCERPT, 5 for TEXT, 6 for FOLDER</li>
 *   <li>validated: 'true' if only validated contributions must be retrieved, 'false' or unset for any</li>
 *   <li>strict: 'true' if all values passed must be treated as strict matches (ex: 'Louis' won't return back 'Louise')</li>
 *   <li>fromid: any integer value defining the lower id from which the contributions must be retrieved</li>
 *   <li>orderby: either "name" or "id" to define the sorting key for the results, default is name.</li>
 *   <li>fetched: 'true' if only automatically fetched contributions must be retrieved (ie, contributions imported from other sources without validation)</li>
 * </ul>
 * </p>
 * <p>For actors:
 * <ul>
 *   <li>actor_name: any string value denoting an actor name</li>
 *   <li>actor_type: 0 for person, 1 for organization, -1 for any</li>
 *   <li>function: the actor function</li>
 * </ul>
 * </p>
 * <p>For texts:
 *   <ul>
 *     <li>text_title: any string value denoting a text title</li>
 *     <li>text_source: any string value denoting a source title</li>
 *     <li>source_author: any string value denoting an actor involved in a contribution as source author</li>
 *   </ul></p>
 * <p>For arguments:
 *   <ul>
 *     <li>argument_title: the title of the argument</li>
 *     <li>argument_type: 0 descriptive, 1 prescriptive, 2 opinion, 3 performative</li>
 *   </ul></p>
 * <p>For debates:
 *  <ul>
 *    <li>debate_title: the debate title</li>
 *  </ul></p>
 * <p>For excerpts:
 *   <ul>
 *     <li>excerpt_title: the original citation</li>
 *   </ul></p>
 * <p>For excerpts and texts (integer values are considered as ids):
 *   <ul>
 *     <li>actor: any string value denoting an actor involved in a contribution</li>
 *     <li>author: any string value denoting an actor involved as an author in a contribution</li>
 *     <li>reporter:  any string value denoting an actor involved as a reporter in a contribution</li>
 *   </ul></p>
 * <p>For folders:
 *   <ul>
 *     <li>folder_name: the name of the tag</li>
 *   </ul></p>
 * </pre>
 * <p>
 * It is also possible to use the query key as a shorthand for text_title, text_source, argument_title, excerpt_title
 * , actor_name, actor, function and tag names.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class QueryExecutor {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private TextFactory textFactory;
  private ValuesHelper values;
  private Configuration configuration;

  private final List<ArgumentType> argumentTypes;
  private final List<ContributionType> contributionTypes;
  private final List<ActorType> actorTypes;

  // string constant for logs
  private static final String UNKNOWN_VALUE = "unknown value given for key ";
  private static final String VALUE_WAS = " value was ";

  /**
   * Construct a query executor to interrogate the knowledge base
   *
   * @param textFactory a text factory to build API texts
   * @param argumentFactory an argument factory to build API arguments
   * @param actorFactory an actor factory to build API actors
   * @param values a values helper object to check passed query values
   */
  @Inject
  public QueryExecutor(TextFactory textFactory, ArgumentFactory argumentFactory, ActorFactory actorFactory,
                       ValuesHelper values, Configuration configuration) {
    this.textFactory = textFactory;
    this.values = values;
    this.configuration = configuration;
    argumentTypes = argumentFactory.getArgumentTypes();
    contributionTypes = argumentFactory.getContributionTypes();
    actorTypes = actorFactory.getActorTypes();
  }

  /**
   * Search for any contribution using given criteria. Criteria are of the form key=value.
   * Multiple values for the same key can be given. Acceptable criteria are documented on top of this class.
   *
   * @param criteria a list of key-value pairs to search for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of ContributionHolder containing arguments that matches the given criteria
   *
   * @throws BadQueryException if any given key is unknown or given value is invalid
   */
  @SuppressWarnings("fallthrough")
  public List<Contribution> searchContributions(List<Map.Entry<EQueryKey, String>> criteria, int fromIndex, int toIndex) throws BadQueryException {
    return searchContributions(criteria, fromIndex, toIndex, null);
  }

  /**
   * Search for any contribution using given criteria. Criteria are of the form key=value.
   * Multiple values for the same key can be given. Acceptable criteria are documented on top of this class.
   *
   * @param criteria a list of key-value pairs to search for
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return a list of ContributionHolder containing arguments that matches the given criteria
   *
   * @throws BadQueryException if any given key is unknown or given value is invalid
   */
  @SuppressWarnings("fallthrough")
  public List<Contribution> searchContributions(List<Map.Entry<EQueryKey, String>> criteria, int fromIndex, int toIndex, String filters) throws BadQueryException {
    logger.debug("execute query with keys " + criteria.toString());
    boolean strict = false;

    fillCriteriaMapWithFilters(criteria, filters);

    for (Map.Entry<EQueryKey, String> criterion : criteria) {
      switch (criterion.getKey()) {
        // arguments
        case ARGUMENT_TYPE:
          if (!values.isNumeric(criterion.getValue())
              || argumentTypes.stream().noneMatch(a -> a.getType() == Integer.parseInt(criterion.getValue()))) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case ID_CONTRIBUTION:
          if (!values.isNumeric(criterion.getValue())) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case ID_IGNORE:
          if (!values.isNumeric(criterion.getValue(), 0, false)) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case CONTEXT_TO_IGNORE:
          if (!values.isNumeric(criterion.getValue(), 0, false)) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case TEXT_TO_LOOK:
          if (!values.isNumeric(criterion.getValue(), 0, false)) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case CONTRIBUTION_TYPE:
          if (!values.isNumeric(criterion.getValue())
              || contributionTypes.stream().noneMatch(c -> c.getType() == Integer.parseInt(criterion.getValue()))) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case ACTOR_TYPE:
          if (!values.isNumeric(criterion.getValue())
              || actorTypes.stream().noneMatch(a -> a.getType() == Integer.parseInt(criterion.getValue()))) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case VALIDATED:
        case FETCHED:
          if (!values.isBoolean(criterion.getValue())) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;
        case FROMID:
          if (!values.isNumeric(criterion.getValue())) {
            logger.warn(UNKNOWN_VALUE + criterion.getKey() + VALUE_WAS + criterion.getValue());
            throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
          }
          break;

        // common texts and arguments' keys
        case ACTOR:
        case AUTHOR:
        case REPORTER:
          // arguments' keys
        case ARGUMENT_TITLE:
          // citation' keys
        case CITATION_TITLE:
        case CITATION_AUTHOR:
        case CITATION_SOURCE:
          // texts' keys
        case TEXT_TITLE:
        case TEXT_AUTHOR:
        case DEBATE_TITLE:
        case TEXT_TYPE:
        case TEXT_SOURCE:
          // tags' keys
        case TAG_NAME:
          // other actors' keys
        case ACTOR_NAME:
        case FUNCTION:
          // contributor
        case CONTRIBUTOR:
          // current group
        case GROUP:
        case AMONG_GROUP:
        case ALL:
        case LATEST:
          // sorting key
        case ORDERBY:
        case TAG:
        case PLACE:
        case FROM_DATE:
        case TO_DATE:
          break;
        // strict search
        case STRICT:
          if(values.isBoolean(criterion.getValue())) {
            strict = Boolean.valueOf(criterion.getValue());
          }
          break;
        default:
          logger.warn("unknown key or unhandled here " + criterion.getKey());
          throw new BadQueryException(criteria.toString(), criterion.getKey().id(), criterion.getValue());
      }
    }

    // any factory will execute the same method
    return textFactory.findByCriteria(criteria, strict, fromIndex, toIndex);
  }

  private void fillCriteriaMapWithFilters(List<Map.Entry<EQueryKey, String>> criteria, String filters) {
    if(filters != null) {
      try {
        filters = URLDecoder.decode(filters, StandardCharsets.UTF_8.toString());
      } catch (UnsupportedEncodingException e) {
      }

      for (String filter : filters.split(";")) {
        String[] keyValFilter = filter.split(":");

        if (keyValFilter.length == 2) {
          EFilterKey key = EFilterKey.value(keyValFilter[0]);

          if (key != null) {
            String[] values = keyValFilter[1].split(",");

            switch (key) {
              case SEARCH_DATE:
                if(!this.values.isBlank(values[0]))
                  criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.FROM_DATE, values[0]));
                if(!this.values.isBlank(values[1]))
                  criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TO_DATE, values[1]));
                break;
              case ACTOR_TYPE:
                criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ACTOR_TYPE, values[0]));
                break;
              case TEXT_AUTHOR:
                criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_AUTHOR, values[0]));
                break;
              case TEXT_SOURCE:
                criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_SOURCE, values[0]));
                break;
              case PLACE:
                criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.PLACE, values[0]));
                break;
              case TAG:
                criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TAG, values[0]));
                break;
            }
          }
        }
      }
    }
  }

  /**
   * Parse a given query string and transform it to a list of pairs that may be processed by the executor
   *
   * @param query a string query of the form key=value+ ... +key=value
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of ContributionHolder containing arguments that matches the given criteria
   *
   * @throws BadQueryException if any given key is unknown or given value is invalid
   */
  @SuppressWarnings("fallthrough")
  public List<Contribution> searchContributions(String query, int fromIndex, int toIndex) throws BadQueryException {
    return searchContributions(query, fromIndex, toIndex, null);
  }

  /**
   * Parse a given query string and transform it to a list of pairs that may be processed by the executor
   *
   * @param query a string query of the form key=value+ ... +key=value
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param filters a string that contains data for filter results
   * @return a list of ContributionHolder containing arguments that matches the given criteria
   *
   * @throws BadQueryException if any given key is unknown or given value is invalid
   */
  @SuppressWarnings("fallthrough")
  public List<Contribution> searchContributions(String query, int fromIndex, int toIndex, String filters) throws BadQueryException {
    // build filters
    logger.debug("execute query " + query);
    List<Map.Entry<EQueryKey, String>> criteria = new ArrayList<>();

    for (String criterion : query.split("\\+")) {
      if (!criterion.contains("=")) {
        // don't handle this criterion since it is badly formed
        logger.warn("badly formed criterion " + criterion);
        continue;
      }
      String[] pair = criterion.split("=");
      switch (EQueryKey.value(pair[0])) {
        case ID_CONTRIBUTION:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ID_CONTRIBUTION, pair[1]));
          break;
        case ID_IGNORE:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ID_IGNORE, pair[1]));
          break;
        case CONTRIBUTOR:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTOR, pair[1]));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.ACTOR.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.DEBATE.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.TEXT.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.CITATION.id()+""));
          break;
        case GROUP:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.GROUP, pair[1]));
          break;
        case AMONG_GROUP:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.AMONG_GROUP, pair[1]));
          break;
        case VALIDATED:
        case FETCHED:
          if (values.isBoolean(pair[1])) {
            criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          } else {
            logger.warn(UNKNOWN_VALUE + pair[0] + VALUE_WAS + pair[1]);
            throw new BadQueryException(query, pair[0], pair[1]);
          }
          break;
        case QUERY:
          // shorthand to make simple queries
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_TITLE, pair[1]));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TAG_NAME, pair[1]));
          //criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.TEXT_SOURCE, pair[1]));
          //criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ARGUMENT_TITLE, pair[1]));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.DEBATE_TITLE, pair[1]));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CITATION_TITLE, pair[1]));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ACTOR_NAME, pair[1]));
          //criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.ACTOR, pair[1]));
          //criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.FUNCTION, pair[1]));
          break;

        case ARGUMENT_TYPE:
          if (values.isNumeric(pair[1])
              && argumentTypes.stream().anyMatch(a -> a.getType() == Integer.parseInt(pair[1]))) {
            criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          } else {
            logger.warn(UNKNOWN_VALUE + pair[0] + VALUE_WAS + pair[1]);
            throw new BadQueryException(query, pair[0], pair[1]);
          }
          break;
        case ALL :
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.ACTOR.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.ARGUMENT.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.DEBATE.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.TEXT.id()+""));
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, EContributionType.TAG.id()+""));
          break;
        case CONTRIBUTION_TYPE:
          if (values.isNumeric(pair[1])
              && contributionTypes.stream().anyMatch(a -> a.getType() == Integer.parseInt(pair[1]))) {
            criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          } else {
            logger.warn(UNKNOWN_VALUE + pair[0] + VALUE_WAS + pair[1]);
            throw new BadQueryException(query, pair[0], pair[1]);
          }
          break;
        case ACTOR_TYPE:
          if (values.isNumeric(pair[1])
              && actorTypes.stream().anyMatch(a -> a.getType() == Integer.parseInt(pair[1]))) {
            criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          } else {
            logger.warn(UNKNOWN_VALUE + pair[0] + VALUE_WAS + pair[1]);
            throw new BadQueryException(query, pair[0], pair[1]);
          }
          break;
        case FROMID:
          if (values.isNumeric(pair[1])) {
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          } else {
            logger.warn(UNKNOWN_VALUE + pair[0] + VALUE_WAS + pair[1]);
            throw new BadQueryException(query, pair[0], pair[1]);
          }
          break;

          // arguments
        case ARGUMENT_TITLE:
          // citations
        case CITATION_TITLE:
        case CITATION_AUTHOR:
        case CITATION_SOURCE:
          // texts
        case TEXT_TITLE:
        case TEXT_AUTHOR:
        case TEXT_SOURCE:
          // texts and arguments
        case ACTOR:
        case AUTHOR:
        case REPORTER:
          // actors
        case ACTOR_NAME:
          // tags
        case TAG_NAME:
        case FUNCTION:
          // strict search
        case STRICT:
        case LATEST:
        case ORDERBY:
          criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.value(pair[0]), pair[1]));
          break;
        default:
          logger.warn("unknown key given " + pair[0]);
          throw new BadQueryException(query, pair[0]);
      }
    }
    return searchContributions(criteria, fromIndex, toIndex, filters);
  }
}
