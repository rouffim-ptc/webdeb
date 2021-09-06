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

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EClaimType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import play.api.Play;

import javax.inject.Inject;
import java.util.Date;

public class ClaimHolder {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);

    @Inject
    private ContributorFactory contributorFactory = Play.current().injector().instanceOf(ContributorFactory.class);

    Long contributionId;
    String contributionTitle;
    EContributionType contributionType;

    Long contributorId;
    String contributorName;

    EClaimType type;
    String group;
    String comment;
    String url;

    Date date;

    public ClaimHolder(Long contributionId, Long contributorId, EClaimType type, String group, String comment, String url, Date date, String lang) {
        this.contributionId = contributionId;
        this.contributorId = contributorId;
        this.type = type;
        this.group = group;
        this.comment = comment;
        this.url = url;
        this.date = date;

        try {
            Contribution c = actorFactory.retrieveContribution(contributionId);
            this.contributionTitle = c.getContributionTitle(lang);
            this.contributionType = c.getType();

            this.contributorName = contributorFactory.retrieveContributor(contributorId).getPseudo();
        } catch (Exception e) {
            this.contributionTitle = "Contribution without title";
            this.contributorName = "Contributor without pseudo";
        }
    }

    public Long getContributionId() {
        return contributionId;
    }

    public String getContributionTitle() {
        return contributionTitle;
    }

    public EContributionType getContributionType() {
        return contributionType;
    }

    public Long getContributorId() {
        return contributorId;
    }

    public String getContributorName() {
        return contributorName;
    }

    public EClaimType getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getComment() {
        return comment;
    }

    public String getUrl() {
        return url;
    }

    public Date getDate() {
        return date;
    }
}
