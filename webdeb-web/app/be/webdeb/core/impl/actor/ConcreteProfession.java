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

package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.EProfessionGender;
import be.webdeb.core.api.actor.EProfessionType;
import be.webdeb.core.api.actor.Profession;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;

import java.util.*;

/**
 * This class implements the profession for individuals (contributor or personal actors)
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteProfession implements Profession {

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private int id;
  private EProfessionType type;
  private boolean displayHierarchy;
  // the super link profession
  private Profession superLink;
  // map of (lang, map of (gender, spelling))
  private Map<String, Map<String, String>> i18names;
  // map of (idProfession, profession)
  private Map<Integer, Profession> links;
  private Map<String, Boolean> hierarchy = null;

  private ActorAccessor accessor;
  /**
   * Constructor
   *
   * @param id a profession id
   * @param i18names a map of language ISO code and profession names
   */
  ConcreteProfession(ActorAccessor accessor, int id, EProfessionType type, boolean displayHierarchy, Map<String, Map<String, String>> i18names) {
    this.accessor = accessor;
    this.id = id;
    this.type = type == null ? EProfessionType.OTHERS : type;
    this.displayHierarchy = displayHierarchy;
    this.i18names = (i18names != null ? i18names : new HashMap<>());
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public EProfessionType getType() {
    return type;
  }

  @Override
  public void setType(EProfessionType type) {
    this.type = type;
  }

  @Override
  public Profession getSuperLink(){
    if(superLink == null){
      this.superLink = setSuperLink();
    }
    return this.superLink;
  }

  @Override
  public Profession getSuperLinkWithoutSetting(){
    return this.superLink;
  }

  @Override
  public void setSuperLink(Profession profession){
    this.superLink = profession;
  }

  @Override
  public boolean matchName(String name) {
    if(name != null) {
      for (Map.Entry<String, Map<String, String>> entryMaps : i18names.entrySet()) {
        for (Map.Entry<String, String> entry : entryMaps.getValue().entrySet()) {
          if (name.equals(entry.getValue())){
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public String getName(String lang) {
    return getName(lang, EProfessionGender.MASCULINE.id().toString());
  }

  @Override
  public String getSimpleName(String lang) {
    return accessor.getProfessionSimpleName(id, lang);
  }

  @Override
  public Map<String, String> getNameByGender(String gender){
    Map<String, String> genderedNames = new HashMap<>();
    for(Map.Entry<String, Map<String, String>> name : i18names.entrySet()){
      genderedNames.put(name.getKey(), getGender(name.getValue(), gender));
    }
    return genderedNames;
  }

  @Override
  public String getName(String lang, String gender) {
    Map<String, String> results = getLang(lang);
    String result = "";
    if(results != null){
      result = getGender(results, gender);
    }
    return result;
  }

  @Override
  public String getGendersNames(String lang) {
    if(langGenderNameExist(lang, EProfessionGender.MASCULINE.id().toString())) {
      return getName(lang, EProfessionGender.MASCULINE.id().toString())
              + (langGenderNameExist(lang, EProfessionGender.FEMININE.id().toString()) ?
              " (" + getName(lang, EProfessionGender.FEMININE.id().toString()) + ")" : "");
    }
    else if(langGenderNameExist(lang, EProfessionGender.FEMININE.id().toString())) {
      return getName(lang, EProfessionGender.FEMININE.id().toString())
              + (langGenderNameExist(lang, EProfessionGender.NEUTRAL.id().toString()) ?
              " (" + getName(lang, EProfessionGender.NEUTRAL.id().toString()) + ")" : "");
    }
    else if(langGenderNameExist(lang, EProfessionGender.NEUTRAL.id().toString())) {
      return getName(lang, EProfessionGender.NEUTRAL.id().toString());
    }
    else{
      return getName(lang);
    }

  }

  private Boolean langGenderNameExist(String lang, String gender){
    Map<String, String> results = i18names.get(lang);
    if(results != null){
      String r = results.get(gender);
      if(r != null && !r.equals("")){
        return true;
      }
    }
    return false;
  }

  private Map<String, String> getLang(String lang){
    Map<String, String> results = i18names.get(lang);
    if (results == null) {
      // try to find english
      results = i18names.get("en");
      if (results == null) {
        // find the first result or return null map
        Optional<Map<String, String>> preresults = i18names.values().stream().filter(Objects::nonNull).findAny();
        results = (preresults.isPresent() ? preresults.get() : null);
      }
    }
    return results;
  }

  private String getGender(Map<String, String> results, String gender){
    String result = results.get(gender);
    if (result == null || "".equals(result)) {
      // try to find neutral name
      result = results.get(EProfessionGender.NEUTRAL.id().toString());
      if (result == null || "".equals(result)) {
        // try to find masculine name
        result = results.get(EProfessionGender.MASCULINE.id().toString());
        if (result == null || "".equals(result)) {
          // find the feminine name
          result = results.get(EProfessionGender.FEMININE.id().toString());
        }
      }
    }
    return result;
  }

  @Override
  public Map<String, Map<String, String>> getNames() {
    return i18names;
  }

  @Override
  public boolean professionHasName(String name){
    if(name != null && !name.equals("")) {
      for (Map.Entry<String, Map<String, String>> i18name : i18names.entrySet()) {
        for (Map.Entry<String, String> nameProfession : i18name.getValue().entrySet()) {
          if (name.equals(nameProfession.getValue())){
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, Boolean> getFunctionHierarchy(String lang) {
    if(hierarchy == null){
      hierarchy = accessor.getFunctionHierarchy(id, lang);
    }
    return hierarchy;
  }

  @Override
  public void addName(String name, String code) {
    if(code != null && code.length() == 3){
      String langW = code.substring(0, 2);
      String genderW = code.substring(2, 3);
      addName(name, langW, genderW);
    }
  }

  @Override
  public void addName(String name, String lang, String gender) {
    // simply overwrite existing value
    if (name != null && !"".equals(name.trim()) && lang != null
        && !"".equals(lang.trim()) && gender!= null && !"".equals(gender.trim())) {
      addNameToMap(name, lang, gender);
    }
  }

  private void addNameToMap(String name, String lang, String gender){
    if(!i18names.containsKey(lang)){
      i18names.put(lang, new HashMap<>());
    }
    i18names.get(lang).put(gender, name);
  }

  @Override
  public boolean isDisplayHierarchy() {
    return displayHierarchy;
  }

  @Override
  public void setDisplayHierarchy(boolean displayHierarchy) {
    this.displayHierarchy = displayHierarchy;
  }

  @Override
  public Map<Integer, Profession> getLinks() {
    if(links == null){
      setLinks();
    }
    return links;
  }

  @Override
  public void setLinks(Map<Integer, Profession> links) {
    this.links = links;
  }

  private Profession setSuperLink(){
    return accessor.getSuperLink(id);
  }

  private void setLinks(){
    links = new HashMap<>();
    getEquivalents().forEach(link -> links.put(link.getId(), link));
  }

  @Override
  public void addLink(Profession profession) {
    if(links == null){
      setLinks();
    }
    // simply overwrite existing value
    if (profession != null) {
      links.put(profession.getId(), profession);
    }
  }

  @Override
  public Profession getSubstitute() {
    return accessor.findSubstituteForProfession(id);
  }

  private List<Profession> getEquivalents() {
    return accessor.findEquivalentsForProfession(id);
  }

  @Override
  public String toString() {
    return "[" + String.join(", ", i18names.entrySet().toString()) + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConcreteProfession that = (ConcreteProfession) o;
    if (i18names == null || that.i18names == null || i18names.size() == 0 || that.i18names.size() == 0) {
      return false;
    }

    // optimistic checking on id
    if (id == that.id) {
      return true;
    }

    // try to match on names of the same language, if possible
    for (Map.Entry<String, Map<String, String>> entry : i18names.entrySet()) {
      Map<String, String> mine = entry.getValue();
      Map<String, String> his = that.i18names.get(entry.getKey());
      if (mine != null && his != null) {
        for (Map.Entry<String, String> entryGender : mine.entrySet()) {
          String mineGender = entryGender.getValue();
          String hisGender = his.get(entry.getKey());
          if (mineGender != null && hisGender != null) {
            return mineGender.equals(hisGender);
          }
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 + (i18names != null ? i18names.hashCode() : 0);
  }
}
