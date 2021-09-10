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

package be.webdeb.presentation.web.controllers.entry.debate;

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributorPictureForm;
import be.webdeb.presentation.web.controllers.entry.ContributorPictureHolder;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.PlaceForm;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.data.validation.ValidationError;
import play.i18n.Lang;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of a Debate form
 *
 * @author Martin Rouffiange
 */
public class DebateForm extends DebateHolder {

  private boolean hasNoExistingPropositions = true;
  private ContributorPictureForm pictureForm;
  private String pictureString;
  private Long argumentJustificationId = null;

  /**
   * Play / JSON compliant constructor
   */
  public DebateForm() {
    super();
    this.pictureForm = new ContributorPictureForm();
  }

  /**
   * Create a new simple debate wrapper for an user lang
   *
   * @param lang the user language
   */
  public DebateForm(WebdebUser user, String lang) {
    super();
    this.lang = lang;
    this.pictureForm = new ContributorPictureForm();
  }

  /**
   * Create a new simple debate wrapper for a given argument justification link id and user lang
   *
   * @param link an argument justification link
   * @param lang     the user language
   */
  public DebateForm(ArgumentJustification link, WebdebUser user, String lang) {
    this((ArgumentShaded) link.getArgument(), user, lang);

    this.argumentJustificationId = link.getId();
  }

  /**
   * Create a new simple debate wrapper for a given shaded argument id and user lang
   *
   * @param argument a shaded argument for create the simple dabete
   * @param lang     the user language
   */
  public DebateForm(ArgumentShaded argument, WebdebUser user, String lang) {
    super();

    this.eDebateType = EDebateType.NORMAL;

    this.titleContributionId = argument.getId();
    this.argumentDictionaryId = argument.getDictionaryId();
    this.title = argument.getFullTitle();
    this.title = this.title.endsWith(".") || this.title.endsWith("?") || this.title.endsWith("!") ?
            this.title.substring(0, this.title.length() - 1) : this.title;

    this.title = values.firstLetterLower(this.title);

    this.language = argument.getLanguage().getCode();
    this.lang = lang;

    this.shade = argument.getArgumentShade().getEType().asDebateShade().id();

    this.pictureForm = new ContributorPictureForm();
  }

  /**
   * Create a new debate wrapper from a debate api and user lang
   *
   * @param debate the debate api
   * @param lang   the user language
   */
  public DebateForm(Debate debate, WebdebUser user, String lang) {
    // must be set manually, may not call super(debate, lang) constructor because their handling is too different
    id = debate.getId();
    type = EContributionType.DEBATE;
    this.user = user;
    this.contribution = debate;

    version = debate.getVersion();
    validated = debate.getValidated().getEType();
    groups = debate.getInGroups().stream().map(Group::getGroupId).collect(Collectors.toList());

    this.lang = lang;

    this.eDebateType = debate.getEType();

    this.description = debate.getDescription();
    this.isMultiple = debate.isMultiple();

    this.pictureForm = new ContributorPictureForm(debate.getPicture());
    this.externalUrls = debate.getExternalUrls().stream().map(DebateExternalUrlForm::new).collect(Collectors.toList());

    switch (eDebateType) {
      case NORMAL:
        DebateSimple debateSimple = (DebateSimple) debate;

        this.shade = debateSimple.getShade().getType();
        this.title = debateSimple.getTitle(lang);
        this.language = debateSimple.getArgument().getLanguage().getCode();
        break;
      case TAG_DEBATE:
      default:
        logger.debug("Can't edit a tag debate... ");
    }

    initActors();
    initPlaces(debate.getPlaces(), lang);
    initTags(debate.getTagsAsList(), lang);
  }

  /**
   * Form validation (implicit call from form submit)
   *
   * @return a map of ValidationError if any error in form was found, null otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();
    Map<String, List<ValidationError>> subErrors = new HashMap<>();

    switch(stepNum) {
      case -1 :
      case 0 :
          if(EDebateShade.value(shade) == null) {
            errors.put("shade", Collections.singletonList(new ValidationError("shade", "general.required")));
          }

          if(values.isBlank(title)) {
            errors.put("title", Collections.singletonList(new ValidationError("title", "general.required")));
          }

        if(stepNum == 0) return returnErrorsMap(errors);
      case 1 :
        if(stepNum == 1) return returnErrorsMap(errors);
      case 2 :
        // check tags
        if (tags == null || tags.isEmpty() || values.isBlank(tags.get(0).getName())) {
          String fieldName = "tags[0].name";
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "text.error.tag.name")));
        }else{
          subErrors = checkTags(tags, "tags");
          if(subErrors != null) errors.putAll(subErrors);
        }

        // check if there is not the same place
        if(places != null){
          subErrors = checkPlaces(places);
          if(subErrors != null) errors.putAll(subErrors);
        }

        if (citedactors != null && !citedactors.stream().allMatch(ActorSimpleForm::isEmpty)) {
          citedactors.stream().filter(a -> values.isBlank(a.getLang())).forEach(a -> a.setLang(lang));
          subErrors = validationListToMap(helper.checkActors(citedactors, "citedactors"));
          if(subErrors != null) errors.putAll(subErrors);
        }
        if(stepNum == 2) return returnErrorsMap(errors);
      case 3 :
        if (externalUrls != null) {
          for(DebateExternalUrlForm url : externalUrls) {
            String fieldName = "externalUrls[" + externalUrls.indexOf(url) + "].url";

            if(!url.isEmpty() && !values.isURL(url.getUrl())) {
              errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "nlp.text.invalidurl")));
            }
          }
        }

        if(description != null && description.length() > DESCRIPTION_MAX_LENGTH){
          errors.put("description", Collections.singletonList(new ValidationError("description", "nlp.text.invalidurl")));
        }
    }

    return returnErrorsMap(errors);
  }

  /**
   * Save a debate into the database. This id is updated if it was not set before.
   *
   * @param contributor the contributor id that ask to save this contribution
   * @return the map of Contribution type and a list of contribution (actors or folders) that have been created during
   * this insertion(for all unknown contributions), an empty list if none had been created
   * @throws FormatException      if this contribution has invalid field values (should be pre-checked before-hand)
   * @throws PermissionException  if given contributor may not perform this action or if such action would cause
   *                              integrity problems
   * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
   */
  public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
    logger.debug("try to save debate " + id + " " + toString() + " with version " + version + " in group " + inGroup);

