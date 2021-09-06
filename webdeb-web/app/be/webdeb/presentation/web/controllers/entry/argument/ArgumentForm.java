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

package be.webdeb.presentation.web.controllers.entry.argument;

import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import play.data.validation.ValidationError;
import play.i18n.Lang;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper class for forms used to encode a new argument into the database.
 *
 * Note that all supertype getters corresponding to predefined values (ie, types) are sending
 * ids instead of language-dependent descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ArgumentForm extends ArgumentHolder {

  private Long citationJustificationLinkId = null;

  /**
   * Play / JSON compliant constructor
   */
  public ArgumentForm() {
    super();
  }

  public ArgumentForm(EArgumentType type, String lang) {
    super();

    argtype = type.id();
    this.lang = lang;
  }

  /**
   * Create an argument form from a given citation justification link
   *
   * @param link citation justification link
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ArgumentForm(CitationJustification link, EArgumentType type, String lang) {
    this(type, lang);

    this.citationJustificationLinkId = link.getId();
  }

  /**
   * Create an argument form from a given argument
   *
   * @param argument an argument
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ArgumentForm(Argument argument, String lang) {
    // must be set manually, may not call super(argument, lang) constructor because their handling is too different
    id = argument.getId();
    dictionaryId = argument.getDictionaryId();
    type = EContributionType.ARGUMENT;

    version = argument.getVersion();
    validated = argument.getValidated().getEType();
    groups = argument.getInGroups().stream().map(Group::getGroupId).collect(Collectors.toList());

    this.argument = argument;
    this.lang = lang;
    argtype = argument.getArgumentType().id();

    title = argument.getTitle();
    fullTitle = argument.getFullTitle();
    language = argument.getLanguage().getName(lang);
    languageCode = argument.getLanguage().getCode();

    switch (argument.getArgumentType()) {
      case SHADED:
        ArgumentShaded shaded = (ArgumentShaded) argument;
        shade = shaded.getArgumentShade().getType();
        break;
    }
  }

  /**
   * Form validation (implicit call from form submit)
   *
   * @return a map of ValidationError if any error in form was found, null otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (values.isBlank(title)) {
      errors.put("title", Collections.singletonList(new ValidationError("title", "argument.error.title.blank")));
    }else if(title.length() > TITLE_MAX_LENGTH){
      errors.put("title", Collections.singletonList(new ValidationError("title", "argument.error.title.size")));
    }

    // in the case of nlp service problem, set the text language as the user language
    if (values.isBlank(languageCode)) {
      languageCode = lang;
    }

    switch (EArgumentType.value(argtype)) {
      case SHADED:
        if (shade == null) {
          errors.put("shade", Collections.singletonList(new ValidationError("shade", "argument.error.type")));
        }
        break;
    }

    // must return null if errors is empty
    return errors.isEmpty() ? null : errors;
  }

  /**
   * Save an argument into the database. This id is updated if it was not set before.
   *
   * @param contributor the contributor id that ask to save this contribution
   * @return the map of Contribution type and a list of contribution (actors or folders) that have been created during
   * this insertion(for all unknown contributions), an empty list if none had been created
   *
   * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
   * @throws PermissionException if given contributor may not perform this action or if such action would cause
   * integrity problems
   * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
   */
  public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
    logger.debug("try to save argument " + id + " " + toString() + " with version " + version + " in group " + inGroup);

    inGroup = values.isBlank(inGroup) ? 0 : inGroup;

    Argument argument = EArgumentType.value(argtype) ==
            EArgumentType.SHADED ? argumentFactory.getArgumentShaded() : argumentFactory.getArgument();
    argument.setId(id != null ? id : -1L);
    argument.setVersion(version);
    argument.addInGroup(inGroup);

    // remove ending "." if any
    title =  title.trim();
    if (title.endsWith(".")) {
      title = title.substring(0,  title.lastIndexOf('.'));
    }
    argument.setTitle(title);

    // argument language
    try {
      String language = values.detectTextLanguage(title, lang);
      argument.setLanguage(argumentFactory.getLanguage(language == null ? lang : language));
    } catch (FormatException e) {
      logger.error("unknown language code " + languageCode, e);
    }
    argument.setDictionaryId(dictionaryId);

    switch (argument.getArgumentType()) {
      case SHADED:
        ArgumentShaded shaded = (ArgumentShaded) argument;
        // argument type
        try {
          shaded.setArgumentShade(argumentFactory.getArgumentShade(shade));
        } catch (FormatException e) {
          logger.error("unable to set argument shade " + shade);
          throw new PersistenceException(PersistenceException.Key.SAVE_ARGUMENT, e);
        }
        break;
    }

    // save this argument
    Map<Integer, List<Contribution>> result = argument.save(contributor, inGroup);
    // update this id (in case of creation)
    id = argument.getId();
    return result;
  }

  /*
   * GETTERS / SETTERS
   */

  /*
   * other supertype fields
   */

  /**
   * Set the title of this argument
   *
   * @param title a argument title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Set the argument type id (see ArgumentType interface)
   *
   * @param argtype an argument type id
   */
  public void setArgtype(int argtype) {
    this.argtype = argtype;
  }

  /**
   * Set the argument language id
   *
   * @param language a language id
   * @see Language
   */
  public void setLanguage(String language) {
    if(lang == null){
      lang = language;
    }
    this.language = language;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public void setDictionaryId(Long argumentDictionaryId) {
    this.dictionaryId = argumentDictionaryId;
  }

  /**
   * Get the argument shade type (see ArgumentShade interface)
   *
   * @return an argument shade label (language-specific)
   */
  @Override
  public Integer getShade() {
    return shade;
  }

  public void setShade(Integer shade) {
    this.shade = shade;
  }

  public Long getCitationJustificationLinkId() {
    return citationJustificationLinkId;
  }

  public void setCitationJustificationLinkId(Long citationJustificationLinkId) {
    this.citationJustificationLinkId = citationJustificationLinkId;
  }
}
