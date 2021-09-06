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

package be.webdeb.infra.ws.external.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This simple class holds values about user that will be jsonified to be sent to external auth services
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest {

    /**
     * User email or pseudonym
     */
    @JsonSerialize
    private String emailOrPseudo;

    /**
     * User name
     */
    @JsonSerialize
    private String firstname;

    /**
     * User name
     */
    @JsonSerialize
    private String lastname;

    /**
     * User name
     */
    @JsonSerialize
    private String token;

    /**
     * Constructor.
     *
     * @param emailOrPseudo the user email or pseudonym
     * @param firstname the user firstname
     * @param lastname the user lastname
     * @param token the user auth token
     */
    public UserRequest(String emailOrPseudo, String firstname, String lastname, String token) {
        this.emailOrPseudo = emailOrPseudo;
        this.firstname = firstname;
        this.lastname = lastname;
        this.token = token;
    }

    public String getEmailOrPseudo() {
        return emailOrPseudo;
    }

    public void setEmailOrPseudo(String email) {
        this.emailOrPseudo = emailOrPseudo;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
