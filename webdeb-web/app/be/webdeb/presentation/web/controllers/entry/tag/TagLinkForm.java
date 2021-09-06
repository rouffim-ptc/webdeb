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

package be.webdeb.presentation.web.controllers.entry.tag;

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import javax.inject.Inject;

import be.webdeb.presentation.web.controllers.entry.link.BaseLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.api.Play;
import play.data.validation.ValidationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrap all fields of a tag link.
 *
 * @author Martin Rouffiange
 */
public class TagLinkForm extends BaseLinkForm {

  @Inject
  private TagFactory factory = Play.current().injector().instanceOf(TagFactory.class);

  /**
   * JSON - Play compliant constructor
   */
  public TagLinkForm() {
    super();
    type = EContributionType.TAG_LINK;
  }

  /**
   * Construct a link form with given tag link
   *
   * @param link a tag link
   * @param lang the language code (2-char ISO) of the user interface
   */
  public TagLinkForm(TagLink link, WebdebUser user, String lang) {
    super(link, user, lang);
  }

  /**
   * Construct a link form with given tag parent and child id
   *
   * @param parent a parent tag id
   * @param child a child tag id
   * @param lang the language code (2-char ISO) of the user interface
   */
  public TagLinkForm(Long parent, Long child, String lang) {
    super(parent, child, lang);
  }

  /**
   * Transform this form into an API form link
   *
   * @return an API link corresponding to this form link form
   */
  public TagLink toLink() {
    TagLink link = factory.getTagLink();
    link.setParent(factory.retrieve(originId));
    link.setChild(factory.retrieve(destinationId));
    link.addInGroup(inGroup);
    return link;
  }

}
