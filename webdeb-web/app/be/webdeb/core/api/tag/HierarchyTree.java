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

package be.webdeb.core.api.tag;

import java.util.HashMap;

/**
 * This interface the hierarchy tree structure of a tag hierarchy.
 * A tree is composed of nodes. Each nodes have parents and children.
 * Nodes without parents are root, and nodes without children are leaf.
 *
 * @author Martin Rouffiange
 */
public interface HierarchyTree {

  /**
   * Get the root of the hierarchy tree. This root is technical due to multiple possible roots on a hierarchy.
   *
   * @return a possibly empty list of folders
   */
  HierarchyNode getRoot();

  /**
   * Get the root depth. A tree depth is the maximum of parent-child relations in the tree
   * (ex : the maximum of generations in a family tree)
   *
   * @return the depth of the tree
   */
  int hierarchyDepth();

  /**
   * Check if the hierarchy is full
   *
   * @return true if the hierarchy depth is full
   */
  boolean hierarchyIsFull();

  /**
   * Add a node to the hierarchy tree technical root.
   *
   * @param node the node to add at the root
   * @return a EHierarchyCode depending of the issue
   * @see EHierarchyCode
   */
  EHierarchyCode addNodeToRoot(HierarchyNode node);

  /**
   * Remove a node from the hierarchy tree technical root.
   *
   * @param node the node to remove
   */
  void removeNodeToRoot(HierarchyNode node);

  /**
   * Add a child to a given node
   *
   * @param child the new node to add in hierarchy
   * @param childTree the new node tree to check the depth of the tree
   * @param node the node in the hierarchy
   * @return a EHierarchy code depending of the issue
   * @see EHierarchyCode
   */
  EHierarchyCode addChildToNode(HierarchyNode child, HierarchyTree childTree, long node);

  /**
   * Add a parent to a given node
   *
   * @param parent the new node to add in hierarchy
   * @param parentTree the new node tree to check the depth of the tree
   * @param node the node in the hierarchy
   * @return a EHierarchy code depending of the issue
   * @see EHierarchyCode
   */
  EHierarchyCode addParentToNode(HierarchyNode parent, HierarchyTree parentTree, long node);

  /**
   * Add a node in the hierarchy
   *
   * @param newNode the node to add at the hierarchy of the given node
   * @param newNodeTree the node hierarchy tree to add at the hierarchy of the given node (for checking tree depth)
   * @param node the node on the hierarchy that will be the parent or the child of the new node
   * @param isParent true if the given existing node is the parent in the hierarchy
   * @return a EHierarchyCode depending of the issue
   * @see EHierarchyCode
   */
  EHierarchyCode addNodeInHierarchy(HierarchyNode newNode, HierarchyTree newNodeTree, long node, boolean isParent);

  /**
   * Search a node in the hierarchy by its unique id.
   *
   * @param node the node id to browse
   * @return a possibly empty list of folders
   */
  HierarchyNode searchNode(long node);

  /**
   * Get the node map that keep all id values and corresponding HierarchyNodes on the tree.
   *
   * @return a possibly empty map of node id and corresponding node
   */
  HashMap<Long, HierarchyNode> getNodeMap();

  /**
   * Check if a given node is in the hierarchy tree
   *
   * @return true if the given node is in the hierarchy tree
   */
  boolean hasNode(Long node);

  /**
   * Remove relation between a given node and the technical root node if the given node has root as parent
   * and also others node (It must not happen)
   *
   * @param node the node id
   */
  void removeIfNodeAsRootAndOthersAsParents(Long node);

  /**
   * Get the parents hierarchy of the given node as JSON
   *
   * @return the parents hierarchy as json
   */
  String getParentsHierarchyAsJson(Long node);

  /**
   * Get the children hierarchy of this node as JSON
   *
   * @return the children hierarchy as json
   */
  String getChildrenHierarchyAsJson(Long node);

}
