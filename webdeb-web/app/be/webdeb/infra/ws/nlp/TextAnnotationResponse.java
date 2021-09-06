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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Simple POJO class to handle JSON response from NLP WDTAL annotation service
 *
 * @author Fabian Gilson
 */
public class TextAnnotationResponse {

  /**
   * Receiver for jackson mapping
   */
  @JsonDeserialize
  @JsonProperty("_links")
  private List<ResponseObject> links;

  /**
   * Extract the href elements from Json response
   *
   * @return the href element (a REST service url to call) or null if unfound
   */
  public String getRelatedHref() {
    for (ResponseObject o : links) {
      if ("related".equals(o.rel)) {
        return o.href;
      }
    }
    return null;
  }

  /**
   * inner class to handle response objects
   */
  private static class ResponseObject {

    /**
     * hyper reference to query in order to get the result of the annotation request
     */
    @JsonDeserialize
    private String href;

    /**
     * related value ("self" or "related")
     */
    @JsonDeserialize
    private String rel;

    /**
     * response-type (eg application/json)
     */
    @JsonDeserialize
    private String type;
  }
}
