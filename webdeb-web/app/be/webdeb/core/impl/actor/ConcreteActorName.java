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

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.ActorName;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.impl.contribution.AbstractContribution;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the actor name interface, used to handle multiple names for actors
 * (with different spellings). Lang mus be set prior to names
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteActorName implements ActorName {

  private String first;
  private String last;
  private String pseudo;
  private String lang;

  ConcreteActorName(String lang) {
    this.lang = lang.trim();
  }

  @Override
  public String getFirst() {
    return first;
  }

  @Override
  public void setFirst(String first) {
    // first names may not contain dots (on top of other sanitizing rules)
    String cleaned = cleanupName(first, AbstractContribution.MAX_FIRSTNAME_SIZE);
    this.first = cleaned != null ? cleaned.replace(".", "") : null;
  }

  @Override
  public String getLast() {
    return last;
  }

  @Override
  public void setLast(String last) {
    this.last = cleanupName(last, AbstractContribution.MAX_NAME_SIZE);
  }

  @Override
  public String getPseudo() {
    return pseudo;
  }

  @Override
  public void setPseudo(String pseudo) {
    this.pseudo = cleanupName(pseudo, AbstractContribution.MAX_NAME_SIZE);
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public String getFullName(EActorType type) {
    if (type.equals(EActorType.PERSON)) {
      // first and last name if any
      if (last != null && !"".equals(last)) {
        return first + " " + last + (pseudo != null && !"".equals(pseudo) ? " (" + pseudo + ")" : "");
      }
      else if (pseudo != null && !"".equals(pseudo)) {
        // pseudo only
        return pseudo;
      }
      else if(first != null){
        // first name only
        return first;
      }
      return "unknown";
    }else if (type.equals(EActorType.ORGANIZATION)) {
      if(first != null){
        // acronyme first and name in brackets
        return first + " - " + last;
      }
    }
    // other types (unknown or organizations without acro)
    return last == null ? "" : last;
  }

  @Override
  public List<String> isValid(EActorType type) {
    List<String> fieldsInError = new ArrayList<>();
    if (EActorType.PERSON.equals(type)) {
      if ((last == null || "".equals(last)) && (pseudo == null || "".equals(pseudo))) {
        fieldsInError.add("last or pseudo must not be null");
      }
    } else {
      if (last == null || "".equals(last)) {
        fieldsInError.add("last is null");
      }
    }
    return fieldsInError;
  }

  @Override
  public String toString() {
    return (first != null ? first + " " : "") + last
        + (pseudo != null ? " (" + pseudo + ") " : " ")
        + "[" + lang + "]";
  }

  /**
   * Clean up given name by
   * <ul>
   *   <li>trimming blanks</li>
   *   <li>truncating it at given size</li>
   *   <li>capitalizing first letter</li>
   *   <li>and replacing "’" by "'"</li>
   * </ul>
   *
   * @param name the name to cleanup
   * @param size max size for the given name (will be shortened if exceeding)
   * @return given name cleaned as specified above
   */
  private String cleanupName(String name, int size) {
    if (name != null && !"".equals(name.trim())) {
      String sanitized = name.trim();
      sanitized = sanitized.length() > size ? sanitized.substring(0, size - 1) : sanitized;
      return sanitized.replace("’", "'");
    }
    return null;
  }
}
