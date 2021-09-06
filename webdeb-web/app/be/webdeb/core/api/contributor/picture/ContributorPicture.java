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

package be.webdeb.core.api.contributor.picture;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.exception.FormatException;

/**
 * This interface represents a contributor's picture in the webdeb system. It holds a picture added by a contributor,
 * the author of the picture, the licence of the picture
 *
 * @author Martin Rouffiange
 */
public interface ContributorPicture {

    /**
     * Get the picture id
     *
     * @return this picture id
     */
    Long getId();

    /**
     * Set the picture id
     *
     * @param id the picture id
     */
    void setId(Long id);

    /**
     * Get the contributor id
     *
     * @return the contributor id
     */
    Long getContributorId();

    /**
     * Set the contributor id
     *
     * @param id the contributor id
     */
    void setContributorId(Long id);

    /**
     * Get the contributor that added this picture, if any
     *
     * @return the contributor, may be null
     */
    Contributor getContributor();

    /**
     * Get the source URL of this picture, if any
     *
     * @return the source URL, may be null
     */
    String getUrl();

    /**
     * Set the URL source
     *
     * @param url a source URL
     * @throws FormatException if the URL has a wrong format or this Source does not accept URL or if it is larger
     * than MAX_URL_SIZE
     */
    void setUrl(String url) throws FormatException;

    /** Get the author of the picture, if any
     *
     * @return the picture's author, may be null
     */
    String getAuthor();

    /**
     * Set the author of the picture
     *
     * @param author the picture's author
     */
    void setAuthor(String author);

    /** Get the picture extension, like png, jpg, ...
     *
     * @return the picture extension
     */
    String getExtension();

    /**
     * Set the picture extension
     *
     * @param extension the picture's file extension
     */
    void setExtension(String extension);

    /**
     * Get the source where the picture comes from
     *
     * @return the source of the picture
     */
    ContributorPictureSource getSource();

    /**
     * Set the source where the picture comes from
     *
     * @param source the source of the picture
     */
    void setSource(ContributorPictureSource source);

    /**
     * Get the licence of the picture
     *
     * @return the picture licence
     */
    PictureLicenceType getLicence();

    /**
     * Set the source where the picture comes from
     *
     * @param licence the picture licence
     */
    void setLicence(PictureLicenceType licence);

    /** Get the picture filename, it obtained by the concatenation of the picture id and the picture extension
     *
     * @return the picture filename
     */
    String getPictureFilename();
}
