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

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class holds concrete values of an Argument Holder (no IDs, but their description, as defined in the
 * database). Except by using a constructor, no value can be edited outside of this package or by
 * subclassing.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ArgumentHolder extends ContributionHolder {

  static final int TITLE_MAX_LENGTH = 512;

  @Inject
  protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

  protected EArgumentType eargtype = EArgumentType.SIMPLE;
  protected int argtype = eargtype.id();

  // used for lazy loading of argument and text title
  protected Long dictionaryId = -1L;
  protected String language;
  protected String languageCode;
  protected String title = "";
  protected String fullTitle = "";

  // used for lazy loading of argument and text title
  protected Argument argument;
  private ContextContribution context;
  private ArgumentJustificationLinkForm justification = null;
  private List<ArgumentJustificationLinkForm> arguments = null;
  private List<CitationJustificationLinkForm> citations = null;

  private int nbCitationsLinks = -1;


  /*
   * argument shade attributes
   */
  // type, subtype, etc are initialized with -1 instead of empty values
  protected Integer shade = null;
  protected String shadeterm;

  /**
   * Play / JSON compliant constructor
   */
  public ArgumentHolder() {
    super();
    type = EContributionType.ARGUMENT;
  }

  /**
   * Construct a ReadOnlyArgument from a given argument with descriptions in the given language
   *
   * @param argument an Argument
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ArgumentHolder(Argument argument, WebdebUser user, String lang) {
    this(argument, user, lang, false);
  }

  /**
   * Construct a ReadOnlyArgument from a given argument with descriptions in the given language
   *
   * @param argument an Argument
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ArgumentHolder(Argument argument, WebdebUser user, String lang, boolean light) {
    super(argument, user, lang, light);

    this.argument = argument;

    dictionaryId = argument.getDictionaryId();

    title = argument.getDictionary().getTitle();
    fullTitle = argument.getFullTitle();

    language = argument.getDictionary().getLanguage().getName(lang);
    languageCode = argument.getDictionary().getLanguage().getCode();

    argtype = argument.getArgumentType().id();

    switch (argument.getArgumentType()) {
      case SHADED:
        ArgumentShaded shaded = (ArgumentShaded) argument;
        shade = shaded.getArgumentShade().getType();
        break;
    }
  }

  /**
   * Construct a ReadOnlyArgument from a given argument justification link with descriptions in the given language
   *
   * @param argument an Argument
   * @param context the context contribution of the link
   * @param link the argument justification link
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public ArgumentHolder(Argument argument, WebdebUser user, ContextContribution context, ArgumentJustificationLinkForm link, String lang) {
    this(argument, user, lang, true);

    this.context = context;
    this.justification = link;
  }

  @JsonIgnore
  public List<ArgumentJustificationLinkForm> getArguments() {
    if(arguments == null && context != null){
      arguments = justification != null && context != null ?
              fromApiToArgumentJustificationLinkForms(
                      context.getArgumentJustificationLinks(
                        justification.getCategoryId(),
                        id,
                        null),
                      context)
              : new ArrayList<>();
    }

    return arguments;
  }

  @JsonIgnore
  public List<CitationJustificationLinkForm> getCitations() {
    if(citations == null && context != null){
      citations = justification != null && context != null ?
              helper.toCitationJustificationLinkForms(
                      context.getCitationJustificationLinks(
                              justification.getCategoryId(),
                              id,
                              context.getType() == EContributionType.TEXT ? null : justification.getShadeId()),
                      user,
                      lang)
              : new ArrayList<>();
    }

    return citations;
  }

  @JsonIgnore
  public int getNbCitationsLinks() {
    if(nbCitationsLinks == -1 && context != null) {
      nbCitationsLinks = argumentFactory.getArgumentNbCitationsLink(
              context.getSuperContextId(),
              context.getSubContextId(),
              justification.getCategoryId(),
              id,
              context.getType() == EContributionType.TEXT ? null : justification.getShadeId(),
              user.getId(),
              user.getGroupId());
    }

    return nbCitationsLinks;
  }


  @Override
  public String toString() {
    return "argument [" + id + "] with title: " + title + " and lang " + language;
  }

  @Override
  public String getContributionDescription(){
    List<String> descriptions = new ArrayList<>();
    descriptions.add(title);

    return String.join(", ", descriptions);
  }

  @Override
  public MediaSharedData getMediaSharedData(){
    if(mediaSharedData == null){
      mediaSharedData = new MediaSharedData(title, "argument");
    }
    return mediaSharedData;
  }

  @Override
  public String getDefaultAvatar(){
    return "";
  }

  /*
   * GETTERS
   */

  /**
   * Get the argument type id (see ArgumentType interface)
   *
   * @return an argument type label (language-specific)
   */
  public int getArgtype() {
    return argtype;
  }

  /*
   * GETTERS
   */

  public Long getDictionaryId() {
    return dictionaryId;
  }

  /**
   * Get the argument title of this argument
   *
   * @return an argument title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get the argument full title (shade and title)
   *
   * @return an argument full title
   */
  public String getFullTitle() {
    return fullTitle;
  }

  /**
   * Get the argument language
   *
   * @return the language description in this.lang
   *
   * @see Language
   */
  public String getLanguage() {
    return language;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  /**
   * Get the argument shade type (see ArgumentShade interface)
   *
   * @return an argument shade label (language-specific)
   */
  public Integer getShade() {
    return shade == null ? -1 : shade;
  }

  /**
   * Get the argument shade term (language specific)
   *
   * @return the shade term
   */
  public String getShadeterm() {
    return shadeterm;
  }

  public ArgumentJustificationLinkForm getJustification() {
    return justification;
  }

  public boolean hasContent() {
    return !getArguments().isEmpty() || !getCitations().isEmpty();
  }
}
