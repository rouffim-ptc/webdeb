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

package be.webdeb.core.exception;

/**
 * This Exception is used to warn that a field does not comply to the expected format or may not hold given
 * value.
 *
 * @author Fabian Gilson
 */
public class FormatException extends AbstractException {

  private static final long serialVersionUID = 1L;

  /**
   * List of acceptable keys for format exceptions
   */
  public enum Key {
    /**
     * Message key used to warn user there is an error in given contributor (should be completed with more details)
     */
    CONTRIBUTOR_ERROR,
    /**
     * Message key used to warn user there is an error in given actor (should be completed with more details)
     */
    ACTOR_ERROR,
    /**
     * Message key used to warn user there is an error in given affiliation (should be completed with more details)
     */
    AFFILIATION_ERROR,
    /**
     * Message key used to warn user there is an error in given affiliation type, ie the affiliation type is invalid for
     * linked affiliation
     *
     * @see be.webdeb.core.api.actor.Affiliation
     * @see be.webdeb.core.api.actor.EAffiliationType
     */
    AFFILIATION_TYPE_ERROR,
    /**
     * Message key used to warn user there is an error in given argument (should be completed with more details)
     */
    ARGUMENT_ERROR,
    /**
     * Message key used to warn user there is an error in given argument dictionary (should be completed with more details)
     */
    ARGUMENT_DICTIONARY_ERROR,
    /**
     * Message key used to warn user there is an error in given contextualized argument (should be completed with more details)
     */
    CONTEXTUALIZED_ARGUMENT_ERROR,
    /**
     * Message key used to warn user there is an error in given citation (should be completed with more details)
     */
    CITATION_ERROR,
    /**
     * Message key used to warn user there is an error in given debate (should be completed with more details)
     */
    DEBATE_ERROR,
    /**
     * Message key used to warn user there is an error in given argument link (should be completed with more details)
     */
    LINK_ERROR,
    /**
     * Message key used to warn user there is an error in given text (should be completed with more details)
     */
    TEXT_ERROR,
    /**
     * Message key used to warn user there is an error in given project (should be completed with more details)
     */
    PROJECT_ERROR,
    /**
     * Message key used to warn user there is an error in given project group (should be completed with more details)
     */
    PROJECT_GROUP_ERROR,
    /**
     * Message key used to warn user there is an error in given project subgroup (should be completed with more details)
     */
    PROJECT_SUBGROUP_ERROR,
    /**
     * Message key used to warn user there is an error in given tag (should be completed with more details)
     */
    TAG_ERROR,
    /**
     * Message key used to warn user there is an error in given tag link (should be completed with more details)
     */
    TAG_LINK_ERROR,
    /**
     * Message key used to warn user there is an error in the actor having a role in given textual contribution (should be completed with more details)
     */
    ROLE_ERROR,
    /**
     * Message key used to warn user given word banned profession is unknown
     */
    UNKNOWN_WORD_BANNED_GENDER,
    /**
     * Message key used to warn user given word gender is unknown
     */
    UNKNOWN_WORD_GENDER,
    /**
     * Message key used to warn user given gender is unknown
     */
    UNKNOWN_GENDER,
    /**
     * Message key used to warn user given territory is unknown
     */
    UNKNOWN_TERRITORY,
    /**
     * Message key used to warn user given affiliation type (for organizations) is unknown
     */
    UNKNOWN_AFFILIATION_TYPE,
    /**
     * Message key used to warn user given legal status (for organizations) is unknown
     */
    UNKNOWN_LEGALSTATUS,
    /**
     * Message key used to warn user given business sector (for organizations) is unknown
     */
    UNKNOWN_SECTOR,
    /**
     * Message key used to warn user given profession id is unknown
     */
    UNKNOWN_PROFESSION,
    /**
     * Message key used to warn user given actor type is unknown
     */
    UNKNOWN_ACTOR_TYPE,
    /**
     * Message key used to warn user given actor role is unknown
     */
    UNKNOWN_ACTOR_ROLE,
    /**
     * Message key used to warn user given argument type is unknown
     */
    UNKNOWN_ARGUMENT_TYPE,
    /**
     * Message key used to warn user given argument type is unknown
     */
    UNKNOWN_ARGUMENT_SHADE_TYPE,
    /**
     * Message key used to warn user given debate type is unknown
     */
    UNKNOWN_DEBATE_TYPE,
    /**
     * Message key used to warn user given debate type is unknown
     */
    UNKNOWN_DEBATE_SHADE_TYPE,
    /**
     * Message key used to warn user given justification link shade is unknown
     */
    UNKNOWN_JUSTIFICATION_SHADE,
    /**
     * Message key used to warn user given position link shade is unknown
     */
    UNKNOWN_POSITION_SHADE,
    /**
     * Message key used to warn user given similarity link shade is unknown
     */
    UNKNOWN_SIMILARITY_SHADE,
    /**
     * Message key used to warn user given tag is unknown
     */
    UNKNOWN_TAG,
    /**
     * Message key used to warn user given tag link is unknown
     */
    UNKNOWN_TAG_LINK,
    /**
     * Message key used to warn user given topic id is unknown
     */
    UNKNOWN_TOPIC,
    /**
     * Message key used to warn user given language is unknown
     */
    UNKNOWN_LANGUAGE,
    /**
     * Message key used to warn user given text visibility is unknown
     */
    UNKNOWN_TEXT_VISIBILITY,
    /**
     * Message key used to warn user given validation state is unknown
     */
    UNKNOWN_VALIDATION_STATE,
    /**
     * Message key used to warn user given modification status is unknown
     */
    UNKNOWN_MODIFICATION_STATUS,
    /**
     * Message key used to warn user given precision date type is unknown
     */
    UNKNOWN_PRECISION_DATE_TYPE,
    /**
     * Message key used to warn user given picture licence type is unknown
     */
    UNKNOWN_PICTURE_LICENCE_TYPE,
    /**
     * Message key used to warn user given picture licence source is unknown
     */
    UNKNOWN_PICTURE_SOURCE_TYPE,
    /**
     * Message key used to warn user given date has not the expected format (more details should be found in exception message)
     */
    DATE_ERROR,
    /**
     * Message key used to warn user given contribution as the wrong contribution type in this context
     */
    WRONG_CONTRIBUTION_TYPE,
    /**
     * Message key used to warn user a given actor name matches many entries in the database so an id must be specified
     */
    AMBIGUOUS_ACTOR_NAME
  }

  /**
   * Construct a Format exception with a key pointing describing the error
   *
   * @param key key that may be used to display a i18n message to the user
   */
  public FormatException(Key key) {
    super(key.name().toLowerCase().replace("_", "."));
  }

  /**
   * Construct a Format exception with the name of the field in error
   *
   * @param key key that may be used to display a i18n message to the user
   * @param reason a message about the field in errors (and its content)
   */
  public FormatException(Key key, String reason) {
    super(key.name().toLowerCase().replace("_", "."), reason);
  }
}
