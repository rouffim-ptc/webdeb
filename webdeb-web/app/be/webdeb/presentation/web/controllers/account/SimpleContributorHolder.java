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

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.actor.AffiliationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reduced form of a contributor. Used to display contributor information that are visible to other users,
 * ie, hiding email, banned status, etc.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class SimpleContributorHolder extends ContributionHolder {

  // for lazy loading of contributions and subscriptions
  private Contributor contributor;
  protected String firstname = null;
  protected String lastname = null;
  protected String email = null;
  protected String pseudo = null;
  protected List<AffiliationHolder> affiliations;

  /**
   * Play / JSON compliant
   */
  public SimpleContributorHolder() {
    // needed by play
  }

  /**
   * Reduced form of a contributor without email address and link to contributor object
   *
   * @param contributor a contributor
   * @param lang the language code (2-char ISO-639-1) of the user interface
   */
  public SimpleContributorHolder(Contributor contributor, WebdebUser user, String lang) {
    this.contributor = contributor;
    this.user = user;

    this.lang = lang;
    id = contributor.getId();
    firstname = contributor.getFirstname();
    lastname = contributor.getLastname();
    email = contributor.getEmail();
    pseudo = contributor.getPseudo();
    affiliations = helper.toAffiliationsHolders(contributor.getAffiliations(), user, lang);
    version = contributor.getVersion();
  }

  @Override
  public MediaSharedData getMediaSharedData() {
    return null;
  }

  @Override
  public String getContributionDescription() {
    return null;
  }

  @Override
  public String getDefaultAvatar(){
    return "";
  }

  /**
   * Get the contributor's first name
   *
   * @return a name
   */
  public String getFirstname() {
    return firstname == null ? pseudo : firstname;
  }

  /**
   * Get the contributor's last name
   *
   * @return a name
   */
  public String getLastname() {
    return lastname == null ? pseudo : lastname;
  }

  /**
   * Get the contributor's email
   *
   * @return an email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * Get the contributor's pseudonym
   *
   * @return a pseudonym
   */
  public String getPseudo() {
    return pseudo;
  }

  /**
   * Get the concatenation of first and last name
   *
   * @return either the first and lastname, or an empty string if no name is set up (not fully registerd user).
   */
  public String getFullname() {
    if (firstname != null && lastname != null) {
      return firstname + " " + lastname;
    }
    return pseudo;
  }

  /**
   * Get the concatenation of first, last name and email
   *
   * @return either the first, lastname and email address or an empty string if no name is set up (not fully registerd user).
   */
  public String getFullnameAndEmail() {
    if (email != null) {
      return getFullname() + " (" + email + ")";
    }
    return getFullname() + (firstname != null && lastname != null ? " (" + pseudo + ")" : "");
  }

  /**
   * Get the list of affiliations
   *
   * @return a (possibly empty) list of affiliations
   */
  public List<? extends AffiliationHolder> getAffiliations() {
    return affiliations;
  }

  /**
   * Helper method to convert API Contribution objects into Presentation objects (into their concrete types
   * depending on their API type)
   *
   * @param contributions a list of API contributions
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the list of converted contributions into ContributionHolder concrete classes
   */
  @JsonIgnore
  public List<ContributionHolder> toHolders(Collection<? extends Contribution> contributions, WebdebUser user, String lang)  {
    return helper.toHolders(contributions, user, lang);
  }

  /**
   * Get all contributions for this contributor
   *
   * @param group group id in which contributions for this contributor must be retrieved (-1 for all
   * @param amount the amount of contributions to retrieve
   * @return the list of all contributions for this contributor
   */
  @JsonIgnore
  public List<ContributionHolder> getContributions(WebdebUser user, int group, int amount) {
    return toHolders(user.filterContributionList(contributor.getContributions(group, amount)), user, lang);
  }

  /**
   * Get all subscriptions (i.e. groups and roles) of this contributor
   *
   * @return a list subscriptions
   */
  @JsonIgnore
  public List<GroupSubscription> getSubscriptions() {
    return contributor.getGroups();
  }

}
