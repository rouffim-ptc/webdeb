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

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.EWarnedWordContextType;
import be.webdeb.core.api.contribution.EWarnedWordType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contribution.WarnedWord;

import java.util.Map;

/**
 * This class implements a warned word
 *
 * @author Martin Rouffiange
 */
public class ConcreteWarnedWord implements WarnedWord {

  private int id;
  private String title;
  private Language language;
  private EWarnedWordType etype;
  private EWarnedWordContextType eContextType;

  /**
   * Constructor
   *
   * @param idBannedWord a word banned id
   * @param title the title of the warned word
   * @param language the language of the warned word
   * @param type a int representing the type of the word (begin word, ...)
   * @param contextType a int representing the warned word type
   * @see be.webdeb.core.api.contribution.EWarnedWordType
   */
  public ConcreteWarnedWord(int idBannedWord, String title, Language language, int type, int contextType) {
    this.id = idBannedWord;
    this.title = title;
    this.language = language;
    etype = EWarnedWordType.value(type);
    eContextType = EWarnedWordContextType.value(contextType);
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public Language getLanguage() {
    return language;
  }

  @Override
  public EWarnedWordType getEType() {
    return etype;
  }

  @Override
  public EWarnedWordContextType getEContextType() {
    return eContextType;
  }

}
