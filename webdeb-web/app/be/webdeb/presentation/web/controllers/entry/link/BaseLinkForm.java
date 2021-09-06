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

package be.webdeb.presentation.web.controllers.entry.link;

import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.link.ContributionLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributionHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;


/**
 * This class holds supertype wrapper for every type of contribution links.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class BaseLinkForm extends ContributionHolder {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    // Note: as for all wrappers, all fields MUST hold empty values for proper form validation
    protected Long originId = -1L;
    protected String originTitle = "";

    protected Long destinationId = -1L;
    protected String destinationTitle = "";

    protected String shadeName = "";
    protected int shadeId = -1;

    protected int order = 0;

    /**
     * Play / JSON compliant constructor
     */
    public BaseLinkForm() {
        super();
    }

    /**
     * Constructor from a given contribution link
     *
     * @param link a contribution link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public BaseLinkForm(ContributionLink link, WebdebUser user, String lang) {
        super(link, user, lang);
        originId = link.getOriginId();
        destinationId = link.getDestinationId();
        order = link.getOrder();
    }

    /**
     * Constructor from a given contribution link
     *
     * @param originId the origin contribution id
     * @param destinationId the destination contribution id
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public BaseLinkForm(Long originId, Long destinationId, String lang) {
        this.originId = originId;
        this.destinationId = destinationId;
        this.lang = lang;
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (!values.isBlank(originId)) {
            errors.put("originId", Collections.singletonList(new ValidationError("originId", "argument.links.error.origin")));
        }

        if (!values.isBlank(destinationId)) {
            errors.put("destinationId", Collections.singletonList(new ValidationError("destinationId", "argument.links.error.destination")));
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this LinkForm in persistence layer
     *
     * @param contributor the contributor id that created this link
     *
     * @throws FormatException if any field contains error
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     *
     */
    public void save(Long contributor) throws FormatException, PermissionException, PersistenceException {
        logger.debug("try to save link " + toString() + " with version " + version + " in group " + inGroup);
        toLink().save(contributor, inGroup);
    }

    /**
     * Transform this form into an API contribution link
     *
     * @return an API contribution link corresponding to this contribution link form
     * @throws PersistenceException if any field contains error
     */
    public abstract ContributionLink toLink() throws PersistenceException ;

    /**
     * Update the given API contribution link with this form values
     *
     * @param link the api contribution link to update
     */
    protected void updateLink(ContributionLink link) {
        link.setId(id);
        link.setVersion(version);
        link.addInGroup(inGroup);

        link.setOriginId(originId);
        link.setDestinationId(destinationId);
        link.setOrder(order);
    }


    @Override
    public MediaSharedData getMediaSharedData() {
        return null;
    }

    @Override
    public String getContributionDescription() {
        return null;
    }

    @Override
    public String getDefaultAvatar(){
        return "";
    }

    @Override
    public String toString() {
        return "link from " + originTitle + "[" + originId + "] to " + destinationTitle + "[" + destinationId +
                "] with shade " + shadeName + " order " + order;
    }

    /*
     * GETTERS
     */

    /**
     * Get the id of the origin of the link (contribution or context contribution)
     *
     * @return a contribution id
     */
    public Long getOriginId() {
        return originId;
    }

    /**
     * Get the id of the destination of the link
     *
     * @return a contribution id
     */
    public Long getDestinationId() {
        return destinationId;
    }

    /**
     * Get the title of the origin (contribution title)
     *
     * @return a contribution title
     */
    public String getOriginTitle() {
        return originTitle;
    }

    /**
     * Get the title of the origin (contribution title)
     *
     * @return a contribution title
     */
    public String getDestinationTitle() {
        return destinationTitle;
    }

    /**
     * Get the link shade id, if any
     *
     * @return a shade id
     */
    public int getShadeId() {
        return shadeId;
    }

    /**
     * Get the link shade label (language specific), if any
     *
     * @return a shade label
     */
    public String getShadeName() {
        return shadeName;
    }

    public int getOrder() {
        return order;
    }

    /*
     * SETTERS
     */

    /**
     * Set the id of the origin of the link (contribution or context contribution)
     *
     * @param originId a contrbution id
     */
    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    /**
     * Set the id of the destination of the link
     *
     * @param destinationId a contribution id
     */
    public void setDestinationId(Long destinationId) {
        this.destinationId = destinationId;
    }

    /**
     * Set the shade id
     *
     * @param shadeId the shade of the link
     */
    public void setShadeId(int shadeId) {
        this.shadeId = shadeId;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
