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

package be.webdeb.core.impl.tag;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagFactory;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.core.impl.contribution.link.AbstractContributionLink;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import java.util.*;

/**
 * This class implements a TagLink representing a link between folders.
 *
 * @author Martin Rouffiange
 */
class ConcreteTagLink extends AbstractContributionLink<TagFactory, TagAccessor> implements TagLink {

  private Tag parent = null;
  private Tag child = null;
  private Long parentId = null;
  private Long childId = null;

  ConcreteTagLink(TagFactory factory, TagAccessor accessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    type = EContributionType.TAG_LINK;
  }

  @Override
  public Tag getParent() {
    return parent;
  }

  @Override
  public Tag getChild() {
    return child;
  }

  @Override
  public void setParent(Tag tag) {
    if(tag != null){
      this.parent = tag;
    }
  }

  @Override
  public void setChild(Tag tag) {
    if(tag != null){
      this.child = tag;
    }
  }

  @Override
  public Long getParentId() {
    return parent != null ? parent.getId() : parentId;
  }

  @Override
  public void setParentId(Long tag) {
    parentId = tag;
  }

  @Override
  public Long getChildId() {
    return child != null ? child.getId() : childId;
  }

  @Override
  public void setChildId(Long tag) {
    childId = tag;
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
    List<String> errors = isValid();
    if (!errors.isEmpty()) {
      logger.error("tag link contains error " + errors.toString());
      throw new FormatException(FormatException.Key.TAG_LINK_ERROR, String.join(",", errors));
    }
    accessor.save(this, contributor);
    return new HashMap<>();
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();
    if(parentId == null)
      fieldsInError.addAll(partialValid(parent));
    if(childId == null)
      fieldsInError.addAll(partialValid(child));
    return fieldsInError;
  }


  @Override
  public String toString() {
    return " with parent " + (parent != null ? parent.getDefaultName() : "")
            + " child " + (child != null ? child.getDefaultName() : "");
  }

  /**
   * Validate a contribution by calling its isContributorValid method
   *
   * @param c a contribution
   * @return a (possibly empty) list of validation errors for given contribution
   */
  private List<String> partialValid(Contribution c) {
    List<String> isValid = new ArrayList<>();
    if (c == null) {
      isValid.add("contribution is null");
    } else {
      isValid = c.isValid();
      if (!isValid.isEmpty()) {
        isValid.add(String.join(",", isValid));
      }
    }
    return isValid;
  }
}