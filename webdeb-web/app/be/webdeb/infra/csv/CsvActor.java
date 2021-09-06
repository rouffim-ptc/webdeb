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
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.util.ValuesHelper;
import com.opencsv.bean.CsvBindByName;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * This class serves as a mapper from a line in actor csv file into a plain java bean.
 * Fields are using only "object" types to allow empty value to be read checked for null values
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CsvActor {

  @Inject
  private ActorFactory factory = play.api.Play.current().injector().instanceOf(ActorFactory.class);

  @Inject
  private TagFactory tagFactory = play.api.Play.current().injector().instanceOf(TagFactory.class);

  @Inject
  private ContributorFactory contributorFactory = play.api.Play.current().injector().instanceOf(ContributorFactory.class);

  @Inject
  private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

  @CsvBindByName
  Integer order;
  @CsvBindByName
  Long webdebId;
  @CsvBindByName
  String type;
  @CsvBindByName
  String url;
  @CsvBindByName
  String avatar;
  @CsvBindByName
  Integer number;
  @CsvBindByName
  String firstFr;
  @CsvBindByName
  String lastFr;
  @CsvBindByName
  String pseudoFr;
  @CsvBindByName
  String firstNl;
  @CsvBindByName
  String lastNl;
  @CsvBindByName
  String pseudoNl;
  @CsvBindByName
  String firstEn;
  @CsvBindByName
  String lastEn;
  @CsvBindByName
  String pseudoEn;
  @CsvBindByName
  String firstDe;
  @CsvBindByName
  String lastDe;
  @CsvBindByName
  String pseudoDe;
  @CsvBindByName
  String firstFrOther;
  @CsvBindByName
  String lastFrOther;
  @CsvBindByName
  String pseudoFrOther;
  @CsvBindByName
  String firstNlOther;
  @CsvBindByName
  String lastNlOther;
  @CsvBindByName
  String pseudoNlOther;
  @CsvBindByName
  String firstEnOther;
  @CsvBindByName
  String lastEnOther;
  @CsvBindByName
  String pseudoEnOther;
  @CsvBindByName
  String firstDeOther;
  @CsvBindByName
  String lastDeOther;
  @CsvBindByName
  String pseudoDeOther;
  @CsvBindByName
  String startDate;
  @CsvBindByName
  String endDate;
  @CsvBindByName
  int legalStatus;
  @CsvBindByName
  int agriculture;
  @CsvBindByName
  int art;
  @CsvBindByName
  int sport;
  @CsvBindByName
  int retail;
  @CsvBindByName
  int building;
  @CsvBindByName
  int education;
  @CsvBindByName
  int energy;
  @CsvBindByName
  int environment;
  @CsvBindByName
  int finance;
  @CsvBindByName
  int tourism;
  @CsvBindByName
  int industry;
  @CsvBindByName
  int mining;
  @CsvBindByName
  int justice;
  @CsvBindByName
  int press;
  @CsvBindByName
  int research;
  @CsvBindByName
  int health;
  @CsvBindByName
  int security;
  @CsvBindByName
  int information;
  @CsvBindByName
  int transport;
  @CsvBindByName
  int other;
  @CsvBindByName
  int all;
  @CsvBindByName
  String gender;
  @CsvBindByName
  String country;
  @CsvBindByName
  Long socialObject1;
  @CsvBindByName
  Long socialObject2;
  @CsvBindByName
  Long socialObject3;

  // used to know if an picture must be retrieved and stored for this actor after it has been saved
  boolean mustRetrieveAvatar = false;

  /**
   * Maps this csv bean into an API Actor
   *
   * @return the mapped API actor
   * @throws FormatException if any field has an invalid format
   * @throws ObjectNotFoundException if, despite the exists flag, given actor could not be retrieved in DB
   */
  Actor toActor() throws FormatException, ObjectNotFoundException {
    if (webdebId == null && order == null) {
      throw new FormatException(FormatException.Key.ACTOR_ERROR, "either an order or a webdebId must be passed");
    }
    Actor actor;
    EActorType actorType = "org".equalsIgnoreCase(type) ? EActorType.ORGANIZATION : EActorType.PERSON;
    if (webdebId != null && webdebId != -1L) {
      // retrieve actor
      actor = factory.retrieve(webdebId);
      if (actor == null) {
        throw new ObjectNotFoundException(Actor.class, webdebId);
      }
      // check if actor types matches or are compatible
      if (!(EActorType.UNKNOWN.equals(actor.getActorType()) || actorType.equals(actor.getActorType()))) {
        throw new FormatException(FormatException.Key.ACTOR_ERROR, "wrong type in CSV for actor " + actor);
      }

    } else {
      // try to find a matching one -> if such, raise ambiguous exception
      List<Actor> matches = makeNamesList().stream()
          .map(n -> factory.findByFullname(n, actorType)).flatMap(List::stream)
          .collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
              new TreeSet<>(Comparator.comparingLong(Actor::getId))), ArrayList::new));

      if (!matches.isEmpty()) {
        throw new FormatException(FormatException.Key.AMBIGUOUS_ACTOR_NAME, matches.toString());
      }
      // instantiate object
      actor = actorType.equals(EActorType.ORGANIZATION) ? factory.getOrganization() : factory.getPerson();
      actor.setNames(new ArrayList<>());
    }

    // if we got an actor, but his type is unknown, must be upcasted before being updated
    if (EActorType.UNKNOWN.equals(actor.getActorType())) {
      actor = upcast(actor, actorType);
    }

    if (EActorType.PERSON.equals(actorType)) {
      return updatePerson((Person) actor);
    }
    return updateOrganization((Organization) actor);
  }


  @Override
  public String toString() {
    return "CsvActor{" +
        "order=" + order +
        ", webdebId=" + webdebId +
        ", type='" + type + '\'' +
        ", url='" + url + '\'' +
        ", avatar='" + avatar + '\'' +
        ", number=" + number +
        ", firstFr='" + firstFr + '\'' +
        ", lastFr='" + lastFr + '\'' +
        ", pseudoFr='" + pseudoFr + '\'' +
        ", firstNl='" + firstNl + '\'' +
        ", lastNl='" + lastNl + '\'' +
        ", pseudoNl='" + pseudoNl + '\'' +
        ", firstEn='" + firstEn + '\'' +
        ", lastEn='" + lastEn + '\'' +
        ", pseudoEn='" + pseudoEn + '\'' +
        ", firstDe='" + firstDe + '\'' +
        ", lastDe='" + lastDe + '\'' +
        ", pseudoDe='" + pseudoDe + '\'' +
        ", firstFrOther='" + firstFrOther + '\'' +
        ", lastFrOther='" + lastFrOther + '\'' +
        ", pseudoFrOther='" + pseudoFrOther + '\'' +
        ", firstNlOther='" + firstNlOther + '\'' +
        ", lastNlOther='" + lastNlOther + '\'' +
        ", pseudoNlOther='" + pseudoNlOther + '\'' +
        ", firstEnOther='" + firstEnOther + '\'' +
        ", lastEnOther='" + lastEnOther + '\'' +
        ", pseudoEnOther='" + pseudoEnOther + '\'' +
        ", firstDeOther='" + firstDeOther + '\'' +
        ", lastDeOther='" + lastDeOther + '\'' +
        ", pseudoDeOther='" + pseudoDeOther + '\'' +
        ", startDate='" + startDate + '\'' +
        ", endDate='" + endDate + '\'' +
        ", legalStatus=" + legalStatus +
        ", agriculture=" + agriculture +
        ", art=" + art +
        ", sport=" + sport +
        ", retail=" + retail +
        ", building=" + building +
        ", education=" + education +
        ", energy=" + energy +
        ", environment=" + environment +
        ", finance=" + finance +
        ", tourism=" + tourism +
        ", industry=" + industry +
        ", mining=" + mining +
        ", justice=" + justice +
        ", press=" + press +
        ", research=" + research +
        ", health=" + health +
        ", security=" + security +
        ", information=" + information +
        ", transport=" + transport +
        ", other=" + other +
        ", all=" + all +
        ", gender='" + gender + '\'' +
        ", country='" + country + '\'' +
        ", socialObject1='" + socialObject1 + '\'' +
        ", socialObject=2'" + socialObject2 + '\'' +
        ", socialObject=3'" + socialObject3 + '\'' +
        '}';
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Upcast given actor to a object of given type, ie an Organization or Person.
   *
   * @param actor an (unknown type) actor
   * @param type the actor type to transform given actor into
   * @return the upcasted actor in given type (containing all fields of given actor)
   */
  private Actor upcast(Actor actor, EActorType type) throws FormatException {
    Actor upcasted = type == EActorType.ORGANIZATION ? factory.getOrganization() : factory.getPerson();
    upcasted.setId(actor.getId());
    upcasted.setVersion(actor.getVersion());
    upcasted.setNames(actor.getNames());
    upcasted.setInGroups(actor.getInGroups());
    upcasted.setAffiliations(actor.getAffiliations());
    upcasted.setCrossReference(actor.getCrossReference());
    upcasted.setValidated(factory.getValidationState(actor.getValidated().getType()));
    return upcasted;
  }

  /**
   * Map all fields of this csv bean into given person
   *
   * @param person a person to update
   * @return given person containing all values from given person and for unset values, all from this csv bean
   * @throws FormatException if any value in this csv bean has an invalid format
   */
  private Person updatePerson(Person person) throws FormatException {
    updateActorFields(person);
    if (person.getBirthdate() == null) {
      person.setBirthdate(startDate);
    }
    if (person.getDeathdate() == null) {
      person.setDeathdate(endDate);
    }
    if (person.getGender() == null && gender != null) {
      person.setGender(factory.getGender(gender));
    }
    if (person.getResidence() == null) {
      person.setResidence(factory.getCountries().stream().filter(c ->
          c.getName("fr").equals(country)).findFirst().orElse(null));
    }
    return person;
  }

  /**
   * Map all fields of this csv bean into given organization
   *
   * @param organization an organization to update
   * @return given organization updated with values from this csv bean where unset
   * @throws FormatException if any value in this csv bean has an invalid format
   */
  private Organization updateOrganization(Organization organization) throws FormatException {
    updateActorFields(organization);
    if (organization.getOldNames() == null) {
      organization.setOldNames(new ArrayList<>());
    }
    makeOldActorNames().stream().filter(n -> organization.getOldNames().stream().noneMatch(name -> name.equals(n)))
        .forEach(organization::addOldName);

    if (organization.getOfficialNumber() == null) {
      organization.setOfficialNumber(String.valueOf(number));
    }
    if (organization.getCreationDate() == null) {
      organization.setCreationDate(startDate);
    }
    if (organization.getTerminationDate() == null) {
      organization.setTerminationDate(endDate);
    }
    if (organization.getLegalStatus() == null) {
      organization.setLegalStatus(factory.getLegalStatus(legalStatus));
    }
    if(organization.getPlaces().isEmpty() && !values.isBlank(country) && values.isNumeric(country)){
      Place place = factory.findPlace(Long.decode(country));

      if(place != null){
        organization.getPlaces().add(place);
      }
    }

    if(organization.getTags().isEmpty()){
      List<Tag> socialObjects = new ArrayList<>();

      addSocialObjectToOrganization(socialObject1, socialObjects);
      addSocialObjectToOrganization(socialObject2, socialObjects);
      addSocialObjectToOrganization(socialObject3, socialObjects);

      organization.setTags(socialObjects);
    }

    makeSectorList().forEach(organization::addBusinessSector);
    return organization;
  }

  private void addSocialObjectToOrganization(Long socialObjectId, List<Tag> socialObjects){
    if(socialObject1 != null){
      Tag tag = tagFactory.retrieve(socialObject1);
      if(tag != null){
        socialObjects.add(tag);
      }
    }
  }

  /**
   * Update common fields of given actor with this bean values, ie names, avatar and cross-reference
   *
   * @param actor an actor to update
   * @throws FormatException if this bean's url is invalid
   */
  private void updateActorFields(Actor actor) throws FormatException {
    makeActorNames().stream().filter(n -> actor.getNames().stream().noneMatch(name -> name.equals(n)))
        .forEach(actor::addName);

    if (actor.getAvatar() == null) {
      actor.setAvatar(createContributorPicture());
    }

    if (actor.getCrossReference() == null) {
      actor.setCrossReference(url);
    }
  }

  private ContributorPicture createContributorPicture() {
    if(values.isURL(avatar)) {
      mustRetrieveAvatar = true;

      try {
        ContributorPicture picture = contributorFactory.getContributorPicture();

        picture.setExtension(avatar.substring(avatar.lastIndexOf('.')));
        picture.setUrl(avatar);
        picture.setContributorId(contributorFactory.getDefaultContributor().getId());

        return picture;
      } catch (FormatException e) {
        // must not be possible
      }
    }

    return null;
  }

  /**
   * Build a list of actor names from this bean names
   *
   * @return a list of actor names
   */
  private List<ActorName> makeActorNames() {
    List<ActorName> names = new ArrayList<>();
    if (firstEn != null || lastEn != null || pseudoEn != null) {
      names.add(makeName(firstEn, lastEn, pseudoEn, "en"));
    }
    if (firstFr != null || lastFr != null || pseudoFr != null) {
      names.add(makeName(firstFr, lastFr, pseudoFr, "fr"));
    }
    if (firstNl != null || lastNl != null || pseudoNl != null) {
      names.add(makeName(firstNl, lastNl, pseudoNl, "nl"));
    }
    if (firstDe != null || lastDe != null || pseudoDe != null) {
      names.add(makeName(firstDe, lastDe, pseudoDe, "de"));
    }
    return names;
  }

  /**
   * Build a list of actor names from this bean other names
   *
   * @return a list of actor names
   */
  private List<ActorName> makeOldActorNames() {
    List<ActorName> names = new ArrayList<>();
    if (firstEnOther != null || lastEnOther != null || pseudoEnOther != null) {
      names.add(makeName(firstEnOther, lastEnOther, pseudoEnOther, "en"));
    }
    if (firstFrOther != null || lastFrOther != null || pseudoFrOther != null) {
      names.add(makeName(firstFrOther, lastFrOther, pseudoFrOther, "fr"));
    }
    if (firstNlOther != null || lastNlOther != null || pseudoNlOther != null) {
      names.add(makeName(firstNlOther, lastNlOther, pseudoNlOther, "nl"));
    }
    if (firstDeOther != null || lastDeOther != null || pseudoDeOther != null) {
      names.add(makeName(firstDeOther, lastDeOther, pseudoDeOther, "de"));
    }
    return names;
  }

  /**
   * Map given fields into an actor name
   *
   * @param first a first name or acronym
   * @param last a last name
   * @param pseudo a pseudonym
   * @param lang 2-char iso-639-1 language code
   * @return the actor name containing given values
   */
  private ActorName makeName(String first, String last, String pseudo, String lang) {
    ActorName name = factory.getActorName(lang);
    name.setFirst(first);
    name.setLast(last);
    name.setPseudo(pseudo);
    return name;
  }

  /**
   * Build a list of fullnames-like to search for actors with same names as this bean names
   *
   * @return a list of string with all names concatenated as full names
   */
  private List<String> makeNamesList() {
    List<String> fullnames = new ArrayList<>();
    fullnames.add(makeFullname(firstFr, lastFr));
    fullnames.add(makeFullname(firstFrOther, lastFrOther));
    fullnames.add(makeFullname(firstNl, lastNl));
    fullnames.add(makeFullname(firstNlOther, lastNlOther));
    fullnames.add(makeFullname(firstEn, lastEn));
    fullnames.add(makeFullname(firstEnOther, lastEnOther));
    fullnames.add(makeFullname(firstDe, lastDe));
    fullnames.add(makeFullname(firstDeOther, lastDeOther));
    fullnames.add(makeFullname(pseudoFr, null));
    fullnames.add(makeFullname(pseudoFrOther, null));
    fullnames.add(makeFullname(pseudoEn, null));
    fullnames.add(makeFullname(pseudoEnOther, null));
    fullnames.add(makeFullname(pseudoNl, null));
    fullnames.add(makeFullname(pseudoNlOther, null));
    fullnames.add(makeFullname(pseudoDe, null));
    fullnames.add(makeFullname(pseudoDeOther, null));
    fullnames.removeIf(String::isEmpty);
    return fullnames;
  }

  /**
   * Create a fullname-like from given first and last name, null are replaced by empty strings
   *
   * @param first a first name
   * @param last a last name
   * @return a full name of the form first + " " + last
   */
  private String makeFullname(String first, String last) {
    return ((first != null ? first + " ": "") + (last != null ? last + " ": "")).trim();
  }

  /**
   * Transform all business sectors-related fields into a list of API business sectors
   *
   * @return a (possibly empty) list of sectors
   * @throws FormatException if any given sector does not match a EBusinessSector id (should never happen)
   */
  private List<BusinessSector> makeSectorList() throws FormatException {
    List<BusinessSector> sectors = new ArrayList<>();
    if (other == 1) {
      sectors.add(factory.getBusinessSector(EBusinessSector.CROSS_SECTORAL.id()));
    }else {
      if (agriculture == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.AGRICULTURE.id()));
      }
      if (art == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.ART.id()));
      }
      if (sport == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.SPORT.id()));
      }
      if (retail == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.RETAIL.id()));
      }
      if (building == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.BUILDING.id()));
      }
      if (education == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.EDUCATION.id()));
      }
      if (energy == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.ENERGY.id()));
      }
      if (environment == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.ENVIRONMENT.id()));
      }
      if (finance == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.FINANCE.id()));
      }
      if (tourism == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.TOURISM.id()));
      }
      if (industry == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.INDUSTRY.id()));
      }
      if (mining == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.MINING.id()));
      }
      if (justice == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.JUSTICE.id()));
      }
      if (press == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.PRESS.id()));
      }
      if (research == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.RESEARCH.id()));
      }
      if (health == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.HEALTH.id()));
      }
      if (security == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.SECURITY.id()));
      }
      if (information == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.INFORMATION.id()));
      }
      if (transport == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.TRANSPORT.id()));
      }
      if (other == 1) {
        sectors.add(factory.getBusinessSector(EBusinessSector.OTHER.id()));
      }
    }
    return sectors;
  }
}
