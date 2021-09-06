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

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The persistent class for the organization database table, conceptual subtype of an actor.
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "organization")
public class Organization extends WebdebModel {

  @Id
  @Column(name = "id_contribution", unique = true, nullable = false)
  private Long idContribution;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
  private Actor actor;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "legal_status", nullable = false)
  private TLegalStatus legalStatus;

  @Column(name = "creation_date")
  private String creationDate;

  @Column(name = "termination_date")
  private String terminationDate;

  @Column(name = "official_number")
  private String officialNumber;

  @ManyToMany(mappedBy = "organizations", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TBusinessSector> sectors;

  /**
   * Get the contribution id
   *
   * @return a unique id
   */
  public Long getIdContribution() {
    return idContribution;
  }

  /**
   * Set this contribution id
   *
   * @param idContribution an id
   */
  public void setIdContribution(Long idContribution) {
    this.idContribution = idContribution;
  }

  /**
   * Get the legal status
   *
   * @return a status
   */
  public TLegalStatus getLegalStatus() {
    return legalStatus;
  }

  /**
   * Set the legal status
   *
   * @param legalStatus a legal status
   */
  public void setLegalStatus(TLegalStatus legalStatus) {
    this.legalStatus = legalStatus;
  }

  /**
   * Get the parent actor object
   *
   * @return an Actor DB object
   */
  public Actor getActor() {
    return actor;
  }

  /**
   * Set the parent actor object
   *
   * @param actor the parent Actor DB object
   */
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  /**
   * Get the creation date of this organization, if any (in DD/MM/YYYY format, DD and MM optional)
   *
   * @return a creation date in the above format, null if none
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Set the creation date of this organization, if any (in DD/MM/YYYY format, DD and MM optional)
   *
   * @param creationDate a creation date in the above format, null if none
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Get the termination date of this organization, if any (in DD/MM/YYYY format, DD and MM optional)
   *
   * @return a termination date in the above format, null if none
   */
  public String getTerminationDate() {
    return terminationDate;
  }

  /**
   * Set the creation date of this organization, if any (in DD/MM/YYYY format, DD and MM optional)
   *
   * @param terminationDate a termination date in the above format, null if none
   */
  public void setTerminationDate(String terminationDate) {
    this.terminationDate = terminationDate;
  }

  /**
   * Get the official number (country-dependent identification number)
   *
   * @return an identification number (may be null)
   */
  public String getOfficialNumber() {
    return officialNumber;
  }

  /**
   * Set the official number (country-dependent identification number)
   *
   * @param officialNumber an identification number
   */
  public void setOfficialNumber(String officialNumber) {
    this.officialNumber = officialNumber;
  }

  /**
   * Get the list of sectors where this organization is active
   *
   * @return a (possibly empty) list of sectors
   */
  public List<TBusinessSector> getSectors() {
    return sectors != null ? sectors : new ArrayList<>();
  }

  /**
   * Set the list of sectors where this organization is active
   *
   * @param sectors a list of sectors
   */
  public void setSectors(List<TBusinessSector> sectors) {
    this.sectors = sectors;
  }

  /**
   * Add given sector to list of business sectors where this organization is active (if not already bound)
   *
   * @param sector a sector to add
   */
  public void addSector(TBusinessSector sector) {
    if (!sectors.contains(sector)) {
      sectors.add(sector);
    }
  }

  /**
   * Remove given sector from list od sectors where this organization is active
   *
   * @param sector a sector to remove
   */
  public void removeSector(TBusinessSector sector) {
    sectors.remove(sector);
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    // must use getters and explicitly loop into references, otherwise ebean may send back deferred beanlist
    // (lazy load not triggered from toString methods)
    StringBuilder builder =  new StringBuilder(", official number: ").append(officialNumber)
        .append(", status: ").append(getLegalStatus() != null ? getLegalStatus().getIdStatus() : "unset")
        .append(", sectors: ").append(getSectors().stream().map(s ->
            String.valueOf(s.getIdBusinessSector())).collect(Collectors.joining(",")))
        .append(", created: ").append(creationDate).append(", terminated: ").append(terminationDate);

    return builder.toString();
  }
}
