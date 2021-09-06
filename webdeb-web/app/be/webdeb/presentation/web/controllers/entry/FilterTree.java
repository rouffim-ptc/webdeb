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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A basic structure for filter. The structure can be used as a basic filter that just need the type of filter
 * or a tree of filter that need filter type and the tree nodes
 *
 * @author Martin Rouffiange
 * @see FilterTreeNode
 * @see EFilterType
 */
public class FilterTree implements Comparable<FilterTree> {

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private EFilterType filter;
  private FilterTreeNode tree;
  private String name;
  private int nbOccurs;
  private boolean linkedHierarchy;


  /**
   * Construct a simple tree from the filter type
   *
   * @param filter the type of filter
   */
  public FilterTree(EFilterType filter) {
    this(filter, false);
  }

  /**
   * Construct a simple tree from the filter type and tree name
   *
   * @param filter the type of filter
   * @param name the name of the filter
   */
  public FilterTree(EFilterType filter, String name) {
    this(filter, name,false);
  }

  /**
   * Construct a simple tree from the filter type
   *
   * @param filter the type of filter
   * @param linkedHierarchy true if the parent included the children
   */
  public FilterTree(EFilterType filter, boolean linkedHierarchy) {
    this(filter, "", linkedHierarchy);
  }

  /**
   * Construct a simple tree from the filter type
   *
   * @param filter the type of filter
   * @param name the name of the filter
   * @param linkedHierarchy true if the parent included the children
   */
  public FilterTree(EFilterType filter, String name, boolean linkedHierarchy) {
    this.filter = filter;
    this.name = name;
    this.nbOccurs = 1;
    this.linkedHierarchy = linkedHierarchy;
  }

  /**
   * Add nodes on the tree
   *
   * @param nodes a list of String that contains all node of a tree branch
   * @param addToOccurs true if the count must be updated
   */
  public void addListToTree(List<String> nodes, boolean addToOccurs){
    if(nodes != null && !nodes.isEmpty()) {
      if(tree == null) tree = new FilterTreeNode("tree");
      FilterTreeNode node = tree;

      for (int iNode = 0; node != null && iNode < nodes.size(); iNode++) {
        node = node.addSubNode(nodes.get(iNode), addToOccurs && iNode == nodes.size()-1);
      }
    }
  }

  /**
   * Add nodes on the tree from map
   *
   * @param nodes a map of String, Boolean (displayHierarchy) that contains all node of a tree branch
   * @param addToOccurs true if the count must be updated
   */
  public void addListToTree(Map<String, Boolean> nodes, boolean addToOccurs){
    if(nodes != null && !nodes.isEmpty()) {
      if(tree == null) tree = new FilterTreeNode("tree");
      FilterTreeNode node = tree;

      int iNode = 0;
      for (Map.Entry<String, Boolean> n : nodes.entrySet()) {
        node = node.addSubNode(n.getKey(), n.getValue(),addToOccurs && iNode == nodes.size()-1);
      }
    }
  }

  /**
   * Get the name of the tree
   *
   * @return the node tree
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the tree
   *
   * @param name the treename
   */
  public void setName(String name) {
    this.name = name;
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
   */
  public void addToOccurs() {
    if(nbOccurs < 99)
      nbOccurs++;
  }

  /**
   * Check if the tree is empty or not
   *
   * @return true if it is empty
   */
  public boolean isEmpty(){
    return (tree == null || tree.isEmpty());
  }

  @Override
  public int compareTo(FilterTree filterTree) {
    int result = filterTree.getNbOccurs() - this.getNbOccurs();

    if (result == 0) {
      result = this.name.compareTo(filterTree.name);
    }

    return result;
  }

  /**
   * Get the type of filter that is concerned by this tree
   *
   * @return the type of filter
   */
  public EFilterType getFilter() {
    return filter;
  }

  /**
   * Set the type of filter that is concerned by this tree
   *
   * @param filter the type of filter
   */
  public void setFilter(EFilterType filter) {
    this.filter = filter;
  }

  /**
   * Get true if the hierarchy is linked
   *
   * @return the flag that said if hierarchy is linked
   */
  public boolean isLinkedHierarchy() {
    return linkedHierarchy;
  }

  /**
   * Get the tree
   *
   * @return the root node of the tree
   */
  public FilterTreeNode getTree() {
    determineTreeIds();
    return tree;
  }

  /**
   * Set the tree
   *
   * @param tree the root node of the tree
   */
  public void setTree(FilterTreeNode tree) {
    this.tree = tree;
  }

  public void sortTree(){
    if(!isEmpty()){
      tree.sortTree();
      tree.getSubNodes().sort(Comparator.comparing((FilterTreeNode e) -> e.getSubNodes().size()).reversed());
    }
  }

  /**
   * Return true if there is only one element
   */
  public boolean isAlone(){
    if(isEmpty()){
      return true;
    }

    return tree.isAlone(linkedHierarchy);
  }

  // private method

  /**
   * Determine the tree nodes ids. It will be usefull for filtering in the view
   *
   */
  private void determineTreeIds(){
    determineNodesIds(tree, 0);
  }

  /**
   * The recursive method to determine tree nodes ids
   *
   * @param n the current tree node
   * @param nodeId the current node id
   */
  private int determineNodesIds(FilterTreeNode n, int nodeId){
    for (int iNode = 0; n != null && iNode < n.getSubNodes().size(); iNode++) {
      FilterTreeNode node = n.getSubNodes().get(iNode);
      node.setId(nodeId);
      nodeId = determineNodesIds(node, nodeId+1);
    }
    return nodeId;
  }
}
