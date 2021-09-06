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

package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.argument.EArgumentShadeType;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EAlliesOpponentsType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateSimple;
import be.webdeb.core.api.debate.EDebateShade;
import be.webdeb.core.api.debate.EDebateType;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.tag.ContextHasCategoryLinkForm;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.core.api.actor.ESocioGroupKey;
import be.webdeb.presentation.web.controllers.viz.Sociography;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;
import play.i18n.Lang;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class holds concrete values for a TextualContribution. It manage all has_actor dependancy.
 *
 * @author Martin Rouffiange
 */
public class ContextContributionHolder extends TextualContributionHolder{

  private final Map<Integer, String> ages = new HashMap<>();

  @Inject
  protected TextFactory textFactory = Play.current().injector().instanceOf(TextFactory.class);

  @Inject
  private ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

  private ContextContribution context;
  private List<DebateHolder> tagDebates = null;
  private List<ContextHasCategoryLinkForm> categories = null;
  private List<ArgumentJustificationLinkForm> arguments = null;
  private Map<EJustificationLinkShade, List<ArgumentJustificationLinkForm>> argumentsMap = null;
  private Map<EJustificationLinkShade, List<CitationJustificationLinkForm>> citationsMap = null;
  private List<CitationHolder> citationsShaded = null;
  private List<CitationHolder> citations = null;
  protected Map<Long, TextHolder> texts = null;
  protected Map<Long, Integer> textsMap = null;

  private Map<Long, Map<ESocioGroupKey, List<ActorAlliesOpponents>>> positionsFromArguments = new HashMap<>();
  private Map<Long, Map<ESocioGroupKey, List<ActorAlliesOpponents>>> positions = new HashMap<>();

  private Map<Long, Map<EJustificationLinkShade, Integer>> nbCitationsMap = new HashMap<>();


  public ContextContributionHolder() {
    super();
  }

  public ContextContributionHolder(ContextContribution contribution, WebdebUser user, String lang) {
    this(contribution, user, lang, false);
  }

  public ContextContributionHolder(ContextContribution contribution, WebdebUser user, String lang, boolean light) {
    this(contribution, user, lang, light, false);
  }

  public ContextContributionHolder(ContextContribution contribution, WebdebUser user, String lang, boolean light, boolean ultraLight) {
    super(contribution, user, lang, light);

    context = contribution;
    this.user = user;

    /*
    // build categories for ages
    int[] rangeLimit = new int[]{15, 25, 35, 45, 55, 65};
    int low = 0;
    for (int range : rangeLimit) {
      for (int i = low; i < range; i++) {
        ages.put(i, i18n.get(Lang.forCode(lang), "viz.actor.socio.age." + range));
      }
      low = range;
    }*/
  }

  public boolean isMultipleDebate(){
    return type == EContributionType.DEBATE && ((Debate)context).isMultiple();
  }

  public boolean isTagDebate(){
    return type == EContributionType.DEBATE && ((Debate)context).getEType() == EDebateType.TAG_DEBATE;
  }

  public boolean isTag(){
    return type == EContributionType.TAG && ((Tag)context).getTagType().getEType() == ETagType.SIMPLE_TAG;
  }

  @JsonIgnore
  public synchronized List<DebateHolder> getTagDebates() {
    if(tagDebates == null) {
      tagDebates = helper.toTagDebatesHolders(context.getTagDebates(user.getId(), user.getGroupId()), user, lang, true);
    }
    return tagDebates;
  }

  @JsonIgnore
  public synchronized List<ContextHasCategoryLinkForm> getCategories() {
    if(categories == null) {
      categories = fromApiToContextHasCategoryLinkForms(context.getContextCategories(), context);
    }

    return categories;
  }

  @JsonIgnore
  public synchronized List<ArgumentJustificationLinkForm> getArguments() {
    if(arguments == null){
      arguments = fromApiToArgumentJustificationLinkForms(
              context.getArgumentJustificationLinks(null, null, null), context);
    }

    return arguments;
  }

  @JsonIgnore
  public synchronized List<ArgumentJustificationLinkForm> getArguments(EJustificationLinkShade shade) {
    if(argumentsMap == null){
      argumentsMap = fromApiToArgumentJustificationLinkFormsMap(
              context.getArgumentJustificationLinks(null, null, null), context);
    }

    return argumentsMap.containsKey(shade) ? argumentsMap.get(shade) : new ArrayList<>();
  }

