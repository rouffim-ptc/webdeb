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

package be.webdeb.core.impl.debate;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.tag.ETagType;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;
import play.i18n.Lang;

import java.util.*;

/**
 * This class implements a DebateTag.
 *
 * @author Martin Rouffiange
 */
public class ConcreteDebateTag extends AbstractDebate implements DebateTag {

  private Long tagId;
  private Tag tag = null;
  private Long linkId;

  private Debate superDebate = null;

  private TagAccessor tagAccessor;

  /**
   * Create a Debate instance
   *
   * @param factory the debate factory
   * @param accessor the debate accessor
   * @param actorAccessor an actor accessor (to retrieve/update involved actors)
   * @param tagAccessor a tag accessor (to retrieve/update involved tags)
   * @param contributorFactory the contributor accessor
   */
  ConcreteDebateTag(DebateFactory factory, DebateAccessor accessor, ActorAccessor actorAccessor, TagAccessor tagAccessor, ContributorFactory contributorFactory) {
    super(factory, accessor, actorAccessor, contributorFactory);
    this.tagAccessor = tagAccessor;
    type = EContributionType.DEBATE;
    etype = EDebateType.TAG_DEBATE;
  }

  @Override
  public String getFullTitle() {
    return getTitle(null);
  }

  @Override
  public String getFullTitle(String lang) {
    return getTitle(lang) ;
  }

  @Override
  public String getTitle(String lang) {
    return getTag().getName(lang);
  }

  @Override
  public Long getTitleContributionId() {
    return tagId;
  }

  @Override
  public Long getTagId() {
    return tagId;
  }

  @Override
  public void setTagId(Long tagId) {
    this.tagId = tagId;
  }

  @Override
  public Tag getTag() {
    if(tag == null) {
      tag = tagAccessor.retrieve(tagId, false);
    }
    return tag;
  }

  @Override
  public Long getLinkId() {
    return linkId;
  }

  @Override
  public void setLinkId(Long link) {
    this.linkId = link;
  }

  @Override
  public Long getCurrentSuperContextId() {
    return superDebate != null ? superDebate.getId() : null;
  }

  @Override
  public Debate getCurrentSuperDebate() {
    return superDebate;
  }

  @Override
  public void setCurrentSuperDebate(Debate debate) {
    this.superDebate = debate;
  }

  @Override
  public List<Debate> getSuperDebates() {
    return null;
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();

    if (factory.getValuesHelper().isBlank(tagId)) {
      fieldsInError.add("tag id is null");
    }

    return fieldsInError;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + tagId.hashCode();
  }


  @Override
  public String toString() {
    return super.toString() + " with tag " + tagId;
  }

}
