package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the tag_link database table. Holds a link between two tags.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "tag_link")
public class TagLink extends Model {

    private static final Model.Finder<Long, TagLink> find = new Model.Finder<>(TagLink.class);

    @Id
    @Column(name = "id_contribution", unique = true, nullable = false)
    private Long idContribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag_from", nullable = false)
    private Tag tagParent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tag_to", nullable = false)
    private Tag tagChild;

    // forcing updates from this object, deletions are handled at the contribution level
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "id_contribution", nullable = false, insertable = false, updatable = false)
    private Contribution contribution;

    @Transient
    @Unqueryable
    private Integer nb_contributions;

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the link id
     *
     * @return an id
     */
    public Long getIdContribution() {
        return idContribution;
    }

    /**
     * Set the link id
     *
     * @param idContribution an id
     */
    public void setIdContribution(Long idContribution) {
        this.idContribution = idContribution;
    }

    /**
     * Get the parent tag of this link
     *
     * @return a tag
     */
    public Tag getTagParent() {
        return tagParent;
    }

    /**
     * Set the parent tag of this link
     *
     * @param tagParent a tag
     */
    public void setTagParent(Tag tagParent) {
        this.tagParent = tagParent;
    }

    /**
     * Get the child tag of this link
     *
     * @return a tag
     */
    public Tag getTagChild() {
        return tagChild;
    }

    /**
     * Set child tag of this link
     *
     * @param tagChild a tag
     */
    public void setTagChild(Tag tagChild) {
        this.tagChild = tagChild;
    }

    /**
     * Get "supertype" contribution object
     *
     * @return a contribution
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Set "supertype" contribution object
     *
     * @param contribution a contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Get the number of contributions if this data is asked in a query
     *
     * @return the number of contributions
     */
    public Integer getNbContributions() {
        return nb_contributions;
    }

    /*
     * CONVENIENCE METHODS
     */

    /**
     * Get the current version of this tag link
     *
     * @return a timestamp with the latest update moment of this tag link
     */
    public Timestamp getVersion() {
        return getContribution().getVersion();
    }

    @Override
    public String toString() {
        // because of lazy load, must explicitly call getter
        return new StringBuffer("link [").append(getIdContribution()).append("] between ")
                .append(getTagParent().getContribution().getIdContribution()).append(" and ")
                .append(getTagChild().getContribution().getIdContribution())
                .append(" [version:").append(getContribution().getVersion()).append("]").toString();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a link by its id
     *
     * @param id the link id
     * @return the link corresponding to that id, null otherwise
     */
    public static TagLink findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Retrieve a link by parent and child
     *
     * @param parent the parent tag id
     * @param child the child tag id
     * @return the link corresponding to given parent and child, null otherwise
     */
    public static TagLink findByParentChild(Long parent, Long child) {
        if(parent == null || child == null){
            return null;
        }
        return find.where().eq("id_tag_from", parent).eq("id_tag_to", child).findUnique();
    }

    /**
     * Retrieve all links related with the given tag id
     *
     * @param tag a tag id
     * @return a possibly empty list of tag links related with the given tag id
     */
    public static List<TagLink> findByTag(Long tag) {
        return find.where().eq("id_tag_from", tag).or().eq("id_tag_to", tag).findList();
    }
}
