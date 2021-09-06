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

package be.webdeb.presentation.web.controllers.account.register;

import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.TmpContributor;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.account.ContributorHolder;
import be.webdeb.presentation.web.controllers.account.settings.PasswordForm;
import javax.inject.Inject;

import be.webdeb.presentation.web.controllers.entry.actor.AffiliationForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class wraps the Contributor fields to be used in forms
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @author Yvonnick Esnault (initial contribution)
 */
public class ContributorForm extends ContributorHolder {

  @Inject
  private ContributorFactory factory = Play.current().injector().instanceOf(ContributorFactory.class);

  private List<AffiliationForm> affiliationsForm = new ArrayList<>();

  // boolean flag to know if it is a signup or an edition of existing (for form field validation)
  private boolean signup;
  private boolean isTmp = false;
  // privately owned (form-related) fields
  private PasswordForm password;

  // use to indicate that the user accepted the user conditions
  private boolean acceptTerms = false;


  /**
   * Default constructor for signup requests.
   */
  public ContributorForm() {
    signup = true;
    newsletter = "true";
    pedagogic = "false";
    affiliationsForm = new ArrayList<>();
  }

  /**
   * Constructor for signup requests for a tpm user from project.
   */
  public ContributorForm(TmpContributor contributor) {
    signup = true;
    isTmp = true;
    pseudo = contributor.getPseudo();
    newsletter = "true";
    browserWarned = true;
    affiliationsForm = new ArrayList<>();
  }

  /**
   * Create a new contributor form with a given contributor
   *
   * @param contributor a contributor to load into form
   * @param signup true if this is a signup request, false if this form is used to update an existing contributor
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ContributorForm(Contributor contributor, boolean signup, WebdebUser user, String lang) {
    super(contributor, user, lang);
    this.signup = signup;
    init(contributor);
  }

  @Override
  protected void init(Contributor contributor) {
    gender = contributor.getGender() != null ? contributor.getGender().getCode() : null;
    residence = contributor.getResidence() != null ? contributor.getResidence().getCode() : null;
  }

  /**
   * Validate the subscription form (implicit call from form submission)
   *
   * @return a map of field name - error keys, or null if none
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (!values.isEmail(email)) {
      errors.put("email", Collections.singletonList(new ValidationError("email", "contributor.error.mail")));
    } else {
      // check uniqueness of email for new registrations
      if (values.isBlank(id) && factory.retrieveContributor(email) != null) {
        errors.put("email", Collections.singletonList(new ValidationError("email", "contributor.error.mail.exist")));
      }
    }

    if (values.isBlank(pseudo) || pseudo.length() < 3) {
      errors.put("pseudo", Collections.singletonList(new ValidationError("pseudo", "contributor.error.pseudo")));
    } else {
      // check uniqueness of pseudo for new registrations
      Contributor c = factory.retrieveContributor(pseudo);
      if ((c != null && (values.isBlank(id) || !c.getId().equals(id))) || (((pseudo.endsWith("_gauth") || pseudo.endsWith("_fauth")) && (c == null || !c.getId().equals(id))))) {
        errors.put("pseudo", Collections.singletonList(new ValidationError("pseudo", "contributor.error.pseudo.exist")));
      }
    }

    if (!values.isBlank(firstname) && firstname.length() < 2) {
      errors.put("firstname", Collections.singletonList(new ValidationError("firstname", "contributor.error.firstname")));
    }

    if (!values.isBlank(lastname) && lastname.length() < 2) {
      errors.put("lastname", Collections.singletonList(new ValidationError("lasttname", "contributor.error.lastname")));
    }

    // year of birth must be 4 char long and valid numeric
    if (!values.isBlank(birthYear) && !values.isNumeric(birthYear) && birthYear.length() == 4) {
      errors.put("birthYear", Collections.singletonList(new ValidationError("birthYear", "contributor.error.birthyear.format")));
    }

    // check affiliations
    Map<String, List<ValidationError>> tempErrors;
    affiliationsForm.removeIf(AffiliationForm::isEmpty);
    if ((tempErrors = checkAffiliations(getFullname(), birthYear, null, true, affiliationsForm, "affiliationsForm")) != null) {
      errors.putAll(tempErrors);
    }

    if (signup) {
      tempErrors = password.validate();
      if (tempErrors != null) {
        // remap errors key to actual key
        tempErrors.forEach((k, v) -> errors.put("password.password", v));
      }else if(isTmp){
        TmpContributor tmpContributor = factory.retrieveTmp(pseudo);
        if (tmpContributor != null && factory.checkPassword(password.getPassword(), tmpContributor.getPassword())){
            errors.put("password.password", Collections.singletonList(new ValidationError("password", "contributor.error.password.tmp.same")));
        }
      }

      // check that user accepted terms and conditions
      if (!acceptTerms) {
        errors.put("acceptTerms", Collections.singletonList(new ValidationError("acceptTerms", "contributor.error.terms")));
      }
    }

    return errors.isEmpty() ? null : errors;
  }


  /**
   * Store new Contributor into the database. For unknown affiliations, creates them (as actors, possible
   * matches have been checked at controller level)
   *
   * @return the newly created contributor
   */
  public Contributor save() throws PersistenceException {
    Contributor contributor = factory.getContributor();
    contributor.setId(id);
    contributor.setVersion(version);
    try {
      contributor.setEmail(email);
      contributor.setPseudo(pseudo);
    } catch (FormatException e) {
      logger.error("unparsable email : " + email + " or pseudo : " + pseudo);
      throw new PersistenceException(values.isBlank(id) ?
          PersistenceException.Key.SAVE_CONTRIBUTOR : PersistenceException.Key.UPDATE_CONTRIBUTOR, e);
    }
    contributor.setFirstname(firstname);
    contributor.setLastname(lastname);

    if (!values.isBlank(birthYear)) {
      try {
        contributor.setBirthyear(Integer.valueOf(birthYear));
      } catch (NumberFormatException | FormatException e) {
        logger.error("unparsable birth year given " + birthYear, e);
      }
    }

    if (!values.isBlank(gender)) {
      try {
        contributor.setGender(actorFactory.getGender(gender));
      } catch (FormatException e) {
        // should not happen here since the form has been validated
        logger.error("unknown gender id " + gender, e);
      }
    }
    // set the password only if it is a signup, otherwise let has is
    if (signup) {
      contributor.setPassword(password.getPassword());
    }

    if (!values.isBlank(residence)) {
      try {
        contributor.setResidence(actorFactory.getCountry(residence));
      } catch (FormatException e) {
        // should not happen here since the form has been validated
        logger.error("unknown country code " + residence, e);
      }
    }
    contributor.setAffiliations(new ArrayList<>());
    affiliationsForm.forEach(a -> {
      // lang may not be set, in case of unknown function / organization
      if (values.isBlank(a.getLang()) || "-1".equals(a.getLang())) {
        a.setLang(lang);
      }
      contributor.addAffiliation(a.toAffiliation());
    });
    contributor.setAvatarId(avatarId);
    contributor.isPedagogic(false);
    contributor.isNewsletter(Boolean.parseBoolean(newsletter));
    contributor.isBrowserWarned(browserWarned);

    if(isTmp){
      contributor.setTmpContributor(factory.retrieveTmp(pseudo));
    }

    // default group
    contributor.save(inGroup);
    logger.info("saved " + contributor.toString());
    return contributor;
  }

