package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The persistent class for tag's names database table. Tag names may have multiple spellings (depending
 * on the language), so they are externalized in a dedicated table with the 2-char ISO code corresponding to the language.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "tag_i18names")
public class TagI18name extends Model {

    @EmbeddedId
    private TagI18name.TagPK id;

    @ManyToOne
    @JoinColumn(name = "id_contribution", nullable = false)
    @Unqueryable
    private Tag tag;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type", nullable = false)
    private TTagType tagtype;


    /**
     * Default constructor. Create tag name holder for given tag in given language.
     *
     * @param tag a tag
     * @param lang a 2-char ISO code of the title's language
     * @param name the String representing the tag name
     */
    public TagI18name(Tag tag, String lang, String name) {
        TagI18name.TagPK pk = new TagI18name.TagPK();
        pk.setIdContribution(tag.getIdContribution());
        pk.setLang(lang);
        this.name = name;
        setId(pk);
        this.tag = tag;
        this.tagtype = tag.getTagtype();
    }

    /*
     * GETTERS AND SETTERS
     */

    /**
     * Get the complex id for this tag name
     *
     * @return a complex id (concatenation of id_contribution and lang)
     */
    public TagI18name.TagPK getId() {
        return this.id;
    }

    /**
     * Set the complex id for this tag name
     *
     * @param id a complex id object
     */
    public void setId(TagI18name.TagPK id) {
        this.id = id;
    }

    /**
     * Get the current language code for this name
     *
     * @return a two-char iso-639-1 language code
     */
    public String getLang() {
        return id.getLang();
    }

    /**
     * Set the current language code for this name
     *
     * @param lang a two-char iso-639-1 language code
     */
    public void setLang(String lang) {
        id.setLang(lang);
    }

    /**
     * Get the tag name
     *
     * @return the name of the tag
     */
    public String getName() {
        return name;
    }

    /**
     * Set the tag's name
     *
     * @param name the name of the tag
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the tag linked to this name
     *
     * @return a tag
     */
    public Tag getTag() {
        return this.tag;
    }

    /**
     * Set the tag linked to this name
     *
     * @param tag a tag
     */
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return getId() + " " + getName();
    }

    /**
     * The primary key class for the tag multiple names database table.
     *
     * @author Martin Rouffiange
     */
    @Embeddable
    public static class TagPK extends Model {

        @Column(name = "id_contribution", insertable = false, updatable = false, unique = true, nullable = false)
        private Long idContribution;

        @Column(name = "id_language", insertable = false, updatable = false, unique = true, nullable = false)
        private String lang;

        /**
         * Get the contribution (tag) id
         *
         * @return an id
         */
        public Long getIdContribution() {
            return idContribution;
        }

        /**
         * Set the contribution (tag) id
         *
         * @param idContribution an id of an existing tag
         */
        public void setIdContribution(Long idContribution) {
            this.idContribution = idContribution;
        }

        /**
         * Get the language associated to this name
         *
         * @return a two-char iso-639-1 language code
         */
        public String getLang() {
            return this.lang;
        }

        /**
         * Set the language associated to this name
         *
         * @param lang a two-char iso-639-1 language code
         */
        public void setLang(String lang) {
            this.lang = lang;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TagI18name.TagPK)) {
                return false;
            }
            TagI18name.TagPK castOther = (TagI18name.TagPK) other;
            return idContribution.equals(castOther.idContribution) && lang.equals(castOther.lang);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 17;
            hash = hash * prime + this.idContribution.hashCode();
            return hash * prime + this.lang.hashCode();
        }
    }
}
