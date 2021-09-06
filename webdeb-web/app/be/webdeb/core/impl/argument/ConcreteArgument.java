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

package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.Language;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContribution;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;

import java.util.*;

/**
 * This class implements a Webdeb argument, ie, a sentence written by a contributor.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteArgument extends AbstractContribution<ArgumentFactory, ArgumentAccessor> implements Argument {

  protected Long dictionaryId;
  protected ArgumentDictionary dictionary = null;
  protected String title;
  protected Language language;
  protected EArgumentType argumentType;


  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  /**
   * Default constructor.
   *
   * @param factory the argument factory (to get and build concrete instances of objects)
   * @param accessor the argument accessor to retrieve and persist arguments
   * @param contributorFactory the contributor factory to get bound contributors
   */
  ConcreteArgument(ArgumentFactory factory, ArgumentAccessor accessor, ContributorFactory contributorFactory) {
    super(factory, accessor, contributorFactory);
    type = EContributionType.ARGUMENT;
    argumentType = EArgumentType.SIMPLE;
  }

  /*
   * GETTERS / SETTERS
   */

  @Override
  public Long getDictionaryId() {
    return dictionaryId;
  }

  @Override
  public void setDictionaryId(Long dictionaryId) {
    this.dictionaryId = dictionaryId;
  }

  @Override
  public ArgumentDictionary getDictionary() {
    if(dictionary == null){
      dictionary = accessor.retrieveDictionary(dictionaryId);

      if(dictionary == null && isValid().isEmpty()) {
        dictionary = factory.getArgumentDictionary();
        dictionary.setTitle(title);
        dictionary.setLanguage(language);
      }
    }
    return dictionary;
  }

  public void setDictionary(ArgumentDictionary dictionary) {
    this.dictionary = dictionary;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getFullTitle() {
    return getTitle();
  }

  @Override
  public Language getLanguage() {
    return language;
  }

  @Override
  public void setLanguage(Language language) {
    this.language = language;
  }

  @Override
  public EArgumentType getArgumentType() {
    return argumentType;
  }

  @Override
  public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
    List<String> errors = isValid();
    if (!errors.isEmpty()) {
      logger.error("argument contains error " + errors.toString());
      throw new FormatException(FormatException.Key.ARGUMENT_ERROR, String.join(",", errors));
    }
    accessor.save(this, currentGroup, contributor);
    return null;
  }

  @Override
  public List<String> isValid() {
    List<String> fieldsInError = new ArrayList<>();

    if (argumentType == null) {
      fieldsInError.add("argumentType is null");
    }

    if (language == null) {
      fieldsInError.add("language is null");
    }

    if (factory.getValuesHelper().isBlank(title)) {
      fieldsInError.add("title is null");
    }
    return fieldsInError;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Argument && hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return 31 * (id == -1L ? 47 : id.hashCode())  + language.getCode().hashCode() + title.hashCode();
  }

  @Override
  public String toString() {
    return "argument " + id + " of type " + argumentType.toString() + " with title " + getFullTitle() + " in " + language.getCode();
  }

}
