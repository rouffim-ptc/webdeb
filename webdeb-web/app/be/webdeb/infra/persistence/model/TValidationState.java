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
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the t_validation_state database table, used to specify contribution validation state.
 *
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.contribution.EValidationState
 */
@Entity
@CacheBeanTuning
@Table(name = "t_validation_state")
public class TValidationState extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TValidationState> find = new Model.Finder<>(TValidationState.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idValidationState;

    @OneToMany(mappedBy = "validated", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Unqueryable
    private List<Contribution> contributions;

    /**
     * Get the validation state id
     *
     * @return an id
     */
    public int getIdValidationState() {
        return this.idValidationState;
    }

    /**
     * Set the validation state id
     *
     * @param idValidationState an id
     */
    public void setIdValidationState(int idValidationState) {
        this.idValidationState = idValidationState;
    }

    /**
     * Get the list of contributions of this validation state
     *
     * @return a (possibly empty) list of contributions
     */
    public List<Contribution> getContributions() {
        return contributions != null ? contributions : new ArrayList<>();
    }

    /**
     * Set the list of contribution of this validation state
     *
     * @param contributions a list of contributions
     */
    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a validation state by its id
     *
     * @param id an id
     * @return the validation state corresponding to the given id, null if not found
     */
    public static TValidationState findById(Integer id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

}
