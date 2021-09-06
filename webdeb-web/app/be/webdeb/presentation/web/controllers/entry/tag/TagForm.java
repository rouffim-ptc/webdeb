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

package be.webdeb.presentation.web.controllers.entry.tag;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.model.Group;

import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper class for forms used to encode a new tag into the database.
 *
 * Note that all supertype getters corresponding to predefined values (ie, types) are sending
 * ids instead of language-dependent descriptions.
 *
 * @author Martin Rouffiange
 */
public class TagForm extends TagHolder {

  private String formTitle;
  private String categoryName;
  // list of tags having the same name
  private boolean isNotSame = false;
  private List<TagHolder> candidates = new ArrayList<>();

  private SimpleTagForm parentThatNameMatch = null;
  private SimpleTagForm childThatNameMatch = null;

  /**
   * Play / JSON compliant constructor
   */
  public TagForm() {
    super();
  }

  /**
   * Constructor with user lang
   */
  public TagForm(String lang) {
    super();
    TagNameForm name = new TagNameForm(lang, "");
    this.tagNames.add(name);
  }

  /**
   * Constructor with context and user lang
   */
  public TagForm(Long contextId, ETagType type, String lang) {
    super();
    tagType = type.id();
    this.contextId = contextId;
    this.lang = lang;
    initLinks();
  }

  /**
   * Constructor. Create a new form object for given tag. Calls super then init beforehand.
   *
   * @param tag a Tag
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagForm(Tag tag, WebdebUser user, String lang) {
    super(tag, user, lang);
    initLinks();
  }

  /**
   * Constructor. Create a new form object for given tag. Calls super then init beforehand.
   *
   * @param tag a Tag
   * @param contextId a context contribution id
   * @param lang 2-char ISO code of context language (among play accepted languages)
   */
  public TagForm(Tag tag, Long contextId, WebdebUser user, String lang) {
    super(tag, user, lang);
    this.contextId = contextId;
    initLinks();
  }

