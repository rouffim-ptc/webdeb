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

package be.webdeb.core.impl.helper;

import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.api.Play;

import javax.inject.Inject;

/**
 * This abstract class holds all common needed informations related to actor.
 *
 * @author Martin Rouffiange
 */
public abstract class ActorHelper {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

    protected Long id;
    protected String name;
    protected String avatar;
    protected Integer actorType;

    public ActorHelper(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ActorHelper(Long id, String name, String avatar, Integer actorType) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.actorType = actorType;
    }

    public ActorHelper(Long id, String name, String avatar, Integer type, String gender) {
        this.id = id;
        this.name = name;
        this.actorType = type;
        this.avatar = avatar == null ? type != null ? helper.computeAvatar(type, gender) : null :
                "/avatar/" + avatar;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public String getJsonName() {
        return name != null ? name.replace("\"", "”") : "";
    }

    public String getAvatar() {
        return avatar;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
