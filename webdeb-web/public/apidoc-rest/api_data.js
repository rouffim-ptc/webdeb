define({ "api": [
  {
    "type": "get",
    "url": "/rest/bycriteria/:query",
    "title": "Retrieve contributions by criteria",
    "name": "ByCriteria",
    "group": "Services",
    "description": "<p>this service is used to retrieve contributions from the database by passing a list of (key, value) pairs to search for. Multiple values for the same key may be passed</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "query",
            "description": "<p>a query with a collection of key-value pairs used as search criteria. All pairs are separated by &quot;+&quot; and keys are separated to values by &quot;=&quot;. Accepted criterion keys are:</p> <p>All contribution types: <ul> <li>contribution_type: 0 for Actor, 1 for Argument, 2 for Text</li> <li>topic: any string value denoting a topic</li> <li>validated: 'true' if only validated contributions must be retrieved, 'false' or unset for any</li> <li>strict: 'true' if all values passed must be treated as strict matches (ex: 'Louis' won't return back 'Louise')</li> <li>fromid: any integer value defining the lower id from which the contributions must be retrieved</li> <li>orderby: only valid for requests with only \"contribution_type\" keys. May pass either \"name\" (default) or \"id\". <li>fetched: 'true' if only automatically fetched contributions must be retrieved (ie, contributions imported from other sources without validation)</li> For all other requests, the sorting key is the relevance regarding the passed search values.</li> </ul></p> <p>For actors: <ul> <li>actor_name: any string value denoting an actor name</li> <li>actor_type: -1 for all types, 0 for persons, 1 for organizations</li> <li>function: any string value denoting a function or profession</li> </ul> </p> <p>For arguments: <ul> <li>argument_type: 0 descriptive, 1 prescriptive, 2 opinion, 3 performative</li> </ul></p> <p>For texts: <ul> <li>text_title: any string value denoting a text title</li> <li>text_source: any string value denoting a source for a text</li> </ul></p> <p>For arguments and texts: <ul> <li>actor: any string value denoting an actor involved in a contribution (any type of involvement)</li> <li>author: any string value denoting an actor involved in a contribution as the author</li> <li>reporter: any string value denoting an actor involved in an argument as a reporter</li> </ul></p> <p>For folders: <ul> <li>folder_name: any string value denoting a folder name</li> </ul></p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Array",
            "size": "Object",
            "optional": false,
            "field": "response",
            "description": "<p>an array of WebdebContribution matching the given criteria (the response has no root element)</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "type": "Object",
            "optional": false,
            "field": "response",
            "description": "<p>a BadRequest object giving more details regarding the error, ie, the service as this service signature, the cause as a description of the cause of the error and DOCUMENTATION as a link to the present DOCUMENTATION (the response has no root element)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "curl https://webdeb.be/rest/bycriteria/actor=Trump+actor_name=Trump",
        "content": "[\n  {\n    \"id\": 768,\n    \"type\": \"actor\",\n    \"version\": 1491377168000,\n    \"name\": [\n      { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"en\" },\n      { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"fr\" }\n    ],\n    \"actorType\": \"person\",\n    \"crossReference\": \"https://fr.wikipedia.org/wiki/Donald_Trump\",\n    \"affiliations\": [\n      {\n        \"id\": 265,\n        \"type\": \"affiliation\",\n        \"version\": 1464270120000,\n        \"affiliationActor\": 769,\n        \"function\": [\n          { \"name\": \"businessperson\", \"lang\": \"en\" },\n        ],\n        \"endDate\": \"-1\"\n      },\n      {\n        \"id\": 266,\n        \"type\": \"affiliation\",\n        \"version\": 1486739277000,\n        \"affiliationActor\": 749,\n        \"function\": [\n          { \"name\": \"president\", \"lang\": \"en\" },\n          { \"name\": \"président\", \"lang\": \"fr\" }\n        ],\n        \"startDate\": \"01/2017\"\n      }\n    ],\n    \"gender\": \"M\",\n    \"birthdate\": \"14/06/1946\",\n    \"countries\": [ \"us\" ]\n  }\n  {\n    \"id\": 776,\n    \"type\": \"argument\",\n    \"version\": 1464271112000,\n    \"textId\": 774,\n    \"excerpt\": \"the doctor or any other person performing this illegal act upon a woman would be held legally responsible\",\n    \"argumentType\": {\n      \"argumentType\": 1,\n      \"typeNames\": { \"en\": \"Prescriptive\", \"fr\": \"Prescriptive\" },\n      \"argumentSubtype\": -1,\n      \"subtypeNames\": {},\n      \"argumentShade\": 10,\n      \"shadeNames\": { \"en\": \"It is necessary to\", \"fr\": \"Il faut\" },\n      \"argumentTiming\": 1,\n      \"timingNames\": { \"en\": \"Present\", \"fr\": \"Présent\" },\n      \"singular\": true\n    },\n    \"standardForm\": \"hold legally responsible the doctor or any other person performing an abortion\",\n    \"topics\": [ \"abortion\", \"perform\", \"hold responsible\", \"doctor\", \"person\" ],\n    \"actors\": [\n      { \"actor\": 775, \"isAuthor\": true, \"isReporter\": false}\n    ],\n    \"language\": \"en\"\n  },\n\n\t{ ... }\n\n]",
        "type": "curl"
      }
    ],
    "version": "0.0.2",
    "filename": "app/be/webdeb/application/rest/service/RESTAccessor.java",
    "groupTitle": "Services"
  },
  {
    "type": "get",
    "url": "/rest/byid/:id/:type",
    "title": "Retrieve contribution details",
    "name": "ById",
    "group": "Services",
    "description": "<p>this service is used to retrieve a particular contribution from the database by passing its unique ID and type</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Integer",
            "optional": false,
            "field": "id",
            "description": "<p>contribution unique id</p>"
          },
          {
            "group": "Parameter",
            "type": "Integer",
            "optional": false,
            "field": "type",
            "description": "<p>a type constant representing the contribution type, ie, <ul></p> <li>0 for Actor</li> <li>1 for Argument</li> <li>2 for Text</li> <li>3 for Argument Link</li> <li>4 for Affiliation</li> <li>5 for Folder</li> </ul>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "object",
            "description": "<p>a WebdebContribution specific JSON structure. Actual list of properties depends on the concrete data structure (see Structures part)</p>"
          }
        ]
      }
    },
    "error": {
      "fields": {
        "Error 4xx": [
          {
            "group": "Error 4xx",
            "type": "Object",
            "optional": false,
            "field": "badrequest",
            "description": "<p>BadRequest object giving more details regarding the error, ie, the service as this service signature, the cause as a description of the cause of the error and DOCUMENTATION as a link to the present DOCUMENTATION</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "curl https://webdeb.be/rest/byid/768/0",
        "content": "[\n  {\n    \"id\": 768,\n    \"type\": \"actor\",\n    \"version\": 1491377168000,\n    \"name\": [\n      { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"en\" },\n      { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"fr\" }\n    ],\n    \"actorType\": \"person\",\n    \"crossReference\": \"https://fr.wikipedia.org/wiki/Donald_Trump\",\n    \"affiliations\": [\n      {\n        \"id\": 265,\n        \"type\": \"affiliation\",\n        \"version\": 1464270120000,\n        \"affiliationActor\": 769,\n        \"function\": [\n          { \"name\": \"businessperson\", \"lang\": \"en\" },\n        ],\n        \"endDate\": \"-1\"\n      },\n      {\n        \"id\": 266,\n        \"type\": \"affiliation\",\n        \"version\": 1486739277000,\n        \"affiliationActor\": 749,\n        \"function\": [\n          { \"name\": \"president\", \"lang\": \"en\" },\n          { \"name\": \"président\", \"lang\": \"fr\" },\n        ],\n        \"startDate\": \"01/2017\"\n      }\n    ],\n    \"gender\": \"M\",\n    \"birthdate\": \"14/06/1946\",\n    \"countries\": [ \"us\" ]\n  }\n]",
        "type": "curl"
      }
    ],
    "version": "0.0.2",
    "filename": "app/be/webdeb/application/rest/service/RESTAccessor.java",
    "groupTitle": "Services"
  },
  {
    "type": "get",
    "url": "WebdebActorRole",
    "title": "Actor role",
    "name": "Actor_Role",
    "group": "Structures",
    "description": "<p>Actor's role in a contribution (always used in a concrete WebdebContribution).</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "actor",
            "description": "<p>the actor id</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "affiliation",
            "description": "<p>id of the affiliation of the above actor in the bound contribution</p>"
          },
          {
            "group": "Success 200",
            "type": "Boolean",
            "optional": false,
            "field": "isAuthor",
            "description": "<p>saying if this actor is the author of the linked contribution</p>"
          },
          {
            "group": "Success 200",
            "type": "Boolean",
            "optional": false,
            "field": "isReporter",
            "description": "<p>saying if this actor is the reporter of the linked contribution</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Actor's role in contribution",
        "content": "{ \"actor\": 775, \"isAuthor\": true, \"isReporter\": false}",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebActorRole.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebAffiliation",
    "title": "Actor affiliation",
    "name": "Affiliation",
    "group": "Structures",
    "description": "<p>An affiliation represents the linkage between an actor and, possibly, another with a function</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "affiliationActor",
            "description": "<p>the id of the affiliation actor, if any (null otherwise)</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebProfession[]",
            "optional": false,
            "field": "function",
            "description": "<p>array of profession object (i18n names) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "startDate",
            "description": "<p>a starting date in DD/MM/YYYY format with D and M optional (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "endDate",
            "description": "<p>an ending date in DD/MM/YYYY format with D and M optional (optional, &quot;-1&quot; denotes an &quot;ongoing&quot; affiliation)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Actor's affiliation",
        "content": "\"affiliations\": [\n  {\n    \"id\": 265,\n    \"type\": \"affiliation\",\n    \"version\": 1464270120000,\n    \"affiliationActor\": 769,\n    \"function\": [\n      { \"name\": \"businessperson\", \"lang\": \"en\", \"gender\": \"N\" },\n    ],\n    \"endDate\": \"-1\"\n  },\n  {\n    \"id\": 266,\n    \"type\": \"affiliation\",\n    \"version\": 1486739277000,\n    \"affiliationActor\": 749,\n    \"function\": [\n      { \"name\": \"president\", \"lang\": \"en\", \"gender\": \"M\" },\n      { \"name\": \"président\", \"lang\": \"fr\", \"gender\": \"M\" }\n    ],\n    \"startDate\": \"01/2017\"\n  }\n]",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebAffiliation.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebArgument",
    "title": "Argument",
    "name": "Argument",
    "group": "Structures",
    "description": "<p>An argument is an excerpt of a text of a certain type standardized in a specific form</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "textId",
            "description": "<p>the id of the text this argument belongs to</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "excerpt",
            "description": "<p>the excerpt from which this argument has been extracted</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "argumentType",
            "description": "<p>the argument type for this argument</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "argumentType.argumentType",
            "description": "<p>the type id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "argumentType.typeNames",
            "description": "<p>type name of the form { &quot;en&quot; : &quot;englishType&quot;, &quot;fr&quot; : &quot;TypeFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "argumentType.argumentTiming",
            "description": "<p>the timing id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "argumentType.timingNames",
            "description": "<p>timing name of the form { &quot;en&quot; : &quot;englishTiming&quot;, &quot;fr&quot; : &quot;TimingFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "argumentType.argumentShade",
            "description": "<p>the shade id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "argumentType.shadeNames",
            "description": "<p>shade name of the form { &quot;en&quot; : &quot;englishShade&quot;, &quot;fr&quot; : &quot;NuanceFrançaise&quot; }, names are empty for performative and opinion.</p>"
          },
          {
            "group": "Success 200",
            "type": "Boolean",
            "optional": false,
            "field": "argumentType.singular",
            "description": "<p>true if this shade applies to one actor, false otherwise</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "standardForm",
            "description": "<p>standardized form (without actors and shade)</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebSimpleFolder[]",
            "optional": false,
            "field": "folders",
            "description": "<p>list of folders linked with this argument</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer[]",
            "optional": false,
            "field": "argumentLinks",
            "description": "<p>the list of ids of linked arguments</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebRole[]",
            "optional": false,
            "field": "actors",
            "description": "<p>a list of actors with their roles involved in this argument</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebPlace[]",
            "optional": false,
            "field": "places",
            "description": "<p>a list of places involved in this argument</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>the iso-639-1 code of the language of this argument</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Argument (extracted from a text)",
        "content": "{\n  \"id\": 776,\n  \"type\": \"argument\",\n  \"version\": 1464271112000,\n  \"textId\": 774,\n  \"excerpt\": \"the doctor or any other person performing this illegal act upon a woman would be held legally responsible\",\n  \"argumentType\": {\n    \"argumentType\": 1,\n    \"typeNames\": { \"en\": \"Prescriptive\", \"fr\": \"Prescriptive\" },\n    \"argumentShade\": 10,\n    \"shadeNames\": { \"en\": \"It is necessary to\", \"fr\": \"Il faut\" },\n    \"argumentTiming\": 1,\n    \"timingNames\": { \"en\": \"Present\", \"fr\": \"Présent\" }\n  },\n  \"argumentPlace\": {\n    { \"lang\": \"fr\", \"name\": \"Namur\", \"subregion\": \"Province de Namur\", \"region\": \"Wallonie\", \"Country\": \"Belgique\", \"continent\": \"Europe\" },\n  },\n  \"standardForm\": \"hold legally responsible the doctor or any other person performing an abortion\",\n  \"folders\": {\n     \"id\": 100000,\n     \"folderType\": \"node\",\n     \"name\": [ { \"name\": \"actualité\", \"lang\": \"fr\" } ],\n  },\n  \"actors\": [ @WebdebActorRole ]\n  \"language\": \"en\"\n}",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebArgument.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebArgumentLink",
    "title": "Argument link",
    "name": "ArgumentLink",
    "group": "Structures",
    "description": "<p>Argument links are devoted to bind two arguments with a typed link</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "WebdebArgument",
            "optional": false,
            "field": "origin",
            "description": "<p>the argument being the origin of this link</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebArgument",
            "optional": false,
            "field": "destination",
            "description": "<p>the argument being the destination of this link</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "linkType",
            "description": "<p>the type of link binding these arguments</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "linkType.etype",
            "description": "<p>the string id of the link type</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "linkType.linkType",
            "description": "<p>the int id of the link type</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "linkType.linkShadeNames",
            "description": "<p>name of shade (nuance) of this link type of the form { &quot;en&quot; : &quot;english nuance&quot;, &quot;fr&quot; : &quot;nuance Française&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "linkType.linkTypeNames",
            "description": "<p>name of link type of the form { &quot;en&quot; : &quot;LinkName&quot;, &quot;fr&quot; : &quot;LienFrançais&quot; }</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Link between arguments",
        "content": "{\n  \"id\": 567,\n  \"type\": \"arglink\",\n  \"version\": 1457967693000,\n  \"origin\": { @WebdebArgumet },\n  \"destination\": { @WebdebArgumet }\n  \"linkType\": {\n    \"etype\": \"REJECTS\",\n    \"linkType\": 1,\n    \"linkShade\": 5,\n    \"linkShadeNames\": { \"en\": \"rejects\", \"fr\": \"rejette\" },\n    \"linkTypeNames\": { \"en\": \"Justification\", \"fr\": \"Justification\" }\n  }\n}",
        "type": "json"
      }
    ],
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/object/WebdebArgumentLink.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "BadRequest",
    "title": "Error message",
    "name": "BadRequest",
    "group": "Structures",
    "description": "<p>Bad requests may occur when any rest access contains invalid parameters</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "service",
            "description": "<p>name of service called</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "cause",
            "description": "<p>a message giving the root cause of the error</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "documentation",
            "description": "<p>url to the documentation page of this service</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Error message",
        "content": "{\n  \"service\":\"ById\",\n  \"cause\":\"unknown id given 8 or wrong type 2\",\n  \"documentation\":\"webdeb.be/assets/apidoc-rest/index.html\"\n}",
        "type": "json"
      }
    ],
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/service/BadRequest.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebContribution",
    "title": "Contribution (generic)",
    "name": "Contribution",
    "group": "Structures",
    "description": "<p>Common properties to all types of contributions, i.e., all other structures contain these fields too.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "id",
            "description": "<p>the contribution id</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "type",
            "description": "<p>the concrete type of this contribution (define remaining fields), either &quot;actor&quot;, &quot;argument&quot;, &quot;text&quot; or &quot;arglink&quot;</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "version",
            "description": "<p>a version number (long value in milliseconds from 1/01/1970)</p>"
          }
        ]
      }
    },
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/object/WebdebContribution.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebFolder",
    "title": "Folder",
    "name": "Folder",
    "group": "Structures",
    "description": "<p>A folder regroup texts and arguments on the same theme. They are lined one another to make a hierarchy.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "folderType",
            "description": "<p>the type of the folder (root for a folder without parent folder, node for normal folder and leaf for folder without children folder)</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "name",
            "description": "<p>and rewording name a structure containing all spellings for this folder's name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.name",
            "description": "<p>the spelling of the name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.lang",
            "description": "<p>the 2-char ISO code of the language corresponding to this spelling</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebSimpleFolder[]",
            "optional": false,
            "field": "parents",
            "description": "<p>the parents folder in the hierarchy</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebSimpleFolder[]",
            "optional": false,
            "field": "children",
            "description": "<p>the children folder in the hierarchy</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebPlace[]",
            "optional": false,
            "field": "places",
            "description": "<p>a list of places involved in this folder</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Folder",
        "content": "{\n  \"id\": 100000,\n  \"type\": \"folder\",\n  \"version\": 07465823022018,\n  \"folderType\": \"node\",\n  \"name\": [ { \"name\": \"actualité\", \"lang\": \"fr\" } ],\n  \"rewordingNames\": [ { \"name\": \"nouvelle\", \"lang\": \"fr\",  \"name\": \"nouveauté\", \"lang\": \"fr\"} ],\n  \"parents\": {\n     \"id\": 100000,\n     \"folderType\": \"node\",\n     \"name\": [ { \"name\": \"actualité\", \"lang\": \"fr\" } ],\n  },\n  \"children\": {\n     \"id\": 100000,\n     \"folderType\": \"node\",\n     \"name\": [ { \"name\": \"actualité\", \"lang\": \"fr\" } ],\n  },\n  \"argumentPlace\": {\n    { \"lang\": \"fr\", \"name\": \"Namur\", \"subregion\": \"Province de Namur\", \"region\": \"Wallonie\", \"Country\": \"Belgique\", \"continent\": \"Europe\" },\n  }\n}",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebFolder.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebOrganization",
    "title": "Organizational actor",
    "name": "Organization",
    "group": "Structures",
    "description": "<p>An organization is an actor that represents a moral entity, it contains all fields from a WebdebActor.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "sectors",
            "description": "<p>array of business sectors (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "sectors.id",
            "description": "<p>business sector id (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "creationDate",
            "description": "<p>the date at which this organization had been created in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "terminationDate",
            "description": "<p>the date at which this organization had been dissolved in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "sectors.names",
            "description": "<p>of the form { &quot;en&quot; : &quot;englishSector&quot;, &quot;fr&quot; : &quot;SecteurFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "legalStatus",
            "description": "<p>the legal status of this organization</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "legalStatus.id",
            "description": "<p>legal status id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "legalStatus.names",
            "description": "<p>of the form { &quot;en&quot; : &quot;englishLegal&quot;, &quot;fr&quot; : &quot;StatutFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "country",
            "description": "<p>where the head office is located in iso 3166-1 alpha-2 country code (optional)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Organizational / institutional actor",
        "content": "{\n  \"id\": 784,\n  \"type\": \"actor\",\n  \"version\": 1464351543000,\n  \"name\": [ { \"first\": null, \"last\": \"Unilever\", \"pseudo\": null, \"lang\": \"fr\" }],\n  \"actorType\": \"organization\",\n  \"crossReference\": \"https://fr.wikipedia.org/wiki/Unilever\",\n  \"sectors\": [\n    {\n      \"names\": {\n        \"en\": \"Retail trade\",\n        \"fr\": \"Commerce de détail\"\n      },\n      \"id\": 3\n    },\n    {\n      \"names\": {\n       \"en\": \"Other services to individuals or organisations\",\n       \"fr\": \"Autres services aux particuliers et organisations\"\n      },\n      \"id\": 19\n    }\n  ],\n  \"legalStatus\": { \"id\": 0, \"names\": { \"en\": \"Company\", \"fr\": \"Entreprise\" } },\n  \"territories\": \"uk\"\n}",
        "type": "json"
      }
    ],
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/object/WebdebOrganization.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebPerson",
    "title": "Individual actor",
    "name": "Person",
    "group": "Structures",
    "description": "<p>A person is an actor being an individual, it contains all fields from a WebdebActor.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "gender",
            "description": "<p>id (either F for female or M for male) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "birthdate",
            "description": "<p>the actor's date of birth in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "deathdate",
            "description": "<p>the actor's date of death in DAY/MONTH/YEAR format (DD/MM/YYYY with DD/MM optional) (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "residence",
            "description": "<p>country code of this person's country of residence (in iso 3166-1 alpha-2)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Individual person",
        "content": "{\n  \"id\": 768,\n  \"type\": \"actor\",\n  \"version\": 1491377168000,\n  \"name\": [\n    { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"en\" },\n    { \"first\": \"Donald\", \"last\": \"Trump\", \"pseudo\": null, \"lang\": \"fr\" }\n  ],\n  \"actorType\": \"person\",\n  \"crossReference\": \"https://fr.wikipedia.org/wiki/Donald_Trump\",\n  \"affiliations\": [\n    {\n      \"id\": 265,\n      \"type\": \"affiliation\",\n      \"version\": 1464270120000,\n      \"affiliationActor\": 769,\n      \"function\": [\n        { \"name\": \"businessperson\", \"lang\": \"en\" },\n      ],\n      \"endDate\": \"-1\"\n    },\n    {\n      \"id\": 266,\n      \"type\": \"affiliation\",\n      \"version\": 1486739277000,\n      \"affiliationActor\": 749,\n      \"function\": [\n        { \"name\": \"president\", \"lang\": \"en\", \"gender\": \"M\" },\n        { \"name\": \"président\", \"lang\": \"fr\", \"gender\": \"M\" }\n      ],\n      \"startDate\": \"01/2017\"\n    }\n  ],\n  \"gender\": \"M\",\n  \"birthdate\": \"14/06/1946\",\n  \"residence\": \"us\"\n}",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebPerson.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebPlace",
    "title": "Argument place",
    "name": "Place_names",
    "group": "Structures",
    "description": "<p>Argument's places.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>profession name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "lang",
            "description": "<p>iso-639-1 language code associated to this profession name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "subregion",
            "description": "<p>subregion name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "region",
            "description": "<p>region name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "country",
            "description": "<p>country name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "continent",
            "description": "<p>continent name</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Profession in affiliation structures",
        "content": "{ \"lang\": \"fr\", \"name\": \"Namur\", \"subregion\": \"Province de Namur\", \"region\": \"Wallonie\", \"country\": \"Belgique\", \"continent\": \"Europe\" }",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebPlace.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebProfession",
    "title": "Actor profession",
    "name": "Profession_names",
    "group": "Structures",
    "description": "<p>Actor's profession in a affiliations.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>profession name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "lang",
            "description": "<p>iso-639-1 language code associated to this profession name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "gender",
            "description": "<p>gender code associated to this profession name</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Profession in affiliation structures",
        "content": "{ \"name\": \"businessperson\", \"lang\": \"en\", \"gender\":\"N\" }",
        "type": "json"
      }
    ],
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/object/WebdebProfession.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebText",
    "title": "Textual contribution",
    "name": "Text",
    "group": "Structures",
    "description": "<p>Texts are contributions that usually contain arguments and are linked to actors</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>the text title</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebRole[]",
            "optional": false,
            "field": "actors",
            "description": "<p>array of WebdebRole with all actors bound to this text</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>iso-639-1 of this text language</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "publicationDate",
            "description": "<p>a publication date (DD/MM/YYYY format with D and M optional) or -1 for unknown</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "textType",
            "description": "<p>a text type</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "textType.type",
            "description": "<p>text type id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "textType.names",
            "description": "<p>text type name of the form { &quot;en&quot; : &quot;EnglishType&quot;, &quot;fr&quot; :&quot;TypeFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "textOrigin",
            "description": "<p>a text origin</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "textOrigin.origin",
            "description": "<p>a text origin id</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "textOrigin.names",
            "description": "<p>text origin name of the form { &quot;en&quot; : &quot;EnglishType&quot;, &quot;fr&quot; :&quot;TypeFrançais&quot; }</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "textOrigin.otherName",
            "description": "<p>a text origin name, in case the origin in &quot;other&quot; (may be null)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source",
            "description": "<p>the source name of this text, ie, the media where it comes from (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebSimpleFolder[]",
            "optional": false,
            "field": "folders",
            "description": "<p>list of folders linked with this text</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "content",
            "description": "<p>the text content it the text is not under copyright (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer[]",
            "optional": false,
            "field": "arguments",
            "description": "<p>the list of argument ids originating from this text (may be null)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Text in webdeb",
        "content": "{\n  \"id\": 614,\n  \"type\": \"text\",\n  \"version\": 1461850237000,\n  \"title\": \"Aung San Suu Kyi : « Le prix Nobel de la paix a ouvert une porte dans mon cœur »\",\n  \"actors\": [ @WebdebActorRole ],\n  \"language\": \"fr\",\n  \"publicationDate\": \"19/06/2012\",\n  \"textType\": {\n    \"type\": 3,\n    \"names\": {\n      \"en\": \"Journalistic (news article, press report,...)\",\n      \"fr\": \"Journalistique (article de presse, reportage,...)\"\n    }\n  },\n  \"textOrigin\": { \"origin\": 0, \"names\": { \"en\": \"Written document\", \"fr\": \"Document écrit\" } },\n  \"source\": \"La Croix\",\n  \"arguments\": [ 615, 616, 617, 618, 619, 620 ],\n  \"folders\": {\n     \"id\": 100000,\n     \"folderType\": \"node\",\n     \"name\": [ { \"name\": \"actualité\", \"lang\": \"fr\" } ],\n  },\n  \"argumentPlace\": {\n    { \"lang\": \"fr\", \"name\": \"Namur\", \"subregion\": \"Province de Namur\", \"region\": \"Wallonie\", \"Country\": \"Belgique\", \"continent\": \"Europe\" },\n  }\n}\n}",
        "type": "json"
      }
    ],
    "version": "0.0.1",
    "filename": "app/be/webdeb/application/rest/object/WebdebText.java",
    "groupTitle": "Structures"
  },
  {
    "type": "get",
    "url": "WebdebActor",
    "title": "Actor (generic)",
    "name": "WebdebObjects",
    "group": "Structures",
    "description": "<p>An actor is a generic object that contains common data to all types of actors.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "name",
            "description": "<p>a structure containing all spellings for this actor's name</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.first",
            "description": "<p>the person's first name or the organization's acronym (may be null)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.last",
            "description": "<p>the person's last name or the organization's name (may be null)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.pseudo",
            "description": "<p>the person's pseudonym (always null for organizations, may be null for persons)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name.lang",
            "description": "<p>the 2-char ISO code of the language corresponding to this spelling</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "oldname",
            "description": "<p>a structure of the same type as name, containing all spellings for this actor's previous or alike names (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "actorType",
            "description": "<p>either &quot;unknown&quot;, &quot;person&quot; or &quot;organization&quot;</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "crossReference",
            "description": "<p>the url of a personal webpage, if any (optional)</p>"
          },
          {
            "group": "Success 200",
            "type": "WebdebAffiliation[]",
            "optional": false,
            "field": "affiliations",
            "description": "<p>an array of WebdebAffiliation objects (optional)</p>"
          }
        ]
      }
    },
    "examples": [
      {
        "title": "Actor in webdeb",
        "content": "{\n  \"id\": 204,\n  \"type\": \"actor\",\n  \"version\": 1439366598000,\n  \"name\": [ { \"first\": null, \"last\": \"Sébastien Dupont\", \"pseudo\": null, \"lang\": \"fr\" } ],\n  \"actorType\": \"unknown\",\n  \"affiliations\": [\n    {\n      \"id\": 87,\n      \"type\": \"affiliation\",\n      \"version\": 1440075236000,\n      \"function\": [\n        { \"name\": \"psychologist\", \"lang\": \"en\", \"gender\": \"N\" },\n        { \"name\": \"psychologue\", \"lang\": \"fr\", \"gender\": \"N\" }\n      ]\n    }\n  ]\n}",
        "type": "json"
      }
    ],
    "version": "0.0.3",
    "filename": "app/be/webdeb/application/rest/object/WebdebActor.java",
    "groupTitle": "Structures"
  }
] });
