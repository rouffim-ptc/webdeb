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

package be.webdeb.core.impl.contribution;

import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.link.PositionLinkType;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.*;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.ConcretePositionLinkType;
import be.webdeb.core.impl.contribution.link.*;
import be.webdeb.core.impl.contributor.place.ConcretePlace;
import be.webdeb.core.impl.contributor.place.ConcretePlaceType;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;

import be.webdeb.presentation.web.controllers.account.ClaimHolder;
import be.webdeb.util.ValuesHelper;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements common functions to handle all type of contributions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class AbstractContributionFactory<T extends ContributionAccessor> implements ContributionFactory {

  protected static final Logger logger = play.Logger.underlying();

  private Map<String, Language> languages = null;
  private Map<Integer, ModificationStatus> modificationStatuses = null;
  private Map<Integer, JustificationLinkType> justificationTypes = null;
  private Map<Integer, PositionLinkType> positionTypes = null;
  private Map<Integer, SimilarityLinkType> similarityTypes = null;

  /**
   * Default language for names
   */
  private static final String DEFAULT_LANG = "fr";

  @Inject
  protected T accessor;

  @Inject
  protected ContributorFactory contributorFactory;

  @Inject
  protected ValuesHelper values;

  private Map<Integer, ContributionType> contributionTypes;
  private Map<Integer, ValidationState> validationStates;

  @Override
  public Contribution retrieve(Long id, EContributionType type) {
    return accessor.retrieve(id, type);
  }

  @Override
  public Contribution retrieveContribution(Long id) {
    return accessor.retrieveContribution(id);
  }

  @Override
  public ContextContribution retrieveContextContribution(Long id) {
    return accessor.retrieveContextContribution(id);
  }

  @Override
  public Contribution retrieveWithHit(Long id) {
    return accessor.retrieve(id, true);
  }

  @Override
  public synchronized List<ContributionType> getContributionTypes() {
    if (contributionTypes == null) {
      contributionTypes = accessor.getContributionTypes().stream().collect(Collectors.toMap(ContributionType::getType, t -> t));
    }
    return new ArrayList<>(contributionTypes.values());
  }

  @Override
  public synchronized ContributionType getContributionType(EContributionType id) {
    if (contributionTypes == null) {
      contributionTypes = new LinkedHashMap<>();
      accessor.getContributionTypes().forEach(e -> contributionTypes.put(e.getType(), e));
    }
    return contributionTypes.get(id.id());
  }

  @Override
  public ContributionType createContributionType(Integer id, Map<String, String> i18names) {
    return new ConcreteContributionType(id, i18names);
  }

  @Override
  public ContributionHistory createHistory(Contributor contributor, EModificationStatus status, String trace, Date version) {
    return new ConcreteContributionHistory(contributor, status, trace, version, this);
  }

  @Override
  public ContributionHistory getCreator(Long id) {
    return accessor.getCreator(id);
  }

  @Override
  public Contributor getLastContributorInGroup(Long id, int group) {
    return accessor.getLastContributorInGroup(id, group);
  }

  @Override
  public Place findPlace(Long id) {
    return accessor.findPlace(id);
  }

  @Override
  public List<Place> findPlace(String name, int fromIndex, int toIndex) {
    return accessor.findPlace(name, fromIndex, toIndex);
  }

  @Override
  public List<Contribution> findByValue(String value, int group, int fromIndex, int toIndex) {
    return accessor.findByValue(value, group, fromIndex, toIndex);
  }

  @Override
  public List<Contribution> findByCriteria(List<Map.Entry<EQueryKey, String>> criteria, boolean strict, int fromIndex, int toIndex) {
    return accessor.findByCriteria(criteria, strict, fromIndex, toIndex);
  }

  @Override
  public List<Contribution> getLatestEntries(EContributionType type, Long contributor, int amount, int group) {
    return accessor.getLatestEntries(type, contributor, amount, group);
  }

  @Override
  public List<Contribution> getPopularEntries(EContributionType type, Long contributor, int group, int fromIndex, int toIndex, EOrderBy orderBy) {
    return accessor.getPopularEntries(type, contributor, group, fromIndex, toIndex, orderBy);
  }

  @Override
  public long getAmountOf(EContributionType type, int group) {
    return accessor.getAmountOf(type, group);
  }

  @Override
  public ValuesHelper getValuesHelper() {
    return values;
  }

  @Override
  public void saveMarkings(List<Contribution> contributions) throws PersistenceException {
    accessor.saveMarkings(contributions);
  }

  @Override
  public boolean merge(Long origin, Long replacement, Long contributor) throws PermissionException, PersistenceException {
    return accessor.merge(origin, replacement, contributor);
  }

  @Override
  public List<ContributionHistory> getHistory(Long contribution) {
    return accessor.getHistory(contribution);
  }

  @Override
  public String getDefaultLanguage() {
    return DEFAULT_LANG;
  }

  @Override
  public synchronized List<WarnedWord> getWarnedWords(int contextType, int type, String lang) {
    Map<Integer, WarnedWord>warnedWords =
            accessor.getWarnedWords(contextType, type, lang).stream().collect(Collectors.toMap(WarnedWord::getId, e -> e));
    return new ArrayList<>(warnedWords.values());
  }

  @Override
  public WarnedWord createWarnedWord(int id, String title, String lang, int type, int contextType) throws FormatException {
    return new ConcreteWarnedWord(id, title, getLanguage(lang), type, contextType);
  }

  @Override
  public Long retrievePlaceContinentCode(String code){
    return accessor.retrievePlaceContinentCode(code);
  }

  @Override
  public Place retrievePlace(Long placeId) {
    return accessor.retrievePlace(placeId);
  }

  @Override
  public Long retrievePlaceByGeonameIdOrPlaceId(Long geonameId, Long placeId){
    return accessor.retrievePlaceByGeonameIdOrPlaceId(geonameId, placeId);
  }

  @Override
  public Place createSimplePlace(Long idPlace){
    return new ConcretePlace(idPlace);
  }

  @Override
  public Place createPlace(Long idPlace, Long geonameId, String code, String latitude, String longitude, Map<String, String> names){
    return new ConcretePlace(idPlace, geonameId, code, latitude, longitude, names);
  }

  @Override
  public PlaceType createPlaceType(int idPlaceType, Map<String, String> i18names){
    return new ConcretePlaceType(idPlaceType, i18names);
  }

  @Override
  public PlaceType findPlaceTypeByCode(int code){
    return accessor.findPlaceTypeByCode(code);
  }

  @Override
  public Set<Tag> getContributionsTags(Long contribution) {
    return accessor.getContributionsTags(contribution);
  }

  @Override
  public synchronized List<ValidationState> getValidationStates() {
    if (validationStates == null) {
      validationStates = accessor.getValidationStates().stream().collect(Collectors.toMap(ValidationState::getType, e -> e));
    }
    return new ArrayList<>(validationStates.values());
  }

  @Override
  public ValidationState createValidationState(int state, Map<String, String> i18names) {
    return new ConcreteValidationState(state, i18names);
  }

  @Override
  public ValidationState getValidationState(int state) throws FormatException {
    if (validationStates == null) {
      getValidationStates();
    }
    if (!validationStates.containsKey(state)) {
      throw new FormatException(FormatException.Key.UNKNOWN_VALIDATION_STATE, String.valueOf(state));
    }
    return validationStates.get(state);
  }

  @Override
  public ValidationState getValidationState(boolean state) {
    if (validationStates == null) {
      getValidationStates();
    }
    return validationStates.get(state ? EValidationState.VALIDATED.id() : EValidationState.INVALIDATED.id());
  }

  @Override
  public List<Place> getContributionsPlaces(Long contribution){
    return accessor.getContributionsPlaces(contribution);
  }

  @Override
  public ExternalContribution retrieveExternal(Long id) {
    return accessor.retrieveExternal(id);
  }

  @Override
  public List<ExternalContribution> getExternalContributionsByExternalSource(Long contributor, int externalSource, int maxResults) {
    return accessor.getExternalContributionsByExternalSource(contributor, externalSource, maxResults);
  }

  @Override
  public void  updateDiscoveredExternalContributionState(Long id, boolean rejected) throws PersistenceException {
    accessor.updateDiscoveredExternalState(id, rejected);
  }

  @Override
  public synchronized List<Language> getLanguages() {
    if (languages == null) {
      languages = accessor.getLanguages().stream().collect(Collectors.toMap(Language::getCode, e -> e));
    }
    return new ArrayList<>(languages.values());
  }

  @Override
  public Language getLanguage(String code) throws FormatException {
    if (languages == null) {
      getLanguages();
    }

    if (!languages.containsKey(code)) {
      throw new FormatException(FormatException.Key.UNKNOWN_LANGUAGE, code != null ? code : "");
    }
    return languages.get(code);
  }

  @Override
  public Language createLanguage(String code, Map<String, String> i18names) {
    return new ConcreteLanguage(code, i18names);
  }

  @Override
  public ModificationStatus getModificationStatus(int status) throws FormatException {
    if (modificationStatuses == null) {
      getModificationStatus();
    }

    if (!modificationStatuses.containsKey(status)) {
      throw new FormatException(FormatException.Key.UNKNOWN_MODIFICATION_STATUS, String.valueOf(status));
    }
    return modificationStatuses.get(status);
  }

  @Override
  public ModificationStatus createModificationStatus(int status, Map<String, String> i18names) {
    return new ConcreteModificationStatus(status, i18names);
  }

  @Override
  public synchronized List<ModificationStatus> getModificationStatus() {
    if (modificationStatuses == null) {
      modificationStatuses = accessor.getModificationStatus().stream().collect(Collectors.toMap(ModificationStatus::getType, e -> e));
    }
    return new ArrayList<>(modificationStatuses.values());
  }

  @Override
  public List<Long> getAllIdByContributionType(EContributionType type) {
    return accessor.getAllIdByContributionType(type);
  }

  @Override
  public String getTechnicalName(String id, String value, String lang) {
    return accessor.getTechnicalName(id, value, lang);
  }

  @Override
  public boolean linkAlreadyExists(Long origin, Long destination) {
    return accessor.linkAlreadyExists(origin, destination);
  }

  @Override
  public boolean citationJustificationLinkAlreadyExists(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade) {
    return accessor.citationJustificationLinkAlreadyExists(contextId, subContextId, categoryId, superArgumentId, citationId, shade);
  }

  @Override
  public CitationJustification findCitationJustificationLink(Long contextId, Long subContextId, Long categoryId, Long superArgumentId, Long citationId, int shade) {
    return accessor.findCitationJustificationLink(contextId, subContextId, categoryId, superArgumentId, citationId, shade);
  }

  @Override
  public int getMaxCitationJustificationLinkOrder(Long context, Long subContext, Long category, Long argument, int shade) {
    return accessor.getMaxCitationJustificationLinkOrder(context, subContext, category, argument, shade);
  }

  @Override
  public JustificationLinkType createJustificationLinkType(int type, Map<String, String> i18names) {
    return new ConcreteJustificationLinkType(type, i18names);
  }

  @Override
  public JustificationLinkType getJustificationLinkType(int shade) throws FormatException {
    if(justificationTypes == null){
      getJustificationLinkTypes();
    }
    if (!justificationTypes.containsKey(shade)) {
      throw new FormatException(FormatException.Key.UNKNOWN_JUSTIFICATION_SHADE, String.valueOf(shade));
    }
    return justificationTypes.get(shade);
  }

  @Override
  public List<JustificationLinkType> getJustificationLinkTypes() {
    if(justificationTypes == null){
      justificationTypes = accessor.getJustificationLinkTypes().stream().collect(Collectors.toMap(JustificationLinkType::getType, e -> e));
    }
    return new ArrayList<>(justificationTypes.values());
  }

  @Override
  public PositionLinkType createPositionLinkType(int type, Map<String, String> i18names) {
    return new ConcretePositionLinkType(type, i18names);
  }

  @Override
  public PositionLinkType getPositionLinkType(int shade) throws FormatException {
    if(positionTypes == null){
      getPositionLinkTypes();
    }
    if (!positionTypes.containsKey(shade)) {
      throw new FormatException(FormatException.Key.UNKNOWN_POSITION_SHADE, String.valueOf(shade));
    }
    return positionTypes.get(shade);
  }

  @Override
  public List<PositionLinkType> getPositionLinkTypes() {
    if(positionTypes == null){
      positionTypes = accessor.getPositionLinkTypes().stream().collect(Collectors.toMap(PositionLinkType::getType, e -> e));
    }
    return new ArrayList<>(positionTypes.values());
  }

  @Override
  public SimilarityLinkType createSimilarityLinkType(int type, Map<String, String> i18names) {
    return new ConcreteSimilarityLinkType(type, i18names);
  }

  @Override
  public SimilarityLinkType getSimilarityLinkType(int shade) throws FormatException {
    if(similarityTypes == null) {
      getSimilarityLinkTypes();
    }
    if (!similarityTypes.containsKey(shade)) {
      throw new FormatException(FormatException.Key.UNKNOWN_SIMILARITY_SHADE, String.valueOf(shade));
    }
    return similarityTypes.get(shade);
  }

  @Override
  public List<SimilarityLinkType> getSimilarityLinkTypes() {
    if(similarityTypes == null) {
      similarityTypes = accessor.getSimilarityLinkTypes().stream().collect(Collectors.toMap(SimilarityLinkType::getType, e -> e));
    }
    return new ArrayList<>(similarityTypes.values());
  }

  /**
   * Make shade reader friendly by avoiding shade ending vowel when title starts with a vowel
   *
   * @param shade a shade term
   * @param follower the string that will follow the shade
   * @param lang the language code (2-char ISO) used for the shade term
   * @return a user friendly shade term (including space if needed)
   */
  @Override
  public String makeShadeReaderFriendly(String shade, String follower, String lang) {
    if ("fr".equals(lang) && !follower.equals("")) {
      StringBuilder friendlyShade = new StringBuilder(shade);

      if (shade.endsWith(" de") || shade.endsWith(" que") || shade.equals("Je")) {
        String temp = Normalizer.normalize(follower, Normalizer.Form.NFD);
        temp = temp.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        if ("aeiou".indexOf(Character.toLowerCase(temp.charAt(0))) >= 0) {
          friendlyShade.replace(friendlyShade.length() - 1, friendlyShade.length(), "'");
          return friendlyShade.toString();
        }
      }

      // other cases
      return friendlyShade.append(" ").toString();
    } else {
      return shade + " ";
    }
  }

  @Override
  public void createFakeCitations(int nbCitations){
    accessor.createFakeCitations(nbCitations);
  }

  @Override
  public void createFakePositions(int nbDebates, int nbPositionsPerDebate) {
    accessor.createFakePositions(nbDebates, nbPositionsPerDebate);
  }

  @Override
  public void updateContributionsRelevance() {
    accessor.updateContributionsRelevance();
  }

  @Override
  public boolean claimContribution(Long contribution, Long contributor, String url, String comment, EClaimType type, int group) {
    return accessor.claimContribution(contribution, contributor, url, comment, type, group);
  }

  @Override
  public boolean deleteClaim(Long contribution, Long contributor) {
    return accessor.deleteClaim(contribution, contributor);
  }

  @Override
  public List<ClaimHolder> retrieveClaims(Long contributor, int fromIndex, int toIndex, String lang) {
    return accessor.retrieveClaims(contributor, fromIndex, toIndex, lang);
  }

  @Override
  public boolean contributionCanBeEdited(Long contributorId, Long contributionId, int groupId) {
    return accessor.contributionCanBeEdited(contributorId, contributionId, groupId);
  }
}
