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

import be.webdeb.core.exception.FormatException;

/**
 * This interface represents a physical person being an Actor in the webdeb database.
 *
 * @author Fabian Gilson
 */
public interface Person extends Individual, Actor {

  /**
   * Get this Person's birth date (may be null)
   * A birth date is in DD/MM/YYYY format, day and month are optional
   *
   * @return the birth date, null if unset
   */
  String getBirthdate();

  /**
   * Set this Person's birth date
   * A birth date is in DD/MM/YYYY format, day and month are optional

   * @param date a date representing this Person's birth date
   * @throws FormatException  if the given date does not have the expected format
   */
  void setBirthdate(String date) throws FormatException;

  /**
   * Get this Person's date of death (may be null)
   * A birth date is in DD/MM/YYYY format, day and month are optional
   *
   * @return the date of death, null if unset
   */
  String getDeathdate();

  /**
   * Set this Person's date of death
   * A birth date is in DD/MM/YYYY format, day and month are optional

   * @param date a date representing this Person's date of death
   * @throws FormatException  if the given date does not have the expected format
   */
  void setDeathdate(String date) throws FormatException;

}
