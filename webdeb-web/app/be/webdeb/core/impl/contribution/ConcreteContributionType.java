/*
 * WebDeb - Copyright (C) <2014-2019> <Universit√© catholique de Louvain (UCL), Belgique ; Universit√© de Namur (UNamur), Belgique>
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

import be.webdeb.core.api.contribution.ContributionType;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;

import java.util.Map;

/**
 * This class implements the contribution type interface.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteContributionType extends AbstractPredefinedIntValue implements ContributionType {

  private EContributionType ctype;

  ConcreteContributionType(Integer type, Map<String, String> i18names) {
    super(type, i18names);
    ctype = EContributionType.value(type);
  }

  @Override
  public EContributionType getEType(){
    return ctype;
  }

  @Override
  public boolean isValid() {
    if(EContributionType.value(type) == null) {
      return false;
    }

    return true;
  }
}
