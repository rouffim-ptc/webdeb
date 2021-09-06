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

package be.webdeb.presentation.web.controllers.entry.actor;

import be.webdeb.core.api.actor.EProfessionType;
import be.webdeb.core.api.actor.Profession;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.presentation.web.controllers.account.admin.AbtractProfessionForm;
import be.webdeb.presentation.web.controllers.account.admin.ProfessionForm;
import play.data.validation.ValidationError;

import java.util.*;

/**
 * Simple form class that contains a list of profession names and a link within a profession like
 *
 * @author Martin Rouffiange
 * @see Profession
 * @see ProfessionNameForm
 */
public class ProfessionEditForm extends AbtractProfessionForm {

    protected int professionId;
    protected int professionTypeId;
    private String displayHierarchy = "true";

    protected int superProfessionId = -1;
    protected String superProfessionName;
    protected List<ProfessionNameForm> professionNames = new ArrayList<>();
    protected int userRole;
    // boolean to check if user has been warned about possibly wrong informations
    protected boolean userWarned = false;
    protected boolean userWarnedMsg = false;

    /**
     * Play / Json compliant constructor
     */
    public ProfessionEditForm() {
        super();
    }

    /**
     * Initialize a profession edit form for given profession
     *
     * @param profession a profession api object for which names will be edit
     */
    public ProfessionEditForm(Profession profession, String lang, int userRole) {
        this();
        professionId = profession.getId();
        professionTypeId = profession.getType().id();
        displayHierarchy = String.valueOf(profession.isDisplayHierarchy());
        this.userRole = userRole;
        Profession superProfession = profession.getSuperLink();
        if(superProfession != null) {
            superProfessionId = superProfession.getId();
            superProfessionName = superProfession.getName(lang);
        }

        profession.getNames().entrySet().forEach(e ->
                e.getValue().entrySet().forEach(f ->
                    professionNames.add(new ProfessionNameForm(f.getValue(), e.getKey(), f.getKey()))));
    }

    /**
     * Play form validation method
     *
     * @return a list of errors or null if none
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if(!valuesHelper.isBlank(superProfessionName)){
            Profession pSearch = actorFactory.findProfession(superProfessionName, true);
            String fieldName = "professionLink.name";
            if(pSearch == null) {
                String message = "admin.profession.error.notFound";
                errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
            }else if(pSearch.getId() != superProfessionId){
                superProfessionId = pSearch.getId();
            }

            if(professionId == superProfessionId){
                String message = "admin.profession.error.recursivelink";
                errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
            }
        }else{
            superProfessionId = -1;
        }

        if(valuesHelper.isBlank(professionTypeId)){
            errors.put("professionTypeId", Collections.singletonList(new ValidationError("professionTypeId", "")));
        }else if(EProfessionType.value(professionTypeId) == null){
            errors.put("professionTypeId", Collections.singletonList(new ValidationError("professionTypeId", "")));
        }

        if(!valuesHelper.isBoolean(displayHierarchy)){
            errors.put("displayHierarchy", Collections.singletonList(new ValidationError("displayHierarchy", "")));
        }

        for (int i = 0; i < professionNames.size(); i++) {
            ProfessionNameForm nameForm = professionNames.get(i);
            String fieldNames = "professionNames[" + i + "].";
            String fieldName = "";
            if (verifyLangForm(nameForm)) {
                // put first value as error (we know there is only one)
                String message = "admin.profession.error.lang";
                fieldName = fieldNames +"lang";
                errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
                if (!valuesHelper.isBlank(nameForm.getName()) && valuesHelper.isBlank(nameForm.getGender())) {
                    // put first value as error (we know there is only one)
                    fieldName = fieldNames + "gender";
                    message = "admin.profession.error.gender";
                    errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
                }
            }
            // check function name format
            if (userRole != EContributorRole.ADMIN.id() && !userWarned && !helper.verifyProfessionName(nameForm.getName(), nameForm.getLang())){
                fieldName = fieldNames+"name";
                errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "")));
                if(!userWarnedMsg){
                    fieldName = "profession_name";
                    String message = "profession.name.error";
                    errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
                    userWarnedMsg = true;
                }
            }
            // Search if there is multiple profession names in the same gender for the same language
            fieldName = fieldNames +"gender";
            if (professionNames.stream().filter(n -> !n.isEmpty()).anyMatch(n -> (n != nameForm && n.getLang().equals(nameForm.getLang()) && n.getGender().equals(nameForm.getGender())))) {
                errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "actor.error.gender.twice")));
            }
        }

        if(!userWarned)userWarned = userWarnedMsg;
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Verify if names or/and genders are specified but not the language
     *
     * @return true if names/genders specified and not the language
     */
    private Boolean verifyLangForm(ProfessionNameForm nameForm){
        return (valuesHelper.isBlank(nameForm.getLang()) && !valuesHelper.isBlank(nameForm.getName())
                && !valuesHelper.isBlank(nameForm.getGender()));
    }

    public int getProfessionId() {
        return professionId;
    }

    public void setProfessionId(int professionId) {
        this.professionId = professionId;
    }

    public int getProfessionTypeId() {
        return professionTypeId;
    }

    public void setProfessionTypeId(int professionTypeId) {
        this.professionTypeId = professionTypeId;
    }

    public int getSuperProfessionId() {
        return superProfessionId;
    }

    public void setSuperProfessionId(int superProfessionId) {
        this.superProfessionId = superProfessionId;
    }

    public String getSuperProfessionName() {
        return superProfessionName;
    }

    public void setSuperProfessionName(String superProfessionName) {
        this.superProfessionName = superProfessionName;
    }

    public String getDisplayHierarchy() {
        return displayHierarchy;
    }

    public void setDisplayHierarchy(String displayHierarchy) {
        this.displayHierarchy = displayHierarchy;
    }

    public List<ProfessionNameForm> getProfessionNames() {
        return professionNames;
    }

    public void setProfessionNames(List<ProfessionNameForm> professionNames) {
        this.professionNames = professionNames;
    }

    public boolean isUserWarned() {
        return userWarned;
    }

    public void setUserWarned(boolean userWarned) {
        this.userWarned = userWarned;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }
}
