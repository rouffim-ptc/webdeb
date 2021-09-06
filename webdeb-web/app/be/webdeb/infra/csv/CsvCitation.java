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

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.ActorName;
import be.webdeb.core.api.actor.BusinessSector;
import be.webdeb.core.api.actor.EActorType;
import be.webdeb.core.api.actor.EBusinessSector;
import be.webdeb.core.api.actor.Organization;
import be.webdeb.core.api.actor.Person;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.util.ValuesHelper;
import com.opencsv.bean.CsvBindByName;
import play.api.Play;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * This class serves as a mapper from a line in citation csv file into a plain java bean.
 * Fields are using only "object" types to allow empty value to be read checked for null values
 *
 * @author Martin Rouffiange
 */
public class CsvCitation {

    @Inject
    private CitationFactory factory = play.api.Play.current().injector().instanceOf(CitationFactory.class);

    @Inject
    private TextFactory textFactory = play.api.Play.current().injector().instanceOf(TextFactory.class);

    @Inject
    private TagFactory tagFactory = play.api.Play.current().injector().instanceOf(TagFactory.class);

    @Inject
    private ContributorFactory contributorFactory = play.api.Play.current().injector().instanceOf(ContributorFactory.class);

    @Inject
    private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

    @CsvBindByName
    Integer order;
    @CsvBindByName
    Long webdebId;
    @CsvBindByName
    String original_excerpt;
    @CsvBindByName
    String working_excerpt;
    @CsvBindByName
    String language;
    @CsvBindByName
    Long placeId1;
    @CsvBindByName
    Long placeId2;
    @CsvBindByName
    Long placeId3;
    @CsvBindByName
    String tagId1;
    @CsvBindByName
    String tagId2;
    @CsvBindByName
    String tagId3;
    @CsvBindByName
    Integer textOrder;
    @CsvBindByName
    Long textWebdebId;
    @CsvBindByName
    String textTitle;
    @CsvBindByName
    String textUrl;
    @CsvBindByName
    String textLanguage;
    @CsvBindByName
    String textPublicationDate;
    @CsvBindByName
    Integer textType;
    @CsvBindByName
    Integer textSourceType;
    @CsvBindByName
    String textSourceName;
    @CsvBindByName
    Integer textVisibility;

    /**
     * Maps this csv bean into an API Citation
     *
     * @return the mapped API citation
     * @throws FormatException if any field has an invalid format
     * @throws ObjectNotFoundException if, despite the exists flag, given actor could not be retrieved in DB
     */
    Citation toCitation(Long contributor, int group, Long textId) throws FormatException, ObjectNotFoundException, PermissionException, PersistenceException {
        if (webdebId == null && order == null) {
            throw new FormatException(FormatException.Key.CITATION_ERROR, "either an order or a webdebId must be passed");
        }
        Citation citation;

        if (webdebId != null && webdebId != -1L) {
            // retrieve citation
            citation = factory.retrieve(webdebId);
            if (citation == null) {
                throw new ObjectNotFoundException(Citation.class, webdebId);
            }

        } else {
            if(values.isBlank(original_excerpt) || values.isBlank(tagId1))
                throw new FormatException(FormatException.Key.CITATION_ERROR);

            citation = factory.getCitation();
        }

        Text text;

        if (!values.isBlank(textWebdebId) || !values.isBlank(textId)) {
            // retrieve citation
            text = textFactory.retrieve(values.isBlank(textWebdebId) ? textId : textWebdebId);
            if (text == null) {
                throw new ObjectNotFoundException(Text.class, values.isBlank(textWebdebId) ? textId : textWebdebId);
            }

        } else {

            text = textFactory.findByUrl(textUrl);

            if(text == null) {
                if(values.isBlank(textUrl) && values.isBlank(textTitle))
                    throw new FormatException(FormatException.Key.TEXT_ERROR);

                text = textFactory.getText();
                text.setId(-1L);
                text.setVersion(0L);
                text.addTitle(language, textTitle);
                text.setSourceTitle(textSourceName);
                text.setTextContent("null");

                if (!values.isBlank(textUrl) && values.isURL(textUrl)) {
                    try {
                        text.setUrl(textUrl.trim());
                    } catch (FormatException e) {
                        if(values.isBlank(textTitle))
                            throw new FormatException(FormatException.Key.TEXT_ERROR);
                        text.setUrl(null);
                    }
                }

                try {
                    text.setPublicationDate(textPublicationDate);
                } catch (FormatException e) {
                    text.setPublicationDate(null);
                }

                try {
                    text.setTextSourceType(textFactory.getTextSourceType(textSourceType));
                } catch (Exception e) {
                    text.setTextSourceType(null);
                }

                try {
                    text.setTextType(textFactory.getTextType(textType));
                } catch (Exception e) {
                    text.setTextType(null);
                }

                try {
                    text.setLanguage(textFactory.getLanguage(textLanguage));
                } catch (FormatException e) {
                    text.setLanguage(null);
                }

                try {
                    text.setTextVisibility(textFactory.getTextVisibility(textVisibility == null || ETextVisibility.value(textVisibility) == null ?
                            ETextVisibility.PEDAGOGIC.id() : textVisibility));
                } catch (FormatException e) {
                    text.setTextVisibility(textFactory.getTextVisibility(ETextVisibility.PEDAGOGIC.id()));
                }

                text.save(contributor, group);
            }

        }

        citation.setTextId(text.getId());
        citation.setOriginalExcerpt(original_excerpt);
        citation.setWorkingExcerpt(values.isBlank(working_excerpt) ? original_excerpt : working_excerpt);

        try {
            citation.setLanguage(textFactory.getLanguage(language));
        } catch (FormatException e) {
            citation.setLanguage(null);
        }

        citation.initTags();

        try {
            citation.addTag(helper.toTag(tagId1, language, ETagType.SIMPLE_TAG));
            citation.addTag(helper.toTag(tagId2, language, ETagType.SIMPLE_TAG));
            citation.addTag(helper.toTag(tagId3, language, ETagType.SIMPLE_TAG));
        } catch (FormatException e) {
        }


        Place place = factory.retrievePlace(placeId1);
        if(place != null)
            citation.addPlace(place);

        place = factory.retrievePlace(placeId2);
        if(place != null)
            citation.addPlace(place);

        place = factory.retrievePlace(placeId3);
        if(place != null)
            citation.addPlace(place);


        return citation;
    }

    @Override
    public String toString() {
        return "CsvCitation{" +
                "order=" + order +
                ", webdebId=" + webdebId +
                ", original_excerpt='" + original_excerpt + '\'' +
                ", working_excerpt='" + working_excerpt + '\'' +
                ", language='" + language + '\'' +
                ", placeId1=" + placeId1 +
                ", placeId2=" + placeId2 +
                ", placeId3=" + placeId3 +
                ", tagId1=" + tagId1 +
                ", tagId2=" + tagId2 +
                ", tagId3=" + tagId3 +
                ", textWebdebId='" + textWebdebId + '\'' +
                ", textTitle='" + textTitle + '\'' +
                ", textUrl='" + textUrl + '\'' +
                ", textLanguage='" + textLanguage + '\'' +
                ", textPublicationDate='" + textPublicationDate + '\'' +
                ", textType=" + textType +
                ", textSourceType=" + textSourceType +
                ", textSourceName='" + textSourceName + '\'' +
                ", textVisibility=" + textVisibility +
                '}';
    }

    /*
     * PRIVATE HELPERS
     */


}
