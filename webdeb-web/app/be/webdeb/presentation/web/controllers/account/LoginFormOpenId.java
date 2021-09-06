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

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.core.api.contributor.EOpenIdType;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Simple form to handle login requests with open id
 *
 * @author Martin Rouffiange
 */
public class LoginFormOpenId {

    @Inject
    private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    private String id;
    private String token;
    private String email;
    private String pseudo;
    private int type;
    private EOpenIdType etype;

    /**
     * Validate the authentication form, ie, only checks if "valid" values are filled in (implicit call)
     *
     * @return null if validation succeeded, error message otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        this.etype = EOpenIdType.value(type);
    }

    public EOpenIdType getEtype() {
        return etype;
    }

    public void setEtype(EOpenIdType etype) {
        this.etype = etype;
    }

    @Override
    public String toString() {
        return "LoginFormOpenId{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", email='" + email + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", type=" + etype +
                '}';
    }
}
