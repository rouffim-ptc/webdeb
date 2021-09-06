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

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.infra.persistence.accessor.api.AffiliationAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements an affiliation of an Actor or a Contributor
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteAffiliation extends AbstractContribution<ActorFactory, AffiliationAccessor> implements Affiliation, Comparable<Affiliation> {

  private Actor actor = null;
  private Profession function = null;
  private Actor affiliated = null;
  private String startDate = null;
  private String endDate = null;
  private PrecisionDateType startDateType = null;
  private PrecisionDateType endDateType = null;
  private EAffiliationType affiliationType = null;

  /**
   * Construct an affiliation object
   *
   * @param factory the actor factory
   * @param accessor the affiliation accessor
   */
  ConcreteAffiliation(ActorFactory factory, AffiliationAccessor accessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    type = EContributionType.AFFILIATION;
  }

  @Override
  public Actor getActor() {
    return actor;
  }

  @Override
  public void setActor(Actor actor) {
    this.actor = actor;
  }

  @Override
  public Profession getFunction() {
    return function;
  }

  @Override
  public void setFunction(Profession function) {
    this.function = function;
  }

  @Override
  public Actor getAffiliated() {
    return affiliated;
  }

  @Override
  public void setAffiliated(Actor affiliated) {
    this.affiliated = affiliated;
  }

  @Override
  public EAffiliationType getAffiliationType() {
    return affiliationType;
  }

  @Override
  public void setAffiliationType(EAffiliationType affiliationType) throws FormatException {
    this.affiliationType = affiliationType;
  }

  @Override
  public String getFullfunction(String lang, boolean gendered, boolean withOrganization) {
    if (actor != null){
      String name = actor.getFullname(lang);
      String func = "";
      if (function != null) {
        String gender = null;
        if(affiliated != null)
          gender = factory.getPersonGender(affiliated.getId());
        if (gendered && gender != null)
          func = function.getName(lang, gender);
        else
          func = function.getGendersNames(lang);
      }
      return func + (withOrganization ? " - " + name : "");
    }
    return "";
  }

  @Override
  public AffiliationType getAffType() {
    if(affiliationType != null) {
      try {
        return factory.getAffiliationType(affiliationType.id());
      } catch (FormatException e) {
        logger.debug("unknown aff type");
      }
    }
    return null;
  }

  @Override
  public String getStartDate() {
    return startDate;
  }

  @Override
  public void setStartDate(String startDate) throws FormatException {
    if (startDate != null && !factory.getValuesHelper().isDate(startDate)) {
      throw new FormatException(FormatException.Key.AFFILIATION_ERROR, "given start date is not valid " + startDate);
    }
    if (endDate != null && !factory.getValuesHelper().isBefore(startDate, endDate)) {
      throw new FormatException(FormatException.Key.AFFILIATION_ERROR, "given date " + startDate + " is later than end date " + endDate);
    }

    // harmonize to "/" separator
    if (startDate != null) {
      this.startDate = startDate.replaceAll("\\.", "/").trim();
    }
  }

  @Override
  public String getEndDate() {
    return endDate;
  }

  @Override
  public void setEndDate(String endDate) throws FormatException {
    if (endDate != null && !factory.getValuesHelper().isDate(endDate)) {
        throw new FormatException(FormatException.Key.AFFILIATION_ERROR, "given end date is not valid " + endDate);
    }
    if (startDate != null && !factory.getValuesHelper().isBefore(startDate, endDate)) {
      throw new FormatException(FormatException.Key.AFFILIATION_ERROR, "given date " + endDate + " is prior to start date " + startDate);
    }
    // harmonize to "/" separator
    if (endDate != null) {
      this.endDate = endDate.replaceAll("\\.", "/").trim();
    }
  }

  @Override
  public PrecisionDateType getStartType() {
    return startDateType;
  }

  @Override
  public void setStartDateType(PrecisionDateType type) throws FormatException {
    if(type != null && type.getEType().isPast()) {
      startDateType = type;
    } else {
      throw new FormatException(FormatException.Key.UNKNOWN_PRECISION_DATE_TYPE, String.valueOf(type));
    }
  }

  @Override
  public PrecisionDateType getEndType() {
    return endDateType;
  }

  @Override
  public void setEndDateType(PrecisionDateType type) throws FormatException {
    if(type != null && !type.getEType().isPast()) {
      endDateType = type;
    } else {
      throw new FormatException(FormatException.Key.UNKNOWN_PRECISION_DATE_TYPE, String.valueOf(type));
    }
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
    String isValid = String.join(",", isValid());
    if (isValid.length() != 0) {
      throw new FormatException(FormatException.Key.AFFILIATION_ERROR, isValid);
    }
    return accessor.save(this, currentGroup, contributor);
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long actor, int currentGroup, Long contributor) throws FormatException, PermissionException, PersistenceException {
    String isValid = String.join(",", isValid());
    if (isValid.length() != 0) {
      logger.debug("affiliation contains invalid fields " + isValid);
      throw new FormatException(FormatException.Key.AFFILIATION_ERROR, isValid);
    }
    return accessor.save(this, actor, currentGroup, contributor);
  }

  @Override
  public void remove(Long contributor) throws PermissionException, PersistenceException {
    accessor.remove(id, contributor, type, true);
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();

    // if both dates are given and endDate not "ongoing", must be ordered
    if (!factory.getValuesHelper().isBlank(startDate) && !factory.getValuesHelper().isBlank(endDate)
            && !factory.getValuesHelper().isBefore(startDate, endDate)) {
      fieldsInError.add("invalid dates");
    }

    // either a function or an actor must be specified
    if (function == null && actor == null) {
      fieldsInError.add("actor or function is mandatory");
      // if such error occur, must return now otherwise next check will crash
      return fieldsInError;
    }

    return fieldsInError;
  }

  @Override
  public String toString() {
    return "affiliation" + (function != null ? " " + function.toString() : "")
        + (affiliationType != null ? " " + affiliationType : "")
        + (actor != null ? " " + actor.toString() : "")
        + " (" + startDate + "--" + endDate + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Affiliation)) {
      return false;
    }

    // check actor
    Affiliation affiliation = (Affiliation) obj;
    if (actor != null) {
      if (!actor.equals(affiliation.getActor())) {
        return false;
      }
    } else {
      if (affiliation.getActor() != null) {
        return false;
      }
    }

    // check function
    if (function != null) {
      if (!function.equals(affiliation.getFunction())) {
        return false;
      }
    } else {
      if (affiliation.getFunction() != null) {
        return false;
      }
    }

    // now check dates, say both affiliations are equals if given obj is less precise in date
    if (startDate != null && affiliation.getStartDate() == null) {
      return false;
    }
    if (endDate != null && affiliation.getEndDate() == null) {
      return false;
    }

    // everything looks the same, check on hashcode
    return hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 37
        + (actor != null ? actor.hashCode() : 0)
        + (function != null ? function.hashCode() : 0)
        + (startDate != null ? startDate.hashCode() : 0)
        + (endDate != null ? endDate.hashCode() : 0);
  }

  @Override
  public int compareTo(Affiliation a) {
    if (a == null) {
      return -1;
    }

    int result = -1;
    // if we have at least an enddate try to compare on this one
    if (endDate != null || a.getEndDate() != null) {
      if (endDate != null && a.getEndDate() != null) {
        result = -factory.getValuesHelper().compareDatesAsString(endDate, a.getEndDate());
        // if equality, try next comparison criteria
        if (result != 0) {
          return result;
        }
      } else {
        return endDate == null ? 1 : -1;
      }
    }

    // try on start date analogously
    if (startDate != null || a.getStartDate() != null) {
      if (startDate != null && a.getStartDate() != null) {
        int previous = result;
        result = - factory.getValuesHelper().compareDatesAsString(startDate, a.getStartDate());
        if (result != 0) {
          // if result == 0, ie end dates are equals, put longer affiliations in priority
          // used to sort affiliated list appropriately
          return previous == 0 ? -result : result;
        }
      } else {
        return startDate == null ? 1 : -1;
      }
    }

    // affiliation actor
    if (actor != null || a.getActor() != null) {
      if (actor != null && a.getActor() != null) {
        result = actor.getFullname(factory.getDefaultLanguage()).compareTo(a.getActor().getFullname(factory.getDefaultLanguage()));
        if (result != 0) {
          return result;
        }
      } else {
        return actor == null ? 1 : -1;
      }
    }

    // using english as sorting language here, as last possibility
    if (function != null || a.getFunction() != null) {
      if (function != null && a.getFunction() != null) {
        result = function.getName("en").compareTo(function.getName("en"));
        if (result != 0) {
          return result;
        }
      } else {
        return function == null ? 1 : -1;
      }
    }

    // no way to disambiguate, use id
    return id.compareTo(a.getId());
  }
}
