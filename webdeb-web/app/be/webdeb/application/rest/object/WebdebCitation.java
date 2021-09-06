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

import be.webdeb.core.api.citation.Citation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is devoted to exchange Excerpts in JSON format.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebdebCitation extends WebdebContribution {

    /**
     * unique id of originating text
     */
    @JsonSerialize
    private Long textId;

    /**
     * original citation from this.textId of this argument
     */
    @JsonSerialize
    private String originalExcerpt;

    /**
     * original citation from this.textId of this argument
     */
    @JsonSerialize
    private String workingExcerpt;

    /**
     * list of linked tags
     */
    @JsonSerialize
    private List<WebdebSimpleTag> tags = new ArrayList<>();

    /**
     * the list of actors involved in this argument (with their role and affiliation in this argument)
     */
    @JsonSerialize
    private List<WebdebActorRole> actors = new ArrayList<>();

    /**
     * language iso-639-1 code
     */
    @JsonSerialize
    private String language;

    /**
     * @api {get} WebdebArgument Citation
     * @apiName Citation
     * @apiGroup Structures
     * @apiDescription An argument is an citation of a text of a certain type standardized in a specific form
     * @apiSuccess {Integer} textId the id of the text this argument belongs to
     * @apiSuccess {String} citation the citation from which this argument has been extracted
     * @apiSuccess {Object} argumentType the argument type for this argument
     * @apiSuccess {Integer} argumentType.argumentType the type id
     * @apiSuccess {Object} argumentType.typeNames type name of the form { "en" : "englishType", "fr" : "TypeFrançais" }
     * @apiSuccess {Integer} argumentType.argumentTiming the timing id
     * @apiSuccess {Object} argumentType.timingNames timing name of the form { "en" : "englishTiming", "fr" :
     * "TimingFrançais" }
     * @apiSuccess {Integer} argumentType.argumentShade the shade id
     * @apiSuccess {Object} argumentType.shadeNames shade name of the form { "en" : "englishShade", "fr" :
     * "NuanceFrançaise" }, names are empty for performative and opinion.
     * @apiSuccess {Boolean} argumentType.singular true if this shade applies to one actor, false otherwise
     * @apiSuccess {String} standardForm standardized form (without actors and shade)
     * @apiSuccess {WebdebSimpleTag[]} tags list of tags linked with this argument
     * @apiSuccess {Integer[]} argumentLinks the list of ids of linked arguments
     * @apiSuccess {WebdebRole[]} actors a list of actors with their roles involved in this argument
     * @apiSuccess {WebdebPlace[]} places a list of places involved in this argument
     * @apiSuccess {String} language the iso-639-1 code of the language of this argument
     * @apiExample Citation (extracted from a text)
     * {
     *   "id": 776,
     *   "type": "argument",
     *   "version": 1464271112000,
     *   "textId": 774,
     *   "citation": "the doctor or any other person performing this illegal act upon a woman would be held legally responsible",
     *   "argumentType": {
     *     "argumentType": 1,
     *     "typeNames": { "en": "Prescriptive", "fr": "Prescriptive" },
     *     "argumentShade": 10,
     *     "shadeNames": { "en": "It is necessary to", "fr": "Il faut" },
     *     "argumentTiming": 1,
     *     "timingNames": { "en": "Present", "fr": "Présent" }
     *   },
     *   "argumentPlace": {
     *     { "lang": "fr", "name": "Namur", "subregion": "Province de Namur", "region": "Wallonie", "Country": "Belgique", "continent": "Europe" },
     *   },
     *   "standardForm": "hold legally responsible the doctor or any other person performing an abortion",
     *   "tags": {
     *      "id": 100000,
     *      "tagType": "node",
     *      "name": [ { "name": "actualité", "lang": "fr" } ],
     *   },
     *   "actors": [ @WebdebActorRole ]
     *   "language": "en"
     * }
     * @apiVersion 0.0.3
     */
    public WebdebCitation(Citation citation) {
        super(citation);
        textId = citation.getTextId();
        originalExcerpt = citation.getOriginalExcerpt();
        workingExcerpt = citation.getWorkingExcerpt();
        citation.getTags().forEach(f -> tags.add(new WebdebSimpleTag(f)));
        citation.getActors().forEach(a -> actors.add(new WebdebActorRole(a)));
        language = citation.getText().getLanguage().getCode();
    }
}
