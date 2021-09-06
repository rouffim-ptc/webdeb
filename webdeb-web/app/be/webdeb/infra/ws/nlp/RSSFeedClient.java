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

package be.webdeb.infra.ws.nlp;

import akka.actor.ActorSystem;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.ActorName;
import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.api.text.TextType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.mail.MailerException;
import be.webdeb.infra.ws.util.WebService;
import be.webdeb.infra.mail.WebdebMail;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.viz.EVizPane;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import play.Configuration;
import play.i18n.Lang;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is used to periodically retrieve  text from the NLP RSS service. Those texts will be
 * added directly in the database
 *
 * @author Fabian Gilson
 */
@Singleton
public class RSSFeedClient {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private WebService ws;
  private TextFactory factory;
  private ActorFactory actorFactory;
  private ContributorFactory contributorFactory;
  private TagFactory tagFactory;
  private Contributor rssContributor;
  private Configuration configuration;
  private Mailer mailer;
  private ContributionHelper helper;

  // contributor id of technical user
  private static final String HARVEST_DIR = "harvest";

  /**
   * Injected constructor
   *
   * @param akka the akka agent system
   * @param configuration the play configuration module
   * @param factory text factory to build tweet object instances and retrieve predefined types
   * @param actorFactory actor factory to retrieve actors associated to tweets
   * @param contributorFactory contributor factory to retrieve contributor
   * @param tagFactory tag factory to retrieve tags and retriveve predefined types
   * @param ws the web service helper
   * @param mailer the mailer service to warn of any problem
   * @param helper the contribution helper to verify names
   */
  @Inject
  public RSSFeedClient(ActorSystem akka, Configuration configuration, TextFactory factory, ActorFactory actorFactory,
                       ContributorFactory contributorFactory, TagFactory tagFactory, WebService ws, Mailer mailer, ContributionHelper helper) {
    this.configuration = configuration;
    this.ws = ws;
    this.factory = factory;
    this.actorFactory = actorFactory;
    this.contributorFactory = contributorFactory;
    this.tagFactory = tagFactory;
    this.mailer = mailer;
    this.rssContributor = contributorFactory.retrieveContributor(0L);
    this.helper = helper;

    // start job to retrieve all rss texts
    if (configuration.getBoolean("rss.scheduler.run")) {
      akka.scheduler().schedule(
          Duration.create(configuration.getInt("rss.scheduler.start"), TimeUnit.SECONDS),
          Duration.create(configuration.getInt("rss.scheduler.delay"), TimeUnit.HOURS),
          this::retrieveRSSFeeds,
          akka.dispatcher()
      );
    }
  }

  /**
   * Contact WDTAL service to retrieve all texts that have been captured from listening on rss channels.
   * For all valid texts, they are persisted into the database.
   */
  private void retrieveRSSFeeds() {
    logger.info("--- start of RSS feeding");
    Group defaultGroup = contributorFactory.getDefaultGroup().getGroup();
    CompletionStage<String[]> list = ws.postWithJsonResponse(
        configuration.getString("contribution.rss.list"),
        Json.toJson(new RSSDirectory(HARVEST_DIR)).toString()
    ).thenApply(r -> {
      String[] files = Json.fromJson(r, String[].class);
      logger.debug("will retrieve rss files " + Arrays.toString(files));
      return files;
    });

    list.thenAccept(names ->
      // retrieve content of all files
      all(Arrays.stream(names).map(name ->
          // and for each of them, get their texts
          retrieveAndHandleRSSFile(name, defaultGroup)).collect(Collectors.toList()))

        // when all files have been processed, send report mail
        .thenAccept(maps -> {
          logger.info("--- end of RSS feeding");
          // not using map-collectors (not working if given maps are too big)
          Map<String, String> tosend = new HashMap<>();
          for (Map<String, String> map : maps) {
            tosend.putAll(map);
          }
          sendMail(tosend);
        })
    );
  }

