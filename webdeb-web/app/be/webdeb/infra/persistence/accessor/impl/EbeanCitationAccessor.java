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

package be.webdeb.infra.persistence.accessor.impl;

import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EModificationStatus;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.debate.DebateShade;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.CitationAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;
import be.webdeb.infra.persistence.model.*;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This accessor handles retrieval and save actions of Citations.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanCitationAccessor extends AbstractContributionAccessor<CitationFactory> implements CitationAccessor {

    @Inject
    private DebateAccessor debateAccessor;

    @Override
    public Citation retrieve(Long id, boolean hit) {
        be.webdeb.infra.persistence.model.Citation citation = be.webdeb.infra.persistence.model.Citation.findById(id);
        if (citation != null) {
            try {
                if (hit) {
                    addHitToContribution(citation.getContribution());
                }
                return mapper.toCitation(citation);
            }catch(FormatException e){
                logger.warn("unable to cast retrieved citation " + id);
            }
        } else {
            logger.warn("no citation found for id " + id);
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.citation.CitationJustification retrieveJustificationLink(Long id) {
        be.webdeb.infra.persistence.model.CitationJustification justification = be.webdeb.infra.persistence.model.CitationJustification.findById(id);
        if (justification != null) {
            try {
                return mapper.toCitationJustification(justification);
            }catch(FormatException e){
                logger.warn("unable to cast retrieved citation justification " + id);
            }
        } else {
            logger.warn("no citation justification found for id " + id);
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.citation.CitationPosition retrievePositionLink(Long id) {
        be.webdeb.infra.persistence.model.CitationPosition position = be.webdeb.infra.persistence.model.CitationPosition.findById(id);
        if (position != null) {
            try {
                return mapper.toCitationPosition(position);
            }catch(FormatException e){
                logger.warn("unable to cast retrieved citation position " + id);
            }
        } else {
            logger.warn("no citation position found for id " + id);
        }
        return null;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Citation contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save citation " + contribution.toString() + " in group " + contribution.getInGroups());
        Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> contributions = new HashMap<>();

        be.webdeb.infra.persistence.model.Contribution c = checkContribution(contribution, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // check if textid exists, otherwise -> ObjectNotFound
        be.webdeb.infra.persistence.model.Text text = Text.findById(contribution.getTextId());
        if (text == null) {
            logger.error("unable to retrieve text " + contribution.getTextId());
            throw new ObjectNotFoundException(Text.class, contribution.getTextId());
        }

        // open transaction and handle the whole save chain
        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

        try {

            EModificationStatus status;
            be.webdeb.infra.persistence.model.Citation citation;

            if (c == null) {
                // new argument
                logger.debug("start creation of new citation " + contribution.getOriginalExcerpt());
                status = EModificationStatus.CREATE;

                // create contribution super type
                c = initContribution(EContributionType.CITATION.id(), contribution.getOriginalExcerpt());

                // create argument and binding
                citation = updateCitation(contribution, new be.webdeb.infra.persistence.model.Citation());
                c.setCitation(citation);

                try {
                    c.save();

                    if(!values.isBlank(contribution.getExternalCitationId())){
                        be.webdeb.infra.persistence.model.ExternalContribution externalCitation =
                                be.webdeb.infra.persistence.model.ExternalContribution.findByUrlAndContributionType(contribution.getText().getUrl(), EContributionType.CITATION);

                        if(externalCitation != null){
                            externalCitation.setInternalContribution(c);
                            externalCitation.update();
                        }
                    }
                } catch (Exception e) {
                    logger.error("error while saving contribution " + contribution.getOriginalExcerpt());
                    throw new PersistenceException(PersistenceException.Key.SAVE_CITATION, e);
                }

                // set id of citation and bind to text
                citation.setIdContribution(c.getIdContribution());
                citation.setContribution(c);
                citation.setText(text);

                // manage bindings to actors
                bindActorsToContribution(c, contribution, currentGroup, contributor, contributions);
                // manage bindings to tags
                bindTagsToContribution(c, contribution, contributor, contributions);
                // manage places
                savePlaces(contribution.getPlaces(), c);
                // update groups
                updateGroups(contribution, c);
                // update groups of text also, in case citation has been created in another group
                // we must add this group for bound text (for visibility purpose)
                updateGroups(contribution, text.getContribution());

                try {
                    citation.save();
                    c.update();
                } catch (Exception e) {
                    logger.error("error while saving citation " + citation.getOriginalExcerpt(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_CITATION, e);
                }

                // set new id for given contribution
                contribution.setId(c.getIdContribution());

            } else {
                logger.debug("start update of existing citation " + contribution.getId());
                status = EModificationStatus.UPDATE;

                citation = c.getCitation();

                citation.setWorkingExcerpt(contribution.getWorkingExcerpt());
                citation.update();

                c.setSortkey(contribution.getOriginalExcerpt());

                // manage places
                savePlaces(contribution.getPlaces(), c);

                c.update();

                // manage bindings to actors, retrieve them, save unexisting ones and remove others
                updateActorBindings(contribution, c, currentGroup, contributor, contributions);
                // manage tags
                bindTagsToContribution(c, contribution, contributor, contributions);

                // manage groups for this citation and originating text
                updateGroups(contribution, c);
                updateGroups(contribution, citation.getText().getContribution());
            }
            // bind contributor to this citation
            bindContributor(citation.getContribution(), dbContributor, status);

            forceContributionUpdate(citation.getText().getContribution());

            transaction.commit();
            logger.info("saved " + citation.toString());

        } finally {
            transaction.end();
        }

        return contributions;
    }

    @Override
    public void save(be.webdeb.core.api.citation.CitationJustification link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save citation justification link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        be.webdeb.infra.persistence.model.Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        be.webdeb.infra.persistence.model.Contribution context =
                be.webdeb.infra.persistence.model.Contribution.findById(link.getOriginId());
        if (context == null) {
            logger.error("unable to retrieve context contribution" + link.getOriginId());
            throw new ObjectNotFoundException(ContextContribution.class, link.getOriginId());
        }

        be.webdeb.infra.persistence.model.Tag subContext =
                be.webdeb.infra.persistence.model.Tag.findById(link.getSubContextId());
        if (!values.isBlank(link.getSubContextId()) && (subContext == null || subContext.getTagtype().getEType() != ETagType.SUB_DEBATE_TAG)) {
            logger.error("unable to retrieve tag sub context " + link.getSubContextId());
            throw new ObjectNotFoundException(ContextContribution.class, link.getSubContextId());
        }

        be.webdeb.infra.persistence.model.Tag category =
                be.webdeb.infra.persistence.model.Tag.findById(link.getTagCategoryId());
        if (!values.isBlank(link.getTagCategoryId()) && (category == null || category.getTagtype().getEType() != ETagType.CATEGORY_TAG)) {
            logger.error("unable to retrieve tag category " + link.getTagCategoryId());
            throw new ObjectNotFoundException(Tag.class, link.getTagCategoryId());
        }

        be.webdeb.infra.persistence.model.Argument superArgument =
                be.webdeb.infra.persistence.model.Argument.findById(link.getSuperArgumentId());
        if (!values.isBlank(link.getSuperArgumentId()) && superArgument == null) {
            logger.error("unable to retrieve super argument " + link.getSuperArgument());
            throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Argument.class, link.getSuperArgumentId());
        }

        be.webdeb.infra.persistence.model.Citation citation =
                be.webdeb.infra.persistence.model.Citation.findById(link.getDestinationId());
        if (citation == null) {
            logger.error("unable to retrieve destination citation " + link.getDestinationId());
            throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Citation.class, link.getDestinationId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.CitationJustification dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.CITATION_JUSTIFICATION.id(), null);
                // create link
                dbLink = updateJustificationLink(link, new be.webdeb.infra.persistence.model.CitationJustification(), context, subContext, category, superArgument, citation);
                contribution.setCitationJustificationLink(dbLink);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for citation justification link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name() + " " + e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
                }

                dbLink.setContribution(contribution);
                dbLink.setIdContribution(contribution.getIdContribution());
                updateGroups(link, contribution);

                // save link
                try {
                    contribution.update();
                    dbLink.save();
                    link.setId(dbLink.getIdContribution());
                    logger.debug("saved citation justification link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving contribution for citation justification link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name());
                    throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateJustificationLink(link, contribution.getCitationJustificationLink(), context, subContext, category, superArgument, citation);
                updateGroups(link, dbLink.getContribution());

                try {
                    contribution.update();
                    dbLink.update();

                    logger.debug("updated citation justification link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while saving contribution for citation justification link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name() + e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            forceContributionUpdate(context);
            forceContributionUpdate(citation.getContribution());

            transaction.commit();
            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(be.webdeb.core.api.citation.CitationPosition link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save citation position link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        be.webdeb.infra.persistence.model.Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        be.webdeb.infra.persistence.model.Debate debate =
                be.webdeb.infra.persistence.model.Debate.findById(link.getOriginId());
        if (debate == null) {
            logger.error("unable to retrieve debate " + link.getOriginId());
            throw new ObjectNotFoundException(Debate.class, link.getOriginId());
        }

        be.webdeb.infra.persistence.model.Tag subDebate =
                be.webdeb.infra.persistence.model.Tag.findById(link.getSubDebateId());
        if (!values.isBlank(link.getSubDebateId()) && (subDebate == null || subDebate.getTagtype().getEType() != ETagType.SUB_DEBATE_TAG)) {
            logger.error("unable to retrieve sub debate " + link.getSubDebateId());
            throw new ObjectNotFoundException(Tag.class, link.getSubDebateId());
        }

        be.webdeb.infra.persistence.model.Citation citation =
                be.webdeb.infra.persistence.model.Citation.findById(link.getDestinationId());
        if (citation == null) {
            logger.error("unable to retrieve destination citation " + link.getDestinationId());
            throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Citation.class, link.getDestinationId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.CitationPosition dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.CITATION_POSITION.id(), null);
                // create link
                dbLink = updatePositionLink(link, new be.webdeb.infra.persistence.model.CitationPosition(), debate, subDebate, citation);
                contribution.setCitationPosition(dbLink);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for citation position link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name() + " " + e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_POSITION_LINK, e);
                }

                dbLink.setContribution(contribution);
                dbLink.setIdContribution(contribution.getIdContribution());
                updateGroups(link, contribution);

                // save link
                try {
                    contribution.update();
                    dbLink.save();
                    link.setId(dbLink.getIdContribution());
                    logger.debug("saved citation position link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving contribution for citation position link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name() + " " +  e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_POSITION_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updatePositionLink(link, contribution.getCitationPosition(), debate, subDebate, citation);;
                updateGroups(link, dbLink.getContribution());

                try {
                    contribution.update();
                    dbLink.update();
                    logger.debug("updated citation position link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while saving contribution for citation position link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name());
                    throw new PersistenceException(PersistenceException.Key.SAVE_POSITION_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            forceContributionUpdate(debate.getContribution());
            forceContributionUpdate(citation.getContribution());

            transaction.commit();

            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public List<Citation> searchCitationByType(SearchContainer query) {
        List<be.webdeb.infra.persistence.model.Citation> citations = new ArrayList<>();
        ContextContribution contextContribution = retrieveContextContribution(query.getContext());

        if(!values.isBlank(query.getTerm()) || query.getSearchCitationType() == SearchContainer.ECitationBrowseType.BY_CONTRIBUTOR_CITATIONS ||
            (contextContribution != null && contextContribution.getType() == EContributionType.TEXT && query.getSearchCitationType() == SearchContainer.ECitationBrowseType.BY_EXCERPT)) {
            query.setTerm(query.getTerm().toLowerCase());

            query.setOnlyInText(contextContribution != null && contextContribution.getType() == EContributionType.TEXT);

            switch (query.getSearchCitationType()) {
                case BY_AUTHOR:
                    citations = be.webdeb.infra.persistence.model.Citation.searchByAuthorIds(
                            Actor.findByPartialName(query.getTerm(), -1, -1, -1).stream()
                                    .map(actor -> actor.getIdContribution().toString())
                                    .collect(Collectors.toList()),
                            query);
                    break;
                case BY_TEXT:
                    citations = be.webdeb.infra.persistence.model.Citation.searchByTextIds(
                            (values.isURL(query.getTerm()) ? Text.findMultipleByUrl(query.getTerm()) : Text.findByPartialTitle(query.getTerm())).stream()
                                    .map(text -> text.getIdContribution().toString())
                                    .collect(Collectors.toList()),
                            query);
                    break;
                case BY_DEBATE:
                    String matchedShade = null;

                    for (DebateShade debateShade : debateAccessor.getDebateShades()) {
                        for (Map.Entry<String, String> entry : debateShade.getNames().entrySet()) {
                            String toAnalyse = entry.getValue().toLowerCase();

                            if (query.getTerm().startsWith(toAnalyse)) {
                                matchedShade = toAnalyse;
                                query.setTerm(query.getTerm().replaceFirst(matchedShade, ""));
                                break;
                            }
                        }

                        if (matchedShade != null) {
                            break;
                        }
                    }

                    citations = be.webdeb.infra.persistence.model.Citation.searchByDebateIds(
                            Debate.findByTitleAndLang(query.getTerm(), null, query.getLang()).stream()
                                    .map(debate -> debate.getIdContribution().toString())
                                    .collect(Collectors.toList()),
                            query);
                    break;
                case BY_TAG:
                    citations = be.webdeb.infra.persistence.model.Citation.searchByTagIds(
                            Tag.findByPartialName(query.getTerm(), query.getLang(), -1).stream()
                                    .map(tag -> tag.getIdContribution().toString())
                                    .collect(Collectors.toList()),
                            query);
                    break;
                case BY_EXCERPT:
                    citations = be.webdeb.infra.persistence.model.Citation.searchByExcerpt(query);
                    break;
                case BY_CONTRIBUTOR_CITATIONS:
                    citations = be.webdeb.infra.persistence.model.Citation.searchByExcerptAndContributor(query);
                    break;


            }
        }

        return buildCitationList(citations);
    }

    @Override
    public List<be.webdeb.core.api.citation.CitationJustification> findCitationLinks(Long context, Long subContext, Long category, Long superArgument, int shade) {
        return buildCitationJustificationLinkList(
                CitationJustification.findLinksForContext(context, subContext, category, superArgument, shade));
    }

    @Override
    public boolean citationPositionLinkAlreadyExists(Long debateId, Long subDebateId, Long citationId, int shade) {
        return CitationPosition.findUnique(debateId, subDebateId, citationId, shade) != null;
    }

    @Override
    public be.webdeb.core.api.debate.Debate findLinkedDebate(Long citation, Long contributor, int group, Long rejectedDebate) {
        be.webdeb.infra.persistence.model.Citation c = be.webdeb.infra.persistence.model.Citation.findById(citation);

        if(c != null){
            Debate debate = c.findLinkedDebate(contributor, group, rejectedDebate);

            if (debate != null) {
                try {
                    return mapper.toDebate(debate);
                } catch (FormatException e) { }
            }
        }
        return null;
    }

    @Override
    public List<be.webdeb.core.api.debate.Debate> findLinkedDebates(Long citation, Long contributor, int group, Long rejectedDebate) {
        be.webdeb.infra.persistence.model.Citation c = be.webdeb.infra.persistence.model.Citation.findById(citation);

        if(c != null){
            return buildDebateList(c.findLinkedDebates(contributor, group, rejectedDebate));
        }
        return new ArrayList<>();
    }

    @Override
    public Citation random() {
        be.webdeb.infra.persistence.model.Citation citation = be.webdeb.infra.persistence.model.Citation.random();
        if (citation != null) {
            try {
                return mapper.toCitation(citation);
            } catch(FormatException e){
                logger.warn("unable to cast random citation");
            }
        } else {
            logger.warn("no citation for random");
        }
        return null;
    }

    @Override
    public Citation getSuggestionCitation(Long idContribution) {
        be.webdeb.infra.persistence.model.Citation citation = be.webdeb.infra.persistence.model.Citation.getSuggestionCitation(idContribution);

        if (citation != null) {
            try {
                return mapper.toCitation(citation);
            } catch(FormatException e){
                logger.warn("unable to cast citation " + citation.getIdContribution());
            }
        }

        return null;
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Update given db citation with API citation's shade and title
     *
     * @return the updated Citation
     */
    private be.webdeb.infra.persistence.model.Citation updateCitation(
            Citation apiCitation, be.webdeb.infra.persistence.model.Citation citation) {

        citation.setOriginalExcerpt(apiCitation.getOriginalExcerpt());
        citation.setWorkingExcerpt(apiCitation.getWorkingExcerpt());
        citation.setLanguage(TLanguage.find.byId(apiCitation.getLanguage().getCode()));
        return citation;
    }

    /**
     * Update given db justification link with api link
     *
     * @param apiLink       an API justification link
     * @param link          a DB justification link to update
     * @param context       the "to" DB context contribution
     * @param subContext    the tag sub context of the link, can be null
     * @param category      the tag category of the link, can be null
     * @param superArgument the super argument of the link, can be null
     * @param citation      the "destination" citation of the link
     * @return the updated DB citation justification link
     */
    private be.webdeb.infra.persistence.model.CitationJustification updateJustificationLink(be.webdeb.core.api.citation.CitationJustification apiLink,
                                                                                            be.webdeb.infra.persistence.model.CitationJustification link,
                                                                                            be.webdeb.infra.persistence.model.Contribution context,
                                                                                            Tag subContext,
                                                                                            Tag category,
                                                                                            be.webdeb.infra.persistence.model.Argument superArgument,
                                                                                            be.webdeb.infra.persistence.model.Citation citation) {

        link.setContext(context);
        link.setSubContext(subContext);
        link.setCategory(category);
        link.setSuperArgument(superArgument);
        link.setCitation(citation);
        link.setShade(TJustificationLinkShadeType.find.byId(apiLink.getLinkType().getType()));
        link.setOrder(apiLink.getOrder());

        return link;
    }

    /**
     * Update given db justification link with api link
     *
     * @param apiLink       an API justification link
     * @param link          a DB justification link to update
     * @param debate       the "to" DB debate
     * @param subDebate       the tag sub debate of the link
     * @param citation      the "destination" citation of the link
     * @return the updated DB citation justification link
     */
    private be.webdeb.infra.persistence.model.CitationPosition updatePositionLink(be.webdeb.core.api.citation.CitationPosition apiLink,
                                                                                            be.webdeb.infra.persistence.model.CitationPosition link,
                                                                                            be.webdeb.infra.persistence.model.Debate debate,
                                                                                            Tag subDebate,
                                                                                            be.webdeb.infra.persistence.model.Citation citation) {

        link.setDebate(debate);
        link.setSubDebate(subDebate);
        link.setCitation(citation);
        link.setShade(TPositionLinkShadeType.find.byId(apiLink.getLinkType().getType()));

        return link;
    }
}