  /**
   * Validator (called from form submit)
   *
   * @return null if no error has been found, otherwise the list of found errors
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    if (values.isBlank(tagType)) {
      errors.put("tagType", Collections.singletonList(new ValidationError("tagType", "tag.error.tagType")));
    }

    if(values.isBlank(id) && (tagType == ETagType.CATEGORY_TAG.id() || tagType == ETagType.SUB_DEBATE_TAG.id())){

      if (values.isBlank(categoryName)) {
        errors.put("categoryName", Collections.singletonList(new ValidationError("categoryName", "general.required")));
      }

    } else {
      boolean noName = true;
      for (int i = 0; i < tagNames.size(); i++) {
        TagNameForm nameForm = tagNames.get(i);
        String fieldNames = "tagNames[" + i + "].";
        String fieldName = "";
        String message = "";

        if (!values.isBlank(nameForm.getLang()) || !values.isBlank(nameForm.getName())) {
          noName = false;
          if (values.isBlank(nameForm.getLang())) {
            if (i == 0) {
              tagNames.get(i).setLang(lang);
            } else {
              fieldName = fieldNames + "lang";
              message = "tag.error.name.lang";
              errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
            }
          }
          if (values.isBlank(nameForm.getName())) {
            fieldName = fieldNames + "name";
            message = "tag.error.name.name";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
          } else {
            if(tagType == ETagType.SIMPLE_TAG.id() && helper.checkTagName(nameForm.getName(), nameForm.getLang())) {
              fieldName = fieldNames + "name";
              errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.begin")));
            }
          }
            /*
          } else if (values.isNotValidTagName(nameForm.getName())) {
            fieldName = fieldNames + "name";
            message = "tag.error.name.name.chars";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
          }
          */

        }
        // Search if there is multiple names in the same language
        if (tagNames.stream().filter(n -> !n.isEmpty()).anyMatch(n -> (n != nameForm && n.getLang().equals(nameForm.getLang())))) {
          errors.put(fieldNames + "lang", Collections.singletonList(new ValidationError("tagNames", "actor.error.lang.twice")));
        }
      }
      if (noName) {
        errors.put("tagNames[0].name", Collections.singletonList(new ValidationError("tagNames[0].name", "tag.error.name")));
      }

      for (int i = 0; i < tagRewordingNames.size(); i++) {
        TagNameForm nameForm = tagRewordingNames.get(i);
        String fieldNames = "tagRewordingNames[" + i + "].";
        String fieldName = "";
        String message = "";

        if (!values.isBlank(nameForm.getLang()) || !values.isBlank(nameForm.getName())) {
          if (values.isBlank(nameForm.getLang())) {
            fieldName = fieldNames + "lang";
            message = "tag.error.name.lang";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
          }
          if (values.isBlank(nameForm.getName())) {
            fieldName = fieldNames + "name";
            message = "tag.error.name.name";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, message)));
          } else {
            if(tagType == ETagType.SIMPLE_TAG.id() && helper.checkTagName(nameForm.getName(), nameForm.getLang())) {
              fieldName = fieldNames + "name";
              errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.begin")));
            }
          }
        }
        // Search if there is multiple names in the same language
        if (tagRewordingNames.stream().filter(n -> !n.isEmpty()).anyMatch(n -> (n != nameForm && n.getLang().equals(nameForm.getLang()) && n.getName().equals(nameForm.getName())))) {
          errors.put(fieldNames + "name", Collections.singletonList(new ValidationError(fieldNames + "name", "tag.error.rewordingname.twice")));
        }
      }

      if(parents != null) {
        for (int iTag = 0; iTag < parents.size(); iTag++) {
          if (!parents.get(iTag).isEmpty() && values.isBlank(parents.get(iTag).getTagId()) && helper.checkTagName(parents.get(iTag).getName(), lang)) {
            String fieldName = "parents[" + iTag + "].name";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.begin")));
          }
        }
      }

      if(children != null) {
        for (int iTag = 0; iTag < children.size(); iTag++) {
          if (!children.get(iTag).isEmpty() && values.isBlank(children.get(iTag).getTagId()) && helper.checkTagName(children.get(iTag).getName(), lang)) {
            String fieldName = "children[" + iTag + "].name";
            errors.put(fieldName, Collections.singletonList(new ValidationError(fieldName, "tag.error.name.begin")));
          }
        }
      }

      // Analyse parents hierarchy
      /*if(tagType != ETagType.ROOT.id()) {
        list = helper.checkTagLinkFromSimpleForm(parents, id, lang, "parents", true);
        if (list != null) {
          list.forEach(e -> errors.put(e.key(), Collections.singletonList(new ValidationError(e.key(), e.message()))));
        }
      }*/

      // Analyse children hierarchy
      /*if(tagType != ETagType.LEAF.id()) {
        list = helper.checkTagLinkFromSimpleForm(children, id, lang, "children", false);
        if (list != null) {
          list.forEach(e -> errors.put(e.key(), Collections.singletonList(new ValidationError(e.key(), e.message()))));
        }
      }*/
      }

    return errors.isEmpty() ? null : errors;
  }

  /**
   * Save a tag into the database. This id is updated if it was not set before.
   *
   * @param contributor the contributor id that ask to save this contribution
   * @param group the group where save the potential tag link
   * @return the map of Contribution type and a list of tags (as Contribution) that have been created during
   * this insertion (for all unknown tags), a empty list if none had been created
   *
   * @throws FormatException if this contribution has invalid field values (should be pre-checked before-hand)
   * @throws PermissionException if given contributor may not perform this action or if such action would cause
   * integrity problems
   * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
   */
  public Map<Integer, List<Contribution>> save(Long contributor, int group) throws FormatException, PermissionException, PersistenceException {
    logger.debug("try to save " + toString() + " with version " + version + " in group " + inGroup);

    Tag tag = tagType == ETagType.CATEGORY_TAG.id() || tagType == ETagType.SUB_DEBATE_TAG.id() ?
            tagFactory.getTagCategory() : tagFactory.getTag();
    tag.setId(id != null ? id : -1L);
    tag.setVersion(version);
    tag.addInGroup(group);

    if(tagType == ETagType.CATEGORY_TAG.id() || tagType == ETagType.SUB_DEBATE_TAG.id()) {
      ((TagCategory) tag).setCurrentContextId(contextId);

      if(values.isBlank(id)){
       tag.addName(lang, categoryName);
      }
      name = categoryName;
    }
    if((tagType != ETagType.CATEGORY_TAG.id() && tagType != ETagType.SUB_DEBATE_TAG.id()) || !values.isBlank(id)) {
      tagNames.stream().filter(TagNameForm::isValid).forEach(n -> tag.addName(n.getLang(), n.getName()));
      tagRewordingNames.stream().filter(TagNameForm::isValid).forEach(n -> tag.addRewordingName(n.getLang(), n.getName()));
      name = tagNames.isEmpty() ? "" : tagNames.get(0).getName();
    }

    try {
      tag.setTagType(tagFactory.getTagType(tagType));
    } catch (NumberFormatException | FormatException e) {
      logger.error("unknown tag type id" + tagType, e);
    }

    if(parents != null) {
      tag.initParents();
      for (SimpleTagForm f : parents) {
        if (f != null) {
          try {
            if (!values.isBlank(f.getTagId())) {
              tag.addParent(tagFactory.retrieve(f.getTagId()));
            } else {
              tag.addParent(f.toTag(lang));
            }
          } catch (FormatException e) {
            logger.debug("error while adding parent to tag ", e);
            throw new PersistenceException(PersistenceException.Key.SAVE_TAG, e);
          }
        }
      }
    }

    if(children != null) {
      tag.initChildren();
      for (SimpleTagForm f : children) {
        if (f != null) {
          if (!values.isBlank(f.getTagId())) {
            try {
              tag.addChild(tagFactory.retrieve(f.getTagId()));
            } catch (FormatException e) {
              logger.debug("error while adding child to tag ", e);
              throw new PersistenceException(PersistenceException.Key.SAVE_TAG, e);
            }
          } else {
            tag.addChild(f.toTag(lang));
          }
        }
      }
    }

    Map<Integer, List<Contribution>> tags = tag.save(contributor, inGroup);
    // do not forget to update the id, since the controller needs it for redirection
    id = tag.getId();
    return tags;
  }

  /*
   * SETTERS
   */

  /**
   * Set the tag name
   *
   * @param name the tag name
   */
  public void setTagName(String name) {
    this.name = name;
  }

  public void setContextId(Long contextId) {
    this.contextId = contextId;
  }

  /**
   * Set the list of parent tag links
   *
   * @param parents a list of tag links
   */
  public void setParents(List<SimpleTagForm> parents) {
    this.parents = parents;
  }

  /**
   * Set the list of child tag links
   *
   * @param children a list of tag links
   */
  public void setChildren(List<SimpleTagForm> children) {
    this.children = children;
  }

  /**
   * Set the tag type from which this tag originates from
   *
   * @param type the tag type
   */
  public void setTagType(int type) {
    this.tagType = type;
  }

  /**
   * Set the tag names
   *
   * @param names the possibly empty list of tag names
   */
  public void setTagNames(List<TagNameForm> names) {
    tagNames = names;
  }

  /**
   * Set the rewording tag names
   *
   * @param names the possibly empty list of rewording tag names
   */
  public void setTagRewordingNames(List<TagNameForm> names) {
   tagRewordingNames = names;
  }

  /**
   * Get the list of candidate tags having the same name as this tag
   *
   * @return a (possibly empty) list of tag holders
   */
  public List<TagHolder> getCandidates() {
    return candidates;
  }

  /**
   * Set he list of candidate tags having the same name as this tag
   *
   * @param candidates a list of api Tags
   */
  public void setCandidates(List<Tag> candidates, WebdebUser user) {
    this.candidates = candidates
            .stream()
            .map(t -> new TagHolder(t, user, lang))
            .collect(Collectors.toList());
  }

  /**
   * Check whether this tag has been explicitly flagged as a new one (used when other tags exists with same name)
   *
   * @return true if this tag is a new one despite it has the same tag as another one in db
   */
  public boolean getIsNotSame() {
    return isNotSame;
  }

  /**
   * Set whether this tag has been explicitly flagged as a new one (used when other tags exists withsame name)
   *
   * @param notSame true if this tag is a new one despite it has the same name as another one in db
   */
  public void setIsNotSame(boolean notSame) {
    isNotSame = notSame;
  }

  /**
   * Get the parent that name match
   *
   * @return a (possibly null) parent as simple tag form
   */
  public SimpleTagForm getParentThatNameMatch() {
    return parentThatNameMatch;
  }

  /**
   * Set the parent that name match
   *
   * @param parentThatNameMatch a parent as simple tag form
   */
  public void setParentThatNameMatch(SimpleTagForm parentThatNameMatch) {
    this.parentThatNameMatch = parentThatNameMatch;
  }

  /**
   * Get the child that name match (if any)
   *
   * @return a (possibly null) child as simple tag form
   */
  public SimpleTagForm getChildThatNameMatch() {
    return childThatNameMatch;
  }

  /**
   * Set the child that name match
   *
   * @param childThatNameMatch a child as simple tag form
   */
  public void setChildThatNameMatch(SimpleTagForm childThatNameMatch) {
    this.childThatNameMatch = childThatNameMatch;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getFormTitle() {
    return formTitle;
  }

  public void setFormTitle(String formTitle) {
    this.formTitle = formTitle;
  }
}
