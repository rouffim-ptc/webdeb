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

import be.webdeb.core.api.contribution.EValidationState;
import be.webdeb.core.api.contribution.ValidationState;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;

import java.util.Map;

/**
 * This class implements a ValidationState
 *
 * @author Martin Rouffiange
 */
class ConcreteValidationState extends AbstractPredefinedIntValue implements ValidationState {

    private EValidationState estate;

    ConcreteValidationState(int state, Map<String, String> i18names) {
        super(state, i18names);
        estate = EValidationState.value(state);
    }

    @Override
    public EValidationState getEType() {
        return estate;
    }

    @Override
    public boolean isValid() {

        if(EValidationState.value(type) == null) {
            return false;
        }

        return true;
    }
}
