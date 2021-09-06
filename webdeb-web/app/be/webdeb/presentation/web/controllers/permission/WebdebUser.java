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

package be.webdeb.presentation.web.controllers.permission;

import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.EContributionVisibility;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.text.Text;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.account.LoginForm;
import be.webdeb.presentation.web.controllers.account.ContributorHolder;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import play.data.Form;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements a deadbolt Subject for user permission management.
 * User permissions depends on their role in public group (if they're part of) and
 * their role in current group (aka current scope).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class WebdebUser implements Subject, Comparable<WebdebUser> {


  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();


  private ContributorFactory factory;
  private ContributionFactory contributionFactory;
  private ContributorHolder contributor;
  private GroupSubscription currentScope;
  private List<GroupSubscription> subscriptions;
  private Map<Integer, GroupSubscription> subscriptionsMap = null;
  private List<GroupSubscription> sortedSubscriptions;
  private Form<LoginForm> loginForm;
  private WebdebRole role;
  private boolean browserWarned;
  private String tmpPseudo = null;
  private Group publicGroup = null;
  private int nbClaims = -1;

  /**
   * Default constructor
   */
  public WebdebUser() {
    role = new WebdebRole(EContributorRole.VIEWER);
    subscriptions = new ArrayList<>();
  }

  /**
   * Create a default empty user, with default 'public' group
   *
   * @param loginForm a loginForm
   * @param factory the contributor factory, needed to retrieve permissions on the fly
   * @param userWarned true if the user want to use old browser
   */
  public WebdebUser(Form<LoginForm> loginForm, ContributorFactory factory, ContributionFactory contributionFactory, String userWarned) {
    this.loginForm = loginForm;
    this.factory = factory;
    this.contributionFactory = contributionFactory;
    currentScope = factory.getDefaultGroup();
    role = new WebdebRole(EContributorRole.VIEWER);
    subscriptions = new ArrayList<>();
    setBrowserWarned(userWarned);
    initSubscriptionsMap();

  }

  /**
   * Create a tmp user that connect from tmp contributor from project
   *
   * @param factory the contributor factory, needed to retrieve permissions on the fly
   * @param login the tmp user login
   */
  public WebdebUser(ContributorFactory factory, ContributionFactory contributionFactory, String login) {
    this.factory = factory;
    this.contributionFactory = contributionFactory;
    this.tmpPseudo = login;
    currentScope = factory.getDefaultGroup();
    role = new WebdebRole(EContributorRole.TMP_CONTRIBUTOR);
    subscriptions = new ArrayList<>();
    setBrowserWarned("true");

  }

  /**
   * Create a user from given subscription (group - contributor)
   *
   * @param currentScope a subscription
   * @param factory the contributor factory, needed to retrieve permissions on the fly
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @param userWarned true if the user want to use old browser
   */
  public WebdebUser(GroupSubscription currentScope, ContributorFactory factory, ContributionFactory contributionFactory, String lang, String userWarned) {
    this.currentScope = currentScope;
    this.factory = factory;
    this.contributionFactory = contributionFactory;
    subscriptions = currentScope.getContributor().getGroups();
    role = new WebdebRole(currentScope.getRole());
    setBrowserWarned(userWarned);

    contributor = new ContributorHolder(currentScope.getContributor(), null, lang);
  }

  private void initSubscriptionsMap(){
    if(subscriptionsMap == null) {
      subscriptionsMap = new LinkedHashMap<>();
      subscriptions.forEach(e -> subscriptionsMap.put(e.getGroup().getGroupId(), e));
    }
  }

  @Override
  public String getIdentifier() {
    return contributor != null ? contributor.getEmail() : null;
  }

  @Override
  public List<? extends Role> getRoles() {
    return Collections.singletonList(role);
  }

  /**
   * Get the user's role has a EContributor role enum value
   *
   * @return the user's role
   */
  public EContributorRole getERole() {
    return role.getRole();
  }

  @Override
  public List<WebdebPermission> getPermissions() {
    Set<WebdebPermission> permissions = new HashSet<>();
    // get role permissions
    permissions.addAll(factory.getPermissionForRole(currentScope.getRole()).stream()
        .map(WebdebPermission::new).collect(Collectors.toList()));
    // add group permissions
    permissions.addAll(currentScope.getGroup().getPermissions().stream()
        .map(WebdebPermission::new).collect(Collectors.toList()));

    return new ArrayList<>(permissions);
  }

  /**
   * Get the login form, used when no contributor is logged
   *
   * @return the loginForm (may be null if a contributor is logged)
   */
  public Form<LoginForm> getLoginForm() {
    return loginForm;
  }

  /**
   * Set the login form, used when no contributor is logged and an error in form has been detected
   *
   * @param loginForm the form object to set for this user
   */
  public void setLoginForm(Form<LoginForm> loginForm) {
    this.loginForm = loginForm;
  }

  /**
   * Get the Contributor holder behind this user
   *
   * @return the contributor, may be null if no contributor is logged
   */
  public ContributorHolder getContributor() {
    return contributor;
  }

  /**
   * Get the user id, aka the contributor id.
   *
   * @return the contributor id, or -1 if user is not logged
   */
  public Long getId() {
    return contributor != null ? contributor.getId() : -1;
  }

  /**
   * Get the current group, aka, kind of scope where all public contributions/contributors are browsable,
   * plus, private contributions/contributors, depending on group's visibility configuration
   *
   * @return the current group
   * @see be.webdeb.core.api.contributor.EContributionVisibility
   * @see be.webdeb.core.api.contributor.EMemberVisibility
   */
  public Group getGroup() {
    return currentScope != null ? currentScope.getGroup() : null;
  }

  public Group getPublicGroup() {
    if(publicGroup == null) {
      publicGroup = factory.retrieveGroup(Group.getGroupPublic());
    }
    return publicGroup;
  }

  /**
   * Get the temporary user pseudo
   *
   * @return the user pseudo
   */
  public String getTmpPseudo() {
    return tmpPseudo;
  }

  /**
   * Get the current group id, aka, kind of scope where all public contributions/contributors are browsable,
   * plus, private contributions/contributors, depending on group's visibility configuration
   *
   * @return the current group id
   */
  public int getGroupId() {
    return (currentScope != null ? currentScope.getGroup().getGroupId() : Group.getGroupPublic());
  }

  /**
   * Check whether this user belongs to given group
   *
   * @param group a group id
   * @return his/her subscription if (s)he belongs to group, null otherwise
   */
  public GroupSubscription belongsTo(int group) {
    initSubscriptionsMap();
    return subscriptionsMap.get(group);
  }

  /**
   * Get all subscriptions of this user
   *
   * @return a list of subscriptions
   */
  public List<GroupSubscription> getSubscriptions() {
    return subscriptions;
  }

  /**
   * Get all subscriptions of this user or only the webdeb public group if the user is admin of the plateform
   *
   * @return a list of subscriptions
   */
  public List<GroupSubscription> getSubscriptionsOrPublic() {
    // if user is admin of the plateform, only return his subscription with the public group. If this subscription
    // does'nt exits, return the currentScope
    if(getERole() == EContributorRole.ADMIN){
      List<GroupSubscription> subs = new ArrayList<>();
      GroupSubscription gs = belongsTo(Group.getGroupPublic());
      subs.add(gs == null ? currentScope : gs);
      return subs;
    }
    return subscriptions;
  }

  /**
   * Get all subscriptions of this user sorted by subscription date
   *
   * @return a list of subscriptions
   */
  public List<GroupSubscription> getSubscriptionsSortedByDate() {
    if(sortedSubscriptions == null) {
      sortedSubscriptions = new ArrayList<>(subscriptions);
      sortedSubscriptions.sort((o1, o2) -> {
        Date o1name = o1.getJoinDate();
        Date o2name = o2.getJoinDate();
        return o1name.compareTo(o2name);
      });
    }
    return sortedSubscriptions;
  }

  /**
   * Reload group subscriptions from database
   */
  public void refreshGroups() {
    subscriptions = factory.retrieveContributor(contributor.getId()).getGroups();
  }


  /**
   * Check whether the contributor is warned about old browser danger
   *
   * @return true if this contributor is warned
   */
  public boolean isBrowserWarned() {
    if(!browserWarned && contributor != null){
      return contributor.getBrowserWarned();
    }
    return browserWarned;
  }

  /**
   * Check whether the given group is the default user group
   *
   * @param groupId the group id
   * @return true if this contributor is warned
   */
  public boolean isDefaultGroup(int groupId){
    return contributor != null && contributor.getContributor() != null
            && contributor.getContributor().getDefaultGroup() != null
            && contributor.getContributor().getDefaultGroup().getGroup() != null
            && contributor.getContributor().getDefaultGroup().getGroup().getGroupId() == groupId;
  }

  /**
   * Set browser warned from string
   *
   * @param browserWarned a string value is true or false or null
   */
  private void setBrowserWarned(String browserWarned) {
    this.browserWarned = (browserWarned != null && browserWarned.equals("true"));
  }

  /*
   * VISIBILITY MANAGEMENT
   */

  /**
   * Filters out given contribution that this user may not see in his current view.
   *
   * @param contributions a list of contributions to
   * @return given contributions filtered regarding the visibility constraints
   */
  public Collection<? extends Contribution> filterContributionList(Collection<? extends Contribution> contributions) {
    contributions.removeIf(c -> !mayView(c));
    return contributions;
  }

  /**
   * Check whether this user may view or not a contribution, ie.
   *
   * Either, user's current view is the public group, then all publicly visible contributions are viewable
   * Or, user's current view is a specific group, then contribution must belong to this group and, if contributions
   * are private, user is the creator of this contribution, or he's an admin
   *
   * @param contribution a contribution
   * @return true if user may view it
   */
  public boolean mayView(Contribution contribution) {

    // admins may always see his contributions
    GroupSubscription sub = belongsTo(Group.getGroupPublic());
    if( sub != null && sub.getRole().id() == EContributorRole.ADMIN.id()){
      return true;
    }

    // user may always see his contributions
    if (contribution.getContributors().stream().anyMatch(c -> getId().equals(c.getId()))) {
      return true;
    }

    // if current group is default public -> opt-in test : any of given contribution's group has public access
    if (contribution.getInGroups().stream().anyMatch(g -> g.getContributionVisibility().equals(EContributionVisibility.PUBLIC))) {
      return true;
    } else {
      // if contribution is an actor or a tag and it is in a group with public visibility of contribution
      if(contribution.getType().isAlwaysPublic() || contribution.getInGroups().stream().anyMatch(g -> g.getContributionVisibility().id() == EContributionVisibility.PUBLIC.id())){
        return true;
      }

      // we are in a group-specific view
      // is this contribution visible in current group ?
      Optional<Group> group = contribution.getInGroups().stream().filter(g -> g.getGroupId() == getGroupId()).findFirst();

      // either this contribution is not visible in current group
      if (group.isPresent() &&
          // group visibility is not private or user is an admin of it
        (group.get().getContributionVisibility().id() < EContributionVisibility.PRIVATE.id() || isAdminOf(group.get()))) {
        return true;
      }
    }

    // false by default
    return false;
  }

  /**
   * Check whether this user may view a text content
   *
   * @param id a text id
   * @return true if user may view it
   */
  public boolean mayViewTextContent(Long id) {
    Contribution c = contributionFactory.retrieve(id, EContributionType.TEXT);

    if(c != null){
      return ((Text) c).isContentVisibleFor(getId());
    }

    return true;
  }

    /**
     * Check if this user follows the given group
     *
     * @param group a group
     * @return true if user follows the given group
     */
    public boolean isFollows(Group group) {
        GroupSubscription subscription = belongsTo(group.getGroupId());

        return subscription != null && subscription.isFollowed();
    }

  /**
   * Check if this user is an admin of given group, and his current view is on given group
   *
   * @param group a group
   * @return true if user's current scope is actually given group and he's an admin of this group
   */
  public boolean isMemberOf(Group group) {
    return belongsTo(group.getGroupId()) != null;
  }

  /**
   * Check if this user is an admin of given group, and his current view is on given group
   *
   * @param group a group
   * @return true if user's current scope is actually given group and he's an admin of this group
   */
  public boolean isAdminOf(Group group) {
    GroupSubscription subscription = belongsTo(group.getGroupId());
    return subscription != null && subscription.getRole().id() > EContributorRole.CONTRIBUTOR.id();
  }

  /**
   * Check if this user is an admin of any group where this contribution is visible
   *
   * @param contribution a contribution
   * @return true if user is an an admin of any group where this contribution is visible
   */
  public boolean isAdminOf(ContributionHolder contribution) {
    return subscriptions.stream().anyMatch(s ->
        // an admin of webdeb
        (s.getGroup().getGroupId() == factory.getDefaultGroup().getGroup().getGroupId() && s.getRole().equals(EContributorRole.ADMIN))
        // at least an owner of one of this contribution's group
        || (contribution.getGroups().contains(s.getGroup().getGroupId()) && s.getRole().id() > EContributorRole.CONTRIBUTOR.id())
        // or he's the creator of it
        || (getId().equals(contribution.getCreator())));
  }

  /**
   * Check if this user is an admin of any group where this contribution is visible
   *
   * @param contribution a contribution
   * @return true if user is an an admin of any group where this contribution is visible
   */
  public boolean isAdminOf(Contribution contribution) {
    return contribution.getContributionType().getEType().isLink() ||
            subscriptions.stream().anyMatch(s ->
            // an admin of webdeb
            (s.getGroup().getGroupId() == factory.getDefaultGroup().getGroup().getGroupId() && s.getRole().equals(EContributorRole.ADMIN))
                    // at least an owner of one of this contribution's group
                    || (contribution.getInGroups().contains(s.getGroup()) && s.getRole().id() > EContributorRole.CONTRIBUTOR.id())
                    // or he's the creator of it
                    || (getId().equals(contribution.getCreator().getId())));
  }

  /**
   * Get the number of contribution claims make by contributors
   *
   * @return the number of claims
   */
  public int getNumberOfClaims() {
    if(nbClaims == -1) {
      nbClaims = this.factory.getNumberOfClaims(this.getId());
    }
    return nbClaims;
  }

  @Override
  public int compareTo(WebdebUser o) {
    if (o == null || o.getContributor() == null) {
      return -1;
    }
    return (contributor.getLastname() + contributor.getFirstname())
        .compareTo(o.getContributor().getLastname() + o.getContributor().getFirstname());
  }

  /**
   * Check whether this contributor is an admin of the platform, ie, an admin of the public group
   *
   * @return true if this contributor is an administrator of Webdeb
   */
  public boolean isPublicAdmin() {
    return subscriptions.stream().anyMatch(s ->
        s.getGroup().getGroupId() == factory.getDefaultGroup().getGroup().getGroupId()
            && s.getRole().equals(EContributorRole.ADMIN));
  }

  @Override
  public boolean equals(Object obj) {
    return !(obj == null || !(obj instanceof WebdebUser)) && hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 83 * (contributor != null ?
        contributor.getFirstname().hashCode() + contributor.getLastname().hashCode() : 17);
  }

  @Override
  public String toString() {
    return getId() + " " + (contributor != null ? contributor.getFullname() : "anonymous viewer");
  }
}
