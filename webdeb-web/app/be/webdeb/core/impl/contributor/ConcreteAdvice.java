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

package be.webdeb.core.impl.contributor;

import be.webdeb.core.api.contributor.Advice;
import be.webdeb.core.exception.PermissionException;
import be.webdeb.core.exception.PersistenceException;
import be.webdeb.infra.persistence.accessor.api.ContributorAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements an Advice.
 *
 * @author Martin Rouffiange
 */
public class ConcreteAdvice implements Advice {

    // custom logger
    protected static final org.slf4j.Logger logger = play.Logger.underlying();

    private int id;
    // map of (lang, title)
    private Map<String, String> i18names = null;

    private ContributorAccessor accessor;

    /**
     * Constructor
     */
    ConcreteAdvice(ContributorAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName(String lang) {
        getNames();
        return i18names.get(lang);
    }

    @Override
    public Map<String, String> getNames() {
        if(i18names == null){
            i18names = new HashMap<>();
        }
        return i18names;
    }

    @Override
    public void setNames(Map<String, String> names) {
        this.i18names = names;
    }

    @Override
    public void addName(String name, String lang) {
        getNames();
        i18names.put(lang, name);
    }

    @Override
    public void save(Long contributor) throws PersistenceException, PermissionException {
        accessor.save(this, contributor);
    }
}
