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

import java.util.List;

/**
 * This interface represents the hierarchy tree structure node of the a tag hierarchy.
 *
 * @author Martin Rouffiange
 */
public interface HierarchyNode {

  /**
   * Get the node id
   *
   * @return the node id
   */
  long getId();

  /**
   * Get the node name
   *
   * @return the node id
   */
  String getName();

  int getDepth();

  void setDepth(int depth);

  public void addDepth();

  /**
   * Get the amount of children of the node
   *
   * @return the amount of children node
   */
  int getNbChildren();

  /**
   * Get the children nodes of this node
   *
   * @return a possibly empty list of HierarchyNodes
   */
  List<HierarchyNode> getChildren();

  /**
   * Set the children nodes of this node
   *
   * @param children a list of HierarchyNodes
   */
  void setChildren(List<HierarchyNode> children);

  /**
   * Add a child to this node
   *
   * @param child the new node to add in hierarchy
   */
  void addChild(HierarchyNode child);

  /**
   * Add a child to this node without adding this node as parent to the new child (avoid recursive)
   *
   * @param child the new node to add in hierarchy
   */
  void addChildSimply(HierarchyNode child);
  /**
   * Get the amount of parents of the node
   *
   * @return the amount of parents node
   */
  int getNbParents();

  /**
   * Get the parents nodes of this node
   *
   * @return a possibly empty list of HierarchyNodes
   */
  List<HierarchyNode> getParents();

  /**
   * Set the parents nodes of this node
   *
   * @param parents a list of HierarchyNodes
   */
  void setParents(List<HierarchyNode> parents);

  /**
   * Add a parent to this node
   *
   * @param parent the new node to add in hierarchy
   */
  void addParent(HierarchyNode parent);

  /**
   * Add a parent to this node without adding this node as child to the new parent (avoid recursive)
   *
   * @param parent the new node to add in hierarchy
   */
  void addParentSimply(HierarchyNode parent);

  /**
   * Add a node in the hierarchy of this node
   *
   * @param hierarchy the new node to add in hierarchy
   * @param isParent true if the given node must be the child of this node
   */
  void addInHierarchy(HierarchyNode hierarchy, boolean isParent);

}