  /**
   * Retrieve given RSS filename and asynchronously handle all texts from this filename. Returns a
   * completable future of the whole process.
   *
   * @param filename the file to retrieve from the WDTAL RSS web service
   * @param defaultGroup the group where to store the retrieved text described in given (to be retrieved) file
   * @return a future of the result of the handling of given RSS file (filename) consisting in a map of strings
   * depicting the result for all texts contained in given file or, in case of an error with that file,
   * the error raised when handling that file
   */
  private CompletableFuture<Map<String, String>> retrieveAndHandleRSSFile(String filename, Group defaultGroup) {
    // get given filename from RSS web service
    return ws.postWithJsonResponse(
        configuration.getString("contribution.rss.get"),
        Json.toJson(new RSSFilename(filename)).toString()

    ).thenApplyAsync(jsonResponse -> {

      // then handle all texts from this file
      Map<String, String> report = new HashMap<>();
      logger.info("successfully retrieved file " + filename);
      RSSText[] rssTexts = Json.fromJson(jsonResponse, RSSText[].class);

      // one RSS file contains a list of texts
      for (RSSText rssText : rssTexts) {
        try {
          handleRSSText(filename, rssText, defaultGroup);
          report.put(rssText.title + " " + Arrays.toString(rssText.authors), "saved");
        } catch (FormatException | PermissionException | PersistenceException e) {
          logger.error("unable to save rss text with title " + rssText.title + " from file " + filename
              + ". Reason: " + e.getMore(), e);

          report.put(rssText.title + " " + Arrays.toString(rssText.authors), e.getMore());
        }
      }

      removeRSSFromList(filename);
      return report;

    }).exceptionally(t -> {
      // an error occured while handling given file, reports it
      logger.error("unable to handle " + filename, t);
      Map<String, String> error = new HashMap<>();
      error.put(filename, t.getMessage());
      // file is badly formed, remove it anyway
      removeRSSFromList(filename);
      return error;

      // make it a completableFuture because we will have to use the allOf method to wait for all resolutions
    }).toCompletableFuture();
  }

  /**
   * Call WDTAL RSS move service to remove given filename from list of files to retrieve
   *
   * @param filename a file to remove from harvest list
   */
  private void removeRSSFromList(String filename) {
    // now send requests to remove those files
    logger.info("will remove " + filename);
    ws.postWithJsonResponse(configuration.getString("contribution.rss.move"),
        Json.toJson(new RSSFilename(filename)).toString()

    ).whenCompleteAsync((moved, throwable) ->
        logger.info("removed " + Arrays.toString(Json.fromJson(moved, String[].class)))

    ).exceptionally(t -> {
      logger.warn("unable to remove " + filename, t);
      return null;
    });
  }

  /**
   * Convert given rss text (found in given filename) and save it into given group
   *
   * @param filename the filename where this rss text is coming from
   * @param rssText an rss text object
   * @param group the group where this text must be saved
   * @throws PersistenceException if any error occurred while persisting the converted api text
   * @throws PermissionException if the technical contributor (RSS Feeder) used to save this text may not save the text into given text
   * @throws FormatException if any structured property could not be created with a corresponding field in given rss text
   */
  private synchronized void handleRSSText(String filename, RSSText rssText, Group group)
      throws PersistenceException, PermissionException, FormatException{
    Text text = factory.getText();
    // check if title already exists for such source
    Optional<Text> candidate = factory.findByTitle(rssText.title).stream().filter(t ->
        t.getSourceTitle() != null && t.getSourceTitle().equals(rssText.source)).findAny();
    if (candidate.isPresent()) {
      // leave now because this text must be ignored
      logger.warn("ignore text with title " + rssText.title + " from file " + filename
          + " since it seems to already exist in database");
      throw new FormatException(FormatException.Key.TEXT_ERROR, "text.rss.duplicate "
          + be.webdeb.presentation.web.controllers.viz.routes.VizActions.text(candidate.get().getId(), EVizPane.DETAILS.id(), 0).url());
    }
    text.addTitle(rssText.language, rssText.title);

    // remaining fields
    text.setLanguage(factory.getLanguage(rssText.language));
    text.setTextVisibility(factory.getTextVisibility(rssText.visibility));

    text.setTextContent(rssText.text);
    text.setTextType(factory.getTextType(rssText.textType));
    text.isFetched(true);

    // dates are YYYY-MM-DD, must be converted to DD/MM/YYYY
    String[] date = (rssText.date != null ? rssText.date : rssText.parsedDate).split("-");
    if (date.length == 3) {
      text.setPublicationDate(date[2] + "/" + date[1] + "/" + date[0]);
    } else {
      text.setPublicationDate(null);
    }
    text.setUrl(rssText.url);
    text.setSourceTitle(rssText.source);

    // creating tag objects (if existing, will be handled at DB level)
    Arrays.stream(rssText.topics).forEach(t -> {
      try {
        Tag tag = tagFactory.findUniqueByNameAndLang(t, rssText.language, ETagType.SUB_DEBATE_TAG);
        if (tag == null) {
          tag = tagFactory.getTag();
          tag.addName(t, rssText.language);
          tag.setTagType(tagFactory.getTagType(ETagType.SIMPLE_TAG.id()));
        }
        text.addTag(tag);
      } catch (FormatException e) {
        logger.warn("will ignore tag " + t, e);
      }
    });

    // add a default tag if no tag are linked to the new text
    if(text.getTags().isEmpty()){
      text.addTag(tagFactory.getRssDefaultTag());
    }

    // default to public group
    text.setInGroups(Collections.singletonList(group));

    // check whether all authors are not already known
    text.getActors();
    for (String author : rssText.authors) {
      List<Actor> match = actorFactory.findByFullname(author, EActorType.UNKNOWN);
      ActorRole role;
      switch (match.size()) {
        case 0:
          Actor temp = actorFactory.getActor();
          ActorName name = actorFactory.getActorName(rssText.language);
          name.setLast(author);
          temp.setNames(Collections.singletonList(name));
          role = actorFactory.getActorRole(temp, text);
          role.setIsAuthor(true);
          text.addActor(role);
          break;

        case 1:
          role = actorFactory.getActorRole(match.get(0), text);
          role.setIsAuthor(true);
          text.addActor(role);
          break;

        default:
          logger.warn("author must be disambiguated for text " + rssText.title + " with authors "
              + Arrays.toString(rssText.authors) + " from file " + filename);
          // send an email to warn rssContributor the text has been ignored
          throw new FormatException(FormatException.Key.TEXT_ERROR, "text.rss.ambiguous.actor " + match.toString());
      }
    }

    logger.info("start saving of " + text.toString());
    text.save(rssContributor.getId(), group.getGroupId());
  }

