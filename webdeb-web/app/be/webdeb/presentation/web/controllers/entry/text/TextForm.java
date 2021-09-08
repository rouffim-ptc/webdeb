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

package be.webdeb.presentation.web.controllers.entry.text;

import be.webdeb.core.api.actor.ExternalAuthor;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.text.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;

import javax.inject.Inject;

import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;
import play.i18n.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper class for forms used to encode a new text into the database.
 *
 * Note that all supertype getters corresponding to predefined values (ie, types) are sending
 * ids instead of language-dependent descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class TextForm extends TextHolder {

  @Inject
  private TextFactory textFactory = Play.current().injector().instanceOf(TextFactory.class);

  private String urlOrTitle;
  private String url2;

  // list of texts having the same title
  private boolean isNotSame = false;
  private List<TextHolder> candidates = new ArrayList<>();
  private Long externalTextId = -1L;

  // flag to add text without any content
  private boolean noContent;
  private boolean mayViewContent;

  /**
   * Play / JSON compliant constructor
   */
  public TextForm() {
    super();
    // needed for proper display
    filename = "";
  }

  /**
   * Create a new text wrapper for an user lang
   *
   * @param lang the user language
   */
  public TextForm(String lang) {
    super();
    this.lang = lang;
    filename = "";
  }


  /**
   * Constructor. Create a new form object for given external text.
   *
   * @param text an ExternalText
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TextForm(ExternalContribution text, String lang) {
    this();
    if(text != null) {
      isOnInternet = true;
      url = text.getSourceUrl();
      sourceTitle = values.getURLDomain(url);
      textSourceType = ETextSourceType.INTERNET.id();

      language = text.getLanguage().getCode();
      externalTextId = text.getId();

      if(sourceTitle.equals( i18n.get(Lang.forCode(lang), "text.external.fromTwitter.url"))){
        title = i18n.get(Lang.forCode(lang), "text.external.fromTwitter",
                !authors.isEmpty() ? !values.isFullBlank(authors.get(0).getFullname()) ? authors.get(0).getFullname() : "?"  : "?",
                !values.isFullBlank(publicationDate) ? publicationDate :"?");
      }else{
        title = text.getTitle();
      }
    }
  }

  /**
   * Constructor. Create a new form object for given text. Calls super then init beforehand.
   *
   * @param text a Text
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TextForm(Text text, WebdebUser user, String lang) {
    super(text, user, lang);

    unknownPubDate = Boolean.toString(publicationDate == null);

    urlOrTitle = values.isBlank(text.getUrl()) ? text.getTitle(lang) : text.getUrl();
    title = text.getTitle(lang);
    url = text.getUrl();
    embedCode = text.getEmbedCode();
    url2 = text.getUrl();

    try {
      filename = user != null ? text.getFilename(user.getId()) : "";
      filename = filename == null ? "" : filename;
      textarea = user != null ? text.getTextContent(user.getId()) : null;
      // noContent flag (used to disable content textarea) is true if we have no textual content and
      // text filename has no extension (ie dot) meaning it is not an external file
      noContent = "".equals(textarea) && !filename.contains(".");
      mayViewContent = true;
    } catch (PermissionException e) {
      logger.info("unable to get content of text " + text.getId() + " " + e.getMessage());
      textarea = "";
      filename = "";
      // force no content to be false, since we could not retrieve the content for that contributor
      // will be used to display a message in form
      noContent = false;
      mayViewContent = false;
    }

    initActors();
  }

  /**
   * Initialize type-related fields with integer values (called from super constructor)
   *
   * @param text a text to cast into this form object
   */
  @Override
  protected void init(Text text) {
    language = String.valueOf(text.getLanguage().getCode());
    textType = text.getTextType() != null ? String.valueOf(text.getTextType().getType()) : "";
    textVisibility = String.valueOf(text.getTextVisibility().getType());
    textSourceType = text.getTextSourceType().getType();
    textSourceTypeName = text.getTextSourceType().getName(lang);
  }

  /**
   * Validator (called from form submit)
   *
   * @return null if no error has been found, otherwise the list of found errors
   */
  public Map<String, List<ValidationError>> validate() {
    return validate("", true);
  }

  /**
   * Validator (called from form submit)
   *
   * @param prefix the prefix of the form to validate
   * @param fullCheck for checking every field
   * @return null if no error has been found, otherwise the list of found errors
   */
  public Map<String, List<ValidationError>> validate(String prefix, boolean fullCheck) {
    Map<String, List<ValidationError>> errors = new HashMap<>();
    Map<String, List<ValidationError>> subErrors = new HashMap<>();
    List<ValidationError> list;

    if (fullCheck && isOnInternet == null) {
      errors.put(prefix + "isOnInternet", Collections.singletonList(new ValidationError(prefix + "title", "text.error.isOnInternet")));
      return errors;
    }

    if (fullCheck && values.isBlank(title)) {
      errors.put(prefix + "title", Collections.singletonList(new ValidationError(prefix + "title", "text.error.title")));
    } else if(!values.isBlank(title) && title.length() > TITLE_MAX_LENGTH){
      if(fullCheck){
        errors.put(prefix + "title", Collections.singletonList(new ValidationError(prefix + "title", "text.error.length.title")));
      } else {
        title = "";
      }
    }

    for (int i = 0; i < titles.size(); i++) {
      TextTitleForm titleForm = titles.get(i);
      String fieldNames = prefix + "titles[" + i + "].";
      String fieldName = "";
      String message = "";
      if (!values.isBlank(titleForm.getLang()) || !values.isBlank(titleForm.getName())) {
        if (fullCheck && values.isBlank(titleForm.getLang())) {
          fieldName = fieldNames + "lang";
          message = "text.error.title.lang";
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
        }
        if (fullCheck && values.isBlank(titleForm.getName())) {
          fieldName = fieldNames + "name";
          message = "text.error.title.name";
          errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
        } else if(titleForm.getName().length() > TITLE_MAX_LENGTH){
          if(fullCheck){
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "text.error.length.title")));
          } else {
            titleForm.setName("");
          }
        }
      }
      // Search if there is multiple title in the same language
      if (fullCheck && titles.stream().filter(n -> !n.isEmpty()).anyMatch(n -> (n != titleForm && n.getLang().equals(titleForm.getLang())))) {
        errors.put(fieldNames + "lang", Collections.singletonList(new ValidationError("titles", "actor.error.lang.twice")));
      }
    }

    if(fullCheck) {
      if (authors == null || authors.stream().allMatch(ActorSimpleForm::isEmpty)) {
        errors.put(prefix + "authors[0]", Collections.singletonList(new ValidationError(prefix + "authors[0]", "text.error.author")));
      } else {
        // check if for all function, a name is given
        authors.stream().filter(a -> values.isBlank(a.getLang())).forEach(a -> a.setLang(lang));
        if ((list = helper.checkActors(authors, prefix + "authors")) != null) {
          list.forEach(e -> errors.put(e.key(), Collections.singletonList(new ValidationError(e.key(), e.message()))));
        }
      }
    }

    if (fullCheck && (values.isBlank(unknownPubDate) || "false".equals(unknownPubDate)) && !values.isBlank(publicationDate) && !Boolean.parseBoolean(unknownPubDate) && !values.isDate(publicationDate)) {
      errors.put(prefix + "publicationDate", Collections.singletonList(new ValidationError(prefix + "publicationDate", "text.error.publicationDate.format")));
    }

    if (fullCheck && isOnInternet) {
      url = values.transformURL(!values.isBlank(url2) ? url2 : url);
      list = helper.checkUrl(url, prefix + "url");
      if (!list.isEmpty()) {
        errors.put(prefix + "url", list);
      } else {
        Text existing = textFactory.findByUrl(url);
        if(existing != null && !existing.getId().equals(id)) {
          errors.put(prefix + "url", Collections.singletonList(new ValidationError(prefix + "url", "text.error.url.exiting")));
        }
      }
    }

    /*if (mayViewContent && !noContent && externalTextId == null || externalTextId == -1L) {
      if (!String.valueOf(ETextVisibility.PRIVATE.id()).equals(textVisibility)) {
        // must pass a content
        if (values.isBlank(textarea) && values.isBlank(filename)) {
          errors.put("textarea", Collections.singletonList(new ValidationError("textarea", "text.error.area")));
        } else {
          excerptsStillPresent(errors);
        }
      } else {
        // must pass a content
        if (values.isBlank(textarea) && values.isBlank(filename)) {
          errors.put("textarea", Collections.singletonList(new ValidationError("textarea", "text.error.area")));
        }
      }
    }*/
    return errors.isEmpty() ? null : errors;
  }

  /**
   * Save a text into the database. This id is updated if it was not set before.
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
    logger.debug("try to save " + toString() + " with version " + version + " in group " + inGroup);

    //boolean isFreeSource = (!values.isBlank(url) && helper.isFreeSource(url, contributor)) || (values.isURL(urlOrTitle) && helper.isFreeSource(urlOrTitle, contributor));
    inGroup = values.isBlank(inGroup) ? 0 : inGroup;

    if(values.isBlank(language)) {
      String language = values.detectTextLanguage(title, lang);
      this.language = language == null ? lang : language;
    }

    if(!values.isBlank(url)) {
      isOnInternet = true;
      textSourceType = url.endsWith(".pdf") ? ETextSourceType.PDF.id() : ETextSourceType.INTERNET.id();
    } else if (filename.contains(".") || !values.isBlank(urlOrTitle)) {
      isOnInternet = values.isURL(urlOrTitle);
      textSourceType = ETextSourceType.PDF.id();
      embedCode = null;
    } else {
      isOnInternet = false;
      textSourceType = ETextSourceType.OTHER.id();
    }

    Text text = textFactory.getText();
    text.setId(id != null ? id : -1L);
    text.setVersion(version);
    text.addInGroup(inGroup);
    text.addTitle(language, values.isBlank(title) ? values.isURL(urlOrTitle) ? null : urlOrTitle : title);
    text.setSourceTitle(sourceTitle);
    text.setExternalTextId(externalTextId);
    titles.removeIf(e -> e.getLang() == null || e.getLang().equals(language));
    titles.forEach(t -> text.addTitle(t.getLang(), t.getName()));

    if(!isOnInternet) {
      url = null;
      embedCode = null;
      urlOrTitle = null;
    }

    if (!Boolean.parseBoolean(unknownPubDate)) {
      try {
        text.setPublicationDate(publicationDate);
      } catch (FormatException e) {
        publicationDate = null;
      }
    } else {
      publicationDate = null;
    }

    Integer type = null;
    try {
      type = Integer.parseInt(textType.trim());
      text.setTextType(textFactory.getTextType(type));
    } catch (Exception e) {
      logger.error("unparsable number " + textType);
    }

    text.setTextSourceType(textFactory.getTextSourceType(textSourceType));

    if (!values.isBlank(url) || values.isURL(urlOrTitle)) {
      try {
        text.setUrl(values.isBlank(url) ? urlOrTitle.trim() : url.trim());
        text.setEmbedCode(embedCode);
      } catch (FormatException e) {
        logger.error("unparsable url " + url, e);
      }
    }

    try {
      String language = values.detectTextLanguage(values.isBlank(textarea) ? title: textarea, lang);
      text.setLanguage(textFactory.getLanguage(language == null ? lang : language));
    } catch (FormatException e) {
      logger.error("unknown language code " + language, e);
    }

    Integer parsedTextVisibility;

    try {
      parsedTextVisibility = Integer.parseInt(textVisibility);
    } catch (NumberFormatException e) {
      logger.warn("Error when parsing text visibility " + textVisibility + ", put public by default", e);
      parsedTextVisibility = ETextVisibility.PUBLIC.id();
    }

    try {
      ETextVisibility parsedTextVisibilityEnum = ETextVisibility.value(parsedTextVisibility);

      text.setTextVisibility(textFactory.getTextVisibility(user.isPublicAdmin() && parsedTextVisibilityEnum != null ?
              parsedTextVisibilityEnum.id() : ETextVisibility.PUBLIC.id()));
    } catch (NumberFormatException | FormatException e) {
      logger.error("unknown visibility id " + textVisibility + ", put public by default", e);
    }

    // bound actors
    boundActors(text);

    // force empty content if no content (-> will create an empty file)
    //text.setTextContent(textarea == null || !isFreeSource ? "" : textarea);
    text.setTextContent(textarea != null ? textarea : "");
    // if we have an external file, set this filename
    if (filename.contains(".")) {
      text.addFilename(contributor, filename);
    }

    // set hidden state (for "empty-shelf" texts)
    text.isHidden(isHidden);
    Map<Integer, List<Contribution>> result = text.save(contributor, inGroup);

    // do not forget to update the id, since the controller needs it for redirection
    id = text.getId();
    return result;
  }

  /*
   * SETTERS
   */

  /**
   * Set the text title
   *
   * @param title a title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Set the text language id
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

  /**
   * Set the list of actors being the authors of this text
   *
   * @param authors a list of actors
   */
  public void setAuthors(List<ActorSimpleForm> authors) {
    this.authors = authors;
  }

  /**
   * Stringified boolean flag to say if the publication date of this text is unknown
   *
   * @param unknownPubDate "true" if the publication date is unknown
   */
  public void setUnknownPubDate(String unknownPubDate) {
    this.unknownPubDate = unknownPubDate;
  }

  /**
   * Set the publication date
   *
   * @param publicationDate a string date of the form DD/MM/YYYY (D and M optional)
   */
  public void setPublicationDate(String publicationDate) {
    this.publicationDate = publicationDate;
  }

  /**
   * Set the type of text id
   *
   * @param textType a type id
   * @see TextType
   */
  public void setTextType(String textType) {
    this.textType = textType;
  }

  /**
   * Set the source type of text id
   *
   * @param textSourceType a type id
   */
  public void setTextSourceType(int textSourceType) {
    this.textSourceType = textSourceType;
  }

  /**
   * Set the flag to know if this text has been found on the internet (will require an url)
   *
   * @param isOnInternet true if this text has been found on the internet
   */
  public void setIsOnInternet(Boolean isOnInternet) {
    this.isOnInternet = isOnInternet;
  }

  /**
   * Set the url where this text comes from
   *
   * @param url an url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Set the embed code for display web content from plaform.
   *
   * @param embedCode the embed code (like iframe)
   */
  public void setEmbedCode(String embedCode) {
    this.embedCode = embedCode;
  }

  /**
   * Set whether this text must be hidden from any visualization (ie it is an empty shelf only meant to gather
   * text-related properties for arguments)
   *
   * @param hidden true if this text must be hidden
   */
  public void setIsHidden(boolean hidden) {
    isHidden = hidden;
  }

  /**
   * Set the visibility id for this text
   *
   * @param visibility a visibility id
   */
  public void setTextVisibility(String visibility) {
    this.textVisibility = visibility;
  }

  /**
   * Set the filename where this text is stored (will be used for file upload)
   *
   * @param filename a filename
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * Set the text content
   *
   * @param textarea a text content
   */
  public void setTextarea(String textarea) {
    this.textarea = textarea;
  }

  /**
   * Set the source title, if any
   *
   * @param sourceTitle a source
   */
  public void setSourceTitle(String sourceTitle) {
    this.sourceTitle = sourceTitle;
  }

  /**
   * Check whether this text has been explicitly flagged as a new one (used when other texts exists with
   * same title)
   *
   * @return true if this text is a new one despite it has the same title as another one in db
   */
  public boolean getIsNotSame() {
    return isNotSame;
  }

  /**
   * Set whether this text has been explicitly flagged as a new one (used when other texts exists with
   * same title)
   *
   * @param notSame true if this text is a new one despite it has the same title as another one in db
   */
  public void setIsNotSame(boolean notSame) {
    isNotSame = notSame;
  }

  /**
   * Get the list of candidate texts having the same title as this text
   *
   * @return a (possibly empty) list of text holders
   */
  public List<TextHolder> getCandidates() {
    return candidates;
  }

  /**
   * Set the list of candidate texts having the same title as this text
   *
   * @param candidates a list of api Texts
   */
  public void setCandidates(List<Text> candidates) {
    // may not use this.lang since it may not have been set
    this.candidates = candidates.stream().map(t -> new TextHolder(t, user, lang)).collect(Collectors.toList());
  }

  /**
   * Check whether this text has some content
   *
   * @return true if this text has no content
   */
  public boolean getNoContent() {
    return noContent;
  }

  /**
   * Set whether this text has no content
   *
   * @param noContent true if this text has no content
   */
  public void setNoContent(boolean noContent) {
    this.noContent = noContent;
  }

  /**
   * Check whether current contributor may view the content
   *
   * @return true if this user may view the content (used to validate content at save time)
   */
  public boolean getMayViewContent() {
    return mayViewContent;
  }

  /**
   * Set whether current contributor may view the content
   *
   * @param mayViewContent true if this user may view the content
   */
  public void setMayViewContent(boolean mayViewContent) {
    this.mayViewContent = mayViewContent;
  }


  /**
   * Get the external text id that will be converted as a webdeb text, if any
   *
   * @return an external text id, if any
   */
  public Long getExternalTextId() {
    return externalTextId;
  }

  /**
   * Set the external text id that will be converted as a webdeb text
   *
   * @param externalTextId an externalText id
   */
  public void setExternalTextId(Long externalTextId) {
    this.externalTextId = externalTextId;
  }

  public String getUrlOrTitle() {
    return urlOrTitle;
  }

  public void setUrlOrTitle(String urlOrTitle) {
    this.urlOrTitle = urlOrTitle;
  }

  public String getUrl2() {
    return url2;
  }

  public void setUrl2(String url2) {
    this.url2 = url2;
  }
}
