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

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.core.impl.helper.ActorAffiliations;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements an Actor of both Person and Organization types. All main common attributes are
 * available from this class.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteActor extends AbstractContribution<ActorFactory, ActorAccessor> implements Actor {

  protected EActorType actortype;
  protected List<ActorName> names;
  private String crossReference;
  private List<Affiliation> affiliations = null;
  private List<Affiliation> simpleAffiliations = null;
  private Map<EAffiliationType, List<Affiliation>> affiliatedTypeMap = new LinkedHashMap<>();
  private Map<EAffiliationType, List<Affiliation>> affiliationsTypeMap = new LinkedHashMap<>();
  private Map<EActorType, List<Affiliation>> affiliatedMap = new LinkedHashMap<>();
  private Map<EActorType, List<Affiliation>> affiliationsMap = new LinkedHashMap<>();
  private Map<EActorType, Map<Long, List<Affiliation>>> affMap = new LinkedHashMap<>();
  protected Long avatarId;
  protected String avatarExtension;
  protected ContributorPicture avatar = null;

  ConcreteActor(ActorFactory factory, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, actorAccessor, contributorFactory);
    type = EContributionType.ACTOR;
    actortype = EActorType.UNKNOWN;
  }

  @Override
  public EActorType getActorType() {
    return actortype;
  }

  @Override
  public List<ActorName> getNames() {
    return names;
  }

  @Override
  public void setNames(List<ActorName> names) {
    this.names = names;
  }

  @Override
  public void addName(ActorName name) {
    updateNameList(names, name);
  }

  @Override
  public ActorName getName(String lang) {
    return getActorName(lang);
  }

  @Override
  public String getFullname(String lang) {
    return getActorName(lang).getFullName(actortype);
  }

  @Override
  public Long getAvatarId() {
    return avatarId;
  }

  @Override
  public void setAvatarId(Long avatarId) {
    this.avatarId = avatarId;
  }

  @Override
  public String getAvatarExtension() {
    return avatarExtension;
  }

  @Override
  public void setAvatarExtension(String extension) {
    this.avatarExtension = extension;
  }

  @Override
  public ContributorPicture getAvatar() {
    if(avatar == null) {
      avatar = contributorFactory.retrieveContributorPicture(avatarId);
    }
    return avatar;
  }

  @Override
  public void setAvatar(ContributorPicture avatar) {
    this.avatar = avatar;
  }

  @Override
  public String getCrossReference() {
    return crossReference;
  }

  @Override
  public void setCrossReference(String crossReference) throws FormatException {
    if (!factory.getValuesHelper().isBlank(crossReference)) {
      if (factory.getValuesHelper().isURL(crossReference) && crossReference.length() <= MAX_URL_SIZE) {
        this.crossReference = crossReference.trim();
      } else {
        throw new FormatException(FormatException.Key.ACTOR_ERROR, "cross reference url is not valid " + crossReference);
      }
    }
  }

  @Override
  public synchronized List<Affiliation> getAffiliations() {
    if(affiliations == null) {
      affiliations = accessor.findAllAffiliations(id);
      Collections.sort(affiliations);
    }
    return affiliations;
  }

  @Override
  public List<Affiliation> getSimpleAffiliations() {
    if(simpleAffiliations == null) {
      simpleAffiliations = accessor.findAffiliations(id);
      Collections.sort(simpleAffiliations);
    }
    return simpleAffiliations;
  }

  @Override
  public synchronized List<Affiliation> getAffiliations(EAffiliationType type) {
   if(!affiliationsTypeMap.containsKey(type)){
     affiliationsTypeMap.put(type, accessor.findAffiliations(id, type));
   }
   return affiliationsTypeMap.get(type);
  }

  @Override
  public synchronized List<Affiliation> getActorsAffiliated(EAffiliationType type) {
    if(!affiliatedTypeMap.containsKey(type)){
      affiliatedTypeMap.put(type, accessor.findAffiliatedActors(id, type));
    }
    return affiliatedTypeMap.get(type);
  }

  @Override
  public synchronized List<Affiliation> getActorsAffiliated(EActorType type) {
    if(!affiliatedMap.containsKey(type)){
      List<Affiliation> aff = sortAffiliationByActor(accessor.findAffiliatedActors(id, type), accessor.sortAffiliatedActor(id, type));
      affiliatedMap.put(type, aff);
      addAffiliationsToAffMap(aff, type);
    }
    return affiliatedMap.get(type);
  }

  @Override
  public synchronized List<Affiliation> getActorsAffiliations(EActorType type) {
    if(!affiliationsMap.containsKey(type)){
      List<Affiliation> aff = sortAffiliationByActor(accessor.findAffiliations(id, type), accessor.sortAffiliationActor(id, type));
      affiliationsMap.put(type, aff);
      addAffiliationsToAffMap(aff, type);
    }
    return affiliationsMap.get(type);
  }

  private List<Affiliation> sortAffiliationByActor(List<Affiliation> aff, List<Long> actorsIds){
    Map<Long, List<Affiliation>> actorMap = new LinkedHashMap<>();
    actorsIds.forEach(id -> actorMap.put(id, new ArrayList<>()));

    aff.stream()
            .filter(a -> actorMap.get(a.getActor().getId()) != null)
            .forEach(a -> actorMap.get(a.getActor().getId()).add(a));


    List<Affiliation> result = new ArrayList<>();
    actorMap.values().forEach(result::addAll);
    return result;
  }

  private void addAffiliationsToAffMap(List<Affiliation> affiliations, EActorType type){
    affMap.put(type, new LinkedHashMap<>());
    affiliations.stream().filter(e -> e.getActor() != null).forEach(e -> {
      Long key = e.getActor().getId();
      if(affMap.get(type).containsKey(key)){
        affMap.get(type).get(key).add(e);
      }else{
        affMap.get(type).put(key, new ArrayList<>(Collections.singletonList(e)));
      }
    });
  }

  @Override
  public Map<Long, List<Affiliation>> getAffMap(EActorType type) {
    return affMap.get(type);
  }

  @Override
  public List<Affiliation> getAffMap(EActorType type, Long actorId) {
    if(affMap.containsKey(type) && affMap.get(type).get(actorId) != null){
      return affMap.get(type).get(actorId);
    }
    return new ArrayList<>();
  }

  @Override
  public synchronized List<Affiliation> getAllAffiliations(EActorType type) {
    List<Affiliation> aff = getActorsAffiliated(type);
    aff.addAll(getActorsAffiliations(type));
    return aff;
  }

  @Override
  public void setAffiliations(List<Affiliation> affiliations) throws FormatException {
    if (affiliations != null) {
      if (this.affiliations != null) {
        // for GC to work properly
        this.affiliations.clear();
      } else {
        this.affiliations = new ArrayList<>();
      }
      for (Affiliation a : affiliations) {
        addAffiliation(a);
      }
      Collections.sort(affiliations);
    }
  }

  @Override
  public Map<Contribution, ActorRole> getContributions(EContributionType type) {
    return accessor.getContributions(id, type);
  }

  @Override
  public void addAffiliation(Affiliation affiliation) throws FormatException {
    if (affiliations == null) {
      getAffiliations();
    }

    if (affiliation != null) {
      String check = String.join(", ", affiliation.isValid());
      if (!factory.getValuesHelper().isBlank(check)) {
        throw new FormatException(FormatException.Key.ACTOR_ERROR, "given affiliation contains invalid fields " + check);
      }

      // loop to find right place
      boolean added = false;
      ListIterator<Affiliation> i = affiliations.listIterator();
      while (i.hasNext() && !added) {
        Affiliation a = i.next();
        if (a.equals(affiliation)) {
          // leave if we already know it
          logger.warn("ignored affiliation since it already exists for actor "
              + getFullname(factory.getDefaultLanguage()) + ": " + affiliation.toString());
          added = true;
        } else {
          // insert at this place if current element is bigger
          if (a.compareTo(affiliation) > 0) {
            i.set(affiliation);
            i.add(a);
            added = true;
          }
        }
      }

      if (!added) {
        affiliations.add(affiliation);
      }
    }
  }

  @Override
  public void removeAffiliation(Long affiliation) throws PermissionException {
    if (affiliations == null) {
      getAffiliations();
    }

    if (affiliation != null) {
      for (ActorRole r : accessor.getContributions(id, EContributionType.ALL).values()) {
        if (r.getAffiliation() != null && r.getAffiliation().getId().equals(affiliation)) {
          throw new PermissionException(PermissionException.Key.AFFILIATION_DELETE_NOTPERMITTED);
        }
      }
      affiliations.removeIf(a -> a.getId().equals(affiliation));
    }
  }

  @Override
  public int getActorTypeId(){
    return actortype.id();
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
    List<String> isValid = isValid();
    if (!isValid.isEmpty()) {
      logger.error("actor contains errors " + isValid.toString());
      throw new FormatException(FormatException.Key.ACTOR_ERROR, String.join(",", isValid.toString()));
    }
    return accessor.save(this, currentGroup, contributor);
  }

  @Override
  public String toString() {
    return getFullname(factory.getDefaultLanguage()) + " (" + id + ") of type " + actortype.name() + " with affiliations " + affiliations;
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();
    if (actortype == null) {
      fieldsInError.add("type");
    }
    fieldsInError.addAll(names.stream().map(n -> n.isValid(actortype)).flatMap(List::stream).collect(Collectors.toList()));
    return fieldsInError;
  }

  @Override
  public int hashCode() {
    return 71 * (id == -1L ? 83 : id.hashCode()) * getFullname(factory.getDefaultLanguage()).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConcreteActor that = (ConcreteActor) o;

    // if we have ids, use them
    if (id != null && id != -1L && that.getId() != null && that.getId() != -1L) {
      return id.equals(that.getId());
    }

    String name = getFullname(factory.getDefaultLanguage());
    if (actortype != that.actortype) {
      return false;
    }

    return name != null ? name.equals(that.getFullname(factory.getDefaultLanguage()))
        : that.getFullname(factory.getDefaultLanguage()) == null;
  }

  @Override
  public int compareTo(Actor o) {
    return getName(factory.getDefaultLanguage()).getFullName(actortype)
        .compareToIgnoreCase(o.getName(factory.getDefaultLanguage()).getFullName(o.getActorType()));
  }

  @Override
  public String getGenderAsString(){
    return factory.getPersonGender(id);
  }

  @Override
  public List<Text> getTextsWhereCitationAuthor(Long contributor, int group) {
    return accessor.getTextsWhereCitationAuthor(id, contributor, group);
  }

  @Override
  public List<Text> getTextsWhereCitationCited(Long contributor, int group) {
    return accessor.getTextsWhereCitationCited(id, contributor, group);
  }

  @Override
  public String getMinOrMaxAffiliationDate(EActorType type, boolean forMin) {
    return accessor.getMinAndMaxAffiliationDate(id, actortype, type, forMin);
  }

  @Override
  public List<ActorAffiliations> getOwners(SearchContainer query) {
    query.setId(id);
    return accessor.getOwners(query);
  }

  @Override
  public List<ActorAffiliations> getOwnedOrganizations(SearchContainer query) {
    query.setId(id);
    return accessor.getOwnedOrganizations(query);
  }

  @Override
  public List<ActorAffiliations> getAffiliationOrganizations(SearchContainer query) {
    query.setId(id);
    return accessor.getAffiliationOrganizations(query);
  }

  @Override
  public List<Citation> getTextsAuthorCitations(Long text) {
    return accessor.getTextsAuthorCitations(id, text);
  }

  @Override
  public List<ActorAffiliations> getMembers(SearchContainer query) {
    query.setId(id);
    return accessor.getMembers(query);
  }

  @Override
  public List<Actor> getOthersActorsCitations(SearchContainer query) {
    return accessor.getOthersActorsCitations(query);
  }

  @Override
  public List<Tag> getTagsCitations(SearchContainer query) {
    return accessor.getTagsCitations(query);
  }

  @Override
  public List<Text> getTextsCitations(SearchContainer query) {
    return accessor.getTextsCitations(query);
  }

  @Override
  public List<Debate> getDebatesCitations(SearchContainer query) {
    return accessor.getDebatesCitations(query);
  }

  @Override
  public List<Citation> getCitations(SearchContainer query) {
    return accessor.getCitations(query);
  }

  @Override
  public List<Citation> getCitationsFromContribution(SearchContainer query) {
    return accessor.getCitationsFromContribution(query);
  }

  /**
   * Update given name list with given name, will override existing name if a name with same language exists in given list
   * @param nameList a list of actor names to be updated
   * @param name a name to add / update in given list
   */
  protected void updateNameList(List<ActorName> nameList, ActorName name) {
    if (nameList == null) {
      nameList = new ArrayList<>();
    }

    if (name != null) {
      Optional<ActorName> optional = nameList.stream().filter(n -> n.getLang().equals(name.getLang())).findAny();
      if (optional.isPresent()) {
        ActorName existing = optional.get();
        existing.setPseudo(name.getPseudo());
        existing.setLast(name.getLast());
        existing.setFirst(name.getFirst());
      } else {
        nameList.add(name);
      }
    }
  }


  /*
   * private helpers
   */

  /**
   * Get an actor name for given language, will default to english, or get the first found one as last resort
   *
   * @param lang a 2-char iso-639-1 language code
   * @return either the actor name in given language, or in English if not found and finally in any existing language if none found
   */
  private ActorName getActorName(String lang) {
    Optional<ActorName> name = names.stream().filter(n -> n.getLang().equals(lang)).findAny();
    ActorName fullname;
    if (name.isPresent()) {
      fullname = name.get();
    } else {
      name = names.stream().filter(n -> "en".equals(n.getLang())).findAny();
      fullname = name.orElseGet(() -> !names.isEmpty() ? names.get(0) : factory.getActorName(lang));
    }
    return fullname;
  }
}
