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

package be.webdeb.core.api.actor;

import play.api.Play;
import play.i18n.Lang;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumeration represents the type of affiliations for organizations being affiliated to other actors
 * <ul>
 *   <li>-1 for UNSET</li>
 *   <li>0 for OWNED_ATLEAST_50</li>
 *   <li>1 for OWNED_UPTO_50</li>
 *   <li>2 for OWNED_UPTO_25</li>
 *   <li>3 for OWNED_UNKNOWN_PC</li>
 *   <li>4 for DEPARTMENT_OF</li>
 *   <li>5 for MEMBER_OF</li>
 *   <li>6 for PRODUCED_BY</li>
 *   <li>7 for AWARDED_BY</li>
 *   <li>8 for PARTICIPATING_IN</li>
 *   <li>9 for CABINET_OF</li>
 *   <li>10 for GRADUATING_FROM</li>
 *   <li>11 for SON_OF</li>
 *   <li>12 for OWNS_ATLEAST_50</li>
 *   <li>13 for OWNS_UPTO_50</li>
 *   <li>14 for OWNS_UPTO_25</li>
 *   <li>15 for OWNS_UNKNOWN_PC</li>
 *   <li>16 for HAS_DIVISION</li>
 *   <li>17 for HAS_MEMBER</li>
 *   <li>18 for PRODUCES</li>
 *   <li>19 for AWARDED</li>
 *   <li>20 for HAS_PARTICIPANT</li>
 *   <li>21 for HAS_CABINET</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EAffiliationType {

  /**
   * Type is unset (temporary solution for migration purpose of legacy data), all UNSET should be updated
   */
  UNSET(-1),
  /**
   * Organization or person is owned or financed at 50 or more p.c by its affiliation actor (any type)
   */
  OWNED_ATLEAST_50(0),
  /**
   * Organization or person is owned or financed between 25 and 50 p.c by its affiliation actor (any type)
   */
  OWNED_UPTO_50(1),
  /**
   * Organization or person is owned or financed up to at 25 p.c by its affiliation actor (any type)
   */
  OWNED_UPTO_25(2),
  /**
   * Organization or person is owned or financed at an unknown p.c by its affiliation actor (any type)
   */
  OWNED_UNKNOWN_PC(3),
  /**
   * Organization is a department of its affiliation organization
   */
  DEPARTMENT_OF(4),
  /**
   * Organization is a member of its affiliation organization
   */
  MEMBER_OF(5),
  /**
   * Organization is produced or organized by its affiliation organization
   */
  PRODUCED_BY(6),
  /**
   * Organization or person is awarded or labbeled by its affiliation organization
   */
  AWARDED_BY(7),
  /**
   * Organization or person is participating in its affiliation organization
   */
  PARTICIPATING_IN(8),
  /**
   * Organization is cabinet of its affiliation person
   */
  CABINET_OF(9),
  /**
   * Person is granduating from an organization
   */
  GRADUATING_FROM(10),
  /**
   * Person is a son of another person
   */
  SON_OF(11),
  /**
   * Actor (any type) owns or finances at 50 or more p.c by its affiliation organization
   */
  OWNS_ATLEAST_50(12),
  /**
   * Actor (any type) owns or finances between 25 and 50 p.c by its affiliation organization
   */
  OWNS_UPTO_50(13),
  /**
   * Actor (any type) owns or finances up to at 25 p.c by its affiliation organization
   */
  OWNS_UPTO_25(15),
  /**
   * Actor (any type) owns or finances at an unknown p.c by its affiliation organization
   */
  OWNS_UNKNOWN_PC(15),
  /**
   * Organization has a department as its affiliation organization
   */
  HAS_DEPARTMENT(16),
  /**
   * Organization has a member as its affiliation organization
   */
  HAS_MEMBER(17),
  /**
   * Organization produces or organizes its affiliation organization
   */
  PRODUCES(18),
  /**
   * Organization awards or labels by its affiliation actor (any type)
   */
  AWARDS(19),
  /**
   * Organization has participant as its affiliation actor (any type)
   */
  HAS_PARTICIPANT(20),
  /**
   * Person has a cabinet as its affiliation organization
   */
  HAS_CABINET(21);

  private int id;
  private static Map<Integer, EAffiliationType> map = new LinkedHashMap<>();

  @Inject
  private MessagesApi i18n = Play.current().injector().instanceOf(play.i18n.MessagesApi .class);

  static {
    for (EAffiliationType type : EAffiliationType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing an affiliation type
   */
  EAffiliationType(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EAffiliationType
   * @return the EaffiliationType enum value corresponding to the given id, null otherwise.
   */
  public static EAffiliationType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this EAffiliationType
   */
  public int id() {
    return id;
  }

  /**
   * Get the EAffiliationType for persistence (always keep type smaller than EAffiliationType.OWNS_ATLEAST_50
   *
   * @return the corresponding persistence EAffiliationType
   */
  public EAffiliationType getPersistenceType(){
    return this.id >= OWNS_ATLEAST_50.id ? getReverseType() : this;
  }

  /**
   * Get the reversed affiliation type
   *
   * @return the corresponding EAffiliationType
   */
  public EAffiliationType getReverseType(){
    switch(this){
      case OWNED_ATLEAST_50 :
        return OWNS_ATLEAST_50;
      case OWNED_UPTO_50 :
        return OWNS_UPTO_50;
      case OWNED_UPTO_25 :
        return OWNS_UPTO_25;
      case OWNED_UNKNOWN_PC :
        return OWNS_UNKNOWN_PC;
      case DEPARTMENT_OF :
        return HAS_DEPARTMENT;
      case MEMBER_OF :
        return HAS_MEMBER;
      case PRODUCED_BY :
        return PRODUCES;
      case AWARDED_BY :
        return AWARDS;
      case PARTICIPATING_IN :
        return HAS_PARTICIPANT;
      case CABINET_OF :
        return HAS_CABINET;
      case OWNS_ATLEAST_50 :
        return OWNED_ATLEAST_50;
      case OWNS_UPTO_50 :
        return OWNED_UPTO_50;
      case OWNS_UPTO_25 :
        return OWNED_UPTO_25;
      case OWNS_UNKNOWN_PC :
        return OWNED_UNKNOWN_PC;
      case HAS_DEPARTMENT :
        return DEPARTMENT_OF;
      case HAS_MEMBER :
        return MEMBER_OF;
      case PRODUCES :
        return PRODUCED_BY;
      case AWARDS :
        return AWARDED_BY;
      case HAS_PARTICIPANT :
        return PARTICIPATING_IN;
      case HAS_CABINET :
        return CABINET_OF;
      default :
        return this;
    }
  }

  public String getSubstitudeName(String lang){
    switch(this){
      case OWNED_ATLEAST_50 :
      case OWNED_UPTO_50 :
      case OWNED_UPTO_25 :
      case OWNED_UNKNOWN_PC :
        return i18n.get(Lang.forCode(lang), "actor.aff.ownedby.label");
      case OWNS_ATLEAST_50 :
      case OWNS_UPTO_50 :
      case OWNS_UPTO_25 :
      case OWNS_UNKNOWN_PC :
        return i18n.get(Lang.forCode(lang), "actor.aff.owns.label");
      default :
        return null;
    }
  }
}
