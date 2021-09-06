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

package be.webdeb.presentation.web.controllers.account.admin;

/**
 * Simple form class that contains a free copyright source for text
 *
 * @author Martin Rouffiange
 */
public class TextCopyrightfreeSourceForm {

  private Integer sourceId = -1;
  private String domainName;

  /**
   * Play / Json compliant constructor
   */
  public TextCopyrightfreeSourceForm() {
    // needed by json/play
  }

  /**
   * Initialize a free source form
   *
   * @param sourceId the id of the free source
   * @param domainName the domain name of the free source
   */
  public TextCopyrightfreeSourceForm(int sourceId, String domainName) {
    this.sourceId = sourceId;
    this.domainName = domainName;
  }

  public Integer getSourceId() {
    return (sourceId != null ? sourceId : -1);
  }

  public void setSourceId(Integer sourceId) {
    this.sourceId = sourceId;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }
}
