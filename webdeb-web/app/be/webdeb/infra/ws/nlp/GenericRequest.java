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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This simple class holds values that will be jsonified to be sent to WDTAL services:
 * # speech act classifier and text annotation by using the GenericRequest(language, text) constructor
 * # text extraction (isUrl) and language extraction (!isUrl) by using the GenericRequest(content, isUrl)
 *
 * @author Fabian Gilson
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericRequest {

  /**
   * iso 639-1 language code
   */
  @JsonSerialize
  private String language;

  /**
   * the text content (incompatible with url), used for language detection and text annotation
   */
  @JsonSerialize
  private String text;

  /**
   * a url (incompatible with text), used for text extraction from this url
   */
  @JsonSerialize
  private String url;


  /**
   * Constructor. Initialize this request for either a text language detection (isUrl = false) or text extraction
   * (isUrl = true).
   *
   * @param content a request content
   */
  public GenericRequest(String content, boolean isUrl) {
    if (isUrl) {
      url = content;
    } else {
      text = content;
    }
  }

  /**
   * Constructor. Initialize this request object for a topic extractor, text annotation or speech of act classifier
   *
   * @param language a 2-char iso 639-1 code of the language of given text
   * @param text a text to analyze
   */
  public GenericRequest(String language, String text) {
    this.language = language;
    this.text = text;
  }
}
