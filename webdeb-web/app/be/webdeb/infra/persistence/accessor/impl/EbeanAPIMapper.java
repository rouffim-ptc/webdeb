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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.ContributionToExplore;
import be.webdeb.core.api.contributor.*;
import be.webdeb.core.api.contributor.Advice;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.citation.CitationFactory;
import be.webdeb.core.api.debate.DebateHasTagDebate;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.project.ProjectFactory;
import be.webdeb.core.api.tag.*;
import be.webdeb.core.api.text.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.accessor.api.APIObjectMapper;
import be.webdeb.infra.persistence.accessor.api.AffiliationAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Actor;
import be.webdeb.infra.persistence.model.Argument;
import be.webdeb.infra.persistence.model.ArgumentDictionary;
import be.webdeb.infra.persistence.model.ArgumentJustification;
import be.webdeb.infra.persistence.model.ArgumentShaded;
import be.webdeb.infra.persistence.model.ArgumentSimilarity;
import be.webdeb.infra.persistence.model.Contribution;
import be.webdeb.infra.persistence.model.ExternalContribution;
import be.webdeb.infra.persistence.model.Group;
import be.webdeb.infra.persistence.model.Organization;
import be.webdeb.infra.persistence.model.Person;
import be.webdeb.infra.persistence.model.Place;
import be.webdeb.infra.persistence.model.Tag;
import be.webdeb.infra.persistence.model.TagLink;
import be.webdeb.infra.persistence.model.Text;
import be.webdeb.infra.persistence.model.TextSourceName;
import be.webdeb.infra.persistence.model.TCopyrightfreeSource;
import be.webdeb.util.ValuesHelper;
import javax.inject.Inject;

