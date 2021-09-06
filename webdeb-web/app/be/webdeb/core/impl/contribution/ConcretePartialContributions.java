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

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.PartialContributions;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a PartialContributions in the webdeb system.
 *
 * @author Martin Rouffiange
 */
public class ConcretePartialContributions<E> implements PartialContributions<E> {

    private int nbLoadedContributions;
    private List<E> contributions;

    public ConcretePartialContributions() {
        this.nbLoadedContributions = 0;
        this.contributions = new ArrayList<>();
    }

    @Override
    public int getNumberOfLoadedContributions() {
        return nbLoadedContributions;
    }

    @Override
    public List<E> getContributions() {
        return contributions;
    }

    @Override
    public void setNumberOfLoadedContributions(int nbLoadedContributions) {
        this.nbLoadedContributions = nbLoadedContributions;
    }

    @Override
    public void setContributions(List<E> contributions) {
        this.contributions = contributions;
    }
}
