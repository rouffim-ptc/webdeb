define({ "api": [
  {
    "type": "post",
    "url": "/affirmations/classify_t1/",
    "title": "Speech act classifier (typology 1).",
    "name": "PostAffirmationsClassify",
    "group": "Affirmations",
    "description": "<p>Classify a text according to its speech act type. Types are as follows: 0=Descriptive, 1=Prescriptive, 2=Performative, 3=Appreciative.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>the language of the text (currently either 'en' or 'fr').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "text",
            "description": "<p>the text to classify.</p>"
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
            "field": "result",
            "description": "<p>A dictionary of the form { &quot;text&quot;: text, &quot;t1&quot;: [[cat1, score1], [cat2, score2], [cat3, score3], [cat4, score4]], &quot;method&quot;: 1 }. The &quot;text&quot; field contains the input text string; the &quot;t1&quot; field holds an array of (2-item) arrays in which the &quot;cat&quot; fields are the categories chosen by the classifier (string values) and each &quot;score&quot; is a percentage (i.e. their sum is 100) expressed as a floating point value. The &quot;method&quot; field, used for development purposes, indicates which classification method was used (0 = machine learning, 1 = symbolic).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "\n{\"text\": \"Nous devons réfléchir à la manière de remédier aux torts que nous avons causés.\", \"t1\": [[1, 85.4], [0, 8.8], [3, 3.4], [2, 2.5]], \"method\": 1}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\ncurl -d '{ \"language\": \"fr\", \"text\": \"Nous devons réfléchir à la manière de remédier aux torts que nous avons causés.\" }' https://nlp.webdeb.be/wdtal2/v0/affirmations/classify_t1/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Affirmations"
  },
  {
    "type": "post",
    "url": "/affirmations/extract_topics_tt/",
    "title": "Baseline topic extractor for affirmations.",
    "name": "PostAffirmationsExtractTopicsTT",
    "group": "Affirmations",
    "description": "<p>This method extracts keyword lemmas (&quot;topics&quot;) from a given affirmation for a specified language (currently English, French or Dutch). It uses the TreeTagger for POS-tagging and lemmatization. Accepted parts-of-speech are specified in external paramter files.</p> <p>Words in the text are filtered according to a set of pre-determined part-of-speech tags. A parameter file specifies, for a given langauge, which parts-of-speech are to be accepted as keywords. Typically, we wish to retain only words with semantic content (nouns, verbs, adjectives, etc.) and discard function words (determiners, prepositions, conjunctions, pronouns, etc.). Each parameter file contains the full tagset for the given language.<br/></p> <p>Each file contains 2 or 3 tab-separated columns with the following format:<br/></p> <p><strong>TAG</strong> [TAB] <strong>STATUS</strong> [TAB] <strong>DESCRIPTION</strong><br/></p> <p><strong>TAG</strong>: the first column specifies the value of the part-of-speech tag, e.g. ADJ, CC, NOUN.<br/> <strong>STATUS</strong>: specifies whether the tag in question is to be included in the output (ACCEPT) or excluded (REJECT).<br/> <strong>DESCRIPTION</strong>: optional column that provides a descrption and examples of words that belong to this grammatical category.<br/></p> <p>The accepted tagset may be modified by changing the value in the second column (ACCEPT or REJECT).<br/></p> <p>The following parameter files are in the resources directory:<br/></p> <p>tt_tagset_en.txt: accepted tagset for English.<br/> tt_tagset_fr.txt: accepted tagset for French.<br/> tt_tagset_nl.txt: accepted tagset for Dutch.<br/></p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>The language of the input text ('en', 'fr' or 'nl').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "text",
            "description": "<p>The text of the affirmation to process.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "netypes",
            "description": "<p>(Optional) Output types for named entities (true or false, default = false).</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String[]",
            "optional": false,
            "field": "topics",
            "description": "<p>The list of keyword lemmas (&quot;topics&quot;) extracted from the input text (with a list of named entity types, or empty list for regular topics).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "\n{\"topics\": [\"Mossack Fonseca\": [\"PERS\"], \"help\": [], \"New York\": [\"PLACE\"], \"art\": [], \"gallery\": [], \"defend\": [], \"claim\": [], \"Nazi-looted\": [], \"artwork\": [], \"apparent\": [], \"original\": [], \"owner\\u2019s\": [], \"descendant\": [], \"launch\": [], \"legal\": [], \"battle\": [], \"return\": [], \"Panama\": [\"PLACE\"], \"Papers\": [], \"reveal\": []]}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\ncurl -d '{ \"language\": \"en\", \"netypes\": \"true\", \"text\": \"Mossack Fonseca helped a New York art gallery defend itself over a claim about a Nazi-looted artwork after the apparent original owner’s descendant launched a legal battle for its return, the Panama Papers reveal.\"}' https://nlp.webdeb.be/wdtal2/v0/affirmations/extract_topics_tt/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Affirmations"
  },
  {
    "type": "post",
    "url": "/affirmations/similarity/",
    "title": "WDTAL similarity between arguments.",
    "name": "PostAffirmationsSimilarity",
    "group": "Affirmations",
    "description": "<p>Run the WDTAL similarity scripts and return a table of dictionaries containing pairs of sentences and their similarity score.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "server",
            "description": "<p>The server to retrieve arguments (affirmations) from ('qa' or 'prod').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "threshold",
            "description": "<p>The similarity threshold to use (decimal value between 0 and 1.0).</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "result",
            "description": "<p>A list of dictionaries of the form {&quot;from&quot;:id, &quot;to&quot;:id, &quot;score&quot;:value} where sentence ids (id) are integer values and the score value (value) is a floating point value.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "\n[{\"to\": 377, \"score\": 0.91005291005291, \"from\": 376}, {\"to\": 66, \"score\": 0.88575837490523, \"from\": 65}, {\"to\": 287, \"score\": 0.835410196624969, \"from\": 286}, {\"to\": 644, \"score\": 0.816227766016838, \"from\": 641}, ... ]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\ncurl -d '{ \"server\": \"prod\", \"threshold\": \"0.6\" }' https://nlp.webdeb.be/wdtal2/v0/affirmations/similarity/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Affirmations"
  },
  {
    "type": "post",
    "url": "/affirmations/update_topics/",
    "title": "Store new topics validated by the user.",
    "name": "PostAffirmationsUpdateTopics",
    "group": "Affirmations",
    "description": "<p>This method updates the list of already existing topics with new ones as they are validated by the user.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>The language of the argument for which topics are being added ('en', 'fr' or 'nl').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "topics",
            "description": "<p>The list of topics to save.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "None",
            "optional": false,
            "field": "String",
            "description": "<p>The name of the file that contains the updated list of topics.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "\n{\"filename\": \"resources/wd_validated_topics_en.txt\"}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\ncurl -d '{ \"language\": \"en\", \"topics\": [\"politics\", \"brexit\", \"uk\", \"european union\"] }' https://nlp.webdeb.be/wdtal2/v0/affirmations/update_topics/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Affirmations"
  },
  {
    "type": "post",
    "url": "/arguments/extract/",
    "title": "Automatically extract arguments from a text.",
    "name": "PostArgumentsExtract",
    "group": "ArgumentExtractor",
    "description": "<p>This method processes the input text to extract a list of WebDeb arguments.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "textid",
            "description": "<p>The id of the text to process.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>A list of new arguments and all their releveant attributes as a list of JSON dictionary structures. Attributes are described below.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "{ \"\": \"\", ..., \"\":\"\" }",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"textid\": 7350 }' https://nlp.webdeb.be/wdtal2/v0/arguments/extract/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "ArgumentExtractor"
  },
  {
    "type": "post",
    "url": "/arguments/import_split/",
    "title": "Retrieve a list of tweets that have been converted into new arguments.",
    "name": "PostArgumentSplitterImport",
    "group": "Arguments",
    "description": "<p>This method imports a list of simple arguments that have been extracted from a complex one. These arguments require manual validation.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>the language of the arguments to split (currently only works for French, 'fr').</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "number",
            "description": "<p>the number of arguments to retrieve (if omitted, all available arguments).</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>A list of new arguments and all their releveant attributes as a list of JSON dictionary structures. Attributes are described below.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "{ \"\": \"\", ..., \"\":\"\" }",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"language\": \"fr\", \"number\": 100 }' https://nlp.webdeb.be/wdtal2/v0/arguments/import_split/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Arguments"
  },
  {
    "type": "post",
    "url": "/data/extract/",
    "title": "Extract information from the specified URL.",
    "name": "PostDataExtract",
    "group": "Data",
    "description": "<p>This method extracts all required information from the specified URL of a news article.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "url",
            "description": "<p>The URL of the article to process.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String[]",
            "optional": false,
            "field": "author",
            "description": "<p>The list of authors of the article.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "date",
            "description": "<p>The article's publication date.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "img",
            "description": "<p>The main image for the article.</p>"
          },
          {
            "group": "Success 200",
            "type": "String[]",
            "optional": false,
            "field": "meta_keywords",
            "description": "<p>The list of metadata keywords (lowercased) extracted from the HTML header.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>The ISO code for the language in which the text is written.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source_name",
            "description": "<p>The name of the source of the article.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source_country",
            "description": "<p>The two-letter ISO code of the country in which the source is based.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "text",
            "description": "<p>The main text of the article.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "title",
            "description": "<p>The title of the article.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "{\"meta_keywords\": [\"turkey\", \"recep tayyip erdo\\u011fan\", \"refugees\", \"migration\", \"world humanitarian summit\", \"world news\", \"global development\", \"uk news\"], \"language\": \"en\", \"author\": [\"Patrick Kingsley\"], \"text\": \"The Turkish president has asked Europe to welcome more of the 3 million refugees currently living in Turkey...\\u201cWe all have to do our fair share.\\u201d\", \"image\": \"https://i.guim.co.uk/img/media/3f3f86b2db02a6ffcbc1460079dcfb042de40b77/0_53_2735_1641/2735.jpg?w=1200&q=55&auto=format&usm=12&fit=max&s=5323a201e9bb5f59238d78b89ff8b491\", \"title\": \"Erdo\\u011fan calls on Europe to take in more Syrian refugees\", \"date\": \"2016-05-23 00:00:00\"}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"url\": \"http://www.theguardian.com/world/2016/may/23/erdogan-calls-on-europe-to-take-in-more-syrian-refugees\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Data"
  },
  {
    "type": "post",
    "url": "/data/extract_dbpedia/",
    "title": "Extract information for actors from DBPedia.",
    "name": "PostDataExtractDBPedia",
    "group": "Data",
    "description": "<p>This method extracts as much information as possible for a given actor via a SPARQL query of DBPedia. Actors may be of one of two types: <strong>person</strong> or <strong>organization</strong>.<br/> <br/> The extracted attributes for the type <strong>person</strong> are as follows:</p> <p><strong>Name</strong> - the name of the person in each of the WebDeb project's languages ('en', 'fr' or 'nl').</p> <p><strong>Date of birth</strong> - the person's date of birth.</p> <p><strong>Date of death</strong> - the person's date of death, if applicable.</p> <p><strong>Image</strong> - an image of the person.</p> <p><strong>Wikipedia URLs</strong> - a list of URLs for the person's Wikipedia page for each language (if pages exist). In the case of a query via URL, this URL is not included in the list.<br/> <br/> The extracted attributes for the type <strong>organization</strong> are as follows:</p> <p><strong>Name</strong> - the name of the organization in each of the WebDeb project's languages ('en', 'fr' or 'nl').</p> <p><strong>Date of creation</strong> - the date the organization was created.</p> <p><strong>Date of dissolution</strong> - the date the organization was disestablished.</p> <p><strong>Image</strong> - an image representing the organization.</p> <p><strong>Wikipedia URLs</strong> - a list of URLs for the organization's Wikipedia page for each language (if pages exist). In the case of a query via URL, this URL is not included in the list.<br/> <br/></p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "actor_type",
            "description": "<p>The type of actor being dealt with ('person' or 'organization').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>The language of the query ('en', 'fr' or 'nl').</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "query",
            "description": "<p>The query expression, i.e. the name of the person or organization.</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "url",
            "description": "<p>The URL of the Wikipedia page of the person or organization.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dob",
            "description": "<p>(person) The person's date of birth.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dod",
            "description": "<p>(person) The person's date of death, if applicable.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "img",
            "description": "<p>The URL for an image representing the person or organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>A mapping of language:name pairs with the person or organization's name for each language.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "aff",
            "description": "<p>(organization) The owner (affiliation) of the organization (if any).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "date_end",
            "description": "<p>(organization) The disestablishment date of the organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "date_start",
            "description": "<p>(organization) The foundation date of the organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "Map",
            "optional": false,
            "field": "wiki_url",
            "description": "<p>A mapping of language:url pairs with the URL of the person or organization's Wikipedia page for each language.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "Person: {\"dob\": \"1452-04-15\", \"img\": \"http://commons.wikimedia.org/wiki/Special:FilePath/LEONARDO.JPG?width=300\", \"wiki_url\": {\"fr\": \"https://fr.wikipedia.org/wiki/L\\u00e9onard_de_Vinci\", \"en\": \"https://en.wikipedia.org/wiki/Leonardo_da_Vinci\", \"nl\": \"https://nl.wikipedia.org/wiki/Leonardo_da_Vinci\"}, \"name\": {\"fr\": \"L\\u00e9onard de Vinci\", \"en\": \"Leonardo da Vinci\", \"nl\": \"Leonardo da Vinci\"}, \"dod\": \"1519-05-02\"}\n\nOrganization: {\"date_end\": \"1991-12-26\", \"date_start\": \"1922-12-30\", \"wiki_url\": {\"fr\": \"https://fr.wikipedia.org/wiki/Union_des_r\\u00e9publiques_socialistes_sovi\\u00e9tiques\", \"nl\": \"https://nl.wikipedia.org/wiki/Sovjet-Unie\"}, \"name\": {\"fr\": \"Union des r\\u00e9publiques socialistes sovi\\u00e9tiques\", \"en\": \"Soviet Union\", \"nl\": \"Sovjet-Unie\"}, \"img\": \"http://commons.wikimedia.org/wiki/Special:FilePath/Flag_of_the_Soviet_Union.svg?width=300\"}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\nPerson:\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"person\", \"query\": \"Leonardo da Vinci\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_dbpedia/\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"person\", \"url\": \"https://en.wikipedia.org/wiki/Leonardo_da_Vinci\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_dbpedia/\n\nOrganization:\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"organization\", \"query\": \"Soviet Union\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_dbpedia/\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"organization\", \"url\": \"https://en.wikipedia.org/wiki/Soviet_Union\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_dbpedia/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Data"
  },
  {
    "type": "post",
    "url": "/data/extract_pdf/",
    "title": "Extract the text from a PDF file specified via URL.",
    "name": "PostDataExtractPDF",
    "group": "Data",
    "description": "<p>This method extracts the text from a PDF file specified in a URL.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "url",
            "description": "<p>The URL of the PDF file to process.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "interface_language",
            "description": "<p>The language of the WebDeb interface.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "text",
            "description": "<p>The text extracted from the PDF file.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>The ISO code for the language (as automatically detected).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "{\"language\": \"fr\", \"text\": \"ATTENTION: Ce texte a été extrait par un processus automatique. Avant de le sauvegarder, veuillez vous assurer que le contenu correspond bien au document d'origine. À défaut, merci de charger le fichier PDF.\\n\\n\\nPARLEMENT WALLON\\n\\nSESSION 2016-2017\\n\\nCOMPTE RENDU INTÉGRAL\\n\\nSéance plénière*\\n\\nJeudi 26 janvier 2017...La séance est levée.Le Parlement s'ajourne jusqu'au mercredi 1er février 2017.\"}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"url\": \"http://nautilus.parlement-wallon.be/Archives/2016_2017/CRI/cri12.pdf\", \"interface_language\": \"fr\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_pdf/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Data"
  },
  {
    "type": "post",
    "url": "/data/extract_wikidata/",
    "title": "Extract information for actors from WikiData.",
    "name": "PostDataExtractWikiData",
    "group": "Data",
    "description": "<p>This method extracts as much information as possible for a given actor via a SPARQL query of WikiData. Actors may be of one of two types: <strong>person</strong> or <strong>organization</strong>.<br/> <br/> The extracted attributes for the type <strong>person</strong> are as follows:</p> <p><strong>Aliases</strong> - alternative names that the person is known by.</p> <p><strong>Birth name</strong> - the person's full name at birth.</p> <p><strong>Given name</strong> - the person's given (first) name(s).</p> <p><strong>Pseudonym</strong> - the person's pseudonym(s), if any.</p> <p><strong>Nationality</strong> - the countries of which the person is a citizen.</p> <p><strong>Date of birth</strong> - the person's date of birth.</p> <p><strong>Date of death</strong> - the person's date of death, if applicable.</p> <p><strong>Gender</strong> - the person's gender.</p> <p><strong>Image</strong> - an image of the person.</p> <p><strong>Affiliations</strong> - the various organizations the person is or has been affiliated to.</p> <p><strong>Wikipedia URLs</strong> - a list of URLs for the person's Wikipedia page for each language (if pages exist). <br/> <br/> The extracted attributes for the type <strong>organization</strong> are as follows:</p> <p><strong>Name</strong> - the name of the organization.</p> <p><strong>Country</strong> - the country the organisation is based in.</p> <p><strong>Date of creation</strong> - the date the organization was created.</p> <p><strong>Date of dissolution</strong> - the date the organization was disestablished.</p> <p><strong>Legal status</strong> - the legal status code/type of the organization.</p> <p><strong>Image</strong> - an image representing the organization.</p> <p><strong>Wikipedia URLs</strong> - a list of URLs for the organization's Wikipedia page for each language (if pages exist). In the case of a query via URL, this URL is not included in the list.<br/> <br/></p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "actor_type",
            "description": "<p>The type of actor being dealt with ('person' or 'organization').</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>The language of the query ('en', 'fr', 'nl', etc.).</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "query",
            "description": "<p>The query parameters, i.e. the first name (first_name), last name (last_name) or pseudonym (pseudonym) of a person or the name of the organization (org_name).</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "url",
            "description": "<p>The URL of the Wikipedia page of the person or organization.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dob",
            "description": "<p>(person) The person's date of birth.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "dod",
            "description": "<p>(person) The person's date of death, if applicable.</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "birth_name",
            "description": "<p>(person) The full name of a person at birth, if different from their current, generally used name, as a dictionary of the form: { 'language': langauge_code, 'value': name_value }.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "family_name",
            "description": "<p>(person) The person's surname or last name.</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "given_name",
            "description": "<p>(person) The person's language-specific first name or another given name as a dictionary of the form: { language_code_1: name, ..., language_code_n: name }.</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "pseudonym",
            "description": "<p>(person) A list of string values for the alias(es) used by someone or by which this person is universally known.</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "aliases",
            "description": "<p>(person) Language-specific alternative names for the person, as a dictionary of the form: { language_code_1: [ alias_1...alias_n ], ..., language_code_n: [ alias_1...alias_n ] }.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "gender",
            "description": "<p>(person) The person's gender code (M or F).</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "nationality",
            "description": "<p>(person) A list of string values for country codes of the countries for which the person is a citizen.</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "affiliations",
            "description": "<p>(person) A list of organizations the person is or has been affiliated to. The list contains a dictionary for each organization of the form { 'organization': { language_code_1: org_name, ..., language_code_n: org_name }, 'function': { language_code_1: function, ..., language_code_n: function }, 'start_date': date, 'end_date': date } ('organization' is the only obligatory attribute).</p>"
          },
          {
            "group": "Success 200",
            "type": "Obhject",
            "optional": false,
            "field": "org_name",
            "description": "<p>(organization) Language-specific names for the organization, as a dictionary of the form: { language_code_1: name, ..., language_code_n: name }.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "date_end",
            "description": "<p>(organization) The disestablishment date of the organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "date_start",
            "description": "<p>(organization) The foundation date of the organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "int",
            "optional": false,
            "field": "legal_status",
            "description": "<p>(organization) The WebDeb integer code for the type of organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "country",
            "description": "<p>(organization) A list of string values for country codes of the countries in which the organization is based (has its headquarters).</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "sector",
            "description": "<p>(organization) A list of WebDeb integer codes (int) of the sectors in which the organization is active.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "image",
            "description": "<p>The URL for an image representing the person or organization.</p>"
          },
          {
            "group": "Success 200",
            "type": "Object",
            "optional": false,
            "field": "wiki_url",
            "description": "<p>A mapping of language:url pairs with the URL of the person or organization's Wikipedia page for each language. This is a dictionary of the form { language_code_1: url, ..., language_code_n: url }.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "Person: {\"family_name\": { ..., \"en\": \"Clinton\", ..., \"fr\": \"Clinton\", ... }, \"wiki_url\": { ..., \"en\": \"https://en.wikipedia.org/wiki/Hillary_Clinton\", ... , \"fr\": \"https://fr.wikipedia.org/wiki/Hillary_Clinton\", ..., }, \"dob\": \"+1947-10-26T00:00:00Z\", \"gender\": \"F\", \"image\": \"https://commons.wikimedia.org/wiki/File:HillaryPA.jpg\", \"affiliations\": [ ..., { \"end_date\": \"+1981-01-01T00:00:00Z\", \"function\": { ..., \"en\": \"board of directors\", ..., \"fr\": \"conseil d'administration\", ... }, \"organization\": { \"en\": \"Legal Services Corporation\", ..., \"fr\": \"Legal Services Corporation\", ... }, \"start_date\": \"+1978-01-01T00:00:00Z\" }, {\"organization\": {\"en\": \"Republican Conference of the United States House of Representatives\", ... }}], \"given_name\": { ..., \"en\": \"Hillary Diane\", ..., \"fr\": \"Hillary Diane\", ... }, \"nationality\": [\"US\"], \"birth_name\": {\"value\": \"Hillary Diane Rodham\", \"language\": \"en\"}, \"aliases\": { ..., \"en\": [\"Hillary Diane Rodham\", \"Hillary Rodham\", \"Hillary Diane Rodham Clinton\", \"Clinton, Hillary\", \"Hillary\", \"Hillary Rodham Clinton\"], ... , \"fr\": [\"Hillary Diane Rodham\", \"Hillary Rodham Clinton\"], ... ]}}\n\nOrganization: { \"country\": [\"FR\",\"CH\"], \"image\": \"https://commons.wikimedia.org/wiki/File:MSF%20HQ.jpg\", \"legal_status\": 4, \"org_name\": { ..., \"en\": \"M\\u00e9decins Sans Fronti\\u00e8res\", \"fr\": \"M\\u00e9decins sans fronti\\u00e8res\", ... }, \"start_date\": \"+1971-12-21T00:00:00Z\", \"wiki_url\": { ..., \"en\": \"https://en.wikipedia.org/wiki/M%C3%A9decins_Sans_Fronti%C3%A8res\", ..., \"fr\": \"https://fr.wikipedia.org/wiki/M%C3%A9decins_sans_fronti%C3%A8res\", ... } }",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\nPerson:\n\ncurl -d '{\"language\":\"en\",\"query\":{\"first_name\":\"Laura\",\"last_name\":\"Palmer\"},\"actor_type\":\"person\"}' https://nlp.webdeb.be/wdtal2/v0/data/extract_wikidata/\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"person\", \"url\": \"https://en.wikipedia.org/wiki/Leonardo_da_Vinci\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_wikidata/\n\nOrganization:\n\ncurl -d '{\"language\":\"en\",\"query\":{\"org_name\":\"Soviet Union\"},\"actor_type\":\"org\"}' https://nlp.webdeb.be/wdtal2/v0/data/extract_wikidata/\n\ncurl -d '{ \"language\": \"en\", \"actor_type\": \"organization\", \"url\": \"https://en.wikipedia.org/wiki/Soviet_Union\" }' https://nlp.webdeb.be/wdtal2/v0/data/extract_wikidata/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Data"
  },
  {
    "type": "post",
    "url": "/language/detect/",
    "title": "Detect the language of a text string.",
    "name": "PostLanguageDetect",
    "group": "Language",
    "description": "<p>This method detects the language of a text string and returns an integer code for that language.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "text",
            "description": "<p>The text for which the language must be determined.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>The ISO code for the language of the text.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "{\"language\": \"fr\"}",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"text\": \"Ce texte est écrit dans la langue de Molière.\" }' https://nlp.webdeb.be/wdtal2/v0/language/detect/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Language"
  },
  {
    "type": "post",
    "url": "/rss/delete/",
    "title": "Delete all files in the delete directory (these files should have already been successfully imported).",
    "name": "PostRSSDelete",
    "group": "RSS_Harvester",
    "description": "<p>This method deletes all files in the delete directory.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "files",
            "description": "<p>The list of files that were deleted.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[\"wd_rss_20161205170752.json\", \"wd_rss_20161205171007.json\", \"wd_test.json\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl https://nlp.webdeb.be/wdtal2/v0/rss/delete/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/get/",
    "title": "Get the contents of a file containing articles produced by the RSS harvester.",
    "name": "PostRSSGet",
    "group": "RSS_Harvester",
    "description": "<p>This method retrieves a list of harvested articles in JSON format.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "filename",
            "description": "<p>the filename to retrieve content from.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "files",
            "description": "<p>The list of retrieved articles.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[{\"meta_keywords\": [\"terre\"], \"source_name\": \"Le Vif\", \"language\": \"fr\", \"title\": \"La v\\u00e9ritable histoire de la seule personne connue touch\\u00e9e par une m\\u00e9t\\u00e9orite\", \"url\": \"http://www.levif.be/actualite/sciences/la-veritable-histoire-de-la-seule-personne-connue-touchee-par-une-meteorite/article-normal-581623.html\", \"text\": \"Sur la Toile, les conspirationnistes sont persuad\\u00e9s que le mois de d\\u00e9cembre est le dernier mois que vivra l'Humanit\\u00e9 \\u00e0 cause de l'arriv\\u00e9e imminente de Nibiru, aussi connue sous le nom de Planet X...\", \"image\": \"http://www.levif.be/medias/7235/3704503.jpg\", \"date_parsed\": \"2016-12-05\", \"source_country\": \"be\", \"text_visibility\": 2, \"authors\": [\"Le Vif\"], \"date\": \"2016-12-05\", \"topics\": [\"histoire\", \"personne\", \"touchage\", \"m\\u00e9t\\u00e9orite\", \"sciences\"], \"text_type\": 3}, ..., {\"meta_keywords\": [\"heungjin ryu\"], \"source_name\": \"Le Vif\", \"language\": \"fr\", \"title\": \"Les bonobos deviennent aussi presbytes en vieillissant\", \"url\": \"http://www.levif.be/actualite/sciences/les-bonobos-deviennent-aussi-presbytes-en-vieillissant/article-normal-571079.html\", \"text\": \"\\\"Nous avons observ\\u00e9 que les bonobos montrent des sympt\\u00f4mes de presbytie vers l'\\u00e2ge de 40 ans\\\", explique Heungjin Ryu, de l'Institut de recherche sur les primates \\u00e0 l'Universit\\u00e9 de Kyoto au Japon, le principal auteur de ces travaux publi\\u00e9s mardi dans la revue scientifique Current Biology...\", \"image\": \"http://www.levif.be/medias/4425/2266087.jpg\", \"date_parsed\": \"2016-11-09\", \"source_country\": \"be\", \"text_visibility\": 2, \"authors\": [\"Le Vif\"], \"date\": \"2016-11-09\", \"topics\": [\"bonobos\", \"devenir\", \"vieillissage\", \"sciences\"], \"text_type\": 3}]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"filename\": \"wd_rss_20161115164123.json\" }' https://nlp.webdeb.be/wdtal2/v0/rss/get/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/harvest/",
    "title": "Harvest the latest articles from RSS feeds.",
    "name": "PostRSSHarvest",
    "group": "RSS_Harvester",
    "description": "<p>This method runs the RSS harvesting module that retrieves new articles.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "None",
            "optional": false,
            "field": "-",
            "description": "<p>No return value.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "null",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl https://nlp.webdeb.be/wdtal2/v0/rss/harvest/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/list/",
    "title": "List the files available for import (harvest directory) or to be deleted (delete directory).",
    "name": "PostRSSList",
    "group": "RSS_Harvester",
    "description": "<p>This method lists the files available import (harvest directory) or to be deleted (delete directory).</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "directory",
            "description": "<p>the name of the directory to list ('harvest' or 'delete').</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "files",
            "description": "<p>The list of files available for import (harvest directory) or to be deleted (delete directory).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[\"wd_rss_20161205150037.json\", \"wd_rss_20161205150045.json\", \"wd_rss_20161205150112.json\", \"wd_rss_20161205150259.json\", \"wd_rss_20161205150437.json\", \"wd_rss_20161205150444.json\", \"wd_rss_20161205150510.json\", \"wd_rss_20161205150845.json\", \"wd_rss_20161205150910.json\", \"wd_rss_20161205155710.json\", \"wd_rss_20161205160432.json\", \"wd_rss_20161205160851.json\", \"wd_rss_20161205170807.json\", \"wd_rss_20161205170832.json\", \"wd_rss_20161205170945.json\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"directory\": \"harvest\" }' https://nlp.webdeb.be/wdtal2/v0/rss/list/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/move/",
    "title": "Move a file to the delete directory (e.g. after a successful import).",
    "name": "PostRSSMove",
    "group": "RSS_Harvester",
    "description": "<p>This method moves a file to the delete directory.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "filename",
            "description": "<p>the name of the file to move.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "files",
            "description": "<p>The list of files in the delete directory.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[\"wd_rss_20161205170752.json\", \"wd_rss_20161205171007.json\", \"wd_test.json\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"filename\": \"wd_rss_20161115164123.json\" }' https://nlp.webdeb.be/wdtal2/v0/rss/move/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/restore/",
    "title": "Restore a file from the delete directory to the original data directory.",
    "name": "PostRSSRestore",
    "group": "RSS_Harvester",
    "description": "<p>This method restores a file that was previously deleted.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "filename",
            "description": "<p>the name of the file to restore.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "files",
            "description": "<p>The list of files in the harvested data directory.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[\"wd_rss_20161205150037.json\", \"wd_rss_20161205150045.json\", \"wd_rss_20161205150112.json\", \"wd_rss_20161205150259.json\", \"wd_rss_20161205150437.json\", \"wd_rss_20161205150444.json\", \"wd_rss_20161205150510.json\", \"wd_rss_20161205150845.json\", \"wd_rss_20161205150910.json\", \"wd_rss_20161205155710.json\", \"wd_rss_20161205160432.json\", \"wd_rss_20161205160851.json\", \"wd_rss_20161205170807.json\", \"wd_rss_20161205170832.json\", \"wd_rss_test.json\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"filename\": \"wd_rss_test.json\" }' https://nlp.webdeb.be/wdtal2/v0/rss/restore/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Harvester"
  },
  {
    "type": "post",
    "url": "/rss/add_source/",
    "title": "Add a new source to the list of RSS feeds to harvest from or overwrite an existing source.",
    "name": "PostRSSAddSource",
    "group": "RSS_Interface",
    "description": "<p>This method adds a new source to the list of RSS feeds to harvest from or overwrites an existing one.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "source_name",
            "description": "<p>the name of the source (e.g. Le Vif)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "source_country",
            "description": "<p>the source's country code (e.g. be, fr, ca, etc. )</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "category",
            "description": "<p>the category of the text according to the source (e.g. actualité, international, politics, sport)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": true,
            "field": "sub_category",
            "description": "<p>the sub-category of the text (e.g. médias, football, olympics)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "genre",
            "description": "<p>the text genre code of the source accoridng to WebDeb (e.g. 3 = journalistic, 5 = opinion)</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "url",
            "description": "<p>the url of the RSS feed</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "usage",
            "description": "<p>the usage according to WebDeb (e.g. 1 = educational)</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "The",
            "description": "<p>added source.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[\"Le Vif\", \"be\", \"international\", \"\", \"journalistic\", \"http://www.levif.be/actualite/photos/feed.rss\", \"accept\", \"private\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"source_name\": \"Le Vif\", \"source_country\": \"be\", \"category\": \"International\", \"genre\": 3, \"url\": \"http://www.levif.be/actualite/feed.rss\", \"usage\": 1 }' https://nlp.webdeb.be/wdtal2/v0/rss/add_source/ (create a new source_id)\ncurl -d '{ \"source_name\": \"Le Vif\", \"source_country\": \"be\", \"category\": \"International\", \"genre\": 3, \"url\": \"http://www.levif.be/actualite/feed.rss\", \"usage\": 1, \"source_id\": 69 }' https://nlp.webdeb.be/wdtal2/v0/rss/add_source/ (overwrite source with source_id 69)",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Interface"
  },
  {
    "type": "post",
    "url": "/rss/list_sources/",
    "title": "Get the list of all existing RSS sources.",
    "name": "PostRSSListSources",
    "group": "RSS_Interface",
    "description": "<p>This method retrieves the list of all existing RSS sources.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>A list of dictionaries containing all exisitng RSS sources (see fields below).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "category",
            "description": "<p>The content type of the source (e.g. actualité, sport, etc.)</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "genre",
            "description": "<p>The WebDeb genre code of the source.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source_name",
            "description": "<p>The name of the source.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "source_country",
            "description": "<p>The code of the country where the source is based (e.g. fr, uk, be)</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "source_id",
            "description": "<p>The source's unique identifier in the list.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "status",
            "description": "<p>The current status for processing the source (accept or ignore).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": true,
            "field": "sub_category",
            "description": "<p>The content sub-type of the source (e.g. politique, football, etc.)</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "url",
            "description": "<p>The URL of the source.</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "usage",
            "description": "<p>The WebDeb usage (visibility) code (e.g. 0, 1, 2)</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[{'category': u'actualit\\xe9', 'genre': 3, 'source_name': u'Le Vif', 'url': u'http://www.levif.be/actualite/feed.rss', 'usage': 1, 'source_id': 1, 'source_country': u'be', 'status': u'accept'}, ..., {'category': u'sports', 'genre': 3, 'source_name': u'Le Devoir', 'url': u'http://www.ledevoir.com/rss/section/sports/soccer.xml', 'usage': 1, 'source_id': 66, 'source_country': u'ca', 'status': u'ignore', 'sub_category': u'soccer'}]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl https://nlp.webdeb.be/wdtal2/v0/rss/list_sources/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Interface"
  },
  {
    "type": "post",
    "url": "/rss/remove_source/",
    "title": "Remove a source from the list of RSS feeds to harvest from.",
    "name": "PostRSSRemoveSource",
    "group": "RSS_Interface",
    "description": "<p>This method removes a new source from the list of RSS feeds to harvest from.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "source_id",
            "description": "<p>the unique integer id of the source to remove.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "The",
            "description": "<p>id of the removed source (-1 if not found).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "12",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"source_id\": 12 }' https://nlp.webdeb.be/wdtal2/v0/rss/remove_source/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Interface"
  },
  {
    "type": "post",
    "url": "/rss/set_source_status/",
    "title": "Set the status for the specified source (toggle whether the source is active for harvesting or ignored).",
    "name": "PostRSSSetSourceStatus",
    "group": "RSS_Interface",
    "description": "<p>This method sets the status of the source to determine whether it is active or ignored during harvesting.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "source_id",
            "description": "<p>the unique integer id of the source to modify.</p>"
          },
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "status",
            "description": "<p>the new status value ('accept' or 'ignore')</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>The updated source entry (the first item is the source_id).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[12, \"Le Vif\", \"be\", \"international\", \"\", 3, \"http://www.levif.be/actualite/photos/feed.rss\", \"accept\", 1]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"source_id\": 12, \"status\": \"accept\" }' https://nlp.webdeb.be/wdtal2/v0/rss/set_source_status/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "RSS_Interface"
  },
  {
    "type": "post",
    "url": "/text/annotate/",
    "title": "Text annotator.",
    "name": "PostTextAnnotate",
    "group": "Text",
    "description": "<p>Annotate a text (e.g. for discourse connectives or named entities).</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>the language of the text. Currently, only English (en) and French (fr) are supported. For Dutch or other languages, you'll have to be patient.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "text",
            "description": "<p>the text to process.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "XML",
            "optional": false,
            "field": "result",
            "description": "<p>An XML string of the annotated text.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (XML)",
          "content": "\n&lt;?xml version='1.0' encoding='utf-8'?&gt;\n&lt;textcontent&gt;\n&lt;sentence&gt;\n&lt;unit subclass=\"PERS\" type=\"entities\"&gt;John&lt;/unit&gt;\n&lt;unit&gt; likes apples, &lt;/unit&gt;\n&lt;unit relations=\"opposition\" type=\"conn\"&gt;however&lt;/unit&gt;\n&lt;unit&gt;, he loves bananas, &lt;/unit&gt;\n&lt;unit relations=\"purpose\" type=\"conn\"&gt;so&lt;/unit&gt;\n&lt;unit&gt; he will buy some, &lt;/unit&gt;\n&lt;unit relations=\"condition\" type=\"conn\"&gt;on the condition that&lt;/unit&gt;\n&lt;unit&gt; the shops are open.&lt;/unit&gt;\n&lt;/sentence&gt;\n&lt;/textcontent&gt;",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "\ncurl -d '{ \"language\": \"en\", \"text\": \"John likes apples, however, he loves bananas, so he will buy some, on the condition that the shops are open.\" }' https://nlp.webdeb.be/wdtal2/v0/text/annotate/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2WebServices/WDTAL2_web_services.py",
    "groupTitle": "Text"
  },
  {
    "type": "post",
    "url": "/twitter/import/",
    "title": "Retrieve a list of tweets that have (hopefully) been converted into new arguments.",
    "name": "PostTwitterImport",
    "group": "Twitter",
    "description": "<p>This method imports a list of tweets that have been (hopefully) converted into new arguments. These tweets require manual validation.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "language",
            "description": "<p>the language of the tweets to retrieve (currently only works for French, 'fr').</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "number",
            "description": "<p>the number of tweets to retrieve (if omitted, all available tweets).</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>A list of processed tweets (new arguments) and all their releveant attributes as a list of JSON dictionary structures. Attributes are described below.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "actor_id",
            "description": "<p>A dictionary containing the speech act classification for this excerpt.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "class",
            "description": "<p>A dictionary containing the speech act classification for this excerpt.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "excerpt",
            "description": "<p>The excerpt (the original tweet).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "id",
            "description": "<p>The id of the tweet.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "language",
            "description": "<p>The two-letter code of the language of the tweet.</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "segmentations",
            "description": "<p>For developement purposes. The output of the hashtag segmentation.</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "shade",
            "description": "<p>The &quot;shade&quot; (nuance) code for the argument.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "standardForm",
            "description": "<p>The normalized form of the argument (tweet).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "time",
            "description": "<p>The publication time of the tweet (parsable time string).</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "topics",
            "description": "<p>The automatically extracted topics for the argument.</p>"
          },
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "transformations",
            "description": "<p>For developement purposes. The output of hashtag topic extraction.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "userid",
            "description": "<p>The Twitter id of the author of the tweet.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "username",
            "description": "<p>The full name (as it is to appear in WebDeb) of the tweet's author.</p>"
          },
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "wdclass",
            "description": "<p>For development purposes. The class of tweet (shows which type of transformations have been applied).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "[{'class': '{\"method\": 0, \"t1\": [[0, 95.1], [3, 1.8], [1, 1.6], [2, 1.5]]}', 'excerpt': u\"De nouveaux droits ont \\xe9t\\xe9 cr\\xe9\\xe9s. Je pense au compte personnel d'activit\\xe9, \\xe0 la garantie jeune,\\xe0 la prime d'activit\\xe9 #Wagram\", 'id': 773828993921478656, 'language': u'fr', 'segmentations': {u'#Wagram': u'Wagram'}, 'shade': 0, 'singular': 1, 'standardForm': u\"De nouveaux droits ont \\xe9t\\xe9 cr\\xe9\\xe9s. Fran\\xe7ois Hollande pense au compte personnel d'activit\\xe9, \\xe0 la garantie jeune, \\xe0 la prime d'activit\\xe9 Wagram\", 'time': u'Thu Sep 08 10:23:03 +0000 2016', 'timing': 1, 'topics': [], 'transformations': ['1ppro', 'hash2words'], 'userid': u'18814998', 'username': u'Fran\\xe7ois Hollande', 'wdclass': 1}, ..., {'class': '{\"method\": 0, \"t1\": [[0, 93.9], [1, 3.3], [2, 1.8], [3, 1.1]]}', 'excerpt': u\"Un pays solide, c'est une nation solidaire. C'est le sens des r\\xe9formes que j'ai conduites depuis 2012 #Wagram\", 'id': 773828825977421829, 'language': u'fr', 'segmentations': {u'#Wagram': u'Wagram'}, 'shade': 0, 'singular': 1, 'standardForm': u\"Un pays solide, c'est une nation solidaire. C'est le sens des r\\xe9formes que j'ai conduites depuis 2012 Wagram\", 'time': u'Thu Sep 08 10:22:23 +0000 2016', 'timing': 1, 'topics': [], 'transformations': ['hash2words'], 'userid': u'18814998', 'username': u'Fran\\xe7ois Hollande', 'wdclass': 1}]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"language\": \"fr\", \"number\": 100 }' https://nlp.webdeb.be/wdtal2/v0/twitter/import/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter"
  },
  {
    "type": "post",
    "url": "/twitter/mark_imported/",
    "title": "Mark a list of tweets as imported (into WebDeb) in the database.",
    "name": "PostTwitterMarkImported",
    "group": "Twitter",
    "description": "<p>This method marks a list of tweets as having been imported (into WebDeb) in the database.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "tweet_ids",
            "description": "<p>the list of ids for the imported tweets.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "-",
            "description": "<p>The number of rows updated in the database.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "3",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"tweet_ids\": [\"503666594049167360\", \"988907770651611\", \"1092838261876376\"] }' https://nlp.webdeb.be/wdtal2/v0/twitter/mark_imported/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter"
  },
  {
    "type": "post",
    "url": "/twitter/reject_tweet/",
    "title": "Mark a tweet as rejected (i.e. not validated for inclusion in WebDeb) in the database.",
    "name": "PostTwitterRejectTweet",
    "group": "Twitter",
    "description": "<p>This method marks a tweet as having been rejected imported (i.e. not validated for inclusion in WebDeb) in the database.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "tweet_id",
            "description": "<p>the id of the rejected tweet.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "-",
            "description": "<p>The number of rows updated in the database (should be 1 if successful, 0 otherwise).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "1",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"tweet_id\": 503666594049167360 }' https://nlp.webdeb.be/wdtal2/v0/twitter/reject_tweet/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter"
  },
  {
    "type": "post",
    "url": "/twitter/stop/",
    "title": "Stop harvesting tweets.",
    "name": "PostTwitterStop",
    "group": "Twitter",
    "description": "<p>This method stops the harvesting of tweets.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "None",
            "optional": false,
            "field": "-",
            "description": "<p>No return value.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (JSON)",
          "content": "None",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl https://nlp.webdeb.be/wdtal2/v0/twitter/stop/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter"
  },
  {
    "type": "post",
    "url": "/twitter/validate_tweet/",
    "title": "Mark a tweet as validated in the database.",
    "name": "PostTwitterValidateTweet",
    "group": "Twitter",
    "description": "<p>This method marks a tweet as validated in the database.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "tweet_id",
            "description": "<p>the id of the tweet to mark as validated.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "arg_id",
            "description": "<p>the id of the validated argument.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "sform",
            "description": "<p>the standard form of the validated argument.</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "class_t1",
            "description": "<p>the speech act class of the validated argument.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "-",
            "description": "<p>The number of rows updated in the database (should be 1 for each successful call, 0 otherwise).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success (Integer)",
          "content": "1",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"tweet_id\": \"503666594049167360\", \"arg_id\": \"1234\", \"sform\": \"François Hollande exprime sa solidarité et sa compassion à la Chancelière Merkel, au peuple allemand et aux familles des victimes de Berlin.\", \"class_t1\": 2 }' https://nlp.webdeb.be/wdtal2/v0/twitter/validate_tweet/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter"
  },
  {
    "type": "post",
    "url": "/twitter/add_account/",
    "title": "Add an Twitter account to harvest tweets from.",
    "name": "PostTwitterAddAccount",
    "group": "Twitter_Interface",
    "description": "<p>This method adds a new account to the list of accounts to harvest from.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "screen_name",
            "description": "<p>the screen name of the Twitter account (e.g. @JLMelenchon).</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "full_name",
            "description": "<p>the full name of the actor as it appears in WebDeb (e.g. Laurette Onkelinx).</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "gender",
            "description": "<p>the actor's gender (f/m).</p>"
          },
          {
            "group": "Parameter",
            "optional": false,
            "field": "webdeb_id",
            "description": "<p>the WebDeb id of the actor.</p>"
          },
          {
            "group": "Parameter",
            "optional": true,
            "field": "languages",
            "description": "<p>the list of languages of the Twitter account (e.g. [fr, en, nl])</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>The new row in the (Excel) table of Twitter accounts (as a list).</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "[\"@JLMelenchon\", \"Jean-Luc Mélenchon\", \"m\", 3099, \"fr\"]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"screen_name\": \"@JLMelenchon\", \"full_name\": \"Jean-Luc Mélenchon\", \"gender\": \"m\", \"webdeb_id\": 3099, \"languages\": [\"fr\"] }' https://nlp.webdeb.be/wdtal2/v0/twitter/add_account/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter_Interface"
  },
  {
    "type": "post",
    "url": "/twitter/delete_account/",
    "title": "Delete a Twitter account from the list of accounts ot harvest from.",
    "name": "PostTwitterDeleteAccount",
    "group": "Twitter_Interface",
    "description": "<p>This method deletes an account from the list of accounts to harvest from.</p>",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "optional": false,
            "field": "screen_name",
            "description": "<p>the screen name of the Twitter account (e.g. @JLMelenchon).</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Integer",
            "optional": false,
            "field": "-",
            "description": "<p>The number of deleted rows.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "1",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d '{ \"screen_name\": \"@JLMelenchon\" }' https://nlp.webdeb.be/wdtal2/v0/twitter/delete_account/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter_Interface"
  },
  {
    "type": "post",
    "url": "/twitter/list_accounts/",
    "title": "List Twitter accounts currently used for harvesting.",
    "name": "PostTwitterListAccounts",
    "group": "Twitter_Interface",
    "description": "<p>This method lists all Twitter accounts currently used for harvesting tweets.</p>",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "List",
            "optional": false,
            "field": "-",
            "description": "<p>A list of Twitter account details represented as dictionaries (see fields below).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "screen_name",
            "description": "<p>The unique Twitter screen name used to identify the account.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "full_name",
            "description": "<p>The account holder's full name (as it appears in WebDeb).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "gender",
            "description": "<p>The gender of the account holder (f/m).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": true,
            "field": "language",
            "description": "<p>The language(s) used in the tweets of this account (e.g. en, fr).</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "webdeb_id",
            "description": "<p>The WebDeb actor id associated with the Twitter account holder.</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Example data on success",
          "content": "curl https://nlp.webdeb.be/wdtal2/v0/twitter/list_accounts/\n[{\"language\": \"fr\", \"gender\": \"m\", \"webdeb_id\": 3701, \"screen_name\": \"@fhollande\", \"full_name\": \"Fran\\u00e7ois Hollande\"}, ..., {\"language\": \"fr\", \"gender\": \"f\", \"webdeb_id\": 82, \"screen_name\": \"@UEfrance\", \"full_name\": \"Commission europ\\u00e9enne\"}]",
          "type": "json"
        }
      ]
    },
    "examples": [
      {
        "title": "Call using curl",
        "content": "curl -d https://nlp.webdeb.be/wdtal2/v0/twitter/list_accounts/",
        "type": "curl"
      }
    ],
    "version": "0.0.0",
    "filename": "/home/andre/PycharmProjects/WDTAL2DataWebServices/WDTAL2_data_web_services.py",
    "groupTitle": "Twitter_Interface"
  }
] });
