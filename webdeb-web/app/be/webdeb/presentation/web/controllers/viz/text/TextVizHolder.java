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

package be.webdeb.presentation.web.controllers.viz.text;

import be.webdeb.core.api.text.Text;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;


/**
 * This class holds text values to build visualisation pages.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class TextVizHolder extends TextHolder {

  /**
   * Construct a Text viz holder with given text and in given interface language
   *
   * @param text an API text
   * @param lang two-char ISO 639-1 code of the user interface language
   */
  public TextVizHolder(Text text, WebdebUser user, String lang) {
    super(text, user, lang);
    this.text = text;
  }


}
