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

package be.webdeb.core.impl.debate;

import be.webdeb.core.api.argument.ArgumentShaded;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.*;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.*;

/**
 * This class implements a simple DebateSimple.
 *
 * @author Martin Rouffiange
 */
public class ConcreteDebateSimple extends AbstractDebate implements DebateSimple {

    private Long argumentId;
    private ArgumentShaded argument = null;

    private ArgumentAccessor argumentAccessor;

    /**
     * Create a Debate instance
     *
     * @param factory the debate factory
     * @param accessor the debate accessor
     * @param actorAccessor an actor accessor (to retrieve/update involved actors)
     * @param argumentAccessor an argument accessor (to retrieve/update involved arguments)
     * @param contributorFactory the contributor accessor
     */
    ConcreteDebateSimple(DebateFactory factory, DebateAccessor accessor, ActorAccessor actorAccessor, ArgumentAccessor argumentAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, actorAccessor, contributorFactory);
        this.argumentAccessor = argumentAccessor;
        type = EContributionType.DEBATE;
        etype = EDebateType.NORMAL;
    }

    @Override
    public String getFullTitle() {
        return computeShadeAndTitle(shade);
    }

    @Override
    // TODO similar argument translation link type?
    public String getFullTitle(String lang) {
        return getFullTitle();
    }

    @Override
    public String getTitle(String lang) {
        return getArgument().getTitle();
    }

    @Override
    public Long getTitleContributionId() {
        return argumentId;
    }

    @Override
    public Long getArgumentId() {
        return argumentId;
    }

    @Override
    public void setArgumentId(Long id) {
        this.argumentId = id;
    }

    @Override
    public ArgumentShaded getArgument() {
        if(argument == null) {
            argument = argumentAccessor.retrieveShaded(argumentId, false);
        }
        return argument;
    }

    @Override
    public void setArgument(ArgumentShaded argument) {
        this.argument = argument;
    }

    @Override
    public List<DebateTag> getSubDebates(Long contributor, int group) {
        return super.getTagDebates(contributor, group);
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();

        if (getArgument() == null || !getArgument().isValid().isEmpty()) {
            fieldsInError.add("argument shaded is null or invalid");
        }

        if (shade == null || !shade.isValid()) {
            fieldsInError.add("debate shade is null or not valid");
        }

        return fieldsInError;
    }

    @Override
    public int hashCode() {
        return super.hashCode() +
                argumentId.hashCode() +
                shade.getType().hashCode();
    }


    @Override
    public String toString() {
        return super.toString() + " with shaded argument " + argumentId + " and shade " + shade.getEType().name();
    }

    /**
     * Compute a language-specific fully readable shade an title combined
     *
     * @param shade a debate shade
     * @return a reader-friendly representation of the shade and title of the debate
     */
    private String computeShadeAndTitle(DebateShade shade) {
        if(isValid().isEmpty()) {
            String shadeterm = factory.makeShadeReaderFriendly(
                    shade.getName(getArgument().getLanguage().getCode()),
                    getArgument().getTitle(),
                    getArgument().getLanguage().getCode());
            
            return shadeterm + getArgument().getTitle() + " ?";

        }

        return "";
    }
}
