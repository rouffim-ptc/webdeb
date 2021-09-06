/*
 *  Copyright 2014-2018 University of Namur (PReCISE) - University of Louvain (Girsef - CENTAL).
 *  This is part of the WebDeb software (WDWEB), a collaborative platform to record and analyze
 *  argumentation-based debates. This is free software:  you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License version 3 as published by the
 *  Free Software Foundation. It is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.
 *
 *  See <https://webdeb.be/> for a running instance of a webdeb web platform.
 *  See the GNU Lesser General Public License (LGPL) for more details over the license terms.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this copy.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.account.group;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.application.query.EQueryKey;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.EContributionVisibility;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.mail.MailerException;
import be.webdeb.presentation.web.controllers.*;
import be.webdeb.infra.mail.WebdebMail;
import be.webdeb.presentation.web.controllers.account.*;
import be.webdeb.presentation.web.controllers.account.settings.*;
import be.webdeb.presentation.web.controllers.browse.SearchForm;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.account.group.*;
import be.webdeb.presentation.web.views.html.account.settings.userSettings;
import be.webdeb.presentation.web.views.html.oops.hack;
import be.webdeb.presentation.web.views.html.util.message;
import be.webdeb.presentation.web.views.html.util.groupChooseButton;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Handle actions regarding group management
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class GroupActions extends CommonController {

  @Inject
  private Mailer mailer;

  @Inject
  private ContributorActions contributorActions;

  /**
   * Get page to follow a new group or create one
   *
   * @return redirect to login if not logged or subscription page
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> index() {

    WebdebUser user = sessionHelper.getUser(ctx());

    return CompletableFuture.supplyAsync(() -> ok(groupIndex.render(user)), context.current());
  }

  /**
   * Get the list of editable groups
   *
   * @return the list of editable groups
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> groups() {
    WebdebUser user = sessionHelper.getUser(ctx());
    List<GroupForm> groups = contributorFactory.getAllGroups()
            .stream()
            .map(GroupForm::new)
            .sorted()
            .collect(Collectors.toList());

    return CompletableFuture.supplyAsync(() -> ok(groupList.render(groups, user)), context.current());
  }

  /**
   * Change current scope for user
   *
   * @param group the group to switch to
   * @return redirect to referer page in case of success, a message template to display otherwise (bad request, 400)
   */
  public CompletionStage<Result> changeCurrentScope(int group) {
    logger.debug("CHANGE current scope to " + group);
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = doChangeCurrentScope(group);

    if(messages != null) {
      return sendBadRequest(message.render(messages));
    }

    return CompletableFuture.supplyAsync(() -> ok(sessionHelper.getReferer(ctx())), context.current());
  }

  private Map<String, String> doChangeCurrentScope(int group) {
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    if (user.getContributor() == null) {
      // no user from context
      logger.error("no contributor could be retrieved from context");
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return messages;
    }

    GroupSubscription subscription = user.belongsTo(group);
    if (subscription == null) {
      logger.error("group does not exist, or contributor does not belong to group " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.notmember"));
      return messages;
    }

    // update session key, rebuild user from context and return new button
    sessionHelper.setGroupInSession(ctx(), subscription.getGroup());

    return null;
  }

  /**
   * Change default group in my-subscription pane (ajax-call)
   *
   * @param group a group id to which current user belongs to
   * @return the my-subscriptions pane or a bad request (400) with the message template containing the error
   * message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> changeDefaultGroup(int group) {
    logger.debug("GET change default group " + group);
    Map<String, String> messages = new HashMap<>();

    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);
    if (subscription == null) {
      logger.error("group does not exist, or contributor does not belong to group " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.leave.notmember"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    try {
      subscription.getGroup().setDefaultFor(user.getContributor().getId());
      user.refreshGroups();
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.switch.ok", subscription.getGroup().getGroupName()));
    } catch (Exception e) {
      logger.error("unable to change current group " + group +  " for contributor " + user.getContributor().getId(), e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.leave.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // all good, return userSettings page
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Display modal to search for groups when user wants to jojn an open group
   *
   * @return the modal page to search for groups, or a bad request with the message template
   * containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> newSubscription() {
    logger.debug("GET modal to search for groups");
    return CompletableFuture.supplyAsync(() ->
        ok(joinGroup.render(sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Search for groups containing given query and render the partial page with the results
   *
   * @param query a partial name to look for (or in description)
   * @return a groupResults template containing a (possibly empty) list of groups
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getGroupResults(String query) {
    return CompletableFuture.supplyAsync(() ->
        ok(groupResults.render(contributorFactory.findGroups(query, true).stream()
            .map(GroupForm::new).collect(Collectors.toList()))),context.current());
  }

  /**
   * Search for groups containing given query
   *
   * @param query a partial name to look for (or in description)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a list of jsonified GroupForm containing given query
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchGroups(String query, int fromIndex, int toIndex) {
    return CompletableFuture.supplyAsync(() ->
        ok(Json.toJson(contributorFactory.findGroups(query, false, fromIndex, toIndex).stream()
            .map(GroupForm::new).collect(Collectors.toList()))), context.current());
  }

  /**
   * Handle join group request from user
   *
   * @param group the group id that context user wants to join
   * @return the subscription tab with the result of the request, or a bad request (400) with the message template
   * containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> joinGroup(int group) {
    logger.debug("ADD context contributor to group " + group);
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    Group g = contributorFactory.retrieveGroup(group);
    if (g == null || !g.isOpen()) {
      logger.error("no group retrieved or group is not open " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // now add user as member of group
    try {
      g.addMember(user.getContributor().getId(), EContributorRole.CONTRIBUTOR);
      user.refreshGroups();
      doChangeCurrentScope(group);
    } catch (PersistenceException e) {
      logger.error("error while adding new member " + user.getContributor().getId() + " to group " + group, e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // try to send mail to group owners
    /*g.getGroupOwners().forEach(s -> {
      try {
        mailer.sendMail(new WebdebMail(WebdebMail.EMailType.JOINED_GROUP, s.getContributor().getEmail(),
            be.webdeb.presentation.web.controllers.account.routes.ContributorActions.settings(s.getContributor().getId(), ESettingsPane.SUBSCRIPTION.id()).url(),
            Arrays.asList(s.getGroup().getGroupName(), user.getContributor().getFullname()),
            ctx().lang()));

      } catch (MailerException e) {
        logger.error("unable to warn owner " + s.getContributor().getEmail() + " of group " + g.getGroupName(), e);
      }
    });*/

    // all good
    messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.join.ok", g.getGroupName()));
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Leave from a group (ajax call)
   *
   * @param group a group id
   * @return the subscription tab if the request completed, or a badrequest (400) or internal server error (500)
   * with the message template corresponding to the result of the request if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> leaveGroup(int group) {
    logger.debug("GET leave group " + group);
    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();

    GroupSubscription subscription = user.belongsTo(group);
    if (subscription == null) {
      logger.error("group does not exist, or contributor does not belong to group " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.notmember"));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));

    } else {
      if (subscription.isDefault()) {
        logger.debug("group " + group + " is default for user " + user.getContributor().getId() + ", not removing it");
        // does not allow to remove membership of default group
        messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.leave.isdefault", subscription.getGroup().getGroupName()));
        return CompletableFuture.completedFuture(badRequest(message.render(messages)));
      }
    }

    try {
      if (subscription.getGroup().removeMember(user.getContributor().getId())) {
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.leave.ok", subscription.getGroup().getGroupName()));
      } else {
        messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.leave.nok", subscription.getGroup().getGroupName()));
      }

    } catch (PersistenceException e) {
      logger.error("unable to remove contributor " + user.getContributor().getId() + " from group " + group, e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.leave.error"));
      return CompletableFuture.completedFuture(internalServerError(message.render(messages)));
    }
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Revoke given contributor from given group
   *
   * @param contributor a contributor id
   * @param group a group id
   * @param revoke true if the user must be revoked, false otherwise
   * @return the manageGroup tab if given group is not -1, the admin tab otherwise, or bad request (400)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> setBannedInGroup(Long contributor, int group, boolean revoke) {
    logger.debug("Set banned state of contributor " + contributor + " from group " + group + " to " + revoke);
    Map<String, String> messages = new HashMap<>();

    WebdebUser user = sessionHelper.getUser(ctx());
    Contributor c = contributorFactory.retrieveContributor(contributor);
    if (c == null) {
      logger.error("no contributor could be retrieved with id " + contributor);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // check if a group was passed, if special id -1 => revoke from whole platform
    if (group != -1) {
      return banFromGroup(user, c, group, revoke);
    } else {
      return banFromPlatform(user, c, revoke);
    }
  }

  /**
   * Get (pre-loaded) modal frame to create or edit a group
   *
   * @param group a group id (-1 for new group)
   * @return the modal frame to create or edit a group, or a bad request (400) with the message template
   * containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editGroup(int group) {
    logger.debug("GET edit group modal for group " + group);
    Map<String, String> messages = new HashMap<>();
    GroupForm form;
    if (group != -1) {
      //must find it, otherwise badRequest
      Group g = contributorFactory.retrieveGroup(group);
      if (g == null) {
        logger.error("unable to retrieve group " + group);
        messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
        return CompletableFuture.completedFuture(badRequest(message.render(messages)));
      }
      WebdebUser user = sessionHelper.getUser(ctx());
      GroupSubscription subscription = user.belongsTo(group);
      if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
        logger.error("user " + user.getContributor().getEmail() + " may not edit group details of " + group);
        return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
      }
      form = new GroupForm(g);
    } else {
      form = new GroupForm();
      form.setEditContributionInGroup(true);
    }

    // return modal frame with (possibly empty) group form
    return CompletableFuture.supplyAsync(() ->
        ok(editGroupDetails.render(formFactory.form(GroupForm.class).fill(form), helper, null)), context.current());
  }

  /**
   * Handle post request to save a (new) group
   *
   * @return the manageGroup tab or a bad request (400) / internal server error (500) with the
   * editgroupdetails modal page if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveGroup() {
    logger.debug("POST save group");
    Form<GroupForm> form = formFactory.form(GroupForm.class).bindFromRequest();
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    if (form.hasErrors()) {
      logger.debug("form has errors " + form.errors());
      messages.put(SessionHelper.ERROR, SessionHelper.ERROR_FORM);
      return CompletableFuture.supplyAsync(() ->
          badRequest(editGroupDetails.render(form, helper, messages)), context.current());
    }

    // form is ok, let's save it
    GroupForm group = form.get();

    // check if we have a group id -> check permissions
    if (group.getId() != -1) {
      GroupSubscription subscription = user.belongsTo(group.getId());
      if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
        logger.error("user " + user.getId() + " may not edit group details of " + group.getId());
        return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
      }
    }

    try {
      group.save(user.getContributor().getId());
    } catch (PersistenceException | PermissionException e) {
      logger.error("unable to save group", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() ->
          internalServerError(editGroupDetails.render(form, helper, messages)), context.current());
    }

    messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.save.ok", group.getName()));
    return groups();
  }

  /**
   * Get invitation modal page
   *
   * @param group a group id
   * @return the modal page to invite or add new members in given group or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> inviteInGroup(int group) {
    logger.debug("GET modal page to invite people");
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription g = user.belongsTo(group);
    // if user is not owner -> unauthorized
    if (g == null || g.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // all good, send modal
    return CompletableFuture.supplyAsync(() ->
        ok(inviteInGroup.render(formFactory.form(GroupInvitation.class).fill(new GroupInvitation(g.getGroup())), helper)), context.current());
  }

  /**
   * Handle post request to invite users to join group
   *
   * @return the invite modal if submitted form contains error or the manageGroupes tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendInvitations() {
    logger.debug("SEND invitations or add members to group");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    // check form
    Form<GroupInvitation> invitationForm = formFactory.form(GroupInvitation.class).bindFromRequest();
    if (invitationForm.hasErrors()) {
      logger.debug("error in form " + invitationForm.errors());
      return CompletableFuture.supplyAsync(() -> badRequest(inviteInGroup.render(invitationForm, helper)), context.current());
    }

    // check group id and subscription
    GroupInvitation invitations = invitationForm.get();
    GroupSubscription group = user.belongsTo(invitations.getGroupId());
    // should not happen since user permissions have been checked when building modal, but who knows
    if (group == null || group.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + invitations.getGroupId());
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.invitation.error"));
      return CompletableFuture.supplyAsync(() ->
          internalServerError(manageGroups.render(user.getContributor(), messages)), context.current());
    }

    // for all email addresses, either add them or send an invitation if given address does not exist
    for (RoleForm invitation : invitations.getInvitations()) {
      try {
        Contributor contributor = contributorFactory.retrieveContributor(invitation.getUserId());

        if (contributor != null) {
          // add this contributor to group
          group.getGroup().addMember(contributor.getId(), EContributorRole.value(invitation.getRoleId()));
          // also send him/her a mail
          mailer.sendMail(new WebdebMail(WebdebMail.EMailType.INVITE_EXISTING, contributor.getEmail(),
              be.webdeb.presentation.web.controllers.account.routes.ContributorActions.settings(contributor.getId(), ESettingsPane.SUBSCRIPTION.id()).url(),
              Arrays.asList(group.getGroup().getGroupName(), user.getContributor().getFirstname() + " " + user.getContributor().getLastname()),
              ctx().lang()));
        }
      } catch (MailerException e) {
        logger.error("unable to warn contributor he was invited to group " + group.getGroup().getGroupName(), e);
        messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.invitation.nomail", invitation));
      } catch (PersistenceException e) {
        logger.error("unable to add contributor " + invitation + " to group " + invitations.getGroupId(), e);
        messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.invitation.notinvited", invitation));
      }
    }

    // check if we do not have messages (meaning all went smoothly)
    if (messages.isEmpty()) {
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.invite.ok", group.getGroup().getGroupName()));
    }
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Get change members roles modal page
   *
   * @param group a group id
   * @return the modal page to change members role in given group or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> changeMembersRole(Integer group) {
    logger.debug("GET modal page to invite people in group " + group);
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription g = user.belongsTo(group);
    // if user is not owner -> unauthorized
    if (g == null || g.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    return CompletableFuture.supplyAsync(() ->
            ok(changeMembersRole.render(new GroupForm(g.getGroup()), user)), context.current());
  }


  /**
   * Get change member role modal page
   *
   * @param group a group id
   * @param userId the user to change the role if we only change one
   * @return the modal page to change members role in given group or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> changeMemberRole(Integer group, Long userId) {
    logger.debug("GET modal page to invite people "+userId);
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription g = user.belongsTo(group);
    // if user is not owner -> unauthorized
    if (g == null || g.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    if(userId != -1) {
      Contributor c = contributorFactory.retrieveContributor(userId);
      if (c == null) {
        return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
      }
      GroupSubscription subscription = c.belongsTo(group);
      if (subscription == null) {
        return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
      }
      return CompletableFuture.supplyAsync(() ->
          ok(changeMemberRole.render(g, formFactory.form(RoleForm.class).fill(new RoleForm(subscription)), helper)), context.current());
    }

    // user is not found
    return sendBadRequest();
  }

  /**
   * Handle post request to change members role inside a group
   *
   * @return the changeMembersRole modal if submitted form contains error or the manageGroupes tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendChangeMemberRole(int group) {
    logger.debug("POST change member role");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    // check form
    Form<RoleForm> roleForm = formFactory.form(RoleForm.class).bindFromRequest();
    GroupSubscription subscription = user.belongsTo(group);

    // check group id and subscription
    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.invitation.error"));
      return CompletableFuture.supplyAsync(() ->
          internalServerError(manageGroups.render(user.getContributor(), messages)), context.current());
    }

    if (roleForm.hasErrors()) {
      logger.debug("error in form " + roleForm.errors());
      return CompletableFuture.supplyAsync(() ->
        ok(changeMemberRole.render(subscription, formFactory.form(RoleForm.class).fill(new RoleForm(subscription)), helper)), context.current());
    }

    RoleForm changeRole = roleForm.get();

    if (changeRole.getUserId() != null && changeRole.getUserId() != -1L && changeRole.getRoleId() <= subscription.getRole().id() && changeRole.getRoleId() <= EContributorRole.OWNER.id()) {
      // Change this contributor role into the group
      Contributor c = contributorFactory.retrieveContributor(changeRole.getUserId());
      if(c != null && !c.getId().equals(user.getId())) {
        GroupSubscription g = c.belongsTo(group);
        if(g != null && g.getRole().id() != changeRole.getRoleId() && g.getRole().id() <= subscription.getRole().id()){
          try {
            g.setRole(EContributorRole.value(changeRole.getRoleId()));
            g.updateRole(changeRole.getUserId());
          } catch (PersistenceException | PermissionException e) {
            logger.error("unable to change member role (" + changeRole.getInvitation() + ") to group " + group, e);
            messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.invitation.notinvited", group));
          }
        }
      }
    }

    // check if we do not have messages (meaning all went smoothly)
    if (messages.isEmpty()) {
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.invite.ok", group+""));
    }
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Get send mail to group modal page
   *
   * @param group a group id
   * @return the modal page to send mail to members of a group or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getMailToGroupModal(int group) {
    logger.debug("GET modal page to invite people");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription g = user.belongsTo(group);
    // if user is not owner -> unauthorized
    if (g == null || g.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.completedFuture(unauthorized(message.render(messages)));
    }

    // all good, send modal
    return CompletableFuture.supplyAsync(() ->
            ok(sendMailModal.render(formFactory.form(GroupMailForm.class).fill(new GroupMailForm(group)))), context.current());
  }

  /**
   * Handle post request to send mail to group
   *
   * @return the send mail to group modal if submitted form contains error or the manageGroupes tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendMailToGroup() {
    logger.debug("SEND mail to members of the group");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    // check form
    Form<GroupMailForm> groupMailForm = formFactory.form(GroupMailForm.class).bindFromRequest();
    if (groupMailForm.hasErrors()) {
      logger.debug("error in form " + groupMailForm.errors());
      return CompletableFuture.supplyAsync(() -> badRequest(sendMailModal.render(groupMailForm)), context.current());
    }

    // check group id and subscription
    GroupMailForm mailForm = groupMailForm.get();
    GroupSubscription group = user.belongsTo(mailForm.getGroupId());
    if (group == null || group.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + mailForm.getGroupId());
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.invitation.error"));
      return CompletableFuture.supplyAsync(() ->
              internalServerError(manageGroups.render(user.getContributor(), messages)), context.current());
    }

    try {
      // send mail to members of the group
      mailer.sendMail(new WebdebMail(
              WebdebMail.EMailType.GROUP_MAIL,
              mailForm.getTitle(),
              mailForm.getContent(),
              getMailToGroup(mailForm.getGroupId()),
              be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1).url(),
              ctx().lang()));
        messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.invite.ok", group.getGroup().getGroupName()));
    } catch (MailerException e) {
      logger.warn("unable to send mail to warn users group has been closed", e);
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.close.nomail"));
    }

    return CompletableFuture.supplyAsync(() ->
            ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Get send mail to group modal page
   *
   * @param contributionType a contribution type
   * @return the modal page to send mail to members of a group or an unauthorized (401)
   * with the message template containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> editContributionsToExplore(int contributionType) {
    logger.debug("GET modal page to manage contributions to explore " + contributionType);
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    //Group group = user.getGroup();
    Group group = contributorFactory.retrieveGroup(Group.getGroupPublic());
    EContributionType cType = EContributionType.value(contributionType) == null ? EContributionType.ALL : EContributionType.value(contributionType);

    // if user is not owner -> unauthorized
    if (group == null || user.getERole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.completedFuture(unauthorized(message.render(messages)));
    }

    List<ContributionToExplore> contributions = contributorFactory.getContributionsToExploreForGroup(contributionType, group.getGroupId());

    // all good, send modal
    return CompletableFuture.supplyAsync(() ->
            ok(manageContributionsToExplore.render(formFactory.form(ContributionsToExploreForm.class)
                    .fill(new ContributionsToExploreForm(contributions, group,cType, user, ctx().lang().code()))
                    , cType)), context.current());
  }

  /**
   * Post contributions to explore
   *
   * @param contributionType a contribution type
   * @return the send mail to group modal if submitted form contains error or the manageGroupes tab with result message(s)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> sendContributionsToExplore(int contributionType) {
    logger.debug("POST contributions to explore");
    WebdebUser user = sessionHelper.getUser(ctx());
    EContributionType cType = EContributionType.value(contributionType) == null ? EContributionType.ALL : EContributionType.value(contributionType);

    // check form
    Form<ContributionsToExploreForm> ctefForm = formFactory.form(ContributionsToExploreForm.class).bindFromRequest();
    if (ctefForm.hasErrors()) {
      logger.debug("error in form " + ctefForm.errors());
      return CompletableFuture.supplyAsync(() ->
              badRequest(manageContributionsToExplore.render(ctefForm, cType)), context.current());
    }

    // check group id and subscription
    ContributionsToExploreForm exploreForm = ctefForm.get();
    GroupSubscription group = user.belongsTo(exploreForm.getGroupId());
    if (group == null || group.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("unable to retrieve group or user " + user.getContributor().getEmail() + " is not owner of " + exploreForm.getGroupId());
      return CompletableFuture.supplyAsync(Results::unauthorized, context.current());
    }


    try {
      exploreForm.save(user.getId(), user.getGroupId());
    } catch (PersistenceException | PermissionException e) {
      logger.debug("Error when save explore form");
      return CompletableFuture.supplyAsync(Results::internalServerError, context.current());
    }


    return CompletableFuture.supplyAsync(Results::ok, context.current());
  }

  /**
   * Get email addresses of all members of given group
   *
   * @param group a group id (must exist)
   * @return the emails of all members of given group
   */
  private List<String> getMailToGroup(int group) {
    return contributorFactory.retrieveGroup(group).getMembers().stream().map(s ->
        s.getContributor().getEmail()).collect(Collectors.toList());
  }

  /**
   * Search for contributors with given query in their names or email
   *
   * @param query a term to look for in contributors' details
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of jsonified contributor holders
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> searchContributors(String query, int fromIndex, int toIndex) {
    WebdebUser user = sessionHelper.getUser(ctx());

    return CompletableFuture.supplyAsync(() ->
        ok(Json.toJson(contributorFactory.findContributors(query, fromIndex, toIndex).stream().map(c ->
            new SimpleContributorHolder(c, user, ctx().lang().code())).collect(Collectors.toList()))),
        context.current());
  }

  /**
   * Get page to see all contribution of a group
   *
   * @param group a group id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return either the hack page (if context user is not member of group with unauthorized status - 500)
   * or the page with all contributions of given group
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> allContributions(int group, int fromIndex, int toIndex) {
    logger.debug("GET all contributions in group " + group);

    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);

    // check whether user belongs to group and contribution visibility is at least group-public, otherwise -> hack page
    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // get all contributions, grouped by their contributors
    Map<ContributorHolder, List<ContributionHolder>> result = getContributionsOfGroup(subscription.getGroup(), user.getId(), null, fromIndex, toIndex);
    return CompletableFuture.supplyAsync(() ->
        ok(groupContributions.render(subscription.getGroup(), result, user)), context.current());
  }

  /**
   * Get page to validate contributions from a group (formerly also for marks contributions, but it was judged later as a non-pedagogical way to learn)
   *
   * @param group a group id
   * @return either the hack page (if context user is not owner of group in an unauthorized 500 status)
   * or the mark page for given group
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> markContributions(int group) {
    logger.debug("GET validation page for group " + group);

    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = userIsOwnerOf(user, group);

    // check whether user belongs to group and is either an owner or an admin, otherwise -> hack page
    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    Map<ContributorHolder, List<ContributionHolder>> result = getContributionsOfGroup(subscription.getGroup(), user.getId(), false, 0, 0);
    List<Long> unorderedList = result.keySet().parallelStream().map(ContributionHolder::getId).collect(Collectors.toList());
    ContributionValidationForm form = makeContributionMarkForm(result);

    // return mark page
    return CompletableFuture.supplyAsync(() ->
        ok(markGroupContributions.render(user, subscription.getGroup(), result, unorderedList,
            formFactory.form(ContributionValidationForm.class).fill(form), null)), context.current());
  }

  /**
   * Save all markings passed in form
   *
   * @param group a group id
   * @return the group settings page of given group with a message of the success/failure of requested
   * action, the markGroupContributions page if passed form contains errors (bad request - 401), or
   * the hack page in an unauthorized reponse (500) if user has no sufficient rights on given group
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveContributionMarks(int group) {
    logger.debug("POST save marks for group " + group);

    Map<String, String> messages = new HashMap<>();
    // check if group exists and if context user is owner of group
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);
    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("group does not exist, or contributor " + user.getId() + " is not owner of group " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    Form<ContributionValidationForm> form = formFactory.form(ContributionValidationForm.class).bindFromRequest();
    if (form.hasErrors()) {
      logger.debug("form has errors ", form.errors());
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "error.form"));
      Map<ContributorHolder, List<ContributionHolder>> result = getContributionsOfGroup(subscription.getGroup(), user.getId(), false, 0, 0);
      List<Long> unorderedList = result.keySet().stream().map(ContributionHolder::getId).collect(Collectors.toList());
      return CompletableFuture.supplyAsync(() ->
          badRequest(markGroupContributions.render(user, subscription.getGroup(), result, unorderedList, form, messages)),
          context.current());
    }

    // no error in form, save marks and validated state
    List<Contribution> toSave = new ArrayList<>();
    form.get().getValidations().stream().flatMap(Collection::stream)
        .filter(m -> m != null && (m.isValidated()))
        .forEach(m -> {
            // something to save, update retrieve API contribution and add to save list
            Contribution c = textFactory.retrieve(m.getId(), EContributionType.value(m.getType()));
            c.setValidated(textFactory.getValidationState(m.isValidated()));
            toSave.add(c);
        }
    );

    try {
      textFactory.saveMarkings(toSave);
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.mark.success"));

    } catch (PersistenceException e) {
      logger.error("unable to save validation", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), e.getMessage()));
    }

    // delete all contributions that have been invalidated ??

    // now return updated form
    Map<ContributorHolder, List<ContributionHolder>> result = getContributionsOfGroup(subscription.getGroup(), user.getId(), false, 0, 0);
    List<Long> unorderedList = result.keySet().stream().map(ContributionHolder::getId).collect(Collectors.toList());
    ContributionValidationForm newform = makeContributionMarkForm(result);

    return CompletableFuture.supplyAsync(() ->
            ok(markGroupContributions.render(user, subscription.getGroup(), result, unorderedList,
                formFactory.form(ContributionValidationForm.class).fill(newform), messages)),
        context.current());
  }

  /**
   * Get page to select contributions to merge into the public website
   *
   * @param group a group id
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the page to select validated contributions from given group that may be injected into the public website or
   * the hack page in an unauthorized reponse (500) if user has no sufficient rights on given group
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> mergeContributions(int group, int fromIndex, int toIndex) {
    logger.debug("GET merge contributions for group " + group);

    // check if context user is owner of this group
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);

    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("group does not exist, or contributor " + user.getId() + " is not owner of group " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // all good, retrieve all validated contributions  (flatten values since we got a map) and display merge page
    List<ContributionHolder> contributions = getContributionsOfGroup(subscription.getGroup(), user.getId(), true, fromIndex, toIndex).values().parallelStream()
        .flatMap(List::stream).collect(Collectors.toList());

    // sort map and init form object to pass to template
    Collections.sort(contributions);
    ContributionMergeForm form = new ContributionMergeForm(contributions);
    return CompletableFuture.completedFuture(
        ok(mergeGroupContributions.render(user, subscription.getGroup(), contributions,
            formFactory.form(ContributionMergeForm.class).fill(form), null)));
  }

  /**
   * Merge contributions passed in form from given group into the public website
   *
   * @param group a group id
   * @return the page to select validated contributions from given group that may be injected into the public website with a
   * message explaining the result of the save action and the (possibibly empty) remaining contributions
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> saveContributionsMerge(int group) {
    logger.debug("POST merge contributions from group " + group);

    // check if context user is owner of this group
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);

    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("group does not exist, or contributor " + user.getId() + " is not owner of group " + group);
      return CompletableFuture.supplyAsync(() -> badRequest(hack.render(user)), context.current());
    }

    Form<ContributionMergeForm> form = formFactory.form(ContributionMergeForm.class).bindFromRequest();
    if (form.hasErrors()) {
      flash(SessionHelper.ERROR, i18n.get(ctx().lang(), "error.crash"));
      return CompletableFuture.supplyAsync(() ->
          redirect(routes.GroupActions.mergeContributions(group, 0, 0)), context.current());
    }

    // no error in form, add all validated contributions of form into public website and remove from current group

    StringBuilder errorMsg = new StringBuilder();
    form.get().getMerge().stream().filter(ContributionMerge::getToMerge).forEach(m -> {
      logger.debug("will send to public group contribution " + m.getId());
      Contribution contribution = textFactory.retrieve(m.getId(), EContributionType.value(m.getType()));
      try {
        doContributionMerge(contribution, group, user);
      } catch (FormatException | PermissionException | PersistenceException e) {
        logger.error("unable to merge contribution in public group", e);
        errorMsg.insert(0, e.getMessage());
      }
    });

    // if we got an error, warn user, otherwise show the success message
    if (errorMsg.length() == 0) {
      flash(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.merge.success"));
    } else {
      flash(SessionHelper.WARNING, i18n.get(ctx().lang(), errorMsg.toString()));
    }

    // redirect to the merge page with updated view
    return CompletableFuture.supplyAsync(() ->
        redirect(routes.GroupActions.mergeContributions(group, 0, 0)), context.current());
  }

  /**
   * Do merge for one contribution into the public website
   *
   * @param contribution the contribution to merge into the public website
   * @param group a group id
   * @param user the user that operate the merge
   */
  private void doContributionMerge(Contribution contribution, int group, WebdebUser user) throws FormatException, PermissionException, PersistenceException{
    int defaultGroup = contributorFactory.getDefaultGroup().getGroup().getGroupId();

    if(contribution != null) {
      contribution.addInGroup(defaultGroup);
      contribution.save(user.getId(), defaultGroup);

      switch (contribution.getType()) {
        case ACTOR:
          Actor actor = (Actor) contribution;

          for(Affiliation aff : actor.getAffiliations()) {
            aff.addInGroup(defaultGroup);
            aff.save(user.getId(), defaultGroup);
          }
          break;
        case DEBATE:
        case TEXT:
          ContextContribution context = (ContextContribution) contribution;

          for (ContextHasCategory link : context.getContextCategories()) {
            link.addInGroup(defaultGroup);
            link.save(user.getId(), defaultGroup);
          }

          for (ArgumentJustification link : context.getAllArgumentJustificationLinks()) {
            link.addInGroup(defaultGroup);
            link.save(user.getId(), defaultGroup);
          }

          for (CitationJustification link : context.getAllCitationJustificationLinks()) {
            link.getCitation().addInGroup(defaultGroup);
            link.getCitation().save(user.getId(), defaultGroup);

            link.addInGroup(defaultGroup);
            link.save(user.getId(), defaultGroup);
          }

          if(contribution.getType() == EContributionType.TEXT) {
            Text text = (Text) contribution;

            for (Citation citation : text.getTextCitations()) {
              citation.addInGroup(defaultGroup);
              citation.save(user.getId(), defaultGroup);
            }
          } else {
            Debate debate = (Debate) contribution;

            List<DebateTag> links = debate.getTagDebates(user.getId(), group);

            if(links != null) {
              for (DebateTag link : links) {
                link.addInGroup(defaultGroup);
                link.save(user.getId(), defaultGroup);
              }
            }
          }
          break;
      }

      // now remove from group
      //contribution.removeFromGroup(group, user.getId());

    }
  }

  /**
   * Empty given group, ie remove all members and delete all contributions of that group. If deleteGroup is true,
   * also delete the group itself.
   *
   * @param group a group id
   * @param deleteGroup true if the group must be deleted also
   * @return a message template with the result of the request (typically, a success message, or an error message if
   * context user is not an owner of given group)
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> emptyGroup(int group, boolean deleteGroup) {
    logger.debug("GET remove group " + group + " completely ? " + deleteGroup);
    Map<String, String> messages = new HashMap<>();

    // check if context user is owner of this group
    WebdebUser user = sessionHelper.getUser(ctx());
    GroupSubscription subscription = user.belongsTo(group);

    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("group does not exist, or contributor " + user.getId() + " is not owner of group " + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // now try to clean up this group
    try {
      List<String> emails = subscription.getGroup().clean(user.getId(), deleteGroup);
      // send mail to users to warn them
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.GROUP_CLOSED, emails,
          be.webdeb.presentation.web.controllers.routes.Application.index().url(),
          Arrays.asList(subscription.getGroup().getGroupName(), user.getContributor().getFullname()),
          ctx().lang()));
      messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.close.ok." + deleteGroup, subscription.getGroup().getGroupName()));

    } catch (PermissionException | PersistenceException e) {
      // permission exception should not be raised since we ensure proper permission already
      logger.debug("unable to remove group " + group, e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.close.error", subscription.getGroup().getGroupName()));

    } catch (MailerException e) {
      logger.warn("unable to send mail to warn users group has been closed", e);
      messages.put(SessionHelper.WARNING, i18n.get(ctx().lang(), "group.close.nomail"));
    }

    // send back group management settings page
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Add a contribution into one of current contributor's group
   *
   * @param group the group id
   * @param contribution the contribution id
   * @param type the contribution type id
   * @return a message template giving the result of this request
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> addContributionToGroup(int group, Long contribution, int type) {
    logger.debug("GET add contribution into group " + group);

    WebdebUser user = sessionHelper.getUser(ctx());
    Map<String, String> messages = new HashMap<>();
    GroupSubscription subscription = user.belongsTo(group);

    // does contribution exists?
    EContributionType cType = EContributionType.value(type);
    if (cType == null) {
      logger.error("given contribution type is invalid " + type);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "entry.import.fail", subscription.getGroup().getGroupName(), ""));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }
    Contribution c = textFactory.retrieve(contribution, cType);
    if (c == null) {
      logger.error("no such contribution with id " + contribution + " and type " + cType.name());
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "entry.import.fail", subscription.getGroup().getGroupName(), ""));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // does user belong to group ?
    if (subscription == null || c.getInGroups().stream().anyMatch(e -> e.getGroupId() == group)) {
      logger.error("group does not exist, or contributor does not belong to group, or contribution already in group " + group);
      if(subscription != null){
        messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "entry.import.fail", subscription.getGroup().getGroupName(), i18n.get(ctx().lang(), "label.adlreadyInGroup")));
      }else{
        messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "entry.import.fail", i18n.get(ctx().lang(), "label.unknown"), ""));
      }
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // let's add this into given group
    try {
      c.addInGroup(group);
      c.save(user.getId(), group);
    } catch (FormatException | PersistenceException | PermissionException e) {
      logger.error("unable to add contribution " + contribution + " in group " + group, e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "entry.import.fail", subscription.getGroup().getGroupName(), ""));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // all good
    messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "entry.import.success", subscription.getGroup().getGroupName()));
    return CompletableFuture.completedFuture(ok(message.render(messages)));
  }

  /**
   * Retrieve a contributor group
   *
   * @param groupId the id of group to retrieve
   * @return a message template giving the result of this request
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> retrieveContributorGroup(int groupId) {
    logger.debug("GET group " + groupId);

    Group group = contributorFactory.retrieveGroup(groupId);
    if(group != null) {
      return CompletableFuture.completedFuture(ok(groupChooseButton.render(group, false)));
    }
    return CompletableFuture.completedFuture(badRequest());
  }

  /**
   * Send followed state to given group for given contributor
   *
   * @param groupId the id of group to follow
   * @param follow the follow state to put
   * @return a message template giving the result of this request
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> followGroup(int groupId, boolean follow){
    logger.debug("POST followed state to group " + groupId);
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    String error = followGroups(groupId, user.getId(), follow);
    // let's add this into given group
    if(!error.equals("")) {
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), error));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

    // all good
      return CompletableFuture.supplyAsync(() ->
              ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Send followed state for given contributor
   *
   * @return a message template giving the result of this request
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> followGroups(){
    logger.debug("POST followed states for user");
    Map<String, String> messages = new HashMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());
    String error = "";
    // let's add this into given group

    // for each edited links from similarity links, check if new shade is -1. Then delete it. Otherwise, apply the new shade
    for (GroupFollowedForm followForm : Json.fromJson(request().body().asJson().get("followed"), GroupFollowedForm[].class)) {
      error = followGroups(followForm.getGroupId(), user.getId(), followForm.isFollowed());
      if(!error.equals("")){
        break;
      }
    }

    if(!error.equals("")){
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), error));
      return CompletableFuture.completedFuture(badRequest(message.render(messages)));
    }

      // all good
      return CompletableFuture.supplyAsync(() ->
              ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Check if given user belongs to given group and if he's at least an owner of it
   *
   * @param user a webdeb user
   * @param group a group id
   * @return the group subscription of given user in given group, if user owns that group
   */
  private GroupSubscription userIsOwnerOf(WebdebUser user, int group) {
    GroupSubscription subscription = user.belongsTo(group);
    if (subscription == null || subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("group does not exist, or contributor " + user.getId() + " is not owner of group " + group);
      return null;
    }
    return subscription;
  }

  /**
   * Ban or re-accept given contributor from given group
   *
   * @param user current user making the ban request
   * @param contributor the contributor to ban
   * @param group the group id
   * @param revoke true if given contributor  must be banned, false if it must be re-accepted
   * @return the user settings page on group management pane, or a bad request with the message template
   * containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  private CompletionStage<Result> banFromGroup(WebdebUser user, Contributor contributor, int group, boolean revoke) {
    Map<String, String> messages = new HashMap<>();
    GroupSubscription subscription = user.belongsTo(group);

    // check if user belongs to group and is owner of it
    if (subscription == null) {
      logger.error("no group could be retrieved or user does not belong to group " + group);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }
    if (subscription.getRole().id() < EContributorRole.OWNER.id()) {
      logger.error("user " + subscription.getContributor().getEmail() + " is not owner of group "  + group);
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    // now we can (un-)ban user
    try {
      subscription.getGroup().setBanned(contributor.getId(), revoke);
    } catch (PersistenceException e) {
      logger.error("unable to revoke member from group", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // warn user he's been (un)banned
    try {
      WebdebMail.EMailType type = revoke ? WebdebMail.EMailType.BAN_FROM_GROUP : WebdebMail.EMailType.UNBAN_FROM_GROUP;
      String url = revoke ? be.webdeb.presentation.web.controllers.routes.Application.terms().url()
          : be.webdeb.presentation.web.controllers.routes.Application.index().url();
      mailer.sendMail(new WebdebMail(type, contributor.getEmail(), url,
          Arrays.asList(subscription.getGroup().getGroupName(), user.getContributor().getFullname()),
          ctx().lang()));
    } catch (MailerException e) {
      logger.error("unable to warn banned user " + contributor.getEmail(), e);
    }

    // all good
    messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "group.revoke.ok." + revoke,
        contributor.getFirstname() + " " + contributor.getLastname(), subscription.getGroup().getGroupName()));
    return CompletableFuture.supplyAsync(() ->
        ok(manageGroups.render(user.getContributor(), messages)), context.current());
  }

  /**
   * Ban or re-accept given contributor from whole platform
   *
   * @param user the user making the ban request
   * @param contributor the contributor to ban or re-accept
   * @param revoke true if given contributor must be banned, false if it must be re-accepted
   * @return the user settings page on admin management pane, or a bad request with the message template
   * containing the error message if an error occurred
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  private CompletionStage<Result> banFromPlatform(WebdebUser user, Contributor contributor, boolean revoke) {
    Map<String, String> messages = new HashMap<>();

    GroupSubscription subscription = user.belongsTo(SessionHelper.PUBLIC_GROUP);
    if (subscription.getRole().id() < EContributorRole.ADMIN.id()) {
      logger.error("user " + subscription.getContributor().getEmail() + " is not admin");
      return CompletableFuture.completedFuture(unauthorized(hack.render(user)));
    }

    try {
      contributor.banFromPlatform(revoke);
    } catch (PersistenceException e) {
      logger.error("unable to revoke member", e);
      messages.put(SessionHelper.ERROR, i18n.get(ctx().lang(), "group.change.error"));
      return CompletableFuture.supplyAsync(() -> badRequest(message.render(messages)), context.current());
    }

    // warn user he's been (un)banned
    try {
      WebdebMail.EMailType type = revoke ? WebdebMail.EMailType.BAN_FROM_GROUP : WebdebMail.EMailType.UNBAN_FROM_GROUP;
      String url = revoke ? be.webdeb.presentation.web.controllers.routes.Application.terms().url()
          : be.webdeb.presentation.web.controllers.routes.Application.index().url();
      mailer.sendMail(new WebdebMail(type, contributor.getEmail(), url,
          Arrays.asList(contributorFactory.getDefaultGroup().getGroup().getGroupName(), user.getContributor().getFullname()),
          ctx().lang()));
    } catch (MailerException e) {
      logger.error("unable to warn banned user " + contributor.getEmail(), e);
    }

    // all good, reload settings page
    messages.put(SessionHelper.SUCCESS, i18n.get(ctx().lang(), "admin.revoke.ok." + revoke,
        contributor.getFirstname() + " " + contributor.getLastname()));

    Form<SearchForm> form = getSettingsSearchForm(user.getId());

    return sendOk(userSettings.render(ESettingsPane.ADMIN, user.getContributor(), form,true, helper, user, messages));
  }

  /**
   * Get all contributions from given group and build a map of (contributor, list of contributions) to be
   * validated / validated by a group owner
   *
   * @param group a group
   * @param contributor a contributor id for whom the content will be displayed
   * @param validated true if only validated contributions must be retrieved, null to get all contributions from group
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the map of (contributors, list of contributions) to be validated for given group
   */
  private Map<ContributorHolder, List<ContributionHolder>> getContributionsOfGroup(Group group, Long contributor, Boolean validated, int fromIndex, int toIndex) {
    // create map of contributors and their contributions
    Map<ContributorHolder, List<ContributionHolder>> result = new TreeMap<>();
    WebdebUser user = sessionHelper.getUser(ctx());

    if(group != null) {
      // build search criteria
      List<Map.Entry<EQueryKey, String>> criteria = new ArrayList<>();
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.GROUP, String.valueOf(group.getGroupId())));
      if(group.getContributionVisibility().id() > EContributionVisibility.PUBLIC.id())
        criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.NOT_IN_GROUP, String.valueOf(Group.getGroupPublic())));
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, "0"));
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, "1"));
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, "3"));
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, "4"));
      criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.CONTRIBUTION_TYPE, "5"));
      if(validated != null)
        criteria.add(new AbstractMap.SimpleEntry<>(EQueryKey.VALIDATED, String.valueOf(validated)));
    List<ContributionHolder> contributions = helper.toHolders(
            textFactory.findByCriteria(criteria, false, fromIndex, toIndex), user, ctx().lang().code());

      // must loop contribution in order of ids, to ensure that if for all arguments, if their texts were in
      // contribution list, they have been looped already (arguments are always created after their texts => higher id)
      // to avoid to cast back to int and because we may not have equality, simply check if one id is bigger than the other
      contributions.sort((o1, o2) -> o1.getId() > o2.getId() ? 1 : -1);
      contributions.forEach(holder -> {
        // Get create of contribution or if is an argument take the latest contributor in the group
        ContributorHolder creator = new ContributorHolder((holder.getType().equals(EContributionType.ARGUMENT) ?
                textFactory.getLastContributorInGroup(holder.getId(), group.getGroupId()) :
                textFactory.getCreator(holder.getId()).getContributor()), user, ctx().lang().code());
        // set creator id, will be used to style texts that have not been created by a contributor, but must be
        // displayed because contributor extracted arguments from it
        holder.setCreator(creator.getId());
        holder.setCreatorName(creator.getFullname());
        // add creator if not yet in map
        if (!result.containsKey(creator)) {
          result.put(creator, new ArrayList<>());
        }
        result.get(creator).add(holder);

        // if it is an citation, must check that contributor has also the text
        // because in views, citations are grouped under their texts
        if (holder.getType().equals(EContributionType.CITATION)) {
          CitationHolder citation = (CitationHolder) holder;
          if (result.get(creator).stream().noneMatch(h -> h.getId().equals(citation.getTextId()))) {
            ContributionHolder missing = helper.toHolder(textFactory.retrieve(citation.getTextId()), user, ctx().lang().code());
            // we are sure that we will have an holder back, otherwise data are corrupted
            result.get(creator).add(missing);
          }
        }
      });
    }
    return result;
  }

  /**
   * Helper method to build a validations form from a map of contributor-list of contributions
   *
   * @param contributions all contributions grouped by their contributors
   * @return the marks form containing the needed data to validated contributions
   */
  private ContributionValidationForm makeContributionMarkForm(Map<ContributorHolder, List<ContributionHolder>> contributions) {
    // now create ordered list of form objects
    List<List<ContributionValidation>> validations = new LinkedList<>();
    NumberFormat formatter = NumberFormat.getInstance(values.getLocale(ctx().lang().code()));
    formatter.setMaximumIntegerDigits(2);
    formatter.setMaximumFractionDigits(2);
    formatter.setMinimumFractionDigits(0);
    contributions.entrySet().forEach(e ->
            validations.add(e.getValue().parallelStream().map(m -> new ContributionValidation(m, formatter)).collect(Collectors.toList())));
    ContributionValidationForm form = new ContributionValidationForm();
    form.setValidations(validations);
    return form;
  }

    /**
     * Send followed state to given group for given contributor
     *
     * @param groupId the id of group to follow
     * @param contributor the contributor that want to follow
     * @param follow the follow state to put
     * @return the error or an empty string if all is good
     */
  private String followGroups(int groupId, Long contributor, boolean follow){
    String error = "";
    try {
      contributorFactory.setFollowGroup(groupId, contributor, follow);
    } catch (PersistenceException e) {
      error = e.getMessage();
    }
    return error;
  }
}


