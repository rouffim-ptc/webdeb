/*
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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.*;

/**
 * The persistent class for the temporary contributor database table. Represents a user that will contributes into webdeb.
 * But their inscription is automated in a project context.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "tmp_contributor")
@Unqueryable
public class TmpContributor extends Model {

    private static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, TmpContributor> find = new Model.Finder<>(TmpContributor.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tmp_contributor", unique = true, nullable = false)
    private Long idContributor;

    @Column(name = "pseudo", nullable = false)
    private String pseudo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToOne(mappedBy = "tmpContributor", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Contributor contributor;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project")
    private Project project;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project_subgroup")
    private ProjectSubgroup subgroup;

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
     * Get the linked contributor if any
     *
     * @return the linked contributor, or null
     */
    public Contributor getContributor() {
        return contributor;
    }

    /**
     * Set the linked contributor
     *
     * @param contributor a contributor
     */
    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    /**
     * Get the linked contributor if any
     *
     * @return the linked contributor, or null
     */
    public Project getProject() {
        return project;
    }

    /**
     * Set the linked project
     *
     * @param project a project
     */
    public void setProject(Project project) {
        this.project = project;
    }
    /**
     * Get the linked project subgroup
     *
     * @return the linked project subgroup
     */
    public ProjectSubgroup getProjectSubgroup() {
        return subgroup;
    }

    /**
     * Set the linked project subgroup
     *
     * @param subgroup a project subgroup
     */
    public void setProjectSubgroup(ProjectSubgroup subgroup) {
        this.subgroup = subgroup;
    }

    @Override
    public String toString() {
        return new StringBuffer("contributor [").append(getIdContributor()).append("] ").append(" ")
                .append(", pseudo: ").append(getPseudo()).append(" ").toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a temporary contributor by its id
     *
     * @param id the temporary contributor id
     * @return the temporary contributor corresponding to that id, null otherwise
     */
    public static TmpContributor findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve a temporary contributor with a pseudonym
     *
     * @param pseudo pseudonym to search for
     * @return the ctemporary ontributor corresponding to given pseudonym, null otherwise
     */
    public static TmpContributor findByPseudo(String pseudo) {
        return find.where().eq("pseudo",pseudo).findUnique();
    }

    /**
     * Retrieve a temporary contributor by project id
     *
     * @param projectId a project id
     * @return a possibly empty list of projects
     */
    public static List<TmpContributor> findByProject(int projectId) {
        return find.where().eq("id_project", projectId).findList();
    }

    /**
     * Delete all tmp contributors from given project
     *
     * @param projectId a project id
     */
    public static void deleteContributorsFromProject(int projectId) {
        find.where().eq("id_project", projectId).delete();
    }

}
