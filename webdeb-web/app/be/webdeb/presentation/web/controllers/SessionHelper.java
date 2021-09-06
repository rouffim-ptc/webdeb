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

package be.webdeb.presentation.web.controllers;

import be.webdeb.core.api.contribution.ContributionFactory;
import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.core.api.contributor.ContributorFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.contributor.GroupSubscription;
import be.webdeb.core.api.text.TextFactory;
import be.webdeb.presentation.web.controllers.account.LoginForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.util.ValuesHelper;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import play.Configuration;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Http.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A couple of session-related utilities. Using the session to cache (key, value) pairs.
 * Also, manages cookies.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
@Singleton
public class SessionHelper {

  private ContributorFactory factory;
  private ContributionFactory contributionFactory;
  private FormFactory formFactory;
  private Configuration configuration;
  private ValuesHelper valuesHelper;

  private JSONSerializer serializer = new JSONSerializer();

  private static final org.slf4j.Logger logger = play.Logger.underlying();
  private static final String DELIM = ",";

  private int longExcerpt;
  private int smallExcerpt;

  private List<String> bots;

  /**
   * message key for success message formatting
   */
  public static final String SUCCESS = "success";
  /**
   * message key for information message formatting
   */
  public static final String INFO = "info";
  /**
   * message key for warning message formatting
   */
  public static final String WARNING = "warning";
  /**
   * message key for error message formatting
   */
  public static final String ERROR = "danger";
  /**
   * minimum password size
   */
  public static final int MIN_PWD_SIZE = 8;
  /**
   * minimum size for a query key value
   */
  public static final int MIN_QUERY_SIZE = 2;
  /**
   * session key for user's email address or pseudonym
   */
  public static final String KEY_USERMAILORPSEUDO = "mailorpseudo";
  /**
   * session key for tmp user's pseudonym
   */
  public static final String KEY_USERPSEUDO_TMP = "mailorpseudotmp";
  /**
   * session key for user's email address
   */
  public static final String KEY_USERWARNED = "browserwarned";
  /**
   * session key for keep is cookie is already checked
   */
  public static final String KEY_COOKIE_CHECKED = "cookiechecked";
  /**
   * session key for the member visibility of user's current group
   */
  public static final String KEY_GROUP_MEMBER = "mvisibility";
  /**
   * session key for the contribution visibility of user's current group
   */
  public static final String KEY_GROUP_CONTRIB = "cvisibility";
  /**
   * session key for user's current group
   */
  public static final String KEY_USERGROUP = "group";
  /**
   * session key for user's current group id
   */
  public static final String KEY_USERGROUPID = "groupid";
  /**
   * session key for user's displayed name
   */
  public static final String KEY_USERNAME = "username";
  /**
   * session key for "go to" next page
   */
  public static final String KEY_GOTO = "cache.key.goto";
  /**
   * session key for tracing auto-created actors
   */
  public static final String KEY_NEWACTOR = "cache.key.newactor";
  /**
   * session key for tracing auto-created professions
   */
  public static final String KEY_NEWPROFESSION = "cache.key.newprofession";
  /**
   * session key for tracing auto-created debates
   */
  public static final String KEY_NEWDEBATE = "cache.key.newdebate";
  /**
   * session key for adding new justification links with citations
   */
  public static final String KEY_NEW_CIT_JUS_LINK = "cache.key.newcitationjustification";
  /**
   * session key for adding new position links with citations
   */
  public static final String KEY_NEW_CIT_POS_LINK = "cache.key.newcitationposition";
  /**
   * session key for adding new justification links with arguments
   */
  public static final String KEY_NEW_ARG_JUS_LINK = "cache.key.newargumentjustification";
  /**
   * session key for adding new similarity links between arguments
   */
  public static final String KEY_NEW_ARG_SIM_LINK = "cache.key.newargumentsimilarity";
  /**
   * session key for adding new similarity links between debates
   */
  public static final String KEY_NEW_DEBATE_SIM_LINK = "cache.key.newdebatesimilarity";
  /**
   * name of the cookie used to store the "accept cookies" value
   */
  public static final String ACCEPT_COOKIE = "webdeb.accept.cookies";
  /**
   * name of the cookie used to store the "remember user email" value
   */
  public static final String REMEMBER_EMAIL = "webdeb.remember.email";
  /**
   * name of the cookie used to store the "remember user password" value
   */
  public static final String REMEMBER_PASSWORD = "webdeb.remember.password";
  /**
   * general key for error message in forms
   */
  public static final String ERROR_FORM = "error.form";
  /**
   * Default public group id
   */
  public static final int PUBLIC_GROUP = 0;
  /**
   * custom header added at nginx gate with real IP from requester
   */
  public static final String REMOTE_ADDRESS = "x-forwarded-for";

