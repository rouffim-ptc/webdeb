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
 *
 */

package be.webdeb.application.nlphelper;

import be.webdeb.core.api.citation.Citation;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.presentation.web.controllers.entry.citation.CitationSimpleForm;
import be.webdeb.presentation.web.controllers.entry.text.TextHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.views.html.util.tooltipBox;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to handle results from the WDTAL annotation service.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class WDTALAnnotator {

  // custom logger
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  // hardcoded default HEADER
  private static final String HEADER = "<?xml version='1.0' encoding='utf-8'?>";

  protected CacheApi cache;
  protected FormFactory formFactory;

  private static final String ANNOTATED_RAW = "annotated.raw.";
  private static final String ANNOTATED_USER = "annotated.user.";

  /**
   * Injected constructor
   *
   * @param cache the play cache API
   * @param formFactory the play form factory
   */
  @Inject
  public WDTALAnnotator(CacheApi cache, FormFactory formFactory) {
    this.cache = cache;
    this.formFactory = formFactory;
  }

  /**
   * Check if the given content is an xml file (very naive verification)
   * reference a schema for syntactical validation
   *
   * @param content a string content
   * @return true if it starts with a default xml HEADER
   */
  public boolean isExpectedContent(String content) {
    return content.startsWith(HEADER);
  }

  /**
   * Create an unannotated file where carriage returns are simply replaced by html "br" tags.
   *
   * @param content a content
   * @return the same content with carriage returns replaced by html br tags
   */
  public String createUnannotatedFile(String content) {
    return content.replace("\r", "<br>");
  }

  /**
   * Parse xml content and transform it as an html content with spans
   *
   * @param content an xml content
   * @return an html representation of the given xml content where annotated elements have been put in span html tags or
   * given unmodified content if it does not look like a valid xml content
   */
  private String transform(String content) {
    if (!isExpectedContent(content)) {
      return content;
    }
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty("javax.xml.stream.isCoalescing", true);
    StringReader sreader = new StringReader(content);
    StringBuilder result = new StringBuilder();
    StringBuilder current = new StringBuilder();
    // since all <unit> tags are not meant to be highlighted, only consider the ones with attributes
    // so we're appending spans only when necessary
    boolean closeMe = false;

    try {
      XMLStreamReader reader = factory.createXMLStreamReader(sreader);

      while (reader.hasNext()) {
        switch (reader.next()) {
          case XMLStreamConstants.START_ELEMENT:
            switch (reader.getLocalName()) {
              case "unit":
                closeMe = reader.getAttributeCount() > 0;
                if (closeMe) {
                  current.append("<span");
                  String type = reader.getAttributeValue("", "type");
                  switch (type) {
                    case "entities":
                      current.append(" class=\"").append(reader.getAttributeValue("", "subclass")).append("\"");
                      break;
                    case "conn":
                      current.append(" class=\"conn\"");
                      break;
                    default:
                      logger.warn("unrecognized tag type " + type);
                      break;
                  }
                  current.append(">");
                }
                break;

              case "br":
                current.append("<br/>");
                break;

              default:
                break;
            }
            break;

          case XMLStreamConstants.CHARACTERS:
            current.append(reader.getText());
            break;

          case XMLStreamConstants.END_ELEMENT:
            if ("unit".equals(reader.getLocalName())) {
              if (closeMe) {
                current.append("</span>");
                closeMe = false;
              }
              result.append(current.toString().replace("\n", ""));
              current.setLength(0);
            }
            break;

          default:
            // ignore
        }
      }

    } catch (XMLStreamException e) {
      logger.error("unable to transform content " + content, e);
      return content;
    }
    return result.toString();
  }


  /**
   * Highlight a given text with all citations belonging to it.
   *
   * @param id the text id
   * @param text a text to be annotated
   * @param citations the list of citations belonging to the given text
   * @param user the user object to check if a cached text can be found
   * @param lang 2-char ISO code of context language (among play accepted languages)
   * @return the given text with all arguments being highlighted, i.e, surrounded by html spans of the form
   * <pre>
   * <span class="highlight" data-argid="a.id"></span>,
   * </pre>
   * where multiple ids may be put in the the data-argid since other spans (from the annotated version) may be present
   */
  // TODO when there is a \n is the citation in the text, the citation doesn't have this \n so the match doesn't happend
  public String highlightText(Long id, final String text, List<Citation> citations, WebdebUser user, String lang) {

      String annotatedKey = ANNOTATED_USER + id + "." + user.getId() + "." + user.getGroup().getGroupId() + "." +
              citations.stream().map(Citation::hashCode).reduce(0, (a, b) -> a + b);
      String result;
      final int cacheTime = 300;

      // check if a raw annotated text exists
      if (cache.get(ANNOTATED_RAW + id) == null) {
          // no annotated text in cache, transform given text and set in cache
          result = transform(text);
          cache.set(ANNOTATED_RAW + id, result, cacheTime);
      } else {
          // annotated exist in cache, reuse it to re-annotate only necessary elements
          result = cache.get(ANNOTATED_RAW + id).toString();
      }

      String tag = "<[^>]+>";
      String multiTag = "(" + tag + ")*+\\\\s*";
      Pattern tagPattern = Pattern.compile(tag);
      Matcher tagMatcher;
      Map<List<String>, List<Html>> popoverboxes = new LinkedHashMap<>();
      List<Html> popoverbox;
      List<String> keys;
      Map<Long, Citation> remaining = citations.stream().collect(Collectors.toMap(Contribution::getId, e -> e));

      for (Citation e : citations) {
          if (remaining.containsKey(e.getId()) && cache.get(annotatedKey + ".citation." + e.getId()) == null) {
              remaining.remove(e.getId());
              cache.set(annotatedKey + "citation." + e.getId(), "true", cacheTime);
              // cleanup citation and protect regex reserved characters
              String toFind = e.getOriginalExcerpt()
                      .replaceAll("\\(", "\\\\(").replaceAll("\\)", " \\\\)")
                      .replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
                      .replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\} ")
                      .replaceAll("\\+", "\\\\+").replaceAll("\\*", "\\\\*")
                      .replaceAll("'", "'" + multiTag).replaceAll("’", "’" + multiTag)
                      .replaceAll("\"", multiTag + "\"" + multiTag).replaceAll("\\?", "\\\\?")
                      .replaceAll("\\.", multiTag + "\\\\.").replaceAll(";", multiTag + ";")
                      .replaceAll(",", multiTag + ",").replaceAll(":", multiTag + ":")
                      .replaceAll("-", multiTag + "-" + multiTag).replaceAll("–", multiTag + "–" + multiTag)
                      .replaceAll("\\s+", multiTag);

              // now must replace protected parenthesis by parenthesis + multitag since
              // those bloody tags may be anywhere and if we replace them all in once,
              // bad things happens with regexp
              toFind = toFind.replaceAll("\\\\\\(", multiTag + "\\\\(" + multiTag);
              toFind = toFind.replaceAll("\\\\\\)",  multiTag + "\\\\)" + multiTag);

              // search for argument in text
              Pattern p = Pattern.compile(toFind);
              Matcher m = p.matcher(result);
              if (m.find()) {
                  String citation = m.group();
                  // possible existing tags to handle
                  String priorTagToClose = "";
                  String priorTagToOpen = "";
                  String followingTagToClose = "";
                  String followingTagToOpen = "";

                  // check first match if we aren't in a html element already
                  tagMatcher = tagPattern.matcher(citation);
                  if (tagMatcher.find()) {
                      String html = tagMatcher.group();
                      if (html.contains("/")) {
                          // closing tag found, must close and reopen prior to argument
                          String tagName = html.substring("</".length(), html.indexOf('>'));
                          String openhtml = result.substring(0, m.start());
                          int tagIdx = openhtml.lastIndexOf(tagName);
                          openhtml = openhtml.substring(tagIdx, openhtml.indexOf('>', tagIdx));
                          priorTagToClose = "</" + tagName + ">";
                          priorTagToOpen = "<" + openhtml + ">";
                      }

                      // loop all matches and check last one if it is closed
                      String lastTag = null;
                      while (tagMatcher.find()) {
                          lastTag = citation.substring(tagMatcher.start() + 1, tagMatcher.end() - 1);
                      }
                      // if last tag found is a not a closing one, we must close and reopen tag
                      if (lastTag != null && !lastTag.contains("/")) {
                          String existingTag = lastTag;
                          if (existingTag.contains(" ")) {
                              // extract tag name only
                              existingTag = existingTag.substring(0, existingTag.indexOf(' '));
                          }
                          followingTagToClose = "</" + existingTag + ">";
                          followingTagToOpen = "<" + lastTag + ">";
                      }
                  }

                  List<Long> argIds = new ArrayList<>();
                  argIds.add(e.getId());
                  StringBuilder standardForm = new StringBuilder(e.getOriginalExcerpt());

                  // create anchor keys to know where to put boxes afterwards
                  keys = new ArrayList<>();
                  keys.add("<excid id=" + e.getId() + ">");
                  keys.add("</excid id=" + e.getId() + ">");

                  // create forms that will be put behind the temp keys to edit arguments

                  // if we have overlapping citation, must handle them all in once
                  List<Citation> overlapping = remaining.values().stream().filter(exc ->
                          overlap(e.getOriginalExcerpt(), exc.getOriginalExcerpt())).collect(Collectors.toList());

                  if (!overlapping.isEmpty()) {
                      // matching citation may be full of html garbage, must clean it for highlighting zone calculation
                      // TODO temp-solution as it hides all overlapping arguments behind first one, complete highlight must be managed
                      for (Citation exc : overlapping) {
                          // append for data-argid field
                          argIds.add(exc.getId());
                          // add standard form to current one
                          standardForm.append("<br style=\"margin-bottom:10px\">").append(exc.getOriginalExcerpt());
                          // remove from remaining this argument since we already handled it
                          remaining.remove(exc.getId());
                      }
                  }

                  String ids = String.join("_", argIds.stream().map(Object::toString).collect(Collectors.toList()));
                  // build and save popover boxes to be added later on
                  popoverbox = new ArrayList<>();
                  popoverbox.add(tooltipBox.render(
                          "tooltip-" + e.getId(),
                          "",
                          "highlight",
                          Html.apply(standardForm.toString()),
                          "data-excid=\"" + ids + "\"",
                          Html.apply(priorTagToOpen + citation + followingTagToClose)));

                  popoverboxes.put(keys, popoverbox);

                  // now surround citation with anchor to add popover box in second pass
                  // doing it in two passes because we may have overlapping boxes and
                  // pattern matcher won't find them if boxes are added on the fly.
                  // previous tag that we must close and reopen
                  StringBuilder citationBuilder = new StringBuilder();
                  citationBuilder.append(priorTagToClose);
                  // open anchor for popovers
                  citationBuilder.append(keys.get(0));
                  // citation as is
                  citationBuilder.append(citation);
                  // close anchor
                  citationBuilder.append(keys.get(1));
                  // tag that has been cut and must be reopened
                  citationBuilder.append(followingTagToOpen);
                  // substitute found argument with updated one
                  result = result.substring(0, m.start()) + citationBuilder + result.substring(m.end());

              } else {
                  logger.warn("unable to highlight citation " + e.getId() + " \"" + e.getOriginalExcerpt());
              }
          }
      }

      // now add popover-boxes
      for (Map.Entry<List<String>, List<Html>> entry : popoverboxes.entrySet()) {
          Pattern p = Pattern.compile(entry.getKey().get(0) + "(.+?)" + entry.getKey().get(1));
          Matcher m = p.matcher(result);
          if (m.find()) {
              result = result.substring(0, m.start())
                      + entry.getValue().get(0).toString()
                      + entry.getValue().get(1).toString()
                      + result.substring(m.end());
          }
      }

      // put in cache computed result
      cache.set(annotatedKey + id, result, cacheTime);
      return result;
  }
  /*public String highlightText(Long id, final String text, List<Citation> citations, WebdebUser user, String lang) {

    String annotatedKey = ANNOTATED_USER + id + "." + user.getId() + "." + user.getGroup().getGroupId() + "." +
            citations.stream().map(Citation::hashCode).reduce(0, (a, b) -> a + b);
    String result;
    final int cacheTime = 300;

    // check if a raw annotated text exists
    if (cache.get(ANNOTATED_RAW + id) == null) {
      // no annotated text in cache, transform given text and set in cache
      result = text;
      cache.set(ANNOTATED_RAW + id, result, cacheTime);
    } else {
      // annotated exist in cache, reuse it to re-annotate only necessary elements
      result = cache.get(ANNOTATED_RAW + id).toString();
    }

    String tag = "<[^>]+>";
    String multiTag = "(" + tag + ")*+\\\\s*";
    Pattern tagPattern = Pattern.compile(tag);
    Matcher tagMatcher;
    Map<List<String>, List<Html>> popoverboxes = new LinkedHashMap<>();
    List<Html> popoverbox;
    List<String> keys;
    Map<Long, Citation> remaining = citations.stream().collect(Collectors.toMap(Contribution::getId, e -> e));

    for (Citation e : citations) {
      if (remaining.containsKey(e.getId()) && cache.get(annotatedKey + ".citation." + e.getId()) == null) {
        remaining.remove(e.getId());
        cache.set(annotatedKey + "citation." + e.getId(), "true", cacheTime);
        // cleanup citation and protect regex reserved characters
        String toFind = e.getOriginalCitation()
            .replaceAll("\r", "<br><br>")
            .replaceAll("'", "’");
        if(toFind.endsWith("<br><br>")) toFind = toFind.substring(0, toFind.length() - 8);

        // now must replace protected parenthesis by parenthesis + multitag since
        // those bloody tags may be anywhere and if we replace them all in once,
        // bad things happens with regexp
        //toFind = toFind.replaceAll("\\\\\\(", multiTag + "\\\\(" + multiTag);
        //toFind = toFind.replaceAll("\\\\\\)",  multiTag + "\\\\)" + multiTag);

        // search for citation in text
        Pattern p = Pattern.compile(toFind);
        Matcher m = p.matcher(result);
        if (m.find()) {
          String citation = m.group();
          // possible existing tags to handle
          String priorTagToClose = "";
          String priorTagToOpen = "";
          String followingTagToClose = "";
          String followingTagToOpen = "";

          // check first match if we aren't in a html element already
          tagMatcher = tagPattern.matcher(citation);
          if (tagMatcher.find()) {
            String html = tagMatcher.group();
            if (html.contains("/")) {
              // closing tag found, must close and reopen prior to citation
              String tagName = html.substring("</".length(), html.indexOf('>'));
              String openhtml = result.substring(0, m.start());
              int tagIdx = openhtml.lastIndexOf(tagName);
              openhtml = openhtml.substring(tagIdx, openhtml.indexOf('>', tagIdx));
              priorTagToClose = "</" + tagName + ">";
              priorTagToOpen = "<" + openhtml + ">";
            }

            // loop all matches and check last one if it is closed
            String lastTag = null;
            while (tagMatcher.find()) {
              lastTag = citation.substring(tagMatcher.start() + 1, tagMatcher.end() - 1);
            }
            // if last tag found is a not a closing one, we must close and reopen tag
            if (lastTag != null && !lastTag.contains("/")) {
              String existingTag = lastTag;
              if (existingTag.contains(" ")) {
                // extract tag name only
                existingTag = existingTag.substring(0, existingTag.indexOf(' '));
              }
              followingTagToClose = "</" + existingTag + ">";
              followingTagToOpen = "<" + lastTag + ">";
            }
          }

          List<Long> excIds = new ArrayList<>();
          excIds.add(e.getId());
          StringBuilder originalCitation = new StringBuilder(e.getOriginalCitation());

          // create anchor keys to know where to put boxes afterwards
          keys = new ArrayList<>();
          keys.add("<excid id=" + e.getId() + ">");
          keys.add("</excid id=" + e.getId() + ">");

          // create forms that will be put behind the temp keys to edit citations

          // if we have overlapping citation, must handle them all in once
          List<Citation> overlapping = remaining.values().stream().filter(exc ->
              overlap(e.getOriginalCitation(), exc.getOriginalCitation())).collect(Collectors.toList());

          if (!overlapping.isEmpty()) {
            // matching citation may be full of html garbage, must clean it for highlighting zone calculation
            // TODO temp-solution as it hides all overlapping arguments behind first one, complete highlight must be managed
            for (Citation exc : overlapping) {
              // append for data-excid field
              excIds.add(exc.getId());
              // add original citation to current one
              originalCitation.append("<br style=\"margin-bottom:10px\">").append(exc.getOriginalCitation());
              // remove from remaining this citation since we already handled it
              remaining.remove(exc.getId());
            }
          }

          String ids = String.join("_", excIds.stream().map(Object::toString).collect(Collectors.toList()));
          // build and save popover boxes to be added later on
          popoverbox = new ArrayList<>();
          popoverbox.add(tooltipBox.render(
              "tooltip-" + e.getId(),
              "",
              "highlight",
              Html.apply(originalCitation.toString()),
              "data-excid=\"" + ids + "\"",
              Html.apply(priorTagToOpen + citation + followingTagToClose)));

          popoverbox.add(tooltipBox.render(
              "popover-" + ids,
              "",
              "popoverbox" + (excIds.size() == 1 ? " btn-only" : ""),
              citationEditBox.render(
                  new TextHolder(e.getText(), user.getId(), lang),
                  formFactory.form(CitationSimpleForm.class).fill(new CitationSimpleForm()),
                  excIds, user),
              "",
              Html.apply("")));

          popoverboxes.put(keys, popoverbox);

          // now surround citation with anchor to add popover box in second pass
          // doing it in two passes because we may have overlapping boxes and
          // pattern matcher won't find them if boxes are added on the fly.
          // previous tag that we must close and reopen
          StringBuilder citationBuilder = new StringBuilder();
          citationBuilder.append(priorTagToClose);
          // open anchor for popovers
          citationBuilder.append(keys.get(0));
          // citation as is
          citationBuilder.append(citation);
          // close anchor
          citationBuilder.append(keys.get(1));
          // tag that has been cut and must be reopened
          citationBuilder.append(followingTagToOpen);
          // substitute found argument with updated one
          result = result.substring(0, m.start()) + citationBuilder + result.substring(m.end());

        } else {
          logger.warn("unable to highlight citation " + e.getId() + " \"" + e.getOriginalCitation() + "\"");
        }
      }
    }

    // now add popover-boxes
    for (Map.Entry<List<String>, List<Html>> entry : popoverboxes.entrySet()) {
      Pattern p = Pattern.compile(entry.getKey().get(0) + "(.+?)" + entry.getKey().get(1));
      Matcher m = p.matcher(result);
      if (m.find()) {
        result = result.substring(0, m.start())
            + entry.getValue().get(0).toString()
            + entry.getValue().get(1).toString()
            + result.substring(m.end());
      }
    }

    // put in cache computed result
    cache.set(annotatedKey + id, result, cacheTime);
    return result;
  }*/

  /**
   * Check whether both given strings overlap to each other, ie,
   * <ul>
   *   <li>s1 starts with a suffix of s2</li>
   *   <li>s1 ends with a prefix of s2</li>
   *   <li>s2 starts with a suffix of s1</li>
   *   <li>s2 ends with a prefix of s1</li>
   *   <li>s1 contains s2 or conversely</li>
   * </ul>
   *
   * @param s1 a first string
   * @param s2 a second string
   * @return true if given strings overlap
   */
  private static boolean overlap(String s1, String s2) {
    int minOverlap = 16;
    return startsWithSubstring(s1, s2).length() > minOverlap
        || startsWithSubstring(s2, s1).length() > minOverlap
        || endsWithSubstring(s1, s2).length() > minOverlap
        || endsWithSubstring(s2, s1).length() > minOverlap
        || s1.contains(s2)
        || s2.contains(s1);
  }

  /**
   * Get the common string being a prefix of s1 and suffix of s2
   *
   * @param s1 a string
   * @param s2 a second string
   * @return the prefix of s2 being a suffix of s2, empty string if no such substring
   */
  private static String startsWithSubstring(String s1, String s2) {
    for (int i = 0; i < s2.length(); i++) {
      // s1 is prefixed by s2, reducing size of s2 from the beginning
      if (s1.regionMatches(0, s2, i, s2.length())) {
        return s2.substring(i, s2.length());
      }
    }
    return "";
  }

  /**
   * Get the common string being a suffix of s1 and prefix of s2
   *
   * @param s1 a string
   * @param s2 a second string
   * @return the prefix of s2 being a suffix of s2, empty string if no such substring
   */
  private static String endsWithSubstring(String s1, String s2) {
    for (int i = 1; i < s2.length(); i++) {
      // s1 is suffixed by s2, reducing the size of s2 from the end
      if (s1.regionMatches(0, s2, i, s2.length() - i)) {
        // must substring one more because we started at 1 and matches work with length, not indexes
        return s2.substring(0, s2.length() - i + 1);
      }
    }
    return "";
  }
}
