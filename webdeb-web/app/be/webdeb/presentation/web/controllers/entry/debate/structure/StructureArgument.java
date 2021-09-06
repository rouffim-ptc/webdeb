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

package be.webdeb.presentation.web.controllers.entry.debate.structure;

import be.webdeb.core.api.contribution.link.EJustificationLinkShade;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds concrete values for the context arguments structure
 *
 * @author Martin Rouffiange
 */
public class StructureArgument extends Structure {

  private Integer shade;
  private List<StructureArgument> arguments = new ArrayList<>();
  private List<StructureCitation> citations = new ArrayList<>();

  public StructureArgument(Long id, Long linkId, String title, int order) {
    super(id, linkId, title, order);
  }

  public StructureArgument(Long id, Long linkId, String title, int order, EJustificationLinkShade linkShade) {
    super(id, linkId, title, order);
    this.shade = linkShade.id();
  }

  public Integer getShade() {
    return shade;
  }

  public List<StructureArgument> getArguments() {
    return arguments;
  }

  public List<StructureCitation> getCitations() {
    return citations;
  }
}
