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

package be.webdeb.infra.ws.nlp;

import be.webdeb.core.api.actor.Country;
import be.webdeb.core.api.text.TextType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used to hold RSS feed sources as retrieved from WDTAL RSS service that may be
 * configured by admin users.
 *
 * @author Fabian Gilson
 */
@JsonInclude(Include.NON_EMPTY)
public class RSSFeedSource {

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("source_id")
  private Integer id;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("source_name")
  private String name;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("genre")
  private Integer type;

  @JsonSerialize
  @JsonDeserialize
  private String category;

  @JsonSerialize
  @JsonDeserialize
  private String subcategory;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("source_country")
  private String country;

  @JsonSerialize
  @JsonDeserialize
  private String status;

  @JsonSerialize
  @JsonDeserialize
  private String url;

  @JsonSerialize
  @JsonDeserialize
  @JsonProperty("usage")
  private Integer visibility;


  /**
   * Default constructor
   */
  public RSSFeedSource() {
    // needed by jackson
  }

  /**
   * Constructor. Used delete an rss source feed
   *
   * @param id an RSS feed source
   */
  public RSSFeedSource(int id) {
    this.id = id;
  }

  /**
   * Constructor. Used to update status of an rss source feed
   *
   * @param id an RSS feed source
   * @param status the status to set to this source
   */
  public RSSFeedSource(int id, EStatus status) {
    this.id = id;
    this.status = status.name().toLowerCase();
  }

  /**
   * Constructor. Used to create a new (unregistered) feed source
   *
   * @param name the name of the source
   * @param type the text type id associated to this source
   * @param category the category (free-form)
   * @param subcategory the (nullable) category (free-form)
   * @param country the territory id where this source originates from
   * @param url the rss url
   * @param visibility the text visibility id to associate to all texts we will retrieve from that source
   *
   * @see be.webdeb.core.api.text.ETextVisibility
   * @see TextType
   * @see Country
   */
  public RSSFeedSource(String name, int type, String category, String subcategory, String country, String url, int visibility) {
    this.name = name;
    this.type = type;
    this.category = category;
    this.subcategory = subcategory;
    this.country = country;
    this.url = url;
    this.visibility = visibility;
  }

  /**
   * Constructor. Used to modify a feed source
   *
   * @param id the source id
   * @param name the name of the source
   * @param type the text type id associated to this source
   * @param category the category (free-form)
   * @param subcategory the (nullable) category (free-form)
   * @param country the territory id where this source originates from
   * @param url the rss url
   * @param visibility the text visibility id to associate to all texts we will retrieve from that source
   */
  public RSSFeedSource(int id, String name, int type, String category, String subcategory, String country, String url, int visibility) {
    this(name, type, category, subcategory, country, url, visibility);
    this.id = id;
  }

  /**
   * Get the source id
   *
   * @return an id
   */
  public Integer getId() {
    return id;
  }

  /**
   * Get the source name
   *
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the source type id
   *
   * @return an id
   * @see TextType
   */
  public Integer getType() {
    return type;
  }

  /**
   * Get the category name
   *
   * @return a name of a category
   */
  public String getCategory() {
    return category;
  }

  /**
   * Get the sub category name
   *
   * @return a name of a sub category, may be null
   */
  public String getSubcategory() {
    return subcategory;
  }

  /**
   * Get the country code
   *
   * @return an iso-639-1 country code
   * @see Country
   */
  public String getCountry() {
    return country;
  }

  /**
   * Get the status of this feed source
   *
   * @return either "accept" or "ignore"
   */
  public String getStatus() {
    return status;
  }

  /**
   * Get the feed source url
   *
   * @return an url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Get the text visibility id
   *
   * @return a visibility id
   * @see be.webdeb.core.api.text.ETextVisibility
   */
  public Integer getVisibility() {
    return visibility;
  }

  /**
   * Helper enum for RSS feed sources statuses
   *
   * @author Fabian Gilson
   */
  public enum EStatus {
    ACCEPT("accept"), IGNORE("ignore");

    private String id;
    private static Map<String, EStatus> map = new LinkedHashMap<>();

    static {
      for (EStatus status : EStatus.values()) {
        map.put(status.id, status);
      }
    }

    EStatus(String id) {
      this.id = id;
    }

    /**
     * Get an EStatus from an id
     *
     * @param id an id
     * @return either the EStatus object corresponding to given id, nul otherwise
     */
    public static EStatus value(String id) {
      return map.get(id);
    }

    /**
     * Get this EStatus id
     *
     * @return an id
     */
    public String id() {
      return id;
    }
  }

}
