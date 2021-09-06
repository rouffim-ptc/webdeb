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

package be.webdeb.core.impl.text;

import be.webdeb.core.api.text.TextCopyrightfreeSource;

/**
 * Simple implementation of a TextCopyrightfreeSource
 *
 * @author Martin Rouffiange
 */
class ConcreteTextCopyrightfreeSource implements TextCopyrightfreeSource {

  private int id;
  private String domainName;

  ConcreteTextCopyrightfreeSource() {

  }

  /**
   * Create a TextCopyrightfreeSource instance
   *
   * @param id the identifier of the free source
   * @param domainName the domain name of the free source
   */
  ConcreteTextCopyrightfreeSource(int id, String domainName) {
    this.id = id;
    this.domainName = domainName;
  }

  @Override
  public int getSourceId() {
    return id;
  }

  @Override
  public String getDomainName() {
    return domainName;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  @Override
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  @Override
  public String toString(){
    return getSourceId() + ", " + getDomainName();
  }
}