    DebateSimple debate = debateFactory.getDebateSimple();
    debate.setId(id != null ? id : -1L);
    debate.setVersion(version);
    debate.addInGroup(inGroup);
    debate.setDescription(description);
    debate.setPictureExtension(pictureString);

    ArgumentShaded argument = argumentFactory.getArgumentShaded();

    argument.setId(titleContributionId != null ? titleContributionId : -1L);
    argument.addInGroup(inGroup);
    argument.setDictionaryId(argumentDictionaryId);

    debate.setArgumentId(argument.getId());
    // debate shade
    try {
      debate.setShade(debateFactory.getDebateShade(shade));
    } catch (FormatException e) {
      logger.error("unable to set debate shade " + shade);
      throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE, e);
    }

//    if(values.isBlank(id)) {
      debate.isMultiple(debate.getShade().getEType().isAlwaysMultiple() ||
              (debate.getShade().getEType().canBeMultiple() && isMultiple));
//    } else {
//      Debate debate1 = debateFactory.retrieve(id);
//
//      if(debate1 != null)
//        debate.isMultiple(debate1.isMultiple());
//    }

    EArgumentShade aShade = null;
    // argument type
    try {
      aShade = debate.getShade().getEType().asArgumentShade();
      argument.setArgumentShade(argumentFactory.getArgumentShade(aShade.id()));
    } catch (FormatException e) {
      logger.error("unable to set argument shade " + aShade);
      throw new PersistenceException(PersistenceException.Key.SAVE_ARGUMENT, e);
    }

    argument.setTitle(title);
    debate.setArgument(argument);


    // debate external url
    debate.initExternalUrls();
    for (DebateExternalUrlForm url : externalUrls.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList())) {
      debate.addExternalUrl(debateFactory.getDebateExternalUrl(url.getIdUrl(), url.getUrl(), url.getAlias()));
    }

    // tags
    debate.initTags();
    for (SimpleTagForm f : tags.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList())) {
      try {
        debate.addTag(helper.toTag(f, lang, ETagType.SIMPLE_TAG));
      } catch (FormatException e) {
        logger.error("unparsable tag " + f, e);
      }
    }

    // bound places
    debate.initPlaces();
    for (PlaceForm place : places) {
      if (!place.isEmpty()) {
        Place placeToAdd = createPlaceFromForm(place);
        if (placeToAdd != null)
          debate.addPlace(placeToAdd);
      }
    }

    // bound actors
    boundActors(debate);

    try {
      String language = values.detectTextLanguage(argument.getTitle(), lang);
      argument.setLanguage(debateFactory.getLanguage(language == null ? lang : language));
    } catch (FormatException e) {
      logger.error("unknown language code " + language, e);
    }


    debate.setId(id != null ? id : -1L);
    // save this debate
    Map<Integer, List<Contribution>> result = debate.save(contributor, inGroup);
    // update this id (in case of creation
    id = debate.getId();
    return result;
  }

  /*
   * SETTERS
   */

  public void setTitle(String title) {
    this.title = title;
  }

  public void setFullTitle(String fullTitle) {
    this.fullTitle = fullTitle;
  }

  public void setTags(List<SimpleTagForm> tags) {
    this.tags = tags;
  }

  public void setPlaces(List<PlaceForm> places) {
    this.places = places;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setIsMultiple(boolean multiple) {
    isMultiple = multiple;
  }

  public void setPicture(ContributorPictureHolder picture) {
    this.picture = picture;
  }

  public void setTitleContributionId(Long titleContributionId) {
    this.titleContributionId = titleContributionId;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setShade(Integer shade) {
    this.shade = shade != null ? shade : -1;
  }

  public void setArgumentDictionaryId(Long argumentDictionaryId) {
    this.argumentDictionaryId = argumentDictionaryId;
  }

  public ContributorPictureForm getPictureForm() {
    return pictureForm;
  }

  public void setPictureForm(ContributorPictureForm pictureForm) {
    this.pictureForm = pictureForm;
  }

  public String getPictureString() {
    return pictureString;
  }

  public void setPictureString(String pictureString) {
    this.pictureString = pictureString;
  }

  public boolean getHasNoExistingPropositions() {
    return hasNoExistingPropositions;
  }

  public void setHasNoExistingPropositions(boolean hasNoExistingPropositions) {
    this.hasNoExistingPropositions = hasNoExistingPropositions;
  }

  public void setExternalUrls(List<DebateExternalUrlForm> externalUrls) {
    this.externalUrls = externalUrls;
  }

  public Long getArgumentJustificationId() {
    return argumentJustificationId;
  }

  public void setArgumentJustificationId(Long argumentJustificationId) {
    this.argumentJustificationId = argumentJustificationId;
  }
}
