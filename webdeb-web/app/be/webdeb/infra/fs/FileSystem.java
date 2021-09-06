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

package be.webdeb.infra.fs;

import be.webdeb.core.api.contribution.EContributionType;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.imgscalr.Scalr;
import play.Configuration;
import play.cache.CacheApi;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handle file system files. <p> Using Strings to read/write files, so they must be small enough to be loaded
 * in a String, i.e. MAXINT char size.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class FileSystem {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private static String contributionPath;
  private static String annotatedPath;
  private static String cachePath;
  private static String imagePath;
  private static String usrimagePath;
  private static String projectPath;
  private static String sitemapPath;
  private static int maximageWidth;
  private static int maximageBigWidth;

  private static String fileCachekey = "file.";
  private static final String ENCODING = "UTF-8";
  private static final int CACHETIME = 60 * 5;
  private static final int BUFFER_SIZE = 1024;

  private CacheApi cache;


  /**
   * Injected constructor
   *
   * @param configuration the play configuration module
   * @param cache the play cache API
   */
  @Inject
  public FileSystem(Configuration configuration, CacheApi cache) {
    this.cache = cache;

    // load configuration data
    contributionPath = configuration.getString("contribution.store.path");
    annotatedPath = configuration.getString("annotated.store.path");
    cachePath = configuration.getString("cache.store.path");
    imagePath = configuration.getString("image.store.path");
    usrimagePath = configuration.getString("userimage.store.path");
    projectPath = configuration.getString("project.store.path");
    sitemapPath = configuration.getString("sitemap.store.path");
    maximageWidth = configuration.getInt("image.store.width");
    maximageBigWidth = configuration.getInt("image.store.big.width");

    final String CREATED_PIC = "tag for pics has been created in ";

    // create FS directories if not existing
    File file = new File(contributionPath);
    if (!file.exists()) {
      logger.info("tag for texts has been created in " + file.getAbsolutePath() + " " + file.mkdirs());
    }
    file = new File(annotatedPath);
    if (!file.exists()) {
      logger.info("tag for annotated text has been created in " + file.getAbsolutePath() + " " + file
          .mkdirs());
    }
    file = new File(cachePath);
    if (!file.exists()) {
      logger.info("tag for cache has been created in " + file.getAbsolutePath() + " " + file.mkdirs());
    }
    file = new File(imagePath);
    if (!file.exists()) {
      logger.info(CREATED_PIC + file.getAbsolutePath() + " " + file.mkdirs());
    }
    file = new File(usrimagePath);
    if (!file.exists()) {
      logger.info(CREATED_PIC + file.getAbsolutePath() + " " + file.mkdirs());
    }
    file = new File(usrimagePath);
    if (!file.exists()) {
      logger.info(CREATED_PIC + file.getAbsolutePath() + " " + file.mkdirs());
    }
  }

  /**
   * Get content of a file corresponding to a text contribution
   *
   * @param filename the name of a file to retrieve on the FS under the dedicated path for texts
   * @return the content of the text file pointed by the given id, or null if not found
   */
  public String getContributionTextFile(String filename) {
    return getFileFromFS(filename, contributionPath, 0, 0);
  }

  /**
   * Get citation of a file corresponding to a text contribution
   *
   * @param filename the name of a file to retrieve on the FS under the dedicated path for texts
   * @param startIndex start index from file to read (in byte)
   * @param endIndex end index from file to read (in byte)
   * @return the content of the citation of the text file pointed by the given id, or null if not found
   */
  public String getContributionTextFile(String filename, int startIndex, int endIndex) {
    return getFileFromFS(filename, contributionPath, startIndex, endIndex);
  }

  /**
   * Save a contribution text into file system
   *
   * @param filename the filename to use on the file system
   * @param content the content of the file
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveContributionTextFile(String filename, String content) {
    return saveFileToFS(filename, contributionPath, content);
  }

  /**
   * Save a contribution text into file system
   *
   * @param filename the filename to use on the file system
   * @param file a file to save
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveContributionTextFile(String filename, File file) {
    try {
      if(!file.setReadable(true, false)){
        throw new IOException();
      }
      Files.move(file.toPath(), Paths.get(contributionPath + filename),
          StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      logger.error("unable to save file to " + contributionPath, e);
      return false;
    }
  }

  /**
   * Copy given contribution source file to given destination
   *
   * @param source a source file name (that must be present in contribution's repository)
   * @param destination a destination file name (will be overwitten if existing)
   * @return true if the copy action succeeded, false otherwise
   */
  public boolean copyContributionFile(String source, String destination) {
    logger.debug("will copy file " + source + " to " + destination);
    return saveContributionTextFile(destination, new File(contributionPath + source));
  }

  /**
   * Get content of a file corresponding to a text contribution
   *
   * @param filename the name of a file to retrieve on the FS under the dedicated path for annotated texts
   * @return the content of the text file pointed by the given id, or null if not found
   */
  public String getAnnotatedTextFile(String filename) {
    return getFileFromFS(filename + ".xml", annotatedPath, 0, 0);
  }

  /**
   * Save an annotated text into file system
   *
   * @param filename the filename to use on the file system
   * @param content the content of the file
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveAnnotatedTextFile(String filename, String content) {
    return saveFileToFS(filename, annotatedPath, content);
  }

  /**
   * Save a picture file into file system
   *
   * @param file a file to save
   * @param name a name for the file to save
   * @param bigSize true if max image width must be big or normal
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean savePictureFile(File file, String name, boolean bigSize) {
    return saveFile(getResizedImage(file, name, bigSize), imagePath + name);
  }

  /**
   * Delete a user picture file into file system
   *
   * @param name a name for the file to delete
   * @return true if the file was successfull deleted, false otherwise
   */
  public boolean deleteUserPictureFile(String name) {
    return deleteFileFromFS(name, usrimagePath + name);
  }

  /**
   * Save a user picture file into file system
   *
   * @param file a file to save
   * @param name a name for the file to save
   * @param bigSize true if max image width must be big or normal
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveUserPictureFile(File file, String name, boolean bigSize) {
    return saveFile(getResizedImage(file, name, bigSize), usrimagePath + name);
  }

  /**
   * Save a given file in cache file system
   *
   * @param file a file to save
   * @param name the name for the file to save
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveToCache(File file, String name) {
    return saveFile(file, cachePath + name);
  }

  /**
   * Save a given csv file in temp file system
   *
   * @param file a csv file to save
   * @param folder the tag name for the file to save (because csv are stored into dedicated folders per imports)
   * @param name the name for the file to save
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveCsvFile(File file, String folder, String name) {
    return saveFile(file, cachePath + folder + File.separator + name);
  }

  /**
   * Get content of a project users file in file system
   *
   * @param filename the name of a file to retrieve on the FS under the dedicated path for project users text
   * @return the content of the text file pointed by the given id, or null if not found
   */
  public String getProjectUsersFile(String filename) {
    return getFileFromFS(filename, projectPath, 0, 0);
  }

  /**
   * Get a sitemap file
   *
   * @param filename the name of sitemap file
   * @return the sitemap file
   */
  public String getSitemapFile(String filename) {
    return getFileFromFS(filename + ".xml", sitemapPath, 0, 0);
  }

  /**
   * Delete a sitemap file from the sitemap store
   *
   * @param filename the filename in the sitemap repo to delete
   * @return true if the deletion succeeded, false otherwise
   */
  public boolean deleteSitemapFile(String filename) {
    return deleteFileFromFS(filename + ".xml", sitemapPath);
  }

  /**
   * Save a given sitemap file in file system
   *
   * @param file a sitemap file to save
   * @param name the name for the file to save
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveSitemapFile(File file, String name) {
    return saveFile(file, sitemapPath + name + ".xml");
  }

  /**
   * Save a given project users file in file system
   *
   * @param file a csv file to save
   * @param name the name for the file to save
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveProjectUsersFile(File file, String name) {
    return saveFile(file, projectPath + name);
  }

  /**
   * Get the map of all csv files that have been imported with their reports,
   * of the form <date, [csv_actor, csv_affiliation, csv_import]>.
   *
   * @param type the contribution type that need reports
   * @return a map of file names
   */
  public Map<String, List<String>> listCsvFile(EContributionType type) {
    Map<String, List<String>> reports = new TreeMap<>(Comparator.reverseOrder());
    File csvFolder = new File(cachePath + type + "/");
    if (csvFolder.listFiles() != null) {
      Arrays.stream(csvFolder.listFiles()).forEach(folder -> {
        if (folder.listFiles() != null) {
          reports.put(folder.getName(), Arrays.stream(folder.listFiles()).map(File::getName).collect(Collectors.toList()));
        }
      });

      // lucky actor, affiliation and import are ascending alphabetical order
      reports.values().forEach(l -> l.sort(Comparator.naturalOrder()));
    }
    return reports;
  }


  /**
   * Retrieve a given file from cache file system
   *
   * @param name the name for the file to retrieve from cache
   * @return the file in cache with given name, or null if file does not exist in cache
   */
  public File getFromCache(String name) {
    File file = new File(cachePath + name);
    if (file.exists()) {
      return file;
    }
    logger.debug("unfound file from cache " + name);
    return null;
  }

  /**
   * Save a file from given stream into given path
   *
   * @param inputStream a stream to read from
   * @param savepath a path where the content of the inpustrean will be saved
   * @return the saved file
   */
  public File getRawFile(InputStream inputStream, String savepath) {
    final File file = new File(savepath);
    try (OutputStream outputStream =
                 new FileOutputStream(file)){
      int read;
      byte[] buffer = new byte[BUFFER_SIZE];

      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
      return file;

    } catch (IOException e) {
      logger.error("unable to write retrieved file to " + savepath, e);
      return null;

    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.error("unable close input stream when saving file to " + savepath, e);
        }
      }
    }
  }

  /**
   * Delete a file from the annotated store ("annotated.store.path" as defined in application.conf)
   *
   * @param filename the filename in the annotated text repo to delete
   * @return true if the deletion succeeded, false otherwise
   */
  public boolean deleteAnnotatedText(String filename) {
    return deleteFileFromFS(filename + ".xml", annotatedPath);
  }

  /**
   * Check if a given file has some content (from contribution file repository)
   *
   * @param filename a filename for a contribution
   * @return true if given file has content
   */
  public boolean isEmpty(String filename) {
    return new File(contributionPath + filename).length() == 0;
  }

  /**
   * Check if given content contains given citation
   *
   * @param content a content
   * @param excerpt an citation
   * @return true if citation is contained in content, false otherwise
   */
  public boolean textContainsExcerpt(String content, String excerpt) {
    if (content == null || excerpt == null) {
      return false;
    }
    int[] indices = getIndices(content, excerpt);
    return indices[0] != -1 && indices[1] != -1;
  }

  /**
   * Retrieve the mime type as defined by the RFC 2045.
   *
   * @param filename a filename to probe (must be placed in the contribution_path)
   * @return the mime type, or null if an error occurred
   */
  @SuppressWarnings("fallthrough")
  public String getContentType(String filename) {
    String type = null;
    try {
      type = Files.probeContentType(Paths.get(contributionPath + filename));
    } catch (IOException e) {
      logger.error("unable to get content type for " + filename, e);
    }
    // because of a bug on oracle implementation, sometimes, probe returns null, so try to guess
    // by file extension, if any, otherwise assume it is plain text.
    if (type == null) {
      //logger.warn("probing content type returned null for " + filename + ", guessing by extension");
      // probe on extension, not ideal...
      if (filename.contains(".")) {
        logger.debug(filename.substring(filename.lastIndexOf('.')));
        switch (filename.substring(filename.lastIndexOf('.'))) {
          case ".pdf":
            type = "application/pdf";
            break;
          case ".xml":
            type = "application/xml";
            break;
          case ".png":
            type = "image/png";
            break;
          case ".jpeg":
          case ".jpg":
            type = "image/jpeg";
            break;
          case ".gif":
            type = "image/gif";
            break;
          case ".svg":
            type = "image/svg+xml";
            break;
          default:
            type = "unknown";
            logger.warn("unrecognized file extension for " + filename);
        }
      } else {
        type = "text/plain";
      }
    }
    return type;
  }

  /**
   * Move given origin picture file to given destination (using pre-configured image path)
   *
   * @param origin the origin picture file name
   * @param destination the destination file name
   * @return true if the origin has been moved to given destination
   */
  public boolean movePictureFile(String origin, String destination) {
    try {
      Files.move(Paths.get(imagePath + origin), Paths.get(imagePath + destination),
          StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      logger.error("unable to move picture from " + origin + " to " + destination, e);
      return false;
    }
  }

  /*
   * PRIVATE CONVENIENCE METHODS
   */

  /**
   * Read a file and wraps its content into a String (file may not be larger than MAXINT)
   *
   * @param filename an absolute path to the file to read
   * @param path an absolute path where the file is stored
   * @param startIndex the index in byte from which the content must be retrieved in the given file
   * @param endIndex the index in byte to which the content must be retrieved in the given file
   * @return the content of the given file, possibly truncated to the given size, or an empty string if an error occurred
   */
  @SuppressWarnings("fallthrough")
  private String getFileFromFS(String filename, String path, long startIndex, long endIndex) {

    long start = startIndex;
    long end = endIndex;

    if (start < 0 || end < 0) {
      logger.error("indices have wrong values " + start + ", " + end + " for " + filename);
      start = start < 0 ? 0 : start;
      end = end < 0 ? 0 : end;
    }

    if (filename == null || path == null) {
      logger.error("given path or filename is invalid " + path + filename);
      return "";
    }

    String cached = cache.get(fileCachekey + filename);
    if (cached != null && !"".equals(cached)) {
      if (end == 0) {
        return cached;
      } else {
        if (cached.length() < end) {
          end = (long) cached.length() - 1;
        }
      }

      try {
        return cached.substring((int) start, (int) end);
      } catch (StringIndexOutOfBoundsException e) {
        logger.debug("invalidate cache", e);
        cache.remove(fileCachekey + filename);
      }
    }

    String mimetype = getContentType(filename);
    switch (mimetype) {
      case "text/plain":
      case "application/xml":
        return getPlainContent(path + "/" + filename, start, end);
      case "application/pdf":
        return getPDFContent(path + "/" + filename, (int) start, (int) end);
      default:
        logger.error("unsupported file type " + mimetype + " for file " + filename);
        return "";
    }

  }

  /**
   * Get textual content of given plain text file
   *
   * @param path a file path
   * @param start begin index
   * @param end end index
   * @return the content of this plain text between given start and end indices
   */
  private String getPlainContent(String path, long start, long end) {
    File file = new File(path);
    StringBuilder content = new StringBuilder();

    // compute size to read
    long size = end == 0 ? file.length() : end - start;

    if (file.exists()) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), ENCODING))) {

        // skip first characters (may be 0)
        reader.skip(start);

        // start reading
        int curlength;
        char[] read = new char[BUFFER_SIZE];
        while ((curlength = reader.read(read)) > 0 && content.length() < size) {
          content.append(read, 0, curlength);
        }

        // we may have appended too much
        if (size < file.length()) {
          content.setLength((int) size);
        }

        // set file in cache if it has been fully read
        if (end == 0) {
          cache.set(fileCachekey + path, content.toString(), CACHETIME);
        }

      } catch (IOException e) {
        logger.error("unable to read file " + path, e);
        return null;
      }
    }
    return content.length() > 0 ? content.toString() : "";
  }

  /**
   * Get textual content of given PDF file
   *
   * @param path a file path
   * @param start begin index
   * @param end end index
   * @return the "textified" content of this pdf between given start and end indices, an empty string if any error occurred
   */
  private String getPDFContent(String path, int start, int end) {
    PDDocument document = null;
    try {
      document = PDDocument.load(new File(path));
      if (!document.isEncrypted()) {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDFTextStripper textStripper = new PDFTextStripper();
        String content = textStripper.getText(document);

        // if start index is too far, just return null
        if (content.length() <= start) {
          logger.warn("given start index " + start + " is too far for " + path + " of size " + content.length());
          return "";
        }
        if (content.length() > end) {
          return content.substring(start, end);
        }
        return content;
      } else {
        logger.warn("pdf document " + path + " is encrypted, unable to read content from");
      }
    } catch (IOException e) {
      logger.error("unable to read pdf content of " + path, e);
    } finally {
      if (document != null) {
        try {
          document.close();
        } catch (IOException e) {
          logger.warn("unable to close pdf file " + path, e);
        }
      }
    }
    return "";
  }

  /**
   * Persist a file to the file system
   *
   * @param filename a filename
   * @param basepath an absolute path to the directory where the file will be saved
   * @param content the content to the file
   * @return true if the file has been persisted onto the FS
   */
  private boolean saveFileToFS(String filename, String basepath, String content) {
    logger.debug("saving file to " + basepath + filename);
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(basepath + filename), ENCODING))) {
      writer.write(content);
      writer.flush();

      // set file in cache
      cache.set(fileCachekey + filename, content, CACHETIME);
      return true;

    } catch (IOException e) {
      logger.error("unable to save file " + filename, e);
      return false;
    }
  }

  /**
   * Delete a file from the file system
   *
   * @param filename the file name
   * @param basepath an absolute path to the directory where the file is located
   * @return true if the file was deleted, false otherwise
   */
  private boolean deleteFileFromFS(String filename, String basepath) {
    boolean success = new File(basepath + filename).delete();
    cache.remove(fileCachekey + filename);
    logger.debug("deleted file from FS " + basepath + filename);
    return success;
  }


  /**
   * Save a given file under given path
   *
   * @param file a file to save
   * @param name the absolute path under which the file must be saved
   * @return true if the file was successfully saved, false otherwise
   */
  private boolean saveFile(File file, String name) {
    if (file == null) {
      logger.error("cannot save null file under " + name);
      return false;
    }
    logger.debug("will save file " + file.getName() + " under " + name);
    try {
      FileUtils.copyFile(file, new File(name));
    } catch (IOException e) {
      logger.error("unable to move temp file " + file.getName() + " to " + name, e);
      return false;
    }
    return true;
  }

  /**
   * Get indices in given content of given citation
   *
   * @param content a string content
   * @param excerpt a string citation to find in given content
   * @return a int array with start and end indices where given citation is located (character counts) into given content.
   * Sends back [-1, -1] if citation is not found.
   */
  private int[] getIndices(String content, String excerpt) {
    // replace regex reserved characters
    String wrappedExcerpt = excerpt
        .replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
        .replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
        .replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")
        .replaceAll("\\>", "\\\\>").replaceAll("\\<", "\\\\<")
        .replaceAll("\\+", "\\\\+").replaceAll("\\*", "\\\\*")
        .replaceAll("\\?", "\\\\?").replaceAll("\\!", "\\\\!")
        .replaceAll("\\s+", "(\\\\s*)");

    Pattern p = Pattern.compile(wrappedExcerpt);
    Matcher m = p.matcher(content);
    if (m.find()) {
      return new int[]{m.start(), m.end()};
    }
    // return default values [-1, -1]
    return new int[]{-1, -1};
  }

  /**
   * Resize an image file to max maximageWidth size and save as png
   *
   * @param file a file containing an image
   * @param bigSize true if max image width must be big or normal
   * @return the resized image or given file if any resizing error occurred
   */
  private File getResizedImage(File file, String name, boolean bigSize) {
    // probing on file type will not work most of the time because files will come from temp cache
    // try to resize it and catch exception even if it is not the best way to do it
    File tosave = new File(cachePath + name);
    try {
      BufferedImage image = ImageIO.read(file);
      BufferedImage scaledImage = Scalr.resize(image, bigSize ? maximageBigWidth : maximageWidth);
      ImageIO.write(scaledImage, name.substring(name.lastIndexOf('.') + 1), tosave);
    } catch (Exception e) {
      tosave = file;
      logger.error("unable to resize image " + file.getAbsolutePath(), e);
    }
    return tosave;
  }

}
