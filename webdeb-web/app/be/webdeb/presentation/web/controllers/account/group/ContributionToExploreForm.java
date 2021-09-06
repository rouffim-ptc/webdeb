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

package be.webdeb.presentation.web.controllers.account.group;

import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.util.ValuesHelper;
import org.jetbrains.annotations.NotNull;
import play.api.Play;

import javax.inject.Inject;

public class ContributionToExploreForm implements Comparable<ContributionToExploreForm> {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();
    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);
    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    // Note: as for all wrappers, all fields MUST hold empty values for proper form validation
    protected Long contributionToExploreId = -1L;

    protected Long id = -1L;
    protected ContributionHolder contribution;
    protected String contributionName;

    protected int order = 1;

    /**
     * Play / JSON compliant constructor
     */
    public ContributionToExploreForm() { }

    /**
     * Constructor from a given contribution to explore
     *
     * @param contribution a contribution
     * @param user a WebDeb user
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public ContributionToExploreForm(ContributionToExplore contribution, WebdebUser user, String lang) {
        this.contributionToExploreId = contribution.getContributionToExploreId();

        this.id = contribution.getContributionId();
        this.contribution = helper.toHolder(contribution.getContribution(), user, lang, true);
        this.contributionName = this.contribution != null ? this.contribution.getContributionTitle() : "";

        this.order = contribution.getOrder();
    }

    @Override
    public int compareTo(@NotNull ContributionToExploreForm o) {
        return Integer.compare(order, o.getOrder());
    }

    public boolean isEmpty(){
        return values.isBlank(id) && values.isBlank(contributionToExploreId);
    }

    /*
     * GETTERS and SETTERS
     */

    public Long getContributionToExploreId() {
        return contributionToExploreId;
    }

    public void setContributionToExploreId(Long contributionToExploreId) {
        this.contributionToExploreId = contributionToExploreId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContributionHolder getContribution() {
        return contribution;
    }

    public String getContributionName() {
        return contributionName;
    }

    public void setContributionName(String contributionName) {
        this.contributionName = contributionName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "ContributionToExploreForm{" +
                "contributionToExploreId=" + contributionToExploreId +
                ", id=" + id +
                ", contributionName='" + contributionName + '\'' +
                ", order=" + order +
                '}';
    }
}
