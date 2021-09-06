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

import java.util.ArrayList;
import java.util.List;

/**
 * This class send the index of the match and the list of contribution name-matched
 *
 * @author Martin Rouffiange
 */
public class ConcreteNameMatch<E> implements NameMatch<E> {

  private String selector;
  private int index;
  private List<E> nameMatches;
  private boolean isActor = false;

  /**
   * Constructor
   *
   * @param selector the selector string for name match
   * @param index the index of the match
   * @param nameMatches the list of name matches
   */
  public ConcreteNameMatch(String selector, int index, List<E> nameMatches) {
    this(selector, index, nameMatches, false);
  }

  /**
   * Constructor
   *
   * @param selector the selector string for name match
   * @param index the index of the match
   * @param nameMatches the list of name matches
   */
  public ConcreteNameMatch(String selector, int index, List<E> nameMatches, boolean isActor) {
    this.selector = selector;
    this.index = index;
    this.nameMatches = nameMatches;
    this.isActor = isActor;
  }

  /**
   * Alternative empty constructor
   *
   */
  public ConcreteNameMatch() {
    this("", -1, new ArrayList<>());
  }

  @Override
  public String getSelector() {
    return selector;
  }

  public int getIndex() {
    return index;
  }

  public List<E> getNameMatches() {
    return nameMatches;
  }

  public boolean isEmpty(){
    return (index == -1);
  }

  @Override
  public boolean isActor() {
    return isActor;
  }

  @Override
  public String toString() {
    return "ConcreteNameMatch{" +
            "selector=" + selector +
            "nameMatches=" + nameMatches +
            '}';
  }
}
