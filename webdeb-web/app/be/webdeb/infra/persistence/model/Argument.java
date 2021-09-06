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

import be.webdeb.core.api.argument.EArgumentShade;
import be.webdeb.core.api.argument.EArgumentType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * The persistent class for the argument database table, conceptual subtype of contribution.
 * May have justification or similarity links to / from other arguments (materialized by ArgumentJustification and
 * ArgumentSimilarity objects).
 *
 * An argument is a simple sentence written by a contributor and that can be added in a debate or a text justification tree.
 * The debate and the text are then the context with others data like folders, places an concerned actors.
 *
 * This sentence is keep in a dictionary table to keep it language unique.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "argument")
public class Argument extends WebdebModel {

  protected static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, Argument> find = new Model.Finder<>(Argument.class);

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  // forcing updates from this object, deletions are handled at the contribution level
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Contribution contribution;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_dictionary", nullable = false)
  private ArgumentDictionary dictionary;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_type", nullable = false)
  private TArgumentType type;

  @OneToOne(mappedBy = "argument", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private ArgumentShaded argumentShaded;

  @OneToMany(mappedBy = "argumentFrom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ArgumentSimilarity> similaritiesFrom;

  @OneToMany(mappedBy = "argumentTo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ArgumentSimilarity> similaritiesTo;

  /**
   * Get parent contribution object
   *
   * @return the parent contribution
   */
  public Contribution getContribution() {
    return contribution;
  }

  /**
   * Set parent contribution object
   *
   * @param contribution the parent contribution
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get the id of argument
   *
   * @return the argument id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set the id of argument
   *
   * @param idContribution the argument id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get the dictionary of this argument
   *
   * @return argument dictionary
   */
  public ArgumentDictionary getDictionary() {
    return dictionary;
  }

  /**
   * Set the dictionary of this argument
   *
   * @param dictionary an argument dictionary
   */
  public void setDictionary(ArgumentDictionary dictionary) {
    this.dictionary = dictionary;
  }

  /**
   * Get this argument type
   *
   * @return the argument type
   */
  public TArgumentType getType() {
    return type;
  }

  /**
   * Set this argument type
   *
   * @param type a type to set
   */
  public void setType(TArgumentType  type) {
    this.type = type;
  }

  /**
   * Get the shaded argument "subtype" object, if this argument is shaded (argumenttype-dependent)
   *
   * @return the argument subtype if this argument is shaded, null otherwise
   */
  public ArgumentShaded getArgumentShaded() {
    return argumentShaded;
  }

  /**
   * Set the shaded argument "subtype" object, if this argument is shaded (argumenttype-dependent)
   *
   * @param argumentShaded the argument subtype object
   */
  public void setArgumentShaded(ArgumentShaded argumentShaded) {
    this.argumentShaded = argumentShaded;
  }

  /**
   * Get the similarities argument "from"
   *
   * @return a possibly empty list of argument similarity
   */
  public List<ArgumentSimilarity> getSimilaritiesFrom() {
    return similaritiesFrom;
  }

  /**
   * Get the similarities argument "to"
   *
   * @return a possibly empty list of argument similarity
   */
  public List<ArgumentSimilarity> getSimilaritiesTo() {
    return similaritiesTo;
  }

  /**
   * Get the current version of this argument
   *
   * @return a timestamp with the latest update moment of this argument
   */
  public Timestamp getVersion() {
    return getContribution().getVersion();
  }

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    StringBuilder builder = new StringBuilder(getDictionary().toString());

    if(getType().getEArgumentType() == EArgumentType.SHADED && getArgumentShaded() != null){
      builder.append(getArgumentShaded().toString());
    }

    return getModelDescription(getContribution(), builder.toString());
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve an argument by its id
   *
   * @param id an id
   * @return the argument corresponding to the given id, null if not found
   */
  public static Argument findById(Long id) {
    return id == null || id == -1L ? null : find.byId(id);
  }

  /**
   * Find an unique argument by a complete title
   *
   * @param title an argument title
   * @param lang a i18 lang
   * @return a matched argument or null
   */
  public static Argument findUniqueByTitleAndLang(String title, String lang) {
    List<Argument> result = null;
    if (title != null && lang != null) {
      String select = "select distinct a.id_contribution from argument a " +
              "inner join argument_dictionary ad on ad.id_contribution = a.id_dictionary " +
              "where ad.title = '" + getStrictSearchToken(title) + "' and ad.id_language = '" + lang + "'";
      result = Ebean.find(Argument.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null && !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Find an unique argument by a complete title and shade
   *
   * @param title an argument title
   * @param lang a i18 lang
   * @param shade an argument shade
   * @return a matched argument or null
   */
  public static Argument findUniqueByTitleLangAndShade(String title, String lang, EArgumentShade shade) {
    List<Argument> result = null;
    if (title != null && lang != null) {
      String select = "select distinct a.id_contribution from argument a " +
              "inner join argument_dictionary ad on ad.id_contribution = a.id_dictionary " +
              "inner join argument_shaded ash on ash.id_contribution = a.id_contribution " +
              "where " + (shade != null ? "ash.id_shade = " + shade.id() + " and" : "") +
              " ad.title = '" + getStrictSearchToken(title) + "' and ad.id_language = '" + lang + "'";
      result = Ebean.find(Argument.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null && !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Check a list of ids and sends back ids that are actually arguments
   *
   * @param ids a list of ids
   * @return the given list of ids where the non-existing argument has been removed
   */
  public static List<Long> exists(List<Long> ids) {
    List<Argument> result = find.select("idContribution").where().in("idContribution", ids).findList();
    return result != null ?
        result.stream().map(a -> a.getContribution().getIdContribution()).collect(Collectors.toList())
        : new ArrayList<>();
  }

  /**
   * Check if an argument is similar to another one
   *
   * @param argToCompare the argument id to compare with the other one
   * @param argument an argument id
   * @return true if both arguments are similar
   */
  public static boolean similarArguments(Long argToCompare, Long argument){
    String select = "SELECT id_contribution FROM argument_similarity where " +
            "((id_argument_from = " + argToCompare + " and id_argument_to = " + argument + ") " +
            "or (id_argument_from = " + argument + " and id_argument_to = " + argToCompare + ") and shade = " + ESimilarityLinkShade.SIMILAR.id() + ")";
    return Ebean.find(ArgumentSimilarity.class).setRawSql(RawSqlBuilder.parse(select).create()).findRowCount() > 0;
  }

  /**
   * Get the list of similar links for this argument
   *
   * @return a possibly empty list of argument similarity links
   */
  public List<ArgumentSimilarity> getSimilarArguments() {
    List<ArgumentSimilarity> similarities = getSimilaritiesFrom();
    similarities.addAll(getSimilaritiesTo());
    return similarities;
  }

  /**
   * Get a randomly chosen argument from the database
   *
   * @return a random Argument
   */
  public static Argument random() {
    return findById(random(EContributionType.ARGUMENT));
  }

  /**
   * Find a list of arguments by a complete title and lang and type if needed
   *
   * @param title an argument dictionary title
   * @param lang a i18 lang
   * @param type the type of argument, if needed
   * @param shade the shade of argument, if any otherwise -1
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of Argument matching the given title and lang
   */
  public static List<Argument> findByTitleAndLang(String title, String lang, int type, int shade, int fromIndex, int toIndex) {
    List<Argument> result = null;
    if (title != null) {
      // will be automatically ordered by relevance
      String token = getSearchToken(title);
      String select = "select distinct a.id_contribution from argument a " +
              "left join argument_dictionary ad on ad.id_contribution = a.id_dictionary " +
              (shade != -2 ? "left join argument_shaded ash on ash.id_contribution = a.id_contribution " : "") +
              "where ad.title like '%" + token + "%'" + (lang == null ? "" : " and ad.id_language = '" + lang + "'") +
              (type != -1 ? " and a.id_type = " + type : "") +
              (shade != -2 ? " and ash.id_shade = " + shade : "") +
              getSearchLimit(fromIndex, toIndex);

      result = Ebean.find(Argument.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get the number of citations linked with given argument in given context
   *
   * @param context the context contribution id
   * @param subContext the tag sub context id
   * @param category the tag category id
   * @param argument the argument of the link id
   * @param shade the link shade id
   * @param contributor a contributor id
   * @param group a group id
   * @return the number of citation justification links
   */
  public static int getNbCitationsLinks(Long context, Long subContext, Long category, Long argument, Integer shade, Long contributor, int group){
    String sql = "SELECT count(distinct link.id_contribution) as 'count' FROM citation_justification_link link " +
            "inner join contribution c on c.id_contribution = link.id_contribution" +
            getContributionStatsJoins("c.id_contribution") +
            " where link.id_context = " + context + " and link.id_sub_context " + isNotBlankOrNull(subContext) +
            " and link.id_category " + isNotBlankOrNull(category) +  " and link.id_argument " + isNotBlankOrNull(argument) +
            (shade != null ? " and link.id_shade = " + shade : "") +
            getContributionStatsWhereClause(contributor, group);
    return Ebean.createSqlQuery(sql).findUnique().getInteger("count");
  }
}
