/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.core.impl.debate;

import be.webdeb.core.api.argument.Argument;
import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.citation.CitationJustification;
import be.webdeb.core.api.citation.CitationPosition;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.link.ContextHasCategory;
import be.webdeb.core.api.contributor.picture.ContributorPicture;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.*;
import be.webdeb.core.api.tag.TagCategory;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContextContribution;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a Debate.
 *
 * @author Martin Rouffiange
 */
public abstract class AbstractDebate extends AbstractContextContribution<DebateFactory, DebateAccessor> implements Debate {

  protected EDebateType etype;
  protected String description;
  protected String pictureExtension;
  protected boolean isMultiple;
  protected Long pictureId = null;
  protected ContributorPicture picture = null;
  protected DebateShade shade;
  protected List<DebateExternalUrl> externalUrls = null;
  protected List<DebateSimilarity> similars = null;
  protected List<Citation> citationFromPositions = null;
  protected List<CitationPosition> citationPositions = null;

  protected int nbPositionLinks = 0;
  protected int nbJustificationLinks = 0;

  /**
   * Create a Debate instance
   *
   * @param factory the debate factory
   * @param accessor the debate accessor
   * @param actorAccessor an actor accessor (to retrieve/update involved actors)
   * @param contributorFactory the contributor accessor
   */
  AbstractDebate(DebateFactory factory, DebateAccessor accessor, ActorAccessor actorAccessor, ContributorFactory contributorFactory) {
    super(factory, accessor, actorAccessor, contributorFactory);
    type = EContributionType.DEBATE;
  }

  @Override
  public EDebateType getEType() {
    return etype;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getPictureExtension() {
    return pictureExtension;
  }

  @Override
  public void setPictureExtension(String pictureExtension) {
    this.pictureExtension = pictureExtension;
  }

  @Override
  public boolean isMultiple() {
    return isMultiple;
  }

  @Override
  public void isMultiple(boolean multiple) {
    isMultiple = multiple;
  }

  @Override
  public void initExternalUrls() {
    externalUrls = new ArrayList<>();
  }

  @Override
  public List<DebateExternalUrl> getExternalUrls() {
    if(externalUrls == null) {
      externalUrls = accessor.getExternalUrls(id);
    }
    return externalUrls;
  }

  @Override
  public void addExternalUrl(DebateExternalUrl url) {
    getExternalUrls();
    externalUrls.add(url);
  }

  @Override
  public Long getPictureId() {
    return pictureId;
  }

  @Override
  public void setPictureId(Long pictureId) {
    this.pictureId = pictureId;
  }

  @Override
  public ContributorPicture getPicture() {
    if(picture == null){
      picture = contributorFactory.retrieveContributorPicture(pictureId);
    }
    return picture;
  }

  @Override
  public EDebateShade getEShade() {
    return shade != null ? shade.getEType() : EDebateShade.NO_SHADE;
  }

  @Override
  public DebateShade getShade() {
    return shade;
  }

  @Override
  public void setShade(DebateShade shade) {
    this.shade = shade;
  }

  @Override
  public List<DebateSimilarity> getSimilar() {
    if(similars == null) {
      similars = accessor.getSimilarityLinks(id);
    }
    return similars;
  }

  @Override
  public List<Citation> getCitationsFromPositions() {
    return null;
  }

  @Override
  public List<Citation> getCitationsFromPositions(Long subDebate) {
    return null;
  }

  @Override
  public List<CitationPosition> getAllCitationPositionLinks(Long subDebate) {
    return null;
  }

  @Override
  public List<CitationPosition> getActorCitationPositions(Long actor) {
    return null;
  }

  @Override
  public int getNbPositionLinks() {
    return nbPositionLinks;
  }

  @Override
  public int getNbJustificationLinks() {
    return nbJustificationLinks;
  }

  @Override
  public void setNbPositionLinks(Integer nbPositions) {
    this.nbPositionLinks = nbPositions != null ? nbPositions : 0;
  }

  @Override
  public void setNbJustificationLinks(Integer nbJustifications) {
    this.nbJustificationLinks = nbJustifications != null ? nbJustifications : 0;
  }

  @Override
  public List<Text> findLinkedTexts() {
    return accessor.findLinkedTexts(id);
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
    List<String> isValid = isValid();
    if (!isValid.isEmpty()) {
      logger.error("debate contains errors " + isValid.toString());
      throw new FormatException(FormatException.Key.DEBATE_ERROR, String.join(",", isValid.toString()));
    }
    return accessor.save(this, currentGroup, contributor);
  }

  @Override
  public int hashCode() {
    return 107 * (id != -1L ? id.hashCode() : 83);
  }


  @Override
  public String toString() {
    return "debate {" +
            "id=" + id +
            '}';
  }
}
