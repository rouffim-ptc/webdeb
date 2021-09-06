/*
 * WebDeb - Copyright (C) <2014-2019> <Universit� catholique de Louvain (UCL), Belgique ; Universit� de Namur (UNamur), Belgique>
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
package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractJustificationLink;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements an ArgumentJustification representing a justification link between a context contribution and
 * an argument.
 *
 * @author Martin Rouffiange
 */
public class ConcreteArgumentJustification extends AbstractJustificationLink<ArgumentFactory, ArgumentAccessor> implements ArgumentJustification, Comparable<ArgumentJustification> {

    private Argument argument = null;
    private Long debateId;
    private Debate debate = null;

    /**
     * Default constructor.
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    ConcreteArgumentJustification (ArgumentFactory factory, ArgumentAccessor accessor, TagAccessor tagAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, tagAccessor, contributorFactory);
        type = EContributionType.ARGUMENT_JUSTIFICATION;
    }

    @Override
    public Argument getArgument() {
        if(argument == null) {
            argument = accessor.retrieve(destinationId, false);
        }

        return argument;
    }

    @Override
    public Long getDebateId() {
        return debateId;
    }

    @Override
    public void setDebateId(Long debateId) {
        this.debateId = debateId;
    }

    @Override
    public Debate getDebate() {
        if(debate == null) {
            ContextContribution context = factory.retrieveContextContribution(debateId);
            debate = context != null && context.getType() == EContributionType.DEBATE ? ((Debate) context) : null;
        }
        return debate;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("argument justification link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getArgument() == null) {
            fieldsInError.add("argument is null");
        }

        if(destinationId.equals(superArgumentId)) {
            fieldsInError.add("argument is equal to super argument id");
        }

        return fieldsInError;
    }

    @Override
    public String toString() {
        return "argument " + super.toString();
    }

    @Override
    public int compareTo(@NotNull ArgumentJustification o) {
        return (int) (version - o.getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof ArgumentJustification && hashCode() == obj.hashCode());
    }

}
