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

package be.webdeb.application.rest.service;

import be.webdeb.application.query.BadQueryException;
import be.webdeb.application.query.QueryExecutor;
import be.webdeb.application.rest.object.*;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.Organization;
import be.webdeb.core.api.actor.Person;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This class wraps all services offered as REST services (currently implemented as Play services.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class RESTAccessor extends Controller {

  private ArgumentFactory argumentFactory;

  private DebateFactory debateFactory;

  private CitationFactory citationFactory;

  private ActorFactory actorFactory;

  private TextFactory textFactory;

  private TagFactory tagFactory;

  private QueryExecutor executor;

  // link to DOCUMENTATION passed when an error occured
  private static final String DOCUMENTATION = "webdeb.be/assets/apidoc-rest/index.html";

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private String secret;

  /**
   * Default injected constructor. Creates a REST accessor facility
   *
   * @param argumentFactory the argument factory to retrieve arguments (for by id requests)
   * @param debateFactory the debate factory to retrieve debate (for by id requests)
   * @param citationFactory the citation factory to retrieve citations (for by id requests)
   * @param actorFactory the actor factory to retrieve actors (for by id requests)
   * @param textFactory the text factory to retrieve texts (for by id requests)
   * @param tagFactory the tag factory to retrieve tags (for by id requests)
   * @param executor the generic query executor facility
   * @param configuration the play configuration facility
   */
  @Inject
  public RESTAccessor(ArgumentFactory argumentFactory, DebateFactory debateFactory, CitationFactory citationFactory,
                      ActorFactory actorFactory, TextFactory textFactory, TagFactory tagFactory,
                      QueryExecutor executor, Configuration configuration) {

    this.argumentFactory = argumentFactory;
    this.debateFactory = debateFactory;
    this.citationFactory = citationFactory;
    this.actorFactory = actorFactory;
    this.textFactory = textFactory;
    this.tagFactory = tagFactory;
    this.executor = executor;
    secret = configuration.getString("query.sensitive.content.key");
  }

  /**
   * @api {get} /rest/byid/:id/:type Retrieve contribution details
   * @apiName ById
   * @apiGroup Services
   * @apiDescription this service is used to retrieve a particular contribution from the database by passing
   * its unique ID and type
   * @apiParam {Integer} id contribution unique id
   * @apiParam {Integer} type a type constant representing the contribution type, ie, <ul>
   * <li>0 for Actor</li>
   * <li>1 for Citation</li>
   * <li>2 for Text</li>
   * <li>3 for Citation Link</li>
   * <li>4 for Affiliation</li>
   * <li>5 for Tag</li>
   * </ul>
   * @apiSuccess {Object} object a WebdebContribution specific JSON structure. Actual list of properties depends on the
   * concrete data structure (see Structures part)
   * @apiError {Object} badrequest BadRequest object giving more details regarding the error, ie, the service as this
   * service signature, the cause as a description of the cause of the error and DOCUMENTATION as a link to the present
   * DOCUMENTATION
   * @apiExample {curl} curl https://webdeb.be/rest/byid/768/0
   * [
   *   {
   *     "id": 768,
   *     "type": "actor",
   *     "version": 1491377168000,
   *     "name": [
   *       { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "en" },
   *       { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "fr" }
   *     ],
   *     "actorType": "person",
   *     "crossReference": "https://fr.wikipedia.org/wiki/Donald_Trump",
   *     "affiliations": [
   *       {
   *         "id": 265,
   *         "type": "affiliation",
   *         "version": 1464270120000,
   *         "affiliationActor": 769,
   *         "function": [
   *           { "name": "businessperson", "lang": "en" },
   *         ],
   *         "endDate": "-1"
   *       },
   *       {
   *         "id": 266,
   *         "type": "affiliation",
   *         "version": 1486739277000,
   *         "affiliationActor": 749,
   *         "function": [
   *           { "name": "president", "lang": "en" },
   *           { "name": "président", "lang": "fr" },
   *         ],
   *         "startDate": "01/2017"
   *       }
   *     ],
   *     "gender": "M",
   *     "birthdate": "14/06/1946",
   *     "countries": [ "us" ]
   *   }
   * ]
   * @apiVersion 0.0.2
   */
  public CompletionStage<Result> retrieveById(Long id, int type) {
    logger.debug("REST retrieve by id for " + id + " of type " + type);
    boolean authorized = isAuthorized(request().getHeader("authorization"));
    String service = "ById";
    switch (EContributionType.value(type)) {
      case ACTOR:
        Actor actor = actorFactory.retrieve(id);
        if (actor != null) {
          return CompletableFuture.supplyAsync(() ->
              ok(Json.toJson(toJSONCompliant(Collections.singletonList(actor), authorized))));
        }
        break;
      case TEXT:
        Text text = textFactory.retrieve(id);
        if (text != null) {
          return CompletableFuture.supplyAsync(() ->
              ok(Json.toJson(toJSONCompliant(Collections.singletonList(text), authorized))));
        }
        break;
      case ARGUMENT:
        Argument argument = argumentFactory.retrieve(id);
        if (argument != null) {
          return CompletableFuture.supplyAsync(() ->
              ok(Json.toJson(toJSONCompliant(Collections.singletonList(argument), authorized))));
        }
        break;
      case CITATION:
        Debate debate = debateFactory.retrieve(id);
        if (debate != null) {
          return CompletableFuture.supplyAsync(() ->
                  ok(Json.toJson(toJSONCompliant(Collections.singletonList(debate), authorized))));
        }
        break;
      case DEBATE:
        Citation citation = citationFactory.retrieve(id);
        if (citation != null) {
          return CompletableFuture.supplyAsync(() ->
                  ok(Json.toJson(toJSONCompliant(Collections.singletonList(citation), authorized))));
        }
        break;
      case TAG:
        Tag tag = tagFactory.retrieve(id);
        if (tag != null) {
          return CompletableFuture.supplyAsync(() ->
              ok(Json.toJson(toJSONCompliant(Collections.singletonList(tag), authorized))));
        }
        break;

      default:
        return CompletableFuture.supplyAsync(() ->
            badRequest(Json.toJson(new BadRequest(service, "unknown type given " + type, DOCUMENTATION))));
    }
    return CompletableFuture.supplyAsync(() ->
        badRequest(Json.toJson(
            new BadRequest(service, "unknown id given " + id + " or wrong type " + type, DOCUMENTATION))));
  }


  /**
   * @api {get} /rest/bycriteria/:query Retrieve contributions by criteria
   * @apiName ByCriteria
   * @apiGroup Services
   * @apiDescription this service is used to retrieve contributions from the database by passing
   * a list of (key, value) pairs to search for. Multiple values for the same key may be passed
   * @apiParam {String} query a query with a collection of key-value pairs used as search criteria. All pairs are
   * separated by "+" and keys are separated to values by "=". Accepted criterion keys are:
   * <p>All contribution types:
   * <ul>
   * <li>contribution_type: 0 for Actor, 1 for Citation, 2 for Text</li>
   * <li>topic: any string value denoting a topic</li>
   * <li>validated: 'true' if only validated contributions must be retrieved, 'false' or unset for any</li>
   * <li>strict: 'true' if all values passed must be treated as strict matches (ex: 'Louis' won't return back 'Louise')</li>
   * <li>fromid: any integer value defining the lower id from which the contributions must be retrieved</li>
   * <li>orderby: only valid for requests with only "contribution_type" keys. May pass either "name" (default) or "id".
   * <li>fetched: 'true' if only automatically fetched contributions must be retrieved (ie, contributions imported from other sources without validation)</li>
   * For all other requests, the sorting key is the relevance regarding the passed search values.</li>
   * </ul></p>
   * <p>For actors:
   * <ul>
   * <li>actor_name: any string value denoting an actor name</li>
   * <li>actor_type: -1 for all types, 0 for persons, 1 for organizations</li>
   * <li>function: any string value denoting a function or profession</li>
   * </ul>
   * </p>
   * <p>For arguments:
   * <ul>
   * <li>argument_type: 0 descriptive, 1 prescriptive, 2 opinion, 3 performative</li>
   * </ul></p>
   * <p>For texts:
   * <ul>
   * <li>text_title: any string value denoting a text title</li>
   * <li>text_source: any string value denoting a source for a text</li>
   * </ul></p>
   * <p>For arguments and texts:
   * <ul>
   * <li>actor: any string value denoting an actor involved in a contribution (any type of involvement)</li>
   * <li>author: any string value denoting an actor involved in a contribution as the author</li>
   * <li>reporter: any string value denoting an actor involved in an argument as a reporter</li>
   * </ul></p>
   * <p>For tags:
   * <ul>
   * <li>tag_name: any string value denoting a tag name</li>
   * </ul></p>
   * @apiSuccess {Array {Object}} response an array of WebdebContribution matching the given criteria (the response
   * has no root element)
   * @apiError {Object} response a BadRequest object giving more details regarding the error, ie,
   * the service as this service signature, the cause as a description of the cause of the error and
   * DOCUMENTATION as a link to the present DOCUMENTATION (the response has no root element)
   * @apiExample {curl} curl https://webdeb.be/rest/bycriteria/actor=Trump+actor_name=Trump
   * [
   *   {
   *     "id": 768,
   *     "type": "actor",
   *     "version": 1491377168000,
   *     "name": [
   *       { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "en" },
   *       { "first": "Donald", "last": "Trump", "pseudo": null, "lang": "fr" }
   *     ],
   *     "actorType": "person",
   *     "crossReference": "https://fr.wikipedia.org/wiki/Donald_Trump",
   *     "affiliations": [
   *       {
   *         "id": 265,
   *         "type": "affiliation",
   *         "version": 1464270120000,
   *         "affiliationActor": 769,
   *         "function": [
   *           { "name": "businessperson", "lang": "en" },
   *         ],
   *         "endDate": "-1"
   *       },
   *       {
   *         "id": 266,
   *         "type": "affiliation",
   *         "version": 1486739277000,
   *         "affiliationActor": 749,
   *         "function": [
   *           { "name": "president", "lang": "en" },
   *           { "name": "président", "lang": "fr" }
   *         ],
   *         "startDate": "01/2017"
   *       }
   *     ],
   *     "gender": "M",
   *     "birthdate": "14/06/1946",
   *     "countries": [ "us" ]
   *   }
   *   {
   *     "id": 776,
   *     "type": "argument",
   *     "version": 1464271112000,
   *     "textId": 774,
   *     "citation": "the doctor or any other person performing this illegal act upon a woman would be held legally responsible",
   *     "argumentType": {
   *       "argumentType": 1,
   *       "typeNames": { "en": "Prescriptive", "fr": "Prescriptive" },
   *       "argumentSubtype": -1,
   *       "subtypeNames": {},
   *       "argumentShade": 10,
   *       "shadeNames": { "en": "It is necessary to", "fr": "Il faut" },
   *       "argumentTiming": 1,
   *       "timingNames": { "en": "Present", "fr": "Présent" },
   *       "singular": true
   *     },
   *     "standardForm": "hold legally responsible the doctor or any other person performing an abortion",
   *     "topics": [ "abortion", "perform", "hold responsible", "doctor", "person" ],
   *     "actors": [
   *       { "actor": 775, "isAuthor": true, "isReporter": false}
   *     ],
   *     "language": "en"
   *   },
   *
   * 	{ ... }
   *
   * ]
   * @apiVersion 0.0.2
   */
  public CompletionStage<Result> retrieveByCriteria(String query) {
    logger.debug("REST retrieve by criteria " + query);
    boolean authorized = isAuthorized(request().getHeader("authorization"));
    try {
      List<Contribution> result = executor.searchContributions(query, 0, 0);
      logger.debug("REST start serialization for query " + query);
      return CompletableFuture.supplyAsync(() -> ok(Json.toJson(toJSONCompliant(result, authorized))));
    } catch (BadQueryException e) {
      logger.error("bad query raised when searching for " + query, e);
      return CompletableFuture.supplyAsync(() ->
          badRequest(Json.toJson(
              new BadRequest("ByCriteria", "error in query " + e.getQuery() + " with key " + e.getErrorKey(),
                  DOCUMENTATION))));
    }
  }

  /**
   * Private method to transform a collection of API contributions into JSON compliant structures
   * (avoiding circular refs)
   *
   * @param contributions a collection of API contributions
   * @return a list of WebdebContribution (JSON-compliant)
   */
  private List<WebdebContribution> toJSONCompliant(Collection<Contribution> contributions, boolean authorized) {
    List<WebdebContribution> result = new ArrayList<>();
    contributions.forEach(c -> {
      switch (c.getType()) {
        case ACTOR:
          Actor actor = (Actor) c;
          switch (actor.getActorType()) {
            case PERSON:
              result.add(new WebdebPerson((Person) actor));
              break;
            case ORGANIZATION:
              result.add(new WebdebOrganization((Organization) actor));
              break;
            default:
              result.add(new WebdebActor((Actor) c));
          }
          break;
        case TEXT:
          result.add(new WebdebText((Text) c, authorized));
          break;
        case ARGUMENT:
          result.add(new WebdebArgument((Argument) c));
          break;
        case CITATION:
          result.add(new WebdebCitation((Citation) c));
          break;
        case DEBATE:
          result.add(new WebdebDebate((Debate) c));
          break;
        case TAG:
          result.add(new WebdebTag((Tag) c));
          break;
        default:
          logger.warn("unknown or unhandled type for contribution " + c.getType().name());
      }
    });
    return result;
  }

  /**
   * Check if given secret corresponds to the one of this application (to get sensitive data like text content)
   *
   * @param secret a secret to check
   * @return true if given secret is the same as this application one for sensitive data queries
   */
  private boolean isAuthorized(String secret) {
    try {
      return secret != null && this.secret.equals(Json.fromJson(Json.parse(secret), Secret.class).key);
    } catch (Exception e) {
      logger.warn("wrong authorization string passed", e);
      return false;
    }
  }

  /**
   * Simple json structure handling the secret authorization header
   */
  private static class Secret {

    @JsonProperty("secret")
    @JsonDeserialize
    private String key;
  }
}
