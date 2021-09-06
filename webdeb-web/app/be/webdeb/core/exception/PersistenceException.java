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
 * This exception is used to warn that a persistence action could not be performed. By default, persistence
 * actions are performed in transaction as a unique action, such that if such an exception occurs, no other
 * element that should have been persisted in the same action has been saved, i.e., if needed, a rollback as
 * been performed.
 *
 * Message keys used as parameters should be added in i18n dedicated messages.xx files, in order to be correctly
 * processed by the XXActions classes (Controllers layer).
 *
 * The mapping between key names and messages.xx property names is of the form "persistence.error." + Key.name.replace("_", ".")
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class PersistenceException extends AbstractException {

  private static final long serialVersionUID = 1L;

  /**
   * The enumeration values to give details regarding persistence exceptions
   */
  public enum Key {
    /**
     * Message key to warn users about an error while saving contributor
     */
    SAVE_CONTRIBUTOR,
    /**
     * Message key to warn users about an error while saving tmp contributor
     */
    SAVE_TMP_CONTRIBUTOR,
    /**
     * Message key to warn users about an error while saving changes to contributor profile, pwd, etc.
     */
    UPDATE_CONTRIBUTOR,
    /**
     * Message key to warn users about an error while binding contributor to a contribution
     */
    BIND_CONTRIBUTOR,
    /**
     * Message key to warn users about an error while binding actor to a contribution
     */
    BIND_ACTOR,
    /**
     * Message key to warn users about an error while unbinding actor to a contribution
     */
    UNBIND_ACTOR,
    /**
     * Message key to warn users about an error while saving a source for a text
     */
    SAVE_SOURCE,
    /**
     * Message key to warn users about an error while saving an external contribution
     */
    SAVE_EXTERNAL,
    /**
     * Message key to warn users about an error while saving a contributor picture
     */
    SAVE_PICTURE,
    /**
     * Message key to warn users about an error while saving an affiliation for an actor
     */
    SAVE_AFFILIATION,
    /**
     * Message key to warn users about an error while removing an affiliation for an actor
     */
    REMOVE_AFFILIATION,
    /**
     * Message key to warn users about an error while saving a context contribution
     */
    SAVE_CONTEXT_CONTRIBUTION,
    /**
     * Message key to warn users about an error while saving actor
     */
    SAVE_ACTOR,
    /**
     * Message key to warn users about an error while saving an argument
     */
    SAVE_ARGUMENT,
    /**
     * Message key to warn users about an error while saving an argument dictionary
     */
    SAVE_ARGUMENT_DICTIONARY,
    /**
     * Message key to warn users about an error while saving a citation
     */
    SAVE_CITATION,
    /**
     * Message key to warn users about an error while saving an external citation
     */
    SAVE_EXTERNAL_CITATION,
    /**
     * Message key to warn users about an error while saving an citation
     */
    SAVE_DEBATE,
    /**
     * Message key to warn users about an error while saving justification links in a context
     */
    SAVE_JUSTIFICATION_LINK,
    /**
     * Message key to warn users about an error while saving position links in a debate
     */
    SAVE_POSITION_LINK,
    /**
     * Message key to warn users about an error while saving similarity link
     */
    SAVE_SIMILARITY_LINK,
    /**
     * Message key to warn users about an error while saving illustration links
     */
    SAVE_ILLUSTRATION_LINK,
    /**
     * Message key to warn users about an error while saving debate links
     */
    SAVE_DEBATE_LINK,
    /**
     * Message key to warn users about an error while saving debate has text link
     */
    SAVE_DEBATE_HAS_TEXT_LINK,
    /**
     * Message key to warn users about an error while saving context has category links
     */
    SAVE_CONTEXT_HAS_CATEGORY_LINK,
    /**
     * Message key to warn users about an error while saving context has subdebate links
     */
    SAVE_CONTEXT_HAS_SUBDEBATE_LINK,
    /**
     * Message key to warn users about an error while saving a text
     */
    SAVE_TEXT,
    /**
     * Message key to warn users about an error while saving a external text
     */
    SAVE_EXTERNAL_TEXT,
    /**
     * Message key to warn users about an error while saving a tag
     */
    SAVE_TAG,
    /**
     * Message key to warn users about an error while saving a link between two tags
     */
    SAVE_TAG_LINK,
    /**
     * Message key to warn users about an error while saving a project
     */
    SAVE_PROJECT,
    /**
     * Message key to warn users about an error while saving a project group
     */
    SAVE_PROJECT_GROUP,
    /**
     * Message key to warn users about an error while saving a project subgroup
     */
    SAVE_PROJECT_SUBGROUP,
    /**
     * Message key to warn users about an error while saving a contribution to explore
     */
    SAVE_CONTRIBUTION_TO_EXPLORE,
    /**
     * Message key to warn users about an error while saving an advice
     */
    SAVE_ADVICE,
    /**
     * Message key to warn users about an error while saving topics
     */
    SAVE_TOPIC,
    /**
     * Message key to warn users about an error while deleting a contribution
     */
    DELETE_CONTRIBUTION,
    /**
     * Message key to warn users about an error while adding a member to a group
     */
    ADD_MEMBER,
    /**
     * Message key to warn users about an error while removing a member from a group
     */
    REMOVE_MEMBER,
    /**
     * Message key to warn users about an error while setting default group
     */
    DEFAULT_GROUP,
    /**
     * Message key to warn users about an error while saving group
     */
    SAVE_GROUP,
    /**
     * Message key to warn users about an error while inviting in group
     */
    INVITE_GROUP,
    /**
     * Message key to warn users about an error while saving marks for a contribution
     */
    MARK_GROUP,
    /**
     * Message key to warn users about a type mismatch in contribution merging tentative
     */
    MERGE_MISMATCH,
    /**
     * Message key to warn users about an error in contribution merging tentative
     */
    MERGE,
    /**
     * Message key to warn users about a error in merge professions
     */
    MERGE_PROFESSIONS,
    /**
     * Message key to warn admin about a duplicate tweet
     */
    DUPLICATE_TWEET,
    /**
     * Message key to warn users that a requested object was not found
     */
    NOT_FOUND,
    /**
     * Message key to warn user a submitted object to persist has been updated in the meantime (optimistic lock)
     */
    OUTDATED_VERSION,
    /**
     * Message key to warn users about an error while saving a free copyright source
     */
    FREESOURCE_SAVE,
    /**
     * Message key to warn users about an error while remove a free copyright source
     */
    FREESOURCE_DELETE,
    /**
     * An error occured when try to change follow group state
     */
    FOLLOW_GROUP_ERROR,
    /**
     * The user try to unfollow his default group
     */
    UNFOLLOW_DEFAULT_GROUP
  }

  /**
   * Default constructor with a message key.
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   */
  public PersistenceException(Key key) {
    super("persistence.error." + key.name().toLowerCase().replace("_", "."));
  }

  /**
   * Constructor used to pack a root exception.
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   * @param more a second message giving more details on the error
   */
  public PersistenceException(Key key, String more) {
    this(key);
    setMore(more);
  }

  /**
   * Constructor used to pack a root exception.
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   * @param t the root cause of this exception
   */
  public PersistenceException(Key key, Throwable t) {
    this(key);
    initCause(t);
  }

  /**
   * Constructor used to pack a root exception and more details.
   *
   * @param key a key to an explanation of the error, that may be used to display a i18n message to user
   * @param more a second message giving more details on the error
   * @param t the root cause of this exception
   */
  public PersistenceException(Key key, String more, Throwable t) {
    this(key, t);
    setMore(more);
  }
}
