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

import be.objectify.deadbolt.java.actions.Restrict;
import be.webdeb.core.api.actor.Actor;
import be.webdeb.core.api.contribution.EContributionType;

import be.webdeb.core.api.contributor.Group;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.tag.Tag;
import be.webdeb.infra.fs.FileSystem;
import be.webdeb.infra.ws.ml.ImageDetection;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleHolder;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.tag.TagHolder;
import be.webdeb.presentation.web.controllers.permission.WebdebRole;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.VizActions;
import be.webdeb.presentation.web.controllers.viz.actor.ActorVizHolder;
import be.webdeb.presentation.web.controllers.viz.actor.EActorVizPane;
import be.webdeb.presentation.web.views.html.*;
import be.webdeb.presentation.web.views.html.export.exportIndex;
import be.webdeb.presentation.web.views.html.others.election.*;
import be.webdeb.presentation.web.views.html.others.election.async.*;
import be.webdeb.presentation.web.views.html.others.test.*;
import be.webdeb.presentation.web.views.html.util.helpModal;
import be.webdeb.presentation.web.views.html.others.*;
import be.webdeb.presentation.web.views.html.others.covid.*;

import be.webdeb.presentation.web.views.html.util.imageModal;
import be.webdeb.presentation.web.views.html.util.message;
import com.fasterxml.jackson.databind.JsonNode;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import jsmessages.japi.Helper;
import play.Configuration;
import play.libs.Json;
import play.libs.Scala;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.JavaScriptReverseRouter;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Public assets management and main actions for
 * <ul>
 *   <li>index page</li>
 *   <li>about, about us, terms, etc</li>
 *   <i>images retrieval (avatars) and offensive checks</i>
 *   <li>javascript reverse router and javascript i18 facility</li>
 * </ul>
 *
 * @author Fabian Gilson
 * @author Cyril Carlier
 * @author Martin Rouffiange
 */
public class Application extends CommonController {

  @Inject
  private FileSystem files;

  @Inject
  private ImageDetection detector;

  @Inject
  private VizActions vizActions;

  private Configuration configuration;

  // javascript localized messages plugin
  private JsMessages jsMessages;

  /**
   * Injected constructor
   *
   * @param jsMessagesFactory a javascript message textFactory (for i18n messages from javascript functions)
   * @param configuration play configuration module
   */
  @Inject
  public Application(JsMessagesFactory jsMessagesFactory, Configuration configuration) {
    this.configuration = configuration;
    jsMessages = jsMessagesFactory.all();
  }

  /**
   * Binding from javascript to localized messages (to use them in javascript code)
   *
   * @return the localized message, as retrieved by the Messages function
   */
  public Result jsMessages() {
    return ok(jsMessages.apply(Scala.Option("window.Messages"), Helper.messagesFromCurrentHttpContext()));
  }


  /**
   * Display the main index page
   *
   * @return the index page
   */
  public CompletionStage<Result> index() {
    return vizActions.index(-1);
  }


  /**
   * Display the tour page
   *
   * @return the tour page
   */
  public Result tour() {
    if (sessionHelper.getUser(ctx()) == null) {
      session().clear();
    }
    // add cookie to response if user never clicked on "got it" message
    if (request().cookie(SessionHelper.ACCEPT_COOKIE) == null ||
            !"true".equals(request().cookie(SessionHelper.ACCEPT_COOKIE).value())) {
      response().setCookie(sessionHelper.createAcceptCookies());
    }

    WebdebUser user = sessionHelper.getUser(ctx());
    Map<EContributionType, Long> amount = new HashMap<>();

    amount.put(EContributionType.ACTOR, helper.getAmountOf(EContributionType.ACTOR, Group.getGroupPublic()));
    amount.put(EContributionType.DEBATE, helper.getAmountOf(EContributionType.DEBATE, Group.getGroupPublic()));
    amount.put(EContributionType.TEXT, helper.getAmountOf(EContributionType.TEXT, Group.getGroupPublic()));
    amount.put(EContributionType.TAG, helper.getAmountOf(EContributionType.TAG, Group.getGroupPublic()));

    return ok(index.render(sessionHelper.getUser(ctx()), amount, null));
  }

  /**
   * Display the main teach page
   *
   * @return the teach page
   */
  public CompletionStage<Result> teach() {
    return sendOk(teach.render(sessionHelper.getUser(ctx())));

  }

  /**
   * Display the modal with details about a collaborator
   *
   * @param id the id of the collaborator
   * @return the teach page
   */
  public CompletionStage<Result> collaboratorDetails(String id) {
    return sendOk(collaboratorModal.render(id));

  }

