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

package be.webdeb.infra.mail;

import be.webdeb.core.api.contributor.Contributor;
import be.webdeb.presentation.web.controllers.account.ContactForm;
import be.webdeb.presentation.web.controllers.account.ContactUserToUserForm;
import be.webdeb.presentation.web.controllers.account.admin.AdminMailForm;
import be.webdeb.presentation.web.views.html.mail.*;

import be.webdeb.util.ValuesHelper;
import play.Configuration;
import play.api.Play;
import play.i18n.Lang;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import java.util.*;

/**
 * This class is used to wrap mail contents behind enum keys. It is used to easily construct mails that will be sent
 * by the mailer.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
public class WebdebMail {

  @Inject
  private Configuration configuration = Play.current().injector().instanceOf(Configuration.class);

  @Inject
  private ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  @Inject
  private MessagesApi i18n = Play.current().injector().instanceOf(MessagesApi.class);

	/**
   * Set of enumerated values for all types of mail sent to users. Used to define the content of the mails.
   */
  public enum EMailType {
    /**
     * subscription emails, request confirmation (requires an additional url parameter)
     */
    SUBSCRIBE,
    /**
     * confirm successful subscription
     */
    CONFIRM_SUBSCRIBE,
    /**
     * send token to confirm new email (requires an additional url parameter)
     */
    CHANGE_MAIL,
    /**
     * send token to change password (requires an additional url parameter)
     */
    CHANGE_PASSWORD,
    /**
     * confirm password has been changed
     */
    CONFIRM_CHANGE_PASSWORD,
    /**
     * given mail address for password recovery is unknown
     */
    UNKNOWN_CHANGE_PASSWORD,
    /**
     * invite new contributor to join platform (requires an additional url parameter)
     */
    INVITE,
    /**
     * warn an existing contributor he has been added into a group (requires an additional url parameter)
     */
    INVITE_EXISTING,
    /**
     * warn group onwer a user joined the group (requires an additional url parameters)
     */
    JOINED_GROUP,
    /**
     * warn a user (s)he has been banned from a group
     */
    BAN_FROM_GROUP,
    /**
     * warn a user (s)he has been unbanned from a group
     */
    UNBAN_FROM_GROUP,
    /**
     * warn users a group has been closed
     */
    GROUP_CLOSED,
    /**
     * report to rss contributor of rss texts importation results
     */
    REPORT_RSS,
    /**
     * report to rss contributor of tweet importation results
     */
    REPORT_TWEET,
    /**
     *
     */
    GROUP_MAIL,
    /**
     * the mail it sends by an admin
     */
    ADMIN_MAIL
  }

  private String sender;
  private List<String> recipients = new ArrayList<>();
  private String subject;
  private String content;
  private static final String MAIL_WIDTH = "100%";
  private static final String MAIL_FROM = "mail.from";
  private static final String MAIL_FROM2 = "mail.from2";

  /**
   * Construct a webdeb email object for a given key. Will contain all necessary details for the email
   *
   * @param type the email type to send
   * @param recipient the recipient to this mail
   * @param url an additional url to be happened to mail content in some cases (may be null and ignored for some keys-
   * @param lang the language object for internationalization purpose
   */
  public WebdebMail(EMailType type, String recipient, String url, Lang lang) {
    this(type, Collections.singletonList(recipient), url, new ArrayList<>(), lang);
  }

  /**
   * Construct a webdeb email object for a given key. Will contain all necessary details for the email
   *
   * @param type the email type to send
   * @param recipient the recipient to this mail
   * @param url an additional url to be happened to mail content in some cases (may be null and ignored for some keys-
   * @param params other parameters to pass to message api to build message content
   * @param lang the language object for internationalization purpose
   */
  public WebdebMail(EMailType type, String recipient, String url, List<String> params, Lang lang) {
    this(type, Collections.singletonList(recipient), url, params, lang);
  }

  /**
   * Construct a webdeb email for reporting purposes (used for REPORT_RSS or REPORT_TWEET types)
   *
   * @param type the email type
   * @param recipient the recipient email
   * @param content the content being a map of content and errors to be put as mail body
   * @param lang the language to use to build the content
   */
  public WebdebMail(EMailType type, String recipient, Map<String, String> content, Lang lang) {
    this.sender = i18n.get(lang, MAIL_FROM);
    this.recipients = Collections.singletonList(recipient);
    String mailType = type.name().toLowerCase();
    subject = i18n.get(lang, "mail.subject." + mailType);
    this.content = reportMail.render("mail.title." + mailType, content).body();
  }

  /**
   * Construct a webdeb email from an adminMail form
   *
   * @param adminMailForm the mail form
   * @param recipient the recipient to this mail
   * @param lang the language to use to build the content
   */
  public WebdebMail(AdminMailForm adminMailForm, Contributor recipient, Lang lang) {
    this.sender = i18n.get(lang, MAIL_FROM);
    this.recipients = Collections.singletonList(recipient.getEmail());
    String mailType = EMailType.ADMIN_MAIL.name().toLowerCase();
    this.subject = adminMailForm.getTitle();
    this.content = newsletterMail.render(adminMailForm, recipient, configuration.getString("server.hostname")).body();
  }

    /**
     * Construct a webdeb email for explain content
     *
     * @param type the email type
     * @param title the subject of the email
     * @param content the content string to be put as mail body
     * @param recipients the list of recipients to this mail
     * @param url an additional url to be happened to mail content in some cases (may be null and ignored for some keys-
     * @param lang the language to use to build the content
     */
  public WebdebMail(EMailType type, String title, String content, List<String> recipients, String url, Lang lang) {
    this.sender = i18n.get(lang, MAIL_FROM);
    this.recipients = recipients;
    String mailType = type.name().toLowerCase();
    this.subject = title;
    this.content = defaultMail.render(this.subject, content, "mail.btn." + mailType,
            configuration.getString("server.hostname") + url, MAIL_WIDTH).body();
  }

  /**
   * Construct a webdeb email object for a given key. Will contain all necessary details for the email
   *
   * @param type the email type to send
   * @param recipients the list of recipients to this mail
   * @param url an additional url to be happened to mail content in some cases (may be null and ignored for some keys-
   * @param params other parameters to pass to message api to build message content
   * @param lang the language object for internationalization purpose
   */
  public WebdebMail(EMailType type, List<String> recipients, String url, List<String> params, Lang lang) {
    this.sender = i18n.get(lang, MAIL_FROM);
    this.recipients = recipients;

    String mailType = type.name().toLowerCase();
    String mailContent;
    subject = i18n.get(lang, "mail.subject." + mailType);

    // check if email type requires params to be built or not
    if (EMailType.INVITE.equals(type)
        || EMailType.INVITE_EXISTING.equals(type)
        || EMailType.JOINED_GROUP.equals(type)
        || EMailType.BAN_FROM_GROUP.equals(type)
        || EMailType.UNBAN_FROM_GROUP.equals(type)
        || EMailType.GROUP_CLOSED.equals(type)) {
      mailContent = i18n.get(lang, "mail.content." + mailType, params);
    } else {
      mailContent = i18n.get(lang, "mail.content." + mailType);
    }

    content = defaultMail.render("mail.title." + mailType, mailContent, "mail.btn." + mailType,
        configuration.getString("server.hostname") + url, MAIL_WIDTH).body();
  }

  /**
   * Construct a webdeb email object for a given contact form to send to the admin mail
   *
   * @param contact the contact form to send
   */
  public WebdebMail(ContactForm contact) {
    sender = i18n.get(Lang.defaultLang(), MAIL_FROM);
    recipients.add("info@webdeb.be");
    subject = contact.getSubject();
    content = contactMail.render(contact).body();
  }

  /**
   * Construct a webdeb email object for a given contact form to send to the admin mail
   *
   * @param contact the contact form to send
   */
  public WebdebMail(ContactUserToUserForm contact) {
    boolean isUCLouvainEmailAddress = values.isUCLouvainEmailAddress(contact.getEmailUserSender());
    sender = isUCLouvainEmailAddress ? i18n.get(Lang.defaultLang(), MAIL_FROM2) : contact.getEmailUserSender();
    recipients.add(contact.getEmailUserReceiver());
    subject = i18n.get(Lang.defaultLang(), "title.prod") + " - " + contact.getSubject();
    content = contactUserToUserMail.render(contact, isUCLouvainEmailAddress).body();
  }

  /**
   * Get sender of this mail
   *
   * @return a sender address
   */
  public String getSender() {
    return sender;
  }

  /**
   * Get the list of recipients to this email
   *
   * @return a list of mail adresses
   */
  public List<String> getRecipients() {
    return recipients;
  }

  /**
   * Get the subject of this mail
   *
   * @return a subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Get the content of this mail (may be in html-format)
   *
   * @return the (html) content
   */
  public String getContent() {
    return content;
  }

}
