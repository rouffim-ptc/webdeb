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

package be.webdeb.infra.csv;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.Affiliation;
import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contribution.TextualContribution;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.ws.nlp.RequestProxy;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvBadConverterException;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.slf4j.Logger;
import play.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles the reading and parsing of CSV files meant to import actors and affiliations into webdeb.
 * It uses the opencsv library on a mapping name basis
 *
 * @author Fabian Gilson
 */
@Singleton
public class CsvImporter {

  private static String outputPath;
  private static String picturePath;
  private static String csvResult;
  private final RequestProxy proxy;
  private static final Logger logger = play.Logger.underlying();

  @Inject
  public CsvImporter(Configuration configuration, RequestProxy proxy) {
    outputPath = configuration.getString("cache.store.path");
    picturePath = configuration.getString("image.store.path");
    csvResult = configuration.getString("csv.import.result");
    File file = new File(outputPath);
    this.proxy = proxy;
    if (!file.exists()) {
      logger.info("tag for csv files has been created in " + file.getAbsolutePath() + " " + file.mkdirs());
    }
  }

  /**
   * Import content of both given file into the database and generate a csv report with the result.
   * Report is placed under returned name in csv store path
   *
   * @param actorFile a csv actor file
   * @param affiliationFile a csv affiliation file
   * @param folder the folder in which the report will be saved
   * @param charset the charset of the csv file, will be used for the export too
   * @param delimiter the delimiter used in the csv file, will be used for the export too
   * @param contributor contributor id that is requesting the import
   * @param group the group id to import the data into
   * @return the name under which the import report is stored, null if an error occurred while creating the result file
   */
  public String importCsvActor(File actorFile, File affiliationFile, String folder, String charset, char delimiter, Long contributor, int group) {
    String report = csvResult + ".csv";
    List<CsvActorResult> csvResults = new ArrayList<>();
    try {
      // map of retrieve newly created actor id when affiliations must be created for such actors
      Map<Integer, Long> actors = new HashMap<>();
      if (actorFile != null) {
        logger.info("start reading and importing " + actorFile.getName());
        List<CsvActor> csvActors = readCsvFile(actorFile, CsvActor.class, charset, delimiter);
        csvActors.forEach(csvActor -> {
          logger.debug(csvActor.toString());
          try {
            Actor actor = csvActor.toActor();
            actor.addInGroup(group);
            actor.save(contributor, group);
            if (csvActor.mustRetrieveAvatar && actor.getAvatar() != null) {
              proxy.getImageFile(csvActor.avatar, picturePath + actor.getAvatar().getPictureFilename());
            }
            actors.put(csvActor.order, actor.getId());
            csvResults.add(new CsvActorResult(csvActor.order, actor.getId()));
          } catch (FormatException | PermissionException | PersistenceException e) {
            logger.error("unable to convert actor " + csvActor, e);
            csvResults.add(new CsvActorResult(csvActor.order, csvActor.webdebId, e.getClass().getSimpleName(),
                e.getMessage() + " (init cause: " + e.getCause() + " - " + e.getMore() + ")"));
          }
        });
      }

      if (affiliationFile != null) {
        logger.info("start reading and importing " + affiliationFile.getName());
        List<CsvAffiliation> csvAffiliations = readCsvFile(affiliationFile, CsvAffiliation.class, charset, delimiter);
        csvAffiliations.forEach(csvAffiliation -> {
          logger.debug(csvAffiliation.toString());
          Long actorId = csvAffiliation.webdebId == null ? actors.get(csvAffiliation.order) : csvAffiliation.webdebId;
          Long affId = csvAffiliation.affWebdebId == null ? actors.get(csvAffiliation.affOrder) : csvAffiliation.affWebdebId;
          try {
            Affiliation affiliation = csvAffiliation.toAffiliation(actorId, affId);
            affiliation.addInGroup(group);
            affiliation.save(actorId, group, contributor);
            csvResults.add(new CsvActorResult(csvAffiliation.order, actorId, csvAffiliation.affOrder, affId));
          } catch (FormatException | PermissionException | PersistenceException e) {
            logger.error("unable to convert affiliation " + csvAffiliation, e);
            csvResults.add(new CsvActorResult(csvAffiliation.order, actorId, csvAffiliation.affOrder, affId,
                e.getClass().getSimpleName(), e.getMessage() + " (init cause: " + e.getCause() + " - " + e.getMore() + ")"));
          }
        });
      }

    } catch (Exception e) {
      logger.error("error while reading file", e);
      csvResults.add(new CsvActorResult(e.getClass().getSimpleName(), e.getMessage()));
    }

    try {
      writeCsvFile(csvResults, EContributionType.ACTOR, folder, report, charset, delimiter);
    } catch (IOException | CsvBeanIntrospectionException e) {
      logger.error("unable to create csv result file", e);
      logger.error("results were: \n" + csvResults);
      return null;
    }
    return report;
  }

