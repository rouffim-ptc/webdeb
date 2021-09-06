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

package be.webdeb.presentation.web.controllers.account.admin;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.util.ValuesHelper;
import play.api.Play;

import javax.inject.Inject;

/**
 * This class is used as a supertype wrapper for any profession form
 *
 * @author Martin Rouffiange
 */
public abstract class AbtractProfessionForm {

    @Inject
    protected ValuesHelper valuesHelper = Play.current().injector().instanceOf(ValuesHelper.class);
    @Inject
    protected ActorFactory actorFactory = Play.current().injector().instanceOf(ActorFactory.class);
    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    /**
     * Play / Json compliant constructor
     */
    public AbtractProfessionForm() {
        // needed by json/play
    }
}
