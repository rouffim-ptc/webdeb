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

import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.place.EPlaceType;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.*;


/**
 * The persistent class for the t_place database table, holding earth places for helping on filter
 *
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "place")
public class Place extends WebdebModel {

  /**
   * Finder to access predefined values
   */
  public static final Model.Finder<Long, Place> find = new Model.Finder<>(Place.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_place", unique = true, nullable = false)
  private Long idPlace;

  @Column(name = "geoname_id", unique = true)
  private Long geonameId;

  @Column(name = "code", unique = true, length = 3)
  private String code;

  @Column(name = "latitude", length = 25)
  private String latitude;

  @Column(name = "longitude", length = 25)
  private String longitude;

  @OneToMany(mappedBy = "place", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<PlaceI18name> spellings;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "placetype", nullable = false)
  private TPlaceType placeType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_continent")
  private Place continent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_country")
  private Place country;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_region")
  private Place region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_subregion")
  private Place subregion;

  @ManyToMany(mappedBy = "places", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Contribution> contributions;

  /**
   * Default constructor. Initialize id to 0.
   */
  public Place() {
    // initialize id to 0 (for auto increment)
    idPlace = 0L;
  }

  /**
   * Other constructor
   */
  public Place(Long geonameId, String code, String latitude, String longitude) {
    // initialize id to 0 (for auto increment)
    this();
    this.geonameId = geonameId;
    this.code = code;
    this.latitude = latitude;
    this.longitude = longitude;
  }
  /**
   * Get the place id
   *
   * @return the place id
   */
  public Long getId() {
    return idPlace;
  }

  /**
   * Get the geoname id of this place (from geonames.org api)
   *
   * @return the geoname id
   */
  public Long getGeonameId() {
    return geonameId;
  }

  /**
   * Set the geoname id of the place (from geonames.org api)
   *
   * @param geonameId the longitude
   */
  public void setGeonameId(Long geonameId) {
    this.geonameId = geonameId;
  }

  /**
   * Get the 3-char ISO 3166 code of the place
   *
   * @return the 3-char ISO 3166 code
   */
  public String getCode() {
    return code;
  }

  /**
   * Set the 3-char ISO 3166 code of the place
   *
   * @param code the 3-char ISO 3166 code
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Get the latitude of the place
   *
   * @return the latitude
   */
  public String getLatitude() {
    return latitude;
  }

  /**
   * Set the latitude of the place
   *
   * @param latitude the latitude
   */
  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  /**
   * Get the longitude of the place
   *
   * @return the longitude
   */
  public String getLongitude() {
    return longitude;
  }

  /**
   * Set the longitude of the place
   *
   * @param longitude the longitude
   */
  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  /**
   * Get the contributions (joint objects) linked to the place
   *
   * @return a possibly empty list of Contribution
   */
  public List<Contribution> getContributions() {
    return contributions != null ? contributions : new ArrayList<>();
  }

  /**
   * Get all spellings for this place
   *
   * @return a list of spellings as PlaceI18names
   */
  public List<PlaceI18name> getSpellings() {
    return spellings;
  }

  /**
   * Set all spellings for this place
   *
   * @param placeId the id of the place
   * @param spellings a list of spellings
   */
  public void setSpellings(Long placeId, Map<String, String> spellings) {
    if (spellings != null && !spellings.isEmpty()) {
      if (this.spellings == null) {
        this.spellings = new ArrayList<>();
      }
      List<PlaceI18name> names = new ArrayList<>();
      String [] langs = {"fr", "en", "de", "nl"};
      String defaultName = "";
      if(spellings.containsKey("default")){
        defaultName = spellings.get("default");
      }else{
        Optional<Map.Entry<String, String>> firstMatchedName = spellings.entrySet().stream().findFirst();
        defaultName = (firstMatchedName.isPresent() ? firstMatchedName.get().getValue() : "");
      }

      for(String lang : langs){
        String name = spellings.getOrDefault(lang, defaultName);
        names.add(new PlaceI18name(placeId, lang, name));
      }

      this.spellings = names;
    }
  }

  /**
   * Get the type of the place
   *
   * @return the type of the place
   */
  public TPlaceType getPlaceType() {
    return placeType;
  }

  /**
   * Set the type of the place
   *
   * @param placeType the type of the place
   */
  public void setPlaceType(TPlaceType placeType) {
    this.placeType = placeType;
  }

  /**
   * Get the continent of the place
   *
   * @return the continent of the place
   */
  public Place getContinent() {
    return continent;
  }

  /**
   * Set the continent of the place
   *
   * @param continent the continent of the place
   */
  public void setContinent(Place continent) {
    this.continent = continent;
  }

  /**
   * Get the country of the place
   *
   * @return the country of the place
   */
  public Place getCountry() {
    return country;
  }

  /**
   * Set the of the place
   *
   * @param country the country of the place
   */
  public void setCountry(Place country) {
    this.country = country;
  }

  /**
   * Get the region of the place
   *
   * @return the region of the place
   */
  public Place getRegion() {
    return region;
  }

  /**
   * Set the of the place
   *
   * @param region the region of the place
   */
  public void setRegion(Place region) {
    this.region = region;
  }

  /**
   * Get the sub-region of the place
   *
   * @return the sub-region of the place
   */
  public Place getSubregion() {
    return subregion;
  }

  /**
   * Set the sub-region of the place
   *
   * @param subregion the sub-region of the place
   */
  public void setSubregion(Place subregion) {
    this.subregion = subregion;
  }

  /**
   * Map the spellings to the place
   *
   * @return the Map of place names
   */
  public Map<String, String> mapSpellings(){
    Map<String, String> names = new HashMap<>();
    for(PlaceI18name name : spellings){
      names.put(name.getLang(), name.getSpelling());
    }
    return names;
  }

  /**
   * Find continent by code
   *
   * @param code the 3-char ISO 3166 code
   * @return the matched place
   */
  public static Place findContinentByCode(String code){
    TPlaceType placeType = TPlaceType.find.byId(EPlaceType.CONTINENT.id());
    return find.where().eq("placeType", placeType).eq("code", code).findUnique();
  }

  /**
   * Find place by geoname id or place id
   *
   * @param geonameId the geoname id
   * @param placeId the place id
   * @return the matched place
   */
  public static Place findByGeonameIdorPlaceId(Long geonameId, Long placeId){
    if(placeId == null || placeId < 0){
      List<Place> places = find.where().eq("geoname_id", geonameId).findList();
      return places != null && !places.isEmpty() ? places.get(0) : null;
    }
    return find.where().eq("id_place", placeId).findUnique();
  }

  /**
   * Find a list of places by a partial title
   *
   * @param name a partial place name
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return the list of Place matching the given partial name
   */
  public static List<Place> findByPartialTitle(String name, int fromIndex, int toIndex) {
    List<Place> result = null;
    if (name != null) {
      String select = "select distinct place.id_place from place right join place_i18names " +
              "on place.id_place = place_i18names.id_place where name like '%" + getSearchToken(name) + "%' " +
              getSearchLimit(fromIndex, toIndex);

      logger.debug("search for place : " + select);
      result = Ebean.find(Place.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    }
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Get a randomly chosen place id from the database
   *
   * @return a random place
   */
  public static Place random() {
    String sql = "select id_place from place order by rand() limit 1";
    return Ebean.find(Place.class).setRawSql(RawSqlBuilder.parse(sql).create()).findUnique();
  }
}
