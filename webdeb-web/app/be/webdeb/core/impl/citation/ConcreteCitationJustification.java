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

package be.webdeb.core.impl.citation;

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractJustificationLink;
import be.webdeb.infra.persistence.accessor.api.CitationAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a CitationJustification representing a justification link between a context contribution
 * and a citation.
 *
 * @author Martin Rouffiange
 */
public class ConcreteCitationJustification extends AbstractJustificationLink<CitationFactory, CitationAccessor> implements CitationJustification, Comparable<CitationJustification> {

    private Citation citation = null;

    /**
     * Default constructor.
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    ConcreteCitationJustification (CitationFactory factory, CitationAccessor accessor, TagAccessor tagAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, tagAccessor, contributorFactory);
        type = EContributionType.CITATION_JUSTIFICATION;
    }

    @Override
    public Citation getCitation() {
        if(citation == null) {
            citation = accessor.retrieve(destinationId, false);
        }
        return citation;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("citation justification link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getCitation() == null) {
            fieldsInError.add("citation is null");
        }

        return fieldsInError;
    }

    @Override
    public String toString() {
        return "citation " + super.toString();
    }

    @Override
    public int compareTo(@NotNull CitationJustification o) {
        return (int) (version - o.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof CitationJustification && hashCode() == obj.hashCode());
    }

}
