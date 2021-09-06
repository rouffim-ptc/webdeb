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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextType;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.presentation.web.controllers.SessionHelper;
import be.webdeb.presentation.web.controllers.entry.ContextContributionHolder;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import play.i18n.Lang;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of a Text (no IDs, but their description, as defined in the database)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class TextHolder extends ContextContributionHolder {

  static final int TITLE_MAX_LENGTH = 255;

  @Inject
  private SessionHelper sessionHelper = play.api.Play.current().injector().instanceOf(SessionHelper.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  // main fields (Note: as for all wrappers, all fields MUST hold empty values for proper form validation)
  protected boolean isHidden;
  protected String title;
  protected String language;

  protected List<TextTitleForm> titles = new ArrayList<>();

  // text properties
  protected String unknownPubDate = "false";
  protected String publicationDate;
  protected String textType;
  protected int textSourceType = -1;
  protected String textSourceTypeName;
  protected Boolean isOnInternet = null;
  protected String url;
  protected String textarea;
  protected ETextVisibility eTextVisibility;
  protected String textVisibility = "";

  // empty filename by default (needed for proper display)
  protected String filename;

  // lazy loading
  protected Text text;

  // part of collection-related fields
  protected String sourceTitle;

  // other bound actors
  protected List<ActorSimpleForm> citedActors = new ArrayList<>();

  // all actors involved in related excerpts (lazy loaded)
  protected List<ActorSimpleForm> citationActors;

  // citations (lazy loaded)
  protected List<CitationHolder> citations;
  protected int nbCitations = -1;

  protected Long linkId = -1L;

  /**
   * Play / JSON compliant constructor
   */
  public TextHolder() {
    super();
    type = EContributionType.TEXT;
  }

  /**
   * Constructor. Create a holder for a Text (i.e. no type/data IDs, but their descriptions, as defined in
   * the database). Text content is not loaded by default.
   *
   * @param text a Text
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TextHolder(Text text, WebdebUser user, String lang) {
    this(text, user, lang, false);
  }

  /**
   * Constructor. Create a holder for a Text (i.e. no type/data IDs, but their descriptions, as defined in
   * the database). Text content is not loaded by default.
   *
   * @param text a Text
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TextHolder(Text text, WebdebUser user, String lang, boolean light) {
    super(text, user, lang, light);

    this.text = text;
    isHidden = text.isHidden();
    title = text.getTitle(lang);

    if(!light) {
      this.titles = text.getTitles().entrySet().stream()
              .filter(t -> !t.getKey().equals(lang))
              .map(t -> new TextTitleForm(t.getKey(), t.getValue()))
              .collect(Collectors.toList());
    }

    publicationDate = text.getPublicationDate();

    url = text.getUrl();
    isOnInternet = url != null;
    sourceTitle = text.getSourceTitle();

    linkId = text.getLinkId();

    init(text);
  }

  /**
   * Initialize type-related fields with language-dependent values
   *
   * @param text a text to cast into this form object
   */
  protected void init(Text text) {
    language = text.getLanguage().getName(lang);

    if(text.getTextType() != null) {
      textType = text.getTextType().getName(lang);
    }

    textSourceType = text.getTextSourceType().getType();
    textSourceTypeName = text.getTextSourceType().getName(lang);

    eTextVisibility = text.getTextVisibility().getEType();
    textVisibility = text.getTextVisibility().getName(lang);
  }

  /*
   * CONVENIENCE PUBLIC METHODS
   */

  @Override
  public String getContributionDescription(){
    List<String> descriptions = new ArrayList<>();
    descriptions.add(publicationDate == null ? unknownPubDate : publicationDate);
    if(sourceTitle != null)
      descriptions.add(sourceTitle);
    descriptions.add(textType);
    descriptions.addAll(getAuthors(4).stream().map(ActorSimpleForm::getFullname).collect(Collectors.toList()));

    return String.join(", ", descriptions);
  }

  @Override
  public MediaSharedData getMediaSharedData(){
    if(mediaSharedData == null){
      mediaSharedData = new MediaSharedData(title, "text");
    }
    return mediaSharedData;
  }

  @Override
  public String getDefaultAvatar(){
    return "";
  }

  @Override
  public String toString() {
    StringBuilder authorString = new StringBuilder();
    for (ActorSimpleForm a : authors) {
      authorString.append(a.getFullname() + " ");
    }
    return "text [" + id + "] " + title + "; lang:" + language + "; authors: " + authorString
            + "; published: " + publicationDate + " with source: " + sourceTitle + " (" + url + ")";
  }

  /*
   * GETTERS
   */

  /**
   * Check if this text must be hidden from any visualization (ie it is an empty shelf only meant to gather
   * text-related properties for arguments)
   *
   * @return true if this text is not visible
   */
  public boolean isHidden() {
    return isHidden;
  }

  /**
   * Get the text title
   *
   * @return the text title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get the text language
   *
   * @return the language description in this.lang
   *
   * @see Language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Get stringified boolean flag to say if the publication date of this text is unknown
   *
   * @return "true" if the publication date is unknown
   */
  public String getUnknownPubDate() {
    return unknownPubDate;
  }

  /**
   * Get the publication date
   *
   * @return a string date of the form DD/MM/YYYY (D and M optional)
   */
  public String getPublicationDate() {
    return publicationDate;
  }

  /**
   * Get the type of text
   *
   * @return the description of the type of text in this.lang
   * @see TextType
   */
  public String getTextType() {
    return textType;
  }

  /**
   * Get the source type of text
   *
   * @return the source type id
   */
  public int getTextSourceType() {
    return textSourceType;
  }

  /**
   * Get the source type of text
   *
   * @return the description of the source type of text in this.lang
   */
  public String getTextSourceTypeName() {
    return textSourceTypeName;
  }

  /**
   * Get the flag to know if this text has been found on the internet (will require an url)
   *
   * @return true if this text has been found on the internet
   */
  public Boolean getIsOnInternet() {
    return isOnInternet;
  }

  /**
   * Get the url where this text comes from
   *
   * @return the url, or an empty string if !getIsOnInternet()
   */
  public String getUrl() {
    return url;
  }

  /**
   * Get the url where this text comes from
   *
   * @return the url, or an empty string if !getIsOnInternet()
   */
  public String getEncodedUrl() {
    try {
      return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Get the text visibility
   *
   * @return the visibility description in this.lang
   */
  public String getTextVisibility() {
    return textVisibility;
  }

  /**
   * Get the e text visibility
   *
   * @return the visibility enum
   */
  public ETextVisibility getETextVisibility() {
    return eTextVisibility;
  }

  /**
   * Get the title translations
   *
   * @return a (possibly empty) list of title translations
   */
  public List<TextTitleForm> getTitles() {
    return titles;
  }

  /**
   * Get the number of citations of the text
   *
   * @return the number of citations
   */
  public int getNbCitations(){
    if(nbCitations == -1){
      nbCitations = text.getNbCitations();
    }
    return nbCitations;
  }

  public Long getLinkId() {
    return linkId;
  }

  /**
   * Get the text content
   *
   * @return the content of the text
   */
  @JsonIgnore
  public String getTextarea() {
    if (textarea == null && user != null) {
      try {
        if (text != null && text.getFilename(user.getId()) != null && !text.getFilename(user.getId()).contains(".")) {
          textarea = text.getTextContent(user.getId());
          textarea = textarea != null ? textarea.replaceAll("\\r\\n|\\r|\\n", " <br>") : "";
        } else {
          // text area has not been set but we have no text to search for
          textarea = "";
        }
      } catch (PermissionException e) {
        logger.info("unable to get content of text " + id + " " + e.getMessage());
        textarea = i18n.get(Lang.forCode(lang), "text.content.notviewable");
      }
    }
    return textarea;
  }

  /**
   * Get the text content or empty string if nothing can be viewed
   *
   * @return the content of the text
   */
  @JsonIgnore
  public String getTextareaOrDefault() {
    String content = "";

    try {
      if (text != null && user != null && text.getFilename(user.getId()) != null && !text.getFilename(user.getId()).contains(".")) {
        content = text.getTextContent(user.getId());
      }
    } catch (PermissionException e) {
      content = "";
    }

    return content;
  }


  /**
   * Get the filename where the content is stored. If this filename has an extension (ie, contains a "."),
   * this means it is an external (binary) file that must be displayed as is, otherwise, name is of the form
   * "text_" + id ( possibly suffixed with "_" contributor id, if this text content is private)
   *
   * @return the filename where the content of this text is stored, may be empty if no content is accessible for current
   * contributor
   */
  @JsonIgnore
  public String getFilename() {
    try {
      return text != null && user != null && filename == null ? text.getFilename(user.getId()) : filename;
    } catch (PermissionException e) {
      //logger.info("current contributor " + contributor + " has no content or may not see " + id + " ", e);
      return "";
    }
  }

  /**
   * Get an citation of the text (size in char is externalized in a conf file and loaded by the SessionHelper)
   *
   * @return an citation of the text or a i18n message text is empty or no preview could be retrieved
   */
  @JsonIgnore
  public String getExcerpt(Long contributor, boolean large) {
    String excerpt;
    try {
      excerpt = text.getTextExcerpt(contributor, sessionHelper.getTextExcerptSize(large));
      if (excerpt.length() == 0) {
        if (text.getFilename(contributor).contains(".")) {
          excerpt = i18n.get(Lang.forCode(lang), "text.label.nopreview");
        } else {
          excerpt = i18n.get(Lang.forCode(lang), "text.label.nocontent");
        }
      } else {
        excerpt += " (...)";
      }
    } catch (PermissionException e) {
      logger.info("unable to get content of text " + id + " " + e.getMessage());
      excerpt = i18n.get(Lang.forCode(lang), "text.label.content.notviewable");
    }
    return excerpt;
  }

  /**
   * Get the source title, if any
   *
   * @return a source title, or an empty string
   */
  public String getSourceTitle() {
    return sourceTitle;
  }

  /**
   * Get the list of actors being referenced in this text's citations (lazy loaded)
   *
   * @return a (possibly empty) list of actors
   */
  @JsonIgnore
  public List<ActorSimpleForm> getCitationActors() {
    Set<Long> ids = new HashSet<>();
    if (text != null && citationActors == null) {
      citationActors = new ArrayList<>();
      ids.addAll(authors.stream().map(ActorSimpleForm::getId).collect(Collectors.toList()));
      ids.addAll(citedActors.stream().map(ActorSimpleForm::getId).collect(Collectors.toList()));

      // other actors involved in excerpts, but not already referenced
      text.getTextCitations().forEach(arg -> arg.getActors().forEach(r -> {
        if (!ids.contains(r.getActor().getId())) {
          ids.add(r.getActor().getId());
          citationActors.add(new ActorSimpleForm(r, lang));
        }
      }));
    }
    return citationActors;
  }

  /**
   * Get the list of citations extracted from this text (lazy loaded)
   *
   * @return a (possibly empty) list of citations
   */
  @JsonIgnore
  public List<? extends CitationHolder> getCitations() {
    if (text != null && citations == null) {
      citations = user.filterContributionList(text.getTextCitations())
              .stream()
              .map(e -> new CitationHolder((Citation) e, user, lang))
              .collect(Collectors.toList());
    }
    return citations;
  }
}
