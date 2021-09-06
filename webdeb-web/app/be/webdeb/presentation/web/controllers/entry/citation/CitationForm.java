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

package be.webdeb.presentation.web.controllers.entry.citation;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ExternalAuthor;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.text.ETextSourceType;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.PlaceForm;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.tag.SimpleTagForm;
import be.webdeb.presentation.web.controllers.entry.text.TextForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;
import play.data.validation.ValidationError;
import play.i18n.Lang;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Wrapper class for forms used to encode a new citation into the database.
 *
 * Note that all supertype getters corresponding to predefined values (ie, types) are sending
 * ids instead of language-dependent descriptions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CitationForm extends CitationHolder {

    private Long externalCitationId = -1L;
    private TextForm text;
    private String textTitle;

    private Long textIdFromUrl = -1L;
    private Long textIdFromUrlOrTitle = -1L;
    private Long textIdFromTitle = -1L;

    private boolean textHasAuthors = false;
    private Integer authorType = -1;
    private Integer authorTextType = -1;
    private Boolean textAuthorsAreCitationAuthors;
    private Boolean citationAuthorsAreTextAuthors;

    private Boolean removeFromOriginalExcerpt;
    private Boolean addToExcerpt;

    private String workingExcerpt1;
    private String workingExcerpt2;
    private Long contextId;

    protected List<ActorSimpleForm> authorsPers = new ArrayList<>();
    protected List<ActorSimpleForm> authorsOrgs = new ArrayList<>();

    protected List<ActorSimpleForm> authorsTextPers = new ArrayList<>();
    protected List<ActorSimpleForm> authorsTextOrgs = new ArrayList<>();

    protected String reloadDragnDrop;

    /**
     * Play / JSON compliant constructor
     */
    public CitationForm() {
        super();
        text = new TextForm();
    }

    /**
     * Constructor. Create a new form object for given external citation.
     *
     * @param citation an ExternalCitation
     * @param lang the user language
     */
    public CitationForm(ExternalContribution citation, String lang) {
        super();
        if(citation != null) {
            originalExcerpt = citation.getTitle();
            workingExcerpt = originalExcerpt;

            textUrl = citation.getSourceUrl();
            externalCitationId  = citation.getId();
            language = citation.getLanguage().getCode();
        }
    }

    /**
     * Create a new citation wrapper for a given original citation from a given text (citation is empty)
     *
     * @param text the text to which this citation will belong to
     * @param excLang the language of the original citation
     * @param lang the user language
     * @param originalExcerpt the selected original citation from the given text
     */
    public CitationForm(Text text, WebdebUser user, String excLang, String lang, String originalExcerpt) {
        super();
        this.user = user;
        this.lang = lang;
        this.originalExcerpt = originalExcerpt;
        this.workingExcerpt = originalExcerpt;
        this.language = excLang;

        this.textId = text.getId();
        this.text = new TextForm(text, user, lang);
        this.textHasAuthors = !this.text.getAuthors().isEmpty();
        this.textUrl = text.getUrl();
        this.textTitle = text.getTitle(lang);
    }

    /**
     * Create a new citation form
     *
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationForm(String lang) {
        super();
        this.lang = lang;
        text = new TextForm(lang);
    }

    /**
     * Create a new citation form
     *
     * @param lang 2-char ISO code of context language (among play accepted languages)
     * @param contextId the context id where the citation must be added
     */
    public CitationForm(String lang, Long contextId) {
        this(lang);
        this.contextId = contextId;
    }

    /**
     * Create a new citation form
     *
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationForm(Text text, WebdebUser user, String lang) {
        this(lang);
        this.user = user;
        this.contextId = text.getId();

        this.textId = text.getId();
        this.text = new TextForm(text, user, lang);
        this.textHasAuthors = !this.text.getAuthors().isEmpty();
        this.textUrl = text.getUrl();
        this.textTitle = text.getTitle(lang);
    }

    /**
     * Create an citation form from a given citation
     *
     * @param citation an citation
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationForm(Citation citation, WebdebUser user, String lang) {
        // must be set manually, may not call super(argument, lang) constructor because their handling is too different
        this.user = user;
        this.contribution = citation;

        id = citation.getId();
        type = EContributionType.CITATION;

        version = citation.getVersion();
        validated = citation.getValidated().getEType();
        groups = citation.getInGroups().stream().map(Group::getGroupId).collect(Collectors.toList());

        this.citation = citation;
        this.language = citation.getLanguage().getCode();
        this.lang = lang;

        textId = citation.getTextId();
        text = new TextForm(citation.getText(), user, lang);
        textTitle = citation.getText().getTitle(lang);

        originalExcerpt = citation.getOriginalExcerpt();
        workingExcerpt = citation.getWorkingExcerpt();
        workingExcerpt1 = citation.getWorkingExcerpt();
        workingExcerpt2 = citation.getWorkingExcerpt();

        removeFromOriginalExcerpt = false;
        addToExcerpt = false;

        initActors();
        initPlaces(citation.getPlaces(), lang);
        initTags(citation.getTagsAsList(), lang);
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();
        Map<String, List<ValidationError>> subErrors = new HashMap<>();

        switch(stepNum) {
            case -1:
            case 0:
                if (values.isBlank(originalExcerpt)) {
                    errors.put("originalExcerpt", Collections.singletonList(new ValidationError("originalExcerpt", "citation.error.original.blank")));
                } else if (originalExcerpt.length() > ORIGINAL_EXCERPT_MAX_LENGTH) {
                    errors.put("originalExcerpt", Collections.singletonList(new ValidationError("originalExcerpt", "citation.error.original.length")));
                }

                if (stepNum == 0) return returnErrorsMap(errors);
            case 1:
                if(stepNum == 1) {
                    if(ETextSourceType.value(text.getTextSourceType()) == null) {
                        errors.put("text.textSourceType", Collections.singletonList(new ValidationError("text.textSourceType", "general.required")));
                    }
                    else {
                        switch (ETextSourceType.value(text.getTextSourceType())) {
                            case INTERNET:
                                if (!values.isURL(text.getUrl())) {
                                    errors.put("text.url", Collections.singletonList(new ValidationError("text.url", "citation.error.text.url")));
                                } else {
                                    changeCitationText(textFactory.findByUrl(text.getUrl()), text.getUrl());
                                }
                                break;
                            case PDF:
                                if (values.isBlank(text.getUrlOrTitle())) {
                                    errors.put("text.urlOrTitle", Collections.singletonList(new ValidationError("text.urlOrTitle", "citation.error.text.urlOrTitle")));
                                } else {
                                    changeCitationText(values.isURL(text.getUrlOrTitle()) ? textFactory.findByUrl(text.getUrlOrTitle()) : textFactory.retrieve(textIdFromUrlOrTitle),
                                            values.isURL(text.getUrlOrTitle()) ? null : text.getUrlOrTitle());
                                }
                                break;
                            case OTHER:
                                if (values.isBlank(textTitle) && values.isBlank(text.getTitle())) {
                                    errors.put("textTitle", Collections.singletonList(new ValidationError("textTitle", "citation.error.text.title")));
                                } else {
                                    if (!values.isBlank(textTitle)) {
                                        changeCitationText(textFactory.retrieve(textIdFromTitle), textTitle);
                                    } else {
                                        textTitle = text.getTitle();
                                    }
                                }
                                break;
                        }
                    }

                    return returnErrorsMap(errors);
                }
            case 2:

                if(values.isBlank(id)) {

                    if(!textHasAuthors) {

                        if(authorType == null || EAuthorType.value(authorType) == null) {
                            errors.put("authorType", Collections.singletonList(new ValidationError("text.textSourceType", "general.required")));
                        }
                        else {
                            switch (EAuthorType.value(authorType)) {
                                case PERSONS:
                                    subErrors = checkActors(authorsPers, "authorsPers");
                                    break;
                                case ORGANIZATIONS:
                                    subErrors = checkActors(authorsOrgs, "authorsOrgs");
                                    break;
                            }

                            if(subErrors != null) {
                                errors.putAll(subErrors);
                            } else {
                                if(citationAuthorsAreTextAuthors != null && citationAuthorsAreTextAuthors)
                                    text.setAuthors(EAuthorType.value(authorType) == EAuthorType.PERSONS ? authorsPers : authorsOrgs);
                            }
                        }

                    } else {

                        if(EAuthorType.value(authorTextType) == null) {
                            errors.put("authorTextType", Collections.singletonList(new ValidationError("text.textSourceType", "general.required")));
                        }
                        else {
                            switch (EAuthorType.value(authorTextType)) {
                                case FROM_TEXT:
                                    subErrors = null;
                                    authors = text.getAuthors();
                                    break;
                                case PERSONS:
                                    subErrors = checkActors(authorsTextPers, "authorsTextPers");
                                    break;
                                case ORGANIZATIONS:
                                    subErrors = checkActors(authorsTextOrgs, "authorsTextOrgs");
                                    break;
                            }

                            if(subErrors != null) {
                                errors.putAll(subErrors);
                            }
                        }

                    }

                } else {
                    subErrors = checkActors(authors, "authors");
                    if(subErrors != null) errors.putAll(subErrors);

                }

                // if we have reporter object, must have a name
               /* if (reporters != null) {
                    reporters.stream().filter(a -> values.isBlank(a.getLang())).forEach(a -> a.setLang(lang));
                    subErrors = validationListToMap(helper.checkActors(reporters, "reporters"));
                    if (subErrors != null) errors.putAll(subErrors);
                }*/

                // reporters and authors may not be the same
                /*if (authors != null && reporters != null) {
                    reporters.stream().filter(r -> !r.isEmpty()).forEach(r -> {
                        if (authors.stream().anyMatch(a -> r.getFullname().equals(a.getFullname()))) {
                            int index = reporters.indexOf(r);
                            errors.put("reporters[" + index + "].fullname", Collections.singletonList(
                                    new ValidationError("reporters[" + index + "].fullname", "citation.error.authorisreporter")));
                        }
                    });
                }*/

                if (stepNum == 2) return returnErrorsMap(errors);

            case 3:
                if (tags == null || tags.isEmpty() || values.isBlank(tags.get(0).getName())) {
                    String fieldName = "tags[0].name";
                    errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "text.error.tag.name")));
                } else {
                    subErrors = checkTags(tags, "tags");
                    if (subErrors != null) errors.putAll(subErrors);
                }

                // if we have reporter object, must have a name
                if (citedactors != null) {
                    citedactors.stream().filter(a -> values.isBlank(a.getLang())).forEach(a -> a.setLang(lang));
                    subErrors = validationListToMap(helper.checkActors(citedactors, "cited"));
                    if (subErrors != null) errors.putAll(subErrors);
                }

                // check if there is not the same place
                if (places != null) {
                    subErrors = checkPlaces(places);
                    if (subErrors != null) errors.putAll(subErrors);
                }

                if (stepNum == 3) return returnErrorsMap(errors);
            case 4:
                if (values.isBlank(workingExcerpt1)) {
                    errors.put("workingExcerpt1", Collections.singletonList(new ValidationError("workingExcerpt1", "citation.error.working.blank")));
                } else if (workingExcerpt1.length() > WORKING_EXCERPT_MAX_LENGTH) {
                    errors.put("workingExcerpt1", Collections.singletonList(new ValidationError("workingExcerpt1", "citation.error.working.length")));
                }

                if (stepNum == 4) return returnErrorsMap(errors);
            case 5:
                if (values.isBlank(workingExcerpt2)) {
                    errors.put("workingExcerpt2", Collections.singletonList(new ValidationError("workingExcerpt2", "citation.error.working.blank")));
                } else if (workingExcerpt1.length() > WORKING_EXCERPT_MAX_LENGTH) {
                    errors.put("workingExcerpt2", Collections.singletonList(new ValidationError("workingExcerpt2", "citation.error.working.length")));
                }

                if (stepNum == 5) return returnErrorsMap(errors);
            case 6:
                text.setIsOnInternet(false);
                subErrors = text.validate("text.", stepNum == 6);
                if (subErrors != null) errors.putAll(subErrors);

                if (stepNum == 6) return returnErrorsMap(errors);
        }

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save an citation into the database. This id is updated if it was not set before.
     *
     * @param contributor the contributor id that ask to save this contribution
     * @return the map of Contribution type and a list of contribution (actors or folders) that have been created during
     * this insertion(for all unknown contributions), an empty list if none had been created
     *
     * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     */
    public Map<Integer, List<Contribution>> save(Long contributor) throws FormatException, PermissionException, PersistenceException {
        logger.debug("try to save citation " + id + " " + toString() + " with version " + version + " in group " + inGroup);

        // Save text form part
        // TODO change textarea
        text.setTextarea("null");
        text.setUser(user);
        Map<Integer, List<Contribution>> result = text.save(contributor);

        Citation citation = citationFactory.getCitation();
        citation.setVersion(version);
        citation.addInGroup(inGroup);

        citation.setTextId(text.getId());
        textId = text.getId();

        citation.setOriginalExcerpt(originalExcerpt);
        citation.setWorkingExcerpt(getWorkingExcerpt());
        
        citation.setExternalCitationId(externalCitationId);

        try {
            String language = values.isBlank(this.language) ? values.detectTextLanguage(originalExcerpt, lang) : this.language.trim();
            citation.setLanguage(citationFactory.getLanguage(language == null ? lang : language));
        } catch (FormatException e) {
            logger.error("unknown language code " + language, e);
        }

        // tags
        citation.initTags();
        for (SimpleTagForm f : tags.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()))  {
            try {
                citation.addTag(helper.toTag(f, lang, ETagType.SIMPLE_TAG));
            } catch (FormatException e) {
                logger.error("unparsable tag " + f, e);
            }
        }

        // bound places
        citation.initPlaces();
        for (PlaceForm place : places) {
            if(!place.isEmpty()) {
                Place placeToAdd = createPlaceFromForm(place);
                if(placeToAdd != null)
                    citation.addPlace(placeToAdd);
            }
        }

        authors = getAuthorsToSave();
        if(citationAuthorsAreTextAuthors != null && citationAuthorsAreTextAuthors){
            text.setAuthors(authors);
        }

        reporters = !citationAuthorsAreTextAuthors() ? text.getAuthors() : new ArrayList<>();

        // bound actors
        boundActors(citation);

        citation.setId(id != null ? id : -1L);
        // save this citation
        citation.save(contributor, inGroup).forEach((cType, contributions) -> {
            if(!result.containsKey(cType)){
                result.put(cType, contributions);
            } else {
                result.get(cType).addAll(contributions.stream().
                        filter(c -> result.get(cType).stream().noneMatch(r -> r.getId().equals(c.getId())))
                        .collect(Collectors.toList()));
            }
        });
        // update this id (in case of creation
        id = citation.getId();
        return result;
    }

    private boolean citationAuthorsAreTextAuthors(){
        return text.getAuthors().stream()
                .filter(e -> !e.isEmpty())
                .anyMatch(e ->
                    authors.stream()
                        .filter(e2 -> !e2.isEmpty())
                        .anyMatch(e2 -> e.getId().equals(e2.getId())));
    }

    private void changeCitationText(Text text, String defaultTextTitle) {
        if (text != null) {
           this.text = new TextForm(text, user, lang);
           this.textId = text.getId();
           this.textUrl = text.getUrl();
           this.textHasAuthors = this.text.getHasAuthors() != null && this.text.getHasAuthors();
        } else if(values.isURL(defaultTextTitle)) {
            this.text.setUrl2(defaultTextTitle);
            this.text.setIsOnInternet(true);
        } else {
            this.text.setTitle(textTitle);
            this.text.setIsOnInternet(false);
        }
    }

    private Map<String, List<ValidationError>> checkActors(List<ActorSimpleForm> actors, String name){
        Map<String, List<ValidationError>> errors = new HashMap<>();

        if (actors == null || actors.stream().allMatch(ActorSimpleForm::isEmpty)) {
            errors.put(name + "[0].fullname", Collections.singletonList(new ValidationError( name + "[0].fullname", "argument.error.noauthor")));
        } else {
            actors.stream().filter(a -> values.isBlank(a.getLang())).forEach(a -> a.setLang(lang));
            return validationListToMap(helper.checkActors(authors,  name));
        }

        return errors;
    }

    /*
     * GETTERS / SETTERS
     */

    /**
     * Get the external citation id if this citation come from an external citation
     *
     * return an external citation id
     */
    public Long getExternalCitationId() {
        return externalCitationId;
    }

    /**
     * Set the external citation id if this citation come from an external citation
     *
     * @param externalCitationId an external citation id
     */
    public void setExternalCitationId(Long externalCitationId) {
        this.externalCitationId = externalCitationId;
    }

    /**
     * Set the original citation of this citation
     *
     * @param originalExcerpt a text citation
     */
    public void setOriginalExcerpt(String originalExcerpt) {
        this.originalExcerpt = originalExcerpt;
    }

    /**
     * Set the citation language id
     *
     * @param language a language id
     * @see Language
     */
    public void setLanguage(String language) {
        if(lang == null){
            lang = language;
        }
        this.language = language;
    }

    public TextForm getText() {
        return text;
    }

    public void setText(TextForm text) {
        this.text = text;
    }

    public void setTags(List<SimpleTagForm> tags) {
        this.tags = tags;
    }

    public void setPlaces(List<PlaceForm> places) {
        this.places = places;
    }

    public void setTextId(Long textId) {
        this.textId = textId;
    }

    public String getWorkingExcerpt() {
        return workingExcerpt1;
    }

    public String getWorkingExcerpt1() {
        return values.isBlank(workingExcerpt1) ? originalExcerpt : workingExcerpt1;
    }

    public void setWorkingExcerpt1(String workingExcerpt1) {
        this.workingExcerpt1 = workingExcerpt1;
    }

    public String getWorkingExcerpt2() {
        return values.isBlank(workingExcerpt1) ? originalExcerpt : workingExcerpt1;
    }

    public void setWorkingExcerpt2(String workingExcerpt2) {
        this.workingExcerpt2 = workingExcerpt2;
    }

    public boolean getTextHasAuthors() {
        return textHasAuthors;
    }

    public void setTextHasAuthors(boolean textHasAuthors) {
        this.textHasAuthors = textHasAuthors;
    }

    public Integer getAuthorType() {
        return authorType;
    }

    public void setAuthorType(Integer authorType) {
        this.authorType = authorType;
    }

    public Integer getAuthorTextType() {
        return authorTextType;
    }

    public void setAuthorTextType(Integer authorTextType) {
        this.authorTextType = authorTextType;
    }

    public Boolean getCitationAuthorsAreTextAuthors() {
        return citationAuthorsAreTextAuthors;
    }

    public void setCitationAuthorsAreTextAuthors(Boolean citationAuthorsAreTextAuthors) {
        this.citationAuthorsAreTextAuthors = citationAuthorsAreTextAuthors;
    }

    public Boolean getTextAuthorsAreCitationAuthors() {
        return textAuthorsAreCitationAuthors;
    }

    public void setTextAuthorsAreCitationAuthors(Boolean textAuthorsAreCitationAuthors) {
        this.textAuthorsAreCitationAuthors = textAuthorsAreCitationAuthors;
    }

    public Boolean getRemoveFromOriginalExcerpt() {
        return removeFromOriginalExcerpt;
    }

    public void setRemoveFromOriginalExcerpt(Boolean removeFromOriginalExcerpt) {
        this.removeFromOriginalExcerpt = removeFromOriginalExcerpt;
    }

    public Boolean getAddToExcerpt() {
        return addToExcerpt;
    }

    public void setAddToExcerpt(Boolean addToExcerpt) {
        this.addToExcerpt = addToExcerpt;
    }

    public List<ActorSimpleForm> getAuthorsPers() {
        return authorsPers;
    }

    public void setAuthorsPers(List<ActorSimpleForm> authorsPers) {
        this.authorsPers = authorsPers;
    }

    public List<ActorSimpleForm> getAuthorsOrgs() {
        return authorsOrgs;
    }

    public void setAuthorsOrgs(List<ActorSimpleForm> authorsOrgs) {
        this.authorsOrgs = authorsOrgs;
    }

    public List<ActorSimpleForm> getAuthorsTextPers() {
        return authorsTextPers;
    }

    public void setAuthorsTextPers(List<ActorSimpleForm> authorsTextPers) {
        this.authorsTextPers = authorsTextPers;
    }

    public List<ActorSimpleForm> getAuthorsTextOrgs() {
        return authorsTextOrgs;
    }

    public void setAuthorsTextOrgs(List<ActorSimpleForm> authorsTextOrgs) {
        this.authorsTextOrgs = authorsTextOrgs;
    }

    public Long getTextIdFromUrl() {
        return textIdFromUrl;
    }

    public void setTextIdFromUrl(Long textIdFromUrl) {
        this.textIdFromUrl = textIdFromUrl;
    }

    public Long getTextIdFromUrlOrTitle() {
        return textIdFromUrlOrTitle;
    }

    public void setTextIdFromUrlOrTitle(Long textIdFromUrlOrTitle) {
        this.textIdFromUrlOrTitle = textIdFromUrlOrTitle;
    }

    public Long getTextIdFromTitle() {
        return textIdFromTitle;
    }

    public void setTextIdFromTitle(Long textIdFromTitle) {
        this.textIdFromTitle = textIdFromTitle;
    }

    @Override
    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public int getOriginalExcerptMaxLength() {
        return ORIGINAL_EXCERPT_MAX_LENGTH;
    }

    public int getWorkingExcerptMaxLength() {
        return WORKING_EXCERPT_MAX_LENGTH;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public String getReloadDragnDrop() {
        return reloadDragnDrop;
    }

    public void setReloadDragnDrop(String reloadDragnDrop) {
        this.reloadDragnDrop = reloadDragnDrop;
    }

    public List<ActorSimpleForm> getAuthorsToSave(){
        return textHasAuthors ? authorTextType == EAuthorType.PERSONS.id() ? authorsTextPers : authorTextType == EAuthorType.ORGANIZATIONS.id() ? authorsTextOrgs : authors
                : authorType == EAuthorType.PERSONS.id() ? authorsPers : authorType == EAuthorType.ORGANIZATIONS.id() ? authorsOrgs : authors;
    }

    public void setAuthors(List<ActorSimpleForm> authors) {
        this.authors = authors;
    }

    public void setReporters(List<ActorSimpleForm> reporters) {
        this.reporters = reporters;
    }

    public void setCitedactors(List<ActorSimpleForm> citedactors) {
        this.citedactors = citedactors;
    }

    /**
     * Get all ist of actor simple form of the citation
     *
     * @return a list of actors simple form
     */
    @JsonIgnore
    public List<ActorSimpleForm> getAllActors() {
        List<ActorSimpleForm> forms = new ArrayList<>();

        forms.addAll(authors);
        forms.addAll(authorsPers);
        forms.addAll(authorsOrgs);
        forms.addAll(authorsTextPers);
        forms.addAll(authorsTextOrgs);
        forms.addAll(citedactors);
        forms.addAll(text.getActors());

        return forms;
    }

    @JsonIgnore
    public List<ActorSimpleForm> getAllNewActors() {
        return getAllActors().stream()
                .filter(a -> a.getIsNew() || a.getIsAffNew())
                .collect(Collectors.toList());
    }

}
