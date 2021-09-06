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

import be.webdeb.core.api.text.TextCopyrightfreeSource;
import play.api.Play;
import play.data.validation.ValidationError;

import java.util.*;
import java.util.stream.Collectors;

import be.webdeb.util.ValuesHelper;

import javax.inject.Inject;

/**
 * Simple form class that contains a free copyright freeSources for text
 *
 * @author Martin Rouffiange
 */
public class TextCopyrightfreeSourcesForm {

  @Inject
  protected ValuesHelper values = Play.current().injector().instanceOf(ValuesHelper.class);

  // custom logger
  protected static final org.slf4j.Logger logger = play.Logger.underlying();

  private List<TextCopyrightfreeSourceForm> freeSources;

  /**
   * Play / Json compliant constructor
   */
  public TextCopyrightfreeSourcesForm() {
    // needed by json/play
  }

  /**
   * Initialize a free freeSources form
   *
   * @param sources a list of free freeSources
   */
  public TextCopyrightfreeSourcesForm(List<TextCopyrightfreeSource> sources) {
    this.freeSources = (sources != null ?
        sources.stream().map(e ->
            new TextCopyrightfreeSourceForm(e.getSourceId(), e.getDomainName())).collect(Collectors.toList())
        : new ArrayList<>());
  }

  /**
   * Play form validation method
   *
   * @return a list of errors or null if none
   */
  public Map<String, List<ValidationError>> validate() {
    Map<String, List<ValidationError>> errors = new HashMap<>();

    for(int i = 0; i < freeSources.size(); i++) {
      TextCopyrightfreeSourceForm source = freeSources.get(i);
      String field = "freeSources[" + i + "].domainName";
      if(values.isBlank(source.getDomainName())){
        freeSources.remove(i);
      }else {
        String dn = values.getURLDomain(values.transformURL(source.getDomainName()));
        if (dn.equals("")) {
          errors.put(field, Collections.singletonList(
              new ValidationError(field, "admin.freeSource.error.invalidDomainName")));
        }else{
          source.setDomainName(dn);
        }
      }
    }

    return errors.isEmpty() ? null : errors;
  }

  public List<TextCopyrightfreeSourceForm> getFreeSources() {
    return freeSources;
  }

  public void setFreeSources(List<TextCopyrightfreeSourceForm> freeSources) {
    this.freeSources = freeSources;
  }
}
