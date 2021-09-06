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

package be.webdeb.infra.relevance;

import akka.actor.ActorSystem;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.presentation.web.controllers.routes;
import play.Configuration;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to periodically compute contributions relevance.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class ContributionRelevance {

    // custom logger
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    private TextFactory factory;

    /**
     * Injected constructor
     *
     * @param akka the akka agent system
     * @param configuration the play configuration module
     * @param factory tag factory to find new links between tags and to create them
     */
    @Inject
    public ContributionRelevance(ActorSystem akka, Configuration configuration, TextFactory factory) {
        this.factory = factory;

        // launch once a day cleaning of non validated a accounts
        if (configuration.getBoolean("relevancer.run")) {
            akka.scheduler().schedule(
                    Duration.create(configuration.getInt("relevancer.start"), TimeUnit.SECONDS),
                    Duration.create(configuration.getInt("relevancer.delay"), TimeUnit.HOURS),
                    this::computeContributionsRelevance,
                    akka.dispatcher()
            );
        }
    }

    /**
     * Compute contribution relevances
     *
     */
    private void computeContributionsRelevance() {
        logger.debug("Compute new relevances...");

        factory.updateContributionsRelevance();

        logger.debug("Contributions relevance updated !");
    }

}
