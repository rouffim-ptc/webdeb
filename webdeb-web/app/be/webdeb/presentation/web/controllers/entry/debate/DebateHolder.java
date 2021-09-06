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

import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.debate.*;
import be.webdeb.presentation.web.controllers.entry.ContextContributionHolder;
import be.webdeb.presentation.web.controllers.entry.ContributorPictureHolder;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.debate.EDebateVizPane;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of a Debate holder
 *
 * @author Martin Rouffiange
 */
public class DebateHolder extends ContextContributionHolder {

  static final int DESCRIPTION_MAX_LENGTH = 1500;

  @Inject
  protected DebateFactory debateFactory = Play.current().injector().instanceOf(DebateFactory.class);
  @Inject
  protected ArgumentFactory argumentFactory = Play.current().injector().instanceOf(ArgumentFactory.class);

  // used for lazy loading of debate justification
  protected Debate debate;

  protected EDebateType eDebateType;

  protected String title;
  protected String fullTitle;
  protected String description;
  protected boolean isMultiple;
  protected ContributorPictureHolder picture = null;
  protected List<DebateExternalUrlForm> externalUrls = new ArrayList<>();

  // contribution that titled the debate id
  protected Long titleContributionId = -1L;
  protected String language;

  protected EDebateVizPane relatedPane = EDebateVizPane.ARGUMENTS;

  protected List<TextHolder> linkedTexts = null;
  protected List<DebateHolder> similarDebates = null;

  protected Long similarityLink = null;

  /*
   * simple debate attributes
   */
  // type, subtype, etc are initialized with -1 instead of empty values
  protected EDebateShade eDebateShade;
  protected int shade = -1;
  protected String shadeterm;
  /*
   * simple and multiple  debate attributes
   */
  protected Long argumentDictionaryId = -1L;
  /*
   * tag debate attributes
   */
  protected Long linkId = -1L;

  /**
   * Play / JSON compliant constructor
   */
  public DebateHolder() {
    super();
    type = EContributionType.DEBATE;
  }

  /**
   * Construct a debate from a given api debate in the given language
   *
   * @param debate a debate
   * @param lang   2-char ISO code of context language (among play accepted languages)
   */
  public DebateHolder(Debate debate, WebdebUser user, String lang) {
    this(debate, user, lang, false);
  }

  /**
   * Construct a debate from a given api debate in the given language
   *
   * @param debate a debate
   * @param lang   2-char ISO code of context language (among play accepted languages)
   */
  public DebateHolder(Debate debate, Long linkId, WebdebUser user, String lang, boolean light) {
    this(debate, user, lang, light);

    this.similarityLink = linkId;
  }
  /**
   * Construct a debate from a given api debate in the given language
   *
   * @param debate a debate
   * @param lang   2-char ISO code of context language (among play accepted languages)
   */
  public DebateHolder(Debate debate, WebdebUser user, String lang, boolean light) {
    super(debate, user, lang, light, light);

    fullTitle = debate.getFullTitle(lang);
    title = debate.getTitle(lang);
    description = debate.getDescription();
    isMultiple = debate.isMultiple();
    eDebateType = debate.getEType();

    picture = light ? null : new ContributorPictureHolder(debate.getPicture());
    
    relatedPane = debate.getNbPositionLinks() > 0 ? EDebateVizPane.SOCIOGRAPHY : EDebateVizPane.ARGUMENTS;

    if(!light)
     externalUrls = debate.getExternalUrls().stream().map(DebateExternalUrlForm::new).collect(Collectors.toList());

    switch (eDebateType) {
      case NORMAL:
        DebateSimple debateSimple = (DebateSimple) debate;

        this.shade = debateSimple.getShade().getType();
        this.eDebateShade = debateSimple.getShade().getEType();
        this.title = debateSimple.getTitle(lang);
        this.language = debateSimple.getArgument().getLanguage().getCode();
        break;
      case TAG_DEBATE:
        DebateTag debateTag = (DebateTag) debate;

        this.fullTitle = debateTag.getFullTitle(lang);
        this.language = lang;
        this.linkId = debateTag.getLinkId();
        break;
    }

  }

  @Override
  public String toString() {
    return "debate [" + id + "] " + (values.isBlank(title) ? fullTitle : title) + " with description " + description + " picture " + picture;
  }

  @Override
  public MediaSharedData getMediaSharedData() {
    if (mediaSharedData == null) {
      mediaSharedData = new MediaSharedData(fullTitle, "debate");
    }
    return mediaSharedData;
  }

  @Override
  public String getDefaultAvatar() {
    return picture != null && picture.isDefined() ? picture.getFilename() : "/assets/images/picto/argument.png";
  }

  @Override
  public String getContributionDescription() {
    List<String> descriptions = new ArrayList<>();
    descriptions.add(fullTitle);
    descriptions.add(description);
    return String.join(", ", descriptions);
  }

  @JsonIgnore
  public List<TextHolder> getTexts() {
    if(linkedTexts == null) {
      linkedTexts = helper.toTextsHolders(debate.findLinkedTexts(), user, lang, true);
    }
    return linkedTexts;
  }

  @JsonIgnore
  public List<DebateHolder> getSimilarDebates() {
    if(similarDebates == null){
      similarDebates = helper.toDebatesHolders(debate.getSimilar(), user, lang);
    }
    return similarDebates;
  }

  @Override
  public Map<Long, TextHolder> getTextsCitations(){
    if(texts == null){
      super.getTextsCitations();

      getTexts().forEach(text -> {
        if(!textsMap.containsKey(text.getId())) {
          textsMap.put(text.getId(), 0);
          texts.put(text.getId(), text);
        }
      });

    }

    return texts;
  }

  /*
   * GETTERS
   */

  /**
   * Get the argument full title (shade and title)
   *
   * @return an argument full title
   */
  public String getFullTitle() {
    return fullTitle;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public boolean getIsMultiple() {
    return isMultiple;
  }

  public ContributorPictureHolder getPicture() {
    return picture;
  }

  public Long getTitleContributionId() {
    return titleContributionId;
  }

  public String getLanguage() {
    return language;
  }

  public int getShade() {
    return shade;
  }

  public EDebateShade getEShade() {
    return eDebateShade;
  }

  public String getShadeterm() {
    return shadeterm;
  }

  public Long getArgumentDictionaryId() {
    return argumentDictionaryId;
  }

  public List<DebateExternalUrlForm> getExternalUrls() {
    return externalUrls;
  }

  public EDebateType geteDebateType() {
    return eDebateType;
  }

  public Long getLinkId() {
    return linkId;
  }

  public EDebateVizPane getRelatedPane() {
    return relatedPane;
  }

  public Long getSimilarityLink() {
    return similarityLink;
  }
}
