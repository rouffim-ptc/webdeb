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
(function(e) { e.fn.dbExportationJs = function (options) {

    let eOptions,
        container = $(this),
        results = [],
        models = {},
        loadedModels = [],
        drawingModel = null,
        loaded = false,
        moreBtn = null,
        lock = false;

    $(document).ready(function() {
        initOptions();
        initListeners();
        load();
    });

    function initOptions(){

        if (options !== null && typeof(options) !== 'undefined') {
            eOptions = {

            };
        } else {
            eOptions = {

            };
        }

    }

    function initListeners() {

        container.on('click', '.model-loadmore', function(){
            if(drawingModel != null && !lock){
                lock = true;

                let submodel = getModel($(this).data('model'));
                let subcontainer = $($(this).data('target'));
                let isLoaded = $(this).data('model-loaded');

                if(submodel != null && subcontainer.exists() && isLoaded !== undefined && !isLoaded){
                    $(this).data('model-loaded', true);
                    submodel = submodel.clone();
                    submodel.draw(subcontainer);
                    loadedModels.push(submodel);
                    addSubmodelToModel(getModelById($(this).data('model-id')), submodel, $(this).data('attribute'));
                }

                lock = false;
            }
        });

        container.on('click', '.check_model', function(){
            if(drawingModel != null && !lock) {
                lock = true;

                let model = getModelById($(this).data('model'));
                model.attributes.forEach(function(attribute){
                    attribute.checked = $(this).is(':checked');
                });

                lock = false;
            }
        });

        container.on('click', '.check_attribute', function(){
            if(drawingModel != null && !lock) {
                lock = true;

                let modelId = $(this).data('model');
                let model = getModelById(modelId);
                model.attributes[$(this).data('attribute')].checked = $(this).is(':checked');

                if($(this).is(':checked')) {
                    $(this).parents('.model-draw').each(function () {
                        modelId = $(this).data('model');
                        getModelById(modelId).checked = true;
                        $('#check-model' + modelId).prop("checked", true);
                    });
                }

                lock = false;
            }
        });

        container.on('click', '.do-export', function(){
            if(drawingModel != null && !lock) {
                lock = true;

                execute();

                lock = false;
            }
        });
    }

    function execute() {
        console.log(drawingModel.getAsExportableQuery());
        /*executeApiQuery(drawingModel.getAsExportableQuery()).done(function(r){
            results = r;
            drawPartialResults();
        }).fail(function(fail){
            console.log(fail);
        });*/
    }

    function drawPartialResults(){
        if(results.length > 0) {
            let table = createResultsArrayHeader();
            createResultsArrayBody(table, false);
            createMoreResultsButton();
        }
    }

    function drawFullResults(){
        if(results.length > 0) {
            let table = createResultsArrayHeader();
            createResultsArrayBody(table, true);
        }
    }

    function createResultsArrayBody(table, full){
        let tbody = $('<tbody></tbody>').appendTo(table);
        let copy = full ? results.slice(1) : results.slice(1, 10);

        for(let i in copy){
            let tr = $('<tr></tr>').appendTo(tbody);
            $('<th scope="row">' + (parseInt(i) + 1) + '</th>').appendTo(tr);
            copy[i].forEach(function(element) {
                $('<td>' + element + '</td>').appendTo(tr);
            });
        }
    }

    function createResultsArrayHeader(){
        let table_container = $('<div class="table-responsive" style="display:none"></div>');
        let table = $('<table class="table"></table>').appendTo(table_container);

        let theader = $('<tr></tr>').appendTo('<thead></thead>').appendTo(table);
        $('<th scope="col">#</th>').appendTo(theader);
        results[0].forEach(function(element) {
            $('<th scope="col">' + element + '</th>').appendTo(theader);
        });

        container.empty().append(table_container);

        return table;
    }

    function createMoreResultsButton(){
        moreBtn = $('<button type="button" class="btn btn-secondary">Secondary</button>').appendTo(container);
        moreBtn.on('click', function(){
            drawFullResults();
        });
    }

    function load(){

        if(!loaded) {
            getModelDescription().done(function (data) {
                models = {};
                loadedModels = [];

                data.forEach(function (model) {
                    let attributes = [];

                    model.attributesMap.forEach(function (attribute) {
                        attributes.push(new ExportableModelAttribute(attribute.attributeName, attribute.attributeTechName, attribute.relationType, attribute.id));
                    });

                    models[model.modelName.toLowerCase()] = new ExportableModel(0, model.modelName, attributes);
                });

                loaded = true;
                drawModel('actor');
            }).fail(function (e) {
                loaded = false;
                console.log(e);
            });
        }
    }

    function getModel(name){
        console.log(name);
        console.log(models);
        console.log(models);
        if(loaded && models.hasOwnProperty(name.toLowerCase())) {
            return models[name.toLowerCase()];
        }

        return null;
    }

    function drawModel(name){
        drawingModel = getModel(name);
console.log(drawingModel);
        if(drawingModel != null) {
            drawingModel = drawingModel.clone();
            loadedModels.push(drawingModel);

            container.empty();
            drawingModel.draw();
        }

        container.append('<button type="button" class="btn btn-primary do-export">Export</button>');
    }

    function getId(){
        return loadedModels.length;
    }

    function addSubmodelToModel(model, submodel, attributeId){
        if(model != null && submodel != null) {
            model.attributes[attributeId].submodel = submodel;
        }
    }

    function getModelById(id){
        return loadedModels !== undefined ? loadedModels[id] : null;
    }


    class ExportableQuery {
        constructor(model) {
            this.model = model;
            this.attributes = [];
            this.joins = [];
            this.wheres = [];
        }
    }

    class ExportableQueryJoin {
        constructor(attribute1, attribute2) {
            this.attribute1 = attribute1;
            this.attribute2 = attribute2;
        }
    }

    class ExportableQueryWhere {
        constructor(attribute, type, values) {
            this.attribute = attribute;
            this.type = type;
            this.values = values;
        }
    }

    class ExportableQueryAttribute {
        constructor(alias, property) {
            this.alias = alias;
            this.property = property;
        }
    }

    class ExportableQueryAlias {
        constructor(name, model) {
            this.name = name;
            this.model = model;
        }
    }

    class ExportableModel{

        constructor(id, name, attributes){
            this.id = id;
            this.name = name;
            this.attributes = attributes;
            this.checked = false;
            this.alias = null;
            this.queryAlias = null;
        }

        draw(){
            let that = this;

            let modelContainer = $('<div class="model-draw p-2" data-model="' + this.id + '" style="background-color: ' + this.getBackgroundColor() + '"></div>').appendTo(container);

            let titleContainer = $('<div class="d-flex"></div>').appendTo(modelContainer);
            $('<input type="checkbox" id="check-model' + this.id + '" class="check_model mr-2" data-model="' + this.id + '">').appendTo(titleContainer);
            $('<h4 class="mb-0"><label class="mb-0" for="check-model' + this.id + '">' + this.name + '</label></h4>').appendTo(titleContainer);

            $('<hr>').appendTo(modelContainer);

            this.attributes.forEach(function(attribute, index){
                attribute.draw(modelContainer, that.id, index);
                modelContainer.append('<hr>');
            });
        }

        clone(){
            let attributes = [];

            this.attributes.forEach(function(attribute){
                attributes.push(new ExportableModelAttribute(attribute.name, attribute.techname, attribute.relationType, attribute.isId));
            });

            return new ExportableModel(getId(), this.name, attributes);
        }

        getBackgroundColor(){
            let nbParents = container.parents('.model-draw').length;

            let nb = (255 - (6 * nbParents));

            if(nb < 0) nb = 0;

            return '#' + nb.toString(16) + nb.toString(16) + nb.toString(16);
        }

        getAttributeId(){
            let ids = this.attributes.filter(attribute => attribute.isId === true);
            return ids.length === 0 ? null : ids[0];
        }

        getAlias(){
            if(this.alias == null) {
                let alias = '';

                for (let iChar = 0; iChar < this.name.length; iChar++) {
                    let char = this.name.charAt(iChar);

                    if (char === char.toUpperCase()) {
                        alias += char;
                    }
                }

                this.alias = alias + this.id;
            }

            return this.alias;
        }

        getAsQueryAlias() {
            if(this.queryAlias == null){
                this.queryAlias = new ExportableQueryAlias(this.getAlias(), this.name);
            }

            return this.queryAlias;
        }

        getAsExportableQuery() {
            let query = new ExportableQuery(this.getAlias());
            this.doExportableQuery(query);
            return query;
        }

        doExportableQuery(query) {
            let that = this;

            this.attributes.forEach(attribute => {
                let hasSubmodel = ExportableModel.prototype.isPrototypeOf(attribute.submodel);

                if(attribute.checked || (hasSubmodel && attribute.submodel.checked)) {
                    let queryAttribute = new ExportableQueryAttribute(that.getAsQueryAlias(), attribute.techname);
                    if(attribute.checked)
                        query.attributes.push(queryAttribute);

                    if(hasSubmodel && attribute.submodel.checked) {
                        let submodelAttributeId = attribute.submodel.getAttributeId();

                        if(submodelAttributeId != null) {
                            let joinAttribute = new ExportableQueryAttribute(attribute.submodel.getAsQueryAlias(), submodelAttributeId.techname);
                            query.joins.push(new ExportableQueryJoin(queryAttribute, joinAttribute));
                            attribute.submodel.doExportableQuery(query);
                        }
                    }
                }
            });
        }
    }

    class ExportableModelAttribute{

        constructor(name, techname, relationType, isId){
            this.name = name;
            this.techname = techname;
            this.relationType = relationType;
            this.isId = isId;
            this.checked = false;
            this.submodel = {};
        }

        draw(container, modelId, attributeId){
            let attributeContainer = $('<div class="ml-2"></div>');
            let id = '-' + modelId + '-' + attributeId;

            if(!this.relationIsMultiple())
                $('<input type="checkbox" id="check-attribute' + id + '" class="check_attribute" data-model="' + modelId + '" data-attribute="' + attributeId + '">').appendTo(attributeContainer);

            let forMultiple = this.relationIsMultiple() ? ' (' + this.techname + ') ' +
                '<button class="btn btn-primary model-loadmore" type="button" ' +
                'data-toggle="collapse" data-target="#collaspe-submodel' + id + '" data-model="' + this.name + '"' +
                'data-model-loaded="false" data-model-id="' + modelId + '" data-attribute="' + attributeId + '" ' +
                'aria-expanded="false" aria-controls="collaspe-model-' + id + '">More</button>' : '';

            $('<span class="ml-2"><label ' + (!this.relationIsMultiple() ? 'for="check-attribute' : '') + id + '">' + (this.relationIsMultiple() ? this.name : this.techname) + '</label>'
                + (this.relationIsMultiple() ? forMultiple : '') + '</span>').appendTo(attributeContainer);

            if(this.relationIsMultiple())
                $('<div id="collaspe-submodel' + id + '"></div>').appendTo(attributeContainer);

            return container.append(attributeContainer);
        }

        relationIsMultiple(){
            return this.relationType === "ONE_TO_MANY" || this.relationType === "ONE_TO_ONE"
                || this.relationType === "MANY_TO_ONE" || this.relationType === "MANY_TO_MANY";
        }
    }



};}(jQuery));