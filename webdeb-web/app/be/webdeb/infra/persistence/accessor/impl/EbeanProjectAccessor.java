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

package be.webdeb.infra.persistence.accessor.impl;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.persistence.accessor.api.APIObjectMapper;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;
import be.webdeb.infra.persistence.model.*;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import org.apache.commons.io.FileUtils;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This accessor handles project-related persistence actions.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanProjectAccessor implements ProjectAccessor {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected APIObjectMapper mapper;

    @Inject
    protected ProjectFactory factory;

    @Inject
    private FileSystem files;

    @Override
    public be.webdeb.core.api.project.Project retrieveProject(Integer id) {
        be.webdeb.infra.persistence.model.Project project = be.webdeb.infra.persistence.model.Project.findById(id);
        if (project != null) {
            try {
                return mapper.toProject(project);
            } catch (FormatException e) {
            logger.error("unable to cast retrieved project " + id, e);
        }
        } else {
            logger.warn("no project found for id " + id);
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.project.ProjectGroup retrieveGroup(Integer id) {
        be.webdeb.infra.persistence.model.ProjectGroup group = be.webdeb.infra.persistence.model.ProjectGroup.findById(id);
        if (group != null) {
            try {
                return mapper.toProjectGroup(group);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved project group " + id, e);
            }
        } else {
            logger.warn("no project group found for id " + id);
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.project.ProjectSubgroup retrieveSubgroup(Integer id) {
        be.webdeb.infra.persistence.model.ProjectSubgroup subgroup = be.webdeb.infra.persistence.model.ProjectSubgroup.findById(id);
        if (subgroup != null) {
            try {
                return mapper.toProjectSubgroup(subgroup);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved project subgroup " + id, e);
            }
        } else {
            logger.warn("no subgroup found for id " + id);
        }
        return null;
    }

    @Override
    public List<be.webdeb.core.api.project.Project> getAllProjects() {
        return buildListOfProjects(Project.getAllProjects());
    }

    @Override
    public List<be.webdeb.core.api.project.Project> findProjectByName(String name) {
        return buildListOfProjects(Project.findByTitle(name));
    }

    @Override
    public List<be.webdeb.core.api.project.ProjectGroup> findProjectGroupByName(String name) {
        return buildListOfGroups(ProjectGroup.findByTitle(name));
    }

    @Override
    public List<be.webdeb.core.api.project.ProjectSubgroup> findProjectSubgroupByName(String name) {
        return buildListOfSubgroups(ProjectSubgroup.findByTitle(name));
    }

    @Override
    public be.webdeb.core.api.project.Project findProjectByTechnicalName(String name) {
        Project project = Project.findByTechnicalName(name);
        if(project != null) {
            try {
                return mapper.toProject(project);
            } catch (FormatException exception) {
                logger.error("unable to cast project " + name + " Reason: " + exception.getMessage(), exception);
            }
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.project.ProjectGroup findProjectGroupByTechnicalName(String name, Integer projectId) {
        ProjectGroup projectGroup = ProjectGroup.findByTechnicalName(name, projectId);
        if(projectGroup != null) {
            try {
                return mapper.toProjectGroup(projectGroup);
            } catch (FormatException exception) {
                logger.error("unable to cast project group " + name + " Reason: " + exception.getMessage(), exception);
            }
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.project.ProjectSubgroup findProjectSubgroupByTechnicalName(String name, Integer projectId, Integer projectGroupId) {
        ProjectSubgroup projectSubgroup = ProjectSubgroup.findByTechnicalName(name, projectId, projectGroupId);
        if(projectSubgroup != null) {
            try {
                return mapper.toProjectSubgroup(projectSubgroup);
            } catch (FormatException exception) {
                logger.error("unable to cast project subgroup " + name + " Reason: " + exception.getMessage(), exception);
            }
        }
        return null;
    }

    @Override
    public void save(be.webdeb.core.api.project.Project project) throws PersistenceException {
        logger.debug("try to save project " + project.getName() + " with id " + project.getId());

        Project p = Project.findById(project.getId());

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            if (p == null) {
                // new project
                logger.debug("start creation of project " + project.getName());

                // create project object and binding
                p = updateProject(project, new be.webdeb.infra.persistence.model.Project());
                p.save();

                logger.info("saved " + p.toString());
            } else if(!p.isInProgress()) {
                // update existing
                logger.debug("update project " + project.getName() + " with id " + project.getId());

                // create project object and binding
                p = updateProject(project, p);
                p.update();

                logger.info("updated " + p.toString());
            }

            transaction.commit();
        } catch (Exception e) {
            logger.error("unable to save project " + project.getName(), e);
            throw new PersistenceException(PersistenceException.Key.SAVE_PROJECT, e);

        } finally {
            logger.debug("e");
            transaction.end();
        }
    }

    @Override
    public void save(be.webdeb.core.api.project.ProjectGroup group) throws PersistenceException {
        logger.debug("try to save project group " + group.getName() + " with id " + group.getId());

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

        ProjectGroup g = ProjectGroup.findById(group.getId());
        Project p = Project.findById(group.getProjectId());

        // check if given project exists, otherwise -> ObjectNotFound
        if (p == null) {
            logger.error("Project not found " + group.getProjectId());
            throw new ObjectNotFoundException(Project.class, group.getProjectId().longValue());
        }

        try {
            if (g == null) {
                // new project group
                logger.debug("start creation of project group " + group.getName());

                // create project group object and binding
                g = updateProjectGroup(group, new be.webdeb.infra.persistence.model.ProjectGroup(), p);
                g.save();

                g.update();

                logger.info("saved " + g.toString());
            } else if (!p.isInProgress()) {
                // update existing
                logger.debug("update project group " + group.getName() + " with id " + group.getId());

                // update project group object
                g = updateProjectGroup(group, g, p);
                g.update();

                g.update();

                logger.info("updated " + g.toString());
            }

            transaction.commit();
        } catch (Exception e) {
            logger.error("unable to save project group " + group.getName(), e);
            throw new PersistenceException(PersistenceException.Key.SAVE_PROJECT_GROUP, e);

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(be.webdeb.core.api.project.ProjectSubgroup subgroup) throws PersistenceException {
        logger.debug("try to save project subgroup " + subgroup.getName() + " with id " + subgroup.getId());

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

        ProjectSubgroup sub = ProjectSubgroup.findById(subgroup.getId());
        ProjectGroup group = ProjectGroup.findById(subgroup.getProjectGroupId());

        // check if given project group exists, otherwise -> ObjectNotFound
        if (group == null) {
            logger.error("Project group not found " + subgroup.getProjectGroupId());
            throw new ObjectNotFoundException(ProjectGroup.class, subgroup.getProjectGroupId().longValue());
        }
        // check if given project exists, otherwise -> ObjectNotFound
        if (group.getProject() == null) {
            logger.error("Project not found ");
            throw new ObjectNotFoundException(Project.class, null);
        }

        try {
            if (sub == null) {
                // new project subgroup
                logger.debug("start creation of project subgroup " + subgroup.getName());

                // create project subgroup object and binding
                sub = updateProjectSubgroup(subgroup, group, new be.webdeb.infra.persistence.model.ProjectSubgroup());
                sub.save();

                logger.info("saved " + sub.toString());
            } else if (!group.getProject().isInProgress()) {
                // update existing
                logger.debug("update project subgroup " + subgroup.getName() + " with id " + subgroup.getId());

                // create project subgroup object and binding
                sub = updateProjectSubgroup(subgroup, group, sub);
                sub.update();

                logger.info("updated " + sub.toString());
            }

            transaction.commit();
        } catch (Exception e) {
            logger.error("unable to save project subgroup " + subgroup.getName(), e);
            throw new PersistenceException(PersistenceException.Key.SAVE_PROJECT_SUBGROUP, e);

        } finally {
            transaction.end();
        }
    }

    @Override
    public List<be.webdeb.core.api.project.ProjectGroup> getGroupsLinkedToProject(int project) {
        Project p = Project.findById(project);
        if(p != null){
            return buildListOfGroups(p.getGroups());
        }
        return new ArrayList<>();
    }

    @Override
    public List<be.webdeb.core.api.project.ProjectSubgroup> getSubgroupsLinkedToGroup(int group) {
        ProjectGroup g = ProjectGroup.findById(group);
        if(g != null){
            return buildListOfSubgroups(g.getSubgroups());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Contributor> getContributorsLinkedToSubgroup(int subgroup) {
        ProjectSubgroup g = ProjectSubgroup.findById(subgroup);
        List<Contributor> result = new ArrayList<>();
        if(g != null){
            for (be.webdeb.infra.persistence.model.Contributor c : g.getContributors()) {
                try {
                    result.add(mapper.toContributor(c));
                } catch (FormatException e) {
                    logger.error("unparseable contributor" + c.getEmail(), e);
                }
            }
        }
        return result;
    }

    @Override
    public List<be.webdeb.core.api.contributor.Group> getContributorGroupsLinkedToSubgroup(int subgroup) {
        ProjectSubgroup g = ProjectSubgroup.findById(subgroup);
        List<be.webdeb.core.api.contributor.Group> result = new ArrayList<>();
        if(g != null){
            for (Group group : g.getContributorGroups()) {
                try {
                    result.add(mapper.toGroup(group));
                } catch (FormatException e) {
                    logger.error("unparseable contributor group " + g.getName(), e);
                }
            }
        }
        return result;
    }

    @Override
    public String generateProjectUser(int projectId) throws PersistenceException{
        Project project = Project.findById(projectId);

        final StringBuilder generated = new StringBuilder();

        if(project != null) {

            if(project.getGenerationDate() != null) {
                String previousContent = files.getProjectUsersFile(project.getTechnicalName());

                if(previousContent != null)
                    generated.append(previousContent);
            }

            Random random = new Random();
            final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";


            Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
            try {

                for (ProjectGroup group : project.getGroups()) {
                    if(project.getGenerationDate() == null || group.getVersion().after(project.getGenerationDate())
                       || group.getSubgroups().stream().anyMatch(sg -> sg.getVersion().after(project.getGenerationDate()))) {
                        generated.append("ECOLE : ")
                                .append(group.getName())
                                .append(" (")
                                .append(group.getTechnicalName())
                                .append(")\n");

                        for (ProjectSubgroup subGroup : group.getSubgroups()) {
                            if(project.getGenerationDate() == null || subGroup.getVersion().after(project.getGenerationDate())) {
                                generated.append("CLASSE : ")
                                        .append(subGroup.getName())
                                        .append(" (")
                                        .append(subGroup.getTechnicalName())
                                        .append(")\n");

                                // Generate password for this subgroup
                                String pwd = generateString(random, CHARACTERS, 8);
                                String pseudo = project.getTechnicalName() + "_" + group.getTechnicalName() + "_" + subGroup.getTechnicalName();
                                String pwd_hash = hashPassword(pwd);

                                generated.append("GROUP(S): ")
                                        .append(subGroup.getContributorGroups().stream().map(Group::getGroupName).collect(Collectors.joining(" ,")))
                                        .append("\n")
                                        .append("PASSWORD : ")
                                        .append(pwd)
                                        .append("\nUSERS:\n");

                                for (int iUser = 1; iUser <= subGroup.getNbContributors(); iUser++) {
                                    String realPseudo = pseudo + "_" + iUser;

                                    TmpContributor tmpContributor = new TmpContributor();
                                    tmpContributor.setIdContributor(0L);
                                    tmpContributor.setPseudo(realPseudo);
                                    tmpContributor.setPasswordHash(pwd_hash);
                                    tmpContributor.setProject(project);
                                    tmpContributor.setProjectSubgroup(subGroup);
                                    tmpContributor.save();

                                    generated.append(realPseudo).append("\n");
                                }
                                generated.append("\n");
                            }
                        }
                        generated.append("___________________________\n\n");
                    }
                }

                File file = File.createTempFile(project.getTechnicalName(), ".txt");
                FileUtils.writeStringToFile(file, generated.toString());
                files.saveProjectUsersFile(file, project.getTechnicalName());

                project.setGenerationDate(new Date());
                project.update();

                transaction.commit();

            } catch (Exception e) {
                logger.debug("Unable to save users project : ", e);
                throw new PersistenceException(PersistenceException.Key.SAVE_TMP_CONTRIBUTOR);
            } finally {
                transaction.end();
            }
        }

        return generated.toString();
    }

    @Override
    public Map<be.webdeb.core.api.contributor.Group, Map<Integer, Long>> getProjectContributionReportByContributorGroup(Integer projectId, Date fromDate, Date toDate) {
        Project project = Project.findById(projectId);
        Map<be.webdeb.core.api.contributor.Group, Map<Integer, Long>> response = new HashMap<>();

        if(project != null){
            project.getAllContributorGroups().forEach(g -> {
                try {
                    be.webdeb.core.api.contributor.Group group = mapper.toGroup(g);
                    response.put(group, g.getContributionsAmount(fromDate, toDate));
                }catch(FormatException e){
                    logger.debug("Unparsable group " + g);
                }
            });
        }

        return response;
    }

    @Override
    public Map<be.webdeb.core.api.project.ProjectSubgroup, Map<Integer, Long>> getProjectContributionReportByProjectSubgroup(Integer projectId, Date fromDate, Date toDate) {
        Project project = Project.findById(projectId);
        Map<be.webdeb.core.api.project.ProjectSubgroup, Map<Integer, Long>> response = new HashMap<>();

        if(project != null){
            project.getAllSubgroups().forEach(sg -> {
                try {
                    be.webdeb.core.api.project.ProjectSubgroup subgroup = mapper.toProjectSubgroup(sg);
                    response.put(subgroup, sg.getContributionsAmount(fromDate, toDate));
                }catch(FormatException e){
                    logger.debug("Unparsable project subgroup " + sg);
                }
            });
        }

        return response;
    }

    /**
     * Generate a random string.
     *
     * @param random the random number generator.
     * @param characters the characters for generating string.
     * @param length the length of the generated string.
     * @return the generated string
     */
    private String generateString(Random random, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    /**
     * // TODO doublon de la méthode ContributorAccessor
     * Create a hashed password from a clear text
     *
     * @param password a plain text
     * @return the hashed password
     * @throws PersistenceException if given password is null or empty
     */
    private String hashPassword(String password) throws PersistenceException {
        if (password == null || "".equals(password)) {
            throw new PersistenceException(PersistenceException.Key.UPDATE_CONTRIBUTOR);
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public void deleteProjectUser(int projectId) {
        Project project = Project.findById(projectId);
        if(project != null && hasTmpContributors(projectId) && project.getBeginDate().after(new Date())){
            TmpContributor.deleteContributorsFromProject(projectId);
            try {
                File file = File.createTempFile(project.getTechnicalName(), ".txt");
                FileUtils.writeStringToFile(file, "");
                files.saveProjectUsersFile(file, project.getTechnicalName());
            }catch(Exception e){
                logger.debug("Delete project error" + e);
            }
        }
    }

    @Override
    public boolean hasTmpContributors(int project) {
        List<TmpContributor> contributors = TmpContributor.findByProject(project);
        return contributors != null && !contributors.isEmpty();
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Helper method to build a list of API Project from DB project. All uncastable elements are ignored.
     *
     * @param projects a list of DB project
     * @return a list of API Project with elements that could have actually been casted to API element (may be empty)
     */
    private List<be.webdeb.core.api.project.Project> buildListOfProjects(List<Project> projects) {
        List<be.webdeb.core.api.project.Project> result = new ArrayList<>();
        for (Project e : projects) {
            try {
                result.add(mapper.toProject(e));
            } catch (FormatException exception) {
                logger.error("unable to cast project " + e.getIdProject() + " Reason: " + exception.getMessage(), exception);
            }
        }
        return result;
    }

    /**
     * Helper method to build a list of API ProjectGroup from DB project groups. All uncastable elements are ignored.
     *
     * @param groups a list of DB project groups
     * @return a list of API ProjectGroup with elements that could have actually been casted to API element (may be
     * empty)
     */
    private List<be.webdeb.core.api.project.ProjectGroup> buildListOfGroups(List<ProjectGroup> groups) {
        List<be.webdeb.core.api.project.ProjectGroup> result = new ArrayList<>();
        for (ProjectGroup e : groups) {
            try {
                result.add(mapper.toProjectGroup(e));
            } catch (FormatException exception) {
                logger.error("unable to cast project group " + e.getIdProjectGroup() + " Reason: " + exception.getMessage(), exception);
            }
        }
        return result;
    }

    /**
     * Helper method to build a list of API ProjectSubgroup from DB project subgroups groups. All uncastable elements are ignored.
     *
     * @param subgroups a list of DB project subgroups
     * @return a list of API ProjectSubgroup with elements that could have actually been casted to API element (may be
     * empty)
     */
    private List<be.webdeb.core.api.project.ProjectSubgroup> buildListOfSubgroups(List<ProjectSubgroup> subgroups) {
        List<be.webdeb.core.api.project.ProjectSubgroup> result = new ArrayList<>();
        for (ProjectSubgroup e : subgroups) {
            try {
                result.add(mapper.toProjectSubgroup(e));
            } catch (FormatException exception) {
                logger.error("unable to cast project group " + e.getIdProjectSubgroup() + " Reason: " + exception.getMessage(), exception);
            }
        }
        return result;
    }

    /**
     * Update a DB project with given API project
     *
     * @param apiProject an API project with data to store
     * @param project a DB project recipient (may contain data to be updated)
     * @return given project updated with given apiProject data
     */
    private Project updateProject(be.webdeb.core.api.project.Project apiProject, Project project){
        project.setName(apiProject.getName());
        project.setTechnicalName(apiProject.getTechnicalName());
        project.setBeginDate(apiProject.getBeginDate());
        project.setEndDate(apiProject.getEndDate());

        return project;
    }

    /**
     * Update a DB project group with given API project group
     *
     * @param apiProjectGroup an API project group with data to store
     * @param projectGroup a DB project group  recipient (may contain data to be updated)
     * @return given project group updated with given apiProjectGroup data
     */
    private ProjectGroup updateProjectGroup(be.webdeb.core.api.project.ProjectGroup apiProjectGroup, ProjectGroup projectGroup, Project project){
        projectGroup.setName(apiProjectGroup.getName());
        projectGroup.setTechnicalName(apiProjectGroup.getTechnicalName());
        projectGroup.setProject(project);

        return projectGroup;
    }

    /**
     * Update a DB project subgroup with given API project subgroup
     *
     * @param apiProjectSubgroup an API project subgroup with data to store
     * @param projectSubgroup a DB project subgroup recipient (may contain data to be updated)
     * @return given project subgroup updated with given apiProjectSubgroup data
     */
    private ProjectSubgroup updateProjectSubgroup(be.webdeb.core.api.project.ProjectSubgroup apiProjectSubgroup,
              ProjectGroup projectGroup, ProjectSubgroup projectSubgroup){
        projectSubgroup.setName(apiProjectSubgroup.getName());
        projectSubgroup.setTechnicalName(apiProjectSubgroup.getTechnicalName());
        projectSubgroup.setNbContributors(apiProjectSubgroup.getNbContributors());
        projectSubgroup.setProjectGroup(projectGroup);

        List<Group> dbGroups = new ArrayList<>();
        for(be.webdeb.core.api.contributor.Group group : apiProjectSubgroup.getContributorGroups()){
            Group dbGroup = Group.findById(group.getGroupId());
            if(dbGroup != null)
                dbGroups.add(dbGroup);
        }
        projectSubgroup.setGroups(dbGroups);

        return projectSubgroup;
    }

}
