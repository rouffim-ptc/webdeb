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

package be.webdeb.presentation.web.controllers.account.group;

import be.webdeb.core.api.contributor.*;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.util.ValuesHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds an api Group. It is mainly used to display group details or manage members.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class GroupForm implements Comparable<GroupForm> {

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Inject
  private ContributorFactory factory = Play.current().injector().instanceOf(ContributorFactory.class);

  @Inject
  protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  // fields (mapped from API Group)
  private Integer id;
  private long version;
  private String name;
  private String description;
  private EContributionVisibility contributionVisibility;
  private EMemberVisibility memberVisibility;
  private String groupColor;
  private String groupColorComplete;
  private Group group;

  private Date joinDate;
  private boolean isBanned;

  private boolean isOpen;
  private boolean isPedagogic;

  private boolean editContribution;
  private boolean editContributionInGroup;
  private boolean deleteContribution;
  private boolean mergeContribution;
  private boolean editMember;
  private boolean disableAnnotation;
  private boolean disableClassification1;


  /**
   * Play / JSON compliant constructor
   */
  public GroupForm() {
    id = -1;
    groupColorComplete = "#" + values.getRandomHexaColor();
  }

  /**
   * Default constructor, create a GroupForm from a given group
   *
   * @param group a group to wrap into a GroupForm
   */
  public GroupForm(Group group) {
    id = group.getGroupId();
    name = group.getGroupName();
    description = group.getDescription();
    contributionVisibility = group.getContributionVisibility();
    memberVisibility = group.getMemberVisibility();
    isOpen = group.isOpen();
    isPedagogic = group.isPedagogic();
    this.group = group;
    groupColor = group.getGroupColor();
    groupColorComplete = "#" + group.getGroupColor();
    editContributionInGroup = group.getPermissions().contains(EPermission.EDIT_CONTRIBUTION_INGROUP);

    /*
    editContribution = group.getPermissions().contains(EPermission.EDIT_CONTRIBUTION);
    deleteContribution = group.getPermissions().contains(EPermission.DELETE_CONTRIBUTION);
    mergeContribution = group.getPermissions().contains(EPermission.MERGE_CONTRIBUTION);
    editMember = group.getPermissions().contains(EPermission.EDIT_MEMBER);
    disableAnnotation = group.getPermissions().contains(EPermission.DISABLE_ANNOTATION);
    disableClassification1 = group.getPermissions().contains(EPermission.DISABLE_CLASSIFICATION);
    */
    version = group.getVersion();
  }

  /**
   * Create a group from a subsription (call default contructor with subscription.getGroup)
   *
   * @param subscription a group subscription
   */
  public GroupForm(GroupSubscription subscription) {
    this(subscription.getGroup());
    joinDate = subscription.getJoinDate();
    isBanned = subscription.isBanned();
  }


  /**
   * Validator (called from form submit)
   *
   * @return null if no error has been found, otherwise the list of found errors
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    // must have a name
    if (values.isBlank(name)) {
      errors.put("name", Collections.singletonList(new ValidationError("name", "group.manage.error.name")));
    } else {
      // check unicity of name
      factory.findGroups(name, false).parallelStream().filter(m ->
          m.getGroupName().equals(name) && m.getGroupId() != id).findAny().ifPresent(group ->
            errors.put("name", Collections.singletonList(new ValidationError("name", "group.manage.error.name.unique"))));
    }

    // contribution visibility is mandatory
    if (contributionVisibility == null) {
      errors.put("contributionVisibility", Collections.singletonList(
          new ValidationError("contributionVisibility", "group.manage.error.contribution.visibility")));
    }

    // member visibility is mandatory
    /*if (memberVisibility == null) {
      errors.put("memberVisibility", Collections.singletonList(
          new ValidationError("memberVisibility", "group.manage.error.member.visibility")));
    }*/

    // must have a color
    if (values.isBlank(groupColorComplete))  {
      errors.put("groupColor", Collections.singletonList(
          new ValidationError("groupColor", "group.manage.error.color.mandatory")));
    }else{
      // verify if it is a correct hexadecimal color
      if(!values.checkHexadecimalColorCode(groupColorComplete)){
        errors.put("groupColor", Collections.singletonList(
            new ValidationError("groupColor", "group.manage.error.color.format")));
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  /**
   * Save this group in database
   *
   * @param contributor the contributor issuing this request (will become default owner)
   * @throws PersistenceException if an error occurred while saving this object into DB
   * @throws PermissionException if given contributor is not authorized to save this group
   */
  public void save(Long contributor) throws PersistenceException, PermissionException {
    logger.debug("try to save group " + name);
    // set all primitive fields
    Group group = factory.getGroup();
    group.setGroupId(id != null ? id : -1);
    group.setVersion(version);
    group.setGroupName(name);
    group.setDescription(description);
    group.setContributionVisibility(contributionVisibility);
    group.setMemberVisibility(memberVisibility == null ? EMemberVisibility.PUBLIC : memberVisibility);
    group.isOpen(isOpen);
    group.isPedagogic(isPedagogic);
    group.setGroupColor(groupColorComplete.replace("#", ""));

    // set contribution-related permissions
    group.setPermissions(new ArrayList<>());
    if (editContributionInGroup) {
      group.getPermissions().add(EPermission.EDIT_CONTRIBUTION_INGROUP);
    }

    group.getPermissions().add(EPermission.MERGE_CONTRIBUTION);
        /*
    if (editContribution) {
      group.getPermissions().add(EPermission.EDIT_CONTRIBUTION);
    }
    if (mergeContribution) {
      group.getPermissions().add(EPermission.MERGE_CONTRIBUTION);
    }
    if (deleteContribution) {
      group.getPermissions().add(EPermission.DELETE_CONTRIBUTION);
    }
    if (editMember) {
      group.getPermissions().add(EPermission.EDIT_MEMBER);
    }
    if (disableAnnotation) {
      group.getPermissions().add(EPermission.DISABLE_ANNOTATION);
    }
    if (disableClassification1) {
      group.getPermissions().add(EPermission.DISABLE_CLASSIFICATION);
    }*/

    // now save it
    group.save(contributor);
  }

  @Override
  public int compareTo(@NotNull GroupForm o) {
    return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get this group id
   *
   * @return an id
   */
  public int getId() {
    return id;
  }

  /**
   * Set this group id
   *
   * @param id the group id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the group name
   *
   * @return the group name
   */
  public String getName() {
    return name;
  }

  /**
   * Set this group's name
   *
   * @param name a name to set
   */
  public void setName(String name) {
    this.name = name;
  }

	/**
   * Set the group id
   *
   * @param id a group id
   */
  public void setId(Integer id) {
    this.id = id;
  }

	/**
   * Get the group description
   *
   * @return a group description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the group description
   *
   * @param description a group description
   */
  public void setDescription(String description) {
    this.description = description;
  }

	/**
   * Get the contribution visibility id
   *
   * @return a visibility id
   * @see EContributionVisibility
   */
  public int getContributionVisibility() {
    return contributionVisibility != null ? contributionVisibility.id() : -1;
  }

  /**
   * Set the contribution visibility id
   *
   * @param contributionVisibility a visibility id
   * @see EContributionVisibility
   */
  public void setContributionVisibility(int contributionVisibility) {
    this.contributionVisibility = EContributionVisibility.value(contributionVisibility);
  }

  /**
   * Get the member visibility id
   *
   * @return a visibility id
   * @see EMemberVisibility
   */
  public int getMemberVisibility() {
    return memberVisibility != null ? memberVisibility.id() : -1;
  }

  /**
   * Set the member visibility id
   *
   * @param memberVisibility a visibility id
   * @see EMemberVisibility
   */
  public void setMemberVisibility(int memberVisibility) {
    this.memberVisibility = EMemberVisibility.value(memberVisibility);
  }

	/**
   * Get the date at which linked user joined this group
   *
   * @return a date
   */
  public Date getJoinDate() {
    return joinDate;
  }

  /**
   * Set the date at which linked user joined this group
   *
   * @param joinDate a date
   */
  public void setJoinDate(Date joinDate) {
    this.joinDate = joinDate;
  }

	/**
   * Check whether this group is open, ie, anyone may join
   *
   * @return true if this group is open
   */
  public boolean getIsOpen() {
    return isOpen;
  }

  /**
   * Set whether this group is open, ie, anyone may join
   *
   * @param open true if this group is open
   */
  public void setIsOpen(boolean open) {
    isOpen = open;
  }

  /**
   * Check whether this group is for a pedagogic use
   *
   * @return true if the group is for a pedagogic use
   */
  public boolean getIsPedagogic() {
    return isPedagogic;
  }

  /**
   * Set whether this group is for a pedagogic use
   *
   * @param pedagogic true if the group is for a pedagogic use
   */
  public void setIsPedagogic(boolean pedagogic) {
    isPedagogic = pedagogic;
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
   * Get the color that representing the group
   *
   * @return the hexadecimal code of the color that representing the group
   */
  public String getGroupColorComplete(){
    return groupColorComplete;
  }

  /**
   * Set the color that representing the group
   *
   * @param color the hexadecimal code of the color that representing the group
   */
  public void setGroupColorComplete(String color){
    groupColorComplete = color;
  }

  /**
   * Check whether bound contributor is banned from this group
   *
   * @return true if linked contributor is banned
   */
  public boolean getIsBanned() {
    return isBanned;
  }

  /**
   * Set whether bound contributor is banned from this group
   *
   * @param banned true if linked contributor is banned
   */
  public void setIsBanned(boolean banned) {
    isBanned = banned;
  }


  /**
   * Check whether anyone that may view a contribution in this group may edit it
   *
   * @return true if anyone may edit a contribution
   */
  public boolean getEditContributionInGroup() {
    return editContributionInGroup;
  }

  /**
   * Set whether anyone that may view a contribution in this group may edit it
   *
   * @param editContributionInGroup true if anyone may edit a contribution
   */
  public void setEditContributionInGroup(boolean editContributionInGroup) {
    this.editContributionInGroup = editContributionInGroup;
  }

  /**
   * Check whether anyone that may view a contribution in this group may edit it
   *
   * @return true if anyone may edit a contribution
   */
  public boolean isEditContribution() {
    return editContribution;
  }

  /**
   * Set whether anyone that may view a contribution in this group may edit it
   *
   * @param editContribution true if anyone may edit a contribution
   */
  public void setEditContribution(boolean editContribution) {
    this.editContribution = editContribution;
  }

  /**
   * Check whether anyone that may view a contribution in this group may delete it
   *
   * @return true if anyone may delete a contribution
   */
  public boolean isDeleteContribution() {
    return deleteContribution;
  }

  /**
   * Set whether anyone that may view a contribution in this group may delete it
   *
   * @param deleteContribution true if anyone may delete a contribution
   */
  public void setDeleteContribution(boolean deleteContribution) {
    this.deleteContribution = deleteContribution;
  }

  /**
   * Check whether the group owner may merge validated contributions from this group into the public database
   *
   * @return true if a group owner may send validated contributions into the public database
   */
  public boolean isMergeContribution() {
    return mergeContribution;
  }

  /**
   * Set whether the group owner may merge validated contributions from this group into the public database
   *
   * @param mergeContribution true if a group owner may send validated contributions into the public database
   */
  public void setMergeContribution(boolean mergeContribution) {
    this.mergeContribution = mergeContribution;
  }

  /**
   * Check whether the group owner may edit member profiles
   *
   * @return true a group owner may may edit member profiles
   */
  public boolean isEditMember() {
    return editMember;
  }

  /**
   * Set whether the group owner may edit member profiles
   *
   * @param editMember true a group owner may may edit member profiles
   */
  public void setEditMember(boolean editMember) {
    this.editMember = editMember;
  }

  /**
   * Check whether the text annotation must be disabled for this group (when working with text to extract arguments)
   *
   * @return true if the text annotation must be disabled
   */
  public boolean isDisableAnnotation() {
    return disableAnnotation;
  }

  /**
   * Set whether the text annotation must be disabled for this group (when working with text to extract arguments)
   *
   * @param disableAnnotation true if the text annotation must be disabled
   */
  public void setDisableAnnotation(boolean disableAnnotation) {
    this.disableAnnotation = disableAnnotation;
  }

  /**
   * Check whether the suggestions for the first argument classification (first type) must be disabled for this group
   *
   * @return true if the first suggestions must be disabled
   */
  public boolean isDisableClassification1() {
    return disableClassification1;
  }

  /**
   * Set whether the suggestions for the first argument classification (first type) must be disabled for this group
   *
   * @param disableClassification1 true if the first suggestions must be disabled
   */
  public void setDisableClassification1(boolean disableClassification1) {
    this.disableClassification1 = disableClassification1;
  }

	/**
   * Get this group version number
   *
   * @return a long timestamp (last update)
   */
  public long getVersion() {
    return version;
  }

  /**
   * Set this group version number
   *
   * @param version a long timestamp (last update)
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Get the group as API object
   *
   * @return the API Group
   */
  @JsonIgnore
  public Group getGroup() {
    return group;
  }
}
