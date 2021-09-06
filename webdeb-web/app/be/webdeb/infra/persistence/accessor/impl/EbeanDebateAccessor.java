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

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.EModificationStatus;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateExternalUrl;
import be.webdeb.core.api.debate.DebateHasText;
import be.webdeb.core.api.debate.DebateSimilarity;
import be.webdeb.core.api.debate.DebateSimple;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.*;
import java.util.stream.Collectors;

import be.webdeb.infra.persistence.model.*;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This accessor handles retrieval and save actions of Debates.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanDebateAccessor extends AbstractContributionAccessor<DebateFactory> implements DebateAccessor {

    @Inject
    private ArgumentAccessor argumentAccessor;

    private List<DebateShade> debateShades = null;
    
    @Override
    public Debate retrieve(Long id, boolean hit) {
        be.webdeb.infra.persistence.model.Debate debate = be.webdeb.infra.persistence.model.Debate.findById(id);

        if (debate != null) {
            try {
                Debate api = mapper.toDebate(debate);
                if (hit) {
                    addHitToContribution(debate.getContribution());
                }
                return api;
            } catch (FormatException e) {
                logger.error("unable to cast retrieved debate " + id, e);
            }
        } else {
            logger.warn("no debate found for id " + id);
        }
        return null;
    }

    @Override
    public DebateSimilarity retrieveSimilarityLink(Long id) {
        be.webdeb.infra.persistence.model.DebateSimilarity link = be.webdeb.infra.persistence.model.DebateSimilarity.findById(id);
        if (link != null) {
            try {
                return mapper.toDebateSimilarity(link);
            } catch (Exception e) {
                logger.error("unable to cast retrieved debate similarity link " + id, e);
            }
        } else {
            logger.warn("no debate similarity link found for id " + id);
        }
        return null;
    }

    @Override
    public be.webdeb.core.api.contribution.link.ContextHasSubDebate retrieveContextHasSubDebate(Long id) {
        be.webdeb.infra.persistence.model.ContextHasSubDebate link = be.webdeb.infra.persistence.model.ContextHasSubDebate.findById(id);
        if (link != null) {
            try {
                return mapper.toContextHasSubDebate(link);
            } catch (Exception e) {
                logger.error("unable to cast retrieved context has subdebate link " + id, e);
            }
        } else {
            logger.warn("no context has subdebate link found for id " + id);
        }
        return null;
    }

    @Override
    public Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> save(Debate contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save debate " + contribution.getFullTitle() + " with id " + contribution.getId() + " in group " + contribution.getInGroups());
        Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> contributions = new HashMap<>();

        Contribution c = checkContribution(contribution, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);
        
        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.Debate debate = be.webdeb.infra.persistence.model.Debate.findById(contribution.getId());

            if (debate == null) {
                // new debate
                logger.debug("start creation of new debate " + contribution.getFullTitle());
                status = EModificationStatus.CREATE;

                // create contribution super type
                c = initContribution(EContributionType.DEBATE.id(), contribution.getFullTitle());

                //create new debate
                debate = new be.webdeb.infra.persistence.model.Debate();

                // create debate and binding
                c.setDebate(debate);

                try {
                    c.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution " + contribution.getFullTitle());
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE, e);
                }

                // set id of debate
                debate.setIdContribution(c.getIdContribution());
                debate.setContribution(c);

                updateDebate(contribution, debate, currentGroup, contributor, c.getIdContribution());

                // manage bindings to actors
                bindActorsToContribution(c, contribution, currentGroup, contributor, contributions);
                // manage bindings to tags
                bindTagsToContribution(c, contribution, contributor, contributions);
                // manage places
                savePlaces(contribution.getPlaces(), c);
                // update groups
                updateGroups(contribution, c);

                try {
                    c.update();
                    debate.save();

                    debate.setExternalUrls(toDebateExternalUrls(contribution, debate));
                } catch (Exception e) {
                    logger.error("error while saving debate " + contribution.getFullTitle(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE, e);
                }
            } else {
                logger.debug("start update of existing debate " + debate.getIdContribution());
                status = EModificationStatus.UPDATE;

                updateDebate(contribution, debate, currentGroup, contributor, debate.getIdContribution());

                // manage places
                savePlaces(contribution.getPlaces(), c);
                // manage groups for this debate and originating text
                updateGroups(contribution, debate.getContribution());

                debate.getContribution().setSortkey(contribution.getFullTitle());

                try {
                    // force update if only classes or bindings have been changed
                    c.update();
                    debate.update();

                    debate.setExternalUrls(toDebateExternalUrls(contribution, debate));

                    // manage bindings to actors, retrieve them, save unexisting ones and remove others
                    updateActorBindings(contribution, debate.getContribution(), currentGroup, contributor, contributions);

                    // manage tags
                    bindTagsToContribution(c, contribution, contributor, contributions);

                } catch (Exception e) {
                    logger.error("error while updating debate " + debate.getIdContribution());
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE, e);
                }
            }
            // bind contributor to this debate
            bindContributor(debate.getContribution(), dbContributor, status);
            // set new id for given contribution
            contribution.setId(debate.getIdContribution());

            transaction.commit();
            logger.info("saved " + debate.toString());

        } finally {
            transaction.end();
        }

        return contributions;
    }

    @Override
    public void save(DebateSimilarity link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save debate  similarity link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // check if given link contains valid debates
        be.webdeb.infra.persistence.model.Debate debateFrom =
                be.webdeb.infra.persistence.model.Debate.findById(link.getOrigin().getId());
        if (debateFrom == null) {
            logger.error("unable to retrieve origin debate " + link.getOrigin().getId());
            throw new ObjectNotFoundException(Debate.class, link.getOrigin().getId());
        }

        be.webdeb.infra.persistence.model.Debate debateTo =
                be.webdeb.infra.persistence.model.Debate.findById(link.getDestination().getId());
        if (debateTo == null) {
            logger.error("unable to retrieve destination debate " + link.getDestination().getId());
            throw new ObjectNotFoundException(Debate.class, link.getDestination().getId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.DebateSimilarity dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.DEBATE_SIMILARITY.id(), null);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for debate similarity link between " + link.getOrigin().getId() + " "
                            + link.getDestination().getId() + " of type " + link.getLinkType().getEType().name());
                    throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
                }

                // create link
                dbLink = new be.webdeb.infra.persistence.model.DebateSimilarity();
                contribution.setDebateSimilarityLink(dbLink);
                dbLink.setContribution(contribution);
                dbLink = updateSimilarityLink(link, dbLink, debateFrom, debateTo);
                updateGroups(link, contribution);
                // save link
                try {
                    contribution.save();
                    dbLink.setIdContribution(contribution.getIdContribution());
                    dbLink.save();
                    logger.debug("saved debate similarity link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving debate similarity link from " + link.getOrigin().getId() + " to "
                            + link.getDestination().getId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateSimilarityLink(link, contribution.getDebateSimilarityLink(), debateFrom, debateTo);
                updateGroups(link, dbLink.getContribution());

                try {
                    dbLink.update();
                    logger.debug("updated debate similarity link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while updating debate similarity link " + link.getId() + " from " + link.getOrigin().getId()
                            + " to " + link.getDestination().getId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            transaction.commit();
            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(DebateHasText link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save debate has text link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // check if given link contains valid debates
        be.webdeb.infra.persistence.model.Debate debate =
                be.webdeb.infra.persistence.model.Debate.findById(link.getOriginId());
        if (debate == null) {
            logger.error("unable to retrieve origin debate " + link.getOriginId());
            throw new ObjectNotFoundException(Debate.class, link.getOriginId());
        }

        be.webdeb.infra.persistence.model.Text text =
                be.webdeb.infra.persistence.model.Text.findById(link.getDestinationId());
        if (text == null) {
            logger.error("unable to retrieve destination text " + link.getDestinationId());
            throw new ObjectNotFoundException(Text.class, link.getDestinationId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.DebateHasText dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.DEBATE_HAS_TEXT.id(), null);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for debate has text link between " + link.getOriginId() + " "
                            + link.getDestinationId());
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE_HAS_TEXT_LINK, e);
                }

                // create link
                dbLink = new be.webdeb.infra.persistence.model.DebateHasText();
                contribution.setDebateHasText(dbLink);
                dbLink.setContribution(contribution);
                dbLink = updateSimilarityLink(link, dbLink, debate, text);
                updateGroups(link, contribution);
                // save link
                try {
                    contribution.save();
                    dbLink.setIdContribution(contribution.getIdContribution());
                    dbLink.save();
                    logger.debug("saved debate has text link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving debate has text link from " + link.getOriginId() + " to "
                            + link.getDestinationId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE_HAS_TEXT_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateSimilarityLink(link, contribution.getDebateHasText(), debate, text);
                updateGroups(link, dbLink.getContribution());

                try {
                    dbLink.update();
                    logger.debug("updated debate has text link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while updating debate has text link " + link.getId() + " from " + link.getOriginId()
                            + " to " + link.getDestinationId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_DEBATE_HAS_TEXT_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            transaction.commit();
            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(be.webdeb.core.api.contribution.link.ContextHasSubDebate link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save context has subdebate link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        be.webdeb.infra.persistence.model.Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // check if given link contains valid context and debate
        be.webdeb.infra.persistence.model.Contribution context =
                be.webdeb.infra.persistence.model.Contribution.findById(link.getOrigin().getId());
        if (context == null || !context.getContributionType().getEContributionType().isContextContribution()) {
            logger.error("unable to retrieve origin context " + link.getOrigin().getId());
            throw new ObjectNotFoundException(be.webdeb.core.api.contribution.ContextContribution.class, link.getOrigin().getId());
        }

        be.webdeb.infra.persistence.model.Debate debateTo =
                be.webdeb.infra.persistence.model.Debate.findById(link.getDestination().getId());
        if (debateTo == null) {
            logger.error("unable to retrieve destination debate " + link.getDestination().getId());
            throw new ObjectNotFoundException(be.webdeb.core.api.debate.Debate.class, link.getDestination().getId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.ContextHasSubDebate dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.CONTEXT_HAS_SUBDEBATE.id(), null);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for context has sub debate link between " +
                            link.getOrigin().getId() + " " + link.getDestination().getId());
                    throw new PersistenceException(PersistenceException.Key.SAVE_CONTEXT_HAS_SUBDEBATE_LINK, e);
                }

                // create link
                dbLink = new be.webdeb.infra.persistence.model.ContextHasSubDebate();
                contribution.setContextHasSubDebate(dbLink);
                dbLink.setContribution(contribution);
                dbLink = updateContextHasSubDebateLink(link, dbLink, context, debateTo);
                updateGroups(link, contribution);
                // save link
                try {
                    contribution.save();
                    dbLink.setIdContribution(contribution.getIdContribution());
                    dbLink.save();
                    logger.debug("saved context has sub debate link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving context has sub debate link from " + link.getOrigin().getId() + " to "
                            + link.getDestination().getId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_CONTEXT_HAS_SUBDEBATE_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateContextHasSubDebateLink(link, contribution.getContextHasSubDebate(), context, debateTo);
                updateGroups(link, dbLink.getContribution());

                try {
                    dbLink.update();
                    logger.debug("updated context has sub debate link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while updating context has sub debate link " + link.getId() + " from " + link.getOrigin().getId()
                            + " to " + link.getDestination().getId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_CONTEXT_HAS_SUBDEBATE_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            transaction.commit();
            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public synchronized List<DebateShade> getDebateShades() {
        if (debateShades == null) {
            debateShades = TDebateShadeType.find.all().stream()
                    .map(t -> factory.createDebateShade(t.getIdShade(), new LinkedHashMap<>(t.getTechnicalNames())))
                    .collect(Collectors.toList());
        }
        return debateShades;
    }

    @Override
    public boolean isSimilarWith(Long debateToCompare, Long debate) {
        return be.webdeb.infra.persistence.model.Debate.similarDebates(debateToCompare, debate);
    }

    @Override
    public List<DebateExternalUrl> getExternalUrls(Long id) {
        be.webdeb.infra.persistence.model.Debate debate = be.webdeb.infra.persistence.model.Debate.findById(id);
        if(debate != null) {
            return buildDebateExternalUrlLinkList(debate.getExternalUrls());
        }
        return new ArrayList<>();
    }

    @Override
    public List<DebateSimilarity> getSimilarityLinks(Long id) {
        return buildDebateSimilarityLinkList(be.webdeb.infra.persistence.model.DebateSimilarity.find(id));
    }

    @Override
    public List<Debate> findByTitle(String title, String lang, int fromIndex, int toIndex) {
        return buildDebateList(be.webdeb.infra.persistence.model.Debate.findByTitleAndLang(title, null, lang));
    }

    @Override
    public List<Debate> findByTitleAndShade(String title, Integer shade, String lang, int fromIndex, int toIndex) {
        return buildDebateList(be.webdeb.infra.persistence.model.Debate.findByTitleAndLang(title, shade, lang));
    }

    @Override
    public List<Citation> getCitationsFromPositions(Long id) {
        return null;
    }

    @Override
    public List<Citation> getCitationsFromPositions(Long id, Long subDebate) {
        return null;
    }

    @Override
    public List<CitationPosition> getAllCitationPositionLinks(Long id, Long subDebate) {
        return null;
    }

    @Override
    public List<CitationPosition> getActorCitationPositions(Long id, Long actor) {
        return null;
    }

    @Override
    public List<be.webdeb.core.api.text.Text> findLinkedTexts(Long id) {
        return buildTextList(be.webdeb.infra.persistence.model.DebateHasText.findLinkByDebate(id)
                .stream()
                .map(be.webdeb.infra.persistence.model.DebateHasText::getText)
                .collect(Collectors.toList()));
    }

    @Override
    public Debate random() {
        try {
            return mapper.toDebate(be.webdeb.infra.persistence.model.Debate.random());
        } catch (FormatException e) {
            logger.error("unable to cast retrieved random tag", e);
        }
        return null;
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Update given db debate with API debate
     *
     * @param apiDebate the debate as the contributor want
     * @param dbDebate  the previous state of the debate
     */
    private void updateDebate(Debate apiDebate, be.webdeb.infra.persistence.model.Debate dbDebate, int currentGroup, Long contributor, Long debateId) throws PermissionException, PersistenceException {

        dbDebate.setDescription(apiDebate.getDescription());
        dbDebate.setMultiple(apiDebate.isMultiple());
        dbDebate.setPicture(updateContributorPicture(apiDebate.getPictureExtension(), debateId, contributor));

        DebateSimple apiDebateSimple = (DebateSimple) apiDebate;

        ArgumentShaded newArgumentShaded = ArgumentShaded.
                findUniqueByTitleAndLang(apiDebateSimple.getArgument().getTitle(), apiDebateSimple.getArgument().getLanguage().getCode(), apiDebateSimple.getArgument().getArgumentShade().getType());

        if(newArgumentShaded == null) {
            argumentAccessor.save(apiDebateSimple.getArgument(), currentGroup, contributor);
            newArgumentShaded  = ArgumentShaded.findById(apiDebateSimple.getArgument().getId());
        }

        dbDebate.setArgument(newArgumentShaded);
        dbDebate.setShade(TDebateShadeType.find.byId(apiDebateSimple.getShade().getType()));
    }

    /**
     * Update given db similarity link with api link, setting from and to debates according to shade
     *
     * @param apiLink an API similarity link
     * @param link    a DB similarity link to update
     * @param from    the "from" DB debate (corresponding to apiLink.getDebateFrom)
     * @param to      the "to" DB debate (corresponding to apiLink.getDebateTo)
     * @return the updated DB debate similarity  link
     */
    private be.webdeb.infra.persistence.model.DebateSimilarity updateSimilarityLink(DebateSimilarity apiLink,
                                                                                      be.webdeb.infra.persistence.model.DebateSimilarity link,
                                                                                      be.webdeb.infra.persistence.model.Debate from,
                                                                                      be.webdeb.infra.persistence.model.Debate to) {
        link.setDebateFrom(from);
        link.setDebateTo(to);
        link.setShade(TSimilarityLinkShadeType.find.byId(apiLink.getLinkType().getType()));

        return link;
    }

    /**
     * Update given db debate has text link with api link, setting debate and text
     *
     * @param apiLink an API similarity link
     * @param link    a DB similarity link to update
     * @param debate  the debate of the link
     * @param text      the text of the link
     * @return the updated DB debate has text link
     */
    private be.webdeb.infra.persistence.model.DebateHasText updateSimilarityLink(DebateHasText apiLink,
                                                                                    be.webdeb.infra.persistence.model.DebateHasText link,
                                                                                    be.webdeb.infra.persistence.model.Debate debate,
                                                                                    be.webdeb.infra.persistence.model.Text text) {
        link.setDebate(debate);
        link.setText(text);
        return link;
    }

    /**
     * Update given db context has subdebate link with api link, setting from context and to debate
     *
     * @param apiLink an API link
     * @param link    a DB link to update
     * @param from    the "from" DB context contribution (corresponding to apiLink.getContext)
     * @param to      the "to" DB debate (corresponding to apiLink.getDebate)
     * @return the updated DB link
     */
    private be.webdeb.infra.persistence.model.ContextHasSubDebate updateContextHasSubDebateLink(be.webdeb.core.api.contribution.link.ContextHasSubDebate apiLink,
                                                                                                be.webdeb.infra.persistence.model.ContextHasSubDebate link,
                                                                                                be.webdeb.infra.persistence.model.Contribution from,
                                                                                                be.webdeb.infra.persistence.model.Debate to) {
        link.setContext(from);
        link.setDebate(to);

        ArgumentJustification justification = ArgumentJustification.findById(apiLink.getArgumentJustificationLinkId());
        if(justification != null)
            link.setArgument(justification);

        return link;
    }
}
