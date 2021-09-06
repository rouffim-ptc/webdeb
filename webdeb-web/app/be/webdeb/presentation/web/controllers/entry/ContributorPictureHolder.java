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

package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;

/**
 * This class holds concrete values of a Contributor Picture.
 * Except by using a constructor, no value can be edited outside of this package or by subclassing.
 *
 * @author Martin Rouffiange
 */
public class ContributorPictureHolder {

    // custom logger
    static final org.slf4j.Logger logger = play.Logger.underlying();
    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);
    @Inject
    protected FileSystem files = Play.current().injector().instanceOf(FileSystem.class);

    protected Long id;
    protected String author;
    protected String url;
    protected String extension;
    protected String filename;

    public ContributorPictureHolder() {
        this(null);
    }

    public ContributorPictureHolder(ContributorPicture picture) {
        if(picture != null) {
            this.id = picture.getId();
            this.author = picture.getAuthor();
            this.url = picture.getUrl();
            this.extension = picture.getExtension();
            this.filename = picture.getPictureFilename();
        } else {
            id = -1L;
            url = null;
            filename = null;
        }
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getExtension() {
        return extension;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isDefined(){
        return !values.isBlank(id) && !values.isBlank(filename);
    }

    @Override
    public String toString() {
        return "ContributorPictureHolder{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", extension='" + extension + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
