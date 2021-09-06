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

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.*;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for the argument_similarity_link database table. Holds a similarity link between two arguments, as
 * well as the type of link between them.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.contribution.link.ESimilarityLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "argument_similarity_link")
@Unqueryable
public class ArgumentSimilarity extends WebdebModel {

  protected static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final Model.Finder<Long, ArgumentSimilarity> find = new Model.Finder<>(ArgumentSimilarity.class);

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  // forcing updates from this object, deletions are handled at the contribution level
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Contribution contribution;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_argument_from", nullable = false)
  private Argument argumentFrom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_argument_to", nullable = false)
  private Argument argumentTo;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_shade", nullable = false)
  private TSimilarityLinkShadeType shade;

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the similarity link id
   *
   * @return an id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set the similarity link id
   *
   * @param idContribution an id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get "supertype" contribution object
   *
   * @return a contribution
   */
  public Contribution getContribution() {
    return contribution;
  }

  /**
   * Set "supertype" contribution object
   *
   * @param contribution a contribution
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get the similarity link shade
   *
   * @return a link shade
   */
  public TSimilarityLinkShadeType getShade() {
    return shade;
  }

  /**
   * Set the similarity link shade
   *
   * @param shade a link shade
   */
  public void setShade(TSimilarityLinkShadeType shade) {
    this.shade = shade;
  }

  /**
   * Get origin argument of this similarity link
   *
   * @return an argument
   */
  public Argument getArgumentFrom() {
    return argumentFrom;
  }

  /**
   * Set origin argument of this similarity link
   *
   * @param argumentFrom an argument
   */
  public void setArgumentFrom(Argument argumentFrom) {
    this.argumentFrom = argumentFrom;
  }

  /**
   * Get destination argument of this similarity link
   *
   * @return an argument
   */
  public Argument getArgumentTo() {
    return argumentTo;
  }

  /**
   * Set destination argument of this similarity link
   *
   * @param argumentTo an argument
   */
  public void setArgumentTo(Argument argumentTo) {
    this.argumentTo = argumentTo;
  }

  /*
   * CONVENIENCE METHODS
   */

  /**
   * Get the current version of this argument link
   *
   * @return a timestamp with the latest update moment of this argument link
   */
  public Timestamp getVersion() {
    return getContribution().getVersion();
  }

  @Override
  public String toString() {
    // because of lazy load, must explicitly call getter
    StringBuilder builder = new StringBuilder(" between ")
            .append(getArgumentFrom().getContribution().getIdContribution()).append(" and ")
            .append(getArgumentTo().getContribution().getIdContribution())
            .append(", shade: [").append(getShade().getIdShade()).append("] ").append(getShade().getEn());

    return getModelDescription(getContribution(), builder.toString());
  }

  /*
   * QUERIES
   */

  /**
   * Retrieve a similarity link by its id
   *
   * @param id the similarity link id
   * @return the similarity link corresponding to that id, null otherwise
   */
  public static ArgumentSimilarity findById(Long id) {
    return id == null || id == -1L ? null : find.byId(id);
  }

  /**
   * Retrieve a similarity link by argument origin and destination.
   *
   * @param origin the argument origin id
   * @param destination the argument destination id
   * @return the corresponding similarity link, null otherwise
   */
  public static ArgumentSimilarity findByOriginAndDestination(Long origin, Long destination) {
    return find.where().eq("id_argument_from", origin).eq("id_argument_to", destination).findUnique();
  }

}
