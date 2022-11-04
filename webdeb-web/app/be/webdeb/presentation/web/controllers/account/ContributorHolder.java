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

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.EOpenIdType;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.actor.AffiliationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class holds a more complete representation of a contributor.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ContributorHolder extends SimpleContributorHolder {

  protected String residence;
  protected String gender;
  protected String birthYear;
  protected boolean isBanned;
  protected boolean isDeleted;
  protected boolean isPedagogic;
  protected boolean isNewsletter;
  protected boolean isUserValid;
  protected String pedagogic;
  protected String newsletter;
  protected boolean browserWarned = false;
  protected Long avatarId;
  protected String avatar;
  protected String subscriptionDate;
  protected Contributor contributor;
  protected String openIdToken = null;
  protected EOpenIdType openIdType = null;

  @Inject
  private ContributorFactory contributorFactory = Play.current().injector().instanceOf(ContributorFactory.class);


  /**
   * Play / JSON compliant
   */
  public ContributorHolder() {
    // needed by play
  }

  /**
   * Default constructor
   *
   * @param contributor a contributor
   * @param lang the language code (2-char ISO-639-1) of the user interface
   */
  public ContributorHolder(Contributor contributor, WebdebUser user, String lang) {
    super(contributor, user, lang);

    isBanned = contributor.isBanned();
    isUserValid = contributor.isValidated();
    isDeleted = contributor.isDeleted();
    isPedagogic = contributor.isPedagogic();
    pedagogic = contributor.isPedagogic() ? "true" : "false";
    isNewsletter = contributor.isNewsletter();
    newsletter = contributor.isNewsletter() ? "true" : "false";
    browserWarned = contributor.isBrowserWarned();
    avatarId = contributor.getAvatarId();
    avatar = contributor.getAvatar() != null ? contributor.getAvatar().getPictureFilename() : null;
    openIdType = contributor.getOpenIdType();
    openIdToken = contributor.getOpenIdToken();
    subscriptionDate = new SimpleDateFormat("dd/MM/yyyy").format(contributor.getSubscriptionDate());

    if (contributor.getBirthyear() != null && contributor.getBirthyear() != 0) {
      birthYear = String.valueOf(contributor.getBirthyear());
    }

    // initialize other fields
    init(contributor);
  }

  /**
   * Initialize other fields that are dependent to the "type of view" on this contributor (editable or not)
   * Must be overridden in "form" object
   */
  protected void init(Contributor contributor) {
    gender = contributor.getGender() != null ? contributor.getGender().getName(lang) : "";
    residence = contributor.getResidence() != null ? contributor.getResidence().getName(lang) : "";
  }

  /**
   * Get the contributor's residence country
   *
   * @return a possibly null country of residence
   * @see Country
   */
  public String getResidence() {
    return residence;
  }

  /**
   * Get the contributor's gender id
   *
   * @return a gender id (F or M)
   * @see be.webdeb.core.api.actor.Gender
   */
  public String getGender() {
    return gender;
  }

  /**
   * Get the contributor year of birth
   *
   * @return a 4-char year of the form YYYY
   */
  public String getBirthYear() {
    return birthYear;
  }

  /**
   * Check whether this contributor is banned from the platform
   *
   * @return true if this contributor is banned
   */
  public Boolean isBanned() {
    return isBanned;
  }

  /**
   * Check whether this contributor is deleted
   *
   * @return true if this contributor is deleted
   */
  public Boolean isDeleted() {
    return isDeleted;
  }

  public Boolean isUserValidated() {
    return isUserValid;
  }

  /**
   * Check whether this contributor is a researcher, teacher or student
   *
   * @return true if this contributor is a researcher, teacher or student
   */
  public Boolean isPedagogic() {
    return isPedagogic;
  }

  /**
   * Check whether this contributor is a teacher, student or researcher
   * Using string because it is split in form in two checkboxes (user must explicitly select yes or no)
   *
   * @return "true" if this contributor is a teacher, student or researcher
   */
  public String getPedagogic() {
    return pedagogic;
  }

  /**
   * Check whether this contributor wants newsletters
   *
   * @return true if this contributor wants newsletters
   */
  public Boolean isNewsletter() {
    return isNewsletter;
  }

  /**
   * Check whether this contributor wants newsletters
   * Using string because it is split in form in two checkboxes (user must explicitly select yes or no)
   *
   * @return "true" if this contributor wants newsletters
   */
  public String getNewsletter() {
    return newsletter;
  }
  /**
   * Check whether the contributor is warned about old browser danger
   *
   * @return true if this contributor is warned
   */
  public boolean getBrowserWarned(){
    return browserWarned;
  }

  /**
   * Set the if this contributor has been warned about old browser danger
   *
   * @param isBrowserWarned true if he has been warned
   */
  public void setBrowserWarned(boolean isBrowserWarned){
    browserWarned = isBrowserWarned;
  }

  /**
   * Get this contributor's subscription date
   *
   * @return a DD/MM/YYYY date
   */
  public String getSubscriptionDate() {
    return subscriptionDate;
  }

  public Contributor getContributor() {
    return contributor;
  }

  public Long getAvatarId() {
    return avatarId;
  }

  public EOpenIdType getOpenIdType() {
    return openIdType;
  }

  public String getOpenIdToken() {
    return openIdToken;
  }

  /**
   * Hide compareTo from ContributionHolder superclass
   *
   * @param o the contributor to compare to this
   * @return a negative number if this.lastname + this.firstname is alphabetically before given contributor, 0 if they are
   * equivalent, a positive number if given contributor is alphabetically after this contributor.
   */
  @Override
  public int compareTo(ContributionHolder o) {
    if (o instanceof ContributorHolder) {
      ContributorHolder holder = (ContributorHolder) o;
      return pseudo.compareToIgnoreCase(holder.getPseudo());
    }
    return pseudo.compareToIgnoreCase(getNameToCompare(o));
  }

  /**
   * Get the avatar file name
   *
   * @return the avatar file name
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * Get either the avatar (file) name for this contributor, or a default one
   *
   * @return a file name
   */
  public String getSomeAvatar() {
    if (avatar != null) {
      return avatar;
    }
    return helper.computeAvatar(EActorType.PERSON.id(), gender, true);
  }

  /**
   * Check whether this contributor has a default avatar
   *
   * @return true if this avatar is null
   */
  public boolean hasDefaultAvatar() {
    return avatar == null;
  }

  /**
   * Get the count of contributions by type
   *
   * @return a map of contributions count, type, count
   */
  @JsonIgnore
  public Map<EContributionType, Long> getContributionsCount(){
    return contributorFactory.getContributionsCount(getId());
  }

  @Override
  @JsonIgnore
  public String toString() {
    return "ContributorHolder{" +
            "residence='" + residence + '\'' +
            ", gender='" + gender + '\'' +
            ", birthYear='" + birthYear + '\'' +
            ", isBanned=" + isBanned +
            ", isDeleted=" + isDeleted +
            ", isPedagogic=" + isPedagogic +
            ", isNewsletter=" + isNewsletter +
            ", pedagogic='" + pedagogic + '\'' +
            ", newsletter='" + newsletter + '\'' +
            ", avatarId='" + avatarId + '\'' +
            ", firstname='" + firstname + '\'' +
            ", lastname='" + lastname + '\'' +
            ", email='" + email + '\'' +
            ", pseudo='" + pseudo + '\'' +
            '}';
  }

}
