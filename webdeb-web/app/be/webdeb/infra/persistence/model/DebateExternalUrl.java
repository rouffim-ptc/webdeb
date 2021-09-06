package be.webdeb.infra.persistence.model;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the debate_external_url database table. Holds external url links for a debate.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "debate_external_url")
public class DebateExternalUrl extends WebdebModel {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    private static final Model.Finder<Long, DebateExternalUrl> find = new Model.Finder<>(DebateExternalUrl.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_url", unique = true, nullable = false)
    private Long idUrl;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "alias")
    private String alias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_debate", nullable = false)
    private Debate debate;

    public DebateExternalUrl(Long idUrl, String url, String alias, Debate debate) {
        this.idUrl = idUrl;
        this.url = url;
        this.alias = alias;
        this.debate = debate;
    }

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the external url id
     *
     * @return an id
     */
    public Long getIdUrl() {
        return idUrl;
    }

    /**
     * Set the external url id
     *
     * @param idUrl an id
     */
    public void setIdUrl(Long idUrl) {
        this.idUrl = idUrl;
    }

    /**
     * Get the external url
     *
     * @return an url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the external url
     *
     * @param url an url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the alias of the url
     *
     * @return the url alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Set the alias of the url
     *
     * @param alias the url alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Get the debate that is linked to this external url
     *
     * @return a debate
     */
    public Debate getDebate() {
        return debate;
    }

    /**
     * Set the debate that is linked to this external url
     *
     * @param debate a debate
     */
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    @Override
    public String toString() {
        return url + (alias != null && !alias.isEmpty() ? " (" + alias + ")" : "");
    }

    /*
     * QUERIES
     */

    /**
     * Get the list of external url for given url and debate id
     *
     * @param url an external url
     * @param debateId a debate id
     * @return a possibly empty list of external url
     */
    public static List<DebateExternalUrl> findByUrlAndDebateId(String url, Long debateId){
        logger.debug(find.where().eq("url", url).eq("id_debate", debateId).findList() + "//");
        return find.where().eq("url", url).eq("id_debate", debateId).findList();
    }
}
