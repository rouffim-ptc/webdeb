//
// Copyright 2014-2016 University of Namur (PReCISE) - University of Louvain (Girsef - CENTAL). This is part of WebDeb-web and is free
// software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
// Public License version 3 as published by the Free Software Foundation. It is distributed in the
// hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// 
// See the GNU Lesser General Public License (LGPL) for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with
// WebDeb-web. If not, see <http://www.gnu.org/licenses/>.
//

name := "webdeb web"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  javaWs,
  filters,
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "mysql" % "mysql-connector-java" % "5.1.21",
  "org.julienrf" %% "play-jsmessages" % "2.0.0",
  "net.sf.flexjson" % "flexjson" % "3.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.directory.studio" % "org.apache.commons.io" % "2.4",
  "org.apache.pdfbox" % "pdfbox" % "2.0.3",
  "org.bouncycastle" % "bcprov-jdk16" % "1.45",
  "commons-io" % "commons-io" % "2.4",
  "org.avaje.ebeanorm" % "avaje-ebeanorm-elastic" % "1.1.4",
  "be.objectify" %% "deadbolt-java" % "2.5.0",
  "org.imgscalr" % "imgscalr-lib" % "4.2",
  "com.opencsv" % "opencsv" % "4.0",
  "com.google.code.gson" % "gson" % "2.3.1",
  "org.jsoup" % "jsoup" % "1.11.3",
  "com.optimaize.languagedetector" % "language-detector" % "0.6",
  "com.google.api-client" % "google-api-client-appengine" % "1.30.10",
  "com.google.api-client" % "google-api-client" % "1.30.10"

)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "sbt-plugin-snapshots" at "http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

dependencyOverrides += "org.avaje.ebeanorm" % "avaje-ebeanorm" % "7.12.1"
dependencyOverrides += "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "4.10.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, SonarRunnerPlugin)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

routesGenerator := InjectedRoutesGenerator

fork in run := false
