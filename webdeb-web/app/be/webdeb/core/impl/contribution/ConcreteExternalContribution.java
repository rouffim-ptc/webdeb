/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see COPYING file).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.TextAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a Contribution in the webdeb system. It implements a set of common
 * methods to handle any type of contributions.
 *
 * @author Martin Rouffiange
 */
public class ConcreteExternalContribution extends AbstractContribution<TextFactory, TextAccessor> implements ExternalContribution {

    protected Long internalContributionId;
    protected String sourceUrl;
    protected int sourceId = -1;
    protected Language language;
    protected String title;

    /**
     * Abstract constructor
     *
     * @param factory a contribution factory
     * @param accessor a contribution accessor
     */
    public ConcreteExternalContribution(TextFactory factory, TextAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    @Override
    public void setSourceUrl(String url) {
        this.sourceUrl = url;
    }

    @Override
    public int getSourceId() {
        return sourceId;
    }

    @Override
    public void setSourceId(Integer source) {
        if(source != null && EExternalSource.value(source) != null){
            this.sourceId = source;
        }
    }

    @Override
    public String getSourceName() {
        if(EExternalSource.value(sourceId) != null){
            return EExternalSource.value(sourceId).name();
        }
        return "";
    }

    @Override
    public Long getInternalContribution() {
        return internalContributionId;
    }

    @Override
    public void setInternalContribution(Long id) {
        this.internalContributionId = id;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public void setContributionType(ContributionType type) {
        this.type = type.getEType();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("citation contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.CITATION_ERROR, String.join(",", isValid.toString()));
        }
        return accessor.save(this, currentGroup, contributor);
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();
        if (sourceUrl == null || sourceUrl.equals("")) {
            fieldsInError.add("url is empty");
        }
        return fieldsInError;
    }

    @Override
    public int hashCode() {
        return 47 * (id != -1L ? id.hashCode() : 83);
    }

    @Override
    public String toString() {
        return "AbstractExternalContribution{" +
                "internalContributionId=" + internalContributionId +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", language='" + language + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
