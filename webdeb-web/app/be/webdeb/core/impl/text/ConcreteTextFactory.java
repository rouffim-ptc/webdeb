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

package be.webdeb.core.impl.text;

import be.webdeb.core.api.contribution.ExternalContribution;
import be.webdeb.core.api.text.WordGender;
import be.webdeb.core.api.text.*;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.AbstractContributionFactory;
import be.webdeb.core.impl.contribution.ConcreteExternalContribution;
import be.webdeb.infra.persistence.accessor.api.ActorAccessor;
import be.webdeb.infra.persistence.accessor.api.TextAccessor;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the text factory that gives access to text implementation and manipulate texts.
 *
 * @author Fabian Gilson
 * @author Martin ROuffiange
 */
@Singleton
public class ConcreteTextFactory extends AbstractContributionFactory<TextAccessor> implements TextFactory {

  @Inject
  private ActorAccessor actorAccessor;

  private Map<Integer, TextType> textTypes;
  private Map<Integer, TextSourceType> textSourceTypes;
  private Map<String, WordGender> genders;
  private Map<Integer, TextVisibility> visibilities;

  @Override
  public Text retrieve(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, false) : null;
  }

  @Override
  public Text retrieveWithHit(Long id) {
    return id != null && id != -1L ? accessor.retrieve(id, true) : null;
  }

  @Override
  public Text getText() {
    return new ConcreteText(this, accessor, actorAccessor, contributorFactory);
  }

  @Override
  public List<TextType> getTextTypes() {
    if (textTypes == null) {
      textTypes = accessor.getTextTypes().stream().collect(Collectors.toMap(TextType::getType, e -> e));
    }
    return new ArrayList<>(textTypes.values());
  }

  @Override
  public TextType createTextType(int type, Map<String, String> i18names) {
    return new ConcreteTextType(type, i18names);
  }

  @Override
  public TextType getTextType(Integer type) {
    if (textTypes == null) {
      getTextTypes();
    }
    return type == null || textTypes.get(type) == null ? textTypes.get(ETextType.OTHER.id()) : textTypes.get(type);
  }

  @Override
  public List<TextSourceType> getTextSourceTypes() {
    if (textSourceTypes == null) {
      textSourceTypes = accessor.getTextSourceTypes().stream().collect(Collectors.toMap(TextSourceType::getType, e -> e));
    }
    return new ArrayList<>(textSourceTypes.values());
  }

  @Override
  public TextSourceType createTextSourceType(int type, Map<String, String> i18names) {
    return new ConcreteTextSourceType(type, i18names);
  }

  @Override
  public TextSourceType getTextSourceType(Integer type) {
    if (textSourceTypes == null) {
      getTextSourceTypes();
    }
    return type == null || textSourceTypes.get(type) == null ? textSourceTypes.get(ETextSourceType.OTHER.id()) : textSourceTypes.get(type);
  }

  @Override
  public List<Text> findByTitle(String title) {
    return accessor.findByTitle(title);
  }

  @Override
  public Text findByUrl(String url) {
    return accessor.findByUrl(url);
  }


  @Override
  public List<TextSourceName> findSourceNames(String source, int fromIndex, int toIndex) {
    return accessor.findSourceNames(source, fromIndex, toIndex);
  }

  @Override
  public TextSourceName getTextSourceName() {
    return new ConcreteTextSourceName();
  }

  @Override
  public List<TextCopyrightfreeSource> getAllCopyrightfreeSources() {
    return accessor.getAllCopyrightfreeSources();
  }

  @Override
  public boolean sourceIsCopyrightfree(String source) {
    return accessor.sourceIsCopyrightfree(source, null);
  }

  @Override
  public boolean sourceIsCopyrightfree(String source, Long contributor) {
    return accessor.sourceIsCopyrightfree(source, contributor);
  }

  @Override
  public TextCopyrightfreeSource getTextCopyrightfreeSource() {
    return new ConcreteTextCopyrightfreeSource();
  }

  @Override
  public int saveTextCopyrightfreeSource(TextCopyrightfreeSource freeSource) throws PersistenceException {
    return accessor.saveTextCopyrightfreeSource(freeSource);
  }

  @Override
  public void removeTextCopyrightfreeSource(int idSource) throws PersistenceException {
    accessor.removeTextCopyrightfreeSource(idSource);
  }

  @Override
  public ExternalContribution getExternalContribution() {
    return new ConcreteExternalContribution(this, accessor, contributorFactory);
  }

  @Override
  public Text random() {
    return accessor.random();
  }

  @Override
  public List<WordGender> getWordGenders() {
    if (genders == null) {
      genders = accessor.getWordGenders().stream().collect(Collectors.toMap(WordGender::getCode, e -> e));
    }
    return new ArrayList<>(genders.values());
  }

  @Override
  public WordGender getWordGender(String code) throws FormatException {
    if (genders == null) {
      getWordGenders();
    }
    if (!genders.containsKey(code)) {
      throw new FormatException(FormatException.Key.UNKNOWN_WORD_GENDER, code);
    }
    return genders.get(code);
  }

  @Override
  public WordGender createWordGender(String code, Map<String, String> i18names) {
    return new ConcreteWordGender(code, i18names);
  }

  @Override
  public TextVisibility getTextVisibility(int visibility) throws FormatException {
    if (visibilities == null) {
      getTextVisibilities();
    }
    if (!visibilities.containsKey(visibility)) {
      throw new FormatException(FormatException.Key.UNKNOWN_TEXT_VISIBILITY, String.valueOf(visibility));
    }
    return visibilities.get(visibility);
  }

  @Override
  public TextVisibility createTextVisibility(ETextVisibility visibility, Map<String, String> i18names) {
    return new ConcreteTextVisibility(visibility.id(), i18names);
  }

  @Override
  public List<TextVisibility> getTextVisibilities() {
    if (visibilities == null) {
      visibilities = accessor.getTextVisibilities().stream().collect(Collectors.toMap(TextVisibility::getType, v -> v));
    }
    return new ArrayList<>(visibilities.values());
  }

}