  /*
   * GETTERS / SETTERS
   */

  public boolean getIsTmp() {
    return isTmp;
  }

  public void setIsTmp(boolean tmp) {
    isTmp = tmp;
  }

  /**
   * Set the contributor's email
   *
   * @param email an email address
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Set the contributor's pseudonym
   *
   * @param pseudo a user pseudonym
   */
  public void setPseudo(String pseudo) {
    this.pseudo = pseudo;
  }

  /**
   * Set the contributor's first name
   *
   * @param firstname a name
   */
  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  /**
   * Set the contributor's last name
   *
   * @param lastname a name
   */
  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  /**
   * Set the contributor's country of residence
   *
   * @param residence a country id
   * @see Country
   */
  public void setResidence(String residence) {
    this.residence = residence;
  }

  /**
   * Set the contributor's gender id
   *
   * @param gender a gender id (F or M)
   * @see be.webdeb.core.api.actor.Gender
   */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /**
   * Set the contributor year of birth
   *
   * @param birthYear a 4-char year of the form YYYY
   */
  public void setBirthYear(String birthYear) {
    this.birthYear = birthYear;
  }

  /**
   * Get the list of affiliations form
   *
   * @return a possibly empty list of affiliations form
   */
  public List<AffiliationForm> getAffiliationsForm() {
    return affiliationsForm;
  }

  /**
   * Set the list of affiliations form
   *
   * @param affiliationsForm a list of affiliations form
   */
  public void setAffiliationsForm(List<AffiliationForm> affiliationsForm) {
    this.affiliationsForm = affiliationsForm;
  }

  /**
   * Get the password object
   *
   * @return a password form object
   */
  public PasswordForm getPassword() {
    return password;
  }

  /**
   * Set the password form object
   *
   * @param password a password form
   */
  public void setPassword(PasswordForm password) {
    this.password = password;
  }

  /**
   * Get the "accept terms and conditions" flag
   *
   * @return true if user accepted
   */
  public boolean getAcceptTerms() {
    return acceptTerms;
  }

  /**
   * Set the "accept terms and conditions" flag
   *
   * @param acceptTerms true if user accepted
   */
  public void setAcceptTerms(boolean acceptTerms) {
    this.acceptTerms = acceptTerms;
  }

  /**
   * Check if it is a signup request
   *
   * @return true if this is a form for a new signup
   */
  public boolean isSignup() {
    return signup;
  }

  /**
   * Set to true if this form is used for a new signup request
   *
   * @param signup true if it is a new signup, false if it is an update of an existing
   */
  public void setSignup(boolean signup) {
    this.signup = signup;
  }

  /**
   * Set whether this contributor is a teacher, student or researcher
   *
   * @param pedagogic true if this contributor is a teacher, student or researcher
   */
  public void setPedagogic(String pedagogic) {
    this.pedagogic = pedagogic;
  }

  /**
   * Set whether this contributor wants newsletters
   *
   * @param newsletter true if this contributor wants newsletters
   */
  public void setNewsletter(String newsletter) {
    this.newsletter = newsletter;
  }

  /**
   * Set the avatar file name
   *
   * @param avatar the avatar file name
   */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public void setAvatarId(Long avatarId) {
    this.avatarId = avatarId;
  }

}
