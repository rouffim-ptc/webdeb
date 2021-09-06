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

package be.webdeb.presentation.web.controllers.entry.tag;

import be.webdeb.core.api.argument.ArgumentFactory;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.EJustificationLinkShade;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.presentation.web.controllers.entry.argument.ArgumentJustificationLinkForm;
import be.webdeb.presentation.web.controllers.entry.citation.CitationHolder;
import be.webdeb.presentation.web.controllers.entry.citation.CitationJustificationLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagCategoryHolder extends TagHolder {

  private ContextHasCategoryLinkForm linkForm;

  private ContextContribution context;
  private Map<EJustificationLinkShade, List<ArgumentJustificationLinkForm>> argumentsMap = null;
  private Map<EJustificationLinkShade, List<CitationJustificationLinkForm>> citationsMap = null;

  /**
   * Constructor. Create a holder for a TagCategory (i.e. no type/data IDs, but their descriptions, as defined in
   * the database).
   *
   * @param category a TagCategory
   * @param context the context of the category
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagCategoryHolder(TagCategory category, ContextContribution context, ContextHasCategoryLinkForm linkForm, WebdebUser user, String lang) {
    this(category, context, linkForm, user, lang, false);
  }

  /**
   * Constructor. Create a holder for a TagCategory (i.e. no type/data IDs, but their descriptions, as defined in
   * the database).
   *
   * @param category a TagCategory
   * @param context the context of the category
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagCategoryHolder(TagCategory category, ContextContribution context, ContextHasCategoryLinkForm linkForm, WebdebUser user, String lang, boolean light) {
    super(category, user, lang, light);

    this.context = context;
    this.linkForm = linkForm;
  }


  public List<ArgumentJustificationLinkForm> getArguments(EJustificationLinkShade shade) {
    if(argumentsMap == null){
      argumentsMap = fromApiToArgumentJustificationLinkFormsMap(
              context.getArgumentJustificationLinks(id, null, null), context);
    }

    return argumentsMap.containsKey(shade) ? argumentsMap.get(shade) : new ArrayList<>();
  }

  public List<CitationJustificationLinkForm> getCitations(EJustificationLinkShade shade) {
    if(citationsMap == null){
      citationsMap = fromApiToCitationJustificationLinkFormsMap(
              context.getCitationJustificationLinks(id, null, null));
    }

    return citationsMap.containsKey(shade) ? citationsMap.get(shade) : new ArrayList<>();
  }

  public boolean hasContent(EJustificationLinkShade shade) {
    return !getArguments(shade).isEmpty() || getNbCitationsLinks(id, shade) != 0;
  }

  @JsonIgnore
  public ContextHasCategoryLinkForm getLinkForm() {
    return linkForm;
  }

}
