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

package be.webdeb.presentation.web.controllers.viz.debate;

import be.webdeb.core.api.debate.Debate;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

/**
 * This class holds debates values to build visualisation pages
 *
 * @author Martin Rouffiange
 */
public class DebateVizHolder extends DebateHolder {

    /**
     * Default Constructor
     *
     * @param debate the debate to visualize
     * @param lang 2-char ISO 639-1 code of context language (among play accepted languages)
     */
    public DebateVizHolder(Debate debate, WebdebUser user, String lang) {
        super(debate, user, lang);
        this.debate = debate;
    }



}
