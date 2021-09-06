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

package be.webdeb.core.impl.contribution.link;

import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasSubDebate;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a context has subdebate link.
 *
 * @author Martin Rouffiange
 */
public class ConcreteContextHasSubDebate extends AbstractContributionLink<DebateFactory, DebateAccessor> implements ContextHasSubDebate {

    private ContextContribution origin = null;
    private Debate destination = null;
    private Long argumentJustificationLinkId;
    private ArgumentJustification argumentJustificationLink = null;

    /**
     * Default constructor.
     *
     * @param factory the debate factory (to get and build concrete instances of objects)
     * @param accessor the debate accessor to retrieve and persist debates
     * @param contributorFactory the contributor factory to get bound contributors
     */
    public ConcreteContextHasSubDebate(DebateFactory factory, DebateAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        type = EContributionType.CONTEXT_HAS_SUBDEBATE;
    }

    @Override
    public ContextContribution getOrigin() {
        if(origin == null) {
            origin = factory.retrieveContextContribution(originId);
        }
        return origin;
    }

    @Override
    public void setOrigin(ContextContribution origin) {
        this.origin = origin;
    }

    @Override
    public Debate getDestination() {
        if(destination == null) {
            destination = factory.retrieve(destinationId);
        }
        return destination;
    }

    @Override
    public void setDestination(Debate destination) {
        this.destination = destination;
    }

    @Override
    public Long getArgumentJustificationLinkId() {
        return argumentJustificationLinkId;
    }

    @Override
    public void setArgumentJustificationLinkId(Long argumentJustificationLinkId) {
        this.argumentJustificationLinkId = argumentJustificationLinkId;
    }

    @Override
    public ArgumentJustification getArgumentJustificationLink() {
        if(argumentJustificationLink == null) {
            Contribution contribution = factory.retrieveContribution(argumentJustificationLinkId);
            argumentJustificationLink = contribution != null && contribution.getType() == EContributionType.ARGUMENT_JUSTIFICATION ? ((ArgumentJustification) contribution) : null;
        }
        return argumentJustificationLink;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("context has subdebate link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getOrigin() == null) {
            fieldsInError.add("origin context is null");
        }

        if(getDestination() == null) {
            fieldsInError.add("destination debate is null");
        }

        return fieldsInError;
    }

    @Override
    public String toString() {
        return "context has sub debate " + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ContextHasSubDebate && hashCode() == obj.hashCode();
    }
}
