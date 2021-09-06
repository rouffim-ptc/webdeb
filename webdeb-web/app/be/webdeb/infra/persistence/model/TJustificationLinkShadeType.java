package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.argument.EArgumentShade;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the t_justification_link_shade_type database table. It represents the shade in a
 * justification link (pros / cons)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see EJustificationLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "t_justification_link_shade_type")
public class TJustificationLinkShadeType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TJustificationLinkShadeType> find = new Model.Finder<>(TJustificationLinkShadeType.class);

    @Id
    @Column(name = "id_shade", unique = true, nullable = false)
    private int idShade;

    /**
     * Get the justification link shade id
     *
     * @return a justification link shade id
     */
    public int getIdShade() {
        return idShade;
    }

    /**
     * Set the justification link shade id
     *
     * @param idShade a justification link shade id
     */
    public void setIdShade(int idShade) {
        this.idShade = idShade;
    }

    /**
     * Get the EJustificationLinkShade corresponding to this db shade link
     *
     * @return the EJustificationLinkShade corresponding
     */
    public EJustificationLinkShade getEShade(){
        return EJustificationLinkShade.value(idShade);
    }
}
