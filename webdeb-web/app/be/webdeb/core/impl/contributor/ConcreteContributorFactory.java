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

package be.webdeb.core.impl.contributor;

import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.picture.ContributorPictureSource;
import be.webdeb.core.api.contributor.picture.PictureLicenceType;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.core.impl.contribution.ConcreteContributionToExplore;
import be.webdeb.core.impl.contribution.picture.ConcreteContributorPicture;
import be.webdeb.core.impl.contribution.picture.ConcreteContributorPictureSource;
import be.webdeb.core.impl.contribution.picture.ConcretePictureLicenceType;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;
import be.webdeb.util.ValuesHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This factory creates Contributor objects or retrieve them from the database. It also manages groups.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteContributorFactory implements ContributorFactory {

  @Inject
  private ContributorAccessor contributorAccessor;

  @Inject
  protected ValuesHelper values;

  private Contributor defaultContributor;

  private GroupSubscription defaultGroup;

  private List<GroupColor> groupColors = null;

  private Map<Integer, ContributorPictureSource> contributorPictureSources = null;
  private Map<Integer, PictureLicenceType> pictureLicenceTypes = null;

  @Override
  public Contributor authenticate(String emailOrPseudo, String password) throws TokenExpiredException {
    return contributorAccessor.authenticate(emailOrPseudo, password);
  }

  @Override
  public TmpContributor tmpauthenticate(String pseudo, String password) throws TokenExpiredException {
    return contributorAccessor.tmpauthenticate(pseudo, password);
  }

  @Override
  public ContributorPicture getContributorPicture() {
    return new ConcreteContributorPicture(this);
  }

  @Override
  public Contributor tokenAuthentication(String emailOrPseudo, String password) {
    return contributorAccessor.tokenAuthentication(emailOrPseudo, password);
  }

  @Override
  public boolean checkAuthTokenValidity(String emailOrPseudo, String token) {
    return contributorAccessor.checkAuthTokenValidity(emailOrPseudo, token);
  }

  @Override
  public Contributor retrieveContributor(Long id) {
    return id != null && id != -1L ? contributorAccessor.retrieve(id) : null;
  }

  @Override
  public Contributor retrieveContributor(String emailOrPseudo) {
    return contributorAccessor.retrieve(emailOrPseudo);
  }

  @Override
  public Contributor retrieveContributor(String openid, EOpenIdType openidType) {
    return contributorAccessor.retrieve(openid, openidType);
  }

  @Override
  public TmpContributor retrieveTmp(String pseudo) {
    return contributorAccessor.retrieveTmp(pseudo);
  }

  @Override
  public TmpContributor retrieveTmp(Long id) {
    return contributorAccessor.retrieveTmp(id);
  }

  @Override
  public Contributor getContributor() {
    return new ConcreteContributor(contributorAccessor);
  }

  @Override
  public TmpContributor getTmpContributor() {
    return new ConcreteTmpContributor(contributorAccessor);
  }

  @Override
  public Contributor retrieveContributorByAuthToken(String emailOrPseudo, String token) {
      return contributorAccessor.retrieveByAuthToken(emailOrPseudo, token);
  }

  @Override
  public Contributor retrieveContributorByConfirmationToken(String token) {
    return contributorAccessor.retrieveByConfirmationToken(token);
  }

  @Override
  public Contributor retrieveContributorByInvitation(String token) {
    return contributorAccessor.retrieveByInvitation(token);
  }

  @Override
  public ContributorPicture retrieveContributorPicture(Long id) {
    return contributorAccessor.retrieveContributorPicture(id);
  }

  @Override
  public Contributor getDefaultContributor() {
    if(defaultContributor == null){
      defaultContributor = contributorAccessor.getDefaultContributor();
    }
    return defaultContributor;
  }

  @Override
  public Group getGroup() {
    return new ConcreteGroup(contributorAccessor);
  }

  @Override
  public Group retrieveGroup(int id) {
    return contributorAccessor.retrieveGroup(id);
  }

  @Override
  public GroupSubscription retrieveGroupSubscription(String emailOrPseudo, String groupName) {
    return contributorAccessor.retrieveGroupSubscription(emailOrPseudo, groupName);
  }

  @Override
  public GroupSubscription retrieveGroupSubscription(Long contributorId, Integer groupId) {
    return contributorAccessor.retrieveGroupSubscription(contributorId, groupId);
  }

  @Override
  public GroupSubscription getDefaultGroup() {
    if (defaultGroup == null) {
      defaultGroup = contributorAccessor.getDefaultGroup();
    }
    return defaultGroup;
  }

  @Override
  public List<Group> findGroups(String query, boolean openOnly) {
    return contributorAccessor.retrieveGroups(query, openOnly);
  }

  @Override
  public List<Group> findGroups(String query, boolean openOnly, int fromIndex, int toIndex) {
    return contributorAccessor.retrieveGroups(query, openOnly, fromIndex, toIndex);
  }

  @Override
  public GroupSubscription getGroupSubscription() {
    return new ConcreteGroupSubscription(contributorAccessor);
  }

  @Override
  public List<EPermission> getPermissionForRole(EContributorRole role) {
    return contributorAccessor.getPermissionForRole(role);
  }

  @Override
  public List<Contributor> findContributorsByRole(EContributorRole role) {
    return contributorAccessor.findContributorsByRole(role);
  }

  @Override
  public List<Contributor> findContributors(String query) {
    return contributorAccessor.findContributors(query);
  }

  @Override
  public List<Contributor> findContributors(String query, int fromIndex, int toIndex) {
    return contributorAccessor.findContributors(query, fromIndex, toIndex);
  }

  @Override
  public List<Contribution> searchContributorContributions(String searchText, Long contributor, int  fromIndex, int toIndex) {
    return contributorAccessor.searchContributorContributions(searchText, contributor, fromIndex, toIndex);
  }

  @Override
  public List<ExternalContribution> getContributorExternalContributions(Long contributor, int fromIndex, int toIndex, String sourceName){
    return contributorAccessor.getContributorExternalContributions(contributor, fromIndex, toIndex, sourceName);
  }

  @Override
  public Map<EContributionType, Long> getContributionsCount(Long contributor){
    return contributorAccessor.getContributionsCount(contributor);
  }

  @Override
  public GroupColor findGroupColorByCode(String code){
    return contributorAccessor.findGroupColorByCode(code);
  }

  @Override
  public List<GroupColor> getGroupColors() {
    if (groupColors == null) {
      groupColors = contributorAccessor.getGroupColors();
    }
    return groupColors;
  }

  @Override
  public GroupColor createGroupColor(Integer idColor, String colorCode){
    return new ConcreteGroupColor(idColor, colorCode);
  }

  @Override
  public void userIsWarnedAboutBrowser(Long user){
    contributorAccessor.userIsWarnedAboutBrowser(user);
  }

  @Override
  public void setFollowGroup(int group, Long contributor, boolean follow) throws PersistenceException {
      contributorAccessor.setFollowGroup(group, contributor, follow);
  }

  @Override
  public Contributor getContributorByToken(String emailOrPseudo, String token) {
    return retrieveContributorByAuthToken(emailOrPseudo, token);
  }

  @Override
  public String hashPassword(String password) throws PersistenceException {
    return contributorAccessor.hashPassword(password);
  }

  @Override
  public boolean checkPassword(String clear, String hashed) {
    return contributorAccessor.checkPassword(clear, hashed);
  }

  @Override
  public List<ModelDescription> getModelDescription() {
    return contributorAccessor.getModelDescription();
  }

  @Override
  public List<List<String>> executeApiQuery(Map<String,String[]> query) {
    return contributorAccessor.executeApiQuery(query);
  }

  @Override
  public boolean deleteContributor(Long idContributor, String password) {
    return contributorAccessor.deleteContributor(idContributor, password);
  }

  @Override
  public List<ContributionToExplore> getContributionsToExploreForGroup(int type, int group) {
    return contributorAccessor.getContributionsToExploreForGroup(type, group);
  }

  @Override
  public ContributionToExplore getContributionToExplore() {
    return new ConcreteContributionToExplore(contributorAccessor);
  }

  @Override
  public ContributionToExplore retrieveContributionToExplore(Long id) {
    return contributorAccessor.retrieveContributionToExplore(id);
  }

  @Override
  public void deleteContributionToExplore(Set<Long> idsToKeep, EContributionType type, int group) {
    contributorAccessor.deleteContributionToExplore(idsToKeep, type, group);
  }

  @Override
  public Advice getAdvice() {
    return new ConcreteAdvice(contributorAccessor);
  }

  @Override
  public List<Advice> getAdvices() {
    return contributorAccessor.getAdvices();
  }

  @Override
  public void deleteAdvices(Set<Integer> idsToKeep) {
    contributorAccessor.deleteAdvices(idsToKeep);
  }

  @Override
  public List<Group> getAllGroups() {
    return contributorAccessor.getAllGroups();
  }

  @Override
  public ContributorPictureSource getContributorPictureSource(int source) throws FormatException {
    if (contributorPictureSources == null) {
      getContributorPictureSources();
    }
    if (!contributorPictureSources.containsKey(source)) {
      throw new FormatException(FormatException.Key.UNKNOWN_PICTURE_SOURCE_TYPE, String.valueOf(source));
    }
    return contributorPictureSources.get(source);
  }

  @Override
  public ContributorPictureSource createContributorPictureSource(int source, Map<String, String> i18names) {
    return new ConcreteContributorPictureSource(source, i18names);
  }

  @Override
  public List<ContributorPictureSource> getContributorPictureSources() {
    if (contributorPictureSources == null) {
      contributorPictureSources = contributorAccessor.getContributorPictureSources().stream().collect(Collectors.toMap(ContributorPictureSource::getType, e -> e));
    }
    return new ArrayList<>(contributorPictureSources.values());
  }

  @Override
  public PictureLicenceType getPictureLicenceType(int type) throws FormatException {
    if(pictureLicenceTypes == null) {
      getPictureLicenceTypes();
    }
    if (!pictureLicenceTypes.containsKey(type)) {
      throw new FormatException(FormatException.Key.UNKNOWN_PICTURE_LICENCE_TYPE, String.valueOf(type));
    }
    return pictureLicenceTypes.get(type);
  }

  @Override
  public PictureLicenceType createPictureLicenceType(int type, Map<String, String> i18names) {
    return new ConcretePictureLicenceType(type, i18names);
  }

  @Override
  public List<PictureLicenceType> getPictureLicenceTypes() {
    if (pictureLicenceTypes == null) {
      pictureLicenceTypes = contributorAccessor.getPictureLicenceTypes().stream().collect(Collectors.toMap(PictureLicenceType::getType, e -> e));
    }
    return new ArrayList<>(pictureLicenceTypes.values());
  }

  @Override
  public boolean contributorIsPedagogicForGroupAndContribution(Long contributorId, Long contributionId) {
    return contributorAccessor.contributorIsPedagogicForGroupAndContribution(contributorId, contributionId);
  }

  @Override
  public int getNumberOfClaims(Long contributor) {
    return contributorAccessor.getNumberOfClaims(contributor);
  }

  @Override
  public ValuesHelper getValuesHelper() {
    return values;
  }
}
