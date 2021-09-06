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

package be.webdeb.core.impl.debate;

import be.webdeb.core.api.contribution.Contribution;
import be.webdeb.core.api.contribution.EContributionType;
import be.webdeb.core.api.contributor.ContributorFactory;
import be.webdeb.core.api.debate.Debate;
import be.webdeb.core.api.debate.DebateFactory;
import be.webdeb.core.api.debate.DebateHasText;
import be.webdeb.core.api.text.Text;
import be.webdeb.core.exception.FormatException;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.core.impl.contribution.link.AbstractContributionLink;
import be.webdeb.infra.persistence.accessor.api.DebateAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a DebateHasText.
 *
 * @author Martin Rouffiange
 */
public class ConcreteDebateHasText extends AbstractContributionLink<DebateFactory, DebateAccessor> implements DebateHasText {

    private Debate debate;
    private Text text;

    /**
     * Create a DebateHasText instance
     *
     * @param factory the debate factory
     * @param accessor the debate accessor
     * @param contributorFactory the contributor accessor
     */
    ConcreteDebateHasText(DebateFactory factory, DebateAccessor accessor, ContributorFactory contributorFactory) {
        super(factory, accessor, contributorFactory);
        type = EContributionType.DEBATE_HAS_TEXT;
    }

    @Override
    public Debate getDebate() {
        return debate;
    }

    @Override
    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    @Override
    public Text getText() {
        return text;
    }

    @Override
    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        return super.hashCode() +
                originId.hashCode() +
                destinationId.hashCode();
    }

    @Override
    public Map<Integer, List<Contribution>> save(Long contributor, int currentGroup) throws FormatException, PermissionException, PersistenceException {
        List<String> isValid = isValid();
        if (!isValid.isEmpty()) {
            logger.error("debate has text contains errors " + isValid.toString());
            throw new FormatException(FormatException.Key.LINK_ERROR, String.join(",", isValid.toString()));
        }
        accessor.save(this, currentGroup, contributor);
        return new HashMap<>();
    }
}
