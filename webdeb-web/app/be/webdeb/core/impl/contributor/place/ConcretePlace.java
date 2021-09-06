/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

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

package be.webdeb.core.impl.contributor.place;

import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contribution.place.PlaceType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple implementation of a Place
 *
 * @author Martin Rouffiange
 */
public class ConcretePlace implements Place {

  private Long id;
  private Long geonameId;
  private String code;
  private String latitude;
  private String longitude;
  // map of (lang, spellings)
  private Map<String, String> i18names = new HashMap<>();
  private PlaceType placeType;
  private Place continent;
  private Place country;
  private Place region;
  private Place subregion;

  public ConcretePlace() { }

  /**
   * Create a simple Place instance
   *
   * @param id the id of the place
   */
  public ConcretePlace(Long id) {
    this.id = id;
  }

  /**
   * Create a Place instance
   *
   * @param id the id of the place
   * @param geonameId the geoname id of the place, can be null
   * @param code the code of the place, can be null
   * @param latitude the latitude of the place
   * @param longitude the longitude of the place
   * @param i18names the names of the place
   */
  public ConcretePlace(Long id, Long geonameId, String code, String latitude, String longitude, Map<String, String> i18names) {
    this.id = id;
    this.geonameId = geonameId;
    this.code = code;
    this.latitude = latitude;
    this.longitude = longitude;
    this.i18names = (i18names != null ? i18names : new HashMap<>());
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public Long getGeonameId() {
    return geonameId;
  }

  @Override
  public String getCode() {
    return code;
  }


  @Override
  public String getLatitude() {
    return latitude;
  }

  @Override
  public String getLongitude() {
    return longitude;
  }

  @Override
  public PlaceType getPlaceType(){
    return placeType;
  }

  @Override
  public void setPlaceType(PlaceType placeType){
    this.placeType = placeType;
  }

  @Override
  public Place getContinent() {
    if(continent == null) {
      if(country != null && country.getContinent() != null) {
        return country.getContinent();
      }else if(region != null && region.getContinent() != null) {
        return region.getContinent();
      }else if(subregion != null) {
        return subregion.getContinent();
      }
    }
    return continent;
  }

  @Override
  public void setContinent(Place continent) {
    this.continent = continent;
  }

  @Override
  public Place getCountry() {
    if(country == null) {
      if(region != null && region.getCountry() != null) {
        return region.getCountry();
      }else if(subregion != null) {
        return subregion.getCountry();
      }
    }
    return country;
  }

  @Override
  public void setCountry(Place country) {
    this.country = country;
    if(country != null && continent == null){
      setContinent(country.getContinent());
    }
  }

  @Override
  public Place getRegion() {
    if(region == null && subregion != null){
      return subregion.getRegion();
    }
    return region;
  }

  @Override
  public void setRegion(Place region) {
    this.region = region;
    if(region != null && country == null){
      setCountry(region.getCountry());
    }
  }

  @Override
  public Place getSubregion() {
    return subregion;
  }

  @Override
  public void setSubregion(Place subregion) {
    this.subregion = subregion;
    if(subregion != null && region == null){
      setRegion(subregion.getRegion());
    }
  }

  @Override
  public Map<String, String> getNames() {
    return i18names;
  }

  @Override
  public void addName(String name, String lang) {
    // simply overwrite existing value
    if (name != null && !"".equals(name.trim()) && lang != null && !"".equals(lang.trim())) {
      i18names.put(lang, name);
    }
  }

  @Override
  public String getName(String lang) {
    String name = i18names.get(lang);
    if(name == null){
      Optional<Map.Entry<String, String>> optional = i18names.entrySet().stream().findAny();
      if(optional.isPresent())
        name = optional.get().getValue();
    }
    return name;
  }

  @Override
  public String getDefaultLang(String lang) {
    if(!i18names.containsKey(lang)){
      if(!i18names.containsKey("en")){
        return "en";
      }
      Optional<Map.Entry<String, String>> s = i18names.entrySet().stream().findFirst();
      return (s.isPresent() ? s.get().getKey() : null);
    }
    return lang;
  }

  @Override
  public String getDefaultName() {
    String name = i18names.get("en");
    if(name == null){
      Optional<Map.Entry<String, String>> s = i18names.entrySet().stream().findFirst();
      name = (s.isPresent() ? s.get().getValue() : null);
    }
    return name;
  }

  @Override
  public String getCompletePlacename(String lang){
    String completeName = getName(lang);
    if(this.region != null) completeName += ", " + this.region.getName(lang);
    if(this.country != null) completeName += ", " + this.country.getName(lang);
    if(this.continent != null) completeName += ", " + this.continent.getCode();
    return completeName;
  }

  @Override
  public String toString() {
    return "ConcretePlace{" +
            "id=" + id +
            ", geonameId=" + geonameId +
            ", name=" + getDefaultName() +
            ", latitude='" + latitude + '\'' +
            ", longitude='" + longitude + '\'' +
            '}';
  }
}
