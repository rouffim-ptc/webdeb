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

package be.webdeb.core.impl.project;

import be.webdeb.core.api.project.BaseProject;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.infra.persistence.accessor.api.ProjectAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBaseProject implements BaseProject {
    protected Integer id;
    protected String name;
    protected String technicalName;

    protected ProjectAccessor accessor;
    protected ProjectFactory factory;

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Abstract constructor
     *
     * @param factory a project factory
     * @param accessor a project accessor
     */
    public AbstractBaseProject(ProjectFactory factory, ProjectAccessor accessor) {
        this.accessor = accessor;
        this.factory = factory;
        id = -1;
    }

    @Override
    public Integer getId() {
        return id == null || id == -1 ? -1 : id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTechnicalName() {
        return technicalName == null || technicalName.equals("") ? id+"" : technicalName;
    }

    @Override
    public void setTechnicalName(String name) {
        this.technicalName = name;
    }

    @Override
    public int compareTo(@NotNull BaseProject o) {
        return name.compareToIgnoreCase(o.getName());
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();

        if (factory.getValuesHelper().isBlank(name)) {
            fieldsInError.add("project has no name");
        }

        if (factory.getValuesHelper().isBlank(technicalName)) {
            fieldsInError.add("project has no technical name");
        }

        return fieldsInError;
    }
}