  /**
   * Display the Debagora page
   *
   * @return the debagora page
   */
  public CompletionStage<Result> debagora() {
    return sendOk(debagora.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Update 'Accept cookies' notification state (called from ajax)
   *
   * @return an empty result with a cookie to avoid displaying 'accept cookie policy' for a while
   */
  public Result acceptCookies() {
    return ok("").withCookies(sessionHelper.acceptCookies());
  }

  /**
   * Return the terms and conditions page
   *
   * @return the page where the terms and conditions are explained
   */
  public Result terms() {
    return ok(termsAndConditions.render(sessionHelper.getUser(ctx()), null));
  }

  /**
   * Change language in context
   *
   * @param language a language ISO code (2-char unique representation)
   * @return the referrer page, or the index ("/") page if none was passed by the browser
   */
  public Result language(String language) {
    // set current requested language
    if (language != null && !language.isEmpty()) {
      logger.debug("set language to " + language);
      ctx().changeLang(language);
      // reset cookie to keep the lang choice more than the session duration
      response().setCookie(sessionHelper.getUserLangCookie(ctx().messages().messagesApi().langCookieName(), language));
    }
    logger.debug("redirecting to " + sessionHelper.getReferer(ctx())
        + " ctx headers " + ctx()._requestHeader().toString());
    return redirect(sessionHelper.getReferer(ctx()));
  }

  /**
   * Render the about project page
   *
   * @return the about project page
   */
  public Result about() {
    return ok(about.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the help page
   *
   * @return the help page
   */
  public CompletionStage<Result> help() {
    return CompletableFuture.supplyAsync(() ->
        ok(help.render(getHelpList(), sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Get the partner page
   *
   * @return the partner page
   */
  public CompletionStage<Result> partners() {
    return CompletableFuture.supplyAsync(() ->
        ok(partners.render(sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Get the page to export some contents from the DB
   *
   * @return the page to do an DB content exportation
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> exportation() {
    logger.debug("Get exporation page");

    return sendOk(exportIndex.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the description of all interesting classes of the model to querying the persistence part.
   *
   * @return a list of ModelDescription as json object
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> getModelDescription() {
    logger.debug("Get model description for API");

    return sendOk(Json.toJson(contributorFactory.getModelDescription()));
  }

  /**
   * Execute the given query to transform into a sql query to perform it into the DB et get the results as a list of list of values.
   * The first list is the keys of sql columns name.
   *
   * @return the result list as json object, possibly empty
   */
  @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.NOTVIEWER))
  public CompletionStage<Result> executeApiQuery() {
    logger.debug("POST api query");

    Map<String, String[]> query = request().body().asFormUrlEncoded();

    return CompletableFuture.supplyAsync(() ->
            ok(Json.toJson(contributorFactory.executeApiQuery(query))), context.current());
  }

  /**
   * Display other page about COVID-19
   *
   * @return the other page
   */
  public CompletionStage<Result> otherCovid() {
    List<TagHolder> tags = new ArrayList<>();
    List<DebateHolder> debates = new ArrayList<>();

    WebdebUser user = sessionHelper.getUser(ctx());
    String lang = ctx().lang().code();

    Arrays.asList(179205L, 181279L, 181277L, 181271L, 181269L, 181261L, 181263L, 181267L, 181265L, 181273L, 179313L, 181259L, 181275L).forEach(tagId -> {
      Tag tag = tagFactory.retrieve(tagId);

      if(tag != null){
        tags.add(helper.toTagHolder(tag, user, lang, true));
      }
    });

    Arrays.asList(181002L, 180109L, 180975L, 180223L, 180187L, 180504L).forEach(tagId -> {
      Debate debate = debateFactory.retrieve(tagId);

      if(debate!= null){
        debates.add(helper.toDebateHolder(debate, user, lang, true));
      }
    });

    return sendOk(covidIndex.render(user, tags, debates));
  }

  /**
   * Get the page to test
   *
   * @return the page to test
   */
  public CompletionStage<Result> testdragndrop() {
    logger.debug("Get test dragndrop page");

    return sendOk(dragndrop.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the page to test
   *
   * @return the page to test
   */
  public CompletionStage<Result> testdragndrop2() {
    logger.debug("Get test dragndrop page");

    return sendOk(dragndrop2.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the page to test form
   *
   * @return the page to test form
   */
  public CompletionStage<Result> testformdebate() {
    logger.debug("Get test form page");

    return sendOk(form_debate.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the page to test form
   *
   * @return the page to test form
   */
  public CompletionStage<Result> testformcitation() {
    logger.debug("Get test form page");

    return sendOk(form_citation.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the page to test form
   *
   * @return the page to test form
   */
  public CompletionStage<Result> testformcitationshort() {
    logger.debug("Get test form page");

    return sendOk(form_citation_short.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Get the page to test interface
   *
   * @return the page to test interface
   */
  public CompletionStage<Result> testinterface1(Long id, Integer pane, Integer pov) {
    logger.debug("Get test interface page for actor " + id + " pane " + pane + " pov " + pov);

    WebdebUser user = sessionHelper.getUser(ctx());

    Actor actor = sessionHelper.isBot(request()) ? actorFactory.retrieve(id) : actorFactory.retrieveWithHit(id);

    if (actor == null) {
        return sendNotFoundContribution(id, EContributionType.ACTOR);
    }

    if(!user.mayView(actor)){
      return sendUnauthorizedContribution();
    }

    return sendOk(interface1.render(
              new ActorVizHolder( actor, user, ctx().lang().code()),
              EActorVizPane.value(pane),
              pov,
              values,
              user
            ));
  }

  /**
   * Get the page to test interface
   *
   * @return the page to test interface
   */
  public CompletionStage<Result> testinterface2(Long id, Integer pane, Integer pov) {
    logger.debug("Get test interface page for actor " + id + " pane " + pane + " pov " + pov);

    WebdebUser user = sessionHelper.getUser(ctx());

    Actor actor = sessionHelper.isBot(request()) ? actorFactory.retrieve(id) : actorFactory.retrieveWithHit(id);

    if (actor == null) {
      return sendNotFoundContribution(id, EContributionType.ACTOR);
    }

    if(!user.mayView(actor)){
      return sendUnauthorizedContribution();
    }

    return sendOk(interface2.render(
            new ActorVizHolder(actor, user, ctx().lang().code()),
            EActorVizPane.value(pane),
            pov,
            values,
            user
    ));
  }

    /**
     * Get the page to test interface
     *
     * @return the page to test interface
     */
    public CompletionStage<Result> testinterface4(Long id, Integer pane, Integer pov) {
        logger.debug("Get test interface page for actor " + id + " pane " + pane + " pov " + pov);

        WebdebUser user = sessionHelper.getUser(ctx());

        Actor actor = sessionHelper.isBot(request()) ? actorFactory.retrieve(id) : actorFactory.retrieveWithHit(id);

        if (actor == null) {
            return sendNotFoundContribution(id, EContributionType.ACTOR);
        }

        if(!user.mayView(actor)){
            return sendUnauthorizedContribution();
        }

        return sendOk(interface4.render(
                new ActorVizHolder( actor, user, ctx().lang().code()),
                EActorVizPane.value(pane),
                pov,
                values,
                user
        ));
    }

  /**
   * Get the page to test interface
   *
   * @return the page to test interface
   */
  public CompletionStage<Result> testinterface3(Long id, Integer pane, Integer pov) {
    logger.debug("Get test interface page for actor " + id + " pane " + pane + " pov " + pov);

    WebdebUser user = sessionHelper.getUser(ctx());

    Actor actor = sessionHelper.isBot(request()) ? actorFactory.retrieve(id) : actorFactory.retrieveWithHit(id);

    if (actor == null) {
      return sendNotFoundContribution(id, EContributionType.ACTOR);
    }

    if(!user.mayView(actor)){
      return sendUnauthorizedContribution();
    }

    return sendOk(interface3.render(
            new ActorVizHolder( actor, user, ctx().lang().code()),
            EActorVizPane.value(pane),
            pov,
            values,
            user
    ));
  }

  /**
   * Get the page to test pdf
   *
   * @return the page to test pdf
   */
  public CompletionStage<Result> testpdf() {
    logger.debug("Get test pdf page");

    return sendOk(pdftest.render(sessionHelper.getUser(ctx())));
  }

  /**
   * Display other page about mobilité
   *
   * @return the other page
   */
  public CompletionStage<Result> otherMobilite() {
    return CompletableFuture.supplyAsync(() ->
            ok(mobilite.render(sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Display other page about logement
   *
   * @return the other page
   */
  public CompletionStage<Result> otherLogement() {
    return CompletableFuture.supplyAsync(() ->
            ok(logement.render(sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Display other page about logement
   *
   * @return the other page
   */
  public CompletionStage<Result> otherEnvironnement() {
    return CompletableFuture.supplyAsync(() ->
            ok(environnement.render(sessionHelper.getUser(ctx()))), context.current());
  }
  /**
   * Display other page about logement
   *
   * @return the other page
   */
  public CompletionStage<Result> otherEconomie() {
    return CompletableFuture.supplyAsync(() ->
            ok(economie.render(sessionHelper.getUser(ctx()))), context.current());
  }
  /**
   * Display other page about logement
   *
   * @return the other page
   */
  public CompletionStage<Result> otherAmenagement() {
    return CompletableFuture.supplyAsync(() ->
            ok(amenagement.render(sessionHelper.getUser(ctx()))), context.current());
  }
  /**
   * Display other page about logement
   *
   * @return the other page
   */
  public CompletionStage<Result> otherDemocratie() {
    return CompletableFuture.supplyAsync(() ->
            ok(democratie.render(sessionHelper.getUser(ctx()))), context.current());
  }
  
  private Map<Long, String> getPartiesColorsMap(){
    Map<Long, String> colorsMap = new LinkedHashMap<>();

    colorsMap.put(133344L, "#5aad39");
    colorsMap.put(163651L, "#005daa");
    colorsMap.put(163553L, "#ff5500");
    colorsMap.put(163647L, "#0287cc");
    colorsMap.put(163710L, "#773179");
    colorsMap.put(163650L, "#f3b61d");
    colorsMap.put(163702L, "#1b1c20");
    colorsMap.put(163620L, "#f9a01b");
    colorsMap.put(163631L, "#dd007a");
    colorsMap.put(163675L, "#ef4034");
    colorsMap.put(165016L, "#143faf");
    colorsMap.put(163638L, "#008479");
    colorsMap.put(163672L, "#fe0000");
    colorsMap.put(163680L, "#ee413d");
    colorsMap.put(169792L, "#439591");
    colorsMap.put(169797L, "#e28887");
    colorsMap.put(169791L, "#632976");
    colorsMap.put(169795L, "#656565");
    colorsMap.put(169798L, "#010101");
    colorsMap.put(169796L, "#bbb");
    colorsMap. put(172919L, "#682b29");
    colorsMap.put(169794L, "#be3868");
    colorsMap.put(169799L, "#656565");
    colorsMap.put(169793L, "#bbb");
    
    return colorsMap;
  }

  private void updatePolicalPartiesMaps(Map<ActorSimpleHolder, List<ActorSimpleHolder>> policalParties){

    String lang = ctx().lang().code();

    updatePolicalPartiesMap(policalParties, 133344L, lang, new Long[]{20114L,2383L,166270L,25087L,26902L,22136L,26319L,166271L,20105L,27966L,26114L,166272L,25923L,24459L,26653L,166273L,166274L,165468L,26914L,24659L,26949L,166275L,166276L,166277L,164403L,166278L,166279L,25121L,166280L,166281L,166282L,166283L,26096L,166284L,25356L,166285L,166286L,24880L,166287L,23825L,166288L,166289L,166290L,166291L,166292L,166293L,166294L,24799L,26644L,173798L,166296L,166297L,26550L,166298L,164334L,24147L,166299L,24089L,166300L,24154L,166301L,166302L,123292L,166303L,26247L,23862L,165460L,24310L,164593L,24670L,4120L,20068L});
    updatePolicalPartiesMap(policalParties, 163651L, lang, new Long[]{6407L,5506L,21899L,5403L,23049L,164734L,24277L,165212L,166999L,167000L,25238L,167001L,167002L,167003L,164811L,167004L,1214L});
    updatePolicalPartiesMap(policalParties, 163553L, lang, new Long[]{6384L,22698L,25263L,28302L,169543L,24774L,20406L,26149L,20107L,27937L,25658L,169245L,169226L,20817L,164725L,28762L,169185L,28347L,165512L,26773L,169544L,169545L,169546L,169547L,169548L,24634L,169549L,171905L,27881L,169551L,169552L,169553L,25331L,169554L,27773L,25920L,27878L,169555L,169556L,169557L,25942L,28225L,169558L,165490L,24494L,169559L,169560L,25723L,169561L,169562L,165502L,24361L,26413L,169563L,28386L,169564L,169565L,169566L,23833L,169567L,169568L,27852L,171775L,169570L,169571L,27831L,170938L,169573L,28058L,169574L,123919L,169575L});
    updatePolicalPartiesMap(policalParties, 163647L, lang, new Long[]{3715L,5505L,23838L,19810L,5517L,28327L,5408L,24172L,25628L,23964L,25633L,28109L,25391L,26444L,169277L,23868L,25116L,165611L,169309L,25240L,169518L,169519L,25724L,24354L,25347L,25466L,24336L,24079L,26416L,169520L,169521L,26154L,24647L,24419L,27871L,169522L,25425L,165177L,169523L,169524L,169525L,24588L,169526L,169527L,169228L,25214L,169287L,27725L,25174L,165217L,169528L,164688L,169529L,169530L,169531L,169532L,169243L,169533L,170684L,169535L,25569L,169536L,169537L,169538L,169539L,169540L,169541L,24611L,28164L,169542L,20076L,5513L});
    updatePolicalPartiesMap(policalParties, 163710L, lang, new Long[]{169576L,169577L,164786L,169578L,169579L,169580L,169581L,169582L,169583L,169584L,169585L,163835L,169586L,169587L,169588L,169589L,169590L,169591L,2207L,169592L,164153L,169593L,169594L,169595L,169596L,169597L,169598L,169599L,169600L,164069L,163750L,169601L,169602L,169603L,163766L,169604L,169605L,169606L,164660L});
    updatePolicalPartiesMap(policalParties, 163650L, lang, new Long[]{20541L,167675L,167676L,25142L,167677L,167678L,167679L,25085L,28375L,167680L,167681L,167682L,167683L,167684L,27804L,167685L,21668L});
    updatePolicalPartiesMap(policalParties, 163702L, lang, new Long[]{20657L,27880L,28291L,169607L,169608L,24744L,169609L,169610L,164596L,169611L,164310L,169612L,169613L,24709L,169614L,165233L,26084L});
    updatePolicalPartiesMap(policalParties, 163620L, lang, new Long[]{6369L,5508L,21706L,167730L,10078L,23895L,167731L,25267L,167732L,28154L,114151L,167735L,25775L,24457L,167736L,28333L,6372L});
    updatePolicalPartiesMap(policalParties, 163631L, lang, new Long[]{5084L,19877L,6383L,22172L,5410L,5731L,22375L,27848L,26751L,19824L,167686L,24332L,167687L,25338L,25531L,165535L,24316L,26669L,167688L,167689L,167690L,167691L,167692L,167693L,25128L,167694L,167695L,25038L,24480L,167696L,167697L,164658L,167698L,167699L,26869L,167700L,167701L,28124L,167702L,165548L,167703L,27999L,167704L,24944L,167705L,23946L,167706L,167707L,167708L,23836L,167709L,167710L,167711L,25297L,167712L,165550L,167713L,167714L,167715L,167716L,167717L,167718L,167719L,26959L,167720L,167721L,24546L,167723L,167724L,23967L,26460L,20537L});
    updatePolicalPartiesMap(policalParties, 163675L, lang, new Long[]{28055L,23150L,167767L,167768L,116082L,24244L,167769L,26240L,2419L,167770L,164343L,167771L,26003L,28210L,23968L,26536L,165582L,24801L,26104L,167772L,27746L,163755L,167773L,25161L,164017L,164022L,167774L,165225L,167775L,24174L,164341L,167776L,25476L,167777L,167778L,167779L,167780L,167781L,164571L,167782L,167783L,167784L,167785L,167786L,167787L,24462L,28370L,167788L,167789L,167790L,167791L,27815L,25352L,167792L,26486L,167793L,167794L,167795L,28281L,167796L,25831L,167797L,165351L,24645L,167798L,167799L,167800L,167801L,167802L,24524L,26668L,28452L});
    updatePolicalPartiesMap(policalParties, 165016L, lang, new Long[]{169623L,25183L,15342L,169624L,169625L,25715L,26098L,169626L,26095L,165604L,169627L,169628L,169629L,169630L,25998L,169631L,116232L,169632L,169633L,169634L,169635L,169636L,169637L,169638L,169639L,169640L,169641L,169642L,164347L,169644L,169645L,169646L,169647L,169648L,169649L,169680L,169650L,169651L,169652L,169653L,169654L,169655L,169656L,169657L,169658L,169659L,169660L,169661L,24848L,169662L,169663L,169664L,169665L,169666L,169667L,169668L,169669L,169670L,169671L,169672L,169673L,25398L,169674L,169675L,24256L,25250L,169676L,169677L,169678L,169679L,26358L,24015L});
    updatePolicalPartiesMap(policalParties, 163638L, lang, new Long[]{20715L,20310L,167838L,165328L,167839L,164226L,167840L,167841L,167842L,167843L,165238L,167844L,167845L,167846L,167847L,24616L,5502L});
    updatePolicalPartiesMap(policalParties, 163672L, lang, new Long[]{6429L,6398L,2176L,2705L,17823L,5399L,166960L,21351L,21437L,21838L,22459L,112867L,20059L,24071L,22437L,164229L,5188L,21898L,26580L,24636L,22923L,166961L,26214L,24706L,21194L,166963L,26952L,24418L,166964L,28017L,28279L,25363L,166965L,26166L,166966L,166967L,27768L,166968L,27844L,166969L,25931L,24866L,26846L,166970L,166971L,24222L,166972L,166973L,166974L,166975L,26165L,24600L,166976L,166977L,166978L,164211L,166979L,166980L,166981L,166982L,166983L,27769L,25741L,166984L,26671L,166985L,166986L,24988L,166987L,24298L,5512L,5400L});
    updatePolicalPartiesMap(policalParties, 163680L, lang, new Long[]{25981L,167827L,167828L,167829L,167830L,24673L,167831L,167832L,164699L,164143L,26747L,24109L,167833L,167834L,167835L,167836L,25948L});
    updatePolicalPartiesMap(policalParties, 169792L, lang, new Long[]{169683L,169684L,169685L,169686L,169687L,169688L,169689L,169690L,169691L,169692L,169693L,169694L,169695L,740L,169696L,169697L,169698L});
    updatePolicalPartiesMap(policalParties, 169797L, lang, new Long[]{6421L,169699L,5383L,169700L,165319L,169183L,169701L,169702L,164532L,169703L,169704L,169705L,169706L,169707L,164647L,169708L,20717L});
    updatePolicalPartiesMap(policalParties, 169791L, lang, new Long[]{169709L,169710L,25899L,169711L,169712L,169713L,169714L,169715L,169716L,169717L,169718L,169719L,165567L,169720L,169721L,165069L,169722L,169723L,169724L,169725L,169726L,169727L,169728L,169729L,169730L,169731L,169732L,169733L,169734L,169735L,169736L,169737L,169738L,169739L,169740L,169741L,169742L});
    updatePolicalPartiesMap(policalParties, 169795L, lang, new Long[]{169743L,169744L,169745L,169746L,28089L,169747L,169748L,169749L,169750L,169751L,169752L,169753L,169754L,169755L,169756L});
    updatePolicalPartiesMap(policalParties, 169798L, lang, new Long[]{163753L,25840L,169757L,169758L,24412L,169759L,169760L,169761L,169762L,164626L,169763L,169764L,169765L,169766L,169767L,169768L});
    updatePolicalPartiesMap(policalParties, 169796L, lang, new Long[]{169769L});
    updatePolicalPartiesMap(policalParties, 172919L, lang, new Long[]{169770L,169771L,169772L,169773L,169774L,169775L,169776L,169777L});
    updatePolicalPartiesMap(policalParties, 169794L, lang, new Long[]{164742L,169778L,169779L,169780L,169781L,169782L,169783L,169784L,169785L,169786L,26567L,169787L});
    updatePolicalPartiesMap(policalParties, 169799L, lang, new Long[]{169788L, 169789L});
    updatePolicalPartiesMap(policalParties, 169793L, lang, new Long[]{169790L});
  }

  /**
   * Display other page about election 2019 on brussels
   *
   * @return the other page
   */
  public CompletionStage<Result> otherElection() {

    Map<ActorSimpleHolder, List<ActorSimpleHolder>> policalParties = new LinkedHashMap<>();
    Map<Long, String> policalPartiesColors = new HashMap<>();
    //updatePolicalPartiesMaps(policalParties);

    return CompletableFuture.supplyAsync(() ->
            ok(election2019.render(policalParties, policalPartiesColors, sessionHelper.getUser(ctx()))), context.current());
  }

  /**
   * Display other page about elections 2019 statistics
   *
   * @return the other page
   */
  public CompletionStage<Result> otherElectionStats() {
    return otherElectionStatsPage(0);
  }

  /**
   * Display other page about elections 2019 statistics
   *
   * @param id the id of the election stats page
   * @return the other page
   */
  public CompletionStage<Result> otherElectionStatsPage(int id) {

    switch(id){
      case 1 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats1.render(sessionHelper.getUser(ctx()))), context.current());
      case 2 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats2.render(sessionHelper.getUser(ctx()))), context.current());

      case 3 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats3.render(sessionHelper.getUser(ctx()))), context.current());

      case 4 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats4.render(sessionHelper.getUser(ctx()))), context.current());

      case 5 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats5.render(sessionHelper.getUser(ctx()))), context.current());

      case 6 :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats6.render(sessionHelper.getUser(ctx()))), context.current());

      default :
        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats.render(sessionHelper.getUser(ctx()))), context.current());
    }
  }

  /**
   * Get data async for elections 2019 statistics
   *
   * @param id the id of the election stats page
   * @return the other page
   */
  public CompletionStage<Result> otherElectionStatsPageAsync(int id) {
    String lang = ctx().lang().code();
    Map<ActorSimpleHolder, List<ActorSimpleHolder>> policalParties = new LinkedHashMap<>();
    Map<Long, String> policalPartiesColors = getPartiesColorsMap();
    List<Long> relatedIds,  relatedIds2;
    Map<Long, List<String>> othersDatum, othersDatum2;

    switch(id){
      case 1 :
        relatedIds = Arrays.asList(133344L,163553L,163612L,163613L,163614L,163615L,163616L,163617L,163618L,163619L,163620L,163621L,163622L,163623L,163624L,163625L,163626L,163627L,163628L,163629L,163630L,163631L,163632L,163633L,163634L,163635L,163636L,163637L,163638L,163639L,163640L,163641L,163642L,163643L,163644L,163645L,163646L,163647L,163648L,163649L,163650L,163651L,163652L,163653L,163655L,163656L,163657L,163658L,163659L,163660L,163661L,163662L,163663L,163664L,163665L,163666L,163667L,163668L,163669L,163670L,163671L,163672L,163673L,163674L,163675L,163676L,163677L,163678L,163679L,163680L,163681L,163682L,163683L,163684L,163685L,163686L,163687L,163688L,163689L,163690L,163691L,163692L,163693L,163694L,163695L,163696L,163697L,163698L,163699L,163700L,163701L,163702L,163703L,163704L,163705L,163706L,163707L,163708L,163709L,163710L,163711L,165016L,169791L,169792L,169793L,169794L,169795L,169796L,169797L,169798L,169799L,172919L);
        updatePolicalPartiesMaps(policalParties);

        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats1Async.render(policalParties, policalPartiesColors, relatedIds)), context.current());
      case 2 :
        List<ActorSimpleHolder> candidates = getActorsFromIds(new Long[]{2180L,5507L,20243L,5384L,5402L,633L,5398L,5413L,5516L,21175L,2761L,5387L,5407L,5408L,5411L,5504L,5515L,17803L,24505L,25325L,5511L,6389L,5102L,5386L,5401L,5503L,16426L,17831L,20820L,22322L,20544L,20255L,2756L,5388L,5510L,16424L,20106L,20210L,21177L,21625L,40L,5385L,5412L,5509L,23172L}, lang);
        Collections.sort(candidates);

        Map<Long, Long> candidatesPartyMap = new LinkedHashMap<>();
        candidatesPartyMap.put(2180L, 133344L);
        candidatesPartyMap.put(5507L, 133344L);
        candidatesPartyMap.put(20243L, 133344L);
        candidatesPartyMap.put(5384L, 163651L);
        candidatesPartyMap.put(5402L, 163651L);
        candidatesPartyMap.put(633L, 163553L);
        candidatesPartyMap.put(5398L, 163553L);
        candidatesPartyMap.put(5413L, 163553L);
        candidatesPartyMap.put(5516L, 163553L);
        candidatesPartyMap.put(21175L, 163553L);
        candidatesPartyMap.put(2761L, 163647L);
        candidatesPartyMap.put(5387L, 163647L);
        candidatesPartyMap.put(5407L, 163647L);
        candidatesPartyMap.put(5408L, 163647L);
        candidatesPartyMap.put(5411L, 163647L);
        candidatesPartyMap.put(5504L, 163647L);
        candidatesPartyMap.put(5515L, 163647L);
        candidatesPartyMap.put(17803L, 163647L);
        candidatesPartyMap.put(24505L, 163647L);
        candidatesPartyMap.put(25325L, 163647L);
        candidatesPartyMap.put(5511L, 163560L);
        candidatesPartyMap.put(6389L, 163631L);
        candidatesPartyMap.put(5102L, 163631L);
        candidatesPartyMap.put(5386L, 163631L);
        candidatesPartyMap.put(5401L, 163631L);
        candidatesPartyMap.put(5503L, 163631L);
        candidatesPartyMap.put(16426L, 163631L);
        candidatesPartyMap.put(17831L, 163631L);
        candidatesPartyMap.put(20820L, 163631L);
        candidatesPartyMap.put(22322L, 163675L);
        candidatesPartyMap.put(20544L, 163675L);
        candidatesPartyMap.put(20255L, 163638L);
        candidatesPartyMap.put(2756L, 163672L);
        candidatesPartyMap.put(5388L, 163672L);
        candidatesPartyMap.put(5510L, 163672L);
        candidatesPartyMap.put(16424L, 163672L);
        candidatesPartyMap.put(20106L, 163672L);
        candidatesPartyMap.put(20210L, 163672L);
        candidatesPartyMap.put(21177L, 169797L);
        candidatesPartyMap.put(21625L, 169797L);

        relatedIds = Arrays.asList(123996L, 2181L);

        return CompletableFuture.supplyAsync(() ->
                ok(election2019stats2Async.render(candidates, candidatesPartyMap, policalPartiesColors, relatedIds)), context.current());

        case 3 :
          updatePolicalPartiesMap(policalParties, 163620L, lang, new Long[]{5508L,6369L,6372L});
          updatePolicalPartiesMap(policalParties, 163553L, lang, new Long[]{6384L,20107L,20406L,20817L,22698L,164725L});
          updatePolicalPartiesMap(policalParties, 163631L, lang, new Long[]{5084L,5410L,5731L,6383L,19877L,22172L,22375L});
          updatePolicalPartiesMap(policalParties, 133344L, lang, new Long[]{2383L,20068L,20105L,20114L,22136L,26319L});
          updatePolicalPartiesMap(policalParties, 163638L, lang, new Long[]{5502L,20310L,20715L});
          updatePolicalPartiesMap(policalParties, 163647L, lang, new Long[]{3715L,5505L,5513L,5517L,19810L,20076L,25633L});
          updatePolicalPartiesMap(policalParties, 163650L, lang, new Long[]{20541L,21668L});
          updatePolicalPartiesMap(policalParties, 169797L, lang, new Long[]{5383L,6421L,20717L});
          updatePolicalPartiesMap(policalParties, 163651L, lang, new Long[]{1214L,5403L,5506L,6407L,21899L,23049L});
          updatePolicalPartiesMap(policalParties, 163672L, lang, new Long[]{2176L, 5188L, 5399L, 5400L, 5512L, 6398L, 6429L, 17823L, 20059L, 21194L, 21351L, 21437L, 21838L, 21898L, 22437L, 22459L, 22923L, 164229L});
          updatePolicalPartiesMap(policalParties, 163675L, lang, new Long[]{23150L, 28055L});
          updatePolicalPartiesMap(policalParties, 163702L, lang, new Long[]{20657L});

          relatedIds = Arrays.asList(123996L, 2181L);

          return CompletableFuture.supplyAsync(() ->
                  ok(election2019stats3Async.render(policalParties, policalPartiesColors, relatedIds)), context.current());

        case 4 :

          updatePolicalPartiesMap(policalParties, 163672L, lang, new Long[]{5400L,5399L,5512L,22437L,24298L,25363L,25741L,26580L,28017L,28279L,166963L,166966L});
          updatePolicalPartiesMap(policalParties, 163553L, lang, new Long[]{22698L, 25263L, 26149L, 28302L, 28347L, 169548L});
          updatePolicalPartiesMap(policalParties, 163647L, lang, new Long[]{5505L, 5513L, 3715L, 25116L, 25466L});
          updatePolicalPartiesMap(policalParties, 163631L, lang, new Long[]{5084L, 26751L, 167694L});
          updatePolicalPartiesMap(policalParties, 163638L, lang, new Long[]{24616L});
          updatePolicalPartiesMap(policalParties, 169797L, lang, new Long[]{20717L});
          updatePolicalPartiesMap(policalParties, 163651L, lang, new Long[]{21899L});
          updatePolicalPartiesMap(policalParties, 163620L, lang, new Long[]{25775L});

          relatedIds = Arrays.asList(2494L,5100L,5058L,5091L,5090L,5096L,5059L,5057L,5095L,5099L,5089L,5097L,5093L,3716L,41L);

          return CompletableFuture.supplyAsync(() ->
                  ok(election2019stats4Async.render(policalParties, policalPartiesColors, relatedIds)), context.current());

        case 5 :


            updatePolicalPartiesMap(policalParties, 163620L, lang, new Long[]{6369L,21706L,25775L,24457L,28333L});
            updatePolicalPartiesMap(policalParties, 163553L, lang, new Long[]{6384L,22698L,25263L,28302L,24774L,20406L,26149L,20107L,27937L,25658L,169245L,169226L,28762L,169185L,28347L,26773L,169544L,169548L,24634L,27881L,27773L});
            updatePolicalPartiesMap(policalParties, 163631L, lang, new Long[]{5084L,19877L,6383L,22172L,5410L,5731L,22375L,27848L,26751L,167686L,24332L,25338L,24316L,167690L,167692L,167693L,25128L,167694L,25038L,167697L,167699L,27999L,24944L,167705L,23946L,167708L,167717L,167719L,167724L,23967L,26460L,20537L});
            updatePolicalPartiesMap(policalParties, 133344L, lang, new Long[]{2383L,25087L,26902L,22136L,20105L,25923L,166274L,165468L,26914L,166275L,166277L,164403L,166278L,25121L,166280L,166284L,23825L,166290L,24799L,166297L,166298L,24147L,166300L,24154L,165460L,24670L,20068L});
            updatePolicalPartiesMap(policalParties, 163638L, lang, new Long[]{20715L,20310L,165328L,167840L,167842L,24616L,5502L});
            updatePolicalPartiesMap(policalParties, 165016L, lang, new Long[]{25183L,25715L,26358L});
            updatePolicalPartiesMap(policalParties, 163647L, lang, new Long[]{3715L,5505L,23838L,19810L,5517L,28327L,24172L,25628L,23964L,25391L,26444L,169277L,23868L,25116L,165611L,169309L,25240L,25724L,24354L,25466L,24079L,26154L,24419L,169228L,169287L,25174L,165217L,169243L,24611L,28164L,20076L,5513L});
            updatePolicalPartiesMap(policalParties, 163650L, lang, new Long[]{167675L,167676L,25142L,167678L});
            updatePolicalPartiesMap(policalParties, 169797L, lang, new Long[]{5383L,165319L,169183L,20717L});
            updatePolicalPartiesMap(policalParties, 163651L, lang, new Long[]{6407L,5506L,21899L,5403L,23049L,164734L,24277L,165212L,166999L,167002L,1214L});
            updatePolicalPartiesMap(policalParties, 163672L, lang, new Long[]{6429L,2176L,17823L,5399L,166960L,21351L,21437L,22459L,112867L,24071L,22437L,5188L,26580L,24636L,22923L,26214L,21194L,166963L,24418L,166964L,28017L,28279L,25363L,166965L,26166L,166966L,27844L,25931L,26846L,166971L,24222L,166974L,26165L,24600L,164211L,166981L,25741L,26671L,166986L,24298L,5512L,5400L});
            updatePolicalPartiesMap(policalParties, 163675L, lang, new Long[]{167768L,26240L,2419L,164343L,165582L,164017L,167788L,28452L});

            relatedIds = Arrays.asList(2494L,5097L,5057L,5100L,5060L,5093L,5099L,5096L,5089L,41L,5091L,3716L,5095L,5090L,5059L,5098L,5094L,5058L,5092L);

            return CompletableFuture.supplyAsync(() ->
                  ok(election2019stats5Async.render(policalParties, policalPartiesColors, relatedIds)), context.current());

        case 6 :

          updatePolicalPartiesMap(policalParties, 163672L, lang, new Long[]{6429L,6398L,2176L,2705L,17823L,5399L,166960L,21351L,21437L,21838L,22459L,22437L,5188L,22923L,21194L,25931L,5400L});
          updatePolicalPartiesMap(policalParties, 133344L, lang, new Long[]{20114L,2383L,166270L,25087L,26902L,22136L,26319L,166271L,20105L,27966L,26114L,166272L,25923L,24310L,20068L});
          updatePolicalPartiesMap(policalParties, 163647L, lang, new Long[]{3715L,5505L,23838L,19810L,5517L,28327L,5408L,24172L,25628L,23964L,25633L,20076L,5513L});
          updatePolicalPartiesMap(policalParties, 163631L, lang, new Long[]{5084L,19877L,6383L,22172L,5410L,5731L,22375L,167693L,167707L,20537L});
          updatePolicalPartiesMap(policalParties, 163675L, lang, new Long[]{28055L,23150L,167767L,167768L,116082L,24244L,167769L,2419L,164017L,25352L});
          updatePolicalPartiesMap(policalParties, 163553L, lang, new Long[]{6384L,22698L,25263L,28302L,20406L,169245L});
          updatePolicalPartiesMap(policalParties, 163638L, lang, new Long[]{20715L,20310L,167838L,167840L});
          updatePolicalPartiesMap(policalParties, 163650L, lang, new Long[]{20541L,167675L,167676L});
          updatePolicalPartiesMap(policalParties, 169797L, lang, new Long[]{6421L,169699L,5383L});
          updatePolicalPartiesMap(policalParties, 163651L, lang, new Long[]{6407L,5506L,1214L});
          updatePolicalPartiesMap(policalParties, 169792L, lang, new Long[]{169683L});
          updatePolicalPartiesMap(policalParties, 163620L, lang, new Long[]{6369L});
          updatePolicalPartiesMap(policalParties, 169795L, lang, new Long[]{169743L});
          updatePolicalPartiesMap(policalParties, 163680L, lang, new Long[]{25981L});
          updatePolicalPartiesMap(policalParties, 163702L, lang, new Long[]{20657L});

          relatedIds = Arrays.asList(2494L,5097L,5057L,5100L,5060L,5093L,5099L,5096L,5089L,41L,5091L,3716L,5095L,5090L,5059L,5098L,5094L,5058L,5092L);
          relatedIds2 = Arrays.asList(123996L, 2181L);

          othersDatum = new HashMap<>();
          othersDatum.put(6429L, Arrays.asList("M", "61", "Evere", "conseiller", "Ministre", "BXL", "Bourg.", "oui", "droit", "1", "16.889", "1"));
          othersDatum.put(6398L, Arrays.asList("F", "52", "Anderlecht", "", "Secrétaire d'état", "BXL et FWB", "", "oui", "droit", "2", "10.385", "3"));
          othersDatum.put(2176L, Arrays.asList("M", "51", "Etterbeek", "conseiller", "", "BXL et FWB", "Ech.", "oui", "communication", "3", "10.833", "2"));
          othersDatum.put(2705L, Arrays.asList("F", "47", "Forest", "", "", "", "", "oui", "anthropologie & sociologie", "4", "3.186", "9"));
          othersDatum.put(17823L, Arrays.asList("M", "57", "Forest", "conseiller", "Parlementaire", "", "Bourg. & échev.", "?", "", "5", "3.199", "10"));
          othersDatum.put(5399L, Arrays.asList("M", "42", "Evere", "échevin", "Parlementaire", "", "Ech.", "?", "", "6", "5.438", "6"));
          othersDatum.put(166960L, Arrays.asList("F", "24", "Molenbeek-Saint-Jean", "conseiller", "", "", "", "?", "", "7", "2.855", "11"));
          othersDatum.put(21351L, Arrays.asList("F", "53", "Anderlecht", "conseiller", "Parlementaire", "", "", "oui", "économie", "8", "3.051", "12"));
          othersDatum.put(21437L, Arrays.asList("M", "49", "Molenbeek-Saint-Jean", "conseiller", "Parlementaire", "", "Ech.", "?", "", "9", "5.217", "7"));
          othersDatum.put(21838L, Arrays.asList("M", "28", "Woluwé-Saint-Lambert", "", "Parlementaire", "", "", "oui", "droit", "10", "2.766", "13"));
          othersDatum.put(22459L, Arrays.asList("F", "54", "Forest", "conseiller", "Parlementaire", "", "Ech.", "?", "", "11", "4.155", "14"));
          othersDatum.put(22437L, Arrays.asList("M", "52", "Bruxelles", "échevin", "Parlementaire", "", "Ech.", "?", "", "15", "4.414", "15"));
          othersDatum.put(5188L, Arrays.asList("M", "45", "Schaerbeek", "conseiller", "Parlementaire", "", "", "?", "", "17", "4.385", "16"));
          othersDatum.put(22923L, Arrays.asList("M", "49", "Bruxelles", "conseiller", "Parlementaire", "", "", "?", "", "21", "4.963", "8"));
          othersDatum.put(21194L, Arrays.asList("M", "38", "Schaerbeek", "conseiller", "Parlementaire", "", "", "oui", "", "25", "6.368", "4"));
          othersDatum.put(25931L, Arrays.asList("M", "48", "Schaerbeek", "conseiller", "", "", "", "?", "", "41", "3.477", "17"));
          othersDatum.put(5400L, Arrays.asList("M", "48", "Bruxelles", "bourgmestre", "", "", "Bourg. & échev.", "?", "", "72", "6.230", "5"));
          othersDatum.put(20114L, Arrays.asList("M", "47", "Ixelles", "", "Parlementaire", "", "", "oui", "politique", "1", "11.183", "1"));
          othersDatum.put(2383L, Arrays.asList("F", "38", "Schaerbeek", "conseiller", "Parlementaire", "", "", "oui", "droit", "2", "7.992", "2"));
          othersDatum.put(166270L, Arrays.asList("M", "37", "Ganshoren", "conseiller", "", "", "", "oui", "politique", "3", "2.684", "4"));
          othersDatum.put(25087L, Arrays.asList("F", "29", "Molenbeek-Saint-Jean", "conseiller", "", "", "", "oui", "communication", "4", "4.297", "5"));
          othersDatum.put(26902L, Arrays.asList("M", "35", "Watermael-Boitsfort", "conseiller", "", "", "Ech.", "oui", "droit", "5", "2.338", "6"));
          othersDatum.put(22136L, Arrays.asList("F", "43", "Forest", "conseiller", "Parlementaire", "", "", "oui", "droit", "6", "3.944", "7"));
          othersDatum.put(26319L, Arrays.asList("M", "44", "Saint-Gilles", "", "Parlementaire", "", "", "oui", "scénographie", "7", "2.035", "8"));
          othersDatum.put(166271L, Arrays.asList("F", "49", "Molenbeek-Saint-Jean", "", "", "", "", "oui", "histoire", "8", "3.606", "9"));
          othersDatum.put(20105L, Arrays.asList("M", "52", "Saint-Josse-Ten-Noode", "conseiller", "", "", "", "?", "", "9", "2.922", "10"));
          othersDatum.put(27966L, Arrays.asList("F", "45", "Saint-Gilles", "", "", "", "", "oui", "philosophie", "10", "3.690", "11"));
          othersDatum.put(26114L, Arrays.asList("M", "40", "Schaerbeek", "", "", "", "", "oui", "droit & éthique", "11", "1.765", "12"));
          othersDatum.put(166272L, Arrays.asList("F", "27", "Etterbeek", "", "", "", "", "?", "", "12", "3.841", "13"));
          othersDatum.put(25923L, Arrays.asList("M", "29", "Evere", "conseiller", "", "", "", "?", "", "13", "2.473", "14"));
          othersDatum.put(24310L, Arrays.asList("F", "39", "Evere", "", "", "", "", "oui", "droit & sociologie", "68", "2.993", "15"));
          othersDatum.put(20068L, Arrays.asList("F", "45", "Saint-Josse-Ten-Noode", "conseiller", "Parlementaire", "", "", "oui", "économie", "72", "5.157", "3"));
          othersDatum.put(3715L, Arrays.asList("F", "59", "Molenbeek-Saint-Jean", "échevin", "", "", "Bourg. & échev.", "oui", "droit", "1", "16.856", "1"));
          othersDatum.put(5505L, Arrays.asList("M", "61", "Etterbeek", "bourgmestre", "Parlementaire", "", "Bourg. & échev.", "oui", "droit & criminologie", "2", "8.576", "2"));
          othersDatum.put(23838L, Arrays.asList("F", "40", "Woluwé-Saint-Pierre", "conseiller", "", "", "", "oui", "droit", "3", "6.098", "3"));
          othersDatum.put(19810L, Arrays.asList("M", "46", "Anderlecht", "conseiller", "Parlementaire", "", "Bourg. & échev.", "oui", "histoire", "4", "3.880", "5"));
          othersDatum.put(5517L, Arrays.asList("F", "58", "Ixelles", "conseiller", "Parlementaire", "", "Bourg. & échev.", "?", "", "5", "3.144", "6"));
          othersDatum.put(28327L, Arrays.asList("M", "42", "Bruxelles", "conseiller", "", "", "Ech.", "?", "", "6", "3.260", "7"));
          othersDatum.put(5408L, Arrays.asList("F", "52", "Woluwé-Saint-Pierre", "conseiller", "", "", "Ech.", "oui", "politique", "7", "4.926", "4"));
          othersDatum.put(24172L, Arrays.asList("F", "45", "Bruxelles", "conseiller", "", "", "Ech.", "oui", "commun., marketing & politique", "8", "2.121", "8"));
          othersDatum.put(25628L, Arrays.asList("M", "35", "Watermael-Boitsfort", "conseiller", "", "", "", "?", "", "9", "1.988", "9"));
          othersDatum.put(23964L, Arrays.asList("F", "36", "Uccle", "conseiller", "", "", "", "?", "", "10", "1.647", "11"));
          othersDatum.put(25633L, Arrays.asList("M", "38", "Bruxelles", "", "Parlementaire", "", "Ech.", "oui", "économie d'entrepreprise\n", "11", "2.509", "13"));
          othersDatum.put(20076L, Arrays.asList("F", "64", "Ixelles", "conseiller", "Parlementaire", "", "Ech.", "?", "", "71", "2.652", "12"));
          othersDatum.put(5513L, Arrays.asList("M", "47", "Uccle", "bourgmestre", "", "", "Bourg.", "?", "", "72", "3.541", "10"));
          othersDatum.put(5084L, Arrays.asList("M", "58", "Schaerbeek", "bourgmestre", "Parlementaire", "Fédéral", "Bourg.", "oui", "économie", "1", "14.672", "1"));
          othersDatum.put(19877L, Arrays.asList("F", "51", "Uccle", "conseiller", "Parlementaire", "", "Ech.", "?", "", "2", "4.778", "2"));
          othersDatum.put(6383L, Arrays.asList("F", "55", "Schaerbeek", "conseiller", "Secrétaire d'état", "BXL", "Ech.", "oui", "économie & géographie", "3", "4.662", "3"));
          othersDatum.put(22172L, Arrays.asList("M", "42", "Forest", "conseiller", "Parlementaire", "", "Ech.", "oui", "droit", "4", "2.058", "4"));
          othersDatum.put(5410L, Arrays.asList("M", "43", "Uccle", "conseiller", "Parlementaire", "", "", "oui", "droit & politique", "5", "2.248", "5"));
          othersDatum.put(5731L, Arrays.asList("F", "62", "Bruxelles", "conseiller", "", "", "", "oui", "sociologie", "6", "2.768", "6"));
          othersDatum.put(22375L, Arrays.asList("M", "33", "Molenbeek-Saint-Jean", "conseiller", "Parlementaire", "", "", "?", "", "7", "1.687", "7"));
          othersDatum.put(167693L, Arrays.asList("M", "35", "Woluwé-Saint-Pierre", "conseiller", "", "", "", "oui", "", "24", "1.941", "10"));
          othersDatum.put(167707L, Arrays.asList("F", "24", "Uccle", "", "", "", "", "non", "", "48", "2.746", "8"));
          othersDatum.put(20537L, Arrays.asList("M", "48", "Auderghem", "conseiller", "", "", "Bourg.", "oui", "politique", "72", "2.363", "9"));
          othersDatum.put(28055L, Arrays.asList("F", "44", "Saint-Gilles", "", "Parlementaire", "", "", "oui", "physique", "1", "12.658", "2"));
          othersDatum.put(23150L, Arrays.asList("M", "43", "", "", "Parlementaire", "", "", "?", "", "2", "13.079", "1"));
          othersDatum.put(167767L, Arrays.asList("M", "53", "Forest", "", "", "", "", "?", "", "3", "2.998", "4"));
          othersDatum.put(167768L, Arrays.asList("F", "42", "Forest", "conseiller", "", "", "", "oui", "arts du spectacle", "4", "3.086", "5"));
          othersDatum.put(116082L, Arrays.asList("M", "55", "Schaerbeek", "", "", "", "", "?", "", "5", "2.496", "6"));
          othersDatum.put(24244L, Arrays.asList("F", "34", "Schaerbeek", "", "", "", "", "?", "", "6", "2.779", "7"));
          othersDatum.put(167769L, Arrays.asList("M", "37", "", "", "", "", "", "?", "", "7", "2.158", "8"));
          othersDatum.put(2419L, Arrays.asList("F", "26", "Ixelles", "conseiller", "", "", "", "oui", "études de genre", "9", "2.635", "10"));
          othersDatum.put(164017L, Arrays.asList("F", "32", "Schaerbeek", "conseiller", "", "", "", "oui", "droit", "25", "6.726", "3"));
          othersDatum.put(25352L, Arrays.asList("M", "63", "Ixelles", "", "", "", "", "?", "", "53", "2.892", "9"));
          othersDatum.put(6384L, Arrays.asList("F", "46", "Uccle", "conseiller", "Ministre", "BXL", "Ech.", "oui", "droit", "1", "7.707", "1"));
          othersDatum.put(22698L, Arrays.asList("M", "72", "Ganshoren", "bourgmestre", "Parlementaire", "", "Bourg. & échev.", "?", "", "2", "4.661", "2"));
          othersDatum.put(25263L, Arrays.asList("F", "46", "Koekelberg", "échevin", "", "", "Ech.", "?", "", "3", "1.503", "3"));
          othersDatum.put(28302L, Arrays.asList("M", "31", "Woluwé-Saint-Pierre", "échevin", "", "", "Ech.", "oui", "gestion", "4", "2.905", "5"));
          othersDatum.put(20406L, Arrays.asList("M", "62", "Bruxelles", "conseiller", "Parlementaire", "", "Ech.", "oui", "économie", "7", "2.929", "4"));
          othersDatum.put(169245L, Arrays.asList("F", "25", "Berchem-Sainte-Agathe", "conseiller", "", "", "", "oui", "relations internationales", "12", "2.088", "6"));
          othersDatum.put(20715L, Arrays.asList("F", "39", "Ganshoren", "conseiller", "", "", "", "oui", "communication", "1", "4.320", "1"));
          othersDatum.put(20310L, Arrays.asList("M", "40", "Schaerbeek", "conseiller", "Parlementaire", "", "", "oui", "psychologie", "2", "1.391", "2"));
          othersDatum.put(167838L, Arrays.asList("M", "27", "Anderlecht", "", "", "", "", "oui", "droit", "3", "862", "3"));
          othersDatum.put(167840L, Arrays.asList("F", "42", "Bruxelles", "conseiller", "", "", "", "oui", "arts du spectacle", "7", "1.696", "4"));
          othersDatum.put(20541L, Arrays.asList("F", "40", "Bruxelles", "", "Parlementaire", "", "", "oui", "droit, coopération et rel. Int.", "1", "3.256", "1"));
          othersDatum.put(167675L, Arrays.asList("M", "32", "Bruxelles", "conseiller", "", "", "", "oui", "droit & politique", "2", "1.178", "2"));
          othersDatum.put(167676L, Arrays.asList("M", "29", "Anderlecht", "conseiller", "", "", "", "oui", "droit", "3", "833", "3"));
          othersDatum.put(6421L, Arrays.asList("M", "52", "Bruxelles", "", "Ministre", "BXL et Flamand", "", "?", "", "1", "4.562", "1"));
          othersDatum.put(169699L, Arrays.asList("F", "45", "", "", "", "", "", "?", "", "2", "1.142", "3"));
          othersDatum.put(5383L, Arrays.asList("M", "46", "Jette", "conseiller", "Parlementaire", "", "", "?", "", "3", "2.451", "2"));
          othersDatum.put(6407L, Arrays.asList("M", "61", "Evere", "conseiller", "Ministre", "BXL et Flamand", "", "?", "", "1", "4.551", "1"));
          othersDatum.put(5506L, Arrays.asList("F", "53", "Woluwé-Saint-Pierre", "conseiller", "Parlementaire", "", "Ech.", "?", "", "2", "1.682", "2"));
          othersDatum.put(1214L, Arrays.asList("M", "52", "Jette", "conseiller", "", "Flamand", "", "oui", "droit", "17", "2.103", "3"));
          othersDatum.put(169683L, Arrays.asList("M", "31", "", "", "", "", "", "oui", "sociologie & études urbaines", "1", "573", "1"));
          othersDatum.put(6369L, Arrays.asList("F", "46", "Bruxelles", "conseiller", "Secrétaire d'état", "BXL", "", "?", "", "1", "2.151", "1"));
          othersDatum.put(169743L, Arrays.asList("F", "27", "", "", "", "", "", "oui", "droit", "1", "1.385", "1"));
          othersDatum.put(25981L, Arrays.asList("M", "40", "Schaerbeek", "", "", "", "", "oui", "arts du spectacle", "1", "741", "1"));
          othersDatum.put(20657L, Arrays.asList("M", "54", "Jette", "", "Parlementaire", "", "", "oui", "droit", "1", "1.553", "1"));

          othersDatum2 = new HashMap<>();
          othersDatum2.put(163672L, Arrays.asList("17", "70,6%", "82,4%", "21,4%", "47", "70,6%", "3", "8"));
          othersDatum2.put(133344L, Arrays.asList("15", "33,3%", "53,3%", "0,0%", "40", "46,7%", "0", "1"));
          othersDatum2.put(163647L, Arrays.asList("13", "38,5%", "92,3%", "30,0%", "48", "46,2%", "0", "10"));
          othersDatum2.put(163631L, Arrays.asList("10", "60,0%", "90,0%", "11,1%", "45", "60,0%", "2", "5"));
          othersDatum2.put(163675L, Arrays.asList("10", "20,0%", "30,0%", "0,0%", "43", "50,0%", "0", "0"));
          othersDatum2.put(163553L, Arrays.asList("6", "50,0%", "100,0%", "100,0%", "47", "50,0%", "1", "5"));
          othersDatum2.put(163638L, Arrays.asList("4", "25,0%", "75,0%", "0,0%", "37", "50,0%", "0", "0"));
          othersDatum2.put(163650L, Arrays.asList("3", "33,3%", "66,7%", "0,0%", "34", "66,7%", "0", "0"));
          othersDatum2.put(169797L, Arrays.asList("3", "66,7%", "33,3%", "0,0%", "48", "66,7%", "1", "0"));
          othersDatum2.put(163651L, Arrays.asList("3", "66,7%", "100,0%", "0,0%", "55", "66,7%", "2", "1"));
          othersDatum2.put(169792L, Arrays.asList("1", "0,0%", "0,0%", "0,0%", "31", "100,0%", "0", "0"));
          othersDatum2.put(163620L, Arrays.asList("1", "100,0%", "100,0%", "0,0%", "46", "0,0%", "1", "0"));
          othersDatum2.put(169795L, Arrays.asList("1", "0,0%", "0,0%", "0,0%", "27", "0,0%", "0", "0"));
          othersDatum2.put(163680L, Arrays.asList("1", "0,0%", "0,0%", "0,0%", "40", "100,0%", "0", "0"));
          othersDatum2.put(163702L, Arrays.asList("1", "100,0%", "0,0%", "0,0%", "54", "100,0%", "0", "0"));

          return CompletableFuture.supplyAsync(() ->
                  ok(election2019stats6Async.render(policalParties, policalPartiesColors, relatedIds, relatedIds2, othersDatum, othersDatum2)), context.current());

        default :
          return CompletableFuture.supplyAsync(Results::badRequest, context.current());
    }
  }

  private void updatePolicalPartiesMap(Map<ActorSimpleHolder, List<ActorSimpleHolder>> parties, Long id, String lang, Long[] membersIds){
    Actor party = actorFactory.retrieve(id);
    if(party != null) {
      List<ActorSimpleHolder> members = new ArrayList<>();

      for(Long memberId : membersIds){
        Actor actor = actorFactory.retrieve(memberId);
        if(actor != null){
          members.add(new ActorSimpleHolder(actor, lang));
        }else{
          logger.debug("Unknown actor : " + memberId);
        }
      }

      parties.put(new ActorSimpleHolder(party, lang), members);
    }
  }

  private List<ActorSimpleHolder> getActorsFromIds(Long[] ids, String lang){
    return actorFactory.retrieveAll(Arrays.asList(ids))
            .stream().map(e -> new ActorSimpleHolder(e, lang)).collect(Collectors.toList());
  }

  /**
   * Custom asset mapper for external files
   *
   * @param file a file name to retrieve
   * @param type a file type key ('avatar', 'user', 'external', 'tmp')
   * @return the file input stream if file of given type has been found, an empty bad request otherwise
   */
  public Result getFile(String file, String type) {
    if (file == null) {
      return badRequest("");
    }
    try {
      switch (type) {
        case "avatar":
          return ok(new FileInputStream(configuration.getString("image.store.path") + file));
        case "user":
          return ok(new FileInputStream(configuration.getString("userimage.store.path") + file));
        case "external":
          return ok(new FileInputStream(configuration.getString("contribution.store.path") + file));
        case "tmp":
          return ok(new FileInputStream(configuration.getString("cache.store.path") + file));

        default:
          logger.warn("unrecognized file type " + type + " for file " + file);
          return badRequest("");
      }
    } catch (IOException e) {
      //logger.error("unable to get file " + file + " of type " + type, e);
      return badRequest("");
    }
  }

    /**
     * Custom asset mapper for external files
     *
     * @param file a file name to retrieve
     * @param type a file type key ('project')
     * @return the file input stream if file of given type has been found, an empty bad request otherwise
     */
    @Restrict(@be.objectify.deadbolt.java.actions.Group(WebdebRole.ADMIN))
    public Result getAdminFile(String file, String type) {
        if (file == null) {
            return badRequest("");
        }
        try {
            switch (type) {
                case "project":
                    return ok(new FileInputStream(configuration.getString("project.store.path") + file));

                default:
                    logger.warn("unrecognized file type " + type + " for file " + file);
                    return badRequest("");
            }
        } catch (IOException e) {
            //logger.error("unable to get file " + file + " of type " + type, e);
            return badRequest("");
        }
    }

  /**
   * Check if given picture (in form) is safe, ie does not contain any nudity or violence content
   *
   * @return an empty ok("") result if given picture is safe, a bad request with a message template otherwise
   */
  public CompletionStage<Result> checkOffensivePicture() {
    logger.debug("POST check offensive picture");
    Http.MultipartFormData<File> body = request().body().asMultipartFormData();
    Optional<Http.MultipartFormData.FilePart<File>> picture =
        body.getFiles().stream().filter(f -> f != null && f.getFile() != null && f.getFile().length() > 0).findFirst();

    if (picture.isPresent()) {
      File file = picture.get().getFile();

      files.saveToCache(file, file.getName());
      Http.Context ctx = ctx();

      return detector.isImageSafe(configuration.getString("server.hostname") + be.webdeb.presentation.web.controllers.routes.Application
          .getFile(file.getName(), "tmp").url()).handleAsync((safe, t) -> {
        if (safe || t != null) {
          return ok("");
        } else {
          Map<String, String> messages = new HashMap<>();
          messages.put(SessionHelper.ERROR, i18n.get(ctx.lang(), "general.upload.pic.unsafe"));
          return badRequest(message.render(messages));
        }
      });
    }
    return CompletableFuture.completedFuture(ok(""));
  }

  /**
   * Get help modal
   *
   * @return the help modal
   */
  public CompletionStage<Result> getHelpModal() {
    logger.debug("POST help modal");

    JsonNode data = request().body().asJson();
    logger.debug(Arrays.asList(data.asText().split(";"))+"//");

    return CompletableFuture.supplyAsync(() ->
        ok(helpModal.render(Arrays.asList(data.asText().split(";")))), context.current());
  }

  /**
   * Get the list of help keys
   *
   * @return the list of help keys
   */
  private List<String> getHelpList(){
    String values [] = {
            "bases",
            "login_group",
            "entry.debate",
            "entry.text",
            "entry.actor",
            "entry.citation",
            "analyze.debate",
            "analyze.text",
            "create_group"
    };

    return Arrays.asList(values);
  }

  /**
   * Display the page that content the default documentation
   *
   * @return the page that content default documentation
   */
  public Result defaultDocumentation() {
    return ok(documentation.render("2019", false, sessionHelper.getUser(ctx())));
  }

  /**
   * Display the page that content some documentations
   *
   * @param documentationId the documentation id to see
   * @param asHtml true if the pdf must be displayed as html
   * @return the page that content some documentations
   */
  public Result documentation(String documentationId, boolean asHtml) {
    return ok(documentation.render(documentationId, asHtml, sessionHelper.getUser(ctx())));
  }

  /**
   * Check the server status
   *
   * @return the server status
   */
  public CompletionStage<Result> checkStatus() {
    return CompletableFuture.supplyAsync(() -> ok("ok"), context.current());
  }

  /**
   * Get a image modal for the given image
   *
   * @param path the path of the image
   * @return the corresponding modal
   */
  public CompletionStage<Result> imageModal(String path) {
    return CompletableFuture.supplyAsync(() -> ok(imageModal.render(path)), context.current());
  }

  /**
   * Javascript AJAX router facility
   * (Note: if using this, do not forget to add a get/post line in routes file).
   *
   * All reverse routing calls are grouped in ajax-router.js file to abstract calls to reverse routers
   * behind javascript functions with the same names for easy maintenance and code cleaness.
   *
   * @return a promise of the Result, depending on the actual method called
   */
  public CompletionStage<Result> javascriptRoutes() {
    return CompletableFuture.supplyAsync(() -> ok(JavaScriptReverseRouter.create(
        // its name
        "jsRoutes",
        // ajax method to use
        "jQuery.ajax",
        // cross-cutting
        be.webdeb.presentation.web.controllers.routes.javascript.Application.acceptCookies(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.checkOffensivePicture(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.getHelpModal(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.imageModal(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.getModelDescription(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.executeApiQuery(),
        be.webdeb.presentation.web.controllers.routes.javascript.Application.collaboratorDetails(),

        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.searchContributions(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.contributionSelection(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.getMergeContributionsModal(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.delete(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.doMerge(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.getContributionHistoryModal(),
        be.webdeb.presentation.web.controllers.entry.routes.javascript.EntryActions.getContributionPlaces(),

        // actors
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.searchActor(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.findAffiliations(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActor(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActorFunctions(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getAutoCreatedActors(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getAutoCreatedProfessions(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.cancelFromModal(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.validateStep(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.uploadActorPicture(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.searchActorDetails(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getPictureFile(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.newAffiliations(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.addAffiliations(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActorCard(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActorContributions(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.searchProfessions(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.editProfession(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.sendEditProfession(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getLinkedContributions(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.resetActorType(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.searchPartyMembers(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActorPositionsForSocioValue(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.findActorCitations(),
        be.webdeb.presentation.web.controllers.entry.actor.routes.javascript.ActorActions.getActorTextCitations(),

        // texts
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.searchText(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.searchTextSource(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getAnnotatedText(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getTextContentOrHtmlContent(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getTextContent(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.extractPDFContent(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getTextLanguage(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getLinkedContributions(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getTextContentModal(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getHtmlContent(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.checkFreeSource(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.findTextByUrl(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.findTextCitations(),
        be.webdeb.presentation.web.controllers.entry.text.routes.javascript.TextActions.getTwitterEmbed(),
        // arguments
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.newArgument(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.newArgumentFromCitation(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.searchArgument(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.searchArgumentDictionary(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.saveArgumentJustificationLink(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.saveArgumentSimilarityLink(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.editDictionary(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.saveDictionary(),
        be.webdeb.presentation.web.controllers.entry.argument.routes.javascript.ArgumentActions.getArgumentCitationLinks(),

        // citations
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.newCitation(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.searchCitation(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.validateStep(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.saveCitationJustificationLink(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.saveCitationJustificationLinks(),
            be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.saveCitationPositionLink(),
            be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.saveCitationPositionLinks(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.citationSelection(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.searchCitationByType(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.newCitation(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.changeCitationPositionShadeModal(),
        be.webdeb.presentation.web.controllers.entry.citation.routes.javascript.CitationActions.changeCitationPositionShade(),
        // tags
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.newTagCategory(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.searchTag(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.saveTagLink(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.getLinkedContributions(),
        be.webdeb.presentation.web.controllers.entry.tag.routes.javascript.TagActions.findHierarchy(),
        // debate
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.editFromModal(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.saveFromModal(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.validateStep(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.searchDebate(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getLinkedContributions(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getContextArgumentStructure(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.changeContextArgumentStructure(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.changeJustificationShade(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getAddTextToDebateModal(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.addTextToDebate(),
        // viz
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getContributionName(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getShareMediaModal(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getContributionActors(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getFindContributionsModal(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.findContributions(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getPlaceName(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getTechnicalName(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getProfessionName(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getAffiliationName(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getSuggestionsContribution(),
        be.webdeb.presentation.web.controllers.viz.routes.javascript.VizActions.getPopularEntries(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getActorCitationInContext(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getTextCitationInContext(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getSociographyCitationInContext(),
        be.webdeb.presentation.web.controllers.entry.debate.routes.javascript.DebateActions.getSociographyCitationsConcurrent(),
        // search
        be.webdeb.presentation.web.controllers.browse.routes.javascript.BrowseActions.doSearch(),
        // contributor
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.authenticateWithOpenId(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.logoutAsync(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.uploadContributorPicture(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.askChangeMail(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.changeMail(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.askChangePassword(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.changePassword(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.recoverPassword(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.sendPasswordRecovery(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.resetPassword(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.doResetPassword(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.searchContributorContributions(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.searchPlace(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.searchExistingPlace(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.userIsBrowserWarned(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.resendSignupMail(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.sendSignupMail(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.contactus(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.sendContactus(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.askDeleteAccount(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.deleteAccount(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.editAdvices(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.sendAdvices(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.userContactUser(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.sendUserContactUser(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.getClaimContribution(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.saveClaimContribution(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.deleteClaim(),
        be.webdeb.presentation.web.controllers.account.routes.javascript.ContributorActions.retrieveClaims(),
        // group
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.changeDefaultGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.changeCurrentScope(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.retrieveContributorGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.searchGroups(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.searchContributors(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.newSubscription(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.getGroupResults(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.joinGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.leaveGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.setBannedInGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.editGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.saveGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.inviteInGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.sendInvitations(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.getMailToGroupModal(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.sendMailToGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.addContributionToGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.emptyGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.changeMembersRole(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.changeMemberRole(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.sendChangeMemberRole(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.followGroup(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.followGroups(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.editContributionsToExplore(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.sendContributionsToExplore(),
        be.webdeb.presentation.web.controllers.account.group.routes.javascript.GroupActions.groups(),

    // admin
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.changeRole(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.searchContributorsAndRoles(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.getCsvReports(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.importCsvFile(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.uploadCsvFile(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.getRssFeeders(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.editRssFeed(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.saveRssFeed(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.activateRssFeed(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.removeRssFeed(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.removeFreeCopyrightSource(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.getTwitterAccounts(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.editTwitterAccount(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.saveTwitterAccount(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.removeTwitterAccount(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.editProfessionHasLink(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.sendProfessionHasLink(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.mergeProfessions(),
        be.webdeb.presentation.web.controllers.account.admin.routes.javascript.AdminActions.sendMergeProfessions(),

    // project
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.manageProject(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.getProjectActivity(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.generateProjectUsers(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.deleteProjectUsers(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.editProject(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.editProjectGroup(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.editProjectSubgroup(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.saveProject(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.saveProjectGroup(),
        be.webdeb.presentation.web.controllers.account.admin.project.routes.javascript.ProjectActions.saveProjectSubgroup(),

        // elections
        routes.javascript.Application.otherElectionStatsPageAsync()

    )).as("text/javascript"), context.current());
  }
}
