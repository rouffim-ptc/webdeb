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

package be.webdeb.presentation.web.controllers.viz.actor;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.impl.helper.ActorAffiliation;
import be.webdeb.core.impl.helper.ActorAffiliations;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.ETextsView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.libs.Json;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This class holds actor values to build visualisation pages
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ActorVizHolder extends ActorHolder {

  private Map<ESocioGroupKey, List<ActorDistance>> actorsDistance = null;
  private Map<Long, TextHolder> texts = null;
  private Map<Long, Integer> textsMap = null;

  private List<ActorAffiliations> affiliateds = null;
  private List<ActorAffiliations> affiliations = null;
  private List<ActorAffiliations> members = null;

  private List<ActorHolder> actorsWhoQuotes = null;
  private Map<EActorRole, List<DebateHolder>> debatesCitations = new HashMap<>();
  private Map<EActorRole, List<TextHolder>> textsCitations = new HashMap<>();
  private Map<EActorRole, List<TagHolder>> tagsCitations = new HashMap<>();
  private Map<EActorRole, List<CitationHolder>> citations = new HashMap<>();

  private SearchContainer affiliationsSearch;

  /**
   * Default Constructor
   *
   * @param actor the actor to visualize
   * @param lang  2-char ISO 639-1 code of context language (among play accepted languages)
   */
  public ActorVizHolder(Actor actor, WebdebUser user, String lang) {
    super(actor, user, lang);

    this.actor = actor;

    EActorRole.mainToList().forEach(role -> {
      debatesCitations.put(role, null);
      textsCitations.put(role, null);
      tagsCitations.put(role, null);
      citations.put(role, null);
    });
  }

  public String affiliationsToJson(List<ActorAffiliations> affiliations) {
    return Json.toJson(affiliations).toString().replaceAll("'", "’");
  }

  /**
   * Get the list of affiliations where this actors is the owner
   *
   * @return the list of affiliations as ActorAffiliations
   */
  public List<ActorAffiliations> getAffiliateds(String filters) {
    if(affiliateds == null ) {
      affiliateds = eactortype ==
              EActorType.ORGANIZATION ? actor.getOwners(getAffiliationSearchContainer(filters)) : new ArrayList<>();
    }

    return affiliateds;
  }

  /**
   * Get the list of affiliations depending of this actor type and given actor type
   *
   * @return the list of affiliations as ActorAffiliations
   */
  public List<ActorAffiliations> getAffiliations(EActorType type, String filters) {
    if(type == EActorType.PERSON) {
       return getMembers(filters);
    }

    return getAllAffiliations(filters);
  }

  public String getBeginDate(EActorType viewed) {
    String min = actor.getMinOrMaxAffiliationDate(viewed, true);
    return min != null ? min : super.getBeginDate();
  }

  public String getEndDate(EActorType viewed) {
    String max = actor.getMinOrMaxAffiliationDate(viewed, false);
    return max != null ? max : super.getEndDate();
  }

  public Map<String, Map<String, List<ActorAffiliations>>> getAffiliationsByKey(EActorType viewBy) {
    if(eactortype == EActorType.PERSON) {
      return getAffiliationsByOrgType();
    }

    if(viewBy == EActorType.PERSON) {
      return getAffiliationsByFunction();
    }

    return getAffiliationsByType();
  }

  /**
   * Get all affiliations of this actor separate by actor type. Will regroup all affiliation instances under the same map key
   * (since an actor my have multiple affiliation to the same actor with different dates and/or functions)
   *
   * @return the aff map by affiliation type
   */
  public Map<String, Map<String, List<ActorAffiliations>>> getAffiliationsByType() {
    Map<String, Map<String, Map<Long, ActorAffiliations>>> map = new LinkedHashMap<>();

    List<ActorAffiliations> actors = getAffiliateds(null);
    actors.addAll(getAllAffiliations(null));

    actors.forEach(actor ->
        actor.getAffiliations().forEach(aff ->
          updateAffMap(map, actor, aff, aff.getAffTypeNameOrSubstitute(), "")));

    return sortAffMap(map);
  }

  /**
   * Get all affiliations of this actor separate by actor type. Will regroup all affiliation instances under the same map key
   * (since an actor my have multiple affiliation to the same actor with different dates and/or functions)
   *
   * @return the aff map by organization type
   */
  public Map<String, Map<String, List<ActorAffiliations>>> getAffiliationsByOrgType() {
    Map<String, Map<String, Map<Long, ActorAffiliations>>> map = new LinkedHashMap<>();

    getAllAffiliations(null).forEach(actor ->
            actor.getAffiliations().forEach(aff ->
                    updateAffMap(map, actor, aff, actor.getOrgType(), "")));

    return sortAffMap(map);
  }

  /**
   * Get all affiliations of this actor separate by actor type. Will regroup all affiliation instances under the same map key
   * (since an actor my have multiple affiliation to the same actor with different dates and/or functions)
   *
   * @return the aff map by function
   */
  public Map<String, Map<String, List<ActorAffiliations>>> getAffiliationsByFunction() {
    Map<String, Map<String, Map<Long, ActorAffiliations>>> map = new LinkedHashMap<>();

    getMembers(null).forEach(actor ->
            actor.getAffiliations()
                    .forEach(aff ->
                    updateAffMap(map, actor, aff, aff.getGenericFunction(), aff.getNeutralFunction())));


    return sortAffMap(map);
  }

  /**
   * Get the map of actors / distance with this actor for global positions
   *
   * @return the map of global positions distances
   */
  @JsonIgnore
  public Map<ESocioGroupKey, List<ActorDistance>> getActorsDistance() {
    if(actorsDistance == null){
      actorsDistance = actorFactory.getActorsDistance(new SearchContainer(
              id,
              user.getId(),
              user.getGroupId(),
              lang
      ));
    }

    return actorsDistance;
  }

  /**
   * Get the map of actor distance with given actor id for global positions
   *
   * @return the map with possibly empty values list
   */
  @JsonIgnore
  public List<ActorDistance> getActorsDistanceByDebate(ESocioGroupKey socioGroupKey, Long value) {
      return actorFactory.getActorsDistanceByDebates(new SearchContainer(
              id,
              socioGroupKey,
              value,
              user.getId(),
              user.getGroupId(),
              lang
      ));
  }

  @JsonIgnore
  public Map<Long, Integer> getTextsMap() {
    if(textsMap == null){
      getTextsCitations();
    }
    return textsMap;
  }

  @JsonIgnore
  public Map<Long, TextHolder> getTextsCitations(){
    if(texts == null){
      textsMap = new LinkedHashMap<>();
      texts = new LinkedHashMap<>();

      helper.toTextsHolders(actor.getTextsWhereCitationAuthor(user.getId(), user.getGroupId()), user, lang, true).forEach(text -> {
        textsMap.put(text.getId(), text.getNbCitations());
        texts.put(text.getId(), text);
      });

      List<Map.Entry<Long, Integer>> list = new ArrayList<>(textsMap.entrySet());
      list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

      textsMap = new LinkedHashMap<>();
      for (Map.Entry<Long, Integer> entry : list) {
        textsMap.put(entry.getKey(), entry.getValue());
      }

    }

    return texts;
  }

  @JsonIgnore
  public List<ActorHolder> getActorsWhoQuotes() {
    return getActorsWhoQuotes(null, 0, -1);
  }

  public List<ActorHolder> getActorsWhoQuotes(String filter, int fromIndex, int toIndex) {
    if(actorsWhoQuotes == null) {
      actorsWhoQuotes = helper.toActorsHolders(actor.getOthersActorsCitations(new SearchContainer(
              id,
              EActorRole.CITED,
              user.getId(),
              user.getGroupId(),
              lang,
              filter,
              fromIndex,
              toIndex
      )), user, lang, true);
    }

    return actorsWhoQuotes;
  }

  public List<DebateHolder> getDebatesCitations(EActorRole role) {
    return getDebatesCitations(role, null, 0, -1);
  }

  public List<DebateHolder> getDebatesCitations(EActorRole role, String filter, int fromIndex, int toIndex) {
    if(debatesCitations.get(role) == null) {
      debatesCitations.put(role, helper.toDebatesHolders(actor.getDebatesCitations(new SearchContainer(
              id,
              role,
              user.getId(),
              user.getGroupId(),
              lang,
              filter,
              fromIndex,
              toIndex
      )), user, lang, true));
    }

    return debatesCitations.get(role);
  }

  public List<TagHolder> getTagsCitations(EActorRole role) {
    return getTagsCitations(role, null, 0, -1);
  }

  public List<TagHolder> getTagsCitations(EActorRole role, String filter, int fromIndex, int toIndex) {
    if(tagsCitations.get(role) == null) {
      tagsCitations.put(role, helper.toTagsHolders(actor.getTagsCitations(new SearchContainer(
              id,
              role,
              user.getId(),
              user.getGroupId(),
              lang,
              filter,
              fromIndex,
              toIndex
      )), user, lang, true));
    }

    return tagsCitations.get(role);
  }

  public List<TextHolder> getTextsCitations(EActorRole role) {
    return getTextsCitations(role, null, 0, -1);
  }

  public List<TextHolder> getTextsCitations(EActorRole role, String filter, int fromIndex, int toIndex) {
    if(textsCitations.get(role) == null) {
      textsCitations.put(role, helper.toTextsHolders(actor.getTextsCitations(new SearchContainer(
              id,
              role,
              user.getId(),
              user.getGroupId(),
              lang,
              filter,
              fromIndex,
              toIndex
      )), user, lang, true));
    }

    return textsCitations.get(role);
  }

  public List<CitationHolder> getCitations(EActorRole role) {
    return getCitations(role, null, 0, -1);
  }

  public List<CitationHolder> getCitations(EActorRole role, String filter, int fromIndex, int toIndex) {
    if(citations.get(role) == null) {
      citations.put(role, helper.toCitationsHolders(actor.getCitations(new SearchContainer(
              id,
              role,
              user.getId(),
              user.getGroupId(),
              lang,
              filter,
              fromIndex,
              toIndex
      )), user, lang, true));
    }

    return citations.get(role);
  }

  public List<CitationHolder> getActorWhoQuotesCitationsList(Long actorId, int fromIndex, int toIndex, String filters) {
    return getContributionCitationsList(actorId, EContributionType.ACTOR, null, fromIndex, toIndex, filters);
  }

  public List<CitationHolder> getDebateCitationsList(Long debateId, EActorRole role, int fromIndex, int toIndex, String filters) {
      return getContributionCitationsList(debateId, EContributionType.DEBATE, role, fromIndex, toIndex, filters);
  }

  public List<CitationHolder> getTagCitationsList(Long tagId, EActorRole role, int fromIndex, int toIndex, String filters) {
    return getContributionCitationsList(tagId, EContributionType.TAG, role, fromIndex, toIndex, filters);
  }

  public List<CitationHolder> getTextCitationsList(Long textId, EActorRole role, int fromIndex, int toIndex, String filters) {
    return getContributionCitationsList(textId, EContributionType.TEXT, role, fromIndex, toIndex, filters);
  }

  public List<CitationHolder> getContributionCitationsList(Long contributionId, EContributionType type, EActorRole role, int fromIndex, int toIndex, String filters) {
    return helper.toCitationsHolders(actor.getCitationsFromContribution(new SearchContainer(
            id,
            contributionId,
            type,
            role,
            user.getId(),
            user.getGroupId(),
            lang,
            filters,
            fromIndex,
            toIndex
    )), user, lang, true);
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Get the list of affiliations where actors are owned by this actor (this actor must be an organization)
   *
   * @return the list of affiliations as ActorAffiliations
   */
  private List<ActorAffiliations> getAllAffiliations(String filters) {
    if(affiliations == null) {
      SearchContainer search = getAffiliationSearchContainer(filters);
      affiliations = (eactortype == EActorType.ORGANIZATION ?
              actor.getOwnedOrganizations(search) : actor.getAffiliationOrganizations(search));
    }

    return affiliations;
  }

  /**
   * Get the list of affiliations where actors own this actor (must be an organization)
   *
   * @return the list of affiliations as ActorAffiliations
   */
  private List<ActorAffiliations> getMembers(String filters) {
    if(members == null) {
      members = actor.getMembers(getAffiliationSearchContainer(filters));
    }

    return members;
  }

  private SearchContainer getAffiliationSearchContainer(String filters) {
    return new SearchContainer(
            lang,
            filters,
            user.getId(),
            user.getGroupId()
    );
  }

  /**
   * Update the affiliation map with given affiliation
   *
   * @param affMap the affmap to sort
   * @param actor the actor linked to the aff
   * @param aff the affiliation to add
   * @param key1 the first key
   * @param key2 the second key
   */
  private void updateAffMap(Map<String, Map<String, Map<Long, ActorAffiliations>>> affMap, ActorAffiliations actor, ActorAffiliation aff, String key1, String key2) {
    if (!values.isBlank(key1)) {

      if (!affMap.containsKey(key1)) {
        affMap.put(key1, new LinkedHashMap<>());
      }

      if (!affMap.get(key1).containsKey(key2)) {
        affMap.get(key1).put(key2, new HashMap<>());
      }

      if (!affMap.get(key1).get(key2).containsKey(actor.getId())) {
        affMap.get(key1).get(key2).put(actor.getId(), actor.clone());
      }

      affMap.get(key1).get(key2).get(actor.getId()).addAffiliation(aff);
    }
  }

  /**
   * Sort the affiliation map
   *
   * @param affMap the affmap to sort
   * @return the affmap sorted
   */
  private Map<String, Map<String, List<ActorAffiliations>>> sortAffMap(Map<String, Map<String, Map<Long, ActorAffiliations>>> affMap) {
    Map<String, Map<String, List<ActorAffiliations>>> sortedMap = new LinkedHashMap<>();

    affMap.keySet()
            .stream()
            .sorted()
            .forEach(key1 -> {
              sortedMap.put(key1, new LinkedHashMap<>());

              affMap.get(key1)
                      .keySet()
                      .stream()
                      .sorted()
                      .forEach(key2 -> {
                        sortedMap.get(key1).put(key2,
                                affMap.get(key1).get(key2)
                                        .values()
                                        .stream()
                                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                                        .collect(Collectors.toList()));
                      });
            });

    return sortedMap;
  }

  public static String hasNoContent(boolean hasNoContent) {
    return hasNoContent ? " (0)" : "";
  }
}