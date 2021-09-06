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

import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.contributor.EOpenIdType;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.contributor.TmpContributor;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.exception.TokenExpiredException;
import be.webdeb.core.impl.actor.AstractIndividual;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

import java.util.*;

/**
 * This class implements a Contributor.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteContributor extends AstractIndividual implements Contributor {

  private Long id = -1L;
  private String email;
  private String pseudo;
  private String firstname;
  private String lastname;
  private Integer birthyear;
  private List<Affiliation> affiliations = new ArrayList<>();
  private String password;
  private String openId;
  private String openIdToken;
  private EOpenIdType openIdType;
  private TmpContributor tmpContributor;
  private boolean validated;
  private boolean deleted;
  private boolean banned;
  private boolean pedagogic;
  private boolean newsletter;
  private boolean browserWarned;
  private Long avatarId = -1L;
  private ContributorPicture avatar = null;
  private Date subscription;
  private long version;

  private ContributorAccessor accessor;

  /**
   * Default constructor.
   *
   * @param accessor the contributor accessor
   */
  ConcreteContributor(ContributorAccessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public void setEmail(String email) throws FormatException {
    if ((email == null || "".equals(email.trim())) && openId == null) {
      throw new FormatException(FormatException.Key.CONTRIBUTOR_ERROR, "email is invalid " + email);
    }
    this.email = email != null ? email.trim() : email;
  }

  @Override
  public String getPseudo() {
    return pseudo;
  }

  @Override
  public void setPseudo(String pseudo) throws FormatException {
    if (pseudo == null || "".equals(pseudo.trim())) {
      throw new FormatException(FormatException.Key.CONTRIBUTOR_ERROR, "pseudo is invalid " + pseudo);
    }
    this.pseudo = pseudo.trim();
  }

  @Override
  public String getFirstname() {
    return firstname;
  }

  @Override
  public void setFirstname(String firstname) {
    this.firstname = cleanupName(firstname, AbstractContribution.MAX_FIRSTNAME_SIZE);
  }

  @Override
  public String getLastname() {
    return lastname;
  }

  @Override
  public void setLastname(String lastname) {
    this.lastname = cleanupName(lastname, AbstractContribution.MAX_NAME_SIZE);
  }

  @Override
  public Integer getBirthyear() {
    return birthyear;
  }

  @Override
  public void setBirthyear(Integer birthyear) {
    this.birthyear = birthyear;
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
  public ContributorPicture getAvatar() {
    if(avatar == null) {
      avatar = accessor.retrieveContributorPicture(avatarId);
    }
    return avatar;
  }

  @Override
  public String getOpenId() {
    return openId;
  }

  @Override
  public void setOpenId(String openId) {
    this.openId = openId;
  }

  @Override
  public EOpenIdType getOpenIdType() {
    return openIdType;
  }

  @Override
  public void setOpenIdType(EOpenIdType openIdType) {
    this.openIdType = openIdType;
  }

  @Override
  public String getOpenIdToken() {
    return openIdToken;
  }

  @Override
  public void setOpenIdToken(String openIdToken) {
    this.openIdToken = openIdToken;
  }

  @Override
  public List<Affiliation> getAffiliations() {
    return affiliations;
  }

  @Override
  public void setAffiliations(List<Affiliation> affiliations) {
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
  public void addAffiliation(Affiliation affiliation) {
    if (affiliation.isValid().isEmpty()) {
      affiliations.add(affiliation);
    }
  }

  @Override
  public void removeAffiliation(Affiliation affiliation) {
    if (affiliation != null) {
      affiliations.remove(affiliation);
    }
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public void changePassword(String password) throws PersistenceException {
    accessor.changePassword(id, password);
  }

  @Override
  public TmpContributor getTmpContributor() {
    return tmpContributor;
  }

  @Override
  public void setTmpContributor(TmpContributor contributor) {
    this.tmpContributor = contributor;
  }

  @Override
  public Date getSubscriptionDate() {
    return subscription;
  }

  @Override
  public void setSubscriptionDate(Date date) {
    subscription = date;
  }

  @Override
  public String newConfirmationToken() throws PersistenceException {
    return accessor.newConfirmationToken(email);
  }

  @Override
  public String getConfirmationToken() {
    return accessor.getConfirmationToken(email);
  }

  @Override
  public boolean hasConfirmationTokenExpired() {
    return accessor.isConfirmationTokenExpired(email);
  }

  @Override
  public String newNewsletterToken() throws PersistenceException {
    return accessor.newNewsletterToken(email);
  }

  @Override
  public String getNewsletterToken() {
    return accessor.getNewsletterToken(email);
  }

  @Override
  public boolean hasNewsletterTokenExpired() {
    return accessor.isNewsletterTokenExpired(email);
  }

  @Override
  public String newAuthToken() {
    return accessor.newAuthToken(email);
  }

  @Override
  public String getAuthToken() {
    return accessor.getAuthToken(email);
  }

  @Override
  public boolean hasAuthTokenExpired() {
    return accessor.isAuthTokenExpired(email);
  }

  @Override
  public boolean isValid() {
    return accessor.isContributorValid(id);
  }

  @Override
  public boolean isValidated() {
    return validated;
  }

  @Override
  public void isValidated(boolean validated) {
    this.validated = validated;
  }

  @Override
  public boolean isBanned() {
    return banned;
  }

  @Override
  public void isBanned(boolean banned) {
    this.banned = banned;
  }

  @Override
  public boolean isDeleted() {
        return deleted;
    }

  @Override
  public void isDeleted(boolean isDeleted) {
    this.deleted = isDeleted;
  }

  @Override
  public void banFromPlatform(boolean isBanned) throws PersistenceException {
    accessor.setBanned(id, isBanned);
  }

  @Override
  public boolean isPedagogic() {
    return pedagogic;
  }

  @Override
  public void isPedagogic(boolean isPedagogic) {
    pedagogic = isPedagogic;
  }

  @Override
  public boolean isNewsletter() {
    return newsletter;
  }

  @Override
  public void isNewsletter(boolean isNewsletter) {
    newsletter = isNewsletter;
  }

  @Override
  public boolean isBrowserWarned() {
    return browserWarned;
  }

  @Override
  public void isBrowserWarned(boolean isBrowserWarned) {
    browserWarned = isBrowserWarned;
  }

  @Override
  public void validate() throws TokenExpiredException, PersistenceException {
    accessor.validate(id, email);
  }

  @Override
  public void save(int currentGroup) throws PersistenceException {
    accessor.save(this, currentGroup);
  }

  @Override
  public GroupSubscription belongsTo(int groupId) {
    Optional<GroupSubscription> optional = getGroups().stream().filter(g -> g.getGroup().getGroupId() == groupId).findFirst();
    return optional.orElse(null);
  }

  @Override
  public List<GroupSubscription> getGroups() {
    List<GroupSubscription> subscriptions = accessor.retrieveGroups(id);
    Collections.sort(subscriptions);
    return subscriptions;
  }

  @Override
  public GroupSubscription getDefaultGroup() {
    Optional<GroupSubscription> group = getGroups().stream().filter(GroupSubscription::isDefault).findFirst();
    return group.orElse(null);
  }

  @Override
  public List<Contribution> getContributions(int group, int amount) {
    return accessor.getContributions(id, group, amount);
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public void giveAdminRights(){
    accessor.giveAdminRights(id);
  }

  @Override
  public void removeAdminRights(){
    accessor.removeAdminRights(id);
  }

  @Override
  public int compareTo(Contributor o) {
    if (lastname == null || firstname == null) {
      return 1;
    }

    if (o == null || o.getLastname() == null && o.getFirstname() == null) {
      return -1;
    }

    return (lastname + firstname).compareToIgnoreCase(o.getLastname() + o.getFirstname());
  }

  @Override
  public String toString() {
    return pseudo + " (" + firstname + " " + lastname + " <" + email + ">) academic: " + pedagogic;
  }

  /**
   * Clean up given name by
   * <ul>
   *   <li>trimming blanks</li>
   *   <li>truncating it at given size</li>
   *   <li>capitalizing first letter and lower remaining</li>
   *   <li>and replacing "’" by "'"</li>
   * </ul>
   *
   * @param name the name to cleanup
   * @param size max size for the given name (will be shortened if exceeding)
   * @return given name cleaned as specified above, or null if given name is null or empty
   */
  private String cleanupName(String name, int size) {
    if (name != null && name.trim().length() > 1) {
      String sanitized = name.trim();
      sanitized = sanitized.substring(0, 1).toUpperCase() + sanitized.substring(1).toLowerCase();
      sanitized = sanitized.length() > size ? sanitized.substring(0, size - 1) : sanitized;
      return sanitized.replaceAll("’", "'");
    }
    return null;
  }
}
