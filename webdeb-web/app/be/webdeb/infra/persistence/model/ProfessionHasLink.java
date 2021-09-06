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

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The persistent class for the joint table between a professions, devoted to hold equivalence between profession.
 * Other types of links between profession are foreseen but not yet defined
 *
 * @author Fabian Gilson
 */
@Entity
@CacheBeanTuning
@Table(name = "profession_has_link")
public class ProfessionHasLink extends Model {

  private static final Model.Finder<Long, ProfessionHasLink> find = new Model.Finder<>(ProfessionHasLink.class);

  @EmbeddedId
  private ProfessionHasLinkPK id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = PROFESSION_FROM, nullable = false, insertable = false, updatable = false)
  private Profession profession;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = PROFESSION_TO, nullable = false, insertable = false, updatable = false)
  private Profession substitute;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = LINK_TYPE, nullable = false)
  private ProfessionLink link;

  // string constante for db query
  private static final String PROFESSION_FROM = "id_profession_from";
  private static final String PROFESSION_TO = "id_profession_to";
  private static final String LINK_TYPE = "link_type";

  /**
   * Constructor, Initialize id to 0 for auto-increment
   *
   * @param professionFrom a profession sub link
   * @param professionTo a profession super link
   */
  public ProfessionHasLink(Profession professionFrom, Profession professionTo) {
    profession = professionFrom;
    substitute = professionTo;
    link = ProfessionLink.find.byId(0);
  }

  /**
   * Get the complex id
   *
   * @return this complex id object
   */
  public ProfessionHasLinkPK getId() {
    return id;
  }

  /**
   * Set the complex id object
   *
   * @param id a complex id
   */
  public void setId(ProfessionHasLinkPK id) {
    this.id = id;
  }

  /**
   * Get the profession that may be substituted
   *
   * @return a profession
   */
  public Profession getProfession() {
    return profession;
  }

  /**
   * Set the profession that may be substituted
   *
   * @param profession a profession
   */
  public void setProfession(Profession profession) {
    this.profession = profession;
  }

  /**
   * Get the profession that may be used as a substitute
   *
   * @return a substitute profession
   */
  public Profession getSubstitute() {
    return substitute;
  }

  /**
   * Set the profession that may be used as a substitute
   *
   * @param substitute a substitute profession
   */
  public void setSubstitute(Profession substitute) {
    this.substitute = substitute;
  }

  /**
   * Get the type of link between these professions (only equivelence atm)
   *
   * @return a type of link
   */
  public ProfessionLink getLink() {
    return link;
  }

  /**
   * Set the type of link between these professions (only equivelence atm)
   *
   * @param link a type of link
   */
  public void setLink(ProfessionLink link) {
    this.link = link;
  }

  /**
   * Find the equivalent (substitute) profession for given profession, ie the profession having a link of type = 0
   *
   * @param profession a profession id (may not exist)
   * @return a possibly null profession
   */
  public static Profession findEquivalent(int profession) {
    List<Profession> equivalents = findAllEquivalents(profession);
    return !equivalents.isEmpty() ? equivalents.get(0) : Profession.findById(profession);
  }

  /**
   * Find all equivalents professions for given profession, ie the profession having a link of type = 0
   *
   * @param profession a profession id (may not exist)
   * @return a possibly empty list
   */
  public static List<Profession> findAllEquivalents(int profession) {
    List<ProfessionHasLink> links = find.where().conjunction().eq(PROFESSION_TO, profession).eq(LINK_TYPE, 0).findList();
    return links.stream().map(ProfessionHasLink::getProfession).collect(Collectors.toList());
  }

  /**
   * Find all links with given profession
   *
   * @param professionId a profession Id
   * @return a possibly empty list of profession links
   */
  public static List<ProfessionHasLink> findLinks(int professionId, boolean asParent){
    return find.where().conjunction().eq(asParent ? PROFESSION_FROM : PROFESSION_TO, professionId).eq(LINK_TYPE, 0).findList();
  }

  /**
   * Search if a link between two profession exist and return it
   *
   * @param professionFrom profession id (may not exist)
   * @param professionTo profession id (may not exist)
   * @return a possibly null profession
   */
  public static ProfessionHasLink findLink(int professionFrom, int professionTo){
    return find.where().conjunction().eq(PROFESSION_FROM, professionFrom).eq(PROFESSION_TO, professionTo).eq(LINK_TYPE, 0).findUnique();
  }

  /**
   * Get the super link for given profession, ie the profession having a link of type = 0
   *
   * @param profession a profession id (may not exist)
   * @return a possibly null profession
   */
  public static Profession getSuperLink(int profession) {
    List<ProfessionHasLink> link = find.where().conjunction().eq(PROFESSION_FROM, profession).eq(LINK_TYPE, 0).findList();
    if(link != null && !link.isEmpty() && link.get(0).getSubstitute() != null){
      return link.get(0).getSubstitute();
    }
    return null;
  }

  @Override
  public String toString() {
    return getProfession().toString() + " " + getSubstitute();
  }

  /**
   * The primary key class for the profession_has_link database table.
   *
   */
  @Embeddable
  public static class ProfessionHasLinkPK extends Model {

    @Column(name = PROFESSION_FROM, nullable = false, insertable = false, updatable = false)
    private Integer profession;

    @Column(name = PROFESSION_TO, nullable = false, insertable = false, updatable = false)
    private Integer substitute;

    @Column(name = LINK_TYPE, nullable = false)
    private Integer link;

    /**
     * Constructor
     *
     * @param profession an profession id
     * @param substitute another profession id being the substitute for given profession
     * @param type the id of the type of link between both professions
     */
    public ProfessionHasLinkPK(Integer profession, Integer substitute, Integer type) {
      this.profession = profession;
      this.substitute = substitute;
      this.link = type;
    }

    /**
     * Get the profession id being the origin of the link
     *
     * @return an profession id
     */
    public Integer getProfession() {
      return this.profession;
    }

    /**
     * Set the profession id being the origin of the link
     *
     * @param profession an profession id
     */
    public void setProfession(Integer profession) {
      this.profession = profession;
    }

    /**
     * Get the substitute profession id being the destination of the link
     *
     * @return an profession id
     */
    public Integer getSubstitute() {
      return this.substitute;
    }

    /**
     * Set the substitute profession id being the destination of the link
     *
     * @param substitute an profession id
     */
    public void setSubstitute(Integer substitute) {
      this.substitute = substitute;
    }

    /**
     * Get the link id binding these professions
     *
     * @return a profession link id
     */
    public Integer getLink() {
      return this.link;
    }

    /**
     * Set the link id binding these professions
     *
     * @param link a profession link id
     */
    public void setLink(Integer link) {
      this.link = link;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ProfessionHasLinkPK)) {
        return false;
      }
      ProfessionHasLinkPK castOther = (ProfessionHasLinkPK) other;
      return
          this.profession.equals(castOther.profession)
              && this.substitute.equals(castOther.substitute)
              && this.link.equals(castOther.link);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.profession.hashCode();
      hash = hash * prime + this.substitute.hashCode();
      hash = hash * prime + this.link.hashCode();

      return hash;
    }
  }


}
