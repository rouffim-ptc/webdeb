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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enumeration to hold acceptable query keys
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EQueryKey {
  /**
   * the id of the contribution
   */
  ID_CONTRIBUTION("id_contribution"),
  /**
   * the id of the contribution to ignore
   */
  ID_IGNORE("id_ignore"),
  /**
   * the id of the context contribution to ignore
   */
  CONTEXT_TO_IGNORE("id_ignore_context"),
  /**
   * the id of the text where to look
   */
  TEXT_TO_LOOK("id_look_text"),
  /**
   * type of contribution: 0 for Actor, 1 for Text, 2 for Citation
   */
  CONTRIBUTION_TYPE("type"),
  /**
   * any string value denoting an actor name
   */
  ACTOR_NAME("actor_name"),
  /**
   * type of actor: 0 for person, 1 for organization, -1 for any
   */
  ACTOR_TYPE("actor_type"),
  /**
   * any string denoting an actor function
   */
  FUNCTION("function"),
  /**
   * any string value denoting a text title
   */
  TEXT_TITLE("text_title"),
  /**
   * any string value denoting a text author
   */
  TEXT_AUTHOR("text_author"),
  /**
   * any string value denoting a text type
   */
  TEXT_TYPE("text_type"),
  /**
   * any string value denoting a source title
   */
  TEXT_SOURCE("text_source"),
  /**
   * any string value denoting an actor involved in a contribution as source author
   */
  SOURCE_AUTHOR("source_author"),
  /**
   * type of argument: 0 descriptive, 1 prescriptive, 2 opinion, 3 performative
   */
  ARGUMENT_TYPE("argument_type"),
  /**
   * argumen title
   */
  ARGUMENT_TITLE("argument_title"),
  /**
   * debate title
   */
  DEBATE_TITLE("debate_title"),
  /**
   * citation title
   */
  CITATION_TITLE("citation_title"),
  /**
   * citation author
   */
  CITATION_AUTHOR("citation_author"),
  /**
   * citation author
   */
  CITATION_SOURCE("citation_author"),
  /**
   * any string value denoting an actor involved in a contribution
   */
  ACTOR("actor"),
  /**
   * any string value denoting an actor involved as an author in a contribution
   */
  AUTHOR("author"),
  /**
   * any string value denoting an actor involved as a reporter in a contribution
   */
  REPORTER("reporter"),
  /**
   * shorthand for text_title, text_source, standard_form, actor_name, actor, function and topic keys
   */
  QUERY("query"),
  /**
   * search all contributions from a given contributor
   */
  CONTRIBUTOR("contributor"),
  /**
   * search all contributions from a given group
   */
  GROUP("group"),
  /**
   * search all contributions from all public group and a given group
   */
  AMONG_GROUP("among_group"),
  /**
   * search all contributions that are ot in given group. If this froup is webdeb public, it will be for all public groups
   */
  NOT_IN_GROUP("not_in_group"),
  /**
   * search all contributions that are validated
   */
  VALIDATED("validated"),
  /**
   * search for strictly given token, without splitting them
   */
  STRICT("strict"),
  /**
   * define lower id from which the research must be performed
   */
  FROMID("fromid"),
  /**
   * define sorting key, valid values are "id" or "name"
   */
  ORDERBY("orderby"),
  /**
   * define if only automatcially fetched contributions must be retrieved (=true), or not
   */
  FETCHED("fetched"),
  /**
   * special value used when a key does not exist
   */
  INVALID("invalid"),
  /**
   * special value used when we need all data for contributions
   */
  ALL("all"),
  /**
   * special value used when we need all contributions sorted by latest first
   */
  LATEST("latest"),
  /**
   * from a specific date
   */
  FROM_DATE("from_date"),
  /**
   * to a specific date
   */
  TO_DATE("to_date"),
  /**
   * tag name
   */
  TAG_NAME("tag_name"),
  /**
   * contribution tag
   */
  TAG("tag"),
  /**
   * contribution place
   */
  PLACE("place");

  private String id;
  private static Map<String, EQueryKey> map = new LinkedHashMap<>();

  static {
    for (EQueryKey key : EQueryKey.values()) {
      map.put(key.id, key);
    }
  }

  EQueryKey(String id) {
    this.id = id;
  }

  /**
   * Get an EQueryKey from a string value
   *
   * @param key a string key
   * @return the EQueryKey object corresponding to given key, EQueryKey.INVALID if given key does not exist
   */
  public static EQueryKey value(String key) {
    return map.getOrDefault(key, EQueryKey.INVALID);
  }

  /**
   * Get this id key
   *
   * @return an id
   */
  public String id() {
    return id;
  }
}
