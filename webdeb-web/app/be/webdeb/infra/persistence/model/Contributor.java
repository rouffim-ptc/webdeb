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

package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.EOpenIdType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The persistent class for the contributor database table. Represents a user that contributes into webdeb.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "contributor")
@Unqueryable
public class Contributor extends WebdebModel {

  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, Contributor> find = new Model.Finder<>(Contributor.class);

  @Id
  @Column(name = "id_contributor", unique = true, nullable = false)
  private Long idContributor;

  @Column(name = "birth_year")
  @Unqueryable
  private Integer birthYear;

  @Column(name = "auth_token")
  @Unqueryable
  private String authToken;

  @Column(name = "confirmation_token")
  @Unqueryable
  private String confirmationToken;

  @Column(name = "newsletter_token")
  @Unqueryable
  private String newsletterToken;

  @Column(name = "email")
  @Unqueryable
  private String email;

  @Column(name = "pseudo", unique = true, nullable = false)
  private String pseudo;

  @Column(name = "firstname")
  @Unqueryable
  private String firstname;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gender", nullable = false)
  private TGender gender;

  @Column(name = "lastname")
  @Unqueryable
  private String lastname;

  @Column(name = "password_hash")
  @Unqueryable
  private String passwordHash;

  @Column(name = "openid")
  @Unqueryable
  private String openId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "openid_type")
  @Unqueryable
  private TOpenIdType openIdType;

  @Column(name = "openid_token")
  @Unqueryable
  private String openIdToken;

  @Column(name = "registration_date")
  private Timestamp registrationDate;

  @Column(name = "auth_token_expiration_date")
  @Unqueryable
  private Timestamp authTokenExpirationDate;

  @Column(name = "newsletter_token_expiration_date")
  @Unqueryable
  private Timestamp newsletterTokenExpirationDate;

  @Column(name = "validated")
  @Unqueryable
  private int validated;

  @Column(name = "is_banned")
  @Unqueryable
  private int isBanned;

  @Column(name = "is_deleted")
  @Unqueryable
  private int isDeleted;

  @Column(name = "pedagogic")
  private int pedagogic;

  @Column(name = "newsletter")
  private int newsletter;

  @Column(name = "browser_warned")
  @Unqueryable
  private int browserWarned;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_picture")
  private ContributorPicture picture;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "default_group", nullable = false)
  @Unqueryable
  private Group defaultGroup;

  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  @JoinColumn(name = "tmp_contributor")
  @Unqueryable
  private TmpContributor tmpContributor;

  // all projects linked to this group
  @ManyToMany(mappedBy = "contributors", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Unqueryable
  private List<ProjectSubgroup> projects;

  @OneToMany(mappedBy = "contributor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Unqueryable
  private List<ContributorHasAffiliation> contributorHasAffiliations;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "residence")
  private TCountry residence;

  @OneToMany(mappedBy = "contributor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<ContributorHasGroup> contributorHasGroups;

  @OneToMany(mappedBy = "contributor", cascade = CascadeType.ALL)
  @Unqueryable
  private List<ContributorPicture> addedPictures;

  @Version
  @Column(name = "version")
  @Unqueryable
  private Timestamp version;

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the id of contributor
   *
   * @return the contributor id
   */
  public Long getIdContributor() {
    return this.idContributor;
  }

  /**
   * Set the id of contributor
   *
   * @param idContributor the contributor id
   */
  public void setIdContributor(Long idContributor) {
    this.idContributor = idContributor;
  }

  /**
   * Get the contributor year of birth
   *
   * @return the 4-digit year of birth
   */
  public Integer getBirthYear() {
    return this.birthYear;
  }

  /**
   * Set the contributor's year of birth
   *
   * @param birthYear a 4-digit year
   */
  public void setBirthYear(Integer birthYear) {
    this.birthYear = birthYear;
  }

  /**
   * Get the auth token for this contributor (used for authenticate user from everywhere)
   *
   * @return the auth token (may be null if contributor doens't has a auth token yet)
   */
  public String getAuthToken() {
    return this.authToken;
  }

  /**
   * Set the auth token for this contributor (used for authenticate user from everywhere)
   *
   * @param authToken a unique auth token
   */
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  /**
   * Get the confirmation token for this contributor (used at subscription)
   *
   * @return the confirmation token (may be null if contributor already confirmed his registration)
   */
  public String getConfirmationToken() {
    return this.confirmationToken;
  }

  /**
   * Set the confirmation token for this contributor (used at subscription)
   *
   * @param confirmationToken a unique confirmation token
   */
  public void setConfirmationToken(String confirmationToken) {
    this.confirmationToken = confirmationToken;
  }

  /**
   * Get the newsletter token for this contributor (used at subscription)
   *
   * @return the newsletter token for this contributor (used at subscription)
   */
  public String getNewsletterToken() {
    return newsletterToken;
  }

  /**
   * Set the newsletter token for this contributor (used at subscription)
   *
   * @param newsletterToken a unique newsletter token
   */
  public void setNewsletterToken(String newsletterToken) {
    this.newsletterToken = newsletterToken;
  }

  /**
   * Get the contributor's email
   *
   * @return a valid email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * Set the contributor's email
   *
   * @param email a valid email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Get the contributor's pseudonym
   *
   * @return a unique pseudonym
   */
  public String getPseudo() {
    return this.pseudo;
  }

  /**
   * Set the contributor's pseudonym
   *
   * @param pseudo a unique pseudonym
   */
  public void setPseudo(String pseudo) {
    this.pseudo = pseudo;
  }

  /**
   * Get the contributor's first name
   *
   * @return his/her first name
   */
  public String getFirstname() {
    return this.firstname;
  }

  /**
   * Set the contributor's first name
   *
   * @param firstname a first name
   */
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  /**
   * Get contributor's gender
   *
   * @return a gender
   */
  public TGender getGender() {
    return this.gender;
  }

  /**
   * Set contributor's gender
   *
   * @param gender a gender
   */
  public void setGender(TGender gender) {
    this.gender = gender;
  }

  /**
   * Get the contributor's last name
   *
   * @return his/her lastname
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * Set the contributor's last name
   *
   * @param lastname a lastname
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * Get the open id of the user, if any
   *
   * @return the user open id
   */
  public String getOpenId(){
    return openId;
  }

  /**
   * Set the open id of the user
   *
   * @param openId the user open id
   */
  public void setOpenId(String openId){
    this.openId = openId;
  }

  /**
   * Get the type of open id (from where the user used it, facebook, google,...)
   *
   * @return the type of open id
   */
  public TOpenIdType getOpenIdType(){
    return openIdType;
  }

  /**
   * Set the type of open id
   *
   * @param type the type of open id
   */
  public void setOpenIdType(TOpenIdType type){
    this.openIdType = type;
  }

  /**
   * Get the open id token of the user, if any
   *
   * @return the user open id token
   */
  public String getOpenIdToken() {
    return openIdToken;
  }

  /**
   * Set the open id of the user token
   *
   * @param openIdToken the user open id token
   */
  public void setOpenIdToken(String openIdToken) {
    this.openIdToken = openIdToken;
  }

  /**
   * Get the hashed password
   *
   * @return a hashed password
   */
  public String getPasswordHash() {
    return passwordHash;
  }

  /**
   * Set the hashed password
   *
   * @param passwordHash a hashed password
   */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /**
   * Get the date at which this contributor registered
   *
   * @return a date
   */
  public Date getRegistrationDate() {
    return registrationDate;
  }

  /**
   * Set the date at which this contributor registered
   * @param registrationDate a timestamp
   */
  public void setRegistrationDate(Timestamp registrationDate) {
    this.registrationDate = registrationDate;
  }

  /**
   * Get the date at which this contributor auth token will be expired
   *
   * @return a date
   */
  public Date getAuthTokenExpirationDate() {
    return authTokenExpirationDate;
  }

  /**
   * Set the date at which this contributor auth token will be expired
   * @param authTokenExpirationDate a timestamp
   */
  public void setAuthTokenExpirationDate(Timestamp authTokenExpirationDate) {
    this.authTokenExpirationDate = authTokenExpirationDate;
  }

  /**
   * Get the date at which this contributor newsletter token will be expired
   *
   * @return a date
   */
  public Date getNewsletterTokenExpirationDate() {
    return newsletterTokenExpirationDate;
  }

  /**
   * Set the date at which this contributor auth token will be expired
   * @param newsletterTokenExpirationDate a timestamp
   */
  public void setNewsletterTokenExpirationDate(Timestamp newsletterTokenExpirationDate) {
    this.newsletterTokenExpirationDate = newsletterTokenExpirationDate;
  }

  /**
   * Check whether this contributor has validated his/her subscription
   *
   * @return true if (s)he's validated
   */
  public boolean isValidated() {
    return validated == 1;
  }

  /**
   * Set the flag saying if this contributor has validated his/her subscription
   *
   * @param validated true if (s)he's validated
   */
  public void setValidated(boolean validated) {
    this.validated = validated ? 1 : 0;
  }

  /**
   * Check whether the contributor is banned from the whole platform, ie, may not access anymore
   *
   * @return true if this contributor is banned
   */
  public boolean isBanned() {
    return isBanned == 1;
  }

  /**
   * Set the banned status of this contributor, preventing him to publish in the platform
   *
   * @param isBanned true to prevent this contributor to publish in the whole platform
   */
  public void setBanned(boolean isBanned) {
    this.isBanned = isBanned ? 1 : 0;
  }

  /**
   * Check whether the contributor is deleted, ie, may not access anymore but it keeped for his contributions.
   *
   * @return true if this contributor is deleted
   */
  public boolean isDeleted() {
    return isDeleted == 1;
  }

  /**
   * Set the deleted status of this contributor, preventing him to login in the platform
   */
  public void setDeleted() {
    this.isDeleted = 1;
  }

  /**
   * Set the pedagogic status of this contributor, preventing him to publish in the platform
   *
   * @param pedagogic true to prevent linked contributor to publish in linked group
   */
  public void setPedagogic(boolean pedagogic) {
    this.pedagogic = pedagogic ? 1 : 0;
  }

  /**
   * Check whether the contributor is from a teaching/research community
   *
   * @return true if this contributor is a teacher, researcher or student
   */
  public boolean isPedagogic() {
    return pedagogic == 1;
  }

  /**
   * Set true if this user wants newsletters
   *
   * @param newsletter true if this user wants newsletters
   */
  public void setNewsletter(boolean newsletter) {
    this.newsletter = newsletter ? 1 : 0;
  }

  /**
   * Check if this user wants newsletters
   *
   * @return true if this user wants newsletters
   */
  public boolean isNewsletter() {
    return newsletter == 1;
  }

  /**
   * Check whether the contributor is warned about old browser danger
   *
   * @return true if this contributor is warned
   */
  public boolean isBrowserWarned() {
    return browserWarned == 1;
  }

  /**
   * Set the if this contributor has been warned about old browser danger
   *
   * @param browserWarned true if he has been warned
   */
  public void setBrowserWarned(boolean browserWarned) {
    this.browserWarned = browserWarned ? 1 : 0;
  }

  /**
   * Get the avatar as contributor picture
   *
   * @return the contributor avatar
   */
  public ContributorPicture getAvatar() {
    return picture;
  }

  /**
   * Set the avatar file extension, avatar file name is of the form getId() + getAvatar().
   *
   * @param picture a contributor picture
   */
  public void setAvatar(ContributorPicture picture) {
    this.picture = picture;
  }

  /**
   * Get the default group of this contributor
   *
   * @return his default group
   */
  public Group getDefaultGroup() {
    return defaultGroup;
  }

  /**
   * Set the default group of this contributor
   *
   * @param defaultGroup a group being the default of this contributor
   */
  public void setDefaultGroup(Group defaultGroup) {
    this.defaultGroup = defaultGroup;
  }

  /**
   * Get the country of residence for this contributor
   *
   * @return a territory, may be null
   */
  public TCountry getResidence() {
    return residence;
  }

  /**
   * Set the the country of residence for this contributor
   *
   * @param residence a territory
   */
  public void setResidence(TCountry residence) {
    this.residence = residence;
  }

  /**
   * Get the temporary contributor where comes from this contributor in a project context
   *
   * @return a tmp contributor, may be null
   */
  public TmpContributor getTmpContributor() {
    return tmpContributor;
  }

  /**
   * Set the temporary contributor where comes from this contributor in a project context
   *
   * @param contributor a tmp contributor
   */
  public void setTmpContributor(TmpContributor contributor) {
    this.tmpContributor = contributor;
  }

  /**
   * Get the list of project subgroups where this contributor is involved
   *
   * @return a possibly empty list of subgroups
   */
  public List<ProjectSubgroup> getProjects() {
    return projects;
  }

  /**
   * Get the list of project subgroups where this contributor is involved
   *
   * @param subgroups a list of subgroups
   */
  public void setProjects(List<ProjectSubgroup> subgroups) {
    this.projects = subgroups;
  }

  /**
   * Get the list of contributions created/updated by this contributor (only one instance of a mapping
   * contributions / contributor will be returned)
   *
   * @return a (possibly empty) list of contributions
   */
  public List<Contribution> getContributions() {
    return getContributions(-1, 0, 0);
  }

  /**
   * Get the list of contributions created/updated by this contributor with a maximum amount of results
   * (only one instance of a mapping contributions / contributor will be returned)
   *
   * @param amount the maximum amount of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of contributions
   */
  public List<Contribution> getContributions(int amount) {
    return getContributions(-1, 0, amount);
  }

  /**
   * Get the list of contributions created/updated by this contributor between to index of results
   * (only one instance of a mapping contributions / contributor will be returned)
   *
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of contributions
   */
  public List<Contribution> getContributions(int fromIndex, int toIndex) {
    return getContributions(-1, fromIndex, toIndex);
  }

  /**
   * Get the list of external contributions created/updated by this contributor between to index of results
   * (only one instance of a mapping contributions / contributor will be returned)
   *
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param externalSource the source where contributions come from
   * @return a (possibly empty) list of external contributions
   */
  public List<ExternalContribution> getExternalContributions(int fromIndex, int toIndex, int externalSource) {
    return Contribution.findLatestExternalEntries(idContributor, fromIndex, toIndex, externalSource);
  }

  /**
   * Get the list of a maximum given amount of contributions created/updated by this contributor in given group
   * (only one instance of a mapping contributions / contributor will be returned). Will be ordered by version from
   * the latest to the earliest.
   *
   * @param group group id to retrieve the contributions in (-1 to ignore)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of contributions
   */
  public List<Contribution> getContributions(int group, int fromIndex, int toIndex) {
    return Contribution.getEntries(Arrays.asList("0", "1", "2", "3", "6") , idContributor, fromIndex, toIndex, "latest", group);
  }

  /**
   * Get the list of affiliations for this contributor
   *
   * @return a list of affiliations (joint-object, may be null)
   */
  public List<ContributorHasAffiliation> getContributorHasAffiliations() {
    return contributorHasAffiliations != null ? contributorHasAffiliations : new ArrayList<>();
  }

  /**
   * Set the list of affiliations for this contributor
   *
   * @param contributorHasAffiliations a list of affiliations (joint-object)
   */
  public void setContributorHasAffiliations(List<ContributorHasAffiliation> contributorHasAffiliations) {
    this.contributorHasAffiliations = contributorHasAffiliations;
  }

  /**
   * Get the list of groups to which this user belongs to
   *
   * @return a (possibly empty) list of groups
   */
  public List<ContributorHasGroup> getContributorHasGroups() {
    return contributorHasGroups != null ? contributorHasGroups : new ArrayList<>();
  }

  /**
   * Set the list of groups to which this user belongs to
   *
   * @param contributorHasGroups a group
   */
  public void setContributorHasGroups(List<ContributorHasGroup> contributorHasGroups) {
    this.contributorHasGroups = contributorHasGroups;
  }

  /**
   * Get all pictures added by this contributor
   *
   * @return a possibly empty list of contributor picture
   */
  public List<ContributorPicture> getAddedPictures() {
    return addedPictures;
  }

  /**
   * Set pictures added by this contributor
   *
   * @param addedPictures the list of contributor picture
   */
  public void setAddedPictures(List<ContributorPicture> addedPictures) {
    this.addedPictures = addedPictures;
  }

  /**
   * Get the current version of this contributor
   *
   * @return a timestamp with the latest update moment of this contributor
   */
  public Timestamp getVersion() {
    return version;
  }

  /**
   * Set the version of this contributor
   *
   * @param version the timestamp with the latest update moment of this contributor
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }

  /**
   * Check if the contributor is a admin.
   *
   * @return true if contributor is admin
   */
  public boolean isAdmin(){
    if(getContributorHasGroups() != null && !getContributorHasGroups().isEmpty()){
      return getContributorHasGroups().get(0).getRole().getIdRole() == EContributorRole.ADMIN.id();
    }
    return false;
  }

  /**
   * Generate a pseudonym for the contributor
   *
   * @return the generated pseudonym
   */
  public String generatePseudo(){
    String pseudo = firstname != null && firstname.length() > 3 ? firstname.substring(0, 3) : "webdeb";
    pseudo += lastname != null && lastname.length() > 3 ? lastname.substring(0, 3) : "";
    return pseudo + "-" + idContributor;
  }

  /**
   * Delete current contributor. Must override de method because it doesn't work for unknown reason.
   */
  @Override
  public boolean delete() {
    find.where().eq("id_contributor", idContributor).delete();
    return true;
  }

  @Override
  public String toString() {
    return new StringBuffer("contributor [").append(getIdContributor()).append("] ").append(getEmail()).append(" ")
        .append(", pseudo: ").append(getPseudo()).append(" ")
        .append(getFirstname()).append(" ").append(getLastname())
        .append(", gender: ").append(getGender() != null ? getGender().getIdGender() : "unset")
        .append(", date of birth: ").append(getBirthYear())
        .append(", registered: ").append(getRegistrationDate())
        .append(", openId: ").append(getOpenId())
        .append(", openIdType: ").append(getOpenIdType() != null ? getOpenIdType().getIdType() : "null")
        .append(", auth token expiration: ").append(getAuthTokenExpirationDate())
        .append(", banned: ").append(isBanned())
        .append(", deleted: ").append(isDeleted())
        .append(", pedagogic: ").append(isPedagogic())
        .append(", newsletter: ").append(isNewsletter())
        .append(", warned about browser: ").append(isBrowserWarned())
        .append(", affiliations: ").append(getContributorHasAffiliations().stream()
            .map(ContributorHasAffiliation::toString).collect(Collectors.joining(", ")))
        .append(", residence: ").append(residence != null ? residence.getIdCountry() : "unset")
        .append(", default group: ").append(getDefaultGroup() != null ? getDefaultGroup() : "no default group")
        .append(", in groups: ").append(getContributorHasGroups().stream().map(g ->
            String.valueOf(g.getGroup().getIdGroup())).collect(Collectors.joining(",")))
        .append(" [version:").append(version).append("]").toString();
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve a contributor by its id
   *
   * @param id the contributor id
   * @return the contributor corresponding to that id, null otherwise
   */
  public static Contributor findById(Long id) {
    return id == null || id == -1L ? null : find.byId(id);
  }

  /**
   * Retrieve a contributor with an email.
   *
   * @param email email to search for
   * @return the contributor corresponding to given email, null otherwise
   */
  public static Contributor findByEmail(String email) {
    return find.where().eq("email", email).findUnique();
  }

  /**
   * Retrieve a contributor with an openid and openid type.
   *
   * @param openId the user openid
   * @param type the type of openid
   * @return the contributor corresponding to given openid, null otherwise
   */
  public static Contributor findByOpenId(String openId, EOpenIdType type) {
    return find.where().eq("openid", openId).eq("openid_type", type.id()).findUnique();
  }

  /**
   * Retrieve a contributor with a pseudonym
   *
   * @param pseudo pseudonym to search for
   * @return the contributor corresponding to given pseudonym, null otherwise
   */
  public static Contributor findByPseudo(String pseudo) {
    return find.where().eq("pseudo",pseudo).findUnique();
  }

  /**
   * Retrieves a user from a auth token.
   *
   * @param email the user email
   * @param token the auth token to use.
   * @return a user if the auth token is found, null otherwise.
   */
  public static Contributor findByAuthToken(String email, String token) {
    Contributor c = findByEmail(email);
    return c != null && token != null && !token.equals("") && token.equals(c.getAuthToken()) ? c : null;
  }

  /**
   * Retrieves a user from his/her email and auth token.
   *
   * @param email email to search for
   * @param token the auth token to use.
   * @return a user if the email and auth token are found, null otherwise.
   */
  public static Contributor findByEmailAndAuthToken(String email, String token) {
    return find.where().eq("email", email).eq("auth_token", token).findUnique();
  }

  /**
   * Retrieves a user from his/her pseudonym and auth token.
   *
   * @param pseudo pseudonym to search for
   * @param token the auth token to use.
   * @return a user if the pseudonym and auth token are found, null otherwise.
   */
  public static Contributor findByPseudoAndAuthToken(String pseudo, String token) {
    return find.where().eq("pseudo", pseudo).eq("authToken", token).findUnique();
  }

  /**
   * Retrieves a user from a confirmation token.
   *
   * @param token the confirmation token to use.
   * @return a user if the confirmation token is found, null otherwise.
   */
  public static Contributor findByConfirmationToken(String token) {
    return find.where().eq("confirmationToken", token).findUnique();
  }

  /**
   * Retrieve a list of contributors by either his/her name or email
   *
   * @param query the term to search in firstname, name or email
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty list) of contributors matching the search query
   */
  public static List<Contributor> findByNameOrEmail(String query, int fromIndex, int toIndex) {

    List<Contributor> result;
    String select;

    if(!query.equals("")) {
      select = "select id_contributor from contributor" +
              " where match(firstname, lastname, email) against ('" + String.join(" ", query) + "' in boolean mode)* 1.2 > 0" +
              " order by match(firstname, lastname, email) against ('" + String.join(" ", query) + "' in boolean mode)* 1.2 desc" +
              getSearchLimit(fromIndex, toIndex);
    }else{
      select = "select id_contributor from contributor" + getSearchLimit(fromIndex, toIndex);
    }

    logger.debug("will execute search contributor query: " + select);
    result = Ebean.find(Contributor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? new ArrayList<>(result) : new ArrayList<>();
  }

  /**
   * Get all contributors that have not already validated their accounts
   *
   * @return a (possibly empty) list of contributors
   */
  public static List<Contributor> findAllUnvalidated() {
    String select = "select distinct c.id_contributor from contributor c " +
            "left join tmp_contributor tc on c.tmp_contributor = tc.id_tmp_contributor " +
            "where validated = 0 and tc.id_tmp_contributor is null";
    List<Contributor> result = Ebean.find(Contributor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Retrieve the list contributors for a given role
   *
   * @return the (possibly empty list) of contributors of the given role
   */
  public static List<Contributor> findContributorsByRole(EContributorRole role) {
    String select = "select distinct c.id_contributor from contributor c " +
            (role == EContributorRole.ALL ? "" :
            "left join contributor_has_group chg on chg.id_contributor = c.id_contributor " +
            "where chg.id_role = " + role.id());
    List<Contributor> result = Ebean.find(Contributor.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Retrieve the list of admin contributors
   *
   * @return the (possibly empty list) of admins contributors
   */
  public static List<Contributor> getAllAdmins() {
    return findContributorsByRole(EContributorRole.ADMIN);
  }



    /**
     * Retrieve all affiliation actors for a given contributor
     *
     * @param id a contributor id
     * @return the list of affiliation of this contributor (may be empty)
     */
  public static List<Actor> getAffiliations(Long id) {
    List<Actor> result = new ArrayList<>();
    List<ContributorHasAffiliation> chas = ContributorHasAffiliation.findByContributor(id);
    result.addAll(chas.stream().filter(cha -> cha.getActor() != null).map(ContributorHasAffiliation::getActor).collect(Collectors.toList()));
    return result;
  }

  /**
   * Get the Webdeb contributor
   *
   * @return the Webdeb contributor
   */
  public static Contributor getWebdebContributor() {
    return findById(0L);
  }

  /**
   * Check if given contributor is member of a pedagogic group where given contribution is
   *
   * @param contributorId a contributor id
   * @param contributionId a contribution id
   * @return true if the contributor is member of a pedagogic group where given contribution is
   */
  public static boolean contributorIsPedagogicForGroupAndContribution(Long contributorId, Long contributionId){
    String select = "SELECT g.id_group FROM webdeb.contributor_group g " +
            "inner join contributor_has_group chg on chg.id_group = g.id_group " +
            "inner join contribution_in_group cig on cig.id_group = g.id_group " +
            "where g.is_pedagogic = 1 " +
            "and chg.id_contributor = " + contributorId +
            " and cig.id_contribution = " + contributionId;

    return !Ebean.find(Group.class).setRawSql(RawSqlBuilder.parse(select).create()).findList().isEmpty();
  }
}
