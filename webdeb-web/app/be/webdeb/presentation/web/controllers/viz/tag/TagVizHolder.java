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

package be.webdeb.presentation.web.controllers.viz.tag;

import be.webdeb.core.api.tag.Tag;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

/**
 * This class holds tags values to build visualisation pages
 *
 * @author Martin Rouffiange
 */
public class TagVizHolder extends TagHolder {

  /**
   * Default Constructor
   *
   * @param tag the tag to visualize
   * @param lang 2-char ISO 639-1 code of context language (among play accepted languages)
   */
  public TagVizHolder(Tag tag, WebdebUser user, String lang) {
    super(tag, user, lang);
    this.tag = tag;
  }



}
