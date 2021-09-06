package be.webdeb.infra.persistence.model;

import be.webdeb.core.api.tag.ETagType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the t_tag_type database table, holding predefined values for types of tag,
 *
 * @author Martin Rouffiange
 * @see ETagType
 */
@Entity
@CacheBeanTuning
@Table(name = "t_tag_type")
public class TTagType extends TechnicalTable {

    /**
     * Finder to access predefined values
     */
    public static final Model.Finder<Integer, TTagType> find = new Model.Finder<>(TTagType.class);

    @Id
    @Column(name = "id_type", unique = true, nullable = false)
    private int idType;

    @OneToMany(mappedBy = "tagtype", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Unqueryable
    private List<Tag> tags;

    /**
     * Get the tag type id
     *
     * @return an id
     */
    public int getIdType() {
        return this.idType;
    }

    /**
     * Set the tag type id
     *
     * @param idType an id
     */
    public void setIdType(int idType) {
        this.idType = idType;
    }

    /**
     * Get the list of tags with this type
     *
     * @return a (possibly empty) list of tags
     */
    public List<Tag> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    /**
     * Set the list of tags with this type
     *
     * @param tags a list of tags
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Get the ETagType corresponding to this db tag type
     *
     * @return the ETagType corresponding
     */
    public ETagType getEType(){
        return ETagType.value(idType);
    }
}
