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

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.ws.nlp.TwitterAccount;
import be.webdeb.util.ValuesHelper;
import play.data.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabian Gilson
 */
public class TwitterAccountForm {

  private TextFactory textFactory = play.api.Play.current().injector().instanceOf(TextFactory.class);
  private ActorFactory actorFactory = play.api.Play.current().injector().instanceOf(ActorFactory.class);
  private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private Long id = -1L;
  private String account;
  private String fullname;
  private String gender;
  private List<String> languages = new ArrayList<>();

  /**
   * Default play-compliant empty constructor
   */
  public TwitterAccountForm() {
    // needed by play / jackson
  }

  /**
   * Constructor, creates a form object from given twitter account to be displayed
   *
   * @param twitterAccount a twitter account
   * @param lang iso-639-1 language code
   */
  public TwitterAccountForm(TwitterAccount twitterAccount, String lang) {
    id = twitterAccount.getId();
    account = twitterAccount.getAccount();
    fullname = twitterAccount.getFullname();
    try {
      gender = actorFactory.getGender(twitterAccount.getGender().toUpperCase()).getName(lang);
    } catch (FormatException e) {
      logger.error("unable to cast gender " + twitterAccount.getGender(), e);
    }

    languages = Arrays.stream(twitterAccount.getLanguages()).map(l -> {
      try {
        return textFactory.getLanguage(l).getName(lang);
      } catch (FormatException e) {
        logger.error("unable to cast language " + l + " for " + account, e);
        return null;
      }
    }).collect(Collectors.toList());
    languages.removeIf(l -> l == null);
  }

  /**
   * Constructor, creates a form object from given twitter account to be modified
   *
   * @param twitterAccount a twitter account
   */
  public TwitterAccountForm(TwitterAccount twitterAccount) {
    id = twitterAccount.getId();
    account = twitterAccount.getAccount();
    fullname = twitterAccount.getFullname();
    gender = twitterAccount.getGender() != null ? twitterAccount.getGender().toUpperCase() : null;
    languages = Arrays.asList(twitterAccount.getLanguages());
  }

  /**
   * Validate the creation of a Twitter account (implicit call from form submission)
   *
   * @return null if validation ok, map of errors for each fields in error otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (values.isBlank(fullname) || values.isBlank(id)) {
      errors.put("fullname", Collections.singletonList(new ValidationError("fullname", "admin.twitter.error.fullname")));
    }

    if (values.isBlank(account)) {
      errors.put("account", Collections.singletonList(new ValidationError("account", "admin.twitter.error.account")));
    }

    if (values.isBlank(gender)) {
      errors.put("gender", Collections.singletonList(new ValidationError("gender", "admin.twitter.error.gender")));
    }

    // force remove of empty lines
    languages.removeIf(l -> l == null || "".equals(l));

    return errors.isEmpty() ? null : errors;
  }

  /*
   * GETTERS / SETTERS
   */

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public void setLanguages(List<String> languages) {
    this.languages = languages;
  }
}
