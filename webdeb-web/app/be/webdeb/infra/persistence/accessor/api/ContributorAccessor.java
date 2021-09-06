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

package be.webdeb.infra.persistence.accessor.api;

import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.picture.ContributorPictureSource;
import be.webdeb.core.api.contributor.picture.PictureLicenceType;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.util.ValuesHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents the accessor for contributor-related management. It allows, a.o, to retrieve a
 * Contributor from the database and manage his/her lost-change password/email tokens.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ContributorAccessor {

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
   * Retrieve a ContributorPicture by its id.
   *
   * @param id a ContributorPicture id
   * @return the ContributorPicture concrete object corresponding to the given id, null if not found
   */
  ContributorPicture retrieveContributorPicture(Long id);

  /**
   * Check the auth token validity.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @param token the token to check
   * @return return true if the token is valid for the given user
   */
  boolean checkAuthTokenValidity(String emailOrPseudo, String token);

  /**
   * Retrieve a Contributor by its email or pseudonym
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the Contributor identified by this email or pseudonym, null if not found
   */
  Contributor retrieve(String emailOrPseudo);

  /**
   * Retrieve a Contributor by its id
   *
   * @param id a Contributor id
   * @return the Contributor identified by this id, null if not found
   */
  Contributor retrieve(Long id);

  /**
   * Retrieve a Contributor by its openid
   *
   * @param openid a Contributor openid
   * @param openidType a Contributor openid type
   * @return the Contributor identified by this openid, null if not found
   */
  Contributor retrieve(String openid, EOpenIdType openidType);

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
  Contributor retrieveByAuthToken(String emailOrPseudo, String token);

  /**
   * Retrieve a contributor by its confirmation token
   *
   * @param token a confirmation token
   * @return the contributor to which the given confirmation token is assigned, null if not found
   */
  Contributor retrieveByConfirmationToken(String token);

  /**
   * Retrieve a contributor by his/her invitation
   *
   * @param token a token
   * @return the contributor to which the given token is assigned as an invitation to join a group, null if not found
   */
  Contributor retrieveByInvitation(String token);

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
   * Create a new subscription/renewal token  for given Contributor
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return a new confirmation token for this Contributor, null if no Contributor exists for given email
   */
  String newConfirmationToken(String emailOrPseudo) throws PersistenceException;

  /**
   * Get the subscription/renewal token for a given contributor. Token may be expired.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the current confirmation token for given contributorEmail, null if not found
   */
  String getConfirmationToken(String emailOrPseudo);

  /**
   * Check if the subscription/renewal token for given contributor has expired
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return true if the confirmation token has expired or if contributor was not found, false otherwise
   */
  boolean isConfirmationTokenExpired(String emailOrPseudo);

  /**
   * Create a new newsletter token for this Contributor (used for unsubscribe from newsletters) Save this token in database
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return a new newsletter token for this Contributor, null if no Contributor exists for given email
   */
  String newNewsletterToken(String emailOrPseudo) throws PersistenceException;

  /**
   * Get current newsletter token, if any (token may be expired)
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the current newsletter token for given contributorEmail, null if not found
   */
  String getNewsletterToken(String emailOrPseudo);

  /**
   * Check if the associated newsletter token has expired
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return true if the newsletter token has expired or if contributor was not found, false otherwise
   */
  boolean isNewsletterTokenExpired(String emailOrPseudo);

  /**
   * Create a new auth token for given Contributor
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return a new auth token  for this Contributor, null if no Contributor exists for given email
   */
  String newAuthToken(String emailOrPseudo);

  /**
   * Get the auth token for a given contributor. Token may be expired.
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return the auth token for given contributorEmail, null if not found
   */
  String getAuthToken(String emailOrPseudo);

  /**
   * Check if the auth token for given contributor has expired
   *
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @return true if the auth token has expired or if contributor was not found, false otherwise
   */
  boolean isAuthTokenExpired(String emailOrPseudo);

  /**
   * Save a new password for given contributor (will be hashed)
   *
   * @param contributor a contributor id
   * @param password a clear password
   */
  void changePassword(Long contributor, String password) throws PersistenceException;

  /**
   * Get the list of contributions for given contributor ordered by descending version
   *
   * @param contributor a contributor id
   * @param group a group id in which we have to retrieve the contributions of given contributor (-1 to ignore the group id)
   * @param amount the amount of contributions to retrieve, if any non-strictly positive value is passed, all contributions are retrieved
   * @return a (possibly empty) list of contributions for given contributor, if contributor is not found, an empty
   * list is returned too
   */
  List<Contribution> getContributions(Long contributor, int group, int amount);

  /**
   * Validate the given contributor, ie, contributor validated his email and may now connect to webdeb.
   *
   * @param contributor a contributor id
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @throws PersistenceException if any error occurred while persisting the validated state
   * @throws TokenExpiredException if the validation token of this contributor has expire
   */
  void validate(Long contributor, String emailOrPseudo) throws PersistenceException, TokenExpiredException;

  /**
   * Persist given Contributor into the database
   *
   * @param contributor a contributor
   * @param currentGroup the current group id from which the contributor triggered the save action (for auto-created actors)
   * @throws PersistenceException if any error occurred while saving given contributor
   */
  void save(Contributor contributor, int currentGroup) throws PersistenceException;

  /**
   * Persist given TmpContributor into the database from a project
   *
   * @param project a webdeb project
   * @param subgroup the subgroup where link the new tmpContributor
   * @param idTmpContributor the unique id for the given project and subgroup
   * @throws PersistenceException if any error occurred while saving given tmp contributor
   */
  void save(Project project, ProjectSubgroup subgroup, String idTmpContributor) throws PersistenceException;

  /**
   * Retrieve a group by its id
   *
   * @param id a group id
   * @return the group corresponding to given id, null if not found
   */
  Group retrieveGroup(int id);

  /**
   * Retrieve a list of groups having given query either in their names or description
   *
   * @param query a search query
   * @param openOnly true if only open groups may be retrieved
   * @return the (possibly empty) list of groups matching given query in their names or descritpions
   */
  List<Group> retrieveGroups(String query, boolean openOnly);

  /**
   * Retrieve a list of groups having given query either in their names or description
   *
   * @param query a search query
   * @param openOnly true if only open groups may be retrieved
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of groups matching given query in their names or descritpions
   */
  List<Group> retrieveGroups(String query, boolean openOnly, int fromIndex, int toIndex);

  /**
   * Get all WebDeb groups
   *
   * @return the list of Webdeb groups
   */
  List<Group> getAllGroups();
  
  /**
   * Find by color code
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
   * Retrieve all groups for given contributor
   *
   * @param contributor a contributor id
   * @return the (possibly empty) list of all groups of given contributor, an empty list if given contributor
   * does not exist
   */
  List<GroupSubscription> retrieveGroups(Long contributor);

  /**
   * Get the default group for all users (aka public group)
   *
   * @return the default group as a subscription object with a null contributor
   */
  GroupSubscription getDefaultGroup();

  /**
   * Retrieve all members of given group with at least given role
   *
   * @param groupId a group id
   * @param role a role id
   * @return a (possibly-empty) list of Contributors subscriptions and their Roles for given group, will be empty if group
   * is not found
   */
  List<GroupSubscription> retrieveGroupMembers(int groupId, EContributorRole role);

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
   * Set the default group for given contributor, must be already part of group, otherwise an error is raised.
   *
   * @param group a group id
   * @param contributor a contributor id
   * @throws PersistenceException if an error occured with the db or if the contributor does not belong to group.
   */
  void setDefaultGroupFor(int group, long contributor) throws PersistenceException;

  /**
   * Add given contributor to given group, will update role if given contributor was already a member.
   * Has no effect if contributor is already a member of given group with given role.
   *
   * This method may be used when contributor are joining public groups or by group owners when adding
   * members without requiring to accept invitations.
   *
   * @param group a group id
   * @param contributor a contributor id
   * @param role a role description object
   * @throws PersistenceException if any error occurred while adding member to group with given role
   */
  void addMemberInGroup(int group, Long contributor, EContributorRole role) throws PersistenceException;

  /**
   * Invite given contributor to join given group with given role. Has no effect if contributor is already a member
   * of given group (regardless of his role).
   * An invitation token will be created in database, that will be necessary to confirm subsrciption in group afterwards.
   *
   * @param group a group id
   * @param contributor a contributor id
   * @param role a role description object
   * @throws PersistenceException if any error occurred while adding contributor to invitation list
   */
  void inviteMemberInGroup(int group, Long contributor, EContributorRole role) throws PersistenceException;

  /**
   * Invite given contributor to join given group (has no effect if contributor is already a member of given group)
   * An invitation token will be created in database, that will be necessary to confirm subsrciption in group afterwards.
   * Will create an empty contributor as a side effect.
   *
   * @param group a group id
   * @param emailOrPseudo the email or the pseudonym of the contributor
   * @throws PersistenceException if any error occurred while adding given email to invitation list for group
   */
  void inviteMemberInGroup(int group, String emailOrPseudo, EContributorRole role) throws PersistenceException;

  /**
   * Set banned state of given contributor in given group (no effect if contributor is not part of group)
   *
   * @param group a group id
   * @param contributor a contributor id
   * @param revoke flag to say contributor must be revoked or re-added
   * @throws PersistenceException if any error occurred while revoking given contributor from given group
   */
  void setBannedInGroup(int group, Long contributor, boolean revoke) throws PersistenceException;

  /**
   * Set banned state of given contributor for the whole platform
   *
   * @param contributor a contributor id
   * @param revoke flag to say contributor must be revoked or re-added
   * @throws PersistenceException if any error occurred while revoking given contributor from given group
   */
  void setBanned(Long contributor, boolean revoke) throws PersistenceException;

  /**
   * Set followed state for given group with given contributor
   *
   * @param contributor a contributor id
   * @param follow flag to say group must be followed or not
   * @throws PersistenceException if any error occurred while changing the followed state
   */
  void setFollowGroup(int group, Long contributor, boolean follow) throws PersistenceException;

  /**
   * Remove a user from given group, used by users that want to leave a group..
   *
   * @param group the group id from which given user must be removed
   * @param member the member id to remove
   * @return true if given member has been removed from given group, false otherwise
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   */
  boolean removeMemberFromGroup(int group, Long member) throws PersistenceException;

  /**
   * Save given group into database. May update name, description and permissions, but not member lists.
   *
   * @param group the group to save
   * @param admin the contributor id that made the save request
   * @throws PersistenceException if any error occurred while persisting this group
   */
  void save(Group group, Long admin) throws PersistenceException;

  /**
   * Clean given group from database: remove all contributions and members subscriptions. If deleteGroup flag is true, also
   * remove subscriptions of admins and owners and delete the group itself.
   *
   * @param group the group id to clean (and maybe remove)
   * @param deleteGroup true if the group itself must also be deleted
   * @throws PersistenceException if an error occurred while communicating with the database (will often contain an explicit
   * message key and a root cause)
   */
  void clean(int group, boolean deleteGroup) throws PersistenceException;

  /**
   * Get the list of permissions associated to given role
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
   * Set the user warned about browser danger
   *
   * @param user an user id
   */
  void userIsWarnedAboutBrowser(Long user);

  /**
   * Give the admin rights to given contributor
   *
   * @param contributor a contributor id
   */
  void giveAdminRights(Long contributor);

  /**
   * Remove the admin rights to given contributor
   *
   * @param contributor a contributor id
   */
  void removeAdminRights(Long contributor);

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
   * Check if this Contributor is valid : if is validated, if he has a email, firstname, lastname and he is not deleted.
   *
   * @param contributor a contributor id
   * @return true if is a valid one
   */
  boolean isContributorValid(Long contributor);

  /**
   * Deleted the given contributor if his given password is correct
   *
   * @param idContributor a idContributor id
   * @param password the contributor password
   * @return true if the contributor is correctly deleted
   */
  boolean deleteContributor(Long idContributor, String password);

  /**
   * Get the helper class to check constraints on values
   *
   * @return the values instance
   */
  ValuesHelper getValuesHelper();

  /**
   * Get the list of contributions to explore for the given group if any
   *
   * @param type a contribution type id
   * @param group a group id
   * @return a possibly empty list of contributions to explore
   */
  List<ContributionToExplore> getContributionsToExploreForGroup(int type, int group);

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
   * Save given contributionToExplore into database.
   *
   * @param contributionToExplore the contributionToExplore to save
   * @param admin the contributor id that made the save request
   * @throws PersistenceException if any error occurred while persisting this group
   * @throws PermissionException if given admin has insufficient rights
   */
  void save(ContributionToExplore contributionToExplore, Long admin) throws PersistenceException, PermissionException;

  /**
   * Get the list of advices to help contributors
   *
   * @return a possibly empty list ofadvices
   */
  List<Advice> getAdvices();

  /**
   * Delete all advices that are not in given set of ids
   *
   * @param idsToKeep a possibly empty set of advice ids
   */
  void deleteAdvices(Set<Integer> idsToKeep);

  /**
   * Save given advice into database.
   *
   * @param advice the advice to save
   * @param contributor the contributor id that made the save request
   * @throws PersistenceException if any error occurred while persisting this group
   * @throws PermissionException if given admin has insufficient rights
   */
  void save(Advice advice, Long contributor) throws PersistenceException, PermissionException;

  /**
   * Get the list of contributor pictures added by given contributor
   *
   * @param contributor a contributor id
   * @return a possibly empty list of contributor pictures
   */
  List<ContributorPicture> getContributorPictures(Long contributor);

  /**
   * Save given contributor picture into database.
   *
   * @param contributorPicture the contributor picture to save
   * @param contributor the contributor id that made the save request
   * @throws PersistenceException if any error occurred while persisting this group
   * @throws PermissionException if given admin has insufficient rights
   */
  void save(ContributorPicture contributorPicture, Long contributor) throws PersistenceException, PermissionException;

  /**
   * Get the number of contributor in the group
   *
   * @param group a group id
   * @return the number of contributors in the group
   */
  int countNbContributors(int group);

  /**
   * Get the number of contributions in the group
   *
   * @param group a group id
   * @param type the contribution type
   * @return the number of contributions in the group
   */
  int countNbContributions(int group, EContributionType type);

  /**
   * Retrieve all contributor picture sources
   *
   * @return the list of all contributor picture sources
   */
  List<ContributorPictureSource> getContributorPictureSources();

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

}