  @JsonIgnore
  public synchronized List<CitationJustificationLinkForm> getCitations(EJustificationLinkShade shade) {
    if(citationsMap == null){
      citationsMap = fromApiToCitationJustificationLinkFormsMap(
              context.getCitationJustificationLinks(null, null, null));
    }

    return citationsMap.containsKey(shade) ? citationsMap.get(shade) : new ArrayList<>();
  }

  @JsonIgnore
  public synchronized List<CitationHolder> getAllCitationLinks() {
    if(citationsShaded == null){
      citationsShaded = helper.toShadedCitationHolders(context.getAllCitationJustificationLinks(), user, lang);
    }

    return citationsShaded;
  }

  @JsonIgnore
  public synchronized List<CitationHolder> getAllCitations() {
    if(citations == null){
      citations = helper.toCitationsHolders(context.getAllCitations(), user, lang, false);
    }
    return citations;
  }

  @JsonIgnore
  public Map<Long, TextHolder> getTextsCitations(){
    if(texts == null){
      textsMap = new LinkedHashMap<>();
      texts = new LinkedHashMap<>();

      helper.toTextsHolders(context.getTextsCitations(user.getId(), user.getGroupId()), user, lang, true).forEach(text -> {
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
  public int getNbCitationsLinks(Long category, EJustificationLinkShade shade) {
    if((!nbCitationsMap.containsKey(category) || !nbCitationsMap.get(category).containsKey(shade)) && context != null) {
      if(!nbCitationsMap.containsKey(category)) {
        nbCitationsMap.put(category, new HashMap<>());
      }

      nbCitationsMap.get(category).put(shade, argumentFactory.getArgumentNbCitationsLink(
              context.getSuperContextId(),
              context.getSubContextId(),
              category,
              -1L,
              shade.id(),
              user.getId(),
              user.getGroupId()));
    }

    return nbCitationsMap.get(category).get(shade);
  }

  @JsonIgnore
  public boolean hasContent(EJustificationLinkShade shade) {
    return !getArguments(shade).isEmpty() || getNbCitationsLinks(-1L, shade) != 0;
  }

  /**
   * Build the sociography view data (allies/opponents) for a given key, i.e., a list of nodes (whose semantics depends on given groupKey)
   * where all excerpts having a similarity link to an excerpt stated by this actor are grouped following
   * given groupKey. The nodes defines aggregated similarity statistics regarding this actor
   * <p>
   * Sociography nodes are grouped regarding the given groupKey (eg. individual actors, functions, countries, etc)
   *
   * @return a collection of ActorSociographyNode containing labels and statistics
   */
  @JsonIgnore
  public Map<ESocioGroupKey, List<ActorAlliesOpponents>> viewPositions(Long subContextId, EAlliesOpponentsType type) {
    subContextId = subContextId == null ? -1L : subContextId;

    if(type == EAlliesOpponentsType.ARGUMENTS ? !positionsFromArguments.containsKey(subContextId) : !positions.containsKey(subContextId)){
      Map<ESocioGroupKey, List<ActorAlliesOpponents>> preresults = actorFactory.getAlliesOpponentsMap(
              new SearchContainer(id,
                      subContextId,
                      user.getId(),
                      user.getGroupId(),
                      lang,
                      type));

      if(type == EAlliesOpponentsType.ARGUMENTS){
        positionsFromArguments.put(subContextId, preresults);
      } else {
        positions.put(subContextId, preresults);
      }
    }

    return (type == EAlliesOpponentsType.ARGUMENTS ? positionsFromArguments.get(subContextId) : positions.get(subContextId));
  }

  /**
   * Get citations from sociography view for given socio key and key value
   *
   * @param subContextId a sub context tag id
   * @param key a ESocioGroupKey key id
   * @param value the key value
   * @param isArgument true for arguments, false for positions
   * @return a map of justification shade enum and list of citation holders
   */
  @JsonIgnore
  public Map<Integer, List<CitationHolder>> sociographyCitations(Long subContextId, ESocioGroupKey key, Long value, boolean isArgument) {
    EAlliesOpponentsType aType =
            isTag() ? EAlliesOpponentsType.TAGS : isArgument ? EAlliesOpponentsType.ARGUMENTS : EAlliesOpponentsType.POSITIONS;

    return helper.toShadeCitationHoldersMap(
              actorFactory.getAlliesOpponentsCitationsMap(
                new SearchContainer(
                        value,
                        key,
                        context.getSuperContextId(),
                        subContextId,
                        user.getId(),
                        user.getGroupId(),
                        lang,
                        aType)),
            user,
            lang,
            aType);
  }

  /*
   * PRIVATE HELPER
   */

  /**
   * Helper method to update a given map of sociography nodes with a new SociographyNode for given actor
   *
   * @param list  a list of sociography node (will be updated)
   * @param actor the actor to put in node map
   * @param type  the link type depicting the position of a particular citation for given label
   */
  private void updateNodeMap(Map<ESocioGroupKey, Map<String, SociographyNode>> list, ESocioGroupKey key, ActorSimpleForm actor, EJustificationLinkShade type) {
    String label = actor.getFullname();
    String sortkey = actor.getActortype() == EActorType.PERSON.id() ? actor.getLastname() : label;
    String avatar = actor.getDefaultAvatar();

    SociographyNode node = list.get(key).containsKey(label) ? list.get(key).get(label)
            : new SociographyNode(actor.getId(), sortkey, label, avatar, String.valueOf(actor.getId()), false);
    list.get(key).put(label, node.increment(type));
  }


  /**
   * Helper method to update a given map of sociography nodes with a new ActorSociographyNode with given label and type
   *
   * @param list  a list of sociography node (will be updated)
   * @param key   the key that will be used to group sociography node in the map
   * @param label a label (semantics depend on the actual key used to group nodes behind a label), may be null
   * @param type  the link type depicting the position of a particular argument for given label
   */
  private void updateNodeMap(Map<ESocioGroupKey, Map<String, SociographyNode>> list, ESocioGroupKey key, String label, EJustificationLinkShade type) {
    boolean isUnknown = false;
    String updatedLabel = label;
    if (updatedLabel == null) {
      isUnknown = true;
      switch (key) {
        case AGE:
          updatedLabel = i18n.get(Lang.forCode(lang), "viz.actor.socio.age.unknown");
          break;

        case COUNTRY:
          updatedLabel = i18n.get(Lang.forCode(lang), "viz.actor.socio.country.unknown");
          break;

        case ORGANIZATION:
          updatedLabel = i18n.get(Lang.forCode(lang), "viz.actor.socio.org.unknown");
          break;

        case FUNCTION:
          updatedLabel = i18n.get(Lang.forCode(lang), "viz.actor.socio.function.unknown");
          break;

        default:
          // do nothing
      }
    }
    SociographyNode node = list.get(key).containsKey(updatedLabel) ? list.get(key).get(updatedLabel)
            : new SociographyNode(updatedLabel, isUnknown ? "null" : updatedLabel, isUnknown);
    list.get(key).put(updatedLabel, node.increment(type));
  }

  @JsonIgnore
  public Map<Long, Integer> getTextsMap() {
    if(textsMap == null){
      getTextsCitations();
    }
    return textsMap;
  }

  @Override
  public String getDefaultAvatar() {
    return null;
  }

  @Override
  public MediaSharedData getMediaSharedData() {
    return null;
  }

  @Override
  public String getContributionDescription() {
    return null;
  }

  /**
   * Inner class from ActorVizHolder to handle actor sociography nodes, ie, data series objects used to build a sociography graph.
   * Contains an id in case the node refers to an actor (author or organization), a label to be displayed and
   * an array of integer values corresponding to the amount of arguments resp. similar, qualifying or opposed
   * to all arguments of the actor holding those nodes.
   *
   * @author Fabian Gilson
   * @author Martin Rouffiange
   */
  public class SociographyNode {

    private Long id;
    private String label;
    private String avatar;
    private String sortkey;
    private String key;
    // amount of similar, qualifying and opposed arguments
    private Map<EJustificationLinkShade, Integer> statistics = new LinkedHashMap<>();
    private boolean isUnknown;

    /**
     * Construct a node for any type of sortkey (being the label itself) except actors
     *
     * @param label      the label displayed in the sociography graph
     * @param key        the sociography key value
     * @param isUnknown  boolean saying if this node is unknown (to be put at end of list in graph)
     */
    SociographyNode(String label, String key, boolean isUnknown) {
      this(-1L, label, label, null, key, isUnknown);
    }

    /**
     * Construct a node for actors
     *
     * @param id         the actor id
     * @param sortkey    the key on which this node will be ordered
     * @param label      the label displayed in the sociography graph
     * @param avatar     the name of the actor's avatar (may be null)
     * @param key        the sociography key value
     * @param isUnknown  boolean saying if this node is unknown (to be put at end of list in graph)
     */
    SociographyNode(Long id, String sortkey, String label, String avatar, String key, boolean isUnknown) {
      this.id = id;
      this.sortkey = sortkey;
      this.label = label;
      this.avatar = avatar;
      this.key = key;
      this.isUnknown = isUnknown;

      statistics.put(EJustificationLinkShade.NONE, 0);
      statistics.put(EJustificationLinkShade.SUPPORTS, 0);
      statistics.put(EJustificationLinkShade.REJECTS, 0);
    }

    /**
     * Get the idof this node, if any
     *
     * @return either an actor id, or -1 if this node is not an actor
     */
    public Long getId() {
      return id;
    }

    /**
     * Get the value on which the nodes are sorted
     *
     * @return a sorting value
     */
    String getSortkey() {
      return sortkey;
    }

    /**
     * Get the sociography key value
     *
     * @return a key value
     */
    public String getKey() {
      return key;
    }

    /**
     * Get the label used to show this node
     *
     * @return a label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Get the avatar file name, if this node is an actor
     *
     * @return a file name, null if unset
     */
    public String getAvatar() {
      return avatar;
    }

    /**
     * Get the statistics array with, resp. IS_SUPPORTED, IS_SHADED and IS_REJECTED amounts for this node
     *
     * @return the statistics array
     */
    public Map<EJustificationLinkShade, Integer> getStatistics() {
      return statistics;
    }

    /**
     * Get the statistics as string separated by ","
     *
     * @return the statistics as string
     */
    public String getStatisticsAsString() {
      return statistics.values().stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * Boolean flag to kwow if this node is the special "unknown" node
     *
     * @return true if this node is the unknown one (will be put at the end of visualization)
     */
    public boolean isUnknown() {
      return isUnknown;
    }

    /**
     * Increment statistics value depending on given link shade
     *
     * @param type a link shade to increment
     * @return this updated node
     */
    public SociographyNode increment(EJustificationLinkShade type) {
      statistics.put(type, statistics.get(type) + 1);
      return this;
    }
  }

  /**
   * Comparator class to sort sociography nodes based on the statistics
   */
  private class SocioComparator implements Comparator<SociographyNode> {

    @Override
    public int compare(SociographyNode o1, SociographyNode o2) {
      if (o1 == null || o1.getStatistics().isEmpty() || o1.isUnknown()) {
        return 1;
      }
      if (o2 == null || o2.getStatistics().isEmpty() || o2.isUnknown()) {
        return -1;
      }

      if(o1.getStatistics().get(EJustificationLinkShade.SUPPORTS) > o2.getStatistics().get(EJustificationLinkShade.SUPPORTS)){
        return -1;
      }

      if(o1.getStatistics().get(EJustificationLinkShade.SUPPORTS) < o2.getStatistics().get(EJustificationLinkShade.SUPPORTS)){
        return 1;
      }

      if(o1.getStatistics().get(EJustificationLinkShade.REJECTS) > o2.getStatistics().get(EJustificationLinkShade.REJECTS)){
        return -1;
      }

      if(o1.getStatistics().get(EJustificationLinkShade.REJECTS) < o2.getStatistics().get(EJustificationLinkShade.REJECTS)){
        return 1;
      }

      return o1.getSortkey().compareTo(o2.getSortkey());
    }
  }

  /**
   * Simple class to says if something is added in sociography map
   */
  private class SocioAdded {
    private boolean added;

    SocioAdded(boolean added){
      this.added = added;
    }

    public boolean isAdded() {
      return added;
    }

    public void setAdded() {
      this.added = true;
    }
  }

}
