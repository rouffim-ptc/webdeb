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

package be.webdeb.presentation.web.controllers.account;

import be.webdeb.core.api.contributor.Advice;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.presentation.web.controllers.entry.ContributionHelper;
import be.webdeb.util.ValuesHelper;
import play.api.Play;
import play.data.validation.ValidationError;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class AdvicesForm {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    @Inject
    protected ContributorFactory factory = Play.current().injector().instanceOf(ContributorFactory.class);
    @Inject
    protected ContributionHelper helper = Play.current().injector().instanceOf(ContributionHelper.class);
    @Inject
    protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

    protected List<AdviceForm> advices;

    /**
     * Play / JSON compliant constructor
     */
    public AdvicesForm() { }

    /**
     * Constructor from given advices
     *
     * @param advices a list of advices
     */
    public AdvicesForm(List<Advice> advices, String lang) {
        this.advices = advices.stream().map(a -> new AdviceForm(a, lang)).collect(Collectors.toList());
    }

    /**
     * Form validation (implicit call from form submit)
     *
     * @return a map of ValidationError if any error in form was found, null otherwise
     */
    public Map<String, List<ValidationError>> validate() {
        Map<String, List<ValidationError>> errors = new HashMap<>();

        // must return null if errors is empty
        return errors.isEmpty() ? null : errors;
    }

    /**
     * Save this AdvicesForm in persistence layer
     *
     * @param contributor the contributor id that created this link
     *
     * @throws PermissionException if given contributor may not perform this action or if such action would cause
     * integrity problems
     * @throws PersistenceException if any error occurred at the persistence layer (concrete error is wrapped)
     *
     */
    public void save(Long contributor) throws PermissionException, PersistenceException {
        logger.debug("try to save advices");

        Set<Integer> idsSet = new HashSet<>();

        for(AdviceForm form : advices) {
            if(!form.isEmpty()) {
                Advice advice = factory.getAdvice();

                advice.setId(form.getId());

                if(!values.isBlank(form.frTitle))
                    advice.addName(form.frTitle, "fr");
                if(!values.isBlank(form.enTitle))
                    advice.addName(form.enTitle, "en");
                if(!values.isBlank(form.nlTitle))
                    advice.addName(form.nlTitle, "nl");

                advice.save(contributor);
                idsSet.add(advice.getId());
            }
        }

        factory.deleteAdvices(idsSet);
    }

    /*
     * GETTERS and SETTERS
     */

    public List<AdviceForm> getAdvices() {
        return advices;
    }

    public void setAdvices(List<AdviceForm> advices) {
        this.advices = advices;
    }
}
