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

package be.webdeb.infra.ws.ml;

import play.Configuration;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This class is used to validate image content to ensure they do not contain sensitive data such as
 * sexual or violent content. Relies on mashape API
 *
 * @author Fabian Gilson
 */
@Singleton
public class ImageDetection {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private WSClient ws;
  private String apiUrl;
  private String apiKey;
  private int timeout;

  /**
   * Injected constructor.
   *
   * @param configuration the webdeb configuration system
   * @param ws the web service utility client
   */
  @Inject
  public ImageDetection(Configuration configuration, WSClient ws) {
    this.ws = ws;
    this.apiUrl = configuration.getString("mashape.safe.image.url");
    this.apiKey = configuration.getString("mashape.safe.image.key");
    timeout = configuration.getInt("contribution.nlp.mashape.timeout");
  }

  /**
   * Check whether content of url looks safe, ie, no violence or nudity.
   *
   * @param url an url to validate
   * @return a promise of the check (true if safe, false in any other case)
   */
  public CompletionStage<Boolean> isImageSafe(String url) {
    try {
      String encoded = URLEncoder.encode(url, "UTF-8");
      logger.info("check if image is safe " + apiUrl + encoded);
      return ws.url(apiUrl + encoded)
          .setHeader("X-Mashape-Key", apiKey)
          .setHeader("Accept", "text/plain")
          .setRequestTimeout(timeout)
          .get().thenApplyAsync(WSResponse::asJson).thenApply(json -> {
            logger.debug("content is rated " + json.get("rating_label").asText());
            return !"adult".equals(json.get("rating_label").asText());
          }
      ).exceptionally(t -> {
        logger.error("unable to read JSON response from mashape safe image service", t);
        return true;
      });

    } catch (Exception e) {
      logger.error("unable to encode url or any other error occurred " + url, e);
      return CompletableFuture.completedFuture(true);
    }
  }
}
