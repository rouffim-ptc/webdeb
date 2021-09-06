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
package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.contribution.place.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Place representation
 *
 * @author Martin Rouffiange
 */
public class PlaceForm {

  protected Long id = -1L;
  protected Long geonameId;
  protected String code;
  protected String latitude = "";
  protected String longitude = "";
  protected List<PlaceNameForm> names = new ArrayList<>();
  protected Integer placeType;
  protected PlaceForm continent;
  protected PlaceForm country;
  protected PlaceForm region;
  protected PlaceForm subregion;
  protected String name;
  protected String completeName;

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Play / JSON compliant constructor
   */
  public PlaceForm() {
    // needed by Play
  }

  /**
   * Simple construct a simple place form
   *
   * @param placeId the place id
   */
  public PlaceForm(Long placeId) {
    this.id = placeId;
  }

  /**
   * Construct a simple place form
   *
   * @param place the place
   * @param lang the user lang
   */
  public PlaceForm(Place place, String lang) {
    if (place != null) {
      this.id = place.getId();
      this.geonameId = place.getGeonameId();
      this.code = place.getCode();
      this.latitude = place.getLatitude();
      this.longitude = place.getLongitude();
      if (place.getPlaceType() != null) this.placeType = place.getPlaceType().getType();
      if (place.getContinent() != null) this.continent = new PlaceForm(place.getContinent(), lang);
      if (place.getCountry() != null) this.country = new PlaceForm(place.getCountry(), lang);
      if (place.getRegion() != null) this.region = new PlaceForm(place.getRegion(), lang);
      if (place.getSubregion() != null) this.subregion = new PlaceForm(place.getSubregion(), lang);
      this.name = place.getName(lang);
      this.completeName = place.getCompletePlacename(lang);
    }
  }


  /**
   * Construct place form from complete data
   *
   * @param geonameId the geonameId of the place
   * @param latitude the latitude of the place
   * @param longitude the longitude of the place
   * @param placeType the placeType of the place
   */
  public PlaceForm(Long geonameId, String latitude, String longitude, Integer placeType) {
      this.geonameId = geonameId;
      this.latitude = latitude;
      this.longitude = longitude;
      this.placeType = placeType;
  }

  @Override
  public String toString() {
    return id + " " + completeName + " (" + name + " " + code + ", geoname id : " + geonameId + ", latitude : " + latitude + ", longitude : " + longitude + ", type : " + placeType + ")";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PlaceForm && obj.hashCode() == hashCode();
  }

  @Override
  public int hashCode() {
    return 83
        + (latitude != null ? latitude.hashCode() : 0)
        + (longitude != null ? longitude.hashCode() : 0)
        + (geonameId != null ? geonameId.hashCode() : 0)
        + (code!= null ? code.hashCode() : 0);
  }

  /**
   * Helper method to check whether this place is empty (contains empty or null values)
   *
   * @return true if all fields are empty
   */
  public boolean isEmpty() {
    return ((geonameId == null || geonameId == -1L)
            && (id == null || id == -1L));
  }

  /*
   * GETTERS / SETTERS
   */

  /**
   * Get the place composition : contient -> country -> region -> subregion -> place
   *
   * @return a list of PlaceForm of the composition
   */
  public List<String> getPlaceComposition(){
    List<String> composition = new ArrayList<>();

    if(continent != null)
      composition.add(continent.getName());
    if(country != null)
      composition.add(country.getName());
    if(region != null)
      composition.add(region.getName());
    if(subregion != null)
      composition.add(subregion.getName());
    composition.add(name);

    return composition;
  }

  public Long getId() {
    return (id != null ? id : -1L);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getGeonameId() {
    return geonameId;
  }

  public void setGeonameId(Long geonameId) {
    this.geonameId = geonameId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPlaceType() {
    return (placeType != null ? placeType : -1);
  }

  public void setPlaceType(Integer placeType) {
    this.placeType = placeType;
  }

  public PlaceForm getContinent() {
    return continent;
  }

  public void setContinent(PlaceForm continent) {
    this.continent = continent;
  }

  public PlaceForm getCountry() {
    return country;
  }

  public void setCountry(PlaceForm country) {
    this.country = country;

    if(country != null) {
      this.setContinent(country.getContinent());
    }
  }

  public PlaceForm getRegion() {
    return region;
  }

  public void setRegion(PlaceForm region) {
    this.region = region;

    if(region != null) {
      this.setCountry(region.getCountry());
      this.setContinent(region.getContinent());
    }
  }

  public PlaceForm getSubregion() {
    return subregion;
  }

  public void setSubregion(PlaceForm subregion) {
    this.subregion = subregion;

    if(subregion != null) {
      this.setRegion(subregion.getRegion());
    }
  }

  public List<PlaceNameForm> getNames() {
    return names;
  }

  public void setNames(List<PlaceNameForm> names) {
    this.names = names;
  }

  public String getCompleteName() {
    return completeName;
  }

  public void setCompleteName(String completeName) {
    this.completeName = completeName;
  }
}
