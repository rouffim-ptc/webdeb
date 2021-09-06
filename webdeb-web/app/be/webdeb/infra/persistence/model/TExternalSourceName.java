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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;


/**
 * The persistent class for the t_external_source_name database table, holding external services names that can
 * collaborate with Webdeb.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "t_external_source_name")
public class TExternalSourceName extends Model {

    private static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Finder<Integer, TExternalSourceName> find = new Finder<>(TExternalSourceName.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idSource;

    @Column(name = "name", length = 255)
    private String name;

    @OneToMany(mappedBy = "externalSource", fetch = FetchType.LAZY)
    @Unqueryable
    private List<ExternalContribution> externalContributions;

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get this source id
     *
     * @return an id
     */
    public int getIdSource() {
        return idSource;
    }

    /**
     * Set this source id
     *
     * @param idSource an id
     */
    public void setIdSource(int idSource) {
        this.idSource = idSource;
    }

    /**
     * Get this source name
     *
     * @return a name
     */
    public String getName() {
        return name;
    }

    /**
     * Set this source name
     *
     * @param name a name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the list of external contributions that come from this source
     *
     * @return a list of external contributions
     */
    public List<ExternalContribution> getExternalContributions() {
        return externalContributions;
    }

    /**
     * Set the list of external contributions that come from this source
     *
     * @param externalContributions a list of external contributions
     */
    public void setExternalContributions(List<ExternalContribution> externalContributions) {
        this.externalContributions = externalContributions;
    }

    @Override
    public String toString() {
        return "source: [" + getIdSource() + "] " + getName();
    }

    /*
     * QUERIES
     */

    /**
     * Find an external source by its id
     *
     * @param id a source id
     * @return found external source for given id, or null if not found
     */
    public static TExternalSourceName findById(int id) {
        return find.byId(id);
    }

    /**
     * Get an external source by its name
     *
     * @param name the source name to look for
     * @return the TExternalSourceName where name=name, or null if not found
     */
    public static TExternalSourceName findByName(String name) {
        return find.where().eq("name", name).findUnique();
    }

}
