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

package be.webdeb.core.api.contribution.link;

import be.webdeb.core.api.contribution.Contribution;

/**
 * This interface represents the links between two contributions. It can represents any kind of link in the webdeb system.
 *
 * @author Martin Rouffiange
 */
public interface ContributionLink extends Contribution {

    /**
     * Get the origin contribution id of this link (or context).
     *
     * @return the origin contribution id of this link.
     */
    Long getOriginId();

    /**
     * Set the origin contribution id of this link (or context).
     *
     * @param originId the origin contribution id of this link.
     */
    void setOriginId(Long originId);

    /**
     * Get the destination contribution id of this link.
     *
     * @return the origin destination id of this link.
     */
    Long getDestinationId();

    /**
     * Set the destination contribution id of this link.
     *
     * @param destinationId the destination contribution id of this link.
     */
    void setDestinationId(Long destinationId);

    /**
     * Get the order of the link
     *
     * @return the order of the link
     */
    int getOrder();

    /**
     * Set the order of the link
     *
     * @param order the order of the link
     */
    void setOrder(int order);
}
