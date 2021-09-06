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

import be.webdeb.core.api.actor.EActorType;
import be.webdeb.infra.persistence.model.annotation.Unqueryable;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;

import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the actor names database table. Actors may have multiple spellings for their names (depending
 * on the language), so they are externalized in a dedicated table with the 2-char ISO code corresponding to the language.
 *
 * For persons, either both the name and first_acro (first name) or (inclusive) pseudo are set
 * For organizations, they may have a name and possibly a first_or_acro (acronyme), but no pseudo
 * For unknown actor type, they only have a name.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Entity
@CacheBeanTuning
@Table(name = "actor_i18names")
public class ActorI18name extends Model {

  @EmbeddedId
  private ActorI18namePK id;

  private static final Model.Finder<ActorI18namePK, ActorI18name> find = new Model.Finder<>(ActorI18name.class);

  @ManyToOne
  @JoinColumn(name = "id_contribution", nullable = false)
  @Unqueryable
  private Actor actor;

  @Column(name = "first_or_acro")
  private String firstOrAcro;

  @Column(name = "name")
  private String name;

  @Column(name = "pseudo")
  private String pseudo;


  /**
   * Default constructor. Create name holder for given actor in given language. Name must be set independently
   *
   * @param actor an actor
   * @param lang a 2-char ISO code of the name's language
   */
  public ActorI18name(Actor actor, String lang) {
    ActorI18namePK pk = new ActorI18namePK();
    pk.setIdContribution(actor.getIdContribution());
    pk.setLang(lang);
    setId(pk);
    this.actor = actor;
  }

  /*
   * GETTERS AND SETTERS
   */

  /**
   * Get the complex id for this actor name
   *
   * @return a complex id (concatenation of id_contribution and lang)
   */
	public ActorI18namePK getId() {
		return this.id;
	}

  /**
   * Set the complex id for this actor name
   *
   * @param id a complex id object
   */
	public void setId(ActorI18namePK id) {
		this.id = id;
	}

  /**
   * Get the current language code for this name
   *
   * @return a two-char iso-639-1 language code
   */
	public String getLang() {
    return id.getLang();
  }

  /**
   * Set the current language code for this name
   *
   * @param lang a two-char iso-639-1 language code
   */
  public void setLang(String lang) {
    id.setLang(lang);
  }

  /**
   * Get the actor's name (if any)
   *
   * @return a name (last name for person or full name for organizations)
   */
  public String getName() {
    return name;
  }

  /**
   * Set the actor's name
   *
   * @param name a name (last name for person or full name for organizations)
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the person firstname or organization acronym (depending on the actortype)
   *
   * @return the person firstname or organization acronym
   */
  public String getFirstOrAcro() {
    return firstOrAcro;
  }

  /**
   * Set the person firstname or organization acronym (depending on the actortype).
   *
   * @param firstOrAccro  the person firstname or organization acronym
   */
  public void setFirstOrAccro(String firstOrAccro) {
    firstOrAcro = firstOrAccro;
  }

  /**
   * Get the actor pseudonym (if any). May contain a value only for persons.
   *
   * @return a pseudonym for this actor
   */
	public String getPseudo() {
		return this.pseudo;
	}

  /**
   * Set the actor pseudonym. Only acceptable for PERSONS.
   *
   * @param pseudo a pseudonym
   */
	public void setPseudo(String pseudo) {
    if (actor.getActortype() != null && actor.getActortype().getIdActorType() == EActorType.PERSON.id()) {
      this.pseudo = pseudo;
    }
	}

  /**
   * Check whether this name is an old name (only applicable for organizations)
   *
   * @return true if this name is an old name
   */
	public boolean isOld() {
    return id.isOld() == 1;
  }

  /**
   * Set whether this name is an old name (only applicable for organizations)
   *
   * @param isOld true if this name is an old name
   */
  public void isOld(boolean isOld) {
    id.isOld(isOld ? 1 : 0);
  }

  /**
   * Get the actor with this name
   *
   * @return an actor
   */
	public Actor getActor() {
		return this.actor;
	}

  /**
   * Set the actor with this name
   *
   * @param actor an actor
   */
	public void setActor(Actor actor) {
		this.actor = actor;
	}

  @Override
  public String toString() {
    // because of lazy load, must explicitly call getter
    return (getFirstOrAcro() != null ? getFirstOrAcro() + " " : "") + getName() +
        (getPseudo() != null ? " (" + getPseudo() + ")" : "") + " [" + getId().getLang() +
        (isOld() ? " - old name" : "") + "]";
  }

  /**
   * Find the actor name by given actor id and name lang
   *
   * @param actor an actor id
   * @param lang the language of the name
   * @return the actor name
   */
  public static ActorI18name findByActorAndLang(Long actor, String lang) {
    List<ActorI18name> names =
            find.where().eq("id_contribution", actor).eq("lang", lang).findList();

    return names.isEmpty() ? null : names.get(0);
  }

  /**
   * The primary key class for the actor multiple name database table.
   *
   * @author Fabian Gilson
   */
  @Embeddable
  public static class ActorI18namePK extends Model {

    @Column(name = "id_contribution", insertable = false, updatable = false, unique = true, nullable = false)
    private Long idContribution;

    @Column(name = "lang", insertable = false, updatable = false, unique = true, nullable = false)
    private String lang;

    @Column(name = "is_old", insertable = false, updatable = false, unique = true, nullable = false)
    private Integer isOld;

    /**
     * Get the contribution (actor) id
     *
     * @return an id
     */
    public Long getIdContribution() {
      return idContribution;
    }

    /**
     * Set the contribution (actor) id
     *
     * @param idContribution an id of an existing actor
     */
    public void setIdContribution(Long idContribution) {
      this.idContribution = idContribution;
    }

    /**
     * Get the language associated to this name (spelling)
     *
     * @return a two-char iso-639-1 language code
     */
    public String getLang() {
      return this.lang;
    }

    /**
     * Set the language associated to this name (spelling)
     *
     * @param lang a two-char iso-639-1 language code
     */
    public void setLang(String lang) {
      this.lang = lang;
    }

    /**
     * Check whether this name is an old name (only applicable for organizations)
     *
     * @return true if this name is an old name
     */
    public Integer isOld() {
      return isOld;
    }

    /**
     * Set whether this name is an old name (only applicable for organizations)
     *
     * @param isOld true if this name is an old name
     */
    public void isOld(Integer isOld) {
      this.isOld = isOld;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ActorI18namePK)) {
        return false;
      }
      ActorI18namePK castOther = (ActorI18namePK) other;
      return idContribution.equals(castOther.idContribution) && lang.equals(castOther.lang) && isOld.equals(castOther.isOld);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.idContribution.hashCode();
      return hash * prime + this.lang.hashCode() + isOld;
    }
  }
}
