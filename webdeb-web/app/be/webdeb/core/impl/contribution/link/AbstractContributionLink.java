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

package be.webdeb.core.impl.contribution.link;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.link.ContributionLink;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a ContributionLink
 *
 * @author Martin Rouffiange
 */
public abstract class AbstractContributionLink<T extends ContributionFactory, V extends ContributionAccessor> extends AbstractContribution<T, V> implements ContributionLink {

    protected Long originId;
    protected Long destinationId;
    protected int order;

    /**
     * Constructor
     *
     * @param factory       a ContributionFactory the factory to construct concrete instances
     * @param accessor      a ContributionAccessor the accessor to retrieve and persist concrete Contribution
     */
    public AbstractContributionLink(T factory, V accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        this.order = 0;
    }

    @Override
    public Long getOriginId() {
        return originId;
    }

    @Override
    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    @Override
    public Long getDestinationId() {
        return destinationId;
    }

    @Override
    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }


    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();

        if(originId == null || originId == -1L) {
            fieldsInError.add("origin contribution is null");
        }
        if(destinationId == null || destinationId == -1L) {
            fieldsInError.add("destination contribution is null");
        }
        return fieldsInError;
    }

    @Override
    public String toString() {
        return "link [" + id + "] between origin " + originId + " and destination " + destinationId + " and order " + order;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + originId.hashCode() + destinationId.hashCode();
    }
}