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
import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.argument.ArgumentType;
import be.webdeb.core.api.contribution.EExternalSource;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.mail.Mailer;
import be.webdeb.infra.mail.MailerException;
import be.webdeb.infra.ws.util.WebService;
import be.webdeb.infra.mail.WebdebMail;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import play.Configuration;
import play.i18n.Lang;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to periodically retrieve  text from the NLP RSS service. Those texts will be
 * added directly in the database
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class TweetFeedClient {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private WebService ws;
  private CitationFactory factory;
  private ActorFactory actorFactory;
  private TextFactory textFactory;
  private Contributor rssContributor;
  private Group defaultGroup;
  private Configuration configuration;
  private Mailer mailer;

  /**
   * Injected constructor
   *
   * @param akka the akka agent system
   * @param configuration the play configuration module
   * @param factory citation factory to build tweet object instances and retrieve predefined types
   * @param actorFactory actor factory to retrieve actors associated to tweets
   * @param textFactory text factory to retrieve language
   * @param ws the web service helper
   * @param mailer the mailer service to warn of any problem
   */
  @Inject
  public TweetFeedClient(ActorSystem akka, Configuration configuration, CitationFactory factory, ActorFactory actorFactory,
                         TextFactory textFactory, ContributorFactory contributorFactory, WebService ws, Mailer mailer) {
    this.configuration = configuration;
    this.ws = ws;
    this.factory = factory;
    this.actorFactory = actorFactory;
    this.textFactory = textFactory;
    this.mailer = mailer;
    // rss contributor has id 0
    rssContributor = contributorFactory.retrieveContributor(0L);
    defaultGroup = contributorFactory.getDefaultGroup().getGroup();

    // start job to retrieve all rss texts
    if (configuration.getBoolean("tweet.scheduler.run")) {
      akka.scheduler().schedule(
          Duration.create(configuration.getInt("tweet.scheduler.start"), TimeUnit.SECONDS),
          Duration.create(configuration.getInt("tweet.scheduler.delay"), TimeUnit.HOURS),
          this::retrieveTweets,
          akka.dispatcher()
      );
    }
  }

  /**
   * Contact WDTAL service to retrieve all texts that have been captured from listening on rss channels.
   * For all valid texts, they are persisted into the database.
   */
  private void retrieveTweets() {
    for (String lang : configuration.getStringList("contribution.tweet.lang")) {
      logger.info("--- start of Twitter feeding in " + lang);
      CompletionStage<JsonTweet[]> stage = ws.postWithJsonResponse(
          configuration.getString("contribution.tweet.import"),
          Json.toJson(new TwitterRequest(lang, configuration.getInt("contribution.tweet.amount"))).toString()
      ).thenApplyAsync(result -> Json.fromJson(result, JsonTweet[].class));

      stage.thenAcceptAsync(tweets -> {
        TweetConfirmation confirm = new TweetConfirmation();
        confirm.tweetIds = new ArrayList<>();
        Map<String, String> report = new HashMap<>();
        for (JsonTweet t : tweets) {
          try {
            handleTweet(t);
            confirm.tweetIds.add(String.valueOf(t.id));
            report.put(t.toString(), "imported");
          } catch (FormatException e) {
            logger.error("unable to save " + t.toString(), e);
            report.put(t.toString(), e.getMore());
          }
        }
        // sending report mail
        sendMail(report);
        logger.info("now confirm tweets");

        ws.postWithJsonResponse(
            configuration.getString("contribution.tweet.confirm"),
            Json.toJson(confirm).toString()
        ).whenComplete((confirmed, throwable1) -> logger.info("Amount of confirmed tweets " + confirmed)
        ).exceptionally(t -> {
          logger.warn("unable to confirm tweets " + confirm.tweetIds.toString(), t);
          return null;
        });

        logger.info("--- end of Tweet feeding");
      }).exceptionally(throwable -> {
        logger.error("error while processing tweets ", throwable);
        return null;
      });
    }
  }

  /**
   * Convert given tweet and save it into given group
   *
   * @param json a tweet object to save in temporary database
   * @throws FormatException if any structured property could not be created with a corresponding field in given rss text
   */
  private synchronized void handleTweet(JsonTweet json) throws FormatException {
    /*if(factory.retrieveExternal(json.id, EExternalSource.TWITTER.id()) == null) {
      logger.debug("handling " + json.toString());
      // dates are of the form Sun Feb 12 17:34:01 +0000 2017
      SimpleDateFormat oFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss Z yyyy", Locale.ENGLISH);
      // must be converted into 12/02/2017
      SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");

      // map all fields
      ExternalCitation tweet = factory.getExternalCitation();
      tweet.setId(json.id);
      tweet.setOriginalExcerpt(convertStringFromJson(json.excerpt));
      tweet.setWorkingExcerpt(convertStringFromJson(json.standardForm));
      tweet.setLanguage(textFactory.getLanguage(json.language));
      tweet.setSourceId(EExternalSource.TWITTER.id());
      try {
        tweet.setPublicationDate(dFormat.format(oFormat.parse(json.time)));
      } catch (ParseException e) {
        logger.error("unable to parse date " + json.time, e);
      }
      Actor actor = actorFactory.retrieve(json.actorId);

      if (actor == null) {
        logger.debug("ignore tweet " + json.id + ", actor with id " + json.actorId + " is unknown");
        throw new FormatException(FormatException.Key.ACTOR_ERROR);
      }
      ExternalAuthor author = actorFactory.getExternalAuthor();
      author.setId(actor.getId());
      author.setName(actor.getFullname(tweet.getLanguage() != null ? tweet.getLanguage().getCode() : "en"));
      tweet.addAuthor(author);
      try {
        tweet.save(rssContributor.getId(), defaultGroup.getGroupId());
      } catch (Exception e) {
        logger.error("unable to save tmp tweet ", e);
        throw new FormatException(FormatException.Key.ARGUMENT_ERROR, e.getMessage());
      }
    }*/
  }

  /**
   * Remove all bad characters from json string
   *
   * @param json the json string
   * @return the correct string
   */
  private String convertStringFromJson(String json){
    char [] charArray = json.toCharArray();
    StringBuilder output = new StringBuilder();

    for(char c : charArray){
      if((int) c <= 255) {
        output.append(c);
      }
    }

    return output.toString();
  }

  /**
   * Will send an email to the rssContributor to give a report on tweet feeding
   *
   * @param report a map of stringified tweets and the import status
   */
  private void sendMail(Map<String, String> report) {
    try {
      mailer.sendMail(new WebdebMail(WebdebMail.EMailType.REPORT_TWEET, rssContributor.getEmail(), report, Lang.forCode("en")));
    } catch (MailerException e) {
      logger.error("unable to warn rss contributor " + rssContributor.getEmail() + " for tweets " + report, e);
    }
  }

  /**
   * Inner class to create twitter requests needing a language 2 char iso-639-1 code and an amount of
   * tweets to retrieve.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private static class TwitterRequest {

    /**
     * iso 639-1 language code
     */
    @JsonSerialize
    private String language;

    /**
     * amount of tweet to retrieve
     */
    @JsonSerialize
    private Integer number;

    /**
     * Default constructor
     *
     * @param language 2-char iso 639-1 code
     * @param number the amount of tweets to retrieve (0 for all)
     */
    public TwitterRequest(String language, int number) {
      this.language = language;
      if (number != 0) {
        this.number = number;
      }
    }
  }

  /**
   * Inner class to handle confirmation of correct reception of tweets
   */
  private static class TweetConfirmation {
    @JsonSerialize
    @JsonProperty("tweet_ids")
    private List<String> tweetIds;
  }

  /**
   * Inner class to retrieve texts from rss get requests
   */
  private static class JsonTweet {

    /**
     * Webdeb actor id
     */
    @JsonProperty("actor_id")
    @JsonDeserialize
    Long actorId;

    /**
     * Tweet unique ID
     */
    @JsonDeserialize
    Long id;

    /**
     * tweet iso 639-1 language
     */
    @JsonDeserialize
    String language;

    /**
     * The tweet's full text (will be saved as (hidden) full text
     */
    @JsonDeserialize
    String excerpt;

    /**
     * Transformed tweet into standardized form to be validated
     */
    @JsonDeserialize
    String standardForm;

    /**
     * Array of topics as extracted from tweet
     */
    @JsonDeserialize
    String[] topics;

    /**
     * Citation shade id as proposed by tweet service
     * @see ArgumentType
     */
    @JsonDeserialize
    int shade;

    /**
     *
     */
    @JsonDeserialize
    String time;

    @Override
    public String toString() {
      return "tweet [" + id + "] " + excerpt + ", standard: " + standardForm
          + ", shade: " + shade
          + ", lang: " + language + ", topics: " + Arrays.toString(topics)
          + ", actor: " + actorId + ", time: " + time;
    }
  }
}
