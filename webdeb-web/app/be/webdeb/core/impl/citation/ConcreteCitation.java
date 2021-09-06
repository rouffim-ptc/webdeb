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

package be.webdeb.core.impl.citation;

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractTextualContribution;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.CitationAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a Citation
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ConcreteCitation extends AbstractTextualContribution<CitationFactory, CitationAccessor> implements Citation {


    private String originalExcerpt;
    private String workingExcerpt;

    private Language language;
    private Long textId;
    private Text text = null;
    private Long externalExcerptId;
    private Long linkId;

    private TextFactory textFactory;
    private List<Debate> debates = null;

    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Create a Text instance
     *
     * @param factory the citation factory
     * @param accessor the citation accessor
     * @param textFactory the text factory
     * @param actorAccessor an actor accessor (to retrieve/update involved actors)
     * @param contributorFactory the contributor accessor
     */
    ConcreteCitation(CitationFactory factory, CitationAccessor accessor, TextFactory textFactory,
                     ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
        super(factory, accessor, actorAccessor, contributorFactory);
        this.textFactory = textFactory;
        type = EContributionType.CITATION;
    }

    @Override
    public String getOriginalExcerpt() {
        return originalExcerpt;
    }

    @Override
    public void setOriginalExcerpt(String originalExcerpt) {
        this.originalExcerpt = originalExcerpt;
    }

    @Override
    public String getWorkingExcerpt() {
        return workingExcerpt;
    }

    @Override
    public void setWorkingExcerpt(String workingExcerpt) {
        this.workingExcerpt = workingExcerpt;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public Long getTextId() {
        return textId;
    }

    @Override
    public void setTextId(Long textId) {
        this.textId = textId;
    }

    @Override
    public Text getText() {
        if(textId != null){
            if(text == null){
                // lazy loading
                text = textFactory.retrieve(textId);
            }
            return text;
        }
        return null;
    }

    @Override
    public Long getExternalCitationId() {
        return externalExcerptId;
    }

    @Override
    public void setExternalCitationId(Long externalExcerptId) {
        this.externalExcerptId = externalExcerptId;
    }

    @Override
    public Long getLinkId() {
        return linkId;
    }

    @Override
    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    @Override
    public Debate findLinkedDebate(Long contributor, int group, Long rejectedDebate) {
        return accessor.findLinkedDebate(id, contributor, group, rejectedDebate);
    }

    @Override
    public List<Debate> findLinkedDebates(Long contributor, int group, Long rejectedDebate) {
        if(debates == null){
            debates = accessor.findLinkedDebates(id, contributor, group, rejectedDebate);
        }
        return debates;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("citation contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.CITATION_ERROR, String.join(",", isValid.toString()));
        }
        return accessor.save(this, currentGroup, contributor);
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = new ArrayList<>();

        if (factory.getValuesHelper().isBlank(originalExcerpt)) {
            fieldsInError.add("citation has no original excerpt");
        }

        if(textId == null){
            fieldsInError.add("citation has no text id");
        }
        return fieldsInError;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Citation && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return 33 * (id == -1L ? 48 : id.hashCode()) + originalExcerpt.hashCode() + textId.hashCode();
    }



    @Override
    public String toString() {
        return "citation {" +
                "original excerpt='" + originalExcerpt + '\'' +
                "working excerpt='" + workingExcerpt + '\'' +
                "language='" + language.getCode() + '\'' +
                ", text id=" + textId +
                '}';
    }

}
