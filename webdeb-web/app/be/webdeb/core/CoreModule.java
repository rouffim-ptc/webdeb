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

package be.webdeb.core;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.impl.actor.ConcreteActorFactory;
import be.webdeb.core.impl.argument.ConcreteArgumentFactory;
import be.webdeb.core.impl.contributor.ConcreteContributorFactory;
import be.webdeb.core.impl.debate.ConcreteDebateFactory;
import be.webdeb.core.impl.citation.ConcreteCitationFactory;
import be.webdeb.core.impl.tag.ConcreteTagFactory;
import be.webdeb.core.impl.project.ConcreteProjectFactory;
import be.webdeb.core.impl.text.ConcreteTextFactory;
import com.google.inject.AbstractModule;

/**
 * Dependency injection module for core factories and helpers, ie
 * <ul>
 *   <li>contributor factory in charge of contributors and groups</li>
 *   <li>text factory in charge of texts (with related predefined types), external texts, languages, sources, etc.</li>
 *   <li>actor factory in charge of actors, affiliations, etc.</li>
 *   <li>argument factory in charge of arguments, similarity, justification, etc.</li>
 *   <li>citation factory in charge of excerpts, external excerpts, tweets, etc.</li>
 *   <li>debate factory in charge of debates</li>
 *   <li>tag factory in charge of tag, tag links, etc.</li>
 *   <li>project factory in charge of project, project group and project subgroup</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CoreModule extends AbstractModule {

  @Override
  protected void configure() {
    // object factories
    bind(ContributorFactory.class).to(ConcreteContributorFactory.class);
    bind(TextFactory.class).to(ConcreteTextFactory.class);
    bind(ActorFactory.class).to(ConcreteActorFactory.class);
    bind(ArgumentFactory.class).to(ConcreteArgumentFactory.class);
    bind(CitationFactory.class).to(ConcreteCitationFactory.class);
    bind(DebateFactory.class).to(ConcreteDebateFactory.class);
    bind(TagFactory.class).to(ConcreteTagFactory.class);
    bind(ProjectFactory.class).to(ConcreteProjectFactory.class);
  }
}
