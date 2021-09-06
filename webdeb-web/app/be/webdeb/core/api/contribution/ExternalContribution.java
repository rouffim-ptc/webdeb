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

package be.webdeb.core.api.contribution;

import be.webdeb.core.api.actor.ExternalAuthor;

import java.util.List;

/**
 * This interface represents a external contribution.
 * It's a temporary contribution that is keep for an easiest communication between services.
 *
 * @author Martin Rouffiange
 */

public interface ExternalContribution extends Contribution {

    /**
     * Get the url where the external contribution comes from
     *
     * @return the external source url
     */
    String getSourceUrl();

    /**
     * Set the url where the external contribution comes from
     *
     * @param url the external source url
     */
    void setSourceUrl(String url);

    /**
     * Get the source id where the external contribution comes from
     *
     * @return the external source id
     */
    int getSourceId();

    /**
     * Set the source id where the external contribution comes from
     *
     * @param source the external source id
     */
    void setSourceId(Integer source);

    /**
     * Get the source name where the external contribution comes from
     *
     * @return the external source name
     */
    String getSourceName();

    /**
     * Get the internal contribution id
     *
     * @return the internal contribution id if any
     */
    Long getInternalContribution();

    /**
     * Set the internal contribution id
     *
     * @param id the internal contribution id
     */
    void setInternalContribution(Long id);

    /**
     * Get the argument language
     *
     * @return a Language object
     *
     * @see Language
     */
    Language getLanguage();

    /**
     * Set the argument language
     *
     * @param language a Language object
     * @see Language
     */
    void setLanguage(Language language);

    /**
     * Set the ContributionType
     *
     * @param type the Contribution type object for this contribution
     */
    void setContributionType(ContributionType type);

    /**
     * Get the title of this external contribution
     *
     * @return contribution title
     */
    String getTitle();

    /**
     * Set the title of this external contribution
     *
     * @param title the contribution title
     */
    void setTitle(String title);

}
