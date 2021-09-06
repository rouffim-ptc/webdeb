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

import akka.actor.ActorSystem;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Send a mail from WebDeb app.
 *
 * @author Fabian Gilson
 * @author Yvonnick Esnault (initial contribution)
 */
@Singleton
public class Mailer {

  //private logging facility
  private static final org.slf4j.Logger logger = play.Logger.underlying();

  private MailerClient mailerClient;
  private ActorSystem akka;
  private Random random;
  private static final int MAX_DELAY = 5;

  /**
   * Injected constructor.
   *
   * @param mailerClient the mailer client
   * @param akka akka agent in charge of handling mailing requests
   */
  @Inject
  public Mailer(MailerClient mailerClient, ActorSystem akka) {
    this.mailerClient = mailerClient;
    this.akka = akka;
    random = new Random();
  }

  /**
   * Send an email, using Akka Actor. Mail will be delayed between 0 and MAX_DELAY seconds in order to
   * avoid flooding smtp server at once, in case of mass mails.
   *
   * @param mail the mail object to send
   */
  public void sendMail(WebdebMail mail) throws MailerException {
    try {
      akka.scheduler().scheduleOnce(
          Duration.create(random.nextInt(MAX_DELAY), TimeUnit.SECONDS),
          () -> doSend(mail), akka.dispatcher()
      );
    } catch (Exception e) {
      logger.error("unable to send mail", e);
      throw new MailerException(e);
    }
  }


  private void doSend(WebdebMail mail) {
    logger.debug("send mail " + mail.getSubject() + " to " + mail.getRecipients());
    Email email = new Email();
    email.setFrom(mail.getSender());
    email.setSubject(mail.getSubject());
    mail.getRecipients().forEach(email::addTo);
    email.setBodyHtml(mail.getContent());
    mailerClient.send(email);
  }
}
