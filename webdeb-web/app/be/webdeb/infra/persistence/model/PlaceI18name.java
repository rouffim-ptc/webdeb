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
 * The persistent class for the place spellings database table. Places have multiple spellings, depending
 * on a 2-char ISO code corresponding to the language.
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name="place_i18names")
public class PlaceI18name extends Model {

  @EmbeddedId
  private PlacePK id;

  @Column(name = "name")
  private String spelling;

  @ManyToOne
  @JoinColumn(name = "id_place", nullable = false)
  @Unqueryable
  private Place place;

  /**
   * Create a new spelling for given place
   *
   * @param id a place id
   * @param lang a 2-char ISO 639 language code
   * @param spelling a place spelling (language dependent)
   */
  public PlaceI18name(long id, String lang, String spelling) {
    this.id = new PlaceI18name.PlacePK(id, lang);
    this.spelling = spelling;
  }

  /**
   * Get the complex id for this place spelling
   *
   * @return a complex id (concatenation of id_place and lang)
   */
  public PlacePK getId() {
    return id;
  }

  /**
   * Set the complex id for this place spelling
   *
   * @param id a complex id (concatenation of id_place and lang)
   */
  public void setId(PlacePK id) {
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
   * Get the place spelling
   *
   * @return a place spelling in associated lang
   */
  public String getSpelling() {
    return spelling;
  }

  /**
   * Set the place spelling
   *
   * @param spelling a place spelling name
   */
  public void setSpelling(String spelling) {
    this.spelling = spelling;
  }

  /**
   * Get the place associated to this spelling
   *
   * @return a place
   */
  public Place getPlace() {
    return place;
  }

  /**
   * Set the place associated to this spelling
   *
   * @param place a place
   */
  public void setPlace(Place place) {
    this.place = place;
  }

  @Override
  public String toString() {
    return getId() + " " + getSpelling();
  }

  /**
   * The primary key class for the place multiple name database table.
   *
   * @author Martin Rouffiange
   */
  @Embeddable
  public static class PlacePK extends Model {

    @Column(name = "id_place", insertable = false, updatable = false, unique = true, nullable = false)
    private Long place;

    @Column(name = "lang", insertable = false, updatable = false, unique = true, nullable = false)
    private String lang;

    /**
     * Default no-arg constructor
     */
    public PlacePK() {
      // needed by ebean
    }

    /**
     * Create a complex key for given place and language
     *
     * @param id a place id
     * @param lang a 2-char iso 639 language code
     */
    public PlacePK(long id, String lang) {
      place = id;
      this.lang = lang;
    }

    /**
     * Get the place id
     *
     * @return an id
     */
    public Long getIdPlace() {
      return place;
    }

    /**
     * Set the place id
     *
     * @param place an id of a place
     */
    public void setPlace(Long place) {
      this.place = place;
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
      if (!(other instanceof PlacePK)) {
        return false;
      }
      PlacePK castOther = (PlacePK) other;
      return this.place.equals(castOther.place)
          && this.lang.equals(castOther.lang);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.place.hashCode();
      return hash * prime + this.lang.hashCode();
    }

    @Override
    public String toString() {
      return "[" + place + ":" + lang + "]";
    }
  }
}
