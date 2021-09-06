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


/**
 * The persistent class for text's titles spellings database table. Texts may have multiple spellings for their title (depending
 * on the language), so they are externalized in a dedicated table with the 2-char ISO code corresponding to the language.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "text_i18names")
public class TextI18name extends Model {

  @EmbeddedId
  private TextPK id;

  @ManyToOne
  @JoinColumn(name = "id_contribution", nullable = false)
  @Unqueryable
  private Text text;

  @Column(name = "spelling", nullable = false)
  private String spelling;


  /**
   * Default constructor. Create title holder for given text in given language.
   *
   * @param text an text
   * @param lang a 2-char ISO code of the title's language
   * @param spelling the String representing the title
   */
  public TextI18name(Text text, String lang, String spelling) {
    TextPK pk = new TextPK();
    pk.setIdContribution(text.getIdContribution());
    pk.setLang(lang);
    this.spelling = spelling;
    setId(pk);
    this.text = text;
  }

  /*
   * GETTERS AND SETTERS
   */

  /**
   * Get the complex id for this text title
   *
   * @return a complex id (concatenation of id_contribution and lang)
   */
  public TextPK getId() {
    return this.id;
  }

  /**
   * Set the complex id for this text title
   *
   * @param id a complex id object
   */
  public void setId(TextPK id) {
    this.id = id;
  }

  /**
   * Get the current language code for this title
   *
   * @return a two-char iso-639-1 language code
   */
  public String getLang() {
    return id.getLang();
  }

  /**
   * Set the current language code for this title
   *
   * @param lang a two-char iso-639-1 language code
   */
  public void setLang(String lang) {
    id.setLang(lang);
  }

  /**
   * Get the text title (if any)
   *
   * @return a title of the text
   */
  public String getSpelling() {
    return spelling;
  }

  /**
   * Set the text's title
   *
   * @param spelling the spelling of the text title
   */
  public void setSpelling(String spelling) {
    this.spelling = spelling;
  }

  /**
   * Get the text linked to this title
   *
   * @return an text
   */
  public Text getText() {
    return this.text;
  }

  /**
   * Set the text linked to this title
   *
   * @param text a text
   */
  public void setText(Text text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return getId() + " " + getSpelling();
  }

  /**
   * The primary key class for the text multiple titles database table.
   *
   * @author Martin Rouffiange
   */
  @Embeddable
  public static class TextPK extends Model {

    @Column(name = "id_contribution", insertable = false, updatable = false, unique = true, nullable = false)
    private Long idContribution;

    @Column(name = "lang", insertable = false, updatable = false, unique = true, nullable = false)
    private String lang;

    /**
     * Get the contribution (text) id
     *
     * @return an id
     */
    public Long getIdContribution() {
      return idContribution;
    }

    /**
     * Set the contribution (text) id
     *
     * @param idContribution an id of an existing text
     */
    public void setIdContribution(Long idContribution) {
      this.idContribution = idContribution;
    }

    /**
     * Get the language associated to this name (spelling)
     *
     * @return a two-char iso-639-1 language code
     */
    public String getLang() {
      return this.lang;
    }

    /**
     * Set the language associated to this name (spelling)
     *
     * @param lang a two-char iso-639-1 language code
     */
    public void setLang(String lang) {
      this.lang = lang;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof TextPK)) {
        return false;
      }
      TextPK castOther = (TextPK) other;
      return idContribution.equals(castOther.idContribution) && lang.equals(castOther.lang);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.idContribution.hashCode();
      return hash * prime + this.lang.hashCode();
    }
  }
}
