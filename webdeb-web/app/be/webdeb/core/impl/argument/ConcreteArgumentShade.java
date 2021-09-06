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

package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.ArgumentShade;
import be.webdeb.core.api.argument.EArgumentShade;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * This class implements an ArgumentShade
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class ConcreteArgumentShade extends AbstractPredefinedIntValue implements ArgumentShade {

    private EArgumentShade eshade;

    ConcreteArgumentShade(Integer linkShade, Map<String, String> shadeNames) {
        super(linkShade, shadeNames);
        eshade = EArgumentShade.value(linkShade);
    }

    @Override
    public String getShadeNameWithDots(String lang) {
        return null;
    }

    @Override
    public EArgumentShade getEType() {
        return eshade;
    }

    /*
     * Convenience methods
     */

    @JsonIgnore
    @Override
    public boolean isValid() {

        if (EArgumentShade.value(type) == null) {
            return false;
        }

        return true;
    }
}
