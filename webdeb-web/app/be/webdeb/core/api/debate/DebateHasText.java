/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 * 	
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 * 	
 * This program (WebDeb) is free software: 
 * you can redistribute it and/or modify it under the terms 
 * of the GNU General License as published by the 
 * Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 *
 * You should have received a copy of the GNU General License
 * along with this program (see COPYING file).  If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.api.debate;

import be.webdeb.core.api.contribution.link.ContributionLink;
import be.webdeb.core.api.text.Text;

/**
 * This interface represents a link between a debate and a text. It used to add text to a debate in bibliography section.
 *
 * @author Martin Rouffiange
 */
public interface DebateHasText extends ContributionLink {

    /**
     * Get the debate of this link
     *
     * @return a debate
     */
    Debate getDebate();

    /**
     * Set the debate of this link
     *
     * @param debate a debate
     */
    void setDebate(Debate debate);

    /**
     * Get the text of the link
     *
     * @return a text
     */
    Text getText();

    /**
     * Set the text of the link
     *
     * @param text a text
     */
    void setText(Text text);
}
