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

package be.webdeb.infra.persistence.accessor.api;

import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.infra.persistence.model.Contribution;
import be.webdeb.infra.persistence.model.Place;
import be.webdeb.infra.persistence.model.TCopyrightfreeSource;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Actor;
import be.webdeb.infra.persistence.model.Argument;
import be.webdeb.infra.persistence.model.Contributor;
import be.webdeb.infra.persistence.model.Group;

/**
 * Object mapper to convert database objects into api objects
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public interface APIObjectMapper {

  /**
   * Map a DB Contributor to an API contributor
   *
   * @param contributor a DB contributor
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.Contributor toContributor(Contributor contributor) throws FormatException;

  /**
   * Map a DB TmpContributor to an API tmpcontributor
   *
   * @param contributor a DB tmpcontributor
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.TmpContributor toTmpContributor(TmpContributor contributor) throws FormatException;

  /**
   * Map a DB ContributorPicture to an API contributor picture
   *
   * @param picture a DB contributor picture
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.picture.ContributorPicture toContributorPicture(ContributorPicture picture) throws FormatException;

  /**
   * Map a DB affiliation to an API affiliation
   *
   * @param affiliation a DB contributor's affiliation
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Affiliation toAffiliation(ContributorHasAffiliation affiliation) throws FormatException;

  /**
   * Map a DB affiliation to an API affiliation
   *
   * @param affiliation a DB Actor's affiliation
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Affiliation toAffiliation(ActorHasAffiliation affiliation) throws FormatException;

  /**
   * Map a DB affiliation to an API affiliation denoting a affiliation member
   *
   * @param affiliation a DB Actor's affiliation
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Affiliation toMember(ActorHasAffiliation affiliation) throws FormatException;

  /**
   * Map a DB Actor to an API Actor
   *
   * @param actor a DB Actor
   * @return an api Actor containing all (actorwise) data from given Actor
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Actor toActor(Actor actor) throws FormatException;

  /**
   * Map a DB Actor to an API person
   *
   * @param actor an Actor
   * @return an api Person containing all data from given Actor, null if given Actor is not a person
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Person toPerson(Actor actor) throws FormatException;

  /**
   * Map a DB Actor to an API organization
   *
   * @param actor an Actor
   * @return an api Person containing all data from given Actor, null if given Actor is not a person
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.actor.Organization toOrganization(Actor actor) throws FormatException;

  /**
   * Map a DB text to an API text
   *
   * @param text a DB text
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.text.Text toText(Text text) throws FormatException;

  /**
   * Map a DB text source to an API text source
   *
   * @param source a DB text source
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.text.TextSourceName toSourceName(TextSourceName source) throws FormatException;

  /**
   * Map a DB text free copyright source to an API text free copyright source
   *
   * @param freeSource a DB text free copyright source
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.text.TextCopyrightfreeSource toFreeSource(TCopyrightfreeSource freeSource) throws FormatException;

  /**
   * Map a DB argument to an API argument (links are not resolved to avoid cyclic never-ending calls)
   *
   * @param argument a DB argument
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.Argument toArgument(Argument argument) throws FormatException;

  /**
   * Map a DB argument to an API shaded argument
   *
   * @param argument a DB argument
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentShaded toArgumentShaded(Argument argument) throws FormatException;

  /**
   * Map a DB argument dictionary to an API argument dictionary (links are not resolved to avoid cyclic never-ending calls)
   *
   * @param dictionary a DB argument dictionary
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentDictionary toArgumentDictionary(ArgumentDictionary dictionary) throws FormatException;
  
  /**
   * Map a DB citation to an API citation
   *
   * @param citation a DB citation
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.citation.Citation toCitation(Citation citation) throws FormatException;

  /**
   * Map a DB debate to an API debate
   *
   * @param debate a DB debate
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.Debate toDebate(Debate debate) throws FormatException;

  /**
   * Map a DB debate to an API simple debate
   *
   * @param debate a DB debate
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateSimple toDebateSimple(Debate debate) throws FormatException;

  /**
   * Map a DB debate to an API tag
   *
   * @param tag a DB tag
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateTag toDebateTag(be.webdeb.infra.persistence.model.Tag tag) throws FormatException;

  /**
   * Map a DB debate to an API tag
   *
   * @param tag a DB tag
   * @param superDebate the multiple thesis debate where the tag comes from
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateTag toDebateTag(be.webdeb.infra.persistence.model.Tag tag, be.webdeb.core.api.debate.Debate superDebate) throws FormatException;

  /**
   * Map a DB debate external url to an API debate external url
   *
   * @param url a DB debate external url
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateExternalUrl toDebateExternalUrl(DebateExternalUrl url) throws FormatException;

  /**
   * Map a DB argument justification link to an API link
   *
   * @param link a DB argument link in a context
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentJustification toArgumentJustification(ArgumentJustification link) throws FormatException;

  /**
   * Map a DB argument justification to an API link. This method is used when creating many links in the same context
   * in order to avoid recreating too many unnecessary API context contributions, categories and super arguments.
   *
   * @param link a DB argument justification link in a context
   * @param context the context contribution of the link
   * @param category the category where the link is, can be null
   * @param superArgument the superArgument of link is, can be null
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentJustification toArgumentJustification(ArgumentJustification link, 
      ContextContribution context, TagCategory category, be.webdeb.core.api.argument.Argument superArgument) throws FormatException;

  /**
   * Map a DB citation justification link to an API link
   *
   * @param link a DB citation link in a context
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.citation.CitationJustification toCitationJustification(CitationJustification link) throws FormatException;

  /**
   * Map a DB citation justification to an API link. This method is used when creating many links in the same context
   * in order to avoid recreating too many unnecessary API context contributions, categories and super citations.
   *
   * @param link a DB citation justification link in a context
   * @param context the context contribution of the link
   * @param category the category where the link is, can be null
   * @param superArgument the superArgument of link is, can be null
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.citation.CitationJustification toCitationJustification(CitationJustification link, 
      ContextContribution context, TagCategory category, be.webdeb.core.api.argument.Argument superArgument) throws FormatException;

  /**
   * Map a DB citation position link to an API link
   *
   * @param link a DB citation link in a debate
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.citation.CitationPosition toCitationPosition(CitationPosition link) throws FormatException;

  /**
   * Map a DB citation position to an API link. This method is used when creating many links in the same context
   * in order to avoid recreating too many unnecessary API context contributions, categories and super citations.
   *
   * @param link a DB citation position link in a debate
   * @param debate the debate of the link
   * @param subDebate the debate category where the link is, can be null
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.citation.CitationPosition toCitationPosition(CitationPosition link,
          be.webdeb.core.api.debate.Debate debate, TagCategory subDebate) throws FormatException;

  /**
   * Map a DB argument similarity link to an API link
   *
   * @param link a DB argument similarity link between two arguments
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentSimilarity toArgumentSimilarity(ArgumentSimilarity link) throws FormatException;

  /**
   * Map a DB argument similarity link to an API link. This method is used when creating many arguments in order to
   * avoid recreating too many unnecessary API arguments.
   *
   * @param link a DB argument similarity link between two arguments
   * @param argument the API argument being one of the arguments in the link
   * @param isSource true if given argument is the source of the link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.argument.ArgumentSimilarity toArgumentSimilarity(ArgumentSimilarity link,
      be.webdeb.core.api.argument.Argument argument, boolean isSource) throws FormatException;

  /**
   * Map a DB debate similarity link to an API link
   *
   * @param link a DB debate similarity link between two debates
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateSimilarity toDebateSimilarity(DebateSimilarity link) throws FormatException;

  /**
   * Map a DB debate similarity link to an API link. This method is used when creating many debates in order to
   * avoid recreating too many unnecessary API debates.
   *
   * @param link a DB debate similarity link between two debates
   * @param debate the API debate being one of the debates in the link
   * @param isSource true if given debate is the source of the link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateSimilarity toDebateSimilarity(DebateSimilarity link,
      be.webdeb.core.api.debate.Debate debate, boolean isSource) throws FormatException;

  /**
   * Map a DB context has subdebate link to an API link
   *
   * @param link a DB context has subdebate link between context and debate
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.link.ContextHasSubDebate toContextHasSubDebate(ContextHasSubDebate link) throws FormatException;

  /**
   * Map a DB debate has tag debate from api debate multiple and api debate has tag debate link.
   *
   * @param link the debate has tag debate db link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateHasTagDebate toDebateHasTagDebate(DebateLink link) throws FormatException;

  /**
   * Map a DB debate has text from api debate has text link.
   *
   * @param link the debate has text db link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.debate.DebateHasText toDebateHasText(DebateHasText link) throws FormatException;

  /**
   * Map a DB tag to an API tag
   *
   * @param tag a DB Tag
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  Tag toTag(be.webdeb.infra.persistence.model.Tag tag) throws FormatException;

  /**
   * Map a DB tag to an API tag
   *
   * @param tag a DB Tag
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  TagCategory toTagCategory(be.webdeb.infra.persistence.model.Tag tag) throws FormatException;

  /**
   * Map a DB tag to an API tag
   *
   * @param tag a DB Tag
   * @param context the context contribution related to the category
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  TagCategory toTagCategory(be.webdeb.infra.persistence.model.Tag tag, ContextContribution context) throws FormatException;

  /**
   * Map a DB context has category to an API link
   *
   * @param link a DB context has category
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(ContextHasCategory link) throws FormatException;

  /**
   * Map a DB context has category to an API link. This method is used when creating many links in the same context
   * in order to avoid recreating too many unnecessary API context contributions.
   *
   * @param link a DB context has contribution
   * @param context the context contribution of the link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(ContextHasCategory link, ContextContribution context) throws FormatException;

  /**
   * Map a DB context has category to an API tag. This method is used when creating many links in the same context
   * in order to avoid recreating too many unnecessary API context contributions.
   *
   * @param tag a DB tag
   * @param context the context contribution of the link
   * @return the corresponding API object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.link.ContextHasCategory toContextHasCategory(be.webdeb.infra.persistence.model.Tag tag, ContextContribution context) throws FormatException;

  /**
   * Map a DB tag link to an API link
   *
   * @param link a DB Tag's link
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  TagLink toTagLink(be.webdeb.infra.persistence.model.TagLink link) throws FormatException;

  /**
   * Create an API Actor role for given Actor and contribution with given "Contribution has Actor"
   *
   * @param cha a contribution has Actor relationship
   * @param actor an API Actor
   * @param contribution an API contribution to which given Actor is related
   * @return an API Actor role
   */
  be.webdeb.core.api.actor.ActorRole toActorRole(
      ContributionHasActor cha, be.webdeb.core.api.actor.Actor actor,
      be.webdeb.core.api.contribution.Contribution contribution);

  /**
   * Dispatcher method to map any kind of contribution
   *
   * @param contribution a contribution
   * @return an API contribution in its concrete object type, or null if given contribution has an unknown type
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.Contribution toContribution(Contribution contribution) throws FormatException;

  /**
   * Dispatcher method to map any kind of context contribution
   *
   * @param contribution a context contribution
   * @return an API context contribution in its concrete object type, or null if given context contribution has an unknown type
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.ContextContribution toContextContribution(Contribution contribution) throws FormatException;

  /**
   * Dispatcher method to map any kind of external contribution
   *
   * @param externalContribution a external contribution
   * @return an API external contribution in its concrete object type, or null if given external contribution has an unknown type
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.ExternalContribution toExternalContribution(ExternalContribution externalContribution) throws FormatException;

  /**
   * Create an API group from a database group
   *
   * @param group a database group to cast
   * @return the mapped API group object
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.Group toGroup(Group group) throws FormatException;

  /**
   * Create an API group subscription from a database group
   *
   * @param chg a database joint table contributor_has_group to cast
   * @param contributor the contributor that must be bound into subscription
   * @param group the group to bind into subscription
   * @return the mapped API group subscription object between given contributor and group
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.GroupSubscription toGroupSubscription(ContributorHasGroup chg,
      be.webdeb.core.api.contributor.Contributor contributor,
      be.webdeb.core.api.contributor.Group group) throws FormatException;

  /**
   * Create a default API group subscription with given group for which no contributor is defined and the default
   * EContributorRole.VIEWER is set as role.
   *
   * @param group a group
   * @return a subscription with no contributor and the default VIEWER role
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.GroupSubscription toGroupSubscription(be.webdeb.core.api.contributor.Group group) throws FormatException;

  /**
   * Create an API place from a database place
   *
   * @param place a place
   * @return an API place
   */
  be.webdeb.core.api.contribution.place.Place toPlace(Place place);

  /**
   * Map a DB project to an API project
   *
   * @param project a DB Project
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.project.Project toProject(Project project) throws FormatException;

  /**
   * Map a DB project group to an API project group
   *
   * @param group a DB ProjectGroup
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.project.ProjectGroup toProjectGroup(ProjectGroup group) throws FormatException;

  /**
   * Map a DB project subgroup to an API project subgroup
   *
   * @param subgroup a DB ProjectSubgroup
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.project.ProjectSubgroup toProjectSubgroup(ProjectSubgroup subgroup) throws FormatException;

  /**
   * Map a DB ContirbutionToExplore to an API ContirbutionToExplore
   *
   * @param contirbutionToExplore a DB ContirbutionToExplore
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contribution.ContributionToExplore toContributionToExplore(ContributionToExplore contirbutionToExplore) throws FormatException;

  /**
   * Map a DB Advice to an API Advice
   *
   * @param advice a DB Advice
   * @return the corresponding API object
   *
   * @throws FormatException if the object retrieved from database is corrupted
   */
  be.webdeb.core.api.contributor.Advice toAdvice(Advice advice) throws FormatException;

}
