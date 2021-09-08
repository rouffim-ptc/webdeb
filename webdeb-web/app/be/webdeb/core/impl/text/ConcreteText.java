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

package be.webdeb.core.impl.text;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.api.text.*;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContextContribution;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.TextAccessor;
import play.i18n.Lang;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements a Text.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteText extends AbstractContextContribution<TextFactory, TextAccessor> implements Text {

  private String publicationDate;
  private Map<String, String> titles = new HashMap<>();

  private Language language;
  private TextType textType;
  private TextSourceType textSourceType;

  private boolean hidden;
  private boolean fetched;

  private TextVisibility textVisibility;

  private String sourceTitle;
  private String url;
  private String embedCode;

  private Map<Long, String> filenames = new HashMap<>();
  private String content;

  private List<Citation> citations = null;
  private int nbCitations = 0;

  private Long externalTextId = -1L;
  private Long linkId = -1L;

  /**
   * Create a Text instance
   *
   * @param factory the text factory
   * @param accessor the text accessor
   * @param actorAccessor an actor accessor (to retrieve/update involved actors)
   * @param contributorFactory the contributor accessor
   */
  ConcreteText(TextFactory factory, TextAccessor accessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, accessor, actorAccessor, contributorFactory);
    this.accessor = accessor;
    type = EContributionType.TEXT;
  }

  @Override
  public Language getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(Language language) {
    this.language = language;
  }

  @Override
  public String getOriginalTitle() {
    String result = null;
    if(language != null)
      result = titles.get(language.getCode());
    if (result == null || "".equals(result)) {
      // try to find french
      result = titles.get("en");
      if (result == null || "".equals(result)) {
        // find the first we can or return empty string
        result = titles.values().stream().filter(Objects::nonNull).findAny().orElse("");
      }
    }
    return result;
  }

  @Override
  public synchronized String getTitle(String lang) {
    String result = titles.get(lang);
    if (result == null || "".equals(result)) {
      // try to find french
      result = titles.get("fr");
      if (result == null || "".equals(result)) {
        // find the first we can or return empty string
        result = titles.values().stream().filter(Objects::nonNull).findAny().orElse("");
      }
    }

    if (factory.getValuesHelper().isBlank(result)) {
      result = i18n.get(Lang.forCode(lang), "viz.text.default.title");
    }

    return result;
  }

  @Override
  public Map<String, String> getTitles() {
    return titles;
  }

  @Override
  public void addTitle(String lang, String name) {
    // simply overwrite existing value
    if (name != null && !"".equals(name.trim()) && lang != null && !"".equals(lang.trim())) {
      titles.put(lang.trim(), name.trim());
    }
  }

  @Override
  public void setTitles(Map<String, String> i18names) {
    this.titles = i18names;
  }

  @Override
  public List<ActorRole> getAuthors() {
    return getActors().stream().filter(ActorRole::isAuthor).collect(Collectors.toList());
  }

  @Override
  public String getPublicationDate() {
    return publicationDate;
  }

  @Override
  public void setPublicationDate(String publicationDate) throws FormatException {
    if (publicationDate != null && !factory.getValuesHelper().isDate(publicationDate)) {
      throw new FormatException(FormatException.Key.TEXT_ERROR, "publication date is invalid " + publicationDate);
    }
    // harmonize to "/" separator
    if (publicationDate != null) {
      this.publicationDate = publicationDate.replace("\\.", "/").trim();
    }
  }

  @Override
  public TextType getTextType() {
    return textType;
  }

  @Override
  public void setTextType(TextType textType) {
    this.textType = textType;
  }

  @Override
  public TextSourceType getTextSourceType() {
    return textSourceType;
  }

  @Override
  public void setTextSourceType(TextSourceType textSourceType) {
    this.textSourceType = textSourceType;
  }

  @Override
  public boolean isHidden() {
    return hidden;
  }

  @Override
  public void isHidden(boolean hidden) {
    this.hidden = hidden;
  }

  @Override
  public boolean isFetched() {
    return fetched;
  }

  @Override
  public void isFetched(boolean fetched) {
    this.fetched = fetched;
  }

  @Override
  public TextVisibility getTextVisibility() {
    return textVisibility;
  }

  @Override
  public void setTextVisibility(TextVisibility textVisibility) {
    this.textVisibility = textVisibility;
  }

  @Override
  public String getSourceTitle() {
    return sourceTitle;
  }

  @Override
  public void setSourceTitle(String sourceTitle) {
    this.sourceTitle = sourceTitle != null ? sourceTitle.replaceAll("’", "'").trim() : null;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) throws FormatException {
    if (factory.getValuesHelper().isURL(url) && url.length() <= MAX_URL_SIZE) {
      this.url = url.trim();

      if(this.url.contains("#")) {
        this.url = this.url.substring(0, this.url.indexOf("#"));
      }

    } else {
      throw new FormatException(FormatException.Key.TEXT_ERROR, "url has a non valid format " + url);
    }
  }

  @Override
  public String getEmbedCode() {
    return embedCode;
  }

  @Override
  public void setEmbedCode(String embedCode) {
    this.embedCode = embedCode != null && !embedCode.isEmpty() ? embedCode : null;
  }

  @Override
  public Long getCurrentSuperContextId() {
    return super.getCurrentSuperContextId();
  }

  @Override
  public Long getExternalTextId() {
    return externalTextId;
  }

  @Override
  public void setExternalTextId(Long externalTextId) {
    this.externalTextId = externalTextId;
  }

  @Override
  public int getNbCitations() {
    return nbCitations;
  }

  @Override
  public void setNbCitations(int nbCitations) {
    this.nbCitations = nbCitations;
  }

  @Override
  public Long getLinkId() {
    return linkId;
  }

  @Override
  public void setLinkId(Long id) {
    this.linkId = id;
  }

  @Override
  public Map<Long, String> getFilenames() {
    return filenames;
  }

  @Override
  public void setFilenames(Map<Long, String> filenames) {
    this.filenames = filenames;
  }

  @Override
  public void addFilename(Long contributor, String filename) {
    if (textVisibility != null) {
      if (ETextVisibility.PUBLIC == textVisibility.getEType()
      || ETextVisibility.PEDAGOGIC == textVisibility.getEType()) {
        filenames.put(-1L, filename);
      } else {
        filenames.put(contributor, filename);
      }
    } else {
      logger.warn("text has no visibility, may not add filename " + filename + " for contributor "
          + contributor + " to text " + id);
    }
  }

  @Override
  public boolean isContentVisibleFor(Long contributor) {
    try {
      return !"".equals(getFilename(contributor));
    } catch (PermissionException e) {
      //logger.debug("content of " + id + " is not viewable for " + contributor);
      return false;
    }
  }

  @Override
  public String getFilename(Long contributor) throws PermissionException {
    String filename = null;
    boolean loaded = false;
    Contributor c;
    GroupSubscription subscription;
    if (textVisibility != null) {
      switch (textVisibility.getEType()) {
        case PUBLIC:
          filename = filenames.get(-1L);
          loaded = true;
          break;
        case PEDAGOGIC:
          c = contributorFactory.retrieveContributor(contributor);
          if(c != null) {
            subscription = contributorFactory.retrieveGroupSubscription(contributor, Group.getGroupPublic());
            boolean inPedagogicGroup = contributorFactory.contributorIsPedagogicForGroupAndContribution(contributor, id);
            if (inPedagogicGroup || subscription.getRole() == EContributorRole.ADMIN) {
              filename = filenames.get(-1L);
              loaded = true;
            }
          }
          break;
        case PRIVATE:
          filename = filenames.get(contributor);
          // if this contributor is at least a group owner of any of this contribution's group,
          // return creator file
          if (filename == null && getInGroups().stream().anyMatch(g ->
              g.getGroupOwners().stream().anyMatch(s -> s.getContributor().getId().equals(contributor)))) {
            c = getCreator();
            // maybe we are saving this file so it has not yet a creator, be careful
            if (c != null) {
              filename = filenames.get(getCreator().getId());
              loaded = true;
            }
          }
          break;
        default:
          logger.error("unsupported text visibility " + textVisibility);
      }
    }
    if (!loaded) {
      throw new PermissionException(PermissionException.Key.TEXT_NOT_VISIBLE);
    }
    return filename;
  }

  @Override
  public String getTextContent(Long contributor) throws PermissionException {
    if (content == null) {
      content = accessor.getContributionTextFile(getFilename(contributor));
    }
    return content;
  }

  @Override
  public void setTextContent(String content) {
    this.content = content;
  }

  @Override
  public String getTextExcerpt(Long contributor, int maxSize) throws PermissionException {
    // if we have a pedagogic or public, simply feed content if possible
    if (textVisibility.getEType() != ETextVisibility.PRIVATE) {
      return accessor.getContributionTextFile(filenames.get(-1L), maxSize);
    }

    // for private texts, get contributor's one
    if (filenames.containsKey(contributor)) {
      return accessor.getContributionTextFile(filenames.get(contributor), maxSize);
    }

    // no content could be retrieved, throw permission exception since given contributor is not allowed to see content
    throw new PermissionException(PermissionException.Key.TEXT_NOT_VISIBLE);
  }

  @Override
  public List<Citation> getTextCitations() {
    if (citations == null) {
      citations = accessor.getCitations(id);
    }
    return citations;
  }

  @Override
  public List<Citation> getTextCitations(SearchContainer query) {
    return accessor.getTextCitations(query);
  }

  @Override
  public void addCitation(Citation citation) {
    getTextCitations().add(citation);
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
    List<String> isValid = isValid();
    if (!isValid.isEmpty()) {
      logger.error("text contains errors " + isValid.toString());
      throw new FormatException(FormatException.Key.TEXT_ERROR, String.join(",", isValid.toString()));
    }
    return accessor.save(this, currentGroup, contributor);
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();
    if (language == null) {
      fieldsInError.add("language is null");
    }
    getAuthors().forEach(a -> {
      String errors = checkAuthor(a.getActor());
      if (errors.length() > 0) {
        fieldsInError.add(errors);
      }
    });
    if (publicationDate != null && !factory.getValuesHelper().isDate(publicationDate)) {
      fieldsInError.add("publicationDate is not a date");
    }

    return fieldsInError;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Text && hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 47 * (id != -1L ? id.hashCode() : 83) + getTitle(getLanguage().getCode()).hashCode()
            + textSourceType.getType().hashCode() + (publicationDate != null ? publicationDate.hashCode() : 97)
            + (url != null ? url.hashCode() : 25);
  }

  @Override
  public String toString() {
    String title = "";
    if(getTitles()!= null) {
      Optional<Map.Entry<String, String>> foundTitle = titles.entrySet().stream().findFirst();
      title = (foundTitle.isPresent() ? foundTitle.get().getValue() : "");
    }
    return  title + " with authors " + getActors().stream().map(a ->
        a.getActor().toString()).collect(Collectors.joining(", "))
        + " of type " + (getTextType() != null ? textType.getName("en") : " null")
        + " of source type " + (getTextSourceType() != null ? textSourceType.getName("en") : " null")
        + " with tags " + (getTags() != null ? tags.toString() : "null") + " (" + url + ")";
  }

  /*
   * private helpers
   */

  /**
   * Check an author and concat result of all results of Actor.isContributorValid calls
   *
   * @param author an actor to check
   * @return a (possibly) empty string with any validation error for given actor
   */
  private String checkAuthor(Actor author) {
    return String.join(",", author.isValid());
  }
}
