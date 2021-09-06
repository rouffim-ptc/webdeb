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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.util.ValuesHelper;
import com.opencsv.bean.CsvBindByName;
import play.i18n.Lang;

import javax.inject.Inject;

/**
 * This class serves as a mapper from a line in author csv file into a plain java bean.
 * Fields are using only "object" types to allow empty value to be read checked for null values
 *
 * @author Martin Rouffiange
 */
public class CsvAuthor {

    @Inject
    private ActorFactory factory = play.api.Play.current().injector().instanceOf(ActorFactory.class);

    @Inject
    private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

    @CsvBindByName
    Integer order;

    @CsvBindByName
    Long webdebId;

    @CsvBindByName
    Long contributionId;

    @CsvBindByName
    Long actorId;

    @CsvBindByName
    Integer isAuthor;

    @CsvBindByName
    Integer isSpeaker;

    @CsvBindByName
    Integer isCited;

    @CsvBindByName
    Integer isTextAuthor;

    @CsvBindByName
    Long affWebdebId;

    @CsvBindByName
    Integer functionId;

    @CsvBindByName
    String functionName;

    @CsvBindByName
    String startDate;

    @CsvBindByName
    String endDate;

    @CsvBindByName
    Integer startDateType;

    @CsvBindByName
    Integer endDateType;

    /**
     * Map this csv author into an API ActorRole. If this bean does not contain both a webdebId
     * , corresponding contribution id must be passed to ensure this csv role bean can be mapped to a valid
     * actor role.
     *
     * @param contributionId if this bean does not contain a webdebId, a contribution id be the contribution id of actor role must be passed
     * @return the mapped API actor role containing a reference to the contribution / actor role API actor role
     * (both has also been checked for its existence)
     *
     * @throws FormatException if any field in this csv bean has an invalid/inconsistent value
     * @throws ObjectNotFoundException if either the webdebID/contributionId could not be found
     */
    ActorRole toActorRole(Long contributionId, Long contributor, int group) throws FormatException, ObjectNotFoundException, PermissionException, PersistenceException {
        return toActorRole(contributionId, false, contributor, group);
    }

    /**
     * Map this csv author into an API ActorRole. If this bean does not contain both a webdebId
     * , corresponding contribution id must be passed to ensure this csv role bean can be mapped to a valid
     * actor role.
     *
     * @param contributionId if this bean does not contain a webdebId, a contribution id be the contribution id of actor role must be passed
     * @return the mapped API actor role containing a reference to the contribution / actor role API actor role
     * (both has also been checked for its existence)
     *
     * @throws FormatException if any field in this csv bean has an invalid/inconsistent value
     * @throws ObjectNotFoundException if either the webdebID/contributionId could not be found
     */
    ActorRole toActorRole(Long contributionId, boolean forText, Long contributor, int group) throws FormatException, ObjectNotFoundException, PermissionException, PersistenceException {
        Contribution contribution = factory.retrieveContribution(contributionId);
        Actor actor = factory.retrieve(actorId);

        if(contribution == null || (contribution.getContributionType().getEType() != EContributionType.CITATION && contribution.getContributionType().getEType() != EContributionType.TEXT)) {
            throw new ObjectNotFoundException(Citation.class, contributionId);
        }

        if(actor == null) {
            throw new ObjectNotFoundException(Actor.class, actorId);
        }

        ActorRole role = factory.getActorRole(actor, contribution);

        if(forText) {
            role.setIsAuthor(true);
        } else {
            role.setIsAuthor(isAuthor != null && isAuthor == 1);
            role.setIsReporter(isSpeaker != null && isSpeaker == 1);
            role.setIsJustCited(isCited != null && isCited == 1);
        }

        if(!values.isBlank(affWebdebId) || !values.isBlank(functionId) || !values.isBlank(functionName) ||
            !values.isBlank(startDate) || !values.isBlank(endDate) || !values.isBlank(endDateType)) {
            Affiliation affiliation = factory.getAffiliation(affWebdebId);

            if(affiliation == null) {
                affiliation = factory.getAffiliation();
                affiliation.setId(-1L);
                affiliation.setVersion(0L);
            }

            Profession profession = factory.getProfession(functionId);

            if (profession == null && functionName != null && !"".equals(functionName)) {
                profession = factory.findProfession(functionName, true);

                if (profession == null) {
                    profession = factory.createProfession(-1, "fr", EProfessionGender.NEUTRAL.id().toString(), functionName);
                    profession = factory.saveProfession(profession) == -1 ? null : profession;
                }
            }

            affiliation.setFunction(profession);

            affiliation.setStartDate(startDate);
            affiliation.setEndDate(endDate);

            if (startDateType != null) {
                affiliation.setStartDateType(factory.getPrecisionDateType(startDateType));
            }

            if (endDateType != null) {
                affiliation.setEndDateType(factory.getPrecisionDateType(endDateType));
            }

            affiliation.save(contributor, group);

            role.setAffiliation(affiliation);
        }

        if(contribution.getContributionType().getEType() == EContributionType.CITATION && (isTextAuthor == 1 || isSpeaker == 1)){
            Citation citation = (Citation) contribution;

            citation.getText().addActor(toActorRole(citation.getTextId(), true, contributor, group));
            citation.getText().save(contributor, group);
        }

        return role;
    }


    @Override
    public String toString() {
        return "CsvAuthor{" +
                "order=" + order +
                ", webdebId=" + webdebId +
                ", contributionId=" + contributionId +
                ", actorId=" + actorId +
                ", isAuthor=" + isAuthor +
                ", isSpeaker=" + isSpeaker +
                ", isCited=" + isCited +
                ", isTextAuthor=" + isTextAuthor +
                ", affWebdebId=" + affWebdebId +
                ", functionId=" + functionId +
                ", functionName='" + functionName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", startDateType=" + startDateType +
                ", endDateType=" + endDateType +
                '}';
    }
}
