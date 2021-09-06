package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contribution.link.EPositionLinkShade;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the t_position_link_shade_type database table. It represents the shade in a debate
 * position link
 *
 * @author Martin Rouffiange
 * @see EPositionLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "t_position_link_shade_type")
public class TPositionLinkShadeType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TPositionLinkShadeType> find = new Model.Finder<>(TPositionLinkShadeType.class);

    @Id
    @Column(name = "id_shade", unique = true, nullable = false)
    private int idShade;

    /**
     * Get the position link shade id
     *
     * @return a position link shade id
     */
    public int getIdShade() {
        return idShade;
    }

    /**
     * Set the position link shade id
     *
     * @param idShade a position link shade id
     */
    public void setIdShade(int idShade) {
        this.idShade = idShade;
    }

    /**
     * Get the EPositionLinkShade corresponding to this db shade link
     *
     * @return the EPositionLinkShade corresponding
     */
    public EPositionLinkShade getEShade(){
        return EPositionLinkShade.value(idShade);
    }
}
