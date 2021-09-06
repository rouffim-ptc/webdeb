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

package be.webdeb.core.impl.tag;

import be.webdeb.core.api.tag.HierarchyNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a Tag hierarchy node that construct a Tag hierarchy.
 *
 * @author Martin Rouffiange
 */
public class ConcreteHierarchyNode implements HierarchyNode {

  private long id;
  private String name;
  private int depth;

  private List<HierarchyNode> parents;
  private List<HierarchyNode> children;

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Construct a complete hierarchy node
   *
   * @param id the node unique id
   * @param name the node name
   * @param depth the depth of the node in the hierarchy tree
   */
  public ConcreteHierarchyNode(long id, String name, int depth) {
    this.id = id;
    this.name = name;
    this.depth = depth;
  }

  /**
   * Construct a complete hierarchy node with a default depth of 0
   *
   * @param id the node unique id
   * @param name the node name
   */
  public ConcreteHierarchyNode(long id, String name) {
    this(id, name, 0);
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getDepth() {
    return depth;
  }

  @Override
  public void setDepth(int depth) {
    this.depth = depth;
  }

  @Override
  public void addDepth() {
    depth++;
  }

  @Override
  public int getNbChildren() {
    return (children != null ? children.size() : 0);
  }

  @Override
  public List<HierarchyNode> getChildren() {
    if(children == null){
      children = new ArrayList<>();
    }
    return children;

  }

  @Override
  public void setChildren(List<HierarchyNode> children) {
    this.children = children;
  }

  @Override
  public void addChild(HierarchyNode child) {
    if(child != null) {
      getChildren();
      children.add(child);
      child.addParentSimply(this);
    }
  }

  @Override
  public void addChildSimply(HierarchyNode child) {
    if(child != null) {
      getChildren();
      children.add(child);
    }
  }

  @Override
  public int getNbParents() {
    return (parents != null ? parents.size() : 0);
  }

  @Override
  public List<HierarchyNode> getParents() {
    if(parents == null){
      parents = new ArrayList<>();
    }
    return parents;

  }

  @Override
  public void setParents(List<HierarchyNode> parents) {
    this.parents = parents;
  }

  @Override
  public void addParent(HierarchyNode parent) {
    if(parent != null) {
      getParents();
      parents.add(parent);
      parent.addChildSimply(this);
    }
  }

  @Override
  public void addParentSimply(HierarchyNode parent) {
    if(parent != null) {
      getParents();
      parents.add(parent);
    }
  }

  @Override
  public void addInHierarchy(HierarchyNode hierarchy, boolean isParent){
    if(isParent){
      addChild(hierarchy);
    }else{
      addParent(hierarchy);
    }
  }

  @Override
  public String toString() {
    return "node " + id + " called " + name;
  }

  /*
   * inner methods
   */

}
