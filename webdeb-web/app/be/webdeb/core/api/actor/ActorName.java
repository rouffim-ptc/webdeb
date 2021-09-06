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

package be.webdeb.core.api.actor;

import java.util.List;

/**
 * This interface represents a list of names for an actor since actor may have names spelled differently
 * regarding a language.
 *
 * For persons, either both the first and last or (inclusive) pseudo are set
 * For organizations, they may have a last (names) and possibly a first (acronym), but no pseudo
 * For unknown actor type, they only have a last.
 *
 * Since names are language dependents, the language parameter must be set prior to any other field.
 *
 * @author Fabian Gilson
 */
public interface ActorName {

  /**
   * Get the person's last name, organization's full name or unknown actor's full name
   *
   * @return a name
   */
  String getLast();

  /**
   * Set the person's last name, organization's full name or unknown actor's full name
   *
   * @param last a name
   */
  void setLast(String last);

  /**
   * Get the person's first name or organization's acronym. Unset of unknown actor type.
   *
   * @return a name
   */
  String getFirst();

  /**
   * Set the person's first name or organization's acronym. Unset of unknown actor type.
   *
   * @param first a name
   */
  void setFirst(String first);

  /**
   * Get the person's pseudo. Unset for organizations and unknown actors.
   *
   * @return a pseudonym
   */
  String getPseudo();

  /**
   * Set the person's pseudo. Unset for organizations and unknown actors.
   *
   * @param pseudo an actor pseudonym
   */
  void setPseudo(String pseudo);

  /**
   * Get the 2-char language ISO code corresponding to the spelling of this name
   *
   * @return a 2-char ISO code for the language (e.g. fr, en, nl,...)
   */
  String getLang();

  /**
   * Get a string representation of this actor name for given actorytype, ie,
   *    a pseudonym, if no last name is set (for persons);
   *    a concatenation of first and last if both are set + pseudo between brackets if pseudo is also set (persons);
   *    the last name only for organizations and actors of unknown type
   *
   * @param type an actor type
   * @return the actor's full name according to above description and given type
   */
  String getFullName(EActorType type);

  /**
   * Check whether this name is valid for a given actor type
   *
   * For persons, either both the first and last or (inclusive) pseudo are set.
   * For organizations, they may have a last (names) and possibly a first (acronym), but no pseudo
   * For unknown actor type, they only have a last.
   *
   * @param type an actor type
   * @return an empty list if no error found, a list of fields in error in case of the above properties are not met.
   */
  List<String> isValid(EActorType type);

}
