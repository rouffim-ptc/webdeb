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

import be.webdeb.core.api.contribution.link.ContextHasSubDebate;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.core.impl.contribution.link.ConcreteContextHasSubDebate;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements an factory for debates, debate types and links.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteDebateFactory extends AbstractContributionFactory<DebateAccessor> implements DebateFactory {

    // key is shade id
    private Map<Integer, DebateShade> debateShades = null;
    
    @Inject
    private ActorAccessor actorAccessor;

    @Inject
    private ArgumentAccessor argumentAccessor;

    @Inject
    private TagAccessor tagAccessor;

    @Override
    public Debate retrieve(Long id) {
        return accessor.retrieve(id, false);
    }

    @Override
    public Debate retrieveWithHit(Long id) {
        return accessor.retrieve(id, true);
    }

    @Override
    public DebateSimilarity retrieveSimilarityLink(Long id) {
        return accessor.retrieveSimilarityLink(id);
    }

    @Override
    public ContextHasSubDebate retrieveContextHasSubDebate(Long id) {
        return accessor.retrieveContextHasSubDebate(id);
    }

    @Override
    public DebateSimple getDebateSimple() {
        return new ConcreteDebateSimple(this, accessor, actorAccessor, argumentAccessor, contributorFactory);
    }

    @Override
    public DebateTag getDebateTag() {
        return new ConcreteDebateTag(this, accessor, actorAccessor, tagAccessor, contributorFactory);
    }

    @Override
    public DebateExternalUrl getDebateExternalUrl() {
        return new ConcreteDebateExternalUrl();
    }

    @Override
    public DebateExternalUrl getDebateExternalUrl(Long id, String url, String alias) {
        return new ConcreteDebateExternalUrl(id, url, alias);
    }

    @Override
    public DebateSimilarity getDebateSimilarityLink() {
        return new ConcreteDebateSimilarity(this, accessor, contributorFactory);
    }

    @Override
    public DebateHasTagDebate getDebateHasTagDebate() {
        return new ConcreteDebateHasTagDebate(this, accessor, contributorFactory);
    }

    @Override
    public ContextHasSubDebate getContextHasSubDebate() {
        return new ConcreteContextHasSubDebate(this, accessor, contributorFactory);
    }

    @Override
    public DebateHasText getDebateHasText() {
        return new ConcreteDebateHasText(this, accessor, contributorFactory);
    }

    @Override
    public DebateShade createDebateShade(int shade, Map<String, String> i18names) {
        return new ConcreteDebateShade(shade, i18names);
    }

    @Override
    public DebateShade getDebateShade(int shade) throws FormatException {
        if (debateShades == null) {
            getDebateShades();
        }
        if (!debateShades.containsKey(shade)) {
            throw new FormatException(FormatException.Key.UNKNOWN_DEBATE_SHADE_TYPE, String.valueOf(shade));
        }
        return debateShades.get(shade);
    }

    @Override
    public List<DebateShade> getDebateShades() {
        if (debateShades == null) {
            debateShades = new LinkedHashMap<>();
            accessor.getDebateShades().forEach(shade -> debateShades.put(shade.getType(), shade));
        }
        return new ArrayList<>(debateShades.values());
    }

    @Override
    public List<Debate> findByTitle(String title, String lang, int fromIndex, int toIndex) {
        return accessor.findByTitle(title, lang, fromIndex, toIndex);
    }

    @Override
    public List<Debate> findByTitleAndShade(String title, Integer shade, String lang, int fromIndex, int toIndex) {
        return accessor.findByTitleAndShade(title, shade, lang, fromIndex, toIndex);
    }

    @Override
    public Debate random() {
        return accessor.random();
    }

}
