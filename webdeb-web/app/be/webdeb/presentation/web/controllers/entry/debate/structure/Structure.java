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

/**
 * This class holds concrete values for the context arguments structure
 *
 * @author Martin Rouffiange
 */
public abstract class Structure {

  private Long id;
  private Long linkId;
  private String title;
  private int order;

  public Structure(Long id, Long linkId, String title, int order){
    this.id = id;
    this.linkId = linkId;
    this.title = title;
    this.order = order;
  }

  public Long getId() {
    return id;
  }

  public Long getLinkId() {
    return linkId;
  }

  public String getTitle() {
    return title;
  }

  public int getOrder() {
    return order;
  }
}
