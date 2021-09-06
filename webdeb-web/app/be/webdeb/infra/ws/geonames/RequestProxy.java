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

import be.webdeb.core.api.contribution.place.EPlaceType;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.infra.ws.util.WebService;
import be.webdeb.presentation.web.controllers.entry.PlaceForm;
import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
import play.libs.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * Request handler for all Geonames requests.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class RequestProxy {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private WebService ws;
  private Configuration configuration;
  private TextFactory factory;

  private static final String SEARCH = "&q=";
  private static final String USERNAME = "&username=webdeb";
  private static final String FULLSEARCH = "&style=full";
  private static final String SEARCHLANG = "&lang=";
  private static final String MAXROWS = "&maxRows=10";

  /**
   * Injected constructor
   *
   * @param ws the webservice helper
   * @param configuration the play configuration module
   * @param factory the factory for Place
   */
  @Inject
  public RequestProxy(WebService ws, Configuration configuration, TextFactory factory) {
    this.ws = ws;
    this.configuration = configuration;
    this.factory = factory;
  }

  /**
   * Contact the Geonames service to search place
   *
   * @param name the query to browse
   * @return a Json string that contains found place
   */
  public CompletionStage<JsonNode> searchPlace(String name, String lang) {
    logger.debug("send GET search request to geonames service");

    if(!isblank(name)) {
      String url = configuration.getString("geoname.search");
      url += SEARCH + name.replaceAll(" ", "%20");
      url += USERNAME;
      url += MAXROWS;
      url += SEARCHLANG + lang;
      url += FULLSEARCH;

    return ws.getWithJsonResponse(url).thenApply(content -> content).exceptionally(t -> {
      logger.error("unable to call genonames api : JSON content", t);
      return null;
    });
    }
    return null;
  }

  /**
   * Contact the Geonames service to search place by geoname id
   *
   * @param id the latitude of the place to search
   * @return a Json string that contains found place
   */
  public CompletionStage<JsonNode> searchPlaceById(Long id) {
    logger.debug("send GET byGeonameId request to geonames service with param : " + id);

    String url = configuration.getString("geoname.searchById");
    url += id;
    url += FULLSEARCH;
    url += USERNAME;

    return ws.getWithJsonResponse(url).thenApply(content -> content).exceptionally(t -> {
      logger.error("unable to call genonames api : JSON content", t);
      return null;
    });
  }

  /**
   * Fill the place form with geonames data
   *
   * @param place the place form
   * @return the filled place form
   */
  public PlaceForm fillPlace(PlaceForm place) throws InterruptedException, ExecutionException {
    logger.debug("fill placeForm : " + place);

    return retrievePlace(place.getId(), place.getGeonameId(), null, null);
  }

  /**
   * Retrieve place from db or geonames api
   *
   * @param geonameId the geoname id
   * @param code the 3-char ISO 3166 code of the place
   * @param placeType the type of the place if it's known
   * @return the filled place form
   */
  private PlaceForm retrievePlace(Long id, Long geonameId, String code, Integer placeType) throws InterruptedException, ExecutionException {
    PlaceForm form = null;

    if(id != null || geonameId != null) {
      Long placeId = factory.retrievePlaceByGeonameIdOrPlaceId(geonameId, id);
      if ((placeId == null || placeType == null) && geonameId != null) {
        GeonamePlace place = Json.fromJson(searchPlaceById(geonameId).toCompletableFuture().get(), GeonamePlace.class);

        if(placeType == null) {
          placeType = transformFCodeToPlaceType(place.getFcode(), place.getAdminId4());
          if (placeType.equals(EPlaceType.OTHER.id())) {
            place = Json.fromJson(searchPlaceById(place.getAdminId4()).toCompletableFuture().get(), GeonamePlace.class);
            GeonamePlace place2 = Json.fromJson(searchPlaceById(place.getAdminId4() - 1).toCompletableFuture().get(), GeonamePlace.class);
            if (place.getAsciiName().equals(place2.getAsciiName())) place.setNames(place2.getNames());
            placeType = EPlaceType.PLACE.id();
          }
        }

        if (placeType >= -1 && !place.getNames().isEmpty()) {
          form = new PlaceForm(place.getGeonameId(), place.getLat(), place.getLng(), placeType);
          form.setNames(place.getNamesToPlaceNameForm());
          if (placeType.equals(EPlaceType.PLACE.id())) {
            form.setSubregion(retrievePlace(null, place.getAdminId2(), place.getAdminCode2(), EPlaceType.SUBREGION.id()));
          }
          if (placeType.equals(EPlaceType.SUBREGION.id()) ||
            place.getAdminId2() == null && placeType.equals(EPlaceType.PLACE.id())) {
            form.setCode(place.getAdminCode2());
            form.setRegion(retrievePlace(null, place.getAdminId1(), place.getAdminCode1(), EPlaceType.REGION.id()));
          }
          else if (placeType.equals(EPlaceType.REGION.id())) {
            form.setCode(place.getAdminCode1());
            form.setCountry(retrievePlace(null, place.getCountryId(), place.getCountryCode(), EPlaceType.COUNTRY.id()));
          }
          else {
            form.setCode((placeType.equals(EPlaceType.COUNTRY.id()) ? place.getCountryCode() : null));
            form.setContinent(new PlaceForm(factory.retrievePlaceContinentCode(place.getContinentCode())));
          }
        }
      } else {
        form = new PlaceForm(placeId);
      }
    }
    return form;
  }

  /**
   * Transform fcode from geonames to PlaceType
   *
   * @param fcode the code to transform
   * @param adminId4 the id of the place city
   * @return the PlaceType id, or -1 if nothing match
   */
  private int transformFCodeToPlaceType(String fcode, Long adminId4){
    if(fcode != null){
      if(!fcode.equals("ADM4") && adminId4 != null){
        return EPlaceType.OTHER.id();
      }
      if(fcode.startsWith("PPL")) fcode = "PPL";
      if(fcode.startsWith("PCL")) fcode = "PCL";
      if(fcode.startsWith("RGN")) fcode = "RGN";
      switch(fcode){
        case "RGN" :
        case "ZN" :
        case "TERR" :
        case "CONT" :
        case "AREA" :
          return EPlaceType.SUBCONTINENT.id();
        case "PCL" :
          return EPlaceType.COUNTRY.id();
        case "ADM1" :
        case "ADM1H" :
          return EPlaceType.REGION.id();
        case "ADM2" :
        case "ADM2H" :
          return EPlaceType.SUBREGION.id();
        case "PPL" :
        case "A" :
          return EPlaceType.PLACE.id();
      }
    }
    return -1;
  }

  /**
   * Check if a specified parameter is blank
   *
   * @param parameter the string to analyse
   * @return true if its blank
   */
  private boolean isblank(String parameter){
    return (parameter == null || parameter.length() == 0);
  }

}
