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
import play.data.validation.ValidationError;

import java.util.*;

/**
 * Simple form class that contains a list of MailForms to invite a list of users
 *
 * @author Martin Rouffiange
 * @see Profession
 */
public class ProfessionHasLinkForm extends AbtractProfessionForm {

    private int professionId;
    private List<ProfessionForm> professionHasLink = new ArrayList<>();

    /**
     * Play / Json compliant constructor
     */
    public ProfessionHasLinkForm() {
        // needed by json/play
    }

    /**
     * Initialize a invitation form for given group
     *
     * @param profession a group api object for which invitations will be sent
     */
    public ProfessionHasLinkForm(Profession profession, String lang) {
        professionId = profession.getId();
        profession.getLinks().forEach((pid, pLink) -> professionHasLink.add(new ProfessionForm(pid, EProfessionType.OTHERS.id(), pLink.isDisplayHierarchy(), pLink.getName(lang))));
    }

    /**
     * Play form validation method
     *
     * @return a list of errors or null if none
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        for (int i = 0; i < professionHasLink.size(); i++) {
            ProfessionForm form = professionHasLink.get(i);
            String fieldName = "professionLink[" + i + "].name";
            if(!valuesHelper.isBlank(form.getName())){
                Profession pSearch = actorFactory.findProfession(form.getName(), true);
                if(pSearch == null) {
                    String message = "admin.profession.error.notFound";
                    errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
                }else if(pSearch.getId() != form.getId()){
                    form.setId(pSearch.getId());
                }

                if(professionId == form.getId()){
                    String message = "admin.profession.error.recursivelink";
                    errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
                }
            }else{
                professionHasLink.get(i).setId(-1);
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    public int getProfessionId() {
        return professionId;
    }

    public void setProfessionId(int professionId) {
        this.professionId = professionId;
    }

    public List<ProfessionForm> getProfessionHasLink() {
        return professionHasLink;
    }

    public void setProfessionHasLink(List<ProfessionForm> professionHasLink) {
        this.professionHasLink = professionHasLink;
    }
}
