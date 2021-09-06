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

package be.webdeb.core.api.argument;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.exception.FormatException;

/**
 * This interface represents an argument dictionary in the webdeb system.
 * An argument dictionary, is the title and language of argument to ensure the unique title / language link.
 *
 * @author Martin Rouffiange
 */
public interface ArgumentDictionary extends Contribution {

    /**
     * Get the argument title
     *
     * @return a title
     */
    String getTitle();

    /**
     * Set the argument title
     *
     * @param title a title
     */
    void setTitle(String title);

    /**
     * Get the argument title language
     *
     * @return a Language object
     *
     * @see Language
     */
    Language getLanguage();

    /**
     * Set the argument title language
     *
     * @param language a Language object
     * @see Language
     */
    void setLanguage(Language language);

}
