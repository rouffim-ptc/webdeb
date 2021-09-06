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
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import be.webdeb.infra.persistence.accessor.api.TagAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements an factory for arguments, argument types and links.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ConcreteArgumentFactory extends AbstractContributionFactory<ArgumentAccessor> implements ArgumentFactory {

  // key is shade id
  private Map<Integer, ArgumentType> argumentTypes = null;
  private Map<Integer, ArgumentShade> argumentShades = null;

  @Inject
  private TagAccessor tagAccessor;

  @Override
  public Argument retrieve(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, false) : null;
  }

  @Override
  public Argument retrieveWithHit(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, true) : null;
  }

  @Override
  public ArgumentShaded retrieveShaded(Long id) {
    return accessor.retrieveShaded(id, false);
  }

  @Override
  public ArgumentShaded retrieveShadedWithHit(Long id) {
    return accessor.retrieveShaded(id, true);
  }

  @Override
  public ArgumentDictionary retrieveDictionary(Long id) {
    return accessor.retrieveDictionary(id);
  }

  @Override
  public ArgumentJustification retrieveJustificationLink(Long id) {
    return accessor.retrieveJustificationLink(id);
  }

  @Override
  public ArgumentSimilarity retrieveSimilarityLink(Long id) {
    return accessor.retrieveSimilarityLink(id);
  }

  @Override
  public Argument getArgument() {
    return new ConcreteArgument(this, accessor, contributorFactory);
  }

  @Override
  public ArgumentShaded getArgumentShaded() {
    return new ConcreteArgumentShaded(this, accessor, contributorFactory);
  }

  @Override
  public ArgumentDictionary getArgumentDictionary() {
    return new ConcreteArgumentDictionary(this, accessor, contributorFactory);
  }

  @Override
  public ArgumentJustification getArgumentJustificationLink() {
    return new ConcreteArgumentJustification(this, accessor, tagAccessor, contributorFactory);
  }

  @Override
  public ArgumentSimilarity getArgumentSimilarityLink() {
    return new ConcreteArgumentSimilarity(this, accessor, contributorFactory);
  }

  @Override
  public ArgumentType createArgumentType(Integer type, Map<String, String> i18names) {
    return new ConcreteArgumentType(type, i18names);
  }

  @Override
  public ArgumentType getArgumentType(int type) throws FormatException {
    if (argumentTypes == null) {
      getArgumentTypes();
    }
    if (!argumentTypes.containsKey(type)) {
      throw new FormatException(FormatException.Key.UNKNOWN_ARGUMENT_TYPE, String.valueOf(type));
    }
    return argumentTypes.get(type);
  }

  @Override
  public List<ArgumentType> getArgumentTypes() {
    if (argumentTypes == null) {
      argumentTypes = accessor.getArgumentTypes().stream().collect(Collectors.toMap(ArgumentType::getType, t -> t));
    }
    return new ArrayList<>(argumentTypes.values());
  }

  @Override
  public ArgumentShade createArgumentShade(Integer shade, Map<String, String> i18names) {
    return new ConcreteArgumentShade(shade, i18names);
  }

  @Override
  public ArgumentShade getArgumentShade(int shade) throws FormatException {
    if (argumentShades == null) {
      getArgumentShades();
    }
    if (!argumentShades.containsKey(shade)) {
      throw new FormatException(FormatException.Key.UNKNOWN_ARGUMENT_SHADE_TYPE, String.valueOf(shade));
    }
    return argumentShades.get(shade);
  }

  @Override
  public List<ArgumentShade> getArgumentShades() {
    if (argumentShades == null) {
      argumentShades = new LinkedHashMap<>();
      accessor.getArgumentShades().forEach(shade -> argumentShades.put(shade.getType(), shade));
    }
    return new ArrayList<>(argumentShades.values());
  }

  @Override
  public List<Argument> findByTitle(String title, String lang, int type, int shade, int fromIndex, int toIndex) {
    return accessor.findByTitle(title, lang, type, shade, fromIndex, toIndex);
  }

  @Override
  public ArgumentDictionary findUniqueDictionaryByTitle(String title, String lang) {
    return accessor.findUniqueDictionaryByTitle(title, lang);
  }

  @Override
  public List<ArgumentDictionary> findDictionaryByTitle(String title, String lang, int fromIndex, int toIndex) {
    return accessor.findDictionaryByTitle(title, lang, fromIndex, toIndex);
  }

  @Override
  public boolean argumentJustificationLinkAlreadyExists(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade) {
    return accessor.argumentJustificationLinkAlreadyExists(context, subContext, category, superArgument, argument, shade);
  }

  @Override
  public ArgumentJustification findArgumentJustification(Long context, Long subContext, Long category, Long superArgument, Long argument, int shade) {
    return accessor.findArgumentJustification(context, subContext, category, superArgument, argument, shade);
  }

  @Override
  public int getMaxArgumentJustificationLinkOrder(Long context, Long subContext, Long category, Long superArgument, int shade) {
    return accessor.getMaxArgumentJustificationLinkOrder(context, subContext, category, superArgument, shade);
  }

  @Override
  public List<ArgumentJustification> findArgumentLinks(Long context, Long subContext, Long category, Long superArgument, int shade) {
    return accessor.findArgumentLinks(context, subContext, category, superArgument, shade);
  }

  @Override
  public int getArgumentNbCitationsLink(Long context, Long subContext, Long category, Long argument, Integer shade, Long contributor, int group) {
    return accessor.getArgumentNbCitationsLink(context, subContext, category, argument, shade, contributor, group);
  }

  @Override
  public Argument random() {
    return accessor.random();
  }
}
