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

package be.webdeb.core.impl.citation;

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.CitationAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * This class implements an factory for citations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteCitationFactory extends AbstractContributionFactory<CitationAccessor> implements CitationFactory {

    @Inject
    private ActorAccessor actorAccessor;

    @Inject
    private TextFactory textFactory;

    @Inject
    private TagAccessor tagAccessor;

    @Override
    public Citation retrieve(Long id) {
        return id != null && id != -1L ? accessor.retrieve(id, false) : null;
    }

    @Override
    public Citation retrieveWithHit(Long id) {
        return id != null && id != -1L ? accessor.retrieve(id, true) : null;
    }

    @Override
    public CitationJustification retrieveJustificationLink(Long id) {
        return accessor.retrieveJustificationLink(id);
    }

    @Override
    public CitationPosition retrievePositionLink(Long id) {
        return accessor.retrievePositionLink(id);
    }

    @Override
    public Citation getCitation() {
        return new ConcreteCitation(this, accessor, textFactory, actorAccessor, contributorFactory);
    }

    @Override
    public CitationJustification getCitationJustificationLink() {
        return new ConcreteCitationJustification(this, accessor, tagAccessor, contributorFactory);
    }

    @Override
    public CitationPosition getCitationPositionLink() {
        return new ConcreteCitationPosition(this, accessor, tagAccessor, contributorFactory);
    }

    @Override
    public List<Citation> searchCitationByType(SearchContainer query) {
        return accessor.searchCitationByType(query);
    }

    @Override
    public List<CitationJustification> findCitationLinks(Long context, Long subContext, Long category, Long superArgument, int shade) {
        return accessor.findCitationLinks(context, subContext, category, superArgument, shade);
    }

    @Override
    public boolean citationPositionLinkAlreadyExists(Long debateId, Long subDebateId, Long citationId, int shade) {
        return accessor.citationPositionLinkAlreadyExists(debateId, subDebateId, citationId, shade);
    }

    @Override
    public Citation random() {
        return accessor.random();
    }

    @Override
    public Citation getSuggestionCitation(Long idContribution) {
        return accessor.getSuggestionCitation(idContribution);
    }
}
