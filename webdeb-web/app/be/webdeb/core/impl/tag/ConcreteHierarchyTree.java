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

import be.webdeb.core.api.tag.EHierarchyCode;
import be.webdeb.core.api.tag.HierarchyNode;
import be.webdeb.core.api.tag.HierarchyTree;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements a Tag hierarchy node that construct a Tag hierarchy.
 *
 * @author Martin Rouffiange
 */
public class ConcreteHierarchyTree implements HierarchyTree {

  private HierarchyNode root;
  private HashMap<Long, HierarchyNode> nodeMap = new HashMap<>();

  // keep the depth value as variable for eco
  private int hierarchyDepth = 0;

  private final static int MAX_PARENTS_NODES = 100;
  private final static int MAX_CHILDREN_NODES = 100;
  private final static int MAX_HIERARCHY_DEPTH = 100;

  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Default constructor. Build a technical root node.
   *
   */
  public ConcreteHierarchyTree(){
    this.root = new ConcreteHierarchyNode(-1, "root");
    nodeMap.put(this.root.getId(), this.root);
  }

  @Override
  public HierarchyNode getRoot() {
    return this.root;
  }

  @Override
  public EHierarchyCode addNodeToRoot(HierarchyNode node){
    //logger.debug(root+"");
    if(root == null){
      return EHierarchyCode.NO_PARENT;
    }
    if(node == null || node.getId() < 0){
      return EHierarchyCode.NULL_OR_NO_ID;
    }
    //logger.debug("d");
    root.addChild(node);
    return EHierarchyCode.OK;
  }

  @Override
  public void removeNodeToRoot(HierarchyNode node){
    if(root != null && node != null){
      int iRootChild;
      for(iRootChild = 0; iRootChild < root.getChildren().size() && root.getChildren().get(iRootChild).getId() == node.getId(); iRootChild++);
      if(iRootChild < root.getChildren().size()){
        node.getParents().removeIf(p -> p.getId() == root.getId());
        root.getChildren().remove(iRootChild);
      }
    }
  }

  @Override
  public int hierarchyDepth() {
    return hierarchyDepth;
  }

  @Override
  public boolean hierarchyIsFull() {
    return (hierarchyDepth() >= MAX_HIERARCHY_DEPTH);
  }

  @Override
  public EHierarchyCode addChildToNode(HierarchyNode child, HierarchyTree childTree, long node) {
    return addNodeInHierarchy(child, childTree, node, true);
  }

  @Override
  public EHierarchyCode addParentToNode(HierarchyNode parent, HierarchyTree parentTree, long node) {
    return addNodeInHierarchy(parent, parentTree, node, false);
  }

  @Override
  public EHierarchyCode addNodeInHierarchy(HierarchyNode newNode, HierarchyTree newNodeTree, long node, boolean isParent) {
    // return error if the new node to add is null or has not id
    if(newNode == null || newNode.getId() < 0) {
      return EHierarchyCode.NULL_OR_NO_ID;
    }
    // return error if hierarchy is already in the hierarchy return
    if(nodeMap.containsKey(newNode.getId())) {
      return EHierarchyCode.ALREADY;
    }
    // return error if the given node is not contained in the hierarchy
    if(!nodeMap.containsKey(node)) {
      return EHierarchyCode.NOT_FOUND;
    }
    // return error if the new node hierarchy depth is full
    if(newNodeTree != null && newNodeTree.hierarchyDepth() >= MAX_HIERARCHY_DEPTH) {
      return EHierarchyCode.NULL_OR_NO_ID;
    }
    // return error if the depth of the tree is at its maximum
    if(hierarchyDepth() >= MAX_HIERARCHY_DEPTH){
      return EHierarchyCode.FULL_DEPTH;
    }
    // search the given node id to found the corresponding object
    HierarchyNode hierarchyNode = searchNode(node);
    // return error if the node is null and remove the node id from the nodeMap (an error must have occurred)
    if(hierarchyNode == null) {
      nodeMap.remove(node);
      return EHierarchyCode.NOT_FOUND;
    }
    // return error if the given node has too much children
    if(isParent && hierarchyNode.getNbChildren() >= MAX_CHILDREN_NODES) {
      return EHierarchyCode.FULL_CHILDREN;
    }
    // return error if the given node has too much parents
    if(isParent && hierarchyNode.getNbParents() >= MAX_PARENTS_NODES) {
      return EHierarchyCode.FULL_PARENTS;
    }
    // add new node in the hierarchy
    if(isParent){
      hierarchyNode.addChild(newNode);
    }
    else{
      hierarchyNode.addParent(newNode);
    }
    // merge trees or if newNodeTree is null simply add the new node to root children
    if(newNodeTree != null){
      mergeTrees(newNodeTree);
    }
    nodeMap.put(newNode.getId(), newNode);

    return EHierarchyCode.OK;
  }

