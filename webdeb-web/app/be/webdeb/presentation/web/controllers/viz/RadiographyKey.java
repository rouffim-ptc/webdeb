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

package be.webdeb.presentation.web.controllers.viz;

import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;
import java.util.Date;

/**
 * This class holds values to be used as key object for the radiography view points.
 *
 * Hashcode is redefined to take into account the type of date it contains and how the
 * hashcode must be actually calculated
 *
 * @author Fabian Gilson
 */
public class RadiographyKey {

  // optional, only for persons
  private String lastname;
  // required
  private String name;
  // used to know the hashcode calculation method
  private ERadiographyViewKey key;
  public static final String NOKEY = "ZZZ_NOKEY";

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    /**
   * Constructor
   *
   * @param name a key name to be dispalyed
   * @param key the corresponding view key
   */
  public RadiographyKey(String name, ERadiographyViewKey key) {
    this(null, name, key);
  }

    /**
   * Constructor
   *
   * @param lastname the actor's lastname (used to sort actors on last names)
   * @param name the key name to be displayed
   * @param key the corresponding argument view key
   */
  public RadiographyKey(String lastname, String name, ERadiographyViewKey key) {
    this.lastname = lastname;
    this.name = name != null ? name : NOKEY;
    this.key = key;
  }

    /**
   * Get the actor's lastname, if any
   *
   * @return the actor's lastname, null if unset
   */
  public String getLastname() {
    return lastname;
  }

    /**
   * Get the key under which this radiography element will be displayed
   *
   * @return a name
   */
  public String getName() {
    return name;
  }

    /**
   * Get the key used to view / gathers arguments
   *
   * @return an ERadiographyViewKey
   * @see ERadiographyViewKey
   */
  public ERadiographyViewKey getKey() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RadiographyKey radioKey = (RadiographyKey) o;

    // a bit of obfuscation, i.e.... checking if we have both names they are equal or if only names are equal
    return !name.equals(radioKey.name) || (lastname != null && !lastname.equals(radioKey.lastname));
  }

  @Override
  public int hashCode() {
    int result;
    switch (key) {
      case AUTHOR:
      case SOURCE:
        result = name != null ? name.hashCode() : -1;
        break;
      case DATE:
        // try to make a date with it and sends back millis to have a correct order
        Date date = values.toDate(name);
        result = date != null ? Long.hashCode(date.getTime()) : -1;
        break;
      default:
        result = -1;
    }

    return 31 * result;
  }

  @Override
  public String toString() {
    return name + " " + key.name();
  }
}
