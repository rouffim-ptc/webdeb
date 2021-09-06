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

package be.webdeb.core.impl.text;

import be.webdeb.core.api.text.ETextType;
import be.webdeb.core.api.text.TextType;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;

import java.util.Map;

/**
 * This class implements a TextType
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 * @see be.webdeb.core.api.text.ETextType
 */
class ConcreteTextType extends AbstractPredefinedIntValue implements TextType {

  private ETextType eType;

  ConcreteTextType(int type, Map<String, String> i18names) {
    super(type, i18names);
    this.eType = ETextType.value(type);
  }

  @Override
  public ETextType getEType() {
    return eType;
  }

  @Override
  public boolean isValid() {

    if(ETextType.value(type) == null){
      return false;
    }

    return true;

  }
}
