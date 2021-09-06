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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.EContributionVisibility;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.PrivateOwned;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The persistent class for the group database table. Specifies the properties of a group, ie, its name, description
 * visibility options (regarding contributions and contributors) and if it openly accessible. Also may have specific
 * permissions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name="contributor_group")
@Unqueryable
public class Group extends WebdebModel {

  private static final Model.Finder<Integer, Group> find = new Model.Finder<>(Group.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_group")
  private Integer idGroup;

  @Column(name = "group_name", nullable = false, unique = true)
  private String groupName;

  @Column(name = "group_description")
  private String groupDescription;

  // group is visible for any contributor and contributor may join freely
  @Column(name = "is_open")
  private int isOpen;

  @Column(name = "is_pedagogic")
  private int isPedagogic;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "member_visibility", nullable = false)
  private TMemberVisibility memberVisibility;

  @Column(name = "group_color")
  private String groupColor;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "contribution_visibility", nullable = false)
  private TContributionVisibility contributionVisibility;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "group_has_permission",
      joinColumns = { @JoinColumn(name = "id_group", referencedColumnName = "id_group")},
      inverseJoinColumns = { @JoinColumn(name = "id_permission", referencedColumnName = "id_permission") }
  )
  private List<Permission> permissions;

  // to delete orphans, since we manage the join table ourselves
  @PrivateOwned
  @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ContributorHasGroup> contributorHasGroups;

  @ManyToMany(mappedBy = "groups", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Contribution> contributions;

  // all project subgroups linked to this group
  @ManyToMany(mappedBy = "groups", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ProjectSubgroup> projectSubgroups;

  @Version
  @Column(name = "version")
  private Timestamp version;

  /**
   * Get the group id
   *
   * @return an id
   */
  public int getIdGroup() {
    return this.idGroup;
  }

  /**
   * Set the group id
   *
   * @param idGroup an id
   */
  public void setIdGroup(int idGroup) {
    this.idGroup = idGroup;
  }

  /**
   * Get the group name
   *
   * @return an identifying name for this group
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * Set the group name
   *
   * @param groupName an identifying name for this group
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * Get the full text description of this group
   *
   * @return a description for this group
   */
  public String getGroupDescription() {
    return groupDescription;
  }

  /**
   * Set the group description of this group
   *
   * @param groupDescription a description
   */
  public void setGroupDescription(String groupDescription) {
    this.groupDescription = groupDescription;
  }

  /**
   * Get the flag saying if this group is opened to the public for viewing and joining
   *
   * @return true if this group is opened to the public
   */
  public boolean isOpen() {
    return isOpen != 0;
  }

  /**
   * Set the flag saying if this group is opened to the public for viewing and joining
   *
   * @param isOpen true if this group is opened to the public
   */
  public void isOpen(boolean isOpen) {
    this.isOpen = isOpen ? 1 : 0;
  }

  /**
   * Check whether this group is for a pedagogic use
   *
   * @return true if the group is for a pedagogic use
   */
  public boolean isPedagogic() {
    return isPedagogic != 0;
  }

  /**
   * Set whether this group is for a pedagogic use
   *
   * @param pedagogic true if the group is for a pedagogic use
   */
  public void isPedagogic(boolean pedagogic) {
    this.isPedagogic = pedagogic ? 1 : 0;
  }

  /**
   * Get the type of visibility for group members
   *
   * @return the member visibility object
   * @see be.webdeb.core.api.contributor.EMemberVisibility
   */
  public TMemberVisibility getMemberVisibility() {
    return memberVisibility;
  }

  /**
   * Set the type of visibility for group members
   *
   * @param memberVisibility the member visibility object
   * @see be.webdeb.core.api.contributor.EMemberVisibility
   */
  public void setMemberVisibility(TMemberVisibility memberVisibility) {
    this.memberVisibility = memberVisibility;
  }

  /**
   * Get the type of visibility for contributions in this group
   *
   * @return the contribution visibility object
   * @see be.webdeb.core.api.contributor.EContributionVisibility
   */
  public TContributionVisibility getContributionVisibility() {
    return contributionVisibility;
  }

  /**
   * Set the type of visibility for contributions in this group
   *
   * @param contributionVisibility the contribution visibility object
   * @see be.webdeb.core.api.contributor.EContributionVisibility
   */
  public void setContributionVisibility(TContributionVisibility contributionVisibility) {
    this.contributionVisibility = contributionVisibility;
  }

  /**
   * Get the color that representing the group
   *
   * @return the hexadecimal code of the color that representing the group
   */
  public String getGroupColor(){
    return groupColor;
  }

  /**
   * Set the color that representing the group
   *
   * @param color the hexadecimal code of the color that representing the group
   */
  public void setGroupColor(String color){
    groupColor = color;
  }

  /**
   * Get the group's specific permissions
   *
   * @return a (possibly empty) list of permissions
   */
  public List<Permission> getPermissions() {
    return permissions != null ? permissions : new ArrayList<>();
  }

  /**
   * Set this group's specific permissions
   *
   * @param permissions a list of permissions to assign to this group
   */
  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }

  /**
   * Get the list of contributor of this group
   *
   * @return a list of contributor joint-objects
   */
  public List<ContributorHasGroup> getContributorHasGroups() {
    return this.contributorHasGroups;
  }

  /**
   * Set the list of contributor of this group
   *
   * @param contributorHasGroups a list of contributor joint-objects
   */
  public void setContributorHasGroups(List<ContributorHasGroup> contributorHasGroups) {
    this.contributorHasGroups = contributorHasGroups;
  }

  /**
   * Get all contributions for this group
   *
   * @return the list of contributions published in this group
   */
  public List<Contribution> getContributions() {
    return contributions != null ?
        contributions.stream().filter(c -> !c.isHidden()).collect(Collectors.toList()) : new ArrayList<>();
  }

  /**
   * Set all contributions for this group
   *
   * @param contributions the list of contributions published in this group
   */
  public void setContributions(List<Contribution> contributions) {
    this.contributions = contributions;
  }

  /**
   * Get the list of subgroups linked to this group
   *
   * @return a possibly empty list of project subgroups
   */
  public List<ProjectSubgroup> getProjectSubgroups() {
    return projectSubgroups;
  }

  /**
   * Set the list of subgroups linked to this group
   *
   * @param projectSubgroups a list of subgroups linked to this group
   */
  public void setProjectSubgroups(List<ProjectSubgroup> projectSubgroups) {
    this.projectSubgroups = projectSubgroups;
  }

  /**
   * Get the latest version timestamp for this group (either creation or modification time)
   *
   * @return a timestamp
   */
  public Timestamp getVersion() {
    return this.version;
  }

  /**
   * Set the latest version timestamp for this group
   *
   * @param version a timestamp
   */
  public void setVersion(Timestamp version) {
    this.version = version;
  }

  /*
   * HELPER METHODS
   */

  /**
   * Add or update given contributor as member of this group with given role (none may be null)
   *
   * @param contributor a contributor
   * @param role a role object
   * @param token the token used to send the invitation to given contributor
   * @param followed true if the group is followed by the contributor
   */
  public void addMember(Contributor contributor, Role role, String token, boolean followed) {
    // check if user is already in group
    ListIterator<ContributorHasGroup> it = contributorHasGroups.listIterator();

    // must remove and add new because ebean has troubles with updates of composite keys
    while (it.hasNext()) {
      ContributorHasGroup chg = it.next();
      if (chg.getContributor().getIdContributor().equals(contributor.getIdContributor())) {
        it.remove();
        it.add(new ContributorHasGroup(contributor, this, role, token, followed));
        return;
      }
    }
    // not found => add it
    contributorHasGroups.add(new ContributorHasGroup(contributor, this, role, token, followed));
  }

  /**
   * Add or update given contributor as member of this group with given role (none may be null)
   *
   * @param contributor a contributor
   * @param role a role object
   * @param token the token used to send the invitation to given contributor
   */
  public void addMember(Contributor contributor, Role role, String token) {
    addMember(contributor, role, token, true);
  }

  /**
   * Remove given contributor from member list of this group
   *
   * @param contributor a contributor
   * @return true if given contributor has been removed from group, false if it was not found in group
   */
  public boolean removeMember(Contributor contributor) {
    ListIterator<ContributorHasGroup> it = contributorHasGroups.listIterator();
    while (it.hasNext()) {
      ContributorHasGroup chg = it.next();
      if (chg.getContributor().getIdContributor().equals(contributor.getIdContributor())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    return new StringBuffer(getGroupName()).append(" [").append(getIdGroup()).append("], ").append(getGroupDescription())
        .append(", open: ").append(isOpen())
        .append(", pedagogic: ").append(isPedagogic())
        .append(", member vis. ").append(getMemberVisibility().getId())
        .append(", contrib. vis. ").append(getContributionVisibility().getId())
        .append(", color: ").append(getGroupColor())
        .append(", permissions: ").append(getPermissions().stream().map(Permission::getName).collect(Collectors.joining(",")))
        .append(" [version: ").append(getVersion()).append("]").toString();
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve a group by its id
   *
   * @param id a group id
   * @return the group object, null if not found
   */
  public static Group findById(int id) {
    return find.byId(id);
  }

  /**
   * Retrieve a group by its name
   *
   * @param name a group name
   * @return the group object, null if not found
   */
  public static Group findByName(String name) {
    return find.where().eq("group_name", name).findUnique();
  }

  /**
   * Retrieve the list of all known groups having given query as name or description and their isOpen
   * flag corresponding to given openOnly
   *
   * @param query a search query
   * @param openOnly true if only open groups must be retrieved
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the (possibly empty) list of groups corresponding to given parameters
   */
  public static List<Group> findGroups(String query, boolean openOnly, int fromIndex, int toIndex) {
    int lower = fromIndex > 0 ? fromIndex : 0;
    int upper = toIndex > 0 && toIndex > fromIndex ? toIndex - fromIndex : Integer.MAX_VALUE;

    List<Group> result = find.where()
        .conjunction()
          .disjunction()
            .contains("group_name", query)
            .contains("group_description", query).endJunction()
          .eq("is_open", openOnly ? 1 : 0).endJunction()
          .setFirstRow(lower).setMaxRows(upper).findList();

    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get all groups where contributions are publicly visible
   *
   * @return a (possibly empty) lisyt of groups
   */
  public static List<Group> findPublicGroups() {
    List<Group> result = find.where().eq("contribution_visibility", EContributionVisibility.PUBLIC.id()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get all group ids where contributions are publicly visible
   *
   * @return a (possibly empty) lisyt of group ids
   */
  public static Set<Integer> findPublicGroupIds() {
    Set<Integer> ids = new HashSet<>();
    Ebean.createSqlQuery("SELECT id_group FROM contributor_group where contribution_visibility = " +  EContributionVisibility.PUBLIC.id())
            .findEach(e ->  ids.add(e.getInteger("id_group")));
    return ids;
  }

  /**
   * Get the default public group
   *
   * @return the default public group
   */
  public static Group getPublicGroup() {
    return findById(0);
  }

  /**
   * Get all groups
   *
   * @return a list of groups
   */
  public static List<Group> getAllGroups() {
    return find.all();
  }

  /**
   * Get the list of group ids (as string) that are visible for given group
   *
   * @param group a group id (-1 to get all groups)
   * @return a list of group ids visible for given group
   */
  public static List<String> getVisibleGroupsFor(int group) {
    switch (group) {
      case -1:
        return find.all().stream().map(g -> String.valueOf(g.getIdGroup())).collect(Collectors.toList());
      case 0:
        return Group.findPublicGroups().stream().map(g -> String.valueOf(g.getIdGroup())).collect(Collectors.toList());
      default:
        return Collections.singletonList(String.valueOf(group));

    }
  }

  /**
   * Get the map of amount of contributions for this group by contribution type, for all contribution created or update
   * between given dates.
   *
   * @param fromDate contribution created or updated from date
   * @param toDate contribution created or updated to date
   * @return a possibly empty map of contribution type, amount of contributions
   */
  public Map<Integer, Long> getContributionsAmount(Date fromDate, Date toDate) {
    Map<Integer, Long> results = new LinkedHashMap<>();

    String pattern = "yyyy-MM-dd HH:mm:ss";
    String fDate = new SimpleDateFormat(pattern).format(fromDate);
    String tDate = new SimpleDateFormat(pattern).format(toDate);

    String select = "SELECT c.contribution_type as 'type', count(c.id_contribution) as 'nb_contributions' FROM contribution c " +
            "left join contribution_in_group cig on cig.id_contribution = c.id_contribution " +
            "where cig.id_group = " + idGroup + " and (c.version BETWEEN '" + fDate + "' AND '" + tDate + "') " +
            "and c.contribution_type <= 6 and c.deleted = 0 and c.hidden = 0 and c.contribution_type != 2 group by c.contribution_type";

    Ebean.createSqlQuery(select).findList().forEach(e -> {
      if(e.getInteger("type") != null && e.getLong("nb_contributions") > 0)
        results.put(e.getInteger("type"), e.getLong("nb_contributions"));
    });

    return results;
  }

  /**
   * Get the number of contributor in the group
   *
   * @param group a group id
   * @return the number of contributors in the group
   */
  public static int countNbContributors(int group){
    String select = "SELECT count(*) as 'count' FROM contributor_group g " +
            "inner join contributor_has_group chg on chg.id_group = g.id_group " +
            "inner join contributor c on chg.id_contributor = c.id_contributor " +
            "where g.id_group = " + group + " and c.is_banned = false and c.is_deleted = false and c.validated = true";

    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  /**
   * Get the number of contributions in the group
   *
   * @param group a group id
   * @param type the contribution type
   * @return the number of contributions in the group
   */
  public static int countNbContributions(int group, EContributionType type){
    String select = "SELECT count(*) as 'count' FROM contributor_group g " +
            "inner join contribution_in_group cig on cig.id_group = g.id_group " +
            "inner join contribution c on cig.id_contribution = c.id_contribution " +
            "where g.id_group = 0 and c.deleted = false";
    if(type != null){
      select += " and c.contribution_type = " + type.id();
    }

    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  public boolean containsContribution(Long contribution) {
    String select = "SELECT distinct g.id_group FROM contributor_group g " +
            "inner join contribution_in_group cig on cig.id_group = g.id_group " +
            "where cig.id_contribution = " + contribution + " and cig.id_group = " + idGroup;
    return !Ebean.find(Group.class).setRawSql(RawSqlBuilder.parse(select).create()).findList().isEmpty();
  }

}
