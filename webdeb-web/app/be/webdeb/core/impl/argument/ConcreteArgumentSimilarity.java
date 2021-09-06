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
package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.argument.ArgumentSimilarity;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractSimilarityLink;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements an ArgumentSimilarity representing a similarity link between two arguments
 *
 * @author Martin Rouffiange
 */
public class ConcreteArgumentSimilarity extends AbstractSimilarityLink<ArgumentFactory, ArgumentAccessor> implements ArgumentSimilarity {

    private Argument origin = null;
    private Argument destination = null;

    /**
     * Default constructor.
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    ConcreteArgumentSimilarity(ArgumentFactory factory, ArgumentAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        type = EContributionType.ARGUMENT_SIMILARITY;
    }

    @Override
    public Argument getOrigin() {
        if(origin == null) {
            origin = accessor.retrieve(originId, false);
        }
        return origin;
    }

    @Override
    public void setOrigin(Argument argument) {
        origin = argument;
    }

    @Override
    public Argument getDestination(){
        if(destination == null) {
            destination = accessor.retrieve(destinationId, false);
        }
        return destination;
    }

    @Override
    public void setDestination(Argument argument) {
        destination = argument;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("argument similarity link contains error " + errors.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if(getOrigin() == null) {
            fieldsInError.add("origin argument is null");
        }

        if(getDestination() == null) {
            fieldsInError.add("destination argument is null");
        }

        return fieldsInError;
    }

    @Override
    public String toString() {
        return "argument " + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ArgumentSimilarity && hashCode() == obj.hashCode();
    }

}