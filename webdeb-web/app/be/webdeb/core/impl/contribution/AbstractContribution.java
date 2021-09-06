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

package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentShade;
import be.webdeb.core.api.argument.ArgumentShaded;
import be.webdeb.core.api.argument.EArgumentType;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.contribution.place.Place;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ContributionAccessor;
import org.slf4j.Logger;
import play.api.Play;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This class implements a Contribution in the webdeb system. It implements a set of common
 * methods to handle any type of contributions.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public abstract class AbstractContribution<T extends ContributionFactory, V extends ContributionAccessor>
    implements Contribution {

  /**
   * Max size for actor names and text titles
   */
  public static final int MAX_NAME_SIZE = 255;

  /**
   * Max size for person first names and organization acronyms
   */
  public static final int MAX_FIRSTNAME_SIZE = 60;

  /**
   * Max size for urls
   */
  protected static final int MAX_URL_SIZE = 2048;

  protected static final Logger logger = play.Logger.underlying();

  protected V accessor;
  protected T factory;
  protected ContributorFactory contributorFactory;

  @Inject
  protected MessagesApi i18n = Play.current().injector().instanceOf(MessagesApi.class);

  protected Long id = -1L;
  protected EContributionType type;
  protected Set<Group> groups = new HashSet<>();
  protected ValidationState validated;
  protected boolean locked;
  protected boolean deleted;
  protected String sortkey;
  protected long version = 1L;
  protected Map<EContributionType, Integer> countRelationsMap = null;
  protected Integer nbContributions = null;

  protected List<Tag> tagsAsList = null;
  protected Set<Tag> tags;
  protected List<Place> places;

  /**
   * Abstract constructor
   *
   * @param factory a contribution factory
   * @param accessor a contribution accessor
   */
  public AbstractContribution(T factory, V accessor, ContributorFactory contributorFactory) {
    this.accessor = accessor;
    this.factory = factory;
    this.contributorFactory = contributorFactory;
    id = -1L;
  }

  @Override
  public Long getId() {
    return id == null || id == -1L ? -1L : id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public EContributionType getType() {
    return type;
  }

  @Override
  public ContributionType getContributionType() {
    return factory.getContributionType(type);
  }

  @Override
  public String getContributionTitle(String lang) {
    switch (type){
      case ACTOR:
        return ((Actor) this).getFullname(lang);
      case DEBATE:
        return ((Debate) this).getFullTitle(lang);
      case CITATION:
        return ((Citation) this).getWorkingExcerpt();
      case ARGUMENT:
        return ((Argument) this).getFullTitle();
      case TEXT:
        return ((Text) this).getTitle(lang);
      case TAG :
        return ((Tag) this).getName(lang);
    }

    return "";
  }

  @Override
  public List<Group> getInGroups() {
    return new ArrayList<>(groups);
  }

  @Override
  public void setInGroups(List<Group> groups) {
    if (groups != null) {
      this.groups.clear();
      groups.forEach(this::addInGroup);
    }
  }

  @Override
  public boolean addInGroup(int group) {
    Group g = contributorFactory.retrieveGroup(group);
    if (g != null) {
      addInGroup(g);
      return true;
    }
    return false;
  }

  @Override
  public boolean addInGroupAndUpdate(int group) throws PersistenceException {
    return accessor.addInGroupAndUpdate(id, group);
  }

  @Override
  public boolean isMemberOfGroup(int group) {
    return groups.contains(group);
  }

  @Override
  public boolean isMemberOfAPublicGroup() {
    return accessor.isMemberOfAPublicGroup(id);
  }

  @Override
  public boolean removeFromGroup(int group) {
    return groups.removeIf(g -> g.getGroupId() == group);
  }

  @Override
  public boolean removeFromGroup(int group, Long contributor) throws PermissionException, PersistenceException {
    boolean result = false;
    Group g = contributorFactory.retrieveGroup(group);
    if (g != null) {
      result = groups.remove(g);
      if (result) {
        accessor.removeFromGroup(id, group, contributor);
      }
    }
    return result;
  }

  @Override
  public long getVersion() {
    return version;
  }

  @Override
  public String getVersionAsString(){
    return new SimpleDateFormat("dd/MM/yyyy").format(new Date(version));
  }

  @Override
  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public synchronized Map<EContributionType, Integer> getCountRelationsMap(Long contributorId, int groupId) {
    if(countRelationsMap == null){
      countRelationsMap = accessor.getCountRelationsMap(id, contributorId, groupId);
    }
    return countRelationsMap;
  }

  @Override
  public ValidationState getValidated() {
    return validated;
  }

  @Override
  public void setValidated(ValidationState validated) {
    this.validated = validated;
  }

  @Override
  public Contributor getCreator() {
    ContributionHistory h = accessor.getCreator(id);
    return h != null ? h.getContributor() : null;
  }

  @Override
  public ContributionHistory getLatestContributor() {
    return accessor.getLatestContributor(id);
  }

  @Override
  public List<Contributor> getContributors() {
    return accessor.getContributors(id);
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public boolean isLocked(){
    return locked;
  }

  @Override
  public void setLocked(boolean locked){
    this.locked = locked;
  }

  @Override
  public void remove(Long contributor) throws PermissionException, PersistenceException {
    accessor.remove(id, type, contributor, type.isLink() ? ERemoveOption.KEEP_LINK : ERemoveOption.TOTAL);
  }

  @Override
  public void remove(Long contributor, boolean force) throws PermissionException, PersistenceException {
    accessor.remove(id, type, contributor, type.isLink() && !force ? ERemoveOption.KEEP_LINK : ERemoveOption.TOTAL);
  }

  @Override
  public Set<Tag> getTags() {
    if (tags == null) {
      tags = factory.getContributionsTags(id);
    }
    return tags;
  }

  @Override
  public List<Tag> getTagsAsList() {
    if(tagsAsList == null){
      tagsAsList = new ArrayList<>(getTags());
    }
    return tagsAsList;
  }

  @Override
  public String getTagsAsStringList() {
    return tags.stream().map(Tag::getDefaultName).collect(Collectors.joining(", "));
  }

  @Override
  public void setTags(List<Tag> tags) throws FormatException {
    Set<Tag> backup = getTags();
    try {
      for (Tag f : tags) {
        addTag(f);
      }
    } catch (FormatException e) {
      this.tags = backup;
      throw e;
    }
  }

  @Override
  public void addTag(Tag tag) throws FormatException {
    if(tag != null) {
      if (!tag.isValid().isEmpty()) {
        throw new FormatException(FormatException.Key.UNKNOWN_TAG, type.toString());
      }
      getTags().add(tag);
    }
  }

  @Override
  public void removeTag(Long tag) {
    getTags().removeIf(t -> t.getId().equals(tag));
  }

  @Override
  public void initTags() {
    tags = new HashSet<>();
  }

  @Override
  public List<Place> getPlaces() {
    if (places == null) {
      places = factory.getContributionsPlaces(id);
    }
    return places;
  }

  @Override
  public void setPlaces(List<Place> places) {
    this.places = places;
  }

  @Override
  public void addPlace(Place place) {
    if(getPlaces() != null){
      places.add(place);
    }
  }

  @Override
  public void initPlaces() {
    places = new ArrayList<>();
  }

  @Override
  public String getSortkey() {
    return sortkey;
  }

  @Override
  public void setSortkey(String sortkey) {
    this.sortkey = sortkey;
  }

  @Override
  public Integer getNbContributions() {
    return nbContributions;
  }

  @Override
  public void setNbContributions(Integer nbContributions) {
    this.nbContributions = nbContributions;
  }

  /**
   * Redefine equals at this level since all contributions may have an id. Hashcode method should be overridden
   * in concrete classes
   *
   * @param obj an object to compare to this
   * @return true if given object is considered as equal to this object
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Contribution)) {
      return false;
    }

    Contribution c = (Contribution) obj;
    if (!id.equals(-1L) && !c.getId().equals(-1L)) {
      return id.equals(c.getId());
    }

    return hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 59 * (id != -1L ? Long.hashCode(id) : 71) + Long.hashCode(version);
  }

  /**
   * Add a group into group set
   *
   * @param group a group
   */
  private void addInGroup(Group group) {
    groups.add(group);
  }
}
