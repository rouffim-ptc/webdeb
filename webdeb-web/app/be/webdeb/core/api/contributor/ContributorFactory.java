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

package be.webdeb.core.api.contributor;

import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.picture.ContributorPictureSource;
import be.webdeb.core.api.contributor.picture.PictureLicenceType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.util.ValuesHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents a factory to retrieve and handle Contributor objects and Groups.
 * Among others, it allows to search for contributors by their IDs, email or invitation/validation tokens.
 *
 * It also allow to search for groups.
 *
 * Finally, it allows to retrieve all permissions assigned to a EContributorRole objects.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ContributorFactory {

  /**
   * Create a new Contributor
   *
   * @return a new Contributor instance
   */
  Contributor getContributor();

  /**
   * Create a new TmpContributor
   *
   * @return a new TmpContributor instance
   */
  TmpContributor getTmpContributor();

  /**
   * Construct an empty contributor picture instance
   *
   * @return a new ContributorPicture instance
   */
  ContributorPicture getContributorPicture();

  /**
   * Authenticate a contributor from an email or a pseudonym and clear password.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param password clear password
   * @return a contributor if given email and password matches, null otherwise
   * @throws TokenExpiredException if this contributor did not validate his account
   */
  Contributor authenticate(String emailOrPseudo, String password) throws TokenExpiredException;

  /**
   * Authenticate a tmpcontributor from a pseudonym and clear password.
   *
   * @param pseudo the pseudonym of the tmpcontributor
   * @param password clear password
   * @return a tmpcontributor if given pseudo and password matches, null otherwise
   * @throws TokenExpiredException if the project linked to this tmpcontributor is not begin or ended
   */
  TmpContributor tmpauthenticate(String pseudo, String password) throws TokenExpiredException;

  /**
   * Authenticate a contributor from an email or a pseudonym and clear password from outside of the plateform.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param password clear password
   * @return a contributor if given email and password matches, null otherwise
   */
  Contributor tokenAuthentication(String emailOrPseudo, String password);

  /**
   * Check the auth token validity.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param token the token to check
   * @return return true if the token is valid for the given user
   */
  boolean checkAuthTokenValidity(String emailOrPseudo, String token);

  /**
   * Retrieve a contributor by his/her id
   *
   * @param contributor a contributor id
   * @return the Contributor identified by the given email, or null if not found
   */
  Contributor retrieveContributor(Long contributor);

  /**
   * Retrieve a contributor by his/her email or his/her pseudonym
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the Contributor identified by the given email or pseudonym, or null if not found
   */
  Contributor retrieveContributor(String emailOrPseudo);

  /**
   * Retrieve a Contributor by its openid
   *
   * @param openid a Contributor openid
   * @param openidType a Contributor openid type
   * @return the Contributor identified by this openid, null if not found
   */
  Contributor retrieveContributor(String openid, EOpenIdType openidType);

  /**
   * Retrieve a TmpContributor by its pseudonym
   *
   * @param pseudo the pseudonym of the tmpcontributor
   * @return the TmpContributor identified by this pseudonym, null if not found
   */
  TmpContributor retrieveTmp(String pseudo);

  /**
   * Retrieve a TmpContributor by its id
   *
   * @param id a TmpContributor id
   * @return the TmpContributor identified by this id, null if not found
   */
  TmpContributor retrieveTmp(Long id);

  /**
   * Retrieve a contributor by its auth token
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param token a auth token
   * @return the contributor to which the given auth token is assigned, null if not found
   */
  Contributor retrieveContributorByAuthToken(String emailOrPseudo, String token);

  /**
   * Retrieve a contributor by his/her confirmation token
   *
   * @param token a token
   * @return the contributor to which the given token is assigned, null if not found
   */
  Contributor retrieveContributorByConfirmationToken(String token);

  /**
   * Retrieve a contributor by his/her invitation
   *
   * @param token a token
   * @return the contributor to which the given token is assigned as an invitation to join a group, null if not found
   */
  Contributor retrieveContributorByInvitation(String token);

  /**
   * Retrieve a ContributorPicture by its id.
   *
   * @param id a ContributorPicture id
   * @return the ContributorPicture concrete object corresponding to the given id, null if not found
   */
  ContributorPicture retrieveContributorPicture(Long id);

  /**
   * Get the default contributor
   *
   * @return the default contributor
   */
  Contributor getDefaultContributor();

  /**
   * Retrieve a list of contributors by theirs role
   *
   * @param role the contributor role
   * @return the (possibly empty) list of contributors with that role
   */
  List<Contributor> findContributorsByRole(EContributorRole role);

  /**
   * Retrieve a list of contributors having given query either in their names or email (query may be empty to get all)
   *
   * @param query a search query
   * @return the (possibly empty) list of contributors matching given query in their names or email
   */
  List<Contributor> findContributors(String query);

  /**
   * Retrieve a list of contributors having given query either in their names or email (query may be empty to get all)
   *
   * @param query a search query
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of contributors matching given query in their names or email
   */
  List<Contributor> findContributors(String query, int fromIndex, int toIndex);

  /**
   * Create a new Group instance
   *
   * @return a group instance
   */
  Group getGroup();

  /**
   * Retrieve a group by its id
   *
   * @param id a group id
   * @return the retrieved group if it exists, null otherwise
   */
  Group retrieveGroup(int id);

  /**
   * Retrieve a list of groups having given query either in their names or description
   *
   * @param query a search query
   * @param openOnly true if only open groups may be retrieved
   * @return the (possibly empty) list of groups matching given query in their names or descritpions
   */
  List<Group> findGroups(String query, boolean openOnly);

  /**
   * Retrieve a list of groups having given query either in their names or description
   *
   * @param query a search query
   * @param openOnly true if only open groups may be retrieved
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of groups matching given query in their names or descritpions
   */
  List<Group> findGroups(String query, boolean openOnly, int fromIndex, int toIndex);

  /**
   * Create a new group subscription instance, used to bind a contributor to a group with his/her role
   *
   * @return a subscription instance
   */
  GroupSubscription getGroupSubscription();

  /**
   * Retrieve a group subscription by given contributor email (or pseudo) and group name (both are unique in Webdeb)
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param groupName a group name
   * @return the group subscription if both given contributor and group exist and the contributor belongs to
   * this group, null otherwise
   */
  GroupSubscription retrieveGroupSubscription(String emailOrPseudo, String groupName);

  /**
   * Retrieve a group subscription by given contributor id and group id
   *
   * @param contributorId a contributor id
   * @param groupId a group id
   * @return the group subscription if both given contributor and group exist and the contributor belongs to
   * this group, null otherwise
   */
  GroupSubscription retrieveGroupSubscription(Long contributorId, Integer groupId);

  /**
   * Get the default group for all users (aka public group)
   *
   * @return the default group as a subscription object with a null contributor
   */
  GroupSubscription getDefaultGroup();

  /**
   * Get the list of permissions asssociated to given role
   *
   * @param role a role
   * @return the list of permissions associated to given role
   */
  List<EPermission> getPermissionForRole(EContributorRole role);

  /**
   * Get the list of contributor's contributions
   *
   * @param searchText a query string
   * @param contributor the contributor id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of contributor's contributions
   */
  List<Contribution> searchContributorContributions(String searchText, Long contributor, int fromIndex, int toIndex);

  /**
   * Get the list of contributor's external contributions by given source name
   *
   * @param contributor the contributor id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param sourceName the name of the external source
   * @return the list of contributor's external contributions
   */
  List<ExternalContribution> getContributorExternalContributions(Long contributor, int fromIndex, int toIndex, String sourceName);

  /**
   * Get the count of contributions by type
   *
   * @param contributor a contributor id
   * @return a map of contributions count, type, count
   */
  Map<EContributionType, Long> getContributionsCount(Long contributor);

  /**
   * Find GroupColor by color code
   *
   * @param code the color code
   * @return a group color
   */
  GroupColor findGroupColorByCode(String code);

  /**
   * Get the list of group colors
   *
   * @return a list of group colors
   */
  List<GroupColor> getGroupColors();

  /**
   * Create a new GroupColor instance
   *
   * @param idColor a groupColor id
   * @param colorCode a hexadecimal code that representing a color
   * @return the created GroupColor instance
   */
  GroupColor createGroupColor(Integer idColor, String colorCode);


  /**
   * Set the user warned about browser danger
   *
   * @param user an user id
   */
  void userIsWarnedAboutBrowser(Long user);

  /**
   * Set followed state for given group with given contributor
   *
   * @param contributor a contributor id
   * @param follow flag to say group must be followed or not
   * @throws PersistenceException if any error occurred while changing the followed state
   */
  void setFollowGroup(int group, Long contributor, boolean follow) throws PersistenceException;

  /**
   * Get a Contributor by auth token
   *
   * @param email the user email
   * @param token the auth token to authenticate user
   * @return the Contributor corresponding to the given token
   */
  Contributor getContributorByToken(String email, String token);

  /**
   * Create a hashed password from a clear text
   *
   * @param password a plain text
   * @return the hashed password
   * @throws PersistenceException if given password is null or empty
   */
  String hashPassword(String password) throws PersistenceException;

  /**
   * Check a clear password against a hashed one to check whether they are equal
   *
   * @param clear a clear text
   * @param hashed a hashed text
   * @return true if both parameters are not null and they matches
   */
  boolean checkPassword(String clear, String hashed);

  /**
   * Get the description of all interesting classes of the model to querying the persistence part.
   *
   * @return a list of ModelDescription
   */
  List<ModelDescription> getModelDescription();

  /**
   * Execute the given query to transform into a sql query to perform it into the DB et get the results as a list of list of values.
   * The first list is the keys of sql columns name.
   *
   * @param query the user query to transform into executable query
   * @return the result list, possibly empty
   */
  List<List<String>> executeApiQuery(Map<String,String[]> query);

  /**
   * Deleted the given contributor if his given password is correct
   *
   * @param idContributor a idContributor id
   * @param password the contributor password
   * @return true if the contributor is correctly deleted
   */
  boolean deleteContributor(Long idContributor, String password);

  /**
   * Get the list of contributions to explore for the given group if any
   *
   * @param type a contribution type id
   * @param group a group id
   * @return a possibly empty list of contributions to explore
   */
  List<ContributionToExplore> getContributionsToExploreForGroup(int type, int group);

  /**
   * Construct an empty ContributionToExplore instance
   *
   * @return a new ContributionToExplore instance
   */
  ContributionToExplore getContributionToExplore();

  /**
   * Retrieve a contribution to explore related to the given id
   *
   * @param id a contributionToExplore id
   * @return the corresponding contribution to explore or null if not found
   */
  ContributionToExplore retrieveContributionToExplore(Long id);

  /**
   * Delete all contributions to explore that are not in given set of ids
   *
   * @param idsToKeep a possibly empty set of contribution to explore ids
   * @param contributionType a contribution type
   * @param group a user group
   */
  void deleteContributionToExplore(Set<Long> idsToKeep, EContributionType contributionType, int group);

  /**
   * Construct an empty Advice instance
   *
   * @return a new Advice instance
   */
  Advice getAdvice();

  /**
   * Get the list of advices to help contributors
   *
   * @return a possibly empty list of advices
   */
  List<Advice> getAdvices();

  /**
   * Delete all advices that are not in given set of ids
   *
   * @param idsToKeep a possibly empty set of advice ids
   */
  void deleteAdvices(Set<Integer> idsToKeep);

  /**
   * Get all WebDeb groups
   *
   * @return the list of Webdeb groups
   */
  List<Group> getAllGroups();

  /**
   * Get a contributor picture source instance by its id
   *
   * @param source a contributor picture source id
   * @return a ContributorPictureSource instance corresponding to given source
   *
   * @throws FormatException if given id does not exist
   */
  ContributorPictureSource getContributorPictureSource(int source) throws FormatException;

  /**
   * Create a new ContributorPictureSource instance
   *
   * @param source the contributor picture source
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created ContributorPictureSource instance
   */
  ContributorPictureSource createContributorPictureSource(int source, Map<String, String> i18names);

  /**
   * Retrieve all contributor picture sources
   *
   * @return the list of all contributor picture sources
   */
  List<ContributorPictureSource> getContributorPictureSources();

  /**
   * Get a picture licence instance by its type id
   *
   * @param type a type id
   * @return a PictureLicenceType instance corresponding to given type
   *
   * @throws FormatException if given id does not exist
   */
  PictureLicenceType getPictureLicenceType(int type) throws FormatException;

  /**
   * Create a new PictureLicenceType instance
   *
   * @param type the contributor picture licence
   * @param i18names a map of pairs of the form (2-char iso-code, name)
   * @return the created PictureLicenceType instance
   */
  PictureLicenceType createPictureLicenceType(int type, Map<String, String> i18names);

  /**
   * Retrieve all contributor picture sources
   *
   * @return the list of all contributor picture sources
   */
  List<PictureLicenceType> getPictureLicenceTypes();

  /**
   * Check if given contributor is member of a pedagogic group where given contribution is
   *
   * @param contributorId a contributor id
   * @param contributionId a contribution id
   * @return true if the contributor is member of a pedagogic group where given contribution is
   */
  boolean contributorIsPedagogicForGroupAndContribution(Long contributorId, Long contributionId);

  /**
   * Get the number of contribution claims make by contributors
   *
   * @param contributor the contributor that want to see claims
   * @return the number of claims
   */
  int getNumberOfClaims(Long contributor);

  /**
   * Get the helper class to check constraints on values
   *
   * @return the values instance
   */
  ValuesHelper getValuesHelper();
}
