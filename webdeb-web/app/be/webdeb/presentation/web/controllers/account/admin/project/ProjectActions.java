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

package be.webdeb.presentation.web.controllers.account.admin.project;

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.project.ProjectGroup;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.CommonController;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.account.admin.project.*;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * All user admin actions for managing project over the platform
 *
 * @author Martin Rouffiange
 */
public class ProjectActions extends CommonController {

    @Inject
    private ProjectFactory projectFactory;

    /**
     * Display the manage projects page
     *
     * @return the page to manage projects
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> manageProjects() {
        logger.debug("GET manage projects page");
        // TODO REMOVE
        logger.debug("Total mem : " + (Runtime.getRuntime().totalMemory() / (double) 1000000000)+"///");
        logger.debug("Free mem : " + (Runtime.getRuntime().freeMemory() / (double) 1000000000)+"///");
        logger.debug("Used mem : " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double) 1000000000)+"///");

        WebdebUser user = sessionHelper.getUser(ctx());
        Map<String, String> messages = new HashMap<>();

        List<ProjectForm> forms = toProjectsForm(projectFactory.getAllProjects());

        Collections.sort(forms);

        return CompletableFuture.supplyAsync(() ->
                ok(adminProjects.render(forms, user, messages)), context.current());
    }

    /**
     * Display the manage given project page
     *
     * @param projectId the id of the project to manage
     * @return the page to manage given project
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> manageProject(int projectId) {
        logger.debug("GET manage project page for " + projectId);

        WebdebUser user = sessionHelper.getUser(ctx());
        Map<String, String> messages = new HashMap<>();

        Project project = projectFactory.retrieveProject(projectId);

        if(project == null){
            return CompletableFuture.supplyAsync(Results::badRequest, context.current());
        }

        ProjectForm form = toProjectForm(project);
        form.setGroups(toProjectGroupsForm(form.getProjet(), form.getProjet().getGroups()));
        form.getGroups().forEach(e2 -> e2.setSubgroups(toProjectSubgroupsForm(form.getProjet(), e2.getGroup().getSubgroups())));

        return CompletableFuture.supplyAsync(() ->
                ok(adminProject.render(form, user, messages)), context.current());
    }

    /**
     * Display the project activity modal
     *
     * @param projectId the project id
     * @return the project activity modal if project existing, bad request otherwise
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> getProjectActivity(Integer projectId) {

        Project project = projectFactory.retrieveProject(projectId);

        if(project == null){
            return CompletableFuture.supplyAsync(Results::badRequest, context.current());
        }

        return CompletableFuture.supplyAsync(() ->
                ok(projectActivityReportModal.render(toProjectForm(project))),
                context.current());
    }

    /**
     * Generate users of a given project
     *
     * @param projectId a project id
     * @return bad request if given project is not found or if there is a problem with the generation. Ok with the
     *  generated users file link.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> generateProjectUsers(Integer projectId) {
        logger.debug("POST generate users project " + projectId);

        Project project = projectFactory.retrieveProject(projectId);
        if (project == null) {
            return CompletableFuture.supplyAsync(Results::badRequest, context.current());
        }

        try {
            projectFactory.generateProjectUser(projectId);
            return CompletableFuture.supplyAsync(Results::ok, context.current());
        } catch (PersistenceException e) {
            return CompletableFuture.supplyAsync(Results::internalServerError, context.current());
        }
    }

    /**
     * Delete users of a given project
     *
     * @param projectId a project id
     * @return bad request if given project is not found; ok otherwise
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> deleteProjectUsers(Integer projectId) {
        logger.debug("POST delete users project " + projectId);

        Project project = projectFactory.retrieveProject(projectId);
        if (project == null || project.getBeginDate().before(new Date()) || !project.hasTmpContributors()) {
            logger.debug("Project is null or is already begin or has no contributors yet");
            return CompletableFuture.supplyAsync(Results::badRequest, context.current());
        }

        projectFactory.deleteProjectUser(projectId);

        return CompletableFuture.supplyAsync(Results::ok, context.current());
    }

    /**
     * Display the page to edit a project
     *
     * @param projectId the project id to edit
     * @return ok with the edition form
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> editProject(Integer projectId) {
        logger.debug("GET project properties of " + projectId);
        Map<String, String> messages = new HashMap<>();

        // prepare wrapper for project
        Project project = projectFactory.retrieveProject(projectId);
        if (project != null) {
            if(project.isInProgress()) {
                return sendBadRequest(i18n.get(ctx().lang(),"project.inprogress"));
            }

            ProjectForm form = new ProjectForm(project);
            return sendOk(editProject.render(formFactory.form(ProjectForm.class).fill(form), null));
        }

        // set group value in form
        ProjectForm form = new ProjectForm();
        return sendOk(editProject.render(formFactory.form(ProjectForm.class).fill(form), messages));
    }

    /**
     * Display the page to edit a project group
     *
     * @param projectId the project id that owns the group
     * @param projectGroupId the project group id to edit
     * @return bad request if the given project is not found, ok with the project group form otherwise
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> editProjectGroup(Integer projectId, Integer projectGroupId) {
        logger.debug("GET project group properties of " + projectGroupId + " with project " + projectId);
        Map<String, String> messages = new HashMap<>();

        // does project exists ?
        Project project = projectFactory.retrieveProject(projectId);
        if (project == null) {
            return sendBadRequest(i18n.get(ctx().lang(),"project.project.notfound"));
        }

        // prepare wrapper for project group
        ProjectGroup group = projectFactory.retrieveGroup(projectGroupId);
        if (group != null) {
            if(project.isInProgress()) {
                return sendBadRequest(i18n.get(ctx().lang(),"project.inprogress"));
            }

            ProjectGroupForm form = new ProjectGroupForm(group, project);
            return sendOk(editProjectGroup.render(formFactory.form(ProjectGroupForm.class).fill(form), null));
        }

        // set project in form
        ProjectGroupForm form = new ProjectGroupForm(project);
        return sendOk(editProjectGroup.render(formFactory.form(ProjectGroupForm.class).fill(form), messages));
    }

    /**
     * Display the page to edit a project subgroup
     *
     * @param projectId the project id context
     * @param projectGroupId the project group id that owns the subgroup
     * @param projectSubgroupId the project subgroup id to edit
     * @return badrequest if the given project or project group are no found, ok with project subgroup form otherwise
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> editProjectSubgroup(Integer projectId, Integer projectGroupId, Integer projectSubgroupId) {
        logger.debug("GET project subgroup properties of " + projectSubgroupId + " with group " + projectGroupId + " for the project " + projectId);

        // does project exists ?
        Project project = projectFactory.retrieveProject(projectId);

        if (project == null) {
            return sendBadRequest(i18n.get(ctx().lang(),"project.project.notfound"));
        }

        // does project group exists ?
        ProjectGroup group = projectFactory.retrieveGroup(projectGroupId);

        if (group == null) {
            return sendBadRequest(i18n.get(ctx().lang(),"project.group.notfound"));
        }

        // Does the given project and group linked ?
        if(project.getGroups().stream().noneMatch(e -> e.getId().equals(group.getId()))){
            return sendBadRequest(i18n.get(ctx().lang(),"project.project.group.notlinked"));
        }

        // prepare wrapper for project subgroup
        ProjectSubgroup subgroup = projectFactory.retrieveSubgroup(projectSubgroupId);
        if (subgroup != null) {
            if(project.isInProgress()) {
                return sendBadRequest(i18n.get(ctx().lang(),"project.inprogress"));
            }

            ProjectSubgroupForm form = new ProjectSubgroupForm(subgroup, project);
            return sendOk(editProjectSubgroup.render(formFactory.form(ProjectSubgroupForm.class).fill(form), null));
        }

        // set project in form
        ProjectSubgroupForm form = new ProjectSubgroupForm(group, project);
        return sendOk(editProjectSubgroup.render(formFactory.form(ProjectSubgroupForm.class).fill(form), null));
    }

    /**
     * Update or create a project
     *
     * @return the project form object if it contains errors or in case of success OK or DB crash 500 with error messages.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> saveProject() {
        logger.debug("POST save properties of project");

        Form<ProjectForm> form = formFactory.form(ProjectForm.class).bindFromRequest();
        Http.Context ctx = ctx();
        Map<String, String> messages = new HashMap<>();

        // check errors, sends back whole form if any
        if (form.hasErrors()) {
            logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
            messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),SessionHelper.ERROR_FORM));
            return CompletableFuture.supplyAsync(() -> badRequest(editProject.render(form, messages)), context.current());
        }

        ProjectForm project = form.get();
        // try to save in DB
        try {
            project.save();
        } catch (FormatException | PersistenceException e) {
            logger.error("unable to save project", e);
            return CompletableFuture.supplyAsync(Results::internalServerError);
        }

        // return success state
        return CompletableFuture.supplyAsync(Results::ok);
    }

    /**
     * Update or create a project group
     *
     * @return the project group form object  if it contains errors or in case of success OK or DB crash 500 with error messages.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> saveProjectGroup() {
        logger.debug("POST save properties of group");

        Form<ProjectGroupForm> form = formFactory.form(ProjectGroupForm.class).bindFromRequest();
        Http.Context ctx = ctx();
        Map<String, String> messages = new HashMap<>();

        // check errors, sends back whole form if any
        if (form.hasErrors()) {
            logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
            messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),SessionHelper.ERROR_FORM));
            return CompletableFuture.supplyAsync(() -> badRequest(editProjectGroup.render(form, messages)), context.current());
        }
        ProjectGroupForm group = form.get();
        // try to save in DB
        try {
            group.save();
        } catch (FormatException | PersistenceException e) {
            logger.error("unable to save project group", e);
            return CompletableFuture.supplyAsync(Results::internalServerError);
        }

        // return success state
        return CompletableFuture.supplyAsync(Results::ok);
    }

    /**
     * Update or create a project subgroup
     *
     * @return the project subgroup form object if it contains errors or in case of success OK or DB crash 500 with error messages.
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public CompletionStage<Result> saveProjectSubgroup() {
        logger.debug("POST save properties of subgroup");

        Form<ProjectSubgroupForm> form = formFactory.form(ProjectSubgroupForm.class).bindFromRequest();
        Http.Context ctx = ctx();
        Map<String, String> messages = new HashMap<>();

        // check errors, sends back whole form if any
        if (form.hasErrors()) {
            logger.debug("form has errors " + form.errors() + "\nData:" + form.data());
            messages.put(SessionHelper.WARNING, i18n.get(ctx.lang(),SessionHelper.ERROR_FORM));
            return CompletableFuture.supplyAsync(() -> badRequest(editProjectSubgroup.render(form, messages)), context.current());
        }

        ProjectSubgroupForm subgroup = form.get();

        // try to save in DB
        try {
            subgroup.save();
        } catch (FormatException | PersistenceException e) {
            logger.error("unable to save project subgroup", e);
            return CompletableFuture.supplyAsync(Results::internalServerError);
        }

        // return success state
        return CompletableFuture.supplyAsync(Results::ok);
    }

    /*
     * Private helpers
     */

