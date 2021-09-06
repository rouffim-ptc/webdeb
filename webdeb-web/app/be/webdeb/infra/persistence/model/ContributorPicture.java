/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the contributor's pictures database table. Represents picture imported by an user and that
 * recovery the licence, the author and the source of the image / picture
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "contributor_picture")
@Unqueryable
public class ContributorPicture extends Model {

    private static final org.slf4j.Logger logger = play.Logger.underlying();
    private static final Model.Finder<Long, ContributorPicture> find = new Model.Finder<>(ContributorPicture.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_picture", unique = true, nullable = false)
    private Long idPicture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contributor")
    private Contributor contributor;

    @Column(name = "url")
    private String url;

    @Column(name = "author")
    private String author;

    @Column(name = "extension")
    private String extension;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_licence_type")
    private TPictureLicenceType licenceType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_source")
    private TContributorPictureSource source;

    /**
     * Get the id of this picture
     *
     * @return the id
     */
    public Long getIdPicture() {
        return idPicture;
    }

    /**
     * Set the id of this picture
     *
     * @param idPicture a unique id
     */
    public void setIdPicture(Long idPicture) {
        this.idPicture = idPicture;
    }

    /**
     * Get the contributor that add the picture. Can be nul.
     *
     * @return a possibly null contributor
     */
    public Contributor getContributor() {
        return contributor;
    }

    /**
     * Set the contributor that add the picture
     *
     * @param contributor the contributor that add the picture
     */
    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    /**
     * Get the url of the picture, if the picture come from internet
     *
     * @return the url of the picture
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url of the picture, if the picture come from internet
     *
     * @param url the url of the picture
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the author of the picture.
     *
     * @return the author of the picture
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author of the picture
     *
     * @param author the author of the picture
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the extension of the file of the picture. The picture filename will be its id + this extension
     *
     * @return the picture file extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Set the extension of the file of the picture
     *
     * @param extension the picture file extension
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Get the licence type of this picture.
     *
     * @return the licence type
     */
    public TPictureLicenceType getLicenceType() {
        return licenceType;
    }

    /**
     * Set the licence type of this picture.
     *
     * @param licenceType the licence type
     */
    public void setLicenceType(TPictureLicenceType licenceType) {
        this.licenceType = licenceType;
    }

    /**
     * Get the source of this contributor picture
     *
     * @return the picture source
     */
    public TContributorPictureSource getSource() {
        return source;
    }

    /**
     * Set the source of this contributor picture
     *
     * @param source the picture source
     */
    public void setSource(TContributorPictureSource source) {
        this.source = source;
    }

    /**
     * Get the filename of this contribution picture (picture.id + picture.extension)
     *
     * @return the picture filename
     */
    public String getFilename() {
        return getIdPicture() + "." + getExtension();
    }

    /*
     * QUERIES
     */

    /**
     * Retrieve a contributor picture by its id
     *
     * @param id an id
     * @return the contributor picture corresponding to the given id, null if not found
     */
    public static ContributorPicture findById(Long id) {
        return id == null || id == -1L ? null : find.byId(id);
    }

    /**
     * Find all contributor pictures objects that exists in database linked to given contributor
     *
     * @param contributor a contributor id
     * @return a list of contributor pictures
     */
    public static List<ContributorPicture> findAllPicturesByContributor(Long contributor) {
        return find.where().eq("id_contributor", contributor).findList();
    }
}
