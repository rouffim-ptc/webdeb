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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A node for basic filter tree structure.
 *
 * @author Martin Rouffiange
 * @see FilterTree
 */
public class FilterTreeNode implements Comparable<FilterTreeNode> {

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private int id;
  private String name;
  private int nbOccurs;
  private boolean displayHierarchy;
  private List<FilterTreeNode> subNodes = new ArrayList<>();

  /**
   * Construct a simple tree from the filter type
   *
   * @param name the name of the filter node (is the data node and its unique one)
   */
  public FilterTreeNode(String name) {
    this.name = name;
    this.displayHierarchy = true;
    this.nbOccurs = 0;
  }

  /**
   * Construct a simple tree from the filter type
   *
   * @param name the name of the filter node (is the data node and its unique one)
   * @param displayHierarchy true if this profession must displayed its hierarchy
   */
  public FilterTreeNode(String name, boolean displayHierarchy) {
    this.name = name;
    this.displayHierarchy = displayHierarchy;
    this.nbOccurs = 0;
  }

  /**
   * Add a new node on the tree if it doesn't exists, otherwise return it
   *
   * @param name the name of the node
   * @param addToOccurs true if the count must be updated
   * @return the new node or the existing one
   */
  public FilterTreeNode addSubNode(String name, boolean addToOccurs) {
    return addSubNode(name, true, addToOccurs);
  }

  /**
   * Add a new node on the tree if it doesn't exists, otherwise return it
   *
   * @param name the name of the node
   * @param displayHierarchy true if this profession must displayed its hierarchy
   * @param addToOccurs true if the count must be updated
   * @return the new node or the existing one
   */
  public FilterTreeNode addSubNode(String name, boolean displayHierarchy, boolean addToOccurs) {
    if(subNodes != null && name != null && !name.equals("")){
      FilterTreeNode node = searchNode(name);
      if(node == null) {
        node = new FilterTreeNode(name, displayHierarchy);
        subNodes.add(node);
      }
      node.addToOccurs(addToOccurs);
      return node;
    }
    return null;
  }

  /**
   * Search a node in children nodes
   *
   * @param name the name of the node
   * @return the node if it exists, null otherwise
   */
  private FilterTreeNode searchNode(String name){
    Optional<FilterTreeNode> oNode = subNodes.stream().filter(n -> name.equals(n.getName())).findFirst();
    return oNode.isPresent() ? oNode.get() : null;
  }

  /**
   * Check if the node is empty or not
   *
   * @return true if it is empty
   */
  public boolean isEmpty(){
    return (name == null && subNodes.isEmpty());
  }

  @Override
  public int compareTo(FilterTreeNode filterTreeNode) {
    int result = filterTreeNode.getNbOccurs() - this.getNbOccurs();

    if (result == 0) {
      result = this.name.compareTo(filterTreeNode.name);
    }

    return result;
  }

  /**
   * Get the id of the node
   *
   * @return the node id
   */
  public int getId() {
    return id;
  }

  /**
   * Set the id of the node
   *
   * @param id the node id
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the name of the node
   *
   * @return the node name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the node
   *
   * @param name the node name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the node children
   *
   * @return the node children
   */
  public List<FilterTreeNode> getSubNodes() {
    return subNodes;
  }

  /**
   * Set the children of the node
   *
   * @param subNodes children nodes
   */
  public void setSubNodes(List<FilterTreeNode> subNodes) {
    this.subNodes = subNodes;
  }

  /**
   * Get the number of occurences of the apparition of this node
   *
   * @return the number of occurences
   */
  public int getNbOccurs() {
    return nbOccurs;
  }

  /**
   * Add one to the number of occurences
   *
   * @param addToOccurs true if the count must be updated
   */
  public void addToOccurs(boolean addToOccurs) {
    if(addToOccurs && nbOccurs < 99)
      nbOccurs++;
  }

  public boolean isDisplayHierarchy() {
    return displayHierarchy;
  }

  public void sortTree(){
    if(!isEmpty()){
      Collections.sort(subNodes);
      subNodes.forEach(FilterTreeNode::sortTree);
    }
  }

  /**
   * Return true if there is only one
   */
  public boolean isAlone(boolean checkOccurs){
    if(subNodes.size() < 1){
      return true;
    }else if(subNodes.size() == 1 && (!checkOccurs || nbOccurs == 0)){
      return subNodes.get(0).isAlone(checkOccurs);
    }
    return false;
  }

  @Override
  public String toString() {
    return name;
  }
}
