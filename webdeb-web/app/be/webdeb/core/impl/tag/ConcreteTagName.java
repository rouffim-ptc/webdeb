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

import be.webdeb.core.api.tag.TagName;

/**
 * This class implements a Tag name.
 *
 * @author Martin Rouffiange
 */
class ConcreteTagName implements TagName {

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private String name;
  private String lang;

  ConcreteTagName(String lang, String name) {
    this.name = name;
    this.lang = lang;
  }

  @Override
  public String getName(){
    return name;
  }

  @Override
  public void setName(String name){
    this.name = name;
  }

  @Override
  public String getLang(){
    return lang;
  }

  @Override
  public void setLang(String lang){
    this.lang = lang;
  }
}