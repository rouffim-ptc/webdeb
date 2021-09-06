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

package be.webdeb.presentation.web.controllers.viz;

import be.webdeb.presentation.web.controllers.entry.ContextContributionHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;

import java.util.Collection;
import java.util.List;

/**
 * This class containes list and collection to make the sociography content and filters
 *
 * @author Martin Rouffiange
 */
public class Sociography {

    private List<CitationHolder> citations;
    private Collection<ContextContributionHolder.SociographyNode> viewed;

    /**
     * Construct a actor sociography for view
     *
     * @param citations the list of citation holder concerned (for make filters)
     * @param viewed a formated collection for the view
     */
    public Sociography(List<CitationHolder> citations, Collection<ContextContributionHolder.SociographyNode> viewed){
        this.citations = citations;
        this.viewed = viewed;
    }

    /**
     * Get the list of citation holder concerned (for make filters)
     *
     * @return the possibly empty list of citation holder concerned (for make filters)
     */
    public List<CitationHolder> getCitations() {
        return citations;
    }

    /**
     * Get the formated collection for the view
     *
     * @return the possibly empty collection for the view
     */
    public Collection<ContextContributionHolder.SociographyNode> getViewed() {
        return viewed;
    }

    @Override
    public String toString() {
        return "Sociography{" +
                "citations=" + citations +
                ", viewed=" + viewed +
                '}';
    }
}