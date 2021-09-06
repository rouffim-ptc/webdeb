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

import be.webdeb.application.nlphelper.WDTALAnnotator;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.ws.util.WebService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flexjson.JSONException;
import play.Configuration;
import play.libs.Json;
import scala.util.parsing.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

/**
 * Request handler for all WDTAL services.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class RequestProxy {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();
  
  private FileSystem files;
  private WDTALAnnotator annotator;
  private WebService ws;
  private Configuration configuration;


  /**
   * Injected constructor
   *
   * @param files the file system helper
   * @param annotator the text annotator helper
   * @param ws the webservice helper
   * @param configuration the play configuration module
   */
  @Inject
  public RequestProxy(FileSystem files, WDTALAnnotator annotator, WebService ws, Configuration configuration) {
    this.files = files;
    this.annotator = annotator;
    this.ws = ws;
    this.configuration = configuration;
  }

  /**
   * Contact the WDTAL-text annotation service to annotate a given text located at given filename
   *
   * @param filename a filename where a text content can be retrieved to be sent
   * @param lang the text language as a 2-char ISO code
   * @return true if the annotation service could be called, and maybe, the annotated text has been saved,
   * or null if an error occurred
   */
  public CompletionStage<String> getAnnotatedText(String filename, String lang) {
    logger.debug("send REST request to WDTAL annotation service");
    String rawContent = files.getContributionTextFile(filename);

    // if no content, give up
    if (rawContent == null) {
      logger.error("no content retrieved for contribution file named " + filename);
      return null;
    }

    return ws.postWithStringResponse(configuration.getString("contribution.nlp.text"),
        Json.toJson(new GenericRequest(lang, rawContent)).toString()).thenApply(content -> {
      // check if the received content looks like an xml file
      if (annotator.isExpectedContent(content)) {
        return content;
      }
      logger.error("retrieved XML content from WDTAL does not look valid " + content);
      return null;

    }).exceptionally(t -> {
      logger.error("unable to retrieve WDTAL-text xml content", t);
      return null;
    });
  }

  /**
   * Contact the WDTAL-text service and retrieve the complete JSON response
   *
   * @param url the url from which we want to retrieve some text
   * @return a Promise of the JSON response from the WDTAL web service, or null if an error occurred
   */
  public CompletionStage<JsonNode> getTextContentAsJson(String url) {
    return ws.postWithJsonResponse(configuration.getString("contribution.nlp.gettext"),
        Json.toJson(new GenericRequest(url, true)).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-text service", t);
          return null;
        });
  }

  /**
   * Contact the WDTAL-text service to retrieve language id of given text
   *
   * @param text the text content to analyze
   * @return a Promise of the JSON response from the WDTAL web service, or null if an error occurred
   */
  public CompletionStage<JsonNode> getTextLanguageAsJson(String text) {
    return ws.postWithJsonResponse(configuration.getString("contribution.nlp.language"),
        Json.toJson(new GenericRequest(text, false)).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-language service", t);
          return null;
        });
  }

  /**
   * Contact the WDTAL-wikidata service to retrieve details for a given actor name or wikipedia url
   *
   * @param request the request object to be passed to the Wikidata service
   * @return a Promise of the JSON response from the WDTAL web service, or null if an error occurred
   */
  public CompletionStage<JsonNode> getActorDetailsAsJson(WikidataExtractRequest request) {
    return ws.postWithJsonResponse(configuration.getString("contribution.nlp.wikidata"),
        Json.toJson(request).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-wikidata service", t);
          return null;
        });
  }

  /**
   * Generic get request for images
   *
   * @param url a url to get
   * @param path path where the file will be stored
   * @return the response as a JSON node or null if call failed.
   */
  public CompletionStage<File> getImageFile(String url, String path) {
    return ws.getRawFile(url, path).thenApply(r -> r).exceptionally(t -> {
      logger.error("unable to call or read JSON response from generic GET call", t);
      return null;
    });
  }

  /**
   * Post validation of argument for discovered tweets
   *
   * @param tweet id of tweet (temporary argument)
   * @param argument corresponding id of argument in webdeb database
   * @param standardForm standard form of this argument as saved by contributor
   * @return the response as a simple string () or null if call failed
   */
  public CompletionStage<String> validateTweet(String tweet, String argument, String standardForm) {
    return ws.postWithStringResponse(configuration.getString("contribution.tweet.validate"),
        Json.toJson(new ValidateTweetRequest(tweet, argument, standardForm)).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-validate-tweet service", t);
          return null;
        });
  }

  /**
   * Post the rejection of a Tweet
   *
   * @param tweet id of Tweet
   * @return the response as a simple string () or null if call failed
   */
  public CompletionStage<String> rejectTweet(String tweet) {
    return ws.postWithStringResponse(configuration.getString("contribution.tweet.reject"),
        Json.toJson(new RejectTweetRequest(tweet)).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-reject-tweet service", t);
          return null;
        });
  }

  /**
   * Post request to extract PDF content from given url
   *
   * @param url an url to get content from
   * @return the response as a JSON node or null if the call failed
   */
  public CompletionStage<JsonNode> retrievePDF(String url) {
    return ws.postWithJsonResponse(configuration.getString("contribution.nlp.pdf"),
        Json.toJson(new GenericRequest(url, true)).toString())
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to call or read JSON response from WDTAL-pdf service", t);
          return null;
        });
  }

  /**
   * Get the list of twitter accounts to which we are listening to
   *
   * @return the response as a json node (list of {@link TwitterAccount} twitter accounts), null if an error occurred
   */
  public CompletionStage<JsonNode> listTwitterAccounts() {
    return ws.postWithJsonResponse(configuration.getString("contribution.tweet.accounts.list"), "")
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to get list of twitter accounts from WDTAL Twitter service", t);
          return null;
        });
  }

  /**
   * Add given Twitter account by calling external WDTAL service
   *
   * @param account a Twitter account
   * @return the response from external service, or null if an error occurred
   */
  public CompletionStage<String> addTwitterAccount(TwitterAccount account) {
    return ws.postWithStringResponse(configuration.getString("contribution.tweet.accounts.add"), Json.toJson(account).toString())
        // result is a list of strings, or starts with "-- Error" in case of error
        .thenApply(r -> r != null && !r.startsWith("-- Error") ? r : null)
        .exceptionally(t -> {
          logger.error("unable to add twitter account " + account.getAccount(), t);
          return null;
        });
  }

  /**
   * Remove given Twitter account by calling external WDTAL service
   *
   * @param account a Twitter account name
   * @return the response from external service, or null if an error occurred
   */
  public CompletionStage<String> removeTwitterAccount(RemoveTwitterAccount account) {
    return ws.postWithStringResponse(configuration.getString("contribution.tweet.accounts.remove"), Json.toJson(account).toString())
        // result is a list of strings, or is a bad gateway in case of error
        .thenApply(r -> r != null && r.startsWith("[") ? r : null)
        .exceptionally(t -> {
          logger.error("unable to remove twitter account " + account, t);
          return null;
        });
  }

  /**
   * Get the list of rss feeds we are feeding the database from
   *
   * @return the response as a json node (list of {@link RSSFeedSource} rss sources), null if an error occurred
   */
  public CompletionStage<JsonNode> listRssFeeders() {
    return ws.postWithJsonResponse(configuration.getString("contribution.rss.feed.list"), "")
        .thenApply(r -> r)
        .exceptionally(t -> {
          logger.error("unable to get list of rss feeders from WDTAL RSS service", t);
          return null;
        });
  }

  /**
   * Add or modify given RSS source by calling external WDTAL service
   *
   * @param source an RSS source
   * @return the response from external service, or null if an error occurred
   */
  public CompletionStage<String> addRssFeed(RSSFeedSource source) {
    return ws.postWithStringResponse(configuration.getString("contribution.rss.feed.add"), Json.toJson(source).toString())
        // result is a list of strings, or starts with "-- Error" in case of error
        .thenApply(r -> r != null && !r.startsWith("-- Error") ? r : null)
        .exceptionally(t -> {
          logger.error("unable to add rss feed " + source.getUrl(), t);
          return null;
        });
  }

  /**
   * Update status of given RSS source by calling external WDTAL service
   *
   * @param source an RSS source
   * @return the response from external service, or null if an error occurred
   */
  public CompletionStage<String> updateStatusOfRssFeed(RSSFeedSource source) {
    return ws.postWithStringResponse(configuration.getString("contribution.rss.feed.status"), Json.toJson(source).toString())
        // result is a String (starting with a "[" but not closed) or value "null" if action could not be performed
        .thenApply(r -> r != null && !"null".equals(r) ? r : null)
        .exceptionally(t -> {
          logger.error("unable to set status of rss feed " + source.getUrl(), t);
          return null;
        });
  }

  /**
   * Remove given RSS source by calling external WDTAL service
   *
   * @param source an RSS source
   * @return the response from external service, or null if an error occurred
   */
  public CompletionStage<String> removeRssFeed(RSSFeedSource source) {
    // result is the removed source id, or -1 in case of error
    return ws.postWithStringResponse(configuration.getString("contribution.rss.feed.remove"), Json.toJson(source).toString())
        .thenApply(r -> r != null && !"-1".equals(r) ? r : null)
        .exceptionally(t -> {
          logger.error("unable to remove rss feed " + source.getUrl(), t);
          return null;
        });
  }

    /**
     * HTTP Get and retrieve result as a JsonNode
     *
     * @param url the url to call
     * @return the json
     */
    public JsonNode readJsonFromUrl(String url) throws Exception {
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();

        connection.getResponseCode();
        InputStream is = connection.getErrorStream();
        if (is == null) {
            is = connection.getInputStream();
        } else {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            throw new Exception();
        }

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);

            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readTree(jsonText);
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
