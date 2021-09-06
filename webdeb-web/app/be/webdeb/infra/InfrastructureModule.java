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

package be.webdeb.infra;

import be.webdeb.infra.csv.CsvImporter;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.persistence.accessor.api.*;
import be.webdeb.infra.persistence.accessor.impl.*;
import be.webdeb.infra.relevance.ContributionRelevance;
import be.webdeb.infra.sitemap.SitemapGenerator;
import be.webdeb.infra.taglinker.TagLinker;
import be.webdeb.infra.ws.ml.ImageDetection;
import be.webdeb.infra.ws.nlp.RSSFeedClient;
import be.webdeb.infra.ws.nlp.RequestProxy;
import be.webdeb.infra.ws.nlp.TweetFeedClient;
import be.webdeb.infra.ws.util.WebService;
import com.google.inject.AbstractModule;

/**
 * This module registers the infrastructure-related implementations and helpers, ie,
 * <ul>
 *   <li>object mapper to transform DB objects to API (core) objects</li>
 *   <li>db accessors (contributors -with groups-, texts, arguments, excerpts, debates, actors, affiliations, projects and folders)</li>
 *   <li>file system helper</li>
 *   <li>mailing helper</li>
 *   <li>external (WDTAL) services, e.g wdtal requests, image detection, akka jobs (rss feeder, twitter and similarity
 *   services)</li>
 * </ul>
 *
 * the accessors, mapper and
 * other helpers (file system, web services, etc).
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class InfrastructureModule extends AbstractModule {

  @Override
  protected void configure() {

    // DB -> API mapper
    bind(APIObjectMapper.class).to(EbeanAPIMapper.class);

    // contribution accessors
    bind(ContributorAccessor.class).to(EbeanContributorAccessor.class);
    bind(TextAccessor.class).to(EbeanTextAccessor.class);
    bind(ActorAccessor.class).to(EbeanActorAccessor.class);
    bind(ArgumentAccessor.class).to(EbeanArgumentAccessor.class);
    bind(CitationAccessor.class).to(EbeanCitationAccessor.class);
    bind(DebateAccessor.class).to(EbeanDebateAccessor.class);
    bind(AffiliationAccessor.class).to(EbeanAffiliationAccessor.class);
    bind(TagAccessor.class).to(EbeanTagAccessor.class);
    bind(ProjectAccessor.class).to(EbeanProjectAccessor.class);

    // File system
    bind(FileSystem.class);

    // Mailer facility
    bind(Mailer.class);

    // external web services
    bind(RequestProxy.class);
    bind(WebService.class);
    bind(ImageDetection.class);
    bind(RSSFeedClient.class).asEagerSingleton();
    bind(TweetFeedClient.class).asEagerSingleton();

    bind(CsvImporter.class).asEagerSingleton();
    bind(SitemapGenerator.class).asEagerSingleton();
    bind(TagLinker.class).asEagerSingleton();
    bind(ContributionRelevance.class).asEagerSingleton();
  }
}
