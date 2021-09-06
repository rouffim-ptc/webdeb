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

package be.webdeb.core.impl.contributor;

import be.webdeb.core.api.contributor.GroupColor;
import org.slf4j.Logger;

/**
 * This class implements the GroupColor abstraction.
 *
 * @author Martin Rouffiange
 */
class ConcreteGroupColor implements GroupColor {

  private int idColor;
  private String colorCode;
  protected static final Logger logger = play.Logger.underlying();
  /**
   * Constructor
   *
   * @param idColor a group color id
   * @param colorCode a color code
   */
  public ConcreteGroupColor(int idColor, String colorCode) {
    this.idColor = idColor;
    this.colorCode = colorCode;
  }

  @Override
  public int getIdColor() {
    return this.idColor;
  }

  @Override
  public String getColorCode() {
    return "#"+this.colorCode;
  }
}
