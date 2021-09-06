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
 *
 */

package be.webdeb.application.rest.object;

import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextType;
import be.webdeb.infra.fs.FileSystem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.inject.Inject;
import play.api.Play;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is devoted to exchange Texts in JSON format. All details of an API Text are represented.
 * Links to other contributions are represented by their ids. Enumerated (predefined) data are passed as typed objects
 * with their ids and i18n descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebText extends WebdebContribution {

  @Inject
  private FileSystem files = Play.current().injector().instanceOf(FileSystem.class);

  /**
   * text title
   */
  @JsonSerialize
  private String title;

  /**
   * list of involved actors with their roles and affiliation in this text
   */
  @JsonSerialize
  private List<WebdebActorRole> actors;

  /**
   * language of this text in 2-char iso-639-1 code
   */
  @JsonSerialize
  private String language;

  /**
   * publication date in DD/MM/YYYY format (D and M optional)
   */
  @JsonSerialize
  private String publicationDate;

  /**
   * type of text (full object)
   */
  @JsonSerialize
  private TextType textType;

  /**
   * source name from which this text has been found
   */
  @JsonSerialize
  private String source;

  /**
   * url where this text originates from
   */
  @JsonSerialize
  private String crossReference;

  /**
   * text content
   */
  @JsonSerialize
  private String content;

  /**
   * list of linked tags
   */
  @JsonSerialize
  private List<WebdebSimpleTag> tags = new ArrayList<>();

  /**
   * @api {get} WebdebText Textual contribution
   * @apiName Text
   * @apiGroup Structures
   * @apiDescription Texts are contributions that usually contain arguments and are linked to actors
   * @apiSuccess {String} title the text title
   * @apiSuccess {WebdebRole[]} actors array of WebdebRole with all actors bound to this text
   * @apiSuccess {String} language iso-639-1 of this text language
   * @apiSuccess {String} publicationDate a publication date (DD/MM/YYYY format with D and M optional) or -1 for unknown
   * @apiSuccess {Object} textType a text type
   * @apiSuccess {Integer} textType.type text type id
   * @apiSuccess {Object} textType.names text type name of the form { "en" : "EnglishType", "fr" :"TypeFrançais" }
   * @apiSuccess {Object} textOrigin a text origin
   * @apiSuccess {Integer} textOrigin.origin a text origin id
   * @apiSuccess {Object} textOrigin.names text origin name of the form { "en" : "EnglishType", "fr" :"TypeFrançais" }
   * @apiSuccess {String} textOrigin.otherName a text origin name, in case the origin in "other" (may be null)
   * @apiSuccess {String} source the source name of this text, ie, the media where it comes from (optional)
   * @apiSuccess {WebdebSimpleTag[]} tags list of tags linked with this text
   * @apiSuccess {String} content the text content it the text is not under copyright (optional)
   * @apiSuccess {Integer[]} arguments the list of argument ids originating from this text (may be null)
   * @apiExample Text in webdeb
   * {
   *   "id": 614,
   *   "type": "text",
   *   "version": 1461850237000,
   *   "title": "Aung San Suu Kyi : « Le prix Nobel de la paix a ouvert une porte dans mon cœur »",
   *   "actors": [ @WebdebActorRole ],
   *   "language": "fr",
   *   "publicationDate": "19/06/2012",
   *   "textType": {
   *     "type": 3,
   *     "names": {
   *       "en": "Journalistic (news article, press report,...)",
   *       "fr": "Journalistique (article de presse, reportage,...)"
   *     }
   *   },
   *   "textOrigin": { "origin": 0, "names": { "en": "Written document", "fr": "Document écrit" } },
   *   "source": "La Croix",
   *   "arguments": [ 615, 616, 617, 618, 619, 620 ],
   *   "tags": {
   *      "id": 100000,
   *      "tagType": "node",
   *      "name": [ { "name": "actualité", "lang": "fr" } ],
   *   },
   *   "argumentPlace": {
   *     { "lang": "fr", "name": "Namur", "subregion": "Province de Namur", "region": "Wallonie", "Country": "Belgique", "continent": "Europe" },
   *   }
   * }
   * }
   * @apiVersion 0.0.1
   */
  /**
   * Constructor
   *
   * @param text a text to wrap
   * @param authorized true if also the content must be passed (even if copyrighted)
   */
  public WebdebText(Text text, boolean authorized) {
    super(text);
    title = text.getTitle(text.getLanguage().getCode());
    language = text.getLanguage().getCode();
    publicationDate = text.getPublicationDate();
    textType = text.getTextType();
    source = text.getSourceTitle();
    crossReference = text.getUrl();
    text.getTags().forEach(f -> tags.add(new WebdebSimpleTag(f)));
    if (authorized || (text.getTextVisibility().getEType() == ETextVisibility.PUBLIC)) {
      content = files.getContributionTextFile(text.getFilenames().get(-1L));
    }
    actors = text.getActors().stream().map(WebdebActorRole::new).collect(Collectors.toList());
  }
}
