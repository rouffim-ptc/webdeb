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

import be.webdeb.core.exception.FormatException;

/**
 * This interface represents a shaded argument in the webdeb system. A shaded argument is an argument preceded by
 * an argument shade.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface ArgumentShaded extends Argument {

    /**
     * Get this Argument shade
     *
     * @return an ArgumentType object
     * @see ArgumentType
     */
    ArgumentShade getArgumentShade();

    /**
     * Set this Argument shade
     *
     * @param argumentShade an ArgumentShade object
     * @throws FormatException if the passed ArgumentType is not valid
     * @see ArgumentShade
     */
    void setArgumentShade(ArgumentShade argumentShade) throws FormatException;

    /**
     * Get the fully readable title with the given shade
     *
     * @param shade the shade to display with the argument title
     * @return a fully readable reconstructed title with given shade
     *
     * @throws FormatException if given shade is not valid
     */
    String getFullTitle(EArgumentShade shade) throws FormatException;

}
