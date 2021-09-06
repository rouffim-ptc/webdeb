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

package be.webdeb.infra.persistence.accessor.impl;

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.debate.EDebateType;
import be.webdeb.core.api.tag.*;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagLink;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.ObjectNotFoundException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Contribution;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This accessor handles tag-related persistence actions.
 *
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanTagAccessor extends AbstractContributionAccessor<TagFactory> implements TagAccessor {

  private final static int MAX_HIERARCHY = 10;

  private List<TagType> tagTypes;

  @Override
  public Tag retrieve(Long id, boolean hit) {
    be.webdeb.infra.persistence.model.Tag tag = be.webdeb.infra.persistence.model.Tag.findById(id);

    if (tag != null) {
      try {
        Tag api =  mapper.toTag(tag);
        if (hit) {
          addHitToContribution(tag.getContribution());
        }
        return api;
      } catch (FormatException e) {
        logger.error("unable to cast retrieved tag " + id, e);
      }
    } else {
      //logger.warn("no tag found for id " + id);
    }
    return null;
  }

  @Override
  public TagCategory retrieveTagCategory(Long id, boolean hit) {
    be.webdeb.infra.persistence.model.Tag tag = be.webdeb.infra.persistence.model.Tag.findById(id);

    if (tag != null) {
      try {
        TagCategory api =  mapper.toTagCategory(tag);
        if (hit) {
          addHitToContribution(tag.getContribution());
        }
        return api;
      } catch (FormatException e) {
        logger.error("unable to cast retrieved tag category" + id, e);
      }
    } else {
      if(id != null)
        logger.warn("no tag category found for id " + id);
    }
    return null;
  }

  @Override
  public TagLink retrieveLink(Long parent, Long child) {
    be.webdeb.infra.persistence.model.TagLink l =
        be.webdeb.infra.persistence.model.TagLink.findByParentChild(parent, child);
    if (l != null) {
      try {
        return mapper.toTagLink(l);
      } catch (FormatException e) {
        logger.error("unable to cast retrieved taglink between " + parent + " and " + child, e);
      }
    } else {
      logger.warn("no tag link found between " + parent + " and " + child);
    }
    return null;
  }

  @Override
  public List<Tag> findByName(String name, ETagType type) {
    List<be.webdeb.infra.persistence.model.Tag> tags =
        be.webdeb.infra.persistence.model.Tag.findByPartialName(name, null, (type == null ? -1 : type.id()));
    if (!tags.isEmpty()) {
      return buildList(tags);
    }
    return new ArrayList<>();
  }

  @Override
  public Tag findUniqueByNameAndLang(String name, String lang, ETagType type){
    be.webdeb.infra.persistence.model.Tag tag =
            be.webdeb.infra.persistence.model.Tag.findByCompleteNameAndLang(name, lang, type);

    if(tag != null){
      try {
        return mapper.toTag(tag);
      } catch (FormatException e) {
        logger.error("unable to cast retrieved tag", e);
      }
    }
    return null;
  }

  @Override
  public List<ContextContribution> getContextContributions(Long category) {
    return toContextContributions(be.webdeb.infra.persistence.model.Tag.getContextContributions(category));
  }

  @Override
  public Tag random() {
    try {
      return mapper.toTag(be.webdeb.infra.persistence.model.Tag.random());
    } catch (FormatException e) {
      logger.error("unable to cast retrieved random tag", e);
    }
    return null;
  }

  @Override
  public Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> save(Tag contribution, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save tag " + contribution.toString() + " in group " + contribution.getInGroups());
    int currentGroup = Group.getPublicGroup().getIdGroup();
    be.webdeb.infra.persistence.model.Contribution context = null;

    // check if contextId exists if tag is category, otherwise -> ObjectNotFound
    if(contribution.getTagType() != null && (contribution.getTagType().getEType() == ETagType.CATEGORY_TAG
            || contribution.getTagType().getEType() == ETagType.SUB_DEBATE_TAG)) {
      Long contextId = ((TagCategory) contribution).getCurrentContextId();
      context = Contribution.findById(contextId);
    }

    Long formerId = null;
    Tag formerContribution = null;
    Tag existing = findUniqueByNameAndLang(contribution.getDefaultName(), contribution.getDefaultLanguage(), contribution.getTagType() != null ? contribution.getTagType().getEType() : null);

    if(contribution.getTagType().getEType() != ETagType.SIMPLE_TAG && !values.isBlank(contribution.getId())) {
      Tag baseContribution = retrieve(contribution.getId(), false);

      if(baseContribution != null && !contribution.getDefaultName().equals(baseContribution.getDefaultName()) && contribution.getDefaultLanguage().equals(baseContribution.getDefaultLanguage())) {
        formerId = contribution.getId();
        contribution.setId(-1L);
      }
    }

    if(existing != null && !existing.getId().equals(contribution.getId())) {
      if(contribution.getTagType().getEType() == ETagType.SIMPLE_TAG) {
        if (!values.isBlank(contribution.getId())) {
          merge(contribution.getId(), existing.getId(), contributor);
          existing.setVersion(new Date().getTime());
        } else {

          for (Tag parent : contribution.getParents()) {
            if (existing.getParents().parallelStream().noneMatch(t2 -> t2.getId().equals(parent.getId()))) {
              existing.getParents().add(parent);
            }
          }

          for (Tag child : contribution.getChildren()) {
            if (existing.getParents().parallelStream().noneMatch(t2 -> t2.getId().equals(child.getId()))) {
              existing.getParents().add(child);
            }
          }
        }
      }

      formerContribution = contribution;
      contribution.setId(existing.getId());
      contribution = existing;
    }

    Tag currentTag = retrieve(contribution.getId(), false);
    Contribution c = checkContribution(contribution, contributor, currentGroup);
    Contributor dbContributor = checkContributor(contributor, currentGroup);

    // open transaction and handle the whole save chain
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

    try {
      EModificationStatus status;
      be.webdeb.infra.persistence.model.Tag tag;

      if (c == null) {
        // new tag
        logger.debug("start creation of new tag " + contribution.getDefaultName());
        status = EModificationStatus.CREATE;

        // create contribution super type
        c = initContribution(EContributionType.TAG.id(), contribution.getDefaultName());

        // create argument and binding
        tag = new be.webdeb.infra.persistence.model.Tag();

        tag.setTagType(TTagType.find.byId(
                contribution.getTagType() == null ? ETagType.SIMPLE_TAG.id() : contribution.getTagType().getType()));

        c.setTag(tag);
        tag.setContribution(c);

        // update groups
        updateGroups(contribution, c);
        c.save();

        // set id of tag
        tag.setIdContribution(c.getIdContribution());
        tag.save();

        // set new id for given contribution
        contribution.setId(c.getIdContribution());
        tag.setNames(toTagI18names(contribution, tag));
        if(tag.getTagtype().getEType() == ETagType.SIMPLE_TAG)
          tag.setRewordingNames(toTagRewordingI18names(contribution, tag));
        tag.update();

        if(contribution.getTagType() != null && context != null) {
          if (contribution.getTagType().getEType() == ETagType.CATEGORY_TAG) {
            updateContextHasCategory(formerContribution != null ? formerContribution : contribution, tag, dbContributor, context, formerId);
          } else {
            updateDebateHasTagDebate(formerContribution != null ? formerContribution : contribution, tag, dbContributor, context.getDebate(), formerId);
          }
        }

      } else {
        logger.debug("start update of existing tag " + contribution.getId());
        tag = c.getTag();
        status = EModificationStatus.UPDATE;

        // update sort key
        tag.getContribution().setSortkey(contribution.getDefaultName());
        tag.setNames(toTagI18names(contribution, tag));
        if(tag.getTagtype().getEType() == ETagType.SIMPLE_TAG)
          tag.setRewordingNames(toTagRewordingI18names(contribution, tag));
        tag.update();

        if(contribution.getTagType() != null && context != null) {
          if (contribution.getTagType().getEType() == ETagType.CATEGORY_TAG) {
            updateContextHasCategory(formerContribution != null ? formerContribution : contribution, tag, dbContributor, context, formerId);
          } else {
            updateDebateHasTagDebate(formerContribution != null ? formerContribution : contribution, tag, dbContributor, context.getDebate(), formerId);
          }
        }
      }

      // Create needed links, and delete old one
      createLinksFromApi(contribution, currentTag, contributor);

      // bind contributor to this tag
      bindContributor(tag.getContribution(), dbContributor, status);

      transaction.commit();
      logger.info("saved " + tag.toString());

    } catch (Exception e) {
      logger.error("unable to save tag " + contribution.getDefaultName());
      throw new PersistenceException(PersistenceException.Key.SAVE_TAG, e);
    }
    finally {
      transaction.end();
    }

    return new HashMap<>();
  }

  @Override
  public void save(TagLink link, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save tag link " + link.toString() + " in group public");

    // check if given link contains valid tags
    be.webdeb.infra.persistence.model.Tag parentTag =
        be.webdeb.infra.persistence.model.Tag.findById(link.getParentId());
    if (parentTag == null || parentTag.getTagtype().getEType() != ETagType.SIMPLE_TAG) {
      logger.error("unable to retrieve parent tag " + link.getParentId());
      throw new ObjectNotFoundException(Tag.class, link.getParentId());
    }

    be.webdeb.infra.persistence.model.Tag childTag =
        be.webdeb.infra.persistence.model.Tag.findById(link.getChildId());
    if (childTag == null || childTag.getTagtype().getEType() != ETagType.SIMPLE_TAG) {
      logger.error("unable to retrieve child tag " + link.getChildId());
      throw new ObjectNotFoundException(Tag.class, link.getChildId());
    }

    be.webdeb.infra.persistence.model.TagLink existing = be.webdeb.infra.persistence.model.TagLink.findById(link.getId());

    createLinkFromDbTags(parentTag, childTag, contributor);
  }

  @Override
  public void save(be.webdeb.core.api.contribution.link.ContextHasCategory link, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    ContextHasCategory chc = ContextHasCategory.findById(link.getId());

    if (chc == null) {
      logger.error("unable to retrieve lin k " + link.getId());
      throw new ObjectNotFoundException(ContextHasCategory .class, link.getId());
    }

    Contribution c = checkContribution(link, contributor, currentGroup);
    Contributor dbContributor = checkContributor(contributor, currentGroup);

    // open transaction and handle the whole save chain
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());

    try {
      chc.setOrder(link.getOrder());
      chc.update();

      // update groups
      updateGroups(link, c);
      c.update();
      // bind contributor to this link
      bindContributor(chc.getContribution(), dbContributor, EModificationStatus.UPDATE);

      transaction.commit();
      logger.info("saved context has category " + link.getId());

    } catch (Exception e) {
      logger.error("unable to save context has category link " + link.getId());
      throw new PersistenceException(PersistenceException.Key.SAVE_CONTEXT_HAS_CATEGORY_LINK, e);
    }
    finally {
      transaction.end();
    }
  }

  /*
   * GETTERS FOR PREDEFINED VALUES
   */

  @Override
  public List<TagType> getTagTypes() {
    if (tagTypes == null) {
      tagTypes = TTagType.find.all().stream().map(t ->
        factory.createTagType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return tagTypes;
  }

  @Override
  public List<Tag> getParents(Long tag) {
    List<Tag> parents = new ArrayList<>();
    be.webdeb.infra.persistence.model.Tag tagDb = be.webdeb.infra.persistence.model.Tag.findById(tag);
    if(tagDb != null){
      parents = buildList(tagDb.getParentsAsLinks().stream()
          .map(be.webdeb.infra.persistence.model.TagLink::getTagParent).collect(Collectors.toList()));
    }
    return parents;
  }

  @Override
  public List<Tag> getChildren(Long tag) {
    List<Tag> children = new ArrayList<>();
    be.webdeb.infra.persistence.model.Tag tagDb = be.webdeb.infra.persistence.model.Tag.findById(tag);

    if(tagDb != null){
      children = buildList(tagDb.getChildrenAsLinks().stream()
          .map(be.webdeb.infra.persistence.model.TagLink::getTagChild).collect(Collectors.toList()));
    }
    return children;
  }

  @Override
  public List<TagLink> getParentLinks(Long tag) {
    List<TagLink> parents = new ArrayList<>();
    be.webdeb.infra.persistence.model.Tag tagDb = be.webdeb.infra.persistence.model.Tag.findById(tag);
    if(tagDb != null){
      parents = buildLinkList(tagDb.getParentsAsLinks());
    }
    return parents;
  }

  @Override
  public List<TagLink> getParentsLinks(SearchContainer query) {
    return buildLinkList(be.webdeb.infra.persistence.model.Tag.getLinks(query, true));
  }

  @Override
  public List<TagLink> getChildLinks(Long tag) {
    List<TagLink> children = new ArrayList<>();
    be.webdeb.infra.persistence.model.Tag tagDb = be.webdeb.infra.persistence.model.Tag.findById(tag);

    if(tagDb != null){
      children = buildLinkList(tagDb.getChildrenAsLinks());
    }
    return children;
  }

  @Override
  public List<TagLink> getChildrenLinks(SearchContainer query) {
    return buildLinkList(be.webdeb.infra.persistence.model.Tag.getLinks(query, false));
  }

  @Override
  public List<TagLink> getRandomChildren(Long tag) {
    return buildLinkList(be.webdeb.infra.persistence.model.Tag.getRandomChildren(tag));
  }

  @Override
  public List<TagName> getTagRewordingNames(Long tag){
    be.webdeb.infra.persistence.model.Tag f = be.webdeb.infra.persistence.model.Tag.findById(tag);
    List<TagName> names = new ArrayList<>();
    if(f != null){
      names = f.getRewordingNames().stream()
          .map(n -> factory.createTagName(n.getLang(), n.getName())).collect(Collectors.toList());
    }
    return names;
  }

  @Override
  public List<Citation> getCitationsFromTagAndText(Long tag, Long text) {
    return buildCitationList(be.webdeb.infra.persistence.model.Citation.findCitationsFromTagAndText(tag, text));
  }

  @Override
  public HierarchyTree getTagHierarchyTree(Long tag){
    return madeTagHierarchyTree(factory.createHierarchyTree(), tag);
  }

  @Override
  public EHierarchyCode checkHierarchy(Tag tag, Tag hierarchy, boolean isParent){
    EHierarchyCode hierarchyCode;

    if(tag != null && hierarchy != null) {
      HierarchyTree tagTree = factory.createHierarchyTree();
      HierarchyTree hierarchyTree = factory.createHierarchyTree();
      tagTree = madeTagHierarchyTree(tagTree, tag.getId());
      hierarchyTree = madeTagHierarchyTree(hierarchyTree, hierarchy.getId());

      HierarchyNode hierarchyNode = hierarchyTree.searchNode(hierarchy.getId());
      if(isParent){
        hierarchyCode = tagTree.addChildToNode(hierarchyNode, hierarchyTree, tag.getId());
      }else{
        hierarchyCode = tagTree.addParentToNode(hierarchyNode, hierarchyTree, tag.getId());
      }
    }else{
      hierarchyCode = EHierarchyCode.NULL_OR_NO_ID;
    }
    return hierarchyCode;
  }

  @Override
  public int getNbContributions(Long tag, Long contributor, int group) {
    return be.webdeb.infra.persistence.model.Tag.getNbContributions(tag, contributor, group);
  }

  @Override
  public int getNbContributionsByType(Long tag, EContributionType type, Long contributor, int group) {
    return be.webdeb.infra.persistence.model.Tag.getNbContributionsByType(tag, type, contributor, group);
  }

  @Override
  public Map<Long, Set<Long>> findNewTagsChildren() {
    return be.webdeb.infra.persistence.model.Tag.findNewTagsChildren();
  }

  @Override
  public List<be.webdeb.core.api.debate.Debate> getDebates(Long tag) {
    return buildDebateList(be.webdeb.infra.persistence.model.Tag.getDebates(tag));
  }

  @Override
  public List<be.webdeb.core.api.debate.Debate> getDebates(SearchContainer query) {
    return buildDebateList(be.webdeb.infra.persistence.model.Tag.getDebates(query));
  }

  private HierarchyTree madeTagHierarchyTree(HierarchyTree tree, Long tag){
    be.webdeb.infra.persistence.model.Tag tagDb = be.webdeb.infra.persistence.model.Tag.findById(tag);
    tree = fillHierarchy(tree, tree.getRoot(), true, tagDb);
    tree.removeIfNodeAsRootAndOthersAsParents(tag);
    return tree;
  }

  private HierarchyTree fillHierarchy(HierarchyTree tree, HierarchyNode currentNode, boolean isParent, be.webdeb.infra.persistence.model.Tag tag){
    HierarchyNode node = getNodeFromTag(tag);
    if(node != null && currentNode != null) {
      //logger.debug(node.toString());
      EHierarchyCode code = tree.addNodeInHierarchy(node, null, currentNode.getId(), isParent);
      //logger.debug(code+"");
      if(code == EHierarchyCode.OK) {
        List<be.webdeb.infra.persistence.model.Tag> parents = tag.getParentsAsTags();
        List<be.webdeb.infra.persistence.model.Tag> children = tag.getChildrenAsTags();
        //logger.debug(parents.isEmpty()+"");
        if (!parents.isEmpty()) {
          for (be.webdeb.infra.persistence.model.Tag parent : parents) {
            tree = fillHierarchy(tree, node, false, parent);
          }
        } else {
          tree.addNodeToRoot(node);
        }
        for (be.webdeb.infra.persistence.model.Tag child : children) {
          tree = fillHierarchy(tree, node, true, child);
        }
      }
    }
    return tree;
  }

  private HierarchyNode getNodeFromTag(be.webdeb.infra.persistence.model.Tag tag){
    HierarchyNode node = null;
    if(tag != null){
      node = factory.createHierarchyNode(tag.getIdContribution(), tag.getDefaultName());
    }
    return node;
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Update debate has tag debate link
   *
   * @param apiTag an API tag with data to store
   * @param tag a DB tag recipient (may contain data to be updated)
   * @param dbContributor the current user as db object
   * @param debate a debate multiple
   * @param formerId the former id of the category
   */
  private void updateDebateHasTagDebate(Tag apiTag, be.webdeb.infra.persistence.model.Tag tag, Contributor dbContributor, Debate debate, Long formerId)
          throws PersistenceException, PermissionException {
      DebateLink link = DebateLink.findUniqueByDebateAndTag(debate.getIdContribution(), tag.getIdContribution());
      DebateLink former = DebateLink.findUniqueByDebateAndTag(debate.getIdContribution(), formerId);

      if(link == null) {
        //List<DebateLink> links = DebateLink.findByDebate(debate.getIdContribution());
        Contribution c = initContribution(EContributionType.DEBATE_HAS_TAG_DEBATE.id(), null);
        link = new DebateLink(debate, tag);

        forceUpdateGroups(apiTag, c);
        c.setDebateHasTagDebate(link);
        link.setContribution(c);
        c.save();

        link.setIdContribution(c.getIdContribution());
        //link.set(links.isEmpty() ? 0 : links.get(links.size() - 1).getOrder() + 1);
        link.save();

        forceContributionUpdate(debate.getContribution());
        forceContributionUpdate(tag.getContribution());

        bindContributor(c, dbContributor, EModificationStatus.CREATE);
      } else {
        forceUpdateGroups(apiTag, link.getContribution());
        link.getContribution().update();
      }

      // delete former link
      if(apiTag.getId() > 0 && !apiTag.getId().equals(tag.getIdContribution())) {
        DebateLink formerLink = DebateLink.findUniqueByDebateAndTag(debate.getIdContribution(), apiTag.getId());

        if(formerLink != null) {
          remove(formerLink.getIdContribution(), EContributionType.DEBATE_HAS_TAG_DEBATE, dbContributor.getIdContributor());
        }
      }

      if(formerId != null) {
        if(former != null) {
          former.delete();
        }

        ArgumentJustification.findLinksForContextAndSubContext(debate.getIdContribution(), formerId).forEach(l -> {
          l.setSubContext(tag);
          l.update();
        });

        CitationJustification.findLinksForContextAndSubContext(debate.getIdContribution(), formerId).forEach(l -> {
          l.setSubContext(tag);
          l.update();
        });
      }
  }

  /**
   * Update the context has category if tag is a category
   *
   * @param apiTag an API tag with data to store
   * @param tag a DB tag recipient (may contain data to be updated)
   * @param dbContributor the current user as db object
   * @param context the context contribution db object if tag is category
   * @param formerId the former id of the category
   */
  private void updateContextHasCategory(Tag apiTag, be.webdeb.infra.persistence.model.Tag tag, Contributor dbContributor, Contribution context, Long formerId)
          throws PersistenceException, PermissionException {
    if(apiTag.getTagType() != null && apiTag.getTagType().getEType() == ETagType.CATEGORY_TAG) {
      ContextHasCategory chc = ContextHasCategory.findByContextAndCategory(context.getIdContribution(), tag.getIdContribution());
      ContextHasCategory former = ContextHasCategory.findByContextAndCategory(context.getIdContribution(), formerId);

      if(chc == null) {
        List<ContextHasCategory> contextCategories = ContextHasCategory.findByContext(context.getIdContribution());

        Contribution c = initContribution(EContributionType.CONTEXT_HAS_CATEGORY.id(), null);
        chc = new ContextHasCategory(context, tag);

        forceUpdateGroups(apiTag, c);
        c.setContextHasCategory(chc);
        chc.setContribution(c);
        c.save();

        chc.setIdContribution(c.getIdContribution());
        chc.setOrder(former != null ? former.getOrder() : contextCategories.isEmpty() ? 0 : contextCategories.get(contextCategories.size() - 1).getOrder() + 1);
        chc.save();

        forceContributionUpdate(context);
        forceContributionUpdate(tag.getContribution());

        bindContributor(c, dbContributor, EModificationStatus.CREATE);
      } else {
        forceUpdateGroups(apiTag, chc.getContribution());
        chc.getContribution().update();
      }

      // delete former link
      if(apiTag.getId() > 0 && !apiTag.getId().equals(tag.getIdContribution())) {
        ContextHasCategory formerChc = ContextHasCategory.findByContextAndCategory(context.getIdContribution(), apiTag.getId());

        if(formerChc != null) {
          remove(formerChc.getIdContribution(), EContributionType.CONTEXT_HAS_CATEGORY, dbContributor.getIdContributor());
        }
      }

      if(formerId != null) {
        if(former != null) {
          former.delete();
        }

        ArgumentJustification.findLinksForContextAndCategory(context.getIdContribution(), formerId).forEach(link -> {
          link.setCategory(tag);
          link.update();
        });

        CitationJustification.findLinksForContextAndCategory(context.getIdContribution(), formerId).forEach(link -> {
          link.setCategory(tag);
          link.update();
        });
      }
    }

  }

  /**
   * Create or update given tag links from api tag
   *
   * @param apiTag an api tag
   * @param currentTag the current state of tag
   * @param contributor the contributor id that asked to save the contribution
   */
  private void createLinksFromApi(Tag apiTag, Tag currentTag, Long contributor) throws PermissionException, PersistenceException {

    if(apiTag.getTagType().getEType() == ETagType.SIMPLE_TAG) {

      if(currentTag != null) {
        for (TagLink parent : currentTag.getParentsAsLink()) {
          if (apiTag.getParents().stream().noneMatch(l -> l.getId().equals(parent.getId())))
            remove(parent.getId(), EContributionType.TAG_LINK, contributor);
        }

        for (TagLink child : currentTag.getChildrenAsLink()) {
          if (apiTag.getChildren().stream().noneMatch(l -> l.getId().equals(child.getId())))
            remove(child.getId(), EContributionType.TAG_LINK, contributor);
        }
      }

      for(Tag parent : apiTag.getParents()) {
        createLinkFromApi(parent, apiTag, contributor);
      }

      for(Tag child : apiTag.getChildren()) {
        createLinkFromApi(apiTag, child, contributor);
      }
    }
  }

  /**
   * Create a link between tags from api, if this link doesn't already exists
   *
   * @param apiParent the tag parent of the link
   * @param apiChild the tag child of the link
   * @param contributor the contributor id that asked to save the contribution
   */
  private void createLinkFromApi(Tag apiParent, Tag apiChild, Long contributor) throws PermissionException, PersistenceException {

    be.webdeb.infra.persistence.model.Tag parent = createOrFindFromApiTag(apiParent, contributor);
    be.webdeb.infra.persistence.model.Tag child = createOrFindFromApiTag(apiChild, contributor);

    if(parent == null || parent.getTagtype().getEType() != ETagType.SIMPLE_TAG) {
      throw new ObjectNotFoundException(Tag.class, -1L);
    }

    if(child == null || child.getTagtype().getEType() != ETagType.SIMPLE_TAG) {
      throw new ObjectNotFoundException(Tag.class, -1L);
    }

    createLinkFromDbTags(parent, child, contributor);
  }

  /**
   * Create or find a tag from api tag
   *
   * @param apiTag an api tag
   * @param contributor the contributor id that asked to save the contribution
   * @return the corresponding db tag
   */
  private be.webdeb.infra.persistence.model.Tag createOrFindFromApiTag(Tag apiTag, Long contributor) throws PermissionException, PersistenceException {

    be.webdeb.infra.persistence.model.Tag tag =
            be.webdeb.infra.persistence.model.Tag.findByCompleteNameAndLang(apiTag.getDefaultName(), apiTag.getDefaultLanguage(), ETagType.SIMPLE_TAG);

    if(tag == null) {
      save(apiTag, contributor);

      tag = be.webdeb.infra.persistence.model.Tag.findById(apiTag.getId());
    }

    return tag;
  }

  /**
   * Save a tag link between two db tags
   *
   * @param parent the parent tag of the link
   * @param child the child tag of the link
   */
  private void createLinkFromDbTags(be.webdeb.infra.persistence.model.Tag parent, be.webdeb.infra.persistence.model.Tag child, Long contributor) throws PermissionException, PersistenceException {

    if(be.webdeb.infra.persistence.model.TagLink.findByParentChild(parent.getIdContribution(), child.getIdContribution()) == null) {

      Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
      try {
        EModificationStatus status;
        be.webdeb.infra.persistence.model.TagLink dbLink;
        Contributor dbContributor = checkContributor(contributor, Group.getPublicGroup().getIdGroup());

        status = EModificationStatus.CREATE;
        // create and save contribution supertype
        Contribution contribution = initContribution(EContributionType.TAG_LINK.id(), null);
        try {
          contribution.save();
        } catch (Exception e) {
          logger.error("error while saving contribution for tag link between " +
                  parent.getIdContribution() + " and " + child.getIdContribution());
          throw new PersistenceException(PersistenceException.Key.SAVE_TAG_LINK, e);
        }

        // create link
        dbLink = new be.webdeb.infra.persistence.model.TagLink();
        contribution.setTagLink(dbLink);
        dbLink.setContribution(contribution);
        dbLink = updateLink(contribution.getTagLink(), parent, child);
        contribution.addGroup(Group.getPublicGroup());
        // save link
        try {
          contribution.save();
          dbLink.setIdContribution(contribution.getIdContribution());
          dbLink.save();
          logger.debug("saved tag link " + dbLink);

        } catch (Exception e) {
          logger.error("error while saving link between " + parent.getIdContribution() + " and " + child.getIdContribution());
          throw new PersistenceException(PersistenceException.Key.SAVE_TAG_LINK, e);
        }
        // save link to contributor
        bindContributor(dbLink.getContribution(), dbContributor, status);

        forceContributionUpdate(parent.getContribution());
        forceContributionUpdate(child.getContribution());

        transaction.commit();
        logger.info("saved " + dbLink.toString());

      } finally {
        transaction.end();
      }
    }
  }

  /**
   * Update given db tag link with api tag link
   *
   * @param link a DB link to update
   * @param parent the parent DB tag (corresponding to apiLink.getParent)
   * @param child the "to" DB tag (corresponding to apiLink.getChild)
   * @return the updated DB tag link
   */
  private be.webdeb.infra.persistence.model.TagLink updateLink(be.webdeb.infra.persistence.model.TagLink link,
                                                                    be.webdeb.infra.persistence.model.Tag parent,
                                                                    be.webdeb.infra.persistence.model.Tag child) {
    link.setTagParent(parent);
    link.setTagChild(child);
    return link;
  }

  /**
   * Helper method to build a list of API tag from DB tags. All uncastable elements are ignored.
   *
   * @param tags a list of DB tags
   * @return a list of API tags with elements that could have actually been casted to API element (may be
   * empty)
   */
  private List<Tag> buildList(List<be.webdeb.infra.persistence.model.Tag> tags) {
    List<Tag> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.Tag f : tags) {
      try {
        result.add(mapper.toTag(f));
      } catch (FormatException e) {
        logger.error("unable to cast tag " + f.getIdContribution() + " Reason: " + e.getMessage(), e);
      }
    }
    return result;
  }

  /**
   * Helper method to build a list of API tag link from DB tag links. All uncastable elements are ignored.
   *
   * @param links a list of DB tag links
   * @return a list of API tag links with elements that could have actually been casted to API element (may be
   * empty)
   */
  private List<TagLink> buildLinkList(List<be.webdeb.infra.persistence.model.TagLink> links) {
    List<TagLink> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.TagLink l : links) {
      try {
        result.add(mapper.toTagLink(l));
      } catch (FormatException e) {
        logger.error("unable to cast tag link " + l.getIdContribution() + " Reason: " + e.getMessage(), e);
      }
    }
    return result;
  }

  /**
   * Create a list of TagI18name for given db tag from given api tag
   *
   * @param api the api tag with a list of names
   * @param db the db tag to which those names will be bound
   * @return the db names from given api names
   */
  protected List<TagI18name> toTagI18names(Tag api, be.webdeb.infra.persistence.model.Tag db) {
    return api.getNames().entrySet().stream().map(n -> toTagI18name(db, n.getKey(), n.getValue())).collect(Collectors.toList());
  }

  /**
   * Convert given tag name into a db tagI18name
   *
   * @param db the db tag holding the link to the new tag nae
   * @param lang the lang to convert
   * @param name the name to convert
   * @return the mapped tagI18name for this given name and linked to given db tag
   */
  private TagI18name toTagI18name(be.webdeb.infra.persistence.model.Tag db, String lang, String name) {
    return new TagI18name(db, lang, name);
  }

  /**
   * Create a list of TagRewordingI18name for given db tag from given api tag
   *
   * @param api the api tag with a list of rewording names
   * @param db the db tag to which those rewording names will be bound
   * @return the db rewording names from given api rewording names
   */
  protected List<TagRewordingI18name> toTagRewordingI18names(Tag api, be.webdeb.infra.persistence.model.Tag db) {
    List<TagRewordingI18name> names = new ArrayList<>();
    api.getRewordingNames().entrySet().forEach(n ->
        names.addAll(n.getValue().stream().map(
            v -> toTagRewordingI18name(db, v.getLang(), v.getName())).collect(Collectors.toList())));
    return names;
  }

  /**
   * Convert given tag rewording name into a db tagRewordingI18name
   *
   * @param db the db tag holding the link to the new tag rewording name
   * @param lang the lang to convert
   * @param name the name to convert
   * @return the mapped tagRewordingI18name for this given rewording name and linked to given db tag
   */
  private TagRewordingI18name toTagRewordingI18name(be.webdeb.infra.persistence.model.Tag db, String lang, String name) {
    return new TagRewordingI18name(db, lang, name);
  }
}
