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
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the t_copyrightfree_source database table, defining the web domain name where text are commonly free to use
 *
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "t_copyrightfree_source")
@CacheBeanTuning
@Unqueryable
public class TCopyrightfreeSource extends Model {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_type", unique = true, nullable = false)
  private int idCopyrightfreeSource;

  @Column(name = "domain_name", nullable = false)
  private String domainName;

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TCopyrightfreeSource> find = new Model.Finder<>(TCopyrightfreeSource.class);


  /*
   * GETTERS / SETTERS
   */

  /**
   * Get this source id
   *
   * @return an id
   */
  public int getIdCopyrightfreeSource() {
    return idCopyrightfreeSource;
  }

  /**
   * Set the source id
   *
   * @param idCopyrightfreeSource a source id
   */
  public void setIdCopyrightfreeSource(int idCopyrightfreeSource) {
    this.idCopyrightfreeSource = idCopyrightfreeSource;
  }

  /**
   * Get the identifying domain name of this source
   *
   * @return the identifying name
   */
  public String getDomainName() {
    return domainName;
  }

  /**
   * Get the identifying domain name of this source
   *
   * @param domainName the identifying name
   */
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

    /*
   * QUERIES
   */

  /**
   * Find a free copyright source by its id
   *
   * @param id a free copyright source id
   * @return the TCopyrightfreeSource if it has been found, null otherwise
   */
  public static TCopyrightfreeSource findById(int id) {
    if (id == -1) {
      return null;
    }
    return find.where().eq("id_type", id).findUnique();
  }

  /**
   * Get all free sources
   *
   * @return a list of TCopyrightfreeSource
   */
  public static List<TCopyrightfreeSource> getAll() {
    return find.all();
  }

  /**
   * Get a source by its name
   *
   * @param name the source name to look for
   * @return the TCopyrightfreeSource where domainName=name, or null if not found
   */
  public static TCopyrightfreeSource findByName(String name) {
    return find.where().eq("domainName", name).findUnique();
  }

  /*
   * CONVENIENCE METHODS
   */

  @Override
  public String toString() {
    return "Free source " + idCopyrightfreeSource + ", domain name : " + domainName;
  }
}
