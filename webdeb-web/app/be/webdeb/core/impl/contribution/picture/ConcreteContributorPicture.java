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

package be.webdeb.core.impl.contribution.picture;

import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.picture.ContributorPictureSource;
import be.webdeb.core.api.contributor.picture.PictureLicenceType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;

/**
 * This class implements a ContributorPicture
 *
 * @author Martin Rouffiange
 */
public class ConcreteContributorPicture implements ContributorPicture {

    private Long id;
    private String url;
    private String author;
    private String extension;

    private Long contributorId;
    private Contributor contributor = null;

    private ContributorPictureSource source;
    private PictureLicenceType licence;

    private ContributorFactory contributorFactory;

    public ConcreteContributorPicture(ContributorFactory contributorFactory) {
       this.contributorFactory = contributorFactory;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public void setExtension(String extension) {
        if (contributorFactory.getValuesHelper().isBlank(extension)) {
            this.extension = null;
        } else {
            if (extension.contains(".")) {
                this.extension = extension.substring(extension.lastIndexOf('.'));
            } else {
                this.extension = extension;
            }
        }
    }

    @Override
    public Long getContributorId() {
        return contributorId;
    }

    @Override
    public void setContributorId(Long contributorId) {
        this.contributorId = contributorId;
    }

    @Override
    public Contributor getContributor() {
        if(contributor == null){
            contributor = contributorFactory.retrieveContributor(contributorId);
        }
        return contributor;
    }

    @Override
    public ContributorPictureSource getSource() {
        return source;
    }

    @Override
    public void setSource(ContributorPictureSource source) {
        this.source = source;
    }

    @Override
    public PictureLicenceType getLicence() {
        return licence;
    }

    @Override
    public void setLicence(PictureLicenceType licence) {
        this.licence = licence;
    }

    @Override
    public String getPictureFilename() {
        return "/avatar/" + id + extension;
    }
}
