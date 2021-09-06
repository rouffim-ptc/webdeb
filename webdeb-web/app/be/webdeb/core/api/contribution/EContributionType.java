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

package be.webdeb.core.api.contribution;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This enumeration holds types for contributions
 * <ul>
 *   <li>-1 for ACTOR, TEXT and ARGUMENT</li>
 *   <li>0 for ACTOR</li>
 *   <li>1 for DEBATE</li>
 *   <li>2 for TEXT</li>
 *   <li>3 for CITATION</li>
 *   <li>4 for ARGUMENT</li>
 *   <li>5 for ARGUMENT_DICTIONARY</li>
 *   <li>6 for TAG</li>
 *   <li>7 for AFFILIATION</li>
 *   <li>8 for ARGUMENT_JUSTIFICATION</li>
 *   <li>9 for ARGUMENT_SIMILARITY</li>
 *   <li>10 for CITATION_JUSTIFICATION</li>
 *   <li>11 for DEBATE_SIMILARITY</li>
 *   <li>12 for DEBATE_HAS_TAG_DEBATE</li>
 *   <li>13 for TAG_LINK</li>
 *   <li>14 for EXTERNAL_TEXT</li>
 *   <li>15 for EXTERNAL_CITATION</li>
 *   <li>16 for CONTEXT_HAS_CATEGORY</li>
 *   <li>17 for DEB_HAS_ARGUMENT_JUSTIFICATION</li>
 *   <li>18 for DEB_HAS_CITATION_JUSTIFICATION</li>
 *   <li>19 for DEB_HAS_TEXT/li>
 *   <li>20 for CONTEXT_HAS_SUBDEBATE</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public enum EContributionType {

  /**
   * any type of concrete contribution, ie, actors, texts and arguments
   */
  ALL(-1),

  /**
   * individual or organizational actor
   */
  ACTOR(0),

  /**
   * debate a subject that can regroups arguments
   */
  DEBATE(1),

  /**
   * text (holding excerpts)
   */
  TEXT(2),

  /**
   * citation extracted from text
   */
  CITATION(3),

  /**
   * argument that resume citations
   */
  ARGUMENT(4),

  /**
   * argument dictionary
   */
  ARGUMENT_DICTIONARY(5),

  /**
   * tag that resume a contribution theme
   */
  TAG(6),

  /**
   * actor or contributor's affiliation (organization and/or function)
   */
  AFFILIATION(7),

  /**
   * justification link between arguments and a context
   */
  ARGUMENT_JUSTIFICATION(8),

  /**
   * similarity link between two arguments
   */
  ARGUMENT_SIMILARITY(9),

  /**
   * justification link between citation and a context
   */
  CITATION_JUSTIFICATION(10),

  /**
   * similarity link between two debates
   */
  DEBATE_SIMILARITY(11),

  /**
   * Link between a debate multiple and a tag debate
   */
  DEBATE_HAS_TAG_DEBATE(12),
  /**
   * Link between two tags
   */
  TAG_LINK(13),

  /**
   * temporary text from external sources, that wait for user confirmation
   */
  EXTERNAL_TEXT(14),

  /**
   * temporary citation from external sources, that wait for user confirmation
   */
  EXTERNAL_CITATION(15),

  /**
   * context contribution has category
   */
  CONTEXT_HAS_CATEGORY(16),

  /**
   * Citation position in a debate
   */
  CITATION_POSITION(17),

  /**
   * Debate's texts
   */
  DEBATE_HAS_TEXT(19),

  /**
   * Context's subdebates
   */
  CONTEXT_HAS_SUBDEBATE(20),

  /**
   * TECHNICAL USE
   */
  ACTOR_PERSON(100);

  private int id;
  private static Map<Integer, EContributionType> map = new LinkedHashMap<>();

  static {
    for (EContributionType type : EContributionType.values()) {
      map.put(type.id, type);
    }
  }

  /**
   * Constructor
   *
   * @param id an int representing a contribution type
   */
  EContributionType(int id) {
    this.id = id;
  }

  /**
   * Get the enum value for a given id
   *
   * @param id an int representing an EContributionType
   * @return the EContributionType enum value corresponding to the given id, null otherwise.
   */
  public static EContributionType value(int id) {
    return map.get(id);
  }

  /**
   * Get this id
   *
   * @return an int representation of this contribution type
   */
  public int id() {
    return id;
  }

  /**
   * Get true if the contribution type is a context contribution
   *
   * @return true if the contribution type is a context contribution
   */
  public boolean isContextContribution(){
    return this == EContributionType.TEXT || this == EContributionType.DEBATE || this == EContributionType.TAG;
  }

  /**
   * Get true if the contribution type is a textual contribution
   *
   * @return true if the contribution type is a textual contribution
   */
  public boolean isTextualContribution(){
    return this == EContributionType.TEXT || this == EContributionType.DEBATE || this == EContributionType.CITATION;
  }

  /**
   * Get true if the contribution type is a context contribution
   *
   * @return true if the contribution type is a context contribution
   */
  public boolean isLink(){
    return this == EContributionType.ARGUMENT_JUSTIFICATION || this == EContributionType.ARGUMENT_SIMILARITY ||
           this == EContributionType.CITATION_JUSTIFICATION || this == EContributionType.DEBATE_SIMILARITY ||
           this == EContributionType.DEBATE_HAS_TAG_DEBATE || this == EContributionType.TAG_LINK ||
           this == CITATION_POSITION || this == EContributionType.CONTEXT_HAS_CATEGORY;
  }

  public boolean isJustificationLink(){
    return this == EContributionType.ARGUMENT_JUSTIFICATION || this == EContributionType.CITATION_JUSTIFICATION;
  }

  public boolean isMajorContributionType(){
    return this == EContributionType.ACTOR || this == EContributionType.DEBATE || this == EContributionType.TEXT
          || this == EContributionType.CITATION || this == EContributionType.ARGUMENT || this == EContributionType.TAG;
  }

  public static List<EContributionType> getMajorContributionETypes() {
    return Arrays.asList(EContributionType.ACTOR, EContributionType.DEBATE, EContributionType.TEXT, EContributionType.CITATION, EContributionType.TAG);
  }

  public static List<Integer> getMajorContributionTypes() {
    return Arrays.asList(EContributionType.ACTOR.id, EContributionType.DEBATE.id, EContributionType.TEXT.id, EContributionType.CITATION.id, EContributionType.TAG.id);
  }

  /**
   * Get true if the contribution type is always public
   *
   * @return true if the contribution type is always public
   */
  public boolean isAlwaysPublic(){
    return  this == EContributionType.TEXT || this == EContributionType.ACTOR || this == EContributionType.ARGUMENT ||
            this == EContributionType.ARGUMENT_DICTIONARY || this == EContributionType.TAG ||
            this == EContributionType.EXTERNAL_TEXT || this == EContributionType.EXTERNAL_CITATION ||
            this == EContributionType.CITATION_POSITION;
  }

  /**
   * Get true if the contribution type can be implicated in a justification link
   *
   * @return true if the contribution type can be in a justification link
   */
  public boolean canBeInJustifciationLink(){
    return isContextContribution() || this == EContributionType.CITATION || this == EContributionType.ARGUMENT
            || this == EContributionType.TAG;
  }

  public String getContributionTypeLogoName(){
    switch (this){
      case ACTOR :
        return "fas fa-street-view";
      case DEBATE :
       return "far fa-comments";
      case TEXT :
        return "far fa-file-alt";
      case CITATION :
        return "fas fa-align-left";
      case ARGUMENT :
        return "far fa-comment";
      case TAG :
        return "far fa-tag";
    }

    return "";
  }

}
