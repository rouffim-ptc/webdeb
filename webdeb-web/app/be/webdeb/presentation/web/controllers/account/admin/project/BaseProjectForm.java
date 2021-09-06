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

import be.webdeb.core.api.project.BaseProject;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import javax.inject.Inject;

/**
* This class holds supertype wrapper for project, project group and subgroup.
*
* @author Martin Rouffiange
*/
public abstract class BaseProjectForm {

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);
    @Inject
    protected ProjectFactory factory = Play.current().injector().instanceOf(ProjectFactory.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    // used for lazy loading of project common fields
    protected Integer id;
    protected String name = "";
    protected String technicalName = "";

    /**
     * Play / JSON compliant constructor
     */
    public BaseProjectForm() {
    }

    /**
     * Constructor from a base project object
     *
     * @param project the super type of project
     */
    public BaseProjectForm(BaseProject project) {
        this.id = project.getId();
        this.name = project.getName();
        this.technicalName = project.getTechnicalName();
    }

    /**
     * Constructor from id, name and technical name
     *
     * @param id the project id
     * @param name the project name
     * @param technicalName the project technicalName
     */
    public BaseProjectForm(Integer id, String name, String technicalName) {
        this.id = id;
        this.name = name;
        this.technicalName = technicalName;
    }

    /*
     * GETTERS and SETTERS
     */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }
}

