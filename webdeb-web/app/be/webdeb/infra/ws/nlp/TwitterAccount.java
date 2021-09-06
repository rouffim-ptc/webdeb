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

package be.webdeb.infra.ws.nlp;

import be.webdeb.core.api.contribution.Language;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;
import java.util.List;

/**
 * This class holds a twitter account as retrieved from WDTAL Twitter service to be managed by administrators.
 *
 * @author Fabian Gilson
 */
@JsonInclude(Include.NON_EMPTY)
public class TwitterAccount {

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("webdeb_id")
  private Long id;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("screen_name")
  private String account;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("full_name")
  private String fullname;

  @JsonSerialize
  @JsonDeserialize
  private String gender;

  @JsonSerialize
  @JsonDeserialize
  private String[] languages;

  /**
   * Default constructor
   */
  public TwitterAccount() {
    // needed by jackson
  }

  /**
   * Constructor. Used to create a new twitter account to listen too.
   *
   * @param id the webdeb actor id
   * @param account the account name (@Someone)
   * @param fullname the full name associated to this account
   * @param gender the gender id (in lowercase) of this twitter account
   * @param languages a (possibly empty) list of languages id
   *
   * @see be.webdeb.core.api.actor.Gender
   * @see Language
   */
  public TwitterAccount(Long id, String account, String fullname, String gender, List<String> languages) {
    this.id = id;
    this.account = account;
    this.fullname = fullname;
    this.gender = gender;
    this.languages = languages.toArray(new String[languages.size()]);
  }

  /**
   * Get the associated webdeb id to this twitter account
   *
   * @return an id
   */
  public Long getId() {
    return id;
  }

  /**
   * Get the twitter account id
   *
   * @return an id
   */
  public String getAccount() {
    return account;
  }

  /**
   * Get the full name associated to this twitter account
   *
   * @return a full name
   */
  public String getFullname() {
    return fullname;
  }

  /**
   * Get the gender id
   *
   * @return a gender id (may be null)
   * @see be.webdeb.core.api.actor.Gender
   */
  public String getGender() {
    if(gender.equals("femme")){
      return "f";
    }else if(gender.equals("homme")){
      return "m";
    }
    return gender;
  }

  /**
   * Get the language associated to this account
   *
   * @return an array the language iso-639-1 code (may be empty)
   * @see Language
   */
  public String[] getLanguages() {
    return languages != null ?
        Arrays.stream(languages).map(String::trim).toArray(String[]::new) : new String[0];
  }
}
