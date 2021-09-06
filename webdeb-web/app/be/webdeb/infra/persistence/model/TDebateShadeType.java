package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.debate.EDebateShade;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The persistent class for the t_debate_shade_type database table holding available debate shades.
 *
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.debate.EDebateShade
 */
@Entity
@Table(name = "t_debate_shade_type")
@CacheBeanTuning
public class TDebateShadeType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TDebateShadeType> find = new Model.Finder<>(TDebateShadeType.class);

    @Id
    @Column(name = "id_shade", unique = true, nullable = false)
    private int idShade;

    /**
     * Get the shade id
     *
     * @return an debate shade id
     */
    public int getIdShade() {
        return idShade;
    }

    /**
     * Set the shade id
     *
     * @param idShade an debate shade id
     */
    public void setIdShade(int idShade) {
        this.idShade = idShade;
    }

    /**
     * Get the EDebateShade corresponding to this db shade link
     *
     * @return the EDebateShade corresponding
     */
    public EDebateShade getEDebateShade(){
        return EDebateShade.value(idShade);
    }
}