  /**
   * Injected constructor
   *
   * @param factory a contributor factory
   * @param configuration a play configuration
   */
  @Inject
  public SessionHelper(ContributorFactory factory, TextFactory contributionFactory, FormFactory formFactory, Configuration configuration, ValuesHelper helper) {
    this.configuration = configuration;
    this.factory = factory;
    this.contributionFactory = contributionFactory;
    this.formFactory = formFactory;
    this.valuesHelper = helper;
    longExcerpt = configuration.getInt("text.citation.long");
    smallExcerpt = configuration.getInt("text.citation.small");
    bots = configuration.getStringList("known.bots");
  }

  /**
   * Retrieve a validated user from given context
   *
   * @param ctx an http context
   * @return the User (found by his email) if the user exists and is validated, an empty user otherwise
   */
  public WebdebUser getUser(Context ctx) {
    return getUser(ctx, false);
  }

  /**
   * Retrieve a validated user from given context
   *
   * @param ctx an http context
   * @param tmpAccepted true if tmp must be concidered
   * @return the User (found by his email) if the user exists and is validated, an empty user otherwise
   */
  public WebdebUser getUser(Context ctx, boolean tmpAccepted) {
    String browserWarned = ctx.session().get(KEY_USERWARNED);
    String emailOrPseudo = ctx.session().get(KEY_USERMAILORPSEUDO);
    String tmpPseudo = ctx.session().get(KEY_USERPSEUDO_TMP);

    if(tmpPseudo != null) {
      if(tmpAccepted) {
        return new WebdebUser(factory, contributionFactory, tmpPseudo);
      }
      ctx.session().remove(KEY_USERPSEUDO_TMP);
    }

    Contributor contributor;
    if (emailOrPseudo == null && ctx.session().get(KEY_COOKIE_CHECKED) == null) {
      ctx.session().put(KEY_COOKIE_CHECKED, "ok");
      Http.Cookie email_cookie = ctx.request().cookie(REMEMBER_EMAIL);
      Http.Cookie pwd_cookie = ctx.request().cookie(REMEMBER_PASSWORD);

      if(email_cookie != null && pwd_cookie != null) {
        contributor = factory.getContributorByToken(email_cookie.value(), pwd_cookie.value());
        if(contributor != null) {
          ctx.session().put(KEY_USERMAILORPSEUDO, contributor.getEmail());
          ctx.session().put(KEY_USERNAME, contributor.getFirstname());
          emailOrPseudo = contributor.getEmail();
        }else{
          emailOrPseudo = null;
        }
      }
    }

    if (emailOrPseudo != null) {
      String group = ctx.session().get(KEY_USERGROUP);
      GroupSubscription subscription = factory.retrieveGroupSubscription(emailOrPseudo, group);

      if (subscription == null) {
        // get default for user
        contributor = factory.retrieveContributor(emailOrPseudo);
        if (contributor != null) {
          subscription = contributor.getDefaultGroup();
          // put default group into session
          setGroupInSession(ctx, subscription.getGroup());
        }
      }

      if (subscription != null && subscription.getContributor() != null && (subscription.getContributor().isValidated()
      || (subscription.getContributor().getTmpContributor() != null && subscription.getContributor().getTmpContributor().getProject().isInProgress()))) {
        // return correctly logged user
        return new WebdebUser(subscription, factory, contributionFactory, ctx.lang().code(), browserWarned);
      }
    }
    // return default unlogged user
    return new WebdebUser(formFactory.form(LoginForm.class), factory, contributionFactory, browserWarned);
  }

