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

package be.webdeb.infra.ws.nlp;

import be.webdeb.core.api.actor.EActorType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class is meant to be json-serialized for requests to the WDTAL wikidata service.
 * Either a query object (with name) or the url must be passed, not both.
 *
 * See available constructors for more details.
 *
 * @author Fabian Gilson
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WikidataExtractRequest {

  /**
   * type of actor (either "organization" or "person")
   */
  @JsonSerialize
  @JsonProperty("actor_type")
  private String actorType;

  /**
   * ISO 639-1 language code
   */
  @JsonSerialize
  private String language;

  /**
   * the query object with the name of the actor to retrieve
   */
  @JsonSerialize
  private QueryObject query;

  /**
   * the wikipedia url to query
   */
  @JsonSerialize
  private String url;

  /**
   * Constructor for both types of actors to serach on an url
   *
   * @param language the 2-char iso-639-1 code
   * @param url an url
   * @param type the type of to look for
   */
  public WikidataExtractRequest(String language, String url, EActorType type) {
    this.language = language;
    this.url = url.trim();
    this.actorType = type.name().toLowerCase();
  }

  /**
   * Constructor for persons to search on their first and last names
   *
   * @param language the 2-char iso-639-1 code
   * @param first the person's first name
   * @param last the person's last name
   */
  public WikidataExtractRequest(String language, String first, String last) {
    this.language = language;
    this.actorType = EActorType.PERSON.name().toLowerCase();
    this.query = new QueryObject(actorType, last, first);
  }

  /**
   * Constructor for organizations to search on their names
   *
   * @param language the 2-char iso-639-1 code
   * @param name the organization's name
   */
  public WikidataExtractRequest(String language, String name) {
    this.language = language;
    this.actorType = EActorType.ORGANIZATION.name().toLowerCase();
    this.query = new QueryObject(actorType, name, null);
  }

  /**
   * Inner class to construct a JSON-compliant query parameter for wikidata WDTAL service
   *
   * @author Fabian Gilson
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private static class QueryObject {

    /**
     * first name (for a person)
     */
    @JsonSerialize
    @JsonProperty("first_name")
    private String firstName;

    /**
     * last name (for a person)
     */
    @JsonSerialize
    @JsonProperty("last_name")
    private String lastName;

    /**
     * organization name
     */
    @JsonSerialize
    @JsonProperty("org_name")
    private String orgName;

    /**
     * Default constructor for JSON serialization
     *
     * @param type either string "person" or "organisation"
     * @param name the name to be put either in orgName (type="organisation"), or in lastName
     * @param first the name to be put in firstName if type="person", ignored otherwise
     */
    public QueryObject(String type, String name, String first) {
      if ("person".equals(type)) {
        this.firstName = first.trim();
        this.lastName = name.trim();
      } else {
        this.orgName = name.trim();
      }
    }
  }
}
