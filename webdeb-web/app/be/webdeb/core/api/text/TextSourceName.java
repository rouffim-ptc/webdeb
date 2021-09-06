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

package be.webdeb.core.api.text;

/**
 * Simple interface representing a text source name, i.e., the name of a source where a text can be found
 * (like magazines, journals, etc.).
 *
 * @author Fabian Gilson
 */
public interface TextSourceName {

  /**
   * Get the source id
   * @return this source unique id
   */
  int getSourceId();

  /**
   * Set the source id
   * @param id this source id
   */
  void setSourceId(int id);

  /**
   * Get this source name
   * @return the full name of this text source
   */
  String getSourceName();

  /**
   * Set the source name
   * @param name a name for this text source
   */
  void setSourceName(String name);

}
