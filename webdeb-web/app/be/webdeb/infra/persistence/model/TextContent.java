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

package be.webdeb.infra.persistence.model;

import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "text_content")
@Unqueryable
public class TextContent extends Model {

  @Id
  @Column(name = "filename")
  private String filename;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_contribution", referencedColumnName = "id_contribution")
  private Text text;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_contributor")
  private Contributor contributor;


  /**
   * Create a (public) text content
   *
   * @param text a text
   */
  public TextContent(Text text) {
    this(text, null, "");
  }

  /**
   * Create a text content for given contributor
   *
   * @param text a text
   * @param contributor a contributor
   */
  public TextContent(Text text, Contributor contributor) {
    this(text, contributor, "");
  }

  /**
   * Create a text content for given contributor
   *
   * @param text a text
   * @param contributor a contributor
   * @param suffix a filename suffix to append to filename (ie, file extension for external non textual files)
   */
  public TextContent(Text text, Contributor contributor, String suffix) {
    this.text = text;
    this.contributor = contributor;
    this.filename = "text_" + text.getIdContribution()
        + (contributor != null ? "_" + contributor.getIdContributor() : "")
        + suffix;
  }


  /**
   * Get the filename for this contribution
   *
   * @return a filename, of the form "text_" + id_contribution ( + "_" id_contributor if any)
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Set the filename for this contribution
   *
   * @param filename a filename, of the form "text_" + id_contribution ( + "_" id_contributor if any)
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * Get the textual contribution related to this content
   *
   * @return the associated text to this text content
   */
  public Text getText() {
    return text;
  }

  /**
   * Set the textual contribution related to this content
   *
   * @param text the associated text to this text content
   */
  public void setText(Text text) {
    this.text = text;
  }

  /**
   * Get the contributor to which this content is associated, if this text is private to a contributor,
   * ie, the text visibility is set to private.
   *
   * @return the contributor associated to this text content, may be null
   */
  public Contributor getContributor() {
    return contributor;
  }

  /**
   * Set the contributor to which this content is associated, if this text is private to a contributor,
   * ie, the text visibility is set to private.
   *
   * @param contributor the contributor associated to this text content
   */
  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  @Override
  public String toString() {
    return "content [t:" + getText().getIdContribution()
        + (getContributor() != null ? " - c:" + getContributor().getIdContributor() : "") + "] " + getFilename();
  }
}
