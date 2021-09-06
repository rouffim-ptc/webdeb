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

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;

import java.util.List;
import java.util.Map;

public abstract class AbstractContextContribution<T extends ContributionFactory,V extends ContributionAccessor> extends AbstractTextualContribution<T, V> implements ContextContribution {

    protected List<Argument> arguments = null;
    protected List<Citation> allCitations = null;
    protected List<Citation> citationFromJustifications = null;
    protected List<Text> texts = null;
    protected List<DebateTag> tagDebates = null;
    protected List<TagCategory> categories = null;
    protected List<ContextHasCategory> contextHasCategories = null;
    protected List<CitationJustification> citationJustifications = null;
    protected List<ArgumentJustification> argumentJustifications = null;

    /**
     * Constructor
     *
     * @param factory       a ContributionFactory the factory to construct concrete instances
     * @param accessor      a ContributionAccessor the accessor to retrieve and persist concrete Contribution
     * @param actorAccessor an ActorAccessor to retrieve bound actors
     */
    public AbstractContextContribution(T factory, V accessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, actorAccessor, contributorFactory);
    }

    @Override
    public Long getCurrentSuperContextId() {
        return null;
    }

    @Override
    public Long getSuperContextId() {
        return getCurrentSuperContextId() == null ? id : getCurrentSuperContextId();
    }

    @Override
    public Long getSubContextId() {
        return getCurrentSuperContextId() == null ? null : id;
    }

    @Override
    public List<DebateTag> getTagDebates(Long contributor, int group) {
        if(tagDebates == null && type == EContributionType.DEBATE && ((Debate) this).getEType() == EDebateType.NORMAL && ((Debate) this).isMultiple()) {

            tagDebates = accessor.getTagDebates(id, contributor, group);

        }
        return tagDebates;
    }

    @Override
    public DebateTag getTagDebate(Long id) {
        return accessor.getTagDebate(this.id, id);
    }

    @Override
    public List<TagCategory> getCategories() {
        if(categories == null){

            categories = accessor.getCategories(getCurrentSuperContextId() != null ? getCurrentSuperContextId() : id);
        }
        return categories;
    }

    @Override
    public List<ContextHasCategory> getContextCategories() {
        if(contextHasCategories == null){

            contextHasCategories = accessor.getContextCategories(getCurrentSuperContextId() != null ? getCurrentSuperContextId() : id);
        }
        return contextHasCategories;
    }

    @Override
    public List<Argument> getAllArguments() {
        return null;
    }

    @Override
    public List<Citation> getAllCitations() {
        if(allCitations == null){
            allCitations = accessor.getAllCitations(id);
        }
        return allCitations;
    }

    @Override
    public List<Citation> getAllCitations(SearchContainer query) {
        return accessor.getAllCitations(query);
    }

    @Override
    public List<Citation> getCitationsFromJustifications() {
        if(citationFromJustifications == null){
            citationFromJustifications = accessor.getCitationsFromJustifications(id, null);
        }
        return citationFromJustifications;
    }

    @Override
    public List<Citation> getCitationsFromJustifications(Long category) {
        return accessor.getCitationsFromJustifications(id, category);
    }

    @Override
    public List<Text> getTextsCitations(Long contributor, int group) {
        if(texts == null){
            texts = accessor.getTextsCitations(id, contributor, group);
        }
        return texts;
    }

    @Override
    public List<CitationJustification> getActorCitationJustifications(Long actor) {
        return accessor.getActorCitationJustifications(id, actor);
    }

    @Override
    public List<CitationJustification> getTextCitationJustifications(Long text) {
        return accessor.getTextCitationJustifications(id, text);
    }

    @Override
    public List<CitationPosition> getTextCitationPositions(Long text) {
        return accessor.getTextCitationPositions(id, text);
    }

    @Override
    public List<ArgumentJustification> getAllArgumentJustificationLinks() {
        if(argumentJustifications == null){
            argumentJustifications = accessor.getArgumentJustificationLinks(id);
        }
        return argumentJustifications;
    }

    @Override
    public List<CitationJustification> getAllCitationJustificationLinks() {
        if(citationJustifications == null){
            citationJustifications = accessor.getCitationJustificationLinks(id);
        }
        return citationJustifications;
    }

    @Override
    public List<ArgumentJustification> getArgumentJustificationLinks(Long category, Long superArgument, Integer shade) {
        return accessor.getArgumentJustificationLinks(getCurrentSuperContextId() != null ? getCurrentSuperContextId() : id, getCurrentSuperContextId() != null ? id : null, category, superArgument, shade);
    }

    @Override
    public List<CitationJustification> getCitationJustificationLinks(Long category, Long superArgument, Integer shade) {
        return accessor.getCitationJustificationLinks(getCurrentSuperContextId() != null ? getCurrentSuperContextId() : id, getCurrentSuperContextId() != null ? id : null, category, superArgument, shade);
    }
}
