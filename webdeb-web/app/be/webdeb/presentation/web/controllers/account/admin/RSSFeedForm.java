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

package be.webdeb.presentation.web.controllers.account.admin;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.text.ETextVisibility;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.infra.ws.nlp.RSSFeedSource;
import be.webdeb.util.ValuesHelper;
import play.data.validation.ValidationError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple form object to display and edit RSS feeds
 *
 * @author Fabian Gilson
 */
public class RSSFeedForm {

  private TextFactory textFactory = play.api.Play.current().injector().instanceOf(TextFactory.class);
  private ActorFactory actorFactory = play.api.Play.current().injector().instanceOf(ActorFactory.class);
  private ValuesHelper values = play.api.Play.current().injector().instanceOf(ValuesHelper.class);

  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private int id = -1;
  private String name;
  private String type;
  private String category;
  private String subcategory;
  private String country;
  private String url;
  private String visibility;
  private boolean status;

  /**
   * Default play-compliant empty constructor
   */
  public RSSFeedForm() {
    // needed by play / jackson
  }

  /**
   * Constructor, creates a form object from given RSS source to be displayed
   *
   * @param feedSource an RSS feed source
   * @param lang iso-639-1 language code
   */
  public RSSFeedForm(RSSFeedSource feedSource, String lang) {
    id = feedSource.getId();
    name = feedSource.getName();
    type = textFactory.getTextType(feedSource.getType()).getName(lang);

    category = feedSource.getCategory();
    subcategory = feedSource.getSubcategory();
    try {
      country = actorFactory.getCountry(feedSource.getCountry()).getName(lang);
    } catch (FormatException e) {
      logger.error("unable to cast retrieved country " + feedSource.getCountry(), e);
    }
    status = RSSFeedSource.EStatus.ACCEPT.equals(RSSFeedSource.EStatus.valueOf(feedSource.getStatus().toUpperCase()));
    url = feedSource.getUrl();
    try {
      visibility = textFactory.getTextVisibility(feedSource.getVisibility()).getName(lang);
    } catch (FormatException e) {
      logger.error("unable to cast retrieved visibility " + feedSource.getVisibility(), e);
    }
  }

  /**
   * Constructor, creates a form object from given RSS source to be displayed
   *
   * @param feedSource an RSS feed source
   */
  public RSSFeedForm(RSSFeedSource feedSource) {
    id = feedSource.getId();
    name = feedSource.getName();
    type = String.valueOf(feedSource.getType());
    category = feedSource.getCategory();
    subcategory = feedSource.getSubcategory();
    country = feedSource.getCountry();
    url = feedSource.getUrl();
    visibility = String.valueOf(feedSource.getVisibility());
  }

  /**
   * Validate the creation of an RSS Feed (implicit call from form submission)
   *
   * @return null if validation ok, map of errors for each fields in error otherwise
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (values.isBlank(name)) {
      errors.put("name", Collections.singletonList(new ValidationError("name", "admin.rss.error.name")));
    }

    if (!values.isNumeric(type, 0, true)) {
      errors.put("type", Collections.singletonList(new ValidationError("type", "text.error.textType")));
    }

    if (values.isBlank(category)) {
      errors.put("category", Collections.singletonList(new ValidationError("category", "admin.rss.error.category")));
    }

    if (values.isBlank(country)) {
      errors.put("country", Collections.singletonList(new ValidationError("country", "admin.rss.error.country")));
      try {
        actorFactory.getCountry(country);
      } catch (FormatException e) {
        logger.error("unable to transform country code " + country, e);
        errors.put("country", Collections.singletonList(new ValidationError("country", "admin.rss.error.country")));
      }
    }

    if (!values.isURL(url)) {
      errors.put("url", Collections.singletonList(new ValidationError("url", "admin.rss.error.url")));
    }

    if (!values.isNumeric(visibility, 0, true)) {
      errors.put("visibility", Collections.singletonList(new ValidationError("visibility", "text.error.visibility")));
      try {
        ETextVisibility.value(Integer.valueOf(visibility));
      } catch (Exception e) {
        logger.error("unable to transform visibility code " + visibility, e);
        errors.put("visibility", Collections.singletonList(new ValidationError("visibility", "text.error.visibility")));
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  /*
   * GETTER / SETTERS
   */

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVisibility() {
    return visibility;
  }

  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
}
