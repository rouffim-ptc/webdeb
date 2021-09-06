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

import be.webdeb.core.api.actor.EProfessionType;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CacheBeanTuning;


import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The persistent class for the t_profession database table, holding professions that should be translateed
 * in all supported languages (some default ones are, others dynamically created from users are mon-lingual).
 */
@Entity
@CacheBeanTuning
@Table(name = "profession")
public class Profession extends WebdebModel {

  private static final Model.Finder<Integer, Profession> find = new Model.Finder<>(Profession.class);
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  @Id
  @Column(name = "id_profession", unique = true, nullable = false)
  private int idProfession;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_type", nullable = false)
  private TProfessionType professionType;

  @OneToMany(mappedBy = "profession", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<ProfessionI18name> spellings;

  @OneToMany(mappedBy = "substitute", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ProfessionHasLink> links;

  @Column(name = "display_hierarchy")
  private boolean displayHierarchy;

  // used for order by clause in raw sql
  @Transient
  private String spelling;

  @Transient
  private Map<String, Map<String, ProfessionI18name>> spellingsMap = null;
  /**
   * Default constructor. Initialize id to 0.
   */
  public Profession() {
    // initialize id to 0 (for auto increment)
    idProfession = 0;
  }

  /**
   * Get this profession id
   *
   * @return this profession id
   */
  public int getIdProfession() {
    return this.idProfession;
  }

  /**
   * Set this profession id
   *
   * @param idProfession an id
   */
  public void setIdProfession(int idProfession) {
    this.idProfession = idProfession;
  }

  /**
   * Get the profession type
   *
   * @return a profession type
   */
  public TProfessionType getProfessionType() {
    return professionType;
  }

  /**
   * Set the profession type
   *
   * @param professionType a profession type
   */
  public void setProfessionType(TProfessionType professionType) {
    this.professionType = professionType;
  }

  /**
   * Check whether this profession must displayed its hierarchy
   *
   * @return true if this profession must displayed its hierarchy
   */
  public boolean isDisplayHierarchy() {
    return displayHierarchy;
  }

  /**
   * Set whether this profession must displayed its hierarchy
   *
   * @param displayHierarchy true if this profession must displayed its hierarchy
   */
  public void setDisplayHierarchy(boolean displayHierarchy) {
    this.displayHierarchy = displayHierarchy;
  }

  /**
   * Get all available spellings for this profession
   *
   * @return a list of spellings as ProfessionI18names
   */
  public List<ProfessionI18name> getSpellings() {
    return spellings;
  }

  /**
   * Set all available spellings for this profession
   *
   * @param spellings a list of spellings
   */
  public void setSpellings(List<ProfessionI18name> spellings) {
    if (spellings != null) {
      // add/update new spellings
      this.spellings = new ArrayList<>();
      update();
      this.spellings.addAll(spellings);
    }
  }

  /**
   * Get all links having this profession being the origin
   *
   * @return a list of profession links
   */
  public List<ProfessionHasLink> getLinks() {
    return links != null ? links : new ArrayList<>();
  }

  /**
   * Set all links having this profession being the origin
   *
   * @param links a list of links
   */
  public void setLinks(List<ProfessionHasLink> links){
    if (links != null) {
      if (this.links == null) {
        this.links = new ArrayList<>();
      }
      // get previous languages for current spellings
      List<ProfessionHasLink> currentLinks = new ArrayList<>(this.links);
      // add/update new spellings
      links.forEach(this::addLink);

      currentLinks.stream().filter(link -> links.stream().allMatch(n -> n.equals(link)))
          .forEach(ProfessionHasLink::delete);
    }
  }

  /**
   * Add new link between professions
   *
   * @param link profession link
   */
  public boolean addLink(ProfessionHasLink link) {
    if (links == null) {
      links = new ArrayList<>();
    }
    Optional<ProfessionHasLink> match = links.stream().filter(s -> s.getProfession().getIdProfession() == link.getProfession().getIdProfession()).findAny();
    if (!match.isPresent()) {
      links.add(link);
      return true;
    }
    return false;
  }

  /**
   * Get the super link of the profession
   *
   * @return a possibly null profession
   */
  public Profession getSuperLink() {
    return ProfessionHasLink.getSuperLink(idProfession);
  }

  @Override
  public String toString() {
    return getSpellings().stream().map(Object::toString).collect(Collectors.joining(","));
  }

  /**
   * Get the first profession spelling
   *
   * @return a possibly null professionI18name
   */
  public ProfessionI18name getSpelling(){
    createNamesMap();

    if(spellingsMap != null){
      String[] tabSearch = {"fr", "en", "nl"};
      int i;
      for(i = 0; i < tabSearch.length; i++){
        if(spellingsMap.containsKey(tabSearch[i])){
          ProfessionI18name r = getSpelling(tabSearch[i], false);
          if(r != null){
            return r;
          }
        }
      }
    }
    return null;
  }

  /**
   * Get the first profession spelling of the given lang
   *
   * @param lang the lang of the name
   * @return a possibly null professionI18name
   */
  public ProfessionI18name getSpelling(String lang){
    return getSpelling(lang, true);
  }

  /**
   * Get the first profession spelling of the given lang
   *
   * @param lang the lang of the name
   * @return a possibly null professionI18name
   */
  public ProfessionI18name getSpelling(String lang, boolean recursive){
    createNamesMap();

    if(spellingsMap != null && spellingsMap.containsKey(lang)){
      String[] tabSearch = {"N", "M", "F"};
      int i;
      for(i = 0; i < tabSearch.length && !spellingsMap.get(lang).containsKey(tabSearch[i]); i++);
      if(i < tabSearch.length){
        return  spellingsMap.get(lang).get(tabSearch[i]);
      }
    }
    return recursive ? getSpelling() : null;
  }

  private void createNamesMap(){
    if(spellingsMap == null){
      spellingsMap = new HashMap<>();
      spellings.forEach(e -> {
        if(!spellingsMap.containsKey(e.getLang())){
          spellingsMap.put(e.getLang(), new HashMap<>());
        }
        spellingsMap.get(e.getLang()).put(e.getGender(), e);
      });
    }
  }

  /*
   * QUERIES
   */

  /**
   * Find all profession objects that exists in database
   *
   * @return a list of professions
   */
  public static List<Profession> findAllProfessions() {
    return find.all();
  }

  /**
   * Find a profession by its id
   *
   * @param id a profession id
   * @return the corresponding profession, or null if not found
   */
  public static Profession findById(int id) {
    return find.byId(id);
  }

  /**
   * Check if profession has a spelling in a given lang and gender
   *
   * @param lang the lang of the name
   * @param gender the gender of the name
   * @return true if given profession has the name
   */
  public boolean hasSpelling(String lang, String gender) {
    return spellings.stream().anyMatch(s -> s.getLang().equals(lang) && s.getGender().equals(gender));
  }

  /**
   * Get a profession by its name
   *
   * @param name the profession name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @return the Profession where lang=name, or null if not found
   */
  public static Profession findByName(String name, String lang) {
    return findByName(name, lang, null);
  }

  /**
   * Get a profession by its name and profession type
   *
   * @param name the profession name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param type the profession type
   * @return the Profession where lang=name, or null if not found
   */
  public static Profession findByName(String name, String lang, int type) {
    List<Profession> result = findProfessions(name, lang, null, type, true);
    return !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Get a profession by its name
   *
   * @param name the profession name to look for
   * @param strict true if only strict matches are considered
   * @return the Profession or null if not found
   */
  public static Profession findByName(String name, boolean strict) {
    List<Profession> professions = findProfessions(name, null, null, strict);
    return (professions.isEmpty() ? null : professions.get(0));
  }

  /**
   * Get a profession by its name
   *
   * @param name the profession name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @return the Profession where lang=name, or null if not found
   */
  public static Profession findByName(String name, String lang, String gender) {
    List<Profession> result = findProfessions(name, lang, gender, true);
    return !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Get a profession by its name, gender and lang
   *
   * @param name the profession name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @param strict true if only strict matches are considered
   * @return the Profession or null if not found
   */
  public static Profession findByName(String name, String gender, String lang, boolean strict) {
    List<Profession> professions = findProfessions(name, lang, gender, strict);
    return (professions.isEmpty() ? null : professions.get(0));
  }

  /**
   * Find a list of professions by a given term in a given language
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, Long idToIgnore, String lang, int fromIndex, int toIndex) {
    return findProfessions(term, idToIgnore, lang, null, null, false, fromIndex, toIndex);
  }

  /**
   * Find a list of professions by a given term in a given language and given profession type
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param type a profession type
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, String lang, int type) {
    return findProfessions(term, null, lang, null, type, false, 0, 0);
  }

  /**
   * Find a list of professions by a given term in a given language and given profession type
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @param type a profession type
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, String lang, String gender, int type) {
    return findProfessions(term, null, lang, gender, type, false, 0, 0);
  }

  /**
   * Find a list of professions by a given term in a given language and given profession type
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @param type a profession type
   * @param strict true if only strict matches are considered
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, String lang, String gender, int type, boolean strict) {
    return findProfessions(term, null, lang, gender, type, strict, 0, 0);
  }

  /**
   * Find a list of professions by a given term in a given language
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, String lang, String gender) {
    return findProfessions(term, null, lang, gender, null, false, 0, 0);
  }

  /**
   * Find a list of professions by a given term in a given language
   *
   * @param term the partial name to look for
   * @param lang the profession language (2-char ISO code) (aka the table column where it is stored)
   * @param gender the gender name (char code) (aka the table column where it is stored)
   * @param strict true if only strict matches are considered
   * @return a (possibly) empty list of all professions in the given language containing the given term
   */
  public static List<Profession> findProfessions(String term, String lang, String gender, boolean strict) {
    return findProfessions(term, null, lang, gender, null, strict, 0, 0);
  }

  /**
   * Find all professions with given name in given (optional) lang
   *
   * @param name a name to look for
   * @param idToIgnore an profession id to ignore
   * @param lang a 2-char ISO 639 code (optional, ignored if null passed)
   * @param gender a char code (optional, ignored if null passed)
   * @param type a profession type
   * @param strict true if only strict matches are considered
   * @param fromIndex the low endpoint of contributions to retrieve (only strictly positive values are considered)
   * @param toIndex the high endpoint of contributions to retrieve (only strictly positive values are considered)
   * @return a (possibly empty) list of professions matching given criteria
   */
  private static List<Profession> findProfessions(String name, Long idToIgnore, String lang, String gender, Integer type, boolean strict, int fromIndex, int toIndex) {
    type = type == null || EProfessionType.value(type) == null ? EProfessionType.OTHERS.id() : EProfessionType.value(type).id();
    String select = "select distinct p.id_profession from profession p left join profession_i18names n" +
        " on p.id_profession = n.profession" +
        " where n.spelling " + (strict ? " = '" + strictCleanUp(name) + "'" : " like '%" + strictCleanUp(name) + "%'") +
        (idToIgnore != null ? " and p.id_profession != " + idToIgnore : "") +
        (lang != null ? " and n.lang = '" + lang + "'" : "") +
        (gender != null ? " and n.gender = '" + gender + "'" : "") + " and p.id_type = " + type +
        " order by n.spelling" + getSearchLimit(fromIndex, toIndex);

    List<Profession> result = Ebean.find(Profession.class).setRawSql(RawSqlBuilder.parse(select).create()).findList();
    return result != null ? result : new ArrayList<>();
  }

  /**
   * Protect single quotes and remove parenthesis, double quotes and dashes from names
   *
   * @param name a name to cleanup
   * @return the clean up name
   */
  /*private static String cleanUp(String name) {
    return name != null ?
        //name.replace("'", "\\'").replace("(", "").replace(")", "").replace("\"", "").replace("-", " ") : null;
  }*/

  /**
   * Protect single quotes
   *
   * @param name a name to cleanup
   * @return the clean up name
   */
  private static String strictCleanUp(String name) {
    return name != null ?
        name.replace("'", "\\'") : null;
  }
}
