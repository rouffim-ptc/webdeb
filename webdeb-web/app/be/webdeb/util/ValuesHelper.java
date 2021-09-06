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

package be.webdeb.util;

import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.FormatException;
import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;

/**
 * Gather a set of utility methods for string, url and date values
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class ValuesHelper {

  private static final String ONGOING_DATE = "-1";

  /**
   * url pattern
   */
  private static final String URL_PATTERN = "^" +
      // protocol identifier
      "(?:(?:https?|ftp)://)" +
      // user:pass authentication
      "(?:\\S+(?::\\S*)?@)?" +
      "(?:" +
      // IP address exclusion
      // private & local networks
      "(?!(?:10|127)(?:\\.\\d{1,3}){3})" +
      "(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})" +
      "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
      // IP address dotted notation octets
      // excludes loopback network 0.0.0.0
      // excludes reserved space >= 224.0.0.0
      // excludes network & broacast addresses
      // (first & last IP address of each class)
      "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +
      "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +
      "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +
      "|" +
      // host name
      "(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)" +
      // domain name
      "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*" +
      // TLD identifier
      "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +
      ")" +
      // port number
      "(?::\\d{2,5})?" +
      // resource path
      "(?:/\\S*)?" +
      "$";

  /**
   * simple email pattern
   */
  private static final String EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\" +
      ".[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

  private static final org.slf4j.Logger logger = play.Logger.underlying();


  /**
   * Check whether the input is blank (null or empty)
   *
   * @param input a string value
   * @return true if the input contains no value
   */
  public boolean isBlank(String input) {
    return input == null || input.trim().isEmpty();
  }

  /**
   * Check whether the input is blank (null or empty)
   *
   * @param input a string value
   * @return true if the input contains no value
   */
  public boolean isFullBlank(String input) {
    return input == null || input.trim().isEmpty() || input.toLowerCase().equals("null");
  }

  /**
   * Check whether the input is blank (-1)
   *
   * @param input an int value
   * @return true if the input contains no value, aka -1
   */
  public boolean isBlank(int input) {
    return input == -1;
  }

  /**
   * Check whether the input is blank (-1)
   *
   * @param input an Integer value
   * @return true if the input contains no value, aka -1
   */
  public boolean isBlank(Integer input) {
    return input == null || input.equals(-1);
  }

  /**
   * Check whether the input is blank (null or -1L)
   *
   * @param input a Long value
   * @return true if the input contains no value, or -1L
   */
  public boolean isBlank(Long input) {
    return input == null || input.equals(-1L);
  }

  /**
   * Check whether a string is a positive integer value (N set)
   *
   * @param value a string
   * @return true if the given string belongs to N+
   */
  public boolean isNumeric(String value) {
    return isNumeric(value, 0, true);
  }

  /**
   * Check whether a string is an integer or a decimal value
   *
   * @param value a string
   * @param decimal max amount of accepted decimal values (valid separators are "." or ",")
   * @param positive true if only positive numeric values are allowed, false otherwise
   * @return true if the given string belongs to Q, according to positive
   */
  public boolean isNumeric(String value, int decimal, boolean positive) {
    // null and empty values
    if (value == null) {
      return false;
    }

    // remove heding/trailing spaces
    char[] data = value.trim().toCharArray();
    if (data.length <= 0) {
      return false;
    }

    // skip '-' sign if any
    int index = 0;
    if (data[0] == '-' && data.length > 1) {
      // looking for positive only => return false
      if (positive) {
        return false;
      }
      index = 1;
    }

    // check all remaining elements if they are digits or valid separator
    boolean separator = false;
    int currentDecimal = 0;
    for (; index < data.length; index++) {
      if (!((data[index] >= '0' && data[index] <= '9') || data[index] == ',' || data[index] == '.')) {
        return false;
      }
      // check if it is the first decimal separator
      if (data[index] == ',' || data[index] == '.') {
        if (separator) {
          return false;
        } else {
          separator = true;
        }
      }
      if (separator) {
        currentDecimal++;
        if (currentDecimal > decimal) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check whether given string representation of a boolean looks valid
   *
   * @param bool a string representation of a boolean
   * @return true if given bool is either "true" or "false" (ignore case)
   */
  public boolean isBoolean(String bool) {
    return "true".equalsIgnoreCase(bool) || "false".equalsIgnoreCase(bool);
  }

  /**
   * Check whether the given email is valid (regex pattern)
   *
   * @param email an email
   * @return true if the given email conforms our regex
   */
  public boolean isEmail(String email) {
    // Simple pattern to check whether an email looks valid
    return email != null && Pattern.matches(EMAIL, email.replaceAll("\\s+",""));
  }

  /**
   * Check if url start with "http://" and add it if necessary
   *
   * @param url an url
   * @return the transformed url
   */
  public String transformURL(String url){
    String changedUrl = (url != null ? (url.contains("://") ? url : "http://" + url) : "");
    return (isURL(changedUrl) ? changedUrl : "");
  }

  /**
   * Check whether the given url is valid (regex pattern)
   *
   * @param url an url
   * @return true if the given url conforms to our regex
   */
  public boolean isURL(String url) {
    return !isBlank(url) && Pattern.matches(URL_PATTERN, url.trim());
  }

  /**
   * Return the domain name of a given url
   *
   * @param url an url
   * @return a string representing the domain
   */
  public String getURLDomain(String url){
    String dn = "";
    try {
      URL aURL = new URL(url);
      dn = aURL.getHost();
    }catch(MalformedURLException e){
    }
    return dn.startsWith("www.") ? dn.substring(4) : dn;
  }

  /**
   * Get the url formatted for search
   *
   * @param url a url to look for
   * @return the formatted url
   */
  public String getFormattedUrl(String url) {
    if(url != null) {
      return url.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "").replaceAll("#", "");
    }
    return null;
  }

  /**
   * Check whether the given date is either of the form YEAR, MONTHYEAR or DAYMONTHYEAR_FORMAT
   *
   * @param date a string representation of a date
   * @return true if the given date complies to at least one of the accepted formats, false otherwise
   */
  public boolean isDate(String date) {
    return getDateFormat(date) != null;
  }

  /**
   * Get date format of given stringified date (among YEAR, MONTHYEAR or DAYMONTHYEAR_FORMAT)
   *
   * @param date a string representation of a date
   * @return the string format among the accepted formats, or null if none applied
   */
  private SimpleDateFormat getDateFormat(String date) {
    // define all formats internally because simpledateformat are not thread safe and this is a singleton
    final SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.FRENCH);
    final int yearFormatSize = 1;

    final SimpleDateFormat monthYear = new SimpleDateFormat("MM/yyyy", Locale.FRENCH);
    final SimpleDateFormat dottedMonthYear = new SimpleDateFormat("MM.yyyy", Locale.FRENCH);
    final int montyearFormatSize = 2;

    final SimpleDateFormat dayMonthYear = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
    final SimpleDateFormat dottedDayMonthYear = new SimpleDateFormat("dd.MM.yyyy", Locale.FRENCH);
    final int daymonthyearFormatSize = 3;

    final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);

    // no date, nothing to do
    if (date == null) {
      return null;
    }

    // check date split char
    String splitChar = "/";
    if (date.contains(".")) {
      splitChar = "\\.";
    }

    // if we have more than one dash, we have a DB date
    if (date.chars().filter(c -> c == '-').count() > 1) {
      splitChar = "-";
    }

    String[] split = date.split(splitChar);
    for (String s : split) {
      if (!isNumeric(s, 0, false)) {
        return null;
      }
    }
    SimpleDateFormat format;
    int yearIdx;

    // switch on amount of splits
    switch (split.length) {
      case yearFormatSize:
        yearIdx = 0;
        format = year;
        break;
      case montyearFormatSize:
        yearIdx = 1;
        format = date.contains("/") ? monthYear : dottedMonthYear;
        break;
      case daymonthyearFormatSize:
        if ("-".equals(splitChar)) {
          yearIdx = 0;
          format = dbFormat;
        } else {
          yearIdx = 2;
          format = date.contains("/") ? dayMonthYear : dottedDayMonthYear;
        }
        break;

      default:
        logger.error("unknown format given " + date);
        return null;
    }

    // ensure year is not longer than four characters
    if (split[yearIdx].length() > 4) {
      return null;
    }

    // force date to be strictly this one
    format.setLenient(true);

    // ensure the format works
    try {
      format.parse(date);
    } catch (ParseException e) {
      logger.warn("identified format " + format.toPattern() + " does not apply to " + date);
      return null;
    }
    return format;
  }

  /**
   * Transform the given date to string
   *
   * @param date a date
   * @return the date to string
   */
  public String dateToString(Date date) {
    return new SimpleDateFormat("dd/MM/yyyy").format(date);
  }

  /**
   * Cast a string representation of a given date into Date. The date must be either in yyyy, mm/yyyy or
   * dd/mm/yyyy formats. Consider ONGOING_DATE values as today.
   *
   * @param date a string representation of a date in any of the accepted formats
   * @return the Date corresponding to the given string representation of a date, null if given date is not
   * parsable
   */
  public Date toDate(String date) {
    if (ONGOING_DATE.equals(date)) {
      // handle special value
      return new Date();
    }

    if (date != null) {
      SimpleDateFormat format = getDateFormat(date);
      if (format != null) {
        try {
          return format.parse(date);
        } catch (ParseException | NumberFormatException e) {
          logger.warn("unparsable date " + date + " with format " + format.toPattern(), e);
        }
      }
    }
    return null;
  }

  /**
   * Get the date of the day at midnight
   *
   * @return the day date
   */
  public Date getDateOfDay() {
    Calendar date = new GregorianCalendar();
    // reset hour, minutes, seconds and millis
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);

    return date.getTime();
  }

  /**
   * Private helper to check and convert a . separated date to a / separated one
   *
   * @param newDate a date to check and convert
   * @return the converted date or null if given date is null
   * @throws FormatException if given date has an invalid format
   */
  public String checkAndHarmonizeDate(String newDate) throws FormatException {
    if (newDate != null && !isDate(newDate.trim())) {
      throw new FormatException(FormatException.Key.DATE_ERROR, "given date is not valid " + newDate);
    }
    // harmonize to "/" separator
    return newDate != null ? newDate.replaceAll("\\.", "/").trim() : null;
  }


  /**
   * Cast an "int like" representation of a date as YYYYMMDD (MM and DD possibly to zero) into a DD/MM/YYYY format
   *
   * @param date a string representation of a date in 8 digit (possibly negative)
   * @return the string representation of given date, or null if given date is not 8-digit long
   */
  public String fromDBFormat(String date) {
    String result = String.valueOf(date);
    boolean isBC = false;

    // handle null or special -1 values for ongoing, let as is
    if (date == null || ONGOING_DATE.equals(date)) {
      return date;
    }

    // we have a negative date, trim leading 0's and just get year
    if (date.startsWith("-") && result.length() == 9) {
      isBC = true;
      result = result.substring(1);
    }

    // check on size (must be 8 digits after leading dash removal)
    if (result.length() == 8) {
      return ("00".equals(result.substring(6)) ? "" : result.substring(6) + "/") +
          ("00".equals(result.substring(4, 6)) ? "" : result.substring(4,6) + "/") +
          // re-put leading dash and trim leading zeros for year
          (isBC ? "-" : "") + result.substring(0, 4).replaceFirst("^0+(?!$)", "");
    }
    return null;
  }

  /**
   * Convert a possibly incomplete date to a string in YYYYMMDD, possibly with leading "-" and zeros.
   *
   * If no suitable format can be found, return null, otherwise transform to expected representation.
   *
   * @param date a date in string format (any of YEAR, MONTHYEAR and DAYMONTHYEAR-like format with either a "/" or a ".")
   * @return the string representing the given date in YYYYMMDD format (may contain 0s as MM and DD)
   */
  public String toDBFormat(String date) {
    final SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.FRENCH);
    final SimpleDateFormat monthYear = new SimpleDateFormat("MM/yyyy", Locale.FRENCH);
    final SimpleDateFormat dayMonthYear = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

    // no date or special value for "ongoing" given, let as is
    if (date == null || ONGOING_DATE.equals(date)) {
      return date;
    }

    String cleanDate = date;
    boolean isBC = false;

    if (cleanDate.contains(".")) {
      cleanDate = cleanDate.replaceAll("\\.", "/");
    }

    // remove dash, if any
    if (cleanDate.contains("-")) {
      isBC = true;
      cleanDate = cleanDate.replace("-", "");
    }

    SimpleDateFormat format = getDateFormat(cleanDate);
    int result = 0;

    if (format != null) {
      try {
        String[] split = cleanDate.split("/");
        if (format.equals(dayMonthYear)) {
          result = Integer.parseInt(split[2]) * 10000 + Integer.parseInt(split[1]) * 100 + Integer.parseInt(split[0]);
        }
        if (format.equals(monthYear)) {
          result = Integer.parseInt(split[1]) * 10000 + Integer.parseInt(split[0]) * 100;
        }
        if (format.equals(year)) {
          result = Integer.parseInt(split[0]) * 10000;
        }
      } catch (NumberFormatException e) {
        logger.debug("unable to parse date " + cleanDate + " with format " + format.toPattern(), e);
        result = 0;
      }
    }

    // put leading "-" if we have a negative date + add leading zeros
    return result != 0 ? (isBC ? "-" : "") + String.format("%08d", result) : null;
  }

  /**
   * Check if two dates are ascending. Dates may be expressed in any accepted format
   *
   * @param date1 a first date, may be null
   * @param date2 a second date, may be null
   * @return true if date1 is before date2 or if any given date is null
   */
  public boolean isBefore(String date1, String date2) {
    return date1 == null || date2 == null || compareDatesAsString(date1, date2) <= 0;
  }

  /**
   * Compare two dates formatted as string under accepted formats by toDate() method.
   *
   * @param date1 a string-formatted date
   * @param date2 another string-formatted date
   * @return a negative number if date1 is prior to date2, a positive number if date1 is later than date2 and 0 if they are equal
   */
  public int compareDatesAsString(String date1, String date2) {
    Date converted2 = toDate(date2);
    if (converted2 == null) {
      return -1;
    }

    Date converted1 = toDate(date1);
    if (converted1 == null) {
      return 1;
    }

    return converted1.compareTo(converted2);
  }

  public String displayDate(Date date){
    if(date != null){
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      return dateFormat.format(date);
    }
    return "";
  }

  public String displayCompleteDate(Date date){
    if(date != null){
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
      return dateFormat.format(date);
    }
    return "";
  }

  public String displayTimeFromNow(Date date) {
    Date now = new Date();
    return displayTimeBetween(date.before(now) ? date : now, date.before(now) ? now : date);
  }

  public String displayTimeBetween(Date date1, Date date2) {
    Calendar c1 = new GregorianCalendar();
    c1.setTime(date1);

    Calendar c2 = new GregorianCalendar();
    c2.setTime(date2);

    long nbOfYearsBetween = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
    if(nbOfYearsBetween >= 1) return nbOfYearsBetween + " an" + (nbOfYearsBetween > 1 ? "s" : "");

    long nbOfDaysBetween = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
    if(nbOfDaysBetween > 1) return nbOfDaysBetween + " jours";

    long nbOfHoursBetween = c2.get(Calendar.DAY_OF_MONTH) - c1.get(Calendar.DAY_OF_MONTH);
    return nbOfHoursBetween > 1 ? nbOfHoursBetween + " heures" : "moins d''une heure";
  }

  public String displayTimeBetween(String date1, String date2) {
    Date d1 = toDate(date1);
    Date d2 = toDate(date2);

    if(d1 != null && d2 != null) {
      return displayTimeBetween(d1, d2);
    }
    else if(d1 != null) {
      return displayTimeFromNow(d1);
    }

    return null;
  }

  /**
   * Remove accents from given string
   *
   * @param s a string
   * @return given string with all accentuated characters replaced
   */
  public String stripAccents(String s) {
    String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
    normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]+", "");
    return normalized;
  }

	/**
   * Cast a string decimal value to a double, if possible
   *
   * @param value a string representation of a decimal value
   * @return the double corresponding to given value, null if not castable
   */
  public Double toDouble(String value) {
    if (!isNumeric(value, Integer.MAX_VALUE, false)) {
      return null;
    }

    if (isNumeric(value)) {
      return (double) Integer.parseInt(value);
    }

    // if we have an ",", replace with "." and return double value
    return Double.parseDouble(value.replace(",", "."));

  }

  /**
   * Sort a map by values instead of keys (for alphabetical order)
   *
   * @param unsortedMap a map to sort
   * @param <K> the key type
   * @param <V> the value type
   * @return unsortedMap alphabetically sorted on values
   */
  public <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> unsortedMap) {
    Map<K, V> sortedMap = new TreeMap<>(new ValueComparator<>(unsortedMap));
    sortedMap.putAll(unsortedMap);
    return sortedMap;
  }

  /**
   * Get a locale object from a 2-char iso code
   *
   * @param lang a 2-char ISO code
   * @return the corresponding locale
   */
  @SuppressWarnings("fallthrough")
  public Locale getLocale(String lang) {
    switch (lang) {
      case "nl":
        return new Locale("nl");
      case "fr":
        return Locale.FRENCH;
      case "en":
      default:
        return Locale.ENGLISH;
    }
  }

  /**
   * Value comparator to sort a map by values
   *
   * @param <K> the key type
   * @param <V> the value type
   * @author Guillaume Bersac
   */
  private class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {
    private Map<K, V> map;
    private Collator compareOperator = Collator.getInstance(Locale.FRENCH);

    ValueComparator(Map<K, V> map) {
      this.map = map;
      compareOperator.setStrength(Collator.PRIMARY);
    }

    @Override
    public int compare(K keyA, K keyB) {
      Comparable<V> valueA = map.get(keyA);
      V valueB = map.get(keyB);
      return compareOperator.compare(valueA, valueB);
    }
  }

  /**
   * Get the substitute (ie equivalent) profession, if any. Used when grouping or filtering on functions
   *
   * @return a (possibly null) profession that may be used as a substitute for this profession
   */
  public Map<String, Map<String, String>> fillProfessionName(String lang, String gender, String name){
    if(!isBlank(lang) && !isBlank(gender) && !isBlank(name)) {
      Map<String, Map<String, String>> langMap = new HashMap<>();
      Map<String, String> genderMap = new HashMap<>();
      genderMap.put(gender, name);
      langMap.put(lang, genderMap);
      return langMap;
    }
    return null;
  }

  /**
   * Check if a String is a correct hexadecimal color code
   *
   * @return true if the code is correct
   */
  public boolean checkHexadecimalColorCode(String code){
    if(!isBlank(code)) {
      code = code.replace("#", "");

      if(code.length() == 6) {
        try {
          Integer.parseInt(code, 16);
          return true;
        } catch (NumberFormatException e) {
          logger.error("Wrong hexadecimal code : ", code);
        }
      }
    }
    return false;
  }

  /**
   * Remove all spaces from a String
   *
   * @param str the string to treat
   * @return the string without spaces
   */
  public String removeSpaces(String str){
    return (str != null ? str.replaceAll("\\s+", "") : null);
  }

  /**
   * Detect the language of the given text
   *
   * @param text the text to analyse
   * @param defaultLang the default lang if not found
   * @return the language of the given text
   */
  public String detectTextLanguage(String text, String defaultLang){
    /*try {
      //load all languages:
      List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

      //build language detector:
      LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
              .withProfiles(languageProfiles)
              .build();

      //create a text object factory
      TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

      //query:
      TextObject textObject = textObjectFactory.forText(text);
      Optional<LdLocale> lang = languageDetector.detect(textObject);

      if(lang.isPresent()) {
        return lang.get().getLanguage();
      }

    } catch (IOException e) {
      logger.debug("Language detection error : " + e.getMessage());
    }*/

    return defaultLang;
  }

  /**
   * Check if the given name is not valid for a Tag
   *
   * @param name the name to check
   * @return true if the given name is not correct for a Tag
   */
  public boolean isNotValidTagName(String name){
    return name.contains("/");
  }

  /**
   * Check if the given name as a good number of words
   *
   * @param name the name to check
   * @return true if the given name has 5 words or less
   */
  public boolean checkTagNameSize(String name){
    return name.split(" ").length > 5;
  }

  /**
   * Convert the given argument title to correct title
   *
   * @param title the title to check
   * @return true if the given name is not correct for a Tag
   */
  public String correctArgumentTitle(String title){
    return title.trim().replace("?", "").replace(".", "");
  }

  /**
   * Get the index of pattern in s or -1, if not found
   *
   * @param pattern the regex pattern to match
   * @param s the string to analyse
   * @return index of pattern in s or -1, if not found
   */
  public int indexOfPattern(Pattern pattern, String s) {
    Matcher matcher = pattern.matcher(s);
    return matcher.find() ? matcher.start() : -1;
  }

  /**
   * Get a string that describe the given contribution (ex : the fullname for an actor)
   *
   * @param contribution the contribution to describe
   * @param lang a 2-char iso code that represents a language
   * @return the description
   */
  public String getContributionDescription(Contribution contribution, String lang){
    EContributionType type = contribution.getType();
    switch (type){
      case ACTOR:
        return ((Actor)contribution).getFullname(lang);
      default:
        return "";
    }
  }

  /**
   * Check that the original citation is the same that the technical citation without the brackets context
   *
   * @param originalExcerpt the original citation
   * @param techWorkingExcerpt the technical citation
   * @return the description
   */
  public boolean correspondingExcerptAndTechExcerpt(String originalExcerpt, String techWorkingExcerpt){
    return techWorkingExcerpt.trim().replaceAll(" \\[.*?\\]", "").equals(originalExcerpt.trim());
  }

  public boolean checkIfUrlIsWikipedia(String url){
    return getURLDomain(url).endsWith("wikipedia.org");
  }

  public boolean isUCLouvainEmailAddress(String email){
    return isEmail(email) && email.endsWith("@uclouvain.be");
  }

  /**
   * Transform given text to readable html content
   *
   * @param text the given text
   * @return the formatted text to be readable
   */
  public String transformTextToHtml(String text){
    logger.debug(text);
    String [] sentences = text.split("[.]\\s+");
    StringBuilder ch = new StringBuilder();

    for(int i = 0; i < sentences.length; i++){
      ch.append(sentences[i]).append(". ");
      if(i % 3 == 0){
        ch.append("<br><br>");
      }
    }

    return ch.toString();
  }

  /**
   * Get a random hexadecimal color code
   *
   * @return the hexadecilmal color code
   */
  public String getRandomHexaColor(){
    int random_r = (int)(Math.random() * 255 + 0);
    int random_g = (int)(Math.random() * 255 + 0);
    int random_b = (int)(Math.random() * 255 + 0);

    return Integer.toHexString(random_r) + Integer.toHexString(random_g) + Integer.toHexString(random_b);
  }

  public int getRandomInt(int min, int max){
    Random r = new Random();
    return r.nextInt((max - min) + 1) + min;
  }

    /**
     * Convert the given string to UT8
     *
     * @param toConvert the string to convert into UT8
     * @return the converted string
     */
    public String convertToUTF8(String toConvert){
        toConvert = toConvert.trim().replaceAll("’", "'").replaceAll("\\p{javaSpaceChar}{2,}"," ");
        return toConvert.replaceAll("[^\\w0-9&\\[\\]\\s.;:=+-/°!?§'|@(){}<>_#²³/\"*%\\p{Sc}\\p{L}`~¨]","");
    }

    public String firstLetterUpper(String ch){
      return ch != null && ch.length() > 1 ? ch.substring(0, 1).toUpperCase() + ch.substring(1) : ch;
    }

  public String firstLetterLower(String ch){
    return ch != null && ch.length() > 1 ? ch.substring(0, 1).toLowerCase() + ch.substring(1) : ch;
  }

}
