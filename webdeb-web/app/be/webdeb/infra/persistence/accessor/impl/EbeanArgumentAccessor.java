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

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentDictionary;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.argument.ArgumentShaded;
import be.webdeb.core.api.argument.ArgumentSimilarity;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.ESimilarityLinkShade;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Contribution;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This accessor handles argument-related persistence actions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanArgumentAccessor extends AbstractContributionAccessor<ArgumentFactory> implements ArgumentAccessor {

    private List<ArgumentType> argumentTypes = null;
    private List<ArgumentShade> argumentShades = null;

    @Override
    public Argument retrieve(Long id, boolean hit) {
        be.webdeb.infra.persistence.model.Argument argument = be.webdeb.infra.persistence.model.Argument.findById(id);
        if (argument != null) {
            try {
                Argument api = mapper.toArgument(argument);
                if (hit) {
                    addHitToContribution(argument.getContribution());
                }
                return api;
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument " + id, e);
            }
        } else {
            if(id != null)
                logger.warn("no argument found for id " + id);
        }
        return null;
    }

    @Override
    public ArgumentShaded retrieveShaded(Long id, boolean hit) {
        be.webdeb.infra.persistence.model.ArgumentShaded argument = be.webdeb.infra.persistence.model.ArgumentShaded.findById(id);
        if (argument != null) {
            try {
                ArgumentShaded api = (ArgumentShaded) mapper.toArgument(argument.getArgument());
                if (hit) {
                    addHitToContribution(argument.getArgument().getContribution());
                }
                return api;
            } catch (Exception e) {
                logger.error("unable to cast retrieved argument " + id, e);
            }
        } else {
            logger.warn("no argument found for id " + id);
        }
        return null;
    }

    @Override
    public ArgumentDictionary retrieveDictionary(Long id) {
        be.webdeb.infra.persistence.model.ArgumentDictionary dictionary =
                be.webdeb.infra.persistence.model.ArgumentDictionary.findById(id);
        if (dictionary != null) {
            try {
                return mapper.toArgumentDictionary(dictionary);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument dictionary " + id, e);
            }
        }
        return null;
    }

    @Override
    public ArgumentJustification retrieveJustificationLink(Long id) {
        be.webdeb.infra.persistence.model.ArgumentJustification link = be.webdeb.infra.persistence.model.ArgumentJustification.findById(id);
        if (link != null) {
            try {
                return mapper.toArgumentJustification(link);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument justification link " + id, e);
            }
        } else {
            logger.warn("no argument justification link found for id " + id);
        }
        return null;
    }

    @Override
    public ArgumentSimilarity retrieveSimilarityLink(Long id) {
        be.webdeb.infra.persistence.model.ArgumentSimilarity link = be.webdeb.infra.persistence.model.ArgumentSimilarity.findById(id);
        if (link != null) {
            try {
                return mapper.toArgumentSimilarity(link);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument similarity link " + id, e);
            }
        } else {
            logger.warn("no argument similarity link found for id " + id);
        }
        return null;
    }

    @Override
    public void save(Argument contribution, int currentGroup, Long contributor)
            throws PermissionException, PersistenceException {
        logger.debug("try to save argument " + contribution.toString() + " in group " + contribution.getInGroups());

        Argument formerContribution = null;
        Argument existing = findUniqueByTitleLangAndShade(
                contribution.getTitle(),
                contribution.getLanguage().getCode(),
                (contribution.getArgumentType() == EArgumentType.SHADED ? ((ArgumentShaded) contribution).getArgumentShade().getEType() : null));

        if(existing != null && !existing.getId().equals(contribution.getId())) {
            if(!values.isBlank(contribution.getId())) {
                merge(contribution.getId(), existing.getId(), contributor);
                existing.setVersion(new Date().getTime());
            }
            formerContribution = contribution;
            contribution.setId(existing.getId());
            contribution = existing;
        }

        Contribution c = checkContribution(contribution, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // open transaction and handle the whole save chain
        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.Argument argument = be.webdeb.infra.persistence.model.Argument.findById(contribution.getId());

            if (argument == null) {
                // new argument
                logger.debug("start creation of new argument " + contribution.getTitle());
                status = EModificationStatus.CREATE;

                // create contribution super type
                c = initContribution(EContributionType.ARGUMENT.id(), contribution.getFullTitle());

                // create argument and binding
                argument = new be.webdeb.infra.persistence.model.Argument();
                c.setArgument(argument);

                try {
                    c.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution " + contribution.getTitle());
                    throw new PersistenceException(PersistenceException.Key.SAVE_ARGUMENT, e);
                }

                // set id of argument
                argument.setIdContribution(c.getIdContribution());
                argument.setContribution(c);

                updateArgument(contribution, argument, currentGroup, contributor, true);
                // manage bindings to tags
                bindTagsToContribution(c, contribution, contributor);
                // update groups
                updateGroups(contribution, c);

            } else {
                logger.debug("start update of existing argument " + argument.getIdContribution());
                status = EModificationStatus.UPDATE;

                updateArgument(contribution, argument, currentGroup, contributor, false);

                // manage bindings to tags
                bindTagsToContribution(c, contribution, contributor);
                // manage groups for this argument and originating text
                updateGroups(contribution, argument.getContribution());

                argument.getContribution().setSortkey(contribution.getFullTitle());
                argument.getContribution().update();
            }
            // bind contributor to this argument
            bindContributor(argument.getContribution(), dbContributor, status);
            // set new id for given contribution
            contribution.setId(argument.getIdContribution());

            transaction.commit();
            logger.info("saved " + argument.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(ArgumentDictionary dictionary, int currentGroup, Long contributor) throws PermissionException, PersistenceException{
        logger.debug("try to save argument dictionary " + dictionary.toString());

        Contribution c = checkContribution(dictionary, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        be.webdeb.infra.persistence.model.ArgumentDictionary dbDictionary =
                be.webdeb.infra.persistence.model.ArgumentDictionary.findById(dictionary.getId());

        // open transaction and handle the whole save chain
        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;

            if (dbDictionary == null) {
                // new argument dictionary
                logger.debug("start creation of new argument dictionary " + dictionary.getTitle());
                status = EModificationStatus.CREATE;

                try {
                    // create contribution super type
                    c = initContribution(EContributionType.ARGUMENT_DICTIONARY.id(), dictionary.getTitle());
                    dbDictionary = new be.webdeb.infra.persistence.model.ArgumentDictionary();
                    c.setArgumentDictionary(dbDictionary);
                    c.save();

                    // set id of argument dictionary
                    dbDictionary.setIdContribution(c.getIdContribution());
                    dbDictionary.setContribution(c);
                    updateArgumentDictionary(dictionary.getTitle(), dictionary.getLanguage().getCode(), dbDictionary);
                    dbDictionary.save();

                    // update groups
                    updateGroups(dictionary, c);
                } catch (Exception e) {
                    logger.error("error while saving argument dictionary " + dictionary.getTitle(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_ARGUMENT_DICTIONARY, e);
                }
            } else {
                // update argument dictionary
                logger.debug("start update of existing argument dictionary " + dictionary.getId());
                status = EModificationStatus.UPDATE;

                try {
                    updateArgumentDictionary(dictionary.getTitle(), dictionary.getLanguage().getCode(), dbDictionary);

                    dbDictionary.update();

                    // manage groups for this argument and originating text
                    updateGroups(dictionary, dbDictionary.getContribution());

                    dbDictionary.getContribution().setSortkey(dictionary.getTitle());
                    dbDictionary.getContribution().update();
                } catch (Exception e) {
                    logger.error("error while updating argument dictionary " + dictionary.getId());
                    throw new PersistenceException(PersistenceException.Key.SAVE_ARGUMENT_DICTIONARY, e);
                }
            }
            // bind contributor to this argument
            bindContributor(dbDictionary.getContribution(), dbContributor, status);
            // set new id for given dictionary
            dictionary.setId(dbDictionary.getIdContribution());

            transaction.commit();
            logger.info("saved " + dictionary.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(ArgumentJustification link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save argument justification link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        Contribution contribution = checkContribution(link, contributor, currentGroup);
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
            logger.error("unable to retrieve super argument " + link.getSuperArgumentId());
            throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Argument.class, link.getSuperArgumentId());
        }

        be.webdeb.infra.persistence.model.Argument argument =
                be.webdeb.infra.persistence.model.Argument.findById(link.getDestinationId());
        if (argument == null) {
            logger.error("unable to retrieve destination argument " + link.getDestinationId());
            throw new ObjectNotFoundException(be.webdeb.infra.persistence.model.Argument.class, link.getDestinationId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.ArgumentJustification dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.ARGUMENT_JUSTIFICATION.id(), null);
                // create link
                dbLink = updateJustificationLink(link, new be.webdeb.infra.persistence.model.ArgumentJustification(), context, subContext, category, superArgument, argument);
                contribution.setArgumentJustificationLink(dbLink);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for argument justification link between " + link.getOriginId() + " "
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
                    logger.debug("saved argument justification link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving contribution for argument justification link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name() + " " +  e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateJustificationLink(link, contribution.getArgumentJustificationLink(), context, subContext, category, superArgument, argument);
                updateGroups(link, dbLink.getContribution());

                try {
                    contribution.update();
                    dbLink.update();
                    logger.debug("updated argument justification link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while saving contribution for argument justification link between " + link.getOriginId() + " "
                            + link.getDestinationId() + " of type " + link.getType().name());
                    throw new PersistenceException(PersistenceException.Key.SAVE_JUSTIFICATION_LINK, e);
                }
            }
            // save link to contributor
            bindContributor(dbLink.getContribution(), dbContributor, status);

            forceContributionUpdate(context);

            transaction.commit();
            logger.info("saved " + dbLink.toString());

        } finally {
            transaction.end();
        }
    }

    @Override
    public void save(ArgumentSimilarity link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save argument similarity link " + link.toString() + " in group " + currentGroup);

        // ensure given parameters are ok
        Contribution contribution = checkContribution(link, contributor, currentGroup);
        Contributor dbContributor = checkContributor(contributor, currentGroup);

        // check if given link contains valid arguments
        be.webdeb.infra.persistence.model.Argument argumentFrom =
                be.webdeb.infra.persistence.model.Argument.findById(link.getOrigin().getId());
        if (argumentFrom == null) {
            logger.error("unable to retrieve origin argument " + link.getOrigin().getId());
            throw new ObjectNotFoundException(Argument.class, link.getOrigin().getId());
        }

        be.webdeb.infra.persistence.model.Argument argumentTo =
                be.webdeb.infra.persistence.model.Argument.findById(link.getDestination().getId());
        if (argumentTo == null) {
            logger.error("unable to retrieve destination argument " + link.getDestination().getId());
            throw new ObjectNotFoundException(Argument.class, link.getDestination().getId());
        }

        Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
        try {
            EModificationStatus status;
            be.webdeb.infra.persistence.model.ArgumentSimilarity dbLink;

            if (contribution == null) {
                status = EModificationStatus.CREATE;

                // create and save contribution supertype
                contribution = initContribution(EContributionType.ARGUMENT_SIMILARITY.id(), null);
                try {
                    contribution.save();
                } catch (Exception e) {
                    logger.error("error while saving contribution for argument similarity link between " + link.getOrigin().getId() + " "
                            + link.getDestination().getId() + " of type " + link.getLinkType().getEType().name());
                    throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
                }

                // create link
                dbLink = new be.webdeb.infra.persistence.model.ArgumentSimilarity();
                contribution.setArgumentSimilarityLink(dbLink);
                dbLink.setContribution(contribution);
                dbLink = updateSimilarityLink(link, dbLink, argumentFrom, argumentTo);
                updateGroups(link, contribution);
                // save link
                try {
                    contribution.save();
                    dbLink.setIdContribution(contribution.getIdContribution());
                    dbLink.save();
                    logger.debug("saved argument similarity link " + dbLink);

                } catch (Exception e) {
                    logger.error("error while saving argument similarity link from " + link.getOrigin().getId() + " to "
                            + link.getDestination().getId(), e);
                    throw new PersistenceException(PersistenceException.Key.SAVE_SIMILARITY_LINK, e);
                }

            } else {
                status = EModificationStatus.UPDATE;
                // update existing link
                dbLink = updateSimilarityLink(link, contribution.getArgumentSimilarityLink(), argumentFrom, argumentTo);
                updateGroups(link, dbLink.getContribution());

                try {
                    dbLink.update();
                    logger.debug("updated argument similarity link " + dbLink);
                } catch (Exception e) {
                    logger.error("error while updating argument similarity link " + link.getId() + " from " + link.getOrigin().getId()
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
    public synchronized List<ArgumentSimilarity> getSimilarityLinks(Long id) {
        be.webdeb.infra.persistence.model.Argument arg =
                be.webdeb.infra.persistence.model.Argument.findById(id);
        List<ArgumentSimilarity> result = new ArrayList<>();
        if (arg != null) {
            result.addAll(buildArgumentSimilarityLinkList(arg.getSimilarArguments()));
        }
        return result;
    }

    @Override
    public synchronized Set<Long> getSimilarIds(Long id, ESimilarityLinkShade shade) {
        be.webdeb.infra.persistence.model.Argument arg =
                be.webdeb.infra.persistence.model.Argument.findById(id);
        Set<Long> result = new HashSet<>();
        if (arg != null) {
            arg.getSimilarArguments().forEach(e -> {
                if (e.getArgumentTo().getIdContribution().equals(id)) {
                    result.add(e.getArgumentFrom().getIdContribution());
                } else {
                    result.add(e.getArgumentTo().getIdContribution());
                }
            });
        }
        return result;
    }

    /*
     * GETTERS FOR PREDEFINED VALUES
     */

    @Override
    public synchronized List<ArgumentType> getArgumentTypes() {
        if (argumentTypes == null) {
            argumentTypes = TArgumentType.find.all().stream()
                    .map(t -> factory.createArgumentType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames())))
                    .collect(Collectors.toList());
        }
        return argumentTypes;
    }

    @Override
    public synchronized List<ArgumentShade> getArgumentShades() {
        if (argumentShades == null) {
            argumentShades = TArgumentShadeType.findAll().stream()
                    .map(t -> factory.createArgumentShade(t.getIdShade(), new LinkedHashMap<>(t.getTechnicalNames())))
                    .collect(Collectors.toList());
        }
        return argumentShades;
    }

    @Override
    public boolean isSimilarWith(Long argToCompare, Long argument) {
        return be.webdeb.infra.persistence.model.Argument.similarArguments(argToCompare, argument);
    }

    @Override
    public List<Argument> findByTitle(String title, String lang, int type, int shade, int fromIndex, int toIndex) {
        return buildArgumentList(be.webdeb.infra.persistence.model.Argument.findByTitleAndLang(title, lang, type, shade, fromIndex, toIndex));
    }

    @Override
    public Argument findUniqueByTitleLangAndShade(String title, String lang, EArgumentShade shade) {
        be.webdeb.infra.persistence.model.Argument argument =
                be.webdeb.infra.persistence.model.Argument.findUniqueByTitleLangAndShade(title, lang, shade);

        if (argument != null) {
            try {
                return mapper.toArgument(argument);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument " + title, e);
            }
        } else {
            logger.warn("no argument found for title " + title);
        }
        return null;
    }

    @Override
    public ArgumentDictionary findUniqueDictionaryByTitle(String title, String lang) {
        be.webdeb.infra.persistence.model.ArgumentDictionary dictionary =
                be.webdeb.infra.persistence.model.ArgumentDictionary.findUniqueByTitleAndLang(title, lang);

        if (dictionary != null) {
            try {
                return mapper.toArgumentDictionary(dictionary);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument dictionary " + dictionary.getIdContribution(), e);
            }
        }
        return null;
    }

    @Override
    public List<ArgumentDictionary> findDictionaryByTitle(String title, String lang, int fromIndex, int toIndex) {
        return buildArgumentDictionaryList(be.webdeb.infra.persistence.model.ArgumentDictionary.
                findByTitleAndLang(title, lang, -1, fromIndex, toIndex));
    }

    @Override
    public boolean argumentJustificationLinkAlreadyExists(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade) {
        return be.webdeb.infra.persistence.model.ArgumentJustification.findUnique(context, subContext, category, superArgument, argument, shade) != null;
    }

    @Override
    public ArgumentJustification findArgumentJustification(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade) {
        be.webdeb.infra.persistence.model.ArgumentJustification link = be.webdeb.infra.persistence.model.ArgumentJustification
                .findUnique(context, subContext, category, superArgument, argument, shade);

        if (link != null) {
            try {
                return mapper.toArgumentJustification(link);
            } catch (FormatException e) {
                logger.error("unable to cast retrieved argument justification link", e);
            }
        } else {
            logger.warn("no argument justification link found.");
        }
        return null;
    }

    @Override
    public int getMaxArgumentJustificationLinkOrder(Long context, Long subContext, Long category, Long superArgument, int shade) {
        return be.webdeb.infra.persistence.model.ArgumentJustification.getMaxArgumentJustificationLinkOrder(context, subContext, category, superArgument, shade);
    }

    @Override
    public List<ArgumentJustification> findArgumentLinks(Long context, Long subContext, Long category, Long superArgument, int shade) {
        return buildArgumentJustificationLinkList(
                be.webdeb.infra.persistence.model.ArgumentJustification.findLinksForContext(context, subContext, category, superArgument, shade));
    }

    @Override
    public int getArgumentNbCitationsLink(Long context, Long subContext, Long category, Long argument, Integer shade, Long contributor, int group) {
        return be.webdeb.infra.persistence.model.Argument.getNbCitationsLinks(context, subContext, category, argument, shade, contributor, group);
    }

    @Override
    public Argument random() {
        try {
            return mapper.toArgument(be.webdeb.infra.persistence.model.Argument.random());
        } catch (FormatException e) {
            logger.error("unable to cast retrieved random argument", e);
        }
        return null;
    }

    /*
     * PRIVATE HELPERS
     */

    /**
     * Update given db argument with API argument's shade and title
     *
     * @param apiArgument the argument as the contributor want
     * @param dbArgument  the previous state of the argument
     */
    private void updateArgument(Argument apiArgument, be.webdeb.infra.persistence.model.Argument dbArgument, int currentGroup, Long contributor, boolean newArgument)
            throws PermissionException, PersistenceException {

        be.webdeb.infra.persistence.model.ArgumentDictionary dictionary =
                be.webdeb.infra.persistence.model.ArgumentDictionary.findUniqueByTitleAndLang(apiArgument.getTitle(), apiArgument.getLanguage().getCode());

        be.webdeb.infra.persistence.model.ArgumentDictionary formerDictionary = dbArgument.getDictionary();

        // If given dictionary doesn't exists or it doesn't match title or language, search if that dictionary exists.
        // If it not exists, create a new one.
        if (dictionary == null) {
            save(apiArgument.getDictionary(), currentGroup, contributor);
            dictionary = be.webdeb.infra.persistence.model.ArgumentDictionary.findById(apiArgument.getDictionary().getId());
        }

        dbArgument.setDictionary(dictionary);
        dbArgument.setType(TArgumentType.find.byId(apiArgument.getArgumentType().id()));

        if(newArgument){
            dbArgument.save();
        } else {
            dbArgument.update();
        }

        if (formerDictionary != null && formerDictionary.getArguments().isEmpty()) {
            formerDictionary.delete();
        }

        switch (apiArgument.getArgumentType()) {
            case SHADED:
                ArgumentShaded apiArgumentShaded = (ArgumentShaded) apiArgument;
                be.webdeb.infra.persistence.model.ArgumentShaded argumentShaded = be.webdeb.infra.persistence.model.ArgumentShaded.findById(apiArgument.getId());

                if (argumentShaded == null) {
                    argumentShaded = new be.webdeb.infra.persistence.model.ArgumentShaded();
                    argumentShaded.setArgument(dbArgument);
                    argumentShaded.setIdContribution(dbArgument.getIdContribution());
                    argumentShaded.setShade(TArgumentShadeType.find.byId(apiArgumentShaded.getArgumentShade().getType()));
                    argumentShaded.save();

                    dbArgument.setArgumentShaded(argumentShaded);
                    dbArgument.update();
                } else {
                    argumentShaded.setShade(TArgumentShadeType.find.byId(apiArgumentShaded.getArgumentShade().getType()));
                    argumentShaded.update();
                }

                break;
        }
    }

    /**
     * Update given db dictionary with given title and language
     *
     * @param title      the tile of the argument
     * @param language   the language of the argument tile
     * @param dictionary the db argument to update
     */
    private void updateArgumentDictionary(String title, String language, be.webdeb.infra.persistence.model.ArgumentDictionary dictionary) {
        dictionary.setTitle(title);
        dictionary.setLanguage(TLanguage.find.byId(language));
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
     * @param argument      the "destination" argument of the link
     * @return the updated DB argument justification link
     */
    private be.webdeb.infra.persistence.model.ArgumentJustification updateJustificationLink(ArgumentJustification apiLink,
                                                                                            be.webdeb.infra.persistence.model.ArgumentJustification link,
                                                                                            be.webdeb.infra.persistence.model.Contribution context,
                                                                                            Tag subContext,
                                                                                            Tag category,
                                                                                            be.webdeb.infra.persistence.model.Argument superArgument,
                                                                                            be.webdeb.infra.persistence.model.Argument argument) {

        link.setContext(context);
        link.setSubContext(subContext);
        link.setCategory(category);
        link.setSuperArgument(superArgument);
        link.setArgument(argument);
        link.setShade(TJustificationLinkShadeType.find.byId(apiLink.getLinkType().getType()));
        link.setOrder(apiLink.getOrder());

        return link;
    }

    /**
     * Update given db similarity link with api link, setting from and to arguments according to shade
     *
     * @param apiLink an API similarity link
     * @param link    a DB similarity link to update
     * @param from    the "from" DB argument (corresponding to apiLink.getArgumentFrom)
     * @param to      the "to" DB argument (corresponding to apiLink.getArgumentTo)
     * @return the updated DB argument similarity  link
     */
    private be.webdeb.infra.persistence.model.ArgumentSimilarity updateSimilarityLink(ArgumentSimilarity apiLink,
                                                                                      be.webdeb.infra.persistence.model.ArgumentSimilarity link,
                                                                                      be.webdeb.infra.persistence.model.Argument from,
                                                                                      be.webdeb.infra.persistence.model.Argument to) {
        link.setArgumentFrom(from);
        link.setArgumentTo(to);
        link.setShade(TSimilarityLinkShadeType.find.byId(apiLink.getLinkType().getType()));
        return link;
    }

}
