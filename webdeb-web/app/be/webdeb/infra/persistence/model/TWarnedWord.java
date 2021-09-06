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

package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the t_warned_word database table, holding predefined values for warned words in different case.
 *
 * @author Martin Rouffiange
 */
@Entity
@Table(name = "t_warned_word")
@CacheBeanTuning
@Unqueryable
public class TWarnedWord {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Integer, TWarnedWord> find = new Model.Finder<>(TWarnedWord.class);
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_warned_word", unique = true, nullable = false)
  private int idWarnedWord;

  @ManyToOne(targetEntity = TWarnedWordType.class, fetch = FetchType.EAGER)
  @JoinColumn(name = "id_type", nullable = false)
  private TWarnedWordType type;

  @ManyToOne(targetEntity = TWarnedWordContextType.class, fetch = FetchType.EAGER)
  @JoinColumn(name = "id_context_type", nullable = false)
  private TWarnedWordContextType contextType;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_language", nullable = false)
  private TLanguage language;

  @Column(name = "title", length = 32, nullable = false)
  private String title;

  /**
   * Get this banned word id
   *
   * @return an id
   */
  public int getIdWarnedWord() {
    return idWarnedWord;
  }

  /**
   * Set this banned word id
   *
   * @param idWarnedWord an id
   */
  public void setIdWarnedWord(int idWarnedWord) {
    this.idWarnedWord = idWarnedWord;
  }

  /**
   * Get the word type (begin of sentence, ...)
   *
   * @return the warned word type
   */
  public TWarnedWordType getType() {
    return type;
  }

  /**
   * Set the word type (begin of sentence, ...)
   *
   * @param type the warned word type
   */
  public void setType(TWarnedWordType type) {
    this.type = type;
  }

  /**
   * Get the word type context. It means where the word need a warning
   *
   * @return the context type
   */
  public TWarnedWordContextType getContextType() {
    return contextType;
  }

  /**
   * Set the word type context. It means where the word need a warning
   *
   * @param contextType the context type
   */
  public void setContextType(TWarnedWordContextType contextType) {
    this.contextType = contextType;
  }

  /**
   * Get the language of the title of this warned word
   *
   * @return the warned word language
   */
  public TLanguage getLanguage() {
    return language;
  }

  /**
   * Set the language of the title of this warned word
   *
   * @param language a language
   */
  public void setLanguage(TLanguage language) {
    this.language = language;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public static List<TWarnedWord> findByTypesAndLang(int type, int contextType, String lang){
    return find.where()
            .eq("id_type", type)
            .eq("id_context_type", contextType)
            .eq("id_language", lang)
            .findList();
  }

}
