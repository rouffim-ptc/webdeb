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

package be.webdeb.core.impl.contributor;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.TmpContributor;
import be.webdeb.core.api.project.Project;
import be.webdeb.core.api.project.ProjectSubgroup;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

/**
 * This class implements a TmpContributor.
 *
 * @author Martin Rouffiange
 */
class ConcreteTmpContributor implements TmpContributor {

    private Long id = -1L;
    private String pseudo;
    private String password;
    private Project project;
    private ProjectSubgroup subgroup;
    private Contributor contributor = null;

    private ContributorAccessor accessor;

    /**
     * Default constructor.
     *
     * @param accessor the contributor accessor
     */
    ConcreteTmpContributor(ContributorAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getPseudo() {
        return pseudo;
    }

    @Override
    public void setPseudo(String pseudo) throws FormatException {
        if (pseudo == null || "".equals(pseudo.trim())) {
            throw new FormatException(FormatException.Key.CONTRIBUTOR_ERROR, "pseudo is invalid " + pseudo);
        }
        this.pseudo = pseudo.trim();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public ProjectSubgroup getProjectSubgroup() {
        return subgroup;
    }

    @Override
    public void setProjectSubgroup(ProjectSubgroup subgroup) {
        this.subgroup = subgroup;
    }

    @Override
    public Contributor getContributor() {
        if(contributor == null){
            contributor = accessor.retrieve(pseudo);
        }
        return contributor;
    }

    @Override
    public String toString() {
        return pseudo;
    }
}
