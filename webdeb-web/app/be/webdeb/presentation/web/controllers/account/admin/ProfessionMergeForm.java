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

import be.webdeb.core.api.actor.Profession;
import play.data.validation.ValidationError;

import java.util.*;

/**
 * Created by adminr on 09-10-17.
 */
public class ProfessionMergeForm extends AbtractProfessionForm {

    protected int professionId;
    protected ProfessionForm professionTomergewith;

    /**
     * Play / Json compliant constructor
     */
    public ProfessionMergeForm() {
        super();
    }

    /**
     * Initialize a profession edit form for given profession
     *
     * @param profession a profession api object for which names will be edit
     */
    public ProfessionMergeForm(Profession profession) {
        this();
        professionId = profession.getId();
        professionTomergewith = new ProfessionForm();
    }

    /**
     * Play form validation method
     *
     * @return a list of errors or null if none
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();
        if(!valuesHelper.isBlank(professionTomergewith.getName()) && professionTomergewith.getId() == -1){
            String fieldName = "professionTomergewith.name";
            String message = "admin.profession.error.notFound";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
        }
        else if(valuesHelper.isBlank(professionTomergewith.getName()) && professionTomergewith.getId() != -1){
            professionTomergewith.setId(-1);
        }
        return errors.isEmpty() ? null : errors;
    }

    public int getProfessionId() {
        return professionId;
    }

    public void setProfessionId(int professionId) {
        this.professionId = professionId;
    }

    public ProfessionForm getProfessionTomergewith() {
        return professionTomergewith;
    }

    public void setProfessionTomergewith(ProfessionForm professionTomergewith) {
        this.professionTomergewith = professionTomergewith;
    }
}
