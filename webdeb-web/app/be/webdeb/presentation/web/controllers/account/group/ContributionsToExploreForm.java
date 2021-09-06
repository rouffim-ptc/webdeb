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

package be.webdeb.presentation.web.controllers.account.group;

import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.account.group.GroupForm;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.util.ValuesHelper;
import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import play.api.Play;
import play.data.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

public class ContributionsToExploreForm {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected ContributorFactory factory = Play.current().injector().instanceOf(ContributorFactory.class);
    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);
    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    // Note: as for all wrappers, all fields MUST hold empty values for proper form validation
    protected List<ContributionToExploreForm> contributions = new ArrayList<>();

    protected Integer contributionType = -1;
    protected Integer groupId = -1;
    protected GroupForm group;

    /**
     * Play / JSON compliant constructor
     */
    public ContributionsToExploreForm() { }

    /**
     * Constructor from a given contribution
     *
     * @param user a WebDeb user
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public ContributionsToExploreForm(List<ContributionToExplore> contributions, Group group, EContributionType type, WebdebUser user, String lang) {
        this.contributions = contributions.stream().map(e -> new ContributionToExploreForm(e, user, lang)).collect(Collectors.toList());
        Collections.sort(this.contributions);

        this.contributionType = type.id();
        this.groupId = group.getGroupId();
        this.group = new GroupForm(group);
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this LinkForm in persistence layer
     *
     * @param contributor the contributor id that created this link
     * @param groupId a user group id
     *
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     *
     */
    public void save(Long contributor, int groupId) throws PermissionException, PersistenceException {
        logger.debug("try to save contribution to explore " + toString());

        Set<Long> idsSet = new HashSet<>();

        for(ContributionToExploreForm form : contributions) {
            if(!form.isEmpty()) {
                ContributionToExplore cte = factory.getContributionToExplore();

                cte.setContributionToExploreId(form.getContributionToExploreId());
                cte.setContributionId(form.getId());
                //cte.setGroupId(groupId);
                cte.setGroupId(groupId);
                cte.setOrder(form.getOrder());

                cte.save(contributor);
                idsSet.add(cte.getContributionToExploreId());
            }
        }

        factory.deleteContributionToExplore(idsSet, EContributionType.value(contributionType), groupId);
    }

    /*
     * GETTERS and SETTERS
     */

    public Integer getContributionType() {
        return contributionType;
    }

    public void setContributionType(Integer contributionType) {
        this.contributionType = contributionType;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public GroupForm getGroup() {
        return group;
    }

    public List<ContributionToExploreForm> getContributions() {
        return contributions;
    }

    public void setContributions(List<ContributionToExploreForm> contributions) {
        this.contributions = contributions;
    }

    @Override
    public String toString() {
        return "ContributionsToExploreForm{" +
                "contributions=" + contributions +
                ", groupId=" + groupId +
                ", contributionTypeId=" + contributionType +
                '}';
    }
}
