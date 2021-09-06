/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

package be.webdeb.presentation.web.controllers.entry;

import be.webdeb.core.api.actor.ActorRole;
import be.webdeb.core.api.actor.EActorRole;
import be.webdeb.core.api.contribution.TextualContribution;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.actor.ActorSimpleForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;
import be.webdeb.presentation.web.controllers.viz.actor.EActorVizPane;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.i18n.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class holds concrete values for a TextualContribution. It manage all has_actor dependancy.
 *
 * @author Martin Rouffiange
 */
public abstract class TextualContributionHolder extends ContributionHolder{

    protected List<ActorSimpleForm> authors = null;
    protected List<ActorSimpleForm> reporters = null;
    protected List<ActorSimpleForm> citedactors = null;

    // for step form
    protected Boolean hasAuthors = null;
    protected Boolean hasReporters = null;
    protected Boolean hasCitedactors = null;


    public TextualContributionHolder() {
        super();
    }

    public TextualContributionHolder(TextualContribution contribution, WebdebUser user, String lang) {
        this(contribution, user, lang, false);
    }

    public TextualContributionHolder(TextualContribution contribution, WebdebUser user, String lang, boolean light) {
        this(contribution, user, lang, light, false);
    }

    public TextualContributionHolder(TextualContribution contribution, WebdebUser user, String lang, boolean light, boolean ultraLight) {
        super(contribution, user, lang, light);

        if(!ultraLight) {
            initTags(contribution.getTagsAsList(), lang);
            initPlaces(contribution.getPlaces(), lang);
        }
        //initActors(contribution.getActors(), lang);
    }

    /**
     * Initialize authors, reporters and cited actors fields with this contribution actor roles
     *
     */
    protected void initActors() {
        if(authors == null) {

            authors = new ArrayList<>();
            reporters = new ArrayList<>();
            citedactors = new ArrayList<>();

            if (contribution != null) {
                ((TextualContribution) contribution).getActors().forEach(r -> {
                    ActorSimpleForm a = new ActorSimpleForm(r, lang);
                    if (r.isAuthor()) {
                        //initActorFilter(a);
                        authors.add(a);
                    } else if (r.isReporter()) {
                        reporters.add(a);
                    } else {
                        citedactors.add(a);
                    }
                });

                hasAuthors = !authors.isEmpty();
                hasReporters = !reporters.isEmpty();
                hasCitedactors = !citedactors.isEmpty();
            }
        }
    }

    /*
     * GETTERS
     */

    /**
     * Get the list of authors of this textual contribution
     *
     * @return a list of authors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getAuthors() {
        initActors();
        return authors;
    }

    /**
     * Get the list of actors being the thinker of this textual contribution
     *
     * @return the list of actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getReporters() {
        initActors();
        return reporters;
    }

    /**
     * Get the list of cited actors of this textual contribution
     *
     * @return a list of cited actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getCitedactors() {
        initActors();
        return citedactors;
    }

    /**
     * Get the list of actors being the authors of this textual contribution limited by max size
     *
     * @return the list of actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getAuthors(int maxSize) {
        return ((TextualContribution) contribution).getActors(maxSize, EActorRole.AUTHOR).stream()
                .map(a ->  new ActorSimpleForm(a, lang))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of actors being the thinker of this textual contribution
     *
     * @return the list of actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getReporters(int maxSize) {
        return ((TextualContribution) contribution).getActors(maxSize, EActorRole.REPORTER).stream()
                .map(a ->  new ActorSimpleForm(a, lang))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of actors being cited of this textual contribution limited by max size
     *
     * @return the list of actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getCitedactors(int maxSize) {
        return ((TextualContribution) contribution).getActors(maxSize, EActorRole.CITED).stream()
                .map(a ->  new ActorSimpleForm(a, lang))
                .collect(Collectors.toList());
    }

    /**
     * Get the number of actors by given role of this contribution
     *
     * @return the number of actors
     */
    @JsonIgnore
    public int getNbActors(EActorRole role) {
        return ((TextualContribution) contribution).getNbActors(role);
    }

    /**
     * Get the list of all directly involved actors as authors or reporters
     *
     * @return a list of actors
     */
    @JsonIgnore
    public List<ActorSimpleForm> getActors() {
        initActors();
        return Stream.concat(Stream.concat(authors.stream(), reporters.stream()), citedactors.stream()).collect(Collectors.toList());
    }

    public String getAuthorsDescription() {
        return getActorDescription(getAuthors(3), false) +
                (getNbActors(EActorRole.AUTHOR) - 3 > 0 ? " " + i18n.get(Lang.get(lang).get(), "actor.authors.others.label", getNbActors(EActorRole.AUTHOR) - 3) : "");
    }

    public String getAuthorsDescriptionWithLink() {
        return getAuthorsDescriptionWithLink(EActorVizPane.BIBLIOGRAPHY.id(), 0);
    }
    public String getAuthorsDescriptionWithLink(int pane, int pov) {
        return getActorDescription(getAuthors(3), true, pane, pov) +
                (getNbActors(EActorRole.AUTHOR) - 3 > 0 ? " " + i18n.get(Lang.get(lang).get(), "actor.authors.others.label", getNbActors(EActorRole.AUTHOR) - 3) : "");
    }



    protected void boundActors(TextualContribution contribution) throws PersistenceException{
        try{
            initActors();
            contribution.initActors();

            for (ActorSimpleForm a : authors.stream().filter(n -> !n.isFullnameEmpty()).collect(Collectors.toList())) {
                a.setAuthor(true);
                a.setReporter(false);
                a.setCited(false);
                contribution.addActor(helper.toActorRole(a, contribution, lang));
            }

            for (ActorSimpleForm a : reporters.stream().filter(n -> !n.isFullnameEmpty()).collect(Collectors.toList())) {
                a.setAuthor(false);
                a.setReporter(true);
                a.setCited(false);
                contribution.addActor(helper.toActorRole(a, contribution, lang));
            }

            for (ActorSimpleForm a : citedactors.stream().filter(n -> !n.isFullnameEmpty()).collect(Collectors.toList())) {
                a.setAuthor(false);
                a.setReporter(false);
                a.setCited(true);
                contribution.addActor(helper.toActorRole(a, contribution, lang));
            }

        }catch (FormatException e) {
            logger.debug("error while adding list of authors", e);
            throw new PersistenceException(PersistenceException.Key.SAVE_CONTEXT_CONTRIBUTION, e);
        }

        // handle possible duplicates in unknown affiliation actors
        helper.removeDuplicateUnknownActors(contribution, lang);
    }

    @JsonIgnore
    public Boolean getHasAuthors() {
        return hasAuthors;
    }

    @JsonIgnore
    public Boolean getHasReporters() {
        return hasReporters;
    }

    @JsonIgnore
    public Boolean getHasCitedactors() {
        return hasCitedactors;
    }

    public void setHasAuthors(Boolean hasAuthors) {
        this.hasAuthors = hasAuthors;
    }

    public void setHasReporters(Boolean hasReporters) {
        this.hasReporters = hasReporters;
    }

    public void setHasCitedactors(Boolean hasCitedactors) {
        this.hasCitedactors = hasCitedactors;
    }

}
