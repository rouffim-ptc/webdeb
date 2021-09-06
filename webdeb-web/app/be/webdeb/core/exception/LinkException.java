/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.exception;

/**
 * This exception is used to warn that the user want to save a contribution link, but what he sent contains errors.
 *
 * @author Martin Rouffiange
 */
public class LinkException extends Exception{

    private Key type;

    /**
     * List of acceptable keys for link exceptions
     */
    public enum Key {
        /**
         * Message key used to warn user that a needed related object if not retrieved
         */
        LINKED_OBJECT,
        /**
         * Message key used to warn user that he is not authorized to do the action
         */
        UNAUTHORIZED,
        /**
         * Message key used to warn user that an internal error happen
         */
        INTERNAL_ERROR
    }

    /**
     * Construct aLink exception with a key pointing describing the error
     *
     * @param key key that may be used to take a specific action
     */
    public LinkException(Key key) {
        super();
        type = key;
    }

    public Key getType() {
        return type;
    }
}
