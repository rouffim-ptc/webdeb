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

package be.webdeb.presentation.web.controllers.entry.tag;

/**
 * Simple form class that contains all attributes to link a contribution to a tag
 *
 * @author Martin Rouffiange
 */
public class TagContributionForm {

  /*
   * Link attributes
   */
  protected Long tagId;
  protected Long contributionId;
  protected String contributionName;

  /**
   * Construct a TagContributionForm for a given tag and contribution
   *
   * @param tagId a tag id
   * @param contributionId a contribution id
   */
  public TagContributionForm(Long tagId, Long contributionId) {
    this.tagId = tagId;
    this.contributionId = contributionId;
  }

  @Override
  public String toString() {
    return "tag [" + tagId + "], contribution [" + contributionId + "]";
  }

  /*
   * GETTERS
   */

  public Long getTagId() {
    return tagId;
  }

  public Long getContributionId() {
    return contributionId;
  }

  public String getContributionName() {
    return contributionName;
  }
}
