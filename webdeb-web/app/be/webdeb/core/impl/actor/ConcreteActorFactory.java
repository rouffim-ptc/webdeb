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
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.core.impl.helper.ActorAlliesOpponents;
import be.webdeb.core.impl.helper.ActorDistance;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.AffiliationAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This factory handles common functions regarding all types of actors.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteActorFactory extends AbstractContributionFactory<ActorAccessor> implements ActorFactory {

  @Inject
  private AffiliationAccessor affAccessor;

  private Map<String, Gender> genders;
  private Map<String, Country> countries;
  private Map<Integer, LegalStatus> legalStatuses;
  private Map<Integer, BusinessSector> businessSectors;
  private Map<Integer, PrecisionDateType> precisionDateTypes;
  private Map<Integer, ProfessionType> professionTypes;

  private Map<Integer, ActorType> actorTypes;

  private Map<Integer, AffiliationType> affiliationTypes;

  @Override
  public Actor getActor() {
    return new ConcreteActor(this, accessor, contributorFactory);
  }

  @Override
  public Person getPerson() {
    return new ConcretePerson(this, accessor, contributorFactory);
  }

  @Override
  public String getPersonGender(Long idActor) {
    return accessor.getGenderActor(idActor);
  }

  @Override
  public Organization getOrganization() {
    return new ConcreteOrganization(this, accessor, contributorFactory);
  }

  @Override
  public ExternalAuthor getExternalAuthor() {
    return new ConcreteExternalAuthor(this);
  }

  @Override
  public List<Actor> getAllActorOrganizations(){
    return accessor.getAllActorOrganizations();
  }

  @Override
  public ActorName getActorName(String lang) {
    return new ConcreteActorName(lang);
  }

  @Override
  public ActorRole getActorRole(Actor actor, Contribution contribution) {
    return new ConcreteActorRole(actor, contribution);
  }

  @Override
  public ActorRole findActorRole(Long actor, Long contribution, EContributionType type){
    return accessor.findActorRole(retrieve(actor), retrieve(contribution, type));
  }

  @Override
  public List<Actor> findByName(String name, EActorType type) {
    return accessor.findByName(name, type);
  }

  @Override
  public List<String> findPartyMemberByName(String name, String lang) {
    return accessor.findPartyMemberByName(name, lang);
  }

  @Override
  public List<Actor> findByFullname(String fullname, EActorType type) {
    return accessor.findByFullname(fullname, type);
  }

  @Override
  public Actor retrieve(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, false) : null;
  }

  @Override
  public List<Actor> retrieveAll(List<Long> ids) {
    return accessor.retrieveAll(ids);
  }

  @Override
  public Actor retrieveWithHit(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, true) : null;
  }

  @Override
  public Affiliation getAffiliation() {
    return new ConcreteAffiliation(this, affAccessor, contributorFactory);
  }

  @Override
  public Affiliation getAffiliation(Long id) {
    return affAccessor.retrieve(id, false);
  }

  /*
   * PREDEFINED VALUES
   */

  @Override
  public synchronized List<ActorType> getActorTypes() {
    if (actorTypes == null) {
      actorTypes = accessor.getActorTypes().stream().collect(Collectors.toMap(ActorType::getType, t -> t));
    }
    return new ArrayList<>(actorTypes.values());
  }

  @Override
  public ActorType getActorType(int type) throws FormatException {
    if (actorTypes == null) {
      getActorTypes();
    }
    if (!actorTypes.containsKey(type)) {
      throw new FormatException(FormatException.Key.UNKNOWN_ACTOR_TYPE, String.valueOf(type));
    }
    return actorTypes.get(type);
  }

  @Override
  public ActorType createActorType(int type, Map<String, String> i18names) {
    return new ConcreteActorType(type, i18names);
  }

  @Override
  public ProfessionType createProfessionType(int type, Map<String, String> i18names) {
    return new ConcreteProfessionType(type, i18names);
  }

  @Override
  public ProfessionType getProfessionType(int type) throws FormatException {
    if (professionTypes == null) {
      getProfessionTypes();
    }
    if (!professionTypes.containsKey(type)) {
      throw new FormatException(FormatException.Key.UNKNOWN_PROFESSION, String.valueOf(type));
    }
    return professionTypes.get(type);
  }

  @Override
  public List<ProfessionType> getProfessionTypes() {
    if (professionTypes == null) {
      professionTypes = accessor.getProfessionTypes().stream().collect(Collectors.toMap(ProfessionType::getType, t -> t));
    }
    return new ArrayList<>(professionTypes.values());
  }

  @Override
  public PrecisionDateType createPrecisionDateType(int type, Map<String, String> i18names) {
    return new ConcretePrecisionDateType(type, i18names);
  }

  @Override
  public PrecisionDateType getPrecisionDateType(int type) throws FormatException {
    if (precisionDateTypes == null) {
      getPrecisionDateTypes();
    }
    if (!precisionDateTypes.containsKey(type)) {
      throw new FormatException(FormatException.Key.UNKNOWN_PRECISION_DATE_TYPE, String.valueOf(type));
    }
    return precisionDateTypes.get(type);
  }

  @Override
  public List<PrecisionDateType> getPrecisionDateTypes() {
    if (precisionDateTypes == null) {
      precisionDateTypes = accessor.getPrecisionDateTypes().stream().collect(Collectors.toMap(PrecisionDateType::getType, t -> t));
    }
    return new ArrayList<>(precisionDateTypes.values());
  }

  @Override
  public synchronized List<Gender> getGenders() {
    if (genders == null) {
      genders = accessor.getGenders().stream().collect(Collectors.toMap(Gender::getCode, e -> e));
    }
    return new ArrayList<>(genders.values());
  }

  @Override
  public Gender getGender(String id) throws FormatException {
    if (genders == null) {
      getGenders();
    }

    if (!genders.containsKey(id)) {
      throw new FormatException(FormatException.Key.UNKNOWN_GENDER, id);
    }
    return genders.get(id);
  }

  @Override
  public Gender createGender(String id, Map<String, String> i18names) {
    return new ConcreteGender(id, i18names);
  }

  @Override
  public AffiliationType createAffiliationType(int id, int actorType, int subtype, Map<String, String> i18names) {
    return new ConcreteAffiliationType(id, actorType, subtype, i18names);
  }

  @Override
  public AffiliationType getAffiliationType(int type) throws FormatException {
    if (affiliationTypes == null) {
      getAffiliationTypes();
    }

    if (affiliationTypes.containsKey(type)) {
      return affiliationTypes.get(type);
    }
    throw new FormatException(FormatException.Key.UNKNOWN_AFFILIATION_TYPE);
  }

  @Override
  public synchronized List<AffiliationType> getAffiliationTypes() {
    if (affiliationTypes == null) {
      affiliationTypes = accessor.getAffiliationTypes().stream().collect(Collectors.toMap(AffiliationType::getType, e -> e));
    }
    return new ArrayList<>(affiliationTypes.values());
  }

  @Override
  public Country getCountry(String id) throws FormatException {
    if (countries == null) {
      getCountries();
    }

    if (!countries.containsKey(id)) {
      throw new FormatException(FormatException.Key.UNKNOWN_TERRITORY, id);
    }
    return countries.get(id);
  }

  @Override
  public synchronized List<Country> getCountries() {
    if (countries == null) {
      countries = accessor.getCountries().stream().collect(Collectors.toMap(Country::getCode, e -> e));
    }
    return new ArrayList<>(countries.values());
  }

  @Override
  public Country createCountry(String id, Map<String, String> i18names) {
    return new ConcreteCountry(id, i18names);
  }

  @Override
  public synchronized List<LegalStatus> getLegalStatuses() {
    if (legalStatuses == null) {
      legalStatuses = accessor.getLegalStatuses().stream().collect(Collectors.toMap(LegalStatus::getType, e -> e));
    }
    return new ArrayList<>(legalStatuses.values());
  }

  @Override
  public LegalStatus getLegalStatus(int status) throws FormatException {
    if (legalStatuses == null) {
      getLegalStatuses();
    }
    if (!legalStatuses.containsKey(status)) {
      throw new FormatException(FormatException.Key.UNKNOWN_LEGALSTATUS, String.valueOf(status));
    }
    return legalStatuses.get(status);
  }

  @Override
  public LegalStatus createLegalStatus(int status, Map<String, String> i18names) {
    return new ConcreteLegalStatus(status, i18names);
  }

  @Override
  public synchronized List<BusinessSector> getBusinessSectors() {
    if (businessSectors == null) {
      businessSectors = accessor.getBusinessSectors().stream().collect(Collectors.toMap(BusinessSector::getType, e -> e));
    }
    return new ArrayList<>(businessSectors.values());
  }

  @Override
  public BusinessSector getBusinessSector(int sector) throws FormatException {
    if (businessSectors == null) {
      getBusinessSectors();
    }
    if (!businessSectors.containsKey(sector)) {
      throw new FormatException(FormatException.Key.UNKNOWN_SECTOR, String.valueOf(sector));
    }
    return businessSectors.get(sector);
  }

  @Override
  public BusinessSector createBusinessSector(int sector, Map<String, String> i18names) {
    return new ConcreteBusinessSector(sector, i18names);
  }

  @Override
  public synchronized List<Profession> getProfessions() {
    return accessor.getProfessions();
  }

  @Override
  public Profession getProfession(int id) {
    return accessor.findProfession(id);
  }

  @Override
  public Profession createProfession(int id, Map<String, Map<String, String>> i18names) {
    return createProfession(id, null, i18names);
  }

  @Override
  public Profession createProfession(int id, String lang, String gender, String title) {
    Map<String, Map<String, String>> i18names = new HashMap<>();
    i18names.put(lang, new HashMap<String, String>() {{ put(gender, title); }});
    return createProfession(id, i18names);
  }

  @Override
  public Profession createProfession(int id, EProfessionType type, boolean displayHierarchy, Map<String, Map<String, String>> i18names) {
    return new ConcreteProfession(accessor, id, type, displayHierarchy, i18names);
  }

  @Override
  public Profession createProfession(int id, EProfessionType type, Map<String, Map<String, String>> i18names) {
    return createProfession(id, type, true, i18names);
  }

  @Override
  public List<Profession> findProfessions(String term, Long idToIgnore, String lang, int fromIndex, int toIndex) {
    return accessor.findProfessions(term, idToIgnore, lang, fromIndex, toIndex);
  }

  @Override
  public List<Profession> findProfessions(String term, String lang, String gender, int type) {
    return accessor.findProfessions(term, lang, gender, type);
  }

  @Override
  public List<Profession> findProfessions(String term, String lang, int type) {
    return accessor.findProfessions(term, lang, type);
  }

  @Override
  public Profession findProfession(String name, String lang, int type) {
    return accessor.findProfession(name, lang);
  }

  @Override
  public Profession findProfession(String name, String lang) {
    return accessor.findProfession(name, lang);
  }

  @Override
  public Profession findProfession(String name, boolean strict) {
    return accessor.findProfession(name, strict);
  }

  @Override
  public int saveProfession(Profession profession) {
    return affAccessor.saveProfession(profession);
  }

  @Override
  public Profession determineSuperLink(Integer professionId){
    return accessor.determineSuperLink(professionId);
  }

  @Override
  public List<Profession> determineSubLinks(Integer professionId){
    return accessor.determineSubLinks(professionId);
  }

  @Override
  public int mergeProfessions(int profession, int professionToMerge) throws PersistenceException{
    return accessor.mergeProfessions(profession, professionToMerge);
  }

  @Override
  public void resetActorType(Long id) {
    accessor.resetActorType(id);
  }

  @Override
  public List<Affiliation> getAffiliationsRelatedTo(Long id, List<Long> actorIds) {
    return accessor.getAffiliationsRelatedTo(id, actorIds);
  }

  @Override
  public Map<ESocioGroupKey, List<ActorAlliesOpponents>> getAlliesOpponentsMap(SearchContainer query) {
    return accessor.getAlliesOpponentsMap(query);
  }

  @Override
  public Map<Integer, List<Citation>> getAlliesOpponentsCitationsMap(SearchContainer query) {
    return accessor.getAlliesOpponentsCitationsMap(query);
  }

  @Override
  public Map<ESocioGroupKey, List<ActorDistance>> getActorsDistance(SearchContainer query) {
    return accessor.getActorsDistance(query);
  }

  @Override
  public List<ActorDistance> getActorsDistanceByDebates(SearchContainer query) {
    return accessor.getActorsDistanceByDebates(query);
  }

  @Override
  public Actor random() {
    return accessor.random();
  }
}
