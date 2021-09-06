# README #
## What is this repository for? ##
This repository holds all resources linked to the **WebDeb** project, a collaborative platform designed to capture debate's arguments with their actors, define relations between them, and run some Natural Language Processing analysis. 
# WebDeb web application
This project is contains all sources of the **WebDeb** project, a collaborative platform designed to capture debate's arguments with their actors, define relations between them, and run some Natural Language Processing analysis.

This project is currently at early development stages, in a beta release.

See main README.md file from root directory of this repository for more details.
It is based on the PlayStartApp (See https://github.com/yesnault/PlayStartApp/tags for download)

A presentation of the project, as well as other resources, can be found on [ResearchGate](https://www.researchgate.net/project/Webdeb-a-collaborative-platform-for-argumentation-visualization).

Version 2.0

## Features
* User sign up, sign in and profile management.
* Contribution management, i.e, add/edit actors, folders, texts, arguments and links between them.
* Browsing and visualization of contributions : search bar (with filters) and graph-based visualizations
* Group management with custom visibility: create, join, validate contribution in groups, etc.
* REST API for browsing and retrieval of contributions

## Project goals ##
The aim of the project is to enable the logical storage of arguments used in any kind of public debate (political, scientific, societal,...). It is based on the fact that any text is made of a complex blend of assertions, which may take the form of a descriptive, prescriptive, appreciative or performative statement. By taking these statements as "basic blocks", we are willing to structure a database in which various objects (statements, actors, references, folders (former themes),...) will be connected.
These arguments and the texts where they come from are linked to one another in folders. Indeed, Folders regroup them under the same theme. Folders are linked each other within a tree hierarchy (ex : Education -> Education in Europe -> Education in Wallonia).
The project aims at developing an application in the form of a collaborative open source platform that will, firstly, display the various positions and justifications of different actors on each assertion and, secondly, that will map the links between assertions.
This platform will be accompanied by various extension and importation plug-ins. In addition to teaching and research, the platform will also serve the general public, organizations, the media and, ultimately, the industrial world.

## How do I get set up? ##

### Summary of set up ###
This project relies on the [Play Framework](https://www.playframework.com/), a full-stack web application container. See *README* file under WebDeb-web folder for more details.
 
### Database configuration ###

The current development relies on MySQL version 5.7. Initialization scripts are specified in project [initialization script](https://bitbucket.org/webdeb/webdeb-sources/src/1d897728e477d4eaeb31d7018a92159435e18b72/webdeb-database/?at=master)

### How to run tests ###

No integration/automated tests are currently defined (under development)

### Deployment instructions ###

See [Play](https://www.playframework.com/documentation/2.5.x/Home) documentation for instructions on *activator* and *sbt* features. The database must be first prepared with needed initialization scripts, available under webdeb-database folder).

## Contribution guidelines ##

To be defined. If you want to contribute, please contact the repository owner.

## System Architecture
The overall system is designed as a three-tier system (presentation layer, core layer and database layer). Every system is specified behind an API and use dependency injections.

Currently, all parts are managed in a play container, but may be easily moved in individual containers by simply changing the implementation and DI binding management.

## Dependencies ###

See LICENSE.txt file

## Main sponsors
* [CENTAL](http://www.uclouvain.be/cental.html)
* [GIRSEF](http://www.uclouvain.be/girsef.html)
* [PReCISE](http://www.unamur.be/en/precise/)
* [Wallonie](http://www.wallonie.be)
* [Bruxelles](http://www.innoviris.be/en)

## Other sponsors (tool and infrastructure help)
* [Atlassian](https://www.atlassian.com/)
* [JetBRAINS](https://www.jetbrains.com/)

## Contacts
Martin Rouffiange (martin.rouffiange@uclouvain.be)

## Licences
See LICENSE.txt file
