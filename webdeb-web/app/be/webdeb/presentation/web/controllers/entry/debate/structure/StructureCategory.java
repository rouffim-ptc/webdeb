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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds concrete values for the context arguments structure
 *
 * @author Martin Rouffiange
 */
public class StructureCategory extends Structure {

  private Map<EJustificationLinkShade, List<StructureArgument>> argumentsMap = new LinkedHashMap<>();
  private List<StructureArgument> arguments = new ArrayList<>();
  private List<StructureCitation> citations = new ArrayList<>();
  private Boolean isParent;

  public StructureCategory(Long id, Long linkId, String title, int order) {
    this(id, linkId, title, order, null);
  }

  public StructureCategory(Long id, Long linkId, String title, int order, Boolean isParent) {
    super(id, linkId, title, order);

    this.isParent = isParent;

    EJustificationLinkShade.valuesAsList().forEach(shade ->
      argumentsMap.put(shade, new ArrayList<>())
    );
  }

  public Map<EJustificationLinkShade, List<StructureArgument>> getArgumentsMap() {
    return argumentsMap;
  }

  public List<StructureArgument> getArguments() {
    return arguments;
  }

  public List<StructureCitation> getCitations() {
    return citations;
  }

  public Boolean getIsParent() {
    return isParent;
  }
}