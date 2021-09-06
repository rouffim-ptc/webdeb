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
import be.webdeb.core.api.contribution.link.SimilarityLink;
import be.webdeb.core.api.contribution.link.SimilarityLinkType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;

/**
 * This class implements a SimilarityLink
 *
 * @author Martin Rouffiange
 */
public abstract class AbstractSimilarityLink<T extends ContributionFactory,V extends ContributionAccessor> extends AbstractContributionLink<T, V> implements SimilarityLink {

    protected SimilarityLinkType linkType;

    /**
     * Constructor
     *
     * @param factory       a ContributionFactory the factory to construct concrete instances
     * @param accessor      a ContributionAccessor the accessor to retrieve and persist concrete Contribution
     */
    public AbstractSimilarityLink(T factory, V accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
    }

    @Override
    public SimilarityLinkType getLinkType() {
        return linkType;
    }

    @Override
    public void setLinkType(SimilarityLinkType linkType) {
        this.linkType = linkType;
    }

    @Override
    public String toString() {
        return "similarity " + super.toString() + " with type " + linkType.getType();
    }

    @Override
    public int hashCode() {
        return super.hashCode() + linkType.getType().hashCode();
    }
}