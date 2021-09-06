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

package be.webdeb.infra.csv;

import com.opencsv.bean.CsvBindByName;

/**
 * This bean class contains information regarding the result of a citation / author import.
 * Fields are using only "object" types to allow null value to be passed for unset values
 *
 * @author Martin Rouffiange
 */
public class CsvCitationResult {

    @CsvBindByName
    private Integer order;

    @CsvBindByName
    private Long contributionId;

    @CsvBindByName
    private Long actorId;

    @CsvBindByName
    private Long authorRelationId;

    @CsvBindByName
    private Long authorAffiliationId;

    @CsvBindByName
    private String result;

    @CsvBindByName
    private String error;

    @CsvBindByName
    private String message;


    /**
     * Constructor for any type of general error (parsing, file not found, etc)
     *
     * @param error the name of the error
     * @param message an more detailed error message
     */
    public CsvCitationResult(String error, String message) {
        this.error = error;
        this.message = message;
        result = "ERROR";
    }

    /**
     * Constructor for citation import success
     *
     * @param order the order number in the csv file
     * @param contributionId the contribution id, if existing
     */
    public CsvCitationResult(Integer order, Long contributionId) {
        this.order = order;
        this.contributionId = contributionId;
        result = "SUCCESS";
    }

    /**
     * Constructor for actor import error
     *
     * @param order the order number in the csv file
     * @param contributionId the contribution id, if existing
     * @param error the error as raised by the system
     * @param message a more detailed message
     */
    public CsvCitationResult(Integer order, Long contributionId, String error, String message) {
        this(error, message);
        this.order = order;
        this.contributionId = contributionId;
    }

    /**
     * Constructor for affiliation import error
     *
     * @param order the order number in the csv file
     * @param contributionId the contribution id, if existing
     * @param actorId the actor id, if existing
     * @param authorRelationId the author relation id, if existing
     * @param authorAffiliationId the author relation affiliation id, if existing
     * @param error the error as raised by the system
     * @param message a more detailed message
     */
    public CsvCitationResult(Integer order, Long contributionId, Long actorId, Long authorRelationId, Long authorAffiliationId, String error, String message) {
        this(order, contributionId, error, message);
        this.actorId = actorId;
        this.authorAffiliationId = authorAffiliationId;
        this.authorRelationId = authorRelationId;
    }

    /**
     * Get the order (appearance index) of the actor
     *
     * @return an index value
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Get the contribution id (citation or text)
     *
     * @return
     */
    public Long getContributionId() {
        return contributionId;
    }

    /**
     * Get the actor id (in webdeb)
     *
     * @return an id
     */
    public Long getActorId() {
        return actorId;
    }

    /**
     * Get the contribution_has_actor id
     *
     * @return the author relation id
     */
    public Long getAuthorRelationId() {
        return authorRelationId;
    }

    /**
     * Get the actor_has_affiliation id in contribution_has_actor
     *
     * @return author affiliation id
     */
    public Long getAuthorAffiliationId() {
        return authorAffiliationId;
    }

    /**
     * Get the result of the import of the actor/affiliation
     *
     * @return either "SUCCESS" or "ERROR"
     */
    public String getResult() {
        return result;
    }

    /**
     * Get the error name (exception name) in case of result = "ERROR"
     *
     * @return an exception name
     */
    public String getError() {
        return error;
    }

    /**
     * Get a more detailed message in case of result = "ERROR"
     *
     * @return a detailed message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CsvResult{" +
                "order=" + order +
                ", contributionId=" + contributionId +
                ", actorId=" + actorId +
                ", authorAffiliationId=" + authorAffiliationId +
                ", authorRelationId=" + authorRelationId +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