  /**
   * Set the group details in session cookie, ie, group name and visibility ids
   *
   * @param ctx the current user context
   * @param group the selected group
   */
  public void setGroupInSession(Context ctx, Group group) {
    ctx.session().put(KEY_USERGROUP, group.getGroupName());
    ctx.session().put(KEY_USERGROUPID, String.valueOf(group.getGroupId()));
    ctx.session().put(KEY_GROUP_MEMBER, String.valueOf(group.getMemberVisibility().id()));
    ctx.session().put(KEY_GROUP_CONTRIB, String.valueOf(group.getContributionVisibility().id()));
  }

  /**
   * Get the id of current group, ie current scope
   *
   * @param ctx the user context
   * @return the value behind the KEY_USERGROUPID in user's session
   */
  public int getCurrentGroup(Context ctx) {
    String id = ctx.session().get(KEY_USERGROUPID);
    return valuesHelper.isNumeric(id) ? Integer.parseInt(id) : 0;
  }

  /**
   * Get referrer page (play uses referer spelling)
   *
   * @param ctx a context
   * @return the referrer url, or "null" if the referrer was either the login page or not found
   */
  public String getReferer(Context ctx) {
    String referer = ctx.request().getHeader("referer");
    if ("/login".equals(referer)) {
      referer = null;
    }
    return referer;
  }

  /**
   * Set a given value for the given key. If key already exist, it is ignored
   *
   * @param ctx a given context
   * @param key a key (valid keys are defined in dedicated conf file)
   * @param value the value to put at the specified key
   */
  public void set(Context ctx, String key, String value) {
    ctx.session().put(configuration.getString(key), value);
  }

  /**
   * Store an object into the cache (JSON-serialized)
   *
   * @param ctx an http context
   * @param key a key to store the element in the cache ((valid keys are defined in dedicated conf file)
   * @param value the object to serialize into the session
   */
  public void store(Context ctx, String key, Object value) {
    String fullkey = configuration.getString(key);
    if (value != null) {
      String serialized = serializer.deepSerialize(value);
      ctx.session().put(fullkey, serialized);
    } else {
      logger.error("Value for " + fullkey + " is null");
    }
  }

  /**
   * Get a given value for the given key.
   *
   * @param ctx a given context
   * @param key a key to an element in the cache (valid keys are defined in dedicated conf file)
   */
  public String get(Context ctx, String key) {
    return ctx.session().get(configuration.getString(key));
  }

  /**
   * Get a list of values for the given user-relative key
   *
   * @param ctx an http context
   * @param key a key to an element in the cache (valid keys are defined in dedicated conf file)
   * @return a list of values stored under the given key, null if none found
   */
  public List<String> getValues(Context ctx, String key) {
    String value = get(ctx, key);
    return value != null ? new ArrayList<>(Arrays.asList(value.split(DELIM))) : null;
  }

  /**
   * Retrieve a JSON object into the session by a given user-relative key
   *
   * @param ctx an http context
   * @param key a key pointing to the needed object (valid keys are defined in dedicated conf file)
   * @return the object if found, null otherwise
   */
  public <T> T retrieve(Context ctx, String key) {
    String value = ctx.session().get(configuration.getString(key));
    if (value == null) {
      return null;
    }
    return new JSONDeserializer<T>().deserialize(value);
  }

  /**
   * Remove an object from the session
   *
   * @param ctx an http context
   * @param key a key to an element in the cache (valid keys are defined in dedicated conf file)
   */
  public void remove(Context ctx, String key) {
    ctx.session().remove(configuration.getString(key));
  }

