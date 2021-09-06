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
 * The persistent class for the profession spellings database table. Professions have multiple spellings, depending
 * on a 2-char ISO code corresponding to the language and a char code corresponding to the word gender.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name="profession_i18names")
public class ProfessionI18name extends Model {

  @EmbeddedId
  private ProfessionPK id;

  @Column(name = "spelling")
  private String spelling;

  @ManyToOne
  @JoinColumn(name = "gender", nullable = false)
  private TWordGender gender;

  @ManyToOne
  @JoinColumn(name = "profession", nullable = false)
  @Unqueryable
  private Profession profession;

  /**
   * Create a new spelling for given profession
   *
   * @param id a profession id
   * @param lang a 2-char ISO 639 language code
   * @param gender a char word gender code
   * @param spelling a profession spelling (language dependent)
   */
  public ProfessionI18name(int id, String lang, String gender, String spelling) {
    this.id = new ProfessionI18name.ProfessionPK(id, lang, gender);
    this.spelling = spelling;
  }

  /**
   * Get the complex id for this profession spelling
   *
   * @return a complex id (concatenation of id_profession, lang and gender)
   */
  public ProfessionPK getId() {
    return id;
  }

  /**
   * Set the complex id for this profession spelling
   *
   * @param id a complex id (concatenation of id_profession, lang and gender)
   */
  public void setId(ProfessionPK id) {
    this.id = id;
  }

  /**
   * Get the current language code for this name
   *
   * @return a two-char iso-639-1 language code
   */
  public String getLang() {
    return id.getLang();
  }

  /**
   * Set the current language code for this name
   *
   * @param lang a two-char iso-639-1 language code
   */
  public void setLang(String lang) {
    id.setLang(lang);
  }

  /**
   * Get the word gender associated to this name (spelling)
   *
   * @return a char gender code
   */
  public String getGender() {
    return id.getGender();
  }

  /**
   * Set the word gender associated to this name (spelling)
   *
   * @param gender a char gender cod
   */
  public void setGender(String gender) {
    id.setGender(gender);
  }

  /**
   * Get the profession spelling
   *
   * @return a profession spelling in associated lang
   */
  public String getSpelling() {
    return spelling;
  }

  /**
   * Set the profession spelling
   *
   * @param spelling a profession spelling name
   */
  public void setSpelling(String spelling) {
    this.spelling = spelling != null ? spelling.toLowerCase() : null;
  }

  /**
   * Get the profession associated to this spelling
   *
   * @return a profession
   */
  public Profession getProfession() {
    return profession;
  }

  /**
   * Set the profession associated to this spelling
   *
   * @param profession a profession
   */
  public void setProfession(Profession profession) {
    this.profession = profession;
  }

  /**
   * Get the unique code (union of lang and gender)
   *
   * @return a 3-char code
   */
  public String getCode() {
    return getLang()+getGender();

  }

  @Override
  public String toString() {
    return getId() + " " + getSpelling();
  }

  /**
   * The primary key class for the profession multiple name database table.
   *
   * @author Fabian Gilson
   * @author Martin Rouffiange
   */
  @Embeddable
  public static class ProfessionPK extends Model {

    @Column(name = "profession", insertable = false, updatable = false, unique = true, nullable = false)
    private Integer profession;

    @Column(name = "lang", insertable = false, updatable = false, unique = true, nullable = false)
    private String lang;

    @Column(name = "gender", insertable = false, updatable = false, unique = true, nullable = false)
    private String gender;

    /**
     * Default no-arg constructor
     */
    public ProfessionPK() {
      // needed by ebean
    }

    /**
     * Create a complex key for given profession, language and gender
     *
     * @param id a profession id
     * @param lang a 2-char iso 639 language code
     * @param gender a char gender code
     */
    public ProfessionPK(int id, String lang, String gender) {
      profession = id;
      this.lang = lang;
      this.gender = gender;
    }

    /**
     * Get the profession id
     *
     * @return an id
     */
    public Integer getIdProfession() {
      return profession;
    }

    /**
     * Set the profession id
     *
     * @param profession an id of a profession
     */
    public void setProfession(Integer profession) {
      this.profession = profession;
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

    /**
     * Get the word gender associated to this name (spelling)
     *
     * @return a char gender code
     */
    public String getGender() {
      return this.gender;
    }

    /**
     * Set the word gender associated to this name (spelling)
     *
     * @param gender a char gender cod
     */
    public void setGender(String gender) {
      this.gender = gender;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ProfessionPK)) {
        return false;
      }
      ProfessionPK castOther = (ProfessionPK) other;
      return this.profession.equals(castOther.profession)
              && this.lang.equals(castOther.lang)
              && this.gender.equals(castOther.gender);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.profession.hashCode();
      hash = hash * prime + this.lang.hashCode();
      return hash * prime + this.gender.hashCode();
    }

    @Override
    public String toString() {
      return "[" + profession + ":" + lang + "-" + gender + "]";
    }
  }
}
