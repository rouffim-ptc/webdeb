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

package be.webdeb.infra.sitemap;

import akka.actor.ActorSystem;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.text.TextFactory;
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
 * This class is used to periodically retrieve  text from the NLP RSS service. Those texts will be
 * added directly in the database
 *
 * @author Martin Rouffiange
 */
@Singleton
public class SitemapGenerator {

    // custom logger
    private static final org.slf4j.Logger logger = play.Logger.underlying();

    private TextFactory factory;
    private Configuration configuration;
    private FileSystem files;

    public static final String SITEMAP = "sitemap";
    public static final String SITEMAP_GENERAL = "sitemapgen";
    public static final String SITEMAP_ACTOR = "sitemapactor";
    public static final String SITEMAP_DEBATE = "sitemapdebate";
    public static final String SITEMAP_TAG = "sitemaptag";
    public static final String SITEMAP_TEXT = "sitemaptext";

    /**
     * Injected constructor
     *
     * @param akka the akka agent system
     * @param configuration the play configuration module
     * @param factory text factory to build tweet object instances and retrieve predefined types
     * @param files the files system
     */
    @Inject
    public SitemapGenerator(ActorSystem akka, Configuration configuration, TextFactory factory, FileSystem files) {
        this.configuration = configuration;
        this.factory = factory;
        this.files = files;

        // launch once a day cleaning of non validated a accounts
        if (configuration.getBoolean("sitemap.scheduler.run")) {
            akka.scheduler().schedule(
                    Duration.create(configuration.getInt("sitemap.scheduler.start"), TimeUnit.SECONDS),
                    Duration.create(configuration.getInt("sitemap.scheduler.delay"), TimeUnit.HOURS),
                    this::refreshSitemap,
                    akka.dispatcher()
            );
        }
    }

    /**
     * Refresh the sitemap file for google and co
     *
     */
    private void refreshSitemap() {
        logger.debug("Refresh sitemaps");

        createGeneralSitemap();
        createContributionSitemap(SITEMAP_ACTOR, EContributionType.ACTOR, 0.90);
        createContributionSitemap(SITEMAP_DEBATE, EContributionType.DEBATE, 0.90);
        createContributionSitemap(SITEMAP_TAG, EContributionType.TAG, 0.90);
        createContributionSitemap(SITEMAP_TEXT, EContributionType.TEXT, 0.70);
    }

    /**
     * Create general sitemap file
     *
     */
    private void createGeneralSitemap() {
        files.deleteSitemapFile(SITEMAP_GENERAL);

        List<Url> elements = Arrays.asList(
                createUrl(routes.Application.about().url(), 0.75),
                //reateUrl(routes.Application.aboutus().url(), 0.75),
                //createUrl(routes.Application.help().url(), 0.80),
                createUrl(routes.Application.index().url(), 1.0),
                //createUrl(routes.Application.partners().url(), 0.5),
                //createUrl(routes.Application.terms().url(), 0.5),
                //createUrl(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.login().url(), 0.80),
                //createUrl(be.webdeb.presentation.web.controllers.account.routes.ContributorActions.signup().url(), 0.80),
                createUrl(be.webdeb.presentation.web.controllers.entry.routes.EntryActions.contribute().url(), 1.0),
                createUrl(be.webdeb.presentation.web.controllers.viz.routes.VizActions.index(-1).url(), 1.0),
                /*createUrl(routes.Application.otherElection().url(), 0.95),
                createUrl(routes.Application.otherElectionStats().url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(1).url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(2).url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(3).url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(4).url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(5).url(), 0.95),
                createUrl(routes.Application.otherElectionStatsPage(6).url(), 0.95)*/

                createUrl(routes.Application.otherCovid().url(), 0.95)
        );

        saveSitemapToXML(SITEMAP_GENERAL, elements);
    }

    /**
     * Create contribution sitemap file
     *
     * @param filename the name of the file
     * @param type the concerned contribution type
     * @param priority the priority for this contribution type
     */
    private void createContributionSitemap(String filename, EContributionType type, Double priority) {
        files.deleteSitemapFile(filename);

        List<Url> elements = new ArrayList<>();

        factory.getAllIdByContributionType(type).forEach(e ->
                elements.add(createUrl(getContributionLink(e, type), priority)));

        saveSitemapToXML(filename, elements);
    }

    /**
     * Get the contribution
     *
     * @param id the contribution id
     * @param type the concerned contribution type
     */
    private String getContributionLink(Long id, EContributionType type) {
        switch (type){
            case ACTOR:
                return  be.webdeb.presentation.web.controllers.viz.routes.VizActions.actor(id, -1, 0).url();
            case DEBATE:
                return  be.webdeb.presentation.web.controllers.viz.routes.VizActions.debate(id, -1, 0).url();
            case TAG:
                return  be.webdeb.presentation.web.controllers.viz.routes.VizActions.tag(id, -1, 0).url();
            case TEXT:
                return  be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(id, -1, 0).url();
            default:
                return  be.webdeb.presentation.web.controllers.viz.routes.VizActions.dispatch(id, type.id(), -1, -1).url();
        }

    }

    /**
     * Create a xml file from a map of string
     *
     * @param title the title of the file
     * @param urls the sitemap urls to save
     */
    private void saveSitemapToXML(String title, List<Url> urls) {
        File file = new File(configuration.getString("sitemap.store.path") + title + ".xml");

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file));
            writer.write(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\t<urlset\n" +
                "\t\t xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
                "\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "\t\t xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9\n" +
                "\t\t\t http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n");

            for(Url url : urls){
                writer.write(url.toXml("\t\t"));
            }

            writer.write("\t</urlset>\n");

            //files.saveSitemapFile(file, title);
        }
        catch ( IOException e) {
            logger.debug("Problem with creating sitemap xml file");
        }
        finally {
            try {
                if ( writer != null)
                    writer.close( );
            }
            catch ( IOException e) {
                logger.debug("Problem with creating sitemap xml file");
            }
        }
    }

    private class Url {

        private String loc;

        private Double priority;

        Url(String loc, Double priority) {
            this.loc = "https://webdeb.be" + loc;
            this.priority = priority;
        }

        String toXml(String tab){
            return "" +
                tab + "<url>\n" +
                tab + "\t<loc>" + loc + "</loc>\n" +
                tab + "\t<priority>" + priority + "</priority>\n" +
                tab + "\t<changefreq>daily</changefreq>\n" +
                tab + "</url>\n";
        }
    }

    private Url createUrl(String loc, Double priority){
        return new Url(loc, priority);
    }
}
