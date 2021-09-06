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

package be.webdeb.core.api.tag;

import be.webdeb.core.api.contribution.link.ContributionLink;

/**
 * This interface represents the links between a parent and a child tag.
 *
 * @author Martin Rouffiange
 */
public interface TagLink extends ContributionLink {

  /**
   * Get the parent tag of this link
   *
   * @return the tag being the parent of this TagLink
   */
  Tag getParent();

  /**
   * Set the parent Tag
   *
   * @param tag a Tag to be bound as the parent of this TagLink
   */
  void setParent(Tag tag);

  /**
   * Get the parent tag id of this link
   *
   * @return the tag id being the parent of this TagLink
   */
  Long getParentId();

  /**
   * Set the parent Tag id
   *
   * @param tag a tag id
   */
  void setParentId(Long tag);

  /**
   * Get the child tag of this link
   *
   * @return the tag being the child of this TagLink
   */
  Tag getChild();

  /**
   * Set the child Tag
   *
   * @param tag a Tag to be bound as the child of this TagLink
   */
  void setChild(Tag tag);

  /**
   * Get the child tag id of this link
   *
   * @return the tag id being the child of this TagLink
   */
  Long getChildId();

  /**
   * Set the child Tag id
   *
   * @param tag a tag id
   */
  void setChildId(Long tag);

}
