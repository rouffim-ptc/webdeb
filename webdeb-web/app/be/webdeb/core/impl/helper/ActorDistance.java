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

import org.jetbrains.annotations.NotNull;

/**
 * This class holds all needed informations for a position actor distance and another one.
 *
 * @author Martin Rouffiange
 */
public class ActorDistance extends ActorHelper implements Comparable<ActorDistance> {

    private Double totalDistance = null;
    private Double distance;
    private int nbDebates;
    private int nbActorLinks;
    private int nbConcurrentLinks;
    private double relevance;

    private Long subDebateId;
    private Long relatedId;
    private String relatedName;

    public ActorDistance(Long id, String name, String avatar, Integer type, String gender) {
        super(id, name, avatar, type, gender);

        init();
    }

    public ActorDistance(Long id, String name, Long subDebateId, Long relatedId, String relatedName) {
        super(id, name);

        init();

        this.subDebateId = subDebateId;
        this.relatedId = relatedId;
        this.relatedName = relatedName;
    }


    public void addDistance(Double actorDistance, Double concurrentDistance, int nbActorLinks, int nbConcurrentLinks) {
        this.distance += Math.abs(actorDistance - concurrentDistance);
        this.nbActorLinks += nbActorLinks;
        this.nbConcurrentLinks += nbConcurrentLinks;
        this.relevance *= (nbActorLinks + nbConcurrentLinks);
        this.nbDebates++;
    }


    public Double getDistance() {
        if (totalDistance == null) {
            totalDistance = (nbDebates > 0 ? (double) Math.round((distance / nbDebates) * 100) / 100 : 0) / 4;
        }
        return totalDistance;
    }


    public Integer getNbDebates() {
        return nbDebates;
    }

    public Integer getNbActorLinks() {
        return nbActorLinks;
    }

    public Integer getNbConcurrentLinks() {
        return nbConcurrentLinks;
    }

    /**
     * Get the relevance value for this distance. The relevance is the number of
     * debate * (nb_links_actor_per_debate + nb_links_concurrent_per_debate)
     *
     * @return the relevance
     */
    public Double getRelevance() {
        return relevance;
    }

    public Long getSubDebateId() {
        return subDebateId;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public String getRelatedName() {
        return relatedName;
    }

    @Override
    public int compareTo(@NotNull ActorDistance o) {
        return o.getId().equals(-1L) ? -1 : this.getId().equals(-1L) ? 1 :
                o.getRelevance().equals(this.getRelevance()) ?
                this.getDistance().compareTo(o.getDistance()) :
                o.getRelevance().compareTo(this.getRelevance());
    }

    private void init() {
        this.distance = (double) 0;
        this.nbDebates = 0;
        this.nbActorLinks = 0;
        this.nbConcurrentLinks = 0;
        this.relevance = 1;
    }
}
