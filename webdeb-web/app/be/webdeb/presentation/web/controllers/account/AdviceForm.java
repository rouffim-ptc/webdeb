/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.core.api.contributor.Advice;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AdviceForm{

    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    protected int id;
    protected String title;

    protected String frTitle;
    protected String enTitle;
    protected String nlTitle;

    /**
     * Play / JSON compliant constructor
     */
    public AdviceForm() { }

    public AdviceForm(Advice advice, String lang) {
        this.id = advice.getId();
        this.title = advice.getName(lang);

        this.frTitle = advice.getName("fr");
        this.enTitle = advice.getName("en");
        this.nlTitle = advice.getName("nl");
    }

    public boolean isEmpty(){
        return values.isBlank(frTitle) && values.isBlank(enTitle) && values.isBlank(nlTitle);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getFrTitle() {
        return frTitle;
    }

    public void setFrTitle(String frTitle) {
        this.frTitle = frTitle;
    }

    public String getEnTitle() {
        return enTitle;
    }

    public void setEnTitle(String enTitle) {
        this.enTitle = enTitle;
    }

    public String getNlTitle() {
        return nlTitle;
    }

    public void setNlTitle(String nlTitle) {
        this.nlTitle = nlTitle;
    }
}
