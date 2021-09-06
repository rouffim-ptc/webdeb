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

package be.webdeb.presentation.web.controllers.entry.argument;

import be.webdeb.core.api.argument.ArgumentJustification;
import be.webdeb.core.api.contribution.ContextContribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.debate.DebateHolder;
import be.webdeb.presentation.web.controllers.entry.link.BaseJustificationLinkForm;
import be.webdeb.presentation.web.controllers.permission.WebdebUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class holds concrete values of an ArgumentJustification link (i.e. no type/data IDs, but their descriptions, as
 * defined in the database)
 *
 * @author Martin Rouffiange
 */
public class ArgumentJustificationLinkForm extends BaseJustificationLinkForm {

    private ArgumentHolder argument;
    private DebateHolder debate = null;

    /**
     * Play / JSON compliant constructor
     */
    public ArgumentJustificationLinkForm() {
        super();
        type = EContributionType.ARGUMENT_JUSTIFICATION;
    }

    /**
     * Construct a link wrapper with a given ArgumentJustification link (full initialization)
     *
     * @param link an existing ArgumentJustification
     * @param context the context contribution
     * @param lang 2-char ISO code of context language (among play accepted languages)
     */
    public ArgumentJustificationLinkForm(ArgumentJustification link, ContextContribution context, WebdebUser user, String lang) {
        super(link, user, lang);

        destinationTitle = link.getArgument().getFullTitle();
        argument = new ArgumentHolder(link.getArgument(), user, context, this, lang);

        if(link.getDebate() != null)
            debate = new DebateHolder(link.getDebate(), user, lang);
    }

    /**
     * Transform this form into an API justification link
     *
     * @return an API justification link corresponding to this justification link form
     * @throws PersistenceException if given linkshade could not be casted into an int value
     */
    @Override
    public ArgumentJustification toLink() throws PersistenceException {
        ArgumentJustification link = argumentFactory.getArgumentJustificationLink();
        updateLink(link);
        return link;
    }

    public ArgumentHolder getArgument() {
        return argument;
    }

    public DebateHolder getDebate() {
        return debate;
    }
}
