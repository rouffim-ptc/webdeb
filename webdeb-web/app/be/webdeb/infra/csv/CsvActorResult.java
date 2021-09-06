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

package be.webdeb.infra.csv;

import com.opencsv.bean.CsvBindByName;

/**
 * This bean class contains information regarding the result of an actor / affiliation import.
 * Fields are using only "object" types to allow null value to be passed for unset values
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class CsvActorResult {

  @CsvBindByName
  private Integer order;

  @CsvBindByName
  private Long actorId;

  @CsvBindByName
  private Integer otherOrder;

  @CsvBindByName
  private Long otherActorId;

  @CsvBindByName
  private String result;

  @CsvBindByName
  private String error;

  @CsvBindByName
  private String message;


  /**
   * Constructor for any type of general error (parsing, file not found, etc)
   *
   * @param error the name of the error
   * @param message an more detailed error message
   */
  public CsvActorResult(String error, String message) {
    this.error = error;
    this.message = message;
    result = "ERROR";
  }

  /**
   * Constructor for actor import success
   *
   * @param order the order number in the csv file
   * @param actorId the actor id, if existing
   */
  public CsvActorResult(Integer order, Long actorId) {
    this.order = order;
    this.actorId = actorId;
    result = "SUCCESS";
  }

  /**
   * Constructor for affiliation import success
   *
   * @param order the order number in the csv file
   * @param actorId the actor id, if existing
   * @param otherOrder the order number in the csv file for the affiliation
   * @param otherActorId the actor id for the affiliation, if existing
   */
  public CsvActorResult(Integer order, Long actorId, Integer otherOrder, Long otherActorId) {
    this(order, actorId);
    this.otherOrder = otherOrder;
    this.otherActorId = otherActorId;
  }

  /**
   * Constructor for actor import error
   *
   * @param order the order number in the csv file
   * @param actorId the actor id, if existing
   * @param error the error as raised by the system
   * @param message a more detailed message
   */
  public CsvActorResult(Integer order, Long actorId, String error, String message) {
    this(error, message);
    this.order = order;
    this.actorId = actorId;
  }

  /**
   * Constructor for affiliation import error
   *
   * @param order the order number in the csv file
   * @param actorId the actor id, if existing
   * @param otherOrder the order number in the csv file for the affiliation
   * @param otherActorId the actor id for the affiliation, if existing
   * @param error the error as raised by the system
   * @param message a more detailed message
   */
  public CsvActorResult(Integer order, Long actorId, Integer otherOrder, Long otherActorId, String error, String message) {
    this(order, actorId, error, message);
    this.otherOrder = otherOrder;
    this.otherActorId = otherActorId;
  }

  /**
   * Get the order (appearance index) of the actor
   *
   * @return an index value
   */
  public Integer getOrder() {
    return order;
  }

  /**
   * Get the actor id (in webdeb)
   *
   * @return an id
   */
  public Long getActorId() {
    return actorId;
  }

  /**
   * Get the affiliation actor id (in webdeb)
   *
   * @return an id (may be null)
   */
  public Integer getOtherOrder() {
    return otherOrder;
  }

  /**
   * Get the order (appearance index) of the affiliation actor
   *
   * @return an index value (may be null)
   */
  public Long getOtherActorId() {
    return otherActorId;
  }

  /**
   * Get the result of the import of the actor/affiliation
   *
   * @return either "SUCCESS" or "ERROR"
   */
  public String getResult() {
    return result;
  }

  /**
   * Get the error name (exception name) in case of result = "ERROR"
   *
   * @return an exception name
   */
  public String getError() {
    return error;
  }

  /**
   * Get a more detailed message in case of result = "ERROR"
   *
   * @return a detailed message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "CsvResult{" +
        "order=" + order +
        ", actorId=" + actorId +
        ", otherOrder=" + otherOrder +
        ", otherActorId=" + otherActorId +
        ", error='" + error + '\'' +
        ", message='" + message + '\'' +
        ", result='" + result + '\'' +
        '}';
  }
}