  @Override
  public HierarchyNode searchNode(long node) {
    return nodeMap.get(node);
  }

  @Override
  public HashMap<Long, HierarchyNode> getNodeMap() {
    return nodeMap;
  }

  @Override
  public boolean hasNode(Long node) {
    return nodeMap.containsKey(node);
  }

  @Override
  public void removeIfNodeAsRootAndOthersAsParents(Long node){
    HierarchyNode hierarchyNode = searchNode(node);
    if(hierarchyNode != null && hierarchyNode.getParents().size() > 1 && root.getChildren().stream().anyMatch(e -> e.getId() == node)){
      removeNodeToRoot(hierarchyNode);
    }
  }

  @Override
  public String getParentsHierarchyAsJson(Long node) {
    String hierarchy = "";
    HierarchyNode hierarchyNode = searchNode(node);
    if (hierarchyNode != null) {
      hierarchy = getHierarchy(hierarchyNode, false);
    }
    return hierarchy;
  }

  @Override
  public String getChildrenHierarchyAsJson(Long node) {
    String hierarchy = "";
    HierarchyNode hierarchyNode = searchNode(node);
    if (hierarchyNode != null) {
      hierarchy = getHierarchy(hierarchyNode, true);
    }
    return hierarchy;
  }

  /*
   * inner methods
   */

  /**
   * merge the given tree with this one
   *
   * @param tree the tree to merge with this one
   */
  private void mergeTrees(HierarchyTree tree){
    if(tree != null){
      if(tree.getRoot() != null) {
        if (root != null) {
          for (HierarchyNode node : tree.getRoot().getChildren()) {
            addNodeToRoot(node);
          }
        } else {
          root = tree.getRoot();
        }
      }
      nodeMap.putAll(tree.getNodeMap());
    }
  }

  /**
   * Get the tag hierarchy from a given node as JSON string
   *
   * @param node the given node to start to draw hierarchy
   * @param isParent true if we want the children hierarchy of the given node
   * @return the hierarchy as JSON string
   */
  private String getHierarchy(HierarchyNode node, boolean isParent){
    JsonObject hierarchy = drawHierarchy(node, isParent);
    return (hierarchy != null ? hierarchy.toString() : "");
  }

  /**
   * Get the tag hierarchy from a given node as JSON object
   *
   * @param node the given node to start to draw hierarchy
   * @param isParent true if we want the children hierarchy of the given node
   * @return the hierarchy as JSON object
   */
  private JsonObject drawHierarchy(HierarchyNode node, boolean isParent){
    JsonObject hierarchy = new JsonObject();
    hierarchy.addProperty("id", node.getId());
    hierarchy.addProperty("name", node.getName());
    List<HierarchyNode> hierarchyNodes = (isParent ? node.getChildren() : node.getParents());
    if(!hierarchyNodes.isEmpty()){
      JsonArray children = new JsonArray();
      for(HierarchyNode child : hierarchyNodes){
        if(root == null || root.getId() != child.getId())
          children.add(drawHierarchy(child, isParent));
      }
      if(children.size() > 0)
        hierarchy.add("children", children);
    }
    return  hierarchy;
  }
}