  /**
   * Import content of both given file into the database and generate a csv report with the result.
   * Report is placed under returned name in csv store path
   *
   * @param citationFile a csv actor file
   * @param authorFile a csv author file
   * @param folder the folder in which the report will be saved
   * @param charset the charset of the csv file, will be used for the export too
   * @param delimiter the delimiter used in the csv file, will be used for the export too
   * @param contributor contributor id that is requesting the import
   * @param group the group id to import the data into
   * @return the name under which the import report is stored, null if an error occurred while creating the result file
   */
  public String importCsvCitation(File citationFile, File authorFile, String folder, String charset, char delimiter, Long contributor, int group) {
    String report = csvResult + ".csv";
    List<CsvCitationResult> csvResults = new ArrayList<>();
    try {
      // map of retrieve newly created citation id when authors must be created for such citations
      Map<Integer, Long> citations = new HashMap<>();
      Map<Integer, Long> texts = new HashMap<>();
      if (citationFile != null) {
        logger.info("start reading and importing " + citationFile.getName());
        List<CsvCitation> csvCitations = readCsvFile(citationFile, CsvCitation.class, charset, delimiter);
        csvCitations.forEach(csvCitation -> {
          try {
            Citation citation = csvCitation.toCitation(contributor, group, texts.get(csvCitation.textOrder));
            
            citation.addInGroup(group);
            citation.save(contributor, group);

            citations.put(csvCitation.order, citation.getId());
            texts.put(csvCitation.textOrder, citation.getTextId());
            csvResults.add(new CsvCitationResult(csvCitation.order, citation.getId()));
          } catch (FormatException | PermissionException | PersistenceException e) {
            logger.error("unable to convert citation " + csvCitation, e);
            csvResults.add(new CsvCitationResult(csvCitation.order, csvCitation.webdebId, e.getClass().getSimpleName(),
                    e.getMessage() + " (init cause: " + e.getCause() + " - " + e.getMore() + ")"));
          }
        });
      }

      if (authorFile != null) {
        logger.info("start reading and importing " + authorFile.getName());
        List<CsvAuthor> csvAuthors = readCsvFile(authorFile, CsvAuthor.class, charset, delimiter);
        csvAuthors.forEach(csvAuthor -> {
          Long citationId = csvAuthor.webdebId == null ? citations.get(csvAuthor.order) : csvAuthor.webdebId;
          if(citationId != null) {
            try {
              ActorRole role = csvAuthor.toActorRole(citationId, contributor, group);
              ((TextualContribution) role.getContribution()).addActor(role);
              role.getContribution().save(contributor, group);
              csvResults.add(new CsvCitationResult(csvAuthor.order, citationId));
            } catch (FormatException | PermissionException | PersistenceException e) {
              logger.error("unable to convert actor role " + csvAuthor, e);
              csvResults.add(new CsvCitationResult(csvAuthor.order, citationId,
                      e.getClass().getSimpleName(), e.getMessage() + " (init cause: " + e.getCause() + " - " + e.getMore() + ")"));
            }
          }
        });
      }

    } catch (Exception e) {
      logger.error("error while reading file", e);
      csvResults.add(new CsvCitationResult(e.getClass().getSimpleName(), e.getMessage()));
    }

    try {
      writeCsvFile(csvResults, EContributionType.CITATION, folder, report, charset, delimiter);
    } catch (IOException | CsvBeanIntrospectionException e) {
      logger.error("unable to create csv result file", e);
      logger.error("results were: \n" + csvResults);
      return null;
    }
    return report;
  }

  /**
   * Write given beans into output path under given filename (csv file)
   *
   * @param beans a list of beans to write into the result csv report
   * @param type the contribution type
   * @param filename the absolute path (with file name too) where to store the csv file
   * @param charset the character set of the file
   * @param delimiter the column delimiter
   * @throws IOException if given path does not exist or needed permission to create the file are not set
   */
  private <T> void writeCsvFile(List<T> beans, EContributionType type, String folder, String filename, String charset, char delimiter) throws IOException {
    try (
        Writer writer = Files.newBufferedWriter(Paths.get(outputPath + folder + File.separator + filename), Charset.forName(charset))
    ) {
      StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(writer);
      // two-step build and write, otherwise nothing is printed
      StatefulBeanToCsv<T> beantToCsv =
          builder.withSeparator(delimiter).withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).build();
      beantToCsv.write(beans);

    } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | CsvBadConverterException e) {
      logger.error("unable to write csv file " + beans, e);
    }
  }

  /**
   * Read from given CSV file path and returns all mapped beans of given clazz.
   *
   * @param file a CSV file
   * @param clazz the bean class to map the csv file lines into
   * @param charset the character set of the file
   * @param delimiter the column delimiter
   * @return the list of retrieved csv bean actors from given path
   * @throws FileNotFoundException if given file does not exist
   * @throws IllegalStateException if an error occurred with the parser (wrong csv format or mapper)
   */
  private <T> List<T> readCsvFile(File file, Class<T> clazz, String charset, char delimiter) throws IOException {
    return new CsvToBeanBuilder<T>(new InputStreamReader(new FileInputStream(file), charset)).withType(clazz).withSeparator(delimiter).build().parse();
  }
}