  /**
   * Will send an email to this rssContributor to warn him given text has been refused
   *
   * @param report a map of stringified texts and import status
   */
  private void sendMail(Map<String, String> report) {
    try {
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.REPORT_RSS, rssContributor.getEmail(), report, Lang.forCode("en")));
    } catch (MailerException e) {
      logger.error("unable to warn rss contributor " + rssContributor.getEmail() + " for texts " + report, e);
    }
  }

  /**
   * Convert list of futures to an array of futures and return the future of the CompletableFuture.allOf call
   *
   * @param futures a list of futures
   * @param <T> type parameter
   * @return the completableFuture waiting for the completion of all given futures
   */
  private <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures) {
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenApply(t ->
        futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
  }

  /**
   * Inner class to create rss requests needing a filename, either to get a file content
   * or to move it somewhere else (deleted tag, or back to repository)
   */
  private static class RSSFilename {

    /**
     * filename to get the content from or to acknowledge reception
     */
    @JsonSerialize
    private String filename;

    /**
     * Default constructor
     *
     * @param filename the filename to retrieve
     */
    public RSSFilename(String filename) {
      this.filename = filename;
    }
  }

  /**
   * Inner class to create rss requests to list files in a directory
   */
  private static class RSSDirectory {

    /**
     * directory name to query
     */
    @JsonSerialize
    private String directory;

    /**
     * Default constructor
     *
     * @param directory the directory to get files from
     */
    public RSSDirectory(String directory) {
      this.directory = directory;
    }
  }

  /**
   * Inner class to retrieve texts from rss get requests
   */
  private static class RSSText {

    /**
     * name of source where this text originates from
     */
    @JsonDeserialize
    @JsonProperty("source_name")
    private String source;

    /**
     * iso 639-1 language code
     */
    @JsonDeserialize
    private String language;

    /**
     * text title
     */
    @JsonDeserialize
    private String title;

    /**
     * url where the original text may be found
     */
    @JsonDeserialize
    private String url;

    /**
     * text content
     */
    @JsonDeserialize
    private String text;

    /**
     * list of topics associated to this text
     */
    @JsonDeserialize
    private String[] topics;

    /**
     * visibility id for this text
     *
     * @see be.webdeb.core.api.text.ETextVisibility
     */
    @JsonDeserialize
    @JsonProperty("text_visibility")
    private int visibility;

    /**
     * list of names of authors of this text
     */
    @JsonDeserialize
    private String[] authors;

    /**
     * publication date of the article
     */
    @JsonDeserialize
    private String date;

    /**
     * date at which this rss text has been retrieved from RSS web service (fallback date)
     */
    @JsonDeserialize
    @JsonProperty("date_parsed")
    private String parsedDate;

    /**
     * text type id
     *
     * @see TextType
     */
    @JsonDeserialize
    @JsonProperty("text_type")
    private int textType;

    /**
     * Default json-compliant constructor
     */
    public RSSText() {
      // needed by jackson
    }
  }
}