  /**
   * Add a value at the specified key which represents a list of strings
   *
   * @param ctx an http context
   * @param key a key to an object (list as string) (valid keys are defined in dedicated conf file)
   * @param value the value to add to this key
   */
  public void addValue(Context ctx, String key, String value) {
    String list = get(ctx, key);
    if (list != null) {
      set(ctx, key, list.concat(DELIM).concat(value));
    } else {
      set(ctx, key, value);
    }
  }

  /**
   * Add a value at the specified key which represents a list of strings
   *
   * @param ctx an http context
   * @param key a key to an object (list as string) (valid keys are defined in dedicated conf file)
   * @param values the values to add to this key
   */
  public void setValues(Context ctx, String key, List<String> values) {
    set(ctx, key, String.join(DELIM, values));
  }

  /**
   * Add a list of values to a given key. If the key does not exist, simply creates it
   *
   * @param ctx an http context
   * @param key a key to an object (list as string) (valid keys are defined in dedicated conf file)
   * @param values the list of values to serialize and add to the given key.
   */
  public void addValues(Context ctx, String key, List<String> values) {
    addValue(ctx, key, String.join(DELIM, values));
  }

  /**
   * Remove the given value from the list of values stored at the given key
   *
   * @param ctx an http context
   * @param key a key to an object (list as string) (valid keys are defined in dedicated conf file)
   * @param value the value to remove from the values at the given key
   */
  public void removeValue(Context ctx, String key, String value) {
    List<String> values = getValues(ctx, key);
    if (value != null && values != null) {
      values.remove(value);
      if (!values.isEmpty()) {
        setValues(ctx, key, values);
      } else {
        remove(ctx, key);
      }
    } else {
      logger.debug("given value is null or given key does not exist");
    }
  }

  /*
   * cookie management
   */

  /**
   * Create a new blank cookie used to display "we use cookies"
   *
   * @return a cookie
   */
  public Http.Cookie createAcceptCookies() {
    return new Http.Cookie(ACCEPT_COOKIE, "false", Integer.MAX_VALUE, "/", null, true, true);
  }

  /**
   * Create cookie to avoid showing "we use cookies" for the next 2 weeks
   *
   * @return a cookie
   */
  public Http.Cookie acceptCookies() {
    return new Http.Cookie(ACCEPT_COOKIE, "true", Integer.MAX_VALUE, "/", null, true, true);
  }

  /**
   * Create a cookie to keep the user lang choice
   *
   * @return a cookie
   */
  public Http.Cookie getUserLangCookie(String key, String value) {
    return new Http.Cookie(key, value, Integer.MAX_VALUE, "/", null, true, true);
  }

  /**
   * Create a cookie to keep the user email
   *
   * @return a cookie
   */
  public Http.Cookie getRememberUserEmailCookie(String value) {
    return new Http.Cookie(REMEMBER_EMAIL, value, Integer.MAX_VALUE, "/", null, true, true);
  }

  /**
   * Create a cookie to keep the user password
   *
   * @return a cookie
   */
  public Http.Cookie getRememberUserPasswordCookie(String value) {
    return new Http.Cookie(REMEMBER_PASSWORD, value, Integer.MAX_VALUE, "/", null, true, true);
  }

  /*
   * other helper stuff
   */

  /**
   * Get the citation size for texts
   *
   * @param large true if we need the size for long excerpts, false otherwise
   * @return the size in char for texts excerpts to be displayed
   */
  public int getTextExcerptSize(boolean large) {
    return large ? longExcerpt : smallExcerpt;
  }

  /**
   * Check whether given request comes from an indexing bot
   *
   * @param request an http request
   * @return true if given request user agent looks like a bot
   */
  public boolean isBot(Http.Request request) {
    return bots != null && bots.stream().anyMatch(request.getHeader("User-Agent").toLowerCase()::contains);
  }

  /**
   * Helper method to only sends back at most the 10 first elements of given list
   *
   * @param list a list of whatever
   * @return a list with at most the 10 first elements
   */
  public <U> List<U> sublist(List<U> list) {
    return list.size() > 10 ? list.subList(0, 10) : list;
  }
}
