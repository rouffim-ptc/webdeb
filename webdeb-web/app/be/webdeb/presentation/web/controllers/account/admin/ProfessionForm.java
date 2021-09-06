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

package be.webdeb.presentation.web.controllers.account.admin;


import be.webdeb.core.api.actor.EProfessionType;
import be.webdeb.core.api.actor.Profession;

/**
 * Simple form to only manage profession
 *
 * @author Martin Rouffiange
 */
public class ProfessionForm extends AbtractProfessionForm {

    private int id = -1;
    private int type = 0;
    private boolean displayHierarchy = true;
    private String name;

    /**
     * Play / Json compliant constructor
     */
    public ProfessionForm() {
        super();
    }

    /**
     * Constructor. Create an ProfessionForm from given profession
     *

     */
    public ProfessionForm(Profession profession, String lang) {
        this();
        this.id = profession.getId();
        this.type = profession.getType().id();
        this.name = profession.getName(lang);
        this.displayHierarchy = false;
    }

    /**
     * Constructor. Create an ProfessionForm with a given id and name
     *

     */
    public ProfessionForm(int id, int type, boolean displayHierarchy, String name) {
        this();
        this.id = id;
        this.type = type;
        this.displayHierarchy = displayHierarchy;
        this.name = name;
    }

    /**
     * Helper method to check whether the id and name are empty (contains empty or null values)
     *
     * @return true if all fields are empty
     */
    public boolean isEmpty() {
        return (name == null || "".equals(name));
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = (id != null ? id : -1);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisplayHierarchy() {
        return displayHierarchy;
    }

    public void setDisplayHierarchy(boolean displayHierarchy) {
        this.displayHierarchy = displayHierarchy;
    }
}
