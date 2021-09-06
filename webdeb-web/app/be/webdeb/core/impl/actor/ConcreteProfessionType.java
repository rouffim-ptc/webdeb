/*
 *  Copyright 2014-2018 University of Namur (PReCISE) - University of Louvain (Girsef - CENTAL).
 *  This is part of the WebDeb software (WDWEB), a collaborative platform to record and analyze
 *  argumentation-based debates. This is free software:  you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License version 3 as published by the
 *  Free Software Foundation. It is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.
 *
 *  See <https://webdeb.be/> for a running instance of a webdeb web platform.
 *  See the GNU Lesser General Public License (LGPL) for more details over the license terms.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this copy.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.EProfessionType;
import be.webdeb.core.api.actor.ProfessionType;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;

import java.util.Map;

/**
 * This class implements the type of profession binding an affiliated person to organization.
 *
 * @author Martin Rouffiange
 */
class ConcreteProfessionType extends AbstractPredefinedIntValue implements ProfessionType {

    private EProfessionType etype;
    private int subType;

    ConcreteProfessionType(Integer type, Map<String, String> i18names) {
        super(type, i18names);
        this.etype = EProfessionType.value(type);
        this.subType = isValid() ? etype.getSubtype() : -1;
    }

    @Override
    public int getSubId() {
        return subType;
    }

    @Override
    public EProfessionType getEType() {
        return etype;
    }

    @Override
    public boolean isValid() {

        if(EProfessionType.value(type) == null) {
            return false;
        }

        return true;
    }
}
