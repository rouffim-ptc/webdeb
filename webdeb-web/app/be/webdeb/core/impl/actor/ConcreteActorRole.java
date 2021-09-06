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

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.contribution.Contribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements an Actor role for an holding Actor in a particular contribution
 *
 * @author Fabian Gilson
 */
class ConcreteActorRole implements ActorRole {

  private Actor actor;
  private Contribution contribution;
  private Affiliation affiliation;

  // author, reporter, source author (in this order)
  private boolean[] roles = new boolean[EActorRole.values().length];

  /**
   * Construct an actor role with given actor and contribution
   *
   * @param actor an actor being bound to given contribution
   * @param contribution a contribution to which given actor plays a role in
   */
  ConcreteActorRole(Actor actor, Contribution contribution) {
    this.actor = actor;
    this.contribution = contribution;
  }

  @Override
  public Actor getActor() {
    return actor;
  }

  public void setActor(Actor actor) {
    this.actor = actor;
  }

  @Override
  public Contribution getContribution() {
    return contribution;
  }

  public void setContribution(Contribution contribution) {
    this.contribution = contribution;
  }

  @Override
  public Affiliation getAffiliation() {
    return affiliation;
  }

  @Override
  public void setAffiliation(Affiliation affiliation) {
    this.affiliation = affiliation;
  }

  @Override
  public boolean isAuthor() {
    return roles[EActorRole.AUTHOR.id()];
  }

  @Override
  public void setIsAuthor(boolean isAuthor) {
    roles[EActorRole.AUTHOR.id()] = isAuthor;
  }

  @Override
  public boolean isReporter() {
    return roles[EActorRole.REPORTER.id()];
  }

  @Override
  public void setIsReporter(boolean isReporter) {
    roles[EActorRole.REPORTER.id()] = isReporter;
  }

  @Override
  public boolean isJustCited() {
    return roles[EActorRole.CITED.id()];
  }

  @Override
  public void setIsJustCited(boolean isCited) {
    roles[EActorRole.CITED.id()] = isCited;
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();
    if (actor != null) {
      fieldsInError.addAll(actor.isValid());
    } else {
      fieldsInError.add("actor");
    }
    if (affiliation != null) {
      fieldsInError.addAll(affiliation.isValid());
    }
    return fieldsInError;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ActorRole && hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 51
        + actor.hashCode()
        + contribution.hashCode()
        + (affiliation != null ? affiliation.hashCode() : 0)
        + Boolean.valueOf(isAuthor()).hashCode() +
        + Boolean.valueOf(isReporter()).hashCode() +
        + Boolean.valueOf(isJustCited()).hashCode();
  }

  @Override
  public String toString() {
    String aff = affiliation != null ? affiliation.toString() + " (" + affiliation.getId() + ")" : "";
    return getActor().getFullname("en") + " (" + getActor().getId() + ") with function " + aff
        + " is author:" + isAuthor() + ",reporter:" + isReporter();
  }
}
