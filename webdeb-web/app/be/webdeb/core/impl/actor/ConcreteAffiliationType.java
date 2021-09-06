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

import be.webdeb.core.api.actor.*;
import be.webdeb.core.impl.contribution.type.AbstractPredefinedIntValue;

import java.util.Map;

/**
 * This class implements the type of affiliations binding an affiliated organization to another actor.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteAffiliationType extends AbstractPredefinedIntValue implements AffiliationType {

  private int actorType;
  private int subtype;

  private EAffiliationType etype;
  private EAffiliationActorType eactortype;
  private EAffiliationSubtype esubtype;

  ConcreteAffiliationType(int type, int actorType, int subtype, Map<String, String> i18names) {
    super(type, i18names);
    this.actorType = actorType;
    this.subtype = subtype;

    this.etype = EAffiliationType.value(type);
    this.eactortype = EAffiliationActorType.value(actorType);
    this.esubtype = EAffiliationSubtype.value(subtype);

  }

  @Override
  public int getActorId() {
    return actorType;
  }

  @Override
  public int getSubId() {
    return subtype;
  }

  @Override
  public EAffiliationType getEType() {
    return etype;
  }

  @Override
  public EAffiliationActorType getEActortype() {
    return eactortype;
  }

  @Override
  public EAffiliationSubtype getESubtype() {
    return esubtype;
  }

  @Override
  public boolean isValid() {

    if(EAffiliationType.value(type) == null){
      return false;
    }

    if(EAffiliationActorType.value(actorType) == null){
      return false;
    }

    if(EAffiliationSubtype.value(subtype) == null){
      return false;
    }

    return true;
  }
}
