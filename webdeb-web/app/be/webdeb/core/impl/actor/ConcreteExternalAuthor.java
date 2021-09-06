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
package be.webdeb.core.impl.actor;

import be.webdeb.core.api.actor.ActorFactory;
import be.webdeb.core.api.actor.ExternalAuthor;

/**
 * This class implements an ExternalAuthor.
 *
 * @author Martin Rouffiange
 */
public class ConcreteExternalAuthor implements ExternalAuthor {

    private Long id = -1L;
    private String name;

    private ActorFactory factory;

    /**
     * Create a ExternalAuthor instance
     *
     * @param factory the actor factory
     */
    ConcreteExternalAuthor(ActorFactory factory) {
        this.factory = factory;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}