import com.google.inject.Singleton;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * This class serves as a mapper from DB objects to core.api objects. Integrity of objects are not checked
 * here and methods are supposed to be used correctly, with right objects to be mapped.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanAPIMapper implements APIObjectMapper {

  @Inject
  private ContributorFactory contributorFactory;

  @Inject
  private ActorFactory actorFactory;

  @Inject
  private TextFactory textFactory;

  @Inject
  private ArgumentFactory argumentFactory;

  @Inject
  private CitationFactory citationFactory;

  @Inject
  private DebateFactory debateFactory;

  @Inject
  private TagFactory tagFactory;

  @Inject
  private ProjectFactory projectFactory;

  @Inject
  private ValuesHelper values;

  @Inject
  private AffiliationAccessor affiliationAccessor;

  private static final Logger logger = play.Logger.underlying();

  @Override
  public be.webdeb.core.api.contributor.Contributor toContributor(
      be.webdeb.infra.persistence.model.Contributor contributor) throws FormatException {
    be.webdeb.core.api.contributor.Contributor wrapped = contributorFactory.getContributor();
    wrapped.setId(contributor.getIdContributor());
    wrapped.setOpenId(contributor.getOpenId());
    wrapped.setOpenIdToken(contributor.getOpenIdToken());

    if(!contributor.isDeleted())
      wrapped.setEmail(contributor.getEmail());

    // check if this contributor is not a skeleton, ie, has been invited to join so details are not known yet
    if (contributor.getPseudo() != null) {
      if(contributor.getTmpContributor() != null)
        wrapped.setTmpContributor(toTmpContributor(contributor.getTmpContributor()));

      wrapped.setPseudo(contributor.getPseudo());
      wrapped.setFirstname(contributor.getFirstname());
      wrapped.setLastname(contributor.getLastname());
      wrapped.setBirthyear(contributor.getBirthYear());
      wrapped.setGender(contributor.getGender() != null ?
          actorFactory.getGender(contributor.getGender().getIdGender()) : null);
      wrapped.setResidence(contributor.getResidence() != null ?
          actorFactory.getCountry(contributor.getResidence().getIdCountry()) : null);


      for (ContributorHasAffiliation cha : contributor.getContributorHasAffiliations()) {
        wrapped.addAffiliation(toAffiliation(cha));
      }
    }

    // all these fields are required
    wrapped.setPassword(contributor.getPasswordHash());
    wrapped.isValidated(contributor.isValidated());
    wrapped.isBanned(contributor.isBanned());
    wrapped.isDeleted(contributor.isDeleted());
    wrapped.isPedagogic(contributor.isPedagogic());
    wrapped.isNewsletter(contributor.isNewsletter());
    wrapped.isBrowserWarned(contributor.isBrowserWarned());
    wrapped.setSubscriptionDate(contributor.getRegistrationDate());
    wrapped.setVersion(contributor.getVersion().getTime());

    if(contributor.getAvatar() != null)
      wrapped.setAvatarId(contributor.getAvatar().getIdPicture());

    if(contributor.getOpenIdType() != null)
      wrapped.setOpenIdType(EOpenIdType.value(contributor.getOpenIdType().getIdType()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contributor.TmpContributor toTmpContributor(
          be.webdeb.infra.persistence.model.TmpContributor contributor) throws FormatException {
    be.webdeb.core.api.contributor.TmpContributor wrapped = contributorFactory.getTmpContributor();

    wrapped.setId(contributor.getIdContributor());
    wrapped.setPseudo(contributor.getPseudo());
    wrapped.setPassword(contributor.getPasswordHash());
    wrapped.setProject(toProject(contributor.getProject()));
    wrapped.setProjectSubgroup(toProjectSubgroup(contributor.getProjectSubgroup()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contributor.picture.ContributorPicture toContributorPicture(ContributorPicture picture) throws FormatException {
    be.webdeb.core.api.contributor.picture.ContributorPicture wrapped = contributorFactory.getContributorPicture();

    wrapped.setId(picture.getIdPicture());
    wrapped.setUrl(picture.getUrl());
    wrapped.setAuthor(picture.getAuthor());
    wrapped.setExtension(picture.getExtension());
    wrapped.setLicence(contributorFactory.getPictureLicenceType(picture.getLicenceType().getIdType()));
    wrapped.setSource(contributorFactory.getContributorPictureSource(picture.getSource().getIdType()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.actor.Affiliation toAffiliation(ContributorHasAffiliation affiliation) throws FormatException {
    be.webdeb.core.api.actor.Affiliation wrapped = actorFactory.getAffiliation();
    wrapped.setId(affiliation.getIdCha());
    wrapped.setVersion(affiliation.getVersion().getTime());
    wrapped.setFunction(affiliation.getFunction() != null ?
        actorFactory.getProfession(affiliation.getFunction().getIdProfession()) : null);
    if (affiliation.getActor() != null) {
      wrapped.setActor(toActor(affiliation.getActor()));
    }
    wrapped.setStartDate(values.fromDBFormat(affiliation.getStartDate()));
    wrapped.setEndDate(values.fromDBFormat(affiliation.getEndDate()));
    if(affiliation.getStartDateType() != null)
      wrapped.setStartDateType(actorFactory.getPrecisionDateType(affiliation.getStartDateType().getIdType()));
    if(affiliation.getEndDateType() != null)
      wrapped.setEndDateType(actorFactory.getPrecisionDateType(affiliation.getEndDateType().getIdType()));
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.actor.Affiliation toAffiliation(ActorHasAffiliation affiliation) throws FormatException {
    return toAffiliation(affiliation, false);
  }

  @Override
  public be.webdeb.core.api.actor.Affiliation toMember(ActorHasAffiliation affiliation) throws FormatException {
    return toAffiliation(affiliation, true);
  }

  /**
   * Wrap a DB affiliation into an API affiliation. Effective actor pointed as the affiliation actor depends
   * on the reverse attribute. Affiliation is reversed if the actor referenced in the affiliation is an affiliated
   * actor (member) instead of an affiliation
   *
   * @param affiliation an affiliation to wrap
   * @param reverse saying if the actor (true) or the affiliation (false) must be pointed as the affiliation actor
   * @return the wrapped DB affiliation
   * @throws FormatException if any unexpected data was passed as function or affiliation date
   */
  private be.webdeb.core.api.actor.Affiliation toAffiliation(ActorHasAffiliation affiliation, boolean reverse) throws FormatException {
    be.webdeb.core.api.actor.Affiliation wrapped = actorFactory.getAffiliation();
    wrapped.setId(affiliation.getId());
    wrapped.setVersion(affiliation.getVersion().getTime());
    wrapped.setFunction(affiliation.getFunction() != null ?
        actorFactory.getProfession(affiliation.getFunction().getIdProfession()) : null);
    if (affiliation.getAffiliation() != null) {
      wrapped.setActor(reverse ? toActor(affiliation.getActor()) : toActor(affiliation.getAffiliation()));
      wrapped.setAffiliated(reverse ? toActor(affiliation.getAffiliation()) : toActor(affiliation.getActor()));
    }

    if (affiliation.getType() != null) {
      wrapped.setAffiliationType(affiliation.getType().getEAffiliationType());
    }
    wrapped.setStartDate(values.fromDBFormat(affiliation.getStartDate()));
    wrapped.setEndDate(values.fromDBFormat(affiliation.getEndDate()));
    if(affiliation.getStartDateType() != null)
      wrapped.setStartDateType(actorFactory.getPrecisionDateType(affiliation.getStartDateType().getIdType()));
    if(affiliation.getEndDateType() != null)
      wrapped.setEndDateType(actorFactory.getPrecisionDateType(affiliation.getEndDateType().getIdType()));
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.actor.Actor toActor(Actor actor) throws FormatException {
    EActorType type = EActorType.value(actor.getActortype().getIdActorType());
    be.webdeb.core.api.actor.Actor result;
    switch (type) {
      case PERSON:
        result = toPerson(actor);
        break;
      case ORGANIZATION:
        result = toOrganization(actor);
        break;
      default:
        result = null;
    }

    if (result == null) {
      result = actorFactory.getActor();
    }
    setContributionData(result, actor.getContribution());
    setNames(result, actor);
    result.setCrossReference(actor.getCrossref());
    result.setNbContributions(actor.getNbContributions());

    if(actor.getAvatar() != null) {
      result.setAvatarId(actor.getAvatar().getIdPicture());
    }

    return result;
  }

  @Override
  public be.webdeb.core.api.actor.Person toPerson(Actor actor) throws FormatException {
    be.webdeb.core.api.actor.Person wrapped = actorFactory.getPerson();
    Person p = actor.getPerson();
    if (p == null) {
      logger.error("unable to get person from actor");
      return null;
    }

    wrapped.setBirthdate(values.fromDBFormat(p.getBirthdate()));
    wrapped.setDeathdate(values.fromDBFormat(p.getDeathdate()));
    wrapped.setGender(p.getGender() != null ? actorFactory.getGender(p.getGender().getIdGender()) : null);
    wrapped.setResidence(p.getResidence() != null ?
        actorFactory.getCountry(p.getResidence().getIdCountry()) : null);
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.actor.Organization toOrganization(Actor actor) throws FormatException {
    be.webdeb.core.api.actor.Organization wrapped = actorFactory.getOrganization();

    Organization o = actor.getOrganization();
    if (o == null) {
      logger.debug("unable to get organization from actor " + actor.getIdContribution() + " sending partial " +
          "organization");
      return wrapped;
    }

    wrapped.setOfficialNumber(o.getOfficialNumber());
    wrapped.setCreationDate(values.fromDBFormat(o.getCreationDate()));
    wrapped.setTerminationDate(values.fromDBFormat(o.getTerminationDate()));
    wrapped.setLegalStatus(o.getLegalStatus() != null ?
        actorFactory.getLegalStatus(o.getLegalStatus().getIdStatus()) : null);

    for (TBusinessSector sector : o.getSectors()) {
      wrapped.addBusinessSector(actorFactory.getBusinessSector(sector.getIdBusinessSector()));
    }
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.Debate toDebate(Debate debate) throws FormatException {
      be.webdeb.core.api.debate.Debate wrapped = toDebateSimple(debate);

      if (wrapped == null) {
          throw new FormatException(FormatException.Key.DEBATE_ERROR);
      }

      setContributionData(wrapped, debate.getContribution());

      wrapped.setDescription(debate.getDescription());
      wrapped.isMultiple(debate.isMultiple());
      wrapped.setNbContributions(debate.getNbContributions());
      wrapped.setNbJustificationLinks(debate.getNbJustifications());
      wrapped.setNbPositionLinks(debate.getNbPositions());

      if(debate.getShade() != null) {
        wrapped.setShade(debateFactory.getDebateShade(debate.getShade().getIdShade()));
      }

      if(debate.getPicture() != null) {
        wrapped.setPictureId(debate.getPicture().getIdPicture());
      }

      return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.DebateSimple toDebateSimple(Debate debate) throws FormatException {
    be.webdeb.core.api.debate.DebateSimple wrapped = debateFactory.getDebateSimple();
    wrapped.setArgumentId(debate.getArgument().getIdContribution());
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.DebateTag toDebateTag(Tag tag) throws FormatException {
    return this.toDebateTag(tag, null);
  }

  @Override
  public DebateTag toDebateTag(Tag tag, be.webdeb.core.api.debate.Debate superDebate) throws FormatException {
    be.webdeb.core.api.debate.DebateTag wrapped = debateFactory.getDebateTag();

    setContributionData(wrapped, tag.getContribution());

    wrapped.setTagId(tag.getIdContribution());
    wrapped.setLinkId(tag.getLinkId());
    wrapped.setCurrentSuperDebate(superDebate);

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.DebateExternalUrl toDebateExternalUrl(DebateExternalUrl url) throws FormatException {
    return debateFactory.getDebateExternalUrl(url.getIdUrl(), url.getUrl(), url.getAlias());
  }

  @Override
  public be.webdeb.core.api.text.Text toText(Text text) throws FormatException {
    be.webdeb.core.api.text.Text wrapped = textFactory.getText();
    setContributionData(wrapped, text.getContribution());

    wrapped.setTitles(text.getTitles().stream().collect(Collectors.toMap(TextI18name::getLang, TextI18name::getSpelling)));
    wrapped.setLanguage(textFactory.getLanguage(text.getLanguage().getCode()));

    wrapped.setPublicationDate(!values.isBlank(text.getPublicationDate()) ? values.fromDBFormat(text.getPublicationDate()) : null);
    wrapped.setTextType(text.getTextType() != null ? textFactory.getTextType(text.getTextType().getIdType()) : null);
    wrapped.setTextSourceType(textFactory.getTextSourceType(text.getTextSourceType().getIdType()));

    wrapped.setTextVisibility(textFactory.getTextVisibility(text.getVisibility().getIdVisibility()));

    wrapped.isHidden(text.getContribution().isHidden());
    if (text.getUrl() != null) {
      wrapped.setUrl(text.getUrl());
    }

    wrapped.setSourceTitle(text.getSourceName() != null ? text.getSourceName().getName() : null);
    wrapped.setNbContributions(text.getNbContributions());
    wrapped.setLinkId(text.getLinkId());

    // filenames are a map of contributor ids and filenames, if no contributor, then use the -1L key
    wrapped.setFilenames(text.getContents().stream().collect(Collectors
        .toMap(c -> c.getContributor() != null ? c.getContributor().getIdContributor() : -1L, TextContent::getFilename)));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.text.TextSourceName toSourceName(TextSourceName source) throws FormatException {
    be.webdeb.core.api.text.TextSourceName wrapped = textFactory.getTextSourceName();
    wrapped.setSourceId(source.getIdSource());
    wrapped.setSourceName(source.getName());
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.text.TextCopyrightfreeSource toFreeSource(TCopyrightfreeSource freeSource) throws FormatException {
    be.webdeb.core.api.text.TextCopyrightfreeSource wrapped = textFactory.getTextCopyrightfreeSource();
    wrapped.setId(freeSource.getIdCopyrightfreeSource());
    wrapped.setDomainName(freeSource.getDomainName());
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.argument.Argument toArgument(Argument argument) throws FormatException {
    EArgumentType type = EArgumentType.value(argument.getType().getIdType());
    be.webdeb.core.api.argument.Argument wrapped;
    switch (type) {
      case SHADED:
        wrapped = toArgumentShaded(argument);
        break;
      default:
        wrapped = argumentFactory.getArgument();
    }

    setContributionData(wrapped, argument.getContribution());
    wrapped.setLanguage(textFactory.getLanguage(argument.getDictionary().getLanguage().getCode()));
    wrapped.setTitle(argument.getDictionary().getTitle());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentShaded toArgumentShaded(Argument argument) throws FormatException {
    be.webdeb.core.api.argument.ArgumentShaded wrapped = argumentFactory.getArgumentShaded();
    ArgumentShaded a = argument.getArgumentShaded();

    if (a == null) {
      logger.error("unable to get shaded argument from argument");
      return null;
    }

    wrapped.setArgumentShade(argumentFactory.getArgumentShade(a.getShade().getIdShade()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentDictionary toArgumentDictionary(ArgumentDictionary dictionary) throws FormatException {
    be.webdeb.core.api.argument.ArgumentDictionary wrapped = argumentFactory.getArgumentDictionary();

    setContributionData(wrapped, dictionary.getContribution());
    wrapped.setLanguage(textFactory.getLanguage(dictionary.getLanguage().getCode()));
    wrapped.setTitle(dictionary.getTitle());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.citation.Citation toCitation(Citation citation) throws FormatException {
    be.webdeb.core.api.citation.Citation wrapped = citationFactory.getCitation();
    setContributionData(wrapped, citation.getContribution());

    wrapped.setTextId(citation.getText().getIdContribution());
    wrapped.setOriginalExcerpt(citation.getOriginalExcerpt());
    wrapped.setWorkingExcerpt(citation.getWorkingExcerpt());
    wrapped.setLanguage(textFactory.getLanguage(citation.getLanguage().getCode()));
    wrapped.setLinkId(citation.getLinkId());
    wrapped.setNbContributions(citation.getNbContributions());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentJustification toArgumentJustification(ArgumentJustification link) throws FormatException {
    return toArgumentJustification(link,
            toContextContribution(link.getContext()),
            link.getCategory() != null ? toTagCategory(link.getCategory()) : null,
            link.getSuperArgument() != null ? toArgument(link.getSuperArgument()) : null);
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentJustification toArgumentJustification(ArgumentJustification link, ContextContribution context, TagCategory category, be.webdeb.core.api.argument.Argument superArgument)
          throws FormatException {
    be.webdeb.core.api.argument.ArgumentJustification wrapped = argumentFactory.getArgumentJustificationLink();
    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getContext().getIdContribution());
    wrapped.setContext(context);
    wrapped.setDestinationId(link.getArgument().getIdContribution());

    if(link.getSubContext() != null)
      wrapped.setSubContextId(link.getSubContext().getIdContribution());
    if(link.getCategory() != null)
      wrapped.setTagCategoryId(link.getCategory().getIdContribution());
    if(link.getSuperArgument() != null)
      wrapped.setSuperArgumentId(link.getSuperArgument().getIdContribution());

    wrapped.setLinkType(argumentFactory.getJustificationLinkType(link.getShade().getIdShade()));
    wrapped.setOrder(link.getOrder());
    if(link.getDebate() != null)
      wrapped.setDebateId(link.getDebate().getIdContribution());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.citation.CitationJustification toCitationJustification(CitationJustification link) throws FormatException {
    return toCitationJustification(link,
            toContextContribution(link.getContext()),
            link.getCategory() != null ? toTagCategory(link.getCategory()) : null,
            link.getSuperArgument() != null ? toArgument(link.getSuperArgument()) : null);
  }

  @Override
  public be.webdeb.core.api.citation.CitationJustification toCitationJustification(CitationJustification link, ContextContribution context, TagCategory category, be.webdeb.core.api.argument.Argument superArgument) throws FormatException {
    be.webdeb.core.api.citation.CitationJustification wrapped = citationFactory.getCitationJustificationLink();
    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getContext().getIdContribution());
    wrapped.setContext(context);
    wrapped.setDestinationId(link.getCitation().getIdContribution());

    if(link.getSubContext() != null)
      wrapped.setSubContextId(link.getSubContext().getIdContribution());
    if(link.getCategory() != null)
      wrapped.setTagCategoryId(link.getCategory().getIdContribution());
    if(link.getSuperArgument() != null)
      wrapped.setSuperArgumentId(link.getSuperArgument().getIdContribution());

    wrapped.setTagCategory(category);
    wrapped.setSuperArgument(superArgument);

    wrapped.setLinkType(argumentFactory.getJustificationLinkType(link.getShade().getIdShade()));
    wrapped.setOrder(link.getOrder());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.citation.CitationPosition toCitationPosition(CitationPosition link) throws FormatException {
    return toCitationPosition(link,
            toDebate(link.getDebate()),
            link.getSubDebate() != null ? toTagCategory(link.getSubDebate()) : null);
  }

  @Override
  public be.webdeb.core.api.citation.CitationPosition toCitationPosition(CitationPosition link, be.webdeb.core.api.debate.Debate debate, TagCategory subDebate) throws FormatException {
    be.webdeb.core.api.citation.CitationPosition wrapped = citationFactory.getCitationPositionLink();
    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getDebate().getIdContribution());
    wrapped.setDebate(debate);
    wrapped.setDestinationId(link.getCitation().getIdContribution());

    if(link.getSubDebate() != null)
      wrapped.setSubDebateId(link.getSubDebate().getIdContribution());

    wrapped.setSubDebate(subDebate);

    wrapped.setLinkType(argumentFactory.getPositionLinkType(link.getShade().getIdShade()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentSimilarity toArgumentSimilarity(ArgumentSimilarity link) throws FormatException {
    return toArgumentSimilarity(link, null, true);
  }

  @Override
  public be.webdeb.core.api.argument.ArgumentSimilarity toArgumentSimilarity(ArgumentSimilarity link, be.webdeb.core.api.argument.Argument argument, boolean isSource) throws FormatException {
    be.webdeb.core.api.argument.ArgumentSimilarity wrapped = argumentFactory.getArgumentSimilarityLink();
    setContributionData(wrapped, link.getContribution());

    if(isSource) {
      if(argument != null) {
        wrapped.setOrigin(argument);
      } else {
        wrapped.setOriginId(link.getArgumentTo().getIdContribution());
      }
      wrapped.setDestinationId(link.getArgumentFrom().getIdContribution());
    } else {
      wrapped.setOriginId(link.getArgumentFrom().getIdContribution());
      if(argument != null) {
        wrapped.setDestination(argument);
      } else {
        wrapped.setDestinationId(link.getArgumentTo().getIdContribution());
      }
    }

    wrapped.setLinkType(argumentFactory.getSimilarityLinkType(link.getShade().getIdShade()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.DebateSimilarity toDebateSimilarity(DebateSimilarity link) throws FormatException {
    return toDebateSimilarity(link, null, true);
  }

  @Override
  public be.webdeb.core.api.debate.DebateSimilarity toDebateSimilarity(DebateSimilarity link, be.webdeb.core.api.debate.Debate debate, boolean isSource) throws FormatException {
    be.webdeb.core.api.debate.DebateSimilarity wrapped = debateFactory.getDebateSimilarityLink();
    setContributionData(wrapped, link.getContribution());

    if(isSource) {
      if(debate != null) {
        wrapped.setOrigin(debate);
      } else {
        wrapped.setOriginId(link.getDebateTo().getIdContribution());
      }
      wrapped.setDestinationId(link.getDebateFrom().getIdContribution());
    } else {
      wrapped.setOriginId(link.getDebateFrom().getIdContribution());
      if(debate != null) {
        wrapped.setDestination(debate);
      } else {
        wrapped.setDestinationId(link.getDebateTo().getIdContribution());
      }
    }

    wrapped.setLinkType(argumentFactory.getSimilarityLinkType(link.getShade().getIdShade()));

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contribution.link.ContextHasSubDebate toContextHasSubDebate(ContextHasSubDebate link) throws FormatException {
    be.webdeb.core.api.contribution.link.ContextHasSubDebate wrapped = debateFactory.getContextHasSubDebate();
    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getContext().getIdContribution());
    wrapped.setDestinationId(link.getDebate().getIdContribution());
    wrapped.setArgumentJustificationLinkId(link.getArgument().getIdContribution());

    return wrapped;
  }

  @Override
  public DebateHasTagDebate toDebateHasTagDebate(DebateLink link) throws FormatException {
    be.webdeb.core.api.debate.DebateHasTagDebate wrapped = debateFactory.getDebateHasTagDebate();

    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getDebate().getIdContribution());
    wrapped.setDestinationId(link.getTag().getIdContribution());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.debate.DebateHasText toDebateHasText(DebateHasText link) throws FormatException {
    be.webdeb.core.api.debate.DebateHasText wrapped = debateFactory.getDebateHasText();

    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getDebate().getIdContribution());
    wrapped.setDestinationId(link.getText().getIdContribution());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.tag.Tag toTag(Tag tag) throws FormatException {
    if(tag.getTagtype().getEType() == ETagType.CATEGORY_TAG || tag.getTagtype().getEType() == ETagType.SUB_DEBATE_TAG) {
      return toTagCategory(tag);
    }

    be.webdeb.core.api.tag.Tag wrapped = tagFactory.getTag();

    setContributionData(wrapped, tag.getContribution());
    tag.getNames().forEach(n -> wrapped.addName(n.getLang(), n.getName()));
    tag.getRewordingNames().forEach(n -> wrapped.addRewordingName(n.getLang(), n.getName()));

    wrapped.setTagType(tagFactory.getTagType(tag.getTagtype().getIdType()));
    wrapped.setNbContributions(tag.getNbContributions());

    return wrapped;
  }

  @Override
  public TagCategory toTagCategory(Tag tag) throws FormatException {
    be.webdeb.core.api.tag.TagCategory wrapped = tagFactory.getTagCategory();

    setContributionData(wrapped, tag.getContribution());
    tag.getNames().forEach(n -> wrapped.addName(n.getLang(), n.getName()));
    tag.getRewordingNames().forEach(n -> wrapped.addRewordingName(n.getLang(), n.getName()));

    wrapped.setTagType(tagFactory.getTagType(tag.getTagtype().getIdType()));

    return wrapped;
  }

  @Override
  public TagCategory toTagCategory(Tag tag, ContextContribution context) throws FormatException {
    be.webdeb.core.api.tag.TagCategory wrapped = toTagCategory(tag);

    wrapped.setCurrentContextId(context.getId());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(ContextHasCategory link) throws FormatException {
    return toContextHasCategory(link, null);
  }

  @Override
  public be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(ContextHasCategory link, ContextContribution context) throws FormatException {
    be.webdeb.core.api.contribution.link.ContextHasCategory wrapped = tagFactory.getContextHasCategory();
    setContributionData(wrapped, link.getContribution());

    wrapped.setOriginId(link.getContext().getIdContribution());
    wrapped.setContext(context);
    wrapped.setDestinationId(link.getCategory().getIdContribution());
    wrapped.setOrder(link.getOrder());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(Tag tag, ContextContribution context) throws FormatException {
    if(context == null){
      throw new FormatException(FormatException.Key.TAG_ERROR);
    }

    be.webdeb.core.api.contribution.link.ContextHasCategory wrapped = tagFactory.getContextHasCategory();
    setContributionData(wrapped, tag.getContribution());

    wrapped.setOriginId(context.getId());
    wrapped.setContext(context);
    wrapped.setDestinationId(tag.getIdContribution());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.tag.TagLink toTagLink(TagLink link) throws FormatException {
    be.webdeb.core.api.tag.TagLink wrapped = tagFactory.getTagLink();

    setContributionData(wrapped, link.getContribution());
    wrapped.setParent(toTag(link.getTagParent()));
    wrapped.setChild(toTag(link.getTagChild()));
    wrapped.setInGroups(new ArrayList<>());
    wrapped.setNbContributions(link.getNbContributions());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.actor.ActorRole toActorRole(
      ContributionHasActor cha, be.webdeb.core.api.actor.Actor actor, be.webdeb.core.api.contribution.Contribution contribution) {
    ActorRole role = actorFactory.getActorRole(actor, contribution);
    role.setIsAuthor(cha.isAuthor());
    role.setIsReporter(cha.isReporter());
    role.setIsJustCited(cha.isAbout() || (!cha.isAuthor() && !cha.isReporter()));
    if (cha.getActorIdAha() != null) {
      role.setAffiliation(affiliationAccessor.retrieve(cha.getActorIdAha(), false));
    }
    return role;
  }

  @Override
  public be.webdeb.core.api.contribution.Contribution toContribution(Contribution contribution) throws FormatException {
    EContributionType type = EContributionType.value(contribution.getContributionType().getIdContributionType());
    switch (type) {
      case ACTOR:
        if(contribution.getActor() != null)return toActor(contribution.getActor());
        break;
      case ARGUMENT:
        if(contribution.getArgument() != null)return toArgument(contribution.getArgument());
        break;
      case CITATION:
        if(contribution.getCitation() != null)return toCitation(contribution.getCitation());
        break;
      case DEBATE:
        if(contribution.getDebate() != null)return toDebate(contribution.getDebate());
        break;
      case TEXT:
        if(contribution.getText() != null)return toText(contribution.getText());
        break;
      case ARGUMENT_JUSTIFICATION:
        if(contribution.getArgumentJustificationLink() != null)
          return toArgumentJustification(contribution.getArgumentJustificationLink());
        break;
      case CITATION_JUSTIFICATION:
        if(contribution.getCitationJustificationLink() != null)
          return toCitationJustification(contribution.getCitationJustificationLink());
        break;
      case ARGUMENT_SIMILARITY:
        if(contribution.getArgumentSimilarityLink() != null)
          return toArgumentSimilarity(contribution.getArgumentSimilarityLink());
        break;
      case DEBATE_SIMILARITY:
        if(contribution.getDebateSimilarityLink() != null)
          return toDebateSimilarity(contribution.getDebateSimilarityLink());
        break;
      case DEBATE_HAS_TAG_DEBATE:
        if(contribution.getDebateHasTagDebate() != null)
          return toDebateHasTagDebate(contribution.getDebateHasTagDebate());
        break;
      case TAG:
        if(contribution.getTag() != null)return toTag(contribution.getTag());
        break;
      case TAG_LINK:
        if(contribution.getTagLink() != null)return toTagLink(contribution.getTagLink());
        break;
      case CONTEXT_HAS_CATEGORY:
        if(contribution.getContextHasCategory() != null)return toContextHasCategory(contribution.getContextHasCategory());
        break;
      case CITATION_POSITION:
        if(contribution.getCitationPosition() != null)return toCitationPosition(contribution.getCitationPosition());
        break;
      case DEBATE_HAS_TEXT:
        if(contribution.getDebateHasText() != null)return toDebateHasText(contribution.getDebateHasText());
        break;
      default:
        logger.error("unknown contribution type for " + contribution.getIdContribution());
    }
    return null;
  }

    @Override
    public ContextContribution toContextContribution(Contribution contribution) throws FormatException {
        EContributionType type = EContributionType.value(contribution.getContributionType().getIdContributionType());
        switch (type) {
            case DEBATE:
              if(contribution.getDebate() != null)return toDebate(contribution.getDebate());
              break;
            case TEXT:
              if(contribution.getText() != null)return toText(contribution.getText());
              break;
            case TAG:
              if(contribution.getTag() != null){
                if(contribution.getTag().getTagtype().getEType() == ETagType.SIMPLE_TAG)
                  return toTag(contribution.getTag());
                else
                  return toDebateTag(contribution.getTag());
              }
              break;
            default:
              logger.error("unknown context contribution type for " + contribution.getIdContribution());
        }
        return null;
    }

    @Override
  public be.webdeb.core.api.contribution.ExternalContribution toExternalContribution(ExternalContribution externalContribution) throws FormatException{
      be.webdeb.core.api.contribution.ExternalContribution wrapped = textFactory.getExternalContribution();
      setContributionData(wrapped, externalContribution.getContribution());

      wrapped.setSourceUrl(externalContribution.getSourceUrl());
      wrapped.setSourceId(externalContribution.getExternalSource().getIdSource());
      wrapped.setLanguage(textFactory.getLanguage(externalContribution.getLanguage().getCode()));
      wrapped.setInternalContribution(externalContribution.getInternalContribution() != null ? externalContribution.getInternalContribution().getIdContribution() : null);

      wrapped.setTitle(externalContribution.getTitle());

      return wrapped;
  }

  @Override
  public be.webdeb.core.api.contributor.Group toGroup(Group group) throws FormatException {
    be.webdeb.core.api.contributor.Group wrapped = contributorFactory.getGroup();
    wrapped.setGroupId(group.getIdGroup());
    wrapped.setGroupName(group.getGroupName());
    wrapped.setDescription(group.getGroupDescription());
    wrapped.isOpen(group.isOpen());
    wrapped.isPedagogic(group.isPedagogic());
    wrapped.setGroupColor(group.getGroupColor());
    wrapped.setVersion(group.getVersion().getTime());
    wrapped.setContributionVisibility(EContributionVisibility.value(group.getContributionVisibility().getId()));
    wrapped.setMemberVisibility(EMemberVisibility.value(group.getMemberVisibility().getId()));
    wrapped.setPermissions(group.getPermissions().stream().map(p ->
        EPermission.value(p.getIdPermission())).collect(Collectors.toList()));
    return wrapped;
  }

  @Override
  public GroupSubscription toGroupSubscription(ContributorHasGroup chg,
      be.webdeb.core.api.contributor.Contributor contributor,
      be.webdeb.core.api.contributor.Group group) throws FormatException {

    be.webdeb.core.api.contributor.GroupSubscription wrapped = contributorFactory.getGroupSubscription();
    wrapped.setContributor(contributor);
    wrapped.setGroup(group);
    wrapped.setJoinDate(new Date(chg.getVersion().getTime()));
    wrapped.isBanned(chg.isBanned());
    wrapped.isFollowed(chg.isFollowed());
    wrapped.setRole(EContributorRole.value(chg.getRole().getIdRole()));
    wrapped.isDefault(chg.getContributor().getDefaultGroup().getIdGroup() == chg.getGroup().getIdGroup());
    wrapped.setInvitation(chg.getInvitation());
    return wrapped;
  }

  @Override
  public GroupSubscription toGroupSubscription(be.webdeb.core.api.contributor.Group group) throws FormatException {
    be.webdeb.core.api.contributor.GroupSubscription wrapped = contributorFactory.getGroupSubscription();
    wrapped.setGroup(group);
    wrapped.setRole(EContributorRole.VIEWER);
    return wrapped;
  }

  @Override
  public be.webdeb.core.api.contribution.place.Place toPlace(Place place){
    if(place != null) {
      be.webdeb.core.api.contribution.place.Place wrapped = textFactory.createPlace(
          place.getId(),
          place.getGeonameId(),
          place.getCode(),
          place.getLatitude(),
          place.getLongitude(),
          place.mapSpellings()
      );
      wrapped.setPlaceType(textFactory.findPlaceTypeByCode(place.getPlaceType().getIdType()));
      wrapped.setSubregion(toPlace(place.getSubregion()));
      wrapped.setRegion(toPlace(place.getRegion()));
      wrapped.setCountry(toPlace(place.getCountry()));
      wrapped.setContinent(toPlace(place.getContinent()));
      return wrapped;
    }
    return null;
  }

  /**
   * Set the names from given db actor into given api actor
   *
   * @param api an api actor to update
   * @param db a db actor
   */
  private void setNames(be.webdeb.core.api.actor.Actor api, Actor db) {
    api.setNames(db.getNames().stream().filter(n -> !n.isOld()).map(this::toActorName).collect(Collectors.toList()));
    if (EActorType.ORGANIZATION.equals(api.getActorType())) {
      ((be.webdeb.core.api.actor.Organization) api).setOldNames(db.getNames().stream().filter(ActorI18name::isOld)
          .map(this::toActorName).collect(Collectors.toList()));
    }
  }

  /**
   * Map a DB actor name to an API one
   *
   * @param name a DB actor name
   * @return the corresponding API name
   */
  private ActorName toActorName(ActorI18name name) {
    ActorName result = actorFactory.getActorName(name.getLang());
    result.setFirst(name.getFirstOrAcro());
    result.setLast(name.getName());
    result.setPseudo(name.getPseudo());
    return result;
  }

  /**
   * Set groups from given DB contribution into given API contribution
   *
   * @param api an api contribution to update
   * @param db a db contribution
   */
  private void setGroups(be.webdeb.core.api.contribution.Contribution api, Contribution db) {
    api.setInGroups(db.getGroups().stream().map(g -> {
      try {
        return toGroup(g);
      } catch (FormatException e) {
        logger.warn("unable to map group " + g.getIdGroup(), e);
        return null;
      }
    }).collect(Collectors.toList()));
  }

  @Override
  public be.webdeb.core.api.project.Project toProject(Project project) throws FormatException {
    be.webdeb.core.api.project.Project wrapped = projectFactory.getProject();

    wrapped.setId(project.getIdProject());
    wrapped.setName(project.getName());
    wrapped.setTechnicalName(project.getTechnicalName());

    wrapped.setBeginDate(project.getBeginDate());
    wrapped.setEndDate(project.getEndDate());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.project.ProjectGroup toProjectGroup(ProjectGroup group) throws FormatException {
    be.webdeb.core.api.project.ProjectGroup wrapped = projectFactory.getProjectGroup();

    wrapped.setId(group.getIdProjectGroup());
    wrapped.setName(group.getName());
    wrapped.setTechnicalName(group.getTechnicalName());
    wrapped.setProjectId(group.getIdProjectGroup());

    return wrapped;
  }

  @Override
  public be.webdeb.core.api.project.ProjectSubgroup toProjectSubgroup(ProjectSubgroup subgroup) throws FormatException {
    be.webdeb.core.api.project.ProjectSubgroup wrapped = projectFactory.getProjectSubgroup();

    wrapped.setId(subgroup.getIdProjectSubgroup());
    wrapped.setName(subgroup.getName());
    wrapped.setTechnicalName(subgroup.getTechnicalName());
    wrapped.setNbContributors(subgroup.getNbContributors());

    wrapped.setProjectGroupId(subgroup.getProjectGroup().getIdProjectGroup());
    wrapped.setProjectGroupTechnicalName(subgroup.getProjectGroup().getTechnicalName());

    return wrapped;
  }

  @Override
  public ContributionToExplore toContributionToExplore(be.webdeb.infra.persistence.model.ContributionToExplore contirbutionToExplore) throws FormatException {
    ContributionToExplore wrapped = contributorFactory.getContributionToExplore();
    wrapped.setContributionToExploreId(contirbutionToExplore.getIdContributionToExplore());
    wrapped.setContributionId(contirbutionToExplore.getContribution().getIdContribution());
    wrapped.setContribution(toContribution(contirbutionToExplore.getContribution()));
    wrapped.setGroup(toGroup(contirbutionToExplore.getGroup()));
    wrapped.setOrder(contirbutionToExplore.getOrder());

    return wrapped;
  }

  @Override
  public Advice toAdvice(be.webdeb.infra.persistence.model.Advice advice) throws FormatException {
    Advice wrapped = contributorFactory.getAdvice();

    wrapped.setId(advice.getIdAdvice());
    for(AdviceI18name name : advice.getTitles()){
      wrapped.addName(name.getTitle(), name.getLang());
    }

    return wrapped;
  }

  /**
   * Map the contribution date for a given contribution API and contribution DB
   *
   * @param c the API contribution
   * @param contribution the DB contribution
   */
  private void setContributionData(be.webdeb.core.api.contribution.Contribution c, Contribution contribution) throws FormatException{
    c.setId(contribution.getIdContribution());
    try {
      c.setVersion(contribution.getVersion().getTime());
    } catch (Exception e){
      throw new FormatException(FormatException.Key.ACTOR_ERROR);
    }
    c.setValidated(textFactory.getValidationState(contribution.getValidated().getIdValidationState()));
    c.setLocked(contribution.isLocked());
    c.setDeleted(contribution.isDeleted());
    c.setSortkey(contribution.getSortkey());
    setGroups(c, contribution);
  }
}
