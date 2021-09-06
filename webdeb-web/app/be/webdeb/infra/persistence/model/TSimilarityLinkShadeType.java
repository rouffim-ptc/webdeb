package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the t_similarity_link_shade_type database table. It represents the shade in a 
 * similarity link between contributions (like translations, oppositions, ...)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see ESimilarityLinkShade
 */
@Entity
@CacheBeanTuning
@Table(name = "t_similarity_link_shade_type")
public class TSimilarityLinkShadeType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TSimilarityLinkShadeType> find = new Model.Finder<>(TSimilarityLinkShadeType.class);

    @Id
    @Column(name = "id_shade", unique = true, nullable = false)
    private int idShade;

    /**
     * Get the similarity link shade id
     *
     * @return a similarity link shade id
     */
    public int getIdShade() {
        return idShade;
    }

    /**
     * Set the similarity link shade id
     *
     * @param idShade a similarity link shade id
     */
    public void setIdShade(int idShade) {
        this.idShade = idShade;
    }

    /**
     * Get the ESimilarityLinkShade corresponding to this db shade link
     *
     * @return the ESimilarityLinkShade corresponding
     */
    public ESimilarityLinkShade getEShade(){
        return ESimilarityLinkShade.value(idShade);
    }
}
