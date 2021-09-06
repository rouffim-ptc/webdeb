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

import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.presentation.web.controllers.entry.ECommentsType;
import be.webdeb.presentation.web.controllers.entry.EFilterName;
import be.webdeb.presentation.web.controllers.entry.TextualContributionHolder;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.actor.EActorVizPane;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;
import play.i18n.Lang;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds concrete values of an Citation (no IDs, but their description, as defined in the
 * database). Except by using a constructor, no value can be edited outside of this package or by
 * subclassing.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CitationHolder extends TextualContributionHolder {

    static final int ORIGINAL_EXCERPT_MAX_LENGTH = 600;
    static final int WORKING_EXCERPT_MAX_LENGTH = 600;

    @Inject
    protected CitationFactory citationFactory = Play.current().injector().instanceOf(CitationFactory.class);

    @Inject
    protected TextFactory textFactory = Play.current().injector().instanceOf(TextFactory.class);

    protected Citation citation;

    private EJustificationLinkShade linkShade = null;
    private CitationJustificationLinkForm justification = null;

    // Note: as for all wrappers, all fields MUST hold empty values for proper form validation
    protected Long textId = -1L;
    protected String textTitle = "";
    protected String textUrl = "";

    protected String originalExcerpt = "";
    protected String workingExcerpt = "";
    protected String language;

    // text-related properties
    protected String publicationDate;
    protected String source;
    protected String textType = "";

    protected ECommentsType commentsType;

    protected Long linkId;

    protected DebateHolder randomLinkedDebate = null;
    protected List<DebateHolder> linkedDebates = null;

    /**
     * Play / JSON compliant constructor
     */
    public CitationHolder() {
        super();
        type = EContributionType.CITATION;
    }

    /**
     * Construct a ReadOnlyExcerpt from a given citation with descriptions in the given language
     *
     * @param citation an Citation
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationHolder(Citation citation, WebdebUser user, String lang) {
        this(citation, user, lang, false);
    }

    /**
     * Construct a ReadOnlyExcerpt from a given citation with descriptions in the given language
     *
     * @param citation an Citation
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationHolder(Citation citation, WebdebUser user, String lang, boolean light) {
        super(citation, user, lang, light);
        this.citation = citation;

        Text text = citation.getText();
        textId = text.getId();
        textTitle = text.getTitle(lang);
        textUrl = text.getUrl();

        publicationDate = text.getPublicationDate();
        source = text.getSourceTitle();

        if (text.getTextType() != null) {
            textType = text.getTextType().getName(lang);
        }

        // set original and working citation
        originalExcerpt = citation.getOriginalExcerpt();
        workingExcerpt = citation.getWorkingExcerpt();

        language = citation.getLanguage().getCode();

        linkId = citation.getLinkId();
    }

    /**
     * Construct a ReadOnlyExcerpt from a given citation justification link with descriptions in the given language
     *
     * @param citation an Citation
     * @param link a citation justification link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationHolder(Citation citation, CitationJustificationLinkForm link, WebdebUser user, String lang) {
        this(citation, user, lang);

        this.justification = link;
    }

    /**
     * Construct a ReadOnlyExcerpt from a given citation and justification shade
     *
     * @param citation an Citation
     * @param shade the shade of a justification link
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public CitationHolder(Citation citation, EJustificationLinkShade shade, WebdebUser user, String lang) {
        this(citation, user, lang, true);

        this.linkShade = shade;
    }

    /**
     * Initialize authors, reporters and cited actors fields with this contribution actor roles
     */
    protected void initActors() {
        super.initActors();

        if (!reporters.isEmpty()) {
            // at least one reporter => reported
            commentsType = ECommentsType.REPORTED;
        } else {
            // no reporter => signed or co-signed
            if (authors.size() > 1) {
                commentsType = ECommentsType.COSIGNED;
            } else {
                // actor is alone and author => signed
                commentsType = ECommentsType.SIGNED;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder actors = new StringBuilder();
        if (getActors() != null) {
            for (ActorSimpleForm a : getActors()) {
                actors.append(a.getFullname() + " (" + a.getId() + ") " + a.getFullFunction() + " (" + a.getAha()
                        + ") author/reporter: " + a.getAuthor() + "-" + a.getReporter() + ", ");
            }
        }
        return "citation [" + id + "] with original citation : " + originalExcerpt + " and lang " + language + " actors " + actors;
    }

    @Override
    public String getContributionDescription(){
        List<String> descriptions = new ArrayList<>();
        descriptions.add(originalExcerpt);
        descriptions.add(textTitle);


        return String.join(", ", descriptions);
    }

    @Override
    public MediaSharedData getMediaSharedData(){
        if(mediaSharedData == null){
            mediaSharedData = new MediaSharedData(originalExcerpt, "citation");
        }
        return mediaSharedData;
    }

    @Override
    public String getDefaultAvatar(){
        return "";
    }

    /**
     * Get a complete description of this citation
     *
     * @return the description
     */
    @JsonIgnore
    public String getDescription() {
        return " (" + getTagDescription(false) +  (places.size() > 0 ? ", "
            + getPlaceDescription() : "") + ", " + getActorDescription(getAuthors(3), false) + ")";
    }

    /**
     * Get a complete description of this citation with redirection link for context elements (like folders, places)
     *
     * @return the description with link
     */
    @JsonIgnore
    public String getDescriptionWithLink() {
        return " (" + getTagDescription(true) + (places.size() > 0 ? ", "
            + getPlaceDescription(true) : "") + ", " + getActorDescription(getAuthors(3), true) + ")";
    }

    public String getAuthorsDescription() {
        return super.getAuthorsDescription() + ", " + i18n.get(Lang.get(lang).get(), "actor.role." + getCommentsType().name().toLowerCase());
    }

    public String getAuthorsDescriptionWithLink() {
        return super.getAuthorsDescriptionWithLink(EActorVizPane.ARGUMENTS.id(), 0) + ", " + i18n.get(Lang.get(lang).get(),"actor.role." + getCommentsType().name().toLowerCase());
    }

    /*
     * GETTERS
     */

    /**
     * Get the text id from which this citation originates from
     *
     * @return a text id
     */
    public Long getTextId() {
        return textId;
    }

    /**
     * Get the text url where it comes, if any
     *
     * @return the text source url
     */
    public String getTextUrl() {
        return textUrl;
    }

    /**
     * Get the text title where this citation originating from
     *
     * @return a text title
     */
    public String getTextTitle() {
        return textTitle;
    }

    /**
     * Get the original citation
     *
     * @return an original citation of this.textId
     */
    public String getOriginalExcerpt() {
        return originalExcerpt;
    }

    /**
     * Get the working citation
     *
     * @return a working citation of this.textId
     */
    public String getWorkingExcerpt() {
        return workingExcerpt;
    }

    /**
     * Get the citation language
     *
     * @return the language description in this.lang
     *
     * @see Language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Get the source name of the text from which this citation has been extracted
     *
     * @return the owning text source name
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the publication date of the text from which this citation has been extracted
     *
     * @return a string representation of date of the form DD/MM/YYYY (D and M optional)
     */
    public String getPublicationDate() {
        return publicationDate;
    }

    /**
     * Get the text type from which this citation originates from
     *
     * @return the text type
     */
    public String getTextType() {
        return textType;
    }

    public EJustificationLinkShade getLinkShade() {
        return linkShade;
    }

    public DebateHolder getRandomLinkedDebate(Long rejectedDebate) {
        if(randomLinkedDebate == null) {
            Debate debate = citation.findLinkedDebate(user.getId(), user.getGroupId(), rejectedDebate);
            randomLinkedDebate = debate != null ? helper.toDebateHolder(debate, user, lang, true) : null;
        }
        return randomLinkedDebate;
    }

    public List<DebateHolder> getLinkedDebates(Long rejectedDebate) {
        if(linkedDebates == null) {
            linkedDebates = helper.toDebatesHolders(citation.findLinkedDebates(user.getId(), user.getGroupId(), rejectedDebate), user, lang, true);
        }
        return linkedDebates;
    }

    @JsonIgnore
    public ECommentsType getCommentsType() {
        if(commentsType == null) {
            if (getNbActors(EActorRole.REPORTER) > 0) {
                // at least one reporter => reported
                commentsType = ECommentsType.REPORTED;
            } else {
                // no reporter => signed or co-signed
                if (getNbActors(EActorRole.AUTHOR) > 1) {
                    commentsType = ECommentsType.COSIGNED;
                } else {
                    // actor is alone and author => signed
                    commentsType = ECommentsType.SIGNED;
                }
            }
        }
        return commentsType;
    }

    @JsonIgnore
    public CitationJustificationLinkForm getJustification() {
        return justification;
    }

    @JsonIgnore
    public Long getPositionId() {
        return linkId;
    }
}
