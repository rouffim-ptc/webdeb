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

package be.webdeb.core.impl.argument;

import be.webdeb.core.api.argument.*;
import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ArgumentAccessor;
import play.i18n.Lang;

import java.util.*;

/**
 * This class implements a Webdeb shaded argument, ie, a sentence written by a contributor began with shade.
 *
 * @author Fabian Gilson
 * @author Martin Rouffiange
 */
class ConcreteArgumentShaded extends ConcreteArgument implements ArgumentShaded {

    protected ArgumentShade argumentShade;

    /**
     * Default constructor.
     *
     * @param factory the argument factory (to get and build concrete instances of objects)
     * @param accessor the argument accessor to retrieve and persist arguments
     * @param contributorFactory the contributor factory to get bound contributors
     */
    ConcreteArgumentShaded(ArgumentFactory factory, ArgumentAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        argumentType = EArgumentType.SHADED;
    }

    /*
     * GETTERS / SETTERS
     */

    @Override
    public String getFullTitle() {
        return computeShadeAndTitle(this.argumentShade);
    }

    @Override
    public String getFullTitle(EArgumentShade shade) throws FormatException {
        if(shade == null) {
            throw new FormatException(FormatException.Key.UNKNOWN_ARGUMENT_SHADE_TYPE, argumentShade.toString());
        }
        return computeShadeAndTitle(factory.getArgumentShade(shade.id()));
    }

    @Override
    public ArgumentShade getArgumentShade() {
        return argumentShade;
    }

    @Override
    public void setArgumentShade(ArgumentShade argumentShade) throws FormatException {
        if(!argumentShade.isValid()){
            throw new FormatException(FormatException.Key.ARGUMENT_ERROR, argumentShade.toString());
        }
        this.argumentShade = argumentShade;
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PersistenceException, PermissionException {
        List<String> errors = isValid();
        if (!errors.isEmpty()) {
            logger.error("argument contains error " + errors.toString());
            throw new FormatException(FormatException.Key.ARGUMENT_ERROR, String.join(",", errors));
        }
        accessor.save(this, currentGroup, contributor);
        return null;
    }

    @Override
    public List<String> isValid() {
        List<String> fieldsInError = super.isValid();

        if (argumentShade == null || !argumentShade.isValid()) {
            fieldsInError.add("shade is null or invalid");
        }

        return fieldsInError;
    }

    /**
     * Compute a language-specific fully readable shade an title combined
     *
     * @param shade an argument shade
     * @return a reader-friendly representation of the shade and title of the argument
     */
    private String computeShadeAndTitle(ArgumentShade shade) {
        if(isValid().isEmpty()) {
            String shadeterm = factory.makeShadeReaderFriendly(shade.getName(language.getCode()), title, language.getCode());
            String title = this.title.substring(0, 1).toUpperCase() + this.title.substring(1) + (this.title.endsWith("!") ? "" : ".");
            String title2 = title.substring(0, 1).toLowerCase() + title.substring(1);

            switch (argumentShade.getEType()) {
                case NO_SHADE:
                    return factory.getValuesHelper().firstLetterUpper(title);
                case NO_DOUBT:
                    return title;
                case STRONG_AND_WEAK_POINTS:
                    return shadeterm + title2 + " " + i18n.get(Lang.forCode(language.getCode()), "argument.debate.2.end");
                default:
                    // all other types
                    return shadeterm + title2;
            }
        }

        return "";
    }

}