    /**
     * Converts a list of api projects to project forms
     *
     * @return the corresponding list of project forms
     */
    private List<ProjectForm> toProjectsForm(List<Project> projects){
        List<ProjectForm> projectsForm = new ArrayList<>();
        projects.forEach(e -> projectsForm.add(toProjectForm(e)));
        return projectsForm;
    }

    /**
     * Converts an api projects to project form
     *
     * @return the corresponding project form
     */
    private ProjectForm toProjectForm(Project project){
        return new ProjectForm(project);
    }

    /**
     * Converts a list of api project groups to project group forms
     *
     * @return the corresponding list of project group forms
     */
    private List<ProjectGroupForm> toProjectGroupsForm(Project project, List<ProjectGroup> projectGroups){
        List<ProjectGroupForm> projectGroupsForm = new ArrayList<>();
        projectGroups.forEach(e -> projectGroupsForm.add(toProjectGroupForm(project, e)));
        return projectGroupsForm;
    }

    /**
     * Converts an api project groups to project group form
     *
     * @return the corresponding project group form
     */
    private ProjectGroupForm toProjectGroupForm(Project project, ProjectGroup projectGroup){
        return new ProjectGroupForm(projectGroup, project);
    }

    /**
     * Converts a list of api project subgroups to project subgroup forms
     *
     * @return the corresponding list of project subgroup forms
     */
    private List<ProjectSubgroupForm> toProjectSubgroupsForm(Project project, List<ProjectSubgroup> projectSubgroups){
        List<ProjectSubgroupForm> projectSubgroupsForm = new ArrayList<>();
        projectSubgroups.forEach(e -> projectSubgroupsForm.add(toProjectSubgroupForm(project, e)));
        return projectSubgroupsForm;
    }

    /**
     * Converts an api project subgroup to project subgroup form
     *
     * @return the corresponding project subgroup form
     */
    private ProjectSubgroupForm toProjectSubgroupForm(Project project, ProjectSubgroup projectSubgroup){
        return new ProjectSubgroupForm(projectSubgroup, project);
    }
}
