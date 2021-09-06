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

import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.actor.Individual;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;

import java.util.Date;
import java.util.List;

/**
 * This interface represents a Contributor of the webdeb system. Contributors are individual persons that
 * enrich the webdeb database with any type of contributions. Contributors may have Affiliations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface Contributor extends Individual, Comparable<Contributor> {

  /**
   * Retrieve the Contributor id
   *
   * @return the Contributor id
   */
  Long getId();

  /**
   * Set the Contributor id
   *
   * @param id the Contributor id to set
   */
  void setId(Long id);

  /**
   * Get this Contributor's email address
   *
   * @return the email
   */
  String getEmail();

  /**
   * Set this Contributor's email address
   *
   * @param email the email address
   */
  void setEmail(String email) throws FormatException;

  /**
   * Get this Contributor's pseudonym
   *
   * @return the pseudonym
   */
  String getPseudo();

  /**
   * Set this Contributor's pseudonym
   *
   * @param pseudo the pseudonym
   */
  void setPseudo(String pseudo) throws FormatException;

  /**
   * Get this Contributor's first name
   *
   * @return the first name
   */
  String getFirstname();

  /**
   * Set this Contributor's first name
   *
   * @param firstname the first name
   */
  void setFirstname(String firstname);

  /**
   * Get this Contributor's last name
   *
   * @return the last name
   */
  String getLastname();

  /**
   * Set this Contributor's last name
   *
   * @param lastname the last name
   */
  void setLastname(String lastname);

  /**
   * Get this Contributor's birth year
   *
   * @return the birth year or -1 if unknown
   */
  Integer getBirthyear();

  /**
   * Set this Contributor's birth year
   *
   * @param year a 4-digit number
   * @throws FormatException if the given year does not look valid
   */
  void setBirthyear(Integer year) throws FormatException;

  /**
   * Get this Contributor's password (hashed)
   *
   * @return the hashed password
   */
  String getPassword();

  /**
   * Set this Contributor's password (hashed, except if this.getId is unset, then password is clear)
   *
   * @param password a hashed password
   */
  void setPassword(String password);

  /**
   * Save a new password (will be persisted in database)
   *
   * @param password a clear password
   */
  void changePassword(String password) throws PersistenceException;

  /**
   * Get the tmp contributor linked to this contributor
   *
   * @return the tmp contributor linked to this contributor
   */
  TmpContributor getTmpContributor();

  /**
   * Set the avatar picture name, if any
   *
   * @param contributor the tmp contributor linked to this one
   */
  void setTmpContributor(TmpContributor contributor);

  /**
   * Get the avatar id of this contributor, if any
   *
   * @return a contributor picture id
   */
  Long getAvatarId();

  /**
   * Set the avatar id of this contributor
   *
   * @param avatarId a contributor picture id
   */
  void setAvatarId(Long avatarId);

  /**
   * Get the avatar that portray this contributor
   *
   * @return the picture that portray this contributor
   */
  ContributorPicture getAvatar();

  /**
   * Get the open id of the user, if any
   *
   * @return the user open id
   */
  String getOpenId();

  /**
   * Set the open id of the user
   *
   * @param openId the user open id
   */
  void setOpenId(String openId);

  /**
   * Get the type of open id (from where the user used it, facebook, google,...)
   *
   * @return the type of open id
   */
  EOpenIdType getOpenIdType();

  /**
   * Set the type of open id
   *
   * @param type the type of open id
   */
  void setOpenIdType(EOpenIdType type);

  /**
   * Get the open id token of the user, if any
   *
   * @return the user open id token
   */
  String getOpenIdToken();

  /**
   * Set the open id token of the user
   *
   * @param openIdToken the user open id token
   */
  void setOpenIdToken(String openIdToken);

  /**
   * Get all affiliations of this contributor
   *
   * @return a list of Affiliation
   */
  List<Affiliation> getAffiliations();

  /**
   * Set the affiliations for this Actor
   *
   * @param affiliations a list of affiliations to set
   */
  void setAffiliations(List<Affiliation> affiliations);

  /**
   * Add a new affiliation to this contributor
   *
   * @param affiliation the affiliation to add.
   */
  void addAffiliation(Affiliation affiliation);

  /**
   * Remove an affiliation from this contributor, if it does not exist, this is unchanged.
   *
   * @param affiliation the affiliation to remove
   */
  void removeAffiliation(Affiliation affiliation);

  /**
   * Get this contributor subscription / invitation date
   *
   * @return the date at which this contributor has been invited / has subscribed
   */
  Date getSubscriptionDate();

  /**
   * Set this contributor subscription / invitation date
   *
   * @param date the date at which this contributor has been invited / has subscribed
   */
  void setSubscriptionDate(Date date);

  /**
   * Create a new auth token for this Contributor (used for auth processes) Save this token in database
   *
   * @return a new universally unique auth token
   */
  String newAuthToken();

  /**
   * Get current auth token, if any (token may be expired)
   *
   * @return the current auth token
   */
  String getAuthToken();

  /**
   * Check if the associated auth token to a auth procedure has expired
   *
   * @return true if the Contributor's auth token has expired
   */
  boolean hasAuthTokenExpired();

  /**
   * Create a new confirmation token for this Contributor (used for auth processes) Save this token in database
   *
   * @return a new universally unique confirmation token
   */
  String newConfirmationToken() throws PersistenceException;

  /**
   * Get current confirmation token, if any (token may be expired)
   *
   * @return the current confirmation token
   */
  String getConfirmationToken();

  /**
   * Check if the associated confirmation token to a subscription/recovery/change procedure has expired
   *
   * @return true if the Contributor's confirmation token has expired
   */
  boolean hasConfirmationTokenExpired();

  /**
   * Create a new newsletter token for this Contributor (used for unsubscribe from newsletters) Save this token in database
   *
   * @return a new universally unique newsletter token
   */
  String newNewsletterToken() throws PersistenceException;

  /**
   * Get current newsletter token, if any (token may be expired)
   *
   * @return the current newsletter token
   */
  String getNewsletterToken();

  /**
   * Check if the associated newsletter token has expired
   *
   * @return true if the Contributor's newsletter token has expired
   */
  boolean hasNewsletterTokenExpired();

  /**
   * Check if this Contributor is valid : if is validated, if he has a email, firstname, lastname and he is not deleted.
   *
   * @return true if is a valid one
   */
  boolean isValid();

  /**
   * Check if this Contributor has been validated, i.e., it followed the validation procedure
   *
   * @return the email
   */
  boolean isValidated();

  /**
   * Set the validated state of this Contributor (won't be persisted, use validate instead if you want to
   * change its state).
   *
   * @param validated boolean saying if this contributor is validated or not
   */
  void isValidated(boolean validated);

  /**
   * Check whether this contributor is banned from the platform, preventing him to publish
   *
   * @return true if contributor is banned
   */
  boolean isBanned();

  /**
   * Set the banned status of this contributor, preventing him to publish in the platform
   *
   * @param isBanned true to prevent linked contributor to publish
   */
  void isBanned(boolean isBanned);

  /**
   * Check whether the contributor is deleted, ie, may not access anymore but it keeped for his contributions.
   *
   * @return true if this contributor is deleted
   */
  boolean isDeleted();

  /**
   * Set thedeleted status of this contributor, preventing him to publish and login in the platform
   *
   * @param isDeleted true to prevent linked contributor to login
   */
  void isDeleted(boolean isDeleted);

  /**
   * Persist the banned status of this contributor, preventing him to publish in the platform
   *
   * @param isBanned true to prevent linked contributor to publish
   * @throws PersistenceException if an error occurred while saving the banned state into the database
   */
  void banFromPlatform(boolean isBanned) throws PersistenceException;

  /**
   * Check whether this contributor is a researcher, teacher or student
   *
   * @return true if this contributor is a researcher, teacher or student
   */
  boolean isPedagogic();

  /**
   * Set whether this contributor is a researcher, teacher or student
   *
   * @param isPedagogic true if (s)he's a researcher, teacher or student
   */
  void isPedagogic(boolean isPedagogic);

  /**
   * Check whether this contributor wants newsletters
   *
   * @return true if this contributor wants newsletters
   */
  boolean isNewsletter();

  /**
   * Set whether this contributor wants newsletters
   *
   * @param isNewsletter true if this user wants newsletters
   */
  void isNewsletter(boolean isNewsletter);

  /**
   * Check whether the contributor is warned about old browser danger
   *
   * @return true if this contributor is warned
   */
  boolean isBrowserWarned();

  /**
   * Set the if this contributor has been warned about old browser danger
   *
   * @param isBrowserWarned true if he has been warned
   */
  void isBrowserWarned(boolean isBrowserWarned);

  /**
   * Set this Contributor as validated (persist this new state in database), also update email address, if needed.
   *
   * @throws TokenExpiredException if this.hasConfirmationTokenExpired
   * @throws PersistenceException if the validated state could not been persisted
   */
  void validate() throws TokenExpiredException, PersistenceException;

  /**
   * Persist this Contributor
   *
   * @throws PersistenceException if the action could not been performed
   */
  void save(int currentGroup) throws PersistenceException;

  /**
   * Check whether this contributor belongs to given group
   *
   * @param groupId a group id
   * @return the group subscription if this contributor belongs to this group with its role, null otherwise
   */
  GroupSubscription belongsTo(int groupId);

  /**
   * Get the list of groups to which this contributor belongs to with its roles in that groups
   *
   * @return a list of group subscriptions
   */
  List<GroupSubscription> getGroups();

  /**
   * Get the default group for this contributor, ie, contributors may select one of their groups as
   * their default one where all their new contributions will be added by default.
   *
   * @return the default group of this contributor with its role in it
   */
  GroupSubscription getDefaultGroup();

  /**
   * Get the list of contributions touched by this contributor, ie, the ones he created or modified, ordered by descending version.
   *
   * @param group a group id in which we have to retrieve the contributions of given contributor (-1 to ignore the group id)
   * @param amount the amount of contributions to retrieve, if any non-strictly positive value is passed, all contributions are retrieved
   * @return a list of contributions
   */
  List<Contribution> getContributions(int group, int amount);

  /**
   * Get the version of this contributor instance (optimistic locking)
   *
   * @return a timestamp representing the version of this particular instance
   */
  long getVersion();

  /**
   * Set a timestamp as the version number of this object (used for optimistic concurrent-locking)
   *
   * @param version a timestamp (as Date.getTime())
   */
  void setVersion(long version);

  /**
   * Give the admin rights to this contributor
   *
   */
  void giveAdminRights();

  /**
   * Remove the admin rights to this contributor
   *
   */
  void removeAdminRights();

  /**
   * Get the webdeb contributor id
   *
   * @return the webdeb contributor id
   */
  static Long getWebdebContributor() {
    return 0L;
  }

}
