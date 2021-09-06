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

import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This abstract class implements common functions to textual contributions (text and arguments) like adding
 * folders (former topics) and binding actors.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class AbstractTextualContribution<T extends ContributionFactory,V extends ContributionAccessor>
    extends AbstractContribution<T,V> implements TextualContribution {

  protected List<ActorRole> actors = null;
  protected Map<EActorRole, List<ActorRole>> actorsLimited = new HashMap<>();
  protected Map<EActorRole, Integer> nbActors = new HashMap<>();
  protected ActorAccessor actorAccessor;

  /**
   * Constructor
   *
   * @param factory       a ContributionFactory the factory to construct concrete instances
   * @param accessor      a ContributionAccessor the accessor to retrieve and persist concrete Contribution
   * @param actorAccessor an ActorAccessor to retrieve bound actors
   */
  public AbstractTextualContribution(T factory, V accessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    this.actorAccessor = actorAccessor;
  }

  @Override
  public void addActor(ActorRole role) throws FormatException {
    if (role == null || role.getActor() == null) {
      logger.error("given role or actor is null");
      throw new FormatException(FormatException.Key.ROLE_ERROR);
    }
    String check = String.join(", ", role.isValid());
    if (!factory.getValuesHelper().isBlank(check)) {
      logger.error("given actor " + role.getActor().getId() + " contains invalid fields " + check);
      throw new FormatException(FormatException.Key.ROLE_ERROR, check);
    }

    if (!getActors().contains(role)) {
      actors.add(role);
    }
  }

  @Override
  public List<Contribution> bindActor(ActorRole role, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    List<Contribution> results = new ArrayList<>();

    if (!getActors().contains(role)) {
      logger.debug("bind actor " + role.toString() + " to contribution " + getId());
      results.addAll(actorAccessor.bindActor(id, role, currentGroup, contributor));
    }
    return results;
  }

  @Override
  public void unbindActor(Long actor, Long contributor) throws PersistenceException {
    actorAccessor.unbindActor(id, actor, contributor);
  }

  @Override
  public void initActors() {
    actors = new ArrayList<>();
  }

  @Override
  public synchronized List<ActorRole> getActors() {
    if (actors == null) {
      actors = actorAccessor.getActors(getId());
    }
    return actors;
  }

  @Override
  public synchronized List<ActorRole> getActors(int limit, EActorRole role) {
    if(actorsLimited.get(role) == null) {
      actorsLimited.put(role, accessor.getActors(getId(), limit, role));
    }
    return actorsLimited.get(role);
  }

  @Override
  public int getNbActors(EActorRole role) {
    if(nbActors.get(role) == null) {
      nbActors.put(role, accessor.getNbActors(getId(), role));
    }
    return nbActors.get(role);
  }
}
