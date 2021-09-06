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

import be.webdeb.core.api.contribution.EModificationStatus;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the joint table between a contribution and a contributor. Also stores
 * the timestamp and the type of action made on that contribution.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "contribution_has_contributor")
@Unqueryable
public class ContributionHasContributor extends Model {

  private static final Model.Finder<ContributionHasContributorPK, ContributionHasContributor> find =
      new Model.Finder<>(ContributionHasContributor.class);

  @EmbeddedId
  private ContributionHasContributorPK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_contribution", nullable = false)
  private Contribution contribution;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_contributor", nullable = false)
  private Contributor contributor;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "status", nullable = false)
  private TModificationStatus status;

  @Column(name = "serialization", nullable = false)
  private String serialization;

  /**
   * Construct a history log entry for a given contribution bound to a given contributor with the given status
   *
   * @param contribution a contribution
   * @param contributor  a contributor
   * @param status the status of this contribution
   */
  public ContributionHasContributor(Contribution contribution, Contributor contributor, TModificationStatus status) {
    this(contribution, contributor, contribution.toString(), status);
  }

  /**
   * Construct a history log entry for a given replacement bound to a given contributor with the given status (used for merge only)
   *
   * @param origin an origin contribution that will be replaced
   q* @param replacement another contribution that will replace the origin
   * @param contributor a contributor
   */
  public ContributionHasContributor(Contribution origin, Contribution replacement, Contributor contributor) {
    this(replacement, contributor, origin.toString() + " MERGED INTO " + replacement.getIdContribution(),
        TModificationStatus.find.byId(EModificationStatus.MERGE.id()));
  }

  /**
   * Construct a history log entry for a given contribution bound to a given contributor with the given status and log entry
   *
   * @param contribution a contribution
   * @param contributor  a contributor
   * @param serialization a log entry
   * @param status the status of this contribution
   */
  public ContributionHasContributor(Contribution contribution, Contributor contributor, String serialization, TModificationStatus status) {
    setContribution(contribution);
    setContributor(contributor);
    setStatus(status);
    setId(new ContributionHasContributorPK(contribution.getIdContribution(), contributor.getIdContributor(), status));
    setSerialization(serialization);
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the complex join-object id
   *
   * @return a complex id
   */
  public ContributionHasContributorPK getId() {
    return this.id;
  }

  /**
   * Set the complex join-object id
   *
   * @param id a complex id
   */
  public void setId(ContributionHasContributorPK id) {
    this.id = id;
  }

  /**
   * Get the modification status of bound contribution
   *
   * @return a status
   */
  public TModificationStatus getStatus() {
    return this.status;
  }

  /**
   * Set the modification status of bound contribution
   *
   * @param status a status
   */
  public void setStatus(TModificationStatus status) {
    this.status = status;
  }

  /**
   * Get version timestamp, ie, when linked contributor made a modification to linked contribution
   *
   * @return a timestamp
   */
  public Timestamp getVersion() {
    return id.getVersion();
  }

  /**
   * Get bound contribution
   *
   * @return a contribution
   */
  public Contribution getContribution() {
    return this.contribution;
  }

  /**
   * Set bound contribution
   *
   * @param contribution a contribution
   */
  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  /**
   * Get bound contributor (that created / updated bound contribution)
   *
   * @return a contributor
   */
  public Contributor getContributor() {
    return contributor;
  }

  /**
   * Set bound contributor (that created / updated bound contribution)
   *
   * @param contributor a contributor
   */
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  /**
   * Get the full serialization (ie dump) of the modification
   *
   * @return a full serialization of the modification that occurred in the database
   */
  public String getSerialization() {
    return serialization;
  }

  /**
   * Set the full serialization (ie dump) of the modification
   *
   * @param serialization a full serialization of the modification that occurred in the database
   */
  public void setSerialization(String serialization) {
    this.serialization = serialization;
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    return new StringBuffer("contribution_has_contributor [").append(getId())
        .append("] from ").append(getContributor().getEmail())
        .append(", status [").append(getStatus().getIdStatus()).append("]: ").append(getStatus().getEn())
        .append(" serialization: ").append(getSerialization()).toString();
  }

  /*
   * QUERIES
   */

  /**
   * Find all traces for given contribution
   *
   * @param contribution a contribution id
   * @return a (possibly empty) list of history traces
   */
  public static List<ContributionHasContributor> findByContribution(Long contribution) {
    List<ContributionHasContributor> result = find.where().eq("id_contribution", contribution).orderBy("version desc").findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Check if a contributor is the creator of a contribution
   *
   * @param contributor a contributor id
   * @param contribution a contribution id
   * @return a (possibly empty) list of history traces
   */
  public static boolean checkContributionCreator(Long contributor, Long contribution) {
    return find.where().eq("id_contributor", contributor).eq("id_contribution", contribution).findUnique() != null;
  }

  /**
   * The primary key class for the contribution_has_contributor joint table.
   *
   * @author Fabian Gilson
   */
  @Embeddable
  public static class ContributionHasContributorPK extends Model {

    @Column(name = "id_contribution", insertable = false, updatable = false, unique = true, nullable = false)
    private Long idContribution;

    @Column(name = "id_contributor", insertable = false, updatable = false, unique = true, nullable = false)
    private Long idContributor;

    @Column(name = "status", insertable = false, updatable = false, unique = true, nullable = false)
    private Integer status;

    @Version
    @Column(name = "version", insertable = false, updatable = false, unique = true, nullable = false)
    private Timestamp version;

    /**
     * Constructor
     *
     * @param idContribution a contribution id
     * @param idContributor a contributor id
     * @param status the modification status
     */
    public ContributionHasContributorPK(Long idContribution, Long idContributor, TModificationStatus status) {
      this.idContribution = idContribution;
      this.idContributor = idContributor;
      this.status = status.getIdStatus();
    }

    /**
     * Get the contribution id
     *
     * @return a contribution id
     */
    public Long getIdContribution() {
      return this.idContribution;
    }

    /**
     * Set the contribution id
     *
     * @param idContribution a contribution id
     */
    public void setIdContribution(Long idContribution) {
      this.idContribution = idContribution;
    }

    /**
     * Get the contributor id
     *
     * @return a contributor id
     */
    public Long getIdContributor() {
      return this.idContributor;
    }

    /**
     * Set the contributor id
     *
     * @param idContributor a contributor id
     */
    public void setIdContributor(Long idContributor) {
      this.idContributor = idContributor;
    }

    /**
     * Get the modification status id
     *
     * @return a modification status id
     */
    public Integer getStatus() {
      return status;
    }

    /**
     * Set the modification status id
     *
     * @param status a modification status id
     */
    public void setStatus(Integer status) {
      this.status = status;
    }

    /**
     * Get version timestamp, ie, when linked contributor made a modification to linked contribution
     *
     * @return a timestamp
     */
    public Timestamp getVersion() {
      return version;
    }

    /**
     * Set version timestamp, ie, when linked contributor made a modification to linked contribution
     *
     * @param version a timestamp
     */
    public void setVersion(Timestamp version) {
      this.version = version;
    }

    @Override
    public String toString() {
      return "chcPK:" + getIdContribution() + "-" + getIdContributor() + "-" + getVersion();
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ContributionHasContributorPK)) {
        return false;
      }
      ContributionHasContributorPK castOther = (ContributionHasContributorPK) other;
      return this.idContribution.equals(castOther.idContribution)
          && this.idContributor.equals(castOther.idContributor)
          && this.version.equals(castOther.version);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.idContribution.hashCode();
      hash = hash * prime + this.idContributor.hashCode();
      return hash * prime + this.version.hashCode();
    }
  }
}
