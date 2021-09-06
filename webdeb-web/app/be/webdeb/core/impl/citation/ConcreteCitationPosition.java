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
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.PositionLinkType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractContributionLink;
import be.webdeb.infra.persistence.accessor.api.CitationAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a CitationPosition representing a position link between a context contribution
 * and a citation.
 *
 * @author Martin Rouffiange
 */
public class ConcreteCitationPosition extends AbstractContributionLink<CitationFactory, CitationAccessor> implements CitationPosition, Comparable<CitationPosition> {


    protected Debate debate = null;
    protected Citation citation = null;
    protected Long subDebateId;
    protected TagCategory subDebate = null;
    private PositionLinkType linkType;

    protected TagAccessor tagAccessor;

    /**
     * Constructor
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    public ConcreteCitationPosition(CitationFactory factory, CitationAccessor accessor, TagAccessor tagAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        this.tagAccessor = tagAccessor;
        type = EContributionType.CITATION_POSITION;
    }

    @Override
    public Citation getCitation() {
        if(citation == null) {
            citation = accessor.retrieve(destinationId, false);
        }
        return citation;
    }

    @Override
    public void setCitation(Citation citation) {
        this.citation = citation;
    }

    @Override
    public Debate getDebate() {
        if(debate == null) {
            Contribution c = accessor.retrieve(originId, EContributionType.DEBATE);
            debate = c != null ? ((Debate) c) : null;
        }
        return debate;
    }

    @Override
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    @Override
    public Long getSubDebateId() {
        return subDebateId;
    }

    @Override
    public void setSubDebateId(Long subDebateId) {
        this.subDebateId = subDebateId;
    }

    @Override
    public TagCategory getSubDebate() {
        return subDebate;
    }

    @Override
    public void setSubDebate(TagCategory subDebate) {
        this.subDebate = subDebate;
    }

    @Override
    public PositionLinkType getLinkType() {
        return linkType;
    }

    @Override
    public void setLinkType(PositionLinkType linkType) {
        this.linkType = linkType;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("citation position link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getDebate() == null) {
            fieldsInError.add("debate is null");
        }

        if(getCitation() == null) {
            fieldsInError.add("citation is null");
        }

        if(linkType == null || !linkType.isValid()) {
            fieldsInError.add("link type is null or not valid");
        }
        return fieldsInError;
    }

    @Override
    public String toString() {
        return "position " + super.toString() + " in sub debate : " + subDebateId + " with type " + linkType.getType();
    }

    @Override
    public int hashCode() {
        return super.hashCode() + (subDebateId != null ? subDebateId.hashCode() : 0) + linkType.getType().hashCode();
    }

    @Override
    public int compareTo(@NotNull CitationPosition o) {
        return (int) (version - o.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof CitationPosition && hashCode() == obj.hashCode());
    }

}
