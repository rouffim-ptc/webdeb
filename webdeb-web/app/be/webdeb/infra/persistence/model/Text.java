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

package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.PrivateOwned;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * The persistent class for the text database table, subtype of contributions, representing a text of any kind,
 * like press articles, book chapters, blog post, etc. A text may contains citations, ie, citations have been
 * extracted from a text to be translated into Citations
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "text")
public class Text extends WebdebModel {

  private static final Model.Finder<Long, Text> find = new Model.Finder<>(Text.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  // forcing updates from this object, deletions are handled at the contribution level
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Contribution contribution;

  @OneToMany(mappedBy = "text", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<TextI18name> titles;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_language", nullable = true)
  private TLanguage language;

  @Column(name = "publication_date")
  private String publicationDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_type", nullable = true)
  private TTextType textType;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_source_type")
  private TTextSourceType textSourceType;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_source_name")
  @PrivateOwned
  private TextSourceName sourceName;

  @Column(name = "url", length = 2048)
  private String url;

  @Column(name = "embed_code", nullable = true)
  private String embedCode;

  @Column(name = "fetched")
  private int fetched;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_visibility", nullable = false)
  private TTextVisibility visibility;

  @OneToMany(mappedBy = "text", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Unqueryable
  private List<TextContent> contents;

  @OneToMany(mappedBy = "text", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
  private List<Citation> citations;

  @Transient
  @Unqueryable
  private Integer nb_contributions;

  @Transient
  @Unqueryable
  private Long linkId;

  /**
   * Get the "supertype" contribution object
   *
   * @return a contribution object
   */
  public Contribution getContribution() {
    return contribution;
  }

  /**
   * Set the "supertype" contribution object
   *
   * @param contribution a contribion
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get this text id
   *
   * @return an id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set this text id
   *
   * @param idContribution an id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get the list of titles for this text (names may have multiple spellings)
   *
   * @return a list of titles
   */
  public List<TextI18name> getTitles() {
    return titles;
  }

  /**
   * Set the list of titles for this text (names may have multiple spellings)
   *
   * @param titles a list of titles to set
   */
  public void setTitles(List<TextI18name> titles) {
    if (titles != null) {
      if (this.titles == null) {
        this.titles = new ArrayList<>();
      }

      // get previous languages for current names
      List<String> currentlangs = this.titles.stream().map(TextI18name::getLang).collect(Collectors.toList());

      // add/update new names
      titles.forEach(this::addTitle);

      currentlangs.stream().filter(lang -> titles.stream().noneMatch(n -> n.getLang().equals(lang))).forEach(lang ->
              this.titles.removeIf(current -> current.getLang().equals(lang))
      );
    }
  }

  /**
   * Add a title to this text, if such language already exists, will update existing title
   *
   * @param title a title structure
   */
  public void addTitle(TextI18name title) {
    if (titles == null) {
      titles = new ArrayList<>();
    }
    Optional<TextI18name> match = titles.stream().filter(n ->
        n.getLang().equals(title.getLang())).findAny();
    if (!match.isPresent()) {
      titles.add(title);
    }else{
      int i = titles.lastIndexOf(match.get());
      if(i >= 0){
        titles.get(i).setSpelling(title.getSpelling());
      }
    }
  }

  public String getOriginalTitle(){
    Optional<TextI18name> firstMatchedLanguage = titles.stream().filter(e -> language.getCode().equals(e.getLang())).findFirst();
    Optional<TextI18name> firstMatched = titles.stream().findFirst();
    String title = "";
    if(firstMatchedLanguage .isPresent()){
      title = firstMatchedLanguage.get().getSpelling();
    } else if(firstMatched.isPresent()){
      title = firstMatched.get().getSpelling();
    }
    return title;
  }

  /**
   * Get the contents associated to this text
   *
   * @return a (possibly empty) list of text content objects
   */
  public List<TextContent> getContents() {
    return contents != null ? contents : new ArrayList<>();
  }

  /**
   * Set the contents associated to this text
   *
   * @param contents a list of text content objects
   */
  public void setContents(List<TextContent> contents) {
    this.contents = contents;
  }

  /**
   * Add given content to this text. If text is private, a new content will be added for this contributor (if necessary),
   * otherwise, existing content will be replaced by given content.
   *
   * @param content a text content to bind to this text
   */
  public boolean addContent(TextContent content) {
    if (contents == null) {
      contents = new ArrayList<>();
    }

    // if this content already exists, ignore
    if (contents.stream().anyMatch(c -> c.getFilename().equals(content.getFilename()))) {
      return false;
    }

    // if this text is not private, force clear of all existing contents (ebean does not want to cascade here, dunno why)
    if (visibility.getIdVisibility() != ETextVisibility.PRIVATE.id()) {
      contents.clear();
      contents.add(content);
    } else {
      Optional<TextContent> toUpdate = contents.stream().filter(c ->
          c.getContributor() != null && c.getContributor().getIdContributor().equals(content.getContributor().getIdContributor())).findAny();
      if (toUpdate.isPresent()) {
        logger.debug("update " + toUpdate.get().toString());
        toUpdate.get().setFilename(content.getFilename());

      } else {
        logger.debug("create new " + content.toString());
        contents.add(content);
      }
    }
    return true;
  }

  /**
   * Get the language of this text
   *
   * @return a language
   */
  public TLanguage getLanguage() {
    return language;
  }

  /**
   * Set the language of this text
   *
   * @param language a language
   */
  public void setLanguage(TLanguage language) {
    this.language = language;
  }

  /**
   * Get the publication date, may be null
   *
   * @return the publication date as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public String getPublicationDate() {
    return publicationDate;
  }

  /**
   * Set the publication date
   *
   * @param publicationDate the publication date as a string of the form YYYYMMDD, where MM and DD may be zeros and Y may be preceded by a "-"
   */
  public void setPublicationDate(String publicationDate) {
    this.publicationDate = publicationDate;
  }

  /**
   * Get the text type
   *
   * @return this text's type
   */
  public TTextType getTextType() {
    return textType;
  }

  /**
   * Set the text type
   *
   * @param textType a type for this text
   */
  public void setTextType(TTextType textType) {
    this.textType = textType;
  }

  /**
   * Get the text source type
   *
   * @return this text's source type
   */
  public TTextSourceType getTextSourceType() {
    return textSourceType;
  }

  /**
   * Set the text source type
   *
   * @param textSourceType a source type for this text
   */
  public void setTextSourceType(TTextSourceType textSourceType) {
    this.textSourceType = textSourceType;
  }

  /**
   * Get the text visibility
   *
   * @return the visibility for this text
   */
  public TTextVisibility getVisibility() {
    return visibility;
  }

  /**
   * Set the text visibility
   *
   * @param visibility the visibility for this text
   */
  public void setVisibility(TTextVisibility visibility) {
    this.visibility = visibility;
  }

  /**
   * Get the external url where this text has been copied from
   *
   * @return an url, or null if this text does not come from an internet page
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the external url where this text has been copied from
   *
   * @param url an url if this text does not come from an internet page
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Get the embed code for display web content from plaform.
   *
   * @return the embed code, if any (like iframe)
   */
  public String getEmbedCode() {
    return embedCode;
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
   * Get the source name (joint-object), if any
   *
   * @return a source name (joint-object), null if this text does not come from an identified source
   */
  public TextSourceName getSourceName() {
    return sourceName;
  }

  /**
   * Set the source name (joint-object), if any
   *
   * @param sourceName a source name (joint-object)
   */
  public void setSourceName(TextSourceName sourceName) {
    this.sourceName = sourceName;
  }

  /**
   * Get the list excepts coming from this text
   *
   * @return a (possibly empty) list of except
   */
  public List<Citation> getCitations() {
    return citations != null ? citations : new ArrayList<>();
  }

  /**
   * Set the list citations coming from this text
   *
   * @param citations a list of citation
   */
  public void setCitations(List<Citation> citations) {
    this.citations = citations;
  }

  /**
   * Check whether this text has been automatically fetched from an external source (RSS feed client for example)
   *
   * @return true if this text has been automatically fetched
   */
  public boolean isFetched() {
    return fetched == 1;
  }

  /**
   * Set whether this text has been automatically fetched from an external source (RSS feed client for example)
   *
   * @param fetched true if this text has been automatically fetched
   */
  public void isFetched(boolean fetched) {
    this.fetched = fetched ? 1 : 0;
  }

  /**
   * Get the number of contributions if this data is asked in a query
   *
   * @return the number of contributions
   */
  public Integer getNbContributions() {
    return nb_contributions;
  }

  public Long getLinkId() {
    return linkId;
  }

  public void setLinkId(Long linkId) {
    this.linkId = linkId;
  }

  /*
   * CONVENIENCE METHODS
   */

  /**
   * Get the current version of this text
   *
   * @return a timestamp with the latest update moment of this text
   */
  public Timestamp getVersion() {
    return getContribution().getVersion();
  }

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    String title = (getTitles() != null && !titles.isEmpty() ? titles.get(0).getSpelling() : "");
    StringBuilder builder =  new StringBuilder(", translations: {").append(titles.stream()
                    .map(t -> t.getSpelling() + " [" + t.getLang() + "]").collect(Collectors.joining(", ")))
        .append("}, lang: ").append(getLanguage().getCode())
        .append(", published: ").append(getPublicationDate())
        .append(", type: ").append(getTextType() != null ? getTextType().getEn() : "none")
        .append(", source type: ").append(getTextSourceType().getEn())
        .append(", source: ").append(getSourceName() != null ? getSourceName().toString() : "none")
        .append(", url: ").append(getUrl())
        .append(", visibility: ").append(getVisibility().getIdVisibility())
        .append(", contents: ").append(getContents().stream().map(TextContent::toString).collect(Collectors.joining(", ")));

    return getModelDescription(getContribution(), title, builder.toString());
  }

  /*
   * QUERIES
   */

  /**
   * Find a text by its id
   *
   * @param id a text (contribution) id
   * @return the Text if it has been found, null otherwise
   */
  public static Text findById(Long id) {
    return id == null || id == -1L ? null : find.byId(id);
  }

  /**
   * Find a list of texts by a partial title
   *
   * @param title a partial title
   * @return the list of Text matching the given partial title
   */
  public static List<Text> findByPartialTitle(String title) {
    List<Text> result = null;
    if (title != null) {
      // will be automatically ordered by relevance
      String token = getSearchToken(title);

      String select = "select distinct text.id_contribution from text right join text_i18names " +
          "on text.id_contribution = text_i18names.id_contribution where spelling like '" + token + "'";

      logger.debug("search for text : " + select);
      result = Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Find a text by its url
   *
   * @param url a url to look for
   * @return the Text matched or null
   */
  public static Text findByUrl(String url) {
    List<Text> result = findMultipleByUrl(url);
    return result != null && !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Find a list of texts by its url
   *
   * @param url a url to look for
   * @return a possibly empty list of texts
   */
  public static List<Text> findMultipleByUrl(String url) {
    List<Text> texts = new ArrayList<>();
    if(url != null && !url.equals("")) {
      String select = "select id_contribution from text where url like \"%" + url.trim() + "\"";
      texts = Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }

    return texts;
  }

  /**
   * Get a randomly chosen publicly visible text from the database
   *
   * @return a random Text
   */
  public static Text random() {
    return findById(random(EContributionType.TEXT));
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Get the list of text where text's citations have given tag
   *
   * @param tag a tag id
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return a possibly empty list of texts
   */
  public static List<Text> findTextsFromCitationsTag(Long tag, Long contributorId, int groupId){
    String select = "SELECT t.id_contribution FROM text t " +
            "inner join citation ci on ci.id_text = t.id_contribution " +
            "inner join contribution c on ci.id_contribution = c.id_contribution " +
            "left join contribution_has_tag cht on cht.id_contribution = c.id_contribution " +
            getContributionStatsJoins() +
            " where cht.id_tag = " + tag + getContributionStatsWhereClause(contributorId, groupId) +
            " order by c.version desc";
    return Ebean.find(Text.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
  }

  /**
   * Get the number of citations linked to this text
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return the number of citations linked to this text
   */
  public int getNbCitations(Long contributorId, int groupId){
    String select = "SELECT count(distinct e.id_contribution) as 'count' FROM citation e " +
            getContributionStatsJoins("e.id_contribution") +
            " where e.id_text = " + idContribution + getContributionStatsWhereClause(contributorId, groupId);
    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  /**
   * Get the number of arguments linked to this text
   *
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return the number of arguments linked to this text
   */
  public int getNbArguments(Long contributorId, int groupId){
    String select = "SELECT count(distinct a.id_contribution) as 'count' FROM argument_context a " +
            getContributionStatsJoins("a.id_contribution") +
            " where a.id_context = " + idContribution + getContributionStatsWhereClause(contributorId, groupId);
    int nbArguments = Ebean.createSqlQuery(select).findUnique().getInteger("count");
    return nbArguments > 0 ? nbArguments - 1 : 0;
  }

  /**
   * Get the number of citations linked to this text in the given context
   *
   * @param contextId a context contribution id
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @param contextIsTag true if the context is a tag
   * @return the number of citations linked to this text in given context
   */
  public int getNbCitationsInContext(Long contextId, Long contributorId, int groupId, boolean contextIsTag){
    String select;

    if(contextIsTag) {
      select = "SELECT count(distinct ci.id_contribution) as 'count' FROM citation ci " +
                "inner join contribution_has_tag cht on cht.id_contribution = ci.id_contribution " +
              getContributionStatsJoins("ci.id_contribution") +
              " where ci.id_text = " + idContribution + " and cht.id_tag = " + contextId +
              getContributionStatsWhereClause(contributorId, groupId);
    } else {
      select = "SELECT count(distinct ci.id_contribution) as 'count' FROM citation ci " +
              "left join citation_justification_link cjl on cjl.id_citation = ci.id_contribution " +
              "left join citation_position_link cpl on cpl.id_citation = ci.id_contribution " +
              getContributionStatsJoins("ci.id_contribution") +
              getContributionStatsJoins("cjl.id_contribution", "1") +
              getContributionStatsJoins("cpl.id_contribution", "2") +
              " where ci.id_text = " + idContribution + " and " +
              "(cjl.id_context = " + contextId + " or cpl.id_debate = " + contextId + ")" +
              getContributionStatsWhereClause(contributorId, groupId) +
              getContributionStatsWhereClause(contributorId, groupId, "1") +
              getContributionStatsWhereClause(contributorId, groupId, "2");
    }

    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }

  /**
   * Get the number of citations linked to this text where given actor is author
   *
   * @param actorId an actorId
   * @param contributorId the id of the contributor for which we need that stats
   * @param groupId the group where see the stats
   * @return the number of citations linked to this text in given context
   */
  public int getNbCitationsWhereActorAuthor(Long actorId, Long contributorId, int groupId){
    String select = "SELECT count(distinct ci.id_contribution) as 'count' FROM citation ci " +
            "inner join contribution_has_actor cha on cha.id_contribution = ci.id_contribution " +
            getContributionStatsJoins("ci.id_contribution") +
            " where ci.id_text = " + idContribution + getContributionStatsWhereClause(contributorId, groupId) +
            " and cha.id_actor = " + actorId + " and cha.is_author = 1";
    return Ebean.createSqlQuery(select).findUnique().getInteger("count");
  }
}
