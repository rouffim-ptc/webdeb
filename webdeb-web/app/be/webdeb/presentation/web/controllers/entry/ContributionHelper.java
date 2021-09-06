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

package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.contribution.link.EPositionLinkShade;
import be.webdeb.core.api.contribution.type.PredefinedIntValue;
import be.webdeb.core.api.contribution.type.PredefinedStringValue;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.EContributorRole;
import be.webdeb.core.api.contributor.GroupColor;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.debate.DebateSimilarity;
import be.webdeb.core.api.debate.DebateTag;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.api.text.WordGender;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.ws.nlp.WikiAffiliation;
import be.webdeb.presentation.web.controllers.entry.actor.*;
import be.webdeb.presentation.web.controllers.entry.argument.*;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationForm;
import be.webdeb.presentation.web.controllers.entry.tag.*;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.actor.ActorVizHolder;
import be.webdeb.presentation.web.controllers.viz.debate.DebateVizHolder;
import be.webdeb.presentation.web.controllers.viz.text.TextVizHolder;
import be.webdeb.util.ValuesHelper;
import play.Configuration;
import play.data.validation.ValidationError;
import play.i18n.Lang;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This helper class (injected) offers a set of functions to manipulate API objects and convert them to
 * Presentation-layer ones.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ContributionHelper {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private ValuesHelper values;
  private ArgumentFactory argumentFactory;
  private ActorFactory actorFactory;
  private TextFactory textFactory;
  private DebateFactory debateFactory;
  private TagFactory tagFactory;
  private ContributorFactory contributorFactory;
  private MessagesApi i18n;

  private static int maxWarningSizeStandardForm;
  private static int maxDangerSizeStandardForm;

  private static final String FILTER_TREE = "three";

  /**
   * Injected constructor
   *
   * @param actorFactory the actor factory
   * @param textFactory the text factory
   * @param debateFactory the debate factory
   * @param argumentFactory the argument factory
   * @param tagFactory the tag factory
   * @param contributorFactory the contributor factory
   * @param configuration play configuration module (to get predefined values)
   * @param i18n the play messages API
   * @param values the values helper singleton
     */
  @Inject
  public ContributionHelper(ActorFactory actorFactory, TextFactory textFactory, DebateFactory debateFactory,
                            ArgumentFactory argumentFactory, TagFactory tagFactory, ContributorFactory contributorFactory,
                            Configuration configuration, MessagesApi i18n, ValuesHelper values) {
    this.values = values;
    this.actorFactory = actorFactory;
    this.textFactory = textFactory;
    this.debateFactory = debateFactory;
    this.argumentFactory = argumentFactory;
    this.tagFactory = tagFactory;
    this.contributorFactory = contributorFactory;
    this.i18n = i18n;
  }

  /**
   * Helper method to convert API ContributionToExplore into Presentation objects (into their concrete types
   * depending on their API type)
   *
   * @param contributions a list of API contributionsToExplore
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the list of converted contributions to explore into ContributionHolder concrete classes
   */
  public final List<ContributionHolder> fromExploreToHolders(List<ContributionToExplore> contributions, WebdebUser user, String lang) {
    return toHolders(contributions.stream().map(ContributionToExplore::getContribution).collect(Collectors.toList()), user, lang);
  }

  /**
   * Helper method to convert API Contribution objects into Presentation objects (into their concrete types
   * depending on their API type)
   *
   * @param contributions a list of API contributions
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the list of converted contributions into ContributionHolder concrete classes
   */
  public final List<ContributionHolder> toHolders(Collection<? extends Contribution> contributions, WebdebUser user, String lang) {
    return contributions.stream().map(c -> toHolder(c, user, lang)).collect(Collectors.toList());
  }

  /**
   * Helper method to convert API Contribution object into a Presentation object
   *
   * @param contribution an API contribution
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the converted contribution into a ContributionHolder concrete class
   */
  public final ContributionHolder toHolder(Contribution contribution, WebdebUser user, String lang) {
    return toHolder(contribution, user, lang, false);
  }

  /**
   * Helper method to convert API Contribution object into a Presentation object
   *
   * @param contribution an API contribution
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the converted contribution into a ContributionHolder concrete class
   */
  public final ContributionHolder toHolder(Contribution contribution, WebdebUser user, String lang, boolean light) {
    switch (contribution.getType()) {
      case ACTOR:
        return toActorHolder((Actor) contribution, user, lang, light);
      case TEXT:
        return toTextHolder((Text) contribution, user, lang, light);
      case DEBATE:
        return toDebateHolder((Debate) contribution, user, lang, light);
      case ARGUMENT:
        return toArgumentHolder((Argument) contribution, user, lang, light);
      case CITATION:
        return toCitationHolder((Citation) contribution, user, lang, light);
      case TAG:
        return toTagHolder((Tag) contribution, user, lang, light);
      default:
        logger.error("unknown or unhandled type for contribution " + contribution.getType().name());
        return null;
    }
  }

  public List<ActorHolder> toActorsHolders(List<Actor> actors, WebdebUser user, String lang, boolean light) {
    return actors.stream()
            .map(actor -> toActorHolder(actor, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<AffiliationHolder> toAffiliationsHolders(List<Affiliation> affiliations, WebdebUser user, String lang) {
    return affiliations.stream()
            .map(actor -> toAffiliationHolder(actor, user, lang))
            .collect(Collectors.toList());
  }

  public List<DebateHolder> toDebatesHolders(List<Debate> debates, WebdebUser user, String lang, boolean light) {
    return debates.stream()
            .filter(user::mayView)
            .map(debate -> toDebateHolder(debate, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<DebateHolder> toDebatesHolders(List<DebateSimilarity> debates, WebdebUser user, String lang) {
    return debates.stream()
            .filter(user::mayView)
            .map(link -> toDebateHolder(link.getDestination(), link.getId(), user, lang, true))
            .collect(Collectors.toList());
  }

  public List<DebateHolder> toTagDebatesHolders(List<DebateTag> debates, WebdebUser user, String lang, boolean light) {
    return debates.stream()
            .filter(user::mayView).map(debate -> toDebateHolder(debate, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<CitationHolder> toCitationsHolders(List<Citation> citations, WebdebUser user, String lang, boolean light) {
    return citations.stream()
            .filter(user::mayView)
            .map(citation -> toCitationHolder(citation, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<CitationHolder> toCitationHoldersFromJustification(List<CitationJustification> links, WebdebUser user, String lang, boolean light) {
    return links.stream()
            .filter(user::mayView)
            .map(link -> new CitationJustificationLinkForm(link, user, lang).getCitation())
            .collect(Collectors.toList());
  }

  public List<ArgumentHolder> toArgumentsHolders(List<Argument> arguments, WebdebUser user, String lang, boolean light) {
    return arguments.stream()
            .map(argument -> toArgumentHolder(argument, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<TextHolder> toTextsHolders(List<Text> texts, WebdebUser user, String lang, boolean light) {
    return texts.stream()
            .map(text -> toTextHolder(text, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<TagHolder> toTagsHolders(List<Tag> tags, WebdebUser user, String lang, boolean light) {
    return tags.stream()
            .map(tag -> toTagHolder(tag, user, lang, light))
            .collect(Collectors.toList());
  }

  public List<TagHolder> toTagsHolders(List<TagLink> links, WebdebUser user, String lang, boolean light, boolean forParents) {
    return links.stream()
            .map(tag -> toTagHolder(tag, user, lang, light, forParents))
            .collect(Collectors.toList());
  }

  public List<CitationJustificationLinkForm> toCitationJustificationLinkForms(List<CitationJustification> links, WebdebUser user, String lang) {
    return links.stream()
            .filter(user::mayView)
            .map(link -> new CitationJustificationLinkForm(link, user, lang))
            .collect(Collectors.toList());
  }

  public List<CitationHolder> toShadedCitationHolders(List<CitationJustification> links, WebdebUser user, String lang) {
     return links.stream()
            .filter(user::mayView)
            .map(link -> new CitationHolder(link.getCitation(), link.getLinkType().getEType(), user, lang))
            .collect(Collectors.toList());
  }

  public Map<Integer, List<CitationHolder>> toShadeCitationHoldersMap(Map<Integer, List<Citation>> citationsMap, WebdebUser user, String lang, EAlliesOpponentsType aType) {
    Map<Integer, List<CitationHolder>> results = new LinkedHashMap<>();

    citationsMap.entrySet()
            .stream()
            .filter(e -> !e.getValue().isEmpty())
            .forEach(e -> results.put(e.getKey(), toCitationsHolders(e.getValue(), user, lang, true)));

    return results;
  }


  public ActorHolder toActorHolder(Actor actor, WebdebUser user, String lang, boolean light) {
    return new ActorHolder(actor, user, lang, light);
  }

  public AffiliationHolder toAffiliationHolder(Affiliation affiliation, WebdebUser user, String lang) {
    return new AffiliationHolder(affiliation, user, lang, false);
  }

  public DebateHolder toDebateHolder(Debate debate, WebdebUser user, String lang, boolean light) {
    return new DebateHolder(debate, user, lang, light);
  }

  public DebateHolder toDebateHolder(Debate debate, Long linkId, WebdebUser user, String lang, boolean light) {
    return new DebateHolder(debate, linkId, user, lang, light);
  }

  public CitationHolder toCitationHolder(Citation citation, WebdebUser user, String lang, boolean light) {
    return new CitationHolder(citation, user, lang, light);
  }

  public ArgumentHolder toArgumentHolder(Argument argument, WebdebUser user, String lang, boolean light) {
    return new ArgumentHolder(argument, user, lang, light);
  }

  public TextHolder toTextHolder(Text text, WebdebUser user, String lang, boolean light) {
    return new TextHolder(text, user, lang, light);
  }

  public TagHolder toTagHolder(Tag tag, WebdebUser user, String lang, boolean light) {
    return new TagHolder(tag, user, lang, light);
  }

  public TagHolder toTagHolder(TagLink link, WebdebUser user, String lang, boolean light, boolean forParents) {
    return new TagHolder(link, user, lang, light, forParents);
  }

  public List<SimpleTagForm> toSimpleTagForms(List<Tag> tags, String lang) {
    return tags.stream()
            .map(tag -> toSimpleTagForm(tag, lang))
            .collect(Collectors.toList());
  }

  public SimpleTagForm toSimpleTagForm(Tag tag, String lang) {
    return new SimpleTagForm(tag, lang);
  }

  /**
   * Helper method to convert API Contribution object into a Presentation object
   *
   * @param contribution an API contribution
   * @param user a WebDeb user
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the converted contribution into a ContributionHolder concrete class
   */
  public final ContributionHolder toVizHolder(Contribution contribution, WebdebUser user, String lang) {
    switch (contribution.getType()) {
      case ACTOR:
        return new ActorVizHolder((Actor) contribution, user, lang);
      case TEXT:
        return new TextVizHolder((Text) contribution, user, lang);
      case DEBATE:
        return new DebateVizHolder((Debate) contribution, user, lang);
      case ARGUMENT:
        return new ArgumentHolder((Argument) contribution, user, lang);
      case CITATION:
        return new CitationHolder((Citation) contribution, user, lang);
      case TAG:
        return new TagHolder((Tag) contribution, user, lang);
      default:
        logger.error("unknown or unhandled type for contribution " + contribution.getType().name());
        return null;
    }
  }

  /**
   * Convert a String fullname to an ActorName
   *
   * @param actor the actor concerned
   * @param fullname the fullname to transform
   * @param lang the language used to fill in the function (profession) field
   * @return an API ActorName object containing the given fullname
   */
  public final ActorName toActorName(Actor actor, String fullname, String lang) {
    ActorName name = null;

    if(fullname != null) {
      name = actorFactory.getActorName(lang);
      logger.debug(fullname+"++");
      String [] names = fullname.split(" ");

      switch (actor.getActorType()) {
        case PERSON:
          if(names.length <= 1){
            name.setFirst(fullname);
            name.setLast(fullname);
          }else{
            name.setFirst(names[0]);
            name.setLast(fullname.substring(name.getFirst().length()));
          }
          break;
        default:
          name.setLast(fullname);
      }
    }

    return name;
  }

  /**
   * Convert an ActorSimpleForm to an ActorRole
   *
   * @param actorname the actor name to, all ids are considered correct (must be validated by checkActors)
   * @param contribution the contribution to be bound to the actor role
   * @param lang the language used to fill in the function (profession) field
   * @return an API ActorRole object containing all fields in actorname
   */
  public final ActorRole toActorRole(ActorSimpleForm actorname, Contribution contribution, String lang) {
    logger.debug("convert " + actorname.toString() + " to actor role");
    Actor actor = actorFactory.retrieve(actorname.getId());

    if(actor == null){
      actor = actorFactory.getActor();
      actor.setId(-1L);
      ActorName name = actorFactory.getActorName(lang);
      name.setLast(actorname.getFullname().trim());
      actor.setNames(Collections.singletonList(name));
    }

    // create the role
    ActorRole role = actorFactory.getActorRole(actor, contribution);
    role.setIsAuthor(actorname.getAuthor());
    role.setIsReporter(actorname.getReporter());
    role.setIsJustCited(actorname.getCited());
    // handle actor affiliation
    if (actorname.hasAffiliation()) {
      logger.debug("bind affiliation " + actorname.getFullFunction() + " with id " + actorname.getAha());
      // look for selected affiliation, if any (id has been checked in checkActors)
      Affiliation affiliation;
      if (!values.isBlank(actorname.getAha())) {
        affiliation = actorFactory.getAffiliation(actorname.getAha());
      } else {
        // create a new affiliation
        affiliation = actorFactory.getAffiliation();
        affiliation.setAffiliated(actor);
        // affiliation actor ?
        if (!values.isBlank(actorname.getAffname())) {
          Actor affActor;
          if (!values.isBlank(actorname.getAffid())) {
            affActor = actorFactory.retrieve(actorname.getAffid());
          } else {
            affActor = actorFactory.getOrganization();
            ActorName name = actorFactory.getActorName(lang);
            name.setLast(actorname.getAffname().trim());
            affActor.setNames(Collections.singletonList(name));
          }
          affiliation.setActor(affActor);
        }

        // function ?
        if (!values.isBlank(actorname.getFunction())) {
          Profession profession;
          if (!values.isBlank(actorname.getFunctionid())) {
            // id must be valid (checked in checkActors)
            try {
              profession = actorFactory.getProfession(actorname.getFunctionid());
            } catch (FormatException e) {
              logger.error("unable to retrieve profession " + actorname.getFunctionid() + " " + actorname.getFunction(), e);
              profession = actorFactory.createProfession(-1,
                  values.fillProfessionName(lang, actorname.getFunctionGender(), actorname.getFunction()));
            }
          } else {
            profession = actorFactory.createProfession(-1,
                values.fillProfessionName(lang, actorname.getFunctionGender(), actorname.getFunction()));
          }
          affiliation.setFunction(profession);
        }
      }
      role.setAffiliation(affiliation);
    }
    return role;
  }

  public final void fromSimpleFormToActor(ActorSimpleForm form, String id, int index, WebdebUser user, String lang, EActorType type) {
    if (values.isBlank(form.getId())) {
      form.setId(saveActorFromName(form.getFullname().trim(), user, lang, type).getId());
      form.setIsNew(true);
    }

    if (values.isBlank(form.getAffid()) && !form.getAffname().isEmpty()) {
      form.setAffid(saveActorFromName(form.getAffname().trim(), user, lang, EActorType.ORGANIZATION).getId());
      form.setIsAffNew(true);
    }

    form.setFormId(id);
    form.setFormIndex(index);
  }

  private Actor saveActorFromName(String fullname, WebdebUser user, String lang, EActorType type) {
    Actor actor;
    ActorName name = actorFactory.getActorName(lang);

    switch (type) {
      case PERSON:
        actor = actorFactory.getPerson();
        String[] names = fullname.split(" ");
        if(names.length > 1) {
          name.setFirst(names[0]);
          String lastname = fullname.replace(names[0] + " ", "");
          name.setLast(lastname.isEmpty() ? fullname : lastname);
        } else {
          name.setLast(names[0]);
        }
        break;
      case ORGANIZATION:
        actor = actorFactory.getOrganization();
        name.setLast(fullname);
        break;
      default:
        actor = actorFactory.getActor();
        name.setLast(fullname);
    }

    actor.setId(-1L);
    actor.setNames(Collections.singletonList(name));

    try {
      actor.save(user.getId(), user.getGroupId());
    } catch (FormatException | PermissionException | PersistenceException e) {
      logger.debug("Error while saving actor " + e);
    }

    return actor;
  }

  /**
   * Convert an SimpleTagForm to a Tag
   *
   * @param tagForm the tagForm
   * @param lang the language used to fill the tag name
   * @param type a tag type
   * @return an API Tag object named
   */
  public final Tag toTag(SimpleTagForm tagForm, String lang, ETagType type) {
    logger.debug("convert " + tagForm.toString() + " to tag");
    Tag tag = tagFactory.retrieve(tagForm.getTagId());

    // create a new one
    if (tag == null) {
      tag = tagFactory.getTag();
      tag.setId(-1L);
      tag.addName(lang, sanitizeTagName(tagForm.getName(), lang));
      try {
        tag.setTagType(tagFactory.getTagType(type.id()));
      } catch (NumberFormatException | FormatException e) {
        logger.error("unknown tag type id" + type.id(), e);
      }
    }

    return tag;
  }

  /**
   * Convert a title to a Tag
   *
   * @param title the title of the tag
   * @param lang the language used to fill the tag name
   * @param type a tag type
   * @return an API Tag object named
   */
  public final Tag toTag(String title, String lang, ETagType type) {
    logger.debug("convert " + title + " to tag");

    if(!values.isBlank(title)) {
      Tag tag = tagFactory.getTag();
      tag.setId(-1L);
      tag.addName(lang, sanitizeTagName(title, lang));
      try {
        tag.setTagType(tagFactory.getTagType(type.id()));
      } catch (NumberFormatException | FormatException e) {
        logger.error("unknown tag type id" + type.id(), e);
      }

      return tag;
    }

    return null;
  }

  /**
   * Check a given url if it is valid, return a validation error list if it is malformed
   *
   * @param url a url
   * @param field the field name in which the url is present (for proper error binding)
   * @return a (possibly empty) list of validation error if the url is malformed
   */
  public List<ValidationError> checkUrl(String url, String field) {
    List<ValidationError> errors = new ArrayList<>();
    if (!values.isURL(url)) {
      errors.add(new ValidationError(field, "entry.error.url.format"));
    }
    return errors;
  }


  /**
   * Check a list of actors, return a validation error list if their content is not valid.
   * Also, check consistency between field and hidden values regarding actor, function and
   * affiliation ids.
   *
   * @param tocheck a list of ActorSimpleForm objects
   * @param field the field name in which the actors are present (for proper error binding)
   * @return a list of validation error if any, null otherwise
   */
  public List<ValidationError> checkActors(List<ActorSimpleForm> tocheck, String field) {
    List<ValidationError> errors = new ArrayList<>();
    if (tocheck == null) {
      errors.add(new ValidationError(field + "[0].fullname", "entry.error.actor.noname"));
      return errors;
    }

    tocheck.forEach(a -> {
      // avoid filter collect (otherwise, a is another reference)
      if (!a.isEmpty()) {
        String fieldname = field + "[" + tocheck.indexOf(a) + "]";
        // do we have a name
        if (values.isBlank(a.getFullname())) {
          errors.add(new ValidationError(fieldname + ".fullname", "entry.error.actor.noname"));
        }

        // now check and update all fields (mainly checking actor/affiliation/function ids)
        a.checkFieldsIntegrity();

        // check if affiliated to self, same id (except -1L) or same name
        if (!values.isBlank(a.getAffname()) && a.getFullname().equals(a.getAffname())) {
          errors.add(new ValidationError(fieldname + ".fullname", "affiliation.error.notself"));
        }

        // check if affiliated to an person
        if (!values.isBlank(a.getAffid())) {
          Actor actor = actorFactory.retrieve(a.getAffid());
          if(actor != null && actor.getActorType().id() == EActorType.PERSON.id())
            errors.add(new ValidationError(fieldname + ".affname", "affiliation.error.notperson"));
        }

      }
    });
    return !errors.isEmpty() ? errors : null;
  }

  /**
   * Will remove (and rebind) duplicates in bound actors' affiliation actors, ie, it is possible that the same
   * unknown affiliation actor is specified more than once in actor's affiliations. Must bind to same instance,
   * otherwise, multiple actors will be created for the same unknown actor
   *
   * @param contribution a textual contribution to be checked for duplicates in actors' affiliations
   * @param lang 2-char ISO code for the UI language
   */
  public void removeDuplicateUnknownActors(TextualContribution contribution, String lang) {
    // post process actors since new affiliations may be put more than once
    for (int i = 0; i < contribution.getActors().size(); i++) {
      // only actors with one and only one affiliation with actor's id = -1 are of interest
      Affiliation current = contribution.getActors().get(i).getAffiliation();
      if (current != null && current.getActor() != null && values.isBlank(current.getActor().getId())) {

        // check all previous elements if such an affiliation does not yet exist, if so, replace current instance with other one
        // replace current affiliation actor with already created one
        Optional<ActorRole> role = contribution.getActors().subList(0, i).stream().filter(r ->
            r.getAffiliation() != null && r.getAffiliation().getActor() != null
                && values.isBlank(r.getAffiliation().getActor().getId())
                && r.getAffiliation().getActor().getFullname(lang).equalsIgnoreCase(current.getActor().getFullname(lang)))
            .findFirst();
        if (role.isPresent()) {
          // replace current affiliation actor with already created one
          logger.debug("bind unknown actor " + role.get().getAffiliation().getActor().getFullname(lang) + " to unique instance");
          current.setActor(role.get().getAffiliation().getActor());
        }
      }
    }
  }

  public String getDefaultActorAvatar(Actor actor) {
    return actor.getAvatar() != null ?
            actor.getAvatar().getPictureFilename() :
            computeAvatar(actor.getActorTypeId(), actor.getGenderAsString());
  }

  /**
   * Compute default avatar picture name
   *
   * @param type an actortype id (-1, 0 or 1), use 0 for contributor
   * @param gender a gender type id (F, M or null)
   * @return the default picture name for an actor's avatar
   */
  public String computeAvatar(int type, String gender) {
    return computeAvatar(type, gender, false);
  }

  /**
   * Compute default avatar picture name
   *
   * @param type an actortype id (-1, 0 or 1), use 0 for contributor
   * @param gender a gender type id (F, M or null)
   * @param forUser true for user, false for contribution
   * @return the default picture name for an actor or contributor's avatar
   */
  public String computeAvatar(int type, String gender, boolean forUser) {
    String avatar = "";

    switch (type) {
      case 0:
        if (gender != null) {
          switch (gender) {
            case "M":
              avatar = "man.png";
              break;
            case "F":
              avatar = "woman.png";
              break;
            default:
              avatar = "person.png";
          }
        } else {
          avatar = "person.png";
        }
        break;
      case 1:
        avatar = "org.png";
        break;
      default:
        avatar = "unknown.png";
    }

    return (forUser ? "/user" : "/") + "avatar/" + avatar;
  }


  /**
   * Compute filters from given list of contributions. Returned filters are a map of the form:
   * <ul>
   *   <li>key: the unique key of the filter name field, eg, publication.date, language, tag, etc.</li>
   *   <li>map (value, object-type)</li>
   *   <ul>
   *     <li>value is the actual value associated to this filtering element. For ranges, two values must be passed,
   *     separated by a comma (",")</li>
   *     <li>the filter object type is either "checkbox" or "range", depicting the type of filter input</li>
   *   </ul>
   * </ul>
   *
   * Note that all ContributionHolder (and forms subtypes not being constructed by explicit super() call)
   * have their filterable property set up in their constructor.
   *
   * @param contributions a list of contributions to build a filter pane for
   * @param lang the UI language code (2-char iso 639-1)
   * @return a map of filters of the form (key, List(objecttype, value)) with the key being the filter key,
   * the objecttype as a key for the html object containing the values for the filter)
   * @see EFilterName for all available filters
   * @see EFilterType for all type of filters (boxes or ranges)
   * @see FilterTree that manage the filter
   */
  public final Map<EFilterName, List<FilterTree>> buildFilters(Collection<? extends ContributionHolder> contributions, String lang) {
    Map<EFilterName, Map<String, FilterTree>> filters = new EnumMap<>(EFilterName.class);
    Map<EFilterName, List<FilterTree>> filtersReturn = new LinkedHashMap<>();

    FilterTree placeTree = new FilterTree(EFilterType.TREE, true);
    FilterTree functionTree = new FilterTree(EFilterType.TREE);

    // initialize filter list with all available filter values
    for (EFilterName e : EFilterName.all()) {
      filters.put(e, new HashMap<>());
    }

    // dates used to create the ranges for birthdate/deathhdate and publication date
    final Map<EDateType, String> birthdateMap = new LinkedHashMap<>();
    final Map<EDateType, String> pubdateMap = new LinkedHashMap<>();
    final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.FRENCH);

    birthdateMap.put(EDateType.LOW_DATE, yearFormat.format(new Date()));
    birthdateMap.put(EDateType.HIGHER_DATE, yearFormat.format(new Date()));
    pubdateMap.put(EDateType.LOW_DATE, yearFormat.format(new Date()));
    pubdateMap.put(EDateType.HIGHER_DATE, yearFormat.format(new Date()));

    contributions.forEach(contribution -> {
      putFilterElementInMap(filters.get(EFilterName.CTYPE), i18n.get(Lang.forCode(lang), "general.filter.ctype."
          + contribution.getType().id()), EFilterType.BOX);
      // object type keys
      switch (contribution.getType()) {
        case ACTOR:

          ActorHolder actor = (ActorHolder) contribution;
          putFilterElementInMap(filters.get(EFilterName.ATYPE), i18n.get(Lang.forCode(lang),
                  "actor.label.actortype." + actor.getActortype()),EFilterType.BOX );
          switch (EActorType.value(actor.getActortype())) {
            case PERSON:
              buildPersonFilters(actor, filters, birthdateMap, functionTree);
              break;
            case ORGANIZATION:
              buildOrganizationFilters(actor, filters, birthdateMap, functionTree, placeTree);
              break;
            default:
              // ignore
          }
          break;

        case DEBATE:
          buildDebateFilters((DebateHolder) contribution, filters, placeTree);
          break;

        case ARGUMENT:
          buildArgumentFilters((ArgumentHolder) contribution, filters);
          break;

        case CITATION:
          buildCitationFilters((CitationHolder) contribution, filters, birthdateMap, pubdateMap, lang, placeTree);
          break;

        case TEXT:
          buildTextFilters((TextHolder) contribution, filters, birthdateMap, pubdateMap);
          break;
        case TAG:
          TagHolder tag = (TagHolder) contribution;
          putFilterElementInMap(filters.get(EFilterName.TAG), tag.getTagName(), EFilterType.BOX);
          break;
        default:
          logger.warn("unsupported contribution type to build filters with " + contribution.getType().name());
      }
    });

    // now add birth date and publication date filters if needed
    if (!birthdateMap.get(EDateType.LOW_DATE).equals(birthdateMap.get(EDateType.HIGHER_DATE))) {
      String date = birthdateMap.get(EDateType.LOW_DATE) + "," + birthdateMap.get(EDateType.HIGHER_DATE);
      filters.get(EFilterName.BIRTHDATE).put(date, new FilterTree(EFilterType.RANGE, date));
    }
    if (!pubdateMap.get(EDateType.LOW_DATE).equals(pubdateMap.get(EDateType.HIGHER_DATE))) {
      String date = pubdateMap.get(EDateType.LOW_DATE) + "," + pubdateMap.get(EDateType.HIGHER_DATE);
      filters.get(EFilterName.PUBLIDATE).put(date, new FilterTree(EFilterType.RANGE, date));
    }

    // add filter places trees
    if(!placeTree.isEmpty()) {
      filters.get(EFilterName.PLACE).put(FILTER_TREE, placeTree);
    }

    // add filter functions trees
    if(!functionTree.isEmpty()) {
      filters.get(EFilterName.FUNCTION).put(FILTER_TREE, functionTree);
    }

    for(Map.Entry<EFilterName, Map<String, FilterTree>> entry : filters.entrySet()){
      List<FilterTree> subFilters = new ArrayList<>(entry.getValue().values());
      Collections.sort(subFilters);
      subFilters.forEach(FilterTree::sortTree);
      filtersReturn.put(entry.getKey(), subFilters);
    }
    filtersReturn.put(EFilterName.FILTERID, Collections.singletonList(new FilterTree(EFilterType.RANGE, generatedRandomItemId())));

    return filtersReturn;
  }

  /**
   * Update filters map with given person
   */
  private void buildPersonFilters(ActorHolder actor, Map<EFilterName, Map<String, FilterTree>> filters, Map<EDateType, String> birthdateMap, FilterTree functionTree) {
    if (!values.isBlank(actor.getResidence())) {
      putFilterElementInMap(filters.get(EFilterName.COUNTRY), actor.getResidence(), EFilterType.BOX);
    }

    buildActorAffiliationsFilters(actor.getAffiliations(), filters, functionTree);

    if (!values.isBlank(actor.getGender())) {
      putFilterElementInMap(filters.get(EFilterName.GENDER), actor.getGender(), EFilterType.BOX);
    }

    if (!values.isBlank(actor.getBirthdate())) {
      updateDateMap(birthdateMap, actor.getBirthdate().split("/"));
    }
  }

  /**
   * Update filters map with given organization
   */
  private void buildOrganizationFilters(ActorHolder actor, Map<EFilterName, Map<String, FilterTree>> filters, Map<EDateType, String> birthdateMap, FilterTree functionTree, FilterTree placeTree) {
    buildActorAffiliationsFilters(actor.getOrgaffiliations(), filters, functionTree);

    actor.getBusinessSectors().forEach(s -> filters.get(EFilterName.SECTOR).put(s, new FilterTree(EFilterType.BOX, s)));
    actor.getPlaces().forEach(p -> placeTree.addListToTree(p.getPlaceComposition(), true));

    if (!values.isBlank(actor.getLegalStatus())) {
      putFilterElementInMap(filters.get(EFilterName.LEGAL), actor.getLegalStatus(), EFilterType.BOX);
    }
    if (!values.isBlank(actor.getCreationDate())) {
      updateDateMap(birthdateMap, actor.getCreationDate().split("/"));
    }
  }

  /**
   * Update filters map with given actor affiliations
   */
  private void buildActorAffiliationsFilters(List<? extends AffiliationHolder> affiliations, Map<EFilterName, Map<String, FilterTree>> filters, FilterTree functionTree){

    Set<String> affTypeMap = new HashSet<>();
    Set<String> functionMap = new HashSet<>();
    Set<String> affMap = new HashSet<>();

    for(AffiliationHolder a : affiliations){
      if (!values.isBlank(a.getAfftype())) {
        putFilterElementInMap(filters.get(EFilterName.AFFTYPE), a.getAfftype(), EFilterType.BOX, !affTypeMap.contains(a.getAfftype()));
        affTypeMap.add(a.getAfftype());
      }
      if (!values.isBlank(a.getFunction())) {
        functionTree.addListToTree(a.getFunctionComposition(), !functionMap.contains(a.getFunction()));
        functionMap.add(a.getFunction());
      }
      if (!values.isBlank(a.getAffname())) {
        putFilterElementInMap(filters.get(EFilterName.AFFILIATION), a.getAffname(), EFilterType.BOX, !affMap.contains(a.getAffname()));
        affMap.add(a.getAffname());
      }
    }
  }

  /**
   * Update filters map with given debate
   */
  private void buildDebateFilters(DebateHolder debate, Map<EFilterName, Map<String, FilterTree>> filters,FilterTree placeTree) {
    if(!values.isBlank(debate.getLanguage()))
      putFilterElementInMap(filters.get(EFilterName.LANGUAGE), debate.getLanguage(), EFilterType.BOX);
    if(!values.isBlank(debate.getShadeterm()))
      putFilterElementInMap(filters.get(EFilterName.DSHADE), debate.getShadeterm(), EFilterType.BOX);

    debate.getPlaces().forEach(p -> placeTree.addListToTree(p.getPlaceComposition(), true));
    debate.getTags().forEach(e ->
            putFilterElementInMap(filters.get(EFilterName.TAG), e.getName(), EFilterType.BOX));
  }

  /**
   * Update filters map with given argument
   */
  private void buildArgumentFilters(ArgumentHolder argument, Map<EFilterName, Map<String, FilterTree>> filters) {
    putFilterElementInMap(filters.get(EFilterName.LANGUAGE), argument.getLanguage(), EFilterType.BOX);
    putFilterElementInMap(filters.get(EFilterName.ARGSHADE), argument.getShadeterm(), EFilterType.BOX);
  }

  /**
   * Update filters map with given citation
   */
  private void buildCitationFilters(CitationHolder citation, Map<EFilterName, Map<String, FilterTree>> filters, Map<EDateType, String> birthdateMap, Map<EDateType, String> pubdateMap, String lang, FilterTree placeTree ) {
    // add authors' filters and update birthdate filter based on authors dates
    updateDateMap(birthdateMap, filters, citation);

    if (citation instanceof CitationForm) {
      putFilterElementInMap(filters.get(EFilterName.LANGUAGE), getLanguageName(citation.getLang(), lang), EFilterType.BOX);
      if (citation.getTextType() != null && !"".equals(citation.getTextType())) {
        putFilterElementInMap(filters.get(EFilterName.TTYPE), getTextTypes(lang).get(citation.getTextType()), EFilterType.BOX);
      }
    } else {
      putFilterElementInMap(filters.get(EFilterName.LANGUAGE), citation.getLang(), EFilterType.BOX);
      putFilterElementInMap(filters.get(EFilterName.TTYPE), citation.getTextType(), EFilterType.BOX);
    }
    if (!values.isBlank(citation.getSource())) {
      putFilterElementInMap(filters.get(EFilterName.SOURCE), citation.getSource(), EFilterType.BOX);
    }

    if (!values.isBlank(citation.getPublicationDate())) {
      updateDateMap(pubdateMap, citation.getPublicationDate().split("/"));
    }

    citation.getPlaces().forEach(p -> placeTree.addListToTree(p.getPlaceComposition(), true));
    citation.getTags().forEach(e ->
            putFilterElementInMap(filters.get(EFilterName.TAG), e.getName(), EFilterType.BOX));
  }

  /**
   * Update filters map with given text
   */
  private void buildTextFilters(TextHolder text, Map<EFilterName, Map<String, FilterTree>> filters, Map<EDateType, String> birthdateMap, Map<EDateType, String> pubdateMap) {
    // add authors' filters and update birthdate filter based on authors dates
    updateDateMap(birthdateMap, filters, text);

    putFilterElementInMap(filters.get(EFilterName.TTYPE), text.getTextType(), EFilterType.BOX);
    if (!values.isBlank(text.getSourceTitle())) {
      putFilterElementInMap(filters.get(EFilterName.SOURCE), text.getSourceTitle(), EFilterType.BOX);
    }
    putFilterElementInMap(filters.get(EFilterName.LANGUAGE), text.getLanguage(), EFilterType.BOX);

    if (!values.isBlank(text.getPublicationDate())) {
      updateDateMap(pubdateMap, text.getPublicationDate().split("/"));
    }

    text.getTags().forEach(e ->
            putFilterElementInMap(filters.get(EFilterName.TAG), e.getName(), EFilterType.BOX));
  }

  /**
   * Update the given map date with given data
   *
   * @param dateMap the map to update
   * @param toAdd the date to put in the map
   */
  private void updateDateMap(Map<EDateType, String> dateMap, String[] toAdd){
    String formerLower = dateMap.get(EDateType.LOW_DATE);
    String lower = getEarlierOrLater( toAdd[ toAdd.length - 1], formerLower, true);
    if(!lower.equals(formerLower))
      dateMap.put(EDateType.LOW_DATE, lower);

    String formerHigher = dateMap.get(EDateType.HIGHER_DATE);
    String higher = getEarlierOrLater( toAdd[ toAdd.length - 1], formerHigher, false);
    if(!higher.equals(formerHigher))
      dateMap.put(EDateType.HIGHER_DATE, higher);
  }

  /**
   * Update the given map date with given data
   *
   * @param dateMap the map to update
   * @param filters needed to update the map date
   * @param contribution the contribution concerned
   */
  private void updateDateMap(Map<EDateType, String> dateMap, Map<EFilterName, Map<String, FilterTree>> filters, ContributionHolder contribution){
    String formerLower = dateMap.get(EDateType.LOW_DATE);
    String formerHigher = dateMap.get(EDateType.HIGHER_DATE);
    String[] authorDates = setAuthorsFilters(filters, contribution, formerLower, formerHigher);

    if(!authorDates[0].equals(formerLower))
      dateMap.put(EDateType.LOW_DATE, authorDates[0]);
    if(!authorDates[1].equals(formerHigher))
      dateMap.put(EDateType.HIGHER_DATE, authorDates[1]);
  }

  /**
   * Add an item and count to the filter map and count the number of occurences
   *
   * @param elementMap the map where add new item or add a new occurences
   * @param key the element key in the map
   * @param type the type of element
   */
  private void putFilterElementInMap(Map<String, FilterTree> elementMap, String key, EFilterType type){
    putFilterElementInMap(elementMap, key, type, true);
  }

  /**
   * Add an item and count to the filter map and count the number of occurences
   *
   * @param elementMap the map where add new item or add a new occurences
   * @param key the element key in the map
   * @param type the type of element
   * @param addToOccurs true if the element must be counted
   */
  private void putFilterElementInMap(Map<String, FilterTree> elementMap, String key, EFilterType type, boolean addToOccurs){
    if(key != null && !key.equals("")) {
      FilterTree element = elementMap.get(key);
      if (element != null && addToOccurs) {
        element.addToOccurs();
      } else {
        elementMap.put(key, new FilterTree(type, key));
      }
    }
  }

  /**
   * Get a random string as an html unique id
   *
   * @return a string
   */
  private String generatedRandomItemId(){
    String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder salt = new StringBuilder();
    Random rnd = new Random();
    while (salt.length() < 10) { // length of the random string.
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
    }
    return salt.toString();
  }

  /**
   * Helper method to check if given actors names (e.g contribution authors, reporters or source authors) must be
   * matched to existing actors in database. First identified possible match (even for an actor's name or its
   * affiliation name) will have its nameMatches list updated and returned. The remaining given actors will not be checked
   * further. Only actors with
   *
   * @param selector the selector string for name match
   * @param actors a list of actors (being part of the actor's list of a textual contribution)
   * @param lang 2-char iso-639-1 language code
   * @return a NameMatch object that contains the index of the match and
   *         an empty list if no matches exists, the list of namesake actors if any of given actors has namesakes
   */
  public NameMatch<ActorHolder> searchForNameMatches(String selector, List<ActorSimpleForm> actors, WebdebUser user, String lang) {
    for (int i = 0; i < actors.size(); i++) {
      ActorSimpleForm actor = actors.get(i);
      boolean matchActor = true;

      if(!actor.isEmpty()) {
        logger.debug("search for matches for " + actor.toString());
        List<ActorHolder> matches = searchForNameMatches(actor, user, lang);

        if(matches.isEmpty()){
          matches = searchForAffNameMatches(actor, user, lang);
          matchActor = false;
        }

        if (!matches.isEmpty()) {
          logger.debug("send modal page to choose what to do with matches for actor " + actor.getFullname()
              + " with " + actor.getNameMatches().toString());
          return new ConcreteNameMatch<>(selector, i, matches, matchActor);
        }
      }
    }

    return new ConcreteNameMatch<>();
  }

  /**
   * Helper method to check if matches can be found for given affiliations' names (full form objects as displayed
   * in ActorForm). This helper method is not used to disambiguate in author forms (aka ActorSimpleForms).
   *
   * Given AffiliationForm nameMatches list will be updated as a side effect.
   *
   * @param selector the selector string for name match
   * @param affiliations a list of affiliations
   * @param type the type of actor to search
   * @param lang 2-char iso-639-1 language code
   * @return a NameMatch object that contains the index of the match and
   *         an empty list if no match exists, the list of namesake actors if any of given affiliation actors has matches
   */
  public NameMatch<ActorHolder> findAffiliationsNameMatches(String selector, List<AffiliationForm> affiliations, EActorType type, WebdebUser user, String lang){
    for (int i = 0; i < affiliations.size(); i++) {
      AffiliationForm aff = affiliations.get(i);
      if(!aff.isEmpty()) {
        List<ActorHolder> matches = searchForNameMatches(aff, type, user, lang);
        if (!matches.isEmpty()) {
          logger.debug("send modal page to choose what to do with matches for affiliation " + aff.getAffname()
              + " with " + matches.toString());
          return new ConcreteNameMatch<>(selector, i, matches, false);
        }
      }
    }
    return new ConcreteNameMatch<>();
  }


  /**
   * Retrieve and update the list of possible matches for actor's name (actor being author/reporter/source author of
   * a contribution). If given actor has no id and is not yet disambiguated, a search on his name is performed, so the
   * id present in the form must be valid and match the DB actor corresponding to this actor form.
   *
   * This method will also check for possible matches in actor's affiliation name, if actor.affid is blank and the
   * affiliation actor is not yet disambiguated, an analogous search is performed for the affiliation actor.
   *
   * @param actor an actor simple form
   * @param lang 2-char ISO 639-1 code of UI
   * @return a (possibly empty) list of ActorHolders that have the same name as given actor's name or affiliation
   */
  public List<ActorHolder> searchForNameMatches(ActorSimpleForm actor, WebdebUser user, String lang) {
    List<ActorHolder> matchNames = simpleSearchForNameMatches(actor, user, lang);
    actor.setNameMatches(matchNames);

    return matchNames;
  }

  /**
   * Retrieve and update the list of possible matches for actor's name (actor being author/reporter/source author of
   * a contribution). If given actor has no id and is not yet disambiguated, a search on his name is performed, so the
   * id present in the form must be valid and match the DB actor corresponding to this actor form.
   *
   * This method will also check for possible matches in actor's affiliation name, if actor.affid is blank and the
   * affiliation actor is not yet disambiguated, an analogous search is performed for the affiliation actor.
   *
   * @param actor an actor simple form
   * @param lang 2-char ISO 639-1 code of UI
   * @return a (possibly empty) list of ActorHolders that have the same name as given actor's name or affiliation
   */
  public List<ActorHolder> searchForAffNameMatches(ActorSimpleForm actor, WebdebUser user, String lang) {
    List<ActorHolder> matchNames = new ArrayList<>();

    if (values.isBlank(actor.getAffid()) && !actor.getIsAffDisambiguated()) {
      matchNames = searchForNameMatches(actor.getAffname().trim(), actor.getAffid(), EActorType.UNKNOWN, user, lang);
      actor.setAffNameMatches(matchNames);
      logger.info("found affiliation matches:" + matchNames);
    }

    return matchNames;
  }

  /**
   * Retrieve and update the list of possible matches for actor person's name.
   *
   * @param actor an actor simple form
   * @param lang 2-char ISO 639-1 code of UI
   * @return a (possibly empty) list of ActorHolders that have the same name as given actor' persons name
   */
  private List<ActorHolder> simpleSearchForNameMatches(ActorSimpleForm actor, WebdebUser user, String lang) {
    if (values.isBlank(actor.getId()) && !actor.getIsDisambiguated()) {
      actor.setNameMatches(searchForNameMatches(actor.getFullname().trim(), actor.getId(), EActorType.value(actor.getActortype()), user, lang));
      logger.info("found matches :" + actor.getNameMatches());
    }
    return actor.getNameMatches();
  }

  /*
   * **************************************
   *
   * HELPER METHODS FOR HTML SELECT OPTIONS
   *
   * **************************************
   */

  /*
   * LINKS
   */

  /**
   * Get all justification link shades
   *
   * @param lang two char ISO code of interface language
   * @return a map of justification link id-value pairs for given lang
   */
  public Map<String, String> getJustificationShades(String lang) {
    return predefinedIntTypeToMap(argumentFactory.getJustificationLinkTypes(), lang);
  }

  /**
   * Get all similarity link shades
   *
   * @param lang two char ISO code of interface language
   * @return a map of similarity link id-value pairs for given lang
   */
  public Map<String, String> getSimilarityShades(String lang) {
    return predefinedIntTypeToMap(argumentFactory.getSimilarityLinkTypes(), lang);
  }

  /*
   * PICTURES
   */

  /**
   * Get all picture licence types
   *
   * @param lang two char ISO code of interface language
   * @return a map of licence types id-value pairs for given lang
   */
  public Map<String, String> getPictureLicenceType(String lang) {
    return predefinedIntTypeToMap(contributorFactory.getPictureLicenceTypes(), lang);
  }

  /**
   * Get all picture sources
   *
   * @param lang two char ISO code of interface language
   * @return a map of picture sources id-value pairs for given lang
   */
  public Map<String, String> getContributorPictureSources(String lang) {
    return predefinedIntTypeToMap(contributorFactory.getContributorPictureSources(), lang);
  }

  /*
   * ACTORS
   */

  /**
   * Get all actor affiliation types (for organizations)
   *
   * @param lang two char ISO code of interface language
   * @param actorType 0 org and person, 1 org, 2 person
   * @param subtype -1 all, 0 affiliation, 1 affiliated, 2 filiation
   * @return a map of actor affiliation types id-value pairs
   */
  public Map<String, String> getAffiliationTypes(String lang, int actorType, int subtype) {
    return predefinedIntTypeToMap(
            actorFactory.getAffiliationTypes().stream().filter(t ->
                    t.getType() > -1 && t.getType() != EAffiliationType.GRADUATING_FROM.id() &&  t.getType() != EAffiliationType.SON_OF.id() &&
                    ((t.getActorId() == 0 && t.getActorId() < 2) || t.getActorId() == actorType || (actorType == 0 && t.getActorId() > -1))
                    && (t.getSubId() == -1 || t.getSubId() == subtype)).collect(Collectors.toList())
            , lang);
  }

  /**
   * Get actor roles
   *
   * @param lang two char ISO code of interface language
   * @return a map of actor role
   */
  public Map<String, String> getActorRole(String lang) {
    return predefinedIntTypeToMap(actorFactory.getActorTypes(), lang);
  }

  /**
   * Get profession types
   *
   * @param lang two char ISO code of interface language
   * @param subType the subtype of profession concerned
   * @return a map of precision date type
   */
  public Map<String, String> getProfessionTypes(String lang, int subType) {
    return predefinedIntTypeToMap(
            actorFactory.getProfessionTypes().stream().filter(t -> t.getSubId() == subType).collect(Collectors.toList())
            , lang);
  }

  /**
   * Get date precision types
   *
   * @param lang two char ISO code of interface language
   * @param inPast true if it concerns dates in the past
   * @return a map of precision date type
   */
  public Map<String, String> getPrecisionDateTypes(String lang, boolean inPast) {
    return predefinedIntTypeToMap(
            actorFactory.getPrecisionDateTypes().stream().filter(t -> t.isInPast() == inPast).collect(Collectors.toList())
            , lang);
  }

  /**
   * Get all countries
   *
   * @param lang two char ISO code of interface language
   * @return a map of countries types id-value pairs
   */
  public Map<String, String> getCountries(String lang) {
    Map<String, String> result = actorFactory.getCountries().stream()
        .collect(Collectors.toMap(Country::getCode, e -> e.getName(lang)
    ));
    return values.sortByValue(result);
  }

  /**
   * Get country by country name
   *
   * @param query the name or the iso code of the country to get
   * @return the corresponding Country
   */
  public Country getCountryByNameOrCode(String query) {
    Country result = null;
    if(!values.isBlank(query)) {
      Optional<Country> r = actorFactory.getCountries().stream()
          .filter(c -> c.getNames().entrySet().stream().anyMatch(n -> n.getValue().equalsIgnoreCase(query))
              || c.getCode().equalsIgnoreCase(query)).findFirst();
      if (r.isPresent()) result = r.get();
    }
    return result;
  }

  /**
   * Get all business sectors (for organizations)
   *
   * @param lang two char ISO code of interface language
   * @return a map of sectors id-value pairs
   */
  public Map<String, String> getBusinessSectors(String lang) {
    return predefinedIntTypeToMap(
            actorFactory.getBusinessSectors().stream().sorted((e1, e2) -> e1.getName(lang).compareTo(e2.getName(lang))).collect(Collectors.toList())
            , lang);
  }

  /**
   * Get all legal statuses (for organizations)
   *
   * @param lang two char ISO code of interface language
   * @return a map statuses id-value pairs
   */
  public Map<String, String> getLegalStatuses(String lang) {
    return predefinedIntTypeToMap(actorFactory.getLegalStatuses(), lang);
  }

  /**
   * Get all actor genders (for persons)
   *
   * @param lang two char ISO code of interface language
   * @return a map of genders id-value pairs
   */
  public Map<String, String> getGenders(String lang) {
    return predefinedStringTypeToMap(
            actorFactory.getGenders().stream().sorted(Comparator.comparing(Gender::getCode)).collect(Collectors.toList())
            , lang);
  }

  /*
   * ARGUMENTS
   */

  /**
   * Get all types of arguments
   *
   * @param lang two char ISO code of interface language
   * @return a map of argument types id-value pairs
   */
  public Map<String, String> getArgumentTypes(String lang) {
    return predefinedIntTypeToMap(argumentFactory.getArgumentTypes(), lang);
  }

  /**
   * Get all argument shades
   *
   * @param lang two char ISO code of interface language
   * @param forText argument for text argumentation
   * @return a map of argument shades id-value pairs for given lang
   */
  public Map<String, String> getArgumentShades(String lang, boolean forText) {
    Stream<ArgumentShade> typesStream = argumentFactory.getArgumentShades()
            .stream()
            .filter(f -> forText || f.getEType() != EArgumentShade.NO_SHADE);

    List<ArgumentShade> types = forText ?
            typesStream.limit(3).collect(Collectors.toList()) : typesStream.limit(2).collect(Collectors.toList());

    return predefinedIntTypeToMap(types, lang);
  }

  /*
   * DEBATES
   */

  /**
   * Get all debate shades
   *
   * @param lang two char ISO code of interface language
   * @return a map of debate shades id-value pairs for given lang
   */
  public Map<String, String> getDebateShades(String lang) {
    return predefinedIntTypeToMap(debateFactory.getDebateShades(), lang);
  }

  /*
   * CONTRIBUTORS
   */

  /**
   * Get all group color codes
   *
   * @return a list of group color codes
   */
  public List<String> getGroupColorCodes() {
    return contributorFactory.getGroupColors().stream().map(GroupColor::getColorCode).collect(Collectors.toList());
  }

  /*
   * TEXTS
   */

  /**
   * Get all genders
   *
   * @param lang two char ISO code of interface language
   * @return a map of genders id-value pairs
   */
  public Map<String, String> getWordGenders(String lang) {
    return predefinedStringTypeToMap(textFactory.getWordGenders(), lang);
  }

  /**
   * Get all genders code
   *
   * @return a map of genders one-char code
   */
  public List<String> getWordGenders() {
    return textFactory.getWordGenders().stream().map(WordGender::getCode).collect(Collectors.toList());
  }

  /**
   * Get all languages
   *
   * @param lang two char ISO 639-1 code of interface language
   * @return a map of languages id-value pairs
   */
  public Map<String, String> getLanguages(String lang) {

    Map<String, String> temp = textFactory.getLanguages().stream().collect(Collectors.toMap(e ->
        String.valueOf(e.getCode()), e -> e.getName(lang) + " (" + e.getName("own") + ")"));

    temp = values.sortByValue(temp);
    Map<String, String> result = new LinkedHashMap<>();
    // must add first french, dutch and english, then the rest
    // id of french
    result.put("fr", temp.get("fr"));
    // id of dutch
    result.put("nl", temp.get("nl"));
    // id of english
    result.put("en", temp.get("en"));
    result.put("optgroup", "---");
    temp.forEach(result::put);
    return result;
  }

  /**
   * Get language from given (lang) code
   *
   * @param lang two char ISO-639-1 code of the language label to retrieve
   * @param inlang two char ISO-639-1 code of the language to use to retrieve given lang
   * @return a language label in given (inlang) language, null if not found
   */
  public String getLanguage(String lang, String inlang) {
    Optional<Language> tofind = textFactory.getLanguages().stream().filter(l -> lang.equals(l.getCode())).findFirst();
    return tofind.isPresent() ? tofind.get().getName(inlang) : null;
  }

  /**
   * Get all types of text
   *
   * @param lang two char ISO 639-1 code of interface language
   * @return a map of types of text id-value pairs
   */
  public Map<String, String> getTextTypes(String lang) {
    return predefinedIntTypeToMap(textFactory.getTextTypes(), lang);
  }

  /**
   * Get all type of visibility for texts
   *
   * @param lang two char ISO 639-1 code of interface language
   * @return a map of text visibility id-value pairs
   */
  public Map<String, String> getTextVisibilities(String lang) {
    return predefinedIntTypeToMap(textFactory.getTextVisibilities().stream().limit(2).collect(Collectors.toList()), lang);
  }

  /*
   * TAGS
   */

  /**
   * Check if the given tag name is correct
   *
   * @param name the name to check
   * @param lang the user lang
   * @return true if the given name is not correct
   */
  public boolean checkTagName(String name, String lang){
    if(name != null && lang != null) {
      List<WarnedWord> words = tagFactory.getWarnedWords(EWarnedWordContextType.TAG.id(), EWarnedWordType.BEGIN.id(), lang);

      if(words.isEmpty()) {
        return false;
      }

      String n = values.firstLetterLower(name);

      return words
              .stream()
              .noneMatch(word -> n.startsWith(word.getTitle()));
    }

    return true;
  }

  /**
   * Get all types of tag
   *
   * @param lang two char ISO 639-1 code of interface language
   * @return a map of types of tag id-value pairs
   */
  public Map<String, String> getTagTypes(String lang) {
    return predefinedIntTypeToMap(tagFactory.getTagTypes(), lang);
  }

  /**
   * Check a list of tag links, return a validation error list if their content is not valid.
   * Also, check tags complete hierarchy
   *
   * @param tocheck a list of TagLinkForm objects
   * @param lang the user language
   * @param linkedTag the id of the tag to link
   * @param field the field name in which the tag links are present (for proper error binding)
   * @param parent true if the parents hierarchy must analysed, false if it is the children one.
   * @return a list of validation error if any, null otherwise
   */
  public List<ValidationError> checkTagLinkFromSimpleForm(List<SimpleTagForm> tocheck, Long linkedTag, String lang, String field, boolean parent) {
    List<TagLinkForm> links = tocheck.stream().map(f ->
        (parent ? new TagLinkForm(linkedTag, f.getTagId(), lang) : new TagLinkForm(f.getTagId(), linkedTag, lang)))
        .collect(Collectors.toList());
    return checkTagLink(links, field, parent);
  }

  /**
   * Check a list of tag links, return a validation error list if their content is not valid.
   * Also, check tags complete hierarchy
   *
   * @param tocheck a list of TagLinkForm objects
   * @param field the field name in which the tag links are present (for proper error binding)
   * @param parent true if the parents hierarchy must analysed, false if it is the children one.
   * @return a list of validation error if any, null otherwise
   */
  public List<ValidationError> checkTagLink(List<TagLinkForm> tocheck, String field, boolean parent) {
    List<ValidationError> errors = new ArrayList<>();
    boolean noHierarchy = true;

    if (tocheck == null) {
      return errors;
    }

    for(int i = 0; i < tocheck.size(); i++){
      TagLinkForm link = tocheck.get(i);

      noHierarchy = false;
      String fieldname = field + "[" + tocheck.indexOf(link) + "]";

      if (parent && tagFactory.retrieve(link.getOriginId()) == null) {
        errors.add(new ValidationError(fieldname + ".tagName", "tag.hierarchy.notfound"));
      }

      if (!parent && tagFactory.retrieve(link.getDestinationId()) == null) {
        errors.add(new ValidationError(fieldname + ".tagName", "tag.hierarchy.notfound"));
      }
    }

    if(parent && noHierarchy){
      errors.add(new ValidationError(field + "[0].tagName", "tag.hierarchy.noparent"));
    }

    return !errors.isEmpty() ? errors : null;
  }

  /*
   * ALL TYPES OF CONTRIBUTIONS
   */

  /**
   * Get the amount of contribution types for given type
   *
   * @param type a contribution type
   * @param group a group id
   * @return the amount of given contribution type
   */
  public long getAmountOf(EContributionType type, int group) {
    return textFactory.getAmountOf(type, group);
  }


  /*
   * PRIVATE HELPERS
   */

  /**
   * Get the language name for given language code in given language
   *
   * @param code a 2 char iso-639-1 code for the language name to retrieve
   * @param lang a 2 char iso-639-1 code for the language in which given code must be retrieved
   * @return a language name for given code in given lang, null if not found
   */
  private String getLanguageName(String code, String lang) {
    try {
      return textFactory.getLanguage(code).getName(lang);
    } catch (FormatException e) {
      logger.error("unable to get language name for " + code + " in lang " + lang, e);
      return null;
    }
  }

  /**
   * Retrieve and update the list of possible matches for given affiliation actor's name. If the affiliation actor id in
   * given affiliation form (aka affid) is blank and not yet disambiguated, a search on full name is performed,
   * so the affid present in given affiliation form must be valid and match the DB actor corresponding to this
   * affiliation actor.
   *
   * @param affiliation an affiliation form
   * @param type the type of actor to search
   * @param lang 2-char ISO 639-1 code of UI displayed language
   * @return a (possibly empty) list of ActorHolders that have the same name as this wrapper
   */
  private List<ActorHolder> searchForNameMatches(AffiliationForm affiliation, EActorType type, WebdebUser user, String lang) {
    if (values.isBlank(affiliation.getAffid()) && !affiliation.getIsDisambiguated()) {
      // will search for any spelling in fetched value
      WikiAffiliation wikiAffiliation = affiliation.deserializeFetched();
      List<String> spellings = new ArrayList<>();
      if (wikiAffiliation != null && wikiAffiliation.getOrganization() != null) {
        // must ensure that affname is still a value in the fetched organizations, if not,
        // clear fetched values since user has changed visible name from fetched values
        // and we do not know if they still correspond
        Map<String, String> unfetched = affiliation.getDeserializedFetched(wikiAffiliation.getOrganization());
        if (unfetched.values().stream().anyMatch(s -> s.equals(affiliation.getAffname()))) {
          spellings.addAll(unfetched.values());
        } else {
          // only add affname and clear fetched
          spellings.add(affiliation.getAffname());
          affiliation.setFetched("");
        }
      } else {
        spellings.add(affiliation.getAffname());
      }

      // add all possible name matches to this affiliation
      affiliation.setNameMatches(spellings.stream().flatMap(v ->
          // search by fullname for all possible spellings
          actorFactory.findByFullname(v, type).stream()).collect(Collectors.toSet()).stream().map(a ->
          // wrap them into actorHolders
          new ActorHolder(a, user, lang)).collect(Collectors.toList()));
    }

    logger.debug("found matches: " + affiliation.getNameMatches().toString());
    return affiliation.getNameMatches();
  }

  /**
   * Retrieve and update the list of matches for given name, actor id and actor type
   *
   * @param name a name to search for matches in database
   * @param id an actor id (may be null)
   * @param type the actor type to look for
   * @param lang 2-char ISO 639-1 code of UI
   * @return a list of ActorHolders that have the same name as this wrapper, may be empty
   */
  private List<ActorHolder> searchForNameMatches(String name, Long id, EActorType type, WebdebUser user, String lang) {
    // search for all matches by full name (exact matches)
    if (!values.isBlank(name)) {
      return actorFactory.findByName(name, type)
          // remove this one from list
          .stream().filter(a -> !a.getId().equals(id) && a.getActorType() != EActorType.UNKNOWN)
          // map to actor holders
          .map(a -> new ActorHolder(a, user, lang)).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  /**
   * Compare given stringified YYYY date return either the earlier one (before = true) or the later
   *
   * @param newOne the string date to compare
   * @param oldOne the date to be compared to
   * @param earlier true if given newOne must be before, false otherwise
   * @return the newOne date if it is earlier (or later if before is false) than given oldOne
   */
  private String getEarlierOrLater(String newOne, String oldOne, boolean earlier) {
    return ((earlier && !values.isBlank(newOne) && oldOne.compareTo(newOne) > 0)
            || (!earlier && !values.isBlank(newOne) && oldOne.compareTo(newOne) < 0) ? newOne : oldOne);
  }

  /**
   * Set the author-related filter values in given filter from given list of actors
   *
   * @param filters the filters map to update
   * @param contribution a contribution holder (argument or text) to retrieve the authors from
   * @param low the lowest date for birthdate at present time
   * @param high the highest date for birthdate at present time
   * @return array of two possibly empty dates to be added checked against existing BIRTHDATE filter values
   */
  private String[] setAuthorsFilters(Map<EFilterName, Map<String, FilterTree>> filters, ContributionHolder contribution, String low, String high) {
    List<ActorSimpleForm> actors;
    String[] result = new String[] {low, high};
    switch (contribution.getType()) {
      case DEBATE:
        actors = ((DebateHolder) contribution).getAuthors();
        break;
      case CITATION:
        actors = ((CitationHolder) contribution).getAuthors();
        break;
      case TEXT:
        actors = ((TextHolder) contribution).getAuthors();
        break;
      default:
        // ignore, just return
        return result;
    }

    actors.forEach(a -> {
      // compute lowest and highest dates for RANGE filter
      String[] temp = a.getBirthOrCreation().split("/");
      result[0] = getEarlierOrLater(temp[temp.length - 1], result[0], true);
      result[1] = getEarlierOrLater(temp[temp.length - 1], result[1], false);

      // create BOX filters
      putFilterElementInMap(filters.get(EFilterName.NAME), a.getFullname(), EFilterType.BOX);
      putFilterElementInMap(filters.get(EFilterName.FUNCTION), a.getFunction(), EFilterType.BOX);
      if (!values.isBlank(a.getAffname())) {
        putFilterElementInMap(filters.get(EFilterName.AFFILIATION), a.getAffname(), EFilterType.BOX);
      }
      if (!values.isBlank(a.getResidence())) {
        putFilterElementInMap(filters.get(EFilterName.COUNTRY), a.getResidence(), EFilterType.BOX);
      }

      switch (EActorType.value(a.getActortype())) {
        case PERSON:
          putFilterElementInMap(filters.get(EFilterName.GENDER), a.getGender(), EFilterType.BOX);
          break;
        case ORGANIZATION:
          putFilterElementInMap(filters.get(EFilterName.LEGAL), a.getLegal(), EFilterType.BOX);
          a.getSectors().forEach(s -> putFilterElementInMap(filters.get(EFilterName.SECTOR), s, EFilterType.BOX));
          break;
        default:
          // ignore
      }
    });
    return result;
  }

  /**
   * Retrieve and update the list of possible matches for tag's name.
   *
   * @param tag an tag simple form
   * @param lang 2-char ISO 639-1 code of UI
   * @return a (possibly empty) list of TagHolders that have the same name as given tag's name
   */
  private List<TagHolder> simpleSearchForTagNameMatches(SimpleTagForm tag, WebdebUser user, String lang) {
    if (values.isBlank(tag.getTagId()) && !tag.getIsDisambiguated()) {
      tag.setNameMatches(searchForTagNameMatches(tag.getName(), tag.getTagId(), user, lang));
      logger.info("found matches :" + tag.getNameMatches());
    }
    return tag.getNameMatches();
  }

  /**
   * Retrieve and update the list of matches for given name and tag id
   *
   * @param name a name to search for matches in database
   * @param id a tag id (may be null)
   * @param lang 2-char ISO 639-1 code of UI
   * @return a list of TagHolders that have the same name as this wrapper, may be empty
   */
  private List<TagHolder> searchForTagNameMatches(String name, Long id, WebdebUser user, String lang) {
    // search for all matches by name (exact matches)
    if (!values.isBlank(name)) {
      return tagFactory.findByName(name)
          // remove this one from list
          .stream().filter(f -> !f.getId().equals(id))
          // map to tag holders
          .map(f -> new TagHolder(f, user, lang)).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  /**
   * Convert an ActorSimpleForm to ProfessionForm
   *
   * @param form an ActorSimpleForm
   * @param lang a 2-char iso code that represents a language
   * @return list ProfessionForm
   */
  public List<ProfessionForm> convertActorSimpleFormToProfessionForm(List<ActorSimpleForm> form, String lang) {
    return form.stream().filter(Objects::nonNull)
        .map(f -> new ProfessionForm(f.getFunctionid(), f.getFunction(), lang, f.getFunctionGender()))
        .collect(Collectors.toList());
  }


  /**
   * Convert an AffiliationForm to ProfessionForm
   *
   * @param form an AffiliationForm
   * @param lang a 2-char iso code that represents a language
   * @return list ProfessionForm
   */
  public List<ProfessionForm> convertAffiliationFormToProfessionForm(List<AffiliationForm> form, String lang){
    return form.stream().map(a -> new ProfessionForm(a.getFunctionid(), a.getFunction(), lang, a.getGender()))
        .collect(Collectors.toList());
  }

  /**
   * check if new professions are mentioned in affiliation forms and create them if they are new
   *
   * @param forms the affiliations forms
   * @return list of Integer (new profession ids), may be empty
   */
  public List<Integer> checkIfNewProfessionsMustBeCreated(List<ProfessionForm> forms){
    List<Integer> professionIds = new ArrayList<>();
    for(ProfessionForm form : forms) {
      Integer idP = checkIfNewProfessionMustBeCreated(form.getId(), form.getName(), form.getLang(), form.getGender());
      if (idP != -1) professionIds.add(idP);
    }
    return professionIds;
  }

  /**
   * check if a given functionId and given functionName are a new profession, create it if it is a new one
   *
   * @param functionId the id of the profession, -1 if it is a new one
   * @param functionName the name of a function to look for
   * @param lang the lang of the function name
   * @param gender the gender of the function name
   * @return a Integer (new profession id), may be null
   */
  public Integer checkIfNewProfessionMustBeCreated(int functionId, String functionName, String lang, String gender){
    if(functionId == -1 && functionName != null
            && !functionName.equals("") && actorFactory.findProfession(functionName, true) == null) {
      Profession p = actorFactory.createProfession(-1, values.fillProfessionName(lang, gender, functionName));
      return actorFactory.saveProfession(p);
    }
    return -1;
  }

  /**
   * check if a given profession name is valid
   *
   * @param name the string corresponding to the profession's name
   * @param lang a two char iso-639-1 code
   * @return true if the name is valid
   */
  public boolean verifyProfessionName(String name, String lang){
    logger.debug("Verify profession name "+name);
    if(name != null && !name.equals("")){
      //Check for begin warned words match())
      if(textFactory.getWarnedWords(EWarnedWordContextType.PROFESSION.id(), EWarnedWordType.BEGIN.id(), lang).stream()
          .anyMatch(w -> matchWarnedNames(w.getTitle(), true, name, false))){
        return false;
      }
      //Check for other warned words match
      if(textFactory.getWarnedWords(EWarnedWordContextType.PROFESSION.id(), EWarnedWordType.ALL.id(), lang).stream().
          anyMatch(w -> matchWarnedNames(w.getTitle(), false, name, false))){
        return false;
      }
      //Check for countries names
      if(actorFactory.getCountries().stream().anyMatch(t -> matchWarnedNames(t.getName(lang), false, name, false))){
        return false;
      }
      //Check for organization names
      /*List<Actor> organizations = actorFactory.getAllActorOrganizations();
      if(organizations.stream().anyMatch(o -> matchWarnedWords(
          o.getNames().stream().collect(Collectors.toMap(ActorName::getLang, ActorName::getLast)), false, name, null))){
        return false;
      }*/
    }
    return true;
  }

  /**
   * Sanitize the given tag name
   *
   * @param name the tagName
   * @param lang a two char iso-639-1 code
   * @return the sanitized tag name
   */
  public String sanitizeTagName(String name, String lang){
    if(!values.isBlank(name)){
      //Check for other warned words match
      /*for(WarnedWord word : argumentFactory.getWarnedWords(EWarnedWordContextType.TAG.id(), EWarnedWordType.BEGIN.id(), lang)) {
        if(name.startsWith(word.getTitle())) {
          return name.replaceFirst(word.getTitle(), "");
        }
      }*/
    }
    return name;
  }

  /**
   * check if a given name is in the warning list
   *
   * @param title the title of the warned word
   * @param beginWord true if the word is to match at the begin of the word
   * @param name word to match
   * @param strict true if all word have to corresponding
   * @return true if the name is valid
   */
  private boolean matchWarnedNames(String title, boolean beginWord, String name, boolean strict){
    String toCompare;
    boolean match = false;
    name = name.toLowerCase();

    if(!values.isBlank(title)){
      toCompare = title.toLowerCase();
      if(strict){
        match = name.equals(toCompare);
      }else{
        match = (beginWord ? name.indexOf(toCompare) == 0 : name.contains(toCompare.toLowerCase()));
      }
    }

    return match;
  }

  /**
   * Get all actor roles
   *
   * @param lang a 2-char iso code that represents a language
   * @return the list of actor roles
   */
  public Map<String, String> getAllContributorRoles(String lang){
    Map<String, String> roles = new HashMap<>();
    for(int i = 1; i <= EContributorRole.OWNER.id(); i++){
      roles.put(i+"", i18n.get(Lang.forCode(lang), "contributor.role."+i));
    }
    return roles;
  }

  /**
   * Get all actor roles until the current contributor role
   *
   * @param lang a 2-char iso code that represents a language
   * @param contributorRole the current contributor role
   * @return the list of actor roles
   */
  public Map<String, String> getAllContributorRoles(String lang, int contributorRole){
    Map<String, String> roles = new HashMap<>();
    for(int i = 1; i <= (contributorRole > EContributorRole.OWNER.id() ? EContributorRole.OWNER.id() : contributorRole); i++){
      roles.put(i+"", i18n.get(Lang.forCode(lang), "contributor.role."+i));
    }
    return roles;
  }

  /**
   * Get contributor group role : contributor, owner, admin and all
   *
   * @param lang a 2-char iso code that represents a language
   * @return the list of contributor roles
   */
  public Map<String, String> getContributorGroupRoles(String lang){
    Map<String, String> roles = new HashMap<>();
    for(int i =  EContributorRole.CONTRIBUTOR.id(); i <= EContributorRole.MYSLEF.id() && i <= EContributorRole.ADMIN.id(); i++){
      roles.put(i+"", i18n.get(Lang.forCode(lang), "admin.mail.usergroup."+i));
    }
    return roles;
  }

  /**
   * Check if a given domain name is a free source or not. Free means free of rights.
   *
   * @param domain the domain name to check
   * @return true if the given domain name is a free source
   */
  public boolean isFreeSource(String domain){
    return textFactory.sourceIsCopyrightfree(domain);
  }

  /**
   * Check if a given domain name is a free source or not. Free means free of rights.
   *
   * @param domain the domain name to check
   * @param contributor the contributor id
   * @return true if the given domain name is a free source
   */
  public boolean
  isFreeSource(String domain, Long contributor){
    return textFactory.sourceIsCopyrightfree(domain, contributor);
  }

  public String getActorSocioName(ESocioGroupKey key, Long id, String name, String lang) {
    if((id == -1 || name == null) && key != null){
      switch (key){
        case AGE :
          return i18n.get(Lang.forCode(lang), "viz.actor.socio.age." + id);
        case COUNTRY :
          return i18n.get(Lang.forCode(lang), "viz.actor.socio.country.unknown");
        case FUNCTION :
          return i18n.get(Lang.forCode(lang), "viz.actor.socio.function.unknown");
        case ORGANIZATION :
          return i18n.get(Lang.forCode(lang), "viz.actor.socio.org.unknown");
      }
    }

    return name;
  }

  public String getCompleteActorSocioName(ESocioGroupKey key, Long id, String lang) {
    if(id == -1 || key == ESocioGroupKey.AGE) {
      return getActorSocioName(key, id, null, lang);
    }

    switch (key) {
      case COUNTRY:
        try {
          return actorFactory.getCountry(countryLongIdToCode(id)).getName(lang);
        } catch (Exception e) {
          return "";
        }
      case FUNCTION:
        try {
          return actorFactory.getProfession(id.intValue()).getSimpleName(lang);
        } catch (Exception e) {
          return "";
        }
      case AUTHOR:
      case ORGANIZATION:
        try {
          return actorFactory.retrieve(id).getFullname(lang);
        } catch (Exception e) {
          return "";
        }
    }

    return "";
  }

  public String countryLongIdToCode(Long id) {
    if(values.isBlank(id)) {
      return null;
    } else {
      String idToString = String.valueOf(id);

      if(idToString.length() == 4) {
        return ((char) Integer.parseInt(idToString.substring(0, 2))) + "" + ((char) Integer.parseInt(idToString.substring(2, 4)));
      } else if(idToString.length() == 6) {
        return ((char) Integer.parseInt(idToString.substring(0, 3))) + "" + ((char) Integer.parseInt(idToString.substring(3, 6)));
      } else if(idToString.charAt(0) == '1') {
        return ((char) Integer.parseInt(idToString.substring(0, 3))) + "" + ((char) Integer.parseInt(idToString.substring(3, 5)));
      } else {
        return ((char) Integer.parseInt(idToString.substring(0, 2))) + "" + ((char) Integer.parseInt(idToString.substring(2, 5)));
      }
    }
  }

  public ValuesHelper getValuesHelper() {
    return values;
  }

  /**
   * Create map of types from a lsit of predefined int values
   *
   * @param types the list of types
   * @param lang two char ISO code of interface language
   * @return a map of types id-value pairs
   */
  private Map<String, String> predefinedIntTypeToMap(List<? extends PredefinedIntValue> types, String lang) {
    Map<String, String> result = new LinkedHashMap<>();
    types.forEach(t -> {
      if (!result.containsKey(String.valueOf(t.getType()))) {
        result.put(String.valueOf(t.getType()), t.getName(lang));
      }
    });
    return result;
  }

  /**
   * Create map of types from a list of predefined string values
   *
   * @param types the list of types
   * @param lang two char ISO code of interface language
   * @return a map of types id-value pairs
   */
  private Map<String, String> predefinedStringTypeToMap(List<? extends PredefinedStringValue> types, String lang) {
    Map<String, String> result = new LinkedHashMap<>();
    types.forEach(t -> {
      if (!result.containsKey(t.getCode())) {
        result.put(t.getCode(), t.getName(lang));
      }
    });
    return result;
  }

  /**
   * This enumeration holds values for lower and higher date in the the corresponding map used for filters.
   *
   * @author Martin Rouffiange
   */
  private enum EDateType {

    /**
     * Lower date
     */
    LOW_DATE(0),
    /**
     * Higher date
     */
    HIGHER_DATE(1);

    private int id;
    private static Map<Integer, EDateType> map = new LinkedHashMap<>();

    static {
      for (EDateType type : EDateType.values()) {
        map.put(type.id, type);
      }
    }

    /**
     * Constructor
     *
     * @param id an int representing a date type
     */
    EDateType(int id) {
      this.id = id;
    }

    /**
     * Get the enum value for a given type
     *
     * @param id an int representing an EDateType
     * @return the EDateType enum value corresponding to the given id, null otherwise.
     */
    public static EDateType value(int id) {
      return map.get(id);
    }

    /**
     * Get this type
     *
     * @return an int representation of this EDateType
     */
    public int id() {
      return id;
    }
  }
}


