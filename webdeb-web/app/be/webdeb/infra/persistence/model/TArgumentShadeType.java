package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.argument.EArgumentShade;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the t_argument_shade_type database table holding available argument shades.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.argument.EArgumentType
 */
@Entity
@Table(name = "t_argument_shade_type")
@CacheBeanTuning
public class TArgumentShadeType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TArgumentShadeType> find = new Model.Finder<>(TArgumentShadeType.class);

    @Id
    @Column(name = "id_shade", unique = true, nullable = false)
    private int idShade;

    /**
     * Get the shade id
     *
     * @return an argument shade id
     */
    public int getIdShade() {
        return idShade;
    }

    /**
     * Set the shade id
     *
     * @param idShade an argument shade id
     */
    public void setIdShade(int idShade) {
        this.idShade = idShade;
    }

    /**
     * Get the EArgumentShade corresponding to this db shade link
     *
     * @return the EArgumentShade corresponding
     */
    public EArgumentShade getEArgumentShade(){
        return EArgumentShade.value(idShade);
    }

    public static List<TArgumentShadeType> findAll() {
        return find.where().orderBy("id_shade").findList();
    }
}
