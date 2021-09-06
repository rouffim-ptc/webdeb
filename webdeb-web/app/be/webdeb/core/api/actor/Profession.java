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

package be.webdeb.core.api.actor;

import java.util.Map;

/**
 * This interface represents a profession (or function) for an individual (contributor or personal actor)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface Profession {

  /**
   * Get the profession id
   *
   * @return an int representing a profession
   */
  int getId();

  /**
   * Get the profession type as EProfessionType
   *
   * @return the profession type as EProfessionType
   * @see EProfessionType
   */
  EProfessionType getType();

  /**
   * Set the profession type as EProfessionType
   *
   * @param type the profession type as EProfessionType
   * @see EProfessionType
   */
  void setType(EProfessionType type);

  /**
   * Get the profession super link
   *
   * @return the profession that is the super link (may be null)
   */
  Profession getSuperLink();

  /**
   * Get the profession super link without setting its value (if null) before return it
   *
   * @return the profession that is the super link (may be null)
   */
  Profession getSuperLinkWithoutSetting();

  /**
   * Set the profession super link
   *
   * @param profession the profession that is the super link of given profession
   */
  void setSuperLink(Profession profession);

  /**
   * Match if a given name corresponding to the profession
   *
   * @param name the name to match
   * @return true is name is matched
   */
  boolean matchName(String name);

  /**
   * Get the name of this profession (masculine gender by default)
   *
   * @param lang a two-char ISO-639-1 code representing the language for the name
   * @return a name for this Profession object
   */
  String getName(String lang);

  /**
   * Get the simple name of this profession (neutral gender by default)
   *
   * @param lang a two-char ISO-639-1 code representing the language for the name
   * @return a name for this Profession object
   */
  String getSimpleName(String lang);

  /**
   * Get the name of this profession by gender (by default : neuter, masculine or feminine)
   *
   * @param gender a char representing the gender of the name
   * @return a map of (2-char ISO-639-1 code, name in this language) of this profession
   */
  Map<String, String> getNameByGender(String gender);

  /**
   * Get the name of this profession
   *
   * @param lang a two-char ISO code representing the language for the name
   * @param gender a char representing the gender of the name
   * @return a name for this Profession object
   */
  String getName(String lang, String gender);

  /**
   * Get the complete description name of profession with declinations of gender
   *
   * @param lang a two-char ISO code representing the language for the name
   * @return a complete gender name for this Profession object
   */
  String getGendersNames(String lang);

  /**
   * Get the whole map of names in all known languages
   *
   * @return a map of (2-char ISO-639-1 code, map of (gender of the name, name in this language)) of this profession
   */
  Map<String, Map<String, String>> getNames();

  /**
   * Add a new spelling for this profession. If such a spelling existed, simply overwrite it.
   *
   * @param name a lang-dependent spelling for this profession
   * @param code the addition of a two char ISO 639-1 code and a char code (lang + gender)
   */
  void addName(String name, String code);

  /**
   * Add a new spelling for this profession. If such a spelling existed, simply overwrite it.
   *
   * @param name a lang-dependent spelling for this profession
   * @param lang a two char ISO 639-1 code
   * @param gender a char code
   */
  void addName(String name, String lang, String gender);

  /**
   * Check whether this profession must displayed its hierarchy
   *
   * @return true if this profession must displayed its hierarchy
   */
  boolean isDisplayHierarchy();

  /**
   * Set whether this profession must displayed its hierarchy
   *
   * @param displayHierarchy true if this profession must displayed its hierarchy
   */
  void setDisplayHierarchy(boolean displayHierarchy);

  /**
   * Set the profession sub links
   *
   * @param links a map of profession sub links
   */
  void setLinks(Map<Integer, Profession> links);

  /**
   * Get the whole map of profession's links
   *
   * @return a map of (int profession id, profession) related with this profession
   */
  Map<Integer, Profession> getLinks();

  /**
   * Add a new link for this profession. If such a link existed, simply overwrite it.
   *
   * @param profession the profession to linked with this one
   */
  void addLink(Profession profession);

  /**
   * Get the substitute (ie equivalent) profession, if any. Used when grouping or filtering on functions
   *
   * @return a (possibly null) profession that may be used as a substitute for this profession
   */
  Profession getSubstitute();

  /**
   * Check if a profession has a given name

   * @param name the name to check
   * @return true if the profession has the given name
   */
  boolean professionHasName(String name);

  /**
   * Build the hierarchy of function as String. Ex : "President -> Federal President -> ..."

   * @param lang the addition of a two char ISO 639-1 code and a char code (lang + gender)
   * @return a map of profession names and boolean to displayHierarchy with the given profession itself, or empty list if profession is not found
   */
  Map<String, Boolean> getFunctionHierarchy(String lang);
}
