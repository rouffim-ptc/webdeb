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

package be.webdeb.infra.persistence.accessor.impl;

import be.webdeb.core.api.contribution.*;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.text.*;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.api.text.TextSourceName;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.ConcretePartialContributions;
import be.webdeb.core.impl.helper.SearchContainer;
import be.webdeb.infra.persistence.accessor.api.TextAccessor;
import be.webdeb.infra.persistence.model.*;
import be.webdeb.infra.persistence.model.Contribution;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This accessor handles retrieval and save actions of Texts
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class EbeanTextAccessor extends AbstractContributionAccessor<TextFactory> implements TextAccessor {

  private List<TextType> textTypes = null;
  private List<TextSourceType> textSourceTypes = null;
  private List<WordGender> genders = null;
  private List<TextVisibility> visibilities = null;

  // string constant for logs
  private static final String WITH_ID = " with id ";

  @Override
  public Text retrieve(Long id, boolean hit) {
    be.webdeb.infra.persistence.model.Text text = be.webdeb.infra.persistence.model.Text.findById(id);
    if (text != null) {
      try {
        Text api = mapper.toText(text);
        if (hit) {
          addHitToContribution(text.getContribution());
        }
        return api;
      } catch (FormatException e) {
        logger.error("unable to cast retrieved text " + id, e);
      }
    } else {
      logger.warn("no text found for id " + id);
    }
    return null;
  }

  @Override
  public String getContributionTextFile(String filename) {
    return getContributionTextFile(filename, Integer.MAX_VALUE);
  }

  @Override
  public String getContributionTextFile(String filename, int maxSize) {
    return files.getContributionTextFile(filename, 0, maxSize);
  }

  @Override
  public List<Citation> getCitations(Long id) {
    return getCitations(id, 0, Integer.MAX_VALUE).getContributions();
  }

  @Override
  public List<Citation> getTextCitations(SearchContainer query) {
    return buildCitationList(be.webdeb.infra.persistence.model.Citation.findCitationsFromText(query));
  }

  @Override
  public PartialContributions<Citation> getCitations(Long id, int fromIndex, int toIndex) {
    PartialContributions<Citation> results = new ConcretePartialContributions<>();

    if(checkSubIndexes(fromIndex, toIndex)) {
      be.webdeb.infra.persistence.model.Text text = be.webdeb.infra.persistence.model.Text.findById(id);

      try {
        if (text != null) {
          List<be.webdeb.infra.persistence.model.Citation> citations = text.getCitations()
                  .stream().skip(fromIndex).limit(toIndex - fromIndex)
                  .sorted(Comparator.comparing(be.webdeb.infra.persistence.model.Citation::getVersion).reversed()).collect(Collectors.toList());

          results.setContributions(buildCitationList(citations));
          results.setNumberOfLoadedContributions(citations.size());
        }
      } catch (Exception e) {
        logger.error("unable to build citation list for text " + id, e);
      }
    }

    return results;
  }

  @Override
  public List<Text> findByTitle(String title) {
    return buildTextList(be.webdeb.infra.persistence.model.Text.findByPartialTitle(title));
  }

  @Override
  public Text findByUrl(String url) {
    be.webdeb.infra.persistence.model.Text text =
            be.webdeb.infra.persistence.model.Text.findByUrl(values.getFormattedUrl(url));
    if(text != null) {
      try {
        return mapper.toText(text);
      } catch (FormatException e) {
        logger.error("unable to build text from db " + e.toString(), e);
        return null;
      }
    }
    return null;
  }

  @Override
  public List<TextSourceName> findSourceNames(String source, int fromIndex, int toIndex) {
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      List<be.webdeb.infra.persistence.model.TextSourceName> sources =
          be.webdeb.infra.persistence.model.TextSourceName.findTextSource(source, fromIndex, toIndex);

      return sources.parallelStream().map(s -> {
        try {
          return mapper.toSourceName(s);
        } catch (FormatException e) {
          logger.error("unable to convert source name " + s.toString(), e);
          return null;
        }
      }).collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("unable to retrieve sources by name " + source, e);

    } finally {
      transaction.end();
    }

    return new ArrayList<>();
  }

  @Override
  public List<TextCopyrightfreeSource> getAllCopyrightfreeSources(){
    List<be.webdeb.infra.persistence.model.TCopyrightfreeSource> sources =
        be.webdeb.infra.persistence.model.TCopyrightfreeSource.getAll();
    return (sources != null ? buildTextCopyrightfreeSourceList(sources) : new ArrayList<>());
  }

  @Override
  public boolean sourceIsCopyrightfree(String source, Long contributor){
    be.webdeb.infra.persistence.model.TCopyrightfreeSource freeSource =
        be.webdeb.infra.persistence.model.TCopyrightfreeSource.findByName(source);
    return freeSource != null;
  }

  @Override
  public int saveTextCopyrightfreeSource(TextCopyrightfreeSource freeSource) throws PersistenceException {
    int freeSourceId = -1;
    if(freeSource != null){
      Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
      try {
        be.webdeb.infra.persistence.model.TCopyrightfreeSource source;
        if (freeSource.getSourceId() == -1) {
          // new text
          logger.debug("start creation of free copyright source " + freeSource.getDomainName());
          source = new TCopyrightfreeSource();
          source.setIdCopyrightfreeSource(0);
          source.setDomainName(freeSource.getDomainName());
          source.save();
          freeSourceId = source.getIdCopyrightfreeSource();
          logger.info("saved " + source.toString());
        } else {
          source = be.webdeb.infra.persistence.model.TCopyrightfreeSource.findById(freeSource.getSourceId());
          if (source != null && source.getDomainName() != null && !source.getDomainName().equals(freeSource.getDomainName())) {
            // update existing
            logger.debug("update free copyright source " + freeSource.getDomainName() + WITH_ID + freeSource.getSourceId());
            source.setDomainName(freeSource.getDomainName());
            source.update();
            freeSourceId = source.getIdCopyrightfreeSource();
            logger.info("updated " + source.toString());
          }
        }
        transaction.commit();
      }catch (Exception e) {
        logger.error("unable to save free copyright source " + freeSource.getDomainName(), e);
        throw new PersistenceException(PersistenceException.Key.FREESOURCE_SAVE, e);
      } finally {
        transaction.end();
      }
    }
    return freeSourceId;
  }

  @Override
  public void removeTextCopyrightfreeSource(int idSource) throws PersistenceException {
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {
      be.webdeb.infra.persistence.model.TCopyrightfreeSource source =
          be.webdeb.infra.persistence.model.TCopyrightfreeSource.findById(idSource);
      if(source != null){
        logger.debug("start remove the free copyright source " + source.toString());
        source.delete();
        transaction.commit();
      }else{
        throw new Exception("Free source not found");
      }
    }catch (Exception e) {
      logger.error("unable to remove free copyright source " + idSource, e);
      throw new PersistenceException(PersistenceException.Key.FREESOURCE_DELETE, e);
    } finally {
      transaction.end();
    }
  }

  @Override
  public Text random() {
    try {
      return mapper.toText(be.webdeb.infra.persistence.model.Text.random());
    } catch (FormatException e) {
      logger.error("unable to cast retrieved random text", e);
    }
    return null;
  }

  @Override
  public Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> save(Text contribution, int currentGroup, Long contributor) throws PermissionException, PersistenceException {
    logger.debug("try to save text " + contribution.getOriginalTitle() + WITH_ID + contribution.getId()
            + " in group " + contribution.getInGroups());
    Contribution c = checkContribution(contribution, contributor, currentGroup);
    Contributor dbContributor = checkContributor(contributor, currentGroup);

    if(!values.isBlank(contribution.getUrl())){
      be.webdeb.infra.persistence.model.Text existingTextUrl = be.webdeb.infra.persistence.model.Text.findByUrl(contribution.getUrl());

      if(existingTextUrl != null && !existingTextUrl.getIdContribution().equals(contribution.getId())){
        logger.error("text already exists with url " + contribution.getUrl() + ", its id is " + existingTextUrl.getIdContribution());
        throw new PersistenceException(PersistenceException.Key.SAVE_TEXT);
      }
    }

    Map<Integer, List<be.webdeb.core.api.contribution.Contribution>> contributions = new HashMap<>();
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    try {

      EModificationStatus status;
      be.webdeb.infra.persistence.model.Text text;

      if (c == null) {
        // new text
        logger.debug("start creation of text " + contribution.getOriginalTitle());
        status = EModificationStatus.CREATE;

        // create contribution super type
        c = initContribution(EContributionType.TEXT.id(), contribution.getOriginalTitle());

        // create text object and binding
        text = updateText(contribution, new be.webdeb.infra.persistence.model.Text());
        c.setText(text);
        text.setContribution(c);

        c.isHidden(contribution.isHidden());

        c.save();

        if(!values.isBlank(contribution.getExternalTextId())) {
          be.webdeb.infra.persistence.model.ExternalContribution externalText =
                  be.webdeb.infra.persistence.model.ExternalContribution.findByUrlAndContributionType(contribution.getUrl(), EContributionType.TEXT);
          if (externalText != null) {
            externalText.setInternalContribution(c);
            externalText.update();
          }
        }

        // set id of text
        text.setIdContribution(c.getIdContribution());

        // manage bindings to actors
        bindActorsToContribution(c, contribution, currentGroup, contributor, contributions);
        // update groups
        updateGroups(contribution, c);

        text.save();
        c.update();
        updateContent(contribution, contributor, text);

        // set new id for given contribution and titles
        contribution.setId(c.getIdContribution());
        text.setTitles(toTextI18titles(contribution, text));
        text.update();

      } else {
        // update existing
        logger.debug("update text " + contribution.getOriginalTitle() + WITH_ID + contribution.getId());
        status = EModificationStatus.UPDATE;
        text = updateText(contribution, c.getText());
        text.setTitles(toTextI18titles(contribution, text));

        text.update();
        c.setSortkey(contribution.getOriginalTitle());
        c.update();

        for (be.webdeb.core.api.contributor.Group group : contribution.getInGroups()) {
          text.getContribution().addGroup(Group.findById(group.getGroupId()));
        }

        // manage bindings to actors, retrieve them, save unexisting ones and remove others
        updateActorBindings(contribution, text.getContribution(), currentGroup, contributor, contributions);
        // update groups
        updateGroups(contribution, c);
        c.update();

        updateContent(contribution, contributor, text);
        logger.info("updated " + text.toString());
      }

      // bind contributor to this text
      bindContributor(text.getContribution(), dbContributor, status);
      transaction.commit();
      logger.info("saved " + text.toString());

    } catch (PermissionException e) {
      // avoid having permission exceptions at contribution bindings to be re-wrapped
      throw e;

    } catch (Exception e) {
      logger.error("unable to save text " + contribution.getOriginalTitle(), e);
      throw new PersistenceException(PersistenceException.Key.SAVE_TEXT, e);

    } finally {
      transaction.end();
    }

    // return auto-created contributions
    return contributions;
  }

  /*
   * GETTERS FOR PREDEFINED VALUES
   */

  @Override
  public List<WordGender> getWordGenders() {
    if (genders == null) {
      genders  = new ArrayList<>();
      TWordGender.find.all().forEach(t ->
        genders.add(factory.createWordGender(t.getIdGender(), new LinkedHashMap<>(t.getTechnicalNames())))
      );
    }
    return genders ;
  }

  @Override
  public List<TextType> getTextTypes() {
    if (textTypes == null) {
      textTypes = TTextType.find.all().stream().map(t ->
        factory.createTextType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return textTypes;
  }

  @Override
  public List<TextSourceType> getTextSourceTypes() {
    if (textSourceTypes == null) {
      textSourceTypes = TTextSourceType.find.all().stream().map(t ->
              factory.createTextSourceType(t.getIdType(), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return textSourceTypes;
  }

  @Override
  public List<TextVisibility> getTextVisibilities() {
    if (visibilities == null) {
      visibilities = TTextVisibility.find.all().stream().map(t ->
        factory.createTextVisibility(ETextVisibility.value(t.getIdVisibility()), new LinkedHashMap<>(t.getTechnicalNames()))
      ).collect(Collectors.toList());
    }
    return visibilities;
  }

  /*
   * PRIVATE HELPERS
   */

  /**
   * Update a DB text with given API text
   *
   * @param apiText an API text with data to store
   * @param text a DB text recipient (may contain data to be updated)
   * @return given text updated with given apiText data
   */
  private be.webdeb.infra.persistence.model.Text updateText(Text apiText, be.webdeb.infra.persistence.model.Text text)
    throws PersistenceException {

    text.setTextSourceType(TTextSourceType.find.byId(apiText.getTextSourceType().getType()));
    text.setLanguage(TLanguage.find.byId(apiText.getLanguage().getCode()));
    text.setVisibility(TTextVisibility.find.byId(apiText.getTextVisibility().getType()));
    text.setUrl(apiText.getUrl());

    if(!values.isBlank(apiText.getPublicationDate()))
      text.setPublicationDate(values.toDBFormat(apiText.getPublicationDate()));

    if(apiText.getTextType() != null)
      text.setTextType(TTextType.find.byId(apiText.getTextType().getType()));

    if (apiText.getSourceTitle() != null)
      text.setSourceName(getSourceName(apiText.getSourceTitle()));

    // update visibility, but disallow putting it back to private if it was not private before
    if (text.getVisibility() != null
        && text.getVisibility().getIdVisibility() > ETextVisibility.PRIVATE.id()
        && ETextVisibility.PRIVATE == apiText.getTextVisibility().getEType()) {
      throw new PersistenceException(PersistenceException.Key.SAVE_TEXT,
          new PermissionException(PermissionException.Key.TEXT_NOT_VISIBLE));
    }

    text.setVisibility(TTextVisibility.find.byId(apiText.getTextVisibility().getType()));
    text.isFetched(apiText.isFetched());
    return text;
  }

  /**
   * Get a source object from a source name, creates a new text source in database if given name is unknown
   *
   * @param name a source name
   * @return the corresponding TextSourceName DB object
   */
  private be.webdeb.infra.persistence.model.TextSourceName getSourceName(String name) throws PersistenceException {
    Transaction transaction = Ebean.getDefaultServer().beginTransaction(TxScope.required());
    be.webdeb.infra.persistence.model.TextSourceName sourceName =
        be.webdeb.infra.persistence.model.TextSourceName.findByName(name);
    if (sourceName == null) {
      sourceName = new be.webdeb.infra.persistence.model.TextSourceName(name);
      try {
        sourceName.save();
        transaction.commit();
        return sourceName;

      } catch (Exception e) {
        logger.error("unable to save new source name " + name, e);
        throw new PersistenceException(PersistenceException.Key.SAVE_SOURCE);
      } finally {
        transaction.end();
      }
    }
    return sourceName;
  }

  /**
   * Update list of contents for a given text
   *
   * @param contribution the api text
   * @param contributor the contributor requesting the update / save
   * @param text the db text to update the content for
   * @throws PersistenceException if any persistence-related error occurred
   */
  private void updateContent(Text contribution, Long contributor, be.webdeb.infra.persistence.model.Text text) throws PersistenceException {
    try {
      if(contribution.getTextContent(contributor) != null && !contribution.getTextContent(contributor).equals("null")) {

        TextContent content;
        String filename = contribution.isContentVisibleFor(contributor) ? contribution.getFilename(contributor) : null;

        if (filename == null || !filename.contains(".")) {
          // if we do not have an external file, or file name has no extension, save content
          logger.debug("save new content for " + contribution.getId() + " of visibility " + text.getVisibility().getIdVisibility());
          content = ETextVisibility.PRIVATE == contribution.getTextVisibility().getEType() ?
                  new TextContent(text, Contributor.findById(contributor)) : new TextContent(text);

          boolean success = files.saveContributionTextFile(content.getFilename(), contribution.getTextContent(contributor));
          if (!success) {
            logger.error("unable to save text onto FS");
            throw new PersistenceException(PersistenceException.Key.SAVE_TEXT, new Exception("Unable to save file"));
          }
        } else {
          String suffix = filename.substring(filename.lastIndexOf('.'));
          // external file, content will be saved at UI layer (because it has the link to the uploaded file)
          content = ETextVisibility.PRIVATE == contribution.getTextVisibility().getEType() ?
                  new TextContent(text, Contributor.findById(contributor), suffix) : new TextContent(text, null, suffix);

        }

        // must check if text was private before because, if put to "pedagogic or public", old contents objects will be cleared
        // and this non-text user content must be copied as the publicly visible content
        Optional<TextContent> oldFilename = text.getContents() != null ?
                text.getContents().stream().filter(f -> f.getContributor() != null && contributor.equals(f.getContributor().getIdContributor())).findAny() : Optional.empty();

        if (text.addContent(content)) {
          content.save();
          text.update();

          // non text files (text files are handled right before)
          if (content.getFilename().contains(".")) {

            // if text is made private, check if we have a public content to copy
            if (text.getVisibility().getIdVisibility() == ETextVisibility.PRIVATE.id()) {
              logger.debug("external text has been made private, check if we have a public content to make private");
              Optional<TextContent> publicContent = text.getContents().stream().filter(c -> c.getContributor() == null).findAny();
              if (publicContent.isPresent()) {
                logger.debug("copy public content to be visible by contributor " + contributor);
                boolean copied = files.copyContributionFile(publicContent.get().getFilename(), content.getFilename());
                logger.info("file has been copied to " + content.getFilename() + " " + copied);
              }
            } else {
              // does this text had private content to this contributor that must be copied
              logger.debug("external text has been made public, check if we have a private content to make public");
              if (oldFilename.isPresent()) {
                logger.debug("copy private content of contributor " + contributor + " to be publicly/pedagogicaly available");
                boolean copied = files.copyContributionFile(oldFilename.get().getFilename(), content.getFilename());
                logger.info("file has been correctly copied to " + content.getFilename() + " " + copied);
              }
            }
          }
        }

        // reset filenames in api object since, if manual content has been passed, a new filename has been set,
        // and for external files, file name will be updated to follow the same name pattern and we be used by UI
        // layer to save content on disk
        contribution.setFilenames(text.getContents().stream().collect(Collectors
                .toMap(ct -> ct.getContributor() != null ? ct.getContributor().getIdContributor() : -1L, TextContent::getFilename)));
      }
    } catch (Exception e) {
      logger.error("error while updating text content of " + text.getIdContribution(), e);
      throw new PersistenceException(PersistenceException.Key.SAVE_TEXT, e);
    }
  }

  /**
   * Helper method to build a list of API text free sources from DB text free sources. All uncastable elements are ignored.
   *
   * @param freeSources a list of DB text free sources
   * @return a list of API text free sources with elements that could have actually been casted to API element (may be empty)
   */
  private List<TextCopyrightfreeSource> buildTextCopyrightfreeSourceList(List<be.webdeb.infra.persistence.model.TCopyrightfreeSource> freeSources) {
    List<TextCopyrightfreeSource> result = new ArrayList<>();
    for (be.webdeb.infra.persistence.model.TCopyrightfreeSource s : freeSources) {
      try {
        result.add(mapper.toFreeSource(s));
      } catch (FormatException e) {
        logger.error("unable to cast free copyright source " + s.getDomainName(), e);
      }
    }
    return result;
  }
}
