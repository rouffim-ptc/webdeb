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
import com.avaje.ebean.*;
import org.apache.commons.lang3.math.NumberUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The abstract class for technical database tables that regroup the common behaviour
 *
 * @author Martin Rouffiange
 */
@MappedSuperclass
@Unqueryable
public abstract class TechnicalTable extends Model {

  private static final org.slf4j.Logger logger = play.Logger.underlying();

  @Column(name = "en", nullable = false)
  private String en;

  @Column(name = "fr", nullable = false)
  private String fr;

  @Column(name = "nl", nullable = false)
  private String nl;

  /**
   * Get the english description
   *
   * @return a description
   */
  public String getEn() {
    return this.en;
  }

  /**
   * Set the english description
   *
   * @param en a description
   */
  public void setEn(String en) {
    this.en = en;
  }

  /**
   * Get the french description
   *
   * @return a description
   */
  public String getFr() {
    return this.fr;
  }

  /**
   * Set the french description
   *
   * @param fr a description
   */
  public void setFr(String fr) {
    this.fr = fr;
  }

  /**
   * Get the dutch description
   *
   * @return a description
   */
  public String getNl() {
    return this.nl;
  }

  /**
   * Set the dutch description
   *
   * @param nl a description
   */
  public void setNl(String nl) {
    this.nl = nl;
  }

  /**
   * Get the map of names
   *
   * @return a map of names
   */
  public Map<String, String> getTechnicalNames(){
    LinkedHashMap<String, String> names = new LinkedHashMap<>();
    names.put("en", en);
    names.put("fr", fr);
    names.put("nl", nl);

    return names;
  }

  /**
   * Get the name of a technical table for given value
   *
   * @param technicalTableName a ETechnicalTableName
   * @param value the value of the type
   * @param lang the user lang
   * @param shade true if the technical table concerns a shade
   * @return the name of the technical type
   */
  public static String getTechnicalTableName(ETechnicalTableName technicalTableName, String value, String lang, boolean shade){

    String valueWhere = NumberUtils.isCreatable(value) ? value : "'" + value + "'";

    String select = "SELECT " + lang.toLowerCase() + " FROM " + technicalTableName.getTableName() +
            " WHERE id_" + (shade ? "shade" : "type") + " = " + valueWhere;

    List<SqlRow> rows = Ebean.createSqlQuery(select).findList();
    return rows != null && !rows.isEmpty() ? rows.get(0).getString(lang.toLowerCase()) : "";
  }

}
