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

import be.webdeb.core.api.actor.EGenderType;
import be.webdeb.core.api.actor.Gender;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedStringValue;

import java.util.Map;

/**
 * This class implements a gender
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ConcreteGender extends AbstractPredefinedStringValue implements Gender {

  private EGenderType etype;

  /**
   * Constructor
   *
   * @param gender a gender id
   * @param i18names a map of language ISO code and gender names
   */
  public ConcreteGender(String gender, Map<String, String> i18names) {
    super(gender, i18names);
    this.etype = EGenderType.value(getCodeAsChar());
  }

  @Override
  public EGenderType getEType() {
    return etype;
  }

  @Override
  public boolean isValid() {
    if(EGenderType.value(getCodeAsChar()) == null) {
      return false;
    }
    return true;
  }
}
