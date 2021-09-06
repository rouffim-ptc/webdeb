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

package be.webdeb.core.impl.tag;

import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import java.util.ArrayList;
import java.util.List;

public class ConcreteTagCategory extends ConcreteTag implements TagCategory {

  private Long currentContextId = null;
  private List<ContextContribution> contextContributions = null;
  private List<ArgumentJustification> argumentJustifications = null;
  private List<CitationJustification> citationJustifications = null;

  ConcreteTagCategory(TagFactory factory, TagAccessor accessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, accessor, actorAccessor, contributorFactory);
    type = EContributionType.TAG;
  }

  @Override
  public Long getCurrentContextId() {
    return currentContextId;
  }

  @Override
  public void setCurrentContextId(Long currentContextId) {
    this.currentContextId = currentContextId;
  }

  @Override
  public List<ContextContribution> getContextContributions() {
    if(contextContributions == null) {
      contextContributions =
              currentContextId == null ? new ArrayList<>() : accessor.getContextContributions(currentContextId);
    }
    return contextContributions;
  }

  @Override
  public List<ArgumentJustification> getAllArgumentJustificationLinks() {
    if (argumentJustifications == null) {

    }
    return argumentJustifications;
  }

  @Override
  public List<CitationJustification> getAllCitationJustificationLinks() {
    if (citationJustifications == null) {

    }
    return citationJustifications;
  }
}
