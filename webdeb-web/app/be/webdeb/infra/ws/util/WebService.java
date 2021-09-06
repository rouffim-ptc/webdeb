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

package be.webdeb.infra.ws.util;

import be.webdeb.infra.fs.FileSystem;
import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
import play.libs.ws.*;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.CompletionStage;


/**
 * Helper class to call external REST webservices
 *
 * @author Fabian Gilson
 */
public class WebService {

  private WSClient ws;
  private FileSystem files;

  private static int timeout;
  private static int longTimeout;
  private static final int CONTENT_LOGSIZE = 800;
  private static final String CONTENT_TYPE = "application/json charset=utf-8";

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();


  /**
   * Injected constructor
   *
   * @param configuration a play configuration module
   * @param ws the web service helper
   * @param files the file system helper
   */
  @Inject
  public WebService(Configuration configuration, WSClient ws, FileSystem files) {
    this.ws = ws;
    this.files = files;
    timeout = configuration.getInt("contribution.nlp.timeout");
    longTimeout = configuration.getInt("contribution.nlp.longtimeout");
  }

  /**
   * HTTP Post and retrieve result as a JsonNode (with default timeout)
   *
   * @param url the url to call
   * @param content the json data to put as the POST content
   * @return a promise of the HTTP response body as a JsonNode, or null if response is not a Json structure
   */
  public CompletionStage<JsonNode> postWithJsonResponse(final String url, final String content) {
    return postWithJsonResponse(url, content, timeout);
  }

  /**
   * HTTP Post and retrieve result as a JsonNode with specified timeout
   *
   * @param url the url to call
   * @param content the json data to put as the POST content
   * @param timeout the timeout of the request, in miliseconds
   * @return a promise of the HTTP response body as a JsonNode, or null if response is not a Json structure
   */
  public CompletionStage<JsonNode> postWithJsonResponse(final String url, final String content, int timeout) {
    logger.debug("WS POST " + (content.length() <= CONTENT_LOGSIZE ?
        content : content.substring(0, CONTENT_LOGSIZE) + "(...)") + " on " + url);
    return ws.url(url).setContentType(CONTENT_TYPE).setRequestTimeout(timeout).post(content).thenApplyAsync(WSResponse::asJson);
  }

  /**
   * HTTP Post and retrieve result as raw content
   *
   * @param url the url to call
   * @param content the json data to put as the post content
   * @return the response from the post call
   */
  public CompletionStage<String> postWithStringResponse(final String url, final String content) {
    logger.debug("WS POST " + (content.length() <= CONTENT_LOGSIZE ?
        content : content.substring(0, CONTENT_LOGSIZE) + "(...)") + " on " + url);
    return ws.url(url).setContentType(CONTENT_TYPE).setRequestTimeout(longTimeout).post(content).thenApplyAsync(WSResponse::getBody);
  }

  /**
   * HTTP Get and retrieve result as a JsonNode
   *
   * @param url the url to call
   * @return a promise of the HTTP response body as a Json node, or null if response is not a Json structure
   */
  public CompletionStage<JsonNode> getWithJsonResponse(final String url) {
    return getWithJsonResponse(url, timeout);
  }

  /**
   * HTTP Get and retrieve result as a JsonNode
   *
   * @param url the url to call
   * @param timeout the timeout of the request, in miliseconds
   * @return a promise of the HTTP response body as a Json node, or null if response is not a Json structure
   */
  public CompletionStage<JsonNode> getWithJsonResponse(final String url, int timeout) {
    logger.debug("WS GET with JSON response on " + url);
    return ws.url(url).setRequestTimeout(timeout).get().thenApplyAsync(WSResponse::asJson);
  }

  /**
   * HTTP GET a file object from a given url and save it to a given path
   *
   * @param url the url to get
   * @param savepath the path where the retrieved file will be stored
   * @return a F.Promise of the retrieved file, null if any error occurred
   */
  public CompletionStage<File> getRawFile(final String url, final String savepath) {
    logger.debug("WS GET raw file on " + url);
    return ws.url(url).setFollowRedirects(true).setRequestTimeout(timeout).get().thenApplyAsync(response -> {
      logger.debug(response.getStatus() + " " + response.getUri());
      return files.getRawFile(response.getBodyAsStream(), savepath);
    });
  }
}
