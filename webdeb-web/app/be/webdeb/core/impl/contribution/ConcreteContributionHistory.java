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

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contribution.ContributionHistory;
import be.webdeb.core.api.contribution.EModificationStatus;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.exception.FormatException;

import java.util.Date;

/**
 * This class implements the ContributionHistory interface.
 *
 * @author Fabian Gilson
 */
class ConcreteContributionHistory implements ContributionHistory {

  private ContributionFactory factory;
  private Contributor contributor;
  private EModificationStatus status;
  private String trace;
  private Date version;

  /**
   * Default constructor, create an history trace
   *
   * @param contributor the contributor that issued this history trace
   * @param status the modification status
   * @param trace the complete trace
   * @param version the date at which this modification happened
   */
  ConcreteContributionHistory(Contributor contributor, EModificationStatus status, String trace, Date version, ContributionFactory factory) {
    this.contributor = contributor;
    this.status = status;
    this.trace = trace;
    this.version = version;
    this.factory = factory;
  }

  @Override
  public Contributor getContributor() {
    return contributor;
  }

  @Override
  public EModificationStatus getModificationStatus() {
    return status;
  }

  @Override
  public String getModificationStatusTitle(String lang) {
    try {
      return factory.getModificationStatus(status.id()).getName(lang);
    } catch (FormatException e) {
      return "";
    }
  }

  @Override
  public String getTrace() {
    return trace;
  }

  @Override
  public Date getVersion() {
    return version;
  }
}
