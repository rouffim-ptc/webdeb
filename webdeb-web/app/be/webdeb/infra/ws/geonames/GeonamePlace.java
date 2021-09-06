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

package be.webdeb.infra.ws.geonames;

import be.webdeb.presentation.web.controllers.entry.PlaceNameForm;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

/**
 * Handle response of Geonames request
 *
 * @author Martin Rouffiange
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeonamePlace {

  /**
   * The geoname id of the place
   */
  @JsonDeserialize
  private Long geonameId;

  /**
   * The toponym name of the place
   */
  @JsonDeserialize
  private String toponymName;

  /**
   * The name of the place
   */
  @JsonDeserialize
  private List<GeonamePlaceName> alternateNames;

  /**
   * The latitude of the place
   */
  @JsonDeserialize
  private String lat;

  /**
   * The longitude of the place
   */
  @JsonDeserialize
  private String lng;

  /**
   * This code represents the type of place (region, country, ...)
   */
  @JsonDeserialize
  private String fcode;

  /**
   * The continent code where the place is (ex : EU for Europe)
   */
  @JsonDeserialize
  private String continentCode;

  /**
   * The country code where the place is (ex : BE for Europe)
   */
  @JsonDeserialize
  private String countryCode;

  /**
   * The country geoname id of the place
   */
  @JsonDeserialize
  private Long countryId;

  /**
   * The id of the admin1 zone
   */
  @JsonDeserialize
  private Long adminId1;

  /**
   * A code representing a region of a country where the place is (ex : WAL for Wallonia)
   */
  @JsonDeserialize
  private String adminCode1;

  /**
   * The name of the region where the city is
   */
  @JsonDeserialize
  private String adminName1;

  /**
   * The id of the admin2 zone
   */
  @JsonDeserialize
  private Long adminId2;

  /**
   * A code representing a sub region (ex : WNA for province de Namur)
   */
  @JsonDeserialize
  private String adminCode2;

  /**
   * The sub region name
   */
  @JsonDeserialize
  private String adminName2;

  /**
   * The id of the admin3 zone
   */
  @JsonDeserialize
  private Long adminId3;

  /**
   * Another sub region code (ex : 92 for Namur borough)
   */
  @JsonDeserialize
  private String adminCode3;

  /**
   * The another sub region name
   */
  @JsonDeserialize
  private String adminName3;

  /**
   * The id of the admin4 zone
   */
  @JsonDeserialize
  private Long adminId4;

  /**
   * City, town, others kind name
   */
  @JsonDeserialize
  private String adminName4;

  /**
   * Other place name
   */
  @JsonDeserialize
  private String asciiName;

  /**
   * Other place name
   */
  @JsonDeserialize
  private String name;

  /**
   * Default constructor, needed by jackson
   */
  public GeonamePlace() {
    // empty for jackson
  }

  public Long getGeonameId() {
    return geonameId;
  }

  public void setGeonameId(Long geonameId) {
    this.geonameId = geonameId;
  }

  public String getToponymName() {
    return toponymName;
  }

  public void setToponymName(String toponymName) {
    this.toponymName = toponymName;
  }

  public List<PlaceNameForm> getNamesToPlaceNameForm() {
    List<PlaceNameForm> tmp = new ArrayList<>();
    Map<String, String> namesMap = new HashMap<>();
    if(alternateNames != null) alternateNames.forEach(n -> namesMap.put(n.lang, n.name));

    String defaultPlaceName = (toponymName != null && toponymName.length() > 0 ? toponymName : asciiName);
    tmp.add(cretatePlaceNameForm("fr", namesMap, defaultPlaceName));
    tmp.add(cretatePlaceNameForm("en", namesMap, defaultPlaceName));
    tmp.add(cretatePlaceNameForm("nl", namesMap, defaultPlaceName));
    tmp.add(cretatePlaceNameForm("de", namesMap, defaultPlaceName));
    return tmp;
  }

  /**
   * Create placeNameForm for naming the place in given language.
   *
   * @param lang the lang of the name
   * @param namesMap the map of names (for keep lang unique)
   * @param defaultPlaceName the default place
   * @return the PlaceNameForm for given language
   */
  private PlaceNameForm cretatePlaceNameForm(String lang, Map<String, String> namesMap, String defaultPlaceName){
    String placename = null;
    if(alternateNames != null) {
      Optional<GeonamePlaceName> preferred =
          alternateNames.stream().filter(n -> n.isPreferredName != null && n.isPreferredName && lang.equals(n.lang)).findFirst();
      if(preferred.isPresent()) placename = preferred.get().name;
    }
    if(placename == null) placename = namesMap.getOrDefault(lang, defaultPlaceName);
    return new PlaceNameForm(lang, placename);
  }

  public List<GeonamePlaceName> getNames() {
    return (alternateNames != null ? alternateNames : new ArrayList<>());
  }

  public void setNames(List<GeonamePlaceName> names) {
    this.alternateNames = names;
  }

  public String getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public String getLng() {
    return lng;
  }

  public void setLng(String lng) {
    this.lng = lng;
  }

  public String getFcode() {
    return fcode;
  }

  public void setFcode(String fcode) {
    this.fcode = fcode;
  }

  public String getContinentCode() {
    return continentCode;
  }

  public void setContinentCode(String continentCode) {
    this.continentCode = continentCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public Long getCountryId() {
    return countryId;
  }

  public void setCountryId(Long countryId) {
    this.countryId = countryId;
  }

  public Long getAdminId1() {
    return adminId1;
  }

  public void setAdminId1(Long adminId1) {
    this.adminId1 = adminId1;
  }

  public String getAdminCode1() {
    return adminCode1;
  }

  public void setAdminCode1(String adminCode1) {
    this.adminCode1 = adminCode1;
  }

  public String getAdminName1() {
    return adminName1;
  }

  public void setAdminName1(String adminName1) {
    this.adminName1 = adminName1;
  }

  public Long getAdminId2() {
    return adminId2;
  }

  public void setAdminId2(Long adminId2) {
    this.adminId2 = adminId2;
  }

  public String getAdminCode2() {
    return adminCode2;
  }

  public void setAdminCode2(String adminCode2) {
    this.adminCode2 = adminCode2;
  }

  public String getAdminName2() {
    return adminName2;
  }

  public void setAdminName2(String adminName2) {
    this.adminName2 = adminName2;
  }

  public Long getAdminId3() {
    return adminId3;
  }

  public void setAdminId3(Long adminId3) {
    this.adminId3 = adminId3;
  }

  public String getAdminCode3() {
    return adminCode3;
  }

  public void setAdminCode3(String adminCode3) {
    this.adminCode3 = adminCode3;
  }

  public String getAdminName3() {
    return adminName3;
  }

  public void setAdminName3(String adminName3) {
    this.adminName3 = adminName3;
  }

  public Long getAdminId4() {
    return adminId4;
  }

  public void setAdminId4(Long adminId4) {
    this.adminId4 = adminId4;
  }

  public String getAdminName4() {
    return adminName4;
  }

  public void setAdminName4(String adminName4) {
    this.adminName4 = adminName4;
  }

  public String getAsciiName() {
    return asciiName;
  }

  public void setAsciiName(String asciiName) {
    this.asciiName = asciiName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "GeonamePlace{" +
        "geonameId=" + geonameId +
        ", toponymName='" + toponymName + '\'' +
        ", lat='" + lat + '\'' +
        ", lng='" + lng + '\'' +
        ", fcode='" + fcode + '\'' +
        ", continentCode='" + continentCode + '\'' +
        ", countryCode='" + countryCode + '\'' +
        ", countryId=" + countryId +
        ", adminId1=" + adminId1 +
        ", adminCode1='" + adminCode1 + '\'' +
        ", adminName1='" + adminName1 + '\'' +
        ", adminId2=" + adminId2 +
        ", adminCode2='" + adminCode2 + '\'' +
        ", adminName2='" + adminName2 + '\'' +
        ", adminId3=" + adminId3 +
        ", adminCode3='" + adminCode3 + '\'' +
        ", adminName3='" + adminName3 + '\'' +
        ", adminId4=" + adminId4 +
        ", adminName4='" + adminName4 + '\'' +
        '}';
  }

  /**
   * Inner class for Geonames name
   *
   * @author Martin Rouffiange
   */
  private static class GeonamePlaceName {

    /**
     * True if the name is preferred
     */
    @JsonDeserialize
    private Boolean isPreferredName;

    /**
     * The name of the place
     */
    @JsonDeserialize
    private String name;

    /**
     * The lang of the name
     */
    @JsonDeserialize
    private String lang;
  }
}