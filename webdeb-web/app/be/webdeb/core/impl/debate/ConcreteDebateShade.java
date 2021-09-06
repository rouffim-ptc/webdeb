/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.impl.debate;

import be.webdeb.core.api.debate.DebateShade;
import be.webdeb.core.api.debate.EDebateShade;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * This class implements an DebateShade
 *
 * @author Martin Rouffiange
 */
public class ConcreteDebateShade extends AbstractPredefinedIntValue implements DebateShade {

  private EDebateShade eshade;

  ConcreteDebateShade(Integer linkShade, Map<String, String> shadeNames) {
    super(linkShade, shadeNames);
    eshade = EDebateShade.value(linkShade);
  }

  @Override
  public EDebateShade getEType() {
    return eshade;
  }

  /*
   * Convenience methods
   */

  @JsonIgnore
  @Override
  public boolean isValid() {

    if (EDebateShade.value(type) == null) {
      return false;
    }

    return true;
  }
}
