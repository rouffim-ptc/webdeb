package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;

/**
 * The persistent class for tag's rewording names database table. Tag names may have multiple spellings (depending
 * on the language), so they are externalized in a dedicated table with the 2-char ISO code corresponding to the language.
 *
 * It could be multiple rewording names in the same language
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "tag_rewording_i18names")
public class TagRewordingI18name extends Model {

    @Id
    @Column(name = "id_tag_rewording", insertable = false, updatable = false, unique = true, nullable = false)
    private Long idRewordingWord;

    @Column(name = "id_language", nullable = false)
    private String lang;

    @ManyToOne
    @JoinColumn(name = "id_contribution", nullable = false)
    @Unqueryable
    private Tag tag;

    @Column(name = "name", nullable = false)
    private String name;


    /**
     * Default constructor. Create tag name holder for given tag in given language.
     *
     * @param tag a tag
     * @param lang a 2-char ISO code of the title's language
     * @param name the String representing the tag name
     */
    public TagRewordingI18name(Tag tag, String lang, String name) {
        idRewordingWord = 0L;
        this.lang = lang;
        this.name = name;
        this.tag = tag;
    }

    /*
     * GETTERS AND SETTERS
     */

    /**
     * Get the rewording name id
     *
     * @return an id
     */
    public Long getIdRewordingWord() {
        return idRewordingWord;
    }

    /**
     * Set the rewording name id
     *
     * @param idRewordingWord an id of an existing rewording name
     */
    public void setIdRewordingWord(Long idRewordingWord) {
        this.idRewordingWord = idRewordingWord;
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
        return getIdRewordingWord() + " " + getName();
    }

}
