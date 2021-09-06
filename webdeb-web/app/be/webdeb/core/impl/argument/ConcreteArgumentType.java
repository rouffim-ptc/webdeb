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

package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.ArgumentType;
import be.webdeb.core.api.argument.EArgumentType;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * This class implements an ArgumentType
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteArgumentType extends AbstractPredefinedIntValue implements ArgumentType {
  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private EArgumentType etype;

  /**
   * Constructor
   *
   * @param type the argument type id
   * @param i18Names a map of pairs of the form (2-char iso-code, type name)
   */
  ConcreteArgumentType(Integer type, Map<String, String> i18Names) {
    super(type, i18Names);
    etype = EArgumentType.value(type);
  }

  @Override
  public EArgumentType getEType(){
    return etype;
  }

  /*
   * Convenience methods
   */
  @JsonIgnore
  @Override
  public boolean isValid() {

    if(EArgumentType.value(type) == null){
      return false;
    }

    return true;
  }

}

